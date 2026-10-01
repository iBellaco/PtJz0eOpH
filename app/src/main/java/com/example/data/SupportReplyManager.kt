package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.data.FeedbackRepository
import com.example.data.remote.model.FeedbackReport
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class SupportReply(
    val reportId: String,
    val text: String,
    val author: String = "Equipo Coach",
    val authorEmail: String? = null,
    val timestampMillis: Long = System.currentTimeMillis()
)

data class SupportMessageEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val senderName: String = "",
    val senderRole: String = "SUPPORT", // "SUPPORT" o "USER"
    val senderEmail: String? = null,
    val text: String = "",
    val timestampMillis: Long = System.currentTimeMillis(),
    val isGreeting: Boolean = false
)

data class AutoDeleteCountdown(
    val maxDays: Int,
    val remainingMillis: Long,
    val remainingDays: Long,
    val remainingHours: Long,
    val isExpired: Boolean,
    val displayText: String
)

object SupportReplyManager {
    private const val TAG = "SupportReplyManager"
    private const val PREFS_NAME = "support_replies_prefs"
    private const val KEY_REPLY_PREFIX = "reply_text_"
    private const val KEY_DATE_PREFIX = "reply_date_"
    private const val KEY_AUTHOR_PREFIX = "reply_author_"
    private const val KEY_AUTHOR_EMAIL_PREFIX = "reply_author_email_"
    private const val KEY_CONVERSATION_PREFIX = "conversation_history_"

    const val DAYS_RETENTION_READ = 30
    const val DAYS_RETENTION_UNREAD = 60

    /**
     * Identifica si un texto coincide con el saludo predeterminado del soporte de Coach.
     */
    fun isDefaultGreeting(text: String): Boolean {
        if (text.trim() == SupportConversationPolicy.SYSTEM_GREETING) return true
        val clean = text.trim()
        val isGreetingPrefix = clean.startsWith("👋 Hola", ignoreCase = true) || 
                               clean.startsWith("Hola", ignoreCase = true) ||
                               clean.startsWith("👋 Saludo", ignoreCase = true)
        val hasSupportMention = clean.contains("equipo de soporte", ignoreCase = true) ||
                                clean.contains("soporte de Coach", ignoreCase = true)
        val hasReceivedMention = clean.contains("recibido tu mensaje", ignoreCase = true) ||
                                 clean.contains("estamos para ayudarte", ignoreCase = true)
        return isGreetingPrefix && hasSupportMention && hasReceivedMention
    }

    /**
     * Determina si el usuario tiene permiso para responder.
     * Al enviar un reporte de soporte se inicia una conversación activa,
     * permitiendo al usuario aportar más información o responder en cualquier momento.
     */
    fun canUserReply(messages: List<SupportMessageEntry>, ticketStatus: String = "PENDIENTE"): Boolean {
        return !SupportConversationPolicy.isClosed(ticketStatus)
    }

    /**
     * Comprueba si sólo hay saludos de bienvenida de parte de soporte.
     */
    fun isOnlyGreeting(messages: List<SupportMessageEntry>): Boolean {
        val supportMessages = messages.filter { 
            it.senderRole.equals("SUPPORT", ignoreCase = true) || it.senderRole.equals("ADMIN", ignoreCase = true)
        }
        if (supportMessages.isEmpty()) return true
        return supportMessages.all { it.isGreeting || isDefaultGreeting(it.text) }
    }

    fun getConversation(context: Context, reportId: String): List<SupportMessageEntry> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_CONVERSATION_PREFIX + reportId, null)
        if (!json.isNullOrBlank()) {
            try {
                val array = org.json.JSONArray(json)
                val list = mutableListOf<SupportMessageEntry>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        SupportMessageEntry(
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                            senderName = obj.optString("senderName", "Soporte"),
                            senderRole = obj.optString("senderRole", "SUPPORT"),
                            senderEmail = obj.optString("senderEmail", "").takeIf { it.isNotBlank() },
                            text = obj.optString("text", ""),
                            timestampMillis = obj.optLong("timestampMillis", System.currentTimeMillis()),
                            isGreeting = obj.optBoolean("isGreeting", false)
                        )
                    )
                }
                return list
            } catch (e: Exception) {
                Log.w(TAG, "Error parseando historial de conversación local: ${e.message}")
            }
        }
        // Fallback a respuesta única legacy si existía
        val legacyReply = getLocalReply(context, reportId)
        if (legacyReply != null && legacyReply.text.isNotBlank()) {
            val entry = SupportMessageEntry(
                senderName = legacyReply.author,
                senderRole = "SUPPORT",
                senderEmail = legacyReply.authorEmail,
                text = legacyReply.text,
                timestampMillis = legacyReply.timestampMillis,
                isGreeting = isDefaultGreeting(legacyReply.text)
            )
            return listOf(entry)
        }
        return emptyList()
    }

    fun saveConversation(context: Context, reportId: String, messages: List<SupportMessageEntry>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val array = org.json.JSONArray()
        messages.forEach { m ->
            val obj = org.json.JSONObject()
            obj.put("id", m.id)
            obj.put("senderName", m.senderName)
            obj.put("senderRole", m.senderRole)
            if (!m.senderEmail.isNullOrBlank()) {
                obj.put("senderEmail", m.senderEmail)
            }
            obj.put("text", m.text)
            obj.put("timestampMillis", m.timestampMillis)
            obj.put("isGreeting", m.isGreeting || isDefaultGreeting(m.text))
            array.put(obj)
        }
        prefs.edit().putString(KEY_CONVERSATION_PREFIX + reportId, array.toString()).apply()
    }

    /**
     * Calcula el tiempo restante de vida antes de la eliminación automática:
     * - 30 días si el mensaje está leído (o resuelto).
     * - 60 días si el mensaje está sin leer (pendiente).
     */
    fun calculateCountdown(createdAtMillis: Long, isRead: Boolean): AutoDeleteCountdown {
        val maxDays = if (isRead) DAYS_RETENTION_READ else DAYS_RETENTION_UNREAD
        val maxLifespanMillis = maxDays * 24L * 60 * 60 * 1000L
        val now = System.currentTimeMillis()
        val elapsed = now - createdAtMillis
        val remainingMillis = maxLifespanMillis - elapsed

        if (remainingMillis <= 0) {
            return AutoDeleteCountdown(
                maxDays = maxDays,
                remainingMillis = 0L,
                remainingDays = 0L,
                remainingHours = 0L,
                isExpired = true,
                displayText = "Expirado ($maxDays d)"
            )
        }

        val totalHours = remainingMillis / (1000 * 60 * 60)
        val days = totalHours / 24
        val hours = totalHours % 24

        val formattedTime = if (days > 0) {
            "${days}d ${hours}h"
        } else {
            "${hours}h"
        }

        return AutoDeleteCountdown(
            maxDays = maxDays,
            remainingMillis = remainingMillis,
            remainingDays = days,
            remainingHours = hours,
            isExpired = false,
            displayText = "$formattedTime ($maxDays d)"
        )
    }

    fun parseDateToMillis(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return System.currentTimeMillis()
        dateStr.toLongOrNull()?.let { return it }
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val clean = dateStr.substringBefore(".").substringBefore("+").substringBefore("Z")
            sdf.parse(clean)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    fun getLocalReply(context: Context, reportId: String): SupportReply? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val text = prefs.getString(KEY_REPLY_PREFIX + reportId, null) ?: return null
        val date = prefs.getLong(KEY_DATE_PREFIX + reportId, System.currentTimeMillis())
        val author = prefs.getString(KEY_AUTHOR_PREFIX + reportId, "Equipo Coach") ?: "Equipo Coach"
        val authorEmail = prefs.getString(KEY_AUTHOR_EMAIL_PREFIX + reportId, null)
        return SupportReply(reportId, text, author, authorEmail, date)
    }

    fun saveLocalReply(context: Context, reportId: String, text: String, author: String = "Equipo Coach", authorEmail: String? = null): SupportReply {
        val now = System.currentTimeMillis()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
            .putString(KEY_REPLY_PREFIX + reportId, text)
            .putLong(KEY_DATE_PREFIX + reportId, now)
            .putString(KEY_AUTHOR_PREFIX + reportId, author)
        if (!authorEmail.isNullOrBlank()) {
            editor.putString(KEY_AUTHOR_EMAIL_PREFIX + reportId, authorEmail)
        }
        editor.apply()
        return SupportReply(reportId, text, author, authorEmail, now)
    }

    suspend fun sendSupportReply(
        context: Context, reportId: String, replyText: String, author: String = "Equipo Coach",
        authorEmail: String? = null, userEmail: String? = null, userId: String? = null,
        reportTitle: String? = null, reportDescription: String? = null, tag: String = "SOPORTE",
        isFirestoreDoc: Boolean = false, markAsRead: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        if (replyText.isBlank() || isDefaultGreeting(replyText) || replyText.trim() == SupportConversationPolicy.SYSTEM_GREETING) return@withContext false
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return@withContext false
        try {
            val db = FirebaseFirestore.getInstance()
            val ref = db.collection("support_reports").document(reportId)
            // Resolve legacy recipients only as an administrator. Moderators use the ticket owner ID.
            var fallbackUid = userId.orEmpty()
            if (fallbackUid.isBlank() && SupportTicketAccess.isAdmin() && !userEmail.isNullOrBlank()) {
                fallbackUid = db.collection("users").whereEqualTo("email", userEmail.trim()).limit(1).get().await().documents.firstOrNull()?.id.orEmpty()
            }
            val entry = SupportMessageEntry(senderName = author, senderRole = "SUPPORT", senderEmail = authorEmail, text = replyText.trim())
            val history = db.runTransaction { transaction ->
                val snapshot = transaction.get(ref)
                check(snapshot.exists() || (SupportTicketAccess.isAdmin() && fallbackUid.isNotBlank())) { "Ticket no disponible" }
                val owner = snapshot.getString("userId").orEmpty().ifBlank { fallbackUid }
                check(owner.isNotBlank() && owner != "anonimo") { "No se pudo identificar al destinatario" }
                val actualTag = snapshot.getString("tag") ?: snapshot.getString("type") ?: tag
                check(SupportTicketAccess.isAdmin() || !SupportConversationPolicy.isSponsor(actualTag)) { "Ticket exclusivo del administrador" }
                val previous = SupportConversationPolicy.decode(snapshot.get("conversation")).ifEmpty {
                    SupportConversationPolicy.initial(reportId, snapshot.getString("userName") ?: "Invocador",
                        snapshot.getString("description") ?: reportDescription.orEmpty(), snapshot.getTimestamp("createdAt")?.toDate()?.time ?: entry.timestampMillis,
                        snapshot.getString("adminReply").orEmpty(), snapshot.getString("repliedBy") ?: "Soporte Coach",
                        snapshot.getTimestamp("repliedAt")?.toDate()?.time ?: entry.timestampMillis)
                }
                val messages = previous + entry
                val rows = (snapshot.get("conversation") as? List<*>)?.takeIf { it.isNotEmpty() }.orEmpty().ifEmpty { previous.map { SupportConversationPolicy.encode(it, if (it.senderRole == "USER") owner else "") } } + SupportConversationPolicy.encode(entry, auth.uid)
                val status = if (markAsRead) "READ" else snapshot.getString("status") ?: "PENDING"
                val data = hashMapOf<String, Any>(
                    "id" to reportId, "reportId" to reportId, "userId" to owner,
                    "userEmail" to (snapshot.getString("userEmail") ?: userEmail.orEmpty()),
                    "userName" to (snapshot.getString("userName") ?: "Invocador"),
                    "title" to (snapshot.getString("title") ?: reportTitle.orEmpty()),
                    "description" to (snapshot.getString("description") ?: reportDescription.orEmpty()),
                    "conversation" to rows, "adminReply" to replyText.trim(), "lastAdminReply" to replyText.trim(),
                    "repliedBy" to author, "repliedEmail" to authorEmail.orEmpty(), "repliedAt" to Timestamp.now(),
                    "lastMessageAt" to Timestamp.now(), "lastReplyRole" to "SUPPORT", "lastReplySenderRole" to "SUPPORT",
                    "status" to status, "isCompleted" to SupportConversationPolicy.isClosed(status),
                    "isRead" to false, "userRead" to false, "hasNewAdminReply" to true, "hasNewReply" to true,
                    "staffRead" to markAsRead, "tag" to actualTag, "type" to (snapshot.getString("type") ?: actualTag),
                    "staffVisible" to !SupportConversationPolicy.isSponsor(actualTag))
                if (!snapshot.exists()) data["createdAt"] = Timestamp.now()
                transaction.set(ref, data, com.google.firebase.firestore.SetOptions.merge())
                transaction.set(db.collection("users").document(owner).collection("messages").document(reportId),
                    data + mapOf("content" to data.getValue("description"), "timestamp" to entry.timestampMillis), com.google.firebase.firestore.SetOptions.merge())
                messages
            }.await()
            saveConversation(context, reportId, history)
            saveLocalReply(context, reportId, replyText.trim(), author, authorEmail)
            true
        } catch (error: Exception) {
            Log.e(TAG, "No se pudo sincronizar la respuesta", error)
            false
        }
    }

    suspend fun sendUserReply(context: Context, reportId: String, userReplyText: String, userName: String,
        userId: String? = null, userEmail: String? = null): Boolean = withContext(Dispatchers.IO) {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return@withContext false
        if (userReplyText.isBlank() || (!userId.isNullOrBlank() && userId != auth.uid)) return@withContext false
        try {
            val db = FirebaseFirestore.getInstance()
            val ref = db.collection("support_reports").document(reportId)
            val entry = SupportMessageEntry(senderName = userName, senderRole = "USER", senderEmail = auth.email, text = userReplyText.trim())
            val history = db.runTransaction { transaction ->
                val snapshot = transaction.get(ref)
                check(snapshot.exists()) { "Ticket no disponible" }
                val owner = snapshot.getString("userId").orEmpty()
                check(owner == auth.uid || (owner.isBlank() && snapshot.getString("userEmail") == auth.email)) { "Ticket de otra cuenta" }
                check(!SupportConversationPolicy.isClosed(snapshot.getString("status").orEmpty())) { "Ticket cerrado" }
                val previous = SupportConversationPolicy.decode(snapshot.get("conversation")).ifEmpty {
                    SupportConversationPolicy.initial(reportId, userName, snapshot.getString("description").orEmpty(),
                        snapshot.getTimestamp("createdAt")?.toDate()?.time ?: entry.timestampMillis,
                        snapshot.getString("adminReply").orEmpty(), snapshot.getString("repliedBy") ?: "Soporte Coach",
                        snapshot.getTimestamp("repliedAt")?.toDate()?.time ?: entry.timestampMillis)
                }
                val messages = previous + entry
                val data = mapOf<String, Any>("conversation" to ((snapshot.get("conversation") as? List<*>)?.takeIf { it.isNotEmpty() }.orEmpty().ifEmpty { previous.map { SupportConversationPolicy.encode(it, if (it.senderRole == "USER") auth.uid else "") } } + SupportConversationPolicy.encode(entry, auth.uid)),
                    "status" to "PENDING", "isCompleted" to false, "lastUserMessage" to userReplyText.trim(),
                    "lastUserMessageAt" to Timestamp.now(), "lastMessageAt" to Timestamp.now(), "updatedAt" to Timestamp.now(),
                    "lastReplyRole" to "USER", "staffRead" to false, "isRead" to true, "userRead" to true,
                    "hasNewAdminReply" to false, "hasNewReply" to false, "userId" to auth.uid)
                transaction.update(ref, data)
                transaction.set(db.collection("users").document(auth.uid).collection("messages").document(reportId),
                    data + mapOf("reportId" to reportId, "title" to snapshot.getString("title").orEmpty(),
                        "content" to snapshot.getString("description").orEmpty(), "tag" to (snapshot.getString("tag") ?: "SOPORTE"),
                        "timestamp" to entry.timestampMillis), com.google.firebase.firestore.SetOptions.merge())
                messages
            }.await()
            saveConversation(context, reportId, history)
            true
        } catch (error: Exception) {
            Log.e(TAG, "No se pudo sincronizar el mensaje", error)
            false
        }
    }

    suspend fun updateReportStatus(context: Context, reportId: String, newStatus: String,
        userId: String? = null, userEmail: String? = null, reportTitle: String? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = FirebaseFirestore.getInstance()
            val ref = db.collection("support_reports").document(reportId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(ref)
                check(snapshot.exists()) { "Ticket no disponible" }
                val data = mapOf<String, Any>("status" to newStatus, "isCompleted" to SupportConversationPolicy.isClosed(newStatus),
                    "staffRead" to true, "updatedAt" to Timestamp.now())
                transaction.update(ref, data)
                val owner = snapshot.getString("userId").orEmpty()
                if (owner.isNotBlank()) transaction.set(db.collection("users").document(owner).collection("messages").document(reportId),
                    data, com.google.firebase.firestore.SetOptions.merge())
            }.await()
            true
        } catch (error: Exception) {
            Log.e(TAG, "No se pudo sincronizar el estado", error)
            false
        }
    }

    suspend fun markUserRead(reportId: String, observedLastMessageId: String?): Boolean = withContext(Dispatchers.IO) {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return@withContext false
        try {
            val db = FirebaseFirestore.getInstance()
            val ref = db.collection("support_reports").document(reportId)
            db.runTransaction { transaction ->
                val source = transaction.get(ref)
                check(source.exists())
                check(source.getString("userId") == user.uid || (source.getString("userId").isNullOrBlank() && source.getString("userEmail") == user.email))
                val latest = SupportConversationPolicy.decode(source.get("conversation")).lastOrNull()?.id
                if (observedLastMessageId != null && latest != observedLastMessageId) return@runTransaction false
                val data = mapOf<String, Any>("userRead" to true, "isRead" to true, "hasNewAdminReply" to false,
                    "hasNewReply" to false, "userReadAtMillis" to System.currentTimeMillis())
                transaction.update(ref, data)
                transaction.set(db.collection("users").document(user.uid).collection("messages").document(reportId), data, com.google.firebase.firestore.SetOptions.merge())
                true
            }.await()
        } catch (error: Exception) { Log.w(TAG, "No se pudo sincronizar la lectura", error); false }
    }

    fun createEmailReplyIntent(email: String, title: String, replyText: String): Intent {
        val subject = "Respuesta de Soporte Coach: $title"
        val body = "Hola,\n\nHemos revisado tu mensaje de soporte: \"$title\"\n\n$replyText\n\nAtentamente,\nEquipo Coach"
        val uri = Uri.parse("mailto:${email.trim()}?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}")
        return Intent(Intent.ACTION_SENDTO, uri)
    }

    fun sanitizePlainText(input: String, maxLength: Int = 500): String {
        val clean = input.replace(Regex("<[^>]*>"), "").replace(Regex("(?i)<script[^>]*>[^<]*</script>"), "")
        return if (clean.length > maxLength) clean.substring(0, maxLength) else clean
    }

    suspend fun autoPurgeAllExpired(context: Context): Int = withContext(Dispatchers.IO) {
        if (!SupportTicketAccess.isAdmin()) return@withContext 0
        var totalPurged = 0
        val now = System.currentTimeMillis()

        // 1. Purga en Firestore de support_reports
        try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection("support_reports").get().await()
            for (doc in snapshot.documents) {
                val status = doc.getString("status") ?: "PENDIENTE"
                val isRead = status.equals("LEIDO", ignoreCase = true) ||
                             status.equals("LEÍDO", ignoreCase = true) ||
                             status.equals("SOLUCIONADO", ignoreCase = true) ||
                             status.equals("SOLVED", ignoreCase = true) ||
                             status.equals("READ", ignoreCase = true)
                val maxDays = if (isRead) DAYS_RETENTION_READ else DAYS_RETENTION_UNREAD
                val maxLifespan = maxDays * 24L * 60 * 60 * 1000L
                val ts = doc.getTimestamp("createdAt")?.toDate()?.time ?: continue
                if (now - ts >= maxLifespan) {
                    try {
                        db.collection("support_reports").document(doc.id).delete().await()
                        totalPurged++
                        Log.d(TAG, "Reporte expirado eliminado de Firestore: ${doc.id}")
                    } catch (_: Exception) {}
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error en purga de reportes en Firestore: ${e.message}")
        }

        // 2. Purga en Local Feedback
        try {
            val fbPurged = FeedbackRepository.autoPurgeExpiredReports(context)
            totalPurged += fbPurged
        } catch (_: Exception) {}

        totalPurged
    }
}
