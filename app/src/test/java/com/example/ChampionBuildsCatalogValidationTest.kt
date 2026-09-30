package com.example

import com.example.data.WildRiftItemsData
import com.example.data.WildRiftSpellsAndRunes
import com.example.data.local.CustomChampionBuildRecord
import com.example.model.Champion
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ChampionBuildsCatalogValidationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `validate every champion lane build one by one against the local catalog`() {
        val buildsFile = File("src/main/assets/champions_creator_builds.json")
        assertTrue("champions_creator_builds.json file must exist", buildsFile.exists())

        val builds = json.decodeFromString<List<CustomChampionBuildRecord>>(buildsFile.readText())
        val champions = listOf(
            File("src/main/res/raw/champions_part1.json"),
            File("src/main/res/raw/champions_part2.json")
        ).flatMap { file ->
            assertTrue("Champion catalog file must exist: ${file.path}", file.exists())
            json.decodeFromString<List<Champion>>(file.readText())
        }

        assertEquals("Champion catalog must contain exactly 142 champions", 142, champions.size)
        assertEquals("Official build catalog must contain exactly 300 lane builds", 300, builds.size)
        assertEquals("All 142 champions must have official builds", 142, builds.map { it.championId }.distinct().size)

        val expectedBuildKeys = champions.flatMap { champion ->
            val primary = "${champion.id.lowercase()}|${champion.primaryRole.displayName.lowercase()}"
            val flex = champion.secondaryRoles.map { role ->
                "${champion.id.lowercase()}|${role.displayName.lowercase()} (flex)"
            }
            listOf(primary) + flex
        }.toSet()

        val actualBuildKeys = builds.map { build ->
            "${build.championId.lowercase()}|${build.role.lowercase()}"
        }

        assertEquals("Every champion/lane combination must have exactly one build", builds.size, actualBuildKeys.toSet().size)
        assertEquals("Official builds must match primary + Flex roles exactly", expectedBuildKeys, actualBuildKeys.toSet())

        val validItemNames = WildRiftItemsData.list.map { it.name }.toSet()
        val validSpellNames = WildRiftSpellsAndRunes.summonerSpells.map { it.name }.toSet()
        val validRunesMap = WildRiftSpellsAndRunes.runes.associateBy { it.name }

        val bootPairs = mapOf(
            "Botas blindadas" to "Avance blindado",
            "Botas de mercurio" to "Trituradoras encadenadas",
            "Botas de maná" to "Botas del lanzahechizos",
            "Grebas de berserker" to "Grebas de metal",
            "Grebas codiciosas" to "Botas inmortales",
            "Botas jonias de la lucidez" to "Lucidez carmesí",
            "Botas dinámicas" to "Botas quebrantarmaduras"
        )

        val supportBuilds = builds.filter { it.role.contains("Soporte", ignoreCase = true) }
        assertEquals("Expected 50 support primary/Flex builds", 50, supportBuilds.size)

        for (build in builds) {
            val prefix = "Build ${build.id} (${build.championName} / ${build.role})"

            assertEquals("$prefix must have exactly 3 core items", 3, build.coreItems.size)
            assertEquals("$prefix must have 3 distinct core items", 3, build.coreItems.distinct().size)
            assertEquals("$prefix core descriptions must mirror core items", build.coreItems, build.coreItemsWithDesc.map { it.itemName })
            assertTrue("$prefix must have at least 2 optional items", build.situationalItems.size >= 2)
            assertEquals("$prefix optional items must not repeat", build.situationalItems.size, build.situationalItems.distinct().size)
            assertEquals("$prefix optional descriptions must mirror optional items", build.situationalItems, build.situationalItemsWithDesc.map { it.itemName })
            assertTrue("$prefix core and optional items cannot overlap", build.coreItems.intersect(build.situationalItems.toSet()).isEmpty())

            for (entry in build.coreItemsWithDesc + build.situationalItemsWithDesc) {
                assertTrue("$prefix item ${entry.itemName} must exist in local catalog", entry.itemName in validItemNames)
                assertTrue("$prefix item ${entry.itemName} must include Coach advice", entry.description.isNotBlank())
            }

            if (build.role.contains("Soporte", ignoreCase = true)) {
                val requiredSupportItems = build.coreItems.filter {
                    it == "Hoz espectral" || it == "Escudo reliquia"
                }
                assertEquals("$prefix support must use exactly one required support item", 1, requiredSupportItems.size)
            }

            val t2 = assertNotNullAndGet("$prefix must have Tier 2 boots", build.bootsT2Item)
            val t3 = assertNotNullAndGet("$prefix must have Tier 3 boots", build.bootsT3Item)
            assertTrue("$prefix Tier 2 boots must exist in catalog", t2.itemName in validItemNames)
            assertTrue("$prefix Tier 3 boots must exist in catalog", t3.itemName in validItemNames)
            assertEquals("$prefix Tier 3 boots must be the exact evolution", bootPairs[t2.itemName], t3.itemName)
            assertTrue("$prefix Tier 2 boots need advice", t2.description.isNotBlank())
            assertTrue("$prefix Tier 3 boots need advice", t3.description.isNotBlank())

            val champion = champions.first { it.id == build.championId }
            val role = com.example.model.LaneRole.entries.first { it.displayName == build.role.substringBefore(" (") }
            val expectedBoots = com.example.util.BuildChoiceRules.boots(t2.itemName, champion.damageType,
                champion.isFrontline, champion.isRanged, role, champion.id)
            assertEquals("$prefix must follow conditional boot recommendations", expectedBoots.singleOrNull()?.name,
                build.situationalBootsT2Item?.itemName)
            assertEquals("$prefix must explain when to change boots", expectedBoots.singleOrNull()?.reason,
                build.situationalBootsT2Item?.description)

            val situationalT2 = build.situationalBootsT2Item
            val situationalT3 = build.situationalBootsT3Item
            assertEquals("$prefix optional boots must form a complete pair", situationalT2 == null, situationalT3 == null)
            if (situationalT2 != null && situationalT3 != null) {
                assertTrue("$prefix situational Tier 2 boots must exist in catalog", situationalT2.itemName in validItemNames)
                assertTrue("$prefix situational Tier 3 boots must exist in catalog", situationalT3.itemName in validItemNames)
                assertEquals("$prefix situational Tier 3 boots must be the exact evolution", bootPairs[situationalT2.itemName], situationalT3.itemName)
                assertFalse("$prefix situational boots must differ from primary boots", situationalT2.itemName.equals(t2.itemName, ignoreCase = true))
                assertTrue("$prefix situational Tier 2 boots need advice", situationalT2.description.isNotBlank())
                assertTrue("$prefix situational Tier 3 boots need advice", situationalT3.description.isNotBlank())
            }

            assertEquals("$prefix must have exactly 2 summoner spells", 2, build.spells.size)
            assertEquals("$prefix spells must be distinct", 2, build.spells.map { it.lowercase() }.distinct().size)
            assertEquals("$prefix spell detail list must match", build.spells, build.coreSpells.map { it.spellName })
            for (spell in build.coreSpells) {
                assertTrue("$prefix spell ${spell.spellName} must exist in local catalog", spell.spellName in validSpellNames)
                if (!spell.spellName.equals("Destello", ignoreCase = true)) {
                    assertTrue("$prefix non-Flash spell ${spell.spellName} needs advice", spell.description.isNotBlank())
                }
            }
            for (spell in build.situationalSpells) {
                assertTrue("$prefix situational spell ${spell.spellName} must exist in local catalog", spell.spellName in validSpellNames)
                assertFalse("$prefix situational spell cannot duplicate a main spell", build.spells.any { it.equals(spell.spellName, ignoreCase = true) })
                if (!spell.spellName.equals("Destello", ignoreCase = true)) {
                    assertTrue("$prefix situational spell ${spell.spellName} needs advice", spell.description.isNotBlank())
                }
            }

            assertEquals("$prefix must have 1 keystone + 4 secondaries", 5, build.coreRunes.size)
            val runeNames = build.coreRunes.map { it.runeName }
            assertEquals("$prefix rune names must be unique", 5, runeNames.distinct().size)
            assertEquals("$prefix runes string must mirror detailed runes", runeNames, build.runes.split(",").map { it.trim() })

            val runePage = runeNames.map { name ->
                com.example.util.BuildChoiceRules.RuneChoice(name, validRunesMap[name]?.category.orEmpty())
            }
            assertTrue("$prefix must keep the first three secondaries in one branch", com.example.util.BuildChoiceRules.validRunePage(runePage))
            val alternativeChoices = build.situationalRunes.map { entry ->
                com.example.util.BuildChoiceRules.RuneChoice(entry.runeName, validRunesMap[entry.runeName]?.category.orEmpty())
            }
            assertEquals("$prefix alternatives must have valid substitution groups", alternativeChoices.size,
                com.example.util.BuildChoiceRules.runeAlternatives(runePage, alternativeChoices).size)

            val keystone = validRunesMap[runeNames[0]]
            assertNotNull("$prefix keystone must exist in local catalog", keystone)
            assertEquals("$prefix first rune must be Clave", "Clave", keystone?.category)

            val secondaryRunes = runeNames.drop(1).map { name ->
                assertNotNullAndGet("$prefix secondary rune $name must exist in local catalog", validRunesMap[name])
            }
            val branchCounts = secondaryRunes.groupingBy { it.category }.eachCount().values.sorted()
            assertEquals("$prefix secondaries must be 3 from one branch + 1 from another", listOf(1, 3), branchCounts)
            assertTrue(
                "$prefix secondary rune categories must be valid",
                secondaryRunes.all { it.category in setOf("Dominación", "Precisión", "Valor", "Brujería") }
            )
            build.coreRunes.forEach { rune ->
                assertTrue("$prefix rune ${rune.runeName} needs advice", rune.description.isNotBlank())
            }

            for (rune in build.situationalRunes) {
                val catalogRune = assertNotNullAndGet(
                    "$prefix situational rune ${rune.runeName} must exist in local catalog",
                    validRunesMap[rune.runeName]
                )
                assertFalse("$prefix situational rune cannot be another keystone", catalogRune.category == "Clave")
                assertFalse("$prefix situational rune cannot duplicate a main rune", rune.runeName in runeNames)
                assertTrue("$prefix situational rune ${rune.runeName} needs advice", rune.description.isNotBlank())
            }
        }
    }

    @Test
    fun `catalog advice and boot pairs avoid known regressions`() {
        val serylda = WildRiftItemsData.list.first { it.name == "Rencor de Serylda" }
        assertTrue("Serylda passive must describe its slow", serylda.passive.contains("ralentizan", ignoreCase = true))
        assertFalse("Serylda advice must not claim a bleed", serylda.coachTip.contains("sangrado", ignoreCase = true))
        assertFalse("Serylda advice must not claim repeated slows apply Grievous Wounds", serylda.coachTip.contains("también aplica heridas graves", ignoreCase = true))
        assertTrue("Serylda advice must explicitly clarify anti-heal", serylda.coachTip.contains("No aplica Heridas Graves", ignoreCase = true))

        val berserkers = WildRiftItemsData.list.first { it.name == "Grebas de berserker" }
        assertTrue("Berserker passive is flat on-hit healing", berserkers.passive.contains("restauran 10 de vida", ignoreCase = true))
        assertFalse("Berserker advice must not describe the passive as life steal", berserkers.coachTip.contains("robo de vida al golpear", ignoreCase = true))
    }

    private fun <T : Any> assertNotNullAndGet(message: String, value: T?): T {
        assertNotNull(message, value)
        return requireNotNull(value)
    }
}
