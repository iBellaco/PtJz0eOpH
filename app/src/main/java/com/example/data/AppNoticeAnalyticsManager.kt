package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NoticeMetrics(
    val noticeId: String,
    val impressions: Long = 0,
    val clicks: Long = 0,
    val totalRawClicks: Long = 0,
    val fullscreenViews: Long = 0,
    val lastViewedTimestamp: Long = System.currentTimeMillis(),
    val customCpmRate: Double? = null
) {
    val ctr: Double
        get() = CpmPolicy.ctr(impressions, clicks)

    fun calculateRevenue(globalCpmRate: Double, mediaMultiplier: Double = 1.0): Double {
        return CpmPolicy.revenue(impressions, customCpmRate ?: globalCpmRate, mediaMultiplier)
    }
}

object AppNoticeAnalyticsManager {
    private const val TAG = "AppNoticeAnalytics"
    private const val PREFS_NAME = "wild_rift_notice_analytics_prefs"
    private const val KEY_METRICS_JSON = "metrics_json_map"
    private const val KEY_BASE_CPM = "base_cpm_rate_usd"
    private const val KEY_START_DATE = "tracking_start_date_ms"
    private const val KEY_DAILY_IMPRESSIONS_PREFIX = "daily_unique_imps_"
    private const val KEY_DAILY_CLICKS_PREFIX = "daily_unique_clicks_"
    private const val KEY_DAILY_FULLSCREEN_PREFIX = "daily_unique_full_"

    private const val FIRESTORE_COLLECTION = "system_config"
    private const val FIRESTORE_DOC_ANALYTICS = "app_notice_analytics"

    private var firestoreListener: ListenerRegistration? = null
    private var authStateListener: FirebaseAuth.AuthStateListener? = null
    private var isAuthenticatingAnonymously = false

    private val _metricsMap = MutableStateFlow<Map<String, NoticeMetrics>>(emptyMap())
    val metricsMap: StateFlow<Map<String, NoticeMetrics>> = _metricsMap.asStateFlow()

    private val _baseCpmRate = MutableStateFlow(2.50) // $2.50 USD por defecto por cada 1,000 impresiones
    val baseCpmRate: StateFlow<Double> = _baseCpmRate.asStateFlow()

    private val _trackingStartDate = MutableStateFlow(System.currentTimeMillis())
    val trackingStartDate: StateFlow<Long> = _trackingStartDate.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(0L)
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    /**
     * Modelo de cálculo inteligente de CPM en tiempo real.
     * Evalúa las métricas de rendimiento reales del app (CTR, ratio de fullscreen, volumen de impresiones)
     * junto con los benchmarks de la industria en apps móviles de eSports y gaming.
     */
    data class DynamicCpmRecommendation(
        val recommendedCpm: Double,
        val tierName: String,
        val marketBenchmarkMin: Double,
        val marketBenchmarkMax: Double,
        val ctrMultiplier: Double,
        val engagementBonus: Double,
        val reasoning: String,
        val suggestedPriceRange: Pair<Double, Double>,
        val price1Day: Double = 0.0,
        val price3Days: Double = 0.0,
        val price1Week: Double = 0.0,
        val price1Month: Double = 0.0,
        val price1Year: Double = 0.0
    )

    fun calculateRecommendedCpm(): DynamicCpmRecommendation {
        val totalImps = getTotalImpressions()
        val days = ((System.currentTimeMillis() - _trackingStartDate.value).coerceAtLeast(1L) / 86_400_000.0).coerceAtLeast(1.0)
        val rate = _baseCpmRate.value.takeIf { it.isFinite() && it > 0 } ?: 2.50
        val daily = totalImps.coerceAtLeast(0) / days
        fun projected(duration: Double) = kotlin.math.round(daily * duration * rate / 1000 * 100) / 100
        return DynamicCpmRecommendation(rate, "Tarifa configurada", rate, rate, 1.0, 0.0,
            if (totalImps == 0L) "Sin impresiones verificadas: proyección diaria de cero. La tarifa la define el responsable de la campaña."
            else "Proyección según las impresiones registradas y la tarifa configurada; no representa un pago recibido ni un precio de mercado.",
            rate to rate, projected(1.0), projected(3.0), projected(7.0), projected(30.0), projected(365.0))
    }

    fun init(context: Context) {
        val appContext = context.applicationContext
        // 1. Cargar caché local de inmediato (garantiza disponibilidad offline instantánea)
        loadFromLocalStorage(appContext)

        // 2. Monitorear cambios de sesión/autenticación para mantener activo el listener
        setupAuthStateListener(appContext)

        // 3. Conectar a la sincronización en la nube multi-dispositivo
        ensureAuthAndSync(appContext)
    }

    private fun setupAuthStateListener(context: Context) {
        if (authStateListener != null) return
        try {
            val auth = FirebaseAuth.getInstance()
            authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                if (user == null) {
                    ensureAuthAndSync(context)
                } else {
                    attachFirestoreListener(context, force = true)
                    syncFromCloud(context)
                }
            }
            auth.addAuthStateListener(authStateListener!!)
        } catch (e: Exception) {
            Log.w(TAG, "Error en setupAuthStateListener: ${e.message}")
        }
    }

    private fun ensureAuthAndSync(context: Context) {
        val appContext = context.applicationContext
        com.example.util.GuestAuthHelper.ensureAuth {
            attachFirestoreListener(appContext, force = true)
            executeCloudFetch(appContext, null)
        }
    }

    fun attachFirestoreListener(context: Context, force: Boolean = false) {
        if (force) {
            try {
                firestoreListener?.remove()
            } catch (_: Exception) {}
            firestoreListener = null
        }
        if (firestoreListener != null) return
        try {
            val db = FirebaseFirestore.getInstance()
            firestoreListener = db.collection(FIRESTORE_COLLECTION).document(FIRESTORE_DOC_ANALYTICS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Error escuchando analíticas en la nube: ${error.message}")
                        try {
                            firestoreListener?.remove()
                        } catch (_: Exception) {}
                        firestoreListener = null
                        com.example.util.GuestAuthHelper.ensureAuth {
                            attachFirestoreListener(context, force = false)
                        }
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        processFirestoreSnapshot(context, snapshot)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo iniciar listener de analíticas: ${e.message}")
            firestoreListener = null
        }
    }

    fun syncFromCloud(context: Context, onComplete: ((Boolean) -> Unit)? = null) {
        val appContext = context.applicationContext
        val auth = try { FirebaseAuth.getInstance() } catch (_: Exception) { null }
        if (auth?.currentUser == null) {
            com.example.util.GuestAuthHelper.ensureAuth {
                executeCloudFetch(appContext, onComplete)
            }
        } else {
            executeCloudFetch(appContext, onComplete)
        }
    }

    private fun executeCloudFetch(appContext: Context, onComplete: ((Boolean) -> Unit)?) {
        _isSyncing.value = true
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection(FIRESTORE_COLLECTION).document(FIRESTORE_DOC_ANALYTICS)
                .get()
                .addOnSuccessListener { snapshot ->
                    _isSyncing.value = false
                    if (snapshot != null && snapshot.exists()) {
                        processFirestoreSnapshot(appContext, snapshot)
                        _lastSyncTime.value = System.currentTimeMillis()
                        onComplete?.invoke(true)
                    } else {
                        pushLocalToCloud(appContext)
                        _lastSyncTime.value = System.currentTimeMillis()
                        onComplete?.invoke(true)
                    }
                }
                .addOnFailureListener { e ->
                    _isSyncing.value = false
                    Log.w(TAG, "Error forzando sincronización de analíticas: ${e.message}")
                    onComplete?.invoke(false)
                }
        } catch (e: Exception) {
            _isSyncing.value = false
            Log.e(TAG, "Excepción en syncFromCloud: ${e.message}")
            onComplete?.invoke(false)
        }
    }

    private fun processFirestoreSnapshot(context: Context, snapshot: com.google.firebase.firestore.DocumentSnapshot) {
        try {
            val cloudBaseCpm = snapshot.getDouble("baseCpmRate")
            val cloudStartDate = snapshot.getLong("trackingStartDate")

            if (cloudBaseCpm != null && cloudBaseCpm.isFinite() && cloudBaseCpm > 0.0) {
                _baseCpmRate.value = cloudBaseCpm
                saveBaseCpmToPrefs(context, cloudBaseCpm)
            }
            if (cloudStartDate != null && cloudStartDate > 0L) {
                _trackingStartDate.value = cloudStartDate
                saveStartDateToPrefs(context, cloudStartDate)
            }

            val metricsRaw = decodedCpmMetrics(snapshot.data.orEmpty())
            if (metricsRaw != null) {
                val current = mutableMapOf<String, NoticeMetrics>()
                for ((k, v) in metricsRaw) {
                    val noticeId = k?.toString() ?: continue
                    val map = v as? Map<*, *> ?: continue
                    val imps = (map["impressions"] as? Number)?.toLong() ?: 0L
                    val clicks = (map["clicks"] as? Number)?.toLong() ?: 0L
                    val rawClicks = (map["totalRawClicks"] as? Number)?.toLong() ?: 0L
                    val full = (map["fullscreenViews"] as? Number)?.toLong() ?: 0L
                    val lastViewed = (map["lastViewedTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    val customCpmRate = (map["customCpmRate"] as? Number)?.toDouble()


                    current[noticeId] = NoticeMetrics(
                        noticeId = noticeId,
                        impressions = imps.coerceAtLeast(0),
                        clicks = clicks.coerceAtLeast(0),
                        totalRawClicks = rawClicks.coerceAtLeast(0),
                        fullscreenViews = full.coerceAtLeast(0),
                        lastViewedTimestamp = lastViewed,
                        customCpmRate = customCpmRate?.takeIf { it.isFinite() && it > 0 }
                    )
                }
                _metricsMap.value = current
                saveToPrefs(context, current)
                _lastSyncTime.value = System.currentTimeMillis()
                Log.d(TAG, "Métricas publicitarias sincronizadas exitosamente en tiempo real (${current.size} anuncios).")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error procesando snapshot de analíticas: ${e.message}")
        }
    }

    private fun pushLocalToCloud(context: Context) {
        if (!SupportTicketAccess.isAdmin()) return
        try {
            val db = FirebaseFirestore.getInstance()
            val metricsData = hashMapOf<String, Any>()
            for ((k, v) in _metricsMap.value) {
                metricsData[k] = hashMapOf(
                    "noticeId" to v.noticeId,
                    "impressions" to v.impressions,
                    "clicks" to v.clicks,
                    "totalRawClicks" to v.totalRawClicks,
                    "fullscreenViews" to v.fullscreenViews,
                    "lastViewedTimestamp" to v.lastViewedTimestamp
                )
            }
            val data = hashMapOf<String, Any>(
                "baseCpmRate" to _baseCpmRate.value,
                "trackingStartDate" to _trackingStartDate.value,
                "metrics" to metricsData,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection(FIRESTORE_COLLECTION).document(FIRESTORE_DOC_ANALYTICS)
                .set(data, SetOptions.merge())
        } catch (e: Exception) {
            Log.w(TAG, "Error subiendo analíticas locales a la nube: ${e.message}")
        }
    }

    private fun loadFromLocalStorage(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val baseCpm = prefs.getString("${KEY_BASE_CPM}_exact", null)?.toDoubleOrNull()
                ?.takeIf { it.isFinite() && it > 0 } ?: prefs.getFloat(KEY_BASE_CPM, 2.50f).toDouble()
            val startDate = prefs.getLong(KEY_START_DATE, System.currentTimeMillis())
            val jsonStr = prefs.getString(KEY_METRICS_JSON, null)

            _baseCpmRate.value = baseCpm
            _trackingStartDate.value = startDate
            saveStartDateToPrefs(context, startDate)

            if (!jsonStr.isNullOrBlank()) {
                val root = JSONObject(jsonStr)
                val map = mutableMapOf<String, NoticeMetrics>()
                val keys = root.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val obj = root.getJSONObject(key)
                    map[key] = NoticeMetrics(
                        noticeId = key,
                        impressions = obj.optLong("impressions", 0L),
                        clicks = obj.optLong("clicks", 0L),
                        totalRawClicks = obj.optLong("totalRawClicks", 0L),
                        fullscreenViews = obj.optLong("fullscreenViews", 0L),
                        customCpmRate = if (obj.has("customCpmRate")) obj.optDouble("customCpmRate").takeIf { it.isFinite() && it > 0 } else null,
                        lastViewedTimestamp = obj.optLong("lastViewedTimestamp", System.currentTimeMillis())
                    )
                }
                _metricsMap.value = map
            }
        } catch (_: Exception) {}
    }

    /**
     * Registra una impresión única (una sola vez por dispositivo al día).
     * Si alreadyRecordedToday=true, omite el conteo local y en la nube.
     */
    @Synchronized
    fun recordImpression(context: Context, noticeId: String, noticeTag: String = "") {
        if (noticeId.isBlank()) return
        
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val todayDate = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
            val dailyKey = "$KEY_DAILY_IMPRESSIONS_PREFIX${todayDate}_$noticeId"
            
            // Comprobamos si ya fue contabilizado hoy en este dispositivo
            val alreadyCountedToday = prefs.getBoolean(dailyKey, false)
            if (alreadyCountedToday) {
                return
            }
            
            // Marcar como contabilizado para hoy
            prefs.edit().putBoolean(dailyKey, true).apply()
        } catch (_: Exception) {}

        val current = _metricsMap.value.toMutableMap()
        val existing = current[noticeId] ?: NoticeMetrics(noticeId = noticeId)
        val updated = existing.copy(
            impressions = existing.impressions + 1,
            lastViewedTimestamp = System.currentTimeMillis()
        )
        current[noticeId] = updated
        _metricsMap.value = current
        saveToPrefs(context, current)

        // Sincronizar incremento atómico en la nube para todos los dispositivos
        try {
            val db = FirebaseFirestore.getInstance()
            val updates = hashMapOf<String, Any>(
                "metrics.$noticeId.impressions" to FieldValue.increment(1L),
                "metrics.$noticeId.noticeId" to noticeId,
                "metrics.$noticeId.tag" to noticeTag,
                "metrics.$noticeId.lastViewedTimestamp" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection(FIRESTORE_COLLECTION).document(FIRESTORE_DOC_ANALYTICS)
                .set(CpmPolicy.nestedWrite(updates), SetOptions.merge())
        } catch (e: Exception) {
            Log.w(TAG, "Error incrementando impresión en la nube: ${e.message}")
        }
    }

    @Synchronized
    fun recordClick(context: Context, noticeId: String) {
        if (noticeId.isBlank()) return

        var incrementUniqueClick = false
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val todayDate = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
            val dailyKey = "$KEY_DAILY_CLICKS_PREFIX${todayDate}_$noticeId"
            
            val alreadyClickedToday = prefs.getBoolean(dailyKey, false)
            if (!alreadyClickedToday) {
                prefs.edit().putBoolean(dailyKey, true).apply()
                incrementUniqueClick = true
            }
        } catch (_: Exception) {}

        val current = _metricsMap.value.toMutableMap()
        val existing = current[noticeId] ?: NoticeMetrics(noticeId = noticeId)
        val updated = existing.copy(
            clicks = existing.clicks + (if (incrementUniqueClick) 1 else 0),
            totalRawClicks = existing.totalRawClicks + 1,
            lastViewedTimestamp = System.currentTimeMillis()
        )
        current[noticeId] = updated
        _metricsMap.value = current
        saveToPrefs(context, current)

        try {
            val db = FirebaseFirestore.getInstance()
            val updates = hashMapOf<String, Any>(
                "metrics.$noticeId.totalRawClicks" to FieldValue.increment(1L),
                "metrics.$noticeId.noticeId" to noticeId,
                "metrics.$noticeId.lastViewedTimestamp" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            if (incrementUniqueClick) {
                updates["metrics.$noticeId.clicks"] = FieldValue.increment(1L)
            }
            db.collection(FIRESTORE_COLLECTION).document(FIRESTORE_DOC_ANALYTICS)
                .set(CpmPolicy.nestedWrite(updates), SetOptions.merge())
        } catch (e: Exception) {
            Log.w(TAG, "Error incrementando clic en la nube: ${e.message}")
        }
    }

    @Synchronized
    fun recordFullscreen(context: Context, noticeId: String) {
        if (noticeId.isBlank()) return

        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val todayDate = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
            val dailyKey = "$KEY_DAILY_FULLSCREEN_PREFIX${todayDate}_$noticeId"
            
            val alreadyFullToday = prefs.getBoolean(dailyKey, false)
            if (alreadyFullToday) {
                return
            }
            prefs.edit().putBoolean(dailyKey, true).apply()
        } catch (_: Exception) {}

        val current = _metricsMap.value.toMutableMap()
        val existing = current[noticeId] ?: NoticeMetrics(noticeId = noticeId)
        val updated = existing.copy(
            fullscreenViews = existing.fullscreenViews + 1,
            lastViewedTimestamp = System.currentTimeMillis()
        )
        current[noticeId] = updated
        _metricsMap.value = current
        saveToPrefs(context, current)

        // Sincronizar incremento atómico de vistas de pantalla completa en la nube
        try {
            val db = FirebaseFirestore.getInstance()
            val updates = hashMapOf<String, Any>(
                "metrics.$noticeId.fullscreenViews" to FieldValue.increment(1L),
                "metrics.$noticeId.noticeId" to noticeId,
                "metrics.$noticeId.lastViewedTimestamp" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection(FIRESTORE_COLLECTION).document(FIRESTORE_DOC_ANALYTICS)
                .set(CpmPolicy.nestedWrite(updates), SetOptions.merge())
        } catch (e: Exception) {
            Log.w(TAG, "Error incrementando fullscreen en la nube: ${e.message}")
        }
    }

    fun setBaseCpm(context: Context, rate: Double, onComplete: (Boolean) -> Unit = {}) {
        if (!rate.isFinite() || rate <= 0) { onComplete(false); return }
        try {
            FirebaseFirestore.getInstance().collection(FIRESTORE_COLLECTION).document(FIRESTORE_DOC_ANALYTICS)
                .set(mapOf("baseCpmRate" to rate, "updatedAt" to System.currentTimeMillis()), SetOptions.merge())
                .addOnSuccessListener { _baseCpmRate.value = rate; saveBaseCpmToPrefs(context, rate); onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        } catch (_: Exception) { onComplete(false) }
    }

    fun setNoticeCpm(context: Context, noticeId: String, customCpm: Double?, onComplete: (Boolean) -> Unit = {}) {
        if (noticeId.isBlank() || customCpm != null && (!customCpm.isFinite() || customCpm <= 0)) { onComplete(false); return }
        try {
            val value: Any = customCpm ?: FieldValue.delete()
            FirebaseFirestore.getInstance().collection(FIRESTORE_COLLECTION).document(FIRESTORE_DOC_ANALYTICS)
                .set(mapOf("metrics" to mapOf(noticeId to mapOf("customCpmRate" to value))), SetOptions.merge())
                .addOnSuccessListener {
                    val map = _metricsMap.value.toMutableMap()
                    map[noticeId] = (map[noticeId] ?: NoticeMetrics(noticeId)).copy(customCpmRate = customCpm)
                    _metricsMap.value = map; saveToPrefs(context, map); onComplete(true)
                }.addOnFailureListener { onComplete(false) }
        } catch (_: Exception) { onComplete(false) }
    }

    fun deleteNoticeMetrics(context: Context, noticeId: String) {
        if (noticeId.isBlank()) return
        try {
            val db = FirebaseFirestore.getInstance()
            val ref = db.collection(FIRESTORE_COLLECTION).document(FIRESTORE_DOC_ANALYTICS)
            db.runTransaction { tx ->
                val data = tx.get(ref).data.orEmpty()
                tx.set(ref, clearedCpmData(data, noticeId) + ("updatedAt" to System.currentTimeMillis()))
            }.addOnSuccessListener {
                val current = _metricsMap.value - noticeId
                _metricsMap.value = current; saveToPrefs(context, current)
            }.addOnFailureListener { e -> Log.w(TAG, "No se pudieron eliminar las métricas: ${e.message}") }
        } catch (e: Exception) { Log.w(TAG, "No se pudieron eliminar las métricas: ${e.message}") }
    }

    fun resetMetrics(context: Context, onComplete: (Boolean) -> Unit = {}) {
        val startDate = System.currentTimeMillis()
        try {
            val db = FirebaseFirestore.getInstance()
            val ref = db.collection(FIRESTORE_COLLECTION).document(FIRESTORE_DOC_ANALYTICS)
            db.runTransaction { tx ->
                val data = tx.get(ref).data.orEmpty()
                tx.set(ref, clearedCpmData(data) + mapOf("trackingStartDate" to startDate,
                    "baseCpmRate" to _baseCpmRate.value, "updatedAt" to startDate))
            }.addOnSuccessListener {
                _metricsMap.value = emptyMap(); _trackingStartDate.value = startDate
                saveToPrefs(context, emptyMap()); saveStartDateToPrefs(context, startDate)
                // A new campaign starts a new deduplication window too.
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val editor = prefs.edit()
                prefs.all.keys.filter { key -> listOf(KEY_DAILY_IMPRESSIONS_PREFIX,
                    KEY_DAILY_CLICKS_PREFIX, KEY_DAILY_FULLSCREEN_PREFIX).any { key.startsWith(it) } }
                    .forEach { editor.remove(it) }
                editor.apply(); onComplete(true)
            }.addOnFailureListener { onComplete(false) }
        } catch (_: Exception) { onComplete(false) }
    }

    private fun saveBaseCpmToPrefs(context: Context, rate: Double) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString("${KEY_BASE_CPM}_exact", rate.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun saveStartDateToPrefs(context: Context, date: Long) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putLong(KEY_START_DATE, date).apply()
        } catch (_: Exception) {}
    }

    private fun saveToPrefs(context: Context, map: Map<String, NoticeMetrics>) {
        try {
            val root = JSONObject()
            for ((key, metrics) in map) {
                val obj = JSONObject().apply {
                    put("impressions", metrics.impressions)
                    put("clicks", metrics.clicks)
                    put("totalRawClicks", metrics.totalRawClicks)
                    metrics.customCpmRate?.let { put("customCpmRate", it) }
                    put("fullscreenViews", metrics.fullscreenViews)
                    put("lastViewedTimestamp", metrics.lastViewedTimestamp)
                }
                root.put(key, obj)
            }
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_METRICS_JSON, root.toString()).apply()
        } catch (_: Exception) {}
    }

    fun getTotalImpressions(): Long = _metricsMap.value.values.sumOf { it.impressions }
    fun getTotalClicks(): Long = _metricsMap.value.values.sumOf { it.clicks }
    fun getTotalFullscreenViews(): Long = _metricsMap.value.values.sumOf { it.fullscreenViews }

    fun getTotalRevenue(globalCpmRate: Double = _baseCpmRate.value, notices: List<AppNotice>? = null): Double {
        return _metricsMap.value.values.sumOf { metrics -> 
            val notice = notices?.find { it.id == metrics.noticeId }
            val mediaMultiplier = if (notice != null && notice.videoUrl.isNotBlank()) {
                if (notice.videoUrl.contains("video") || notice.videoUrl.endsWith(".mp4") || notice.videoUrl.contains("youtube")) 2.5 // Video 10s = 2.5x base CPM
                else 1.5 // Image = 1.5x base CPM
            } else 1.0 // Plain text = 1x base CPM
            metrics.calculateRevenue(globalCpmRate, mediaMultiplier)
        }
    }

    fun getOverallCtr(): Double {
        val totalImps = getTotalImpressions()
        val totalClicks = getTotalClicks()
        return CpmPolicy.ctr(totalImps, totalClicks)
    }

    fun generateSummaryReport(notices: List<AppNotice>): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val startFormatted = sdf.format(Date(_trackingStartDate.value))
        val nowFormatted = sdf.format(Date())
        val totalImps = getTotalImpressions()
        val totalClicks = getTotalClicks()
        val totalFullscreen = getTotalFullscreenViews()
        val cpm = _baseCpmRate.value
        val totalRev = getTotalRevenue(cpm, notices)
        val overallCtr = getOverallCtr()
        val dynamicRec = calculateRecommendedCpm()

        val totalBudget = notices.sumOf { it.budget }
        val sb = StringBuilder()
        sb.append("REPORTE DE MONETIZACION Y CPM - WILD RIFT COACH\n")
        sb.append("====================================================\n")
        sb.append("Periodo: $startFormatted hasta $nowFormatted\n")
        sb.append("Tarifa Configurada: $${String.format(Locale.US, "%.2f", cpm)} USD / 1,000 Imp.\n")
        sb.append("Tarifa Recomendada Inteligente: $${String.format(Locale.US, "%.2f", dynamicRec.recommendedCpm)} USD (${dynamicRec.tierName})\n")
        sb.append("Rango de Venta Sugerido: $${String.format(Locale.US, "%.2f", dynamicRec.suggestedPriceRange.first)} - $${String.format(Locale.US, "%.2f", dynamicRec.suggestedPriceRange.second)} USD\n")
        sb.append("Impresiones Unicas Totales: $totalImps (1x disp/dia)\n")
        sb.append("Clics Unicos Totales: $totalClicks (CTR: ${String.format(Locale.US, "%.2f", overallCtr)}%)\n")
        sb.append("Pantalla Completa: $totalFullscreen vistas\n")
        sb.append("Presupuesto Total de Anuncios: $${String.format(Locale.US, "%.2f", totalBudget)} USD\n")
        sb.append("Ingresos Estimados Totales: $${String.format(Locale.US, "%.2f", totalRev)} USD\n")
        sb.append("====================================================\n")
        sb.append("DESGLOSE POR ANUNCIO / CAMPANA:\n")

        for (n in notices) {
            val m = _metricsMap.value[n.id] ?: NoticeMetrics(n.id)
            val mediaMultiplier = if (n.videoUrl.isNotBlank()) {
                if (n.videoUrl.contains("video") || n.videoUrl.endsWith(".mp4") || n.videoUrl.contains("youtube")) 2.5
                else 1.5
            } else 1.0
            val rev = m.calculateRevenue(cpm, mediaMultiplier)
            val budgetStr = if (n.budget > 0) "$${String.format(Locale.US, "%.2f", n.budget)} USD" else "Sin asignar"
            val spentPct = if (n.budget > 0) " (${String.format(Locale.US, "%.1f", (rev / n.budget) * 100.0)}% consumido)" else ""
            sb.append("\n[${n.tag.uppercase()}] ${n.title}\n")
            sb.append("  - Presupuesto: $budgetStr$spentPct\n")
            sb.append("  - Imp. Unicas: ${m.impressions}\n")
            sb.append("  - Clics Unicos: ${m.clicks} (CTR: ${String.format(Locale.US, "%.2f", m.ctr)}%)\n")
            sb.append("  - Fullscreen: ${m.fullscreenViews}\n")
            sb.append("  - Generado: $${String.format(Locale.US, "%.2f", rev)} USD\n")
        }

        return sb.toString()
    }
}

