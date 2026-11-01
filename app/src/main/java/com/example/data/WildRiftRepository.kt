package com.example.data



import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf


import com.example.model.Champion
import com.example.model.ChampionSkill
import com.example.model.DamageType
import com.example.model.DraftAnalysisResult
import com.example.model.DraftRecommendation
import com.example.util.DraftRecommendationText
import com.example.model.LaneRole
import com.example.model.MapObjectiveItem
import com.example.model.RuneItem
import com.example.model.SummonerSpellItem
import com.example.model.WildRiftItem
import com.example.model.MetaDataSource
import com.example.data.sync.ChampionMetaStats

object WildRiftRepository {

    // Versión canónica oficial de Wild Rift
    val CURRENT_PATCH_VERSION = GamePatch.LABEL
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
    val matchupSources: List<MetaDataSource> = listOf(
        MetaDataSource(id = "global_bestbuildwr", name = "BestBuildWR", description = "Guías y tier list Global",
            url = "https://bestbuildwr.com/champions", focusArea = "Categorías y builds"),
        MetaDataSource(id = "global_wildriftfire", name = "WildRiftFire", description = "Guías y tier list Global",
            url = "https://www.wildriftfire.com/", focusArea = "Categorías y builds"),
        MetaDataSource(id = "global_wildriftcore", name = "WildRiftCore", description = "Tier list Global",
            url = "https://wildriftcore.com/es/champions/", focusArea = "Ventajas, debilidades y sinergias"),
        MetaDataSource(id = "global_wildriftcounter", name = "WildRiftCounter", description = "Counters y enfrentamientos",
            url = "https://wildriftcounter.com/", focusArea = "Ventajas y debilidades"),
        MetaDataSource(id = "global_thewildpick", name = "TheWildPick", description = "Counters y selecciones",
            url = "https://thewildpick.com/counter/", focusArea = "Ventajas y debilidades")
    )
    val metaSources: List<MetaDataSource> = listOf(
        MetaDataSource(id = "global_wildriftfire", name = "WildRiftFire", description = "Guías y tier list Global",
            url = "https://www.wildriftfire.com/tier-list", focusArea = "Categorías y builds"),
        MetaDataSource(id = "global_wildriftcore", name = "WildRiftCore", description = "Tier list Global",
            url = "https://wildriftcore.com/en/tierlist/", focusArea = "Categorías de campeones"),
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

    /** SnapshotStateList.toList returns its immutable backing snapshot in O(1).
     * Never hand its live iterator to a long-running OCR/portrait scan. */
    @Synchronized
    fun championSnapshot(): List<Champion> = champions.toList()
    var lastError: String? by mutableStateOf(null)


    private val baseChampions = mutableListOf<Champion>()
    val baseChampionsList: List<Champion> get() = baseChampions.toList()
    private fun canonicalChampionId(id: String): String =
        if (id.equals("nunu_and_willump", ignoreCase = true)) "nunu_willump" else id

    fun getBaseChampion(idOrName: String): Champion? = baseChampions.find {
        it.id.equals(canonicalChampionId(idOrName), ignoreCase = true) || it.name.equals(idOrName, ignoreCase = true)
    }
    var activeRegionName by mutableStateOf(com.example.data.sync.MetaRegion.DEFAULT)
    private val regionalChampions = mutableMapOf<String, List<Champion>>()
    var regionRevision by mutableStateOf(0)
        private set

    @Synchronized
    fun applyRegionalTierList(
        region: String,
        tiers: Map<String, String>,
        stats: Map<String, ChampionMetaStats> = emptyMap(),
        roleStats: Map<String, Map<com.example.model.LaneRole, ChampionMetaStats>> = emptyMap(),
        previousStats: Map<String, ChampionMetaStats> = emptyMap(),
        previousRoleStats: Map<String, Map<com.example.model.LaneRole, ChampionMetaStats>> = emptyMap(),
        previousTiers: Map<String, String> = emptyMap(),
        checkedAt: Long = 0L,
        history: Map<String, List<com.example.data.sync.ObservedRate>> = emptyMap(),
        patchMovements: Map<String, Int>? = null
    ) {
        if (!region.equals(com.example.data.sync.MetaRegion.DEFAULT, ignoreCase = true)) return
        val normalized = com.example.data.sync.MetaRegion.normalize(region)
        val priorMovements = regionalChampions[normalized].orEmpty().associate { it.id to it.tierMovement }
        val updated = baseChampions.map { champion ->
            val category = com.example.data.sync.GlobalTierConsensus.displayCategory(tiers[com.example.data.sync.RegionalTierParser.canonical(champion.id)]
                ?: tiers[com.example.data.sync.RegionalTierParser.canonical(champion.name)]
                ?: champion.tier.ifBlank { "A" })
            val previousTier = previousTiers[com.example.data.sync.RegionalTierParser.canonical(champion.id)]
                ?: previousTiers[com.example.data.sync.RegionalTierParser.canonical(champion.name)]
            fun tierRank(value: String): Int = when (value.trim().uppercase()) {
                "S+" -> 5
                "S" -> 4
                "A+", "A" -> 3
                "B" -> 2
                "C", "D" -> 1
                else -> 0
            }
            val meta = stats[com.example.data.sync.RegionalTierParser.canonical(champion.id)]
                ?: stats[com.example.data.sync.RegionalTierParser.canonical(champion.name)]
            val championRoleStats = roleStats[com.example.data.sync.RegionalTierParser.canonical(champion.id)]
                ?: roleStats[com.example.data.sync.RegionalTierParser.canonical(champion.name)].orEmpty()
            val oldChampionRoleStats = previousRoleStats[com.example.data.sync.RegionalTierParser.canonical(champion.id)]
                ?: previousRoleStats[com.example.data.sync.RegionalTierParser.canonical(champion.name)].orEmpty()

            // Rates are supplied by the current snapshot. Keep the movement
            // indicator tied to the previous snapshot instead of the static
            // values bundled in the catalog.
            val previous = previousStats[com.example.data.sync.RegionalTierParser.canonical(champion.id)]
                ?: previousStats[com.example.data.sync.RegionalTierParser.canonical(champion.name)]
            val samples = history[com.example.data.sync.RegionalTierParser.canonical(champion.id)].orEmpty()
            val day = com.example.data.sync.MetaRateHistory.sampleAtAge(samples, checkedAt, 24)
            val halfDay = com.example.data.sync.MetaRateHistory.sampleAtAge(samples, checkedAt, 12)
            fun change(current: Double?, old: Double?): Double =
                if (current != null && old != null && current.isFinite() && old.isFinite()) current - old else 0.0

            champion.copy(
                hasRegionalStats = meta?.isUsable() == true,
                // Este es el momento de la consulta válida, no el momento de
                // la última muestra histórica. Así la ficha no queda congelada
                // cuando la fuente publica el mismo valor durante un rato.
                statsCheckedAt = if (meta?.isUsable() == true) checkedAt else 0L,
                winrate24h = com.example.data.sync.MetaRateHistory.atAge(history[com.example.data.sync.RegionalTierParser.canonical(champion.id)].orEmpty(), checkedAt, 24),
                winrate12h = com.example.data.sync.MetaRateHistory.atAge(history[com.example.data.sync.RegionalTierParser.canonical(champion.id)].orEmpty(), checkedAt, 12),
                historyUsesDailyReports = day?.daily == true || halfDay?.daily == true,
                has24hComparison = day != null,
                history24hAt = day?.at ?: 0L,
                history12hAt = halfDay?.at ?: 0L,
                tier = category,
                tierMovement = patchMovements?.get(com.example.data.sync.RegionalTierParser.canonical(champion.id)) ?: if (patchMovements != null) 0 else previousTier?.takeIf { it.isNotBlank() }?.let {
                    when {
                        tierRank(category) > tierRank(it) -> 1
                        tierRank(category) < tierRank(it) -> -1
                        else -> priorMovements[champion.id] ?: champion.tierMovement
                    }
                } ?: (priorMovements[champion.id] ?: champion.tierMovement),
                winrate = if (meta != null) meta.winRate ?: Double.NaN else champion.winrate,
                pickRate = if (meta != null) meta.pickRate ?: Double.NaN else champion.pickRate,
                banRate = if (meta != null) meta.banRate ?: Double.NaN else champion.banRate,
                winrateDelta = change(meta?.winRate, day?.value),
                pickRateDelta = change(meta?.pickRate, day?.pick),
                banRateDelta = change(meta?.banRate, day?.ban),
                laneStats = championRoleStats.mapValues { (lane, rates) ->
                    val old = oldChampionRoleStats[lane]
                    com.example.model.ChampionLaneStats(
                        winRate = rates.winRate ?: Double.NaN,
                        pickRate = rates.pickRate ?: Double.NaN,
                        banRate = rates.banRate ?: Double.NaN,
                        winRateDelta = change(rates.winRate, old?.winRate),
                        pickRateDelta = change(rates.pickRate, old?.pickRate),
                        banRateDelta = change(rates.banRate, old?.banRate)
                    )
                }.mapKeys { it.key.name }
            )
        }
        regionalChampions[normalized] = updated
        androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
            regionRevision++
            if (activeRegionName == normalized) {
                champions.clear()
                champions.addAll(updated)
            }
        }
    }

    fun regionalSnapshot(region: String): List<Champion> =
        regionalChampions[com.example.data.sync.MetaRegion.normalize(region)] ?: baseChampions.toList()

    @Synchronized fun applyPatchMovements(movements: Map<String, Int>) {
        regionalChampions.keys.toList().forEach { region ->
            regionalChampions[region] = regionalChampions[region].orEmpty().map {
                it.copy(tierMovement = movements[com.example.data.sync.RegionalTierParser.canonical(it.id)] ?: 0)
            }
        }
        androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
            regionRevision++
            val updated = regionalSnapshot(activeRegionName)
            champions.clear()
            champions.addAll(updated)
        }
    }

    @Synchronized
    fun selectMetaRegion(region: String) {
        val normalized = com.example.data.sync.MetaRegion.normalize(region)
        val source = regionalSnapshot(normalized)
        activeRegionName = normalized
        if (source.isNotEmpty()) androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
            champions.clear()
            champions.addAll(source)
        }
    }

    @Synchronized
    fun initChampions(context: android.content.Context, forceReload: Boolean = false) {
        if (champions.isNotEmpty() && !forceReload) return
        try {
            com.example.util.WebMatchupReferences.load(context)
            val format = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            val parsed1 = context.resources.openRawResource(com.example.R.raw.champions_part1).bufferedReader().use { reader ->
                format.decodeFromString<List<Champion>>(reader.readText()).map {
                    val updated = it
                    val resolvedAvatar = if (updated.avatarUrl.isBlank() || updated.avatarUrl.startsWith("http")) {
                        "file:///android_asset/champions/${updated.id}.png"
                    } else updated.avatarUrl
                    updated.copy(
                        avatarUrl = resolvedAvatar,
                        tier = com.example.data.sync.GlobalTierConsensus.displayCategory(updated.tier),
                        hasRegionalStats = false,
                        winrateDelta = 0.0, pickRateDelta = 0.0, banRateDelta = 0.0
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
                        tier = com.example.data.sync.GlobalTierConsensus.displayCategory(updated.tier),
                        hasRegionalStats = false,
                        winrateDelta = 0.0, pickRateDelta = 0.0, banRateDelta = 0.0
                    )
                }
            }
            baseChampions.clear()
            baseChampions.addAll(parsed1)
            baseChampions.addAll(parsed2)
            androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
                champions.clear()
                champions.addAll(baseChampions)
            }
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
        val reasons = mutableListOf(com.example.util.CoachingGenerator.generateTacticalAdvice(champ,myRole,lang,lane))
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
            else ""
        return DraftRecommendation(champion=champ,estimatedWinrate=(champ.winrate.takeIf { it.isFinite() } ?: 50.0).coerceIn(0.0,100.0),
            advantageBadge=badge,tacticalReason=DraftRecommendationText.clean(reasons.joinToString(" ")),runes=champ.recommendedRunes,
            synergyDetails=DraftRecommendationText.clean(synergyText),counterDetails=DraftRecommendationText.clean(counterText),
            draftFitScore=DraftScoringPolicy.score(champ,myRole,activeAllies,activeEnemies,lane,false,champions))
    }

    fun analyzeDraft(
        myRole: LaneRole? = null,
        allies: List<Champion>,
        enemies: List<Champion>,
        enemyLaneOpponent: Champion? = null,
        isFirstPick: Boolean = false,
        lang: String = "es",
        bannedChampionIds: Set<String> = emptySet()
    ): DraftAnalysisResult {

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

        // A caller can omit the explicit lane slot while still providing a
        // complete enemy roster. Infer the one natural-lane opponent in that
        // case so the one-versus-one rule is never replaced by global tier.
        val lane = enemyLaneOpponent?.takeIf { it.id != "empty" } ?: myRole?.let { role ->
            activeEnemies.filter { it.primaryRole == role }.singleOrNull()
        }
        val isFirstPickEffective = isFirstPick && activeEnemies.isEmpty() && lane == null

        var directMatchupWarning: String? = null
        var directCounterBestPick: String? = null

        val enemyAssassins = activeEnemies.filter { it.id in listOf("zed", "kayn", "talon", "khazix", "akali", "evelynn", "katarina", "fizz", "pyke") }
        val enemyTanks = activeEnemies.filter { it.isFrontline }
        val enemyRangedAdvantage = activeEnemies.filter { it.id in listOf("caitlyn", "lux", "xerath", "varus", "ezreal", "ziggs", "corki") }
        val enemyDashHeavy = activeEnemies.filter { it.id in listOf("yasuo", "yone", "irelia", "riven", "lee_sin", "akali", "katarina", "fizz") }

        if (lane != null) {
            val opponent = lane
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
            champ.id != "empty" && champ.id !in bannedChampionIds &&
            !activeAllies.any { it.id == champ.id } && !activeEnemies.any { it.id == champ.id }
        }

        val candidates = if (myRole != null) {
            val naturalLane = availableChampions.filter { it.primaryRole == myRole }
            // The main draft answer must represent the selected lane. Flex picks
            // are useful only as a fallback for a damaged or incomplete catalog;
            // otherwise a jungle primary should never appear as a mid answer.
            if (naturalLane.size >= 5) naturalLane else availableChampions.filter { champ ->
                champ.primaryRole == myRole || champ.secondaryRoles.contains(myRole)
            }
        } else {
            availableChampions
        }

        val uniqueCandidates = candidates.groupBy { it.id }.values.map { profiles -> profiles.firstOrNull { it.primaryRole == myRole } ?: profiles.first() }
        val compatible = if (lane != null) uniqueCandidates.filter { MatchupKnowledge.relation(it,lane,myRole ?: it.primaryRole) != MatchupRelation.UNFAVORABLE } else uniqueCandidates
        val rankedCandidates = compatible.ifEmpty { uniqueCandidates }
        val serverLabel = "Global Meta"

        val recommendations = rankedCandidates.map { champ ->
            val effectiveRole = myRole ?: champ.primaryRole
            var synergyText = ""
            var counterText = ""

            val isFlex = myRole != null && champ.primaryRole != myRole
            val dynamicAdvice = com.example.util.CoachingGenerator.generateTacticalAdvice(champ, effectiveRole, lang, lane)
            val roleContextAdvice = if (isFlex) {
                t(lang, "Flex na ${effectiveRole.getLocalizedName(lang)}: Vantagem de fator surpresa. Desvantagem: Pode sofrer contra escolhas dominantes naturais desta rota. Dicas: $dynamicAdvice", "Flex en ${effectiveRole.getLocalizedName(lang)}: Ventaja de factor sorpresa. Desventaja: Puede sufrir contra picks dominantes naturales de la línea. Consejos: $dynamicAdvice")
            } else {
                dynamicAdvice
            }

            // Detect Iconic Wombocombos with active allies
            val allyIds = activeAllies.map { it.id }
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
                val knownSynergies = MatchupKnowledge.synergies(champ, activeAllies)
                synergyText = if (comboSynergies.isNotEmpty()) comboSynergies.distinct().joinToString(" • ")
                    else if (knownSynergies.isNotEmpty()) t(lang,
                        "Combina com: ${knownSynergies.joinToString { it.getLocalizedName(lang) }}.",
                        "Combina con: ${knownSynergies.joinToString { it.getLocalizedName(lang) }}.")
                    else ""
                counterText = if (possibleWeaknesses.isNotEmpty()) t(lang,
                    "Possíveis confrontos difíceis: ${possibleWeaknesses.joinToString { it.getLocalizedName(lang) }}.",
                    "Posibles enfrentamientos difíciles: ${possibleWeaknesses.joinToString { it.getLocalizedName(lang) }}.")
                    else t(lang,"O rival ainda não foi revelado. Reavalie quando ele selecionar.","El rival aún no se ha revelado. Reevalúa cuando seleccione.")

                DraftRecommendation(
                    champion = champ,
                    estimatedWinrate = (champ.winrate.takeIf { it.isFinite() } ?: 50.0).coerceIn(0.0,100.0),
                    draftFitScore = DraftScoringPolicy.score(champ,effectiveRole,activeAllies,activeEnemies,lane,isFirstPickEffective,champions),
                    advantageBadge = badge,
                    tacticalReason = DraftRecommendationText.clean(reason),
                    runes = champ.recommendedRunes,
                    synergyDetails = DraftRecommendationText.clean(synergyText),
                    counterDetails = DraftRecommendationText.clean(counterText)
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
                if (isAllyFullAd && champ.damageType == DamageType.MAGIC) {
                    reasonParts.add(t(lang, "Fornece o dano mágico crucial para que o rival não acumule armadura.", "Aporta el daño mágico crucial para que el rival no apile armadura."))
                }
                if (activeAllies.isNotEmpty() && frontlineAllies == 0 && champ.isFrontline) {
                    reasonParts.add(t(lang, "Garante a iniciação e resistência que sua equipe precisa.", "Garantiza la iniciación y resistencia que tu equipo necesita."))
                }
                if (activeEnemies.count { it.isFrontline } >= 2 && champ.id in setOf("darius", "fiora", "gwen", "kaisa", "lillia", "master_yi", "vayne", "varus")) {
                    reasonParts.add(t(lang, "Tem ferramentas para punir uma composição inimiga resistente.", "Tiene herramientas para castigar una composición rival resistente."))
                }
                if (activeEnemies.count { it.id in setOf("akali", "diana", "ekko", "evelynn", "fizz", "katarina", "kayn", "khazix", "rengar", "talon", "zed") } >= 2 && (champ.isFrontline || champ.id in setOf("braum", "janna", "lulu", "milio", "morgana", "nami", "thresh", "yuumi", "soraka"))) {
                    reasonParts.add(t(lang, "Protege a equipe contra a entrada de vários assassinos.", "Protege al equipo contra la entrada de varios asesinos."))
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
                    ""
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
                    estimatedWinrate = (champ.winrate.takeIf { it.isFinite() } ?: 50.0).coerceIn(0.0,100.0),
                    draftFitScore = DraftScoringPolicy.score(champ,effectiveRole,activeAllies,activeEnemies,lane,isFirstPickEffective,champions),
                    advantageBadge = badge,
                    tacticalReason = DraftRecommendationText.clean(reasonParts.joinToString(" ")),
                    runes = champ.recommendedRunes,
                    synergyDetails = DraftRecommendationText.clean(synergyText),
                    counterDetails = DraftRecommendationText.clean(counterText)
                )
            }
        }.sortedByDescending { it.draftFitScore }

        val bestOverall = recommendations.firstOrNull()
        if (lane != null) {
            val choices = recommendations.take(3).joinToString { it.champion.getLocalizedName(lang) }
            directMatchupWarning = t(lang,"Rival na sua rota: ${lane.getLocalizedName(lang)}. Opções para esta rota: $choices.",
                "Rival en tu línea: ${lane.getLocalizedName(lang)}. Opciones para esta línea: $choices.")
        }
        directCounterBestPick = recommendations.take(3).takeIf { it.isNotEmpty() }?.joinToString { it.champion.getLocalizedName(lang) }

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
