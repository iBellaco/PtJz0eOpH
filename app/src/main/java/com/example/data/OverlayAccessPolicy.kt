package com.example.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

/** Fail-open policy for the optional viewer buttons inside the Hub. */
data class OverlayAccessSnapshot(
    val hubButtonsVisible: Boolean = true,
    val allowedAccountIds: Set<String> = emptySet(),
    val allowedUserUids: Set<String> = emptySet(),
    val activationButtonEnabled: Boolean = true
) {
    private val effectiveAccounts get() = allowedAccountIds.ifEmpty { allowedUserUids }
    fun hubButtonsVisibleFor(accountId: String?): Boolean = hubButtonsVisible &&
        (effectiveAccounts.isEmpty() || accountId.orEmpty() in effectiveAccounts)
}

object OverlayAccessPolicy {
    private const val COLLECTION = "system_config"
    private const val DOCUMENT = "overlay_access"
    // Documento público que expone solo el estado global del botón, nunca la lista de cuentas.
    private const val ACTIVATION_DOCUMENT = "overlay_activation"

    private fun viewerSnapshot(data: Map<String, Any?>): OverlayAccessSnapshot = OverlayAccessSnapshot(
        hubButtonsVisible = data["hubButtonsVisible"] as? Boolean ?: data["enabled"] as? Boolean ?: true,
        allowedAccountIds = (data["allowedAccountIds"] as? List<*>)?.filterIsInstance<String>()
            ?.map(String::trim)?.filter(String::isNotBlank)?.toSet().orEmpty(),
        allowedUserUids = (data["allowedUserUids"] as? List<*>)?.filterIsInstance<String>()
            ?.map(String::trim)?.filter(String::isNotBlank)?.toSet().orEmpty()
    )

    suspend fun load(): OverlayAccessSnapshot {
        val db = FirebaseFirestore.getInstance().collection(COLLECTION)
        val viewer = viewerSnapshot(db.document(DOCUMENT).get().await().data.orEmpty())
        return viewer.copy(activationButtonEnabled =
            db.document(ACTIVATION_DOCUMENT).get().await().getBoolean("enabled") ?: true)
    }

    /** Se actualiza en tiempo real; al faltar conexión, el botón permanece bloqueado hasta la primera lectura. */
    fun observeActivation(onChange: (Boolean) -> Unit): ListenerRegistration =
        FirebaseFirestore.getInstance().collection(COLLECTION).document(ACTIVATION_DOCUMENT)
            .addSnapshotListener { document, error ->
                if (error == null) onChange(document?.getBoolean("enabled") ?: true)
            }

    suspend fun save(snapshot: OverlayAccessSnapshot) {
        val db = FirebaseFirestore.getInstance()
        val config = db.collection(COLLECTION)
        val batch = db.batch()
        batch.set(config.document(DOCUMENT),
            mapOf("hubButtonsVisible" to snapshot.hubButtonsVisible,
                "allowedAccountIds" to snapshot.allowedAccountIds.toList().sorted(),
                "updatedAt" to com.google.firebase.Timestamp.now()))
        batch.set(config.document(ACTIVATION_DOCUMENT),
            mapOf("enabled" to snapshot.activationButtonEnabled,
                "updatedAt" to com.google.firebase.Timestamp.now()))
        batch.commit().await()
    }
}
