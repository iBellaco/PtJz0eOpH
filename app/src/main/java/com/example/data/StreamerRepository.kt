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
    @Suppress("UNCHECKED_CAST")
    fun entries(value: Any?): List<Map<String, Any>> = (value as? List<*>)?.mapNotNull { it as? Map<String, Any> }.orEmpty()
    private fun hasRole(role: String?, secondary: String?, adminClaim: Boolean = false) =
        com.example.model.RolePanelAccess.canOpen(com.example.model.RolePanel.STREAMER, role.orEmpty(), secondary.orEmpty(), adminClaim)

    suspend fun submit(name: String, rawUrl: String): Result<Unit> = withContext(Dispatchers.IO) { runCatching {
        val user = FirebaseAuth.getInstance().currentUser ?: error("streamer_error")
        check(name.trim().length in 2..60) { "streamer_name_error" }
        val adminClaim = com.example.util.AuthManager.isAdminClaim.value
        val ref = requests.document(user.uid)
        db.runTransaction { transaction ->
            val live = entries(transaction.get(registry).get("entries"))
            val current = transaction.get(ref)
            val account = transaction.get(db.collection("users").document(user.uid))
            val isAdmin = com.example.model.RolePanelAccess.isAdministrator(account.getString("role").orEmpty(), adminClaim)
            check(hasRole(account.getString("role"), account.getString("secondaryRole"), adminClaim)) { "streamer_role_error" }
            val channel = StreamChannelUrl.parse(rawUrl, allowAdminTest = isAdmin) ?: error("streamer_url_error")
            check(StreamerPublicationPolicy.canRequest(live, user.uid)) { "streamer_max" }
            check(current.getString("status") != "PENDING") { "streamer_pending" }
            val fields = mutableMapOf<String, Any>("userId" to user.uid, "userName" to (account.getString("userName") ?: user.displayName.orEmpty()),
                "channelName" to name.trim(), "channelUrl" to channel.url, "platform" to channel.platform,
                "status" to "PENDING", "usingCoachAcknowledged" to true, "submittedAtMillis" to System.currentTimeMillis())
            if (isAdmin) fields["adminTest"] = channel.platform == "Google"
            transaction.set(ref, fields)
        }.await()
        Unit
    } }

    suspend fun review(uid: String, approve: Boolean, verifiedUsingCoach: Boolean): Result<Unit> = withContext(Dispatchers.IO) { runCatching {
        check(SupportTicketAccess.isAdmin()) { "streamer_error" }
        val ref = requests.document(uid)
        db.runTransaction { transaction ->
            val request = transaction.get(ref)
            val live = entries(transaction.get(registry).get("entries"))
            val account = transaction.get(db.collection("users").document(uid))
            check(request.getString("status") == "PENDING") { "streamer_error" }
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
            transaction.update(ref, mapOf("status" to if (approve) "APPROVED" else "REJECTED",
                "verifiedUsingCoach" to (approve && verifiedUsingCoach), "reviewedAtMillis" to System.currentTimeMillis()))
        }.await()
        Unit
    } }

    suspend fun end(uid: String): Result<Unit> = withContext(Dispatchers.IO) { runCatching {
        val user = FirebaseAuth.getInstance().currentUser ?: error("streamer_error")
        check(user.uid == uid || SupportTicketAccess.isAdmin()) { "streamer_error" }
        val ref = requests.document(uid)
        db.runTransaction { transaction ->
            val live = entries(transaction.get(registry).get("entries"))
            transaction.get(ref)
            transaction.set(registry, mapOf("entries" to live.filterNot { it["userId"] == uid }), SetOptions.merge())
            transaction.update(ref, mapOf("status" to "ENDED", "endedAtMillis" to System.currentTimeMillis()))
        }.await()
        Unit
    } }
}
