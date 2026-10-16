package com.example

import com.example.data.sync.GlobalTierConsensus
import com.example.data.sync.RegionalTierParser
import com.example.data.sync.ChampionMetaStats
import org.junit.Assert.*
import org.junit.Test

class GlobalTierScrapingTest {
    @Test fun `three requested live pages parse the current patch and real tier rows`() {
        listOf("core" to "wildriftcore", "guides" to "wildriftguides", "fire" to "wildriftfire").forEach { (file, source) ->
            val html = javaClass.getResource("/tier-sources-302/$file.html")!!.readText()
            assertEquals(source, "7.3b", com.example.data.sync.PublishedStatsParser.patch(html, source))
            val tiers = RegionalTierParser.parse(html, source)
            assertTrue("$source must contain a useful champion roster, found ${tiers.size}", tiers.size >= 100)
            assertTrue(tiers.keys.none { it.contains("tierlist") || it.contains("build") })
        }
    }

    @Test fun `guides keeps each category local and does not interpret navigation as champions`() {
        val html = """<section id="mid-tier-list"><div><div><span title="Strong">S+</span></div>
            <ul><li><a href="/champion/ahri/mid">Ahri</a></li></ul></div>
            <div><div><span title="Weak">C</span></div><ul><li><a href="/champion/zed/mid">Zed</a></li></ul></div>
            </section><footer><a href="/champion/lux/mid">Lux</a></footer>"""
        assertEquals(mapOf("ahri" to "S+", "zed" to "C"), RegionalTierParser.parse(html, "wildriftguides"))
        assertTrue(RegionalTierParser.parseStats(html,"wildriftguides").isEmpty())
    }

    @Test fun `BestBuildWR dictionary tiers do not depend on a tier field per champion`() {
        val html = """<script type="application/json" id="__NEXT_DATA__">{"props":{"pageProps":{"tierData":{"champions":[{"context":"top","tiers":{"S":[{"id":55,"slug":"poppy"}],"B":[{"slug":"graves"}]}}]}}}}</script>"""
        assertEquals(mapOf("poppy" to "S", "graves" to "B"), RegionalTierParser.parse(html,"bestbuildwr"))
    }
    @Test fun `WildRiftFire closes tiers before footer champion navigation`() {
        val html = """<div class="wf-tier-list__tiers__main"><div class="tier splus"><a class="ico-holder" href="/guide/syndra">Syndra</a></div><div class="tier b"><a class="ico-holder" href="/guide/graves">Graves</a></div></div><footer><a href="/guide/ahri">Ahri</a></footer>"""
        assertEquals(mapOf("syndra" to "S+", "graves" to "B"), RegionalTierParser.parse(html,"wildriftfire"))
    }
    @Test fun `WildRiftCore uses data tier and ignores build links and duplicated role panels`() {
        val html = """<section id="all-tier-list"><div class="tl-tier-row" data-tier="S+"><a class="tl-champ-tile" href="/es/champions/nunu-and-willump/">Nunu</a><a href="/es/champions/nunu-and-willump/builds/">Build</a></div></section><section><div class="tl-tier-row" data-tier="C"><a class="tl-champ-tile" href="/es/champions/ahri/">Ahri</a></div></section>"""
        assertEquals(mapOf("nunuwillump" to "S+"), RegionalTierParser.parse(html,"wildriftcore"))
    }
    @Test fun `WildRiftCore reads published win pick and ban rates`() {
        val html = """
            <ul><li class="tl-cell">
              <a class="tl-cell__name" href="/es/champions/ahri/">Ahri</a>
              <p class="tl-cell__wr"><b>54.2</b><span>%</span></p>
              <p class="tl-cell__rates">8.1% pick · 12.4% ban</p>
            </li></ul>
        """.trimIndent()
        val stats = RegionalTierParser.parseStats(html, "wildriftcore")["ahri"]
        assertNotNull(stats)
        assertEquals(54.2, stats!!.winRate!!, 0.001)
        assertEquals(8.1, stats.pickRate!!, 0.001)
        assertEquals(12.4, stats.banRate!!, 0.001)
    }
    @Test fun `empty error pages and insufficient unknown champions fail validation`() {
        assertTrue(RegionalTierParser.parse("<html>Service unavailable</html>").isEmpty())
        val known=(1..100).map { "champ$it" }.toSet()
        assertTrue(GlobalTierConsensus.validated(mapOf("champ1" to "S", "unknown" to "A"),known).isEmpty())
        val full=(1..40).associate { "champ$it" to "S" }
        assertEquals(full, GlobalTierConsensus.validated(full + ("unknown" to "S+"),known))
    }
    @Test fun `a single publisher tier is preserved and invalid votes do not become A`() {
        assertEquals(mapOf("ahri" to "S"), GlobalTierConsensus.fuse(listOf(mapOf("ahri" to "S", "zed" to "invalid"))))
        assertEquals(mapOf("ahri" to "A"), GlobalTierConsensus.fuse(listOf(mapOf("ahri" to "S"),mapOf("ahri" to "B"))))
    }

    @Test fun `fresh published rates override the cached snapshot`() {
        val old = ChampionMetaStats(winRate = 50.0, pickRate = 2.0, banRate = 1.0)
        val fresh = ChampionMetaStats(winRate = 54.0, pickRate = 8.0, banRate = 5.0)
        val merged = GlobalTierConsensus.mergeStats(mapOf("Ahri" to old), mapOf("ahri" to fresh))
        assertEquals(1, merged.size)
        assertEquals(fresh, merged["ahri"])
    }
}
