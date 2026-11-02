package com.example

import com.example.data.AppNotice
import com.example.data.SponsorSubmission
import com.example.data.authoritativePendingRequests
import com.example.data.staleSponsorSubmissionIds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SponsorPendingStateTest {
    @Test fun cachedRowsNeverAppearAsPending() {
        val rows = listOf(mapOf<String, Any>("status" to "PENDING"))
        assertTrue(authoritativePendingRequests(fromCache = true, hasError = false, rows).isEmpty())
    }

    @Test fun serverRowsKeepOnlyActiveStatuses() {
        val rows = listOf(
            mapOf<String, Any>("status" to "PENDING"),
            mapOf<String, Any>("status" to "PROCESSING"),
            mapOf<String, Any>("status" to "COMPLETED")
        )
        assertEquals(2, authoritativePendingRequests(false, false, rows).size)
    }

    @Test fun missingOldOutboxRowsAreRemovedButFreshRowsGetAGracePeriod() {
        val old = SponsorSubmission(AppNotice(id = "old"), 500L, createdAtMillis = 1_000L)
        val fresh = SponsorSubmission(AppNotice(id = "fresh"), 500L, createdAtMillis = 99_500L)
        assertEquals(setOf("old"), staleSponsorSubmissionIds(listOf(old, fresh), emptySet(), 100_000L))
        assertEquals(emptySet<String>(), staleSponsorSubmissionIds(listOf(old), setOf("old"), 100_000L))
    }
}
