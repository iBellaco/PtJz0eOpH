package com.example.service.screen

/** Fast title frames run between metadata scans; processing time does not force another heavy scan. */
object DraftSyncCadence {
    fun metadataDue(nowMs: Long, lastFinishedMs: Long): Boolean = nowMs-lastFinishedMs>=1500L
}
