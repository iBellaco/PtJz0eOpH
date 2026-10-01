package com.example

import com.example.data.*
import org.junit.Assert.*
import org.junit.Test

class SupportAndStreamerPolicyTest {
    @Test fun `system acknowledges a ticket once and never as a staff reply`() {
        val initial = SupportConversationPolicy.initial("ticket", "Diego", "Mi mensaje", 100L)
        assertEquals(listOf("USER", "SYSTEM"), initial.map { it.senderRole })
        assertEquals("Mi mensaje", initial.first().text)
        assertEquals("ticket_system", initial.last().id)
        val repeated = initial + SupportMessageEntry(senderRole = "SUPPORT", text = "Respuesta uno") + SupportMessageEntry(senderRole = "SUPPORT", text = "Respuesta dos")
        assertEquals(1, repeated.count { it.senderRole == "SYSTEM" })
        assertEquals(2, repeated.count { it.senderRole == "SUPPORT" })
        assertEquals(repeated, SupportConversationPolicy.decode(repeated.map { SupportConversationPolicy.encode(it) }))
    }
    @Test fun `shared read flag takes precedence over stale device state`() {
        assertTrue(SupportConversationPolicy.userHasRead(mapOf("userRead" to true, "isRead" to false, "hasNewAdminReply" to false)))
        assertFalse(SupportConversationPolicy.userHasRead(mapOf("userRead" to false, "isRead" to true)))
        assertFalse(SupportConversationPolicy.userHasRead(mapOf("userRead" to true, "hasNewAdminReply" to true)))
    }
    @Test fun `moderators see support but only administrators see sponsors`() {
        listOf("PATROCINADOR", "patrocinio", "SPONSOR", "PUBLICIDAD").forEach { tag ->
            assertFalse(SupportConversationPolicy.canView("moderador", tag))
            assertTrue(SupportConversationPolicy.canView("admin", tag))
        }
        assertTrue(SupportConversationPolicy.canView("moderador", "SOPORTE"))
        assertFalse(SupportConversationPolicy.canView("free", "SOPORTE"))
        listOf("SOLVED", "SOLUCIONADO", "RESUELTO", "CLOSED").forEach { assertTrue(SupportConversationPolicy.isClosed(it)) }
        assertFalse(SupportConversationPolicy.isClosed("READ"))
    }
    @Test fun `valid platform channels normalize to safe URLs`() {
        mapOf("https://www.tiktok.com/@coach.test/live" to "TikTok", "https://www.youtube.com/@coachcanal/live?utm_source=test" to "YouTube",
            "https://youtube.com/channel/UC1234567890123456789012" to "YouTube", "https://m.twitch.tv/coach_test/" to "Twitch", "https://kick.com/coach-test" to "Kick").forEach { (url, platform) ->
            val parsed = StreamChannelUrl.parse(url)
            assertNotNull(url, parsed)
            assertEquals(platform, parsed!!.platform)
            assertFalse(parsed.url.contains('?'))
        }
    }
    @Test fun `invalid and nonchannel URLs are rejected`() {
        listOf("http://twitch.tv/coach", "https://twitch.tv.evil.com/coach", "https://twitch.tv@evil.com/coach", "https://evil.com/?url=https://kick.com/coach",
            "https://youtube.com/watch?v=123", "https://youtu.be/123", "https://tiktok.com/video/123", "https://twitch.tv/directory", "https://kick.com/categories",
            "https://kick.com/coach:123", "https://kick.com:443/coach", "https://kick.com/coach#fragment", "https://youtube.com/@coach%2Fwatch", "javascript:alert(1)").forEach { assertNull(it, StreamChannelUrl.parse(it)) }
    }
    @Test fun `fifth approval fills capacity and a sixth approval cannot publish`() {
        var entries = (1..4).map { mapOf<String, Any>("userId" to "u$it") }
        assertTrue(StreamerPublicationPolicy.canRequest(entries, "u5"))
        assertFalse(StreamerPublicationPolicy.canRequest(entries, "u1"))
        entries = StreamerPublicationPolicy.approve(entries, mapOf("userId" to "u5"))
        assertEquals(5, entries.size)
        assertFalse(StreamerPublicationPolicy.canRequest(entries, "u6"))
        try { StreamerPublicationPolicy.approve(entries, mapOf("userId" to "u6")); fail("Sixth publication accepted") } catch (_: IllegalStateException) { }
        assertTrue(StreamerPublicationPolicy.canRequest(entries.filterNot { it["userId"] == "u3" }, "u6"))
    }
}
