package com.example.ui.components

import com.example.ui.components.CoachButton as Button
import com.example.ui.components.CoachOutlinedButton as OutlinedButton
import com.example.ui.components.CoachTextButton as TextButton
import com.example.ui.components.CoachIconButton as IconButton
import com.example.util.tr
import androidx.compose.foundation.BorderStroke
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.ui.components.coachClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextDecoration
import java.util.UUID
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun AdminNoticeConfigDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val currentNotices by com.example.data.AppNoticeManager.notices.collectAsState()
    val currentIntervalVal by com.example.data.AppNoticeManager.streamerIntervalValue.collectAsState()
    val currentIntervalUnit by com.example.data.AppNoticeManager.streamerIntervalUnit.collectAsState()
    var showCpmFromNotices by remember { mutableStateOf(false) }

    if (showCpmFromNotices) {
        AdminCpmAnalyticsDialog(onDismiss = { showCpmFromNotices = false })
    }

    var noticesList by remember { mutableStateOf(currentNotices) }
    var intervalValueText by remember { mutableStateOf(currentIntervalVal.toString()) }
    var intervalUnit by remember { mutableStateOf(currentIntervalUnit) }
    var isSavingCloud by remember { mutableStateOf(false) }

    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var editingNoticeId by remember { mutableStateOf<String?>(null) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    var expandedImageUrl by remember { mutableStateOf("") }
    var externalUrl by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("Anuncios importantes") }
    var titleColor by remember { mutableStateOf("#FFD700") }
    var contentColor by remember { mutableStateOf("#CCCCCC") }
    var isEnabled by remember { mutableStateOf(true) }
    var budgetText by remember { mutableStateOf("") }

    // Carga instantánea y sincronización reactiva de anuncios
    LaunchedEffect(Unit) {
        com.example.data.AppNoticeManager.syncFromCloud(context)
    }
    LaunchedEffect(currentNotices) {
        if (editingIndex == null && title.isBlank() && content.isBlank()) {
            noticesList = currentNotices
        }
    }

    val tagsList = listOf("Anuncios importantes", "Ofertas", "Mantenimiento", "Noticia", "Streamer", "PUBLICIDAD")
    val isUrlValid = remember(videoUrl) { NoticeMediaUtils.isValidNoticeMedia(videoUrl) }
    val isExpandedUrlValid = remember(expandedImageUrl) { NoticeMediaUtils.isValidNoticeMedia(expandedImageUrl) }
    val coroutineScope = rememberCoroutineScope()
    var isProcessingMedia by remember { mutableStateOf(false) }
    var showManualVideoUrlInput by remember { mutableStateOf(false) }
    var showManualExpandedUrlInput by remember { mutableStateOf(false) }

    val clipboardManager = remember { context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager }
    fun copyToClipboard(label: String, text: String) {
        val clip = android.content.ClipData.newPlainText(label, text)
        clipboardManager?.setPrimaryClip(clip)
        Toast.makeText(context, com.example.util.appTr("Copiado al portapapeles: $text"), Toast.LENGTH_SHORT).show()
    }

    val hasFilledFields = title.isNotBlank() ||
        content.isNotBlank() ||
        videoUrl.isNotBlank() ||
        expandedImageUrl.isNotBlank() ||
        externalUrl.isNotBlank() ||
        budgetText.isNotBlank() ||
        editingIndex != null

    fun attemptDismiss() {
        if (hasFilledFields) {
            Toast.makeText(
                context,
                com.example.util.appTr("No puedes salir mientras haya campos con información. Guarda el anuncio o límpialos para evitar cierres accidentales."),
                Toast.LENGTH_LONG
            ).show()
        } else {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = { attemptDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        BackHandler(enabled = true) {
            if (showCpmFromNotices) {
                showCpmFromNotices = false
            } else if (editingIndex != null) {
                editingIndex = null
                editingNoticeId = null
                title = ""
                content = ""
                videoUrl = ""
                expandedImageUrl = ""
                externalUrl = ""
                showManualVideoUrlInput = false
                showManualExpandedUrlInput = false
                Toast.makeText(context, com.example.util.appTr("Edición cancelada."), Toast.LENGTH_SHORT).show()
            } else {
                attemptDismiss()
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = HextechDarkBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Barra superior fija / cabecera completa
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = HextechSurface,
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Announcement, contentDescription = null, tint = HextechGold, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(tr("Gestor de Anuncios y Noticias"), color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(
                                    com.example.util.tr(if (hasFilledFields) "Edición activa (salida bloqueada contra pérdidas)" else "Pantalla completa • Gestión de avisos oficiales"),
                                    color = if (hasFilledFields) HextechGold else TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        IconButton(
                            onClick = { attemptDismiss() },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (hasFilledFields) HextechGold.copy(alpha = 0.15f) else HextechSurfaceVariant
                            )
                        ) {
                            Icon(
                                imageVector = if (hasFilledFields) Icons.Default.Lock else Icons.Default.Close,
                                contentDescription = com.example.util.trNullable("Cerrar"),
                                tint = if (hasFilledFields) HextechGold else TextSecondary
                            )
                        }
                    }
                }

                // Banner de advertencia de protección si hay campos llenos
                if (hasFilledFields) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = HextechGold.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = HextechGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    tr("Protección activa: Hay campos con información. Guarda o vacía los campos para poder salir."),
                                    color = HextechGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            TextButton(
                                onClick = {
                                    editingIndex = null
                                    editingNoticeId = null
                                    title = ""
                                    content = ""
                                    videoUrl = ""
                                    expandedImageUrl = ""
                                    externalUrl = ""
                                    showManualVideoUrlInput = false
                                    showManualExpandedUrlInput = false
                                    Toast.makeText(context, com.example.util.appTr("Campos limpiados. Salida desbloqueada."), Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = DangerRed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(tr("Limpiar campos"), color = DangerRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Contenido desplazable
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(tr("Administra los avisos y anuncios oficiales que se muestran en la pantalla de inicio:"), color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(10.dp))

                // Banner to open CPM & Monetization metrics
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .coachClickable { showCpmFromNotices = true },
                    color = Color(0xFF00FF66).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.2.dp, Color(0xFF00FF66).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF00FF66), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(tr("Métricas de CPM & Monetización"), color = Color(0xFF00FF66), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(tr("Ver impresiones, clics, CTR e ingresos estimados"), color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF00FF66), modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Card with copyable image dimension recommendations
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhotoSizeSelectActual, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(tr("📐 Medidas Recomendadas (Toca para Copiar)"), color = HextechCyan, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(tr("Utiliza estas resoluciones exactas para que tus imágenes y videos queden perfectamente encuadrados:"), color = TextSecondary, fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Horizontal dimensions chip
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .coachClickable { copyToClipboard("Medida Horizontal", "1920x1080") },
                            color = HextechSurfaceVariant,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tr("🖼️ Horizontal (Panel de Inicio / Tarjeta):"), color = HextechCyan, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                    Text(tr("1920 x 1080 px  (Relación 16:9)"), color = TextPrimary, fontSize = 10.sp)
                                }
                                Icon(Icons.Default.ContentCopy, contentDescription = com.example.util.trNullable("Copiar"), tint = HextechCyan, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Vertical dimensions chip
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .coachClickable { copyToClipboard("Medida Vertical", "1080x1920") },
                            color = HextechSurfaceVariant,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tr("📱 Vertical (Vista Ampliada / Fullscreen):"), color = HextechGold, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                    Text(tr("1080 x 1920 px  (9:16 Pantalla Completa)"), color = TextPrimary, fontSize = 10.sp)
                                }
                                Icon(Icons.Default.ContentCopy, contentDescription = com.example.util.trNullable("Copiar"), tint = HextechGold, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Interval configuration for streamer and publicidad notices
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = HextechSurfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(tr("⏱️ Intervalo de Rotación (Todos los Anuncios > 1)"), color = HextechGold, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(tr("Si hay más de 1 anuncio en una categoría, rotarán automáticamente con este intervalo (si solo hay 1, se queda fijo):"), color = TextSecondary, fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = intervalValueText,
                                onValueChange = { intervalValueText = it.filter { c -> c.isDigit() } },
                                label = { Text(tr("Valor")) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            val units = listOf("seconds" to "Seg", "minutes" to "Min", "hours" to "Horas")
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                units.forEach { (key, label) ->
                                    val isSelected = intervalUnit == key
                                    Button(
                                        onClick = { intervalUnit = key },
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) HextechCyan else HextechSurface
                                        )
                                    ) {
                                        Text(com.example.util.tr(label), color = if (isSelected) HextechDarkBg else TextPrimary, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // List of existing notices (only active/non-expired ones in Anuncios Actuales)
                val now = System.currentTimeMillis()
                val activeNotices = noticesList.filter { notice -> notice.expiresAtMillis == 0L || notice.expiresAtMillis > now }
                val expiredNotices = noticesList.filter { notice -> notice.expiresAtMillis > 0L && notice.expiresAtMillis <= now }

                if (activeNotices.isNotEmpty()) {
                    Text(com.example.util.tr("Anuncios Actuales (${activeNotices.size}):"), color = HextechCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    activeNotices.forEach { notice ->
                        val originalIndex = noticesList.indexOfFirst { it.id == notice.id }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            color = HextechSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (notice.isEnabled) HextechGold.copy(alpha = 0.5f) else TextMuted.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = com.example.util.tr(notice.tag), color = HextechGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        if (notice.sponsorEmail.isNotBlank() || notice.tag.equals("Publicidad", true)) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                                border = BorderStroke(0.5.dp, Color(0xFF10B981))
                                            ) {
                                                Text(
                                                    tr("PATROCINIO APROBADO"),
                                                    color = Color(0xFF10B981),
                                                    fontSize = 7.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = com.example.util.tr("•"), color = TextMuted, fontSize = 9.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = com.example.util.tr(notice.title.ifBlank { "Sin título" }),
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                    Text(
                                        text = com.example.util.tr(notice.content),
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    // Visual Media Indicators
                                    Row(
                                        modifier = Modifier.padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (notice.videoUrl.isNotBlank()) {
                                            Surface(
                                                color = HextechCyan.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp),
                                                border = BorderStroke(0.5.dp, HextechCyan.copy(alpha = 0.4f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        if (notice.videoUrl.contains("video") || notice.videoUrl.endsWith(".mp4")) Icons.Default.Videocam else Icons.Default.Image,
                                                        contentDescription = null,
                                                        tint = HextechCyan,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(tr("Horizontal"), color = HextechCyan, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        if (notice.expandedImageUrl.isNotBlank()) {
                                            Surface(
                                                color = HextechGold.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp),
                                                border = BorderStroke(0.5.dp, HextechGold.copy(alpha = 0.4f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = HextechGold, modifier = Modifier.size(10.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(tr("Vertical"), color = HextechGold, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        if (notice.externalUrl.isNotBlank()) {
                                            Surface(
                                                color = Color(0xFF3399FF).copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp),
                                                border = BorderStroke(0.5.dp, Color(0xFF3399FF).copy(alpha = 0.4f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = Color(0xFF3399FF), modifier = Modifier.size(10.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(tr("Enlace"), color = Color(0xFF3399FF), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        if (notice.budget > 0) {
                                            Surface(
                                                color = Color(0xFF00FF66).copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp),
                                                border = BorderStroke(0.5.dp, Color(0xFF00FF66).copy(alpha = 0.4f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color(0xFF00FF66), modifier = Modifier.size(10.dp))
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(com.example.util.tr("$${String.format(Locale.US, "%.2f", notice.budget)}"), color = Color(0xFF00FF66), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            editingIndex = originalIndex
                                            editingNoticeId = notice.id
                                            title = notice.title
                                            content = notice.content
                                            videoUrl = notice.videoUrl
                                            expandedImageUrl = notice.expandedImageUrl
                                            externalUrl = notice.externalUrl
                                            selectedTag = notice.tag
                                            titleColor = notice.titleColor
                                            contentColor = notice.contentColor
                                            isEnabled = notice.isEnabled
                                            budgetText = if (notice.budget > 0) String.format(Locale.US, "%.2f", notice.budget) else ""
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = com.example.util.trNullable("Editar"), tint = HextechCyan, modifier = Modifier.size(14.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            if (editingIndex == originalIndex || editingNoticeId == notice.id) {
                                                editingIndex = null
                                                editingNoticeId = null
                                                title = ""
                                                content = ""
                                                videoUrl = ""
                                                expandedImageUrl = ""
                                                externalUrl = ""
                                                budgetText = ""
                                            }
                                            noticesList = noticesList.filter { it.id != notice.id }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Eliminar"), tint = DangerRed, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (expiredNotices.isNotEmpty()) {
                    Text(com.example.util.tr("Anuncios Expirados (${expiredNotices.size}):"), color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    expiredNotices.forEach { notice ->
                        val originalIndex = noticesList.indexOfFirst { it.id == notice.id }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            color = HextechSurfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.5.dp, TextMuted.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = com.example.util.tr("${notice.tag} • ${notice.title.ifBlank { "Sin título" }} (Expirado)"), color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                                IconButton(
                                    onClick = {
                                        noticesList = noticesList.filter { it.id != notice.id }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Eliminar"), tint = DangerRed, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Divider(color = HextechCardBorder)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = com.example.util.tr(if (editingIndex != null) "✏️ Editando Anuncio #${editingIndex!! + 1}" else "➕ Agregar Nuevo Anuncio:"),
                        color = HextechGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (editingIndex != null) {
                        TextButton(
                            onClick = {
                                editingIndex = null
                                editingNoticeId = null
                                title = ""
                                content = ""
                                videoUrl = ""
                                expandedImageUrl = ""
                                externalUrl = ""
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = DangerRed, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(tr("Cancelar"), color = DangerRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Tag selector
                Text(tr("Etiqueta / Categoría:"), color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tagsList.forEach { tag ->
                        val isSelected = selectedTag == tag
                        Button(
                            onClick = { selectedTag = tag },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) HextechGold else HextechSurfaceVariant
                            )
                        ) {
                            Text(com.example.util.tr(tag), color = if (isSelected) HextechDarkBg else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(tr("Título del Aviso")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(com.example.util.tr("🎨 " + tr("Color del Título:")), color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val colorsList = listOf(
                        "Dorado" to "#FFD700",
                        "Cian" to "#00F2FE",
                        "Blanco" to "#FFFFFF",
                        "Verde" to "#00FF66",
                        "Naranja" to "#FF9900",
                        "Rojo" to "#FF3333",
                        "Morado" to "#CC66FF"
                    )
                    colorsList.forEach { (name, hex) ->
                        val isSelected = titleColor.equals(hex, true)
                        val parsedColor = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { HextechGold }
                        Button(
                            onClick = { titleColor = hex },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) parsedColor else HextechSurfaceVariant
                            ),
                            border = BorderStroke(1.dp, parsedColor)
                        ) {
                            Text(tr(name), color = if (isSelected) HextechDarkBg else parsedColor, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(tr("Contenido / Descripción")) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(tr("🎨 Color de la Descripción:"), color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val contentColorsList = listOf(
                        "Gris Claro" to "#CCCCCC",
                        "Blanco" to "#FFFFFF",
                        "Cian" to "#00F2FE",
                        "Dorado" to "#FFD700",
                        "Verde" to "#00FF66",
                        "Amarillo" to "#FFEE55"
                    )
                    contentColorsList.forEach { (name, hex) ->
                        val isSelected = contentColor.equals(hex, true)
                        val parsedColor = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { TextSecondary }
                        Button(
                            onClick = { contentColor = hex },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) parsedColor else HextechSurfaceVariant
                            ),
                            border = BorderStroke(1.dp, parsedColor)
                        ) {
                            Text(com.example.util.tr(name), color = if (isSelected) HextechDarkBg else parsedColor, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 1. Horizontal Media (Home screen)
                Spacer(modifier = Modifier.height(12.dp))
                Text(tr("1. Multimedia Horizontal (Panel de Inicio):"), color = HextechCyan, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                val horizontalMediaPickerLauncher = rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                ) { uri: android.net.Uri? ->
                    uri?.let { pickedUri ->
                        coroutineScope.launch {
                            isProcessingMedia = true
                            try {
                                val validation = NoticeMediaUtils.validateMediaForSlot(context, pickedUri, isVerticalSlot = false)
                                if (!validation.isValid) {
                                    Toast.makeText(context, com.example.util.appTr(validation.errorMessage ?: "El archivo no cumple con el tamaño o proporción recomendada para banner horizontal"), Toast.LENGTH_LONG).show()
                                    isProcessingMedia = false
                                    return@launch
                                }

                                if (validation.isVideo) {
                                    Toast.makeText(context, com.example.util.appTr("Procesando video horizontal..."), Toast.LENGTH_SHORT).show()
                                    val finalVideoUrl = com.example.util.NoticeMediaStorageManager.uploadOrSaveVideo(context, pickedUri)
                                    videoUrl = finalVideoUrl
                                    Toast.makeText(context, com.example.util.appTr("Video horizontal configurado exitosamente"), Toast.LENGTH_SHORT).show()
                                } else {
                                    val cloudDataUrl = com.example.util.NoticeMediaStorageManager.convertImageToCloudDataUrl(context, pickedUri)
                                    videoUrl = cloudDataUrl
                                    Toast.makeText(context, com.example.util.appTr("Imagen horizontal configurada correctamente"), Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, com.example.util.appTr("No se pudo procesar el archivo seleccionado"), Toast.LENGTH_SHORT).show()
                            } finally {
                                isProcessingMedia = false
                            }
                        }
                    }
                }

                if (videoUrl.isNotBlank()) {
                    Surface(
                        color = HextechSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            val isVideo = NoticeMediaUtils.isVideo(context, videoUrl)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isVideo) {
                                    NoticeMediaViewer(
                                        mediaUrl = videoUrl,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    val decodedBytes = remember(videoUrl) {
                                        if (videoUrl.startsWith("data:image/")) {
                                            com.example.util.NoticeMediaStorageManager.decodeDataUriToBytes(videoUrl)
                                        } else if (videoUrl.startsWith("file://")) {
                                            val path = Uri.parse(videoUrl).path ?: ""
                                            java.io.File(path)
                                        } else null
                                    }
                                    AsyncImage(
                                        model = decodedBytes ?: videoUrl,
                                        contentDescription = com.example.util.tr("Vista previa multimedia horizontal"),
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = com.example.util.tr(if (isVideo) "🎬 Video Horizontal Configurado" else "🖼️ Imagen Horizontal Configurada"),
                                        color = HextechCyan,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = tr("Visible en la tarjeta de novedades en el inicio."),
                                        color = TextMuted,
                                        fontSize = 9.5.sp
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = { horizontalMediaPickerLauncher.launch("*/*") },
                                        colors = ButtonDefaults.buttonColors(containerColor = HextechSurface),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.6f)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(tr("Cambiar"), color = HextechCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { videoUrl = "" },
                                        colors = ButtonDefaults.buttonColors(containerColor = HextechSurface),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.6f)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = DangerRed, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(tr("Quitar"), color = DangerRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (showManualVideoUrlInput) {
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = videoUrl,
                                    onValueChange = { videoUrl = it },
                                    label = { Text(tr("Editar enlace o URL")) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(6.dp)
                                )
                            } else {
                                Text(
                                    text = tr("Editar enlace o URL"),
                                    color = HextechCyan.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .coachClickable { showManualVideoUrlInput = true }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = videoUrl,
                        onValueChange = { videoUrl = it },
                        label = { Text(tr("URL YouTube o Imagen/Video Horizontal")) },
                        singleLine = true,
                        maxLines = 1,
                        isError = !isUrlValid,
                        supportingText = {
                            if (!isUrlValid) {
                                Text(tr("Enlace inválido. Solo URLs de YouTube o archivos de galería."), color = DangerRed, fontSize = 10.sp)
                            } else {
                                Text(tr("Se muestra en la tarjeta del panel de inicio (Horizontal)"), color = TextMuted, fontSize = 10.sp)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { horizontalMediaPickerLauncher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = HextechSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(tr("Subir Multimedia Horizontal desde Galería"), color = HextechCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // 2. Vertical Expanded Media (Video or Image)
                Spacer(modifier = Modifier.height(12.dp))
                Text(tr("2. Multimedia Vertical (Video o Imagen para Vista Ampliada):"), color = HextechGold, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                val verticalImagePickerLauncher = rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                ) { uri: android.net.Uri? ->
                    uri?.let { pickedUri ->
                        coroutineScope.launch {
                            isProcessingMedia = true
                            try {
                                val validation = NoticeMediaUtils.validateMediaForSlot(context, pickedUri, isVerticalSlot = true)
                                if (!validation.isValid) {
                                    Toast.makeText(context, com.example.util.appTr(validation.errorMessage ?: "El archivo no cumple con el tamaño o proporción recomendada para vista vertical"), Toast.LENGTH_LONG).show()
                                    isProcessingMedia = false
                                    return@launch
                                }

                                if (validation.isVideo) {
                                    val finalVideoUrl = com.example.util.NoticeMediaStorageManager.uploadOrSaveVideo(context, pickedUri)
                                    expandedImageUrl = finalVideoUrl
                                    Toast.makeText(context, com.example.util.appTr("Video vertical configurado correctamente"), Toast.LENGTH_SHORT).show()
                                } else {
                                    val cloudDataUrl = com.example.util.NoticeMediaStorageManager.convertImageToCloudDataUrl(context, pickedUri)
                                    expandedImageUrl = cloudDataUrl
                                    Toast.makeText(context, com.example.util.appTr("Imagen vertical subida correctamente"), Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, com.example.util.appTr("No se pudo procesar el archivo seleccionado"), Toast.LENGTH_SHORT).show()
                            } finally {
                                isProcessingMedia = false
                            }
                        }
                    }
                }

                if (expandedImageUrl.isNotBlank()) {
                    Surface(
                        color = HextechSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val isVerticalVideo = remember(expandedImageUrl) {
                                    NoticeMediaUtils.isVideo(context, expandedImageUrl)
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.55f)
                                        .aspectRatio(9f / 16f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black)
                                        .border(1.dp, HextechGold.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isVerticalVideo) {
                                        NoticeMediaViewer(
                                            mediaUrl = expandedImageUrl,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        val decodedVerticalBytes = remember(expandedImageUrl) {
                                            if (expandedImageUrl.startsWith("data:image/")) {
                                                com.example.util.NoticeMediaStorageManager.decodeDataUriToBytes(expandedImageUrl)
                                            } else if (expandedImageUrl.startsWith("file://")) {
                                                val path = Uri.parse(expandedImageUrl).path ?: ""
                                                java.io.File(path)
                                            } else null
                                        }
                                        AsyncImage(
                                            model = decodedVerticalBytes ?: expandedImageUrl,
                                            contentDescription = com.example.util.tr("Vista previa vertical"),
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = com.example.util.tr(if (isVerticalVideo) "🎬 Video Vertical Configurado" else "📱 Imagen Vertical Configurada"),
                                        color = HextechGold,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = com.example.util.tr(if (isVerticalVideo) "Se reproducirá a pantalla completa en modo vertical al pulsar 'Ampliar'." else "Se mostrará a pantalla completa al pulsar 'Ampliar' o al tocar la imagen."),
                                        color = TextMuted,
                                        fontSize = 9.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = { verticalImagePickerLauncher.launch("*/*") },
                                            colors = ButtonDefaults.buttonColors(containerColor = HextechSurface),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.6f)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, tint = HextechGold, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(tr("Cambiar"), color = HextechGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { expandedImageUrl = "" },
                                            colors = ButtonDefaults.buttonColors(containerColor = HextechSurface),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.6f)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, tint = DangerRed, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(tr("Quitar"), color = DangerRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            if (showManualExpandedUrlInput) {
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = expandedImageUrl,
                                    onValueChange = { expandedImageUrl = it },
                                    label = { Text(tr("Editar URL vertical manualmente")) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(6.dp)
                                )
                            } else {
                                Text(
                                    text = tr("Editar enlace o URL"),
                                    color = HextechGold.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .coachClickable { showManualExpandedUrlInput = true }
                                 )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = expandedImageUrl,
                        onValueChange = { expandedImageUrl = it },
                        label = { Text(tr("URL o Video/Imagen Vertical Ampliada (Opcional)")) },
                        singleLine = true,
                        maxLines = 1,
                        isError = !isExpandedUrlValid,
                        supportingText = {
                            if (!isExpandedUrlValid) {
                                Text(tr("Enlace inválido. Solo URLs de YouTube/Imágenes o archivos de galería."), color = DangerRed, fontSize = 10.sp)
                            } else {
                                Text(tr("Video o Imagen vertical que se observará al pulsar en 'Ampliar'"), color = TextMuted, fontSize = 10.sp)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { verticalImagePickerLauncher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = HextechSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = HextechGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(tr("Subir Multimedia Vertical (Video o Imagen)"), color = HextechGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (isProcessingMedia) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(HextechSurfaceVariant, RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = HextechCyan, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(tr("Optimizando multimedia para Multidispositivo..."), color = HextechCyan, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = HextechGold.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = HextechGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tr("☁️ MULTIDISPOSITIVO ACTIVO (Sincronización en la nube)"),
                                color = HextechGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = com.example.util.tr("• Las imágenes de galería se convierten automáticamente al formato de nube para verse en CUALQUIER celular y no ponerse negras al reiniciar.\n• Para videos en múltiples celulares, usa enlaces de YouTube (o Shorts) o URLs web (.mp4). Los videos locales se guardan permanentemente en este celular."),
                            color = TextSecondary,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(tr("3. 🔗 Enlace Web Externo (Opcional):"), color = HextechGold, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = externalUrl,
                    onValueChange = { externalUrl = it },
                    label = { Text(tr("URL de sitio web externo (Redirección al tocar imagen)")) },
                    supportingText = {
                        Text(tr("Si se define, al ampliar la imagen se podrá abrir este enlace web externamente."), color = TextMuted, fontSize = 10.sp)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(com.example.util.tr("4. 💰 " + tr("Presupuesto de Campaña / Anuncio (USD - Opcional):")), color = HextechGold, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) {
                            budgetText = input
                        }
                    },
                    label = { Text(tr("Presupuesto en USD (ej. 50.00)")) },
                    placeholder = { Text(com.example.util.tr("0.00")) },
                    leadingIcon = {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, tint = HextechGold)
                    },
                    supportingText = {
                        Text(tr("Monitorea el gasto y saldo restante de este anuncio en el panel de analíticas CPM."), color = TextMuted, fontSize = 10.sp)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(tr("Activar anuncio en inicio"), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Switch(checked = isEnabled, onCheckedChange = { isEnabled = it })
                }

                Spacer(modifier = Modifier.height(8.dp))
                if (editingIndex != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                editingIndex = null
                                editingNoticeId = null
                                title = ""
                                content = ""
                                videoUrl = ""
                                expandedImageUrl = ""
                                externalUrl = ""
                                budgetText = ""
                                showManualVideoUrlInput = false
                                showManualExpandedUrlInput = false
                                Toast.makeText(context, com.example.util.appTr("Edición cancelada"), Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                            border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = DangerRed, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(tr("Cancelar Edición"), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                if (title.isBlank() && content.isBlank()) {
                                    Toast.makeText(context, com.example.util.appTr("Ingresa un título o contenido para el anuncio"), Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (!isUrlValid || !isExpandedUrlValid) {
                                    Toast.makeText(context, com.example.util.appTr("URL multimedia inválida"), Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val targetId = editingNoticeId ?: noticesList[editingIndex!!].id
                                val newNotice = com.example.data.AppNotice(
                                    id = targetId,
                                    title = title.ifBlank { "Aviso Oficial" },
                                    content = content,
                                    videoUrl = videoUrl,
                                    expandedImageUrl = expandedImageUrl,
                                    externalUrl = externalUrl,
                                    tag = selectedTag,
                                    titleColor = titleColor,
                                    contentColor = contentColor,
                                    isEnabled = isEnabled,
                                    budget = budgetText.toDoubleOrNull() ?: 0.0
                                )
                                noticesList = noticesList.toMutableList().apply { set(editingIndex!!, newNotice) }
                                editingIndex = null
                                editingNoticeId = null
                                title = ""
                                content = ""
                                videoUrl = ""
                                expandedImageUrl = ""
                                externalUrl = ""
                                budgetText = ""
                                showManualVideoUrlInput = false
                                showManualExpandedUrlInput = false
                                Toast.makeText(context, com.example.util.appTr("Anuncio guardado en la lista"), Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = HextechCyan, contentColor = HextechDarkBg),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(tr("Guardar Cambios"), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            if (title.isBlank() && content.isBlank()) {
                                Toast.makeText(context, com.example.util.appTr("Ingresa un título o contenido para el anuncio"), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (!isUrlValid || !isExpandedUrlValid) {
                                Toast.makeText(context, com.example.util.appTr("URL multimedia inválida"), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val targetId = UUID.randomUUID().toString()
                            val newNotice = com.example.data.AppNotice(
                                id = targetId,
                                title = title.ifBlank { "Aviso Oficial" },
                                content = content,
                                videoUrl = videoUrl,
                                expandedImageUrl = expandedImageUrl,
                                externalUrl = externalUrl,
                                tag = selectedTag,
                                titleColor = titleColor,
                                contentColor = contentColor,
                                isEnabled = isEnabled,
                                budget = budgetText.toDoubleOrNull() ?: 0.0
                            )
                            noticesList = noticesList + newNotice
                            title = ""
                            content = ""
                            videoUrl = ""
                            expandedImageUrl = ""
                            externalUrl = ""
                            budgetText = ""
                            editingNoticeId = null
                            showManualVideoUrlInput = false
                            showManualExpandedUrlInput = false
                            Toast.makeText(context, com.example.util.appTr("Anuncio guardado en la lista"), Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = HextechCyan, contentColor = HextechDarkBg),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(tr("➕ Agregar a la Lista de Anuncios"), fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(tr("👁️ Vista Previa del Anuncio:"), color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))

                // Live Preview Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, HextechGold.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = HextechSurfaceVariant.copy(alpha = 0.9f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Campaign, contentDescription = null, tint = HextechGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = com.example.util.tr(title.ifBlank { "Título del aviso..." }),
                                    color = HextechGold,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                color = HextechGold.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = com.example.util.tr(selectedTag.uppercase()),
                                    color = HextechGold,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = com.example.util.tr(content.ifBlank { "Escribe el contenido del anuncio para visualizarlo aquí..." }),
                            color = TextPrimary,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                        if (videoUrl.isNotBlank() && isUrlValid) {
                            Spacer(modifier = Modifier.height(8.dp))
                            NoticeMediaViewer(
                                mediaUrl = videoUrl,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                            )
                        }
                        if (expandedImageUrl.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(tr("📱 Vista previa (Media Ampliada):"), color = HextechGold, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            NoticeMediaViewer(
                                mediaUrl = expandedImageUrl,
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(160.dp)
                            )
                        }
                    }
                }
            }

            // Barra inferior fija de acciones
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = HextechSurface,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { attemptDismiss() },
                            enabled = !isSavingCloud,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (hasFilledFields) HextechGold.copy(alpha = 0.5f) else TextMuted),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (hasFilledFields) HextechGold else TextSecondary
                            )
                        ) {
                            Icon(
                                imageVector = if (hasFilledFields) Icons.Default.Lock else Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(com.example.util.tr(if (hasFilledFields) "Cerrar (Bloqueado)" else "Cerrar"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (isSavingCloud) return@Button
                                isSavingCloud = true
                                val intVal = intervalValueText.toIntOrNull() ?: 10
                                com.example.data.AppNoticeManager.saveAllNoticesAndInterval(
                                    context = context,
                                    newNotices = noticesList,
                                    intervalValue = intVal,
                                    intervalUnit = intervalUnit
                                ) { success, errorMsg ->
                                    isSavingCloud = false
                                    if (success) {
                                        Toast.makeText(context, com.example.util.appTr("✅ ¡Anuncios sincronizados correctamente!"), Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    } else {
                                        Toast.makeText(context, com.example.util.appTr("✅ Guardado localmente con éxito"), Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                }
                            },
                            enabled = !isSavingCloud,
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HextechGold, contentColor = HextechDarkBg)
                        ) {
                            if (isSavingCloud) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = HextechDarkBg, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(tr("Sincronizando..."), fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(tr("Publicar Todos"), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
