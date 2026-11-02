package com.example.service.screen

import kotlin.math.abs

/** Each side keeps its measured grid until a changed grid repeats in a later alignment. */
internal class DraftGeometryStability {
    private data class Column(val xs: List<Float>, val ys: List<Float>, val diameters: List<Float>) {
        fun near(other: Column): Boolean = xs.zip(other.xs).all { abs(it.first - it.second) <= .004f } &&
            ys.zip(other.ys).all { abs(it.first - it.second) <= .004f } &&
            diameters.zip(other.diameters).all { abs(it.first - it.second) <= .008f }
    }
    private val accepted = arrayOfNulls<Column>(2)
    private val pending = arrayOfNulls<Column>(2)

    fun reset() {
        accepted.fill(null)
        pending.fill(null)
    }

    fun update(candidate: VisionCalibrationConfig): VisionCalibrationConfig {
        fun select(side: Int, next: Column): Column {
            val old = accepted[side]
            if (old == null) {
                accepted[side] = next
                return next
            }
            if (old.near(next)) {
                pending[side] = null
                return old
            }
            if (pending[side]?.near(next) == true) {
                accepted[side] = next
                pending[side] = null
                return next
            }
            pending[side] = next
            return old
        }
        val ally = select(0, Column(candidate.allySlotXRatios, candidate.allySlotYRatios, candidate.allySlotDiameterRatios))
        val enemy = select(1, Column(candidate.enemySlotXRatios, candidate.enemySlotYRatios, candidate.enemySlotDiameterRatios))
        return candidate.copy(
            allyAvatarCenterX = ally.xs.first(), allySlotXRatios = ally.xs,
            allySlotYRatios = ally.ys, allySlotDiameterRatios = ally.diameters,
            enemyAvatarCenterX = enemy.xs.first(), enemySlotXRatios = enemy.xs,
            enemySlotYRatios = enemy.ys, enemySlotDiameterRatios = enemy.diameters
        )
    }
}
