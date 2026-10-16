package com.example.data.sync

/** Pure validation/fusion so a successful HTTP response is not mistaken for useful data. */
object GlobalTierConsensus {
    fun displayCategory(value: String): String = when (val tier = value.trim().uppercase()) {
        "A+" -> "A"
        "D" -> "C"
        else -> tier
    }
    private val weights = mapOf("S+" to 7.0, "S" to 6.0, "A+" to 5.0, "A" to 4.0,
        "B" to 3.0, "C" to 2.0, "D" to 1.0)

    fun validated(parsed: Map<String, String>, knownIds: Set<String>): Map<String, String> {
        val known = knownIds.map(RegionalTierParser::canonical).toSet()
        val valid = parsed.filter { (id, tier) -> id in known && tier in weights }
        val required = minOf(40, known.size)
        return valid.takeIf { required > 0 && it.size >= required } ?: emptyMap()
    }

    fun mergeSnapshot(previous: Map<String, String>, fresh: Map<String, String>): Map<String, String> =
        previous.filterValues { it in weights } + fresh.filterValues { it in weights }

    /**
     * Replaces every champion published by the newest rate snapshot while
     * retaining older entries when a publisher only exposes a partial table.
     * The right-hand side always wins, so a changed patch value cannot be
     * hidden behind a cached percentage.
     */
    fun mergeStats(
        previous: Map<String, ChampionMetaStats>,
        fresh: Map<String, ChampionMetaStats>
    ): Map<String, ChampionMetaStats> =
        previous.mapKeys { (id, _) -> RegionalTierParser.canonical(id) } +
            fresh.mapKeys { (id, _) -> RegionalTierParser.canonical(id) }

    fun fuse(sources: List<Map<String, String>>): Map<String, String> {
        val votes = mutableMapOf<String, MutableList<Double>>()
        sources.forEach { source -> source.forEach { (id, tier) ->
            weights[tier]?.let { votes.getOrPut(id) { mutableListOf() }.add(it) }
        } }
        return votes.mapValues { (_, scores) ->
            val mean = scores.average()
            when {
                mean >= 6.5 -> "S+"
                mean >= 5.5 -> "S"
                mean >= 3.5 -> "A"
                mean >= 2.5 -> "B"
                mean >= 1.5 -> "C"
                else -> "C"
            }
        }
    }

    fun fuseStats(sources: List<Map<String, ChampionMetaStats>>): Map<String, ChampionMetaStats> {
        val grouped = mutableMapOf<String, MutableList<ChampionMetaStats>>()
        sources.forEach { source ->
            source.forEach { (id, stats) ->
                if (stats.isUsable()) {
                    grouped.getOrPut(RegionalTierParser.canonical(id)) { mutableListOf() }.add(stats)
                }
            }
        }
        fun average(values: List<Double?>): Double? {
            val present = values.filterNotNull().filter { it.isFinite() && it >= 0.0 }
            return present.takeIf { it.isNotEmpty() }?.average()
        }
        return grouped.mapValues { (_, values) ->
            ChampionMetaStats(
                winRate = average(values.map { it.winRate }),
                pickRate = average(values.map { it.pickRate }),
                banRate = average(values.map { it.banRate })
            )
        }
    }

    fun fuseRoleStats(sources: List<Map<String, Map<com.example.model.LaneRole, ChampionMetaStats>>>): Map<String, Map<com.example.model.LaneRole, ChampionMetaStats>> {
        val output = linkedMapOf<String, MutableMap<com.example.model.LaneRole, ChampionMetaStats>>()
        sources.forEach { source -> source.forEach { (id, lanes) -> lanes.forEach { (lane, stats) ->
            if (stats.isUsable()) output.getOrPut(RegionalTierParser.canonical(id)) { linkedMapOf() }[lane] = stats
        } } }
        return output
    }

    fun mergeRoleStats(
        previous: Map<String, Map<com.example.model.LaneRole, ChampionMetaStats>>,
        fresh: Map<String, Map<com.example.model.LaneRole, ChampionMetaStats>>
    ): Map<String, Map<com.example.model.LaneRole, ChampionMetaStats>> =
        (previous.keys + fresh.keys).associateWith { id -> previous[id].orEmpty() + fresh[id].orEmpty() }
}
