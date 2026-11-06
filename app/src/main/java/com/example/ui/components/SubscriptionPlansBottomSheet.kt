package com.example.ui.components

import com.example.ui.components.CoachButton as Button
import com.example.ui.components.CoachOutlinedButton as OutlinedButton
import com.example.ui.components.CoachTextButton as TextButton

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
                PremiumPlansOverview(blue, orange)
                PanelReadControl(NotificationPanel.PLANS)
                if (PremiumAccessPolicy.isLifetime(role, secondary, claim)) Text(tr("Tu acceso premium es vitalicio"), color = HextechGold)
                else EssencePlanOptions(blue, orange, !busy) { plan, currency ->
                    operationId = java.util.UUID.randomUUID().toString(); feedback = null; selected = plan to currency
                }
                feedback?.let { Text(tr(it), color = HextechCyan) }
                Text(tr("Elige tu plan y confirma antes de descontar esencias."), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = onDismiss, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text(tr("Cerrar")) }
            }
        }
    }
    selected?.let { (plan, currency) ->
        val planLabel = tr(plan.labelKey)
        AlertDialog(onDismissRequest = { if (!busy) selected = null },
            title = { Text(tr("Confirmar suscripción")) },
            text = { Column {
                Text(
                    tr("Se descontarán {0} {1}. El plan {2} añade {3} días a tu tiempo premium.")
                        .replace("{0}", plan.cost(currency).toString())
                        .replace("{1}", currency.abbreviation)
                        .replace("{2}", planLabel)
                        .replace("{3}", plan.days.toString())
                )
                feedback?.let { Text(tr(it), color = DangerRed) }
            } },
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
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        EssencePremiumPlan.entries.forEach { plan ->
            Surface(color = HextechSurface, border = BorderStroke(1.dp, if (plan != EssencePremiumPlan.MONTHLY) HextechGold else HextechCyan.copy(alpha = 0.6f)), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(if (plan == EssencePremiumPlan.MONTHLY) Icons.Default.DateRange else Icons.Default.WorkspacePremium,
                            contentDescription = null, tint = if (plan == EssencePremiumPlan.MONTHLY) HextechCyan else HextechGold)
                        Text(tr(plan.labelKey), color = TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    if (plan != EssencePremiumPlan.MONTHLY) Text(tr("Más tiempo, mejor valor"), color = HextechGold, style = MaterialTheme.typography.labelMedium)
                    Text(tr("{0} días de Premium").replace("{0}", plan.days.toString()), color = TextSecondary)
                    Button(onClick = { onChoose(plan, EssenceCurrency.BLUE) }, enabled = enabled && blue >= plan.blueCost,
                        modifier = Modifier.fillMaxWidth().testTag("premium_${plan.name}_BLUE"),
                        shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = HextechCyan, contentColor = HextechDarkBg)) { Text("${plan.blueCost} ${tr("Esencias Azules")}") }
                    SavingsBadge(plan, EssenceCurrency.BLUE)
                    if (orange > 0) {
                        OutlinedButton(onClick = { onChoose(plan, EssenceCurrency.ORANGE) }, enabled = enabled && orange >= plan.orangeCost,
                            modifier = Modifier.fillMaxWidth().testTag("premium_${plan.name}_ORANGE"), shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFFF9E1B)), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF9E1B))) { Text("${plan.orangeCost} ${tr("Esencias Naranjas")}") }
                        SavingsBadge(plan, EssenceCurrency.ORANGE)
                    }
                    if (blue < plan.blueCost && orange < plan.orangeCost) Text(tr("Esencias insuficientes"), color = TextMuted)
                }
            }
        }
    }
}

@Composable
private fun SavingsBadge(plan: EssencePremiumPlan, currency: EssenceCurrency) {
    val savings = plan.savings(currency)
    if (savings <= 0L) return
    val pulse = rememberInfiniteTransition(label = "premium_savings_${plan.name}_${currency.name}").animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(animation = tween(900), repeatMode = RepeatMode.Reverse),
        label = "premium_savings_scale"
    )
    val abbreviation = currency.abbreviation
    Surface(
        color = HextechGreen.copy(alpha = 0.14f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, HextechGreen.copy(alpha = 0.55f)),
        modifier = Modifier
            .scale(pulse.value)
            .testTag("premium_savings_${plan.name}_${currency.name}")
    ) {
        Text(
            text = tr("Ahorro de {0} $abbreviation").replace("{0}", savings.toString()),
            color = HextechGreen,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}

internal fun economyFailure(error: Throwable?): String = when {
    generateSequence(error) { it.cause }.any { it is com.google.firebase.firestore.FirebaseFirestoreException && it.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED } ->
        "No se pudo autorizar la operación. No se descontaron esencias."
    error?.message?.contains("Esencias insuficientes") == true -> "Esencias insuficientes"
    else -> "No se pudo completar la operación. Comprueba tu conexión y vuelve a intentarlo."
}

@Composable
internal fun PremiumPlansOverview(blue: Long, orange: Long) {
    Column(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(HextechGold.copy(alpha = 0.15f), HextechSurface)), RoundedCornerShape(24.dp))
        .padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = HextechGold, modifier = Modifier.size(36.dp))
        Text(tr("Tu próximo nivel en Coach"), color = HextechGold, style = MaterialTheme.typography.labelLarge)
        Text(tr("Suscripción Premium"), color = TextPrimary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(tr("Herramientas para analizar tus partidas y personalizar tu experiencia."), color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        HorizontalDivider(color = HextechGold.copy(alpha = 0.25f))
        Text(tr("Saldo: $blue EA • $orange EN"), color = HextechCyan, style = MaterialTheme.typography.labelLarge)
        listOf(
            "Escáner Automático del draft (función exclusiva Premium)",
            "Historial del draft y perfiles de prueba",
            "Campeones Favoritos y análisis ampliado",
            "24 ventajas, 24 debilidades y 24 sinergias por campeón",
            "Builds, runas y hechizos adaptados al parche",
            "Tier list y estadísticas actualizadas",
            "Temas de navegación y partículas exclusivos",
            "Avatares y marcos exclusivos"
        ).forEach { benefit ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HextechGold, modifier = Modifier.size(18.dp))
                Text(tr(benefit), color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
