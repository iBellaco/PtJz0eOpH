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
    @Test fun resizedPaleBadgeWingsAreRemovedWithoutErasingVi() {
        val source=javaClass.getResourceAsStream("/portraits/draft-ten-selected.jpg").use {
            android.graphics.BitmapFactory.decodeStream(it)!!
        }
        val frame=Bitmap.createBitmap(1920,869,Bitmap.Config.ARGB_8888)
        Canvas(frame).drawBitmap(source,null,android.graphics.Rect(0,0,1920,868),Paint(Paint.FILTER_BITMAP_FLAG))
        val original=Bitmap.createBitmap(frame,183,586,269,48)
        val cleaned=original.copy(Bitmap.Config.ARGB_8888,true)
        try {
            com.example.service.screen.DraftImagePreprocessor.suppressLeadingMasteryBadge(cleaned)
            fun gold(bitmap:Bitmap):Int=(0 until bitmap.height).sumOf { y -> (0 until 50).count { x ->
                val c=bitmap.getPixel(x,y)
                Color.red(c)>95 && Color.green(c)>65 && Color.red(c)>Color.green(c)*1.08f && Color.green(c)>Color.blue(c)*1.25f
            } }
            assertTrue(gold(original)>100)
            assertEquals("Resizing must not leave warm wings beside a two-letter title",0,gold(cleaned))
            for(y in 19..34) for(x in 52..71)
                assertEquals(original.getPixel(x,y),cleaned.getPixel(x,y))
        } finally { cleaned.recycle();original.recycle();frame.recycle();source.recycle() }
    }
    @Test fun actualMasteryBadgeIsFullyRemovedWithoutTouchingTheTwoViLetters() {
        val source=javaClass.getResourceAsStream("/portraits/draft-ten-selected.jpg").use {
            android.graphics.BitmapFactory.decodeStream(it)!!
        }
        val original=Bitmap.createBitmap(source,130,390,50,32)
        val cleaned=original.copy(Bitmap.Config.ARGB_8888,true)
        try {
            com.example.service.screen.DraftImagePreprocessor.suppressLeadingMasteryBadge(cleaned)
            fun gold(bitmap:Bitmap):Int=(0 until bitmap.height).sumOf { y -> (0 until bitmap.width).count { x ->
                val c=bitmap.getPixel(x,y)
                Color.red(c)>95 && Color.green(c)>65 && Color.red(c)>Color.green(c)*1.08f && Color.green(c)>Color.blue(c)*1.25f
            } }
            assertTrue(gold(original)>10)
            assertEquals("A partial badge must not remain attached to VI",0,gold(cleaned))
            for(y in 14..24) for(x in 26..39)
                assertEquals("Real VI pixels must remain unchanged",original.getPixel(x,y),cleaned.getPixel(x,y))
        } finally { cleaned.recycle();original.recycle();source.recycle() }
    }
    @Test fun retryKeepsExactMaskedPixelsAndTitleBoundsButOnlyTheUnresolvedSlots() {
        val source = Bitmap.createBitmap(1280,579,Bitmap.Config.ARGB_8888)
        Canvas(source).drawColor(Color.rgb(40,60,90))
        val config=VisionCalibrationConfig()
        val band=AdaptiveScreenLayoutEngine.calculateSlotNameRect(1280,579,false,4,config)
        val atlas=DraftNameAtlas.create(source,config,listOf(band))
        try {
            val tile=atlas.tiles.single { !it.ally && it.slot==4 }
            val retry=DraftNameAtlas.retryUnresolved(atlas,listOf(tile))
            try {
                assertEquals(3,retry.tiles.size)
                assertTrue(retry.tiles.all { !it.ally && it.slot==4 && it.source==tile.source && it.titleMaxY==tile.titleMaxY })
                val raw=retry.tiles.single { it.original }
                assertTrue(retry.bitmap.width<atlas.bitmap.width)
                for(y in 0 until tile.region.height()) for(x in 0 until tile.region.width()) {
                    assertEquals(Color.BLACK,atlas.bitmap.getPixel(tile.region.left+x,tile.region.top+y))
                    assertEquals(atlas.bitmap.getPixel(tile.region.left+x,tile.region.top+y),
                        retry.bitmap.getPixel(raw.region.left+x,raw.region.top+y))
                }
                val binary=retry.tiles[2]
                for(y in binary.region.top until binary.region.bottom) for(x in binary.region.left until binary.region.right)
                    assertEquals("A fully masked title cannot acquire a black stroke",Color.WHITE,retry.bitmap.getPixel(x,y))
            } finally { retry.bitmap.recycle() }
        } finally { atlas.bitmap.recycle(); source.recycle() }
    }
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
                assertTrue("Avoid a wide atlas that shrinks short words inside native OCR",
                    maxOf(atlas.bitmap.width,atlas.bitmap.height)<3000)
                for (tile in atlas.tiles) {
                    assertEquals(AdaptiveScreenLayoutEngine.calculateSlotNameRect(640, 290, tile.ally, tile.slot, config), tile.source)
                    assertTrue("Small titles need more than a fixed 2x copy", tile.region.height() >= tile.source.height()*4)
                }
            } finally { atlas.bitmap.recycle() }
        } finally { source.recycle() }
    }
    @Test fun retryAtlasStaysCompactForEveryNumberOfUnresolvedTitles() {
        val source=Bitmap.createBitmap(1280,579,Bitmap.Config.ARGB_8888)
        val atlas=DraftNameAtlas.create(source,VisionCalibrationConfig(),emptyList())
        try {
            val originals=atlas.tiles.filter { it.original }
            for(count in 1..10) {
                val selected=originals.take(count)
                val retry=DraftNameAtlas.retryUnresolved(atlas,selected)
                try {
                    assertEquals(count*3,retry.tiles.size)
                    val width=selected.maxOf { it.region.width() }+64
                    val height=selected.maxOf { it.region.height() }+64
                    val minimum=(1..minOf(4,count*3)).minOf { cols ->
                        maxOf(cols*width,((count*3+cols-1)/cols)*height)
                    }
                    assertEquals(minimum,maxOf(retry.bitmap.width,retry.bitmap.height))
                    assertTrue(retry.tiles.all { it.region.left>=0 && it.region.top>=0 &&
                        it.region.right<=retry.bitmap.width && it.region.bottom<=retry.bitmap.height })
                } finally { retry.bitmap.recycle() }
            }
        } finally { atlas.bitmap.recycle();source.recycle() }
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
                    assertTrue("Small golden title letters must survive badge removal too", goldPixels(processed)>20)
                } finally { atlas.bitmap.recycle() }
            } finally { source.recycle() }
        }
    }
}
