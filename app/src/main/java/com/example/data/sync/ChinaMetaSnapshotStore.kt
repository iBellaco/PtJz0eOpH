package com.example.data.sync

import com.example.data.GamePatch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Published China meta is shared by account sessions instead of being a
 * device-only preference. The public Tencent feed remains the source; this
 * document makes the exact parsed snapshot and tier movement available to
 * every Coach installation, including users who have not created an account.
 * Only the staff publisher writes this document; all clients consume the same
 * read-only snapshot so China statistics and tier movements do not diverge by
 * device.
 */
object ChinaMetaSnapshotStore {
    data class Snapshot(
        val patch: String,
        val checkedAt: Long,
        val tiersJson: String,
        val statsJson: String,
        val roleStatsJson: String,
        val groupsJson: String,
        val previousTiersJson: String,
        val previousGroupsJson: String,
        val movementsJson: String,
        val groupMovementsJson: String
    )

    private const val DOCUMENT_ID = "china_meta_snapshot"
    private var initialized = false
    private var documentListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var callback: ((Snapshot) -> Unit)? = null
    private var latest: Snapshot? = null

    private val document
        get() = FirebaseFirestore.getInstance().collection("system_config").document(DOCUMENT_ID)

    @Synchronized
    fun initialize(onSnapshot: (Snapshot) -> Unit) {
        callback = onSnapshot
        if (initialized) return
        initialized = true
        runCatching {
            // This listener is deliberately attached without waiting for an
            // authenticated session. Firestore rules expose this one document
            // publicly; the write path below remains restricted to staff.
            documentListener = document.addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val parsed = decode(snapshot.data.orEmpty()) ?: return@addSnapshotListener
                synchronized(this) { latest = parsed }
                callback?.invoke(parsed)
            }
        }
    }

    @Synchronized
    fun current(): Snapshot? = latest

    /**
     * Gives a new installation a short initial read window before it falls
     * back to the public feed. This keeps the first China render aligned with
     * the same published snapshot used by other devices, even when the
     * realtime callback has not fired yet.
     */
    suspend fun awaitCurrent(timeoutMillis: Long = 2_500L): Snapshot? {
        current()?.let { return it }
        return try {
            withTimeoutOrNull(timeoutMillis) { document.get().await() }
                ?.takeIf { it.exists() }
                ?.let { decode(it.data.orEmpty()) }
                ?.also { parsed -> synchronized(this) { latest = parsed } }
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null
        }
    }

    suspend fun publish(snapshot: Snapshot) {
        if (snapshot.patch != GamePatch.VERSION) return
        try {
            withTimeoutOrNull(5_000L) {
                val user = FirebaseAuth.getInstance().currentUser ?: return@withTimeoutOrNull
                if (user.getIdToken(false).await().claims["admin"] != true) return@withTimeoutOrNull
                document.set(
                    mapOf(
                        "patch" to snapshot.patch,
                        "checkedAt" to snapshot.checkedAt,
                        "tiersJson" to snapshot.tiersJson,
                        "statsJson" to snapshot.statsJson,
                        "roleStatsJson" to snapshot.roleStatsJson,
                        "groupsJson" to snapshot.groupsJson,
                        "previousTiersJson" to snapshot.previousTiersJson,
                        "previousGroupsJson" to snapshot.previousGroupsJson,
                        "movementsJson" to snapshot.movementsJson,
                        "groupMovementsJson" to snapshot.groupMovementsJson
                    )
                ).await()
                synchronized(this@ChinaMetaSnapshotStore) { latest = snapshot }
            }
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // The feed and local cache remain authoritative when Cloud Firestore
            // is unavailable; the next authenticated staff poll retries publish.
        }
    }

    private fun decode(data: Map<String, Any?>): Snapshot? {
        val patch = data["patch"] as? String ?: return null
        val checkedAt = (data["checkedAt"] as? Number)?.toLong() ?: return null
        fun text(key: String) = data[key] as? String ?: "{}"
        return Snapshot(
            patch = patch,
            checkedAt = checkedAt,
            tiersJson = text("tiersJson"),
            statsJson = text("statsJson"),
            roleStatsJson = text("roleStatsJson"),
            groupsJson = text("groupsJson"),
            previousTiersJson = text("previousTiersJson"),
            previousGroupsJson = text("previousGroupsJson"),
            movementsJson = text("movementsJson"),
            groupMovementsJson = text("groupMovementsJson")
        )
    }
}
