package com.example.service.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.example.WildRiftApp
import com.example.data.WildRiftRepository
import com.example.model.Champion
import com.example.util.AppLogger
import com.example.util.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/** Local portrait matching for the final pick; no remote API or inference SDK. */
object LiteRTVisionClassifier {

    private const val TAG = "LiteRTVisionClassifier"
    private const val TENSOR_INPUT_SIZE = 24 // Interior del retrato

    // Umbral de confianza por defecto (80% de similitud real centrada en cero)
    const val DEFAULT_CONFIDENCE_THRESHOLD = 0.80f
    const val MIN_CONFIDENCE_THRESHOLD = 0.80f

    private var customConfidenceThreshold: Float = DEFAULT_CONFIDENCE_THRESHOLD

    fun getEffectiveThreshold(context: Context? = null): Float {
        val ctx = context ?: WildRiftApp.instance
        return if (ctx != null) {
            UserPreferences.getLiteRTConfidenceThreshold(ctx)
        } else {
            customConfidenceThreshold
        }
    }

    fun setThreshold(threshold: Float, context: Context? = null) {
        val clamped = threshold.coerceIn(0.50f, 0.95f)
        customConfidenceThreshold = clamped
        val ctx = context ?: WildRiftApp.instance
        if (ctx != null) {
            UserPreferences.setLiteRTConfidenceThreshold(ctx, clamped)
        }
        _reportFlow.value = _reportFlow.value.copy(minConfidenceThreshold = clamped)
    }

    // Cantidad de frames estables consecutivos requeridos para confirmar el 10º pick
    const val REQUIRED_STABLE_FRAMES = 2

    // Variables de seguimiento de estabilidad temporal entre fotogramas
    private val frameStability = CompletedFrameStability(REQUIRED_STABLE_FRAMES,1200L)
    private var lastVisibleSelection: LiteRTInferenceReport? = null

    /** The last unambiguous visible portrait is authoritative at draft departure. */
    fun finishLastVisibleSelection(): LiteRTInferenceReport? {
        val last = lastVisibleSelection ?: return null
        return last.copy(status = EngineStatus.COMPLETED, isConfirmed = true, isSelectionFinalized = true,
            decisionReason = "Último retrato válido antes de desaparecer los slots.").also {
            _reportFlow.value = it
        }
    }

    fun resetStabilityTracker() {
        frameStability.reset()
    }

    enum class EngineStatus {
        WAITING_FOR_PICKS_1_TO_9,
        WAITING_FOR_TENTH_PICK,
        RUNNING_INFERENCE,
        COMPLETED,
        NO_DETECTION
    }

    data class LiteRTCandidateScore(
        val champion: Champion,
        val similarityScore: Float, // Correlación espacial, no probabilidad
        val softmaxProbability: Float, // Peso relativo, no probabilidad calibrada
        val confidencePercent: Int,
        val rank: Int
    )

    data class LiteRTInferenceReport(
        val status: EngineStatus = EngineStatus.RUNNING_INFERENCE,
        val pickedChampion: Champion? = null,
        val confidencePercent: Int = 0,
        val inferenceTimeMs: Long = 0L,
        val topCandidates: List<LiteRTCandidateScore> = emptyList(),
        val cropBitmap: Bitmap? = null,
        val decisionReason: String = "Comparando el avatar del décimo pick con retratos locales.",
        val slotDescription: String = "Slot 5 (10º Pick)",
        val tensorDimensions: String = "${TENSOR_INPUT_SIZE}x${TENSOR_INPUT_SIZE}x3 (Float32)",
        val evaluatedPicksCount: Int = 0,
        val isConfirmed: Boolean = false,
        val isSelectionFinalized: Boolean = false,
        val stableFramesCount: Int = 0,
        val requiredStableFrames: Int = REQUIRED_STABLE_FRAMES,
        val minConfidenceThreshold: Float = DEFAULT_CONFIDENCE_THRESHOLD
    )

    private val _reportFlow = MutableStateFlow(LiteRTInferenceReport())
    val reportFlow: StateFlow<LiteRTInferenceReport> = _reportFlow.asStateFlow()

    private val championEmbeddingCache = ConcurrentHashMap<String, List<FloatArray>>()
    private val allyEmbeddingCache = ConcurrentHashMap<String, List<FloatArray>>()
    private val portraitAssetPaths = ConcurrentHashMap<String,String>()
    private var indexedIds: Set<String> = emptySet()
    private var targetKey: String? = null
    private var manuallyConfirmedChampion: Champion? = null

    @Synchronized
    fun ensureIndexed(context: Context? = null, champs: List<Champion> = WildRiftRepository.championSnapshot()) {
        val ctx = context ?: WildRiftApp.instance ?: return
        if (indexedIds == champs.map { it.id }.toSet()) return
        val assets = ctx.assets.list("champions")?.filter { it.endsWith(".png") }.orEmpty()
        fun canonical(id: String) = id.lowercase().filter { it.isLetterOrDigit() }
        val paths = assets.associateBy { canonical(it.removeSuffix(".png")) }
        for (champ in champs) {
            if (championEmbeddingCache.containsKey(champ.id)) continue
            val asset = paths[canonical(champ.id)] ?: paths[canonical(champ.name)] ?: continue
            try {
                val bmp = ctx.assets.open("champions/$asset").use { BitmapFactory.decodeStream(it) } ?: continue
                try {
                    val pixels = IntArray(bmp.width * bmp.height)
                    bmp.getPixels(pixels, 0, bmp.width, 0, 0, bmp.width, bmp.height)
                    championEmbeddingCache[champ.id] = PortraitMatcher.references(pixels, bmp.width, bmp.height)
                    allyEmbeddingCache[champ.id] = PortraitMatcher.references(pixels, bmp.width, bmp.height, excludeBottomBadge = true)
                    portraitAssetPaths[champ.id] = asset
                } finally { bmp.recycle() }
            } catch (e: Exception) {
                AppLogger.w(TAG, "Retrato no disponible: ${champ.id}")
            }
        }
        // Retry missing assets on subsequent scans; never mark a partial catalog complete.
        indexedIds = championEmbeddingCache.keys.toSet()
    }

    private var emptyBanReferenceCache: List<FloatArray>? = null
    /** Shared by production inference and real-frame regression. Thresholds are unchanged. */
    internal fun rankSlotPortrait(crop: Bitmap, isAlly: Boolean, context: Context?, excludedIds: Set<String> = emptySet()): List<Pair<Champion, Float>> {
        val catalog = WildRiftRepository.championSnapshot()
        ensureIndexed(context, catalog)
        val pixels = IntArray(crop.width * crop.height)
        crop.getPixels(pixels, 0, crop.width, 0, 0, crop.width, crop.height)
        val input = PortraitMatcher.descriptor(pixels, crop.width, crop.height, excludeBottomBadge = isAlly)
        val cache = if (isAlly) allyEmbeddingCache else championEmbeddingCache
        val ranked = catalog.filter { it.id !in excludedIds }.mapNotNull { champion ->
            cache[champion.id]?.let { champion to PortraitMatcher.similarity(input, it) }
        }.sortedByDescending { it.second }
        if (ranked.size < 2 || PortraitMatcher.accepts(ranked[0].second, ranked[1].second, getEffectiveThreshold(context))) return ranked
        val ctx = context ?: WildRiftApp.instance ?: return ranked
        // Refine only unresolved candidates. Do not lower confidence or separation.
        return ranked.take(4).map { (champion, coarse) ->
            val fine = runCatching {
                val asset = portraitAssetPaths[champion.id] ?: return@runCatching coarse
                val image = ctx.assets.open("champions/$asset").use { BitmapFactory.decodeStream(it) } ?: return@runCatching coarse
                try {
                    val reference = IntArray(image.width * image.height)
                    image.getPixels(reference, 0, image.width, 0, 0, image.width, image.height)
                    PortraitMatcher.fineSimilarity(input, reference, image.width, image.height, excludeBottomBadge = isAlly)
                } finally { image.recycle() }
            }.getOrDefault(coarse)
            champion to maxOf(coarse, fine)
        }.plus(ranked.drop(4)).sortedByDescending { it.second }
    }
    private fun emptyBanReferences(context: Context?): List<FloatArray> {
        emptyBanReferenceCache?.let { return it }
        if (context == null) return emptyList()
        return runCatching {
            val data = org.json.JSONObject(context.assets.open("ban_empty_reference.json").bufferedReader().use { it.readText() })
            val values = data.getJSONArray("pixels")
            val pixels = IntArray(values.length()) { values.getInt(it) or (0xff shl 24) }
            PortraitMatcher.references(pixels, data.getInt("width"), data.getInt("height"))
        }.getOrElse { emptyList() }.also { emptyBanReferenceCache = it }
    }

    /** Locates small header portraits at multiple scales, independently from the pick-column calibrator. */
    internal fun findHeaderBanObservations(bitmap: Bitmap, context: Context?, exclusions: List<android.graphics.Rect>, config: VisionCalibrationConfig = VisionCalibrationConfig()): List<BanObservation> {
        val bounds = DraftFrameGeometry.contentBounds(bitmap)
        if (bounds != android.graphics.Rect(0, 0, bitmap.width, bitmap.height)) {
            val content = Bitmap.createBitmap(bitmap, bounds.left, bounds.top, bounds.width(), bounds.height())
            try {
                fun x(value: Float) = (value * bitmap.width - bounds.left) / bounds.width()
                fun y(value: Float) = (value * bitmap.height - bounds.top) / bounds.height()
                val localConfig = config.copy(
                    allyBanXRatios = config.allyBanXRatios.map(::x), enemyBanXRatios = config.enemyBanXRatios.map(::x),
                    allyBanYRatio = y(config.allyBanYRatio), enemyBanYRatio = y(config.enemyBanYRatio),
                    banDiameterRatio = config.banDiameterRatio * bitmap.height / bounds.height())
                val masks = exclusions.mapNotNull { rect ->
                    android.graphics.Rect(rect).takeIf { it.intersect(bounds) }?.apply { offset(-bounds.left, -bounds.top) }
                }
                return findHeaderBanObservations(content, context, masks, localConfig).map { observation ->
                    observation.copy(region = BanRegion(
                        (bounds.left + observation.region.x * bounds.width()) / bitmap.width,
                        (bounds.top + observation.region.y * bounds.height()) / bitmap.height,
                        observation.region.radius * bounds.height() / bitmap.height))
                }
            } finally { content.recycle() }
        }
        // Header matching uses one physical scale on HD/FHD/QHD captures. Coordinates stay normalized.
        if (bitmap.height > 720) {
            val scale = 720f / bitmap.height
            val resized = Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt().coerceAtLeast(1), 720, true)
            try {
                val masks = exclusions.map { rect -> android.graphics.Rect(
                    kotlin.math.floor(rect.left * scale).toInt(), kotlin.math.floor(rect.top * scale).toInt(),
                    kotlin.math.ceil(rect.right * scale).toInt(), kotlin.math.ceil(rect.bottom * scale).toInt()) }
                return findHeaderBanObservations(resized, context, masks, config)
            } finally { resized.recycle() }
        }
        ensureIndexed(context)
        val catalog = WildRiftRepository.championSnapshot().distinctBy { it.id }
            .filter { championEmbeddingCache.containsKey(it.id) }
        if (catalog.isEmpty()) return emptyList()
        val height = bitmap.height
        val width = bitmap.width
        val sideWidth = banHeaderSideWidthRatio(width.toFloat() / height)
        val headerHeight = (height * .12f).toInt()
        val header = IntArray(width * headerHeight)
        bitmap.getPixels(header, 0, width, 0, 0, width, headerHeight)
        data class Proposal(val x: Int, val y: Int, val radius: Int, val score: Float,
                            val descriptor: FloatArray, val shortlist: List<Champion>)
        val proposals = mutableListOf<Proposal>()
        val emptyProposals = mutableListOf<Proposal>()
        val emptyReferences = emptyBanReferences(context)
        // Use image height to preserve physical portrait proportions on every aspect ratio.
        // Translation/scale refinement is performed against the existing local reference descriptors.
        for (ratio in floatArrayOf(.026f, .030f, .034f, .038f)) {
            val radius = (height * ratio).toInt().coerceAtLeast(6)
            val diameter = radius * 2
            if (diameter > headerHeight) continue
            val cropPixels = IntArray(diameter * diameter)
            for (yRatio in floatArrayOf(.044f, .048f, .052f, .056f, .060f)) {
                val y = (height * yRatio).toInt()
                if (y - radius < 0 || y + radius > headerHeight) continue
                for (x in radius until width - radius step (radius / 4).coerceAtLeast(3)) {
                    if (x > width * sideWidth && x < width * (1f - sideWidth)) continue
                    val cropRect = android.graphics.Rect(x - radius, y - radius, x + radius, y + radius)
                    if (exclusions.any { android.graphics.Rect.intersects(it, PortraitMatcher.sampledRect(cropRect)) }) continue
                    for (row in 0 until diameter) {
                        System.arraycopy(header, (y - radius + row) * width + x - radius, cropPixels, row * diameter, diameter)
                    }
                    val input = PortraitMatcher.descriptor(cropPixels, diameter, diameter)
                    val coarse = catalog.map { champ ->
                        champ to PortraitMatcher.correlation(input, championEmbeddingCache.getValue(champ.id)[13])
                    }.sortedByDescending { it.second }
                    if (coarse.first().second < .65f && emptyReferences.isNotEmpty() &&
                        PortraitMatcher.correlation(input, emptyReferences[13]) >= .65f) {
                        val score = PortraitMatcher.similarity(input, emptyReferences)
                        if (score >= .90f) emptyProposals.add(Proposal(x, y, radius, score, input, emptyList()))
                    }
                    if (coarse.first().second >= .60f && PortraitMatcher.hasSpatialContrast(cropPixels,diameter,diameter)) {
                        proposals.add(Proposal(x, y, radius, coarse.first().second, input, coarse.take(8).map { it.first }))
                    }
                }
            }
        }
        val found = mutableListOf<Pair<Proposal, Champion?>>()
        for (proposal in (proposals.sortedByDescending { it.score }.take(200) + emptyProposals.sortedByDescending { it.score }.take(40)).sortedByDescending { it.score }) {
            if (found.count { (it.first.x < width / 2) == (proposal.x < width / 2) } >= 7) continue
            if (found.any { kotlin.math.abs(it.first.x - proposal.x) < maxOf(it.first.radius, proposal.radius) * 1.4f }) continue
            if (proposal.shortlist.isEmpty()) {
                found.add(proposal to null)
                continue
            }
            // The coarse shortlist locates circles, not identities. A differently
            // scaled Mel/Darius can be absent from its eight candidates; compare
            // every local identity before accepting or computing the margin.
            val ranked = catalog.map { champ ->
                champ to PortraitMatcher.similarity(proposal.descriptor, championEmbeddingCache.getValue(champ.id))
            }.sortedByDescending { it.second }
            val best = ranked.first()
            if (PortraitMatcher.accepts(best.second, ranked.getOrNull(1)?.second ?: 0f, DEFAULT_CONFIDENCE_THRESHOLD)) {
                found.add(proposal.copy(score = best.second) to best.first)
            }
        }
        // A single gameplay HUD portrait must not be interpreted as a ban row.
        if (found.size < 3 || found.none { it.first.x < width / 2 } || found.none { it.first.x > width / 2 }) return emptyList()
        val observations = found.map { (proposal, champion) ->
            BanObservation(if (proposal.x < width / 2) BanSide.ALLY else BanSide.ENEMY,
                BanRegion(proposal.x.toFloat() / width, proposal.y.toFloat() / height,
                    proposal.radius.toFloat() / height), champion?.id, champion == null, proposal.score)
        }
        // A final unused rival ban may have no crossed-out emblem. Infer its position
        // only from a regular row of at least four observed circles near the screen edge,
        // and require a uniform interior. A textured unknown portrait stays unknown.
        val rivalRow = observations.filter { it.side == BanSide.ENEMY }.sortedBy { it.region.x }
        val finalEmpty = if (rivalRow.size >= 4) {
            val gaps = rivalRow.zipWithNext { a, b -> b.region.x - a.region.x }.sorted()
            val step = gaps[gaps.size / 2]
            val last = rivalRow.last().region
            val region = last.copy(x = last.x + step)
            val regular = gaps.all { gap ->
                val multiples = kotlin.math.round(gap / step).toInt()
                multiples in 1..3 && kotlin.math.abs(gap - multiples * step) < step * .25f
            }
            val candidateRect = android.graphics.Rect(((region.x * width) - region.radius * height).toInt(),
                ((region.y * height) - region.radius * height).toInt(),
                ((region.x * width) + region.radius * height).toInt(),
                ((region.y * height) + region.radius * height).toInt())
            if (regular && step > 0f && region.x > 1f - step * 1.5f &&
                region.x + region.radius * height / width < 1f &&
                exclusions.none { android.graphics.Rect.intersects(it, PortraitMatcher.sampledRect(candidateRect)) }) {
                val colors = (0 until 24).flatMap { angleIndex ->
                    val angle = angleIndex * Math.PI * 2 / 24
                    listOf(.3f, .4f, .5f).map { distance ->
                        val x = (region.x * width + region.radius * height * distance * kotlin.math.cos(angle)).toInt().coerceIn(0,width-1)
                        val y = (region.y * height + region.radius * height * distance * kotlin.math.sin(angle)).toInt().coerceIn(0,height-1)
                        bitmap.getPixel(x,y)
                    }
                }
                val variance = (0..2).sumOf { channel ->
                    val values = colors.map { ((it ushr (16-channel*8)) and 255).toDouble() }
                    val mean = values.average()
                    values.sumOf { (it-mean)*(it-mean) } / values.size
                } / 3.0
                if (variance < 36.0 && hasHeaderCircleRing(bitmap,region))
                    BanObservation(BanSide.ENEMY,region,empty=true) else null
            } else null
        } else null
        val positioned = fillHeaderPositions(observations + listOfNotNull(finalEmpty), width.toFloat() / height) { region ->
            val cx = region.x * width
            val cy = region.y * height
            val radius = region.radius * height
            val rect = android.graphics.Rect((cx-radius).toInt(), (cy-radius).toInt(),
                (cx+radius).toInt(), (cy+radius).toInt())
            if (exclusions.any { android.graphics.Rect.intersects(it, PortraitMatcher.sampledRect(rect)) }) false else {
                // A header portrait has spatial color variation; a blank background does not.
                // This only reserves an unknown position, never confirms a ban or its identity.
                val sums = DoubleArray(3); val squares = DoubleArray(3)
                var count = 0
                for (distance in floatArrayOf(.3f, .5f, .7f)) for (angleIndex in 0 until 24) {
                    val angle = angleIndex * Math.PI * 2 / 24
                    val x = (cx + radius * distance * kotlin.math.cos(angle)).toInt().coerceIn(0, width-1)
                    val y = (cy + radius * distance * kotlin.math.sin(angle)).toInt().coerceIn(0, height-1)
                    val pixel = bitmap.getPixel(x, y)
                    for (c in 0..2) {
                        val value = ((pixel ushr (16 - c*8)) and 255).toDouble()
                        sums[c] += value; squares[c] += value * value
                    }
                    count++
                }
                (0..2).sumOf { squares[it] / count - (sums[it] / count) * (sums[it] / count) } / 3 >= 144.0
            }
        }
        // Refine unknown and weakly matched positions. A single central template can miss
        // differently scaled portraits; the final confidence and separation gates stay unchanged.
        val calibrated = positioned.filter { if (it.side == BanSide.ALLY) !config.manualAllyBans else !config.manualEnemyBans } +
            BanSide.entries.flatMap { side ->
                val ally = side == BanSide.ALLY
                if (if (ally) config.manualAllyBans else config.manualEnemyBans)
                    config.banRegions(ally).map { BanObservation(side, it, null, false) } else emptyList()
            }
        return calibrated.map { observation ->
            if (observation.championId != null && observation.similarity >= .90f) return@map observation
            val region = observation.region
            val centerX = kotlin.math.round(region.x * width).toInt()
            val centerY = kotlin.math.round(region.y * height).toInt()
            val baseRadius = kotlin.math.round(region.radius * height).toInt()
            var bestScore = if (observation.championId != null) observation.similarity else 0f
            var refined = observation
            // Keep the best alignment at each radius: resampling can move the optimum
            // between neighbouring crop sizes even when coarse correlation is lower.
            val unresolved = mutableMapOf<Int, Triple<Float, FloatArray, List<Champion>>>()
            for (dx in intArrayOf(-4, -2, 0, 2, 4)) for (dy in -3..3) for (dr in -5..5) {
                val x = centerX + dx; val y = centerY + dy
                val radius = (baseRadius + dr).coerceAtLeast(6)
                val rect = android.graphics.Rect(x-radius, y-radius, x+radius, y+radius)
                if (rect.left < 0 || rect.right > width || rect.top < 0 || rect.bottom > headerHeight ||
                    exclusions.any { android.graphics.Rect.intersects(it, PortraitMatcher.sampledRect(rect)) }) continue
                val diameter = radius * 2
                val pixels = IntArray(diameter * diameter)
                for (row in 0 until diameter) System.arraycopy(header,
                    (y-radius+row)*width+x-radius, pixels, row*diameter, diameter)
                if(!PortraitMatcher.hasSpatialContrast(pixels,diameter,diameter)) continue
                val descriptor = PortraitMatcher.descriptor(pixels, diameter, diameter)
                val candidates = catalog.map { champion ->
                    val references = championEmbeddingCache.getValue(champion.id)
                    champion to intArrayOf(4, 13, 22).maxOf { PortraitMatcher.correlation(descriptor, references[it]) }
                }.sortedByDescending { it.second }.take(8)
                if (refined.championId == null && emptyReferences.isNotEmpty() &&
                    candidates.first().second < .65f && PortraitMatcher.similarity(descriptor, emptyReferences) >= .90f) {
                    refined = observation.copy(empty = true)
                }
                if (candidates.first().second < .60f) continue
                val ranked = candidates.map { (champion, _) -> champion to
                    PortraitMatcher.similarity(descriptor, championEmbeddingCache.getValue(champion.id))
                }.sortedByDescending { it.second }
                val best = ranked.first()
                if (best.second >= .70f && best.second > (unresolved[radius]?.first ?: 0f)) {
                    unresolved[radius] = Triple(best.second, descriptor, ranked.map { it.first })
                }
                if (best.second > bestScore && PortraitMatcher.accepts(best.second,
                        ranked.getOrNull(1)?.second ?: 0f, DEFAULT_CONFIDENCE_THRESHOLD)) {
                    bestScore = best.second
                    refined = observation.copy(region = BanRegion(x.toFloat()/width, y.toFloat()/height,
                        radius.toFloat()/height), championId = best.first.id, empty = false, similarity = best.second)
                }
            }
            val pending = unresolved.values.sortedByDescending { it.first }.take(4)
            val ctx = context ?: WildRiftApp.instance
            // An off-center crop may resemble the empty emblem while another crop
            // contains a textured portrait. Evaluate that stronger evidence before
            // committing an empty slot; keep both identity confidence gates.
            if (refined.championId == null && pending.isNotEmpty() && ctx != null) {
                val ranked = pending.flatMap { it.third }.distinctBy { it.id }.mapNotNull { champion ->
                    val asset = portraitAssetPaths[champion.id] ?: return@mapNotNull null
                    runCatching {
                        val bmp = ctx.assets.open("champions/$asset").use { BitmapFactory.decodeStream(it) } ?: return@runCatching null
                        try {
                            val pixels=IntArray(bmp.width*bmp.height)
                            bmp.getPixels(pixels,0,bmp.width,0,0,bmp.width,bmp.height)
                            champion to pending.maxOf { PortraitMatcher.fineSimilarity(it.second,pixels,bmp.width,bmp.height) }
                        } finally { bmp.recycle() }
                    }.getOrNull()
                }.sortedByDescending { it.second }
                val best=ranked.firstOrNull()
                if (best != null && PortraitMatcher.accepts(best.second,ranked.getOrNull(1)?.second ?: 0f,DEFAULT_CONFIDENCE_THRESHOLD)) {
                    refined=observation.copy(championId=best.first.id,empty=false,similarity=best.second)
                }
            }
            refined
        }
    }

    /** An unused ban has a real rim; uniform background after five bans has none. */
    private fun hasHeaderCircleRing(bitmap: Bitmap, region: BanRegion): Boolean {
        val radius = region.radius*bitmap.height
        fun light(distance: Float, angle: Double): Float {
            val x=(region.x*bitmap.width+radius*distance*kotlin.math.cos(angle)).toInt().coerceIn(0,bitmap.width-1)
            val y=(region.y*bitmap.height+radius*distance*kotlin.math.sin(angle)).toInt().coerceIn(0,bitmap.height-1)
            val pixel=bitmap.getPixel(x,y)
            return Color.red(pixel)*.299f+Color.green(pixel)*.587f+Color.blue(pixel)*.114f
        }
        var visibleArcs=0
        for(index in 0 until 32) {
            val angle=index*2.0*Math.PI/32
            val border=floatArrayOf(.90f,1f,1.10f,1.20f).maxOf { light(it,angle) }
            if(border>maxOf(light(.5f,angle),light(1.4f,angle))+12f) visibleArcs++
        }
        return visibleArcs>=8
    }

    /** Keep only evidence from a visible draft slot, never from the loading background. */
    internal fun hasSlotRing(bitmap: Bitmap, isAlly: Boolean): Boolean {
        var matches = 0
        val radii = doubleArrayOf(.44, .47, .50, .53, .56)
        for (i in 0 until 64) {
            val angle = i * 2.0 * Math.PI / 64
            // Selected portraits have a thin gold rim just outside the usual
            // team ring. Sampling one radius rejected Mel despite a clear match.
            for (radius in radii) {
                val x = (bitmap.width * (0.5 + radius * kotlin.math.cos(angle))).toInt().coerceIn(0, bitmap.width - 1)
                val y = (bitmap.height * (0.5 + radius * kotlin.math.sin(angle))).toInt().coerceIn(0, bitmap.height - 1)
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel); val g = Color.green(pixel); val b = Color.blue(pixel)
                val team = if (isAlly) b > 70 && b > r * 1.25f else r > 70 && r > g * 1.35f
                val selection = r > 90 && g > 60 && b < g * 0.8f
                if (team || selection) { matches++; break }
            }
        }
        return matches >= 10
    }

    suspend fun executeTenthPickInference(
        cropBitmap: Bitmap?, isAlly: Boolean, confirmedChampionIds: Set<String>,
        confirmedPicksCount: Int, slotIndex: Int = 4, context: Context? = null,
        confirmedTargetChampion: Champion? = null, allowVisualConfirmation: Boolean = true
    ): Pair<Champion, Int>? = withContext(Dispatchers.Default) {
        val key = "$isAlly:$slotIndex"
        if (targetKey != null && targetKey != key) reset()
        targetKey = key
        val slotDesc = if (isAlly) "Aliado ${slotIndex + 1} (10º Pick)" else "Rival ${slotIndex + 1} (10º Pick)"
        val threshold = getEffectiveThreshold(context)
        if (confirmedTargetChampion != null) {
            _reportFlow.value = LiteRTInferenceReport(
                status = EngineStatus.COMPLETED, pickedChampion = confirmedTargetChampion,
                confidencePercent = 100, isConfirmed = true, isSelectionFinalized = true, requiredStableFrames = 0,
                slotDescription = slotDesc, evaluatedPicksCount = confirmedPicksCount,
                decisionReason = "Nombre confirmado por texto: ${confirmedTargetChampion.name}",
                minConfidenceThreshold = threshold
            )
            lastVisibleSelection = _reportFlow.value
            return@withContext confirmedTargetChampion to 100
        }
        if (!allowVisualConfirmation) {
            resetStabilityTracker()
            _reportFlow.value = LiteRTInferenceReport(status = EngineStatus.WAITING_FOR_TENTH_PICK,
                slotDescription = slotDesc, evaluatedPicksCount = confirmedPicksCount,
                decisionReason = "Línea aliada visible; esperando selección.")
            return@withContext null
        }
        manuallyConfirmedChampion?.takeIf { it.id !in confirmedChampionIds }?.let { champion ->
            _reportFlow.value = _reportFlow.value.copy(status = EngineStatus.COMPLETED,
                pickedChampion = champion, confidencePercent = 100, isConfirmed = true, isSelectionFinalized = true,
                slotDescription = slotDesc, evaluatedPicksCount = confirmedPicksCount)
            lastVisibleSelection = _reportFlow.value
            return@withContext champion to 100
        }
        val previous = _reportFlow.value
        val now = android.os.SystemClock.elapsedRealtime()
        if (cropBitmap == null || cropBitmap.isRecycled || cropBitmap.width < 16 ||
            cropBitmap.height < 16 || !hasSlotRing(cropBitmap, isAlly)) {
            if (previous.isConfirmed && previous.pickedChampion != null) {
                return@withContext previous.pickedChampion to previous.confidencePercent
            }
            // A recent stable portrait can survive the slot disappearing at loading.
            if (frameStability.isRecent(now) && frameStability.count >= REQUIRED_STABLE_FRAMES &&
                confirmedPicksCount >= 9 && previous.pickedChampion != null) {
                _reportFlow.value = previous.copy(status = EngineStatus.COMPLETED, isConfirmed = true,
                    evaluatedPicksCount = confirmedPicksCount,
                    decisionReason = "Retrato estable conservado al desaparecer el slot.")
                return@withContext previous.pickedChampion to previous.confidencePercent
            }
            if (!frameStability.isRecent(now)) resetStabilityTracker()
            return@withContext null
        }
        if (isSlotWaitingIcon(cropBitmap, isAlly)) {
            resetStabilityTracker()
            _reportFlow.value = LiteRTInferenceReport(status = EngineStatus.WAITING_FOR_TENTH_PICK,
                slotDescription = slotDesc, evaluatedPicksCount = confirmedPicksCount,
                decisionReason = "Icono de espera; todavía no hay retrato.")
            return@withContext null
        }
        val ranked = rankSlotPortrait(cropBitmap, isAlly, context, confirmedChampionIds)
        val best = ranked.firstOrNull() ?: return@withContext null
        val second = ranked.getOrNull(1)?.second ?: 0f
        val accepted = PortraitMatcher.accepts(best.second, second, threshold)
        if (accepted && confirmedPicksCount >= 7) {
            frameStability.record(best.first.id,now,android.os.SystemClock.elapsedRealtime())
        } else resetStabilityTracker()
        val confirmed = accepted && frameStability.count >= REQUIRED_STABLE_FRAMES && confirmedPicksCount >= 9
        val weights = ranked.map { exp((it.second - best.second) / 0.08f) }
        val total = weights.sum().coerceAtLeast(1e-6f)
        val candidates = ranked.take(5).mapIndexed { i, (champ, score) ->
            LiteRTCandidateScore(champ, score, weights[i] / total, (score * 100).toInt(), i + 1)
        }
        val cropCopy = cropBitmap.copy(Bitmap.Config.ARGB_8888, false)
        val reason = when {
            confirmed -> "Retrato confirmado por coincidencia espacial y margen entre candidatos."
            !accepted -> "Coincidencia insuficiente o ambigua; no se confirma."
            else -> "Retrato en observación: ${frameStability.count}/$REQUIRED_STABLE_FRAMES frames; $confirmedPicksCount/9 selecciones previas."
        }
        _reportFlow.value = LiteRTInferenceReport(
            status = if (confirmed) EngineStatus.COMPLETED else EngineStatus.RUNNING_INFERENCE,
            pickedChampion = best.first, confidencePercent = (best.second * 100).toInt(),
            inferenceTimeMs = android.os.SystemClock.elapsedRealtime() - now,
            topCandidates = candidates, cropBitmap = cropCopy, decisionReason = reason,
            slotDescription = slotDesc, evaluatedPicksCount = confirmedPicksCount,
            isConfirmed = confirmed, stableFramesCount = frameStability.count,
            minConfidenceThreshold = threshold
        )
        // A single late classification must not replace the last stable portrait
        // just because the next frame is already the loading screen.
        if (confirmed) lastVisibleSelection = _reportFlow.value
        TenthPickDiagnosticManager.recordTenthPickCrop(cropCopy ?: cropBitmap, isAlly, slotIndex,
            stage = if (confirmed) "CONFIRMED" else "INFERENCE", candidateName = best.first.name,
            confidence = (best.second * 100).toInt(), similarityScore = best.second, context = context)
        if (confirmed) best.first to (best.second * 100).toInt() else null
    }

    fun isSlotWaitingIcon(bitmap: Bitmap, isAlly: Boolean): Boolean {
        val w = bitmap.width
        val h = bitmap.height
        if (w < 16 || h < 16) return true

        val cx = w / 2f
        val cy = h / 2f
        val radius = min(cx, cy)

        // Muestrear píxeles en el área central (0.15 * radius a 0.55 * radius)
        var totalSamples = 0
        var totalBrightness = 0f
        var totalColorVariance = 0f
        var maxBrightness = 0

        val step = max(1, (radius * 0.08f).toInt())
        val startY = (cy - radius * 0.55f).toInt()
        val endY = (cy + radius * 0.55f).toInt()
        val startX = (cx - radius * 0.55f).toInt()
        val endX = (cx + radius * 0.55f).toInt()

        for (y in startY until endY step step) {
            for (x in startX until endX step step) {
                val dx = x - cx
                val dy = y - cy
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < radius * 0.15f || dist > radius * 0.55f) continue

                val px = bitmap.getPixel(x.coerceIn(0, w - 1), y.coerceIn(0, h - 1))
                val r = Color.red(px)
                val g = Color.green(px)
                val b = Color.blue(px)
                val lum = (0.299f * r + 0.587f * g + 0.114f * b).toInt()
                val colorDiff = (max(r, max(g, b)) - min(r, min(g, b))).toFloat()

                totalSamples++
                totalBrightness += lum
                totalColorVariance += colorDiff
                if (lum > maxBrightness) maxBrightness = lum
            }
        }

        if (totalSamples == 0) return true
        val avgBrightness = totalBrightness / totalSamples
        val avgColorDiff = totalColorVariance / totalSamples

        // El yelmo espartano (rival) o el icono de línea (aliado) son siluetas neutras y oscuras sin saturación:
        // - El brillo promedio en su interior es extremadamente bajo (< 22 de 255).
        // - No contienen ninguna zona con brillo superior a 55.
        // - La varianza de color (saturación) es prácticamente nula (< 8).
        // Cualquier campeón (incluso de tonalidad oscura como Vi con pelo rojizo, Zed o Viego)
        // posee varianza cromática o zonas de luz superiores, pasando de inmediato a la inferencia LiteRT.
        val isIcon = (avgBrightness < 22f && maxBrightness < 55 && avgColorDiff < 8f)
        return isIcon
    }

    fun manuallyConfirmTenthPick(champion: Champion) {
        manuallyConfirmedChampion = champion
        _reportFlow.value = _reportFlow.value.copy(status = EngineStatus.COMPLETED,
            pickedChampion = champion, confidencePercent = 100, isConfirmed = true, isSelectionFinalized = true,
            decisionReason = "Confirmado manualmente: ${champion.name}")
        lastVisibleSelection = _reportFlow.value
    }

    fun reset() {
        lastVisibleSelection = null
        resetStabilityTracker()
        targetKey = null
        manuallyConfirmedChampion = null
        _reportFlow.value = LiteRTInferenceReport()
    }
}
