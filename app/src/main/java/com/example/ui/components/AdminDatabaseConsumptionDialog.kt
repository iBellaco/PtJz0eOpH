package com.example.ui.components

import com.example.ui.components.CoachButton as Button
import com.example.ui.components.CoachTextButton as TextButton

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.theme.*
import com.example.util.tr
import kotlinx.coroutines.launch

@Composable
fun AdminDatabaseConsumptionDialog(onDismiss: () -> Unit, onOpenCashRequests: (() -> Unit)? = null) {
    var rows by remember { mutableStateOf<List<SavedDataStatistic>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun refresh(forceRefresh: Boolean = false) {
        if (busy) return
        busy = true; failed = false
        scope.launch {
            val result = runCatching { DatabaseStatisticsRepository.load(forceRefresh = forceRefresh) }
            if (result.isSuccess) rows = result.getOrThrow() else failed = true
            busy = false
        }
    }
    val consumption by DatabaseStatisticsRepository.consumption.collectAsState()
    LaunchedEffect(Unit) {
        while (true) { refresh(); kotlinx.coroutines.delay(300_000) }
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(color = HextechDarkBg, shape = MaterialTheme.shapes.large, border = BorderStroke(1.dp, HextechGold)) {
            Column(Modifier.fillMaxWidth().heightIn(max = 700.dp).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(tr("Resumen de datos"), color = HextechGold, style = MaterialTheme.typography.titleLarge)
                Text(tr("Consulta el volumen y abre cada categoría para ver los detalles."), color = TextSecondary)
                if (busy) CircularProgressIndicator(color = HextechCyan)
                if (failed) Text(tr("No se pudieron consultar los datos. Vuelve a intentarlo."), color = DangerRed)
                Surface(color = HextechSurface, shape = MaterialTheme.shapes.medium) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(tr("Registros"), color = TextSecondary)
                            Text(rows.mapNotNull { it.count }.sum().toString(), color = HextechGold, style = MaterialTheme.typography.titleLarge)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(tr("Tamaño estimado"), color = TextSecondary)
                            Text(consumption.estimatedBytes?.let(StorageConsumptionPolicy::formatBytes) ?: tr("No disponible"), color = HextechCyan, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
                var showStorageDetails by remember { mutableStateOf(false) }
                TextButton(onClick = { showStorageDetails = !showStorageDetails }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("toggle_storage_details")) {
                    Text(tr(if (showStorageDetails) "Ocultar detalles de almacenamiento" else "Ver detalles de almacenamiento"))
                }
                if (showStorageDetails) StorageConsumptionSummary(consumption)
                SavedDataStatisticsContent(rows)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { refresh(forceRefresh = true) }, enabled = !busy, modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("refresh_data_summary")) { Text(tr("Actualizar")) }
                    TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp).testTag("close_data_summary")) { Text(tr("Cerrar")) }
                }
                if (onOpenCashRequests != null) TextButton(onClick = onOpenCashRequests, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("open_cash_requests")) { Text(tr("Solicitudes de canje")) }
            }
        }
    }
}

@Composable
fun SavedDataStatisticsContent(rows: List<SavedDataStatistic>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEachIndexed { index, row ->
            var expanded by remember(row.label) { mutableStateOf(false) }
            Surface(color = HextechSurface, shape = MaterialTheme.shapes.medium) {
                Column(Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(tr(row.label), color = HextechGold, modifier = Modifier.weight(1f))
                        Text(row.count?.toString() ?: "—", color = if (row.failed) DangerRed else HextechCyan,
                            modifier = Modifier.testTag("saved_data_count_$index"))
                    }
                    if (expanded) {
                        Text(tr(row.description), color = TextSecondary)
                        row.estimatedBytes?.let { Text(StorageConsumptionPolicy.formatBytes(it), color = HextechCyan) }
                        if (row.failed || row.failureReason != null) Text(tr(row.failureReason ?: "No se pudo consultar esta categoría."), color = if (row.failed) DangerRed else HextechGold)
                    }
                }
            }
        }
    }
}

@Composable
fun StorageConsumptionSummary(consumption: StorageConsumption) {
    Surface(color = HextechSurface, shape = MaterialTheme.shapes.medium) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(tr("Tu almacenamiento"), color = HextechGold, style = MaterialTheme.typography.titleMedium)
            Text(tr("Contenido guardado estimado") + ": " + (consumption.estimatedBytes?.let(StorageConsumptionPolicy::formatBytes) ?: tr("No disponible")), color = HextechCyan)
            Text(tr("Crecimiento diario aproximado") + ": " + (consumption.dailyGrowthBytes?.let { StorageConsumptionPolicy.formatBytes(it) + " / " + tr("día") } ?: tr("Calculando con las próximas muestras")), color = TextPrimary)
            Text(tr("Límite del plan: consulta del proveedor no conectada"), color = TextSecondary)
            Text(tr("Actualización automática cada 6 horas. Estimación del contenido de documentos; excluye índices, archivos externos y tráfico de lecturas y escrituras."), color = TextSecondary)
            if (!consumption.complete) Text(tr("Muestra parcial: algunas categorías no se pudieron consultar."), color = HextechGold)
        }
    }
}
