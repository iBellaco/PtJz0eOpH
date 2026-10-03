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

    @Test fun `every champion recommendation agrees with the build matchup knowledge`() {
        val context = RuntimeEnvironment.getApplication()
        DynamicTranslations.loadSync(context)
        WildRiftRepository.initChampions(context, forceReload = true)
        val roster = WildRiftRepository.champions.filter { it.id != "empty" }.distinctBy { it.id }
        assertTrue("Complete champion roster", roster.size >= 142)
        var checked = 0
        val renderedTexts = linkedSetOf<String>()
        fun inspect(analysis: DraftAnalysisResult) {
            val texts = listOfNotNull(analysis.allyCompositionWarning,analysis.frontlineStatus,analysis.directMatchupWarning,analysis.directCounterBestPick) +
                analysis.recommendations.flatMap { listOf(it.advantageBadge,it.tacticalReason,it.synergyDetails,it.counterDetails,it.runes) }
            for (source in texts) {
                val localized = com.example.util.trStr("pt",source)
                assertFalse("Draft Portuguese: $localized",SpanishUiResidue.pattern.containsMatchIn(localized.replace("Lee Sin","LeeSin")))
                renderedTexts.add(localized)
            }
        }
        for (role in LaneRole.entries) inspect(WildRiftRepository.analyzeDraft(role,emptyList(),emptyList(),null,true,"pt"))
        for (enemy in roster) {
            val role = enemy.primaryRole
            val analysis = WildRiftRepository.analyzeDraft(role, emptyList(), listOf(enemy), enemy, true, "pt")
            inspect(analysis)
            assertFalse("Known opponents override blind selection", analysis.isFirstPickMode)
            val available = roster.filter { it.id != enemy.id && (it.primaryRole == role || role in it.secondaryRoles) }
            val alternatives = available.any { MatchupKnowledge.relation(it,enemy) != MatchupRelation.UNFAVORABLE }
            for (recommendation in analysis.recommendations) {
                val champion = recommendation.champion
                assertTrue(champion.primaryRole == role || role in champion.secondaryRoles)
                assertNotEquals(enemy.id,champion.id)
                if (alternatives) assertNotEquals("${champion.name} versus ${enemy.name}",MatchupRelation.UNFAVORABLE,MatchupKnowledge.relation(champion,enemy))
                val evaluation = WildRiftRepository.evaluateChampion(champion,role,emptyList(),listOf(enemy),enemy,"pt")
                assertEquals(evaluation.draftFitScore,recommendation.draftFitScore,0.001)
                assertEquals(champion.winrate,recommendation.estimatedWinrate,0.001)
                checked++
            }
            assertEquals(analysis.recommendations.map { it.champion.id }.distinct().size,analysis.recommendations.size)
        }
        java.io.File("build/reports/portuguese-rendered").apply { mkdirs() }.resolve("draft-coherence-texts.json")
            .writeText(org.json.JSONArray(renderedTexts.toList()).toString(2))
        println("DRAFT_COHERENCE_AUDIT: ${roster.size} champions; $checked lane recommendations")
    }

    @Test fun `damage profiles and rounding never invent pure true damage`() {
        val context = RuntimeEnvironment.getApplication()
        WildRiftRepository.initChampions(context,forceReload=true)
        val roster = WildRiftRepository.champions.filter { it.id != "empty" }
        for (champion in roster) {
            val percentages = DraftDamagePolicy.composition(listOf(champion))
            assertEquals(100,percentages.sum())
            assertTrue(percentages.all { it in 0..100 })
        }
        val vayne = roster.first { it.id == "vayne" }
        assertEquals(listOf(75,0,25),DraftDamagePolicy.composition(listOf(vayne)))
        val corki = roster.first { it.id == "corki" }
        assertEquals(listOf(35,65,0),DraftDamagePolicy.composition(listOf(corki)))
        assertEquals(listOf(0,0,0),DraftDamagePolicy.composition(emptyList()))
        assertEquals(100,DraftDamagePolicy.composition(listOf(vayne,corki,roster.first())).sum())
    }

    @Test fun `conflicting matchup claims are variable and never a guaranteed counter`() {
        val context = RuntimeEnvironment.getApplication()
        WildRiftRepository.initChampions(context,forceReload=true)
        val first=WildRiftRepository.champions.first().copy(advantageAgainst=listOf("rival"),counteredBy=listOf("rival"))
        val rival=first.copy(id="rival",name="Rival",advantageAgainst=emptyList(),counteredBy=emptyList())
        assertEquals(MatchupRelation.VARIABLE,MatchupKnowledge.relation(first,rival))
        assertEquals(MatchupRelation.VARIABLE,MatchupKnowledge.relation(rival,first))
    }

    @Test fun `read support clears its badge until a real followup arrives and payment remains administrator only`() {
        assertTrue(UserPanelNotificationPolicy.staffNeedsAttention(mapOf("status" to "PENDING", "staffRead" to false),true))
        assertFalse(UserPanelNotificationPolicy.staffNeedsAttention(mapOf("status" to "PENDING", "staffRead" to true),true))
        assertTrue(UserPanelNotificationPolicy.staffNeedsAttention(mapOf("status" to "READ", "staffRead" to true, "hasNewUserReply" to true),true))
        assertFalse(SupportConversationPolicy.canView("moderador","PAGO"))
        assertTrue(SupportConversationPolicy.canView("admin","PAGO"))
    }

    @Test fun `USDT address validation checks network format checksum and excludes token contracts`() {
        assertTrue(UsdtWalletPolicy.valid(UsdtNetwork.TRC20,"TJRabPrwbZy45sbavfcjinPJC18kjpRTv8"))
        assertFalse(UsdtWalletPolicy.valid(UsdtNetwork.TRC20,"TJRabPrwbZy45sbavfcjinPJC18kjpRTv9"))
        assertFalse(UsdtWalletPolicy.valid(UsdtNetwork.TRC20,"TXLAQ63Xg1NAzckPwKHvzw7CSEmLMEqcdj"))
        assertTrue(UsdtWalletPolicy.valid(UsdtNetwork.ERC20,"0x1111111111111111111111111111111111111111"))
        assertFalse(UsdtWalletPolicy.valid(UsdtNetwork.ERC20,"0x0000000000000000000000000000000000000000"))
        assertFalse(UsdtWalletPolicy.valid(UsdtNetwork.BEP20,"0xdAC17F958D2ee523a2206206994597C13D831ec7"))
    }

    @Test fun `subscription price and inherited deadlines depend on selected plan and currency`() {
        val account = mapOf<String, Any>("role" to "creador", "secondaryRole" to "streamer", "premiumUntil" to now + 1000L,
            "blueEssence" to 1200L, "orangeEssence" to 100L)
        val cases = listOf(Triple(EssencePremiumPlan.MONTHLY, EssenceCurrency.BLUE, 100L),
            Triple(EssencePremiumPlan.MONTHLY, EssenceCurrency.ORANGE, 9L),
            Triple(EssencePremiumPlan.ANNUAL, EssenceCurrency.BLUE, 1100L), Triple(EssencePremiumPlan.ANNUAL, EssenceCurrency.ORANGE, 95L))
        for ((plan, currency, price) in cases) {
            val (balance, deadline) = EssenceEconomyPolicy.purchase(account, plan, currency, now)
            assertEquals(EssenceEconomyPolicy.balance(account, currency) - price, balance)
            assertEquals(now + 1000L + plan.days * PremiumAccessPolicy.DAY_MILLIS, deadline)
        }
        assertEquals("creador", account["role"]); assertEquals("streamer", account["secondaryRole"])
    }

    @Test fun `insufficient balance and banned accounts cannot purchase or redeem`() {
        val poor = mapOf<String, Any>("role" to "free", "blueEssence" to 99L, "orangeEssence" to 8L)
        assertTrue(runCatching { EssenceEconomyPolicy.purchase(poor, EssencePremiumPlan.MONTHLY, EssenceCurrency.BLUE, now) }.isFailure)
        assertTrue(runCatching { EssenceEconomyPolicy.redeem(poor, 10) }.isFailure)
        for (amount in listOf(10L, 25L, 50L)) assertEquals(60L - amount, EssenceEconomyPolicy.redeem(poor + ("orangeEssence" to 60L), amount))
        assertTrue(runCatching { EssenceEconomyPolicy.redeem(poor + mapOf("orangeEssence" to 100L, "role" to "banned"), 10) }.isFailure)
        assertTrue(runCatching { EssenceEconomyPolicy.redeem(poor + ("orangeEssence" to 100L), 11) }.isFailure)
    }

    @Test fun `same installation and its legacy aliases consume only one slot across repeated logins`() {
        var devices = listOf("legacy-install", "another-phone", "hardware-current")
        val aliases = setOf("legacy-install", "hardware-current")
        repeat(5) { devices = DeviceSlotPolicy.register(devices, "legacy-install", aliases, false) }
        assertEquals(listOf("legacy-install", "another-phone"), devices)
        assertTrue(runCatching { DeviceSlotPolicy.register(devices, "third-phone", setOf("third-phone"), false) }.isFailure)
    }

    @Test fun `reading one revision never hides a subsequent reply`() {
        val first = mapOf<String, Any>("conversation" to listOf(mapOf("id" to "reply-one")))
        val next = mapOf<String, Any>("conversation" to listOf(mapOf("id" to "reply-one"), mapOf("id" to "reply-two")))
        assertNotEquals(PanelReadRepository.key("support:one", PanelReadRepository.revision(first)), PanelReadRepository.key("support:one", PanelReadRepository.revision(next)))
    }

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
            assertTrue(role, PremiumAccessPolicy.isActive(role, now + 1000, now))
            assertFalse(role, PremiumAccessPolicy.isActive(role, null, now))
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
        assertEquals(0L, PremiumAccessPolicy.deadlineForRole("premium", 0, now))
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
        assertEquals("free", first["role"])
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
        assertNull(creator["secondaryRole"])
        assertTrue(PremiumAccessPolicy.isActiveAccount(creator, now))
        for (account in listOf(mapOf<String, Any>("role" to "admin"), mapOf("role" to "free", "admin" to true),
            mapOf("role" to "banned"), mapOf("role" to "free", "banned" to true), mapOf("role" to "creador", "secondaryRole" to "banned"))) {
            assertTrue(runCatching { PremiumGrantPolicy.apply(account, 1, true, now, "blocked") }.isFailure)
        }
    }

    @Test fun `legacy paid access and Timestamp deadlines are classified consistently`() {
        val account = mapOf<String, Any>("role" to "free", "subscriptionPlan" to "Admin Grant (7 days)",
            "premiumUntil" to com.google.firebase.Timestamp(java.util.Date(now + 70000)))
        assertTrue(PremiumAccessPolicy.isActiveAccount(account, now))
        assertTrue(PremiumAccessPolicy.isActiveAccount(account + ("subscriptionPlan" to ""), now))
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

    @Test fun `all advertised Premium durations preserve both occupied roles and inherited deadlines`() {
        val inherited = java.time.Instant.parse("2030-11-18T19:27:00Z").toEpochMilli()
        val account = mapOf<String, Any>("role" to "creador", "secondaryRole" to "streamer",
            "premiumUntil" to com.google.firebase.Timestamp(java.util.Date(inherited)))
        assertTrue(PremiumAccessPolicy.isActiveAccount(account, now))
        for (days in listOf(1, 7, 30, 90, 365)) {
            val gift = PremiumGrantPolicy.apply(account, days, true, now, "gift-$days")
            assertEquals(inherited + days * PremiumAccessPolicy.DAY_MILLIS, gift["premiumUntil"])
            assertEquals("creador", gift["role"])
            assertEquals("streamer", gift["secondaryRole"])
            assertTrue(PremiumAccessPolicy.isActiveAccount(gift, now))
            assertEquals(days * PremiumAccessPolicy.DAY_MILLIS,
                ((gift["subscriptionHistory"] as List<*>).single() as Map<*, *>)["durationMillis"])
            val custom = PremiumGrantPolicy.apply(account, days, false, now, "custom-$days")
            assertEquals(now + days * PremiumAccessPolicy.DAY_MILLIS, custom["premiumUntil"])
            val expired = PremiumGrantPolicy.apply(account + ("premiumUntil" to now - 1), days, true, now, "expired-$days")
            assertEquals(now + days * PremiumAccessPolicy.DAY_MILLIS, expired["premiumUntil"])
        }
        for (role in listOf("free", "creador", "creador_lvl5", "streamer", "patrocinador", "premium")) {
            val changed = account + ("role" to role)
            assertTrue(PremiumAccessPolicy.isActiveAccount(changed, now))
            assertEquals(inherited, PremiumAccessPolicy.deadlineForRole(role, inherited, now))
            assertFalse(PremiumAccessPolicy.isActiveAccount(changed + ("premiumUntil" to now), now))
        }
        assertFalse(PremiumAccessPolicy.isActive("free", inherited, now, secondary = "banned"))
        for (days in listOf(0, -1, 36501)) assertTrue(runCatching {
            PremiumGrantPolicy.apply(account, days, true, now, "invalid")
        }.isFailure)
    }

    @Test fun `panel notifications route shared messages without duplicating navigation counts`() {
        val streamer = mapOf<String, Any>("id" to "streamer_review_one", "isRead" to false)
        val creator = mapOf<String, Any>("id" to "creator-one", "title" to "Límite de Suscriptores", "isRead" to false)
        val history = mapOf<String, Any>("id" to "gift-one", "panel" to "HISTORY", "isRead" to false)
        val sponsor = mapOf<String, Any>("id" to "ad-one", "panel" to "SPONSOR", "isRead" to false)
        val messages = listOf(streamer, streamer, creator, history, sponsor)
        val keys = InboxNotificationPolicy.unreadKeys(messages)
        val routes = messages.associate { "message:${it["id"]}" to com.example.data.PanelNotificationPolicy.messagePanels(it) }
        val queue = mapOf(com.example.data.NotificationPanel.SUPPORT to setOf("support:thread-one"),
            com.example.data.NotificationPanel.ADMINISTRATION to setOf("streamer:approval-one"))
        val summary = com.example.data.PanelNotificationPolicy.combine(keys, routes, queue, setOf("notice:ad-review"))
        assertEquals(7, summary.total)
        assertEquals(4, summary.count(com.example.data.NotificationPanel.INBOX))
        for (panel in listOf(com.example.data.NotificationPanel.STREAMER, com.example.data.NotificationPanel.CREATOR,
            com.example.data.NotificationPanel.HISTORY, com.example.data.NotificationPanel.SPONSOR,
            com.example.data.NotificationPanel.ADMINISTRATION, com.example.data.NotificationPanel.SUPPORT,
            com.example.data.NotificationPanel.SPONSOR_MODERATION)) assertEquals(panel.name, 1, summary.count(panel))
        val read = com.example.data.PanelNotificationPolicy.combine(emptySet(), routes, emptyMap(), emptySet())
        assertEquals(0, read.total)
        com.example.data.NotificationPanel.entries.forEach { assertEquals(0, read.count(it)) }
        assertEquals(setOf(com.example.data.NotificationPanel.INBOX), com.example.data.PanelNotificationPolicy.messagePanels(mapOf("title" to "Mensaje")))
    }

    @Test fun `essence adjustments clamp deductions and reject overflow before a commit`() {
        org.junit.Assert.assertEquals(7L, com.example.data.AdminEssenceAdjustment.delta(9, 7, true))
        org.junit.Assert.assertEquals(-9L, com.example.data.AdminEssenceAdjustment.delta(9, 30, false))
        org.junit.Assert.assertEquals(0L, com.example.data.AdminEssenceAdjustment.delta(0, 30, false))
        try { com.example.data.AdminEssenceAdjustment.delta(Long.MAX_VALUE, 1, true); org.junit.Assert.fail("Overflow must fail") } catch (_: ArithmeticException) {}
    }
    @Test fun `history restores actual old gifts and preserves their original timestamp`() {
        val message = mapOf<String, Any>("id" to "legacy-gift", "title" to "¡Recompensa de Esencias!", "timestamp" to 123456789L,
            "content" to "¡Felicidades! Se han acreditado +25 Esencias Naranjas (EN) a tu cuenta de Coach.")
        val data = com.example.data.AccountHistoryPolicy.notificationReceipt(message)!!
        val record = com.example.model.SubscriptionRecord.fromData("legacy-gift", data)!!
        org.junit.Assert.assertEquals(123456789L, record.timestamp); org.junit.Assert.assertEquals("+25 EN", record.amount)
        org.junit.Assert.assertTrue(record.isFromAdmin && record.isAddition && record.isOrangeEssence)
        org.junit.Assert.assertNull(com.example.data.AccountHistoryPolicy.notificationReceipt(message + ("title" to "Mensaje personal")))
        org.junit.Assert.assertNull(com.example.data.AccountHistoryPolicy.notificationReceipt(message - "timestamp"))
    }
    @Test fun `payment and purchased subscriptions retain their distinct history categories`() {
        val cash = com.example.model.SubscriptionRecord(source = "CASH_REDEMPTION", amount = "-25 EN", planName = "Canje de Esencia Naranja")
        val subscription = com.example.model.SubscriptionRecord(source = "ESSENCE_PURCHASE", amount = "-9 EN", planName = "Suscripción Premium mensual")
        org.junit.Assert.assertTrue(cash.isEssenceTransaction && cash.isDeduction)
        org.junit.Assert.assertFalse(subscription.isEssenceTransaction)
        org.junit.Assert.assertTrue(subscription.isFromSubscription)
    }
    @Test fun `system role colors remain fixed and creator colors no longer duplicate secondary ranks`() {
        org.junit.Assert.assertEquals(androidx.compose.ui.graphics.Color(0xFFFFD700), com.example.model.AppUserRole.ADMIN.primaryColor)
        org.junit.Assert.assertEquals(androidx.compose.ui.graphics.Color(0xFF10B981), com.example.model.AppUserRole.MODERATOR.primaryColor)
        val ranks = listOf(com.example.model.AppUserRole.ESMERALDA, com.example.model.AppUserRole.DIAMANTE, com.example.model.AppUserRole.MAESTRO,
            com.example.model.AppUserRole.GRAN_MAESTRO, com.example.model.AppUserRole.ASPIRANTE, com.example.model.AppUserRole.SOBERANO)
        for (role in listOf(com.example.model.AppUserRole.CREATOR, com.example.model.AppUserRole.CREATOR_LVL2, com.example.model.AppUserRole.CREATOR_LVL3,
            com.example.model.AppUserRole.CREATOR_LVL4, com.example.model.AppUserRole.CREATOR_LVL5, com.example.model.AppUserRole.STREAMER,
            com.example.model.AppUserRole.PATROCINADOR, com.example.model.AppUserRole.PREMIUM)) {
            org.junit.Assert.assertTrue(ranks.none { it.primaryColor == role.primaryColor })
        }
    }

    @Test fun `read markers accept a newer real mirror and reject an unseen notification`() {
        val old = mapOf<String, Any>("id" to "notice", "timestamp" to 10L, "isRead" to false)
        val current = old + ("timestamp" to 20L)
        val unseen = current + ("timestamp" to 30L)
        assertTrue(com.example.data.UserMessageReadRepository.canAcknowledge(listOf(old,current),current))
        assertFalse(com.example.data.UserMessageReadRepository.canAcknowledge(listOf(old,current,unseen),current))
        assertFalse(com.example.data.UserMessageReadRepository.canAcknowledge(emptyList(),current))
    }

}
