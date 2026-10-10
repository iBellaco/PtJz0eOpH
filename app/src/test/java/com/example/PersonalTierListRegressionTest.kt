package com.example

import com.example.data.WildRiftRepository
import com.example.data.analytics.PersonalTierListManager as Manager
import com.example.data.analytics.TierGrade
import com.example.data.local.entity.SavedDraftEntity
import com.example.model.Champion
import com.example.model.LaneRole
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PersonalTierListRegressionTest {
    @Before fun catalog() {
        WildRiftRepository.champions.clear()
        WildRiftRepository.champions.addAll(listOf(
            Champion(id="teemo", name="Teemo", primaryRole=LaneRole.TOP),
            Champion(id="ahri", name="Ahri", primaryRole=LaneRole.MID),
            Champion(id="garen", name="Garen", primaryRole=LaneRole.TOP),
            Champion(id="hecarim", name="Hecarim", primaryRole=LaneRole.JUNGLE)))
    }
    private fun draft(id: Long = 1, result: String = "VICTORY", score: String = "") = SavedDraftEntity(
        id=id, userRole="TOP", myChampionId="teemo", myChampionName="Teemo",
        enemyLaneOpponentName="Garen", matchResult=result, myScore=score,
        allyPicksJson="""[{"championId":"teemo","championName":"Teemo","role":"TOP"},{"championId":"ahri","championName":"Ahri","role":"MID"}]""",
        enemyPicksJson="""[{"championId":"hecarim","championName":"Hecarim","role":"JUNGLE"},{"championId":"garen","championName":"Garen","role":"TOP"}]""")

    @Test fun `catalog and teammates are not personal played champions`() {
        val result = Manager.calculatePersonalTierList(listOf(draft()))
        assertEquals(listOf("Teemo"), result.allRankedChampions.map { it.championName })
        assertEquals(TierGrade.PROVISIONAL, result.allRankedChampions.single().tier)
        assertNull(result.overview.best1v1Matchup)
        assertNull(result.overview.nemesisOpponent)
        assertNull(result.overview.bestRole)
        assertEquals(1, result.overview.totalGames)
    }
    @Test fun `missing and ambiguous player slots never select first ally`() {
        val missing = draft().copy(myChampionId="", myChampionName="", userRole="SUPPORT")
        val ambiguous = missing.copy(userRole="MID", allyPicksJson="""[{"championId":"teemo","championName":"Teemo","role":"MID"},{"championId":"ahri","championName":"Ahri","role":"MID"}]""")
        val result = Manager.calculatePersonalTierList(listOf(missing, ambiguous, missing.copy(userRole="INVALID")))
        assertTrue(result.allRankedChampions.isEmpty())
        assertEquals(3, result.overview.excludedUnidentifiedGames)
        assertEquals(0, result.overview.totalWins)
    }
    @Test fun `legacy identity uses unique exact role and never first enemy`() {
        val legacy = draft().copy(myChampionId="", myChampionName="", enemyLaneOpponentName="")
        assertEquals("Teemo", Manager.extractPlayerChampionName(legacy))
        assertEquals("Garen", Manager.recordedLaneOpponent(legacy))
        assertEquals("", Manager.recordedLaneOpponent(legacy.copy(userRole="SUPPORT")))
        assertEquals("", Manager.recordedLaneOpponent(legacy.copy(enemyPicksJson="invalid")))
        assertEquals("", Manager.extractPlayerChampionName(legacy.copy(myChampionId="empty", myChampionName="empty")))
    }
    @Test fun `optional KDA excludes blanks pending results invalid notes and keeps zero`() {
        val stats = Manager.calculatePersonalTierList(listOf(draft(1, score="0/4/0"), draft(2, score="6/0/4"),
            draft(3), draft(4, score="nota 12"), draft(5, result="PENDING", score="99/0/99"))).allRankedChampions.single()
        assertEquals(2, stats.kdaGames)
        assertEquals(5.0, stats.avgKda, 0.001)
        assertTrue(stats.coachVerdict.contains("2 de 4"))
        assertNull(Manager.parseKdaFromScore("nota 12"))
        assertNull(Manager.parseKdaFromScore("-5"))
        assertNull(Manager.parseKdaFromScore("1/2"))
        assertEquals(0.0, Manager.parseKdaFromScore("0/0/0")!!, 0.0)
        assertEquals(4.2, Manager.parseKdaFromScore("KDA 4.2")!!, 0.001)
    }
    @Test fun `KDA is context and does not inflate rank`() {
        val originals = (1L..6L).map { draft(it, if(it <= 3) "VICTORY" else "DEFEAT") }
        val noKda = Manager.calculatePersonalTierList(originals).allRankedChampions.single()
        val highKda = Manager.calculatePersonalTierList(originals.map { it.copy(myScore="999/0/999") }).allRankedChampions.single()
        assertEquals(noKda.tierScore, highKda.tierScore, 0.0)
        assertEquals(noKda.tier, highKda.tier)
    }
    @Test fun `pending results do not become losses or matchup records`() {
        val stats = Manager.calculatePersonalTierList(listOf(draft(result="PENDING", score="2/1/3"))).allRankedChampions.single()
        assertEquals(1, stats.pending)
        assertTrue(stats.matchups.isEmpty())
        assertEquals(0, stats.kdaGames)
        assertEquals(TierGrade.PROVISIONAL, stats.tier)
    }
    @Test fun `matchup filter isolates own champion role and account`() {
        val drafts = listOf(draft(1), draft(2).copy(myChampionId="ahri", myChampionName="Ahri"),
            draft(3).copy(userRole="MID"), draft(4).copy(accountProfileId="other"),
            draft(5).copy(enemyLaneOpponentName="Hecarim"))
        assertEquals(listOf(1L), Manager.draftsForMatchup(drafts,"Teemo","Garen",LaneRole.TOP,"default").map { it.id })
        assertTrue(Manager.draftsForMatchup(drafts,"Teemo","Garen",null,"default").isEmpty())
    }
    @Test fun `lane and queue filters apply before analysis`() {
        val drafts = listOf(draft(1),draft(2).copy(isLegendary=true),draft(3).copy(userRole="MID"))
        val result = Manager.calculatePersonalTierList(drafts, LaneRole.TOP, modeFilter="LEGENDARY")
        assertEquals(1, result.overview.totalGames)
        assertEquals(2L, result.allRankedChampions.single().draftMatches.single().id)
    }
    @Test fun `most played champion is not replaced by single lucky win`() {
        val drafts = (1L..5L).map { draft(it,"DEFEAT") } + draft(6).copy(myChampionId="ahri",myChampionName="Ahri")
        assertEquals("Teemo",Manager.calculatePersonalTierList(drafts).overview.signatureChampion?.championName)
    }
    @Test fun `five outcomes allow descriptive matchup but never claim duel mastery in either language`() {
        for (lang in listOf("es", "pt")) {
            val result=Manager.calculatePersonalTierList((1L..5L).map { draft(it) },lang=lang)
            assertEquals("Garen",result.overview.best1v1Matchup?.opponentName)
            assertNull(result.overview.nemesisOpponent)
            assertTrue(result.overview.coach1v1Analysis.contains("5V - 0D"))
            assertFalse(result.overview.coach1v1Analysis.contains("prioridad absoluta"))
            assertFalse(result.allRankedChampions.single().coachVerdict.contains("Maestría absoluta"))
        }
    }
    @Test fun `imported champions retain optional KDA and lane records without catalog entry`() {
        val stats=Manager.calculatePersonalTierList(listOf(draft().copy(myChampionId="custom",myChampionName="Custom",myScore="4/2/6")))
            .allRankedChampions.single()
        assertEquals("Custom",stats.championName)
        assertEquals(1,stats.kdaGames)
        assertEquals("Garen",stats.matchups.single().opponentName)
    }
}
