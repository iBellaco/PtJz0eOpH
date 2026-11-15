package com.example.service.screen

import com.example.model.Champion
import com.example.model.LaneRole
import kotlin.math.roundToInt

/** Hidden enemy lanes are estimates. Evaluate the whole composition, never the pick-row order. */
object EnemyRoleInference {
    val roles = listOf(LaneRole.TOP, LaneRole.JUNGLE, LaneRole.MID, LaneRole.ADC, LaneRole.SUPPORT)
    // Conservative solo-lane alternatives when the catalog's primary/flex roles
    // cannot cover a full composition. Do not treat every ADC as a Baron laner.
    private val soloLaneCarries = setOf("tristana", "vayne", "lucian", "akshan", "kalista")

    private fun Champion.statsFor(role: LaneRole) = laneStats[role.name]
        ?: laneStats.entries.firstOrNull { it.key.equals(role.name, ignoreCase = true) }?.value

    /**
     * The row on the right side of the draft is a pick order, not a lane.
     * Prefer the champion's natural/flex roles and use published lane rates as
     * a tie breaker. A tiny off-role rate must never beat a real primary role.
     */
    fun affinity(champion: Champion, role: LaneRole): Int {
        val primary = role == champion.primaryRole
        val secondary = role in champion.secondaryRoles
        val stats = champion.statsFor(role)
        val published = if (stats != null && stats.pickRate.isFinite() && stats.winRate.isFinite()) {
            // Pick rate is useful evidence, but not enough to turn a rare flex
            // appearance into the champion's lane.
            (stats.pickRate.coerceIn(0.0, 100.0) * 2.5 +
                (stats.winRate.coerceIn(0.0, 100.0) - 50.0) * 1.5).roundToInt()
        } else 0
        val roleBase = when {
            primary -> 120
            // Yasuo/other melee flexes are legal in Dragon lane, but without
            // a published ADC rate the engine should prefer a real solo lane
            // over inventing an off-role carry just to fill a partial draft.
            secondary && role == LaneRole.ADC -> 70
            secondary -> 96
            // A ranged carry can occupy a solo lane, but is not evidence of a
            // jungler/support. Keep this fallback for incomplete catalog data.
            role == LaneRole.TOP && champion.id in soloLaneCarries -> 25
            role == LaneRole.JUNGLE -> -300
            role == LaneRole.SUPPORT -> -200
            else -> -100
        }
        return roleBase + published
    }

    private fun naturalOptions(champion: Champion): Set<LaneRole> = buildSet {
        add(champion.primaryRole)
        addAll(champion.secondaryRoles)
        if (champion.id in soloLaneCarries) add(LaneRole.TOP)
    }

    /**
     * Composition bonuses keep the least-flexible champion on its primary
     * lane and move a champion with more valid alternatives when two picks
     * share a primary lane. This is what prevents Lux/Yasuo/Fizz from being
     * reported as three mid laners: Lux is the only support candidate, Fizz
     * keeps mid, and Yasuo flexes to top.
     */
    private fun compositionScore(assignment: Map<LaneRole, Champion>, roster: List<Champion>): Int {
        var score = 0
        assignment.forEach { (role, champion) ->
            val options = naturalOptions(champion)
            val candidateCount = roster.count { role in naturalOptions(it) }
            score += when (candidateCount) {
                1 -> 36 // protect a role that only one observed champion can fill
                2 -> 22
                else -> 0
            }
            if (role != champion.primaryRole && role in champion.secondaryRoles) {
                // When several picks can move, use the one with more choices
                // as the flex and preserve the narrower champion's primary.
                score += (options.size - 1) * 8
            }
        }

        val byPrimary = roster.groupBy { it.primaryRole }
        byPrimary.forEach { (role, primaries) ->
            if (primaries.size < 2) return@forEach
            val assigned = assignment[role] ?: return@forEach
            if (assigned.primaryRole == role) {
                val maxAlternativeCount = primaries.filter { it.id != assigned.id }
                    .maxOfOrNull { naturalOptions(it).size } ?: naturalOptions(assigned).size
                score += ((maxAlternativeCount - naturalOptions(assigned).size).coerceAtLeast(0)) * 28
            }
        }
        return score
    }

    fun resolve(champions: List<Champion>, locked: Map<LaneRole, Champion> = emptyMap()): Map<LaneRole, Champion> {
        val fixed = locked.filterKeys { it in roles }
        val fixedIds = fixed.values.map { it.id }.toSet()
        val pending = champions.distinctBy { it.id }.filter { it.id !in fixedIds }
            .sortedBy { it.id }.take(roles.size - fixed.size)
        var best = fixed
        var bestScore = Int.MIN_VALUE
        val roster = (pending + fixed.values).distinctBy { it.id }
        fun visit(index: Int, remaining: List<LaneRole>, assignment: Map<LaneRole, Champion>, score: Int) {
            if (index == pending.size) {
                val total = score + compositionScore(assignment, roster)
                if (total > bestScore) { bestScore = total; best = assignment }
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

    fun confidence(champion: Champion, role: LaneRole): Int {
        val stats = champion.statsFor(role)
        val hasPublishedRate = stats != null && stats.pickRate.isFinite() && stats.pickRate > 0.0
        return when {
            role == champion.primaryRole && hasPublishedRate -> 94
            role == champion.primaryRole -> 90
            role in champion.secondaryRoles && hasPublishedRate -> 82
            role in champion.secondaryRoles -> 75
            else -> 40
        }
    }
}
