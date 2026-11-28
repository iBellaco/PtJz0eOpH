package com.example.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SeasonalEffectsConfigTest {
    @Test
    fun `scheduled effects respect each device local date including year boundaries`() {
        val config = SeasonalEffectsConfig(
            christmas = SeasonalEventSettings(
                enabled = true,
                scheduled = true,
                start = SeasonalDate(12, 24),
                end = SeasonalDate(12, 26)
            ),
            newYear = SeasonalEventSettings(
                enabled = true,
                scheduled = true,
                start = SeasonalDate(12, 31),
                end = SeasonalDate(1, 2)
            )
        )

        assertTrue(SeasonalEffect.CHRISTMAS in config.activeEffectsAt(SeasonalDate(12, 25)))
        assertFalse(SeasonalEffect.CHRISTMAS in config.activeEffectsAt(SeasonalDate(12, 27)))
        assertTrue(SeasonalEffect.NEW_YEAR in config.activeEffectsAt(SeasonalDate(12, 31)))
        assertTrue(SeasonalEffect.NEW_YEAR in config.activeEffectsAt(SeasonalDate(1, 1)))
        assertFalse(SeasonalEffect.NEW_YEAR in config.activeEffectsAt(SeasonalDate(1, 3)))
    }

    @Test
    fun `manual enable is available when scheduling is disabled`() {
        val alwaysOn = SeasonalEventSettings(
            enabled = true,
            scheduled = false,
            start = SeasonalDate(12, 20),
            end = SeasonalDate(12, 26)
        )
        assertTrue(alwaysOn.isActiveOn(SeasonalDate(4, 1)))
        assertEquals(SeasonalDate(1, 2), SeasonalDate.parse("01-02"))
        assertEquals(null, SeasonalDate.parse("13-42"))
    }
}
