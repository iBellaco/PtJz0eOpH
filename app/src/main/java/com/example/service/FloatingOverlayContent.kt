package com.example.service

import kotlinx.coroutines.flow.collect
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.material.icons.filled.BugReport
import kotlinx.coroutines.launch
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.R
import com.example.data.WildRiftRepository
import com.example.model.Champion
import com.example.model.DraftSlot
import com.example.model.LaneRole
import com.example.service.screen.DraftPickTurn
import com.example.service.screen.DraftVisionScanner
import com.example.service.screen.ScreenCaptureManager
import com.example.ui.components.ChampionAvatar
import com.example.ui.theme.AllyBlue
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.HextechSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.AppLogger
import com.example.util.SubscriptionManager
import com.example.util.tr
import com.example.util.trStr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@Composable
internal fun FloatingOverlayContent(
    state: OverlayState,
    isLandscapeMode: Boolean,
    screenCaptureManager: ScreenCaptureManager?,
    onClose: () -> Unit,
    onDragDelta: (dx: Int, dy: Int, isDragging: Boolean, isEnded: Boolean) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onCompactModeChange: (Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isExpanded by state::isExpanded
    var overlayHubTab by state::overlayHubTab
    var showSaveDraftDialog by state::showSaveDraftDialog
    var showRoleChangeDialog by state::showRoleChangeDialog
    var isSavedRecently by state::isSavedRecently
    val isPremium by com.example.util.SubscriptionManager.isPremium.collectAsStateWithLifecycle()
    val activeProfileId by com.example.data.AccountProfileManager.activeProfileId.collectAsStateWithLifecycle()
    val isLoggedInAndPremium = isPremium && activeProfileId != null
    val userRole by com.example.util.SubscriptionManager.userRole.collectAsStateWithLifecycle()
    val currentAuthEmail = remember { com.example.util.AuthManager.getAuth()?.currentUser?.email }
    val isAdmin = userRole == "admin" || userRole == "moderador" || (currentAuthEmail != null && currentAuthEmail.contains("barbadiego", ignoreCase = true)) || com.example.util.AuthManager.isCurrentUserAdmin()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val currentLang = com.example.util.currentAppLanguage()
    var activeRole by state::activeRole

    val sharedPrefs = remember { context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }

    LaunchedEffect(Unit) {
        val savedRoleStr = sharedPrefs.getString("saved_active_role", null)
        if (savedRoleStr != null) {
            try {
                activeRole = LaneRole.valueOf(savedRoleStr)
                state.isRoleManuallySelected = true
            } catch (e: Exception) { }
        }
    }

    LaunchedEffect(activeRole) {
        sharedPrefs.edit().putString("saved_active_role", activeRole.name).apply()
    }

    var isFirstPick by state::isFirstPick
    var isLegendaryQueue by state::isLegendaryQueue
    var isCompactBubble by state::isCompactBubble
    var showLiteRTViewer by remember { mutableStateOf(false) }

    val defaultRoles = remember { listOf(LaneRole.TOP, LaneRole.JUNGLE, LaneRole.MID, LaneRole.ADC, LaneRole.SUPPORT) }
    val allies = state.allies
    val enemies = state.enemies
    val manualLockedAllySlots = state.manualLockedAllySlots
    val manualLockedEnemySlots = state.manualLockedEnemySlots

    val syncAlliedHud: (Map<LaneRole, Champion>) -> Int = { scanned ->
        val next = com.example.service.screen.AllyDraftReconciler.hudAllies(
            allies.toList(), scanned, manualLockedAllySlots.filterValues { it }.keys
        )
        val changes = next.indices.count { allies[it]?.id != next[it]?.id }
        androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
            next.forEachIndexed { index, champ -> allies[index] = champ }
            for (index in enemies.indices) {
                if (manualLockedEnemySlots[index] != true && next.any { it != null && it.id == enemies[index]?.id }) {
                    enemies[index] = null
                }
            }
        }
        changes
    }

    val syncEnemyHud: (com.example.service.screen.DraftScanResult) -> Int = { state.syncScannedEnemies(it) }

    val confirmedHudPicks = { (allies.toList() + enemies.toList()).filterNotNull().takeIf { it.size == 9 }.orEmpty() }
    val commitLastPickToHud: (com.example.service.screen.DraftScanResult) -> Unit = { result ->
        state.applyConfirmedLastPick(result)
    }

    // The viewer and the HUD consume the same confirmed decision. This also
    // applies an explicit viewer selection while auto-scan is paused.
    LaunchedEffect(state) {
        com.example.service.screen.LiteRTVisionClassifier.reportFlow.collect { report ->
            if (report.isConfirmed && report.pickedChampion != null &&
                (report.slotDescription.startsWith("Aliado") || report.slotDescription.startsWith("Rival"))) {
                state.applyConfirmedLastPick(com.example.service.screen.DraftScanResult(
                    allies = allies.filterNotNull(), enemies = enemies.filterNotNull(),
                    isLastPickConfirmed = true, lastPickChampion = report.pickedChampion,
                    tenthPickIsAlly = report.slotDescription.startsWith("Aliado"), tenthPickSlotIndex = 4,
                    allyRolesBySlot = DraftVisionScanner.allyRolesBySlotFlow.value,
                    isSuccessful = true, statusMessage = ""
                ))
            }
        }
    }

    var isScanning by state::isScanning
    var autoScanEnabled by state::autoScanEnabled
    var scanNoticeMessage by state::scanNoticeMessage

    var isDraggingBubble by state::isDraggingBubble
    var dragAccumulatedY by state::dragAccumulatedY
    var isNearCloseThreshold by state::isNearCloseThreshold

    var selectedChampionDetail by state::selectedChampionDetail
    var showChampionPickerForSlot by state::showChampionPickerForSlot
    var isLoadingScreenMode by state::isLoadingScreenMode
    var isOverlayTabsMinimized by state::isOverlayTabsMinimized

    val explicitEnemyOpponent = remember(activeRole, enemies.toList()) {
        val roleIndex = defaultRoles.indexOf(activeRole).coerceIn(0, 4)
        enemies.getOrNull(roleIndex)
    }

    val analysis = remember(activeRole, isFirstPick, allies.toList(), enemies.toList(), explicitEnemyOpponent) {
        WildRiftRepository.analyzeDraft(
            myRole = activeRole,
            allies = allies.filterNotNull(),
            enemies = enemies.filterNotNull(),
            enemyLaneOpponent = explicitEnemyOpponent,
            isFirstPick = isFirstPick
        )
    }

    // FRAME SKIPPING & BACKGROUND PROCESSING STRATEGY:
    // El bucle de visión se ejecuta en un contexto de segundo plano desacoplado (Dispatchers.Default),
    // liberando por completo el hilo principal (UI) para garantizar una tasa de refresco fluida (60-120 FPS)
    // tanto en el live scan overlay (ScannerDebugOverlay) como en las animaciones y gestos de la burbuja.
    // Estrategia de salto de fotogramas (Frame Skipping Strategy):
    // 1. In-flight Concurrency Guard (AtomicBoolean): Si el motor de visión ya se encuentra procesando
    //    una inferencia (OCR o LiteRT), cualquier fotograma entrante o ciclo es descartado de inmediato.
    // 2. User Gesture Throttling: Cuando el usuario arrastra la burbuja (isDraggingBubble) o interactúa
    //    con diálogos modales, la captura se suspende, dedicando el 100% de GPU y CPU a la fluidez táctil.
    // 3. Adaptive Cooldown: Si una inferencia toma más tiempo del previsto, se aplica un intervalo compensatorio
    //    para evitar saturación térmica y mantener estabilidad de fotogramas en Android.
    // 4. Null-Frame Drop: Si el ImageReader no tiene un fotograma nuevo, se salta el ciclo sin re-analizar imágenes estáticas.
    LaunchedEffect(autoScanEnabled) {
        if (!autoScanEnabled) {
            DraftVisionScanner.isVisionEngineBusy.value = false
            return@LaunchedEffect
        }

        withContext(Dispatchers.Default) {
            val isProcessingFrame = java.util.concurrent.atomic.AtomicBoolean(false)
            var loopCycleCounter = 0L
            var fpsLastTime = System.currentTimeMillis()
            var framesInSecond = 0

            while (autoScanEnabled) {
                // 1. REGLA DE SALTO: Arrastre o interacción de usuario
                if (isDraggingBubble || showSaveDraftDialog || showRoleChangeDialog) {
                    DraftVisionScanner.recordFrameSkipped()
                    delay(80L)
                    continue
                }

                // 2. REGLA DE SALTO: In-flight Concurrency Guard (si hay un análisis activo, omitir)
                if (!isProcessingFrame.compareAndSet(false, true)) {
                    DraftVisionScanner.recordFrameSkipped()
                    delay(30L)
                    continue
                }

                loopCycleCounter++
                val cycleStartTime = System.currentTimeMillis()

                // Medición de fotogramas de escaneo por segundo (Scan FPS)
                framesInSecond++
                val nowTime = System.currentTimeMillis()
                if (nowTime - fpsLastTime >= 1000L) {
                    val computedFps = (framesInSecond * 1000f) / (nowTime - fpsLastTime).coerceAtLeast(1L)
                    DraftVisionScanner.updateFps(computedFps)
                    framesInSecond = 0
                    fpsLastTime = nowTime
                }

                try {
                    DraftVisionScanner.isVisionEngineBusy.value = true

                    val confirmedPicksCount = allies.count { it != null } + enemies.count { it != null }
                    val tentativeFirstPick = isFirstPick ?: true
                    val sequence = DraftVisionScanner.getDraftPickSequence(tentativeFirstPick)
                    val activeTurns = DraftVisionScanner.computeActiveSelectionTurns(
                        sequence,
                        DraftVisionScanner.allySlotConfirmedChampions,
                        DraftVisionScanner.enemySlotConfirmedChampions
                    )

                    val isDraftComplete = (confirmedPicksCount >= 10)
                    val hasActiveTurns = activeTurns.isNotEmpty() && !isDraftComplete
                    val isTenthPickActive = activeTurns.any { it.turnNumber == 10 } || confirmedPicksCount >= 8
                    // Garantizar ciclo de sincronización global periódico o continuo si el visor está abierto
                    val isGlobalSyncCycle = com.example.service.screen.DraftSyncCadence.globalCycle(
                        confirmedPicksCount, hasActiveTurns, isTenthPickActive, loopCycleCounter)


                    val dynamicLoopDelay = when {
                        showLiteRTViewer -> 50L // 20 Hz ultra-fluido en vivo para pruebas del usuario
                        isDraftComplete -> 800L
                        !isGlobalSyncCycle && hasActiveTurns -> 50L
                        isTenthPickActive -> 60L
                        else -> 120L
                    }

                    if (screenCaptureManager == null || !screenCaptureManager.isReady()) {
                        withContext(Dispatchers.Main) {
                            scanNoticeMessage = "Permiso de captura inactivo. Toca aquí para activarlo."
                        }
                        delay(500L)
                    } else if (!isScanning) {
                        val bitmap = screenCaptureManager?.captureCurrentFrame()
                        if (bitmap == null || bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) {
                            // Salto de fotograma: no hay imagen nueva disponible en ImageReader
                            DraftVisionScanner.recordFrameSkipped()
                        } else {
                            try {
                                if (!isGlobalSyncCycle && hasActiveTurns) {
                                    // -----------------------------------------------------------------
                                    // RUTA DE ALTA PRIORIDAD: ESCANEO DIRIGIDO DEL SLOT ACTIVO (<30ms)
                                    // -----------------------------------------------------------------
                                    val detectedPicks = mutableListOf<Pair<DraftPickTurn, Champion>>()
                                    for (turn in activeTurns) {
                                        val activeResult = DraftVisionScanner.scanActiveSlotDirectly(bitmap, turn, context, confirmedHudPicks())
                                        val champ = activeResult?.champion
                                        if (champ != null) {
                                            detectedPicks.add(turn to champ)
                                        }
                                    }

                                    if (detectedPicks.isNotEmpty()) {
                                        withContext(Dispatchers.Main) {
                                            var fastPicksAdded = 0
                                            for ((turn, champ) in detectedPicks) {
                                                val before = (allies + enemies).count { it != null }
                                                val scan = com.example.service.screen.DraftScanResult(
                                                    allies = allies.filterNotNull(), enemies = enemies.filterNotNull(),
                                                    isLastPickConfirmed = true, lastPickChampion = champ,
                                                    tenthPickIsAlly = turn.isAlly, tenthPickSlotIndex = turn.slotIndex,
                                                    allyRolesBySlot = DraftVisionScanner.allyRolesBySlotFlow.value,
                                                    isSuccessful = true, statusMessage = ""
                                                )
                                                state.applyConfirmedLastPick(scan)
                                                if ((allies + enemies).count { it != null } > before) {
                                                    (if (turn.isAlly) DraftVisionScanner.allySlotConfirmedChampions
                                                     else DraftVisionScanner.enemySlotConfirmedChampions)[turn.slotIndex] = champ
                                                    fastPicksAdded++
                                                }
                                            }
                                            if (fastPicksAdded > 0) {
                                                val totalAllies = allies.filterNotNull().size
                                                val totalEnemies = enemies.filterNotNull().size
                                                if (totalAllies == 5 && totalEnemies == 5) {
                                                    scanNoticeMessage = "10/10 Campeones confirmados"
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // -----------------------------------------------------------------
                                    // RUTA DE BAJA FRECUENCIA: SINCRONIZACIÓN GLOBAL Y SLOTS INACTIVOS
                                    // -----------------------------------------------------------------
                                    val result = DraftVisionScanner.scanDraftFromBitmap(bitmap, context, if (state.isFirstPickManuallySelected) isFirstPick else null, activeRole, confirmedHudPicks())
                                    if (result.isSuccessful) {
                                        withContext(Dispatchers.Main) {
                                            if (result.detectedFirstPick != null && !state.isFirstPickManuallySelected) {
                                                isFirstPick = result.detectedFirstPick
                                            }

                                            val newAlliesAdded = if (result.allyRolesBySlot.size == 5) syncAlliedHud(result.alliesByRole) else 0
                                            val newEnemiesAdded = syncEnemyHud(result)

                                            commitLastPickToHud(result)

                                            val currentAllyPicks = allies.count { it != null }
                                            val currentEnemyPicks = enemies.count { it != null }
                                            if (!state.isFirstPickManuallySelected) {
                                                if (currentEnemyPicks > 0 && currentAllyPicks == 0) {
                                                    isFirstPick = false
                                                } else if (currentAllyPicks > 0 && currentEnemyPicks == 0) {
                                                    isFirstPick = true
                                                }
                                            }

                                            if (result.isLegendaryRanked) {
                                                if (!isLegendaryQueue) {
                                                    isLegendaryQueue = true
                                                }
                                                if (state.allySummonerNames.isNotEmpty()) {
                                                    state.allySummonerNames.clear()
                                                }
                                            } else {
                                                defaultRoles.forEachIndexed { idx, role ->
                                                    val sName = result.allySummonerNamesByRole[role]
                                                    if (!sName.isNullOrBlank()) {
                                                        state.allySummonerNames[idx] = sName
                                                    } else {
                                                        state.allySummonerNames.remove(idx)
                                                    }
                                                    val spells = result.allySpellsByRole[role]
                                                    if (!spells.isNullOrEmpty()) {
                                                        state.allySpells[idx] = spells
                                                    } else {
                                                        state.allySpells.remove(idx)
                                                    }
                                                }
                                            }
                                            if (state.enemySpells.isNotEmpty()) {
                                                state.enemySpells.clear()
                                            }

                                            val finalAlliesPicked = allies.filterNotNull().size
                                            val finalEnemiesPicked = enemies.filterNotNull().size
                                            val isDraftFullyConfirmed = (finalAlliesPicked == 5 && finalEnemiesPicked == 5)

                                            if (result.userExplicitlyDetectedRole != null && activeRole != result.userExplicitlyDetectedRole) {
                                                activeRole = result.userExplicitlyDetectedRole
                                                com.example.util.UserPreferences.setActiveDraftRole(context, result.userExplicitlyDetectedRole)
                                                scanNoticeMessage = "Auto-Scan: Tu rol detectado (${result.userExplicitlyDetectedRole.shortName})"
                                            } else if (isDraftFullyConfirmed) {
                                                scanNoticeMessage = "10/10 Campeones confirmados"
                                                AppLogger.i("FloatingService", "10/10 confirmados: seguimiento de intercambios activo.")
                                            } else if (result.isPreparationPhase && (finalAlliesPicked < 5 || finalEnemiesPicked < 5)) {
                                                scanNoticeMessage = "Fase de Preparación: completando selección ($finalAlliesPicked/5 vs $finalEnemiesPicked/5)..."
                                            } else if (newAlliesAdded > 0 || newEnemiesAdded > 0) {
                                                scanNoticeMessage = "Auto-Scan: +${newAlliesAdded + newEnemiesAdded} picks detectados ($finalAlliesPicked/5 vs $finalEnemiesPicked/5)"
                                            }

                                            if (scanNoticeMessage != null) {
                                                coroutineScope.launch {
                                                    delay(2000)
                                                    scanNoticeMessage = null
                                                }
                                            }
                                        }
                                    }
                                }
                                val processDuration = System.currentTimeMillis() - cycleStartTime
                                DraftVisionScanner.recordFrameProcessed(processDuration)
                            } finally {
                                try {
                                    bitmap.recycle()
                                } catch (_: Throwable) {}
                            }
                        }
                    }

                    val totalCycleDuration = System.currentTimeMillis() - cycleStartTime
                    // 3. ESTRATEGIA ADAPTATIVA: Si el ciclo tomó más tiempo del previsto,
                    // compensar el retraso para evitar ráfagas y proteger la tasa de fotogramas del live overlay
                    val compensatedDelay = (dynamicLoopDelay - totalCycleDuration).coerceAtLeast(30L)
                    delay(compensatedDelay)

                } catch (t: Throwable) {
                    AppLogger.e("FloatingService", "Error in auto-scan loop", t)
                    delay(200L)
                } finally {
                    isProcessingFrame.set(false)
                    DraftVisionScanner.isVisionEngineBusy.value = false
                }
            }
        }
    }

    fun triggerManualScan() {
        if (screenCaptureManager?.isReady() != true) {
            scanNoticeMessage = "Requiere permiso de pantalla. Abriendo solicitud..."
            try {
                val reqIntent = Intent(context, com.example.MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra("EXTRA_REQUEST_CAPTURE", true)
                }
                context.startActivity(reqIntent)
            } catch (_: Exception) {}
            coroutineScope.launch {
                delay(3500)
                scanNoticeMessage = null
            }
            return
        }
        isScanning = true
        scanNoticeMessage = "Escaneando selección en directo..."
        coroutineScope.launch(Dispatchers.IO) {
            val bitmap = screenCaptureManager?.captureCurrentFrame()
            if (bitmap != null) {
                val result = DraftVisionScanner.scanDraftFromBitmap(bitmap, context, if (state.isFirstPickManuallySelected) isFirstPick else null, activeRole, confirmedHudPicks())
                withContext(Dispatchers.Main) {
                    if (result.isSuccessful) {
                        // Sincronizar primera selección si se detectó y no ha sido fijada manualmente
                        if (result.detectedFirstPick != null && !state.isFirstPickManuallySelected) {
                            isFirstPick = result.detectedFirstPick
                        }

                        // 1. Asignación directa y de alta precisión por rol (respetando selecciones manuales)
                        if (result.allyRolesBySlot.size == 5) syncAlliedHud(result.alliesByRole)
                        syncEnemyHud(result)

                        commitLastPickToHud(result)

                        // Verificación complementaria: si el rival ya tiene picks y aliados no, rival eligió 1º
                        val currentAllyPicks = allies.count { it != null }
                        val currentEnemyPicks = enemies.count { it != null }
                        if (!state.isFirstPickManuallySelected) {
                            if (currentEnemyPicks > 0 && currentAllyPicks == 0) {
                                isFirstPick = false
                            } else if (currentAllyPicks > 0 && currentEnemyPicks == 0) {
                                isFirstPick = true
                            }
                        }

                        // Sincronizar nombres de invocador aliados y hechizos
                        if (result.isLegendaryRanked) {
                            state.allySummonerNames.clear()
                        } else {
                            defaultRoles.forEachIndexed { idx, role ->
                                val sName = result.allySummonerNamesByRole[role]
                                if (!sName.isNullOrBlank()) {
                                    state.allySummonerNames[idx] = sName
                                } else {
                                    state.allySummonerNames.remove(idx)
                                }
                                val spells = result.allySpellsByRole[role]
                                if (!spells.isNullOrEmpty()) {
                                    state.allySpells[idx] = spells
                                } else {
                                    state.allySpells.remove(idx)
                                }
                            }
                        }
                        state.enemySpells.clear()

                        val totalAlliesPicked = allies.filterNotNull().size
                        val totalEnemiesPicked = enemies.filterNotNull().size

                        // Fase de confirmación de picks finalizada mediante escaneo local y OCR
                        if (result.detectedRole != null) {
                            activeRole = result.detectedRole
                            com.example.util.UserPreferences.setActiveDraftRole(context, result.detectedRole)
                        }
                        val totalDetected = allies.filterNotNull().size + enemies.filterNotNull().size
                        scanNoticeMessage = if (allies.filterNotNull().size == 5 && enemies.filterNotNull().size == 5) {
                            "10/10 Campeones confirmados"
                        } else {
                            "Escaneo exitoso ($totalDetected picks" +
                                (if (result.detectedRole != null) ", tu rol: ${result.detectedRole.shortName})" else ")")
                        }
                    } else {
                        scanNoticeMessage = result.statusMessage
                    }
                    isScanning = false
                }
            } else {
                withContext(Dispatchers.Main) {
                    scanNoticeMessage = "No hay frame de captura disponible"
                    isScanning = false
                }
            }
            delay(3500)
            scanNoticeMessage = null
        }
    }

    Box(modifier = Modifier.padding(2.dp)) {
        Column(horizontalAlignment = Alignment.Start) {
            if (!isExpanded) {
                // Minimized Floating Bubble
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val bubbleBorderColor by animateColorAsState(
                        targetValue = if (isNearCloseThreshold) DangerRed else (if (isScanning) HextechCyan else HextechGold),
                        animationSpec = tween(200)
                    )

                    Box(
                        modifier = Modifier
                            .size(if (isCompactBubble) 36.dp else 46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(HextechCyan, Color(0xFF005A82), HextechDarkBg)
                                )
                            )
                            .border(2.5.dp, if (isScanning) HextechCyan else HextechGold, CircleShape)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = {
                                        isDraggingBubble = true
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        onDragDelta(dragAmount.x.roundToInt(), dragAmount.y.roundToInt(), true, false)
                                    },
                                    onDragEnd = {
                                        isDraggingBubble = false
                                        onDragDelta(0, 0, false, false)
                                    },
                                    onDragCancel = {
                                        isDraggingBubble = false
                                        onDragDelta(0, 0, false, false)
                                    }
                                )
                            }
                            .clickable {
                                isExpanded = true
                                onExpandedChange(true)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(if (isCompactBubble) 28.dp else 36.dp),
                                color = HextechCyan,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Image(
                                painter = painterResource(id = com.example.R.drawable.ic_overlay_logo),
                                contentDescription = "Wild Rift Drafting Coach",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(if (isCompactBubble) 32.dp else 42.dp)
                                    .clip(CircleShape)
                            )
                        }

                        if (!isScanning) {
                            // Pulsing green auto-scan indicator
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .align(Alignment.TopEnd)
                                    .clip(CircleShape)
                                    .background(if (autoScanEnabled && isAdmin) Color(0xFF00FF7F) else HextechGold)
                            )
                        }
                    }

                    // Indicador sutil de arrastre
                    if (isDraggingBubble) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tr("↓ Arrastra al círculo inferior para cerrar"),
                                color = TextSecondary,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    val isBubbleLiveVisionActive by com.example.service.screen.DraftVisionScanner.showCalibrationBoxes.collectAsStateWithLifecycle()
                    if (isBubbleLiveVisionActive && !isDraggingBubble) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Surface(
                            modifier = Modifier
                                .clickable {
                                    com.example.service.screen.DraftVisionScanner.showCalibrationBoxes.value = false
                                }
                                .testTag("btn_bubble_live_vision_off"),
                            shape = RoundedCornerShape(8.dp),
                            color = HextechDarkBg.copy(alpha = 0.9f),
                            border = BorderStroke(1.dp, HextechCyan)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(HextechCyan)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = com.example.util.tr("VISIÓN"),
                                    color = HextechCyan,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Expanded Drafting Hub
            AnimatedVisibility(
                visible = isExpanded,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                var isDraggingPanel by remember { mutableStateOf(false) }

                val targetCardHeight = if (isLandscapeMode) 345.dp else 520.dp
                Card(
                    modifier = Modifier
                        .widthIn(min = if (isLandscapeMode) 520.dp else 300.dp, max = if (isLandscapeMode) 560.dp else 340.dp)
                        .height(targetCardHeight)
                        .clip(RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, HextechGold)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                        // Header con barra de arrastre para reposicionar el Hub cómodamente
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = {
                                            isDraggingPanel = true
                                            dragAccumulatedY = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragAccumulatedY += dragAmount.y
                                            onDragDelta(dragAmount.x.roundToInt(), dragAmount.y.roundToInt(), true, false)
                                        },
                                        onDragEnd = {
                                            isDraggingPanel = false
                                            onDragDelta(0, 0, false, false)
                                            dragAccumulatedY = 0f
                                        },
                                        onDragCancel = {
                                            isDraggingPanel = false
                                            dragAccumulatedY = 0f
                                            onDragDelta(0, 0, false, false)
                                        }
                                    )
                                }
                                .padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = com.example.R.drawable.ic_overlay_logo),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, HextechGold, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(com.example.util.tr("COACH"), color = HextechGold, fontWeight = FontWeight.Black, fontSize = 11.5.sp, maxLines = 1)
                                    val isCaptureReady = screenCaptureManager?.isReady() == true
                                    val indicatorColor = when {
                                        !isCaptureReady -> Color(0xFFFFB300)
                                        autoScanEnabled -> Color(0xFF00FF7F)
                                        else -> HextechGold
                                    }
                                    val indicatorText = when {
                                        !isCaptureReady -> tr("Sin permiso")
                                        autoScanEnabled -> tr("Auto-Scan")
                                        else -> tr("Manual")
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            if (!isCaptureReady) {
                                                try {
                                                    val reqIntent = Intent(context, com.example.MainActivity::class.java).apply {
                                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                                        putExtra("EXTRA_REQUEST_CAPTURE", true)
                                                    }
                                                    context.startActivity(reqIntent)
                                                } catch (_: Exception) {}
                                            }
                                        }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(indicatorColor)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = com.example.util.tr(indicatorText),
                                            color = indicatorColor,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val isLiveVisionActive by com.example.service.screen.DraftVisionScanner.showCalibrationBoxes.collectAsStateWithLifecycle()

                                // 1. Botón de Visión en Vivo (Solo icono de ojo, sin texto)
                                Surface(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clickable {
                                            com.example.service.screen.DraftVisionScanner.showCalibrationBoxes.value = !isLiveVisionActive
                                        }
                                        .testTag("btn_live_vision_toggle"),
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isLiveVisionActive) HextechCyan.copy(alpha = 0.35f) else Color(0xFF1E293B),
                                    border = BorderStroke(1.dp, if (isLiveVisionActive) HextechCyan else HextechCyan.copy(alpha = 0.6f))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isLiveVisionActive) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = com.example.util.trNullable("Visión"),
                                            tint = if (isLiveVisionActive) HextechCyan else TextSecondary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                // 2. Botón de Depurado LiteRT (Solo icono)
                                Surface(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clickable {
                                            autoScanEnabled = true
                                            showLiteRTViewer = true
                                        }
                                        .testTag("btn_debug_overlay"),
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (showLiteRTViewer) HextechCyan.copy(alpha = 0.35f) else Color(0xFF1E293B),
                                    border = BorderStroke(1.dp, if (showLiteRTViewer) HextechCyan else HextechCyan.copy(alpha = 0.6f))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.BugReport,
                                            contentDescription = com.example.util.trNullable("Depurado"),
                                            tint = HextechCyan,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                // 3. Botón Minimizar (a Burbuja flotante) - Visible, resaltado y siempre asegurado
                                Surface(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clickable {
                                            isExpanded = false
                                            onExpandedChange(false)
                                        }
                                        .testTag("btn_minimize_hub"),
                                    shape = RoundedCornerShape(6.dp),
                                    color = HextechGold.copy(alpha = 0.2f),
                                    border = BorderStroke(1.2.dp, HextechGold)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = com.example.util.trNullable("Minimizar"),
                                            tint = HextechGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Sub-Header con Pestañas de Navegación del Hub
                        AnimatedVisibility(
                            visible = !isOverlayTabsMinimized
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                            // Pestaña 1: Draft Coach
                            val isDraftActive = overlayHubTab == OverlayHubTab.DRAFT
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isDraftActive) HextechCyan.copy(alpha = 0.2f) else HextechSurface)
                                    .border(
                                        1.dp,
                                        if (isDraftActive) HextechCyan else HextechCardBorder.copy(alpha = 0.5f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { overlayHubTab = OverlayHubTab.DRAFT }
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = if (isDraftActive) HextechCyan else TextMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = com.example.util.tr("Draft"),
                                        color = if (isDraftActive) HextechCyan else TextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = if (isDraftActive) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }

                            // Pestaña 2: Tier & Builds
                            val isTierActive = overlayHubTab == OverlayHubTab.TIER_LIST
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isTierActive) HextechGold.copy(alpha = 0.2f) else HextechSurface)
                                    .border(
                                        1.dp,
                                        if (isTierActive) HextechGold else HextechCardBorder.copy(alpha = 0.5f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { overlayHubTab = OverlayHubTab.TIER_LIST }
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = if (isTierActive) HextechGold else TextMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = com.example.util.tr("Tiers"),
                                        color = if (isTierActive) HextechGold else TextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = if (isTierActive) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }

                            // Pestaña 3: Campeones
                            val isChampsActive = overlayHubTab == OverlayHubTab.CHAMPIONS
                            Box(
                                modifier = Modifier
                                    .weight(1.1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isChampsActive) HextechCyan.copy(alpha = 0.2f) else HextechSurface)
                                    .border(
                                        1.dp,
                                        if (isChampsActive) HextechCyan else HextechCardBorder.copy(alpha = 0.5f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { overlayHubTab = OverlayHubTab.CHAMPIONS }
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = if (isChampsActive) HextechCyan else TextMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = com.example.util.tr("Champs"),
                                        color = if (isChampsActive) HextechCyan else TextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = if (isChampsActive) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }

                            // Pestaña 4: Historial
                            if (isLoggedInAndPremium) {
                                val isHistoryActive = overlayHubTab == OverlayHubTab.HISTORY
                                Box(
                                    modifier = Modifier
                                        .weight(0.85f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isHistoryActive) Color(0xFF00FF7F).copy(alpha = 0.15f) else HextechSurface)
                                        .border(
                                            1.dp,
                                            if (isHistoryActive) Color(0xFF00FF7F) else HextechCardBorder.copy(alpha = 0.5f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { overlayHubTab = OverlayHubTab.HISTORY }
                                        .padding(vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.History,
                                            contentDescription = null,
                                            tint = if (isHistoryActive) Color(0xFF00FF7F) else TextMuted,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = com.example.util.tr("Hist"),
                                            color = if (isHistoryActive) Color(0xFF00FF7F) else TextMuted,
                                            fontSize = 9.5.sp,
                                            fontWeight = if (isHistoryActive) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                        }

                        if (isOverlayTabsMinimized) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(HextechSurface)
                                    .border(1.dp, HextechGold.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .clickable { isOverlayTabsMinimized = false }
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = com.example.util.tr("Pestaña: ${when (overlayHubTab) {
                                        OverlayHubTab.DRAFT -> "Draft Coach"
                                        OverlayHubTab.TIER_LIST -> "Tiers & Builds"
                                        OverlayHubTab.CHAMPIONS -> "Campeones"
                                        OverlayHubTab.HISTORY -> "Historial & Perfiles"
                                    }}"),
                                    color = HextechGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(com.example.util.tr("Mostrar barra"), color = HextechCyan, fontSize = 9.sp)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(12.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        // Banner de estado de escaneo si existe
                        if (scanNoticeMessage != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(HextechSurface)
                                    .border(1.dp, HextechCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .clickable {
                                        if (scanNoticeMessage?.contains("Permiso", ignoreCase = true) == true) {
                                            try {
                                                val intent = Intent(context, com.example.MainActivity::class.java).apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                                    putExtra("EXTRA_REQUEST_CAPTURE", true)
                                                }
                                                context.startActivity(intent)
                                            } catch (_: Exception) {}
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = com.example.util.tr(scanNoticeMessage ?: ""),
                                    color = HextechCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // Contenido Principal del Hub según la Pestaña Activa o Detalle de Campeón
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            if (selectedChampionDetail != null) {
                                com.example.ui.screens.ChampionDetailSheet(
                                    isOverlay = true,
                                    champion = selectedChampionDetail,
                                    onDismiss = { selectedChampionDetail = null },
                                    onChampionSelected = { selectedChampionDetail = it }
                                )
                            } else {
                                when (overlayHubTab) {
                                    OverlayHubTab.DRAFT -> {
                                        FloatingDraftCoachView(
                                            isLandscapeMode = isLandscapeMode,
                                            activeRole = activeRole,
                                            onActiveRoleChange = {
                                                activeRole = it
                                                state.isRoleManuallySelected = true
                                                com.example.util.UserPreferences.setActiveDraftRole(context, it)
                                            },
                                            isFirstPick = isFirstPick,
                                            onFirstPickToggle = {
                                                state.isFirstPickManuallySelected = true
                                                isFirstPick = !isFirstPick
                                            },
                                            isLegendaryQueue = isLegendaryQueue,
                                            onToggleLegendaryQueue = { isLegendaryQueue = !isLegendaryQueue },
                                            isLoadingScreenMode = isLoadingScreenMode,
                                            onLoadingScreenModeToggle = { isLoadingScreenMode = !isLoadingScreenMode },
                                            allies = allies,
                                            enemies = enemies,
                                            enemyConfidences = state.enemyConfidences,
                                            allySummonerNames = state.allySummonerNames,
                                            enemySummonerNames = state.enemySummonerNames,
                                            allySpells = state.allySpells,
                                            enemySpells = state.enemySpells,
                                            analysis = analysis,
                                            selectedChampionDetail = selectedChampionDetail,
                                            onSelectChampion = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); selectedChampionDetail = it },
                                            onOpenChampionPicker = { isAlly, idx ->
                                                autoScanEnabled = false
                                                showChampionPickerForSlot = Pair(isAlly, idx)
                                            },
                                             onSaveDraftClick = {
                                                if (isPremium) {
                                                    if (!state.isRoleManuallySelected) {
                                                        android.widget.Toast.makeText(context, trStr(currentLang, "Selecciona tu línea primero"), android.widget.Toast.LENGTH_SHORT).show()
                                                    } else if (allies.count { it != null } < 5 || enemies.count { it != null } < 5) {
                                                        android.widget.Toast.makeText(context, trStr(currentLang, "Debes seleccionar los 10 campeones"), android.widget.Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        showSaveDraftDialog = true
                                                    }
                                                } else {
                                                    android.widget.Toast.makeText(context, trStr(currentLang, "Requiere suscripción Premium"), android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            isSavedRecently = isSavedRecently,
                                            onClearAll = {
                                                for (i in 0 until 5) {
                                                    allies[i] = null
                                                    enemies[i] = null
                                                }
                                                manualLockedAllySlots.clear()
                                                manualLockedEnemySlots.clear()
                                                state.enemyConfidences.clear()
                                                state.allySummonerNames.clear()
                                                state.enemySummonerNames.clear()
                                                state.allySpells.clear()
                                                state.enemySpells.clear()
                                                state.isRoleManuallySelected = false
                                                state.isFirstPickManuallySelected = false
                                                DraftVisionScanner.resetSlotMemory()
                                                android.widget.Toast.makeText(context, com.example.util.appTr("Equipos vaciados"), android.widget.Toast.LENGTH_SHORT).show()
                                            },
                                            onManualEdit = { autoScanEnabled = false },
                                            onOpenLiteRTViewer = {
                                                autoScanEnabled = true
                                                showLiteRTViewer = true
                                            }
                                        )
                                    }
                                    OverlayHubTab.TIER_LIST -> {
                                        com.example.ui.screens.TierListTab(
                                            isOverlay = true,
                                            onSelectChampion = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); selectedChampionDetail = it },
                                            isPremium = isPremium
                                        )
                                    }
                                    OverlayHubTab.CHAMPIONS -> {
                                        com.example.ui.screens.ChampionsCatalogTab(
                                            isOverlay = true,
                                            onSelectChampion = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); selectedChampionDetail = it }
                                        )
                                    }
                                    OverlayHubTab.HISTORY -> {
                                        com.example.ui.screens.DraftHistoryScreen(
                                            isOverlay = true,
                                            onNavigateBack = {
                                                overlayHubTab = OverlayHubTab.DRAFT
                                            },
                                            onLoadDraft = { loadedAllies, loadedEnemies, role, isFirst ->
                                                for (i in 0 until 5) {
                                                    allies[i] = loadedAllies.getOrNull(i)?.champion
                                                    enemies[i] = loadedEnemies.getOrNull(i)?.champion
                                                }
                                                activeRole = role
                                                isFirstPick = isFirst
                                                overlayHubTab = OverlayHubTab.DRAFT
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Footer con acciones y estado
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = com.example.util.tr("✕ " + tr("Detener Asistente")),
                                color = DangerRed,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { onClose() }
                                    .padding(4.dp)
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = tr("Auto-Scan"),
                                    color = TextMuted,
                                    fontSize = 9.5.sp,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Switch(
                                    checked = autoScanEnabled,
                                    enabled = true,
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) {
                                            autoScanEnabled = true
                                            DraftVisionScanner.resetSlotMemory()
                                            if (screenCaptureManager?.isReady() != true) {
                                                scanNoticeMessage = "Requiere permiso de pantalla. Toca aquí para activarlo."
                                                try {
                                                    val reqIntent = Intent(context, com.example.MainActivity::class.java).apply {
                                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                                        putExtra("EXTRA_REQUEST_CAPTURE", true)
                                                    }
                                                    context.startActivity(reqIntent)
                                                } catch (_: Exception) {}
                                            }
                                        } else {
                                            autoScanEnabled = false
                                        }
                                    },
                                    modifier = Modifier.scale(0.7f),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = HextechDarkBg,
                                        checkedTrackColor = Color(0xFF00FF7F),
                                        disabledCheckedTrackColor = TextMuted.copy(alpha = 0.3f),
                                        disabledUncheckedTrackColor = HextechSurface
                                    )
                                )
                            }
                            }
                        }
                    }

                    } // close Box
                } // close Card
            } // close AnimatedVisibility
        } // close Column

    // Modal de selección de rol
    if (showRoleChangeDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable { showRoleChangeDialog = false },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clickable { /* no-op */ },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HextechSurfaceVariant),
                border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tr("Selecciona tu Línea"),
                        color = HextechGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LaneRole.entries.forEach { role ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (role == activeRole) HextechCyan.copy(alpha = 0.2f) else HextechSurface)
                                .border(1.dp, if (role == activeRole) HextechCyan else HextechCardBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    activeRole = role
                                    com.example.util.UserPreferences.setActiveDraftRole(context, role)
                                    showRoleChangeDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = role.iconResId),
                                contentDescription = null,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = tr(role.displayName),
                                color = if (role == activeRole) HextechCyan else TextPrimary,
                                fontWeight = if (role == activeRole) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // Modal para Guardar Partida en Base de Datos Room
    if (showSaveDraftDialog) {
        FloatingSaveMatchDialog(
            activeRole = activeRole,
            isFirstPick = isFirstPick,
            isLegendary = isLegendaryQueue,
            allies = allies.mapIndexedNotNull { index, champ ->
                champ?.let {
                    val role = when (index) {
                        0 -> LaneRole.TOP
                        1 -> LaneRole.JUNGLE
                        2 -> LaneRole.MID
                        3 -> LaneRole.ADC
                        else -> LaneRole.SUPPORT
                    }
                    DraftSlot(it, role)
                }
            },
            enemies = enemies.mapIndexedNotNull { index, champ ->
                champ?.let {
                    val role = when (index) {
                        0 -> LaneRole.TOP
                        1 -> LaneRole.JUNGLE
                        2 -> LaneRole.MID
                        3 -> LaneRole.ADC
                        else -> LaneRole.SUPPORT
                    }
                    DraftSlot(it, role)
                }
            },
            analysis = analysis,
            onDismiss = { showSaveDraftDialog = false },
            onSaved = {
                isSavedRecently = true
                showSaveDraftDialog = false
                overlayHubTab = OverlayHubTab.HISTORY
            }
        )
    }

    // Modal del Visor Reconocimiento visual local para el 10º Pick
    if (showLiteRTViewer) {
        com.example.ui.components.LiteRTEngineViewerDialog(
            onDismissRequest = { showLiteRTViewer = false }
        )
    }

    // Modal de selección rápida de campeón si el usuario toca un slot manual
    if (showChampionPickerForSlot != null) {
        val (isAllySlot, slotIndex) = showChampionPickerForSlot!!
        val targetRole = defaultRoles.getOrNull(slotIndex)
        var selectedRoleFilter by remember { mutableStateOf<LaneRole?>(null) }
        var searchChampQuery by remember { mutableStateOf("") }
        val currentChampInSlot = if (isAllySlot) allies.getOrNull(slotIndex)?.id else enemies.getOrNull(slotIndex)?.id
        val alreadySelectedIds = remember(allies.toList(), enemies.toList(), slotIndex, isAllySlot) {
            val set = (allies.filterNotNull().map { it.id } + enemies.filterNotNull().map { it.id }).toMutableSet()
            if (currentChampInSlot != null) {
                set.remove(currentChampInSlot)
            }
            set
        }

        val filteredList = remember(searchChampQuery, alreadySelectedIds, selectedRoleFilter, targetRole) {
            WildRiftRepository.champions.filter { champ ->
                val notSelected = !alreadySelectedIds.contains(champ.id)
                val matchesQuery = searchChampQuery.isBlank() || champ.name.contains(searchChampQuery, ignoreCase = true) || champ.summary.contains(searchChampQuery, ignoreCase = true)
                val matchesRole = selectedRoleFilter == null || champ.primaryRole == selectedRoleFilter || champ.secondaryRoles.contains(selectedRoleFilter)
                notSelected && matchesQuery && matchesRole
            }.sortedWith(
                compareByDescending<Champion> { selectedRoleFilter != null && it.primaryRole == selectedRoleFilter }
                    .thenByDescending { selectedRoleFilter == null && targetRole != null && (it.primaryRole == targetRole || it.secondaryRoles.contains(targetRole)) }
                    .thenByDescending { it.tier == "S+" }
                    .thenByDescending { it.tier == "S" }
                    .thenBy { it.name }
            )
        }

        Box(
            modifier = Modifier
                .widthIn(min = 280.dp, max = 330.dp)
                .heightIn(min = 340.dp, max = 460.dp)
                .padding(4.dp)
                .pointerInput(Unit) { },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(390.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isAllySlot) AllyBlue else DangerRed)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = com.example.util.tr((if (isAllySlot) tr("Elegir Aliado") else tr("Elegir Rival")) + (if (targetRole != null) " - ${com.example.util.tr(targetRole.displayName)}" else "")),
                            color = if (isAllySlot) AllyBlue else DangerRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                        IconButton(onClick = { showChampionPickerForSlot = null }, modifier = Modifier.size(22.dp)) {
                            Icon(Icons.Default.Close, contentDescription = tr("Cerrar"), tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Buscador Compacto y Proporcionado
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(HextechSurface)
                            .border(1.dp, HextechCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchChampQuery.isEmpty()) {
                                    Text(tr("Buscar campeón..."), color = TextMuted, fontSize = 10.5.sp)
                                }
                                androidx.compose.foundation.text.BasicTextField(
                                    value = searchChampQuery,
                                    onValueChange = { searchChampQuery = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 10.5.sp),
                                    singleLine = true,
                                    cursorBrush = androidx.compose.ui.graphics.SolidColor(HextechCyan)
                                )
                            }
                            if (searchChampQuery.isNotEmpty()) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = com.example.util.trNullable("Limpiar"),
                                    tint = TextMuted,
                                    modifier = Modifier.size(12.dp).clickable { searchChampQuery = "" }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Selector de Líneas / Filtro Flexible por Rol
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        val filterOptions = listOf<LaneRole?>(null, LaneRole.TOP, LaneRole.JUNGLE, LaneRole.MID, LaneRole.ADC, LaneRole.SUPPORT)
                        filterOptions.forEach { lane ->
                            val isSel = selectedRoleFilter == lane
                            val label = lane?.let { com.example.util.tr(it.shortName) } ?: tr("Todos")
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSel) HextechCyan else HextechSurface)
                                    .border(0.5.dp, if (isSel) HextechGold else HextechCardBorder, RoundedCornerShape(4.dp))
                                    .clickable { selectedRoleFilter = lane }
                                    .padding(vertical = 3.dp, horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (lane != null) {
                                        Image(
                                            painter = painterResource(id = lane.iconResId),
                                            contentDescription = null,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                    }
                                    Text(
                                        text = com.example.util.tr(label),
                                        fontSize = 8.sp,
                                        fontWeight = if (isSel) FontWeight.Black else FontWeight.Medium,
                                        color = if (isSel) HextechDarkBg else TextPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        items(filteredList) { champ ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(HextechSurface)
                                    .clickable {
                                        if (isAllySlot) {
                                            manualLockedAllySlots[slotIndex] = true
                                            for (i in 0 until 5) {
                                                if (allies[i]?.id == champ.id) allies[i] = null
                                                if (enemies[i]?.id == champ.id) enemies[i] = null
                                            }
                                            if (slotIndex in 0 until 5) {
                                                allies[slotIndex] = champ
                                            }
                                        } else {
                                            manualLockedEnemySlots[slotIndex] = true
                                            for (i in 0 until 5) {
                                                if (allies[i]?.id == champ.id) allies[i] = null
                                                if (enemies[i]?.id == champ.id) enemies[i] = null
                                            }
                                            if (slotIndex in 0 until 5) {
                                                enemies[slotIndex] = champ
                                            }
                                        }
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showChampionPickerForSlot = null
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ChampionAvatar(champion = champ, size = 26.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(com.example.util.tr(champ.name), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                                    Text(
                                        text = com.example.util.tr(champ.primaryRole.displayName),
                                        color = TextMuted,
                                        fontSize = 8.5.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(HextechGold.copy(alpha = 0.15f))
                                        .border(0.5.dp, HextechGold.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(com.example.util.tr(champ.tier), color = HextechGold, fontWeight = FontWeight.Black, fontSize = 9.sp)
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}
