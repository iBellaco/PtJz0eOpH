package com.example.data

import com.example.model.Champion
import com.example.model.DraftSlot
import com.example.model.LaneRole
import kotlin.random.Random

data class RandomDraft(val allies: List<DraftSlot>, val enemies: List<DraftSlot>)

/** Every allied champion participates in a connected engage/follow-up combo. */
object RandomDraftPolicy {
    private val initiators = setOf("malphite", "amumu", "jarvaniv", "jarvan", "diana", "wukong", "rakan", "alistar", "leona", "nautilus", "galio", "kennen", "sett", "seraphine")
    private val airborne = setOf("malphite", "diana", "wukong", "rakan", "alistar", "nautilus", "galio", "sett", "jarvaniv", "jarvan")
    private val followups = setOf("orianna", "yasuo", "kennen", "missfortune", "samira", "xayah", "kalista", "galio", "diana")
    private fun key(champion: Champion) = champion.name.lowercase(java.util.Locale.ROOT).filter(Char::isLetterOrDigit)

    fun combo(a: Champion, b: Champion): Boolean {
        if (a.id == b.id) return false
        val x = key(a); val y = key(b)
        fun connects(source: String, target: String): Boolean = when (target) {
            "yasuo" -> source in airborne
            "orianna" -> source in initiators && source != "seraphine"
            else -> source in initiators && target in followups
        }
        return connects(x, y) || connects(y, x)
    }

    fun isWomboTeam(champions: List<Champion>): Boolean {
        if (champions.size != 5 || champions.map { it.id }.distinct().size != 5) return false
        val reached = mutableSetOf(champions.first().id)
        repeat(champions.size) {
            champions.filter { it.id in reached }.forEach { source ->
                champions.filter { combo(source, it) }.forEach { reached += it.id }
            }
        }
        return reached.size == champions.size
    }

    fun generate(roster: List<Champion>, random: Random = Random.Default): RandomDraft {
        val champions = roster.filter { it.id.isNotBlank() && it.id != "empty" }.distinctBy { it.id }
        val pools = LaneRole.entries.associateWith { role ->
            champions.filter { it.primaryRole == role && (key(it) in initiators || key(it) in followups) }.shuffled(random)
        }
        fun search(index: Int, chosen: List<Champion>): List<Champion>? {
            if (index == LaneRole.entries.size) return chosen.takeIf(::isWomboTeam)
            for (candidate in pools.getValue(LaneRole.entries[index])) {
                if (chosen.none { it.id == candidate.id }) search(index + 1, chosen + candidate)?.let { return it }
            }
            return null
        }
        val allies = checkNotNull(search(0, emptyList())) { "No se pudo formar un equipo con combos. Vuelve a intentarlo." }
        val occupied = allies.mapTo(mutableSetOf()) { it.id }
        val enemies = LaneRole.entries.map { role ->
            val champion = champions.filter { it.primaryRole == role && it.id !in occupied }.randomOrNull(random)
                ?: error("No hay suficientes campeones disponibles para esta línea.")
            occupied += champion.id
            DraftSlot(champion, role)
        }
        return RandomDraft(allies.map { DraftSlot(it, it.primaryRole) }, enemies)
    }
}
