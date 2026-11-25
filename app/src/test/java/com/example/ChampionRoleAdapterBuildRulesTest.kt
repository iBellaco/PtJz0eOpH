package com.example

import com.example.model.Champion
import com.example.model.ChampionBuild
import com.example.model.LaneRole
import com.example.data.WildRiftSpellsAndRunes
import com.example.util.ChampionBuildOption
import com.example.util.ChampionRoleAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ChampionRoleAdapterBuildRulesTest {

    @Test
    fun `jungle build always places smite before the secondary spell`() {
        val champion = Champion(
            id = "test-jungle",
            name = "Test Jungle",
            primaryRole = LaneRole.JUNGLE,
            builds = listOf(
                ChampionBuild(
                    role = "jungle",
                    coreItems = listOf("Fuerza de trinidad", "Recaudadora", "Ángel de la guarda"),
                    spells = listOf("Destello", "Ignición")
                )
            )
        )

        val option = ChampionRoleAdapter.getProfile(champion, LaneRole.JUNGLE).buildOptions.single()
        val canonicalSmite = requireNotNull(WildRiftSpellsAndRunes.getSpellByName("Castigo")).name

        assertEquals(canonicalSmite, option.spells.first())
        assertEquals(2, option.spells.size)
        assertFalse(option.spells.drop(1).any { it.equals(canonicalSmite, ignoreCase = true) })
    }

    @Test
    fun `jungle normalization restores smite when an imported build omits it`() {
        val option = ChampionRoleAdapter.normalizeBuildOption(
            ChampionBuildOption(
                optionNumber = 1,
                title = "Jungla heredada",
                subtitle = "",
                source = "",
                badge = "",
                tacticalReason = "",
                items = listOf("Fuerza de trinidad", "Danza de la muerte"),
                bootBase = "Botas blindadas",
                bootUpgrade = "Avance blindado",
                runes = emptyList(),
                spells = listOf("Destello", "Ignición"),
                spellsIcons = emptyList()
            ),
            Champion(id = "lee-sin", name = "Lee Sin", primaryRole = LaneRole.JUNGLE),
            LaneRole.JUNGLE
        )
        val canonicalSmite = requireNotNull(WildRiftSpellsAndRunes.getSpellByName("Castigo")).name

        assertEquals(canonicalSmite, option.spells.first())
        assertEquals(2, option.spells.size)
    }

    @Test
    fun `support normalization inserts one quest item and removes repeated core items`() {
        val option = ChampionRoleAdapter.normalizeBuildOption(
            ChampionBuildOption(
                optionNumber = 1,
                title = "Soporte legado",
                subtitle = "",
                source = "",
                badge = "",
                tacticalReason = "",
                items = listOf("Eco de Luden", "Eco de Luden", "Incensario ardiente"),
                bootBase = "Botas jonias de la lucidez",
                bootUpgrade = "Lucidez carmesí",
                runes = emptyList(),
                spells = listOf("Destello", "Castigo"),
                spellsIcons = emptyList()
            ),
            Champion(id = "nami", name = "Nami", primaryRole = LaneRole.SUPPORT),
            LaneRole.SUPPORT
        )

        assertEquals(1, option.items.count(ChampionRoleAdapter::isSupportStarter))
        assertEquals(option.items.size, option.items.map { it.lowercase() }.distinct().size)
        assertFalse(option.spells.any { it.equals("Castigo", ignoreCase = true) })
        assertEquals(2, option.spells.size)
    }
}
