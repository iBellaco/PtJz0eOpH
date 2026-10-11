package com.example.data

import com.example.util.AuthManager
import com.example.util.appTr
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/** Delete both legacy and current copies atomically; success means the server committed. */
object UserMessageDeleteRepository {
    fun remaining(messages: List<Map<String, Any>>, ids: Set<String>) = messages.filterNot {
        it["id"] in ids || it["reportId"] in ids
    }

    suspend fun delete(uid: String, id: String, reportId: String = "") {
        val actor = AuthManager.getAuth()?.currentUser ?: error(appTr("Inicia sesión"))
        check(actor.getIdToken(true).await().claims["admin"] == true) { appTr("No tienes permisos para esta operación") }
        val db = FirebaseFirestore.getInstance()
        val ids = setOf(id, reportId).filter(String::isNotBlank).toSet()
        require(uid.isNotBlank() && ids.isNotEmpty())
        db.runTransaction { tx ->
            val profile = db.collection("users").document(uid)
            val snapshot = tx.get(profile)
            val legacy = (snapshot.get("privateMessages") as? List<*>).orEmpty().filterIsInstance<Map<String, Any>>()
            val reports = ids.map { tx.get(db.collection("support_reports").document(it)) }
            val updated = remaining(legacy, ids)
            reports.filter { it.exists() && it.getString("userId") == uid }.forEach { tx.delete(it.reference) }
            ids.forEach { tx.delete(profile.collection("messages").document(it)) }
            tx.update(profile, mapOf("privateMessages" to updated,
                "hasUnreadMessages" to InboxNotificationPolicy.unreadKeys(updated).isNotEmpty(),
                "unreadMessagesCount" to InboxNotificationPolicy.unreadKeys(updated).size))
        }.await()
    }
}
