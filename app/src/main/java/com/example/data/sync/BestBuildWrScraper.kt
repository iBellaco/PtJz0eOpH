package com.example.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.data.WildRiftRepository
import com.example.model.Champion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ScraperSourceStatus(
    val name: String,
    val url: String,
    val isHealthy: Boolean,
    val lastChecked: Long,
    val responseTimeMs: Long,
    val errorMessage: String?,
    val region: String = "CN"
)

object BestBuildWrScraper {
    private const val PREFS_NAME = "wr_tier_list_cache"
    private const val KEY_LAST_TIMESTAMP = "last_stats_timestamp"
    private const val KEY_LAST_FORMATTED = "last_stats_formatted"
    private const val KEY_CACHED_CHAMPIONS = "cached_champions_json"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val _isOnline = MutableStateFlow<Boolean>(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _isSyncing = MutableStateFlow<Boolean>(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _isLastSyncSuccess = MutableStateFlow<Boolean>(false)
    val isLastSyncSuccess: StateFlow<Boolean> = _isLastSyncSuccess.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<Long>(0L)
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private val _lastSyncFormattedTime = MutableStateFlow<String>("Sin sincronización")
    val lastSyncFormattedTime: StateFlow<String> = _lastSyncFormattedTime.asStateFlow()

    private val _sourceStatuses = MutableStateFlow<Map<String, ScraperSourceStatus>>(
        mapOf("TencentSuperServer" to ScraperSourceStatus("Servidor chino", "https://lolm.qq.com/",
            false, 0L, 0L, "Pendiente de consulta", "CN"))
    )
    val sourceStatuses: StateFlow<Map<String, ScraperSourceStatus>> = _sourceStatuses.asStateFlow()

    private val _globalSyncStatus = MutableStateFlow<String>("Conectando con fuentes de estadísticas...")
    val globalSyncStatus: StateFlow<String> = _globalSyncStatus.asStateFlow()

    private var isInitialized = false

    fun checkNetwork(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val network = cm?.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            false
        }
    }

    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedTime = prefs.getLong(KEY_LAST_TIMESTAMP, 0L)
            val savedFormatted = prefs.getString(KEY_LAST_FORMATTED, null)

            if (savedTime > 0L && !savedFormatted.isNullOrBlank()) {
                _lastSyncTimestamp.value = savedTime
                _lastSyncFormattedTime.value = savedFormatted
            }
        } catch (e: Exception) {
            // Ignorar errores de carga inicial de preferencias
        }
        try {
            val saved = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_CACHED_CHAMPIONS, null)
            if (!saved.isNullOrBlank()) {
                val legacy = if (saved.startsWith("[")) json.decodeFromString<List<Champion>>(saved).associateBy { it.id } else emptyMap()
                val compact = if (saved.startsWith("{")) org.json.JSONObject(saved) else null
                for (i in WildRiftRepository.champions.indices) {
                    val champ = WildRiftRepository.champions[i]
                    val stats = compact?.optJSONObject(champ.id)
                    val old = legacy[champ.id]
                    if (stats == null && old == null) continue
                    WildRiftRepository.champions[i] = champ.copy(
                        winrate = stats?.optDouble("winrate", champ.winrate) ?: old!!.winrate,
                        pickRate = stats?.optDouble("pickRate", champ.pickRate) ?: old!!.pickRate,
                        banRate = stats?.optDouble("banRate", champ.banRate) ?: old!!.banRate)
                }
            }
        } catch (_: Exception) { }

    }

    fun startContinuousSync(context: Context) { initialize(context) }
    private val syncMutex = kotlinx.coroutines.sync.Mutex()
    private var lastAttemptAt = 0L

    suspend fun syncGlobalTierList(context: Context, region: String = "CN", force: Boolean = false) {
        withContext(Dispatchers.IO) {
            syncMutex.lock()
            try {
                initialize(context)
                val now = System.currentTimeMillis()
                if (!force && _isLastSyncSuccess.value && now - _lastSyncTimestamp.value < 60_000L) return@withContext
                if (_isLastSyncSuccess.value && now - lastAttemptAt < 5_000L) return@withContext
                lastAttemptAt = now
                _isOnline.value = checkNetwork(context)
                if (!_isOnline.value) {
                    _isLastSyncSuccess.value = false
                    _globalSyncStatus.value = "Sin conexión; conservando datos guardados."
                    _sourceStatuses.value = mapOf("TencentSuperServer" to ScraperSourceStatus(
                        "Servidor chino", "https://lolm.qq.com/", false, now, 0L, "Sin conexión", "CN"))
                    return@withContext
                }
                _isSyncing.value = true
                val success = com.example.service.MetaScrapingWorker.fetchChineseStats()
                _isLastSyncSuccess.value = success
                val duration = System.currentTimeMillis() - now
                _sourceStatuses.value = mapOf("TencentSuperServer" to ScraperSourceStatus(
                    "Servidor chino", "https://lolm.qq.com/", success, now, duration,
                    if (success) null else "Respuesta sin estadísticas válidas", "CN"))
                if (success) {
                    val formatted = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(now))
                    _lastSyncTimestamp.value = now
                    _lastSyncFormattedTime.value = formatted
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                        .putLong(KEY_LAST_TIMESTAMP, now).putString(KEY_LAST_FORMATTED, formatted)
                        .putString(KEY_CACHED_CHAMPIONS, org.json.JSONObject().apply {
                            WildRiftRepository.champions.forEach { champ -> put(champ.id,
                                org.json.JSONObject().put("winrate", champ.winrate)
                                    .put("pickRate", champ.pickRate).put("banRate", champ.banRate)) }
                        }.toString()).apply()
                    _globalSyncStatus.value = "Estadísticas del servidor chino actualizadas."
                } else {
                    _globalSyncStatus.value = "Sin actualizar; conservando últimos datos guardados."
                }
            } catch (e: kotlinx.coroutines.CancellationException) { throw e
            } catch (e: Exception) {
                _isLastSyncSuccess.value = false
                _globalSyncStatus.value = "Consulta fallida; conservando últimos datos guardados."
                _sourceStatuses.value = mapOf("TencentSuperServer" to ScraperSourceStatus(
                    "Servidor chino", "https://lolm.qq.com/", false, System.currentTimeMillis(),
                    0L, e.message ?: "Error de consulta", "CN"))
            } finally {
                _isSyncing.value = false
                syncMutex.unlock()
            }
        }
    }

    suspend fun syncAllChampionBuilds(context: Context, region: String = "CN") {
        syncGlobalTierList(context, region, force = true)
    }
}
