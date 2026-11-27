package com.example

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.luminance
import com.example.ui.theme.AppTheme
import com.example.ui.theme.AppThemeManager
import com.example.ui.theme.NavBarColorOption
import com.example.util.ChampionMatchupCoaching
import com.example.model.Champion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class ApplicationThemeCatalogTest {
    @Test
    fun `application catalog preserves the twelve saved theme identifiers`() {
        val themes = AppTheme.entries.filter { it.isApplicationTheme }

        assertEquals(12, themes.size)
        assertEquals(
            listOf("WHITE", "BLACK", "AURORA", "EMBER", "SAKURA", "FOREST", "COBALT", "GRAPHITE", "MINT", "VOLT", "CANDY", "SLATE"),
            themes.map { it.id }
        )
        assertTrue(themes.all { it.regionTag == "Tema de aplicación" })
        assertTrue(themes.map { it.descKey }.distinct().size == themes.size)
        assertEquals(themes.size, themes.map { listOf(it.background, it.surface, it.primary, it.secondary) }.distinct().size)
        themes.forEach { assertEquals(it, AppTheme.fromId(it.id)) }
        assertTrue(themes.first { it.id == "WHITE" }.isDark.not())
        assertTrue(themes.filter { it.id != "WHITE" }.all { it.isDark })
    }

    @Test
    fun `application accents stay distinct from every other palette even when swapped`() {
        for ((index, first) in AppTheme.entries.withIndex()) {
            for (second in AppTheme.entries.drop(index + 1)) {
                if (!first.isApplicationTheme && !second.isApplicationTheme) continue
                val direct = (distance(first.primary, second.primary) + distance(first.secondary, second.secondary)) / 2
                val swapped = (distance(first.primary, second.secondary) + distance(first.secondary, second.primary)) / 2
                assertTrue("${first.id} and ${second.id} have similar accent pairs", minOf(direct, swapped) >= 0.11f)
            }
        }
    }

    private fun distance(first: Color, second: Color): Float {
        val a = first.convert(ColorSpaces.Oklab)
        val b = second.convert(ColorSpaces.Oklab)
        val l = a.red - b.red
        val u = a.green - b.green
        val v = a.blue - b.blue
        return sqrt(l * l + u * u + v * v)
    }

    @Test
    fun `application text and accents are readable on each surface`() {
        for (theme in AppTheme.entries.filter { it.isApplicationTheme }) {
            for (background in listOf(theme.background, theme.surface, theme.surfaceVariant)) {
                for (foreground in listOf(theme.primary, theme.secondary, theme.textPrimary, theme.textSecondary, theme.textMuted)) {
                    val a = background.luminance()
                    val b = foreground.luminance()
                    val contrast = (maxOf(a, b) + 0.05f) / (minOf(a, b) + 0.05f)
                    assertTrue("${theme.id} contrast ${contrast}", contrast >= 4.5f)
                }
            }
        }
    }

    @Test
    fun `regional text is adjusted when a palette would hide it`() {
        for (theme in AppTheme.entries) {
            for (background in listOf(theme.background, theme.surface, theme.surfaceVariant)) {
                for (foreground in listOf(AppThemeManager.readablePrimary(theme), AppThemeManager.readableSecondary(theme), AppThemeManager.readableMuted(theme))) {
                    val a = background.luminance()
                    val b = foreground.luminance()
                    val contrast = (maxOf(a, b) + 0.05f) / (minOf(a, b) + 0.05f)
                    assertTrue("${theme.id} adjusted contrast ${contrast}", contrast >= 3.0f)
                }
            }
        }
    }

    @Test
    fun `navigation foregrounds remain readable for every theme and color option`() {
        synchronized(AppThemeManager) {
            val previousTheme = AppThemeManager.currentTheme
            val previousOption = AppThemeManager.currentNavBarOption
            try {
                AppTheme.entries.forEach { theme ->
                    AppThemeManager.setTheme(theme)
                    NavBarColorOption.entries.forEach { option ->
                        AppThemeManager.setNavBarOption(option)
                        assertContrast(
                            "${theme.id}/${option.id} selected icon",
                            AppThemeManager.getNavBarSelectedIconColor(),
                            AppThemeManager.getNavBarSelectionContainerColor(),
                            4.5f
                        )
                        assertContrast(
                            "${theme.id}/${option.id} active surface",
                            AppThemeManager.getNavBarSelectionContainerColor(),
                            AppThemeManager.getNavBarBackgroundColor(),
                            1.5f
                        )
                        assertContrast(
                            "${theme.id}/${option.id} selected label",
                            AppThemeManager.getNavBarSelectedTextColor(),
                            AppThemeManager.getNavBarBackgroundColor(),
                            4.5f
                        )
                        assertContrast(
                            "${theme.id}/${option.id} inactive icon",
                            AppThemeManager.getNavBarUnselectedColor(),
                            AppThemeManager.getNavBarBackgroundColor(),
                            4.5f
                        )
                    }
                }
            } finally {
                AppThemeManager.setTheme(previousTheme)
                AppThemeManager.setNavBarOption(previousOption)
            }
        }
    }

    private fun assertContrast(label: String, foreground: Color, background: Color, minimum: Float) {
        val a = background.luminance()
        val b = foreground.luminance()
        val contrast = (maxOf(a, b) + 0.05f) / (minOf(a, b) + 0.05f)
        assertTrue("$label contrast $contrast", contrast >= minimum)
    }

    @Test
    fun `draft card advice uses the champion-specific catalog text`() {
        val first = Champion(id = "first", name = "First", tacticalAdvice = "Abre con una marca y rota por el río.")
        val second = Champion(id = "second", name = "Second", tacticalAdvice = "Protege la entrada del objetivo y guarda tu definitiva.")

        val firstAdvice = ChampionMatchupCoaching.draftCardAdvice(first, first.primaryRole, "es")
        val secondAdvice = ChampionMatchupCoaching.draftCardAdvice(second, second.primaryRole, "es")

        assertTrue(firstAdvice.contains("Abre con una marca"))
        assertTrue(secondAdvice.contains("Protege la entrada"))
        assertNotEquals(firstAdvice, secondAdvice)
    }
}
