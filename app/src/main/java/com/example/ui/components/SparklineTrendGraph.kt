package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.tr
import java.util.Locale

/** Only observed samples are drawn; missing 12h/24h history stays missing. */
@Composable
fun SparklineTrendGraph(
    winrate: Double,
    delta: Double,
    modifier: Modifier = Modifier,
    showTimeLabels: Boolean = true,
    showFullText: Boolean = false,
    canvasHeight: Int = 20,
    observed24h: Double? = null,
    observed12h: Double? = null
) {
    val samples = listOf(observed24h, observed12h, winrate.takeIf { it.isFinite() && it in 0.0..100.0 })
    val present = samples.filterNotNull()
    val labels = listOf(tr(if (showFullText) "hace 24 horas" else "hace 24h"),
        tr(if (showFullText) "hace 12 horas" else "hace 12h"), tr("actual"))
    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(canvasHeight.dp).testTag("observed_trend_canvas")) {
            val low = (present.minOrNull() ?: 0.0) - 0.2
            val high = (present.maxOrNull() ?: 0.0) + 0.2
            val points = samples.mapIndexed { index, value -> value?.let {
                Offset(3f + index / 2f * (size.width - 6f),
                    (size.height - 3f - ((it - low) / (high - low) * (size.height - 6f))).toFloat())
            } }
            points.zipWithNext().forEach { (a, b) ->
                if (a != null && b != null) drawLine(HextechCyan, a, b, strokeWidth = 2.dp.toPx())
            }
            points.filterNotNull().forEach { drawCircle(HextechCyan, 2.dp.toPx(), it) }
        }
        if (showTimeLabels) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            samples.forEachIndexed { index, value ->
                Column {
                    Text(labels[index], color = TextMuted, fontSize = 8.sp)
                    Text(value?.let { String.format(Locale.US, "%.2f%%", it) } ?: "—", color = HextechCyan, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
fun DetailedTrendGraphCard(
    winrate: Double,
    delta: Double,
    modifier: Modifier = Modifier,
    observed24h: Double? = null,
    observed12h: Double? = null
) {
    Card(modifier.fillMaxWidth().testTag("detailed_trend_graph"),
        colors = CardDefaults.cardColors(containerColor = HextechSurfaceVariant)) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(tr("Evolución del Win Rate"), color = HextechGold, fontSize = 12.sp)
            SparklineTrendGraph(winrate, delta, showFullText = true, canvasHeight = 30,
                observed24h = observed24h, observed12h = observed12h)
            if (observed24h == null || observed12h == null) {
                Text(tr("El historial se completa con observaciones reales de 12 y 24 horas."), color = TextMuted, fontSize = 10.sp)
            }
        }
    }
}
