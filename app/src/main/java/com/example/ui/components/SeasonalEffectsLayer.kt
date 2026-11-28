package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SeasonalEffect
import com.example.data.SeasonalEffectsManager
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Decorative, input-transparent effects for the application activity. The
 * floating assistant renders through a separate surface and deliberately never
 * hosts this layer.
 */
@Composable
fun SeasonalEffectsLayer(modifier: Modifier = Modifier) {
    val config by SeasonalEffectsManager.config.collectAsStateWithLifecycle()
    var localDate by remember { mutableStateOf(SeasonalEffectsManager.localDate()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            localDate = SeasonalEffectsManager.localDate()
        }
    }
    val activeEffects = remember(config, localDate) { config.activeEffectsAt(localDate) }
    if (activeEffects.isEmpty()) return

    val transition = rememberInfiniteTransition(label = "seasonal_effects")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(12_000, easing = LinearEasing)),
        label = "seasonal_effects_progress"
    )

    Box(modifier = modifier.testTag("seasonal_effect_layer")) {
        if (SeasonalEffect.CHRISTMAS in activeEffects) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("seasonal_christmas_effect")
            ) {
                repeat(28) { index ->
                    val drift = ((index * 0.173f + progress * (0.18f + (index % 4) * 0.025f)) % 1f)
                    val x = ((index * 0.371f + sin(progress * 6.283f + index) * 0.025f) % 1f) * size.width
                    val y = drift * size.height
                    val radius = 1.6f + (index % 4) * 0.65f
                    val tint = if (index % 5 == 0) Color(0xFFFFD166) else Color.White
                    drawCircle(tint.copy(alpha = 0.28f + (index % 3) * 0.08f), radius, Offset(x, y))
                    if (index % 6 == 0) {
                        drawLine(
                            tint.copy(alpha = 0.22f),
                            Offset(x - radius * 2, y), Offset(x + radius * 2, y), 0.8f
                        )
                        drawLine(
                            tint.copy(alpha = 0.22f),
                            Offset(x, y - radius * 2), Offset(x, y + radius * 2), 0.8f
                        )
                    }
                }
            }
        }
        if (SeasonalEffect.NEW_YEAR in activeEffects) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("seasonal_new_year_effect")
            ) {
                val colors = listOf(Color(0xFFFFD166), Color(0xFF72E0E0), Color(0xFFFF8CB8), Color(0xFFB99CFF))
                repeat(22) { index ->
                    val arc = ((index * 0.217f + progress * (0.08f + (index % 3) * 0.02f)) % 1f)
                    val x = ((index * 0.269f + sin(progress * 6.283f + index) * 0.07f) % 1f) * size.width
                    val y = size.height * (0.07f + arc * 0.58f)
                    val length = 4f + (index % 4) * 2f
                    val angle = progress * 6.283f + index * 0.9f
                    val color = colors[index % colors.size].copy(alpha = 0.38f)
                    drawLine(
                        color,
                        Offset(x, y),
                        Offset(x + cos(angle) * length, y + sin(angle) * length),
                        strokeWidth = 1.7f
                    )
                }
            }
        }
    }
}
