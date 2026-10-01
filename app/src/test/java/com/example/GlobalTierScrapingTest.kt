package com.example

import com.example.data.sync.GlobalTierConsensus
import com.example.data.sync.RegionalTierParser
import org.junit.Assert.*
import org.junit.Test

class GlobalTierScrapingTest {
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
    @Test fun `empty error pages and insufficient unknown champions fail validation`() {
        assertTrue(RegionalTierParser.parse("<html>Service unavailable</html>").isEmpty())
        val known=(1..100).map { "champ$it" }.toSet()
        assertTrue(GlobalTierConsensus.validated(mapOf("champ1" to "S", "unknown" to "A"),known).isEmpty())
        val full=(1..40).associate { "champ$it" to "S" }
        assertEquals(full, GlobalTierConsensus.validated(full + ("unknown" to "S+"),known))
    }
    @Test fun `a single publisher tier is preserved and invalid votes do not become A`() {
        assertEquals(mapOf("ahri" to "S"), GlobalTierConsensus.fuse(listOf(mapOf("ahri" to "S", "zed" to "invalid"))))
        assertEquals(mapOf("ahri" to "A+"), GlobalTierConsensus.fuse(listOf(mapOf("ahri" to "S"),mapOf("ahri" to "B"))))
    }
}
