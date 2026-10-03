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

    suspend fun submit(name: String, rawUrl: String): Result<Unit> = withContext(Dispatchers.IO) { runCatching {
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
            val fields = mutableMapOf<String, Any>("userId" to user.uid, "userName" to (account.getString("userName") ?: user.displayName.orEmpty()),
                "channelName" to name.trim(), "channelUrl" to channel.url, "platform" to channel.platform,
                "status" to "PENDING", "usingCoachAcknowledged" to true, "submittedAtMillis" to now)
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
        val archive = historyAvailable(uid)
        db.runTransaction { transaction ->
            val request = transaction.get(ref)
            val live = entries(transaction.get(registry).get("entries"))
            val account = transaction.get(db.collection("users").document(uid))
            check(request.getString("status") == "PENDING") { "streamer_error" }
            check(!StreamerPublicationPolicy.isExpired(request.data.orEmpty())) { "streamer_expired" }
            if (approve) {
                check(verifiedUsingCoach && request.getBoolean("usingCoachAcknowledged") == true) { "streamer_requirement" }
                // Only a trusted staff writer can create adminTest; ordinary requests forbid this field.
                val trustedTest = request.getBoolean("adminTest") == true
                check(hasRole(account.getString("role"), account.getString("secondaryRole"), trustedTest)) { "streamer_role_error" }
                val channel = StreamChannelUrl.parse(request.getString("channelUrl").orEmpty(),
                    allowAdminTest = trustedTest) ?: error("streamer_url_error")
                val entry = mapOf<String, Any>("userId" to uid, "channelName" to request.getString("channelName").orEmpty(),
                    "channelUrl" to channel.url, "platform" to channel.platform, "approvedAtMillis" to System.currentTimeMillis(),
                    "publicationId" to StreamerPublicationPolicy.publicationId(request.data.orEmpty()))
                transaction.set(registry, mapOf("entries" to StreamerPublicationPolicy.approve(live, entry)), SetOptions.merge())
                transaction.set(metrics.document(StreamerPublicationPolicy.publicationId(request.data.orEmpty())),
                    mapOf("userId" to uid, "publicationId" to StreamerPublicationPolicy.publicationId(request.data.orEmpty()),
                        "submittedAtMillis" to StreamerPublicationPolicy.submittedAt(request.data.orEmpty()), "clickCount" to 0L,
                        "status" to "APPROVED"))
            }
            val reviewedAt = System.currentTimeMillis()
            val reviewed = mutableMapOf<String, Any>("status" to if (approve) "APPROVED" else "REJECTED",
                "verifiedUsingCoach" to (approve && verifiedUsingCoach), "reviewedAtMillis" to reviewedAt)
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
                    "clickCount" to 0L, "status" to "APPROVED"))
            }
        }.await()
        Unit
    } }

    /** Count successful channel-open actions; no visitor identity is stored and no login is required. */
    suspend fun recordClick(entry: Map<String, Any>): Result<Unit> = withContext(Dispatchers.IO) { runCatching {
        val id = (entry["publicationId"] as? String)?.takeIf { it.isNotBlank() } ?: return@runCatching
        metrics.document(id).update(mapOf("clickCount" to com.google.firebase.firestore.FieldValue.increment(1L),
            "lastClickedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp())).await()
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
