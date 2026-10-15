package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.sync.BestBuildWrScraper
import com.example.ui.theme.*
import com.example.util.tr
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun metaTime(at: Long): String = if (at > 0) SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()).format(Date(at)) else "—"

@Composable
fun MetaSyncSummary() {
    val health by BestBuildWrScraper.freshness.collectAsState()
    val syncing by BestBuildWrScraper.isSyncing.collectAsState()
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(15_000L) } }
    Column(Modifier.fillMaxWidth().testTag("meta_sync_summary").padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(tr(when {
            syncing -> "Sincronizando..."
            health.stale(now) -> "Alerta: estadísticas pendientes de actualización"
            health.longUnchanged(now) -> "Aviso: la fuente mantiene los mismos valores desde hace 24 horas"
            health.unchangedChecks > 0 -> "Fuente consultada: sin cambios publicados"
            else -> "Estadísticas verificadas"
        }), color = if (health.stale(now)) DangerRed else HextechCyan, fontSize = 11.sp)
        Text(tr("Categorías consultadas") + ": " + metaTime(health.tiersCheckedAt) + "\n" +
            tr("Tasas consultadas") + ": " + metaTime(health.ratesCheckedAt), color = TextMuted, fontSize = 10.sp)
        Text(tr("Cobertura de campeones") + ": " + health.tierCount + "/" + health.expectedCount + " Tier · " +
            health.statsCount + "/" + health.expectedCount + " WR/Pick/Ban", color = TextSecondary, fontSize = 10.sp)
        if (health.statsCount < health.expectedCount && health.lastAttemptAt > 0) {
            Text(tr("Cobertura incompleta: algunos valores no están publicados en la fuente"), color = HextechGold, fontSize = 10.sp)
            val missing = com.example.data.WildRiftRepository.champions.filter { !it.hasRegionalStats }
            if (missing.isNotEmpty()) Text(tr("Sin tasas publicadas") + ": " + missing.joinToString { tr(it.name) }, color = TextMuted, fontSize = 10.sp)
        }
        Text(tr("Último cambio de valores") + ": " + metaTime(health.lastChangedAt), color = TextMuted, fontSize = 10.sp)
    }
}
