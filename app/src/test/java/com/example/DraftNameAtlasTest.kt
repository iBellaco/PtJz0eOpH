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
    @Test fun normalTextSizesKeepBothOriginalSourcesAndTitleBoundsWithoutRestoringMasks() {
        val source=Bitmap.createBitmap(1280,579,Bitmap.Config.ARGB_8888).apply { eraseColor(Color.WHITE) }
        val config=VisionCalibrationConfig()
        val mask=AdaptiveScreenLayoutEngine.calculateSlotNameRect(1280,579,false,4,config)
        val atlas=DraftNameAtlas.create(source,config,listOf(mask))
        try {
            val tile=atlas.tiles.single { it.original && !it.ally && it.slot==4 }
            val focused=DraftNameAtlas.focusDetectedLine(atlas,tile,android.graphics.Rect(100,60,150,100))
            try {
                val normal=DraftNameAtlas.nativeTextSizes(focused,40)
                try {
                    assertEquals(9,normal.tiles.size)
                    assertEquals(3,normal.tiles.count { it.original })
                    for((index,part) in normal.tiles.withIndex()) {
                        val original=focused.tiles[index%3]
                        val factor=listOf(24f,36f,48f)[index/3]/40
                        assertEquals(32,part.region.left)
                        if(index>0) assertTrue(part.region.top>normal.tiles[index-1].region.bottom)
                        assertEquals(original.source,part.source)
                        assertEquals(kotlin.math.round(original.region.width()*factor).toInt(),part.region.width())
                        assertEquals(kotlin.math.round(original.titleMaxY*factor).toInt(),part.titleMaxY)
                        assertEquals(kotlin.math.round(original.titleMinY*factor).toInt(),part.titleMinY)
                        if(part.original) for(y in part.region.top until part.region.bottom) for(x in part.region.left until part.region.right)
                            assertEquals(Color.BLACK,normal.bitmap.getPixel(x,y))
                    }
                } finally { normal.bitmap.recycle() }
            } finally { focused.bitmap.recycle() }
        } finally { atlas.bitmap.recycle();source.recycle() }
    }
    @Test fun retriesKeepSecondMeasuredBoxAndCalibratedRowWithoutRepeatingIdenticalBoxes() {
        val first=android.graphics.Rect(200,30,270,80)
        val second=android.graphics.Rect(190,10,280,90)
        val fallback=android.graphics.Rect(0,48,900,144)
        assertEquals(listOf(fallback,first,second),DraftNameAtlas.retryLineBounds(
            listOf(first,first,second,android.graphics.Rect(210,20,280,90)),fallback))
        assertEquals(listOf(fallback),DraftNameAtlas.retryLineBounds(emptyList(),fallback))
        assertEquals(listOf(fallback),DraftNameAtlas.retryLineBounds(listOf(fallback),fallback))
        assertEquals(listOf(fallback),DraftNameAtlas.retryLineBounds(listOf(android.graphics.Rect()),fallback))
    }
    @Test fun batchedAlternativesPreserveEveryPixelMaskAndTitleBoundOnBoundedPages() {
        val source=Bitmap.createBitmap(1280,579,Bitmap.Config.ARGB_8888).apply { eraseColor(Color.BLUE) }
        val config=VisionCalibrationConfig()
        val mask=AdaptiveScreenLayoutEngine.calculateSlotNameRect(1280,579,true,4,config)
        val atlas=DraftNameAtlas.create(source,config,listOf(mask))
        try {
            val pages=DraftNameAtlas.packAlternatives(listOf(atlas))
            try {
                assertTrue(pages.size<atlas.tiles.size)
                assertEquals(atlas.tiles.size,pages.sumOf { it.tiles.size })
                var index=0
                for(page in pages) {
                    assertTrue(page.bitmap.height<=1536)
                    for(tile in page.tiles) {
                        val original=atlas.tiles[index++]
                        assertEquals(original.copy(region=tile.region),tile)
                        val expected=IntArray(original.region.width()*original.region.height())
                        val actual=IntArray(expected.size)
                        atlas.bitmap.getPixels(expected,0,original.region.width(),original.region.left,
                            original.region.top,original.region.width(),original.region.height())
                        page.bitmap.getPixels(actual,0,tile.region.width(),tile.region.left,
                            tile.region.top,tile.region.width(),tile.region.height())
                        assertArrayEquals(expected,actual)
                    }
                }
            } finally { pages.forEach { it.bitmap.recycle() } }
            assertFalse("Packing does not recycle its caller's input",atlas.bitmap.isRecycled)
            assertTrue(DraftNameAtlas.packAlternatives(emptyList()).isEmpty())
        } finally { atlas.bitmap.recycle();source.recycle() }
    }
    @Test fun sceneRereadingRetainsOriginalSceneButOnlyDecodesTenMaskedTitleBands() {
        val source=Bitmap.createBitmap(640,360,Bitmap.Config.ARGB_8888).apply { eraseColor(Color.WHITE) }
        val config=VisionCalibrationConfig()
        val mask=AdaptiveScreenLayoutEngine.calculateSlotNameRect(640,360,false,4,config)
        val scene=DraftNameAtlas.sceneTitles(source,config,listOf(mask))
        try {
            assertEquals(1920,scene.bitmap.width)
            assertEquals(1080,scene.bitmap.height)
            assertEquals(10,scene.tiles.size)
            for(tile in scene.tiles) {
                assertEquals(AdaptiveScreenLayoutEngine.calculateSlotNameRect(640,360,tile.ally,tile.slot,config),tile.source)
                assertFalse("The centre champion's label is not an OCR identity region",tile.region.contains(960,810))
                assertEquals(tile.region.height(),tile.titleMaxY)
            }
            val target=scene.tiles.single { !it.ally && it.slot==4 }
            for(y in target.region.top until target.region.bottom) for(x in target.region.left until target.region.right)
                assertEquals("Overlay pixels cannot return in the original-scene retry",Color.BLACK,scene.bitmap.getPixel(x,y))
        } finally { scene.bitmap.recycle();source.recycle() }
    }
    @Test fun alliedBadgeMarginCanBeRemovedWithoutChangingTheTitleSource() {
        val source=Bitmap.createBitmap(1280,579,Bitmap.Config.ARGB_8888).apply { eraseColor(Color.WHITE) }
        val config=VisionCalibrationConfig()
        val atlas=DraftNameAtlas.create(source,config,emptyList())
        try {
            val tile=atlas.tiles.single { it.original && it.ally && it.slot==4 }
            val withoutBadge=DraftNameAtlas.withoutLeadingBadge(atlas,tile)
            try {
                assertEquals(1,withoutBadge.tiles.size)
                val result=withoutBadge.tiles.single()
                assertEquals(tile.source, result.source)
                assertEquals(tile.region.width(), result.region.width())
                assertEquals(tile.source.top,result.source.top)
                assertEquals(tile.source.right,result.source.right)
                assertEquals(tile.region.height(),result.region.height())
            } finally { withoutBadge.bitmap.recycle() }
        } finally { atlas.bitmap.recycle();source.recycle() }
    }
    @Test fun aDetectorSeeingOnlyTheLastEnemyLetterMustStillRetainTheWholeViTitle() {
        val reference=javaClass.getResourceAsStream("/portraits/draft-ten-selected.jpg").use {
            android.graphics.BitmapFactory.decodeStream(it)!!
        }
        val source=DraftSequenceFixture.create(reference,false)
        val atlas=DraftNameAtlas.create(source,VisionCalibrationConfig(),emptyList())
        try {
            val tile=atlas.tiles.single { it.original && !it.ally && it.slot==4 }
            val lastLetter=android.graphics.Rect(
                (1186-tile.source.left)*tile.region.width()/tile.source.width(),
                (399-tile.source.top)*tile.region.height()/tile.source.height(),
                (1192-tile.source.left)*tile.region.width()/tile.source.width(),
                (414-tile.source.top)*tile.region.height()/tile.source.height())
            val focused=DraftNameAtlas.focusDetectedLine(atlas,tile,lastLetter)
            try { assertTrue(focused.tiles.single { it.original }.source.contains(android.graphics.Rect(1174,399,1192,414))) }
                finally { focused.bitmap.recycle() }
        } finally { atlas.bitmap.recycle();source.recycle();reference.recycle() }
    }
    @Test fun detectedLinesMapBackToTheOriginalTitleWithoutUsingANameHint() {
        val original=DraftNameAtlas.Tile(true,3,android.graphics.Rect(10,20,110,60),
            android.graphics.Rect(32,32,1032,432),400)
        val focused=original.copy(source=android.graphics.Rect(30,25,80,55),
            region=android.graphics.Rect(32,32,532,332))
        assertEquals(android.graphics.Rect(300,100,400,200),DraftNameAtlas.mapDetectedLine(original,focused,
            android.graphics.Rect(132,82,232,182)))
    }
    @Test fun projectedTitleInkSeparatesWhiteAndGoldFromBlueWithoutRestoringMasks() {
        val crop=Bitmap.createBitmap(6,1,Bitmap.Config.ARGB_8888)
        crop.setPixels(intArrayOf(Color.rgb(30,70,150),Color.WHITE,Color.rgb(230,180,60),
            Color.BLACK,Color.rgb(15,30,90),Color.rgb(80,80,80)),0,6,0,0,6,1)
        try {
            DraftNameAtlas.isolateTitleColor(crop)
            assertEquals(Color.WHITE,crop.getPixel(0,0))
            assertEquals(Color.BLACK,crop.getPixel(1,0))
            assertEquals(Color.BLACK,crop.getPixel(2,0))
            assertEquals("A black exclusion is always background",Color.WHITE,crop.getPixel(3,0))
            assertEquals(Color.WHITE,crop.getPixel(4,0))
            for(color in listOf(Color.BLACK,Color.WHITE,Color.rgb(30,70,150),Color.rgb(180,150,80))) {
                crop.eraseColor(color)
                DraftNameAtlas.isolateTitleColor(crop)
                for(x in 0 until crop.width) assertEquals("Uniform fields cannot acquire letter strokes",Color.WHITE,crop.getPixel(x,0))
            }
        } finally { crop.recycle() }
    }
    @Test fun detectedLineRetryRetainsBothLettersAndNeverPromotesThePlayerRow() {
        val source=javaClass.getResourceAsStream("/portraits/draft-ten-selected.jpg").use {
            android.graphics.BitmapFactory.decodeStream(it)!!
        }
        val atlas=DraftNameAtlas.create(source,VisionCalibrationConfig(),emptyList())
        try {
            val tile=atlas.tiles.single { it.original && it.ally && it.slot==4 }
            // Simulate a text detector returning only the V, not a champion label.
            val line=android.graphics.Rect(
                (155-tile.source.left)*tile.region.width()/tile.source.width(),
                (400-tile.source.top)*tile.region.height()/tile.source.height(),
                (166-tile.source.left)*tile.region.width()/tile.source.width(),
                (415-tile.source.top)*tile.region.height()/tile.source.height())
            val focused=DraftNameAtlas.focusDetectedLine(atlas,tile,line)
            try {
                val raw=focused.tiles.single { it.original }
                assertTrue("Padding must preserve I even if the detector returned only V",
                    raw.source.contains(android.graphics.Rect(155,400,173,415)))
                val top=(line.top-(line.height()*.20f).toInt().coerceAtLeast(2)).coerceAtLeast(0)
                val left=(line.left-(line.height()*.15f).toInt()).coerceAtLeast(0)
                assertEquals((minOf(tile.titleMaxY,(tile.region.height()*.85f).toInt())-top)
                    .coerceIn(0,raw.region.height()),raw.titleMaxY)
                for(y in 0 until raw.region.height()) for(x in 0 until raw.region.width()) {
                    assertEquals(atlas.bitmap.getPixel(tile.region.left+left+x,tile.region.top+top+y),
                        focused.bitmap.getPixel(raw.region.left+x,raw.region.top+y))
                }
            } finally { focused.bitmap.recycle() }
        } finally { atlas.bitmap.recycle();source.recycle() }
    }

    @Test fun detectedLineRetryRetainsExactPixelsAndDiagnosticExclusions() {
        val source=Bitmap.createBitmap(1280,579,Bitmap.Config.ARGB_8888).apply { eraseColor(Color.WHITE) }
        val config=VisionCalibrationConfig()
        val band=AdaptiveScreenLayoutEngine.calculateSlotNameRect(1280,579,false,4,config)
        val atlas=DraftNameAtlas.create(source,config,listOf(band))
        try {
            val tile=atlas.tiles.single { it.original && !it.ally && it.slot==4 }
            val line=android.graphics.Rect(100,60,150,100)
            val focused=DraftNameAtlas.focusDetectedLine(atlas,tile,line)
            try {
                val raw=focused.tiles.single { it.original }
                for(y in raw.region.top until raw.region.bottom) for(x in raw.region.left until raw.region.right)
                    assertEquals("Masked pixels cannot be restored by a tighter reading",Color.BLACK,focused.bitmap.getPixel(x,y))
                assertTrue(raw.titleMaxY<=raw.region.height())
                assertTrue(raw.titleMinY>=0)
                val playerRow=DraftNameAtlas.focusDetectedLine(atlas,tile,android.graphics.Rect(100,135,150,149))
                try {
                    val player=playerRow.tiles.single { it.original }
                    val playerCenter=142-(135-(14*.20f).toInt().coerceAtLeast(2))
                    assertTrue("A lower player-name line must remain outside the title acceptance region",
                        playerCenter>player.titleMaxY)
                } finally { playerRow.bitmap.recycle() }
            } finally { focused.bitmap.recycle() }
        } finally { atlas.bitmap.recycle();source.recycle() }
    }

    @Test fun bothRealViGlyphsStayInsideTheFocusedRegionAtTheFailingMeasures() {
        val reference=javaClass.getResourceAsStream("/portraits/draft-ten-selected.jpg").use {
            android.graphics.BitmapFactory.decodeStream(it)!!
        }
        try { for(firstPick in listOf(true,false)) {
            val source=DraftSequenceFixture.create(reference,firstPick)
            try { for((w,h,rightInset) in listOf(Triple(1280,579,0f),Triple(640,360,0f),
                Triple(1920,864,0f),Triple(854,480,.05f))) {
                val frame=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
                try {
                    val available=android.graphics.Rect(0,0,w-(w*rightInset).toInt(),h-if(rightInset>0) (h*.03f).toInt() else 0)
                    val scale=minOf(available.width().toFloat()/source.width,available.height().toFloat()/source.height)
                    val rw=(source.width*scale).toInt();val rh=(source.height*scale).toInt()
                    val viewport=android.graphics.Rect((available.width()-rw)/2,(available.height()-rh)/2,
                        (available.width()-rw)/2+rw,(available.height()-rh)/2+rh)
                    Canvas(frame).apply { drawColor(Color.BLACK);drawBitmap(source,null,viewport,Paint(Paint.FILTER_BITMAP_FLAG)) }
                    val adapted=com.example.service.screen.DraftFrameGeometry.forViewport(VisionCalibrationConfig(),viewport,w,h)
                    val config=com.example.service.screen.DraftRingAlignment.align(frame,adapted,null)
                    val atlas=DraftNameAtlas.create(frame,config,emptyList())
                    try {
                        val tile=atlas.tiles.single { it.original && it.ally==firstPick && it.slot==4 }
                        val bounds=DraftNameAtlas.titleFocusBounds(atlas,tile)!!
                        val focused=DraftNameAtlas.focusTitle(atlas,tile,bounds)
                        try {
                            val glyph=if(firstPick) android.graphics.Rect(155,400,173,415) else android.graphics.Rect(1174,399,1192,414)
                            val expected=android.graphics.Rect(viewport.left+glyph.left*rw/1280,viewport.top+glyph.top*rh/579,
                                viewport.left+(glyph.right*rw+1279)/1280,viewport.top+(glyph.bottom*rh+578)/579)
                            assertTrue("Both letters must remain at $w/$h, ally=$firstPick: $expected",
                                focused.tiles.single { it.original }.source.contains(expected))
                            if(firstPick) {
                                // Badge removal runs BEFORE upscaling. Compare
                                // the actual source glyph pixels at that boundary,
                                // not interpolation of neighbouring badge pixels.
                                val sample=Bitmap.createBitmap(frame,tile.source.left,tile.source.top,tile.source.width(),tile.source.height())
                                val cleaned=try { sample.copy(Bitmap.Config.ARGB_8888,true) }
                                    finally { if(sample!==frame) sample.recycle() }
                                try {
                                    com.example.service.screen.DraftImagePreprocessor.suppressLeadingMasteryBadge(cleaned)
                                    var strokes=0
                                    for(y in expected.top until expected.bottom) for(x in expected.left until expected.right) {
                                        val c=frame.getPixel(x,y)
                                        val channels=listOf(Color.red(c),Color.green(c),Color.blue(c))
                                        if(channels.min()>100 && channels.max()-channels.min()<70) {
                                            strokes++
                                            assertEquals("Removing a badge cannot erase a real source VI stroke at $w/$h ($x,$y)",
                                                c,cleaned.getPixel(x-tile.source.left,y-tile.source.top))
                                        }
                                    }
                                    assertTrue("The independent real title must contain visible ink",strokes>0)
                                } finally { cleaned.recycle() }
                            }
                        } finally { focused.bitmap.recycle() }
                    } finally { atlas.bitmap.recycle() }
                } finally { frame.recycle() }
            } } finally { source.recycle() }
        } } finally { reference.recycle() }
    }

    @Test fun focusedRetryKeepsTheActualTitlePixelsAndTheirEntireHeight() {
        val source=javaClass.getResourceAsStream("/portraits/draft-ten-selected.jpg").use {
            android.graphics.BitmapFactory.decodeStream(it)!!
        }
        val atlas=DraftNameAtlas.create(source,VisionCalibrationConfig(),emptyList())
        try {
            for(tile in atlas.tiles.filter { it.original }) {
                val bounds=DraftNameAtlas.titleFocusBounds(atlas,tile) ?: continue
                val focused=DraftNameAtlas.focusTitle(atlas,tile,bounds)
                try {
                    assertEquals("Established OCR input variants keep their original size",3,focused.tiles.size)
                    val color=DraftNameAtlas.focusTitle(atlas,tile,bounds,colorOnly=true)
                    try {
                        assertEquals(1,color.tiles.size)
                        assertEquals(kotlin.math.round((tile.region.height()+64)*72f/tile.region.height()).toInt(),color.bitmap.height)
                    } finally { color.bitmap.recycle() }
                    val raw=focused.tiles.single { it.original }
                    assertTrue(tile.source.contains(raw.source))
                    assertEquals("The focused crop is already title-only",raw.region.height(),raw.titleMaxY)
                    assertEquals("The focused crop is title-only from its first row",0,raw.titleMinY)
                    assertEquals(tile.region.height(),raw.region.height())
                    for(y in 0 until raw.region.height()) for(x in 0 until raw.region.width())
                        assertEquals(atlas.bitmap.getPixel(tile.region.left+bounds.left+x,tile.region.top+y),
                            focused.bitmap.getPixel(raw.region.left+x,raw.region.top+y))
                } finally { focused.bitmap.recycle() }
            }
        } finally { atlas.bitmap.recycle();source.recycle() }
    }

    @Test fun blankOrFullyMaskedTitlesNeverRequestAnIsolatedOcrReading() {
        for(color in listOf(Color.BLACK,Color.rgb(30,60,110))) {
            val source=Bitmap.createBitmap(1280,579,Bitmap.Config.ARGB_8888).apply { eraseColor(color) }
            val atlas=DraftNameAtlas.create(source,VisionCalibrationConfig(),emptyList())
            try { atlas.tiles.filter { it.original }.forEach { assertNull(DraftNameAtlas.titleFocusBounds(atlas,it)) } }
                finally { atlas.bitmap.recycle();source.recycle() }
        }
    }

    @Test fun everyCompleteCatalogTitleSurvivesFocusInWhiteAndGoldWithoutRedrawingInput() {
        val context=org.robolectric.RuntimeEnvironment.getApplication()
        com.example.data.WildRiftRepository.initChampions(context,forceReload=true)
        for(champion in com.example.data.WildRiftRepository.champions) for(color in listOf(Color.WHITE,Color.rgb(210,180,80))) {
            val source=Bitmap.createBitmap(1280,579,Bitmap.Config.ARGB_8888)
            val config=VisionCalibrationConfig()
            val band=AdaptiveScreenLayoutEngine.calculateSlotNameRect(source.width,source.height,true,0,config)
            val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color=color;textSize=9f }
            val text=champion.name.uppercase()
            val glyphs=android.graphics.Rect().also { paint.getTextBounds(text,0,text.length,it) }
            val x=band.left+8f;val baseline=band.top+band.height()*.70f
            Canvas(source).drawText(text,x,baseline,paint)
            val atlas=DraftNameAtlas.create(source,config,emptyList())
            try {
                val tile=atlas.tiles.single { it.original && it.ally && it.slot==0 }
                val bounds=DraftNameAtlas.titleFocusBounds(atlas,tile)!!
                val focused=DraftNameAtlas.focusTitle(atlas,tile,bounds)
                try {
                    val kept=focused.tiles.single { it.original }.source
                    val expected=android.graphics.Rect((x+glyphs.left).toInt(),(baseline+glyphs.top).toInt(),
                        kotlin.math.ceil((x+glyphs.right).toDouble()).toInt(),kotlin.math.ceil((baseline+glyphs.bottom).toDouble()).toInt())
                    assertTrue("The WHOLE $text title must survive: $expected in $kept",kept.contains(expected))
                } finally { focused.bitmap.recycle() }
            } finally { atlas.bitmap.recycle();source.recycle() }
        }
    }

    @Test fun realCaptureTitlesAndEmptyBanAreRecordedWithoutInventingSelections() {
        val context=org.robolectric.RuntimeEnvironment.getApplication()
        com.example.data.WildRiftRepository.initChampions(context,forceReload=true)
        for(fixture in DraftRealCaptureFixtures.captures) {
            val bytes=javaClass.getResourceAsStream("/portraits/${fixture.file}")!!.use { it.readBytes() }
            assertEquals(fixture.sha256,java.security.MessageDigest.getInstance("SHA-256").digest(bytes)
                .joinToString("") { "%02x".format(it) })
            fixture.titles.values.forEach { id ->
                val champion=com.example.data.WildRiftRepository.getChampionById(id)!!
                assertEquals(id,com.example.service.screen.ChampionNameResolver.findChampionInNameBand(
                    champion.name.uppercase(),com.example.data.WildRiftRepository.champions)?.id)
            }
            if(fixture.file=="draft-swain-eight.jpg") assertEquals(8,fixture.titles.size)
        }
        val frame=javaClass.getResourceAsStream("/portraits/draft-kaisa-nine.jpg").use {
            android.graphics.BitmapFactory.decodeStream(it)!!
        }
        try {
            val scanner=com.example.service.screen.BannedPortraitScanner()
            scanner.scan(frame,context,0)
            scanner.scan(frame,context,1500)
            val empty=scanner.slots.single { it.side==com.example.service.screen.BanSide.ALLY && it.index==1 }
            assertNull("Omitted ban cannot become a champion",empty.championId)
            assertEquals(com.example.service.screen.BanState.EMPTY,empty.state)
            assertEquals(10,scanner.slots.size)
        } finally { frame.recycle() }
    }

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
