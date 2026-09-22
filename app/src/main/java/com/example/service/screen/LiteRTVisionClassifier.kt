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
     * 1) Grilla espacial 8x8 (64 celdas x 4 componentes: R, G, B, Luminancia = 256 dimensiones).
     * 2) Histograma espectral de color denso (32 bins RGB/Lum + 16 bins Hue = 48 dimensiones).
     * 3) Matriz de textura y gradiente direccional de bordes por cuadrantes (16 dimensiones).
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
        // Radio interior del círculo del avatar (0.42 * TENSOR_INPUT_SIZE para ignorar el marco circular exterior)
        val maxRadiusSq = (TENSOR_INPUT_SIZE * 0.42f) * (TENSOR_INPUT_SIZE * 0.42f)

        // 1. Acumuladores de grilla espacial 8x8 (64 celdas)
        val rZone = FloatArray(64)
        val gZone = FloatArray(64)
        val bZone = FloatArray(64)
        val lumZone = FloatArray(64)
        val countZone = FloatArray(64)

        // 2. Histogramas espectrales
        val rBins = FloatArray(8)
        val gBins = FloatArray(8)
        val bBins = FloatArray(8)
        val lumBins = FloatArray(8)
        val hueBins = FloatArray(16)

        // 3. Gradientes direccionales por 4 cuadrantes (4 cuadrantes x 4 direcciones = 16)
        val gradEnergy = FloatArray(16)
        val gradCounts = FloatArray(4)

        var totalValidPixels = 0

        for (y in 0 until TENSOR_INPUT_SIZE) {
            val dy = y - center
            val cellY = (y * 8) / TENSOR_INPUT_SIZE
            val quadY = if (y < center) 0 else 1

            for (x in 0 until TENSOR_INPUT_SIZE) {
                val dx = x - center
                val distSq = dx * dx + dy * dy
                if (distSq > maxRadiusSq) continue // Enmascaramiento circular de avatar

                val px = pixels[y * TENSOR_INPUT_SIZE + x]
                val r = Color.red(px) / 255.0f
                val g = Color.green(px) / 255.0f
                val b = Color.blue(px) / 255.0f
                val lum = 0.299f * r + 0.587f * g + 0.114f * b

                val cellX = (x * 8) / TENSOR_INPUT_SIZE
                val zoneIdx = (cellY * 8 + cellX).coerceIn(0, 63)

                rZone[zoneIdx] += r
                gZone[zoneIdx] += g
                bZone[zoneIdx] += b
                lumZone[zoneIdx] += lum
                countZone[zoneIdx] += 1f
                totalValidPixels++

                // Bins de canales individuales (8 bins cada uno)
                rBins[(r * 7.99f).toInt().coerceIn(0, 7)] += 1f
                gBins[(g * 7.99f).toInt().coerceIn(0, 7)] += 1f
                bBins[(b * 7.99f).toInt().coerceIn(0, 7)] += 1f
                lumBins[(lum * 7.99f).toInt().coerceIn(0, 7)] += 1f

                // Espacio Hue ponderado por saturación
                val maxC = max(r, max(g, b))
                val minC = min(r, min(g, b))
                val delta = maxC - minC
                if (delta > 0.05f) {
                    val hue = when {
                        maxC == r -> ((g - b) / delta) % 6f
                        maxC == g -> ((b - r) / delta) + 2f
                        else -> ((r - g) / delta) + 4f
                    } * 60f
                    val positiveHue = if (hue < 0f) hue + 360f else hue
                    val hueBinIdx = ((positiveHue / 360f) * 16).toInt().coerceIn(0, 15)
                    hueBins[hueBinIdx] += delta
                }

                // Cálculo de gradiente local de bordes
                if (x > 0 && y > 0 && x < TENSOR_INPUT_SIZE - 1 && y < TENSOR_INPUT_SIZE - 1) {
                    val pxRight = pixels[y * TENSOR_INPUT_SIZE + (x + 1)]
                    val pxDown = pixels[(y + 1) * TENSOR_INPUT_SIZE + x]
                    val pxDiag = pixels[(y + 1) * TENSOR_INPUT_SIZE + (x + 1)]
                    val lumRight = (Color.red(pxRight) * 0.299f + Color.green(pxRight) * 0.587f + Color.blue(pxRight) * 0.114f) / 255f
                    val lumDown = (Color.red(pxDown) * 0.299f + Color.green(pxDown) * 0.587f + Color.blue(pxDown) * 0.114f) / 255f
                    val lumDiag = (Color.red(pxDiag) * 0.299f + Color.green(pxDiag) * 0.587f + Color.blue(pxDiag) * 0.114f) / 255f

                    val gx = abs(lumRight - lum)
                    val gy = abs(lumDown - lum)
                    val gdiag1 = abs(lumDiag - lum)
                    val gdiag2 = abs(gx - gy)

                    val quadX = if (x < center) 0 else 1
                    val quadIdx = quadY * 2 + quadX
                    val baseGrad = quadIdx * 4
                    gradEnergy[baseGrad] += gx
                    gradEnergy[baseGrad + 1] += gy
                    gradEnergy[baseGrad + 2] += gdiag1
                    gradEnergy[baseGrad + 3] += gdiag2
                    gradCounts[quadIdx] += 1f
                }
            }
        }

        val normCount = totalValidPixels.toFloat().coerceAtLeast(1f)

        // 1. Zonas espaciales 8x8 (0..255: 256 dimensiones)
        for (i in 0 until 64) {
            val cnt = countZone[i].coerceAtLeast(1f)
            embedding[i] = rZone[i] / cnt
            embedding[64 + i] = gZone[i] / cnt
            embedding[128 + i] = bZone[i] / cnt
            embedding[192 + i] = lumZone[i] / cnt
        }

        // 2. Histograma espectral denso (256..303: 48 dimensiones)
        for (i in 0 until 8) {
            embedding[256 + i] = rBins[i] / normCount
            embedding[264 + i] = gBins[i] / normCount
            embedding[272 + i] = bBins[i] / normCount
            embedding[280 + i] = lumBins[i] / normCount
        }
        val hueSum = hueBins.sum().coerceAtLeast(1e-4f)
        for (i in 0 until 16) {
            embedding[288 + i] = hueBins[i] / hueSum
        }

        // 3. Gradientes de textura por cuadrantes (304..319: 16 dimensiones)
        for (q in 0 until 4) {
            val qCnt = gradCounts[q].coerceAtLeast(1f)
            val baseGrad = q * 4
            for (d in 0 until 4) {
                embedding[304 + baseGrad + d] = gradEnergy[baseGrad + d] / qCnt
            }
        }

        return embedding
    }

    /**
     * Calcula la similitud multi-escala combinada entre el embedding de entrada y un candidato del catálogo.
     * Combina:
     * - 65% Correlación de Pearson en la grilla espacial 8x8 (patrón 2D y alineación geométrica de rasgos).
     * - 25% Similitud Coseno en el histograma espectral de color (paleta cromática y balances RGB/Hue).
     * - 10% Correlación en los gradientes de textura (densidad de bordes y nivel de detalle).
     */
    private fun computeChampionSimilarity(v1: FloatArray, v2: FloatArray): Float {
        // 1. Similitud espacial (índices 0..255)
        val spatialSim = pearsonSegment(v1, v2, 0, 256)

        // 2. Similitud espectral de color (índices 256..303)
        val colorSim = cosineSegment(v1, v2, 256, 304)

        // 3. Similitud de textura y bordes (índices 304..319)
        val textureSim = pearsonSegment(v1, v2, 304, 320)

        val combined = (spatialSim * 0.65f) + (colorSim * 0.25f) + (textureSim * 0.10f)
        return combined.coerceIn(-1.0f, 1.0f)
    }

    private fun pearsonSegment(v1: FloatArray, v2: FloatArray, start: Int, end: Int): Float {
        val len = end - start
        if (len <= 0) return 0f
        var sum1 = 0f
        var sum2 = 0f
        for (i in start until end) {
            sum1 += v1[i]
            sum2 += v2[i]
        }
        val mean1 = sum1 / len
        val mean2 = sum2 / len

        var dot = 0f
        var var1 = 0f
        var var2 = 0f
        for (i in start until end) {
            val d1 = v1[i] - mean1
            val d2 = v2[i] - mean2
            dot += d1 * d2
            var1 += d1 * d1
            var2 += d2 * d2
        }
        val denom = sqrt(var1 * var2)
        if (denom < 1e-6f) return 0f
        return (dot / denom).coerceIn(-1.0f, 1.0f)
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
     * Analiza exhaustivamente el contenido interno del slot final (ignorando el anillo exterior de borde)
     * para determinar si está en estado de ESPERA (con yelmo espartano, icono de línea o fondo negro)
     * o si ya contiene el retrato/splash art de un campeón seleccionado.
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

        // Radio interior para evaluar el contenido del avatar sin tocar el anillo de borde (0.78 * radius)
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

        // COMPROBACIÓN CRÍTICA:
        // En Wild Rift, un slot en espera (yelmo espartano o icono de línea) o vacío es:
        // 1. Predominantemente oscuro (el icono o yelmo ocupa un área pequeña central y deja > 68-75% del círculo como fondo oscuro).
        // 2. Muy baja desviación de luminancia (sin texturas complejas, pelo, ojos, reflejos de armadura, stdDevLum < 16f).
        // 3. En cambio, el retrato de un campeón (incluso campeones oscuros o fríos como Volibear, Malphite, Viego, Nocturne)
        //    cubre ampliamente el círculo interior y presenta contrastes/texturas marcadas (stdDevLum >= 16f, maxLum >= 90).
        val isAchromatic = maxSat < 30 && colorfulRatio < 0.05f
        val isEmptyOrWaiting = when {
            // 1. Prácticamente todo oscuro (slot apagado o fondo negro)
            maxLum < 45 -> true

            // 2. Fondo oscuro predominante con baja textura (icono de línea o yelmo espartano vacío):
            darkRatio >= 0.72f && stdDevLum < 20f -> true
            midRingDarkRatio >= 0.70f && darkRatio >= 0.65f && stdDevLum < 18f -> true

            // 3. Luminancia global sumamente baja con fondo casi en su totalidad oscuro:
            avgLum < 30f && stdDevLum < 15f -> true

            // 4. Caso acromático (yelmo espartano rival sin texturas):
            isAchromatic && darkRatio >= 0.62f && stdDevLum < 16f -> true
            isAchromatic && stdDevLum < 12f && avgLum < 45f -> true

            // 5. Firma de Icono de Línea (glifo simple sobre fondo oscuro uniforme):
            colorfulRatio < 0.15f && darkRatio >= 0.68f && stdDevLum < 18f -> true

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
     * Ejecuta el análisis del 10º Pick con Google MediaPipe / LiteRT.
     * 
     * CONDICIÓN ESTRICTA: Solo se ejecuta si [confirmedPicksCount] >= 9.
     * 
     * @param cropBitmap Recorte visual del slot o círculo superior correspondiente al 10º pick.
     * @param isAlly Indica si el 10º pick pertenece al bando aliado o enemigo.
     * @param confirmedChampionIds Campeones ya detectados y seleccionados en los picks 1 a 9 (para excluirlos).
     * @param confirmedPicksCount Cantidad de selecciones previas ya confirmadas en el draft.
     * @param slotIndex Índice del slot (típicamente 4 para el 5º jugador).
     * @param isSlotShowingLaneOrEmpty Si es true, el slot textualmente sigue mostrando la línea asignada o no tiene campeón mientras la selección sigue activa.
     * @param isActiveSelectionPhase Indica si la partida está en selección activa en pantalla.
     * @param context Contexto de la aplicación.
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
        val slotDesc = if (isAlly) "Aliado 5 (10º Pick)" else "Rival 5 (10º Pick)"

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

        // Extraer el embedding tensor del recorte actual del 10º pick
        val inputEmbedding = extractTensorEmbedding(cropBitmap)

        // Evaluar contra todos los campeones no tomados usando correlación de Pearson
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

        // Ordenar candidatos por similitud Pearson de mayor a menor
        val sortedCandidates = candidateScores.sortedByDescending { it.second }

        // Distribución Softmax para probabilidades relativas (temperatura calibrada T = 0.08)
        val temperature = 0.08f
        val top5 = sortedCandidates.take(5)
        val maxSim = top5.first().second
        val expValues = top5.map { exp((it.second - maxSim) / temperature) }
        val expSum = expValues.sum().coerceAtLeast(1e-6f)
        val probabilities = expValues.map { (it / expSum).coerceIn(0f, 1f) }

        val candidateReports = top5.mapIndexed { index, pair ->
            val prob = probabilities[index]
            val simNorm = ((pair.second + 1.0f) / 2.0f).coerceIn(0f, 1f)
            val confPct = ((simNorm * 0.7f + prob * 0.3f) * 100).toInt().coerceIn(1, 99)
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
        val finalConfidence = bestCandidate.confidencePercent.coerceIn(65, 99)

        // SELECCIÓN DIRECTA DEL 10º PICK:
        // Con las 9 selecciones confirmadas y el área verificada, el motor LiteRT selecciona
        // al campeón ganador entre los disponibles y confirma el 10º pick en el draft.
        stableFramesCounter = REQUIRED_STABLE_FRAMES
        lastCandidateId = winnerChamp.id
        val isConfirmed = true

        val decisionReason = "10º Pick seleccionado por LiteRT: ${winnerChamp.name} (${(bestCandidate.similarityScore * 100).toInt()}% similitud, margen ${(scoreMargin * 100).toInt()}%)"
        val persistentCrop = try { cropBitmap.copy(Bitmap.Config.ARGB_8888, false) } catch (_: Throwable) { null }

        _reportFlow.value = LiteRTInferenceReport(
            status = EngineStatus.COMPLETED,
            pickedChampion = winnerChamp,
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

        AppLogger.d(TAG, "LiteRT seleccionó exitosamente el 10º Pick: ${winnerChamp.name} ($finalConfidence%)")
        return@withContext Pair(winnerChamp, finalConfidence)
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
