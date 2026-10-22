package com.example

import android.app.Application
import android.graphics.*
import com.example.data.WildRiftRepository
import com.example.service.screen.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Real supplied frame; no portrait is pasted, recolored or replaced by a test template. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DraftReferenceFrameTest {
    private val expected = listOf("senna", "ornn", "mel", "volibear", "vi",
        "alistar", "caitlyn", "nocturne", "vex", "teemo")

    @Test fun tenRealPortraitsRemainInTheirOwnSlotsAcrossCaptureSizesAndLetterboxing() {
        val context = RuntimeEnvironment.getApplication()
        WildRiftRepository.initChampions(context, forceReload = true)
        val failures = mutableListOf<String>()
        val source = javaClass.getResourceAsStream("/portraits/draft-ten-selected.jpg").use { BitmapFactory.decodeStream(it)!! }
        try {
            // Reference capture, HD, FHD, Red Magic/Poco-size surfaces, tablet and ultrawide.
            for ((w, h) in listOf(1280 to 579, 960 to 434, 1920 to 869, 2688 to 1216,
                2400 to 1080, 2712 to 1220, 1280 to 720, 1920 to 1080, 1600 to 1000,
                1024 to 768, 2520 to 1080)) {
                val frame = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                try {
                    val scale = minOf(w.toFloat()/source.width, h.toFloat()/source.height)
                    val rw = (source.width*scale).toInt(); val rh = (source.height*scale).toInt()
                    val viewport = Rect((w-rw)/2, (h-rh)/2, (w-rw)/2+rw, (h-rh)/2+rh)
                    Canvas(frame).apply { drawColor(Color.BLACK); drawBitmap(source, null, viewport, Paint(Paint.FILTER_BITMAP_FLAG)) }
                    val detected = DraftFrameGeometry.contentBounds(frame)
                    assertEquals("Viewport $w/$h", viewport, detected)
                    for (prior in listOf(VisionCalibrationConfig(), VisionCalibrationConfig.defaultsForDevice("Xiaomi", "2412DPC0AG"))) {
                    val config = DraftFrameGeometry.forViewport(prior, detected, w, h)
                    // Alignment is exercised on the actual frame, not on synthetic rings.
                    val aligned = DraftRingAlignment.align(frame, config, null)
                    for (index in expected.indices) {
                        val ally = index < 5; val slot = index % 5
                        val crop = AdaptiveScreenLayoutEngine.extractSlotAvatarBitmap(frame,w,h,ally,slot,aligned)!!
                        try {
                            val ranked = LiteRTVisionClassifier.rankSlotPortrait(crop,ally,context)
                            val label = "$w/$h prior ${prior.allyAvatarCenterX} slot $index"
                            if (ranked.first().first.id != expected[index]) failures += "$label identity: ${ranked.take(3).map { it.first.id to it.second }}"
                            if (!PortraitMatcher.accepts(ranked[0].second, ranked[1].second, .80f))
                                failures += "$label confidence ${ranked.take(2).map { it.first.id to it.second }}"
                            kotlinx.coroutines.runBlocking {
                                LiteRTVisionClassifier.reset()
                                LiteRTVisionClassifier.setThreshold(.80f, context)
                                var confirmed: String? = null
                                repeat(2) {
                                    confirmed = LiteRTVisionClassifier.executeTenthPickInference(crop, ally,
                                        emptySet(), 9, slot, context)?.first?.id
                                }
                                if (confirmed != expected[index]) failures += "$label stable: $confirmed, ring=${LiteRTVisionClassifier.hasSlotRing(crop,ally)}, ${LiteRTVisionClassifier.reportFlow.value.decisionReason}"
                            }
                        } finally { crop.recycle() }
                    }
                    }
                } finally { frame.recycle() }
            }
        } finally { source.recycle() }
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }

    @Test fun realBansAreRecognizedInsideLetterboxedSurfaces() {
        val context = RuntimeEnvironment.getApplication()
        WildRiftRepository.initChampions(context, forceReload = true)
        val source = javaClass.getResourceAsStream("/portraits/draft-ten-selected.jpg").use { BitmapFactory.decodeStream(it)!! }
        val expectedBans = listOf("mordekaiser", "kayn", "jhin", "gwen", "hwei",
            "mordekaiser", "syndra", "cho_gath", "hwei", "soraka")
        try { for ((w,h) in listOf(1280 to 579,1280 to 720,1024 to 768,2400 to 1080)) {
            val frame=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
            try {
                val scale=minOf(w.toFloat()/source.width,h.toFloat()/source.height)
                val rw=(source.width*scale).toInt();val rh=(source.height*scale).toInt()
                val area=Rect((w-rw)/2,(h-rh)/2,(w-rw)/2+rw,(h-rh)/2+rh)
                Canvas(frame).apply { drawColor(Color.BLACK);drawBitmap(source,null,area,Paint(Paint.FILTER_BITMAP_FLAG)) }
                val observations=LiteRTVisionClassifier.findHeaderBanObservations(frame,context,emptyList())
                    .sortedWith(compareBy({it.side.ordinal},{it.region.x}))
                assertEquals("Bans $w/$h",expectedBans,observations.map { it.championId })
                assertTrue(observations.all { it.region.y*h in area.top.toFloat()..area.top+area.height()*.12f })
            } finally { frame.recycle() }
        } } finally { source.recycle() }
    }

    @Test fun manualHeaderGeometryIsNotReplacedAndOrientationProfilesAreDistinct() {
        val manual = VisionCalibrationConfig().copy(topAllyXRatios=List(5){.04f+it*.04f})
        for ((w,h) in listOf(1280 to 720,2688 to 1216,1024 to 768)) {
            assertEquals(manual.topAllyXRatios,AdaptiveScreenLayoutEngine.computeAdaptiveConfig(w,h,manual).topAllyXRatios)
        }
        assertNotEquals(VisionCalibrationConfig.captureProfileKey(1280,720),VisionCalibrationConfig.captureProfileKey(720,1280))
    }
}
