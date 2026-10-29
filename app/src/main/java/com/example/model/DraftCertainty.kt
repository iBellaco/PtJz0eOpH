package com.example.model

enum class DraftCertainty { COLLECTING, PROVISIONAL, CONFIRMED }

object DraftCertaintyPolicy {
    fun selection(closed: Boolean, hasLastPick: Boolean, distinctPicks: Int): DraftCertainty = when {
        closed && distinctPicks == 10 -> DraftCertainty.CONFIRMED
        hasLastPick && distinctPicks == 10 -> DraftCertainty.PROVISIONAL
        else -> DraftCertainty.COLLECTING
    }
}
