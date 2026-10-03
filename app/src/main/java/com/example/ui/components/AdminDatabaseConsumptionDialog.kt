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
    fun refresh() {
        if (busy) return
        busy = true; failed = false
        scope.launch {
            val result = runCatching { DatabaseStatisticsRepository.load() }
            if (result.isSuccess) rows = result.getOrThrow() else failed = true
            busy = false
        }
    }
    LaunchedEffect(Unit) { refresh() }
    Dialog(onDismissRequest = onDismiss) {
        Surface(color = HextechDarkBg, shape = MaterialTheme.shapes.large, border = BorderStroke(1.dp, HextechGold)) {
            Column(Modifier.fillMaxWidth().heightIn(max = 700.dp).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(tr("Estadísticas de datos guardados"), color = HextechGold, style = MaterialTheme.typography.titleLarge)
                Text(tr("Recuento consultado en la nube. Cada categoría indica qué información está guardada."), color = TextSecondary)
                if (busy) CircularProgressIndicator(color = HextechCyan)
                if (failed) Text(tr("No se pudieron consultar los datos. Vuelve a intentarlo."), color = DangerRed)
                SavedDataStatisticsContent(rows)
                Button(onClick = { refresh() }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text(tr("Actualizar")) }
                if (onOpenCashRequests != null) TextButton(onClick = onOpenCashRequests) { Text(tr("Solicitudes de canje")) }
                TextButton(onClick = onDismiss) { Text(tr("Cerrar")) }
            }
        }
    }
}

@Composable
fun SavedDataStatisticsContent(rows: List<SavedDataStatistic>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEachIndexed { index, row ->
            Surface(color = HextechSurface, shape = MaterialTheme.shapes.medium) {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(tr(row.label), color = HextechGold)
                    Text(row.count?.toString() ?: tr("No disponible"), color = if (row.failed) DangerRed else HextechCyan,
                        modifier = Modifier.testTag("saved_data_count_$index"))
                    Text(tr(row.description), color = TextSecondary)
                    if (row.failed) Text(tr("No se pudo consultar esta categoría."), color = DangerRed)
                }
            }
        }
    }
}
