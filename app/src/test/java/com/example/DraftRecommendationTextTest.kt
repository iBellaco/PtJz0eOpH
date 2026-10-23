package com.example

import com.example.util.DraftRecommendationText
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DraftRecommendationTextTest {
    @Test fun `removes balance history sentences from draft copy`() {
        val cleaned = DraftRecommendationText.clean(
            "Controla la zona. Parche 7.3c: bajaron el daño y el alcance. Conserva la salida."
        )
        assertTrue(cleaned.contains("Controla la zona"))
        assertTrue(cleaned.contains("Conserva la salida"))
        assertFalse(cleaned.contains("Parche"))
    }

    @Test fun `keeps useful advice after balance clauses in both languages`() {
        listOf(
            "Hwei fue nerfeado; controla la zona.",
            "Hwei recebeu buffs; controle a zona.",
            "Atualização de balanceamento: mudou o alcance; controle a zona.",
            "Cambios de balance: subieron el daño; controla la zona.",
            "7.3c: bajaron el escudo; controla la zona."
        ).forEach { input ->
            val output = DraftRecommendationText.clean(input)
            assertTrue(input, output.contains("control", ignoreCase = true))
            assertFalse(input, output.contains("Hwei"))
            assertFalse(input, output.contains("7.3c"))
            assertFalse(input, output.contains("balance", ignoreCase = true))
        }
    }

    @Test fun `inflected buffs and nerfs do not leak into champion advice`() {
        listOf("buffado", "buffada", "buffeado", "buffeada", "nerfado", "nerfada", "nerfeado", "nerfeada", "buffed", "nerfed")
            .forEach { term -> assertEquals("Conserva la salida.", DraftRecommendationText.clean("Hwei $term. Conserva la salida.")) }
    }

    @Test fun `preserves decimal timings ability labels and jungle resources`() {
        val advice = "Encadena H1 y H4 en 0.5 s. Controla el buff rojo. Controle o buff azul."
        assertEquals(advice, DraftRecommendationText.clean(advice))
        assertEquals("Usa H3 a 0,5 s.", DraftRecommendationText.clean("Usa H3 a 0,5 s."))
    }

    @Test fun `removes duplicated sentences and markup without removing strategy`() {
        assertEquals("Controla la zona. Conserva la salida.",
            DraftRecommendationText.clean("<b>Controla la zona.</b> Controla la zona.\nConserva la salida."))
    }

    @Test fun `filters balance verbs but retains tactical damage reduction`() {
        assertEquals("Reduce el daño recibido con H2.",
            DraftRecommendationText.clean("Bajaron el daño de H1. Aumentou o alcance. Reduce el daño recibido con H2."))
    }

    @Test fun `cleaning is idempotent and empty balance notes stay empty`() {
        val clean = DraftRecommendationText.clean("Parche 7.3c. Mantén la distancia; guarda H4.")
        assertEquals(clean, DraftRecommendationText.clean(clean))
        assertEquals("", DraftRecommendationText.clean("Parche 7.3c: buffeado."))
    }
}
