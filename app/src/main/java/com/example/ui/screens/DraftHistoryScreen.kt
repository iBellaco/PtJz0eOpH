package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AccountProfile
import com.example.data.AccountProfileManager
import com.example.data.WildRiftRepository
import com.example.data.backup.BackupRestoreManager
import com.example.data.local.entity.SavedDraftEntity
import com.example.data.repository.DraftHistoryRepository
import com.example.model.DraftSlot
import com.example.model.LaneRole
import com.example.ui.components.ChampionAvatar
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedSurface
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.HextechSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.LocalLanguage
import com.example.util.tr
import com.example.util.trStr
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun Context.isOverlayOrNonActivity(): Boolean {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return false
        ctx = ctx.baseContext
    }
    return true
}

@Composable
private fun AdaptiveHistoryDialog(
    isOverlay: Boolean,
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null
) {
    val context = LocalContext.current
    val effectiveOverlay = isOverlay || context.isOverlayOrNonActivity()

    if (effectiveOverlay) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.85f))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onDismissRequest
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                border = BorderStroke(1.5.dp, HextechGold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    title()
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        text()
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        dismissButton?.invoke()
                        if (dismissButton != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        confirmButton()
                    }
                }
            }
        }
    } else {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = title,
            text = text,
            confirmButton = confirmButton,
            dismissButton = dismissButton,
            containerColor = HextechSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DraftHistoryScreen(
    isOverlay: Boolean = false,
    onNavigateBack: () -> Unit,
    onLoadDraft: (allies: List<DraftSlot>, enemies: List<DraftSlot>, role: LaneRole, isFirstPick: Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    androidx.compose.runtime.LaunchedEffect(Unit) {
        AccountProfileManager.init(context)
    }
    val draftsFlow = remember(context) { DraftHistoryRepository.getAllDrafts(context) }
    val draftsList by draftsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    val profiles by AccountProfileManager.allProfiles.collectAsStateWithLifecycle()
    val activeProfileId by AccountProfileManager.activeProfileId.collectAsStateWithLifecycle()
    var selectedProfileIdFilter by remember { mutableStateOf<String?>("ALL") } // "ALL" or profile.id

    var showCreateProfileDialog by remember { mutableStateOf(false) }
    var profileToEdit by remember { mutableStateOf<AccountProfile?>(null) }
    var showBlueEssenceStore by remember { mutableStateOf<String?>(null) }

    var currentHistoryTab by remember { mutableStateOf("DRAFTS") } // "DRAFTS" or "TIER_LIST"
    var searchQuery by remember { mutableStateOf("") }
    var selectedResultFilter by remember { mutableStateOf<String?>(null) } // null = ALL, "VICTORY", "DEFEAT"
    var selectedQueueFilter by remember { mutableStateOf<String?>(null) } // null = ALL, "NORMAL", "LEGENDARY"
    var selectedRoleFilter by remember { mutableStateOf<LaneRole?>(null) }
    var selectedDraftForDetail by remember { mutableStateOf<SavedDraftEntity?>(null) }
    var draftToDelete by remember { mutableStateOf<SavedDraftEntity?>(null) }
    var showClearAllConfirm by remember { mutableStateOf(false) }
    var profileToClearHistory by remember { mutableStateOf<AccountProfile?>(null) }
    var profileToDeleteProfile by remember { mutableStateOf<AccountProfile?>(null) }

    val currentLang = LocalLanguage.current
    val effectiveLang = if (currentLang == "auto") "es" else currentLang

    // Backup & Restore state
    var showBackupRestoreDialog by remember { mutableStateOf(false) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }
    var pendingImportJson by remember { mutableStateOf<String?>(null) }
    var importMergeMode by remember { mutableStateOf(true) }
    var isProcessingBackup by remember { mutableStateOf(false) }

    val registryOwner = androidx.activity.compose.LocalActivityResultRegistryOwner.current
    val canUseLaunchers = !isOverlay && registryOwner != null

    // File Save Launcher (Export JSON)
    val exportFileLauncher = if (canUseLaunchers) {
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/json")
        ) { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    isProcessingBackup = true
                    try {
                        val jsonContent = BackupRestoreManager.generateBackupJson(context)
                        val success = BackupRestoreManager.writeTextToUri(context, uri, jsonContent)
                        if (success) {
                            Toast.makeText(context, trStr(effectiveLang, "Copia de seguridad exportada con éxito"), Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, trStr(effectiveLang, "Error al guardar el archivo"), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isProcessingBackup = false
                    }
                }
            }
        }
    } else null

    // File Open Launcher (Import JSON)
    val importFileLauncher = if (canUseLaunchers) {
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                coroutineScope.launch {
                    isProcessingBackup = true
                    try {
                        val fileText = BackupRestoreManager.readTextFromUri(context, uri)
                        pendingImportJson = fileText
                        showImportConfirmDialog = true
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error al leer archivo: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isProcessingBackup = false
                    }
                }
            }
        }
    } else null

    // Drafts scoped to selected account profile
    val currentScopeDrafts = remember(draftsList, selectedProfileIdFilter) {
        if (selectedProfileIdFilter == null || selectedProfileIdFilter == "ALL") {
            draftsList
        } else {
            draftsList.filter { it.accountProfileId == selectedProfileIdFilter }
        }
    }

    val filteredDrafts = remember(currentScopeDrafts, searchQuery, selectedResultFilter, selectedRoleFilter) {
        currentScopeDrafts.filter { draft ->
            val matchesQuery = searchQuery.isBlank() ||
                    draft.title.contains(searchQuery, ignoreCase = true) ||
                    draft.myChampionName.contains(searchQuery, ignoreCase = true) ||
                    draft.enemyLaneOpponentName.contains(searchQuery, ignoreCase = true) ||
                    draft.notes.contains(searchQuery, ignoreCase = true) ||
                    draft.accountProfileName.contains(searchQuery, ignoreCase = true)

            val matchesResult = when (selectedResultFilter) {
                null -> true
                "PENDING" -> !draft.matchResult.equals("VICTORY", ignoreCase = true) && !draft.matchResult.equals("DEFEAT", ignoreCase = true)
                "VICTORY" -> draft.matchResult.equals("VICTORY", ignoreCase = true)
                "DEFEAT" -> draft.matchResult.equals("DEFEAT", ignoreCase = true)
                else -> draft.matchResult.equals(selectedResultFilter, ignoreCase = true)
            }

            val matchesQueue = when (selectedQueueFilter) {
                null -> true
                "LEGENDARY" -> draft.isLegendary
                "NORMAL" -> !draft.isLegendary
                else -> true
            }

            val matchesRole = selectedRoleFilter == null || draft.userRole.equals(selectedRoleFilter?.name, ignoreCase = true)

            matchesQuery && matchesResult && matchesQueue && matchesRole
        }
    }

    val totalCount = currentScopeDrafts.size
    val pendingCount = currentScopeDrafts.count { !it.matchResult.equals("VICTORY", ignoreCase = true) && !it.matchResult.equals("DEFEAT", ignoreCase = true) }
    val victoriesCount = currentScopeDrafts.count { it.matchResult.equals("VICTORY", ignoreCase = true) }
    val defeatsCount = currentScopeDrafts.count { it.matchResult.equals("DEFEAT", ignoreCase = true) }
    val totalFinished = victoriesCount + defeatsCount
    val winRate = if (totalFinished > 0) (victoriesCount.toDouble() / totalFinished * 100).toInt() else 0

    val activeSelectedProfile = profiles.find { it.id == selectedProfileIdFilter }
    val effectiveOverlay = isOverlay || context.isOverlayOrNonActivity()
    var isNavMinimized by rememberSaveable { mutableStateOf(effectiveOverlay) }

    androidx.activity.compose.BackHandler {
        if (selectedDraftForDetail != null) {
            selectedDraftForDetail = null
        } else if (draftToDelete != null) {
            draftToDelete = null
        } else if (showClearAllConfirm) {
            showClearAllConfirm = false
        } else if (profileToClearHistory != null) {
            profileToClearHistory = null
        } else if (profileToDeleteProfile != null) {
            profileToDeleteProfile = null
        } else if (showCreateProfileDialog) {
            showCreateProfileDialog = false
        } else if (profileToEdit != null) {
            profileToEdit = null
        } else if (showBlueEssenceStore != null) {
            showBlueEssenceStore = null
        } else if (showBackupRestoreDialog) {
            showBackupRestoreDialog = false
        } else if (showImportConfirmDialog) {
            showImportConfirmDialog = false
        } else {
            onNavigateBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                if (effectiveOverlay) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.size(28.dp).testTag("history_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = tr("Volver"),
                                tint = HextechGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = tr("Historial & Perfiles"),
                                color = HextechGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("draft_history_title")
                            )
                            Text(
                                text = "${draftsList.size} " + tr("partidas guardadas"),
                                color = HextechCyan,
                                fontSize = 9.5.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Botón para minimizar/expandir barra de navegación y perfiles
                        IconButton(
                            onClick = { isNavMinimized = !isNavMinimized },
                            modifier = Modifier.size(28.dp).testTag("history_toggle_nav_button")
                        ) {
                            Icon(
                                imageVector = if (isNavMinimized) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                contentDescription = if (isNavMinimized) tr("Expandir barra de navegación") else tr("Minimizar barra de navegación"),
                                tint = HextechGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { showBackupRestoreDialog = true },
                            modifier = Modifier.size(28.dp).testTag("history_backup_restore_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = tr("Copia de Seguridad / Restaurar"),
                                tint = HextechCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (draftsList.isNotEmpty()) {
                            IconButton(
                                onClick = { showClearAllConfirm = true },
                                modifier = Modifier.size(28.dp).testTag("history_clear_all_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = tr("Limpiar Historial"),
                                    tint = DangerRed.copy(alpha = 0.85f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = tr("Historial de Drafts"),
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("draft_history_title")
                            )
                            Text(
                                text = "${draftsList.size} " + tr("partidas guardadas"),
                                color = HextechCyan,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("history_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = tr("Volver"),
                                tint = HextechGold
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { isNavMinimized = !isNavMinimized },
                            modifier = Modifier.testTag("history_toggle_nav_button")
                        ) {
                            Icon(
                                imageVector = if (isNavMinimized) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                contentDescription = if (isNavMinimized) tr("Expandir barra") else tr("Minimizar barra"),
                                tint = HextechGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = { showBackupRestoreDialog = true },
                            modifier = Modifier.testTag("history_backup_restore_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = tr("Copia de Seguridad / Restaurar"),
                                tint = HextechCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        if (draftsList.isNotEmpty()) {
                            IconButton(
                                onClick = { showClearAllConfirm = true },
                                modifier = Modifier.testTag("history_clear_all_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = tr("Limpiar Historial"),
                                    tint = DangerRed.copy(alpha = 0.85f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = if (isOverlay) 4.dp else 16.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            if (isNavMinimized) {
                // Barra de navegación compacta y minimizada
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(HextechSurface)
                        .border(1.dp, HextechGold.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Selector rápido de perfil desplegable
                    var showProfileDropdown by remember { mutableStateOf(false) }
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(HextechDarkBg.copy(alpha = 0.6f))
                                .clickable { showProfileDropdown = true }
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = HextechGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = activeSelectedProfile?.name ?: tr("Todas"),
                                color = HextechGold,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = HextechGold,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showProfileDropdown,
                            onDismissRequest = { showProfileDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(tr("🌐 Todas las cuentas")) },
                                onClick = {
                                    selectedProfileIdFilter = "ALL"
                                    showProfileDropdown = false
                                }
                            )
                            profiles.forEach { prof ->
                                DropdownMenuItem(
                                    text = { Text("👤 ${prof.name}${if (prof.tag.isNotBlank()) " #${prof.tag}" else ""}") },
                                    onClick = {
                                        selectedProfileIdFilter = prof.id
                                        AccountProfileManager.setActiveProfile(context, prof.id)
                                        showProfileDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Selector de Pestaña Compacto
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(HextechDarkBg.copy(alpha = 0.6f))
                            .padding(1.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(if (currentHistoryTab == "DRAFTS") HextechGold else Color.Transparent)
                                .clickable { currentHistoryTab = "DRAFTS" }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = tr("Partidas"),
                                color = if (currentHistoryTab == "DRAFTS") HextechDarkBg else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(if (currentHistoryTab == "TIER_LIST") HextechGold else Color.Transparent)
                                .clickable { currentHistoryTab = "TIER_LIST" }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = tr("Tier List"),
                                color = if (currentHistoryTab == "TIER_LIST") HextechDarkBg else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Botón para expandir la barra completa
                    IconButton(
                        onClick = { isNavMinimized = false },
                        modifier = Modifier.size(26.dp).testTag("history_expand_nav_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = tr("Expandir barra"),
                            tint = HextechCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                // Account Profiles Selector Bar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                    border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = HextechGold,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = tr("Cuentas / Perfiles"),
                                    color = HextechGold,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(
                                    onClick = { showBackupRestoreDialog = true },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                ) {
                                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = HextechGold, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(tr("JSON Backup"), color = HextechGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                TextButton(
                                    onClick = { showCreateProfileDialog = true },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(tr("Crear Perfil"), color = HextechCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(2.dp))
                                IconButton(
                                    onClick = { isNavMinimized = true },
                                    modifier = Modifier.size(24.dp).testTag("history_collapse_nav_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ExpandLess,
                                        contentDescription = tr("Minimizar barra"),
                                        tint = HextechGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Indicador de deslizamiento para perfiles
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tr("Cuentas Activas"),
                                color = HextechCyan,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(HextechCyan.copy(alpha = 0.12f))
                                    .padding(horizontal = 5.dp, vertical = 1.5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "↔ " + tr("Desliza para ver más perfiles"),
                                    color = HextechCyan,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        // Horizontal list of profile chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isAllSelected = selectedProfileIdFilter == "ALL" || selectedProfileIdFilter == null
                            FilterChip(
                                selected = isAllSelected,
                                onClick = { selectedProfileIdFilter = "ALL" },
                                label = {
                                    Text(tr("🌐 Todas (${draftsList.size})"), fontSize = 10.5.sp)
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HextechCyan,
                                    selectedLabelColor = HextechDarkBg
                                )
                            )

                            profiles.forEach { prof ->
                                val isSelected = selectedProfileIdFilter == prof.id
                                val countForProf = draftsList.count { it.accountProfileId == prof.id }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedProfileIdFilter = prof.id
                                        AccountProfileManager.setActiveProfile(context, prof.id)
                                    },
                                    label = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("👤 ${prof.name}", fontSize = 10.5.sp)
                                            if (prof.tag.isNotBlank()) {
                                                Text(" #${prof.tag}", fontSize = 9.sp, color = if (isSelected) HextechDarkBg else HextechCyan)
                                            }
                                            Text(" ($countForProf)", fontSize = 9.5.sp)
                                        }
                                    },
                                    trailingIcon = {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = tr("Editar"),
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clickable { profileToEdit = prof },
                                                tint = HextechDarkBg
                                            )
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = HextechGold,
                                        selectedLabelColor = HextechDarkBg
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Main History / Tier List Tab Bar Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(HextechSurface)
                        .border(1.dp, HextechGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (currentHistoryTab == "DRAFTS") HextechGold else Color.Transparent)
                            .clickable { currentHistoryTab = "DRAFTS" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = if (currentHistoryTab == "DRAFTS") HextechDarkBg else HextechGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tr("Partidas Guardadas"),
                                color = if (currentHistoryTab == "DRAFTS") HextechDarkBg else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (currentHistoryTab == "TIER_LIST") HextechGold else Color.Transparent)
                            .clickable { currentHistoryTab = "TIER_LIST" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Leaderboard,
                                contentDescription = null,
                                tint = if (currentHistoryTab == "TIER_LIST") HextechDarkBg else HextechGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (activeSelectedProfile != null) "${tr("Tier List")} (${activeSelectedProfile.name})" else tr("Mi Tier List Personal"),
                                color = if (currentHistoryTab == "TIER_LIST") HextechDarkBg else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (currentHistoryTab == "TIER_LIST") {
                    PersonalTierListView(
                        draftsList = currentScopeDrafts,
                        onSelectDraftForDetail = { selectedDraftForDetail = it },
                        isOverlay = effectiveOverlay
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Vista de Partidas Guardadas
                        // Stats Summary Card
                        if (draftsList.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechSurface),
                        border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = tr("Rendimiento en Partidas"),
                                    color = HextechGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(" $victoriesCount " + tr("Vic."), color = Color(0xFF81C784), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    Text(" $defeatsCount " + tr("Derr."), color = DangerRed, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (totalFinished > 0) {
                                Surface(
                                    color = if (winRate >= 50) Color(0xFF81C784).copy(alpha = 0.15f) else DangerRed.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (winRate >= 50) Color(0xFF81C784) else DangerRed)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "$winRate%",
                                            color = if (winRate >= 50) Color(0xFF81C784) else DangerRed,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = tr("Winrate"),
                                            color = TextMuted,
                                            fontSize = 9.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Box
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_search_input"),
                        placeholder = { Text(tr("Buscar por campeón, rival o nota..."), color = TextMuted, fontSize = 12.5.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = tr("Limpiar"), tint = TextMuted, modifier = Modifier.size(16.dp))
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

                    // Queue Filters
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = selectedQueueFilter == null,
                            onClick = { selectedQueueFilter = null },
                            label = { Text(tr("Todas las Colas"), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HextechCyan,
                                selectedLabelColor = HextechDarkBg
                            )
                        )
                        FilterChip(
                            selected = selectedQueueFilter == "NORMAL",
                            onClick = { selectedQueueFilter = if (selectedQueueFilter == "NORMAL") null else "NORMAL" },
                            label = { Text("⚔️ " + tr("Partidas Normales"), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HextechGold,
                                selectedLabelColor = HextechDarkBg
                            )
                        )
                        FilterChip(
                            selected = selectedQueueFilter == "LEGENDARY",
                            onClick = { selectedQueueFilter = if (selectedQueueFilter == "LEGENDARY") null else "LEGENDARY" },
                            label = { Text("🏆 " + tr("Legendarias"), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE040FB),
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Result Filters
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = selectedResultFilter == null,
                            onClick = { selectedResultFilter = null },
                            label = { Text(tr("Todos") + " ($totalCount)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HextechCyan,
                                selectedLabelColor = HextechDarkBg
                            )
                        )
                        FilterChip(
                            selected = selectedResultFilter == "PENDING",
                            onClick = { selectedResultFilter = if (selectedResultFilter == "PENDING") null else "PENDING" },
                            label = { Text("⏳ " + tr("En espera") + " ($pendingCount)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HextechGold,
                                selectedLabelColor = HextechDarkBg
                            )
                        )
                        FilterChip(
                            selected = selectedResultFilter == "VICTORY",
                            onClick = { selectedResultFilter = if (selectedResultFilter == "VICTORY") null else "VICTORY" },
                            label = { Text("👑 " + tr("Victorias") + " ($victoriesCount)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF81C784),
                                selectedLabelColor = Color.Black
                            )
                        )
                        FilterChip(
                            selected = selectedResultFilter == "DEFEAT",
                            onClick = { selectedResultFilter = if (selectedResultFilter == "DEFEAT") null else "DEFEAT" },
                            label = { Text("💔 " + tr("Derrotas") + " ($defeatsCount)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DangerRed,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    
                    if (pendingCount > 0) {
                        val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "pulse")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1f,
                            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                                animation = androidx.compose.animation.core.tween(1000),
                                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                            ),
                            label = "pulse_alpha"
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = HextechGold.copy(alpha = 0.1f)),
                            border = BorderStroke(1.dp, HextechGold.copy(alpha = alpha))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = HextechGold, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tr("Aún hay draft en espera por registrar resultado."),
                                    color = HextechGold,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                if (filteredDrafts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = HextechSurface,
                                border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f)),
                                modifier = Modifier.size(100.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = HextechGold,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (draftsList.isEmpty()) tr("Tu historial está limpio.") else tr("No se encontraron partidas con ese filtro"),
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (draftsList.isEmpty())
                                    tr("Ve al Asistente de Draft, crea tu primera composición y guárdala para analizarla después.")
                                else
                                    tr("Intenta cambiar el término de búsqueda o restablecer los filtros de resultado y rol."),
                                color = TextMuted,
                                fontSize = 12.5.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 17.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredDrafts, key = { it.id }) { draft ->
                            SavedDraftCard(
                                draft = draft,
                                isOverlay = effectiveOverlay,
                                onClick = { selectedDraftForDetail = draft },
                                onLoad = {
                                    val allies = DraftHistoryRepository.parseDraftSlots(draft.allyPicksJson)
                                    val enemies = DraftHistoryRepository.parseDraftSlots(draft.enemyPicksJson)
                                    val role = try { LaneRole.valueOf(draft.userRole) } catch (_: Exception) { LaneRole.MID }
                                    onLoadDraft(allies, enemies, role, draft.isFirstPick)
                                },
                                onUpdateResult = { newResult ->
                                    coroutineScope.launch {
                                        DraftHistoryRepository.updateMatchResult(context, draft.id, newResult)
                                    }
                                },
                                onDelete = { draftToDelete = draft }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }
                }
                    } // Cierra el Column
            }
        }
    }
    }

    // Detail Bottom Sheet
    if (selectedDraftForDetail != null) {
        DraftDetailBottomSheet(
            isOverlay = effectiveOverlay,
            draft = selectedDraftForDetail!!,
            profiles = profiles,
            onDismiss = { selectedDraftForDetail = null },
            onLoad = {
                val draft = selectedDraftForDetail!!
                val allies = DraftHistoryRepository.parseDraftSlots(draft.allyPicksJson)
                val enemies = DraftHistoryRepository.parseDraftSlots(draft.enemyPicksJson)
                val role = try { LaneRole.valueOf(draft.userRole) } catch (_: Exception) { LaneRole.MID }
                selectedDraftForDetail = null
                onLoadDraft(allies, enemies, role, draft.isFirstPick)
            },
            onSaveNotes = { newNotes ->
                coroutineScope.launch {
                    DraftHistoryRepository.updateNotes(context, selectedDraftForDetail!!.id, newNotes)
                    selectedDraftForDetail = selectedDraftForDetail?.copy(notes = newNotes)
                }
            },
            onAssignProfile = { newProfileId, newProfileName ->
                coroutineScope.launch {
                    DraftHistoryRepository.updateAccountProfile(context, selectedDraftForDetail!!.id, newProfileId, newProfileName)
                    selectedDraftForDetail = selectedDraftForDetail?.copy(accountProfileId = newProfileId, accountProfileName = newProfileName)
                }
            }
        )
    }

    // Create Profile Dialog
    if (showCreateProfileDialog) {
        var newNick by remember { mutableStateOf("") }
        var newTag by remember { mutableStateOf("") }

        AdaptiveHistoryDialog(
            isOverlay = effectiveOverlay,
            onDismissRequest = { showCreateProfileDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = HextechGold, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(tr("Crear Perfil"), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = tr("Crea un perfil para registrar historiales y Tier Lists de forma 100% independiente (ej: Smurf, Dúo, etc)."),
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newNick,
                        onValueChange = { newNick = it },
                        label = { Text(tr("Nick de la Cuenta"), fontSize = 12.sp) },
                        placeholder = { Text("Ej: FakerWR, SmurfSoloQ", fontSize = 11.5.sp, color = TextMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HextechGold,
                            unfocusedBorderColor = HextechCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newTag,
                        onValueChange = { newTag = it },
                        label = { Text(tr("Tag / Rango Opcional"), fontSize = 12.sp) },
                        placeholder = { Text("Ej: Soberano, LAN, Smurf", fontSize = 11.5.sp, color = TextMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HextechCyan,
                            unfocusedBorderColor = HextechCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newNick.isNotBlank()) {
                            val created = AccountProfileManager.createProfile(context, newNick.trim(), newTag.trim())
                            selectedProfileIdFilter = created.id
                            showCreateProfileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold, contentColor = HextechDarkBg),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(tr("Crear Perfil"), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateProfileDialog = false }) {
                    Text(tr("Cancelar"), color = TextMuted)
                }
            }
        )
    }

    // Edit Profile Dialog
    if (profileToEdit != null) {
        val prof = profileToEdit!!
        var editNick by remember { mutableStateOf(prof.name) }
        var editTag by remember { mutableStateOf(prof.tag) }

        AdaptiveHistoryDialog(
            isOverlay = effectiveOverlay,
            onDismissRequest = { profileToEdit = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = HextechGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(tr("Editar Perfil de Cuenta"), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = editNick,
                        onValueChange = { editNick = it },
                        label = { Text(tr("Nick de la Cuenta"), fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HextechGold,
                            unfocusedBorderColor = HextechCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editTag,
                        onValueChange = { editTag = it },
                        label = { Text(tr("Tag / Rango Opcional"), fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HextechCyan,
                            unfocusedBorderColor = HextechCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(HextechSurfaceVariant)
                            .clickable {
                                val isAdmin = com.example.util.AuthManager.isCurrentUserAdmin()
                                if (isAdmin) {
                                    showBlueEssenceStore = prof.id
                                } else {
                                    android.widget.Toast.makeText(context, "Servicio temporalmente fuera de servicio", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_blue_essence),
                                contentDescription = "Esencia Azul",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Esencias: ${prof.blueEssence}",
                                color = HextechCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Text("Tienda", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    if (profiles.size > 1 && prof.id != "default") {
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        OutlinedButton(
                            onClick = {
                                profileToClearHistory = prof
                                profileToEdit = null
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = HextechGold),
                            border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(tr("Vaciar Historial del Perfil"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                profileToDeleteProfile = prof
                                profileToEdit = null
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                            border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(tr("Eliminar Perfil e Historial"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else if (prof.id == "default") {
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = {
                                profileToClearHistory = prof
                                profileToEdit = null
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                            border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(tr("Vaciar Historial del Perfil"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editNick.isNotBlank()) {
                            AccountProfileManager.updateProfile(context, prof.id, editNick.trim(), editTag.trim())
                            profileToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold, contentColor = HextechDarkBg),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(tr("Guardar"), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToEdit = null }) {
                    Text(tr("Cancelar"), color = TextMuted)
                }
            }
        )
    }

    if (showBlueEssenceStore != null) {
        com.example.ui.components.BlueEssenceStoreDialog(
            profileId = showBlueEssenceStore!!,
            onDismiss = { showBlueEssenceStore = null }
        )
    }

    // Confirm Delete Dialog
    if (draftToDelete != null) {
        AdaptiveHistoryDialog(
            isOverlay = effectiveOverlay,
            onDismissRequest = { draftToDelete = null },
            title = { Text(tr("Eliminar partida"), color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text(tr("¿Deseas eliminar este registro del historial? Esta acción no se puede deshacer."), color = TextSecondary, fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        val id = draftToDelete!!.id
                        draftToDelete = null
                        coroutineScope.launch {
                            DraftHistoryRepository.deleteDraft(context, id)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text(tr("Eliminar"), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { draftToDelete = null }) {
                    Text(tr("Cancelar"), color = TextMuted)
                }
            }
        )
    }

    // Confirm Clear Profile History Dialog
    if (profileToClearHistory != null) {
        val targetProf = profileToClearHistory!!
        AdaptiveHistoryDialog(
            isOverlay = effectiveOverlay,
            onDismissRequest = { profileToClearHistory = null },
            title = { Text(tr("Vaciar Historial del Perfil"), color = DangerRed, fontWeight = FontWeight.Bold) },
            text = { Text(tr("¿Estás seguro de que deseas vaciar todo el historial del perfil '${targetProf.name}'? Esta acción no se puede deshacer."), color = TextSecondary, fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        val profId = targetProf.id
                        profileToClearHistory = null
                        coroutineScope.launch {
                            DraftHistoryRepository.clearDraftsByProfile(context, profId)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text(tr("Vaciar Historial"), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToClearHistory = null }) {
                    Text(tr("Cancelar"), color = TextMuted)
                }
            }
        )
    }

    // Confirm Delete Profile Dialog
    if (profileToDeleteProfile != null) {
        val targetProf = profileToDeleteProfile!!
        AdaptiveHistoryDialog(
            isOverlay = effectiveOverlay,
            onDismissRequest = { profileToDeleteProfile = null },
            title = { Text(tr("Eliminar Perfil e Historial"), color = DangerRed, fontWeight = FontWeight.Bold) },
            text = { Text(tr("¿Estás seguro de que deseas eliminar el perfil '${targetProf.name}' y todo su historial asociado? Esta acción es irreversible y permanente."), color = TextSecondary, fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        val profId = targetProf.id
                        profileToDeleteProfile = null
                        coroutineScope.launch {
                            DraftHistoryRepository.clearDraftsByProfile(context, profId)
                            AccountProfileManager.deleteProfile(context, profId)
                            if (selectedProfileIdFilter == profId) {
                                selectedProfileIdFilter = "ALL"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text(tr("Eliminar Perfil"), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToDeleteProfile = null }) {
                    Text(tr("Cancelar"), color = TextMuted)
                }
            }
        )
    }

    // Confirm Clear All Dialog
    if (showClearAllConfirm) {
        val isAllSelected = selectedProfileIdFilter == "ALL" || selectedProfileIdFilter == null
        val profName = profiles.find { it.id == selectedProfileIdFilter }?.name ?: "esta cuenta"

        AdaptiveHistoryDialog(
            isOverlay = effectiveOverlay,
            onDismissRequest = { showClearAllConfirm = false },
            title = { 
                Text(
                    if (isAllSelected) tr("Borrar todo el historial") else tr("Vaciar historial del perfil"), 
                    color = DangerRed, 
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = { 
                Text(
                    if (isAllSelected) tr("¿Estás seguro de vaciar todas las partidas y composiciones guardadas de todas tus cuentas?")
                    else tr("¿Estás seguro de vaciar todas las partidas guardadas de la cuenta '$profName'?"), 
                    color = TextSecondary, 
                    fontSize = 13.sp
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearAllConfirm = false
                        coroutineScope.launch {
                            if (isAllSelected) {
                                DraftHistoryRepository.clearAllDrafts(context)
                            } else {
                                DraftHistoryRepository.clearDraftsByProfile(context, selectedProfileIdFilter!!)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text(if (isAllSelected) tr("Borrar Todo") else tr("Vaciar Historial"), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) {
                    Text(tr("Cancelar"), color = TextMuted)
                }
            }
        )
    }

    // Backup & Restore Dialog (Export/Import JSON)
    if (showBackupRestoreDialog) {
        AdaptiveHistoryDialog(
            isOverlay = effectiveOverlay,
            onDismissRequest = { showBackupRestoreDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = HextechGold, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(tr("Copia de Seguridad y Restaurar"), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (isOverlay || !canUseLaunchers) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                            colors = CardDefaults.cardColors(containerColor = HextechGold.copy(alpha = 0.12f)),
                            border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = HextechGold, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tr("Para exportar o importar archivos JSON en almacenamiento, abre la app principal. En este Hub puedes crear perfiles y consultar partidas."),
                                    color = HextechGold,
                                    fontSize = 10.5.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = tr("Exporta o importa tus perfiles de cuenta personalizados y todo el historial de drafts que construye tu Tier List Personal en formato JSON."),
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = HextechSurfaceVariant),
                        border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${profiles.size} " + tr("Perfiles registrados"), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Leaderboard, contentDescription = null, tint = HextechGold, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${draftsList.size} " + tr("Partidas e historial para Tier List"), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action 1: Export JSON
                    Button(
                        onClick = {
                            if (canUseLaunchers && exportFileLauncher != null) {
                                val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                exportFileLauncher.launch("WildRift_TierList_Backup_$timestamp.json")
                                showBackupRestoreDialog = false
                            } else {
                                Toast.makeText(context, "Abre la app principal para exportar a archivos del sistema.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = HextechGold, contentColor = HextechDarkBg),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(tr("Exportar JSON (Copia de Seguridad)"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action 2: Import JSON
                    OutlinedButton(
                        onClick = {
                            if (canUseLaunchers && importFileLauncher != null) {
                                importFileLauncher.launch(arrayOf("application/json", "text/*"))
                                showBackupRestoreDialog = false
                            } else {
                                Toast.makeText(context, "Abre la app principal para importar archivos del sistema.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HextechCyan),
                        border = BorderStroke(1.dp, HextechCyan),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(tr("Importar JSON desde Archivo"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBackupRestoreDialog = false }) {
                    Text(tr("Cerrar"), color = TextMuted)
                }
            }
        )
    }

    // Import Confirmation Dialog
    if (showImportConfirmDialog && pendingImportJson != null) {
        AdaptiveHistoryDialog(
            isOverlay = effectiveOverlay,
            onDismissRequest = {
                showImportConfirmDialog = false
                pendingImportJson = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(tr("Restaurar Configuración"), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = tr("¿Cómo deseas aplicar la copia de seguridad importada a este dispositivo?"),
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Mode selection: Merge vs Replace
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(HextechSurfaceVariant)
                            .clickable { importMergeMode = true }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (importMergeMode) HextechCyan else Color.Transparent,
                            border = BorderStroke(1.dp, HextechCyan),
                            modifier = Modifier.size(16.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(tr("Combinar datos (Recomendado)"), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(tr("Mantiene tus perfiles actuales y agrega los nuevos sin duplicados."), color = TextMuted, fontSize = 10.5.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(HextechSurfaceVariant)
                            .clickable { importMergeMode = false }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (!importMergeMode) DangerRed else Color.Transparent,
                            border = BorderStroke(1.dp, DangerRed),
                            modifier = Modifier.size(16.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(tr("Reemplazar todo (Limpiar actual)"), color = DangerRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(tr("Sustituye por completo los perfiles y drafts por los del archivo."), color = TextMuted, fontSize = 10.5.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val jsonToRestore = pendingImportJson
                        showImportConfirmDialog = false
                        pendingImportJson = null
                        if (jsonToRestore != null) {
                            coroutineScope.launch {
                                isProcessingBackup = true
                                val result = BackupRestoreManager.restoreFromJson(
                                    context = context,
                                    rawJson = jsonToRestore,
                                    merge = importMergeMode
                                )
                                isProcessingBackup = false
                                if (result.success) {
                                    Toast.makeText(
                                        context,
                                        "Restaurados ${result.profilesImported} perfiles y ${result.draftsImported} partidas.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (importMergeMode) HextechCyan else DangerRed,
                        contentColor = if (importMergeMode) HextechDarkBg else Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (importMergeMode) tr("Combinar e Importar") else tr("Reemplazar Todo"), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImportConfirmDialog = false
                    pendingImportJson = null
                }) {
                    Text(tr("Cancelar"), color = TextMuted)
                }
            }
        )
    }
    } // End of enclosing Box
}

@Composable
private fun SavedDraftCard(
    draft: SavedDraftEntity,
    isOverlay: Boolean = false,
    onClick: () -> Unit,
    onLoad: () -> Unit,
    onUpdateResult: (String) -> Unit,
    onDelete: () -> Unit
) {
    val allies = remember(draft.allyPicksJson) { DraftHistoryRepository.parseDraftSlots(draft.allyPicksJson) }
    val enemies = remember(draft.enemyPicksJson) { DraftHistoryRepository.parseDraftSlots(draft.enemyPicksJson) }
    val formattedDate = remember(draft.timestamp) {
        val sdf = SimpleDateFormat("dd MMM • HH:mm", Locale.getDefault())
        sdf.format(Date(draft.timestamp))
    }

    var resultMenuExpanded by remember { mutableStateOf(false) }

    val roleObj = try { LaneRole.valueOf(draft.userRole) } catch (_: Exception) { LaneRole.MID }

    val resultBg = when (draft.matchResult.uppercase()) {
        "VICTORY" -> Color(0xFF81C784).copy(alpha = 0.18f)
        "DEFEAT" -> DangerRed.copy(alpha = 0.18f)
        else -> HextechGold.copy(alpha = 0.18f)
    }
    val resultBorder = when (draft.matchResult.uppercase()) {
        "VICTORY" -> Color(0xFF81C784)
        "DEFEAT" -> DangerRed
        else -> HextechGold
    }
    val resultLabel = when (draft.matchResult.uppercase()) {
        "VICTORY" -> "👑 " + tr("Victoria")
        "DEFEAT" -> "💔 " + tr("Derrota")
        else -> "⏳ " + tr("En espera")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("saved_draft_card_${draft.id}"),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        border = BorderStroke(1.dp, HextechCardBorder)
    ) {
        Column(modifier = Modifier.padding(if (isOverlay) 10.dp else 12.dp)) {
            // Header Row: Date & Result Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = HextechGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tr(roleObj.displayName),
                        color = HextechGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (draft.isLegendary) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Surface(
                            color = Color(0xFF9333EA).copy(alpha = 0.25f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.8.dp, Color(0xFFC084FC))
                        ) {
                            Text(
                                text = "🏆 " + tr("Legendaria"),
                                color = Color(0xFFE9D5FF),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    if (draft.accountProfileName.isNotBlank()) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Surface(
                            color = HextechCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.5.dp, HextechCyan.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "👤 ${draft.accountProfileName}",
                                color = HextechCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "• $formattedDate",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                // Interactive Result Badge with dropdown
                Box {
                    Surface(
                        color = resultBg,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, resultBorder),
                        modifier = Modifier.clickable { resultMenuExpanded = true }
                    ) {
                        Text(
                            text = resultLabel,
                            color = resultBorder,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = resultMenuExpanded,
                        onDismissRequest = { resultMenuExpanded = false },
                        modifier = Modifier.background(HextechSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("👑 " + tr("Victoria"), color = Color(0xFF81C784), fontWeight = FontWeight.Bold) },
                            onClick = {
                                onUpdateResult("VICTORY")
                                resultMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("💔 " + tr("Derrota"), color = DangerRed, fontWeight = FontWeight.Bold) },
                            onClick = {
                                onUpdateResult("DEFEAT")
                                resultMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Champion matchup headline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = draft.title,
                    color = TextPrimary,
                    fontSize = if (isOverlay) 12.5.sp else 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${draft.estimatedWinrate}% " + tr("WR Est."),
                    color = HextechCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isOverlay) {
                // Team-by-team rows for compact overlay
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(HextechDarkBg.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Allies Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔵",
                            fontSize = 9.sp,
                            modifier = Modifier.width(18.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            allies.take(5).forEach { slot ->
                                ChampionAvatar(champion = slot.champion, size = 26.dp)
                            }
                        }
                    }
                    // Enemies Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔴",
                            fontSize = 9.sp,
                            modifier = Modifier.width(18.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            enemies.take(5).forEach { slot ->
                                ChampionAvatar(champion = slot.champion, size = 26.dp)
                            }
                        }
                    }
                }
            } else {
                // 5v5 Team Avatar Visualizer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(HextechDarkBg.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Allies
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        allies.take(5).forEach { slot ->
                            ChampionAvatar(
                                champion = slot.champion,
                                size = 30.dp
                            )
                        }
                    }

                    // VS Badge
                    Text(
                        text = "VS",
                        color = DangerRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    // Enemies
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        enemies.take(5).forEach { slot ->
                            ChampionAvatar(
                                champion = slot.champion,
                                size = 30.dp
                            )
                        }
                    }
                }
            }

            // Notes preview if present
            if (draft.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = " ${draft.notes}",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.SportsKabaddi, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(tr("Ver Análisis"), color = HextechCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = tr("Eliminar"), tint = TextMuted, modifier = Modifier.size(15.dp))
                    }

                    Button(
                        onClick = onLoad,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HextechGold,
                            contentColor = HextechDarkBg
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(tr("Cargar Draft"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DraftDetailBottomSheet(
    isOverlay: Boolean = false,
    draft: SavedDraftEntity,
    profiles: List<AccountProfile>,
    onDismiss: () -> Unit,
    onLoad: () -> Unit,
    onSaveNotes: (String) -> Unit,
    onAssignProfile: (String, String) -> Unit
) {
    val context = LocalContext.current
    val effectiveOverlay = isOverlay || context.isOverlayOrNonActivity()

    if (effectiveOverlay) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.85f))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                border = BorderStroke(1.5.dp, HextechGold)
            ) {
                DraftDetailInnerContent(
                    isOverlay = true,
                    draft = draft,
                    profiles = profiles,
                    onDismiss = onDismiss,
                    onLoad = onLoad,
                    onSaveNotes = onSaveNotes,
                    onAssignProfile = onAssignProfile
                )
            }
        }
    } else {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = HextechSurfaceVariant
        ) {
            DraftDetailInnerContent(
                isOverlay = false,
                draft = draft,
                profiles = profiles,
                onDismiss = onDismiss,
                onLoad = onLoad,
                onSaveNotes = onSaveNotes,
                onAssignProfile = onAssignProfile
            )
        }
    }
}

@Composable
private fun DraftDetailInnerContent(
    isOverlay: Boolean,
    draft: SavedDraftEntity,
    profiles: List<AccountProfile>,
    onDismiss: () -> Unit,
    onLoad: () -> Unit,
    onSaveNotes: (String) -> Unit,
    onAssignProfile: (String, String) -> Unit
) {
    val allies = remember(draft.allyPicksJson) { DraftHistoryRepository.parseDraftSlots(draft.allyPicksJson) }
    val enemies = remember(draft.enemyPicksJson) { DraftHistoryRepository.parseDraftSlots(draft.enemyPicksJson) }

    var userNotes by remember(draft.notes) { mutableStateOf(draft.notes) }
    var isEditingNotes by remember { mutableStateOf(false) }

    val roleObj = try { LaneRole.valueOf(draft.userRole) } catch (_: Exception) { LaneRole.MID }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isOverlay) 12.dp else 20.dp, vertical = if (isOverlay) 8.dp else 0.dp)
            .verticalScroll(rememberScrollState())
    ) {
        if (isOverlay) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(HextechSurface)
                        .clickable { onDismiss() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = HextechGold, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("Volver"), color = HextechGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.Close, contentDescription = tr("Cerrar"), tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = draft.title,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tr("Línea:") + " ${com.example.util.tr(roleObj.displayName)} • " + (if (draft.isFirstPick) tr("Primer Pick") else tr("Counter Pick")) + (if (draft.isLegendary) " • 🏆 " + tr("Legendaria") else " • ⚔️ " + tr("Clasificatoria")),
                        color = if (draft.isLegendary) Color(0xFFC084FC) else HextechGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onLoad,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HextechGold,
                        contentColor = HextechDarkBg
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("Cargar en Selección"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Account Profile Pill & Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(HextechSurface)
                    .border(1.dp, HextechGold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var accountMenuExpanded by remember { mutableStateOf(false) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(tr("Cuenta Asignada:"), color = TextMuted, fontSize = 11.5.sp)
                }

                Box {
                    Surface(
                        color = HextechGold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f)),
                        modifier = Modifier.clickable { accountMenuExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (draft.accountProfileName.isNotBlank()) draft.accountProfileName else tr("Principal"),
                                color = HextechGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.SwapHoriz, contentDescription = tr("Cambiar cuenta"), tint = HextechGold, modifier = Modifier.size(13.dp))
                        }
                    }

                    DropdownMenu(
                        expanded = accountMenuExpanded,
                        onDismissRequest = { accountMenuExpanded = false },
                        modifier = Modifier.background(HextechSurface)
                    ) {
                        profiles.forEach { prof ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(prof.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        if (prof.tag.isNotBlank()) {
                                            Text("#${prof.tag}", color = HextechCyan, fontSize = 10.sp)
                                        }
                                    }
                                },
                                onClick = {
                                    accountMenuExpanded = false
                                    onAssignProfile(prof.id, prof.name)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Equipo Aliado
            Text(tr("Tu Equipo Aliado"), color = HextechCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                allies.forEach { slot ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (slot.assignedRole == roleObj) HextechGold.copy(alpha = 0.15f) else HextechSurface)
                            .border(1.dp, if (slot.assignedRole == roleObj) HextechGold else HextechCardBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ChampionAvatar(champion = slot.champion, size = 36.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(slot.champion.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    if (slot.assignedRole == roleObj) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("(Mío)", color = HextechGold, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = slot.assignedRole.iconResId),
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = tr(slot.assignedRole.displayName) + " • Tier ${slot.champion.tier}",
                                        color = HextechCyan,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${String.format(java.util.Locale.US, "%.2f", slot.champion.winrate)}% WR",
                            color = HextechGold,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Equipo Rival
            Text(tr("Equipo Rival"), color = DangerRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                enemies.forEach { slot ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (slot.assignedRole == roleObj) DangerRedSurface else HextechSurface)
                            .border(1.dp, if (slot.assignedRole == roleObj) DangerRed else HextechCardBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ChampionAvatar(champion = slot.champion, size = 36.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(slot.champion.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    if (slot.assignedRole == roleObj) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("(" + tr("Rival Directo") + ")", color = DangerRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = slot.assignedRole.iconResId),
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = tr(slot.assignedRole.displayName) + " • Tier ${slot.champion.tier}",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${String.format(java.util.Locale.US, "%.2f", slot.champion.winrate)}% WR",
                            color = HextechGold,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Damage Balance
            Text(tr("Distribución de Daño Aliado"), color = HextechGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))) {
                if (draft.allyDamagePhysical > 0) Box(modifier = Modifier.weight(draft.allyDamagePhysical.toFloat()).fillMaxHeight().background(Color(0xFFE57373)))
                if (draft.allyDamageMagic > 0) Box(modifier = Modifier.weight(draft.allyDamageMagic.toFloat()).fillMaxHeight().background(Color(0xFF64B5F6)))
                if (draft.allyDamageTrue > 0) Box(modifier = Modifier.weight(draft.allyDamageTrue.toFloat()).fillMaxHeight().background(Color.White))
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${draft.allyDamagePhysical}% " + tr("Físico"), color = Color(0xFFE57373), fontSize = 9.5.sp)
                Text("${draft.allyDamageMagic}% " + tr("Mágico"), color = Color(0xFF64B5F6), fontSize = 9.5.sp)
                Text("${draft.allyDamageTrue}% " + tr("Verdadero"), color = Color.White, fontSize = 9.5.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Notes Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(tr("Notas Personales / Lecciones"), color = HextechGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                if (!isEditingNotes) {
                    TextButton(onClick = { isEditingNotes = true }) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(tr("Editar"), color = HextechCyan, fontSize = 11.sp)
                    }
                }
            }

            if (isEditingNotes) {
                OutlinedTextField(
                    value = userNotes,
                    onValueChange = { userNotes = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(tr("Escribe qué funcionó, errores o notas tácticas..."), color = TextMuted, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HextechGold,
                        unfocusedBorderColor = HextechCardBorder,
                        focusedContainerColor = HextechSurface,
                        unfocusedContainerColor = HextechSurface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = { isEditingNotes = false }) {
                        Text(tr("Cancelar"), color = TextMuted, fontSize = 11.5.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSaveNotes(userNotes)
                            isEditingNotes = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HextechGold, contentColor = HextechDarkBg),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(tr("Guardar Nota"), fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                    }
                }
            } else {
                Text(
                    text = if (draft.notes.isNotBlank()) draft.notes else tr("Sin notas adicionales registradas."),
                    color = if (draft.notes.isNotBlank()) TextPrimary else TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
}
