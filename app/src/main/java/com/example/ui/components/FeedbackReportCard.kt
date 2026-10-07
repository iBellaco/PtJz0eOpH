package com.example.ui.components

import com.example.ui.components.CoachOutlinedButton as OutlinedButton
import com.example.ui.components.CoachIconButton as IconButton
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.ui.components.coachClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.data.SupportReplyManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.model.FeedbackReport
import com.example.data.FeedbackRepository
import com.example.model.WildRiftItem
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechGreen
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.HextechSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.tr
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
internal fun ComprehensiveFeedbackCard(
    report: FeedbackReport,
    currentStatus: String,
    onSelectStatus: (String) -> Unit,
    onReply: (() -> Unit)? = null,
    onDelete: (() -> Unit)?,
    onCopy: () -> Unit,
    onOpenImage: (Bitmap) -> Unit,
    onItemClick: (WildRiftItem) -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val itemCategory = remember(report) { getFeedbackCategory(report) }
    val isBugOrSupport = itemCategory in setOf("BUG", "SUPPORT", "PATROCINADOR", "PAGO")
    val canContinueConversation = onReply != null && !com.example.data.SupportConversationPolicy.isClosed(currentStatus)

    val isReadOrSolved = currentStatus == FeedbackRepository.STATUS_READ ||
            currentStatus == FeedbackRepository.STATUS_SOLVED ||
            currentStatus == FeedbackRepository.STATUS_COMPLETED ||
            currentStatus == FeedbackRepository.STATUS_ACCEPTED
    val createdMillis = remember(report.createdAt) {
        SupportReplyManager.parseDateToMillis(report.createdAt)
    }
    val countdown = remember(createdMillis, isReadOrSolved) {
        SupportReplyManager.calculateCountdown(createdMillis, isReadOrSolved)
    }

    // Parsear sugerencia de build si contiene el formato estructurado
    val parsedBuild = remember(report.cleanDescription, report.description, report.title) {
        parseBuildSuggestionFromText(report.cleanDescription.ifEmpty { report.description }, report.title)
    }

    // Parsear imágenes base64 si existen
    val attachedBitmaps = remember(report.deviceInfo, report.description) {
        extractBase64Images(report.deviceInfo + "\n" + report.description)
    }

    // Información del tipo
    val (typeColor, typeIcon, typeLabel) = when (itemCategory) {
        "PAGO" -> Triple(HextechGold, Icons.Default.Payments, "Pago")
        "PATROCINADOR" -> Triple(HextechGold, Icons.Default.Star, "PATROCINADOR")
        "BUG" -> Triple(DangerRed, Icons.Default.BugReport, "BUG / ERROR")
        "SUPPORT" -> Triple(HextechCyan, Icons.Default.SupportAgent, "SOPORTE")
        "BUILD" -> Triple(HextechGold, Icons.Default.SportsEsports, "BUILD SUGERIDA")
        else -> Triple(Color(0xFFFFB74D), Icons.Default.Lightbulb, "SUGERENCIA")
    }

    // Información del estado visual actual
    val (statusLabel, statusColor, statusIcon) = when (currentStatus) {
        FeedbackRepository.STATUS_SOLVED, FeedbackRepository.STATUS_COMPLETED -> {
            Triple(tr("Solucionado"), HextechGreen, Icons.Default.CheckCircle)
        }
        FeedbackRepository.STATUS_READ -> {
            Triple(tr("Leído"), HextechCyan, Icons.Default.Visibility)
        }
        FeedbackRepository.STATUS_ACCEPTED -> {
            Triple(tr("Aceptada"), HextechGold, Icons.Default.Star)
        }
        FeedbackRepository.STATUS_REJECTED -> {
            Triple(tr("Rechazada"), DangerRed, Icons.Default.Cancel)
        }
        else -> {
            Triple(tr("Pendiente"), Color(0xFFFFB300), Icons.Default.HourglassEmpty)
        }
    }

    val cardBorderColor by animateColorAsState(
        targetValue = statusColor.copy(alpha = 0.45f),
        label = "card_border"
    )

    val formattedDate = remember(report.createdAt) {
        formatReportDate(report.createdAt)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, cardBorderColor, RoundedCornerShape(12.dp))
            .animateContentSize(animationSpec = tween(180)),
        colors = CardDefaults.cardColors(containerColor = HextechSurface.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Fila Superior: Badges + Fecha + Acciones (Copiar, Borrar, Expandir)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Badge de Tipo
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(typeColor.copy(alpha = 0.18f))
                            .border(1.dp, typeColor.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(typeIcon, contentDescription = null, tint = typeColor, modifier = Modifier.size(11.dp))
                            Text(text = com.example.util.tr(typeLabel), color = typeColor, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Badge de Estado Actual
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.18f))
                            .border(1.dp, statusColor.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(11.dp))
                            Text(text = com.example.util.tr(statusLabel), color = statusColor, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Contador de Auto-eliminación (30 días leídos, 60 días sin leer)
                    val countdownBg = when {
                        countdown.isExpired -> DangerRed.copy(alpha = 0.2f)
                        countdown.remainingDays <= 3 -> Color(0xFFFF9800).copy(alpha = 0.2f)
                        isReadOrSolved -> HextechCyan.copy(alpha = 0.15f)
                        else -> Color(0xFFFFB300).copy(alpha = 0.15f)
                    }
                    val countdownColor = when {
                        countdown.isExpired -> DangerRed
                        countdown.remainingDays <= 3 -> Color(0xFFFF9800)
                        isReadOrSolved -> HextechCyan
                        else -> Color(0xFFFFB300)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(countdownBg)
                            .border(0.8.dp, countdownColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = countdownColor, modifier = Modifier.size(10.dp))
                            Text(text = com.example.util.tr(countdown.displayText), color = countdownColor, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = com.example.util.tr(formattedDate),
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCopy, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = tr("Copiar"), tint = TextMuted, modifier = Modifier.size(15.dp))
                    }
                    if (onDelete != null) IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = tr("Eliminar"), tint = DangerRed.copy(alpha = 0.8f), modifier = Modifier.size(15.dp))
                    }
                    IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(26.dp)) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = com.example.util.trNullable(if (expanded) tr("Contraer") else tr("Expandir")),
                            tint = HextechGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Título con botón para copiarlo directamente
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(HextechDarkBg.copy(alpha = 0.45f))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = com.example.util.tr(report.title),
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .coachClickable { expanded = !expanded }
                )
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Título", report.title))
                        Toast.makeText(context, com.example.util.appTr(" Título copiado al portapapeles"), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = com.example.util.trNullable("Copiar título"),
                        tint = HextechGold,
                        modifier = Modifier.size(13.5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            // RENDERIZADO DE SUGERENCIA DE BUILD GRÁFICA O DESCRIPCIÓN ESTÁNDAR
            val cleanDescription = remember(report.cleanDescription) {
                cleanDescriptionText(report.cleanDescription)
            }

            if (parsedBuild != null) {
                Spacer(modifier = Modifier.height(2.dp))
                GraphicalBuildSuggestionView(
                    build = parsedBuild,
                    onItemClick = onItemClick
                )
                Spacer(modifier = Modifier.height(4.dp))
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(HextechDarkBg.copy(alpha = 0.25f))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = com.example.util.tr(cleanDescription),
                        color = TextSecondary,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp,
                        maxLines = if (expanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .coachClickable { expanded = !expanded }
                    )
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Descripción", cleanDescription))
                            Toast.makeText(context, com.example.util.appTr(" Descripción copiada al portapapeles"), Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(24.dp)
                            .padding(start = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = com.example.util.trNullable("Copiar descripción"),
                            tint = HextechCyan,
                            modifier = Modifier.size(13.5.dp)
                        )
                    }
                }
            }

            // Miniaturas de Imágenes Adjuntas con indicador de zoom/descarga
            if (attachedBitmaps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = HextechGold, modifier = Modifier.size(14.dp))
                    Text(
                        text = tr("Capturas adjuntas (Toca para ampliar y descargar):"),
                        color = TextMuted,
                        fontSize = 10.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(attachedBitmaps) { bmp ->
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, HextechGold.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .coachClickable { onOpenImage(bmp) }
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = com.example.util.tr("Captura adjunta"),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(topStart = 4.dp))
                                    .padding(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = com.example.util.trNullable("Ampliar"),
                                    tint = HextechGold,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // CONTROLES DE ESTADO (Requisitos de selección para el usuario/admin)
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(HextechDarkBg.copy(alpha = 0.7f))
                    .border(0.6.dp, HextechCardBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Column {
                    Text(
                        text = tr("Marcar estado:"),
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    if (isBugOrSupport) {
                        // Opciones de Reportes / Soporte: Pendiente | Leído | Solucionado
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StatusActionButton(
                                label = tr("Pendiente"),
                                icon = Icons.Default.HourglassEmpty,
                                isSelected = currentStatus == FeedbackRepository.STATUS_PENDING,
                                activeColor = Color(0xFFFFB300),
                                modifier = Modifier.weight(1f),
                                onClick = { onSelectStatus(FeedbackRepository.STATUS_PENDING) }
                            )
                            StatusActionButton(
                                label = tr("Leído"),
                                icon = Icons.Default.Visibility,
                                isSelected = currentStatus == FeedbackRepository.STATUS_READ,
                                activeColor = HextechCyan,
                                modifier = Modifier.weight(1f),
                                onClick = { onSelectStatus(FeedbackRepository.STATUS_READ) }
                            )
                            StatusActionButton(
                                label = com.example.util.localizedString(com.example.R.string.support_close_conversation),
                                icon = Icons.Default.CheckCircle,
                                isSelected = currentStatus == FeedbackRepository.STATUS_SOLVED || currentStatus == FeedbackRepository.STATUS_COMPLETED,
                                activeColor = HextechGreen,
                                modifier = Modifier.weight(1.1f),
                                onClick = { onSelectStatus(FeedbackRepository.STATUS_SOLVED) }
                            )
                        }
                    } else {
                        // Opciones de Sugerencias / Builds: Pendiente | Aceptada | Rechazada
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StatusActionButton(
                                label = tr("Pendiente"),
                                icon = Icons.Default.HourglassEmpty,
                                isSelected = currentStatus == FeedbackRepository.STATUS_PENDING,
                                activeColor = Color(0xFFFFB300),
                                modifier = Modifier.weight(1f),
                                onClick = { onSelectStatus(FeedbackRepository.STATUS_PENDING) }
                            )
                            StatusActionButton(
                                label = tr("Aceptada"),
                                icon = Icons.Default.Check,
                                isSelected = currentStatus == FeedbackRepository.STATUS_ACCEPTED,
                                activeColor = HextechGold,
                                modifier = Modifier.weight(1f),
                                onClick = { onSelectStatus(FeedbackRepository.STATUS_ACCEPTED) }
                            )
                            StatusActionButton(
                                label = tr("Rechazada"),
                                icon = Icons.Default.Close,
                                isSelected = currentStatus == FeedbackRepository.STATUS_REJECTED,
                                activeColor = DangerRed,
                                modifier = Modifier.weight(1f),
                                onClick = { onSelectStatus(FeedbackRepository.STATUS_REJECTED) }
                            )
                        }
                    }
                }
            }

            // 💬 Sección de Respuesta de Soporte
            val reportKey = report.id ?: "${report.title}_${report.createdAt}"
            val localReply = remember(reportKey) { SupportReplyManager.getLocalReply(context, reportKey) }
            val finalReplyText = if (!report.adminReply.isNullOrBlank()) report.adminReply else (localReply?.text ?: "")

            if (finalReplyText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(HextechDarkBg)
                        .border(1.dp, HextechCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(13.dp))
                                Text(text = tr("Respuesta de Soporte Coach:"), color = HextechCyan, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            }

                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = com.example.util.tr(finalReplyText),
                            color = TextPrimary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        // Información del moderador que respondió
                        val authorName = report.repliedBy?.takeIf { it.isNotBlank() } ?: localReply?.author
                        val authorMail = report.repliedEmail?.takeIf { it.isNotBlank() } ?: localReply?.authorEmail
                        if (!authorName.isNullOrBlank() || !authorMail.isNullOrBlank()) {
                            val displayName = authorName ?: "Equipo Coach"
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(HextechSurfaceVariant.copy(alpha = 0.6f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = HextechGold, modifier = Modifier.size(10.dp))
                                Text(
                                    text = com.example.util.tr("${tr("Respondido por:")} $displayName${if (authorMail != null) " • $authorMail" else ""}"),
                                    color = HextechGold,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            if (canContinueConversation) {
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedButton(
                    onClick = { onReply?.invoke() },
                    modifier = Modifier
                        .testTag("support_continue_reply")
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    border = BorderStroke(0.8.dp, HextechCyan.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Reply, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(13.dp))
                        Text(text = tr("Responder Mensaje"), color = HextechCyan, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Diagnóstico y metadatos expandibles
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(140)),
                exit = fadeOut(tween(140))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(HextechDarkBg.copy(alpha = 0.9f))
                        .border(0.5.dp, HextechCardBorder, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val cleanDeviceInfo = remember(report.deviceInfo) {
                        cleanDeviceInfoText(report.deviceInfo)
                    }

                    if (parsedBuild != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(HextechSurfaceVariant.copy(alpha = 0.4f))
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tr("Texto crudo de la sugerencia:"),
                                color = HextechGold,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Build Cruda", cleanDescription))
                                    Toast.makeText(context, com.example.util.appTr(" Build copiada al portapapeles"), Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = com.example.util.trNullable("Copiar"), tint = HextechGold, modifier = Modifier.size(13.dp))
                            }
                        }
                        Text(
                            text = com.example.util.tr(cleanDescription),
                            color = TextSecondary,
                            fontSize = 10.5.sp,
                            lineHeight = 14.5.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .coachClickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Dispositivo", cleanDeviceInfo))
                                Toast.makeText(context, com.example.util.appTr(" Dispositivo copiado"), Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 2.dp, horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Smartphone, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(13.dp))
                        Text(
                            text = com.example.util.tr("${tr("Dispositivo:")} $cleanDeviceInfo"),
                            color = HextechCyan,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = HextechCyan.copy(alpha = 0.5f), modifier = Modifier.size(11.dp))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .coachClickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Versión", report.appVersion))
                                Toast.makeText(context, com.example.util.appTr(" Versión copiada"), Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 2.dp, horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = HextechGold, modifier = Modifier.size(13.dp))
                        Text(
                            text = com.example.util.tr("${tr("Versión:")} ${report.appVersion}"),
                            color = HextechGold,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = HextechGold.copy(alpha = 0.5f), modifier = Modifier.size(11.dp))
                    }

                    if (!report.parsedEmail.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .coachClickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Correo", report.parsedEmail))
                                    Toast.makeText(context, com.example.util.appTr("️ Correo copiado"), Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 2.dp, horizontal = 4.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF64B5F6), modifier = Modifier.size(13.dp))
                            Text(
                                text = com.example.util.tr("Correo: ${report.parsedEmail}"),
                                color = Color(0xFF64B5F6),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF64B5F6).copy(alpha = 0.5f), modifier = Modifier.size(11.dp))
                        }
                    }

                    if (!report.id.isNullOrBlank()) {
                        Text(
                            text = com.example.util.tr("UUID: ${report.id}"),
                            color = TextMuted,
                            fontSize = 9.5.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusActionButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isSelected) activeColor.copy(alpha = 0.25f)
                else HextechSurface
            )
            .border(
                width = if (isSelected) 1.2.dp else 0.6.dp,
                color = if (isSelected) activeColor else HextechCardBorder.copy(alpha = 0.5f),
                shape = RoundedCornerShape(6.dp)
            )
            .coachClickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) activeColor else TextMuted,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = com.example.util.tr(label),
                color = if (isSelected) activeColor else TextPrimary,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

private fun formatReportDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "Fecha no disponible"
    return try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val cleanDate = dateString.substringBefore(".").substringBefore("+").substringBefore("Z")
        val date = isoFormat.parse(cleanDate)
        if (date != null) {
            val localFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            localFormat.format(date)
        } else {
            dateString.take(16).replace("T", " ")
        }
    } catch (e: Exception) {
        dateString.take(16).replace("T", " ")
    }
}

private fun cleanDescriptionText(text: String): String {
    return text.substringBefore("[IMAGE_BASE64]").trim()
}

private fun cleanDeviceInfoText(text: String): String {
    return text.substringBefore("[IMAGE_BASE64]").trim()
}

private fun extractBase64Images(rawText: String): List<Bitmap> {
    val results = mutableListOf<Bitmap>()
    if (!rawText.contains("[IMAGE_BASE64]")) return results
    val parts = rawText.split("[IMAGE_BASE64]")
    for (i in 1 until parts.size) {
        val segment = parts[i].trim().substringBefore("\n\n").substringBefore("[IMAGE_BASE64]").trim()
        if (segment.isNotEmpty()) {
            try {
                val cleanBase64 = if (segment.contains(",")) segment.substringAfter(",") else segment
                val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bmp != null) {
                    results.add(bmp)
                }
            } catch (e: Exception) {
                // Ignore corrupted image
            }
        }
    }
    return results
}
