package com.example.ui.components

import com.example.ui.components.CoachButton as Button
import com.example.ui.components.CoachOutlinedButton as OutlinedButton
import com.example.ui.components.CoachTextButton as TextButton
import com.example.ui.components.CoachIconButton as IconButton
import com.example.util.tr
import androidx.compose.foundation.BorderStroke
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.util.AuthManager
import kotlinx.coroutines.launch

internal val HextechSurfaceBg: Color get() = HextechSurface

@Composable
fun AnimatedAdminActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    border: BorderStroke? = null,
    shape: Shape = RoundedCornerShape(8.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && !com.example.ui.components.LocalCoachButtonAnimation.current) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "AdminBtnScale"
    )

    Button(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        interactionSource = interactionSource,
        colors = colors,
        border = border,
        shape = shape,
        contentPadding = contentPadding,
        enabled = enabled,
        content = content
    )
}

@Composable
fun AnimatedAdminOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    border: BorderStroke? = null,
    shape: Shape = RoundedCornerShape(8.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && !com.example.ui.components.LocalCoachButtonAnimation.current) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "AdminOutlinedBtnScale"
    )

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        interactionSource = interactionSource,
        colors = colors,
        border = border,
        shape = shape,
        contentPadding = contentPadding,
        enabled = enabled,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val userRole by com.example.util.SubscriptionManager.userRole.collectAsState()
    val isAdmin = userRole == "admin" || AuthManager.isCurrentUserAdmin()

    if (!isAdmin) {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }

    DisposableEffect(context) {
        com.example.data.sync.GlobalMetaSyncService.startLive(context, "admin_dashboard")
        onDispose { com.example.data.sync.GlobalMetaSyncService.stopLive("admin_dashboard") }
    }

    var showUsers by remember { mutableStateOf(false) }
    var showReportsPanel by remember { mutableStateOf(false) }
    var showSupportReportsPanel by remember { mutableStateOf(false) }
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var showNoticeConfigDialog by remember { mutableStateOf(false) }
    var showCpmAnalyticsDialog by remember { mutableStateOf(false) }
    var showCashRequests by remember { mutableStateOf(false) }
    var showDatabaseConsumptionDialog by remember { mutableStateOf(false) }
    var showModeratorActivityDialog by remember { mutableStateOf(false) }
    var showSponsorModerationDialog by remember { mutableStateOf(false) }
    var showSponsorPanelDialog by remember { mutableStateOf(false) }
    var showModeratorRequestsDialog by remember { mutableStateOf(false) }
    var showOverlayAccessDialog by remember { mutableStateOf(false) }
    val adminNotificationSummary = userPanelNotificationSummary()
    val pendingModeratorRequestsCount = adminNotificationSummary.count(com.example.data.NotificationPanel.ADMINISTRATION)
    val pendingSupportReportsCount = adminNotificationSummary.count(com.example.data.NotificationPanel.SUPPORT)
    val reviewScope = rememberCoroutineScope()
    var isMonitoringMinimized by remember { mutableStateOf(false) }

    val userRoleForRequests = com.example.util.SubscriptionManager.userRole.collectAsState().value
    val isAdminUserForRequests = userRoleForRequests == "admin" || com.example.util.AuthManager.isCurrentUserAdmin()

    if (showUsers) {
        Dialog(onDismissRequest = { showUsers = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(Modifier.fillMaxSize().safeDrawingPadding(), color = HextechDarkBg) {
                Column {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(tr("Gestión de usuarios"), Modifier.weight(1f), color = HextechGold, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showUsers = false }, modifier = Modifier.size(48.dp).testTag("close_user_directory")) {
                            Icon(Icons.Default.Close, contentDescription = tr("Cerrar"))
                        }
                    }
                    EnhancedUserManagementPanel(showMonitoring = false)
                }
            }
        }
    }

    // Sub-dialogs
    if (showModeratorRequestsDialog) {
        AdminModeratorRequestsDialog(onDismiss = { showModeratorRequestsDialog = false })
    }

    if (showReportsPanel) {
        AdminFeedbackBottomSheet(onDismiss = { showReportsPanel = false })
    }

    if (showSupportReportsPanel) {
        AdminSupportReportsDialog(onDismiss = { showSupportReportsPanel = false })
    }

    if (showBroadcastDialog) {
        AdminBroadcastAnnouncementDialog(onDismiss = { showBroadcastDialog = false })
    }

    if (showNoticeConfigDialog) {
        AdminNoticeConfigDialog(onDismiss = { showNoticeConfigDialog = false })
    }

    if (showCpmAnalyticsDialog) {
        AdminCpmAnalyticsDialog(onDismiss = { showCpmAnalyticsDialog = false })
    }

    if (showCashRequests) {
        Dialog(onDismissRequest = { showCashRequests = false }) {
            Surface(color = HextechDarkBg, shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth().heightIn(max = 650.dp).verticalScroll(rememberScrollState()).padding(16.dp)) {
                    PanelReadControl(com.example.data.NotificationPanel.ADMINISTRATION)
                    CashRedemptionReviewPanel()
                    TextButton(onClick = { showCashRequests = false }) { Text(tr("Cerrar")) }
                }
            }
        }
    }
    if (showDatabaseConsumptionDialog) {
        AdminDatabaseConsumptionDialog(onDismiss = { showDatabaseConsumptionDialog = false }, onOpenCashRequests = { showCashRequests = true })
    }
    if (showModeratorActivityDialog) AdminModeratorActivityDialog(onDismiss = { showModeratorActivityDialog = false })
    if (showOverlayAccessDialog) AdminOverlayAccessDialog(onDismiss = { showOverlayAccessDialog = false })

    if (showSponsorModerationDialog) {
        AdminSponsorModerationDialog(onDismiss = { showSponsorModerationDialog = false })
    }

    if (showSponsorPanelDialog) {
        SponsorCpmPanelDialog(onDismiss = { showSponsorPanelDialog = false })
    }

    Dialog(
        onDismissRequest = {
            when {
                showReportsPanel -> showReportsPanel = false
                showSupportReportsPanel -> showSupportReportsPanel = false
                showBroadcastDialog -> showBroadcastDialog = false
                showNoticeConfigDialog -> showNoticeConfigDialog = false
                showCpmAnalyticsDialog -> showCpmAnalyticsDialog = false
                showDatabaseConsumptionDialog -> showDatabaseConsumptionDialog = false
                showModeratorActivityDialog -> showModeratorActivityDialog = false
                showSponsorModerationDialog -> showSponsorModerationDialog = false
                showSponsorPanelDialog -> showSponsorPanelDialog = false
                showModeratorRequestsDialog -> showModeratorRequestsDialog = false
                else -> onDismiss()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false
        )
    ) {
        BackHandler(enabled = true) {
            when {
                showReportsPanel -> showReportsPanel = false
                showSupportReportsPanel -> showSupportReportsPanel = false
                showBroadcastDialog -> showBroadcastDialog = false
                showNoticeConfigDialog -> showNoticeConfigDialog = false
                showCpmAnalyticsDialog -> showCpmAnalyticsDialog = false
                showDatabaseConsumptionDialog -> showDatabaseConsumptionDialog = false
                showSponsorModerationDialog -> showSponsorModerationDialog = false
                showSponsorPanelDialog -> showSponsorPanelDialog = false
                showModeratorRequestsDialog -> showModeratorRequestsDialog = false
                else -> onDismiss()
            }
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = HextechDarkBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
            ) {
                // Header Premium Hextech
                AdminDashboardHeader(
                    onClose = onDismiss,
                    onOpenUsers = { showUsers = true },
                    onOpenFeedbackAndSupport = { showReportsPanel = true },
                    pendingSupportReportsCount = pendingSupportReportsCount,
                    onOpenModeratorRequests = { showModeratorRequestsDialog = true },
                    pendingModeratorRequestsCount = pendingModeratorRequestsCount,
                    onOpenBroadcast = { showBroadcastDialog = true },
                    onOpenNotice = { showNoticeConfigDialog = true },
                    onOpenCpmAnalytics = { showCpmAnalyticsDialog = true },
                    onOpenDatabaseConsumption = { showDatabaseConsumptionDialog = true },
                    onOpenModeratorActivity = { showModeratorActivityDialog = true },
                    onOpenSponsorPanel = { showSponsorPanelDialog = true },
                    onOpenOverlayAccess = { showOverlayAccessDialog = true },
                    isFullAdmin = userRole == "admin" || com.example.util.AuthManager.isCurrentUserAdmin()
                )

                // Panel principal de gestión
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    if (userRole == "admin" || userRole == "moderador" || com.example.util.AuthManager.isCurrentUserAdmin()) {
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            MetaMonitoringCard()
                            if (userRole == "admin" || com.example.util.AuthManager.isCurrentUserAdmin()) {
                                AppUpdateVerificationControl()
                            }
                            AdminUserPresenceCard()
                        }
                    } else {
                        // Moderador View
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.SupportAgent, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(tr("Panel de Moderación"), color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(tr("Abre 'Soporte' en la parte superior para moderar los aportes de la comunidad."), color = TextSecondary, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            }
        }
    }

}

@Composable
private fun AppUpdateVerificationControl() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val enabled by com.example.util.AppUpdateManager.verificationEnabled.collectAsState()
    val versionStatus by com.example.util.AppUpdateManager.checkStatus.collectAsState()
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        com.example.util.AppUpdateManager.refreshVerificationSetting()
        com.example.util.AppUpdateManager.checkForUpdates(context, forceManualCheck = true)
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        border = BorderStroke(1.dp, if (enabled) HextechCyan else TextMuted.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
          Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.SystemUpdate, contentDescription = null,
                tint = if (enabled) HextechCyan else TextMuted, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(tr("Verificación de actualización"), color = TextPrimary, fontWeight = FontWeight.Bold)
                Text(
                    tr(if (enabled) "La aplicación exige la versión publicada más reciente." else "La comprobación de versión está desactivada."),
                    color = TextSecondary, fontSize = 11.sp
                )
                if (enabled) Text(
                    tr("Versión disponible") + ": " + versionStatus.publishedName.ifBlank { "—" },
                    color = HextechCyan, fontSize = 11.sp,
                    modifier = Modifier.testTag("published_app_version")
                )
                error?.let { Text(tr(it), color = DangerRed, fontSize = 10.sp) }
            }
            Switch(
                checked = enabled,
                enabled = !busy,
                onCheckedChange = { value ->
                    busy = true
                    error = null
                    scope.launch {
                        runCatching { com.example.util.AppUpdateManager.setVerificationEnabled(context, value) }
                            .onFailure { error = "No se pudo guardar el cambio." }
                        busy = false
                    }
                },
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .testTag("toggle_app_update_verification")
            )
          }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AdminDashboardHeader(
    onClose: () -> Unit,
    onOpenUsers: () -> Unit,
    onOpenFeedbackAndSupport: () -> Unit,
    pendingSupportReportsCount: Int = 0,
    onOpenModeratorRequests: () -> Unit = {},
    pendingModeratorRequestsCount: Int = 0,
    onOpenBroadcast: () -> Unit,
    onOpenNotice: () -> Unit,
    onOpenCpmAnalytics: () -> Unit = {},
    onOpenDatabaseConsumption: () -> Unit = {},
    onOpenModeratorActivity: () -> Unit = {},
    onOpenSponsorPanel: () -> Unit = {},
    onOpenOverlayAccess: () -> Unit = {},
    isFullAdmin: Boolean = true
) {
    Surface(
        color = HextechSurfaceBg,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(HextechGold, Color(0xFF8B6B23)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = HextechDarkBg,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = tr("Panel de Administración"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HextechGold
                        )
                        Text(
                            text = tr("Control de Usuarios, Membresías y Slots"),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(48.dp).testTag("close_admin_dashboard")
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = com.example.util.trNullable("Cerrar"),
                        tint = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Wrap every action onto the next row. No horizontal scrolling or hidden actions.
            FlowRow(
                modifier = Modifier.fillMaxWidth().testTag("admin_action_grid"),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AnimatedAdminActionButton(onClick = onOpenUsers,
                    modifier = Modifier.heightIn(min = 48.dp).testTag("open_user_directory"),
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold.copy(alpha = 0.2f))) {
                    Icon(Icons.Default.ManageAccounts, null, tint = HextechGold, modifier = Modifier.size(16.dp))
                    Text(tr("Usuarios"), color = HextechGold, fontSize = 12.sp)
                }
                Box {
                AnimatedAdminActionButton(onClick = onOpenFeedbackAndSupport,
                    modifier = Modifier.heightIn(min = 48.dp).testTag("open_admin_support"),
                    colors = ButtonDefaults.buttonColors(containerColor = HextechCyan.copy(alpha = 0.2f))) {
                    Text(tr("Soporte"), color = HextechCyan, fontSize = 12.sp)
                }
                if (pendingSupportReportsCount > 0) {
                    Badge(containerColor = DangerRed, contentColor = Color.White, modifier = Modifier.align(Alignment.TopEnd)) { Text(pendingSupportReportsCount.toString()) }
                }
                }
                if (isFullAdmin) AnimatedAdminActionButton(onClick = onOpenSponsorPanel,
                    modifier = Modifier.heightIn(min = 48.dp).testTag("open_admin_sponsor_panel"),
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold.copy(alpha = 0.2f))) {
                    Text(tr("Patrocinio"), color = HextechGold, fontSize = 12.sp)
                }
                // Botón Moderación (Peticiones de Moderadores con badge de notificación)
                Box {
                    AnimatedAdminActionButton(
                        onClick = onOpenModeratorRequests,
                        modifier = Modifier.heightIn(min = 48.dp).testTag("open_admin_moderation"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pendingModeratorRequestsCount > 0) HextechGold.copy(alpha = 0.28f) else HextechSurfaceVariant.copy(alpha = 0.6f)
                        ),
                        border = if (pendingModeratorRequestsCount > 0) BorderStroke(1.2.dp, HextechGold) else null,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.PendingActions, contentDescription = null, tint = HextechGold, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(tr("Moderación"), fontSize = 10.5.sp, color = HextechGold, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    if (pendingModeratorRequestsCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp)
                                .size(18.dp)
                                .background(DangerRed, CircleShape)
                                .border(1.2.dp, HextechDarkBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = com.example.util.tr(if (pendingModeratorRequestsCount > 99) "99+" else pendingModeratorRequestsCount.toString()),
                                color = Color.White,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                // Botón Broadcast
                AnimatedAdminActionButton(
                    onClick = onOpenBroadcast,
                        modifier = Modifier.heightIn(min = 48.dp).testTag("open_admin_broadcast"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFFC4B5FD), modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("Broadcast"), fontSize = 10.5.sp, color = Color(0xFFC4B5FD), fontWeight = FontWeight.Bold, maxLines = 1)
                }

                // Botón Avisos
                AnimatedAdminActionButton(
                    onClick = onOpenNotice,
                        modifier = Modifier.heightIn(min = 48.dp).testTag("open_admin_notice"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Announcement, contentDescription = null, tint = Color(0xFF2DD4BF), modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("Avisos"), fontSize = 10.5.sp, color = Color(0xFF2DD4BF), fontWeight = FontWeight.Bold, maxLines = 1)
                }

                // Botón CPM
                AnimatedAdminActionButton(
                    onClick = onOpenCpmAnalytics,
                        modifier = Modifier.heightIn(min = 48.dp).testTag("open_admin_cpm"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF00FF66), modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("CPM"), fontSize = 10.5.sp, color = Color(0xFF00FF66), fontWeight = FontWeight.Bold, maxLines = 1)
                }

                // Botón Base de Datos
                AnimatedAdminActionButton(
                    onClick = onOpenDatabaseConsumption,
                        modifier = Modifier.heightIn(min = 48.dp).testTag("open_admin_database"),
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = HextechGold, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("Base Datos"), fontSize = 10.5.sp, color = HextechGold, fontWeight = FontWeight.Bold, maxLines = 1)
                }
                AnimatedAdminActionButton(
                    onClick = onOpenModeratorActivity,
                    modifier = Modifier.heightIn(min = 48.dp).testTag("open_moderator_activity"),
                    colors = ButtonDefaults.buttonColors(containerColor = HextechCyan.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("Actividad"), fontSize = 10.5.sp, color = HextechCyan, fontWeight = FontWeight.Bold)
                }
                AnimatedAdminActionButton(
                    onClick = onOpenOverlayAccess,
                    modifier = Modifier.heightIn(min = 48.dp).testTag("open_overlay_access"),
                    colors = ButtonDefaults.buttonColors(containerColor = HextechCyan.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(tr("Visor"), fontSize = 10.5.sp, color = HextechCyan, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
