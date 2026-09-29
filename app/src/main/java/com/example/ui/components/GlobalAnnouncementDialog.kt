package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.GlobalAnnouncement
import com.example.data.GlobalAnnouncementManager
import com.example.ui.theme.*

/**
 * Diálogo flotante modal para presentar comunicados y anuncios globales oficiales.
 * Se muestra tanto en dispositivos con sesión activa como en dispositivos invitados/sin login.
 */
@Composable
fun GlobalAnnouncementDialog(
    announcement: GlobalAnnouncement,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val accentColor = if (announcement.isUrgent) DangerRed else HextechGold
    val badgeBg = if (announcement.isUrgent) DangerRed.copy(alpha = 0.2f) else HextechGold.copy(alpha = 0.15f)
    val headerBorder = if (announcement.isUrgent) DangerRed.copy(alpha = 0.6f) else HextechGold.copy(alpha = 0.5f)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = !announcement.isUrgent,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, headerBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Barra superior de encabezado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge distintivo
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(badgeBg)
                            .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (announcement.isUrgent) Icons.Default.Warning else Icons.Default.Campaign,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = com.example.util.tr(if (announcement.isUrgent) "AVISO URGENTE / MANTENIMIENTO" else "COMUNICADO OFICIAL"),
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Botón de cierre superior
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = com.example.util.trNullable("Cerrar"),
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Icono central representativo
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(accentColor.copy(alpha = 0.35f), Color.Transparent)
                            )
                        )
                        .border(1.dp, accentColor.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (announcement.isUrgent) Icons.Default.Warning else Icons.Default.Campaign,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Título del anuncio
                Text(
                    text = com.example.util.tr(announcement.title),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Fecha / hora del comunicado
                if (announcement.timestamp > 0L) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = com.example.util.tr("Emitido: ${announcement.getFormattedDate()}"),
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Contenido del comunicado con scroll si es extenso
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 280.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(HextechSurface.copy(alpha = 0.7f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = com.example.util.tr(announcement.message),
                        color = TextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Botón de confirmación / lectura
                Button(
                    onClick = {
                        GlobalAnnouncementManager.dismissAnnouncement(
                            context = context,
                            announcementId = announcement.id,
                            timestamp = announcement.timestamp
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text(
                        text = com.example.util.tr("Entendido"),
                        color = HextechDarkBg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

/**
 * Banner persistente y accesible en la pantalla de inicio para consultar el anuncio activo.
 */
@Composable
fun GlobalAnnouncementBanner(
    announcement: GlobalAnnouncement,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = if (announcement.isUrgent) DangerRed else HextechGold
    val bgColor = if (announcement.isUrgent) DangerRed.copy(alpha = 0.15f) else HextechGold.copy(alpha = 0.12f)
    val borderColor = if (announcement.isUrgent) DangerRed.copy(alpha = 0.45f) else HextechGold.copy(alpha = 0.35f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (announcement.isUrgent) Icons.Default.Warning else Icons.Default.Campaign,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = com.example.util.tr(announcement.title),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = com.example.util.tr(if (announcement.isUrgent) "Aviso urgente de mantenimiento • Toca para leer" else "Comunicado oficial activo • Toca para leer"),
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = com.example.util.tr("Ver"),
            color = accentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
