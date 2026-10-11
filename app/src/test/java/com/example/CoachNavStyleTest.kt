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

    @Test fun removedNotchedStylesRecoverTheOriginalBar() {
        assertEquals(CoachNavStyle.ORIGINAL, CoachNavStyle.fromId("curved_fab"))
        assertEquals(CoachNavStyle.ORIGINAL, CoachNavStyle.fromId("curved_scoop"))
    }

    @Test fun unknownPersistedStyleFallsBackToTheStableDefault() {
        assertEquals(CoachNavStyle.ORIGINAL, CoachNavStyle.fromId("missing"))
        assertEquals(CoachNavStyle.ORIGINAL, CoachNavStyle.fromId(null))
    }
}
