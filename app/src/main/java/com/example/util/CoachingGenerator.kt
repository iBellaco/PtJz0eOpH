package com.example.util

import com.example.model.Champion
import com.example.model.LaneRole

object CoachingGenerator {
    private data class AdviceKey(val id: String, val name: String, val advice: String,
        val skills: List<com.example.model.ChampionSkill>, val role: LaneRole, val language: String)
    private val adviceCache = object : LinkedHashMap<AdviceKey, String>(640, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<AdviceKey, String>?): Boolean = size > 640
    }

    
    fun generateMatchupReason(champion: Champion, activeRole: LaneRole, target: String, type: String, lang: String): String {
        val isPt = AppLanguage.normalize(lang) == "pt"
        val targetChamp = com.example.data.WildRiftRepository.getChampionByName(target)
        if (targetChamp != null && type in listOf("Ventaja", "Debilidad")) {
            return ChampionMatchupCoaching.sovereignFeedback(champion, activeRole, lang, targetChamp)
        }
        return when (type) {
            "Situacional" -> {
                val advice = com.example.data.SituationalItemAdvisor.getAdvice(target)
                val localizedName = trStr(lang, advice.name)
                val localizedCat = trStr(lang, advice.categoryName)
                val localizedPurpose = trStr(lang, advice.purpose)
                val localizedKeyEffect = trStr(lang, advice.keyEffect)
                val localizedTip = trStr(lang, advice.recommendationTip)
                if (isPt) {
                    "️ **$localizedName ($localizedCat)**\n\n$localizedPurpose\n\n• **Eficaz contra:** ${advice.bestAgainst.map { trStr("pt", it) }.joinToString(", ")}\n• **Efeito chave:** $localizedKeyEffect\n\n **Dica:** $localizedTip"
                } else {
                    "️ **$localizedName ($localizedCat)**\n\n$localizedPurpose\n\n• **Efectivo contra:** ${advice.bestAgainst.joinToString(", ")}\n• **Efecto clave:** $localizedKeyEffect\n\n **Consejo:** $localizedTip"
                }
            }
            else -> {
                val ownPlan = ChampionMatchupCoaching.championPlan(champion, activeRole, lang)
                val allyPlan = targetChamp?.let { ChampionMatchupCoaching.championPlan(it, it.primaryRole, lang) }.orEmpty()
                ownPlan + "\n\n" + allyPlan
            }
        }
    }

    fun generateTacticalAnalysis(champion: Champion, activeRole: LaneRole, lang: String): String {
        return ChampionMatchupCoaching.championPlan(champion, activeRole, lang)
    }

    fun generateTacticalAdvice(champion: Champion, activeRole: LaneRole, lang: String): String {
        val language = AppLanguage.normalize(lang)
        val key = AdviceKey(champion.id, champion.name, champion.tacticalAdvice, champion.skills, activeRole, language)
        synchronized(adviceCache) { adviceCache[key]?.let { return it } }
        val pt = language == "pt"
        val me = champion.getLocalizedName(language)
        val h1 = champion.skills.firstOrNull { it.slot == "1" }
        val h4 = champion.skills.firstOrNull { it.slot == "4" }
        val detail = listOfNotNull(h1, h4).joinToString(" ") {
            "H${it.slot} · ${it.getLocalizedName(language)}: ${it.getLocalizedDescription(language).substringBefore(". ")}."
        }
        val specific = trStr(language, champion.tacticalAdvice)
        val result = if (pt) "Diagnóstico do erro/situação: $me · ${activeRole.getLocalizedName(language)}; confirme recursos e recargas antes de comprometer a troca.\n" +
            "Decisão Soberano: $specific\nMicro e Macro detalhe: $detail\n" +
            "Regra aplicável: resolva a onda ou o campo antes de rotacionar; sem prioridade, troque a pressão para o lado oposto."
        else "Diagnóstico del error/situación: $me · ${activeRole.getLocalizedName(language)}; confirma recursos y enfriamientos antes de comprometer el intercambio.\n" +
            "Decisión Soberano: $specific\nMicro y Macro detalle: $detail\n" +
            "Regla aplicable: resuelve la oleada o el campamento antes de rotar; sin prioridad, intercambia presión hacia el lado opuesto."
        synchronized(adviceCache) { adviceCache[key] = result }
        return result
    }
}
