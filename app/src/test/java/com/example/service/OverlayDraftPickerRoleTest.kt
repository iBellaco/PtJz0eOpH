package com.example.service

import com.example.model.LaneRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OverlayDraftPickerRoleTest {
    @Test
    fun `hub picker opens with the tapped draft lane selected`() {
        assertEquals(LaneRole.TOP, overlayDraftPickerDefaultRole(0))
        assertEquals(LaneRole.JUNGLE, overlayDraftPickerDefaultRole(1))
        assertEquals(LaneRole.MID, overlayDraftPickerDefaultRole(2))
        assertEquals(LaneRole.ADC, overlayDraftPickerDefaultRole(3))
        assertEquals(LaneRole.SUPPORT, overlayDraftPickerDefaultRole(4))
        assertNull(overlayDraftPickerDefaultRole(5))
    }
}
