package com.example.data

import java.util.Locale

/** Shared filtering and legacy metadata cleanup; never deduplicate by subject. */
object SupportTicketPresentation {
    fun isUserTicket(data: Map<String, Any>): Boolean =
        data["isDeleted"] != true && data["deleted"] != true &&
            (data["status"] as? String).orEmpty().uppercase(Locale.ROOT) !in setOf("ELIMINADO", "DELETED") &&
            (data["category"] as? String).orEmpty().uppercase(Locale.ROOT) != "MODERATOR_REQUEST" &&
            (data["type"] as? String).orEmpty().uppercase(Locale.ROOT) !in setOf("MODERATOR_REQUEST", "VERIFICATION_REQUEST", "ROLE_REQUEST")

    fun cleanBody(text: String): String {
        // Old versions injected identity/contact lines, sometimes twice and in either order.
        // Remove only a leading metadata block, leaving the user's actual message intact.
        val header = Regex("^(?:Usuario|Usuário|Invocador|Correo de contacto|E-mail de contato):[^\\r\\n]*(?:\\r?\\n|$)", RegexOption.IGNORE_CASE)
        var body = text.trimStart()
        while (header.containsMatchIn(body)) body = body.replaceFirst(header, "").trimStart()
        return body.trim()
    }

    fun status(raw: String): String = when (raw.uppercase(Locale.ROOT)) {
        "SOLVED", "SOLUCIONADO", "RESUELTO", "CLOSED", "CERRADO", "COMPLETED", "COMPLETADO" -> "SOLVED"
        "READ", "LEIDO", "LEÍDO" -> "READ"
        "ACCEPTED", "ACEPTADA", "ACEPTADO" -> "ACCEPTED"
        "REJECTED", "RECHAZADA", "RECHAZADO" -> "REJECTED"
        else -> "PENDING"
    }
}
