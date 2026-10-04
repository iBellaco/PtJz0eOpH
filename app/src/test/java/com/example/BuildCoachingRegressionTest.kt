package com.example

import com.example.data.WildRiftSpellsAndRunes
import com.example.data.local.CustomChampionBuildRecord
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class BuildCoachingRegressionTest {
    private fun builds() = Json { ignoreUnknownKeys = true }.decodeFromString<List<CustomChampionBuildRecord>>(
        File("src/main/assets/champions_creator_builds.json").readText())

    @Test fun `all champions have independent plans instead of their first item note`() {
        val all=builds()
        assertEquals(142,all.map { it.championId }.distinct().size)
        assertEquals(300,all.map { it.coachAdvice }.distinct().size)
        for (build in all) {
            assertTrue(build.coachAdvice.contains(build.championName))
            assertNotEquals(build.coreItemsWithDesc.first().description,build.coachAdvice)
            assertFalse(build.coachAdvice.contains("Tu siguiente compra debe potenciar"))
            assertFalse(build.coachAdvice.contains("Completa esta compra cuando"))
            assertFalse(build.creatorName.contains(" IA"))
        }
    }

    @Test fun `flash notes are absent and runes contain decisions rather than duplicate mechanics`() {
        val runes=WildRiftSpellsAndRunes.runes.associateBy { it.name }
        for (build in builds()) {
            for (spell in build.coreSpells+build.situationalSpells) {
                if (spell.spellName=="Destello") assertTrue(spell.description.isEmpty())
                else assertTrue(spell.description.contains(build.championName))
            }
            for (rune in build.coreRunes+build.situationalRunes) {
                assertNotEquals(runes[rune.runeName]?.description,rune.description)
                assertTrue(rune.description.contains(build.championName))
                assertTrue(rune.description.contains("Decisión Soberano"))
            }
        }
    }

    @Test fun `syndra and jhin have different resource constraints and actionable item notes`() {
        val all=builds()
        val syndra=all.first { it.championId=="syndra" }
        val jhin=all.first { it.championId=="jhin" }
        assertTrue(syndra.coachAdvice.contains("H3"))
        assertTrue(syndra.coachAdvice.contains("esferas"))
        assertFalse(syndra.coreRunes.any { it.runeName=="Impacto Repentino" })
        assertTrue(syndra.coreRunes.any { it.runeName=="Golpe Bajo" })
        assertTrue(jhin.coachAdvice.contains("cuarto disparo"))
        assertTrue(jhin.coachAdvice.contains("recarga"))
        val luden=syndra.coreItemsWithDesc.first { it.itemName=="Eco de Luden" }.description
        assertTrue(luden.contains("H3"))
        assertFalse(luden.contains("+100 Poder de habilidad"))
    }

    @Test fun `every generated plan and element has its portuguese version without spanish headings`() {
        val translations=Json.decodeFromString<Map<String,String>>(File("src/main/assets/translations_pt.json").readText())
        for (build in builds()) {
            val advice=listOf(build.coachAdvice)+(build.coreItemsWithDesc+build.situationalItemsWithDesc).map { it.description }+
                (build.coreRunes+build.situationalRunes).map { it.description }+
                (build.coreSpells+build.situationalSpells).map { it.description }
            for (text in advice.filter { it.isNotBlank() }) {
                val pt=translations[text].orEmpty()
                assertTrue("Missing Portuguese: ${build.id}",pt.contains("Decisão Soberano"))
                assertFalse(pt.contains("Diagnóstico del error/situación"))
                assertFalse(pt.contains("Regla aplicable"))
            }
        }
    }
}
