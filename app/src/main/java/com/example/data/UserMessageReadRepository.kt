package com.example.data

import com.example.util.AuthManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/** Update existing mirrors together. Never create an empty message or overwrite a new reply. */
object UserMessageReadRepository {
    suspend fun mark(uid: String, id: String, observed: Map<String, Any>): Boolean {
        check(AuthManager.getAuth()?.currentUser?.uid == uid) { "Inicia sesión" }
        val db = FirebaseFirestore.getInstance()
        val profile = db.collection("users").document(uid)
        val reportId = (observed["reportId"] as? String).orEmpty()
        val ids = setOf(id, reportId).filter(String::isNotBlank)
        return db.runTransaction { tx ->
            val account = tx.get(profile)
            val documents = ids.map { tx.get(profile.collection("messages").document(it)) }
            val originals = (account.get("privateMessages") as? List<*>).orEmpty().filterIsInstance<Map<String, Any>>()
            val targets = documents.filter { it.exists() }.map { it.data.orEmpty() } + originals.filter { it["id"] in ids }
            check(targets.isNotEmpty()) { "Mensaje no disponible" }
            if (targets.any { PanelReadRepository.revision(it) != PanelReadRepository.revision(observed) }) return@runTransaction false
            val update = mapOf("isRead" to true, "userRead" to true, "hasNewAdminReply" to false, "hasNewReply" to false)
            documents.filter { it.exists() }.forEach { tx.update(it.reference, update) }
            val updated = originals.map { if (it["id"] in ids || (reportId.isNotBlank() && it["reportId"] == reportId)) it + update else it }
            if (updated != originals) tx.update(profile, mapOf("privateMessages" to updated,
                "hasUnreadMessages" to updated.any { it["isRead"] == false },
                "unreadMessagesCount" to updated.count { it["isRead"] == false }))
            true
        }.await()
    }
}
