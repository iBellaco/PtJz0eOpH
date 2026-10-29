package com.example

import com.example.data.SponsorReviewPolicy
import com.example.data.NotificationPanel
import com.example.data.PanelNotificationPolicy
import org.junit.Assert.*
import org.junit.Test

class SponsorReviewPolicyTest {
    @Test fun onlyUnresolvedUnrefundedRequestsNeedReview() {
        val pending = mapOf<String, Any>("isApproved" to false)
        assertTrue(SponsorReviewPolicy.pending(pending))
        assertFalse(SponsorReviewPolicy.pending(pending + ("isApproved" to true)))
        assertFalse(SponsorReviewPolicy.pending(pending + ("refundedAtMillis" to 123L)))
        assertFalse(SponsorReviewPolicy.pending(pending + ("status" to "REFUNDED")))
        assertFalse(SponsorReviewPolicy.pending(pending + ("status" to "REJECTED")))
        assertFalse(SponsorReviewPolicy.pending(emptyMap()))
    }

    @Test fun publicationUsesRemoteContentAndExcludesPrivateAccountingFields() {
        val data = mapOf<String, Any>("title" to "Oferta", "videoUrl" to "https://example.test/ad.mp4",
            "durationValue" to 3L, "durationUnit" to "day", "titleColor" to "#FFFFFF",
            "isApproved" to true, "userId" to "private-user", "chargedEssence" to 200L)
        val publication = SponsorReviewPolicy.publicNotice("ad-123", data)
        assertEquals("ad-123", publication["id"])
        assertEquals("Oferta", publication["title"])
        assertFalse(publication.containsKey("userId"))
        assertFalse(publication.containsKey("chargedEssence"))
        val notice = SponsorReviewPolicy.notice("ad-123", data)
        assertEquals(3, notice.durationValue)
        assertEquals("https://example.test/ad.mp4", notice.videoUrl)
        assertEquals("#FFFFFF", notice.titleColor)
    }

    @Test fun sharedQueueCountsAnAdOnceAndClearsItWhenResolved() {
        val events = setOf("sponsor:ad-123")
        val summary = PanelNotificationPolicy.combine(emptySet(), emptyMap(),
            mapOf(NotificationPanel.ADMINISTRATION to events), emptySet())
        assertEquals(1, summary.count(NotificationPanel.ADMINISTRATION))
        assertEquals(1, summary.total)
        assertEquals(0, PanelNotificationPolicy.combine(emptySet(), emptyMap(), emptyMap(), emptySet()).total)
    }
}
