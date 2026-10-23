package com.example

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.service.screen.AdaptiveScreenLayoutEngine
import com.example.service.screen.DraftNameAtlas
import com.example.service.screen.VisionCalibrationConfig
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DraftNameAtlasTest {
    @Test fun oppositeOrderFixtureHasARealNinthTitleAndAnAcceptedTenthPortrait() = kotlinx.coroutines.runBlocking<Unit> {
        val context = org.robolectric.RuntimeEnvironment.getApplication()
        com.example.data.WildRiftRepository.initChampions(context, forceReload = true)
        val source = javaClass.getResourceAsStream("/portraits/draft-ten-selected.jpg").use {
            android.graphics.BitmapFactory.decodeStream(it)!!
        }
        val variant = DraftSequenceFixture.create(source, false)
        try {
            for (y in 400 until 415) for (x in 155 until 173)
                assertEquals("Original VI title pixels must not be synthesized",source.getPixel(x,y),variant.getPixel(x+1019,y-1))
            for ((ally, slot, expected) in listOf(Triple(true,4,"teemo"),Triple(false,4,"vi"))) {
                val aligned = com.example.service.screen.DraftRingAlignment.align(variant,VisionCalibrationConfig(),null)
                val crop = AdaptiveScreenLayoutEngine.extractSlotAvatarBitmap(variant,1280,579,ally,slot,aligned)!!
                try {
                    val ranked = com.example.service.screen.LiteRTVisionClassifier.rankSlotPortrait(crop,ally,context)
                    assertEquals(expected,ranked.first().first.id)
                    // The ninth pick is deliberately confirmed by its real title, never by
                    // accepting a preview portrait. Only the tenth has a visual threshold.
                    if (ally) {
                        assertTrue(ranked.take(2).map { it.first.id to it.second }.toString(),
                            com.example.service.screen.PortraitMatcher.accepts(ranked[0].second,ranked[1].second,.80f))
                        val classifier = com.example.service.screen.LiteRTVisionClassifier
                        classifier.reset()
                        classifier.setThreshold(.80f,context)
                        assertNull(classifier.executeTenthPickInference(crop,true,emptySet(),9,4,context))
                        assertEquals("teemo",classifier.executeTenthPickInference(crop,true,emptySet(),9,4,context)?.first?.id)
                    }
                } finally { crop.recycle() }
            }
        } finally { source.recycle(); variant.recycle() }
    }
    @Test fun nameBandsFollowTheGameViewportAndDoNotGrowIntoAnotherPlayersRow() {
        val base = VisionCalibrationConfig()
        val layouts = listOf(Triple(1024, 768, android.graphics.Rect(0, 152, 1024, 615)),
            Triple(1080, 1080, android.graphics.Rect(0, 296, 1080, 784)),
            Triple(1920, 1080, android.graphics.Rect(0, 106, 1920, 974)))
        for ((width, height, viewport) in layouts) {
            val adapted = com.example.service.screen.DraftFrameGeometry.forViewport(base, viewport, width, height)
            for (ally in listOf(true, false)) for (slot in 0..4) {
                val original = AdaptiveScreenLayoutEngine.calculateSlotNameRect(1280, 579, ally, slot, base)
                val actual = AdaptiveScreenLayoutEngine.calculateSlotNameRect(width, height, ally, slot, adapted)
                val expected = android.graphics.Rect(
                    viewport.left + original.left * viewport.width() / 1280,
                    viewport.top + original.top * viewport.height() / 579,
                    viewport.left + original.right * viewport.width() / 1280,
                    viewport.top + original.bottom * viewport.height() / 579)
                listOf(actual.left to expected.left, actual.top to expected.top,
                    actual.right to expected.right, actual.bottom to expected.bottom).forEach { (value, target) ->
                    assertTrue("$width x $height $ally/$slot: $actual expected $expected", kotlin.math.abs(value-target) <= 2)
                }
            }
            assertEquals(1f, base.nameBandScale, 0f)
        }
    }

    @Test fun smallNameBandsAreUpscaledWithoutChangingTheSourceRegions() {
        val source = Bitmap.createBitmap(640, 290, Bitmap.Config.ARGB_8888)
        try {
            val config = VisionCalibrationConfig()
            val atlas = DraftNameAtlas.create(source, config, emptyList())
            try {
                assertEquals(20, atlas.tiles.size)
                for (tile in atlas.tiles) {
                    assertEquals(AdaptiveScreenLayoutEngine.calculateSlotNameRect(640, 290, tile.ally, tile.slot, config), tile.source)
                    assertTrue("Small titles need more than a fixed 2x copy", tile.region.height() >= tile.source.height()*4)
                }
            } finally { atlas.bitmap.recycle() }
        } finally { source.recycle() }
    }
    @Test fun goldTitleSurvivesBadgeSuppressionOnBothMeasuredPhoneProfiles() {
        for (config in listOf(VisionCalibrationConfig(),
            VisionCalibrationConfig.defaultsForDevice("Xiaomi", "2412DPC0AG"))) {
            val source = Bitmap.createBitmap(1920, 868, Bitmap.Config.ARGB_8888)
            try {
                source.eraseColor(Color.BLACK)
                val band = AdaptiveScreenLayoutEngine.calculateSlotNameRect(1920, 868, true, 0, config)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(220, 180, 80); textSize = 20f }
                Canvas(source).drawText("SERAPHINE", band.left + 3f, band.bottom - 8f, paint)
                val atlas = DraftNameAtlas.create(source, config, emptyList())
                try {
                    val raw = atlas.tiles.single { it.ally && it.slot == 0 && it.original }
                    val processed = atlas.tiles.single { it.ally && it.slot == 0 && !it.original && !it.contrast }
                    fun goldPixels(tile: DraftNameAtlas.Tile): Int {
                        var count = 0
                        val end = tile.region.left + (band.height() * .65f).toInt() * 2 - 2
                        for (y in tile.region.top until tile.region.bottom) for (x in tile.region.left until end) {
                            val pixel = atlas.bitmap.getPixel(x, y)
                            if (Color.red(pixel) > 95 && Color.green(pixel) > 65) count++
                        }
                        return count
                    }
                    assertTrue("The untouched title must retain its leading letters", goldPixels(raw) > 20)
                    assertEquals("Badge removal would have erased those letters", 0, goldPixels(processed))
                } finally { atlas.bitmap.recycle() }
            } finally { source.recycle() }
        }
    }
}
