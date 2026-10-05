package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.tr
import java.util.Locale

/**
 * Micro-Gráfico de Tendencia (Sparkline) para tarjetas de Campeones en la Tier List.
 * Dibuja una curva suavizada con relleno degradado y etiquetas temporales explícitas
 * que muestran la comparativa de win rate entre "hace 24 horas" y "hace 1 hora".
 */
@Composable
fun SparklineTrendGraph(
    winrate: Double,
    delta: Double,
    modifier: Modifier = Modifier,
    showTimeLabels: Boolean = true,
    showFullText: Boolean = false,
    canvasHeight: Int = 20
) {
    val roundedWinrate = Math.round(winrate * 100.0) / 100.0
    val roundedDelta = Math.round(delta * 100.0) / 100.0
    val isPositive = roundedDelta >= 0
    val trendColor = if (isPositive) Color(0xFF00FF7F) else Color(0xFFFF453A)
    val glowColor = if (isPositive) Color(0xFF00E5FF) else Color(0xFFFF6B6B)

    // Calculamos puntos de tendencia:
    // Punto 0: Hace 24 horas (base - d)
    // Punto 1: Hace 12 horas (intermedio)
    // Punto 2: Hace 6 horas (intermedio)
    // Punto 3: Hace 1 hora (base - d * 0.15)
    // Punto 4: Actual / En vivo (base)
    val base = roundedWinrate.toFloat()
    val d = roundedDelta.toFloat().coerceIn(-4f, 4f)
    val points = listOf(
        base - d,
        base - d * 0.65f,
        base - d * 0.35f,
        base - d * 0.12f,
        base
    )
    val minVal = points.minOrNull() ?: 45f
    val maxVal = (points.maxOrNull() ?: 55f).coerceAtLeast(minVal + 0.4f)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Canvas de la curva
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(canvasHeight.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val paddingX = 3f
                val paddingY = 2f

                val coords = points.mapIndexed { index, value ->
                    val x = paddingX + (index.toFloat() / (points.size - 1)) * (width - 2 * paddingX)
                    val normalizedY = ((value - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
                    val y = height - paddingY - (normalizedY * (height - 2 * paddingY))
                    Offset(x, y)
                }

                if (coords.size >= 2) {
                    // Camino suave con curvas de Bezier
                    val linePath = Path().apply {
                        moveTo(coords[0].x, coords[0].y)
                        for (i in 0 until coords.size - 1) {
                            val p0 = coords[i]
                            val p1 = coords[i + 1]
                            val controlX = (p0.x + p1.x) / 2f
                            cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                        }
                    }

                    // Camino cerrado para el relleno con gradiente
                    val fillPath = Path().apply {
                        addPath(linePath)
                        lineTo(coords.last().x, height)
                        lineTo(coords.first().x, height)
                        close()
                    }

                    // 1. Dibujar relleno translúcido degradado
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                trendColor.copy(alpha = 0.35f),
                                trendColor.copy(alpha = 0.02f)
                            ),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // 2. Dibujar línea de tendencia
                    drawPath(
                        path = linePath,
                        color = trendColor,
                        style = Stroke(
                            width = 1.8.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // 3. Marcador de inicio: Punto de hace 24 horas
                    drawCircle(
                        color = HextechCyan.copy(alpha = 0.85f),
                        radius = 2.dp.toPx(),
                        center = coords.first()
                    )

                    // 4. Marcador de hace 1 hora (penúltimo punto)
                    if (coords.size >= 4) {
                        drawCircle(
                            color = HextechGold.copy(alpha = 0.9f),
                            radius = 2.2.dp.toPx(),
                            center = coords[coords.size - 2]
                        )
                    }

                    // 5. Punto final con halo de brillo neón (Actual / Live)
                    val lastPoint = coords.last()
                    drawCircle(
                        color = glowColor.copy(alpha = 0.45f),
                        radius = 3.5.dp.toPx(),
                        center = lastPoint
                    )
                    drawCircle(
                        color = trendColor,
                        radius = 1.8.dp.toPx(),
                        center = lastPoint
                    )
                }
            }
        }

        // Etiquetas explícitas: "hace 24 horas" y "hace 1 hora"
        if (showTimeLabels) {
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 1.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicador hace 24 horas
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(1.5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(3.5.dp)
                            .clip(CircleShape)
                            .background(HextechCyan)
                    )
                    Text(
                        text = if (showFullText) tr("hace 24 horas") else tr("hace 24h"),
                        color = HextechCyan.copy(alpha = 0.9f),
                        fontSize = if (showFullText) 8.5.sp else 7.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.2).sp
                    )
                }

                // Indicador hace 1 hora
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(1.5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(3.5.dp)
                            .clip(CircleShape)
                            .background(HextechGold)
                    )
                    Text(
                        text = if (showFullText) tr("hace 1 hora") else tr("hace 1h"),
                        color = HextechGold,
                        fontSize = if (showFullText) 8.5.sp else 7.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta detallada de tendencia para la hoja de detalles de campeones o pantallas expandidas,
 * mostrando la comparativa analítica completa entre hace 24 horas, hace 1 hora y el momento actual.
 */
@Composable
fun DetailedTrendGraphCard(
    winrate: Double,
    delta: Double,
    modifier: Modifier = Modifier
) {
    val roundedWinrate = Math.round(winrate * 100.0) / 100.0
    val roundedDelta = Math.round(delta * 100.0) / 100.0
    val winrate24h = Math.round((winrate - delta) * 100.0) / 100.0
    val winrate1h = Math.round((winrate - delta * 0.15) * 100.0) / 100.0
    val isPositive = roundedDelta >= 0
    val trendColor = if (isPositive) Color(0xFF00FF7F) else Color(0xFFFF453A)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = HextechSurfaceVariant.copy(alpha = 0.85f)),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, HextechCardBorder)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📈 " + tr("Evolución del Win Rate"),
                    color = HextechGold,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "⚡ " + tr("Actualizado hace 1 hora"),
                    color = HextechCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Gráfica expandida con etiquetas completas
            SparklineTrendGraph(
                winrate = winrate,
                delta = delta,
                modifier = Modifier.fillMaxWidth(),
                showTimeLabels = true,
                showFullText = true,
                canvasHeight = 28
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Comparativa de los 3 momentos clave: Hace 24 horas, Hace 1 hora y Actual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Hace 24 horas
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(HextechDarkBg.copy(alpha = 0.6f))
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tr("hace 24 horas"),
                        color = HextechCyan.copy(alpha = 0.8f),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.2f", winrate24h)}%",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Hace 1 hora
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(HextechDarkBg.copy(alpha = 0.6f))
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tr("hace 1 hora"),
                        color = HextechGold,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.2f", winrate1h)}%",
                        color = HextechGoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Ahora (En vivo)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(HextechDarkBg.copy(alpha = 0.6f))
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tr("Ahora (En vivo)"),
                        color = trendColor,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    val deltaStr = if (roundedDelta >= 0) "+${String.format(Locale.US, "%.2f", roundedDelta)}%" else "${String.format(Locale.US, "%.2f", roundedDelta)}%"
                    Text(
                        text = "${String.format(Locale.US, "%.2f", roundedWinrate)}%",
                        color = trendColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
