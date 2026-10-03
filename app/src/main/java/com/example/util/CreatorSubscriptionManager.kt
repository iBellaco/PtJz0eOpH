package com.example.util

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object CreatorSubscriptionManager {
    private const val TAG = "CreatorSubManager"
    private const val PREFS_NAME = "wr_creator_subscriptions_prefs"
    private const val KEY_SUBS_SET = "subscribed_creators_set"
    const val SUBSCRIPTION_EA_COST = 50L
    const val SUBSCRIPTION_EN_COST = 5L

    data class CreatorLimits(
        val maxChampions: Int,
        val maxSubscribers: Int,
        val displayName: String
    )

    fun getCreatorLimits(role: String?): CreatorLimits {
        val norm = role?.trim()?.lowercase() ?: ""
        return when (norm) {
            "creador" -> CreatorLimits(1, 50, "Creador Lvl 1")
            "creador_lvl2" -> CreatorLimits(3, 150, "Creador Lvl 2")
            "creador_lvl3" -> CreatorLimits(5, 250, "Creador Lvl 3")
            "creador_lvl4" -> CreatorLimits(7, 350, "Creador Lvl 4")
            "creador_lvl5" -> CreatorLimits(10, 500, "Creador Lvl 5")
            "admin", "moderador" -> CreatorLimits(999, 99999, "Staff")
            "streamer" -> CreatorLimits(999, 99999, "Streamer")
            else -> CreatorLimits(1, 50, "Creador Lvl 1")
        }
    }

    fun sendLimitExceededNotification(creatorUid: String, creatorName: String) {
        if (creatorUid.isBlank()) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val messageId = java.util.UUID.randomUUID().toString()
                val messageData = hashMapOf<String, Any>(
                    "id" to messageId,
                    "title" to "¡Límite de Suscriptores Alcanzado!",
                    "content" to "Hola $creatorName, un usuario intentó suscribirse a tu perfil pero has alcanzado el límite máximo de suscriptores permitido para tu nivel actual. Te sugerimos mejorar tu plan para ampliar tu límite y seguir recibiendo suscriptores.",
                    "tag" to "GENERAL", "panel" to "CREATOR",
                    "timestamp" to System.currentTimeMillis(),
                    "isRead" to false
                )
                val userDocRef = db.collection("users").document(creatorUid)
                userDocRef.collection("messages").document(messageId).set(messageData).await()
                userDocRef.update(
                    "hasUnreadMessages", true,
                    "unreadMessagesCount", FieldValue.increment(1),
                    "privateMessages", FieldValue.arrayUnion(messageData)
                ).await()
                Log.d(TAG, "Sent automatic limit exceeded inbox notification to creator $creatorUid")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send automatic notification: ${e.message}")
            }
        }
    }

    private val _subscribedCreatorKeys = MutableStateFlow<Set<String>>(emptySet())
    val subscribedCreatorKeys: StateFlow<Set<String>> = _subscribedCreatorKeys.asStateFlow()

    private var firestoreListener: ListenerRegistration? = null
    private var authStateListener: com.google.firebase.auth.FirebaseAuth.AuthStateListener? = null
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        loadFromLocalStorage(context)
        try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            authStateListener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { firebaseAuth ->
                attachCloudListener(context)
            }
            auth.addAuthStateListener(authStateListener!!)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding auth state listener: ${e.message}")
            attachCloudListener(context)
        }
    }

    private fun loadFromLocalStorage(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getStringSet(KEY_SUBS_SET, emptySet()) ?: emptySet()
        _subscribedCreatorKeys.value = saved
    }

    private fun saveToLocalStorage(context: Context, set: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_SUBS_SET, set).apply()
    }

    private fun attachCloudListener(context: Context) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null || user.isAnonymous) {
            firestoreListener?.remove()
            firestoreListener = null
            _subscribedCreatorKeys.value = emptySet()
            return
        }

        try {
            firestoreListener?.remove()
            val db = FirebaseFirestore.getInstance()
            firestoreListener = db.collection("users").document(user.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                    val rawList = snapshot.get("subscribedCreators") as? List<*>
                    if (rawList != null) {
                        val stringSet = rawList.filterIsInstance<String>().toSet()
                        _subscribedCreatorKeys.value = stringSet
                        saveToLocalStorage(context, stringSet)
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error attaching cloud listener: ${e.message}")
        }
    }

    fun isSubscribed(creatorKey: String): Boolean {
        if (creatorKey.isBlank()) return true
        val cleanKey = creatorKey.trim().lowercase()
        return _subscribedCreatorKeys.value.any { it.trim().lowercase() == cleanKey }
    }

    fun subscribeWithBlueEssence(
        creatorKey: String,
        creatorName: String,
        creatorUid: String,
        context: Context,
        onResult: (Boolean, String) -> Unit
    ) {
        onResult(false, "Las suscripciones a perfiles de creadores ahora se realizan exclusivamente con Esencia Naranja (${SUBSCRIPTION_EN_COST} EN).")
    }

    fun subscribeWithOrangeEssence(
        creatorKey: String,
        creatorName: String,
        creatorUid: String,
        context: Context,
        onResult: (Boolean, String) -> Unit
    ) {
        val currentEssence = SubscriptionManager.orangeEssence.value
        if (currentEssence < SUBSCRIPTION_EN_COST) {
            onResult(false, "Necesitas al menos $SUBSCRIPTION_EN_COST de Esencia Naranja para suscribirte.")
            return
        }

        val cleanKey = if (creatorUid.isNotBlank()) creatorUid else creatorName.trim()
        if (cleanKey.isBlank()) {
            onResult(false, "Creador no válido.")
            return
        }

        val currentSet = _subscribedCreatorKeys.value.toMutableSet()
        if (currentSet.contains(cleanKey)) {
            onResult(true, "Ya estás suscrito a este creador.")
            return
        }

        currentSet.add(cleanKey)
        _subscribedCreatorKeys.value = currentSet
        saveToLocalStorage(context, currentSet)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                var creatorRole = "creador"
                if (creatorUid.isNotBlank()) {
                    val db = FirebaseFirestore.getInstance()
                    val doc = db.collection("users").document(creatorUid).get().await()
                    if (doc.exists()) {
                        creatorRole = doc.getString("role") ?: "creador"
                    }
                }

                val percentage = when (creatorRole.trim().lowercase()) {
                    "creador" -> 0.50
                    "creador_lvl2" -> 0.60
                    "creador_lvl3" -> 0.70
                    "creador_lvl4" -> 0.80
                    "creador_lvl5" -> 0.80
                    "moderador" -> 0.80
                    "streamer" -> 0.80
                    else -> 0.50
                }
                val enRewarded = kotlin.math.round(SUBSCRIPTION_EN_COST * percentage).toLong()

                SubscriptionManager.addOrangeEssence(-SUBSCRIPTION_EN_COST, "Suscripción a Creador: $creatorName")
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null && !user.isAnonymous) {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").document(user.uid)
                        .set(
                            mapOf("subscribedCreators" to FieldValue.arrayUnion(cleanKey)),
                            SetOptions.merge()
                        ).await()
                }

                // Reward creator and notify
                if (creatorUid.isNotBlank()) {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").document(creatorUid)
                        .update("orangeEssence", FieldValue.increment(enRewarded))
                        .await()

                    // Log history for creator
                    val subscriberName = SubscriptionManager.userName.value.ifBlank { "Un invocador" }
                    SubscriptionHistoryManager.addRecordForUser(
                        creatorUid,
                        0L,
                        "Pago por Suscriptor: $subscriberName ($creatorRole)",
                        "Añadido por Suscripción",
                        "+$enRewarded EN"
                    )

                    // Send notification to creator
                    val messageId = java.util.UUID.randomUUID().toString()
                    val messageData = hashMapOf<String, Any>(
                        "id" to messageId,
                        "title" to "¡Nueva Suscripción con Esencia Naranja!",
                        "content" to "¡Felicidades! El invocador $subscriberName se ha suscrito a tu perfil con Esencia Naranja. De acuerdo con tu nivel de creador ($creatorRole), has recibido un pago de $enRewarded Esencias Naranjas (el ${ (percentage * 100).toInt() }% de la suscripción). ¡Sigue publicando builds grandiosas!",
                        "tag" to "GENERAL", "panel" to "CREATOR",
                        "timestamp" to System.currentTimeMillis(),
                        "isRead" to false
                    )
                    val creatorDocRef = db.collection("users").document(creatorUid)
                    creatorDocRef.collection("messages").document(messageId).set(messageData).await()
                    creatorDocRef.update(
                        "hasUnreadMessages", true,
                        "unreadMessagesCount", FieldValue.increment(1),
                        "privateMessages", FieldValue.arrayUnion(messageData)
                    ).await()
                }

                CoroutineScope(Dispatchers.Main).launch {
                    onResult(true, "¡Te has suscrito con éxito a $creatorName con Esencia Naranja!")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error subscribing to creator with Orange Essence: ${e.message}")
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(true, "Suscripción guardada localmente.")
                }
            }
        }
    }

    fun unsubscribe(creatorKey: String, context: Context) {
        val currentSet = _subscribedCreatorKeys.value.toMutableSet()
        currentSet.remove(creatorKey)
        _subscribedCreatorKeys.value = currentSet
        saveToLocalStorage(context, currentSet)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && !user.isAnonymous) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(user.uid)
                .set(
                    mapOf("subscribedCreators" to FieldValue.arrayRemove(creatorKey)),
                    SetOptions.merge()
                )
        }
    }
}
