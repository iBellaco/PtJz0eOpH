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
        val user = AuthManager.getAuth()?.currentUser ?: error("Inicia sesión")
        val id = key(event, revision)
        val profile = FirebaseFirestore.getInstance().collection("users").document(user.uid)
        FirebaseFirestore.getInstance().runTransaction { tx ->
            val snapshot = tx.get(profile)
            check(snapshot.exists()) { "Usuario no disponible" }
            val previous = (snapshot.get("panelReadKeys") as? List<*>)?.filterIsInstance<String>().orEmpty()
            tx.update(profile, "panelReadKeys", (previous.filterNot { it == id } + id).takeLast(512))
        }.await()
        _read.value = _read.value + id
    }

    suspend fun markPanelRead(panel: NotificationPanel, events: Set<String>) {
        val uid = AuthManager.getAuth()?.currentUser?.uid ?: error("Inicia sesión")
        val observedVersions = _versions.value
        val db = FirebaseFirestore.getInstance()
        val profile = db.collection("users").document(uid)
        val documents = profile.collection("messages").get().await()
        for (document in documents.documents) {
            val message = document.data.orEmpty() + ("id" to document.id)
            if (panel in PanelNotificationPolicy.messagePanels(message) && message["isRead"] == false) {
                val reportId = (message["reportId"] as? String)?.takeIf { it.isNotBlank() }
                if (message.containsKey("conversation")) {
                    val observed = ((message["conversation"] as? List<*>)?.lastOrNull() as? Map<*, *>)?.get("id") as? String
                    check(SupportReplyManager.markUserRead(reportId ?: document.id, observed))
                } else check(UserMessageReadRepository.mark(uid, document.id, message))
            }
        }
        val legacySnapshot = profile.get().await()
        val legacy = (legacySnapshot.get("privateMessages") as? List<*>).orEmpty().filterIsInstance<Map<String, Any>>()
        for (message in legacy.filter { panel in PanelNotificationPolicy.messagePanels(it) && it["isRead"] == false }) {
            val id = (message["id"] as? String)?.takeIf(String::isNotBlank) ?: continue
            if (message.containsKey("conversation")) {
                val report = (message["reportId"] as? String)?.takeIf(String::isNotBlank) ?: id
                check(SupportReplyManager.markUserRead(report, SupportConversationPolicy.decode(message["conversation"]).lastOrNull()?.id))
            } else check(UserMessageReadRepository.mark(uid, id, message))
        }
        events.forEach { acknowledge(it, observedVersions[it].orEmpty()) }
    }
}
