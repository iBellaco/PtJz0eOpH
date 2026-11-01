package com.example

import com.example.data.sync.PatchTierMovement
import org.junit.Assert.assertEquals
import org.junit.Test

class PatchTierMovementTest {
    @Test fun unchangedRefreshKeepsLastDirectionAndReversalChangesIt() {
        val rising = PatchTierMovement.update(mapOf("ahri" to "A"), mapOf("ahri" to "S"), emptyMap())
        assertEquals(1, rising["ahri"])
        assertEquals(rising, PatchTierMovement.update(mapOf("ahri" to "S"), mapOf("ahri" to "S"), rising))
        assertEquals(-1, PatchTierMovement.update(mapOf("ahri" to "S"), mapOf("ahri" to "A"), rising)["ahri"])
    }
    @Test fun emptyPatchStartsWithoutInventingMovementAndPartialFetchPreservesOtherChampions() {
        assertEquals(emptyMap<String, Int>(), PatchTierMovement.update(emptyMap(), mapOf("ahri" to "S"), emptyMap()))
        assertEquals(mapOf("ahri" to 1), PatchTierMovement.update(mapOf("lux" to "A"), mapOf("lux" to "A"), mapOf("ahri" to 1)))
    }
}
