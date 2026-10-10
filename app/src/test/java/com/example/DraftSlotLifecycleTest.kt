package com.example

import com.example.service.screen.DraftSlotLifecycle
import com.example.service.screen.DraftSlotLifecycle.Observation.*
import org.junit.Assert.assertEquals
import org.junit.Test

class DraftSlotLifecycleTest {
    @Test fun lastSelectionStaysActiveUntilTheSlotsDisappear() {
        val lifecycle = DraftSlotLifecycle()
        assertEquals(ACTIVE, lifecycle.observe(false, false))
        assertEquals(ACTIVE, lifecycle.observe(true, true))
        repeat(10) { assertEquals(ACTIVE, lifecycle.observe(true, true)) }
        assertEquals(DISAPPEARING, lifecycle.observe(false, true))
        assertEquals(FINISHED, lifecycle.observe(false, true))
    }

    @Test fun singleMissingFrameDoesNotFinishAndNewDraftResetsTheTracker() {
        val lifecycle = DraftSlotLifecycle()
        lifecycle.observe(true, true)
        assertEquals(DISAPPEARING, lifecycle.observe(false, true))
        assertEquals(ACTIVE, lifecycle.observe(true, true))
        assertEquals(DISAPPEARING, lifecycle.observe(false, true))
        lifecycle.reset()
        assertEquals(ACTIVE, lifecycle.observe(false, false))
    }
}
