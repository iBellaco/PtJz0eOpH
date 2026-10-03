package com.example.model

/** Premium is a timed entitlement independent of account roles; staff retains system lifetime. */
object PremiumAccessPolicy {
    const val DAY_MILLIS = 86_400_000L
    fun isLifetime(role: String, secondary: String = "", adminClaim: Boolean = false): Boolean =
        role != "banned" && secondary != "banned" && (RolePanelAccess.isAdministrator(role, adminClaim) || role == "moderador" || secondary == "moderador")

    fun isActive(role: String, until: Long?, now: Long = System.currentTimeMillis(), secondary: String = "", adminClaim: Boolean = false, banned: Boolean = false, granted: Boolean = false): Boolean =
        !banned && role != "banned" && secondary != "banned" && (isLifetime(role, secondary, adminClaim) || (until != null && until > now))

    fun deadline(value: Any?): Long? = when (value) {
        is Number -> value.toLong()
        is com.google.firebase.Timestamp -> value.toDate().time
        is java.util.Date -> value.time
        is String -> value.toLongOrNull() ?: runCatching { java.time.Instant.parse(value).toEpochMilli() }.getOrNull()
        else -> null
    }

    fun hasGrant(plan: String?): Boolean = plan?.startsWith("Admin Grant", ignoreCase = true) == true

    fun isActiveAccount(account: Map<String, Any>, now: Long = System.currentTimeMillis()): Boolean = isActive(
        (account["role"] as? String).orEmpty(), deadline(account["premiumUntil"]), now,
        secondary = (account["secondaryRole"] as? String).orEmpty(), adminClaim = account["admin"] == true,
        banned = account["banned"] == true, granted = hasGrant(account["subscriptionPlan"] as? String))

    fun isExpiringSoon(until: Long?, now: Long): Boolean = until != null && until - now in 1..3 * DAY_MILLIS

    fun remaining(until: Long, now: Long): String {
        val total = ((until - now).coerceAtLeast(0L) + 999L) / 1000L
        return "${total / 86400}d ${(total / 3600) % 24}h ${(total / 60) % 60}m ${total % 60}s"
    }

    fun deadlineForRole(role: String, inherited: Long?, now: Long): Long? =
        inherited

    fun extend(until: Long?, days: Int, now: Long): Long {
        require(days in 1..36500)
        return maxOf(now, until ?: 0L) + days.toLong() * DAY_MILLIS
    }
}
