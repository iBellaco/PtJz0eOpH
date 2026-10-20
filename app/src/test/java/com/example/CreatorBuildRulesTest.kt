package com.example

import com.example.data.local.CreatorBuildRules
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Test

class CreatorBuildRulesTest {
    @Test fun `core y situacionales no pueden compartir objeto`() {
        assertFalse(CreatorBuildRules.overlap(listOf("A", "B"), listOf("C")))
        assert(CreatorBuildRules.overlap(listOf("A"), listOf("a")))
    }

    @Test fun `soporte exige un starter`() {
        assertEquals(1, CreatorBuildRules.supportStarterCount(listOf("Hoz espectral", "Redención")))
        assertEquals(0, CreatorBuildRules.supportStarterCount(listOf("Redención")))
    }
}
