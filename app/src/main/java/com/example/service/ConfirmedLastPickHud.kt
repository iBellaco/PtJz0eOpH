package com.example.service

import com.example.model.LaneRole
import com.example.service.screen.DraftScanResult
import com.example.service.screen.TenthPickHudPolicy

internal fun OverlayState.syncScannedEnemies(result: DraftScanResult): Int {
    val roles = com.example.service.screen.AllyDraftReconciler.roles
    val next = com.example.service.screen.EnemyDraftReconciler.hudEnemies(
        enemies.toList(), result.enemiesByRole, allies.toList(), manualLockedEnemySlots.filterValues { it }.keys)
    val changes = next.indices.count { enemies[it]?.id != next[it]?.id }
    androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
        next.forEachIndexed { index, champion ->
            enemies[index] = champion
            if (champion == null) enemyConfidences.remove(roles[index])
            else if (manualLockedEnemySlots[index] != true)
                enemyConfidences[roles[index]] = result.enemyConfidencesByRole[roles[index]] ?: 85
        }
    }
    return changes
}

/** Apply the final confirmed portrait to the same state displayed by the overlay. */
internal fun OverlayState.applyConfirmedLastPick(result: DraftScanResult) {
    val champion = result.lastPickChampion ?: return
    val roles = listOf(LaneRole.TOP, LaneRole.JUNGLE, LaneRole.MID, LaneRole.ADC, LaneRole.SUPPORT)
    when (result.tenthPickIsAlly) {
        true -> {
            val role = result.tenthPickSlotIndex?.let { result.allyRolesBySlot[it] } ?: return
            val expected = roles.indexOf(role).takeIf { it >= 0 } ?: return
            TenthPickHudPolicy.targetIndex(result.isLastPickConfirmed, champion,
                allies.toList(), enemies.toList(), manualLockedAllySlots.filterValues { it }.keys,
                expected)?.let { allies[it] = champion }
        }
        false -> TenthPickHudPolicy.targetIndex(result.isLastPickConfirmed, champion,
            enemies.toList(), allies.toList(), manualLockedEnemySlots.filterValues { it }.keys
        )?.let { index ->
            enemies[index] = champion
            enemyConfidences[roles[index]] = 100
        }
        null -> Unit
    }
}
