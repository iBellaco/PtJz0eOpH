package com.example

import com.example.data.sync.*
import com.example.util.PublishedAppVersion
import org.junit.Assert.*
import org.junit.Test

class LiveMetaRegressionTest {
    @Test fun `rankings reads full rows including zero ban without podium markup`() {
        val html = """<table><tbody>
          <tr class="rk-row"><td><a class="rk-champ" href="/es/champions/ahri/">Ahri</a></td>
          <td><span class="rk-val--win">51.23%</span></td><td>
          <span class="rk-stat--pick">4,1%</span><span class="rk-stat--ban">0.0%</span></td></tr>
          <tr class="rk-row"><td><a class="rk-champ" href="/es/champions/akali/">Akali</a></td>
          <td><span class="rk-val--win">49.21%</span></td><td>
          <span class="rk-stat--pick">8.5%</span><span class="rk-stat--ban">20.3%</span></td></tr>
        </tbody></table>"""
        val data = RegionalTierParser.parseStats(html, "wildriftcore")
        assertEquals(2, data.size)
        assertEquals(51.23, data["ahri"]!!.winRate!!, 0.001)
        assertEquals(0.0, data["ahri"]!!.banRate!!, 0.0)
        assertEquals(4.1, data["ahri"]!!.pickRate!!, 0.0)
        assertFalse(ChampionMetaStats(winRate = 101.0).isUsable())
    }

    @Test fun `full tier tiles supply rates when the separate rankings request is unavailable`() {
        val html = """<a class="tl-champ-tile" href="/es/champions/ahri/">
            <span class="tl-champ-wr">52.7% WR</span><span class="tl-champ-pr">4.8% PR</span>
            <span class="tl-champ-br">0.0% BR</span></a>"""
        val rates = RegionalTierParser.parseStats(html, "wildriftcore")["ahri"]!!
        assertEquals(52.7, rates.winRate!!, 0.001)
        assertEquals(0.0, rates.banRate!!, 0.0)
    }

    @Test fun `a stopped poll is stale even if the last HTTP request succeeded`() {
        val now = 100_000_000L
        val healthy = MetaFreshness(tiersCheckedAt = now, ratesCheckedAt = now,
            lastChangedAt = now - 25 * 60 * 60_000L, unchangedChecks = 1500)
        assertFalse(healthy.stale(now))
        assertTrue(healthy.longUnchanged(now))
        assertTrue(healthy.stale(now + 4 * MetaFreshness.POLL_INTERVAL_MS))
        assertTrue(healthy.copy(ratesCheckedAt = 0).stale(now))
    }

    @Test fun `history records repeated polls and only returns observed 12h and 24h values`() {
        val start = 99_600_000L
        var history = emptyList<ObservedRate>()
        repeat(24 * 60 + 1) { minute ->
            history = MetaRateHistory.record(history, ObservedRate(start + minute * 60_000L,
                if (minute < 12 * 60) 51.0 else 53.0))
        }
        val now = start + 24 * 60 * 60_000L
        assertEquals(51.0, MetaRateHistory.atAge(history, now, 24)!!, 0.0)
        assertEquals(53.0, MetaRateHistory.atAge(history, now, 12)!!, 0.0)
        assertNull(MetaRateHistory.atAge(listOf(ObservedRate(now, 53.0)), now, 12))
        assertTrue(history.size <= 160)
    }

    @Test fun `monitoring coverage includes categorized champions without inventing rates`() {
        val publishedRates = (1..136).map { "champion$it" }.toSet()
        val categoryOnly = (137..142).map { "champion$it" }.toSet()
        assertEquals(142, MetaFreshness.coveredChampionCount(publishedRates + categoryOnly, publishedRates))
        assertEquals(136, publishedRates.size)
    }

    @Test fun `production release suffix compares the real build and full version`() {
        val latest = PublishedAppVersion.parse("v1.1.10.281-b997")!!
        assertEquals("1.1.10.281", latest.name)
        assertEquals(997, latest.code)
        assertTrue(latest.newerThan("1.1.10.280", 996))
        assertFalse(latest.newerThan("1.1.10.281", 997))
        assertFalse(latest.newerThan("1.1.10.282", 998))
        assertNull(PublishedAppVersion.parse("v1.1.10.282-beta"))
        assertNull(PublishedAppVersion.parse("v1.1.10.281-binvalid"))
    }
}
