package com.example

import com.example.data.GlobalAnnouncementPolicy
import org.junit.Assert.*
import org.junit.Test

class GlobalAnnouncementPolicyTest {
    @Test fun targetedBroadcastIsVisibleOnlyInItsSelectedLanguage() {
        val spanish = mapOf<String, Any>(
            "id" to "es-1", "title" to "Aviso", "message" to "Mensaje", "active" to true
        )
        val portuguese = mapOf<String, Any>(
            "id" to "pt-1", "title" to "Aviso", "message" to "Mensagem", "active" to true
        )
        val document = mapOf<String, Any>(
            "targetedSchema" to GlobalAnnouncementPolicy.TARGETED_SCHEMA,
            "announcement_es" to spanish,
            "announcement_pt" to portuguese,
            "id" to "legacy"
        )

        assertEquals("es-1", GlobalAnnouncementPolicy.payloadForLanguage(document, "es")?.get("id"))
        assertEquals("pt-1", GlobalAnnouncementPolicy.payloadForLanguage(document, "pt-BR")?.get("id"))
    }

    @Test fun targetedSchemaNeverFallsBackToAnotherLanguage() {
        val document = mapOf<String, Any>(
            "targetedSchema" to 1L,
            "announcement_es" to mapOf("id" to "es-only", "active" to true),
            "id" to "legacy"
        )
        assertNull(GlobalAnnouncementPolicy.payloadForLanguage(document, "pt"))
        assertEquals("es-only", GlobalAnnouncementPolicy.payloadForLanguage(document, "es")?.get("id"))
    }

    @Test fun legacyBroadcastRemainsCompatibleUntilTargetedPublishingStarts() {
        val legacy = mapOf<String, Any>("id" to "legacy", "active" to true)
        assertEquals("legacy", GlobalAnnouncementPolicy.payloadForLanguage(legacy, "es")?.get("id"))
        assertEquals("legacy", GlobalAnnouncementPolicy.payloadForLanguage(legacy, "pt")?.get("id"))
    }
}
