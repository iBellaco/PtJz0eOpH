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
    private var revision = 0L

    @Synchronized fun reset() {
        revision++
        previous = emptyMap(); slots = emptyBanSlots(); lastScanAt = -1500L; lastAspect = 0f
    }

    fun scan(bitmap: Bitmap, context: Context?, nowMs: Long,
                           exclusions: List<Rect> = emptyList(), config: VisionCalibrationConfig = VisionCalibrationConfig(),
                           pickedIds: Set<String> = emptySet(), isCurrent: () -> Boolean = { true }): Set<String> {
        val capturedRevision = synchronized(this) {
            if (!isCurrent() || bitmap.isRecycled || bitmap.width < bitmap.height || nowMs - lastScanAt < 250L ||
                slots.all { it.state == BanState.CONFIRMED }) return confirmedIds()
            lastScanAt = nowMs
            revision
        }
        val observations = LiteRTVisionClassifier.findHeaderBanObservations(bitmap, context, exclusions, config).map { observation ->
            if (observation.championId in pickedIds) observation.copy(championId=null,empty=false) else observation
        }
        return synchronized(this) {
            if(revision != capturedRevision || !isCurrent()) return@synchronized confirmedIds()
            updateFrame(observations, bitmap)
            if (config.manualAllyBans || config.manualEnemyBans) slots = slots.map {
                it.copy(region = it.region?.copy(radius = config.banDiameterRatio / 2f))
            }
            confirmedIds()
        }
    }

    @Synchronized fun recalibrate(config: VisionCalibrationConfig) {
        revision++
        previous=emptyMap()
        lastScanAt=-1500L
        slots=slots.map { slot ->
            val manual=if(slot.side==BanSide.ALLY) config.manualAllyBans else config.manualEnemyBans
            if(manual) slot.copy(region=config.banRegions(slot.side==BanSide.ALLY)[slot.index]) else slot
        }
    }

    private fun confirmedIds(): Set<String> = slots.filter { it.state == BanState.CONFIRMED }
        .mapNotNull { it.championId }.toSet()

    /** Recognition publishes frame-relative circles; their spacing follows the visible game, not outer bars. */
    @Synchronized internal fun updateFrame(observations: List<BanObservation>, bitmap: Bitmap) {
        val content = DraftFrameGeometry.contentBounds(bitmap)
        update(observations, bitmap.width.toFloat() / bitmap.height, content.height().toFloat() / bitmap.height)
    }

    /** Public to module tests so disappearance, replacement and team assignment are deterministic. */
    @Synchronized internal fun update(observations: List<BanObservation>, aspect: Float, contentHeightRatio: Float = 1f) {
        if (lastAspect != 0f && abs(aspect - lastAspect) > .02f) {
            previous = emptyMap()
            slots = slots.map { if(it.state==BanState.CONFIRMED) it else it.copy(region=null) }
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
                val nominal = .080f * contentHeightRatio / aspect
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
                slot.state == BanState.CONFIRMED -> slot // Latched until explicit draft clear.
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
        val radii = slots.mapNotNull { it.region?.radius }.sorted()
        val commonRadius = radii.getOrNull(radii.size / 2)
        if (commonRadius != null) slots = slots.map { slot ->
            slot.copy(region = slot.region?.copy(radius = commonRadius))
        }
        previous = assigned
    }
}
