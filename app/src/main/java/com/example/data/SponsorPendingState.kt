package com.example.data

private val pendingStatuses = setOf("PENDING", "PROCESSING", "REVIEW")

/** Keep only unresolved server records when reconciling local submissions. */
internal fun pendingSponsorIdsFromServer(
    fromCache: Boolean,
    hasError: Boolean,
    ads: List<Pair<String, Map<String, Any>>>,
    commands: List<Pair<String, Map<String, Any>>>
): Set<String> {
    if (fromCache || hasError) return emptySet()
    val ids = mutableSetOf<String>()
    ads.forEach { (id, row) ->
        val status = row["status"]?.toString()?.uppercase().orEmpty()
        val settled = row["isApproved"] == true || status in setOf("REJECTED", "REFUNDED") ||
            (row["refundedAtMillis"] as? Number)?.toLong()?.let { it > 0L } == true
        if (!settled) ids += id
    }
    commands.forEach { (id, row) ->
        val payload = row["payload"] as? Map<*, *>
        if (payload?.get("action") == "SPONSOR" && row["status"] in pendingStatuses) ids += id
    }
    return ids
}

/**
 * Only a server snapshot can claim that an economy operation is pending. A Firestore
 * cache entry is useful for rendering history, but it must never resurrect a request.
 */
internal fun authoritativePendingRequests(
    fromCache: Boolean,
    hasError: Boolean,
    rows: List<Map<String, Any>>
): List<Map<String, Any>> = if (fromCache || hasError) {
    emptyList()
} else {
    rows.filter { it["status"] in pendingStatuses }
}

internal const val sponsorOutboxGraceMillis = 30_000L

/** Local rows older than the grace period are removed when the server has no matching record. */
internal fun staleSponsorSubmissionIds(
    submissions: List<SponsorSubmission>,
    serverIds: Set<String>,
    nowMillis: Long,
    graceMillis: Long = sponsorOutboxGraceMillis
): Set<String> = submissions.asSequence()
    .filter { submission ->
        val oldEnough = submission.createdAtMillis == 0L ||
            nowMillis - submission.createdAtMillis >= graceMillis
        oldEnough && submission.notice.id !in serverIds
    }
    .map { it.notice.id }
    .toSet()
