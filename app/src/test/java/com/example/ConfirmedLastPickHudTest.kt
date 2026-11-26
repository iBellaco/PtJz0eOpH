package com.example

import com.example.model.Champion
import com.example.model.LaneRole
import com.example.service.OverlayState
import com.example.service.screen.DraftScanResult
import com.example.service.syncScannedAllies
import org.junit.Assert.assertEquals
import org.junit.Test

class ConfirmedLastPickHudTest {
    @Test fun slotLocalLaneEvidenceOverridesAStaleRoleMap() {
        val jhin = Champion(id = "jhin", name = "Jhin")
        val nocturne = Champion(id = "nocturne", name = "Nocturne")
        val state = OverlayState()
        val result = DraftScanResult(
            allies = listOf(jhin, nocturne),
            enemies = emptyList(),
            alliesBySlot = mapOf(0 to jhin, 3 to nocturne),
            // Simulate the stale map that previously put Jhin in Jungle.
            alliesByRole = mapOf(LaneRole.JUNGLE to jhin),
            allyRolesBySlot = mapOf(3 to LaneRole.JUNGLE),
            isSuccessful = true,
            statusMessage = ""
        )

        state.syncScannedAllies(result)

        assertEquals("nocturne", state.allies[1]?.id)
    }
}
