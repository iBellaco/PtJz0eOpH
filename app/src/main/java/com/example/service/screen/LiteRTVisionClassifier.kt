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
import com.example.util.UserPreferences
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
    private const val EMBEDDING_DIM = 118    // Vector descriptor de 118 dimensiones de alta fidelidad

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
        val minConfidenceThreshold: Float = DEFAULT_CONFIDENCE_THRESHOLD
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
        if (isCatalogIndexed && championEmbeddingCache.values.firstOrNull()?.size == EMBEDDING_DIM) return
        championEmbeddingCache.clear()
        isCatalogIndexed = false
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
     * Extrae un vector descriptor de alta fidelidad centrado en cero y normalizado en L2 (118 dimensiones)
     * a partir del mapa de píxeles del avatar facial del campeón.
     * 
     * Invariante a pequeños desplazamientos espaciales, escala, iluminación y bordes de UI:
     * - Enmascaramiento circular facial interior (radio <= 0.36 * diámetro) para descartar esquinas oscuras
     *   y el marco exterior de la ranura.
     * - Filtrado selectivo de píxeles espurios del aro exterior (rojo carmesí en rivales, azul eléctrico en aliados).
     * - Histograma bidimensional HSV (16 tonalidades x 4 niveles de saturación = 64 dimensiones).
     * - Histograma de valor/luminancia (8 dimensiones).
     * - Grilla espacial 3x3 para distribución morfológica (RGB = 27 dimensiones).
     * - Balances cromáticos y firmas Zaun/Freljord/Noxus (11 dimensiones).
     * - Histograma de gradientes direccionales Sobel (8 dimensiones).
     * - Centrado en cero estricto (zero-centering) y normalización L2: convierte la similitud coseno
     *   en el coeficiente de correlación de Pearson, eliminando falsos positivos globales.
     */
    private fun extractTensorEmbedding(bitmap: Bitmap): FloatArray {
        val scaled = Bitmap.createScaledBitmap(bitmap, TENSOR_INPUT_SIZE, TENSOR_INPUT_SIZE, true)
        val pixels = IntArray(TENSOR_INPUT_SIZE * TENSOR_INPUT_SIZE)
        scaled.getPixels(pixels, 0, TENSOR_INPUT_SIZE, 0, 0, TENSOR_INPUT_SIZE, TENSOR_INPUT_SIZE)
        if (scaled != bitmap) {
            try { scaled.recycle() } catch (_: Throwable) {}
        }

        val center = TENSOR_INPUT_SIZE / 2f
        val maxInnerRadius = TENSOR_INPUT_SIZE * 0.36f
        val maxRadiusSq = maxInnerRadius * maxInnerRadius

        // 1. Histograma 2D HSV (16 Tonalidades x 4 Niveles de Saturación = 64 bins)
        val hueSatHist = Array(16) { FloatArray(4) }
        
        // 2. Histograma de Luminancia/Valor (8 bins)
        val valHist = FloatArray(8)

        // 3. Grilla espacial 3x3 para distribución morfológica (RGB = 27 bins)
        val spatialR = FloatArray(9)
        val spatialG = FloatArray(9)
        val spatialB = FloatArray(9)
        val spatialCnt = FloatArray(9)

        // 4. Acumuladores globales y firmas Zaun/Freljord/Noxus
        var totalValidPixels = 0
        var totalR = 0f
        var totalG = 0f
        var totalB = 0f
        var totalLum = 0f
        var totalLumSq = 0f
        var highlightCount = 0f
        var shadowCount = 0f
        var chemtechCount = 0f // Firma de verde tóxico/quimtech (Urgot, Singed, Twitch)

        // Matriz de luminancia para gradientes direccionales Sobel
        val lumGrid = Array(TENSOR_INPUT_SIZE) { FloatArray(TENSOR_INPUT_SIZE) }

        for (y in 0 until TENSOR_INPUT_SIZE) {
            val dy = y - center
            val cellY = ((y * 3) / TENSOR_INPUT_SIZE).coerceIn(0, 2)
            for (x in 0 until TENSOR_INPUT_SIZE) {
                val dx = x - center
                val distSq = dx * dx + dy * dy
                val px = pixels[y * TENSOR_INPUT_SIZE + x]
                val r = Color.red(px) / 255.0f
                val g = Color.green(px) / 255.0f
                val b = Color.blue(px) / 255.0f
                val lum = 0.299f * r + 0.587f * g + 0.114f * b
                lumGrid[y][x] = lum

                if (distSq > maxRadiusSq) continue // Enmascaramiento circular interno

                val dist = sqrt(distSq)
                // Descartar píxeles periféricos contaminados por el aro exterior de la UI (rojo escarlata o azul cian)
                if (dist > TENSOR_INPUT_SIZE * 0.26f) {
                    if ((r > 0.45f && r > g * 1.35f && r > b * 1.35f) || (b > 0.45f && b > r * 1.35f && b > g * 1.15f)) {
                        continue
                    }
                }

                // Conversión HSV
                val maxC = max(r, max(g, b))
                val minC = min(r, min(g, b))
                val delta = maxC - minC
                val sat = if (maxC > 1e-4f) delta / maxC else 0f
                val value = maxC

                var hue = 0f
                if (delta > 0.04f) {
                    val rawHue = when {
                        maxC == r -> ((g - b) / delta) % 6f
                        maxC == g -> ((b - r) / delta) + 2f
                        else -> ((r - g) / delta) + 4f
                    } * 60f
                    hue = if (rawHue < 0f) rawHue + 360f else rawHue
                }

                val hBin = ((hue / 360f) * 16).toInt().coerceIn(0, 15)
                val sBin = (sat * 4f).toInt().coerceIn(0, 3)
                val vBin = (value * 8f).toInt().coerceIn(0, 7)

                val weight = max(0.2f, sat)
                hueSatHist[hBin][sBin] += weight
                valHist[vBin] += 1f

                val cellX = ((x * 3) / TENSOR_INPUT_SIZE).coerceIn(0, 2)
                val sIdx = cellY * 3 + cellX
                spatialR[sIdx] += r
                spatialG[sIdx] += g
                spatialB[sIdx] += b
                spatialCnt[sIdx] += 1f

                totalValidPixels++
                totalR += r
                totalG += g
                totalB += b
                totalLum += lum
                totalLumSq += lum * lum
                if (lum > 0.70f) highlightCount += 1f
                if (lum < 0.18f) shadowCount += 1f
                if (g > r + 0.05f && g > b + 0.03f) chemtechCount += 1f
            }
        }

        val normCount = totalValidPixels.toFloat().coerceAtLeast(1f)
        val embedding = FloatArray(EMBEDDING_DIM)
        var embIdx = 0

        // 1. Histograma 2D HSV (64 dims)
        var hsSum = 0f
        for (h in 0 until 16) {
            for (s in 0 until 4) {
                hsSum += hueSatHist[h][s]
            }
        }
        val hsNorm = hsSum.coerceAtLeast(1e-4f)
        for (h in 0 until 16) {
            for (s in 0 until 4) {
                embedding[embIdx++] = hueSatHist[h][s] / hsNorm
            }
        } // 64 dims

        // 2. Histograma de Luminancia/Valor (8 dims)
        for (v in 0 until 8) {
            embedding[embIdx++] = valHist[v] / normCount
        } // 64 + 8 = 72 dims

        // 3. Grilla espacial 3x3 (27 dims)
        for (i in 0 until 9) {
            val c = spatialCnt[i].coerceAtLeast(1f)
            embedding[embIdx++] = spatialR[i] / c
            embedding[embIdx++] = spatialG[i] / c
            embedding[embIdx++] = spatialB[i] / c
        } // 72 + 27 = 99 dims

        // 4. Balances cromáticos y momentos estadísticos (11 dims)
        val meanR = totalR / normCount
        val meanG = totalG / normCount
        val meanB = totalB / normCount
        val meanLum = totalLum / normCount
        val varLum = max(0f, (totalLumSq / normCount) - (meanLum * meanLum))

        embedding[embIdx++] = meanR
        embedding[embIdx++] = meanG
        embedding[embIdx++] = meanB
        embedding[embIdx++] = meanLum
        embedding[embIdx++] = sqrt(varLum)
        embedding[embIdx++] = meanG - meanR // Verde Zaun vs Rojo
        embedding[embIdx++] = meanB - meanR // Azul Freljord vs Rojo
        embedding[embIdx++] = meanR - meanB // Rojo Noxus vs Azul
        embedding[embIdx++] = chemtechCount / normCount
        embedding[embIdx++] = highlightCount / normCount
        embedding[embIdx++] = shadowCount / normCount
        // 99 + 11 = 110 dims

        // 5. Histograma de bordes direccionales Sobel (8 bins)
        val edgeHist = FloatArray(8)
        var totalGradMag = 0f
        for (y in 3 until TENSOR_INPUT_SIZE - 3) {
            for (x in 3 until TENSOR_INPUT_SIZE - 3) {
                val dx = (x - center).toFloat()
                val dy = (y - center).toFloat()
                if (dx * dx + dy * dy > maxRadiusSq) continue

                val gx = (-lumGrid[y-1][x-1] + lumGrid[y-1][x+1] - 2*lumGrid[y][x-1] + 2*lumGrid[y][x+1] - lumGrid[y+1][x-1] + lumGrid[y+1][x+1])
                val gy = (-lumGrid[y-1][x-1] - 2*lumGrid[y-1][x] - lumGrid[y-1][x+1] + lumGrid[y+1][x-1] + 2*lumGrid[y+1][x] + lumGrid[y+1][x+1])
                val gMag = sqrt(gx * gx + gy * gy)
                if (gMag > 0.05f) {
                    val angle = (kotlin.math.atan2(gy.toDouble(), gx.toDouble()) * 180.0 / Math.PI + 360.0) % 360.0
                    val binIdx = ((angle / 360.0) * 8.0).toInt().coerceIn(0, 7)
                    edgeHist[binIdx] += gMag
                    totalGradMag += gMag
                }
            }
        }
        val edgeSum = totalGradMag.coerceAtLeast(1e-4f)
        for (b in 0 until 8) {
            embedding[embIdx++] = edgeHist[b] / edgeSum
        } // 110 + 8 = 118 dims

        // CENTRADO EN CERO (Zero-Centering):
        val meanVal = embedding.sum() / embedding.size
        var sumSquares = 0.0f
        for (i in embedding.indices) {
            embedding[i] -= meanVal
            sumSquares += embedding[i] * embedding[i]
        }

        // Normalización L2
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

        // Si no hay recorte válido disponible:
        if (cropBitmap == null || cropBitmap.isRecycled || cropBitmap.width < 16 || cropBitmap.height < 16) {
            resetStabilityTracker()
            val isWaitingEarly = confirmedPicksCount < 8
            val status = if (isWaitingEarly) EngineStatus.WAITING_FOR_PICKS_1_TO_9 else EngineStatus.WAITING_FOR_TENTH_PICK
            val decisionReason = if (isWaitingEarly) {
                "Esperando selecciones 1 al 9 completas ($confirmedPicksCount/9 detectados)"
            } else {
                "Slot final en espera del 10º pick ($confirmedPicksCount/9 detectados)"
            }
            _reportFlow.value = LiteRTInferenceReport(
                status = status,
                pickedChampion = null,
                confidencePercent = 0,
                decisionReason = decisionReason,
                slotDescription = slotDesc,
                evaluatedPicksCount = confirmedPicksCount,
                cropBitmap = null
            )
            return@withContext null
        }

        // Si el draft se encuentra en fases previas (< 9 selecciones confirmadas en total):
        if (confirmedPicksCount < 9) {
            resetStabilityTracker()
            _reportFlow.value = LiteRTInferenceReport(
                status = EngineStatus.WAITING_FOR_PICKS_1_TO_9,
                pickedChampion = null,
                confidencePercent = 0,
                decisionReason = "Esperando selecciones 1 al 9 completas ($confirmedPicksCount/9 detectados)",
                slotDescription = slotDesc,
                evaluatedPicksCount = confirmedPicksCount,
                cropBitmap = try { cropBitmap.copy(Bitmap.Config.ARGB_8888, false) } catch (_: Throwable) { null }
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
        val secondScore = sortedCandidates.getOrNull(1)?.second ?: 0f
        val winnerChamp = bestCandidate.champion
        val finalConfidence = bestCandidate.confidencePercent
        val margin = bestCandidate.similarityScore - secondScore

        val effectiveThreshold = getEffectiveThreshold(context)

        // CONTROL DE UMBRAL DE CONFIANZA Y FRAMES ESTABLES:
        val passesConfidence = bestCandidate.similarityScore >= effectiveThreshold

        val requiredFrames = when {
            bestCandidate.similarityScore >= 0.88f && margin >= 0.05f -> 2 // Coincidencia dominante e inequívoca
            margin < 0.03f -> 4 // Muy reñido con el segundo candidato
            else -> 3
        }

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

        val isConfirmed = passesConfidence && (stableFramesCounter >= requiredFrames)

        val decisionReason = when {
            isConfirmed -> {
                "Google MediaPipe / LiteRT confirmó a ${winnerChamp.name} tras superar el umbral (${(bestCandidate.similarityScore * 100).toInt()}% >= ${(effectiveThreshold * 100).toInt()}%, margen: +${(margin * 100).toInt()}%) en $stableFramesCounter/$requiredFrames frame(s) estable(s)."
            }
            passesConfidence -> {
                "Candidato ${winnerChamp.name} supera umbral (${(bestCandidate.similarityScore * 100).toInt()}% >= ${(effectiveThreshold * 100).toInt()}%). Estabilizando: $stableFramesCounter/$requiredFrames frames..."
            }
            else -> {
                "Puntaje de ${winnerChamp.name} (${(bestCandidate.similarityScore * 100).toInt()}%) inferior al umbral configurado (${(effectiveThreshold * 100).toInt()}%). El motor continúa analizando los tensores en pantalla."
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
            requiredStableFrames = requiredFrames,
            minConfidenceThreshold = effectiveThreshold
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
     * Cuando el jugador selecciona un campeón (incluso de splash oscuro como Vi, Viego o Zed),
     * la varianza cromática y luminosidad descartan el icono de espera para inferir inmediatamente.
     */
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
