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
    @Test fun `partial snapshots preserve previous champions and reject invalid categories`() {
        assertEquals(mapOf("ahri" to "S","garen" to "B"), GlobalTierConsensus.mergeSnapshot(
            mapOf("ahri" to "A","garen" to "B","unknown" to "invalid"), mapOf("ahri" to "S")))
    }
}
