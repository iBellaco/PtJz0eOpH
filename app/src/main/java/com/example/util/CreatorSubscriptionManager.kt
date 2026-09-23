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
    const val SUBSCRIPTION_EA_COST = 500L

    private val _subscribedCreatorKeys = MutableStateFlow<Set<String>>(emptySet())
    val subscribedCreatorKeys: StateFlow<Set<String>> = _subscribedCreatorKeys.asStateFlow()

    private var firestoreListener: ListenerRegistration? = null
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        loadFromLocalStorage(context)
        attachCloudListener(context)
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
        val user = FirebaseAuth.getInstance().currentUser ?: return
        if (user.isAnonymous) return

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
        val currentEssence = SubscriptionManager.blueEssence.value
        if (currentEssence < SUBSCRIPTION_EA_COST) {
            onResult(false, "Necesitas al menos $SUBSCRIPTION_EA_COST de Esencia Azul para suscribirte.")
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
                SubscriptionManager.addBlueEssence(-SUBSCRIPTION_EA_COST)
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null && !user.isAnonymous) {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").document(user.uid)
                        .set(
                            mapOf("subscribedCreators" to FieldValue.arrayUnion(cleanKey)),
                            SetOptions.merge()
                        ).await()
                }
                CoroutineScope(Dispatchers.Main).launch {
                    onResult(true, "¡Te has suscrito con éxito a $creatorName!")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error subscribing to creator: ${e.message}")
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
