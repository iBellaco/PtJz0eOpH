package com.example.service.screen

/** Ranked draft is 1-2-2-2-2-1. Equal counts cannot reveal which side picked first. */
object DraftPickOrderPolicy {
    fun inferFirstPick(allies: Int, rivals: Int): Boolean? = when (allies to rivals) {
        1 to 0, 1 to 2, 3 to 2, 3 to 4, 5 to 4 -> true
        0 to 1, 2 to 1, 2 to 3, 4 to 3, 4 to 5 -> false
        else -> null
    }
}
