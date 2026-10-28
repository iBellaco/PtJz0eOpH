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
    @Test fun shiftedLaneBandsKeepSummonerRowsOutAndPreserveMasksAtDifferentSizes() {
        for ((width, height) in listOf(1280 to 579, 1920 to 868, 2400 to 1080)) {
            val config = VisionCalibrationConfig()
            val source = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.WHITE) }
            val masked = AdaptiveScreenLayoutEngine.calculateAllyLaneRect(width, height, 4, config)
            val atlas = DraftNameAtlas.createAllyLanes(source, config, listOf(masked), listOf(1, 2, 3, 4))
            try {
                assertTrue(atlas.tiles.all { it.ally && it.laneOnly && it.slot in 1..4 })
                for (tile in atlas.tiles) {
                    val title = AdaptiveScreenLayoutEngine.calculateSlotNameRect(width, height, true, tile.slot, config)
                    assertTrue(tile.source.left < title.left)
                    assertEquals(title.top, tile.source.top)
                    assertEquals(title.bottom, tile.source.bottom)
                    if (tile.slot == 4) assertEquals(Color.BLACK,
                        atlas.bitmap.getPixel(tile.region.centerX(), tile.region.centerY()))
                }
            } finally { atlas.bitmap.recycle(); source.recycle() }
        }
    }
    @Test fun sceneContextIsBoundedAndExcludesTargetAndSummonerRows() {
        val frame = Bitmap.createBitmap(1920,889,Bitmap.Config.ARGB_8888)
        val config = VisionCalibrationConfig()
        val atlas = DraftNameAtlas.create(frame,config,emptyList())
        val scene = DraftNameAtlas.sceneTitles(frame,config,emptyList())
        try {
            val target = atlas.tiles.single { it.original && it.ally && it.slot == 4 }
            val boxes = scene.tiles.map { android.graphics.Rect(it.region).apply { inset(2,2) } }
            val donors = DraftNameAtlas.sceneContextSources(atlas,scene,boxes.reversed(),target)
            assertEquals(listOf(0,1,2),donors.map { it.first.slot })
            assertTrue(donors.all { it.first.ally })
            assertEquals(3,donors.size)
            for ((donor,bounds) in donors) {
                assertTrue(android.graphics.Rect(0,0,donor.region.width(),donor.region.height()).contains(bounds))
            }
            val targetBox = scene.tiles.single { it.ally && it.slot == 4 }.region
            val below = scene.tiles.first().region.let {
                android.graphics.Rect(it.left,it.bottom+1,it.right,it.bottom+4)
            }
            assertTrue(DraftNameAtlas.sceneContextSources(atlas,scene,
                listOf(targetBox,below,android.graphics.Rect()),target).isEmpty())
            val enemies = scene.tiles.filter { !it.ally }.map { android.graphics.Rect(it.region).apply { inset(2,2) } }
            assertTrue(DraftNameAtlas.sceneContextSources(atlas,scene,enemies,target).all { !it.first.ally })
        } finally { scene.bitmap.recycle();atlas.bitmap.recycle();frame.recycle() }
    }
    @Test fun neutralDebrisRemovalKeepsTallNarrowTitleStroke() {
        val bitmap = Bitmap.createBitmap(100,40,Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(bitmap).apply { drawColor(Color.WHITE) }
            val paint = Paint().apply { color = Color.BLACK }
            canvas.drawRect(5f,15f,6f,19f,paint)
            canvas.drawRect(20f,8f,36f,32f,paint)
            canvas.drawRect(45f,8f,48f,32f,paint)
            DraftNameAtlas.removeSmallNeutralDebris(bitmap)
            assertEquals(Color.WHITE,bitmap.getPixel(5,16))
            assertEquals(Color.BLACK,bitmap.getPixel(25,16))
            assertEquals("A narrow I must not be removed as debris",Color.BLACK,bitmap.getPixel(46,16))
        } finally { bitmap.recycle() }
    }
    @Test fun paleConnectedCrestEdgeIsRemovedButSeparateTitleStrokesRemain() {
        val crop = Bitmap.createBitmap(100,40,Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(crop).apply { drawColor(Color.rgb(20,30,70)) }
            val paint = Paint().apply { color = Color.rgb(220,170,60) }
            canvas.drawRect(4f,5f,20f,32f,paint)
            paint.color = Color.rgb(210,210,210)
            canvas.drawRect(20f,12f,25f,24f,paint)
            canvas.drawRect(29f,15f,32f,24f,paint)
            canvas.drawRect(36f,15f,39f,24f,paint)
            val baseline = crop.copy(Bitmap.Config.ARGB_8888,true)
            try {
                com.example.service.screen.DraftImagePreprocessor.suppressLeadingMasteryBadge(baseline)
                assertEquals("General OCR must retain its established gold-only mask",
                    Color.rgb(210,210,210),baseline.getPixel(23,18))
            } finally { baseline.recycle() }
            com.example.service.screen.DraftImagePreprocessor.suppressLeadingMasteryBadge(crop,includePaleEdges=true)
            assertEquals("Connected pale wing must not become an OCR letter",Color.BLACK,crop.getPixel(23,18))
            assertEquals("Separate title stroke must remain",Color.rgb(210,210,210),crop.getPixel(30,18))
            assertEquals(Color.rgb(210,210,210),crop.getPixel(37,18))
        } finally { crop.recycle() }
    }
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
        assertEquals(listOf(first,second,fallback),DraftNameAtlas.retryLineBounds(
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
    @Test fun measuredRetryBatchPreservesAllTenSlotsAndTheirNativeSizePixels() {
        val source=Bitmap.createBitmap(1280,579,Bitmap.Config.ARGB_8888).apply { eraseColor(Color.BLUE) }
        val atlas=DraftNameAtlas.create(source,VisionCalibrationConfig(),emptyList())
        try {
            for(nativeSize in listOf(false,true)) {
                val inputs=atlas.tiles.filter { it.original }.map { tile ->
                    val focused=DraftNameAtlas.focusDetectedLine(atlas,tile,android.graphics.Rect(20,30,100,50))
                    if(!nativeSize) focused else try { DraftNameAtlas.nativeTextSizes(focused,20) }
                        finally { focused.bitmap.recycle() }
                }
                try {
                    val pages=DraftNameAtlas.packAlternatives(inputs)
                    try {
                        assertTrue("Batch must reduce requests for ten unresolved slots",pages.size<inputs.size)
                        val expected=inputs.flatMap { input -> input.tiles.map { input to it } }
                        val actual=pages.flatMap { page -> page.tiles.map { page to it } }
                        assertEquals(expected.size,actual.size)
                        assertEquals(10,actual.map { it.second.ally to it.second.slot }.toSet().size)
                        expected.zip(actual).forEach { (before,after) ->
                            val (original,tile)=before
                            val (page,packed)=after
                            assertEquals(tile.copy(region=packed.region),packed)
                            val pixels=IntArray(tile.region.width()*tile.region.height())
                            val batchPixels=IntArray(pixels.size)
                            original.bitmap.getPixels(pixels,0,tile.region.width(),tile.region.left,tile.region.top,
                                tile.region.width(),tile.region.height())
                            page.bitmap.getPixels(batchPixels,0,packed.region.width(),packed.region.left,packed.region.top,
                                packed.region.width(),packed.region.height())
                            assertArrayEquals(pixels,batchPixels)
                        }
                    } finally { pages.forEach { it.bitmap.recycle() } }
                } finally { inputs.forEach { it.bitmap.recycle() } }
            }
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
    @Test fun detectedMasteryBadgeProducesACompactPixelOnlyTitleAlternative() {
        val source=javaClass.getResourceAsStream("/portraits/draft-ten-selected.jpg").use {
            android.graphics.BitmapFactory.decodeStream(it)!!
        }
        val atlas=DraftNameAtlas.create(source,VisionCalibrationConfig(),emptyList())
        try {
            val tile=atlas.tiles.single { it.original && it.ally && it.slot==4 }
            val cleaned=atlas.tiles.single { it.ally && it.slot==4 && !it.original && !it.contrast }
            assertTrue("The real mastery component must expose its removed margin",cleaned.contentInsetLeft>0)
            val compact=DraftNameAtlas.withoutLeadingBadge(atlas,tile)
            try {
                val result=compact.tiles.single()
                assertTrue(result.region.width()<tile.region.width())
                assertTrue(result.source.left>tile.source.left)
                assertTrue(tile.source.contains(result.source))
            } finally { compact.bitmap.recycle() }
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

    @Test fun realMelCaptureOffersBadgeFreeShortTitlePixelsAtEveryKnownFailingSize() {
        val raw=javaClass.getResourceAsStream("/portraits/draft-mel-nine.jpg").use {
            android.graphics.BitmapFactory.decodeStream(it)!!
        }
        try { for((height,measured) in listOf(
            822 to VisionCalibrationConfig(nameBandScale=1f,allySlotXRatios=List(5){.09453125f},
                enemySlotXRatios=List(5){.934375f},allySlotYRatios=listOf(.19343066f,.32791665f,.4604249f,.5919443f,.72445256f),
                enemySlotYRatios=listOf(.19102918f,.3270292f,.46002918f,.59302914f,.72502923f),
                allySlotDiameterRatios=listOf(.11f,.11f,.11f,.11f,.105f),enemySlotDiameterRatios=List(5){.11f}),
            889 to VisionCalibrationConfig(nameBandScale=.97637796f,allySlotXRatios=List(5){.07109375f},
                enemySlotXRatios=List(5){.9585937f},allySlotYRatios=listOf(.1989882f,.33052906f,.4601355f,.58877474f,.7183811f),
                enemySlotYRatios=listOf(.19883494f,.33162236f,.4614806f,.5913389f,.7202207f),
                allySlotDiameterRatios=List(5){.10740157f},enemySlotDiameterRatios=List(5){.10740157f}),
            924 to VisionCalibrationConfig(nameBandScale=.93939394f,allySlotXRatios=List(5){.0703125f},
                enemySlotXRatios=List(5){.9578125f},allySlotYRatios=listOf(.21428572f,.340504f,.4648661f,.5883001f,.7126623f),
                enemySlotYRatios=listOf(.21266234f,.33917627f,.4628995f,.5866226f,.70941556f),
                allySlotDiameterRatios=listOf(.10333333f,.10333333f,.10333333f,.10333333f,.09863636f),
                enemySlotDiameterRatios=List(5){.10333333f})
        )) {
            val frame=Bitmap.createBitmap(1920,height,Bitmap.Config.ARGB_8888)
            try {
                val scale=minOf(1920f/raw.width,height.toFloat()/raw.height)
                val width=(raw.width*scale).toInt();val renderedHeight=(raw.height*scale).toInt()
                val viewport=android.graphics.Rect((1920-width)/2,(height-renderedHeight)/2,
                    (1920-width)/2+width,(height-renderedHeight)/2+renderedHeight)
                Canvas(frame).apply { drawColor(Color.BLACK);drawBitmap(raw,null,viewport,Paint(Paint.FILTER_BITMAP_FLAG)) }
                // These are measured/manual frame-relative profiles. Production
                // deliberately does not normalize them through the black bars.
                val atlas=DraftNameAtlas.create(frame,measured,emptyList())
                try {
                    val tile=atlas.tiles.single { it.original && it.ally && it.slot==4 }
                    if (height == 889) {
                        // Actual ML Kit box from run 40311314615. The successful
                        // diagnostic used this scene title, not the widest crop.
                        val scene = DraftNameAtlas.sceneTitles(frame,measured,emptyList())
                        try {
                            val nativeBox = android.graphics.Rect(202,153,253,165)
                            val (donor,bounds) = DraftNameAtlas.sceneContextSources(atlas,scene,listOf(nativeBox),tile).single()
                            assertTrue(donor.ally)
                            assertEquals(0,donor.slot)
                            assertEquals(android.graphics.Rect(
                                (202-donor.source.left)*donor.region.width()/donor.source.width(),
                                (153-donor.source.top)*donor.region.height()/donor.source.height(),
                                (253-donor.source.left)*donor.region.width()/donor.source.width(),
                                (165-donor.source.top)*donor.region.height()/donor.source.height()),bounds)
                            val contextual = DraftNameAtlas.contextualTitle(atlas,tile,donor,bounds)!!
                            try { assertEquals(4,contextual.tiles.single().slot) }
                            finally { contextual.bitmap.recycle() }
                        } finally { scene.bitmap.recycle() }
                    }
                    val capturedPixels = IntArray(tile.source.width()*tile.source.height())
                    frame.getPixels(capturedPixels,0,tile.source.width(),tile.source.left,tile.source.top,
                        tile.source.width(),tile.source.height())
                    val alternatives=DraftNameAtlas.shortAlliedTitleAlternatives(atlas,tile)
                        ?: throw AssertionError("$height must produce a compact title retry")
                    try {
                        assertEquals(listOf(32,48,72,96),alternatives.tiles.map { it.region.height() })
                        val packed = DraftNameAtlas.packAlternatives(listOf(alternatives))
                        try {
                            for (page in packed) for (part in page.tiles) {
                                for (x in part.region.left until part.region.right) {
                                    assertEquals(Color.WHITE, page.bitmap.getPixel(x, part.region.top))
                                    assertEquals(Color.WHITE, page.bitmap.getPixel(x, part.region.bottom - 1))
                                }
                                for (y in part.region.top until part.region.bottom) {
                                    assertEquals(Color.WHITE, page.bitmap.getPixel(part.region.left, y))
                                    assertEquals(Color.WHITE, page.bitmap.getPixel(part.region.right - 1, y))
                                }
                            }
                        } finally { packed.forEach { it.bitmap.recycle() } }
                        assertTrue(alternatives.tiles.all { it.badgeRemoved && tile.source.contains(it.source) })
                        assertTrue("$height must scale ink rather than the empty title band",
                            alternatives.tiles.all { it.source.height() < tile.source.height() })
                        assertTrue("$height must exclude the distant bright background streak",
                            alternatives.tiles.all { it.source.width() < tile.source.width() / 3 })
                        assertTrue("$height retry must retain real dark title strokes on white",
                            alternatives.tiles.any { part ->
                                (part.region.top until part.region.bottom).any { y ->
                                    (part.region.left until part.region.right).any { x ->
                                        alternatives.bitmap.getPixel(x,y)==Color.BLACK
                                    }
                                }
                            })
                    } finally { alternatives.bitmap.recycle() }
                    val repeated=DraftNameAtlas.repeatedShortAlliedTitleAlternative(atlas,tile)
                        ?: throw AssertionError("$height must produce repeated optical evidence")
                    try {
                        assertEquals(1,repeated.tiles.size)
                        assertTrue(repeated.tiles.single().region.width()>repeated.tiles.single().region.height()*3)
                    } finally { repeated.bitmap.recycle() }
                    val neutral = DraftNameAtlas.shortAlliedTitleAlternatives(atlas,tile,neutralOnly=true)
                        ?: throw AssertionError("$height must retain neutral title strokes")
                    try {
                        assertTrue(neutral.tiles.all { tile.source.contains(it.source) })
                        assertTrue(neutral.tiles.any { part ->
                            (part.region.top until part.region.bottom).any { y ->
                                (part.region.left until part.region.right).any { x ->
                                    neutral.bitmap.getPixel(x,y) == Color.BLACK
                                }
                            }
                        })
                    } finally { neutral.bitmap.recycle() }
                    val color = DraftNameAtlas.shortAlliedTitleAlternatives(atlas,tile,binary=false,neutralOnly=true)!!
                    val repeatedColor = DraftNameAtlas.repeatedShortAlliedTitleAlternative(atlas,tile,neutralOnly=true,binary=false)!!
                    try {
                        val part = color.tiles.single { it.region.height() == 32 }
                        val target = repeatedColor.tiles.single().region
                        for (copy in 0..2) for (y in 0 until part.region.height())
                            for (x in 0 until part.region.width())
                                assertEquals("Repeated OCR evidence must contain only original captured pixels",
                                    color.bitmap.getPixel(part.region.left+x,part.region.top+y),
                                    repeatedColor.bitmap.getPixel(target.left+copy*(part.region.width()+24)+x,target.top+y))
                    } finally { color.bitmap.recycle(); repeatedColor.bitmap.recycle() }
                    val donor = atlas.tiles.single { it.original && it.ally && it.slot == 0 }
                    val contextual = DraftNameAtlas.contextualTitle(atlas,tile,donor,
                        android.graphics.Rect(0,0,donor.region.width(),donor.region.height()))!!
                    try {
                        val assigned = contextual.tiles.single()
                        assertTrue(assigned.contextual)
                        assertEquals(tile.slot,assigned.slot)
                        assertTrue(tile.source.contains(assigned.source))
                        assertFalse("Left context must never be assigned to the target",assigned.region.contains(32,32))
                        assertFalse("Right context must never be assigned to the target",
                            assigned.region.contains(contextual.bitmap.width-33,32))
                    } finally { contextual.bitmap.recycle() }
                    val after = IntArray(capturedPixels.size)
                    assertFalse("The atlas must never recycle its borrowed capture",frame.isRecycled)
                    frame.getPixels(after,0,tile.source.width(),tile.source.left,tile.source.top,
                        tile.source.width(),tile.source.height())
                    org.junit.Assert.assertArrayEquals("Native-resolution cleanup must not modify the capture",capturedPixels,after)
                } finally { atlas.bitmap.recycle() }
            } finally { frame.recycle() }
        } } finally { raw.recycle() }
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
