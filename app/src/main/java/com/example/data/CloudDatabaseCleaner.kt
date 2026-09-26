package com.example.data

import android.content.Context
import android.util.Log
import com.example.data.supabase.FeedbackRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class CloudCleanResult(
    val globalNotificationsDeleted: Int = 0,
    val userInboxesCleaned: Int = 0,
    val supportReportsDeleted: Int = 0,
    val totalDeleted: Int = 0,
    val success: Boolean = true,
    val errorMessage: String? = null
)

object CloudDatabaseCleaner {

    private const val TAG = "CloudDatabaseCleaner"

    /**
     * Elimina absolutamente todos los documentos de la colección 'global_notifications'.
     */
    suspend fun purgeGlobalNotifications(): Int = withContext(Dispatchers.IO) {
        var count = 0
        try {
            val db = FirebaseFirestore.getInstance()
            val snap = db.collection("global_notifications").get().await()
            for (doc in snap.documents) {
                try {
                    doc.reference.delete().await()
                    count++
                } catch (_: Exception) {}
            }
            Log.d(TAG, "Se eliminaron $count notificaciones globales de la nube")
        } catch (e: Exception) {
            Log.w(TAG, "Error eliminando global_notifications: ${e.message}")
        }
        count
    }

    /**
     * Limpia todos los mensajes privados y bandejas de entrada de todos los usuarios en Firestore.
     */
    suspend fun purgeAllUserInboxes(): Int = withContext(Dispatchers.IO) {
        var cleanedUsers = 0
        try {
            val db = FirebaseFirestore.getInstance()
            val usersSnap = db.collection("users").get().await()

            for (userDoc in usersSnap.documents) {
                try {
                    // 1. Limpiar array privateMessages en el documento del usuario
                    userDoc.reference.update(
                        mapOf(
                            "privateMessages" to emptyList<Any>(),
                            "hasUnreadMessages" to false,
                            "unreadMessagesCount" to 0
                        )
                    ).await()
                    cleanedUsers++
                } catch (_: Exception) {}

                // 2. Limpiar subcolección 'messages'
                try {
                    val msgsSnap = userDoc.reference.collection("messages").get().await()
                    for (mDoc in msgsSnap.documents) {
                        try { mDoc.reference.delete().await() } catch (_: Exception) {}
                    }
                } catch (_: Exception) {}
            }
            Log.d(TAG, "Se limpiaron las bandejas de $cleanedUsers usuarios")
        } catch (e: Exception) {
            Log.w(TAG, "Error purgando bandejas de usuarios: ${e.message}")
        }
        cleanedUsers
    }

    /**
     * Ejecuta una purga completa y profunda de todos los datos residuales (notificaciones globales,
     * mensajes privados antiguos y reportes de prueba).
     */
    suspend fun purgeAllResidualCloudData(context: Context): CloudCleanResult = withContext(Dispatchers.IO) {
        try {
            val notifsCount = purgeGlobalNotifications()
            val usersCleaned = purgeAllUserInboxes()
            
            // Purgar reportes y soporte
            var reportsCount = 0
            try {
                val db = FirebaseFirestore.getInstance()
                val rSnap = db.collection("support_reports").get().await()
                reportsCount = rSnap.size()
                FeedbackRepository.purgeAllSupportMessagesAcrossSystem()
            } catch (_: Exception) {}

            // Desactivar y limpiar comunicado global activo
            try {
                GlobalAnnouncementManager.deactivateAnnouncement(context) { _, _ -> }
            } catch (_: Exception) {}

            val total = notifsCount + usersCleaned + reportsCount
            CloudCleanResult(
                globalNotificationsDeleted = notifsCount,
                userInboxesCleaned = usersCleaned,
                supportReportsDeleted = reportsCount,
                totalDeleted = total,
                success = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error en purgeAllResidualCloudData: ${e.message}", e)
            CloudCleanResult(
                success = false,
                errorMessage = e.localizedMessage ?: e.message
            )
        }
    }
}
