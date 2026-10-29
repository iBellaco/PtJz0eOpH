package com.example.service.screen

import com.example.model.Champion
import com.example.model.LaneRole

/** Hidden enemy lanes are estimates. Evaluate the whole composition, never the pick-row order. */
object EnemyRoleInference {
    val roles = listOf(LaneRole.TOP, LaneRole.JUNGLE, LaneRole.MID, LaneRole.ADC, LaneRole.SUPPORT)
    // Conservative solo-lane alternatives when the catalog's primary/flex roles
    // cannot cover a full composition. Do not treat every ADC as a Baron laner.
    private val soloLaneCarries = setOf("tristana", "vayne", "lucian", "akshan", "kalista")

    fun affinity(champion: Champion, role: LaneRole): Int = when {
        role == champion.primaryRole -> 120
        role in champion.secondaryRoles -> 100
        // A ranged carry can occupy a solo lane, but is not evidence of a jungler/support.
        role == LaneRole.TOP && champion.id in soloLaneCarries -> 25
        role == LaneRole.JUNGLE -> -300
        role == LaneRole.SUPPORT -> -200
        else -> -100
    }

    fun resolve(champions: List<Champion>, locked: Map<LaneRole, Champion> = emptyMap()): Map<LaneRole, Champion> {
        val fixed = locked.filterKeys { it in roles }
        val fixedIds = fixed.values.map { it.id }.toSet()
        val pending = champions.distinctBy { it.id }.filter { it.id !in fixedIds }
            .sortedBy { it.id }.take(roles.size - fixed.size)
        var best = fixed
        var bestScore = Int.MIN_VALUE
        fun visit(index: Int, remaining: List<LaneRole>, assignment: Map<LaneRole, Champion>, score: Int) {
            if (index == pending.size) {
                if (score > bestScore) { bestScore = score; best = assignment }
                return
            }
            val champion = pending[index]
            for (role in remaining) visit(index + 1, remaining - role,
                assignment + (role to champion), score + affinity(champion, role))
        }
        // At most 5! assignments; deterministic across partial scans and champion swaps.
        visit(0, roles - fixed.keys, fixed, 0)
        return best
    }

    fun confidence(champion: Champion, role: LaneRole): Int = when {
        role == champion.primaryRole -> 90
        role in champion.secondaryRoles -> 75
        else -> 40
    }
}
