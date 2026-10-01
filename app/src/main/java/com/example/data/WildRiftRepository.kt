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
    fun getBaseChampion(idOrName: String): Champion? = baseChampions.find { it.id.equals(idOrName, ignoreCase = true) || it.name.equals(idOrName, ignoreCase = true) }
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
        return champions.find { it.id.equals(id, ignoreCase = true) }
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

    private fun t(lang: String, en: String, pt: String, es: String): String {
        return when (lang) {
            "en" -> en
            "pt" -> pt
            else -> es
        }
    }

    fun evaluateChampion(
        champ: Champion,
        myRole: LaneRole,
        allies: List<Champion>,
        enemies: List<Champion>,
        enemyLaneOpponent: Champion? = null,
        lang: String = "es"
    ): DraftRecommendation {
        val otherAllies = allies.filter { it.id != champ.id }
        val allyPhysCount = otherAllies.count { it.damageType == DamageType.PHYSICAL }
        val allyMagicCount = otherAllies.count { it.damageType == DamageType.MAGIC }
        val isAllyFullAd = otherAllies.isNotEmpty() && allyPhysCount >= 3 && allyMagicCount == 0
        val isAllyFullAp = otherAllies.isNotEmpty() && allyMagicCount >= 3 && allyPhysCount == 0
        val frontlineAllies = otherAllies.count { it.isFrontline }
        var score = champ.winrate
        var badge = ""
        val reasonParts = mutableListOf<String>()
        var synergyText = ""
        var counterText = ""
        val isPt = lang.lowercase().startsWith("pt")
        val isEn = lang.lowercase().startsWith("en")
        val isEs = !isPt && !isEn

        // Check for Off-role / Troll pick
        val isOffRole = champ.primaryRole != myRole && !champ.secondaryRoles.contains(myRole)
        if (isOffRole) {
            score -= 15.0 // heavy penalty
            badge = if (isPt) " ESCOLHA ATÍPICA (OFF-META)" else if (isEn) " OFF-META PICK" else " SELECCIÓN ATÍPICA (OFF-META)"
            val msgEs = "Llevar a ${champ.name} a ${com.example.util.trStr(lang, myRole.displayName)} es una selección atípica (off-meta). Sus habilidades no están diseñadas para ganar esta línea. ${com.example.util.trStr(lang, champ.tacticalAdvice)}"
            val msgPt = "Levar ${champ.name} para ${com.example.util.trStr(lang, myRole.displayName)} é uma escolha atípica (off-meta). Suas habilidades não foram projetadas para esta rota. ${com.example.util.trStr(lang, champ.tacticalAdvice)}"
            val msgEn = "Taking ${champ.name} to ${com.example.util.trStr(lang, myRole.displayName)} is an off-meta pick. Their kit isn't designed for this lane. ${com.example.util.trStr(lang, champ.tacticalAdvice)}"
            reasonParts.add(if (isPt) msgPt else if (isEn) msgEn else msgEs)
        }


        val directCounters = champ.advantageAgainst.filter { adv ->
            enemies.any { it.name.equals(adv, ignoreCase = true) || it.id.equals(adv, ignoreCase = true) }
        }.toMutableList()
        enemies.forEach { enemy ->
            if (enemy.counteredBy.any { it.equals(champ.name, ignoreCase = true) || it.equals(champ.id, ignoreCase = true) }) {
                if (!directCounters.any { it.equals(enemy.name, ignoreCase = true) }) {
                    directCounters.add(enemy.name)
                }
            }
        }

        val directWeaknesses = champ.counteredBy.filter { weak ->
            enemies.any { it.name.equals(weak, ignoreCase = true) || it.id.equals(weak, ignoreCase = true) }
        }.toMutableList()
        enemies.forEach { enemy ->
            if (enemy.advantageAgainst.any { it.equals(champ.name, ignoreCase = true) || it.equals(champ.id, ignoreCase = true) }) {
                if (!directWeaknesses.any { it.equals(enemy.name, ignoreCase = true) }) {
                    directWeaknesses.add(enemy.name)
                }
            }
        }

        val directSynergies = champ.synergies.filter { syn ->
            otherAllies.any { it.name.equals(syn, ignoreCase = true) || it.id.equals(syn, ignoreCase = true) }
        }.toMutableList()
        otherAllies.forEach { ally ->
            if (ally.synergies.any { it.equals(champ.name, ignoreCase = true) || it.equals(champ.id, ignoreCase = true) }) {
                if (!directSynergies.any { it.equals(ally.name, ignoreCase = true) }) {
                    directSynergies.add(ally.name)
                }
            }
        }

        // Ponderación de counters y sinergias generales
        score += (directCounters.size * 2.8)
        score -= (directWeaknesses.size * 2.2)
        score += (directSynergies.size * 2.2)

        // Análisis específico del rival directo de línea (Matchup de carril)
        if (enemyLaneOpponent != null) {
            val opponent = enemyLaneOpponent
            val isDirectLaneCounter = champ.advantageAgainst.any { it.equals(opponent.name, ignoreCase = true) || it.equals(opponent.id, ignoreCase = true) } ||
                    opponent.counteredBy.any { it.equals(champ.name, ignoreCase = true) || it.equals(champ.id, ignoreCase = true) }
            val isDirectLaneWeakness = champ.counteredBy.any { it.equals(opponent.name, ignoreCase = true) || it.equals(opponent.id, ignoreCase = true) } ||
                    opponent.advantageAgainst.any { it.equals(champ.name, ignoreCase = true) || it.equals(champ.id, ignoreCase = true) }

            if (isDirectLaneCounter) {
                score += 5.0
                if (badge.isBlank()) badge = if (isPt) " DOMINA A ROTA (${champ.name} vs ${opponent.name})" else " DOMINAS LÍNEA (${champ.name} vs ${opponent.name})"
                reasonParts.add(if (isPt) "Vantagem direta de rota contra ${opponent.name}. Você tem superioridade nas trocas e escalonamento." else "Ventaja directa de carril contra ${opponent.name}. Tienes superioridad en tradeos y escalado.")
            } else if (isDirectLaneWeakness) {
                score -= 4.0
                if (badge.isBlank()) badge = if (isPt) "️ CONFRONTO DESFAVORÁVEL (${opponent.name})" else "️ MATCHUP DESFAVORABLE (${opponent.name})"
                reasonParts.add(if (isPt) "Rota difícil contra ${opponent.name}. Evite trocas longas no início e solicite apoio do caçador." else "Línea difícil contra ${opponent.name}. Evita tradeos largos en early y solicita apoyo del jungla.")
            }

            // Caso especial: ADC / Rango en línea de Barón (Top)
            if (myRole == LaneRole.TOP && opponent.isRanged && !champ.isRanged) {
                reasonParts.add(if (isPt) "Rival com alcance (${opponent.name} no Top): Jogue recuado nos níveis 1-3, compre Escudo de Doran / Ventos Revigorantes e faça all-in quando o rival gastar sua habilidade de fuga." else "Rival con rango (${opponent.name} en Top): Juega pasivo niveles 1-3, compra Escudo de Doran / Segundo Aire y all-in cuando gaste su habilidad de escape.")
            } else if (myRole == LaneRole.TOP && champ.isRanged && !opponent.isRanged) {
                reasonParts.add(if (isPt) "Vantagem de alcance no Top: Pressione ${opponent.name} nos níveis 1-2 mas congele perto da sua torre para evitar emboscadas." else "Ventaja de rango en Top: Acosa a ${opponent.name} en niveles 1-2 pero congela cerca de tu torre para evitar gankeos.")
            }
        }

        if (isAllyFullAd && champ.damageType == DamageType.MAGIC) { score += 3.5 }
        else if (isAllyFullAp && champ.damageType == DamageType.PHYSICAL) { score += 3.5 }
        if (frontlineAllies == 0 && champ.isFrontline) { score += 2.8 }

        if (badge.isBlank()) {
            if (directCounters.isNotEmpty() && directCounters.size >= directWeaknesses.size) {
                badge = if (isPt) " COUNTER FORTE (+${directCounters.size})" else " COUNTER FUERTE (+${directCounters.size})"
                reasonParts.add(if (isPt) "Você tem vantagem sobre ${directCounters.joinToString(", ")}." else "Tienes ventaja sobre ${directCounters.joinToString(", ")}.")
            } else if (directWeaknesses.isNotEmpty() && directWeaknesses.size > directCounters.size) {
                badge = if (isPt) "️ PERIGO NO CONFRONTO (-${directWeaknesses.size})" else "️ PELIGRO MATCHUP (-${directWeaknesses.size})"
                reasonParts.add(if (isPt) "Cuidado: Sofre contra ${directWeaknesses.joinToString(", ")}." else "Cuidado: Sufres contra ${directWeaknesses.joinToString(", ")}.")
            } else if (directSynergies.isNotEmpty()) {
                badge = if (isPt) " SINERGIA COM EQUIPE (+${directSynergies.size})" else " SINERGIA CON EQUIPO (+${directSynergies.size})"
                reasonParts.add(if (isPt) "Sinergia excelente com ${directSynergies.joinToString(", ")}." else "Sinergia óptima con ${directSynergies.joinToString(", ")}.")
            } else if (isAllyFullAd && champ.damageType == DamageType.MAGIC) {
                badge = if (isPt) " DANO MÁGICO ESSENCIAL" else " APERTURA MÁGICA"
                reasonParts.add(if (isPt) "Fornece o dano mágico necessário para evitar que acumulem armadura." else "Aportas el daño mágico necesario para evitar que apilen armadura.")
            } else if (isAllyFullAp && champ.damageType == DamageType.PHYSICAL) {
                badge = if (isPt) "️ DANO FÍSICO ESSENCIAL" else "️ APERTURA FÍSICA"
                reasonParts.add(if (isPt) "Fornece dano físico para evitar resistência mágica." else "Aportas daño físico para evitar resistencia mágica.")
            } else if (frontlineAllies == 0 && champ.isFrontline) {
                badge = if (isPt) "️ LINHA DE FRENTE ESSENCIAL" else "️ SALVADOR FRONTLINE"
                reasonParts.add(if (isPt) "Cobre a falta de tanques e iniciação da equipe." else "Cubres la falta de tanques e iniciación.")
            } else {
                badge = if (isPt) "️ ESCOLHA CONSISTENTE" else "️ SELECCIÓN ESTÁNDAR"
                reasonParts.add(if (isPt) "Opção neutra e consistente neste cenário." else "Opción neutral y consistente en este escenario.")
            }
        }

        val mainSkill = champ.skills.find { it.slot == "1" }?.let { if (isPt) it.namePt.ifBlank { com.example.util.trStr("pt", it.name) } else it.name } ?: champ.skills.firstOrNull()?.name ?: if (isPt) "habilidades" else "habilidades"
        synergyText = if (directSynergies.isNotEmpty()) {
            if (isPt) "Sincronize suas iniciações e combine $mainSkill junto com ${directSynergies.joinToString(", ")} para dominar as lutas de equipe."
            else "Sincroniza tus engages y combina $mainSkill junto con ${directSynergies.joinToString(", ")} para dominar las peleas de equipo."
        } else {
            if (isPt) "Campeão independente. Priorize seu próprio escalonamento e $mainSkill."
            else "Campeón independiente. Prioriza tu propio escalado y $mainSkill."
        }

        counterText = if (directCounters.isNotEmpty()) {
            if (isPt) "Use sua $mainSkill para anular: ${directCounters.joinToString(", ")}."
            else "Usa tu $mainSkill para anular completamente a: ${directCounters.joinToString(", ")}."
        } else if (directWeaknesses.isNotEmpty()) {
            if (isPt) "Cuidado com ${directWeaknesses.joinToString(", ")}, podem interromper sua $mainSkill facilmente."
            else "Cuidado con ${directWeaknesses.joinToString(", ")}, pueden interrumpir tu $mainSkill fácilmente."
        } else {
            if (isPt) "Confronto estável sem counters diretos à vista."
            else "Enfrentamiento estable sin counters directos a la vista."
        }

        val localizedTacticalAdvice = com.example.util.trStr(lang, champ.tacticalAdvice)
        return DraftRecommendation(
            champion = champ,
            estimatedWinrate = ((score * 10).toInt() / 10.0).coerceIn(35.0, 72.0),
            advantageBadge = badge,
            tacticalReason = (localizedTacticalAdvice + " " + reasonParts.joinToString(" ")).trim(),
            runes = champ.recommendedRunes,
            synergyDetails = synergyText,
            counterDetails = counterText
        )
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

        val activeEnemies = enemies.filter { it.id != "empty" }
        val activeAllies = allies.filter { it.id != "empty" }

        var physCount = 0
        var magicCount = 0
        var trueCount = 0

        activeEnemies.forEach { champ ->
            when (champ.damageType) {
                DamageType.PHYSICAL -> physCount++
                DamageType.MAGIC -> magicCount++
                DamageType.TRUE_HYBRID -> trueCount++
            }
        }

        val physPct: Int
        val magicPct: Int
        val truePct: Int
        if (activeEnemies.isEmpty()) {
            physPct = 0
            magicPct = 0
            truePct = 0
        } else {
            val totalEnemies = (physCount + magicCount + trueCount).coerceAtLeast(1)
            physPct = (physCount * 100) / totalEnemies
            magicPct = (magicCount * 100) / totalEnemies
            truePct = (100 - (physPct + magicPct)).coerceAtLeast(0)
        }

        // Ally Damage Profile
        val allyPhysCount = activeAllies.count { it.damageType == DamageType.PHYSICAL }
        val allyMagicCount = activeAllies.count { it.damageType == DamageType.MAGIC }
        val isAllyFullAd = activeAllies.isNotEmpty() && allyPhysCount >= 3 && allyMagicCount == 0
        val isAllyFullAp = activeAllies.isNotEmpty() && allyMagicCount >= 3 && allyPhysCount == 0

        var allyPhysPct = 0
        var allyMagicPct = 0
        var allyTruePct = 0
        var allyCompositionWarning: String? = null
        if (activeAllies.isNotEmpty()) {
            var aPhys = 0
            var aMag = 0
            var aTrue = 0
            activeAllies.forEach {
                when (it.damageType) {
                    DamageType.PHYSICAL -> aPhys++
                    DamageType.MAGIC -> aMag++
                    DamageType.TRUE_HYBRID -> aTrue++
                }
            }
            val totalAlly = (aPhys + aMag + aTrue).coerceAtLeast(1)
            allyPhysPct = (aPhys * 100) / totalAlly
            allyMagicPct = (aMag * 100) / totalAlly
            allyTruePct = (100 - (allyPhysPct + allyMagicPct)).coerceAtLeast(0)

            if (allyPhysPct >= 80) {
                allyCompositionWarning = com.example.util.trStr(lang, "Exceso de Daño Físico aliado (AD). El rival acumulará armadura.")
            } else if (allyMagicPct >= 75) {
                allyCompositionWarning = com.example.util.trStr(lang, "Exceso de Daño Mágico aliado (AP). El rival acumulará resistencia mágica.")
            }
        }

        val frontlineAllies = activeAllies.count { it.isFrontline }
        val frontlineStatus = when {
            activeAllies.isEmpty() -> "No hay campeones aliados"
            frontlineAllies >= 2 -> "Frontline Sólida (${frontlineAllies} Tanques/Luchadores)"
            frontlineAllies == 1 -> "Frontline Moderada (1 Tanque)"
            else -> "¡Alerta! Falta Frontline e Iniciación aliada"
        }

        val isFirstPickEffective = isFirstPick

        // Known safe blind-picks per role in Wild Rift Meta
        val safeBlindPicks = mapOf(
            LaneRole.TOP to listOf("sett", "aatrox", "darius", "renekton", "ornn", "camille", "gwen", "urgot"),
            LaneRole.JUNGLE to listOf("vi", "lee_sin", "xin_zhao", "viego", "wukong", "kayn", "jarvan_iv", "volibear"),
            LaneRole.MID to listOf("ahri", "orianna", "syndra", "yone", "karma", "vex", "jayce", "galio", "hwei"),
            LaneRole.ADC to listOf("varus", "ezreal", "kaisa", "caitlyn", "xayah", "jinx", "lucian", "sivir"),
            LaneRole.SUPPORT to listOf("thresh", "nautilus", "lulu", "nami", "karma", "morgana", "leona", "rakan", "braum")
        )

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
                    "en" -> "Baron Lane Alert! Facing a ranged/ADC opponent (${opponent.name}). Prioritize sustain (Second Wind), wave control, and wait for your jungler."
                    "pt" -> "Alerta no Top! Enfrentando oponente com alcance/ADC (${opponent.name}). Priorize sustentação (Ventos Revigorantes), controle de onda e espere o caçador."
                    else -> "¡Alerta en Línea de Barón! Enfrentas a un rival con rango/ADC (${opponent.name}). Prioriza sustain (Segundo Aire), control de oleada y espera al jungla."
                }
                directMatchupWarning = topAlert
                directCounterBestPick = "Malphite, Irelia, Pantheon, Wukong"
            } else if (opponent.counteredBy.isNotEmpty()) {
                val countersList = opponent.counteredBy.take(3).joinToString(", ")
                val template = when (lang) {
                    "en" -> "Direct opponent in your lane: %s. Ideal picks to counter: %s."
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
            !allies.any { it.id == champ.id } && !enemies.any { it.id == champ.id }
        }

        val candidates = if (myRole != null) {
            availableChampions.filter { champ ->
                champ.primaryRole == myRole || champ.secondaryRoles.contains(myRole)
            }.ifEmpty { availableChampions }
        } else {
            availableChampions
        }

        val serverLabel = "Global Meta"

        val recommendations = candidates.map { champ ->
            var score = champ.winrate

            val effectiveRole = myRole ?: champ.primaryRole
            // Role affinity bonus so recommendations vary correctly across lanes
            if (myRole != null) {
                if (champ.primaryRole == myRole) {
                    score += 10.0
                } else if (champ.secondaryRoles.contains(myRole)) {
                    score += 5.0
                } else {
                    score -= 15.0
                }
            }

            // Tier Bonus
            when (champ.tier) {
                "S+" -> score += 3.5
                "S" -> score += 2.2
                "A+" -> score += 1.2
                "A" -> score += 0.5
            }

            // Server-specific tuning


            var synergyText = ""
            var counterText = ""

            val isFlex = myRole != null && champ.primaryRole != myRole
            val dynamicAdvice = com.example.util.CoachingGenerator.generateTacticalAdvice(champ, effectiveRole, lang)
            val roleContextAdvice = if (isFlex) {
                t(lang,
                    "Flex in ${effectiveRole.displayName}: Surprise factor advantage. Cons: May struggle against natural dominant picks in this lane. Tips: $dynamicAdvice",
                    "Flex no ${effectiveRole.displayName}: Vantagem de fator surpresa. Desvantagem: Pode sofrer contra escolhas dominantes naturais desta rota. Dicas: $dynamicAdvice",
                    "Flex en ${effectiveRole.displayName}: Ventaja de factor sorpresa. Desventaja: Puede sufrir contra picks dominantes naturales de la línea. Consejos: $dynamicAdvice"
                )
            } else {
                dynamicAdvice
            }

            // Detect Iconic Wombocombos with active allies
            val allyIds = allies.map { it.id }
            val comboSynergies = mutableListOf<String>()
            if (champ.id == "yasuo" && (allyIds.any { it in listOf("malphite", "diana", "nautilus", "alistar", "wukong", "aatrox", "rakan", "vi") })) {
                val knockupEnabler = allies.firstOrNull { it.id in listOf("malphite", "diana", "nautilus", "alistar", "wukong", "aatrox", "rakan", "vi") }?.name ?: "Iniciador"
                comboSynergies.add(t(lang, "💥 Combo Aéreo: Levantamiento con $knockupEnabler + Definitiva de Yasuo", "💥 Combo Aéreo: Arremesso com $knockupEnabler + Ultimate do Yasuo", "💥 Combo Aéreo: Levantamiento con $knockupEnabler + Definitiva de Yasuo"))
            }
            if (champ.id in listOf("malphite", "wukong", "jarvan_iv", "diana") && allyIds.contains("orianna")) {
                comboSynergies.add(t(lang, "💥 Wombocombo Definitiva: Llevas la bola de Orianna para Onda de Choque masiva", "💥 Wombocombo Ultimate: Você carrega a esfera de Orianna para Onda de Choque massiva", "💥 Wombocombo Definitiva: Llevas la bola de Orianna para Onda de Choque masiva"))
            }
            if (champ.id == "orianna" && allyIds.any { it in listOf("malphite", "jarvan_iv", "wukong", "vi", "hecarim") }) {
                val carrier = allies.firstOrNull { it.id in listOf("malphite", "jarvan_iv", "wukong", "vi", "hecarim") }?.name ?: "Iniciador"
                comboSynergies.add(t(lang, "💥 Balón Transportado: Protege a $carrier con Habilidad 3 para iniciar con Definitiva", "💥 Esfera Transportada: Proteja $carrier com Habilidade 3 para iniciar com Ultimate", "💥 Balón Transportado: Protege a $carrier con Habilidad 3 para iniciar con Definitiva"))
            }
            if (champ.id in listOf("miss_fortune", "samira", "katarina") && allyIds.any { it in listOf("amumu", "leona", "nautilus", "malphite", "seraphine") }) {
                val ccChamp = allies.firstOrNull { it.id in listOf("amumu", "leona", "nautilus", "malphite", "seraphine") }?.name ?: "CC"
                comboSynergies.add(t(lang, "💥 CC en Cadena: Definitiva en área sobre el control de masas de $ccChamp", "💥 CC em Cadeia: Ultimate em área sobre o controle de grupo de $ccChamp", "💥 CC en Cadena: Definitiva en área sobre el control de masas de $ccChamp"))
            }
            if (champ.id in listOf("jinx", "vayne", "twitch", "zeri", "kogmaw") && allyIds.any { it in listOf("lulu", "yuumi", "milio", "janna", "nami") }) {
                val enchanter = allies.firstOrNull { it.id in listOf("lulu", "yuumi", "milio", "janna", "nami") }?.name ?: "Support"
                comboSynergies.add(t(lang, "🛡️ Hipercarry Peel: Máxima supervivencia y esteroides de daño con $enchanter", "🛡️ Hipercarregador Peel: Máxima sobrevivência e fortalecimento de dano com $enchanter", "🛡️ Hipercarry Peel: Máxima supervivencia y esteroides de daño con $enchanter"))
            }
            if (champ.id == "braum" && allyIds.contains("lucian")) {
                comboSynergies.add(t(lang, "💥 Pasiva Rápida: Lucian activa tus 4 marcas de aturdimiento en 0.5s", "💥 Passiva Rápida: Lucian ativa suas 4 marcas de atordoamento em 0,5s", "💥 Pasiva Rápida: Lucian activa tus 4 marcas de aturdimiento en 0.5s"))
            }
            if (champ.id == "lucian" && allyIds.any { it in listOf("braum", "nami") }) {
                comboSynergies.add(t(lang, "💥 Sinergia Bot: Activación instantánea de Bendición/Golpe Conmocionante", "💥 Sinergia Bot: Ativação instantânea de Bênção/Golpe Concussivo", "💥 Sinergia Bot: Activación instantánea de Bendición/Golpe Conmocionante"))
            }
            if (champ.id == "xayah" && allyIds.contains("rakan") || (champ.id == "rakan" && allyIds.contains("xayah"))) {
                comboSynergies.add(t(lang, "❤️ Dúo Sagrado: Mayor alcance en Danza de Batalla y retirada conjunta", "❤️ Dupla Sagrada: Maior alcance na Dança da Batalha e retorno conjunto", "❤️ Dúo Sagrado: Mayor alcance en Danza de Batalla y retirada conjunta"))
            }
            if (champ.id == "hwei" && allyIds.any { it in listOf("amumu", "malphite", "jarvan_iv", "leona", "nautilus") }) {
                val ccChamp = allies.firstOrNull { it.id in listOf("amumu", "malphite", "jarvan_iv", "leona", "nautilus") }?.name ?: "Iniciador"
                comboSynergies.add(t(lang, "💥 Tormento Artístico: Definitiva (H4) sobre el control de masas de $ccChamp para detonación de Pasiva en área", "💥 Tormento Artístico: Ultimate (H4) sobre o controle de grupo de $ccChamp para detonação da Passiva em área", "💥 Tormento Artístico: Definitiva (H4) sobre el control de masas de $ccChamp para detonación de Pasiva en área"))
            }

            if (isFirstPickEffective) {
                // FIRST PICK / BLIND PICK CALCULATION
                val roleBlindList = safeBlindPicks[effectiveRole] ?: emptyList()
                val isSafeBlind = roleBlindList.contains(champ.id)
                if (isSafeBlind) {
                    score += 6.5 // High priority bonus for genuine safe blind picks
                }
                if (champ.tier == "S+") {
                    score += 3.0
                }

                val badge = when {
                    isSafeBlind && champ.tier == "S+" -> t(lang, "👑 1ER PICK PRIORITARIO (Meta $serverLabel)", "👑 1º PICK PRIORITÁRIO (Meta $serverLabel)", "👑 1ER PICK PRIORITARIO (Meta $serverLabel)")
                    isSafeBlind -> t(lang, "🛡️ BLIND PICK SEGURO (Versátil)", "🛡️ BLIND PICK SEGURO (Versátil)", "🛡️ BLIND PICK SEGURO (Versátil)")
                    champ.tier == "S+" -> t(lang, "⭐ META S+ ($serverLabel)", "⭐ META S+ ($serverLabel)", "⭐ META S+ ($serverLabel)")
                    else -> t(lang, "Opción Estable en ${effectiveRole.shortName}", "Opção Estável no ${effectiveRole.shortName}", "Opción Estable en ${effectiveRole.shortName}")
                }

                val reason = when {
                    isSafeBlind && champ.tier == "S+" ->
                        t(lang,
                            "Prioridad #1 de Primer Pick en ${effectiveRole.displayName} [$serverLabel]: ${champ.name} es el pick a ciegas más seguro y autosuficiente. Domina la línea, resiste ganks y no tiene counters abusivos. $roleContextAdvice",
                            "Prioridade #1 de Primeiro Pick no ${effectiveRole.displayName} [$serverLabel]: ${champ.name} é o pick às cegas mais seguro e autossuficiente. Domina a rota, resiste a emboscadas e não tem counters abusivos. $roleContextAdvice",
                            "Prioridad #1 de Primer Pick en ${effectiveRole.displayName} [$serverLabel]: ${champ.name} es el pick a ciegas más seguro y autosuficiente. Domina la línea, resiste ganks y no tiene counters abusivos. $roleContextAdvice"
                        )
                    isSafeBlind ->
                        t(lang,
                            "Excelente selección a ciegas en ${effectiveRole.displayName}: Gran versatilidad y control de oleadas sin riesgo de ser countereado gravemente. $roleContextAdvice",
                            "Excelente escolha às cegas no ${effectiveRole.displayName}: Grande versatilidade e controle de rotas sem risco de counter pesado. $roleContextAdvice",
                            "Excelente selección a ciegas en ${effectiveRole.displayName}: Gran versatilidad y control de oleadas sin riesgo de ser countereado gravemente. $roleContextAdvice"
                        )
                    else -> roleContextAdvice
                }

                synergyText = if (comboSynergies.isNotEmpty()) {
                    comboSynergies.joinToString(" • ")
                } else {
                    t(lang, "Autosuficiencia en línea y escalado seguro en $serverLabel.", "Autossuficiência na rota e escalamento seguro em $serverLabel.", "Autosuficiencia en línea, control de oleadas y escalado en $serverLabel.")
                }
                counterText = t(lang, "Sin counters directos fatales en el meta de $serverLabel.", "Sem counters diretos fatais no meta de $serverLabel.", "Sin counters directos fatales en el meta de $serverLabel.")

                DraftRecommendation(
                    champion = champ,
                    estimatedWinrate = ((score * 10).toInt() / 10.0).coerceAtMost(72.0),
                    advantageBadge = badge,
                    tacticalReason = reason,
                    runes = champ.recommendedRunes,
                    synergyDetails = synergyText,
                    counterDetails = counterText
                )
            } else {
                // REACTIVE / COUNTER & SYNERGY DRAFT CALCULATION
                val directCounters = champ.advantageAgainst.filter { adv ->
                    enemies.any { it.name.equals(adv, ignoreCase = true) || it.id.equals(adv, ignoreCase = true) }
                }
                val directWeaknesses = champ.counteredBy.filter { weak ->
                    enemies.any { it.name.equals(weak, ignoreCase = true) || it.id.equals(weak, ignoreCase = true) }
                }
                val directSynergies = champ.synergies.filter { syn ->
                    allies.any { it.name.equals(syn, ignoreCase = true) || it.id.equals(syn, ignoreCase = true) }
                }

                // Enemy Counter Scoring
                score += (directCounters.size * 3.2)
                score -= (directWeaknesses.size * 2.5)

                // Ally Synergy Scoring + Combo Bonus
                score += (directSynergies.size * 2.4)
                if (comboSynergies.isNotEmpty()) {
                    score += (comboSynergies.size * 2.8)
                }

                // Damage Balance compensation
                if (isAllyFullAd && champ.damageType == DamageType.MAGIC) {
                    score += 4.0 // Prevents enemy from just building Armor
                } else if (isAllyFullAp && champ.damageType == DamageType.PHYSICAL) {
                    score += 4.0 // Prevents enemy from just building Magic Resist
                }

                // Frontline compensation
                if (frontlineAllies == 0 && champ.isFrontline) {
                    score += 3.0 // Needed frontline
                }

                // Anti-tank or anti-assassin bonus
                if (enemyTanks.size >= 2 && (champ.id in listOf("vayne", "sett", "gwen", "fiora", "varus", "lillia"))) {
                    score += 3.5
                }
                if (enemyDashHeavy.size >= 2 && (champ.id in listOf("vex", "poppy", "gragas", "taliyah", "nautilus", "malphite"))) {
                    score += 3.2
                }

                val badge = when {
                    comboSynergies.isNotEmpty() && directCounters.isNotEmpty() -> t(lang, "🔥 COMBO + COUNTER (+${directCounters.size})", "🔥 COMBO + COUNTER (+${directCounters.size})", "🔥 COMBO + COUNTER (+${directCounters.size})")
                    comboSynergies.isNotEmpty() -> t(lang, "💥 WOMBO-COMBO ALIADO", "💥 WOMBO-COMBO ALIADO", "💥 WOMBO-COMBO ALIADO")
                    directCounters.isNotEmpty() && directSynergies.isNotEmpty() -> t(lang, "⭐ #1 SINERGIA + COUNTER", "⭐ #1 SINERGIA + COUNTER", "⭐ #1 SINERGIA + COUNTER")
                    directCounters.isNotEmpty() && champ.tier == "S+" -> t(lang, "⚔️ COUNTER TIER S+ (+${directCounters.size})", "⚔️ COUNTER TIER S+ (+${directCounters.size})", "⚔️ COUNTER TIER S+ (+${directCounters.size})")
                    directCounters.isNotEmpty() -> t(lang, "🛡️ COUNTER DIRECTO (+${directCounters.size})", "🛡️ COUNTER DIRETO (+${directCounters.size})", "🛡️ COUNTER DIRECTO (+${directCounters.size})")
                    directSynergies.isNotEmpty() -> t(lang, "🤝 SINERGIA DE EQUIPO (+${directSynergies.size})", "🤝 SINERGIA DE EQUIPE (+${directSynergies.size})", "🤝 SINERGIA CON EQUIPO (+${directSynergies.size})")
                    champ.tier == "S+" -> t(lang, "👑 PRIORIDAD S+ ($serverLabel)", "👑 PRIORIDADE S+ ($serverLabel)", "👑 PRIORIDAD S+ ($serverLabel)")
                    else -> t(lang, "Elección Balanceada ($serverLabel)", "Escolha Balanceada ($serverLabel)", "Elección Balanceada ($serverLabel)")
                }

                val reasonParts = mutableListOf<String>()
                if (comboSynergies.isNotEmpty()) {
                    reasonParts.add(comboSynergies.joinToString(" • "))
                }
                if (directCounters.isNotEmpty()) {
                    reasonParts.add(t(lang, "Ventaja táctica directa contra ${directCounters.joinToString(", ")}.", "Vantagem tática direta contra ${directCounters.joinToString(", ")}.", "Ventaja táctica directa contra ${directCounters.joinToString(", ")}."))
                }
                if (directSynergies.isNotEmpty()) {
                    reasonParts.add(t(lang, "Sinergia comprobada con ${directSynergies.joinToString(", ")}.", "Sinergia comprovada com ${directSynergies.joinToString(", ")}.", "Sinergia comprobada con ${directSynergies.joinToString(", ")}."))
                }
                if (isAllyFullAd && champ.damageType == DamageType.MAGIC) {
                    reasonParts.add(t(lang, "Aporta el daño mágico crucial para que el rival no apile armadura.", "Fornece o dano mágico crucial para que o rival não acumule armadura.", "Aporta el daño mágico crucial para que el rival no apile armadura."))
                }
                if (frontlineAllies == 0 && champ.isFrontline) {
                    reasonParts.add(t(lang, "Garantiza la iniciación y resistencia que tu equipo necesita.", "Garante a iniciação e resistência que sua equipe precisa.", "Garantiza la iniciación y resistencia que tu equipo necesita."))
                }
                if (reasonParts.isEmpty()) {
                    reasonParts.add(roleContextAdvice)
                } else {
                    reasonParts.add(0, roleContextAdvice)
                }

                synergyText = if (comboSynergies.isNotEmpty()) {
                    comboSynergies.joinToString(" • ")
                } else if (directSynergies.isNotEmpty()) {
                    t(lang, "Combina con: ${directSynergies.joinToString(", ")}", "Combina com: ${directSynergies.joinToString(", ")}", "Combina con: ${directSynergies.joinToString(", ")}")
                } else {
                    t(lang, "Alineación estándar de equipo", "Composição de equipe padrão", "Alineación estándar de equipo")
                }

                counterText = if (directCounters.isNotEmpty()) {
                    t(lang, "Anula a: ${directCounters.joinToString(", ")}", "Anula: ${directCounters.joinToString(", ")}", "Anula a: ${directCounters.joinToString(", ")}")
                } else if (directWeaknesses.isNotEmpty()) {
                    t(lang, "Cuidado con: ${directWeaknesses.joinToString(", ")}", "Cuidado com: ${directWeaknesses.joinToString(", ")}", "Cuidado con: ${directWeaknesses.joinToString(", ")}")
                } else {
                    t(lang, "Enfrentamiento parejo", "Confronto parelho", "Enfrentamiento parejo")
                }

                DraftRecommendation(
                    champion = champ,
                    estimatedWinrate = ((score * 10).toInt() / 10.0).coerceAtMost(73.5),
                    advantageBadge = badge,
                    tacticalReason = reasonParts.joinToString(" "),
                    runes = champ.recommendedRunes,
                    synergyDetails = synergyText,
                    counterDetails = counterText
                )
            }
        }.sortedByDescending { it.estimatedWinrate }

        val bestOverall = recommendations.firstOrNull()

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
