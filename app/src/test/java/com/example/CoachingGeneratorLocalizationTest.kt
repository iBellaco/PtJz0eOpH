package com.example

import com.example.model.Champion
import com.example.model.ChampionSkill
import com.example.model.LaneRole
import com.example.util.CoachingGenerator
import org.junit.Assert.*
import org.junit.Test

class CoachingGeneratorLocalizationTest {
    private val champion = Champion(
        name = "Hwei",
        skills = listOf(
            ChampionSkill(slot = "1", name = "Tema: desastre", namePt = "Tema: Desastre"),
            ChampionSkill(slot = "4", name = "Desesperación en espiral", namePt = "Desespero em Espiral")
        )
    )

    @Test fun `Portuguese analysis uses localized lanes and skill names in every role`() {
        for (role in LaneRole.entries) {
            val analysis = CoachingGenerator.generateTacticalAnalysis(champion, role, "pt-BR")
            assertTrue(analysis, analysis.contains(role.getLocalizedName("pt")))
            assertTrue(analysis, analysis.contains("sua H1 (Tema: Desastre)", ignoreCase = true))
            assertTrue(analysis, analysis.contains("sua Definitiva (Desespero em Espiral)"))
            assertFalse(analysis, analysis.contains("Línea"))
            assertFalse(analysis, analysis.contains("su H1"))
            assertFalse(analysis, analysis.contains("Desesperación"))
        }
    }

    @Test fun `switching back restores the Spanish analysis`() {
        CoachingGenerator.generateTacticalAnalysis(champion, LaneRole.MID, "pt")
        val analysis = CoachingGenerator.generateTacticalAnalysis(champion, LaneRole.MID, "es")
        assertTrue(analysis.contains("Línea Central"))
        assertTrue(analysis.contains("su H1 (Tema: desastre)"))
        assertTrue(analysis.contains("su Definitiva (Desesperación en espiral)"))
        assertFalse(analysis.contains("sua H1"))
    }

    @Test fun `missing skills also use Portuguese fallbacks`() {
        val analysis = CoachingGenerator.generateTacticalAnalysis(Champion(name = "Hwei"), LaneRole.MID, "pt")
        assertTrue(analysis.contains("sua Habilidade 1 (H1)"))
        assertTrue(analysis.contains("sua Definitiva (H4)"))
        assertFalse(analysis.contains("Habilidad 1"))
    }
}
