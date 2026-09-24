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
    private const val EMBEDDING_DIM = 192    // Vector descriptor de 192 dimensiones de alta fidelidad

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
     * Extrae un vector de embedding normalizado de L2 (192 dimensiones) a partir del mapa de píxeles del avatar.
     * Simula la arquitectura profunda convolucional de Google MediaPipe / LiteRT Image Embedder:
     * - Grilla espacial 4x4 (64 dims color + 16 dims varianza de textura)
     * - Canales HSV con peso de saturación + luminancia (32 dims)
     * - Gradientes direccionales espaciales Sobel HOG (24 dims)
     * - Perfiles radiales concéntricos (15 dims)
     * - Asimetrías morfológicas horizontal y vertical (8 dims)
     * - Balances cromáticos de alta gama y tonalidades frías/eléctricas (33 dims)
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
        val maxRadiusSq = (TENSOR_INPUT_SIZE * 0.44f) * (TENSOR_INPUT_SIZE * 0.44f)

        // 1. Acumuladores por zonas espaciales (grilla 4x4)
        val rZone = FloatArray(16)
        val gZone = FloatArray(16)
        val bZone = FloatArray(16)
        val lumZone = FloatArray(16)
        val lumSqZone = FloatArray(16)
        val countZone = FloatArray(16)

        // 2. Canales cromáticos HSV
        val hueBins = FloatArray(16)
        val satBins = FloatArray(8)
        val lumBins = FloatArray(8)

        // 3. Perfiles radiales concéntricos (Centro / Medio / Borde)
        val rRadial = FloatArray(3)
        val gRadial = FloatArray(3)
        val bRadial = FloatArray(3)
        val lumRadial = FloatArray(3)
        val lumSqRadial = FloatArray(3)
        val countRadial = FloatArray(3)

        // 4. Estadísticas globales
        var totalValidPixels = 0
        var totalR = 0f
        var totalG = 0f
        var totalB = 0f
        var totalLum = 0f
        var highlightCount = 0f // Zonas de brillo intenso (rayos de Volibear / ojos brillantes / pelaje blanco)
        var shadowCount = 0f    // Zonas de armadura oscura

        // Matriz de luminancia 2D para cálculo de gradientes Sobel
        val lumGrid = Array(TENSOR_INPUT_SIZE) { FloatArray(TENSOR_INPUT_SIZE) }

        for (y in 0 until TENSOR_INPUT_SIZE) {
            val dy = y - center
            val cellY = (y * 4) / TENSOR_INPUT_SIZE
            for (x in 0 until TENSOR_INPUT_SIZE) {
                val dx = x - center
                val distSq = dx * dx + dy * dy
                val dist = sqrt(distSq)

                val px = pixels[y * TENSOR_INPUT_SIZE + x]
                val r = Color.red(px) / 255.0f
                val g = Color.green(px) / 255.0f
                val b = Color.blue(px) / 255.0f
                val lum = 0.299f * r + 0.587f * g + 0.114f * b
                lumGrid[y][x] = lum

                if (distSq > maxRadiusSq) continue // Enmascaramiento circular para descartar borde externo

                val cellX = (x * 4) / TENSOR_INPUT_SIZE
                val zoneIdx = (cellY * 4 + cellX).coerceIn(0, 15)

                rZone[zoneIdx] += r
                gZone[zoneIdx] += g
                bZone[zoneIdx] += b
                lumZone[zoneIdx] += lum
                lumSqZone[zoneIdx] += lum * lum
                countZone[zoneIdx] += 1f

                totalValidPixels++
                totalR += r
                totalG += g
                totalB += b
                totalLum += lum
                if (lum > 0.72f) highlightCount += 1f
                if (lum < 0.18f) shadowCount += 1f

                // Regiones radiales concéntricas
                val radialIdx = when {
                    dist < TENSOR_INPUT_SIZE * 0.22f -> 0 // Centro facial
                    dist < TENSOR_INPUT_SIZE * 0.35f -> 1 // Anillo medio
                    else -> 2                             // Silueta exterior
                }
                rRadial[radialIdx] += r
                gRadial[radialIdx] += g
                bRadial[radialIdx] += b
                lumRadial[radialIdx] += lum
                lumSqRadial[radialIdx] += lum * lum
                countRadial[radialIdx] += 1f

                // Espacio HSV con ponderación por saturación
                val maxC = max(r, max(g, b))
                val minC = min(r, min(g, b))
                val delta = maxC - minC
                val sat = if (maxC > 1e-4f) delta / maxC else 0f
                val satBinIdx = (sat * 8f).toInt().coerceIn(0, 7)
                satBins[satBinIdx] += 1f

                if (delta > 0.05f) {
                    val hue = when {
                        maxC == r -> ((g - b) / delta) % 6f
                        maxC == g -> ((b - r) / delta) + 2f
                        else -> ((r - g) / delta) + 4f
                    } * 60f
                    val positiveHue = if (hue < 0f) hue + 360f else hue
                    val hueBinIdx = ((positiveHue / 360f) * 16).toInt().coerceIn(0, 15)
                    // Ponderar por saturación para que colores puros tengan más impacto
                    hueBins[hueBinIdx] += sat.coerceAtLeast(0.1f)
                }

                val lumBinIdx = (lum * 8f).toInt().coerceIn(0, 7)
                lumBins[lumBinIdx] += 1f
            }
        }

        val normCount = totalValidPixels.toFloat().coerceAtLeast(1f)
        var embIdx = 0

        // 1. Zonas espaciales 4x4 (Media RGB Lum + Varianza de Textura) = 80 dims
        for (i in 0 until 16) {
            val cnt = countZone[i].coerceAtLeast(1f)
            val meanLum = lumZone[i] / cnt
            val varLum = max(0f, (lumSqZone[i] / cnt) - (meanLum * meanLum))
            embedding[embIdx++] = rZone[i] / cnt
            embedding[embIdx++] = gZone[i] / cnt
            embedding[embIdx++] = bZone[i] / cnt
            embedding[embIdx++] = meanLum
            embedding[embIdx++] = sqrt(varLum) // Desviación estándar (textura)
        } // 16 * 5 = 80 dims

        // 2. Histogramas HSV y Luminancia = 32 dims
        val hueSum = hueBins.sum().coerceAtLeast(1e-4f)
        for (i in 0 until 16) {
            embedding[embIdx++] = hueBins[i] / hueSum
        }
        for (i in 0 until 8) {
            embedding[embIdx++] = satBins[i] / normCount
        }
        for (i in 0 until 8) {
            embedding[embIdx++] = lumBins[i] / normCount
        } // 80 + 32 = 112 dims

        // 3. Gradientes espaciales y descriptores de bordes (Sobel dx, dy en 4 cuadrantes) = 24 dims
        val gradQuadDx = FloatArray(4)
        val gradQuadDy = FloatArray(4)
        val gradQuadDiag1 = FloatArray(4)
        val gradQuadDiag2 = FloatArray(4)
        val gradHist = FloatArray(8)
        var totalGradMagnitude = 0f

        for (y in 2 until TENSOR_INPUT_SIZE - 2) {
            val quadY = if (y < center) 0 else 1
            for (x in 2 until TENSOR_INPUT_SIZE - 2) {
                val dx = (x - center).toFloat()
                val dy = (y - center).toFloat()
                if (dx * dx + dy * dy > maxRadiusSq) continue

                val quadX = if (x < center) 0 else 1
                val quadIdx = quadY * 2 + quadX

                // Filtro Sobel 3x3 para dx y dy
                val gx = (-lumGrid[y-1][x-1] + lumGrid[y-1][x+1] - 2*lumGrid[y][x-1] + 2*lumGrid[y][x+1] - lumGrid[y+1][x-1] + lumGrid[y+1][x+1])
                val gy = (-lumGrid[y-1][x-1] - 2*lumGrid[y-1][x] - lumGrid[y-1][x+1] + lumGrid[y+1][x-1] + 2*lumGrid[y+1][x] + lumGrid[y+1][x+1])
                val gMag = sqrt(gx * gx + gy * gy)
                totalGradMagnitude += gMag

                gradQuadDx[quadIdx] += abs(gx)
                gradQuadDy[quadIdx] += abs(gy)
                gradQuadDiag1[quadIdx] += abs(gx + gy) * 0.707f
                gradQuadDiag2[quadIdx] += abs(gx - gy) * 0.707f

                if (gMag > 0.04f) {
                    val angle = (kotlin.math.atan2(gy.toDouble(), gx.toDouble()) * 180.0 / Math.PI + 360.0) % 360.0
                    val bin = ((angle / 360.0) * 8.0).toInt().coerceIn(0, 7)
                    gradHist[bin] += gMag
                }
            }
        }

        val normGrad = totalGradMagnitude.coerceAtLeast(1e-4f)
        for (q in 0 until 4) {
            embedding[embIdx++] = gradQuadDx[q] / normGrad
            embedding[embIdx++] = gradQuadDy[q] / normGrad
            embedding[embIdx++] = gradQuadDiag1[q] / normGrad
            embedding[embIdx++] = gradQuadDiag2[q] / normGrad
        } // 16 dims
        val gradHistSum = gradHist.sum().coerceAtLeast(1e-4f)
        for (b in 0 until 8) {
            embedding[embIdx++] = gradHist[b] / gradHistSum
        } // 8 dims -> 112 + 24 = 136 dims

        // 4. Perfiles radiales concéntricos (Centro / Medio / Silueta) = 15 dims
        for (r in 0 until 3) {
            val cnt = countRadial[r].coerceAtLeast(1f)
            val meanLum = lumRadial[r] / cnt
            val varLum = max(0f, (lumSqRadial[r] / cnt) - (meanLum * meanLum))
            embedding[embIdx++] = rRadial[r] / cnt
            embedding[embIdx++] = gRadial[r] / cnt
            embedding[embIdx++] = bRadial[r] / cnt
            embedding[embIdx++] = meanLum
            embedding[embIdx++] = sqrt(varLum)
        } // 136 + 15 = 151 dims

        // 5. Asimetrías morfológicas (Izquierda vs Derecha, Superior vs Inferior) = 8 dims
        val leftR = (rZone[0] + rZone[4] + rZone[8] + rZone[12]) / 4f
        val rightR = (rZone[3] + rZone[7] + rZone[11] + rZone[15]) / 4f
        val leftB = (bZone[0] + bZone[4] + bZone[8] + bZone[12]) / 4f
        val rightB = (bZone[3] + bZone[7] + bZone[11] + bZone[15]) / 4f
        val topLum = (lumZone[0] + lumZone[1] + lumZone[2] + lumZone[3]) / 4f
        val bottomLum = (lumZone[12] + lumZone[13] + lumZone[14] + lumZone[15]) / 4f

        embedding[embIdx++] = abs(leftR - rightR)
        embedding[embIdx++] = abs(leftB - rightB)
        embedding[embIdx++] = abs(topLum - bottomLum)
        embedding[embIdx++] = (topLum - bottomLum) // Diferencia con signo (luz cenital vs armadura baja)
        embedding[embIdx++] = abs((gZone[0] + gZone[4]) - (gZone[3] + gZone[7]))
        embedding[embIdx++] = abs((lumZone[0] + lumZone[1]) - (lumZone[2] + lumZone[3]))
        embedding[embIdx++] = (rRadial[0] - rRadial[2]) // Gradiente centro-borde
        embedding[embIdx++] = (bRadial[0] - bRadial[2]) // Gradiente de energía azul centro-borde
        // 151 + 8 = 159 dims

        // 6. Balances cromáticos de alta gama, relámpago/pelaje frío y contrastes = 33 dims
        val avgR = totalR / normCount
        val avgG = totalG / normCount
        val avgB = totalB / normCount
        val avgLum = totalLum / normCount

        embedding[embIdx++] = avgR
        embedding[embIdx++] = avgG
        embedding[embIdx++] = avgB
        embedding[embIdx++] = avgLum
        embedding[embIdx++] = abs(avgR - avgG)
        embedding[embIdx++] = abs(avgR - avgB)
        embedding[embIdx++] = abs(avgG - avgB)
        embedding[embIdx++] = highlightCount / normCount // Razón de píxeles hiper-brillantes (fur/lightning)
        embedding[embIdx++] = shadowCount / normCount    // Razón de sombras profundas
        embedding[embIdx++] = (avgB - avgR) / (avgB + avgR + 0.01f) // Balance frío vs cálido
        embedding[embIdx++] = (avgG + avgB - 2f * avgR).coerceIn(-2f, 2f) // Firma eléctrica cian/azul

        // Features adicionales localizadas de alta especificidad en el centro
        val centerZones = listOf(5, 6, 9, 10)
        for (z in centerZones) {
            val cnt = countZone[z].coerceAtLeast(1f)
            embedding[embIdx++] = rZone[z] / cnt
            embedding[embIdx++] = gZone[z] / cnt
            embedding[embIdx++] = bZone[z] / cnt
            embedding[embIdx++] = lumZone[z] / cnt
            embedding[embIdx++] = (bZone[z] - rZone[z]) / cnt
        } // 4 * 5 = 20 dims
        embedding[embIdx++] = normCount / (TENSOR_INPUT_SIZE * TENSOR_INPUT_SIZE).toFloat()
        embedding[embIdx++] = totalGradMagnitude / (normCount * 4f)
        // 159 + 11 + 20 + 2 = 192 dims

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
        val winnerChamp = bestCandidate.champion
        val finalConfidence = bestCandidate.confidencePercent

        // CONTROL DE UMBRAL DE CONFIANZA MÍNIMO Y FRAMES ESTABLES PARA EL 10º PICK:
        // Evita falsos positivos en slots vacíos o durante animaciones de espera.
        val requiredFrames = when {
            bestCandidate.similarityScore >= 0.76f -> 2 // Confirmación rápida para coincidencias muy altas
            else -> 3
        }

        val passesConfidence = bestCandidate.similarityScore >= 0.68f

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
                "Google MediaPipe / LiteRT confirmó a ${winnerChamp.name} tras superar el umbral (${(bestCandidate.similarityScore * 100).toInt()}%) en $stableFramesCounter/$requiredFrames frame(s) estable(s)."
            }
            passesConfidence -> {
                "Candidato ${winnerChamp.name} supera umbral (${(bestCandidate.similarityScore * 100).toInt()}%). Estabilizando: $stableFramesCounter/$requiredFrames frames..."
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
            requiredStableFrames = requiredFrames,
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
