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

    @Test fun `source diagnostics distinguish complete and partial rate rows`() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""
                <title>Wild Rift Tier List — v7.3f | WildRifter</title>
                <a href="/en/champion/champ1" aria-label="View Champ 1 details">
                  <span aria-label="Tier A">A</span>
                  <dl><div><dt>Win</dt><dd>51%</dd></div>
                      <div><dt>Pick</dt><dd>4%</dd></div></dl>
                </a>
            """.trimIndent()))
            val source = GlobalScrapingSource("wildrifter", "Test", server.url("/en").toString(), "test")
            val result = GlobalTierSourceClient().fetch(source, setOf("champ1"))
            assertTrue(result.status.isHealthy)
            assertEquals(0, result.status.statsCount)
            assertEquals(1, result.status.partialStatsCount)
            assertEquals(0, result.status.missingStatsCount)
            assertEquals(listOf("champ1"), result.status.partialStatIds)
        }
    }
    @Test fun `core fetches complete rankings independently from tier podium`() = runBlocking {
        MockWebServer().use { server ->
            val tierPage = html().replace(">champ1</a>", ">champ1" +
                "<span class='tl-champ-wr'>50.10%</span><span class='tl-champ-pr'>4.0%</span>" +
                "<span class='tl-champ-br'>2.0%</span></a>")
            server.enqueue(MockResponse().setBody(tierPage))
            server.enqueue(MockResponse().setBody("""
                <tr class="rkc-row" data-k="champ1:MID"><td><a class="rkc-champ" href="/es/champions/champ1/">Champ 1</a></td>
                <td class="rkc-c-win"><span class="rkc-v">53.47%</span></td>
                <td class="rkc-c-pick"><span class="rkc-v">6.2%</span></td>
                <td class="rkc-c-ban"><span class="rkc-v">0%</span></td></tr>
            """.let { "<table>$it</table>" }))
            val source = GlobalScrapingSource("wildriftcore", "Test", server.url("/tierlist/").toString(), "test")
            val result = GlobalTierSourceClient().fetch(source, known)
            assertEquals(40, result.tiers.size)
            // The explicit ALL tile is the global value; rankings contributes
            // the independent MID row without overwriting it.
            assertEquals(50.10, result.stats["champ1"]!!.winRate!!, 0.001)
            assertEquals(53.47, result.roleStats["champ1"]!![com.example.model.LaneRole.MID]!!.winRate!!, 0.001)
            assertEquals("/tierlist/", server.takeRequest().path)
            assertEquals("/rankings/", server.takeRequest().path)
        }
    }
    @Test fun `outdated patch cannot enter the live consensus even with full champion coverage`() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("<span class='nav-patch-badge'>7.3a</span>" + html()))
            val guarded = source(server).copy(expectedPatch = "7.3c")
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

    @Test fun `full roster accepts at least seventy percent rounded up`() = runBlocking {
        val roster = (1..142).map { "champ$it" }.toSet()
        for (count in listOf(99, 100, 142)) {
            MockWebServer().use { server ->
                val html = "<section id='all-tier-list'><div class='tl-tier-row' data-tier='S'>" +
                    roster.take(count).joinToString("") { "<a class='tl-champ-tile' href='/es/champions/$it/'>$it</a>" } +
                    "</div></section>"
                server.enqueue(MockResponse().setBody(html))
                val result = GlobalTierSourceClient().fetch(source(server), roster)
                assertEquals(count >= 100, result.status.isHealthy)
                assertEquals(count, result.status.rosterCount)
                if (count == 99) assertEquals("Cobertura insuficiente: 99/142 (mínimo 100)", result.status.errorMessage)
            }
        }
    }

    @Test fun `unknown and duplicate champions cannot inflate coverage`() = runBlocking {
        val roster = (1..142).map { "champ$it" }.toSet()
        MockWebServer().use { server ->
            val entries = roster.take(99) + List(80) { "champ1" } + List(80) { "unknown$it" }
            server.enqueue(MockResponse().setBody("<section id='all-tier-list'><div class='tl-tier-row' data-tier='S'>" +
                entries.joinToString("") { "<a class='tl-champ-tile' href='/es/champions/$it/'>$it</a>" } + "</div></section>"))
            val result = GlobalTierSourceClient().fetch(source(server), roster)
            assertFalse(result.status.isHealthy)
            assertEquals(99, result.status.rosterCount)
        }
    }
}
