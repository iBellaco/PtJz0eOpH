package com.example

import com.example.model.Champion
import com.example.model.LaneRole
import com.example.service.screen.AllyDraftReconciler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class AllyDraftReconcilerTest {
    private val senna = Champion(id = "senna", name = "Senna", primaryRole = LaneRole.SUPPORT, secondaryRoles = listOf(LaneRole.ADC))
    private val sett = Champion(id = "sett", name = "Sett", primaryRole = LaneRole.TOP)
    private val smolder = Champion(id = "smolder", name = "Smolder", primaryRole = LaneRole.ADC)

    private fun assertUnique(roles: Map<Int, LaneRole>) {
        assertEquals((0..4).toSet(), roles.keys)
        assertEquals(AllyDraftReconciler.roles.toSet(), roles.values.toSet())
    }

    @Test
    fun screenshotKeepsDragonLaneOnUnpickedSecondSlotAndShowsSennaAndSett() {
        val visible = mapOf(1 to LaneRole.ADC, 3 to LaneRole.MID, 4 to LaneRole.JUNGLE)
        val contaminated = mapOf(0 to LaneRole.ADC, 1 to LaneRole.ADC, 2 to LaneRole.ADC)
        val champions = mapOf(0 to senna, 2 to sett)
        val remembered = AllyDraftReconciler.rememberedRoles(visible, contaminated, champions)
        assertFalse(remembered.containsKey(0))
        assertFalse(remembered.containsKey(2))
        val roles = AllyDraftReconciler.resolveRoles(champions, remembered)
        assertUnique(roles)
        assertEquals(LaneRole.SUPPORT, roles[0])
        assertEquals(LaneRole.ADC, roles[1])
        assertEquals(LaneRole.TOP, roles[2])
        val byRole = champions.map { (slot, champ) -> roles.getValue(slot) to champ }.toMap()
        val hud = AllyDraftReconciler.hudAllies(List(5) { null }, byRole, emptySet())
        assertEquals(sett, hud[0])
        assertEquals(senna, hud[4])
        assertNull(hud[3])
    }

    @Test
    fun visibleLaneElsewhereInvalidatesSelectedChampionsRememberedLane() {
        val previous = mapOf(0 to LaneRole.SUPPORT, 1 to LaneRole.JUNGLE, 2 to LaneRole.MID, 3 to LaneRole.TOP, 4 to LaneRole.ADC)
        val visible = mapOf(1 to LaneRole.JUNGLE, 2 to LaneRole.MID, 3 to LaneRole.TOP, 4 to LaneRole.SUPPORT)
        val champions = mapOf(0 to senna)
        val remembered = AllyDraftReconciler.rememberedRoles(visible, previous, champions, champions)
        assertFalse(remembered.containsKey(0))
        val resolved = AllyDraftReconciler.resolveRoles(champions, remembered, previous)
        assertUnique(resolved)
        assertEquals(LaneRole.SUPPORT, resolved[4])
        assertEquals(LaneRole.ADC, resolved[0])
    }

    @Test
    fun supportCanMoveFromLastToFirstBeforeSelection() {
        val previous = AllyDraftReconciler.roles.mapIndexed { index, role -> index to role }.toMap()
        val visible = mapOf(0 to LaneRole.SUPPORT, 4 to LaneRole.TOP)
        val remembered = AllyDraftReconciler.rememberedRoles(visible, previous)
        val resolved = AllyDraftReconciler.resolveRoles(emptyMap(), remembered, previous)
        assertUnique(resolved)
        assertEquals(LaneRole.SUPPORT, resolved[0])
        assertEquals(LaneRole.TOP, resolved[4])
    }

    @Test
    fun selectedChampionsCarryRememberedLanesWhenTheyExchangeScreenPositions() {
        val previous = mapOf(0 to sett, 4 to senna)
        val current = mapOf(0 to senna, 4 to sett)
        val remembered = AllyDraftReconciler.rememberedRoles(
            emptyMap(), mapOf(0 to LaneRole.TOP, 4 to LaneRole.SUPPORT), current, previous
        )
        assertEquals(mapOf(0 to LaneRole.SUPPORT, 4 to LaneRole.TOP), remembered)
        assertUnique(AllyDraftReconciler.resolveRoles(current, remembered))
    }

    @Test
    fun missingTextPreservesEvidenceWithoutReservingALaneTwice() {
        val previous = mapOf(0 to LaneRole.SUPPORT, 1 to LaneRole.ADC)
        val champions = mapOf(0 to senna, 1 to smolder)
        assertEquals(previous, AllyDraftReconciler.rememberedRoles(emptyMap(), previous, champions, champions))
        val duplicate = mapOf(0 to LaneRole.ADC, 1 to LaneRole.ADC)
        val remembered = AllyDraftReconciler.rememberedRoles(duplicate, previous, champions, champions)
        assertEquals(emptyMap<Int, LaneRole>(), remembered)
        assertUnique(AllyDraftReconciler.resolveRoles(champions, remembered))
    }

    @Test
    fun changingChampionInTheSameSlotDoesNotEraseItsObservedLane() {
        val remembered = AllyDraftReconciler.rememberedRoles(
            emptyMap(), mapOf(0 to LaneRole.SUPPORT), mapOf(0 to smolder), mapOf(0 to senna)
        )
        assertEquals(mapOf(0 to LaneRole.SUPPORT), remembered)
    }

    @Test
    fun hudMovesBothChampionsAndClearsPreviouslyOccupiedRows() {
        val old = listOf(senna, null, null, sett, null)
        val next = AllyDraftReconciler.hudAllies(old, mapOf(LaneRole.TOP to sett, LaneRole.SUPPORT to senna), emptySet())
        assertEquals(listOf(sett, null, null, null, senna), next)
    }

    @Test
    fun manualPickIsPreservedAndCannotBeDuplicatedByAutomaticAssignment() {
        val current = listOf(senna, null, null, null, null)
        val next = AllyDraftReconciler.hudAllies(current, mapOf(LaneRole.TOP to sett, LaneRole.SUPPORT to senna), setOf(0))
        assertEquals(current, next)
    }

    @Test
    fun everyVisibleRolePermutationIsRespected() {
        fun check(prefix: List<LaneRole>, remaining: List<LaneRole>) {
            if (remaining.isEmpty()) {
                val visible = prefix.mapIndexed { index, role -> index to role }.toMap()
                val resolved = AllyDraftReconciler.resolveRoles(mapOf(0 to senna, 1 to sett), visible)
                assertEquals(visible, resolved)
                assertUnique(resolved)
            } else {
                for (role in remaining) check(prefix + role, remaining - role)
            }
        }
        check(emptyList(), AllyDraftReconciler.roles)
    }
}
