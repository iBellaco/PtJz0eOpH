package com.example.ui.components

import com.example.ui.components.CoachButton as Button
import com.example.ui.components.CoachOutlinedButton as OutlinedButton
import com.example.ui.components.CoachTextButton as TextButton
import com.example.ui.components.CoachIconButton as IconButton
import com.example.util.tr
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.BorderStroke
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import com.example.data.SponsorReviewPolicy
import com.example.data.SponsorReviewRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminModeratorRequestsDialog(
    onDismiss: () -> Unit
) {
    val adminClaim by com.example.util.AuthManager.isAdminClaim.collectAsState()
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var requests by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showStreamers by remember { mutableStateOf(false) }
    var showSponsors by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    val reviewScope = rememberCoroutineScope()
    var sponsorBusy by remember { mutableStateOf<Set<String>>(emptySet()) }

    fun resolveSponsor(id: String, approve: Boolean) {
        if (id in sponsorBusy) return
        sponsorBusy = sponsorBusy + id
        reviewScope.launch {
            try {
                SponsorReviewRepository.resolve(id, approve, requests.firstOrNull { it["id"] == id }?.get("queuedRequestId") as? String)
                // The committed decision remains successful even if refreshing the feed fails.
                try { com.example.data.AppNoticeManager.syncFromCloud(context) }
                catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
                catch (_: Exception) { }
                Toast.makeText(context, if (approve) com.example.util.appTr("Anuncio aprobado y visible en el panel de anuncios")
                    else context.getString(com.example.R.string.sponsor_rejection_done), Toast.LENGTH_SHORT).show()
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (pending: com.example.data.EconomyPendingException) {
                Toast.makeText(context, pending.message, Toast.LENGTH_LONG).show()
            } catch (review: com.example.data.EconomyReviewException) {
                Toast.makeText(context, review.message, Toast.LENGTH_LONG).show()
            } catch (_: Exception) {
                Toast.makeText(context, context.getString(com.example.R.string.sponsor_review_error), Toast.LENGTH_LONG).show()
            } finally { sponsorBusy = sponsorBusy - id }
        }
    }

    fun loadRequests() {
        isLoading = true
        val combinedMap = mutableMapOf<String, MutableMap<String, Any>>()

        // 1. Cargar desde support_reports (canal garantizado para solicitudes de moderador)
        db.collection("support_reports")
            .whereEqualTo("category", "MODERATOR_REQUEST")
            .get()
            .addOnSuccessListener { snap1 ->
                snap1.documents.forEach { doc ->
                    val data = doc.data?.toMutableMap() ?: mutableMapOf()
                    data["id"] = doc.id
                    combinedMap[doc.id] = data
                }

                // 2. Cargar también desde moderator_requests
                db.collection("moderator_requests")
                    .get()
                    .addOnSuccessListener { snap2 ->
                        snap2.documents.forEach { doc ->
                            val data = doc.data?.toMutableMap() ?: mutableMapOf()
                            data["id"] = doc.id
                            combinedMap[doc.id] = data
                        }
                        requests = requests.filter { it["requestType"] == "SPONSOR" } + combinedMap.values
                            .sortedByDescending { (it["timestamp"] as? Number)?.toLong() ?: 0L }
                        isLoading = false
                    }
                    .addOnFailureListener {
                        requests = requests.filter { it["requestType"] == "SPONSOR" } + combinedMap.values
                            .sortedByDescending { (it["timestamp"] as? Number)?.toLong() ?: 0L }
                        isLoading = false
                    }
            }
            .addOnFailureListener {
                db.collection("moderator_requests")
                    .get()
                    .addOnSuccessListener { snap2 ->
                        val list = snap2.documents.map { doc ->
                            val data = doc.data?.toMutableMap() ?: mutableMapOf()
                            data["id"] = doc.id
                            data
                        }.sortedByDescending { (it["timestamp"] as? Number)?.toLong() ?: 0L }
                        requests = requests.filter { it["requestType"] == "SPONSOR" } + list
                        isLoading = false
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, com.example.util.appTr("Error al cargar solicitudes: ${e.message}"), Toast.LENGTH_LONG).show()
                        isLoading = false
                    }
            }
    }

    DisposableEffect(adminClaim) {
        val listeners = if (!adminClaim) emptyList() else SponsorReviewRepository.observe(onChange = { rows ->
            val sponsors = rows.map { row -> row + mapOf(
                "requestType" to "SPONSOR", "status" to "PENDIENTE",
                "targetName" to (row["title"] ?: ""), "targetEmail" to (row["sponsorEmail"] ?: ""),
                "newValue" to (row["content"] ?: ""), "timestamp" to (row["createdAt"] ?: 0L)) }
            requests = requests.filterNot { it["requestType"] == "SPONSOR" } + sponsors
        }, onError = {
            Toast.makeText(context, context.getString(com.example.R.string.sponsor_review_error), Toast.LENGTH_LONG).show()
        })
        onDispose { listeners.forEach { it.remove() } }
    }

    LaunchedEffect(Unit) {
        loadRequests()
    }

    val filtered = requests.filter { req ->
        val status = req["status"] as? String ?: "PENDIENTE"
        val isSponsor = req["requestType"] == "SPONSOR"
        if (showSponsors != isSponsor) return@filter false
        if (showHistory) {
            status != "PENDIENTE"
        } else {
            status == "PENDIENTE"
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
            color = HextechSurfaceBg,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, HextechGold)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PendingActions,
                            contentDescription = null,
                            tint = HextechGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = com.example.util.localizedString(com.example.R.string.streamer_review_title),
                            color = HextechGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Cerrar"), tint = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showStreamers = false; showSponsors = false }) { Text(com.example.util.localizedString(com.example.R.string.streamer_roles), color = if (!showStreamers && !showSponsors) HextechGold else TextSecondary) }
                    TextButton(onClick = { showStreamers = true; showSponsors = false }) { Text(com.example.util.localizedString(com.example.R.string.streamer_reviews), color = if (showStreamers) HextechGold else TextSecondary) }
                    if (adminClaim) TextButton(onClick = { showStreamers = false; showSponsors = true }, modifier = Modifier.testTag("admin_sponsor_requests_open")) {
                        Text(tr("Patrocinios"), color = if (showSponsors) Color(0xFFF97316) else TextSecondary)
                    }
                }
                PanelReadControl(com.example.data.NotificationPanel.ADMINISTRATION)
                if (showStreamers) {
                    StreamerReviewPanel(Modifier.weight(1f).fillMaxWidth())
                } else {
                // Toggle history
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = com.example.util.tr(if (showHistory) "Mostrando: Historial de Solicitudes" else "Mostrando: Pendientes de Aprobación"),
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    TextButton(onClick = { showHistory = !showHistory }) {
                        Text(
                            text = com.example.util.tr(if (showHistory) "Ver Pendientes" else "Ver Historial"),
                            color = HextechCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (isLoading) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = HextechGold)
                    }
                } else if (filtered.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = com.example.util.tr(if (showHistory) "No hay historial de solicitudes registrado" else "¡Todo al día! No tienes solicitudes pendientes"),
                                color = TextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filtered) { req ->
                            val id = req["id"] as? String ?: ""
                            val type = req["requestType"] as? String ?: ""
                            val targetUid = req["targetUid"] as? String ?: ""
                            val targetName = req["targetName"] as? String ?: "Usuario"
                            val targetEmail = req["targetEmail"] as? String ?: ""
                            val newValue = req["newValue"] as? String ?: ""
                            val requestedByName = req["requestedByName"] as? String ?: "Moderador"
                            val status = req["status"] as? String ?: "PENDIENTE"
                            val timestamp = (req["timestamp"] as? Number)?.toLong() ?: 0L

                            val formattedTime = try {
                                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                                sdf.format(java.util.Date(timestamp))
                            } catch (e: Exception) {
                                "Reciente"
                            }

                            Surface(
                                color = HextechDarkBg,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    when (status) {
                                        "PENDIENTE" -> HextechGold.copy(alpha = 0.5f)
                                        "APROBADA" -> Color(0xFF00FF7F).copy(alpha = 0.5f)
                                        else -> DangerRed.copy(alpha = 0.5f)
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    // Row 1: Tipo + Fecha
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = when (type) {
                                                "VERIFICATION" -> HextechCyan.copy(alpha = 0.15f)
                                                "SPONSOR" -> Color(0xFFF97316).copy(alpha = 0.15f)
                                                else -> HextechGold.copy(alpha = 0.15f)
                                            },
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = com.example.util.tr(when (type) {
                                                    "VERIFICATION" -> "VERIFICACIÓN"
                                                    "SPONSOR" -> "PATROCINIO"
                                                    else -> "ROL SECUNDARIO"
                                                }),
                                                color = when (type) {
                                                    "VERIFICATION" -> HextechCyan
                                                    "SPONSOR" -> Color(0xFFF97316)
                                                    else -> HextechGold
                                                },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = com.example.util.tr(formattedTime),
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Row 2: Target Info
                                    Text(
                                        text = com.example.util.tr("Para: $targetName"),
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = com.example.util.tr(targetEmail),
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Row 3: Proposed Value
                                    Surface(
                                        color = HextechSurfaceBg,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = tr(if (type == "SPONSOR") "Anuncio: " else "Cambio propuesto: "),
                                                color = TextMuted,
                                                fontSize = 11.sp
                                            )
                                            if (type == "SPONSOR") {
                                                Column {
                                                    Text(
                                                        text = newValue.ifBlank { context.getString(com.example.R.string.sponsor_pending_review) },
                                                        color = TextPrimary,
                                                        fontSize = 11.sp,
                                                        maxLines = 3
                                                    )
                                                    (req["chargedEssence"] as? Number)?.let {
                                                        Text(tr("Taxa: ${it.toLong()} EA · devolvida em caso de recusa"), color = HextechGold, fontSize = 10.sp)
                                                    }
                                                }
                                            } else if (type == "VERIFICATION") {
                                                val verifyVal = newValue.toBoolean()
                                                Surface(
                                                    color = if (verifyVal) HextechCyan.copy(alpha = 0.12f) else DangerRed.copy(alpha = 0.12f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = com.example.util.tr(if (verifyVal) "VERIFICAR" else "QUITAR VERIFICACIÓN"),
                                                        color = if (verifyVal) HextechCyan else DangerRed,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            } else {
                                                if (newValue.isBlank()) {
                                                    Text(
                                                        text = tr("QUITAR ROL SECUNDARIO"),
                                                        color = DangerRed,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                } else {
                                                    RoleBadge(
                                                        role = newValue,
                                                        isPremiumActive = false,
                                                        isBanned = false,
                                                        size = RoleBadgeSize.COMPACT
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Requested by
                                    Text(
                                        text = com.example.util.tr(if (type == "SPONSOR") "Solicitado por patrocinador: $targetEmail" else "Solicitado por moderador: $requestedByName"),
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )

                                    if (status == "PENDIENTE") {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Reject
                                            OutlinedButton(
                                                onClick = {
                                                    val updateMap = mapOf<String, Any>("status" to "RECHAZADA")
                                                    if (type == "SPONSOR") {
                                                        resolveSponsor(id, false)
                                                    } else {
                                                        try { db.collection("support_reports").document(id).update(updateMap) } catch (_: Exception) {}
                                                        try { db.collection("moderator_requests").document(id).update(updateMap) } catch (_: Exception) {}
                                                        Toast.makeText(context, com.example.util.appTr("Solicitud rechazada"), Toast.LENGTH_SHORT).show()
                                                        loadRequests()
                                                    }
                                                },
                                                enabled = id !in sponsorBusy,
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(vertical = 6.dp)
                                            ) {
                                                Text(tr(if (type == "SPONSOR") "Recusar e devolver EA" else "Rechazar"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            // Approve
                                            Button(
                                                onClick = {
                                                    val updateMap = mapOf<String, Any>("status" to "APROBADA")
                                                    // Apply change first
                                                    if (type == "SPONSOR") {
                                                        resolveSponsor(id, true)
                                                    } else if (type == "VERIFICATION") {
                                                        val verifyVal = newValue.toBoolean()
                                                        updateUserVerification(context, targetUid, verifyVal) {
                                                            try { db.collection("support_reports").document(id).update(updateMap) } catch (_: Exception) {}
                                                            try { db.collection("moderator_requests").document(id).update(updateMap) } catch (_: Exception) {}
                                                            loadRequests()
                                                        }
                                                    } else {
                                                        updateUserSecondaryRoleInCloud(context, targetUid, newValue) {
                                                            try { db.collection("support_reports").document(id).update(updateMap) } catch (_: Exception) {}
                                                            try { db.collection("moderator_requests").document(id).update(updateMap) } catch (_: Exception) {}
                                                            loadRequests()
                                                        }
                                                    }
                                                },
                                                enabled = id !in sponsorBusy,
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = HextechGold, contentColor = HextechDarkBg),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(vertical = 6.dp)
                                            ) {
                                                Text(tr("Aprobar"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = com.example.util.tr("ESTADO: $status"),
                                            color = if (status == "APROBADA") Color(0xFF00FF7F) else DangerRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
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
}
