package com.example.ui.components

import com.example.util.tr

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AppNotice
import com.example.data.AppNoticeManager
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun AdminSponsorModerationDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val allNotices by AppNoticeManager.notices.collectAsState()
    var isSyncing by remember { mutableStateOf(false) }

    fun triggerSync() {
        coroutineScope.launch {
            isSyncing = true
            AppNoticeManager.syncFromCloud(context)
            AppNoticeManager.syncPendingSponsors(context)
            isSyncing = false
        }
    }

    LaunchedEffect(Unit) {
        triggerSync()
    }

    val sponsorNotices = remember(allNotices) {
        allNotices.filter { it.tag.equals("Publicidad", true) || it.sponsorEmail.isNotBlank() || !it.isApproved }
    }

    val pendingCount = remember(sponsorNotices) { sponsorNotices.count { !it.isApproved } }
    val approvedCount = remember(sponsorNotices) { sponsorNotices.count { it.isApproved } }

    var selectedFilter by remember { mutableStateOf(if (pendingCount > 0) "PENDING" else "ALL") }

    val filteredNotices = remember(sponsorNotices, selectedFilter) {
        when (selectedFilter) {
            "PENDING" -> sponsorNotices.filter { !it.isApproved }
            "APPROVED" -> sponsorNotices.filter { it.isApproved }
            else -> sponsorNotices.sortedBy { it.isApproved } // Pendientes primero
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false
        )
    ) {
        BackHandler(enabled = true) {
            onDismiss()
        }
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = HextechDarkBg,
            border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = HextechGold, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(tr("Moderación de Patrocinadores"), color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(tr("Acepta o rechaza publicaciones de anunciantes y patrocinadores"), color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { triggerSync() }) {
                            if (isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = HextechGold, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Sincronizar", tint = HextechGold)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Banner Informativo de Flujo
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HextechSurfaceVariant,
                    border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            tr("Al aprobar un patrocinio, este se activará y se visualizará directamente en el Panel de Anuncios y en la rotación de avisos de la app."),
                            color = TextPrimary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filtros de navegación
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedFilter == "PENDING",
                        onClick = { selectedFilter = "PENDING" },
                        label = { Text("Pendientes ($pendingCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFF59E0B),
                            selectedLabelColor = HextechDarkBg
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "APPROVED",
                        onClick = { selectedFilter = "APPROVED" },
                        label = { Text("Aprobados ($approvedCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF10B981),
                            selectedLabelColor = HextechDarkBg
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("Todos (${sponsorNotices.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HextechGold,
                            selectedLabelColor = HextechDarkBg
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (filteredNotices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            val emptyMsg = when (selectedFilter) {
                                "PENDING" -> "No hay anuncios de patrocinadores pendientes de revisión."
                                "APPROVED" -> "No hay anuncios aprobados aún."
                                else -> "No hay anuncios de patrocinadores registrados."
                            }
                            Text(emptyMsg, color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredNotices, key = { it.id }) { notice ->
                            AdminSponsorNoticeItem(
                                notice = notice,
                                onApprove = {
                                    AppNoticeManager.approveSponsorNotice(context, notice.id)
                                    Toast.makeText(context, "Anuncio aprobado y visible en el panel de anuncios", Toast.LENGTH_SHORT).show()
                                },
                                onReject = {
                                    AppNoticeManager.rejectSponsorNotice(context, notice.id)
                                    Toast.makeText(context, "Anuncio rechazado y eliminado", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSponsorNoticeItem(
    notice: AppNotice,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val context = LocalContext.current
    val statusText = if (notice.isApproved) "Aprobado (Visible)" else "Pendiente de Aprobación"
    val statusColor = if (notice.isApproved) Color(0xFF10B981) else Color(0xFFF59E0B)

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDmDialog by remember { mutableStateOf(false) }
    var showMediaViewerDialog by remember { mutableStateOf(false) }

    val isVideo = NoticeMediaUtils.isVideo(context, notice.videoUrl)
    val isVertical = notice.expandedImageUrl.isNotBlank() || NoticeMediaUtils.isMediaVertical(context, notice.videoUrl)
    val hasUrl = notice.externalUrl.isNotBlank()

    if (showMediaViewerDialog) {
        Dialog(onDismissRequest = { showMediaViewerDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HextechSurface),
                border = BorderStroke(1.dp, HextechGold)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(tr("Vista Previa Multimedia"), color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(if (isVertical) "Orientación: Vertical" else "Orientación: Horizontal", color = HextechCyan, fontSize = 11.sp)
                        }
                        IconButton(onClick = { showMediaViewerDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        val mediaUrlToPlay = notice.videoUrl.ifBlank { notice.expandedImageUrl }
                        NoticeMediaViewer(
                            mediaUrl = mediaUrlToPlay,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(tr("Advertencia de Eliminación"), color = HextechGold, fontWeight = FontWeight.Bold) },
            text = { Text(tr("¿Estás seguro de que deseas eliminar este patrocinador? Esta acción no se puede deshacer."), color = TextPrimary) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onReject()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text(tr("Eliminar"), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(tr("Cancelar"), color = TextSecondary)
                }
            },
            containerColor = HextechSurface
        )
    }

    if (showDmDialog) {
        SupportReplyDialog(
            reportId = notice.id,
            reportTitle = notice.title.ifBlank { "Patrocinio: ${notice.sponsorEmail}" },
            reportDescription = notice.content.ifBlank { "Consulta o moderación de anuncio patrocinado" },
            userEmail = notice.sponsorEmail.ifBlank { "patrocinador@coach.app" },
            userName = notice.sponsorEmail.substringBefore("@").ifBlank { "Patrocinador" },
            tag = "PATROCINADOR",
            isFirestoreDoc = true,
            onDismiss = { showDmDialog = false },
            onReplySent = { replyText, _ ->
                Toast.makeText(context, "DM enviado a ${notice.sponsorEmail}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(notice.title, color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(notice.content, color = TextSecondary, fontSize = 12.sp, maxLines = 3, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)

            // Clickable Media Preview Banner (Toca para ver imagen o video)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMediaViewerDialog = true },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = HextechSurfaceVariant
                        ) {
                            Icon(
                                imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.Image,
                                contentDescription = null,
                                tint = HextechGold,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isVideo) "Video adjunto (${if (isVertical) "Vertical" else "Horizontal"})" else "Imagen adjunta (${if (isVertical) "Vertical" else "Horizontal"})",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = tr("Toca para reproducir / ver en pantalla completa"),
                                color = HextechCyan,
                                fontSize = 9.5.sp
                            )
                        }
                    }
                    Icon(Icons.Default.Visibility, contentDescription = "Ver", tint = HextechGold, modifier = Modifier.size(18.dp))
                }
            }

            // Badges: Tipo, Orientación y URL con opción de copiar/abrir
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HextechDarkBg, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tipo
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = HextechSurfaceVariant
                    ) {
                        Text(
                            text = if (isVideo) "VIDEO" else "IMAGEN",
                            color = HextechGold,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    // Orientación
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = HextechSurfaceVariant
                    ) {
                        Text(
                            text = if (isVertical) "VERTICAL" else "HORIZONTAL",
                            color = HextechCyan,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                // URL con botones Copiar y Abrir
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null, tint = if (hasUrl) Color(0xFF10B981) else TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (hasUrl) "URL: ${notice.externalUrl}" else "Sin URL externa",
                            color = if (hasUrl) Color(0xFF10B981) else TextSecondary,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    if (hasUrl) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("URL Anuncio" , notice.externalUrl)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "URL copiada al portapapeles", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(tr("Copiar"), color = HextechCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            TextButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(notice.externalUrl.trim()))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "No se pudo abrir la URL", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(tr("Abrir"), color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            val unitLabel = when (notice.durationUnit.lowercase(Locale.ROOT)) {
                "hour", "hours", "hora", "horas" -> "Horas"
                "day", "days", "dia", "dias", "día", "días" -> "Días"
                "week", "weeks", "semana", "semanas" -> "Semanas"
                "month", "months", "mes", "meses" -> "Meses"
                "year", "years", "año", "años", "ano", "anos" -> "Años"
                else -> notice.durationUnit
            }

            val expirationStr = if (notice.isApproved && notice.expiresAtMillis > 0L) {
                val now = System.currentTimeMillis()
                val diff = notice.expiresAtMillis - now
                if (diff > 0) {
                    val hours = diff / (1000 * 60 * 60)
                    val days = hours / 24
                    if (days > 0) "Expira en: ${days}d ${hours % 24}h"
                    else "Expira en: ${hours}h ${(diff / (1000 * 60)) % 60}m"
                } else {
                    val remainingDeletionMillis = (notice.expiresAtMillis + 7 * 24 * 60 * 60 * 1000L) - now
                    if (remainingDeletionMillis > 0) {
                        val totalHours = remainingDeletionMillis / (1000 * 60 * 60)
                        val days = totalHours / 24
                        val hours = totalHours % 24
                        val mins = (remainingDeletionMillis / (1000 * 60)) % 60
                        if (days > 0) "Expirado (Se eliminará en ${days}d ${hours}h)"
                        else "Expirado (Se eliminará en ${hours}h ${mins}m)"
                    } else {
                        "Expirado (Programado para eliminación)"
                    }
                }
            } else null

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Patrocinador: ${if (notice.sponsorEmail.isNotBlank()) notice.sponsorEmail else "N/D"}", color = HextechCyan, fontSize = 11.sp)
                    Text("Presupuesto: $${String.format(Locale.US, "%.2f", notice.budget)} USD", color = TextSecondary, fontSize = 11.sp)
                    Text("Duración: ${notice.durationValue} $unitLabel", color = HextechGold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    if (expirationStr != null) {
                        Text(expirationStr, color = if (expirationStr.startsWith("Expirado")) DangerRed else Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { showDmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = HextechSurfaceVariant),
                        border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(tr("DM"), color = HextechCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (!notice.isApproved) {
                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(tr("Aprobar"), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Button(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(tr("Eliminar"), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
