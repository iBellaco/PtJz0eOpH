package com.example

import com.example.model.DamageType
import com.example.model.LaneRole
import com.example.util.BuildChoiceRules
import com.example.util.BuildChoiceRules.RuneChoice
import org.junit.Assert.*
import org.junit.Test

class BuildChoiceRulesTest {
    private val core = listOf(RuneChoice("Conquistador", "Clave"), RuneChoice("Triunfo", "Precisión"),
        RuneChoice("Leyenda: Velocidad", "Precisión"), RuneChoice("Último Esfuerzo", "Precisión"),
        RuneChoice("Revestimiento de Huesos", "Valor"))

    @Test fun `alternatives explicitly separate shared branch and fourth secondary`() {
        val alternatives = BuildChoiceRules.runeAlternatives(core, listOf(
            RuneChoice("Brutal", "Precisión"), RuneChoice("Fuerzas Renovadas", "Valor"),
            RuneChoice("Capa del Nimbo", "Brujería"), RuneChoice("Electrocutar", "Clave"), core[1]))
        assertEquals(3, alternatives.size)
        assertNull(alternatives[0].secondarySlot)
        assertEquals(4, alternatives[1].secondarySlot)
        assertEquals(4, alternatives[2].secondarySlot)
    }
    @Test fun `mixed first three branches cannot produce a valid page or alternatives`() {
        val invalid = core.toMutableList().apply { this[2] = RuneChoice("Orbe Anulador", "Valor") }
        assertFalse(BuildChoiceRules.validRunePage(invalid))
        assertTrue(BuildChoiceRules.runeAlternatives(invalid, listOf(RuneChoice("Brutal", "Precisión"))).isEmpty())
        assertFalse(BuildChoiceRules.validRunePage(core.toMutableList().apply { this[4] = core[1] }))
    }
    @Test fun `boots are conditional and Mercury is not universal`() {
        val mage = BuildChoiceRules.boots("Botas de maná", DamageType.MAGIC, false, true, LaneRole.MID, "ahri")
        assertTrue(mage.isEmpty())
        val marksman = BuildChoiceRules.boots("Grebas de berserker", DamageType.PHYSICAL, false, true, LaneRole.ADC, "jinx")
        assertEquals("Botas blindadas", marksman.single().name)
        val fighter = BuildChoiceRules.boots("Botas blindadas", DamageType.PHYSICAL, true, false, LaneRole.TOP, "darius")
        assertEquals("Botas de mercurio", fighter.single().name)
        assertTrue(fighter.single().reason.isNotBlank())
        assertTrue(BuildChoiceRules.boots("Botas blindadas", DamageType.PHYSICAL, true, false, LaneRole.TOP, "olaf").isEmpty())
    }
    @Test fun `subscription limits apply to all three matchup categories`() {
        assertEquals(6, BuildChoiceRules.matchupLimit(false))
        assertEquals(12, BuildChoiceRules.matchupLimit(true))
    }
}
