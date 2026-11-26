package com.example.service

import com.example.model.LaneRole
import com.example.service.screen.DraftScanResult
import com.example.service.screen.TenthPickHudPolicy

internal fun OverlayState.syncScannedAllies(result: DraftScanResult): Int {
    val locked = manualLockedAllySlots.filterValues { it }.keys
    // The slot-local mapping is the source of truth. The role map can contain
    // a stale entry from an earlier frame when a player's lane title changed
    // into a champion name; never let that stale entry hide the current slot.
    val byRole = mutableMapOf<LaneRole, com.example.model.Champion>()
    for ((slot, role) in result.allyRolesBySlot) {
        result.alliesBySlot[slot]?.let { byRole.putIfAbsent(role, it) }
    }
    for ((role, champion) in result.alliesByRole) byRole.putIfAbsent(role, champion)
    val pending = result.alliesBySlot.filterKeys { it !in result.allyRolesBySlot }
        .values.distinctBy { it.id }
    val pendingIds = pending.map { it.id }.toSet()
    val current = allies.mapIndexed { index, champion ->
        champion.takeUnless { index !in locked && it?.id in pendingIds }
    }
    val next = com.example.service.screen.AllyDraftReconciler.hudAllies(
        current, byRole, locked, result.allyRolesBySlot.values.toSet() + byRole.keys)
    val assignedIds = next.filterNotNull().map { it.id }.toSet()
    val detectedIds = assignedIds + pendingIds
    val changes = next.indices.count { allies[it]?.id != next[it]?.id }
    androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
        next.forEachIndexed { index, champion -> allies[index] = champion }
        for (index in enemies.indices) {
            if (manualLockedEnemySlots[index] != true && enemies[index]?.id in detectedIds)
                enemies[index] = null
        }
        unassignedAllies.clear()
        unassignedAllies.addAll(pending.filterNot { it.id in assignedIds })
    }
    return changes
}

internal fun OverlayState.syncScannedEnemies(result: DraftScanResult): Int {
    val roles = com.example.service.screen.AllyDraftReconciler.roles
    val next = com.example.service.screen.EnemyDraftReconciler.hudEnemies(
        enemies.toList(), result.enemiesByRole, confirmedAllies, manualLockedEnemySlots.filterValues { it }.keys)
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
                expected, previousId, unassignedOwn = unassignedAllies)
                ?.let { allies[it] = champion; applied = true }
        }
        false -> TenthPickHudPolicy.targetIndex(result.isLastPickConfirmed, champion,
            enemies.toList(), allies.toList(), manualLockedEnemySlots.filterValues { it }.keys,
            replaceChampionId = previousId, unassignedOther = unassignedAllies
        )?.let { index ->
            enemies[index] = champion
            applied = true
            enemyConfidences[roles[index]] = com.example.service.screen.EnemyRoleInference.confidence(champion, roles[index])
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
        if (result.tenthPickIsAlly == false) {
            // The last portrait can change the optimal roles of all five enemies.
            // A portrait confirms identity, never 100% certainty of an enemy lane.
            val policy = com.example.service.screen.EnemyRoleInference
            val locked = manualLockedEnemySlots.filterValues { it }.keys.mapNotNull { index ->
                enemies.getOrNull(index)?.let { roles[index] to it }
            }.toMap()
            val estimated = policy.resolve(enemies.filterNotNull(), locked)
            androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
                roles.forEachIndexed { index, role ->
                    enemies[index] = estimated[role]
                    estimated[role]?.let { enemyConfidences[role] = policy.confidence(it, role) }
                }
            }
        }
        // Keep this preview replaceable until the selection screen disappears.
    }
}
