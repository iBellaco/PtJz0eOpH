package com.example

import android.app.Application
import com.example.data.WildRiftRepository
import com.example.model.LaneRole
import com.example.service.OverlayState
import com.example.service.applyConfirmedLastPick
import com.example.service.screen.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class EnemyRoleInferenceTest {
    @Before fun prepare() { WildRiftRepository.initChampions(RuntimeEnvironment.getApplication(), forceReload = true) }
    private fun champion(id: String) = WildRiftRepository.champions.single { it.id == id }
    private fun permutations(ids: List<String>): List<List<String>> = if (ids.isEmpty()) listOf(emptyList())
        else ids.flatMap { id -> permutations(ids - id).map { listOf(id) + it } }

    @Test fun yoneYiMelNamiCompositionIsIndependentOfAll120PickOrders() {
        val expected = mapOf(LaneRole.TOP to "yone", LaneRole.JUNGLE to "master_yi", LaneRole.MID to "mel",
            LaneRole.ADC to "jinx", LaneRole.SUPPORT to "nami")
        // The unreported fifth champion is explicitly a test fixture, not claimed screenshot evidence.
        for (order in permutations(expected.values.toList())) {
            val slots = order.mapIndexed { index, id -> ScannedSlotInfo(index, false).apply { this.champion = champion(id) } }
            val result = DraftValidationLayer.resolveTeamRolesDetailed(slots, WildRiftRepository.champions,
                mutableListOf(), isAllyTeam = false)
            assertEquals(order.toString(), expected, result.assignments.mapValues { it.value.id })
            assertTrue(result.confidences.values.all { it < 100 })
        }
    }

    @Test fun compatibleJunglerWinsOverOffRoleAdcInAll120Orders() {
        val expected = mapOf(LaneRole.TOP to "tristana", LaneRole.JUNGLE to "pantheon", LaneRole.MID to "ahri",
            LaneRole.ADC to "jinx", LaneRole.SUPPORT to "nami")
        for (order in permutations(expected.values.toList())) {
            val result = EnemyRoleInference.resolve(order.map(::champion))
            assertEquals(expected, result.mapValues { it.value.id })
            assertEquals(40, EnemyRoleInference.confidence(champion("tristana"), LaneRole.TOP))
        }
    }

    @Test fun renderingPreservesSolvedAndManualRolesWithoutCompressingVacantRows() {
        val team = listOf(champion("yone"), null, champion("mel"), champion("jinx"), champion("nami"))
        val slots = EnemyDraftReconciler.hudSlots(team, mapOf(LaneRole.TOP to 75), mapOf(2 to "mid-player"))
        assertEquals(listOf(LaneRole.TOP, LaneRole.MID, LaneRole.ADC, LaneRole.SUPPORT), slots.map { it.assignedRole })
        assertEquals("yone", slots.first().champion.id)
        assertEquals(75, slots.first().confidence)
        assertEquals("mid-player", slots[1].summonerName)
    }

    @Test fun tenthPortraitRebalancesEnemyLanesWithoutChangingIdentitiesOrManualChoices() {
        val state = OverlayState()
        val ids = listOf("master_yi", "mel", "jinx", "nami")
        val initial = EnemyRoleInference.resolve(ids.map(::champion))
        EnemyRoleInference.roles.forEachIndexed { index, role -> state.enemies[index] = initial[role] }
        listOf("darius", "vi", "ahri", "caitlyn", "lulu").forEachIndexed { index, id -> state.allies[index] = champion(id) }
        state.manualLockedEnemySlots[3] = true
        state.applyConfirmedLastPick(DraftScanResult(emptyList(), emptyList(), isSuccessful = true, statusMessage = "",
            lastPickChampion = champion("yone"), isLastPickConfirmed = true, tenthPickIsAlly = false, tenthPickSlotIndex = 4))
        assertEquals(listOf("yone", "master_yi", "mel", "jinx", "nami"), state.enemies.map { it?.id })
        assertTrue(state.enemyConfidences.values.all { it < 100 })
        state.applyConfirmedLastPick(DraftScanResult(emptyList(), emptyList(), isSuccessful = true, statusMessage = "",
            lastPickChampion = champion("gwen"), isLastPickConfirmed = true, tenthPickIsAlly = false, tenthPickSlotIndex = 4))
        assertEquals(listOf("gwen", "master_yi", "mel", "jinx", "nami"), state.enemies.map { it?.id })
    }

    @Test fun manualLaneLocksSurviveReconciliationWithoutDroppingOtherPicks() {
        val current = listOf(champion("tristana"), null, null, null, null)
        val ids = listOf("tristana", "pantheon", "ahri", "jinx", "nami")
        val result = EnemyDraftReconciler.hudEnemies(current, EnemyRoleInference.resolve(ids.map(::champion)),
            emptyList(), setOf(0))
        assertEquals(ids, result.map { it?.id })
    }

    @Test fun emptyEvidenceIsNotRivalsFirstAndManualChoiceRemainsAuthoritative() {
        val state = OverlayState()
        assertFalse(state.isFirstPickKnown)
        state.observeFirstPick(null)
        assertFalse(state.isFirstPickKnown)
        state.observeFirstPick(DraftPickOrderPolicy.inferFirstPick(1, 0))
        assertTrue(state.isFirstPickKnown)
        assertTrue(state.isFirstPick)
        state.isFirstPickManuallySelected = true
        state.isFirstPick = false
        state.observeFirstPick(true)
        assertFalse(state.isFirstPick)
    }
}
