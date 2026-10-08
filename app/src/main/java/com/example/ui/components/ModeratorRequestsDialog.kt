package com.example.ui.components

import com.example.ui.components.CoachButton as Button
import com.example.ui.components.CoachOutlinedButton as OutlinedButton
import com.example.ui.components.CoachTextButton as TextButton
import com.example.ui.components.CoachIconButton as IconButton
import com.example.util.tr
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminModeratorRequestsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var requests by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showPayments by remember { mutableStateOf(false) }
    var showStreamers by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }

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
                        requests = combinedMap.values.sortedByDescending { (it["timestamp"] as? Number)?.toLong() ?: 0L }
                        isLoading = false
                    }
                    .addOnFailureListener {
                        requests = combinedMap.values.sortedByDescending { (it["timestamp"] as? Number)?.toLong() ?: 0L }
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
                        requests = list
                        isLoading = false
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, com.example.util.appTr("Error al cargar solicitudes: ${e.message}"), Toast.LENGTH_LONG).show()
                        isLoading = false
                    }
            }
    }

    LaunchedEffect(Unit) {
        loadRequests()
    }

    val filtered = requests.filter { req ->
        val status = req["status"] as? String ?: "PENDIENTE"
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

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showStreamers = false; showPayments = false }) { Text(com.example.util.localizedString(com.example.R.string.streamer_roles), color = if (!showStreamers) HextechGold else TextSecondary) }
                    TextButton(onClick = { showStreamers = true; showPayments = false }) { Text(com.example.util.localizedString(com.example.R.string.streamer_reviews), color = if (showStreamers) HextechGold else TextSecondary) }
                }
                TextButton(onClick = { showPayments = true; showStreamers = false }) { Text(tr("Pagos USDT")) }
                PanelReadControl(com.example.data.NotificationPanel.ADMINISTRATION)
                if (showPayments) {
                    Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) { CashRedemptionReviewPanel() }
                } else
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
                                                else -> HextechGold.copy(alpha = 0.15f)
                                            },
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = com.example.util.tr(if (type == "VERIFICATION") "VERIFICACIÓN" else "ROL SECUNDARIO"),
                                                color = if (type == "VERIFICATION") HextechCyan else HextechGold,
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
                                                text = tr("Cambio propuesto: "),
                                                color = TextMuted,
                                                fontSize = 11.sp
                                            )
                                            if (type == "VERIFICATION") {
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
                                        text = com.example.util.tr("Solicitado por moderador: $requestedByName"),
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
                                                    try { db.collection("support_reports").document(id).update(updateMap) } catch (_: Exception) {}
                                                    try { db.collection("moderator_requests").document(id).update(updateMap) } catch (_: Exception) {}
                                                    Toast.makeText(context, com.example.util.appTr("Solicitud rechazada"), Toast.LENGTH_SHORT).show()
                                                    loadRequests()
                                                },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(vertical = 6.dp)
                                            ) {
                                                Text(tr("Rechazar"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            // Approve
                                            Button(
                                                onClick = {
                                                    val updateMap = mapOf<String, Any>("status" to "APROBADA")
                                                    // Apply change first
                                                    if (type == "VERIFICATION") {
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
