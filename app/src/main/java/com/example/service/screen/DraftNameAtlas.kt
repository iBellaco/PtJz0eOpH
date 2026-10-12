package com.example.service.screen

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect

/** Ten isolated title bands in one OCR image; gutters prevent neighbouring players mixing. */
internal class DraftNameAtlas private constructor(val bitmap: Bitmap, val tiles: List<Tile>) {
    data class Tile(val ally: Boolean, val slot: Int, val source: Rect, val region: Rect, val titleMaxY: Int)

    companion object {
        private const val GUTTER = 32

        fun create(source: Bitmap, config: VisionCalibrationConfig, masks: List<Rect>): DraftNameAtlas {
            val bands = (0..4).flatMap { slot -> listOf(true, false).map { ally ->
                Triple(ally, slot, AdaptiveScreenLayoutEngine.calculateSlotNameRect(source.width,source.height,ally,slot,config))
            } }
            val cellWidth = bands.maxOf { (it.third.width()*3).coerceAtMost(1400) } + GUTTER*2
            val cellHeight = bands.maxOf { (it.third.height()*3).coerceAtMost(420) } + GUTTER*2
            val bitmap = Bitmap.createBitmap(cellWidth*2,cellHeight*5,Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.BLACK)
            val paint = Paint().apply { color=Color.BLACK }
            try {
                val tiles = bands.map { (ally,slot,band) ->
                    val crop = Bitmap.createBitmap(band.width(),band.height(),Bitmap.Config.ARGB_8888)
                    try {
                        val localCanvas = Canvas(crop)
                        localCanvas.drawBitmap(source,band,Rect(0,0,crop.width,crop.height),null)
                        masks.forEach { mask -> localCanvas.drawRect(Rect(mask).apply { offset(-band.left,-band.top) },paint) }
                        if (ally) DraftImagePreprocessor.suppressLeadingMasteryBadge(crop)
                        val width=(crop.width*3).coerceAtMost(1400)
                        val height=(crop.height*3).coerceAtMost(420)
                        val left=(if(ally) 0 else cellWidth)+GUTTER
                        val top=slot*cellHeight+GUTTER
                        val region=Rect(left,top,left+width,top+height)
                        val scaled=Bitmap.createScaledBitmap(crop,width,height,true)
                        try { canvas.drawBitmap(scaled,left.toFloat(),top.toFloat(),null) }
                        finally { if(scaled!==crop) scaled.recycle() }
                        val y=if(ally) config.allySlotYRatios[slot] else config.enemySlotYRatios[slot]
                        Tile(ally,slot,band,region,((source.height*(y+.005f)-band.top)*height/band.height()).toInt())
                    } finally { crop.recycle() }
                }
                return DraftNameAtlas(bitmap,tiles)
            } catch (error: Throwable) { bitmap.recycle(); throw error }
        }
    }
}
