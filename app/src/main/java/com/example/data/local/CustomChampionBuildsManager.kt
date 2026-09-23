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

    private fun loadFromLocalStorage(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rawJson = prefs.getString(KEY_BUILDS_JSON, null)
        if (!rawJson.isNullOrBlank()) {
            try {
                val list = json.decodeFromString<List<CustomChampionBuildRecord>>(rawJson)
                if (list.isNotEmpty()) {
                    _customBuilds.value = list
                    return
                }
            } catch (_: Exception) {}
        }
        val defaults = getDefaultBuilds()
        _customBuilds.value = defaults
        saveToLocalStorage(context, defaults)
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
                        val currentList = _customBuilds.value.ifEmpty { getDefaultBuilds() }
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
        try {
            val rawJson = snapshot.getString("builds_json")
            if (!rawJson.isNullOrBlank()) {
                val parsedList = json.decodeFromString<List<CustomChampionBuildRecord>>(rawJson)
                if (parsedList.isNotEmpty()) {
                    _customBuilds.value = parsedList
                    saveToLocalStorage(context, parsedList)
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
                    _customBuilds.value = parsedList
                    saveToLocalStorage(context, parsedList)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error procesando snapshot de builds en la nube: ${e.message}")
        }
    }

    private fun getDefaultBuilds(): List<CustomChampionBuildRecord> {
        val dummyItems = listOf(
            ItemBuildEntry("La Sanguinaria", "Gran curación en peleas prolongadas."),
            ItemBuildEntry("Ángel custodio", "Una segunda oportunidad en teamfights.")
        )
        val dummySituational = listOf(
            ItemBuildEntry("Fuerza de la Naturaleza", "Alta resistencia contra daño mágico.")
        )
        val dummyRunes = listOf(
            RuneBuildEntry("Conquistador", "", "Acumula daño adaptable al golpear."),
            RuneBuildEntry("Triunfo", "", "Restaura vida en asesinatos o asistencias.")
        )
        val dummySpells = listOf(
            SpellBuildEntry("Destello", "", "Teletransporte instantáneo."),
            SpellBuildEntry("Prender", "", "Quema al enemigo reduciendo su curación.")
        )

        return listOf(
            CustomChampionBuildRecord(
                championId = "yasuo",
                championName = "Yasuo",
                buildTitle = "Yasuo Mid Core",
                role = "Mid Lane",
                coreItems = listOf("La Sanguinaria", "Ángel custodio"),
                situationalItems = listOf("Fuerza de la Naturaleza"),
                runes = "Conquistador",
                spells = listOf("Destello", "Prender"),
                coreItemsWithDesc = dummyItems,
                situationalItemsWithDesc = dummySituational,
                coreRunes = dummyRunes,
                coreSpells = dummySpells,
                creatorName = "Coach System",
                ratingSum = 25.0,
                voteCount = 5
            ),
            CustomChampionBuildRecord(
                championId = "ahri",
                championName = "Ahri",
                buildTitle = "Ahri Burst",
                role = "Mid Lane",
                coreItems = listOf("Eco de Luden", "Sombrero Mortal de Rabadon"),
                situationalItems = listOf("Reloj de Arena de Zhonya"),
                runes = "Electrocutar",
                spells = listOf("Destello", "Prender"),
                coreItemsWithDesc = listOf(
                    ItemBuildEntry("Eco de Luden", "Ráfaga de daño mágico."),
                    ItemBuildEntry("Sombrero Mortal de Rabadon", "Aumento masivo de poder de habilidad.")
                ),
                situationalItemsWithDesc = listOf(ItemBuildEntry("Reloj de Arena de Zhonya", "Estasis invulnerable.")),
                coreRunes = listOf(RuneBuildEntry("Electrocutar", "", "Daño extra por combos rápidos.")),
                coreSpells = dummySpells,
                creatorName = "Coach System",
                ratingSum = 24.0,
                voteCount = 5
            ),
            CustomChampionBuildRecord(
                championId = "jinx",
                championName = "Jinx",
                buildTitle = "Jinx Hypercarry",
                role = "Dragon Lane",
                coreItems = listOf("Huracán de Runaan", "Filo Infinito"),
                situationalItems = listOf("Ángel custodio"),
                runes = "Compás Letal",
                spells = listOf("Destello", "Curar"),
                coreItemsWithDesc = listOf(
                    ItemBuildEntry("Huracán de Runaan", "Disparos múltiples a objetivos secundarios."),
                    ItemBuildEntry("Filo Infinito", "Daño crítico devastador.")
                ),
                situationalItemsWithDesc = listOf(ItemBuildEntry("Ángel custodio", "Resurrección en peleas.")),
                coreRunes = listOf(RuneBuildEntry("Compás Letal", "", "Velocidad de ataque incrementada.")),
                coreSpells = listOf(
                    SpellBuildEntry("Destello", "", "Teletransporte instantáneo."),
                    SpellBuildEntry("Curar", "", "Cura y velocidad de movimiento de emergencia.")
                ),
                creatorName = "Coach System",
                ratingSum = 22.0,
                voteCount = 5
            ),
            CustomChampionBuildRecord(
                championId = "lee_sin",
                championName = "Lee Sin",
                buildTitle = "Lee Sin Jungle",
                role = "Jungle",
                coreItems = listOf("Cuchilla Negra", "Danza de la Muerte"),
                situationalItems = listOf("Ángel custodio"),
                runes = "Conquistador",
                spells = listOf("Destello", "Aplastar"),
                coreItemsWithDesc = listOf(
                    ItemBuildEntry("Cuchilla Negra", "Reducción de armadura y salud."),
                    ItemBuildEntry("Danza de la Muerte", "Mitigación de daño aplazado.")
                ),
                situationalItemsWithDesc = listOf(ItemBuildEntry("Ángel custodio", "Resurrección clave para iniciar.")),
                coreRunes = dummyRunes,
                coreSpells = listOf(
                    SpellBuildEntry("Destello", "", "Teletransporte instantáneo."),
                    SpellBuildEntry("Aplastar", "", "Daño verdadero a monstruos épicos.")
                ),
                creatorName = "Coach System",
                ratingSum = 20.0,
                voteCount = 4
            ),
            CustomChampionBuildRecord(
                championId = "darius",
                championName = "Darius",
                buildTitle = "Darius Bruiser",
                role = "Baron Lane",
                coreItems = listOf("Fuerza de la Trinidad", "Calibrador de Sterak"),
                situationalItems = listOf("Placa del Hombre Muerto"),
                runes = "Conquistador",
                spells = listOf("Destello", "Fantasmal"),
                coreItemsWithDesc = listOf(
                    ItemBuildEntry("Fuerza de la Trinidad", "Aumento de daño sostenido y movilidad."),
                    ItemBuildEntry("Calibrador de Sterak", "Escudo anti-burst vital.")
                ),
                situationalItemsWithDesc = listOf(ItemBuildEntry("Placa del Hombre Muerto", "Velocidad de movimiento extra para perseguir.")),
                coreRunes = dummyRunes,
                coreSpells = listOf(
                    SpellBuildEntry("Destello", "", "Teletransporte instantáneo."),
                    SpellBuildEntry("Fantasmal", "", "Gran velocidad de movimiento durante varios segundos.")
                ),
                creatorName = "Coach System",
                ratingSum = 23.0,
                voteCount = 5
            )
        )
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

    fun deleteBuild(context: Context, id: String) {
        init(context)
        val current = _customBuilds.value.filter { it.id != id }
        _customBuilds.value = current
        saveToLocalStorage(context, current)
        saveToCloud(context, current)
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
            val encoded = json.encodeToString(list)
            prefs.edit().putString(KEY_BUILDS_JSON, encoded).apply()
        } catch (_: Exception) {}
    }

    private fun saveToCloud(context: Context, list: List<CustomChampionBuildRecord>) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val encodedJson = json.encodeToString(list)
                val payload = mapOf(
                    "builds_json" to encodedJson,
                    "updatedAt" to System.currentTimeMillis(),
                    "totalBuilds" to list.size
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
