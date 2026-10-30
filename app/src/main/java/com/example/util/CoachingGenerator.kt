package com.example.util

import com.example.model.Champion
import com.example.model.LaneRole

object CoachingGenerator {
    private data class AdviceKey(val champion: Champion, val role: LaneRole, val language: String, val enemy: Champion?)
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

    fun generateTacticalAdvice(champion: Champion, activeRole: LaneRole, lang: String, enemy: Champion? = null): String {
        val language = AppLanguage.normalize(lang)
        val key = AdviceKey(champion, activeRole, language, enemy)
        synchronized(adviceCache) { adviceCache[key]?.let { return it } }
        val rawResult = ChampionMatchupCoaching.sovereignFeedback(champion, activeRole, language, enemy)
        val uncertainty = if (language == "pt")
            "Plano condicionado à seleção: não observamos vida, ouro, ondas nem recargas. Confirme a rota estimada do rival antes de aplicar o confronto."
        else "Plan condicionado al draft: no observamos vida, oro, oleadas ni enfriamientos. Confirma la línea estimada del rival antes de aplicar el enfrentamiento."
        val result = DraftRecommendationText.clean(rawResult + "\n" + uncertainty)
        synchronized(adviceCache) { adviceCache[key] = result }
        return result
    }
}
