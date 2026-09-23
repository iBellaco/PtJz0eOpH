package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.service.screen.DraftVisionScanner
import com.example.service.screen.LiteRTVisionClassifier
import com.example.service.screen.TenthPickDiagnosticManager

@Composable
fun LiteRTEngineViewerDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val report by LiteRTVisionClassifier.reportFlow.collectAsStateWithLifecycle()
    val isDiagnosticEnabled by TenthPickDiagnosticManager.isDiagnosticModeEnabled.collectAsStateWithLifecycle()
    val savedFrames by TenthPickDiagnosticManager.savedFramesFlow.collectAsStateWithLifecycle()
    val cacheStats by TenthPickDiagnosticManager.cacheStatsFlow.collectAsStateWithLifecycle()
    var inspectingFrame by remember { mutableStateOf<TenthPickDiagnosticManager.DiagnosticCropInfo?>(null) }

    LaunchedEffect(Unit) {
        TenthPickDiagnosticManager.init(context)
        TenthPickDiagnosticManager.refreshSavedFrames(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable { onDismissRequest() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = false) { /* Evitar cerrar al hacer click dentro */ }
                .padding(4.dp)
                .testTag("litert_viewer_dialog"),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0F172A),
            tonalElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Cabecera
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "LiteRT Motor",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Visor Google MediaPipe / LiteRT",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Motor de Inferencia del 10º Pick (On-Device)",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Estado del Motor
                val (badgeBg, badgeBorder, badgeText, statusIcon) = when (report.status) {
                    LiteRTVisionClassifier.EngineStatus.WAITING_FOR_PICKS_1_TO_9 -> Quadruple(
                        Color(0xFF422006),
                        Color(0xFFF59E0B),
                        "EN ESPERA: Requiere selecciones 1 al 9 (${report.evaluatedPicksCount}/9)",
                        Icons.Default.HourglassEmpty
                    )
                    LiteRTVisionClassifier.EngineStatus.WAITING_FOR_TENTH_PICK -> Quadruple(
                        Color(0xFF0C4A6E),
                        Color(0xFF38BDF8),
                        "SLOT FINAL EN ESPERA • APUNTANDO AL 10º PICK",
                        Icons.Default.HourglassEmpty
                    )
                    LiteRTVisionClassifier.EngineStatus.RUNNING_INFERENCE -> Quadruple(
                        Color(0xFF1E293B),
                        Color(0xFF38BDF8),
                        "PROCESANDO TENSORES DE IMAGEN",
                        Icons.Default.AutoAwesome
                    )
                    LiteRTVisionClassifier.EngineStatus.COMPLETED -> Quadruple(
                        Color(0xFF064E3B),
                        Color(0xFF10B981),
                        "INFERENCIA COMPLETADA • 10º PICK CONFIRMADO",
                        Icons.Default.CheckCircle
                    )
                    LiteRTVisionClassifier.EngineStatus.NO_DETECTION -> Quadruple(
                        Color(0xFF1E293B),
                        Color(0xFF64748B),
                        "EN ESPERA DE FRAME ACTIVO",
                        Icons.Default.Info
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .border(1.dp, badgeBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = badgeBorder,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = badgeText,
                            color = badgeBorder,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tarjeta de Decisión y Recorte Analizado
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "DECISIÓN DEL MOTOR MEDIAPIPE / LITERT",
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Vista del recorte real analizado
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Text(
                                    text = "Recorte Escaneado",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val cropBorderColor = if (report.slotDescription.contains("Aliado", ignoreCase = true)) {
                                    Color(0xFF00E5FF) // Azul para lado aliado
                                } else {
                                    Color(0xFFEF4444) // Rojo para lado rival
                                }

                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black)
                                        .border(2.dp, cropBorderColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (report.cropBitmap != null && !report.cropBitmap!!.isRecycled) {
                                        Image(
                                            bitmap = report.cropBitmap!!.asImageBitmap(),
                                            contentDescription = "Recorte 10º Pick",
                                            modifier = Modifier.size(60.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color(0xFF475569),
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }

                            // Datos del campeón decidido
                            Column(modifier = Modifier.weight(1f)) {
                                val champ = report.pickedChampion
                                if (champ != null) {
                                    Text(
                                        text = champ.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                    Text(
                                        text = "Rol: ${champ.primaryRole.displayName}",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Confianza: ${report.confidencePercent}%",
                                            color = Color(0xFF10B981),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "• ${report.inferenceTimeMs} ms",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                    }
                                } else {
                                    Text(
                                        text = when (report.status) {
                                            LiteRTVisionClassifier.EngineStatus.WAITING_FOR_PICKS_1_TO_9 -> "A la espera de picks 1 a 9"
                                            LiteRTVisionClassifier.EngineStatus.WAITING_FOR_TENTH_PICK -> "Slot final en espera"
                                            else -> "Evaluando tensores..."
                                        },
                                        color = Color(0xFF94A3B8),
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = when (report.status) {
                                            LiteRTVisionClassifier.EngineStatus.WAITING_FOR_TENTH_PICK -> {
                                                if (report.slotDescription.contains("Aliado", ignoreCase = true)) {
                                                    "Mostrando icono de línea. Esperando Avatar."
                                                } else {
                                                    "Mostrando yelmo espartano. Esperando Avatar."
                                                }
                                            }
                                            else -> report.slotDescription.ifBlank { "Slot 5 (10º Pick)" }
                                        },
                                        color = Color(0xFF64748B),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Métricas técnicas del Tensor y Control de Estabilidad Temporal
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tensor: ${report.tensorDimensions}",
                                    color = Color(0xFF64748B),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Umbral mín: ${(report.minConfidenceThreshold * 100).toInt()}%",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Estabilidad: ${report.stableFramesCount}/${report.requiredStableFrames} frames",
                                    color = if (report.isConfirmed) Color(0xFF10B981) else Color(0xFFF59E0B),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Espacio: RGB [-1.0, 1.0]",
                                    color = Color(0xFF64748B),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabla de Candidatos Comparados por LiteRT
                Text(
                    text = "COMPARACIÓN DE TENSORES (TOP 5 CANDIDATOS)",
                    color = Color(0xFFE2E8F0),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (report.topCandidates.isNotEmpty()) {
                    report.topCandidates.forEach { candidate ->
                        CandidateRowItem(
                            candidate = candidate,
                            onSelect = { selectedChamp ->
                                LiteRTVisionClassifier.manuallyConfirmTenthPick(selectedChamp)
                                val isAlly = report.slotDescription.contains("Aliado", ignoreCase = true)
                                if (isAlly) {
                                    DraftVisionScanner.allySlotConfirmedChampions[4] = selectedChamp
                                } else {
                                    DraftVisionScanner.enemySlotConfirmedChampions[4] = selectedChamp
                                }
                                Toast.makeText(context, "10º Pick fijado: ${selectedChamp.name}", Toast.LENGTH_SHORT).show()
                            }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Los candidatos comparados aparecerán aquí cuando se procese el frame del 10º pick.",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Motivo y Registro de Decisión
                Text(
                    text = "MOTIVO DE LA DECISIÓN",
                    color = Color(0xFFE2E8F0),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = report.decisionReason,
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECCIÓN: MODO DIAGNÓSTICO DE RECORTES DEL 10º PICK
                DiagnosticModeSection(
                    isEnabled = isDiagnosticEnabled,
                    onToggle = { TenthPickDiagnosticManager.setDiagnosticMode(context, it) },
                    cacheStats = cacheStats,
                    savedFrames = savedFrames,
                    onInspectFrame = { inspectingFrame = it },
                    onClearCache = { TenthPickDiagnosticManager.clearAllCaches(context) },
                    onRefresh = { TenthPickDiagnosticManager.refreshSavedFrames(context) },
                    onCopyPath = { path ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Ruta de Diagnóstico 10º Pick", path)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Ruta copiada al portapapeles", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    if (inspectingFrame != null) {
        DiagnosticFrameInspectorDialog(
            frame = inspectingFrame!!,
            onDismiss = { inspectingFrame = null },
            onShare = { TenthPickDiagnosticManager.shareFrame(context, inspectingFrame!!.file) },
            onDelete = {
                TenthPickDiagnosticManager.deleteFrame(context, inspectingFrame!!.fileName)
                inspectingFrame = null
            }
        )
    }
}

@Composable
private fun DiagnosticModeSection(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    cacheStats: TenthPickDiagnosticManager.DiagnosticCacheStats,
    savedFrames: List<TenthPickDiagnosticManager.DiagnosticCropInfo>,
    onInspectFrame: (TenthPickDiagnosticManager.DiagnosticCropInfo) -> Unit,
    onClearCache: () -> Unit,
    onRefresh: () -> Unit,
    onCopyPath: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0D1527))
            .border(1.dp, Color(0xFF1E3A5F), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // Encabezado con switch de activación
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "MODO DIAGNÓSTICO (RECORTES)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (isEnabled) "Guardando recortes en caché para inspección" else "Modo diagnóstico inactivo",
                        color = if (isEnabled) Color(0xFF10B981) else Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF00E5FF),
                    uncheckedThumbColor = Color(0xFF64748B),
                    uncheckedTrackColor = Color(0xFF1E293B)
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Guarda automáticamente los recortes del 10º pick en el directorio de caché de la app para que puedas inspeccionar manualmente la calidad de imagen, píxeles, brillo y contraste que reciben los motores de reconocimiento.",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            lineHeight = 15.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tarjeta con la ruta de caché del dispositivo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E293B))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = Color(0xFFC6A15B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Directorio en Caché:",
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (cacheStats.directoryPath.isNotEmpty()) {
                        IconButton(
                            onClick = { onCopyPath(cacheStats.directoryPath) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar ruta",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (cacheStats.directoryPath.isNotEmpty()) cacheStats.directoryPath else ".../cache/diagnostic_10th_pick",
                    color = Color(0xFF38BDF8),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${cacheStats.totalFiles} recortes guardados • ${cacheStats.formattedTotalSize}",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = onRefresh,
                            modifier = Modifier.height(28.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            border = BorderStroke(1.dp, Color(0xFF475569)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCBD5E1))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refrescar",
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Refrescar", fontSize = 10.sp)
                        }

                        if (cacheStats.totalFiles > 0) {
                            OutlinedButton(
                                onClick = onClearCache,
                                modifier = Modifier.height(28.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF87171))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Limpiar",
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Limpiar", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Galería de recortes guardados
        Text(
            text = "RECORTES CAPTURADOS (TOCA PARA INSPECCIONAR)",
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (savedFrames.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                    .border(1.dp, Color(0xFF334155).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no se han guardado recortes en el directorio de caché.\nCuando el escáner apunte al 10º pick, se guardarán aquí automáticamente.",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 15.sp
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                savedFrames.forEach { frame ->
                    SavedCropThumbnailCard(
                        frame = frame,
                        onClick = { onInspectFrame(frame) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedCropThumbnailCard(
    frame: TenthPickDiagnosticManager.DiagnosticCropInfo,
    onClick: () -> Unit
) {
    val borderColor = if (frame.isAlly) Color(0xFF00E5FF) else Color(0xFFEF4444)
    val teamLabel = if (frame.isAlly) "Aliado" else "Rival"

    Column(
        modifier = Modifier
            .width(84.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E293B))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black)
                .border(1.5.dp, borderColor, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = frame.file,
                contentDescription = frame.fileName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = teamLabel,
            color = borderColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = frame.formattedTime,
            color = Color(0xFFCBD5E1),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )

        Text(
            text = frame.dimensionsText,
            color = Color(0xFF94A3B8),
            fontSize = 8.sp
        )
    }
}

@Composable
private fun DiagnosticFrameInspectorDialog(
    frame: TenthPickDiagnosticManager.DiagnosticCropInfo,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val borderColor = if (frame.isAlly) Color(0xFF00E5FF) else Color(0xFFEF4444)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clickable(enabled = false) {}
                .padding(8.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0F172A),
            tonalElevation = 10.dp,
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Barra de título
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Inspección de Calidad Óptica",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Recorte de imagen aumentado para inspección manual precisa
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                        .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = frame.file,
                        contentDescription = "Inspección de calidad del frame",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabla de datos ópticos y de reconocimiento
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MetricRow(label = "Archivo:", value = frame.fileName, isMono = true)
                    MetricRow(label = "Resolución:", value = "${frame.width} x ${frame.height} px (Fidelidad 100%)")
                    MetricRow(label = "Tamaño en Disco:", value = frame.formattedSize)
                    MetricRow(label = "Hora Captura:", value = frame.formattedTime)
                    MetricRow(
                        label = "Lado / Slot:",
                        value = if (frame.isAlly) "Aliado (Slot ${frame.slotIndex + 1})" else "Rival (Slot ${frame.slotIndex + 1})",
                        valueColor = borderColor
                    )
                    MetricRow(label = "Etapa del Escáner:", value = frame.stage)
                    if (frame.candidateName != null) {
                        MetricRow(
                            label = "Candidato Decidido:",
                            value = "${frame.candidateName} (${frame.confidence ?: 0}%)",
                            valueColor = Color(0xFF10B981)
                        )
                    }
                    if (frame.avgBrightness > 0f) {
                        MetricRow(
                            label = "Brillo Promedio:",
                            value = "${frame.avgBrightness.toInt()} / 255"
                        )
                    }
                    if (frame.maxBrightness > 0) {
                        MetricRow(
                            label = "Píxel más Brillante:",
                            value = "${frame.maxBrightness} / 255"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Acciones: Compartir PNG, Eliminar, Cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onShare,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5FF),
                            contentColor = Color(0xFF0F172A)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Compartir PNG", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF87171)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFFCBD5E1),
    isMono: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default,
            maxLines = 1
        )
    }
}

@Composable
private fun CandidateRowItem(
    candidate: LiteRTVisionClassifier.LiteRTCandidateScore,
    onSelect: ((com.example.model.Champion) -> Unit)? = null
) {
    val isWinner = candidate.rank == 1
    val borderColor = if (isWinner) Color(0xFF00E5FF) else Color(0xFF334155)
    val bgColor = if (isWinner) Color(0xFF1E293B) else Color(0xFF0F172A)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(enabled = onSelect != null) {
                onSelect?.invoke(candidate.champion)
            }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Text(
                text = "#${candidate.rank}",
                color = if (isWinner) Color(0xFF00E5FF) else Color(0xFF64748B),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.width(24.dp)
            )

            ChampionAvatar(
                champion = candidate.champion,
                size = 32.dp,
                modifier = Modifier.clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = candidate.champion.name,
                    color = Color.White,
                    fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                )
                Text(
                    text = "Probabilidad Softmax: ${(candidate.softmaxProbability * 100).toInt()}%",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${(candidate.similarityScore * 100).toInt()}% Tensor",
                color = if (isWinner) Color(0xFF10B981) else Color(0xFFCBD5E1),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            LinearProgressIndicator(
                progress = { candidate.similarityScore.coerceIn(0f, 1f) },
                modifier = Modifier
                    .width(64.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (isWinner) Color(0xFF00E5FF) else Color(0xFF64748B),
                trackColor = Color(0xFF334155)
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
