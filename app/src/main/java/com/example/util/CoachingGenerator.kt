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

        val mySkill = champion.skills.find { it.slot == "1" }?.name ?: champion.skills.firstOrNull()?.name ?: "habilidades"
        val isMeRanged = champion.isRanged
        val isTargetRanged = targetChamp?.isRanged ?: false
        
        return when (type) {
            "Ventaja" -> {
                if (isPt) {
                    "${champion.name} tem forte vantagem sobre $target. Puna-o com $mySkill sempre que tentar farmar."
                } else {
                    if (targetChamp != null) {
                        if (isMeRanged && !isTargetRanged) {
                            "${champion.name} puede abusar de su rango contra $target. Castígalo con $mySkill cada vez que intente farmear, y mantén la distancia para ganar la línea sin recibir daño ${targetDamage.lowercase()}."
                        } else if (!isMeRanged && isTargetRanged) {
                            "${champion.name} tiene un all-in superior al de $target. Soporta el desgaste inicial y usa $mySkill para acortar distancias; una vez encima, no podrá sobrevivir a tu daño."
                        } else {
                            "${champion.name} domina este enfrentamiento. Aprovecha el enfriamiento de las habilidades de $target para intercambiar daño con $mySkill, forzándolo a jugar bajo su torre."
                        }
                    } else {
                        "${champion.name} tiene un kit superior frente a $target. Castiga sus errores de posicionamiento para conseguir prioridad de mapa."
                    }
                }
            }
            "Debilidad" -> {
                if (isPt) {
                    "$target é extremamente letal contra ${champion.name}. Jogue recuado e reserve $mySkill para defesa."
                } else {
                    if (targetChamp != null) {
                        if (!isMeRanged && isTargetRanged) {
                            "$target te castigará constantemente por tu falta de rango. Sacrifica algunos súbditos si es necesario, usa $mySkill solo para asegurar oro seguro y espera la rotación de tu jungla o el Fruto de Miel (1:15 min)."
                        } else if (targetDamage.equals("Mágico", true)) {
                            "El daño mágico explosivo de $target es letal para ${champion.name}. Considera botas de resistencia, evita los intercambios largos y guarda $mySkill para protegerte o escapar."
                        } else {
                            "$target supera a ${champion.name} en 1vs1. Respeta su daño ${targetDamage.lowercase()}, no fuerces peleas innecesarias y maximiza tu farmeo bajo torre."
                        }
                    } else {
                        "${champion.name} sufre mucho contra el kit de $target. Juega de forma conservadora y pide rotaciones tempranas."
                    }
                }
            }
            "Situacional" -> {
                val advice = com.example.data.SituationalItemAdvisor.getAdvice(target)
                val localizedName = trStr(advice.name, lang)
                val localizedCat = trStr(advice.categoryName, lang)
                val localizedPurpose = trStr(advice.purpose, lang)
                val localizedKeyEffect = trStr(advice.keyEffect, lang)
                val localizedTip = trStr(advice.recommendationTip, lang)
                if (isPt) {
                    "️ **$localizedName ($localizedCat)**\n\n$localizedPurpose\n\n• **Eficaz contra:** ${advice.bestAgainst.joinToString(", ")}\n• **Efeito chave:** $localizedKeyEffect\n\n **Dica:** $localizedTip"
                } else {
                    "️ **$localizedName ($localizedCat)**\n\n$localizedPurpose\n\n• **Efectivo contra:** ${advice.bestAgainst.joinToString(", ")}\n• **Efecto clave:** $localizedKeyEffect\n\n **Consejo:** $localizedTip"
                }
            }
            else -> { // Sinergia
                if (isPt) {
                    "Excelente sinergia com $target. A combinação de habilidades garante grande vantagem nas lutas de equipe e objetivos neutros."
                } else {
                    "Excelente sinergia con $target. La combinación de control de masas, daño y protección de ambos campeones garantiza una superioridad aplastante en peleas de equipo y toma de objetivos."
                }
            }
        }
    }

    fun generateTacticalAnalysis(champion: Champion, activeRole: LaneRole, lang: String): String {
        val cleanLang = lang.lowercase().trim()
        val isPt = cleanLang.startsWith("pt")
        val roleStr = activeRole.displayName
        
        val qSkill = champion.skills.find { it.slot == "1" }?.name ?: if (isPt) "suas habilidades" else "sus habilidades"
        val ultSkill = champion.skills.find { it.slot == "4" }?.name ?: if (isPt) "sua ultimate" else "sua definitiva"
        
        val base = if (isPt) {
            when (activeRole) {
                LaneRole.TOP -> if (champion.isRanged) "**Fase Inicial (Rotas de Wild Rift):** Na ${roleStr}, ${champion.name} deve abusar do seu alcance usando $qSkill para desgastar oponentes corpo a corpo e controlar a onda." else "**Fase Inicial (Rotas de Wild Rift):** Na ${roleStr}, ${champion.name} deve jogar em torno dos tempos de recarga de $qSkill, buscando trocas curtas e garantindo a visão do rio."
                LaneRole.JUNGLE -> "**Fase de Limpeza:** Na ${roleStr}, ${champion.name} deve priorizar o farm eficiente e buscar emboscadas (ganks) apoiando-se em $qSkill para garantir vantagens iniciais."
                LaneRole.MID -> "**Fase Inicial (Rotas de Wild Rift):** Na ${roleStr}, a prioridade de ${champion.name} é conseguir o empurre (prio) usando $qSkill para poder rotacionar para os objetivos do rio ou ajudar o caçador."
                LaneRole.ADC -> "**Fase Inicial (Rotas de Wild Rift):** Na ${roleStr}, ${champion.name} depende de um posicionamento seguro. Use $qSkill para garantir tropas e punir erros de posicionamento da dupla rival."
                LaneRole.SUPPORT -> "**Fase Inicial (Rotas de Wild Rift):** Como ${roleStr}, ${champion.name} dita o ritmo das trocas. Use $qSkill para pressionar os rivais, ganhar prioridade de nível 2 e proteger seu atirador."
            }
        } else {
            when (activeRole) {
                LaneRole.TOP -> if (champion.isRanged) "**Fase Temprana (Línea de Wild Rift):** En la ${roleStr}, ${champion.name} debe abusar de su rango usando $qSkill para desgastar a los oponentes cuerpo a cuerpo y controlar la oleada." else "**Fase Temprana (Línea de Wild Rift):** En la ${roleStr}, ${champion.name} debe jugar alrededor de los enfriamientos de $qSkill, buscando intercambios cortos y controlando el Escurridizo del río (1:15 min) y la Flor del Adivino."
                LaneRole.JUNGLE -> "**Ruta de Jungla y Control de Río:** En la ${roleStr}, ${champion.name} debe priorizar el farmeo eficiente y buscar emboscadas (ganks) clave apoyándose en $qSkill para asegurar ventajas tempranas."
                LaneRole.MID -> "**Fase Temprana (Línea de Wild Rift):** En la ${roleStr}, la prioridad de ${champion.name} es conseguir el empuje (prio) usando $qSkill para poder rotar a los objetivos del río o emboscar junto al junglero."
                LaneRole.ADC -> "**Fase Temprana (Línea de Wild Rift):** En la ${roleStr}, ${champion.name} depende de un posicionamiento seguro. Utiliza $qSkill para asegurar súbditos y castigar los errores de posicionamiento del dúo rival."
                LaneRole.SUPPORT -> "**Fase Temprana (Línea de Wild Rift):** Como ${roleStr}, ${champion.name} dicta el ritmo de los intercambios. Usa $qSkill para presionar a los rivales, ganar prioridad de nivel 2 y proteger a tu tirador."
            }
        }
        
        val mid = if (isPt) {
            when (activeRole) {
                LaneRole.TOP -> if (champion.isFrontline) "**Meio/Fim de Jogo (Macro Wild Rift):** Nas lutas de equipe, ${champion.name} funciona como a principal linha de frente. Absorva o dano e busque usar $ultSkill em momentos críticos." else "**Meio/Fim de Jogo (Macro Wild Rift):** Empurre sua rota para pressão dividida, mas lembre-se que o mapa é curto. Agrupe rapidamente para os objetivos e flanqueie com $ultSkill."
                LaneRole.JUNGLE -> "**Meio/Fim de Jogo (Macro Wild Rift):** O mapa é pequeno e as rotações são rápidas. Priorize garantir o Dragão ou Arauto cedo, e use seu $ultSkill para conseguir emboscadas decisivas."
                LaneRole.MID -> "**Meio/Fim de Jogo (Macro Wild Rift):** Neste jogo de ritmo acelerado, uma emboscada no late game é fatal. Mova-se com sua equipe e use $ultSkill de forma explosiva em espaços fechados da selva."
                LaneRole.ADC -> "**Meio/Fim de Jogo (Macro Wild Rift):** Agrupe-se com seu suporte. Os cercos às torres de inibidor em Wild Rift são muito rápidos; posicione-se atrás da linha de frente e cause dano com $ultSkill."
                LaneRole.SUPPORT -> "**Meio/Fim de Jogo (Macro Wild Rift):** Negue a visão inimiga com a Lente Detectora no rio. Use o baixo tempo de recarga das botas encantadas e seu $ultSkill para virar lutas a seu favor."
            }
        } else {
            when (activeRole) {
                LaneRole.TOP -> if (champion.isFrontline) "**Juego Medio/Tardío (Macro Wild Rift):** En peleas grupales, ${champion.name} funciona como la principal línea frontal (frontline). Absorbe daño y busca usar $ultSkill en momentos críticos por objetivos." else "**Juego Medio/Tardío (Macro Wild Rift):** Empuja tu línea para aplicar presión dividida, pero recuerda que el mapa es corto: agrupa rápidamente a pie para los objetivos (Heraldo/Barón) y flanquea con tu $ultSkill a los objetivos vulnerables."
                LaneRole.JUNGLE -> "**Juego Medio/Tardío (Macro Wild Rift):** El mapa es pequeño y las rotaciones son rápidas. Prioriza asegurar el Dragón o Heraldo temprano, y usa tu $ultSkill para conseguir emboscadas clave que permitan a tu equipo tirar torres e invadir la jungla."
                LaneRole.MID -> "**Juego Medio/Tardío (Macro Wild Rift):** En este juego de ritmo acelerado, una emboscada tardía es fatal. Muévete siempre con tu equipo por el río o la jungla y usa tu $ultSkill de forma explosiva en espacios cerrados (pasillos de jungla) para borrar a los rivales."
                LaneRole.ADC -> "**Juego Medio/Tardío (Macro Wild Rift):** Agrupa con tu soporte lo antes posible. Los asedios a torres de inhibidor en Wild Rift son rápidos. Posiciónate seguro detrás de tu línea frontal y castiga con $ultSkill sin arriesgar tu vida."
                LaneRole.SUPPORT -> "**Juego Medio/Tardío (Macro Wild Rift):** Deniega la visión enemiga con Lente Revelador en el río antes de los objetivos neutrales (Dragones, Barón). Usa el corto enfriamiento de las botas encantadas y tu $ultSkill para salvar a tu ADC o enganchar al acarreador rival."
            }
        }
        
        return base + "\n\n" + mid
    }

    fun generateTacticalAdvice(champion: Champion, activeRole: LaneRole, lang: String): String {
        val cleanLang = lang.lowercase().trim()
        val isPt = cleanLang.startsWith("pt")
        
        val specificAdvice = if (champion.tacticalAdvice.isNotBlank()) champion.tacticalAdvice else ""
        
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