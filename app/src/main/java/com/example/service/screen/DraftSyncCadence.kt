package com.example.service.screen

/** Fast title frames run between metadata scans; processing time does not force another heavy scan. */
object DraftSyncCadence {
    /** A slow inference must leave CPU time for the game instead of restarting after 30 ms. */
    fun restDelayMs(targetMs: Long, processingMs: Long, constrained: Boolean = false): Long {
        val elapsed = processingMs.coerceAtLeast(0L)
        val recovery = (elapsed / if (constrained) 1 else 2).coerceAtMost(1200L)
        return maxOf(targetMs - elapsed, recovery, if (constrained) 600L else 180L)
    }
    fun metadataDue(nowMs: Long, lastFinishedMs: Long, confirmedPicks: Int = 0): Boolean =
        confirmedPicks in 3..7 && nowMs-lastFinishedMs>=6000L

    /** Keeps live-pick responsiveness while avoiding a hot capture/OCR loop when idle. */
    fun scanDelayMs(hasActiveTurns: Boolean, tenthPickActive: Boolean, metadataCycle: Boolean): Long = when {
        tenthPickActive -> 90L
        !metadataCycle && hasActiveTurns -> 100L
        else -> 220L
    }

    /**
     * Full title recovery is deliberately bounded on devices where ML Kit is
     * taking several seconds per frame. The normal title atlas is enough for
     * the live pass; recovery is still retried periodically once the draft has
     * evidence, but never on every eighth frame. Before three confirmed
     * picks, the normal title atlas remains live while recovery is less
     * frequent: the screen is still changing quickly and a full OCR pass can
     * otherwise monopolize slower devices without improving the suggestion.
     */
    fun quickTitlePass(quickReadCount: Int, confirmedPicks: Int): Boolean =
        when {
            confirmedPicks == 0 -> true
            confirmedPicks <= 2 -> quickReadCount % 24 != 0
            else -> quickReadCount % 16 != 0
        }
}
