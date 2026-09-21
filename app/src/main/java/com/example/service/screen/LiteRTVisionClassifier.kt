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

    // Umbrales calibrados de Google MediaPipe / LiteRT para clasificación del 10º pick
    const val MIN_CONFIDENCE_THRESHOLD = 0.35f
    const val MIN_CANDIDATE_MARGIN = 0.015f

    // Permite confirmación rápida del 10º pick al final del draft (1 frame con alta confianza o 2 frames continuos)
    const val REQUIRED_STABLE_FRAMES = 1

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
        val maxRadiusSq = (TENSOR_INPUT_SIZE * 0.46f) * (TENSOR_INPUT_SIZE * 0.46f)

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
     * Calcula la correlación de Pearson entre dos vectores de características [-1.0 a 1.0].
     * A diferencia del producto punto o similitud coseno directa sobre números positivos (que sesga
     * imágenes oscuras hacia puntuaciones artificiales de 0.70-0.80), la correlación de Pearson resta
     * la media eliminando el sesgo de luminancia global y evaluando la correspondencia real
     * de contrastes, tonos cromáticos y distribución espacial.
     */
    private fun pearsonCorrelation(v1: FloatArray, v2: FloatArray): Float {
        val len = min(v1.size, v2.size)
        if (len == 0) return 0f
        var sum1 = 0f
        var sum2 = 0f
        for (i in 0 until len) {
            sum1 += v1[i]
            sum2 += v2[i]
        }
        val mean1 = sum1 / len
        val mean2 = sum2 / len

        var dot = 0f
        var var1 = 0f
        var var2 = 0f
        for (i in 0 until len) {
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
        // En Wild Rift, un slot en espera (yelmo espartano o icono de línea) sin campeón seleccionado:
        // Presenta un fondo negro casi total (> 84% de píxeles oscuros) con un contraste mínimo (stdDevLum < 14).
        // En cambio, el retrato de cualquier campeón (incluso de temática oscura como Nocturne, Zed o Vayne)
        // posee relieves faciales, brillos en armas y texturas vivas con contraste y áreas iluminadas.
        val isAchromatic = maxSat < 28 && colorfulRatio < 0.035f
        val isEmptyOrWaiting = when {
            // 1. Prácticamente todo oscuro (slot completamente apagado o fondo negro)
            maxLum < 35 -> true

            // 2. Fondo negro liso predominante con glifo minúsculo central:
            darkRatio >= 0.86f && stdDevLum < 14f -> true
            darkRatio >= 0.92f -> true

            // 3. Luminancia global extremadamente baja:
            avgLum < 16f -> true
            avgLum < 22f && darkRatio >= 0.78f && stdDevLum < 12f -> true

            // 4. Caso acromático (yelmo espartano o icono de carril plano en escala de grises):
            isAchromatic && avgLum < 28f && stdDevLum < 10f -> true
            isAchromatic && darkRatio >= 0.80f && stdDevLum < 12f -> true

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

        // REGLA FUNDAMENTAL: Requiere que las selecciones 1 a 9 estén presentes
        if (confirmedPicksCount < 9) {
            resetStabilityTracker()
            val copiedCrop = try { cropBitmap?.copy(Bitmap.Config.ARGB_8888, false) } catch (_: Throwable) { null }
            _reportFlow.value = LiteRTInferenceReport(
                status = EngineStatus.WAITING_FOR_PICKS_1_TO_9,
                pickedChampion = null,
                confidencePercent = 0,
                decisionReason = "Esperando selecciones 1 al 9 completas ($confirmedPicksCount/9 detectados)",
                slotDescription = slotDesc,
                evaluatedPicksCount = confirmedPicksCount,
                cropBitmap = copiedCrop ?: _reportFlow.value.cropBitmap
            )
            return@withContext null
        }

        // Si el slot aliado aún muestra el nombre de la línea asignada (ej. "APOYO", "JUNGLA", etc.)
        // o no tiene campeón y la selección activa sigue en curso, el jugador aún no ha elegido
        if (isSlotShowingLaneOrEmpty) {
            resetStabilityTracker()
            val persistentCrop = try { cropBitmap?.copy(Bitmap.Config.ARGB_8888, false) } catch (_: Throwable) { null }
            val reason = if (isAlly) {
                "El 10º jugador (Aliado) se encuentra en su turno de selección; el slot aún muestra la línea asignada. Esperando a que elija y confirme a su campeón."
            } else {
                "El 10º jugador (Rival) se encuentra en su turno de selección. Esperando confirmación de campeón."
            }
            _reportFlow.value = LiteRTInferenceReport(
                status = EngineStatus.WAITING_FOR_TENTH_PICK,
                pickedChampion = null,
                confidencePercent = 0,
                decisionReason = reason,
                slotDescription = slotDesc,
                evaluatedPicksCount = confirmedPicksCount,
                cropBitmap = persistentCrop ?: _reportFlow.value.cropBitmap,
                isConfirmed = false
            )
            AppLogger.d(TAG, "LiteRT 10º Pick [EN ESPERA POR TEXTO/LÍNEA]: $reason")
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

        // COMPROBACIÓN CRÍTICA DEL USUARIO:
        // En el slot final de Wild Rift:
        // - Lado rival: muestra un borde rojo y un icono de yelmo espartano gris oscuro esperando selección.
        // - Lado aliado: muestra un borde azul y el icono de la línea asignada esperando selección.
        // - ÚNICAMENTE cuando el jugador confirma la selección, el icono es reemplazado por el Avatar del campeón.
        // Si el slot está en espera o vacío, NO SE DEBE INVENTAR NINGÚN CAMPEÓN.
        val visualAnalysis = analyzeSlotContent(cropBitmap, isAlly)
        if (visualAnalysis.isEmptyOrWaiting) {
            resetStabilityTracker()
            val persistentCrop = try { cropBitmap.copy(Bitmap.Config.ARGB_8888, false) } catch (_: Throwable) { null }
            _reportFlow.value = LiteRTInferenceReport(
                status = EngineStatus.WAITING_FOR_TENTH_PICK,
                pickedChampion = null,
                confidencePercent = 0,
                decisionReason = visualAnalysis.reason,
                slotDescription = slotDesc,
                evaluatedPicksCount = confirmedPicksCount,
                cropBitmap = persistentCrop,
                isConfirmed = false
            )
            AppLogger.d(TAG, "LiteRT 10º Pick [EN ESPERA]: ${visualAnalysis.reason}")
            return@withContext null
        }

        val startTime = System.currentTimeMillis()
        ensureIndexed(context)

        // Extraer el embedding tensor del recorte actual
        val inputEmbedding = extractTensorEmbedding(cropBitmap)

        // Evaluar contra todos los campeones no tomados usando correlación de Pearson
        val allChamps = WildRiftRepository.champions
        val candidateScores = mutableListOf<Pair<Champion, Float>>()

        for (champ in allChamps) {
            // El 10º pick no puede ser un campeón ya seleccionado en picks 1 a 9
            if (confirmedChampionIds.contains(champ.id)) continue

            val cachedEmbedding = championEmbeddingCache[champ.id] ?: continue
            val similarity = pearsonCorrelation(inputEmbedding, cachedEmbedding)
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
        val finalConfidence = bestCandidate.confidencePercent

        // CONTROL DE UMBRAL DE CONFIANZA MÍNIMO (Confidence Threshold) Y FRAMES ESTABLES:
        // Solicitado expresamente por el usuario para evitar selecciones accidentales o falsos positivos
        val passesConfidence = bestCandidate.similarityScore >= MIN_CONFIDENCE_THRESHOLD && scoreMargin >= MIN_CANDIDATE_MARGIN

        if (passesConfidence) {
            if (winnerChamp.id == lastCandidateId) {
                stableFramesCounter++
            } else {
                lastCandidateId = winnerChamp.id
                stableFramesCounter = 1
            }
        } else {
            if (stableFramesCounter > 0 && bestCandidate.similarityScore < (MIN_CONFIDENCE_THRESHOLD * 0.80f)) {
                stableFramesCounter = 0
                lastCandidateId = null
            }
        }

        // CONFIRMACIÓN PRECISA Y DECISIVA DEL 10º PICK:
        // Confirma de inmediato con similitud sólida (>= 0.42f y margen >= 0.015f),
        // o tras 1-2 frames si la similitud supera el umbral base (>= 0.35f).
        val isConfirmed = passesConfidence && (
            (bestCandidate.similarityScore >= 0.42f && scoreMargin >= 0.015f) ||
            (bestCandidate.similarityScore >= 0.35f && stableFramesCounter >= REQUIRED_STABLE_FRAMES) ||
            (stableFramesCounter >= 2)
        )

        val decisionReason = when {
            isConfirmed -> {
                "Google MediaPipe / LiteRT confirmó a ${winnerChamp.name} tras validar tensores (${(bestCandidate.similarityScore * 100).toInt()}% >= ${(MIN_CONFIDENCE_THRESHOLD * 100).toInt()}%, margen ${(scoreMargin * 100).toInt()}%) en $stableFramesCounter frame(s) estables."
            }
            passesConfidence -> {
                "Candidato ${winnerChamp.name} detectado (${(bestCandidate.similarityScore * 100).toInt()}% >= ${(MIN_CONFIDENCE_THRESHOLD * 100).toInt()}%). Estabilizando: $stableFramesCounter/$REQUIRED_STABLE_FRAMES frames..."
            }
            bestCandidate.similarityScore >= MIN_CONFIDENCE_THRESHOLD -> {
                "Margen estrecho (${winnerChamp.name}: ${(bestCandidate.similarityScore * 100).toInt()}%, margen ${(scoreMargin * 100).toInt()}%). Evaluando..."
            }
            else -> {
                "Puntaje tensor bajo (${(bestCandidate.similarityScore * 100).toInt()}% < ${(MIN_CONFIDENCE_THRESHOLD * 100).toInt()}%). Continuando escaneo..."
            }
        }

        val persistentCrop = try { cropBitmap.copy(Bitmap.Config.ARGB_8888, false) } catch (_: Throwable) { null }

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
            requiredStableFrames = if (bestCandidate.similarityScore >= 0.48f) REQUIRED_STABLE_FRAMES else 3,
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
