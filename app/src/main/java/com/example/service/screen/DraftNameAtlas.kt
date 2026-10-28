package com.example.service.screen

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect

/** Isolated title bands in one OCR request, including an intact copy of every allied title. */
internal class DraftNameAtlas private constructor(val bitmap: Bitmap, val tiles: List<Tile>,
    // Borrowed for this scan only; never recycled or modified by the atlas.
    private val capturedFrame: Bitmap? = null, private val captureMasks: List<Rect> = emptyList()) {
    data class Tile(val ally: Boolean, val slot: Int, val source: Rect, val region: Rect,
                    val titleMaxY: Int, val original: Boolean = true, val contrast: Boolean = false,
                    val titleMinY: Int = 0, val badgeRemoved: Boolean = false,
                    val contentInsetLeft: Int = 0, val contextual: Boolean = false,
                    val laneOnly: Boolean = false)

    companion object {
        private const val GUTTER = 32

        /** Pack alternatives without rescaling their pixels. Bounded pages keep
         * the detector from shrinking small glyphs and avoid one request per crop.
         * Inputs remain owned by the caller; returned pages own independent pixels. */
        fun packAlternatives(inputs: List<DraftNameAtlas>): List<DraftNameAtlas> {
            val pages = mutableListOf<DraftNameAtlas>()
            val pending = mutableListOf<Pair<DraftNameAtlas, Tile>>()
            var pageHeight = GUTTER
            fun flush() {
                if (pending.isEmpty()) return
                val width = pending.maxOf { it.second.region.width() } + 2 * GUTTER
                val bitmap = Bitmap.createBitmap(width, pageHeight, Bitmap.Config.ARGB_8888)
                try {
                    val canvas = Canvas(bitmap).apply { drawColor(Color.BLACK) }
                    var top = GUTTER
                    val tiles = pending.map { (input, tile) ->
                        val region = Rect(GUTTER, top, GUTTER + tile.region.width(), top + tile.region.height())
                        canvas.drawBitmap(input.bitmap, tile.region, region, null)
                        top = region.bottom + GUTTER
                        tile.copy(region = region)
                    }
                    pages += DraftNameAtlas(bitmap, tiles)
                } catch (error: Throwable) { bitmap.recycle(); throw error }
                pending.clear()
                pageHeight = GUTTER
            }
            try {
                for (input in inputs) for (tile in input.tiles) {
                    if (pageHeight + tile.region.height() + GUTTER > 1536) flush()
                    pending += input to tile
                    pageHeight += tile.region.height() + GUTTER
                }
                flush()
                return pages
            } catch (error: Throwable) { pages.forEach { it.bitmap.recycle() }; throw error }
        }

        /** The first native box may cover a badge or only one glyph. Keep two
         * distinct measured boxes plus the full calibrated row, without guesses. */
        fun retryLineBounds(detected: List<Rect>, fallback: Rect): List<Rect> =
            (detected.filter { it.width() > 0 && it.height() > 0 }.distinct().take(2) + fallback)
                .filter { it.width() > 0 && it.height() > 0 }.distinct()

        private fun compactColumns(tiles:Int,cellWidth:Int,cellHeight:Int):Int =
            (1..minOf(4,tiles)).minBy { columns ->
                maxOf(columns*cellWidth,((tiles+columns-1)/columns)*cellHeight)
            }

        internal fun mapDetectedLine(original: Tile, tile: Tile, box: Rect): Rect {
            require(original.ally==tile.ally && original.slot==tile.slot)
            fun sourceX(x:Int)=tile.source.left+(x-tile.region.left)*tile.source.width()/tile.region.width()
            fun sourceY(y:Int)=tile.source.top+(y-tile.region.top)*tile.source.height()/tile.region.height()
            return Rect((sourceX(box.left)-original.source.left)*original.region.width()/original.source.width(),
                (sourceY(box.top)-original.source.top)*original.region.height()/original.source.height(),
                (sourceX(box.right)-original.source.left)*original.region.width()/original.source.width(),
                (sourceY(box.bottom)-original.source.top)*original.region.height()/original.source.height())
        }

        /** Native scene boxes, not boxes inferred from enhanced/repacked crops.
         * Callers supply only whole champion-title lines validated by the reader.
         * At most three other title bands may supply context; none is an answer
         * for the target slot. Stable spatial order avoids preferring long names. */
        fun sceneContextSources(atlas: DraftNameAtlas, scene: DraftNameAtlas,
                                titleBoxes: List<Rect>, target: Tile): List<Pair<Tile, Rect>> =
            scene.tiles.asSequence()
                .filter { it.ally != target.ally || it.slot != target.slot }
                .sortedWith(compareByDescending<Tile> { it.ally }.thenBy { it.slot })
                .mapNotNull { tile ->
                    val box = titleBoxes.firstOrNull {
                        it.width() > 0 && it.height() > 0 && tile.region.contains(it)
                    } ?: return@mapNotNull null
                    val original = atlas.tiles.single {
                        it.original && it.ally == tile.ally && it.slot == tile.slot
                    }
                    original to mapDetectedLine(original, tile, box)
                }.take(3).toList()

        /** Preserve the original scene for the detector when isolated two-letter
         * titles remain unreadable. Only calibrated TITLE regions are decoded;
         * the centre champion, summoner row and overlays cannot supply identities. */
        fun sceneTitles(source: Bitmap, config: VisionCalibrationConfig, masks: List<Rect>): DraftNameAtlas {
            val scale=(1920f/source.width).coerceIn(1f,3f)
            fun low(value:Int)=kotlin.math.floor(value*scale).toInt()
            fun high(value:Int)=kotlin.math.ceil(value*scale).toInt()
            val bitmap=Bitmap.createBitmap(high(source.width),high(source.height),Bitmap.Config.ARGB_8888)
            val input=if(masks.isEmpty()) source else source.copy(Bitmap.Config.ARGB_8888,true)
            try {
                val paint=Paint().apply { color=Color.BLACK }
                if(input!==source) masks.forEach { Canvas(input).drawRect(it,paint) }
                val canvas=Canvas(bitmap)
                canvas.drawBitmap(input,null,Rect(0,0,bitmap.width,bitmap.height),Paint(Paint.FILTER_BITMAP_FLAG))
                masks.forEach { canvas.drawRect(Rect(low(it.left),low(it.top),high(it.right),high(it.bottom)),paint) }
                val tiles=(0..4).flatMap { slot -> listOf(true,false).map { ally ->
                    val band=AdaptiveScreenLayoutEngine.calculateSlotNameRect(source.width,source.height,ally,slot,config)
                    val region=Rect(low(band.left),low(band.top),high(band.right),high(band.bottom))
                    Tile(ally,slot,band,region,region.height())
                } }
                return DraftNameAtlas(bitmap,tiles)
            } catch(error:Throwable) { bitmap.recycle();throw error }
            finally { if(input!==source) input.recycle() }
        }

        /** Re-read only unresolved titles at full tile size, avoiding a wide atlas
         * shrinking a two-letter name inside the native text detector. No new text pixels. */
        fun retryUnresolved(atlas: DraftNameAtlas, unresolved: List<Tile>): DraftNameAtlas {
            require(unresolved.isNotEmpty() && unresolved.all { it.original && it in atlas.tiles })
            val cellWidth = unresolved.maxOf { it.region.width() } + GUTTER*2
            val cellHeight = unresolved.maxOf { it.region.height() } + GUTTER*2
            val columns=compactColumns(unresolved.size*3,cellWidth,cellHeight)
            val rows=(unresolved.size*3+columns-1)/columns
            val bitmap = Bitmap.createBitmap(cellWidth*columns,cellHeight*rows,Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap).apply { drawColor(Color.BLACK) }
            try {
                val tiles = unresolved.flatMapIndexed { row, tile ->
                    (0..2).map { variant ->
                        val contrast=variant==1
                        val binary=variant==2
                        val prior = if ((contrast || binary) && tile.ally) atlas.tiles.first {
                            it.ally && it.slot==tile.slot && !it.original && it.contrast==contrast
                        } else tile
                        val sampled = Bitmap.createBitmap(atlas.bitmap,prior.region.left,prior.region.top,
                            prior.region.width(),prior.region.height())
                        val crop = try { sampled.copy(Bitmap.Config.ARGB_8888,true) }
                            finally { if(sampled!==atlas.bitmap) sampled.recycle() }
                        try {
                            if (contrast && !tile.ally) enhanceTitleContrast(crop)
                            if (binary) isolateBrightTitleStrokes(crop)
                            val index=row*3+variant
                            val left = (index%columns)*cellWidth+GUTTER
                            val top = (index/columns)*cellHeight+GUTTER
                            canvas.drawBitmap(crop,left.toFloat(),top.toFloat(),null)
                            tile.copy(region=Rect(left,top,left+crop.width,top+crop.height),
                                original=variant==0,contrast=variant!=0)
                        } finally { crop.recycle() }
                    }
                }
                return DraftNameAtlas(bitmap,tiles)
            } catch (error: Throwable) { bitmap.recycle(); throw error }
        }

        /** Locate possible title strokes, never a champion identity. Large side
         * padding retains the whole word, including weak leading/trailing glyphs.
         * Uniform/fully masked slots do not need more native OCR requests. */
        fun titleFocusBounds(atlas: DraftNameAtlas, tile: Tile): Rect? {
            require(tile.original && tile in atlas.tiles)
            val w=tile.region.width();val h=tile.region.height()
            val pixels=IntArray(w*h)
            atlas.bitmap.getPixels(pixels,0,w,tile.region.left,tile.region.top,w,h)
            fun light(index:Int):Int {
                val c=pixels[index]
                return (Color.red(c)*299+Color.green(c)*587+Color.blue(c)*114)/1000
            }
            val radius=(h*.08f).toInt().coerceAtLeast(2)
            var left=w;var right=-1
            for(y in (h*.15f).toInt() until (h*.85f).toInt()) for(x in 0 until w) {
                val c=pixels[y*w+x];val r=Color.red(c);val g=Color.green(c);val b=Color.blue(c)
                val neutral=maxOf(r,g,b)-minOf(r,g,b)<=70
                val warm=r>95 && g>65 && r>=g*.85f && g>=b*.90f
                if(!neutral && !warm) continue
                val value=light(y*w+x)
                if(value<70) continue
                val mean=(light(y*w+(x-radius).coerceAtLeast(0))+
                    light(y*w+(x+radius).coerceAtMost(w-1))+
                    light((y-radius).coerceAtLeast(0)*w+x)+
                    light((y+radius).coerceAtMost(h-1)*w+x))/4
                if(value>=mean+5) { left=minOf(left,x);right=maxOf(right,x) }
            }
            if(right<left) return null
            val padding=maxOf(h,(w*.20f).toInt())
            // Keep the entire vertical band: a fixed .80 cutoff clipped the
            // bottom of short titles in some measured configurations.
            return Rect((left-padding).coerceAtLeast(0),0,(right+padding+1).coerceAtMost(w),h)
        }

        /** A compact reading of intact text pixels, not a rendered name or a
         * portrait fallback. In particular, retain BOTH strokes of VI. */
        fun focusTitle(atlas: DraftNameAtlas, tile: Tile, bounds: Rect, colorOnly: Boolean = false): DraftNameAtlas {
            require(tile.original && tile in atlas.tiles && bounds.top==0 &&
                bounds.bottom==tile.region.height() && bounds.left>=0 && bounds.right<=tile.region.width() && bounds.width()>0)
            val width=bounds.width();val height=bounds.height()
            val variants=if(colorOnly) listOf(3) else (0..2).toList()
            val bitmap=Bitmap.createBitmap(width+GUTTER*2,(height+GUTTER*2)*variants.size,Bitmap.Config.ARGB_8888)
            val canvas=Canvas(bitmap).apply { drawColor(Color.BLACK) }
            val tiles=mutableListOf<Tile>()
            try {
                for((index,variant) in variants.withIndex()) {
                    val prior=if(variant==3 && tile.ally) atlas.tiles.first {
                        it.ally && it.slot==tile.slot && !it.original && !it.contrast
                    } else tile
                    val sample=Bitmap.createBitmap(atlas.bitmap,prior.region.left+bounds.left,prior.region.top,width,height)
                    val crop=try { sample.copy(Bitmap.Config.ARGB_8888,true) }
                        finally { if(sample!==atlas.bitmap) sample.recycle() }
                    try {
                        if(variant==1) enhanceTitleContrast(crop)
                        if(variant==2) isolateBrightTitleStrokes(crop)
                        if(variant==3) isolateTitleColor(crop)
                        val top=index*(height+GUTTER*2)+GUTTER
                        canvas.drawBitmap(crop,GUTTER.toFloat(),top.toFloat(),null)
                        val source=Rect(tile.source.left+bounds.left*tile.source.width()/tile.region.width(),
                            tile.source.top,tile.source.left+(bounds.right*tile.source.width()+tile.region.width()-1)/tile.region.width(),tile.source.bottom)
                        tiles+=tile.copy(source=source,region=Rect(GUTTER,top,GUTTER+width,top+height),
                            original=variant==0,contrast=variant!=0,
                            // The source rect is already the calibrated title
                            // band and ends before the summoner row. Keep its
                            // complete vertical extent: at small scales ML Kit
                            // can place VI/JHIN in the upper third.
                            titleMinY=0,
                            // This crop contains only the calibrated title band:
                            // calculateSlotNameRect already ends before the summoner row.
                            // Reusing the atlas-relative cutoff here rejected a real VI
                            // line near the bottom after the focused crop was repacked.
                            titleMaxY=height)
                    } finally { crop.recycle() }
                }
                if(colorOnly) {
                    // Keep isolated glyphs near the native recognizer's normal
                    // text size instead of presenting an enlarged two-letter logo.
                    val factor=72f/height
                    val resized=Bitmap.createScaledBitmap(bitmap,
                        kotlin.math.round(bitmap.width*factor).toInt().coerceAtLeast(1),
                        kotlin.math.round(bitmap.height*factor).toInt().coerceAtLeast(1),false)
                    val adjusted=tiles.map { part -> part.copy(region=Rect(
                        kotlin.math.round(part.region.left*factor).toInt(),kotlin.math.round(part.region.top*factor).toInt(),
                        kotlin.math.round(part.region.right*factor).toInt(),kotlin.math.round(part.region.bottom*factor).toInt()),
                        titleMinY=kotlin.math.round(part.titleMinY*factor).toInt(),
                        titleMaxY=kotlin.math.round(part.titleMaxY*factor).toInt()) }
                    if(resized!==bitmap) bitmap.recycle()
                    return DraftNameAtlas(resized,adjusted)
                }
                return DraftNameAtlas(bitmap,tiles)
            } catch(error:Throwable) { bitmap.recycle();throw error }
        }

        /** Re-run the native recognizer on a detected text line, not its guessed
         * identity. A detector returning only V still supplies a useful box: the
         * padding retains the adjacent I. All variants use existing image pixels.
         * The original, full-height band is always read before this alternative. */
        fun focusDetectedLine(atlas: DraftNameAtlas, tile: Tile, line: Rect): DraftNameAtlas {
            require(tile.original && tile in atlas.tiles && line.width()>0 && line.height()>0)
            val leftPadding=if(!tile.ally || line.width()<line.height()/2) line.height()
                else (line.height()*.15f).toInt()
            val horizontal=line.height()
            val vertical=(line.height()*.20f).toInt().coerceAtLeast(2)
            val bounds=Rect((line.left-leftPadding).coerceAtLeast(0),
                (line.top-vertical).coerceAtLeast(0),
                (line.right+horizontal).coerceAtMost(tile.region.width()),
                (line.bottom+vertical).coerceAtMost(tile.region.height()))
            require(bounds.width()>0 && bounds.height()>0)
            val width=bounds.width();val height=bounds.height()
            val bitmap=Bitmap.createBitmap(width+GUTTER*2,(height+GUTTER*2)*3,Bitmap.Config.ARGB_8888)
            val canvas=Canvas(bitmap).apply { drawColor(Color.BLACK) }
            try {
                val tiles=(0..2).map { variant ->
                    val prior=if(variant!=0 && tile.ally) atlas.tiles.first {
                        it.ally && it.slot==tile.slot && !it.original && !it.contrast
                    } else tile
                    val sample=Bitmap.createBitmap(atlas.bitmap,prior.region.left+bounds.left,
                        prior.region.top+bounds.top,width,height)
                    val crop=try { sample.copy(Bitmap.Config.ARGB_8888,true) }
                        finally { if(sample!==atlas.bitmap) sample.recycle() }
                    try {
                        if(variant==1) enhanceTitleContrast(crop)
                        if(variant==2) isolateBrightTitleStrokes(crop)
                        if(variant==3) isolateTitleColor(crop)
                        val top=variant*(height+GUTTER*2)+GUTTER
                        canvas.drawBitmap(crop,GUTTER.toFloat(),top.toFloat(),null)
                        val source=Rect(tile.source.left+bounds.left*tile.source.width()/tile.region.width(),
                            tile.source.top+bounds.top*tile.source.height()/tile.region.height(),
                            tile.source.left+(bounds.right*tile.source.width()+tile.region.width()-1)/tile.region.width(),
                            tile.source.top+(bounds.bottom*tile.source.height()+tile.region.height()-1)/tile.region.height())
                        tile.copy(source=source,region=Rect(GUTTER,top,GUTTER+width,top+height),
                            original=variant==0,contrast=variant!=0,
                            // The focused crop already starts at the calibrated
                            // title band; a second 30% cutoff can remove the
                            // first row on short 640x360 captures.
                            titleMinY=0,
                            titleMaxY=(minOf(tile.titleMaxY,(tile.region.height()*.85f).toInt())-bounds.top).coerceIn(0,height))
                    } finally { crop.recycle() }
                }
                return DraftNameAtlas(bitmap,tiles)
            } catch(error:Throwable) { bitmap.recycle();throw error }
        }

        /**
         * Re-read the existing badge-suppressed alternative at full band width.
         * Never crop a percentage of the band: it can remove title letters.
         * The badge is part of the captured pixels, but it is not the
         * champion title and ML Kit can merge its silhouette with short names
         * such as "Vi". The crop never supplies a name or a portrait; it only
         * gives OCR the same title pixels with the calibrated badge margin out
         * of its text line.
         */
        fun withoutLeadingBadge(atlas: DraftNameAtlas, tile: Tile): DraftNameAtlas {
            require(tile.original && tile in atlas.tiles && tile.ally)
            val prior = atlas.tiles.single { it.ally && it.slot == tile.slot && !it.original && !it.contrast }
            val inset = prior.contentInsetLeft.coerceIn(0,(prior.region.width()-1).coerceAtLeast(0))
            // Once a real badge was removed, omit its black margin and cap this
            // alternative to a compact title window. The intact and contrast
            // variants still retain the full band for long champion names.
            val width = if(inset>0) minOf(prior.region.width()-inset,(prior.region.height()*2.5f).toInt())
                else tile.region.width()
            val height = tile.region.height()
            val bitmap = Bitmap.createBitmap(width + GUTTER * 2, height + GUTTER * 2,
                Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap).apply { drawColor(Color.BLACK) }
            try {
                val crop = Bitmap.createBitmap(atlas.bitmap, prior.region.left+inset,
                    prior.region.top, width, height)
                try {
                    canvas.drawBitmap(crop, GUTTER.toFloat(), GUTTER.toFloat(), null)
                } finally { crop.recycle() }
                val source = Rect(
                    tile.source.left+inset*tile.source.width()/prior.region.width(),
                    tile.source.top,
                    tile.source.left+(inset+width)*tile.source.width()/prior.region.width(),
                    tile.source.bottom
                )
                return DraftNameAtlas(bitmap, listOf(tile.copy(
                    source = source,
                    region = Rect(GUTTER, GUTTER, GUTTER + width, GUTTER + height),
                    titleMinY = tile.titleMinY,
                    titleMaxY = tile.titleMaxY,
                    badgeRemoved = true,
                    contentInsetLeft = 0
                )))
            } catch (error: Throwable) {
                bitmap.recycle()
                throw error
            }
        }

        /**
         * Present a genuinely short allied title to the native recognizer without
         * the mastery badge or the empty margin that makes two glyphs look like an
         * icon.  The samples contain only captured pixels: no champion name,
         * expected value or rendered glyph is introduced.
         */
        fun shortAlliedTitleAlternatives(atlas: DraftNameAtlas, tile: Tile, binary: Boolean = true,
            neutralOnly: Boolean = false): DraftNameAtlas? {
            require(tile.original && tile in atlas.tiles && tile.ally)
            val prior = atlas.tiles.single {
                it.ally && it.slot == tile.slot && !it.original && !it.contrast
            }
            val inset = prior.contentInsetLeft.coerceIn(0, (prior.region.width() - 1).coerceAtLeast(0))
            if (inset == 0) return null
            val sourceWidth = minOf(prior.region.width() - inset,
                (prior.region.height() * 2.5f).toInt().coerceAtLeast(1))
            val sourceHeight = prior.region.height()
            val targets = listOf(32, 48, 72, 96)
            val cellWidth = targets.maxOf { target ->
                kotlin.math.round(sourceWidth * target.toFloat() / sourceHeight).toInt()
            } + GUTTER * 2
            val cellHeight = targets.max() + GUTTER * 2
            val bitmap = Bitmap.createBitmap(cellWidth, cellHeight * targets.size,
                Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap).apply { drawColor(Color.BLACK) }
            try {
                // Preserve an independent original-color sample. Thresholding
                // and badge erasure can damage a faint two-letter title.
                val sample = Bitmap.createBitmap(atlas.bitmap, tile.region.left + inset,
                    tile.region.top, sourceWidth, sourceHeight)
                try {
                    // Measure ink on the badge-suppressed pixels. Measuring the
                    // original sample reintroduced a pale crest wing touching V
                    // and made ML Kit read NI instead of the complete VI title.
                    // Remove the pale crest on the COMPLETE band before cropping
                    // its leading margin. A partial crest no longer has the tall
                    // core needed to distinguish its wing from a title letter.
                    val captured = atlas.capturedFrame
                    val fullSample = if (captured != null)
                        Bitmap.createBitmap(captured,tile.source.left,tile.source.top,tile.source.width(),tile.source.height())
                    else Bitmap.createBitmap(atlas.bitmap,tile.region.left,tile.region.top,
                        tile.region.width(),tile.region.height())
                    val fullMask = try { fullSample.copy(Bitmap.Config.ARGB_8888,true) }
                        finally { if(fullSample !== atlas.bitmap && fullSample !== captured) fullSample.recycle() }
                    val isolated = try {
                        if (captured != null) {
                            val maskCanvas = Canvas(fullMask)
                            val maskPaint = Paint().apply { color = Color.BLACK }
                            atlas.captureMasks.forEach { mask ->
                                maskCanvas.drawRect(Rect(mask).apply { offset(-tile.source.left,-tile.source.top) },maskPaint)
                            }
                        }
                        DraftImagePreprocessor.suppressLeadingMasteryBadge(fullMask,includePaleEdges=true)
                        val scaledMask = Bitmap.createScaledBitmap(fullMask,tile.region.width(),tile.region.height(),true)
                        try {
                            val cropped = Bitmap.createBitmap(scaledMask,inset,0,sourceWidth,sourceHeight)
                            try { cropped.copy(Bitmap.Config.ARGB_8888,true) }
                            finally { if(cropped !== scaledMask) cropped.recycle() }
                        } finally { if(scaledMask !== fullMask) scaledMask.recycle() }
                    } finally { fullMask.recycle() }
                    try {
                        isolateTitleColor(isolated)
                        if (neutralOnly) {
                            // Independent white-title alternative: the warm gold
                            // wing can remain connected to a neutral V after the
                            // general white/gold projection. Preserve the normal
                            // alternatives for golden titles such as Jhin.
                            for (y in 0 until sourceHeight) for (x in 0 until sourceWidth) {
                                val c = sample.getPixel(x, y)
                                val r = Color.red(c); val g = Color.green(c); val b = Color.blue(c)
                                if (maxOf(r,g,b) - minOf(r,g,b) > 40 || minOf(r,g,b) < 90)
                                    isolated.setPixel(x, y, Color.WHITE)
                            }
                            removeSmallNeutralDebris(isolated)
                        }
                        // Scale the actual ink, not the empty calibrated band.
                        // Otherwise a 72px row can still contain a 10px VI and
                        // repeating that row only repeats the same empty margin.
                        var inkLeft = sourceWidth
                        var inkTop = sourceHeight
                        var inkRight = -1
                        var inkBottom = -1
                        for (y in 0 until sourceHeight) for (x in 0 until sourceWidth) {
                            if (isolated.getPixel(x, y) == Color.BLACK) {
                                inkLeft = minOf(inkLeft, x); inkRight = maxOf(inkRight, x)
                                inkTop = minOf(inkTop, y); inkBottom = maxOf(inkBottom, y)
                            }
                        }
                        if (inkRight < inkLeft) { bitmap.recycle(); return null }
                        // A distant bright background stroke is not part of the
                        // leading title. Split on an empty column gap measured
                        // from this band, rather than scaling all page ink as a
                        // single word (which reduced VI to a few pixels).
                        val maxGap = (sourceHeight * .12f).toInt().coerceAtLeast(3)
                        var gap = 0
                        var wordRight = inkLeft
                        for (x in inkLeft..inkRight) {
                            val hasInk = (0 until sourceHeight).any { y -> isolated.getPixel(x, y) == Color.BLACK }
                            if (hasInk) { wordRight = x; gap = 0 } else gap++
                            if (gap > maxGap) break
                        }
                        inkRight = wordRight
                        inkTop = sourceHeight; inkBottom = -1
                        for (y in 0 until sourceHeight) for (x in inkLeft..inkRight) {
                            if (isolated.getPixel(x, y) == Color.BLACK) {
                                inkTop = minOf(inkTop, y); inkBottom = maxOf(inkBottom, y)
                            }
                        }
                        val inkBounds = Rect(inkLeft, inkTop, inkRight + 1, inkBottom + 1)
                        val source = Rect(
                            tile.source.left + (inset + inkLeft) * tile.source.width() / prior.region.width(),
                            tile.source.top + inkTop * tile.source.height() / sourceHeight,
                            tile.source.left + (inset + inkRight + 1) * tile.source.width() / prior.region.width(),
                            tile.source.top + (inkBottom + 1) * tile.source.height() / sourceHeight
                        )
                        val tiles = targets.mapIndexed { index, target ->
                            // Padding must be INSIDE the tile: packAlternatives
                            // copies only region, dropping the atlas gutters.
                            // Black ink touching a black page merges with it.
                            val padding = 8
                            val width = (kotlin.math.round(inkBounds.width() * (target - 2 * padding).toFloat() / inkBounds.height())
                                .toInt() + 2 * padding).coerceIn(2 * padding + 1, cellWidth - GUTTER * 2)
                            val top = index * cellHeight + GUTTER
                            val region = Rect(GUTTER, top, GUTTER + width, top + target)
                            canvas.drawRect(region, Paint().apply { color = Color.WHITE })
                            val textRegion = Rect(region).apply { inset(padding, padding) }
                            canvas.drawBitmap(if (binary) isolated else sample, inkBounds, textRegion, Paint(Paint.FILTER_BITMAP_FLAG))
                            tile.copy(source = source, region = region, original = false,
                                contrast = true, titleMinY = 0, titleMaxY = target,
                                badgeRemoved = true, contentInsetLeft = 0)
                        }
                        return DraftNameAtlas(bitmap, tiles)
                    } finally { isolated.recycle() }
                } finally { sample.recycle() }
            } catch (error: Throwable) {
                bitmap.recycle()
                throw error
            }
        }

        /** Context contains captured text only. Only the central tile is decoded;
         * neighbouring text supplies detection context, never a slot identity. */
        fun contextualTitle(atlas: DraftNameAtlas, tile: Tile, donor: Tile, bounds: Rect): DraftNameAtlas? {
            val short = shortAlliedTitleAlternatives(atlas,tile,binary=false,neutralOnly=true) ?: return null
            try {
                val clipped = Rect(bounds)
                if (!clipped.intersect(0,0,donor.region.width(),donor.region.height())) return null
                val target = short.tiles.single { it.region.height() == 32 }
                val ink = Rect(target.region).apply { inset(8,8) }
                val textHeight = ink.height()
                val donorWidth = (clipped.width()*textHeight.toFloat()/clipped.height()).toInt().coerceAtLeast(1)
                val left = GUTTER + donorWidth + 24
                val region = Rect(left,GUTTER,left+ink.width(),GUTTER+textHeight)
                val output = Bitmap.createBitmap(region.right+24+donorWidth+GUTTER,textHeight+GUTTER*2,Bitmap.Config.ARGB_8888)
                try {
                    val canvas = Canvas(output).apply { drawColor(Color.BLACK) }
                    val donorSource = Rect(clipped).apply { offset(donor.region.left,donor.region.top) }
                    val paint = Paint(Paint.FILTER_BITMAP_FLAG)
                    canvas.drawBitmap(atlas.bitmap,donorSource,Rect(GUTTER,GUTTER,GUTTER+donorWidth,GUTTER+textHeight),paint)
                    canvas.drawBitmap(short.bitmap,ink,region,null)
                    canvas.drawBitmap(atlas.bitmap,donorSource,Rect(region.right+24,GUTTER,region.right+24+donorWidth,GUTTER+textHeight),paint)
                    val acceptedRegion = Rect(region).apply { inset(-4,-4) }
                    return DraftNameAtlas(output,listOf(target.copy(region=acceptedRegion,titleMinY=0,titleMaxY=acceptedRegion.height(),contextual=true)))
                } catch(error:Throwable) { output.recycle();throw error }
            } finally { short.bitmap.recycle() }
        }

        internal fun removeSmallNeutralDebris(bitmap: Bitmap) {
            val width = bitmap.width; val height = bitmap.height
            val seen = BooleanArray(width * height)
            val components = mutableListOf<Pair<List<Int>, Int>>()
            for (start in seen.indices) {
                if (seen[start] || bitmap.getPixel(start % width, start / width) != Color.BLACK) continue
                val queue = ArrayDeque<Int>().apply { add(start) }
                val pixels = mutableListOf<Int>()
                var top = height; var bottom = -1
                seen[start] = true
                while (queue.isNotEmpty()) {
                    val index = queue.removeFirst(); val x = index % width; val y = index / width
                    pixels += index; top = minOf(top,y); bottom = maxOf(bottom,y)
                    for (dy in -1..1) for (dx in -1..1) {
                        val nx = x + dx; val ny = y + dy
                        if (nx !in 0 until width || ny !in 0 until height) continue
                        val next = ny * width + nx
                        if (!seen[next] && bitmap.getPixel(nx,ny) == Color.BLACK) {
                            seen[next] = true; queue.add(next)
                        }
                    }
                }
                components += pixels to (bottom - top + 1)
            }
            val tallest = components.maxOfOrNull { it.second } ?: return
            val largest = components.maxOf { it.first.size }
            for ((pixels, componentHeight) in components)
                if (componentHeight < tallest * .6f && pixels.size < largest * .15f)
                    pixels.forEach { bitmap.setPixel(it % width,it / width,Color.WHITE) }
        }

        /** Repeat the same captured short-title pixels on one baseline. Native
         * OCR may reject an isolated two-glyph crop as an icon but accepts the
         * repeated optical evidence as text. No glyph or expected name is drawn. */
        fun repeatedShortAlliedTitleAlternative(atlas: DraftNameAtlas, tile: Tile, neutralOnly: Boolean = false,
            binary: Boolean = true): DraftNameAtlas? {
            val short = shortAlliedTitleAlternatives(atlas, tile, binary=binary, neutralOnly=neutralOnly) ?: return null
            try {
                // Preserve the established binary request, but offer original
                // antialiased pixels at ordinary text size independently. A
                // thresholded two-letter glyph can lose its diagonal strokes.
                val sampleTile = short.tiles.single { it.region.height() == if (binary) 72 else 32 }
                val repeats = 3
                val gap = 24
                val width = sampleTile.region.width() * repeats + gap * (repeats - 1)
                val height = sampleTile.region.height()
                val bitmap = Bitmap.createBitmap(width + GUTTER * 2, height + GUTTER * 2,
                    Bitmap.Config.ARGB_8888)
                try {
                    val canvas = Canvas(bitmap).apply { drawColor(Color.WHITE) }
                    for (index in 0 until repeats) {
                        val left = GUTTER + index * (sampleTile.region.width() + gap)
                        canvas.drawBitmap(short.bitmap, sampleTile.region,
                            Rect(left, GUTTER, left + sampleTile.region.width(), GUTTER + height), null)
                    }
                    return DraftNameAtlas(bitmap, listOf(sampleTile.copy(
                        region = Rect(GUTTER, GUTTER, GUTTER + width, GUTTER + height),
                        titleMinY = 0, titleMaxY = height)))
                } catch (error: Throwable) {
                    bitmap.recycle()
                    throw error
                }
            } finally { short.bitmap.recycle() }
        }

        /** Native OCR expects ordinary text sizes, not enlarged title logos.
         * Resample the same measured line at three character heights, retaining
         * all variants, source rectangles and title-only acceptance bounds. */
        fun nativeTextSizes(atlas: DraftNameAtlas, measuredTextHeight: Int): DraftNameAtlas {
            require(measuredTextHeight>0 && atlas.tiles.isNotEmpty())
            val entries=listOf(24,36,48).flatMap { target -> atlas.tiles.map { tile ->
                tile to (target.toFloat()/measuredTextHeight).coerceIn(.10f,4f)
            } }
            fun size(value:Int,factor:Float)=kotlin.math.round(value*factor).toInt().coerceAtLeast(1)
            val cellWidth=entries.maxOf { (tile,factor) -> size(tile.region.width(),factor) }+GUTTER*2
            val cellHeight=entries.maxOf { (tile,factor) -> size(tile.region.height(),factor) }+GUTTER*2
            // Keep each sample on its own row: native line grouping must not
            // concatenate neighbouring variants into a different full title.
            val columns=1
            val bitmap=Bitmap.createBitmap(cellWidth*columns,
                ((entries.size+columns-1)/columns)*cellHeight,Bitmap.Config.ARGB_8888)
            try {
                val canvas=Canvas(bitmap).apply { drawColor(Color.BLACK) }
                val tiles=entries.mapIndexed { index,(tile,factor) ->
                    val left=(index%columns)*cellWidth+GUTTER
                    val top=(index/columns)*cellHeight+GUTTER
                    val region=Rect(left,top,left+size(tile.region.width(),factor),top+size(tile.region.height(),factor))
                    canvas.drawBitmap(atlas.bitmap,tile.region,region,Paint(Paint.FILTER_BITMAP_FLAG))
                    tile.copy(region=region,
                        titleMinY=kotlin.math.round(tile.titleMinY*factor).toInt().coerceIn(0,region.height()),
                        titleMaxY=kotlin.math.round(tile.titleMaxY*factor).toInt().coerceIn(0,region.height()))
                }
                return DraftNameAtlas(bitmap,tiles)
            } catch(error:Throwable) { bitmap.recycle();throw error }
        }

        /** Project white/gold title ink away from blue scenery, then select a
         * threshold from this crop's histogram. No letter templates or identities;
         * black exclusions stay background even next to bright game effects. */
        internal fun isolateTitleColor(crop: Bitmap) {
            val pixels=IntArray(crop.width*crop.height)
            crop.getPixels(pixels,0,crop.width,0,0,crop.width,crop.height)
            val ink=IntArray(pixels.size) { i ->
                val c=pixels[i];val r=Color.red(c);val g=Color.green(c);val b=Color.blue(c)
                (minOf(r,g)-maxOf(0,b-maxOf(r,g))).coerceAtLeast(0)
            }
            val histogram=IntArray(256)
            ink.forEach { histogram[it]++ }
            val total=ink.size
            val sum=ink.sumOf { it.toLong() }
            var backgroundWeight=0L;var backgroundSum=0L;var threshold=0;var best=-1.0
            for(value in 0..254) {
                backgroundWeight+=histogram[value]
                if(backgroundWeight==0L) continue
                val foregroundWeight=total-backgroundWeight
                if(foregroundWeight==0L) break
                backgroundSum+=value.toLong()*histogram[value]
                val distance=backgroundSum.toDouble()/backgroundWeight-
                    (sum-backgroundSum).toDouble()/foregroundWeight
                val variance=backgroundWeight.toDouble()*foregroundWeight*distance*distance
                if(variance>best) { best=variance;threshold=value }
            }
            for(i in pixels.indices) pixels[i]=if(best>=0 && ink[i]>threshold && ink[i]>=45) Color.BLACK else Color.WHITE
            crop.setPixels(pixels,0,crop.width,0,0,crop.width,crop.height)
        }

        /** A separate compact request for shifted empty allied rows. Its pixels
         * can only supply lanes, never champion or summoner identities. */
        fun createAllyLanes(source: Bitmap, config: VisionCalibrationConfig,
                            masks: List<Rect>, slots: List<Int>): DraftNameAtlas {
            require(slots.isNotEmpty() && slots.distinct().size == slots.size && slots.all { it in 0..4 })
            val bands = slots.map { it to AdaptiveScreenLayoutEngine.calculateAllyLaneRect(
                source.width, source.height, it, config) }
            val scale = (100f / bands.minOf { it.second.height() }).coerceIn(1f, 12f)
            val cellWidth = bands.maxOf { (it.second.width() * scale).toInt() } + GUTTER * 2
            val cellHeight = bands.maxOf { (it.second.height() * scale).toInt() } + GUTTER * 2
            val columns = compactColumns(slots.size * 2, cellWidth, cellHeight)
            val rows = (slots.size * 2 + columns - 1) / columns
            val bitmap = Bitmap.createBitmap(cellWidth * columns, cellHeight * rows, Bitmap.Config.ARGB_8888)
            try {
                val canvas = Canvas(bitmap).apply { drawColor(Color.BLACK) }
                val paint = Paint().apply { color = Color.BLACK }
                val tiles = bands.flatMapIndexed { index, (slot, band) -> (0..1).map { variant ->
                    val crop = Bitmap.createBitmap(band.width(), band.height(), Bitmap.Config.ARGB_8888)
                    try {
                        val local = Canvas(crop)
                        local.drawBitmap(source, band, Rect(0, 0, crop.width, crop.height), null)
                        masks.forEach { local.drawRect(Rect(it).apply { offset(-band.left, -band.top) }, paint) }
                        if (variant == 1) DraftImagePreprocessor.suppressLeadingMasteryBadge(crop)
                        val tileIndex = index * 2 + variant
                        val left = (tileIndex % columns) * cellWidth + GUTTER
                        val top = (tileIndex / columns) * cellHeight + GUTTER
                        val region = Rect(left, top, left + (band.width() * scale).toInt(),
                            top + (band.height() * scale).toInt())
                        canvas.drawBitmap(crop, null, region, Paint(Paint.FILTER_BITMAP_FLAG))
                        Tile(true, slot, band, region, region.height(), original = variant == 0, laneOnly = true)
                    } finally { crop.recycle() }
                } }
                return DraftNameAtlas(bitmap, tiles)
            } catch (failure: Throwable) { bitmap.recycle(); throw failure }
        }

        fun create(source: Bitmap, config: VisionCalibrationConfig, masks: List<Rect>): DraftNameAtlas {
            val bands = (0..4).flatMap { slot -> listOf(true, false).map { ally ->
                Triple(ally, slot, AdaptiveScreenLayoutEngine.calculateSlotNameRect(source.width,source.height,ally,slot,config))
            } }
            // Keep small title glyphs legible to native OCR rather than making
            // recognition depend on the input resolution. Upscaling cannot invent
            // detail, so the native sequence audit still has to prove each size.
            val scale = (150f / bands.minOf { it.third.height() }).coerceIn(2f, 12f)
            val cellWidth = bands.maxOf { (it.third.width()*scale).toInt().coerceAtMost(1400) } + GUTTER*2
            val cellHeight = bands.maxOf { (it.third.height()*scale).toInt().coerceAtMost(420) } + GUTTER*2
            // Native diagnostics could read the exact VI pixels in a taller
            // retry but not in a very wide one. Minimise the longest image edge
            // so the detector does not downsample a two-letter title away.
            val columns=compactColumns(20,cellWidth,cellHeight)
            val rows=(20+columns-1)/columns
            val bitmap = Bitmap.createBitmap(cellWidth*columns,cellHeight*rows,Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.BLACK)
            val paint = Paint().apply { color=Color.BLACK }
            try {
                val tiles = bands.flatMap { (ally,slot,band) ->
                    // Gold is also used for actual game titles. Badge removal is
                    // only an alternative, never the sole source of OCR evidence.
                    (if (ally) listOf(0, 1, 2) else listOf(0)).map { variant ->
                        val original = variant == 0
                        val contrast = variant == 2
                        val crop = Bitmap.createBitmap(band.width(),band.height(),Bitmap.Config.ARGB_8888)
                        try {
                            val localCanvas = Canvas(crop)
                            localCanvas.drawBitmap(source,band,Rect(0,0,crop.width,crop.height),null)
                            masks.forEach { mask -> localCanvas.drawRect(Rect(mask).apply { offset(-band.left,-band.top) },paint) }
                            // Keep the established source-resolution alternative.
                            // Enlarged masking regressed Volibear and one known
                            // phone reference; the intact copy always remains.
                            val badgeRight=if(ally && !original) DraftImagePreprocessor.suppressLeadingMasteryBadge(crop) else 0
                            if(contrast) enhanceTitleContrast(crop)
                            val width=(crop.width*scale).toInt().coerceAtMost(1400)
                            val height=(crop.height*scale).toInt().coerceAtMost(420)
                            val index=slot*4+(if(!ally) 3 else variant)
                            val left=(index%columns)*cellWidth+GUTTER
                            val top=(index/columns)*cellHeight+GUTTER
                            val region=Rect(left,top,left+width,top+height)
                            val scaled=Bitmap.createScaledBitmap(crop,width,height,true)
                            try {
                                canvas.drawBitmap(scaled,left.toFloat(),top.toFloat(),null)
                            }
                            finally { if(scaled!==crop) scaled.recycle() }
                            val y=if(ally) config.allySlotYRatios[slot] else config.enemySlotYRatios[slot]
                            Tile(ally,slot,band,region,((source.height*(y+.005f*config.nameBandScale)-band.top)*height/band.height()).toInt(),original,contrast,
                                contentInsetLeft=(badgeRight*width/crop.width).coerceIn(0,width))
                        } finally { crop.recycle() }
                    }
                }
                return DraftNameAtlas(bitmap,tiles,source,masks.map { Rect(it) })
            } catch (error: Throwable) { bitmap.recycle(); throw error }
        }

        /** Another reading of the same pixels, never a generated champion label. */
        private fun isolateBrightTitleStrokes(crop: Bitmap) {
            val width=crop.width;val height=crop.height
            val pixels=IntArray(width*height)
            crop.getPixels(pixels,0,width,0,0,width,height)
            val light=IntArray(pixels.size) { i ->
                (Color.red(pixels[i])*299+Color.green(pixels[i])*587+Color.blue(pixels[i])*114)/1000
            }
            val stride=width+1
            val integral=LongArray(stride*(height+1))
            for(y in 0 until height) {
                var row=0L
                for(x in 0 until width) {
                    row+=light[y*width+x]
                    integral[(y+1)*stride+x+1]=integral[y*stride+x+1]+row
                }
            }
            val radius=(height*.10f).toInt().coerceAtLeast(2)
            for(y in 0 until height) for(x in 0 until width) {
                val left=(x-radius).coerceAtLeast(0);val right=(x+radius+1).coerceAtMost(width)
                val top=(y-radius).coerceAtLeast(0);val bottom=(y+radius+1).coerceAtMost(height)
                val sum=integral[bottom*stride+right]-integral[top*stride+right]-
                    integral[bottom*stride+left]+integral[top*stride+left]
                val mean=sum.toFloat()/((right-left)*(bottom-top))
                val value=light[y*width+x]
                // Local contrast removes the changing blue scenery without
                // drawing a glyph or guessing a champion from a portrait.
                pixels[y*width+x]=if(value>=80 && value>=mean+8f) Color.BLACK else Color.WHITE
            }
            crop.setPixels(pixels,0,width,0,0,width,height)
        }

        /** Another reading of the same pixels, never a generated champion label. */
        private fun enhanceTitleContrast(crop: Bitmap) {
            val pixels = IntArray(crop.width * crop.height)
            crop.getPixels(pixels, 0, crop.width, 0, 0, crop.width, crop.height)
            val luminance = pixels.map { (Color.red(it)*299 + Color.green(it)*587 + Color.blue(it)*114)/1000 }
            val sorted = luminance.sorted()
            val low = sorted[sorted.size / 10]
            val high = sorted[(sorted.size * .98f).toInt().coerceAtMost(sorted.lastIndex)]
            val range = (high - low).coerceAtLeast(30)
            for (index in pixels.indices) {
                val value = 255 - ((luminance[index]-low)*255/range).coerceIn(0,255)
                pixels[index] = Color.rgb(value,value,value)
            }
            crop.setPixels(pixels, 0, crop.width, 0, 0, crop.width, crop.height)
        }
    }
}
