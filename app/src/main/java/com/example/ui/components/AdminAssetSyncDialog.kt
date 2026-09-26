package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.sync.FirebaseAssetSyncManager
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAssetSyncDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val syncState by FirebaseAssetSyncManager.syncProgress.collectAsState()

    var totalGameAssets by remember { mutableStateOf(0) }
    var localUserAssetsCount by remember { mutableStateOf(0) }
    var customBucket by remember { mutableStateOf(FirebaseAssetSyncManager.getCustomBucket(context)) }
    var testResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var showRulesHint by remember { mutableStateOf(false) }

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
                .fillMaxHeight(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = HextechDarkBg,
            border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                fontSize = 17.sp
                            )
                            Text(
                                "Almacenamiento Cloud de imágenes del juego",
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

                // Resumen de imágenes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechSurface),
                        border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Para la Nube", color = TextSecondary, fontSize = 11.sp)
                            Text("$totalGameAssets archivos", color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Objetos, runas, campeones...", color = TextMuted, fontSize = 10.sp, maxLines = 1)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechSurface),
                        border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Locales (Excluidos)", color = TextSecondary, fontSize = 11.sp)
                            Text("$localUserAssetsCount archivos", color = HextechGoldLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Avatares y marcos de perfil", color = TextMuted, fontSize = 10.sp, maxLines = 1)
                        }
                    }
                }

                // Diagnóstico y prueba de conexión
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                    border = BorderStroke(1.dp, HextechSurfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Diagnóstico de Almacenamiento",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        isTestingConnection = true
                                        testResult = FirebaseAssetSyncManager.testConnection(context)
                                        isTestingConnection = false
                                    }
                                },
                                enabled = !isTestingConnection && !syncState.isRunning,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.5f))
                            ) {
                                if (isTestingConnection) {
                                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = HextechCyan)
                                } else {
                                    Text("Probar Conexión", fontSize = 11.sp, color = HextechCyan)
                                }
                            }
                        }

                        testResult?.let { (success, msg) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (success) Color(0xFF1B5E20).copy(alpha = 0.3f) else Color(0xFFB71C1C).copy(alpha = 0.3f))
                                    .border(1.dp, if (success) Color(0xFF4CAF50) else Color(0xFFE57373), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = msg,
                                    color = if (success) Color(0xFF81C784) else Color(0xFFFF8A80),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Error detallado si ocurrió
                if (syncState.lastError != null || (syncState.isFinished && syncState.errorCount > 0)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1515)),
                        border = BorderStroke(1.dp, Color(0xFFE57373))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF8A80), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Motivo del Fallo", color = Color(0xFFFF8A80), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Text(
                                text = syncState.lastError ?: "Error de permisos en el servicio de almacenamiento.",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Comprueba que las reglas del servicio de almacenamiento en la nube permitan la escritura pública.",
                                color = HextechGoldLight,
                                fontSize = 10.5.sp
                            )
                            Button(
                                onClick = { showRulesHint = !showRulesHint },
                                colors = ButtonDefaults.buttonColors(containerColor = HextechGold.copy(alpha = 0.2f)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(if (showRulesHint) "Ocultar Reglas de Seguridad" else "Ver Reglas de Seguridad", fontSize = 11.sp, color = HextechGold)
                            }
                        }
                    }
                }

                if (showRulesHint) {
                    val rulesCode = "rules_version = '2';\nservice firebase.storage {\n  match /b/{bucket}/o {\n    match /{allPaths=**} {\n      allow read, write: if true;\n    }\n  }\n}"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                        border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Reglas de Almacenamiento en la Nube", color = HextechCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                IconButton(
                                    onClick = { clipboardManager.setText(AnnotatedString(rulesCode)) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = HextechCyan, modifier = Modifier.size(14.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = rulesCode,
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Progreso
                if (syncState.isRunning || syncState.isFinished) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                fontSize = 12.5.sp
                            )
                            if (syncState.totalFiles > 0) {
                                Text(
                                    text = "${syncState.processedFiles} / ${syncState.totalFiles}",
                                    color = if (syncState.errorCount > 0 && !syncState.isRunning) Color(0xFFE57373) else HextechCyan,
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
                            color = if (syncState.errorCount > 0 && !syncState.isRunning) Color(0xFFE57373) else HextechCyan,
                            trackColor = HextechSurface
                        )

                        Text(
                            text = syncState.statusMessage,
                            color = if (syncState.errorCount > 0 && !syncState.isRunning) Color(0xFFE57373) else TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

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
                            FirebaseAssetSyncManager.setCustomBucket(context, customBucket)
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
