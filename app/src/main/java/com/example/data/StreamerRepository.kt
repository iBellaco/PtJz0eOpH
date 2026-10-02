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
    fun history(uid: String) = requests.document(uid).collection("history")
    private suspend fun historyAvailable(uid: String): Boolean = try {
        history(uid).limit(1).get(com.google.firebase.firestore.Source.SERVER).await()
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
            if (archive && current.exists()) {
                val previous = prior.toMutableMap()
                if (StreamerPublicationPolicy.isExpired(prior, now)) previous.putAll(mapOf("status" to "REJECTED",
                    "rejectionReason" to "TIMEOUT", "reviewedAtMillis" to StreamerPublicationPolicy.expiresAt(prior)))
                transaction.set(history(user.uid).document(StreamerPublicationPolicy.publicationId(prior)), previous)
            }
            val fields = mutableMapOf<String, Any>("userId" to user.uid, "userName" to (account.getString("userName") ?: user.displayName.orEmpty()),
                "channelName" to name.trim(), "channelUrl" to channel.url, "platform" to channel.platform,
                "status" to "PENDING", "usingCoachAcknowledged" to true, "submittedAtMillis" to now)
            if (archive) fields.putAll(mapOf("submittedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(), "publicationId" to publicationId))
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
                    "channelUrl" to channel.url, "platform" to channel.platform, "approvedAtMillis" to System.currentTimeMillis())
                transaction.set(registry, mapOf("entries" to StreamerPublicationPolicy.approve(live, entry)), SetOptions.merge())
            }
            val reviewed = mapOf("status" to if (approve) "APPROVED" else "REJECTED",
                "verifiedUsingCoach" to (approve && verifiedUsingCoach), "reviewedAtMillis" to System.currentTimeMillis())
            transaction.update(ref, reviewed)
            if (archive) transaction.set(history(uid).document(StreamerPublicationPolicy.publicationId(request.data.orEmpty())), request.data.orEmpty() + reviewed)
        }.await()
        Unit
    } }

    suspend fun end(uid: String): Result<Unit> = withContext(Dispatchers.IO) { runCatching {
        val user = FirebaseAuth.getInstance().currentUser ?: error("streamer_error")
        check(user.uid == uid || SupportTicketAccess.isAdmin()) { "streamer_error" }
        val ref = requests.document(uid)
        val archive = historyAvailable(uid)
        db.runTransaction { transaction ->
            val live = entries(transaction.get(registry).get("entries"))
            val request = transaction.get(ref)
            transaction.set(registry, mapOf("entries" to live.filterNot { it["userId"] == uid }), SetOptions.merge())
            if (request.exists()) {
                val ended = mapOf("status" to "ENDED", "endedAtMillis" to System.currentTimeMillis())
                transaction.update(ref, ended)
                if (archive) transaction.set(history(uid).document(StreamerPublicationPolicy.publicationId(request.data.orEmpty())), request.data.orEmpty() + ended)
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
                    transaction.set(history(uid).document(StreamerPublicationPolicy.publicationId(data)), rejected)
                    transaction.delete(ref)
                } else transaction.update(ref, rejected)
            }
        }.await()
        Unit
    } }

}
