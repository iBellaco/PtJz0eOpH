package com.example.data

import com.example.util.AuthManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/** Acknowledging a queue item never approves it or closes a conversation. */
object PanelReadRepository {
    private val _read = MutableStateFlow<Set<String>>(emptySet())
    val read = _read.asStateFlow()
    private val _versions = MutableStateFlow<Map<String, String>>(emptyMap())
    val versions = _versions.asStateFlow()
    fun key(event: String, revision: String = "") = java.security.MessageDigest.getInstance("SHA-256")
        .digest("$event:$revision".toByteArray()).joinToString("") { "%02x".format(it) }
    fun revision(data: Map<String, Any>): String {
        val last = (data["conversation"] as? List<*>)?.filterIsInstance<Map<String, Any>>()
            ?.lastOrNull { it["senderRole"] != "SYSTEM" }
        val id = last?.get("id") as? String
        if (!id.isNullOrBlank()) return id
        if (data["paymentCurrency"] == "USDT") return "payment_${data["id"]}_initial"
        return listOf(data["publicationId"], data["submittedAtMillis"], data["requestedAtMillis"], data["lastMessageAt"],
            data["lastUserMessageAt"], data["lastUserMessage"], data["timestamp"]).joinToString("|")
    }
    fun updateRead(keys: Set<String>) { _read.value = keys }
    fun updateVersions(values: Map<String, String>) { _versions.value = values }
    fun isRead(event: String, revision: String = "") = key(event, revision) in _read.value

    suspend fun acknowledge(event: String, revision: String = _versions.value[event].orEmpty()) {
        val user = AuthManager.getAuth()?.currentUser ?: return
        val id = key(event, revision)
        FirebaseFirestore.getInstance().collection("users").document(user.uid).collection("panel_reads").document(id)
            .set(mapOf("event" to event, "revision" to revision, "readAt" to FieldValue.serverTimestamp())).await()
        _read.value = _read.value + id
    }

    suspend fun markPanelRead(panel: NotificationPanel, events: Set<String>) {
        val uid = AuthManager.getAuth()?.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val profile = db.collection("users").document(uid)
        val documents = profile.collection("messages").get().await()
        for (document in documents.documents) {
            val message = document.data.orEmpty() + ("id" to document.id)
            if (panel in PanelNotificationPolicy.messagePanels(message) && message["isRead"] == false) {
                val reportId = (message["reportId"] as? String)?.takeIf { it.isNotBlank() }
                if (reportId != null || message.containsKey("conversation")) {
                    val observed = ((message["conversation"] as? List<*>)?.lastOrNull() as? Map<*, *>)?.get("id") as? String
                    check(SupportReplyManager.markUserRead(reportId ?: document.id, observed))
                } else document.reference.update("isRead", true).await()
            }
        }
        // Legacy mirrored notices use the same canonical routes as the inbox.
        db.runTransaction { tx ->
            val snapshot = tx.get(profile)
            val messages = (snapshot.get("privateMessages") as? List<*>).orEmpty().filterIsInstance<Map<String, Any>>()
            val updated = messages.map { if (panel in PanelNotificationPolicy.messagePanels(it) && it["isRead"] == false) it + ("isRead" to true) else it }
            if (messages != updated) tx.update(profile, "privateMessages", updated)
        }.await()
        events.filter { it.startsWith("notice:") || it in _versions.value }.forEach { acknowledge(it) }
    }
}
