package com.example.data

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.BuildConfig
import com.example.data.WildRiftRepository
import com.example.data.remote.model.FeedbackReport
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

object FeedbackRepository {

    private const val TAG = "FeedbackRepository"
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
     * Envía un reporte o sugerencia y purga automáticamente
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
            // 2. Preparar el nuevo reporte
            val baseDeviceInfo = "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT})"
            var deviceInfo = baseDeviceInfo
            if (imagesBase64.isNotEmpty()) {
                for (img in imagesBase64) {
                    deviceInfo += "\n\n[IMAGE_BASE64]\n$img"
                }
            }
            val appVersion = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) [${WildRiftRepository.CURRENT_PATCH_VERSION}]"

            val finalDescription = description.trim()

            val reportId = if (!id.isNullOrBlank()) id else java.util.UUID.randomUUID().toString()

            // 3. Guardar en almacenamiento en la nube (support_reports) para garantizar sincronización multidispositivo
            val db = FirebaseFirestore.getInstance()
            val account = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: error("Inicia sesión para enviar un mensaje")
            val now = System.currentTimeMillis()
            val history = SupportConversationPolicy.initial(reportId, userName ?: "Invocador", description.trim(), now)
            val firestoreMap = hashMapOf<String, Any>(
                "id" to reportId,
                "reportId" to reportId,
                "type" to type,
                "tag" to type,
                "title" to title.trim(),
                "description" to finalDescription,
                "content" to finalDescription,
                "userName" to (userName ?: "Usuario"),
                "userEmail" to account.email.orEmpty(),
                "contactEmail" to email.orEmpty(), "userId" to account.uid,
                "staffVisible" to !SupportConversationPolicy.isAdministratorOnly(type), "userCanReply" to false,
                "conversation" to history.map { SupportConversationPolicy.encode(it, if (it.senderRole == "USER") account.uid else "") },
                "userRead" to true, "isRead" to true, "hasNewAdminReply" to false, "staffRead" to false,
                "appVersion" to appVersion,
                "deviceInfo" to deviceInfo,
                "photos" to imagesBase64,
                "status" to STATUS_PENDING,
                "timestamp" to System.currentTimeMillis(),
                "createdAt" to com.google.firebase.Timestamp.now()
            )
            val batch = db.batch()
            batch.set(db.collection("support_reports").document(reportId), firestoreMap)
            batch.set(db.collection("users").document(account.uid).collection("messages").document(reportId),
                firestoreMap + mapOf("timestamp" to now))
            batch.commit().await()
            com.example.WildRiftApp.instance?.let { SupportReplyManager.saveConversation(it, reportId, history) }
            Log.d(TAG, "Feedback guardado exitosamente en la nube")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando feedback: ${e.message}", e)
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

        // 1. Revisar campo status remoto
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

        // 4. Si el mensaje ya ha sido respondido por soporte, no debe salir en espera/pendiente
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
     * Marca o desmarca un reporte como completado de forma persistente en SharedPreferences.
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
     * Comprueba si un reporte está completado.
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
     * Sincroniza el cambio de estado en la nube.
     */
    suspend fun updateFeedbackStatusInCloud(id: String, status: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val context = com.example.WildRiftApp.instance ?: error("Aplicación no disponible")
            check(SupportReplyManager.updateReportStatus(context, id, status)) { "No se pudo sincronizar el estado" }
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

    /** Reads only the current account's canonical tickets. Failures never trigger inbox deletion. */
    suspend fun getActiveSupportReportIds(): Set<String> = withContext(Dispatchers.IO) {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return@withContext emptySet()
        val reports = FirebaseFirestore.getInstance().collection("support_reports")
        val byId = reports.whereEqualTo("userId", user.uid).get().await().documents
        val byEmail = if (user.email.isNullOrBlank()) emptyList() else reports.whereEqualTo("userEmail", user.email).get().await().documents
        (byId + byEmail).filterNot { it.getBoolean("deleted") == true || it.getBoolean("isDeleted") == true }.map { it.id }.toSet()
    }

    /** Compatibility entry point: synchronize references without purging messages. */
    suspend fun syncAndPurgeOrphansForUser(context: Context, userUid: String, userEmail: String): Set<String> {
        check(com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid == userUid)
        return getActiveSupportReportIds()
    }

    /**
     * Obtiene todos los reportes ordenados de más reciente a más antiguo para el Panel de Soporte y Administrador.
     */
    suspend fun getAllFeedbacks(): Result<List<FeedbackReport>> = withContext(Dispatchers.IO) {
        val combinedList = mutableListOf<FeedbackReport>()
        val seenIds = mutableSetOf<String>()

        try {
            val db = FirebaseFirestore.getInstance()
            SupportTicketAccess.migrateLegacyVisibility()
            val fireSnap = SupportTicketAccess.staffQuery()
                .get()
                .await()

            combinedList.addAll(feedbacksFromSnapshot(fireSnap))

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
        deleteFeedback(FeedbackReport(id = id, title = ""))
    }

    /**
     * Elimina un reporte específico de forma definitiva.
     */
    suspend fun deleteFeedback(report: FeedbackReport, onlySolved: Boolean = false): Result<Unit> = withContext(Dispatchers.IO) {
        if (!SupportTicketAccess.isAdmin()) return@withContext Result.failure(SecurityException("Solo el administrador puede eliminar mensajes"))
        val id = report.id ?: return@withContext Result.failure(Exception("ID nulo"))
        try {
            val db = FirebaseFirestore.getInstance()
            val ref = db.collection("support_reports").document(id)
            db.runTransaction { transaction ->
                val ticket = transaction.get(ref)
                if (onlySolved && SupportTicketPresentation.status(ticket.getString("status").orEmpty()) != STATUS_SOLVED) return@runTransaction
                val owner = ticket.getString("userId").orEmpty()
                val ownerRef = owner.takeIf { it.isNotBlank() }?.let { db.collection("users").document(it) }
                val account = ownerRef?.let { transaction.get(it) }
                transaction.delete(ref)
                if (ownerRef != null) {
                    transaction.delete(ownerRef.collection("messages").document(id))
                    @Suppress("UNCHECKED_CAST")
                    val old = account?.get("privateMessages") as? List<Map<String, Any>>
                    if (old != null) transaction.update(ownerRef, "privateMessages",
                        old.filterNot { it["id"] == id || it["reportId"] == id || it["ticketId"] == id })
                }
            }.await()
            Result.success(Unit)
        } catch (error: Exception) {
            Log.e(TAG, "No se pudo eliminar el reporte $id", error)
            Result.failure(error)
        }
    }

    /** Converts one authoritative snapshot, excluding internal requests and removed tickets. */
    fun feedbacksFromSnapshot(snapshot: com.google.firebase.firestore.QuerySnapshot): List<FeedbackReport> =
        snapshot.documents.mapNotNull { doc ->
            SupportReportDecoder.decode(doc.id, doc.data.orEmpty())
        }.distinctBy { it.id }.sortedByDescending { SupportReportDecoder.timestampMillis(it.createdAt) ?: 0L }

    /** Bulk removal touches only solved tickets; pending and read messages remain. */
    suspend fun clearSolvedFeedbacks(): Result<Unit> = withContext(Dispatchers.IO) {
        if (!SupportTicketAccess.isAdmin()) return@withContext Result.failure(SecurityException("Solo el administrador puede eliminar mensajes"))
        runCatching {
            getAllFeedbacks().getOrThrow().filter { SupportTicketPresentation.status(it.status.orEmpty()) == STATUS_SOLVED }
                .forEach { deleteFeedback(it, onlySolved = true).getOrThrow() }
        }
    }

    /**
     * Elimina manualmente o por mantenimiento los reportes con más de [days] días de antigüedad.
     */
    suspend fun purgeOldReports(days: Int = 7): Result<Unit> = withContext(Dispatchers.IO) {
        if (!SupportTicketAccess.isAdmin()) return@withContext Result.failure(SecurityException("Solo el administrador puede eliminar mensajes"))
        try {
            val db = FirebaseFirestore.getInstance()
            val cutoffMillis = System.currentTimeMillis() - (days.toLong() * 24 * 60 * 60 * 1000L)
            val snapshot = db.collection("support_reports").get().await()
            for (doc in snapshot.documents) {
                val ts = doc.getTimestamp("createdAt")?.toDate()?.time ?: continue
                if (ts < cutoffMillis && SupportTicketPresentation.isUserTicket(doc.data.orEmpty())) {
                    deleteFeedback(doc.id).getOrThrow()
                }
            }
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
        if (!SupportTicketAccess.isAdmin()) return@withContext 0
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
                val createdMillis = SupportReportDecoder.timestampMillis(report.createdAt) ?: continue

                if (now - createdMillis >= maxLifespan) {
                    val id = report.id
                    if (!id.isNullOrBlank()) {
                        try {
                            deleteFeedback(id).getOrThrow()
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
