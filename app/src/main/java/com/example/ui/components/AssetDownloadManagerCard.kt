package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

    LaunchedEffect(Unit) {
        GameAssetDownloadManager.refreshProgress(context)
    }

    val isCompleted = progressState.status == AssetDownloadStatus.COMPLETED ||
            (progressState.totalFiles > 0 && progressState.downloadedFiles >= progressState.totalFiles)
    val isDownloading = progressState.status == AssetDownloadStatus.DOWNLOADING
    val isPaused = progressState.status == AssetDownloadStatus.PAUSED

    // Al completarse todos los recursos, el gestor se oculta automáticamente
    AnimatedVisibility(
        visible = !isCompleted,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        val cardBorderColor = when {
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
                // Header: Título y Porcentaje
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
                                        isDownloading -> HextechCyan.copy(alpha = 0.2f)
                                        isPaused -> HextechGold.copy(alpha = 0.2f)
                                        else -> HextechSurfaceVariant
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    isDownloading -> Icons.Default.Download
                                    isPaused -> Icons.Default.PauseCircle
                                    else -> Icons.Default.CloudDownload
                                },
                                contentDescription = null,
                                tint = when {
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
                                text = "Paquete de Recursos del Juego",
                                color = HextechGoldLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                            Text(
                                text = "Campeones, habilidades, hechizos, runas y objetos",
                                color = TextMuted,
                                fontSize = 10.5.sp
                            )
                        }
                    }

                    // Badge de Porcentaje / Estado
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when {
                            isDownloading -> HextechCyan.copy(alpha = 0.15f)
                            isPaused -> HextechGold.copy(alpha = 0.15f)
                            else -> HextechSurfaceVariant.copy(alpha = 0.5f)
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                isDownloading -> HextechCyan.copy(alpha = 0.4f)
                                isPaused -> HextechGold.copy(alpha = 0.4f)
                                else -> TextMuted.copy(alpha = 0.2f)
                            }
                        )
                    ) {
                        Text(
                            text = when {
                                isDownloading -> "${(progressState.progressPercent * 100).toInt()}%"
                                isPaused -> "Pausado"
                                else -> "Pendiente"
                            },
                            color = when {
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
                            isDownloading -> HextechCyan
                            isPaused -> HextechGold
                            else -> HextechGoldLight.copy(alpha = 0.5f)
                        },
                        trackColor = HextechDarkBg
                    )

                    // Cantidad descargada y faltante
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val downloadedStr = GameAssetDownloadManager.formatBytesToMb(progressState.downloadedBytes)
                        val totalStr = GameAssetDownloadManager.formatBytesToMb(progressState.totalBytes)
                        val remainingStr = GameAssetDownloadManager.formatBytesToMb(progressState.remainingBytes)
                        val missingCount = (progressState.totalFiles - progressState.downloadedFiles).coerceAtLeast(0)

                        Text(
                            text = "Descargado: $downloadedStr de $totalStr",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "Faltan: $remainingStr ($missingCount)",
                            color = HextechCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Botón de Acción (Descargar / Pausar / Reanudar)
                when {
                    isDownloading -> {
                        Button(
                            onClick = { GameAssetDownloadManager.pauseDownload() },
                            modifier = Modifier.fillMaxWidth(),
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
                        Button(
                            onClick = { GameAssetDownloadManager.startOrResumeDownload(context) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = HextechCyan),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = HextechDarkBg, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reanudar Descarga", color = HextechDarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    else -> {
                        val totalStr = GameAssetDownloadManager.formatBytesToMb(progressState.totalBytes)
                        Button(
                            onClick = { GameAssetDownloadManager.startOrResumeDownload(context) },
                            modifier = Modifier.fillMaxWidth(),
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
            }
        }
    }
}
