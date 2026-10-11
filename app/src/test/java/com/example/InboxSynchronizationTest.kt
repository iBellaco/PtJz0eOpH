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
}
