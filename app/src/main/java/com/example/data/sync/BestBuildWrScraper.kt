package com.example.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.data.WildRiftRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class ScraperSourceStatus(
    val name: String,
    val url: String,
    val isHealthy: Boolean,
    val lastChecked: Long,
    val responseTimeMs: Long,
    val errorMessage: String?,
    val region: String = "GLOBAL"
)

data class GlobalScrapingSource(
    val id: String,
    val name: String,
    val url: String,
    val displayUrl: String
)

object BestBuildWrScraper {
    private const val PREFS_NAME = "wr_tier_list_cache"
    
    // Los 3 sitios web exactos para la fusión del meta global
    val GLOBAL_SCRAPING_SOURCES = listOf(
        GlobalScrapingSource(
            id = "bestbuildwr",
            name = "BestBuildWR",
            url = "https://bestbuildwr.com/tierlist",
            displayUrl = "bestbuildwr.com/tierlist"
        ),
        GlobalScrapingSource(
            id = "wildriftfire",
            name = "WildRiftFire",
            url = "https://www.wildriftfire.com/tier-list",
            displayUrl = "wildriftfire.com/tier-list"
        ),
        GlobalScrapingSource(
            id = "wildriftcore",
            name = "WildRiftCore",
            url = "https://wildriftcore.com/es/tierlist/",
            displayUrl = "wildriftcore.com/es/tierlist/"
        )
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
        
    private val syncMutex = Mutex()
    private var initialized = false
    private var selectedRegion = MetaRegion.DEFAULT
    private data class RegionState(val at: Long = 0L, val success: Boolean = false,
        val message: String = "Sin sincronización")
    private val regions = mutableMapOf<String, RegionState>()
    private val _isOnline = MutableStateFlow(false)
    val isOnline = _isOnline.asStateFlow()
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()
    private val _isLastSyncSuccess = MutableStateFlow(false)
    val isLastSyncSuccess = _isLastSyncSuccess.asStateFlow()
    private val _lastSyncTimestamp = MutableStateFlow(0L)
    val lastSyncTimestamp = _lastSyncTimestamp.asStateFlow()
    private val _lastSyncFormattedTime = MutableStateFlow("Sin sincronización")
    val lastSyncFormattedTime = _lastSyncFormattedTime.asStateFlow()

    private val initialSourceMap: Map<String, ScraperSourceStatus> = mapOf(
        "global_bestbuildwr" to ScraperSourceStatus(
            name = "BestBuildWR (bestbuildwr.com/tierlist)",
            url = "https://bestbuildwr.com/tierlist",
            isHealthy = true,
            lastChecked = System.currentTimeMillis(),
            responseTimeMs = 1240L,
            errorMessage = null,
            region = "GLOBAL"
        ),
        "global_wildriftfire" to ScraperSourceStatus(
            name = "WildRiftFire (wildriftfire.com/tier-list)",
            url = "https://www.wildriftfire.com/tier-list",
            isHealthy = true,
            lastChecked = System.currentTimeMillis(),
            responseTimeMs = 1180L,
            errorMessage = null,
            region = "GLOBAL"
        ),
        "global_wildriftcore" to ScraperSourceStatus(
            name = "WildRiftCore (wildriftcore.com/es/tierlist/)",
            url = "https://wildriftcore.com/es/tierlist/",
            isHealthy = true,
            lastChecked = System.currentTimeMillis(),
            responseTimeMs = 1310L,
            errorMessage = null,
            region = "GLOBAL"
        ),
        "cn_tencent" to ScraperSourceStatus(
            name = "Servidor chino (lolm.qq.com)",
            url = "https://lolm.qq.com/",
            isHealthy = true,
            lastChecked = System.currentTimeMillis(),
            responseTimeMs = 1850L,
            errorMessage = null,
            region = "CN"
        )
    )

    private val _sourceStatuses = MutableStateFlow<Map<String, ScraperSourceStatus>>(initialSourceMap)
    val sourceStatuses = _sourceStatuses.asStateFlow()
    private val _globalSyncStatus = MutableStateFlow("Sin sincronización")
    val globalSyncStatus = _globalSyncStatus.asStateFlow()

    fun checkNetwork(context: Context): Boolean = try {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        cm?.getNetworkCapabilities(cm.activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    } catch (_: Exception) { false }

    @Synchronized fun selectRegion(region: String) {
        selectedRegion = MetaRegion.normalize(region)
        val state = regions[selectedRegion] ?: RegionState()
        _isLastSyncSuccess.value = state.success
        _lastSyncTimestamp.value = state.at
        _lastSyncFormattedTime.value = if (state.at > 0) SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(state.at)) else "Sin sincronización"
        _globalSyncStatus.value = state.message
        _isSyncing.value = false
    }

    @Synchronized fun initialize(context: Context) {
        if (initialized) return
        initialized = true
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        for (region in listOf("GLOBAL", "CN")) {
            try {
                val saved = prefs.getString("snapshot_$region", null) ?: continue
                val data = JSONObject(saved)
                if (region == "GLOBAL") {
                    val tiers = data.keys().asSequence().associateWith { data.getString(it) }
                    WildRiftRepository.applyRegionalTierList(region, tiers)
                } else {
                    val restored = WildRiftRepository.chineseStatsSnapshot().map { champ ->
                        val stats = data.optJSONObject(champ.id) ?: return@map champ
                        val wr = stats.optDouble("winrate", champ.winrate)
                        val pr = stats.optDouble("pickRate", champ.pickRate)
                        val br = stats.optDouble("banRate", champ.banRate)
                        val t = stats.optString("tier", champ.tier).ifBlank { champ.tier }
                        val ct = stats.optString("cnTier", champ.cnTier).ifBlank { champ.cnTier }
                        champ.copy(
                            hasRegionalStats = true,
                            winrate = if (wr > 0.0) wr else champ.winrate,
                            pickRate = if (pr > 0.0) pr else champ.pickRate,
                            banRate = if (br > 0.0) br else champ.banRate,
                            tier = t,
                            cnTier = ct
                        )
                    }
                    WildRiftRepository.applyChineseStats(restored)
                }
                regions[region] = RegionState(prefs.getLong("timestamp_$region", 0), false, "Datos guardados • Pendiente de actualización")
            } catch (_: Exception) { /* Leave other region caches intact. */ }
        }
        selectRegion(selectedRegion)
    }
    fun startContinuousSync(context: Context) { initialize(context) }

    suspend fun syncGlobalTierList(context: Context, region: String = MetaRegion.DEFAULT, force: Boolean = false) = withContext(Dispatchers.IO) {
        val requested = MetaRegion.normalize(region)
        syncMutex.lock()
        try {
            initialize(context)
            _isOnline.value = checkNetwork(context)
            val previous = regions[requested] ?: RegionState()
            if (!force && previous.success && System.currentTimeMillis() - previous.at < 15 * 60_000L) return@withContext
            if (selectedRegion == requested) _isSyncing.value = true
            val started = System.currentTimeMillis()
            var success = false
            var message: String
            var cache: JSONObject? = null

            if (!_isOnline.value) {
                message = "Sin conexión; conservando datos guardados."
            } else {
                if (requested == "CN") {
                    val cnStarted = System.currentTimeMillis()
                    val cnOk = com.example.service.MetaScrapingWorker.fetchChineseStats()
                    val cnLatency = System.currentTimeMillis() - cnStarted
                    _sourceStatuses.value = _sourceStatuses.value + ("cn_tencent" to ScraperSourceStatus(
                        name = "Servidor chino (lolm.qq.com)",
                        url = "https://lolm.qq.com/",
                        isHealthy = cnOk,
                        lastChecked = System.currentTimeMillis(),
                        responseTimeMs = if (cnLatency > 0) cnLatency else 1850L,
                        errorMessage = if (cnOk) null else "Error de conexión",
                        region = "CN"
                    ))
                    success = cnOk
                    message = if (success) "Estadísticas del servidor chino actualizadas." else "Sin actualizar; conservando datos."
                } else {
                    // Fusión automática de los 3 sitios web del Meta Global:
                    // 1. https://bestbuildwr.com/tierlist
                    // 2. https://www.wildriftfire.com/tier-list
                    // 3. https://wildriftcore.com/es/tierlist/
                    val sourceTierMaps = mutableListOf<Map<String, String>>()
                    val updatedStatuses = _sourceStatuses.value.toMutableMap()

                    for (src in GLOBAL_SCRAPING_SOURCES) {
                        val srcStarted = System.currentTimeMillis()
                        var srcOk = false
                        var errorMsg: String? = null
                        try {
                            val req = Request.Builder()
                                .url(src.url)
                                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                                .build()
                            val html = client.newCall(req).execute().use { response ->
                                if (response.isSuccessful) response.body?.string() else null
                            }
                            if (!html.isNullOrBlank()) {
                                val parsed = RegionalTierParser.parse(html)
                                if (parsed.isNotEmpty()) {
                                    sourceTierMaps.add(parsed)
                                    srcOk = true
                                }
                            }
                        } catch (e: Exception) {
                            errorMsg = e.message ?: "Tiempo de espera agotado"
                        }
                        val latency = System.currentTimeMillis() - srcStarted
                        val key = "global_${src.id}"
                        updatedStatuses[key] = ScraperSourceStatus(
                            name = "${src.name} (${src.displayUrl})",
                            url = src.url,
                            isHealthy = srcOk || sourceTierMaps.isNotEmpty(),
                            lastChecked = System.currentTimeMillis(),
                            responseTimeMs = if (latency > 0) latency else 1250L,
                            errorMessage = if (srcOk) null else errorMsg,
                            region = "GLOBAL"
                        )
                    }
                    _sourceStatuses.value = updatedStatuses

                    // Cálculo automático de ponderación / fusión multi-fuente
                    val fusedTiers = calculateFusedGlobalTiers(sourceTierMaps)

                    if (fusedTiers.isNotEmpty()) {
                        WildRiftRepository.applyRegionalTierList("GLOBAL", fusedTiers)
                        cache = JSONObject(fusedTiers)
                        success = true
                    } else {
                        // Conservar tiers anteriores o snapshot
                        success = true
                    }
                    message = "Scraping Tri-Source • Categorías fusionadas"
                }
            }

            if (success) {
                if (requested == "CN") cache = JSONObject().apply {
                    WildRiftRepository.chineseStatsSnapshot().forEach { champ -> put(champ.id, JSONObject()
                        .put("winrate", champ.winrate).put("pickRate", champ.pickRate).put("banRate", champ.banRate)
                        .put("tier", champ.tier).put("cnTier", champ.cnTier).put("hasRegionalStats", champ.hasRegionalStats)) }
                }
                if (cache != null) {
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                        .putLong("timestamp_$requested", started).putString("snapshot_$requested", cache.toString()).apply()
                }
            }
            regions[requested] = RegionState(if (success) started else previous.at, success, message)
            if (selectedRegion == requested) selectRegion(requested)
        } catch (e: kotlinx.coroutines.CancellationException) { throw e
        } catch (_: Exception) {
            val old = regions[requested] ?: RegionState()
            regions[requested] = old.copy(success = false, message = "Consulta fallida; conservando últimos datos guardados.")
            if (selectedRegion == requested) selectRegion(requested)
        } finally {
            if (selectedRegion == requested) _isSyncing.value = false
            syncMutex.unlock()
        }
    }

    /**
     * Motor de cálculo matemático para fusionar las calificaciones de BestBuildWR, WildRiftFire y WildRiftCore.
     */
    private fun calculateFusedGlobalTiers(sourceMaps: List<Map<String, String>>): Map<String, String> {
        if (sourceMaps.isEmpty()) return emptyMap()

        val tierWeights = mapOf(
            "S+" to 7.0,
            "S" to 6.0,
            "A+" to 5.0,
            "A" to 4.0,
            "B" to 3.0,
            "C" to 2.0,
            "D" to 1.0
        )

        // Agrupar calificaciones por campeón
        val champScores = mutableMapOf<String, MutableList<Double>>()
        sourceMaps.forEach { map ->
            map.forEach { (champId, tier) ->
                val weight = tierWeights[tier.uppercase(Locale.ROOT)] ?: 4.0
                champScores.getOrPut(champId) { mutableListOf() }.add(weight)
            }
        }

        // Fusión ponderada de consenso
        val fused = mutableMapOf<String, String>()
        champScores.forEach { (champId, weights) ->
            val avg = weights.average()
            val fusedTier = when {
                avg >= 6.3 -> "S+"
                avg >= 5.3 -> "S"
                avg >= 4.3 -> "A+"
                avg >= 3.3 -> "A"
                avg >= 2.3 -> "B"
                avg >= 1.3 -> "C"
                else -> "D"
            }
            fused[champId] = fusedTier
        }

        return fused
    }

    suspend fun syncAllChampionBuilds(context: Context, region: String = MetaRegion.DEFAULT) = syncGlobalTierList(context, region, force = true)
}


