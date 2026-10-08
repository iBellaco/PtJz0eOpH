package com.example.ui.components

import com.example.ui.components.CoachTab as Tab
import com.example.ui.components.CoachButton as Button
import com.example.ui.components.CoachOutlinedButton as OutlinedButton
import com.example.ui.components.CoachTextButton as TextButton
import com.example.ui.components.CoachIconButton as IconButton
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import com.example.ui.components.coachClickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.example.data.SupportReplyManager
import com.example.ui.components.SupportReplyDialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.model.FeedbackReport
import com.example.data.FeedbackRepository
import com.example.model.WildRiftItem
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechGreen
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.tr
import kotlinx.coroutines.launch
import java.util.Locale

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
        rawType in listOf("PAGO", "PAYMENT", "PAGAMENTO") -> "PAGO"
        com.example.data.SupportConversationPolicy.isSponsor(rawType) -> "PATROCINADOR"
        rawType in listOf("BUG", "ERROR", "BUG_REPORT", "BUG / ERROR") -> "BUG"
        rawType in listOf("BUILD_SUGGESTION", "BUILD", "SUGERIR BUILD", "SUGERENCIA DE BUILD") || parseBuildSuggestionFromText(desc, title) != null -> "BUILD"
        rawType in listOf("SOPORTE", "SUPPORT", "TICKET", "AYUDA", "REPORTE", "REPORT") -> "SUPPORT"
        else -> "SUGGESTION"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFeedbackBottomSheet(
    onDismiss: () -> Unit
) {
    val userRole by com.example.util.SubscriptionManager.userRole.collectAsState()
    val secondaryRole by com.example.util.SubscriptionManager.secondaryRole.collectAsState()

    // Explicit UI navigation logic verification: permitido para admin y moderador
    if (userRole != "admin" && userRole != "moderador" && secondaryRole != "moderador" && !com.example.util.AuthManager.isCurrentUserAdmin()) {
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

    fun loadReports(silent: Boolean = false) {
        if (!silent) {
            isLoading = true
            errorMessage = null
        }
        scope.launch {
            if (!silent) {
                // Auto-purga de 30 días para reportes leídos/solucionados y 60 días para pendientes
                try {
                    SupportReplyManager.autoPurgeAllExpired(context)
                } catch (_: Exception) {}
            }

            val result = FeedbackRepository.getAllFeedbacks()
            if (!silent) isLoading = false
            if (result.isSuccess) {
                val list = result.getOrDefault(emptyList())
                reports = list
                refreshStatusMap(list)
            } else if (!silent) {
                errorMessage = result.exceptionOrNull()?.message ?: "Error al cargar reportes"
            }
        }
    }

    DisposableEffect(userRole, secondaryRole) {
        var registration: com.google.firebase.firestore.ListenerRegistration? = null
        val job = scope.launch {
            try {
                com.example.data.SupportTicketAccess.migrateLegacyVisibility()
                registration = com.example.data.SupportTicketAccess.staffQuery().addSnapshotListener { snapshot, error ->
                    isLoading = false
                    if (error != null) {
                        errorMessage = com.example.util.appTr("No se pudieron cargar los mensajes. Comprueba los permisos y vuelve a intentarlo.")
                    } else if (snapshot != null) {
                        errorMessage = null
                        reports = FeedbackRepository.feedbacksFromSnapshot(snapshot)
                        refreshStatusMap(reports)
                    }
                }
            } catch (error: Exception) {
                isLoading = false
                errorMessage = com.example.util.appTr("No se pudieron cargar los mensajes. Comprueba los permisos y vuelve a intentarlo.")
            }
        }
        onDispose { job.cancel(); registration?.remove() }
    }

    val isAdmin = userRole == "admin" || com.example.util.AuthManager.isCurrentUserAdmin()

    // Reportes visibles según el rol del usuario (Los reportes de Patrocinador son exclusivos del Administrador)
    val visibleReports = remember(reports, isAdmin) {
        if (isAdmin) reports else reports.filter { getFeedbackCategory(it) !in setOf("PATROCINADOR", "PAGO") }
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
            PanelReadControl(com.example.data.NotificationPanel.SUPPORT)
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
                                        text = com.example.util.tr("$totalCount"),
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
                                    text = com.example.util.tr("${tr(tab.titleKey)} ($count)"),
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

                // Solo el administrador puede eliminar mensajes.
                if (isAdmin) OutlinedButton(
                    onClick = {
                        if (!isPurging) {
                            isPurging = true
                            scope.launch {
                                val res = FeedbackRepository.purgeOldReports(days = 7)
                                isPurging = false
                                if (res.isSuccess) {
                                    Toast.makeText(context, com.example.util.appTr(" Purga de reportes >7 días completada"), Toast.LENGTH_SHORT).show()
                                    loadReports()
                                } else {
                                    Toast.makeText(context, com.example.util.appTr("Error al purgar: ${res.exceptionOrNull()?.message}"), Toast.LENGTH_SHORT).show()
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
                if (isAdmin) OutlinedButton(
                    onClick = { showClearAllConfirm = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = HextechSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(46.dp)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = tr("Eliminar solucionados"), tint = DangerRed, modifier = Modifier.size(18.dp))
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
                        Text(text = com.example.util.tr(errorMessage!!), color = TextMuted, fontSize = 12.sp)
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
                            text = com.example.util.tr(if (searchQuery.isNotBlank() || selectedSubFilter != "ALL" || currentCategoryTab != FeedbackCategoryTab.ALL)
                                tr("No hay resultados en esta vista")
                            else
                                tr("No hay reportes ni sugerencias registradas")),
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
                                    scope.launch {
                                        val ok = SupportReplyManager.updateReportStatus(context, report.id ?: key, newStatus)
                                        if (ok) statusMap[key] = newStatus
                                        Toast.makeText(context, com.example.util.appTr(if (ok) "Estado actualizado" else "No se pudo sincronizar el estado. Inténtalo de nuevo."), Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onReply = { reportToReply = report },
                                onDelete = if (isAdmin) ({ reportToDelete = report }) else null,
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
                                    Toast.makeText(context, com.example.util.appTr(" Reporte copiado al portapapeles"), Toast.LENGTH_SHORT).show()
                                },
                                onOpenImage = { bmp -> previewImageBitmap = bmp },
                                onItemClick = {}
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
            userId = rep.userId,
            initialReply = "",
            tag = if (rep.type.uppercase() in setOf("PAGO", "PAYMENT", "PAGAMENTO")) "PAGO" else if (isSponsorItem) "PATROCINADOR" else "SOPORTE",
            isFirestoreDoc = false,
            onDismiss = { reportToReply = null },
            onReplySent = { newReply, markedAsRead ->
                if (markedAsRead) {
                    statusMap[repId] = FeedbackRepository.STATUS_READ
                    // The committed conversation and live listener supply the shared status.
                }
                loadReports()
            }
        )
    }

    // Diálogo de Confirmación de Eliminación Individual
    if (isAdmin && reportToDelete != null) {
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
                        text = com.example.util.tr("\"${rep.title}\""),
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
                                    Toast.makeText(context, com.example.util.appTr("Elemento eliminado"), Toast.LENGTH_SHORT).show()
                                    reportToDelete = null
                                    loadReports()
                                } else {
                                    Toast.makeText(context, com.example.util.appTr("Error: ${res.exceptionOrNull()?.message}"), Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, com.example.util.appTr("Eliminado localmente"), Toast.LENGTH_SHORT).show()
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
    if (isAdmin && showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showClearAllConfirm = false },
            containerColor = HextechDarkBg,
            shape = RoundedCornerShape(14.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = DangerRed, modifier = Modifier.size(24.dp))
                    Text(tr("¿Eliminar todos los mensajes solucionados?"), color = DangerRed, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = tr("Se eliminarán los mensajes solucionados. Los mensajes pendientes y leídos se conservarán."),
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleting = true
                        scope.launch {
                            val res = FeedbackRepository.clearSolvedFeedbacks()
                            isDeleting = false
                            showClearAllConfirm = false
                            if (res.isSuccess) {
                                Toast.makeText(context, com.example.util.appTr("Los mensajes solucionados fueron eliminados"), Toast.LENGTH_SHORT).show()
                                loadReports()
                            } else {
                                Toast.makeText(context, com.example.util.appTr("Error: ${res.exceptionOrNull()?.message}"), Toast.LENGTH_SHORT).show()
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
                        Text(tr("Sí, eliminar solucionados"), color = Color.White, fontWeight = FontWeight.Bold)
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
                    .coachClickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        showControls = !showControls
                    }
            ) {
                // Imagen con zoom
                Image(
                    bitmap = previewImageBitmap!!.asImageBitmap(),
                    contentDescription = com.example.util.tr("Vista previa ampliable"),
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
                                text = com.example.util.tr(if (scale > 1.05f) "${tr("Captura")} (${(scale * 100).toInt()}%)" else tr("Captura Adjunta")),
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
                                Icon(Icons.Default.ZoomOut, contentDescription = com.example.util.trNullable("Alejar"), tint = if (scale > 1f) HextechCyan else TextMuted, modifier = Modifier.size(22.dp))
                            }

                            // Zoom In
                            IconButton(
                                onClick = {
                                    scale = (scale * 1.3f).coerceIn(1f, 5f)
                                },
                                modifier = Modifier.size(36.dp),
                                enabled = scale < 5f
                            ) {
                                Icon(Icons.Default.ZoomIn, contentDescription = com.example.util.trNullable("Acercar"), tint = if (scale < 5f) HextechCyan else TextMuted, modifier = Modifier.size(22.dp))
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
                                    Icon(Icons.Default.RestartAlt, contentDescription = com.example.util.trNullable("Restablecer"), tint = HextechGold, modifier = Modifier.size(22.dp))
                                }
                            }

                            // Descargar Imagen
                            IconButton(
                                onClick = {
                                    saveBitmapToGallery(context, previewImageBitmap!!)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = com.example.util.trNullable("Descargar"), tint = HextechGreen, modifier = Modifier.size(22.dp))
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

    // Fin del panel de administración
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
            .coachClickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = com.example.util.tr(label),
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
                    text = com.example.util.tr(count.toString()),
                    color = if (isSelected) color else TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
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
                Toast.makeText(context, com.example.util.appTr(" Captura guardada en Galería (Imágenes)"), Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, com.example.util.appTr(" Captura guardada en Galería (Imágenes)"), Toast.LENGTH_SHORT).show()
            return
        }
    } catch (e: Exception) {
        Toast.makeText(context, com.example.util.appTr("Error al guardar imagen: ${e.localizedMessage ?: "Error desconocido"}"), Toast.LENGTH_SHORT).show()
    }
}
