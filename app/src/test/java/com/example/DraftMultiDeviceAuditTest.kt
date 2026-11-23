package com.example

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.example.data.WildRiftRepository
import com.example.service.screen.*
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowBuild
import java.io.File
import kotlin.math.hypot

internal data class OverlayDisplay(val id: String, val manufacturer: String, val model: String, val width: Int, val height: Int,
    val edgeAnchored: Boolean = false, val specification: String? = null,
    val safeInsets: List<Float> = listOf(0f, 0f, 0f, 0f))

internal val reportedOverlayDisplays = requireNotNull(OverlayDisplay::class.java.getResourceAsStream("/scanner/overlay-displays.csv"))
    .bufferedReader().useLines { lines ->
        lines.drop(1).filter { it.isNotBlank() }.map { line ->
            val fields = line.split(',', limit = 6)
            require(fields.size == 6) { line }
            OverlayDisplay(fields[0], fields[1], fields[2], fields[3].toInt(), fields[4].toInt(), specification = fields[5])
        }.toList()
    }

/** Real production capture limits, measured fixture coordinates and identity checks, not hardware certification. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DraftMultiDeviceAuditTest {
    private val variants = listOf("original", "changed-background", "changed-portraits-and-bans")
    private val picks = listOf("senna", "ornn", "mel", "volibear", "vi", "alistar", "caitlyn", "nocturne", "vex", "teemo")
    private val bans = listOf("mordekaiser", "kayn", "jhin", "gwen", "hwei", "mordekaiser", "syndra", "cho_gath", "hwei", "soraka")
    // Independently measured in the owner's 1280 x 579 image, not derived from calibration defaults.
    private val pickCenters = listOf(92f to 110f, 92f to 188f, 92f to 265f, 92f to 343f, 92f to 420f,
        1227f to 110f, 1227f to 188f, 1227f to 265f, 1227f to 342f, 1227f to 419f)
    private val banCenters = listOf(35f, 82f, 129f, 175f, 221f, 1059f, 1105f, 1151f, 1197f, 1244f)
    private val output = File("build/reports/scanner-multidevice").apply { mkdirs() }

    @Test fun reportedDevicesRecognizeAllPicksAndBans() = audit(reportedOverlayDisplays, "device-profiles")

    // Exercise the adaptive path on display sizes observed during play. The fixture moves
    // inside that frame, so it must not load the owner's frame-relative manual calibration.
    @Test fun formerlyFailingCentersStayAlignedWhenPortraitsChange() = audit(
        reportedOverlayDisplays.distinctBy { it.width to it.height }.map { profile ->
            profile.copy(id = "edge-${profile.id}", manufacturer = "Android", model = "adaptive",
                edgeAnchored = true)
        }, "center-regressions")

    @Test fun smallCapturePreservesJhinWhenPortraitsAndBansChange() = audit(
        listOf(OverlayDisplay("native-reference-jhin", "Android", "shared-draft-capture", 1280, 579)),
        "small-jhin-regression")

    @Test fun landscapeCutoutAndNavigationSafeAreasUseTheObservedGameViewport() = audit(
        reportedOverlayDisplays.distinctBy { it.width to it.height }.flatMap { profile ->
            listOf("left", "right").map { side ->
                profile.copy(id = "safe-${profile.id}-$side", manufacturer = "Android", model = "adaptive",
                    safeInsets = if (side == "left") listOf(.05f, 0f, 0f, .03f) else listOf(0f, 0f, .05f, .03f))
            }
        }, "cutout-safe-area-scenarios")

    private fun audit(cases: List<OverlayDisplay>, group: String) {
        val context = RuntimeEnvironment.getApplication()
        val prefs = context.getSharedPreferences("vision_calibration_prefs", 0)
        WildRiftRepository.initChampions(context, forceReload = true)
        val source = javaClass.getResourceAsStream("/portraits/draft-ten-selected.jpg").use { BitmapFactory.decodeStream(it)!! }
        val results = JSONArray()
        val failures = mutableListOf<String>()
        val previousOverlay = DraftVisionScanner.overlayRect
        val previousDiagnostics = DraftVisionScanner.showCalibrationBoxes.value
        try {
            DraftVisionScanner.overlayRect = null
            DraftVisionScanner.showCalibrationBoxes.value = false
            for (profile in cases) for (variant in variants.indices) {
                val caseId = "${profile.id}-${variants[variant]}"
                val variantSource = referenceVariant(source, variant)
                prefs.edit().clear().commit()
                ShadowBuild.setManufacturer(profile.manufacturer)
                ShadowBuild.setModel(profile.model)
                DraftVisionScanner.initCalibration(context)
                val (w, h) = CaptureFrameDecoder.captureSize(profile.width, profile.height)
                val frame = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val available = Rect((w*profile.safeInsets[0]).toInt(),(h*profile.safeInsets[1]).toInt(),
                    w-(w*profile.safeInsets[2]).toInt(),h-(h*profile.safeInsets[3]).toInt())
                val scale = if (profile.edgeAnchored) h.toFloat() / source.height
                    else minOf(available.width().toFloat() / source.width, available.height().toFloat() / source.height)
                val rw = (source.width * scale).toInt()
                val rh = (source.height * scale).toInt()
                val viewport = if (profile.edgeAnchored) Rect(0, 0, w, h)
                    else Rect(available.left+(available.width()-rw)/2,available.top+(available.height()-rh)/2,
                        available.left+(available.width()-rw)/2+rw,available.top+(available.height()-rh)/2+rh)
                fun targetX(x: Float): Float = if (profile.edgeAnchored) {
                    if (x < source.width / 2f) x * scale else w - (source.width - x) * scale
                } else viewport.left + x * viewport.width() / source.width
                fun targetY(y: Float): Float = viewport.top + y * viewport.height() / source.height
                val entry = JSONObject().put("profile", profile.id).put("case", caseId)
                    .put("variant", variants[variant]).put("model", profile.model)
                    .put("manufacturer", profile.manufacturer)
                    .put("specification", profile.specification ?: JSONObject.NULL)
                    .put("evidenceType", "resized-reference-not-physical-device")
                    .put("layout", if (profile.edgeAnchored) "synthetic-edge-anchored-side-panels" else "uniform-fit-reference")
                    .put("display", JSONArray(listOf(profile.width, profile.height)))
                    .put("capture", JSONArray(listOf(w, h)))
                    .put("safeInsets",JSONArray(profile.safeInsets))
                    .put("viewport", JSONArray(listOf(viewport.left, viewport.top, viewport.right, viewport.bottom)))
                val checks = JSONArray()
                val before = failures.size
                try {
                    Canvas(frame).apply {
                        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
                        if (profile.edgeAnchored) {
                            drawColor(Color.rgb(10, 15, 30))
                            drawBitmap(variantSource, Rect(0, 0, 280, source.height), Rect(0, 0, (280*scale).toInt(), h), paint)
                            drawBitmap(variantSource, Rect(1040, 0, source.width, source.height), Rect(w-(240*scale).toInt(), 0, w, h), paint)
                        } else {
                            drawColor(Color.BLACK)
                            drawBitmap(variantSource, null, viewport, paint)
                        }
                    }
                    val detected = DraftFrameGeometry.contentBounds(frame)
                    if (viewport != detected) failures += "${profile.id}: viewport $detected expected $viewport"
                    val config = DraftVisionScanner.configForFrame(frame)
                    entry.put("calibration", JSONObject()
                        .put("nameBandScale", config.nameBandScale)
                        .put("allySlotXRatios", JSONArray(config.allySlotXRatios))
                        .put("enemySlotXRatios", JSONArray(config.enemySlotXRatios))
                        .put("allySlotYRatios", JSONArray(config.allySlotYRatios))
                        .put("enemySlotYRatios", JSONArray(config.enemySlotYRatios))
                        .put("allySlotDiameterRatios", JSONArray(config.allySlotDiameterRatios))
                        .put("enemySlotDiameterRatios", JSONArray(config.enemySlotDiameterRatios)))
                    if (config != DraftVisionScanner.alignedConfigFlow.value) failures += "${profile.id}: viewer configuration differs from scanner"
                    val measured = DraftVisionScanner.calibrationReportSnapshot()
                    if (measured.width != w || measured.height != h || measured.config != config)
                        failures += "${profile.id}: published capture dimensions/configuration differ"
                    for (index in picks.indices) {
                        val ally = index < 5
                        val slot = index % 5
                        val rect = AdaptiveScreenLayoutEngine.calculateSlotCropRect(w, h, ally, slot, config)
                        val expectedX = targetX(pickCenters[index].first)
                        val expectedY = targetY(pickCenters[index].second)
                        val referenceError = hypot(rect.exactCenterX()-expectedX, rect.exactCenterY()-expectedY) * source.height / viewport.height()
                        val crop = AdaptiveScreenLayoutEngine.extractSlotAvatarBitmap(frame, w, h, ally, slot, config)!!
                        try {
                            val ranked = LiteRTVisionClassifier.rankSlotPortrait(crop, ally, context)
                            val identity = ranked.firstOrNull()?.first?.id
                            val accepted = ranked.size >= 2 && PortraitMatcher.accepts(ranked[0].second, ranked[1].second, .80f)
                            val expected = picks[permutedIndex(index, variant)]
                            var confirmed: String? = null
                            kotlinx.coroutines.runBlocking {
                                LiteRTVisionClassifier.reset()
                                LiteRTVisionClassifier.setThreshold(.80f, context)
                                repeat(2) { confirmed = LiteRTVisionClassifier.executeTenthPickInference(
                                    crop, ally, emptySet(), 9, slot, context)?.first?.id }
                            }
                            if (identity != expected || !accepted || confirmed != expected)
                                failures += "$caseId: slot $index expected $expected got ${ranked.take(2).map { it.first.id to it.second }}, confirmed=$confirmed"
                            if (referenceError > 6f) failures += "${profile.id}: slot $index center error $referenceError reference pixels"
                            if (crop.width != rect.width() || crop.height != rect.height()) failures += "${profile.id}: crop and viewer size mismatch $index"
                            checks.put(JSONObject().put("slot", index).put("expected", expected).put("recognized", identity)
                                .put("confirmed", confirmed).put("stableFramesExercised", 2)
                                .put("score", ranked.first().second).put("runnerUpScore", ranked[1].second)
                                .put("accepted", accepted).put("centerErrorReferencePixels", referenceError)
                                .put("crop", JSONArray(listOf(rect.left, rect.top, rect.right, rect.bottom))))
                        } finally { crop.recycle() }
                    }
                    val tracker = BannedPortraitScanner()
                    repeat(2) { tracker.scan(frame, context, it * 1500L, config = config) }
                    val expectedBans = bans.indices.map { bans[permutedIndex(it, variant)] }
                    if (tracker.slots.map { it.championId } != expectedBans || tracker.slots.any { it.state != BanState.CONFIRMED })
                        failures += "$caseId: bans expected $expectedBans got ${tracker.slots.map { it.championId }}"
                    tracker.slots.forEachIndexed { index, slot ->
                        val region = slot.region
                        if (region != null) {
                            val expectedX = targetX(banCenters[index])
                            val expectedY = targetY(30f)
                            val error = hypot(region.x*w-expectedX, region.y*h-expectedY) * source.height / viewport.height()
                            if (error > 6f) failures += "${profile.id}: ban $index center error $error reference pixels"
                            checks.put(JSONObject().put("ban", index).put("expected", expectedBans[index]).put("recognized", slot.championId)
                                .put("state", slot.state.name).put("scanFramesExercised", 2)
                                .put("centerErrorReferencePixels", error))
                        }
                    }
                    saveEvidence(frame, config, tracker.slots, caseId)
                } finally {
                    frame.recycle()
                    if (variantSource !== source) variantSource.recycle()
                    entry.put("checks", checks).put("status", if (failures.size == before) "PASS" else "FAIL")
                    results.put(entry)
                    File(output, "$group.json").writeText(JSONObject().put("source", "draft-ten-selected.jpg")
                        .put("sourceSha256", "42661aefae3df52b4f72047c039120f72f4ea351139d7bdb5107351fe8d3342a")
                        .put("profiles", results).put("failures", JSONArray(failures)).toString(2))
                }
            }
        } finally {
            source.recycle()
            prefs.edit().clear().commit()
            ShadowBuild.reset()
            DraftVisionScanner.overlayRect = previousOverlay
            DraftVisionScanner.showCalibrationBoxes.value = previousDiagnostics
            DraftVisionScanner.initCalibration(context)
        }
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }

    private fun permutedIndex(index: Int, variant: Int): Int = if (variant != 2) index
        else index / 5 * 5 + (index % 5 + if (index < 5) 1 else 2) % 5

    /** Synthetic content variation, not a claim of testing every champion skin. Positions never move. */
    private fun referenceVariant(source: Bitmap, variant: Int): Bitmap {
        if (variant == 0) return source
        return source.copy(Bitmap.Config.ARGB_8888, true).also { changed ->
            val canvas = Canvas(changed)
            canvas.drawRect(290f, 75f, 1030f, 470f, Paint().apply {
                color = if (variant == 1) Color.MAGENTA else Color.CYAN
            })
            if (variant == 2) {
                // Replace the entire scene, not just the middle champion. Restore only
                // the twenty measured portrait regions, independently of names or artwork.
                canvas.drawColor(Color.rgb(30, 35, 45))
                val paint = Paint(Paint.FILTER_BITMAP_FLAG)
                for (index in picks.indices) {
                    fun pickRect(i: Int): Rect {
                        val (x, y) = pickCenters[i]
                        return Rect(x.toInt()-34, y.toInt()-34, x.toInt()+34, y.toInt()+34)
                    }
                    canvas.drawBitmap(source, pickRect(permutedIndex(index, variant)), pickRect(index), paint)
                    fun banRect(i: Int) = Rect(banCenters[i].toInt()-22, 8, banCenters[i].toInt()+22, 52)
                    canvas.drawBitmap(source, banRect(permutedIndex(index, variant)), banRect(index), paint)
                }
            }
        }
    }


    private fun saveEvidence(frame: Bitmap, config: VisionCalibrationConfig, bans: List<BanSlot>, id: String) {
        val evidence = frame.copy(Bitmap.Config.ARGB_8888, true)
        try {
            val canvas = Canvas(evidence)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 2f }
            for (ally in listOf(true, false)) for (slot in 0..4) {
                paint.color = if (ally) Color.CYAN else Color.RED
                val crop = AdaptiveScreenLayoutEngine.calculateSlotCropRect(frame.width, frame.height, ally, slot, config)
                canvas.drawCircle(crop.exactCenterX(), crop.exactCenterY(), crop.width()/2f, paint)
            }
            paint.color = Color.YELLOW
            bans.forEach { slot -> slot.region?.let { canvas.drawCircle(it.x*frame.width, it.y*frame.height, it.radius*frame.height, paint) } }
            File(output, "$id.png").outputStream().use { evidence.compress(Bitmap.CompressFormat.PNG, 100, it) }
        } finally { evidence.recycle() }
    }
}
