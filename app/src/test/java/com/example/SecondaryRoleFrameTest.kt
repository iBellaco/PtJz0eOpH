package com.example

import com.example.ui.components.secondaryRoleFrameAfterChange
import com.example.ui.components.secondaryRoleFrameId
import org.junit.Assert.assertEquals
import org.junit.Test

class SecondaryRoleFrameTest {
    @Test fun `secondary role maps to its frame`() {
        assertEquals("DIAMANTE", secondaryRoleFrameId("diamante"))
        assertEquals("MAESTRO_FRAME", secondaryRoleFrameId("maestro"))
        assertEquals("SOBERANO", secondaryRoleFrameId("soberano"))
    }

    @Test fun `changing the secondary role replaces only its active frame`() {
        assertEquals("ASPIRANTE", secondaryRoleFrameAfterChange("DIAMANTE", "diamante", "aspirante"))
        assertEquals("NONE", secondaryRoleFrameAfterChange("DIAMANTE", "diamante", ""))
        assertEquals("CREADOR", secondaryRoleFrameAfterChange("CREADOR", "diamante", "soberano"))
    }
}
