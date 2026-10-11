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
    @Test fun readAcknowledgementDoesNotApplyToANewReply() {
        val observed = mapOf<String, Any>("id" to "one", "timestamp" to 10L, "isRead" to false)
        assertTrue(UserMessageReadRepository.canAcknowledge(listOf(observed), observed))
        assertFalse(UserMessageReadRepository.canAcknowledge(listOf(observed + ("timestamp" to 11L)), observed))
        assertTrue(InboxNotificationPolicy.unreadKeys(listOf(observed + ("isRead" to true))).isEmpty())
    }
    @Test fun retryingAnUnconfirmedDirectActionDoesNotIssueAnotherOperation() {
        assertEquals("first", AdminDirectOperationIds.reserve("admin-grant", "first"))
        assertEquals("first", AdminDirectOperationIds.reserve("admin-grant", "second"))
        AdminDirectOperationIds.complete("admin-grant")
        assertEquals("third", AdminDirectOperationIds.reserve("admin-grant", "third"))
        AdminDirectOperationIds.complete("admin-grant")
    }
    @Test fun directPremiumRemovalDemotesOnlyPremiumAndModeratorAndRevokesSecondaryModerator() {
        for (role in listOf("premium", "moderador", "streamer", "creador", "patrocinador")) {
            val patch = AdminPremiumPolicy.patch(mapOf("role" to role, "secondaryRole" to "moderador"), 0, true, 1000L, "test")
            assertEquals(0L, patch["premiumUntil"])
            assertEquals("", patch["secondaryRole"])
            if (role in listOf("premium", "moderador")) assertEquals("free", patch["role"])
            else assertFalse(patch.containsKey("role"))
        }
    }
    @Test fun directPremiumGiftExtendsWithoutChangingTheExistingRoleAndCannotRemoveAdministrator() {
        val patch = AdminPremiumPolicy.patch(mapOf("role" to "creador", "premiumUntil" to 10000L), 7, true, 1000L, "test")
        assertEquals(10000L + 7L * 86400000L, patch["premiumUntil"])
        assertFalse(patch.containsKey("role"))
        assertThrows(IllegalArgumentException::class.java) { AdminPremiumPolicy.patch(mapOf("role" to "admin"), 0, true, 1000L, "test") }
        assertThrows(IllegalArgumentException::class.java) { AdminPremiumPolicy.patch(mapOf("role" to "free"), 3651, true, 1000L, "test") }
    }
}
