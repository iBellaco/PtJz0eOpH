package com.example.service.screen

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect

/** Isolated title bands in one OCR request, including an intact copy of every allied title. */
internal class DraftNameAtlas private constructor(val bitmap: Bitmap, val tiles: List<Tile>) {
    data class Tile(val ally: Boolean, val slot: Int, val source: Rect, val region: Rect,
                    val titleMaxY: Int, val original: Boolean = true)

    companion object {
        private const val GUTTER = 32
        private const val SCALE = 2

        fun create(source: Bitmap, config: VisionCalibrationConfig, masks: List<Rect>): DraftNameAtlas {
            val bands = (0..4).flatMap { slot -> listOf(true, false).map { ally ->
                Triple(ally, slot, AdaptiveScreenLayoutEngine.calculateSlotNameRect(source.width,source.height,ally,slot,config))
            } }
            val cellWidth = bands.maxOf { (it.third.width()*SCALE).coerceAtMost(1400) } + GUTTER*2
            val cellHeight = bands.maxOf { (it.third.height()*SCALE).coerceAtMost(420) } + GUTTER*2
            val bitmap = Bitmap.createBitmap(cellWidth*3,cellHeight*5,Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.BLACK)
            val paint = Paint().apply { color=Color.BLACK }
            try {
                val tiles = bands.flatMap { (ally,slot,band) ->
                    // Gold is also used for actual game titles. Badge removal is
                    // only an alternative, never the sole source of OCR evidence.
                    (if (ally) listOf(true, false) else listOf(true)).map { original ->
                        val crop = Bitmap.createBitmap(band.width(),band.height(),Bitmap.Config.ARGB_8888)
                        try {
                            val localCanvas = Canvas(crop)
                            localCanvas.drawBitmap(source,band,Rect(0,0,crop.width,crop.height),null)
                            masks.forEach { mask -> localCanvas.drawRect(Rect(mask).apply { offset(-band.left,-band.top) },paint) }
                            if (ally && !original) DraftImagePreprocessor.suppressLeadingMasteryBadge(crop)
                            val width=(crop.width*SCALE).coerceAtMost(1400)
                            val height=(crop.height*SCALE).coerceAtMost(420)
                            val column = if (!ally) 2 else if (original) 0 else 1
                            val left=column*cellWidth+GUTTER
                            val top=slot*cellHeight+GUTTER
                            val region=Rect(left,top,left+width,top+height)
                            val scaled=Bitmap.createScaledBitmap(crop,width,height,true)
                            try { canvas.drawBitmap(scaled,left.toFloat(),top.toFloat(),null) }
                            finally { if(scaled!==crop) scaled.recycle() }
                            val y=if(ally) config.allySlotYRatios[slot] else config.enemySlotYRatios[slot]
                            Tile(ally,slot,band,region,((source.height*(y+.005f)-band.top)*height/band.height()).toInt(),original)
                        } finally { crop.recycle() }
                    }
                }
                return DraftNameAtlas(bitmap,tiles)
            } catch (error: Throwable) { bitmap.recycle(); throw error }
        }
    }
}
