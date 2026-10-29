package com.example

import com.example.data.WildRiftItemsData
import com.example.data.WildRiftSpellsAndRunes
import org.junit.Assert.*
import org.junit.Test

/** Official balance values, independent from generated descriptions and old fixture snapshots. */
class CatalogBalanceTest {
    @Test fun sixUpdatedItemsHaveCurrentValuesInBothLanguages() {
        fun item(id: String) = WildRiftItemsData.getItemById(id)!!
        val bolts = item("fiendhunter_bolts")
        for (text in listOf(bolts.passive, bolts.passivePt)) {
            assertTrue(text.contains("85%")); assertTrue(text.contains("17,5%"))
            assertFalse(text.contains("80%"))
        }
        assertEquals(3300, item("divine_sunderer").goldCost)
        val reaver = item("essence_reaver")
        assertTrue(reaver.stats.startsWith("+55 "))
        assertTrue(reaver.statsPt.startsWith("+55 "))
        for (id in listOf("whispering_headband", "diadem_of_songs")) {
            assertTrue(item(id).stats.contains("+6%"))
            assertTrue(item(id).statsPt.contains("+6%"))
            assertFalse(item(id).stats.contains("+8%"))
        }
        val razor = item("stormrazor")
        for (text in listOf(razor.passive, razor.passivePt)) {
            assertTrue(text.contains("100")); assertTrue(text.contains("35%"))
            assertFalse(text.contains("120")); assertFalse(text.contains("45%"))
        }
    }

    @Test fun runeChangesAlreadyPresentArePreservedAndSpellsAreNotInvented() {
        fun rune(id: String) = WildRiftSpellsAndRunes.runes.single { it.id == id }.description
        assertTrue(rune("electrocute").contains("60-210"))
        assertTrue(rune("aery").contains("20 - 100"))
        assertTrue(rune("grasp_undying").contains("3%"))
        assertTrue(rune("grasp_undying").contains("permanente: 8"))
        assertTrue(rune("cheap_shot").contains("5 s"))
        val spells = WildRiftSpellsAndRunes.summonerSpells.associateBy { it.id }
        assertEquals("150s", spells.getValue("flash").cooldown)
        assertEquals("110s", spells.getValue("cleanse").cooldown)
    }

    @Test fun catalogDescriptionsContainNoPatchLabels() {
        val label = Regex("(?i)\\b(?:parche|patch)\\s+\\d+\\.\\d+")
        for (item in WildRiftItemsData.list) {
            assertFalse(item.id, listOf(item.stats, item.passive, item.coachTip,
                item.statsPt, item.passivePt, item.coachTipPt).any { label.containsMatchIn(it) })
        }
        assertFalse(WildRiftSpellsAndRunes.runes.any { label.containsMatchIn(it.description) })
    }
}
