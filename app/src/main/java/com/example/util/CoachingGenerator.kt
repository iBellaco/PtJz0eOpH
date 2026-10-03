package com.example.util

import com.example.model.Champion
import com.example.model.LaneRole

object CoachingGenerator {
    
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
        return ChampionMatchupCoaching.sovereignFeedback(champion, activeRole, lang)
    }
}
