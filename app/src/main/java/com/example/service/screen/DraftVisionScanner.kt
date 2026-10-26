package com.example.service.screen

import android.content.Context
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    val isLastPickFinalized: Boolean = false,
    val lastPickChampion: Champion? = null,
    val tenthPickIsAlly: Boolean? = null,
    val tenthPickSlotIndex: Int? = null,
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
    val statusMessage: String,
    val allyRolesBySlot: Map<Int, LaneRole> = emptyMap()
)

object DraftVisionScanner {
    private const val TAG = "DraftVisionScanner"
    @Volatile private var overlayMasks = CaptureMaskSnapshot(emptyList())
    @Volatile private var diagnosticMasks = CaptureMaskSnapshot(emptyList())
    var overlayRect: Rect?
        get() = overlayMasks.rects.firstOrNull()?.let { Rect(it) }
        set(value) { overlayMasks = CaptureMaskSnapshot(listOfNotNull(value?.let { Rect(it) })) }
    fun publishScreenOverlay(rect: Rect, screenWidth: Int, screenHeight: Int) {
        overlayMasks = CaptureMaskSnapshot(listOf(Rect(rect)), screenWidth, screenHeight)
    }
    fun publishScreenDiagnosticText(rects: List<Rect>, screenWidth: Int, screenHeight: Int) {
        diagnosticMasks = CaptureMaskSnapshot(rects.map { Rect(it) }, screenWidth, screenHeight)
    }
    internal fun overlayForFrame(width: Int, height: Int): Rect? = overlayMasks.forFrame(width, height).firstOrNull()
    internal fun diagnosticTextForFrame(width: Int, height: Int): List<Rect> = diagnosticMasks.forFrame(width, height)
    val showCalibrationBoxes = kotlinx.coroutines.flow.MutableStateFlow(false)
    val debugVisualMatches = kotlinx.coroutines.flow.MutableStateFlow<Map<String, String>>(emptyMap())
    val allyRolesBySlotFlow = kotlinx.coroutines.flow.MutableStateFlow<Map<Int, LaneRole>>(emptyMap())
    var debugTextRects: List<Rect>
        get() = diagnosticMasks.rects.map { Rect(it) }
        set(value) { diagnosticMasks = CaptureMaskSnapshot(value.map { Rect(it) }) }

    val isVisionEngineBusy = kotlinx.coroutines.flow.MutableStateFlow(false)
    val liveScanFps = kotlinx.coroutines.flow.MutableStateFlow(0f)
    val framesSkippedCount = kotlinx.coroutines.flow.MutableStateFlow(0L)
    val lastProcessingDurationMs = kotlinx.coroutines.flow.MutableStateFlow(0L)
    val isFirstPickState = kotlinx.coroutines.flow.MutableStateFlow(false)

    val allySlotRolesCache = mutableMapOf<Int, LaneRole>()
    val allySlotOcrLaneCache = mutableMapOf<Int, LaneRole>()
    private val bannedPortraitScanner = BannedPortraitScanner()
    internal val banDetectionActiveFlow = kotlinx.coroutines.flow.MutableStateFlow(false)
    internal val banSlotsFlow = kotlinx.coroutines.flow.MutableStateFlow(emptyBanSlots())
    val bannedChampionIdsFlow = kotlinx.coroutines.flow.MutableStateFlow<Set<String>>(emptySet())
    val detectedBannedChampionIds: Set<String> get() = bannedChampionIdsFlow.value
    private val banPublicationLock = Any()
    @Volatile private var banGeneration = 0L

    internal fun captureBanFrame(bitmap: Bitmap, pickedIds: Set<String>): DraftBanFrame = synchronized(banPublicationLock) {
        banDetectionActiveFlow.value=true
        DraftBanFrame(bitmap.copy(Bitmap.Config.ARGB_8888,false) ?: error("Ban frame unavailable"),
            diagnosticTextForFrame(bitmap.width,bitmap.height)+listOfNotNull(overlayForFrame(bitmap.width,bitmap.height)),
            configForFrame(bitmap),
            pickedIds,banGeneration)
    }

    internal fun processBanFrame(frame: DraftBanFrame, context: Context, canPublish: () -> Boolean) {
        if(!synchronized(banPublicationLock) { frame.generation==banGeneration } || !canPublish()) return
        val ids = bannedPortraitScanner.scan(frame.bitmap,context,android.os.SystemClock.elapsedRealtime(),
            frame.masks,frame.config,frame.pickedIds) { frame.generation==banGeneration && canPublish() }
        synchronized(banPublicationLock) {
            if(frame.generation!=banGeneration || !canPublish()) return
            banDetectionActiveFlow.value=true
            bannedChampionIdsFlow.value=ids
            banSlotsFlow.value=bannedPortraitScanner.slots
        }
    }

    fun clearDetectedBans() = synchronized(banPublicationLock) {
        banGeneration++
        banDetectionActiveFlow.value = false
        bannedPortraitScanner.reset()
        banSlotsFlow.value = emptyBanSlots()
        bannedChampionIdsFlow.value = emptySet()
    }

    fun recordFrameSkipped() {
        framesSkippedCount.value = framesSkippedCount.value + 1L
    }

    fun recordFrameProcessed(durationMs: Long) {
        lastProcessingDurationMs.value = durationMs
    }

    fun updateFps(fps: Float) {
        liveScanFps.value = fps
    }

    internal fun preferFullScreenAllyChampion(fullScreenChampion: Champion?, targetedChampion: Champion?): Champion? =
        fullScreenChampion ?: targetedChampion

    fun computeActiveSelectionTurns(
        sequence: List<DraftPickTurn>,
        allies: Array<Champion?>,
        enemies: Array<Champion?>
    ): List<DraftPickTurn> {
        val active = mutableListOf<DraftPickTurn>()
        val totalConfirmed = allies.count { it != null } + enemies.count { it != null }

        for (turn in sequence) {
            val isPicked = if (turn.isAlly) {
                allies.getOrNull(turn.slotIndex) != null
            } else {
                enemies.getOrNull(turn.slotIndex) != null
            }
            if (!isPicked) {
                active.add(turn)
                break
            }
        }

        // Si ya hay 7 o más selecciones confirmadas en total y el 10º pick no ha sido confirmado,
        // incluir explícitamente el 10º turno en activeTurns para que el escaneo dirigido lo procese
        val tenthTurn = sequence.lastOrNull()
        if (tenthTurn != null && totalConfirmed >= 7) {
            val isTenthPicked = if (tenthTurn.isAlly) {
                allies.getOrNull(tenthTurn.slotIndex) != null
            } else {
                enemies.getOrNull(tenthTurn.slotIndex) != null
            }
            if (!isTenthPicked && !active.any { it.turnNumber == tenthTurn.turnNumber }) {
                active.add(tenthTurn)
            }
        }

        return active
    }

    suspend fun scanActiveSlotDirectly(bitmap: Bitmap, turn: DraftPickTurn, context: Context? = null, confirmedHudPicks: List<Champion> = emptyList()): ScannedSlotInfo? {
        if (turn.turnNumber == 10) {
            val w = bitmap.width
            val h = bitmap.height
            val calib = configForFrame(bitmap)
            val crop = AdaptiveScreenLayoutEngine.extractSlotAvatarBitmap(
                sourceBitmap = bitmap,
                width = w,
                height = h,
                isAlly = turn.isAlly,
                slotIndex = turn.slotIndex,
                config = calib
            ) ?: return null

            val otherPicks = getConfirmedPicksExcept(turn, confirmedHudPicks)
            val confirmedChampIds = otherPicks.map { it.id }.toSet() + detectedBannedChampionIds
            val targetChampion = if (turn.isAlly) {
                allySlotConfirmedChampions.getOrNull(turn.slotIndex)
                    ?.takeIf { allySlotNameConfirmed[turn.slotIndex] }
            } else {
                enemySlotConfirmedChampions.getOrNull(turn.slotIndex)
                    ?.takeIf { enemySlotNameConfirmed[turn.slotIndex] }
            }

            val decision = LiteRTVisionClassifier.executeTenthPickInference(
                cropBitmap = crop,
                isAlly = turn.isAlly,
                confirmedChampionIds = confirmedChampIds,
                confirmedPicksCount = otherPicks.size,
                slotIndex = turn.slotIndex,
                context = context,
                confirmedTargetChampion = targetChampion
            )
            try { crop.recycle() } catch (_: Throwable) {}

            if (decision != null) {
                val (champ, conf) = decision
                lastVisualPick = turn.isAlly to champ
                return ScannedSlotInfo(
                    slotIndex = turn.slotIndex,
                    isAlly = turn.isAlly,
                    champion = champ,
                    confidencePercent = conf,
                    isLikelyUnpicked = false
                )
            }
        }
        return null
    }

    internal fun getConfirmedPicksExcept(turn: DraftPickTurn, confirmedHudPicks: List<Champion> = emptyList()): List<Champion> {
        val target = (if (turn.isAlly) allySlotConfirmedChampions else enemySlotConfirmedChampions).getOrNull(turn.slotIndex)
        val remembered = allySlotConfirmedChampions.filterIndexed { index, _ ->
            !turn.isAlly || index != turn.slotIndex
        }.filterNotNull() + enemySlotConfirmedChampions.filterIndexed { index, _ ->
            turn.isAlly || index != turn.slotIndex
        }.filterNotNull()
        // Only a complete, unique set of nine HUD picks can recover a transient OCR gap.
        val hud = confirmedHudPicks.takeIf { it.size == 9 && it.map { c -> c.id }.distinct().size == 9 }.orEmpty()
        val uniqueRemembered = remembered.filter { it.id != target?.id }.distinctBy { it.id }
        // An old HUD and a current OCR reading are alternative snapshots, not extra picks.
        // In particular a changed preview must never inflate the count beyond nine.
        if (uniqueRemembered.size == 9) return uniqueRemembered
        val compatibleHud = hud.filter { it.id != target?.id }
        return if (compatibleHud.size == 9 && uniqueRemembered.all { c -> compatibleHud.any { it.id == c.id } })
            compatibleHud else uniqueRemembered
    }

    internal fun getRememberedAllyRoles(currentRoles: Map<Int, LaneRole>): Map<Int, LaneRole> {
        return AllyDraftReconciler.rememberedRoles(currentRoles, allySlotOcrLaneCache)
    }

    fun getAllySlotRole(slotIndex: Int): LaneRole {
        return allySlotOcrLaneCache[slotIndex]
            ?: allySlotRolesCache[slotIndex]
            ?: allySlotConfirmedChampions.getOrNull(slotIndex)?.primaryRole
            ?: when (slotIndex) {
                0 -> LaneRole.TOP
                1 -> LaneRole.JUNGLE
                2 -> LaneRole.MID
                3 -> LaneRole.ADC
                else -> LaneRole.SUPPORT
            }
    }

    
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

    val alignedConfigFlow = kotlinx.coroutines.flow.MutableStateFlow(VisionCalibrationConfig())
    private var alignedBase: VisionCalibrationConfig? = null
    @Volatile private var alignedSize = 0 to 0
    internal data class CalibrationReportSnapshot(val width: Int, val height: Int, val config: VisionCalibrationConfig)
    @Synchronized
    internal fun calibrationReportSnapshot() = CalibrationReportSnapshot(alignedSize.first, alignedSize.second, alignedConfigFlow.value)
    private var lastAlignmentAt = 0L
    private var calibrationContext: android.content.Context? = null
    private var activeCaptureProfile: String? = null
    private var globalCalibrationConfig = VisionCalibrationConfig()
    @Synchronized
    internal fun configForFrame(bitmap: Bitmap): VisionCalibrationConfig {
        val profile = VisionCalibrationConfig.captureProfileKey(bitmap.width, bitmap.height)
        if (profile != activeCaptureProfile) {
            val profileConfig = calibrationContext?.let { context ->
                VisionCalibrationConfig.loadProfileFromPrefs(context, profile)
            }
            val hasProfiles = calibrationContext?.let { VisionCalibrationConfig.hasCaptureProfiles(it) } ?: false
            val selected = profileConfig ?: if (activeCaptureProfile == null && !hasProfiles) globalCalibrationConfig
                else VisionCalibrationConfig.defaultsForDevice(android.os.Build.MANUFACTURER, android.os.Build.MODEL)
            calibrationConfig = selected
            calibrationConfigFlow.value = selected
            alignedConfigFlow.value = selected
            activeCaptureProfile = profile
            alignedBase = null
        }
        val defaults = VisionCalibrationConfig.defaultsForDevice(android.os.Build.MANUFACTURER, android.os.Build.MODEL)
        val manual = calibrationConfig.allySlotXRatios != defaults.allySlotXRatios ||
            calibrationConfig.enemySlotXRatios != defaults.enemySlotXRatios ||
            calibrationConfig.allySlotYRatios != defaults.allySlotYRatios ||
            calibrationConfig.enemySlotYRatios != defaults.enemySlotYRatios ||
            calibrationConfig.allySlotDiameterRatios != defaults.allySlotDiameterRatios ||
            calibrationConfig.enemySlotDiameterRatios != defaults.enemySlotDiameterRatios
        val now = android.os.SystemClock.elapsedRealtime()
        // Detect bars at the same cadence as ring alignment, not on every OCR pass.
        // Reinitialization invalidates the viewport too, even when capture dimensions
        // stay identical (full-screen HUD versus the same surface with outer bars).
        val refresh = alignedBase == null || alignedSize != (bitmap.width to bitmap.height) || lastAlignmentAt == 0L || now-lastAlignmentAt >= 2000
        if (refresh) activeViewport = DraftFrameGeometry.contentBounds(bitmap)
        val normalized = AdaptiveScreenLayoutEngine.computeAdaptiveConfig(bitmap.width, bitmap.height, calibrationConfig)
        val base = if (manual) normalized else DraftFrameGeometry.forViewport(normalized, activeViewport, bitmap.width, bitmap.height)
        if (alignedBase != base || alignedSize != (bitmap.width to bitmap.height)) {
            alignedConfigFlow.value = base
            alignedBase = base; alignedSize = bitmap.width to bitmap.height
            lastAlignmentAt = 0L
        }
        if (!manual && (lastAlignmentAt == 0L || now-lastAlignmentAt >= 2000)) {
            alignedConfigFlow.value = DraftRingAlignment.align(bitmap, alignedConfigFlow.value,
                overlayForFrame(bitmap.width, bitmap.height), showCalibrationBoxes.value)
            lastAlignmentAt = now
        }
        return alignedConfigFlow.value
    }
    private var activeViewport = android.graphics.Rect()
    @Volatile var calibrationConfig = VisionCalibrationConfig()
    val calibrationConfigFlow = kotlinx.coroutines.flow.MutableStateFlow(VisionCalibrationConfig())

    @Synchronized
    fun initCalibration(context: android.content.Context) {
        calibrationContext = context.applicationContext
        activeCaptureProfile = null
        calibrationConfig = VisionCalibrationConfig.loadFromPrefs(context)
        globalCalibrationConfig = calibrationConfig
        calibrationConfigFlow.value = calibrationConfig
        alignedConfigFlow.value = calibrationConfig
        alignedBase = null
    }

    @Synchronized
    fun updateCalibration(context: android.content.Context, newConfig: VisionCalibrationConfig) {
        val old = calibrationConfig
        if (old.allyBanXRatios != newConfig.allyBanXRatios || old.enemyBanXRatios != newConfig.enemyBanXRatios ||
            old.allyBanYRatio != newConfig.allyBanYRatio || old.enemyBanYRatio != newConfig.enemyBanYRatio ||
            old.banDiameterRatio != newConfig.banDiameterRatio || old.manualAllyBans != newConfig.manualAllyBans ||
            old.manualEnemyBans != newConfig.manualEnemyBans) {
            // Calibration moves the viewer regions, never revokes a confirmed ban.
            bannedPortraitScanner.recalibrate(newConfig)
            banSlotsFlow.value = bannedPortraitScanner.slots
        }
        calibrationConfig = newConfig
        globalCalibrationConfig = newConfig
        calibrationConfigFlow.value = newConfig
        alignedConfigFlow.value = newConfig
        alignedBase = null
        newConfig.saveToPrefs(context)
        activeCaptureProfile?.let { newConfig.saveProfileToPrefs(context, it) }
    }

    @Synchronized
    fun resetCalibration(context: android.content.Context) {
        calibrationConfig = VisionCalibrationConfig.resetToDefaults(context)
        globalCalibrationConfig = calibrationConfig
        calibrationConfigFlow.value = calibrationConfig
        alignedConfigFlow.value = calibrationConfig
        alignedBase = null
        activeCaptureProfile = null
    }

    private var recognizerInstance: com.google.mlkit.vision.text.TextRecognizer? = null
    private val recognizerWarmupMutex = Mutex()
    @Volatile private var recognizerWarmedUp = false

    // Memoria persistente de los nombres de invocador aliados (0..4)
    private val allySummonerNamesCache = mutableMapOf<Int, String>()
    // Memoria persistente del slot asignado al usuario
    private var cachedUserSlotIndex: Int? = null

    // Memoria persistente de campeones confirmados por slot para evitar que desaparezcan al terminar o transicionar
    val allySlotConfirmedChampions = arrayOfNulls<Champion>(5)
    val enemySlotConfirmedChampions = arrayOfNulls<Champion>(5)
    // Origen de la confirmación: solo el nombre del campeón leído por OCR puede bloquear un slot.
    // Las predicciones visuales del décimo pick deben volver a evaluarse en cada fotograma.
    private val allySlotNameConfirmed = BooleanArray(5)
    private val enemySlotNameConfirmed = BooleanArray(5)
    private val titleConfirmation = SlotTitleConfirmation()
    private var lastVisualPick: Pair<Boolean, Champion>? = null
    private val slotLifecycle = DraftSlotLifecycle()

    fun resetSlotLifecycle() = slotLifecycle.reset()

    fun observeSelectionSlots(bitmap: Bitmap, confirmedPicksCount: Int): DraftSlotLifecycle.Observation {
        if (bitmap.isRecycled) return DraftSlotLifecycle.Observation.DISAPPEARING
        if (bitmap.width < bitmap.height) return slotLifecycle.observe(false,confirmedPicksCount>=9)
        val config = configForFrame(bitmap)
        val visibleSlots = listOf(true, false).sumOf { isAlly ->
            (0..4).count { index ->
                val crop = AdaptiveScreenLayoutEngine.extractSlotAvatarBitmap(
                    bitmap, bitmap.width, bitmap.height, isAlly, index, config)
                try {
                    crop != null && !crop.isRecycled && LiteRTVisionClassifier.hasSlotRing(crop, isAlly)
                } finally { crop?.recycle() }
            }
        }
        // One circular decoration in the loading screen is not a visible draft.
        val visible = visibleSlots >= 3
        return slotLifecycle.observe(visible, confirmedPicksCount >= 9)
    }

    // Filtros de estabilización temporal (anti-parpadeo y anti-oscilación)
    private class SlotTemporalFilter {
        private var lastConfirmedChampion: Champion? = null

        fun process(candidate: Champion?, isUnpicked: Boolean, persistentCache: Champion?): Champion? {
            if (isUnpicked) {
                lastConfirmedChampion = null
                return null
            }
            val champ = candidate ?: persistentCache ?: lastConfirmedChampion
            if (champ != null) {
                lastConfirmedChampion = champ
                return champ
            }
            return null
        }

        fun reset() {
            lastConfirmedChampion = null
        }
    }

    private var observedFirstPick: Boolean? = null
    internal fun recoverFirstPick(allies: Int, rivals: Int, indicator: Boolean? = null): Boolean? {
        // Partial OCR counts are not a new draft order. The tenth-side evidence below
        // remains authoritative; a manual choice is handled by currentIsFirstPick.
        val observed = observedFirstPick ?: indicator ?: DraftPickOrderPolicy.inferFirstPick(allies, rivals)
        if (observed != null) observedFirstPick = observed
        return observed
    }
    private var isLegendaryRankedCache = false
    private val allySlotFilters = Array(5) { SlotTemporalFilter() }
    private val enemySlotFilters = Array(5) { SlotTemporalFilter() }

    fun resetSlotMemory(preserveBans: Boolean = false) {
        titleConfirmation.reset()
        lastVisualPick = null
        slotLifecycle.reset()
        observedFirstPick = null
        isLegendaryRankedCache = false
        cachedUserSlotIndex = null
        allySlotRolesCache.clear()
        allySlotOcrLaneCache.clear()
        allyRolesBySlotFlow.value = emptyMap()
        debugTextRects = emptyList()
        allySummonerNamesCache.clear()
        allySlotConfirmedChampions.fill(null)
        enemySlotConfirmedChampions.fill(null)
        allySlotNameConfirmed.fill(false)
        enemySlotNameConfirmed.fill(false)
        if (!preserveBans) clearDetectedBans()
        allySlotFilters.forEach { it.reset() }
        enemySlotFilters.forEach { it.reset() }
        LiteRTVisionClassifier.reset()
        debugVisualMatches.value = emptyMap()
        AppLogger.d(TAG, "Memoria de roles, invocadores y motor LiteRT reiniciada por completo")
    }

    fun clearSessionState() {
        resetSlotMemory()
        overlayRect = null
        debugTextRects = emptyList()
        showCalibrationBoxes.value = false
        isVisionEngineBusy.value = false
        isFirstPickState.value = false
        updateFps(0f)
        framesSkippedCount.value = 0L
        lastProcessingDurationMs.value = 0L
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

    /** Load the on-device text model before the first large draft frame. The
     * warm-up contains no game text and cannot confirm a slot or champion. */
    suspend fun warmUpTextRecognizer(): Boolean = recognizerWarmupMutex.withLock {
        if (recognizerWarmedUp) return@withLock true
        val recognizer = getRecognizer() ?: return@withLock false
        val sample = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888).apply {
            eraseColor(android.graphics.Color.BLACK)
        }
        try {
            recognizer.process(InputImage.fromBitmap(sample, 0)).await()
            recognizerWarmedUp = true
            true
        } catch (error: Throwable) {
            AppLogger.w(TAG, "No se pudo precargar OCR local: ${error.message}")
            false
        } finally {
            sample.recycle()
        }
    }

    suspend fun scanDraftFromBitmap(
        bitmap: Bitmap,
        context: android.content.Context? = null,
        currentIsFirstPick: Boolean? = null,
        currentActiveRole: LaneRole? = null,
        confirmedHudPicks: List<Champion> = emptyList(),
        detectBannedPortraits: Boolean = false,
        namesOnly: Boolean = false
    ): DraftScanResult {
        if (bitmap.isRecycled || bitmap.width < bitmap.height) {
            return DraftScanResult(emptyList(), emptyList(), isSuccessful = false, statusMessage = "Orientación no horizontal")
        }

        return try {
            val recognizer = getRecognizer() ?: return DraftScanResult(emptyList(), emptyList(), isSuccessful = false, statusMessage = "OCR no disponible")

        val width = bitmap.width
        val height = bitmap.height
        val frameOverlay = overlayForFrame(width, height)
        val frameDiagnosticTexts = diagnosticTextForFrame(width, height)
        if (context != null) {
            TenthPickDiagnosticManager.init(context)
        }
        val allChamps = WildRiftRepository.champions
        val auditList = mutableListOf<String>()
        val defaultRolesList = listOf(LaneRole.TOP, LaneRole.JUNGLE, LaneRole.MID, LaneRole.ADC, LaneRole.SUPPORT)
        val currentScanFrameOcrLanes = mutableMapOf<Int, LaneRole>()
        val previousLanes = allySlotOcrLaneCache.toMap()
        val freshAllyChampions = mutableMapOf<Int, Champion>()

        // Calibración adaptativa multipantalla para cualquier relación de aspecto (16:9, 18:9, 19.5:9, 20:9, 21:9, tablets)
        val calib = configForFrame(bitmap)

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
        val targetedAllyReadings = Array(5) { AllyDraftNameReader.Reading() }
        val targetedEnemyReadings = Array(5) { AllyDraftNameReader.Reading() }
        val targetedEnemyChampions = Array<Champion?>(5) { null }

        // -----------------------------------------------------------------------------------------
        // PASO 1: OCR DIRECTO CON MÁXIMA FIDELIDAD ÓPTICA
        // -----------------------------------------------------------------------------------------
        var isLegendaryRanked = if (namesOnly) isLegendaryRankedCache else false
        var isPreparationPhase = false
        var isActiveSelectionDetected = false
        var isPreparationBannerDetected = false
        try {
            // Diagnostic labels are our output, never evidence about the game.
            // Las etiquetas laterales del diagnóstico se dibujan fuera de la ventana central.
            // Deben excluirse siempre; de lo contrario el OCR puede leer "Pantheon" del propio overlay.
            val exclusions = frameDiagnosticTexts
            // namesOnly is the live draft fast path. All ten title rows are read
            // below from calibrated title-only atlases, so a second OCR request
            // over the entire 1920x863 frame is both redundant and extremely
            // expensive on an emulator/low-end device. Full scans retain the
            // whole-screen request for banners, account identity and metadata.
            val visionText: com.google.mlkit.vision.text.Text? = if (namesOnly) null else {
                val ocrBitmap = if (exclusions.isEmpty()) bitmap else
                    bitmap.copy(Bitmap.Config.ARGB_8888, true) ?: bitmap
                try {
                    if (ocrBitmap !== bitmap) {
                        val canvas = android.graphics.Canvas(ocrBitmap)
                        val paint = android.graphics.Paint().apply { color = android.graphics.Color.BLACK }
                        exclusions.forEach { canvas.drawRect(it, paint) }
                    }
                    recognizer.process(InputImage.fromBitmap(ocrBitmap, 0)).await()
                } finally { if (ocrBitmap !== bitmap) ocrBitmap.recycle() }
            }

            // Read every player title in one request, including the visible portion
            // of a name band partly covered by the central assistant window.
            val atlas = DraftNameAtlas.create(bitmap,calib,exclusions+listOfNotNull(frameOverlay))
            try {
                val detectedTitleLines=mutableMapOf<Pair<Boolean,Int>,MutableList<Rect>>()
                suspend fun readTitles(input: DraftNameAtlas) {
                val text = recognizer.process(InputImage.fromBitmap(input.bitmap,0)).await()
                val lines = text.textBlocks.flatMap { it.lines }
                for (tile in input.tiles) {
                    val selected = lines.filter { line ->
                        line.boundingBox?.let { tile.region.contains(it.centerX(),it.centerY()) &&
                            it.centerY()-tile.region.top>=tile.titleMinY } == true
                    }
                    // In a focused recovery crop the title legitimately starts
                    // at the left edge. Do not discard it as if it were the
                    // mastery badge; the title reader still requires a whole
                    // champion name or a complete lane title.
                    val titleLines = selected
                    // Keep unknown native line boxes for a tighter pixel-only
                    // re-read. Never accept their partial text as a champion.
                    val original=atlas.tiles.single { it.original && it.ally==tile.ally && it.slot==tile.slot }
                    for(line in titleLines) {
                        val box=line.boundingBox!!
                        val y=box.centerY()-tile.region.top
                        if(y>minOf(tile.titleMaxY,(tile.region.height()*.85f).toInt())) continue
                        if(AllyDraftNameReader.read(listOf(line.text),allChamps).let {
                            it.champion!=null || it.lane!=null || it.ambiguous }) continue
                        val mapped=DraftNameAtlas.mapDetectedLine(original,tile,box)
                        if(mapped.width()>0 && mapped.height()>0) {
                            val candidates=detectedTitleLines.getOrPut(tile.ally to tile.slot) { mutableListOf() }
                            if(mapped !in candidates) candidates+=mapped
                        }
                    }
                    val titlePairs = titleLines.sortedBy { it.boundingBox!!.left }.map {
                        it.text to (it.boundingBox!!.centerY()-tile.region.top)
                    }
                    val reading = AllyDraftNameReader.readTitleRows(titlePairs,
                        (tile.region.height()*.30f).toInt(),allChamps,tile.titleMaxY)
                    val readings = if(tile.ally) targetedAllyReadings else targetedEnemyReadings
                    // Every alternative obeys the same title-row bounds. A
                    // short nickname below the title is never a fallback.
                    readings[tile.slot]=AllyDraftNameReader.preferOriginal(readings[tile.slot],reading)
                }
                }
                fun unresolvedReading(tile: DraftNameAtlas.Tile) =
                    (if(tile.ally) targetedAllyReadings else targetedEnemyReadings)[tile.slot].let {
                        !it.ambiguous && it.champion==null && it.lane==null && !it.unpicked
                    }
                suspend fun readAlternatives(inputs: List<DraftNameAtlas>) {
                    try {
                        val pages=DraftNameAtlas.packAlternatives(inputs)
                        try {
                            for(page in pages) {
                                // Do not spend another native request on slots
                                // already resolved by an earlier page.
                                if(page.tiles.any(::unresolvedReading)) readTitles(page)
                            }
                        } finally { pages.forEach { it.bitmap.recycle() } }
                    } finally { inputs.forEach { it.bitmap.recycle() } }
                }
                readTitles(atlas)
                val unresolved = atlas.tiles.filter { tile -> tile.original &&
                    (if(tile.ally) targetedAllyReadings else targetedEnemyReadings)[tile.slot].let {
                        !it.ambiguous && it.champion==null && it.lane==null && !it.unpicked
                    }
                }.mapNotNull { tile -> DraftNameAtlas.titleFocusBounds(atlas,tile)?.let { tile to it } }
                if (unresolved.isNotEmpty()) {
                    val retry = DraftNameAtlas.retryUnresolved(atlas,unresolved.map { it.first })
                    try { readTitles(retry) } finally { retry.bitmap.recycle() }
                }
                // Keep the recovery bounded: the previous global multiscale pass
                // created several large OCR requests per unresolved slot and made
                // 640x360 hit the 60-second frame deadline. One shared page now
                // contains only the focused row and its badge-suppressed sibling.
                val focusedAlternatives=mutableListOf<DraftNameAtlas>()
                try {
                    for((tile,bounds) in unresolved) if(unresolvedReading(tile)) {
                        val focused=DraftNameAtlas.focusTitle(atlas,tile,bounds)
                        focusedAlternatives += focused
                        if(tile.ally) {
                            focusedAlternatives += DraftNameAtlas.withoutLeadingBadge(atlas,tile)
                        }
                    }
                    if(focusedAlternatives.isNotEmpty()) readAlternatives(focusedAlternatives)
                } catch(error:Throwable) {
                    focusedAlternatives.forEach { if(!it.bitmap.isRecycled) it.bitmap.recycle() }
                    throw error
                }
                for((tile,bounds) in unresolved) {
                    if(!unresolvedReading(tile)) continue
                    // First use measured native text boxes. If the detector did
                    // not find a line, also isolate the calibrated title row;
                    // the untouched full band has already been read above.
                    val detected=detectedTitleLines[tile.ally to tile.slot].orEmpty()
                    val fallback=Rect(bounds.left,(tile.region.height()*.30f).toInt(),bounds.right,
                        minOf(tile.titleMaxY+(tile.region.height()*.12f).toInt(),(tile.region.height()*.90f).toInt()))
                    // The second measured box reads MEL in the native 640x360
                    // evidence; discarding it after the first unreadable box
                    // made recovery impossible. The full row stays available.
                    val candidates=if(detected.isNotEmpty()) detected.distinct().take(2)
                        else listOf(fallback)
                    for(line in candidates) {
                        if(!unresolvedReading(tile)) break
                        val focused=DraftNameAtlas.focusDetectedLine(atlas,tile,line)
                        try {
                            // Native evidence already reads MEL from the second
                            // unscaled crop. Do not normalize away that evidence.
                            readTitles(focused)
                            // Short titles such as Vi can disappear at the
                            // source crop size. The diagnostic path proved that
                            // these same pixels become readable at native text
                            // sizes; use that bounded recovery in production as
                            // well, without inventing a champion identity.
                            if (unresolvedReading(tile)) {
                                val native = DraftNameAtlas.nativeTextSizes(focused, line.height())
                                try { readTitles(native) } finally { native.bitmap.recycle() }
                            }
                        } finally { focused.bitmap.recycle() }
                    }
                }
                // Keep established OCR image sizes unchanged. Color separation is
                // a final independent request over the same title pixels. It is
                // also required for short allied titles: on the 1920x924 P20
                // geometry ML Kit sees neither VI nor a native line box in the
                // intact band, while the badge-suppressed color crop retains both
                // glyphs. Pack all unresolved crops so this remains one bounded
                // request rather than one request per slot.
                val colorAlternatives=mutableListOf<DraftNameAtlas>()
                try {
                    for((tile,bounds) in unresolved) if(unresolvedReading(tile)) {
                        colorAlternatives += DraftNameAtlas.focusTitle(atlas,tile,bounds,colorOnly=true)
                    }
                    if(colorAlternatives.isNotEmpty()) readAlternatives(colorAlternatives)
                } catch(error:Throwable) {
                    colorAlternatives.forEach { if(!it.bitmap.isRecycled) it.bitmap.recycle() }
                    throw error
                }
                // ML Kit can reject a real two-letter title as an icon when a
                // mastery badge and its wide empty margin share the same crop.
                // Retry only still-unresolved allied rows with the badge-cleared
                // captured pixels at ordinary OCR sizes. No expected title or
                // portrait result participates in this recovery.
                val shortAlternatives=mutableListOf<DraftNameAtlas>()
                try {
                    for((tile,_) in unresolved) if(tile.ally && unresolvedReading(tile)) {
                        DraftNameAtlas.shortAlliedTitleAlternatives(atlas,tile)?.let(shortAlternatives::add)
                        DraftNameAtlas.shortAlliedTitleAlternatives(atlas,tile,binary=false)?.let(shortAlternatives::add)
                        DraftNameAtlas.shortAlliedTitleAlternatives(atlas,tile,neutralOnly=true)?.let(shortAlternatives::add)
                    }
                    if(shortAlternatives.isNotEmpty()) readAlternatives(shortAlternatives)
                } catch(error:Throwable) {
                    shortAlternatives.forEach { if(!it.bitmap.isRecycled) it.bitmap.recycle() }
                    throw error
                }
                val repeatedShortAlternatives=mutableListOf<DraftNameAtlas>()
                try {
                    for((tile,_) in unresolved) if(tile.ally && unresolvedReading(tile)) {
                        DraftNameAtlas.repeatedShortAlliedTitleAlternative(atlas,tile)
                            ?.let(repeatedShortAlternatives::add)
                    }
                    if(repeatedShortAlternatives.isNotEmpty()) readAlternatives(repeatedShortAlternatives)
                } catch(error:Throwable) {
                    repeatedShortAlternatives.forEach { if(!it.bitmap.isRecycled) it.bitmap.recycle() }
                    throw error
                }
                val observedTitleIds=(targetedAllyReadings.toList()+targetedEnemyReadings.toList())
                    .mapNotNull { it.champion?.id }.toSet()
                if(observedTitleIds.size<9 && unresolved.any { (tile,_) ->
                    unresolvedReading(tile)
                }) {
                    val scene=DraftNameAtlas.sceneTitles(bitmap,calib,exclusions+listOfNotNull(frameOverlay))
                    try { readTitles(scene) } finally { scene.bitmap.recycle() }
                }
                for (slot in 0..4) {
                    targetedEnemyChampions[slot] = targetedEnemyReadings[slot].let {
                        it.champion.takeUnless { _ -> it.ambiguous }
                    }
                }
            } finally { atlas.bitmap.recycle() }

            // Detección proactiva de Clasificatoria Legendaria en pantalla completa
            if (!namesOnly) isLegendaryRanked = DraftValidationLayer.isLegendaryRankedDraft(
                fullOcrText = visionText?.text.orEmpty(),
                hasRealSummonerNames = allySummonerNamesCache.isNotEmpty()
            )
            isLegendaryRankedCache = isLegendaryRanked
            if (isLegendaryRanked) {
                AppLogger.d(TAG, "Clasificatoria Legendaria detectada en pantalla (Nombres anónimos). Búsqueda de invocadores desactivada.")
                allySummonerNamesCache.clear()
            }

            for (block in visionText?.textBlocks.orEmpty()) {
                for (line in block.lines) {
                    val text = line.text.trim()
                    if (text.isBlank() || text.length < 2) continue

                    val box = line.boundingBox
                    if (box != null && frameOverlay != null) {
                        if (android.graphics.Rect.intersects(box, frameOverlay)) {
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
                                                 lowerText.contains("visor") ||
                                                 lowerText.contains("aliado") || lowerText.contains("rival")
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
                    if (box != null && frameOverlay != null && android.graphics.Rect.intersects(box, frameOverlay)) {
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
                        text.contains("RIVAL", ignoreCase = true) || text.contains("VACÍO", ignoreCase = true) ||
                        text.contains("VACIO", ignoreCase = true) || text.contains("CONFIRMADO", ignoreCase = true) ||
                        text.contains("AMBIGUO", ignoreCase = true) || text.contains("⚡") || text.contains("🐛") ||
                        text.contains("Diagnóstico", ignoreCase = true) || text.contains("Diagnostico", ignoreCase = true) ||
                        text.contains("Score", ignoreCase = true)) continue
                    
                    detectedWords.add(text)

                    // Header names are not evidence of bans; only stable header portraits qualify.
                    if (yRatio < 0.12f) continue
                    if (yRatio > 0.880f) continue

                    val boxLeftRatio = if (box != null && width > 0) box.left.toFloat() / width.toFloat() else xRatio
                    val boxRightRatio = if (box != null && width > 0) box.right.toFloat() / width.toFloat() else xRatio

                    val isAllyCol = (xRatio in calib.allyOcrMinX..calib.allyOcrMaxX) ||
                            (xRatio < 0.32f && (boxLeftRatio <= calib.allyOcrMaxX && boxRightRatio >= calib.allyOcrMinX))
                    val isEnemyCol = (xRatio in calib.enemyOcrMinX..calib.enemyOcrMaxX) ||
                            (xRatio > 0.78f && (boxLeftRatio <= calib.enemyOcrMaxX && boxRightRatio >= calib.enemyOcrMinX))

                    // 1.1 COLUMNA ALIADA (Texto a la derecha del avatar aliado)
                    if (isAllyCol) {
                        var bestSlot = -1
                        var minDiff = 0.095f
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
                        var minDiff = 0.095f
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
            val currentUserNameClean = if (!namesOnly && !isLegendaryRanked) {
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
            currentScanFrameOcrLanes.clear()

            // Procesar textos aliados: Detección estricta de Línea 1 (Rol o Campeón) y Línea 2 (Nombre de Invocador)
            for (i in 0..4) {
                val slot = allySlots[i]
                val entries = allySlotTexts[i]

                var detectedRoleInSlot: LaneRole? = null
                var detectedChampInSlot: Champion? = null
                val summonerCandidates = mutableListOf<String>()

                val titleRect = AdaptiveScreenLayoutEngine.calculateSlotNameRect(width, height, true, i, calib)
                val titleLines = entries.filter { (_, box) -> box != null && titleRect.contains(box.centerX(), box.centerY()) }
                    .map { it.first to it.second!!.centerY() }
                val isolated = targetedAllyReadings[i]
                val reading = if (isolated.ambiguous || isolated.lane != null ||
                    isolated.champion != null || isolated.unpicked) isolated
                    else AllyDraftNameReader.readTitleRows(titleLines, (titleRect.height() * 0.30f).toInt(), allChamps,
                        (height * (calib.allySlotYRatios[i] + 0.005f * calib.nameBandScale)).toInt())
                detectedRoleInSlot = reading.lane
                val stableTitle = titleConfirmation.observe(true, i,
                    reading.champion?.id ?: reading.lane?.let { "lane:${it.name}" })
                detectedChampInSlot = reading.champion.takeIf { stableTitle }
                if (detectedRoleInSlot != null && (stableTitle || allySlotConfirmedChampions[i] == null)) {
                    currentScanFrameOcrLanes[i] = detectedRoleInSlot
                    slot.explicitRole = detectedRoleInSlot
                }
                if (detectedChampInSlot != null) freshAllyChampions[i] = detectedChampInSlot
                if (entries.isNotEmpty()) {
                    // 3. ANALIZAR NOMBRE DE INVOCADOR / TAG DE USUARIO:
                    for ((line, box) in entries) {
                        val safeBox = box ?: Rect(0, 0, 10, 10)
                        if (DraftValidationLayer.isNoiseText(line)) continue

                        val lineNorm = DraftValidationLayer.normalize(line).lowercase(Locale.ROOT)

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

                        val selfMarker = AllyDraftNameReader.identifiesUser(line, emptyList())
                        val isPlayerNameLine = box != null && box.centerY() > height * (calib.allySlotYRatios[i] + 0.005f * calib.nameBandScale)
                        val isUserTag = !isLegendaryRanked && (selfMarker ||
                            (isPlayerNameLine && AllyDraftNameReader.identifiesUser(line, currentUserNameClean)))

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
                    allySlotNameConfirmed[i] = true
                    allyOcrChampions[i] = detectedChampInSlot
                    slot.champion = detectedChampInSlot
                    slot.confidencePercent = 100
                    slot.isLikelyUnpicked = false
                } else if (detectedRoleInSlot != null && stableTitle) {
                    // El slot está mostrando el nombre de la línea asignada (ej. "CALLE CENTRAL", "APOYO", "JUNGLA").
                    // Esto indica de forma concluyente que el jugador AÚN NO ha seleccionado ningún campeón.
                    allySlotConfirmedChampions[i] = null
                    allySlotNameConfirmed[i] = false
                    allyOcrChampions[i] = null
                    slot.champion = null
                    slot.confidencePercent = 0
                    slot.isLikelyUnpicked = true
                } else if (allySlotConfirmedChampions[i] != null && allySlotNameConfirmed[i]) {
                    // Mantener el campeón ya confirmado previamente
                    val existingChamp = allySlotConfirmedChampions[i]
                    allyOcrChampions[i] = existingChamp
                    slot.champion = existingChamp
                    slot.confidencePercent = 100
                    slot.isLikelyUnpicked = false
                } else {
                    // El slot aún no ha seleccionado ningún campeón
                    allySlotConfirmedChampions[i] = null
                    allySlotNameConfirmed[i] = false
                    allyOcrChampions[i] = null
                    slot.champion = null
                    slot.confidencePercent = 0
                    slot.isLikelyUnpicked = true
                    allySlotFilters[i].reset()
                }
            }

            // Un campeón confirmado por el lado aliado no puede quedar vivo en la memoria rival.
            val currentAllyIds = allySlotConfirmedChampions.mapNotNull { it?.id }.toSet()
            for (i in 0..4) {
                if (currentAllyIds.contains(enemySlotConfirmedChampions[i]?.id)) {
                    enemySlotConfirmedChampions[i] = null
                    enemyOcrChampions[i] = null
                    enemySlots[i].champion = null
                    enemySlots[i].isLikelyUnpicked = true
                }
            }

            // Remove stale copies when an already selected champion moves to another slot.
            for ((slotIndex, champion) in freshAllyChampions) {
                for (other in 0..4) {
                    if (other != slotIndex && allySlots[other].champion?.id == champion.id &&
                        freshAllyChampions[other]?.id != champion.id) {
                        allySlots[other].champion = null
                        allySlots[other].isLikelyUnpicked = true
                        allySlotConfirmedChampions[other] = null
                        allyOcrChampions[other] = null
                        allySlotFilters[other].reset()
                    }
                }
            }
            val resolvedRoles = AllyDraftReconciler.observedRoles(currentScanFrameOcrLanes, previousLanes)
            allySlotOcrLaneCache.clear()
            allySlotOcrLaneCache.putAll(resolvedRoles)
            allySlotRolesCache.clear()
            allySlotRolesCache.putAll(resolvedRoles)
            for (slot in allySlots) slot.explicitRole = resolvedRoles[slot.slotIndex]

            // Solo identidad observada y línea del mismo puesto pueden confirmar tu línea.
            if (currentScanFrameOcrLanes.size == 5) cachedUserSlotIndex = null
            if (userSlotIndex != null) {
                cachedUserSlotIndex = userSlotIndex
            } else if (cachedUserSlotIndex != null) {
                userSlotIndex = cachedUserSlotIndex
                userExplicitlyConfirmed = true
            }

            val uIdx = userSlotIndex
            if (uIdx != null && uIdx in 0..4) {
                val explicitRole = allySlotOcrLaneCache[uIdx] ?: allySlotRolesCache[uIdx]
                if (explicitRole != null) {
                    userDetectedLane = explicitRole
                    AppLogger.d(TAG, "Rol de usuario confirmado explícitamente en Slot $uIdx -> ${userDetectedLane.shortName}")
                } else {
                    userExplicitlyConfirmed = false
                    userDetectedLane = currentActiveRole
                }
            } else if (currentActiveRole != null) {
                userDetectedLane = currentActiveRole
            }

            // Para el lado rival: Analizamos el texto de cada slot.
            // En Wild Rift, cuando un rival fija o selecciona un campeón, el nombre aparece en texto:
            // "ANNIE", "VOLIBEAR", "SERAPHINE", "ASHE", "SHYVANA", "YUUMI", "SAMIRA", etc.
            // Mientras no seleccione, muestra "Jugador 1", "Jugador 2", etc.
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

                detectedEnemyChamp = (targetedEnemyChampions[i] ?: detectedEnemyChamp).takeIf {
                    titleConfirmation.observe(false, i, it?.id)
                }

                // REGLAS ESTRICTAS DEL USUARIO:
                // Si el OCR detecta un campeón en el slot rival, se confirma al 100% y se guarda en memoria.
                // Si en fotogramas posteriores no se detecta nuevo texto, se MANTIENE intacto el campeón ya confirmado.
                if (detectedEnemyChamp != null) {
                    enemySlotConfirmedChampions[i] = detectedEnemyChamp
                    enemySlotNameConfirmed[i] = true
                    enemyOcrChampions[i] = detectedEnemyChamp
                    enemySlots[i].champion = detectedEnemyChamp
                    enemySlots[i].confidencePercent = 100
                    enemySlots[i].isLikelyUnpicked = false
                } else if (enemySlotConfirmedChampions[i] != null && enemySlotNameConfirmed[i]) {
                    val existingEnemy = enemySlotConfirmedChampions[i]
                    enemyOcrChampions[i] = existingEnemy
                    enemySlots[i].champion = existingEnemy
                    enemySlots[i].confidencePercent = 100
                    enemySlots[i].isLikelyUnpicked = false
                } else {
                    enemySlotConfirmedChampions[i] = null
                    enemySlotNameConfirmed[i] = false
                    enemyOcrChampions[i] = null
                    enemySlots[i].champion = null
                    enemySlots[i].confidencePercent = 0
                    enemySlots[i].isLikelyUnpicked = true
                    enemySlotFilters[i].reset()
                }
            }
        } catch (e: CancellationException) {
            throw e
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
        val allySlotYRatios = calib.allySlotYRatios
        val enemySlotYRatios = calib.enemySlotYRatios
        val diagnosticsList = mutableListOf<SlotDiagnostic>()

        // 3.1 Aliados: Si se detectó el nombre del campeón en el slot, se asocia directamente a la línea memorizada de ese slot
        for (i in 0..4) {
            val slot = allySlots[i]
            val roiRect = AdaptiveScreenLayoutEngine.calculateSlotCropRect(width, height, true, i, calib)

            val ocrChamp = allyOcrChampions[i]
            val roleForSlot = allySlotRolesCache[i]
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
                "Campeón confirmado por nombre OCR: ${finalChamp.name} -> ${roleForSlot?.shortName ?: "Línea pendiente"}"
            } else {
                "Esperando selección en carril ${roleForSlot?.shortName ?: "Línea pendiente"} (${allySummonerNamesCache[i] ?: "Invocador"})"
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
            val roiRect = AdaptiveScreenLayoutEngine.calculateSlotCropRect(width, height, false, i, calib)

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

        if (!namesOnly) for (i in 0..4) {
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

        // Recover the order from a valid prefix even when auto-scan starts late.
        // Ambiguous prefixes preserve observed order; only an explicit manual choice overrides it.
        val missingTenthSide = DraftPickOrderPolicy.tenthSideFromTitles(
            allySlotNameConfirmed.toList(), enemySlotNameConfirmed.toList())
        detectedFirstPick = if (missingTenthSide != null) {
            DraftPickOrderPolicy.firstPickFromTenthSide(missingTenthSide).also { observedFirstPick = it }
        } else recoverFirstPick(totalAllyOcr, totalEnemyOcr, detectedFirstPick)

        val effectiveFirstPick = currentIsFirstPick ?: detectedFirstPick ?: false
        isFirstPickState.value = effectiveFirstPick
        val pickSequence = getDraftPickSequence(effectiveFirstPick)

        // -----------------------------------------------------------------------------------------
        // PASO 3.5: MOTOR RECONOCIMIENTO DE RETRATOS LOCALES PARA EL 10º PICK
        // REGLA CRÍTICA:
        // - Si Rival es 1ª Selección (isFirstPick == false) -> 10º Pick es ALIADO 5 (isAlly = true, slotIndex = 4).
        // - Si Aliado es 1ª Selección (isFirstPick == true) -> 10º Pick es RIVAL 5 (isAlly = false, slotIndex = 4).
        // Se activa cuando las 9 selecciones previas están listas o el slot objetivo está pendiente.
        // -----------------------------------------------------------------------------------------
        val tenthTurn = pickSequence.last()
        val tenthIsAlly = tenthTurn.isAlly
        val tenthSlotIndex = tenthTurn.slotIndex

        val draftVisible = allyOcrChampions.any { it != null } || enemyOcrChampions.any { it != null } ||
            currentScanFrameOcrLanes.isNotEmpty() || isActiveSelectionDetected || isPreparationPhase
        if (detectBannedPortraits) banDetectionActiveFlow.value = true
        if (!namesOnly && detectBannedPortraits && draftVisible) {
            val exclusions = frameDiagnosticTexts + listOfNotNull(frameOverlay)
            val pickedIds = (allyOcrChampions.filterNotNull() + enemyOcrChampions.filterNotNull()).map { it.id }.toSet()
            bannedChampionIdsFlow.value = bannedPortraitScanner.scan(bitmap, context,
                android.os.SystemClock.elapsedRealtime(), exclusions, calib, pickedIds)
            banSlotsFlow.value = bannedPortraitScanner.slots
        }

        val otherPicks = getConfirmedPicksExcept(tenthTurn, confirmedHudPicks)
        val confirmedPicksCount = otherPicks.size
        val confirmedChampIds = otherPicks.map { it.id }.toSet() + detectedBannedChampionIds

        var detectedTenthChampion: Champion? = null
        var isTenthConfirmed = false

        val targetSlot = if (tenthIsAlly) allySlots[tenthSlotIndex] else enemySlots[tenthSlotIndex]
        val targetNameConfirmed = if (tenthIsAlly) allySlotNameConfirmed[tenthSlotIndex] else enemySlotNameConfirmed[tenthSlotIndex]
        val targetAlreadyConfirmed = targetNameConfirmed &&
            (if (tenthIsAlly) allySlotConfirmedChampions[tenthSlotIndex] != null else enemySlotConfirmedChampions[tenthSlotIndex] != null)

        // EXTRACCIÓN Y ANÁLISIS EN VIVO CONTINUO DEL 10º PICK (Reconocimiento visual local):
        // Se extrae el recorte del slot en cada fotograma para alimentar el visor en tiempo real y permitir pruebas del usuario en todo momento.
        val tenthCrop: Bitmap? = if (namesOnly) null else AdaptiveScreenLayoutEngine.extractSlotAvatarBitmap(
            sourceBitmap = bitmap,
            width = width,
            height = height,
            isAlly = tenthIsAlly,
            slotIndex = tenthSlotIndex,
            config = calib
        )

        if (tenthCrop != null && !tenthCrop.isRecycled) {
            TenthPickDiagnosticManager.recordTenthPickCrop(
                cropBitmap = tenthCrop,
                isAlly = tenthIsAlly,
                slotIndex = tenthSlotIndex,
                stage = "CROP_EXTRACTED",
                context = context
            )
        }

        val liteRTDecision = if (namesOnly) null else LiteRTVisionClassifier.executeTenthPickInference(
            cropBitmap = tenthCrop,
            isAlly = tenthIsAlly,
            confirmedChampionIds = confirmedChampIds,
            confirmedPicksCount = confirmedPicksCount,
            slotIndex = tenthSlotIndex,
            context = context,
            confirmedTargetChampion = targetSlot.champion?.takeIf { targetNameConfirmed },
            allowVisualConfirmation = !tenthIsAlly || currentScanFrameOcrLanes[tenthSlotIndex] == null || otherPicks.size >= 9
        )

        if (liteRTDecision != null && confirmedPicksCount >= 9) {
            val (champWinner, confidence) = liteRTDecision
            if (currentIsFirstPick == null) {
                detectedFirstPick = DraftPickOrderPolicy.firstPickFromTenthSide(tenthIsAlly)
                isFirstPickState.value = detectedFirstPick!!
            }
            lastVisualPick = tenthIsAlly to champWinner
            detectedTenthChampion = champWinner
            isTenthConfirmed = true
            if (tenthIsAlly) {
                allySlots[tenthSlotIndex].champion = champWinner
                allySlots[tenthSlotIndex].confidencePercent = confidence
                allySlots[tenthSlotIndex].isLikelyUnpicked = false
                allySlotConfirmedChampions[tenthSlotIndex] = champWinner
            } else {
                enemySlots[tenthSlotIndex].champion = champWinner
                enemySlots[tenthSlotIndex].confidencePercent = confidence
                enemySlots[tenthSlotIndex].isLikelyUnpicked = false
                enemySlotConfirmedChampions[tenthSlotIndex] = champWinner
            }
            AppLogger.d(TAG, "Reconocimiento visual local decidió el 10º Pick -> ${champWinner.name} ($confidence%)")
        } else if (targetAlreadyConfirmed || lastVisualPick?.first == tenthIsAlly) {
            // Preservar la confirmación previa del 10º pick en el modelo de juego
            val cachedChamp = if (targetAlreadyConfirmed) {
                if (tenthIsAlly) allySlotConfirmedChampions[tenthSlotIndex] else enemySlotConfirmedChampions[tenthSlotIndex]
            } else lastVisualPick?.second
            if (cachedChamp != null) {
                detectedTenthChampion = cachedChamp
                isTenthConfirmed = true
                if (tenthIsAlly) {
                    allySlotConfirmedChampions[tenthSlotIndex] = cachedChamp
                    allySlots[tenthSlotIndex].champion = cachedChamp
                    allySlots[tenthSlotIndex].confidencePercent = 100
                    allySlots[tenthSlotIndex].isLikelyUnpicked = false
                } else {
                    enemySlotConfirmedChampions[tenthSlotIndex] = cachedChamp
                    enemySlots[tenthSlotIndex].champion = cachedChamp
                    enemySlots[tenthSlotIndex].confidencePercent = 100
                    enemySlots[tenthSlotIndex].isLikelyUnpicked = false
                }
            }
        }

        try { tenthCrop?.recycle() } catch (_: Throwable) {}
        
        // Observed or remembered lane titles are authoritative. If a capture
        // starts after some players already selected, complete only the still
        // unknown slots deterministically from champion affinity; never move
        // or overwrite an OCR-observed lane.
        val resolvedAlliedRoles = AllyDraftReconciler.resolveRoles(
            allySlots.mapNotNull { slot -> slot.champion?.let { slot.slotIndex to it } }.toMap(),
            allySlotOcrLaneCache,
            allySlotRolesCache
        )
        // 4.1 Aliados: cada slot conserva la línea leída en su propia banda.
        val alliesMap = mutableMapOf<LaneRole, Champion>()
        val allyConfidences = mutableMapOf<LaneRole, Int>()

        for (i in 0..4) {
            val slot = allySlots[i]
            val role = resolvedAlliedRoles[i] ?: continue
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
                val targetRole = availableRoles.firstOrNull()
                if (targetRole != null) {
                    enemiesMap[targetRole] = champ
                    slot.assignedRole = targetRole
                    AppLogger.d(TAG, "Rival ${champ.name} preservado y asignado a ${targetRole.shortName}")
                }
            }
        }

        // Deduplicación: Un campeón aliado jamás puede aparecer en el equipo enemigo
        val allyChampIds = alliesMap.values.map { it.id }.toSet()
        val finalEnemiesMap = enemiesMap.filterNot { allyChampIds.contains(it.value.id) }
        val enemyConfidences = enemyResolved.confidences.filterKeys { finalEnemiesMap.containsKey(it) }

        // Los datos aliados siguen la misma línea observada que el campeón.
        val allySummonerNamesByRole = mutableMapOf<LaneRole, String>()
        val allySpellsByRole = mutableMapOf<LaneRole, List<String>>()

        for (i in 0..4) {
            val slot = allySlots[i]
            val role = allySlotRolesCache[i]
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
        val enemiesBySlotMap = enemySlots.mapNotNull { s -> s.champion?.let { s.slotIndex to it } }.toMap()

        val visualMatches = mutableMapOf<String, String>()
        for (i in 0..4) {
            val isUnpickedSlot = allySlots[i].isLikelyUnpicked || currentScanFrameOcrLanes.containsKey(i)
            val confirmedChamp = if (isUnpickedSlot) null else (alliesBySlotMap[i]?.name ?: allySlotConfirmedChampions[i]?.name)
            val detectedLaneName = (allySlotOcrLaneCache[i] ?: allySlotRolesCache[i] ?: allySlots.getOrNull(i)?.explicitRole)?.displayName
            val aDisplayName = confirmedChamp ?: detectedLaneName
            if (aDisplayName != null) visualMatches["ally_$i"] = aDisplayName
            val eChamp = enemiesBySlotMap[i]?.name ?: enemySlotConfirmedChampions[i]?.name
            if (eChamp != null) visualMatches["enemy_$i"] = eChamp
        }
        allyRolesBySlotFlow.value = resolvedAlliedRoles
        debugVisualMatches.value = visualMatches

        val allyChampsList = alliesBySlotMap.values.distinctBy { it.id }
        val enemyChampsList = finalEnemiesMap.values.toList()
        val total = allyChampsList.size + enemyChampsList.size

        val hasDraftActivity = total > 0 || currentScanFrameOcrLanes.isNotEmpty() || allySummonerNamesCache.isNotEmpty() || userDetectedLane != null || detectedFirstPick != null || isLegendaryRanked || isPreparationPhase

        val statusMsg = when {
            isLegendaryRanked && total == 0 -> "Clasificatoria Legendaria (Nombres anónimos)"
            isLegendaryRanked -> "Clasificatoria Legendaria • $total picks detectados"
            total == 0 && allySummonerNamesCache.isNotEmpty() -> "Invocadores aliados detectados (${allySummonerNamesCache.size}/5)"
            total == 0 -> "Esperando selección en directo..."
            total == 10 -> {
                val tenthChamp = if (tenthIsAlly) allySlots[tenthSlotIndex].champion else enemySlots[tenthSlotIndex].champion
                if (tenthChamp != null) {
                    "10/10 Completo • 10º Pick confirmado (${tenthChamp.name})"
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
            allyRolesBySlot = resolvedAlliedRoles,
            enemiesByRole = finalEnemiesMap,
            enemyConfidencesByRole = enemyConfidences,
            detectedRole = userDetectedLane,
            userExplicitlyDetectedRole = if (userExplicitlyConfirmed) userDetectedLane else null,
            detectedFirstPick = detectedFirstPick,
            isLastPickImageRecognized = detectedTenthChampion != null,
            isLastPickConfirmed = isTenthConfirmed,
            isLastPickFinalized = isTenthConfirmed && (targetAlreadyConfirmed ||
                (LiteRTVisionClassifier.reportFlow.value.isSelectionFinalized &&
                 LiteRTVisionClassifier.reportFlow.value.pickedChampion?.id == detectedTenthChampion?.id)),
            lastPickChampion = detectedTenthChampion,
            tenthPickIsAlly = tenthIsAlly,
            tenthPickSlotIndex = tenthSlotIndex,
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
            isSuccessful = hasDraftActivity || isTenthConfirmed,
            statusMessage = statusMsg
        )
        } catch (t: CancellationException) {
            throw t
        } catch (t: Throwable) {
            AppLogger.e(TAG, "Excepción no controlada en scanDraftFromBitmap prevenida", t)
            DraftScanResult(emptyList(), emptyList(), isSuccessful = false, statusMessage = "Error en escaneo")
        }
    }
}
