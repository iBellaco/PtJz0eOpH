package com.example.data

/** Mirrors the inbox's canonical conversation IDs across its legacy and current sources. */
object InboxNotificationPolicy {
    fun key(message: Map<String, Any>): String? {
        val id = (message["id"] as? String).orEmpty()
        val reportId = (message["reportId"] as? String).orEmpty()
        val support = FeedbackRepository.isSupportMessage(message) || reportId.isNotBlank() || message["conversation"] != null ||
            (message["type"] as? String).orEmpty().uppercase(java.util.Locale.ROOT) in setOf("SOPORTE", "SUPPORT")
        val canonical = reportId.ifBlank { id }
        return canonical.takeIf { it.isNotBlank() }?.let { (if (support) "support:" else "message:") + it }
    }

    fun unreadKeys(messages: List<Map<String, Any>>): Set<String> = messages.filter {
        it["isRead"] == false && it["isDeleted"] != true && it["deleted"] != true &&
            (it["status"] as? String).orEmpty().uppercase(java.util.Locale.ROOT) !in setOf("DELETED", "ELIMINADO")
    }.mapNotNull(::key).toSet()
}
