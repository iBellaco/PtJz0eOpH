package com.example.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.data.WildRiftRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    private val sourceClient = GlobalTierSourceClient()

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

    private val initialSourceMap = GLOBAL_SCRAPING_SOURCES.associate { source ->
        "global_${source.id}" to ScraperSourceStatus(
            name = "${source.name} (${source.displayUrl})", url = source.url,
            isHealthy = false, lastChecked = 0L, responseTimeMs = 0L,
            errorMessage = "Pendiente de comprobación"
        )
    }

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
        for (region in MetaRegion.available) {
            try {
                val saved = prefs.getString("snapshot_$region", null) ?: continue
                val data = JSONObject(saved)
                val tiers = data.keys().asSequence().associateWith { data.getString(it) }
                WildRiftRepository.applyRegionalTierList(region, tiers)
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
                _sourceStatuses.value = _sourceStatuses.value.mapValues { (_, status) ->
                    status.copy(isHealthy = false, errorMessage = message)
                }
            } else {
                val knownIds = WildRiftRepository.baseChampionsList.flatMap {
                    listOf(it.id, it.name, it.ddragonId).filter(String::isNotBlank)
                }.toSet()
                val results = coroutineScope {
                    GLOBAL_SCRAPING_SOURCES.map { source -> async { sourceClient.fetch(source, knownIds) } }.awaitAll()
                }
                _sourceStatuses.value = results.associate { "global_${it.source.id}" to it.status }
                val validSources = results.filter { it.status.isHealthy }
                val fusedTiers = GlobalTierConsensus.fuse(validSources.map { it.tiers })
                if (fusedTiers.isNotEmpty()) {
                    val saved = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .getString("snapshot_$requested", null)
                    val previousTiers = runCatching {
                        val json = JSONObject(saved.orEmpty())
                        json.keys().asSequence().associateWith { json.getString(it) }
                    }.getOrDefault(emptyMap())
                    val merged = GlobalTierConsensus.mergeSnapshot(previousTiers, fusedTiers)
                    WildRiftRepository.applyRegionalTierList("GLOBAL", merged)
                    cache = JSONObject(merged)
                    success = true
                }
                message = if (success) "Categorías globales actualizadas (${validSources.size}/3 fuentes)"
                    else "Sin actualizar; conservando datos."

            }

            if (success) {

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

    suspend fun syncAllChampionBuilds(context: Context, region: String = MetaRegion.DEFAULT) = syncGlobalTierList(context, region, force = true)
}
