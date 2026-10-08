package com.example.data

/** Reuse the outstanding command only when its meaning is identical, not just its action. */
object EconomyRequestPolicy {
    fun sameCommand(existing: Map<*, *>, requested: Map<*, *>): Boolean =
        canonical(existing.filterKeys { it != "id" }) == canonical(requested.filterKeys { it != "id" })

    private fun canonical(value: Any?): Any? = when (value) {
        is Map<*, *> -> value.entries.associate { it.key.toString() to canonical(it.value) }.toSortedMap()
        is List<*> -> value.map(::canonical)
        is Number -> value.toDouble()
        else -> value
    }
}
