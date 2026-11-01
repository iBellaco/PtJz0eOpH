package com.example.util

/** Bounded, best-effort redaction. Never deliberately log credentials or personal data. */
object DiagnosticPrivacy {
    private val email = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
    private val bearer = Regex("(?i)Bearer\\s+[^\\s,;]+")
    private val secrets = Regex("(?i)(password|passwd|token|authorization|api[_-]?key|secret)\\s*[:=]\\s*[^\\s,;]+")
    private val urlQuery = Regex("(https?://[^\\s?#]+)[?#][^\\s]+")
    fun redact(value: String): String = value.take(16_384)
        .replace(email, "[email]")
        .replace(bearer, "Bearer [redacted]")
        .replace(secrets) { "${it.groupValues[1]}=[redacted]" }
        .replace(urlQuery) { "${it.groupValues[1]}?[redacted]" }
        .take(4_096)
}
