package com.example

import android.app.Application
import com.example.data.DraftScoringPolicy
import com.example.data.MatchupKnowledge
import com.example.data.MatchupRelation
import com.example.data.WildRiftRepository
import com.example.model.LaneRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.example.ui.screens.shouldShowDraftRecommendations
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
    fun `blind pick returns ranked picks without a draft generated build`() {
        val result = WildRiftRepository.analyzeDraft(
            myRole = LaneRole.MID,
            allies = emptyList(),
            enemies = emptyList(),
            isFirstPick = true
        )
        assertTrue("Blind Pick must not be a no-op", result.isFirstPickMode)
        assertTrue(result.recommendations.isNotEmpty())
        assertTrue(result.bestOverallPick != null)
        assertTrue(shouldShowDraftRecommendations(LaneRole.MID, isFirstPick = true, hasDraftSelection = false))
        assertFalse(shouldShowDraftRecommendations(null, isFirstPick = true, hasDraftSelection = false))
    }
}
