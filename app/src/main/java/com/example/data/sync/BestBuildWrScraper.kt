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
    private const val ROLE_STATS_SNAPSHOT_PREFIX = "role_stats_snapshot_"
    // v2 adds the category published for each champion/lane. Ignore the old
    // aggregate cache so the China selector cannot keep showing stale roles.
    private const val CHINA_ELO_GROUPS_KEY = "china_elo_groups_v2"
    private const val CHINA_ELO_GROUPS_LEGACY_KEY = "china_elo_groups_v1"
    private const val CHINA_CACHE_SCHEMA_KEY = "china_cache_schema"
    private const val SOURCE_SET_KEY = "active_source_set_v2"

    // Only publishers used by the statistics pipeline belong in live monitoring.
    val GLOBAL_SCRAPING_SOURCES = listOf(
        GlobalScrapingSource("wildriftfire", "WildRiftFire", "https://www.wildriftfire.com/tier-list", "wildriftfire.com/tier-list", com.example.data.GamePatch.VERSION),
        GlobalScrapingSource("wildriftcore", "WildRiftCore", "https://wildriftcore.com/en/tierlist/", "wildriftcore.com/en/tierlist/", com.example.data.GamePatch.VERSION)
    )
    /** The mainland-China feed is intentionally a single-source region. */
    val CHINA_SCRAPING_SOURCES = listOf(
        GlobalScrapingSource(
            "china_lolm", "Tencent China", "https://lolm.qq.com/act/a20220818raider/index.html",
            "lolm.qq.com/act/a20220818raider/index.html", region = MetaRegion.CHINA
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

    private val initialSourceMap = (GLOBAL_SCRAPING_SOURCES + CHINA_SCRAPING_SOURCES).associate { source ->
        "${source.region.lowercase(Locale.ROOT)}_${source.id}" to ScraperSourceStatus(
            name = "${source.name} (${source.displayUrl})", url = source.url,
            region = source.region, isHealthy = false, lastChecked = 0L, responseTimeMs = 0L,
            errorMessage = "Pendiente de comprobación"
        )
    }

    private val _sourceStatuses = MutableStateFlow<Map<String, ScraperSourceStatus>>(initialSourceMap)
    val sourceStatuses = _sourceStatuses.asStateFlow()
    private val _globalSyncStatus = MutableStateFlow("Sin sincronización")
    val globalSyncStatus = _globalSyncStatus.asStateFlow()
    private val _chinaEloGroups = MutableStateFlow<Map<String, ChinaLolmParser.EloSnapshot>>(emptyMap())
    val chinaEloGroups = _chinaEloGroups.asStateFlow()
    private var selectedChinaElo = MetaRegion.DEFAULT_CHINA_ELO

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
        // Do not carry a status for a publisher removed from a previous
        // installation into the current regional monitor.
        _sourceStatuses.value = initialSourceMap
        PatchTierMovementStore.initialize(context)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        selectedChinaElo = MetaRegion.normalizeChinaElo(
            prefs.getString("selected_china_elo", MetaRegion.DEFAULT_CHINA_ELO)
        )
        if (prefs.getString("patch", null) != com.example.data.GamePatch.VERSION) {
            prefs.edit().clear().putString("patch", com.example.data.GamePatch.VERSION).apply()
        }
        if (prefs.getString(CHINA_CACHE_SCHEMA_KEY, null) != "2") {
            prefs.edit()
                .remove("snapshot_${MetaRegion.CHINA}")
                .remove(STATS_SNAPSHOT_PREFIX + MetaRegion.CHINA)
                .remove(ROLE_STATS_SNAPSHOT_PREFIX + MetaRegion.CHINA)
                .remove("timestamp_${MetaRegion.CHINA}")
                .remove(CHINA_ELO_GROUPS_LEGACY_KEY)
                .putString(CHINA_CACHE_SCHEMA_KEY, "2")
                .apply()
        }
        val activeSourceSet = (GLOBAL_SCRAPING_SOURCES + CHINA_SCRAPING_SOURCES)
            .joinToString(",") { "${it.region}:${it.id}" }
        if (prefs.getString(SOURCE_SET_KEY, null) != activeSourceSet) {
            // A previous build could have persisted rates fused from a
            // different publisher set. Discard the global snapshot so a
            // retired source cannot reappear until its active pages are checked.
            prefs.edit()
                .remove("snapshot_${MetaRegion.DEFAULT}")
                .remove(STATS_SNAPSHOT_PREFIX + MetaRegion.DEFAULT)
                .remove(ROLE_STATS_SNAPSHOT_PREFIX + MetaRegion.DEFAULT)
                .remove("timestamp_${MetaRegion.DEFAULT}")
                .remove("rate_history_v2")
                .putString(SOURCE_SET_KEY, activeSourceSet)
                .apply()
        }
        val cachedChinaGroups = decodeChinaEloGroups(prefs.getString(CHINA_ELO_GROUPS_KEY, null))
        if (cachedChinaGroups.isNotEmpty()) {
            _chinaEloGroups.value = cachedChinaGroups
            val checkedAt = prefs.getLong("timestamp_${MetaRegion.CHINA}", 0L)
            cachedChinaGroups.values.forEach { group ->
                WildRiftRepository.applyRegionalTierList(
                    MetaRegion.CHINA, group.tiers, group.stats, roleStats = group.roleStats,
                    checkedAt = checkedAt, snapshotKey = chinaEloSnapshotKey(group.id)
                )
            }
        }
        for (region in MetaRegion.available) {
            try {
                val saved = prefs.getString("snapshot_$region", null) ?: continue
                val data = JSONObject(saved)
                val tiers = data.keys().asSequence().associateWith { data.getString(it) }
                // Drop legacy partial rows before they reach the first frame;
                // they were the reason a champion could render two rates and a
                // dash until the next network sync.
                val stats = decodeStats(prefs.getString(STATS_SNAPSHOT_PREFIX + region, null))
                    .filterValues { it.isComplete() }
                val roleStats = decodeRoleStats(prefs.getString(ROLE_STATS_SNAPSHOT_PREFIX + region, null))
                val at = prefs.getLong("timestamp_$region", 0)
                WildRiftRepository.applyRegionalTierList(region, tiers, stats, roleStats = roleStats,
                    checkedAt = at, history = decodeHistory(prefs.getString("rate_history_v2", null)),
                    patchMovements = PatchTierMovementStore.snapshot())
                _freshness.value = MetaFreshness(
                    tiersCheckedAt = prefs.getLong("tiers_checked", 0),
                    ratesCheckedAt = prefs.getLong("rates_checked", 0),
                    lastChangedAt = prefs.getLong("last_changed", 0),
                    unchangedChecks = prefs.getInt("unchanged_checks", 0),
                    tierCount = tiers.size, statsCount = stats.size,
                    updatedChampionCount = stats.size,
                    expectedCount = WildRiftRepository.baseChampionsList.size)
                regions[region] = RegionState(prefs.getLong("timestamp_$region", 0), false, "Datos guardados • Pendiente de actualización")
            } catch (_: Exception) { /* Leave other region caches intact. */ }
        }
        selectRegion(selectedRegion)
        WildRiftRepository.selectChinaEloGroup(selectedChinaElo)
    }

    @Synchronized
    fun selectChinaElo(groupId: String) {
        selectedChinaElo = MetaRegion.normalizeChinaElo(groupId)
        WildRiftRepository.selectChinaEloGroup(selectedChinaElo)
    }

    private fun chinaEloSnapshotKey(groupId: String): String =
        "${MetaRegion.CHINA}:$groupId"
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
            var roleStatsCache: JSONObject? = null

            if (!_isOnline.value) {
                message = "Sin conexión; conservando datos guardados."
                _sourceStatuses.value = _sourceStatuses.value.mapValues { (_, status) ->
                    status.copy(isHealthy = false, errorMessage = message)
                }
            } else {
                val activeSources = if (requested == MetaRegion.CHINA) CHINA_SCRAPING_SOURCES else GLOBAL_SCRAPING_SOURCES
                val activeKeys = activeSources.map { "${it.region.lowercase(Locale.ROOT)}_${it.id}" }.toSet()
                val knownIds = WildRiftRepository.baseChampionsList.flatMap {
                    listOf(it.id, it.name, it.ddragonId).filter(String::isNotBlank)
                }.toSet()
                val results = coroutineScope {
                    activeSources.map { source -> async {
                        sourceClient.fetch(source, knownIds).also { result ->
                            synchronized(this@BestBuildWrScraper) {
                                val key = "${source.region.lowercase(Locale.ROOT)}_${source.id}"
                                val old = _sourceStatuses.value[key]
                                _sourceStatuses.value = _sourceStatuses.value + (key to result.status.copy(
                                    lastSuccessAt = old?.lastSuccessAt ?: 0,
                                    lastChangedAt = old?.lastChangedAt ?: 0,
                                    lastRatesChangedAt = old?.lastRatesChangedAt ?: 0,
                                    unchangedChecks = old?.unchangedChecks ?: 0))
                            }
                        }
                    } }.awaitAll()
                }
                // China is a separate live region and must never be fused with
                // Global publishers. Apply the single Tencent snapshot as-is,
                // preserving an older valid snapshot when this poll fails.
                if (requested == MetaRegion.CHINA) {
                    val result = results.singleOrNull()
                    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    if (result != null) {
                        val key = "${result.source.region.lowercase(Locale.ROOT)}_${result.source.id}"
                        val oldStatus = _sourceStatuses.value[key]
                        val fingerprint = (result.tiers.toSortedMap().toString() + result.stats.toSortedMap().toString() + result.chinaEloGroups.toSortedMap().toString())
                            .let { value -> java.security.MessageDigest.getInstance("SHA-256")
                                .digest(value.toByteArray()).joinToString("") { byte -> "%02x".format(byte) } }
                        val ratesFingerprint = (result.stats.toSortedMap().toString() + result.roleStats.toSortedMap().toString() +
                            result.chinaEloGroups.toSortedMap().toString()).let { value ->
                            java.security.MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
                                .joinToString("") { byte -> "%02x".format(byte) }
                        }
                        val changed = fingerprint != prefs.getString("hash_$key", null)
                        val ratesChanged = ratesFingerprint != prefs.getString("rates_hash_$key", null)
                        val changedAt = if (result.status.isHealthy && changed) started
                            else oldStatus?.lastChangedAt ?: prefs.getLong("changed_$key", 0)
                        val ratesChangedAt = if (result.status.isHealthy && ratesChanged) started
                            else oldStatus?.lastRatesChangedAt ?: prefs.getLong("rates_changed_$key", 0)
                        _sourceStatuses.value = _sourceStatuses.value + (key to result.status.copy(
                            lastSuccessAt = if (result.status.isHealthy) started else oldStatus?.lastSuccessAt ?: 0L,
                            lastChangedAt = changedAt, lastRatesChangedAt = ratesChangedAt,
                            unchangedChecks = if (changed) 0 else (oldStatus?.unchangedChecks ?: 0) + 1
                        ))
                        if (result.status.isHealthy) prefs.edit()
                            .putString("hash_$key", fingerprint)
                            .putString("rates_hash_$key", ratesFingerprint)
                            .putLong("changed_$key", changedAt)
                            .putLong("rates_changed_$key", ratesChangedAt)
                            .putLong("success_$key", started)
                            .apply()
                    }
                    val previousSnapshot = prefs.getString("snapshot_$requested", null)
                    val previousTiers = runCatching {
                        val json = JSONObject(previousSnapshot.orEmpty())
                        json.keys().asSequence().associateWith { json.getString(it) }
                    }.getOrDefault(emptyMap())
                    val previousStats = decodeStats(prefs.getString(STATS_SNAPSHOT_PREFIX + requested, null))
                    val previousRoleStats = decodeRoleStats(prefs.getString(ROLE_STATS_SNAPSHOT_PREFIX + requested, null))
                    val completeStats = result?.stats.orEmpty().filterValues { it.isComplete() }
                    val successNow = result?.status?.isHealthy == true &&
                        (result.tiers.isNotEmpty() || completeStats.isNotEmpty() || result.chinaEloGroups.isNotEmpty())
                    if (successNow) {
                        val checked = started
                        val groups = result!!.chinaEloGroups
                        if (groups.isNotEmpty()) {
                            _chinaEloGroups.value = groups
                            groups.values.forEach { group ->
                                WildRiftRepository.applyRegionalTierList(
                                    MetaRegion.CHINA, group.tiers,
                                    group.stats.filterValues { it.isComplete() },
                                    roleStats = group.roleStats,
                                    checkedAt = checked,
                                    snapshotKey = chinaEloSnapshotKey(group.id)
                                )
                            }
                        }
                        WildRiftRepository.applyRegionalTierList(
                            MetaRegion.CHINA, result!!.tiers, completeStats,
                            roleStats = result.roleStats,
                            previousStats = previousStats, previousRoleStats = previousRoleStats,
                            previousTiers = previousTiers, checkedAt = checked
                        )
                        _freshness.value = _freshness.value.copy(
                            tiersCheckedAt = if (result.tiers.isNotEmpty()) checked else _freshness.value.tiersCheckedAt,
                            ratesCheckedAt = if (completeStats.isNotEmpty()) checked else _freshness.value.ratesCheckedAt,
                            tierCount = result.tiers.size, statsCount = completeStats.size,
                            updatedChampionCount = completeStats.size,
                            changedChampions = (previousStats.keys + completeStats.keys)
                                .count { previousStats[it] != completeStats[it] }
                        )
                        prefs.edit().putLong("timestamp_$requested", checked)
                            .putString("snapshot_$requested", JSONObject(result.tiers).toString())
                            .putString(STATS_SNAPSHOT_PREFIX + requested, encodeStats(completeStats).toString())
                            .putString(ROLE_STATS_SNAPSHOT_PREFIX + requested, encodeRoleStats(result.roleStats).toString())
                            .putString(CHINA_ELO_GROUPS_KEY, encodeChinaEloGroups(result.chinaEloGroups).toString())
                            .apply()
                        selectChinaElo(selectedChinaElo)
                        regions[requested] = RegionState(checked, true,
                            "China actualizada (${completeStats.size}/${WildRiftRepository.baseChampionsList.size} estadísticas completas)")
                    } else {
                        regions[requested] = RegionState(regions[requested]?.at ?: 0L, false,
                            result?.status?.errorMessage ?: "Sin datos nuevos; conservando China guardada.")
                    }
                    if (selectedRegion == requested) selectRegion(requested)
                    return@withContext
                }
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                _sourceStatuses.value = _sourceStatuses.value.filterKeys { it !in activeKeys } + results.associate { result ->
                    val key = "${result.source.region.lowercase(Locale.ROOT)}_${result.source.id}"
                    val old = _sourceStatuses.value[key]
                    val fingerprint = (result.tiers.toSortedMap().toString() + result.stats.toSortedMap().toString() +
                        result.roleStats.toSortedMap().toString())
                        .let { bytes -> java.security.MessageDigest.getInstance("SHA-256")
                            .digest(bytes.toByteArray()).joinToString("") { "%02x".format(it) } }
                    val changed = fingerprint != prefs.getString("hash_$key", null)
                    val ratesFingerprint = (result.stats.toSortedMap().toString() + result.roleStats.toSortedMap().toString()).let { bytes ->
                        java.security.MessageDigest.getInstance("SHA-256").digest(bytes.toByteArray())
                            .joinToString("") { "%02x".format(it) }
                    }
                    val previousRatesChange = old?.lastRatesChangedAt?.takeIf { it > 0 }
                        ?: prefs.getLong("rates_changed_$key", 0)
                    val ratesChangedAt = if ((result.stats.isNotEmpty() || result.roleStats.isNotEmpty()) && (previousRatesChange == 0L ||
                        ratesFingerprint != prefs.getString("rates_hash_$key", null))) started else previousRatesChange
                    val priorChanged = old?.lastChangedAt?.takeIf { it > 0 }
                        ?: prefs.getLong("changed_$key", 0)
                    val status = result.status.copy(
                        lastSuccessAt = if (result.status.isHealthy) started else old?.lastSuccessAt ?: prefs.getLong("success_$key", 0),
                        lastChangedAt = if (result.status.isHealthy && changed) started else priorChanged,
                        lastRatesChangedAt = ratesChangedAt,
                        unchangedChecks = if (changed) 0 else (old?.unchangedChecks ?: 0) + 1)
                    if (result.status.isHealthy) {
                        val editor = prefs.edit().putString("hash_$key", fingerprint)
                            .putLong("changed_$key", status.lastChangedAt).putLong("success_$key", started)
                        if (result.stats.isNotEmpty() || result.roleStats.isNotEmpty()) editor.putString("rates_hash_$key", ratesFingerprint)
                            .putLong("rates_changed_$key", ratesChangedAt)
                        editor.apply()
                    }
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
                val previousRoleStats = decodeRoleStats(
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .getString(ROLE_STATS_SNAPSHOT_PREFIX + requested, null)
                )
                // Keep the same rank bracket and publisher for current and dated history.
                // Fallback publishers only fill champions absent from the primary report.
                val freshStats = linkedMapOf<String, ChampionMetaStats>()
                val statOrigins = mutableMapOf<String, String>()
                // Only the two active publishers contribute to the global
                // snapshot. A source removed from monitoring must not remain
                // as a hidden statistical fallback.
                listOf("wildriftfire", "wildriftcore").forEach { id ->
                    validSources.firstOrNull { it.source.id == id }?.stats?.forEach { (champion, rates) ->
                        val existing = freshStats[champion]
                        if (existing == null) {
                            statOrigins[champion] = id
                        }
                        freshStats[champion] = if (existing == null) rates else mergePublishedStats(existing, rates)
                    }
                }
                // A partial pair (for example Core's explanatory text with no
                // ban rate) must never be presented as a current published
                // snapshot. It remains visible in source diagnostics only.
                val completeFreshStats = freshStats.filterValues { it.isComplete() }
                val completeOrigins = statOrigins.filterKeys { it in completeFreshStats }
                val freshRoleStats = GlobalTierConsensus.fuseRoleStats(
                    validSources.sortedBy { it.source.id == "wildriftfire" }.map { it.roleStats }
                )

                // Categories and rates are independent publications. A source
                // can expose current win/pick/ban values while its tier markup
                // is temporarily unavailable (and vice versa). Do not discard
                // a valid rate update just because the category parser returned
                // an empty map.
                if (fusedTiers.isNotEmpty() || completeFreshStats.isNotEmpty()) {
                    val merged = GlobalTierConsensus.mergeSnapshot(previousTiers, fusedTiers)
                    // A successful poll publishes only what was actually
                    // observed now. Cached rates remain available only when a
                    // complete poll fails; they must not be re-dated as fresh.
                    val mergedStats = completeFreshStats.toMap()
                    val mergedRoleStats = freshRoleStats
                    val history = decodeHistory(prefs.getString("rate_history_v2", null)).toMutableMap()
                    val publishedHistory = validSources.firstOrNull { it.source.id == "wildriftfire" }?.history.orEmpty()
                    publishedHistory.forEach { (id, rows) ->
                        if (statOrigins[id] != "wildriftfire") return@forEach
                        val ownRows = history[id].orEmpty().filter { !it.daily && it.source == "wildriftfire" }
                        history[id] = (ownRows + rows).distinctBy { Pair(it.at, it.daily) }.sortedBy { it.at }
                    }
                    completeFreshStats.forEach { (id, rates) -> rates.winRate?.let { value ->
                        val origin = completeOrigins[id].orEmpty()
                        history[id] = MetaRateHistory.record(history[id].orEmpty().filter { it.source == origin },
                            ObservedRate(started, value, rates.pickRate, rates.banRate, source = origin))
                    } }
                    // Categories still use their partial-snapshot merge, so a
                    // temporarily absent category is not a repeated change.
                    val changedTierIds = fusedTiers.keys
                        .filter { previousTiers[it] != fusedTiers[it] }
                    val changedStatIds = (previousStats.keys + completeFreshStats.keys)
                        .filter { previousStats[it] != completeFreshStats[it] }
                    val changedRoleIds = (previousRoleStats.keys + freshRoleStats.keys)
                        .filter { previousRoleStats[it] != freshRoleStats[it] }
                    val changedIds = (changedTierIds + changedStatIds + changedRoleIds).toSet().size
                    val oldHealth = _freshness.value
                    _freshness.value = oldHealth.copy(
                        tiersCheckedAt = if (fusedTiers.isNotEmpty()) started else oldHealth.tiersCheckedAt,
                        ratesCheckedAt = if (completeFreshStats.isNotEmpty()) started else oldHealth.ratesCheckedAt,
                        lastChangedAt = if (changedIds > 0 || oldHealth.lastChangedAt == 0L) started else oldHealth.lastChangedAt,
                        unchangedChecks = if (changedIds > 0) 0 else oldHealth.unchangedChecks + 1,
                        tierCount = fusedTiers.size, statsCount = completeFreshStats.size,
                        updatedChampionCount = completeFreshStats.size,
                        changedChampions = changedIds)
                    prefs.edit().putString("rate_history_v2", encodeHistory(history).toString())
                        .putLong("tiers_checked", _freshness.value.tiersCheckedAt)
                        .putLong("rates_checked", _freshness.value.ratesCheckedAt)
                        .putLong("last_changed", _freshness.value.lastChangedAt)
                        .putInt("unchanged_checks", _freshness.value.unchangedChecks).apply()
                    PatchTierMovementStore.update(previousTiers, merged)
                    WildRiftRepository.applyRegionalTierList(
                        "GLOBAL",
                        merged,
                        mergedStats,
                        roleStats = mergedRoleStats,
                        previousRoleStats = previousRoleStats,
                        previousStats = previousStats, previousTiers = previousTiers,
                        checkedAt = started, history = history, patchMovements = PatchTierMovementStore.snapshot()
                    )
                    cache = JSONObject(merged)
                    statsCache = encodeStats(mergedStats)
                    roleStatsCache = encodeRoleStats(mergedRoleStats)
                    success = true
                }
                message = if (success) "Categorías y estadísticas completas actualizadas (${validSources.size}/${GLOBAL_SCRAPING_SOURCES.size} fuentes)"
                    else "Sin actualizar; conservando datos."

            }

            if (success) {

                if (cache != null) {
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                        .putLong("timestamp_$requested", started)
                        .putString("snapshot_$requested", cache.toString())
                        .also { editor -> statsCache?.let { editor.putString(STATS_SNAPSHOT_PREFIX + requested, it.toString()) } }
                        .also { editor -> roleStatsCache?.let { editor.putString(ROLE_STATS_SNAPSHOT_PREFIX + requested, it.toString()) } }
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
                ObservedRate(row.getLong(0), row.getDouble(1), row.optDouble(2, Double.NaN).takeIf(Double::isFinite),
                    row.optDouble(3, Double.NaN).takeIf(Double::isFinite), row.optBoolean(4, false), row.optString(5)) }
        }
    }.getOrDefault(emptyMap())

    private fun encodeHistory(history: Map<String, List<ObservedRate>>): JSONObject = JSONObject().apply {
        history.forEach { (id, samples) -> put(id, JSONArray().apply {
            samples.forEach { put(JSONArray().put(it.at).put(it.value).put(it.pick ?: JSONObject.NULL).put(it.ban ?: JSONObject.NULL).put(it.daily).put(it.source)) }
        }) }
    }

    private fun mergePublishedStats(current: ChampionMetaStats, fallback: ChampionMetaStats): ChampionMetaStats =
        ChampionMetaStats(
            winRate = current.winRate ?: fallback.winRate,
            pickRate = current.pickRate ?: fallback.pickRate,
            banRate = current.banRate ?: fallback.banRate,
            tier = current.tier ?: fallback.tier
        )

    private fun decodeStats(raw: String?): Map<String, ChampionMetaStats> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            val root = JSONObject(raw)
            root.keys().asSequence().mapNotNull { id ->
                val value = root.optJSONObject(id) ?: return@mapNotNull null
                val stats = ChampionMetaStats(
                    winRate = value.optDouble("winRate", Double.NaN).takeUnless { it.isNaN() },
                    pickRate = value.optDouble("pickRate", Double.NaN).takeUnless { it.isNaN() },
                    banRate = value.optDouble("banRate", Double.NaN).takeUnless { it.isNaN() },
                    tier = value.optString("tier").takeIf { it.isNotBlank() }
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
                value.tier?.let { put("tier", it) }
            })
        }
    }

    private fun decodeRoleStats(raw: String?): Map<String, Map<com.example.model.LaneRole, ChampionMetaStats>> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            val root = JSONObject(raw)
            root.keys().asSequence().associateWith { id ->
                val lanes = root.getJSONObject(id)
                lanes.keys().asSequence().mapNotNull { name ->
                    val lane = runCatching { com.example.model.LaneRole.valueOf(name) }.getOrNull() ?: return@mapNotNull null
                    val value = lanes.getJSONObject(name)
                    lane to ChampionMetaStats(
                        value.optDouble("winRate", Double.NaN).takeIf(Double::isFinite),
                        value.optDouble("pickRate", Double.NaN).takeIf(Double::isFinite),
                        value.optDouble("banRate", Double.NaN).takeIf(Double::isFinite),
                        value.optString("tier").takeIf { it.isNotBlank() }
                    )
                }.toMap().filterValues { it.isComplete() }
            }.filterValues { it.isNotEmpty() }
        }.getOrDefault(emptyMap())
    }

    private fun encodeRoleStats(stats: Map<String, Map<com.example.model.LaneRole, ChampionMetaStats>>): JSONObject = JSONObject().apply {
        stats.forEach { (id, lanes) -> put(id, JSONObject().apply {
            lanes.forEach { (lane, value) -> put(lane.name, JSONObject().apply {
                value.winRate?.let { put("winRate", it) }
                value.pickRate?.let { put("pickRate", it) }
                value.banRate?.let { put("banRate", it) }
                value.tier?.let { put("tier", it) }
            }) }
        }) }
    }

    private fun encodeChinaEloGroups(groups: Map<String, ChinaLolmParser.EloSnapshot>): JSONObject = JSONObject().apply {
        groups.forEach { (id, group) ->
            put(id, JSONObject().apply {
                put("label", group.label)
                put("publishedDate", group.publishedDate)
                put("tiers", JSONObject(group.tiers))
                put("stats", encodeStats(group.stats))
                put("roleStats", encodeRoleStats(group.roleStats))
            })
        }
    }

    private fun decodeChinaEloGroups(raw: String?): Map<String, ChinaLolmParser.EloSnapshot> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            val root = JSONObject(raw)
            root.keys().asSequence().mapNotNull { id ->
                val value = root.optJSONObject(id) ?: return@mapNotNull null
                val tiersJson = value.optJSONObject("tiers")
                val tiers = tiersJson?.keys()?.asSequence()?.associateWith { tiersJson.getString(it) }.orEmpty()
                ChinaLolmParser.EloSnapshot(
                    id = id,
                    label = value.optString("label", MetaRegion.chinaEloLabel(id)),
                    tiers = tiers,
                    stats = decodeStats(value.optJSONObject("stats")?.toString()),
                    roleStats = decodeRoleStats(value.optJSONObject("roleStats")?.toString()),
                    publishedDate = value.optString("publishedDate")
                ).let { id to it }
            }.toMap()
        }.getOrDefault(emptyMap())
    }
}
