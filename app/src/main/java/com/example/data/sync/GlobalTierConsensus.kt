package com.example.data.sync

/** Pure validation/fusion so a successful HTTP response is not mistaken for useful data. */
object GlobalTierConsensus {
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
                mean >= 4.5 -> "A+"
                mean >= 3.5 -> "A"
                mean >= 2.5 -> "B"
                mean >= 1.5 -> "C"
                else -> "D"
            }
        }
    }
}
