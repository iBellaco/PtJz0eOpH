package com.example.service.screen

import kotlin.math.abs

/** Each allied row and the rival grid confirm changes on separate observations. */
internal class DraftGeometryStability {
    private data class Column(val xs: List<Float>, val ys: List<Float>, val diameters: List<Float>) {
        fun near(other: Column): Boolean = xs.zip(other.xs).all { abs(it.first - it.second) <= .004f } &&
            ys.zip(other.ys).all { abs(it.first - it.second) <= .004f } &&
            diameters.zip(other.diameters).all { abs(it.first - it.second) <= .008f }
    }
    private val accepted = arrayOfNulls<Column>(6)
    private val pending = arrayOfNulls<Column>(6)
    private var enemyLocked = false

    fun reset() {
        accepted.fill(null)
        pending.fill(null)
        enemyLocked = false
    }

    fun update(candidate: VisionCalibrationConfig, enemyMeasured: Boolean = true): VisionCalibrationConfig {
        fun select(side: Int, next: Column): Column {
            val old = accepted[side]
            if (side == 5 && !enemyMeasured) {
                pending[side] = null
                return old ?: next
            }
            if (side == 5 && enemyLocked && old != null) return old
            if (old == null) {
                accepted[side] = next
                return next
            }
            if (old.near(next)) {
                pending[side] = null
                if (side == 5) enemyLocked = true
                return old
            }
            if (pending[side]?.near(next) == true) {
                accepted[side] = next
                pending[side] = null
                if (side == 5) enemyLocked = true
                return next
            }
            pending[side] = next
            return old
        }
        val rows = (0..4).map { slot -> select(slot, Column(
            listOf(candidate.getAllySlotX(slot)), listOf(candidate.allySlotYRatios[slot]),
            listOf(candidate.getSlotDiameter(true, slot)))) }
        val ally = Column(rows.map { it.xs.single() }, rows.map { it.ys.single() }, rows.map { it.diameters.single() })
        val enemy = select(5, Column(candidate.enemySlotXRatios, candidate.enemySlotYRatios, candidate.enemySlotDiameterRatios))
        return candidate.copy(
            allyAvatarCenterX = ally.xs.first(), allySlotXRatios = ally.xs,
            allySlotYRatios = ally.ys, allySlotDiameterRatios = ally.diameters,
            enemyAvatarCenterX = enemy.xs.first(), enemySlotXRatios = enemy.xs,
            enemySlotYRatios = enemy.ys, enemySlotDiameterRatios = enemy.diameters
        )
    }
}
