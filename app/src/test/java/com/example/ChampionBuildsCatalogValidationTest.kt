package com.example

import com.example.data.WildRiftItemsData
import com.example.data.WildRiftSpellsAndRunes
import com.example.data.local.CustomChampionBuildRecord
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
    fun `validate all 142 champions have official builds conforming to rules`() {
        val file = File("src/main/assets/champions_creator_builds.json")
        assertTrue("champions_creator_builds.json file must exist", file.exists())

        val raw = file.readText()
        val builds = json.decodeFromString<List<CustomChampionBuildRecord>>(raw)

        assertTrue("Must have at least 142 builds", builds.size >= 142)

        val uniqueChampions = builds.map { it.championId }.distinct()
        assertEquals("Total champions with builds must be exactly 142", 142, uniqueChampions.size)

        val validItemNames = WildRiftItemsData.list.map { it.name }.toSet()
        val validSpellNames = WildRiftSpellsAndRunes.summonerSpells.map { it.name }.toSet()
        val validRunesMap = WildRiftSpellsAndRunes.runes.associateBy { it.name }

        val supportBuilds = builds.filter { it.role.contains("Soporte", ignoreCase = true) }
        assertTrue("Must have support builds", supportBuilds.isNotEmpty())

        for (b in builds) {
            // Rule 1: 3 core items
            assertEquals("Build ${b.id} must have exactly 3 core items", 3, b.coreItems.size)
            assertEquals("Build ${b.id} coreItemsWithDesc must be 3", 3, b.coreItemsWithDesc.size)
            for (coreEntry in b.coreItemsWithDesc) {
                assertTrue("Item ${coreEntry.itemName} must exist in catalog", validItemNames.contains(coreEntry.itemName))
                assertTrue("Item ${coreEntry.itemName} must have coach tip description", coreEntry.description.isNotBlank())
            }

            // Rule 2: Minimum 2 optional items
            assertTrue("Build ${b.id} must have at least 2 situational items", b.situationalItems.size >= 2)
            assertTrue("Build ${b.id} situationalItemsWithDesc must have at least 2 items", b.situationalItemsWithDesc.size >= 2)
            for (sitEntry in b.situationalItemsWithDesc) {
                assertTrue("Item ${sitEntry.itemName} must exist in catalog", validItemNames.contains(sitEntry.itemName))
                assertTrue("Item ${sitEntry.itemName} must have coach tip description", sitEntry.description.isNotBlank())
            }

            // Rule 3: Support builds must have exactly ONE of Hoz espectral or Escudo reliquia
            if (b.role.contains("Soporte", ignoreCase = true)) {
                val hasHoz = b.coreItems.contains("Hoz espectral")
                val hasEscudo = b.coreItems.contains("Escudo reliquia")
                assertTrue("Support build ${b.id} must have Hoz espectral or Escudo reliquia", hasHoz || hasEscudo)
                assertFalse("Support build ${b.id} cannot have both Hoz espectral and Escudo reliquia", hasHoz && hasEscudo)
            }

            // Rule 4: Boots Nivel 2 + Nivel 3 and situational boots
            assertNotNull("Build ${b.id} must have bootsT2Item", b.bootsT2Item)
            assertNotNull("Build ${b.id} must have bootsT3Item", b.bootsT3Item)
            assertTrue("Boot T2 ${b.bootsT2Item?.itemName} must exist in catalog", validItemNames.contains(b.bootsT2Item?.itemName))
            assertTrue("Boot T3 ${b.bootsT3Item?.itemName} must exist in catalog", validItemNames.contains(b.bootsT3Item?.itemName))
            assertTrue("Boot T2 must have coach description", b.bootsT2Item?.description?.isNotBlank() == true)
            assertTrue("Boot T3 must have coach description", b.bootsT3Item?.description?.isNotBlank() == true)

            assertNotNull("Build ${b.id} must have situationalBootsT2Item", b.situationalBootsT2Item)
            assertNotNull("Build ${b.id} must have situationalBootsT3Item", b.situationalBootsT3Item)
            assertTrue("Boot T2 Sit must have description", b.situationalBootsT2Item?.description?.isNotBlank() == true)
            assertTrue("Boot T3 Sit must have description", b.situationalBootsT3Item?.description?.isNotBlank() == true)

            // Rule 5: Exactly 2 summoner spells without duplicate
            assertEquals("Build ${b.id} must have exactly 2 spells", 2, b.spells.size)
            assertFalse("Build ${b.id} spells must not duplicate", b.spells[0].equals(b.spells[1], ignoreCase = true))
            for (s in b.coreSpells) {
                assertTrue("Spell ${s.spellName} must exist in catalog", validSpellNames.contains(s.spellName))
                if (!s.spellName.equals("Destello", ignoreCase = true)) {
                    assertTrue("Non-flash spell ${s.spellName} must have coach tip", s.description.isNotBlank())
                }
            }

            // Rule 6: Runes: 1 Keystone + 4 Secondaries (3 same branch + 1 other branch)
            assertEquals("Build ${b.id} must have 5 runes in coreRunes", 5, b.coreRunes.size)
            val keystone = b.coreRunes[0]
            val keystoneRuneItem = validRunesMap[keystone.runeName]
            assertNotNull("Keystone ${keystone.runeName} must exist in catalog", keystoneRuneItem)
            assertEquals("Rune 0 must be Clave category", "Clave", keystoneRuneItem?.category)

            val sec1 = validRunesMap[b.coreRunes[1].runeName]
            val sec2 = validRunesMap[b.coreRunes[2].runeName]
            val sec3 = validRunesMap[b.coreRunes[3].runeName]
            val sec4 = validRunesMap[b.coreRunes[4].runeName]

            assertNotNull(sec1)
            assertNotNull(sec2)
            assertNotNull(sec3)
            assertNotNull(sec4)

            // 3 of same branch
            val branchPrimary = sec1!!.category
            assertTrue("Branch must be one of secondary categories", branchPrimary in listOf("Dominación", "Precisión", "Valor", "Brujería"))
            assertEquals("Sec 2 must match primary branch", branchPrimary, sec2!!.category)
            assertEquals("Sec 3 must match primary branch", branchPrimary, sec3!!.category)

            // all 3 distinct
            val distinctPrimary = setOf(sec1.name, sec2.name, sec3.name)
            assertEquals("Primary secondary runes must be 3 distinct runes", 3, distinctPrimary.size)

            // 1 of other branch
            val branchOther = sec4!!.category
            assertTrue("Branch other must be one of secondary categories", branchOther in listOf("Dominación", "Precisión", "Valor", "Brujería"))
            assertFalse("Fourth secondary rune must be from a different branch", branchOther == branchPrimary)
        }
    }
}
