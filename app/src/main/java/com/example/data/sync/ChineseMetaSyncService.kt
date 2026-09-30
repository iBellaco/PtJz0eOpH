package com.example.data.sync

import android.content.Context
import com.example.data.WildRiftRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.model.Champion
import com.example.model.LaneRole

object ChineseMetaSyncService {
    private val _syncState = MutableStateFlow<ChineseSyncState>(ChineseSyncState.Idle)
    val syncState: StateFlow<ChineseSyncState> = _syncState.asStateFlow()

    private val _currentTier = MutableStateFlow(TencentRankTier.DIAMOND_PLUS)
    val currentTier: StateFlow<TencentRankTier> = _currentTier.asStateFlow()

    private val _currentRegion = MutableStateFlow("GLOBAL")
    val currentRegion: StateFlow<String> = _currentRegion.asStateFlow()

    fun loadRegion(context: Context) {
        BestBuildWrScraper.initialize(context)
        val saved = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("selected_meta_region", "GLOBAL") ?: "GLOBAL"
        _currentRegion.value = MetaRegion.normalize(saved)
        WildRiftRepository.selectMetaRegion(_currentRegion.value)
    }

    fun setRegion(context: Context, regionId: String, scope: CoroutineScope) {
        val region = MetaRegion.normalize(regionId)
        _currentRegion.value = region
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit()
            .putString("selected_meta_region", region).apply()
        WildRiftRepository.selectMetaRegion(region)
        if (region == "CN") {
            scope.launch { syncChineseMeta(context, _currentTier.value, forceRefresh = true) }
        } else {
            _syncState.value = ChineseSyncState.Idle
        }
    }

    suspend fun syncChineseMeta(context: Context, tier: TencentRankTier = TencentRankTier.DIAMOND_PLUS, forceRefresh: Boolean = false) {
        _syncState.value = ChineseSyncState.Syncing
        _currentTier.value = tier
        BestBuildWrScraper.syncGlobalTierList(context, _currentRegion.value, force = forceRefresh)
        _syncState.value = if (BestBuildWrScraper.isLastSyncSuccess.value)
            ChineseSyncState.Success(BestBuildWrScraper.lastSyncFormattedTime.value, tier)
        else ChineseSyncState.Error("No se pudo actualizar; usando los últimos datos guardados.")
    }

    suspend fun getFilteredRankings(context: Context, tier: TencentRankTier, lane: LaneRole?): List<Champion> {
        return emptyList()
    }

    fun getLastSyncInfo(context: Context): Pair<String, String> {
        val isOnline = BestBuildWrScraper.isOnline.value
        val time = BestBuildWrScraper.lastSyncFormattedTime.value
        return Pair(if (isOnline) "En vivo" else "Sin conexión", time)
    }
}
