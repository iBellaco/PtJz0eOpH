package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.R

@Composable
fun AnimatedSplashScreen(onSplashFinished: () -> Unit) {
    // 0: Initial (matches system splash), 1: Pulse Up, 2: Fade Out
    var animationState by remember { mutableStateOf(0) }

    val scaleAnim by animateFloatAsState(
        targetValue = when (animationState) {
            0 -> 1.0f
            1 -> 1.25f // Pulse up
            else -> 1.8f // Zoom out and fade
        },
        animationSpec = when (animationState) {
            1 -> tween(durationMillis = 600, easing = FastOutSlowInEasing)
            else -> tween(durationMillis = 500, easing = FastOutLinearInEasing)
        },
        label = "SplashScale"
    )

    val alphaAnim by animateFloatAsState(
        targetValue = if (animationState == 2) 0f else 1f,
        animationSpec = tween(durationMillis = 500, easing = LinearEasing),
        label = "SplashAlpha"
    )

    LaunchedEffect(Unit) {
        delay(200) // Hold system-like state for a moment
        animationState = 1 // Pulse
        delay(600)
        animationState = 2 // Zoom and fade out
        delay(500)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_app_logo),
            contentDescription = com.example.util.tr("App Logo"),
            modifier = Modifier
                .size(110.dp)
                .scale(scaleAnim)
                .alpha(alphaAnim)
                .clip(RoundedCornerShape(32.dp))
        )
    }
}
