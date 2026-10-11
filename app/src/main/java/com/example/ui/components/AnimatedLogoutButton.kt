package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Icon
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
import kotlinx.coroutines.delay

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
    val walk by animateFloatAsState(if (phase == 1) 34f else 0f, tween(260), label = "logout_walk")
    val doorRotation by animateFloatAsState(if (phase >= 2) 0f else 42f, tween(180), label = "logout_door")
    val shake by animateFloatAsState(if (phase == 3) 5f else 0f, tween(90), label = "logout_shake")
    val fall by animateFloatAsState(if (phase >= 4) 24f else 0f, tween(360), label = "logout_fall")
    val alpha by animateFloatAsState(if (phase >= 4) 0f else 1f, tween(360), label = "logout_fade")
    val bg = com.example.ui.theme.HextechSurfaceVariant
    val fg = com.example.ui.theme.TextPrimary
    val danger = com.example.ui.theme.DangerRed
    LaunchedEffect(startAnimation) { if (startAnimation) running = true }
    LaunchedEffect(running) {
        if (running) {
            phase = 1; delay(270); phase = 2; delay(190); phase = 3; delay(110); phase = 4; delay(380)
            onLogoutComplete()
            running = false; phase = 0
        }
    }
    Row(
        modifier = modifier.heightIn(min = 52.dp).clip(RoundedCornerShape(13.dp)).background(bg)
            .clickable(enabled = enabled && !running) {
                if (onRequestConfirmation != null) onRequestConfirmation() else running = true
            }.testTag("animated_logout_button")
            .graphicsLayer { translationX = if (phase == 3) shake else 0f },
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(width = 42.dp, height = 40.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize().graphicsLayer { translationY = fall; rotationZ = fall * 1.8f; this.alpha = alpha }) {
                val personX = size.width * .28f + walk
                drawCircle(fg, 2.dp.toPx(), androidx.compose.ui.geometry.Offset(personX, size.height * .31f))
                drawLine(fg, androidx.compose.ui.geometry.Offset(personX, size.height * .39f), androidx.compose.ui.geometry.Offset(personX, size.height * .66f), 2.dp.toPx())
                drawLine(fg, androidx.compose.ui.geometry.Offset(personX, size.height * .45f), androidx.compose.ui.geometry.Offset(personX - 4.dp.toPx(), size.height * .55f), 2.dp.toPx())
                drawLine(fg, androidx.compose.ui.geometry.Offset(personX, size.height * .45f), androidx.compose.ui.geometry.Offset(personX + 4.dp.toPx(), size.height * .55f), 2.dp.toPx())
            }
            Canvas(Modifier.align(Alignment.CenterEnd).padding(end = 5.dp).width(11.dp).height(27.dp)
                .graphicsLayer { rotationY = doorRotation; cameraDistance = 8 * density }) {
                drawRoundRect(danger.copy(alpha = .85f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()), size = size)
                drawCircle(fg, 1.dp.toPx(), androidx.compose.ui.geometry.Offset(size.width * .72f, size.height * .52f))
            }
        }
        Icon(Icons.Default.ExitToApp, null, tint = danger, modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = danger, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(Modifier.width(14.dp))
    }
}
