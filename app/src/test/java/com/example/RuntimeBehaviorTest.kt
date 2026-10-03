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
            assertEquals(panel.name, panel == RolePanel.CREATOR, RolePanelAccess.canOpen(panel, "free"))
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

    @Test fun `active history has no deadline and seven days start at actual end`() {
        val publication = mapOf<String, Any>("submittedAtMillis" to now, "status" to "APPROVED")
        val endedAt = now + 9 * 86400000L
        assertEquals(0L, StreamerPublicationPolicy.historyExpiresAt(publication))
        assertFalse(StreamerPublicationPolicy.historyExpired(publication, endedAt + 100 * 86400000L))
        val ended = publication + mapOf("status" to "ENDED", "endedAtMillis" to endedAt)
        val end = endedAt + StreamerPublicationPolicy.HISTORY_WINDOW_MILLIS
        assertFalse(StreamerPublicationPolicy.historyExpired(ended, end - 1))
        assertTrue(StreamerPublicationPolicy.historyExpired(ended, end))
        assertFalse(StreamerPublicationPolicy.historyExpired(emptyMap(), end))
    }

    @Test fun `pending and timed out histories disappear 24 hours after server submission`() {
        val data = mapOf<String, Any>("submittedAtMillis" to now - 1000,
            "submittedAt" to com.google.firebase.Timestamp(java.util.Date(now)), "status" to "PENDING")
        val deadline = now + 86400000L
        assertEquals(deadline, StreamerPublicationPolicy.historyExpiresAt(data))
        assertFalse(StreamerPublicationPolicy.historyExpired(data, deadline - 1))
        assertTrue(StreamerPublicationPolicy.historyExpired(data, deadline))
        assertEquals(deadline, StreamerPublicationPolicy.historyExpiresAt(data + mapOf("status" to "REJECTED", "rejectionReason" to "TIMEOUT")))
    }

    @Test fun `backup preserves separate publications restored after reopening and removes only expired rows`() {
        val context = RuntimeEnvironment.getApplication()
        val first = mapOf<String, Any>("publicationId" to "first", "submittedAtMillis" to now - 9 * 86400000L, "status" to "APPROVED", "channelName" to "Primeiro")
        val second = mapOf<String, Any>("publicationId" to "second", "submittedAtMillis" to now, "status" to "PENDING", "channelName" to "Segundo")
        StreamerHistoryCache.merge(context, "owner", listOf(first), now)
        assertEquals(2, StreamerHistoryCache.merge(context, "owner", listOf(second), now).size)
        assertEquals(2, StreamerHistoryCache.records(context, "owner", now).size)
        assertTrue(StreamerHistoryCache.records(context, "other", now).isEmpty())
        val ended = first + mapOf("status" to "ENDED", "endedAtMillis" to now, "clickCount" to 42L)
        StreamerHistoryCache.merge(context, "owner", listOf(ended), now)
        assertEquals(42L, (StreamerHistoryCache.records(context, "owner", now).last()["clickCount"] as Number).toLong())
        assertEquals(listOf("first"), StreamerHistoryCache.records(context, "owner", now + 86400000L).map { it["publicationId"] })
        assertTrue(StreamerHistoryCache.records(context, "owner", now + 7 * 86400000L).isEmpty())
    }

    @Test fun `notification policy includes new staff turns and respects sponsor and resolved restrictions`() {
        val pending = mapOf<String, Any>("status" to "PENDING", "tag" to "SOPORTE")
        assertTrue(UserPanelNotificationPolicy.staffNeedsAttention(pending, false))
        assertFalse(UserPanelNotificationPolicy.staffNeedsAttention(pending + ("tag" to "PATROCINADOR"), false))
        assertTrue(UserPanelNotificationPolicy.staffNeedsAttention(pending + ("tag" to "PATROCINADOR"), true))
        assertTrue(UserPanelNotificationPolicy.staffNeedsAttention(pending + mapOf("status" to "READ", "staffRead" to false), false))
        assertFalse(UserPanelNotificationPolicy.staffNeedsAttention(pending + ("status" to "SOLVED"), true))
        assertFalse(UserPanelNotificationPolicy.staffNeedsAttention(pending + mapOf("status" to "READ", "staffRead" to true), false))
    }

    @Test fun `creator panel readers cannot publish builds without a creator staff or streamer role`() {
        for (role in listOf("free", "premium", "patrocinador")) {
            assertTrue(RolePanelAccess.canOpen(RolePanel.CREATOR, role))
            assertFalse(RolePanelAccess.canCreateBuild(role))
        }
        for (role in listOf("creador", "creador_lvl2", "creador_lvl5", "streamer", "moderador", "admin")) {
            assertTrue(RolePanelAccess.canCreateBuild(role))
            if (role != "admin") assertTrue(RolePanelAccess.canCreateBuild("free", role))
        }
        assertFalse(RolePanelAccess.canOpen(RolePanel.CREATOR, "guest"))
        assertFalse(RolePanelAccess.canCreateBuild("banned", "streamer"))
    }

    @Test fun `Premium gift activates free accounts and extends history without losing older gifts`() {
        val first = PremiumGrantPolicy.apply(mapOf("role" to "free"), 1, true, now, "gift-one")
        assertEquals("premium", first["role"])
        assertEquals(now + PremiumAccessPolicy.DAY_MILLIS, first["premiumUntil"])
        assertTrue(PremiumAccessPolicy.isActiveAccount(first, now))
        val second = PremiumGrantPolicy.apply(first, 2, true, now, "gift-two")
        assertEquals(now + 3 * PremiumAccessPolicy.DAY_MILLIS, second["premiumUntil"])
        val history = second["subscriptionHistory"] as List<*>
        assertEquals(2, history.size)
        assertEquals("gift-one", (history[0] as Map<*, *>)["id"])
        assertEquals("ADMIN_GIFT", (history[1] as Map<*, *>)["source"])
        assertTrue(SubscriptionRecord(source = "ADMIN_GIFT", amount = "Regalo").isFromAdmin)
        assertFalse(SubscriptionRecord(source = "ADMIN_GIFT", amount = "Regalo").isEssenceTransaction)
    }

    @Test fun `gifts preserve a creator role and protect system lifetime administration`() {
        val creator = PremiumGrantPolicy.apply(mapOf("role" to "creador"), 1, false, now, "creator-gift")
        assertEquals("creador", creator["role"])
        assertEquals("premium", creator["secondaryRole"])
        assertTrue(PremiumAccessPolicy.isActiveAccount(creator, now))
        for (account in listOf(mapOf<String, Any>("role" to "admin"), mapOf("role" to "free", "admin" to true),
            mapOf("role" to "banned"), mapOf("role" to "creador", "secondaryRole" to "streamer"))) {
            assertTrue(runCatching { PremiumGrantPolicy.apply(account, 1, true, now, "blocked") }.isFailure)
        }
    }

    @Test fun `legacy paid access and Timestamp deadlines are classified consistently`() {
        val account = mapOf<String, Any>("role" to "free", "subscriptionPlan" to "Admin Grant (7 days)",
            "premiumUntil" to com.google.firebase.Timestamp(java.util.Date(now + 70000)))
        assertTrue(PremiumAccessPolicy.isActiveAccount(account, now))
        assertFalse(PremiumAccessPolicy.isActiveAccount(account + ("subscriptionPlan" to ""), now))
        assertFalse(PremiumAccessPolicy.isActiveAccount(account + ("banned" to true), now))
        assertFalse(PremiumAccessPolicy.isActiveAccount(account, now + 70000))
        assertEquals("0d 0h 1m 5s", PremiumAccessPolicy.remaining(now + 65000, now))
        assertTrue(PremiumAccessPolicy.isExpiringSoon(now + 3 * PremiumAccessPolicy.DAY_MILLIS, now))
        assertFalse(PremiumAccessPolicy.isExpiringSoon(now, now))
    }

    @Test fun `message mirrors and support queues count a single notification per conversation`() {
        val message = mapOf<String, Any>("id" to "one", "isRead" to false)
        assertEquals(setOf("message:one"), InboxNotificationPolicy.unreadKeys(listOf(message, message)))
        val report = message + mapOf("id" to "ticket", "type" to "SOPORTE")
        val mirror = message + mapOf("id" to "mirror", "reportId" to "ticket")
        assertEquals(setOf("support:ticket"), InboxNotificationPolicy.unreadKeys(listOf(report, mirror)) + setOf("support:ticket"))
        assertEquals(setOf("support:ticket"), InboxNotificationPolicy.unreadKeys(listOf(report, message + mapOf("id" to "ticket", "tag" to "REPORTE", "adminReply" to "Resposta"))))
        assertTrue(InboxNotificationPolicy.unreadKeys(listOf(message + ("isRead" to true), message + ("deleted" to true))).isEmpty())
        assertTrue(SupportReplyManager.isDefaultGreeting(SupportConversationPolicy.SYSTEM_GREETING))
        assertTrue(SupportReplyManager.isDefaultGreeting("Hola. El sistema ha recibido tu mensaje. El equipo de Coach te responderá aquí."))
        assertTrue(SupportConversationPolicy.SYSTEM_GREETING.contains("cuenta de juego"))
        assertTrue(SupportConversationPolicy.SYSTEM_GREETING.contains("vida personal"))
    }

}
