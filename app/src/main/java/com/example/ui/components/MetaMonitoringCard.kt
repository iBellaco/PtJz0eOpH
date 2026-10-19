package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.sync.BestBuildWrScraper
import com.example.data.sync.MetaFreshness
import com.example.data.sync.ScraperSourceStatus
import com.example.ui.theme.*
import com.example.util.tr
import kotlinx.coroutines.delay

/** Every source problem remains visible; technical detail is opened deliberately. */
@Composable
fun MetaMonitoringCard() {
    val sources by BestBuildWrScraper.sourceStatuses.collectAsState()
    val health by BestBuildWrScraper.freshness.collectAsState()
    val syncing by BestBuildWrScraper.isSyncing.collectAsState()
    val version by com.example.util.AppUpdateManager.checkStatus.collectAsState()
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    var selected by remember { mutableStateOf<ScraperSourceStatus?>(null) }
    LaunchedEffect(Unit) { while (true) { delay(1_000); now = System.currentTimeMillis() } }
    val expected = health.expectedCount.takeIf { it > 0 } ?: 142
    fun issue(status: ScraperSourceStatus): String? = when {
        status.lastChecked == 0L -> "Pendiente de comprobación"
        now - status.lastChecked > 3 * MetaFreshness.POLL_INTERVAL_MS -> "Consulta detenida"
        !status.isHealthy -> status.errorMessage ?: "Error de conexión"
        status.errorMessage != null -> status.errorMessage
        status.lastRatesChangedAt > 0 && now - status.lastRatesChangedAt >= 24 * 60 * 60_000L -> "Tasas sin cambios desde hace 24 horas"
        status.tierCount < expected -> "Cobertura incompleta"
        status.lastChangedAt > 0 && now - status.lastChangedAt >= 24 * 60 * 60_000L -> "Sin cambios desde hace 24 horas"
        else -> null
    }
    val issues = sources.values.count { issue(it) != null }
    Surface(Modifier.fillMaxWidth().padding(12.dp).testTag("meta_monitoring_card"),
        color = HextechSurface, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (issues > 0 || health.stale(now)) HextechGold else HextechCardBorder)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MonitorHeart, null, tint = HextechCyan, modifier = Modifier.size(22.dp))
                Text(tr("Monitoreo"), Modifier.weight(1f).padding(start = 8.dp), color = HextechGold, fontWeight = FontWeight.Bold)
                Text("${sources.size - issues}/${sources.size} " + tr("sin avisos"),
                    color = if (issues > 0) HextechGold else HextechCyan, fontSize = 12.sp)
            }
            Text(tr(if (syncing) "Comprobando fuentes" else if (health.stale(now)) "Actualización pendiente" else "Consulta automática cada minuto"),
                color = if (health.stale(now)) HextechGold else TextSecondary, fontSize = 11.sp)
            sources.forEach { (id, status) ->
                val problem = issue(status)
                val color = when { !status.isHealthy && status.lastChecked > 0 -> DangerRed
                    problem != null -> HextechGold; else -> HextechCyan }
                CoachTextButton(onClick = { selected = status }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    .testTag("meta_source_$id"), contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)) {
                    Icon(if (problem == null) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                        null, tint = color, modifier = Modifier.size(18.dp))
                    Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                        Text(status.name.substringBefore(" ("), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(tr(problem ?: "Funcionando") + " · ${status.tierCount}/$expected", color = color, fontSize = 10.sp)
                    }
                    Text(if (status.lastChecked > 0) java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                        .format(java.util.Date(status.lastChecked)) else "—", color = TextMuted, fontSize = 10.sp)
                }
            }
            HorizontalDivider(color = HextechCardBorder)
            val updated = health.updatedChampionCount.takeIf { it > 0 } ?: health.statsCount
            Text(tr("Campeones actualizados") + ": $updated/$expected", color = HextechCyan, fontSize = 11.sp)
            val versionFailed = version.lastAttemptAt > version.lastSuccessAt && now - version.lastAttemptAt > 25_000L
            Text(tr(if (versionFailed) "No se pudo comprobar la versión publicada" else "Versión disponible") +
                if (versionFailed) "" else ": " + version.publishedName.ifBlank { "—" },
                color = if (versionFailed) HextechGold else TextMuted, fontSize = 11.sp)
        }
    }
    selected?.let { source ->
        // Keep an open detail synchronized as new responses arrive.
        val status = sources.values.firstOrNull { it.url == source.url } ?: source
        AlertDialog(onDismissRequest = { selected = null }, containerColor = HextechSurface,
            title = { Text(status.name.substringBefore(" ("), color = HextechGold) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(tr(issue(status) ?: "Funcionando"), color = if (issue(status) != null) HextechGold else HextechCyan)
                Text(tr("Parche") + ": " + status.publishedPatch.ifBlank { "—" })
                Text(tr("Categorías") + ": ${status.tierCount}/$expected")
                Text(tr("Estadísticas") + ": ${status.statsCount}/$expected")
                Text(tr("Historial disponible") + ": ${status.historyCount}/$expected")
                Text(tr("Última consulta válida") + ": " + metaTime(status.lastSuccessAt))
                Text(tr("Último cambio de valores") + ": " + metaTime(status.lastChangedAt))
                Text(tr("Tiempo de respuesta") + ": ${status.responseTimeMs} ms")
            } }, confirmButton = { CoachTextButton(onClick = { selected = null },
                modifier = Modifier.heightIn(min = 48.dp).testTag("close_meta_source_detail")) { Text(tr("Cerrar")) } })
    }
}
