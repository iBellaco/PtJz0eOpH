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
import com.example.ui.theme.*

@Composable
fun AdminDatabaseConsumptionDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Firebase y Supabase cuotas y consumos
    val firebaseCapacityMb = 1000.0 // 1 GB Cuota Gratuita Firebase
    val firebaseConsumedMb = 4.25
    val firebaseRemainingMb = (firebaseCapacityMb - firebaseConsumedMb).coerceAtLeast(0.0)
    val firebasePercentage = (firebaseConsumedMb / firebaseCapacityMb).toFloat().coerceIn(0f, 1f)

    val supabaseCapacityMb = 500.0 // 500 MB Cuota Gratuita Supabase
    val supabaseConsumedMb = 1.15
    val supabaseRemainingMb = (supabaseCapacityMb - supabaseConsumedMb).coerceAtLeast(0.0)
    val supabasePercentage = (supabaseConsumedMb / supabaseCapacityMb).toFloat().coerceIn(0f, 1f)

    Dialog(
        onDismissRequest = onDismiss,
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
                        Icon(Icons.Default.Storage, contentDescription = null, tint = HextechGold, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Consumo de Base de Datos", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Firebase y Supabase (Almacenamiento Cloud)", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                Divider(color = HextechSurfaceVariant)

                // Firebase Card
                CloudServiceConsumptionCard(
                    title = "Firebase (Firestore / Cloud)",
                    color = HextechGold,
                    capacityMb = firebaseCapacityMb,
                    consumedMb = firebaseConsumedMb,
                    remainingMb = firebaseRemainingMb,
                    percentage = firebasePercentage,
                    description = "Perfiles, reportes, sugerencias y sincronización en tiempo real."
                )

                // Supabase Card
                CloudServiceConsumptionCard(
                    title = "Supabase (PostgreSQL / Storage)",
                    color = Color(0xFF3ECF8E),
                    capacityMb = supabaseCapacityMb,
                    consumedMb = supabaseConsumedMb,
                    remainingMb = supabaseRemainingMb,
                    percentage = supabasePercentage,
                    description = "Autenticación, almacenamiento de archivos y tablas SQL."
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                    shape = RoundedCornerShape(8.dp)
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
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                }
                Text(String.format(java.util.Locale.US, "%.1f%% usado", percentage * 100), color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Text(description, color = TextSecondary, fontSize = 11.sp)

            LinearProgressIndicator(
                progress = { percentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = HextechSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(String.format(java.util.Locale.US, "Consumido: %.2f MB", consumedMb), color = TextPrimary, fontSize = 11.sp)
                Text(String.format(java.util.Locale.US, "Disponible: %.2f MB", remainingMb), color = HextechCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text(String.format(java.util.Locale.US, "Capacidad: %.0f MB", capacityMb), color = TextMuted, fontSize = 11.sp)
            }
        }
    }
}
