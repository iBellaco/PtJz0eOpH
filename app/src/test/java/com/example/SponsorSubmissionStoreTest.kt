package com.example

import com.example.data.AppNotice
import com.example.data.SponsorSubmission
import com.example.data.SponsorSubmissionStore
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SponsorSubmissionStoreTest {
    @Test fun originalPublicationAndCostSurviveReopeningAndOtherAccountsCannotSeeThem() {
        val context = RuntimeEnvironment.getApplication()
        val store = SponsorSubmissionStore(context, "owner")
        val first = SponsorSubmission(AppNotice(id = "one", title = "Test", content = "Test"), 500L)
        val second = first.copy(notice = first.notice.copy(id = "two"))
        assertTrue(store.save(first))
        assertTrue(store.save(second))
        val reopened = SponsorSubmissionStore(context, "owner")
        assertEquals(setOf("one", "two"), reopened.read().map { it.notice.id }.toSet())
        assertEquals(500L, reopened.read().first().expectedCost)
        assertTrue(SponsorSubmissionStore(context, "other").read().isEmpty())
        assertTrue(reopened.complete("one"))
        assertEquals(listOf("two"), reopened.read().map { it.notice.id })
    }
}
