package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import com.example.ui.components.coachClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.theme.AppThemeManager
import com.example.ui.theme.TextMuted
import com.example.util.tr
import kotlin.math.cos
import kotlin.math.sin

/**
 * Orbe Circular Hextech / Arcano de Activación (Inspirado en Runaterra de League of Legends):
 * - Por defecto (Piltover): Orbe cristalino con anillos de energía Hextech rotativos, engranajes dorados y pulso cian/zafiro.
 * - Al cambiar de tema (Regiones de Runaterra: Noxus, Jonia, Freljord, Zaun, Demacia, Aguas Estancadas, etc.):
 *   Sus animaciones, efectos de partículas orbitales, anillos concéntricos y resplandores cambian dinámicamente según la lore y estética de la región.
 */
@Composable
fun HextechOrbButton(
    isActive: Boolean,
    onToggle: () -> Unit,
    enabled: Boolean = true,
    orbSize: Dp = 220.dp,
    modifier: Modifier = Modifier
) {
    val theme = AppThemeManager.currentTheme
    val primaryColor = theme.primary
    val primaryLight = theme.primaryLight
    val primaryDark = theme.primaryDark
    val secondaryColor = theme.secondary
    val secondaryLight = theme.secondaryLight
    val surfaceColor = theme.surface
    val bgDark = theme.background

    val infiniteTransition = rememberInfiniteTransition(label = "runeterra_orb_anim")

    // 1. Animación de pulsación rítmica del orbe (respiración de energía)
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isActive) 1.06f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isActive) 750 else 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_pulse"
    )

    // 2. Rotación continua de los anillos externos de runas/engranajes
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isActive) 6000 else 14000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_rotation"
    )

    // 3. Rotación inversa del anillo interior
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isActive) 4000 else 10000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rotation"
    )

    // 4. Intensidad del resplandor de fondo
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = if (isActive) 0.65f else 0.35f,
        targetValue = if (isActive) 0.95f else 0.60f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isActive) 750 else 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val energyTint by animateColorAsState(
        targetValue = if (isActive) primaryLight else secondaryLight,
        animationSpec = tween(400),
        label = "energy_tint"
    )

    Box(
        modifier = modifier
            .size(orbSize)
            .scale(pulseScale),
        contentAlignment = Alignment.Center
    ) {
        // ===================================================================
        // CANVAS DE EFECTOS VISUALES Y ANIMACIONES CIRCULARES POR REGIÓN
        // ===================================================================
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(outerRotation)
        ) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val radius = w / 2f - 12.dp.toPx()

            // A. Resplandor Ambiental Radial Único por Región
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (isActive) {
                        listOf(primaryColor.copy(alpha = glowAlpha), primaryLight.copy(alpha = glowAlpha * 0.4f), Color.Transparent)
                    } else {
                        listOf(secondaryColor.copy(alpha = glowAlpha * 0.6f), secondaryLight.copy(alpha = glowAlpha * 0.2f), Color.Transparent)
                    },
                    center = Offset(cx, cy),
                    radius = radius * 1.1f
                ),
                radius = radius * 1.1f,
                center = Offset(cx, cy)
            )

            // B. Anillo Exterior Gravitacional / Rúnico
            val ringColor = if (isActive) primaryLight else secondaryColor
            drawCircle(
                color = ringColor.copy(alpha = 0.5f),
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = 3.dp.toPx())
            )

            // Marcas o runas orbitales exteriores (8 nodos rúnicos)
            for (i in 0 until 8) {
                val angle = (i * 45f) * (Math.PI / 180f)
                val x = cx + (radius * 0.92f) * cos(angle).toFloat()
                val y = cy + (radius * 0.92f) * sin(angle).toFloat()
                drawCircle(
                    color = if (i % 2 == 0) secondaryLight else primaryColor,
                    radius = 3.5.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }

        // Canvas Interior con Rotación Inversa (Anillo Mágico / Mecánico)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(innerRotation)
        ) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val innerRadius = (w / 2f) * 0.72f

            // Anillo secundario arcano segmentado
            drawCircle(
                color = secondaryLight.copy(alpha = 0.4f),
                radius = innerRadius,
                center = Offset(cx, cy),
                style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f))
            )

            // Partículas energéticas orbitales interiores (4 nodos estelares)
            for (i in 0 until 4) {
                val angle = (i * 90f + 22.5f) * (Math.PI / 180f)
                val x = cx + innerRadius * cos(angle).toFloat()
                val y = cy + innerRadius * sin(angle).toFloat()
                drawCircle(
                    color = primaryLight,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }

        // ===================================================================
        // NÚCLEO CENTRAL DEL ORBE (DISEÑO CIRCULAR CON GRADIENTE REGIONAL)
        // ===================================================================
        Box(
            modifier = Modifier
                .size(orbSize * 0.68f)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = when (theme) {
                            AppTheme.PILTOVER -> listOf(surfaceColor, bgDark, Color(0xFF040A14))
                            AppTheme.NOXUS -> listOf(Color(0xFF3B0707), Color(0xFF150202), Color(0xFF050000))
                            AppTheme.JONIA -> listOf(Color(0xFF2E1A47), Color(0xFF140B24), Color(0xFF06020D))
                            AppTheme.FRELJORD -> listOf(Color(0xFF0C2233), Color(0xFF05101A), Color(0xFF02060A))
                            AppTheme.ZAUN -> listOf(Color(0xFF0A2616), Color(0xFF041209), Color(0xFF010603))
                            AppTheme.DEMACIA -> listOf(Color(0xFF102852), Color(0xFF071226), Color(0xFF02060F))
                            AppTheme.AGUAS_ESTANCADAS -> listOf(Color(0xFF381B08), Color(0xFF1A0B03), Color(0xFF070301))
                            AppTheme.ISLAS_DE_LA_SOMBRA -> listOf(Color(0xFF0C3326), Color(0xFF051711), Color(0xFF010805))
                            AppTheme.EL_VACIO -> listOf(Color(0xFF2E083B), Color(0xFF15031C), Color(0xFF07010A))
                            else -> listOf(surfaceColor, bgDark, Color(0xFF040A14))
                        }
                    )
                )
                .coachClickable(
                    enabled = enabled,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = if (enabled) ripple(bounded = true, color = energyTint) else null,
                    onClick = onToggle
                )
                .testTag("hextech_activate_button"),
            contentAlignment = Alignment.Center
        ) {
            // Anillo metálico interno del núcleo
            Box(
                modifier = Modifier
                    .fillMaxSize(0.92f)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(secondaryLight.copy(alpha = 0.3f), Color.Transparent, primaryColor.copy(alpha = 0.3f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Etiqueta de la Región Actual de Runaterra (Dinámica según el tema)
                    Text(
                        text = tr(theme.regionTag).uppercase(),
                        color = secondaryLight,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Ícono Central con Halo Luminoso
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = if (isActive) {
                                        listOf(primaryColor.copy(alpha = 0.7f), primaryDark.copy(alpha = 0.3f))
                                    } else {
                                        listOf(secondaryColor.copy(alpha = 0.5f), Color.Transparent)
                                    }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = if (!enabled) TextMuted else energyTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Texto de Acción Principal: INICIAR / DETENER / ACTIVAR
                    Text(
                        text = com.example.util.tr(if (!enabled) tr("ACTIVAR") else if (isActive) tr("DETENER") else tr("INICIAR")),
                        color = if (!enabled) TextMuted else if (isActive) Color.White else secondaryLight,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Nombre del Tema / Estado Regional
                    Text(
                        text = com.example.util.tr(if (!enabled) tr("OFF") else if (isActive) "ONLINE" else tr(theme.titleKey).uppercase()),
                        color = if (!enabled) TextMuted else if (isActive) Color(0xFF00FFC2) else TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.0.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
