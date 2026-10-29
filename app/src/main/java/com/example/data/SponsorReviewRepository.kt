package com.example.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object SponsorReviewPolicy {
    fun pending(data: Map<String, Any>): Boolean = data["isApproved"] == false &&
        (data["refundedAtMillis"] as? Number)?.toLong().let { it == null || it == 0L } &&
        data["status"] !in setOf("REFUNDED", "REJECTED")

    fun publicNotice(id: String, data: Map<String, Any>): Map<String, Any> =
        data.filterKeys { it in setOf("title", "content", "videoUrl", "expandedImageUrl", "externalUrl",
            "titleColor", "contentColor", "budget", "budgetUnit", "durationValue", "durationUnit",
            "isApproved", "isEnabled", "approvedAtMillis", "expiresAtMillis", "sponsorEmail") } +
            mapOf("id" to id, "tag" to "PUBLICIDAD")

    fun notice(id: String, d: Map<String, Any>) = AppNotice(
        id=id, title=d["title"] as? String ?: "", content=d["content"] as? String ?: "",
        videoUrl=d["videoUrl"] as? String ?: "", expandedImageUrl=d["expandedImageUrl"] as? String ?: "",
        externalUrl=d["externalUrl"] as? String ?: "", titleColor=d["titleColor"] as? String ?: "#FFD700",
        tag="PUBLICIDAD", budget=(d["budget"] as? Number)?.toDouble() ?: 0.0,
        budgetUnit=d["budgetUnit"] as? String ?: "day", durationValue=(d["durationValue"] as? Number)?.toInt() ?: 1,
        durationUnit=d["durationUnit"] as? String ?: "day", isApproved=d["isApproved"] == true,
        isEnabled=d["isEnabled"] == true, approvedAtMillis=(d["approvedAtMillis"] as? Number)?.toLong() ?: 0,
        expiresAtMillis=(d["expiresAtMillis"] as? Number)?.toLong() ?: 0,
        sponsorEmail=d["sponsorEmail"] as? String ?: "")
}

/** Resolve from server data; success means both moderation and publication committed. */
object SponsorReviewRepository {
    suspend fun resolve(id: String, approve: Boolean) {
        val db = FirebaseFirestore.getInstance()
        val request = db.collection("pending_sponsor_ads").document(id)
        val registry = db.collection("system_config").document("app_notices")
        db.runTransaction { tx ->
            val pending = tx.get(request)
            val current = tx.get(registry)
            val data = pending.data ?: error("Solicitud no disponible")
            check(SponsorReviewPolicy.pending(data)) { "Solicitud ya procesada" }
            val notices = (current.get("notices") as? List<*>).orEmpty()
                .filterIsInstance<Map<String, Any>>().filterNot { it["id"] == id }
            if (approve) {
                val now = System.currentTimeMillis()
                val changes = mapOf("isApproved" to true, "isEnabled" to true, "approvedAtMillis" to now,
                    "expiresAtMillis" to AppNoticeManager.calculateExpirationMillis(now,
                        (data["durationValue"] as? Number)?.toInt() ?: 1, data["durationUnit"] as? String ?: "day"))
                tx.update(request, changes)
                tx.set(registry, mapOf("notices" to (notices + SponsorReviewPolicy.publicNotice(id, data + changes))), SetOptions.merge())
            } else {
                // Keep the outcome visible to its owner instead of silently losing the ad.
                tx.update(request, mapOf("status" to "REJECTED", "isApproved" to false, "isEnabled" to false))
                tx.set(registry, mapOf("notices" to notices), SetOptions.merge())
            }
        }.await()
    }
}
