package com.example.service.screen

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect

/** Isolated title bands in one OCR request, including an intact copy of every allied title. */
internal class DraftNameAtlas private constructor(val bitmap: Bitmap, val tiles: List<Tile>) {
    data class Tile(val ally: Boolean, val slot: Int, val source: Rect, val region: Rect,
                    val titleMaxY: Int, val original: Boolean = true, val contrast: Boolean = false)

    companion object {
        private const val GUTTER = 32

        private fun compactColumns(tiles:Int,cellWidth:Int,cellHeight:Int):Int =
            (1..minOf(4,tiles)).minBy { columns ->
                maxOf(columns*cellWidth,((tiles+columns-1)/columns)*cellHeight)
            }

        /** Re-read only unresolved titles at full tile size, avoiding a wide atlas
         * shrinking a two-letter name inside the native text detector. No new text pixels. */
        fun retryUnresolved(atlas: DraftNameAtlas, unresolved: List<Tile>, titleOnly: Boolean = false): DraftNameAtlas {
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
                        // The lower edge can contain the beginning of a very large
                        // summoner nickname. A last, isolated reading excludes that
                        // edge, without generating glyphs or guessing a short name.
                        val topCut=if(titleOnly) (prior.region.height()*.12f).toInt() else 0
                        val bottom=if(titleOnly) (prior.region.height()*.80f).toInt() else prior.region.height()
                        val sampled = Bitmap.createBitmap(atlas.bitmap,prior.region.left,prior.region.top+topCut,
                            prior.region.width(),bottom-topCut)
                        val crop = try { sampled.copy(Bitmap.Config.ARGB_8888,true) }
                            finally { if(sampled!==atlas.bitmap) sampled.recycle() }
                        try {
                            if (contrast && !tile.ally) enhanceTitleContrast(crop)
                            if (binary) isolateBrightTitleStrokes(crop)
                            val index=row*3+variant
                            val left = (index%columns)*cellWidth+GUTTER
                            val top = (index/columns)*cellHeight+GUTTER
                            canvas.drawBitmap(crop,left.toFloat(),top.toFloat(),null)
                            tile.copy(region=Rect(left,top,left+crop.width,top+crop.height),titleMaxY=tile.titleMaxY-topCut,
                                original=variant==0,contrast=variant!=0)
                        } finally { crop.recycle() }
                    }
                }
                return DraftNameAtlas(bitmap,tiles)
            } catch (error: Throwable) { bitmap.recycle(); throw error }
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
                            val width=(crop.width*scale).toInt().coerceAtMost(1400)
                            val height=(crop.height*scale).toInt().coerceAtMost(420)
                            val index=slot*4+(if(!ally) 3 else variant)
                            val left=(index%columns)*cellWidth+GUTTER
                            val top=(index/columns)*cellHeight+GUTTER
                            val region=Rect(left,top,left+width,top+height)
                            val scaled=Bitmap.createScaledBitmap(crop,width,height,true)
                            try {
                                // At small capture sizes a one-pixel badge padding
                                // can erase the neighbouring I. Mask after resizing.
                                if(ally && !original) DraftImagePreprocessor.suppressLeadingMasteryBadge(scaled)
                                if(contrast) enhanceTitleContrast(scaled)
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
