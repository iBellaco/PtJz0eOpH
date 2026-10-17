package com.example

import com.example.model.Champion
import com.example.model.ChampionLaneStats
import com.example.model.LaneRole
import com.example.util.ChampionRoleAdapter
import com.example.ui.screens.publishedChampionRoles
import com.example.ui.screens.tierDisplayedStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChampionLaneStatsTest {
    @Test fun `selected lane never reuses primary lane rates`() {
        val champion = Champion(
            id = "hwei", name = "Hwei", primaryRole = LaneRole.MID,
            secondaryRoles = listOf(LaneRole.SUPPORT), winrate = 52.5, pickRate = 7.4, banRate = 61.0,
            laneStats = mapOf(
                LaneRole.MID.name to ChampionLaneStats(52.5, 7.4, 61.0),
                LaneRole.SUPPORT.name to ChampionLaneStats(54.6, 1.1, 76.6)
            )
        )
        val mid = ChampionRoleAdapter.getProfile(champion, LaneRole.MID)
        val support = ChampionRoleAdapter.getProfile(champion, LaneRole.SUPPORT)
        assertEquals(52.5, mid.winrate, 0.001)
        assertEquals(54.6, support.winrate, 0.001)
        assertEquals(1.1, support.pickRate, 0.001)
        assertEquals(76.6, support.banRate, 0.001)

        val unpublished = ChampionRoleAdapter.getProfile(champion.copy(laneStats = champion.laneStats - LaneRole.SUPPORT.name), LaneRole.SUPPORT)
        assertTrue(unpublished.winrate.isNaN())
    }

    @Test fun `flex selector only exposes lanes with complete published rates`() {
        val champion = Champion(
            id = "rammus", name = "Rammus", primaryRole = LaneRole.JUNGLE,
            secondaryRoles = listOf(LaneRole.TOP, LaneRole.SUPPORT),
            laneStats = mapOf(
                LaneRole.JUNGLE.name to ChampionLaneStats(52.1, 4.2, 3.0),
                LaneRole.SUPPORT.name to ChampionLaneStats(49.8, 0.4, 0.1),
                LaneRole.TOP.name to ChampionLaneStats()
            )
        )

        assertEquals(listOf(LaneRole.JUNGLE, LaneRole.SUPPORT), publishedChampionRoles(champion))

        val partial = champion.copy(laneStats = champion.laneStats +
            (LaneRole.TOP.name to ChampionLaneStats(winRate = 50.0)))
        assertEquals(listOf(LaneRole.JUNGLE, LaneRole.SUPPORT), publishedChampionRoles(partial))
    }

    @Test fun `tier card and detail use the same active lane rates`() {
        val champion = Champion(
            id = "malphite", name = "Malphite", primaryRole = LaneRole.TOP,
            secondaryRoles = listOf(LaneRole.SUPPORT), winrate = 51.72, pickRate = 8.75, banRate = 44.13,
            laneStats = mapOf(
                LaneRole.TOP.name to ChampionLaneStats(54.77, 7.81, 44.13),
                LaneRole.SUPPORT.name to ChampionLaneStats(52.10, 2.20, 18.0)
            )
        )
        val card = tierDisplayedStats(champion, null)
        val detail = ChampionRoleAdapter.getProfile(champion, LaneRole.TOP)
        assertEquals(detail.winrate, card.win, 0.001)
        assertEquals(detail.pickRate, card.pick, 0.001)
        assertEquals(detail.banRate, card.ban, 0.001)
    }
}
