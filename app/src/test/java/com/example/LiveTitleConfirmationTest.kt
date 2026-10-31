package com.example

import com.example.service.screen.SlotTitleConfirmation
import org.junit.Assert.*
import org.junit.Test

class LiveTitleConfirmationTest {
    @Test fun difficultTitleCanConfirmAcrossBoundedFramesWithoutCountingThemAsEvidence() {
        for (ally in listOf(true, false)) for (slot in 0..4) {
            val titles = SlotTitleConfirmation()
            assertFalse(titles.observe(ally, slot, "vi"))
            repeat(30) { assertFalse(titles.observe(ally, slot, null, exhaustive = false)) }
            assertTrue(titles.observe(ally, slot, "vi"))
        }
    }

    @Test fun quickFramesStillRequireTwoMatchingTitlesAndRespectLaneChanges() {
        val titles = SlotTitleConfirmation()
        assertFalse(titles.observe(true, 0, "senna", exhaustive = false))
        assertTrue(titles.observe(true, 0, "senna", exhaustive = false))
        assertFalse(titles.observe(false, 0, "senna", exhaustive = false))
        assertFalse(titles.observe(true, 1, "senna", exhaustive = false))
        assertFalse(titles.observe(true, 0, "lane:JUNGLE", exhaustive = false))
        assertFalse(titles.observe(true, 0, "senna", exhaustive = false))
        assertTrue(titles.observe(true, 0, "senna", exhaustive = false))
    }

    @Test fun exhaustiveMissesAndSessionResetExpirePendingEvidence() {
        val titles = SlotTitleConfirmation()
        assertFalse(titles.observe(true, 0, "vi"))
        repeat(2) {
            assertFalse(titles.observe(true, 0, null))
            repeat(5) { assertFalse(titles.observe(true, 0, null, exhaustive = false)) }
        }
        assertFalse(titles.observe(true, 0, "vi"))
        titles.reset()
        assertFalse(titles.observe(true, 0, "vi"))
    }
}
