package com.example.ui.screens

import com.example.ui.components.CoachTab as Tab
import com.example.ui.components.CoachIconButton as IconButton
import androidx.activity.compose.BackHandler
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import android.widget.Toast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WildRiftRepository
import com.example.model.Champion
import com.example.util.SubscriptionManager
import com.example.model.DraftSlot
import com.example.model.LaneRole
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.HextechSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class MetaScreenMode {
    DRAFTING,
    TIER_LIST,
    CATALOG
}

private data class MetaNavTabItem(val title: String, val count: Int? = null)

// Gestor de sesión persistente y estable para el Draft (sin rotaciones aleatorias involuntarias)
object DraftSessionManager {
    private var isInitialized = false
    val allySlots = mutableStateListOf<DraftSlot>()
    val enemySlots = mutableStateListOf<DraftSlot>()

    fun initDefaults() {
        if (isInitialized && (allySlots.isNotEmpty() || enemySlots.isNotEmpty())) return

        if (allySlots.isEmpty()) {
            val roles = listOf(LaneRole.TOP, LaneRole.JUNGLE, LaneRole.MID, LaneRole.ADC, LaneRole.SUPPORT)
            roles.forEach { role ->
                allySlots.add(DraftSlot(champion = WildRiftRepository.EMPTY_CHAMPION, assignedRole = role))
            }
        }

        if (enemySlots.isEmpty()) {
            val roles = listOf(LaneRole.TOP, LaneRole.JUNGLE, LaneRole.MID, LaneRole.ADC, LaneRole.SUPPORT)
            roles.forEach { role ->
                enemySlots.add(DraftSlot(champion = WildRiftRepository.EMPTY_CHAMPION, assignedRole = role))
            }
        }
        isInitialized = true
    }

    fun clearAll() {
        allySlots.clear()
        enemySlots.clear()
        isInitialized = false
        initDefaults()
    }
}

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
    val lang = com.example.util.currentAppLanguage()
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
        DraftSessionManager.initDefaults()
    }

    // Draft State estable que se mantiene fijo sin rotar aleatoriamente
    val allySlots = DraftSessionManager.allySlots
    val enemySlots = DraftSessionManager.enemySlots

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
    val myChampion = if (activeRole != null) allySlots.find { it.assignedRole == activeRole }?.champion?.takeUnless { it.id == "empty" } else null
    val roleIndex = when (activeRole) {
        LaneRole.TOP -> 0
        LaneRole.JUNGLE -> 1
        LaneRole.MID -> 2
        LaneRole.ADC -> 3
        LaneRole.SUPPORT -> 4
        null -> 0
    }
    val enemyLaneOpponent = if (activeRole != null) enemySlots.find { it.assignedRole == activeRole }?.champion ?: enemySlots.getOrNull(roleIndex)?.champion else null
    val selectedLaneOpponent = enemyLaneOpponent?.takeUnless { it.id == "empty" }

    val analysis = remember(activeRole, isFirstPick, allySlots.toList(), enemySlots.toList(), lang) {
        WildRiftRepository.analyzeDraft(
            myRole = activeRole,
            allies = allySlots.map { it.champion },
            enemies = enemySlots.map { it.champion },
            enemyLaneOpponent = selectedLaneOpponent,
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
                        enemyLaneOpponent = selectedLaneOpponent,
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
                            android.widget.Toast.makeText(screenContext, com.example.util.appTr("Equipos vaciados"), android.widget.Toast.LENGTH_SHORT).show()
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
                                            text = com.example.util.tr(tabItem.title),
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
                                                    text = com.example.util.tr(tabItem.count.toString()),
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
                            text = com.example.util.tr(topBarTitle),
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
                        enemyLaneOpponent = selectedLaneOpponent,
                        onToggleFirstPick = { isFirstPick = !isFirstPick },
                        onChangeRole = { showRoleChangeDialog = true },
                        onPickAllyRole = { role ->
                            if (activeRole == null) showRoleChangeDialog = true
                            else { suggestedPickingRole = role; pickingForTeam = "ALLY" }
                        },
                        onPickEnemyRole = { role ->
                            if (activeRole == null) showRoleChangeDialog = true
                            else { suggestedPickingRole = role; pickingForTeam = "ENEMY" }
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
                            if (activeRole == null) { showRoleChangeDialog = true; return@DraftAnalysisTab }
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val targetRole = activeRole!!
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
                        onRandomFill = {
                            if (com.example.model.RolePanelAccess.isAdministrator(SubscriptionManager.userRole.value,
                                    com.example.util.AuthManager.isAdminClaim.value)) {
                                if (activeRole == null) showRoleChangeDialog = true
                                else runCatching { com.example.data.RandomDraftPolicy.generate(WildRiftRepository.champions.toList()) }
                                    .onSuccess { draft ->
                                        allySlots.clear(); allySlots.addAll(draft.allies)
                                        enemySlots.clear(); enemySlots.addAll(draft.enemies)
                                    }.onFailure {
                                        android.widget.Toast.makeText(screenContext, com.example.util.appTr("No se pudo formar un equipo con combos. Vuelve a intentarlo."), android.widget.Toast.LENGTH_SHORT).show()
                                    }
                            }
                        },
                        onClearAll = {
                            allySlots.clear()
                            enemySlots.clear()
                            android.widget.Toast.makeText(screenContext, com.example.util.appTr("Equipos vaciados"), android.widget.Toast.LENGTH_SHORT).show()
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
                                            text = com.example.util.tr(tabItem.title),
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
                                                    text = com.example.util.tr("${tabItem.count}"),
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
                                            text = com.example.util.tr(tabItem.title),
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
                                                    text = com.example.util.tr("${tabItem.count}"),
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
            onDismiss = { selectedDetailChampion = null },
            onChampionSelected = { selectedDetailChampion = it }
        )
    }

    // Removed build creator dialog from catalog

    // Modal Champion Picker for Draft
    if (pickingForTeam != null && (isOverlay || activeRole != null)) {
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
                            allySlots.add(insertPos.coerceAtMost(allySlots.size), DraftSlot(champ, selfRole))
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
                            allySlots.add(insertPos.coerceAtMost(allySlots.size), DraftSlot(champ, targetRole))
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
                            enemySlots.add(insertPos.coerceAtMost(enemySlots.size), DraftSlot(champ, targetRole))
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
