package com.example.data.sync

import android.content.Context
import com.example.data.WildRiftRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object GlobalMetaSyncService {
    private val _syncState = MutableStateFlow<MetaSyncState>(MetaSyncState.Idle)
    val syncState: StateFlow<MetaSyncState> = _syncState.asStateFlow()

    private val _currentRegion = MutableStateFlow(MetaRegion.DEFAULT)
    val currentRegion: StateFlow<String> = _currentRegion.asStateFlow()

    fun loadRegion(context: Context) {
        BestBuildWrScraper.initialize(context)
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val saved = prefs.getString("selected_meta_region", MetaRegion.DEFAULT) ?: MetaRegion.DEFAULT
        _currentRegion.value = MetaRegion.normalize(saved)
        if (saved != MetaRegion.DEFAULT) prefs.edit().putString("selected_meta_region", MetaRegion.DEFAULT).apply()
        WildRiftRepository.selectMetaRegion(_currentRegion.value)
        BestBuildWrScraper.selectRegion(_currentRegion.value)
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

    suspend fun syncGlobalMeta(context: Context, forceRefresh: Boolean = false) {
        _syncState.value = MetaSyncState.Syncing
        val requestedRegion = _currentRegion.value
        BestBuildWrScraper.syncGlobalTierList(context, requestedRegion, force = forceRefresh)
        if (requestedRegion != _currentRegion.value) return
        _syncState.value = if (BestBuildWrScraper.isLastSyncSuccess.value)
            MetaSyncState.Success(BestBuildWrScraper.lastSyncFormattedTime.value)
        else MetaSyncState.Error("No se pudo actualizar; usando los últimos datos guardados.")
    }

    fun getLastSyncInfo(context: Context): Pair<String, String> {
        val isOnline = BestBuildWrScraper.isOnline.value
        val time = BestBuildWrScraper.lastSyncFormattedTime.value
        return Pair(if (isOnline) "En vivo" else "Sin conexión", time)
    }
}
