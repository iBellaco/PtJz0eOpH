package com.example.util

import com.example.data.SituationalItemAdvisor
import com.example.data.WildRiftItemsData
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

    /**
     * Generates advice for the tapped item instead of repeating the generic champion plan.
     * Situational items always name the trigger and concrete enemy examples.
     */
    fun contextualItemAdvice(
        itemName: String,
        championName: String,
        roleName: String,
        language: String,
        situational: Boolean
    ): String {
        if (isFlash(itemName)) return ""
        val lang = AppLanguage.normalize(language)
        val pt = lang == "pt"
        val catalog = WildRiftItemsData.getItemByName(itemName)
        val strategic = SituationalItemAdvisor.getAdvice(itemName, lang)
        val localizedName = catalog?.getLocalizedName(lang) ?: strategic.name
        val catalogTip = catalog?.getLocalizedCoachTip(lang).orEmpty()
        val purpose = strategic.purpose.takeIf { it.isNotBlank() }
            ?: catalog?.getLocalizedPassive(lang).orEmpty().substringBefore("\n")
        val against = strategic.bestAgainst
            .filterNot {
                it.contains("Composiciones rivales especializadas", ignoreCase = true) ||
                    it.contains("Amenazas prioritarias", ignoreCase = true)
            }
            .take(8)
            .joinToString(", ")
        val trigger = strategic.recommendationTip.takeIf {
            it.isNotBlank() && !it.contains("según el estado de la partida", ignoreCase = true)
        }.orEmpty()

        return if (situational) {
            if (pt) buildString {
                appendLine("Quando usar $localizedName:")
                appendLine(trigger.ifBlank { catalogTip.ifBlank { purpose } })
                if (against.isNotBlank()) appendLine("\nContra quais campeões/composições:\n$against")
                appendLine("\nPor que funciona com $championName ($roleName):")
                appendLine(catalogTip.ifBlank { purpose })
                append("\nRegra de compra: não substitua o núcleo por padrão; troque um item apenas quando essa ameaça for uma das condições principais da partida.")
            } else buildString {
                appendLine("Cuándo usar $localizedName:")
                appendLine(trigger.ifBlank { catalogTip.ifBlank { purpose } })
                if (against.isNotBlank()) appendLine("\nContra qué campeones/composiciones:\n$against")
                appendLine("\nPor qué funciona con $championName ($roleName):")
                appendLine(catalogTip.ifBlank { purpose })
                append("\nRegla de compra: no reemplaces el core por defecto; cambia un objeto solo cuando esa amenaza sea una de las condiciones principales de la partida.")
            }
        } else {
            if (pt) buildString {
                appendLine("Função de $localizedName nesta build de $championName ($roleName):")
                appendLine(catalogTip.ifBlank { purpose })
                if (trigger.isNotBlank()) appendLine("\nQuando priorizar:\n$trigger")
                if (against.isNotBlank()) append("\nEspecialmente útil contra: $against")
            } else buildString {
                appendLine("Función de $localizedName en esta build de $championName ($roleName):")
                appendLine(catalogTip.ifBlank { purpose })
                if (trigger.isNotBlank()) appendLine("\nCuándo priorizarlo:\n$trigger")
                if (against.isNotBlank()) append("\nEspecialmente útil contra: $against")
            }
        }.trim()
    }
}
