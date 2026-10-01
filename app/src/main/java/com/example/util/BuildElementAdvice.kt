package com.example.util

import java.util.Locale

/** Keep build-specific advice separate from immutable catalog descriptions. */
object BuildElementAdvice {
    fun resolve(name: String, entries: List<Pair<String, String>>, fallback: String): String {
        fun key(value: String) = value.lowercase(Locale.ROOT).filter { it.isLetterOrDigit() }
        val wanted = key(name)
        return entries.firstOrNull { key(it.first) == wanted && it.second.isNotBlank() }
            ?.second?.trim() ?: fallback.trim()
    }
}
