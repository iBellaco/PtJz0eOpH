package com.example.util

/** Offline exact phrases and full sentence templates. Never performs network translation. */
class TranslationCatalog(private val portuguese: Map<String, String>, private val spanish: Map<String, String> = emptyMap(), private val portugueseAliases: Map<String, String> = emptyMap()) {
    private val reverse = portuguese.entries.filter { it.key != it.value }.associate { it.value to it.key }
    private fun folded(map: Map<String, String>) = map.entries.associate {
        it.key.trim().replace(Regex("\\s+"), " ").lowercase(java.util.Locale.ROOT) to it.value
    }
    private val ptFolded by lazy { folded(portuguese) }
    private val esFolded by lazy { folded(reverse) }
    private fun preserveCase(source: String, translated: String): String =
        if (source.any { it.isLetter() } && source.filter { it.isLetter() }.all { it.isUpperCase() }) translated.uppercase(java.util.Locale.ROOT) else translated
    private data class Template(val pattern: Regex, val output: String, val slots: List<Int>, val weight: Int, val anchor: String)
    private val tokens = Regex("\\{(\\d+)\\}|%(?:\\d+\\$)?[-+0#]*\\d*(?:\\.\\d+)?[dsf](?![\\p{L}])")
    private fun buildTemplates(map: Map<String, String>): List<Template> = map.mapNotNull { (source, target) ->
        val matches = tokens.findAll(source).toList()
        if (matches.isEmpty()) return@mapNotNull null
        var offset = 0
        val slots = mutableListOf<Int>()
        val regex = StringBuilder("^")
        matches.forEachIndexed { index, match ->
            regex.append(Regex.escape(source.substring(offset, match.range.first)))
            regex.append("([\\s\\S]*?)")
            slots.add(match.groups[1]?.value?.toIntOrNull() ?: index)
            offset = match.range.last + 1
        }
        regex.append(Regex.escape(source.substring(offset))).append("$")
        val literalLength = source.length - matches.sumOf { it.value.length }
        // Avoid matching arbitrary user messages with generic patterns such as "{0}".
        if (literalLength < 4) null else Template(Regex(regex.toString(), RegexOption.IGNORE_CASE), target, slots, literalLength, tokens.split(source).maxByOrNull { it.length }.orEmpty())
    }.sortedByDescending { it.weight }
    private val ptTemplates by lazy { buildTemplates(portuguese) }
    private val esTemplates by lazy { buildTemplates(reverse) }
    private val translatedPhrases = portuguese.values.toHashSet()

    private val cache = object : LinkedHashMap<Pair<String, String>, String>(512, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Pair<String, String>, String>?) = size > 512
    }
    @Synchronized
    fun translate(language: String, text: String): String {
        val key = language to text
        cache[key]?.let { return it }
        val result = translate(language, text, 0)
        if (text.length < 2048) cache[key] = result
        return result
    }

    private fun translate(language: String, text: String, depth: Int): String {
        if (text.isEmpty()) return text
        val isPt = language.trim().lowercase(java.util.Locale.ROOT).startsWith("pt")
        if (isPt) portugueseAliases[text]?.let { return it }
        val map = if (isPt) portuguese else reverse
        if (!isPt) {
            spanish[text]?.let { return it }
            // Canonical Spanish must not be mistaken for an ambiguous reverse alias.
            if (text in portuguese) return text
        }
        map[text]?.let { return it }
        if (isPt && text in translatedPhrases) return text
        val trimmed = text.trim()
        map[trimmed]?.let { return text.takeWhile { c -> c.isWhitespace() } + it + text.takeLastWhile { c -> c.isWhitespace() } }
        val foldedKey = trimmed.replace(Regex("\\s+"), " ").lowercase(java.util.Locale.ROOT)
        (if (isPt) ptFolded else esFolded)[foldedKey]?.let {
            return text.takeWhile { c -> c.isWhitespace() } + preserveCase(trimmed, it) + text.takeLastWhile { c -> c.isWhitespace() }
        }
        if (depth < 5) {
            val templates = if (isPt) ptTemplates else esTemplates
            for (template in templates) {
                if (!text.contains(template.anchor, ignoreCase = true)) continue
                val match = template.pattern.matchEntire(text) ?: continue
                val values = template.slots.mapIndexed { i, slot -> slot to translate(language, match.groupValues[i + 1], depth + 1) }.toMap()
                var formatIndex = 0
                val translated = tokens.replace(template.output) { token ->
                    val slot = token.groups[1]?.value?.toIntOrNull() ?: formatIndex++
                    values[slot] ?: token.value
                }
                return preserveCase(text, translated)
            }
            // Decorative prefixes and styled wrappers must not block a complete phrase.
            val envelope = Regex("^([^\\p{L}\\p{N}]*)([\\s\\S]*?)([^\\p{L}\\p{N}]*)$").matchEntire(text)
            if (envelope != null && envelope.groupValues[2].isNotEmpty() && envelope.groupValues[2] != text) {
                val center = envelope.groupValues[2]
                val translated = translate(language, center, depth + 1)
                if (translated != center) return envelope.groupValues[1] + translated + envelope.groupValues[3]
            }
            // Independently assembled list rows retain complete phrase translations.
            for (separator in listOf("\n", " • ", " | ", ": ")) {
                if (text.contains(separator)) {
                    val chunks = text.split(separator)
                    val localized = chunks.map { translate(language, it, depth + 1) }
                    if (localized != chunks) return localized.joinToString(separator)
                }
            }
        }
        // Unknown proper names, URLs and user-authored content are kept verbatim.
        return text
    }
}
