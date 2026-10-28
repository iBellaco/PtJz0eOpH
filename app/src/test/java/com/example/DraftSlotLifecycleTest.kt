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
        assertEquals(DISAPPEARING, lifecycle.observe(false, true, 100))
        assertEquals(DISAPPEARING, lifecycle.observe(false, true, 150))
        assertEquals(DISAPPEARING, lifecycle.observe(false, true, 200))
        assertEquals(FINISHED, lifecycle.observe(false, true, 900))
    }

    @Test fun lateNinthConfirmationRequiresSustainedAbsence() {
        val lifecycle = DraftSlotLifecycle()
        assertEquals(ACTIVE, lifecycle.observe(true, false))
        assertEquals(DISAPPEARING, lifecycle.observe(false, true, 100))
        assertEquals(DISAPPEARING, lifecycle.observe(false, true, 1000))
        assertEquals(FINISHED, lifecycle.observe(false, true, 1100))
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
