package com.example.service.screen

import com.example.model.Champion
import com.example.model.LaneRole

/** Keeps screen positions separate from the five role rows in the HUD. */
object AllyDraftReconciler {
    val roles = listOf(LaneRole.TOP, LaneRole.JUNGLE, LaneRole.MID, LaneRole.ADC, LaneRole.SUPPORT)

    fun rememberedRoles(
        visibleLanes: Map<Int, LaneRole>,
        previousLanes: Map<Int, LaneRole>,
        champions: Map<Int, Champion> = emptyMap(),
        previousChampions: Map<Int, Champion> = emptyMap()
    ): Map<Int, LaneRole> {
        // Conflicting OCR readings are ambiguous; never reserve one lane twice.
        val visible = visibleLanes.filterKeys { it in 0..4 }
        val counts = visible.values.groupingBy { it }.eachCount()
        val resolved = visible.filterValues { counts[it] == 1 }.toMutableMap()
        val proposals = mutableMapOf<Int, LaneRole>()
        for (slot in 0..4) {
            if (slot in visible) continue
            // A lane belongs to the player's slot, including when champions are
            // exchanged. Champion identity cannot move or replace that evidence.
            val role = previousLanes[slot]
            if (role != null && role !in visible.values) proposals[slot] = role
        }
        val rememberedCounts = proposals.values.groupingBy { it }.eachCount()
        resolved.putAll(proposals.filterValues { rememberedCounts[it] == 1 })
        return resolved.toMap()
    }

    fun observedRoles(visible: Map<Int, LaneRole>, previous: Map<Int, LaneRole>): Map<Int, LaneRole> {
        val known = rememberedRoles(visible, previous)
        // Four distinct observed lanes determine the remaining slot uniquely.
        return if (known.size == 4) known + ((0..4).single { it !in known } to roles.single { it !in known.values }) else known
    }

    fun resolveRoles(
        champions: Map<Int, Champion>,
        remembered: Map<Int, LaneRole>,
        previousAssignments: Map<Int, LaneRole> = emptyMap(),
        smiteSlots: Set<Int> = emptySet()
    ): Map<Int, LaneRole> {
        val fixed = rememberedRoles(remembered, emptyMap())
        val slots = (0..4).filterNot { it in fixed }
        val available = roles.filterNot { it in fixed.values }
        var best = fixed
        var bestScore = Int.MIN_VALUE
        fun visit(index: Int, remaining: List<LaneRole>, assignment: Map<Int, LaneRole>, score: Int) {
            if (index == slots.size) {
                if (score > bestScore) {
                    bestScore = score
                    best = assignment
                }
                return
            }
            val slot = slots[index]
            val champ = champions[slot]
            for (role in remaining) {
                val affinity = when {
                    champ == null -> 0
                    champ.primaryRole == role -> 100
                    role in champ.secondaryRoles -> 50
                    else -> 0
                }
                val smite = if (slot in smiteSlots && role == LaneRole.JUNGLE) 200 else 0
                val continuity = if (previousAssignments[slot] == role) 1 else 0
                visit(index + 1, remaining - role, assignment + (slot to role), score + affinity + smite + continuity)
            }
        }
        visit(0, available, fixed, 0)
        return best
    }

    fun hudAllies(
        current: List<Champion?>,
        scannedByRole: Map<LaneRole, Champion>,
        lockedIndices: Set<Int>,
        observedRoles: Set<LaneRole> = roles.toSet()
    ): List<Champion?> {
        val lockedIds = lockedIndices.mapNotNull { current.getOrNull(it)?.id }.toSet()
        val used = lockedIds.toMutableSet()
        return roles.mapIndexed { index, role ->
            if (index in lockedIndices) current.getOrNull(index)
            else if (role in observedRoles) scannedByRole[role]?.takeIf { used.add(it.id) }
            else current.getOrNull(index)?.takeIf { it.id !in scannedByRole.values.map { c -> c.id } && used.add(it.id) }
        }
    }
}
