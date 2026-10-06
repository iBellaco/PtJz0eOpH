package com.example.util

data class SpellCatalogPresentation(
    val mapLabels: List<String>,
    val description: String
)

/** Separates the map applicability header from the spell mechanic text for catalog chips. */
object SpellCatalogFormatting {
    private val mapPrefix = Regex(
        "^\\s*Mapas?\\s+(?:aplicables?|disponibles?|aplicáveis?|disponíveis?)\\s*:\\s*",
        RegexOption.IGNORE_CASE
    )

    fun split(description: String, language: String): SpellCatalogPresentation {
        if (description.isBlank()) return SpellCatalogPresentation(emptyList(), "")
        val normalized = description.replace("\r\n", "\n")
        val firstBreak = normalized.indexOf('\n')
        val firstLine = if (firstBreak >= 0) normalized.substring(0, firstBreak).trim() else normalized.trim()
        if (!mapPrefix.containsMatchIn(firstLine)) {
            return SpellCatalogPresentation(emptyList(), normalized.trim())
        }

        val rawMaps = firstLine.replaceFirst(mapPrefix, "")
            .trim()
            .trimEnd('.', ';')
            .replace(Regex("\\s+y\\s+el\\s+", RegexOption.IGNORE_CASE), ", ")
            .replace(Regex("\\s+e\\s+o\\s+", RegexOption.IGNORE_CASE), ", ")
            .replace(Regex("\\s+y\\s+", RegexOption.IGNORE_CASE), ", ")
            .replace(Regex("\\s+e\\s+", RegexOption.IGNORE_CASE), ", ")

        val pt = AppLanguage.normalize(language) == "pt"
        val labels = rawMaps.split(',')
            .map { it.trim().removePrefix("el ").removePrefix("El ") }
            .filter { it.isNotBlank() }
            .map { label ->
                if (pt && label.equals("Abismo de los Lamentos", ignoreCase = true)) {
                    "Abismo dos Lamentos"
                } else label
            }
            .distinctBy { it.lowercase() }

        val body = if (firstBreak >= 0) normalized.substring(firstBreak + 1).trim() else ""
        return SpellCatalogPresentation(labels, body)
    }
}
