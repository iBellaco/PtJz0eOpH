package com.example.service.screen

import android.graphics.Bitmap
import android.graphics.Rect
import kotlin.math.*

/** Fits visible portrait rings, rather than inferring game margins from the phone model. */
internal object DraftRingAlignment {
    /** Preserve an established grid; a clearly foreign horizontal preset still needs initial recovery. */
    internal fun alignRows(ys: List<Float>, height: Int, radius: Int, observations: List<Pair<Int, Int>>,
                           recoverForeignColumn: Boolean = false): List<Float> {
        if (observations.size < 2) return ys
        fun median(values: List<Float>): Float {
            val sorted = values.sorted()
            return (sorted[(sorted.size-1)/2] + sorted[sorted.size/2]) / 2f
        }
        val offsets = observations.map { (slot, y) -> y - ys[slot] * height }
        if (!recoverForeignColumn && observations.size < 3 && offsets.max() - offsets.min() > maxOf(3f, radius * .20f)) return ys
        val slopes = if (observations.size < 3 && !recoverForeignColumn) emptyList() else observations.flatMapIndexed { index, a ->
            observations.drop(index+1).mapNotNull { b ->
                val nominal = (ys[b.first] - ys[a.first]) * height
                if (abs(nominal) < 1f) null else ((b.second-a.second)/nominal).takeIf { it in .85f..1.15f }
            }
        }
        val scale = if (slopes.isEmpty()) 1f else median(slopes)
        val shift = median(observations.map { (slot,y) -> y - ys[slot]*height*scale })
        // Slot spacing belongs to the grid, not to the artwork in a single portrait.
        // Snapping individual rows back to a noisy rim undoes this robust fit.
        return ys.map { ((it*height*scale+shift)/height).coerceIn(0f,1f) }
    }

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
            val fits = mutableListOf<Triple<Int, Pair<Int, Int>, Int>>()
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
                val minX = (w * if (ally) .035 else .76).toInt()
                val maxX = (w * if (ally) .24 else .975).toInt()
                val minY = max(radius, nominalY - h / 25)
                val maxY = min(h - radius - 1, nominalY + h / 25)
                val minimumHits = if (ally) 20 else 16
                fun evidence(cx: Int, cy: Int): Int {
                        if (excluded?.intersects(cx-radius, cy-radius, cx+radius, cy+radius) == true) return 0
                        // A debug outline around empty background is not a portrait ring.
                        var low=255; var high=0
                        for ((dx,dy) in interiorOffsets) {
                            val color=pixels[(cy+dy).coerceIn(0,h-1)*w+(cx+dx).coerceIn(0,w-1)]
                            val brightness=(((color shr 16) and 255)+((color shr 8) and 255)+(color and 255))/3
                            low=min(low,brightness); high=max(high,brightness)
                        }
                        if(high-low<55) return 0
                        var hits = 0
                        var quality = 0
                        for ((dx, dy) in samples) {
                            var bestRadialQuality = 0
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
                                    colorDistance(color,pixels[iy*w+ix]) > 45) {
                                    // A shifted circle can touch the same rim only at the
                                    // ends of the radial window. Prefer a centered rim,
                                    // not whichever portrait first reaches the hit count.
                                    bestRadialQuality = max(bestRadialQuality, radialRange + 1 - abs(dr))
                                }
                            }
                            if (bestRadialQuality > 0) hits++
                            quality += bestRadialQuality
                        }
                        // Keep coverage as a tie-breaker and as evidence for grid recovery.
                        return if (hits >= minimumHits) quality * 64 + hits else 0
                }
                var best = 0
                var center: Pair<Int, Int>? = null
                fun consider(cx: Int, cy: Int) {
                    val score = evidence(cx, cy)
                    val previous = center
                    val distance = hypot(cx-nominalX, (cy-nominalY).toFloat())
                    val previousDistance = previous?.let { hypot(it.first-nominalX, (it.second-nominalY).toFloat()) }
                    if (score > best || score > 0 && score == best &&
                        (previousDistance == null || distance < previousDistance)) {
                        best = score
                        center = cx to cy
                    }
                }
                for (cy in minY..maxY step 3) for (cx in minX..maxX step 3) consider(cx, cy)
                // Resolve the coarse 3px grid at capture resolution. This matters
                // when a letterboxed foldable reduces a portrait to a few dozen pixels.
                center?.let { coarse ->
                    for (cy in max(minY, coarse.second-3)..min(maxY, coarse.second+3))
                        for (cx in max(minX, coarse.first-3)..min(maxX, coarse.first+3)) consider(cx, cy)
                }
                center?.let { if (best > 0) fits += Triple(slot, it, best % 64) }
            }
            if (fits.size < 2) return null
            val medianX = fits.map { it.second.first }.sorted()[fits.size / 2]
            if (fits.count { abs(it.second.first - medianX) <= radius / 3 } < 2) return null

            // Keep each detected slot's Y instead of reusing another phone's row spacing.
            // This matters when the captured frame includes a different top inset or letterbox.
            val consistent = fits.filter { abs(it.second.first - medianX) <= radius / 3 }
            // Robust row spacing from independent visible rings. One weak rim
            // on a face must not drag its row away from the rest of the column.
            // A preset from another layout can have both a different X and row spacing.
            // Once X agrees with the observed rings, sparse evidence must not stretch Y.
            val foreignColumn = consistent.count { (slot, center, coverage) ->
                val nominalX = w * if (ally) base.getAllySlotX(slot) else base.getEnemySlotX(slot)
                val displacement = abs(center.first - nominalX)
                // Sparse partial rims must not stretch a nearly correct grid. A
                // displacement larger than 1.25 radii, however, is unmistakably
                // another layout's preset and still needs its initial row recovery.
                displacement > radius * 1.25f || coverage >= 28 && displacement > radius * .5f
            } >= 2
            val alignedYs = alignRows(ys, h, radius, consistent.map { it.first to it.second.second }, foreignColumn)
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
