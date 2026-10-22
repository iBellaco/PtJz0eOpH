package com.example.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/** Remote, fail-open feature gate for the floating viewer. Fail-open keeps the
 * existing scanner usable if the configuration service is temporarily offline. */
data class OverlayAccessSnapshot(
    val enabled: Boolean = true,
    val allowedUserUids: Set<String> = emptySet()
) {
    fun allows(uid: String?): Boolean = enabled &&
        (allowedUserUids.isEmpty() || uid.orEmpty() in allowedUserUids)
}

object OverlayAccessPolicy {
    private const val COLLECTION = "system_config"
    private const val DOCUMENT = "overlay_access"

    suspend fun load(): OverlayAccessSnapshot = runCatching {
        val data = FirebaseFirestore.getInstance().collection(COLLECTION).document(DOCUMENT).get().await().data.orEmpty()
        OverlayAccessSnapshot(
            enabled = data["enabled"] as? Boolean ?: true,
            allowedUserUids = (data["allowedUserUids"] as? List<*>)?.filterIsInstance<String>()
                ?.map(String::trim)?.filter(String::isNotBlank)?.toSet().orEmpty()
        )
    }.getOrDefault(OverlayAccessSnapshot())

    suspend fun save(snapshot: OverlayAccessSnapshot) {
        FirebaseFirestore.getInstance().collection(COLLECTION).document(DOCUMENT).set(
            mapOf("enabled" to snapshot.enabled, "allowedUserUids" to snapshot.allowedUserUids.toList().sorted(),
                "updatedAt" to com.google.firebase.Timestamp.now())
        ).await()
    }
}
