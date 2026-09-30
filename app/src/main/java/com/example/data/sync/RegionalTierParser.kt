package com.example.data.sync

/** Parses published editorial categories; never derives match percentages from page reachability. */
object RegionalTierParser {
    fun canonical(value: String): String = value.lowercase(java.util.Locale.ROOT)
        .filter { it.isLetterOrDigit() }.let { when (it) {
            "nunu", "nunuwillump" -> "nunuwillump"
            "monkeyking" -> "wukong"
            else -> it
        } }
    private val events = Regex("""<div\b[^>]*class=["']tier\s+(splus|s|a|b|c)["'][^>]*>|<a\b[^>]*href=["']/guide/([^"']+)["'][^>]*class=["'][^"']*\bico-holder\b""", RegexOption.IGNORE_CASE)
    private val order = listOf("S+", "S", "A", "B", "C")
    fun parse(html: String): Map<String, String> {
        val result = linkedMapOf<String, String>()
        var category: String? = null
        events.findAll(html).forEach { match ->
            val tier = match.groupValues[1]
            if (tier.isNotEmpty()) category = if (tier.equals("splus", true)) "S+" else tier.uppercase(java.util.Locale.ROOT)
            else if (category != null) {
                val id = canonical(match.groupValues[2])
                val old = result[id]
                if (old == null || order.indexOf(category!!) < order.indexOf(old)) result[id] = category!!
            }
        }
        return result
    }
}
