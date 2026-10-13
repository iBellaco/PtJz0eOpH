package com.example

import android.app.Application
import com.example.data.DraftScoringPolicy
import com.example.data.DraftBuildPolicy
import com.example.data.MatchupKnowledge
import com.example.data.MatchupRelation
import com.example.data.WildRiftItemsData
import com.example.data.WildRiftSpellsAndRunes
import com.example.data.WildRiftRepository
import com.example.model.LaneRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class DraftRecommendationQualityTest {
    @Before
    fun loadCatalog() {
        WildRiftRepository.initChampions(RuntimeEnvironment.getApplication(), forceReload = true)
    }

    @Test
    fun `akali is a losing mid matchup for orianna and cannot enter the main recommendations`() {
        val akali = WildRiftRepository.champions.first { it.id == "akali" }
        val orianna = WildRiftRepository.champions.first { it.id == "orianna" }
        assertEquals(MatchupRelation.UNFAVORABLE, MatchupKnowledge.relation(orianna, akali, LaneRole.MID))

        val result = WildRiftRepository.analyzeDraft(
            myRole = LaneRole.MID,
            allies = emptyList(),
            enemies = listOf(akali),
            enemyLaneOpponent = akali
        )
        assertFalse(result.recommendations.take(5).any { it.champion.id == "orianna" })
        assertTrue(result.recommendations.take(10).all { it.champion.primaryRole == LaneRole.MID })
    }

    @Test
    fun `team synergy changes the score when a real combo is present`() {
        val orianna = WildRiftRepository.champions.first { it.id == "orianna" }
        val malphite = WildRiftRepository.champions.first { it.id == "malphite" }
        val roster = WildRiftRepository.champions.toList()
        val withoutAlly = DraftScoringPolicy.score(orianna, LaneRole.MID, emptyList(), emptyList(), null, false, roster)
        val withCombo = DraftScoringPolicy.score(orianna, LaneRole.MID, listOf(malphite), emptyList(), null, false, roster)
        assertTrue("Orianna should gain value from Malphite's engage", withCombo > withoutAlly)
    }

    @Test
    fun `peel and frontline are preferred against a double assassin composition`() {
        val braum = WildRiftRepository.champions.first { it.id == "braum" }
        val senna = WildRiftRepository.champions.first { it.id == "senna" }
        val enemies = listOf(
            WildRiftRepository.champions.first { it.id == "akali" },
            WildRiftRepository.champions.first { it.id == "zed" }
        )
        val roster = WildRiftRepository.champions.toList()
        val frontlineScore = DraftScoringPolicy.score(braum, LaneRole.SUPPORT, emptyList(), enemies, null, false, roster)
        val exposedScore = DraftScoringPolicy.score(senna, LaneRole.SUPPORT, emptyList(), enemies, null, false, roster)
        assertTrue("The support recommendation must account for assassin access", frontlineScore > exposedScore)
    }

    @Test
    fun `blind pick returns ranked picks and a catalog-only custom build`() {
        val result = WildRiftRepository.analyzeDraft(
            myRole = LaneRole.MID,
            allies = emptyList(),
            enemies = emptyList(),
            isFirstPick = true
        )
        assertTrue("Blind Pick must not be a no-op", result.isFirstPickMode)
        assertTrue(result.recommendations.isNotEmpty())
        val plan = result.bestOverallPick?.draftBuild
        assertTrue(plan != null)
        assertEquals(3, plan!!.coreItems.size)
        assertTrue(plan.coreItems.all { WildRiftItemsData.getItemByName(it) != null })
        assertTrue(plan.situationalItems.all { WildRiftItemsData.getItemByName(it) != null })
        assertTrue(plan.runes.all { WildRiftSpellsAndRunes.getRuneByName(it) != null })
        assertTrue(plan.spells.all { WildRiftSpellsAndRunes.getSpellByName(it) != null })
    }

    @Test
    fun `draft build changes for healing and tanks and keeps support starter`() {
        val orianna = WildRiftRepository.champions.first { it.id == "orianna" }
        val soraka = WildRiftRepository.champions.first { it.id == "soraka" }
        val malphite = WildRiftRepository.champions.first { it.id == "malphite" }
        val antiHealing = DraftBuildPolicy.create(orianna, LaneRole.MID, emptyList(), listOf(soraka))
        assertTrue(antiHealing.situationalItems.any { it.equals("Morellonomicón", true) })

        val braum = WildRiftRepository.champions.first { it.id == "braum" }
        val supportPlan = DraftBuildPolicy.create(braum, LaneRole.SUPPORT, emptyList(), listOf(malphite))
        assertTrue(supportPlan.coreItems.any { it.equals("Escudo reliquia", true) })
        assertTrue(supportPlan.spells.any { it.equals("Destello", true) })
    }

    @Test
    fun `draft exposes separate lane and complete team plans and uses AP anti shield`() {
        val ahri = WildRiftRepository.champions.first { it.id == "ahri" }
        val laneRival = WildRiftRepository.champions.first { it.id == "lulu" }
        val fullEnemies = listOf("lulu", "braum", "shen", "karma", "sett").map { id ->
            WildRiftRepository.champions.first { it.id == id }
        }
        val result = WildRiftRepository.analyzeDraft(
            myRole = LaneRole.MID,
            allies = listOf(ahri),
            enemies = fullEnemies,
            enemyLaneOpponent = laneRival
        )
        val plan = result.recommendations.first().draftBuilds
        assertTrue("A known lane rival must produce a 1v1 plan", plan.oneVsOne != null)
        assertTrue("Five enemy champions must produce a complete-team plan", plan.fullTeam != null)
        assertEquals(listOf("lulu"), plan.oneVsOne!!.matchupChampions.map { WildRiftRepository.getChampionByName(it)?.id ?: it })
        assertEquals(fullEnemies.map { it.id }, plan.fullTeam!!.matchupChampions.map { WildRiftRepository.getChampionByName(it)?.id ?: it })
        assertTrue("The lane build must identify its 1v1 scope", plan.oneVsOne.title.contains("Enfrentamiento 1v1"))
        assertTrue("The second build must identify the complete enemy draft", plan.fullTeam.title.contains("Composición completa"))
        assertNotEquals("The two plans must remain independently calculated", plan.oneVsOne.title, plan.fullTeam.title)
        val antiShield = DraftBuildPolicy.create(ahri, LaneRole.MID, emptyList(), listOf(laneRival))
        assertTrue("AP champions need the catalog anti-shield item", antiShield.situationalItems.any { it.equals("Tridente de Oceánida", true) })
    }
}
