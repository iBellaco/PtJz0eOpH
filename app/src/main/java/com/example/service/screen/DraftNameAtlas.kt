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
            val bitmap = Bitmap.createBitmap(cellWidth*4,cellHeight*5,Bitmap.Config.ARGB_8888)
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
                            if (ally && !original) DraftImagePreprocessor.suppressLeadingMasteryBadge(crop)
                            if (contrast) enhanceTitleContrast(crop)
                            val width=(crop.width*scale).toInt().coerceAtMost(1400)
                            val height=(crop.height*scale).toInt().coerceAtMost(420)
                            val column = if (!ally) 3 else variant
                            val left=column*cellWidth+GUTTER
                            val top=slot*cellHeight+GUTTER
                            val region=Rect(left,top,left+width,top+height)
                            val scaled=Bitmap.createScaledBitmap(crop,width,height,true)
                            try { canvas.drawBitmap(scaled,left.toFloat(),top.toFloat(),null) }
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
