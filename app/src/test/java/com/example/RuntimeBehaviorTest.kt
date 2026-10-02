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
}
