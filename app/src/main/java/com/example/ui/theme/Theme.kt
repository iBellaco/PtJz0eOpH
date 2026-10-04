package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.util.tr
import androidx.compose.ui.graphics.Color

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    animateButtons: Boolean = false,
    content: @Composable () -> Unit
) {
    val theme = AppThemeManager.currentTheme
    val colorScheme = if (theme.isDark) {
        darkColorScheme(
            primary = HextechCyan,
            onPrimary = HextechDarkBg,
            primaryContainer = HextechBlue,
            onPrimaryContainer = HextechCyanLight,
            secondary = HextechGold,
            onSecondary = HextechDarkBg,
            secondaryContainer = HextechSurfaceVariant,
            onSecondaryContainer = HextechGoldLight,
            tertiary = TierSPlusColor,
            onTertiary = HextechDarkBg,
            background = HextechDarkBg,
            onBackground = TextPrimary,
            surface = HextechSurface.copy(alpha = if (theme.isDark) 0.6f else 0.85f),
            onSurface = TextPrimary,
            surfaceVariant = HextechSurfaceVariant.copy(alpha = if (theme.isDark) 0.6f else 0.85f),
            onSurfaceVariant = TextSecondary,
            error = DangerRed,
            onError = Color.White,
            outline = HextechCardBorder
        )
    } else {
        lightColorScheme(
            primary = HextechCyan,
            onPrimary = Color.White,
            primaryContainer = HextechSurfaceVariant,
            onPrimaryContainer = HextechCyan,
            secondary = HextechGold,
            onSecondary = Color.White,
            secondaryContainer = HextechSurfaceVariant,
            onSecondaryContainer = HextechGold,
            tertiary = TierSPlusColor,
            onTertiary = Color.White,
            background = HextechDarkBg,
            onBackground = TextPrimary,
            surface = HextechSurface.copy(alpha = if (theme.isDark) 0.6f else 0.85f),
            onSurface = TextPrimary,
            surfaceVariant = HextechSurfaceVariant.copy(alpha = if (theme.isDark) 0.6f else 0.85f),
            onSurfaceVariant = TextSecondary,
            error = DangerRed,
            onError = Color.White,
            outline = HextechCardBorder
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = {
            androidx.compose.runtime.CompositionLocalProvider(com.example.ui.components.LocalCoachButtonAnimation provides animateButtons) {
                content()
            }
        }
    )
}

