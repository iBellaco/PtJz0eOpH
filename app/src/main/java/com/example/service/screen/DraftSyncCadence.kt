package com.example.service.screen

/** Fast title frames run between metadata scans; processing time does not force another heavy scan. */
object DraftSyncCadence {
    fun metadataDue(nowMs: Long, lastFinishedMs: Long, confirmedPicks: Int = 0): Boolean =
        confirmedPicks < 8 && nowMs-lastFinishedMs>=6000L

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
     * evidence, but never on every eighth frame. Before the first confirmed
     * pick we keep the fast path only because the screen is usually still
     * animating and a full OCR pass would waste battery without new evidence.
     */
    fun quickTitlePass(quickReadCount: Int, confirmedPicks: Int): Boolean =
        confirmedPicks == 0 || quickReadCount % 16 != 0
}
