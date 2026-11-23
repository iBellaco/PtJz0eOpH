package com.example.service.screen

import kotlin.math.abs

/** Allied X positions can change independently. Heights and diameters belong to one grid per team. */
internal class DraftGeometryStability {
    private data class Column(val xs: List<Float>, val ys: List<Float>, val diameters: List<Float>) {
        fun near(other: Column): Boolean = xs.zip(other.xs).all { abs(it.first - it.second) <= .004f } &&
            ys.zip(other.ys).all { abs(it.first - it.second) <= .004f } &&
            diameters.zip(other.diameters).all { abs(it.first - it.second) <= .008f }
    }
    private val accepted = arrayOfNulls<Column>(7)
    private val pending = arrayOfNulls<Column>(7)
    private var enemyLocked = false

    fun reset() {
        accepted.fill(null)
        pending.fill(null)
        enemyLocked = false
    }

    fun update(candidate: VisionCalibrationConfig, enemyMeasured: Boolean = true,
               baseline: VisionCalibrationConfig = VisionCalibrationConfig(), frameHeight: Int = 0): VisionCalibrationConfig {
        fun separated(grid: Column): Boolean = (1 until grid.ys.size).all { i ->
            if (frameHeight > 0) {
                fun bounds(slot: Int): IntRange {
                    val diameter = (grid.diameters[slot] * frameHeight).toInt().coerceIn(minOf(32, frameHeight), frameHeight)
                    val top = ((grid.ys[slot] * frameHeight).toInt() - diameter / 2).coerceIn(0, frameHeight - diameter)
                    return top until top + diameter
                }
                bounds(i - 1).last < bounds(i).first
            } else grid.ys[i] - grid.ys[i - 1] >= (grid.diameters[i] + grid.diameters[i - 1]) / 2
        }
        fun select(side: Int, next: Column, measured: Boolean = true, fallback: Column = next): Column {
            val old = accepted[side]
            if (!measured) {
                pending[side] = null
                return old ?: fallback
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
        val allyXs = (0..4).map { slot ->
            select(slot, Column(listOf(candidate.getAllySlotX(slot)), emptyList(), emptyList())).xs.single()
        }
        fun allyGrid(config: VisionCalibrationConfig) = Column(emptyList(), config.allySlotYRatios,
            List(5) { config.getSlotDiameter(true, it) })
        fun enemyGrid(config: VisionCalibrationConfig) = Column(config.enemySlotXRatios, config.enemySlotYRatios,
            List(5) { config.getSlotDiameter(false, it) })
        val nextAlly = allyGrid(candidate)
        val ally = select(6, nextAlly, separated(nextAlly), allyGrid(baseline))
        val nextEnemy = enemyGrid(candidate)
        val enemy = select(5, nextEnemy, enemyMeasured && separated(nextEnemy), enemyGrid(baseline))
        return candidate.copy(
            allyAvatarCenterX = allyXs.first(), allySlotXRatios = allyXs,
            allySlotYRatios = ally.ys, allySlotDiameterRatios = ally.diameters,
            enemyAvatarCenterX = enemy.xs.first(), enemySlotXRatios = enemy.xs,
            enemySlotYRatios = enemy.ys, enemySlotDiameterRatios = enemy.diameters
        )
    }
}
