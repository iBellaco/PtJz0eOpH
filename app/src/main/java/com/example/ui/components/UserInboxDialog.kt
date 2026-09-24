package com.example.ui.components

import android.content.Context
import androidx.compose.animation.core.*

import com.example.ui.theme.*
import com.example.util.SubscriptionManager
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Image
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.data.SupportMessageEntry
import com.example.data.SupportReplyManager
import com.example.data.supabase.FeedbackRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun UserInboxDialog(
    userUid: String,
    onDismiss: () -> Unit
) {
    val activeTheme = AppThemeManager.currentTheme
    val context = LocalContext.current
    val inboxPrefs = remember(userUid) { context.getSharedPreferences("user_inbox_cache_$userUid", Context.MODE_PRIVATE) }

    var subcollectionMessages by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var arrayMessages by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var supportReportMessages by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var deletedIds by remember(userUid) {
        mutableStateOf(inboxPrefs.getStringSet("deleted_ids", emptySet())?.toSet() ?: emptySet())
    }
    var localReadIds by remember(userUid) {
        mutableStateOf(inboxPrefs.getStringSet("read_ids", emptySet())?.toSet() ?: emptySet())
    }
    var deletedRefreshTrigger by remember { mutableStateOf(0) }
    var activeSupportIds by remember { mutableStateOf<Set<String>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()
    val authUser = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }
    var resolvedUserName by remember { mutableStateOf(authUser?.displayName?.takeIf { it.isNotBlank() } ?: "") }
    val userEmail = authUser?.email ?: ""

    LaunchedEffect(userUid) {
        if (resolvedUserName.isBlank()) {
            try {
                val userSnap = FirebaseFirestore.getInstance().collection("users").document(userUid).get().await()
                val name = userSnap.getString("userName")
                    ?: userSnap.getString("name")
                    ?: userSnap.getString("username")
                    ?: userEmail.substringBefore("@").takeIf { it.isNotBlank() }
                    ?: "Invocador"
                resolvedUserName = name
            } catch (_: Exception) {
                resolvedUserName = userEmail.substringBefore("@").ifBlank { "Invocador" }
            }
        }
    }

    LaunchedEffect(userUid, userEmail) {
        if (userUid.isNotBlank() && userUid != "anonimo") {
            try {
                val act = FeedbackRepository.syncAndPurgeOrphansForUser(context, userUid, userEmail)
                activeSupportIds = act
            } catch (_: Exception) {}
        }

        val db = FirebaseFirestore.getInstance()
        val userDoc = db.collection("users").document(userUid)
        
        // 1. Escuchar subcolección messages
        userDoc.collection("messages")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val msgs = snapshot.documents.mapNotNull { doc ->
                        doc.data?.plus("id" to doc.id)
                    }
                    subcollectionMessages = msgs
                }
                isLoading = false
            }

        // 2. Escuchar campo privateMessages en el documento del usuario
        userDoc.addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null && snapshot.exists()) {
                @Suppress("UNCHECKED_CAST")
                val pMsgs = snapshot.get("privateMessages") as? List<Map<String, Any>>
                if (pMsgs != null) {
                    arrayMessages = pMsgs
                }
            }
            isLoading = false
        }

        // 3. Escuchar tickets de soporte directamente desde support_reports para este usuario (por userId o userEmail)
        try {
            val supportMap = mutableMapOf<String, Map<String, Any>>()
            fun updateSupportList() {
                supportReportMessages = supportMap.values.toList()
            }

            if (userUid.isNotBlank() && userUid != "anonimo") {
                db.collection("support_reports")
                    .whereEqualTo("userId", userUid)
                    .addSnapshotListener { snap, err ->
                        if (err == null && snap != null) {
                            for (change in snap.documentChanges) {
                                if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                                    deletedIds = deletedIds + change.document.id
                                    deletedRefreshTrigger++
                                    supportMap.remove(change.document.id)
                                }
                            }
                            for (doc in snap.documents) {
                                val data = doc.data ?: continue
                                val isDeleted = (data["isDeleted"] as? Boolean) == true || (data["deleted"] as? Boolean) == true || (data["status"] as? String)?.uppercase() in listOf("ELIMINADO", "DELETED", "CERRADO")
                                if (isDeleted) {
                                    deletedIds = deletedIds + doc.id
                                    deletedRefreshTrigger++
                                    supportMap.remove(doc.id)
                                    continue
                                }
                                val title = data["title"] as? String ?: "Reporte de Soporte"
                                val desc = data["description"] as? String ?: (data["content"] as? String ?: "")
                                val conv = data["conversation"] as? List<Map<String, Any>> ?: emptyList()
                                val admRep = data["adminReply"] as? String ?: ""
                                val repBy = data["repliedBy"] as? String ?: ""
                                val lastEntry = conv.lastOrNull()
                                val lastConvTs = (lastEntry?.get("timestampMillis") as? Number)?.toLong()
                                val ts = (data["lastMessageAt"] as? Timestamp)?.toDate()?.time
                                    ?: (data["repliedAt"] as? Timestamp)?.toDate()?.time
                                    ?: lastConvTs
                                    ?: (data["updatedAt"] as? Timestamp)?.toDate()?.time
                                    ?: (data["createdAt"] as? Timestamp)?.toDate()?.time
                                    ?: System.currentTimeMillis()
                                val status = data["status"] as? String ?: "PENDIENTE"
                                val lastRole = (lastEntry?.get("senderRole") as? String)?.uppercase()
                                    ?: (data["lastReplyRole"] as? String)?.uppercase()
                                    ?: (data["lastReplySenderRole"] as? String)?.uppercase()
                                    ?: ""
                                val isLastReplyFromSupport = lastRole == "SUPPORT" || lastRole == "ADMIN" || admRep.isNotBlank()
                                val hasNewAdminReply = (data["hasNewAdminReply"] as? Boolean) == true || (data["hasNewReply"] as? Boolean) == true

                                val rawDocTag = (data["tag"] as? String) ?: (data["type"] as? String) ?: "SUPPORT"
                                val isSponsorTag = rawDocTag.equals("PATROCINADOR", ignoreCase = true) || rawDocTag.equals("SPONSOR", ignoreCase = true)
                                val finalTag = if (isSponsorTag) "PATROCINADOR" else "SUPPORT"
                                val displayTitle = if (isSponsorTag) {
                                    if (title.startsWith("Patrocinio:", ignoreCase = true)) title else "Patrocinio: $title"
                                } else {
                                    if (title.startsWith("Soporte:", ignoreCase = true)) title else "Soporte: $title"
                                }

                                supportMap[doc.id] = mapOf(
                                    "id" to doc.id,
                                    "reportId" to doc.id,
                                    "title" to displayTitle,
                                    "content" to desc,
                                    "description" to desc,
                                    "tag" to finalTag,
                                    "timestamp" to ts,
                                    "status" to status,
                                    "conversation" to conv,
                                    "adminReply" to admRep,
                                    "repliedBy" to repBy,
                                    "isRead" to ((data["isRead"] as? Boolean) == true),
                                    "userRead" to ((data["userRead"] as? Boolean) == true),
                                    "hasNewAdminReply" to hasNewAdminReply,
                                    "isLastReplyFromSupport" to isLastReplyFromSupport,
                                    "lastReplyTs" to (lastConvTs ?: ts),
                                    "sender" to (data["userName"] as? String ?: (if (isSponsorTag) "Patrocinador" else "Soporte Coach"))
                                )
                            }
                            updateSupportList()
                        }
                    }
            }

            if (userEmail.isNotBlank()) {
                db.collection("support_reports")
                    .whereEqualTo("userEmail", userEmail)
                    .addSnapshotListener { snap, err ->
                        if (err == null && snap != null) {
                            for (change in snap.documentChanges) {
                                if (change.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED) {
                                    deletedIds = deletedIds + change.document.id
                                    deletedRefreshTrigger++
                                    supportMap.remove(change.document.id)
                                }
                            }
                            for (doc in snap.documents) {
                                val data = doc.data ?: continue
                                val isDeleted = (data["isDeleted"] as? Boolean) == true || (data["deleted"] as? Boolean) == true || (data["status"] as? String)?.uppercase() in listOf("ELIMINADO", "DELETED", "CERRADO")
                                if (isDeleted) {
                                    deletedIds = deletedIds + doc.id
                                    deletedRefreshTrigger++
                                    supportMap.remove(doc.id)
                                    continue
                                }
                                val title = data["title"] as? String ?: "Reporte de Soporte"
                                val desc = data["description"] as? String ?: (data["content"] as? String ?: "")
                                val conv = data["conversation"] as? List<Map<String, Any>> ?: emptyList()
                                val admRep = data["adminReply"] as? String ?: ""
                                val repBy = data["repliedBy"] as? String ?: ""
                                val lastEntry = conv.lastOrNull()
                                val lastConvTs = (lastEntry?.get("timestampMillis") as? Number)?.toLong()
                                val ts = (data["lastMessageAt"] as? Timestamp)?.toDate()?.time
                                    ?: (data["repliedAt"] as? Timestamp)?.toDate()?.time
                                    ?: lastConvTs
                                    ?: (data["updatedAt"] as? Timestamp)?.toDate()?.time
                                    ?: (data["createdAt"] as? Timestamp)?.toDate()?.time
                                    ?: System.currentTimeMillis()
                                val status = data["status"] as? String ?: "PENDIENTE"
                                val lastRole = (lastEntry?.get("senderRole") as? String)?.uppercase()
                                    ?: (data["lastReplyRole"] as? String)?.uppercase()
                                    ?: (data["lastReplySenderRole"] as? String)?.uppercase()
                                    ?: ""
                                val isLastReplyFromSupport = lastRole == "SUPPORT" || lastRole == "ADMIN" || admRep.isNotBlank()
                                val hasNewAdminReply = (data["hasNewAdminReply"] as? Boolean) == true || (data["hasNewReply"] as? Boolean) == true

                                val rawDocTagEmail = (data["tag"] as? String) ?: (data["type"] as? String) ?: "SUPPORT"
                                val isSponsorTagEmail = rawDocTagEmail.equals("PATROCINADOR", ignoreCase = true) || rawDocTagEmail.equals("SPONSOR", ignoreCase = true)
                                val finalTagEmail = if (isSponsorTagEmail) "PATROCINADOR" else "SUPPORT"
                                val displayTitleEmail = if (isSponsorTagEmail) {
                                    if (title.startsWith("Patrocinio:", ignoreCase = true)) title else "Patrocinio: $title"
                                } else {
                                    if (title.startsWith("Soporte:", ignoreCase = true)) title else "Soporte: $title"
                                }

                                supportMap[doc.id] = mapOf(
                                    "id" to doc.id,
                                    "reportId" to doc.id,
                                    "title" to displayTitleEmail,
                                    "content" to desc,
                                    "description" to desc,
                                    "tag" to finalTagEmail,
                                    "timestamp" to ts,
                                    "status" to status,
                                    "conversation" to conv,
                                    "adminReply" to admRep,
                                    "repliedBy" to repBy,
                                    "isRead" to ((data["isRead"] as? Boolean) == true),
                                    "userRead" to ((data["userRead"] as? Boolean) == true),
                                    "hasNewAdminReply" to hasNewAdminReply,
                                    "isLastReplyFromSupport" to isLastReplyFromSupport,
                                    "lastReplyTs" to (lastConvTs ?: ts),
                                    "sender" to (data["userName"] as? String ?: (if (isSponsorTagEmail) "Patrocinador" else "Soporte Coach"))
                                )
                            }
                            updateSupportList()
                        }
                    }
            }
        } catch (_: Exception) {}
    }

    // Unir mensajes de todas las fuentes eliminando duplicados por id y filtrando soporte eliminado/cerrado
    val messages = remember(subcollectionMessages, arrayMessages, supportReportMessages, deletedIds, deletedRefreshTrigger, activeSupportIds, localReadIds) {
        val currActive = activeSupportIds
        val validSupportIds = supportReportMessages.mapNotNull { it["id"] as? String }.toSet()
        val all = mutableMapOf<String, Map<String, Any>>()

        fun resolveIsRead(m: Map<String, Any>, id: String, reportId: String): Boolean {
            val lastReadTs = maxOf(
                inboxPrefs.getLong("last_read_ts_$reportId", 0L),
                inboxPrefs.getLong("last_read_ts_$id", 0L)
            )

            val isSupport = (m["tag"] as? String)?.equals("SUPPORT", ignoreCase = true) == true ||
                (m["reportId"] as? String)?.isNotBlank() == true ||
                (m["ticketId"] as? String)?.isNotBlank() == true ||
                m["conversation"] != null ||
                (m["adminReply"] as? String)?.isNotBlank() == true ||
                FeedbackRepository.isSupportMessage(m) ||
                (m["title"] as? String)?.contains("Soporte", ignoreCase = true) == true ||
                (m["title"] as? String)?.contains("Ticket", ignoreCase = true) == true

            val conv = (m["conversation"] as? List<*>)?.filterIsInstance<Map<String, Any>>() ?: emptyList()
            val lastEntry = conv.lastOrNull()
            val lastRole = (lastEntry?.get("senderRole") as? String)?.uppercase()
                ?: (m["lastReplyRole"] as? String)?.uppercase()
                ?: (m["lastReplySenderRole"] as? String)?.uppercase()
                ?: ""
            val hasSupportReply = (m["adminReply"] as? String)?.isNotBlank() == true
            val isLastReplyFromSupport = (m["isLastReplyFromSupport"] as? Boolean) == true ||
                lastRole == "SUPPORT" || lastRole == "ADMIN" || (conv.isEmpty() && hasSupportReply)
            val hasNewAdminReply = (m["hasNewAdminReply"] as? Boolean) == true || (m["hasNewReply"] as? Boolean) == true

            val replyTs = (lastEntry?.get("timestampMillis") as? Number)?.toLong()
                ?: (m["lastReplyTs"] as? Long)
                ?: (m["lastMessageAt"] as? Timestamp)?.toDate()?.time
                ?: (m["repliedAt"] as? Timestamp)?.toDate()?.time
                ?: (m["timestamp"] as? Long)
                ?: 0L

            val isLocallyMarkedRead = localReadIds.contains(id) || localReadIds.contains(reportId)

            // Si es un reporte propio y soporte aún no ha respondido (el usuario envió el reporte y está en espera):
            if (isSupport && !hasSupportReply && !hasNewAdminReply) {
                return true
            }

            // Si el equipo de soporte respondió y el usuario no lo ha leído después de esa respuesta:
            if (isSupport && (hasNewAdminReply || isLastReplyFromSupport)) {
                if (hasNewAdminReply) {
                    return false // Incondicionalmente NUEVO
                }
                if (replyTs > lastReadTs && replyTs > 0L) {
                    return false // ¡Nueva respuesta de soporte posterior a la lectura previa!
                }
                if (lastReadTs == 0L && ((m["isRead"] as? Boolean) == false || (m["userRead"] as? Boolean) == false)) {
                    return false
                }
                if (lastReadTs >= replyTs && lastReadTs > 0L) {
                    return true
                }
            }

            if (isLocallyMarkedRead) {
                return true
            }

            val rawRead = (m["isRead"] as? Boolean) == true || (m["userRead"] as? Boolean) == true
            return rawRead
        }

        for (m in supportReportMessages) {
            val id = m["id"] as? String ?: continue
            val reportId = m["reportId"] as? String ?: id
            val title = (m["title"] as? String ?: "").trim()
            val cleanTitle = title.removePrefix("Soporte: ").removePrefix("Reporte: ").trim()
            if (deletedIds.contains(id) || deletedIds.contains(reportId) ||
                (title.isNotBlank() && deletedIds.contains(title)) ||
                (cleanTitle.isNotBlank() && deletedIds.contains(cleanTitle))) continue

            val isDeleted = (m["isDeleted"] as? Boolean) == true || 
                            (m["deleted"] as? Boolean) == true || 
                            (m["status"] as? String)?.uppercase(Locale.US) in listOf("ELIMINADO", "DELETED", "CERRADO")
            if (isDeleted) continue

            if (currActive != null) {
                if (currActive.isEmpty()) continue
                val matches = currActive.contains(id) || currActive.contains(reportId) ||
                              currActive.contains(title) || currActive.contains(cleanTitle)
                if (!matches) continue
            }

            val isRead = resolveIsRead(m, id, reportId)
            all[id] = m.toMutableMap().apply { put("isRead", isRead) }
        }

        fun processMessage(m: Map<String, Any>) {
            val id = m["id"] as? String ?: return
            val reportId = m["reportId"] as? String ?: id
            val title = (m["title"] as? String ?: "").trim()
            val cleanTitle = title.removePrefix("Soporte: ").removePrefix("Reporte: ").trim()
            if (deletedIds.contains(id) || deletedIds.contains(reportId) ||
                (title.isNotBlank() && deletedIds.contains(title)) ||
                (cleanTitle.isNotBlank() && deletedIds.contains(cleanTitle))) return

            val isDeleted = (m["isDeleted"] as? Boolean) == true || 
                            (m["deleted"] as? Boolean) == true || 
                            (m["status"] as? String)?.uppercase(Locale.US) in listOf("ELIMINADO", "DELETED", "CERRADO")
            if (isDeleted) return

            val isSupport = FeedbackRepository.isSupportMessage(m)
            if (isSupport) {
                val rId = m["reportId"] as? String
                if (currActive != null) {
                    if (currActive.isEmpty()) return
                    val matchesActive = currActive.contains(id) || 
                                       currActive.contains(reportId) || 
                                       (rId != null && currActive.contains(rId)) ||
                                       (title.isNotBlank() && (currActive.contains(title) || currActive.contains(cleanTitle)))
                    if (!matchesActive) return
                } else {
                    val matchesValid = validSupportIds.contains(id) || validSupportIds.contains(reportId) || (rId != null && validSupportIds.contains(rId))
                    if (!matchesValid) return
                }
            }
            val isRead = resolveIsRead(m, id, reportId)
            all[id] = m.toMutableMap().apply { put("isRead", isRead) }
        }

        for (m in arrayMessages) {
            processMessage(m)
        }
        for (m in subcollectionMessages) {
            processMessage(m)
        }
        all.values.filter { m ->
            val id = m["id"] as? String ?: ""
            val reportId = m["reportId"] as? String ?: ""
            val title = (m["title"] as? String ?: "").trim()
            val cleanTitle = title.removePrefix("Soporte: ").removePrefix("Reporte: ").trim()
            !deletedIds.contains(id) && !deletedIds.contains(reportId) &&
            !(title.isNotBlank() && deletedIds.contains(title)) &&
            !(cleanTitle.isNotBlank() && deletedIds.contains(cleanTitle))
        }.sortedByDescending { (it["timestamp"] as? Long) ?: 0L }
    }

    // Si la bandeja está vacía o hay mensajes de soporte huérfanos, limpiar en Firestore
    LaunchedEffect(isLoading, messages.isEmpty(), supportReportMessages, subcollectionMessages, arrayMessages, activeSupportIds) {
        if (!isLoading && userUid.isNotBlank() && userUid != "anonimo") {
            val db = FirebaseFirestore.getInstance()
            val uRef = db.collection("users").document(userUid)

            if (messages.isEmpty()) {
                uRef.update(
                    "hasUnreadMessages", false,
                    "unreadMessagesCount", 0
                )
            }

            val currActive = activeSupportIds
            val validSupportIds = if (currActive != null) {
                currActive
            } else {
                supportReportMessages.mapNotNull { it["id"] as? String }.toSet()
            }

            // Purgar mensajes huérfanos de soporte en subcolección messages
            for (m in subcollectionMessages) {
                val id = m["id"] as? String ?: continue
                val reportId = m["reportId"] as? String ?: id
                val title = (m["title"] as? String ?: "").trim()
                val isSupport = FeedbackRepository.isSupportMessage(m)

                val shouldKeep = isSupport && validSupportIds.isNotEmpty() &&
                        (validSupportIds.contains(id) || validSupportIds.contains(reportId) || validSupportIds.contains(title))

                if (isSupport && !shouldKeep) {
                    try { uRef.collection("messages").document(id).delete() } catch (_: Exception) {}
                }
            }

            // Purgar mensajes huérfanos de soporte en array privateMessages
            val orphanInArray = arrayMessages.any { m ->
                val id = m["id"] as? String ?: ""
                val reportId = m["reportId"] as? String ?: id
                val title = (m["title"] as? String ?: "").trim()
                val isSupport = FeedbackRepository.isSupportMessage(m)
                val shouldKeep = isSupport && validSupportIds.isNotEmpty() &&
                        (validSupportIds.contains(id) || validSupportIds.contains(reportId) || validSupportIds.contains(title))
                isSupport && !shouldKeep
            }

            if (orphanInArray) {
                uRef.get().addOnSuccessListener { snap ->
                    if (snap.exists()) {
                        @Suppress("UNCHECKED_CAST")
                        val pMsgs = snap.get("privateMessages") as? List<Map<String, Any>>
                        if (pMsgs != null) {
                            val cleaned = pMsgs.filterNot { m ->
                                val id = m["id"] as? String ?: ""
                                val reportId = m["reportId"] as? String ?: id
                                val title = (m["title"] as? String ?: "").trim()
                                val isSupport = FeedbackRepository.isSupportMessage(m)
                                val shouldKeep = isSupport && validSupportIds.isNotEmpty() &&
                                        (validSupportIds.contains(id) || validSupportIds.contains(reportId) || validSupportIds.contains(title))
                                isSupport && !shouldKeep
                            }
                            uRef.update("privateMessages", cleaned)
                        }
                    }
                }
            }
        }
    }

    fun markMessageAsRead(id: String) {
        val targetMsg = messages.find { (it["id"] as? String) == id }
        val reportId = (targetMsg?.get("reportId") as? String ?: "").takeIf { it.isNotBlank() } ?: id
        val now = System.currentTimeMillis()
        val newRead = localReadIds + id + reportId
        localReadIds = newRead
        inboxPrefs.edit()
            .putStringSet("read_ids", newRead)
            .putLong("last_read_ts_$id", now)
            .putLong("last_read_ts_$reportId", now)
            .apply()

        // Actualizar listas en memoria de forma inmediata
        subcollectionMessages = subcollectionMessages.map { m ->
            val mId = m["id"] as? String ?: ""
            val rId = m["reportId"] as? String ?: ""
            if (mId == id || mId == reportId || rId == id || (reportId.isNotBlank() && rId == reportId)) {
                m.toMutableMap().apply { 
                    put("isRead", true)
                    put("userRead", true)
                    put("hasNewAdminReply", false)
                    put("hasNewReply", false)
                }
            } else m
        }
        arrayMessages = arrayMessages.map { m ->
            val mId = m["id"] as? String ?: ""
            val rId = m["reportId"] as? String ?: ""
            if (mId == id || mId == reportId || rId == id || (reportId.isNotBlank() && rId == reportId)) {
                m.toMutableMap().apply { 
                    put("isRead", true)
                    put("userRead", true)
                    put("hasNewAdminReply", false)
                    put("hasNewReply", false)
                }
            } else m
        }
        supportReportMessages = supportReportMessages.map { m ->
            val mId = m["id"] as? String ?: ""
            val rId = m["reportId"] as? String ?: ""
            if (mId == id || mId == reportId || rId == id || (reportId.isNotBlank() && rId == reportId)) {
                m.toMutableMap().apply { 
                    put("isRead", true)
                    put("userRead", true)
                    put("hasNewAdminReply", false)
                    put("hasNewReply", false)
                }
            } else m
        }

        val remainingUnread = messages.count { m ->
            val mId = m["id"] as? String ?: ""
            val rId = m["reportId"] as? String ?: ""
            val isThisOne = (mId == id || mId == reportId || rId == id || (reportId.isNotBlank() && rId == reportId))
            if (isThisOne) false else ((m["isRead"] as? Boolean) == false)
        }
        SubscriptionManager.setUnreadMessagesCount(remainingUnread)

        // Persistir en Firestore de forma segura con merge
        val db = FirebaseFirestore.getInstance()
        val uRef = db.collection("users").document(userUid)
        val readUpdate = mapOf(
            "isRead" to true,
            "userRead" to true,
            "hasNewAdminReply" to false,
            "hasNewReply" to false
        )
        uRef.collection("messages").document(id).set(readUpdate, com.google.firebase.firestore.SetOptions.merge())
        if (reportId.isNotBlank() && reportId != id) {
            uRef.collection("messages").document(reportId).set(readUpdate, com.google.firebase.firestore.SetOptions.merge())
        }
        try { db.collection("support_reports").document(id).set(readUpdate, com.google.firebase.firestore.SetOptions.merge()) } catch (_: Exception) {}
        if (reportId.isNotBlank() && reportId != id) {
            try { db.collection("support_reports").document(reportId).set(readUpdate, com.google.firebase.firestore.SetOptions.merge()) } catch (_: Exception) {}
        }

        uRef.get().addOnSuccessListener { snap ->
            @Suppress("UNCHECKED_CAST")
            val pMsgs = snap.get("privateMessages") as? List<Map<String, Any>>
            if (pMsgs != null) {
                val updated = pMsgs.map { m ->
                    val mId = m["id"] as? String ?: ""
                    val rId = m["reportId"] as? String ?: ""
                    if (mId == id || mId == reportId || rId == id || (reportId.isNotBlank() && rId == reportId)) {
                        m.toMutableMap().apply { put("isRead", true) }
                    } else m
                }
                val remaining = updated.count { 
                    val mId = it["id"] as? String ?: ""
                    val rId = it["reportId"] as? String ?: ""
                    (it["isRead"] as? Boolean) == false && !newRead.contains(mId) && !newRead.contains(rId)
                }
                uRef.set(
                    mapOf(
                        "privateMessages" to updated,
                        "hasUnreadMessages" to (remaining > 0),
                        "unreadMessagesCount" to remaining
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                )
            } else {
                uRef.set(
                    mapOf(
                        "hasUnreadMessages" to false,
                        "unreadMessagesCount" to 0
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                )
            }
        }
    }

    fun markAllAsRead() {
        val now = System.currentTimeMillis()
        val editor = inboxPrefs.edit()
        val allIds = messages.flatMap {
            val mId = it["id"] as? String ?: ""
            val rId = it["reportId"] as? String ?: ""
            if (mId.isNotBlank()) editor.putLong("last_read_ts_$mId", now)
            if (rId.isNotBlank()) editor.putLong("last_read_ts_$rId", now)
            listOfNotNull(mId.takeIf { it.isNotBlank() }, rId.takeIf { it.isNotBlank() })
        }.toSet()
        val newRead = localReadIds + allIds
        localReadIds = newRead
        editor.putStringSet("read_ids", newRead).apply()

        subcollectionMessages = subcollectionMessages.map { it.toMutableMap().apply { put("isRead", true); put("userRead", true); put("hasNewAdminReply", false) } }
        arrayMessages = arrayMessages.map { it.toMutableMap().apply { put("isRead", true); put("userRead", true); put("hasNewAdminReply", false) } }
        supportReportMessages = supportReportMessages.map { it.toMutableMap().apply { put("isRead", true); put("userRead", true); put("hasNewAdminReply", false) } }

        SubscriptionManager.setUnreadMessagesCount(0)

        val db = FirebaseFirestore.getInstance()
        val uRef = db.collection("users").document(userUid)
        val readUpdate = mapOf(
            "isRead" to true,
            "userRead" to true,
            "hasNewAdminReply" to false,
            "hasNewReply" to false
        )
        for (m in messages) {
            val mId = m["id"] as? String ?: continue
            val rId = m["reportId"] as? String ?: ""
            uRef.collection("messages").document(mId).set(readUpdate, com.google.firebase.firestore.SetOptions.merge())
            if (rId.isNotBlank() && rId != mId) {
                uRef.collection("messages").document(rId).set(readUpdate, com.google.firebase.firestore.SetOptions.merge())
            }
            try { db.collection("support_reports").document(mId).set(readUpdate, com.google.firebase.firestore.SetOptions.merge()) } catch (_: Exception) {}
            if (rId.isNotBlank() && rId != mId) {
                try { db.collection("support_reports").document(rId).set(readUpdate, com.google.firebase.firestore.SetOptions.merge()) } catch (_: Exception) {}
            }
        }
        uRef.get().addOnSuccessListener { snap ->
            @Suppress("UNCHECKED_CAST")
            val pMsgs = snap.get("privateMessages") as? List<Map<String, Any>>
            val updated = pMsgs?.map { m ->
                m.toMutableMap().apply { put("isRead", true) }
            } ?: emptyList()
            uRef.set(
                mapOf(
                    "privateMessages" to updated,
                    "hasUnreadMessages" to false,
                    "unreadMessagesCount" to 0
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )
        }
    }

    fun deleteMessage(id: String) {
        val targetMsg = messages.find { (it["id"] as? String) == id }
        val reportId = targetMsg?.get("reportId") as? String ?: ""
        val title = (targetMsg?.get("title") as? String ?: "").trim()
        val cleanTitle = title.removePrefix("Soporte: ").removePrefix("Reporte: ").trim()

        // Protección estricta: Los mensajes/tickets de soporte únicamente pueden ser eliminados por el administrador
        val isTargetSupport = targetMsg != null && (
            (targetMsg["tag"] as? String)?.equals("SUPPORT", ignoreCase = true) == true ||
            (targetMsg["reportId"] as? String)?.isNotBlank() == true ||
            (targetMsg["ticketId"] as? String)?.isNotBlank() == true ||
            FeedbackRepository.isSupportMessage(targetMsg) ||
            title.contains("Soporte", ignoreCase = true) ||
            title.contains("Ticket", ignoreCase = true) ||
            title.contains("Reporte", ignoreCase = true) ||
            (targetMsg["category"] as? String)?.isNotBlank() == true ||
            (targetMsg["isSupport"] as? Boolean) == true
        )
        if (isTargetSupport) {
            return
        }

        val newDeleted = deletedIds + id +
            (if (reportId.isNotBlank()) listOf(reportId) else emptyList()) +
            (if (title.isNotBlank()) listOf(title) else emptyList()) +
            (if (cleanTitle.isNotBlank()) listOf(cleanTitle) else emptyList())

        deletedIds = newDeleted
        inboxPrefs.edit().putStringSet("deleted_ids", newDeleted).apply()
        deletedRefreshTrigger++

        // Actualizar estado local de inmediato
        subcollectionMessages = subcollectionMessages.filterNot { m ->
            val mId = m["id"] as? String ?: ""
            val rId = m["reportId"] as? String ?: ""
            val mTitle = (m["title"] as? String ?: "").trim()
            mId == id || (reportId.isNotBlank() && (mId == reportId || rId == reportId)) || (title.isNotBlank() && mTitle == title)
        }
        arrayMessages = arrayMessages.filterNot { m ->
            val mId = m["id"] as? String ?: ""
            val rId = m["reportId"] as? String ?: ""
            val mTitle = (m["title"] as? String ?: "").trim()
            mId == id || (reportId.isNotBlank() && (mId == reportId || rId == reportId)) || (title.isNotBlank() && mTitle == title)
        }
        supportReportMessages = supportReportMessages.filterNot { m ->
            val mId = m["id"] as? String ?: ""
            val rId = m["reportId"] as? String ?: ""
            val mTitle = (m["title"] as? String ?: "").trim()
            mId == id || (reportId.isNotBlank() && (mId == reportId || rId == reportId)) || (title.isNotBlank() && mTitle == title)
        }

        val db = FirebaseFirestore.getInstance()
        val uRef = db.collection("users").document(userUid)
        uRef.collection("messages").document(id).delete()
        if (reportId.isNotBlank() && reportId != id) {
            uRef.collection("messages").document(reportId).delete()
        }
        try { db.collection("support_reports").document(id).delete() } catch (_: Exception) {}
        if (reportId.isNotBlank() && reportId != id) {
            try { db.collection("support_reports").document(reportId).delete() } catch (_: Exception) {}
        }

        // Eliminar también en la nube de soporte
        coroutineScope.launch {
            try {
                FeedbackRepository.deleteFeedback(id)
                if (reportId.isNotBlank() && reportId != id) {
                    FeedbackRepository.deleteFeedback(reportId)
                }
            } catch (_: Exception) {}
        }

        uRef.get().addOnSuccessListener { snap ->
            @Suppress("UNCHECKED_CAST")
            val pMsgs = snap.get("privateMessages") as? List<Map<String, Any>>
            if (pMsgs != null) {
                val updated = pMsgs.filterNot { m ->
                    val mId = m["id"] as? String ?: ""
                    val rId = m["reportId"] as? String ?: ""
                    val mTitle = (m["title"] as? String ?: "").trim()
                    mId == id || (reportId.isNotBlank() && (mId == reportId || rId == reportId)) || (title.isNotBlank() && mTitle == title)
                }
                val remainingUnread = updated.count { (it["isRead"] as? Boolean) == false && !localReadIds.contains(it["id"] as? String ?: "") }
                uRef.update(
                    "privateMessages", updated,
                    "hasUnreadMessages", remainingUnread > 0,
                    "unreadMessagesCount", remainingUnread
                )
            } else {
                uRef.update(
                    "hasUnreadMessages", false,
                    "unreadMessagesCount", 0
                )
            }
        }
    }

    var showSupportDialog by remember { mutableStateOf(false) }
    var selectedSupportMessage by remember { mutableStateOf<Map<String, Any>?>(null) }

    if (showSupportDialog) {
        SupportReportDialog(onDismiss = { showSupportDialog = false })
    }

    if (selectedSupportMessage != null) {
        val msg = selectedSupportMessage!!
        val id = msg["id"] as String
        val title = msg["title"] as? String ?: "Soporte"
        val content = msg["content"] as? String ?: ""
        val timestamp = msg["timestamp"] as? Long ?: 0L
        val adminReply = msg["adminReply"] as? String ?: ""
        val repliedBy = msg["repliedBy"] as? String ?: ""
        val reportId = (msg["reportId"] as? String)?.takeIf { it.isNotBlank() } ?: id
        val rawStatus = (msg["status"] as? String)?.uppercase() ?: "PENDIENTE"
        val normalizedStatus = when (rawStatus) {
            "SOLVED", "SOLUCIONADO", "RESUELTO" -> "SOLUCIONADO"
            "READ", "LEIDO", "LEÍDO" -> "LEÍDO"
            else -> "PENDIENTE"
        }
        @Suppress("UNCHECKED_CAST")
        val rawConversation = msg["conversation"] as? List<Map<String, Any>>
        val conversationEntries = remember(rawConversation, adminReply) {
            if (rawConversation != null && rawConversation.isNotEmpty()) {
                rawConversation.mapNotNull { m ->
                    val text = m["text"] as? String ?: return@mapNotNull null
                    SupportMessageEntry(
                        id = m["id"] as? String ?: UUID.randomUUID().toString(),
                        senderName = m["senderName"] as? String ?: "Soporte",
                        senderRole = m["senderRole"] as? String ?: "SUPPORT",
                        text = text,
                        timestampMillis = (m["timestampMillis"] as? Long) ?: (m["timestamp"] as? Long) ?: 0L,
                        isGreeting = (m["isGreeting"] as? Boolean) ?: false
                    )
                }
            } else if (adminReply.isNotBlank()) {
                listOf(
                    SupportMessageEntry(
                        senderName = repliedBy.ifBlank { "Soporte Coach" },
                        senderRole = "SUPPORT",
                        text = adminReply,
                        timestampMillis = timestamp,
                        isGreeting = SupportReplyManager.isDefaultGreeting(adminReply)
                    )
                )
            } else emptyList()
        }

        Dialog(
            onDismissRequest = { selectedSupportMessage = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .fillMaxHeight(0.9f),
                shape = RoundedCornerShape(16.dp),
                color = activeTheme.surface,
                border = BorderStroke(1.5.dp, activeTheme.primary)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.HeadsetMic, contentDescription = null, tint = activeTheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(title, fontWeight = FontWeight.Bold, color = activeTheme.primary, fontSize = 16.sp, maxLines = 1)
                        }
                        HextechAnimatedIconButton(
                            onClick = { selectedSupportMessage = null },
                            size = 36.dp,
                            backgroundColor = Color.Transparent,
                            borderColor = Color.Transparent,
                            glowColor = activeTheme.primary
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = activeTheme.textSecondary, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = activeTheme.cardBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            UserSupportThreadCard(
                                reportId = reportId,
                                originalContent = content,
                                initialConversation = conversationEntries,
                                adminReply = adminReply,
                                repliedBy = repliedBy,
                                timestamp = timestamp,
                                userName = resolvedUserName,
                                userUid = userUid,
                                userEmail = userEmail,
                                ticketStatus = normalizedStatus
                            )
                        }
                    }
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            color = activeTheme.surface,
            border = BorderStroke(1.5.dp, activeTheme.primary)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(activeTheme.primary.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                .border(1.dp, activeTheme.primary.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Message, contentDescription = null, tint = activeTheme.primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Bandeja de Entrada", fontWeight = FontWeight.Black, color = activeTheme.textPrimary, fontSize = 16.sp)
                            Text("Notificaciones y anuncios oficiales", color = activeTheme.textSecondary, fontSize = 11.sp)
                        }
                    }
                    HextechAnimatedIconButton(
                        onClick = onDismiss,
                        size = 36.dp,
                        backgroundColor = Color.Transparent,
                        borderColor = Color.Transparent,
                        glowColor = activeTheme.primary
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = activeTheme.textSecondary, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HextechAnimatedOutlinedButton(
                    onClick = { showSupportDialog = true },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    borderColor = activeTheme.primary,
                    glowColor = activeTheme.primary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.HeadsetMic, contentDescription = null, tint = activeTheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Centro de Soporte y Ayuda", color = activeTheme.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))

                val unreadCount = messages.count { (it["isRead"] as? Boolean) == false }
                if (messages.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (unreadCount > 0) "$unreadCount no leído(s)" else "Todos leídos",
                            color = if (unreadCount > 0) activeTheme.primaryLight else activeTheme.textSecondary,
                            fontSize = 12.sp
                        )
                        if (unreadCount > 0) {
                            HextechAnimatedTextLink(
                                text = "Marcar todos leídos",
                                onClick = { markAllAsRead() },
                                color = activeTheme.primaryLight,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                
                Divider(color = activeTheme.cardBorder)
                Spacer(modifier = Modifier.height(8.dp))
                
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = activeTheme.primary)
                    }
                } else if (messages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Message, contentDescription = null, tint = activeTheme.textMuted, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No tienes mensajes en tu bandeja.", color = activeTheme.textSecondary, fontSize = 14.sp)
                        }
                    }
                } else {
                    val infiniteTransition = rememberInfiniteTransition(label = "inboxPulse")
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(900, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseAlpha"
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages, key = { it["id"] as String }) { msg ->
                            val id = msg["id"] as String
                            val title = msg["title"] as? String ?: "Sin título"
                            val content = msg["content"] as? String ?: ""
                            val rawTag = msg["tag"] as? String
                            val messageTag = MessageTag.fromId(rawTag)
                            val timestamp = msg["timestamp"] as? Long ?: 0L
                            val isRead = msg["isRead"] as? Boolean ?: false
                            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            val dateStr = sdf.format(Date(timestamp))

                            val isSupportReply = messageTag == MessageTag.SUPPORT && !isRead

                            Card(
                                colors = CardDefaults.cardColors(containerColor = HextechSurfaceVariant),
                                shape = RoundedCornerShape(8.dp),
                                border = if (isSupportReply) {
                                    BorderStroke(1.5.dp, HextechCyan.copy(alpha = pulseAlpha))
                                } else if (!isRead) {
                                    BorderStroke(1.dp, HextechCyan)
                                } else {
                                    BorderStroke(1.dp, HextechCardBorder)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    val sender = msg["sender"] as? String ?: ""
                                    val repliedBy = msg["repliedBy"] as? String ?: ""
                                    val adminReply = msg["adminReply"] as? String ?: ""
                                    val reportId = (msg["reportId"] as? String)?.takeIf { it.isNotBlank() } ?: id

                                    @Suppress("UNCHECKED_CAST")
                                    val rawConversation = msg["conversation"] as? List<Map<String, Any>>
                                    val conversationEntries = remember(rawConversation, adminReply) {
                                        if (rawConversation != null && rawConversation.isNotEmpty()) {
                                            rawConversation.mapNotNull { m ->
                                                val text = m["text"] as? String ?: return@mapNotNull null
                                                SupportMessageEntry(
                                                    id = m["id"] as? String ?: UUID.randomUUID().toString(),
                                                    senderName = m["senderName"] as? String ?: "Soporte",
                                                    senderRole = m["senderRole"] as? String ?: "SUPPORT",
                                                    text = text,
                                                    timestampMillis = (m["timestampMillis"] as? Long) ?: (m["timestamp"] as? Long) ?: 0L,
                                                    isGreeting = (m["isGreeting"] as? Boolean) ?: false
                                                )
                                            }
                                        } else if (adminReply.isNotBlank()) {
                                            listOf(
                                                SupportMessageEntry(
                                                    senderName = repliedBy.ifBlank { "Soporte Coach" },
                                                    senderRole = "SUPPORT",
                                                    text = adminReply,
                                                    timestampMillis = timestamp,
                                                    isGreeting = SupportReplyManager.isDefaultGreeting(adminReply)
                                                )
                                            )
                                        } else emptyList()
                                    }

                                    val isSupportTicket = rawTag.equals("SUPPORT", ignoreCase = true) ||
                                        (msg["reportId"] as? String)?.isNotBlank() == true ||
                                        (msg["ticketId"] as? String)?.isNotBlank() == true ||
                                        messageTag == MessageTag.SUPPORT ||
                                        FeedbackRepository.isSupportMessage(msg) ||
                                        conversationEntries.isNotEmpty() ||
                                        adminReply.isNotBlank() ||
                                        title.contains("Soporte", ignoreCase = true) ||
                                        title.contains("Ticket", ignoreCase = true) ||
                                        title.contains("Reporte", ignoreCase = true) ||
                                        sender.contains("Soporte", ignoreCase = true) ||
                                        (msg["category"] as? String)?.isNotBlank() == true ||
                                        (msg["isSupport"] as? Boolean) == true

                                    // Badge de Estado para reportes de soporte (sincronizado multidispositivo)
                                    val rawStatus = (msg["status"] as? String)?.uppercase() ?: "PENDIENTE"
                                    val normalizedStatus = when (rawStatus) {
                                        "SOLVED", "SOLUCIONADO", "RESUELTO" -> "SOLUCIONADO"
                                        "READ", "LEIDO", "LEÍDO" -> "EN TRÁMITE"
                                        else -> "PENDIENTE"
                                    }
                                    val (statusColor, statusBg) = when (normalizedStatus) {
                                        "SOLUCIONADO" -> Color(0xFF10B981) to Color(0xFF10B981).copy(alpha = 0.2f)
                                        "EN TRÁMITE" -> Color(0xFF38BDF8) to Color(0xFF38BDF8).copy(alpha = 0.2f)
                                        else -> Color(0xFFF59E0B) to Color(0xFFF59E0B).copy(alpha = 0.2f)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (!isRead) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFF0EA5E9).copy(alpha = 0.2f),
                                                    modifier = Modifier.padding(end = 6.dp)
                                                ) {
                                                    Text(
                                                        "NUEVO",
                                                        color = Color(0xFF38BDF8),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            // Badge de Etiqueta del Mensaje (Mantenimiento, Importante, Oferta, etc.)
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = messageTag.badgeBg.copy(alpha = 0.2f),
                                                border = BorderStroke(0.5.dp, messageTag.badgeBg),
                                                modifier = Modifier.padding(end = 6.dp)
                                            ) {
                                                Text(
                                                    "${messageTag.emoji} ${messageTag.label.uppercase()}",
                                                    color = if (messageTag.textColor == Color.Black) messageTag.badgeBg else messageTag.textColor,
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }

                                            if (rawTag.equals("SUPPORT", ignoreCase = true) || (msg["reportId"] as? String)?.isNotBlank() == true) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = statusBg,
                                                    border = BorderStroke(0.5.dp, statusColor),
                                                    modifier = Modifier.padding(end = 6.dp)
                                                ) {
                                                    Text(
                                                        normalizedStatus,
                                                        color = statusColor,
                                                        fontSize = 8.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Text(title, color = if (!isRead) Color(0xFF0EA5E9) else Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (!isRead) {
                                                HextechAnimatedIconButton(
                                                    onClick = { markMessageAsRead(id) },
                                                    size = 30.dp,
                                                    backgroundColor = Color.Transparent,
                                                    borderColor = Color.Transparent,
                                                    glowColor = Color(0xFF0EA5E9)
                                                ) {
                                                    Icon(Icons.Default.MarkEmailRead, contentDescription = "Marcar como leído", tint = Color(0xFF0EA5E9), modifier = Modifier.size(18.dp))
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            // Los reportes de soporte solo pueden ser eliminados por el administrador
                                            if (!isSupportTicket) {
                                                HextechAnimatedIconButton(
                                                    onClick = { deleteMessage(id) },
                                                    size = 30.dp,
                                                    backgroundColor = Color.Transparent,
                                                    borderColor = Color.Transparent,
                                                    glowColor = DangerRed
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = DangerRed.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                    }
                                    Text(dateStr, color = Color.Gray, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                if (isSupportTicket) {
                                    var showSupportPopup by remember(id) { mutableStateOf(false) }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                markMessageAsRead(id)
                                                showSupportPopup = true
                                            }
                                    ) {
                                            if (sender.isNotBlank()) {
                                                Text("Remitente: $sender", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                                Spacer(modifier = Modifier.height(2.dp))
                                            }
                                            Text(
                                                text = if (content.length > 90) content.substring(0, 90) + "..." else content,
                                                color = Color.LightGray,
                                                fontSize = 13.sp,
                                                maxLines = 2
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Toca para abrir pop-up de soporte", color = HextechCyan.copy(alpha = pulseAlpha), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = HextechCyan.copy(alpha = pulseAlpha), modifier = Modifier.size(14.dp))
                                            }
                                        }

                                        if (showSupportPopup) {
                                            Dialog(
                                                onDismissRequest = { showSupportPopup = false },
                                                properties = DialogProperties(usePlatformDefaultWidth = false)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Color.Black.copy(alpha = 0.8f))
                                                        .clickable { showSupportPopup = false },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth(0.95f)
                                                            .fillMaxHeight(0.85f)
                                                            .background(HextechDarkBg, RoundedCornerShape(16.dp))
                                                            .border(1.dp, HextechCyan, RoundedCornerShape(16.dp))
                                                            .padding(16.dp)
                                                            .clickable(enabled = false) {}
                                                    ) {
                                                        Column(modifier = Modifier.fillMaxSize()) {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                                IconButton(onClick = { showSupportPopup = false }) {
                                                                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
                                                                }
                                                            }
                                                            Spacer(modifier = Modifier.height(8.dp))
                                                            Divider(color = Color(0xFF334155))
                                                            Spacer(modifier = Modifier.height(8.dp))
                                                            Box(modifier = Modifier.weight(1f)) {
                                                                UserSupportThreadCard(
                                                                    reportId = reportId,
                                                                    originalContent = content,
                                                                    initialConversation = conversationEntries,
                                                                    adminReply = adminReply,
                                                                    repliedBy = repliedBy,
                                                                    timestamp = timestamp,
                                                                    userName = resolvedUserName,
                                                                    userUid = userUid,
                                                                    userEmail = userEmail,
                                                                    ticketStatus = normalizedStatus
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        if (sender.isNotBlank()) {
                                            Text("Enviado por: $sender", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }
                                        Text(content, color = Color.LightGray, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImageViewerDialog(
    photoBase64: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .fillMaxHeight(0.8f)
                        .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    val bitmap = remember(photoBase64) {
                        try {
                            val cleanBase64 = if (photoBase64.contains(",")) photoBase64.substringAfter(",") else photoBase64
                            val decodedBytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
                            android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        } catch (_: Exception) {
                            null
                        }
                    }
                    if (bitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Imagen adjunta",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    } else {
                        coil.compose.AsyncImage(
                            model = photoBase64,
                            contentDescription = "Imagen adjunta",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9))
                ) {
                    Text("Cerrar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun UserSupportThreadCard(
    reportId: String,
    originalContent: String,
    initialConversation: List<SupportMessageEntry>,
    adminReply: String,
    repliedBy: String,
    timestamp: Long,
    userName: String,
    userUid: String,
    userEmail: String,
    ticketStatus: String = "PENDIENTE"
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var liveStatus by remember(ticketStatus) { mutableStateOf(ticketStatus) }
    var livePhotos by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedPhotoToView by remember { mutableStateOf<String?>(null) }

    if (selectedPhotoToView != null) {
        ImageViewerDialog(photoBase64 = selectedPhotoToView!!) {
            selectedPhotoToView = null
        }
    }

    var conversation by remember(initialConversation, adminReply) {
        mutableStateOf(
            if (initialConversation.isNotEmpty()) initialConversation
            else {
                val local = SupportReplyManager.getConversation(context, reportId)
                if (local.isNotEmpty()) local
                else if (adminReply.isNotBlank()) {
                    val parts = adminReply.split("\n\n---\n\n")
                    parts.mapIndexed { idx, part ->
                        SupportMessageEntry(
                            id = "${reportId}_adm_$idx",
                            senderName = repliedBy.ifBlank { "Soporte Coach" },
                            senderRole = "SUPPORT",
                            text = part.trim(),
                            timestampMillis = timestamp + (idx * 1000L),
                            isGreeting = SupportReplyManager.isDefaultGreeting(part.trim())
                        )
                    }
                } else emptyList()
            }
        )
    }

    // Escucha en tiempo real para sincronización multidispositivo de la conversación y estado del ticket
    DisposableEffect(reportId, userUid) {
        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("support_reports").document(reportId)
            .addSnapshotListener { snap, err ->
                if (err == null && snap != null && snap.exists()) {
                    val rawSt = snap.getString("status") ?: "PENDIENTE"
                    liveStatus = when (rawSt.uppercase()) {
                        "SOLVED", "SOLUCIONADO", "RESUELTO", "CERRADO", "CLOSED" -> "SOLUCIONADO"
                        "READ", "LEIDO", "LEÍDO" -> "LEÍDO"
                        else -> "PENDIENTE"
                    }
                    val remotePhotos = snap.get("photos") as? List<*>
                    if (remotePhotos != null) {
                        livePhotos = remotePhotos.mapNotNull { it?.toString() }
                    }
                    val desc = snap.getString("description") ?: snap.getString("content") ?: originalContent
                    val remoteConv = snap.get("conversation") as? List<Map<String, Any>>
                    if (!remoteConv.isNullOrEmpty()) {
                        val parsed = remoteConv.mapNotNull { m ->
                            val text = m["text"] as? String ?: return@mapNotNull null
                            SupportMessageEntry(
                                id = m["id"] as? String ?: UUID.randomUUID().toString(),
                                senderName = m["senderName"] as? String ?: "Soporte",
                                senderRole = m["senderRole"] as? String ?: "SUPPORT",
                                text = text,
                                timestampMillis = (m["timestampMillis"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                                isGreeting = (m["isGreeting"] as? Boolean) ?: false
                            )
                        }
                        val hasUserInitial = parsed.any { it.senderRole.equals("USER", ignoreCase = true) && it.text.trim() == desc.trim() }
                        val fullList = if (!hasUserInitial && desc.isNotBlank()) {
                            listOf(
                                SupportMessageEntry(
                                    id = "${reportId}_initial",
                                    senderName = userName.ifBlank { "Invocador" },
                                    senderRole = "USER",
                                    text = desc,
                                    timestampMillis = snap.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                                    isGreeting = false
                                )
                            ) + parsed
                        } else {
                            parsed
                        }
                        conversation = fullList
                        SupportReplyManager.saveConversation(context, reportId, fullList)
                    } else {
                        // Fallback si no hay array pero sí adminReply acumulado
                        val admRep = snap.getString("adminReply") ?: snap.getString("lastAdminReply") ?: ""
                        if (admRep.isNotBlank()) {
                            val repAt = snap.getTimestamp("repliedAt")?.toDate()?.time ?: System.currentTimeMillis()
                            val repBy = snap.getString("repliedBy") ?: "Soporte Coach"
                            val fallbackList = mutableListOf<SupportMessageEntry>()
                            if (desc.isNotBlank()) {
                                fallbackList.add(
                                    SupportMessageEntry(
                                        id = "${reportId}_initial",
                                        senderName = userName.ifBlank { "Invocador" },
                                        senderRole = "USER",
                                        text = desc,
                                        timestampMillis = snap.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                                        isGreeting = false
                                    )
                                )
                            }
                            val parts = admRep.split("\n\n---\n\n")
                            for ((pIdx, part) in parts.withIndex()) {
                                if (part.isNotBlank()) {
                                    fallbackList.add(
                                        SupportMessageEntry(
                                            id = "${reportId}_adm_$pIdx",
                                            senderName = repBy,
                                            senderRole = "SUPPORT",
                                            text = part.trim(),
                                            timestampMillis = repAt + (pIdx * 1000L),
                                            isGreeting = SupportReplyManager.isDefaultGreeting(part.trim())
                                        )
                                    )
                                }
                            }
                            conversation = fallbackList
                            SupportReplyManager.saveConversation(context, reportId, fallbackList)
                        }
                    }
                }
            }

        // Listener secundario sobre la bandeja del usuario para sincronización cruzada
        var userListener: com.google.firebase.firestore.ListenerRegistration? = null
        if (userUid.isNotBlank() && userUid != "anonimo") {
            userListener = db.collection("users").document(userUid).collection("messages").document(reportId)
                .addSnapshotListener { uSnap, uErr ->
                    if (uErr == null && uSnap != null && uSnap.exists()) {
                        val rawSt = uSnap.getString("status")
                        if (!rawSt.isNullOrBlank()) {
                            liveStatus = when (rawSt.uppercase()) {
                                "SOLVED", "SOLUCIONADO", "RESUELTO" -> "SOLUCIONADO"
                                "READ", "LEIDO", "LEÍDO" -> "LEÍDO"
                                else -> "PENDIENTE"
                            }
                        }
                    }
                }
        }

        onDispose {
            listener.remove()
            userListener?.remove()
        }
    }

    var userReplyText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    val canReply = remember(conversation, liveStatus) { SupportReplyManager.canUserReply(conversation, liveStatus) }
    val isOnlyGreeting = remember(conversation) { SupportReplyManager.isOnlyGreeting(conversation) }
    val isClosed = liveStatus.uppercase() == "SOLUCIONADO" || liveStatus.uppercase() == "CERRADO" || liveStatus.uppercase() == "CLOSED" || liveStatus.uppercase() == "RESUELTO"
    val timeFormatter = remember { SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        // Etiqueta de soporte y estado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Soporte Técnico / Reporte", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = when (liveStatus.uppercase()) {
                    "SOLUCIONADO", "CERRADO", "CLOSED" -> Color(0xFF10B981).copy(alpha = 0.2f)
                    else -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                },
                border = BorderStroke(0.5.dp, when (liveStatus.uppercase()) {
                    "SOLUCIONADO", "CERRADO", "CLOSED" -> Color(0xFF10B981)
                    else -> Color(0xFFF59E0B)
                })
            ) {
                Text(
                    text = liveStatus,
                    color = when (liveStatus.uppercase()) {
                        "SOLUCIONADO", "CERRADO", "CLOSED" -> Color(0xFF34D399)
                        else -> Color(0xFFFBBF24)
                    },
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        // Mensaje original (Descripción)
        if (originalContent.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.6f),
                border = BorderStroke(0.5.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Descripción:", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(originalContent, color = Color.LightGray, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Imagen adjunta (si existe) con botón para abrir en ventana
        if (livePhotos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text("Imagen adjunta:", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                livePhotos.forEach { photoStr ->
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFF0EA5E9), RoundedCornerShape(8.dp))
                            .clickable { selectedPhotoToView = photoStr },
                        contentAlignment = Alignment.Center
                    ) {
                        val thumbBitmap = remember(photoStr) {
                            try {
                                val clean = if (photoStr.contains(",")) photoStr.substringAfter(",") else photoStr
                                val bytes = android.util.Base64.decode(clean, android.util.Base64.DEFAULT)
                                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            } catch (_: Exception) { null }
                        }
                        if (thumbBitmap != null) {
                            androidx.compose.foundation.Image(
                                bitmap = thumbBitmap.asImageBitmap(),
                                contentDescription = "Imagen adjunta",
                                modifier = Modifier.fillMaxSize().padding(2.dp),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF0EA5E9), modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Historial de conversación
        if (conversation.isNotEmpty()) {
            Text(
                "Historial de Respuestas (${conversation.size})",
                color = Color(0xFF38BDF8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                conversation.forEach { msg ->
                    val isUserMsg = msg.senderRole.equals("USER", ignoreCase = true)
                    val bubbleBg = if (isUserMsg) Color(0xFF1E293B) else Color(0xFF0F2B48)
                    val bubbleBorder = if (isUserMsg) BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.5f))
                                       else BorderStroke(1.dp, Color(0xFF0EA5E9).copy(alpha = 0.6f))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isUserMsg) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = bubbleBg,
                            border = bubbleBorder,
                            modifier = Modifier.fillMaxWidth(0.92f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f, fill = false),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            if (isUserMsg) "👤 ${msg.senderName} (Tú)" else "🛡️ ${msg.senderName}",
                                            color = if (isUserMsg) Color(0xFFD4AF37) else Color(0xFF38BDF8),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (msg.isGreeting || (!isUserMsg && SupportReplyManager.isDefaultGreeting(msg.text))) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                                                border = BorderStroke(0.5.dp, Color(0xFF38BDF8))
                                            ) {
                                                Text(
                                                    "SALUDO",
                                                    color = Color(0xFF38BDF8),
                                                    fontSize = 7.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        timeFormatter.format(Date(msg.timestampMillis)),
                                        color = Color.Gray,
                                        fontSize = 9.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(msg.text, color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Validación de respuesta (Cerrado, solo saludo, o esperando respuesta)
        when {
            isClosed -> {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Este reporte ha sido marcado como cerrado. Ya no es posible enviar más respuestas.",
                            color = Color(0xFFFCA5A5),
                            fontSize = 10.5.sp
                        )
                    }
                }
            }
            !canReply || isOnlyGreeting -> {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Esperando respuesta del equipo de soporte. La opción de responder se habilitará cuando soporte responda formalmente a tu ticket.",
                            color = Color(0xFFFDE68A),
                            fontSize = 10.5.sp
                        )
                    }
                }
            }
            else -> {
                // Sección para que el usuario responda
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0B132B),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        "Responder a Soporte",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = userReplyText,
                        onValueChange = { userReplyText = SupportReplyManager.sanitizePlainText(it, 500) },
                        placeholder = { Text("Escribe tu respuesta aquí...", color = Color.Gray, fontSize = 11.5.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${userReplyText.length}/500",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                        Button(
                            onClick = {
                                if (userReplyText.trim().isBlank()) return@Button
                                isSending = true
                                coroutineScope.launch {
                                    val success = SupportReplyManager.sendUserReply(
                                        context = context,
                                        reportId = reportId,
                                        userReplyText = userReplyText.trim(),
                                        userName = userName,
                                        userId = userUid,
                                        userEmail = userEmail
                                    )
                                    if (success) {
                                        userReplyText = ""
                                        conversation = SupportReplyManager.getConversation(context, reportId)
                                        Toast.makeText(context, "Respuesta enviada a soporte", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Error al enviar respuesta", Toast.LENGTH_SHORT).show()
                                    }
                                    isSending = false
                                }
                            },
                            enabled = !isSending && userReplyText.trim().isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            if (isSending) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text("Enviar", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
}


