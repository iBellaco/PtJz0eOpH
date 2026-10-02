package com.example.model

/** A stored deadline survives role changes; only an eligible role can use it. */
object PremiumAccessPolicy {
    const val DAY_MILLIS = 86_400_000L
    fun isLifetime(role: String, secondary: String = "", adminClaim: Boolean = false): Boolean =
        role != "banned" && (RolePanelAccess.isAdministrator(role, adminClaim) || role == "moderador" || secondary == "moderador")

    fun isActive(role: String, until: Long?, now: Long = System.currentTimeMillis(), secondary: String = "", adminClaim: Boolean = false, banned: Boolean = false): Boolean =
        !banned && role != "banned" && (isLifetime(role, secondary, adminClaim) ||
            ((role == "premium" || secondary == "premium") && until != null && until > now))

    fun deadlineForRole(role: String, inherited: Long?, now: Long): Long? =
        if (role == "premium" && (inherited == null || inherited == 0L)) now + 30 * DAY_MILLIS else inherited

    fun extend(until: Long?, days: Int, now: Long): Long {
        require(days in 1..36500)
        return maxOf(now, until ?: 0L) + days.toLong() * DAY_MILLIS
    }
}
