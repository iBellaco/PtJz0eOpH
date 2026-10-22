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

/** Real production capture limits, measured fixture coordinates and identity checks, not hardware certification. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DraftMultiDeviceAuditTest {
    private data class Profile(val id: String, val manufacturer: String, val model: String, val width: Int, val height: Int)
    private val profiles = listOf(
        Profile("red-magic-reference", "nubia", "NX789J", 2688, 1216),
        Profile("poco-x7-pro", "Xiaomi", "2412DPC0AG", 2712, 1220),
        // Resolution scenarios only. These do not assert the native panel or OS settings of an unconnected S26.
        Profile("s26-fhd-scenario", "samsung", "S26-simulated", 2340, 1080),
        Profile("s26-fhd-plus-scenario", "samsung", "S26-simulated", 2400, 1080),
        Profile("s26-qhd-scenario", "samsung", "S26-simulated", 3120, 1440),
        Profile("s26-qhd-plus-scenario", "samsung", "S26-simulated", 3200, 1440)
    ) + listOf(640 to 360, 800 to 450, 960 to 540, 1280 to 579, 1280 to 720,
        1334 to 750, 1440 to 720, 1600 to 720, 1600 to 900, 1920 to 864,
        1920 to 1080, 2160 to 1080, 2400 to 1080, 2460 to 1080, 2520 to 1080,
        2560 to 1080, 2560 to 1440, 2778 to 1284, 3088 to 1440, 3840 to 2160,
        1024 to 768, 1600 to 1000, 2048 to 1536, 2560 to 1600).map { (w, h) ->
        Profile("generic-$w-$h", "generic", "resolution-simulation", w, h)
    }
    private val picks = listOf("senna", "ornn", "mel", "volibear", "vi", "alistar", "caitlyn", "nocturne", "vex", "teemo")
    private val bans = listOf("mordekaiser", "kayn", "jhin", "gwen", "hwei", "mordekaiser", "syndra", "cho_gath", "hwei", "soraka")
    // Independently measured in the owner's 1280 x 579 image, not derived from calibration defaults.
    private val pickCenters = listOf(92f to 110f, 92f to 188f, 92f to 265f, 92f to 343f, 92f to 420f,
        1227f to 110f, 1227f to 188f, 1227f to 265f, 1227f to 342f, 1227f to 419f)
    private val banCenters = listOf(35f, 82f, 129f, 175f, 221f, 1059f, 1105f, 1151f, 1197f, 1244f)
    private val output = File("build/reports/scanner-multidevice").apply { mkdirs() }

    @Test fun referenceDevicesAndS26CaptureScenarios() = audit(profiles.take(6), "device-profiles")
    @Test fun phoneResolutionMatrix() = audit(profiles.drop(6).take(20), "phone-resolutions")
    @Test fun tabletResolutionMatrix() = audit(profiles.takeLast(4), "tablet-resolutions")

    private fun audit(cases: List<Profile>, group: String) {
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
            for (profile in cases) {
                prefs.edit().clear().commit()
                ShadowBuild.setManufacturer(profile.manufacturer)
                ShadowBuild.setModel(profile.model)
                DraftVisionScanner.initCalibration(context)
                val (w, h) = CaptureFrameDecoder.captureSize(profile.width, profile.height)
                val frame = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val scale = minOf(w.toFloat() / source.width, h.toFloat() / source.height)
                val rw = (source.width * scale).toInt()
                val rh = (source.height * scale).toInt()
                val viewport = Rect((w-rw)/2, (h-rh)/2, (w-rw)/2+rw, (h-rh)/2+rh)
                val entry = JSONObject().put("profile", profile.id).put("model", profile.model)
                    .put("evidenceType", "resized-reference-not-physical-device")
                    .put("display", JSONArray(listOf(profile.width, profile.height)))
                    .put("capture", JSONArray(listOf(w, h)))
                val checks = JSONArray()
                val before = failures.size
                try {
                    Canvas(frame).apply { drawColor(Color.BLACK); drawBitmap(source, null, viewport, Paint(Paint.FILTER_BITMAP_FLAG)) }
                    val detected = DraftFrameGeometry.contentBounds(frame)
                    if (viewport != detected) failures += "${profile.id}: viewport $detected expected $viewport"
                    val config = DraftVisionScanner.configForFrame(frame)
                    if (config != DraftVisionScanner.alignedConfigFlow.value) failures += "${profile.id}: viewer configuration differs from scanner"
                    val measured = DraftVisionScanner.calibrationReportSnapshot()
                    if (measured.width != w || measured.height != h || measured.config != config)
                        failures += "${profile.id}: published capture dimensions/configuration differ"
                    for (index in picks.indices) {
                        val ally = index < 5
                        val slot = index % 5
                        val rect = AdaptiveScreenLayoutEngine.calculateSlotCropRect(w, h, ally, slot, config)
                        val expectedX = viewport.left + pickCenters[index].first * viewport.width() / source.width
                        val expectedY = viewport.top + pickCenters[index].second * viewport.height() / source.height
                        val referenceError = hypot(rect.exactCenterX()-expectedX, rect.exactCenterY()-expectedY) * source.height / viewport.height()
                        val crop = AdaptiveScreenLayoutEngine.extractSlotAvatarBitmap(frame, w, h, ally, slot, config)!!
                        try {
                            val ranked = LiteRTVisionClassifier.rankSlotPortrait(crop, ally, context)
                            val identity = ranked.firstOrNull()?.first?.id
                            val accepted = ranked.size >= 2 && PortraitMatcher.accepts(ranked[0].second, ranked[1].second, .80f)
                            if (identity != picks[index] || !accepted) failures += "${profile.id}: slot $index expected ${picks[index]} got ${ranked.take(2).map { it.first.id to it.second }}"
                            if (referenceError > 6f) failures += "${profile.id}: slot $index center error $referenceError reference pixels"
                            if (crop.width != rect.width() || crop.height != rect.height()) failures += "${profile.id}: crop and viewer size mismatch $index"
                            checks.put(JSONObject().put("slot", index).put("expected", picks[index]).put("recognized", identity)
                                .put("accepted", accepted).put("centerErrorReferencePixels", referenceError)
                                .put("crop", JSONArray(listOf(rect.left, rect.top, rect.right, rect.bottom))))
                        } finally { crop.recycle() }
                    }
                    val observations = LiteRTVisionClassifier.findHeaderBanObservations(frame, context, emptyList(), config)
                        .sortedWith(compareBy({ it.side.ordinal }, { it.region.x }))
                    if (observations.map { it.championId } != bans) failures += "${profile.id}: bans ${observations.map { it.championId }}"
                    val tracker = BannedPortraitScanner()
                    repeat(2) { tracker.update(observations, w.toFloat()/h) }
                    if (tracker.slots.map { it.championId } != bans || tracker.slots.any { it.state != BanState.CONFIRMED })
                        failures += "${profile.id}: ban tracker/viewer did not confirm all ten positions"
                    tracker.slots.forEachIndexed { index, slot ->
                        val region = slot.region
                        if (region != null) {
                            val expectedX = viewport.left + banCenters[index] * viewport.width() / source.width
                            val expectedY = viewport.top + 30f * viewport.height() / source.height
                            val error = hypot(region.x*w-expectedX, region.y*h-expectedY) * source.height / viewport.height()
                            if (error > 6f) failures += "${profile.id}: ban $index center error $error reference pixels"
                            checks.put(JSONObject().put("ban", index).put("expected", bans[index]).put("recognized", slot.championId)
                                .put("centerErrorReferencePixels", error))
                        }
                    }
                    saveEvidence(frame, config, tracker.slots, profile.id)
                } finally {
                    frame.recycle()
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
