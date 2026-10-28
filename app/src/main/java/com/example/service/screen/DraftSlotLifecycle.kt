package com.example.service.screen

/** Freeze the last selection while the draft disappears, then stop capture processing. */
class DraftSlotLifecycle {
    enum class Observation { ACTIVE, DISAPPEARING, FINISHED }
    private var observedFinalSelection = false
    private var missingFrames = 0
    private var missingSince: Long? = null

    fun observe(slotsVisible: Boolean, previousPicksReady: Boolean,
        nowMillis: Long = System.nanoTime() / 1_000_000): Observation {
        observedFinalSelection = observedFinalSelection || previousPicksReady
        if (slotsVisible) {
            missingFrames = 0
            missingSince = null
            return Observation.ACTIVE
        }
        if (!observedFinalSelection) return Observation.ACTIVE
        missingFrames++
        val since = missingSince ?: nowMillis.also { missingSince = it }
        return if (missingFrames >= 3 && nowMillis - since >= 800L) Observation.FINISHED else Observation.DISAPPEARING
    }

    fun reset() {
        observedFinalSelection = false
        missingFrames = 0
        missingSince = null
    }
}
