package com.example.service.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import kotlin.math.abs
import kotlin.math.roundToInt

/** Tracks each team's five positions; ambiguous or hidden frames cannot erase confirmed bans. */
internal class BannedPortraitScanner {
    private var previous = emptyMap<Pair<BanSide, Int>, BanObservation>()
    var slots: List<BanSlot> = emptyBanSlots()
        private set
    private var lastAspect = 0f
    private var lastScanAt = -1500L

    @Synchronized fun reset() {
        previous = emptyMap(); slots = emptyBanSlots(); lastScanAt = -1500L; lastAspect = 0f
    }

    @Synchronized fun scan(bitmap: Bitmap, context: Context?, nowMs: Long,
                           exclusions: List<Rect> = emptyList()): Set<String> {
        if (bitmap.isRecycled || bitmap.width < bitmap.height || nowMs - lastScanAt < 1500L) return confirmedIds()
        lastScanAt = nowMs
        update(LiteRTVisionClassifier.findHeaderBanObservations(bitmap, context, exclusions),
            bitmap.width.toFloat() / bitmap.height)
        return confirmedIds()
    }

    private fun confirmedIds(): Set<String> = slots.filter { it.state == BanState.CONFIRMED }
        .mapNotNull { it.championId }.toSet()

    /** Public to module tests so disappearance, replacement and team assignment are deterministic. */
    @Synchronized internal fun update(observations: List<BanObservation>, aspect: Float) {
        if (lastAspect != 0f && abs(aspect - lastAspect) > .02f) {
            previous = emptyMap(); slots = emptyBanSlots()
        }
        lastAspect = aspect
        val assigned = mutableMapOf<Pair<BanSide, Int>, BanObservation>()
        var current = slots
        for (side in BanSide.entries) {
            val seen = observations.filter { it.side == side }.sortedBy { it.region.x }
            if (seen.isEmpty()) continue
            val existing = current.filter { it.side == side }
            val regions = if (existing.all { it.region != null }) existing.map { it.region!! } else {
                // Learn horizontal spacing from visible neighbors, retaining missing positions.
                val nominal = .080f / aspect
                val gaps = seen.zipWithNext { a, b -> b.region.x - a.region.x }
                    .map { gap -> gap / (gap / nominal).roundToInt().coerceIn(1, 4) }
                    .filter { it in nominal * .65f..nominal * 1.4f }.sorted()
                val step = gaps.getOrNull(gaps.size / 2) ?: nominal
                val start = if (side == BanSide.ALLY) seen.first().region.x else seen.last().region.x - 4 * step
                val y = seen.map { it.region.y }.average().toFloat()
                val radius = seen.map { it.region.radius }.average().toFloat()
                List(5) { BanRegion((start + it * step).coerceIn(0f, 1f), y, radius) }
            }
            current = current.map { if (it.side == side) it.copy(region = regions[it.index]) else it }
            val tolerance = ((regions.last().x - regions.first().x) / 4 * .45f).coerceAtLeast(.006f)
            for (observation in seen) {
                val index = regions.indices.minBy { abs(regions[it].x - observation.region.x) }
                if (abs(regions[index].x - observation.region.x) <= tolerance) {
                    assigned[side to index] = observation
                }
            }
        }
        slots = current.map { slot ->
            val key = slot.side to slot.index
            val observation = assigned[key]
            val old = previous[key]
            when {
                observation == null -> slot // no evidence of an empty slot, preserve last confirmation
                observation.championId == null && !observation.empty ->
                    if (slot.state == BanState.CONFIRMED) slot else slot.copy(region = observation.region,
                        championId = null, state = BanState.UNKNOWN)
                old != null && observation.empty && old.empty -> slot.copy(region = observation.region, championId = null, state = BanState.EMPTY)
                old != null && !observation.empty && observation.championId == old.championId && old.championId != null ->
                    slot.copy(region = observation.region, championId = observation.championId, state = BanState.CONFIRMED)
                slot.state == BanState.CONFIRMED -> slot
                else -> slot.copy(region = observation.region, championId = null, state = BanState.PENDING)
            }
        }
        previous = assigned
    }
}

internal object BanDetectionAccess {
    fun canDetect(role: String, adminClaim: Boolean): Boolean = role == "admin" || adminClaim
}
