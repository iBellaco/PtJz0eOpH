package com.example.data.supabase

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.BuildConfig
import com.example.data.WildRiftRepository
import com.example.data.remote.model.FeedbackReport
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import com.example.data.SupportReplyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.DocumentReference
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class InsertFeedbackReport(
    val id: String? = null,
    val type: String,
    val title: String,
    val description: String,
    @SerialName("app_version") val appVersion: String,
    @SerialName("device_info") val deviceInfo: String
)

object FeedbackRepository {

    private const val TAG = "FeedbackRepository"
    private const val TABLE_NAME = "feedbacks"
    private const val PREFS_NAME = "feedback_admin_prefs"
    private const val KEY_COMPLETED_IDS = "completed_feedback_ids"
    private const val PREF_STATUS_PREFIX = "status_"

    // Constantes de Estado
    const val STATUS_PENDING = "PENDING"
    const val STATUS_READ = "READ"             // Leído (para Reportes)
    const val STATUS_SOLVED = "SOLVED"         // Solucionado (para Reportes)
    const val STATUS_ACCEPTED = "ACCEPTED"     // Aceptada (para Sugerencias)
    const val STATUS_REJECTED = "REJECTED"     // Rechazada (para Sugerencias)
    const val STATUS_COMPLETED = "COMPLETED"   // Compatibilidad

    /**
     * Envía un reporte o sugerencia a Supabase y purga automáticamente
     * los reportes con más de [retentionDays] días de antigüedad.
     */
    suspend fun submitFeedback(
        type: String,
        title: String,
        description: String,
        email: String? = null,
        imagesBase64: List<String> = emptyList(),
        retentionDays: Int = 7,
        userName: String? = null,
        id: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val client = SupabaseClientManager.client
            val postgrest = client.postgrest

            // 1. Purgar reportes que exceden el ciclo de retención (60 días máximo)
            try {
                purgeOldReports(60)
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo realizar la purga automática de reportes antiguos: ${e.message}")
            }

            // 2. Preparar el nuevo reporte
            val baseDeviceInfo = "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT})"
            var deviceInfo = baseDeviceInfo
            if (imagesBase64.isNotEmpty()) {
                for (img in imagesBase64) {
                    deviceInfo += "\n\n[IMAGE_BASE64]\n$img"
                }
            }
            val appVersion = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) [${WildRiftRepository.CURRENT_PATCH_VERSION}]"

            val headerBuilder = StringBuilder()
            if (!userName.isNullOrBlank() && !userName.contains("@")) {
                headerBuilder.append("Usuario: ${userName.trim()}\n")
            }
            if (!email.isNullOrBlank()) {
                headerBuilder.append("Correo de contacto: ${email.trim()}\n")
            }
            val finalDescription = if (headerBuilder.isNotEmpty()) {
                "${headerBuilder.toString()}\n${description.trim()}"
            } else {
                description.trim()
            }

            val report = InsertFeedbackReport(
                id = id,
                type = type,
                title = title.trim(),
                description = finalDescription,
                appVersion = appVersion,
                deviceInfo = deviceInfo
            )

            // 3. Insertar en la tabla feedbacks de Supabase
            postgrest.from(TABLE_NAME).insert(report)
            Log.d(TAG, "Feedback enviado exitosamente a Supabase")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando feedback a Supabase: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene el estado actual de un reporte/sugerencia.
     */
    fun getReportStatus(context: Context, report: FeedbackReport): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = report.id
        val compositeKey = if (report.createdAt != null) "${report.title}_${report.createdAt}" else null

        // 1. Revisar campo status de Supabase/Firestore (Cloud-First Multi-Device Sync)
        val remoteStatus = report.status?.trim()?.uppercase(Locale.US)
        if (!remoteStatus.isNullOrBlank()) {
            val normalized = when (remoteStatus) {
                "SOLVED", "SOLUCIONADO", "RESUELTO" -> STATUS_SOLVED
                "READ", "LEIDO", "LEÍDO" -> STATUS_READ
                "ACCEPTED", "ACEPTADA", "ACEPTADO" -> STATUS_ACCEPTED
                "REJECTED", "RECHAZADA", "RECHAZADO" -> STATUS_REJECTED
                "COMPLETED", "COMPLETADO" -> if (report.type.equals("SUGGESTION", ignoreCase = true)) STATUS_ACCEPTED else STATUS_SOLVED
                "PENDING", "PENDIENTE" -> STATUS_PENDING
                else -> remoteStatus
            }
            if (!id.isNullOrBlank()) {
                prefs.edit().putString(PREF_STATUS_PREFIX + id, normalized).apply()
            }
            return normalized
        }

        // 2. Revisar estado local prefs
        if (!id.isNullOrBlank()) {
            val localStatus = prefs.getString(PREF_STATUS_PREFIX + id, null)
            if (!localStatus.isNullOrBlank()) return localStatus
        }
        if (!compositeKey.isNullOrBlank()) {
            val localStatus = prefs.getString(PREF_STATUS_PREFIX + compositeKey, null)
            if (!localStatus.isNullOrBlank()) return localStatus
        }

        // 3. Revisar legacy completed ids
        val completedIds = getCompletedFeedbackIds(context)
        if (isReportCompleted(report, completedIds)) {
            return if (report.type.equals("SUGGESTION", ignoreCase = true)) STATUS_ACCEPTED else STATUS_SOLVED
        }

        // 4. Si el mensaje ya ha sido respondido por un moderador/soporte, no debe salir en espera/pendiente
        if (!report.adminReply.isNullOrBlank()) {
            return STATUS_READ
        }
        if (!id.isNullOrBlank()) {
            val localReply = SupportReplyManager.getLocalReply(context, id)
            if (localReply != null && localReply.text.isNotBlank()) {
                return STATUS_READ
            }
        }

        return STATUS_PENDING
    }

    /**
     * Guarda el estado de un reporte/sugerencia de forma persistente.
     */
    fun setFeedbackStatus(
        context: Context,
        report: FeedbackReport,
        newStatus: String
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val id = report.id
        val compositeKey = if (report.createdAt != null) "${report.title}_${report.createdAt}" else null

        if (!id.isNullOrBlank()) editor.putString(PREF_STATUS_PREFIX + id, newStatus)
        if (!compositeKey.isNullOrBlank()) editor.putString(PREF_STATUS_PREFIX + compositeKey, newStatus)

        // Sincronizar también con legacy completedIds
        val isDone = newStatus == STATUS_SOLVED || newStatus == STATUS_ACCEPTED || newStatus == STATUS_COMPLETED
        val currentSet = prefs.getStringSet(KEY_COMPLETED_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (isDone) {
            if (!id.isNullOrBlank()) currentSet.add(id)
            if (!compositeKey.isNullOrBlank()) currentSet.add(compositeKey)
        } else {
            if (!id.isNullOrBlank()) currentSet.remove(id)
            if (!compositeKey.isNullOrBlank()) currentSet.remove(compositeKey)
        }
        editor.putStringSet(KEY_COMPLETED_IDS, currentSet)
        editor.apply()

        // Sincronizar en la nube en segundo plano si hay un ID disponible
        if (!id.isNullOrBlank()) {
            kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                updateFeedbackStatusInCloud(id, newStatus)
            }
        }
    }

    /**
     * Obtiene los IDs y claves de los reportes marcados como completados localmente.
     */
    fun getCompletedFeedbackIds(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_COMPLETED_IDS, emptySet())?.toSet() ?: emptySet()
    }

    /**
     * Marca o desmarca un reporte como completado de forma persistente en SharedPreferences y
     * sincroniza el estado en la base de datos de Supabase si está disponible.
     */
    fun setFeedbackCompleted(
        context: Context,
        report: FeedbackReport,
        completed: Boolean
    ) {
        val newStatus = if (completed) {
            if (report.type.equals("SUGGESTION", ignoreCase = true)) STATUS_ACCEPTED else STATUS_SOLVED
        } else {
            STATUS_PENDING
        }
        setFeedbackStatus(context, report, newStatus)
    }

    /**
     * Comprueba si un reporte está completado (ya sea por dato remoto de Supabase o guardado local permanente).
     */
    fun isReportCompleted(report: FeedbackReport, completedIds: Set<String>): Boolean {
        if (report.status.equals("COMPLETED", ignoreCase = true) ||
            report.status.equals("SOLVED", ignoreCase = true) ||
            report.status.equals("ACCEPTED", ignoreCase = true)
        ) return true
        if (report.isCompleted == true) return true
        
        val id = report.id
        if (!id.isNullOrBlank() && completedIds.contains(id)) return true

        val compositeKey = if (report.createdAt != null) "${report.title}_${report.createdAt}" else null
        if (!compositeKey.isNullOrBlank() && completedIds.contains(compositeKey)) return true

        if (completedIds.contains("title:${report.title}")) return true

        return false
    }

    /**
     * Sincroniza el cambio de estado en la nube de Supabase.
     */
    suspend fun updateFeedbackStatusInCloud(id: String, status: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val client = SupabaseClientManager.client
            val postgrest = client.postgrest
            val isCompleted = (status == STATUS_SOLVED || status == STATUS_ACCEPTED || status == STATUS_COMPLETED)

            // Intentar actualizar status e is_completed en Supabase
            try {
                postgrest.from(TABLE_NAME).update(
                    mapOf(
                        "status" to status,
                        "is_completed" to isCompleted
                    )
                ) {
                    filter {
                        eq("id", id)
                    }
                }
            } catch (ignored: Exception) {
                // Fallback por si la columna is_completed no existe
                postgrest.from(TABLE_NAME).update(
                    mapOf("status" to status)
                ) {
                    filter {
                        eq("id", id)
                    }
                }
            }

            Log.d(TAG, "Estado de reporte $id actualizado en nube a $status")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo actualizar en nube el estado del reporte $id: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateFeedbackStatusInCloud(id: String, completed: Boolean): Result<Unit> =
        updateFeedbackStatusInCloud(id, if (completed) STATUS_COMPLETED else STATUS_PENDING)

    /**
     * Determina si un mapa de mensaje corresponde a un ticket/mensaje de soporte o comunicación oficial.
     */
    fun isSupportMessage(m: Map<String, Any>?): Boolean {
        if (m == null) return false
        val tag = (m["tag"] as? String)?.uppercase(Locale.ROOT) ?: ""
        val title = (m["title"] as? String) ?: ""
        val sender = (m["sender"] as? String) ?: ""
        val hasReportId = m.containsKey("reportId") && (m["reportId"] as? String)?.isNotBlank() == true
        val hasAdminReply = m.containsKey("adminReply") && (m["adminReply"] as? String)?.isNotBlank() == true
        val hasConversation = m.containsKey("conversation") && (m["conversation"] as? List<*>)?.isNotEmpty() == true
        val isSupportTag = tag in listOf("SUPPORT", "SOPORTE", "REPORTE", "TICKET", "PATROCINADOR", "SPONSOR", "PATROCINIO")
        val isSupportTitle = title.startsWith("Soporte:", ignoreCase = true) ||
                title.startsWith("Reporte:", ignoreCase = true) ||
                title.startsWith("Patrocinio:", ignoreCase = true) ||
                title.contains("Ticket de soporte", ignoreCase = true) ||
                title.contains("Soporte de Coach", ignoreCase = true)
        val isSupportSender = sender.contains("Soporte", ignoreCase = true) ||
                sender.contains("Patrocinador", ignoreCase = true) ||
                (sender.equals("Equipo Coach", ignoreCase = true) && (hasReportId || hasAdminReply || hasConversation || isSupportTitle))
        return isSupportTag || isSupportTitle || isSupportSender || hasReportId || hasAdminReply || hasConversation
    }

    /**
     * Obtiene el conjunto de IDs y títulos de tickets de soporte y DMs actualmente activos en el sistema.
     * Combina tanto Supabase como Firestore para garantizar sincronización multidispositivo sin pérdida de mensajes.
     */
    suspend fun getActiveSupportReportIds(): Set<String> = withContext(Dispatchers.IO) {
        val activeIds = mutableSetOf<String>()
        try {
            // 1. Supabase (Panel de soporte en la nube)
            try {
                val client = SupabaseClientManager.client
                val postgrest = client.postgrest
                val list = postgrest.from(TABLE_NAME)
                    .select {
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<FeedbackReport>()
                    .filter { !it.type.equals("SPONSOR_AD", ignoreCase = true) }

                for (fb in list) {
                    val rawType = fb.type.trim().uppercase(Locale.US)
                    val isSupport = rawType in listOf("SOPORTE", "SUPPORT", "TICKET", "AYUDA", "PATROCINADOR", "SPONSOR") ||
                            fb.title.contains("Soporte", ignoreCase = true) ||
                            fb.title.contains("Ticket", ignoreCase = true) ||
                            fb.title.contains("Patrocinio", ignoreCase = true)
                    if (isSupport) {
                        fb.id?.let { activeIds.add(it) }
                        if (fb.title.isNotBlank()) {
                            val cleanT = fb.title.trim()
                            activeIds.add(cleanT)
                            activeIds.add("Soporte: $cleanT")
                            activeIds.add("Reporte: $cleanT")
                            activeIds.add("Patrocinio: $cleanT")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error consultando Supabase para activeIds: ${e.message}")
            }

            // 2. Firestore support_reports (Sincronización en tiempo real multidispositivo)
            try {
                val db = FirebaseFirestore.getInstance()
                val fireSnap = db.collection("support_reports").get().await()
                for (doc in fireSnap.documents) {
                    val isDeleted = doc.getBoolean("isDeleted") == true ||
                            doc.getBoolean("deleted") == true ||
                            (doc.getString("status") ?: "").uppercase(Locale.US) in listOf("ELIMINADO", "DELETED", "CERRADO")
                    if (!isDeleted) {
                        activeIds.add(doc.id)
                        doc.getString("id")?.let { if (it.isNotBlank()) activeIds.add(it) }
                        doc.getString("reportId")?.let { if (it.isNotBlank()) activeIds.add(it) }
                        val t = (doc.getString("title") ?: "").trim()
                        if (t.isNotBlank()) {
                            activeIds.add(t)
                            activeIds.add("Soporte: $t")
                            activeIds.add("Reporte: $t")
                            activeIds.add("Patrocinio: $t")
                            val cleanNoPrefix = t.removePrefix("Soporte: ").removePrefix("Reporte: ").removePrefix("Patrocinio: ").trim()
                            if (cleanNoPrefix.isNotBlank()) {
                                activeIds.add(cleanNoPrefix)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error consultando Firestore para activeIds: ${e.message}")
            }
        } catch (_: Exception) {}
        activeIds
    }

    /**
     * Sincroniza y purga de la bandeja del usuario cualquier mensaje de soporte huérfano.
     * Si no existe ningún mensaje de soporte en el sistema, purga todos los mensajes de soporte del usuario.
     */
    suspend fun syncAndPurgeOrphansForUser(
        context: Context,
        userUid: String,
        userEmail: String
    ): Set<String> = withContext(Dispatchers.IO) {
        val activeIds = getActiveSupportReportIds()
        if (userUid.isBlank() || userUid == "anonimo") return@withContext activeIds

        try {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(userUid)

            // 1. Mensajes en subcolección messages
            try {
                val msgsSnap = userRef.collection("messages").get().await()
                var deletedCount = 0
                for (doc in msgsSnap.documents) {
                    val data = doc.data ?: continue
                    if (isSupportMessage(data)) {
                        val mId = doc.id
                        val rId = doc.getString("reportId") ?: mId
                        val title = (doc.getString("title") ?: "").trim()
                        val cleanTitle = title.removePrefix("Soporte: ").removePrefix("Reporte: ").trim()
                        val shouldKeep = activeIds.isNotEmpty() && (
                            activeIds.contains(mId) || 
                            activeIds.contains(rId) || 
                            activeIds.contains(title) || 
                            activeIds.contains(cleanTitle)
                        )
                        if (!shouldKeep) {
                            try { doc.reference.delete().await() } catch (_: Exception) {}
                            deletedCount++
                        }
                    }
                }
                if (deletedCount > 0) {
                    Log.d(TAG, "Se purgaron $deletedCount mensajes de soporte huérfanos de la subcolección messages para $userUid")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error purgando subcolección messages: ${e.message}")
            }

            // 2. Limpiar array privateMessages del documento de usuario
            try {
                val userSnap = userRef.get().await()
                if (userSnap.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val pMsgs = userSnap.get("privateMessages") as? List<Map<String, Any>>
                    if (!pMsgs.isNullOrEmpty()) {
                        val cleaned = pMsgs.filterNot { m ->
                            if (isSupportMessage(m)) {
                                val mId = m["id"] as? String ?: ""
                                val rId = m["reportId"] as? String ?: mId
                                val title = (m["title"] as? String ?: "").trim()
                                val cleanTitle = title.removePrefix("Soporte: ").removePrefix("Reporte: ").trim()
                                val shouldKeep = activeIds.isNotEmpty() && (
                                    activeIds.contains(mId) || 
                                    activeIds.contains(rId) || 
                                    activeIds.contains(title) || 
                                    activeIds.contains(cleanTitle)
                                )
                                !shouldKeep
                            } else {
                                false
                            }
                        }
                        userRef.update("privateMessages", cleaned).await()
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error purgando privateMessages: ${e.message}")
            }

            // 3. Purgar tickets huérfanos en support_reports de este usuario
            try {
                val myReports = db.collection("support_reports").whereEqualTo("userId", userUid).get().await()
                for (doc in myReports.documents) {
                    val mId = doc.id
                    val rId = doc.getString("reportId") ?: mId
                    val title = (doc.getString("title") ?: "").trim()
                    val cleanTitle = title.removePrefix("Soporte: ").removePrefix("Reporte: ").trim()
                    val shouldKeep = activeIds.isNotEmpty() && (
                        activeIds.contains(mId) || 
                        activeIds.contains(rId) || 
                        activeIds.contains(title) || 
                        activeIds.contains(cleanTitle)
                    )
                    if (!shouldKeep) {
                        try { doc.reference.delete().await() } catch (_: Exception) {}
                    }
                }
            } catch (_: Exception) {}

            if (userEmail.isNotBlank()) {
                try {
                    val myEmailReports = db.collection("support_reports").whereEqualTo("userEmail", userEmail).get().await()
                    for (doc in myEmailReports.documents) {
                        val mId = doc.id
                        val rId = doc.getString("reportId") ?: mId
                        val title = (doc.getString("title") ?: "").trim()
                        val cleanTitle = title.removePrefix("Soporte: ").removePrefix("Reporte: ").trim()
                        val shouldKeep = activeIds.isNotEmpty() && (
                            activeIds.contains(mId) || 
                            activeIds.contains(rId) || 
                            activeIds.contains(title) || 
                            activeIds.contains(cleanTitle)
                        )
                        if (!shouldKeep) {
                            try { doc.reference.delete().await() } catch (_: Exception) {}
                        }
                    }
                } catch (_: Exception) {}
            }

            // Asegurar contadores limpios si activeIds está vacío
            if (activeIds.isEmpty()) {
                try {
                    userRef.update(
                        mapOf(
                            "hasUnreadMessages" to false,
                            "unreadMessagesCount" to 0
                        )
                    ).await()
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error en syncAndPurgeOrphansForUser: ${e.message}")
        }
        activeIds
    }

    /**
     * Obtiene todos los reportes ordenados de más reciente a más antiguo para el Panel de Soporte y Administrador.
     * Sincroniza tanto Supabase como Firestore para garantizar visibilidad total y unificada.
     */
    suspend fun getAllFeedbacks(): Result<List<FeedbackReport>> = withContext(Dispatchers.IO) {
        val combinedList = mutableListOf<FeedbackReport>()
        val seenIds = mutableSetOf<String>()
        val seenTitles = mutableSetOf<String>()

        try {
            // 1. Supabase
            try {
                val client = SupabaseClientManager.client
                val postgrest = client.postgrest
                val list = postgrest.from(TABLE_NAME)
                    .select {
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<FeedbackReport>()
                    .filter { !it.type.equals("SPONSOR_AD", ignoreCase = true) }

                for (item in list) {
                    item.id?.let { seenIds.add(it) }
                    if (item.title.isNotBlank()) seenTitles.add(item.title.trim())
                    combinedList.add(item)
                }
                Log.d(TAG, "Se obtuvieron ${list.size} reportes de Supabase")
            } catch (e: Exception) {
                Log.w(TAG, "Error al obtener feedbacks de Supabase: ${e.message}")
            }

            // 2. Firestore support_reports
            try {
                val db = FirebaseFirestore.getInstance()
                val fireSnap = db.collection("support_reports")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(100)
                    .get()
                    .await()

                for (doc in fireSnap.documents) {
                    val docId = doc.id
                    val title = doc.getString("title") ?: ""
                    val isDeleted = doc.getBoolean("isDeleted") == true ||
                            doc.getBoolean("deleted") == true ||
                            (doc.getString("status") ?: "").uppercase(Locale.US) in listOf("ELIMINADO", "DELETED", "CERRADO")
                    if (isDeleted) continue

                    val rawStatus = doc.getString("status") ?: "PENDIENTE"
                    val normalizedStatus = when (rawStatus.uppercase(Locale.US)) {
                        "SOLVED", "SOLUCIONADO", "RESUELTO" -> STATUS_SOLVED
                        "READ", "LEIDO", "LEÍDO" -> STATUS_READ
                        "ACCEPTED", "ACEPTADA", "ACEPTADO" -> STATUS_ACCEPTED
                        "REJECTED", "RECHAZADA", "RECHAZADO" -> STATUS_REJECTED
                        else -> STATUS_PENDING
                    }
                    val adminReply = doc.getString("adminReply")
                    val repliedBy = doc.getString("repliedBy")
                    val repliedEmail = doc.getString("repliedEmail")

                    // Si ya existe en combinedList (desde Supabase), sincronizar su estado y respuesta más recientes de Firestore
                    val existingIdx = combinedList.indexOfFirst {
                        it.id == docId || (title.isNotBlank() && it.title.trim().equals(title.trim(), ignoreCase = true))
                    }
                    if (existingIdx != -1) {
                        val existing = combinedList[existingIdx]
                        val finalReply = if (!adminReply.isNullOrBlank()) adminReply else existing.adminReply
                        val finalRepliedBy = if (!repliedBy.isNullOrBlank()) repliedBy else existing.repliedBy
                        val finalRepliedEmail = if (!repliedEmail.isNullOrBlank()) repliedEmail else existing.repliedEmail
                        combinedList[existingIdx] = existing.copy(
                            status = normalizedStatus,
                            adminReply = finalReply,
                            repliedBy = finalRepliedBy,
                            repliedEmail = finalRepliedEmail
                        )
                        continue
                    }

                    if (seenIds.contains(docId) || (title.isNotBlank() && seenTitles.contains(title.trim()))) {
                        continue
                    }

                    val desc = doc.getString("description") ?: ""
                    val userEmail = doc.getString("userEmail") ?: ""
                    val userName = doc.getString("userName") ?: ""
                    val appVer = doc.getString("appVersion") ?: ""
                    val dev = doc.getString("device") ?: ""
                    val ts = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                    val isoDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }.format(Date(ts))

                    val converted = FeedbackReport(
                        id = docId,
                        type = "SOPORTE",
                        title = title.ifBlank { "Ticket de soporte" },
                        description = if (userEmail.isNotBlank()) "Correo de contacto: $userEmail\n$desc" else desc,
                        appVersion = appVer,
                        deviceInfo = dev,
                        createdAt = isoDate,
                        status = normalizedStatus,
                        adminReply = adminReply,
                        repliedBy = repliedBy,
                        repliedEmail = repliedEmail
                    )
                    seenIds.add(docId)
                    if (title.isNotBlank()) seenTitles.add(title.trim())
                    combinedList.add(converted)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error al obtener soporte de Firestore: ${e.message}")
            }

            // Ordenar por fecha descendente
            combinedList.sortByDescending { it.createdAt }

            Result.success(combinedList)
        } catch (e: Exception) {
            Log.e(TAG, "Error general en getAllFeedbacks: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Elimina un reporte específico por su ID UUID.
     */
    suspend fun deleteFeedback(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        deleteFeedback(com.example.data.remote.model.FeedbackReport(id = id, title = ""))
    }

    /**
     * Elimina un reporte específico de forma definitiva:
     * 1. Elimina de Supabase.
     * 2. Elimina de Firestore support_reports.
     * 3. Purga de la bandeja de entrada de los usuarios mediante collectionGroup("messages").
     * 4. Limpia el array privateMessages del documento de usuario.
     */
    suspend fun deleteFeedback(report: com.example.data.remote.model.FeedbackReport): Result<Unit> = withContext(Dispatchers.IO) {
        val id = report.id ?: return@withContext Result.failure(Exception("ID nulo"))
        try {
            val client = SupabaseClientManager.client
            val postgrest = client.postgrest

            // 1. Eliminar de Supabase
            try {
                postgrest.from(TABLE_NAME).delete {
                    filter {
                        eq("id", id)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error eliminando de Supabase: ${e.message}")
            }

            // 2. Eliminar de Firestore (support_reports, collectionGroup messages y usuarios)
            try {
                val db = FirebaseFirestore.getInstance()
                val docsToDelete = mutableSetOf<String>()
                docsToDelete.add(id)

                // Buscar documentos en support_reports por ID directo
                val directDoc = db.collection("support_reports").document(id).get().await()
                if (directDoc.exists()) {
                    docsToDelete.add(directDoc.id)
                }

                // Buscar por campos id, reportId o título en support_reports
                try {
                    val snap1 = db.collection("support_reports").whereEqualTo("id", id).get().await()
                    for (d in snap1.documents) docsToDelete.add(d.id)
                    val snap2 = db.collection("support_reports").whereEqualTo("reportId", id).get().await()
                    for (d in snap2.documents) docsToDelete.add(d.id)
                } catch (_: Exception) {}

                if (report.title.isNotBlank()) {
                    try {
                        val snap3 = db.collection("support_reports").whereEqualTo("title", report.title.trim()).get().await()
                        for (d in snap3.documents) docsToDelete.add(d.id)
                    } catch (_: Exception) {}
                }

                // Purgar documentos de la colección support_reports
                for (fId in docsToDelete) {
                    val docSnap = db.collection("support_reports").document(fId).get().await()
                    val uid = if (docSnap.exists()) docSnap.getString("userId") else null
                    val email = if (docSnap.exists()) docSnap.getString("userEmail") else null

                    // Eliminar de support_reports
                    try { db.collection("support_reports").document(fId).delete().await() } catch (_: Exception) {}

                    // Eliminar de la bandeja del usuario directamente si tenemos el uid
                    if (!uid.isNullOrBlank() && uid != "anonimo") {
                        try { db.collection("users").document(uid).collection("messages").document(fId).delete().await() } catch (_: Exception) {}
                        try { db.collection("users").document(uid).collection("messages").document(id).delete().await() } catch (_: Exception) {}

                        try {
                            val uRef = db.collection("users").document(uid)
                            val uSnap = uRef.get().await()
                            if (uSnap.exists()) {
                                @Suppress("UNCHECKED_CAST")
                                val pMsgs = uSnap.get("privateMessages") as? List<Map<String, Any>>
                                if (pMsgs != null) {
                                    val updated = pMsgs.filterNot { m ->
                                        val mId = m["id"] as? String ?: ""
                                        val rId = m["reportId"] as? String ?: ""
                                        mId == fId || mId == id || rId == fId || rId == id
                                    }
                                    uRef.update("privateMessages", updated).await()
                                }
                            }
                        } catch (_: Exception) {}
                    }

                    if (!email.isNullOrBlank()) {
                        try {
                            val usersByEmail = db.collection("users").whereEqualTo("email", email).get().await()
                            for (uDoc in usersByEmail.documents) {
                                try { db.collection("users").document(uDoc.id).collection("messages").document(fId).delete().await() } catch (_: Exception) {}
                                try { db.collection("users").document(uDoc.id).collection("messages").document(id).delete().await() } catch (_: Exception) {}
                                @Suppress("UNCHECKED_CAST")
                                val pMsgs = uDoc.get("privateMessages") as? List<Map<String, Any>>
                                if (pMsgs != null) {
                                    val updated = pMsgs.filterNot { m ->
                                        val mId = m["id"] as? String ?: ""
                                        val rId = m["reportId"] as? String ?: ""
                                        mId == fId || mId == id || rId == fId || rId == id
                                    }
                                    db.collection("users").document(uDoc.id).update("privateMessages", updated).await()
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }

                // 3. Purga en collectionGroup("messages") para asegurar eliminación en bandejas de entrada
                try {
                    val groupRefsToDelete = mutableSetOf<DocumentReference>()
                    try {
                        val r1 = db.collectionGroup("messages").whereEqualTo("reportId", id).get().await()
                        for (d in r1.documents) groupRefsToDelete.add(d.reference)
                    } catch (_: Exception) {}
                    try {
                        val r2 = db.collectionGroup("messages").whereEqualTo("id", id).get().await()
                        for (d in r2.documents) groupRefsToDelete.add(d.reference)
                    } catch (_: Exception) {}

                    for (fId in docsToDelete) {
                        if (fId != id) {
                            try {
                                val rf1 = db.collectionGroup("messages").whereEqualTo("reportId", fId).get().await()
                                for (d in rf1.documents) groupRefsToDelete.add(d.reference)
                            } catch (_: Exception) {}
                            try {
                                val rf2 = db.collectionGroup("messages").whereEqualTo("id", fId).get().await()
                                for (d in rf2.documents) groupRefsToDelete.add(d.reference)
                            } catch (_: Exception) {}
                        }
                    }

                    if (report.title.isNotBlank()) {
                        val cleanTitle = report.title.trim()
                        try {
                            val rt1 = db.collectionGroup("messages").whereEqualTo("title", "Soporte: $cleanTitle").get().await()
                            for (d in rt1.documents) groupRefsToDelete.add(d.reference)
                        } catch (_: Exception) {}
                        try {
                            val rt2 = db.collectionGroup("messages").whereEqualTo("title", "Reporte: $cleanTitle").get().await()
                            for (d in rt2.documents) groupRefsToDelete.add(d.reference)
                        } catch (_: Exception) {}
                    }

                    for (ref in groupRefsToDelete) {
                        try { ref.delete().await() } catch (_: Exception) {}
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error en purga de collectionGroup messages: ${e.message}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error eliminando reporte de Firestore: ${e.message}")
            }

            Log.d(TAG, "Reporte $id eliminado exitosamente de todas las fuentes")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar reporte $id: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Purga absolutamente todos los mensajes y tickets de soporte en todo el sistema (Firestore y bandejas).
     */
    suspend fun purgeAllSupportMessagesAcrossSystem(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val db = FirebaseFirestore.getInstance()

            // 1. Eliminar todos los documentos de support_reports
            try {
                val allReports = db.collection("support_reports").get().await()
                for (doc in allReports.documents) {
                    try { doc.reference.delete().await() } catch (_: Exception) {}
                }
                Log.d(TAG, "Se eliminaron ${allReports.size()} documentos de support_reports")
            } catch (e: Exception) {
                Log.w(TAG, "Error eliminando support_reports: ${e.message}")
            }

            // 2. Eliminar mensajes de soporte en collectionGroup("messages")
            try {
                val tags = listOf("SUPPORT", "SOPORTE", "REPORTE", "TICKET")
                val refsToDelete = mutableSetOf<DocumentReference>()
                for (t in tags) {
                    try {
                        val snap = db.collectionGroup("messages").whereEqualTo("tag", t).get().await()
                        for (d in snap.documents) refsToDelete.add(d.reference)
                    } catch (_: Exception) {}
                }

                try {
                    val allMsgSnap = db.collectionGroup("messages").get().await()
                    for (d in allMsgSnap.documents) {
                        val data = d.data
                        if (isSupportMessage(data)) {
                            refsToDelete.add(d.reference)
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error escaneando mensajes en collectionGroup: ${e.message}")
                }

                for (ref in refsToDelete) {
                    try { ref.delete().await() } catch (_: Exception) {}
                }
                Log.d(TAG, "Se eliminaron ${refsToDelete.size} mensajes de soporte en bandejas de entrada")
            } catch (e: Exception) {
                Log.w(TAG, "Error purgando collectionGroup messages: ${e.message}")
            }

            // 3. Limpiar arrays privateMessages en la colección de usuarios
            try {
                val usersSnap = db.collection("users").get().await()
                for (uDoc in usersSnap.documents) {
                    @Suppress("UNCHECKED_CAST")
                    val pMsgs = uDoc.get("privateMessages") as? List<Map<String, Any>>
                    if (!pMsgs.isNullOrEmpty()) {
                        val cleaned = pMsgs.filterNot { isSupportMessage(it) }
                        uDoc.reference.update(
                            mapOf(
                                "privateMessages" to cleaned,
                                "hasUnreadMessages" to false,
                                "unreadMessagesCount" to 0
                            )
                        ).await()
                    } else {
                        uDoc.reference.update(
                            mapOf(
                                "hasUnreadMessages" to false,
                                "unreadMessagesCount" to 0
                            )
                        ).await()
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error limpiando privateMessages en users: ${e.message}")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error en purgeAllSupportMessagesAcrossSystem: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Elimina todos los reportes de la tabla de Supabase y purga completamente Firestore y las bandejas.
     */
    suspend fun clearAllFeedbacks(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val client = SupabaseClientManager.client
            val postgrest = client.postgrest

            try {
                postgrest.from(TABLE_NAME).delete {
                    filter {
                        neq("type", "___DUMMY_NEVER_MATCH___")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error limpiando tabla Supabase: ${e.message}")
            }

            // Purgar todo soporte en Firestore y bandejas de usuario
            purgeAllSupportMessagesAcrossSystem()

            Log.d(TAG, "Todos los reportes han sido eliminados de forma global")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al limpiar todos los reportes: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Elimina manualmente o por mantenimiento los reportes con más de [days] días de antigüedad.
     */
    suspend fun purgeOldReports(days: Int = 7): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val client = SupabaseClientManager.client
            val postgrest = client.postgrest

            // Calcular fecha límite ISO-8601 (hace N días)
            val cutoffMillis = System.currentTimeMillis() - (days.toLong() * 24 * 60 * 60 * 1000L)
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val cutoffIso = sdf.format(Date(cutoffMillis))

            postgrest.from(TABLE_NAME).delete {
                filter {
                    lt("created_at", cutoffIso)
                }
            }
            Log.d(TAG, "Purga completada: eliminados reportes anteriores a $cutoffIso")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Fallo al purgar reportes antiguos: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Parsea fechas ISO-8601 a milisegundos de forma segura.
     */
    fun parseIsoToMillis(dateStr: String?): Long {
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

    /**
     * Purga automáticamente los reportes expirados según la política de retención:
     * - 30 días para mensajes ya leídos o solucionados.
     * - 60 días para mensajes aún no leídos / pendientes.
     */
    suspend fun autoPurgeExpiredReports(context: Context): Int = withContext(Dispatchers.IO) {
        try {
            val result = getAllFeedbacks()
            if (!result.isSuccess) return@withContext 0
            val list = result.getOrNull() ?: return@withContext 0
            val now = System.currentTimeMillis()
            var purged = 0

            for (report in list) {
                val status = getReportStatus(context, report)
                val isRead = status == STATUS_READ || status == STATUS_SOLVED || status == STATUS_ACCEPTED || status == STATUS_COMPLETED
                val maxDays = if (isRead) 30 else 60
                val maxLifespan = maxDays * 24L * 60 * 60 * 1000L
                val createdMillis = parseIsoToMillis(report.createdAt)

                if (now - createdMillis >= maxLifespan) {
                    val id = report.id
                    if (!id.isNullOrBlank()) {
                        try {
                            deleteFeedback(id)
                            purged++
                            Log.d(TAG, "Reporte expirado eliminado automáticamente (${maxDays}d): $id")
                        } catch (e: Exception) {
                            Log.w(TAG, "Error eliminando reporte expirado $id: ${e.message}")
                        }
                    }
                }
            }
            purged
        } catch (e: Exception) {
            Log.w(TAG, "Error durante autoPurgeExpiredReports: ${e.message}")
            0
        }
    }
}
