package com.example

import com.example.data.*
import org.junit.Assert.*
import org.junit.Test

class InboxSynchronizationTest {
    @Test fun deletionRemovesEveryCopyAndKeepsUnrelatedUnreadMessages() {
        val messages = listOf(mapOf<String, Any>("id" to "target", "isRead" to false),
            mapOf("id" to "mirror", "reportId" to "target", "isRead" to false),
            mapOf("id" to "other", "isRead" to false))
        val remaining = UserMessageDeleteRepository.remaining(messages, setOf("target"))
        assertEquals(listOf(messages.last()), remaining)
        assertEquals(setOf("message:other"), InboxNotificationPolicy.unreadKeys(remaining))
    }

    @Test fun legacySupportReportCanBeMatchedByUidOrEmail() {
        assertTrue(UserMessageDeleteRepository.reportBelongsToUser(mapOf("userId" to "u1"), "u1"))
        assertTrue(UserMessageDeleteRepository.reportBelongsToUser(mapOf("userEmail" to "User@Test.com"), "u1", "user@test.com"))
        assertFalse(UserMessageDeleteRepository.reportBelongsToUser(mapOf("userEmail" to "other@test.com"), "u1", "user@test.com"))
    }
    @Test fun readAcknowledgementDoesNotApplyToANewReply() {
        val observed = mapOf<String, Any>("id" to "one", "timestamp" to 10L, "isRead" to false)
        assertTrue(UserMessageReadRepository.canAcknowledge(listOf(observed), observed))
        assertFalse(UserMessageReadRepository.canAcknowledge(listOf(observed + ("timestamp" to 11L)), observed))
        assertTrue(InboxNotificationPolicy.unreadKeys(listOf(observed + ("isRead" to true))).isEmpty())
    }
    @Test fun retryingAnUnconfirmedDirectActionDoesNotIssueAnotherOperation() {
        val disk = mutableMapOf<String, String>()
        fun process() = PendingOperationIds({ disk[it] }, { key, value -> disk[key] = value; true }, { disk.remove(it); true })
        assertEquals("first", process().reserve("admin-grant", "first"))
        assertEquals("first", process().reserve("admin-grant", "second"))
        process().complete("admin-grant")
        assertEquals("third", process().reserve("admin-grant", "third"))
        process().complete("admin-grant")
    }
    @Test fun directPremiumRemovalDemotesOnlyPremiumAndPreservesEveryOtherRole() {
        for (role in listOf("premium", "moderador", "streamer", "creador", "patrocinador")) {
            val patch = AdminPremiumPolicy.patch(mapOf("role" to role, "secondaryRole" to "moderador"), 0, true, 1000L, "test")
            assertEquals(0L, patch["premiumUntil"])
            assertFalse(patch.containsKey("secondaryRole"))
            if (role == "premium") assertEquals("free", patch["role"])
            else assertFalse(patch.containsKey("role"))
        }
    }
    @Test fun directPremiumGiftPromotesOnlyFreeAndPreservesExistingRoles() {
        val freePatch = AdminPremiumPolicy.patch(mapOf("role" to "free", "premiumUntil" to 0L), 7, true, 1000L, "free-test")
        assertEquals("premium", freePatch["role"])
        val patch = AdminPremiumPolicy.patch(mapOf("role" to "creador", "premiumUntil" to 10000L), 7, true, 1000L, "test")
        assertEquals(10000L + 7L * 86400000L, patch["premiumUntil"])
        assertFalse(patch.containsKey("role"))
        assertThrows(IllegalArgumentException::class.java) { AdminPremiumPolicy.patch(mapOf("role" to "admin"), 0, true, 1000L, "test") }
        assertThrows(IllegalArgumentException::class.java) { AdminPremiumPolicy.patch(mapOf("role" to "free"), 3651, true, 1000L, "test") }
    }
}
