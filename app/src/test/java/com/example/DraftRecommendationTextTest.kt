package com.example

import com.example.util.DraftRecommendationText
import org.junit.Assert.assertFalse
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
}
