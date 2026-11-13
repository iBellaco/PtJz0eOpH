package com.example

import com.example.data.sync.GlobalTierConsensus
import com.example.data.sync.RegionalTierParser
import com.example.data.sync.ChampionMetaStats
import org.junit.Assert.*
import org.junit.Test

class GlobalTierScrapingTest {
    @Test fun `active live pages parse their current fixture and real tier rows`() {
        listOf("core" to "wildriftcore", "fire" to "wildriftfire").forEach { (file, source) ->
            val html = javaClass.getResource("/tier-sources-302/$file.html")!!.readText()
            assertEquals(source, "7.3b", com.example.data.sync.PublishedStatsParser.patch(html, source))
            val tiers = RegionalTierParser.parse(html, source)
            assertTrue("$source must contain a useful champion roster, found ${tiers.size}", tiers.size >= 100)
            assertTrue(tiers.keys.none { it.contains("tierlist") || it.contains("build") })
        }
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
    @Test fun `WildRiftCore keeps independent rates for each lane`() {
        val html = """
            <section data-role="MID"><li class="tl-cell"><a class="tl-cell__name" href="/en/champions/hwei/">Hwei</a><p class="tl-cell__wr"><b>52.5</b></p><p class="tl-cell__rates">7.4% pick · 61% ban</p></li></section>
            <section data-role="SUPPORT"><li class="tl-cell"><a class="tl-cell__name" href="/en/champions/hwei/">Hwei</a><p class="tl-cell__wr"><b>54.6</b></p><p class="tl-cell__rates">1.1% pick · 76.6% ban</p></li></section>
        """.trimIndent()
        val lanes = RegionalTierParser.parseRoleStats(html, "wildriftcore")["hwei"]!!
        assertEquals(52.5, lanes[com.example.model.LaneRole.MID]!!.winRate!!, 0.001)
        assertEquals(54.6, lanes[com.example.model.LaneRole.SUPPORT]!!.winRate!!, 0.001)
        assertNotEquals(lanes[com.example.model.LaneRole.MID], lanes[com.example.model.LaneRole.SUPPORT])
    }
    @Test fun `WildRiftCore reads the current rkc rankings schema and lane from data key`() {
        val html = """
            <table><tr class="rkc-row" data-k="hwei:MID">
              <td><a class="rkc-champ" href="/en/champions/hwei/">Hwei</a></td>
              <td class="rkc-c-win"><span class="rkc-v">53.47%</span></td>
              <td class="rkc-c-pick"><span class="rkc-v">6.20%</span></td>
              <td class="rkc-c-ban"><span class="rkc-v">14.05%</span></td>
            </tr></table>
        """.trimIndent()
        val global = RegionalTierParser.parseStats(html, "wildriftcore")["hwei"]!!
        assertEquals(53.47, global.winRate!!, 0.001)
        assertEquals(6.20, global.pickRate!!, 0.001)
        assertEquals(14.05, global.banRate!!, 0.001)
        val mid = RegionalTierParser.parseRoleStats(html, "wildriftcore")["hwei"]!!
            .getValue(com.example.model.LaneRole.MID)
        assertEquals(global, mid)
    }
    @Test fun `WildRiftCore global fallback keeps the most representative repeated lane`() {
        val html = """
            <section data-role="TOP"><a class="tl-champ-tile" href="/en/champions/yone/">
              <span class="tl-champ-wr">51.0%</span><span class="tl-champ-pr">2.0%</span><span class="tl-champ-br">3.0%</span>
            </a></section>
            <section data-role="MID"><a class="tl-champ-tile" href="/en/champions/yone/">
              <span class="tl-champ-wr">53.0%</span><span class="tl-champ-pr">8.0%</span><span class="tl-champ-br">7.0%</span>
            </a></section>
            <section data-role="JUNGLE"><a class="tl-champ-tile" href="/en/champions/yone/">
              <span class="tl-champ-wr">49.0%</span><span class="tl-champ-pr">1.0%</span><span class="tl-champ-br">2.0%</span>
            </a></section>
        """.trimIndent()
        val stats = RegionalTierParser.parseStats(html, "wildriftcore")["yone"]!!
        assertEquals(53.0, stats.winRate!!, 0.001)
        assertEquals(8.0, stats.pickRate!!, 0.001)
    }
    @Test fun `WildRiftCore ALL panel remains global when later role panels repeat a champion`() {
        val html = """
            <section id="all-tier-list" data-role="ALL"><a class="tl-champ-tile" href="/en/champions/yone/">
              <span class="tl-champ-wr">49.1%</span><span class="tl-champ-pr">4.3%</span><span class="tl-champ-br">7.7%</span>
            </a></section>
            <section id="top-tier-list" data-role="TOP"><a class="tl-champ-tile" href="/en/champions/yone/">
              <span class="tl-champ-wr">50.1%</span><span class="tl-champ-pr">5.2%</span><span class="tl-champ-br">7.7%</span>
            </a></section>
            <section id="jungle-tier-list" data-role="JUNGLE"><a class="tl-champ-tile" href="/en/champions/yone/">
              <span class="tl-champ-wr">48.5%</span><span class="tl-champ-pr">2.2%</span><span class="tl-champ-br">7.7%</span>
            </a></section>
        """.trimIndent()
        val stats = RegionalTierParser.parseStats(html, "wildriftcore")["yone"]!!
        assertEquals(49.1, stats.winRate!!, 0.001)
        assertEquals(4.3, stats.pickRate!!, 0.001)
    }

    @Test fun `WildRiftCore recovers rates hidden in embedded ranking data and tile reason`() {
        val html = """
            <section id="all-tier-list">
              <a class="tl-champ-tile" href="/en/champions/zoe/">
                <span class="tl-champ-badge">A</span>
              </a>
              <a class="tl-champ-tile" href="/en/champions/bard/"
                 data-reason="Bard holds B on the current patch. Win rate 51.5% at 1.2% pick rate.">
                <span class="tl-champ-badge">B</span>
              </a>
            </section>
            <script id="tlRankData" type="application/json">
              {"b":{"0":{"MID":[["zoe",3,47.5,1.1,0.5]]}}}
            </script>
        """.trimIndent()
        val stats = RegionalTierParser.parseStats(html, "wildriftcore")
        assertEquals(47.5, stats["zoe"]!!.winRate!!, 0.001)
        assertEquals(1.1, stats["zoe"]!!.pickRate!!, 0.001)
        assertEquals(51.5, stats["bard"]!!.winRate!!, 0.001)
        assertEquals(1.2, stats["bard"]!!.pickRate!!, 0.001)
        assertNull(stats["bard"]!!.banRate)
        val roles = RegionalTierParser.parseRoleStats(html, "wildriftcore")
        assertEquals(47.5, roles["zoe"]!![com.example.model.LaneRole.MID]!!.winRate!!, 0.001)
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
