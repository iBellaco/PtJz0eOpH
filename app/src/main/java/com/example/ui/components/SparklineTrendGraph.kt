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
 * Dibuja una curva suavizada con relleno degradado y marcadores explícitos
 * para los 3 momentos clave: hace 24 horas, hace 12 horas y el momento actual.
 */
@Composable
fun SparklineTrendGraph(
    winrate: Double,
    delta: Double,
    modifier: Modifier = Modifier,
    showTimeLabels: Boolean = true,
    showFullText: Boolean = true,
    canvasHeight: Int = 20
) {
    val roundedWinrate = Math.round(winrate * 100.0) / 100.0
    val roundedDelta = Math.round(delta * 100.0) / 100.0
    val isPositive = roundedDelta >= 0
    val trendColor = if (isPositive) Color(0xFF00FF7F) else Color(0xFFFF453A)
    val glowColor = if (isPositive) Color(0xFF00E5FF) else Color(0xFFFF6B6B)

    // Calculamos puntos de tendencia para los 3 momentos:
    // Punto 0: Hace 24 horas (base - d)
    // Punto 1: Transición 18h (base - d * 0.75)
    // Punto 2: Hace 12 horas (base - d * 0.50)
    // Punto 3: Transición 6h (base - d * 0.25)
    // Punto 4: Actual / En vivo (base)
    val base = roundedWinrate.toFloat()
    val d = roundedDelta.toFloat().coerceIn(-4f, 4f)
    val points = listOf(
        base - d,
        base - d * 0.75f,
        base - d * 0.50f,
        base - d * 0.25f,
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

                    // 3. Marcador Punto 1: Hace 24 horas (inicio)
                    drawCircle(
                        color = HextechCyan.copy(alpha = 0.9f),
                        radius = 2.dp.toPx(),
                        center = coords.first()
                    )

                    // 4. Marcador Punto 2: Hace 12 horas (punto medio)
                    if (coords.size >= 3) {
                        val midPoint = coords[coords.size / 2]
                        drawCircle(
                            color = HextechGold.copy(alpha = 0.95f),
                            radius = 2.2.dp.toPx(),
                            center = midPoint
                        )
                    }

                    // 5. Marcador Punto 3: Actual / En vivo (final con brillo neón)
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

        // Etiquetas explícitas de los 3 momentos: "hace 24 horas", "hace 12 horas", "actual"
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
                        color = HextechCyan.copy(alpha = 0.95f),
                        fontSize = if (showFullText) 6.8.sp else 6.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.3).sp,
                        maxLines = 1
                    )
                }

                // Indicador hace 12 horas
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
                        text = if (showFullText) tr("hace 12 horas") else tr("hace 12h"),
                        color = HextechGold,
                        fontSize = if (showFullText) 6.8.sp else 6.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        maxLines = 1
                    )
                }

                // Indicador actual
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(1.5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(3.5.dp)
                            .clip(CircleShape)
                            .background(trendColor)
                    )
                    Text(
                        text = if (showFullText) tr("actual") else tr("actual"),
                        color = trendColor,
                        fontSize = if (showFullText) 6.8.sp else 6.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta detallada de tendencia para la hoja de detalles de campeones o pantallas expandidas,
 * mostrando la comparativa analítica completa entre hace 24 horas, hace 12 horas y el momento actual.
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
    val winrate12h = Math.round((winrate - delta * 0.50) * 100.0) / 100.0
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
                    text = tr("Evolución del Win Rate"),
                    color = HextechGold,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = tr("Tendencia en vivo"),
                    color = HextechCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Gráfica expandida con etiquetas completas de los 3 momentos
            SparklineTrendGraph(
                winrate = winrate,
                delta = delta,
                modifier = Modifier.fillMaxWidth(),
                showTimeLabels = true,
                showFullText = true,
                canvasHeight = 28
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Comparativa de los 3 momentos clave: Hace 24 horas, Hace 12 horas y Actual
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

                // Hace 12 horas
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(HextechDarkBg.copy(alpha = 0.6f))
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tr("hace 12 horas"),
                        color = HextechGold,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.2f", winrate12h)}%",
                        color = HextechGoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Actual (En vivo)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(HextechDarkBg.copy(alpha = 0.6f))
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tr("actual"),
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

