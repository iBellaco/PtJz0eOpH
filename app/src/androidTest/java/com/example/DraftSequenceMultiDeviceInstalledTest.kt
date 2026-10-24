package com.example

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.model.LaneRole
import com.example.service.screen.*
import java.io.File
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.tasks.await

/** Real OCR of resized screenshot pixels, with synthetic successive title visibility. No mock recognizer. */
@RunWith(AndroidJUnit4::class)
class DraftSequenceMultiDeviceInstalledTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val scanner get() = DraftVisionScanner
    private val centers = listOf(110f, 188f, 265f, 343f, 420f, 110f, 188f, 265f, 342f, 419f)
    private val roles = listOf(LaneRole.ADC, LaneRole.TOP, LaneRole.MID, LaneRole.JUNGLE, LaneRole.SUPPORT)

    @Test fun firstNineNamesThenOnlyTheTenthPortraitAtEveryPlannedGeometry() = kotlinx.coroutines.runBlocking<Unit> {
        val shard = InstrumentationRegistry.getArguments().getString("sequenceShard")!!.toInt()
        val preflight = InstrumentationRegistry.getArguments().getString("sequencePreflight")=="true"
        val stages = if(preflight) listOf(0,9) else (0..9).toList()
        val plan = instrumentation.context.assets.open("scanner-sequence-plan.json").bufferedReader().use { JSONObject(it.readText()) }
        val cases = plan.getJSONArray("cases")
        val bytes = instrumentation.context.assets.open("portraits/draft-ten-selected.jpg").use { it.readBytes() }
        val hash = java.security.MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
        assertTrue("Unexpected fixture", hash == plan.getString("sourceSha256"))
        val source = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)!!
        val previous = scanner.calibrationConfig
        val failures = mutableListOf<String>()
        val evidence = File(context.getExternalFilesDir(null), if(preflight) "scanner-sequence-preflight" else "scanner-sequence-audit").apply { mkdirs() }
        try {
            scanner.overlayRect = null
            scanner.debugTextRects = emptyList()
            for (index in 0 until cases.length()) {
                val case = cases.getJSONObject(index)
                if (if(preflight) !case.getBoolean("preflight") else case.getInt("shard")!=shard) continue
                val result = JSONObject(case.toString()).put("evidenceType", "installed-native-ocr-resized-fixture-not-physical-model")
                    .put("validationMode",if(preflight) "preflight-not-release" else "full")
                val checks = JSONArray()
                val errors = mutableListOf<String>()
                val started = android.os.SystemClock.elapsedRealtime()
                try {
                    val order = case.getBoolean("firstPick")
                    val ids = DraftSequenceFixture.ids(order)
                    val sequenceSource = DraftSequenceFixture.create(source, order)
                    // Independent explicit game order; do not derive the expected sequence from the implementation.
                    val positions = if (order) listOf(0, 5, 6, 1, 2, 7, 8, 3, 4, 9)
                        else listOf(5, 0, 1, 6, 7, 2, 3, 8, 9, 4)
                    scanner.resetSlotMemory()
                    context.getSharedPreferences("vision_calibration_prefs", 0).edit().clear().commit()
                    scanner.initCalibration(context)
                    // Model-specific auto-alignment was measured independently by the production
                    // geometry matrix. An emulator's Build.MODEL cannot impersonate a physical POCO.
                    val measured = case.getJSONObject("calibration")
                    fun floats(key: String): List<Float> = measured.getJSONArray(key).let { values ->
                        List(values.length()) { values.getDouble(it).toFloat() }
                    }
                    val allyXs = floats("allySlotXRatios")
                    val enemyXs = floats("enemySlotXRatios")
                    val config = VisionCalibrationConfig(nameBandScale = measured.getDouble("nameBandScale").toFloat(),
                        allyAvatarCenterX = allyXs[0], enemyAvatarCenterX = enemyXs[0],
                        allySlotXRatios = allyXs, enemySlotXRatios = enemyXs,
                        allySlotYRatios = floats("allySlotYRatios"), enemySlotYRatios = floats("enemySlotYRatios"),
                        allySlotDiameterRatios = floats("allySlotDiameterRatios"), enemySlotDiameterRatios = floats("enemySlotDiameterRatios"))
                    scanner.updateCalibration(context, config)
                    try { for (step in stages) {
                        val visible = positions.take(step).toSet()
                        val staged = stage(sequenceSource, visible, case.getString("language"))
                        val frame = resize(staged, case)
                        staged.recycle()
                        try {
                            val expected = visible.associateWith { ids[it] }
                            var read: DraftScanResult? = null
                            val readings = if (step == 0) 1 else 2
                            repeat(readings) {
                                read = kotlinx.coroutines.withTimeout(60_000) {
                                    scanner.scanDraftFromBitmap(frame, context, order, LaneRole.MID, namesOnly = true)
                                }
                                if (read?.isSuccessful != true) errors += "step=$step: OCR request failed"
                                if (it == 0 && step > 0) {
                                    val unconfirmed = (if (positions[step - 1] < 5) read!!.alliesBySlot else read!!.enemiesBySlot)[positions[step - 1] % 5]
                                    if (unconfirmed != null) errors += "step=$step: a single new title was prematurely confirmed"
                                }
                            }
                            val actual = read!!.alliesBySlot.mapKeys { it.key }.mapValues { it.value.id } +
                                read!!.enemiesBySlot.mapKeys { it.key + 5 }.mapValues { it.value.id }
                            if (actual != expected) errors += "step=$step: expected=$expected actual=$actual"
                            val actualRoles = read!!.allyRolesBySlot
                            roles.forEachIndexed { slot, role ->
                                if (actualRoles[slot] != role) errors += "step=$step: observed lane $slot expected=$role actual=${actualRoles[slot]}"
                            }
                            if (errors.isNotEmpty() && !result.has("firstFailureStage")) {
                                result.put("firstFailureStage", step)
                                val atlas = DraftNameAtlas.create(frame, scanner.alignedConfigFlow.value, emptyList())
                                val recognizer = com.google.mlkit.vision.text.TextRecognition.getClient(
                                    com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS)
                                try {
                                    File(evidence, "${case.getString("id")}-first-failure.png").outputStream().use {
                                        frame.compress(Bitmap.CompressFormat.PNG, 100, it)
                                    }
                                    File(evidence, "${case.getString("id")}-title-atlas.png").outputStream().use {
                                        atlas.bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                                    }
                                    val text = recognizer.process(com.google.mlkit.vision.common.InputImage.fromBitmap(atlas.bitmap, 0)).await()
                                    result.put("diagnosticOcrText", text.text)
                                    val missing=atlas.tiles.filter { tile -> tile.original &&
                                        !actual.containsKey(tile.slot+if(tile.ally) 0 else 5) }
                                    if(missing.isNotEmpty()) {
                                        val detectedLines=mutableMapOf<Pair<Boolean,Int>,MutableList<Rect>>()
                                        fun collectLines(input:DraftNameAtlas,read:com.google.mlkit.vision.text.Text) {
                                            for(original in missing) for(tile in input.tiles.filter { it.ally==original.ally && it.slot==original.slot })
                                                for(line in read.textBlocks.flatMap { it.lines }) {
                                                    val box=line.boundingBox ?: continue
                                                    if(!tile.region.contains(box.centerX(),box.centerY())) continue
                                                    val y=box.centerY()-tile.region.top
                                                    if(y<tile.region.height()*.30f || y>minOf(tile.titleMaxY,(tile.region.height()*.85f).toInt())) continue
                                                    val mapped=DraftNameAtlas.mapDetectedLine(original,tile,box)
                                                    if(mapped.width()>0 && mapped.height()>0)
                                                        detectedLines.getOrPut(original.ally to original.slot) { mutableListOf() }.add(mapped)
                                                }
                                        }
                                        collectLines(atlas,text)
                                        val retry=DraftNameAtlas.retryUnresolved(atlas,missing)
                                        try {
                                            File(evidence,"${case.getString("id")}-retry-atlas.png").outputStream().use {
                                                retry.bitmap.compress(Bitmap.CompressFormat.PNG,100,it)
                                            }
                                            val reread=recognizer.process(com.google.mlkit.vision.common.InputImage.fromBitmap(retry.bitmap,0)).await()
                                            collectLines(retry,reread)
                                            result.put("diagnosticRetryText",reread.text)
                                            result.put("diagnosticRetryLines",JSONArray(reread.textBlocks.flatMap { it.lines }.map {
                                                JSONObject().put("text",it.text).put("bounds",it.boundingBox.toString())
                                            }))
                                        } finally { retry.bitmap.recycle() }
                                        val focusedDiagnostics=JSONArray()
                                        for(tile in missing.filter { expected.containsKey(it.slot+if(it.ally) 0 else 5) }) {
                                            val bounds=DraftNameAtlas.titleFocusBounds(atlas,tile) ?: continue
                                            val focused=DraftNameAtlas.focusTitle(atlas,tile,bounds)
                                            try {
                                                File(evidence,"${case.getString("id")}-focused-${tile.ally}-${tile.slot}.png").outputStream().use {
                                                    focused.bitmap.compress(Bitmap.CompressFormat.PNG,100,it)
                                                }
                                                val text=recognizer.process(com.google.mlkit.vision.common.InputImage.fromBitmap(focused.bitmap,0)).await()
                                                collectLines(focused,text)
                                                focusedDiagnostics.put(JSONObject().put("ally",tile.ally).put("slot",tile.slot)
                                                    .put("text",text.text).put("bounds",bounds.toString())
                                                    .put("lines",JSONArray(text.textBlocks.flatMap { it.lines }.map {
                                                        JSONObject().put("text",it.text).put("bounds",it.boundingBox.toString())
                                                    })))
                                            } finally { focused.bitmap.recycle() }
                                            val color=DraftNameAtlas.focusTitle(atlas,tile,bounds,colorOnly=true)
                                            try {
                                                File(evidence,"${case.getString("id")}-color-${tile.ally}-${tile.slot}.png").outputStream().use {
                                                    color.bitmap.compress(Bitmap.CompressFormat.PNG,100,it)
                                                }
                                                val read=recognizer.process(com.google.mlkit.vision.common.InputImage.fromBitmap(color.bitmap,0)).await()
                                                focusedDiagnostics.put(JSONObject().put("ally",tile.ally).put("slot",tile.slot)
                                                    .put("variant","independent-color").put("text",read.text)
                                                    .put("lines",JSONArray(read.textBlocks.flatMap { it.lines }.map {
                                                        JSONObject().put("text",it.text).put("bounds",it.boundingBox.toString())
                                                    })))
                                            } finally { color.bitmap.recycle() }
                                        }
                                        result.put("focusedDiagnostics",focusedDiagnostics)
                                        val lineDiagnostics=JSONArray()
                                        for(tile in missing.filter { expected.containsKey(it.slot+if(it.ally) 0 else 5) }) {
                                            val bounds=DraftNameAtlas.titleFocusBounds(atlas,tile) ?: continue
                                            val candidates=detectedLines[tile.ally to tile.slot].orEmpty().distinct().take(3)+
                                                Rect(bounds.left,(tile.region.height()*.30f).toInt(),bounds.right,
                                                    minOf(tile.titleMaxY+(tile.region.height()*.12f).toInt(),(tile.region.height()*.90f).toInt()))
                                            for((candidateIndex,line) in candidates.withIndex()) {
                                                val focused=DraftNameAtlas.focusDetectedLine(atlas,tile,line)
                                                try {
                                                    File(evidence,"${case.getString("id")}-line-${tile.ally}-${tile.slot}-$candidateIndex.png").outputStream().use {
                                                        focused.bitmap.compress(Bitmap.CompressFormat.PNG,100,it)
                                                    }
                                                    val read=recognizer.process(com.google.mlkit.vision.common.InputImage.fromBitmap(focused.bitmap,0)).await()
                                                    lineDiagnostics.put(JSONObject().put("ally",tile.ally).put("slot",tile.slot)
                                                        .put("bounds",line.toString()).put("text",read.text))
                                                } finally { focused.bitmap.recycle() }
                                            }
                                        }
                                        result.put("lineFocusedDiagnostics",lineDiagnostics)
                                    }
                                } finally { recognizer.close(); atlas.bitmap.recycle() }
                            }
                            val snapshot = scanner.calibrationReportSnapshot()
                            if (snapshot.width != frame.width || snapshot.height != frame.height ||
                                snapshot.config != scanner.alignedConfigFlow.value) errors += "step=$step: viewer/capture mismatch"
                            checks.put(JSONObject().put("step", step).put("frames", readings)
                                .put("expected", JSONObject(expected.mapKeys { it.key.toString() }))
                                .put("recognized", JSONObject(actual.mapKeys { it.key.toString() }))
                                .put("roles", JSONObject(actualRoles.mapKeys { it.key.toString() }.mapValues { it.value.name })))
                            if (step == 9) {
                                val turn = scanner.getDraftPickSequence(order).last()
                                if (turn.isAlly != !order || turn.slotIndex != 4) errors += "incorrect tenth position"
                                var visual: ScannedSlotInfo? = null
                                val visualFrames=JSONArray()
                                repeat(2) {
                                    visual = scanner.scanActiveSlotDirectly(frame, turn, context)
                                    val report=LiteRTVisionClassifier.reportFlow.value
                                    visualFrames.put(JSONObject().put("candidate",report.pickedChampion?.id?:JSONObject.NULL)
                                        .put("score",report.topCandidates.firstOrNull()?.similarityScore?:JSONObject.NULL)
                                        .put("frames",report.stableFramesCount).put("confirmed",report.isConfirmed)
                                        .put("processingMs",report.inferenceTimeMs).put("reason",report.decisionReason))
                                }
                                val expectedLast = ids[positions.last()]
                                if (visual?.champion?.id != expectedLast) errors += "tenth: expected=$expectedLast actual=${visual?.champion?.id}"
                                result.put("tenth", JSONObject().put("expected", expectedLast)
                                    .put("recognized", visual?.champion?.id ?: JSONObject.NULL)
                                    .put("confirmed", LiteRTVisionClassifier.reportFlow.value.isConfirmed)
                                    .put("nameHidden", true).put("observations",visualFrames))
                                if (!LiteRTVisionClassifier.reportFlow.value.isConfirmed) errors += "tenth: not confirmed"
                                val canvas = Canvas(frame)
                                val paint = Paint().apply { color = Color.GREEN; style = Paint.Style.STROKE; strokeWidth = 2f }
                                for (ally in listOf(true, false)) for (slot in 0..4) {
                                    canvas.drawRect(AdaptiveScreenLayoutEngine.calculateSlotNameRect(frame.width, frame.height, ally, slot, snapshot.config), paint)
                                    canvas.drawRect(AdaptiveScreenLayoutEngine.calculateSlotCropRect(frame.width, frame.height, ally, slot, snapshot.config), paint)
                                }
                                File(evidence, "${case.getString("id")}.png").outputStream().use { frame.compress(Bitmap.CompressFormat.PNG, 100, it) }
                            }
                        } finally { frame.recycle() }
                    }
                    // Real captures complement the staged order test: no drawn
                    // lane labels, transplanted names or expected OCR injections.
                    // In preflight retain the two known phone priors; the full
                    // matrix must exercise these three captures at every geometry.
                    if(!preflight || case.getJSONArray("profiles").toString().let {
                        it.contains("red-magic-reference") || it.contains("poco-x7-pro") }) {
                        val realChecks=JSONArray()
                        for(fixture in DraftRealCaptureFixtures.captures) {
                            scanner.resetSlotMemory()
                            val rawBytes=instrumentation.context.assets.open("portraits/${fixture.file}").use { it.readBytes() }
                            val rawHash=java.security.MessageDigest.getInstance("SHA-256").digest(rawBytes)
                                .joinToString("") { "%02x".format(it) }
                            check(rawHash==fixture.sha256)
                            val raw=BitmapFactory.decodeByteArray(rawBytes,0,rawBytes.size)!!
                            val realFrame=try { resize(raw,case) } finally { raw.recycle() }
                            try {
                                var realRead:DraftScanResult?=null
                                repeat(2) { frameIndex ->
                                    realRead=kotlinx.coroutines.withTimeout(60_000) {
                                        scanner.scanDraftFromBitmap(realFrame,context,fixture.firstPick,LaneRole.MID,namesOnly=true)
                                    }
                                    if(frameIndex==0 && (realRead!!.alliesBySlot.isNotEmpty() || realRead!!.enemiesBySlot.isNotEmpty()))
                                        errors+="${fixture.file}: names confirmed before the second frame"
                                }
                                val actual=realRead!!.alliesBySlot.mapKeys { it.key }.mapValues { it.value.id }+
                                    realRead!!.enemiesBySlot.mapKeys { it.key+5 }.mapValues { it.value.id }
                                if(actual!=fixture.titles) errors+="${fixture.file}: expected=${fixture.titles} actual=$actual"
                                fixture.remainingAllyLane?.let { lane ->
                                    if(realRead!!.allyRolesBySlot[4]!=lane) errors+="${fixture.file}: observed last ally lane must be $lane"
                                }
                                val turn=scanner.getDraftPickSequence(fixture.firstPick).last()
                                var visual:ScannedSlotInfo?=null
                                if(fixture.titles.size<9 || fixture.tenth!=null) {
                                    repeat(2) { visual=scanner.scanActiveSlotDirectly(realFrame,turn,context) }
                                    if(fixture.titles.size<9 && visual!=null) errors+="${fixture.file}: visual pick before nine names"
                                    if(fixture.tenth!=null && visual?.champion?.id!=fixture.tenth)
                                        errors+="${fixture.file}: tenth expected=${fixture.tenth} actual=${visual?.champion?.id}"
                                }
                                realChecks.put(JSONObject().put("file",fixture.file).put("sha256",rawHash)
                                    .put("expectedTitles",JSONObject(fixture.titles.mapKeys { it.key.toString() }))
                                    .put("recognizedTitles",JSONObject(actual.mapKeys { it.key.toString() }))
                                    .put("frames",2).put("tenthExpected",fixture.tenth?:JSONObject.NULL)
                                    .put("tenthRecognized",visual?.champion?.id?:JSONObject.NULL)
                                    .put("visualCheck",if(fixture.titles.size<9) "blocked-before-nine" else
                                        if(fixture.tenth!=null) "required" else "not-applicable"))
                            } finally { realFrame.recycle() }
                        }
                        result.put("realCaptures",realChecks)
                    }
                    } finally { sequenceSource.recycle() }
                } catch (failure: Exception) {
                    errors += "${failure.javaClass.simpleName}: ${failure.message}"
                } finally {
                    result.put("steps", checks).put("failures", JSONArray(errors))
                        .put("status", if (errors.isEmpty() && checks.length() == stages.size) "PASS" else "FAIL")
                        .put("elapsedMs", android.os.SystemClock.elapsedRealtime() - started)
                    instrumentation.sendStatus(2, Bundle().apply { putString("stream", "\nCOACH_SEQUENCE_CASE: $result\n") })
                    failures += errors.map { "${case.getString("id")}: $it" }
                }
            }
        } finally { source.recycle(); scanner.resetSlotMemory(); scanner.updateCalibration(context, previous) }
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }

    /** Mask only unselected titles. Real selected champion pixels and all preview portraits stay intact. */
    private fun stage(source: Bitmap, visible: Set<Int>, language: String): Bitmap = source.copy(Bitmap.Config.ARGB_8888, true).also { frame ->
        val canvas = Canvas(frame)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK }
        val labels = if (language == "es") listOf("CALLE DEL DRAGÓN", "CALLE DEL BARÓN", "CALLE CENTRAL", "JUNGLA", "APOYO")
            else listOf("ROTA DO DRAGÃO", "ROTA DO BARÃO", "ROTA DO MEIO", "SELVA", "SUPORTE")
        for (slot in 0..9) if (slot !in visible) {
            val ally = slot < 5
            val x = if (ally) 130f else 1040f
            val right = if (ally) 310f else 1194f
            paint.color = Color.BLACK
            canvas.drawRect(x, centers[slot] - 29f, right, centers[slot] + 1f, paint)
            if (ally) {
                paint.color = Color.WHITE
                paint.textSize = 11f
                paint.typeface = Typeface.DEFAULT_BOLD
                canvas.drawText(labels[slot], 134f, centers[slot] - 10f, paint)
            }
        }
    }

    private fun resize(source: Bitmap, case: JSONObject): Bitmap {
        val capture = case.getJSONArray("capture")
        val width = capture.getInt(0)
        val height = capture.getInt(1)
        val frame = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val insets = case.getJSONArray("safeInsets")
        val available = Rect((width*insets.getDouble(0)).toInt(),(height*insets.getDouble(1)).toInt(),
            width-(width*insets.getDouble(2)).toInt(),height-(height*insets.getDouble(3)).toInt())
        val edge = case.getString("layout") == "synthetic-edge-anchored-side-panels"
        val scale = if (edge) height.toFloat() / source.height else minOf(available.width().toFloat() / source.width, available.height().toFloat() / source.height)
        val canvas = Canvas(frame)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
        canvas.drawColor(if (edge) Color.rgb(10, 15, 30) else Color.BLACK)
        if (edge) {
            canvas.drawBitmap(source, Rect(0, 0, 280, source.height), Rect(0, 0, (280 * scale).toInt(), height), paint)
            canvas.drawBitmap(source, Rect(1040, 0, source.width, source.height), Rect(width - (240 * scale).toInt(), 0, width, height), paint)
        } else {
            val w = (source.width * scale).toInt()
            val h = (source.height * scale).toInt()
            canvas.drawBitmap(source,null,Rect(available.left+(available.width()-w)/2,available.top+(available.height()-h)/2,
                available.left+(available.width()-w)/2+w,available.top+(available.height()-h)/2+h),paint)
        }
        return frame
    }
}
