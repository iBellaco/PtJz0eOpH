package com.example.service.screen

import com.example.model.Champion
import com.example.model.LaneRole
import com.example.model.DraftSlot

/** Apply one complete scan atomically, preserving manual choices without stale role rows. */
object EnemyDraftReconciler {
    fun hudSlots(current: List<Champion?>, confidences: Map<LaneRole, Int>,
        names: Map<Int, String> = emptyMap(), spells: Map<Int, List<String>> = emptyMap()): List<DraftSlot> =
        current.mapIndexedNotNull { index, champion ->
            champion?.let {
                val role = EnemyRoleInference.roles[index]
                DraftSlot(champion = it, assignedRole = role,
                    confidence = confidences[role] ?: EnemyRoleInference.confidence(it, role),
                    summonerName = names[index], spells = spells[index].orEmpty())
            }
        }

    fun hudEnemies(current: List<Champion?>, scanned: Map<LaneRole, Champion>,
        allies: List<Champion?>, lockedIndices: Set<Int>): List<Champion?> {
        val forbidden = allies.filterNotNull().map { it.id }.toMutableSet()
        val locked = lockedIndices.mapNotNull { current.getOrNull(it)?.id }.toSet()
        val used = (forbidden + locked).toMutableSet()
        val resolved = if (lockedIndices.isEmpty()) scanned else EnemyRoleInference.resolve(
            scanned.values.filter { it.id !in forbidden },
            lockedIndices.mapNotNull { index -> current.getOrNull(index)?.let {
                AllyDraftReconciler.roles[index] to it
            } }.toMap())
        return AllyDraftReconciler.roles.mapIndexed { index, role ->
            if (index in lockedIndices) current.getOrNull(index)
            else resolved[role]?.takeIf { used.add(it.id) }
        }
    }
}
