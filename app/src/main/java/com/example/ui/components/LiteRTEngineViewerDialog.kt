package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.screen.DraftVisionScanner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Champion
import com.example.service.screen.LiteRTVisionClassifier

@Composable
fun LiteRTEngineViewerDialog(
    onDismissRequest: () -> Unit,
    onSelectChampion: ((Champion) -> Unit)? = null
) {
    val report by LiteRTVisionClassifier.reportFlow.collectAsStateWithLifecycle()

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
                val showScanCircles by DraftVisionScanner.showCalibrationBoxes.collectAsStateWithLifecycle()

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
                                    if (onSelectChampion != null) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = { onSelectChampion(champ) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(30.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Seleccionar a ${champ.name}",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                } else {
                                    val topCand = report.topCandidates.firstOrNull()
                                    Text(
                                        text = when {
                                            topCand != null -> "Candidato #1: ${topCand.champion.name}"
                                            report.status == LiteRTVisionClassifier.EngineStatus.WAITING_FOR_PICKS_1_TO_9 -> "A la espera de picks 1 a 9"
                                            report.status == LiteRTVisionClassifier.EngineStatus.WAITING_FOR_TENTH_PICK -> "Slot final en espera"
                                            else -> "Evaluando tensores..."
                                        },
                                        color = if (topCand != null) Color.White else Color(0xFF94A3B8),
                                        fontWeight = if (topCand != null) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = when {
                                            topCand != null -> "Similitud tensor: ${(topCand.similarityScore * 100).toInt()}% • ${topCand.champion.primaryRole.displayName}"
                                            report.status == LiteRTVisionClassifier.EngineStatus.WAITING_FOR_TENTH_PICK -> {
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
                                    if (topCand != null && onSelectChampion != null) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = { onSelectChampion(topCand.champion) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(30.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Seleccionar a ${topCand.champion.name}",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Botón para ver u ocultar los círculos de escaneo en pantalla
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    DraftVisionScanner.showCalibrationBoxes.value = !showScanCircles
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (showScanCircles) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (showScanCircles) Color(0xFF00E5FF) else Color(0xFF334155)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (showScanCircles) Icons.Default.Check else Icons.Default.Close,
                                    contentDescription = if (showScanCircles) "Ocultar círculos de escaneo" else "Ver círculos de escaneo",
                                    tint = if (showScanCircles) Color(0xFF00E5FF) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (showScanCircles) "Círculos de Escaneo Activos [✓]" else "Mostrar Círculos de Escaneo [✕]",
                                    color = if (showScanCircles) Color(0xFF00E5FF) else Color(0xFFE2E8F0),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
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
                            onSelect = if (onSelectChampion != null) {
                                { onSelectChampion(candidate.champion) }
                            } else null
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
            }
        }
    }
}

@Composable
private fun CandidateRowItem(
    candidate: LiteRTVisionClassifier.LiteRTCandidateScore,
    onSelect: (() -> Unit)? = null
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

        Row(verticalAlignment = Alignment.CenterVertically) {
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
                        .width(56.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (isWinner) Color(0xFF00E5FF) else Color(0xFF64748B),
                    trackColor = Color(0xFF334155)
                )
            }

            if (onSelect != null) {
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = onSelect,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isWinner) Color(0xFF0284C7) else Color(0xFF334155),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = if (isWinner) "Elegir" else "Usar",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
