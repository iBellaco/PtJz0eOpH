package com.example.service.screen

/** Confirming the last portrait must not starve role and previous-slot reconciliation. */
object DraftSyncCadence {
    fun globalCycle(confirmedCount: Int, hasActiveTurns: Boolean, tenthActive: Boolean, cycle: Long): Boolean = when {
        !hasActiveTurns -> true
        confirmedCount >= 9 || tenthActive -> cycle % 12L == 0L
        else -> cycle % 4L == 0L
    }
}
