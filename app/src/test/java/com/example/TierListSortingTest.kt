package com.example

import com.example.model.Champion
import com.example.model.ChampionLaneStats
import com.example.model.LaneRole
import com.example.ui.screens.TierSortOption
import com.example.ui.screens.sortTierListChampions
import org.junit.Assert.assertEquals
import org.junit.Test

class TierListSortingTest {
    private fun champion(id: String, win: Double, ban: Double) = Champion(
        id = id,
        name = id,
        primaryRole = LaneRole.MID,
        tier = "A",
        hasRegionalStats = true,
        statsCheckedAt = 1L,
        laneStats = mapOf(
            LaneRole.MID.name to ChampionLaneStats(
                winRate = win,
                pickRate = 5.0,
                banRate = ban,
                tier = "A"
            )
        )
    )

    @Test fun `China can sort by win rate using its selected lane`() {
        val rows = sortTierListChampions(
            listOf(champion("low", 49.0, 8.0), champion("high", 55.0, 1.0)),
            TierSortOption.WIN_RATE,
            LaneRole.MID,
            "CHINA"
        )
        assertEquals(listOf("high", "low"), rows.map { it.id })
    }

    @Test fun `China can sort by ban rate using its selected lane`() {
        val rows = sortTierListChampions(
            listOf(champion("low", 55.0, 1.0), champion("high", 49.0, 8.0)),
            TierSortOption.BAN_RATE,
            LaneRole.MID,
            "CHINA"
        )
        assertEquals(listOf("high", "low"), rows.map { it.id })
    }
}
