package com.example.data

import com.example.data.remote.model.FeedbackReport
import com.google.firebase.Timestamp
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Reads current and legacy tickets without inventing dates or empty tickets. */
object SupportReportDecoder {
    fun timestampMillis(value: Any?): Long? {
        val millis = when (value) {
            is Timestamp -> value.toDate().time
            is Date -> value.time
            is Number -> value.toLong().let { if (it in 1..9_999_999_999L) it * 1000 else it }
            is String -> value.trim().let { text ->
                text.toLongOrNull()?.let { return timestampMillis(it) }
                val normalized = text.replace(Regex("\\.(\\d+)(?=Z|[+-]\\d{2}:?\\d{2}|$)")) {
                    "." + it.groupValues[1].take(3).padEnd(3, '0')
                }
                listOf("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", "yyyy-MM-dd'T'HH:mm:ssXXX",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ss").firstNotNullOfOrNull { pattern ->
                    val parser = SimpleDateFormat(pattern, Locale.US).apply {
                        isLenient = false
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    val position = ParsePosition(0)
                    parser.parse(normalized, position)?.time?.takeIf { position.index == normalized.length }
                }
            }
            is Map<*, *> -> ((value["seconds"] ?: value["_seconds"]) as? Number)?.toLong()?.let { seconds ->
                seconds * 1000 + (((value["nanoseconds"] ?: value["_nanoseconds"]) as? Number)?.toLong() ?: 0) / 1_000_000
            }
            else -> null
        }
        return millis?.takeIf { it > 0 }
    }

    fun isoDate(millis: Long?): String? = millis?.takeIf { it > 0 }?.let {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(it))
    }

    fun decode(id: String, data: Map<String, Any>): FeedbackReport? {
        if (!SupportTicketPresentation.isUserTicket(data)) return null
        fun text(vararg keys: String): String = keys.firstNotNullOfOrNull { key ->
            (data[key] as? String)?.takeIf { it.isNotBlank() }
        }.orEmpty()
        val history = SupportConversationPolicy.decode(data["conversation"])
        val initial = history.firstOrNull { it.senderRole.equals("USER", true) && it.text.isNotBlank() }
        val title = text("title", "subject", "titulo", "asunto")
        val body = text("description", "content", "message", "body", "descripcion", "mensaje").ifBlank { initial?.text.orEmpty() }
        // A read/status synchronization document is not a message by itself.
        if (title.isBlank() && body.isBlank() && history.isEmpty()) return null
        fun date(vararg keys: String): String? = isoDate(keys.firstNotNullOfOrNull { timestampMillis(data[it]) })
        val created = date("createdAt", "created_at", "timestamp", "timestampMillis", "date", "fecha")
            ?: isoDate(initial?.timestampMillis)
        val reply = text("adminReply", "admin_reply").ifBlank {
            history.lastOrNull { it.senderRole.uppercase(java.util.Locale.ROOT) in setOf("SUPPORT", "ADMIN", "MODERATOR", "MODERADOR") && !it.isGreeting }?.text.orEmpty()
        }
        return FeedbackReport(
            id = id, userId = text("userId", "user_id", "senderUid"),
            userName = text("userName", "user_name", "senderName").ifBlank { initial?.senderName.orEmpty() },
            userEmail = text("contactEmail", "userEmail", "user_email", "email"),
            type = SupportConversationPolicy.ticketTag(data),
            title = title.ifBlank { "Ticket de soporte" }, description = body,
            photosBase64 = ((data["photos"] ?: data["photosBase64"]) as? List<*>)?.filterIsInstance<String>().orEmpty(),
            appVersion = text("appVersion", "app_version", "version"),
            deviceInfo = text("deviceInfo", "device_info", "device", "dispositivo"),
            createdAt = created, status = SupportTicketPresentation.status(text("status")),
            isCompleted = (data["isCompleted"] ?: data["is_completed"]) as? Boolean,
            adminReply = reply.takeIf { it.isNotBlank() },
            repliedBy = text("repliedBy", "replied_by").takeIf { it.isNotBlank() },
            repliedEmail = text("repliedEmail", "replied_email").takeIf { it.isNotBlank() },
            repliedAt = date("repliedAt", "replied_at"),
            lastActivityAtMillis = InboxMessageOrder.latestMessageAt(data)
        )
    }
}
