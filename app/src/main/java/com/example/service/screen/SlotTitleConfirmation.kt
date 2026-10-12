package com.example.service.screen

/** A single OCR artifact cannot select a player; every slot has independent evidence. */
internal class SlotTitleConfirmation {
    private val previous = mutableMapOf<Pair<Boolean, Int>, String>()
    private val counts = mutableMapOf<Pair<Boolean, Int>, Int>()
    private val gaps = mutableMapOf<Pair<Boolean, Int>, Int>()
    fun observe(ally: Boolean, slot: Int, championId: String?): Boolean {
        val key = ally to slot
        if (championId == null) {
            val gap = (gaps[key] ?: 0) + 1
            gaps[key] = gap
            if (gap > 1) { previous.remove(key); counts.remove(key) }
            return false
        }
        gaps.remove(key)
        counts[key] = if (previous[key] == championId) (counts[key] ?: 0) + 1 else 1
        previous[key] = championId
        return counts.getValue(key) >= 2
    }
    fun reset() { previous.clear(); counts.clear(); gaps.clear() }
}
