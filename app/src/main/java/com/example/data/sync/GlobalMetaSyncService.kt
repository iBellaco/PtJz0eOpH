package com.example.data.sync

import android.content.Context
import com.example.data.WildRiftRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object GlobalMetaSyncService {
    private val liveScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val liveOwners = mutableSetOf<String>()
    private var liveJob: Job? = null
    private val syncMutex = kotlinx.coroutines.sync.Mutex()

    @Synchronized private fun pollInterval(): Long =
        if (liveOwners.isNotEmpty() && liveOwners.all { it == "overlay" }) 5 * 60_000L
        else MetaFreshness.POLL_INTERVAL_MS

    /** One shared poller for the activity and floating hub. Stops when both close. */
    @Synchronized fun startLive(context: Context, owner: String) {
        liveOwners.add(owner)
        if (liveJob?.isActive == true) return
        val app = context.applicationContext
        com.example.util.AppUpdateManager.restore(app)
        liveJob = liveScope.launch {
            while (isActive) {
                val cycleStarted = android.os.SystemClock.elapsedRealtime()
                // Version verification is independent of source latency/failure.
                com.example.util.AppUpdateManager.checkForUpdates(app)
                syncGlobalMeta(app, forceRefresh = true)
                delay((pollInterval() - (android.os.SystemClock.elapsedRealtime() - cycleStarted)).coerceAtLeast(1_000L))
            }
        }
    }

    @Synchronized fun stopLive(owner: String) {
        liveOwners.remove(owner)
        if (liveOwners.isEmpty()) { liveJob?.cancel(); liveJob = null }
    }

    private val _syncState = MutableStateFlow<MetaSyncState>(MetaSyncState.Idle)
    val syncState: StateFlow<MetaSyncState> = _syncState.asStateFlow()

    private val _currentRegion = MutableStateFlow(MetaRegion.DEFAULT)
    val currentRegion: StateFlow<String> = _currentRegion.asStateFlow()
    private val _currentChinaElo = MutableStateFlow(MetaRegion.DEFAULT_CHINA_ELO)
    val currentChinaElo: StateFlow<String> = _currentChinaElo.asStateFlow()

    fun loadRegion(context: Context) {
        BestBuildWrScraper.initialize(context)
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val saved = prefs.getString("selected_meta_region", MetaRegion.DEFAULT) ?: MetaRegion.DEFAULT
        val savedChinaElo = MetaRegion.normalizeChinaElo(
            prefs.getString("selected_china_elo", MetaRegion.DEFAULT_CHINA_ELO)
        )
        _currentRegion.value = MetaRegion.normalize(saved)
        _currentChinaElo.value = savedChinaElo
        if (saved != _currentRegion.value) prefs.edit().putString("selected_meta_region", _currentRegion.value).apply()
        WildRiftRepository.selectMetaRegion(_currentRegion.value)
        BestBuildWrScraper.selectRegion(_currentRegion.value)
        BestBuildWrScraper.selectChinaElo(savedChinaElo)
    }

    fun setRegion(context: Context, regionId: String, scope: CoroutineScope) {
        val region = MetaRegion.normalize(regionId)
        _currentRegion.value = region
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit()
            .putString("selected_meta_region", region).apply()
        WildRiftRepository.selectMetaRegion(region)
        BestBuildWrScraper.selectRegion(region)
        scope.launch { syncGlobalMeta(context, forceRefresh = true) }
    }

    fun setChinaElo(context: Context, groupId: String) {
        val normalized = MetaRegion.normalizeChinaElo(groupId)
        _currentChinaElo.value = normalized
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit()
            .putString("selected_china_elo", normalized).apply()
        BestBuildWrScraper.selectChinaElo(normalized)
        WildRiftRepository.selectChinaEloGroup(normalized)
    }

    suspend fun syncGlobalMeta(context: Context, forceRefresh: Boolean = false) {
        if (!syncMutex.tryLock()) return
        try {
        _syncState.value = MetaSyncState.Syncing
        val requestedRegion = _currentRegion.value
        // The tier-list selector only controls the dataset shown to the user;
        // administrative monitoring must keep every configured server fresh.
        // Run the selected region first so its status is available as soon as
        // the cycle returns, then refresh the remaining regions independently.
        val regionsToCheck = buildList {
            add(requestedRegion)
            MetaRegion.available.filterNot { it == requestedRegion }.forEach(::add)
        }
        regionsToCheck.forEach { region ->
            BestBuildWrScraper.syncGlobalTierList(context, region, force = forceRefresh)
        }
        if (requestedRegion != _currentRegion.value) return
        _syncState.value = if (BestBuildWrScraper.isLastSyncSuccess.value)
            MetaSyncState.Success(BestBuildWrScraper.lastSyncFormattedTime.value)
        else MetaSyncState.Error("No se pudo actualizar; usando los últimos datos guardados.")
        } finally { syncMutex.unlock() }
    }

    fun getLastSyncInfo(context: Context): Pair<String, String> {
        val isOnline = !BestBuildWrScraper.freshness.value.stale(System.currentTimeMillis())
        val time = BestBuildWrScraper.lastSyncFormattedTime.value
        return Pair(if (isOnline) "En vivo" else "Sin conexión", time)
    }
}
