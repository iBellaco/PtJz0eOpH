package com.example

import com.example.data.AppNotice
import com.example.data.SponsorReviewPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SponsorNoticeClassificationTest {
    @Test fun approvedPublicMirrorIsStillRecognizedAsSponsorNotice() {
        assertTrue(SponsorReviewPolicy.isSponsorNotice(AppNotice(tag = "PUBLICIDAD")))
        assertTrue(
            SponsorReviewPolicy.isSponsorNotice(
                AppNotice(tag = "Anuncios importantes", sponsorEmail = "owner@example.test")
            )
        )
        assertFalse(SponsorReviewPolicy.isSponsorNotice(AppNotice(tag = "Anuncios importantes")))
    }
}
