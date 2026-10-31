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
    suspend fun resolve(id: String, approve: Boolean) {
        require(id.matches(Regex("[a-zA-Z0-9_-]{8,80}")))
        val suffix = if (approve) "approve" else "reject"
        EconomyServiceClient.call("SPONSOR_RESOLVE", mapOf("sponsorId" to id, "approved" to approve),
            "sponsor_review_${id.take(40)}_$suffix")
    }
}
