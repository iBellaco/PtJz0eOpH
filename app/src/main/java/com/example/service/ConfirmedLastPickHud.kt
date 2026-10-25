package com.example.service

import com.example.model.LaneRole
import com.example.service.screen.DraftScanResult
import com.example.service.screen.TenthPickHudPolicy

internal fun OverlayState.syncScannedAllies(result: DraftScanResult): Int {
    val next = com.example.service.screen.AllyDraftReconciler.hudAllies(
        allies.toList(), result.alliesByRole, manualLockedAllySlots.filterValues { it }.keys,
        result.allyRolesBySlot.values.toSet())
        .toMutableList()

    // The draft rows are authoritative even when only some lane labels were
    // readable in the same frame. This is common in the release OCR path:
    // champion titles are visible, while a mastery badge or the viewer mask
    // can hide one or more lane labels. Do not discard those slot identities
    // merely because the role map is incomplete. Known roles keep their normal
    // HUD position; unresolved slots fill the remaining positions in the
    // stable Wild Rift row order.
    val usedIds = next.filterNotNull().mapTo(mutableSetOf()) { it.id }
    val unresolvedSlots = result.alliesBySlot.entries
        .sortedBy { it.key }
        .filter { it.value.id !in usedIds }
    for ((slot, champion) in unresolvedSlots) {
        val roleIndex = result.allyRolesBySlot[slot]
            ?.let(com.example.service.screen.AllyDraftReconciler.roles::indexOf)
            ?.takeIf { it >= 0 }
        val target = roleIndex?.takeIf { it !in manualLockedAllySlots && next[it] == null }
            ?: next.indices.firstOrNull { it !in manualLockedAllySlots && next[it] == null }
        if (target != null) {
            next[target] = champion
            usedIds += champion.id
        }
    }
    val changes = next.indices.count { allies[it]?.id != next[it]?.id }
    androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
        next.forEachIndexed { index, champion -> allies[index] = champion }
        for (index in enemies.indices) {
            if (manualLockedEnemySlots[index] != true && next.any { it != null && it.id == enemies[index]?.id })
                enemies[index] = null
        }
        unassignedAllies.clear()
        unassignedAllies.addAll(result.alliesBySlot.filterKeys { it !in result.allyRolesBySlot }.values.distinctBy { it.id })
    }
    return changes
}

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
    // Once the tenth pick closed this scan, late classifier reports belong to
    // the completed frame sequence and must not replace the saved champion.
    if (completedDraftLocked) return
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
        stopAutomaticScanIfComplete(result.isLastPickConfirmed)
    }
}
