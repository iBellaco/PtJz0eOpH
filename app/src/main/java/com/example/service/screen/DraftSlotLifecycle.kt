package com.example.service.screen

/** Freeze the last selection while the draft disappears, then stop capture processing. */
class DraftSlotLifecycle {
    enum class Observation { ACTIVE, DISAPPEARING, FINISHED }
    private var observedFinalSelection = false
    private var missingFrames = 0

    fun observe(slotsVisible: Boolean, previousPicksReady: Boolean): Observation {
        observedFinalSelection = observedFinalSelection || previousPicksReady
        if (slotsVisible) {
            missingFrames = 0
            return Observation.ACTIVE
        }
        if (!observedFinalSelection) return Observation.ACTIVE
        missingFrames++
        return if (missingFrames >= 2) Observation.FINISHED else Observation.DISAPPEARING
    }

    fun reset() {
        observedFinalSelection = false
        missingFrames = 0
    }
}
