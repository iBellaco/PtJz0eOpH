package com.example.service

import com.example.model.Champion
import com.example.model.LaneRole

/** Pure navigation decision used when the overlay has confirmed all ten picks. */
internal object DraftCompletionNavigation {
    private val slotRoles = listOf(LaneRole.TOP, LaneRole.JUNGLE, LaneRole.MID, LaneRole.ADC, LaneRole.SUPPORT)

    fun ownChampionWhenComplete(
        allies: List<Champion?>,
        enemies: List<Champion?>,
        activeRole: LaneRole
    ): Champion? {
        val picks = allies.filterNotNull() + enemies.filterNotNull()
        if (allies.size < 5 || enemies.size < 5 || picks.size != 10 || picks.map { it.id }.distinct().size != 10) return null
        val slot = slotRoles.indexOf(activeRole)
        if (slot !in allies.indices) return null
        return allies[slot]?.takeUnless { it.id.isBlank() || it.id.equals("empty", ignoreCase = true) }
    }
}
