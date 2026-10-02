package com.example.util

import com.example.model.Champion
import com.example.model.LaneRole

object CoachingGenerator {
    
    fun generateMatchupReason(champion: Champion, activeRole: LaneRole, target: String, type: String, lang: String): String {
        val isPt = AppLanguage.normalize(lang) == "pt"
        val targetChamp = com.example.data.WildRiftRepository.getChampionByName(target)
        if (targetChamp != null && type in listOf("Ventaja", "Debilidad")) {
            val plan = ChampionMatchupCoaching.forDuel(champion, targetChamp, activeRole, lang)
            return if (type == "Ventaja") plan.early + "\n\n" + plan.winCondition else plan.rival + "\n\n" + plan.ultimate
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
        val specific = trStr(language, champion.tacticalAdvice).ifBlank {
            champion.skills.firstOrNull { it.slot == "1" }?.getLocalizedDescription(language).orEmpty()
        }.ifBlank {
            if (language == "pt") "Descrição não disponível para este campeão." else "Descripción no disponible para este campeón."
        }
        return "${champion.getLocalizedName(language)} · ${activeRole.getLocalizedName(language)}: $specific"
    }
}
