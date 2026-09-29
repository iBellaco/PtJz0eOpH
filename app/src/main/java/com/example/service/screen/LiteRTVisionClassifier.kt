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
    private var lastCandidateId: String? = null
    private var stableFramesCounter: Int = 0

    fun resetStabilityTracker() {
        lastCandidateId = null
        stableFramesCounter = 0
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
        val stableFramesCount: Int = 0,
        val requiredStableFrames: Int = REQUIRED_STABLE_FRAMES,
        val minConfidenceThreshold: Float = DEFAULT_CONFIDENCE_THRESHOLD
    )

    private val _reportFlow = MutableStateFlow(LiteRTInferenceReport())
    val reportFlow: StateFlow<LiteRTInferenceReport> = _reportFlow.asStateFlow()

    private val championEmbeddingCache = ConcurrentHashMap<String, List<FloatArray>>()
    private var indexedIds: Set<String> = emptySet()
    private var lastValidFrameAt = 0L
    private var targetKey: String? = null

    @Synchronized
    fun ensureIndexed(context: Context? = null) {
        val ctx = context ?: WildRiftApp.instance ?: return
        val champs = WildRiftRepository.champions
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
                } finally { bmp.recycle() }
            } catch (e: Exception) {
                AppLogger.w(TAG, "Retrato no disponible: ${champ.id}")
            }
        }
        // Retry missing assets on subsequent scans; never mark a partial catalog complete.
        indexedIds = championEmbeddingCache.keys.toSet()
    }

    /** Keep only evidence from a visible draft slot, never from the loading background. */
    private fun hasSlotRing(bitmap: Bitmap, isAlly: Boolean): Boolean {
        var matches = 0
        for (i in 0 until 64) {
            val angle = i * 2.0 * Math.PI / 64
            val x = (bitmap.width * (0.5 + 0.47 * kotlin.math.cos(angle))).toInt().coerceIn(0, bitmap.width - 1)
            val y = (bitmap.height * (0.5 + 0.47 * kotlin.math.sin(angle))).toInt().coerceIn(0, bitmap.height - 1)
            val pixel = bitmap.getPixel(x, y)
            val r = Color.red(pixel); val g = Color.green(pixel); val b = Color.blue(pixel)
            val team = if (isAlly) b > 70 && b > r * 1.25f else r > 70 && r > g * 1.35f
            val selection = r > 90 && g > 60 && b < g * 0.8f
            if (team || selection) matches++
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
                confidencePercent = 100, isConfirmed = true, requiredStableFrames = 0,
                slotDescription = slotDesc, evaluatedPicksCount = confirmedPicksCount,
                decisionReason = "Nombre confirmado por texto: ${confirmedTargetChampion.name}",
                minConfidenceThreshold = threshold
            )
            return@withContext confirmedTargetChampion to 100
        }
        if (!allowVisualConfirmation) {
            resetStabilityTracker()
            _reportFlow.value = LiteRTInferenceReport(status = EngineStatus.WAITING_FOR_TENTH_PICK,
                slotDescription = slotDesc, evaluatedPicksCount = confirmedPicksCount,
                decisionReason = "Línea aliada visible; esperando selección.")
            return@withContext null
        }
        val previous = _reportFlow.value
        val now = android.os.SystemClock.elapsedRealtime()
        if (cropBitmap == null || cropBitmap.isRecycled || cropBitmap.width < 16 ||
            cropBitmap.height < 16 || !hasSlotRing(cropBitmap, isAlly)) {
            if (previous.isConfirmed && previous.pickedChampion != null) {
                return@withContext previous.pickedChampion to previous.confidencePercent
            }
            // A recent stable portrait can survive the slot disappearing at loading.
            if (now - lastValidFrameAt <= 1200L && stableFramesCounter >= REQUIRED_STABLE_FRAMES &&
                confirmedPicksCount >= 9 && previous.pickedChampion != null) {
                _reportFlow.value = previous.copy(status = EngineStatus.COMPLETED, isConfirmed = true,
                    evaluatedPicksCount = confirmedPicksCount,
                    decisionReason = "Retrato estable conservado al desaparecer el slot.")
                return@withContext previous.pickedChampion to previous.confidencePercent
            }
            if (now - lastValidFrameAt > 1200L) resetStabilityTracker()
            return@withContext null
        }
        if (isSlotWaitingIcon(cropBitmap, isAlly)) {
            resetStabilityTracker()
            _reportFlow.value = LiteRTInferenceReport(status = EngineStatus.WAITING_FOR_TENTH_PICK,
                slotDescription = slotDesc, evaluatedPicksCount = confirmedPicksCount,
                decisionReason = "Icono de espera; todavía no hay retrato.")
            return@withContext null
        }
        ensureIndexed(context)
        val pixels = IntArray(cropBitmap.width * cropBitmap.height)
        cropBitmap.getPixels(pixels, 0, cropBitmap.width, 0, 0, cropBitmap.width, cropBitmap.height)
        val input = PortraitMatcher.descriptor(pixels, cropBitmap.width, cropBitmap.height)
        val ranked = WildRiftRepository.champions.filter { it.id !in confirmedChampionIds }
            .mapNotNull { champ -> championEmbeddingCache[champ.id]?.let {
                champ to PortraitMatcher.similarity(input, it)
            } }.sortedByDescending { it.second }
        val best = ranked.firstOrNull() ?: return@withContext null
        val second = ranked.getOrNull(1)?.second ?: 0f
        val accepted = PortraitMatcher.accepts(best.second, second, threshold)
        if (now - lastValidFrameAt > 1200L) resetStabilityTracker()
        if (accepted && confirmedPicksCount >= 7) {
            stableFramesCounter = if (lastCandidateId == best.first.id) stableFramesCounter + 1 else 1
            lastCandidateId = best.first.id
            lastValidFrameAt = now
        } else resetStabilityTracker()
        stableFramesCounter = stableFramesCounter.coerceAtMost(REQUIRED_STABLE_FRAMES)
        val confirmed = accepted && stableFramesCounter >= REQUIRED_STABLE_FRAMES && confirmedPicksCount >= 9
        val weights = ranked.map { exp((it.second - best.second) / 0.08f) }
        val total = weights.sum().coerceAtLeast(1e-6f)
        val candidates = ranked.take(5).mapIndexed { i, (champ, score) ->
            LiteRTCandidateScore(champ, score, weights[i] / total, (score * 100).toInt(), i + 1)
        }
        val cropCopy = cropBitmap.copy(Bitmap.Config.ARGB_8888, false)
        val reason = when {
            confirmed -> "Retrato confirmado por coincidencia espacial y margen entre candidatos."
            !accepted -> "Coincidencia insuficiente o ambigua; no se confirma."
            else -> "Retrato en observación: $stableFramesCounter/$REQUIRED_STABLE_FRAMES frames; $confirmedPicksCount/9 selecciones previas."
        }
        _reportFlow.value = LiteRTInferenceReport(
            status = if (confirmed) EngineStatus.COMPLETED else EngineStatus.RUNNING_INFERENCE,
            pickedChampion = best.first, confidencePercent = (best.second * 100).toInt(),
            inferenceTimeMs = android.os.SystemClock.elapsedRealtime() - now,
            topCandidates = candidates, cropBitmap = cropCopy, decisionReason = reason,
            slotDescription = slotDesc, evaluatedPicksCount = confirmedPicksCount,
            isConfirmed = confirmed, stableFramesCount = stableFramesCounter,
            minConfidenceThreshold = threshold
        )
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
        _reportFlow.value = _reportFlow.value.copy(status = EngineStatus.COMPLETED,
            pickedChampion = champion, isConfirmed = true,
            decisionReason = "Confirmado manualmente: ${champion.name}")
    }

    fun reset() {
        resetStabilityTracker()
        targetKey = null
        lastValidFrameAt = 0L
        _reportFlow.value = LiteRTInferenceReport()
    }
}
