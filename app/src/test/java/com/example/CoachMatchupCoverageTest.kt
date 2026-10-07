package com.example

import com.example.model.Champion
import com.example.model.LaneRole
import com.example.util.CoachMatchupRanking
import com.example.util.MatchupRoleResult
import org.junit.Assert.*
import org.junit.Test

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk = [34], application = android.app.Application::class)
class CoachMatchupCoverageTest {
    private fun catalog(): List<Champion> {
        val context = org.robolectric.RuntimeEnvironment.getApplication()
        com.example.util.AppLanguage.select(context, "es")
        com.example.data.WildRiftRepository.initChampions(context, forceReload = true)
        return com.example.data.WildRiftRepository.baseChampionsList
    }
    @Test fun `all bundled champions and playable roles have twelve unique relationships per category`() {
        val champions=catalog()
        assertEquals(142,champions.size)
        var profiles=0
        for (champion in champions) for (role in listOf(champion.primaryRole)+champion.secondaryRoles) {
            val profile = com.example.util.ChampionRoleAdapter.getProfile(champion, role, "es")
            val result = MatchupRoleResult(profile.advantageAgainst, profile.counteredBy, profile.synergies)
            for (list in listOf(result.advantages,result.counters,result.synergies)) {
                assertEquals("${champion.id} $role",12,list.size)
                assertEquals(list.size,list.distinct().size)
                assertFalse(list.contains(champion.name))
                for ((signedIn, premium, expected) in listOf(
                    Triple(false, false, 3), Triple(true, false, 6), Triple(true, true, 12))) {
                    val rows = com.example.util.BuildChoiceRules.matchupRows(list, premium, signedIn)
                    assertEquals("${champion.id} $role signedIn=$signedIn premium=$premium", expected, rows.flatten().size)
                    assertEquals(expected / 3, rows.size)
                    rows.forEach { assertEquals(3, it.size) }
                    assertEquals(list.take(expected), rows.flatten())
                }
            }
            assertTrue(result.advantages.intersect(result.counters.toSet()).isEmpty())
            // Reviewed relations can describe threats across the map, not only direct lane rivals.
            for (name in result.advantages+result.counters+result.synergies) {
                assertTrue("${champion.id} $role has an unknown relationship: $name", champions.any { it.name==name })
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
