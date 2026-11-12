package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.example.util.appTr
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
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
    val sponsorEmail: String = "", // email of the sponsor who published it
    /**
     * Audience language scope for sponsored notices. `ALL` is the default and
     * keeps legacy notices visible. A single language is intentionally also a
     * general publication; `BOTH` is the opt-in, language-targeted campaign.
     */
    val languageScope: String = "ALL"
)

private fun sponsorStateRank(notice: AppNotice): Int = when {
    notice.isApproved && notice.isEnabled -> 3
    notice.isApproved -> 2
    notice.isEnabled -> 1
    else -> 0
}

/** Never let a late cache callback erase a feed already confirmed by the server. */
internal fun shouldApplyNoticeSnapshot(fromCache: Boolean, hasServerSnapshot: Boolean): Boolean =
    !fromCache || !hasServerSnapshot

/**
 * Reconcile the private sponsor mirror without allowing a stale pending row to
 * replace an already published notice. The previous append-only merge kept a
 * locally cached ad disabled after an administrator approved it.
 */
internal fun mergeSponsorNotices(
    current: List<AppNotice>,
    incoming: List<AppNotice>
): Pair<List<AppNotice>, Boolean> {
    if (incoming.isEmpty()) return current to false
    val merged = current.toMutableList()
    var changed = false

    incoming.forEach { candidate ->
        if (candidate.id.isBlank()) return@forEach
        val index = merged.indexOfFirst { it.id == candidate.id }
        if (index < 0) {
            merged += candidate
            changed = true
            return@forEach
        }

        val existing = merged[index]
        val candidateRank = sponsorStateRank(candidate)
        val existingRank = sponsorStateRank(existing)
        val candidateIsNewer = candidate.approvedAtMillis > existing.approvedAtMillis ||
            candidate.expiresAtMillis > existing.expiresAtMillis
        val shouldReplace = candidateRank > existingRank ||
            (candidateRank == existingRank && candidateRank >= 2 && candidateIsNewer)
        if (shouldReplace && candidate != existing) {
            // Public registry entries intentionally omit sponsorEmail; retain
            // a locally known value for the sponsor's own status panel.
            merged[index] = candidate.copy(
                videoUrl = candidate.videoUrl.ifBlank { existing.videoUrl },
                expandedImageUrl = candidate.expandedImageUrl.ifBlank { existing.expandedImageUrl },
                externalUrl = candidate.externalUrl.ifBlank { existing.externalUrl },
                titleColor = candidate.titleColor.ifBlank { existing.titleColor },
                contentColor = candidate.contentColor.ifBlank { existing.contentColor },
                sponsorEmail = candidate.sponsorEmail.ifBlank { existing.sponsorEmail },
                languageScope = candidate.languageScope
            )
            changed = true
        }
    }
    return merged to changed
}

internal fun mergePublicSponsorNotices(
    base: List<AppNotice>,
    previousPublic: List<AppNotice>,
    currentPublic: List<AppNotice>
): List<AppNotice> {
    val publicIds = (previousPublic.asSequence() + currentPublic.asSequence())
        .mapTo(mutableSetOf()) { it.id }
    return base.filterNot { it.id in publicIds } + currentPublic
}

object AppNoticeManager {
    private const val TAG = "AppNoticeManager"
    private const val PREFS_NAME = "wild_rift_app_notices_prefs"
    private const val KEY_NOTICES_JSON = "notices_json_list"
    private const val KEY_INTERVAL_VALUE = "streamer_interval_value"
    private const val KEY_INTERVAL_UNIT = "streamer_interval_unit"

    private const val FIRESTORE_COLLECTION = "system_config"
    private const val FIRESTORE_DOC_NOTICES = "app_notices"

    private var firestoreListener: ListenerRegistration? = null
    private var publicSponsorListener: ListenerRegistration? = null
    private var publicSponsorNotices: List<AppNotice> = emptyList()
    private var hasServerPublicSnapshot = false
    private var hasServerRegistrySnapshot = false
    private val mediaScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val preloadedImages = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()
    private var authStateListener: FirebaseAuth.AuthStateListener? = null
    private var isAuthenticatingAnonymously = false

    /** Remove an advertisement, its private pending record and its queue row atomically. */
    suspend fun deleteSponsorNotice(context: Context, id: String) = withContext(Dispatchers.IO) {
        require(id.matches(Regex("[a-zA-Z0-9_-]{8,80}")))
        val auth = com.example.util.AuthManager.getAuth()?.currentUser ?: error(appTr("Inicia sesión"))
        check(!com.example.util.AuthManager.isGuestOrUnauthenticated(auth)) { appTr("Inicia sesión") }
        // The panel can be opened with a token whose admin claim is stale. Read
        // a fresh token here so the delete path uses the same authority as the
        // Firestore rules, even when the claim was granted while the panel was open.
        val token = auth.getIdToken(true).await()
        val admin = token.claims["admin"] == true && token.claims["email_verified"] == true
        val db = FirebaseFirestore.getInstance()
        val adRef = db.collection("pending_sponsor_ads").document(id)
        val publicRef = db.collection("app_notices").document(id)
        val registryRef = db.collection(FIRESTORE_COLLECTION).document(FIRESTORE_DOC_NOTICES)
        val initialAd = adRef.get(Source.SERVER).await()
        val ownerUid = initialAd.getString("userId")?.takeIf { it.isNotBlank() }
        check(initialAd.exists() || admin) { appTr("El anuncio ya no está disponible") }
        check(admin || ownerUid == auth.uid) { appTr("No tienes permisos para eliminar este anuncio") }

        // Newer rows keep queuedRequestId. Older builds did not, so also find
        // matching sponsor commands by owner (or by id for an administrator).
        val fallbackQuery = if (admin) {
            // The rules authorize administrator queries when the action filter
            // is explicit; filter the publication id locally to keep old rows
            // compatible without requiring a new composite index.
            db.collection("economy_requests").whereEqualTo("payload.action", "SPONSOR")
        } else {
            db.collection("economy_requests").whereEqualTo("userId", ownerUid ?: auth.uid)
        }
        val fallbackQueueRefs = fallbackQuery.get(Source.SERVER).await().documents
            .filter { row ->
                val payload = row.get("payload") as? Map<*, *>
                val notice = payload?.get("notice") as? Map<*, *>
                val payloadId = payload?.get("id")?.toString()
                val noticeId = notice?.get("id")?.toString()
                payload?.get("action") == "SPONSOR" && (payloadId == id || noticeId == id) &&
                    row.getString("status") in setOf("PENDING", "PROCESSING", "REVIEW")
            }.map { it.reference }

        val removed = db.runTransaction { tx ->
            val ad = tx.get(adRef)
            if (ad.exists()) {
                check(admin || ad.getString("userId") == auth.uid) { appTr("No tienes permisos para eliminar este anuncio") }
            } else if (!admin) {
                return@runTransaction false
            }
            val queueId = ad.getString("queuedRequestId")
            val queueRefs: List<com.google.firebase.firestore.DocumentReference> =
                (fallbackQueueRefs + (queueId?.let {
                    listOf(db.collection("economy_requests").document(it))
                } ?: emptyList())).distinctBy { it.path }
            val queues = queueRefs.map { tx.get(it) }
            val registry = if (admin) tx.get(registryRef) else null
            tx.delete(publicRef)
            if (ad.exists()) tx.delete(adRef)
            queues.filter { it.exists() }.forEach { tx.delete(it.reference) }
            if (admin && registry != null) {
                @Suppress("UNCHECKED_CAST")
                val notices = (registry.get("notices") as? List<Map<String, Any>>).orEmpty()
                    .filterNot { it["id"] == id }
                tx.set(registryRef, mapOf("notices" to notices), SetOptions.merge())
            }
            true
        }.await()
        check(removed) { appTr("El anuncio ya no está disponible") }
        val updated = _notices.value.filterNot { it.id == id }
        if (updated != _notices.value) {
            _notices.value = updated
            saveNoticesToPrefs(context.applicationContext, updated)
        }
        true
    }

    /**
     * Returns ids that are known by the server. A null result means that the server
     * could not be consulted, so callers must retain local data and avoid declaring it lost.
     */
    suspend fun authoritativePendingSponsorIds(context: Context, candidateIds: Set<String>): Set<String>? =
        withContext(Dispatchers.IO) {
            if (candidateIds.isEmpty()) return@withContext emptySet()
            val uid = com.example.util.AuthManager.getAuth()?.currentUser?.uid
                ?: return@withContext emptySet()
            runCatching {
                val db = FirebaseFirestore.getInstance()
                val ads = db.collection("pending_sponsor_ads")
                    .whereEqualTo("userId", uid)
                    .get(com.google.firebase.firestore.Source.SERVER).await()
                val requests = db.collection("economy_requests")
                    .whereEqualTo("userId", uid)
                    .get(com.google.firebase.firestore.Source.SERVER).await()
                val adRows = ads.documents.flatMap { document ->
                    setOf(document.id, document.getString("id").orEmpty())
                        .filter { it.isNotBlank() }
                        .map { it to document.data.orEmpty() }
                }
                val commandRows = requests.documents.flatMap { document ->
                    val payload = document.get("payload") as? Map<*, *> ?: return@flatMap emptyList()
                    if (payload["action"]?.toString() != "SPONSOR") return@flatMap emptyList()
                    val notice = payload["notice"] as? Map<*, *>
                    setOf(document.getString("operationId").orEmpty(), document.id,
                        payload["id"]?.toString().orEmpty(), notice?.get("id")?.toString().orEmpty())
                        .filter { it.isNotBlank() }
                        .map { it to document.data.orEmpty() }
                }
                pendingSponsorIdsFromServer(false, false, adRows, commandRows).intersect(candidateIds)
            }.getOrNull()
        }

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
                    if (snapshot != null && snapshot.exists() &&
                        shouldApplyNoticeSnapshot(true, hasServerRegistrySnapshot)) {
                        processFirestoreSnapshot(appContext, snapshot)
                    }
                }
                .addOnFailureListener {
                    // Si aún no está en caché de Firestore, se usará la caché local ya cargada
                }
            db.collection("app_notices").get(Source.CACHE)
                .addOnSuccessListener {
                    if (shouldApplyNoticeSnapshot(true, hasServerPublicSnapshot))
                        processPublicSponsorSnapshot(appContext, it)
                }
                .addOnFailureListener { }
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
                    attachPublicSponsorListener(context, force = true)
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
        attachFirestoreListener(appContext, force = false)
        attachPublicSponsorListener(appContext, force = false)

        // En segundo plano sin bloquear la UI ni la carga inicial
        val auth = try { FirebaseAuth.getInstance() } catch (_: Exception) { null }
        if (auth?.currentUser == null) {
            com.example.util.GuestAuthHelper.ensureAuth {
                attachFirestoreListener(appContext, force = true)
                attachPublicSponsorListener(appContext, force = true)
            }
        }
    }

    fun syncFromCloud(context: Context, onComplete: ((Boolean) -> Unit)? = null) {
        val appContext = context.applicationContext
        // Inmediato desde caché y en paralelo desde la nube
        attachPublicSponsorListener(appContext, force = false)
        // The public listener is the single source of sponsor publications.
        // A second one-shot query can finish out of order and clear a newer ad.
        executeCloudFetch(appContext, onComplete, Source.SERVER)
    }

    private fun executeCloudFetch(
        appContext: Context,
        onComplete: ((Boolean) -> Unit)?,
        source: Source = Source.DEFAULT
    ) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection(FIRESTORE_COLLECTION).document(FIRESTORE_DOC_NOTICES)
                .get(source)
                .addOnSuccessListener { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        if (shouldApplyNoticeSnapshot(snapshot.metadata.isFromCache, hasServerRegistrySnapshot)) {
                            if (!snapshot.metadata.isFromCache) hasServerRegistrySnapshot = true
                            processFirestoreSnapshot(appContext, snapshot)
                        }
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

    private fun mergePublicSponsors(base: List<AppNotice>): List<AppNotice> {
        return mergePublicSponsorNotices(base, publicSponsorNotices, publicSponsorNotices)
    }

    private fun processPublicSponsorSnapshot(
        context: Context,
        snapshot: com.google.firebase.firestore.QuerySnapshot
    ) {
        val next = snapshot.documents.mapNotNull { document ->
            val data = document.data ?: return@mapNotNull null
            if (data["isApproved"] != true || data["isEnabled"] != true) return@mapNotNull null
            SponsorReviewPolicy.notice(document.id, data)
        }
        applyPublicSponsors(context, next)
    }

    /** Show an ad immediately after its public document is verified from the server. */
    fun acceptVerifiedSponsor(context: Context, id: String, data: Map<String, Any>) {
        if (!SponsorReviewPolicy.visibleOnHome(data, System.currentTimeMillis())) return
        hasServerPublicSnapshot = true
        applyPublicSponsors(context, publicSponsorNotices.filterNot { it.id == id } +
            SponsorReviewPolicy.notice(id, data))
    }

    private fun applyPublicSponsors(context: Context, next: List<AppNotice>) {
        val previous = publicSponsorNotices
        publicSponsorNotices = next
        val combined = mergePublicSponsorNotices(_notices.value, previous, publicSponsorNotices)
        if (combined != _notices.value) {
            _notices.value = combined
            saveNoticesToPrefs(context, combined)
            preloadMedia(context, publicSponsorNotices)
        }
        Log.d(TAG, "Sincronizados ${publicSponsorNotices.size} anuncios patrocinados públicos")
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
                            sponsorEmail = map["sponsorEmail"]?.toString() ?: "",
                            languageScope = SponsorReviewPolicy.normalizeLanguageScope(map["languageScope"]?.toString())
                        )
                    )
                }
                // Public sponsor documents are authoritative. A stale shared
                // registry entry must not resurrect an ad deleted by its owner.
                val publicIds = publicSponsorNotices.mapTo(mutableSetOf()) { it.id }
                val combined = mergePublicSponsors(parsedNotices.filterNot {
                    it.tag.equals("PUBLICIDAD", ignoreCase = true) && it.id !in publicIds
                })
                if (combined != _notices.value) {
                    _notices.value = combined
                    saveNoticesToPrefs(context, combined)
                    preloadMedia(context, combined)
                }
                Log.d(TAG, "Sincronizados exitosamente ${combined.size} anuncios desde Firestore para todos los dispositivos.")
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
                        if (shouldApplyNoticeSnapshot(snapshot.metadata.isFromCache, hasServerRegistrySnapshot)) {
                            if (!snapshot.metadata.isFromCache) hasServerRegistrySnapshot = true
                            processFirestoreSnapshot(context, snapshot)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo iniciar listener de Firestore: ${e.message}")
            firestoreListener = null
        }
    }

    private fun attachPublicSponsorListener(context: Context, force: Boolean = false) {
        if (force) {
            try {
                publicSponsorListener?.remove()
            } catch (_: Exception) { }
            publicSponsorListener = null
        }
        if (publicSponsorListener != null) return
        try {
            publicSponsorListener = FirebaseFirestore.getInstance().collection("app_notices")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Error escuchando anuncios patrocinados públicos: ${error.message}")
                        try {
                            publicSponsorListener?.remove()
                        } catch (_: Exception) { }
                        publicSponsorListener = null
                        return@addSnapshotListener
                    }
                    if (snapshot != null &&
                        shouldApplyNoticeSnapshot(snapshot.metadata.isFromCache, hasServerPublicSnapshot)) {
                        if (!snapshot.metadata.isFromCache) hasServerPublicSnapshot = true
                        processPublicSponsorSnapshot(context, snapshot)
                    }
                }
        } catch (error: Exception) {
            Log.w(TAG, "No se pudo iniciar el listener de patrocinio público: ${error.message}")
            publicSponsorListener = null
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
                    put("languageScope", n.languageScope)
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
                            sponsorEmail = obj.optString("sponsorEmail", ""),
                            languageScope = SponsorReviewPolicy.normalizeLanguageScope(obj.optString("languageScope", "ALL"))
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

    /** Preload only visible image thumbnails; videos and expanded media load on demand. */
    fun preloadMedia(context: Context, list: List<AppNotice>) {
        val appContext = context.applicationContext
        list.asSequence().filter { it.isEnabled && it.isApproved }
            .map { it.videoUrl.trim() }
            .filter { it.startsWith("https://", ignoreCase = true) }
            .filterNot { com.example.ui.components.NoticeMediaUtils.isVideo(appContext, it) }
            .distinct().take(2).forEach { url ->
                if (!preloadedImages.add(url)) return@forEach
                mediaScope.launch {
                    try {
                        coil.Coil.imageLoader(appContext).enqueue(
                            coil.request.ImageRequest.Builder(appContext)
                                .data(url)
                                .size(512, 512)
                                .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                .build()
                        )
                    } catch (error: Exception) {
                        preloadedImages.remove(url)
                        Log.w(TAG, "Error en precarga de imagen: ${error.message}")
                    }
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
                    "sponsorEmail" to n.sponsorEmail,
                    "languageScope" to SponsorReviewPolicy.normalizeLanguageScope(n.languageScope)
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
            "durationUnit" to notice.durationUnit,
            "languageScope" to SponsorReviewPolicy.normalizeLanguageScope(notice.languageScope))), notice.id)
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
                put("languageScope", SponsorReviewPolicy.normalizeLanguageScope(pendingNotice.languageScope))
            }
            array.put(obj)
            prefs.edit().putString("pending_ads", array.toString()).apply()
        } catch (_: Exception) {}
    }

    /**
     * Sincroniza anuncios de patrocinadores pendientes desde colecciones alternativas
     * (pending_sponsor_ads y SharedPreferences locales).
     */
    suspend fun syncPendingSponsors(context: Context) = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        val listToAdd = mutableListOf<AppNotice>()
        val rejectedIds = mutableSetOf<String>()

        // 1. Cargar desde SharedPreferences locales de patrocinador
        try {
            val prefs = appContext.getSharedPreferences("sponsor_ads_prefs", Context.MODE_PRIVATE)
            val jsonStr = prefs.getString("pending_ads", null)
            if (!jsonStr.isNullOrBlank()) {
                val array = org.json.JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    if (obj.optString("status").uppercase() in setOf("REJECTED", "REFUNDED") || obj.optLong("refundedAtMillis", 0L) > 0L) continue
                    listToAdd.add(
                        AppNotice(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            title = obj.optString("title", "Publicidad"),
                            content = obj.optString("content", ""),
                            videoUrl = obj.optString("videoUrl", ""),
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
                            sponsorEmail = obj.optString("sponsorEmail", ""),
                            languageScope = SponsorReviewPolicy.normalizeLanguageScope(obj.optString("languageScope", "ALL"))
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
                if (doc.getString("status") in setOf("REJECTED", "REFUNDED") || (doc.getLong("refundedAtMillis") ?: 0L) > 0L) {
                    rejectedIds += id
                    continue
                }
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
                val languageScope = SponsorReviewPolicy.normalizeLanguageScope(doc.getString("languageScope"))

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
                        sponsorEmail = sponsorEmail,
                        languageScope = languageScope
                    )
                )
            }
        } catch (_: Exception) {}

        if (rejectedIds.isNotEmpty()) {
            val current = _notices.value.filterNot { it.id in rejectedIds }
            if (current.size != _notices.value.size) {
                _notices.value = current
                saveNoticesToPrefs(appContext, current)
            }
            runCatching {
                val prefs = appContext.getSharedPreferences("sponsor_ads_prefs", Context.MODE_PRIVATE)
                val raw = prefs.getString("pending_ads", null)
                if (!raw.isNullOrBlank()) {
                    val source = org.json.JSONArray(raw)
                    val clean = org.json.JSONArray()
                    for (i in 0 until source.length()) {
                        val obj = source.getJSONObject(i)
                        if (obj.optString("id") !in rejectedIds) clean.put(obj)
                    }
                    prefs.edit().putString("pending_ads", clean.toString()).apply()
                }
            }
        }

        if (listToAdd.isNotEmpty()) {
            val (current, changed) = mergeSponsorNotices(_notices.value, listToAdd)
            if (changed) {
                _notices.value = current
                saveNoticesToPrefs(appContext, current)
                // pending_sponsor_ads is already the source of truth for this
                // private mirror. Writing the merged cache back to the public
                // registry could let a stale device erase a freshly approved
                // announcement for every user.
                preloadMedia(appContext, current)
                Log.d(TAG, "Sincronizados ${listToAdd.size} patrocinios en AppNoticeManager")
            }
        }
    }
}

