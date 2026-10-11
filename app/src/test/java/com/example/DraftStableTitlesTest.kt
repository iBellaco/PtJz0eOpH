package com.example

import com.example.service.screen.SlotTitleConfirmation
import com.example.service.screen.DraftPickOrderPolicy
import org.junit.Assert.*
import org.junit.Test

class DraftStableTitlesTest {
    @Test fun everySlotNeedsRepeatedEvidenceAndDoesNotShareAnotherSlotsReading() {
        val reader = SlotTitleConfirmation()
        for (ally in listOf(true, false)) for (slot in 0..4) {
            assertFalse(reader.observe(ally, slot, "champ-$ally-$slot"))
        }
        for (ally in listOf(true, false)) for (slot in 0..4) {
            assertTrue(reader.observe(ally, slot, "champ-$ally-$slot"))
            assertFalse(reader.observe(ally, slot, null))
            assertFalse(reader.observe(ally, slot, "different"))
            assertFalse(reader.observe(ally, slot, "original"))
        }
        reader.reset()
        assertFalse(reader.observe(true, 0, "original"))
    }
    @Test fun banControlsStartAtObservedPhoneCoordinatesAndPreserveManualAdjustment() {
        val config = com.example.service.screen.VisionCalibrationConfig()
        val regions = List(5) { com.example.service.screen.BanRegion(.08f + it * .035f, .052f, .030f) }
        val baseline = config.withObservedBanRow(true, regions)
        val moved = baseline.adjustBanRow(true, deltaX = .005f)
        regions.indices.forEach { assertEquals(regions[it].x + .005f, moved.allyBanXRatios[it], .00001f) }
        assertEquals(moved, moved.withObservedBanRow(true, regions))
        assertEquals(config.enemyBanXRatios, moved.enemyBanXRatios)
        assertEquals(config.allySlotXRatios, moved.allySlotXRatios)
    }

    @Test fun tenthPickIsAlwaysOppositeFirstPick() {
        assertFalse(DraftPickOrderPolicy.firstPickFromTenthSide(true))
        assertTrue(DraftPickOrderPolicy.firstPickFromTenthSide(false))
    }
}
