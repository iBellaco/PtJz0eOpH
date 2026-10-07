package com.example.ui.components

import com.example.ui.components.CoachButton as Button
import com.example.ui.components.CoachTextButton as TextButton
import com.example.util.tr
import androidx.compose.foundation.BorderStroke
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.ui.components.coachClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBroadcastAnnouncementDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isUrgent by remember { mutableStateOf(false) }
    var sendNotification by remember { mutableStateOf(false) }
    var isPublishing by remember { mutableStateOf(false) }
    var isDeactivating by remember { mutableStateOf(false) }

    LaunchedEffect(isUrgent) {
        if (isUrgent) {
            sendNotification = true
        }
    }

    val activeAnnouncement by com.example.data.GlobalAnnouncementManager.currentAnnouncement.collectAsState()
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Campaign, contentDescription = null, tint = HextechGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(tr("Publicar Anuncio Global"), fontWeight = FontWeight.Bold, color = HextechGold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    tr("Este anuncio se enviará en tiempo real a todos los dispositivos (con o sin sesión iniciada)."),
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                // Si hay un anuncio activo actualmente, mostrar ficha con opción de desactivarlo
                if (activeAnnouncement != null && activeAnnouncement!!.active) {
                    val activeAnn = activeAnnouncement!!
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = HextechSurface),
                        border = BorderStroke(1.dp, if (activeAnn.isUrgent) DangerRed else HextechGold.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (activeAnn.isUrgent) DangerRed else Color(0xFF22C55E))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = com.example.util.tr(if (activeAnn.isUrgent) "ACTIVO (URGENTE)" else "ACTIVO EN DISPOSITIVOS"),
                                        color = if (activeAnn.isUrgent) DangerRed else Color(0xFF22C55E),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = com.example.util.tr(activeAnn.getFormattedDate()),
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = com.example.util.tr(activeAnn.title),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = com.example.util.tr(activeAnn.message),
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    isDeactivating = true
                                    com.example.data.GlobalAnnouncementManager.deactivateAnnouncement(context) { success, err ->
                                        isDeactivating = false
                                        if (success) {
                                            Toast.makeText(context, com.example.util.appTr("Anuncio global desactivado"), Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, com.example.util.appTr("Error al desactivar: $err"), Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                enabled = !isDeactivating,
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.85f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    com.example.util.tr(if (isDeactivating) "Desactivando..." else "Desactivar Anuncio Actual"),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(tr("Título del Anuncio")) },
                    placeholder = { Text(tr("Ej. Nuevo parche 6.0 o Mantenimiento")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text(tr("Mensaje Detallado")) },
                    placeholder = { Text(tr("Escribe el comunicado para los usuarios...")) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.coachClickable { isUrgent = !isUrgent }
                ) {
                    Checkbox(
                        checked = isUrgent,
                        onCheckedChange = { isUrgent = it },
                        colors = CheckboxDefaults.colors(checkedColor = DangerRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(tr("Marcar como Urgente / Mantenimiento"), color = if (isUrgent) DangerRed else TextSecondary, fontSize = 12.sp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.coachClickable { sendNotification = !sendNotification }
                ) {
                    Checkbox(
                        checked = sendNotification,
                        onCheckedChange = { sendNotification = it },
                        colors = CheckboxDefaults.colors(checkedColor = HextechGold)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(tr("Enviar notificación a los dispositivos"), color = if (sendNotification) HextechGold else TextSecondary, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || message.isBlank()) {
                        Toast.makeText(context, com.example.util.appTr("Por favor completa título y mensaje"), Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isPublishing = true
                    com.example.data.GlobalAnnouncementManager.publishAnnouncement(
                        context = context,
                        title = title,
                        message = message,
                        isUrgent = isUrgent,
                        sendNotification = sendNotification
                    ) { success, err ->
                        isPublishing = false
                        if (success) {
                            Toast.makeText(context, com.example.util.appTr("¡Anuncio global publicado a todos los dispositivos!"), Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } else {
                            Toast.makeText(context, com.example.util.appTr("Error al publicar: $err"), Toast.LENGTH_LONG).show()
                        }
                    }
                },
                enabled = !isPublishing,
                colors = ButtonDefaults.buttonColors(containerColor = HextechGold)
            ) {
                Text(com.example.util.tr(if (isPublishing) "Publicando..." else "Publicar"), color = HextechDarkBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(tr("Cancelar"), color = TextMuted)
            }
        }
    )
}
