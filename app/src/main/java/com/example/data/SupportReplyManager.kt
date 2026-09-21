package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.data.supabase.FeedbackRepository
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
        val normStatus = ticketStatus.uppercase()
        if (normStatus == "SOLUCIONADO" || normStatus == "CERRADO" || normStatus == "CLOSED" || normStatus == "RESUELTO") {
            return false
        }
        val supportReplies = messages.filter { 
            (it.senderRole.equals("SUPPORT", ignoreCase = true) || it.senderRole.equals("ADMIN", ignoreCase = true)) &&
            !it.isGreeting && !isDefaultGreeting(it.text)
        }
        return supportReplies.isNotEmpty()
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
        context: Context,
        reportId: String,
        replyText: String,
        author: String = "Equipo Coach",
        authorEmail: String? = null,
        userEmail: String? = null,
        userId: String? = null,
        reportTitle: String? = null,
        reportDescription: String? = null,
        tag: String = "SOPORTE",
        isFirestoreDoc: Boolean = false,
        markAsRead: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = FirebaseFirestore.getInstance()
            var docSnap: com.google.firebase.firestore.DocumentSnapshot? = null
            var resolvedUserId = userId?.takeIf { it.isNotBlank() && it != "anonimo" } ?: ""
            var resolvedTitle = reportTitle ?: "Reporte de Soporte"
            var resolvedOriginalDesc = reportDescription?.takeIf { it.isNotBlank() } ?: reportTitle ?: ""
            var resolvedSenderName = "Usuario"
            var firestoreReportId = reportId
            val finalTag = tag.uppercase(Locale.ROOT)

            // Resolver userId por userEmail de forma exhaustiva
            val cleanEmail = userEmail?.trim().orEmpty()
            if (resolvedUserId.isBlank() && cleanEmail.isNotBlank()) {
                try {
                    // 1. Búsqueda exacta por email
                    var userQuery = db.collection("users").whereEqualTo("email", cleanEmail).limit(1).get().await()
                    // 2. Búsqueda en minúsculas por email
                    if (userQuery.isEmpty) {
                        userQuery = db.collection("users").whereEqualTo("email", cleanEmail.lowercase(Locale.ROOT)).limit(1).get().await()
                    }
                    // 3. Búsqueda por campo userEmail
                    if (userQuery.isEmpty) {
                        userQuery = db.collection("users").whereEqualTo("userEmail", cleanEmail).limit(1).get().await()
                    }
                    if (userQuery.isEmpty) {
                        userQuery = db.collection("users").whereEqualTo("userEmail", cleanEmail.lowercase(Locale.ROOT)).limit(1).get().await()
                    }
                    if (!userQuery.isEmpty) {
                        val userDoc = userQuery.documents[0]
                        resolvedUserId = userDoc.id
                        resolvedSenderName = userDoc.getString("userName") 
                            ?: userDoc.getString("displayName")
                            ?: userDoc.getString("name") 
                            ?: cleanEmail.substringBefore("@")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error buscando usuario por email en sendSupportReply: ${e.message}")
                }
            }

            try {
                var doc = db.collection("support_reports").document(reportId).get().await()
                if (!doc.exists()) {
                    // Fallback para reportes antiguos donde el ID no coincide con Firestore
                    val query = db.collection("support_reports")
                        .whereEqualTo("title", reportTitle?.trim() ?: "")
                        .limit(1).get().await()
                    if (!query.isEmpty) {
                        doc = query.documents[0]
                        firestoreReportId = doc.id
                        Log.d(TAG, "Reporte encontrado por título. Reemplazando ID: $reportId -> $firestoreReportId")
                    }
                }
                
                if (doc.exists()) {
                    docSnap = doc
                    val dbUid = doc.getString("userId")
                    if (!dbUid.isNullOrBlank() && dbUid != "anonimo" && resolvedUserId.isBlank()) {
                        resolvedUserId = dbUid
                    }
                    resolvedTitle = doc.getString("title") ?: resolvedTitle
                    resolvedOriginalDesc = doc.getString("description") ?: doc.getString("content") ?: resolvedOriginalDesc
                    resolvedSenderName = doc.getString("userName") ?: resolvedSenderName
                } else {
                    // Si no existe en support_reports (ej. anuncio/patrocinador), crearlo automáticamente para garantizar entrega
                    val newReportData = mutableMapOf<String, Any>(
                        "id" to reportId,
                        "title" to resolvedTitle,
                        "description" to resolvedOriginalDesc,
                        "content" to resolvedOriginalDesc,
                        "userId" to resolvedUserId,
                        "userEmail" to cleanEmail,
                        "userName" to resolvedSenderName.ifBlank { if (finalTag == "PATROCINADOR") "Patrocinador" else "Invocador" },
                        "createdAt" to Timestamp.now(),
                        "createdAtMillis" to System.currentTimeMillis(),
                        "status" to "PENDIENTE",
                        "tag" to finalTag,
                        "type" to finalTag
                    )
                    db.collection("support_reports").document(reportId).set(newReportData, com.google.firebase.firestore.SetOptions.merge()).await()
                    firestoreReportId = reportId
                    docSnap = db.collection("support_reports").document(reportId).get().await()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error obteniendo o creando documento de soporte en Firestore: ${e.message}")
            }

            val isGreeting = isDefaultGreeting(replyText)
            val newEntry = SupportMessageEntry(
                senderName = author,
                senderRole = "SUPPORT",
                senderEmail = authorEmail,
                text = replyText,
                timestampMillis = System.currentTimeMillis(),
                isGreeting = isGreeting
            )

            // Combinar historial de la nube como fuente de verdad para multidispositivo
            val baseConversation = mutableListOf<SupportMessageEntry>()
            val remoteConv = docSnap?.get("conversation") as? List<Map<String, Any>>
            if (!remoteConv.isNullOrEmpty()) {
                remoteConv.forEach { item ->
                    val text = item["text"] as? String ?: ""
                    if (text.isNotBlank()) {
                        baseConversation.add(
                            SupportMessageEntry(
                                id = item["id"] as? String ?: java.util.UUID.randomUUID().toString(),
                                senderName = item["senderName"] as? String ?: "Soporte",
                                senderRole = item["senderRole"] as? String ?: "SUPPORT",
                                senderEmail = item["senderEmail"] as? String ?: (item["repliedEmail"] as? String),
                                text = text,
                                timestampMillis = (item["timestampMillis"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                                isGreeting = (item["isGreeting"] as? Boolean) ?: false
                            )
                        )
                    }
                }
            } else {
                baseConversation.addAll(getConversation(context, reportId))
            }

            // Asegurar que el reporte original del usuario sea el primer mensaje de la conversación
            if (resolvedOriginalDesc.isNotBlank() && baseConversation.none { it.senderRole.equals("USER", ignoreCase = true) && it.text.trim() == resolvedOriginalDesc.trim() }) {
                baseConversation.add(
                    0,
                    SupportMessageEntry(
                        id = "${reportId}_initial",
                        senderName = resolvedSenderName.ifBlank { if (finalTag == "PATROCINADOR") "Patrocinador" else "Invocador" },
                        senderRole = "USER",
                        text = resolvedOriginalDesc,
                        timestampMillis = docSnap?.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                        isGreeting = false
                    )
                )
            }

            baseConversation.add(newEntry)
            val currentConversation = baseConversation.toList()
            saveConversation(context, reportId, currentConversation)
            saveLocalReply(context, reportId, replyText, author, authorEmail)

            val conversationListMap = currentConversation.map { m ->
                val map = mutableMapOf<String, Any>(
                    "id" to m.id,
                    "senderName" to m.senderName,
                    "senderRole" to m.senderRole,
                    "text" to m.text,
                    "timestampMillis" to m.timestampMillis,
                    "isGreeting" to m.isGreeting
                )
                if (!m.senderEmail.isNullOrBlank()) {
                    map["senderEmail"] = m.senderEmail
                }
                map
            }

            val prevAdminReply = docSnap?.getString("adminReply") ?: ""
            val accumulatedReply = if (prevAdminReply.isNotBlank() && !prevAdminReply.contains(replyText.trim())) {
                "$prevAdminReply\n\n---\n\n$replyText"
            } else {
                replyText
            }

            // Actualizar documento de soporte en Firestore
            try {
                val updateData = mutableMapOf<String, Any>(
                    "adminReply" to accumulatedReply,
                    "lastAdminReply" to replyText,
                    "repliedAt" to Timestamp.now(),
                    "repliedBy" to author,
                    "repliedEmail" to (authorEmail ?: ""),
                    "conversation" to conversationListMap,
                    "lastMessageAt" to Timestamp.now(),
                    "isRead" to false,
                    "userRead" to false,
                    "hasNewAdminReply" to true,
                    "hasNewReply" to true,
                    "lastReplyRole" to "SUPPORT",
                    "lastReplySenderRole" to "SUPPORT",
                    "lastReplyAt" to Timestamp.now(),
                    "tag" to finalTag,
                    "type" to finalTag
                )
                if (markAsRead) {
                    updateData["status"] = "LEIDO"
                    updateData["isCompleted"] = false
                }
                db.collection("support_reports").document(firestoreReportId).set(updateData, com.google.firebase.firestore.SetOptions.merge()).await()
                Log.d(TAG, "Respuesta e historial sincronizados en Firestore para $firestoreReportId con tag $finalTag")
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo actualizar respuesta en Firestore: ${e.message}")
            }

            // Sincronizar en la nube en Supabase
            try {
                val effectiveStatus = if (markAsRead) FeedbackRepository.STATUS_READ else FeedbackRepository.STATUS_PENDING
                FeedbackRepository.updateFeedbackStatusInCloud(reportId, effectiveStatus)
            } catch (_: Exception) {}

            // Actualizar estado en almacenamiento local
            if (markAsRead) {
                FeedbackRepository.setFeedbackStatus(
                    context,
                    FeedbackReport(id = reportId, title = reportTitle ?: ""),
                    FeedbackRepository.STATUS_READ
                )
            }

            // Notificar a la bandeja de entrada del usuario en Firestore (multidispositivo)
            try {
                val targetUserIds = mutableSetOf<String>()
                if (resolvedUserId.isNotBlank() && resolvedUserId != "anonimo") {
                    targetUserIds.add(resolvedUserId)
                }
                if (cleanEmail.isNotBlank()) {
                    try {
                        val usersByEmail = db.collection("users").whereEqualTo("email", cleanEmail).get().await()
                        for (u in usersByEmail.documents) targetUserIds.add(u.id)
                        val usersByEmailLower = db.collection("users").whereEqualTo("email", cleanEmail.lowercase(Locale.ROOT)).get().await()
                        for (u in usersByEmailLower.documents) targetUserIds.add(u.id)
                        val usersByUserEmail = db.collection("users").whereEqualTo("userEmail", cleanEmail).get().await()
                        for (u in usersByUserEmail.documents) targetUserIds.add(u.id)
                    } catch (_: Exception) {}
                }

                for (targetUid in targetUserIds) {
                    val messageMap = hashMapOf<String, Any>(
                        "title" to if (finalTag == "PATROCINADOR") "Patrocinio: $resolvedTitle" else "Soporte: $resolvedTitle",
                        "content" to resolvedOriginalDesc,
                        "description" to resolvedOriginalDesc,
                        "adminReply" to accumulatedReply,
                        "repliedBy" to author,
                        "repliedEmail" to (authorEmail ?: ""),
                        "timestamp" to System.currentTimeMillis(),
                        "isRead" to false,
                        "userRead" to false,
                        "hasNewAdminReply" to true,
                        "lastReplyRole" to "SUPPORT",
                        "tag" to finalTag,
                        "sender" to (if (finalTag == "PATROCINADOR") "Patrocinador" else resolvedSenderName),
                        "reportId" to firestoreReportId,
                        "conversation" to conversationListMap
                    )
                    if (markAsRead) {
                        messageMap["status"] = "LEIDO"
                        messageMap["isCompleted"] = false
                    }
                    db.collection("users").document(targetUid).collection("messages").document(firestoreReportId)
                        .set(messageMap, com.google.firebase.firestore.SetOptions.merge()).await()

                    val userRef = db.collection("users").document(targetUid)
                    userRef.set(
                        mapOf(
                            "hasUnreadMessages" to true,
                            "unreadMessagesCount" to com.google.firebase.firestore.FieldValue.increment(1)
                        ),
                        com.google.firebase.firestore.SetOptions.merge()
                    ).await()
                }
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo enviar mensaje a la bandeja del usuario: ${e.message}")
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando respuesta de soporte: ${e.message}")
            false
        }
    }

    suspend fun sendUserReply(
        context: Context,
        reportId: String,
        userReplyText: String,
        userName: String,
        userId: String? = null,
        userEmail: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = FirebaseFirestore.getInstance()
            var docSnap: com.google.firebase.firestore.DocumentSnapshot? = null
            var resolvedDesc = ""
            var resolvedUserName = userName.takeIf { it.isNotBlank() && !it.contains("@") } ?: "Invocador"

            try {
                docSnap = db.collection("support_reports").document(reportId).get().await()
                if (docSnap != null && docSnap.exists()) {
                    resolvedDesc = docSnap.getString("description") ?: docSnap.getString("content") ?: ""
                    val dbUser = docSnap.getString("userName")
                    if (!dbUser.isNullOrBlank() && !dbUser.contains("@")) {
                        resolvedUserName = dbUser
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error obteniendo reporte en sendUserReply: ${e.message}")
            }

            val newEntry = SupportMessageEntry(
                senderName = resolvedUserName,
                senderRole = "USER",
                text = userReplyText.trim(),
                timestampMillis = System.currentTimeMillis(),
                isGreeting = false
            )

            // Combinar historial de la nube como fuente de verdad
            val baseConversation = mutableListOf<SupportMessageEntry>()
            val remoteConv = docSnap?.get("conversation") as? List<Map<String, Any>>
            if (!remoteConv.isNullOrEmpty()) {
                remoteConv.forEach { item ->
                    val text = item["text"] as? String ?: ""
                    if (text.isNotBlank()) {
                        baseConversation.add(
                            SupportMessageEntry(
                                id = item["id"] as? String ?: java.util.UUID.randomUUID().toString(),
                                senderName = item["senderName"] as? String ?: "Soporte",
                                senderRole = item["senderRole"] as? String ?: "SUPPORT",
                                text = text,
                                timestampMillis = (item["timestampMillis"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                                isGreeting = (item["isGreeting"] as? Boolean) ?: false
                            )
                        )
                    }
                }
            } else {
                baseConversation.addAll(getConversation(context, reportId))
            }

            // Asegurar que el reporte original del usuario esté como primer mensaje
            if (resolvedDesc.isNotBlank() && baseConversation.none { it.senderRole.equals("USER", ignoreCase = true) && it.text.trim() == resolvedDesc.trim() }) {
                baseConversation.add(
                    0,
                    SupportMessageEntry(
                        id = "${reportId}_initial",
                        senderName = resolvedUserName,
                        senderRole = "USER",
                        text = resolvedDesc,
                        timestampMillis = docSnap?.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                        isGreeting = false
                    )
                )
            }

            baseConversation.add(newEntry)
            val currentConversation = baseConversation.toList()
            saveConversation(context, reportId, currentConversation)

            val conversationListMap = currentConversation.map { m ->
                mapOf(
                    "id" to m.id,
                    "senderName" to m.senderName,
                    "senderRole" to m.senderRole,
                    "text" to m.text,
                    "timestampMillis" to m.timestampMillis,
                    "isGreeting" to m.isGreeting
                )
            }
            
            // 1. Actualizar el ticket de soporte marcándolo como PENDIENTE para que el admin lo atienda
            try {
                val updateData = hashMapOf<String, Any>(
                    "conversation" to conversationListMap,
                    "status" to "PENDIENTE",
                    "lastUserMessage" to userReplyText.trim(),
                    "lastUserMessageAt" to Timestamp.now(),
                    "updatedAt" to Timestamp.now()
                )
                db.collection("support_reports").document(reportId)
                    .set(updateData, com.google.firebase.firestore.SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.w(TAG, "Error actualizando ticket de soporte con respuesta de usuario: ${e.message}")
            }

            // 2. Actualizar la bandeja de entrada del usuario
            val effectiveUserId = userId?.takeIf { it.isNotBlank() && it != "anonimo" }
                ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (!effectiveUserId.isNullOrBlank()) {
                try {
                    val userMsgUpdate = hashMapOf<String, Any>(
                        "conversation" to conversationListMap,
                        "timestamp" to System.currentTimeMillis(),
                        "isRead" to true,
                        "status" to "PENDIENTE"
                    )
                    db.collection("users").document(effectiveUserId).collection("messages").document(reportId)
                        .set(userMsgUpdate, com.google.firebase.firestore.SetOptions.merge()).await()
                } catch (e: Exception) {
                    Log.w(TAG, "Error actualizando mensaje de usuario en su bandeja: ${e.message}")
                }
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando respuesta de usuario: ${e.message}")
            false
        }
    }

    suspend fun updateReportStatus(
        context: Context,
        reportId: String,
        newStatus: String,
        userId: String? = null,
        userEmail: String? = null,
        reportTitle: String? = null,
        supabaseId: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val normalizedCloudStatus = when (newStatus.uppercase()) {
                "SOLVED", "SOLUCIONADO", "RESUELTO" -> "SOLUCIONADO"
                "READ", "LEIDO", "LEÍDO" -> "LEIDO"
                "ACCEPTED", "ACEPTADA", "ACEPTADO" -> "ACEPTADO"
                "REJECTED", "RECHAZADA", "RECHAZADO" -> "RECHAZADO"
                else -> "PENDIENTE"
            }
            val isCompleted = (normalizedCloudStatus == "SOLUCIONADO" || normalizedCloudStatus == "ACEPTADO")
            val db = FirebaseFirestore.getInstance()
            val updateData = hashMapOf<String, Any>(
                "status" to normalizedCloudStatus,
                "isCompleted" to isCompleted,
                "updatedAt" to Timestamp.now()
            )

            // 1. Actualizar ticket principal en Firestore por ID directo
            var matchedDocId = reportId
            try {
                val docRef = db.collection("support_reports").document(reportId)
                val check = docRef.get().await()
                if (check.exists()) {
                    docRef.set(updateData, com.google.firebase.firestore.SetOptions.merge()).await()
                } else if (!reportTitle.isNullOrBlank()) {
                    val query = db.collection("support_reports")
                        .whereEqualTo("title", reportTitle.trim())
                        .limit(5).get().await()
                    for (d in query.documents) {
                        matchedDocId = d.id
                        d.reference.set(updateData, com.google.firebase.firestore.SetOptions.merge()).await()
                    }
                } else {
                    docRef.set(updateData, com.google.firebase.firestore.SetOptions.merge()).await()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error al actualizar support_reports por document($reportId): ${e.message}")
            }

            // 2. Resolver userId si no se especificó
            var targetUserId = userId?.takeIf { it.isNotBlank() && it != "anonimo" }
            if (targetUserId.isNullOrBlank()) {
                try {
                    val snap = db.collection("support_reports").document(matchedDocId).get().await()
                    targetUserId = snap.getString("userId")?.takeIf { it.isNotBlank() && it != "anonimo" }
                    if (targetUserId.isNullOrBlank() && !userEmail.isNullOrBlank()) {
                        val uSnap = db.collection("users").whereEqualTo("email", userEmail.trim()).limit(1).get().await()
                        if (!uSnap.isEmpty) {
                            targetUserId = uSnap.documents[0].id
                        }
                    }
                } catch (_: Exception) {}
            }

            // 3. Sincronizar en la bandeja del usuario (para sincronización multidispositivo)
            if (!targetUserId.isNullOrBlank()) {
                try {
                    val userMsgUpdate = hashMapOf<String, Any>(
                        "status" to normalizedCloudStatus,
                        "isCompleted" to isCompleted,
                        "updatedAt" to Timestamp.now()
                    )
                    db.collection("users").document(targetUserId).collection("messages").document(matchedDocId)
                        .set(userMsgUpdate, com.google.firebase.firestore.SetOptions.merge()).await()
                    if (matchedDocId != reportId) {
                        db.collection("users").document(targetUserId).collection("messages").document(reportId)
                            .set(userMsgUpdate, com.google.firebase.firestore.SetOptions.merge()).await()
                    }
                } catch (_: Exception) {}
            }

            // 4. Sincronizar en Supabase en la nube
            val localStatus = when (normalizedCloudStatus) {
                "SOLUCIONADO" -> FeedbackRepository.STATUS_SOLVED
                "LEIDO" -> FeedbackRepository.STATUS_READ
                "ACEPTADO" -> FeedbackRepository.STATUS_ACCEPTED
                "RECHAZADO" -> FeedbackRepository.STATUS_REJECTED
                else -> FeedbackRepository.STATUS_PENDING
            }
            val supaTargetId = supabaseId?.takeIf { it.isNotBlank() } ?: reportId
            try {
                FeedbackRepository.updateFeedbackStatusInCloud(supaTargetId, localStatus)
            } catch (_: Exception) {}

            // 5. Actualizar repositorio local
            FeedbackRepository.setFeedbackStatus(
                context,
                FeedbackReport(id = reportId, title = reportTitle ?: ""),
                localStatus
            )
            true
        } catch (e: Exception) {
            Log.w(TAG, "Error actualizando estado multidispositivo: ${e.message}")
            false
        }
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

        // 2. Purga en Supabase / Local Feedback
        try {
            val fbPurged = FeedbackRepository.autoPurgeExpiredReports(context)
            totalPurged += fbPurged
        } catch (_: Exception) {}

        totalPurged
    }
}
