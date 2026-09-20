package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Gestor de suscripciones a perfiles de Creadores y Streamers.
 * Permite a los usuarios suscribirse a creadores oficiales, streamers y VIP.
 * Mantiene sincronización local instantánea en SharedPreferences y sincronización en la nube en tiempo real.
 */
object CreatorSubscriptionManager {
    private const val TAG = "CreatorSubManager"
    private const val PREFS_NAME = "creator_subscriptions_prefs"
    private const val KEY_SUBS = "subscribed_creators_set"

    private val _subscribedCreatorKeys = MutableStateFlow<Set<String>>(emptySet())
    val subscribedCreatorKeys: StateFlow<Set<String>> = _subscribedCreatorKeys.asStateFlow()

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getStringSet(KEY_SUBS, emptySet()) ?: emptySet()
        _subscribedCreatorKeys.value = saved.map { it.lowercase(Locale.ROOT) }.toSet()

        // Sincronizar desde Firestore si hay un usuario autenticado
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            try {
                FirebaseFirestore.getInstance().collection("users").document(user.uid)
                    .get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val cloudSubs = doc.get("subscribedCreators") as? List<*> ?: emptyList<Any>()
                            val setCloud = cloudSubs.mapNotNull { it?.toString()?.lowercase(Locale.ROOT) }.toSet()
                            if (setCloud.isNotEmpty()) {
                                val merged = _subscribedCreatorKeys.value + setCloud
                                _subscribedCreatorKeys.value = merged
                                saveLocal(context, merged)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "No se pudieron sincronizar suscripciones de la nube", e)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error iniciando CreatorSubscriptionManager", e)
            }
        }
    }

    fun isSubscribed(creatorKey: String?): Boolean {
        if (creatorKey.isNullOrBlank()) return false
        val cleanKey = creatorKey.trim().lowercase(Locale.ROOT)
        return _subscribedCreatorKeys.value.contains(cleanKey)
    }

    fun toggleSubscription(creatorKey: String, creatorName: String, context: Context, onResult: (Boolean) -> Unit = {}) {
        if (creatorKey.isBlank()) return
        val cleanKey = creatorKey.trim().lowercase(Locale.ROOT)
        val currentSet = _subscribedCreatorKeys.value.toMutableSet()
        val willBeSubscribed = !currentSet.contains(cleanKey)

        if (willBeSubscribed) {
            currentSet.add(cleanKey)
        } else {
            currentSet.remove(cleanKey)
        }

        _subscribedCreatorKeys.value = currentSet
        saveLocal(context, currentSet)

        // Sincronizar con la nube
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            try {
                val userRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)
                val updateData = if (willBeSubscribed) {
                    hashMapOf("subscribedCreators" to FieldValue.arrayUnion(cleanKey))
                } else {
                    hashMapOf("subscribedCreators" to FieldValue.arrayRemove(cleanKey))
                }
                userRef.set(updateData, SetOptions.merge())
            } catch (e: Exception) {
                Log.e(TAG, "Error guardando suscripcion en Firestore", e)
            }
        }

        onResult(willBeSubscribed)
    }

    private fun saveLocal(context: Context, set: Set<String>) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putStringSet(KEY_SUBS, set).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error guardando suscripciones en SharedPreferences", e)
        }
    }
}
