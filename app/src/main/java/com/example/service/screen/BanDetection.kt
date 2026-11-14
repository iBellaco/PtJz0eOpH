package com.example.service.screen

/** Normalized coordinates belong to the captured frame, not the floating window. */
internal data class BanRegion(val x: Float, val y: Float, val radius: Float)
internal enum class BanSide { ALLY, ENEMY }
internal enum class BanState { UNKNOWN, PENDING, EMPTY, CONFIRMED }
internal data class BanObservation(val side: BanSide, val region: BanRegion,
    val championId: String? = null, val empty: Boolean = false, val similarity: Float = 0f)
internal data class BanSlot(val side: BanSide, val index: Int, val region: BanRegion? = null,
    val championId: String? = null, val state: BanState = BanState.UNKNOWN, val similarity: Float = 0f)

/** A nearby crop must not replace an accepted identity on a marginal score gain. */
internal fun isClearlyStrongerBan(candidateScore: Float, previousScore: Float): Boolean =
    candidateScore >= maxOf(.90f, previousScore + .05f)

internal fun emptyBanSlots(): List<BanSlot> = BanSide.entries.flatMap { side ->
    List(5) { BanSlot(side, it) }
}

/** Side HUDs scale with height; the central artwork grows on panoramic screens. */
internal fun banHeaderSideWidthRatio(aspect: Float): Float =
    (.48f / aspect.coerceAtLeast(1f)).coerceIn(.20f, .45f)

/** Theoretical five-slot row used only to assign incomplete observations. */
internal fun computeAdaptiveBanRegions(aspect: Float, side: BanSide): List<BanRegion> {
    val safeAspect = aspect.coerceAtLeast(1f)
    val step = .080f / safeAspect
    val radius = .030f
    val y = .052f
    return if (side == BanSide.ALLY) {
        val startX = .050f / safeAspect
        List(5) { index -> BanRegion((startX + index * step).coerceIn(0f, 1f), y, radius) }
    } else {
        val endX = 1f - (.050f / safeAspect)
        val startX = endX - 4 * step
        List(5) { index -> BanRegion((startX + index * step).coerceIn(0f, 1f), y, radius) }
    }
}

/** Fill visible but unreadable portraits before assigning slot numbers. Never invents a champion. */
internal fun fillHeaderPositions(observations: List<BanObservation>, aspect: Float,
    hasOuterPortrait: ((BanRegion) -> Boolean)? = null,
    hasPortrait: (BanRegion) -> Boolean): List<BanObservation> = BanSide.entries.flatMap { side ->
    val candidates = observations.filter { it.side == side }.sortedBy { it.region.x }
    val row = candidates.toMutableList()
    if (row.isNotEmpty()) {
        val nominal = .080f / aspect
        val gaps = row.zipWithNext { a, b -> b.region.x - a.region.x }
            .map { gap -> gap / kotlin.math.round(gap / nominal).toInt().coerceIn(1, 4) }
            .filter { it in nominal * .65f..nominal * 1.4f }.sorted()
        val step = gaps.getOrNull(gaps.size / 2) ?: nominal
        val radius = row.map { it.region.radius }.average().toFloat()
        // A template can align to artwork a few pixels above the true rim.
        // Extrapolate the row, not the last champion's individual crop offset.
        val rowY = row.map { it.region.y }.sorted().let { it[it.size / 2] }
        fun addAt(x: Float, y: Float, outer: Boolean = false): Boolean {
            val region = BanRegion(x, y, radius)
            val sideWidth = banHeaderSideWidthRatio(aspect)
            val min = if (side == BanSide.ALLY) 0f else 1f - sideWidth
            val max = if (side == BanSide.ALLY) sideWidth else 1f
            if (x - radius / aspect < min || x + radius / aspect > max ||
                row.any { kotlin.math.abs(it.region.x - x) < step * .45f } || !hasPortrait(region)) return false
            if (outer && hasOuterPortrait != null && !hasOuterPortrait(region)) return false
            row.add(BanObservation(side, region))
            row.sortBy { it.region.x }
            return true
        }
        for ((a, b) in row.toList().zipWithNext()) {
            var x = a.region.x + step
            while (x < b.region.x - step * .45f && row.size < 7) {
                addAt(x, (a.region.y + b.region.y) / 2)
                x += step
            }
        }
        // A false inner candidate can hide the fifth outer circle at any aspect.
        // With explicit rim evidence, complete the row before selecting five;
        // mere texture/status indicators must not extend a full panoramic row.
        val positionLimit = if (hasOuterPortrait != null || banHeaderSideWidthRatio(aspect) > .27f) 7 else 5
        fun extend(left: Boolean) {
            while (row.size < positionLimit && addAt(
                if (left) row.first().region.x - step else row.last().region.x + step,
                rowY, outer = true)) { /* visible positions only */ }
        }
        // Reserve the outward search first, before an inner status/profile icon
        // can consume the bounded candidate budget.
        extend(left = side == BanSide.ALLY)
        extend(left = side != BanSide.ALLY)
        // The classifier may reject a deliberately empty outer ring before
        // the row selector sees it. If the ring is visible but the inner-most
        // candidate sits exactly on the side-band boundary, treat that one as
        // adjacent artwork and keep the four real ban portraits.
        if (side == BanSide.ENEMY && row.size == 5 && hasOuterPortrait != null &&
            row.first().region.x < 1f - banHeaderSideWidthRatio(aspect) + step * .2f &&
            hasOuterPortrait(row.last().region.copy(x = row.last().region.x + step))) {
            row.removeAt(0)
        }
    }
    // Template similarity alone can match a textured background outside the row.
    // Select the five circles with consistent spacing before latching identities.
    if (row.size <= 5) row else {
      val windows = row.windowed(5).map { window ->
        val gaps = window.zipWithNext { a, b -> b.region.x-a.region.x }.sorted()
        val step = (gaps[1]+gaps[2])/2f
        val nominal = .080f/aspect
        val spacingError = gaps.sumOf { gap ->
            val relative = (gap-step)/nominal
            (relative*relative).toDouble()
        }
        val scaleError = if(step in nominal*.65f..nominal*1.4f) 0.0 else 10.0
        val y = window.map { it.region.y }.average()
        window to (spacingError+scaleError+window.sumOf { (it.region.y-y)*(it.region.y-y) })
      }
      val minimum = windows.minOf { it.second }
      val regular = windows.filter { it.second<=minimum+.02 }
      // With equally regular candidates, the ban row is toward the screen edge,
      // not the adjacent player/profile artwork toward the centre.
      (if(side==BanSide.ALLY) regular.first() else regular.last()).first
    }
}
