package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.tr
import java.util.Locale

/** Three real reference points. Dated daily reports are identified explicitly. */
@Composable
fun SparklineTrendGraph(
    winrate: Double, delta: Double, modifier: Modifier = Modifier,
    showTimeLabels: Boolean = true, showFullText: Boolean = false, canvasHeight: Int = 48,
    observed24h: Double? = null, observed12h: Double? = null,
    dailyReferences: Boolean = false, reference24hAt: Long = 0L, reference12hAt: Long = 0L
) {
    // Pulso continuo del punto actual y del trazo para que la gráfica no parezca
    // estática entre dos consultas de datos reales.
    val trendAnimation = rememberInfiniteTransition(label = "tier_trend_animation")
    val livePulse by trendAnimation.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(950), RepeatMode.Reverse),
        label = "tier_trend_pulse"
    )
    val trendColor = when {
        delta > 0.005 -> HextechGreen
        delta < -0.005 -> DangerRed
        else -> HextechCyan
    }
    val samples = listOf(observed24h, observed12h, winrate).map { it?.takeIf { v -> v.isFinite() && v in 0.0..100.0 } }
    val present = samples.filterNotNull()
    val labels = listOf(tr("hace 24h"), tr("hace 12h"), tr("actual"))
    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(canvasHeight.coerceAtLeast(48).dp).testTag("observed_trend_canvas")) {
            val margin = 6.dp.toPx()
            val middle = ((present.minOrNull() ?: 50.0) + (present.maxOrNull() ?: 50.0)) / 2.0
            val span = maxOf(1.0, (present.maxOrNull() ?: middle) - (present.minOrNull() ?: middle) + 0.4)
            val low = middle - span / 2
            val points = samples.mapIndexed { index, value -> value?.let {
                Offset(margin + index / 2f * (size.width - 2 * margin),
                    (size.height - margin - ((it - low) / span * (size.height - 2 * margin))).toFloat())
            } }
            repeat(3) { index ->
                val x = margin + index / 2f * (size.width - 2 * margin)
                drawLine(HextechCardBorder, Offset(x, margin), Offset(x, size.height - margin),
                    1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx())))
            }
            points.zipWithNext().forEach { (a, b) ->
                if (a != null && b != null) drawLine(trendColor, a, b, strokeWidth = 2.dp.toPx())
            }
            points.forEachIndexed { i, point -> if (point != null) {
                if (i == 2) drawCircle(trendColor.copy(alpha = livePulse * 0.28f), 7.dp.toPx(), point)
                drawCircle(if (i == 1) HextechGold else trendColor, 3.dp.toPx(), point)
            } }
        }
        if (showTimeLabels) Row(Modifier.fillMaxWidth()) {
            samples.forEachIndexed { index, value ->
                Column(Modifier.weight(1f), horizontalAlignment = when (index) {
                    0 -> Alignment.Start; 1 -> Alignment.CenterHorizontally; else -> Alignment.End
                }) {
                    Text(labels[index], color = TextSecondary, fontSize = 9.sp)
                    Text(value?.let { String.format(Locale.US, "%.2f%%", it) } ?: "—",
                        color = if (index == 1) HextechGold else trendColor, fontSize = 10.sp)
                    val at = if (index == 0) reference24hAt else if (index == 1) reference12hAt else 0L
                    if (dailyReferences && at > 0) Text(java.text.SimpleDateFormat("dd/MM", Locale.getDefault())
                        .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }.format(java.util.Date(at)), color = TextMuted, fontSize = 8.sp)
                }
            }
        }
        if (dailyReferences) Text(tr("Referencias diarias publicadas"), color = TextMuted, fontSize = 9.sp,
            modifier = Modifier.testTag("trend_daily_reference"))
        else if (observed24h == null || observed12h == null) Text(tr("Recopilando historial"),
            color = TextMuted, fontSize = 9.sp, modifier = Modifier.testTag("trend_history_pending"))
    }
}

@Composable
fun DetailedTrendGraphCard(winrate: Double, delta: Double, modifier: Modifier = Modifier,
    observed24h: Double? = null, observed12h: Double? = null, dailyReferences: Boolean = false,
    reference24hAt: Long = 0L, reference12hAt: Long = 0L) {
    Card(modifier.fillMaxWidth().testTag("detailed_trend_graph"),
        colors = CardDefaults.cardColors(containerColor = HextechSurfaceVariant)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(tr("Evolución del Win Rate"), color = HextechGold, fontSize = 12.sp)
            SparklineTrendGraph(winrate, delta, showFullText = true, canvasHeight = 72,
                observed24h = observed24h, observed12h = observed12h, dailyReferences = dailyReferences,
                reference24hAt = reference24hAt, reference12hAt = reference12hAt)
        }
    }
}
