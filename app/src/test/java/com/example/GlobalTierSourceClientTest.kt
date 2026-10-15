package com.example

import com.example.data.sync.GlobalScrapingSource
import com.example.data.sync.GlobalTierConsensus
import com.example.data.sync.GlobalTierSourceClient
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test

class GlobalTierSourceClientTest {
    private val known=(1..40).map { "champ$it" }.toSet()
    private fun html() = "<section id='all-tier-list'><div class='tl-tier-row' data-tier='S'>" +
        known.joinToString("") { "<a class='tl-champ-tile' href='/es/champions/$it/'>$it</a>" } + "</div></section>"
    private fun source(server: MockWebServer) = GlobalScrapingSource("wildriftcore","Test",server.url("/tierlist").toString(),"test")

    @Test fun `transient HTTP failure retries then accepts validated categories`() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(503))
            server.enqueue(MockResponse().setBody(html()))
            val result=GlobalTierSourceClient().fetch(source(server),known)
            assertTrue(result.status.isHealthy)
            assertNull(result.status.errorMessage)
            assertEquals(40,result.tiers.size)
            assertEquals(2,server.requestCount)
        }
    }
    @Test fun `HTTP denial reports its status without pretending to be healthy or retrying`() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(403))
            val result=GlobalTierSourceClient().fetch(source(server),known)
            assertFalse(result.status.isHealthy)
            assertEquals("HTTP 403",result.status.errorMessage)
            assertEquals(1,server.requestCount)
        }
    }
    @Test fun `HTTP success with error markup is still a failed source`() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("<html>Temporarily unavailable</html>"))
            val result=GlobalTierSourceClient().fetch(source(server),known)
            assertFalse(result.status.isHealthy)
            assertTrue(result.tiers.isEmpty())
            assertNotNull(result.status.errorMessage)
            assertEquals(1,server.requestCount)
        }
    }

    @Test fun `published rates keep a source usable when tier markup is missing`() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""
                <ul><li class="tl-cell"><a class="tl-cell__name" href="/es/champions/champ1/">Champ 1</a>
                <p class="tl-cell__wr"><b>53.4</b><span>%</span></p>
                <p class="tl-cell__rates">6.2% pick · 4.1% ban</p></li></ul>
            """.trimIndent()))
            val result = GlobalTierSourceClient().fetch(source(server), setOf("champ1"))
            assertTrue(result.status.isHealthy)
            assertEquals(53.4, result.stats["champ1"]?.winRate ?: 0.0, 0.001)
        }
    }
    @Test fun `core fetches complete rankings independently from tier podium`() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody(html()))
            server.enqueue(MockResponse().setBody("""
                <tr class="rk-row"><td><a class="rk-champ" href="/es/champions/champ1/">Champ 1</a></td>
                <td><span class="rk-val--win">53.47%</span></td>
                <td><span class="rk-stat--pick">6.2%</span><span class="rk-stat--ban">0%</span></td></tr>
            """.let { "<table>$it</table>" }))
            val source = GlobalScrapingSource("wildriftcore", "Test", server.url("/tierlist/").toString(), "test")
            val result = GlobalTierSourceClient().fetch(source, known)
            assertEquals(40, result.tiers.size)
            assertEquals(53.47, result.stats["champ1"]!!.winRate!!, 0.001)
            assertEquals("/tierlist/", server.takeRequest().path)
            assertEquals("/rankings/", server.takeRequest().path)
        }
    }
    @Test fun `outdated patch cannot enter the live consensus even with full champion coverage`() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("<span class='nav-patch-badge'>7.3a</span>" + html()))
            val guarded = source(server).copy(expectedPatch = "7.3b")
            val result = GlobalTierSourceClient().fetch(guarded, known)
            assertFalse(result.status.isHealthy)
            assertEquals("7.3a", result.status.publishedPatch)
            assertTrue(result.tiers.isEmpty())
            assertTrue(result.stats.isEmpty())
            assertEquals(1, server.requestCount)
        }
    }

    @Test fun `partial snapshots preserve previous champions and reject invalid categories`() {
        assertEquals(mapOf("ahri" to "S","garen" to "B"), GlobalTierConsensus.mergeSnapshot(
            mapOf("ahri" to "A","garen" to "B","unknown" to "invalid"), mapOf("ahri" to "S")))
    }
}
