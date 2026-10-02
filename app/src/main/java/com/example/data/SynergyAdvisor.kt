package com.example.data

import com.example.model.Champion
import com.example.model.DamageType
import com.example.model.LaneRole
import com.example.util.trStr

data class SynergyTeammate(
    val championName: String,
    val championId: String,
    val role: LaneRole,
    val tier: String,
    val winrate: Double,
    val synergyScore: Int, // e.g. 98%
    val category: String, // "Wombo Combo", "Iniciación & CC", "Peel & Buffer", "Sinergia de Dúo", "Gank Setup & Roam", "Dive & Flanqueo"
    val badgeIcon: String, // "🌪️", "🛡️", "⚔️", "🎯", "⚡", "🏹"
    val synergyTitle: String, // e.g. "Combo Aéreo y Cadena de CC"
    val tacticalReason: String,
    val comboTips: String,
    val avatarUrl: String = ""
)

data class ChampionSynergyProfile(
    val archetype: String,
    val archetypeBadge: String,
    val archetypeDesc: String,
    val coreStrengths: List<String>,
    val bestTeammates: List<SynergyTeammate>
)

object SynergyAdvisor {

    private data class SpecificSynergyEntry(
        val teammateName: String,
        val preferredRole: LaneRole,
        val score: Int,
        val category: String,
        val icon: String,
        val titleEs: String,

        val titlePt: String,
        val reasonEs: String,

        val reasonPt: String,
        val comboEs: String,

        val comboPt: String
    )

    // Catálogo profundo de pares sinérgicos Soberano para Wild Rift
    private val synergyDatabase: Map<String, List<SpecificSynergyEntry>> = mapOf(
        "yasuo" to listOf(
            SpecificSynergyEntry(
                teammateName = "Malphite",
                preferredRole = LaneRole.TOP,
                score = 99,
                category = "Wombo Combo",
                icon = "🌪️",
                titleEs = "Iniciación Aérea Imparable",

                titlePt = "Iniciação Aérea Imparável",
                reasonEs = "La Definitiva (Fuerza Imparable) de Malphite proyecta por los aires a todo el equipo enemigo en área, activando al instante la Definitiva de Yasuo (Último Aliento) para una aniquilación masiva inmediata.",

                reasonPt = "A Ultimate do Malphite projeta os inimigos no ar em área, ativando instantaneamente a Ultimate do Yasuo.",
                comboEs = "Malphite inicia con Definitiva en el centro del combate -> Yasuo presiona Definitiva en el aire + H1 + H3 al aterrizar.",

                comboPt = "Malphite inicia com Ultimate -> Yasuo ativa Ultimate no ar + H1 + H3 ao aterrissar."
            ),
            SpecificSynergyEntry(
                teammateName = "Diana",
                preferredRole = LaneRole.JUNGLE,
                score = 97,
                category = "Wombo Combo",
                icon = "🌙",
                titleEs = "Vórtice Lunar & Remate Aéreo",

                titlePt = "Vórtice Lunar e Finalização Aérea",
                reasonEs = "La Definitiva de Diana agrupa a todos los rivales y los suspende por los aires, permitiendo a Yasuo rematar a múltiples carries con su Definitiva.",

                reasonPt = "A Ultimate da Diana puxa e suspende múltiplos inimigos, permitindo que Yasuo finalize com sua Ultimate.",
                comboEs = "Diana entra con H3 y activa Definitiva -> Yasuo encadena Definitiva + H1 giratorio.",

                comboPt = "Diana avança com H3 e usa Ultimate -> Yasuo comba Ultimate + H1 giratório."
            ),
            SpecificSynergyEntry(
                teammateName = "Gragas",
                preferredRole = LaneRole.MID,
                score = 95,
                category = "Iniciación & CC",
                icon = "🍺",
                titleEs = "Barril Explosivo & Desplazamiento",

                titlePt = "Barril Explosivo e Deslocamento",
                reasonEs = "El empujón con la H3 (Lanzamiento de Barriga) y la Definitiva de Gragas desplazan y levantan a los enemigos, generando ventanas continuas para la Definitiva de Yasuo.",

                reasonPt = "A H3 e a Ultimate de Gragas lançam os inimigos pelo ar, ativando a Ultimate de Yasuo.",
                comboEs = "Gragas lanza Definitiva hacia la posición de Yasuo -> Yasuo castiga en el aire.",

                comboPt = "Gragas lança Ultimate em direção a Yasuo -> Yasuo ataca no ar."
            ),
            SpecificSynergyEntry(
                teammateName = "Alistar",
                preferredRole = LaneRole.SUPPORT,
                score = 96,
                category = "Iniciación & CC",
                icon = "🐂",
                titleEs = "Combo Pulverizar en Cadena",

                titlePt = "Combo Pulverizar em Cadeia",
                reasonEs = "El clásico combo H2 (Testarazo) + H1 (Pulverizar) de Alistar levanta a los objetivos clave de forma segura para que Yasuo salte con su Definitiva.",

                reasonPt = "O combo H2 + H1 do Alistar levanta alvos com segurança para a Ultimate do Yasuo.",
                comboEs = "Alistar H2 + H1 sobre el carry enemigo -> Yasuo presiona Definitiva inmediatamente.",

                comboPt = "Alistar H2 + H1 no carry inimigo -> Yasuo ativa Ultimate imediatamente."
            )
        ),
        "samira" to listOf(
            SpecificSynergyEntry(
                teammateName = "Nautilus",
                preferredRole = LaneRole.SUPPORT,
                score = 99,
                category = "Sinergia de Dúo",
                icon = "⚓",
                titleEs = "Cadena de CC & Cargas de Estilo",

                titlePt = "Cadeia de CC e Cargas de Estilo",
                reasonEs = "Cada inmovilización de Nautilus (Pasiva, H1, H3 y Definitiva) activa la Pasiva de Samira (Impulso Temerario), otorgándole desplazamiento instantáneo y facilitando llegar a rango 'S' en segundos.",

                reasonPt = "Qualquer controle de grupo de Nautilus ativa a passiva da Samira para atingir rank 'S' rápido.",
                comboEs = "Nautilus H1 + Básico -> Samira ataca con pasiva + H1 + H2 + H3 -> Definitiva (Gatillo Infernal).",

                comboPt = "Nautilus H1 + Ataque -> Samira ativa passiva + H1 + H2 + H3 -> Ultimate."
            ),
            SpecificSynergyEntry(
                teammateName = "Leona",
                preferredRole = LaneRole.SUPPORT,
                score = 98,
                category = "Iniciación & CC",
                icon = "☀️",
                titleEs = "All-In Letal a Nivel 2 y 5",

                titlePt = "All-In Letal nos Níveis 2 e 5",
                reasonEs = "El bloqueo solar de Leona con H3 + H1 y Definitiva inmoviliza al dúo rival, dejando a Samira girar con su Definitiva sin riesgo de ser interrumpida.",

                reasonPt = "O atordoamento contínuo de Leona permite que Samira canalize sua Ultimate sem ser interrompida.",
                comboEs = "Leona inicia con Definitiva + H3 -> Samira entra con H3 y activa Definitiva.",

                comboPt = "Leona inicia com Ultimate + H3 -> Samira entra com H3 e canaliza Ultimate."
            ),
            SpecificSynergyEntry(
                teammateName = "Amumu",
                preferredRole = LaneRole.JUNGLE,
                score = 96,
                category = "Wombo Combo",
                icon = "🩹",
                titleEs = "Maldición en Área & Resets",

                titlePt = "Maldição em Área e Resets",
                reasonEs = "La Definitiva de Amumu aturde a los 5 rivales, otorgando el escenario soñado para que Samira salte al medio con H3 y borre al equipo entero con su Definitiva.",

                reasonPt = "A Ultimate de Amumu atordoa todos os inimigos, criando o cenário perfeito para a Ultimate de Samira.",
                comboEs = "Amumu H1 + Definitiva -> Samira H3 al grupo + Definitiva.",

                comboPt = "Amumu H1 + Ultimate -> Samira H3 no grupo + Ultimate."
            )
        ),
        "jinx" to listOf(
            SpecificSynergyEntry(
                teammateName = "Lulu",
                preferredRole = LaneRole.SUPPORT,
                score = 99,
                category = "Peel & Buffer",
                icon = "🧚",
                titleEs = "Hiperacreador Potenciado (Kog/Jinx Meta)",

                titlePt = "Hipercarregador Fortalecido",
                reasonEs = "La H2 (Capricho) de Lulu otorga velocidad de ataque y movimiento masivos, mientras que su Definitiva (Crecimiento Salvaje) salva a Jinx de asesinos con derribo aéreo y vida extra.",

                reasonPt = "O H2 de Lulu concede velocidade de ataque e a Ultimate protege Jinx com vida extra e controle de grupo.",
                comboEs = "Lulu bufa con H2 + H3 a Jinx -> Jinx activa Pasiva (¡A divertirse!) y arrasa la teamfight con H1 cohetes.",

                comboPt = "Lulu buffa com H2 + H3 -> Jinx ativa passiva e destrói lutas com H1 de foguetes."
            ),
            SpecificSynergyEntry(
                teammateName = "Thresh",
                preferredRole = LaneRole.SUPPORT,
                score = 97,
                category = "Peel & Iniciación",
                icon = "⛓️",
                titleEs = "Linterna de Rescate & Cadenas CC",

                titlePt = "Lanterna de Fuga e Correntes",
                reasonEs = "La linterna (H2) de Thresh compensa la falta de movilidad de Jinx, y su gancho (H1) le permite encadenar sus trampas H3 (Mascafuegos) debajo del objetivo.",

                reasonPt = "A lanterna de Thresh dá mobilidade à Jinx e o gancho permite encaixar as armadilhas H3 perfeitamente.",
                comboEs = "Thresh conecta H1 -> Jinx coloca H3 trampas bajo los pies del rival -> daño continuo.",

                comboPt = "Thresh acerta H1 -> Jinx coloca armadilhas H3 embaixo do alvo -> dano garantido."
            ),
            SpecificSynergyEntry(
                teammateName = "Malphite",
                preferredRole = LaneRole.TOP,
                score = 94,
                category = "Frontline & Engage",
                icon = "🪨",
                titleEs = "Frontline de Hierro & Cohetes de Remate",

                titlePt = "Linha de Frente e Foguetes Finais",
                reasonEs = "Malphite absorbe todo el daño enemigo y fuerza a los rivales a agruparse, permitiendo a Jinx impactar su Definitiva (¡Supermegacohete Mortal!) y daño de cohetes en área.",

                reasonPt = "Malphite absorbe o dano e agrupa inimigos para o dano em área do foguete e Ultimate de Jinx.",
                comboEs = "Malphite impacta Definitiva -> Jinx dispara Definitiva para asegurar la primera baja y activar su pasiva.",

                comboPt = "Malphite acerta Ultimate -> Jinx dispara Ultimate para resetar a passiva."
            )
        ),
        "lucian" to listOf(
            SpecificSynergyEntry(
                teammateName = "Nami",
                preferredRole = LaneRole.SUPPORT,
                score = 99,
                category = "Sinergia de Dúo",
                icon = "🌊",
                titleEs = "Electrocutar & Ráfaga de Bendición",

                titlePt = "Explosão de Bênção e Eletrocutar",
                reasonEs = "La H3 (Bendición de la Marea) de Nami se activa 3 veces en un solo segundo gracias a los disparos dobles de la Pasiva (Pistolero Iluminado) de Lucian, infligiendo ralentización y daño mágico devastador.",

                reasonPt = "O H3 de Nami é ativado instantaneamente com o tiro duplo da passiva de Lucian, causando dano massivo.",
                comboEs = "Nami aplica H3 a Lucian -> Lucian H3 + Pasiva + H1 + Pasiva (daño explosivo en 0.5s).",

                comboPt = "Nami aplica H3 no Lucian -> Lucian H3 + Passiva + H1 + Passiva (explosão em 0.5s)."
            ),
            SpecificSynergyEntry(
                teammateName = "Braum",
                preferredRole = LaneRole.SUPPORT,
                score = 98,
                category = "Sinergia de Dúo",
                icon = "🛡️",
                titleEs = "Aturdimiento Instantáneo de Pasiva",

                titlePt = "Atordoamento Instantâneo da Passiva",
                reasonEs = "Los disparos dobles de Lucian aplican las 4 marcas de la Pasiva (Golpes Conmocionantes) de Braum en menos de un segundo, congelando al rival sin posibilidad de respuesta.",

                reasonPt = "Os tiros duplos de Lucian ativam as 4 marcas da passiva de Braum em menos de 1 segundo.",
                comboEs = "Braum H1 + ataque -> Lucian H3 + Pasiva (aturdimiento instantáneo) + H1.",

                comboPt = "Braum H1 + ataque -> Lucian H3 + Passiva (stun instantâneo) + H1."
            )
        ),
        "miss_fortune" to listOf(
            SpecificSynergyEntry(
                teammateName = "Amumu",
                preferredRole = LaneRole.JUNGLE,
                score = 99,
                category = "Wombo Combo",
                icon = "🩹",
                titleEs = "Maldición & Balacera Implacable",

                titlePt = "Maldição e Metralhadora Mortal",
                reasonEs = "La Definitiva de Amumu inmoviliza a todo el equipo rival en el río o fosas de Dragón/Barón, asegurando que Miss Fortune descargue el 100% de su Definitiva (Balacera).",

                reasonPt = "A Ultimate de Amumu prende todos os inimigos dentro de toda a duração da Ultimate de Miss Fortune.",
                comboEs = "Amumu entra con Definitiva en cuello de botella -> Miss Fortune canaliza Definitiva completa.",

                comboPt = "Amumu usa Ultimate em local fechado -> Miss Fortune canaliza Ultimate completa."
            ),
            SpecificSynergyEntry(
                teammateName = "Jarvan IV",
                preferredRole = LaneRole.JUNGLE,
                score = 97,
                category = "Wombo Combo",
                icon = "🚩",
                titleEs = "Cataclismo & Jaula de Balas",

                titlePt = "Cataclismo e Gaiola de Balas",
                reasonEs = "La Definitiva (Cataclismo) de Jarvan IV encierra a los carries enemigos sin destello dentro de una arena circular, donde la Definitiva de Miss Fortune es ineludible.",

                reasonPt = "A Ultimate de Jarvan IV prende os inimigos em uma arena onde a Ultimate de Miss Fortune não pode ser esquivada.",
                comboEs = "Jarvan IV E-Q + Definitiva -> Miss Fortune activa Definitiva sobre el cráter.",

                comboPt = "Jarvan IV H3-H1 + Ultimate -> Miss Fortune usa Ultimate sobre a arena."
            ),
            SpecificSynergyEntry(
                teammateName = "Seraphine",
                preferredRole = LaneRole.SUPPORT,
                score = 96,
                category = "Iniciación & CC",
                icon = "🎤",
                titleEs = "Nota Bis & Balacera Coral",

                titlePt = "Encanto Musical e Metralhadora",
                reasonEs = "La Definitiva (Bis) de Seraphine enamora y atrae a los enemigos en línea recta, alineándolos a la perfección para el cono de la Definitiva de Miss Fortune.",

                reasonPt = "A Ultimate de Seraphine alinha os inimigos para o cone devastador da Ultimate de Miss Fortune.",
                comboEs = "Seraphine conecta Definitiva + H3 aturdimiento -> Miss Fortune activa Definitiva.",

                comboPt = "Seraphine acerta Ultimate + H3 -> Miss Fortune ativa Ultimate."
            )
        ),
        "katarina" to listOf(
            SpecificSynergyEntry(
                teammateName = "Amumu",
                preferredRole = LaneRole.JUNGLE,
                score = 98,
                category = "Wombo Combo",
                icon = "🩹",
                titleEs = "Inmovilización Masiva & Resets de Dagas",

                titlePt = "Atordoamento em Massa e Resets",
                reasonEs = "Amumu retiene a los objetivos vulnerables evitando que usen CC contra Katarina, permitiéndole canalizar su Definitiva (Loto Mortal) y conseguir reinicios continuos con su Pasiva.",

                reasonPt = "Amumu desativa o controle de grupo rival para que Katarina canalize a Ultimate com segurança.",
                comboEs = "Amumu Definitiva -> Katarina salta con H3 + H2 + Definitiva.",

                comboPt = "Amumu Ultimate -> Katarina pula com H3 + H2 + Ultimate."
            ),
            SpecificSynergyEntry(
                teammateName = "Malphite",
                preferredRole = LaneRole.TOP,
                score = 97,
                category = "Wombo Combo",
                icon = "🪨",
                titleEs = "Impacto Sísmico & Limpieza de Resets",

                titlePt = "Impacto Sísmico e Limpeza de Lutas",
                reasonEs = "El derribo aéreo masivo de Malphite deja a los enemigos con media vida, el umbral perfecto para que Katarina salte y ejecute en cadena.",

                reasonPt = "A iniciação de Malphite deixa alvos com metade da vida, pronto para Katarina resetar.",
                comboEs = "Malphite Definitiva -> Katarina entra inmediatamente a recoger las bajas.",

                comboPt = "Malphite Ultimate -> Katarina entra para finalizar os abates."
            )
        ),
        "zed" to listOf(
            SpecificSynergyEntry(
                teammateName = "Shen",
                preferredRole = LaneRole.TOP,
                score = 98,
                category = "Dive & Flanqueo",
                icon = "🥷",
                titleEs = "Marca de la Muerte Blindada",

                titlePt = "Marca Fatal com Escudo Ninja",
                reasonEs = "La Definitiva (Mantenerse Unidos) de Shen protege a Zed con un escudo masivo cuando este se lanza a la línea trasera rival con su Definitiva, teletransportando a Shen para rematar con su provocación.",

                reasonPt = "A Ultimate de Shen protege Zed ao mergulhar na linha de trás inimiga.",
                comboEs = "Zed activa Definitiva sobre el carry -> Shen castea Definitiva sobre Zed -> Shen provoca al aterrizar.",

                comboPt = "Zed usa Ultimate no carry -> Shen usa Ultimate no Zed -> Shen provoca com H3."
            ),
            SpecificSynergyEntry(
                teammateName = "Galio",
                preferredRole = LaneRole.MID,
                score = 96,
                category = "Dive & Flanqueo",
                icon = "🏛️",
                titleEs = "Entrada Heroica tras Asesinato",

                titlePt = "Entrada Heroica e Finalização",
                reasonEs = "Cuando Zed aparece detrás del tirador enemigo con Marca de la Muerte, Galio utiliza su Definitiva sobre él, derribando a los protectores enemigos y sellando la pelea.",

                reasonPt = "O mergulho de Zed serve de alvo para a Entrada Heroica de Galio, derrubando os protetores.",
                comboEs = "Zed entra con Definitiva -> Galio presiona Definitiva sobre Zed -> CC masivo + escape seguro.",

                comboPt = "Zed entra com Ultimate -> Galio usa Ultimate no Zed -> controle em área e saída segura."
            )
        ),
        "master_yi" to listOf(
            SpecificSynergyEntry(
                teammateName = "Lulu",
                preferredRole = LaneRole.SUPPORT,
                score = 99,
                category = "Peel & Buffer",
                icon = "🧚",
                titleEs = "Estrategia Funneling / Hipercarry Imparable",

                titlePt = "Hipercarregador Imparável",
                reasonEs = "Lulu aporta escudos constantes, velocidad de ataque y convierte a los atacantes en ardillas con su H2, garantizando que Maestro Yi no sea frenado por el CC rival durante su Definitiva (Imparable).",

                reasonPt = "Lulu fornece escudos, velocidade de ataque e polimorfia para que Master Yi nunca seja parado.",
                comboEs = "Yi activa Definitiva + H3 -> Lulu aplica H2 y H3 sobre Yi + Definitiva al recibir daño.",

                comboPt = "Yi ativa Ultimate + H3 -> Lulu usa H2 e H3 no Yi + Ultimate para protegê-lo."
            ),
            SpecificSynergyEntry(
                teammateName = "Yuumi",
                preferredRole = LaneRole.SUPPORT,
                score = 98,
                category = "Peel & Buffer",
                icon = "🐱",
                titleEs = "Simbionte Letal & Sanación Continua",

                titlePt = "Simbionte Letal e Cura Contínua",
                reasonEs = "Yuumi se vincula a Yi siendo invulnerable, aportándole daño de ataque adaptativo, curaciones aceleradas y enraizado en área con su Definitiva.",

                reasonPt = "Yuumi se conecta ao Yi ficando inalvejável, dando AD adaptativo e velocidade de movimento.",
                comboEs = "Yuumi H3 velocidad de movimiento -> Yi entra con H1 -> Yuumi activa Definitiva para enraizar.",

                comboPt = "Yuumi usa H3 de velocidade -> Yi usa H1 -> Yuumi ativa Ultimate para enraizar."
            )
        ),
        "darius" to listOf(
            SpecificSynergyEntry(
                teammateName = "Thresh",
                preferredRole = LaneRole.SUPPORT,
                score = 97,
                category = "Iniciación & Reposicionamiento",
                icon = "⛓️",
                titleEs = "Linterna de Alcance & Ganchos",

                titlePt = "Entrega por Lanterna e Puxão",
                reasonEs = "Thresh soluciona la debilidad principal de Darius (falta de movilidad) arrojando su linterna (H2) para depositarlo directamente encima de los carries rivales.",

                reasonPt = "Thresh resolve a falta de mobilidade do Darius com a lanterna diretamente nos carries inimigos.",
                comboEs = "Thresh tira linterna hacia atrás -> Darius la toma y jala a los rivales con su H3 (Aprehender).",

                comboPt = "Thresh joga lanterna para trás -> Darius pega e puxa com H3."
            ),
            SpecificSynergyEntry(
                teammateName = "Yuumi",
                preferredRole = LaneRole.SUPPORT,
                score = 96,
                category = "Peel & Buffer",
                icon = "🐱",
                titleEs = "Velocidad de Cazador & Cargas Rápidas",

                titlePt = "Velocidade de Caça e Sangramento",
                reasonEs = "La velocidad de movimiento de Yuumi sumada a su ralentización con H1 permite a Darius alcanzar a cualquier objetivo y acumular sus 5 cargas de Hemorragia.",

                reasonPt = "A velocidade de Yuumi permite que Darius alcance qualquer alvo e atinja 5 cargas de Hemorragia.",
                comboEs = "Yuumi H3 aceleración -> Darius corta distancia con H3 + H1 giratorio + H2 básico.",

                comboPt = "Yuumi H3 aceleração -> Darius aproxima com H3 + H1 + H2."
            )
        )
    )

    private var teammateName_display: String = ""

    fun getSynergyProfile(
        champion: Champion,
        activeRole: LaneRole,
        lang: String = "es"
    ): ChampionSynergyProfile {
        val champId = champion.id.lowercase().trim()
        val allChampions = WildRiftRepository.champions

        // 1. Determinar Arquetipo del Campeón
        val (archetype, badge, desc, strengths) = determineArchetype(champion, activeRole, lang)

        // 2. Extraer Compañeros Específicos de Base de Datos
        val specificEntries = synergyDatabase[champId] ?: emptyList()
        val recommendedTeammates = mutableListOf<SynergyTeammate>()

        specificEntries.forEach { entry ->
            val teammateName = if (entry.teammateName == "Ghost_Yuumi") "Yuumi" else entry.teammateName
            val champData = WildRiftRepository.getChampionByName(teammateName)
            val title = when (lang) {
                "pt" -> entry.titlePt
                else -> entry.titleEs
            }
            val reason = when (lang) {
                "pt" -> entry.reasonPt
                else -> entry.reasonEs
            }
            val combo = when (lang) {
                "pt" -> entry.comboPt
                else -> entry.comboEs
            }

            val iconUrl = champData?.avatarUrl ?: ""
            val tier = champData?.tier ?: "S"
            val winrate = champData?.winrate ?: 52.0

            recommendedTeammates.add(
                SynergyTeammate(
                    championName = teammateName,
                    championId = champData?.id ?: teammateName.lowercase(),
                    role = entry.preferredRole,
                    tier = tier,
                    winrate = winrate,
                    synergyScore = entry.score,
                    category = entry.category,
                    badgeIcon = entry.icon,
                    synergyTitle = title,
                    tacticalReason = reason,
                    comboTips = combo,
                    avatarUrl = iconUrl
                )
            )
        }

        // 3. Complementar con las sinergias listadas en el perfil del campeón
        val declaredSynergies = (champion.synergies + (champion.advantageAgainst.take(1))).distinct()
        for (synName in declaredSynergies) {
            if (recommendedTeammates.any { it.championName.equals(synName, ignoreCase = true) }) continue
            val foundChamp = WildRiftRepository.getChampionByName(synName) ?: continue
            val synRole = if (foundChamp.primaryRole != activeRole) foundChamp.primaryRole else foundChamp.secondaryRoles.firstOrNull() ?: LaneRole.SUPPORT
            val (cat, icon, title, reason, combo) = generateDynamicSynergyDetail(champion, foundChamp, activeRole, synRole, lang)

            recommendedTeammates.add(
                SynergyTeammate(
                    championName = foundChamp.name,
                    championId = foundChamp.id,
                    role = synRole,
                    tier = foundChamp.tier,
                    winrate = foundChamp.winrate,
                    synergyScore = ((foundChamp.winrate + 43.0).toInt()).coerceIn(88, 96),
                    category = cat,
                    badgeIcon = icon,
                    synergyTitle = title,
                    tacticalReason = reason,
                    comboTips = combo,
                    avatarUrl = foundChamp.avatarUrl
                )
            )
        }

        // 4. Si aún tenemos menos de 4 compañeros, buscar campeones Meta S+/S complementarios por rol
        if (recommendedTeammates.size < 4 && allChampions.isNotEmpty()) {
            val complementaryRoles = listOf(LaneRole.SUPPORT, LaneRole.JUNGLE, LaneRole.TOP, LaneRole.MID, LaneRole.ADC)
                .filter { it != activeRole }

            for (compRole in complementaryRoles) {
                if (recommendedTeammates.size >= 4) break
                val metaPick = allChampions
                    .filter { (it.primaryRole == compRole || it.secondaryRoles.contains(compRole)) && it.id != champion.id }
                    .filter { c -> recommendedTeammates.none { it.championId == c.id } }
                    .sortedByDescending { it.winrate }
                    .firstOrNull()

                if (metaPick != null) {
                    val (cat, icon, title, reason, combo) = generateDynamicSynergyDetail(champion, metaPick, activeRole, compRole, lang)
                    recommendedTeammates.add(
                        SynergyTeammate(
                            championName = metaPick.name,
                            championId = metaPick.id,
                            role = compRole,
                            tier = metaPick.tier,
                            winrate = metaPick.winrate,
                            synergyScore = ((metaPick.winrate + 40.0).toInt()).coerceIn(85, 93),
                            category = cat,
                            badgeIcon = icon,
                            synergyTitle = title,
                            tacticalReason = reason,
                            comboTips = combo,
                            avatarUrl = metaPick.avatarUrl
                        )
                    )
                }
            }
        }

        return ChampionSynergyProfile(
            archetype = archetype,
            archetypeBadge = badge,
            archetypeDesc = desc,
            coreStrengths = strengths,
            bestTeammates = recommendedTeammates.sortedByDescending { it.synergyScore }
        )
    }

    private fun determineArchetype(champion: Champion, role: LaneRole, lang: String): Quadruple<String, String, String, List<String>> {
        val isPt = lang.lowercase().startsWith("pt")

        return when {
            champion.isFrontline -> {
                Quadruple(
                    if (isPt) "Iniciador e Linha de Frente" else "Iniciador & Tanque Frontline",
                    "🛡️ FRONTLINE",
                    if (isPt) "Especialista em absorver dano massivo e criar espaço para os atiradores e magos da equipe." else "Especialista en absorber daño masivo, aplicar control de masas duro y abrir espacio para los acarreadores aliados en Wild Rift.",
                    if (isPt) listOf("Iniciação em Área", "Absorção de Dano", "Controle de Objetivos") else listOf("Iniciación en Área", "Absorción de Daño", "Control de Objetivos (Dragones/Barón)")
                )
            }
            role == LaneRole.ADC -> {
                Quadruple(
                    if (isPt) "Hipercarregador de DPS Contínuo" else "Hiperacarreador de Daño Continuo (DPS)",
                    "🏹 DPS CARRY",
                    if (isPt) "Principal fonte de dano físico à distância. Escala fortemente com itens e requer proteção contínua." else "Principal fuente de daño físico a distancia del equipo. Escala exponencialmente con objetos y requiere protección y peel constante.",
                    if (isPt) listOf("Dano Crítico Explosivo", "Destruição de Torres", "Poder no Late Game") else listOf("Daño Crítico Explosivo", "Destrucción de Torretas", "Poder en Peleas Tardías")
                )
            }
            role == LaneRole.SUPPORT -> {
                Quadruple(
                    if (isPt) "Suporte Tático e Protetor" else "Amplificador Táctico & Protector",
                    "✨ UTILIDAD",
                    if (isPt) "Multiplica a eficácia dos carregadores através de escudos, curas e controle de visão." else "Multiplica la eficacia de los carries mediante escudos, curaciones, visión estratégica y mitigación de amenazas.",
                    if (isPt) listOf("Peel para Atiradores", "Controle de Sentinelas", "Curas e Escudos") else listOf("Peel a Tiradores", "Control de Visión (Lente/Wards)", "Curación y Escudos")
                )
            }
            champion.damageType == DamageType.MAGIC -> {
                Quadruple(
                    if (isPt) "Mago de Controle e Explosão AP" else "Mago de Control & Ráfaga AP",
                    "🔮 BURST AP",
                    if (isPt) "Capaz de deletar alvos frágeis e controlar áreas estrechas com dano mágico massivo." else "Capaz de borrar objetivos frágiles en segundos y zonificar áreas estrechas del mapa con daño mágico masivo.",
                    if (isPt) listOf("Dano em Área Massivo", "Controle de Corredores", "Prioridade de Onda") else listOf("Daño en Área Masivo", "Control de Corredores de Jungla", "Prioridad de Empuje")
                )
            }
            else -> {
                Quadruple(
                    if (isPt) "Duelista de Flanco e Impacto" else "Duelista de Impacto & Flanqueo",
                    "⚔️ DUELISTA",
                    if (isPt) "Especialista em lutas 1v1, isolamento de alvos e escaramuças rápidas." else "Especialista en duelos 1vs1, aislamiento de objetivos y escaramuzas rápidas por el río y la jungla.",
                    if (isPt) listOf("Pressão Dividida", "Execução de Alvos", "Mobilidade e Flancos") else listOf("Presión Dividida (Split-push)", "Ejecución de Carries", "Movilidad y Flanqueos")
                )
            }
        }
    }

    private fun generateDynamicSynergyDetail(
        source: Champion,
        partner: Champion,
        sourceRole: LaneRole,
        partnerRole: LaneRole,
        lang: String
    ): Quintuple<String, String, String, String, String> {
        val isPt = lang.lowercase().startsWith("pt")
        val partnerName = partner.getLocalizedName(lang)
        val sourceName = source.getLocalizedName(lang)

        val pSkill = partner.skills.find { it.slot == "1" }?.getLocalizedName(lang) ?: trStr(lang, "habilidades")
        val pUlt = partner.skills.find { it.slot == "4" }?.getLocalizedName(lang) ?: trStr(lang, "Definitiva")
        val sSkill = source.skills.find { it.slot == "1" }?.getLocalizedName(lang) ?: trStr(lang, "habilidades")

        return if (partner.isFrontline) {
            Quintuple(
                "Iniciación & Frontline",
                "🛡️",
                if (isPt) "Iniciação e Espaço Seguro" else "Iniciación de Tanque & Espacio Seguro",
                if (isPt) "$partnerName absorve habilidades inimigas e inicia com $pUlt, permitindo que $sourceName use $sSkill livremente." else "$partnerName absorbe las habilidades rivales e inicia con su $pUlt, permitiendo que $sourceName conecte su $sSkill con total libertad.",
                if (isPt) "$partnerName usa $pSkill/$pUlt -> $sourceName finaliza os alvos." else "$partnerName conecta $pSkill/$pUlt -> $sourceName castiga a los objetivos inmovilizados."
            )
        } else if (partnerRole == LaneRole.SUPPORT) {
            Quintuple(
                "Peel & Sinergia de Dúo",
                "✨",
                if (isPt) "Proteção e Amplificação de Dano" else "Protección y Amplificación de Daño",
                if (isPt) "O kit de utilidade de $partnerName protege $sourceName contra assassinos e amplifica seu dano." else "El kit de utilidad de $partnerName protege a $sourceName contra asesinos con escudos y control de masas, aumentando su supervivencia en teamfights.",
                if (isPt) "$partnerName aplica escudos -> $sourceName avança agressivo com $sSkill." else "$partnerName aplica escudos/curaciones -> $sourceName avanza agresivo con $sSkill."
            )
        } else if (partnerRole == LaneRole.JUNGLE) {
            Quintuple(
                "Gank Setup & Emboscadas",
                "🎯",
                if (isPt) "Controle de Rio e Ganks Letais" else "Control de Río y Emboscadas Letales",
                if (isPt) "A rotação rápida de $partnerName na selva garante abates rápidos em emboscadas conjuntas." else "La combinación de daño de $partnerName y el control de $sourceName asegura bajas inmediatas en rotaciones al río y peleas de Heraldo/Dragón.",
                if (isPt) "$sourceName pressiona a rota -> $partnerName ganka com $pSkill." else "$sourceName presiona la línea -> $partnerName embosca con $pSkill para cerrar la baja."
            )
        } else {
            Quintuple(
                "Wombo Combo & Daño Mixto",
                "⚡",
                if (isPt) "Cadeia de Dano e Pressão Global" else "Cadena de Daño y Presión en Mapa",
                if (isPt) "$partnerName equilibra o dano da equipe e comita nas lutas de equipe junto com $sourceName." else "$partnerName equilibra el perfil de daño del equipo y combina sus tiempos de recarga con $sourceName para ganar peleas grupales.",
                if (isPt) "Sincronizar Ultimates em corredores da selva perto de objetivos." else "Sincronizar Definitivas en espacios cerrados de jungla cerca de objetivos neutrales."
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
private data class Quintuple<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)
