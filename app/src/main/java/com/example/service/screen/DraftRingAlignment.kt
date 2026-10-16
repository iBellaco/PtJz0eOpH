package com.example.service.screen

import android.graphics.Bitmap
import android.graphics.Rect
import kotlin.math.*

/** Fits visible portrait rings, rather than inferring game margins from the phone model. */
internal object DraftRingAlignment {
    fun align(bitmap: Bitmap, base: VisionCalibrationConfig, excluded: Rect?): VisionCalibrationConfig {
        val w = bitmap.width; val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        fun fit(ally: Boolean): Pair<List<Float>, List<Float>>? {
            val ys = if (ally) base.allySlotYRatios else base.enemySlotYRatios
            val fits = mutableListOf<Pair<Int, Int>>()
            val radius = (h * base.avatarDiameterRatio / 2).roundToInt().coerceAtLeast(8)
            val samples = List(40) { angle ->
                val a = angle * 2 * Math.PI / 40
                (cos(a) * radius).roundToInt() to (sin(a) * radius).roundToInt()
            }
            val interiorOffsets=listOf(-.5f to 0f,.5f to 0f,0f to -.5f,0f to .5f,
                0f to 0f,-.35f to -.35f,.35f to .35f).map { (dx,dy) ->
                (radius*dx).toInt() to (radius*dy).toInt()
            }
            for (slot in 0..4) {
                val nominalY = (ys[slot] * h).toInt()
                var best = 0; var center: Pair<Int, Int>? = null
                val minX = (w * if (ally) .035 else .76).toInt()
                val maxX = (w * if (ally) .24 else .975).toInt()
                for (cy in max(radius, nominalY - h / 25)..min(h - radius - 1, nominalY + h / 25) step 3) {
                    for (cx in minX..maxX step 3) {
                        if (excluded?.intersects(cx-radius, cy-radius, cx+radius, cy+radius) == true) continue
                        // A debug outline around empty background is not a portrait ring.
                        var low=255; var high=0
                        for ((dx,dy) in interiorOffsets) {
                            val color=pixels[(cy+dy).coerceIn(0,h-1)*w+(cx+dx).coerceIn(0,w-1)]
                            val brightness=(((color shr 16) and 255)+((color shr 8) and 255)+(color and 255))/3
                            low=min(low,brightness); high=max(high,brightness)
                        }
                        if(high-low<55) continue
                        var score = 0
                        for ((dx, dy) in samples) {
                            var hit = false
                            for (dr in -2..2) {
                                val x = (cx + dx + dx * dr / radius).coerceIn(0,w-1)
                                val y = (cy + dy + dy * dr / radius).coerceIn(0,h-1)
                                val color = pixels[y*w+x]
                                val r = (color shr 16) and 255; val g = (color shr 8) and 255; val b = color and 255
                                if (if (ally) b > 85 && g > 65 && b > r * 1.25 && g > r * 1.15
                                    else r > 100 && r > g * 1.45 && r > b * 1.15) { hit = true; break }
                            }
                            if (hit) score++
                        }
                        if (score > best) { best=score; center=cx to cy }
                    }
                }
                if (best >= 16 && center != null) fits += slot to center.first
            }
            if (fits.size < 2) return null
            val median = fits.map { it.second }.sorted()[fits.size/2]
            if (fits.count { abs(it.second-median) <= radius/3 } < 2) return null
            return List(5) { median.toFloat()/w } to ys
        }
        val ally = fit(true); val enemy = fit(false)
        return base.copy(
            allyAvatarCenterX = ally?.first?.first() ?: base.allyAvatarCenterX,
            allySlotXRatios = ally?.first ?: base.allySlotXRatios,
            enemyAvatarCenterX = enemy?.first?.first() ?: base.enemyAvatarCenterX,
            enemySlotXRatios = enemy?.first ?: base.enemySlotXRatios
        )
    }
}
