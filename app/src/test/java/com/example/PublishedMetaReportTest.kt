package com.example

import com.example.data.sync.*
import org.junit.Assert.*
import org.junit.Test

class PublishedMetaReportTest {
    @Test fun `five publishers identify their own patch instead of accepting navigation text`() {
        assertEquals("7.3a", PublishedStatsParser.patch("""<script id="__NEXT_DATA__">{"props":{"pageProps":{"patchVersion":"7.3a"}}}</script><p>7.3b</p>""", "bestbuildwr"))
        assertEquals("7.3b", PublishedStatsParser.patch("<title>Wild Rift - v7.3b</title>", "wildrifter"))
        assertNull(PublishedStatsParser.patch("<title>Unavailable</title><p>7.3b</p>", "wildrifter"))
    }

    @Test fun `new publishers preserve named categories and zero published rates`() {
        assertEquals("nunuwillump", RegionalTierParser.canonical("nunu-amp-willump"))
        val legends = """<div><span class="relative z-10">S</span><div class="tier-list-grid"><a href="/en/guia/Ahri?role=3">Ahri</a></div></div>"""
        assertEquals("S", RegionalTierParser.parse(legends, "wildlegends")["ahri"])
        val counter = """<a href="/en/champion/ahri" data-tier="A">Ahri</a>"""
        assertEquals("A", RegionalTierParser.parse(counter, "wildriftcounter")["ahri"])
    }

    @Test fun `dated archive selects primary role in the same bracket and excludes earlier patches`() {
        val html = """<script id="wf-stats-data" type="application/json">{"patch":"7.3b","brackets":{"master":{"rows":[
        {"slug":"ahri","champion":"Ahri","role":"Mid","main":true,"win":52.4,"pick":3.5,"ban":0},
        {"slug":"ahri","champion":"Ahri","role":"Support","main":false,"win":60,"pick":0.1,"ban":0}]}}}</script>
        <script id="wf-stats-history" type="application/json">{"patches":[{"version":"7.3b","date":"2026-10-14"}],"brackets":{"master":{"Ahri (Mid)":[
        {"date":"2026-10-13","win":49,"pick":3,"ban":1},{"date":"2026-10-14","win":51,"pick":3.4,"ban":0}]}}}</script>"""
        val report = PublishedStatsParser.fire(html)
        assertEquals(52.4, report.stats["ahri"]!!.winRate!!, 0.0)
        assertEquals(52.4, report.roleStats["ahri"]!![com.example.model.LaneRole.MID]!!.winRate!!, 0.0)
        assertEquals(60.0, report.roleStats["ahri"]!![com.example.model.LaneRole.SUPPORT]!!.winRate!!, 0.0)
        assertEquals(1, report.history["ahri"]!!.size)
        assertTrue(report.history["ahri"]!!.single().daily)
        val sample = report.history["ahri"]!!.single()
        assertEquals(51.0, MetaRateHistory.atAge(listOf(sample), sample.at + 40 * 60 * 60_000L, 24)!!, 0.0)
        assertTrue(MetaRateHistory.sampleAtAge(listOf(sample), sample.at + 40 * 60 * 60_000L, 12)!!.daily)
    }

    @Test fun `hourly observations supersede daily references without inventing missing points`() {
        val now = 300_000_000L
        val daily = ObservedRate(now - 36 * 60 * 60_000L, 51.0, daily = true)
        val hourly = ObservedRate(now - 24 * 60 * 60_000L, 52.0)
        assertEquals(hourly, MetaRateHistory.sampleAtAge(listOf(daily, hourly), now, 24))
        assertNull(MetaRateHistory.atAge(listOf(ObservedRate(now, 55.0)), now, 24))
    }
}
