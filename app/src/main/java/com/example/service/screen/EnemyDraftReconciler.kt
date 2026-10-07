package com.example.service.screen

import com.example.model.Champion
import com.example.model.LaneRole

/** Apply one complete scan atomically, preserving manual choices without stale role rows. */
object EnemyDraftReconciler {
    fun hudEnemies(current: List<Champion?>, scanned: Map<LaneRole, Champion>,
        allies: List<Champion?>, lockedIndices: Set<Int>): List<Champion?> {
        val forbidden = allies.filterNotNull().map { it.id }.toMutableSet()
        val locked = lockedIndices.mapNotNull { current.getOrNull(it)?.id }.toSet()
        val used = (forbidden + locked).toMutableSet()
        return AllyDraftReconciler.roles.mapIndexed { index, role ->
            if (index in lockedIndices) current.getOrNull(index)
            else scanned[role]?.takeIf { used.add(it.id) }
        }
    }
}
