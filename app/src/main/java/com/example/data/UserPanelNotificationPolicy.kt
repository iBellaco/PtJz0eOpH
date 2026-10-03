package com.example.data

object UserPanelNotificationPolicy {
    fun staffNeedsAttention(data: Map<String, Any>, admin: Boolean): Boolean {
        if (!admin && SupportConversationPolicy.isAdministratorOnly(SupportConversationPolicy.ticketTag(data))) return false
        val status = (data["status"] as? String).orEmpty().uppercase(java.util.Locale.ROOT)
        if (SupportConversationPolicy.isClosed(status) || status in setOf("DELETED", "ELIMINADO", "ACCEPTED", "ACEPTADO", "REJECTED", "RECHAZADO")) return false
        if (data["hasNewUserReply"] == true) return true
        if (data["staffRead"] is Boolean) return data["staffRead"] == false
        return status in setOf("PENDING", "PENDIENTE", "UNREAD")
    }
}
