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
    private data class Profile(val id: String, val manufacturer: String, val model: String, val width: Int, val height: Int,
        val edgeAnchored: Boolean = false, val specification: String? = null)
    private val variants = listOf("original", "changed-background", "changed-portraits-and-bans")
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
    @Test fun formerlyFailingCentersStayAlignedWhenPortraitsChange() = audit(listOf(
        Profile("regression-square", "generic", "square-resolution-simulation", 1800, 1800),
        Profile("regression-small-edge", "generic", "synthetic-side-panels", 640, 360, true),
        Profile("regression-tablet-edge", "generic", "synthetic-side-panels", 1024, 768, true)
    ), "center-regressions")
    @Test fun documentedAndroidDisplayProfiles() = audit(listOf(
        Profile("pixel-8", "Google", "Pixel 8", 2400, 1080, specification = PIXEL_SPECS),
        Profile("pixel-8-pro", "Google", "Pixel 8 Pro", 2992, 1344, specification = PIXEL_SPECS),
        Profile("pixel-9", "Google", "Pixel 9", 2424, 1080, specification = PIXEL_SPECS),
        Profile("pixel-9-pro", "Google", "Pixel 9 Pro", 2856, 1280, specification = PIXEL_SPECS),
        Profile("pixel-9-pro-xl", "Google", "Pixel 9 Pro XL", 2992, 1344, specification = PIXEL_SPECS),
        Profile("oneplus-12", "OnePlus", "OnePlus 12", 3168, 1440,
            specification = "https://www.oneplus.com/global/12/specs"),
        Profile("galaxy-s24", "samsung", "Galaxy S24", 2340, 1080, specification = S24_SPECS),
        Profile("galaxy-s24-plus", "samsung", "Galaxy S24+", 3120, 1440, specification = S24_SPECS),
        Profile("galaxy-s24-ultra", "samsung", "Galaxy S24 Ultra", 3120, 1440, specification = S24_SPECS),
        Profile("galaxy-z-fold5-inner", "samsung", "Galaxy Z Fold5 (inner)", 2176, 1812,
            specification = "https://news.samsung.com/global/samsung-galaxy-z-flip5-and-galaxy-z-fold5-delivering-flexibility-and-versatility-without-compromise")
    ), "documented-android-displays")
    @Test fun nearlySquareFoldableCapturesKeepAllTwentyPositions() = audit(
        listOf(1920 to 1792, 2208 to 1840, 2440 to 2268, 1800 to 1800).map { (w, h) ->
            Profile("foldable-$w-$h", "generic", "foldable-resolution-simulation", w, h)
        }, "foldable-resolutions")
    @Test fun fullScreenSidePanelsRetainCircularPortraitsWithoutLetterboxing() = audit(
        listOf(640 to 360, 1280 to 720, 2400 to 1080, 2712 to 1220, 3120 to 1440,
            1024 to 768, 1600 to 1000, 2560 to 1600, 2440 to 2268, 1800 to 1800).map { (w, h) ->
            Profile("edge-anchored-$w-$h", "generic", "synthetic-side-panels", w, h, true)
        }, "edge-anchored-resolutions")

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
            for (profile in cases) for (variant in variants.indices) {
                val caseId = "${profile.id}-${variants[variant]}"
                val variantSource = referenceVariant(source, variant)
                prefs.edit().clear().commit()
                ShadowBuild.setManufacturer(profile.manufacturer)
                ShadowBuild.setModel(profile.model)
                DraftVisionScanner.initCalibration(context)
                val (w, h) = CaptureFrameDecoder.captureSize(profile.width, profile.height)
                val frame = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val scale = if (profile.edgeAnchored) h.toFloat() / source.height
                    else minOf(w.toFloat() / source.width, h.toFloat() / source.height)
                val rw = (source.width * scale).toInt()
                val rh = (source.height * scale).toInt()
                val viewport = if (profile.edgeAnchored) Rect(0, 0, w, h)
                    else Rect((w-rw)/2, (h-rh)/2, (w-rw)/2+rw, (h-rh)/2+rh)
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

    companion object {
        private const val PIXEL_SPECS = "https://support.google.com/pixelphone/answer/7158570?hl=es"
        private const val S24_SPECS = "https://news.samsung.com/global/enter-the-new-era-of-mobile-ai-with-samsung-galaxy-s24-series"
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
