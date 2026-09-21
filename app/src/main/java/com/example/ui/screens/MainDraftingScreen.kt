package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import java.util.Locale
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import com.example.ui.components.PrivacyPolicyDialog
import com.example.ui.theme.DangerRed



import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable

import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.example.ui.components.UserAvatarView
import com.example.util.SubscriptionManager
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp

import com.example.util.tr
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.ui.theme.TierAColor
import com.example.model.LaneRole
import com.example.ui.components.BugReportFeedbackDialog
import com.example.ui.components.AdminFeedbackBottomSheet
import com.example.ui.components.HextechOrbButton
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.isLightAppTheme
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechGoldLight
import android.content.Intent
import android.net.Uri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.TextMuted

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Favorite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.SystemPermissionHelper
import android.media.projection.MediaProjectionManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.service.screen.ScreenCaptureManager
import android.content.Context

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun MainDraftingScreen(
    onNavigateToInfo: () -> Unit,
    onNavigateToFAQ: () -> Unit,
    onNavigateToMeta: () -> Unit,
    onNavigateToLogin: () -> Unit,
    mainRole: LaneRole,
    onMainRoleChange: (LaneRole) -> Unit,
    secondRole: LaneRole,
    onSecondRoleChange: (LaneRole) -> Unit,
    autofillRole: LaneRole,
    onAutofillRoleChange: (LaneRole) -> Unit,
    currentLanguage: String = "es",
    onLanguageChange: (String) -> Unit = {}
) {
    val context = LocalContext.current
    
    val lifecycleOwner = LocalLifecycleOwner.current
    var isAssistantActive by remember { mutableStateOf(SystemPermissionHelper.isServiceRunning(context)) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showBugReportDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPlansDialog by remember { mutableStateOf(false) }
    val currentAvatarId by SubscriptionManager.currentAvatarId.collectAsState()
    val currentRankBorder by SubscriptionManager.currentRankBorder.collectAsState()

    // Sincronizar estado del servicio cuando la app pasa a primer plano
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isAssistantActive = SystemPermissionHelper.isServiceRunning(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val mediaProjectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            ScreenCaptureManager.pendingMediaProjectionResultCode = result.resultCode
            ScreenCaptureManager.pendingMediaProjectionData = result.data
            SystemPermissionHelper.startFloatingService(context)
            isAssistantActive = true
        } else {
            // Permiso de captura denegado
            isAssistantActive = false
        }
    }

    LaunchedEffect(Unit) {
        val activity = context as? Activity
        if (activity?.intent?.getBooleanExtra("EXTRA_REQUEST_CAPTURE", false) == true) {
            activity.intent.removeExtra("EXTRA_REQUEST_CAPTURE")
            val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjectionLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                val activity = context as? Activity
                if (activity?.intent?.getBooleanExtra("EXTRA_REQUEST_CAPTURE", false) == true) {
                    activity.intent.removeExtra("EXTRA_REQUEST_CAPTURE")
                    val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    mediaProjectionLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val toggleAssistant: () -> Unit = {
        com.example.ui.components.NoticeMediaUtils.pauseAndMuteAll()
        if (isAssistantActive) {
            SystemPermissionHelper.stopFloatingService(context)
            isAssistantActive = false
        } else {
            if (!SystemPermissionHelper.hasOverlayPermission(context)) {
                showPermissionDialog = true
            } else {
                val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                mediaProjectionLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
            }
        }
    }

    // Si hay un diálogo o modal abierto en la pantalla de inicio, el botón atrás lo cierra primero
    BackHandler(enabled = showPermissionDialog || showBugReportDialog) {
        if (showPermissionDialog) showPermissionDialog = false
        if (showBugReportDialog) showBugReportDialog = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "« Coach »",
                                color = TextPrimary,
                                fontSize = 17.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.testTag("app_title_centered")
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(HextechCyan)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = tr(com.example.data.WildRiftRepository.CURRENT_PATCH_VERSION),
                                    color = HextechCyan,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.2.sp
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            IconButton(
                                onClick = onNavigateToInfo,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(HextechSurface)
                                    .border(1.dp, HextechGold.copy(alpha = 0.6f), CircleShape)
                                    .size(38.dp)
                                    .testTag("nav_info_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = tr("Información"),
                                    tint = HextechGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = { showThemeDialog = true },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(HextechSurface)
                                    .border(1.dp, HextechGold.copy(alpha = 0.6f), CircleShape)
                                    .size(38.dp)
                                    .testTag("nav_theme_button")
                            ) {
                                Text("🎨", fontSize = 18.sp)
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showBugReportDialog = true },
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .clip(CircleShape)
                                .background(HextechSurface)
                                .border(1.dp, HextechGold.copy(alpha = 0.6f), CircleShape)
                                .size(38.dp)
                                .testTag("nav_bug_report_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = "Reportar Bugs o Sugerencias",
                                tint = HextechGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // User Avatar Profile button
                        val authUser = com.example.util.AuthManager.getAuth()?.currentUser
                        if (authUser != null && !com.example.util.AuthManager.isGuestOrUnauthenticated(authUser)) {
                            val isCurrentUserAdmin = com.example.util.AuthManager.isCurrentUserAdmin()
                            IconButton(
                                onClick = onNavigateToLogin,
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(38.dp)
                                    .testTag("nav_profile_avatar_button")
                            ) {
                                UserAvatarView(
                                    avatarId = currentAvatarId,
                                    rankBorder = currentRankBorder,
                                    size = if (isCurrentUserAdmin) 26.dp else 36.dp,
                                    fallbackInitial = authUser.displayName ?: authUser.email ?: "U",
                                    isAdmin = isCurrentUserAdmin
                                )
                            }
                        }


                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )
            },
        ) { innerPadding ->
            val notices by com.example.data.AppNoticeManager.notices.collectAsState()
            val globalAnnouncement by com.example.data.GlobalAnnouncementManager.currentAnnouncement.collectAsState()
            var showGlobalAnnouncementModal by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                // Banner de comunicado / alerta global activa en tiempo real
                if (globalAnnouncement != null && globalAnnouncement!!.active) {
                    com.example.ui.components.GlobalAnnouncementBanner(
                        announcement = globalAnnouncement!!,
                        onClick = { showGlobalAnnouncementModal = true },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                if (showGlobalAnnouncementModal && globalAnnouncement != null) {
                    com.example.ui.components.GlobalAnnouncementDialog(
                        announcement = globalAnnouncement!!,
                        onDismiss = { showGlobalAnnouncementModal = false }
                    )
                }

                // 1. Paneles de Avisos separados por categoría (Importantes, Ofertas, Publicidad, etc.)
                // Cada tipo de anuncio tiene su propio panel independiente; si hay varios del mismo tipo se agrupan.
                val now = System.currentTimeMillis()
                val activeNotices = notices.filter { notice ->
                    val isNotExpired = notice.expiresAtMillis == 0L || notice.expiresAtMillis > now
                    notice.isEnabled && notice.isApproved && isNotExpired && (notice.content.isNotBlank() || notice.title.isNotBlank())
                }

                // Sincronizar anuncios desde la nube al cargar la pantalla
                LaunchedEffect(Unit) {
                    com.example.data.AppNoticeManager.syncFromCloud(context)
                    com.example.data.AppNoticeManager.syncPendingSponsors(context)
                    com.example.data.GlobalAnnouncementManager.refreshFromCloud(context)
                }

                val streamerIntervalValue by com.example.data.AppNoticeManager.streamerIntervalValue.collectAsState()
                val streamerIntervalUnit by com.example.data.AppNoticeManager.streamerIntervalUnit.collectAsState()

                val intervalMillis = remember(streamerIntervalValue, streamerIntervalUnit) {
                    val value = streamerIntervalValue.coerceAtLeast(1)
                    when (streamerIntervalUnit) {
                        "minutes" -> value * 60 * 1000L
                        "hours" -> value * 60 * 60 * 1000L
                        else -> value * 1000L
                    }
                }

                val groupedNotices = remember(activeNotices) {
                    val orderPriority = listOf("Anuncios importantes", "PUBLICIDAD", "Ofertas", "Mantenimiento", "Noticias", "Streamers")
                    val map = activeNotices.groupBy { normalizeNoticeTag(it.tag) }.toMutableMap()
                    if (!map.containsKey("PUBLICIDAD")) {
                        map["PUBLICIDAD"] = emptyList()
                    }
                    map.toList()
                        .sortedBy { (cat, _) ->
                            val idx = orderPriority.indexOf(cat)
                            if (idx >= 0) idx else 99
                        }
                }

                groupedNotices.forEach { (catTag, noticeList) ->
                    NoticeCategoryCard(
                        categoryTag = catTag,
                        noticeList = noticeList,
                        intervalMillis = intervalMillis,
                        context = context
                    )
                }


                
                val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                var isIgnoringBatteryOpt by remember { mutableStateOf(SystemPermissionHelper.isIgnoringBatteryOptimizations(context)) }
                var hasOverlayPermission by remember { mutableStateOf(SystemPermissionHelper.hasOverlayPermission(context)) }
                var hasStoragePermission by remember { mutableStateOf(SystemPermissionHelper.hasStoragePermission(context)) }
                var areNotificationsEnabled by remember {
                    mutableStateOf(
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
                            androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
                        } else {
                            androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
                        }
                    )
                }
                
                val requestStoragePermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    hasStoragePermission = SystemPermissionHelper.hasStoragePermission(context)
                }

                DisposableEffect(lifecycleOwner) {
                    val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                        if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                            isIgnoringBatteryOpt = SystemPermissionHelper.isIgnoringBatteryOptimizations(context)
                            hasOverlayPermission = SystemPermissionHelper.hasOverlayPermission(context)
                            hasStoragePermission = SystemPermissionHelper.hasStoragePermission(context)
                            areNotificationsEnabled = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
                                androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
                            } else {
                                androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
                            }
                            com.example.data.AppNoticeManager.syncFromCloud(context)
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                val allOptimizationsGranted = hasOverlayPermission && isIgnoringBatteryOpt

                // Card de Rendimiento en Segundo Plano (Se oculta automáticamente si todo está activo)
                if (!allOptimizationsGranted) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, HextechGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = HextechSurface.copy(alpha = 0.95f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = HextechGold, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = tr("Rendimiento de Segundo Plano"),
                                            color = HextechGold,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = tr("Configuración necesaria para que el asistente no se cierre"),
                                            color = TextMuted,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 1. Superposición
                            if (!hasOverlayPermission) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = tr("Ventana Flotante (Overlay)"),
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = tr("Requerido para mostrar recomendaciones sobre el juego"),
                                            color = DangerRed,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { SystemPermissionHelper.openOverlaySettings(context) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HextechCyan),
                                        border = BorderStroke(1.dp, HextechCyan),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text(tr("Activar"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // 2. Batería / Segundo plano
                            if (!isIgnoringBatteryOpt) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = tr("Ahorro de Batería"),
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = tr("Pon en 'Sin Restricciones' para no cerrarse"),
                                            color = TierAColor,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { SystemPermissionHelper.requestIgnoreBatteryOptimization(context) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HextechCyan),
                                        border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.8f)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text(tr("Ajustes"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (!areNotificationsEnabled) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = HextechGold.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = HextechGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Recomendado: Activar Notificaciones",
                                    color = HextechGold,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Activa las notificaciones para recibir avisos y alertas en tiempo real.",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        try {
                                            val generalIntent = android.content.Intent(android.provider.Settings.ACTION_SETTINGS)
                                            context.startActivity(generalIntent)
                                        } catch (_: Exception) {}
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Activar", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Activar Botón y descripción abajo de las recomendaciones
                HextechOrbButton(
                    isActive = isAssistantActive,
                    onToggle = toggleAssistant,
                    enabled = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isAssistantActive) tr("Asistente Hextech Activo • Toca la cámara flotante")
                           else tr("Presiona ACTIVAR para iniciar el Asistente Flotante"),
                    color = if (isAssistantActive) HextechCyan else TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                    // Derechos de autor y créditos
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(HextechSurface.copy(alpha = 0.5f))
                            .border(1.dp, HextechCardBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "© 2026 Diego Barba Chavez",
                            color = HextechGoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tr("Desarrollador Principal • Todos los derechos reservados"),
                            color = TextMuted,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Alfa v${com.example.BuildConfig.VERSION_NAME} (${com.example.BuildConfig.VERSION_CODE})",
                            color = TextMuted.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

        // Diálogo para conceder el permiso de superposición (Aparecer sobre otras apps)
        if (showPermissionDialog) {
            AlertDialog(
                onDismissRequest = { showPermissionDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Layers, contentDescription = null, tint = HextechGold, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(tr("Permiso de Superposición"), color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = tr("Para que el asistente inteligente funcione en segundo plano sobre Wild Rift, Android requiere habilitar 'Aparecer encima' (Superposición)."),
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = tr("PermisoSuperposicionTexto"),
                            color = HextechCyan,
                            fontSize = 12.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showPermissionDialog = false
                            SystemPermissionHelper.openOverlaySettings(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HextechGold)
                    ) {
                        Text(tr("Conceder Permiso"), color = HextechDarkBg, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionDialog = false }) {
                        Text(tr("Cancelar"), color = TextMuted)
                    }
                },
                containerColor = HextechSurface,
                shape = RoundedCornerShape(16.dp)
            )
        }

        if (showBugReportDialog) {
            BugReportFeedbackDialog(
                onDismiss = { showBugReportDialog = false }
            )
        }



        if (showThemeDialog) {
            com.example.ui.components.ThemeCustomizationBottomSheet(
                isPremium = com.example.util.SubscriptionManager.isPremium.collectAsState().value,
                onOpenPremiumPlans = {
                    showThemeDialog = false
                    showPlansDialog = true
                },
                onDismiss = { showThemeDialog = false }
            )
        }

        if (showPlansDialog) {
            com.example.ui.components.SubscriptionPlansBottomSheet(
                onDismiss = { showPlansDialog = false }
            )
        }


    }
}

fun normalizeNoticeTag(tag: String): String {
    val clean = tag.trim()
    val lower = clean.lowercase(Locale.ROOT)
    return when {
        lower.contains("importante") || lower.contains("aviso") -> "Anuncios importantes"
        lower.contains("oferta") || lower.contains("descuento") -> "Ofertas"
        lower.contains("publicidad") || lower.contains("ads") || lower.contains("promo") -> "PUBLICIDAD"
        lower.contains("mantenimiento") -> "Mantenimiento"
        lower.contains("noticia") -> "Noticias"
        lower.contains("streamer") -> "Streamers"
        clean.isNotBlank() -> clean
        else -> "Anuncios importantes"
    }
}

fun getNoticeTagColor(tag: String): Color {
    val l = tag.lowercase(Locale.ROOT)
    return when {
        l.contains("importante") || l.contains("aviso") -> HextechGold
        l.contains("publicidad") || l.contains("ads") || l.contains("promo") -> Color(0xFF00FF66)
        l.contains("oferta") || l.contains("descuento") -> HextechCyan
        l.contains("mantenimiento") -> Color(0xFFFF3333)
        l.contains("noticia") -> Color(0xFFCC66FF)
        l.contains("streamer") -> Color(0xFFFF66CC)
        else -> HextechCyan
    }
}

fun getNoticeTagIcon(tag: String): androidx.compose.ui.graphics.vector.ImageVector {
    val l = tag.lowercase(Locale.ROOT)
    return when {
        l.contains("importante") || l.contains("aviso") -> Icons.Default.Campaign
        l.contains("oferta") || l.contains("descuento") -> Icons.Default.LocalOffer
        l.contains("publicidad") || l.contains("ads") || l.contains("promo") -> Icons.Default.Storefront
        l.contains("mantenimiento") -> Icons.Default.Build
        l.contains("noticia") -> Icons.Default.Article
        l.contains("streamer") -> Icons.Default.LiveTv
        else -> Icons.Default.Label
    }
}

@Composable
fun NoticeCategoryCard(
    categoryTag: String,
    noticeList: List<com.example.data.AppNotice>,
    intervalMillis: Long,
    context: android.content.Context
) {
    val isPublicidadCategory = categoryTag.equals("PUBLICIDAD", true) || categoryTag.equals("Publicidad", true)

    var areNotificationsEnabled by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
                androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
            } else {
                androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        )
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                areNotificationsEnabled = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
                    androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
                } else {
                    androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (noticeList.isEmpty() && (!isPublicidadCategory || areNotificationsEnabled)) return

    var currentIndex by remember(noticeList.size) { mutableStateOf(0) }
    var isPinned by remember { mutableStateOf(false) }
    var isFullscreenMedia by remember { mutableStateOf(false) }
    var slideDirection by remember { mutableStateOf(1) }
    var autoTimerTrigger by remember { mutableStateOf(0) }

    val safeIndex = currentIndex.coerceIn(0, (noticeList.size - 1).coerceAtLeast(0))
    val currentNotice = if (noticeList.isNotEmpty()) noticeList[safeIndex] else null

    val tagColor = getNoticeTagColor(categoryTag)
    val tagIcon = getNoticeTagIcon(categoryTag)
    val isSponsored = currentNotice != null && (currentNotice.sponsorEmail.isNotBlank() || currentNotice.tag.equals("PUBLICIDAD", true) || currentNotice.tag.equals("Publicidad", true) || categoryTag.equals("PUBLICIDAD", true) || categoryTag.equals("Publicidad", true))
    val displayTag = if (isPublicidadCategory) "Publicidad" else categoryTag

    // Registro de impresiones analíticas
    LaunchedEffect(currentNotice?.id) {
        if (currentNotice != null) {
            com.example.data.AppNoticeAnalyticsManager.recordImpression(context, currentNotice.id, currentNotice.tag)
        }
    }

    val rotationIntervalSec = remember(intervalMillis) { (intervalMillis / 1000L).coerceAtLeast(1L).toInt() }
    var remainingRotationSec by remember(currentNotice?.id, autoTimerTrigger, rotationIntervalSec) { mutableStateOf(rotationIntervalSec) }

    // Rotación automática activa únicamente si hay múltiples avisos en esta misma categoría con contador de rotación en tiempo real
    LaunchedEffect(currentNotice?.id, noticeList.size, intervalMillis, isPinned, isFullscreenMedia, autoTimerTrigger) {
        if (noticeList.size > 1 && !isPinned && !isFullscreenMedia) {
            remainingRotationSec = rotationIntervalSec
            while (remainingRotationSec > 0) {
                kotlinx.coroutines.delay(1000L)
                if (!isFullscreenMedia && !isPinned) {
                    remainingRotationSec--
                }
            }
            if (!isFullscreenMedia && !isPinned) {
                slideDirection = 1
                currentIndex = (currentIndex + 1) % noticeList.size
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.2.dp, tagColor.copy(alpha = 0.8f), RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = HextechSurface.copy(alpha = 0.95f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Encabezado del panel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = tagIcon,
                            contentDescription = null,
                            tint = tagColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = displayTag,
                            color = tagColor,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Etiqueta "Patrocinado" en la esquina derecha cuando el anuncio es de patrocinador
                        if (isSponsored) {
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = HextechGold.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, HextechGold)
                            ) {
                                Text(
                                    text = "Patrocinado",
                                    color = HextechGold,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Si hay varios avisos del mismo tipo, se muestra únicamente el icono de fijación
                        if (noticeList.size > 1) {
                            IconButton(
                                onClick = {
                                    isPinned = !isPinned
                                    if (isPinned) {
                                        Toast.makeText(context, "📌 Publicación fijada. No rotará automáticamente.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Rotación automática activada.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = if (isPinned) "Desfijar publicación" else "Fijar publicación",
                                    tint = if (isPinned) tagColor else tagColor.copy(alpha = 0.4f),
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }



                if (currentNotice != null) {
                    Spacer(modifier = Modifier.height(10.dp))

                    AnimatedContent(
                        targetState = currentNotice,
                        transitionSpec = {
                            if (slideDirection >= 0) {
                                (slideInHorizontally(animationSpec = tween(350)) { width -> width } + fadeIn(animationSpec = tween(350)))
                                    .togetherWith(slideOutHorizontally(animationSpec = tween(350)) { width -> -width } + fadeOut(animationSpec = tween(350)))
                            } else {
                                (slideInHorizontally(animationSpec = tween(350)) { width -> -width } + fadeIn(animationSpec = tween(350)))
                                    .togetherWith(slideOutHorizontally(animationSpec = tween(350)) { width -> width } + fadeOut(animationSpec = tween(350)))
                            }.using(SizeTransform(clip = false))
                        },
                        label = "NoticeAnimatedContent_${categoryTag}"
                    ) { noticeItem ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = noticeItem.title,
                                color = try { Color(android.graphics.Color.parseColor(noticeItem.titleColor)) } catch (_: Exception) { HextechGold },
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (noticeItem.content.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = noticeItem.content,
                                    color = try { Color(android.graphics.Color.parseColor(noticeItem.contentColor)) } catch (_: Exception) { TextSecondary },
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }

                            if (noticeItem.videoUrl.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                com.example.ui.components.NoticeMediaViewer(
                                    mediaUrl = noticeItem.videoUrl,
                                    externalUrl = noticeItem.externalUrl,
                                    modifier = Modifier.fillMaxWidth(),
                                    onExpand = {
                                        com.example.data.AppNoticeAnalyticsManager.recordFullscreen(context, currentNotice.id)
                                        isFullscreenMedia = true
                                    }
                                )
                            }
                        }
                    }

                    if (noticeList.size > 1) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            noticeList.indices.forEach { idx ->
                                val isSelected = idx == safeIndex
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp)
                                        .size(if (isSelected) 8.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) tagColor else tagColor.copy(alpha = 0.3f))
                                        .clickable {
                                            slideDirection = if (idx > currentIndex) 1 else -1
                                            currentIndex = idx
                                            autoTimerTrigger++
                                        }
                                )
                            }
                        }
                    }

                    if (isFullscreenMedia) {
                        val mediaToExpand = if (currentNotice.expandedImageUrl.isNotBlank()) currentNotice.expandedImageUrl else currentNotice.videoUrl
                        val isVertical = currentNotice.expandedImageUrl.isNotBlank() && mediaToExpand == currentNotice.expandedImageUrl
                        com.example.ui.components.NoticeMediaFullscreenDialog(
                            mediaUrl = mediaToExpand,
                            externalUrl = currentNotice.externalUrl,
                            noticeId = currentNotice.id,
                            isVertical = isVertical,
                            onDismiss = { isFullscreenMedia = false }
                        )
                    }
                }
            }
        }
    }
}
