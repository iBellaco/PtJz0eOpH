package com.example.util

/** Offline exact phrases and full sentence templates. Never performs network translation. */
class TranslationCatalog(private val portuguese: Map<String, String>, private val spanish: Map<String, String> = emptyMap(), private val portugueseAliases: Map<String, String> = emptyMap()) {
    // Several UI labels intentionally share a Portuguese target (for example
    // "Cerrar" and "Close" both map to "Fechar"). Keep the first canonical
    // Spanish source so switching back to Spanish does not return an English
    // alias selected only because it appears later in the catalog.
    private val reverse = portuguese.entries.filter { it.key != it.value }
        .groupBy({ it.value }, { it.key })
        .mapValues { (_, sources) ->
            sources.firstOrNull { source ->
                source.lowercase(java.util.Locale.ROOT) !in setOf(
                    "close", "back", "save", "open", "cancel", "settings", "language",
                    "continue", "next", "previous", "delete", "edit", "search", "home"
                )
            } ?: sources.first()
        }
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
        val raw = translate(language, text, 0)
        val result = if (language.trim().lowercase(java.util.Locale.ROOT).startsWith("pt")) PortugueseTextCleanup.apply(raw) else raw
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

    /** Normalizes frequent Spanish leftovers found in offline Portuguese catalog and champion data. */
    private object PortugueseTextCleanup {
        private val replacements = listOf(
            "Invocación Estelar" to "Invocação Estelar",
            "Juego Medio/Tardío" to "Jogo Médio/Tardio",
            "Asesinos" to "Assassinos", "asesinos" to "assassinos",
            "Tardío" to "Tardio", "tardío" to "tardio",
            "Dragones" to "Dragões", "dragones" to "dragões",
            "Heraldo" to "Arauto", "heraldo" to "arauto",
            "obligatorio" to "obrigatório", "obligatoria" to "obrigatória",
            "obligatorios" to "obrigatórios", "obligatorias" to "obrigatórias",
            "primera línea" to "linha de frente", "primera linea" to "linha de frente",
            "peleas grupales" to "lutas em equipe", "peleas de equipo" to "lutas em equipe",
            "peleas" to "lutas", "pelea" to "luta",
            "Empuja" to "Empurre", "empuja" to "empurre",
            "recuerda" to "lembre-se", "agrupa" to "agrupe", "flanquea" to "flanqueie",
            "Muévete" to "Mova-se", "muévete" to "mova-se",
            "pasillos" to "corredores", "Absorbe" to "Absorva", "absorbe" to "absorva",
            "Deniega" to "Negue", "deniega" to "negue", "farmeo" to "farm",
            "apoindo-se" to "apoiando-se", "Acierta" to "Acerte",
            "Bloquea automáticamente" to "Bloqueie automaticamente",
            "bloquea automáticamente" to "bloqueie automaticamente", "estuneado" to "atordoado",
            "la próxima" to "a próxima", "la siguiente" to "a seguinte",
            "habilidad" to "habilidade", "enemiga" to "inimiga", "enemigo" to "inimigo",
            "enemigas" to "inimigas", "enemigos" to "inimigos",
            "cazado" to "caçado",
            "projectoil" to "projétil", "projetoil" to "projétil", "projeito" to "projétil",
            "Relanzamiento" to "Reativação", "por cada" to "para cada",
            "Cada vez que" to "Sempre que", "cada vez que" to "sempre que",
            "outorga" to "concede", "Outorga" to "Concede",
            "inflige danos mágico" to "causa dano mágico", "inflige dano mágico" to "causa dano mágico",
            "inflige danos físicos" to "causa dano físico", "inflige dano físico" to "causa dano físico",
            "inflige danos verdadeiros" to "causa dano verdadeiro", "inflige dano verdadeiro" to "causa dano verdadeiro",
            "Evolución" to "Evolução", "Selección" to "Seleção", "selección" to "seleção",
            "Táctico" to "Tático", "táctico" to "tático",
            "Canalización" to "Canalização", "canalización" to "canalização",
            "Habilidade corriente" to "Habilidade comum", "habilidade corriente" to "habilidade comum",
            "Cada 4.o ataque" to "A cada 4º ataque",
            "Esses Laranjas" to "Essências Laranja", "esses Laranjas" to "Essências Laranja",
            "A coste cero" to "Sem custo", "a coste cero" to "sem custo"
        )
        private val replacementMap = replacements.associate { it.first.lowercase(java.util.Locale.ROOT) to it.second }
        private val matcher = Regex(
            "(?<![\\p{L}])(?:${replacements.map { Regex.escape(it.first) }.sortedByDescending { it.length }.joinToString("|")})(?![\\p{L}])",
            RegexOption.IGNORE_CASE
        )

        fun apply(text: String): String = matcher.replace(text) { match ->
            val replacement = replacementMap[match.value.lowercase(java.util.Locale.ROOT)] ?: return@replace match.value
            when {
                match.value.all { !it.isLetter() || it.isUpperCase() } -> replacement.uppercase(java.util.Locale.ROOT)
                match.value.firstOrNull()?.isUpperCase() == true -> replacement.replaceFirstChar { it.uppercase(java.util.Locale.ROOT) }
                else -> replacement
            }
        }
    }
}
