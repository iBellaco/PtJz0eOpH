package com.example

import com.example.model.Champion
import com.example.model.ChampionLaneStats
import com.example.model.LaneRole
import com.example.util.ChampionRoleAdapter
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
}
