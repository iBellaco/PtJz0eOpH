package com.example

import com.example.ui.components.isSponsorModerationRow
import com.example.ui.components.mergeModeratorRows
import com.example.ui.components.sponsorModerationRowId
import org.junit.Assert.*
import org.junit.Test

class SponsorModerationRowTest {
    @Test fun resolvedSponsorRepresentationsShareTheSameIdAndAreRemoved() {
        val live = mapOf<String, Any>("requestType" to "SPONSOR", "id" to "ad-12345678")
        val legacy = mapOf<String, Any>("type" to "PATROCINADOR", "sponsorId" to "ad-12345678")
        val publicFeed = mapOf<String, Any>("tag" to "PUBLICIDAD", "operationId" to "ad-12345678")
        assertTrue(isSponsorModerationRow(live))
        assertTrue(isSponsorModerationRow(legacy))
        assertTrue(isSponsorModerationRow(publicFeed))
        assertEquals("ad-12345678", sponsorModerationRowId(live))
        assertEquals("ad-12345678", sponsorModerationRowId(legacy))
        assertEquals("ad-12345678", sponsorModerationRowId(publicFeed))
    }

    @Test fun ordinaryModeratorRowsRemainVisible() {
        val row = mapOf<String, Any>("requestType" to "VERIFICATION", "id" to "verify-123")
        assertFalse(isSponsorModerationRow(row))
        assertEquals("verify-123", sponsorModerationRowId(row))
    }

    @Test fun legacyLoadDoesNotEraseLiveSponsorRows() {
        val sponsor = mapOf<String, Any>("requestType" to "SPONSOR", "id" to "ad-12345678")
        val moderator = mapOf<String, Any>("requestType" to "VERIFICATION", "id" to "verify-123")
        assertEquals(listOf(sponsor, moderator), mergeModeratorRows(listOf(sponsor), listOf(moderator)))
    }
}
