package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.example.data.SupportReplyManager
import com.example.ui.components.SupportReplyDialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.Champion
import com.example.model.RuneItem
import com.example.model.SummonerSpellItem
import com.example.data.remote.model.FeedbackReport
import com.example.data.supabase.FeedbackRepository
import com.example.data.WildRiftItemsData
import com.example.data.WildRiftSpellsAndRunes
import com.example.data.WildRiftRepository
import com.example.model.WildRiftItem
import com.example.ui.components.AppAssetImage
import com.example.ui.components.FormattedWildRiftText
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechGoldLight
import com.example.ui.theme.HextechGreen
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.HextechSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.LocalLanguage
import com.example.util.tr
import com.example.utils.parseHtmlColorToAnnotatedString
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

enum class FeedbackCategoryTab(val titleKey: String, val icon: ImageVector) {
    ALL("Todos", Icons.Default.Inbox),
    BUGS("Bugs", Icons.Default.BugReport),
    SUGGESTIONS("Sugerencias", Icons.Default.Lightbulb),
    BUILDS("Builds", Icons.Default.SportsEsports),
    SUPPORT("Soporte", Icons.Default.SupportAgent),
    SPONSOR("Patrocinador", Icons.Default.Star)
}

/**
 * Clasifica de forma estricta y segura el tipo de feedback para evitar mezclas
 */
fun getFeedbackCategory(report: FeedbackReport): String {
    val rawType = report.type.trim().uppercase(Locale.US)
    val desc = report.cleanDescription.ifEmpty { report.description }
    val title = report.title

    return when {
        rawType in listOf("PATROCINADOR", "PATROCINIO", "SPONSOR") -> "PATROCINADOR"
        rawType in listOf("BUG", "ERROR", "BUG_REPORT", "BUG / ERROR") -> "BUG"
        rawType in listOf("BUILD_SUGGESTION", "BUILD", "SUGERIR BUILD", "SUGERENCIA DE BUILD") || parseBuildSuggestionFromText(desc, title) != null -> "BUILD"
        rawType in listOf("SOPORTE", "SUPPORT", "TICKET", "AYUDA") -> "SUPPORT"
        else -> "SUGGESTION"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFeedbackBottomSheet(
    onDismiss: () -> Unit
) {
    val userRole by com.example.util.SubscriptionManager.userRole.collectAsState()
    
    // Explicit UI navigation logic verification: permitido para admin y moderador
    if (userRole != "admin" && userRole != "moderador" && !com.example.util.AuthManager.isCurrentUserAdmin()) {
        LaunchedEffect(Unit) {
            onDismiss()
        }
        return
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentCategoryTab by remember { mutableStateOf(FeedbackCategoryTab.ALL) }
    var selectedSubFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    var reports by remember { mutableStateOf<List<FeedbackReport>>(emptyList()) }
    val statusMap = remember { mutableStateMapOf<String, String>() }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reportToDelete by remember { mutableStateOf<FeedbackReport?>(null) }
    var showClearAllConfirm by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var isPurging by remember { mutableStateOf(false) }
    var previewImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var itemForDetail by remember { mutableStateOf<WildRiftItem?>(null) }
    var reportToReply by remember { mutableStateOf<FeedbackReport?>(null) }

    fun refreshStatusMap(list: List<FeedbackReport>) {
        statusMap.clear()
        for (item in list) {
            val key = item.id ?: "${item.title}_${item.createdAt}"
            val status = FeedbackRepository.getReportStatus(context, item)
            statusMap[key] = status
        }
    }

    fun loadReports() {
        isLoading = true
        errorMessage = null
        scope.launch {
            // Auto-purga de 30 días para reportes leídos/solucionados y 60 días para pendientes
            try {
                SupportReplyManager.autoPurgeAllExpired(context)
            } catch (_: Exception) {}

            val result = FeedbackRepository.getAllFeedbacks()
            isLoading = false
            if (result.isSuccess) {
                val list = result.getOrDefault(emptyList())
                reports = list
                refreshStatusMap(list)
            } else {
                errorMessage = result.exceptionOrNull()?.message ?: "Error al cargar reportes"
            }
        }
    }

    LaunchedEffect(Unit) {
        loadReports()
    }

    val isAdmin = userRole == "admin" || com.example.util.AuthManager.isCurrentUserAdmin()

    // Reportes visibles según el rol del usuario (Los reportes de Patrocinador son exclusivos del Administrador)
    val visibleReports = remember(reports, isAdmin) {
        if (isAdmin) reports else reports.filter { getFeedbackCategory(it) != "PATROCINADOR" }
    }

    val availableTabs = remember(isAdmin) {
        if (isAdmin) FeedbackCategoryTab.entries else FeedbackCategoryTab.entries.filter { it != FeedbackCategoryTab.SPONSOR }
    }

    // Contadores y listas clasificadas
    val totalCount = visibleReports.size
    val bugList = remember(visibleReports) { visibleReports.filter { getFeedbackCategory(it) == "BUG" } }
    val suggestionList = remember(visibleReports) { visibleReports.filter { getFeedbackCategory(it) == "SUGGESTION" } }
    val buildList = remember(visibleReports) { visibleReports.filter { getFeedbackCategory(it) == "BUILD" } }
    val supportList = remember(visibleReports) { visibleReports.filter { getFeedbackCategory(it) == "SUPPORT" } }
    val sponsorList = remember(visibleReports) { visibleReports.filter { getFeedbackCategory(it) == "PATROCINADOR" } }

    val currentCategoryItems = remember(visibleReports, currentCategoryTab, bugList, suggestionList, buildList, supportList, sponsorList) {
        when (currentCategoryTab) {
            FeedbackCategoryTab.ALL -> visibleReports
            FeedbackCategoryTab.BUGS -> bugList
            FeedbackCategoryTab.SUGGESTIONS -> suggestionList
            FeedbackCategoryTab.BUILDS -> buildList
            FeedbackCategoryTab.SUPPORT -> supportList
            FeedbackCategoryTab.SPONSOR -> sponsorList
        }
    }

    val pendingCount = remember(currentCategoryItems, statusMap.toMap()) {
        currentCategoryItems.count {
            val key = it.id ?: "${it.title}_${it.createdAt}"
            (statusMap[key] ?: FeedbackRepository.STATUS_PENDING) == FeedbackRepository.STATUS_PENDING
        }
    }

    val solvedCount = remember(currentCategoryItems, statusMap.toMap()) {
        currentCategoryItems.count {
            val key = it.id ?: "${it.title}_${it.createdAt}"
            val s = statusMap[key]
            s == FeedbackRepository.STATUS_SOLVED || s == FeedbackRepository.STATUS_COMPLETED
        }
    }

    val readCount = remember(currentCategoryItems, statusMap.toMap()) {
        currentCategoryItems.count {
            val key = it.id ?: "${it.title}_${it.createdAt}"
            statusMap[key] == FeedbackRepository.STATUS_READ
        }
    }

    val acceptedCount = remember(currentCategoryItems, statusMap.toMap()) {
        currentCategoryItems.count {
            val key = it.id ?: "${it.title}_${it.createdAt}"
            statusMap[key] == FeedbackRepository.STATUS_ACCEPTED
        }
    }

    val rejectedCount = remember(currentCategoryItems, statusMap.toMap()) {
        currentCategoryItems.count {
            val key = it.id ?: "${it.title}_${it.createdAt}"
            statusMap[key] == FeedbackRepository.STATUS_REJECTED
        }
    }

    // Filtrado de reportes
    val filteredReports = remember(visibleReports, currentCategoryTab, selectedSubFilter, searchQuery, statusMap.toMap()) {
        visibleReports.filter { item ->
            val key = item.id ?: "${item.title}_${item.createdAt}"
            val currentStatus = statusMap[key] ?: FeedbackRepository.STATUS_PENDING
            val cat = getFeedbackCategory(item)

            // Filtro por pestaña principal
            val matchCategory = when (currentCategoryTab) {
                FeedbackCategoryTab.ALL -> true
                FeedbackCategoryTab.BUGS -> cat == "BUG"
                FeedbackCategoryTab.SUGGESTIONS -> cat == "SUGGESTION"
                FeedbackCategoryTab.BUILDS -> cat == "BUILD"
                FeedbackCategoryTab.SUPPORT -> cat == "SUPPORT"
                FeedbackCategoryTab.SPONSOR -> cat == "PATROCINADOR"
            }

            // Filtro por subestado
            val matchSubFilter = when (selectedSubFilter) {
                "ALL" -> true
                "PENDING" -> currentStatus == FeedbackRepository.STATUS_PENDING
                "READ" -> currentStatus == FeedbackRepository.STATUS_READ
                "SOLVED" -> currentStatus == FeedbackRepository.STATUS_SOLVED || currentStatus == FeedbackRepository.STATUS_COMPLETED
                "ACCEPTED" -> currentStatus == FeedbackRepository.STATUS_ACCEPTED
                "REJECTED" -> currentStatus == FeedbackRepository.STATUS_REJECTED
                else -> true
            }

            // Filtro por texto de búsqueda
            val matchSearch = if (searchQuery.isBlank()) true else {
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true) ||
                item.deviceInfo.contains(searchQuery, ignoreCase = true) ||
                item.appVersion.contains(searchQuery, ignoreCase = true)
            }

            matchCategory && matchSubFilter && matchSearch
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = HextechDarkBg,
        dragHandle = null,
        modifier = Modifier
            .fillMaxHeight(0.92f)
            .statusBarsPadding()
            .testTag("admin_feedback_panel")
    ) {
        BackHandler(enabled = true) {
            when {
                previewImageBitmap != null -> previewImageBitmap = null
                itemForDetail != null -> itemForDetail = null
                reportToReply != null -> reportToReply = null
                reportToDelete != null -> reportToDelete = null
                showClearAllConfirm -> showClearAllConfirm = false
                currentCategoryTab != FeedbackCategoryTab.ALL -> currentCategoryTab = FeedbackCategoryTab.ALL
                searchQuery.isNotBlank() -> searchQuery = ""
                else -> onDismiss()
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D1424),
                            HextechDarkBg
                        )
                    )
                )
        ) {
            // Header del Panel con partículas rúnicas resplandecientes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HextechSurface.copy(alpha = 0.9f))
                    .border(0.5.dp, HextechCardBorder.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(HextechGold.copy(alpha = 0.18f))
                                .border(1.2.dp, HextechGold, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = HextechGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = tr("Panel de Reportes & Sugerencias"),
                                    color = HextechGold,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(HextechCyan.copy(alpha = 0.2f))
                                        .border(0.8.dp, HextechCyan.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "$totalCount",
                                        color = HextechCyan,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = tr("Gestión, revisión de bugs y evaluación de ideas"),
                                color = TextMuted,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(
                            onClick = { loadReports() },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = tr("Recargar"),
                                tint = HextechCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = tr("Cerrar"),
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Pestañas Principales con Scroll Horizontal para categorías bien diferenciadas
            val activeTabIndex = availableTabs.indexOf(currentCategoryTab).coerceAtLeast(0)
            ScrollableTabRow(
                selectedTabIndex = activeTabIndex,
                containerColor = HextechSurface,
                contentColor = HextechGold,
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    if (activeTabIndex in tabPositions.indices) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[activeTabIndex]),
                            color = HextechGold
                        )
                    }
                },
                divider = {}
            ) {
                availableTabs.forEach { tab ->
                    val isSelected = currentCategoryTab == tab
                    val count = when (tab) {
                        FeedbackCategoryTab.ALL -> totalCount
                        FeedbackCategoryTab.BUGS -> bugList.size
                        FeedbackCategoryTab.SUGGESTIONS -> suggestionList.size
                        FeedbackCategoryTab.BUILDS -> buildList.size
                        FeedbackCategoryTab.SUPPORT -> supportList.size
                        FeedbackCategoryTab.SPONSOR -> sponsorList.size
                    }
                    Tab(
                        selected = isSelected,
                        onClick = {
                            currentCategoryTab = tab
                            selectedSubFilter = "ALL"
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = if (isSelected) HextechGold else TextMuted
                                )
                                Text(
                                    text = "${tr(tab.titleKey)} ($count)",
                                    color = if (isSelected) HextechGold else TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                }
            }

            // Barra de Subfiltros Dinámicos según la categoría
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HextechDarkBg.copy(alpha = 0.95f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    StatusFilterChip(
                        label = tr("Todos"),
                        count = when (currentCategoryTab) {
                            FeedbackCategoryTab.ALL -> totalCount
                            FeedbackCategoryTab.BUGS -> bugList.size
                            FeedbackCategoryTab.SUGGESTIONS -> suggestionList.size
                            FeedbackCategoryTab.BUILDS -> buildList.size
                            FeedbackCategoryTab.SUPPORT -> supportList.size
                            FeedbackCategoryTab.SPONSOR -> sponsorList.size
                        },
                        isSelected = selectedSubFilter == "ALL",
                        color = HextechGold,
                        onClick = { selectedSubFilter = "ALL" }
                    )
                }

                item {
                    StatusFilterChip(
                        label = tr("⏳ Pendientes"),
                        count = pendingCount,
                        isSelected = selectedSubFilter == "PENDING",
                        color = Color(0xFFFFB300),
                        onClick = { selectedSubFilter = "PENDING" }
                    )
                }

                if (currentCategoryTab == FeedbackCategoryTab.ALL ||
                    currentCategoryTab == FeedbackCategoryTab.BUGS ||
                    currentCategoryTab == FeedbackCategoryTab.SUPPORT) {
                    item {
                        StatusFilterChip(
                            label = tr("️ Leídos"),
                            count = readCount,
                            isSelected = selectedSubFilter == "READ",
                            color = HextechCyan,
                            onClick = { selectedSubFilter = "READ" }
                        )
                    }

                    item {
                        StatusFilterChip(
                            label = tr(" Solucionados"),
                            count = solvedCount,
                            isSelected = selectedSubFilter == "SOLVED",
                            color = HextechGreen,
                            onClick = { selectedSubFilter = "SOLVED" }
                        )
                    }
                }

                if (currentCategoryTab == FeedbackCategoryTab.ALL ||
                    currentCategoryTab == FeedbackCategoryTab.SUGGESTIONS ||
                    currentCategoryTab == FeedbackCategoryTab.BUILDS) {
                    item {
                        StatusFilterChip(
                            label = tr(" Aceptadas"),
                            count = acceptedCount,
                            isSelected = selectedSubFilter == "ACCEPTED",
                            color = HextechGold,
                            onClick = { selectedSubFilter = "ACCEPTED" }
                        )
                    }

                    item {
                        StatusFilterChip(
                            label = tr(" Rechazadas"),
                            count = rejectedCount,
                            isSelected = selectedSubFilter == "REJECTED",
                            color = DangerRed,
                            onClick = { selectedSubFilter = "REJECTED" }
                        )
                    }
                }
            }

            // Barra de Búsqueda y Acciones de Mantenimiento
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(tr("Buscar por título, contenido o modelo..."), fontSize = 11.5.sp, color = TextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = HextechGold, modifier = Modifier.size(17.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = tr("Limpiar"), tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HextechGold,
                        unfocusedBorderColor = HextechCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = HextechSurface,
                        unfocusedContainerColor = HextechSurface
                    )
                )

                // Purga rápida > 7 días
                OutlinedButton(
                    onClick = {
                        if (!isPurging) {
                            isPurging = true
                            scope.launch {
                                val res = FeedbackRepository.purgeOldReports(days = 7)
                                isPurging = false
                                if (res.isSuccess) {
                                    Toast.makeText(context, " Purga de reportes >7 días completada", Toast.LENGTH_SHORT).show()
                                    loadReports()
                                } else {
                                    Toast.makeText(context, "Error al purgar: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = HextechSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(46.dp),
                    enabled = !isPurging
                ) {
                    if (isPurging) {
                        CircularProgressIndicator(color = HextechCyan, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.CleaningServices, contentDescription = tr("Purgar >7 días"), tint = HextechCyan, modifier = Modifier.size(18.dp))
                    }
                }

                // Borrar todos
                OutlinedButton(
                    onClick = { showClearAllConfirm = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = HextechSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(46.dp)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = tr("Borrar todo"), tint = DangerRed, modifier = Modifier.size(18.dp))
                }
            }

            // Lista de Contenido
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 4.dp)
            ) {
                if (isLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = HextechGold, modifier = Modifier.size(36.dp), strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = tr("Cargando reportes y sugerencias..."),
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                } else if (errorMessage != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.BugReport, contentDescription = null, tint = DangerRed, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = tr("Error de conexión:"), color = DangerRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = errorMessage!!, color = TextMuted, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { loadReports() },
                            colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(tr("Reintentar"), color = HextechDarkBg, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (filteredReports.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Inbox, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank() || selectedSubFilter != "ALL" || currentCategoryTab != FeedbackCategoryTab.ALL) 
                                tr("No hay resultados en esta vista") 
                            else 
                                tr("No hay reportes ni sugerencias registradas"),
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tr("Los nuevos mensajes enviados por los usuarios aparecerán aquí."),
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredReports, key = { it.id ?: "${it.title}_${it.createdAt}_${it.hashCode()}" }) { report ->
                            val key = report.id ?: "${report.title}_${report.createdAt}"
                            val currentStatus = statusMap[key] ?: FeedbackRepository.STATUS_PENDING

                            ComprehensiveFeedbackCard(
                                report = report,
                                currentStatus = currentStatus,
                                onSelectStatus = { newStatus ->
                                    statusMap[key] = newStatus
                                    FeedbackRepository.setFeedbackStatus(context, report, newStatus)
                                    val reportId = report.id
                                    if (!reportId.isNullOrBlank()) {
                                        scope.launch {
                                            FeedbackRepository.updateFeedbackStatusInCloud(reportId, newStatus)
                                            SupportReplyManager.updateReportStatus(context, reportId, newStatus)
                                        }
                                    }
                                    val msg = when (newStatus) {
                                        FeedbackRepository.STATUS_SOLVED -> " Marcado como Solucionado"
                                        FeedbackRepository.STATUS_READ -> "️ Marcado como Leído"
                                        FeedbackRepository.STATUS_ACCEPTED -> " Sugerencia Aceptada"
                                        FeedbackRepository.STATUS_REJECTED -> " Sugerencia Rechazada"
                                        else -> "⏳ Marcado como Pendiente"
                                    }
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                },
                                onReply = { reportToReply = report },
                                onDelete = { reportToDelete = report },
                                onCopy = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val textToCopy = """
                                        [${report.type}] ${report.title}
                                        Estado: $currentStatus
                                        Descripción: ${report.cleanDescription}
                                        Versión: ${report.appVersion}
                                        Dispositivo: ${report.deviceInfo}
                                        Fecha: ${report.createdAt ?: "N/A"}
                                    """.trimIndent()
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Feedback Report", textToCopy))
                                    Toast.makeText(context, " Reporte copiado al portapapeles", Toast.LENGTH_SHORT).show()
                                },
                                onOpenImage = { bmp -> previewImageBitmap = bmp },
                                onItemClick = { itemForDetail = it }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo para responder al usuario
    if (reportToReply != null) {
        val rep = reportToReply!!
        val repId = rep.id ?: "${rep.title}_${rep.createdAt}"
        val localRep = SupportReplyManager.getLocalReply(context, repId)
        val curReply = if (!rep.adminReply.isNullOrBlank()) rep.adminReply else (localRep?.text ?: "")
        val isSponsorItem = getFeedbackCategory(rep) == "PATROCINADOR" || rep.type.equals("sponsor", ignoreCase = true) || rep.type.equals("patrocinador", ignoreCase = true)

        SupportReplyDialog(
            reportId = repId,
            reportTitle = rep.title,
            reportDescription = rep.cleanDescription.ifEmpty { rep.description },
            userEmail = rep.parsedEmail ?: "",
            userName = rep.parsedUserName ?: "",
            initialReply = curReply,
            tag = if (isSponsorItem) "PATROCINADOR" else "SOPORTE",
            isFirestoreDoc = false,
            onDismiss = { reportToReply = null },
            onReplySent = { newReply, markedAsRead ->
                if (markedAsRead) {
                    statusMap[repId] = FeedbackRepository.STATUS_READ
                    FeedbackRepository.setFeedbackStatus(context, rep, FeedbackRepository.STATUS_READ)
                }
                loadReports()
            }
        )
    }

    // Diálogo de Confirmación de Eliminación Individual
    if (reportToDelete != null) {
        val rep = reportToDelete!!
        AlertDialog(
            onDismissRequest = { if (!isDeleting) reportToDelete = null },
            containerColor = HextechDarkBg,
            shape = RoundedCornerShape(14.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed, modifier = Modifier.size(22.dp))
                    Text(tr("¿Eliminar este elemento?"), color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = tr("Esta acción borrará permanentemente del servidor el reporte:"),
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"${rep.title}\"",
                        color = HextechGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rep != null) {
                            isDeleting = true
                            scope.launch {
                                val res = FeedbackRepository.deleteFeedback(rep)
                                isDeleting = false
                                if (res.isSuccess) {
                                    Toast.makeText(context, "Elemento eliminado", Toast.LENGTH_SHORT).show()
                                    reportToDelete = null
                                    loadReports()
                                } else {
                                    Toast.makeText(context, "Error: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Eliminado localmente", Toast.LENGTH_SHORT).show()
                            reportToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text(tr("Eliminar"), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { reportToDelete = null }, enabled = !isDeleting) {
                    Text(tr("Cancelar"), color = TextMuted)
                }
            }
        )
    }

    // Diálogo de Confirmación Borrar Todo
    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showClearAllConfirm = false },
            containerColor = HextechDarkBg,
            shape = RoundedCornerShape(14.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = DangerRed, modifier = Modifier.size(24.dp))
                    Text(tr("¿Borrar todos los reportes?"), color = DangerRed, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = tr("Esta acción eliminará todos los reportes y sugerencias registrados en la nube y el dispositivo de forma irreversible."),
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleting = true
                        scope.launch {
                            val res = FeedbackRepository.clearAllFeedbacks()
                            isDeleting = false
                            showClearAllConfirm = false
                            if (res.isSuccess) {
                                Toast.makeText(context, "Todos los reportes fueron eliminados", Toast.LENGTH_SHORT).show()
                                loadReports()
                            } else {
                                Toast.makeText(context, "Error: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text(tr("Sí, Borrar Todo"), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }, enabled = !isDeleting) {
                    Text(tr("Cancelar"), color = TextMuted)
                }
            }
        )
    }

    // Diálogo de Vista Previa de Imagen con Zoom y Descarga
    if (previewImageBitmap != null) {
        var scale by remember(previewImageBitmap) { mutableFloatStateOf(1f) }
        var offset by remember(previewImageBitmap) { mutableStateOf(Offset.Zero) }
        var showControls by remember(previewImageBitmap) { mutableStateOf(true) }

        Dialog(
            onDismissRequest = { 
                previewImageBitmap = null 
                scale = 1f
                offset = Offset.Zero
            },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            val view = LocalView.current
            LaunchedEffect(view) {
                val window = (view.parent as? DialogWindowProvider)?.window
                if (window != null) {
                    val insetsController = WindowCompat.getInsetsController(window, view)
                    insetsController.hide(WindowInsetsCompat.Type.systemBars())
                    insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        showControls = !showControls
                    }
            ) {
                // Imagen con zoom
                Image(
                    bitmap = previewImageBitmap!!.asImageBitmap(),
                    contentDescription = "Vista previa ampliable",
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(previewImageBitmap) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                val maxOffsetX = (size.width * (scale - 1f)) / 2f
                                val maxOffsetY = (size.height * (scale - 1f)) / 2f
                                offset = if (scale > 1f) {
                                    Offset(
                                        (offset.x + pan.x).coerceIn(-maxOffsetX, maxOffsetX),
                                        (offset.y + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                                    )
                                } else {
                                    Offset.Zero
                                }
                            }
                        }
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        ),
                    contentScale = ContentScale.Fit
                )

                // Top Bar superpuesta
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically, 
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = HextechGold, modifier = Modifier.size(20.dp))
                            Text(
                                text = if (scale > 1.05f) "${tr("Captura")} (${(scale * 100).toInt()}%)" else tr("Captura Adjunta"), 
                                color = HextechGold, 
                                fontSize = 16.sp, 
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            // Zoom Out
                            IconButton(
                                onClick = {
                                    scale = (scale / 1.3f).coerceIn(1f, 5f)
                                    if (scale <= 1.05f) {
                                        scale = 1f
                                        offset = Offset.Zero
                                    }
                                },
                                modifier = Modifier.size(36.dp),
                                enabled = scale > 1f
                            ) {
                                Icon(Icons.Default.ZoomOut, contentDescription = "Alejar", tint = if (scale > 1f) HextechCyan else TextMuted, modifier = Modifier.size(22.dp))
                            }

                            // Zoom In
                            IconButton(
                                onClick = {
                                    scale = (scale * 1.3f).coerceIn(1f, 5f)
                                },
                                modifier = Modifier.size(36.dp),
                                enabled = scale < 5f
                            ) {
                                Icon(Icons.Default.ZoomIn, contentDescription = "Acercar", tint = if (scale < 5f) HextechCyan else TextMuted, modifier = Modifier.size(22.dp))
                            }

                            // Reset Zoom
                            if (scale > 1.05f) {
                                IconButton(
                                    onClick = {
                                        scale = 1f
                                        offset = Offset.Zero
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.RestartAlt, contentDescription = "Restablecer", tint = HextechGold, modifier = Modifier.size(22.dp))
                                }
                            }

                            // Descargar Imagen
                            IconButton(
                                onClick = {
                                    saveBitmapToGallery(context, previewImageBitmap!!)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = "Descargar", tint = HextechGreen, modifier = Modifier.size(22.dp))
                            }

                            // Cerrar
                            IconButton(
                                onClick = { 
                                    previewImageBitmap = null 
                                    scale = 1f
                                    offset = Offset.Zero
                                }, 
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = tr("Cerrar"), tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }

                // Tip flotante en la parte inferior
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 36.dp)
                ) {
                    Text(
                        text = tr(" Pellizca o usa los botones para hacer zoom y arrastrar\nToca la pantalla para ocultar los controles"),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }

    // Diálogo emergente de Detalle de Objeto (al tocar un objeto en la build)
    itemForDetail?.let { item ->
        AdminItemDetailDialog(
            item = item,
            onDismiss = { itemForDetail = null }
        )
    }
}

@Composable
private fun StatusFilterChip(
    label: String,
    count: Int,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) color.copy(alpha = 0.22f)
                else HextechSurface
            )
            .border(
                width = if (isSelected) 1.5.dp else 0.8.dp,
                color = if (isSelected) color else HextechCardBorder.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = label,
                color = if (isSelected) color else TextPrimary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isSelected) color.copy(alpha = 0.3f) else HextechDarkBg)
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = count.toString(),
                    color = if (isSelected) color else TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ComprehensiveFeedbackCard(
    report: FeedbackReport,
    currentStatus: String,
    onSelectStatus: (String) -> Unit,
    onReply: (() -> Unit)? = null,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onOpenImage: (Bitmap) -> Unit,
    onItemClick: (WildRiftItem) -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val itemCategory = remember(report) { getFeedbackCategory(report) }
    val isBugOrSupport = itemCategory == "BUG" || itemCategory == "SUPPORT"

    val isReadOrSolved = currentStatus == FeedbackRepository.STATUS_READ ||
            currentStatus == FeedbackRepository.STATUS_SOLVED ||
            currentStatus == FeedbackRepository.STATUS_COMPLETED ||
            currentStatus == FeedbackRepository.STATUS_ACCEPTED
    val createdMillis = remember(report.createdAt) {
        SupportReplyManager.parseDateToMillis(report.createdAt)
    }
    val countdown = remember(createdMillis, isReadOrSolved) {
        SupportReplyManager.calculateCountdown(createdMillis, isReadOrSolved)
    }

    // Parsear sugerencia de build si contiene el formato estructurado
    val parsedBuild = remember(report.cleanDescription, report.description, report.title) {
        parseBuildSuggestionFromText(report.cleanDescription.ifEmpty { report.description }, report.title)
    }

    // Parsear imágenes base64 si existen
    val attachedBitmaps = remember(report.deviceInfo, report.description) {
        extractBase64Images(report.deviceInfo + "\n" + report.description)
    }

    // Información del tipo
    val (typeColor, typeIcon, typeLabel) = when (itemCategory) {
        "PATROCINADOR" -> Triple(HextechGold, Icons.Default.Star, "PATROCINADOR")
        "BUG" -> Triple(DangerRed, Icons.Default.BugReport, "BUG / ERROR")
        "SUPPORT" -> Triple(HextechCyan, Icons.Default.SupportAgent, "SOPORTE")
        "BUILD" -> Triple(HextechGold, Icons.Default.SportsEsports, "BUILD SUGERIDA")
        else -> Triple(Color(0xFFFFB74D), Icons.Default.Lightbulb, "SUGERENCIA")
    }

    // Información del estado visual actual
    val (statusLabel, statusColor, statusIcon) = when (currentStatus) {
        FeedbackRepository.STATUS_SOLVED, FeedbackRepository.STATUS_COMPLETED -> {
            Triple(tr("Solucionado"), HextechGreen, Icons.Default.CheckCircle)
        }
        FeedbackRepository.STATUS_READ -> {
            Triple(tr("Leído"), HextechCyan, Icons.Default.Visibility)
        }
        FeedbackRepository.STATUS_ACCEPTED -> {
            Triple(tr("Aceptada"), HextechGold, Icons.Default.Star)
        }
        FeedbackRepository.STATUS_REJECTED -> {
            Triple(tr("Rechazada"), DangerRed, Icons.Default.Cancel)
        }
        else -> {
            Triple(tr("Pendiente"), Color(0xFFFFB300), Icons.Default.HourglassEmpty)
        }
    }

    val cardBorderColor by animateColorAsState(
        targetValue = statusColor.copy(alpha = 0.45f),
        label = "card_border"
    )

    val formattedDate = remember(report.createdAt) {
        formatReportDate(report.createdAt)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, cardBorderColor, RoundedCornerShape(12.dp))
            .animateContentSize(animationSpec = tween(180)),
        colors = CardDefaults.cardColors(containerColor = HextechSurface.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Fila Superior: Badges + Fecha + Acciones (Copiar, Borrar, Expandir)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Badge de Tipo
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(typeColor.copy(alpha = 0.18f))
                            .border(1.dp, typeColor.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(typeIcon, contentDescription = null, tint = typeColor, modifier = Modifier.size(11.dp))
                            Text(text = typeLabel, color = typeColor, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Badge de Estado Actual
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.18f))
                            .border(1.dp, statusColor.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(11.dp))
                            Text(text = statusLabel, color = statusColor, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Contador de Auto-eliminación (30 días leídos, 60 días sin leer)
                    val countdownBg = when {
                        countdown.isExpired -> DangerRed.copy(alpha = 0.2f)
                        countdown.remainingDays <= 3 -> Color(0xFFFF9800).copy(alpha = 0.2f)
                        isReadOrSolved -> HextechCyan.copy(alpha = 0.15f)
                        else -> Color(0xFFFFB300).copy(alpha = 0.15f)
                    }
                    val countdownColor = when {
                        countdown.isExpired -> DangerRed
                        countdown.remainingDays <= 3 -> Color(0xFFFF9800)
                        isReadOrSolved -> HextechCyan
                        else -> Color(0xFFFFB300)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(countdownBg)
                            .border(0.8.dp, countdownColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = countdownColor, modifier = Modifier.size(10.dp))
                            Text(text = countdown.displayText, color = countdownColor, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = formattedDate,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCopy, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = tr("Copiar"), tint = TextMuted, modifier = Modifier.size(15.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = tr("Eliminar"), tint = DangerRed.copy(alpha = 0.8f), modifier = Modifier.size(15.dp))
                    }
                    IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(26.dp)) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) tr("Contraer") else tr("Expandir"),
                            tint = HextechGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Título con botón para copiarlo directamente
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(HextechDarkBg.copy(alpha = 0.45f))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = report.title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { expanded = !expanded }
                )
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Título", report.title))
                        Toast.makeText(context, " Título copiado al portapapeles", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar título",
                        tint = HextechGold,
                        modifier = Modifier.size(13.5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            // RENDERIZADO DE SUGERENCIA DE BUILD GRÁFICA O DESCRIPCIÓN ESTÁNDAR
            val cleanDescription = remember(report.cleanDescription) {
                cleanDescriptionText(report.cleanDescription)
            }

            if (parsedBuild != null) {
                Spacer(modifier = Modifier.height(2.dp))
                GraphicalBuildSuggestionView(
                    build = parsedBuild,
                    onItemClick = onItemClick
                )
                Spacer(modifier = Modifier.height(4.dp))
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(HextechDarkBg.copy(alpha = 0.25f))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = cleanDescription,
                        color = TextSecondary,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp,
                        maxLines = if (expanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { expanded = !expanded }
                    )
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Descripción", cleanDescription))
                            Toast.makeText(context, " Descripción copiada al portapapeles", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(24.dp)
                            .padding(start = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copiar descripción",
                            tint = HextechCyan,
                            modifier = Modifier.size(13.5.dp)
                        )
                    }
                }
            }

            // Miniaturas de Imágenes Adjuntas con indicador de zoom/descarga
            if (attachedBitmaps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = HextechGold, modifier = Modifier.size(14.dp))
                    Text(
                        text = tr("Capturas adjuntas (Toca para ampliar y descargar):"),
                        color = TextMuted,
                        fontSize = 10.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(attachedBitmaps) { bmp ->
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, HextechGold.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .clickable { onOpenImage(bmp) }
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Captura adjunta",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(topStart = 4.dp))
                                    .padding(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = "Ampliar",
                                    tint = HextechGold,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // CONTROLES DE ESTADO (Requisitos de selección para el usuario/admin)
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(HextechDarkBg.copy(alpha = 0.7f))
                    .border(0.6.dp, HextechCardBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Column {
                    Text(
                        text = tr("Marcar estado:"),
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    if (isBugOrSupport) {
                        // Opciones de Reportes / Soporte: Pendiente | Leído | Solucionado
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StatusActionButton(
                                label = tr("Pendiente"),
                                icon = Icons.Default.HourglassEmpty,
                                isSelected = currentStatus == FeedbackRepository.STATUS_PENDING,
                                activeColor = Color(0xFFFFB300),
                                modifier = Modifier.weight(1f),
                                onClick = { onSelectStatus(FeedbackRepository.STATUS_PENDING) }
                            )
                            StatusActionButton(
                                label = tr("Leído"),
                                icon = Icons.Default.Visibility,
                                isSelected = currentStatus == FeedbackRepository.STATUS_READ,
                                activeColor = HextechCyan,
                                modifier = Modifier.weight(1f),
                                onClick = { onSelectStatus(FeedbackRepository.STATUS_READ) }
                            )
                            StatusActionButton(
                                label = tr("Solucionado"),
                                icon = Icons.Default.CheckCircle,
                                isSelected = currentStatus == FeedbackRepository.STATUS_SOLVED || currentStatus == FeedbackRepository.STATUS_COMPLETED,
                                activeColor = HextechGreen,
                                modifier = Modifier.weight(1.1f),
                                onClick = { onSelectStatus(FeedbackRepository.STATUS_SOLVED) }
                            )
                        }
                    } else {
                        // Opciones de Sugerencias / Builds: Pendiente | Aceptada | Rechazada
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StatusActionButton(
                                label = tr("Pendiente"),
                                icon = Icons.Default.HourglassEmpty,
                                isSelected = currentStatus == FeedbackRepository.STATUS_PENDING,
                                activeColor = Color(0xFFFFB300),
                                modifier = Modifier.weight(1f),
                                onClick = { onSelectStatus(FeedbackRepository.STATUS_PENDING) }
                            )
                            StatusActionButton(
                                label = tr("Aceptada"),
                                icon = Icons.Default.Check,
                                isSelected = currentStatus == FeedbackRepository.STATUS_ACCEPTED,
                                activeColor = HextechGold,
                                modifier = Modifier.weight(1f),
                                onClick = { onSelectStatus(FeedbackRepository.STATUS_ACCEPTED) }
                            )
                            StatusActionButton(
                                label = tr("Rechazada"),
                                icon = Icons.Default.Close,
                                isSelected = currentStatus == FeedbackRepository.STATUS_REJECTED,
                                activeColor = DangerRed,
                                modifier = Modifier.weight(1f),
                                onClick = { onSelectStatus(FeedbackRepository.STATUS_REJECTED) }
                            )
                        }
                    }
                }
            }

            // 💬 Sección de Respuesta de Soporte
            val reportKey = report.id ?: "${report.title}_${report.createdAt}"
            val localReply = remember(reportKey) { SupportReplyManager.getLocalReply(context, reportKey) }
            val finalReplyText = if (!report.adminReply.isNullOrBlank()) report.adminReply else (localReply?.text ?: "")

            if (finalReplyText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(HextechDarkBg)
                        .border(1.dp, HextechCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(13.dp))
                                Text(text = tr("Respuesta de Soporte Coach:"), color = HextechCyan, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            }
                            if (onReply != null && (itemCategory == "SUPPORT" || itemCategory == "PATROCINADOR" || itemCategory == "BUG")) {
                                Text(
                                    text = tr("Editar"),
                                    color = HextechCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { onReply() }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = finalReplyText,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        // Información del moderador que respondió
                        val authorName = report.repliedBy?.takeIf { it.isNotBlank() } ?: localReply?.author
                        val authorMail = report.repliedEmail?.takeIf { it.isNotBlank() } ?: localReply?.authorEmail
                        if (!authorName.isNullOrBlank() || !authorMail.isNullOrBlank()) {
                            val displayName = authorName ?: "Equipo Coach"
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(HextechSurfaceVariant.copy(alpha = 0.6f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = HextechGold, modifier = Modifier.size(10.dp))
                                Text(
                                    text = "${tr("Respondido por:")} $displayName${if (authorMail != null) " • $authorMail" else ""}",
                                    color = HextechGold,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            } else if (onReply != null && (itemCategory == "SUPPORT" || itemCategory == "PATROCINADOR" || itemCategory == "BUG")) {
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedButton(
                    onClick = onReply,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp),
                    border = BorderStroke(0.8.dp, HextechCyan.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Reply, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(13.dp))
                        Text(text = tr("Responder Mensaje"), color = HextechCyan, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Diagnóstico y metadatos expandibles
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(140)),
                exit = fadeOut(tween(140))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(HextechDarkBg.copy(alpha = 0.9f))
                        .border(0.5.dp, HextechCardBorder, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val cleanDeviceInfo = remember(report.deviceInfo) {
                        cleanDeviceInfoText(report.deviceInfo)
                    }

                    if (parsedBuild != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(HextechSurfaceVariant.copy(alpha = 0.4f))
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tr("Texto crudo de la sugerencia:"),
                                color = HextechGold,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Build Cruda", cleanDescription))
                                    Toast.makeText(context, " Build copiada al portapapeles", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = HextechGold, modifier = Modifier.size(13.dp))
                            }
                        }
                        Text(
                            text = cleanDescription,
                            color = TextSecondary,
                            fontSize = 10.5.sp,
                            lineHeight = 14.5.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Dispositivo", cleanDeviceInfo))
                                Toast.makeText(context, " Dispositivo copiado", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 2.dp, horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Smartphone, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(13.dp))
                        Text(
                            text = "${tr("Dispositivo:")} $cleanDeviceInfo",
                            color = HextechCyan,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = HextechCyan.copy(alpha = 0.5f), modifier = Modifier.size(11.dp))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Versión", report.appVersion))
                                Toast.makeText(context, " Versión copiada", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 2.dp, horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = HextechGold, modifier = Modifier.size(13.dp))
                        Text(
                            text = "${tr("Versión:")} ${report.appVersion}",
                            color = HextechGold,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = HextechGold.copy(alpha = 0.5f), modifier = Modifier.size(11.dp))
                    }
                    
                    if (!report.parsedEmail.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically, 
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Correo", report.parsedEmail))
                                    Toast.makeText(context, "️ Correo copiado", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 2.dp, horizontal = 4.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF64B5F6), modifier = Modifier.size(13.dp))
                            Text(
                                text = "Correo: ${report.parsedEmail}",
                                color = Color(0xFF64B5F6),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF64B5F6).copy(alpha = 0.5f), modifier = Modifier.size(11.dp))
                        }
                    }
                    
                    if (!report.id.isNullOrBlank()) {
                        Text(
                            text = "UUID: ${report.id}",
                            color = TextMuted,
                            fontSize = 9.5.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusActionButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isSelected) activeColor.copy(alpha = 0.25f)
                else HextechSurface
            )
            .border(
                width = if (isSelected) 1.2.dp else 0.6.dp,
                color = if (isSelected) activeColor else HextechCardBorder.copy(alpha = 0.5f),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) activeColor else TextMuted,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = if (isSelected) activeColor else TextPrimary,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

private fun formatReportDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "Reciente"
    return try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val cleanDate = dateString.substringBefore(".").substringBefore("+").substringBefore("Z")
        val date = isoFormat.parse(cleanDate)
        if (date != null) {
            val localFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            localFormat.format(date)
        } else {
            dateString.take(16).replace("T", " ")
        }
    } catch (e: Exception) {
        dateString.take(16).replace("T", " ")
    }
}

private fun cleanDescriptionText(text: String): String {
    return text.substringBefore("[IMAGE_BASE64]").trim()
}

private fun cleanDeviceInfoText(text: String): String {
    return text.substringBefore("[IMAGE_BASE64]").trim()
}

private fun extractBase64Images(rawText: String): List<Bitmap> {
    val results = mutableListOf<Bitmap>()
    if (!rawText.contains("[IMAGE_BASE64]")) return results
    val parts = rawText.split("[IMAGE_BASE64]")
    for (i in 1 until parts.size) {
        val segment = parts[i].trim().substringBefore("\n\n").substringBefore("[IMAGE_BASE64]").trim()
        if (segment.isNotEmpty()) {
            try {
                val cleanBase64 = if (segment.contains(",")) segment.substringAfter(",") else segment
                val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bmp != null) {
                    results.add(bmp)
                }
            } catch (e: Exception) {
                // Ignore corrupted image
            }
        }
    }
    return results
}

private fun saveBitmapToGallery(context: Context, bitmap: Bitmap) {
    try {
        val filename = "WR_Feedback_${System.currentTimeMillis()}.png"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/WildRiftFeedback")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (imageUri != null) {
                resolver.openOutputStream(imageUri)?.use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
                Toast.makeText(context, " Captura guardada en Galería (Imágenes)", Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val appDir = java.io.File(imagesDir, "WildRiftFeedback")
            if (!appDir.exists()) appDir.mkdirs()
            val imageFile = java.io.File(appDir, filename)
            java.io.FileOutputStream(imageFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            android.media.MediaScannerConnection.scanFile(
                context,
                arrayOf(imageFile.absolutePath),
                arrayOf("image/png"),
                null
            )
            Toast.makeText(context, " Captura guardada en Galería (Imágenes)", Toast.LENGTH_SHORT).show()
            return
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error al guardar imagen: ${e.localizedMessage ?: "Error desconocido"}", Toast.LENGTH_SHORT).show()
    }
}

// ==========================================
// MODELOS Y PARSER DE SUGERENCIAS DE BUILD
// ==========================================

data class ParsedBuildSuggestion(
    val championName: String,
    val championAvatar: String?,
    val championObj: Champion? = null,
    val role: String?,
    val coreItems: List<WildRiftItem>,
    val situationalItems: List<WildRiftItem>,
    val altSituationalItems: List<WildRiftItem>,
    val boots: WildRiftItem?,
    val keystoneRune: RuneItem?,
    val secondaryRunes: List<RuneItem>,
    val spells: List<SummonerSpellItem>,
    val tacticalNotes: String?
)

private fun normalizeSearchString(text: String): String {
    return text.lowercase(Locale.ROOT)
        .replace("á", "a")
        .replace("é", "e")
        .replace("í", "i")
        .replace("ó", "o")
        .replace("ú", "u")
        .replace("ü", "u")
        .replace("ñ", "n")
        .replace(Regex("[^a-z0-9]"), "")
        .trim()
}

private fun findChampionByName(rawName: String): Champion? {
    val clean = rawName.trim().removePrefix("•").trim()
    if (clean.isEmpty() || clean.equals("Ninguno", ignoreCase = true) || clean.equals("N/A", ignoreCase = true) || clean.equals("General", ignoreCase = true) || clean.equals("Campeon General", ignoreCase = true) || clean.equals("Campeón General", ignoreCase = true)) return null
    val norm = normalizeSearchString(clean)

    // Búsqueda exacta
    WildRiftRepository.champions.firstOrNull { it.name.equals(clean, ignoreCase = true) || it.nameEn.equals(clean, ignoreCase = true) }?.let { return it }
    WildRiftRepository.champions.firstOrNull { normalizeSearchString(it.name) == norm || normalizeSearchString(it.nameEn) == norm || it.id.equals(norm, ignoreCase = true) }?.let { return it }
    WildRiftRepository.champions.firstOrNull {
        val normEs = normalizeSearchString(it.name)
        val normEn = normalizeSearchString(it.nameEn)
        normEs.contains(norm) || norm.contains(normEs) || normEn.contains(norm) || norm.contains(normEn)
    }?.let { return it }
    return null
}

private fun findItemByName(rawName: String): WildRiftItem? {
    val clean = rawName.trim()
    if (clean.isEmpty() || clean.equals("Ninguno", ignoreCase = true) || clean.equals("N/A", ignoreCase = true)) return null
    val norm = normalizeSearchString(clean)
    
    // Búsqueda exacta primero
    WildRiftItemsData.list.firstOrNull { it.name.equals(clean, ignoreCase = true) || it.nameEn.equals(clean, ignoreCase = true) }?.let { return it }
    WildRiftItemsData.list.firstOrNull { normalizeSearchString(it.name) == norm || normalizeSearchString(it.nameEn) == norm }?.let { return it }
    
    // Búsqueda por contención
    WildRiftItemsData.list.firstOrNull { 
        val normEs = normalizeSearchString(it.name)
        val normEn = normalizeSearchString(it.nameEn)
        normEs.contains(norm) || norm.contains(normEs) || normEn.contains(norm) || norm.contains(normEn)
    }?.let { return it }
    
    return null
}

private fun findRuneByName(rawName: String): RuneItem? {
    val clean = rawName.trim().removePrefix("Clave:").removePrefix("Secundarias:").trim()
    if (clean.isEmpty() || clean.equals("Ninguno", ignoreCase = true) || clean.equals("Ninguna", ignoreCase = true) || clean.equals("N/A", ignoreCase = true)) return null
    val norm = normalizeSearchString(clean)
    
    WildRiftSpellsAndRunes.runes.firstOrNull { it.name.equals(clean, ignoreCase = true) || it.nameEn.equals(clean, ignoreCase = true) }?.let { return it }
    WildRiftSpellsAndRunes.runes.firstOrNull { normalizeSearchString(it.name) == norm || normalizeSearchString(it.nameEn) == norm }?.let { return it }
    WildRiftSpellsAndRunes.runes.firstOrNull { 
        val normEs = normalizeSearchString(it.name)
        val normEn = normalizeSearchString(it.nameEn)
        normEs.contains(norm) || norm.contains(normEs) || normEn.contains(norm) || norm.contains(normEn)
    }?.let { return it }
    return null
}

private fun findSpellByName(rawName: String): SummonerSpellItem? {
    val clean = rawName.trim()
    if (clean.isEmpty() || clean.equals("Ninguno", ignoreCase = true) || clean.equals("N/A", ignoreCase = true)) return null
    val norm = normalizeSearchString(clean)
    
    WildRiftSpellsAndRunes.summonerSpells.firstOrNull { it.name.equals(clean, ignoreCase = true) || it.nameEn.equals(clean, ignoreCase = true) }?.let { return it }
    WildRiftSpellsAndRunes.summonerSpells.firstOrNull { normalizeSearchString(it.name) == norm || normalizeSearchString(it.nameEn) == norm }?.let { return it }
    WildRiftSpellsAndRunes.summonerSpells.firstOrNull { 
        val normEs = normalizeSearchString(it.name)
        val normEn = normalizeSearchString(it.nameEn)
        normEs.contains(norm) || norm.contains(normEs) || normEn.contains(norm) || norm.contains(normEn)
    }?.let { return it }
    return null
}

fun parseBuildSuggestionFromText(text: String, title: String = ""): ParsedBuildSuggestion? {
    if (!text.contains("SUGERENCIA DE BUILD", ignoreCase = true) &&
        !text.contains("OBJETOS CORE", ignoreCase = true) &&
        !text.contains("OBJETOS PRINCIPALES", ignoreCase = true) &&
        !text.contains("OBJETOS SITUACIONALES", ignoreCase = true) &&
        !text.contains("RUNAS", ignoreCase = true) &&
        !text.contains("CAMPEÓN", ignoreCase = true) &&
        !text.contains("CAMPEON", ignoreCase = true) &&
        !title.contains("Build", ignoreCase = true)) {
        return null
    }

    try {
        val lines = text.lines()
        var champName = ""
        var role: String? = null
        val coreItemsList = mutableListOf<WildRiftItem>()
        val sitItemsList = mutableListOf<WildRiftItem>()
        val altSitItemsList = mutableListOf<WildRiftItem>()
        var bootsItem: WildRiftItem? = null
        var keystone: RuneItem? = null
        val secondaryRunesList = mutableListOf<RuneItem>()
        val spellsList = mutableListOf<SummonerSpellItem>()
        val notesBuilder = StringBuilder()
        var isReadingNotes = false

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue

            if (isReadingNotes) {
                notesBuilder.appendLine(rawLine)
                continue
            }

            val upper = line.uppercase(Locale.ROOT)

            if (upper.contains("CAMPEÓN:") || upper.contains("CAMPEON:")) {
                val value = line.substringAfter(":").trim()
                if (value.contains("(") && value.contains(")")) {
                    champName = value.substringBefore("(").trim().removePrefix("•").trim()
                    role = value.substringAfter("(").substringBefore(")").trim()
                } else {
                    champName = value.removePrefix("•").trim()
                }
            } else if (upper.contains("ROL/LÍNEA:") || upper.contains("ROL/LINEA:") || upper.contains("LÍNEA / ROL:") || upper.contains("LINEA / ROL:") || upper.contains("ROL:")) {
                role = line.substringAfter(":").trim().removePrefix("•").trim()
            } else if (upper.contains("BOTAS") || upper.contains("ENCANTAMIENTO")) {
                val valStr = line.substringAfter(":").trim().removePrefix("•").trim()
                findItemByName(valStr)?.let { bootsItem = it }
            } else if (upper.contains("RUNAS:") || upper.contains("RUNA CLAVE:") || upper.contains("RUNA PRINCIPAL:")) {
                val valStr = line.substringAfter(":").trim().removePrefix("•").trim()
                if (valStr.contains("|") || valStr.contains("+") || valStr.contains(",")) {
                    val parts = valStr.split(Regex("[|+,]")).map { it.trim().removePrefix("Clave:").removePrefix("Secundarias:").trim() }.filter { it.isNotEmpty() }
                    if (parts.isNotEmpty()) {
                        findRuneByName(parts[0])?.let { keystone = it }
                        for (i in 1 until parts.size) {
                            findRuneByName(parts[i])?.let { 
                                if (!secondaryRunesList.contains(it)) secondaryRunesList.add(it)
                            }
                        }
                    }
                } else {
                    findRuneByName(valStr)?.let { keystone = it }
                }
            } else if (upper.contains("SECUNDARIAS:") || upper.contains("RUNAS SECUNDARIAS:")) {
                val valStr = line.substringAfter(":").trim().removePrefix("•").trim()
                val parts = valStr.split(Regex("[,|+]| - | • ")).map { it.trim() }.filter { it.isNotEmpty() }
                for (part in parts) {
                    findRuneByName(part)?.let {
                        if (!secondaryRunesList.contains(it)) secondaryRunesList.add(it)
                    }
                }
            } else if (upper.contains("HECHIZOS:") || upper.contains("HECHIZOS DE INVOCADOR:")) {
                val valStr = line.substringAfter(":").trim().removePrefix("•").trim()
                val parts = valStr.split(Regex("[,|+]| / | - | • ")).map { it.trim() }.filter { it.isNotEmpty() }
                for (part in parts) {
                    findSpellByName(part)?.let {
                        if (!spellsList.contains(it)) spellsList.add(it)
                    }
                }
            } else if (upper.contains("OBJETOS CORE") || upper.contains("OBJETOS PRINCIPALES")) {
                val valStr = line.substringAfter(":").trim().removePrefix("•").trim()
                val parts = valStr.split(Regex("[•,+]| - ")).map { it.trim() }.filter { it.isNotEmpty() }
                for (part in parts) {
                    findItemByName(part)?.let {
                        if (!coreItemsList.contains(it)) coreItemsList.add(it)
                    }
                }
            } else if (upper.contains("OBJETOS SITUACIONALES") || upper.contains("SITUACIONALES (7-8)") || upper.contains("SITUACIONALES (7 Y 8)")) {
                val valStr = line.substringAfter(":").trim().removePrefix("•").trim()
                val parts = valStr.split(Regex("[•,+]| - ")).map { it.trim() }.filter { it.isNotEmpty() }
                for (part in parts) {
                    findItemByName(part)?.let {
                        if (!sitItemsList.contains(it)) sitItemsList.add(it)
                    }
                }
            } else if (upper.contains("ALTERNATIVAS SITUACIONALES") || upper.contains("ALT SITUACIONAL") || upper.contains("ALT SIT")) {
                val valStr = line.substringAfter(":").trim().removePrefix("•").trim()
                val parts = valStr.split(Regex("[•,+]| - ")).map { it.trim() }.filter { it.isNotEmpty() }
                for (part in parts) {
                    findItemByName(part)?.let {
                        if (!altSitItemsList.contains(it)) altSitItemsList.add(it)
                    }
                }
            } else if (upper.contains("JUSTIFICACIÓN TÁCTICA") || upper.contains("JUSTIFICACION TACTICA") || upper.contains("NOTAS / EXPLICACIÓN TÁCTICA:") || upper.contains("NOTAS / EXPLICACION TACTICA:") || upper.contains("EXPLICACIÓN TÁCTICA:") || upper.contains("EXPLICACION TACTICA:") || upper.contains("NOTAS:")) {
                isReadingNotes = true
                val remaining = line.substringAfter(":").trim()
                if (remaining.isNotEmpty()) {
                    notesBuilder.appendLine(remaining)
                }
            } else if (Regex("""^[1-6]\.\s*""").containsMatchIn(line)) {
                // Item core 1 a 6
                val itemName = line.replace(Regex("""^[1-6]\.\s*"""), "").trim()
                findItemByName(itemName)?.let { if (!coreItemsList.contains(it)) coreItemsList.add(it) }
            } else if (line.startsWith("7.") || line.startsWith("8.") || upper.contains("SITUACIONAL 1") || upper.contains("SITUACIONAL 2")) {
                val itemName = line.substringAfter(":").ifEmpty { line.replace(Regex("""^[78]\.\s*(\(.*\))?\s*:?"""), "") }.trim()
                findItemByName(itemName)?.let { if (!sitItemsList.contains(it)) sitItemsList.add(it) }
            }
        }

        // Si el campeón no fue detectado en el cuerpo, extraerlo del título (ej: "Sugerencia de Build para Zed (Mid)")
        if (champName.isBlank() && title.isNotBlank()) {
            if (title.contains("para ", ignoreCase = true)) {
                val extracted = title.substringAfter("para ", "").substringBefore("(").trim()
                if (extracted.isNotBlank()) {
                    val matchChamp = findChampionByName(extracted)
                    champName = matchChamp?.name ?: extracted
                }
            }
            if (champName.isBlank()) {
                for (c in WildRiftRepository.champions) {
                    if (title.contains(c.name, ignoreCase = true) || title.contains(c.nameEn, ignoreCase = true)) {
                        champName = c.name
                        break
                    }
                }
            }
            if (role == null && title.contains("(") && title.contains(")")) {
                role = title.substringAfter("(").substringBefore(")").trim()
            }
        }

        if (champName.isBlank() && coreItemsList.isEmpty() && sitItemsList.isEmpty() && bootsItem == null) {
            return null
        }

        // Buscar el objeto Champion real de la base de datos para obtener avatarUrl oficial y tier
        val foundChamp = if (champName.isNotBlank()) findChampionByName(champName) else null
        val effectiveChampName = foundChamp?.name ?: champName.ifBlank { "Campeón" }
        val champAvatar = foundChamp?.avatarUrl?.takeIf { it.isNotBlank() }

        return ParsedBuildSuggestion(
            championName = effectiveChampName,
            championAvatar = champAvatar,
            championObj = foundChamp,
            role = role,
            coreItems = coreItemsList,
            situationalItems = sitItemsList,
            altSituationalItems = altSitItemsList,
            boots = bootsItem,
            keystoneRune = keystone,
            secondaryRunes = secondaryRunesList,
            spells = spellsList,
            tacticalNotes = notesBuilder.toString().trim().ifEmpty { null }
        )
    } catch (e: Exception) {
        return null
    }
}

// ==========================================
// VISTA GRÁFICA DE SUGERENCIA DE BUILD
// ==========================================

@Composable
private fun GraphicalBuildSuggestionView(
    build: ParsedBuildSuggestion,
    onItemClick: (WildRiftItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(HextechDarkBg.copy(alpha = 0.85f))
            .border(1.dp, HextechGold.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ENCABEZADO: Campeón + Rol + Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .border(2.dp, HextechGold, CircleShape)
                    .background(HextechSurface),
                contentAlignment = Alignment.Center
            ) {
                if (build.championObj != null) {
                    ChampionAvatar(
                        champion = build.championObj,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (!build.championAvatar.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(build.championAvatar)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)
                            
                            .build(),
                        contentDescription = build.championName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(HextechGold.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = build.championName.take(2).uppercase(),
                            color = HextechGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = build.championName,
                        color = HextechGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (build.championObj != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(HextechCyan.copy(alpha = 0.2f))
                                .border(0.8.dp, HextechCyan.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "Tier ${build.championObj.tier}",
                                color = HextechCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                if (!build.role.isNullOrBlank()) {
                    Text(
                        text = "Rol / Línea: ${build.role}",
                        color = HextechCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Hechizos y Runa Clave en el header si están disponibles
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (spell in build.spells) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.dp, HextechCyan.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                            .background(Color.Black)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(spell.iconUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)
                                
                                .build(),
                            contentDescription = spell.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                build.keystoneRune?.let { rune ->
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .border(1.dp, HextechGold, CircleShape)
                            .background(Color.Black)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(rune.iconUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)
                                
                                .build(),
                            contentDescription = rune.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = HextechCardBorder.copy(alpha = 0.6f), thickness = 0.8.dp)

        // SECCIÓN 1: OBJETOS CORE (Slots 1 al 5) Y BOTAS (Slot 6)
        if (build.coreItems.isNotEmpty() || build.boots != null) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr("OBJETOS CORE (1 al 5) & OBJETO 6 (BOTAS):"),
                        color = HextechGold,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val core5 = build.coreItems.take(5)
                    items(core5) { item ->
                        BuildItemSlot(
                            item = item,
                            badgeText = "${core5.indexOf(item) + 1}",
                            onClick = { onItemClick(item) }
                        )
                    }
                    build.boots?.let { boot ->
                        item {
                            BuildItemSlot(
                                item = boot,
                                badgeText = "6 ",
                                badgeColor = HextechGold,
                                onClick = { onItemClick(boot) }
                            )
                        }
                    }
                }
            }
        }

        // SECCIÓN 2: OBJETOS SITUACIONALES (Slots 7 y 8)
        if (build.situationalItems.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = tr("OBJETOS SITUACIONALES (7 y 8):"),
                        color = HextechCyan,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tr("(Adaptación estándar)"),
                        color = TextMuted,
                        fontSize = 9.5.sp
                    )
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(build.situationalItems) { item ->
                        val slotNum = 7 + build.situationalItems.indexOf(item)
                        BuildItemSlot(
                            item = item,
                            badgeText = "$slotNum",
                            badgeColor = HextechCyan,
                            onClick = { onItemClick(item) }
                        )
                    }
                }
            }
        }

        // SECCIÓN 3: ALTERNATIVAS SITUACIONALES (Vs Composición Rival)
        if (build.altSituationalItems.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = tr("ALTERNATIVAS SITUACIONALES:"),
                        color = Color(0xFFFFB74D),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tr("(Vs composición enemiga)"),
                        color = TextMuted,
                        fontSize = 9.5.sp
                    )
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(build.altSituationalItems) { item ->
                        BuildItemSlot(
                            item = item,
                            badgeText = "Alt",
                            badgeColor = Color(0xFFFFB74D),
                            onClick = { onItemClick(item) }
                        )
                    }
                }
            }
        }

        // SECCIÓN 4: BOTAS DETALLE + RUNAS SECUNDARIAS + HECHIZOS
        if (build.boots != null || build.secondaryRunes.isNotEmpty() || build.spells.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(HextechSurfaceVariant.copy(alpha = 0.5f))
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botas
                build.boots?.let { boot ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        BuildItemSlot(
                            item = boot,
                            badgeText = "",
                            size = 32.dp,
                            onClick = { onItemClick(boot) }
                        )
                        Column {
                            Text(
                                text = tr("Botas (Slot 6)"),
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                            Text(
                                text = boot.name,
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Runas Secundarias
                if (build.secondaryRunes.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (rune in build.secondaryRunes) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .border(0.8.dp, HextechGold.copy(alpha = 0.6f), CircleShape)
                                    .background(Color.Black)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(rune.iconUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)
                                        
                                        .build(),
                                    contentDescription = rune.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }

        // SECCIÓN 5: JUSTIFICACIÓN TÁCTICA / MATCHUPS
        if (!build.tacticalNotes.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(HextechSurfaceVariant.copy(alpha = 0.6f))
                    .border(0.8.dp, HextechGold.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = HextechGold, modifier = Modifier.size(12.dp))
                        Text(
                            text = tr("Justificación Táctica / Matchups:"),
                            color = HextechGold,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = build.tacticalNotes,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun BuildItemSlot(
    item: WildRiftItem,
    badgeText: String,
    badgeColor: Color = HextechGold,
    size: Dp = 38.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
            .background(HextechSurface)
            .border(1.dp, HextechCardBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.iconUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)
                
                .build(),
            contentDescription = item.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Badge de posición o tipo en esquina
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(topStart = 4.dp))
                .padding(horizontal = 3.dp, vertical = 1.dp)
        ) {
            Text(
                text = badgeText,
                color = badgeColor,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==========================================
// DIÁLOGO DE DETALLE DE OBJETO EN ADMIN
// ==========================================

@Composable
private fun AdminItemDetailDialog(
    item: WildRiftItem,
    onDismiss: () -> Unit
) {
    val statsList = remember(item) {
        if (item.stats.isNotBlank()) {
            item.stats.split(Regex("[•\n]")).map { it.trim() }.filter { it.isNotBlank() }
        } else emptyList()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.5.dp, HextechGold, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = HextechDarkBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header con Icono + Nombre + Costo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.5.dp, HextechGold, RoundedCornerShape(10.dp))
                            .background(HextechSurface)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(item.iconUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)
                                
                                .build(),
                            contentDescription = item.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            color = HextechGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = " ${item.goldCost} oro",
                                color = Color(0xFFFFD54F),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (item.category.isNotBlank()) {
                                Text(
                                    text = "• ${item.category}",
                                    color = HextechCyan,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(28.dp)
                            .background(HextechSurfaceVariant.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextPrimary, modifier = Modifier.size(16.dp))
                    }
                }

                HorizontalDivider(color = HextechCardBorder, thickness = 1.dp)

                // Stats del objeto
                if (statsList.isNotEmpty()) {
                    Text(
                        text = tr("Estadísticas:"),
                        color = HextechGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(HextechSurface.copy(alpha = 0.6f))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        statsList.forEach { stat ->
                            Text(
                                text = "• $stat",
                                color = TextPrimary,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }

                // Pasivas y Coach Tips con formato Wild Rift
                if (item.passive.isNotBlank() || item.coachTip.isNotBlank()) {
                    Text(
                        text = tr("Efectos y Consejos:"),
                        color = HextechCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(HextechSurface.copy(alpha = 0.6f))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (item.passive.isNotBlank()) {
                            FormattedWildRiftText(
                                text = item.passive,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                        if (item.coachTip.isNotBlank()) {
                            FormattedWildRiftText(
                                text = " ${item.coachTip}",
                                fontSize = 10.5.sp,
                                lineHeight = 14.5.sp
                            )
                        }
                    }
                }

                // Botón Entendido / Cerrar
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = tr("Entendido"),
                        color = HextechDarkBg,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

