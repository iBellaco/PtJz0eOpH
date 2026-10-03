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
