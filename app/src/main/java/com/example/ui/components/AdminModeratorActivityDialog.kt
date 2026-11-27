package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.util.tr
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.DateFormat

private data class ModeratorEvent(
    val id: String,
    val action: String,
    val targetUid: String,
    val value: String,
    val previousValue: String,
    val reportId: String,
    val detail: String,
    val targetName: String,
    val createdAt: Timestamp?
)

private data class ModeratorLog(val uid: String, val name: String, val events: List<ModeratorEvent>)

/** Read-only audit: every entry is paired with the account or conversation mutation. */
@Composable
fun AdminModeratorActivityDialog(onDismiss: () -> Unit) {
    var logs by remember { mutableStateOf<List<ModeratorLog>>(emptyList()) }
    var busy by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }
    var selectedUid by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableIntStateOf(0) }

    LaunchedEffect(refresh) {
        busy = true
        error = false
        runCatching {
            val db = FirebaseFirestore.getInstance()
            val users = db.collection("users").get().await().documents
            val names = users.associate { it.id to (it.getString("name") ?: it.getString("username") ?: it.id) }
            val reportLabels = db.collection("support_reports").get().await().documents.associate { report ->
                report.id to (report.getString("title") ?: report.getString("tag") ?: "Solicitud de soporte")
            }
            val activeModerators = users.filter { it.getString("role") == "moderador" || it.getString("secondaryRole") == "moderador" }.map { it.id }
            val events = db.collectionGroup("events").get().await().documents
                .filter { it.reference.parent.parent?.parent?.id == "moderator_activity" }
                .groupBy { it.getString("actorUid").orEmpty() }
            (activeModerators + events.keys).distinct().map { uid ->
                ModeratorLog(uid, names[uid] ?: uid, events[uid].orEmpty().map {
                    val targetUid = it.getString("targetUid").orEmpty()
                    val reportId = it.getString("reportId").orEmpty()
                    ModeratorEvent(
                        id = it.id,
                        action = it.getString("action").orEmpty(),
                        targetUid = targetUid,
                        value = it.getString("value").orEmpty(),
                        previousValue = it.getString("previousValue").orEmpty(),
                        reportId = reportId,
                        detail = it.getString("detail").orEmpty().ifBlank { reportLabels[reportId].orEmpty() },
                        targetName = names[targetUid] ?: targetUid,
                        createdAt = it.getTimestamp("createdAt")
                    )
                }.sortedByDescending { it.createdAt?.toDate()?.time ?: 0L })
            }
        }.onSuccess { logs = it }.onFailure { error = true }
        busy = false
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(color = HextechDarkBg, shape = MaterialTheme.shapes.large, border = BorderStroke(1.dp, HextechGold)) {
            Column(Modifier.fillMaxWidth().heightIn(max = 650.dp).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(tr("Actividad de moderación"), color = HextechGold, style = MaterialTheme.typography.titleLarge)
                Text(tr("Consulta quién hizo cada cambio, a qué usuario afectó, cuándo ocurrió y qué resultado produjo."), color = TextSecondary)
                if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (error) Text(tr("No se pudo cargar la actividad."), color = DangerRed)
                LazyColumn(Modifier.weight(1f).testTag("moderator_activity_scroll"), userScrollEnabled = true, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(logs, key = { it.uid }) { log ->
                        val expanded = selectedUid == log.uid
                        Surface(onClick = { selectedUid = if (expanded) null else log.uid }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("moderator_log_${log.uid}"), color = HextechSurface, shape = MaterialTheme.shapes.medium) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(log.name, color = HextechGold, style = MaterialTheme.typography.titleMedium)
                                val replyCount = log.events.count { it.action == "SUPPORT_REPLY" }
                                val roleCount = log.events.count { it.action == "SECONDARY_ROLE" && it.value.isNotBlank() }
                                val verificationCount = log.events.count { it.action == "VERIFICATION" && it.value == "true" }
                                Text("${tr("Respuestas")}: $replyCount  ·  ${tr("Cambios de rol")}: $roleCount  ·  ${tr("Revisiones de perfil")}: $verificationCount", color = HextechCyan)
                                if (expanded) {
                                    log.events.forEach { event ->
                                        val label = when (event.action) {
                                            "SUPPORT_REPLY" -> tr("Respuesta de soporte")
                                            "SUPPORT_STATUS" -> tr("Estado de solicitud")
                                            "SECONDARY_ROLE" -> tr("Rol secundario")
                                            "VERIFICATION" -> tr("Verificación de perfil")
                                            else -> event.action
                                        }
                                        val date = event.createdAt?.toDate()?.let { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(it) }.orEmpty()
                                        Surface(
                                            modifier = Modifier.fillMaxWidth().testTag("moderator_event_${event.id}"),
                                            color = HextechDarkBg.copy(alpha = 0.72f),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(0.8.dp, HextechCardBorder.copy(alpha = 0.9f))
                                        ) {
                                            Column(Modifier.padding(9.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                                    Icon(Icons.Default.History, contentDescription = null, tint = HextechGold, modifier = Modifier.size(14.dp))
                                                    Text(label, color = HextechGold, style = MaterialTheme.typography.labelLarge)
                                                }
                                                Text("${tr("Fecha y hora")}: $date", color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                                                Text("${tr("Usuario afectado")}: ${event.targetName}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                                                if (event.targetUid.isNotBlank()) Text("${tr("Identificador")}: ${event.targetUid}", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                                                if (event.previousValue.isNotBlank()) Text("${tr("Antes")}: ${event.previousValue}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                                                if (event.value.isNotBlank()) Text("${tr("Después")}: ${event.value}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                                                if (event.detail.isNotBlank()) Text("${tr("Detalle")}: ${event.detail}", color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                                                if (event.reportId.isNotBlank()) Text("${tr("Solicitud relacionada")}: ${event.reportId}", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { refresh++ }, modifier = Modifier.heightIn(min = 48.dp).testTag("refresh_moderator_logs")) { Text(tr("Actualizar")) }
                    TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp).testTag("close_moderator_logs")) { Text(tr("Cerrar")) }
                }
            }
        }
    }
}
