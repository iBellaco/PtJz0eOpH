package com.example.data

/** A retry is successful only when the original decision is already durably reflected. */
object StreamerReviewPolicy {
    fun isAlreadyApplied(request: Map<String, Any>, live: List<Map<String, Any>>, approve: Boolean): Boolean {
        if (!approve) return request["status"] == "REJECTED"
        if (request["status"] != "APPROVED") return false
        val id = StreamerPublicationPolicy.publicationId(request)
        return live.any { it["publicationId"] == id && it["userId"] == request["userId"] &&
            it["channelUrl"] == request["channelUrl"] }
    }
}
