package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** The platform supplies the actual cutout bounds, in portrait and landscape. */
@Composable
fun SafeDisplayContent(modifier: Modifier = Modifier, cutout: WindowInsets = WindowInsets.displayCutout,
    content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxSize().windowInsetsPadding(cutout), content = content)
}
