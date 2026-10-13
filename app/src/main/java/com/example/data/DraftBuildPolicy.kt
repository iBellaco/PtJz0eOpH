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

    private fun item(vararg names: String): List<String> = names.mapNotNull { name ->
        WildRiftItemsData.getItemByName(name)?.name
    }

    private fun rune(vararg names: String): List<String> = names.mapNotNull { name ->
        WildRiftSpellsAndRunes.getRuneByName(name)?.name
    }

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
            .flatMap { it.split(":", limit = 2).last().split("/", "|") }
            .map { it.replace(Regex("\\s*\\(.*?\\)"), "").trim() }
            .filter { it.isNotBlank() }

    private fun parseChampionSpells(champion: Champion): List<String> = champion.recommendedSpells
        .flatMap { it.split("+", ",", "/") }
        .map { it.trim() }
        .filter { it.isNotBlank() }

    private fun fallbackCore(role: LaneRole, damageType: DamageType): List<String> = when (role) {
        LaneRole.TOP -> item("Cuchilla negra", "Desgarrador divino", "Ángel de la guarda")
        LaneRole.JUNGLE -> if (damageType == DamageType.MAGIC) {
            item("Eco de Luden", "Bastón del Vacío", "Ángel de la guarda")
        } else item("Filo fantasmal de Youmuu", "Cuchilla negra", "Ángel de la guarda")
        LaneRole.MID -> if (damageType == DamageType.PHYSICAL) {
            item("Filo fantasmal de Youmuu", "Eclipse", "Ángel de la guarda")
        } else item("Eco de Luden", "Bastón del Vacío", "Reloj de Arena de Zhonya")
        LaneRole.ADC -> item("Hoja del rey arruinado", "Filo Infinito", "Recordatorio letal")
        LaneRole.SUPPORT -> item("Hoz espectral", "Baluarte de la montaña", "Redención")
    }

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

        val catalogChampionCore = resolveItems(champion.coreItems)
        val core = buildList {
            if (role == LaneRole.SUPPORT) supportStarter(champion)?.let(::add)
            addAll(catalogChampionCore)
            addAll(fallbackCore(role, champion.damageType))
        }.distinctBy { it.lowercase() }.take(3)

        val situationalCandidates = buildList {
            if (hasHealing) add(if (isMagic) "Morellonomicón" else "Recordatorio letal")
            if (hasShields) add(if (isMagic) "Tridente de Oceánida" else "Colmillo de serpiente")
            if (hasTanks) add(if (isMagic) "Bastón del Vacío" else "Recuerdos de Lord Dominik")
            if (hasAssassins) add(if (isMagic) "Reloj de Arena de Zhonya" else "Ángel de la guarda")
            addAll(champion.situationalItems)
        }
        val situational = resolveItems(situationalCandidates)
            .filterNot { it.lowercase() in core.map(String::lowercase) }
            .take(4)

        val runesFromChampion = resolveRunes(parseChampionRunes(champion))
        val runes = (runesFromChampion + when {
            role == LaneRole.SUPPORT && hasAssassins -> rune("Réplica", "Revestimiento de Huesos")
            hasTanks -> rune("Derribado", "Conquistador")
            hasHealing -> rune("Triunfo", "Golpe de Gracia")
            else -> rune("Conquistador", "Triunfo")
        }).distinctBy { it.lowercase() }.take(5)

        val championSpells = resolveSpells(parseChampionSpells(champion))
        val spells = when (role) {
            LaneRole.JUNGLE -> (spell("Destello", "Aplastar") + championSpells).distinctBy { it.lowercase() }.take(2)
            LaneRole.SUPPORT -> {
                val second = if (hasAssassins) "Extenuación" else if (hasHealing) "Curar" else "Prender"
                (spell("Destello", second) + championSpells).distinctBy { it.lowercase() }.take(2)
            }
            else -> (championSpells + spell("Destello", if (hasAssassins) "Extenuación" else "Prender"))
                .distinctBy { it.lowercase() }.take(2)
        }

        val reasons = buildList {
            if (hasHealing) add(reason("Incluye una respuesta del catálogo contra la curación rival.", "Inclui uma resposta do catálogo contra a cura inimiga.", lang))
            if (hasShields && !isMagic) add(reason("Incluye reducción de escudos para que el daño físico atraviese la protección rival.", "Inclui redução de escudos para que o dano físico atravesse a proteção inimiga.", lang))
            if (hasTanks) add(reason("Incluye penetración contra la vida y resistencias de la primera línea rival.", "Inclui penetração contra a vida e as resistências da linha de frente inimiga.", lang))
            if (hasAssassins) add(reason("Prioriza supervivencia y control del ritmo contra entradas explosivas.", "Prioriza sobrevivência e controle do ritmo contra entradas explosivas.", lang))
            if (allies.any { it.isFrontline } && champion.isRanged) add(reason("Aprovecha la primera línea aliada para jugar a distancia.", "Aproveita a linha de frente aliada para jogar à distância.", lang))
            if (isMagic && hasShields) add(reason("Incluye Tridente de Oceánida, el anti-escudos AP del catálogo, para reducir las barreras antes del daño principal.", "Inclui o Tridente de Oceânida, o anti-escudos AP do catálogo, para reduzir as barreiras antes do dano principal.", lang))
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
