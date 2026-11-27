package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

// Global theme toggle (compatibility bridge)
val isLightAppTheme: Boolean
    get() = !AppThemeManager.currentTheme.isDark

val HextechDarkBg: Color get() = if (AppThemeManager.isOledMode) Color(0xFF000000) else AppThemeManager.currentTheme.background
val HextechSurface: Color get() = if (AppThemeManager.isOledMode) {
    val s = AppThemeManager.currentTheme.surface
    Color(0.02f + s.red * 0.15f, 0.02f + s.green * 0.15f, 0.03f + s.blue * 0.2f, 1f)
} else AppThemeManager.currentTheme.surface.copy(alpha = if (AppThemeManager.currentTheme.isDark) 0.65f else 0.85f)
val HextechSurfaceVariant: Color get() = if (AppThemeManager.isOledMode) {
    val v = AppThemeManager.currentTheme.surfaceVariant
    Color(0.05f + v.red * 0.2f, 0.05f + v.green * 0.2f, 0.07f + v.blue * 0.25f, 1f)
} else AppThemeManager.currentTheme.surfaceVariant.copy(alpha = if (AppThemeManager.currentTheme.isDark) 0.65f else 0.85f)
val HextechCardBorder: Color get() = if (AppThemeManager.isOledMode) AppThemeManager.currentTheme.cardBorder.copy(alpha = 0.95f) else AppThemeManager.currentTheme.cardBorder

val HextechGold: Color get() = AppThemeManager.readableAccent(AppThemeManager.currentTheme.secondary)
val HextechGoldLight: Color get() = AppThemeManager.readableAccent(AppThemeManager.currentTheme.secondaryLight)
val HextechGoldDark: Color get() = AppThemeManager.currentTheme.secondaryDark
val HextechGoldGlow: Color get() = AppThemeManager.currentTheme.secondaryGlow

val HextechCyan: Color get() = AppThemeManager.readableAccent(AppThemeManager.currentTheme.primary)
val HextechCyanLight: Color get() = AppThemeManager.readableAccent(AppThemeManager.currentTheme.primaryLight)
val HextechBlue: Color get() = AppThemeManager.currentTheme.primaryDark
val HextechBlueGlow: Color get() = AppThemeManager.currentTheme.primaryGlow

val TierSPlusColor = Color(0xFF00E5FF)
val TierSColor = Color(0xFF10B981)
val TierAColor = Color(0xFFF59E0B)
val TierBColor = Color(0xFF3B82F6)
val TierCColor = Color(0xFF9CA3AF)
val TierDColor = Color(0xFFEF4444)
val DangerRed = Color(0xFFFF4655)
val DangerRedSurface: Color get() = if (isLightAppTheme) Color(0xFFFFE3E6) else Color(0xFF33141B)
val HextechGreen = Color(0xFF22C55E)
val AllyBlue = Color(0xFF00D2D3)
val AllyBlueSurface: Color get() = if (isLightAppTheme) Color(0xFFE3F9F9) else Color(0xFF0D253A)

val TextPrimary: Color get() = AppThemeManager.readablePrimary()
val TextSecondary: Color get() = AppThemeManager.readableSecondary()
val TextMuted: Color get() = AppThemeManager.readableMuted()
val TextCyan: Color get() = AppThemeManager.readableAccent(AppThemeManager.currentTheme.primary)

