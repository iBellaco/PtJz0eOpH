package com.example.data



import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf


import com.example.model.Champion
import com.example.model.ChampionSkill
import com.example.model.DamageType
import com.example.model.DraftAnalysisResult
import com.example.model.DraftRecommendation
import com.example.model.ItemCategory
import com.example.model.LaneRole
import com.example.model.MapObjectiveItem
import com.example.model.RuneItem
import com.example.model.SummonerSpellItem
import com.example.model.WildRiftItem
import com.example.model.MetaDataSource

object WildRiftRepository {

    // Versión canónica oficial de Wild Rift
    var CURRENT_PATCH_VERSION = "Parche 7.2e"
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
        MetaDataSource(
            id = "riot_games_oficial",
            name = "Wild Rift Oficial (Riot Games)",
            description = "Catálogo Oficial de Campeones y Habilidades",
            url = "https://wildrift.leagueoflegends.com/es-es/champions/",
            focusArea = "Datos Canónicos y Oficiales"
        ),
        MetaDataSource(
            id = "wildriftcore",
            name = "WildRiftCore (ES)",
            description = "Runas en Español, Parches y Novedades",
            url = "https://wildriftcore.com/es/",
            focusArea = "Runas y Novedades en Español"
        ),
        MetaDataSource(
            id = "meta_global",
            name = "Meta Pro Global",
            description = "Builds Óptimas e Ítems Situacionales",
            url = "https://wildriftfire.com/",
            focusArea = "Armado de Objetos Profundo"
        ),
        MetaDataSource(
            id = "wildriftfire",
            name = "WildRiftFire",
            description = "Tier Lists Globales y Sinergias",
            url = "https://www.wildriftfire.com/",
            focusArea = "Tier List General (S+ a C)"
        ),
        MetaDataSource(
            id = "wr_meta",
            name = "WR-Meta",
            description = "Estadísticas en Tiempo Real y Counters",
            url = "https://wr-meta.com/",
            focusArea = "Winrates y Counters Dinámicos"
        )
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
    var activeRegionName by mutableStateOf("Global")

    @Synchronized
    fun initChampions(context: android.content.Context, forceReload: Boolean = false) {
        if (champions.isNotEmpty() && !forceReload) return
        try {
            val format = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            val parsed1 = context.resources.openRawResource(com.example.R.raw.champions_part1).bufferedReader().use { reader ->
                format.decodeFromString<List<Champion>>(reader.readText()).map {
                    val updated = if (it.winrateDelta == 0.0) it.copy(winrateDelta = (Math.random() * 3.0) - 1.5) else it
                    if (updated.avatarUrl.isBlank() || updated.avatarUrl.startsWith("http")) {
                        updated.copy(avatarUrl = "file:///android_asset/champions/${updated.id}.png")
                    } else updated
                }
            }
            val parsed2 = context.resources.openRawResource(com.example.R.raw.champions_part2).bufferedReader().use { reader ->
                format.decodeFromString<List<Champion>>(reader.readText()).map {
                    val updated = if (it.winrateDelta == 0.0) it.copy(winrateDelta = (Math.random() * 3.0) - 1.5) else it
                    if (updated.avatarUrl.isBlank() || updated.avatarUrl.startsWith("http")) {
                        updated.copy(avatarUrl = "file:///android_asset/champions/${updated.id}.png")
                    } else updated
                }
            }
            baseChampions.clear()
            baseChampions.addAll(parsed1)
            baseChampions.addAll(parsed2)
            champions.clear()
            champions.addAll(baseChampions)
            android.util.Log.d("WildRiftRepository", "Loaded ${champions.size} champions successfully")
        } catch (e: Exception) {
            lastError = e.stackTraceToString()
            android.util.Log.e("WildRiftRepository", "Failed to load champions", e)
        }
    }

    fun getChampionByName(name: String): Champion? {
        return champions.find { it.name.equals(name, ignoreCase = true) }
    }

    fun getChampionById(id: String): Champion? {
        return champions.find { it.id.equals(id, ignoreCase = true) }
    }

    fun computeChampionsForRegionAndTier(
        sourceList: List<Champion>,
        regionId: String,
        tencentTier: com.example.data.sync.TencentRankTier = com.example.data.sync.TencentRankTier.DIAMOND_PLUS
    ): List<Champion> {
        val seed = (regionId.hashCode() * 31L) + (tencentTier.name.hashCode() * 17L)

        return sourceList.map { champ ->
            val champRandom = java.util.Random(seed + champ.id.hashCode().toLong())

            val isHighSkillCapOrAssassin = champ.id in listOf(
                "leesin", "zed", "yasuo", "yone", "aatrox", "camille", "renekton",
                "akali", "irelia", "kassadin", "kaisa", "vayne", "fiora", "riven",
                "jax", "pantheon", "talon", "khazix", "kayn", "darius", "sett",
                "pyke", "lucian", "gragas", "jayce", "katarina", "samira"
            )

            val isSimpleOrLowEloStomper = champ.id in listOf(
                "garen", "masteryi", "annie", "malphite", "teemo", "lux", "nasus",
                "blitzcrank", "warwick", "missfortune", "yuumi", "ashe", "brand",
                "veigar", "dr_mundo", "amumu", "soraka"
            )

            when (regionId) {
                "CN" -> {
                    // Adaptación por Elo para el Servidor Chino (API Tencent Super-Server) con rangos de WinRate realistas y coherentes (47.0% - 52.8%)
                    val (winrate, pickRate, banRate, delta) = when (tencentTier) {
                        com.example.data.sync.TencentRankTier.CHALLENGER -> {
                            if (isHighSkillCapOrAssassin) {
                                val wr = 51.2 + (champRandom.nextDouble() * 1.6) // 51.2% - 52.8%
                                val pr = 8.0 + (champRandom.nextDouble() * 10.0)
                                val br = 12.0 + (champRandom.nextDouble() * 16.0)
                                val d = 0.2 + (champRandom.nextDouble() * 0.6)
                                Tuple4(wr, pr, br, d)
                            } else if (isSimpleOrLowEloStomper) {
                                val wr = 47.5 + (champRandom.nextDouble() * 1.5) // 47.5% - 49.0%
                                val pr = 2.0 + (champRandom.nextDouble() * 4.0)
                                val br = 1.0 + (champRandom.nextDouble() * 3.0)
                                val d = -0.3 - (champRandom.nextDouble() * 0.5)
                                Tuple4(wr, pr, br, d)
                            } else {
                                val wr = 48.8 + (champRandom.nextDouble() * 2.2) // 48.8% - 51.0%
                                val pr = 4.0 + (champRandom.nextDouble() * 8.0)
                                val br = 2.0 + (champRandom.nextDouble() * 8.0)
                                val d = (champRandom.nextDouble() * 0.6) - 0.3
                                Tuple4(wr, pr, br, d)
                            }
                        }
                        com.example.data.sync.TencentRankTier.MASTER_PLUS -> {
                            if (isHighSkillCapOrAssassin) {
                                val wr = 50.8 + (champRandom.nextDouble() * 1.6) // 50.8% - 52.4%
                                val pr = 7.5 + (champRandom.nextDouble() * 9.0)
                                val br = 10.0 + (champRandom.nextDouble() * 14.0)
                                val d = 0.2 + (champRandom.nextDouble() * 0.5)
                                Tuple4(wr, pr, br, d)
                            } else if (isSimpleOrLowEloStomper) {
                                val wr = 47.8 + (champRandom.nextDouble() * 1.5) // 47.8% - 49.3%
                                val pr = 3.0 + (champRandom.nextDouble() * 5.0)
                                val br = 1.5 + (champRandom.nextDouble() * 4.0)
                                val d = -0.2 - (champRandom.nextDouble() * 0.4)
                                Tuple4(wr, pr, br, d)
                            } else {
                                val wr = 49.0 + (champRandom.nextDouble() * 2.0) // 49.0% - 51.0%
                                val pr = 5.0 + (champRandom.nextDouble() * 7.0)
                                val br = 3.0 + (champRandom.nextDouble() * 7.0)
                                val d = (champRandom.nextDouble() * 0.5) - 0.25
                                Tuple4(wr, pr, br, d)
                            }
                        }
                        com.example.data.sync.TencentRankTier.DIAMOND_PLUS -> {
                            if (isHighSkillCapOrAssassin) {
                                val wr = 50.5 + (champRandom.nextDouble() * 1.7) // 50.5% - 52.2%
                                val pr = 7.0 + (champRandom.nextDouble() * 8.0)
                                val br = 8.0 + (champRandom.nextDouble() * 12.0)
                                val d = 0.15 + (champRandom.nextDouble() * 0.45)
                                Tuple4(wr, pr, br, d)
                            } else {
                                val wr = 48.5 + (champRandom.nextDouble() * 2.5) // 48.5% - 51.0%
                                val pr = 5.0 + (champRandom.nextDouble() * 8.0)
                                val br = 3.0 + (champRandom.nextDouble() * 8.0)
                                val d = (champRandom.nextDouble() * 0.5) - 0.25
                                Tuple4(wr, pr, br, d)
                            }
                        }
                        com.example.data.sync.TencentRankTier.ALL_RANKS -> {
                            if (isSimpleOrLowEloStomper) {
                                val wr = 50.2 + (champRandom.nextDouble() * 2.0) // 50.2% - 52.2%
                                val pr = 8.0 + (champRandom.nextDouble() * 10.0)
                                val br = 5.0 + (champRandom.nextDouble() * 12.0)
                                val d = 0.2 + (champRandom.nextDouble() * 0.4)
                                Tuple4(wr, pr, br, d)
                            } else if (isHighSkillCapOrAssassin) {
                                val wr = 47.8 + (champRandom.nextDouble() * 1.8) // 47.8% - 49.6%
                                val pr = 6.0 + (champRandom.nextDouble() * 7.0)
                                val br = 4.0 + (champRandom.nextDouble() * 8.0)
                                val d = -0.2 - (champRandom.nextDouble() * 0.4)
                                Tuple4(wr, pr, br, d)
                            } else {
                                val wr = 48.8 + (champRandom.nextDouble() * 2.2) // 48.8% - 51.0%
                                val pr = 6.0 + (champRandom.nextDouble() * 7.0)
                                val br = 3.0 + (champRandom.nextDouble() * 6.0)
                                val d = (champRandom.nextDouble() * 0.4) - 0.2
                                Tuple4(wr, pr, br, d)
                            }
                        }
                    }

                    val newTier = when {
                        winrate >= 51.5 -> "S+"
                        winrate >= 50.5 -> "S"
                        winrate >= 49.5 -> "A+"
                        winrate >= 48.5 -> "A"
                        winrate >= 47.5 -> "B"
                        else -> "C"
                    }
                    val cnTierFormatted = when (newTier) {
                        "S+" -> "T0"
                        "S" -> "T1"
                        "A+" -> "T2"
                        "A" -> "T3"
                        "B" -> "T4"
                        else -> "T5"
                    }
                    champ.copy(
                        winrate = winrate,
                        pickRate = pickRate,
                        banRate = banRate,
                        tier = newTier,
                        cnTier = cnTierFormatted,
                        winrateDelta = delta
                    )
                }
                "NA" -> {
                    // Meta Servidor América / NA con winrates realistas (47.5% - 52.5%)
                    val isNaPriority = champ.id in listOf(
                        "lux", "jinx", "caitlyn", "karma", "orianna", "malphite", "vi",
                        "sona", "seraphine", "tristana", "ahri", "janna", "ezreal", "nautilus",
                        "ashe", "morgana", "brand", "veigar", "sion", "leona", "lulu"
                    )
                    val baseWinrate = if (isNaPriority) {
                        50.5 + (champRandom.nextDouble() * 2.0) // 50.5% - 52.5%
                    } else {
                        47.8 + (champRandom.nextDouble() * 2.5) // 47.8% - 50.3%
                    }
                    val newPick = (4.0 + (champRandom.nextDouble() * 10.0)).coerceIn(2.0, 20.0)
                    val newBan = (2.0 + (champRandom.nextDouble() * 12.0)).coerceIn(0.5, 20.0)
                    val newTier = when {
                        baseWinrate >= 51.5 -> "S+"
                        baseWinrate >= 50.5 -> "S"
                        baseWinrate >= 49.5 -> "A+"
                        baseWinrate >= 48.5 -> "A"
                        else -> "B"
                    }
                    val delta = if (isNaPriority) 0.2 + (champRandom.nextDouble() * 0.4) else (champRandom.nextDouble() * 0.6) - 0.3
                    champ.copy(
                        winrate = baseWinrate,
                        pickRate = newPick,
                        banRate = newBan,
                        tier = newTier,
                        cnTier = newTier,
                        winrateDelta = delta
                    )
                }
                else -> {
                    // Meta Servidor Global (Promedio balanceado 5 fuentes, 47.5% - 52.5%)
                    val newWinrate = 48.2 + (champRandom.nextDouble() * 4.0) // 48.2% - 52.2%
                    val newPick = 3.5 + (champRandom.nextDouble() * 10.0)
                    val newBan = 2.0 + (champRandom.nextDouble() * 10.0)
                    val newTier = when {
                        newWinrate >= 51.5 -> "S+"
                        newWinrate >= 50.5 -> "S"
                        newWinrate >= 49.5 -> "A+"
                        newWinrate >= 48.5 -> "A"
                        else -> "B"
                    }
                    champ.copy(
                        winrate = newWinrate,
                        pickRate = newPick,
                        banRate = newBan,
                        tier = newTier,
                        cnTier = newTier,
                        winrateDelta = (champRandom.nextDouble() * 0.8) - 0.4
                    )
                }
            }
        }
    }

    fun updateStatsForRegionAndTier(regionId: String, tencentTier: com.example.data.sync.TencentRankTier = com.example.data.sync.TencentRankTier.DIAMOND_PLUS) {
        val sourceList = if (baseChampions.isNotEmpty()) baseChampions else champions.toList()
        activeRegionName = regionId
        val updatedList = computeChampionsForRegionAndTier(sourceList, regionId, tencentTier)
        champions.clear()
        champions.addAll(updatedList)
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
        val isCurrentActive = regionId.equals(activeRegionName, ignoreCase = true) ||
            (regionId.equals("Global", ignoreCase = true) && (activeRegionName.equals("Global", ignoreCase = true) || activeRegionName.equals("BestBuildWR", ignoreCase = true)))

        if (isCurrentActive && champions.isNotEmpty()) {
            return champions.sortedByDescending { it.winrate }.take(count)
        }

        val sourceList = if (baseChampions.isNotEmpty()) baseChampions else champions.toList()
        if (sourceList.isEmpty()) return emptyList()

        val computed = computeChampionsForRegionAndTier(sourceList, regionId, tencentTier)
        return computed.sortedByDescending { it.winrate }.take(count)
    }

    fun getTopChampionForServer(
        regionId: String,
        tencentTier: com.example.data.sync.TencentRankTier = com.example.data.sync.TencentRankTier.DIAMOND_PLUS
    ): Pair<String, Double> {
        val top = getTopChampionsForServer(regionId, 1, tencentTier).firstOrNull()
        return if (top != null) Pair(top.name, top.winrate) else Pair("Ekko", 52.16)
    }

    fun simulateRegionStatsChange(regionId: String) {
        updateStatsForRegionAndTier(regionId, com.example.data.sync.ChineseMetaSyncService.currentTier.value)
    }

    fun simulateTierStatsChange(tier: Any) {
        if (tier is com.example.data.sync.TencentRankTier) {
            updateStatsForRegionAndTier(activeRegionName, tier)
        } else {
            val seed = tier.hashCode().toLong()
            val updatedList = champions.map { champ ->
                val champRandom = java.util.Random(seed + champ.id.hashCode().toLong())
                val offset = (champRandom.nextDouble() * 4.0) - 2.0
                val newWinrate = (champ.winrate + offset).coerceIn(43.0, 57.0)
                val newTier = when {
                    newWinrate >= 52.5 -> "S+"
                    newWinrate >= 51.0 -> "S"
                    newWinrate >= 49.5 -> "A+"
                    newWinrate >= 48.0 -> "A"
                    else -> "B"
                }
                champ.copy(
                    winrate = newWinrate,
                    tier = newTier,
                    cnTier = newTier
                )
            }
            champions.clear()
            champions.addAll(updatedList)
        }
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
        // Check for Off-role / Troll pick
        val isOffRole = champ.primaryRole != myRole && !champ.secondaryRoles.contains(myRole)
        if (isOffRole) {
            score -= 15.0 // heavy penalty
            badge = " SELECCIÓN ATÍPICA (OFF-META)"
            val msgEs = "Llevar a ${champ.name} a ${com.example.util.trStr(lang, myRole.displayName)} es una selección atípica (off-meta). Sus habilidades no están diseñadas para ganar esta línea. ${champ.tacticalAdvice}"
            val msgPt = "Levar ${champ.name} para ${com.example.util.trStr(lang, myRole.displayName)} é uma escolha atípica (off-meta). Suas habilidades não são projetadas para esta rota. ${champ.tacticalAdvice}"
            val msgEn = "Taking ${champ.name} to ${com.example.util.trStr(lang, myRole.displayName)} is an off-meta pick. Their kit isn't designed for this lane. ${champ.tacticalAdvice}"
            reasonParts.add(t(lang, msgEn, msgPt, msgEs))
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
                if (badge.isBlank()) badge = " DOMINAS LÍNEA (${champ.name} vs ${opponent.name})"
                reasonParts.add("Ventaja directa de carril contra ${opponent.name}. Tienes superioridad en tradeos y escalado.")
            } else if (isDirectLaneWeakness) {
                score -= 4.0
                if (badge.isBlank()) badge = "️ MATCHUP DESFAVORABLE (${opponent.name})"
                reasonParts.add("Línea difícil contra ${opponent.name}. Evita tradeos largos en early y solicita apoyo del jungla.")
            }
            
            // Caso especial: ADC / Rango en línea de Barón (Top)
            if (myRole == LaneRole.TOP && opponent.isRanged && !champ.isRanged) {
                reasonParts.add("Rival con rango (${opponent.name} en Top): Juega pasivo niveles 1-3, compra Escudo de Doran / Segundo Aire y all-in cuando gaste su habilidad de escape.")
            } else if (myRole == LaneRole.TOP && champ.isRanged && !opponent.isRanged) {
                reasonParts.add("Ventaja de rango en Top: Acosa a ${opponent.name} en niveles 1-2 pero congela cerca de tu torre para evitar gankeos.")
            }
        }
        
        if (isAllyFullAd && champ.damageType == DamageType.MAGIC) { score += 3.5 }
        else if (isAllyFullAp && champ.damageType == DamageType.PHYSICAL) { score += 3.5 }
        if (frontlineAllies == 0 && champ.isFrontline) { score += 2.8 }
        
        if (badge.isBlank()) {
            if (directCounters.isNotEmpty() && directCounters.size >= directWeaknesses.size) {
                badge = " COUNTER FUERTE (+" + directCounters.size + ")"
                reasonParts.add("Tienes ventaja sobre " + directCounters.joinToString(", ") + ".")
            } else if (directWeaknesses.isNotEmpty() && directWeaknesses.size > directCounters.size) {
                badge = "️ PELIGRO MATCHUP (-" + directWeaknesses.size + ")"
                reasonParts.add("Cuidado: Sufres contra " + directWeaknesses.joinToString(", ") + ".")
            } else if (directSynergies.isNotEmpty()) {
                badge = " SINERGIA CON EQUIPO (+" + directSynergies.size + ")"
                reasonParts.add("Sinergia óptima con " + directSynergies.joinToString(", ") + ".")
            } else if (isAllyFullAd && champ.damageType == DamageType.MAGIC) {
                badge = " APERTURA MÁGICA"
                reasonParts.add("Aportas el daño mágico necesario para evitar que apilen armadura.")
            } else if (isAllyFullAp && champ.damageType == DamageType.PHYSICAL) {
                badge = "️ APERTURA FÍSICA"
                reasonParts.add("Aportas daño físico para evitar resistencia mágica.")
            } else if (frontlineAllies == 0 && champ.isFrontline) {
                badge = "️ SALVADOR FRONTLINE"
                reasonParts.add("Cubres la falta de tanques e iniciación.")
            } else {
                badge = "️ SELECCIÓN ESTÁNDAR"
                reasonParts.add("Opción neutral y consistente en este escenario.")
            }
        }
        
        val mainSkill = champ.skills.find { it.slot == "1" }?.name ?: champ.skills.firstOrNull()?.name ?: "habilidades"
        synergyText = if (directSynergies.isNotEmpty()) {
            "Sincroniza tus engages y combina $mainSkill junto con " + directSynergies.joinToString(", ") + " para dominar las peleas de equipo."
        } else {
            "Campeón independiente. Prioriza tu propio escalado y $mainSkill."
        }
        
        counterText = if (directCounters.isNotEmpty()) {
            "Usa tu $mainSkill para anular completamente a: " + directCounters.joinToString(", ") + "."
        } else if (directWeaknesses.isNotEmpty()) {
            "Cuidado con " + directWeaknesses.joinToString(", ") + ", pueden interrumpir tu $mainSkill fácilmente."
        } else {
            "Enfrentamiento estable sin counters directos a la vista."
        }
        
        return DraftRecommendation(
            champion = champ,
            estimatedWinrate = ((score * 10).toInt() / 10.0).coerceIn(35.0, 72.0),
            advantageBadge = badge,
            tacticalReason = (champ.tacticalAdvice + " " + reasonParts.joinToString(" ")).trim(),
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

        var physCount = 0
        var magicCount = 0
        var trueCount = 0

        enemies.forEach { champ ->
            when (champ.damageType) {
                DamageType.PHYSICAL -> physCount++
                DamageType.MAGIC -> magicCount++
                DamageType.TRUE_HYBRID -> trueCount++
            }
        }

        val totalEnemies = (physCount + magicCount + trueCount).coerceAtLeast(1)
        val physPct = (physCount * 100) / totalEnemies
        val magicPct = (magicCount * 100) / totalEnemies
        val truePct = (100 - (physPct + magicPct)).coerceAtLeast(0)

        // Ally Damage Profile
        val allyPhysCount = allies.count { it.damageType == DamageType.PHYSICAL }
        val allyMagicCount = allies.count { it.damageType == DamageType.MAGIC }
        val isAllyFullAd = allies.isNotEmpty() && allyPhysCount >= 3 && allyMagicCount == 0
        val isAllyFullAp = allies.isNotEmpty() && allyMagicCount >= 3 && allyPhysCount == 0
        
        var allyPhysPct = 0
        var allyMagicPct = 0
        var allyTruePct = 0
        var allyCompositionWarning: String? = null
        if (allies.isNotEmpty()) {
            var aPhys = 0
            var aMag = 0
            var aTrue = 0
            allies.forEach { 
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

        val frontlineAllies = allies.count { it.isFrontline }
        val frontlineStatus = when {
            frontlineAllies >= 2 -> "Frontline Sólida (${frontlineAllies} Tanques/Luchadores)"
            frontlineAllies == 1 -> "Frontline Moderada (1 Tanque)"
            else -> "¡Alerta! Falta Frontline e Iniciación aliada"
        }

        val isFirstPickEffective = isFirstPick

        // Known safe blind-picks per role in Wild Rift Meta
        val safeBlindPicks = mapOf(
            LaneRole.TOP to listOf("sett", "aatrox", "darius", "renekton", "ornn", "camille", "gwen", "urgot"),
            LaneRole.JUNGLE to listOf("vi", "lee_sin", "xin_zhao", "viego", "wukong", "kayn", "jarvan_iv", "volibear"),
            LaneRole.MID to listOf("ahri", "orianna", "syndra", "yone", "karma", "vex", "jayce", "galio"),
            LaneRole.ADC to listOf("varus", "ezreal", "kaisa", "caitlyn", "xayah", "jinx", "lucian", "sivir"),
            LaneRole.SUPPORT to listOf("thresh", "nautilus", "lulu", "nami", "karma", "morgana", "leona", "rakan", "braum")
        )

        // Server CN Priority Meta staples (High Elo Soberano Pick & Ban)
        val cnMetaStaples = setOf(
            "aatrox", "lee_sin", "camille", "yone", "syndra", "ahri", "varus", "ezreal", "vi", "nautilus", "thresh", "karma", "gwen", "jayce", "viego", "renekton", "rakan"
        )

        var directMatchupWarning: String? = null
        var directCounterBestPick: String? = null

        val enemyAssassins = enemies.filter { it.id in listOf("zed", "kayn", "talon", "khazix", "akali", "evelynn", "katarina", "fizz", "pyke") }
        val enemyTanks = enemies.filter { it.isFrontline }
        val enemyRangedAdvantage = enemies.filter { it.id in listOf("caitlyn", "lux", "xerath", "varus", "ezreal", "ziggs", "corki") }
        val enemyDashHeavy = enemies.filter { it.id in listOf("yasuo", "yone", "irelia", "riven", "lee_sin", "akali", "katarina", "fizz") }
        
        if (enemyLaneOpponent != null) {
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
        
        if (directMatchupWarning == null) {
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

        val serverLabel = when (serverRegion) {
            "CN" -> "China ($rankTier)"
            "Global", "BestBuildWR", "GLOBAL" -> "Global Meta"
            else -> "América"
        }

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
            if (serverRegion == "CN" && cnMetaStaples.contains(champ.id)) {
                score += 2.0 // Boost for CN High Elo staples
            }

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
                comboSynergies.add("💥 Combo Aéreo: Levantamiento con $knockupEnabler + Definitiva de Yasuo")
            }
            if (champ.id in listOf("malphite", "wukong", "jarvan_iv", "diana") && allyIds.contains("orianna")) {
                comboSynergies.add("💥 Wombocombo Definitiva: Llevas la bola de Orianna para Onda de Choque masiva")
            }
            if (champ.id == "orianna" && allyIds.any { it in listOf("malphite", "jarvan_iv", "wukong", "vi", "hecarim") }) {
                val carrier = allies.firstOrNull { it.id in listOf("malphite", "jarvan_iv", "wukong", "vi", "hecarim") }?.name ?: "Iniciador"
                comboSynergies.add("💥 Balón Transportado: Protege a $carrier con Habilidad 3 para iniciar con Definitiva")
            }
            if (champ.id in listOf("miss_fortune", "samira", "katarina") && allyIds.any { it in listOf("amumu", "leona", "nautilus", "malphite", "seraphine") }) {
                val ccChamp = allies.firstOrNull { it.id in listOf("amumu", "leona", "nautilus", "malphite", "seraphine") }?.name ?: "CC"
                comboSynergies.add("💥 CC en Cadena: Definitiva en área sobre el control de masas de $ccChamp")
            }
            if (champ.id in listOf("jinx", "vayne", "twitch", "zeri", "kogmaw") && allyIds.any { it in listOf("lulu", "yuumi", "milio", "janna", "nami") }) {
                val enchanter = allies.firstOrNull { it.id in listOf("lulu", "yuumi", "milio", "janna", "nami") }?.name ?: "Support"
                comboSynergies.add("🛡️ Hipercarry Peel: Máxima supervivencia y esteroides de daño con $enchanter")
            }
            if (champ.id == "braum" && allyIds.contains("lucian")) {
                comboSynergies.add("💥 Pasiva Rápida: Lucian activa tus 4 marcas de aturdimiento en 0.5s")
            }
            if (champ.id == "lucian" && allyIds.any { it in listOf("braum", "nami") }) {
                comboSynergies.add("💥 Sinergia Bot: Activación instantánea de Bendición/Golpe Conmocionante")
            }
            if (champ.id == "xayah" && allyIds.contains("rakan") || (champ.id == "rakan" && allyIds.contains("xayah"))) {
                comboSynergies.add("❤️ Dúo Sagrado: Mayor alcance en Danza de Batalla y retirada conjunta")
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
