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

data class ScraperSourceStatus(val name: String, val url: String, val isHealthy: Boolean,
    val lastChecked: Long, val responseTimeMs: Long, val errorMessage: String?, val region: String = "CN")

object BestBuildWrScraper {
    private const val PREFS_NAME = "wr_tier_list_cache"
    const val GLOBAL_URL = "https://www.wildriftfire.com/tier-list"
    private val client = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).build()
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
    private val _sourceStatuses = MutableStateFlow<Map<String, ScraperSourceStatus>>(emptyMap())
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
                        champ.copy(hasRegionalStats = stats.optBoolean("hasRegionalStats", true), winrate = stats.optDouble("winrate", champ.winrate),
                            pickRate = stats.optDouble("pickRate", champ.pickRate), banRate = stats.optDouble("banRate", champ.banRate),
                            tier = stats.optString("tier", champ.tier), cnTier = stats.optString("cnTier", champ.cnTier))
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
            val source = when (requested) { "CN" -> "Servidor chino"; "NA" -> "América (NA)"; else -> "WildRiftFire" }
            val url = when (requested) { "CN" -> "https://lolm.qq.com/"; "NA" -> "https://www.wildriftstats.org/champions"; else -> GLOBAL_URL }
            if (requested == "NA") {
                message = "NA sin fuente disponible • Consulta la lista Global"
            } else if (!_isOnline.value) {
                message = "Sin conexión; conservando datos guardados."
            } else {
                success = if (requested == "CN") com.example.service.MetaScrapingWorker.fetchChineseStats() else {
                    val req = Request.Builder().url(GLOBAL_URL).header("User-Agent", "Coach/1.1 Android").build()
                    val html = client.newCall(req).execute().use { response -> if (response.isSuccessful) response.body?.string() else null }
                    val tiers = html?.let(RegionalTierParser::parse).orEmpty()
                    val matched = WildRiftRepository.regionalSnapshot("GLOBAL").count {
                        RegionalTierParser.canonical(it.id) in tiers || RegionalTierParser.canonical(it.name) in tiers }
                    if (matched >= 30) {
                        WildRiftRepository.applyRegionalTierList("GLOBAL", tiers)
                        cache = JSONObject(tiers)
                        true
                    } else false
                }
                message = if (success) {
                    if (requested == "CN") "Estadísticas del servidor chino actualizadas." else "WildRiftFire • Categorías actualizadas • Sin porcentajes regionales"
                } else "Sin actualizar; conservando últimos datos guardados."
            }
            if (success) {
                if (requested == "CN") cache = JSONObject().apply {
                    WildRiftRepository.chineseStatsSnapshot().forEach { champ -> put(champ.id, JSONObject()
                        .put("winrate", champ.winrate).put("pickRate", champ.pickRate).put("banRate", champ.banRate)
                        .put("tier", champ.tier).put("cnTier", champ.cnTier).put("hasRegionalStats", champ.hasRegionalStats)) }
                }
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                    .putLong("timestamp_$requested", started).putString("snapshot_$requested", cache.toString()).apply()
            }
            regions[requested] = RegionState(if (success) started else previous.at, success, message)
            _sourceStatuses.value = _sourceStatuses.value + (requested to ScraperSourceStatus(source, url, success,
                started, System.currentTimeMillis() - started, if (success) null else message, requested))
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
    suspend fun syncAllChampionBuilds(context: Context, region: String = MetaRegion.DEFAULT) = syncGlobalTierList(context, region, force = true)
}
