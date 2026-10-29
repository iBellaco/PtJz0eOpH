package com.example.data

import com.example.model.Champion
import com.example.model.DamageType
import com.example.model.DraftBuildPlan
import com.example.model.LaneRole

enum class DraftBuildContext { GENERAL, ONE_VS_ONE, FULL_TEAM }

/**
 * Selects one build for the current draft. Every value returned by this policy is
 * first looked up in the local Coach catalogs, so a missing or stale catalog entry
 * is simply omitted instead of becoming a made-up item, rune, or spell.
 */
object DraftBuildPolicy {
    private val healingThreats = setOf(
        "aatrox", "drmundo", "fiddlesticks", "nami", "olaf", "soraka", "sona",
        "swain", "samira", "vladimir", "warwick", "yuumi"
    )
    private val shieldThreats = setOf(
        "braum", "janna", "karma", "lulu", "lux", "milio", "shen", "sett"
    )
    private val assassinThreats = setOf(
        "akali", "diana", "evelynn", "fizz", "katarina", "kayn", "khazix",
        "talon", "zed", "pyke"
    )

    private fun spell(vararg names: String): List<String> = names.mapNotNull { name ->
        WildRiftSpellsAndRunes.getSpellByName(name)?.name
    }

    private fun resolveItems(names: List<String>): List<String> = names.mapNotNull { raw ->
        WildRiftItemsData.getItemByName(raw)?.name
    }.distinctBy { it.lowercase() }

    private fun resolveRunes(names: List<String>): List<String> = names.mapNotNull { raw ->
        WildRiftSpellsAndRunes.getRuneByName(raw)?.name
    }.distinctBy { it.lowercase() }

    private fun resolveSpells(names: List<String>): List<String> = names.mapNotNull { raw ->
        WildRiftSpellsAndRunes.getSpellByName(raw)?.name
    }.distinctBy { it.lowercase() }

    private fun parseChampionRunes(champion: Champion): List<String> =
        (listOf(champion.recommendedRunes) + champion.runeTreeDetails.split("•", ","))
            .flatMap { it.split(",", "/", "|") }
            .map { it.replace(Regex("\\s*\\(.*?\\)"), "").trim() }
            .map { if (WildRiftSpellsAndRunes.getRuneByName(it) != null) it else it.substringAfter(": ", it) }
            .filter { it.isNotBlank() }

    private fun parseChampionSpells(champion: Champion): List<String> = champion.recommendedSpells
        .flatMap { it.split("+", ",", "/") }
        .map { it.trim() }
        .filter { it.isNotBlank() }

    private val supportQuestItems = setOf("hoz espectral", "escudo reliquia", "guadaña de la niebla negra", "baluarte de la montaña")

    private fun validRunePage(names: List<String>): Boolean = com.example.util.BuildChoiceRules.validRunePage(
        names.map { com.example.util.BuildChoiceRules.RuneChoice(it, WildRiftSpellsAndRunes.getRuneByName(it)?.category.orEmpty()) })

    private fun supportStarter(champion: Champion): String? {
        val tankLike = champion.isFrontline || champion.id in setOf("nautilus", "leona", "alistar", "braum", "thresh")
        return WildRiftItemsData.getItemByName(if (tankLike) "Escudo reliquia" else "Hoz espectral")?.name
    }

    private fun enemyIds(enemies: List<Champion>): Set<String> = enemies.map { it.id.lowercase() }.toSet()

    private fun titleFor(
        champion: Champion,
        role: LaneRole,
        hasHealing: Boolean,
        hasShields: Boolean,
        hasTanks: Boolean,
        hasAssassins: Boolean,
        blindPick: Boolean,
        lang: String,
        context: DraftBuildContext
    ): String {
        val focus = when {
            hasHealing -> if (lang == "pt") "Resposta anti-cura" else "Respuesta anti-curación"
            hasShields -> if (lang == "pt") "Resposta a escudos" else "Respuesta anti-escudos"
            hasTanks -> if (lang == "pt") "Pressão contra tanques" else "Presión contra tanques"
            hasAssassins -> if (lang == "pt") "Proteção contra assassinos" else "Protección contra asesinos"
            blindPick -> if (lang == "pt") "Plano seguro de Blind Pick" else "Plan seguro de Blind Pick"
            else -> if (lang == "pt") "Plano de composição" else "Plan de composición"
        }
        val contextLabel = when (context) {
            DraftBuildContext.ONE_VS_ONE -> if (lang == "pt") "Confronto 1v1" else "Enfrentamiento 1v1"
            DraftBuildContext.FULL_TEAM -> if (lang == "pt") "Composição completa" else "Composición completa"
            DraftBuildContext.GENERAL -> ""
        }
        return listOf("${champion.getLocalizedName(lang)} • ${role.getLocalizedName(lang)}", contextLabel, focus)
            .filter { it.isNotBlank() }
            .joinToString(" • ")
    }

    private fun reason(es: String, pt: String, lang: String): String = if (lang == "pt") pt else es

    fun create(
        champion: Champion,
        role: LaneRole,
        allies: List<Champion>,
        enemies: List<Champion>,
        blindPick: Boolean = false,
        lang: String = "es",
        context: DraftBuildContext = DraftBuildContext.GENERAL
    ): DraftBuildPlan {
        val ids = enemyIds(enemies)
        val hasHealing = ids.any { it in healingThreats }
        val hasShields = ids.any { it in shieldThreats }
        val hasTanks = enemies.count { it.isFrontline } >= 2
        val hasAssassins = ids.count { it in assassinThreats } >= 1
        val isPhysical = champion.damageType == DamageType.PHYSICAL
        val isMagic = champion.damageType == DamageType.MAGIC

        // Prefer the champion's explicit lane build. Never invent a generic physical top
        // build for a mage, or combine two support quests to fill empty slots.
        val laneBuild = champion.builds.firstOrNull { it.role.equals(role.name, true) }
        val catalogChampionCore = resolveItems(laneBuild?.coreItems?.takeIf { it.isNotEmpty() }
            ?: champion.coreItems.takeIf { role == champion.primaryRole }.orEmpty())
        val core = buildList {
            if (role == LaneRole.SUPPORT) supportStarter(champion)?.let(::add)
            addAll(catalogChampionCore.filterNot { it.lowercase() in supportQuestItems })
        }.distinctBy { it.lowercase() }.take(3)

        val championSituational = resolveItems(laneBuild?.situationalItems ?: champion.situationalItems)
        val situationalCandidates = buildList {
            val threats = buildList {
                if (hasHealing) add(if (isMagic) "Morellonomicón" else "Recordatorio letal")
                if (hasShields) add(if (isMagic) "Tridente de Oceánida" else "Colmillo de serpiente")
                if (hasTanks) add(if (isMagic) "Bastón del Vacío" else "Recuerdos de Lord Dominik")
                if (hasAssassins) add(if (isMagic) "Reloj de Arena de Zhonya" else "Ángel de la guarda")
            }
            // Threats reorder compatible, champion-specific options, not its archetype.
            addAll(resolveItems(threats).filter { it in championSituational })
            addAll(championSituational)
        }
        val situational = resolveItems(situationalCandidates)
            .filterNot { it.lowercase() in supportQuestItems }
            .filterNot { it.lowercase() in core.map(String::lowercase) }
            .take(4)

        val runesFromChampion = resolveRunes(laneBuild?.runes?.takeIf { it.isNotBlank() }?.split(",")?.map { it.trim() }
            ?: parseChampionRunes(champion))
        val runes = runesFromChampion.takeIf(::validRunePage).orEmpty()

        val championSpells = resolveSpells(laneBuild?.spells ?: parseChampionSpells(champion))
        val spells = when (role) {
            LaneRole.JUNGLE -> (spell("Destello", "Aplastar") + championSpells).distinctBy { it.lowercase() }.take(2)
            LaneRole.SUPPORT -> {
                val second = if (hasAssassins) "Extenuación" else "Prender"
                (spell("Destello", second) + championSpells).distinctBy { it.lowercase() }.take(2)
            }
            else -> (championSpells + spell("Destello", if (hasAssassins) "Extenuación" else "Prender"))
                .distinctBy { it.lowercase() }.take(2)
        }

        val reasons = buildList {
            add(reason("Plan condicionado al draft: no conocemos oro, inventarios ni enfriamientos de la partida.", "Plano condicionado à seleção: não conhecemos ouro, inventários nem recargas da partida.", lang))
            if (core.size < 3 || runes.isEmpty()) add(reason("Base incompleta para este campeón y línea: revisa sus builds; no se rellenan huecos con opciones genéricas.", "Base incompleta para este campeão e rota: confira suas builds; as lacunas não são preenchidas com opções genéricas.", lang))
            if (hasHealing) add(reason("Si la curación decide los combates, coordina quién puede aplicar anti-curación de forma constante sin retrasar su pico esencial.", "Se a cura decide as lutas, combinem quem pode aplicar anti-cura de forma constante sem atrasar seu pico essencial.", lang))
            if (hasShields) add(reason("Si los escudos absorben tu ventana de daño, compara una opción anti-escudos compatible con retrasar tu objeto principal.", "Se os escudos absorvem sua janela de dano, compare uma opção anti-escudos compatível com o atraso do seu item principal.", lang))
            if (hasTanks) add(reason("Confirma las resistencias compradas antes de priorizar penetración; vida y resistencia no requieren la misma respuesta.", "Confirme as resistências compradas antes de priorizar penetração; vida e resistência não exigem a mesma resposta.", lang))
            if (hasAssassins) add(reason("Prioriza supervivencia y control del ritmo contra entradas explosivas.", "Prioriza sobrevivência e controle do ritmo contra entradas explosivas.", lang))
            if (allies.any { it.isFrontline } && champion.isRanged) add(reason("Aprovecha la primera línea aliada para jugar a distancia.", "Aproveita a linha de frente aliada para jogar à distância.", lang))
            if (isPhysical && !hasHealing && !hasTanks && !hasAssassins && !hasShields) add(reason("Mantiene el pico de poder propio del campeón y su función principal.", "Mantém o pico de poder do campeão e sua função principal.", lang))
        }

        return DraftBuildPlan(
            title = titleFor(champion, role, hasHealing, hasShields, hasTanks, hasAssassins, blindPick, lang, context),
            coreItems = core,
            situationalItems = situational,
            runes = runes,
            spells = spells,
            reasons = reasons,
            matchupChampions = enemies
                .filter { it.id != "empty" }
                .distinctBy { it.id }
                .map { it.getLocalizedName(lang) }
        )
    }
}
