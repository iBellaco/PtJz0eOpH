package com.example.data

/** Reuse the outstanding command only when its meaning is identical, not just its action. */
object EconomyRequestPolicy {
    /** Personal ads use the same trusted queue regardless of the account's staff claim. */
    fun canQueue(action: String, administrator: Boolean): Boolean = !administrator || action == "SPONSOR"

    fun sameCommand(existing: Map<*, *>, requested: Map<*, *>): Boolean =
        canonical(existing.filterKeys { it != "id" }) == canonical(requested.filterKeys { it != "id" })

    private fun canonical(value: Any?): Any? = when (value) {
        is Map<*, *> -> value.entries.associate { it.key.toString() to canonical(it.value) }.toSortedMap()
        is List<*> -> value.map(::canonical)
        is Number -> value.toDouble()
        else -> value
    }
}
