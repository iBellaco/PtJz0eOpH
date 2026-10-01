package com.example.util

import com.example.model.Champion
import com.example.model.DamageType
import com.example.model.LaneRole

/** Tactical ranking to extend editorial references; scores are not measured win rates. */
object CoachMatchupRanking {
    private fun ids(names: String) = names.split(' ').toSet()
    private val divers = ids("akali ambessa camille diana ekko evelynn fizz hecarim irelia jarvan_iv kassadin katarina kayn kha_zix lee_sin master_yi nocturne rengar riven talon vi viego xin_zhao yasuo yone zed")
    private val peel = ids("alistar braum galio janna lissandra lulu milio morgana nami nautilus poppy rakan shen thresh zilean")
    private val catch = ids("ahri annie ashe blitzcrank galio leona lissandra lux maokai morgana nautilus pantheon pyke rammus skarner thresh twisted_fate veigar vex vi warwick")
    private val poke = ids("caitlyn ezreal heimerdinger hwei jayce karma lux mel nidalee orianna senna seraphine varus vel_koz viktor xerath ziggs zoe zyra")
    private val sustain = ids("aatrox darius dr_mundo fiora gwen irelia nasus olaf renekton soraka swain vladimir volibear warwick")
    private val tankKillers = ids("brand darius fiora gwen kai_sa kog_maw lillia master_yi vayne varus")
    private val area = ids("amumu annie aurelion_sol brand diana fiddlesticks galio hwei kennen lillia malphite miss_fortune morgana nunu_willump orianna ornn rell rumble samira seraphine swain wukong yone zyra")
    private val empower = ids("janna karma lulu milio nami senna sona soraka yuumi zilean")

    private fun pressure(attacker: Champion, defender: Champion): Int {
        var score = 0
        if (attacker.id in divers && defender.isRanged && defender.id !in divers) score += 5
        if (attacker.id in peel && defender.id in divers) score += 6
        if (attacker.id in catch && defender.id in divers) score += 3
        if (attacker.id in poke && !defender.isRanged && defender.id !in divers) score += 4
        if (attacker.id in sustain && defender.id in poke) score += 3
        if (attacker.id in tankKillers && defender.isFrontline) score += 6
        if (attacker.isFrontline && defender.damageType == DamageType.PHYSICAL && defender.id !in tankKillers) score += 2
        if (attacker.id == "rammus" && defender.damageType == DamageType.PHYSICAL) score += 4
        if (attacker.id == "galio" && defender.damageType == DamageType.MAGIC) score += 4
        if (attacker.id == "vex" && defender.id in divers) score += 5
        return score
    }

    private fun allyScore(champion: Champion, role: LaneRole, ally: Champion): Int {
        var score = 0
        if (ally.primaryRole != role) score += 3
        if (champion.isRanged && !champion.isFrontline && ally.id in peel) score += 5
        if (champion.id in divers && ally.id in catch) score += 5
        if (champion.id in area && ally.id in area) score += 5
        if (champion.damageType != ally.damageType) score += 2
        if (champion.isFrontline != ally.isFrontline) score += 2
        if (role == LaneRole.ADC && ally.primaryRole == LaneRole.SUPPORT) score += 8
        if (role == LaneRole.SUPPORT && ally.primaryRole == LaneRole.ADC) score += 8
        if (role == LaneRole.JUNGLE && ally.id in catch) score += 3
        if (champion.id in empower && ally.isRanged && ally.damageType == DamageType.PHYSICAL) score += 6
        if (ally.id in empower && champion.isRanged && champion.damageType == DamageType.PHYSICAL) score += 6
        return score
    }

    fun complete(champion: Champion, role: LaneRole, known: MatchupRoleResult,
                 catalog: List<Champion>): MatchupRoleResult {
        val others = catalog.filterNot { it.id == champion.id }.distinctBy { it.id }
        val rivals = others.filter { it.primaryRole == role || role in it.secondaryRoles }
        // Include documented relations first. Fill shortages by concrete kit/role interactions.
        fun ranked(candidates: List<Champion>, score: (Champion) -> Int) =
            candidates.sortedWith(compareByDescending(score).thenBy { it.id }).map { it.name }
        val counters = (known.counters + ranked(rivals.filterNot { it.name in known.advantages }) { pressure(it, champion) - pressure(champion, it) })
            .distinct().take(12)
        val advantages = (known.advantages + ranked(rivals) { pressure(champion, it) - pressure(it, champion) })
            .distinct().filterNot { it in counters }.take(12)
        val teammates = others.filter { it.primaryRole != role || it.secondaryRoles.any { candidateRole -> candidateRole != role } }
        val synergies = (known.synergies + ranked(teammates) { allyScore(champion, role, it) })
            .distinct().take(12)
        return MatchupRoleResult(advantages, counters, synergies)
    }
}
