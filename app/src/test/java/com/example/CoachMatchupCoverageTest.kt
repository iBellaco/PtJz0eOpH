package com.example

import com.example.model.Champion
import com.example.model.DamageType
import com.example.model.LaneRole
import com.example.util.CoachMatchupRanking
import com.example.util.MatchupRoleResult
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class CoachMatchupCoverageTest {
    private fun strings(json: JSONObject, key: String): List<String> {
        val array = json.optJSONArray(key) ?: return emptyList()
        return (0 until array.length()).map { array.getString(it) }
    }
    private fun catalog(): List<Champion> {
        val raw = File("src/main/res/raw").takeIf { it.isDirectory } ?: File("app/src/main/res/raw")
        return (1..2).flatMap { part ->
            val array = JSONArray(File(raw,"champions_part$part.json").readText())
            (0 until array.length()).map { i ->
                val c = array.getJSONObject(i)
                Champion(id=c.getString("id"), name=c.getString("name"),
                    primaryRole=LaneRole.valueOf(c.getString("primaryRole")),
                    secondaryRoles=strings(c,"secondaryRoles").map(LaneRole::valueOf),
                    damageType=DamageType.valueOf(c.getString("damageType")),
                    isRanged=c.optBoolean("isRanged"), isFrontline=c.optBoolean("isFrontline"))
            }
        }
    }
    @Test fun `all bundled champions and playable roles have twelve unique relationships per category`() {
        val champions=catalog()
        assertEquals(142,champions.size)
        var profiles=0
        for (champion in champions) for (role in listOf(champion.primaryRole)+champion.secondaryRoles) {
            val result=CoachMatchupRanking.complete(champion,role,MatchupRoleResult(emptyList(),emptyList(),emptyList()),champions)
            for (list in listOf(result.advantages,result.counters,result.synergies)) {
                assertEquals("${champion.id} $role",12,list.size)
                assertEquals(list.size,list.distinct().size)
                assertFalse(list.contains(champion.name))
            }
            assertTrue(result.advantages.intersect(result.counters.toSet()).isEmpty())
            for (name in result.advantages+result.counters) {
                val rival=champions.first { it.name==name }
                assertTrue(rival.primaryRole==role || role in rival.secondaryRoles)
            }
            profiles++
        }
        assertEquals(300,profiles)
    }
    @Test fun `documented relations keep priority without becoming both advantage and weakness`() {
        val champions=catalog();val ahri=champions.first { it.id=="ahri" }
        val known=MatchupRoleResult(listOf("Lux"),listOf("Yasuo"),listOf("Vi"))
        val result=CoachMatchupRanking.complete(ahri,LaneRole.MID,known,champions)
        assertEquals("Lux",result.advantages.first())
        assertEquals("Yasuo",result.counters.first())
        assertEquals("Vi",result.synergies.first())
        assertEquals(result,CoachMatchupRanking.complete(ahri,LaneRole.MID,known,champions.reversed()))
    }
}
