package com.example.ui.screens

import com.example.ui.components.CoachFilterChip as FilterChip
import com.example.ui.components.CoachButton as Button
import com.example.ui.components.CoachTextButton as TextButton
import com.example.ui.components.CoachIconButton as IconButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.ui.components.coachClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.History
import android.widget.Toast
import com.example.data.repository.DraftHistoryRepository
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.example.util.tr
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.ui.components.DraftWomboSynergyCard
import com.example.ui.components.WomboComboSynergyDetector
import com.example.ui.components.MatchupPreviewDialog
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.WildRiftRepository
import kotlinx.coroutines.launch
import com.example.model.Champion
import com.example.util.SubscriptionManager
import com.example.model.DraftAnalysisResult
import com.example.model.DraftSlot
import com.example.model.LaneRole
import com.example.ui.components.ChampionAvatar
import com.example.ui.components.DraftTeamPositionCard
import com.example.ui.theme.AllyBlue
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedSurface
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechGoldLight
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.HextechSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TierSPlusColor

data class PendingSaveData(
    val result: String,
    val notes: String,
    val profileId: String,
    val profileName: String,
    val isLegendaryMatch: Boolean,
    val matchMode: String = if (isLegendaryMatch) "LEGENDARY" else "RANKED",
    val myScore: String = ""
)

@Composable
fun DraftAnalysisTab(
    isOverlay: Boolean = false,
    myChampion: Champion?,
    activeRole: LaneRole?,
    allySlots: List<DraftSlot>,
    enemySlots: List<DraftSlot>,
    analysis: DraftAnalysisResult,
    isFirstPick: Boolean,
    enemyLaneOpponent: Champion?,
    onToggleFirstPick: () -> Unit,
    onChangeRole: () -> Unit,
    onPickAllyRole: (LaneRole) -> Unit,
    onPickEnemyRole: (LaneRole) -> Unit,
    onRemoveAllyRole: (LaneRole) -> Unit,
    onRemoveEnemyRole: (LaneRole) -> Unit,
    onPickRecommendation: (Champion) -> Unit,
    onSelectChampion: (Champion) -> Unit,
    onOpenHistory: () -> Unit,
    onClearAll: () -> Unit,
    onRandomFill: (() -> Unit)? = null
) {
    val selectedOwnChampion = myChampion?.takeUnless { it.id.equals("empty", true) || it.id.isBlank() }
    val selectedEnemyChampion = enemyLaneOpponent?.takeUnless { it.id.equals("empty", true) || it.id.isBlank() }
    val selectedAllySlots = allySlots.filterNot { it.champion.id.equals("empty", true) || it.champion.id.isBlank() }
    val selectedEnemySlots = enemySlots.filterNot { it.champion.id.equals("empty", true) || it.champion.id.isBlank() }
    val tabContext = LocalContext.current
    val accountRole by SubscriptionManager.userRole.collectAsStateWithLifecycle()
    val adminClaim by com.example.util.AuthManager.isAdminClaim.collectAsStateWithLifecycle()
    val canRandomFill = !isOverlay && onRandomFill != null && com.example.model.RolePanelAccess.isAdministrator(accountRole, adminClaim)
    val draftLanguage = com.example.util.currentAppLanguage()
    val coroutineScope = rememberCoroutineScope()
    val isPremium by com.example.util.SubscriptionManager.isPremium.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    var showSaveDraftDialog by remember { mutableStateOf(false) }
    var pendingSaveData by remember { mutableStateOf<PendingSaveData?>(null) }
    var showMatchupDialog by remember { mutableStateOf(false) }
    var showClearDraftConfirm by remember { mutableStateOf(false) }
    val savedDraftToastText = tr("¡Draft guardado en el Historial!")
    val victoryToastText = " " + tr("Draft registrado como Victoria")
    val defeatToastText = " " + tr("Draft registrado como Derrota")

    if (showMatchupDialog && selectedOwnChampion != null && selectedEnemyChampion != null) {
        MatchupPreviewDialog(
            myChampion = selectedOwnChampion,
            enemyOpponent = selectedEnemyChampion,
            activeRole = activeRole ?: selectedOwnChampion.primaryRole,
            onDismiss = { showMatchupDialog = false }
        )
    }

    if (showClearDraftConfirm) {
        AlertDialog(
            onDismissRequest = { showClearDraftConfirm = false },
            title = { Text(tr("Limpiar Borrador"), color = DangerRed, fontWeight = FontWeight.Bold) },
            text = { Text(tr("¿Estás seguro de que deseas vaciar los equipos y reiniciar el draft actual? Se perderán las selecciones de campeones actuales."), color = TextSecondary, fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showClearDraftConfirm = false
                        onClearAll()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text(tr("Limpiar"), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDraftConfirm = false }) {
                    Text(tr("Cancelar"), color = TextMuted)
                }
            },
            containerColor = HextechSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showSaveDraftDialog) {
        com.example.ui.components.SaveDraftDialog(
            myChampion = selectedOwnChampion,
            enemyLaneOpponent = selectedEnemyChampion,
            userRole = activeRole ?: LaneRole.MID,
            estimatedWinrate = analysis.bestOverallPick?.estimatedWinrate ?: 50.0,
            onDismiss = { showSaveDraftDialog = false },
            onSave = { result, notes, profileId, profileName, isLegendaryMatch, matchMode, myScore ->
                showSaveDraftDialog = false
                coroutineScope.launch {
                    val exists = DraftHistoryRepository.checkDraftExists(
                        context = tabContext,
                        myRole = activeRole ?: LaneRole.MID,
                        allies = selectedAllySlots,
                        enemies = selectedEnemySlots,
                        accountProfileId = profileId
                    )

                    if (exists) {
                        pendingSaveData = PendingSaveData(result, notes, profileId, profileName, isLegendaryMatch, matchMode, myScore)
                    } else {
                        DraftHistoryRepository.saveDraft(
                            context = tabContext,
                            myRole = activeRole ?: LaneRole.MID,
                            isFirstPick = isFirstPick,
                            isLegendary = isLegendaryMatch,
                            matchMode = matchMode,
                            myScore = myScore,
                            allowDuplicate = false,
                            allies = selectedAllySlots,
                            enemies = selectedEnemySlots,
                            analysis = analysis,
                            notes = notes,
                            matchResult = result,
                            accountProfileId = profileId,
                            accountProfileName = profileName
                        )
                        val toastMsg = when (result) {
                            "VICTORY" -> victoryToastText
                            "DEFEAT" -> defeatToastText
                            else -> " $savedDraftToastText"
                        }
                        android.widget.Toast.makeText(tabContext, com.example.util.appTr(toastMsg), android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    if (pendingSaveData != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pendingSaveData = null },
            title = { androidx.compose.material3.Text(tr("Draft Duplicado"), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = com.example.ui.theme.TextPrimary) },
            text = { androidx.compose.material3.Text(tr("Es el mismo draft que el anterior, ¿deseas guardarlo de todas formas?"), color = com.example.ui.theme.TextSecondary) },
            containerColor = com.example.ui.theme.HextechSurface,
            confirmButton = {
                com.example.ui.components.CoachButton(
                    onClick = {
                        val data = pendingSaveData!!
                        pendingSaveData = null
                        coroutineScope.launch {
                            DraftHistoryRepository.saveDraft(
                                context = tabContext,
                                myRole = activeRole ?: LaneRole.MID,
                                isFirstPick = isFirstPick,
                                isLegendary = data.isLegendaryMatch,
                                matchMode = data.matchMode,
                                myScore = data.myScore,
                                allowDuplicate = true,
                                allies = selectedAllySlots,
                                enemies = selectedEnemySlots,
                                analysis = analysis,
                                notes = data.notes,
                                matchResult = data.result,
                                accountProfileId = data.profileId,
                                accountProfileName = data.profileName
                            )
                            val toastMsg = when (data.result) {
                                "VICTORY" -> victoryToastText
                                "DEFEAT" -> defeatToastText
                                else -> " $savedDraftToastText"
                            }
                            android.widget.Toast.makeText(tabContext, com.example.util.appTr(toastMsg), android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.HextechGold)
                ) {
                    androidx.compose.material3.Text(tr("Sí"), color = androidx.compose.ui.graphics.Color.Black)
                }
            },
            dismissButton = {
                com.example.ui.components.CoachOutlinedButton(
                    onClick = { pendingSaveData = null },
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = com.example.ui.theme.TextMuted)
                ) {
                    androidx.compose.material3.Text(tr("No"))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Role active pill & First Pick Row
        val roleActivePill = @Composable {
            // Role active pill (Hextech styled)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .coachClickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onChangeRole()
                    }
                    .testTag("draft_active_role_pill"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = HextechSurface),
                border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.8f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = if (isOverlay) Modifier else Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        if (activeRole != null) {
                            Image(
                                painter = painterResource(id = activeRole.iconResId),
                                contentDescription = com.example.util.tr(activeRole.displayName),
                                modifier = Modifier.size(32.dp).padding(end = 8.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = HextechGold,
                                modifier = Modifier.size(32.dp).padding(end = 8.dp)
                            )
                        }
                        Column {
                            Text(
                                text = tr("Mi Línea"),
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = com.example.util.tr(if (activeRole != null) com.example.util.tr(activeRole.displayName) else tr("Todas las líneas")),
                                color = HextechGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = HextechCyan.copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, HextechCyan.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = com.example.util.tr(if (activeRole != null) tr("Cambiar") else tr("Elegir")),
                            color = HextechCyan,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

        }

        val firstPickCard = @Composable {
            // First Pick / Blind Pick Mode Switch (Redesigned with Hextech theme)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .coachClickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleFirstPick()
                    }
                    .testTag("draft_first_pick_toggle"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFirstPick) HextechGold.copy(alpha = 0.16f) else HextechSurface
                ),
                border = BorderStroke(
                    1.2.dp,
                    if (isFirstPick) HextechGold else HextechCardBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = if (isOverlay) Modifier else Modifier.weight(1f)) {
                        Text(
                            text = com.example.util.tr(if (isFirstPick) tr("1er Pick") else tr("Counter Pick")),
                            color = if (isFirstPick) HextechGold else HextechCyan,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = com.example.util.tr(if (isFirstPick) tr("Blind Pick") else tr("Adaptativo")),
                            color = if (isFirstPick) HextechGoldLight else TextMuted,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isFirstPick,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (activeRole == null) {
                                android.widget.Toast.makeText(tabContext, com.example.util.appTr("Selecciona tu línea primero"), android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                onToggleFirstPick()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = HextechGold,
                            checkedTrackColor = HextechGold.copy(alpha = 0.4f),
                            checkedBorderColor = HextechGold,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = HextechSurfaceVariant,
                            uncheckedBorderColor = HextechCardBorder
                        ),
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }

        if (isOverlay) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                roleActivePill()
                firstPickCard()
            }
        } else {
            androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 360.dp) {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        roleActivePill()
                        firstPickCard()
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) { roleActivePill() }
                        androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) { firstPickCard() }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Actions Row: Guardar Draft, Historial de Partidas & Vaciar Todo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (isPremium) {
                        val alliesSelected = selectedAllySlots.count { it.champion.id != "empty" }
                        val enemiesSelected = selectedEnemySlots.count { it.champion.id != "empty" }
                        if (activeRole == null) {
                            android.widget.Toast.makeText(tabContext, com.example.util.appTr("Selecciona tu línea primero"), android.widget.Toast.LENGTH_SHORT).show()
                        } else if (alliesSelected < 5 || enemiesSelected < 5) {
                            android.widget.Toast.makeText(tabContext, com.example.util.appTr("Debes seleccionar los 5 campeones aliados y 5 enemigos"), android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            showSaveDraftDialog = true
                        }
                    } else {
                        android.widget.Toast.makeText(tabContext, com.example.util.appTr("Requiere suscripción Premium"), android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(1.1f)
                    .height(44.dp)
                    .testTag("save_draft_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HextechGold.copy(alpha = 0.16f),
                    contentColor = HextechGold
                ),
                border = BorderStroke(
                    1.2.dp,
                    HextechGold.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkAdd,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = tr("Guardar"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    if (!isPremium) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(Brush.horizontalGradient(listOf(HextechGold, Color(0xFFD4AF37))))
                                .padding(horizontal = 3.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = com.example.util.tr("PRO"),
                                color = HextechDarkBg,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            if (canRandomFill) {
                Button(
                    onClick = { onRandomFill?.invoke() },
                    modifier = Modifier.weight(1f).height(44.dp).testTag("random_draft_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = HextechCyan.copy(alpha = 0.16f), contentColor = HextechCyan),
                    border = BorderStroke(1.2.dp, HextechCyan.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) { Text(tr("Aleatorio"), fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            }

            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showClearDraftConfirm = true
                },
                modifier = Modifier
                    .weight(0.9f)
                    .height(44.dp)
                    .testTag("clear_draft_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DangerRedSurface,
                    contentColor = DangerRed
                ),
                border = BorderStroke(1.2.dp, DangerRed.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = DangerRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = tr("Vaciar"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = DangerRed
                    )
                }
            }

            if (isPremium) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOpenHistory()
                    },
                    modifier = Modifier
                        .weight(1.1f)
                        .height(44.dp)
                        .testTag("open_draft_history_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HextechSurface,
                        contentColor = HextechCyan
                    ),
                    border = BorderStroke(1.2.dp, HextechCyan.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = HextechCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = tr("Historial"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = HextechCyan
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Panel de Selección Oficial de Posiciones - Equipo Aliado
        DraftTeamPositionCard(
            isOverlay = isOverlay,
            title = "Equipo Aliado",
            isEnemy = false,
            slots = selectedAllySlots,
            activeUserRole = activeRole,
            onPickChampionForRole = onPickAllyRole,
            onRemoveChampionForRole = onRemoveAllyRole,
            onChampionClick = onSelectChampion
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Panel de Selección de Equipo Rival
        com.example.ui.components.DraftTeamPositionCard(
            isOverlay = isOverlay,
            title = "Equipo Rival",
            isEnemy = true,
            slots = selectedEnemySlots,
            activeUserRole = activeRole,
            onPickChampionForRole = onPickEnemyRole,
            onRemoveChampionForRole = onRemoveEnemyRole,
            onChampionClick = onSelectChampion
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Warnings & Matchup Directo
        if (analysis.directMatchupWarning != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DangerRedSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(tr("Alerta Táctica de Matchup"), color = DangerRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(com.example.util.tr(analysis.directMatchupWarning), color = TextPrimary, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Cálculo Automático de Matchup 1v1 vs Rival de Línea
        if (selectedEnemyChampion != null && selectedOwnChampion != null) {
            val historyProfile = com.example.data.AccountProfileManager.getActiveProfile(tabContext)
            val profileDrafts = remember(historyProfile.id) { DraftHistoryRepository.getDraftsByProfile(tabContext, historyProfile.id) }
            val allSavedDraftsState by profileDrafts.collectAsState(initial = emptyList())
            val matchesVsOpponent = remember(allSavedDraftsState, selectedEnemyChampion.name, selectedOwnChampion.name, activeRole, historyProfile.id) {
                com.example.data.analytics.PersonalTierListManager.draftsForMatchup(allSavedDraftsState,
                    selectedOwnChampion.name, selectedEnemyChampion.name, activeRole, historyProfile.id)
            }
            val winsVsOpp = matchesVsOpponent.count { it.matchResult.equals("VICTORY", ignoreCase = true) }
            val lossesVsOpp = matchesVsOpponent.count { it.matchResult.equals("DEFEAT", ignoreCase = true) }
            val totalDecidedOpp = winsVsOpp + lossesVsOpp
            val wrVsOpp = if (totalDecidedOpp > 0) (winsVsOpp.toDouble() / totalDecidedOpp * 100.0).toInt() else null

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HextechSurface),
                border = BorderStroke(1.dp, if (wrVsOpp != null && wrVsOpp >= 50) Color(0xFF81C784).copy(alpha = 0.6f) else HextechCardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SportsKabaddi,
                                contentDescription = null,
                                tint = HextechGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tr("Resultados con tu campeón ante este rival:") + " ${selectedOwnChampion.name} vs ${selectedEnemyChampion.name}",
                                color = HextechGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        }

                        if (wrVsOpp != null) {
                            val badgeColor = if (wrVsOpp >= 50) Color(0xFF81C784) else DangerRed
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = badgeColor.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, badgeColor)
                            ) {
                                Text(
                                    text = com.example.util.tr("$wrVsOpp% WR"),
                                    color = badgeColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (totalDecidedOpp > 0) {
                        Text(
                            text = com.example.util.tr("${winsVsOpp}W - ${lossesVsOpp}L (${totalDecidedOpp} ${tr("partidas registradas")})"),
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val adviceText = if (totalDecidedOpp < 5) {
                            "Muestra pequeña: registra al menos 5 resultados con este campeón y rival en la misma línea. Son victorias de partidas, no de duelos 1v1."
                        } else {
                            "Estos resultados pertenecen a tu perfil, campeón y línea. Revisa las derrotas para identificar oleadas, recursos y objetivos; el porcentaje no demuestra ventaja en el duelo."
                        }
                        Text(
                            text = com.example.util.tr(adviceText),
                            color = TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    } else {
                        Text(
                            text = tr("Sin resultados registrados con tu campeón ante este rival en esta línea y perfil."),
                            color = TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Sinergias Letales y Wombo-Combos Detectados
        val allAllyChamps = remember(selectedAllySlots.toList(), selectedOwnChampion) {
            (selectedAllySlots.map { it.champion } + listOfNotNull(selectedOwnChampion)).distinctBy { it.id }
        }
        val womboCombos = remember(allAllyChamps) {
            WomboComboSynergyDetector.detectWombos(allAllyChamps)
        }
        if (womboCombos.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                womboCombos.forEach { wombo ->
                    DraftWomboSynergyCard(
                        wombo = wombo,
                        onChampionClick = onSelectChampion
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Ally Damage distribution
        if (selectedAllySlots.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(tr("Balance de Daño Aliado (orientativo)"), color = HextechGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                if (analysis.allyCompositionWarning != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(com.example.util.tr(analysis.allyCompositionWarning), color = DangerRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))) {
                if (analysis.allyPhysicalDamagePercent > 0) {
                    Box(modifier = Modifier.weight(analysis.allyPhysicalDamagePercent.toFloat()).fillMaxHeight().background(Color(0xFFE57373)))
                }
                if (analysis.allyMagicDamagePercent > 0) {
                    Box(modifier = Modifier.weight(analysis.allyMagicDamagePercent.toFloat()).fillMaxHeight().background(Color(0xFF64B5F6)))
                }
                if (analysis.allyTrueDamagePercent > 0) {
                    Box(modifier = Modifier.weight(analysis.allyTrueDamagePercent.toFloat()).fillMaxHeight().background(Color.White))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(com.example.util.tr("${analysis.allyPhysicalDamagePercent}% " + tr("Físico")), color = Color(0xFFE57373), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Text(com.example.util.tr("${analysis.allyMagicDamagePercent}% " + tr("Mágico")), color = Color(0xFF64B5F6), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Text(com.example.util.tr("${analysis.allyTrueDamagePercent}% " + tr("Verdadero")), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Damage distribution
        if (selectedEnemySlots.isNotEmpty()) {
            Text(tr("La proporción cambia con la build y la partida."), color = TextSecondary, fontSize = 10.sp)
            Text(tr("Balance de Daño Rival (orientativo)"), color = HextechGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))) {
                if (analysis.physicalDamagePercent > 0) {
                    Box(modifier = Modifier.weight(analysis.physicalDamagePercent.toFloat()).fillMaxHeight().background(Color(0xFFE57373)))
                }
                if (analysis.magicDamagePercent > 0) {
                    Box(modifier = Modifier.weight(analysis.magicDamagePercent.toFloat()).fillMaxHeight().background(Color(0xFF64B5F6)))
                }
                if (analysis.trueDamagePercent > 0) {
                    Box(modifier = Modifier.weight(analysis.trueDamagePercent.toFloat()).fillMaxHeight().background(Color.White))
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(com.example.util.tr("${analysis.physicalDamagePercent}% " + tr("Físico")), color = Color(0xFFE57373), fontSize = 10.sp)
                Text(com.example.util.tr("${analysis.magicDamagePercent}% " + tr("Mágico")), color = Color(0xFF64B5F6), fontSize = 10.sp)
                Text(com.example.util.tr("${analysis.trueDamagePercent}% " + tr("Verdadero")), color = Color.White, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // My Champion Evaluation (Sincronizado automáticamente sin selecciones duplicadas)
        if (selectedOwnChampion != null) {
            val myChamp = selectedOwnChampion
            val myEval = WildRiftRepository.evaluateChampion(
                champ = myChamp,
                myRole = activeRole ?: myChamp.primaryRole,
                allies = selectedAllySlots.map { it.champion },
                enemies = selectedEnemySlots.map { it.champion },
                enemyLaneOpponent = selectedEnemyChampion,
                lang = draftLanguage
            )
            val isOffRole = activeRole != null && myChamp.primaryRole != activeRole && !myChamp.secondaryRoles.contains(activeRole)
            val isDirectLaneWeakness = selectedEnemyChampion?.let {
                com.example.data.MatchupKnowledge.relation(myEval.champion,it) == com.example.data.MatchupRelation.UNFAVORABLE
            } == true
            val shouldChange = isOffRole || myEval.advantageBadge.contains("ATÍPICA") || (myEval.draftFitScore < 48.0) || (isDirectLaneWeakness && myEval.draftFitScore < 50.0)

            val recommendationText = when {
                shouldChange -> tr("️ Considera cambiarlo")
                myEval.advantageBadge.contains("DOMINAS LÍNEA") || myEval.advantageBadge.contains("COUNTER") -> tr(" Favorable en carril")
                else -> tr(" Buena elección para tu línea")
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.5.dp, if (shouldChange) DangerRed else HextechGold, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = HextechSurface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = (activeRole ?: myChamp.primaryRole).iconResId),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = com.example.util.tr(if (activeRole != null) tr("TU ELECCIÓN EN") + " ${com.example.util.tr(activeRole.displayName).uppercase()}" else tr("TU ELECCIÓN (GENERAL)")),
                                color = if (shouldChange) DangerRed else HextechGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = com.example.util.tr(tr("Encaje en el draft:") + " ${myEval.draftFitScore}/100"),
                            color = if (shouldChange) DangerRed else HextechCyan,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ChampionAvatar(champion = myEval.champion, size = 50.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(com.example.util.tr(myEval.champion.name), color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = com.example.util.tr("Tier ${myEval.champion.tier}"),
                                    color = HextechGold,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = com.example.util.tr(recommendationText),
                                color = if (shouldChange) DangerRed else Color(0xFF81C784),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = { onRemoveAllyRole(activeRole ?: myChamp.primaryRole) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = tr("Eliminar"), tint = TextMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(tr(myEval.tacticalReason), color = TextPrimary.copy(alpha = 0.9f), fontSize = 12.sp, lineHeight = 16.sp)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tr("Toca para ver la build completa y el análisis táctico"),
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // 1v1 Matchup Preview Trigger Button
                    if (selectedEnemyChampion != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showMatchupDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("open_matchup_preview_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HextechGold.copy(alpha = 0.2f),
                                contentColor = HextechGold
                            ),
                            border = BorderStroke(1.2.dp, HextechGold),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp),
                                tint = HextechGold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tr("⚔️ Cara a Cara 1v1 (Matchup Preview)"),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onPickAllyRole(activeRole ?: LaneRole.MID)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("select_my_pick_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HextechGold.copy(alpha = 0.2f),
                    contentColor = HextechGold
                ),
                border = BorderStroke(1.5.dp, HextechGold),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = (activeRole ?: LaneRole.MID).iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tr("Selecciona tu campeón"),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (selectedOwnChampion == null || selectedEnemyChampion == null) {
            Text(
                text = tr(when {
                    selectedOwnChampion == null && selectedEnemyChampion == null -> "Selecciona tu campeón y el rival para ver el cara a cara 1 vs 1."
                    selectedOwnChampion == null -> "Selecciona tu campeón para ver el cara a cara 1 vs 1."
                    else -> "Selecciona el campeón rival para ver el cara a cara 1 vs 1."
                }),
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.testTag("draft_matchup_missing_selection")
            )
            Spacer(Modifier.height(16.dp))
        }

        if (activeRole != null && (selectedAllySlots.isNotEmpty() || selectedEnemySlots.isNotEmpty())) {
            // Live Recommendations Header
            Row(modifier = Modifier.testTag("draft_recommendations"), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = activeRole.iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = com.example.util.tr(if (analysis.isFirstPickMode) tr("Mejor Primer Pick Seguro para") + " ${com.example.util.tr(activeRole.displayName)}" else tr("Mejor Opción según tu Equipo y el Rival")),
                    color = HextechGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // #1 Best Pick Hero Card
            val topPick = analysis.bestOverallPick ?: analysis.recommendations.firstOrNull()
            if (topPick != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .coachClickable { onSelectChampion(topPick.champion) }
                        .border(1.5.dp, HextechGold, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = com.example.util.tr(if (analysis.isFirstPickMode) tr(" #1 RECOMENDACIÓN BLIND PICK") else tr(" #1 MEJOR ELECCIÓN TÁCTICA")),
                                color = HextechGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = com.example.util.tr(tr("Encaje en el draft:") + " ${topPick.draftFitScore}/100"),
                                color = HextechCyan,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ChampionAvatar(champion = topPick.champion, size = 56.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = com.example.util.tr(topPick.champion.name),
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = com.example.util.tr("Tier ${topPick.champion.tier}"),
                                        color = TierSPlusColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = tr(topPick.advantageBadge),
                                    color = HextechCyan,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = tr(topPick.tacticalReason),
                            color = TextPrimary.copy(alpha = 0.95f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        if (topPick.synergyDetails.isNotBlank() || topPick.counterDetails.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(HextechDarkBg.copy(alpha = 0.6f))
                                    .border(0.8.dp, HextechCardBorder.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (topPick.synergyDetails.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = com.example.util.tr("🤝 " + tr("Sinergia / Combo:")),
                                            color = HextechCyan,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = tr(topPick.synergyDetails),
                                            color = TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                if (topPick.counterDetails.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = com.example.util.tr("🛡️ " + tr("Ventaja / Counter:")),
                                            color = HextechGold,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = tr(topPick.counterDetails),
                                            color = TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tr("Toca para ver la build completa y el análisis táctico"),
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = { onPickRecommendation(topPick.champion) },
                                colors = ButtonDefaults.buttonColors(containerColor = HextechGold, contentColor = HextechDarkBg),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(tr("Elegir como mi Pick"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Secondary Recommendations
            val otherRecs = analysis.recommendations.filter { it.champion.id != topPick?.champion?.id }
            // Keep the floating assistant's existing rendering path. The application
            // loads alternatives progressively instead of laying out every long card
            // while the champion picker is being dismissed.
            var visibleRecommendationCount by remember(activeRole, selectedAllySlots, selectedEnemySlots, isFirstPick) {
                mutableIntStateOf(4)
            }
            val visibleOtherRecs = if (isOverlay) otherRecs else otherRecs.take(visibleRecommendationCount)
            if (otherRecs.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = activeRole.iconResId),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = com.example.util.tr(tr("Otras Opciones Viables para") + " ${com.example.util.tr(activeRole.displayName)}:"),
                        color = HextechCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                visibleOtherRecs.forEach { rec ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("draft_secondary_recommendation")
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .coachClickable { onSelectChampion(rec.champion) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ChampionAvatar(champion = rec.champion, size = 46.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(com.example.util.tr(rec.champion.name), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(com.example.util.tr(tr("Encaje:") + " ${rec.draftFitScore}/100"), color = HextechGold, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(tr(rec.advantageBadge), color = HextechCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(tr(rec.tacticalReason), color = TextMuted, fontSize = 11.sp, lineHeight = 14.sp)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { onPickRecommendation(rec.champion) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = tr("Elegir como mi Pick"),
                                        tint = HextechCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            if (rec.synergyDetails.isNotBlank() || rec.counterDetails.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (rec.synergyDetails.isNotBlank()) {
                                        Text(
                                            text = com.example.util.tr("🤝 " + rec.synergyDetails),
                                            color = HextechCyan,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (rec.counterDetails.isNotBlank()) {
                                        Text(
                                            text = com.example.util.tr("🛡️ " + rec.counterDetails),
                                            color = HextechGoldLight,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
            if (!isOverlay && visibleOtherRecs.size < otherRecs.size) {
                com.example.ui.components.CoachOutlinedButton(
                    onClick = { visibleRecommendationCount = (visibleRecommendationCount + 5).coerceAtMost(otherRecs.size) },
                    modifier = Modifier.fillMaxWidth().testTag("draft_more_recommendations"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HextechCyan),
                    border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(tr("Ver más recomendaciones"), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun TeamChampionSlot(
    slot: DraftSlot,
    isEnemy: Boolean,
    isMyPick: Boolean = false,
    onRoleChanged: (LaneRole) -> Unit,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    var showRoleMenu by remember { mutableStateOf(false) }
    val isOffMeta = slot.assignedRole != slot.champion.primaryRole && !slot.champion.secondaryRoles.contains(slot.assignedRole)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .coachClickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMyPick) HextechGold.copy(alpha = 0.12f) else HextechSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isMyPick) 1.5.dp else 1.dp,
            when {
                isMyPick -> HextechGold
                isEnemy -> DangerRed.copy(alpha = 0.7f)
                else -> AllyBlue.copy(alpha = 0.7f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                ChampionAvatar(champion = slot.champion, size = 38.dp, showTierBadge = false)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = com.example.util.tr(slot.champion.name),
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isMyPick) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = com.example.util.tr(" Mío"),
                                color = HextechGold,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))

                    // Chip interactivo para cambiar la línea asignada de este campeón
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isMyPick) HextechGold.copy(alpha = 0.2f)
                                    else if (isEnemy) DangerRed.copy(alpha = 0.15f)
                                    else AllyBlue.copy(alpha = 0.15f)
                                )
                                .coachClickable { showRoleMenu = true }
                                .padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = slot.assignedRole.iconResId),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            val roleLabel = com.example.util.tr(slot.assignedRole.shortName) +
                                    if (isOffMeta) " [${com.example.util.tr(slot.champion.primaryRole.shortName)}]" else ""
                            Text(
                                text = com.example.util.tr(roleLabel),
                                color = if (isMyPick) HextechGold else if (isEnemy) DangerRed else AllyBlue,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = tr("Cambiar línea"),
                                tint = if (isMyPick) HextechGold else if (isEnemy) DangerRed else AllyBlue,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showRoleMenu,
                            onDismissRequest = { showRoleMenu = false },
                            modifier = Modifier.background(HextechSurfaceVariant)
                        ) {
                            LaneRole.entries.forEach { role ->
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Image(
                                            painter = painterResource(id = role.iconResId),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = com.example.util.tr(role.displayName),
                                            color = if (role == slot.assignedRole) HextechCyan else TextPrimary,
                                            fontWeight = if (role == slot.assignedRole) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    },
                                    onClick = {
                                        onRoleChanged(role)
                                        showRoleMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = tr("Eliminar"),
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun AddChampionSlotButton(
    isEnemy: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(HextechSurface)
            .border(
                1.dp,
                if (isEnemy) DangerRed.copy(alpha = 0.4f) else AllyBlue.copy(alpha = 0.4f),
                RoundedCornerShape(10.dp)
            )
            .coachClickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Add, contentDescription = null, tint = if (isEnemy) DangerRed else AllyBlue, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(tr("Añadir"), color = if (isEnemy) DangerRed else AllyBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun DraftChampionPickerSheet(
    team: String,
    suggestedRole: LaneRole?,
    alreadySelected: List<String>,
    onChampionPicked: (Champion, LaneRole) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var search by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf<LaneRole?>(null) }
    val dragHandleLabel = tr("Arrastra para cerrar")

    val availableChamps = remember(search, alreadySelected, selectedRoleFilter, WildRiftRepository.champions.toList()) {
        val list = WildRiftRepository.champions.filter { champ ->
            val notSelected = !alreadySelected.contains(champ.id)
            val matchesQuery = search.isBlank() ||
                    champ.name.contains(search, ignoreCase = true) ||
                    champ.summary.contains(search, ignoreCase = true)
            val matchesRole = selectedRoleFilter == null ||
                    champ.primaryRole == selectedRoleFilter ||
                    champ.secondaryRoles.contains(selectedRoleFilter)
            notSelected && matchesQuery && matchesRole
        }
        if (selectedRoleFilter != null) {
            list.sortedWith(
                compareByDescending<Champion> { it.primaryRole == selectedRoleFilter }
                    .thenByDescending { it.tier == "S+" }
                    .thenByDescending { it.tier == "S" }
                    .thenByDescending { it.winrate }
            )
        } else {
            list.sortedWith(
                compareByDescending<Champion> { it.tier == "S+" }
                    .thenByDescending { it.tier == "S" }
                    .thenByDescending { it.winrate }
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = HextechSurfaceVariant,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                modifier = Modifier.clearAndSetSemantics { contentDescription = dragHandleLabel }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = com.example.util.tr(when (team) {
                    "MYSELF" -> tr("Seleccionar Mi Campeón") + if (suggestedRole != null) " (${com.example.util.tr(suggestedRole.displayName)})" else ""
                    "ALLY" -> tr("Seleccionar Campeón Aliado")
                    else -> tr("Seleccionar Campeón Rival")
                }),
                color = when (team) {
                    "MYSELF" -> HextechGold
                    "ALLY" -> AllyBlue
                    else -> DangerRed
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(tr("Buscar campeón..."), color = TextMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HextechCyan) },
                trailingIcon = {
                    if (search.isNotEmpty()) {
                        IconButton(onClick = { search = "" }) {
                            Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Limpiar"), tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HextechCyan,
                    unfocusedBorderColor = HextechCardBorder,
                    focusedContainerColor = HextechSurface,
                    unfocusedContainerColor = HextechSurface
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Role Filters
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = selectedRoleFilter == null,
                    onClick = { selectedRoleFilter = null },
                    label = { Text(tr("Todos"), fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = HextechCyan,
                        selectedLabelColor = HextechDarkBg
                    )
                )
                LaneRole.entries.forEach { role ->
                    FilterChip(
                        selected = selectedRoleFilter == role,
                        onClick = { selectedRoleFilter = if (selectedRoleFilter == role) null else role },
                        leadingIcon = {
                            Image(
                                painter = painterResource(id = role.iconResId),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(com.example.util.tr(role.shortName), fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HextechCyan,
                            selectedLabelColor = HextechDarkBg
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 72.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(availableChamps) { champ ->
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .coachClickable {
                                val assignedRole = suggestedRole ?: selectedRoleFilter ?: champ.primaryRole
                                onChampionPicked(champ, assignedRole)
                            }
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ChampionAvatar(champion = champ, size = 56.dp, showTierBadge = false)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = com.example.util.tr(champ.name),
                            color = TextPrimary,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
internal fun RoleChangeBottomSheet(
    currentRole: LaneRole?,
    onRoleSelected: (LaneRole) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HextechSurfaceVariant),
            border = BorderStroke(1.5.dp, HextechGold.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(tr("Selecciona tu Línea para esta Partida"), color = HextechGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                LaneRole.entries.forEach { role ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (role == currentRole) HextechCyan.copy(alpha = 0.2f) else HextechSurface)
                            .border(1.dp, if (role == currentRole) HextechCyan else HextechCardBorder, RoundedCornerShape(10.dp))
                            .coachClickable { onRoleSelected(role) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = role.iconResId),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(com.example.util.tr(role.displayName), color = if (role == currentRole) HextechCyan else TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(com.example.util.tr(role.shortName), color = TextMuted, fontSize = 11.sp)
                            }
                        }
                        if (role == currentRole) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = HextechCyan)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
