package com.example.ui.components

import com.example.ui.components.CoachFilterChip as FilterChip

import com.example.ui.components.CoachButton as Button
import com.example.ui.components.CoachTextButton as TextButton

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.theme.*
import com.example.util.*
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

@Composable
fun OrangeEssenceRedemptionDialog(onDismiss: () -> Unit) {
    val uid = AuthManager.getAuth()?.currentUser?.uid ?: return
    val role by SubscriptionManager.userRole.collectAsState()
    val secondary by SubscriptionManager.secondaryRole.collectAsState()
    val adminClaim by AuthManager.isAdminClaim.collectAsState()
    if (!com.example.model.RolePanelAccess.canRedeemEssence(role, secondary, adminClaim)) return
    val orange by SubscriptionManager.orangeEssence.collectAsState()
    var requests by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var loadError by remember { mutableStateOf(false) }
    var wallet by remember { mutableStateOf("") }
    var network by remember { mutableStateOf(UsdtNetwork.TRC20) }
    var amount by remember { mutableStateOf<Long?>(null) }
    var id by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    DisposableEffect(uid) {
        val listener = EssenceEconomyRepository.redemptions.whereEqualTo("userId", uid).addSnapshotListener(MetadataChanges.INCLUDE) { snap, error ->
            loadError = error != null
            if (snap != null && error == null) requests = snap.documents.mapNotNull { it.data }.sortedByDescending { (it["requestedAtMillis"] as? Number)?.toLong() ?: 0L }
        }
        onDispose { listener.remove() }
    }
    if (amount == null) {
        Dialog(onDismissRequest = { if (!busy) onDismiss() }) {
            Surface(color = HextechDarkBg, shape = MaterialTheme.shapes.large) {
                Column(Modifier.fillMaxWidth().heightIn(max = 650.dp).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(tr("Canjear Esencia Naranja"), style = MaterialTheme.typography.titleLarge, color = HextechGold)
                    Text(tr("Saldo: $orange EN"), color = HextechCyan)
                    Text(tr("El pago es manual y demora de 24 a 72 horas. El equipo coordinará el pago contigo desde la bandeja de entrada."), color = TextSecondary)
                    Text(tr("Pago exclusivamente en USDT (la comisión de red corre por cuenta del usuario en EN)."), color = HextechGold)
                    UsdtWalletFields(network, wallet, !busy, onNetwork = { network = it }, onWallet = { wallet = it })
                    if (orange > 0) CashRedemptionOptions(orange, network, !busy && UsdtWalletPolicy.valid(network, wallet.trim())) { selected -> amount = selected; id = java.util.UUID.randomUUID().toString(); feedback = null }
                    feedback?.let { Text(tr(it), color = HextechCyan) }
                    Text(tr("Historial"), color = HextechGold)
                    if (loadError) Text(tr("No se pudo cargar el historial. Vuelve a intentarlo."), color = DangerRed)
                    requests.forEach { request ->
                        val date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, if (currentAppLanguage() == "pt") java.util.Locale("pt", "BR") else java.util.Locale("es"))
                            .format(Date((request["requestedAtMillis"] as? Number)?.toLong() ?: 0L))
                        val reqAmount = request["amount"] ?: 0
                        val reqTotal = request["totalDeducted"] ?: reqAmount
                        Text(tr("$date • $reqTotal EN (${request["network"]}) → $reqAmount USDT"), color = TextPrimary)
                        Text(tr(when (request["status"]) { "PAID" -> "Pagado"; "REJECTED" -> "Rechazado y reembolsado"; else -> "Pendiente" }), color = HextechCyan)
                    }
                    TextButton(onClick = onDismiss, enabled = !busy) { Text(tr("Cerrar")) }
                }
            }
        }
    }
    amount?.let { selected ->
        CashRedemptionConfirmation(selected,network,wallet,busy,feedback,onConfirm = {
            busy = true; feedback = null
            scope.launch {
                val result = EssenceEconomyRepository.redeem(id, selected, network, wallet.trim())
                busy = false
                if (result.isSuccess) { amount = null; feedback = "Solicitud de canje registrada" }
                else feedback = economyFailure(result.exceptionOrNull())
            }
        },onDismiss = { amount = null; feedback = null })
    }
}

@Composable
fun CashRedemptionOptions(balance: Long, network: UsdtNetwork = UsdtNetwork.TRC20, enabled: Boolean = true, onChoose: (Long) -> Unit) {
    if (balance <= 0) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(tr("Opciones de canje (comisión: ${network.feeEn} EN):"), color = HextechCyan, fontSize = 12.sp)
        EssenceEconomyPolicy.redemptionAmounts.forEach { amount ->
            val totalNeeded = amount + network.feeEn
            val canAfford = balance >= totalNeeded
            Button(onClick = { onChoose(amount) }, enabled = enabled && canAfford,
                modifier = Modifier.fillMaxWidth().testTag("cash_redemption_$amount")) {
                Text(tr("$amount USDT (Total: $totalNeeded EN)"))
            }
        }
    }
}

@Composable
fun CashRedemptionReviewPanel() {
    var requests by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var historyRequests by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var retry by remember { mutableIntStateOf(0) }
    var selectedTab by remember { mutableStateOf("PENDING") }
    val sevenDaysAgo = remember { System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000L }

    DisposableEffect(retry) {
        val pendingListener = EssenceEconomyRepository.redemptions.whereEqualTo("status", "PENDING").addSnapshotListener { snapshot, failure ->
            if (failure != null) error = "No se pudieron cargar las solicitudes"
            else { error = null; requests = snapshot?.documents.orEmpty().mapNotNull { it.data?.plus("id" to it.id) } }
        }
        val historyListener = EssenceEconomyRepository.redemptions.addSnapshotListener { snapshot, failure ->
            if (snapshot != null && failure == null) {
                historyRequests = snapshot.documents.mapNotNull { it.data?.plus("id" to it.id) }
                    .filter { doc ->
                        val status = doc["status"] as? String ?: ""
                        val reqAt = (doc["requestedAtMillis"] as? Number)?.toLong() ?: 0L
                        val resAt = (doc["resolvedAtMillis"] as? Number)?.toLong() ?: reqAt
                        status in setOf("PAID", "REJECTED") && (reqAt >= sevenDaysAgo || resAt >= sevenDaysAgo)
                    }
                    .sortedByDescending { (it["resolvedAtMillis"] as? Number)?.toLong() ?: (it["requestedAtMillis"] as? Number)?.toLong() ?: 0L }
            }
        }
        onDispose {
            pendingListener.remove()
            historyListener.remove()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(tr("Solicitudes de canje"), color = HextechGold)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedTab == "PENDING",
                onClick = { selectedTab = "PENDING" },
                label = { Text(tr("Pendientes (${requests.size})")) }
            )
            FilterChip(
                selected = selectedTab == "HISTORY",
                onClick = { selectedTab = "HISTORY" },
                label = { Text(tr("Historial (7 días) (${historyRequests.size})")) }
            )
        }

        error?.let {
            Text(tr(it), color = DangerRed)
            TextButton(onClick = { error = null; retry++ }) { Text(tr("Reintentar")) }
        }

        if (selectedTab == "PENDING") {
            if (requests.isEmpty()) {
                Text(tr("No hay solicitudes pendientes de pago."), color = TextSecondary, fontSize = 12.sp)
            }
            requests.forEach { request ->
                val reqAmount = request["amount"] ?: 0
                val reqFee = request["fee"] ?: 0
                val reqTotal = request["totalDeducted"] ?: reqAmount
                var decision by remember(request["id"]) { mutableStateOf<Boolean?>(null) }
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    color = HextechSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(tr("${request["email"]} • $reqAmount USDT (Descontado: $reqTotal EN)"), color = TextPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Text(tr("Red: ${request["network"]} • Comisión: $reqFee EN"), color = HextechCyan, fontSize = 12.sp)
                        Text(request["wallet"] as? String ?: "", color = TextSecondary, fontSize = 11.sp)
                        var dm by remember(request["id"]) { mutableStateOf(false) }
                        if (dm) SupportReplyDialog(reportId = "payment_${request["id"]}", userName = request["email"] as? String ?: "Usuario",
                            reportTitle = "Solicitud de pago USDT", reportDescription = "", initialReply = "",
                            onDismiss = { dm = false }, onReplySent = { _, _ -> dm = false }, userEmail = request["email"] as? String ?: "",
                            userId = request["userId"] as? String ?: "", tag = "PAGO", isFirestoreDoc = true)
                        TextButton(onClick = { dm = true }) { Text(tr("Enviar DM privado")) }
                        Row {
                            TextButton(enabled = !busy, onClick = { decision = true }) { Text(tr("Marcar pagado"), color = Color(0xFF10B981)) }
                            TextButton(enabled = !busy, onClick = { decision = false }) { Text(tr("Rechazar y devolver esencias"), color = DangerRed) }
                        }
                        decision?.let { paid ->
                            AlertDialog(
                                onDismissRequest = { if (!busy) decision = null },
                                title = { Text(tr("Confirmar")) },
                                text = { Text(tr(if (paid) "Confirma únicamente después de realizar el pago manual." else "Se devolverán las esencias ($reqTotal EN) al usuario.")) },
                                confirmButton = {
                                    TextButton(enabled = !busy, onClick = {
                                        busy = true
                                        scope.launch {
                                            val result = EssenceEconomyRepository.resolve(request["id"] as String, paid)
                                            error = if (result.isFailure) "No se pudo completar la operación. Comprueba tu conexión y vuelve a intentarlo." else null
                                            busy = false; decision = null
                                        }
                                    }) { Text(tr("Confirmar")) }
                                },
                                dismissButton = { TextButton(enabled = !busy, onClick = { decision = null }) { Text(tr("Cancelar")) } }
                            )
                        }
                    }
                }
            }
        } else {
            if (historyRequests.isEmpty()) {
                Text(tr("No hay solicitudes procesadas en los últimos 7 días."), color = TextSecondary, fontSize = 12.sp)
            }
            historyRequests.forEach { request ->
                val reqAmount = request["amount"] ?: 0
                val reqFee = request["fee"] ?: 0
                val reqTotal = request["totalDeducted"] ?: reqAmount
                val status = (request["status"] as? String)?.uppercase() ?: ""
                val isPaid = status == "PAID"
                val timestamp = (request["resolvedAtMillis"] as? Number)?.toLong() ?: (request["requestedAtMillis"] as? Number)?.toLong() ?: 0L
                val dateStr = if (timestamp > 0) java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(timestamp)) else "Reciente"
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    color = HextechSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isPaid) Color(0xFF10B981).copy(alpha = 0.5f) else DangerRed.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text(
                                text = tr(if (isPaid) "PAGADO" else "RECHAZADO Y REEMBOLSADO"),
                                color = if (isPaid) Color(0xFF10B981) else DangerRed,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(dateStr, color = TextSecondary, fontSize = 10.5.sp)
                        }
                        Text(tr("${request["email"]} • $reqAmount USDT (Total: $reqTotal EN)"), color = TextPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                        Text(tr("Red: ${request["network"]} • Comisión de red: $reqFee EN"), color = HextechCyan, fontSize = 11.5.sp)
                        Text(tr("Billetera: ") + (request["wallet"] as? String ?: ""), color = TextSecondary, fontSize = 11.sp)
                        var dm by remember(request["id"]) { mutableStateOf(false) }
                        if (dm) SupportReplyDialog(reportId = "payment_${request["id"]}", userName = request["email"] as? String ?: "Usuario",
                            reportTitle = "Solicitud de pago USDT", reportDescription = "", initialReply = "",
                            onDismiss = { dm = false }, onReplySent = { _, _ -> dm = false }, userEmail = request["email"] as? String ?: "",
                            userId = request["userId"] as? String ?: "", tag = "PAGO", isFirestoreDoc = true)
                        TextButton(onClick = { dm = true }) { Text(tr("Enviar DM privado"), fontSize = 11.sp) }
                    }
                }
            }
        }
    }
}

@Composable
fun UsdtWalletFields(network: UsdtNetwork, wallet: String, enabled: Boolean,
    onNetwork: (UsdtNetwork) -> Unit, onWallet: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(tr("Red de USDT"), color = TextSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            UsdtNetwork.entries.forEach { option ->
                FilterChip(selected = network == option, onClick = { onNetwork(option) }, enabled = enabled, label = { Text("${option.name} (${option.feeEn} EN)") })
            }
        }
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
            color = HextechSurfaceVariant.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, HextechGold.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = tr("Comisión de red (${network.name}): ${network.feeEn} EN a cargo del usuario (se descuenta en Esencia Naranja)."),
                color = HextechGoldLight,
                fontSize = 11.5.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
        OutlinedTextField(wallet, onWallet, enabled = enabled, singleLine = true,
            label = { Text(tr("Tu billetera digital USDT")) }, modifier = Modifier.fillMaxWidth().testTag("usdt_wallet"),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
        Text(tr("Comprueba que la dirección corresponde a la red seleccionada. No envíes claves privadas ni frases de recuperación."), color = TextSecondary, fontSize = 11.sp)
        if (wallet.isNotBlank() && !UsdtWalletPolicy.valid(network, wallet.trim())) Text(tr("Billetera USDT no válida"), color = DangerRed, fontSize = 11.5.sp)
    }
}

@Composable
fun CashRedemptionConfirmation(amount: Long, network: UsdtNetwork, wallet: String, busy: Boolean,
    feedback: String? = null, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val fee = network.feeEn
    val totalDeducted = amount + fee
    AlertDialog(onDismissRequest = { if (!busy) onDismiss() }, title = { Text(tr("Confirmar canje")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(tr("Monto a recibir: $amount USDT"), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = HextechGold)
                Text(tr("Red seleccionada: ${network.name}"))
                Text(tr("Comisión de red: $fee EN (a cargo del usuario)"))
                Text(tr("Total a descontar: $totalDeducted EN"), fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, color = HextechCyan)
                Text(wallet, fontSize = 11.sp, color = TextSecondary)
                Text(tr("El pago es manual y demora de 24 a 72 horas."), fontSize = 11.sp, color = TextSecondary)
                feedback?.let { Text(tr(it), color = DangerRed) }
            }
        },
        confirmButton = { TextButton(enabled = !busy, modifier = Modifier.testTag("cash_redemption_confirm"), onClick = onConfirm) { Text(tr(if (busy) "Procesando…" else "Confirmar")) } },
        dismissButton = { TextButton(enabled = !busy, modifier = Modifier.testTag("cash_redemption_cancel"), onClick = onDismiss) { Text(tr("Cancelar")) } })
}
