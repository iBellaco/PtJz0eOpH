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
import androidx.compose.ui.graphics.lerp

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    animateButtons: Boolean = false,
    content: @Composable () -> Unit
) {
    val theme = AppThemeManager.currentTheme
    val onPrimary = if (theme.isDark) theme.background else Color.White
    val colorScheme = if (theme.isDark) {
        darkColorScheme(
            primary = theme.primary,
            onPrimary = onPrimary,
            primaryContainer = theme.primaryDark,
            onPrimaryContainer = theme.primaryLight,
            secondary = theme.secondary,
            onSecondary = onPrimary,
            secondaryContainer = theme.surfaceVariant,
            onSecondaryContainer = theme.secondaryLight,
            tertiary = TierSPlusColor,
            onTertiary = onPrimary,
            background = theme.background,
            onBackground = theme.textPrimary,
            surface = theme.surface,
            onSurface = theme.textPrimary,
            surfaceVariant = theme.surfaceVariant,
            onSurfaceVariant = theme.textSecondary,
            error = DangerRed,
            onError = Color.White,
            outline = theme.cardBorder
        )
    } else {
        lightColorScheme(
            primary = theme.primary,
            onPrimary = onPrimary,
            primaryContainer = theme.surfaceVariant,
            onPrimaryContainer = theme.primaryDark,
            secondary = theme.secondary,
            onSecondary = Color.White,
            secondaryContainer = theme.surfaceVariant,
            onSecondaryContainer = theme.secondaryDark,
            tertiary = TierSPlusColor,
            onTertiary = Color.White,
            background = theme.background,
            onBackground = theme.textPrimary,
            surface = theme.surface,
            onSurface = theme.textPrimary,
            surfaceVariant = theme.surfaceVariant,
            onSurfaceVariant = theme.textSecondary,
            error = DangerRed,
            onError = Color.White,
            outline = theme.cardBorder
        )
    }

    MaterialTheme(
        colorScheme = if (theme.isApplicationTheme) colorScheme.copy(
            surfaceTint = theme.primary,
            surfaceDim = if (theme.isDark) theme.background else theme.surfaceVariant,
            surfaceBright = if (theme.isDark) theme.surfaceVariant else theme.surface,
            surfaceContainerLowest = if (theme.isDark) theme.background else theme.surface,
            surfaceContainerLow = lerp(theme.background, theme.surface, 0.5f),
            surfaceContainer = theme.surface,
            surfaceContainerHigh = lerp(theme.surface, theme.surfaceVariant, 0.5f),
            surfaceContainerHighest = theme.surfaceVariant,
            outlineVariant = theme.cardBorder,
            tertiary = if (theme.isDark) theme.primaryGlow else theme.primaryDark,
            tertiaryContainer = theme.surfaceVariant,
            onTertiaryContainer = theme.textPrimary
        ) else colorScheme,
        typography = Typography,
        content = {
            androidx.compose.runtime.CompositionLocalProvider(com.example.ui.components.LocalCoachButtonAnimation provides animateButtons) {
                content()
            }
        }
    )
}

