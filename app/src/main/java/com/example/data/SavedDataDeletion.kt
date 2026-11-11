package com.example.data

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

/** Only individual documents from explicit categories; never profiles, balances or devices. */
object SavedDataDeletion {
    val categories = mapOf(
        "Conversaciones de soporte" to "support_reports",
        "Reportes y sugerencias" to "feedbacks",
        "Solicitudes de patrocinio" to "pending_sponsor_ads",
        "Solicitudes de streamers" to "streamer_requests",
        "Contadores de streamers" to "streamer_click_metrics",
        "Solicitudes de roles" to "moderator_requests",
        "Configuración compartida" to "system_config",
        "Mensajes de la bandeja de entrada" to "users/messages",
        "Historial de publicaciones" to "streamer_requests/history"
    )
    data class Record(val path: String, val label: String)
    suspend fun records(category: String): List<Record> {
        check(SupportTicketAccess.isAdmin())
        val path = categories.getValue(category)
        val db = FirebaseFirestore.getInstance()
        val parts = path.split('/')
        val docs = if (parts.size == 1) db.collection(path).get(Source.SERVER).await().documents
        else db.collection(parts[0]).get(Source.SERVER).await().documents.flatMap {
            it.reference.collection(parts[1]).get(Source.SERVER).await().documents
        }
        return docs.filterNot { it.reference.parent.id == "system_config" &&
            it.id in setOf("streamer_live", "economy_service", "account_deletion_service") }
            .map { Record(it.reference.path, (it.get("title") as? String)?.takeIf(String::isNotBlank) ?: it.id) }
    }
    suspend fun delete(context: Context, category: String, record: Record) {
        check(SupportTicketAccess.isAdmin())
        val allowed = categories.getValue(category).split('/')
        val parts = record.path.split('/')
        require(if (allowed.size == 1) parts.size == 2 && parts[0] == allowed[0]
            else parts.size == 4 && parts[0] == allowed[0] && parts[2] == allowed[1])
        when (parts[0]) {
            "support_reports" -> FeedbackRepository.deleteFeedback(parts[1]).getOrThrow()
            "pending_sponsor_ads" -> AppNoticeManager.deleteSponsorNotice(context, parts[1])
            "streamer_requests" -> {
                val db = FirebaseFirestore.getInstance()
                if (parts.size == 2) {
                    val registry = db.document("system_config/streamer_live")
                    db.runTransaction { tx ->
                        val entries = (tx.get(registry).get("entries") as? List<*>)
                            .orEmpty().filterNot { (it as? Map<*, *>)?.get("userId") == parts[1] }
                        tx.set(registry, mapOf("entries" to entries), com.google.firebase.firestore.SetOptions.merge())
                        tx.delete(db.document(record.path))
                    }.await()
                } else db.document(record.path).delete().await()
            }
            else -> FirebaseFirestore.getInstance().document(record.path).delete().await()
        }
    }
}
