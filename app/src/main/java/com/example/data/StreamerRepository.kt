package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object StreamerRepository {
    private val db get() = FirebaseFirestore.getInstance()
    val registry get() = db.collection("system_config").document("streamer_live")
    val requests get() = db.collection("streamer_requests")
    val metrics get() = db.collection("streamer_click_metrics")
    fun history(uid: String) = requests.document(uid).collection("history")
    private suspend fun historyAvailable(uid: String): Boolean = try {
        history(uid).limit(1).get(com.google.firebase.firestore.Source.SERVER).await()
        true
    } catch (error: com.google.firebase.firestore.FirebaseFirestoreException) {
        if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) false else throw error
    }
    private suspend fun metricsAvailable(uid: String): Boolean = try {
        metrics.whereEqualTo("userId", uid).limit(1).get(com.google.firebase.firestore.Source.SERVER).await()
        true
    } catch (error: com.google.firebase.firestore.FirebaseFirestoreException) {
        if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) false else throw error
    }
    @Suppress("UNCHECKED_CAST")
    fun entries(value: Any?): List<Map<String, Any>> = (value as? List<*>)?.mapNotNull { it as? Map<String, Any> }.orEmpty()
    private fun hasRole(role: String?, secondary: String?, adminClaim: Boolean = false) =
        com.example.model.RolePanelAccess.canOpen(com.example.model.RolePanel.STREAMER, role.orEmpty(), secondary.orEmpty(), adminClaim)

    suspend fun submit(name: String, rawUrl: String, durationHours: Int = 3): Result<Unit> = withContext(Dispatchers.IO) { runCatching {
        val user = FirebaseAuth.getInstance().currentUser ?: error("streamer_error")
        check(name.trim().length in 2..60) { "streamer_name_error" }
        val adminClaim = com.example.util.AuthManager.isAdminClaim.value
        val ref = requests.document(user.uid)
        val publicationId = java.util.UUID.randomUUID().toString()
        val archive = historyAvailable(user.uid)
        db.runTransaction { transaction ->
            val live = entries(transaction.get(registry).get("entries"))
            val current = transaction.get(ref)
            val account = transaction.get(db.collection("users").document(user.uid))
            val isAdmin = com.example.model.RolePanelAccess.isAdministrator(account.getString("role").orEmpty(), adminClaim)
            check(hasRole(account.getString("role"), account.getString("secondaryRole"), adminClaim)) { "streamer_role_error" }
            val channel = StreamChannelUrl.parse(rawUrl, allowAdminTest = isAdmin) ?: error("streamer_url_error")
            check(StreamerPublicationPolicy.canRequest(live, user.uid)) { "streamer_max" }
            val now = System.currentTimeMillis()
            val prior = current.data.orEmpty()
            check(current.getString("status") != "PENDING" || StreamerPublicationPolicy.isExpired(prior, now)) { "streamer_pending" }
            if (archive && current.exists() && !StreamerPublicationPolicy.historyExpired(prior, now)) {
                val previous = prior.toMutableMap()
                if (StreamerPublicationPolicy.isExpired(prior, now)) previous.putAll(mapOf("status" to "REJECTED",
                    "rejectionReason" to "TIMEOUT", "reviewedAtMillis" to StreamerPublicationPolicy.expiresAt(prior)))
                transaction.set(history(user.uid).document(StreamerPublicationPolicy.publicationId(prior)), previous)
            }
            val durationLabel = when (durationHours) {
                3 -> "3 horas"
                6 -> "6 horas"
                12 -> "12 horas"
                else -> "Extensible"
            }
            val accountUserName = account.getString("name")?.takeIf { it.isNotBlank() }
                ?: account.getString("userName")?.takeIf { it.isNotBlank() }
                ?: user.displayName?.takeIf { it.isNotBlank() }
                ?: "Usuario"
            val fields = mutableMapOf<String, Any>("userId" to user.uid, "userName" to accountUserName,
                "channelName" to name.trim(), "channelUrl" to channel.url, "platform" to channel.platform,
                "status" to "PENDING", "usingCoachAcknowledged" to true, "submittedAtMillis" to now,
                "durationHours" to durationHours, "durationLabel" to durationLabel)
            if (archive) fields.putAll(mapOf("submittedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(), "publicationId" to publicationId,
                "streamerHistoryDeleteAt" to com.google.firebase.Timestamp(java.util.Date(now + StreamerPublicationPolicy.PENDING_HISTORY_WINDOW_MILLIS))))
            if (isAdmin) fields["adminTest"] = channel.platform == "Google"
            transaction.set(ref, fields)
            if (archive) transaction.set(history(user.uid).document(publicationId), fields)
        }.await()
        Unit
    } }

    suspend fun review(uid: String, approve: Boolean, verifiedUsingCoach: Boolean): Result<Unit> = withContext(Dispatchers.IO) { runCatching {
        check(SupportTicketAccess.isAdmin()) { "streamer_error" }
        val ref = requests.document(uid)
        FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.await()
        val archive = true
        val countClicks = approve
        db.runTransaction { transaction ->
            val request = transaction.get(ref)
            val live = entries(transaction.get(registry).get("entries"))
            val account = transaction.get(db.collection("users").document(uid))
            val publicationId = StreamerPublicationPolicy.publicationId(request.data.orEmpty())
            val metricRef = metrics.document(publicationId)
            val metric = if (approve && countClicks) transaction.get(metricRef) else null
            // A repeated tap or a retry after a committed operation must not reset counters.
            if (StreamerReviewPolicy.isAlreadyApplied(request.data.orEmpty(), live, approve)) {
                if (approve && metric?.exists() != true) transaction.set(metricRef,
                    mapOf("userId" to uid, "publicationId" to publicationId,
                        "submittedAtMillis" to StreamerPublicationPolicy.submittedAt(request.data.orEmpty()),
                        "clickCount" to 0L, "status" to "APPROVED", "countingStartedAtMillis" to System.currentTimeMillis()))
                return@runTransaction
            }
            check(request.getString("status") == "PENDING") { "streamer_error" }
            check(!StreamerPublicationPolicy.isExpired(request.data.orEmpty())) { "streamer_expired" }
            val reviewedAt = System.currentTimeMillis()
            if (approve) {
                check(verifiedUsingCoach && request.getBoolean("usingCoachAcknowledged") == true) { "streamer_requirement" }
                // Only a trusted staff writer can create adminTest; ordinary requests forbid this field.
                val trustedTest = request.getBoolean("adminTest") == true
                check(hasRole(account.getString("role"), account.getString("secondaryRole"), trustedTest)) { "streamer_role_error" }
                val channel = StreamChannelUrl.parse(request.getString("channelUrl").orEmpty(),
                    allowAdminTest = trustedTest) ?: error("streamer_url_error")
                val durHours = (request.getLong("durationHours") ?: (request.get("durationHours") as? Number)?.toLong() ?: 3L).toInt()
                val durLabel = request.getString("durationLabel") ?: if (durHours == 0) "Extensible" else "$durHours horas"
                val entry = mapOf<String, Any>("userId" to uid, "channelName" to request.getString("channelName").orEmpty(),
                    "channelUrl" to channel.url, "platform" to channel.platform, "approvedAtMillis" to reviewedAt,
                    "durationHours" to durHours, "durationLabel" to durLabel,
                    "publicationId" to StreamerPublicationPolicy.publicationId(request.data.orEmpty()))
                transaction.set(registry, mapOf("entries" to StreamerPublicationPolicy.approve(live, entry)), SetOptions.merge())
                if (countClicks && metric?.exists() != true) transaction.set(metricRef,
                    mapOf("userId" to uid, "publicationId" to StreamerPublicationPolicy.publicationId(request.data.orEmpty()),
                        "submittedAtMillis" to StreamerPublicationPolicy.submittedAt(request.data.orEmpty()), "clickCount" to 0L,
                        "status" to "APPROVED"))
            }
            val reviewed = mutableMapOf<String, Any>("status" to if (approve) "APPROVED" else "REJECTED",
                "verifiedUsingCoach" to (approve && verifiedUsingCoach), "reviewedAtMillis" to reviewedAt)
            if (approve) reviewed["approvedAtMillis"] = reviewedAt
            if (archive) reviewed["streamerHistoryDeleteAt"] = if (approve)
                com.google.firebase.firestore.FieldValue.delete() else com.google.firebase.Timestamp(java.util.Date(reviewedAt + StreamerPublicationPolicy.HISTORY_WINDOW_MILLIS))
            transaction.update(ref, reviewed)
            val messageId = "streamer_review_${StreamerPublicationPolicy.publicationId(request.data.orEmpty())}"
            transaction.set(db.collection("users").document(uid).collection("messages").document(messageId),
                mapOf("id" to messageId, "title" to "Publicación de streamer",
                    "content" to if (approve) "Tu publicación fue aceptada y el canal ya está visible." else "Tu publicación fue rechazada. Puedes enviar una nueva solicitud.",
                    "tag" to "GENERAL", "panel" to "STREAMER", "timestamp" to reviewedAt, "isRead" to false))
            if (archive) {
                val archived = (request.data.orEmpty() + reviewed).toMutableMap()
                if (approve) archived.remove("streamerHistoryDeleteAt")
                transaction.set(history(uid).document(StreamerPublicationPolicy.publicationId(request.data.orEmpty())), archived)
            }
        }.await()
        Unit
    } }

    suspend fun end(uid: String): Result<Unit> = withContext(Dispatchers.IO) { runCatching {
        val user = FirebaseAuth.getInstance().currentUser ?: error("streamer_error")
        check(user.uid == uid || SupportTicketAccess.isAdmin()) { "streamer_error" }
        val ref = requests.document(uid)
        val archive = historyAvailable(uid)
        val countClicks = metricsAvailable(uid)
        db.runTransaction { transaction ->
            val live = entries(transaction.get(registry).get("entries"))
            val request = transaction.get(ref)
            val metricRef = metrics.document(StreamerPublicationPolicy.publicationId(request.data.orEmpty()))
            val metric = if (countClicks) transaction.get(metricRef) else null
            transaction.set(registry, mapOf("entries" to live.filterNot { it["userId"] == uid }), SetOptions.merge())
            if (request.exists()) {
                check(request.getString("status") == "APPROVED") { "streamer_error" }
                val endedAt = System.currentTimeMillis()
                val ended = mutableMapOf<String, Any>("status" to "ENDED", "endedAtMillis" to endedAt)
                if (archive) ended["streamerHistoryDeleteAt"] = com.google.firebase.Timestamp(java.util.Date(endedAt + StreamerPublicationPolicy.HISTORY_WINDOW_MILLIS))
                transaction.update(ref, ended)
                if (archive) transaction.set(history(uid).document(StreamerPublicationPolicy.publicationId(request.data.orEmpty())), request.data.orEmpty() + ended)
                if (metric?.exists() == true) transaction.update(metricRef, ended)

            }
        }.await()
        Unit
    } }
    /** Idempotent across devices; the expired request leaves the queue and its history survives. */
    suspend fun expire(uid: String): Result<Unit> = withContext(Dispatchers.IO) { runCatching {
        val ref = requests.document(uid)
        val archive = historyAvailable(uid)
        if (!archive && !SupportTicketAccess.isAdmin()) throw com.google.firebase.firestore.FirebaseFirestoreException(
            "streamer_history_error", com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED)
        db.runTransaction { transaction ->
            val request = transaction.get(ref)
            val data = request.data.orEmpty()
            if (request.exists() && StreamerPublicationPolicy.isExpired(data)) {
                val rejected = data + mapOf("status" to "REJECTED", "rejectionReason" to "TIMEOUT",
                    "reviewedAtMillis" to StreamerPublicationPolicy.expiresAt(data))
                if (archive) {
                    if (!StreamerPublicationPolicy.historyExpired(data))
                        transaction.set(history(uid).document(StreamerPublicationPolicy.publicationId(data)), rejected)
                    transaction.delete(ref)
                } else transaction.update(ref, rejected)
            }
        }.await()
        Unit
    } }

    /** Upgrade published legacy entries and initialise missing counters without resetting existing clicks. */
    suspend fun repairMetrics(): Result<Unit> = withContext(Dispatchers.IO) { runCatching {
        check(SupportTicketAccess.isAdmin())
        db.runTransaction { tx ->
            val snapshot = tx.get(registry)
            val live = entries(snapshot.get("entries"))
            val legacyRequests = live.associate { entry -> entry.getValue("userId") to tx.get(requests.document(entry["userId"] as String)).data.orEmpty() }
            val upgraded = live.map { entry ->
                if ((entry["publicationId"] as? String).isNullOrBlank()) {
                    val request = legacyRequests[entry["userId"]].orEmpty()
                    val id = if (request["status"] == "APPROVED" && request["channelUrl"] == entry["channelUrl"])
                        StreamerPublicationPolicy.publicationId(request) else "legacy_${entry["userId"]}_${entry["approvedAtMillis"]}"
                    entry + ("publicationId" to id)
                } else entry
            }
            val missing = upgraded.map { entry -> entry to tx.get(metrics.document(entry["publicationId"] as String)) }.filterNot { it.second.exists() }
            if (live != upgraded) tx.set(registry, mapOf("entries" to upgraded), SetOptions.merge())
            missing.forEach { (entry, _) ->
                val id = entry["publicationId"] as String
                tx.set(metrics.document(id), mapOf("userId" to entry.getValue("userId"), "publicationId" to id,
                    "submittedAtMillis" to ((entry["approvedAtMillis"] as? Number)?.toLong() ?: System.currentTimeMillis()),
                    "clickCount" to 0L, "status" to "APPROVED", "countingStartedAtMillis" to System.currentTimeMillis()))
            }
        }.await()
        Unit
    } }

    /** An anonymous event has no identity data. The event and increment commit atomically. */
    suspend fun recordClick(publicationId: String, eventId: String): Result<Unit> = withContext(Dispatchers.IO) { runCatching {
        require(publicationId.isNotBlank() && eventId.isNotBlank())
        val metric = metrics.document(publicationId)
        val event = metric.collection("click_events").document(eventId)
        if (!event.get(com.google.firebase.firestore.Source.SERVER).await().exists()) {
            try {
                val batch = db.batch()
                batch.set(event, mapOf("publicationId" to publicationId,
                    "clickedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    "deleteAt" to com.google.firebase.Timestamp(java.util.Date(System.currentTimeMillis() + 8 * 86_400_000L))))
                batch.update(metric, mapOf("clickCount" to com.google.firebase.firestore.FieldValue.increment(1L),
                    "lastClickId" to eventId, "lastClickedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()))
                batch.commit().await()
            } catch (error: Exception) {
                // A concurrent retry may have already committed this exact event.
                if (!event.get(com.google.firebase.firestore.Source.SERVER).await().exists()) throw error
            }
        }
        Unit
    } }

    suspend fun pruneHistory(uid: String): Result<Unit> = withContext(Dispatchers.IO) { runCatching {
        val now = System.currentTimeMillis()
        val rows = history(uid).get(com.google.firebase.firestore.Source.SERVER).await().documents
        val counters = if (metricsAvailable(uid)) metrics.whereEqualTo("userId", uid)
            .get(com.google.firebase.firestore.Source.SERVER).await().documents else emptyList()
        val old = (rows + counters).filter { StreamerPublicationPolicy.historyExpired(it.data.orEmpty(), now) }
        old.chunked(400).forEach { records ->
            val batch = db.batch()
            records.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
        Unit
    } }

}
