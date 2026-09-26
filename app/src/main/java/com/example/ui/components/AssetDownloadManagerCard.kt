package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.download.AssetDownloadStatus
import com.example.data.download.GameAssetDownloadManager
import com.example.ui.theme.*

@Composable
fun AssetDownloadManagerCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val progressState by GameAssetDownloadManager.downloadProgress.collectAsState()
    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        GameAssetDownloadManager.refreshProgress(context)
    }

    val isCompleted = progressState.status == AssetDownloadStatus.COMPLETED
    val isDownloading = progressState.status == AssetDownloadStatus.DOWNLOADING
    val isPaused = progressState.status == AssetDownloadStatus.PAUSED

    val cardBorderColor = when {
        isCompleted -> Color(0xFF10B981).copy(alpha = 0.5f)
        isDownloading -> HextechCyan.copy(alpha = 0.6f)
        isPaused -> HextechGold.copy(alpha = 0.5f)
        else -> HextechCardBorder.copy(alpha = 0.5f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(
            containerColor = HextechSurface.copy(alpha = 0.85f)
        ),
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Título y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted -> Color(0xFF10B981).copy(alpha = 0.2f)
                                    isDownloading -> HextechCyan.copy(alpha = 0.2f)
                                    isPaused -> HextechGold.copy(alpha = 0.2f)
                                    else -> HextechSurfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isCompleted -> Icons.Default.CheckCircle
                                isDownloading -> Icons.Default.Download
                                isPaused -> Icons.Default.PauseCircle
                                else -> Icons.Default.CloudDownload
                            },
                            contentDescription = null,
                            tint = when {
                                isCompleted -> Color(0xFF10B981)
                                isDownloading -> HextechCyan
                                isPaused -> HextechGold
                                else -> TextSecondary
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Recursos del Juego",
                            color = HextechGoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                        Text(
                            text = if (isCompleted) "Habilidades, campeones y objetos listos"
                                   else "Habilidades, campeones y objetos en la nube",
                            color = TextMuted,
                            fontSize = 10.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Badge de Estado
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when {
                        isCompleted -> Color(0xFF10B981).copy(alpha = 0.15f)
                        isDownloading -> HextechCyan.copy(alpha = 0.15f)
                        isPaused -> HextechGold.copy(alpha = 0.15f)
                        else -> HextechSurfaceVariant.copy(alpha = 0.5f)
                    },
                    border = BorderStroke(
                        1.dp,
                        when {
                            isCompleted -> Color(0xFF10B981).copy(alpha = 0.4f)
                            isDownloading -> HextechCyan.copy(alpha = 0.4f)
                            isPaused -> HextechGold.copy(alpha = 0.4f)
                            else -> TextMuted.copy(alpha = 0.2f)
                        }
                    )
                ) {
                    Text(
                        text = when {
                            isCompleted -> "Completado"
                            isDownloading -> "${(progressState.progressPercent * 100).toInt()}%"
                            isPaused -> "Pausado"
                            else -> "Pendiente"
                        },
                        color = when {
                            isCompleted -> Color(0xFF10B981)
                            isDownloading -> HextechCyan
                            isPaused -> HextechGold
                            else -> TextSecondary
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Barra de Progreso
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progressState.progressPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        isCompleted -> Color(0xFF10B981)
                        isDownloading -> HextechCyan
                        isPaused -> HextechGold
                        else -> HextechGoldLight.copy(alpha = 0.5f)
                    },
                    trackColor = HextechDarkBg
                )

                // Métricas de Peso y Cantidad
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val downloadedStr = GameAssetDownloadManager.formatBytesToMb(progressState.downloadedBytes)
                    val totalStr = GameAssetDownloadManager.formatBytesToMb(progressState.totalBytes)
                    val remainingStr = GameAssetDownloadManager.formatBytesToMb(progressState.remainingBytes)

                    Text(
                        text = if (isCompleted) "Descargado: $totalStr ($downloadedStr)"
                               else "Descargado: $downloadedStr de $totalStr",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = if (isCompleted) "${progressState.totalFiles} archivos"
                               else "Faltan: $remainingStr (${(progressState.totalFiles - progressState.downloadedFiles).coerceAtLeast(0)})",
                        color = if (isCompleted) Color(0xFF10B981) else HextechCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Archivo en descarga actual
            if (isDownloading && progressState.currentAssetName.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(HextechDarkBg.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.5.dp,
                        color = HextechCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = progressState.currentAssetName,
                        color = TextPrimary.copy(alpha = 0.9f),
                        fontSize = 10.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Botones de Control (Descargar / Pausar / Reanudar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when {
                    isDownloading -> {
                        // Botón Pausar
                        Button(
                            onClick = { GameAssetDownloadManager.pauseDownload() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null, tint = HextechDarkBg, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pausar Descarga", color = HextechDarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    isPaused -> {
                        // Botón Reanudar
                        Button(
                            onClick = { GameAssetDownloadManager.startOrResumeDownload(context) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = HextechCyan),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = HextechDarkBg, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reanudar Descarga", color = HextechDarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    isCompleted -> {
                        // Botón Verificado / Re-descargar
                        OutlinedButton(
                            onClick = { GameAssetDownloadManager.startOrResumeDownload(context) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Verificar Paquete Completo", color = Color(0xFF10B981), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    else -> {
                        // Botón Descargar Todo
                        val totalStr = GameAssetDownloadManager.formatBytesToMb(progressState.totalBytes)
                        Button(
                            onClick = { GameAssetDownloadManager.startOrResumeDownload(context) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = HextechCyan),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = HextechDarkBg, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Descargar Recursos ($totalStr)", color = HextechDarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Botón Detalles
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(HextechSurfaceVariant)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Detalles",
                        tint = HextechGoldLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Desplegable de Detalles de Recursos
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(HextechDarkBg.copy(alpha = 0.7f))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Contenido del Paquete de Recursos:",
                        color = HextechGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• Habilidades e Íconos de Campeones", color = TextSecondary, fontSize = 10.5.sp)
                        Text("Descarga en la Nube", color = HextechCyan, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• Objetos, Runas y Hechizos", color = TextSecondary, fontSize = 10.5.sp)
                        Text("Descarga en la Nube", color = HextechCyan, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• Avatares y Marcos de Perfil", color = TextSecondary, fontSize = 10.5.sp)
                        Text("100% Local (Excluido)", color = HextechGoldLight, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
