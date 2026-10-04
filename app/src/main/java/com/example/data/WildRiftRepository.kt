package com.example.data



import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf


import com.example.model.Champion
import com.example.model.ChampionSkill
import com.example.model.DamageType
import com.example.model.DraftAnalysisResult
import com.example.model.DraftRecommendation
import com.example.model.LaneRole
import com.example.model.MapObjectiveItem
import com.example.model.RuneItem
import com.example.model.SummonerSpellItem
import com.example.model.WildRiftItem
import com.example.model.MetaDataSource

object WildRiftRepository {

    // Versión canónica oficial de Wild Rift
    var CURRENT_PATCH_VERSION = "Parche 7.3a"
    var LAST_SYNC_STATUS = "Sincronización Automática Activa"

    // CDN base URL para avatares, habilidades, objetos y hechizos
    private const val CDN_VERSION = "16.16.1"
    private const val DDRAGON_CHAMP_IMG = "https://ddragon.leagueoflegends.com/cdn/$CDN_VERSION/img/champion"
    private const val DDRAGON_SPELL_IMG = "https://ddragon.leagueoflegends.com/cdn/$CDN_VERSION/img/spell"
    private const val DDRAGON_ITEM_IMG = "https://ddragon.leagueoflegends.com/cdn/$CDN_VERSION/img/item"
    private const val DDRAGON_PASSIVE_IMG = "https://ddragon.leagueoflegends.com/cdn/$CDN_VERSION/img/passive"


    // ==========================================


    // ==========================================
    // FUENTES DE DATOS Y META ACTUAL
    // ==========================================
    val metaSources: List<MetaDataSource> = listOf(
        MetaDataSource(id = "global_bestbuildwr", name = "BestBuildWR", description = "Tier list Global",
            url = "https://bestbuildwr.com/tierlist", focusArea = "Categorías de campeones"),
        MetaDataSource(id = "global_wildriftfire", name = "WildRiftFire", description = "Guías y tier list Global",
            url = "https://www.wildriftfire.com/tier-list", focusArea = "Categorías y builds"),
        MetaDataSource(id = "global_wildriftcore", name = "WildRiftCore", description = "Tier list Global",
            url = "https://wildriftcore.com/es/tierlist/", focusArea = "Categorías de campeones")
    )

    // CATÁLOGO DE HECHIZOS DE INVOCADOR (SUMMONER SPELLS)
    // ==========================================
    var summonerSpells: List<SummonerSpellItem> by mutableStateOf(WildRiftSpellsAndRunes.summonerSpells)

    // ==========================================
    // CATÁLOGO DE RUNAS DE WILD RIFT
    // ==========================================
    var runes: List<RuneItem> by mutableStateOf(WildRiftSpellsAndRunes.runes)

    // ==========================================
    // CATÁLOGO DE OBJETOS DE WILD RIFT
    // ==========================================
    var items: List<WildRiftItem> by mutableStateOf(WildRiftItemsData.list)

    // ==========================================
    // CATÁLOGO DE OBJETIVOS DE MAPA (MONSTRUOS ÉPICOS DE WILD RIFT)
    // ==========================================
    var mapObjectives: List<MapObjectiveItem> by mutableStateOf(listOf(
        MapObjectiveItem(
            id = "infernal_dragon",
            name = "Dragón Infernal (Fuego)",
            spawnTime = "Minuto 5:00",
            respawnTime = "Reaparece cada 5:00",
            iconUrl = "file:///android_asset/offline_images/8109dc5585dded20f884d47e23537283.png",
            buffDescription = "Otorga a todo el equipo +3% de daño de ataque y +3% de poder de habilidad acumulable.",
            tactics = "Prioriza asegurar la línea de dragón empujando oleadas 30s antes de su aparición. Ideal para composiciones de daño explosivo."
        ),
        MapObjectiveItem(
            id = "mountain_dragon",
            name = "Dragón de Montaña (Tierra)",
            spawnTime = "Minuto 5:00",
            respawnTime = "Reaparece cada 5:00",
            iconUrl = "file:///android_asset/offline_images/8597fe6e53515dfe1d8fbf3b55b5b75b.png",
            buffDescription = "Otorga a todo el equipo +6% de armadura y resistencia mágica adicionales.",
            tactics = "Refuerza la línea frontal de los tanques, facilitando asedios prolongados bajo torre enemiga."
        ),
        MapObjectiveItem(
            id = "ocean_dragon",
            name = "Dragón de los Océanos (Agua)",
            spawnTime = "Minuto 5:00",
            respawnTime = "Reaparece cada 5:00",
            iconUrl = "file:///android_asset/offline_images/e1996d3b51e1163a2ad7636b1fadfe22.png",
            buffDescription = "Restaura un 2.5% de la vida faltante cada 5 segundos a todos los miembros del equipo.",
            tactics = "Otorga sustain inagotable en el mapa para desgastar al rival sin necesidad de volver a base."
        ),
        MapObjectiveItem(
            id = "ice_dragon",
            name = "Dragón de Hielo (Glacial)",
            spawnTime = "Minuto 5:00",
            respawnTime = "Reaparece cada 5:00",
            iconUrl = "file:///android_asset/offline_images/0e558656422feb0c3e35aa94d0a7cbdf.png",
            buffDescription = "Otorga +7 de aceleración de habilidad a todo el equipo y crea zonas de escarcha.",
            tactics = "Permite rotar habilidades mucho más rápido en escaramuzas y peleas por el Barón."
        ),
        MapObjectiveItem(
            id = "elder_dragon",
            name = "Dragón Anciano (Elder Dragon)",
            spawnTime = "Minuto 12:00",
            respawnTime = "Reaparece cada 5:00",
            iconUrl = "file:///android_asset/offline_images/f4111f9ad7ed66a3099483928579e31d.png",
            buffDescription = "Ataques y habilidades queman a los rivales. Si la vida del rival cae por debajo del 15%, es ejecutado de inmediato.",
            tactics = "El buff más decisivo de Wild Rift en el juego tardío. Asegura visión perimetral con centinelas antes de iniciar."
        ),
        MapObjectiveItem(
            id = "rift_herald",
            name = "Heraldo de la Grieta (Rift Herald)",
            spawnTime = "Minuto 5:00",
            respawnTime = "Solo aparece 1 por partida",
            iconUrl = "file:///android_asset/offline_images/d37173abaed44602b4fec37ee9678bd3.png",
            buffDescription = "Al recoger el Ojo del Heraldo, permite invocar al Heraldo para embestir y destruir placas de torretas enemigas.",
            tactics = "Úsalo en la línea de Barón o Mid para derribar la primera torreta y desbloquear rotaciones tempranas."
        ),
        MapObjectiveItem(
            id = "baron_nashor",
            name = "Barón Nashor",
            spawnTime = "Minuto 12:00",
            respawnTime = "Reaparece cada 5:00",
            iconUrl = "file:///android_asset/offline_images/8a4e349e5f6110a53c0c0ac259281a1b.png",
            buffDescription = "Otorga Mano del Barón: potencia el daño de los súbditos aliados cercanos y reduce el tiempo de Retirada a 4 segundos.",
            tactics = "Aprovecha el buff para asediar las tres líneas simultáneamente y forzar la caída de torres de inhibidor."
        ),
        MapObjectiveItem(
            id = "scuttle_crab",
            name = "Cangrejo Escurridizo",
            spawnTime = "Minuto 1:15",
            respawnTime = "Reaparece cada 2:30",
            iconUrl = "file:///android_asset/offline_images/235f48460a0e2e8996472effce9468f8.png",
            buffDescription = "Genera un Santuario de Velocidad y visión inquebrantable en el río frente al Dragón o Barón.",
            tactics = "Aplica control de masas duro para romper su escudo de inmediato y acelerar la limpieza del río."
        ),
        MapObjectiveItem(
            id = "red_buff",
            name = "Ancestro Ígneo (Buff Rojo)",
            spawnTime = "Minuto 0:20",
            respawnTime = "Reaparece cada 2:30",
            iconUrl = "file:///android_asset/spells/ignite.webp",
            buffDescription = "Otorga Escudo de Cenizas: ataques básicos queman causando daño verdadero periódico y ralentizan.",
            tactics = "Esencial para tiradores y junglas físicos para aumentar el potencial de persecución y hostigamiento."
        ),
        MapObjectiveItem(
            id = "blue_buff",
            name = "Coloso Celeste (Buff Azul)",
            spawnTime = "Minuto 0:20",
            respawnTime = "Reaparece cada 2:30",
            iconUrl = "file:///android_asset/spells/clarity.jpg",
            buffDescription = "Otorga Perspicacia Espiritual: regeneración masiva de maná/energía y aceleración de habilidad adicional.",
            tactics = "Cédelo a tu carrilero central mágico para asegurar empuje continuo de oleadas antes de los objetivos."
        )
    ))

    // ==========================================
    // ROSTER INTEGRAL DE CAMPEONES DE WILD RIFT
    // ==========================================
    val champions = androidx.compose.runtime.mutableStateListOf<Champion>()
    var lastError: String? by mutableStateOf(null)


    private val baseChampions = mutableListOf<Champion>()
    val baseChampionsList: List<Champion> get() = baseChampions.toList()
    private fun canonicalChampionId(id: String): String =
        if (id.equals("nunu_and_willump", ignoreCase = true)) "nunu_willump" else id

    fun getBaseChampion(idOrName: String): Champion? = baseChampions.find {
        it.id.equals(canonicalChampionId(idOrName), ignoreCase = true) || it.name.equals(idOrName, ignoreCase = true)
    }
    var activeRegionName by mutableStateOf(com.example.data.sync.MetaRegion.DEFAULT)
    private var chineseChampions: List<Champion> = emptyList()
    private val regionalChampions = mutableMapOf<String, List<Champion>>()
    var regionRevision by mutableStateOf(0)
        private set

    private data class RegionalStatsTuple(val wr: Double, val pr: Double, val br: Double, val delta: Double)

    @Synchronized
    fun applyRegionalTierList(region: String, tiers: Map<String, String>) {
        val normalized = com.example.data.sync.MetaRegion.normalize(region)
        val updated = baseChampions.map { champion ->
            val category = tiers[com.example.data.sync.RegionalTierParser.canonical(champion.id)]
                ?: tiers[com.example.data.sync.RegionalTierParser.canonical(champion.name)]
                ?: champion.tier.ifBlank { "A" }

            val (baseWr, basePr, baseBr, delta) = when (category) {
                "S+" -> RegionalStatsTuple(53.8, 14.5, 22.0, 0.48)
                "S" -> RegionalStatsTuple(52.3, 11.2, 12.5, 0.32)
                "A+", "A" -> RegionalStatsTuple(50.8, 8.4, 5.0, 0.12)
                "B+", "B" -> RegionalStatsTuple(49.4, 5.6, 2.1, -0.18)
                "C+", "C" -> RegionalStatsTuple(48.1, 3.2, 0.8, -0.35)
                else -> RegionalStatsTuple(46.8, 1.8, 0.4, -0.52)
            }
            val finalWr = if (champion.winrate > 0.0) champion.winrate else baseWr
            val finalPr = if (champion.pickRate > 0.0) champion.pickRate else basePr
            val finalBr = if (champion.banRate > 0.0) champion.banRate else baseBr
            val finalDelta = if (champion.winrateDelta != 0.0) champion.winrateDelta else delta
            val isCn = false
            val finalCnTier = if (champion.cnTier.isNotBlank()) champion.cnTier else when (category) {
                "S+" -> "T0"
                "S" -> "T1"
                "A+", "A" -> "T2"
                "B+", "B" -> "T3"
                "C+", "C" -> "T4"
                else -> "T5"
            }

            champion.copy(
                hasRegionalStats = isCn,
                tier = category,
                cnTier = finalCnTier,
                // GLOBAL también conserva las estadísticas reales incluidas en el catálogo.
                // Antes se ponían en 0.0 al fusionar tiers, provocando ▲ +0.00% en toda la Tier List.
                winrate = finalWr,
                pickRate = finalPr,
                banRate = finalBr,
                winrateDelta = finalDelta
            )
        }
        regionalChampions[normalized] = updated
        regionRevision++
        if (activeRegionName == normalized) {
            champions.clear()
            champions.addAll(updated)
        }
    }

    fun regionalSnapshot(region: String): List<Champion> =
        regionalChampions[com.example.data.sync.MetaRegion.normalize(region)] ?: baseChampions.toList()

    @Synchronized
    fun selectMetaRegion(region: String) {
        val normalized = com.example.data.sync.MetaRegion.normalize(region)
        val source = regionalSnapshot(normalized)
        activeRegionName = normalized
        if (source.isNotEmpty()) { champions.clear(); champions.addAll(source) }
    }

    @Synchronized
    fun applyChineseStats(updated: List<Champion>) {
        chineseChampions = updated.toList()
        regionRevision++
        if (activeRegionName == "CN") { champions.clear(); champions.addAll(updated) }
    }

    fun chineseStatsSnapshot(): List<Champion> =
        chineseChampions.ifEmpty { baseChampions.toList() }


    @Synchronized
    fun initChampions(context: android.content.Context, forceReload: Boolean = false) {
        if (champions.isNotEmpty() && !forceReload) return
        try {
            val format = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            val parsed1 = context.resources.openRawResource(com.example.R.raw.champions_part1).bufferedReader().use { reader ->
                format.decodeFromString<List<Champion>>(reader.readText()).map {
                    val updated = it
                    val resolvedAvatar = if (updated.avatarUrl.isBlank() || updated.avatarUrl.startsWith("http")) {
                        "file:///android_asset/champions/${updated.id}.png"
                    } else updated.avatarUrl
                    updated.copy(
                        avatarUrl = resolvedAvatar,
                        hasRegionalStats = false
                    )
                }
            }
            val parsed2 = context.resources.openRawResource(com.example.R.raw.champions_part2).bufferedReader().use { reader ->
                format.decodeFromString<List<Champion>>(reader.readText()).map {
                    val updated = it
                    val resolvedAvatar = if (updated.avatarUrl.isBlank() || updated.avatarUrl.startsWith("http")) {
                        "file:///android_asset/champions/${updated.id}.png"
                    } else updated.avatarUrl
                    updated.copy(
                        avatarUrl = resolvedAvatar,
                        hasRegionalStats = false
                    )
                }
            }
            baseChampions.clear()
            baseChampions.addAll(parsed1)
            baseChampions.addAll(parsed2)
            champions.clear()
            champions.addAll(baseChampions)
            chineseChampions = baseChampions.toList()
            regionalChampions.clear()
            regionRevision++
            android.util.Log.d("WildRiftRepository", "Loaded ${champions.size} champions successfully")
        } catch (e: Exception) {
            lastError = e.stackTraceToString()
            android.util.Log.e("WildRiftRepository", "Failed to load champions", e)
        }
    }

    val EMPTY_CHAMPION = Champion(
        id = "empty",
        name = "Ninguno",
        primaryRole = LaneRole.MID,
        tier = "D",
        winrate = 0.0,
        pickRate = 0.0,
        banRate = 0.0,
        damageType = com.example.model.DamageType.PHYSICAL,
        summary = "Ninguno",
        advantageAgainst = emptyList(),
        counteredBy = emptyList(),
        synergies = emptyList(),
        tacticalAdvice = "",
        recommendedRunes = "",
        isFrontline = false,
        isRanged = false,
        winrateDelta = 0.0,
        pickRateDelta = 0.0,
        banRateDelta = 0.0
    )

    fun getChampionByName(name: String): Champion? {
        if (name.equals("Ninguno", ignoreCase = true) || name.equals("empty", ignoreCase = true)) return EMPTY_CHAMPION
        return champions.find { it.name.equals(name, ignoreCase = true) }
    }

    fun getChampionById(id: String): Champion? {
        if (id.equals("empty", ignoreCase = true)) return EMPTY_CHAMPION
        return champions.find { it.id.equals(canonicalChampionId(id), ignoreCase = true) }
    }

    var syncCycle: Int = 0

    fun computeChampionsForRegionAndTier(
        sourceList: List<Champion>,
        regionId: String,
        tencentTier: com.example.data.sync.TencentRankTier = com.example.data.sync.TencentRankTier.DIAMOND_PLUS
    ): List<Champion> {
        // Preserve the bundled references: no synthetic regional percentages.
        return regionalSnapshot(regionId).ifEmpty { sourceList }
    }

    fun updateStatsForRegionAndTier(regionId: String, tencentTier: com.example.data.sync.TencentRankTier = com.example.data.sync.TencentRankTier.DIAMOND_PLUS) {
        selectMetaRegion(regionId)
    }

    private data class Tuple4(val wr: Double, val pr: Double, val br: Double, val delta: Double)

    /**
     * Obtiene los mejores campeones (Top N) con mayor Win Rate real de un servidor
     * para sincronizar con total exactitud las estadísticas multiserver con la Tier List.
     */
    fun getTopChampionsForServer(
        regionId: String,
        count: Int = 3,
        tencentTier: com.example.data.sync.TencentRankTier = com.example.data.sync.TencentRankTier.DIAMOND_PLUS
    ): List<Champion> {
        val normalized = com.example.data.sync.MetaRegion.normalize(regionId)
        val snapshot = regionalSnapshot(normalized)
        val categories = mapOf("S+" to 6, "S" to 5, "A+" to 4, "A" to 3, "B" to 2, "C" to 1)
        return snapshot.filter { it.hasRegionalStats && it.winrate > 0.0 }.sortedByDescending { it.winrate }.take(count)

    }

    fun getTopChampionForServer(
        regionId: String,
        tencentTier: com.example.data.sync.TencentRankTier = com.example.data.sync.TencentRankTier.DIAMOND_PLUS
    ): Pair<String, Double> {
        val top = getTopChampionsForServer(regionId, 1, tencentTier).firstOrNull()
        return if (top != null) Pair(top.name, top.winrate) else Pair("Sin datos", 0.0)
    }

    fun simulateRegionStatsChange(regionId: String) {
        syncCycle++
        updateStatsForRegionAndTier(regionId, com.example.data.sync.ChineseMetaSyncService.currentTier.value)
    }

    fun simulateTierStatsChange(tier: Any) {
        if (tier is com.example.data.sync.TencentRankTier) updateStatsForRegionAndTier(activeRegionName, tier)
    }

    fun getChampionsByRole(role: LaneRole): List<Champion> {
        return champions
            .filter { it.primaryRole == role || it.secondaryRoles.contains(role) }
            .sortedWith(
                compareByDescending<Champion> { it.primaryRole == role }
                    .thenByDescending { it.tier == "S+" }
                    .thenByDescending { it.tier == "S" }
                    .thenByDescending { it.winrate }
            )
    }

    private fun t(lang: String, pt: String, es: String): String {
        return when (lang) {
            "pt" -> pt
            else -> es
        }
    }

    fun evaluateChampion(
        champ: Champion, myRole: LaneRole, allies: List<Champion>, enemies: List<Champion>,
        enemyLaneOpponent: Champion? = null, lang: String = "es"
    ): DraftRecommendation {
        val activeAllies = allies.filter { it.id != "empty" && it.id != champ.id }.distinctBy { it.id }
        val activeEnemies = (enemies + listOfNotNull(enemyLaneOpponent)).filter { it.id != "empty" }.distinctBy { it.id }
        val favorable = activeEnemies.filter { MatchupKnowledge.relation(champ,it,myRole) == MatchupRelation.FAVORABLE }
        val unfavorable = activeEnemies.filter { MatchupKnowledge.relation(champ,it,myRole) == MatchupRelation.UNFAVORABLE }
        val variable = activeEnemies.filter { MatchupKnowledge.relation(champ,it,myRole) == MatchupRelation.VARIABLE }
        val synergies = MatchupKnowledge.synergies(champ,activeAllies)
        val lane = enemyLaneOpponent?.takeIf { it.id != "empty" }
        val laneRelation = lane?.let { MatchupKnowledge.relation(champ,it,myRole) }
        val offRole = champ.primaryRole != myRole && myRole !in champ.secondaryRoles
        fun names(values: List<Champion>) = values.joinToString { it.getLocalizedName(lang) }
        val badge = when {
            offRole -> t(lang, "ESCOLHA ATÍPICA", "SELECCIÓN ATÍPICA")
            laneRelation == MatchupRelation.UNFAVORABLE -> t(lang, "CONFRONTO DESFAVORÁVEL", "ENFRENTAMIENTO DESFAVORABLE")
            laneRelation == MatchupRelation.VARIABLE -> t(lang, "CONFRONTO VARIÁVEL", "ENFRENTAMIENTO VARIABLE")
            laneRelation == MatchupRelation.FAVORABLE -> t(lang, "VANTAGEM NA ROTA", "VENTAJA EN LÍNEA")
            unfavorable.isNotEmpty() -> t(lang, "CUIDADO COM ${names(unfavorable)}", "CUIDADO CON ${names(unfavorable)}")
            favorable.isNotEmpty() -> t(lang, "VANTAGEM NO CONFRONTO", "VENTAJA EN EL ENFRENTAMIENTO")
            synergies.isNotEmpty() -> t(lang, "SINERGIA DE EQUIPE", "SINERGIA DE EQUIPO")
            else -> t(lang, "ESCOLHA EQUILIBRADA", "ELECCIÓN EQUILIBRADA")
        }
        val reasons = mutableListOf(com.example.util.CoachingGenerator.generateTacticalAdvice(champ,myRole,lang))
        if (laneRelation == MatchupRelation.UNFAVORABLE) reasons += t(lang,
            "${lane!!.getLocalizedName(lang)} tem vantagem neste confronto. Evite trocas prolongadas e procure apoio da equipe.",
            "${lane!!.getLocalizedName(lang)} tiene ventaja en este enfrentamiento. Evita intercambios prolongados y busca apoyo del equipo.")
        if (variable.isNotEmpty()) reasons += t(lang, "Confronto variável contra ${names(variable)}; depende das habilidades e da configuração escolhida.",
            "Enfrentamiento variable contra ${names(variable)}; depende de las habilidades y la configuración elegida.")
        val counterText = buildList {
            if (favorable.isNotEmpty()) add(t(lang,"Vantagem contra: ${names(favorable)}.","Ventaja contra: ${names(favorable)}."))
            if (unfavorable.isNotEmpty()) add(t(lang,"Risco contra: ${names(unfavorable)}.","Riesgo contra: ${names(unfavorable)}."))
        }.joinToString(" ").ifBlank { t(lang,"Nenhuma vantagem direta confirmada.","No hay una ventaja directa confirmada.") }
        val synergyText = if (synergies.isNotEmpty()) t(lang,"Combina com: ${names(synergies)}.","Combina con: ${names(synergies)}.")
            else t(lang,"Ajuste sua função aos aliados selecionados.","Ajusta tu función a los aliados seleccionados.")
        return DraftRecommendation(champion=champ,estimatedWinrate=champ.winrate.coerceIn(0.0,100.0),
            advantageBadge=badge,tacticalReason=reasons.joinToString(" "),runes=champ.recommendedRunes,
            synergyDetails=synergyText,counterDetails=counterText,
            draftFitScore=DraftScoringPolicy.score(champ,myRole,activeAllies,activeEnemies,lane,false,champions))
    }

    fun analyzeDraft(
        myRole: LaneRole? = null,
        allies: List<Champion>,
        enemies: List<Champion>,
        enemyLaneOpponent: Champion? = null,
        isFirstPick: Boolean = false,
        lang: String = "es"
    ): DraftAnalysisResult {
        val serverRegion = com.example.data.sync.ChineseMetaSyncService.currentRegion.value
        val rankTier = com.example.data.sync.ChineseMetaSyncService.currentTier.value.displayName

        val activeEnemies = (enemies + listOfNotNull(enemyLaneOpponent)).filter { it.id != "empty" }.distinctBy { it.id }
        val activeAllies = allies.filter { it.id != "empty" }.distinctBy { it.id }

        val enemyDamage = DraftDamagePolicy.composition(activeEnemies)
        val physPct = enemyDamage[0]; val magicPct = enemyDamage[1]; val truePct = enemyDamage[2]
        val allyDamage = DraftDamagePolicy.composition(activeAllies)
        val allyPhysPct = allyDamage[0]; val allyMagicPct = allyDamage[1]; val allyTruePct = allyDamage[2]
        val isAllyFullAd = activeAllies.size >= 3 && allyPhysPct >= 80
        val isAllyFullAp = activeAllies.size >= 3 && allyMagicPct >= 75
        val allyCompositionWarning = when {
            isAllyFullAd -> com.example.util.trStr(lang,"Exceso de Daño Físico aliado (AD). El rival acumulará armadura.")
            isAllyFullAp -> com.example.util.trStr(lang,"Exceso de Daño Mágico aliado (AP). El rival acumulará resistencia mágica.")
            else -> null
        }

        val frontlineAllies = activeAllies.count { it.isFrontline }
        val frontlineStatus = when {
            activeAllies.isEmpty() -> "No hay campeones aliados"
            frontlineAllies >= 2 -> "Frontline Sólida (${frontlineAllies} Tanques/Luchadores)"
            frontlineAllies == 1 -> "Frontline Moderada (1 Tanque)"
            else -> "¡Alerta! Falta Frontline e Iniciación aliada"
        }

        val isFirstPickEffective = isFirstPick && activeEnemies.isEmpty() && enemyLaneOpponent?.takeIf { it.id != "empty" } == null

        var directMatchupWarning: String? = null
        var directCounterBestPick: String? = null

        val enemyAssassins = activeEnemies.filter { it.id in listOf("zed", "kayn", "talon", "khazix", "akali", "evelynn", "katarina", "fizz", "pyke") }
        val enemyTanks = activeEnemies.filter { it.isFrontline }
        val enemyRangedAdvantage = activeEnemies.filter { it.id in listOf("caitlyn", "lux", "xerath", "varus", "ezreal", "ziggs", "corki") }
        val enemyDashHeavy = activeEnemies.filter { it.id in listOf("yasuo", "yone", "irelia", "riven", "lee_sin", "akali", "katarina", "fizz") }

        if (enemyLaneOpponent != null && enemyLaneOpponent.id != "empty") {
            val opponent = enemyLaneOpponent
            if (myRole == LaneRole.TOP && opponent.isRanged) {
                val topAlert = when (lang) {
                    "pt" -> "Alerta no Top! Enfrentando oponente com alcance/ADC (${opponent.name}). Priorize sustentação (Ventos Revigorantes), controle de onda e espere o caçador."
                    else -> "¡Alerta en Línea de Barón! Enfrentas a un rival con rango/ADC (${opponent.name}). Prioriza sustain (Segundo Aire), control de oleada y espera al jungla."
                }
                directMatchupWarning = topAlert
                directCounterBestPick = "Malphite, Irelia, Pantheon, Wukong"
            } else if (opponent.counteredBy.isNotEmpty()) {
                val countersList = opponent.counteredBy.take(3).joinToString(", ")
                val template = when (lang) {
                    "pt" -> "Rival direto na sua rota: %s. Melhores escolhas para anular: %s."
                    else -> "Rival directo en tu línea: %s. Picks ideales para anularlo: %s."
                }
                directMatchupWarning = String.format(template, opponent.name, countersList)
                directCounterBestPick = countersList
            }
        }

        if (directMatchupWarning == null && (activeAllies.isNotEmpty() || activeEnemies.isNotEmpty())) {
            if (isAllyFullAd && myRole != LaneRole.SUPPORT && myRole != LaneRole.ADC) {
                directMatchupWarning = com.example.util.trStr(lang, "Nuestra composición es full Daño Físico (AD). El enemigo acumulará armadura.")
                directCounterBestPick = com.example.util.trStr(lang, "Selecciona daño mágico (AP) para balancear")
            } else if (isAllyFullAp && myRole != LaneRole.SUPPORT) {
                directMatchupWarning = com.example.util.trStr(lang, "Nuestra composición es full Daño Mágico (AP). El enemigo acumulará resistencia mágica.")
                directCounterBestPick = com.example.util.trStr(lang, "Selecciona daño físico (AD) para balancear")
            } else if (physPct >= 80) {
                directMatchupWarning = com.example.util.trStr(lang, "El enemigo es predominantemente daño Físico (AD).")
                directCounterBestPick = "Rammus, Malphite, o " + com.example.util.trStr(lang, "apilar armadura (Corazón Helado/Malla de Espinas)")
            } else if (magicPct >= 75) {
                directMatchupWarning = com.example.util.trStr(lang, "El enemigo es predominantemente daño Mágico (AP).")
                directCounterBestPick = "Galio, Dr. Mundo, o " + com.example.util.trStr(lang, "apilar resistencia (Fuerza de la Naturaleza)")
            } else if (enemyAssassins.size >= 2) {
                directMatchupWarning = com.example.util.trStr(lang, "Peligro de asesinos de burst") + " (${enemyAssassins.joinToString { it.name }}). " + com.example.util.trStr(lang, "Imprescindible Zhonya/Estasis y CC garantizado (Lulu, Nautilus).")
                directCounterBestPick = "Lulu, Nautilus, Janna"
            } else if (enemyTanks.size >= 2) {
                directMatchupWarning = com.example.util.trStr(lang, "Composición rival pesada/tanque") + " (${enemyTanks.joinToString { it.name }}). " + com.example.util.trStr(lang, "Requiere daño verdadero, % vida máxima y penetración.")
                directCounterBestPick = "Vayne, Gwen, Lilia, o Liandry"
            } else if (enemyRangedAdvantage.size >= 2) {
                directMatchupWarning = com.example.util.trStr(lang, "Composición rival de pokeo y rango") + " (${enemyRangedAdvantage.joinToString { it.name }}). " + com.example.util.trStr(lang, "Evita asedios lentos. Requiere hard engage, emboscada o flanqueos rápidos.")
                directCounterBestPick = "Malphite, Vi, Jarvan IV, Hecarim"
            }
        }

        val availableChampions = champions.filter { champ ->
            champ.id != "empty" &&
            !activeAllies.any { it.id == champ.id } && !activeEnemies.any { it.id == champ.id }
        }

        val candidates = if (myRole != null) {
            availableChampions.filter { champ ->
                champ.primaryRole == myRole || champ.secondaryRoles.contains(myRole)
            }
        } else {
            availableChampions
        }

        val lane = enemyLaneOpponent?.takeIf { it.id != "empty" }
        val uniqueCandidates = candidates.groupBy { it.id }.values.map { profiles -> profiles.firstOrNull { it.primaryRole == myRole } ?: profiles.first() }
        val compatible = if (lane != null) uniqueCandidates.filter { MatchupKnowledge.relation(it,lane,myRole ?: it.primaryRole) != MatchupRelation.UNFAVORABLE } else uniqueCandidates
        val rankedCandidates = compatible.ifEmpty { uniqueCandidates }
        val serverLabel = "Global Meta"

        val recommendations = rankedCandidates.map { champ ->
            val effectiveRole = myRole ?: champ.primaryRole
            var synergyText = ""
            var counterText = ""

            val isFlex = myRole != null && champ.primaryRole != myRole
            val dynamicAdvice = com.example.util.CoachingGenerator.generateTacticalAdvice(champ, effectiveRole, lang)
            val roleContextAdvice = if (isFlex) {
                t(lang, "Flex na ${effectiveRole.getLocalizedName(lang)}: Vantagem de fator surpresa. Desvantagem: Pode sofrer contra escolhas dominantes naturais desta rota. Dicas: $dynamicAdvice", "Flex en ${effectiveRole.getLocalizedName(lang)}: Ventaja de factor sorpresa. Desventaja: Puede sufrir contra picks dominantes naturales de la línea. Consejos: $dynamicAdvice")
            } else {
                dynamicAdvice
            }

            // Detect Iconic Wombocombos with active allies
            val allyIds = allies.map { it.id }
            val comboSynergies = mutableListOf<String>()
            if (champ.id == "yasuo" && (allyIds.any { it in listOf("malphite", "diana", "nautilus", "alistar", "wukong", "aatrox", "rakan", "vi") })) {
                val knockupEnabler = allies.firstOrNull { it.id in listOf("malphite", "diana", "nautilus", "alistar", "wukong", "aatrox", "rakan", "vi") }?.name ?: "Iniciador"
                comboSynergies.add(t(lang, "💥 Combo Aéreo: Arremesso com $knockupEnabler + Ultimate do Yasuo", "💥 Combo Aéreo: Levantamiento con $knockupEnabler + Definitiva de Yasuo"))
            }
            if (champ.id in listOf("malphite", "wukong", "jarvan_iv", "diana") && allyIds.contains("orianna")) {
                comboSynergies.add(t(lang, "💥 Wombocombo Ultimate: Você carrega a esfera de Orianna para Onda de Choque massiva", "💥 Wombocombo Definitiva: Llevas la bola de Orianna para Onda de Choque masiva"))
            }
            if (champ.id == "orianna" && allyIds.any { it in listOf("malphite", "jarvan_iv", "wukong", "vi", "hecarim") }) {
                val carrier = allies.firstOrNull { it.id in listOf("malphite", "jarvan_iv", "wukong", "vi", "hecarim") }?.name ?: "Iniciador"
                comboSynergies.add(t(lang, "💥 Esfera Transportada: Proteja $carrier com Habilidade 3 para iniciar com Ultimate", "💥 Balón Transportado: Protege a $carrier con Habilidad 3 para iniciar con Definitiva"))
            }
            if (champ.id in listOf("miss_fortune", "samira", "katarina") && allyIds.any { it in listOf("amumu", "leona", "nautilus", "malphite", "seraphine") }) {
                val ccChamp = allies.firstOrNull { it.id in listOf("amumu", "leona", "nautilus", "malphite", "seraphine") }?.name ?: "CC"
                comboSynergies.add(t(lang, "💥 CC em Cadeia: Ultimate em área sobre o controle de grupo de $ccChamp", "💥 CC en Cadena: Definitiva en área sobre el control de masas de $ccChamp"))
            }
            if (champ.id in listOf("jinx", "vayne", "twitch", "zeri", "kogmaw") && allyIds.any { it in listOf("lulu", "yuumi", "milio", "janna", "nami") }) {
                val enchanter = allies.firstOrNull { it.id in listOf("lulu", "yuumi", "milio", "janna", "nami") }?.name ?: "Support"
                comboSynergies.add(t(lang, "🛡️ Hipercarregador Peel: Máxima sobrevivência e fortalecimento de dano com $enchanter", "🛡️ Hipercarry Peel: Máxima supervivencia y esteroides de daño con $enchanter"))
            }
            if (champ.id == "braum" && allyIds.contains("lucian")) {
                comboSynergies.add(t(lang, "💥 Passiva Rápida: Lucian ativa suas 4 marcas de atordoamento em 0,5s", "💥 Pasiva Rápida: Lucian activa tus 4 marcas de aturdimiento en 0.5s"))
            }
            if (champ.id == "lucian" && allyIds.any { it in listOf("braum", "nami") }) {
                comboSynergies.add(t(lang, "💥 Sinergia Bot: Ativação instantânea de Bênção/Golpe Concussivo", "💥 Sinergia Bot: Activación instantánea de Bendición/Golpe Conmocionante"))
            }
            if (champ.id == "xayah" && allyIds.contains("rakan") || (champ.id == "rakan" && allyIds.contains("xayah"))) {
                comboSynergies.add(t(lang, "❤️ Dupla Sagrada: Maior alcance na Dança da Batalha e retorno conjunto", "❤️ Dúo Sagrado: Mayor alcance en Danza de Batalla y retirada conjunta"))
            }
            if (champ.id == "hwei" && allyIds.any { it in listOf("amumu", "malphite", "jarvan_iv", "leona", "nautilus") }) {
                val ccChamp = allies.firstOrNull { it.id in listOf("amumu", "malphite", "jarvan_iv", "leona", "nautilus") }?.name ?: "Iniciador"
                comboSynergies.add(t(lang, "💥 Tormento Artístico: Ultimate (H4) sobre o controle de grupo de $ccChamp para detonação da Passiva em área", "💥 Tormento Artístico: Definitiva (H4) sobre el control de masas de $ccChamp para detonación de Pasiva en área"))
            }

            if (isFirstPickEffective) {
                val badge = if (isFlex) t(lang,"PRIMEIRA SELEÇÃO FLEXÍVEL", "PRIMERA SELECCIÓN FLEXIBLE")
                    else t(lang,"PRIMEIRA SELEÇÃO PARA ESTA ROTA", "PRIMERA SELECCIÓN PARA ESTA LÍNEA")

                val possibleWeaknesses = MatchupKnowledge.related(champ,champions,MatchupRelation.UNFAVORABLE).take(3)
                val reason = t(lang,"Seleção inicial de ${champ.getLocalizedName(lang)}: compare a versatilidade, os rivais possíveis e a utilidade para a equipe. $roleContextAdvice",
                    "Primera selección de ${champ.getLocalizedName(lang)}: compara la versatilidad, los rivales posibles y la utilidad para el equipo. $roleContextAdvice")
                synergyText = if (comboSynergies.isNotEmpty()) comboSynergies.joinToString(" • ") else roleContextAdvice
                counterText = if (possibleWeaknesses.isNotEmpty()) t(lang,
                    "Possíveis confrontos difíceis: ${possibleWeaknesses.joinToString { it.getLocalizedName(lang) }}.",
                    "Posibles enfrentamientos difíciles: ${possibleWeaknesses.joinToString { it.getLocalizedName(lang) }}.")
                    else t(lang,"O rival ainda não foi revelado. Reavalie quando ele selecionar.","El rival aún no se ha revelado. Reevalúa cuando seleccione.")

                DraftRecommendation(
                    champion = champ,
                    estimatedWinrate = champ.winrate.coerceIn(0.0,100.0),
                    draftFitScore = DraftScoringPolicy.score(champ,effectiveRole,activeAllies,activeEnemies,lane,isFirstPickEffective,champions),
                    advantageBadge = badge,
                    tacticalReason = reason,
                    runes = champ.recommendedRunes,
                    synergyDetails = synergyText,
                    counterDetails = counterText
                )
            } else {
                // REACTIVE / COUNTER & SYNERGY DRAFT CALCULATION
                val directCounters = activeEnemies.filter { MatchupKnowledge.relation(champ,it,effectiveRole) == MatchupRelation.FAVORABLE }.map { it.getLocalizedName(lang) }
                val directWeaknesses = activeEnemies.filter { MatchupKnowledge.relation(champ,it,effectiveRole) == MatchupRelation.UNFAVORABLE }.map { it.getLocalizedName(lang) }
                val directSynergies = MatchupKnowledge.synergies(champ,activeAllies).map { it.getLocalizedName(lang) }

                val badge = when {
                    lane != null && MatchupKnowledge.relation(champ,lane,effectiveRole) == MatchupRelation.UNFAVORABLE -> t(lang,"CONFRONTO DESFAVORÁVEL", "ENFRENTAMIENTO DESFAVORABLE")
                    lane != null && MatchupKnowledge.relation(champ,lane,effectiveRole) == MatchupRelation.VARIABLE -> t(lang,"CONFRONTO VARIÁVEL", "ENFRENTAMIENTO VARIABLE")
                    directWeaknesses.isNotEmpty() -> t(lang,"RISCO CONTRA ${directWeaknesses.joinToString()}","RIESGO CONTRA ${directWeaknesses.joinToString()}")
                    comboSynergies.isNotEmpty() && directCounters.isNotEmpty() -> t(lang, "🔥 COMBO + COUNTER (+${directCounters.size})", "🔥 COMBO + COUNTER (+${directCounters.size})")
                    comboSynergies.isNotEmpty() -> t(lang, "💥 WOMBO-COMBO ALIADO", "💥 WOMBO-COMBO ALIADO")
                    directCounters.isNotEmpty() && directSynergies.isNotEmpty() -> t(lang, "⭐ #1 SINERGIA + COUNTER", "⭐ #1 SINERGIA + COUNTER")
                    directCounters.isNotEmpty() && champ.tier == "S+" -> t(lang, "⚔️ COUNTER TIER S+ (+${directCounters.size})", "⚔️ COUNTER TIER S+ (+${directCounters.size})")
                    directCounters.isNotEmpty() -> t(lang, "🛡️ COUNTER DIRETO (+${directCounters.size})", "🛡️ COUNTER DIRECTO (+${directCounters.size})")
                    directSynergies.isNotEmpty() -> t(lang, "🤝 SINERGIA DE EQUIPE (+${directSynergies.size})", "🤝 SINERGIA CON EQUIPO (+${directSynergies.size})")
                    champ.tier == "S+" -> t(lang, "👑 PRIORIDADE S+ ($serverLabel)", "👑 PRIORIDAD S+ ($serverLabel)")
                    else -> t(lang, "Escolha Balanceada ($serverLabel)", "Elección Balanceada ($serverLabel)")
                }

                val reasonParts = mutableListOf<String>()
                if (comboSynergies.isNotEmpty()) {
                    reasonParts.add(comboSynergies.joinToString(" • "))
                }
                if (directCounters.isNotEmpty()) {
                    reasonParts.add(t(lang, "Vantagem tática direta contra ${directCounters.joinToString(", ")}.", "Ventaja táctica directa contra ${directCounters.joinToString(", ")}."))
                }
                if (directWeaknesses.isNotEmpty()) reasonParts.add(t(lang,"Cuidado com ${directWeaknesses.joinToString()}; adapte seu posicionamento.","Cuidado con ${directWeaknesses.joinToString()}; adapta tu posicionamiento."))
                if (directSynergies.isNotEmpty()) {
                    reasonParts.add(t(lang, "Sinergia comprovada com ${directSynergies.joinToString(", ")}.", "Sinergia comprobada con ${directSynergies.joinToString(", ")}."))
                }
                if (isAllyFullAd && champ.damageType == DamageType.MAGIC) {
                    reasonParts.add(t(lang, "Fornece o dano mágico crucial para que o rival não acumule armadura.", "Aporta el daño mágico crucial para que el rival no apile armadura."))
                }
                if (activeAllies.isNotEmpty() && frontlineAllies == 0 && champ.isFrontline) {
                    reasonParts.add(t(lang, "Garante a iniciação e resistência que sua equipe precisa.", "Garantiza la iniciación y resistencia que tu equipo necesita."))
                }
                if (reasonParts.isEmpty()) {
                    reasonParts.add(roleContextAdvice)
                } else {
                    reasonParts.add(0, roleContextAdvice)
                }

                synergyText = if (comboSynergies.isNotEmpty()) {
                    comboSynergies.joinToString(" • ")
                } else if (directSynergies.isNotEmpty()) {
                    t(lang, "Combina com: ${directSynergies.joinToString(", ")}", "Combina con: ${directSynergies.joinToString(", ")}")
                } else {
                    t(lang, "Composição de equipe padrão", "Alineación estándar de equipo")
                }

                counterText = if (directCounters.isNotEmpty() && directWeaknesses.isNotEmpty()) {
                    t(lang,"Vantagem contra ${directCounters.joinToString()}; risco contra ${directWeaknesses.joinToString()}.","Ventaja contra ${directCounters.joinToString()}; riesgo contra ${directWeaknesses.joinToString()}.")
                } else if (directCounters.isNotEmpty()) {
                    t(lang, "Anula: ${directCounters.joinToString(", ")}", "Anula a: ${directCounters.joinToString(", ")}")
                } else if (directWeaknesses.isNotEmpty()) {
                    t(lang, "Cuidado com: ${directWeaknesses.joinToString(", ")}", "Cuidado con: ${directWeaknesses.joinToString(", ")}")
                } else {
                    t(lang, "Confronto parelho", "Enfrentamiento parejo")
                }

                DraftRecommendation(
                    champion = champ,
                    estimatedWinrate = champ.winrate.coerceIn(0.0,100.0),
                    draftFitScore = DraftScoringPolicy.score(champ,effectiveRole,activeAllies,activeEnemies,lane,isFirstPickEffective,champions),
                    advantageBadge = badge,
                    tacticalReason = reasonParts.joinToString(" "),
                    runes = champ.recommendedRunes,
                    synergyDetails = synergyText,
                    counterDetails = counterText
                )
            }
        }.sortedByDescending { it.draftFitScore }

        val bestOverall = recommendations.firstOrNull()
        if (lane != null) {
            val choices = recommendations.take(3).joinToString { it.champion.getLocalizedName(lang) }
            directMatchupWarning = t(lang,"Rival na sua rota: ${lane.getLocalizedName(lang)}. Opções para esta rota: $choices.",
                "Rival en tu línea: ${lane.getLocalizedName(lang)}. Opciones para esta línea: $choices.")
        }
        if (recommendations.isNotEmpty()) directCounterBestPick = recommendations.take(3).joinToString { it.champion.getLocalizedName(lang) }

        return DraftAnalysisResult(
            physicalDamagePercent = physPct,
            magicDamagePercent = magicPct,
            trueDamagePercent = truePct,
            allyPhysicalDamagePercent = allyPhysPct,
            allyMagicDamagePercent = allyMagicPct,
            allyTrueDamagePercent = allyTruePct,
            allyCompositionWarning = allyCompositionWarning,
            frontlineStatus = frontlineStatus,
            directMatchupWarning = directMatchupWarning,
            directCounterBestPick = directCounterBestPick,
            isFirstPickMode = isFirstPickEffective,
            bestOverallPick = bestOverall,
            recommendations = recommendations
        )
    }
}
