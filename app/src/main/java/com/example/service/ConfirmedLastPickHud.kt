package com.example.service

import com.example.model.LaneRole
import com.example.service.screen.DraftScanResult
import com.example.service.screen.TenthPickHudPolicy

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
