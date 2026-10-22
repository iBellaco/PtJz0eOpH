package com.example.service.screen

import android.graphics.Bitmap
import android.graphics.Rect
import kotlin.math.*

/** Fits visible portrait rings, rather than inferring game margins from the phone model. */
internal object DraftRingAlignment {
    fun align(bitmap: Bitmap, base: VisionCalibrationConfig, excluded: Rect?, ignoreDebugRings: Boolean = false): VisionCalibrationConfig {
        if (bitmap.width > 1280 || bitmap.height > 720) {
            val scale = minOf(1280f / bitmap.width, 720f / bitmap.height)
            val reduced = Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).roundToInt().coerceAtLeast(1),
                (bitmap.height * scale).roundToInt().coerceAtLeast(1), true)
            try {
                val mask = excluded?.let { Rect(floor(it.left * scale).toInt(), floor(it.top * scale).toInt(),
                    ceil(it.right * scale).toInt(), ceil(it.bottom * scale).toInt()) }
                return align(reduced, base, mask, ignoreDebugRings)
            } finally { reduced.recycle() }
        }
        val w = bitmap.width; val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        fun fit(ally: Boolean): Pair<List<Float>, List<Float>>? {
            val ys = if (ally) base.allySlotYRatios else base.enemySlotYRatios
            val fits = mutableListOf<Pair<Int, Pair<Int, Int>>>()
            val radius = (h * base.avatarDiameterRatio / 2).roundToInt().coerceAtLeast(8)
            val radialRange = maxOf(3, (radius * .12f).toInt())
            val edgeDistance = maxOf(4, (radius * .16f).toInt())
            fun colorDistance(a: Int, b: Int): Int =
                abs(((a shr 16) and 255)-((b shr 16) and 255)) +
                    abs(((a shr 8) and 255)-((b shr 8) and 255)) + abs((a and 255)-(b and 255))
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
                val nominalX = w * (if(ally) base.getAllySlotX(slot) else base.getEnemySlotX(slot))
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
                            for (dr in -radialRange..radialRange) {
                                val x = (cx + dx + dx * dr / radius).coerceIn(0,w-1)
                                val y = (cy + dy + dy * dr / radius).coerceIn(0,h-1)
                                // Do not use our own diagnostic outline as evidence of a game ring.
                                if(ignoreDebugRings && abs(hypot(x-nominalX,(y-nominalY).toFloat())-radius)<3f) continue
                                val color = pixels[y*w+x]
                                val r = (color shr 16) and 255; val g = (color shr 8) and 255; val b = color and 255
                                val team = if (ally) b > 85 && g > 65 && b > r * 1.25 && g > r * 1.15
                                    else r > 100 && r > g * 1.45 && r > b * 1.15
                                if (!team) continue
                                val ox=(x+dx*edgeDistance/radius).coerceIn(0,w-1)
                                val oy=(y+dy*edgeDistance/radius).coerceIn(0,h-1)
                                val ix=(x-dx*edgeDistance/radius).coerceIn(0,w-1)
                                val iy=(y-dy*edgeDistance/radius).coerceIn(0,h-1)
                                // A colored patch is not a narrow circular rim. Both
                                // sides must differ, otherwise the blue game background wins.
                                if (colorDistance(color,pixels[oy*w+ox]) > 75 &&
                                    colorDistance(color,pixels[iy*w+ix]) > 45) { hit = true; break }
                            }
                            if (hit) score++
                        }
                        if (score > best) { best=score; center=cx to cy }
                    }
                }
                if (best >= (if (ally) 20 else 16) && center != null) fits += slot to center
            }
            if (fits.size < 2) return null
            val medianX = fits.map { it.second.first }.sorted()[fits.size / 2]
            if (fits.count { abs(it.second.first - medianX) <= radius / 3 } < 2) return null

            // Keep each detected slot's Y instead of reusing another phone's row spacing.
            // This matters when the captured frame includes a different top inset or letterbox.
            val consistent = fits.filter { abs(it.second.first - medianX) <= radius / 3 }
            fun median(values: List<Float>): Float {
                val sorted=values.sorted()
                return (sorted[(sorted.size-1)/2]+sorted[sorted.size/2])/2f
            }
            // Robust row spacing from independent visible rings. One weak rim
            // on a face must not drag its row away from the rest of the column.
            val slopes=consistent.flatMapIndexed { index,a -> consistent.drop(index+1).mapNotNull { b ->
                val nominal=(ys[b.first]-ys[a.first])*h
                if(abs(nominal)<1f) null else ((b.second.second-a.second.second)/nominal).takeIf { it in .85f..1.15f }
            } }
            val scale=if(slopes.isEmpty()) 1f else median(slopes)
            val shift=median(consistent.map { it.second.second-ys[it.first]*h*scale })
            val alignedYs = ys.map { ((it * h * scale + shift) / h).coerceIn(0f, 1f) }.toMutableList()
            fits.forEach { (slot, center) ->
                if (abs(center.first - medianX) <= radius / 3 &&
                    abs(center.second-alignedYs[slot]*h) <= maxOf(3f,radius*.12f)) {
                    alignedYs[slot] = (center.second.toFloat() / h).coerceIn(0f, 1f)
                }
            }
            return List(5) { medianX.toFloat() / w } to alignedYs
        }
        val ally = fit(true); val enemy = fit(false)
        return base.copy(
            allyAvatarCenterX = ally?.first?.first() ?: base.allyAvatarCenterX,
            allySlotXRatios = ally?.first ?: base.allySlotXRatios,
            allySlotYRatios = ally?.second ?: base.allySlotYRatios,
            enemyAvatarCenterX = enemy?.first?.first() ?: base.enemyAvatarCenterX,
            enemySlotXRatios = enemy?.first ?: base.enemySlotXRatios,
            enemySlotYRatios = enemy?.second ?: base.enemySlotYRatios
        )
    }
}
