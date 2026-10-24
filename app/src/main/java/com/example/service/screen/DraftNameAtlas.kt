package com.example.service.screen

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect

/** Isolated title bands in one OCR request, including an intact copy of every allied title. */
internal class DraftNameAtlas private constructor(val bitmap: Bitmap, val tiles: List<Tile>) {
    data class Tile(val ally: Boolean, val slot: Int, val source: Rect, val region: Rect,
                    val titleMaxY: Int, val original: Boolean = true, val contrast: Boolean = false,
                    val titleMinY: Int = 0, val badgeRemoved: Boolean = false)

    companion object {
        private const val GUTTER = 32

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
                            titleMinY=(height*.30f).toInt(),titleMaxY=minOf(tile.titleMaxY,(height*.85f).toInt()))
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
                            titleMinY=((tile.region.height()*.30f).toInt()-bounds.top).coerceIn(0,height),
                            titleMaxY=(minOf(tile.titleMaxY,(tile.region.height()*.85f).toInt())-bounds.top).coerceIn(0,height))
                    } finally { crop.recycle() }
                }
                return DraftNameAtlas(bitmap,tiles)
            } catch(error:Throwable) { bitmap.recycle();throw error }
        }

        /**
         * Re-read an allied title after removing only the leading mastery badge
         * area. The badge is part of the captured pixels, but it is not the
         * champion title and ML Kit can merge its silhouette with short names
         * such as "Vi". The crop never supplies a name or a portrait; it only
         * gives OCR the same title pixels with the calibrated badge margin out
         * of its text line.
         */
        fun withoutLeadingBadge(atlas: DraftNameAtlas, tile: Tile): DraftNameAtlas {
            require(tile.original && tile in atlas.tiles && tile.ally)
            val trim = (tile.region.width() * .24f).toInt().coerceIn(1, tile.region.width() - 1)
            val width = tile.region.width() - trim
            val height = tile.region.height()
            val bitmap = Bitmap.createBitmap(width + GUTTER * 2, height + GUTTER * 2,
                Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap).apply { drawColor(Color.BLACK) }
            try {
                val crop = Bitmap.createBitmap(atlas.bitmap, tile.region.left + trim,
                    tile.region.top, width, height)
                try {
                    canvas.drawBitmap(crop, GUTTER.toFloat(), GUTTER.toFloat(), null)
                } finally { crop.recycle() }
                val source = Rect(
                    tile.source.left + trim * tile.source.width() / tile.region.width(),
                    tile.source.top,
                    tile.source.right,
                    tile.source.bottom
                )
                return DraftNameAtlas(bitmap, listOf(tile.copy(
                    source = source,
                    region = Rect(GUTTER, GUTTER, GUTTER + width, GUTTER + height),
                    titleMinY = tile.titleMinY,
                    titleMaxY = tile.titleMaxY,
                    badgeRemoved = true
                )))
            } catch (error: Throwable) {
                bitmap.recycle()
                throw error
            }
        }

        /** Native OCR expects ordinary text sizes, not enlarged title logos.
         * Resample the same measured line at two character heights, retaining
         * all variants, source rectangles and title-only acceptance bounds. */
        fun nativeTextSizes(atlas: DraftNameAtlas, measuredTextHeight: Int): DraftNameAtlas {
            require(measuredTextHeight>0 && atlas.tiles.isNotEmpty())
            val entries=listOf(48).flatMap { target -> atlas.tiles.map { tile ->
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
                            if(ally && !original) DraftImagePreprocessor.suppressLeadingMasteryBadge(crop)
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
                            Tile(ally,slot,band,region,((source.height*(y+.005f*config.nameBandScale)-band.top)*height/band.height()).toInt(),original,contrast)
                        } finally { crop.recycle() }
                    }
                }
                return DraftNameAtlas(bitmap,tiles)
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
