package com.example.service.screen

/** Ranked draft is 1-2-2-2-2-1. Equal counts cannot reveal which side picked first. */
object DraftPickOrderPolicy {
    fun firstPickFromTenthSide(tenthIsAlly: Boolean): Boolean = !tenthIsAlly

    /** A lane title is an unselected ally, never a tenth-pick portrait confirmation. */
    fun tenthSideFromTitles(allies: List<Boolean>, rivals: List<Boolean>, allyLaneSlots: Set<Int>): Boolean? {
        if (allies.size != 5 || rivals.size != 5) return null
        return when {
            allies.all { it } && rivals.take(4).all { it } && !rivals[4] -> false
            rivals.all { it } && allies.take(4).all { it } && !allies[4] && 4 !in allyLaneSlots -> true
            else -> null
        }
    }

    fun inferFirstPick(allies: Int, rivals: Int): Boolean? = when (allies to rivals) {
        1 to 0, 1 to 2, 3 to 2, 3 to 4, 5 to 4 -> true
        0 to 1, 2 to 1, 2 to 3, 4 to 3, 4 to 5 -> false
        else -> null
    }
}
