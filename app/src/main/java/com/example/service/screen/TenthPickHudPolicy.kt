package com.example.service.screen

import com.example.model.Champion

/** Only the tracked final portrait may replace its own preview. */
object TenthPickHudPolicy {
    fun targetIndex(confirmed: Boolean, champion: Champion?, ownTeam: List<Champion?>,
        otherTeam: List<Champion?>, lockedSlots: Set<Int>, expectedIndex: Int? = null,
        replaceChampionId: String? = null): Int? {
        if (!confirmed || champion == null || ownTeam.size != 5 || otherTeam.size != 5) return null
        val selected = (ownTeam + otherTeam).filterNotNull()
        if (selected.size == 10 && replaceChampionId != null) {
            val previous = ownTeam.indices.singleOrNull { ownTeam[it]?.id == replaceChampionId } ?: return null
            if (previous in lockedSlots || expectedIndex != null && expectedIndex != previous) return null
            if (selected.any { it.id == champion.id }) return null
            return previous
        }
        // The final portrait is already confirmed by the vision engine. Do not block the
        // tenth pick merely because an earlier HUD slot temporarily duplicated another
        // champion; still require exactly nine occupied slots and never replace a pick.
        if (selected.size != 9 || selected.any { it.id == champion.id }) return null
        val vacancy = ownTeam.indices.singleOrNull { ownTeam[it] == null } ?: return null
        if (vacancy in lockedSlots || expectedIndex != null && expectedIndex != vacancy) return null
        return vacancy
    }
}
