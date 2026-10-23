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
                                        val retry=DraftNameAtlas.retryUnresolved(atlas,missing)
                                        try {
                                            File(evidence,"${case.getString("id")}-retry-atlas.png").outputStream().use {
                                                retry.bitmap.compress(Bitmap.CompressFormat.PNG,100,it)
                                            }
                                            val reread=recognizer.process(com.google.mlkit.vision.common.InputImage.fromBitmap(retry.bitmap,0)).await()
                                            result.put("diagnosticRetryText",reread.text)
                                            result.put("diagnosticRetryLines",JSONArray(reread.textBlocks.flatMap { it.lines }.map {
                                                JSONObject().put("text",it.text).put("bounds",it.boundingBox.toString())
                                            }))
                                        } finally { retry.bitmap.recycle() }
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
                                repeat(2) { visual = scanner.scanActiveSlotDirectly(frame, turn, context) }
                                val expectedLast = ids[positions.last()]
                                if (visual?.champion?.id != expectedLast) errors += "tenth: expected=$expectedLast actual=${visual?.champion?.id}"
                                result.put("tenth", JSONObject().put("expected", expectedLast)
                                    .put("recognized", visual?.champion?.id ?: JSONObject.NULL)
                                    .put("confirmed", LiteRTVisionClassifier.reportFlow.value.isConfirmed)
                                    .put("nameHidden", true))
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
                    } } finally { sequenceSource.recycle() }
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
