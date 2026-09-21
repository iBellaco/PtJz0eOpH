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
        val titleEn: String,
        val titlePt: String,
        val reasonEs: String,
        val reasonEn: String,
        val reasonPt: String,
        val comboEs: String,
        val comboEn: String,
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
                titleEn = "Unstoppable Airborne Engage",
                titlePt = "Iniciação Aérea Imparável",
                reasonEs = "La Definitiva (Fuerza Imparable) de Malphite proyecta por los aires a todo el equipo enemigo en área, activando al instante la Definitiva de Yasuo (Último Aliento) para una aniquilación masiva inmediata.",
                reasonEn = "Malphite's Ultimate knocks up entire enemy groups, instantly enabling Yasuo's Ultimate (Last Breath) for instant team wipe.",
                reasonPt = "A Ultimate do Malphite projeta os inimigos no ar em área, ativando instantaneamente a Ultimate do Yasuo.",
                comboEs = "Malphite inicia con Definitiva en el centro del combate -> Yasuo presiona Definitiva en el aire + H1 + H3 al aterrizar.",
                comboEn = "Malphite engages with Ultimate -> Yasuo hits Ultimate in mid-air + S1 + S3 on landing.",
                comboPt = "Malphite inicia com Ultimate -> Yasuo ativa Ultimate no ar + H1 + H3 ao aterrissar."
            ),
            SpecificSynergyEntry(
                teammateName = "Diana",
                preferredRole = LaneRole.JUNGLE,
                score = 97,
                category = "Wombo Combo",
                icon = "🌙",
                titleEs = "Vórtice Lunar & Remate Aéreo",
                titleEn = "Moonfall Vortex & Airborne Burst",
                titlePt = "Vórtice Lunar e Finalização Aérea",
                reasonEs = "La Definitiva de Diana agrupa a todos los rivales y los suspende por los aires, permitiendo a Yasuo rematar a múltiples carries con su Definitiva.",
                reasonEn = "Diana's Ultimate pulls and suspends multiple foes, granting Yasuo a multi-target Ultimate strike.",
                reasonPt = "A Ultimate da Diana puxa e suspende múltiplos inimigos, permitindo que Yasuo finalize com sua Ultimate.",
                comboEs = "Diana entra con H3 y activa Definitiva -> Yasuo encadena Definitiva + H1 giratorio.",
                comboEn = "Diana dashes in with S3 and casts Ultimate -> Yasuo chains Ultimate + sweeping S1.",
                comboPt = "Diana avança com H3 e usa Ultimate -> Yasuo comba Ultimate + H1 giratório."
            ),
            SpecificSynergyEntry(
                teammateName = "Gragas",
                preferredRole = LaneRole.MID,
                score = 95,
                category = "Iniciación & CC",
                icon = "🍺",
                titleEs = "Barril Explosivo & Desplazamiento",
                titleEn = "Explosive Cask Displacement",
                titlePt = "Barril Explosivo e Deslocamento",
                reasonEs = "El empujón con la H3 (Lanzamiento de Barriga) y la Definitiva de Gragas desplazan y levantan a los enemigos, generando ventanas continuas para la Definitiva de Yasuo.",
                reasonEn = "Gragas' S3 body slam and Ultimate barrel displace enemies in the air, giving Yasuo continuous Ultimate triggers.",
                reasonPt = "A H3 e a Ultimate de Gragas lançam os inimigos pelo ar, ativando a Ultimate de Yasuo.",
                comboEs = "Gragas lanza Definitiva hacia la posición de Yasuo -> Yasuo castiga en el aire.",
                comboEn = "Gragas throws Ultimate towards Yasuo -> Yasuo strikes in mid-air.",
                comboPt = "Gragas lança Ultimate em direção a Yasuo -> Yasuo ataca no ar."
            ),
            SpecificSynergyEntry(
                teammateName = "Alistar",
                preferredRole = LaneRole.SUPPORT,
                score = 96,
                category = "Iniciación & CC",
                icon = "🐂",
                titleEs = "Combo Pulverizar en Cadena",
                titleEn = "Pulverize Knockup Chain",
                titlePt = "Combo Pulverizar em Cadeia",
                reasonEs = "El clásico combo H2 (Testarazo) + H1 (Pulverizar) de Alistar levanta a los objetivos clave de forma segura para que Yasuo salte con su Definitiva.",
                reasonEn = "Alistar's S2 into S1 pulverize combo safely knocks up target carries for Yasuo's Ultimate.",
                reasonPt = "O combo H2 + H1 do Alistar levanta alvos com segurança para a Ultimate do Yasuo.",
                comboEs = "Alistar H2 + H1 sobre el carry enemigo -> Yasuo presiona Definitiva inmediatamente.",
                comboEn = "Alistar S2 + S1 onto enemy carry -> Yasuo casts Ultimate immediately.",
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
                titleEn = "CC Chain & Style Rank Stacks",
                titlePt = "Cadeia de CC e Cargas de Estilo",
                reasonEs = "Cada inmovilización de Nautilus (Pasiva, H1, H3 y Definitiva) activa la Pasiva de Samira (Impulso Temerario), otorgándole desplazamiento instantáneo y facilitando llegar a rango 'S' en segundos.",
                reasonEn = "Every Nautilus CC triggers Samira's passive dash, allowing her to stack Style Rank 'S' instantly.",
                reasonPt = "Qualquer controle de grupo de Nautilus ativa a passiva da Samira para atingir rank 'S' rápido.",
                comboEs = "Nautilus H1 + Básico -> Samira ataca con pasiva + H1 + H2 + H3 -> Definitiva (Gatillo Infernal).",
                comboEn = "Nautilus S1 + Auto -> Samira procs passive + S1 + S2 + S3 -> Ultimate (Inferno Trigger).",
                comboPt = "Nautilus H1 + Ataque -> Samira ativa passiva + H1 + H2 + H3 -> Ultimate."
            ),
            SpecificSynergyEntry(
                teammateName = "Leona",
                preferredRole = LaneRole.SUPPORT,
                score = 98,
                category = "Iniciación & CC",
                icon = "☀️",
                titleEs = "All-In Letal a Nivel 2 y 5",
                titleEn = "Lethal All-In at Level 2 & 5",
                titlePt = "All-In Letal nos Níveis 2 e 5",
                reasonEs = "El bloqueo solar de Leona con H3 + H1 y Definitiva inmoviliza al dúo rival, dejando a Samira girar con su Definitiva sin riesgo de ser interrumpida.",
                reasonEn = "Leona's heavy lockdown keeps targets pinned, allowing Samira to channel her Ultimate uninterrupted.",
                reasonPt = "O atordoamento contínuo de Leona permite que Samira canalize sua Ultimate sem ser interrompida.",
                comboEs = "Leona inicia con Definitiva + H3 -> Samira entra con H3 y activa Definitiva.",
                comboEn = "Leona initiates with Ultimate + S3 -> Samira dives in with S3 and channels Ultimate.",
                comboPt = "Leona inicia com Ultimate + H3 -> Samira entra com H3 e canaliza Ultimate."
            ),
            SpecificSynergyEntry(
                teammateName = "Amumu",
                preferredRole = LaneRole.JUNGLE,
                score = 96,
                category = "Wombo Combo",
                icon = "🩹",
                titleEs = "Maldición en Área & Resets",
                titleEn = "AoE Curse & Teamfight Resets",
                titlePt = "Maldição em Área e Resets",
                reasonEs = "La Definitiva de Amumu aturde a los 5 rivales, otorgando el escenario soñado para que Samira salte al medio con H3 y borre al equipo entero con su Definitiva.",
                reasonEn = "Amumu's Ultimate stuns the whole enemy team, creating the ideal setup for Samira's pentakill Ultimate.",
                reasonPt = "A Ultimate de Amumu atordoa todos os inimigos, criando o cenário perfeito para a Ultimate de Samira.",
                comboEs = "Amumu H1 + Definitiva -> Samira H3 al grupo + Definitiva.",
                comboEn = "Amumu S1 + Ultimate -> Samira S3 into group + Ultimate.",
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
                titleEn = "Hypercarry Turbo Buff",
                titlePt = "Hipercarregador Fortalecido",
                reasonEs = "La H2 (Capricho) de Lulu otorga velocidad de ataque y movimiento masivos, mientras que su Definitiva (Crecimiento Salvaje) salva a Jinx de asesinos con derribo aéreo y vida extra.",
                reasonEn = "Lulu's S2 grants enormous attack speed & movement, while her Ultimate protects Jinx from assassins.",
                reasonPt = "O H2 de Lulu concede velocidade de ataque e a Ultimate protege Jinx com vida extra e controle de grupo.",
                comboEs = "Lulu bufa con H2 + H3 a Jinx -> Jinx activa Pasiva (¡A divertirse!) y arrasa la teamfight con H1 cohetes.",
                comboEn = "Lulu buffs with S2 + S3 -> Jinx triggers passive and deletes teamfights with S1 rockets.",
                comboPt = "Lulu buffa com H2 + H3 -> Jinx ativa passiva e destrói lutas com H1 de foguetes."
            ),
            SpecificSynergyEntry(
                teammateName = "Thresh",
                preferredRole = LaneRole.SUPPORT,
                score = 97,
                category = "Peel & Iniciación",
                icon = "⛓️",
                titleEs = "Linterna de Rescate & Cadenas CC",
                titleEn = "Lantern Escape & Hook Setup",
                titlePt = "Lanterna de Fuga e Correntes",
                reasonEs = "La linterna (H2) de Thresh compensa la falta de movilidad de Jinx, y su gancho (H1) le permite encadenar sus trampas H3 (Mascafuegos) debajo del objetivo.",
                reasonEn = "Thresh's lantern provides safety for immobile Jinx, while his hook sets up free Chomper traps.",
                reasonPt = "A lanterna de Thresh dá mobilidade à Jinx e o gancho permite encaixar as armadilhas H3 perfeitamente.",
                comboEs = "Thresh conecta H1 -> Jinx coloca H3 trampas bajo los pies del rival -> daño continuo.",
                comboEn = "Thresh lands S1 hook -> Jinx places S3 Chompers underneath -> free DPS.",
                comboPt = "Thresh acerta H1 -> Jinx coloca armadilhas H3 embaixo do alvo -> dano garantido."
            ),
            SpecificSynergyEntry(
                teammateName = "Malphite",
                preferredRole = LaneRole.TOP,
                score = 94,
                category = "Frontline & Engage",
                icon = "🪨",
                titleEs = "Frontline de Hierro & Cohetes de Remate",
                titleEn = "Iron Frontline & Rocket Finisher",
                titlePt = "Linha de Frente e Foguetes Finais",
                reasonEs = "Malphite absorbe todo el daño enemigo y fuerza a los rivales a agruparse, permitiendo a Jinx impactar su Definitiva (¡Supermegacohete Mortal!) y daño de cohetes en área.",
                reasonEn = "Malphite locks down targets and clusters them for Jinx's massive AoE rockets and Ultimate.",
                reasonPt = "Malphite absorbe o dano e agrupa inimigos para o dano em área do foguete e Ultimate de Jinx.",
                comboEs = "Malphite impacta Definitiva -> Jinx dispara Definitiva para asegurar la primera baja y activar su pasiva.",
                comboEn = "Malphite lands Ultimate -> Jinx fires Ultimate to secure the first reset.",
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
                titleEn = "Electrocute & Blessing Burst",
                titlePt = "Explosão de Bênção e Eletrocutar",
                reasonEs = "La H3 (Bendición de la Marea) de Nami se activa 3 veces en un solo segundo gracias a los disparos dobles de la Pasiva (Pistolero Iluminado) de Lucian, infligiendo ralentización y daño mágico devastador.",
                reasonEn = "Nami's S3 tidecaller buff procs instantly on Lucian's double-shot passive, deleting enemy health bars.",
                reasonPt = "O H3 de Nami é ativado instantaneamente com o tiro duplo da passiva de Lucian, causando dano massivo.",
                comboEs = "Nami aplica H3 a Lucian -> Lucian H3 + Pasiva + H1 + Pasiva (daño explosivo en 0.5s).",
                comboEn = "Nami casts S3 on Lucian -> Lucian S3 + Passive + S1 + Passive (instant burst).",
                comboPt = "Nami aplica H3 no Lucian -> Lucian H3 + Passiva + H1 + Passiva (explosão em 0.5s)."
            ),
            SpecificSynergyEntry(
                teammateName = "Braum",
                preferredRole = LaneRole.SUPPORT,
                score = 98,
                category = "Sinergia de Dúo",
                icon = "🛡️",
                titleEs = "Aturdimiento Instantáneo de Pasiva",
                titleEn = "Instant Concussive Blows Stun",
                titlePt = "Atordoamento Instantâneo da Passiva",
                reasonEs = "Los disparos dobles de Lucian aplican las 4 marcas de la Pasiva (Golpes Conmocionantes) de Braum en menos de un segundo, congelando al rival sin posibilidad de respuesta.",
                reasonEn = "Lucian's double tap passive instantly procs all 4 stacks of Braum's stun passive.",
                reasonPt = "Os tiros duplos de Lucian ativam as 4 marcas da passiva de Braum em menos de 1 segundo.",
                comboEs = "Braum H1 + ataque -> Lucian H3 + Pasiva (aturdimiento instantáneo) + H1.",
                comboEn = "Braum S1 + auto -> Lucian S3 + Passive (instant stun) + S1.",
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
                titleEn = "Curse & Bullet Time Destruction",
                titlePt = "Maldição e Metralhadora Mortal",
                reasonEs = "La Definitiva de Amumu inmoviliza a todo el equipo rival en el río o fosas de Dragón/Barón, asegurando que Miss Fortune descargue el 100% de su Definitiva (Balacera).",
                reasonEn = "Amumu's Ultimate traps enemies inside Miss Fortune's full Bullet Time channel.",
                reasonPt = "A Ultimate de Amumu prende todos os inimigos dentro de toda a duração da Ultimate de Miss Fortune.",
                comboEs = "Amumu entra con Definitiva en cuello de botella -> Miss Fortune canaliza Definitiva completa.",
                comboEn = "Amumu casts Ultimate in a choke point -> Miss Fortune channels full Ultimate.",
                comboPt = "Amumu usa Ultimate em local fechado -> Miss Fortune canaliza Ultimate completa."
            ),
            SpecificSynergyEntry(
                teammateName = "Jarvan IV",
                preferredRole = LaneRole.JUNGLE,
                score = 97,
                category = "Wombo Combo",
                icon = "🚩",
                titleEs = "Cataclismo & Jaula de Balas",
                titleEn = "Cataclysm & Bullet Cage",
                titlePt = "Cataclismo e Gaiola de Balas",
                reasonEs = "La Definitiva (Cataclismo) de Jarvan IV encierra a los carries enemigos sin destello dentro de una arena circular, donde la Definitiva de Miss Fortune es ineludible.",
                reasonEn = "Jarvan IV's Ultimate locks enemies in an arena where Miss Fortune's Ultimate hits every single wave.",
                reasonPt = "A Ultimate de Jarvan IV prende os inimigos em uma arena onde a Ultimate de Miss Fortune não pode ser esquivada.",
                comboEs = "Jarvan IV E-Q + Definitiva -> Miss Fortune activa Definitiva sobre el cráter.",
                comboEn = "Jarvan IV E-Q + Ultimate -> Miss Fortune fires Ultimate over the arena.",
                comboPt = "Jarvan IV H3-H1 + Ultimate -> Miss Fortune usa Ultimate sobre a arena."
            ),
            SpecificSynergyEntry(
                teammateName = "Seraphine",
                preferredRole = LaneRole.SUPPORT,
                score = 96,
                category = "Iniciación & CC",
                icon = "🎤",
                titleEs = "Nota Bis & Balacera Coral",
                titleEn = "Encore Charm & Bullet Time",
                titlePt = "Encanto Musical e Metralhadora",
                reasonEs = "La Definitiva (Bis) de Seraphine enamora y atrae a los enemigos en línea recta, alineándolos a la perfección para el cono de la Definitiva de Miss Fortune.",
                reasonEn = "Seraphine's charm aligns enemies in a straight line for Miss Fortune's devastating cone.",
                reasonPt = "A Ultimate de Seraphine alinha os inimigos para o cone devastador da Ultimate de Miss Fortune.",
                comboEs = "Seraphine conecta Definitiva + H3 aturdimiento -> Miss Fortune activa Definitiva.",
                comboEn = "Seraphine lands Ultimate + S3 -> Miss Fortune opens Ultimate.",
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
                titleEn = "Mass Stun & Dagger Resets",
                titlePt = "Atordoamento em Massa e Resets",
                reasonEs = "Amumu retiene a los objetivos vulnerables evitando que usen CC contra Katarina, permitiéndole canalizar su Definitiva (Loto Mortal) y conseguir reinicios continuos con su Pasiva.",
                reasonEn = "Amumu disables threat CC so Katarina can channel Death Lotus freely and chain dagger resets.",
                reasonPt = "Amumu desativa o controle de grupo rival para que Katarina canalize a Ultimate com segurança.",
                comboEs = "Amumu Definitiva -> Katarina salta con H3 + H2 + Definitiva.",
                comboEn = "Amumu Ultimate -> Katarina S3 + S2 + Ultimate.",
                comboPt = "Amumu Ultimate -> Katarina pula com H3 + H2 + Ultimate."
            ),
            SpecificSynergyEntry(
                teammateName = "Malphite",
                preferredRole = LaneRole.TOP,
                score = 97,
                category = "Wombo Combo",
                icon = "🪨",
                titleEs = "Impacto Sísmico & Limpieza de Resets",
                titleEn = "Seismic Impact & Reset Cleanup",
                titlePt = "Impacto Sísmico e Limpeza de Lutas",
                reasonEs = "El derribo aéreo masivo de Malphite deja a los enemigos con media vida, el umbral perfecto para que Katarina salte y ejecute en cadena.",
                reasonEn = "Malphite's engage bursts targets to half HP, prime execution threshold for Katarina resets.",
                reasonPt = "A iniciação de Malphite deixa alvos com metade da vida, pronto para Katarina resetar.",
                comboEs = "Malphite Definitiva -> Katarina entra inmediatamente a recoger las bajas.",
                comboEn = "Malphite Ultimate -> Katarina dives in to clean up kills.",
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
                titleEn = "Shielded Death Mark Dive",
                titlePt = "Marca Fatal com Escudo Ninja",
                reasonEs = "La Definitiva (Mantenerse Unidos) de Shen protege a Zed con un escudo masivo cuando este se lanza a la línea trasera rival con su Definitiva, teletransportando a Shen para rematar con su provocación.",
                reasonEn = "Shen's Ultimate shields Zed during his backline dive, allowing him to burst carries with zero risk.",
                reasonPt = "A Ultimate de Shen protege Zed ao mergulhar na linha de trás inimiga.",
                comboEs = "Zed activa Definitiva sobre el carry -> Shen castea Definitiva sobre Zed -> Shen provoca al aterrizar.",
                comboEn = "Zed casts Ultimate on carry -> Shen Ults Zed -> Shen lands S3 taunt on arrival.",
                comboPt = "Zed usa Ultimate no carry -> Shen usa Ultimate no Zed -> Shen provoca com H3."
            ),
            SpecificSynergyEntry(
                teammateName = "Galio",
                preferredRole = LaneRole.MID,
                score = 96,
                category = "Dive & Flanqueo",
                icon = "🏛️",
                titleEs = "Entrada Heroica tras Asesinato",
                titleEn = "Heroic Entrance Follow-up",
                titlePt = "Entrada Heroica e Finalização",
                reasonEs = "Cuando Zed aparece detrás del tirador enemigo con Marca de la Muerte, Galio utiliza su Definitiva sobre él, derribando a los protectores enemigos y sellando la pelea.",
                reasonEn = "Zed's deep dive becomes a beacon for Galio's Hero's Entrance, knocking up anyone trying to peel.",
                reasonPt = "O mergulho de Zed serve de alvo para a Entrada Heroica de Galio, derrubando os protetores.",
                comboEs = "Zed entra con Definitiva -> Galio presiona Definitiva sobre Zed -> CC masivo + escape seguro.",
                comboEn = "Zed dives with Ultimate -> Galio Ults Zed -> heavy knockup and safe escape.",
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
                titleEn = "Unstoppable Hypercarry Buff",
                titlePt = "Hipercarregador Imparável",
                reasonEs = "Lulu aporta escudos constantes, velocidad de ataque y convierte a los atacantes en ardillas con su H2, garantizando que Maestro Yi no sea frenado por el CC rival durante su Definitiva (Imparable).",
                reasonEn = "Lulu grants shields, massive attack speed, and polymorphs threats so Yi never gets shut down.",
                reasonPt = "Lulu fornece escudos, velocidade de ataque e polimorfia para que Master Yi nunca seja parado.",
                comboEs = "Yi activa Definitiva + H3 -> Lulu aplica H2 y H3 sobre Yi + Definitiva al recibir daño.",
                comboEn = "Yi casts Ultimate + S3 -> Lulu applies S2 and S3 -> Ultimate when Yi takes focus.",
                comboPt = "Yi ativa Ultimate + H3 -> Lulu usa H2 e H3 no Yi + Ultimate para protegê-lo."
            ),
            SpecificSynergyEntry(
                teammateName = "Yuumi",
                preferredRole = LaneRole.SUPPORT,
                score = 98,
                category = "Peel & Buffer",
                icon = "🐱",
                titleEs = "Simbionte Letal & Sanación Continua",
                titleEn = "Untargetable Symbiote Boost",
                titlePt = "Simbionte Letal e Cura Contínua",
                reasonEs = "Yuumi se vincula a Yi siendo invulnerable, aportándole daño de ataque adaptativo, curaciones aceleradas y enraizado en área con su Definitiva.",
                reasonEn = "Yuumi attaches to Yi, giving adaptive AD, movement speed buffs, and multi-target root.",
                reasonPt = "Yuumi se conecta ao Yi ficando inalvejável, dando AD adaptativo e velocidade de movimento.",
                comboEs = "Yuumi H3 velocidad de movimiento -> Yi entra con H1 -> Yuumi activa Definitiva para enraizar.",
                comboEn = "Yuumi S3 speed boost -> Yi dashes with S1 -> Yuumi casts Ultimate to root.",
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
                titleEn = "Lantern Delivery & Flay Lock",
                titlePt = "Entrega por Lanterna e Puxão",
                reasonEs = "Thresh soluciona la debilidad principal de Darius (falta de movilidad) arrojando su linterna (H2) para depositarlo directamente encima de los carries rivales.",
                reasonEn = "Thresh fixes Darius' mobility issue by throwing lanterns directly into enemy squishies.",
                reasonPt = "Thresh resolve a falta de mobilidade do Darius com a lanterna diretamente nos carries inimigos.",
                comboEs = "Thresh tira linterna hacia atrás -> Darius la toma y jala a los rivales con su H3 (Aprehender).",
                comboEn = "Thresh throws lantern back -> Darius takes it and pulls enemies with S3 (Apprehend).",
                comboPt = "Thresh joga lanterna para trás -> Darius pega e puxa com H3."
            ),
            SpecificSynergyEntry(
                teammateName = "Yuumi",
                preferredRole = LaneRole.SUPPORT,
                score = 96,
                category = "Peel & Buffer",
                icon = "🐱",
                titleEs = "Velocidad de Cazador & Cargas Rápidas",
                titleEn = "Apex Speed & Bleed Stacking",
                titlePt = "Velocidade de Caça e Sangramento",
                reasonEs = "La velocidad de movimiento de Yuumi sumada a su ralentización con H1 permite a Darius alcanzar a cualquier objetivo y acumular sus 5 cargas de Hemorragia.",
                reasonEn = "Yuumi's speed boost and slow enable Darius to stick to targets and stack 5 bleed stacks effortlessly.",
                reasonPt = "A velocidade de Yuumi permite que Darius alcance qualquer alvo e atinja 5 cargas de Hemorragia.",
                comboEs = "Yuumi H3 aceleración -> Darius corta distancia con H3 + H1 giratorio + H2 básico.",
                comboEn = "Yuumi S3 speed -> Darius closes gap with S3 + S1 decimate + S2 auto.",
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
                "en" -> entry.titleEn
                "pt" -> entry.titlePt
                else -> entry.titleEs
            }
            val reason = when (lang) {
                "en" -> entry.reasonEn
                "pt" -> entry.reasonPt
                else -> entry.reasonEs
            }
            val combo = when (lang) {
                "en" -> entry.comboEn
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
        val isEs = lang == "es" || lang == "auto"
        val isPt = lang == "pt"

        return when {
            champion.isFrontline -> {
                Quadruple(
                    if (isEs) "Iniciador & Tanque Frontline" else if (isPt) "Iniciador e Linha de Frente" else "Engage & Frontline Tank",
                    "🛡️ FRONTLINE",
                    if (isEs) "Especialista en absorber daño masivo, aplicar control de masas duro y abrir espacio para los acarreadores aliados en Wild Rift."
                    else if (isPt) "Especialista em absorver dano massivo e criar espaço para os atiradores e magos da equipe."
                    else "Specialist in soaking damage and locking down targets to create space for team carries.",
                    if (isEs) listOf("Iniciación en Área", "Absorción de Daño", "Control de Objetivos (Dragones/Barón)")
                    else if (isPt) listOf("Iniciação em Área", "Absorção de Dano", "Controle de Objetivos")
                    else listOf("AoE Engage", "Damage Soaking", "Objective Zone Control")
                )
            }
            role == LaneRole.ADC -> {
                Quadruple(
                    if (isEs) "Hiperacarreador de Daño Continuo (DPS)" else if (isPt) "Hipercarregador de DPS Contínuo" else "Sustained DPS Hypercarry",
                    "🏹 DPS CARRY",
                    if (isEs) "Principal fuente de daño físico a distancia del equipo. Escala exponencialmente con objetos y requiere protección y peel constante."
                    else if (isPt) "Principal fonte de dano físico à distância. Escala fortemente com itens e requer proteção contínua."
                    else "Primary ranged physical damage source. Scales heavily with items and thrives with peel and enchanter buffs.",
                    if (isEs) listOf("Daño Crítico Explosivo", "Destrucción de Torretas", "Poder en Peleas Tardías")
                    else if (isPt) listOf("Dano Crítico Explosivo", "Destruição de Torres", "Poder no Late Game")
                    else listOf("Critical Burst DPS", "Turret Shredding", "Late-game Teamfight Carry")
                )
            }
            role == LaneRole.SUPPORT -> {
                Quadruple(
                    if (isEs) "Amplificador Táctico & Protector" else if (isPt) "Suporte Tático e Protetor" else "Tactical Buffer & Enabler",
                    "✨ UTILIDAD",
                    if (isEs) "Multiplica la eficacia de los carries mediante escudos, curaciones, visión estratégica y mitigación de amenazas."
                    else if (isPt) "Multiplica a eficácia dos carregadores através de escudos, curas e controle de visão."
                    else "Multiplies team carry efficacy through shields, healing, vision control, and peeling.",
                    if (isEs) listOf("Peel a Tiradores", "Control de Visión (Lente/Wards)", "Curación y Escudos")
                    else if (isPt) listOf("Peel para Atiradores", "Controle de Sentinelas", "Curas e Escudos")
                    else listOf("Peeling for Carries", "Vision Denial", "Buffs & Disruption")
                )
            }
            champion.damageType == DamageType.MAGIC -> {
                Quadruple(
                    if (isEs) "Mago de Control & Ráfaga AP" else if (isPt) "Mago de Controle e Explosão AP" else "Control Mage & AP Burst",
                    "🔮 BURST AP",
                    if (isEs) "Capaz de borrar objetivos frágiles en segundos y zonificar áreas estrechas del mapa con daño mágico masivo."
                    else if (isPt) "Capaz de deletar alvos frágeis e controlar áreas estrechas com dano mágico massivo."
                    else "Deletes squishy champions and zones tight choke points with massive area-of-effect magic damage.",
                    if (isEs) listOf("Daño en Área Masivo", "Control de Corredores de Jungla", "Prioridad de Empuje")
                    else if (isPt) listOf("Dano em Área Massivo", "Controle de Corredores", "Prioridade de Onda")
                    else listOf("AoE Magic Burst", "Jungle Choke Control", "Wave Prio & Roam")
                )
            }
            else -> {
                Quadruple(
                    if (isEs) "Duelista de Impacto & Flanqueo" else if (isPt) "Duelista de Flanco e Impacto" else "Skirmisher & Flank Duelist",
                    "⚔️ DUELISTA",
                    if (isEs) "Especialista en duelos 1vs1, aislamiento de objetivos y escaramuzas rápidas por el río y la jungla."
                    else if (isPt) "Especialista em lutas 1v1, isolamento de alvos e escaramuças rápidas."
                    else "Excels in 1v1 skirmishes, isolation plays, and aggressive river rotations.",
                    if (isEs) listOf("Presión Dividida (Split-push)", "Ejecución de Carries", "Movilidad y Flanqueos")
                    else if (isPt) listOf("Pressão Dividida", "Execução de Alvos", "Mobilidade e Flancos")
                    else listOf("Split-push Pressure", "Backline Assassination", "High Mobility")
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
        val isEs = lang == "es" || lang == "auto"
        val isPt = lang == "pt"

        val pSkill = partner.skills.find { it.slot == "1" }?.name ?: "habilidades"
        val pUlt = partner.skills.find { it.slot == "4" }?.name ?: "Definitiva"
        val sSkill = source.skills.find { it.slot == "1" }?.name ?: "habilidades"

        return if (partner.isFrontline) {
            Quintuple(
                "Iniciación & Frontline",
                "🛡️",
                if (isEs) "Iniciación de Tanque & Espacio Seguro" else if (isPt) "Iniciação e Espaço Seguro" else "Frontline Engage & Safe Zone",
                if (isEs) "${partner.name} absorbe las habilidades rivales e inicia con su $pUlt, permitiendo que ${source.name} conecte su $sSkill con total libertad."
                else if (isPt) "${partner.name} absorve habilidades inimigas e inicia com $pUlt, permitindo que ${source.name} use $sSkill livremente."
                else "${partner.name} tanks enemy cooldowns and locks foes down with $pUlt, creating free space for ${source.name}.",
                if (isEs) "${partner.name} conecta $pSkill/$pUlt -> ${source.name} castiga a los objetivos inmovilizados."
                else if (isPt) "${partner.name} usa $pSkill/$pUlt -> ${source.name} finaliza os alvos."
                else "${partner.name} engages with $pUlt -> ${source.name} follows up with full burst."
            )
        } else if (partnerRole == LaneRole.SUPPORT) {
            Quintuple(
                "Peel & Sinergia de Dúo",
                "✨",
                if (isEs) "Protección y Amplificación de Daño" else if (isPt) "Proteção e Amplificação de Dano" else "Peel & Damage Amp",
                if (isEs) "El kit de utilidad de ${partner.name} protege a ${source.name} contra asesinos con escudos y control de masas, aumentando su supervivencia en teamfights."
                else if (isPt) "O kit de utilidade de ${partner.name} protege ${source.name} contra assassinos e amplifica seu dano."
                else "${partner.name}'s enchanter utility shields and peels for ${source.name}, maximizing survivability.",
                if (isEs) "${partner.name} aplica escudos/curaciones -> ${source.name} avanza agresivo con $sSkill."
                else if (isPt) "${partner.name} aplica escudos -> ${source.name} avança agressivo com $sSkill."
                else "${partner.name} shields/buffs -> ${source.name} trades aggressively with $sSkill."
            )
        } else if (partnerRole == LaneRole.JUNGLE) {
            Quintuple(
                "Gank Setup & Emboscadas",
                "🎯",
                if (isEs) "Control de Río y Emboscadas Letales" else if (isPt) "Controle de Rio e Ganks Letais" else "River Ganks & Objective Setup",
                if (isEs) "La combinación de daño de ${partner.name} y el control de ${source.name} asegura bajas inmediatas en rotaciones al río y peleas de Heraldo/Dragón."
                else if (isPt) "A rotação rápida de ${partner.name} na selva garante abates rápidos em emboscadas conjuntas."
                else "Fast jungle rotations from ${partner.name} ensure quick kills during river skirmishes and objective fights.",
                if (isEs) "${source.name} presiona la línea -> ${partner.name} embosca con $pSkill para cerrar la baja."
                else if (isPt) "${source.name} pressiona a rota -> ${partner.name} ganka com $pSkill."
                else "${source.name} sets wave prio -> ${partner.name} ganks with $pSkill."
            )
        } else {
            Quintuple(
                "Wombo Combo & Daño Mixto",
                "⚡",
                if (isEs) "Cadena de Daño y Presión en Mapa" else if (isPt) "Cadeia de Dano e Pressão Global" else "Damage Chain & Map Pressure",
                if (isEs) "${partner.name} equilibra el perfil de daño del equipo y combina sus tiempos de recarga con ${source.name} para ganar peleas grupales."
                else if (isPt) "${partner.name} equilibra o dano da equipe e comita nas lutas de equipe junto com ${source.name}."
                else "${partner.name} balances damage profiles and synchronizes key cooldowns with ${source.name} in teamfights.",
                if (isEs) "Sincronizar Definitivas en espacios cerrados de jungla cerca de objetivos neutrales."
                else if (isPt) "Sincronizar Ultimates em corredores da selva perto de objetivos."
                else "Synchronize Ultimates inside tight jungle choke points near Dragons/Baron."
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
private data class Quintuple<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)
