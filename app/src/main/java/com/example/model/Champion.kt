package com.example.model

import kotlinx.serialization.Serializable

@Serializable
enum class LaneRole(val displayName: String, val shortName: String) {
    TOP("Línea de Barón", "Top"),
    JUNGLE("Jungla", "Jungla"),
    MID("Línea Central", "Mid"),
    ADC("Línea de Dragón", "Dúo"),
    SUPPORT("Soporte", "Soporte");

    val iconResId: Int
        get() = when (this) {
            TOP -> com.example.R.drawable.ic_role_baron
            JUNGLE -> com.example.R.drawable.ic_role_jungle
            MID -> com.example.R.drawable.ic_role_mid
            ADC -> com.example.R.drawable.ic_role_dragon
            SUPPORT -> com.example.R.drawable.ic_role_support
        }
}

@Serializable
enum class DamageType(val displayName: String) {
    PHYSICAL("Físico"),
    MAGIC("Mágico"),
    TRUE_HYBRID("Híbrido / Verdadero")
}

@Serializable
enum class ItemCategory(val displayName: String, val sectionTitle: String, val iconEmoji: String) {
    BASIC("Artículos Básicos", "ARTÍCULOS BÁSICOS", ""),
    MID_TIER("Objetos de Nivel Medio", "OBJETOS DE NIVEL MEDIO", "️"),
    PHYSICAL("Objetos con Daños Físicos", "OBJETOS CON DAÑOS FÍSICOS", "️"),
    MAGIC("Objetos de Daño Mágico", "OBJETOS DE DAÑO MÁGICO", ""),
    DEFENSE("Artículos de Defensa", "ARTÍCULOS DE DEFENSA", "️"),
    SUPPORT("Artículos de Apoyo", "ARTÍCULOS DE APOYO", ""),
    BOOTS_T2("Botas Nivel 2", "BOTAS NIVEL 2", ""),
    BOOTS_T3("Botas Nivel 3", "BOTAS NIVEL 3", ""),
    ACTIVE("Objetos de Hechizo Activos", "OBJETOS DE HECHIZO ACTIVOS", "")
}


@Serializable
data class ChampionSkill(
    val slot: String = "", // "P", "1", "2", "3", "4"
    val slotName: String = "", // "Pasiva", "Habilidad 1", "Habilidad 2", "Habilidad 3", "Definitiva"
    val name: String = "",
    val nameEn: String = "",
    val namePt: String = "",
    val iconUrl: String = "",
    val description: String = "",
    val cooldown: String = ""
)


@Serializable
data class ItemSwap(
    val coreItem: String,
    val coreItemIcon: String,
    val altItem: String,
    val altItemIcon: String,
    val reasonTitle: String,
    val reasonDesc: String,
    val againstWho: String
)

@Serializable
data class ChampionBuild(
    val id: String = "",
    val title: String = "",
    val role: String = "",
    val items: List<String> = emptyList(),
    val coreItems: List<String> = emptyList(),
    val situationalItems: List<String> = emptyList(),
    val runes: String = "",
    val spells: List<String> = emptyList(),
    val bootBase: String = "",
    val bootUpgrade: String = "",
    val situationalBoots: List<String> = emptyList()
)

@Serializable
data class Champion(
    val id: String = "",
    val name: String = "",
    val nameEn: String = "",
    val namePt: String = "",
    val title: String = "",
    val titleEn: String = "",
    val titlePt: String = "",
    val ddragonId: String = "",
    val avatarUrl: String = "",
    val primaryRole: LaneRole = LaneRole.MID,
    val secondaryRoles: List<LaneRole> = emptyList(),
    val tier: String = "B", // "S+", "S", "A+", "A", "B"
    val winrate: Double = 50.0, // e.g. 53.8
    val pickRate: Double = 0.0,
    val banRate: Double = 0.0,
    val winrateDelta: Double = 0.0, // vs. ayer (+0.3%, -0.2%)
    val pickRateDelta: Double = 0.0,
    val banRateDelta: Double = 0.0,
    val cnTier: String = "", // T0, T1, T2, T3 en servidor chino
    val damageType: DamageType = DamageType.PHYSICAL,
    val summary: String = "",
    val advantageAgainst: List<String> = emptyList(),
    val counteredBy: List<String> = emptyList(),
    val synergies: List<String> = emptyList(),
    val tacticalAdvice: String = "",
    val recommendedRunes: String = "",
    val runeTreeDetails: String = "",
    val primaryRuneIconUrl: String = "",
    val recommendedSpells: List<String> = emptyList(),
    val spellsIcons: List<String> = emptyList(),
    val coreItems: List<String> = emptyList(),
    val coreItemsIcons: List<String> = emptyList(),
    val situationalItems: List<String> = emptyList(),
    val situationalItemsIcons: List<String> = emptyList(),
    val build2Runes: String = "",
    val build2Spells: List<String> = emptyList(),
    val itemSwaps: List<ItemSwap> = emptyList(),
    val skillOrder: String = "",
    val skills: List<ChampionSkill> = emptyList(),
    val isRanged: Boolean = false,
    val isFrontline: Boolean = false,
    val wildRiftFireUrl: String = "https://www.wildriftfire.com/tier-list",
    val wrMetaUrl: String = "https://wr-meta.com/",
    val wildRiftCoreUrl: String = "https://wildriftcore.com/es/",
    val bestBuildWrUrl: String = "https://bestbuildwr.com/"
    , val builds: List<ChampionBuild> = emptyList()
) {
    fun getLocalAvatarUri(): String = "file:///android_asset/champions/$id.png"
}

@Serializable
data class WildRiftItem(
    val id: String,
    val name: String,
    val nameEn: String = "",
    val namePt: String = "",
    val category: String,
    val goldCost: Int,
    val stats: String,
    val statsEn: String = "",
    val statsPt: String = "",
    val passive: String,
    val passiveEn: String = "",
    val passivePt: String = "",
    val coachTip: String = "",
    val coachTipEn: String = "",
    val coachTipPt: String = "",
    val iconUrl: String
) {
    fun getLocalizedName(lang: String): String = when (lang) {
        "en" -> nameEn.ifBlank { name }
        "pt" -> namePt.ifBlank { name }
        else -> name
    }

    fun getLocalizedStats(lang: String): String = when (lang) {
        "en" -> statsEn.ifBlank { stats }
        "pt" -> statsPt.ifBlank { stats }
        else -> stats
    }

    fun getLocalizedPassive(lang: String): String = when (lang) {
        "en" -> passiveEn.ifBlank { passive }
        "pt" -> passivePt.ifBlank { passive }
        else -> passive
    }

    fun getLocalizedCoachTip(lang: String): String = when (lang) {
        "en" -> coachTipEn.ifBlank { coachTip }
        "pt" -> coachTipPt.ifBlank { coachTip }
        else -> coachTip
    }

    fun getStatsList(lang: String): List<String> {
        val raw = getLocalizedStats(lang)
        if (raw.isBlank()) return emptyList()
        return raw.split(Regex("[•\n]"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }
}

@Serializable
data class SummonerSpellItem(
    val id: String,
    val name: String,
    val nameEn: String = "",
    val namePt: String = "",
    val cooldown: String,
    val iconUrl: String,
    val description: String,
    val descriptionEn: String = "",
    val descriptionPt: String = "",
    val category: String = "Hechizos"
) {
    fun getLocalizedName(lang: String): String = when (lang) {
        "en" -> nameEn.ifBlank { name }
        "pt" -> namePt.ifBlank { name }
        else -> name
    }

    fun getLocalizedDescription(lang: String): String = when (lang) {
        "en" -> descriptionEn.ifBlank { description }
        "pt" -> descriptionPt.ifBlank { description }
        else -> description
    }
}

@Serializable
data class RuneItem(
    val id: String,
    val name: String,
    val nameEn: String = "",
    val namePt: String = "",
    val category: String, // "Clave", "Dominación", "Precisión", "Valor", "Inspiración"
    val iconUrl: String,
    val description: String,
    val descriptionEn: String = "",
    val descriptionPt: String = ""
)

@Serializable
data class MapObjectiveItem(
    val id: String,
    val name: String,
    val nameEn: String = "",
    val namePt: String = "",
    val spawnTime: String,
    val respawnTime: String,
    val iconUrl: String,
    val buffDescription: String,
    val tactics: String
)

@Serializable
data class DraftSlot(
    val champion: Champion,
    val assignedRole: LaneRole = champion.primaryRole,
    val confidence: Int? = null,
    val summonerName: String? = null,
    val spells: List<String> = emptyList()
)

@Serializable
data class DraftAnalysisResult(
    val physicalDamagePercent: Int,
    val magicDamagePercent: Int,
    val trueDamagePercent: Int,
    val allyPhysicalDamagePercent: Int = 0,
    val allyMagicDamagePercent: Int = 0,
    val allyTrueDamagePercent: Int = 0,
    val allyCompositionWarning: String? = null,
    val frontlineStatus: String,
    val directMatchupWarning: String?,
    val directCounterBestPick: String?,
    val isFirstPickMode: Boolean = false,
    val bestOverallPick: DraftRecommendation? = null,
    val recommendations: List<DraftRecommendation>
)

@Serializable
data class DraftRecommendation(
    val champion: Champion,
    val estimatedWinrate: Double,
    val advantageBadge: String,
    val tacticalReason: String,
    val runes: String,
    val synergyDetails: String = "",
    val counterDetails: String = ""
)

@Serializable
data class MetaDataSource(
    val id: String,
    val name: String,
    val nameEn: String = "",
    val namePt: String = "",
    val description: String,
    val descriptionEn: String = "",
    val descriptionPt: String = "",
    val url: String,
    val focusArea: String,
    val badge: String = "Sincronizado"
)
