package com.example.service.screen

/** Normalized coordinates belong to the captured frame, not the floating window. */
internal data class BanRegion(val x: Float, val y: Float, val radius: Float)
internal enum class BanSide { ALLY, ENEMY }
internal enum class BanState { UNKNOWN, PENDING, EMPTY, CONFIRMED }
internal data class BanObservation(val side: BanSide, val region: BanRegion,
    val championId: String? = null, val empty: Boolean = false)
internal data class BanSlot(val side: BanSide, val index: Int, val region: BanRegion? = null,
    val championId: String? = null, val state: BanState = BanState.UNKNOWN)

internal fun emptyBanSlots(): List<BanSlot> = BanSide.entries.flatMap { side ->
    List(5) { BanSlot(side, it) }
}
