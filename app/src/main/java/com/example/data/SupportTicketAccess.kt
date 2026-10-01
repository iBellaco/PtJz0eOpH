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
        var batch = db.batch()
        var updates = 0
        documents.forEach { doc ->
            val patch = mutableMapOf<String, Any>()
            val visible = !SupportConversationPolicy.isSponsor(doc.getString("tag") ?: doc.getString("type").orEmpty())
            if (doc.getBoolean("staffVisible") != visible) patch["staffVisible"] = visible
            var owner = doc.getString("userId").orEmpty()
            if (owner.isBlank() && !doc.getString("userEmail").isNullOrBlank()) {
                owner = db.collection("users").whereEqualTo("email", doc.getString("userEmail")).limit(1).get().await().documents.firstOrNull()?.id.orEmpty()
                if (owner.isNotBlank()) patch["userId"] = owner
            }
            if (SupportConversationPolicy.decode(doc.get("conversation")).isEmpty()) {
                val initial = SupportConversationPolicy.initial(doc.id, doc.getString("userName") ?: "Invocador", doc.getString("description").orEmpty(), doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L)
                val legacyReply = doc.getString("adminReply").orEmpty()
                val history = if (legacyReply.isBlank()) initial else initial + SupportMessageEntry(id = "${doc.id}_legacy_reply", senderName = doc.getString("repliedBy") ?: "Soporte Coach", senderRole = "SUPPORT", text = legacyReply, timestampMillis = doc.getTimestamp("repliedAt")?.toDate()?.time ?: 0L)
                patch["conversation"] = history.map { SupportConversationPolicy.encode(it, if (it.senderRole == "USER") owner else "") }
            }
            if (patch.isNotEmpty()) {
                batch.update(doc.reference, patch); updates++
                if (updates == 400) { batch.commit().await(); batch = db.batch(); updates = 0 }
            }
        }
        if (updates > 0) batch.commit().await()
        migratedForUid = uid
    }
}
