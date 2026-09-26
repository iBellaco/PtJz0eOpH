package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.sync.FirebaseAssetSyncManager
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AdminAssetSyncDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val syncState by FirebaseAssetSyncManager.syncProgress.collectAsState()

    var totalGameAssets by remember { mutableStateOf(0) }
    var localUserAssetsCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        val gameAssets = FirebaseAssetSyncManager.getGameAssetsToUpload(context)
        totalGameAssets = gameAssets.size
        localUserAssetsCount = FirebaseAssetSyncManager.getUserLocalFileNames().size
    }

    Dialog(
        onDismissRequest = {
            if (!syncState.isRunning) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(16.dp),
            color = HextechDarkBg,
            border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = HextechGold,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Sincronizador de Recursos",
                                color = HextechGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                "Carga de imágenes del juego a la nube",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    if (!syncState.isRunning) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                        }
                    }
                }

                Divider(color = HextechSurfaceVariant)

                // Info Cards
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                    border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Imágenes del juego a sincronizar:", color = TextSecondary, fontSize = 12.sp)
                            Text("$totalGameAssets archivos", color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Text(
                            "Incluye: Campeones, habilidades, objetos, runas y hechizos.",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                    border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Recursos locales (Excluidos):", color = TextSecondary, fontSize = 12.sp)
                            Text("$localUserAssetsCount archivos", color = HextechGoldLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Text(
                            "Avatares del panel de usuario y marcos de rango se mantienen 100% locales.",
                            color = HextechGold.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }

                // Status & Progress Section
                if (syncState.isRunning || syncState.isFinished) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (syncState.isRunning) "Sincronizando..." else "Estado",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            if (syncState.totalFiles > 0) {
                                Text(
                                    text = "${syncState.processedFiles} / ${syncState.totalFiles}",
                                    color = HextechCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        val progressRatio = if (syncState.totalFiles > 0) {
                            (syncState.processedFiles.toFloat() / syncState.totalFiles.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = HextechCyan,
                            trackColor = HextechDarkBg
                        )

                        Text(
                            text = syncState.statusMessage,
                            color = if (syncState.errorCount > 0 && !syncState.isRunning) Color(0xFFE57373) else TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!syncState.isRunning) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f))
                        ) {
                            Text("Cerrar", color = HextechGold)
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                FirebaseAssetSyncManager.startSync(context)
                            }
                        },
                        modifier = if (syncState.isRunning) Modifier.fillMaxWidth() else Modifier.weight(1.5f),
                        enabled = !syncState.isRunning,
                        colors = ButtonDefaults.buttonColors(containerColor = HextechCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (syncState.isRunning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = HextechDarkBg,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Subiendo imágenes...", color = HextechDarkBg, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = HextechDarkBg)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (syncState.isFinished) "Volver a Sincronizar" else "Iniciar Carga a la Nube",
                                color = HextechDarkBg,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
