package com.example

import com.example.data.local.CustomChampionBuildRecord
import com.example.util.BuildElementAdvice
import kotlinx.serialization.json.Json
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BuildElementAdviceCoverageTest {
    @Test
    fun `rune and spell coaching never copies the catalog description in either language`() {
        for (language in listOf("es", "pt")) {
            com.example.data.WildRiftSpellsAndRunes.runes.forEach { rune ->
                val description = rune.getLocalizedDescription(language).substringBefore("\n").trim()
                val advice = BuildElementAdvice.contextualRuneAdvice(rune.name, "Vi", "Jungla", language, false)
                if (description.length > 30) assertFalse("${rune.name}: $language repeats mechanics",
                    advice.contains(description))
            }
            com.example.data.WildRiftSpellsAndRunes.summonerSpells.forEach { spell ->
                val description = com.example.util.SpellCatalogFormatting.split(
                    spell.getLocalizedDescription(language), language).description.substringBefore("\n").trim()
                val advice = BuildElementAdvice.contextualSpellAdvice(spell.name, "Vi", "Jungla", language)
                if (description.length > 30) assertFalse("${spell.name}: $language repeats mechanics",
                    advice.contains(description))
            }
        }
    }
    private fun builds() = Json { ignoreUnknownKeys = true }
        .decodeFromString<List<CustomChampionBuildRecord>>(
            File("src/main/assets/champions_creator_builds.json").readText()
        )

    @Test
    fun `every bundled core item has specific non generic timing advice`() {
        val names = builds().flatMap { it.coreItemsWithDesc }.map { it.itemName }.filter { it.isNotBlank() }.distinct()
        assertTrue(names.size >= 50)
        names.forEach { name ->
            val advice = BuildElementAdvice.contextualItemAdvice(name, "Campeón", "Línea", "es", false)
            assertTrue("$name must identify its core purchase timing", advice.contains("Cuándo completarlo:"))
            assertFalse("$name still uses old generic timing", advice.contains("Complétalo en el punto indicado de la build"))
            assertFalse("$name fell through the new generic core fallback", advice.contains("el efecto descrito del objeto sea una condición activa"))
        }
    }

    @Test
    fun `every bundled rune has an explicit activation scenario`() {
        val names = builds().flatMap { it.coreRunes + it.situationalRunes }
            .map { it.runeName }.filter { it.isNotBlank() }.distinct()
        assertTrue(names.size >= 35)
        names.forEach { name ->
            val advice = BuildElementAdvice.contextualRuneAdvice(name, "Campeón", "Línea", "es", false)
            assertTrue("$name must name its usage window", advice.contains("Cuándo usarla:"))
            assertFalse("$name fell through generic rune advice", advice.contains("Elige esta runa únicamente"))
        }
    }

    @Test
    fun `every bundled coached spell has an explicit decision and silent spells stay silent`() {
        val names = builds().flatMap { it.coreSpells + it.situationalSpells }
            .map { it.spellName }.filter { it.isNotBlank() }.distinct()
        names.forEach { name ->
            val advice = BuildElementAdvice.contextualSpellAdvice(name, "Campeón", "Línea", "es")
            if (BuildElementAdvice.isSpellWithoutCoachAdvice(name)) {
                assertTrue("Flash/Smite must not show coach advice", advice.isBlank())
            } else {
                assertTrue("$name must have contextual spell advice", advice.contains("Cuándo usar"))
                assertFalse("$name fell through generic spell advice", advice.contains("Elige este hechizo"))
            }
        }
    }

    @Test
    fun `every bundled tier two and tier three boot has dedicated advice`() {
        val names = builds().flatMap {
            listOfNotNull(
                it.bootsT2Item?.itemName,
                it.bootsT3Item?.itemName,
                it.situationalBootsT2Item?.itemName,
                it.situationalBootsT3Item?.itemName
            )
        }.filter { it.isNotBlank() }.distinct()
        assertTrue(names.size >= 14)
        names.forEach { name ->
            val advice = BuildElementAdvice.contextualBootAdvice(name, "Campeón", "Línea", "es", false)
            assertTrue("$name must have a dedicated boot decision", advice.contains("Cuándo elegirla:"))
            assertFalse("$name fell through generic boot advice", advice.contains("Elige estas botas solo"))
        }
    }

    @Test
    fun `mercury boots can never reuse gluttonous greaves advice`() {
        val mercury = BuildElementAdvice.contextualBootAdvice("Botas de mercurio", "Vi", "Jungla", "es", false)
        val gluttonous = BuildElementAdvice.contextualBootAdvice("Grebas codiciosas", "Vi", "Jungla", "es", false)
        assertTrue(mercury.contains("Botas de mercurio"))
        assertTrue(gluttonous.contains("Grebas codiciosas"))
        assertFalse(mercury.contains("Grebas codiciosas"))
        assertFalse(gluttonous.contains("Botas de mercurio"))
        assertFalse(mercury == gluttonous)
    }
}
