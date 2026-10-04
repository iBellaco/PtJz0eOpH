package com.example.data.local

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.google.firebase.firestore.FieldValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.util.UUID

@Serializable
data class ItemBuildEntry(
    val itemName: String = "",
    val description: String = ""
)

@Serializable
data class RuneBuildEntry(
    val runeName: String = "",
    val iconUrl: String = "",
    val description: String = ""
)

@Serializable
data class SpellBuildEntry(
    val spellName: String = "",
    val iconUrl: String = "",
    val description: String = ""
)

@Serializable
data class CustomChampionBuildRecord(
    val id: String = UUID.randomUUID().toString(),
    val championId: String = "",
    val championName: String = "",
    val buildTitle: String = "",
    val coachAdvice: String = "",
    val role: String = "",
    val coreItems: List<String> = emptyList(),
    val situationalItems: List<String> = emptyList(),
    val runes: String = "",
    val spells: List<String> = emptyList(),
    val coreItemsWithDesc: List<ItemBuildEntry> = emptyList(),
    val situationalItemsWithDesc: List<ItemBuildEntry> = emptyList(),
    val coreRunes: List<RuneBuildEntry> = emptyList(),
    val situationalRunes: List<RuneBuildEntry> = emptyList(),
    val coreSpells: List<SpellBuildEntry> = emptyList(),
    val situationalSpells: List<SpellBuildEntry> = emptyList(),
    val bootsT2Item: ItemBuildEntry? = null,
    val bootsT3Item: ItemBuildEntry? = null,
    val situationalBootsT2Item: ItemBuildEntry? = null,
    val situationalBootsT3Item: ItemBuildEntry? = null,
    val comboVideoUri: String? = null,
    val gameplayVideoUri: String? = null,
    val creatorName: String = "Creador Oficial",
    val creatorAvatarId: String? = null,
    val creatorRankBorder: String = "NONE",
    val creatorIsAdmin: Boolean = false,
    val creatorUserId: String = "",
    val ratingSum: Double = 0.0,
    val voteCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Gestor centralizado de builds creadas por creadores con sincronización en la nube en tiempo real.
 * Permite que las builds sean multi-dispositivo y se visualicen instantáneamente en cualquier celular.
 */
object CustomChampionBuildsManager {
    private const val TAG = "CreatorBuildsManager"
    private const val PREFS_NAME = "wr_custom_champion_builds_prefs"
    private const val KEY_BUILDS_JSON = "custom_champion_builds_json"
    private const val KEY_DELETED_BUILDS = "deleted_build_ids"
    private val deletionMutex = Mutex()
    @Volatile private var defaultBuildsCache: List<CustomChampionBuildRecord>? = null
    private val deletedBuildIds = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()
    private const val KEY_USER_VOTES = "user_voted_builds_map"

    private const val REMOTE_CONFIG_COLLECTION = "system_config"
    private const val REMOTE_DOC_CREATOR_BUILDS = "creator_builds"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val _customBuilds = MutableStateFlow<List<CustomChampionBuildRecord>>(emptyList())
    val customBuilds: StateFlow<List<CustomChampionBuildRecord>> = _customBuilds.asStateFlow()

    private val _userVotedBuilds = MutableStateFlow<Map<String, Int>>(emptyMap())
    val userVotedBuilds: StateFlow<Map<String, Int>> = _userVotedBuilds.asStateFlow()

    private var firestoreListener: ListenerRegistration? = null
    private var authStateListener: FirebaseAuth.AuthStateListener? = null
    private var initialized = false

    fun init(context: Context) {
        val appContext = context.applicationContext
        if (initialized) return
        initialized = true

        // 1. Cargar caché local de inmediato (garantiza visualización instantánea a 0ms de latencia)
        loadFromLocalStorage(appContext)

        // 2. Intentar leer caché en memoria del servicio en la nube
        fetchFromCloudCache(appContext)

        // 3. Monitorear cambios de sesión/autenticación para mantener listener activo
        setupAuthStateListener(appContext)

        // 4. Conectar y sincronizar garantizando acceso inmediato multi-dispositivo
        ensureAuthAndSync(appContext)
    }

    internal fun isBundledOfficialBuild(record: CustomChampionBuildRecord): Boolean {
        return record.creatorName.contains("Coach IA", ignoreCase = true) ||
            (record.creatorUserId.isBlank() && record.creatorName == "Coach (Criterio Táctico)")
    }

    private fun officialBuildKey(record: CustomChampionBuildRecord): String {
        return "${record.championId.trim().lowercase()}|${record.role.trim().lowercase()}"
    }

    /**
     * Las builds oficiales incluidas en la app son la fuente canónica.
     * Al cargar cachés anteriores, sustituimos cualquier versión oficial antigua por la
     * versión actual y conservamos intactas las builds creadas por usuarios.
     */
    private fun reconcileWithOfficialDefaults(
        context: Context,
        incoming: List<CustomChampionBuildRecord>
    ): List<CustomChampionBuildRecord> {
        val defaults = getDefaultBuilds(context)
            .distinctBy { officialBuildKey(it) }

        val userCreated = incoming
            .filterNot { isBundledOfficialBuild(it) }
            .distinctBy { it.id }

        return (defaults + userCreated).filterNot { it.id in deletedBuildIds }
    }

    private fun loadFromLocalStorage(context: Context) {
        val defaults = getDefaultBuilds(context)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        deletedBuildIds.addAll(prefs.getStringSet(KEY_DELETED_BUILDS, emptySet()).orEmpty())
        val rawJson = prefs.getString(KEY_BUILDS_JSON, null)

        if (!rawJson.isNullOrBlank()) {
            try {
                val cached = json.decodeFromString<List<CustomChampionBuildRecord>>(rawJson)
                run {
                    val reconciled = reconcileWithOfficialDefaults(context, cached)
                    _customBuilds.value = reconciled
                    saveToLocalStorage(context, reconciled)
                    return
                }
            } catch (_: Exception) {}
        }

        _customBuilds.value = defaults.filterNot { it.id in deletedBuildIds }
        saveToLocalStorage(context, _customBuilds.value)
    }

    private fun fetchFromCloudCache(appContext: Context) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection(REMOTE_CONFIG_COLLECTION).document(REMOTE_DOC_CREATOR_BUILDS)
                .get(com.google.firebase.firestore.Source.CACHE)
                .addOnSuccessListener { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        processCloudSnapshot(appContext, snapshot)
                    }
                }
                .addOnFailureListener {
                    // Si aún no está en caché, se mantiene la versión local actual
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
                    attachCloudListener(context, force = true)
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
        fetchFromCloudCache(appContext)
        attachCloudListener(appContext, force = false)
        executeCloudFetch(appContext, null)

        val auth = try { FirebaseAuth.getInstance() } catch (_: Exception) { null }
        if (auth?.currentUser == null) {
            com.example.util.GuestAuthHelper.ensureAuth {
                attachCloudListener(appContext, force = true)
                executeCloudFetch(appContext, null)
            }
        }
    }

    fun syncFromCloud(context: Context, onComplete: ((Boolean) -> Unit)? = null) {
        val appContext = context.applicationContext
        fetchFromCloudCache(appContext)
        executeCloudFetch(appContext, onComplete)
    }

    private fun executeCloudFetch(appContext: Context, onComplete: ((Boolean) -> Unit)?) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection(REMOTE_CONFIG_COLLECTION).document(REMOTE_DOC_CREATOR_BUILDS)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        processCloudSnapshot(appContext, snapshot)
                        onComplete?.invoke(true)
                    } else {
                        Log.d(TAG, "Documento de builds en la nube no encontrado, sembrando iniciales.")
                        // Si el documento no existe en la nube, inicializarlo con las builds existentes
                        val currentList = _customBuilds.value.ifEmpty { getDefaultBuilds(appContext) }
                        saveToCloud(appContext, currentList)
                        onComplete?.invoke(false)
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error forzando sincronización desde la nube: ${e.message}")
                    onComplete?.invoke(false)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción en executeCloudFetch: ${e.message}")
            onComplete?.invoke(false)
        }
    }

    private fun attachCloudListener(context: Context, force: Boolean = false) {
        val appContext = context.applicationContext
        if (firestoreListener != null && !force) return

        try {
            firestoreListener?.remove()
            firestoreListener = null

            val db = FirebaseFirestore.getInstance()
            firestoreListener = db.collection(REMOTE_CONFIG_COLLECTION).document(REMOTE_DOC_CREATOR_BUILDS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Error en listener de sincronización en la nube: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        processCloudSnapshot(appContext, snapshot)
                    }
                }
            Log.d(TAG, "Listener en tiempo real para builds de creadores conectado correctamente.")
        } catch (e: Exception) {
            Log.e(TAG, "Error conectando listener en tiempo real: ${e.message}")
        }
    }

    private fun processCloudSnapshot(context: Context, snapshot: com.google.firebase.firestore.DocumentSnapshot) {
        CoroutineScope(Dispatchers.IO).launch {
            deletionMutex.withLock { processCloudSnapshotLocked(context, snapshot) }
        }
    }

    private fun processCloudSnapshotLocked(context: Context, snapshot: com.google.firebase.firestore.DocumentSnapshot) {
        try {
            deletedBuildIds.addAll((snapshot.get("deletedBuildIds") as? List<*>)?.filterIsInstance<String>().orEmpty())
            val rawJson = snapshot.getString("builds_json")
            if (!rawJson.isNullOrBlank()) {
                val parsedList = json.decodeFromString<List<CustomChampionBuildRecord>>(rawJson)
                run {
                    val reconciled = reconcileWithOfficialDefaults(context, parsedList)
                    _customBuilds.value = reconciled
                    saveToLocalStorage(context, reconciled)
                    return
                }
            }

            // Fallback: interpretar lista de mapas si se guardó como lista estructurada
            val rawList = snapshot.get("builds") as? List<*>
            if (rawList != null && rawList.isNotEmpty()) {
                val parsedList = mutableListOf<CustomChampionBuildRecord>()
                for (item in rawList) {
                    val map = item as? Map<*, *> ?: continue
                    try {
                        val recordJson = JSONObject(map).toString()
                        val record = json.decodeFromString<CustomChampionBuildRecord>(recordJson)
                        parsedList.add(record)
                    } catch (_: Exception) {}
                }
                if (parsedList.isNotEmpty()) {
                    val reconciled = reconcileWithOfficialDefaults(context, parsedList)
                    _customBuilds.value = reconciled
                    saveToLocalStorage(context, reconciled)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error procesando snapshot de builds en la nube: ${e.message}")
        }
    }

    @Synchronized
    fun getDefaultBuilds(context: Context): List<CustomChampionBuildRecord> {
        defaultBuildsCache?.let { return it }
        try {
            val assetStream = context.assets.open("champions_creator_builds.json")
            val raw = assetStream.bufferedReader().use { it.readText() }
            val parsed = json.decodeFromString<List<CustomChampionBuildRecord>>(raw)
            if (parsed.isNotEmpty()) {
                defaultBuildsCache = parsed
                return parsed
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando builds oficiales desde assets: ${e.message}")
        }
        return emptyList()
    }

    fun addBuild(context: Context, record: CustomChampionBuildRecord) {
        init(context)
        val current = _customBuilds.value.toMutableList()
        current.add(0, record)
        _customBuilds.value = current
        saveToLocalStorage(context, current)
        saveToCloud(context, current)
    }

    fun updateBuild(context: Context, record: CustomChampionBuildRecord) {
        init(context)
        val current = _customBuilds.value.toMutableList()
        val index = current.indexOfFirst { it.id == record.id }
        if (index != -1) {
            current[index] = record
        } else {
            current.add(0, record)
        }
        _customBuilds.value = current
        saveToLocalStorage(context, current)
        saveToCloud(context, current)
    }

    /** Persist before updating the list; decoding and disk writes never block a delete tap. */
    suspend fun deleteBuild(context: Context, id: String): Result<Unit> = withContext(Dispatchers.IO) {
        deletionMutex.withLock {
            runCatching {
                init(context)
                require(id.isNotBlank())
                val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val deleted = deletedBuildIds.toSet() + id
                val current = _customBuilds.value.filterNot { it.id in deleted }
                val encoded = json.encodeToString(current.filterNot(::isBundledOfficialBuild))
                check(prefs.edit().putString(KEY_BUILDS_JSON, encoded)
                    .putStringSet(KEY_DELETED_BUILDS, deleted).commit())
                deletedBuildIds.add(id)
                _customBuilds.value = current
                saveToCloud(context, current)
                Unit
            }
        }
    }

    fun getBuildsForChampion(championId: String): List<CustomChampionBuildRecord> {
        return _customBuilds.value.filter { it.championId.equals(championId, ignoreCase = true) }
    }

    fun rateBuild(context: Context, id: String, stars: Int) {
        init(context)
        val current = _customBuilds.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            val record = current[index]
            val newVoteCount = record.voteCount + 1
            val newRatingSum = record.ratingSum + stars.toDouble()
            current[index] = record.copy(
                voteCount = newVoteCount,
                ratingSum = newRatingSum
            )
            _customBuilds.value = current
            saveToLocalStorage(context, current)
            saveToCloud(context, current)
        }
    }

    private fun saveToLocalStorage(context: Context, list: List<CustomChampionBuildRecord>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        try {
            val encoded = json.encodeToString(list.filterNot(::isBundledOfficialBuild))
            prefs.edit().putString(KEY_BUILDS_JSON, encoded)
                .putStringSet(KEY_DELETED_BUILDS, deletedBuildIds.toSet()).apply()
        } catch (_: Exception) {}
    }

    private fun saveToCloud(context: Context, list: List<CustomChampionBuildRecord>) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val encodedJson = json.encodeToString(list.filterNot(::isBundledOfficialBuild))
                val payload = mapOf(
                    "builds_json" to encodedJson,
                    "updatedAt" to System.currentTimeMillis(),
                    "totalBuilds" to list.size,
                    "deletedBuildIds" to FieldValue.arrayUnion(*deletedBuildIds.toTypedArray())
                )
                val db = FirebaseFirestore.getInstance()
                db.collection(REMOTE_CONFIG_COLLECTION).document(REMOTE_DOC_CREATOR_BUILDS)
                    .set(payload, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d(TAG, "Builds de creadores sincronizadas con éxito en la nube multi-dispositivo (${list.size} builds).")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error sincronizando builds en la nube: ${e.message}")
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Excepción guardando builds en la nube: ${e.message}")
            }
        }
    }
}
