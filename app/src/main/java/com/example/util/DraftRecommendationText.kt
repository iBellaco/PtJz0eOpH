package com.example.util

/** Public draft recommendations stay focused on decisions, not balance-history notes. */
object DraftRecommendationText {
    private val balanceHistory = Regex("(?i)\\b(parche|patch|buff|buffs|buffe|buffed|nerf|nerfs|nerfe|nerfed)\\b")

    fun clean(value: String): String {
        if (value.isBlank()) return ""
        return value
            .replace(Regex("<[^>]+>"), " ")
            .split(Regex("(?<=[.!?])\\s+|\\n+"))
            .map(String::trim)
            .filter { it.isNotBlank() && !balanceHistory.containsMatchIn(it) }
            .joinToString(" ")
            .replace(Regex("\\s{2,}"), " ")
            .trim()
    }
}
