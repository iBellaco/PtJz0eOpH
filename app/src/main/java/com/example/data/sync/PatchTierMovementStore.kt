package com.example.data.sync

import android.content.Context
import com.example.data.GamePatch
import com.example.data.WildRiftRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject

/** One authoritative patch snapshot, published by signed staff; every device observes it. */
object PatchTierMovementStore {
    private var context: Context? = null
    private var listener: ListenerRegistration? = null
    private var shared = false
    private var movements: Map<String, Int> = emptyMap()
    private val key get() = "movement_${GamePatch.VERSION}"
    private val document get() = FirebaseFirestore.getInstance().collection("system_config")
        .document("tier_movement_${GamePatch.VERSION}")

    @Synchronized fun initialize(app: Context) {
        if (context != null) return
        context = app.applicationContext
        val prefs = app.getSharedPreferences("patch_tier_movement", Context.MODE_PRIVATE)
        movements = runCatching {
            val json = JSONObject(prefs.getString(key, "{}").orEmpty())
            json.keys().asSequence().associateWith { json.getInt(it).coerceIn(-1, 1) }
        }.getOrDefault(emptyMap())
        runCatching {
            FirebaseAuth.getInstance().addAuthStateListener { auth ->
                listener?.remove()
                if (auth.currentUser != null) listener = document.addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot?.getString("patch") == GamePatch.VERSION) {
                        val values = snapshot.get("movements") as? Map<*, *> ?: return@addSnapshotListener
                        synchronized(this) {
                            shared = true
                            save(values.entries.mapNotNull { (id, value) ->
                                if (id is String && value is Number) id to value.toInt().coerceIn(-1, 1) else null
                            }.toMap())
                            WildRiftRepository.applyPatchMovements(movements)
                        }
                    }
                }
            }
        }
    }

    @Synchronized fun snapshot(): Map<String, Int> = movements.toMap()
    private fun save(values: Map<String, Int>) {
        movements = values
        context?.getSharedPreferences("patch_tier_movement", Context.MODE_PRIVATE)?.edit()
            ?.putString(key, JSONObject(values).toString())?.apply()
    }

    suspend fun update(previous: Map<String, String>, current: Map<String, String>) {
        synchronized(this) { if (!shared) save(PatchTierMovement.update(previous, current, movements)) }
        // No public client can publish a global movement or overwrite another device's state.
        try {
            withTimeoutOrNull(5_000L) {
                val user = FirebaseAuth.getInstance().currentUser ?: return@withTimeoutOrNull
                if (user.getIdToken(false).await().claims["admin"] != true) return@withTimeoutOrNull
                FirebaseFirestore.getInstance().runTransaction { tx ->
                    val remote = tx.get(document)
                    @Suppress("UNCHECKED_CAST")
                    val oldTiers = (remote.get("tiers") as? Map<String, String>) ?: previous
                    val oldMovements = (remote.get("movements") as? Map<*, *>)?.entries?.mapNotNull { (id, v) ->
                        if (id is String && v is Number) id to v.toInt() else null
                    }?.toMap() ?: snapshot()
                    tx.set(document, mapOf("patch" to GamePatch.VERSION, "tiers" to (oldTiers + current),
                        "movements" to PatchTierMovement.update(oldTiers, current, oldMovements)))
                }.await()
            }
        } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled
        } catch (_: Exception) { /* Keep the last confirmed patch snapshot for offline use. */ }
    }
}
