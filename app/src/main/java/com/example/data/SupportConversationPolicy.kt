package com.example.data

import java.util.Locale

object SupportConversationPolicy {
    const val SYSTEM_GREETING = "Hola. El sistema ha recibido tu mensaje. El equipo de Coach te responderá aquí."
    fun isSponsor(tag: String): Boolean = tag.trim().uppercase(Locale.ROOT) in setOf("PATROCINADOR", "PATROCINIO", "SPONSOR", "PUBLICIDAD")
    fun isClosed(status: String): Boolean = status.trim().uppercase(Locale.ROOT) in setOf("SOLVED", "SOLUCIONADO", "RESUELTO", "CLOSED", "CERRADO", "COMPLETED", "COMPLETADO")
    fun canView(role: String, tag: String): Boolean = role == "admin" || (role == "moderador" && !isSponsor(tag))
    fun userHasRead(data: Map<String, Any>): Boolean {
        if (data["userRead"] is Boolean) return data["userRead"] == true && data["hasNewAdminReply"] != true
        return data["isRead"] == true && data["hasNewAdminReply"] != true
    }
    fun initial(reportId: String, userName: String, text: String, timestamp: Long): List<SupportMessageEntry> = listOf(
        SupportMessageEntry(id = "${reportId}_initial", senderName = userName, senderRole = "USER", text = text, timestampMillis = timestamp),
        SupportMessageEntry(id = "${reportId}_system", senderName = "Sistema Coach", senderRole = "SYSTEM", text = SYSTEM_GREETING, timestampMillis = timestamp + 1, isGreeting = true)
    )
    fun encode(message: SupportMessageEntry, senderUid: String = ""): Map<String, Any> = mapOf(
        "id" to message.id, "senderName" to message.senderName, "senderRole" to message.senderRole,
        "senderEmail" to message.senderEmail.orEmpty(), "senderUid" to senderUid,
        "text" to message.text, "timestampMillis" to message.timestampMillis, "isGreeting" to message.isGreeting
    )
    fun decode(value: Any?): List<SupportMessageEntry> = (value as? List<*>)?.mapNotNull { raw ->
        val row = raw as? Map<*, *> ?: return@mapNotNull null
        val text = row["text"] as? String ?: return@mapNotNull null
        SupportMessageEntry(id = row["id"] as? String ?: "legacy_${row["timestampMillis"]}_${text.hashCode()}",
            senderName = row["senderName"] as? String ?: "", senderRole = row["senderRole"] as? String ?: "SUPPORT",
            senderEmail = (row["senderEmail"] as? String)?.takeIf { it.isNotBlank() }, text = text,
            timestampMillis = (row["timestampMillis"] as? Number)?.toLong() ?: 0L, isGreeting = row["isGreeting"] == true)
    }.orEmpty()
}
