package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

/** Public tier view only needs a concise refresh state, not diagnostic paragraphs. */
@Composable
fun MetaSyncSummary() {
    val health by BestBuildWrScraper.freshness.collectAsState()
    val syncing by BestBuildWrScraper.isSyncing.collectAsState()
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    // Actualiza la lectura de frescura cada segundo. Esto no altera una tasa
    // publicada: solo hace visible cuánto tiempo lleva vigente la observación.
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(1_000L) } }
    val at = minOf(health.tiersCheckedAt, health.ratesCheckedAt)
    Row(Modifier.fillMaxWidth().testTag("meta_sync_summary").padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (syncing) CircularProgressIndicator(Modifier.size(12.dp), color = HextechCyan, strokeWidth = 1.dp)
        Text(tr(when {
            syncing -> "Actualizando estadísticas"
            health.stale(now) -> "Datos guardados · esperando actualización"
            else -> "En vivo · consulta automática"
        }) + if (!syncing && at > 0) " · " + metaTime(at) else "",
            color = if (health.stale(now) && !syncing) HextechGold else HextechCyan, fontSize = 10.sp)
    }
}
