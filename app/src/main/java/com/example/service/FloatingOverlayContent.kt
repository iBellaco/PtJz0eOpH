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
    val detectedBans by DraftVisionScanner.bannedChampionIdsFlow.collectAsStateWithLifecycle()
    val banSlots by DraftVisionScanner.banSlotsFlow.collectAsStateWithLifecycle()
    val bannedIds = detectedBans
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

    val syncAlliedHud: (com.example.service.screen.DraftScanResult) -> Int = { state.syncScannedAllies(it) }

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
                    isLastPickConfirmed = true, isLastPickFinalized=report.isSelectionFinalized, lastPickChampion = report.pickedChampion,
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

    val analysis = remember(activeRole, isFirstPick, allies.toList(), enemies.toList(), explicitEnemyOpponent, bannedIds) {
        WildRiftRepository.analyzeDraft(
            myRole = activeRole,
            allies = allies.filterNotNull(),
            enemies = enemies.filterNotNull(),
            enemyLaneOpponent = explicitEnemyOpponent,
            isFirstPick = isFirstPick,
            bannedChampionIds = bannedIds
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
        if (autoScanEnabled) {
            DraftVisionScanner.resetSlotLifecycle()
        }
        if (!autoScanEnabled) {
            DraftVisionScanner.isVisionEngineBusy.value = false
            return@LaunchedEffect
        }

        withContext(Dispatchers.Default) {
            val isProcessingFrame = java.util.concurrent.atomic.AtomicBoolean(false)
            var lastGlobalSyncFinishedAt = android.os.SystemClock.elapsedRealtime()
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
                    val unresolvedTurns = DraftVisionScanner.computeActiveSelectionTurns(
                        sequence,
                        DraftVisionScanner.allySlotConfirmedChampions,
                        DraftVisionScanner.enemySlotConfirmedChampions
                    )

                    val isDraftComplete = (confirmedPicksCount >= 10)
                    val activeTurns = if (isDraftComplete) listOf(sequence.last()) else unresolvedTurns
                    val hasActiveTurns = activeTurns.isNotEmpty()
                    val isTenthPickActive = activeTurns.any { it.turnNumber == 10 } || confirmedPicksCount >= 8
                    // La navegación y las cajas de diagnóstico no cambian la cadencia del escaneo.
                    val isGlobalSyncCycle = com.example.service.screen.DraftSyncCadence.metadataDue(
                        android.os.SystemClock.elapsedRealtime(),lastGlobalSyncFinishedAt)


                    val dynamicLoopDelay = when {
                        !isGlobalSyncCycle && hasActiveTurns -> 50L
                        isTenthPickActive -> 60L
                        else -> 120L
                    }

                    if (screenCaptureManager == null || !screenCaptureManager.isReady()) {
                        withContext(Dispatchers.Main) {
                                            if (!autoScanEnabled) return@withContext
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
                                val slotObservation = DraftVisionScanner.observeSelectionSlots(bitmap, confirmedPicksCount)
                                if (slotObservation != com.example.service.screen.DraftSlotLifecycle.Observation.ACTIVE) {
                                    if (slotObservation == com.example.service.screen.DraftSlotLifecycle.Observation.FINISHED) {
                                        withContext(Dispatchers.Main) {
                                            if (!autoScanEnabled) return@withContext
                                            val finalReport = com.example.service.screen.LiteRTVisionClassifier.finishLastVisibleSelection()
                                            if (finalReport?.pickedChampion != null) {
                                                state.applyConfirmedLastPick(com.example.service.screen.DraftScanResult(
                                                    allies = allies.filterNotNull(), enemies = enemies.filterNotNull(),
                                                    isLastPickConfirmed = true, isLastPickFinalized=true, lastPickChampion = finalReport.pickedChampion,
                                                    tenthPickIsAlly = finalReport.slotDescription.startsWith("Aliado"),
                                                    tenthPickSlotIndex = 4,
                                                    allyRolesBySlot = DraftVisionScanner.allyRolesBySlotFlow.value,
                                                    isSuccessful = true, statusMessage = ""
                                                ))
                                            }
                                            autoScanEnabled = false
                                        }
                                    }
                                } else if (!isGlobalSyncCycle && confirmedPicksCount<9) {
                                    val result=DraftVisionScanner.scanDraftFromBitmap(bitmap,context,
                                        if(state.isFirstPickManuallySelected) isFirstPick else null,activeRole,
                                        confirmedHudPicks(),namesOnly=true)
                                    if(result.isSuccessful) withContext(Dispatchers.Main) {
                                        if(!autoScanEnabled) return@withContext
                                        if(result.detectedFirstPick!=null && !state.isFirstPickManuallySelected)
                                            isFirstPick=result.detectedFirstPick
                                        syncAlliedHud(result)
                                        syncEnemyHud(result)
                                        commitLastPickToHud(result)
                                        state.stopAutomaticScanIfComplete(result.isLastPickFinalized)
                                    }
                                } else if (!isGlobalSyncCycle && hasActiveTurns) {
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
                                            if (!autoScanEnabled) return@withContext
                                            var fastPicksAdded = 0
                                            for ((turn, champ) in detectedPicks) {
                                                val before = (allies + enemies).count { it != null }
                                                val scan = com.example.service.screen.DraftScanResult(
                                                    allies = allies.filterNotNull(), enemies = enemies.filterNotNull(),
                                                    isLastPickConfirmed = true,
                                                    isLastPickFinalized=com.example.service.screen.LiteRTVisionClassifier.reportFlow.value.isSelectionFinalized,
                                                    lastPickChampion = champ,
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
                                                    state.stopAutomaticScanIfComplete(com.example.service.screen.LiteRTVisionClassifier.reportFlow.value.isSelectionFinalized)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // -----------------------------------------------------------------
                                    // RUTA DE BAJA FRECUENCIA: SINCRONIZACIÓN GLOBAL Y SLOTS INACTIVOS
                                    // -----------------------------------------------------------------
                                    val result = DraftVisionScanner.scanDraftFromBitmap(bitmap, context, if (state.isFirstPickManuallySelected) isFirstPick else null, activeRole, confirmedHudPicks(), detectBannedPortraits = true)
                                    lastGlobalSyncFinishedAt = android.os.SystemClock.elapsedRealtime()
                                    if (result.isSuccessful) {
                                        withContext(Dispatchers.Main) {
                                            if (!autoScanEnabled) return@withContext
                                            if (result.detectedFirstPick != null && !state.isFirstPickManuallySelected) {
                                                isFirstPick = result.detectedFirstPick
                                            }

                                            val newAlliesAdded = syncAlliedHud(result)
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
                                            if(isDraftFullyConfirmed) state.stopAutomaticScanIfComplete(result.isLastPickFinalized)

                                            if (result.userExplicitlyDetectedRole != null && activeRole != result.userExplicitlyDetectedRole) {
                                                activeRole = result.userExplicitlyDetectedRole
                                                com.example.util.UserPreferences.setActiveDraftRole(context, result.userExplicitlyDetectedRole)
                                                scanNoticeMessage = "Auto-Scan: Tu rol detectado (${result.userExplicitlyDetectedRole.shortName})"
                                            } else if (isDraftFullyConfirmed) {
                                                scanNoticeMessage = "10/10 Campeones confirmados"
                                                AppLogger.i("FloatingService", if(autoScanEnabled) "Último retrato visible: esperando cierre de selección." else "10/10 confirmados: escáner automático detenido.")
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
                val result = DraftVisionScanner.scanDraftFromBitmap(bitmap, context, if (state.isFirstPickManuallySelected) isFirstPick else null, activeRole, confirmedHudPicks(), detectBannedPortraits = true)
                withContext(Dispatchers.Main) {
                    if (result.isSuccessful) {
                        // Sincronizar primera selección si se detectó y no ha sido fijada manualmente
                        if (result.detectedFirstPick != null && !state.isFirstPickManuallySelected) {
                            isFirstPick = result.detectedFirstPick
                        }

                        // 1. Asignación directa y de alta precisión por rol (respetando selecciones manuales)
                        syncAlliedHud(result)
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
                                        isDraggin…35598 tokens truncated…-----------------
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
        
        val resolvedAlliedRoles = AllyDraftReconciler.resolveRoles(
            allySlots.mapNotNull { slot -> slot.champion?.let { slot.slotIndex to it } }.toMap(),
            allySlotOcrLaneCache, allySlotRolesCache)
        // 4.1 Aliados: Cada slot aliado (0..4) mapea determinísticamente a su carril (allySlotRolesCache)
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
        } catch (t: Throwable) {
            AppLogger.e(TAG, "Excepción no controlada en scanDraftFromBitmap prevenida", t)
            DraftScanResult(emptyList(), emptyList(), isSuccessful = false, statusMessage = "Error en escaneo")
        }
    }
}
