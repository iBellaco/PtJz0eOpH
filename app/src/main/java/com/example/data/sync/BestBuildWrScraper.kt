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
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BestBuildWrScraper {
    private const val PREFS_NAME = "wr_tier_list_cache"
    private const val STATS_SNAPSHOT_PREFIX = "stats_snapshot_"

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

    private val _freshness = MutableStateFlow(MetaFreshness())
    val freshness = _freshness.asStateFlow()

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
                val stats = decodeStats(prefs.getString(STATS_SNAPSHOT_PREFIX + region, null))
                val at = prefs.getLong("timestamp_$region", 0)
                WildRiftRepository.applyRegionalTierList(region, tiers, stats,
                    checkedAt = at, history = decodeHistory(prefs.getString("rate_history", null)))
                _freshness.value = MetaFreshness(
                    tiersCheckedAt = prefs.getLong("tiers_checked", 0),
                    ratesCheckedAt = prefs.getLong("rates_checked", 0),
                    lastChangedAt = prefs.getLong("last_changed", 0),
                    unchangedChecks = prefs.getInt("unchanged_checks", 0),
                    tierCount = tiers.size, statsCount = stats.size,
                    expectedCount = WildRiftRepository.baseChampionsList.size)
                regions[region] = RegionState(prefs.getLong("timestamp_$region", 0), false, "Datos guardados • Pendiente de actualización")
            } catch (_: Exception) { /* Leave other region caches intact. */ }
        }
        selectRegion(selectedRegion)
    }
    fun startContinuousSync(context: Context) { GlobalMetaSyncService.startLive(context, "legacy") }

    suspend fun syncGlobalTierList(context: Context, region: String = MetaRegion.DEFAULT, force: Boolean = false) = withContext(Dispatchers.IO) {
        val requested = MetaRegion.normalize(region)
        syncMutex.lock()
        try {
            initialize(context)
            _isOnline.value = checkNetwork(context)
            val previous = regions[requested] ?: RegionState()
            if (!force && previous.success && System.currentTimeMillis() - previous.at < MetaFreshness.POLL_INTERVAL_MS) return@withContext
            if (selectedRegion == requested) _isSyncing.value = true
            val started = System.currentTimeMillis()
            _freshness.value = _freshness.value.copy(lastAttemptAt = started,
                expectedCount = WildRiftRepository.baseChampionsList.size)
            var success = false
            var message: String
            var cache: JSONObject? = null
            var statsCache: JSONObject? = null

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
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                _sourceStatuses.value = results.associate { result ->
                    val key = "global_${result.source.id}"
                    val old = _sourceStatuses.value[key]
                    val fingerprint = (result.tiers.toSortedMap().toString() + result.stats.toSortedMap().toString())
                        .let { bytes -> java.security.MessageDigest.getInstance("SHA-256")
                            .digest(bytes.toByteArray()).joinToString("") { "%02x".format(it) } }
                    val changed = fingerprint != prefs.getString("hash_$key", null)
                    val priorChanged = old?.lastChangedAt?.takeIf { it > 0 }
                        ?: prefs.getLong("changed_$key", 0)
                    val status = result.status.copy(
                        lastSuccessAt = if (result.status.isHealthy) started else old?.lastSuccessAt ?: prefs.getLong("success_$key", 0),
                        lastChangedAt = if (result.status.isHealthy && changed) started else priorChanged,
                        unchangedChecks = if (changed) 0 else (old?.unchangedChecks ?: 0) + 1)
                    if (result.status.isHealthy) prefs.edit().putString("hash_$key", fingerprint)
                        .putLong("changed_$key", status.lastChangedAt).putLong("success_$key", started).apply()
                    key to status
                }
                val validSources = results.filter { it.status.isHealthy }
                val fusedTiers = GlobalTierConsensus.fuse(validSources.map { it.tiers })
                val saved = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .getString("snapshot_$requested", null)
                val previousTiers = runCatching {
                    val json = JSONObject(saved.orEmpty())
                    json.keys().asSequence().associateWith { json.getString(it) }
                }.getOrDefault(emptyMap())
                val previousStats = decodeStats(
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .getString(STATS_SNAPSHOT_PREFIX + requested, null)
                )
                val freshStats = GlobalTierConsensus.fuseStats(validSources.map { it.stats })

                // Categories and rates are independent publications. A source
                // can expose current win/pick/ban values while its tier markup
                // is temporarily unavailable (and vice versa). Do not discard
                // a valid rate update just because the category parser returned
                // an empty map.
                if (fusedTiers.isNotEmpty() || freshStats.isNotEmpty()) {
                    val merged = GlobalTierConsensus.mergeSnapshot(previousTiers, fusedTiers)
                    val mergedStats = GlobalTierConsensus.mergeStats(previousStats, freshStats)
                    val history = decodeHistory(prefs.getString("rate_history", null)).toMutableMap()
                    freshStats.forEach { (id, rates) -> rates.winRate?.let { value ->
                        history[id] = MetaRateHistory.record(history[id].orEmpty(), ObservedRate(started, value))
                    } }
                    val changedIds = (fusedTiers.filter { (id, tier) -> previousTiers[id] != tier }.keys +
                        freshStats.filter { (id, rates) -> previousStats[id] != rates }.keys).size
                    val oldHealth = _freshness.value
                    _freshness.value = oldHealth.copy(
                        tiersCheckedAt = if (fusedTiers.isNotEmpty()) started else oldHealth.tiersCheckedAt,
                        ratesCheckedAt = if (freshStats.isNotEmpty()) started else oldHealth.ratesCheckedAt,
                        lastChangedAt = if (changedIds > 0 || oldHealth.lastChangedAt == 0L) started else oldHealth.lastChangedAt,
                        unchangedChecks = if (changedIds > 0) 0 else oldHealth.unchangedChecks + 1,
                        tierCount = fusedTiers.size, statsCount = freshStats.size, changedChampions = changedIds)
                    prefs.edit().putString("rate_history", encodeHistory(history).toString())
                        .putLong("tiers_checked", _freshness.value.tiersCheckedAt)
                        .putLong("rates_checked", _freshness.value.ratesCheckedAt)
                        .putLong("last_changed", _freshness.value.lastChangedAt)
                        .putInt("unchanged_checks", _freshness.value.unchangedChecks).apply()
                    WildRiftRepository.applyRegionalTierList(
                        "GLOBAL",
                        merged,
                        mergedStats,
                        previousStats = previousStats, checkedAt = started, history = history
                    )
                    cache = JSONObject(merged)
                    statsCache = encodeStats(mergedStats)
                    success = true
                }
                message = if (success) "Categorías y estadísticas globales actualizadas (${validSources.size}/3 fuentes)"
                    else "Sin actualizar; conservando datos."

            }

            if (success) {

                if (cache != null) {
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                        .putLong("timestamp_$requested", started)
                        .putString("snapshot_$requested", cache.toString())
                        .also { editor -> statsCache?.let { editor.putString(STATS_SNAPSHOT_PREFIX + requested, it.toString()) } }
                        .apply()
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

    private fun decodeHistory(raw: String?): Map<String, List<ObservedRate>> = runCatching {
        val root = JSONObject(raw ?: "{}")
        root.keys().asSequence().associateWith { id ->
            val rows = root.getJSONArray(id)
            (0 until rows.length()).map { i -> val row = rows.getJSONArray(i)
                ObservedRate(row.getLong(0), row.getDouble(1)) }
        }
    }.getOrDefault(emptyMap())

    private fun encodeHistory(history: Map<String, List<ObservedRate>>): JSONObject = JSONObject().apply {
        history.forEach { (id, samples) -> put(id, JSONArray().apply {
            samples.forEach { put(JSONArray().put(it.at).put(it.value)) }
        }) }
    }

    private fun decodeStats(raw: String?): Map<String, ChampionMetaStats> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            val root = JSONObject(raw)
            root.keys().asSequence().mapNotNull { id ->
                val value = root.optJSONObject(id) ?: return@mapNotNull null
                val stats = ChampionMetaStats(
                    winRate = value.optDouble("winRate", Double.NaN).takeUnless { it.isNaN() },
                    pickRate = value.optDouble("pickRate", Double.NaN).takeUnless { it.isNaN() },
                    banRate = value.optDouble("banRate", Double.NaN).takeUnless { it.isNaN() }
                )
                id to stats
            }.toMap()
        }.getOrDefault(emptyMap())
    }

    private fun encodeStats(stats: Map<String, ChampionMetaStats>): JSONObject = JSONObject().apply {
        stats.forEach { (id, value) ->
            put(id, JSONObject().apply {
                value.winRate?.let { put("winRate", it) }
                value.pickRate?.let { put("pickRate", it) }
                value.banRate?.let { put("banRate", it) }
            })
        }
    }
}
