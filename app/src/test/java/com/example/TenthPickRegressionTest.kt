package com.example

import com.example.model.Champion
import com.example.model.LaneRole
import com.example.service.screen.ChampionNameResolver
import com.example.service.screen.DraftPickTurn
import com.example.service.screen.DraftVisionScanner
import com.example.service.screen.LiteRTVisionClassifier
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TenthPickRegressionTest {
    private val vi = Champion(id = "vi", name = "Vi", primaryRole = LaneRole.JUNGLE)

    @Before
    fun setUp() {
        DraftVisionScanner.resetSlotMemory()
    }

    @After
    fun tearDown() {
        DraftVisionScanner.resetSlotMemory()
    }

    @Test fun lateScanRecoversTheOrderFromEveryUnambiguousDraftPrefix() {
        for (firstAlly in listOf(true, false)) {
            val sequence = DraftVisionScanner.getDraftPickSequence(firstAlly)
            assertEquals(listOf(true, false, false, true, true, false, false, true, true, false),
                sequence.map { if (firstAlly) it.isAlly else !it.isAlly })
            assertEquals(!firstAlly, sequence.last().isAlly)
            for (count in 0..10) {
                val prefix = sequence.take(count)
                val allies = prefix.count { it.isAlly }
                val rivals = count - allies
                val inferred = com.example.service.screen.DraftPickOrderPolicy.inferFirstPick(allies, rivals)
                if (count in listOf(1, 3, 5, 7, 9)) assertEquals(firstAlly, inferred)
                else assertNull(inferred)
            }
        }
        assertNull(com.example.service.screen.DraftPickOrderPolicy.inferFirstPick(2, 0))
        assertNull(com.example.service.screen.DraftPickOrderPolicy.inferFirstPick(5, 2))
        assertEquals(true, DraftVisionScanner.recoverFirstPick(5, 4))
        assertEquals(true, DraftVisionScanner.recoverFirstPick(5, 5))
        DraftVisionScanner.resetSlotMemory()
        assertNull(DraftVisionScanner.recoverFirstPick(4, 4))
        assertEquals(false, DraftVisionScanner.recoverFirstPick(4, 5))
        assertEquals(false, DraftVisionScanner.recoverFirstPick(5, 5))
    }

    @Test
    fun excludesOnlyTheTargetSlotOnEitherSide() {
        for (isAlly in listOf(true, false)) {
            for (i in 0..4) {
                DraftVisionScanner.allySlotConfirmedChampions[i] = Champion(id = "ally$i")
                DraftVisionScanner.enemySlotConfirmedChampions[i] = Champion(id = "enemy$i")
            }
            val target = if (isAlly) DraftVisionScanner.allySlotConfirmedChampions
                else DraftVisionScanner.enemySlotConfirmedChampions
            target[4] = vi
            val otherPicks = DraftVisionScanner.getConfirmedPicksExcept(DraftPickTurn(10, isAlly, 4))
            assertEquals(9, otherPicks.size)
            assertFalse(otherPicks.any { it.id == "vi" })
            assertTrue(otherPicks.any { it.id == if (isAlly) "enemy4" else "ally4" })
            target[4] = null
            assertEquals(otherPicks, DraftVisionScanner.getConfirmedPicksExcept(DraftPickTurn(10, isAlly, 4)))
        }
    }

    @Test
    fun missingEarlierPickDoesNotCountTheTargetAsTheNinthPick() {
        for (i in 0..4) {
            DraftVisionScanner.allySlotConfirmedChampions[i] = Champion(id = "ally$i")
            DraftVisionScanner.enemySlotConfirmedChampions[i] = Champion(id = "enemy$i")
        }
        DraftVisionScanner.allySlotConfirmedChampions[4] = vi
        DraftVisionScanner.enemySlotConfirmedChampions[3] = null
        assertEquals(8, DraftVisionScanner.getConfirmedPicksExcept(DraftPickTurn(10, true, 4)).size)
    }

    @Test
    fun rememberedLaneSurvivesChampionNameReplacingLaneText() {
        DraftVisionScanner.allySlotOcrLaneCache[4] = LaneRole.TOP
        DraftVisionScanner.allySlotRolesCache[4] = LaneRole.JUNGLE
        DraftVisionScanner.allySlotConfirmedChampions[4] = vi
        assertEquals(LaneRole.TOP, DraftVisionScanner.getAllySlotRole(4))
        assertEquals(mapOf(4 to LaneRole.TOP), DraftVisionScanner.getRememberedAllyRoles(emptyMap()))
    }

    @Test
    fun currentLaneChangeTakesPriorityWithoutDuplicatingRememberedRoles() {
        DraftVisionScanner.allySlotOcrLaneCache[0] = LaneRole.TOP
        DraftVisionScanner.allySlotOcrLaneCache[4] = LaneRole.JUNGLE
        val resolved = DraftVisionScanner.getRememberedAllyRoles(mapOf(4 to LaneRole.TOP))
        assertEquals(mapOf(4 to LaneRole.TOP), resolved)
    }

    @Test
    fun explicitTenthSelectionSurvivesFurtherValidFrames() = runBlocking {
        val portrait = android.graphics.Bitmap.createBitmap(64, 64, android.graphics.Bitmap.Config.ARGB_8888)
        for (isAlly in listOf(true, false)) {
            LiteRTVisionClassifier.reset()
            LiteRTVisionClassifier.manuallyConfirmTenthPick(vi)
            repeat(3) {
                val decision = LiteRTVisionClassifier.executeTenthPickInference(portrait, isAlly,
                    emptySet(), confirmedPicksCount = 9)
                assertEquals(vi, decision?.first)
                assertTrue(LiteRTVisionClassifier.reportFlow.value.isConfirmed)
                assertEquals(100, decision?.second)
            }
        }
        portrait.recycle()
    }

    @Test
    fun knownTenthChampionReplacesStaleVisualReportOnEitherSide() = runBlocking {
        for (isAlly in listOf(true, false)) {
            LiteRTVisionClassifier.manuallyConfirmTenthPick(Champion(id = "graves", name = "Graves"))
            val result = LiteRTVisionClassifier.executeTenthPickInference(
                cropBitmap = null,
                isAlly = isAlly,
                confirmedChampionIds = emptySet(),
                confirmedPicksCount = 9,
                confirmedTargetChampion = vi
            )
            assertEquals(vi, result?.first)
            val report = LiteRTVisionClassifier.reportFlow.value
            assertEquals(vi, report.pickedChampion)
            assertTrue(report.isConfirmed)
            assertTrue(report.topCandidates.isEmpty())
            assertEquals(0, report.requiredStableFrames)
        }
    }

    @Test
    fun visibleAllyLaneBlocksVisualConfirmation() = runBlocking {
        LiteRTVisionClassifier.manuallyConfirmTenthPick(vi)
        val result = LiteRTVisionClassifier.executeTenthPickInference(
            cropBitmap = null,
            isAlly = true,
            confirmedChampionIds = emptySet(),
            confirmedPicksCount = 9,
            allowVisualConfirmation = false
        )
        assertNull(result)
        val report = LiteRTVisionClassifier.reportFlow.value
        assertNull(report.pickedChampion)
        assertFalse(report.isConfirmed)
        assertEquals(LiteRTVisionClassifier.EngineStatus.WAITING_FOR_TENTH_PICK, report.status)
    }

    @Test fun nineHudSelectionsRecoverATransientOcrGapBeforeFinalConfirmation() = runBlocking {
        val allies = (0..4).map { Champion(id = "ally$it") }
        val enemies = (0..3).map { Champion(id = "enemy$it") }
        allies.forEachIndexed { i, champion -> DraftVisionScanner.allySlotConfirmedChampions[i] = champion }
        enemies.forEachIndexed { i, champion -> DraftVisionScanner.enemySlotConfirmedChampions[i] = champion }
        DraftVisionScanner.allySlotConfirmedChampions[3] = null
        val turn = DraftPickTurn(10, false, 4)
        assertEquals(8, DraftVisionScanner.getConfirmedPicksExcept(turn).size)
        val recovered = DraftVisionScanner.getConfirmedPicksExcept(turn, allies + enemies)
        assertEquals(9, recovered.size)
        LiteRTVisionClassifier.reset()
        LiteRTVisionClassifier.manuallyConfirmTenthPick(vi)
        val recognized = LiteRTVisionClassifier.executeTenthPickInference(null, false,
            recovered.map { it.id }.toSet(), recovered.size)
        val own = enemies.map { it as Champion? } + listOf(null)
        assertEquals(4, com.example.service.screen.TenthPickHudPolicy.targetIndex(
            LiteRTVisionClassifier.reportFlow.value.isConfirmed, recognized?.first, own, allies, emptySet()))
        assertEquals(enemies, own.filterNotNull())
    }

    @Test fun finalHudCommitPreservesTheOtherNineAndManualLocksOnBothTeams() {
        val own = listOf<Champion?>(Champion(id="a"), null, Champion(id="b"), Champion(id="c"), Champion(id="d"))
        val other = (0..4).map { Champion(id="other$it") }
        val policy = com.example.service.screen.TenthPickHudPolicy
        assertEquals(1, policy.targetIndex(true, vi, own, other, emptySet(), expectedIndex=1))
        assertNull(policy.targetIndex(false, vi, own, other, emptySet()))
        assertNull(policy.targetIndex(true, vi, own, other, setOf(1)))
        assertNull(policy.targetIndex(true, vi, own, other, emptySet(), expectedIndex=4))
        assertNull(policy.targetIndex(true, other[0], own, other, emptySet()))
        assertNull(policy.targetIndex(true, vi, own, other.drop(1), emptySet()))
        assertNull(policy.targetIndex(true, vi, other, own, emptySet()))
    }

    @Test fun incompleteOrDuplicatedHudCannotInflateTheEarlierPickCount() {
        val picks = (0..7).map { Champion(id="picked$it") }
        val turn = DraftPickTurn(10, false, 4)
        assertTrue(DraftVisionScanner.getConfirmedPicksExcept(turn, picks).isEmpty())
        assertTrue(DraftVisionScanner.getConfirmedPicksExcept(turn, picks + picks.first()).isEmpty())
    }

    @Test
    fun fullChampionNamesKeepViSeparateFromViktorAndSummonerText() {
        val viktor = Champion(id = "viktor", name = "Viktor")
        val champions = listOf(vi, viktor)
        assertEquals(vi, ChampionNameResolver.findChampionInText("VI", champions))
        assertEquals(viktor, ChampionNameResolver.findChampionInText("VIKTOR", champions))
        assertNull(ChampionNameResolver.findChampionInText("Jugador 5", champions))
        assertNull(ChampionNameResolver.findChampionInText("Akashny Seijuro", champions))
    }
}
