package com.example

import com.example.service.screen.AllyPlayerSlotDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AllyPlayerSlotDetectorTest {
    @Test fun warmHighlightIsDistinctFromNeutralAndTeamColors() {
        assertTrue(AllyPlayerSlotDetector.isWarmHighlight(0xFFEEBE3E.toInt()))
        assertFalse(AllyPlayerSlotDetector.isWarmHighlight(0xFFF0F0F0.toInt()))
        assertFalse(AllyPlayerSlotDetector.isWarmHighlight(0xFF1EBEDC.toInt()))
    }

    @Test fun choosesOnlyAConfidentUniqueHighlightedRow() {
        assertEquals(3, AllyPlayerSlotDetector.choose(mapOf(0 to .021f, 3 to .087f, 4 to .011f)))
        assertNull(AllyPlayerSlotDetector.choose(mapOf(1 to .081f, 3 to .079f)))
    }
}
