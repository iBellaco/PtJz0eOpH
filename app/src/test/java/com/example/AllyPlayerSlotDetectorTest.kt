package com.example

import android.graphics.Color
import com.example.service.screen.AllyPlayerSlotDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AllyPlayerSlotDetectorTest {
    @Test fun warmHighlightIsDistinctFromNeutralAndTeamColors() {
        assertTrue(AllyPlayerSlotDetector.isWarmHighlight(Color.rgb(238, 190, 62)))
        assertFalse(AllyPlayerSlotDetector.isWarmHighlight(Color.rgb(240, 240, 240)))
        assertFalse(AllyPlayerSlotDetector.isWarmHighlight(Color.rgb(30, 190, 220)))
    }

    @Test fun choosesOnlyAConfidentUniqueHighlightedRow() {
        assertEquals(3, AllyPlayerSlotDetector.choose(mapOf(0 to .021f, 3 to .087f, 4 to .011f)))
        assertNull(AllyPlayerSlotDetector.choose(mapOf(1 to .081f, 3 to .079f)))
    }
}
