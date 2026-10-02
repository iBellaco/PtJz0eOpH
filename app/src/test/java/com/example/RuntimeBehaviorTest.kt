package com.example

import android.app.Application
import android.content.Context
import com.example.data.*
import com.example.model.*
import com.example.util.AppLanguage
import com.example.util.DynamicTranslations
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class RuntimeBehaviorTest {
    private val now = 1_790_000_000_000L

    @Test fun `support reply raises old ticket ahead of a newer creation`() {
        val old = mapOf<String, Any>("id" to "old", "timestamp" to now - 30_000,
            "conversation" to listOf(mapOf("text" to "Resposta", "senderRole" to "SUPPORT", "timestampMillis" to now)))
        val new = mapOf<String, Any>("id" to "new", "timestamp" to now - 10_000)
        assertEquals(listOf("old", "new"), InboxMessageOrder.newestFirst(listOf(new, old)).map { it["id"] })
        assertEquals(now, InboxMessageOrder.latestMessageAt(old))
    }

    @Test fun `reading or changing status does not raise a message`() {
        val old = mapOf<String, Any>("id" to "old", "created_at" to now - 30_000,
            "updatedAt" to now + 10_000, "readAt" to now + 20_000, "status" to "READ")
        val new = mapOf<String, Any>("id" to "new", "timestamp" to now)
        assertEquals("new", InboxMessageOrder.newestFirst(listOf(old, new)).first()["id"])
        assertEquals(now - 30_000, InboxMessageOrder.latestMessageAt(old))
        assertEquals(now + 30_000, InboxMessageOrder.latestMessageAt(old + ("replied_at" to now + 30_000)))
        assertEquals(now + 40_000, InboxMessageOrder.latestMessageAt(old + ("lastMessageAt" to now + 40_000)))
    }

    @Test fun `a system greeting does not invent activity or an epoch date`() {
        val unknown = mapOf<String, Any>("conversation" to listOf(mapOf("text" to "Olá",
            "senderRole" to "SYSTEM", "timestampMillis" to 1L, "isGreeting" to true)))
        assertEquals(0L, InboxMessageOrder.latestMessageAt(unknown))
        assertEquals(now, InboxMessageOrder.latestMessageAt(unknown + ("created_at" to now)))
    }

    @Test fun `administrators have every role panel including a trusted claim`() {
        for (panel in RolePanel.entries) {
            assertTrue(panel.name, RolePanelAccess.canOpen(panel, "admin"))
            assertTrue(panel.name, RolePanelAccess.canOpen(panel, "free", adminClaim = true))
            assertFalse(panel.name, RolePanelAccess.canOpen(panel, "free"))
        }
        assertTrue(RolePanelAccess.canOpen(RolePanel.STREAMER, "free", "streamer"))
        assertFalse(RolePanelAccess.canOpen(RolePanel.ADMINISTRATION, "moderador"))
        assertFalse(RolePanelAccess.canOpen(RolePanel.SPONSOR, "streamer"))
    }

    @Test fun `only administrator test mode accepts the exact Google HTTPS home page`() {
        assertNotNull(StreamChannelUrl.parse("https://www.twitch.tv/riotgames"))
        for (url in listOf("https://google.com", "https://www.google.com/")) {
            assertNull(StreamChannelUrl.parse(url))
            assertEquals("https://www.google.com", StreamChannelUrl.parse(url, allowAdminTest = true)?.url)
        }
        for (url in listOf("http://www.google.com", "https://google.com.evil.test", "https://google.com@evil.test",
            "https://user@google.com", "https://google.com:443", "https://google.com/search", "https://google.com//",
            "https://google.com?redirect=evil", "https://google.com/#test")) {
            assertNull(url, StreamChannelUrl.parse(url, allowAdminTest = true))
        }
    }

    @Test fun `approved channels are public while Google submissions remain administrator only`() {
        assertEquals("Google", StreamChannelUrl.approved("https://www.google.com")?.platform)
        assertNull(StreamChannelUrl.parse("https://www.google.com"))
        assertNull(StreamChannelUrl.approved("https://google.com.evil.test"))
        assertNotNull(StreamChannelUrl.approved("https://twitch.tv/coach_test"))
    }

    @Test fun `sponsor tags take precedence over conflicting legacy support categories`() {
        for (key in listOf("tag", "type", "category")) {
            val data = mapOf<String, Any>("tag" to "SOPORTE", "type" to "SUPPORT", "title" to "Teste", "description" to "Mensagem") + (key to "PATROCINADOR")
            assertTrue(SupportConversationPolicy.isSponsor(SupportConversationPolicy.ticketTag(data)))
            assertEquals("PATROCINADOR", SupportReportDecoder.decode("legacy", data)?.type)
            assertFalse(SupportConversationPolicy.canView("moderador", SupportConversationPolicy.ticketTag(data)))
        }
    }

    @Test fun `a conversation stays replyable across repeated turns until explicitly closed`() {
        var history = SupportConversationPolicy.initial("local", "Teste", "Mensagem inicial", now)
        assertFalse(SupportConversationPolicy.canUserReply(history, "PENDING"))
        repeat(4) { turn ->
            history = history + SupportMessageEntry(senderRole = "SUPPORT", text = "Resposta $turn", timestampMillis = now + turn + 1)
            assertTrue(SupportConversationPolicy.canUserReply(history, "READ"))
            history = history + SupportMessageEntry(senderRole = "USER", text = "Detalhes $turn", timestampMillis = now + turn + 2)
            assertTrue(SupportConversationPolicy.canUserReply(history, "PENDING"))
        }
        assertFalse(SupportConversationPolicy.canUserReply(history, "SOLVED"))
        assertFalse(SupportConversationPolicy.canUserReply(history, "CLOSED"))
    }

    @Test fun `a saved English choice is migrated and models no longer select English`() {
        val context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().putString("selected_language", "en").commit()
        AppLanguage.initialize(context)
        assertEquals("es", AppLanguage.current.value)
        assertEquals("es", context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getString("selected_language", null))
        DynamicTranslations.loadSync(context)
        WildRiftRepository.initChampions(context, forceReload = true)
        LaneRole.entries.forEach { assertEquals(it.getLocalizedName("es"), it.getLocalizedName("en")) }
        WildRiftRepository.champions.forEach { champion ->
            assertEquals(champion.getLocalizedTitle("es"), champion.getLocalizedTitle("en"))
            assertEquals(champion.getLocalizedSummary("es"), champion.getLocalizedSummary("en"))
            champion.skills.forEach {
                assertEquals(it.getLocalizedName("es"), it.getLocalizedName("en"))
                assertEquals(it.getLocalizedDescription("es"), it.getLocalizedDescription("en"))
            }
        }
        val champion = WildRiftRepository.champions.first { it.id == "garen" }
        assertEquals(WildRiftRepository.evaluateChampion(champion, LaneRole.TOP, emptyList(), emptyList(), lang = "es"),
            WildRiftRepository.evaluateChampion(champion, LaneRole.TOP, emptyList(), emptyList(), lang = "en"))
        AppLanguage.select(context, "pt")
        assertEquals("pt", AppLanguage.current.value)
    }
    @Test fun `only staff are lifetime and a premium account always has a finite deadline`() {
        for (role in listOf("admin", "moderador")) {
            assertTrue(PremiumAccessPolicy.isActive(role, null, now))
            assertTrue(PremiumAccessPolicy.isActive(role, now - 1, now))
        }
        assertFalse(PremiumAccessPolicy.isActive("premium", null, now))
        assertFalse(PremiumAccessPolicy.isActive("premium", 0L, now))
        assertFalse(PremiumAccessPolicy.isActive("premium", now, now))
        assertTrue(PremiumAccessPolicy.isActive("premium", now + 1, now))
        for (role in listOf("free", "streamer", "patrocinador", "creador", "creador_lvl2", "creador_lvl3", "creador_lvl4", "creador_lvl5")) {
            assertFalse(role, PremiumAccessPolicy.isActive(role, now + 1000, now))
        }
        assertTrue(PremiumAccessPolicy.isActive("streamer", now + 1, now, secondary = "premium"))
        assertTrue(PremiumAccessPolicy.isActive("streamer", null, now, secondary = "moderador"))
        assertFalse(PremiumAccessPolicy.isActive("admin", null, now, banned = true))
    }
    @Test fun `role transitions retain an expiry without inheriting staff lifetime`() {
        val until = now + 7 * PremiumAccessPolicy.DAY_MILLIS
        for (role in listOf("streamer", "moderador", "creador", "free", "premium"))
            assertEquals(until, PremiumAccessPolicy.deadlineForRole(role, until, now))
        assertEquals(now - 1, PremiumAccessPolicy.deadlineForRole("premium", now - 1, now))
        assertEquals(now + 30 * PremiumAccessPolicy.DAY_MILLIS, PremiumAccessPolicy.deadlineForRole("premium", 0, now))
        assertEquals(until + PremiumAccessPolicy.DAY_MILLIS, PremiumAccessPolicy.extend(until, 1, now))
        assertEquals(now + PremiumAccessPolicy.DAY_MILLIS, PremiumAccessPolicy.extend(null, 1, now))
    }
    @Test fun `pending publications expire at three hours and approved ones remain accepted`() {
        val data = mapOf<String, Any>("status" to "PENDING", "submittedAtMillis" to now, "publicationId" to "publication-test")
        val end = now + StreamerPublicationPolicy.REVIEW_WINDOW_MILLIS
        assertFalse(StreamerPublicationPolicy.isExpired(data, end - 1))
        assertTrue(StreamerPublicationPolicy.isExpired(data, end))
        assertEquals("REJECTED", StreamerPublicationPolicy.historyStatus(data, end))
        assertEquals("APPROVED", StreamerPublicationPolicy.historyStatus(data + ("status" to "APPROVED"), end))
        assertFalse(StreamerPublicationPolicy.isExpired(data + ("status" to "ENDED"), end))
        assertEquals("publication-test", StreamerPublicationPolicy.publicationId(data))
        val server = data + ("submittedAt" to com.google.firebase.Timestamp(java.util.Date(now + 1000)))
        assertEquals(end + 1000, StreamerPublicationPolicy.expiresAt(server))
    }
    @Test fun `bulk deletion eligibility excludes unread and read tickets`() {
        for (status in listOf("PENDING", "PENDIENTE", "READ", "LEIDO", "ACCEPTED", "REJECTED"))
            assertNotEquals("SOLVED", SupportTicketPresentation.status(status))
        for (status in listOf("SOLVED", "SOLUCIONADO", "CLOSED", "CERRADO", "COMPLETED"))
            assertEquals("SOLVED", SupportTicketPresentation.status(status))
    }

    @Test fun `history expires at exactly seven days using the authoritative submission time`() {
        val publication = mapOf<String, Any>("submittedAtMillis" to now, "status" to "APPROVED")
        val end = now + StreamerPublicationPolicy.HISTORY_WINDOW_MILLIS
        assertFalse(StreamerPublicationPolicy.historyExpired(publication, end - 1))
        assertTrue(StreamerPublicationPolicy.historyExpired(publication, end))
        assertTrue(StreamerPublicationPolicy.historyExpired(publication + ("status" to "REJECTED"), end))
        assertTrue(StreamerPublicationPolicy.historyExpired(publication + ("status" to "ENDED"), end))
        assertFalse(StreamerPublicationPolicy.historyExpired(emptyMap(), end))
        val server = publication + ("submittedAt" to com.google.firebase.Timestamp(java.util.Date(now + 1000)))
        assertFalse(StreamerPublicationPolicy.historyExpired(server, end))
        assertTrue(StreamerPublicationPolicy.historyExpired(server, end + 1000))
    }

}
