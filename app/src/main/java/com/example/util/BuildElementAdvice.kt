package com.example.util

import java.util.Locale

/** Keep build-specific advice separate from immutable catalog descriptions. */
object BuildElementAdvice {
    fun isFlash(name: String): Boolean = name.trim().lowercase(Locale.ROOT) in setOf("destello", "flash")
    fun resolve(name: String, entries: List<Pair<String, String>>, fallback: String, catalogDescription: String = ""): String {
        if (isFlash(name)) return ""
        fun key(value: String) = value.lowercase(Locale.ROOT).filter { it.isLetterOrDigit() }
        val wanted = key(name)
        return entries.firstOrNull { key(it.first) == wanted && it.second.isNotBlank() }
            ?.second?.trim()?.takeUnless { it == catalogDescription.trim() } ?: fallback.trim()
    }
}
