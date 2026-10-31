package com.example

import com.example.data.SupportConversationPolicy
import com.example.data.SupportLanguageDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportLanguageDetectorTest {
    @Test fun `detects supported message language and localizes automatic greeting`() {
        assertEquals("pt", SupportLanguageDetector.detect("Erro no aplicativo", "Preciso de ajuda porque a tela não abre", "es"))
        assertEquals("es", SupportLanguageDetector.detect("Error en la aplicación", "Necesito ayuda porque la pantalla no abre", "pt"))
        val conversation = SupportConversationPolicy.initial("ticket", "Tester", "Preciso de ajuda", 1L, language = "pt")
        assertEquals(SupportConversationPolicy.PORTUGUESE_SYSTEM_GREETING, conversation[1].text)
        assertTrue(conversation[1].isGreeting)
    }
}
