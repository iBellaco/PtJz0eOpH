package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


/** Cierre de sesión confirmado con una breve animación de salida antes de invocar la acción. */
@Composable
fun AnimatedLogoutButton(
    onLogoutComplete: () -> Unit,
    modifier: Modifier = Modifier,
    darkMode: Boolean = true,
    label: String = "Cerrar sesión",
    enabled: Boolean = true,
    startAnimation: Boolean = false,
    onRequestConfirmation: (() -> Unit)? = null
) {
    var running by remember { mutableStateOf(false) }
    var phase by remember { mutableIntStateOf(0) }
    val walk = animateFloatAsState(if (phase in 1..3) 1f else 0f, tween(1250), label = "logout_walk")
    val doorRotation = animateFloatAsState(if (phase >= 2) 0f else 42f, tween(450), label = "logout_door")
    val shake = animateFloatAsState(if (phase == 3) 5f else 0f, tween(180), label = "logout_shake")
    val fall = animateFloatAsState(if (phase >= 4) 24f else 0f, tween(900), label = "logout_fall")
    val alpha = animateFloatAsState(if (phase >= 4) 0f else 1f, tween(900), label = "logout_fade")
    val logoutComplete by rememberUpdatedState(onLogoutComplete)
    val bg = com.example.ui.theme.HextechSurfaceVariant
    val fg = com.example.ui.theme.TextPrimary
    val danger = com.example.ui.theme.DangerRed
    LaunchedEffect(startAnimation) { if (startAnimation) running = true }
    LaunchedEffect(running) {
        if (running) {
            phase = 1; awaitLogoutFrames(1300); phase = 2; awaitLogoutFrames(500); phase = 3; awaitLogoutFrames(200); phase = 4; awaitLogoutFrames(950)
            awaitLogoutFrames(550)
            logoutComplete()
            running = false; phase = 0
        }
    }
    Row(
        modifier = modifier.heightIn(min = 52.dp).clip(RoundedCornerShape(13.dp)).background(bg)
            .clickable(enabled = enabled && !running) {
                if (onRequestConfirmation != null) onRequestConfirmation() else running = true
            }.testTag("animated_logout_button")
            .graphicsLayer { translationX = if (phase == 3) shake.value else 0f },
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(width = 56.dp, height = 40.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize().graphicsLayer { translationY = fall.value * density; rotationZ = fall.value * 1.8f; this.alpha = if (phase in 2..3) 0f else alpha.value }) {
                val personX = size.width * .28f + (if (phase < 4) walk.value else 0f) * size.width * .44f
                drawCircle(fg, 2.dp.toPx(), androidx.compose.ui.geometry.Offset(personX, size.height * .31f))
                drawLine(fg, androidx.compose.ui.geometry.Offset(personX, size.height * .39f), androidx.compose.ui.geometry.Offset(personX, size.height * .66f), 2.dp.toPx())
                drawLine(fg, androidx.compose.ui.geometry.Offset(personX, size.height * .45f), androidx.compose.ui.geometry.Offset(personX - 4.dp.toPx(), size.height * .55f), 2.dp.toPx())
                drawLine(fg, androidx.compose.ui.geometry.Offset(personX, size.height * .45f), androidx.compose.ui.geometry.Offset(personX + 4.dp.toPx(), size.height * .55f), 2.dp.toPx())
            }
            Canvas(Modifier.align(Alignment.CenterEnd).padding(end = 5.dp).width(20.dp).height(27.dp)
                .graphicsLayer { rotationY = doorRotation.value; cameraDistance = 8 * density }) {
                drawRoundRect(danger.copy(alpha = .85f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()), size = size)
                drawCircle(fg, 1.dp.toPx(), androidx.compose.ui.geometry.Offset(size.width * .72f, size.height * .52f))
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(label, color = danger, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(Modifier.width(14.dp))
    }
}

/** Uses the frame clock so the full sequence also obeys Compose previews and tests. */
private suspend fun awaitLogoutFrames(milliseconds: Long) {
    val start = withFrameNanos { it }
    do { val elapsed = withFrameNanos { it } - start } while (elapsed < milliseconds * 1_000_000L)
}
