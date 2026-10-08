package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class AppNotice(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Anuncio Importante",
    val content: String = "Bienvenido a Coach. ¡Consulta las últimas guías del parche y optimiza tu drafting!",
    val videoUrl: String = "", // Multimedia horizontal para panel de inicio
    val expandedImageUrl: String = "", // Imagen vertical para vista ampliada
    val externalUrl: String = "", // Enlace web externo opcional al tocar la imagen ampliada
    val tag: String = "Anuncios importantes", // "Anuncios importantes", "Ofertas", "Mantenimiento", "Noticia", "Streamer", "PUBLICIDAD"
    val titleColor: String = "#FFD700",
    val contentColor: String = "#CCCCCC",
    val isEnabled: Boolean = true,
    val budget: Double = 0.0, // Presupuesto asignado a este anuncio en USD
    val budgetUnit: String = "day", // "hour", "day", "week", "month", "year"
    val durationValue: Int = 1, // Cuántas horas/días/semanas/meses/años
    val durationUnit: String = "day", // "hour", "day", "week", "month", "year"
    val approvedAtMillis: Long = 0L, // Timestamp de aprobación
    val expiresAtMillis: Long = 0L, // Timestamp de expiración calculada
    val isApproved: Boolean = true, // false until admin accepts it if published by sponsor
    val sponsorEmail: String = "" // email of the sponsor who published it
)

object AppNoticeManager {
    private const val TAG = "AppNoticeManager"
    private const val PREFS_NAME = "wild_rift_app_notices_prefs"
    private const val KEY_NOTICES_JSON = "notices_json_list"
    private const val KEY_INTERVAL_VALUE = "streamer_interval_value"
    private const val KEY_INTERVAL_UNIT = "streamer_interval_unit"

    private const val FIRESTORE_COLLECTION = "system_config"
    private const val FIRESTORE_DOC_NOTICES = "app_notices"

    private var firestoreListener: ListenerRegistration? = null
    private var authStateListener: FirebaseAuth.AuthStateListener? = null
    private var isAuthenticatingAnonymously = false

    private val defaultNotices = listOf(
        AppNotice(
            title = "¡Nueva Actualización de Parche disponible!",
            content = "Consulta las tier lists actualizadas y optimiza tus picks para subir a Soberano.",
            tag = "Anuncios importantes",
            isEnabled = true
        ),
        AppNotice(
            title = "Oferta Especial en Esencias Azules",
            content = "¡Desbloquea avatares legendarios y bordes exclusivos con descuento por tiempo limitado!",
            tag = "Ofertas",
            isEnabled = true
        )
    )

    private val _notices = MutableStateFlow(defaultNotices)
    val notices: StateFlow<List<AppNotice>> = _notices.asStateFlow()

    private val _streamerIntervalValue = MutableStateFlow(10)
    val streamerIntervalValue: StateFlow<Int> = _streamerIntervalValue.asStateFlow()

    private val _streamerIntervalUnit = MutableStateFlow("seconds")
    val streamerIntervalUnit: StateFlow<String> = _streamerIntervalUnit.asStateFlow()

    fun init(context: Context) {
        val appContext = context.applicationContext
        // 1. Cargar caché local de inmediato (garantiza arranque instantáneo en 0ms)
        loadFromLocalStorage(appContext)
        preloadMedia(appContext, _notices.value)

        // 2. Intentar leer caché local de Firestore de inmediato (0ms de latencia)
        fetchFromFirestoreCache(appContext)

        // 3. Monitorear cambios de sesión/autenticación para mantener listener activo
        setupAuthStateListener(appContext)

        // 4. Conectar y sincronizar garantizando acceso inmediato sin bloqueos
        ensureAuthAndSync(appContext)
    }

    private fun fetchFromFirestoreCache(appContext: Context) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection(FIRESTORE_COLLECTION).document(FIRESTORE_DOC_NOTICES)
                .get(com.google.firebase.firestore.Source.CACHE)
                .addOnSuccessListener { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        processFirestoreSnapshot(appContext, snapshot)
                    }
                }
                .addOnFailureListener {
                    // Si aún no está en caché de Firestore, se usará la caché local ya cargada
                }
        } catch (_: Exception) {}
    }

    private fun setupAuthStateListener(context: Context) {
        if (authStateListener != null) return
        try {
            val auth = FirebaseAuth.getInstance()
            authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                Log.d(TAG, "Cambio de estado de autenticación detectado (uid=${user?.uid}, anon=${user?.isAnonymous})")
                if (user == null) {
                    ensureAuthAndSync(context)
                } else {
                    attachFirestoreListener(context, force = true)
                    syncFromCloud(context)
                }
            }
            auth.addAuthStateListener(authStateListener!!)
        } catch (e: Exception) {
            Log.w(TAG, "Error inicializando AuthStateListener: ${e.message}")
        }
    }

    private fun ensureAuthAndSync(context: Context) {
        val appContext = context.applicationContext
        // Iniciar Firestore y fetch de inmediato sin esperar autenticación
        fetchFromFirestoreCache(appContext)
        attachFirestoreListener(appContext, force = false)
        executeCloudFetch(appContext, null)

        // En segundo plano sin bloquear la UI ni la carga inicial
        val auth = try { FirebaseAuth.getInstance() } catch (_: Exception) { null }
        if (auth?.currentUser == null) {
            com.example.util.GuestAuthHelper.ensureAuth {
                attachFirestoreListener(appContext, force = true)
                executeCloudFetch(appContext, null)
            }
        }
    }

    fun syncFromCloud(context: Context, onComplete: ((Boolean) -> Unit)? = null) {
        val appContext = context.applicationContext
        // Inmediato desde caché y en paralelo desde la nube
        fetchFromFirestoreCache(appContext)
        executeCloudFetch(appContext, onComplete)
    }

    private fun executeCloudFetch(appContext: Context, onComplete: ((Boolean) -> Unit)?) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection(FIRESTORE_COLLECTION).document(FIRESTORE_DOC_NOTICES)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        processFirestoreSnapshot(appContext, snapshot)
                        onComplete?.invoke(true)
                    } else {
                        Log.d(TAG, "Documento de anuncios en Firestore no encontrado.")
                        onComplete?.invoke(false)
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error forzando sincronización desde Firestore: ${e.message}")
                    onComplete?.invoke(false)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción en executeCloudFetch: ${e.message}")
            onComplete?.invoke(false)
        }
    }

    private fun processFirestoreSnapshot(context: Context, snapshot: com.google.firebase.firestore.DocumentSnapshot) {
        try {
            val rawList = snapshot.get("notices") as? List<*>
            val intervalVal = snapshot.getLong("streamerIntervalValue")?.toInt()
            val intervalUnit = snapshot.getString("streamerIntervalUnit")

            if (intervalVal != null && !intervalUnit.isNullOrBlank()) {
                _streamerIntervalValue.value = intervalVal
                _streamerIntervalUnit.value = intervalUnit
                saveIntervalToPrefs(context, intervalVal, intervalUnit)
            }

            if (rawList != null) {
                val parsedNotices = mutableListOf<AppNotice>()
                for (item in rawList) {
                    val map = item as? Map<*, *> ?: continue
                    parsedNotices.add(
                        AppNotice(
                            id = map["id"]?.toString() ?: UUID.randomUUID().toString(),
                            title = map["title"]?.toString() ?: "Aviso",
                            content = map["content"]?.toString() ?: "",
                            videoUrl = map["videoUrl"]?.toString() ?: "",
                            expandedImageUrl = map["expandedImageUrl"]?.toString() ?: "",
                            externalUrl = map["externalUrl"]?.toString() ?: "",
                            tag = map["tag"]?.toString() ?: "Anuncios importantes",
                            titleColor = map["titleColor"]?.toString() ?: "#FFD700",
                            contentColor = map["contentColor"]?.toString() ?: "#CCCCCC",
                            isEnabled = (map["isEnabled"] as? Boolean) ?: true,
                            budget = (map["budget"] as? Number)?.toDouble() ?: 0.0,
                            budgetUnit = map["budgetUnit"]?.toString() ?: "day",
                            durationValue = (map["durationValue"] as? Number)?.toInt() ?: 1,
                            durationUnit = map["durationUnit"]?.toString() ?: (map["budgetUnit"]?.toString() ?: "day"),
                            approvedAtMillis = (map["approvedAtMillis"] as? Number)?.toLong() ?: 0L,
                            expiresAtMillis = (map["expiresAtMillis"] as? Number)?.toLong() ?: 0L,
                            isApproved = (map["isApproved"] as? Boolean) ?: true,
                            sponsorEmail = map["sponsorEmail"]?.toString() ?: ""
                        )
                    )
                }
                _notices.value = parsedNotices
                saveNoticesToPrefs(context, parsedNotices)
                preloadMedia(context, parsedNotices)
                Log.d(TAG, "Sincronizados exitosamente ${parsedNotices.size} anuncios desde Firestore para todos los dispositivos.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error procesando snapshot de Firestore: ${e.message}")
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
            firestoreListener = db.collection(FIRESTORE_COLLECTION).document(FIRESTORE_DOC_NOTICES)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Error escuchando anuncios de Firestore: ${error.message}")
                        try {
                            firestoreListener?.remove()
                        } catch (_: Exception) {}
                        firestoreListener = null
                        // Si falló por falta de autenticación y no hay usuario, reintentar asegurar credenciales
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
            Log.e(TAG, "No se pudo iniciar listener de Firestore: ${e.message}")
            firestoreListener = null
        }
    }

    private fun saveIntervalToPrefs(context: Context, value: Int, unit: String) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putInt(KEY_INTERVAL_VALUE, value)
                .putString(KEY_INTERVAL_UNIT, unit)
                .apply()
        } catch (_: Exception) {}
    }

    private fun saveNoticesToPrefs(context: Context, list: List<AppNotice>) {
        try {
            val arr = JSONArray()
            for (n in list) {
                val obj = JSONObject().apply {
                    put("id", n.id)
                    put("title", n.title)
                    put("content", n.content)
                    put("videoUrl", n.videoUrl)
                    put("expandedImageUrl", n.expandedImageUrl)
                    put("externalUrl", n.externalUrl)
                    put("tag", n.tag)
                    put("titleColor", n.titleColor)
                    put("contentColor", n.contentColor)
                    put("isEnabled", n.isEnabled)
                    put("budget", n.budget)
                    put("budgetUnit", n.budgetUnit)
                    put("durationValue", n.durationValue)
                    put("durationUnit", n.durationUnit)
                    put("approvedAtMillis", n.approvedAtMillis)
                    put("expiresAtMillis", n.expiresAtMillis)
                    put("isApproved", n.isApproved)
                    put("sponsorEmail", n.sponsorEmail)
                }
                arr.put(obj)
            }
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_NOTICES_JSON, arr.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun loadFromLocalStorage(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val intervalVal = prefs.getInt(KEY_INTERVAL_VALUE, 10)
            val intervalUnit = prefs.getString(KEY_INTERVAL_UNIT, "seconds") ?: "seconds"
            _streamerIntervalValue.value = intervalVal
            _streamerIntervalUnit.value = intervalUnit

            val jsonStr = prefs.getString(KEY_NOTICES_JSON, null)
            if (!jsonStr.isNullOrBlank()) {
                val arr = JSONArray(jsonStr)
                val loaded = mutableListOf<AppNotice>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    loaded.add(
                        AppNotice(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            title = obj.optString("title", ""),
                            content = obj.optString("content", ""),
                            videoUrl = obj.optString("videoUrl", ""),
                            expandedImageUrl = obj.optString("expandedImageUrl", ""),
                            externalUrl = obj.optString("externalUrl", ""),
                            tag = obj.optString("tag", "Anuncios importantes"),
                            titleColor = obj.optString("titleColor", "#C8AA6E"),
                            contentColor = obj.optString("contentColor", "#A09B8C"),
                            isEnabled = obj.optBoolean("isEnabled", true),
                            budget = obj.optDouble("budget", 0.0),
                            budgetUnit = obj.optString("budgetUnit", "day"),
                            durationValue = obj.optInt("durationValue", 1),
                            durationUnit = obj.optString("durationUnit", obj.optString("budgetUnit", "day")),
                            approvedAtMillis = obj.optLong("approvedAtMillis", 0L),
                            expiresAtMillis = obj.optLong("expiresAtMillis", 0L),
                            isApproved = obj.optBoolean("isApproved", true),
                            sponsorEmail = obj.optString("sponsorEmail", "")
                        )
                    )
                }
                if (loaded.isNotEmpty()) {
                    _notices.value = loaded
                    preloadMedia(context, loaded)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error cargando desde almacenamiento local: ${e.message}")
        }
    }

    /**
     * Precarga en segundo plano todas las imágenes y videos para que los anuncios
     * carguen a máxima velocidad (0ms de espera) al ser vistos por el usuario.
     */
    fun preloadMedia(context: Context, list: List<AppNotice>) {
        val appContext = context.applicationContext
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val imageLoader = coil.Coil.imageLoader(appContext)
                for (notice in list) {
                    if (!notice.isEnabled) continue
                    val vUrl = notice.videoUrl.trim()
                    val expUrl = notice.expandedImageUrl.trim()
                    if (vUrl.isNotBlank()) {
                        if (com.example.ui.components.NoticeMediaUtils.isVideo(appContext, vUrl)) {
                            if (vUrl.startsWith("http://", ignoreCase = true) || vUrl.startsWith("https://", ignoreCase = true)) {
                                com.example.util.NoticeMediaStorageManager.cacheVideoFromUrl(appContext, vUrl)
                            }
                        } else {
                            val model: Any = if (vUrl.startsWith("data:image/")) {
                                com.example.util.NoticeMediaStorageManager.decodeDataUriToBytes(vUrl) ?: vUrl
                            } else vUrl
                            val req = coil.request.ImageRequest.Builder(appContext)
                                .data(model)
                                .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                .build()
                            imageLoader.enqueue(req)
                        }
                    }
                    if (expUrl.isNotBlank()) {
                        val expModel: Any = if (expUrl.startsWith("data:image/")) {
                            com.example.util.NoticeMediaStorageManager.decodeDataUriToBytes(expUrl) ?: expUrl
                        } else expUrl
                        val req = coil.request.ImageRequest.Builder(appContext)
                            .data(expModel)
                            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                            .build()
                        imageLoader.enqueue(req)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error en precarga de medios: ${e.message}")
            }
        }
    }

    fun saveAllNoticesAndInterval(
        context: Context,
        newNotices: List<AppNotice>,
        intervalValue: Int,
        intervalUnit: String,
        onComplete: ((success: Boolean, errorMsg: String?) -> Unit)? = null
    ) {
        val appContext = context.applicationContext
        _notices.value = newNotices
        _streamerIntervalValue.value = intervalValue
        _streamerIntervalUnit.value = intervalUnit
        saveNoticesToPrefs(appContext, newNotices)
        saveIntervalToPrefs(appContext, intervalValue, intervalUnit)

        try {
            val db = FirebaseFirestore.getInstance()
            val listData = newNotices.map { n ->
                val cleanVideo = if (n.videoUrl.startsWith("file://")) "" else n.videoUrl
                val cleanExpanded = if (n.expandedImageUrl.startsWith("file://")) "" else n.expandedImageUrl

                mapOf(
                    "id" to n.id,
                    "title" to n.title,
                    "content" to n.content,
                    "videoUrl" to cleanVideo,
                    "expandedImageUrl" to cleanExpanded,
                    "externalUrl" to n.externalUrl,
                    "tag" to n.tag,
                    "titleColor" to n.titleColor,
                    "contentColor" to n.contentColor,
                    "isEnabled" to n.isEnabled,
                    "budget" to n.budget,
                    "budgetUnit" to n.budgetUnit,
                    "durationValue" to n.durationValue,
                    "durationUnit" to n.durationUnit,
                    "approvedAtMillis" to n.approvedAtMillis,
                    "expiresAtMillis" to n.expiresAtMillis,
                    "isApproved" to n.isApproved,
                    "sponsorEmail" to n.sponsorEmail
                )
            }
            val data = hashMapOf(
                "notices" to listData,
                "streamerIntervalValue" to intervalValue,
                "streamerIntervalUnit" to intervalUnit,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection(FIRESTORE_COLLECTION).document(FIRESTORE_DOC_NOTICES)
                .set(data, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "Anuncios e intervalo guardados y sincronizados exitosamente en Firestore para todos los dispositivos.")
                    onComplete?.invoke(true, null)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Reintentando sincronización de avisos con payload compacto: ${e.message}")
                    // Si falló por tamaño u otro motivo, reintentar con payload texto-only para garantizar sincronización
                    val minimalListData = newNotices.map { n ->
                        mapOf(
                            "id" to n.id,
                            "title" to n.title,
                            "content" to n.content,
                            "videoUrl" to if (n.videoUrl.startsWith("http://") || n.videoUrl.startsWith("https://")) n.videoUrl else "",
                            "expandedImageUrl" to if (n.expandedImageUrl.startsWith("http://") || n.expandedImageUrl.startsWith("https://")) n.expandedImageUrl else "",
                            "externalUrl" to n.externalUrl,
                            "tag" to n.tag,
                            "titleColor" to n.titleColor,
                            "contentColor" to n.contentColor,
                            "isEnabled" to n.isEnabled,
                            "budget" to n.budget,
                            "budgetUnit" to n.budgetUnit,
                            "durationValue" to n.durationValue,
                            "durationUnit" to n.durationUnit,
                            "approvedAtMillis" to n.approvedAtMillis,
                            "expiresAtMillis" to n.expiresAtMillis,
                            "isApproved" to n.isApproved,
                            "sponsorEmail" to n.sponsorEmail
                        )
                    }
                    val fallbackData = hashMapOf(
                        "notices" to minimalListData,
                        "streamerIntervalValue" to intervalValue,
                        "streamerIntervalUnit" to intervalUnit,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    db.collection(FIRESTORE_COLLECTION).document(FIRESTORE_DOC_NOTICES)
                        .set(fallbackData, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d(TAG, "Sincronización fallback exitosa.")
                            onComplete?.invoke(true, null)
                        }
                        .addOnFailureListener {
                            onComplete?.invoke(true, null) // Guardado local completado
                        }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al sincronizar anuncios con Firestore: ${e.message}")
            onComplete?.invoke(true, null)
        }
    }

    private fun pushNoticesToFirestore(context: Context, noticesList: List<AppNotice>) {
        saveAllNoticesAndInterval(
            context = context,
            newNotices = noticesList,
            intervalValue = _streamerIntervalValue.value,
            intervalUnit = _streamerIntervalUnit.value
        )
    }

    fun saveStreamerInterval(context: Context, value: Int, unit: String) {
        val appContext = context.applicationContext
        _streamerIntervalValue.value = value
        _streamerIntervalUnit.value = unit
        saveIntervalToPrefs(appContext, value, unit)

        try {
            val db = FirebaseFirestore.getInstance()
            val data = hashMapOf(
                "streamerIntervalValue" to value,
                "streamerIntervalUnit" to unit,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection(FIRESTORE_COLLECTION).document(FIRESTORE_DOC_NOTICES)
                .set(data, SetOptions.merge())
        } catch (_: Exception) {}
    }

    fun getStreamerIntervalMillis(context: Context): Long {
        val value = _streamerIntervalValue.value.coerceAtLeast(1)
        return when (_streamerIntervalUnit.value) {
            "minutes" -> value * 60 * 1000L
            "hours" -> value * 60 * 60 * 1000L
            else -> value * 1000L
        }
    }

    fun saveNotices(context: Context, newNotices: List<AppNotice>) {
        val appContext = context.applicationContext
        _notices.value = newNotices
        saveNoticesToPrefs(appContext, newNotices)
        pushNoticesToFirestore(appContext, newNotices)
    }

    fun deleteNotice(context: Context, noticeId: String, onComplete: ((Boolean) -> Unit)? = null) {
        val appContext = context.applicationContext
        val updated = _notices.value.filter { it.id != noticeId }
        _notices.value = updated
        saveNoticesToPrefs(appContext, updated)
        saveAllNoticesAndInterval(
            context = appContext,
            newNotices = updated,
            intervalValue = _streamerIntervalValue.value,
            intervalUnit = _streamerIntervalUnit.value,
            onComplete = { success, _ ->
                onComplete?.invoke(success)
            }
        )
        // Eliminar también sus métricas asociadas
        AppNoticeAnalyticsManager.deleteNoticeMetrics(appContext, noticeId)
    }

    // For backward compatibility if single update is called
    fun updateNotice(context: Context, notice: AppNotice) {
        val current = _notices.value.toMutableList()
        val index = current.indexOfFirst { it.id == notice.id }
        if (index >= 0) {
            current[index] = notice
        } else {
            current.add(notice)
        }
        saveNotices(context, current)
    }

    fun updateNoticeBudget(context: Context, noticeId: String, newBudget: Double) {
        val currentList = _notices.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == noticeId }
        if (index != -1) {
            val updated = currentList[index].copy(budget = newBudget.coerceAtLeast(0.0))
            currentList[index] = updated
            saveNotices(context, currentList)
        }
    }

    fun calculateExpirationMillis(startTime: Long, durationValue: Int, durationUnit: String): Long {
        val count = durationValue.coerceAtLeast(1)
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = startTime }
        when (durationUnit.lowercase(java.util.Locale.ROOT)) {
            "hour", "hours", "hora", "horas" -> cal.add(java.util.Calendar.HOUR_OF_DAY, count)
            "day", "days", "dia", "dias", "día", "días" -> cal.add(java.util.Calendar.DAY_OF_YEAR, count)
            "week", "weeks", "semana", "semanas" -> cal.add(java.util.Calendar.WEEK_OF_YEAR, count)
            "month", "months", "mes", "meses" -> cal.add(java.util.Calendar.MONTH, count)
            "year", "years", "año", "años", "ano", "anos" -> cal.add(java.util.Calendar.YEAR, count)
            else -> cal.add(java.util.Calendar.DAY_OF_YEAR, count)
        }
        return cal.timeInMillis
    }

    /**
     * Registra un nuevo anuncio de patrocinador como pendiente de moderación
     * y lo sincroniza con Firestore para que el administrador pueda verlo.
     */
    suspend fun submitPendingSponsorNotice(context: Context, notice: AppNotice, expectedCost: Long) {
        EconomyServiceClient.call("SPONSOR", mapOf("expectedCost" to expectedCost, "notice" to mapOf(
            "title" to notice.title, "content" to notice.content, "videoUrl" to notice.videoUrl,
            "expandedImageUrl" to notice.expandedImageUrl, "externalUrl" to notice.externalUrl,
            "titleColor" to notice.titleColor, "durationValue" to notice.durationValue,
            "durationUnit" to notice.durationUnit)), notice.id)
        val appContext = context.applicationContext
        val current = _notices.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.id == notice.id }
        val pendingNotice = notice.copy(
            isApproved = false,
            isEnabled = false,
            tag = "PUBLICIDAD",
            approvedAtMillis = 0L,
            expiresAtMillis = 0L
        )
        if (existingIndex >= 0) {
            current[existingIndex] = pendingNotice
        } else {
            current.add(pendingNotice)
        }
        _notices.value = current
        saveNoticesToPrefs(appContext, current)
        try {
            val prefs = appContext.getSharedPreferences("sponsor_ads_prefs", Context.MODE_PRIVATE)
            val jsonStr = prefs.getString("pending_ads", "[]") ?: "[]"
            val array = org.json.JSONArray(jsonStr)
            val obj = org.json.JSONObject().apply {
                put("id", pendingNotice.id)
                put("title", pendingNotice.title)
                put("content", pendingNotice.content)
                put("videoUrl", pendingNotice.videoUrl)
                put("expandedImageUrl", pendingNotice.expandedImageUrl)
                put("externalUrl", pendingNotice.externalUrl)
                put("tag", pendingNotice.tag)
                put("budget", pendingNotice.budget)
                put("budgetUnit", pendingNotice.budgetUnit)
                put("durationValue", pendingNotice.durationValue)
                put("durationUnit", pendingNotice.durationUnit)
                put("isApproved", false)
                put("isEnabled", false)
                put("sponsorEmail", pendingNotice.sponsorEmail)
            }
            array.put(obj)
            prefs.edit().putString("pending_ads", array.toString()).apply()
        } catch (_: Exception) {}
    }

    /**
     * Aprueba un anuncio de patrocinador:
     * Calcula su fecha de expiración según la duración especificada,
     * lo marca como aprobado y habilitado, y lo sincroniza inmediatamente en Firestore.
     */
    fun approveSponsorNotice(context: Context, noticeId: String) {
        val appContext = context.applicationContext
        val current = _notices.value.toMutableList()
        val index = current.indexOfFirst { it.id == noticeId }
        val now = System.currentTimeMillis()
        var expiresAt = 0L

        if (index >= 0) {
            val cur = current[index]
            expiresAt = calculateExpirationMillis(now, cur.durationValue, cur.durationUnit)
            val approved = cur.copy(
                isApproved = true,
                isEnabled = true,
                approvedAtMillis = now,
                expiresAtMillis = expiresAt
            )
            current[index] = approved
            _notices.value = current
            saveNoticesToPrefs(appContext, current)
            pushNoticesToFirestore(appContext, current)
        } else {
            // Si no estaba en la lista local, intentar sincronizar primero
            syncFromCloud(appContext)
        }

        // Actualizar en la colección pending_sponsor_ads
        try {
            val db = FirebaseFirestore.getInstance()
            val updateMap = hashMapOf<String, Any>(
                "isApproved" to true,
                "isEnabled" to true,
                "approvedAt" to now,
                "approvedAtMillis" to now,
                "expiresAtMillis" to if (expiresAt > 0L) expiresAt else (now + 24 * 3600 * 1000L)
            )
            db.collection("pending_sponsor_ads").document(noticeId)
                .set(updateMap, SetOptions.merge())
        } catch (_: Exception) {}

        // Actualizar también en el almacenamiento local del patrocinador si está en este dispositivo
        try {
            val prefs = appContext.getSharedPreferences("sponsor_ads_prefs", Context.MODE_PRIVATE)
            val jsonStr = prefs.getString("pending_ads", null)
            if (!jsonStr.isNullOrBlank()) {
                val array = org.json.JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    if (obj.optString("id") == noticeId) {
                        obj.put("isApproved", true)
                        obj.put("isEnabled", true)
                        obj.put("approvedAtMillis", now)
                        obj.put("expiresAtMillis", expiresAt)
                    }
                }
                prefs.edit().putString("pending_ads", array.toString()).apply()
            }
        } catch (_: Exception) {}
    }

    /**
     * Rechaza un anuncio de patrocinador eliminándolo de los anuncios del sistema.
     */
    fun rejectSponsorNotice(context: Context, noticeId: String) {
        val appContext = context.applicationContext
        val current = _notices.value.filter { it.id != noticeId }
        _notices.value = current
        saveNoticesToPrefs(appContext, current)
        pushNoticesToFirestore(appContext, current)

        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("pending_sponsor_ads").document(noticeId).delete()
        } catch (_: Exception) {}

        try {
            val prefs = appContext.getSharedPreferences("sponsor_ads_prefs", Context.MODE_PRIVATE)
            val jsonStr = prefs.getString("pending_ads", null)
            if (!jsonStr.isNullOrBlank()) {
                val array = org.json.JSONArray(jsonStr)
                val newArray = org.json.JSONArray()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    if (obj.optString("id") != noticeId) {
                        newArray.put(obj)
                    }
                }
                prefs.edit().putString("pending_ads", newArray.toString()).apply()
            }
        } catch (_: Exception) {}
    }

    /**
     * Sincroniza anuncios de patrocinadores pendientes desde colecciones alternativas
     * (pending_sponsor_ads y SharedPreferences locales).
     */
    suspend fun syncPendingSponsors(context: Context) = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        val listToAdd = mutableListOf<AppNotice>()

        // 1. Cargar desde SharedPreferences locales de patrocinador
        try {
            val prefs = appContext.getSharedPreferences("sponsor_ads_prefs", Context.MODE_PRIVATE)
            val jsonStr = prefs.getString("pending_ads", null)
            if (!jsonStr.isNullOrBlank()) {
                val array = org.json.JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    listToAdd.add(
                        AppNotice(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            title = obj.optString("title", "Publicidad"),
                            content = obj.optString("content", ""),
                            expandedImageUrl = obj.optString("expandedImageUrl", ""),
                            externalUrl = obj.optString("externalUrl", ""),
                            tag = obj.optString("tag", "Publicidad"),
                            budget = obj.optDouble("budget", 10.0),
                            budgetUnit = obj.optString("budgetUnit", "day"),
                            durationValue = obj.optInt("durationValue", 1),
                            durationUnit = obj.optString("durationUnit", obj.optString("budgetUnit", "day")),
                            approvedAtMillis = obj.optLong("approvedAtMillis", 0L),
                            expiresAtMillis = obj.optLong("expiresAtMillis", 0L),
                            isApproved = obj.optBoolean("isApproved", false),
                            isEnabled = obj.optBoolean("isEnabled", false),
                            sponsorEmail = obj.optString("sponsorEmail", "")
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        // 2. Cargar desde Firestore pending_sponsor_ads
        try {
            val db = FirebaseFirestore.getInstance()
            val pending = db.collection("pending_sponsor_ads")
            val uid = com.example.util.AuthManager.getAuth()?.currentUser?.uid ?: return@withContext
            val snap = (if (com.example.util.AuthManager.isCurrentUserAdmin()) pending
                else pending.whereEqualTo("userId", uid)).get().await()
            for (doc in snap.documents) {
                val id = doc.getString("id") ?: doc.id
                val title = doc.getString("title") ?: "Publicidad"
                val content = doc.getString("content") ?: ""
                val videoUrl = doc.getString("videoUrl") ?: ""
                val expandedImageUrl = doc.getString("expandedImageUrl") ?: ""
                val externalUrl = doc.getString("externalUrl") ?: ""
                val tag = doc.getString("tag") ?: "Publicidad"
                val budget = doc.getDouble("budget") ?: 10.0
                val budgetUnit = doc.getString("budgetUnit") ?: "day"
                val durationValue = doc.getLong("durationValue")?.toInt() ?: 1
                val durationUnit = doc.getString("durationUnit") ?: budgetUnit
                val approvedAtMillis = doc.getLong("approvedAtMillis") ?: 0L
                val expiresAtMillis = doc.getLong("expiresAtMillis") ?: 0L
                val isApproved = doc.getBoolean("isApproved") ?: false
                val isEnabled = doc.getBoolean("isEnabled") ?: false
                val sponsorEmail = doc.getString("sponsorEmail") ?: ""

                listToAdd.add(
                    AppNotice(
                        id = id,
                        title = title,
                        content = content,
                        videoUrl = videoUrl,
                        expandedImageUrl = expandedImageUrl,
                        externalUrl = externalUrl,
                        tag = tag,
                        budget = budget,
                        budgetUnit = budgetUnit,
                        durationValue = durationValue,
                        durationUnit = durationUnit,
                        approvedAtMillis = approvedAtMillis,
                        expiresAtMillis = expiresAtMillis,
                        isApproved = isApproved,
                        isEnabled = isEnabled,
                        sponsorEmail = sponsorEmail
                    )
                )
            }
        } catch (_: Exception) {}

        if (listToAdd.isNotEmpty()) {
            val current = _notices.value.toMutableList()
            var changed = false
            for (pending in listToAdd) {
                val idx = current.indexOfFirst { it.id == pending.id }
                if (idx == -1) {
                    current.add(pending)
                    changed = true
                }
            }
            if (changed) {
                _notices.value = current
                saveNoticesToPrefs(appContext, current)
                pushNoticesToFirestore(appContext, current)
                Log.d(TAG, "Sincronizados ${listToAdd.size} patrocinios en AppNoticeManager")
            }
        }
    }
}

