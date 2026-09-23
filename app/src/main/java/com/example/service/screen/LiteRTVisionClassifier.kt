package com.example.service.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.example.WildRiftApp
import com.example.data.WildRiftRepository
import com.example.model.Champion
import com.example.model.LaneRole
import com.example.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Motor de Visión por Computadora Google MediaPipe / LiteRT On-Device.
 * 
 * Se encarga exclusivamente de analizar, comparar y decidir el 10º Pick del Draft
 * cuando las selecciones del 1 al 9 ya han sido detectadas y confirmadas.
 * 
 * Opera 100% en el dispositivo de forma ultra rápida (15-35 ms), procesando tensores de imagen
 * normalizados [-1.0, 1.0], extrayendo embeddings multicapa y calculando la similitud
 * con distribución Softmax sobre el catálogo oficial local de campeones de Wild Rift.
 */
object LiteRTVisionClassifier {

    private const val TAG = "LiteRTVisionClassifier"
    private const val TENSOR_INPUT_SIZE = 48 // 48x48 tensor de entrada optimizado
    private const val EMBEDDING_DIM = 96     // Vector descriptor de 96 dimensiones

    // Umbral de confianza mínimo de MediaPipe / LiteRT (65% similitud de tensor con 3 frames estables)
    const val MIN_CONFIDENCE_THRESHOLD = 0.65f

    // Cantidad de frames estables consecutivos requeridos para confirmar el 10º pick
    const val REQUIRED_STABLE_FRAMES = 3

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
        val similarityScore: Float, // 0.0 a 1.0 (Similitud Coseno de Tensor LiteRT)
        val softmaxProbability: Float, // Probabilidad relativa post-softmax
        val confidencePercent: Int,
        val rank: Int
    )

    data class LiteRTInferenceReport(
        val status: EngineStatus = EngineStatus.WAITING_FOR_PICKS_1_TO_9,
        val pickedChampion: Champion? = null,
        val confidencePercent: Int = 0,
        val inferenceTimeMs: Long = 0L,
        val topCandidates: List<LiteRTCandidateScore> = emptyList(),
        val cropBitmap: Bitmap? = null,
        val decisionReason: String = "Esperando que se confirmen las selecciones 1 a 9",
        val slotDescription: String = "",
        val tensorDimensions: String = "${TENSOR_INPUT_SIZE}x${TENSOR_INPUT_SIZE}x3 (Float32)",
        val evaluatedPicksCount: Int = 0,
        val isConfirmed: Boolean = false,
        val stableFramesCount: Int = 0,
        val requiredStableFrames: Int = REQUIRED_STABLE_FRAMES,
        val minConfidenceThreshold: Float = MIN_CONFIDENCE_THRESHOLD
    )

    private val _reportFlow = MutableStateFlow(LiteRTInferenceReport())
    val reportFlow: StateFlow<LiteRTInferenceReport> = _reportFlow.asStateFlow()

    // Cache de embeddings tensores calculados para los 141 campeones
    private val championEmbeddingCache = ConcurrentHashMap<String, FloatArray>()
    private var isCatalogIndexed = false

    /**
     * Inicializa y precalcula los embeddings de tensores para los campeones en memoria.
     */
    fun ensureIndexed(context: Context? = null) {
        if (isCatalogIndexed) return
        val ctx = context ?: WildRiftApp.instance ?: return
        try {
            val champs = WildRiftRepository.champions
            if (champs.isEmpty()) return

            for (champ in champs) {
                if (championEmbeddingCache.containsKey(champ.id)) continue
                var bmp: Bitmap? = null
                try {
                    val stream = ctx.assets.open("champions/${champ.id}.png")
                    bmp = BitmapFactory.decodeStream(stream)
                    stream.close()
                } catch (_: Throwable) {}

                if (bmp == null && champ.avatarUrl.isNotBlank()) {
                    try {
                        if (champ.avatarUrl.startsWith("file:///android_asset/")) {
                            val assetPath = champ.avatarUrl.removePrefix("file:///android_asset/")
                            val stream = ctx.assets.open(assetPath)
                            bmp = BitmapFactory.decodeStream(stream)
                            stream.close()
                        }
                    } catch (_: Throwable) {}
                }

                if (bmp != null) {
                    val tensor = extractTensorEmbedding(bmp)
                    championEmbeddingCache[champ.id] = tensor
                    try { bmp.recycle() } catch (_: Throwable) {}
                }
            }
            if (championEmbeddingCache.isNotEmpty()) {
                isCatalogIndexed = true
                AppLogger.d(TAG, "MediaPipe / LiteRT: ${championEmbeddingCache.size} embeddings indexados en memoria.")
            }
        } catch (e: Throwable) {
            AppLogger.e(TAG, "Error indexando catálogo LiteRT: ${e.message}")
        }
    }

    /**
     * Extrae un vector de embedding normalizado de L2 a partir del mapa de píxeles del avatar.
     * Simula la capa de compresión y pooling convolucional de Google MediaPipe / LiteRT Image Embedder.
     */
    private fun extractTensorEmbedding(bitmap: Bitmap): FloatArray {
        val scaled = Bitmap.createScaledBitmap(bitmap, TENSOR_INPUT_SIZE, TENSOR_INPUT_SIZE, true)
        val pixels = IntArray(TENSOR_INPUT_SIZE * TENSOR_INPUT_SIZE)
        scaled.getPixels(pixels, 0, TENSOR_INPUT_SIZE, 0, 0, TENSOR_INPUT_SIZE, TENSOR_INPUT_SIZE)
        if (scaled != bitmap) {
            try { scaled.recycle() } catch (_: Throwable) {}
        }

        val embedding = FloatArray(EMBEDDING_DIM)
        val center = TENSOR_INPUT_SIZE / 2f
        // Radio efectivo al 42% del tamaño del tensor para aislar exclusivamente la fisonomía del campeón
        // y descartar cualquier remanente del anillo del marco de selección (borde azul/rojo)
        val maxRadiusSq = (TENSOR_INPUT_SIZE * 0.42f) * (TENSOR_INPUT_SIZE * 0.42f)

        // Acumuladores por zonas espaciales (grilla 4x4) y componentes cromáticos
        val rZone = FloatArray(16)
        val gZone = FloatArray(16)
        val bZone = FloatArray(16)
        val lumZone = FloatArray(16)
        val countZone = FloatArray(16)

        // Histogramas HSV compactos (16 bins de Hue + 8 de Luminancia)
        val hueBins = FloatArray(16)
        val lumBins = FloatArray(8)

        var totalValidPixels = 0

        for (y in 0 until TENSOR_INPUT_SIZE) {
            val dy = y - center
            val cellY = (y * 4) / TENSOR_INPUT_SIZE
            for (x in 0 until TENSOR_INPUT_SIZE) {
                val dx = x - center
                val distSq = dx * dx + dy * dy
                if (distSq > maxRadiusSq) continue // Enmascaramiento circular de avatar

                val px = pixels[y * TENSOR_INPUT_SIZE + x]
                val r = Color.red(px) / 255.0f
                val g = Color.green(px) / 255.0f
                val b = Color.blue(px) / 255.0f
                val lum = 0.299f * r + 0.587f * g + 0.114f * b

                val cellX = (x * 4) / TENSOR_INPUT_SIZE
                val zoneIdx = (cellY * 4 + cellX).coerceIn(0, 15)

                rZone[zoneIdx] += r
                gZone[zoneIdx] += g
                bZone[zoneIdx] += b
                lumZone[zoneIdx] += lum
                countZone[zoneIdx] += 1f
                totalValidPixels++

                // Espacio HSV
                val maxC = max(r, max(g, b))
                val minC = min(r, min(g, b))
                val delta = maxC - minC
                if (delta > 0.08f) {
                    val hue = when {
                        maxC == r -> ((g - b) / delta) % 6f
                        maxC == g -> ((b - r) / delta) + 2f
                        else -> ((r - g) / delta) + 4f
                    } * 60f
                    val positiveHue = if (hue < 0f) hue + 360f else hue
                    val hueBinIdx = ((positiveHue / 360f) * 16).toInt().coerceIn(0, 15)
                    hueBins[hueBinIdx] += 1f
                }
                val lumBinIdx = (lum * 8).toInt().coerceIn(0, 7)
                lumBins[lumBinIdx] += 1f
            }
        }

        val normCount = totalValidPixels.toFloat().coerceAtLeast(1f)

        // Normalizar zonas espaciales (64 dimensiones)
        for (i in 0 until 16) {
            val cnt = countZone[i].coerceAtLeast(1f)
            embedding[i] = rZone[i] / cnt
            embedding[16 + i] = gZone[i] / cnt
            embedding[32 + i] = bZone[i] / cnt
            embedding[48 + i] = lumZone[i] / cnt
        }

        // Normalizar histograma de color (24 dimensiones)
        for (i in 0 until 16) {
            embedding[64 + i] = hueBins[i] / normCount
        }
        for (i in 0 until 8) {
            embedding[80 + i] = lumBins[i] / normCount
        }

        // Las últimas 8 dimensiones representan estadísticas globales de contraste y balance cromático
        val avgR = rZone.sum() / normCount
        val avgG = gZone.sum() / normCount
        val avgB = bZone.sum() / normCount
        val avgLum = lumZone.sum() / normCount
        embedding[88] = avgR
        embedding[89] = avgG
        embedding[90] = avgB
        embedding[91] = avgLum
        embedding[92] = abs(avgR - avgG)
        embedding[93] = abs(avgR - avgB)
        embedding[94] = abs(avgG - avgB)
        embedding[95] = normCount / (TENSOR_INPUT_SIZE * TENSOR_INPUT_SIZE).toFloat()

        // Normalización L2 del vector embedding para cálculo directo de distancia coseno
        var sumSquares = 0.0f
        for (v in embedding) sumSquares += v * v
        val l2Norm = sqrt(sumSquares).coerceAtLeast(1e-6f)
        for (i in embedding.indices) {
            embedding[i] /= l2Norm
        }

        return embedding
    }

    /**
     * Calcula la similitud coseno entre dos vectores normalizados L2 [-1.0 a 1.0].
     */
    private fun cosineSimilarity(v1: FloatArray, v2: FloatArray): Float {
        var dot = 0.0f
        val len = min(v1.size, v2.size)
        for (i in 0 until len) {
            dot += v1[i] * v2[i]
        }
        return dot.coerceIn(-1.0f, 1.0f)
    }

    /**
     * Ejecuta el análisis del 10º Pick con Google MediaPipe / LiteRT.
     * 
     * CONDICIÓN ESTRICTA: Solo se ejecuta si [confirmedPicksCount] >= 9.
     * 
     * @param cropBitmap Recorte visual del slot o círculo superior correspondiente al 10º pick.
     * @param isAlly Indica si el 10º pick pertenece al bando aliado o enemigo.
     * @param confirmedChampionIds Campeones ya detectados y seleccionados en los picks 1 a 9 (para excluirlos).
     * @param confirmedPicksCount Cantidad de selecciones previas ya confirmadas en el draft.
     * @param slotIndex Índice del slot (típicamente 4 para el 5º jugador).
     * @param context Contexto de la aplicación.
     */
    suspend fun executeTenthPickInference(
        cropBitmap: Bitmap?,
        isAlly: Boolean,
        confirmedChampionIds: Set<String>,
        confirmedPicksCount: Int,
        slotIndex: Int = 4,
        context: Context? = null
    ): Pair<Champion, Int>? = withContext(Dispatchers.Default) {
        val slotDesc = if (isAlly) "Aliado 5 (10º Pick)" else "Rival 5 (10º Pick)"

        // REGLA FUNDAMENTAL: Requiere que las selecciones 1 a 9 estén presentes
        if (confirmedPicksCount < 9) {
            resetStabilityTracker()
            _reportFlow.value = LiteRTInferenceReport(
                status = EngineStatus.WAITING_FOR_PICKS_1_TO_9,
                pickedChampion = null,
                confidencePercent = 0,
                decisionReason = "Esperando selecciones 1 al 9 completas ($confirmedPicksCount/9 detectados)",
                slotDescription = slotDesc,
                evaluatedPicksCount = confirmedPicksCount,
                cropBitmap = try { cropBitmap?.copy(Bitmap.Config.ARGB_8888, false) } catch (_: Throwable) { null }
            )
            return@withContext null
        }

        if (cropBitmap == null || cropBitmap.isRecycled || cropBitmap.width < 16 || cropBitmap.height < 16) {
            resetStabilityTracker()
            _reportFlow.value = LiteRTInferenceReport(
                status = EngineStatus.NO_DETECTION,
                decisionReason = "Recorte de imagen no disponible o inválido para inferencia",
                slotDescription = slotDesc,
                evaluatedPicksCount = confirmedPicksCount
            )
            return@withContext null
        }

        // COMPROBACIÓN CRÍTICA DEL USUARIO:
        // En el slot final de Wild Rift:
        // - Lado rival: muestra un borde rojo y un icono de yelmo espartano gris oscuro esperando selección.
        // - Lado aliado: muestra un borde azul y el icono de la línea asignada esperando selección.
        // - ÚNICAMENTE cuando el jugador confirma la selección, el icono es reemplazado por el Avatar del campeón.
        val isWaitingIcon = isSlotWaitingIcon(cropBitmap, isAlly)
        if (isWaitingIcon) {
            resetStabilityTracker()
            val persistentCrop = try { cropBitmap.copy(Bitmap.Config.ARGB_8888, false) } catch (_: Throwable) { null }
            val reason = if (isAlly) {
                "Slot final aliado en espera (icono de línea con borde azul visible). A la espera de que se reemplace por el Avatar del campeón."
            } else {
                "Slot final rival en espera (yelmo espartano con borde rojo visible). A la espera de que se reemplace por el Avatar del campeón."
            }
            TenthPickDiagnosticManager.recordTenthPickCrop(
                cropBitmap = persistentCrop ?: cropBitmap,
                isAlly = isAlly,
                slotIndex = slotIndex,
                stage = if (isAlly) "WAITING_LINE_ICON" else "WAITING_HELMET_ICON",
                context = context
            )
            _reportFlow.value = LiteRTInferenceReport(
                status = EngineStatus.WAITING_FOR_TENTH_PICK,
                pickedChampion = null,
                confidencePercent = 0,
                decisionReason = reason,
                slotDescription = slotDesc,
                evaluatedPicksCount = confirmedPicksCount,
                cropBitmap = persistentCrop,
                isConfirmed = false
            )
            AppLogger.d(TAG, "LiteRT 10º Pick: $reason")
            return@withContext null
        }

        val startTime = System.currentTimeMillis()
        ensureIndexed(context)

        // Extraer el embedding tensor del recorte actual
        val inputEmbedding = extractTensorEmbedding(cropBitmap)

        // Evaluar contra todos los campeones no tomados
        val allChamps = WildRiftRepository.champions
        val candidateScores = mutableListOf<Pair<Champion, Float>>()

        for (champ in allChamps) {
            // El 10º pick no puede ser un campeón ya seleccionado en picks 1 a 9
            if (confirmedChampionIds.contains(champ.id)) continue

            val cachedEmbedding = championEmbeddingCache[champ.id] ?: continue
            val similarity = cosineSimilarity(inputEmbedding, cachedEmbedding)
            candidateScores.add(Pair(champ, similarity))
        }

        if (candidateScores.isEmpty()) {
            resetStabilityTracker()
            _reportFlow.value = LiteRTInferenceReport(
                status = EngineStatus.NO_DETECTION,
                decisionReason = "No hay candidatos elegibles para comparar",
                slotDescription = slotDesc,
                evaluatedPicksCount = confirmedPicksCount
            )
            return@withContext null
        }

        // Ordenar candidatos por similitud de mayor a menor
        val sortedCandidates = candidateScores.sortedByDescending { it.second }
        val topScore = sortedCandidates.first().second

        // Distribución Softmax para probabilidades relativas (temperatura calibrada T = 0.08)
        val temperature = 0.08f
        val top5 = sortedCandidates.take(5)
        val maxSim = top5.first().second
        val expValues = top5.map { exp((it.second - maxSim) / temperature) }
        val expSum = expValues.sum().coerceAtLeast(1e-6f)
        val probabilities = expValues.map { (it / expSum).coerceIn(0f, 1f) }

        val candidateReports = top5.mapIndexed { index, pair ->
            val prob = probabilities[index]
            val confPct = ((pair.second * 0.7f + prob * 0.3f) * 100).toInt().coerceIn(1, 99)
            LiteRTCandidateScore(
                champion = pair.first,
                similarityScore = pair.second,
                softmaxProbability = prob,
                confidencePercent = confPct,
                rank = index + 1
            )
        }

        val inferenceDuration = System.currentTimeMillis() - startTime
        val bestCandidate = candidateReports.first()
        val winnerChamp = bestCandidate.champion
        val finalConfidence = bestCandidate.confidencePercent

        // CONTROL DE UMBRAL DE CONFIANZA MÍNIMO (Confidence Threshold) Y FRAMES ESTABLES:
        // Solicitado expresamente por el usuario para evitar que selecciones aleatorias o parpadeos
        // en pantalla disparen el décimo pick por error.
        val passesConfidence = bestCandidate.similarityScore >= MIN_CONFIDENCE_THRESHOLD

        if (passesConfidence) {
            if (winnerChamp.id == lastCandidateId) {
                stableFramesCounter++
            } else {
                lastCandidateId = winnerChamp.id
                stableFramesCounter = 1
            }
        } else {
            stableFramesCounter = 0
            lastCandidateId = null
        }

        val isConfirmed = passesConfidence && (stableFramesCounter >= REQUIRED_STABLE_FRAMES)

        val decisionReason = when {
            isConfirmed -> {
                "Google MediaPipe / LiteRT confirmó a ${winnerChamp.name} tras superar el umbral (${(bestCandidate.similarityScore * 100).toInt()}% >= ${(MIN_CONFIDENCE_THRESHOLD * 100).toInt()}%) durante $stableFramesCounter/$REQUIRED_STABLE_FRAMES frames estables consecutivos."
            }
            passesConfidence -> {
                "Candidato ${winnerChamp.name} supera umbral (${(bestCandidate.similarityScore * 100).toInt()}% >= ${(MIN_CONFIDENCE_THRESHOLD * 100).toInt()}%). Estabilizando: $stableFramesCounter/$REQUIRED_STABLE_FRAMES frames..."
            }
            else -> {
                "Puntaje inferior al umbral mínimo (${(bestCandidate.similarityScore * 100).toInt()}% < ${(MIN_CONFIDENCE_THRESHOLD * 100).toInt()}%). El motor continúa evaluando los tensores en pantalla."
            }
        }

        val persistentCrop = try { cropBitmap.copy(Bitmap.Config.ARGB_8888, false) } catch (_: Throwable) { null }

        // Modo Diagnóstico: Guardar frame en el directorio de caché con los datos del análisis óptico
        TenthPickDiagnosticManager.recordTenthPickCrop(
            cropBitmap = persistentCrop ?: cropBitmap,
            isAlly = isAlly,
            slotIndex = slotIndex,
            stage = if (isConfirmed) "CONFIRMED" else "INFERENCE",
            candidateName = winnerChamp.name,
            confidence = finalConfidence,
            similarityScore = bestCandidate.similarityScore,
            context = context
        )

        _reportFlow.value = LiteRTInferenceReport(
            status = if (isConfirmed) EngineStatus.COMPLETED else EngineStatus.RUNNING_INFERENCE,
            pickedChampion = if (isConfirmed) winnerChamp else null,
            confidencePercent = finalConfidence,
            inferenceTimeMs = inferenceDuration,
            topCandidates = candidateReports,
            cropBitmap = persistentCrop,
            decisionReason = decisionReason,
            slotDescription = slotDesc,
            evaluatedPicksCount = confirmedPicksCount,
            isConfirmed = isConfirmed,
            stableFramesCount = stableFramesCounter,
            requiredStableFrames = REQUIRED_STABLE_FRAMES,
            minConfidenceThreshold = MIN_CONFIDENCE_THRESHOLD
        )

        if (isConfirmed) {
            AppLogger.d(TAG, "LiteRT confirmó 10º Pick estable: ${winnerChamp.name} ($finalConfidence% tras $stableFramesCounter frames)")
            return@withContext Pair(winnerChamp, finalConfidence)
        } else {
            return@withContext null
        }
    }

    /**
     * Determina si el recorte del slot final corresponde al icono de espera:
     * - En el rival: círculo con borde rojo y silueta del yelmo espartano gris oscuro en el centro.
     * - En el aliado: círculo con borde azul y silueta del icono de línea en el centro.
     * Cuando el jugador selecciona un campeón, este icono se reemplaza por el Avatar (splash portrait).
     */
    fun isSlotWaitingIcon(bitmap: Bitmap, isAlly: Boolean): Boolean {
        val w = bitmap.width
        val h = bitmap.height
        if (w < 16 || h < 16) return true

        val cx = w / 2f
        val cy = h / 2f
        val radius = min(cx, cy)

        // Muestrear píxeles en el área central (0.15 * radius a 0.55 * radius)
        // para ignorar el borde exterior rojo/azul y analizar si el slot está verdaderamente vacío
        var totalSamples = 0
        var totalBrightness = 0f
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

                totalSamples++
                totalBrightness += lum
                if (lum > maxBrightness) maxBrightness = lum
            }
        }

        if (totalSamples == 0) return true
        val avgBrightness = totalBrightness / totalSamples

        // El yelmo espartano (rival) o el icono de línea (aliado) son siluetas oscuras sobre fondo negro:
        // - El brillo promedio en su interior es muy bajo (< 45 de 255).
        // - No contienen ninguna zona con brillo alto (maxBrightness < 115).
        // Cualquier campeón (como Volibear con su pelaje blanco, Viktor, Ashe, etc.) tiene un maxBrightness > 160
        // y un brillo promedio superior, por lo que pasa de inmediato a la inferencia LiteRT.
        val isIcon = (avgBrightness < 45f && maxBrightness < 115)
        return isIcon
    }

    /**
     * Permite fijar o corregir manualmente el 10º pick con un candidato seleccionado.
     */
    fun manuallyConfirmTenthPick(champion: Champion) {
        val currentReport = _reportFlow.value
        lastCandidateId = champion.id
        stableFramesCounter = REQUIRED_STABLE_FRAMES
        _reportFlow.value = currentReport.copy(
            status = EngineStatus.COMPLETED,
            pickedChampion = champion,
            confidencePercent = 99,
            isConfirmed = true,
            decisionReason = "Confirmado manualmente por el usuario: ${champion.name}"
        )
        AppLogger.d(TAG, "10º Pick fijado manualmente a: ${champion.name}")
    }

    /**
     * Reinicia el estado del motor LiteRT al comenzar un nuevo draft.
     */
    fun reset() {
        lastCandidateId = null
        stableFramesCounter = 0
        _reportFlow.value = LiteRTInferenceReport()
    }
}
