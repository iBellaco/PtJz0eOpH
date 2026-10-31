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
        "REDEEM", "RESOLVE", "SPONSOR_RESOLVE", "ADJUST", "PREMIUM_GRANT", "PREMIUM_REMOVE", "ROLE"
    )

    fun canQueue(action: String, administrator: Boolean): Boolean = action in queueableActions

    fun sameCommand(existing: Map<*, *>, requested: Map<*, *>): Boolean =
        canonical(existing.filterKeys { it != "id" }) == canonical(requested.filterKeys { it != "id" })

    fun operationKey(uid: String, action: String, fields: Map<String, Any>): String {
        val serialized = encode(canonical(listOf(uid, action, fields)))
        return java.security.MessageDigest.getInstance("SHA-256").digest(serialized.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    // Length-prefixed values avoid collisions caused by delimiters inside user text.
    private fun encode(value: Any?): String = when (value) {
        null -> "n"
        is String -> "s${value.length}:$value"
        is Boolean -> if (value) "b1" else "b0"
        is Number -> "d${value.toDouble()};"
        is Map<*, *> -> "m${value.size}:" + value.entries.joinToString("") { encode(it.key) + encode(it.value) }
        is List<*> -> "l${value.size}:" + value.joinToString("") { encode(it) }
        else -> error("Unsupported operation value")
    }

    private fun canonical(value: Any?): Any? = when (value) {
        is Map<*, *> -> value.entries.associate { it.key.toString() to canonical(it.value) }.toSortedMap()
        is List<*> -> value.map(::canonical)
        is Number -> value.toDouble()
        else -> value
    }
}
