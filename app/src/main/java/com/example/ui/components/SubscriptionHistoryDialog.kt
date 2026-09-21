package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.SubscriptionRecord
import com.example.ui.theme.*
import com.example.util.SubscriptionHistoryManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SubscriptionHistoryDialog(
    userId: String? = null,
    userEmail: String? = null,
    onDismiss: () -> Unit
) {
    var history by remember { mutableStateOf<List<SubscriptionRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun loadHistory() {
        scope.launch {
            isLoading = true
            history = SubscriptionHistoryManager.getHistory(userId = userId, userEmail = userEmail)
            isLoading = false
        }
    }

    LaunchedEffect(userId, userEmail) {
        loadHistory()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.82f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
            border = BorderStroke(1.dp, HextechCardBorder)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HextechSurface)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = HextechCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Historial de Suscripciones",
                                color = HextechCyan,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (!userEmail.isNullOrBlank()) {
                                Text(
                                    text = userEmail,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HextechAnimatedIconButton(
                            onClick = { loadHistory() },
                            size = 32.dp,
                            backgroundColor = Color.Transparent,
                            borderColor = Color.Transparent,
                            glowColor = HextechCyan
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Actualizar",
                                tint = HextechCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        HextechAnimatedIconButton(
                            onClick = onDismiss,
                            size = 32.dp,
                            backgroundColor = Color.Transparent,
                            borderColor = Color.Transparent,
                            glowColor = HextechGold
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                HorizontalDivider(color = HextechCardBorder)

                // Sección de Esencia Azul dentro del Historial
                val currentBlueEssence by com.example.util.SubscriptionManager.blueEssence.collectAsState()
                var showBuyEssenceDialogInside by remember { mutableStateOf(false) }
                if (showBuyEssenceDialogInside) {
                    BuyEssenceDialog(
                        isAdmin = com.example.util.AuthManager.isCurrentUserAdmin(),
                        onDismiss = { showBuyEssenceDialogInside = false }
                    )
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clickable { showBuyEssenceDialogInside = true },
                    shape = RoundedCornerShape(12.dp),
                    color = HextechSurfaceVariant.copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = com.example.R.drawable.ic_blue_essence),
                                contentDescription = "Esencia Azul",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Esencia Azul Disponible",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "$currentBlueEssence EA",
                                    color = HextechGold,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = HextechGold,
                            contentColor = HextechDarkBg
                        ) {
                            Text(
                                text = "+ Recargar",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                HorizontalDivider(color = HextechCardBorder)

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = HextechGold, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Consultando registros en la nube...",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else if (history.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No hay historial de suscripciones",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tus registros de compras, pases temporales y membresías otorgadas aparecerán aquí.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            HextechAnimatedOutlinedButton(
                                onClick = { loadHistory() },
                                borderColor = HextechCyan,
                                glowColor = HextechCyan
                            ) {
                                Icon(Icons.Default.Refresh, tint = HextechCyan, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reintentar búsqueda", color = HextechCyan, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(history, key = { it.id }) { record ->
                            SubscriptionHistoryItem(record)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionHistoryItem(record: SubscriptionRecord) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val dateString = if (record.timestamp > 0L) dateFormat.format(Date(record.timestamp)) else "Reciente"

    val isRevocation = record.planName.contains("Revocación", ignoreCase = true) ||
            record.status.contains("Cancelado", ignoreCase = true) ||
            record.status.contains("Revocado", ignoreCase = true)

    val isGift = record.planName.contains("Regalo Admin", ignoreCase = true) ||
            record.planName.contains("Asignación Manual", ignoreCase = true) ||
            record.planName.contains("Admin", ignoreCase = true)

    val isLifetime = record.durationMillis == 0L && !isRevocation
    val expiryTimestamp = if (record.durationMillis > 0L) record.timestamp + record.durationMillis else 0L
    val isExpired = !isLifetime && !isRevocation && expiryTimestamp > 0L && expiryTimestamp < System.currentTimeMillis()
    val isActive = !isRevocation && (isLifetime || (!isExpired && expiryTimestamp > 0L))

    val statusColor = when {
        isRevocation -> com.example.ui.theme.DangerRed
        isExpired -> Color(0xFFFFB74D) // Amber/orange
        isActive -> Color(0xFF00FF7F) // Vivid Zaun/Emerald Green
        else -> TextSecondary
    }

    val displayStatus = when {
        isRevocation -> "Revocado / Cancelado"
        isExpired -> "Expirado / Finalizado"
        isLifetime -> "Activo (Vitalicio)"
        isActive -> "Vigente / Activo"
        else -> record.status.ifBlank { "Completado" }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(HextechSurfaceVariant)
            .border(1.dp, if (isActive) HextechGold.copy(alpha = 0.35f) else HextechCardBorder, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = record.planName.ifEmpty { "Suscripción Premium" },
                color = if (isActive) HextechGoldLight else TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (record.amount.isNotBlank()) record.amount else "$0.00",
                color = HextechGold,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))

        if (isRevocation) {
            Text(
                text = "Fecha de registro: $dateString",
                color = DangerRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        } else {
            val endDateStr = if (isLifetime) {
                "Para siempre (Vitalicio)"
            } else if (expiryTimestamp > 0L) {
                dateFormat.format(Date(expiryTimestamp))
            } else {
                "Permanente"
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Activado: $dateString",
                        color = TextSecondary,
                        fontSize = 11.5.sp
                    )
                    Text(
                        text = "Vence: $endDateStr",
                        color = if (isExpired) Color(0xFFFFB74D) else HextechGoldLight,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (isGift) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "🎁 Concesión Oficial de Administrador",
                        color = HextechCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = displayStatus,
                color = statusColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatDurationLocal(millis: Long): String {
    val days = millis / (1000L * 60 * 60 * 24)
    if (days >= 365) return "${days / 365} Año(s)"
    if (days > 0) return "$days Día(s)"
    return "Personalizado"
}
