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
    val previousId = trackedLastPick?.takeIf { it.first == result.tenthPickIsAlly }?.second
    var applied = false
    val roles = listOf(LaneRole.TOP, LaneRole.JUNGLE, LaneRole.MID, LaneRole.ADC, LaneRole.SUPPORT)
    when (result.tenthPickIsAlly) {
        true -> {
            // La detección del retrato puede ser válida aunque el OCR no haya
            // asociado una línea al slot. En ese caso, conserva los nueve picks
            // y coloca el último únicamente en el hueco aliado libre.
            val expected = result.tenthPickSlotIndex?.let { result.allyRolesBySlot[it] }
                ?.let(roles::indexOf)?.takeIf { it >= 0 }
            TenthPickHudPolicy.targetIndex(result.isLastPickConfirmed, champion,
                allies.toList(), enemies.toList(), manualLockedAllySlots.filterValues { it }.keys,
                expected, previousId)?.let { allies[it] = champion; applied = true }
        }
        false -> TenthPickHudPolicy.targetIndex(result.isLastPickConfirmed, champion,
            enemies.toList(), allies.toList(), manualLockedEnemySlots.filterValues { it }.keys,
            replaceChampionId = previousId
        )?.let { index ->
            enemies[index] = champion
            applied = true
            enemyConfidences[roles[index]] = 100
        }
        null -> Unit
    }
    val ownTeam = if (result.tenthPickIsAlly == true) allies else enemies
    val scannedTarget = if (result.tenthPickIsAlly == true) result.alliesBySlot[result.tenthPickSlotIndex]
        else result.enemiesBySlot[result.tenthPickSlotIndex]
    if (result.isLastPickConfirmed && result.tenthPickIsAlly != null &&
        (applied || previousId == champion.id || scannedTarget?.id == champion.id) &&
        ownTeam.any { it?.id == champion.id }) {
        trackedLastPick = result.tenthPickIsAlly to champion.id
    }
}
