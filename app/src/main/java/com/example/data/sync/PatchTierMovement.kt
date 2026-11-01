package com.example.data.sync

/** Last observed direction survives unchanged refreshes for the lifetime of one game patch. */
object PatchTierMovement {
    private fun rank(tier: String) = when (GlobalTierConsensus.displayCategory(tier)) {
        "S+" -> 5; "S" -> 4; "A" -> 3; "B" -> 2; "C" -> 1; else -> 0
    }
    fun update(previous: Map<String, String>, current: Map<String, String>, saved: Map<String, Int>): Map<String, Int> =
        saved.toMutableMap().apply {
            current.forEach { (id, tier) ->
                previous[id]?.let { old ->
                    val direction = rank(tier).compareTo(rank(old))
                    if (direction != 0) put(id, direction)
                }
            }
        }
}
