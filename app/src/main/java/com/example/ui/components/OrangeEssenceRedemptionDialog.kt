package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
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
    Dialog(onDismissRequest = { if (!busy) onDismiss() }) {
        Surface(color = HextechDarkBg, shape = MaterialTheme.shapes.large) {
            Column(Modifier.fillMaxWidth().heightIn(max = 650.dp).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(tr("Canjear Esencia Naranja"), style = MaterialTheme.typography.titleLarge, color = HextechGold)
                Text(tr("Saldo: $orange EN"), color = HextechCyan)
                Text(tr("El pago es manual y demora de 24 a 72 horas. El equipo coordinará el pago contigo desde la bandeja de entrada."), color = TextSecondary)
                Text(tr("Pago exclusivamente en USDT"), color = HextechGold)
                UsdtWalletFields(network, wallet, !busy, onNetwork = { network = it }, onWallet = { wallet = it })
                if (orange > 0) CashRedemptionOptions(orange, !busy && UsdtWalletPolicy.valid(network, wallet.trim())) { selected -> amount = selected; id = java.util.UUID.randomUUID().toString(); feedback = null }
                feedback?.let { Text(tr(it), color = HextechCyan) }
                Text(tr("Historial"), color = HextechGold)
                if (loadError) Text(tr("No se pudo cargar el historial. Vuelve a intentarlo."), color = DangerRed)
                requests.forEach { request ->
                    val date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, if (currentAppLanguage() == "pt") java.util.Locale("pt", "BR") else java.util.Locale("es"))
                        .format(Date((request["requestedAtMillis"] as? Number)?.toLong() ?: 0L))
                    Text(tr("$date • ${request["amount"]} EN → ${request["usd"]} USDT"), color = TextPrimary)
                    Text(tr(when (request["status"]) { "PAID" -> "Pagado"; "REJECTED" -> "Rechazado y reembolsado"; else -> "Pendiente" }), color = HextechCyan)
                }
                TextButton(onClick = onDismiss, enabled = !busy) { Text(tr("Cerrar")) }
            }
        }
    }
}

@Composable
fun CashRedemptionOptions(balance: Long, enabled: Boolean = true, onChoose: (Long) -> Unit) {
    if (balance <= 0) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EssenceEconomyPolicy.redemptionAmounts.forEach { amount ->
            Button(onClick = { onChoose(amount) }, enabled = enabled && balance >= amount,
                modifier = Modifier.fillMaxWidth().testTag("cash_redemption_$amount")) { Text(tr("$amount EN → $amount USDT")) }
        }
    }
}

@Composable
fun CashRedemptionReviewPanel() {
    var requests by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    DisposableEffect(Unit) {
        val listener = EssenceEconomyRepository.redemptions.whereEqualTo("status", "PENDING").addSnapshotListener { snapshot, failure ->
            if (failure != null) error = "No se pudieron cargar las solicitudes"
            else { error = null; requests = snapshot?.documents.orEmpty().mapNotNull { it.data?.plus("id" to it.id) } }
        }
        onDispose { listener.remove() }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(tr("Solicitudes de canje"), color = HextechGold)
        error?.let { Text(tr(it), color = DangerRed) }
        requests.forEach { request ->
            var decision by remember(request["id"]) { mutableStateOf<Boolean?>(null) }
            Text(tr("${request["email"]} • ${request["amount"]} EN → ${request["usd"]} USDT"), color = TextPrimary)
            Text("USDT • ${request["network"]}", color = TextSecondary)
            Text(request["wallet"] as? String ?: "", color = TextPrimary)
            var dm by remember(request["id"]) { mutableStateOf(false) }
            if (dm) SupportReplyDialog(reportId = "payment_${request["id"]}", userName = request["email"] as? String ?: "Usuario",
                reportTitle = "Solicitud de pago USDT", reportDescription = "", initialReply = "",
                onDismiss = { dm = false }, onReplySent = { _, _ -> dm = false }, userEmail = request["email"] as? String ?: "",
                userId = request["userId"] as? String ?: "", tag = "PAGO", isFirestoreDoc = true)
            TextButton(onClick = { dm = true }) { Text(tr("Enviar DM privado")) }
            Row {
                TextButton(enabled = !busy, onClick = { decision = true }) { Text(tr("Marcar pagado")) }
                TextButton(enabled = !busy, onClick = { decision = false }) { Text(tr("Rechazar y devolver esencias")) }
            }
            decision?.let { paid ->
                AlertDialog(onDismissRequest = { if (!busy) decision = null }, title = { Text(tr("Confirmar")) },
                    text = { Text(tr(if (paid) "Confirma únicamente después de realizar el pago manual." else "Se devolverán las esencias al usuario.")) },
                    confirmButton = { TextButton(enabled = !busy, onClick = { busy = true; scope.launch {
                        val result = EssenceEconomyRepository.resolve(request["id"] as String, paid)
                        error = if (result.isFailure) "No se pudo completar la operación. Comprueba tu conexión y vuelve a intentarlo." else null
                        busy = false; decision = null
                    } }) { Text(tr("Confirmar")) } }, dismissButton = { TextButton(enabled = !busy, onClick = { decision = null }) { Text(tr("Cancelar")) } })
            }
        }
    }
}


@Composable
fun UsdtWalletFields(network: UsdtNetwork, wallet: String, enabled: Boolean,
    onNetwork: (UsdtNetwork) -> Unit, onWallet: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(tr("Red de USDT"), color = TextSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            UsdtNetwork.entries.forEach { option ->
                FilterChip(selected = network == option, onClick = { onNetwork(option) }, enabled = enabled, label = { Text(option.name) })
            }
        }
        OutlinedTextField(wallet, onWallet, enabled = enabled, singleLine = true,
            label = { Text(tr("Tu billetera digital USDT")) }, modifier = Modifier.fillMaxWidth().testTag("usdt_wallet"),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
        Text(tr("Comprueba que la dirección corresponde a la red seleccionada. No envíes claves privadas ni frases de recuperación."), color = TextSecondary)
        if (wallet.isNotBlank() && !UsdtWalletPolicy.valid(network, wallet.trim())) Text(tr("Billetera USDT no válida"), color = DangerRed)
    }
}


@Composable
fun CashRedemptionConfirmation(amount: Long, network: UsdtNetwork, wallet: String, busy: Boolean,
    feedback: String? = null, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = { if (!busy) onDismiss() }, title = { Text(tr("Confirmar canje")) },
        text = { Column { Text(tr("Se descontarán $amount EN para recibir $amount USDT. El pago es manual y demora de 24 a 72 horas.")); Text("USDT • ${network.name}"); Text(wallet); feedback?.let { Text(tr(it), color = DangerRed) } } },
        confirmButton = { TextButton(enabled = !busy, modifier = Modifier.testTag("cash_redemption_confirm"), onClick = onConfirm) { Text(tr(if (busy) "Procesando…" else "Confirmar")) } },
        dismissButton = { TextButton(enabled = !busy, modifier = Modifier.testTag("cash_redemption_cancel"), onClick = onDismiss) { Text(tr("Cancelar")) } })
}
