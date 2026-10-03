package com.example.data

import com.example.model.*
import kotlin.math.abs
import kotlin.math.floor

enum class MatchupRelation { FAVORABLE, UNFAVORABLE, VARIABLE, UNKNOWN }

/** Both directions use the same knowledge. Conflicting claims cannot become a guaranteed counter. */
object MatchupKnowledge {
    private val names = java.util.concurrent.ConcurrentHashMap<String, String>()
    private val alphabet = Regex("[^a-z0-9]")
    private fun normalized(value: String): String = names.computeIfAbsent(value) { java.text.Normalizer.normalize(it, java.text.Normalizer.Form.NFD)
        .lowercase(java.util.Locale.ROOT).replace(alphabet, "") }
    fun matches(value: String, champion: Champion) = normalized(value) in setOf(normalized(champion.id), normalized(champion.name))
    fun relation(own: Champion, rival: Champion, role: LaneRole = own.primaryRole): MatchupRelation {
        if (own.id == rival.id) return MatchupRelation.UNKNOWN
        // Catalog relationships are the same complete lists rendered in the champion sheet.
        // A displayed weakness always penalizes that pick, including conflicting source opinions.
        if (WildRiftRepository.getBaseChampion(own.id) != null && WildRiftRepository.getBaseChampion(rival.id) != null) {
            val known = com.example.util.ChampionRoleMatchupAdvisor.getMatchups(own, role)
            if (known.counters.any { matches(it, rival) }) return MatchupRelation.UNFAVORABLE
            if (known.advantages.any { matches(it, rival) }) return MatchupRelation.FAVORABLE
        }
        val positive = own.advantageAgainst.any { matches(it, rival) } || rival.counteredBy.any { matches(it, own) }
        val negative = own.counteredBy.any { matches(it, rival) } || rival.advantageAgainst.any { matches(it, own) }
        return when { positive && negative -> MatchupRelation.VARIABLE; negative -> MatchupRelation.UNFAVORABLE; positive -> MatchupRelation.FAVORABLE; else -> MatchupRelation.UNKNOWN }
    }
    fun related(own: Champion, roster: List<Champion>, relation: MatchupRelation): List<Champion> =
        roster.filter { it.primaryRole == own.primaryRole || own.primaryRole in it.secondaryRoles }
            .distinctBy { it.id }.filter { MatchupKnowledge.relation(own, it) == relation }
    fun synergies(own: Champion, allies: List<Champion>) = allies.distinctBy { it.id }.filter { ally ->
        own.synergies.any { matches(it, ally) } || ally.synergies.any { matches(it, own) }
    }
}

data class DamageProfile(val physical: Double, val magic: Double, val trueDamage: Double) {
    init { require(physical >= 0 && magic >= 0 && trueDamage >= 0 && abs(physical + magic + trueDamage - 100) < 0.01) }
}

/** Ability/build archetypes guide composition; they are not a measurement of damage dealt in a match. */
object DraftDamagePolicy {
    private val mixed = mapOf(
        "corki" to DamageProfile(35.0,65.0,0.0), "kaisa" to DamageProfile(50.0,50.0,0.0),
        "varus" to DamageProfile(65.0,35.0,0.0), "ezreal" to DamageProfile(75.0,25.0,0.0),
        "jax" to DamageProfile(60.0,40.0,0.0), "warwick" to DamageProfile(40.0,60.0,0.0),
        "shyvana" to DamageProfile(45.0,55.0,0.0), "yone" to DamageProfile(70.0,30.0,0.0),
        "irelia" to DamageProfile(80.0,20.0,0.0), "teemo" to DamageProfile(20.0,80.0,0.0),
        "vayne" to DamageProfile(75.0,0.0,25.0), "fiora" to DamageProfile(75.0,0.0,25.0),
        "camille" to DamageProfile(65.0,0.0,35.0), "masteryi" to DamageProfile(85.0,0.0,15.0),
        "sett" to DamageProfile(85.0,0.0,15.0), "gwen" to DamageProfile(5.0,85.0,10.0),
        "darius" to DamageProfile(85.0,0.0,15.0), "garen" to DamageProfile(90.0,0.0,10.0),
        "olaf" to DamageProfile(90.0,0.0,10.0), "ahri" to DamageProfile(5.0,85.0,10.0),
        "lillia" to DamageProfile(5.0,85.0,10.0), "velkoz" to DamageProfile(5.0,75.0,20.0),
        "smolder" to DamageProfile(75.0,10.0,15.0)
    )
    fun profile(champion: Champion): DamageProfile {
        mixed[champion.id.lowercase().replace("_", "").replace("-", "")]?.let { return it }
        return when (champion.damageType) {
            DamageType.PHYSICAL -> DamageProfile(100.0,0.0,0.0)
            DamageType.MAGIC -> DamageProfile(0.0,100.0,0.0)
            DamageType.TRUE_HYBRID -> if (champion.skills.any { it.description.contains("daño verdadero", true) || it.descriptionPt.contains("dano verdadeiro", true) }) DamageProfile(40.0,40.0,20.0) else DamageProfile(50.0,50.0,0.0)
        }
    }
    fun composition(champions: List<Champion>): List<Int> {
        val active = champions.filter { it.id != "empty" }.distinctBy { it.id }
        if (active.isEmpty()) return listOf(0,0,0)
        val profiles = active.map(::profile)
        val values = listOf(profiles.sumOf { it.physical }, profiles.sumOf { it.magic }, profiles.sumOf { it.trueDamage }).map { it / profiles.size }
        val rounded = values.map { floor(it).toInt() }.toMutableList()
        values.indices.sortedByDescending { values[it] - rounded[it] }.take(100-rounded.sum()).forEach { rounded[it]++ }
        return rounded
    }
    fun balanceGain(champion: Champion, allies: List<Champion>): Double {
        if (allies.isEmpty()) return 0.0
        val before = composition(allies); val after = composition(allies + champion)
        return ((abs(before[0]-before[1]) - abs(after[0]-after[1])) / 12.0).coerceIn(-3.0,4.0)
    }
}

object DraftScoringPolicy {
    fun score(champion: Champion, role: LaneRole, allies: List<Champion>, enemies: List<Champion>,
        opponent: Champion?, firstPick: Boolean, roster: List<Champion>): Double {
        val activeAllies = allies.filter { it.id != "empty" && it.id != champion.id }.distinctBy { it.id }
        val activeEnemies = enemies.filter { it.id != "empty" }.distinctBy { it.id }
        var score = 50.0 + (champion.winrate.takeIf { it.isFinite() }?.coerceIn(0.0,100.0)?.minus(50.0) ?: 0.0) * 0.5
        score += when { champion.primaryRole == role -> 3.0; role in champion.secondaryRoles -> 1.0; else -> -18.0 }
        score += when (champion.tier) { "S+" -> 2.0; "S" -> 1.2; "A+" -> 0.7; "A" -> 0.3; else -> 0.0 }
        val lane = opponent?.takeIf { it.id != "empty" }
        score += when (lane?.let { MatchupKnowledge.relation(champion,it,role) }) {
            MatchupRelation.FAVORABLE -> 12.0; MatchupRelation.UNFAVORABLE -> -14.0; MatchupRelation.VARIABLE -> -2.0; else -> 0.0
        }
        val others = activeEnemies.filter { it.id != lane?.id }
        score += others.count { MatchupKnowledge.relation(champion,it,role) == MatchupRelation.FAVORABLE } * 1.5
        score -= others.count { MatchupKnowledge.relation(champion,it,role) == MatchupRelation.UNFAVORABLE } * 2.0
        score += (MatchupKnowledge.synergies(champion,activeAllies).size * 1.8).coerceAtMost(5.0)
        score += DraftDamagePolicy.balanceGain(champion,activeAllies)
        if (activeAllies.isNotEmpty() && activeAllies.none { it.isFrontline } && champion.isFrontline) score += 2.0
        if (activeEnemies.count { it.isFrontline } >= 2) score += (DraftDamagePolicy.profile(champion).trueDamage / 10).coerceAtMost(3.0)
        if (firstPick && activeEnemies.isEmpty() && lane == null) {
            val possible = roster.filter { it.primaryRole == role || role in it.secondaryRoles }.distinctBy { it.id }
            val losses = possible.count { MatchupKnowledge.relation(champion,it,role) == MatchupRelation.UNFAVORABLE }
            val wins = possible.count { MatchupKnowledge.relation(champion,it,role) == MatchupRelation.FAVORABLE }
            score += ((wins-losses).toDouble() / (wins+losses).coerceAtLeast(4) * 2).coerceIn(-2.0,2.0)
        }
        return (kotlin.math.round(score.coerceIn(0.0,100.0) * 10) / 10)
    }
}
