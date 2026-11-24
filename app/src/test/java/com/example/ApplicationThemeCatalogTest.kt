package com.example

import com.example.ui.theme.AppTheme
import com.example.util.ChampionMatchupCoaching
import com.example.model.Champion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApplicationThemeCatalogTest {
    @Test
    fun `application catalog has thirteen premium styles with custom last`() {
        val themes = AppTheme.entries.filter { it.isApplicationTheme }

        assertEquals(13, themes.size)
        assertEquals(
            listOf("WHITE", "BLACK", "AURORA", "EMBER", "SAKURA", "FOREST", "COBALT", "GRAPHITE", "MINT", "VOLT", "CANDY", "SLATE", "CUSTOM"),
            themes.map { it.id }
        )
        assertTrue(themes.all { it.regionTag == "Tema de aplicación" })
        assertTrue(themes.map { it.descKey }.distinct().size == themes.size)
        assertEquals(themes.size, themes.map { listOf(it.background, it.surface, it.primary, it.secondary) }.distinct().size)
        assertTrue(themes.first { it.id == "WHITE" }.isDark.not())
        assertTrue(themes.filter { it.id != "WHITE" }.all { it.isDark })
        assertEquals("Custom", themes.last().titleKey)
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
