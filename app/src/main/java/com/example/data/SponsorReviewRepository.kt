package com.example.data

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
    /** Include durable submissions even before the worker creates the charged advertisement. */
    fun observe(onChange: (List<Map<String, Any>>) -> Unit, onError: () -> Unit): List<com.google.firebase.firestore.ListenerRegistration> {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        var ads = emptyList<Map<String, Any>>()
        var queued = emptyList<Map<String, Any>>()
        fun publish() {
            val known = ads.map { it["id"] }.toSet()
            onChange(ads.filter { SponsorReviewPolicy.pending(it) } + queued.filter { it["id"] !in known })
        }
        val adListener = db.collection("pending_sponsor_ads").addSnapshotListener { snapshot, error ->
            if (error != null) { onError(); return@addSnapshotListener }
            ads = snapshot?.documents.orEmpty().map { it.data.orEmpty() + ("id" to it.id) }
            publish()
        }
        val queueListener = db.collection("economy_requests").whereEqualTo("payload.action", "SPONSOR")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { onError(); return@addSnapshotListener }
                queued = snapshot?.documents.orEmpty().filter { it.getString("status") in setOf("PENDING", "PROCESSING", "REVIEW") }.mapNotNull { row ->
                    @Suppress("UNCHECKED_CAST")
                    val notice = row.get("payload.notice") as? Map<String, Any> ?: return@mapNotNull null
                    val id = row.getString("operationId") ?: return@mapNotNull null
                    notice + mapOf("id" to id, "queuedRequestId" to row.id, "isApproved" to false,
                        "createdAt" to (row.getTimestamp("createdAt")?.toDate()?.time ?: 0L))
                }
                publish()
            }
        return listOf(adListener, queueListener)
    }
    suspend fun resolve(id: String, approve: Boolean, queuedRequestId: String? = null) {
        require(id.matches(Regex("[a-zA-Z0-9_-]{8,80}")))
        val suffix = if (approve) "approve" else "reject"
        val fields = mapOf<String, Any>("sponsorId" to id, "approved" to approve) +
            (queuedRequestId?.let { mapOf("queuedRequestId" to it) } ?: emptyMap())
        EconomyServiceClient.call("SPONSOR_RESOLVE", fields,
            "sponsor_review_${id.take(40)}_$suffix")
    }
}
