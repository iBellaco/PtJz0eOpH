package com.example

import com.example.ui.theme.CoachNavStyle
import org.junit.Assert.assertEquals
import org.junit.Test

class CoachNavStyleTest {
    @Test fun everyStyleRoundTripsThroughPersistedId() {
        CoachNavStyle.entries.forEach { style ->
            assertEquals(style, CoachNavStyle.fromId(style.id))
        }
    }

    @Test fun unknownPersistedStyleFallsBackToTheStableDefault() {
        assertEquals(CoachNavStyle.ARC_GLOWING, CoachNavStyle.fromId("missing"))
        assertEquals(CoachNavStyle.ARC_GLOWING, CoachNavStyle.fromId(null))
    }
}
