package com.example.data

/** Reuse the outstanding command only when its meaning is identical, not just its action. */
object EconomyRequestPolicy {
    /**
     * The private worker is also the durable fallback for staff operations.
     * The server still checks the signed administrator claim before executing
     * any privileged command; this only avoids a false connection error when
     * the optional callable transport is unavailable.
     */
    private val queueableActions = setOf(
        "PURCHASE", "SUBSCRIBE", "UNSUBSCRIBE", "SPONSOR",
        "REDEEM", "RESOLVE", "ADJUST", "PREMIUM_GRANT", "PREMIUM_REMOVE", "ROLE"
    )

    fun canQueue(action: String, administrator: Boolean): Boolean = action in queueableActions

    fun sameCommand(existing: Map<*, *>, requested: Map<*, *>): Boolean =
        canonical(existing.filterKeys { it != "id" }) == canonical(requested.filterKeys { it != "id" })

    private fun canonical(value: Any?): Any? = when (value) {
        is Map<*, *> -> value.entries.associate { it.key.toString() to canonical(it.value) }.toSortedMap()
        is List<*> -> value.map(::canonical)
        is Number -> value.toDouble()
        else -> value
    }
}
