package com.example.service.screen

import android.graphics.Bitmap
import android.graphics.Rect
import com.example.data.WildRiftRepository
import com.example.model.Champion
import com.example.model.LaneRole
import com.example.util.AppLogger
import com.example.util.SummonerSpellDetector
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.util.Locale

enum class DiagnosticStatus {
    CONFIRMADO,
    RECHAZADO,
    AMBIGUO,
    VACIO
}

data class SlotDiagnostic(
    val slotIndex: Int,
    val isAlly: Boolean,
    val roiRect: Rect,
    val candidate1: Champion?,
    val score1: Float,
    val candidate2: Champion?,
    val score2: Float,
    val margin: Float,
    val ocrChampion: Champion?,
    val finalChampion: Champion?,
    val status: DiagnosticStatus,
    val reason: String
) {
    fun toFormattedString(): String {
        val team = if (isAlly) "Aliado" else "Enemigo"
        return """
            [$team Slot $slotIndex]
            ROI: ${roiRect.left},${roiRect.top} → ${roiRect.right},${roiRect.bottom}
            Candidato #1: ${candidate1?.name ?: "Ninguno"} (Score: ${"%.2f".format(Locale.US, score1)})
            Candidato #2: ${candidate2?.name ?: "Ninguno"} (Score: ${"%.2f".format(Locale.US, score2)})
            Margen: ${"%.2f".format(Locale.US, margin)}
            OCR: ${ocrChampion?.name ?: "Ninguno"}
            Estado: $status
            Razón: $reason
        """.trimIndent()
    }
}

data class ScannedSlotInfo(
    val slotIndex: Int,
    val isAlly: Boolean = true,
    var champion: Champion? = null,
    var explicitRole: LaneRole? = null,
    var assignedRole: LaneRole? = null,
    var confidencePercent: Int = 0,
    var isLikelyUnpicked: Boolean = false,
    var auditLog: String? = null,
    var summonerSpells: List<String> = emptyList()
)

data class TextBlockDiagnostic(
    val text: String,
    val rect: Rect,
    val isAlly: Boolean,
    val slotIndex: Int,
    val tag: String,
    val color: Int
)

data class DraftPickTurn(
    val turnNumber: Int, // 1..10
    val isAlly: Boolean,
    val slotIndex: Int // 0..4
)

data class DraftScanResult(
    val allies: List<Champion>,
    val enemies: List<Champion>,
    val alliesBySlot: Map<Int, Champion> = emptyMap(),
    val enemiesBySlot: Map<Int, Champion> = emptyMap(),
    val alliesByRole: Map<LaneRole, Champion> = emptyMap(),
    val enemiesByRole: Map<LaneRole, Champion> = emptyMap(),
    val enemyConfidencesByRole: Map<LaneRole, Int> = emptyMap(),
    val detectedRole: LaneRole? = null,
    val userExplicitlyDetectedRole: LaneRole? = null,
    val detectedFirstPick: Boolean? = null,
    val isLastPickImageRecognized: Boolean = false,
    val isLastPickConfirmed: Boolean = false,
    val lastPickChampion: Champion? = null,
    val detectedRawWords: List<String> = emptyList(),
    val discrepancies: List<String> = emptyList(),
    val diagnostics: List<SlotDiagnostic> = emptyList(),
    val allySummonerNamesBySlot: Map<Int, String> = emptyMap(),
    val allySpellsBySlot: Map<Int, List<String>> = emptyMap(),
    val enemySpellsBySlot: Map<Int, List<String>> = emptyMap(),
    val allySummonerNamesByRole: Map<LaneRole, String> = emptyMap(),
    val allySpellsByRole: Map<LaneRole, List<String>> = emptyMap(),
    val isLegendaryRanked: Boolean = false,
    val isPreparationPhase: Boolean = false,
    val hasDraftActivity: Boolean = false,
    val isSuccessful: Boolean,
    val statusMessage: String
)

object DraftVisionScanner {
    private const val TAG = "DraftVisionScanner"
    var overlayRect: android.graphics.Rect? = null
    val showCalibrationBoxes = kotlinx.coroutines.flow.MutableStateFlow(false)
    val debugVisualMatches = kotlinx.coroutines.flow.MutableStateFlow<Map<String, String>>(emptyMap())

    
    /**
     * Devuelve la secuencia real de los 10 turnos del Draft de Wild Rift:
     * - Si PRIMERA SELECCIÓN (Aliado elige primero):
     *   A1 -> E1 -> E2 -> A2 -> A3 -> E3 -> E4 -> A4 -> A5 -> E5 (Pick 10: Rival 5)
     * - Si SIN PRIMERA SELECCIÓN (Rival elige primero):
     *   E1 -> A1 -> A2 -> E2 -> E3 -> A3 -> A4 -> E4 -> E5 -> A5 (Pick 10: Aliado 5)
     */
    fun getDraftPickSequence(isFirstPick: Boolean): List<DraftPickTurn> {
        return if (isFirstPick) {
            listOf(
                DraftPickTurn(1, isAlly = true, slotIndex = 0),   // A1
                DraftPickTurn(2, isAlly = false, slotIndex = 0),  // E1
                DraftPickTurn(3, isAlly = false, slotIndex = 1),  // E2
                DraftPickTurn(4, isAlly = true, slotIndex = 1),   // A2
                DraftPickTurn(5, isAlly = true, slotIndex = 2),   // A3
                DraftPickTurn(6, isAlly = false, slotIndex = 2),  // E3
                DraftPickTurn(7, isAlly = false, slotIndex = 3),  // E4
                DraftPickTurn(8, isAlly = true, slotIndex = 3),   // A4
                DraftPickTurn(9, isAlly = true, slotIndex = 4),   // A5
                DraftPickTurn(10, isAlly = false, slotIndex = 4)  // E5
            )
        } else {
            listOf(
                DraftPickTurn(1, isAlly = false, slotIndex = 0),  // E1
                DraftPickTurn(2, isAlly = true, slotIndex = 0),   // A1
                DraftPickTurn(3, isAlly = true, slotIndex = 1),   // A2
                DraftPickTurn(4, isAlly = false, slotIndex = 1),  // E2
                DraftPickTurn(5, isAlly = false, slotIndex = 2),  // E3
                DraftPickTurn(6, isAlly = true, slotIndex = 2),   // A3
                DraftPickTurn(7, isAlly = true, slotIndex = 3),   // A4
                DraftPickTurn(8, isAlly = false, slotIndex = 3),  // E4
                DraftPickTurn(9, isAlly = false, slotIndex = 4),  // E5
                DraftPickTurn(10, isAlly = true, slotIndex = 4)   // A5
            )
        }
    }

    var calibrationConfig = VisionCalibrationConfig()
    val calibrationConfigFlow = kotlinx.coroutines.flow.MutableStateFlow(VisionCalibrationConfig())

    fun initCalibration(context: android.content.Context) {
        calibrationConfig = VisionCalibrationConfig.loadFromPrefs(context)
        calibrationConfigFlow.value = calibrationConfig
    }

    fun updateCalibration(context: android.content.Context, newConfig: VisionCalibrationConfig) {
        calibrationConfig = newConfig
        calibrationConfigFlow.value = newConfig
        newConfig.saveToPrefs(context)
    }

    fun resetCalibration(context: android.content.Context) {
        calibrationConfig = VisionCalibrationConfig()
        calibrationConfigFlow.value = calibrationConfig
        calibrationConfig.saveToPrefs(context)
    }

    private var recognizerInstance: com.google.mlkit.vision.text.TextRecognizer? = null

    // Memoria persistente de los carriles asignados a cada slot aliado (0..4)
    // En Wild Rift, el carril asignado a cada jugador es fijo durante toda la fase de selección
    private val allySlotRolesCache = mutableMapOf<Int, LaneRole>()
    // Memoria persistente de los nombres de invocador aliados (0..4)
    private val allySummonerNamesCache = mutableMapOf<Int, String>()
    // Memoria persistente del slot asignado al usuario
    private var cachedUserSlotIndex: Int? = null

    // Memoria persistente de campeones confirmados por slot para evitar que desaparezcan al terminar o transicionar
    val allySlotConfirmedChampions = arrayOfNulls<Champion>(5)
    val enemySlotConfirmedChampions = arrayOfNulls<Champion>(5)

    // Filtros de estabilización temporal (anti-parpadeo y anti-oscilación)
    private class SlotTemporalFilter {
        private var lastConfirmedChampion: Champion? = null

        fun process(candidate: Champion?, isUnpicked: Boolean, persistentCache: Champion?): Champion? {
            val champ = candidate ?: persistentCache ?: lastConfirmedChampion
            if (champ != null) {
                lastConfirmedChampion = champ
                return champ
            }
            if (isUnpicked) {
                lastConfirmedChampion = null
                return null
            }
            return null
        }

        fun reset() {
            lastConfirmedChampion = null
        }
    }

    private var isLegendaryRankedCache = false
    private val allySlotFilters = Array(5) { SlotTemporalFilter() }
    private val enemySlotFilters = Array(5) { SlotTemporalFilter() }

    fun resetSlotMemory() {
        isLegendaryRankedCache = false
        cachedUserSlotIndex = null
        allySlotRolesCache.clear()
        allySummonerNamesCache.clear()
        allySlotConfirmedChampions.fill(null)
        enemySlotConfirmedChampions.fill(null)
        allySlotFilters.forEach { it.reset() }
        enemySlotFilters.forEach { it.reset() }
        LiteRTVisionClassifier.reset()
        AppLogger.d(TAG, "Memoria de roles, invocadores y motor LiteRT reiniciada por completo")
    }

    private fun getRecognizer(): com.google.mlkit.vision.text.TextRecognizer? {
        if (recognizerInstance == null) {
            try {
                recognizerInstance = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            } catch (e: Throwable) {
                AppLogger.e(TAG, "ML Kit TextRecognizer init warning", e)
            }
        }
        return recognizerInstance
    }

    suspend fun scanDraftFromBitmap(
        bitmap: Bitmap,
        context: android.content.Context? = null,
        currentIsFirstPick: Boolean? = null,
        currentActiveRole: LaneRole? = null
    ): DraftScanResult {
        if (bitmap.isRecycled || bitmap.width < bitmap.height) {
            return DraftScanResult(emptyList(), emptyList(), isSuccessful = false, statusMessage = "Orientación no horizontal")
        }

        return try {
            val recognizer = getRecognizer() ?: return DraftScanResult(emptyList(), emptyList(), isSuccessful = false, statusMessage = "OCR no disponible")

        val width = bitmap.width
        val height = bitmap.height
        val allChamps = WildRiftRepository.champions
        val auditList = mutableListOf<String>()
        val defaultRolesList = listOf(LaneRole.TOP, LaneRole.JUNGLE, LaneRole.MID, LaneRole.ADC, LaneRole.SUPPORT)

        // Calibración adaptativa multipantalla para cualquier relación de aspecto (16:9, 18:9, 19.5:9, 20:9, 21:9, tablets)
        val calib = AdaptiveScreenLayoutEngine.computeAdaptiveConfig(width, height, calibrationConfig)

        // 5 slots para aliados y 5 slots para enemigos
        val allySlots = (0..4).map { ScannedSlotInfo(slotIndex = it, isAlly = true) }
        val enemySlots = (0..4).map { ScannedSlotInfo(slotIndex = it, isAlly = false) }
        val detectedWords = mutableListOf<String>()
        var userDetectedLane: LaneRole? = null
        var userSlotIndex: Int? = null
        var userExplicitlyConfirmed = false
        var detectedFirstPick: Boolean? = null
        val allySlotTexts = Array(5) { mutableListOf<Pair<String, Rect?>>() }
        val enemySlotTexts = Array(5) { mutableListOf<Pair<String, Rect?>>() }
        val allyOcrChampions = Array<Champion?>(5) { null }
        val enemyOcrChampions = Array<Champion?>(5) { null }
        val textDiagnosticsList = mutableListOf<TextBlockDiagnostic>()

        // -----------------------------------------------------------------------------------------
        // PASO 1: OCR DIRECTO CON MÁXIMA FIDELIDAD ÓPTICA
        // -----------------------------------------------------------------------------------------
        var isLegendaryRanked = false
        var isPreparationPhase = false
        var isActiveSelectionDetected = false
        var isPreparationBannerDetected = false
        try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val visionText = recognizer.process(inputImage).await()

            // Detección proactiva de Clasificatoria Legendaria en pantalla completa
            isLegendaryRanked = DraftValidationLayer.isLegendaryRankedDraft(
                fullOcrText = visionText.text,
                hasRealSummonerNames = allySummonerNamesCache.isNotEmpty()
            )
            isLegendaryRankedCache = isLegendaryRanked
            if (isLegendaryRanked) {
                AppLogger.d(TAG, "Clasificatoria Legendaria detectada en pantalla (Nombres anónimos). Búsqueda de invocadores desactivada.")
                allySummonerNamesCache.clear()
            }

            for (block in visionText.textBlocks) {
                for (line in block.lines) {
                    val text = line.text.trim()
                    if (text.isBlank() || text.length < 2) continue

                    val box = line.boundingBox
                    if (box != null && overlayRect != null) {
                        if (android.graphics.Rect.intersects(box, overlayRect!!)) {
                            continue // Ignorar texto que cae dentro de la ventana flotante
                        }
                    }

                    val centerY = box?.centerY() ?: 0
                    val centerX = box?.centerX() ?: 0
                    val xRatio = if (width > 0) centerX.toFloat() / width.toFloat() else 0.5f
                    val yRatio = if (height > 0) centerY.toFloat() / height.toFloat() else 0.5f

                    val lowerText = text.lowercase(Locale.ROOT)
                    val isAssistantOverlayText = lowerText.contains("campeones confirmados") || 
                                                 lowerText.contains("escaneo manual") || 
                                                 lowerText.contains("modo manual") ||
                                                 lowerText.contains("coach") ||
                                                 lowerText.contains("visor")
                    if (isAssistantOverlayText) continue

                    val textNormLine = DraftValidationLayer.normalize(lowerText)

                    // Detección de Selección Activa en la cabecera / pantalla (Ej: "LOS OPONENTES ESTÁN ELIGIENDO", "ELIGE TU CAMPEÓN")
                    val isSelectionPhaseText = textNormLine.contains("estan eligiendo") || 
                                               textNormLine.contains("estao escolhendo") || 
                                               textNormLine.contains("are picking") || 
                                               textNormLine.contains("are choosing") ||
                                               textNormLine.contains("elige tu") || 
                                               textNormLine.contains("escolha seu") || 
                                               textNormLine.contains("choose your") || 
                                               textNormLine.contains("bloquea") || 
                                               textNormLine.contains("bloqueie") || 
                                               textNormLine.contains("ban a")
                    if (isSelectionPhaseText) {
                        isActiveSelectionDetected = true
                    }

                    if (yRatio < 0.22f && (textNormLine.contains("fase de preparacion") || 
                        textNormLine.contains("fase de preparacao") || 
                        textNormLine.contains("preparation phase") ||
                        (textNormLine.contains("preparaci") && !textNormLine.contains("preselecci")) ||
                        (textNormLine.contains("preparaç") && !textNormLine.contains("pre-seleç")))) {
                        isPreparationBannerDetected = true
                    }

                    // EXCLUSIÓN DEL OVERLAY FLOTANTE DEL ASISTENTE
                    if (box != null && overlayRect != null && android.graphics.Rect.intersects(box, overlayRect!!)) {
                        continue
                    }

                    // Detección automática de Primera / Segunda Selección por texto y ubicación espacial superior
                    val textNorm = DraftValidationLayer.normalize(text)

                    val hasPrimera = textNorm.contains("primera eleccion") || textNorm.contains("primera seleccion") ||
                                     textNorm.contains("primer pick") || textNorm.contains("first pick") ||
                                     textNorm.contains("1a eleccion") || textNorm.contains("1ª eleccion") ||
                                     textNorm.contains("1.a eleccion") || textNorm.contains("1.ª eleccion") ||
                                     textNorm.contains("1a seleccion") || textNorm.contains("1ª seleccion") ||
                                     textNorm.contains("1.a seleccion") || textNorm.contains("1.ª seleccion") ||
                                     textNorm.contains("primeira escolha") || textNorm.contains("primeira selecao")

                    val hasSegunda = textNorm.contains("segunda eleccion") || textNorm.contains("segunda seleccion") ||
                                     textNorm.contains("segundo pick") || textNorm.contains("second pick") ||
                                     textNorm.contains("2a eleccion") || textNorm.contains("2ª eleccion") ||
                                     textNorm.contains("2.a eleccion") || textNorm.contains("2.ª eleccion") ||
                                     textNorm.contains("2a seleccion") || textNorm.contains("2ª seleccion") ||
                                     textNorm.contains("2.a seleccion") || textNorm.contains("2.ª seleccion") ||
                                     textNorm.contains("segunda escolha") || textNorm.contains("segunda selecao")

                    // Detectar en la cabecera superior extrema donde aparecen los banners oficiales
                    if (yRatio < 0.25f && (xRatio < 0.40f || xRatio > 0.60f)) {
                        if (hasPrimera) {
                            if (xRatio > 0.50f) {
                                detectedFirstPick = false
                                AppLogger.d(TAG, "OCR Primera Selección detectada en lado RIVAL (xRatio=$xRatio) -> Aliados = Segunda Selección")
                            } else {
                                detectedFirstPick = true
                                AppLogger.d(TAG, "OCR Primera Selección detectada en lado ALIADO (xRatio=$xRatio) -> Aliados = Primera Selección")
                            }
                        } else if (hasSegunda) {
                            if (xRatio > 0.50f) {
                                detectedFirstPick = true
                                AppLogger.d(TAG, "OCR Segunda Selección detectada en lado RIVAL (xRatio=$xRatio) -> Aliados = Primera Selección")
                            } else {
                                detectedFirstPick = false
                                AppLogger.d(TAG, "OCR Segunda Selección detectada en lado ALIADO (xRatio=$xRatio) -> Aliados = Segunda Selección")
                            }
                        }
                    }

                    // Si el texto es una indicación de primera/segunda selección o ruido de interfaz general, descartarlo
                    if (hasPrimera || hasSegunda || DraftValidationLayer.isNoiseText(text)) {
                        continue
                    }

                    // Ignorar cualquier texto generado por el overlay de depuración
                    if (text.contains("[") || text.contains("]") ||
                        text.contains("VISUAL", ignoreCase = true) || text.contains("VIS:", ignoreCase = true) ||
                        text.contains("OCR", ignoreCase = true) || text.contains("INVOCADOR", ignoreCase = true) ||
                        text.contains("CAMPEÓN", ignoreCase = true) || text.contains("CAMPEON", ignoreCase = true) ||
                        text.contains("LÍNEA", ignoreCase = true) || text.contains("LINEA", ignoreCase = true) ||
                        text.contains("RIVAL", ignoreCase = true) || text.contains("VACÍO", ignoreCase = true) ||
                        text.contains("VACIO", ignoreCase = true) || text.contains("CONFIRMADO", ignoreCase = true) ||
                        text.contains("AMBIGUO", ignoreCase = true) || text.contains("⚡") || text.contains("🐛") ||
                        text.contains("Diagnóstico", ignoreCase = true) || text.contains("Diagnostico", ignoreCase = true) ||
                        text.contains("Score", ignoreCase = true)) continue
                    
                    detectedWords.add(text)

                    // Ignorar barra de bans superior (< 0.12f) y botones inferiores extremos (> 0.88f)
                    // Permitir todos los 5 slots (desde y=0.15 hasta y=0.85)
                    if (yRatio < 0.12f || yRatio > 0.880f) continue

                    val isAllyCol = xRatio in calib.allyOcrMinX..calib.allyOcrMaxX
                    val isEnemyCol = xRatio in calib.enemyOcrMinX..calib.enemyOcrMaxX

                    // 1.1 COLUMNA ALIADA (Texto a la derecha del avatar aliado)
                    if (isAllyCol) {
                        var bestSlot = -1
                        var minDiff = 0.085f
                        for (s in 0..4) {
                            val diff = kotlin.math.abs(yRatio - calib.allySlotYRatios[s])
                            if (diff < minDiff) {
                                minDiff = diff
                                bestSlot = s
                            }
                        }
                        if (bestSlot != -1) {
                            allySlotTexts[bestSlot].add(Pair(text, box))
                        }
                    }
                    // 1.2 COLUMNA ENEMIGA (Texto a la izquierda del avatar rival)
                    else if (isEnemyCol) {
                        var bestSlot = -1
                        var minDiff = 0.085f
                        for (s in 0..4) {
                            val diff = kotlin.math.abs(yRatio - calib.enemySlotYRatios[s])
                            if (diff < minDiff) {
                                minDiff = diff
                                bestSlot = s
                            }
                        }
                        if (bestSlot != -1) {
                            enemySlotTexts[bestSlot].add(Pair(text, box))
                        }
                    }
                }
            }

            if (isActiveSelectionDetected) {
                isPreparationPhase = false
                AppLogger.d(TAG, "OCR: Selección activa en curso detectada ('Están eligiendo / Elige / Bloquea'). Fase de Preparación desactivada.")
            } else if (isPreparationBannerDetected) {
                isPreparationPhase = true
                AppLogger.d(TAG, "OCR: Banner 'Fase de Preparación' confirmado en cabecera.")
            }

            userExplicitlyConfirmed = false
            val currentUserNameClean = if (!isLegendaryRanked) {
                try {
                    val u1 = com.example.util.SubscriptionManager.userName.value.trim().lowercase(Locale.ROOT).replace(" ", "")
                    val u2 = com.example.util.AuthManager.getAuth()?.currentUser?.displayName?.trim()?.lowercase(Locale.ROOT)?.replace(" ", "") ?: ""
                    val u3 = try { if (context != null) com.example.data.AccountProfileManager.getActiveProfile(context).name.trim().lowercase(Locale.ROOT).replace(" ", "") else "" } catch (_: Exception) { "" }
                    listOf(u1, u2, u3, "yo", "tu").filter { it.isNotBlank() }
                } catch (_: Exception) {
                    listOf("yo", "tu")
                }
            } else {
                emptyList()
            }

            val tentativeFirstPick = detectedFirstPick ?: currentIsFirstPick ?: false

            // Procesar textos aliados: Detección estricta de Línea 1 (Rol o Campeón) y Línea 2 (Nombre de Invocador)
            for (i in 0..4) {
                val slot = allySlots[i]
                val entries = allySlotTexts[i]

                var detectedRoleInSlot: LaneRole? = null
                var detectedChampInSlot: Champion? = null
                var isSlotShowingLane = false
                val summonerCandidates = mutableListOf<String>()

                if (entries.isNotEmpty()) {
                    // En Wild Rift (Lado Aliado): cada slot muestra [ICONO_MAESTRIA_O_ROL] + [VALOR].
                    // - Si el jugador ya seleccionó su campeón: el nombre de la línea cambia por el NOMBRE DEL CAMPEÓN (ej: "• JINX", "V JARVAN IV").
                    // - Si el jugador aún no ha seleccionado: el valor sigue siendo el NOMBRE DE LA LÍNEA (ej: "CALLE CENTRAL", "• APOYO", "JUNGLA").
                    // El icono a la izquierda siempre se mantiene y debe ignorarse.
                    for ((line, box) in entries) {
                        if (DraftValidationLayer.isNoiseText(line)) continue
                        val strippedLine = DraftValidationLayer.stripLeadingMasteryOrRoleIcon(line)

                        // 1. ¿Es un campeón seleccionado?
                        val matchedChamp = ChampionNameResolver.findChampionInText(strippedLine, allChamps)
                            ?: ChampionNameResolver.findChampionInText(line, allChamps)

                        if (matchedChamp != null) {
                            detectedChampInSlot = matchedChamp
                            textDiagnosticsList.add(
                                TextBlockDiagnostic(
                                    text = line,
                                    rect = box ?: Rect(0, 0, 10, 10),
                                    isAlly = true,
                                    slotIndex = i,
                                    tag = "CAMPEÓN: ${matchedChamp.name}",
                                    color = android.graphics.Color.GREEN
                                )
                            )
                            AppLogger.d(TAG, "OCR Aliado Slot $i -> Campeón confirmado tras icono: ${matchedChamp.name}")
                            break // Campeón confirmado en este slot; la línea ya cambió
                        }

                        // 2. ¿Es el nombre de la línea asignada (en espera de selección)?
                        val role = DraftValidationLayer.parseRoleFromText(strippedLine)
                            ?: DraftValidationLayer.parseRoleFromText(line)

                        if (role != null) {
                            detectedRoleInSlot = role
                            slot.explicitRole = role
                            allySlotRolesCache[i] = role
                            textDiagnosticsList.add(
                                TextBlockDiagnostic(
                                    text = line,
                                    rect = box ?: Rect(0, 0, 10, 10),
                                    isAlly = true,
                                    slotIndex = i,
                                    tag = "LÍNEA: ${role.shortName} (Esperando)",
                                    color = android.graphics.Color.CYAN
                                )
                            )
                            AppLogger.d(TAG, "OCR Aliado Slot $i -> Línea: ${role.shortName} (Esperando selección)")
                        }
                    }

                    if (detectedChampInSlot != null) {
                        isSlotShowingLane = false
                    } else if (detectedRoleInSlot != null) {
                        isSlotShowingLane = true
                    }

                    // 3. ANALIZAR NOMBRE DE INVOCADOR / TAG DE USUARIO:
                    for ((line, box) in entries) {
                        val safeBox = box ?: Rect(0, 0, 10, 10)
                        if (DraftValidationLayer.isNoiseText(line)) continue

                        val lineNorm = DraftValidationLayer.normalize(line).lowercase(Locale.ROOT)
                        val lineCompressed = lineNorm.replace(" ", "")

                        // DETECCIÓN INFALIBLE DEL SLOT DEL USUARIO EN WILD RIFT:
                        // 1) En Wild Rift, el indicador/botón "Porcentaje de victorias..." ("Taxa de vit...", "Win rate...")
                        //    aparece ÚNICAMENTE en el slot del propio usuario local.
                        val isWinRateIndicator = lineNorm.contains("porcentaje de vic") ||
                            lineNorm.contains("porcentaje de") ||
                            lineNorm.contains("porcentaje") ||
                            lineNorm.contains("taxa de vit") ||
                            lineNorm.contains("taxa de") ||
                            lineNorm.contains("win rate") ||
                            lineNorm.contains("winrate")

                        // 2) Nombre de invocador conocido del usuario (ej: Diego / D I E G O / barbadiego):
                        val isUserNameMatch = lineCompressed == "diego" ||
                            lineCompressed.contains("diego") ||
                            currentUserNameClean.any { it.length >= 3 && (lineCompressed == it || lineCompressed.contains(it)) }

                        // 3) Indicadores canónicos de Riot ("Tú", "You", "Você"):
                        val isExplicitTag = lineNorm == "tu" || lineNorm == "(tu)" || lineNorm == "you" || lineNorm == "(you)" ||
                            lineNorm == "voce" || lineNorm == "(voce)" ||
                            lineNorm.startsWith("(tu) ") || lineNorm.endsWith(" (tu)") ||
                            lineNorm.startsWith("(you) ") || lineNorm.endsWith(" (you)") ||
                            lineNorm.contains(" tú ") || lineNorm.contains("(tú)") ||
                            lineNorm.contains("( tu )") || lineNorm.contains("[tu]") || lineNorm.contains("[tú]") ||
                            lineNorm.contains("( you )") || lineNorm.contains("[you]")

                        val isUserTag = !isLegendaryRanked && (isWinRateIndicator || isUserNameMatch || isExplicitTag)

                        if (isUserTag) {
                            userSlotIndex = i
                            userExplicitlyConfirmed = true
                            textDiagnosticsList.add(
                                TextBlockDiagnostic(
                                    text = line,
                                    rect = safeBox,
                                    isAlly = true,
                                    slotIndex = i,
                                    tag = "¡TU SLOT!",
                                    color = android.graphics.Color.YELLOW
                                )
                            )
                            AppLogger.d(TAG, "Slot del usuario confirmado explícitamente en Slot Aliado $i ('$line')")
                        }

                        val strippedCandidate = DraftValidationLayer.stripLeadingMasteryOrRoleIcon(line)
                        if (!isLegendaryRanked && line.length in 2..24 && !line.startsWith("(") && !line.endsWith(")") &&
                            !isWinRateIndicator &&
                            DraftValidationLayer.parseRoleFromText(line) == null &&
                            DraftValidationLayer.parseRoleFromText(strippedCandidate) == null &&
                            ChampionNameResolver.findChampionInText(line, allChamps) == null &&
                            ChampionNameResolver.findChampionInText(strippedCandidate, allChamps) == null) {
                            summonerCandidates.add(line)
                        }
                    }
                }

                // Si el OCR global no detectó campeón por ruido o icono de maestría, aplicar preprocesamiento adaptativo
                if (detectedChampInSlot == null && allySlotConfirmedChampions[i] == null && recognizer != null) {
                    try {
                        val slotCenterY = calib.allySlotYRatios[i]
                        val startYRatio = (slotCenterY - 0.050f).coerceIn(0f, 0.90f)
                        val endYRatio = (slotCenterY + 0.050f).coerceIn(startYRatio + 0.02f, 1f)
                        val cropX = (width * calib.allyOcrMinX).toInt().coerceIn(0, width - 10)
                        val cropW = ((width * calib.allyOcrMaxX).toInt() - cropX).coerceIn(10, width - cropX)
                        val cropY = (height * startYRatio).toInt().coerceIn(0, height - 10)
                        val cropH = ((height * endYRatio).toInt() - cropY).coerceIn(10, height - cropY)

                        if (cropW > 30 && cropH > 15) {
                            val rawSlotCrop = Bitmap.createBitmap(bitmap, cropX, cropY, cropW, cropH)
                            val preprocBitmap = SlotImagePreProcessor.preprocessSlotTextRegion(
                                rawSlotCrop,
                                isAlly = true,
                                suppressLeadingMasteryIcon = true
                            )
                            if (preprocBitmap != null) {
                                val inputImg = com.google.mlkit.vision.common.InputImage.fromBitmap(preprocBitmap, 0)
                                val task = recognizer.process(inputImg)
                                val preprocRes = com.google.android.gms.tasks.Tasks.await(task, 400, java.util.concurrent.TimeUnit.MILLISECONDS)
                                for (blk in preprocRes.textBlocks) {
                                    for (ln in blk.lines) {
                                        val lnTxt = ln.text.trim()
                                        val strippedLn = DraftValidationLayer.stripLeadingMasteryOrRoleIcon(lnTxt)
                                        val cMatched = ChampionNameResolver.findChampionInText(strippedLn, allChamps)
                                            ?: ChampionNameResolver.findChampionInText(lnTxt, allChamps)
                                        if (cMatched != null) {
                                            detectedChampInSlot = cMatched
                                            AppLogger.d(TAG, "OCR Aliado Slot $i -> Campeón detectado con Pre-Procesamiento (Anti-Maestría): ${cMatched.name}")
                                            break
                                        }
                                        val rMatched = DraftValidationLayer.parseRoleFromText(strippedLn)
                                            ?: DraftValidationLayer.parseRoleFromText(lnTxt)
                                        if (rMatched != null && detectedRoleInSlot == null) {
                                            detectedRoleInSlot = rMatched
                                            allySlotRolesCache[i] = rMatched
                                            slot.explicitRole = rMatched
                                        }
                                    }
                                    if (detectedChampInSlot != null) break
                                }
                                if (!preprocBitmap.isRecycled) preprocBitmap.recycle()
                            }
                            if (!rawSlotCrop.isRecycled) rawSlotCrop.recycle()
                        }
                    } catch (e: Throwable) {
                        AppLogger.d(TAG, "Pre-procesamiento slot $i skipped: ${e.message}")
                    }
                }

                // Guardar nombre de invocador detectado al instante
                if (summonerCandidates.isNotEmpty() && !isLegendaryRanked) {
                    val candidateName = summonerCandidates.first()
                    if (!candidateName.lowercase(Locale.ROOT).startsWith("jugador en") &&
                        !candidateName.lowercase(Locale.ROOT).startsWith("jogador na")) {
                        isLegendaryRanked = false
                        isLegendaryRankedCache = false
                        allySummonerNamesCache[i] = candidateName
                        textDiagnosticsList.add(
                            TextBlockDiagnostic(
                                text = candidateName,
                                rect = entries.lastOrNull()?.second ?: Rect(0, 0, 10, 10),
                                isAlly = true,
                                slotIndex = i,
                                tag = "INVOCADOR: $candidateName",
                                color = android.graphics.Color.WHITE
                            )
                        )
                    }
                }

                // REGLAS ESTRICTAS DEL USUARIO:
                // "al seleccionar el campeón es porque has tomado su nombre lo cual es 100% correcto y no deberías quitarlo"
                // Si el OCR detecta un campeón en este slot, se confirma al 100%.
                // Si en frames subsiguientes no se detecta nuevo texto, se MANTIENE intacto el campeón ya confirmado.
                if (detectedChampInSlot != null) {
                    allySlotConfirmedChampions[i] = detectedChampInSlot
                    allyOcrChampions[i] = detectedChampInSlot
                    slot.champion = detectedChampInSlot
                    slot.confidencePercent = 100
                    slot.isLikelyUnpicked = false
                } else if (allySlotConfirmedChampions[i] != null) {
                    // Mantener el campeón ya confirmado previamente
                    val existingChamp = allySlotConfirmedChampions[i]
                    allyOcrChampions[i] = existingChamp
                    slot.champion = existingChamp
                    slot.confidencePercent = 100
                    slot.isLikelyUnpicked = false
                } else {
                    // El slot aún no ha seleccionado ningún campeón
                    allyOcrChampions[i] = null
                    slot.champion = null
                    slot.confidencePercent = 0
                    slot.isLikelyUnpicked = true
                }
            }

            // ASIGNACIÓN DETERMINÍSTICA DE ROLES ALIADOS (5 ROLES POR SLOT SEGÚN DISEÑO DE WILD RIFT)
            // En Wild Rift, cada slot aliado muestra primero el carril que va a ir:
            // Slot 0 -> TOP (Calle de Barón)
            // Slot 1 -> JUNGLE (Jungla)
            // Slot 2 -> MID (Calle Central)
            // Slot 3 -> ADC (Calle del Dragón)
            // Slot 4 -> SUPPORT (Soporte / Apoyo)
            // Luego esa línea se cambia por el nombre del campeón al seleccionarlo.
            // Por lo tanto, el carril del slot i es fijo determinístico según su posición o lectura explícita.
            for (i in 0..4) {
                val assignedRole = allySlots[i].explicitRole ?: allySlotRolesCache[i] ?: defaultRolesList[i]
                allySlotRolesCache[i] = assignedRole
                allySlots[i].explicitRole = assignedRole
                allySlots[i].assignedRole = assignedRole
            }

            // Si se detectó el slot del usuario (marcado con "(TÚ)", "Porcentaje de victorias" o nombre), asignar su rol; si no, preservar el rol activo del usuario
            if (userSlotIndex != null) {
                cachedUserSlotIndex = userSlotIndex
            } else if (cachedUserSlotIndex != null) {
                userSlotIndex = cachedUserSlotIndex
                userExplicitlyConfirmed = true
            } else if (currentActiveRole != null) {
                // Si el usuario tiene seleccionado un rol y coincide con el rol de un slot, vincular temporalmente
                val matchingSlot = allySlots.indexOfFirst { it.explicitRole == currentActiveRole || allySlotRolesCache[it.slotIndex] == currentActiveRole }
                if (matchingSlot != -1) {
                    userSlotIndex = matchingSlot
                    cachedUserSlotIndex = matchingSlot
                    userExplicitlyConfirmed = false
                }
            }

            val uIdx = userSlotIndex
            if (uIdx != null && uIdx in 0..4) {
                var explicitRole = allySlots[uIdx].explicitRole ?: allySlotRolesCache[uIdx]
                // Si aún no tenía rol explícito, comprobar si el campeón seleccionado tiene un rol primario (ej: Galio -> MID)
                if (explicitRole == null) {
                    val champ = allySlots[uIdx].champion ?: allySlotConfirmedChampions[uIdx] ?: allyOcrChampions[uIdx]
                    if (champ != null) {
                        explicitRole = champ.primaryRole
                        allySlots[uIdx].explicitRole = explicitRole
                        allySlotRolesCache[uIdx] = explicitRole
                    }
                }
                if (explicitRole != null) {
                    userDetectedLane = explicitRole
                    AppLogger.d(TAG, "Rol de usuario confirmado explícitamente en Slot $uIdx -> ${userDetectedLane.shortName}")
                } else {
                    // Si el slot aliado tiene Castigo/Smite, asignar Jungla
                    val hasSmite = allySlots[uIdx].summonerSpells.any { it.equals("Castigo", ignoreCase = true) || it.equals("Smite", ignoreCase = true) }
                    if (hasSmite) {
                        userDetectedLane = LaneRole.JUNGLE
                        allySlots[uIdx].explicitRole = LaneRole.JUNGLE
                        allySlotRolesCache[uIdx] = LaneRole.JUNGLE
                    } else {
                        // Preservar el rol previamente seleccionado por el usuario en lugar de forzar TOP/default
                        userDetectedLane = currentActiveRole ?: allySlotRolesCache[uIdx]
                    }
                }
            } else if (currentActiveRole != null) {
                userDetectedLane = currentActiveRole
            }

            // Para el lado rival: Analizamos el texto de cada slot.
            // En Wild Rift, cuando un rival fija o selecciona un campeón, el nombre aparece en texto:
            // "ANNIE", "VOLIBEAR", "SERAPHINE", "ASHE", "SHYVANA", "YUUMI", "SAMIRA", etc.
            // Mientras no seleccione, muestra "Jugador 1", "Jugador 2", etc.
            // REGLA CRÍTICA MOBA: Un campeón seleccionado por el equipo aliado JAMÁS puede ser asignado al rival.
            val currentAllyChampIds = (allySlots.mapNotNull { it.champion?.id } + allySlotConfirmedChampions.mapNotNull { it?.id }).toSet()

            for (i in 0..4) {
                var detectedEnemyChamp: Champion? = null
                val enemyEntries = enemySlotTexts[i]

                for ((line, box) in enemyEntries) {
                    val safeBox = box ?: Rect(0, 0, 10, 10)
                    if (DraftValidationLayer.isNoiseText(line)) continue

                    val strippedLine = DraftValidationLayer.stripLeadingMasteryOrRoleIcon(line)
                    val cleanLine = line.replace(Regex("^[^a-zA-Z0-9]+"), "").trim()
                    val lineWithoutLeadingArtifact = if (cleanLine.length > 2 && (cleanLine[1] == ' ' || cleanLine[2] == ' ')) {
                        cleanLine.dropWhile { it != ' ' }.trim()
                    } else cleanLine
                    val tokens = cleanLine.split(Regex("\\s+"))
                    val tokenAfter1 = if (tokens.size > 1) tokens.drop(1).joinToString(" ") else null
                    val tokenAfter2 = if (tokens.size > 2) tokens.drop(2).joinToString(" ") else null
                    val tokenLast = if (tokens.isNotEmpty()) tokens.last() else null

                    val matched = ChampionNameResolver.findChampionInText(strippedLine, allChamps)
                        ?: ChampionNameResolver.findChampionInText(cleanLine, allChamps)
                        ?: ChampionNameResolver.findChampionInText(lineWithoutLeadingArtifact, allChamps)
                        ?: ChampionNameResolver.findChampionInText(line, allChamps)
                        ?: (if (tokenAfter1 != null) ChampionNameResolver.findChampionInText(tokenAfter1, allChamps) else null)
                        ?: (if (tokenAfter2 != null) ChampionNameResolver.findChampionInText(tokenAfter2, allChamps) else null)
                        ?: (if (tokenLast != null) ChampionNameResolver.findChampionInText(tokenLast, allChamps) else null)

                    if (matched != null) {
                        if (currentAllyChampIds.contains(matched.id)) {
                            AppLogger.d(TAG, "OCR Rival Slot $i -> Ignorado ${matched.name}: ya seleccionado por el equipo aliado")
                            continue
                        }
                        detectedEnemyChamp = matched
                        textDiagnosticsList.add(
                            TextBlockDiagnostic(
                                text = line,
                                rect = safeBox,
                                isAlly = false,
                                slotIndex = i,
                                tag = "RIVAL: ${matched.name}",
                                color = android.graphics.Color.RED
                            )
                        )
                        AppLogger.d(TAG, "OCR Rival Slot $i -> Campeón 100%: ${matched.name}")
                        break
                    }
                }

                // Si el OCR global no detectó campeón en slot rival, aplicar preprocesamiento adaptativo
                if (detectedEnemyChamp == null && enemySlotConfirmedChampions[i] == null && recognizer != null) {
                    try {
                        val slotCenterY = calib.enemySlotYRatios[i]
                        val startYRatio = (slotCenterY - 0.050f).coerceIn(0f, 0.90f)
                        val endYRatio = (slotCenterY + 0.050f).coerceIn(startYRatio + 0.02f, 1f)
                        val cropX = (width * calib.enemyOcrMinX).toInt().coerceIn(0, width - 10)
                        val cropW = ((width * calib.enemyOcrMaxX).toInt() - cropX).coerceIn(10, width - cropX)
                        val cropY = (height * startYRatio).toInt().coerceIn(0, height - 10)
                        val cropH = ((height * endYRatio).toInt() - cropY).coerceIn(10, height - cropY)

                        if (cropW > 30 && cropH > 15) {
                            val rawSlotCrop = Bitmap.createBitmap(bitmap, cropX, cropY, cropW, cropH)
                            val preprocBitmap = SlotImagePreProcessor.preprocessSlotTextRegion(
                                rawSlotCrop,
                                isAlly = false,
                                suppressLeadingMasteryIcon = false
                            )
                            if (preprocBitmap != null) {
                                val inputImg = com.google.mlkit.vision.common.InputImage.fromBitmap(preprocBitmap, 0)
                                val task = recognizer.process(inputImg)
                                val preprocRes = com.google.android.gms.tasks.Tasks.await(task, 400, java.util.concurrent.TimeUnit.MILLISECONDS)
                                for (blk in preprocRes.textBlocks) {
                                    for (ln in blk.lines) {
                                        val lnTxt = ln.text.trim()
                                        val strippedLn = DraftValidationLayer.stripLeadingMasteryOrRoleIcon(lnTxt)
                                        val cMatched = ChampionNameResolver.findChampionInText(strippedLn, allChamps)
                                            ?: ChampionNameResolver.findChampionInText(lnTxt, allChamps)
                                        if (cMatched != null) {
                                            if (currentAllyChampIds.contains(cMatched.id)) {
                                                AppLogger.d(TAG, "OCR Rival Preproc Slot $i -> Ignorado ${cMatched.name}: ya seleccionado por el equipo aliado")
                                            } else {
                                                detectedEnemyChamp = cMatched
                                                AppLogger.d(TAG, "OCR Rival Slot $i -> Campeón detectado con Pre-Procesamiento: ${cMatched.name}")
                                                break
                                            }
                                        }
                                    }
                                    if (detectedEnemyChamp != null) break
                                }
                                if (!preprocBitmap.isRecycled) preprocBitmap.recycle()
                            }
                            if (!rawSlotCrop.isRecycled) rawSlotCrop.recycle()
                        }
                    } catch (e: Throwable) {
                        AppLogger.d(TAG, "Pre-procesamiento slot rival $i skipped: ${e.message}")
                    }
                }

                // REGLAS ESTRICTAS DEL USUARIO:
                // Si el OCR detecta un campeón en el slot rival, se confirma al 100% y se guarda en memoria.
                // Si en fotogramas posteriores no se detecta nuevo texto, se MANTIENE intacto el campeón ya confirmado.
                // Si el campeón pertenece al equipo aliado, jamás se asigna al rival.
                if (detectedEnemyChamp != null && !currentAllyChampIds.contains(detectedEnemyChamp.id)) {
                    enemySlotConfirmedChampions[i] = detectedEnemyChamp
                    enemyOcrChampions[i] = detectedEnemyChamp
                    enemySlots[i].champion = detectedEnemyChamp
                    enemySlots[i].confidencePercent = 100
                    enemySlots[i].isLikelyUnpicked = false
                } else if (enemySlotConfirmedChampions[i] != null) {
                    val existingEnemy = enemySlotConfirmedChampions[i]
                    if (existingEnemy != null && !currentAllyChampIds.contains(existingEnemy.id)) {
                        enemyOcrChampions[i] = existingEnemy
                        enemySlots[i].champion = existingEnemy
                        enemySlots[i].confidencePercent = 100
                        enemySlots[i].isLikelyUnpicked = false
                    } else {
                        enemySlotConfirmedChampions[i] = null
                        enemyOcrChampions[i] = null
                        enemySlots[i].champion = null
                        enemySlots[i].confidencePercent = 0
                        enemySlots[i].isLikelyUnpicked = true
                    }
                } else {
                    enemySlotConfirmedChampions[i] = null
                    enemyOcrChampions[i] = null
                    enemySlots[i].champion = null
                    enemySlots[i].confidencePercent = 0
                    enemySlots[i].isLikelyUnpicked = true
                }
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error durante el análisis OCR", e)
        }

        // -----------------------------------------------------------------------------------------
        // PASO 2: ASIGNACIÓN DE ROLES EXPLÍCITOS (Solo cuando hay texto comprobado)
        // -----------------------------------------------------------------------------------------
        for (i in 0..4) {
            val slot = allySlots[i]
            if (slot.explicitRole == null) {
                slot.explicitRole = allySlotRolesCache[i]
            }
        }
        // No forzamos roles naturales aquí, DraftValidationLayer se encarga de usar el índice del slot si hace falta.

        // -----------------------------------------------------------------------------------------
        // PASO 3: EVALUACIÓN DE SLOTS Y DIAGNÓSTICO EN TIEMPO REAL (100% BASADO EN OCR Y ROLES)
        // -----------------------------------------------------------------------------------------
        val avatarDiameter = (height * calib.avatarDiameterRatio).toInt().coerceAtLeast(32)
        val allyAvatarCenterX = (width * calib.allyAvatarCenterX).toInt()
        val enemyAvatarCenterX = (width * calib.enemyAvatarCenterX).toInt()
        val allySlotYRatios = calib.allySlotYRatios
        val enemySlotYRatios = calib.enemySlotYRatios
        val diagnosticsList = mutableListOf<SlotDiagnostic>()

        // 3.1 Aliados: Si se detectó el nombre del campeón en el slot, se asocia directamente a la línea memorizada de ese slot
        for (i in 0..4) {
            val slot = allySlots[i]
            val yCenter = (height * allySlotYRatios[i]).toInt()
            val startX = (allyAvatarCenterX - avatarDiameter / 2).coerceIn(0, width - avatarDiameter)
            val startY = (yCenter - avatarDiameter / 2).coerceIn(0, height - avatarDiameter)
            val roiRect = Rect(startX, startY, startX + avatarDiameter, startY + avatarDiameter)

            val ocrChamp = allyOcrChampions[i]
            val roleForSlot = allySlotRolesCache[i] ?: defaultRolesList[i]
            val isUnpicked = (slot.champion == null && allySlotConfirmedChampions[i] == null)

            val finalChamp = allySlotFilters[i].process(ocrChamp, isUnpicked = isUnpicked, persistentCache = allySlotConfirmedChampions[i])
            
            if (finalChamp != null) {
                allySlotConfirmedChampions[i] = finalChamp
                slot.champion = finalChamp
                slot.confidencePercent = 100
                slot.explicitRole = roleForSlot
                slot.assignedRole = roleForSlot
            } else {
                slot.champion = null
                slot.confidencePercent = 0
                slot.explicitRole = roleForSlot
            }

            val diagStatus = if (finalChamp != null) DiagnosticStatus.CONFIRMADO else DiagnosticStatus.VACIO
            val diagReason = if (finalChamp != null) {
                "Campeón confirmado por nombre OCR: ${finalChamp.name} -> ${roleForSlot.shortName}"
            } else {
                "Esperando selección en carril ${roleForSlot.shortName} (${allySummonerNamesCache[i] ?: "Invocador"})"
            }

            val diagnostic = SlotDiagnostic(
                slotIndex = i,
                isAlly = true,
                roiRect = roiRect,
                candidate1 = finalChamp,
                score1 = if (finalChamp != null) 1.0f else 0.0f,
                candidate2 = null,
                score2 = 0f,
                margin = if (finalChamp != null) 1.0f else 0f,
                ocrChampion = ocrChamp,
                finalChampion = finalChamp,
                status = diagStatus,
                reason = diagReason
            )
            diagnosticsList.add(diagnostic)
            AppLogger.d(TAG, diagnostic.toFormattedString())
        }

        // 3.2 Rivales: Se detecta el nombre del campeón cuando desaparece 'Jugador X'
        for (i in 0..4) {
            val slot = enemySlots[i]
            val yCenter = (height * enemySlotYRatios[i]).toInt()
            val startX = (enemyAvatarCenterX - avatarDiameter / 2).coerceIn(0, width - avatarDiameter)
            val startY = (yCenter - avatarDiameter / 2).coerceIn(0, height - avatarDiameter)
            val roiRect = Rect(startX, startY, startX + avatarDiameter, startY + avatarDiameter)

            val ocrChamp = enemyOcrChampions[i]
            val isUnpicked = (slot.champion == null && enemySlotConfirmedChampions[i] == null)

            val finalChamp = enemySlotFilters[i].process(ocrChamp, isUnpicked = isUnpicked, persistentCache = enemySlotConfirmedChampions[i])

            if (finalChamp != null) {
                enemySlotConfirmedChampions[i] = finalChamp
                slot.champion = finalChamp
                slot.confidencePercent = 100
            } else {
                slot.champion = null
                slot.confidencePercent = 0
            }

            val diagStatus = if (finalChamp != null) DiagnosticStatus.CONFIRMADO else DiagnosticStatus.VACIO
            val diagReason = if (finalChamp != null) {
                "Campeón rival confirmado por OCR: ${finalChamp.name}"
            } else {
                "Slot rival esperando selección (Jugador ${i + 1})"
            }

            val diagnostic = SlotDiagnostic(
                slotIndex = i,
                isAlly = false,
                roiRect = roiRect,
                candidate1 = finalChamp,
                score1 = if (finalChamp != null) 1.0f else 0.0f,
                candidate2 = null,
                score2 = 0f,
                margin = if (finalChamp != null) 1.0f else 0f,
                ocrChampion = ocrChamp,
                finalChampion = finalChamp,
                status = diagStatus,
                reason = diagReason
            )
            diagnosticsList.add(diagnostic)
            AppLogger.d(TAG, diagnostic.toFormattedString())
        }

        // -----------------------------------------------------------------------------------------
        // PASO 3.3: ESCANEO DE HECHIZOS DE INVOCADOR ALIADOS (SUMMONER SPELLS)
        // -----------------------------------------------------------------------------------------
        // En Wild Rift, los hechizos aliados se sitúan en el extremo izquierdo de la pantalla:
        val spellSize = (height * calib.spellSizeRatio).toInt().coerceAtLeast(18)
        val spellLeft = (width * calib.spellLeftRatio).toInt().coerceIn(0, width - spellSize)
        val spellRight = spellLeft + spellSize

        val allySpellsMap = mutableMapOf<Int, MutableList<String>>()
        val detectedSpellsList = mutableListOf<com.example.util.SummonerSpellDetector.SpellMatch>()

        for (i in 0..4) {
            val yCenter = (height * (allySlotYRatios[i] + calib.spellYOffsetRatio)).toInt()

            val spell1Top = (yCenter - spellSize - (height * 0.003f).toInt()).coerceIn(0, height - spellSize)
            val spell1Bottom = spell1Top + spellSize
            val spell2Top = (yCenter + (height * 0.003f).toInt()).coerceIn(0, height - spellSize)
            val spell2Bottom = spell2Top + spellSize

            val allyCandidateRects = listOf(
                // Hechizo 1 (arriba)
                Rect(spellLeft, spell1Top, spellRight, spell1Bottom),
                // Hechizo 2 (abajo)
                Rect(spellLeft, spell2Top, spellRight, spell2Bottom)
            )

            val allySlotSpells = mutableListOf<String>()
            for (r in allyCandidateRects) {
                if (allySlotSpells.size >= 2) break
                try {
                    val crop = Bitmap.createBitmap(bitmap, r.left, r.top, r.width(), r.height())
                    val match = SummonerSpellDetector.detectSpell(crop, r)
                    crop.recycle()
                    if (match != null && !allySlotSpells.contains(match.spellName)) {
                        allySlotSpells.add(match.spellName)
                        detectedSpellsList.add(match)
                    }
                } catch (_: Exception) {}
            }
            if (allySlotSpells.isNotEmpty()) {
                allySpellsMap[i] = allySlotSpells
            }
            allySlots[i].summonerSpells = allySlotSpells
        }

        // -----------------------------------------------------------------------------------------
        // PASO 3.4: RECONOCIMIENTO VISUAL INTELIGENTE DEL 10º PICK (ÚLTIMO PICK DEL DRAFT)
        // En Wild Rift, cuando el último jugador selecciona su campeón, la partida transiciona
        // inmediatamente a la pantalla de carga del juego, por lo que el nombre textual desaparece
        // y el OCR no puede leerlo.
        // La secuencia de selección según el orden de Draft:
        // - Si el Equipo Aliado es Primer Pick (1A -> 2E -> 2A -> 2E -> 2A -> 1E):
        //   El 10º pick es RIVAL (el último campeón enemigo). Se aplica reconocimiento visual al slot rival restante.
        // - Si el Equipo Aliado es Segundo Pick (1E -> 2A -> 2E -> 2A -> 2E -> 1A):
        //   El 10º pick es ALIADO (el último campeón aliado). Se aplica reconocimiento visual al slot aliado restante.
        // -----------------------------------------------------------------------------------------
        val totalAllyOcr = allySlots.count { it.champion != null }
        val totalEnemyOcr = enemySlots.count { it.champion != null }

        // Inferencia determinista de Primera Selección según la regla exacta del usuario:
        // Si el equipo aliado selecciona primero (Slot 0 aliado) -> Primera Selección (true)
        // Si el equipo rival selecciona primero (Slot 0 rival) -> Segunda Selección (false)
        if (allySlots[0].champion != null && enemySlots[0].champion == null) {
            detectedFirstPick = true
            AppLogger.d(TAG, "Inferencia First Pick: Aliados seleccionaron en Slot 0 primero -> Primera Selección (true)")
        } else if (enemySlots[0].champion != null && allySlots[0].champion == null) {
            detectedFirstPick = false
            AppLogger.d(TAG, "Inferencia First Pick: Rival seleccionó en Slot 0 primero -> Segunda Selección (false)")
        } else if (totalAllyOcr > 0 && totalEnemyOcr == 0) {
            detectedFirstPick = true
            AppLogger.d(TAG, "Inferencia First Pick: Aliados tienen $totalAllyOcr picks y Rival 0 -> Primera Selección (true)")
        } else if (totalEnemyOcr > 0 && totalAllyOcr == 0) {
            detectedFirstPick = false
            AppLogger.d(TAG, "Inferencia First Pick: Rival tiene $totalEnemyOcr picks y Aliados 0 -> Segunda Selección (false)")
        } else if (detectedFirstPick == null) {
            when {
                totalAllyOcr == 1 && totalEnemyOcr == 2 -> detectedFirstPick = true
                totalEnemyOcr == 1 && totalAllyOcr == 2 -> detectedFirstPick = false
                totalAllyOcr == 3 && totalEnemyOcr == 2 -> detectedFirstPick = true
                totalAllyOcr == 2 && totalEnemyOcr == 3 -> detectedFirstPick = false
                totalAllyOcr == 3 && totalEnemyOcr == 4 -> detectedFirstPick = true
                totalEnemyOcr == 3 && totalAllyOcr == 4 -> detectedFirstPick = false
                totalAllyOcr == 5 && totalEnemyOcr == 4 -> detectedFirstPick = true
                totalEnemyOcr == 5 && totalAllyOcr == 4 -> detectedFirstPick = false
            }
        }

        val effectiveFirstPick = detectedFirstPick ?: currentIsFirstPick ?: false
        val pickSequence = getDraftPickSequence(effectiveFirstPick)

        debugVisualMatches.value = emptyMap()

        // -----------------------------------------------------------------------------------------
        // PASO 3.5: MOTOR GOOGLE MEDIAPIPE / LITER TENSOR CLASSIFIER PARA EL 10º PICK
        // REGLA CRÍTICA: Se activa EXCLUSIVAMENTE cuando las selecciones del 1 al 9 ya están confirmadas.
        // Google MediaPipe / LiteRT se encarga de realizar las comparaciones de tensores,
        // decide de forma autónoma quién es el 10º pick y lo selecciona en el draft.
        // -----------------------------------------------------------------------------------------
        val confirmedPicksCount = allySlots.count { it.champion != null } + enemySlots.count { it.champion != null }
        val tenthTurn = pickSequence.last()
        val tenthIsAlly = tenthTurn.isAlly
        val tenthSlotIndex = tenthTurn.slotIndex

        // Localizar el slot exacto correspondiente al 10º pick:
        // Si hay 9 selecciones confirmadas en total, el 10º pick es determinísticamente el único slot
        // restante entre aliados y rivales que aún no tiene campeón asignado (champion == null).
        val unpickedAlly = allySlots.firstOrNull { it.champion == null }
        val unpickedEnemy = enemySlots.firstOrNull { it.champion == null }

        val actualTenthIsAlly = when {
            unpickedAlly != null && unpickedEnemy == null -> true
            unpickedEnemy != null && unpickedAlly == null -> false
            else -> tenthIsAlly
        }
        val actualTenthSlotIndex = when {
            unpickedAlly != null && unpickedEnemy == null -> unpickedAlly.slotIndex
            unpickedEnemy != null && unpickedAlly == null -> unpickedEnemy.slotIndex
            else -> tenthSlotIndex
        }

        val confirmedChampIds = (allySlots.mapNotNull { it.champion?.id } + enemySlots.mapNotNull { it.champion?.id }).toSet()

        // Extraer SIEMPRE el recorte del avatar del slot del 10º pick para garantizar que el Visor
        // y el motor LiteRT tengan la imagen disponible en tiempo real en todo momento durante el draft
        val tenthCrop: Bitmap? = try {
            AdaptiveScreenLayoutEngine.extractSlotAvatarBitmap(
                sourceBitmap = bitmap,
                width = width,
                height = height,
                isAlly = actualTenthIsAlly,
                slotIndex = actualTenthSlotIndex,
                config = calib
            )
        } catch (_: Throwable) { null }

        // REGLA CRÍTICA DEL USUARIO:
        // "únicamente la Selección del décimo pick este pues de haber seleccionado las otras 9"
        // "el escaneo del décimo pick tienes que apuntar al slot final se ve con el yelmo espartano o icono de línea"
        // Google MediaPipe / LiteRT se activa exclusivamente cuando las selecciones 1 al 9 ya están presentes
        // y apunta estrictamente al círculo del slot correspondiente.
        val targetSlot = if (actualTenthIsAlly) allySlots[actualTenthSlotIndex] else enemySlots[actualTenthSlotIndex]
        if (confirmedPicksCount == 9 && targetSlot.champion == null) {
            val liteRTDecision = LiteRTVisionClassifier.executeTenthPickInference(
                cropBitmap = tenthCrop,
                isAlly = actualTenthIsAlly,
                confirmedChampionIds = confirmedChampIds,
                confirmedPicksCount = confirmedPicksCount,
                slotIndex = actualTenthSlotIndex,
                context = context
            )

            if (liteRTDecision != null) {
                val (champWinner, confidence) = liteRTDecision
                if (actualTenthIsAlly) {
                    allySlots[actualTenthSlotIndex].champion = champWinner
                    allySlots[actualTenthSlotIndex].confidencePercent = confidence
                    allySlots[actualTenthSlotIndex].isLikelyUnpicked = false
                    allySlotConfirmedChampions[actualTenthSlotIndex] = champWinner
                } else {
                    enemySlots[actualTenthSlotIndex].champion = champWinner
                    enemySlots[actualTenthSlotIndex].confidencePercent = confidence
                    enemySlots[actualTenthSlotIndex].isLikelyUnpicked = false
                    enemySlotConfirmedChampions[actualTenthSlotIndex] = champWinner
                }
                AppLogger.d(TAG, "Google MediaPipe / LiteRT decidió el 10º Pick -> ${champWinner.name} ($confidence%)")
            }
        } else {
            // Notificar al motor LiteRT suministrando el recorte capturado para que el Visor informe en vivo
            LiteRTVisionClassifier.executeTenthPickInference(
                cropBitmap = tenthCrop,
                isAlly = actualTenthIsAlly,
                confirmedChampionIds = confirmedChampIds,
                confirmedPicksCount = confirmedPicksCount,
                slotIndex = actualTenthSlotIndex,
                context = context
            )
        }

        try { tenthCrop?.recycle() } catch (_: Throwable) {}
        
        // 4.1 Aliados: Cada slot aliado (0..4) mapea determinísticamente a su carril (allySlotRolesCache)
        val alliesMap = mutableMapOf<LaneRole, Champion>()
        val allyConfidences = mutableMapOf<LaneRole, Int>()

        for (i in 0..4) {
            val slot = allySlots[i]
            val role = allySlotRolesCache[i] ?: slot.explicitRole ?: slot.assignedRole ?: defaultRolesList[i]
            slot.assignedRole = role
            slot.explicitRole = role
            val champ = slot.champion
            if (champ != null) {
                alliesMap[role] = champ
                allyConfidences[role] = slot.confidencePercent.coerceIn(1, 100)
                AppLogger.d(TAG, "Aliado Slot $i (${role.shortName}) -> ${champ.name}")
            }
        }

        // 4.2 Enemigos: Asignación validada por roles primarios y secundarios de los picks seleccionados
        val standardRoles = listOf(LaneRole.TOP, LaneRole.JUNGLE, LaneRole.MID, LaneRole.ADC, LaneRole.SUPPORT)
        val validEnemySlots = enemySlots.filter { it.champion != null }
        val enemyResolved = DraftValidationLayer.resolveTeamRolesDetailed(validEnemySlots, allChamps, auditList, isAllyTeam = false)
        val enemiesMap = enemyResolved.assignments.toMutableMap()

        val assignedEnemyChamps = enemiesMap.values.map { it.id }.toSet()
        for (slot in validEnemySlots) {
            val champ = slot.champion ?: continue
            if (!assignedEnemyChamps.contains(champ.id)) {
                val availableRoles = standardRoles.filter { !enemiesMap.containsKey(it) }
                val targetRole = availableRoles.firstOrNull() ?: defaultRolesList.getOrNull(slot.slotIndex) ?: LaneRole.MID
                enemiesMap[targetRole] = champ
                slot.assignedRole = targetRole
                AppLogger.d(TAG, "Rival ${champ.name} preservado y asignado a ${targetRole.shortName}")
            }
        }

        // Deduplicación: Un campeón aliado jamás puede aparecer en el equipo enemigo
        val allyChampIds = alliesMap.values.map { it.id }.toSet()
        val finalEnemiesMap = enemiesMap.filterNot { allyChampIds.contains(it.value.id) }
        val enemyConfidences = enemyResolved.confidences.filterKeys { finalEnemiesMap.containsKey(it) }

        // Mapear nombres de invocador y hechizos al rol final asignado (o rol por defecto del slot)
        val allySummonerNamesByRole = mutableMapOf<LaneRole, String>()
        val allySpellsByRole = mutableMapOf<LaneRole, List<String>>()

        for (i in 0..4) {
            val slot = allySlots[i]
            val role = slot.assignedRole ?: slot.explicitRole ?: defaultRolesList.getOrNull(i)
            if (role != null) {
                val sName = allySummonerNamesCache[i]
                if (!sName.isNullOrBlank()) {
                    allySummonerNamesByRole[role] = sName
                }
                val sp = allySpellsMap[i]
                if (!sp.isNullOrEmpty()) {
                    allySpellsByRole[role] = sp
                }
            }
        }

        val alliesBySlotMap = allySlots.mapNotNull { s -> s.champion?.let { s.slotIndex to it } }.toMap()
        val enemiesBySlotMap = enemySlots.mapNotNull { s ->
            val champ = s.champion
            if (champ != null && !allyChampIds.contains(champ.id)) {
                s.slotIndex to champ
            } else null
        }.toMap()

        val allyChampsList = alliesMap.values.toList()
        val enemyChampsList = finalEnemiesMap.values.toList()
        val total = allyChampsList.size + enemyChampsList.size

        val hasDraftActivity = total > 0 || allySummonerNamesCache.isNotEmpty() || userDetectedLane != null || detectedFirstPick != null || isLegendaryRanked || isPreparationPhase

        val statusMsg = when {
            isLegendaryRanked && total == 0 -> "Clasificatoria Legendaria (Nombres anónimos)"
            isLegendaryRanked -> "Clasificatoria Legendaria • $total picks detectados"
            total == 0 && allySummonerNamesCache.isNotEmpty() -> "Invocadores aliados detectados (${allySummonerNamesCache.size}/5)"
            total == 0 -> "Esperando selección en directo..."
            total == 10 -> {
                val tenthChamp = if (actualTenthIsAlly) allySlots[actualTenthSlotIndex].champion else enemySlots[actualTenthSlotIndex].champion
                if (tenthChamp != null) {
                    "10/10 Completo • 10º Pick por LiteRT (${tenthChamp.name})"
                } else {
                    "10/10 Completo • Selección finalizada"
                }
            }
            total in 1..9 -> "$total/10 picks detectados con certeza"
            auditList.isNotEmpty() -> "Detectados: $total picks (${auditList.size} adaptaciones)"
            else -> "Detectados: $total picks con certeza"
        }

        return DraftScanResult(
            allies = allyChampsList,
            enemies = enemyChampsList,
            alliesBySlot = alliesBySlotMap,
            enemiesBySlot = enemiesBySlotMap,
            alliesByRole = alliesMap,
            enemiesByRole = finalEnemiesMap,
            enemyConfidencesByRole = enemyConfidences,
            detectedRole = userDetectedLane,
            userExplicitlyDetectedRole = if (userExplicitlyConfirmed) userDetectedLane else null,
            detectedFirstPick = detectedFirstPick,
            detectedRawWords = detectedWords,
            discrepancies = auditList,
            diagnostics = diagnosticsList,
            allySummonerNamesBySlot = allySummonerNamesCache.toMap(),
            allySpellsBySlot = allySpellsMap.mapValues { it.value.toList() },
            enemySpellsBySlot = emptyMap(),
            allySummonerNamesByRole = allySummonerNamesByRole,
            allySpellsByRole = allySpellsByRole,
            isLegendaryRanked = isLegendaryRanked,
            isPreparationPhase = isPreparationPhase,
            hasDraftActivity = hasDraftActivity,
            isSuccessful = hasDraftActivity,
            statusMessage = statusMsg
        )
        } catch (t: Throwable) {
            AppLogger.e(TAG, "Excepción no controlada en scanDraftFromBitmap prevenida", t)
            DraftScanResult(emptyList(), emptyList(), isSuccessful = false, statusMessage = "Error en escaneo")
        }
    }
}
