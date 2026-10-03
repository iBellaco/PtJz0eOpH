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
import com.example.util.tr
import kotlinx.coroutines.launch
import kotlinx.coroutines.ensureActive
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

    var loadJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    fun loadHistory() {
        loadJob?.cancel()
        loadJob = scope.launch {
            isLoading = true
            val records = SubscriptionHistoryManager.getHistory(userId = userId, userEmail = userEmail)
            coroutineContext.ensureActive()
            history = records
            isLoading = false
        }
    }

    val targetUid = userId ?: com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
    DisposableEffect(targetUid) {
        var previousHistory: List<Any?>? = null
        val listener = targetUid?.let { uid -> com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users").document(uid).addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val relevant = listOf(snapshot.get("subscriptionHistory"), snapshot.get("subscriptionPlan"),
                        snapshot.get("lastModifiedByAdmin"), (snapshot.get("privateMessages") as? List<*>)
                            .orEmpty().filterIsInstance<Map<String, Any>>().mapNotNull(com.example.data.AccountHistoryPolicy::notificationReceipt))
                    if (previousHistory != relevant) { previousHistory = relevant; loadHistory() }
                }
            } }
        val recordsListener = targetUid?.let { uid -> com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users").document(uid).collection("subscription_history").addSnapshotListener { _, error -> if (error == null) loadHistory() } }
        onDispose { listener?.remove(); recordsListener?.remove() }
    }
    LaunchedEffect(userId, userEmail) { loadHistory() }

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
                PanelReadControl(com.example.data.NotificationPanel.HISTORY)
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
                                text = com.example.util.tr("Historial"),
                                color = HextechCyan,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (!userEmail.isNullOrBlank()) {
                                Text(
                                    text = com.example.util.tr(userEmail),
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
                                contentDescription = com.example.util.trNullable("Actualizar"),
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
                                contentDescription = com.example.util.trNullable("Cerrar"),
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                HorizontalDivider(color = HextechCardBorder)

                // Sección de Esencias (Azul y Naranja) y Botón de Recargar dentro del Historial
                val currentBlueEssence by com.example.util.SubscriptionManager.blueEssence.collectAsState()
                val currentOrangeEssence by com.example.util.SubscriptionManager.orangeEssence.collectAsState()
                var buyEssenceCurrency by remember { mutableStateOf("BLUE") }
                var showBuyEssenceDialogInside by remember { mutableStateOf(false) }

                if (showBuyEssenceDialogInside) {
                    BuyEssenceDialog(
                        isAdmin = com.example.util.AuthManager.isCurrentUserAdmin(),
                        initialCurrency = buyEssenceCurrency,
                        onDismiss = { showBuyEssenceDialogInside = false }
                    )
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = HextechSurfaceVariant.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Esencias badges (Azul y Naranja)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            // Chip Esencia Azul
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        buyEssenceCurrency = "BLUE"
                                        showBuyEssenceDialogInside = true
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = HextechDarkBg.copy(alpha = 0.7f),
                                border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = com.example.R.drawable.ic_blue_essence),
                                        contentDescription = com.example.util.tr("Esencia Azul"),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = com.example.util.tr("Esencia Azul"),
                                            color = TextSecondary,
                                            fontSize = 9.5.sp,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = com.example.util.tr("$currentBlueEssence EA"),
                                            color = HextechCyan,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            // Chip Esencia Naranja
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        buyEssenceCurrency = "ORANGE"
                                        showBuyEssenceDialogInside = true
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = HextechDarkBg.copy(alpha = 0.7f),
                                border = BorderStroke(1.dp, Color(0xFFFF8C00).copy(alpha = 0.45f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = com.example.R.drawable.ic_orange_essence),
                                        contentDescription = com.example.util.tr("Esencia Naranja"),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = com.example.util.tr("Esencia Naranja"),
                                            color = TextSecondary,
                                            fontSize = 9.5.sp,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = com.example.util.tr("$currentOrangeEssence EN"),
                                            color = Color(0xFFFF9E1B),
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Botón de Recargar
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    showBuyEssenceDialogInside = true
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = HextechGold,
                            contentColor = HextechDarkBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = com.example.util.tr("+ Recargar"),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(color = HextechCardBorder)

                // Filtros de Historial: Todos, Esencias, Suscripciones
                var selectedFilter by remember { mutableStateOf("ALL") }
                val essenceCount = remember(history) { history.count { it.isEssenceTransaction } }
                val subCount = remember(history) { history.count { !it.isEssenceTransaction } }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Chip Todos
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedFilter = "ALL" },
                        shape = RoundedCornerShape(16.dp),
                        color = if (selectedFilter == "ALL") HextechCyan.copy(alpha = 0.2f) else HextechDarkBg,
                        border = BorderStroke(1.dp, if (selectedFilter == "ALL") HextechCyan else HextechCardBorder)
                    ) {
                        Text(
                            text = com.example.util.tr("Todos (${history.size})"),
                            color = if (selectedFilter == "ALL") HextechCyan else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (selectedFilter == "ALL") FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    // Chip Esencias
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedFilter = "ESSENCE" },
                        shape = RoundedCornerShape(16.dp),
                        color = if (selectedFilter == "ESSENCE") Color(0xFFFF9E1B).copy(alpha = 0.2f) else HextechDarkBg,
                        border = BorderStroke(1.dp, if (selectedFilter == "ESSENCE") Color(0xFFFF9E1B) else HextechCardBorder)
                    ) {
                        Text(
                            text = com.example.util.tr("Esencias ($essenceCount)"),
                            color = if (selectedFilter == "ESSENCE") Color(0xFFFF9E1B) else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (selectedFilter == "ESSENCE") FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    // Chip Membresías
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedFilter = "SUBS" },
                        shape = RoundedCornerShape(16.dp),
                        color = if (selectedFilter == "SUBS") HextechGold.copy(alpha = 0.2f) else HextechDarkBg,
                        border = BorderStroke(1.dp, if (selectedFilter == "SUBS") HextechGold else HextechCardBorder)
                    ) {
                        Text(
                            text = com.example.util.tr("Suscripciones ($subCount)"),
                            color = if (selectedFilter == "SUBS") HextechGoldLight else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (selectedFilter == "SUBS") FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                HorizontalDivider(color = HextechCardBorder)

                val displayedHistory = remember(history, selectedFilter) {
                    when (selectedFilter) {
                        "ESSENCE" -> history.filter { it.isEssenceTransaction }
                        "SUBS" -> history.filter { !it.isEssenceTransaction }
                        else -> history
                    }
                }

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
                                text = com.example.util.tr("Consultando registros en la nube..."),
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else if (displayedHistory.isEmpty()) {
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
                                text = com.example.util.tr(if (selectedFilter == "ESSENCE") "Sin movimientos de esencias" else if (selectedFilter == "SUBS") "Sin registros de membresías" else "No hay historial disponible"),
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = com.example.util.tr("Tus registros de consumo, recargas, suscripciones y asignaciones aparecerán aquí."),
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
                                Text(tr("Reintentar búsqueda"), color = HextechCyan, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(displayedHistory, key = { it.id }) { record ->
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

    if (record.isEssenceTransaction) {
        // === RENDERIZADO DE MOVIMIENTO DE ESENCIAS (AZUL / NARANJA - AÑADIDO / DESCONTADO) ===
        val isOrange = record.isOrangeEssence
        val isDeduction = record.isDeduction
        val actionColor = if (isDeduction) Color(0xFFFF5252) else Color(0xFF00FF7F)
        val actionBg = actionColor.copy(alpha = 0.12f)
        val actionBorder = actionColor.copy(alpha = 0.45f)
        val actionTag = if (isDeduction) "Descontado (-)" else "Añadido (+)"

        val formattedAmount = remember(record.amount, isDeduction) {
            val trimmed = record.amount.trim()
            when {
                trimmed.startsWith("+") || trimmed.startsWith("-") -> trimmed
                isDeduction -> "-$trimmed"
                else -> "+$trimmed"
            }
        }

        val originTag = when {
            record.isFromAdmin -> "Por Administrador"
            record.isFromSubscription -> "Por Suscripción"
            record.status.contains("Recarga", ignoreCase = true) || record.planName.contains("Recarga", ignoreCase = true) -> "Recarga Oficial"
            else -> "Transacción de Esencia"
        }

        val originColor = when {
            record.isFromAdmin -> HextechCyan
            record.isFromSubscription -> HextechGold
            else -> Color(0xFF00FF7F)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(HextechSurfaceVariant)
                .border(
                    BorderStroke(1.dp, actionBorder.copy(alpha = 0.35f)),
                    RoundedCornerShape(10.dp)
                )
                .padding(12.dp)
        ) {
            // Fila superior: Ícono de esencia + Título de operación + Monto con indicador
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(
                            id = if (isOrange) com.example.R.drawable.ic_orange_essence else com.example.R.drawable.ic_blue_essence
                        ),
                        contentDescription = com.example.util.tr(if (isOrange) "Esencia Naranja" else "Esencia Azul"),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = com.example.util.tr(record.planName.ifEmpty { if (isOrange) "Movimiento de Esencia Naranja" else "Movimiento de Esencia Azul" }),
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        // Chip de origen (Admin / Suscripción / Recarga)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = originColor.copy(alpha = 0.12f),
                            border = BorderStroke(0.5.dp, originColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = com.example.util.tr(originTag),
                                color = originColor,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Columna derecha: Monto destacado y estado
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = com.example.util.tr(formattedAmount),
                        color = actionColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = actionBg,
                        border = BorderStroke(0.5.dp, actionBorder)
                    ) {
                        Text(
                            text = com.example.util.tr(actionTag),
                            color = actionColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = HextechCardBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(6.dp))

            // Fila inferior: Fecha de registro y detalle del estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = com.example.util.tr("Fecha: $dateString"),
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = com.example.util.tr(record.status.ifBlank { if (isDeduction) "Descontado" else "Añadido" }),
                    color = actionColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    } else {
        // === RENDERIZADO DE SUSCRIPCIÓN TRADICIONAL / MEMBRESÍA VIP ===
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
                    text = com.example.util.tr(record.planName.ifEmpty { "Suscripción Premium" }),
                    color = if (isActive) HextechGoldLight else TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = com.example.util.tr(if (record.amount.isNotBlank()) record.amount else "$0.00"),
                    color = HextechGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            if (isRevocation) {
                Text(
                    text = com.example.util.tr("Fecha de registro: $dateString"),
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
                            text = com.example.util.tr("Activado: $dateString"),
                            color = TextSecondary,
                            fontSize = 11.5.sp
                        )
                        Text(
                            text = com.example.util.tr("Vence: $endDateStr"),
                            color = if (isExpired) Color(0xFFFFB74D) else HextechGoldLight,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (isGift) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = com.example.util.tr("Concesión Oficial de Administrador"),
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
                    text = com.example.util.tr(displayStatus),
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun formatDurationLocal(millis: Long): String {
    val days = millis / (1000L * 60 * 60 * 24)
    if (days >= 365) return "${days / 365} Año(s)"
    if (days > 0) return "$days Día(s)"
    return "Personalizado"
}
