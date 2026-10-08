package com.example.ui.components

import com.example.ui.components.CoachFilterChip as FilterChip

import com.example.ui.components.CoachButton as Button
import com.example.ui.components.CoachTextButton as TextButton

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import android.widget.Toast
import com.example.data.*
import com.example.ui.theme.*
import com.example.util.*
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
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
    if (orange <= 0L) return
    var requests by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var loadError by remember { mutableStateOf(false) }
    var wallet by remember { mutableStateOf("") }
    var network by remember { mutableStateOf<UsdtNetwork?>(null) }
    var binanceEmail by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf<Long?>(null) }
    var id by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        BinanceCommissionManager.init()
    }
    val liveFees by BinanceCommissionManager.liveFees.collectAsState()
    val normalizedBinanceEmail = binanceEmail.trim().lowercase(java.util.Locale.ROOT)
    val usingBinanceEmail = normalizedBinanceEmail.isNotBlank()
    val binanceEmailValid = usingBinanceEmail && UsdtWalletPolicy.validBinanceEmail(normalizedBinanceEmail)
    val activeFee = if (usingBinanceEmail) 0L else network?.let { liveFees[it] ?: it.feeEn } ?: 0L

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
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                        color = DangerRed.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.8f)),
                        modifier = Modifier.fillMaxWidth().testTag("manual_payment_warning_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Warning,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = tr("El pago es manual y demora de 24 a 72 horas. El equipo coordinará el pago contigo desde la bandeja de entrada."),
                                color = DangerRed,
                                fontSize = 12.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    Text(tr("Pago exclusivamente en USDT. Puedes usar correo de Binance o billetera por red; la comisión de red solo aplica a billeteras."), color = HextechGold)
                    UsdtWalletFields(
                        network = network,
                        wallet = wallet,
                        enabled = !busy,
                        onNetwork = { network = it },
                        onWallet = { wallet = it },
                        fee = activeFee,
                        binanceEmail = binanceEmail,
                        onBinanceEmail = { value ->
                            binanceEmail = value
                            if (value.isNotBlank()) {
                                network = null
                                wallet = ""
                            }
                        }
                    )
                    if (orange > 0) {
                        val isWalletValid = !usingBinanceEmail && network != null && UsdtWalletPolicy.valid(network!!, wallet.trim())
                        val destinationReady = binanceEmailValid || isWalletValid
                        if (network != null && !usingBinanceEmail) {
                            Surface(
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                color = HextechSurfaceVariant.copy(alpha = 0.7f),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, HextechGold.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = tr("Aviso de confirmación de envío para cobro"),
                                        color = HextechGold,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = tr("Al seleccionar un monto se abrirá la confirmación final del cobro. Revisa que tu billetera en Binance coincida con la red seleccionada. El retiro se gestiona manualmente de 24 a 72 horas."),
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                        CashRedemptionOptions(
                            balance = orange,
                            network = network ?: UsdtNetwork.TRC20,
                            fee = activeFee,
                            enabled = !busy && destinationReady,
                            hasSelectedNetwork = destinationReady
                        ) { selected ->
                            amount = selected
                            id = java.util.UUID.randomUUID().toString()
                            feedback = null
                        }
                    }
                    feedback?.let { Text(tr(it), color = HextechCyan) }
                    Text(tr("Historial"), color = HextechGold)
                    if (loadError) Text(tr("No se pudo cargar el historial. Vuelve a intentarlo."), color = DangerRed)
                    requests.forEach { request ->
                        val date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, if (currentAppLanguage() == "pt") java.util.Locale("pt", "BR") else java.util.Locale("es"))
                            .format(Date((request["requestedAtMillis"] as? Number)?.toLong() ?: 0L))
                        val reqAmount = request["amount"] ?: 0
                        val reqTotal = request["totalDeducted"] ?: reqAmount
                        val payoutEmail = (request["binanceEmail"] as? String).orEmpty()
                        val destination = if (payoutEmail.isNotBlank()) tr("Correo Binance") + ": " + payoutEmail else (request["network"] as? String).orEmpty()
                        Text(tr("$date • $reqTotal EN → $reqAmount USDT") + " • " + destination, color = TextPrimary)
                        Text(tr(when (request["status"]) { "PAID" -> "Pagado"; "REJECTED" -> "Rechazado y reembolsado"; else -> "Pendiente" }), color = HextechCyan)
                    }
                    TextButton(onClick = onDismiss, enabled = !busy) { Text(tr("Cerrar")) }
                }
            }
        }
    }
    amount?.let { selected ->
        val emailForPayment = normalizedBinanceEmail.takeIf { binanceEmailValid }.orEmpty()
        val networkForPayment = if (emailForPayment.isNotBlank()) null else network
        CashRedemptionConfirmation(
            amount = selected,
            network = networkForPayment,
            wallet = if (emailForPayment.isNotBlank()) "" else wallet.trim(),
            busy = busy,
            feedback = feedback,
            onConfirm = {
                busy = true; feedback = null
                scope.launch {
                    val result = EssenceEconomyRepository.redeem(id, selected, networkForPayment, wallet.trim(), emailForPayment)
                    busy = false
                    if (result.isSuccess) { amount = null; feedback = "Solicitud de canje registrada" }
                    else feedback = result.exceptionOrNull()?.message?.takeIf { it.isNotBlank() && !it.startsWith("java.") } ?: economyFailure(result.exceptionOrNull())
                }
            },
            onDismiss = { amount = null; feedback = null },
            fee = activeFee,
            binanceEmail = emailForPayment
        )
    }
}

@Composable
fun CashRedemptionOptions(
    balance: Long,
    network: UsdtNetwork = UsdtNetwork.TRC20,
    fee: Long = network.feeEn,
    enabled: Boolean = true,
    hasSelectedNetwork: Boolean = true,
    onChoose: (Long) -> Unit
) {
    if (balance <= 0) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (hasSelectedNetwork) {
            Text(tr("Opciones de canje (comisión: $fee EN):"), color = HextechCyan, fontSize = 12.sp)
        } else {
            Text(tr("Ingresa un correo de Binance o selecciona una red para ver las opciones de canje"), color = TextSecondary, fontSize = 12.sp)
        }
        EssenceEconomyPolicy.redemptionAmounts.forEach { amount ->
            val totalNeeded = amount + fee
            val canAfford = balance >= totalNeeded
            Button(
                onClick = { onChoose(amount) },
                enabled = enabled && canAfford && hasSelectedNetwork,
                modifier = Modifier.fillMaxWidth().testTag("cash_redemption_$amount")
            ) {
                Text(tr("$amount USDT (Total: $totalNeeded EN)"))
            }
        }
    }
}

@Composable
fun CashRedemptionReviewPanel() {
    val adminClaim by AuthManager.isAdminClaim.collectAsState()
    if (!adminClaim) return
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var requests by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var historyRequests by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var retry by remember { mutableIntStateOf(0) }
    var selectedTab by remember { mutableStateOf("PENDING") }
    var selectedUserFilter by remember { mutableStateOf<String?>(null) }
    var resolvedUserNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val fourteenDaysAgo = remember { System.currentTimeMillis() - EssenceEconomyRepository.CASH_HISTORY_RETENTION_MILLIS }

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
                        status in setOf("PAID", "REJECTED") && (reqAt >= fourteenDaysAgo || resAt >= fourteenDaysAgo)
                    }
                    .sortedByDescending { (it["resolvedAtMillis"] as? Number)?.toLong() ?: (it["requestedAtMillis"] as? Number)?.toLong() ?: 0L }
            }
        }
        onDispose {
            pendingListener.remove()
            historyListener.remove()
        }
    }

    LaunchedEffect(historyRequests.mapNotNull { it["userId"] as? String }.distinct()) {
        val ids = historyRequests.mapNotNull { (it["userId"] as? String)?.takeIf(String::isNotBlank) }.distinct()
        if (ids.isEmpty()) {
            resolvedUserNames = emptyMap()
        } else {
            val db = FirebaseFirestore.getInstance()
            resolvedUserNames = ids.associateWith { uid ->
                runCatching {
                    val profile = db.collection("users").document(uid).get().await()
                    profile.getString("name")?.takeIf { it.isNotBlank() }
                        ?: profile.getString("userName")?.takeIf { it.isNotBlank() }
                        ?: "Usuario"
                }.getOrDefault("Usuario")
            }
        }
    }

    fun paymentUserName(request: Map<String, Any>): String {
        val uid = (request["userId"] as? String).orEmpty()
        val resolved = resolvedUserNames[uid].orEmpty()
        val stored = (request["userName"] as? String).orEmpty()
        return resolved.takeIf { it.isNotBlank() && !it.equals("Usuario", ignoreCase = true) }
            ?: stored.takeIf { it.isNotBlank() && !it.equals("Usuario", ignoreCase = true) }
            ?: "Usuario"
    }

    val historyGroupedByUser = remember(historyRequests, resolvedUserNames) {
        historyRequests.groupBy(::paymentUserName)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(tr(if (selectedTab == "HISTORY") "Historial de pago" else "Solicitudes de canje"), color = HextechGold)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedTab == "PENDING",
                onClick = { selectedTab = "PENDING" },
                label = { Text(tr("Pendientes (${requests.size})")) }
            )
            FilterChip(
                selected = selectedTab == "HISTORY",
                onClick = { selectedTab = "HISTORY" },
                label = { Text(tr("Historial de pago")) }
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
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val requestUserName = paymentUserName(request)
                        Text(tr("$requestUserName • $reqAmount USDT (Descontado: $reqTotal EN)"), color = TextPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        val payoutEmail = (request["binanceEmail"] as? String).orEmpty()
                        if (payoutEmail.isNotBlank()) {
                            Text(tr("Sin comisión de red"), color = HextechCyan, fontSize = 12.sp)
                            Row(
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().background(Color(0xFF0F172A), androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(tr("Correo Binance") + ": " + payoutEmail, color = HextechGoldLight, fontSize = 11.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, modifier = Modifier.weight(1f))
                                IconButton(onClick = {
                                    clipboardManager.setText(AnnotatedString(payoutEmail))
                                    Toast.makeText(context, com.example.util.appTr("Correo de Binance copiado al portapapeles"), Toast.LENGTH_SHORT).show()
                                }, modifier = Modifier.size(28.dp).testTag("copy_binance_email_btn_${request["id"]}")) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = tr("Copiar correo Binance"), tint = HextechGold, modifier = Modifier.size(16.dp))
                                }
                            }
                        } else {
                            Text(tr("Red: ${request["network"]} • Comisión: $reqFee EN"), color = HextechCyan, fontSize = 12.sp)
                            val walletAddress = request["wallet"] as? String ?: ""
                            Row(
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().background(Color(0xFF0F172A), androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(walletAddress, color = HextechGoldLight, fontSize = 11.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, modifier = Modifier.weight(1f))
                                IconButton(onClick = {
                                    if (walletAddress.isNotBlank()) {
                                        clipboardManager.setText(AnnotatedString(walletAddress))
                                        Toast.makeText(context, com.example.util.appTr("Dirección de billetera copiada al portapapeles"), Toast.LENGTH_SHORT).show()
                                    }
                                }, modifier = Modifier.size(28.dp).testTag("copy_wallet_btn_${request["id"]}")) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = tr("Copiar billetera"), tint = HextechGold, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        var dm by remember(request["id"]) { mutableStateOf(false) }
                        if (dm) SupportReplyDialog(reportId = "payment_${request["id"]}", userName = requestUserName,
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
                Text(tr("No hay solicitudes procesadas en los últimos 14 días."), color = TextSecondary, fontSize = 12.sp)
            } else {
                // Selector de usuario individual
                if (historyGroupedByUser.size > 1) {
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedUserFilter == null,
                                onClick = { selectedUserFilter = null },
                                label = { Text(tr("Todos (${historyGroupedByUser.size})"), fontSize = 11.sp) }
                            )
                        }
                        items(historyGroupedByUser.keys.toList()) { userKey ->
                            val userReqs = historyGroupedByUser[userKey].orEmpty()
                            val totalPaidUsdt = userReqs.filter { it["status"] == "PAID" }.sumOf { (it["amount"] as? Number)?.toLong() ?: 0L }
                            FilterChip(
                                selected = selectedUserFilter == userKey,
                                onClick = { selectedUserFilter = if (selectedUserFilter == userKey) null else userKey },
                                label = { Text("$userKey (${userReqs.size} • $totalPaidUsdt USDT)", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                val displayedGroups = if (selectedUserFilter != null) {
                    historyGroupedByUser.filterKeys { it == selectedUserFilter }
                } else {
                    historyGroupedByUser
                }

                displayedGroups.forEach { (userKey, userReqs) ->
                    val totalPaid = userReqs.filter { it["status"] == "PAID" }.sumOf { (it["amount"] as? Number)?.toLong() ?: 0L }
                    val totalEnDeducted = userReqs.filter { it["status"] == "PAID" }.sumOf { (it["totalDeducted"] as? Number)?.toLong() ?: (it["amount"] as? Number)?.toLong() ?: 0L }

                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                        color = HextechDarkBg.copy(alpha = 0.8f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HextechGold.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Cabecera individual del cobrador
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tr("Invocador") + ": " + userKey,
                                        color = HextechGold,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = tr("Historial de pago • Vigencia de 14 días"),
                                        color = HextechCyan,
                                        fontSize = 10.5.sp
                                    )
                                }
                                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                                    Text(
                                        text = "$totalPaid USDT",
                                        color = Color(0xFF10B981),
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = tr("${userReqs.size} operaciones"),
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            HorizontalDivider(color = HextechCardBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                            // Lista de pagos individuales durante los 14 días de vigencia
                            userReqs.forEach { request ->
                                val reqAmount = request["amount"] ?: 0
                                val reqFee = request["fee"] ?: 0
                                val reqTotal = request["totalDeducted"] ?: reqAmount
                                val status = (request["status"] as? String)?.uppercase() ?: ""
                                val isPaid = status == "PAID"
                                val timestamp = (request["resolvedAtMillis"] as? Number)?.toLong() ?: (request["requestedAtMillis"] as? Number)?.toLong() ?: 0L
                                val dateStr = if (timestamp > 0) java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(timestamp)) else "Reciente"
                                
                                Surface(
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                                    color = HextechSurfaceVariant.copy(alpha = 0.6f),
                                    border = androidx.compose.foundation.BorderStroke(0.8.dp, if (isPaid) Color(0xFF10B981).copy(alpha = 0.4f) else DangerRed.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                            Text(
                                                text = tr(if (isPaid) "PAGADO" else "RECHAZADO Y REEMBOLSADO"),
                                                color = if (isPaid) Color(0xFF10B981) else DangerRed,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                fontSize = 10.5.sp
                                            )
                                            Text(dateStr, color = TextSecondary, fontSize = 10.sp)
                                        }
                                        Text(tr("$reqAmount USDT (Total: $reqTotal EN)"), color = TextPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, fontSize = 12.sp)
                                        val histBinanceEmail = (request["binanceEmail"] as? String).orEmpty()
                                        if (histBinanceEmail.isNotBlank()) {
                                            Text(tr("Sin comisión de red"), color = HextechCyan, fontSize = 11.sp)
                                            Row(
                                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth().background(Color(0xFF0F172A).copy(alpha = 0.6f), androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(tr("Correo Binance") + ": " + histBinanceEmail, color = TextSecondary, fontSize = 10.5.sp,
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, modifier = Modifier.weight(1f))
                                                IconButton(onClick = {
                                                    clipboardManager.setText(AnnotatedString(histBinanceEmail))
                                                    Toast.makeText(context, com.example.util.appTr("Correo de Binance copiado al portapapeles"), Toast.LENGTH_SHORT).show()
                                                }, modifier = Modifier.size(24.dp)) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = tr("Copiar correo Binance"), tint = HextechGold, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        } else {
                                            Text(tr("Red: ${request["network"]} • Comisión de red: $reqFee EN"), color = HextechCyan, fontSize = 11.sp)
                                            val histWallet = request["wallet"] as? String ?: ""
                                            Row(
                                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth().background(Color(0xFF0F172A).copy(alpha = 0.6f), androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(histWallet, color = TextSecondary, fontSize = 10.5.sp,
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, modifier = Modifier.weight(1f))
                                                IconButton(onClick = {
                                                    if (histWallet.isNotBlank()) {
                                                        clipboardManager.setText(AnnotatedString(histWallet))
                                                        Toast.makeText(context, com.example.util.appTr("Dirección de billetera copiada al portapapeles"), Toast.LENGTH_SHORT).show()
                                                    }
                                                }, modifier = Modifier.size(24.dp)) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = tr("Copiar billetera"), tint = HextechGold, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                        var dm by remember(request["id"]) { mutableStateOf(false) }
                                        if (dm) SupportReplyDialog(reportId = "payment_${request["id"]}", userName = (request["userName"] as? String).orEmpty().ifBlank { userKey },
                                            reportTitle = "Solicitud de pago USDT", reportDescription = "", initialReply = "",
                                            onDismiss = { dm = false }, onReplySent = { _, _ -> dm = false }, userEmail = request["email"] as? String ?: "",
                                            userId = request["userId"] as? String ?: "", tag = "PAGO", isFirestoreDoc = true)
                                        TextButton(onClick = { dm = true }, modifier = Modifier.padding(top = 2.dp)) { Text(tr("Enviar DM privado"), fontSize = 10.5.sp) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UsdtWalletFields(
    network: UsdtNetwork?,
    wallet: String,
    enabled: Boolean,
    onNetwork: (UsdtNetwork) -> Unit,
    onWallet: (String) -> Unit,
    fee: Long = network?.feeEn ?: 0L,
    binanceEmail: String = "",
    onBinanceEmail: (String) -> Unit = {}
) {
    val email = binanceEmail.trim().lowercase(java.util.Locale.ROOT)
    val usingEmail = email.isNotBlank()
    val validEmail = usingEmail && UsdtWalletPolicy.validBinanceEmail(email)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
            value = binanceEmail,
            onValueChange = onBinanceEmail,
            enabled = enabled,
            singleLine = true,
            label = { Text(tr("Correo electrónico de Binance (opcional)")) },
            modifier = Modifier.fillMaxWidth().testTag("binance_email"),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
        )
        if (usingEmail) {
            if (validEmail) {
                Text(tr("Con un correo de Binance válido no necesitas seleccionar una red ni ingresar una billetera."), color = HextechCyan, fontSize = 11.sp)
            } else {
                Text(tr("Correo electrónico de Binance no válido"), color = DangerRed, fontSize = 11.5.sp)
            }
        }
        Text(tr("O usa una billetera USDT por red"), color = TextSecondary, fontSize = 11.sp)
        Text(tr("Red de USDT"), color = TextSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            UsdtNetwork.entries.forEach { option ->
                FilterChip(
                    selected = network == option,
                    onClick = { onNetwork(option) },
                    enabled = enabled && !usingEmail,
                    label = { Text(option.name) }
                )
            }
        }
        if (!usingEmail && network == null) {
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                color = HextechSurfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, HextechCyan.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = tr("Selecciona una red de transferencia de Binance para continuar."),
                    color = HextechCyan,
                    fontSize = 11.5.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
        } else if (!usingEmail && network != null) {
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                color = HextechSurfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, HextechGold.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = tr("Comisión de red (${network.name}): $fee EN a cargo del usuario (se descuenta en Esencia Naranja)."),
                        color = HextechGoldLight,
                        fontSize = 11.5.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(Color(0xFF10B981), androidx.compose.foundation.shape.CircleShape))
                        Text(text = tr("Comisión de red actualizada automáticamente en tiempo real"), color = HextechCyan, fontSize = 10.sp)
                    }
                    Text(
                        text = tr("Aviso de red Binance: Asegúrate de que la dirección ingresada pertenezca a la red ${network.name}. Enviar fondos a una red incompatible causará la pérdida irrecuperable de tus fondos."),
                        color = HextechCyan,
                        fontSize = 10.5.sp
                    )
                }
            }
        }
        OutlinedTextField(
            wallet,
            onWallet,
            enabled = enabled && !usingEmail,
            singleLine = true,
            label = { Text(tr("Tu billetera digital USDT")) },
            modifier = Modifier.fillMaxWidth().testTag("usdt_wallet"),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
        )
        Text(
            tr(if (usingEmail) "Al usar un correo de Binance válido, el pago se envía dentro de Binance y no necesitas indicar red ni billetera."
                else "Comprueba que la dirección corresponde a la red seleccionada. No envíes claves privadas ni frases de recuperación."),
            color = TextSecondary,
            fontSize = 11.sp
        )
        if (!usingEmail && network != null && wallet.isNotBlank() && !UsdtWalletPolicy.valid(network, wallet.trim())) {
            Text(tr("Billetera USDT no válida"), color = DangerRed, fontSize = 11.5.sp)
        }
    }
}

@Composable
fun CashRedemptionConfirmation(
    amount: Long,
    network: UsdtNetwork?,
    wallet: String,
    busy: Boolean,
    feedback: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    fee: Long = network?.feeEn ?: 0L,
    binanceEmail: String = ""
) {
    val cleanEmail = binanceEmail.trim().lowercase(java.util.Locale.ROOT)
    val usingEmail = cleanEmail.isNotBlank()
    val totalDeducted = amount + fee
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(tr("Confirmar canje")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(tr("Monto a recibir: $amount USDT"), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = HextechGold)
                if (usingEmail) {
                    Text(tr("Correo Binance") + ": " + cleanEmail)
                    Text(tr("Sin comisión de red"), color = HextechCyan)
                } else {
                    Text(tr("Red seleccionada: ${network?.name.orEmpty()} (Binance)"))
                    Text(tr("Comisión de red: $fee EN (a cargo del usuario)"))
                    Text(wallet, fontSize = 11.sp, color = TextSecondary)
                }
                Text(tr("Total a descontar: $totalDeducted EN"), fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, color = HextechCyan)
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    color = DangerRed.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.8f)),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp).testTag("confirmation_manual_payment_warning")
                ) {
                    Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(imageVector = androidx.compose.material.icons.Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                        Text(
                            text = tr(if (usingEmail) "Aviso: El pago es manual y demora de 24 a 72 horas. Verifica minuciosamente el correo de Binance antes de confirmar."
                                else "Aviso: El pago es manual y demora de 24 a 72 horas. Verifica minuciosamente tu billetera y red antes de confirmar."),
                            color = DangerRed,
                            fontSize = 11.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                            lineHeight = 15.sp
                        )
                    }
                }
                feedback?.let { Text(tr(it), color = DangerRed) }
            }
        },
        confirmButton = {
            TextButton(enabled = !busy, modifier = Modifier.testTag("cash_redemption_confirm"), onClick = onConfirm) {
                Text(tr(if (busy) "Procesando…" else "Confirmar"))
            }
        },
        dismissButton = {
            TextButton(enabled = !busy, modifier = Modifier.testTag("cash_redemption_cancel"), onClick = onDismiss) {
                Text(tr("Cancelar"))
            }
        }
    )
}
