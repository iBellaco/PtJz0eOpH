package com.example

import com.example.data.GamePatch
import com.example.data.WildRiftItemsData
import com.example.data.WildRiftSpellsAndRunes
import com.example.model.Champion
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class Patch73fCatalogTest {
    @Test fun officialBalanceMatchesPatch73f() {
        assertEquals("7.3f", GamePatch.VERSION)
        fun item(id: String) = WildRiftItemsData.getItemById(id)!!
        assertTrue(item("eclipse").stats.contains("+60 Daño"))
        assertEquals(2550, item("morellonomicon").goldCost)
        assertEquals(2600, item("rod_of_ages").goldCost)
        assertTrue(item("rod_of_ages").passive.contains("20 de vida, 40 de maná"))
        assertTrue(item("luden_s_echo").passive.contains("80 + 9%"))
        assertTrue(item("lich_bane").passive.contains("50% del poder"))
        assertTrue(item("hextech_roketbelt").stats.contains("+300 salud"))
        assertTrue(item("liandry_s_torment").stats.contains("+60 de poder"))
        assertTrue(item("goredrinker").stats.contains("+45 Daño"))
        assertTrue(item("bulwark_of_the_mountain").passive.contains("60 de oro"))
        assertTrue(item("black_mist_scythe").passive.contains("60 de oro"))
        assertTrue(WildRiftSpellsAndRunes.summonerSpells.single { it.id == "smite" }
            .description.contains("18-158"))
    }

    @Test fun sylasHasTranslatedAbilitiesAndCatalogBuildsWithoutInventedStats() {
        val json = Json { ignoreUnknownKeys = true }
        val champions = listOf("champions_part1.json", "champions_part2.json")
            .flatMap { json.decodeFromString<List<Champion>>(File("src/main/res/raw/$it").readText()) }
        val sylas = champions.single { it.id == "sylas" }
        assertEquals("SIN DATOS", sylas.tier)
        assertEquals(5, sylas.skills.size)
        assertEquals(listOf("P", "1", "2", "3", "4"), sylas.skills.map { it.slot })
        assertTrue(sylas.builds.all { build -> build.coreItems.all { item -> WildRiftItemsData.list.any { it.name == item } } })
        assertEquals(setOf("mid", "jungle"), sylas.builds.map { it.role }.toSet())
        val pt = Json.decodeFromString<Map<String, String>>(File("src/main/assets/translations_pt.json").readText())
        sylas.skills.forEach { assertTrue(pt[it.description].orEmpty().isNotBlank()) }
    }
}
