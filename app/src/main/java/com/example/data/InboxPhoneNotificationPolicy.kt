package com.example.data

/** Collapse legacy/current copies before testing unread state; content revisions notify once. */
object InboxPhoneNotificationPolicy {
    fun supportMessage(id: String, data: Map<String, Any>): Map<String, Any> {
        val status = (data["status"] as? String).orEmpty().uppercase(java.util.Locale.ROOT)
        val closed = status in setOf("SOLUCIONADO", "CERRADO", "CLOSED", "RESUELTO", "ELIMINADO", "DELETED")
        val last = (data["conversation"] as? List<*>)?.lastOrNull() as? Map<*, *>
        val sender = (last?.get("senderRole") as? String).orEmpty().uppercase(java.util.Locale.ROOT)
        val staffAnswer = sender in setOf("SUPPORT", "ADMIN", "MODERADOR") ||
            (data["adminReply"] as? String).orEmpty().isNotBlank()
        val unread = !closed && (data["hasNewAdminReply"] == true || data["hasNewReply"] == true ||
            (data["userRead"] != true && staffAnswer && (data["userRead"] == false || data["isRead"] == false)))
        return data + mapOf("id" to id, "reportId" to id, "isRead" to !unread)
    }

    fun candidates(messages: List<Map<String, Any>>): Map<String, String> = messages
        .mapNotNull { message -> InboxNotificationPolicy.key(message)?.let { it to message } }
        .groupBy({ it.first }, { it.second })
        .mapValues { (_, copies) -> copies.sortedBy { InboxMessageOrder.latestMessageAt(it) }.last() }
        .filterValues { message -> InboxNotificationPolicy.unreadKeys(listOf(message)).isNotEmpty() }
        .mapValues { (key, message) -> PanelReadRepository.key(key,
            PanelReadRepository.revision(message) + "|" + listOf(message["title"], message["body"], message["content"], message["message"], message["adminReply"]).joinToString("|")) }
}
