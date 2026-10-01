package com.example.data

import com.example.util.AuthManager
import com.example.util.SubscriptionManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object SupportTicketAccess {
    private val migrationMutex = Mutex()
    private var migratedForUid: String? = null
    fun isAdmin() = SubscriptionManager.userRole.value == "admin" || AuthManager.isCurrentUserAdmin()
    fun staffQuery(): Query {
        val collection = FirebaseFirestore.getInstance().collection("support_reports")
        return if (isAdmin()) collection else collection.whereEqualTo("staffVisible", true)
    }
    /** Admin backfill gives existing tickets the same visibility as new tickets. */
    suspend fun migrateLegacyVisibility() = migrationMutex.withLock {
        if (!isAdmin()) return@withLock
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@withLock
        if (migratedForUid == uid) return@withLock
        val db = FirebaseFirestore.getInstance()
        val documents = db.collection("support_reports").get().await().documents
        documents.forEach { doc ->
            if (!SupportTicketPresentation.isUserTicket(doc.data.orEmpty())) return@forEach
            val visible = !SupportConversationPolicy.isSponsor(doc.getString("tag") ?: doc.getString("type").orEmpty())
            var owner = doc.getString("userId").orEmpty()
            if (owner.isBlank() && !doc.getString("userEmail").isNullOrBlank()) {
                owner = db.collection("users").whereEqualTo("email", doc.getString("userEmail")).limit(1).get().await().documents.firstOrNull()?.id.orEmpty()
            }
            if (doc.getBoolean("userCanReply") == null || doc.getBoolean("staffVisible") != visible || (doc.getString("userId").isNullOrBlank() && owner.isNotBlank()) || SupportConversationPolicy.decode(doc.get("conversation")).isEmpty()) {
                db.runTransaction { transaction ->
                    val latest = transaction.get(doc.reference)
                    if (latest.exists()) {
                        val patch = mutableMapOf<String, Any>()
                        val latestVisible = !SupportConversationPolicy.isSponsor(latest.getString("tag") ?: latest.getString("type").orEmpty())
                        if (latest.getBoolean("staffVisible") != latestVisible) patch["staffVisible"] = latestVisible
                        val latestOwner = latest.getString("userId").orEmpty().ifBlank { owner }
                        if (latest.getString("userId").isNullOrBlank() && latestOwner.isNotBlank()) patch["userId"] = latestOwner
                        if (SupportConversationPolicy.decode(latest.get("conversation")).isEmpty()) {
                            val history = SupportConversationPolicy.initial(doc.id, latest.getString("userName") ?: "Invocador",
                                latest.getString("description").orEmpty(), latest.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
                                latest.getString("adminReply").orEmpty(), latest.getString("repliedBy") ?: "Soporte Coach",
                                latest.getTimestamp("repliedAt")?.toDate()?.time ?: 0L)
                            patch["conversation"] = history.map { SupportConversationPolicy.encode(it, if (it.senderRole == "USER") latestOwner else "") }
                        }
                        val history = SupportConversationPolicy.decode(patch["conversation"] ?: latest.get("conversation"))
                        if (latest.getBoolean("userCanReply") == null) patch["userCanReply"] = SupportConversationPolicy.hasStaffAnswer(history)
                        if (patch.isNotEmpty()) transaction.update(doc.reference, patch)
                    }
                }.await()
            }
        }
        migratedForUid = uid
    }
}
