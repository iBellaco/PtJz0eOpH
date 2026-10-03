package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.*
import com.example.model.PremiumAccessPolicy
import com.example.ui.theme.*
import com.example.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionPlansBottomSheet(onDismiss: () -> Unit) {
    val blue by SubscriptionManager.blueEssence.collectAsState()
    val orange by SubscriptionManager.orangeEssence.collectAsState()
    val role by SubscriptionManager.userRole.collectAsState()
    val secondary by SubscriptionManager.secondaryRole.collectAsState()
    val claim by AuthManager.isAdminClaim.collectAsState()
    var selected by remember { mutableStateOf<Pair<EssencePremiumPlan, EssenceCurrency>?>(null) }
    var operationId by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    if (selected == null) {
        ModalBottomSheet(onDismissRequest = { if (!busy) onDismiss() }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = HextechDarkBg) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(tr("Suscripción Premium"), style = MaterialTheme.typography.headlineSmall, color = HextechGold)
                PanelReadControl(NotificationPanel.PLANS)
                Text(tr("Saldo: $blue EA • $orange EN"), color = HextechCyan)
                Text(tr("Escáner Automático del draft"), color = TextPrimary)
                Text(tr("Historial del draft"), color = TextPrimary)
                Text(tr("Campeones Favoritos • Temas Exclusivos • Avatares Exclusivos"), color = TextPrimary)
                if (PremiumAccessPolicy.isLifetime(role, secondary, claim)) Text(tr("Tu acceso premium es vitalicio"), color = HextechGold)
                else EssencePlanOptions(blue, orange, !busy) { plan, currency ->
                    operationId = java.util.UUID.randomUUID().toString(); feedback = null; selected = plan to currency
                }
                feedback?.let { Text(tr(it), color = HextechCyan) }
                TextButton(onClick = onDismiss, enabled = !busy) { Text(tr("Cerrar")) }
            }
        }
    }
    selected?.let { (plan, currency) ->
        val planLabel = tr(if (plan == EssencePremiumPlan.MONTHLY) "Mensual" else "Anual")
        AlertDialog(onDismissRequest = { if (!busy) selected = null },
            title = { Text(tr("Confirmar suscripción")) },
            text = { Column { Text(tr("Se descontarán ${plan.cost(currency)} ${currency.abbreviation}. El plan $planLabel añade ${plan.days} días a tu tiempo premium.")); feedback?.let { Text(tr(it), color = DangerRed) } } },
            confirmButton = { TextButton(enabled = !busy, modifier = Modifier.testTag("premium_purchase_confirm"), onClick = {
                busy = true; feedback = null
                scope.launch {
                    val result = EssenceEconomyRepository.purchase(operationId, plan, currency)
                    busy = false
                    if (result.isSuccess) { selected = null; feedback = "Suscripción activada" }
                    else feedback = economyFailure(result.exceptionOrNull())
                }
            }) { Text(tr(if (busy) "Procesando…" else "Confirmar")) } },
            dismissButton = { TextButton(enabled = !busy, onClick = { selected = null; feedback = null }) { Text(tr("Cancelar")) } })
    }
}

@Composable
fun EssencePlanOptions(blue: Long, orange: Long, enabled: Boolean = true,
    onChoose: (EssencePremiumPlan, EssenceCurrency) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        EssencePremiumPlan.entries.forEach { plan ->
            Surface(color = HextechSurface, border = BorderStroke(1.dp, HextechGold), shape = MaterialTheme.shapes.medium) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(tr(if (plan == EssencePremiumPlan.MONTHLY) "Mensual" else "Anual"), color = HextechGold, style = MaterialTheme.typography.titleLarge)
                    Text(tr("${plan.days} días de Premium"), color = TextSecondary)
                    Button(onClick = { onChoose(plan, EssenceCurrency.BLUE) }, enabled = enabled && blue >= plan.blueCost,
                        modifier = Modifier.fillMaxWidth().testTag("premium_${plan.name}_BLUE")) { Text(tr("${plan.blueCost} Esencias Azules")) }
                    if (orange > 0) Button(onClick = { onChoose(plan, EssenceCurrency.ORANGE) }, enabled = enabled && orange >= plan.orangeCost,
                        modifier = Modifier.fillMaxWidth().testTag("premium_${plan.name}_ORANGE")) { Text(tr("${plan.orangeCost} Esencias Naranjas")) }
                    if (blue < plan.blueCost && orange < plan.orangeCost) Text(tr("Esencias insuficientes"), color = TextMuted)
                }
            }
        }
    }
}

internal fun economyFailure(error: Throwable?): String = when {
    generateSequence(error) { it.cause }.any { it is com.google.firebase.firestore.FirebaseFirestoreException && it.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED } ->
        "No se pudo autorizar la operación. No se descontaron esencias."
    error?.message?.contains("Esencias insuficientes") == true -> "Esencias insuficientes"
    else -> "No se pudo completar la operación. Comprueba tu conexión y vuelve a intentarlo."
}
