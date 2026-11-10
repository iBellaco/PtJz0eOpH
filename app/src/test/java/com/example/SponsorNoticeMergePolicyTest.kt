package com.example

import com.example.data.AppNotice
import com.example.data.mergePublicSponsorNotices
import com.example.data.shouldApplyNoticeSnapshot
import com.example.data.mergeSponsorNotices
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SponsorNoticeMergePolicyTest {
    @Test
    fun delayedCacheCannotReplaceServerConfirmedPublication() {
        assertTrue(shouldApplyNoticeSnapshot(fromCache = true, hasServerSnapshot = false))
        assertTrue(shouldApplyNoticeSnapshot(fromCache = false, hasServerSnapshot = true))
        assertFalse(shouldApplyNoticeSnapshot(fromCache = true, hasServerSnapshot = true))
    }

    @Test
    fun approvedServerRowReplacesLocallyCachedPendingRow() {
        val pending = AppNotice(id = "ad-12345678", title = "Oferta", isApproved = false, isEnabled = false)
        val approved = pending.copy(
            isApproved = true,
            isEnabled = true,
            approvedAtMillis = 100L,
            expiresAtMillis = 86_500L
        )

        val (merged, changed) = mergeSponsorNotices(listOf(pending), listOf(approved))

        assertTrue(changed)
        assertEquals(1, merged.size)
        assertTrue(merged.single().isApproved)
        assertTrue(merged.single().isEnabled)
    }

    @Test
    fun stalePendingRowCannotHideAnAlreadyPublishedNotice() {
        val approved = AppNotice(
            id = "ad-12345678",
            title = "Oferta publicada",
            isApproved = true,
            isEnabled = true,
            approvedAtMillis = 100L,
            expiresAtMillis = 86_500L
        )
        val pending = approved.copy(isApproved = false, isEnabled = false, approvedAtMillis = 0L, expiresAtMillis = 0L)

        val (merged, changed) = mergeSponsorNotices(listOf(approved), listOf(pending))

        assertFalse(changed)
        assertEquals("Oferta publicada", merged.single().title)
        assertTrue(merged.single().isApproved)
    }

    @Test
    fun dedicatedPublicDocumentOverridesPendingCacheAndDisappearsWhenDeleted() {
        val pending = AppNotice(id = "ad-12345678", title = "Pendiente", isApproved = false, isEnabled = false)
        val published = pending.copy(
            title = "Publicidad visible",
            tag = "PUBLICIDAD",
            isApproved = true,
            isEnabled = true,
            approvedAtMillis = 100L,
            expiresAtMillis = 86_500L
        )

        val visible = mergePublicSponsorNotices(listOf(pending), emptyList(), listOf(published))
        assertEquals(1, visible.size)
        assertEquals("Publicidad visible", visible.single().title)
        assertTrue(visible.single().isEnabled)

        val removed = mergePublicSponsorNotices(visible, listOf(published), emptyList())
        assertTrue(removed.isEmpty())
    }
}
