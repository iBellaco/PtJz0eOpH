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
            if (!isCurrent()) return confirmedIds()
            slots = slots.map { slot ->
                if (slot.championId in pickedIds) slot.copy(championId = null, state = BanState.UNKNOWN, similarity = 0f)
                else slot
            }
            previous = previous.filterValues { it.championId !in pickedIds }
            // Recheck a completed header at a lower cadence: an early weak match
            // must not remain permanent for the rest of the draft.
            val interval = if (slots.all { it.state == BanState.CONFIRMED }) 1500L else 250L
            if (!isCurrent() || bitmap.isRecycled || bitmap.width < bitmap.height || nowMs - lastScanAt < interval)
                return confirmedIds()
            lastScanAt = nowMs
            revision
        }
        val observations = LiteRTVisionClassifier.findHeaderBanObservations(bitmap, context, exclusions, config).map { observation ->
            if (observation.championId in pickedIds) observation.copy(championId=null,empty=false) else observation
        }
        return synchronized(this) {
            if(revision != capturedRevision || !isCurrent()) return@synchronized confirmedIds()
            updateFrame(observations, bitmap, config)
            if (config.manualAllyBans || config.manualEnemyBans) slots = slots.map {
                val manual = if (it.side == BanSide.ALLY) config.manualAllyBans else config.manualEnemyBans
                if (manual) it.copy(region = config.banRegions(it.side == BanSide.ALLY)[it.index]) else it
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
    @Synchronized internal fun updateFrame(
        observations: List<BanObservation>,
        bitmap: Bitmap,
        config: VisionCalibrationConfig = VisionCalibrationConfig()
    ) {
        val content = DraftFrameGeometry.contentBounds(bitmap)
        update(observations, bitmap.width.toFloat() / bitmap.height,
            content.height().toFloat() / bitmap.height, config)
    }

    /** Public to module tests so disappearance, replacement and team assignment are deterministic. */
    @Synchronized internal fun update(
        observations: List<BanObservation>,
        aspect: Float,
        contentHeightRatio: Float = 1f,
        config: VisionCalibrationConfig = VisionCalibrationConfig()
    ) {
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
            val manual = if (side == BanSide.ALLY) config.manualAllyBans else config.manualEnemyBans
            val nominal = if (manual) config.banRegions(side == BanSide.ALLY)
                else computeAdaptiveBanRegions(aspect, side)
            val measured = when {
                seen.size == 5 -> seen.map { it.region }
                else -> {
                    // Learn spacing from visible neighbors for captures whose
                    // content viewport has a stable offset from the nominal
                    // edge. This preserves measured geometry while the
                    // nominal matcher handles genuinely sparse rows.
                    val nominalStep = .080f * contentHeightRatio / aspect.coerceAtLeast(1f)
                    val gaps = seen.zipWithNext { a, b -> b.region.x - a.region.x }
                        .map { gap -> gap / (gap / nominalStep).roundToInt().coerceIn(1, 4) }
                        .filter { it in nominalStep * .65f..nominalStep * 1.4f }.sorted()
                    val step = gaps.getOrNull(gaps.size / 2) ?: nominalStep
                    val start = if (side == BanSide.ALLY) seen.first().region.x
                    else seen.last().region.x - 4 * step
                    val y = seen.map { it.region.y }.average().toFloat()
                    val radius = seen.map { it.region.radius }.average().toFloat()
                    List(5) { BanRegion((start + it * step).coerceIn(0f, 1f), y, radius) }
                }
            }
            val nominalDistance = .080f / aspect.coerceAtLeast(1f)
            val tolerance = (nominalDistance * .5f).coerceAtLeast(.006f)
            val nearest = seen.map { observation ->
                nominal.indices.minBy { abs(nominal[it].x - observation.region.x) }
            }
            val rowIsContiguous = nearest.distinct().size == seen.size &&
                nearest.zipWithNext().all { (a, b) -> b == a + 1 }
            val useMeasuredRow = !manual && (seen.size == 1 || seen.size == 5 ||
                (seen.size >= 3 && rowIsContiguous))
            val sparseNominal = if (manual) true else if (useMeasuredRow) false else
                (seen.size > 1 && seen.size <= 2) ||
                    nearest.distinct().size != seen.size ||
                    nearest.zipWithNext().any { (a, b) -> b != a + 1 }
            val assignmentRegions = if (sparseNominal) nominal else measured
            val assignments = if (sparseNominal) {
                seen.mapIndexed { offset, observation -> observation to nearest[offset] }
            } else {
                seen.mapIndexed { offset, observation -> observation to offset.coerceAtMost(4) }
            }
            val slotRegions = measured.toMutableList()
            if (sparseNominal) assignments.forEach { (observation, index) -> slotRegions[index] = observation.region }
            current = current.map { slot ->
                if (slot.side != side) slot
                else assignments.firstOrNull { it.second == slot.index }?.first?.let { slot.copy(region = it.region) }
                    ?: slot.copy(region = slotRegions[slot.index])
            }
            for ((observation, index) in assignments) {
                val distance = abs(assignmentRegions[index].x - observation.region.x)
                if (distance <= tolerance || !sparseNominal) assigned[side to index] = observation
            }
            // If four or more rival circles form a regular row near the right
            // edge, retain the fifth position as an explicit empty slot even
            // when the classifier could not see its crossed-out emblem.
            if (side == BanSide.ENEMY && seen.size >= 4 &&
                assignments.none { it.second == 4 }) {
                val lastSeen = seen.last().region
                val outer = lastSeen.copy(x = (lastSeen.x + nominalDistance).coerceAtMost(.999f))
                if (outer.x + outer.radius / aspect.coerceAtLeast(1f) < 1f &&
                    1f - outer.x <= nominalDistance * 1.8f) {
                    assigned[side to 4] = BanObservation(side, outer, empty = true)
                }
            }
        }
        slots = current.map { slot ->
            val key = slot.side to slot.index
            val observation = assigned[key]
            val old = previous[key]
            when {
                slot.state == BanState.CONFIRMED && observation != null && observation.championId == slot.championId && old != null &&
                    old.championId == slot.championId -> slot.copy(similarity = maxOf(slot.similarity,
                        minOf(observation.similarity, old.similarity)))
                slot.state == BanState.CONFIRMED && observation?.championId != null &&
                    observation.championId != slot.championId && old != null && observation.championId == old.championId &&
                    isClearlyStrongerBan(minOf(observation.similarity, old.similarity), slot.similarity) ->
                    slot.copy(region = observation.region, championId = observation.championId,
                        similarity = minOf(observation.similarity, old.similarity))
                slot.state == BanState.CONFIRMED -> slot // Hidden/empty/weaker evidence cannot erase a ban.
                observation == null -> slot // no evidence of an empty slot, preserve last confirmation
                observation.championId == null && !observation.empty ->
                    if (slot.state == BanState.CONFIRMED) slot else slot.copy(region = observation.region,
                        championId = null, state = BanState.UNKNOWN)
                old != null && observation.empty && old.empty -> slot.copy(region = observation.region, championId = null, state = BanState.EMPTY)
                old != null && !observation.empty && observation.championId == old.championId && old.championId != null ->
                    slot.copy(region = observation.region, championId = observation.championId, state = BanState.CONFIRMED,
                        similarity = minOf(observation.similarity, old.similarity))
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
