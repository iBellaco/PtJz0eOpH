package com.example.util

/** Public draft recommendations stay focused on decisions, not balance-history notes. */
object DraftRecommendationText {
    private val balanceHistory = Regex(
        "(?iu)\\b(?:parches?|patch(?:es)?|buff(?:s|ed|e(?:ad[oa]s?|os?|s)?|ad[oa]s?|ou)?|" +
            "nerf(?:s|ed|e(?:ad[oa]s?|os?|s)?|ad[oa]s?|ou)?|" +
            "(?:actualizaci[oó]n|atualiza[çc][aã]o|cambios?|mudan[çc]as?|ajustes?)\\s+(?:de\\s+|d[eo]\\s+)?balance(?:amento|o)?|" +
            "(?:versi[oó]n|vers[aã]o)\\s+\\d+(?:\\.\\d+)+[a-z]?|\\d{1,2}\\.\\d{1,2}[a-z])\\b"
    )
    private val balanceChange = Regex(
        "(?iu)\\b(?:bajaron|subieron|aumentaron|redujeron|aumentou|reduziu|diminu[ií]ram|aumentaram|" +
            "foi\\s+(?:aumentad[oa]|reduzid[oa])|fue\\s+(?:aumentad[oa]|reducid[oa]))\\s+" +
            "(?:(?:el|la|los|las|o|a|os|as|su|seu|sua)\\s+)?(?:da[ñn]o|alcance|escudos?|enfriamiento|recarga|vida|armadura)\\b"
    )

    fun clean(value: String): String {
        if (value.isBlank()) return ""
        return value
            .replace(Regex("<[^>]+>"), " ")
            // A semicolon or bullet can separate a balance note from a useful instruction.
            // Decimal values (0.5 s / 0,5 s) do not split sentences.
            .split(Regex("(?<=[.!?])\\s+|[\\n;•]+"))
            .map(String::trim)
            .filter { sentence ->
                val balanceCandidate = sentence.replace(
                    Regex("(?iu)\\bbuff\\s+(?:rojo|azul|vermelho|red|blue)\\b"), "recurso"
                )
                sentence.isNotBlank() && !balanceHistory.containsMatchIn(balanceCandidate) && !balanceChange.containsMatchIn(sentence)
            }
            .distinctBy { it.lowercase(java.util.Locale.ROOT).trimEnd('.', '!', '?') }
            .joinToString(" ")
            .replace(Regex("\\s{2,}"), " ")
            .trim()
    }
}
