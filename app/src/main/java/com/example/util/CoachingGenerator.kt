package com.example.util

import com.example.model.Champion
import com.example.model.LaneRole

object CoachingGenerator {
    
    fun generateMatchupReason(champion: Champion, activeRole: LaneRole, target: String, type: String, lang: String): String {
        val cleanLang = lang.lowercase().trim()
        val isPt = cleanLang.startsWith("pt")
        val isEs = !isPt
        val champName = champion.name
        val sourceRole = activeRole
        
        val targetChamp = com.example.data.WildRiftRepository.getChampionByName(target)
        val targetRole = targetChamp?.primaryRole ?: LaneRole.MID
        val targetDamage = targetChamp?.damageType?.displayName ?: "mixto"

        val mySkill = champion.skills.find { it.slot == "1" }?.let { 
            if (isPt) "sua H1 (${it.getLocalizedName("pt")})" else "su H1 (${it.name})" 
        } ?: if (isPt) "sua Habilidade 1 (H1)" else "su Habilidad 1 (H1)"
        val isMeRanged = champion.isRanged
        val isTargetRanged = targetChamp?.isRanged ?: false
        val localizedTargetName = if (isPt) trStr("pt", target) else target
        
        if (targetChamp != null && type in listOf("Ventaja", "Debilidad")) {
            val plan = ChampionMatchupCoaching.forDuel(champion, targetChamp, activeRole, lang)
            return if (type == "Ventaja") plan.early + "\n\n" + plan.winCondition else plan.rival + "\n\n" + plan.ultimate
        }
        return when (type) {
            "Ventaja" -> {
                if (isPt) {
                    "${champion.getLocalizedName("pt")} tem forte vantagem sobre $localizedTargetName. Puna-o com $mySkill sempre que tentar farmar."
                } else {
                    if (targetChamp != null) {
                        if (isMeRanged && !isTargetRanged) {
                            "${champion.name} abusa de su ventaja de rango frente a $target. Castígalo con $mySkill cada vez que intente dar último golpe a súbditos y mantén el espaciado para evitar su daño ${targetDamage.lowercase()}."
                        } else if (!isMeRanged && isTargetRanged) {
                            "${champion.name} tiene un all-in superior al de $target. Soporta el desgaste inicial, acorta distancias con $mySkill y busca la eliminación en tus ventanas de poder tras nivel 3 y 5."
                        } else {
                            "${champion.name} domina este enfrentamiento. Aprovecha los enfriamientos de $target para intercambiar daño con $mySkill y ganar la prioridad de carril."
                        }
                    } else {
                        "${champion.name} tiene un kit superior frente a $target. Castiga sus errores de posicionamiento para conseguir prioridad de mapa."
                    }
                }
            }
            "Debilidad" -> {
                if (isPt) {
                    "$localizedTargetName é extremamente letal contra ${champion.getLocalizedName("pt")}. Jogue recuado e reserve $mySkill para defesa."
                } else {
                    if (targetChamp != null) {
                        if (!isMeRanged && isTargetRanged) {
                            "$target te castigará fuertemente por tu falta de rango. No regales vida por súbditos lejanos, usa $mySkill para farmear de forma segura y espera el Fruto de Miel (1:15 min) o el gank de tu jungla."
                        } else if (targetDamage.equals("Mágico", true)) {
                            "El daño mágico explosivo de $target es letal para ${champion.name}. Prioriza Botas de Resistencia Mágica, evita intercambios largos y reserva $mySkill para reposicionarte."
                        } else {
                            "$target supera a ${champion.name} en 1v1. Respeta su daño ${targetDamage.lowercase()}, congela la oleada cerca de tu torre y maximiza tu economía."
                        }
                    } else {
                        "${champion.name} sufre mucho contra el kit de $target. Juega de forma conservadora y pide rotaciones tempranas."
                    }
                }
            }
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
        val cleanLang = lang.lowercase().trim()
        val isPt = cleanLang.startsWith("pt")
        
        val specificAdvice = if (champion.tacticalAdvice.isNotBlank()) {
            if (isPt) trStr("pt", champion.tacticalAdvice) else champion.tacticalAdvice
        } else ""
        
        val roleAdvice = when (activeRole) {
            LaneRole.ADC -> if (isPt) "Concentre-se em seu posicionamento e acumular ouro." else "Concéntrate en tu posicionamiento y acumular oro."
            LaneRole.SUPPORT -> if (isPt) "Controle a visão (sentinelas) e proteja sua equipe." else "Controla la visión (wards) y protege a tu equipo."
            LaneRole.MID -> if (isPt) "Use sua pressão para rotacionar para os objetivos." else "Usa tu presión para rotar a los objetivos."
            LaneRole.JUNGLE -> if (isPt) "Garanta o controle do mapa e dos Dragões/Arautos." else "Garantiza el control del mapa y los Dragones/Heraldos."
            LaneRole.TOP -> if (isPt) "Mantenha a pressão dividida ou seja a iniciação da equipe." else "Mantén la presión dividida o sé la iniciación del equipo."
        }
        
        return if (specificAdvice.isNotBlank()) "$specificAdvice $roleAdvice" else roleAdvice
    }
}