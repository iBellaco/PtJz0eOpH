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
                1024 to 768, 2520 to 1080, 800 to 450, 1440 to 720, 2340 to 1080,
                2560 to 1440, 3120 to 1440, 3840 to 2160)) {
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
                            val label = "$w/$h prior ${prior.allyAvatarCenterX} slot $index at ${if(ally) aligned.getAllySlotX(slot) else aligned.getEnemySlotX(slot)},${(if(ally) aligned.allySlotYRatios else aligned.enemySlotYRatios)[slot]}"
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
        try { for ((w,h) in listOf(1280 to 579,1280 to 720,1024 to 768,2400 to 1080,
            800 to 450,1440 to 720,2340 to 1080,3120 to 1440,3840 to 2160)) {
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

    @Test fun changedCentralArtworkDoesNotChangeSidePortraitsOrBans() {
        val context = RuntimeEnvironment.getApplication()
        WildRiftRepository.initChampions(context, forceReload = true)
        val source = javaClass.getResourceAsStream("/portraits/draft-ten-selected.jpg").use { BitmapFactory.decodeStream(it)!! }
        try { for (color in listOf(Color.BLACK, Color.CYAN, Color.MAGENTA)) {
            val frame = source.copy(Bitmap.Config.ARGB_8888, true)
            try {
                Canvas(frame).drawRect(290f, 75f, 1030f, 470f, Paint().apply { this.color = color })
                val aligned = DraftRingAlignment.align(frame, VisionCalibrationConfig(), null)
                for (index in expected.indices) {
                    val crop = AdaptiveScreenLayoutEngine.extractSlotAvatarBitmap(frame, frame.width, frame.height, index < 5, index % 5, aligned)!!
                    try {
                        val ranked = LiteRTVisionClassifier.rankSlotPortrait(crop, index < 5, context)
                        assertEquals("Central variant $color slot $index", expected[index], ranked.first().first.id)
                        assertTrue(PortraitMatcher.accepts(ranked[0].second, ranked[1].second, .80f))
                    } finally { crop.recycle() }
                }
                assertEquals(10, LiteRTVisionClassifier.findHeaderBanObservations(frame, context, emptyList()).size)
            } finally { frame.recycle() }
        } } finally { source.recycle() }
    }

    @Test fun changedPortraitOrderIsRecognizedInsteadOfRememberingReferenceSlots() {
        val context = RuntimeEnvironment.getApplication()
        WildRiftRepository.initChampions(context, forceReload = true)
        val source = javaClass.getResourceAsStream("/portraits/draft-ten-selected.jpg").use { BitmapFactory.decodeStream(it)!! }
        try {
            val original = DraftRingAlignment.align(source, VisionCalibrationConfig(), null)
            for (shift in listOf(1, 3)) {
                val frame = source.copy(Bitmap.Config.ARGB_8888, true)
                try {
                    val canvas = Canvas(frame)
                    for (ally in listOf(true, false)) for (slot in 0..4) {
                        val from = AdaptiveScreenLayoutEngine.calculateSlotCropRect(source.width,source.height,ally,(slot+shift)%5,original)
                        val to = AdaptiveScreenLayoutEngine.calculateSlotCropRect(source.width,source.height,ally,slot,original)
                        canvas.drawBitmap(source, from, to, Paint(Paint.FILTER_BITMAP_FLAG))
                    }
                    // Synthetic permutation, independent expected identities; not a physical match.
                    val aligned = DraftRingAlignment.align(frame, original, null)
                    for (ally in listOf(true, false)) for (slot in 0..4) {
                        val crop = AdaptiveScreenLayoutEngine.extractSlotAvatarBitmap(frame,frame.width,frame.height,ally,slot,aligned)!!
                        try {
                            val ranked = LiteRTVisionClassifier.rankSlotPortrait(crop,ally,context)
                            assertEquals(expected[(if (ally) 0 else 5)+(slot+shift)%5],ranked[0].first.id)
                            assertTrue(PortraitMatcher.accepts(ranked[0].second,ranked[1].second,.80f))
                        } finally { crop.recycle() }
                    }
                } finally { frame.recycle() }
            }
        } finally { source.recycle() }
    }

    @Test fun viewportAndMaskMappingRemainConsistentAcrossDenseSizeMatrix() {
        // Geometry only: not a claim of recognition or physical device testing at these sizes.
        val base = VisionCalibrationConfig()
        for (width in 640..3840 step 37) for (height in 360..2160 step 31) {
            val scale = minOf(width / 1280.0, height / 579.0)
            val w = (1280 * scale).toInt(); val h = (579 * scale).toInt()
            val bounds = Rect((width-w)/2, (height-h)/2, (width-w)/2+w, (height-h)/2+h)
            val mapped = DraftFrameGeometry.forViewport(base, bounds, width, height)
            for (index in 0..4) {
                assertEquals(bounds.left + base.getAllySlotX(index)*w, mapped.getAllySlotX(index)*width, .002f)
                assertEquals(bounds.top + base.enemySlotYRatios[index]*h, mapped.enemySlotYRatios[index]*height, .002f)
            }
            val mask = CaptureCoordinateSpace.map(Rect(0,0,1280,579),1280,579,width,height)
            assertTrue(mask.left >= 0 && mask.top >= 0 && mask.right <= width && mask.bottom <= height)
            assertTrue(kotlin.math.abs(mask.left-bounds.left) <= 1 && kotlin.math.abs(mask.top-bounds.top) <= 1)
        }
    }

    @Test fun manualHeaderGeometryIsNotReplacedAndOrientationProfilesAreDistinct() {
        val manual = VisionCalibrationConfig().copy(topAllyXRatios=List(5){.04f+it*.04f})
        for ((w,h) in listOf(1280 to 720,2688 to 1216,1024 to 768)) {
            assertEquals(manual.topAllyXRatios,AdaptiveScreenLayoutEngine.computeAdaptiveConfig(w,h,manual).topAllyXRatios)
        }
        assertNotEquals(VisionCalibrationConfig.captureProfileKey(1280,720),VisionCalibrationConfig.captureProfileKey(720,1280))
    }
}
