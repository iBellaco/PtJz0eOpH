package com.example.model

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Premium is a timed entitlement independent of account roles; staff retains system lifetime. */
object PremiumAccessPolicy {
    const val DAY_MILLIS = 86_400_000L
    private val isoDeadline = Regex("""^(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2})(?:\.(\d{1,9}))?(Z|[+-]\d{2}:\d{2})$""")
    fun isLifetime(role: String, secondary: String = "", adminClaim: Boolean = false): Boolean =
        role != "banned" && secondary != "banned" && (adminClaim || role in setOf("admin", "administrador", "moderador") || secondary == "moderador")

    fun isActive(role: String, until: Long?, now: Long = System.currentTimeMillis(), secondary: String = "", adminClaim: Boolean = false, banned: Boolean = false, granted: Boolean = false): Boolean =
        !banned && role != "banned" && secondary != "banned" && (isLifetime(role, secondary, adminClaim) || (until != null && until > now))

    fun deadline(value: Any?): Long? = when (value) {
        is Number -> value.toLong()
        is com.google.firebase.Timestamp -> value.toDate().time
        is java.util.Date -> value.time
        is String -> value.toLongOrNull() ?: parseIsoDeadline(value)
        else -> null
    }

    private fun parseIsoDeadline(value: String): Long? {
        val parts = isoDeadline.matchEntire(value) ?: return null
        val milliseconds = parts.groupValues[2].take(3).padEnd(3, '0')
        val normalized = "${parts.groupValues[1]}.$milliseconds${parts.groupValues[3]}"
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ROOT).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val position = ParsePosition(0)
        return formatter.parse(normalized, position)?.time?.takeIf { position.index == normalized.length }
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
        if (role == "free") 0L else inherited

    fun extend(until: Long?, days: Int, now: Long): Long {
        require(days in 1..36500)
        return maxOf(now, until ?: 0L) + days.toLong() * DAY_MILLIS
    }
}
