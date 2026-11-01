package com.example.service.screen

/** Fast title frames run between metadata scans; processing time does not force another heavy scan. */
object DraftSyncCadence {
    fun metadataDue(nowMs: Long, lastFinishedMs: Long, confirmedPicks: Int = 0): Boolean =
        confirmedPicks < 8 && nowMs-lastFinishedMs>=6000L
}
