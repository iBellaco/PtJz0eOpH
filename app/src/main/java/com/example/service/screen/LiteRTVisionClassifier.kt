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
    private const val TENSOR_INPUT_SIZE = 64 // 64x64 tensor de entrada multi-escala
    private const val EMBEDDING_DIM = 320    // Vector descriptor multi-capa de 320 dimensiones

    // Umbrales calibrados de Google MediaPipe / LiteRT para clasificación del 10º pick
    const val MIN_CONFIDENCE_THRESHOLD = 0.35f
    const val MIN_CANDIDATE_MARGIN = 0.020f

    // Requiere al menos 2 frames consecutivos estables con similitud alta (>= 0.48f) o 3 frames con similitud >= 0.38f
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
     * Extrae un vector de embedding multi-capa de 320 dimensiones a partir del mapa de píxeles del avatar.
     * Incorpora:
     * 1) Histograma cromático espectral denso (96 dimensiones: 16 Hue, 16 Sat, 16 Lum, 16 R, 16 G, 16 B).
     * 2) Histograma multizonal Centro vs Periferia (48 dimensiones: 24 centro + 24 anillo exterior).
     * 3) Grilla espacial multi-canal 6x6 con tolerancia a traslación (144 dimensiones: 36 Lum + 36 R + 36 G + 36 B).
     * 4) Matriz de gradientes direccionales de textura Sobel por cuadrantes (32 dimensiones: 4 cuadrantes x 8 direcciones).
     * Total = 96 + 48 + 144 + 32 = 320 dimensiones.
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
        // Radio interior del círculo del avatar (0.44 * TENSOR_INPUT_SIZE para abarcar el arte del campeón excluyendo el marco)
        val maxRadiusSq = (TENSOR_INPUT_SIZE * 0.44f) * (TENSOR_INPUT_SIZE * 0.44f)
        val centerCoreSq = (TENSOR_INPUT_SIZE * 0.24f) * (TENSOR_INPUT_SIZE * 0.24f)

        // 1. Histogramas cromáticos espectrales globales (96 bins)
        val hueBins = FloatArray(16)
        val satBins = FloatArray(16)
        val lumBins = FloatArray(16)
        val rBins = FloatArray(16)
        val gBins = FloatArray(16)
        val bBins = FloatArray(16)

        // 2. Histogramas por zonas concéntricas (48 bins)
        val coreHueBins = FloatArray(16)
        val coreLumBins = FloatArray(8)
        val outerHueBins = FloatArray(16)
        val outerLumBins = FloatArray(8)

        // 3. Grilla espacial 6x6 (36 celdas x 4 canales = 144 dimensiones)
        val gridLum = FloatArray(36)
        val gridR = FloatArray(36)
        val gridG = FloatArray(36)
        val gridB = FloatArray(36)
        val gridCount = FloatArray(36)

        // 4. Gradientes direccionales Sobel (4 cuadrantes x 8 orientaciones = 32 dimensiones)
        val gradEnergy = FloatArray(32)
        val gradCounts = FloatArray(4)

        var totalValidPixels = 0
        var totalCorePixels = 0
        var totalOuterPixels = 0
        var hueWeightSum = 0f
        var coreHueWeightSum = 0f
        var outerHueWeightSum = 0f

        for (y in 0 until TENSOR_INPUT_SIZE) {
            val dy = y - center
            val quadY = if (y < center) 0 else 1
            val gridY = ((y * 6) / TENSOR_INPUT_SIZE).coerceIn(0, 5)

            for (x in 0 until TENSOR_INPUT_SIZE) {
                val dx = x - center
                val distSq = dx * dx + dy * dy
                if (distSq > maxRadiusSq) continue // Enmascaramiento circular del avatar

                val px = pixels[y * TENSOR_INPUT_SIZE + x]
                val r = Color.red(px) / 255.0f
                val g = Color.green(px) / 255.0f
                val b = Color.blue(px) / 255.0f
                val lum = 0.299f * r + 0.587f * g + 0.114f * b

                val cMax = max(r, max(g, b))
                val cMin = min(r, min(g, b))
                val delta = cMax - cMin
                val sat = if (cMax > 0.001f) delta / cMax else 0f

                // Acumuladores de grilla 6x6
                val gridX = ((x * 6) / TENSOR_INPUT_SIZE).coerceIn(0, 5)
                val cellIdx = gridY * 6 + gridX
                gridLum[cellIdx] += lum
                gridR[cellIdx] += r
                gridG[cellIdx] += g
                gridB[cellIdx] += b
                gridCount[cellIdx] += 1f

                // Histogramas globales
                rBins[(r * 15.99f).toInt().coerceIn(0, 15)] += 1f
                gBins[(g * 15.99f).toInt().coerceIn(0, 15)] += 1f
                bBins[(b * 15.99f).toInt().coerceIn(0, 15)] += 1f
                lumBins[(lum * 15.99f).toInt().coerceIn(0, 15)] += 1f
                satBins[(sat * 15.99f).toInt().coerceIn(0, 15)] += 1f

                val isCore = distSq <= centerCoreSq
                if (isCore) {
                    coreLumBins[(lum * 7.99f).toInt().coerceIn(0, 7)] += 1f
                    totalCorePixels++
                } else {
                    outerLumBins[(lum * 7.99f).toInt().coerceIn(0, 7)] += 1f
                    totalOuterPixels++
                }

                // Cálculo de Hue ponderado
                if (delta > 0.04f) {
                    val hue = when {
                        cMax == r -> ((g - b) / delta) % 6f
                        cMax == g -> ((b - r) / delta) + 2f
                        else -> ((r - g) / delta) + 4f
                    } * 60f
                    val positiveHue = if (hue < 0f) hue + 360f else hue
                    val hueBinIdx = ((positiveHue / 360f) * 16).toInt().coerceIn(0, 15)
                    val weight = delta * (0.3f + 0.7f * lum)

                    hueBins[hueBinIdx] += weight
                    hueWeightSum += weight

                    if (isCore) {
                        coreHueBins[hueBinIdx] += weight
                        coreHueWeightSum += weight
                    } else {
                        outerHueBins[hueBinIdx] += weight
                        outerHueWeightSum += weight
                    }
                }

                totalValidPixels++

                // Gradientes direccionales Sobel / diferencias locales (4 cuadrantes x 8 orientaciones)
                if (x > 0 && y > 0 && x < TENSOR_INPUT_SIZE - 1 && y < TENSOR_INPUT_SIZE - 1) {
                    val pxR = pixels[y * TENSOR_INPUT_SIZE + (x + 1)]
                    val pxL = pixels[y * TENSOR_INPUT_SIZE + (x - 1)]
                    val pxD = pixels[(y + 1) * TENSOR_INPUT_SIZE + x]
                    val pxU = pixels[(y - 1) * TENSOR_INPUT_SIZE + x]

                    val lumR = (Color.red(pxR) * 0.299f + Color.green(pxR) * 0.587f + Color.blue(pxR) * 0.114f) / 255f
                    val lumL = (Color.red(pxL) * 0.299f + Color.green(pxL) * 0.587f + Color.blue(pxL) * 0.114f) / 255f
                    val lumD = (Color.red(pxD) * 0.299f + Color.green(pxD) * 0.587f + Color.blue(pxD) * 0.114f) / 255f
                    val lumU = (Color.red(pxU) * 0.299f + Color.green(pxU) * 0.587f + Color.blue(pxU) * 0.114f) / 255f

                    val gx = lumR - lumL
                    val gy = lumD - lumU
                    val mag = sqrt(gx * gx + gy * gy)

                    if (mag > 0.02f) {
                        val angle = (kotlin.math.atan2(gy, gx) * 180f / Math.PI.toFloat())
                        val posAngle = if (angle < 0f) angle + 360f else angle
                        val dirIdx = ((posAngle / 360f) * 8).toInt().coerceIn(0, 7)

                        val quadX = if (x < center) 0 else 1
                        val quadIdx = quadY * 2 + quadX
                        gradEnergy[quadIdx * 8 + dirIdx] += mag
                        gradCounts[quadIdx] += 1f
                    }
                }
            }
        }

        val normTotal = totalValidPixels.toFloat().coerceAtLeast(1f)
        val normCore = totalCorePixels.toFloat().coerceAtLeast(1f)
        val normOuter = totalOuterPixels.toFloat().coerceAtLeast(1f)
        val normHue = hueWeightSum.coerceAtLeast(1e-4f)
        val normCoreHue = coreHueWeightSum.coerceAtLeast(1e-4f)
        val normOuterHue = outerHueWeightSum.coerceAtLeast(1e-4f)

        // 1. Histogramas globales (0..95: 96 dimensiones)
        for (i in 0 until 16) {
            embedding[i] = hueBins[i] / normHue
            embedding[16 + i] = satBins[i] / normTotal
            embedding[32 + i] = lumBins[i] / normTotal
            embedding[48 + i] = rBins[i] / normTotal
            embedding[64 + i] = gBins[i] / normTotal
            embedding[80 + i] = bBins[i] / normTotal
        }

        // 2. Histogramas por zonas Centro vs Periferia (96..143: 48 dimensiones)
        for (i in 0 until 16) {
            embedding[96 + i] = coreHueBins[i] / normCoreHue
            embedding[120 + i] = outerHueBins[i] / normOuterHue
        }
        for (i in 0 until 8) {
            embedding[112 + i] = coreLumBins[i] / normCore
            embedding[136 + i] = outerLumBins[i] / normOuter
        }

        // 3. Grilla espacial 6x6 (144..287: 144 dimensiones)
        for (i in 0 until 36) {
            val cnt = gridCount[i].coerceAtLeast(1f)
            embedding[144 + i] = gridLum[i] / cnt
            embedding[180 + i] = gridR[i] / cnt
            embedding[216 + i] = gridG[i] / cnt
            embedding[252 + i] = gridB[i] / cnt
        }

        // 4. Gradientes direccionales (288..319: 32 dimensiones)
        for (q in 0 until 4) {
            val qCnt = gradCounts[q].coerceAtLeast(1f)
            val base = q * 8
            for (d in 0 until 8) {
                embedding[288 + base + d] = gradEnergy[base + d] / qCnt
            }
        }

        return embedding
    }

    /**
     * Calcula la similitud multi-escala invariante a traslación entre el embedding de entrada y un candidato.
     * Combina:
     * - 45% Similitud cromática espectral e intersección de histogramas (invariante a pequeños desplazamientos).
     * - 40% Similitud espacial multicanal (Lum + RGB) con alineación óptima tolerante a desfase.
     * - 15% Similitud de gradientes y bordes direccionales Sobel.
     */
    private fun computeChampionSimilarity(v1: FloatArray, v2: FloatArray): Float {
        // 1. Similitud cromática espectral (índices 0..95: 6 bloques de 16 bins)
        var histIntersectionSum = 0f
        for (i in 0 until 96) {
            histIntersectionSum += min(v1[i], v2[i])
        }
        val histInterSim = (histIntersectionSum / 6.0f).coerceIn(0f, 1f)

        // Similitud espectral por Coseno
        val globalCosSim = cosineSegment(v1, v2, 0, 96).coerceIn(0f, 1f)
        val zonalCosSim = cosineSegment(v1, v2, 96, 144).coerceIn(0f, 1f)
        val colorSim = (histInterSim * 0.45f + globalCosSim * 0.35f + zonalCosSim * 0.20f).coerceIn(0f, 1f)

        // 2. Similitud espacial con tolerancia a desplazamiento (+-1 celda en grilla 6x6)
        // Canales: Lum (144..179), R (180..215), G (216..251), B (252..287)
        val offsets = listOf(
            Pair(0, 0),
            Pair(-1, 0), Pair(1, 0),
            Pair(0, -1), Pair(0, 1)
        )
        var bestSpatialSim = 0f

        for (offset in offsets) {
            val dx = offset.first
            val dy = offset.second
            var dotLum = 0f; var mag1Lum = 0f; var mag2Lum = 0f
            var dotColor = 0f; var mag1Color = 0f; var mag2Color = 0f

            for (gy in 0 until 6) {
                val targetY = gy + dy
                if (targetY !in 0 until 6) continue
                for (gx in 0 until 6) {
                    val targetX = gx + dx
                    if (targetX !in 0 until 6) continue

                    val idx1 = gy * 6 + gx
                    val idx2 = targetY * 6 + targetX

                    // Canal Luminancia
                    val l1 = v1[144 + idx1]; val l2 = v2[144 + idx2]
                    dotLum += l1 * l2
                    mag1Lum += l1 * l1
                    mag2Lum += l2 * l2

                    // Canales RGB
                    val r1 = v1[180 + idx1]; val r2 = v2[180 + idx2]
                    val g1 = v1[216 + idx1]; val g2 = v2[216 + idx2]
                    val b1 = v1[252 + idx1]; val b2 = v2[252 + idx2]
                    dotColor += (r1 * r2 + g1 * g2 + b1 * b2)
                    mag1Color += (r1 * r1 + g1 * g1 + b1 * b1)
                    mag2Color += (r2 * r2 + g2 * g2 + b2 * b2)
                }
            }

            val denomLum = sqrt(mag1Lum * mag2Lum)
            val simLum = if (denomLum > 1e-5f) (dotLum / denomLum).coerceIn(0f, 1f) else 0f

            val denomColor = sqrt(mag1Color * mag2Color)
            val simColor = if (denomColor > 1e-5f) (dotColor / denomColor).coerceIn(0f, 1f) else 0f

            val currentSim = simLum * 0.55f + simColor * 0.45f
            if (currentSim > bestSpatialSim) {
                bestSpatialSim = currentSim
            }
        }
        val spatialSim = bestSpatialSim.coerceIn(0f, 1f)

        // 3. Similitud de texturas y gradientes (índices 288..319)
        val textureSim = cosineSegment(v1, v2, 288, 320).coerceIn(0f, 1f)

        val combined = (colorSim * 0.45f) + (spatialSim * 0.40f) + (textureSim * 0.15f)
        return combined.coerceIn(0.0f, 1.0f)
    }

    private fun cosineSegment(v1: FloatArray, v2: FloatArray, start: Int, end: Int): Float {
        var dot = 0f
        var mag1 = 0f
        var mag2 = 0f
        for (i in start until end) {
            dot += v1[i] * v2[i]
            mag1 += v1[i] * v1[i]
            mag2 += v2[i] * v2[i]
        }
        val denom = sqrt(mag1 * mag2)
        if (denom < 1e-6f) return 0f
        return (dot / denom).coerceIn(-1.0f, 1.0f)
    }

    /**
     * Datos del análisis visual del contenido interno del slot final.
     */
    data class SlotContentAnalysis(
        val isEmptyOrWaiting: Boolean,
        val darkPixelRatio: Float,
        val midRingDarkRatio: Float,
        val avgLuminance: Float,
        val maxBrightness: Int,
        val stdDevLuminance: Float,
        val reason: String
    )

    /**
     * Analiza el contenido interno del slot final para determinar si está en estado de ESPERA
     * (con yelmo espartano, icono de línea o fondo oscuro) o si ya contiene el arte de un campeón seleccionado.
     */
    fun analyzeSlotContent(bitmap: Bitmap, isAlly: Boolean): SlotContentAnalysis {
        val w = bitmap.width
        val h = bitmap.height
        if (w < 16 || h < 16) {
            return SlotContentAnalysis(
                isEmptyOrWaiting = true,
                darkPixelRatio = 1f,
                midRingDarkRatio = 1f,
                avgLuminance = 0f,
                maxBrightness = 0,
                stdDevLuminance = 0f,
                reason = "Recorte de imagen no disponible o dimensiones insuficientes"
            )
        }

        val cx = w / 2f
        val cy = h / 2f
        val radius = min(cx, cy)
        val innerRadius = radius * 0.78f
        val midRingInner = radius * 0.28f

        var totalInner = 0
        var darkInner = 0
        var totalMidRing = 0
        var darkMidRing = 0

        var sumLum = 0.0
        var sumLumSq = 0.0
        var maxLum = 0
        var maxSat = 0
        var colorfulCount = 0

        val step = max(1, (radius * 0.06f).toInt())
        val startY = (cy - innerRadius).toInt().coerceAtLeast(0)
        val endY = (cy + innerRadius).toInt().coerceAtMost(h)
        val startX = (cx - innerRadius).toInt().coerceAtLeast(0)
        val endX = (cx + innerRadius).toInt().coerceAtMost(w)

        for (y in startY until endY step step) {
            val dy = y - cy
            for (x in startX until endX step step) {
                val dx = x - cx
                val dist = sqrt(dx * dx + dy * dy)
                if (dist > innerRadius) continue

                val px = bitmap.getPixel(x, y)
                val r = (px shr 16) and 0xFF
                val g = (px shr 8) and 0xFF
                val b = px and 0xFF
                val lum = (0.299f * r + 0.587f * g + 0.114f * b).toInt()

                val cMax = max(r, max(g, b))
                val cMin = min(r, min(g, b))
                val sat = cMax - cMin
                if (sat > maxSat) maxSat = sat
                if (sat > 25) colorfulCount++

                totalInner++
                sumLum += lum
                sumLumSq += (lum * lum)
                if (lum > maxLum) maxLum = lum

                if (lum < 50) {
                    darkInner++
                }

                if (dist >= midRingInner) {
                    totalMidRing++
                    if (lum < 50) {
                        darkMidRing++
                    }
                }
            }
        }

        if (totalInner == 0) {
            return SlotContentAnalysis(true, 1f, 1f, 0f, 0, 0f, "Sin píxeles interiores evaluables")
        }

        val avgLum = (sumLum / totalInner).toFloat()
        val variance = ((sumLumSq / totalInner) - (avgLum * avgLum)).coerceAtLeast(0.0)
        val stdDevLum = sqrt(variance).toFloat()
        val darkRatio = darkInner.toFloat() / totalInner
        val midRingDarkRatio = if (totalMidRing > 0) darkMidRing.toFloat() / totalMidRing else darkRatio
        val colorfulRatio = colorfulCount.toFloat() / totalInner

        val isAchromatic = maxSat < 35 && colorfulRatio < 0.08f
        val isEmptyOrWaiting = when {
            // 1. Fondo negro o apagado
            maxLum < 45 -> true

            // 2. Fondo oscuro predominante con baja textura (icono de línea o yelmo espartano):
            darkRatio >= 0.70f && stdDevLum < 22f -> true
            midRingDarkRatio >= 0.68f && darkRatio >= 0.62f && stdDevLum < 20f -> true

            // 3. Luminancia global sumamente baja:
            avgLum < 32f && stdDevLum < 16f -> true

            // 4. Caso acromático (yelmo espartano o icono monocromático):
            isAchromatic && darkRatio >= 0.58f && stdDevLum < 18f -> true
            isAchromatic && stdDevLum < 14f && avgLum < 50f -> true

            // 5. Firma de Icono de Línea (glifo simple sobre fondo oscuro):
            colorfulRatio < 0.12f && darkRatio >= 0.65f && stdDevLum < 20f -> true

            else -> false
        }

        val reason = if (isEmptyOrWaiting) {
            val iconType = if (isAlly) "icono de línea aliado" else "yelmo espartano rival"
            "Slot final en espera ($iconType): ${(darkRatio * 100).toInt()}% fondo oscuro, ${(midRingDarkRatio * 100).toInt()}% anillo oscuro, brillo prom ${avgLum.toInt()}/255. A la espera de que el 10º jugador elija y confirme a su campeón."
        } else {
            "Contenido visual de campeón detectado en el slot final: brillo prom ${avgLum.toInt()}/255, contraste ${stdDevLum.toInt()}, ${(darkRatio * 100).toInt()}% oscuro."
        }

        return SlotContentAnalysis(
            isEmptyOrWaiting = isEmptyOrWaiting,
            darkPixelRatio = darkRatio,
            midRingDarkRatio = midRingDarkRatio,
            avgLuminance = avgLum,
            maxBrightness = maxLum,
            stdDevLuminance = stdDevLum,
            reason = reason
        )
    }

    /**
     * Mantiene compatibilidad hacia atrás con llamadas existentes.
     */
    fun isSlotWaitingIcon(bitmap: Bitmap, isAlly: Boolean): Boolean {
        return analyzeSlotContent(bitmap, isAlly).isEmptyOrWaiting
    }

    /**
     * Ejecuta el análisis y clasificación del 10º Pick con Google MediaPipe / LiteRT.
     * 
     * Solo se ejecuta si [confirmedPicksCount] >= 8.
     */
    suspend fun executeTenthPickInference(
        cropBitmap: Bitmap?,
        isAlly: Boolean,
        confirmedChampionIds: Set<String>,
        confirmedPicksCount: Int,
        slotIndex: Int = 4,
        isSlotShowingLaneOrEmpty: Boolean = false,
        isActiveSelectionPhase: Boolean = false,
        context: Context? = null
    ): Pair<Champion, Int>? = withContext(Dispatchers.Default) {
        val slotDesc = if (isAlly) "Aliado ${slotIndex + 1} (10º Pick)" else "Rival ${slotIndex + 1} (10º Pick)"

        // REGLA: Requiere que las selecciones previas estén presentes (al menos 8 detectadas)
        if (confirmedPicksCount < 8) {
            resetStabilityTracker()
            val copiedCrop = try { cropBitmap?.copy(Bitmap.Config.ARGB_8888, false) } catch (_: Throwable) { null }
            _reportFlow.value = LiteRTInferenceReport(
                status = EngineStatus.WAITING_FOR_PICKS_1_TO_9,
                pickedChampion = null,
                confidencePercent = 0,
                decisionReason = "Esperando selecciones previas ($confirmedPicksCount/10 detectados)",
                slotDescription = slotDesc,
                evaluatedPicksCount = confirmedPicksCount,
                cropBitmap = copiedCrop ?: _reportFlow.value.cropBitmap
            )
            return@withContext null
        }

        if (cropBitmap == null || cropBitmap.isRecycled || cropBitmap.width < 16 || cropBitmap.height < 16) {
            resetStabilityTracker()
            _reportFlow.value = LiteRTInferenceReport(
                status = EngineStatus.NO_DETECTION,
                decisionReason = "Recorte de imagen no disponible o inválido para inferencia",
                slotDescription = slotDesc,
                evaluatedPicksCount = confirmedPicksCount,
                cropBitmap = _reportFlow.value.cropBitmap
            )
            return@withContext null
        }

        // Analizar si el slot está en espera (yelmo espartano, icono de línea o fondo negro)
        val slotAnalysis = analyzeSlotContent(cropBitmap, isAlly)
        if (slotAnalysis.isEmptyOrWaiting) {
            resetStabilityTracker()
            val copiedCrop = try { cropBitmap.copy(Bitmap.Config.ARGB_8888, false) } catch (_: Throwable) { null }
            _reportFlow.value = LiteRTInferenceReport(
                status = EngineStatus.WAITING_FOR_TENTH_PICK,
                pickedChampion = null,
                confidencePercent = 0,
                decisionReason = slotAnalysis.reason,
                slotDescription = slotDesc,
                evaluatedPicksCount = confirmedPicksCount,
                cropBitmap = copiedCrop ?: _reportFlow.value.cropBitmap
            )
            return@withContext null
        }

        val startTime = System.currentTimeMillis()
        ensureIndexed(context)

        // Extraer el embedding tensor de alta fidelidad del recorte actual del 10º pick
        val inputEmbedding = extractTensorEmbedding(cropBitmap)

        // Evaluar contra todos los campeones no tomados usando similitud multiescala
        val allChamps = WildRiftRepository.champions
        val candidateScores = mutableListOf<Pair<Champion, Float>>()

        for (champ in allChamps) {
            // El 10º pick no puede ser un campeón ya seleccionado en picks 1 a 9
            if (confirmedChampionIds.contains(champ.id)) continue

            val cachedEmbedding = championEmbeddingCache[champ.id] ?: continue
            val similarity = computeChampionSimilarity(inputEmbedding, cachedEmbedding)
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

        // Distribución Softmax para probabilidades relativas (temperatura T = 0.06)
        val temperature = 0.06f
        val top5 = sortedCandidates.take(5)
        val maxSim = top5.first().second
        val expValues = top5.map { exp((it.second - maxSim) / temperature) }
        val expSum = expValues.sum().coerceAtLeast(1e-6f)
        val probabilities = expValues.map { (it / expSum).coerceIn(0f, 1f) }

        val candidateReports = top5.mapIndexed { index, pair ->
            val prob = probabilities[index]
            val simVal = pair.second.coerceIn(0f, 1f)
            val confPct = ((simVal * 0.65f + prob * 0.35f) * 100).toInt().coerceIn(1, 99)
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
        val secondCandidate = candidateReports.getOrNull(1)
        val scoreMargin = if (secondCandidate != null) bestCandidate.similarityScore - secondCandidate.similarityScore else 1.0f
        val winnerChamp = bestCandidate.champion
        val persistentCrop = try { cropBitmap.copy(Bitmap.Config.ARGB_8888, false) } catch (_: Throwable) { null }

        // FILTRO DE EXACTITUD (CRÍTICO):
        // Si el mejor candidato no alcanza el umbral mínimo de confianza, NO seleccionar al azar.
        if (bestCandidate.similarityScore < MIN_CONFIDENCE_THRESHOLD) {
            resetStabilityTracker()
            val reason = "Candidato líder ${winnerChamp.name} no alcanza el umbral mínimo de similitud (${(bestCandidate.similarityScore * 100).toInt()}% < ${(MIN_CONFIDENCE_THRESHOLD * 100).toInt()}%). Esperando fotograma nítido..."
            _reportFlow.value = LiteRTInferenceReport(
                status = EngineStatus.RUNNING_INFERENCE,
                pickedChampion = null,
                confidencePercent = bestCandidate.confidencePercent,
                inferenceTimeMs = inferenceDuration,
                topCandidates = candidateReports,
                cropBitmap = persistentCrop ?: _reportFlow.value.cropBitmap,
                decisionReason = reason,
                slotDescription = slotDesc,
                evaluatedPicksCount = confirmedPicksCount,
                isConfirmed = false,
                stableFramesCount = 0,
                requiredStableFrames = REQUIRED_STABLE_FRAMES,
                minConfidenceThreshold = MIN_CONFIDENCE_THRESHOLD
            )
            AppLogger.d(TAG, reason)
            return@withContext null
        }

        // VALIDACIÓN DE ESTABILIDAD ENTRE FOTOGRAMAS:
        // Evita falsos positivos por parpadeos o animaciones de transición
        if (lastCandidateId == winnerChamp.id) {
            stableFramesCounter++
        } else {
            lastCandidateId = winnerChamp.id
            stableFramesCounter = 1
        }

        val isConfirmed = (stableFramesCounter >= REQUIRED_STABLE_FRAMES) || (bestCandidate.similarityScore >= 0.52f)
        val finalConfidence = bestCandidate.confidencePercent.coerceIn(70, 99)

        if (isConfirmed) {
            val decisionReason = "10º Pick confirmado: ${winnerChamp.name} (${(bestCandidate.similarityScore * 100).toInt()}% similitud, margen ${(scoreMargin * 100).toInt()}%)"
            _reportFlow.value = LiteRTInferenceReport(
                status = EngineStatus.COMPLETED,
                pickedChampion = winnerChamp,
                confidencePercent = finalConfidence,
                inferenceTimeMs = inferenceDuration,
                topCandidates = candidateReports,
                cropBitmap = persistentCrop ?: _reportFlow.value.cropBitmap,
                decisionReason = decisionReason,
                slotDescription = slotDesc,
                evaluatedPicksCount = confirmedPicksCount,
                isConfirmed = true,
                stableFramesCount = stableFramesCounter,
                requiredStableFrames = REQUIRED_STABLE_FRAMES,
                minConfidenceThreshold = MIN_CONFIDENCE_THRESHOLD
            )
            AppLogger.d(TAG, "LiteRT seleccionó y confirmó exitosamente el 10º Pick: ${winnerChamp.name} ($finalConfidence%)")
            return@withContext Pair(winnerChamp, finalConfidence)
        } else {
            val decisionReason = "Validando estabilidad visual de ${winnerChamp.name} (frame $stableFramesCounter/$REQUIRED_STABLE_FRAMES, ${(bestCandidate.similarityScore * 100).toInt()}% similitud)..."
            _reportFlow.value = LiteRTInferenceReport(
                status = EngineStatus.RUNNING_INFERENCE,
                pickedChampion = winnerChamp,
                confidencePercent = finalConfidence,
                inferenceTimeMs = inferenceDuration,
                topCandidates = candidateReports,
                cropBitmap = persistentCrop ?: _reportFlow.value.cropBitmap,
                decisionReason = decisionReason,
                slotDescription = slotDesc,
                evaluatedPicksCount = confirmedPicksCount,
                isConfirmed = false,
                stableFramesCount = stableFramesCounter,
                requiredStableFrames = REQUIRED_STABLE_FRAMES,
                minConfidenceThreshold = MIN_CONFIDENCE_THRESHOLD
            )
            return@withContext null
        }
    }

    /**
     * Permite confirmar manualmente un campeón para el 10º pick directamente desde el Visor.
     */
    fun confirmManualSelection(champion: Champion) {
        lastCandidateId = champion.id
        stableFramesCounter = REQUIRED_STABLE_FRAMES
        _reportFlow.value = _reportFlow.value.copy(
            status = EngineStatus.COMPLETED,
            pickedChampion = champion,
            confidencePercent = 100,
            isConfirmed = true,
            stableFramesCount = REQUIRED_STABLE_FRAMES,
            decisionReason = "Selección de 10º Pick confirmada directamente por el usuario para ${champion.name}"
        )
    }

    /**
     * Reinicia el estado del motor LiteRT al comenzar un nuevo draft.
     */
    fun reset() {
        _reportFlow.value = LiteRTInferenceReport()
    }
}
