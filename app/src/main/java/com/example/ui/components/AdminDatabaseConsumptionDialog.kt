package com.example.ui.components

import android.widget.Toast
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
import com.example.data.CloudDatabaseCleaner
import com.example.data.sync.FirebaseAssetSyncManager
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AdminDatabaseConsumptionDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val (syncedCount, syncedBytes) = remember { FirebaseAssetSyncManager.getSyncedStats(context) }
    val syncedMb = syncedBytes / (1024.0 * 1024.0)

    val firebaseCapacityMb = 1000.0 // 1 GB Cuota Gratuita
    val baseDataConsumedMb = 4.25
    val firebaseConsumedMb = baseDataConsumedMb + syncedMb
    val firebaseRemainingMb = (firebaseCapacityMb - firebaseConsumedMb).coerceAtLeast(0.0)
    val firebasePercentage = (firebaseConsumedMb / firebaseCapacityMb).toFloat().coerceIn(0f, 1f)

    var isPurging by remember { mutableStateOf(false) }
    var purgeStatusMsg by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = {
            if (!isPurging) onDismiss()
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = HextechGold, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Consumo de Base de Datos", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Almacenamiento Cloud en Tiempo Real", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                    if (!isPurging) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                        }
                    }
                }

                Divider(color = HextechSurfaceVariant)

                // Firebase Card
                CloudServiceConsumptionCard(
                    title = "Almacenamiento Cloud",
                    color = HextechGold,
                    capacityMb = firebaseCapacityMb,
                    consumedMb = firebaseConsumedMb,
                    remainingMb = firebaseRemainingMb,
                    percentage = firebasePercentage,
                    description = if (syncedCount > 0) {
                        "Perfiles, reportes, avisos y $syncedCount imágenes del juego sincronizadas (${String.format(java.util.Locale.US, "%.2f MB", syncedMb)})."
                    } else {
                        "Perfiles, reportes, sugerencias, avisos y sincronización en tiempo real."
                    }
                )

                // Botón y panel de purga de datos residuales
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                    border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = DangerRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Limpieza y Purga de Datos Residuales", color = DangerRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text(
                            "Elimina notificaciones globales huérfanas, mensajes de prueba y tickets eliminados de la nube.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )

                        if (purgeStatusMsg != null) {
                            Text(
                                text = purgeStatusMsg!!,
                                color = HextechCyan,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    isPurging = true
                                    purgeStatusMsg = "Purgando notificaciones y bandejas en la nube..."
                                    val result = CloudDatabaseCleaner.purgeAllResidualCloudData(context)
                                    isPurging = false
                                    if (result.success) {
                                        purgeStatusMsg = "Limpieza exitosa: ${result.globalNotificationsDeleted} notificaciones y ${result.userInboxesCleaned} bandejas purgadas."
                                        Toast.makeText(context, "Base de datos purgada con éxito.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        purgeStatusMsg = "Error en la purga: ${result.errorMessage}"
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.85f)),
                            enabled = !isPurging,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isPurging) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Limpiando registros...", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.CleaningServices, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Purgar Datos Residuales de la Nube", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isPurging
                ) {
                    Text("Cerrar Panel", color = HextechDarkBg, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CloudServiceConsumptionCard(
    title: String,
    color: Color,
    capacityMb: Double,
    consumedMb: Double,
    remainingMb: Double,
    percentage: Float,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = "${String.format(java.util.Locale.US, "%.1f", percentage * 100)}% Usado",
                    color = if (percentage > 0.85f) Color(0xFFE57373) else HextechCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            LinearProgressIndicator(
                progress = { percentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = HextechDarkBg,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Consumido", color = TextSecondary, fontSize = 11.sp)
                    Text(String.format(java.util.Locale.US, "%.2f MB", consumedMb), color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Disponible", color = TextSecondary, fontSize = 11.sp)
                    Text(String.format(java.util.Locale.US, "%.2f MB", remainingMb), color = HextechCyan, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Capacidad Total", color = TextSecondary, fontSize = 11.sp)
                    Text(String.format(java.util.Locale.US, "%.0f MB", capacityMb), color = HextechGoldLight, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }

            Text(
                text = description,
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
    }
}
