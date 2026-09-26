package com.example.data.supabase

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.BuildConfig
import com.example.data.WildRiftRepository
import com.example.data.remote.model.FeedbackReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.SetOptions

object FeedbackRepository {

    private const val TAG = "FeedbackRepository"
    private const val COLLECTION_REPORTS = "support_reports"
    private const val COLLECTION_FEEDBACKS = "feedbacks"
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
     * Envía un reporte o sugerencia a Firebase Firestore y purga automáticamente
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
            val db = FirebaseFirestore.getInstance()
            val reportId = id ?: UUID.randomUUID().toString()

            // 1. Purga automática de reportes antiguos en segundo plano
            try {
                purgeOldReports(60)
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo realizar la purga automática: ${e.message}")
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

            val docData = hashMapOf(
                "id" to reportId,
                "reportId" to reportId,
                "type" to type,
                "title" to title.trim(),
                "description" to finalDescription,
                "appVersion" to appVersion,
                "deviceInfo" to deviceInfo,
                "device" to deviceInfo,
                "userEmail" to (email?.trim() ?: ""),
                "userName" to (userName?.trim() ?: ""),
                "status" to STATUS_PENDING,
                "is_completed" to false,
                "isDeleted" to false,
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            // 3. Guardar en Firebase Firestore
            db.collection(COLLECTION_REPORTS).document(reportId).set(docData).await()
            try {
                db.collection(COLLECTION_FEEDBACKS).document(reportId).set(docData).await()
            } catch (_: Exception) {}

            Log.d(TAG, "Feedback enviado exitosamente a Firebase Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando feedback a Firebase: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene el estado actual de un reporte/sugerencia.
     */
    fun getReportStatus(context: Context, report: FeedbackReport): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = report.id

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

        if (!id.isNullOrBlank()) {
            val localStatus = prefs.getString(PREF_STATUS_PREFIX + id, null)
            if (!localStatus.isNullOrBlank()) return localStatus
        }

        return STATUS_PENDING
    }

    fun setFeedbackStatus(context: Context, report: FeedbackReport, newStatus: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val id = report.id
        val compositeKey = if (report.createdAt != null) "${report.title}_${report.createdAt}" else null

        if (!id.isNullOrBlank()) {
            editor.putString(PREF_STATUS_PREFIX + id, newStatus)
        }
        if (!compositeKey.isNullOrBlank()) {
            editor.putString(PREF_STATUS_PREFIX + compositeKey, newStatus)
        }

        val currentSet = prefs.getStringSet(KEY_COMPLETED_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        val isDone = (newStatus == STATUS_SOLVED || newStatus == STATUS_ACCEPTED || newStatus == STATUS_COMPLETED)
        if (isDone) {
            if (!id.isNullOrBlank()) currentSet.add(id)
            if (!compositeKey.isNullOrBlank()) currentSet.add(compositeKey)
        } else {
            if (!id.isNullOrBlank()) currentSet.remove(id)
            if (!compositeKey.isNullOrBlank()) currentSet.remove(compositeKey)
        }
        editor.putStringSet(KEY_COMPLETED_IDS, currentSet)
        editor.apply()

        if (!id.isNullOrBlank()) {
            kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                updateFeedbackStatusInCloud(id, newStatus)
            }
        }
    }

    fun getCompletedFeedbackIds(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_COMPLETED_IDS, emptySet())?.toSet() ?: emptySet()
    }

    fun setFeedbackCompleted(context: Context, report: FeedbackReport, completed: Boolean) {
        val newStatus = if (completed) {
            if (report.type.equals("SUGGESTION", ignoreCase = true)) STATUS_ACCEPTED else STATUS_SOLVED
        } else {
            STATUS_PENDING
        }
        setFeedbackStatus(context, report, newStatus)
    }

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

    suspend fun updateFeedbackStatusInCloud(id: String, status: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val db = FirebaseFirestore.getInstance()
            val isCompleted = (status == STATUS_SOLVED || status == STATUS_ACCEPTED || status == STATUS_COMPLETED)
            val updates = mapOf(
                "status" to status,
                "is_completed" to isCompleted
            )

            db.collection(COLLECTION_REPORTS).document(id).set(updates, SetOptions.merge()).await()
            try {
                db.collection(COLLECTION_FEEDBACKS).document(id).set(updates, SetOptions.merge()).await()
            } catch (_: Exception) {}

            Log.d(TAG, "Estado de reporte $id actualizado en Firebase a $status")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo actualizar en Firebase el estado del reporte $id: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateFeedbackStatusInCloud(id: String, completed: Boolean): Result<Unit> =
        updateFeedbackStatusInCloud(id, if (completed) STATUS_COMPLETED else STATUS_PENDING)

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

    suspend fun getActiveSupportReportIds(): Set<String> = withContext(Dispatchers.IO) {
        val activeIds = mutableSetOf<String>()
        try {
            val db = FirebaseFirestore.getInstance()
            val snap = db.collection(COLLECTION_REPORTS).limit(100).get().await()
            for (doc in snap.documents) {
                if (doc.getBoolean("isDeleted") == true) continue
                activeIds.add(doc.id)
                val rId = doc.getString("reportId")
                if (!rId.isNullOrBlank()) activeIds.add(rId)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error consultando activeIds de Firebase: ${e.message}")
        }
        activeIds
    }

    suspend fun autoPurgeExpiredReports(context: Context, days: Int = 7): Int = withContext(Dispatchers.IO) {
        try {
            val db = FirebaseFirestore.getInstance()
            val cutoffMillis = System.currentTimeMillis() - (days.toLong() * 24 * 60 * 60 * 1000L)
            val cutoffTimestamp = com.google.firebase.Timestamp(Date(cutoffMillis))

            val oldDocs = db.collection(COLLECTION_REPORTS)
                .whereLessThan("createdAt", cutoffTimestamp)
                .get()
                .await()

            var count = 0
            for (d in oldDocs.documents) {
                try {
                    d.reference.delete().await()
                    count++
                } catch (_: Exception) {}
            }
            count
        } catch (e: Exception) {
            Log.w(TAG, "Error en autoPurgeExpiredReports: ${e.message}")
            0
        }
    }

    suspend fun syncAndPurgeOrphansForUser(
        context: Context,
        userId: String? = null,
        userEmail: String? = null
    ): Set<String> = withContext(Dispatchers.IO) {
        val activeIds = mutableSetOf<String>()
        try {
            val db = FirebaseFirestore.getInstance()
            val snap = db.collection(COLLECTION_REPORTS).limit(100).get().await()
            for (doc in snap.documents) {
                if (doc.getBoolean("isDeleted") == true) continue
                activeIds.add(doc.id)
                val rId = doc.getString("reportId")
                if (!rId.isNullOrBlank()) activeIds.add(rId)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error en syncAndPurgeOrphansForUser: ${e.message}")
        }
        activeIds
    }

    suspend fun getAllFeedbacks(): Result<List<FeedbackReport>> = withContext(Dispatchers.IO) {
        val combinedList = mutableListOf<FeedbackReport>()
        val seenIds = mutableSetOf<String>()
        val seenTitles = mutableSetOf<String>()

        try {
            val db = FirebaseFirestore.getInstance()
            val fireSnap = db.collection(COLLECTION_REPORTS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .await()

            for (doc in fireSnap.documents) {
                val docId = doc.id
                val isDeleted = doc.getBoolean("isDeleted") == true ||
                        doc.getBoolean("deleted") == true ||
                        (doc.getString("status") ?: "").uppercase(Locale.US) in listOf("ELIMINADO", "DELETED", "CERRADO")
                if (isDeleted) continue

                val title = doc.getString("title") ?: ""
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

                val desc = doc.getString("description") ?: ""
                val userEmail = doc.getString("userEmail") ?: ""
                val appVer = doc.getString("appVersion") ?: ""
                val dev = doc.getString("device") ?: doc.getString("deviceInfo") ?: ""
                val type = doc.getString("type") ?: "SOPORTE"
                val ts = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                val isoDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(Date(ts))

                val converted = FeedbackReport(
                    id = docId,
                    type = type,
                    title = title.ifBlank { "Ticket de soporte" },
                    description = if (userEmail.isNotBlank() && !desc.contains("Correo de contacto")) "Correo de contacto: $userEmail\n$desc" else desc,
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

            combinedList.sortByDescending { it.createdAt }
            Result.success(combinedList)
        } catch (e: Exception) {
            Log.e(TAG, "Error en getAllFeedbacks de Firebase: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteFeedback(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        deleteFeedback(FeedbackReport(id = id, title = ""))
    }

    suspend fun deleteFeedback(report: FeedbackReport): Result<Unit> = withContext(Dispatchers.IO) {
        val id = report.id ?: return@withContext Result.failure(Exception("ID nulo"))
        try {
            val db = FirebaseFirestore.getInstance()
            val docsToDelete = mutableSetOf<String>()
            docsToDelete.add(id)

            val directDoc = db.collection(COLLECTION_REPORTS).document(id).get().await()
            if (directDoc.exists()) {
                docsToDelete.add(directDoc.id)
            }

            try {
                val snap1 = db.collection(COLLECTION_REPORTS).whereEqualTo("id", id).get().await()
                for (d in snap1.documents) docsToDelete.add(d.id)
                val snap2 = db.collection(COLLECTION_REPORTS).whereEqualTo("reportId", id).get().await()
                for (d in snap2.documents) docsToDelete.add(d.id)
            } catch (_: Exception) {}

            for (fId in docsToDelete) {
                val docSnap = db.collection(COLLECTION_REPORTS).document(fId).get().await()
                val uid = if (docSnap.exists()) docSnap.getString("userId") else null
                val email = if (docSnap.exists()) docSnap.getString("userEmail") else null

                try { db.collection(COLLECTION_REPORTS).document(fId).delete().await() } catch (_: Exception) {}
                try { db.collection(COLLECTION_FEEDBACKS).document(fId).delete().await() } catch (_: Exception) {}

                if (!uid.isNullOrBlank() && uid != "anonimo") {
                    try { db.collection("users").document(uid).collection("messages").document(fId).delete().await() } catch (_: Exception) {}
                    try { db.collection("users").document(uid).collection("messages").document(id).delete().await() } catch (_: Exception) {}
                }

                if (!email.isNullOrBlank()) {
                    try {
                        val usersByEmail = db.collection("users").whereEqualTo("email", email).get().await()
                        for (uDoc in usersByEmail.documents) {
                            try { db.collection("users").document(uDoc.id).collection("messages").document(fId).delete().await() } catch (_: Exception) {}
                            try { db.collection("users").document(uDoc.id).collection("messages").document(id).delete().await() } catch (_: Exception) {}
                        }
                    } catch (_: Exception) {}
                }
            }

            // Purga en collectionGroup("messages")
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

                for (ref in groupRefsToDelete) {
                    try { ref.delete().await() } catch (_: Exception) {}
                }
            } catch (_: Exception) {}

            Log.d(TAG, "Reporte $id eliminado exitosamente de Firebase")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar reporte $id: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun purgeAllSupportMessagesAcrossSystem(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val db = FirebaseFirestore.getInstance()

            try {
                val allReports = db.collection(COLLECTION_REPORTS).get().await()
                for (doc in allReports.documents) {
                    try { doc.reference.delete().await() } catch (_: Exception) {}
                }
            } catch (_: Exception) {}

            try {
                val allFeedbacks = db.collection(COLLECTION_FEEDBACKS).get().await()
                for (doc in allFeedbacks.documents) {
                    try { doc.reference.delete().await() } catch (_: Exception) {}
                }
            } catch (_: Exception) {}

            try {
                val tags = listOf("SUPPORT", "SOPORTE", "REPORTE", "TICKET")
                val refsToDelete = mutableSetOf<DocumentReference>()
                for (t in tags) {
                    try {
                        val snap = db.collectionGroup("messages").whereEqualTo("tag", t).get().await()
                        for (d in snap.documents) refsToDelete.add(d.reference)
                    } catch (_: Exception) {}
                }

                for (ref in refsToDelete) {
                    try { ref.delete().await() } catch (_: Exception) {}
                }
            } catch (_: Exception) {}

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error en purgeAllSupportMessagesAcrossSystem: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun clearAllFeedbacks(): Result<Unit> = withContext(Dispatchers.IO) {
        purgeAllSupportMessagesAcrossSystem()
    }

    suspend fun purgeOldReports(days: Int = 7): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val db = FirebaseFirestore.getInstance()
            val cutoffMillis = System.currentTimeMillis() - (days.toLong() * 24 * 60 * 60 * 1000L)
            val cutoffTimestamp = com.google.firebase.Timestamp(Date(cutoffMillis))

            val oldDocs = db.collection(COLLECTION_REPORTS)
                .whereLessThan("createdAt", cutoffTimestamp)
                .get()
                .await()

            for (d in oldDocs.documents) {
                try { d.reference.delete().await() } catch (_: Exception) {}
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Error purgando reportes antiguos en Firebase: ${e.message}")
            Result.failure(e)
        }
    }
}
