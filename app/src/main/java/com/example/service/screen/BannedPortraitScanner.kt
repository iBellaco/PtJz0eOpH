package com.example.service.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import kotlin.math.abs

/** Tracks each team's five positions; ambiguous or hidden frames cannot erase confirmed bans. */
internal class BannedPortraitScanner {
    private var previous = emptyMap<Pair<BanSide, Int>, BanObservation>()
    var slots: List<BanSlot> = emptyBanSlots()
        private set
    private val completeRows = mutableMapOf<BanSide, List<BanRegion>>()
    private var lastViewport: Rect? = null
    private var lastFrameSize: Pair<Int, Int>? = null
    private var lastAspect = 0f
    private var lastScanAt = -1500L
    private var revision = 0L

    @Synchronized fun reset() {
        revision++
        completeRows.clear()
        lastViewport = null
        lastFrameSize = null
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
        val bounds = DraftFrameGeometry.contentBounds(bitmap)
        val tracked = synchronized(this) {
            if (bounds == lastViewport && lastFrameSize == (bitmap.width to bitmap.height)) completeRows.toMap() else emptyMap()
        }
        var searchConfig = config
        for ((side, row) in tracked) searchConfig = searchConfig.withObservedBanRow(side == BanSide.ALLY, row)
        val observations = LiteRTVisionClassifier.findHeaderBanObservations(
            bitmap, context, exclusions, searchConfig, tracked.keys, bounds, isCurrent
        ).map { observation ->
            if (observation.championId in pickedIds) observation.copy(championId=null,empty=false) else observation
        }
        return synchronized(this) {
            if(revision != capturedRevision || !isCurrent()) return@synchronized confirmedIds()
            updateFrame(observations, bitmap, config, bounds)
            if (config.manualAllyBans || config.manualEnemyBans) slots = slots.map {
                val manual = if (it.side == BanSide.ALLY) config.manualAllyBans else config.manualEnemyBans
                if (manual) it.copy(region = config.banRegions(it.side == BanSide.ALLY)[it.index]) else it
            }
            confirmedIds()
        }
    }

    @Synchronized fun recalibrate(config: VisionCalibrationConfig) {
        revision++
        completeRows.clear()
        lastViewport = null
        lastFrameSize = null
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
        config: VisionCalibrationConfig = VisionCalibrationConfig(),
        content: Rect = DraftFrameGeometry.contentBounds(bitmap)
    ) {
        val frameSize = bitmap.width to bitmap.height
        if (content != lastViewport || frameSize != lastFrameSize) {
            lastFrameSize = frameSize
            completeRows.clear()
            previous = emptyMap()
            slots = slots.map { it.copy(region = null) }
            lastViewport = content
        }
        update(observations, bitmap.width.toFloat() / bitmap.height,
            content.height().toFloat() / bitmap.height, config,
            content.left.toFloat() / bitmap.width, content.top.toFloat() / bitmap.height,
            content.width().toFloat() / bitmap.width)
    }

    /** Public to module tests so disappearance, replacement and team assignment are deterministic. */
    @Synchronized internal fun update(
        observations: List<BanObservation>,
        aspect: Float,
        contentHeightRatio: Float = 1f,
        config: VisionCalibrationConfig = VisionCalibrationConfig(),
        contentLeftRatio: Float = 0f,
        contentTopRatio: Float = 0f,
        contentWidthRatio: Float = 1f
    ) {
        if (lastAspect != 0f && abs(aspect - lastAspect) > .02f) {
            previous = emptyMap()
            completeRows.clear()
            slots = slots.map { it.copy(region = null) }
        }
        lastAspect = aspect
        val assigned = mutableMapOf<Pair<BanSide, Int>, BanObservation>()
        var current = slots
        for (side in BanSide.entries) {
            val seen = observations.filter { it.side == side }.sortedBy { it.region.x }
            if (seen.isEmpty()) continue
            val manual = if (side == BanSide.ALLY) config.manualAllyBans else config.manualEnemyBans
            val nominal = if (manual) config.banRegions(side == BanSide.ALLY)
                else computeAdaptiveBanRegions(aspect * contentWidthRatio / contentHeightRatio, side).map {
                    BanRegion(contentLeftRatio + it.x * contentWidthRatio, contentTopRatio + it.y * contentHeightRatio,
                        it.radius * contentHeightRatio)
                }
            val knownRow = if (manual) nominal else completeRows[side]
            val nominalStep = .080f * contentHeightRatio / aspect.coerceAtLeast(1f)
            val reference = knownRow ?: nominal
            fun nearest(region: BanRegion) = reference.indices.minBy { abs(reference[it].x - region.x) }
            val nearestIndices = seen.map { nearest(it.region) }
            val fullRow = !manual && seen.size == 5
            val positions = if (fullRow) seen.map { it.region } else reference
            if (fullRow) {
                if (knownRow == null) current = anchorConfirmedSlots(side, current, seen, nominalStep * .55f)
                completeRows[side] = positions
            }
            val assignments = if (fullRow) seen.mapIndexed { index, observation -> observation to index }
            else seen.mapIndexed { offset, observation -> observation to nearestIndices[offset] }
                .filter { (observation, index) -> abs(reference[index].x - observation.region.x) <= nominalStep * .55f }
            val located = positions.toMutableList()
            // Two proposals for one position cannot confirm two identities or shift a neighbor.
            for ((index, proposals) in assignments.groupBy { it.second }) {
                val observation = proposals.minBy { abs(positions[index].x - it.first.region.x) }.first
                if (!manual) located[index] = observation.region
                assigned[side to index] = observation
            }
            current = current.map { slot ->
                if (slot.side != side) slot
                else if (knownRow == null && !fullRow && slot.region != null && (side to slot.index) !in assigned) slot
                else slot.copy(region = located[slot.index])
            }
            // If four or more rival circles form a regular row near the right
            // edge, retain the fifth position as an explicit empty slot even
            // when the classifier could not see its crossed-out emblem.
            if (side == BanSide.ENEMY && seen.size == 4 &&
                assignments.map { it.second } == listOf(0, 1, 2, 3)) {
                val lastSeen = seen.last().region
                val outer = lastSeen.copy(x = (lastSeen.x + nominalStep).coerceAtMost(.999f))
                val rightEdge = contentLeftRatio + contentWidthRatio
                if (outer.x + outer.radius / aspect.coerceAtLeast(1f) < rightEdge &&
                    rightEdge - outer.x <= nominalStep * 1.8f) {
                    assigned[side to 4] = BanObservation(side, outer, empty = true)
                }
            }
        }
        val challengers = resolveCompetingBans(current, assigned)
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
        // A stronger challenger must actually win its own slot on two frames before the previous owner is released.
        for (challenger in challengers) {
            if (slots.any { it.side == challenger.side && it.index == challenger.index && it.championId == challenger.championId }) {
                slots = slots.map {
                    if (it.side == challenger.side && it.index != challenger.index && it.championId == challenger.championId)
                        it.copy(championId = null, state = BanState.UNKNOWN, similarity = 0f)
                    else it
                }
            }
        }
        val radii = slots.mapNotNull { it.region?.radius }.sorted()
        val commonRadius = radii.getOrNull(radii.size / 2)
        if (commonRadius != null) slots = slots.map { slot ->
            slot.copy(region = slot.region?.copy(radius = commonRadius))
        }
        previous = assigned
    }

    /** The first full row locates bans that arrived before the viewport's HUD offset was known. */
    private fun anchorConfirmedSlots(side: BanSide, current: List<BanSlot>, row: List<BanObservation>, tolerance: Float): List<BanSlot> {
        val confirmed = current.filter { it.side == side && it.state == BanState.CONFIRMED }
        val anchored = confirmed.groupBy { slot ->
            val x = slot.region?.x ?: return@groupBy slot.index
            val index = row.indices.minBy { abs(row[it].region.x - x) }
            if (abs(row[index].region.x - x) <= tolerance) index else slot.index
        }
        if (anchored.none { (index, owners) -> owners.any { it.index != index } }) return current
        previous = previous.filterKeys { it.first != side }
        return current.map { slot ->
            if (slot.side != side) slot else {
                val matches = anchored[slot.index].orEmpty()
                val owner = matches.singleOrNull() ?: matches.singleOrNull { it.championId == row[slot.index].championId }
                owner?.copy(index = slot.index) ?: BanSlot(side, slot.index)
            }
        }
    }

    /** A team cannot ban a champion twice. Leave equally plausible new circles open for later evidence. */
    private fun resolveCompetingBans(current: List<BanSlot>, assigned: MutableMap<Pair<BanSide, Int>, BanObservation>): List<BanSlot> {
        val incumbents = current.filter { it.state == BanState.CONFIRMED }.associateBy { it.side to it.championId }
        val groups = assigned.entries.filter { it.value.championId != null }.groupBy { it.key.first to it.value.championId }
        return buildList {
            for ((identity, matches) in groups) {
                val incumbent = incumbents[identity]
                if (incumbent == null && matches.size == 1) continue
                val ranked = matches.sortedByDescending { it.value.similarity }
                val best = ranked.first()
                val hasLead = ranked.size == 1 || isClearlyStrongerBan(best.value.similarity, ranked[1].value.similarity)
                val bestIndex = best.key.second
                val accepted = when {
                    incumbent == null -> bestIndex.takeIf { hasLead }
                    bestIndex != incumbent.index && hasLead && isClearlyStrongerBan(best.value.similarity, incumbent.similarity) -> {
                        add(incumbent.copy(index = bestIndex))
                        bestIndex
                    }
                    else -> incumbent.index
                }
                for ((key, observation) in matches) {
                    if (key.second != accepted) assigned[key] = observation.copy(championId = null, empty = false)
                }
            }
        }
    }

}
