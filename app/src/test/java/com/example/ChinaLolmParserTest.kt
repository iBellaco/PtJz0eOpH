package com.example

import com.example.data.sync.ChinaLolmParser
import com.example.data.sync.RegionalTierParser
import com.example.model.LaneRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChinaLolmParserTest {
    @Test fun `Tencent JSON maps roles, rates and derived strength without translated names`() {
        val heroes = """{"heroList":{"10001":{"heroId":"10001"},"10002":{"heroId":"10002"}}}"""
        val ranks = """{"result":0,"data":{"1":{"1":[
            {"hero_id":"10001","position":"1","strength_level":"5","win_rate_percent":"55.10","appear_rate_percent":"4.20","forbid_rate_percent":"2.00","dtstatdate":"20261113"}],
            "2":[{"hero_id":"10002","position":"2","strength_level":"3","win_rate_percent":"51.00","appear_rate_percent":"2.00","forbid_rate_percent":"0.40","dtstatdate":"20261113"}]}}}"""
        val result = ChinaLolmParser.parse(heroes, ranks, setOf("garen", "aatrox"))
        assertEquals("S+", result.tiers["garen"])
        assertEquals(55.10, result.stats["garen"]!!.winRate!!, .001)
        assertEquals(LaneRole.MID, result.roleStats["garen"]!!.keys.single())
        assertEquals("A", result.tiers["aatrox"])
        assertEquals(20261113.toString(), result.publishedDate)
    }

    @Test fun `unknown Tencent ids are ignored and canonical aliases remain stable`() {
        val heroes = """{"heroList":{"10008":{"heroId":"10008"},"99999":{"heroId":"99999"}}}"""
        val ranks = """{"result":0,"data":{"1":{"5":[
            {"hero_id":"10008","position":"5","strength_level":"2","win_rate":"0.50","appear_rate":"0.01","forbid_rate":"0.02","dtstatdate":"20261113"},
            {"hero_id":"99999","position":"5","strength_level":"5","win_rate":"0.99","appear_rate":"0.99","forbid_rate":"0.99"}]}}}"""
        val result = ChinaLolmParser.parse(heroes, ranks, setOf("nunuwillump"))
        assertTrue(result.stats.containsKey("nunuwillump"))
        assertTrue("Unknown IDs must not enter the roster", result.stats.keys.all { it == RegionalTierParser.canonical("nunu") })
    }

    @Test fun `China exposes each published Elo bracket independently`() {
        val heroes = """{"heroList":{"10001":{"heroId":"10001"}}}"""
        val ranks = """{"result":0,"data":{
            "1":{"1":[{"hero_id":"10001","strength_level":"5","win_rate_percent":"55","appear_rate_percent":"4","forbid_rate_percent":"2","dtstatdate":"20261114"}]},
            "2":{"1":[{"hero_id":"10001","strength_level":"3","win_rate_percent":"52","appear_rate_percent":"3","forbid_rate_percent":"1","dtstatdate":"20261114"}]},
            "3":{"1":[{"hero_id":"10001","strength_level":"2","win_rate_percent":"50","appear_rate_percent":"2","forbid_rate_percent":"0.5","dtstatdate":"20261114"}]},
            "4":{"1":[{"hero_id":"10001","strength_level":"4","win_rate_percent":"54","appear_rate_percent":"5","forbid_rate_percent":"3","dtstatdate":"20261114"}]}
        }}"""
        val result = ChinaLolmParser.parse(heroes, ranks, setOf("garen"))
        assertEquals(listOf("1", "2", "3", "4"), result.eloGroups.keys.toList())
        assertEquals("Diamante+", result.eloGroups["1"]!!.label)
        assertEquals(52.0, result.eloGroups["2"]!!.stats["garen"]!!.winRate!!, .001)
        assertEquals("S", result.eloGroups["4"]!!.tiers["garen"])
    }
}
