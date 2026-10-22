package com.example.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/** Fail-open policy for the optional viewer buttons inside the Hub. */
data class OverlayAccessSnapshot(
    val hubButtonsVisible: Boolean = true,
    val allowedAccountIds: Set<String> = emptySet(),
    val allowedUserUids: Set<String> = emptySet()
) {
    private val effectiveAccounts get() = allowedAccountIds.ifEmpty { allowedUserUids }
    fun hubButtonsVisibleFor(accountId: String?): Boolean = hubButtonsVisible &&
        (effectiveAccounts.isEmpty() || accountId.orEmpty() in effectiveAccounts)
}

object OverlayAccessPolicy {
    private const val COLLECTION = "system_config"
    private const val DOCUMENT = "overlay_access"

    suspend fun load(): OverlayAccessSnapshot = runCatching {
        val data = FirebaseFirestore.getInstance().collection(COLLECTION).document(DOCUMENT).get().await().data.orEmpty()
        OverlayAccessSnapshot(
            hubButtonsVisible = data["hubButtonsVisible"] as? Boolean ?: data["enabled"] as? Boolean ?: true,
            allowedAccountIds = (data["allowedAccountIds"] as? List<*>)?.filterIsInstance<String>()
                ?.map(String::trim)?.filter(String::isNotBlank)?.toSet().orEmpty(),
            allowedUserUids = (data["allowedUserUids"] as? List<*>)?.filterIsInstance<String>()
                ?.map(String::trim)?.filter(String::isNotBlank)?.toSet().orEmpty()
        )
    }.getOrDefault(OverlayAccessSnapshot())

    suspend fun save(snapshot: OverlayAccessSnapshot) {
        FirebaseFirestore.getInstance().collection(COLLECTION).document(DOCUMENT).set(
            mapOf("hubButtonsVisible" to snapshot.hubButtonsVisible,
                "allowedAccountIds" to snapshot.allowedAccountIds.toList().sorted(),
                "updatedAt" to com.google.firebase.Timestamp.now())
        ).await()
    }
}
