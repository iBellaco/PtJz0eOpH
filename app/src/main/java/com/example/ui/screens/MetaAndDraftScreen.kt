package com.example.ui.screens

import com.example.utils.parseHtmlColorToAnnotatedString

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.History
import android.widget.Toast
import com.example.data.local.FavoriteChampionsManager
import com.example.ui.components.ChampionBuildCreatorDialog
import com.example.data.repository.DraftHistoryRepository
import com.example.data.sync.BestBuildWrScraper
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.example.util.tr
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.ui.components.FormattedWildRiftText
import com.example.ui.components.formatWildRiftDescription
import com.example.ui.components.SparklineTrendGraph
import com.example.ui.components.DraftWomboSynergyCard
import com.example.ui.components.WomboComboSynergyDetector
import com.example.ui.components.MatchupPreviewDialog
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.WildRiftRepository
import com.example.data.sync.ChineseMetaSyncService
import com.example.data.sync.ChineseSyncState
import com.example.data.sync.TencentRankTier
import kotlinx.coroutines.launch
import com.example.model.Champion
import com.example.util.SubscriptionManager
import com.example.model.DamageType
import com.example.model.DraftAnalysisResult
import com.example.model.DraftSlot
import com.example.model.ItemCategory
import com.example.model.LaneRole
import com.example.model.MapObjectiveItem
import com.example.model.RuneItem
import com.example.model.SummonerSpellItem
import com.example.model.WildRiftItem
import com.example.ui.components.AppAssetImage
import com.example.ui.components.ChampionAvatar
import com.example.ui.components.DraftTeamPositionCard
import com.example.ui.components.CooldownTrackerPanel
import com.example.ui.components.DamagePenetrationCalculator
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
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TierAColor
import com.example.ui.theme.TierSColor
import com.example.ui.theme.TierSPlusColor

import com.example.util.LocalLanguage

enum class MetaScreenMode {
    DRAFTING,
    TIER_LIST,
    CATALOG
}

private data class MetaNavTabItem(val title: String, val count: Int? = null)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MetaAndDraftScreen(
    mode: MetaScreenMode = MetaScreenMode.CATALOG,
    userMainRole: LaneRole,
    initialChampionId: String? = null,
    isOverlay: Boolean = false,
    onNavigateBack: () -> Unit
) {
    val screenContext = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isPremium by SubscriptionManager.isPremium.collectAsState()
    val lang = LocalLanguage.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    val sharedPrefs = remember { screenContext.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }
    var activeRole by remember {
        val savedRoleStr = sharedPrefs.getString("saved_active_role", null)
        val initialRole = if (savedRoleStr != null) {
            try { LaneRole.valueOf(savedRoleStr) } catch (e: Exception) { null }
        } else null
        mutableStateOf<LaneRole?>(initialRole)
    }

    LaunchedEffect(activeRole) {
        if (activeRole != null) {
            sharedPrefs.edit().putString("saved_active_role", activeRole!!.name).apply()
        } else {
            sharedPrefs.edit().remove("saved_active_role").apply()
        }
    }

    var showRoleChangeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        com.example.data.local.CustomChampionBuildsManager.init(screenContext)
    }

    val defaultChamp = WildRiftRepository.champions.firstOrNull() ?: Champion(
        id = "garen",
        name = "Garen",
        title = "El Poder de Demacia",
        primaryRole = LaneRole.TOP
    )

    // Generador dinámico de composiciones de draft iniciales basadas estrictamente en el rol
    fun generateRoleBasedDraft(excludeIds: MutableSet<String>): List<DraftSlot> {
        val roles = listOf(LaneRole.TOP, LaneRole.JUNGLE, LaneRole.MID, LaneRole.ADC, LaneRole.SUPPORT)
        return roles.mapNotNull { role ->
            val rolePool = WildRiftRepository.champions.filter { champ ->
                !excludeIds.contains(champ.id) && (champ.primaryRole == role || champ.secondaryRoles.contains(role))
            }
            val chosen = rolePool.shuffled().firstOrNull()
                ?: WildRiftRepository.champions.filter { !excludeIds.contains(it.id) }.shuffled().firstOrNull()
                ?: defaultChamp
            excludeIds.add(chosen.id)
            DraftSlot(chosen, role)
        }
    }

    // Draft State con asignación dinámica por rol en cada apertura
    val usedDraftChampIds = remember { mutableSetOf<String>() }
    val allySlots = remember(WildRiftRepository.champions.toList()) {
        mutableStateListOf<DraftSlot>().apply {
            addAll(generateRoleBasedDraft(usedDraftChampIds))
        }
    }

    val enemySlots = remember(WildRiftRepository.champions.toList()) {
        mutableStateListOf<DraftSlot>().apply {
            addAll(generateRoleBasedDraft(usedDraftChampIds))
        }
    }

    // Modal Champion Picker & Detail State
    var pickingForTeam by remember { mutableStateOf<String?>(null) } // "ALLY", "ENEMY", "MYSELF"
    var suggestedPickingRole by remember { mutableStateOf<LaneRole?>(null) }
    var selectedDetailChampion by remember { mutableStateOf<Champion?>(
        initialChampionId?.let { id -> WildRiftRepository.getChampionById(id) }
    ) }
    var isFirstPick by remember { mutableStateOf(false) }
    var showDraftHistoryScreen by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler {
        when {
            selectedDetailChampion != null -> {
                selectedDetailChampion = null
            }
            pickingForTeam != null -> {
                pickingForTeam = null
            }
            showDraftHistoryScreen -> {
                showDraftHistoryScreen = false
            }
            showRoleChangeDialog -> {
                showRoleChangeDialog = false
            }
            selectedTabIndex != 0 -> {
                selectedTabIndex = 0
            }
            else -> {
                onNavigateBack()
            }
        }
    }

    if (showDraftHistoryScreen) {
        DraftHistoryScreen(
            onNavigateBack = { showDraftHistoryScreen = false },
            onLoadDraft = { allies, enemies, role, firstPick ->
                allySlots.clear()
                allySlots.addAll(allies)
                enemySlots.clear()
                enemySlots.addAll(enemies)
                activeRole = role
                isFirstPick = firstPick
                showDraftHistoryScreen = false
            }
        )
        return
    }

    // Sincronización contextual automática: Mi campeón es el aliado en mi línea activa
    val myChampion = if (activeRole != null) allySlots.find { it.assignedRole == activeRole }?.champion else null
    val roleIndex = when (activeRole) {
        LaneRole.TOP -> 0
        LaneRole.JUNGLE -> 1
        LaneRole.MID -> 2
        LaneRole.ADC -> 3
        LaneRole.SUPPORT -> 4
        null -> 0
    }
    val enemyLaneOpponent = if (activeRole != null) enemySlots.find { it.assignedRole == activeRole }?.champion ?: enemySlots.getOrNull(roleIndex)?.champion else null

    val analysis = remember(activeRole, isFirstPick, allySlots.toList(), enemySlots.toList(), lang) {
        WildRiftRepository.analyzeDraft(
            myRole = activeRole,
            allies = allySlots.map { it.champion },
            enemies = enemySlots.map { it.champion },
            enemyLaneOpponent = enemyLaneOpponent,
            isFirstPick = isFirstPick,
            lang = lang
        )
    }

    val topBarTitle = when (mode) {
        MetaScreenMode.DRAFTING -> tr("Selección de Campeones")
        MetaScreenMode.TIER_LIST -> tr("Tier List & Campeones")
        MetaScreenMode.CATALOG -> tr("Catálogo")
    }

    if (isOverlay) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
        ) {
            when (mode) {
                MetaScreenMode.DRAFTING -> {
                    DraftAnalysisTab(
                        myChampion = myChampion,
                        activeRole = activeRole,
                        allySlots = allySlots,
                        enemySlots = enemySlots,
                        analysis = analysis,
                        isFirstPick = isFirstPick,
                        enemyLaneOpponent = enemyLaneOpponent,
                        onToggleFirstPick = { isFirstPick = !isFirstPick },
                        onChangeRole = { showRoleChangeDialog = true },
                        onPickAllyRole = { role ->
                            suggestedPickingRole = role
                            pickingForTeam = "ALLY"
                        },
                        onPickEnemyRole = { role ->
                            suggestedPickingRole = role
                            pickingForTeam = "ENEMY"
                        },
                        onRemoveAllyRole = { role ->
                            val idx = allySlots.indexOfFirst { it.assignedRole == role }
                            if (idx >= 0) allySlots.removeAt(idx)
                        },
                        onRemoveEnemyRole = { role ->
                            val idx = enemySlots.indexOfFirst { it.assignedRole == role }
                            if (idx >= 0) enemySlots.removeAt(idx)
                        },
                        onPickRecommendation = { champ ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val targetRole = activeRole ?: champ.primaryRole
                            val existingIndex = allySlots.indexOfFirst { it.assignedRole == targetRole }
                            if (existingIndex >= 0) {
                                allySlots[existingIndex] = DraftSlot(champ, targetRole)
                            } else {
                                if (allySlots.size >= 5) {
                                    allySlots.removeAt(allySlots.size - 1)
                                }
                                allySlots.add(0, DraftSlot(champ, targetRole))
                            }
                        },
                        onSelectChampion = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); selectedDetailChampion = it },
                        onOpenHistory = { showDraftHistoryScreen = true },
                        onClearAll = {
                            allySlots.clear()
                            enemySlots.clear()
                            android.widget.Toast.makeText(screenContext, "Equipos vaciados", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                MetaScreenMode.TIER_LIST -> {
                    val totalChamps = WildRiftRepository.champions.size

                    val tierTabs = listOf(
                        MetaNavTabItem(tr("Tier List")),
                        MetaNavTabItem(tr("Campeones"), totalChamps)
                    )

                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = HextechSurface,
                        contentColor = HextechCyan,
                        edgePadding = 12.dp,
                        indicator = { tabPositions ->
                            if (selectedTabIndex in tabPositions.indices) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                    color = HextechCyan,
                                    height = 3.dp
                                )
                            }
                        }
                    ) {
                        tierTabs.forEachIndexed { index, tabItem ->
                            val isSelected = selectedTabIndex == index
                            Tab(
                                selected = isSelected,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        modifier = Modifier
                                            .background(
                                                if (isSelected) HextechCyan.copy(alpha = 0.15f) else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                if (isSelected) 1.dp else 0.dp,
                                                if (isSelected) HextechCyan.copy(alpha = 0.6f) else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = tabItem.title,
                                            color = if (isSelected) HextechCyan else TextMuted,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.5.sp
                                        )
                                        if (tabItem.count != null) {
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        if (isSelected) HextechCyan else HextechSurfaceVariant,
                                                        RoundedCornerShape(10.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = tabItem.count.toString(),
                                                    color = if (isSelected) HextechDarkBg else TextSecondary,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                    if (selectedTabIndex == 0) {
                        TierListTab(onSelectChampion = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); selectedDetailChampion = it }, isPremium = isPremium)
                    } else {
                        ChampionsCatalogTab(onSelectChampion = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); selectedDetailChampion = it })
                    }
                }
                MetaScreenMode.CATALOG -> {
                    ChampionsCatalogTab(onSelectChampion = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); selectedDetailChampion = it })
                }
            }
        }
        return
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = topBarTitle,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tr(WildRiftRepository.CURRENT_PATCH_VERSION),
                            color = HextechCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("draft_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = tr("Volver"),
                            tint = HextechGold
                        )
                    }
                },
                actions = {
                    if (mode == MetaScreenMode.DRAFTING && isPremium) {
                        IconButton(
                            onClick = { showDraftHistoryScreen = true },
                            modifier = Modifier.testTag("nav_draft_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = tr("Historial de Drafts"),
                                tint = HextechGold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (mode) {
                MetaScreenMode.DRAFTING -> {
                    DraftAnalysisTab(
                        myChampion = myChampion,
                        activeRole = activeRole,
                        allySlots = allySlots,
                        enemySlots = enemySlots,
                        analysis = analysis,
                        isFirstPick = isFirstPick,
                        enemyLaneOpponent = enemyLaneOpponent,
                        onToggleFirstPick = { isFirstPick = !isFirstPick },
                        onChangeRole = { showRoleChangeDialog = true },
                        onPickAllyRole = { role ->
                            suggestedPickingRole = role
                            pickingForTeam = "ALLY"
                        },
                        onPickEnemyRole = { role ->
                            suggestedPickingRole = role
                            pickingForTeam = "ENEMY"
                        },
                        onRemoveAllyRole = { role ->
                            val idx = allySlots.indexOfFirst { it.assignedRole == role }
                            if (idx >= 0) allySlots.removeAt(idx)
                        },
                        onRemoveEnemyRole = { role ->
                            val idx = enemySlots.indexOfFirst { it.assignedRole == role }
                            if (idx >= 0) enemySlots.removeAt(idx)
                        },
                        onPickRecommendation = { champ ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val targetRole = activeRole ?: champ.primaryRole
                            val existingIndex = allySlots.indexOfFirst { it.assignedRole == targetRole }
                            if (existingIndex >= 0) {
                                allySlots[existingIndex] = DraftSlot(champ, targetRole)
                            } else {
                                if (allySlots.size >= 5) {
                                    allySlots.removeAt(allySlots.size - 1)
                                }
                                allySlots.add(0, DraftSlot(champ, targetRole))
                            }
                        },
                        onSelectChampion = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); selectedDetailChampion = it },
                        onOpenHistory = { showDraftHistoryScreen = true },
                        onClearAll = {
                            allySlots.clear()
                            enemySlots.clear()
                            android.widget.Toast.makeText(screenContext, "Equipos vaciados", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                MetaScreenMode.TIER_LIST -> {
                    val totalChamps = WildRiftRepository.champions.size

                    val tierTabs = listOf(
                        MetaNavTabItem(tr("Tier List")),
                        MetaNavTabItem(tr("Campeones"), totalChamps)
                    )

                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = HextechSurface,
                        contentColor = HextechCyan,
                        edgePadding = 12.dp,
                        indicator = { tabPositions ->
                            if (selectedTabIndex in tabPositions.indices) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                    color = HextechCyan,
                                    height = 3.dp
                                )
                            }
                        }
                    ) {
                        tierTabs.forEachIndexed { index, tabItem ->
                            val isSelected = selectedTabIndex == index
                            Tab(
                                selected = isSelected,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        modifier = Modifier
                                            .background(
                                                if (isSelected) HextechCyan.copy(alpha = 0.15f) else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                if (isSelected) 1.dp else 0.dp,
                                                if (isSelected) HextechCyan.copy(alpha = 0.6f) else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = tabItem.title,
                                            color = if (isSelected) HextechCyan else TextMuted,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.5.sp
                                        )
                                        if (tabItem.count != null) {
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        if (isSelected) HextechCyan else HextechSurfaceVariant,
                                                        RoundedCornerShape(10.dp)
                                                    )
                                                    .border(
                                                        0.5.dp,
                                                        if (isSelected) HextechGold else HextechCardBorder,
                                                        RoundedCornerShape(10.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "${tabItem.count}",
                                                    color = if (isSelected) HextechDarkBg else HextechGold,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }

                    AnimatedContent(
                        targetState = selectedTabIndex,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
                            } else {
                                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(slideOutHorizontally { width -> width } + fadeOut())
                            }
                        },
                        label = "tier_tab_animation"
                    ) { targetIndex ->
                        when (targetIndex) {
                            0 -> TierListTab(onSelectChampion = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); selectedDetailChampion = it }, isPremium = isPremium)
                            1 -> ChampionsCatalogTab(onSelectChampion = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); selectedDetailChampion = it })
                            else -> TierListTab(onSelectChampion = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); selectedDetailChampion = it }, isPremium = isPremium)
                        }
                    }
                }
                MetaScreenMode.CATALOG -> {
                    val totalItems = WildRiftRepository.items.size
                    val totalRunes = WildRiftRepository.runes.size
                    val totalSpells = WildRiftRepository.summonerSpells.size

                    val catalogTabs = listOf(
                        MetaNavTabItem(tr("Objetos"), totalItems),
                        MetaNavTabItem(tr("Runas"), totalRunes),
                        MetaNavTabItem(tr("Hechizos"), totalSpells)
                    )

                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = HextechSurface,
                        contentColor = HextechCyan,
                        edgePadding = 12.dp,
                        indicator = { tabPositions ->
                            if (selectedTabIndex in tabPositions.indices) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                    color = HextechCyan,
                                    height = 3.dp
                                )
                            }
                        }
                    ) {
                        catalogTabs.forEachIndexed { index, tabItem ->
                            val isSelected = selectedTabIndex == index
                            Tab(
                                selected = isSelected,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        modifier = Modifier
                                            .background(
                                                if (isSelected) HextechCyan.copy(alpha = 0.15f) else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                if (isSelected) 1.dp else 0.dp,
                                                if (isSelected) HextechCyan.copy(alpha = 0.6f) else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = tabItem.title,
                                            color = if (isSelected) HextechCyan else TextMuted,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.5.sp
                                        )
                                        if (tabItem.count != null) {
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        if (isSelected) HextechCyan else HextechSurfaceVariant,
                                                        RoundedCornerShape(10.dp)
                                                    )
                                                    .border(
                                                        0.5.dp,
                                                        if (isSelected) HextechGold else HextechCardBorder,
                                                        RoundedCornerShape(10.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "${tabItem.count}",
                                                    color = if (isSelected) HextechDarkBg else HextechGold,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }

                    AnimatedContent(
                        targetState = selectedTabIndex,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
                            } else {
                                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(slideOutHorizontally { width -> width } + fadeOut())
                            }
                        },
                        label = "catalog_tab_animation"
                    ) { targetIndex ->
                        when (targetIndex) {
                            0 -> ItemsCatalogTab()
                            1 -> RunesTab()
                            2 -> SpellsTab()
                            else -> ItemsCatalogTab()
                        }
                    }
                }
            }
        }
    }

    // Modal Champion Detail Sheet
    if (selectedDetailChampion != null) {
        ChampionDetailSheet(
            champion = selectedDetailChampion,
            onDismiss = { selectedDetailChampion = null }
        )
    }

    // Removed build creator dialog from catalog

    // Modal Champion Picker for Draft
    if (pickingForTeam != null) {
        DraftChampionPickerSheet(
            team = pickingForTeam!!,
            suggestedRole = suggestedPickingRole,
            alreadySelected = allySlots.map { it.champion.id } + enemySlots.map { it.champion.id },
            onChampionPicked = { champ, chosenRole ->
                val targetRole = suggestedPickingRole ?: chosenRole
                val roleOrder = listOf(LaneRole.TOP, LaneRole.JUNGLE, LaneRole.MID, LaneRole.ADC, LaneRole.SUPPORT)
                when (pickingForTeam) {
                    "MYSELF" -> {
                        val selfRole = suggestedPickingRole ?: activeRole ?: champ.primaryRole
                        val idx = allySlots.indexOfFirst { it.assignedRole == selfRole }
                        if (idx >= 0) {
                            allySlots[idx] = DraftSlot(champ, selfRole)
                        } else {
                            val targetOrder = roleOrder.indexOf(selfRole)
                            var insertPos = allySlots.indexOfFirst { roleOrder.indexOf(it.assignedRole) > targetOrder }
                            if (insertPos < 0) insertPos = allySlots.size
                            if (allySlots.size >= 5) allySlots.removeAt(allySlots.size - 1)
                            allySlots.add(insertPos, DraftSlot(champ, selfRole))
                        }
                    }
                    "ALLY" -> {
                        val idx = allySlots.indexOfFirst { it.assignedRole == targetRole }
                        if (idx >= 0) {
                            allySlots[idx] = DraftSlot(champ, targetRole)
                        } else {
                            val targetOrder = roleOrder.indexOf(targetRole)
                            var insertPos = allySlots.indexOfFirst { roleOrder.indexOf(it.assignedRole) > targetOrder }
                            if (insertPos < 0) insertPos = allySlots.size
                            if (allySlots.size >= 5) allySlots.removeAt(allySlots.size - 1)
                            allySlots.add(insertPos, DraftSlot(champ, targetRole))
                        }
                    }
                    "ENEMY" -> {
                        val idx = enemySlots.indexOfFirst { it.assignedRole == targetRole }
                        if (idx >= 0) {
                            enemySlots[idx] = DraftSlot(champ, targetRole)
                        } else {
                            val targetOrder = roleOrder.indexOf(targetRole)
                            var insertPos = enemySlots.indexOfFirst { roleOrder.indexOf(it.assignedRole) > targetOrder }
                            if (insertPos < 0) insertPos = enemySlots.size
                            if (enemySlots.size >= 5) enemySlots.removeAt(enemySlots.size - 1)
                            enemySlots.add(insertPos, DraftSlot(champ, targetRole))
                        }
                    }
                }
                pickingForTeam = null
            },
            onDismiss = { pickingForTeam = null }
        )
    }

    // Role Switch Dialog
    if (showRoleChangeDialog) {
        RoleChangeBottomSheet(
            currentRole = activeRole,
            onRoleSelected = {
                activeRole = it
                com.example.util.UserPreferences.setActiveDraftRole(screenContext, it)
                showRoleChangeDialog = false
            },
            onDismiss = { showRoleChangeDialog = false }
        )
    }
}

// ====================================================================
// TAB 1: CATÁLOGO DE CAMPEONES (BUSCADOR, FILTROS, IMÁGENES Y METAS)
// ====================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChampionsCatalogTab(
    isOverlay: Boolean = false,
    onSelectChampion: (Champion) -> Unit,
    horizontalPadding: androidx.compose.ui.unit.Dp = if (isOverlay) 4.dp else 16.dp
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isPremium by com.example.util.SubscriptionManager.isPremium.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val syncState by ChineseMetaSyncService.syncState.collectAsStateWithLifecycle()
    val currentTier by ChineseMetaSyncService.currentTier.collectAsStateWithLifecycle()
    val currentRegion by ChineseMetaSyncService.currentRegion.collectAsStateWithLifecycle()
    val favorites by FavoriteChampionsManager.favoritesFlow.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf<LaneRole?>(null) }
    var selectedTierFilter by remember { mutableStateOf<String?>(null) }
    var showOnlyFavorites by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(true) }
    var showFilterChips by remember { mutableStateOf(true) }

    val filteredChampions = remember(searchQuery, selectedRoleFilter, selectedTierFilter, showOnlyFavorites, favorites, syncState, currentRegion, WildRiftRepository.activeRegionName, WildRiftRepository.champions.toList()) {
        val trimmedQuery = searchQuery.trim()
        val isCn = currentRegion == "CN"
        val list = WildRiftRepository.champions.filter { champ ->
            val matchesQuery = trimmedQuery.isBlank() ||
                    champ.name.contains(trimmedQuery, ignoreCase = true)
            val matchesRole = selectedRoleFilter == null ||
                    champ.primaryRole == selectedRoleFilter ||
                    champ.secondaryRoles.contains(selectedRoleFilter)
            val matchesTier = selectedTierFilter == null || champ.tier == selectedTierFilter || (isCn && champ.cnTier == selectedTierFilter)
            val matchesFavorite = !showOnlyFavorites || favorites.contains(champ.id.lowercase())
            matchesQuery && matchesRole && matchesTier && matchesFavorite
        }
        if (selectedRoleFilter != null) {
            list.sortedWith(
                compareByDescending<Champion> { it.primaryRole == selectedRoleFilter }
                    .thenByDescending { it.tier == "S+" }
                    .thenByDescending { it.tier == "S" }
                    .thenByDescending { it.tier == "A+" }
                    .thenByDescending { it.winrate }
            )
        } else {
            list.sortedWith(
                compareByDescending<Champion> { it.tier == "S+" }
                    .thenByDescending { it.tier == "S" }
                    .thenByDescending { it.tier == "A+" }
                    .thenByDescending { it.winrate }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding)
    ) {
        if (isOverlay) Spacer(modifier = Modifier.height(4.dp)) else Spacer(modifier = Modifier.height(10.dp))

        if (!isOverlay) { TierSelectionPanel(currentTier, syncState, currentRegion, context, coroutineScope) }


        // Search Bar (Proporcionado y compacto en overlay)
        if (isOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(HextechSurface)
                    .border(1.dp, HextechCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp)
                    .testTag("champions_search_input"),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = HextechCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 10.5.sp),
                        singleLine = true,
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(HextechCyan),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = tr("Buscar campeón..."),
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .clickable { searchQuery = "" },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Limpiar",
                                tint = TextMuted,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("champions_search_input"),
                placeholder = { Text(tr("Buscar campeón por nombre o habilidad..."), color = TextMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HextechCyan) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = TextMuted)
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
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Collapsible Header for Role Filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { showFilterChips = !showFilterChips }
                .padding(vertical = 4.dp, horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = HextechCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = tr("Filtrar por Rol"),
                    color = HextechCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                if (!showFilterChips) {
                    Spacer(modifier = Modifier.width(6.dp))
                    val roleLabel = selectedRoleFilter?.displayName ?: "Todos los Roles"
                    Surface(
                        color = HextechGold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, HextechGold.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = tr(roleLabel),
                            color = HextechGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            IconButton(
                onClick = { showFilterChips = !showFilterChips },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (showFilterChips) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (showFilterChips) tr("Minimizar filtros") else tr("Expandir filtros"),
                    tint = HextechGold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = showFilterChips,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header con indicador de deslizamiento para líneas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = HextechCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tr("Filtrar por Línea"),
                            color = HextechCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(HextechCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "↔ " + tr("Desliza para ver más líneas"),
                            color = HextechCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Role & Favorites Filter Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(if (isOverlay) 4.dp else 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    // 1. Favoritos Filter Chip
                    val favCount = favorites.size
                    FilterChip(
                        selected = showOnlyFavorites,
                        onClick = {
                            if (isPremium) {
                                showOnlyFavorites = !showOnlyFavorites
                                if (showOnlyFavorites) selectedRoleFilter = null
                            } else {
                                android.widget.Toast.makeText(context, "Requiere suscripción Premium", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⭐ " + tr("Favoritos"), fontSize = if (isOverlay) 10.sp else 11.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "($favCount)",
                                    color = if (showOnlyFavorites) HextechDarkBg else HextechGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isOverlay) 9.5.sp else 10.5.sp
                                )
                                if (!isPremium) {
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (showOnlyFavorites) HextechDarkBg else HextechGold)
                                            .padding(horizontal = 2.5.dp, vertical = 0.5.dp)
                                    ) {
                                        Text(
                                            text = "PRO",
                                            color = if (showOnlyFavorites) HextechGold else HextechDarkBg,
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HextechGold,
                            selectedLabelColor = HextechDarkBg
                        )
                    )

                    // 2. Todos los Roles
                    val totalCount = WildRiftRepository.champions.size
                    val isAllSelected = selectedRoleFilter == null && !showOnlyFavorites
                    FilterChip(
                        selected = isAllSelected,
                        onClick = {
                            selectedRoleFilter = null
                            showOnlyFavorites = false
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (isOverlay) tr("Todos") else tr("Todos los Roles"), fontSize = if (isOverlay) 10.sp else 11.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "($totalCount)",
                                    color = if (isAllSelected) HextechDarkBg else HextechGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isOverlay) 9.5.sp else 10.5.sp
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HextechCyan,
                            selectedLabelColor = HextechDarkBg
                        )
                    )
                    LaneRole.entries.forEach { role ->
                        val count = WildRiftRepository.champions.count { it.primaryRole == role || it.secondaryRoles.contains(role) }
                        val isSelected = selectedRoleFilter == role && !showOnlyFavorites
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                showOnlyFavorites = false
                                selectedRoleFilter = if (selectedRoleFilter == role) null else role
                            },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(tr(role.shortName), fontSize = if (isOverlay) 10.sp else 11.sp)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "($count)",
                                        color = if (isSelected) HextechDarkBg else HextechGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (isOverlay) 9.5.sp else 10.5.sp
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HextechCyan,
                                selectedLabelColor = HextechDarkBg
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Champions List or Empty State
        if (filteredChampions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 32.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (showOnlyFavorites) Icons.Default.Star else Icons.Default.Search,
                        contentDescription = null,
                        tint = if (showOnlyFavorites) HextechGold else HextechCyan,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (showOnlyFavorites) tr("No tienes campeones favoritos") else tr("No se encontraron campeones"),
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (showOnlyFavorites)
                            tr("Toca la estrella ⭐ en cualquier campeón de la lista para añadirlo a tus favoritos y tener acceso directo.")
                        else
                            tr("Prueba a buscar con otro nombre o restablece los filtros."),
                        color = TextMuted,
                        fontSize = 12.5.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
            items(filteredChampions) { champion ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectChampion(champion) }
                        .testTag("champion_item_${champion.id}"),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChampionAvatar(champion = champion, size = if (isOverlay) 42.dp else 58.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    val isFav = favorites.contains(champion.id.lowercase())
                                    IconButton(
                                        onClick = { 
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            if (isPremium) {
                                                FavoriteChampionsManager.toggleFavorite(context, champion.id) 
                                            } else {
                                                android.widget.Toast.makeText(context, "Requiere suscripción Premium", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier
                                            .size(24.dp)
                                            .testTag("fav_btn_${champion.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = if (isFav) tr("Quitar de Favoritos") else tr("Marcar como Favorito"),
                                            tint = if (isFav) HextechGold else TextMuted.copy(alpha = 0.35f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = champion.name,
                                        color = TextPrimary,
                                        fontSize = if (isOverlay) 13.sp else 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (!isOverlay) { Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val winDelta = champion.winrateDelta
                                    val formattedDelta = String.format(java.util.Locale.US, "%.2f", winDelta)
                                    val winDeltaText = if (winDelta >= 0) "+${formattedDelta}%" else "${formattedDelta}%"
                                    val winDeltaColor = if (winDelta >= 0) Color(0xFF4CAF50) else DangerRed
                                    Text(
                                        text = if (winDelta >= 0) "▲ $winDeltaText" else "▼ $winDeltaText",
                                        color = winDeltaColor,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val formattedWr = String.format(java.util.Locale.US, "%.2f", champion.winrate)
                                    val regionTag = when (currentRegion) {
                                        "CN" -> "🇨🇳 CN"
                                        "NA" -> "🌎 NA"
                                        else -> "🌍 Global"
                                    }
                                    Text(
                                        text = "$regionTag WR: $formattedWr%",
                                        color = HextechGold,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            val roleFilter = selectedRoleFilter
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (roleFilter != null && champion.primaryRole != roleFilter) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(HextechGold.copy(alpha = 0.2f))
                                            .border(1.dp, HextechGold.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "⭐ " + tr("Flex en ") + tr(roleFilter.shortName),
                                            color = HextechGold,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "Principal: ${com.example.util.tr(champion.primaryRole.shortName)} • ${com.example.util.tr(champion.damageType.displayName)}",
                                        color = HextechCyan,
                                        fontSize = 11.sp
                                    )
                                } else {
                                    Text(
                                        text = "${com.example.util.tr(champion.primaryRole.displayName)} • ${com.example.util.tr(champion.damageType.displayName)}",
                                        color = HextechCyan,
                                        fontSize = if (isOverlay) 9.5.sp else 11.5.sp
                                    )
                                    if (champion.secondaryRoles.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(HextechCyan.copy(alpha = 0.15f))
                                                .border(0.5.dp, HextechCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            val lang = com.example.util.LocalLanguage.current
                                            Text(
                                                text = tr("Flex: ") + champion.secondaryRoles.joinToString("/") { com.example.util.translations[lang]?.get(it.shortName) ?: it.shortName },
                                                color = HextechCyan,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = champion.summary,
                                color = TextMuted,
                                fontSize = if (isOverlay) 9.5.sp else 11.sp,
                                maxLines = if (isOverlay) 1 else 2,
                                lineHeight = if (isOverlay) 12.sp else 15.sp,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Skill icons preview
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                champion.skills.take(5).forEach { skill ->
                                    AppAssetImage(
                                        url = skill.iconUrl,
                                        contentDescription = skill.name,
                                        fallbackText = skill.slot,
                                        modifier = Modifier.size(20.dp),
                                        borderColor = HextechCyan.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Toca para ver build y runas",
                                    color = TextPrimary,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
}

// ====================================================================
// TAB 2: TIER LIST OFICIAL WILD RIFT (POR LÍNEAS Y TIERS)
// ====================================================================
enum class TierSortOption(val displayName: String, val shortLabel: String) {
    BY_TIER("Por Tier", "Tier "),
    WIN_RATE("Win Rate", "Win Rate "),
    PICK_RATE("Pick Rate", "Pick Rate "),
    BAN_RATE("Ban Rate", "Ban Rate ")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TierListTab(
    isOverlay: Boolean = false,
    onSelectChampion: (Champion) -> Unit,
    isPremium: Boolean = false,
    horizontalPadding: androidx.compose.ui.unit.Dp = if (isOverlay) 4.dp else 16.dp
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val syncState by ChineseMetaSyncService.syncState.collectAsStateWithLifecycle()
    val currentTier by ChineseMetaSyncService.currentTier.collectAsStateWithLifecycle()
    val currentRegion by ChineseMetaSyncService.currentRegion.collectAsStateWithLifecycle()

    val favorites by FavoriteChampionsManager.favoritesFlow.collectAsStateWithLifecycle()
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var selectedLane by remember { mutableStateOf<LaneRole?>(null) }
    var selectedSort by remember { mutableStateOf(TierSortOption.BY_TIER) }

    val rawChampionsToDisplay = remember(selectedLane, showFavoritesOnly, favorites, syncState, currentTier, currentRegion, WildRiftRepository.champions.toList()) {
        val champs = if (selectedLane == null) WildRiftRepository.champions.toList()
        else WildRiftRepository.getChampionsByRole(selectedLane!!)
        if (showFavoritesOnly) champs.filter { it.id in favorites } else champs
    }

    val championsToDisplay = remember(rawChampionsToDisplay, selectedSort, currentTier, currentRegion) {
        when (selectedSort) {
            TierSortOption.BY_TIER -> rawChampionsToDisplay
            TierSortOption.WIN_RATE -> rawChampionsToDisplay.sortedByDescending { it.winrate }
            TierSortOption.PICK_RATE -> rawChampionsToDisplay.sortedByDescending { it.pickRate }
            TierSortOption.BAN_RATE -> rawChampionsToDisplay.sortedByDescending { it.banRate }
        }
    }

    val isCn = currentRegion == "CN"
    val tierSPlus = championsToDisplay.filter { it.tier == "S+" || (isCn && it.cnTier == "T0") }
    val tierS = championsToDisplay.filter { (it.tier == "S" || (isCn && it.cnTier == "T1")) && it !in tierSPlus }
    val tierA = championsToDisplay.filter { (it.tier == "A+" || it.tier == "A" || (isCn && (it.cnTier == "T2" || it.cnTier == "T3"))) && it !in tierSPlus && it !in tierS }
    val tierB = championsToDisplay.filter { (it.tier == "B" || it.tier == "B+" || (isCn && it.cnTier == "T4")) && it !in tierSPlus && it !in tierS && it !in tierA }
    val tierC = championsToDisplay.filter { (it.tier == "C" || it.tier == "C+" || (isCn && it.cnTier == "T5")) && it !in tierSPlus && it !in tierS && it !in tierA && it !in tierB }
    val tierD = championsToDisplay.filter { it !in tierSPlus && it !in tierS && it !in tierA && it !in tierB && it !in tierC }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            TierSelectionPanel(currentTier, syncState, currentRegion, context, coroutineScope, isOverlay = isOverlay)
        }

        item {
            // Header con indicador de deslizamiento para líneas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = HextechCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tr("Filtrar por Línea"),
                        color = HextechCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(HextechCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "↔ " + tr("Desliza para ver más líneas"),
                        color = HextechCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Role Filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(if (isOverlay) 4.dp else 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = showFavoritesOnly,
                    onClick = { 
                        if (isPremium) {
                            showFavoritesOnly = !showFavoritesOnly 
                        } else {
                            Toast.makeText(context, "Requiere Premium", Toast.LENGTH_SHORT).show()
                        }
                    },
                    label = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(tr("Favoritos"), fontSize = if (isOverlay) 10.sp else 11.5.sp)
                            if (!isPremium) {
                                Spacer(modifier = Modifier.width(3.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(HextechGold)
                                        .padding(horizontal = 2.5.dp, vertical = 0.5.dp)
                                ) {
                                    Text("PRO", color = HextechDarkBg, fontSize = 7.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = HextechCyan,
                        selectedLabelColor = HextechDarkBg
                    ),
                    leadingIcon = {
                        if (showFavoritesOnly) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(if (isOverlay) 14.dp else 16.dp))
                        } else {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(if (isOverlay) 14.dp else 16.dp), tint = if (isPremium) TextPrimary else TextMuted)
                        }
                    }
                )
                FilterChip(
                    selected = selectedLane == null,
                    onClick = { selectedLane = null },
                    label = { Text(if (isOverlay) tr("Todas") else tr("Todas las Líneas"), fontSize = if (isOverlay) 10.sp else 11.5.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = HextechCyan,
                        selectedLabelColor = HextechDarkBg
                    )
                )
                LaneRole.entries.forEach { role ->
                    FilterChip(
                        selected = selectedLane == role,
                        onClick = { selectedLane = if (selectedLane == role) null else role },
                        leadingIcon = {
                            Image(
                                painter = painterResource(id = role.iconResId),
                                contentDescription = null,
                                modifier = Modifier.size(if (isOverlay) 13.dp else 16.dp)
                            )
                        },
                        label = { Text(tr(role.shortName), fontSize = if (isOverlay) 10.sp else 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HextechCyan,
                            selectedLabelColor = HextechDarkBg
                        )
                    )
                }
            }
        }

        item {
            // Header con indicador de deslizamiento para orden
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = HextechGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tr("Criterio de Orden"),
                        color = HextechGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "↔ " + tr("Desliza opciones"),
                    color = TextMuted,
                    fontSize = 9.sp
                )
            }

            // Sorting Selector (Por Tier, Win Rate, Pick Rate, Ban Rate)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(if (isOverlay) 4.dp else 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TierSortOption.entries.forEach { sortOpt ->
                    val isSelected = selectedSort == sortOpt
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSort = sortOpt },
                        label = { Text(tr(sortOpt.shortLabel), fontSize = if (isOverlay) 9.5.sp else 10.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HextechGold,
                            selectedLabelColor = HextechDarkBg
                        )
                    )
                }
            }
        }

        if (selectedSort == TierSortOption.BY_TIER) {
            // Tier S+ / T0
            if (tierSPlus.isNotEmpty()) {
                item {
                    TierSectionCard(
                        isOverlay = isOverlay,
                        tierName = if (isCn) "TIER S+ / T0 (${tr("Dominantes en")} ${tr(currentTier.displayName)})" else "TIER S+ (${tr("Dominantes / Prioridad Pick & Ban")})",
                        tierColor = TierSPlusColor,
                        champions = tierSPlus,
                        onSelectChampion = onSelectChampion
                    )
                }
            }

            // Tier S / T1
            if (tierS.isNotEmpty()) {
                item {
                    TierSectionCard(
                        isOverlay = isOverlay,
                        tierName = if (isCn) "TIER S / T1 (${tr("Meta Muy Fuerte / Alta Prioridad")})" else "TIER S (${tr("Meta Muy Fuerte / Alta Prioridad")})",
                        tierColor = TierSColor,
                        champions = tierS,
                        onSelectChampion = onSelectChampion
                    )
                }
            }

            // Tier A / T2-T3
            if (tierA.isNotEmpty()) {
                item {
                    TierSectionCard(
                        isOverlay = isOverlay,
                        tierName = if (isCn) "TIER A / T2-T3 (${tr("Opciones Sólidas y Balanceadas")})" else "TIER A (${tr("Opciones Sólidas y Balanceadas")})",
                        tierColor = TierAColor,
                        champions = tierA,
                        onSelectChampion = onSelectChampion
                    )
                }
            }
            
            // Tier B / T4
            if (tierB.isNotEmpty()) {
                item {
                    TierSectionCard(
                        isOverlay = isOverlay,
                        tierName = if (isCn) "TIER B / T4 (${tr("Opciones Viables")})" else "TIER B (${tr("Opciones Viables")})",
                        tierColor = com.example.ui.theme.TierBColor,
                        champions = tierB,
                        onSelectChampion = onSelectChampion
                    )
                }
            }

            // Tier C / T5
            if (tierC.isNotEmpty()) {
                item {
                    TierSectionCard(
                        isOverlay = isOverlay,
                        tierName = if (isCn) "TIER C / T5 (${tr("Situacionales")})" else "TIER C (${tr("Situacionales")})",
                        tierColor = com.example.ui.theme.TierCColor,
                        champions = tierC,
                        onSelectChampion = onSelectChampion
                    )
                }
            }

            // Tier D
            if (tierD.isNotEmpty()) {
                item {
                    TierSectionCard(
                        isOverlay = isOverlay,
                        tierName = if (isCn) "TIER D / OTROS (${tr("Fuera del Meta")})" else "TIER D / OTROS",
                        tierColor = com.example.ui.theme.TierDColor,
                        champions = tierD,
                        onSelectChampion = onSelectChampion
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        } else {
            // Sorted Ranked List by Win Rate, Pick Rate, or Ban Rate
            item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${tr("Clasificación por")} ${tr(selectedSort.displayName)} (${championsToDisplay.size})",
                            color = HextechGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                itemsIndexed(championsToDisplay) { index, champ ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectChampion(champ) },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechSurface),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when {
                                index == 0 -> HextechGold
                                index < 3 -> HextechCyan
                                else -> HextechCardBorder
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Rank Badge
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (index) {
                                                0 -> HextechGold
                                                1 -> HextechCyan
                                                2 -> Color(0xFFCD7F32)
                                                else -> HextechSurfaceVariant
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = if (index < 3) HextechDarkBg else TextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                ChampionAvatar(champion = champ, size = 44.dp, showTierBadge = true)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(champ.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        text = "${com.example.util.tr(champ.primaryRole.shortName)} • ${com.example.util.tr(champ.damageType.displayName)}",
                                        color = HextechCyan,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (!isOverlay) { SparklineTrendGraph(
                                    winrate = champ.winrate,
                                    delta = champ.winrateDelta,
                                    modifier = Modifier.width(46.dp).height(22.dp)
                                ) }
                                Column(horizontalAlignment = Alignment.End) {
                                    when (selectedSort) {
                                        TierSortOption.WIN_RATE -> {
                                            Text("WR: ${String.format(java.util.Locale.US, "%.2f", champ.winrate)}%", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("Pick: ${String.format(java.util.Locale.US, "%.2f", champ.pickRate)}% • Ban: ${String.format(java.util.Locale.US, "%.2f", champ.banRate)}%", color = TextMuted, fontSize = 10.sp)
                                        }
                                        TierSortOption.PICK_RATE -> {
                                            Text("Pick: ${String.format(java.util.Locale.US, "%.2f", champ.pickRate)}%", color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("WR: ${String.format(java.util.Locale.US, "%.2f", champ.winrate)}% • Ban: ${String.format(java.util.Locale.US, "%.2f", champ.banRate)}%", color = TextMuted, fontSize = 10.sp)
                                        }
                                        TierSortOption.BAN_RATE -> {
                                            Text("Ban: ${String.format(java.util.Locale.US, "%.2f", champ.banRate)}%", color = DangerRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("WR: ${String.format(java.util.Locale.US, "%.2f", champ.winrate)}% • Pick: ${String.format(java.util.Locale.US, "%.2f", champ.pickRate)}%", color = TextMuted, fontSize = 10.sp)
                                        }
                                        else -> {}
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
        }
    }
}

@Composable
fun TierSectionCard(
    isOverlay: Boolean = false,
    tierName: String,
    tierColor: Color,
    champions: List<Champion>,
    onSelectChampion: (Champion) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, tierColor.copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(tierColor)
                )
                Text(
                    text = tierName,
                    color = tierColor,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                champions.forEach { champ ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(HextechSurfaceVariant.copy(alpha = 0.6f))
                            .clickable { onSelectChampion(champ) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ChampionAvatar(champion = champ, size = 44.dp, showTierBadge = false)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(champ.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${com.example.util.tr(champ.primaryRole.shortName)} • ${com.example.util.tr(champ.damageType.displayName)}", color = HextechCyan, fontSize = 11.sp)
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!isOverlay) { SparklineTrendGraph(
                                winrate = champ.winrate,
                                delta = champ.winrateDelta,
                                modifier = Modifier.width(46.dp).height(22.dp)
                            ) }
                            Column(horizontalAlignment = Alignment.End) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val winDelta = champ.winrateDelta
                                    val formattedDelta = String.format(java.util.Locale.US, "%.2f", winDelta)
                                    val winDeltaText = if (winDelta >= 0) "+${formattedDelta}%" else "${formattedDelta}%"
                                    val winDeltaColor = if (winDelta >= 0) Color(0xFF4CAF50) else DangerRed
                                    if (!isOverlay) {
                                        Text(
                                            text = if (winDelta >= 0) "▲ $winDeltaText" else "▼ $winDeltaText",
                                            color = winDeltaColor,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(tr("WR") + ": ${String.format(java.util.Locale.US, "%.2f", champ.winrate)}%", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                }
                                if (!isOverlay) {
                                    Text("Pick: ${String.format(java.util.Locale.US, "%.2f", champ.pickRate)}% • Ban: ${String.format(java.util.Locale.US, "%.2f", champ.banRate)}%", color = TextMuted, fontSize = 10.sp)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

// ====================================================================
// TAB 3: CATÁLOGO DE OBJETOS (ITEMS) DE WILD RIFT
// ====================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ItemsCatalogTab() {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isGridView by remember { mutableStateOf(true) }
    var showFilterChips by remember { mutableStateOf(true) }
    var itemForDetail by remember { mutableStateOf<WildRiftItem?>(null) }

    val allItems = WildRiftRepository.items

    val allCategories = remember(allItems) {
        val cats = allItems.map { it.category }.distinct()
        val priority = listOf("Físico", "Physical", "Magia", "Magic", "Defensa", "Defense", "Botas", "Boots", "Encantamiento", "Enchantment")
        cats.sortedBy { cat ->
            val p = priority.indexOfFirst { cat.contains(it, ignoreCase = true) }
            if (p >= 0) p else 99
        }
    }

    val filterOptions = remember(allCategories, allItems) {
        val options = mutableListOf("TODOS" to "Todos")
        allCategories.forEach { cat ->
            options.add(cat to cat)
        }
        options
    }

    val lang = LocalLanguage.current
    val filteredItems = remember(selectedCategory, searchQuery, allItems, lang) {
        allItems.filter { item ->
            val matchesCategory = selectedCategory == null || item.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    item.getLocalizedName(lang).contains(searchQuery, ignoreCase = true) ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.nameEn.contains(searchQuery, ignoreCase = true) ||
                    item.namePt.contains(searchQuery, ignoreCase = true) ||
                    item.getLocalizedStats(lang).contains(searchQuery, ignoreCase = true) ||
                    item.getLocalizedPassive(lang).contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    val treeCategories = remember(filteredItems, allCategories) {
        val result = mutableListOf<Pair<String, List<WildRiftItem>>>()
        val groups = filteredItems.groupBy { it.category }
        
        allCategories.forEach { cat ->
            val itemsInCat = groups[cat]
            if (!itemsInCat.isNullOrEmpty()) {
                result.add(cat to itemsInCat)
            }
        }
        groups.forEach { (cat, itemsInCat) ->
            if (result.none { it.first == cat }) {
                result.add(cat to itemsInCat)
            }
        }
        result
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // WR-Meta Database Status Banner & View Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HextechSurfaceVariant, RoundedCornerShape(8.dp))
                .border(0.5.dp, HextechGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${filteredItems.size} ${tr("Objetos Oficiales")}",
                color = HextechCyan,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                // View mode toggle
                Row(
                    modifier = Modifier
                        .background(HextechSurface, RoundedCornerShape(6.dp))
                        .border(0.5.dp, HextechCardBorder, RoundedCornerShape(6.dp))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isGridView) HextechCyan else Color.Transparent)
                            .clickable { isGridView = true }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = tr("Cuadrícula"),
                            color = if (isGridView) HextechDarkBg else TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (!isGridView) HextechCyan else Color.Transparent)
                            .clickable { isGridView = false }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = tr("Detallado"),
                            color = if (!isGridView) HextechDarkBg else TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(tr("Buscar objeto por nombre o estadísticas..."), color = TextMuted, fontSize = 12.5.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HextechCyan) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = tr("Cerrar"), tint = TextMuted)
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
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Collapsible Header for Item Categories
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { showFilterChips = !showFilterChips }
                .padding(vertical = 4.dp, horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = HextechCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = tr("Filtrar por Categoría"),
                    color = HextechCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                if (!showFilterChips) {
                    Spacer(modifier = Modifier.width(6.dp))
                    val catLabel = selectedCategory ?: "Todos"
                    Surface(
                        color = HextechGold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, HextechGold.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = tr(catLabel),
                            color = HextechGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            IconButton(
                onClick = { showFilterChips = !showFilterChips },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (showFilterChips) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (showFilterChips) tr("Minimizar filtros") else tr("Expandir filtros"),
                    tint = HextechGold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = showFilterChips,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            // Category Filter Chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                filterOptions.forEach { (key, label) ->
                    val count = if (key == "TODOS") allItems.size else allItems.count { it.category.equals(key, ignoreCase = true) }
                    val isSelected = (selectedCategory == null && key == "TODOS") || (selectedCategory != null && selectedCategory.equals(key, ignoreCase = true))
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = if (key == "TODOS") null else key },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tr(label), fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "($count)",
                                    color = if (isSelected) HextechDarkBg else HextechGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HextechCyan,
                            selectedLabelColor = HextechDarkBg
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            treeCategories.forEachIndexed { catIdx, (categoryName, itemsInCat) ->
                item(key = "item_cat_${catIdx}_${categoryName}") {
                    val catColor = when {
                        categoryName.contains("físic", ignoreCase = true) || categoryName.contains("physic", ignoreCase = true) || categoryName.contains("ataque", ignoreCase = true) -> Color(0xFFFF8C00)
                        categoryName.contains("magi", ignoreCase = true) || categoryName.contains("magic", ignoreCase = true) || categoryName.contains("habilidad", ignoreCase = true) -> Color(0xFF60A5FA)
                        categoryName.contains("defen", ignoreCase = true) || categoryName.contains("tanque", ignoreCase = true) || categoryName.contains("vida", ignoreCase = true) -> Color(0xFF4ADE80)
                        categoryName.contains("bota", ignoreCase = true) || categoryName.contains("boot", ignoreCase = true) -> HextechGold
                        categoryName.contains("encant", ignoreCase = true) || categoryName.contains("enchant", ignoreCase = true) -> Color(0xFFE879F9)
                        else -> HextechCyan
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechSurface.copy(alpha = 0.95f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, catColor.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 10.dp)
                            ) {
                                Text(
                                    text = tr(categoryName).uppercase(),
                                    color = catColor,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.5.sp,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${itemsInCat.size})",
                                    color = catColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            if (isGridView) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    itemsInCat.forEach { item ->
                                        ItemGridCard(
                                            item = item,
                                            onClick = { itemForDetail = item },
                                            modifier = Modifier.width(68.dp),
                                            borderColor = catColor
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    itemsInCat.forEach { item ->
                                        ItemListCard(
                                            item = item,
                                            onClick = { itemForDetail = item },
                                            borderColor = catColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Item Detail Modal Dialog
    selectedRuneItemModal(itemForDetail) { itemForDetail = null }
}

@Composable
private fun selectedRuneItemModal(
    item: WildRiftItem?,
    onDismiss: () -> Unit
) {
    item?.let { itm ->
        val lang = LocalLanguage.current
        val localizedName = itm.getLocalizedName(lang)
        val statsList = itm.getStatsList(lang)
        val localizedPassive = itm.getLocalizedPassive(lang)
        val localizedCoachTip = itm.getLocalizedCoachTip(lang)

        androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HextechSurface),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, HextechGold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppAssetImage(
                        url = itm.iconUrl,
                        contentDescription = localizedName,
                        fallbackText = localizedName,
                        modifier = Modifier.size(72.dp),
                        borderColor = HextechGold,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = localizedName,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(HextechCyan.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = tr(itm.category),
                                color = HextechCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(HextechGold.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = " ${itm.goldCost} ${tr("Oro")}",
                                color = HextechGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (statsList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = tr("Estadísticas:"),
                            color = HextechGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            statsList.forEach { stat ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(HextechCyan, CircleShape)
                                    )
                                    Text(
                                        text = stat.parseHtmlColorToAnnotatedString(),
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    if (localizedPassive.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = tr("Efecto / Pasiva:"),
                            color = HextechGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FormattedWildRiftText(
                            text = localizedPassive,
                            color = TextPrimary.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (localizedCoachTip.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = HextechGold.copy(alpha = 0.08f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HextechGold.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("", fontSize = 13.sp)
                                    Text(
                                        text = tr("Consejos del Coach:"),
                                        color = HextechGoldLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = localizedCoachTip,
                                    color = TextPrimary.copy(alpha = 0.95f),
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.5.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(HextechCyan)
                            .clickable { onDismiss() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(tr("Cerrar"), color = HextechDarkBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemGridCard(
    item: WildRiftItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = HextechGold
) {
    val lang = LocalLanguage.current
    val localizedName = item.getLocalizedName(lang)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(HextechSurface.copy(alpha = 0.6f))
            .border(0.5.dp, HextechCardBorder.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(6.dp)
    ) {
        AppAssetImage(
            url = item.iconUrl,
            contentDescription = localizedName,
            fallbackText = localizedName,
            modifier = Modifier.size(42.dp),
            borderColor = borderColor,
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = localizedName,
            color = TextPrimary,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 11.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "${item.goldCost} G",
            color = HextechGold,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ItemListCard(
    item: WildRiftItem,
    onClick: () -> Unit,
    borderColor: Color = HextechCardBorder
) {
    val lang = LocalLanguage.current
    val localizedName = item.getLocalizedName(lang)
    val statsList = item.getStatsList(lang)
    val localizedPassive = item.getLocalizedPassive(lang)
    val localizedCoachTip = item.getLocalizedCoachTip(lang)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            AppAssetImage(
                url = item.iconUrl,
                contentDescription = localizedName,
                fallbackText = localizedName,
                modifier = Modifier.size(44.dp),
                borderColor = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(localizedName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    Text(" ${item.goldCost} G", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                Text(tr(item.category), color = HextechCyan, fontSize = 10.5.sp)
                if (statsList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        statsList.forEach { stat ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(HextechCyan, CircleShape)
                                )
                                Text(
                                    text = stat.parseHtmlColorToAnnotatedString(),
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                if (localizedPassive.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    FormattedWildRiftText(
                        text = localizedPassive,
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (localizedCoachTip.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("", fontSize = 10.sp)
                        Text(
                            text = localizedCoachTip,
                            color = HextechGoldLight.copy(alpha = 0.9f),
                            fontSize = 10.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ====================================================================
// TAB 4: CATÁLOGO EXCLUSIVO DE RUNAS (BÚSQUEDA Y RAMAS)
// ====================================================================
// TAB 4: CATÁLOGO EXCLUSIVO DE RUNAS
// ====================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RunesTab() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("TODOS") }
    var isGridView by remember { mutableStateOf(true) }
    var showFilterChips by remember { mutableStateOf(true) }
    var selectedRune by remember { mutableStateOf<RuneItem?>(null) }

    val allCategories = remember(com.example.data.WildRiftRepository.runes) {
        val cats = com.example.data.WildRiftRepository.runes.map { it.category }.distinct()
        cats.sortedBy { if (it.contains("Clave", ignoreCase = true) || it.contains("Keystone", ignoreCase = true)) 0 else 1 }
    }

    val filterOptions = remember(allCategories) {
        val options = mutableListOf("TODOS" to "Todos")
        allCategories.forEach { cat ->
            options.add(cat to cat)
        }
        options
    }

    val filteredRunes = remember(searchQuery, selectedFilter, com.example.data.WildRiftRepository.runes) {
        WildRiftRepository.runes.filter { rune ->
            val matchesCategory = selectedFilter == "TODOS" || rune.category.equals(selectedFilter, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    rune.name.contains(searchQuery, ignoreCase = true) ||
                    rune.description.contains(searchQuery, ignoreCase = true) ||
                    rune.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    val treeCategories = remember(filteredRunes) {
        val result = mutableListOf<Pair<String, List<RuneItem>>>()
        val groups = filteredRunes.groupBy { it.category }
        
        val claveKey = groups.keys.firstOrNull { it.contains("Clave", ignoreCase = true) || it.contains("Keystone", ignoreCase = true) }
        if (claveKey != null) {
            result.add(claveKey to (groups[claveKey] ?: emptyList()))
        }
        
        groups.forEach { (cat, items) ->
            if (cat != claveKey) {
                result.add(cat to items)
            }
        }
        result
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        // Status & View Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HextechSurfaceVariant, RoundedCornerShape(8.dp))
                .border(0.5.dp, HextechGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${filteredRunes.size} " + tr("Runas Oficiales"),
                color = HextechCyan,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            // View mode toggle
            Row(
                modifier = Modifier
                    .background(HextechSurface, RoundedCornerShape(6.dp))
                    .border(0.5.dp, HextechCardBorder, RoundedCornerShape(6.dp))
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isGridView) HextechCyan else Color.Transparent)
                        .clickable { isGridView = true }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = tr("Cuadrícula"),
                        color = if (isGridView) HextechDarkBg else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (!isGridView) HextechCyan else Color.Transparent)
                        .clickable { isGridView = false }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = tr("Detallado"),
                        color = if (!isGridView) HextechDarkBg else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(tr("Buscar runa (ej. Conquistador, Banda de Flujo)..."), color = TextMuted, fontSize = 12.5.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HextechCyan) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = tr("Cerrar"), tint = TextMuted)
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
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Collapsible Header for Rune Categories
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { showFilterChips = !showFilterChips }
                .padding(vertical = 4.dp, horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = HextechCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = tr("Filtrar por Categoría"),
                    color = HextechCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                if (!showFilterChips) {
                    Spacer(modifier = Modifier.width(6.dp))
                    val currentLabel = filterOptions.firstOrNull { it.first == selectedFilter }?.second ?: "Todos"
                    Surface(
                        color = HextechGold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, HextechGold.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = tr(currentLabel),
                            color = HextechGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            IconButton(
                onClick = { showFilterChips = !showFilterChips },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (showFilterChips) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (showFilterChips) tr("Minimizar filtros") else tr("Expandir filtros"),
                    tint = HextechGold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = showFilterChips,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            // Category Filter Chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                filterOptions.forEach { (key, label) ->
                    val count = if (key == "TODOS") WildRiftRepository.runes.size else WildRiftRepository.runes.count { it.category.equals(key, ignoreCase = true) }
                    val isSelected = selectedFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = key },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tr(label), fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "($count)",
                                    color = if (isSelected) HextechDarkBg else HextechGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HextechCyan,
                            selectedLabelColor = HextechDarkBg
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isGridView) {
            // GRID / TREE VIEW LIKE WR-META
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                treeCategories.forEachIndexed { catIdx, (categoryName, runesInCat) ->
                    item(key = "tree_cat_${catIdx}_${categoryName}") {
                        val catColor = when {
                            categoryName.contains("clave", ignoreCase = true) -> HextechGold
                            categoryName.contains("brujer", ignoreCase = true) -> Color(0xFF6C75F0)
                            categoryName.contains("dominac", ignoreCase = true) -> Color(0xFFE84057)
                            categoryName.contains("precis", ignoreCase = true) -> Color(0xFFF3C258)
                            categoryName.contains("valor", ignoreCase = true) -> Color(0xFF4AC27E)
                            categoryName.contains("inspirac", ignoreCase = true) -> HextechCyan
                            else -> HextechCyan
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = HextechSurface.copy(alpha = 0.95f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, catColor.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                ) {
                                    Text(
                                        text = tr(categoryName).uppercase(),
                                        color = catColor,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.5.sp,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${runesInCat.size})",
                                        color = catColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    runesInCat.forEach { rune ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier
                                                .width(68.dp)
                                                .height(86.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { selectedRune = rune }
                                                .background(HextechSurface.copy(alpha = 0.6f))
                                                .border(0.5.dp, HextechCardBorder.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 4.dp, vertical = 6.dp)
                                        ) {
                                            AppAssetImage(
                                                url = rune.iconUrl,
                                                contentDescription = rune.name,
                                                fallbackText = rune.name,
                                                modifier = Modifier.size(40.dp),
                                                borderColor = catColor,
                                                shape = CircleShape
                                            )
                                            Text(
                                                text = tr(rune.name),
                                                color = TextPrimary,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 2,
                                                minLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                lineHeight = 11.sp,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        } else {
            // DETAILED LIST VIEW
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredRunes, key = { rune -> "rune_det_${rune.id}_${rune.name}" }) { rune ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedRune = rune },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            AppAssetImage(
                                url = rune.iconUrl,
                                contentDescription = rune.name,
                                fallbackText = rune.name,
                                modifier = Modifier.size(44.dp),
                                borderColor = HextechCyan,
                                shape = CircleShape
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tr(rune.name), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(HextechCyan.copy(alpha = 0.12f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(tr(rune.category), color = HextechCyan, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                FormattedWildRiftText(
                                    text = tr(rune.description),
                                    color = TextPrimary.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }

    // Rune Detail Dialog
    selectedRune?.let { rune ->
        AlertDialog(
            onDismissRequest = { selectedRune = null },
            containerColor = HextechSurface,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppAssetImage(
                        url = rune.iconUrl,
                        contentDescription = rune.name,
                        fallbackText = rune.name,
                        modifier = Modifier.size(48.dp),
                        borderColor = HextechGold,
                        shape = CircleShape
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = tr(rune.name),
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = tr("Rama") + ": " + tr(rune.category),
                            color = HextechCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(HextechSurface, RoundedCornerShape(8.dp))
                            .border(0.5.dp, HextechCardBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        FormattedWildRiftText(
                            text = tr(rune.description),
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = tr(" Consejo del Coach:"),
                        color = HextechGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tr(when (rune.name.lowercase()) {
                            "electrocutar" -> " Ideal para combos cortos de asesinos o magos que buscan estallar a un rival rápido."
                            "cosecha oscura" -> " Perfecto para campeones que escalan y aseguran asesinatos en peleas largas (ej. Katarina, Khazix)."
                            "fortalecimiento" -> " Excelente para tiradores o luchadores que dependen de ataques básicos rápidos."
                            "compás letal", "cadencia letal" -> " Fundamental en hypercarries como Jinx o Vayne para dominar las peleas largas."
                            "pies veloces" -> " Útil para sobrevivir líneas difíciles gracias a su curación y movilidad al kitear."
                            "conquistador" -> " La mejor opción para luchadores y duelistas que buscan intercambios prolongados (ej. Darius, Riven)."
                            "garras del inmortal" -> " Indispensable en tanques y colosos para tener sustain y escalar vida máxima."
                            "guardián" -> " Selecciona esta runa en soportes protectores (ej. Braum, Lulu) para mitigar burst enemigo."
                            "aery", "invocar a aery" -> " Muy versátil para soportes encantadores o magos de pokeo constante (ej. Karma, Orianna)."
                            "cometa arcano" -> " Ideal para magos de artillería que pokean a distancia (ej. Ziggs, Lux)."
                            "irrupción de fase" -> " Perfecta para magos de combo que necesitan reposicionarse rápido (ej. Orianna, Vladimir)."
                            "primer golpe" -> " Útil en asesinos o magos de ráfaga para escalar en oro rápidamente y explotar objetivos."
                            "soberano gélido" -> " Excelente para soportes de iniciación (ej. Leona, Nautilus) para potenciar su CC."
                            "réplica" -> " Runa perfecta para tanques de iniciación masiva (ej. Amumu, Alistar) que necesitan resistir el focus enemigo post-combo."
                            "triunfo" -> " Ideal en peleas de equipo cerradas. Te recompensa con vida vital tras cada eliminación o asistencia."
                            "fervor de batalla" -> " Útil en intercambios sostenidos cortos, incrementa tu daño para asegurar duelos tempranos."
                            "derribado" -> " Obligatorio si el equipo enemigo tiene muchos tanques y campeones con mucha vida extra."
                            "golpe de gracia" -> " Para asesinos o ADC que buscan asegurar la baja (ejecutar) a enemigos que intenten escapar a baja vida."
                            "leyenda: presteza" -> " Escoge esta runa si priorizas maximizar tu DPS (daño por segundo) a través de ataques básicos rápidos."
                            "leyenda: tenacidad" -> " Vital si el equipo enemigo está lleno de control de masas (Stun, Inmovilización, etc). Evitará que te eliminen encadenado."
                            "leyenda: linaje" -> " Si tu campeón no armará Robo de Vida temprano pero necesita sustento para sobrevivir y farmear."
                            "último esfuerzo" -> " Excelente en duelistas como Olaf o Tryndamere que se vuelven más letales cuando se acercan a la muerte."
                            else -> " Runa situacional: Úsala para complementar el estilo de juego de tu campeón frente a esta composición específica."
                        }),
                        color = TextMuted,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedRune = null },
                    colors = ButtonDefaults.buttonColors(containerColor = HextechCyan, contentColor = HextechDarkBg),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(tr("Cerrar"), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}


// ====================================================================
// TAB 5: CATÁLOGO EXCLUSIVO DE HECHIZOS DE INVOCADOR
// ====================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpellsTab() {
    var searchQuery by remember { mutableStateOf("") }
    var isGridView by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("TODOS") }
    var selectedSpell by remember { mutableStateOf<SummonerSpellItem?>(null) }

    val allCategories = remember(com.example.data.WildRiftRepository.summonerSpells) {
        com.example.data.WildRiftRepository.summonerSpells.map { it.category }.distinct().sorted()
    }

    val filterOptions = remember(allCategories) {
        val options = mutableListOf("TODOS" to "Todos")
        allCategories.forEach { cat ->
            options.add(cat to cat)
        }
        options
    }

    val filteredSpells = remember(searchQuery, selectedFilter, com.example.data.WildRiftRepository.summonerSpells) {
        WildRiftRepository.summonerSpells.filter { spell ->
            val matchesFilter = selectedFilter == "TODOS" || spell.category.equals(selectedFilter, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    spell.name.contains(searchQuery, ignoreCase = true) ||
                    spell.description.contains(searchQuery, ignoreCase = true) ||
                    spell.category.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        // Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HextechSurfaceVariant, RoundedCornerShape(8.dp))
                .border(0.5.dp, HextechGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${filteredSpells.size} " + tr("Hechizos de Invocador"),
                color = HextechCyan,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            // View mode toggle
            Row(
                modifier = Modifier
                    .background(HextechSurface, RoundedCornerShape(6.dp))
                    .border(0.5.dp, HextechCardBorder, RoundedCornerShape(6.dp))
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isGridView) HextechCyan else Color.Transparent)
                        .clickable { isGridView = true }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = tr("Cuadrícula"),
                        color = if (isGridView) HextechDarkBg else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (!isGridView) HextechCyan else Color.Transparent)
                        .clickable { isGridView = false }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = tr("Detallado"),
                        color = if (!isGridView) HextechDarkBg else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(tr("Buscar hechizo (ej. Destello, Prender, Castigo)..."), color = TextMuted, fontSize = 12.5.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HextechGold) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = tr("Cerrar"), tint = TextMuted)
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
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            filterOptions.forEach { (key, label) ->
                FilterChip(
                    selected = selectedFilter == key,
                    onClick = { selectedFilter = key },
                    label = { Text(tr(label), fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = HextechCyan,
                        selectedLabelColor = HextechDarkBg
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 30.dp)
            ) {
                items(filteredSpells) { spell ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedSpell = spell },
                        colors = CardDefaults.cardColors(containerColor = HextechSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp, vertical = 8.dp)
                        ) {
                            AppAssetImage(
                                url = spell.iconUrl,
                                contentDescription = spell.name,
                                fallbackText = spell.name,
                                modifier = Modifier.size(48.dp),
                                borderColor = HextechCyan,
                                shape = RoundedCornerShape(10.dp)
                            )
                            Text(
                                text = tr(spell.name),
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 2,
                                minLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 13.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "CD ${spell.cooldown}",
                                color = HextechCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredSpells) { spell ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSpell = spell },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            AppAssetImage(
                                url = spell.iconUrl,
                                contentDescription = spell.name,
                                fallbackText = spell.name,
                                modifier = Modifier.size(44.dp),
                                borderColor = HextechCyan,
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tr(spell.name), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(HextechGold.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("CD: ${spell.cooldown}", color = HextechCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(HextechGold.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(tr(spell.category), color = HextechCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                FormattedWildRiftText(
                                    text = tr(spell.description),
                                    color = TextPrimary.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }

    // Spell Detail Dialog
    selectedSpell?.let { spell ->
        AlertDialog(
            onDismissRequest = { selectedSpell = null },
            containerColor = HextechSurface,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppAssetImage(
                        url = spell.iconUrl,
                        contentDescription = spell.name,
                        fallbackText = spell.name,
                        modifier = Modifier.size(48.dp),
                        borderColor = HextechCyan,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = tr(spell.name),
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = tr("Enfriamiento:") + " ${spell.cooldown}",
                            color = HextechCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(HextechSurface, RoundedCornerShape(8.dp))
                            .border(0.5.dp, HextechCardBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        FormattedWildRiftText(
                            text = tr(spell.description),
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = tr(" Recomendación de Invocador:"),
                        color = HextechCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tr(when (spell.id) {
                            "flash" -> "Imprescindible en el 99% de las partidas para reposicionarse, iniciar peleas de equipo o escapar por encima de muros."
                            "ignite" -> "Clave para asesinos y soportes agresivos para asegurar asesinatos en juego temprano y anular curaciones de campeones como Aatrox, Soraka o Dr. Mundo."
                            "smite" -> "Obligatorio para el rol de Jungla para asegurar monstruos épicos (Dragones, Heraldo, Barón) y farmear eficientemente."
                            "exhaust" -> "Vital para neutralizar a hipercarries o asesinos rivales en peleas grupales reduciendo su daño y movilidad drásticamente."
                            "barrier" -> "Excelente para tiradores o magos de ráfaga para resistir emboscadas o burst sorpresa en línea."
                            "ghost" -> "Ideal para campeones con movilidad continua como Darius, Olaf, Singed o Gwen para evitar que los enemigos escapen."
                            "heal" -> "Ideal para el Tirador (ADC) en la línea de Dragón para sobrevivir al burst y salvar al soporte en 2vs2."
                            else -> "Uso situacional según la composición y mapa."
                        }),
                        color = TextMuted,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedSpell = null },
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold, contentColor = HextechDarkBg),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(tr("Cerrar"), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// ====================================================================
// TAB 5: OBJETIVOS DE MAPA (MONSTRUOS ÉPICOS DE WILD RIFT)
// ====================================================================
@Composable
private fun MapObjectivesTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(tr("Monstruos Épicos & Tiempos de Aparición"), color = HextechGold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(tr("Conocer los tiempos exactos de aparición en Wild Rift asegura la victoria de tu equipo:"), color = TextMuted, fontSize = 11.5.sp)
        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            WildRiftRepository.mapObjectives.forEach { obj ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tr(obj.name), color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(HextechCyan.copy(alpha = 0.15f))
                                    .border(1.dp, HextechCyan.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(tr(obj.spawnTime), color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(tr("Reaparición:") + " " + tr(obj.respawnTime), color = TextMuted, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(tr("Mejora:") + " " + tr(obj.buffDescription), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(tr("Táctica:") + " " + tr(obj.tactics), color = TextPrimary.copy(alpha = 0.9f), fontSize = 11.5.sp, lineHeight = 15.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ====================================================================
// TAB 0: ANÁLISIS DE DRAFTING & COUNTERS
// ====================================================================

data class PendingSaveData(
    val result: String,
    val notes: String,
    val profileId: String,
    val profileName: String,
    val isLegendaryMatch: Boolean
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
    onClearAll: () -> Unit
) {
    val tabContext = LocalContext.current
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

    if (showMatchupDialog && myChampion != null) {
        val opponent = enemyLaneOpponent ?: enemySlots.firstOrNull()?.champion ?: myChampion
        MatchupPreviewDialog(
            myChampion = myChampion,
            enemyOpponent = opponent,
            activeRole = activeRole ?: myChampion.primaryRole,
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
            myChampion = myChampion,
            enemyLaneOpponent = enemyLaneOpponent,
            userRole = activeRole ?: LaneRole.MID,
            estimatedWinrate = analysis.bestOverallPick?.estimatedWinrate ?: 50.0,
            onDismiss = { showSaveDraftDialog = false },
            onSave = { result, notes, profileId, profileName, isLegendaryMatch ->
                coroutineScope.launch {
                    val exists = DraftHistoryRepository.checkDraftExists(
                        context = tabContext,
                        myRole = activeRole ?: LaneRole.MID,
                        allies = allySlots,
                        enemies = enemySlots,
                        accountProfileId = profileId
                    )
                    
                    if (exists) {
                        pendingSaveData = PendingSaveData(result, notes, profileId, profileName, isLegendaryMatch)
                    } else {
                        DraftHistoryRepository.saveDraft(
                            context = tabContext,
                            myRole = activeRole ?: LaneRole.MID,
                            isFirstPick = isFirstPick,
                            isLegendary = isLegendaryMatch,
                            allies = allySlots,
                            enemies = enemySlots,
                            analysis = analysis,
                            notes = notes,
                            matchResult = result,
                            accountProfileId = profileId,
                            accountProfileName = profileName
                        )
                        showSaveDraftDialog = false
                        val toastMsg = when (result) {
                            "VICTORY" -> victoryToastText
                            "DEFEAT" -> defeatToastText
                            else -> " $savedDraftToastText"
                        }
                        android.widget.Toast.makeText(tabContext, toastMsg, android.widget.Toast.LENGTH_SHORT).show()
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
                androidx.compose.material3.Button(
                    onClick = {
                        val data = pendingSaveData!!
                        pendingSaveData = null
                        showSaveDraftDialog = false
                        coroutineScope.launch {
                            DraftHistoryRepository.saveDraft(
                                context = tabContext,
                                myRole = activeRole ?: LaneRole.MID,
                                isFirstPick = isFirstPick,
                                isLegendary = data.isLegendaryMatch,
                                allies = allySlots,
                                enemies = enemySlots,
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
                            android.widget.Toast.makeText(tabContext, toastMsg, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.HextechGold)
                ) {
                    androidx.compose.material3.Text(tr("Sí"), color = androidx.compose.ui.graphics.Color.Black)
                }
            },
            dismissButton = {
                androidx.compose.material3.OutlinedButton(
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
                    .clickable { 
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                text = if (activeRole != null) com.example.util.tr(activeRole.displayName) else tr("Todas las líneas"),
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
                            text = if (activeRole != null) tr("Cambiar") else tr("Elegir"),
                            color = HextechCyan,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
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
                    .clickable { 
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
                    Column {
                        Text(
                            text = if (isFirstPick) tr("1er Pick") else tr("Counter Pick"),
                            color = if (isFirstPick) HextechGold else HextechCyan,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = if (isFirstPick) tr("Blind Pick") else tr("Adaptativo"),
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
                                android.widget.Toast.makeText(tabContext, "Selecciona tu línea primero", android.widget.Toast.LENGTH_SHORT).show()
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) { roleActivePill() }
                androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) { firstPickCard() }
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
                        if (activeRole == null) {
                            android.widget.Toast.makeText(tabContext, "Selecciona tu línea primero", android.widget.Toast.LENGTH_SHORT).show()
                        } else if (allySlots.size < 5 || enemySlots.size < 5) {
                            android.widget.Toast.makeText(tabContext, "Debes seleccionar los 10 campeones", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            showSaveDraftDialog = true
                        }
                    } else {
                        android.widget.Toast.makeText(tabContext, "Requiere suscripción Premium", android.widget.Toast.LENGTH_SHORT).show()
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
                                text = "PRO",
                                color = HextechDarkBg,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
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
            slots = allySlots,
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
            slots = enemySlots,
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
                        Text(analysis.directMatchupWarning, color = TextPrimary, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Sinergias Letales y Wombo-Combos Detectados
        val allAllyChamps = remember(allySlots.toList(), myChampion) {
            (allySlots.map { it.champion } + listOfNotNull(myChampion)).distinctBy { it.id }
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
        if (allySlots.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(tr("Balance de Daño Aliado"), color = HextechGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                if (analysis.allyCompositionWarning != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(analysis.allyCompositionWarning, color = DangerRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                Text("${analysis.allyPhysicalDamagePercent}% " + tr("Físico"), color = Color(0xFFE57373), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Text("${analysis.allyMagicDamagePercent}% " + tr("Mágico"), color = Color(0xFF64B5F6), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Text("${analysis.allyTrueDamagePercent}% " + tr("Verdadero"), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Damage distribution
        if (enemySlots.isNotEmpty()) {
            Text(tr("Balance de Daño Rival"), color = HextechGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                Text("${analysis.physicalDamagePercent}% " + tr("Físico"), color = Color(0xFFE57373), fontSize = 10.sp)
                Text("${analysis.magicDamagePercent}% " + tr("Mágico"), color = Color(0xFF64B5F6), fontSize = 10.sp)
                Text("${analysis.trueDamagePercent}% " + tr("Verdadero"), color = Color.White, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // My Champion Evaluation (Sincronizado automáticamente sin selecciones duplicadas)
        if (myChampion != null) {
            val myChamp = myChampion
            val myEval = WildRiftRepository.evaluateChampion(
                champ = myChamp,
                myRole = activeRole ?: myChamp.primaryRole,
                allies = allySlots.map { it.champion },
                enemies = enemySlots.map { it.champion },
                enemyLaneOpponent = enemyLaneOpponent,
                lang = "es"
            )
            val isOffRole = activeRole != null && myChamp.primaryRole != activeRole && !myChamp.secondaryRoles.contains(activeRole)
            val isDirectLaneWeakness = enemyLaneOpponent != null && (
                myChamp.counteredBy.any { it.equals(enemyLaneOpponent.name, ignoreCase = true) || it.equals(enemyLaneOpponent.id, ignoreCase = true) } ||
                enemyLaneOpponent.advantageAgainst.any { it.equals(myChamp.name, ignoreCase = true) || it.equals(myChamp.id, ignoreCase = true) }
            )
            val shouldChange = isOffRole || myEval.advantageBadge.contains("ATÍPICA") || (myEval.estimatedWinrate < 48.0) || (isDirectLaneWeakness && myEval.estimatedWinrate < 50.0)
            
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
                                text = if (activeRole != null) tr("TU ELECCIÓN EN") + " ${com.example.util.tr(activeRole.displayName).uppercase()}" else tr("TU ELECCIÓN (GENERAL)"),
                                color = if (shouldChange) DangerRed else HextechGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = tr("Winrate Est.:") + " ${myEval.estimatedWinrate}%",
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
                                Text(myEval.champion.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tier ${myEval.champion.tier}",
                                    color = HextechGold,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = recommendationText,
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
                    Text(myEval.tacticalReason, color = TextPrimary.copy(alpha = 0.9f), fontSize = 12.sp, lineHeight = 16.sp)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tr("Toca para ver build completa"),
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // 1v1 Matchup Preview Trigger Button
                    if (enemyLaneOpponent != null || enemySlots.isNotEmpty()) {
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
                    text = if (activeRole != null) tr("SELECCIONAR MI PICK PARA") + " ${com.example.util.tr(activeRole.displayName).uppercase()}" else tr("SELECCIONAR MI CAMPEÓN (GLOBAL)"),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Live Recommendations Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = (activeRole ?: LaneRole.MID).iconResId),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (activeRole != null) {
                    if (isFirstPick) tr("Mejor Primer Pick Seguro para") + " ${com.example.util.tr(activeRole.displayName)}" else tr("Mejor Opción según tu Equipo y el Rival")
                } else {
                    tr("Mejores Opciones Globales según tu Equipo y el Rival")
                },
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
                    .clickable { onSelectChampion(topPick.champion) }
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
                            text = if (isFirstPick) tr(" #1 RECOMENDACIÓN BLIND PICK") else tr(" #1 MEJOR ELECCIÓN TÁCTICA"),
                            color = HextechGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = tr("Winrate Est.:") + " ${topPick.estimatedWinrate}%",
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
                                    text = topPick.champion.name,
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tier ${topPick.champion.tier}",
                                    color = TierSPlusColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = topPick.advantageBadge,
                                color = HextechCyan,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = topPick.tacticalReason,
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
                                        text = "🤝 " + tr("Sinergia / Combo:"),
                                        color = HextechCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = topPick.synergyDetails,
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            if (topPick.counterDetails.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "🛡️ " + tr("Ventaja / Counter:"),
                                        color = HextechGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = topPick.counterDetails,
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
                            text = tr("Toca para ver build completa"),
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
        if (otherRecs.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = (activeRole ?: LaneRole.MID).iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (activeRole != null) tr("Otras Opciones Viables para") + " ${com.example.util.tr(activeRole.displayName)}:" else tr("Otras Opciones Viables (Todas las Líneas):"),
                    color = HextechCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            otherRecs.forEach { rec ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectChampion(rec.champion) },
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
                                    Text(rec.champion.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("WR: ${rec.estimatedWinrate}%", color = HextechGold, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(rec.advantageBadge, color = HextechCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(rec.tacticalReason, color = TextMuted, fontSize = 11.sp, lineHeight = 14.sp)
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
                                        text = "🤝 " + rec.synergyDetails,
                                        color = HextechCyan,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rec.counterDetails.isNotBlank()) {
                                    Text(
                                        text = "🛡️ " + rec.counterDetails,
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
            .clickable { onClick() },
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
                            text = slot.champion.name,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isMyPick) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = " Mío",
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
                                .clickable { showRoleMenu = true }
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
                                text = roleLabel,
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
            .clickable { onClick() },
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
private fun DraftChampionPickerSheet(
    team: String,
    suggestedRole: LaneRole?,
    alreadySelected: List<String>,
    onChampionPicked: (Champion, LaneRole) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var search by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf<LaneRole?>(null) }

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
        containerColor = HextechSurfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = when (team) {
                    "MYSELF" -> tr("Seleccionar Mi Campeón") + if (suggestedRole != null) " (${com.example.util.tr(suggestedRole.displayName)})" else ""
                    "ALLY" -> tr("Seleccionar Campeón Aliado")
                    else -> tr("Seleccionar Campeón Rival")
                },
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
                            Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = TextMuted)
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
                            .clickable {
                                val assignedRole = suggestedRole ?: selectedRoleFilter ?: champ.primaryRole
                                onChampionPicked(champ, assignedRole)
                            }
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ChampionAvatar(champion = champ, size = 56.dp, showTierBadge = false)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = champ.name,
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
private fun RoleChangeBottomSheet(
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
                            .clickable { onRoleSelected(role) }
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

@Composable
private fun ChampionGridCard(
    champion: Champion,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("champion_item_${champion.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ChampionAvatar(champion = champion, size = 52.dp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = champion.name,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .background(HextechSurfaceVariant, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "WR: ${String.format(java.util.Locale.US, "%.2f", champion.winrate)}%",
                    color = HextechGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RuneGridCard(
    rune: RuneItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppAssetImage(
                url = rune.iconUrl,
                contentDescription = rune.name,
                fallbackText = rune.name,
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                borderColor = HextechGold.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = tr(rune.name),
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .background(HextechSurfaceVariant, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = tr(rune.category),
                    color = HextechCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SpellGridCard(
    spell: SummonerSpellItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppAssetImage(
                url = spell.iconUrl,
                contentDescription = spell.name,
                fallbackText = spell.name,
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(8.dp),
                borderColor = HextechGold.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = tr(spell.name),
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .background(HextechSurfaceVariant, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = spell.cooldown,
                    color = HextechGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
fun TierSelectionPanel(
    currentTier: TencentRankTier,
    syncState: ChineseSyncState,
    currentRegion: String,
    context: android.content.Context,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    isOverlay: Boolean = false
) {
    val isOnline by BestBuildWrScraper.isOnline.collectAsStateWithLifecycle()
    val isSyncing by BestBuildWrScraper.isSyncing.collectAsStateWithLifecycle()
    val lastSyncFormattedTime by BestBuildWrScraper.lastSyncFormattedTime.collectAsStateWithLifecycle()
    var showMultiServerStats by remember { mutableStateOf(false) }

    if (showMultiServerStats) {
        com.example.ui.components.MultiServerStatsDialog(
            onDismiss = { showMultiServerStats = false }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        shape = RoundedCornerShape(if (isOverlay) 10.dp else 14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, HextechGold.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(if (isOverlay) 8.dp else 12.dp)) {
            // CABECERA: 🌐 Servidor / Meta: | Botón Estadísticas + Botón de Actualizar con estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🌐 " + tr("Servidor / Meta:"),
                        color = HextechGold,
                        fontSize = if (isOverlay) 11.5.sp else 13.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón Estadísticas Multi-Servidor
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(HextechGold.copy(alpha = 0.2f))
                            .border(0.8.dp, HextechGold, RoundedCornerShape(6.dp))
                            .clickable { showMultiServerStats = true }
                            .padding(horizontal = 8.dp, vertical = 3.5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "📊 " + tr("Estadísticas"),
                                color = HextechGold,
                                fontSize = if (isOverlay) 8.5.sp else 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (isOverlay) 8.dp else 10.dp))

            // SELECTOR DE 3 SERVIDORES: Servidor Chino (API Tencent) | Global (Meta Live) | América (NA) (Local Cache)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (isOverlay) 4.dp else 8.dp)
            ) {
                val regionItems = listOf(
                    Triple("CN", "🇨🇳 " + tr("Servidor Chino"), "API Tencent"),
                    Triple("Global", "🌍 " + tr("Global"), "Meta Live"),
                    Triple("NA", "🌎 " + tr("América (NA)"), "Local Cache")
                )
                regionItems.forEach { (regionId, label, sub) ->
                    val isSelected = (regionId == "CN" && currentRegion == "CN") ||
                                     (regionId == "NA" && currentRegion == "NA") ||
                                     (regionId == "Global" && (currentRegion == "Global" || currentRegion == "BestBuildWR"))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) Brush.verticalGradient(
                                    listOf(HextechGold.copy(alpha = 0.22f), HextechGold.copy(alpha = 0.08f))
                                ) else Brush.verticalGradient(
                                    listOf(HextechSurfaceVariant.copy(alpha = 0.35f), HextechSurfaceVariant.copy(alpha = 0.2f))
                                )
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 0.8.dp,
                                color = if (isSelected) HextechGold else HextechCardBorder.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                ChineseMetaSyncService.setRegion(context, regionId, coroutineScope)
                            }
                            .padding(horizontal = 4.dp, vertical = if (isOverlay) 6.dp else 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) HextechGold else TextMuted,
                                fontSize = if (isOverlay) 8.5.sp else 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 1
                            )
                            Text(
                                text = sub,
                                color = if (isSelected) HextechCyan else TextMuted.copy(alpha = 0.7f),
                                fontSize = if (isOverlay) 7.5.sp else 8.5.sp,
                                fontWeight = FontWeight.Normal,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // RANGO DE ELO PARA SERVIDOR CHINO: Retador/Soberano | Maestro/Gran Maestro | Esmeralda/Diamante | General
            AnimatedVisibility(visible = currentRegion == "CN") {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TencentRankTier.entries.forEach { tier ->
                            val isSelected = currentTier == tier
                            val rankColor = when (tier) {
                                TencentRankTier.CHALLENGER -> Color(0xFFFFD700)
                                TencentRankTier.MASTER_PLUS -> Color(0xFF00E5FF)
                                TencentRankTier.DIAMOND_PLUS -> Color(0xFF3B82F6)
                                TencentRankTier.ALL_RANKS -> Color(0xFF10B981)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) Brush.verticalGradient(
                                            listOf(HextechCyan.copy(alpha = 0.30f), HextechSurfaceVariant.copy(alpha = 0.6f))
                                        ) else Brush.verticalGradient(
                                            listOf(HextechSurfaceVariant.copy(alpha = 0.25f), HextechSurfaceVariant.copy(alpha = 0.15f))
                                        )
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.6.dp,
                                        color = if (isSelected) HextechCyan else HextechCardBorder.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        coroutineScope.launch {
                                            ChineseMetaSyncService.syncChineseMeta(context, tier, forceRefresh = true)
                                        }
                                    }
                                    .padding(horizontal = 2.dp, vertical = if (isOverlay) 5.dp else 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tr(tier.displayName),
                                    color = if (isSelected) HextechCyan else TextMuted,
                                    fontSize = if (isOverlay) 7.5.sp else 8.5.sp,
                                    lineHeight = 10.5.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Barra de Estado de Conexión, Hora de Captura y Caché Persistente
            Surface(
                color = if (isOnline) Color(0xFF00E5FF).copy(alpha = 0.08f) else Color(0xFFE65100).copy(alpha = 0.12f),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(0.5.dp, if (isOnline) HextechCyan.copy(alpha = 0.4f) else Color(0xFFFF9800).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(6.5.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) Color(0xFF00FF7F) else Color(0xFFFF5252))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOnline) {
                                if (isSyncing) "⏳ " + tr("Sincronizando...")
                                else "⚡ " + tr("Actualizado:") + " $lastSyncFormattedTime"
                            } else {
                                "⚠️ " + tr("Sin conexión • Última estadística:") + " $lastSyncFormattedTime"
                            },
                            color = if (isOnline) (if (isSyncing) HextechCyan else Color(0xFF81C784)) else Color(0xFFFFB74D),
                            fontSize = if (isOverlay) 8.sp else 9.5.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isOnline) HextechGold.copy(alpha = 0.15f) else Color(0xFFFF5252).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isOnline) tr("Auto-Sync 24/7") else tr("Caché Local"),
                            color = if (isOnline) HextechGold else Color(0xFFFF8A80),
                            fontSize = if (isOverlay) 7.5.sp else 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp)
                        )
                    }
                }
            }
        }
    }
}
