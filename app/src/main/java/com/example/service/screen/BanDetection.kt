package com.example.service.screen

/** Normalized coordinates belong to the captured frame, not the floating window. */
internal data class BanRegion(val x: Float, val y: Float, val radius: Float)
internal enum class BanSide { ALLY, ENEMY }
internal enum class BanState { UNKNOWN, PENDING, EMPTY, CONFIRMED }
internal data class BanObservation(val side: BanSide, val region: BanRegion,
    val championId: String? = null, val empty: Boolean = false)
internal data class BanSlot(val side: BanSide, val index: Int, val region: BanRegion? = null,
    val championId: String? = null, val state: BanState = BanState.UNKNOWN)

internal fun emptyBanSlots(): List<BanSlot> = BanSide.entries.flatMap { side ->
    List(5) { BanSlot(side, it) }
}

/** Fill visible but unreadable portraits before assigning slot numbers. Never invents a champion. */
internal fun fillHeaderPositions(observations: List<BanObservation>, aspect: Float,
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
        fun addAt(x: Float, y: Float): Boolean {
            val region = BanRegion(x, y, radius)
            val min = if (side == BanSide.ALLY) 0f else .73f
            val max = if (side == BanSide.ALLY) .27f else 1f
            if (x - radius / aspect < min || x + radius / aspect > max ||
                row.any { kotlin.math.abs(it.region.x - x) < step * .45f } || !hasPortrait(region)) return false
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
        while (row.size < 5 && addAt(row.first().region.x - step, row.first().region.y)) { /* visible leading slots */ }
        while (row.size < 5 && addAt(row.last().region.x + step, row.last().region.y)) { /* visible trailing slots */ }
    }
    // Template similarity alone can match a textured background outside the row.
    // Select the five circles with consistent spacing before latching identities.
    if (row.size <= 5) row else row.windowed(5).minBy { window ->
        val gaps = window.zipWithNext { a, b -> b.region.x-a.region.x }.sorted()
        val step = (gaps[1]+gaps[2])/2f
        val nominal = .080f/aspect
        val spacingError = gaps.sumOf { gap ->
            val relative = (gap-step)/nominal
            (relative*relative).toDouble()
        }
        val scaleError = if(step in nominal*.65f..nominal*1.4f) 0.0 else 10.0
        val y = window.map { it.region.y }.average()
        spacingError+scaleError+window.sumOf { (it.region.y-y)*(it.region.y-y) }
    }
}
