package com.example

import com.example.data.InboxPhoneNotificationPolicy
import org.junit.Assert.*
import org.junit.Test

class InboxPhoneNotificationPolicyTest {
    private fun message(read: Boolean = false, body: String = "Uno", time: Long = 1L) =
        mapOf<String, Any>("id" to "one", "isRead" to read, "body" to body, "timestampMillis" to time)

    @Test fun mirroredInboxCopiesAlertOnceAndCurrentReadCopySuppressesOldUnreadCopy() {
        assertEquals(1, InboxPhoneNotificationPolicy.candidates(listOf(message(), message())).size)
        assertTrue(InboxPhoneNotificationPolicy.candidates(listOf(message(), message(read = true))).isEmpty())
    }

    @Test fun changedReplyGetsANewRevisionButReadingNeverCreatesAnAlert() {
        val old = InboxPhoneNotificationPolicy.candidates(listOf(message())).values.single()
        val updated = InboxPhoneNotificationPolicy.candidates(listOf(message(body = "Dos", time = 2L))).values.single()
        assertNotEquals(old, updated)
        assertTrue(InboxPhoneNotificationPolicy.candidates(listOf(message(read = true))).isEmpty())
    }

    @Test fun deletedMessagesNeverAlertAndSupportMirrorUsesItsCanonicalConversation() {
        for (flag in listOf(mapOf("isDeleted" to true), mapOf("deleted" to true), mapOf("status" to "DELETED"))) {
            assertTrue(InboxPhoneNotificationPolicy.candidates(listOf(message() + flag)).isEmpty())
        }
        val support = message() + mapOf("reportId" to "conversation-one", "type" to "SUPPORT")
        assertEquals(setOf("support:conversation-one"), InboxPhoneNotificationPolicy.candidates(listOf(support, support + ("id" to "mirror"))).keys)
    }
}
