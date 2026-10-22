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
        val references = context.assets.list("champions")!!.filter { it.endsWith(".png") }.associate { name ->
            val image = context.assets.open("champions/$name").use { BitmapFactory.decodeStream(it)!! }
            try {
                val pixels = IntArray(image.width * image.height)
                image.getPixels(pixels, 0, image.width, 0, 0, image.width, image.height)
                name.removeSuffix(".png").replace("_", "") to PortraitMatcher.references(pixels, image.width, image.height)
            } finally { image.recycle() }
        }
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
                    val config = DraftFrameGeometry.forViewport(VisionCalibrationConfig(), detected, w, h)
                    // Alignment is exercised on the actual frame, not on synthetic rings.
                    val aligned = DraftRingAlignment.align(frame, config, null)
                    for (index in expected.indices) {
                        val ally = index < 5; val slot = index % 5
                        val crop = AdaptiveScreenLayoutEngine.extractSlotAvatarBitmap(frame,w,h,ally,slot,aligned)!!
                        try {
                            val pixels = IntArray(crop.width*crop.height)
                            crop.getPixels(pixels,0,crop.width,0,0,crop.width,crop.height)
                            val input = PortraitMatcher.descriptor(pixels,crop.width,crop.height)
                            val ranked = references.map { it.key to PortraitMatcher.similarity(input,it.value) }.sortedByDescending { it.second }
                            assertEquals("$w/$h slot $index: ${ranked.take(3)}",expected[index],ranked.first().first)
                            assertTrue("$w/$h slot $index confidence ${ranked.take(2)}",
                                PortraitMatcher.accepts(ranked[0].second, ranked[1].second, .80f))
                        } finally { crop.recycle() }
                    }
                } finally { frame.recycle() }
            }
        } finally { source.recycle() }
    }

    @Test fun manualHeaderGeometryIsNotReplacedAndOrientationProfilesAreDistinct() {
        val manual = VisionCalibrationConfig().copy(topAllyXRatios=List(5){.04f+it*.04f})
        for ((w,h) in listOf(1280 to 720,2688 to 1216,1024 to 768)) {
            assertEquals(manual.topAllyXRatios,AdaptiveScreenLayoutEngine.computeAdaptiveConfig(w,h,manual).topAllyXRatios)
        }
        assertNotEquals(VisionCalibrationConfig.captureProfileKey(1280,720),VisionCalibrationConfig.captureProfileKey(720,1280))
    }
}
