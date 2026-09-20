package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import com.example.data.sync.BestBuildWrScraper

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AvatarCatalog
import com.example.model.AvatarItem
import com.example.model.AppUserRole
import com.example.ui.theme.*
import com.example.util.AuthManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextDecoration
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val HextechSurfaceBg: Color get() = HextechSurface

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
        targetValue = if (isPressed) 0.92f else 1f,
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
        targetValue = if (isPressed) 0.92f else 1f,
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

    var showReportsPanel by remember { mutableStateOf(false) }
    var showSupportReportsPanel by remember { mutableStateOf(false) }
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var showNoticeConfigDialog by remember { mutableStateOf(false) }
    var showCpmAnalyticsDialog by remember { mutableStateOf(false) }
    var showDatabaseConsumptionDialog by remember { mutableStateOf(false) }
    var showSponsorModerationDialog by remember { mutableStateOf(false) }
    var showSponsorPanelDialog by remember { mutableStateOf(false) }
    var isMonitoringMinimized by remember { mutableStateOf(false) }

    // Sub-dialogs
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

    if (showDatabaseConsumptionDialog) {
        AdminDatabaseConsumptionDialog(onDismiss = { showDatabaseConsumptionDialog = false })
    }

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
                showSponsorModerationDialog -> showSponsorModerationDialog = false
                showSponsorPanelDialog -> showSponsorPanelDialog = false
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
                    .systemBarsPadding()
            ) {
                // Header Premium Hextech
                AdminDashboardHeader(
                    onClose = onDismiss,
                    onOpenFeedbackAndSupport = { showReportsPanel = true },
                    onOpenBroadcast = { showBroadcastDialog = true },
                    onOpenNotice = { showNoticeConfigDialog = true },
                    onOpenCpmAnalytics = { showCpmAnalyticsDialog = true },
                    onOpenDatabaseConsumption = { showDatabaseConsumptionDialog = true },
                    onOpenSponsorPanel = { showSponsorPanelDialog = true },
                    isFullAdmin = userRole == "admin" || com.example.util.AuthManager.isCurrentUserAdmin()
                )

                // Panel principal de gestión
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    if (userRole == "admin" || userRole == "moderador" || com.example.util.AuthManager.isCurrentUserAdmin()) {
                        EnhancedUserManagementPanel(
                        isMinimized = isMonitoringMinimized,
                        onToggleMinimize = { isMonitoringMinimized = !isMonitoringMinimized }
                    )
                    } else {
                        // Moderador View
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.SupportAgent, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Panel de Moderación", color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Abre 'Soporte' en la parte superior para moderar los aportes de la comunidad.", color = TextSecondary, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminDashboardHeader(
    onClose: () -> Unit,
    onOpenFeedbackAndSupport: () -> Unit,
    onOpenBroadcast: () -> Unit,
    onOpenNotice: () -> Unit,
    onOpenCpmAnalytics: () -> Unit = {},
    onOpenDatabaseConsumption: () -> Unit = {},
    onOpenSponsorPanel: () -> Unit = {},
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
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Column {
                        Text(
                            text = "Panel de Administración",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HextechGold
                        )
                        Text(
                            text = "Control de Usuarios, Membresías y Slots",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botones de acción rápida superiores (Fijados y siempre visibles)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Botón Soporte
                AnimatedAdminActionButton(
                    onClick = onOpenFeedbackAndSupport,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = HextechCyan.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.SupportAgent, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Soporte", fontSize = 9.sp, color = HextechCyan, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }

                // Botón Broadcast
                AnimatedAdminActionButton(
                    onClick = onOpenBroadcast,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFFC4B5FD), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Broadcast", fontSize = 9.sp, color = Color(0xFFC4B5FD), fontWeight = FontWeight.SemiBold, maxLines = 1)
                }

                // Botón Avisos
                AnimatedAdminActionButton(
                    onClick = onOpenNotice,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Announcement, contentDescription = null, tint = Color(0xFF2DD4BF), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Avisos", fontSize = 9.sp, color = Color(0xFF2DD4BF), fontWeight = FontWeight.SemiBold, maxLines = 1)
                }

                // Botón CPM
                AnimatedAdminActionButton(
                    onClick = onOpenCpmAnalytics,
                    modifier = Modifier.weight(0.8f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF00FF66), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("CPM", fontSize = 9.sp, color = Color(0xFF00FF66), fontWeight = FontWeight.SemiBold, maxLines = 1)
                }

                // Botón Base de Datos
                AnimatedAdminActionButton(
                    onClick = onOpenDatabaseConsumption,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = HextechGold, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Base Datos", fontSize = 9.sp, color = HextechGold, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun AdminNoticeConfigDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val currentNotices by com.example.data.AppNoticeManager.notices.collectAsState()
    val currentIntervalVal by com.example.data.AppNoticeManager.streamerIntervalValue.collectAsState()
    val currentIntervalUnit by com.example.data.AppNoticeManager.streamerIntervalUnit.collectAsState()
    var showCpmFromNotices by remember { mutableStateOf(false) }

    if (showCpmFromNotices) {
        AdminCpmAnalyticsDialog(onDismiss = { showCpmFromNotices = false })
    }

    var noticesList by remember { mutableStateOf(currentNotices) }
    var intervalValueText by remember { mutableStateOf(currentIntervalVal.toString()) }
    var intervalUnit by remember { mutableStateOf(currentIntervalUnit) }
    var isSavingCloud by remember { mutableStateOf(false) }

    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var editingNoticeId by remember { mutableStateOf<String?>(null) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    var expandedImageUrl by remember { mutableStateOf("") }
    var externalUrl by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("Anuncios importantes") }
    var titleColor by remember { mutableStateOf("#FFD700") }
    var contentColor by remember { mutableStateOf("#CCCCCC") }
    var isEnabled by remember { mutableStateOf(true) }
    var budgetText by remember { mutableStateOf("") }

    // Carga instantánea y sincronización reactiva de anuncios
    LaunchedEffect(Unit) {
        com.example.data.AppNoticeManager.syncFromCloud(context)
    }
    LaunchedEffect(currentNotices) {
        if (editingIndex == null && title.isBlank() && content.isBlank()) {
            noticesList = currentNotices
        }
    }

    val tagsList = listOf("Anuncios importantes", "Ofertas", "Mantenimiento", "Noticia", "Streamer", "PUBLICIDAD")
    val isUrlValid = remember(videoUrl) { NoticeMediaUtils.isValidNoticeMedia(videoUrl) }
    val isExpandedUrlValid = remember(expandedImageUrl) { NoticeMediaUtils.isValidNoticeMedia(expandedImageUrl) }
    val coroutineScope = rememberCoroutineScope()
    var isProcessingMedia by remember { mutableStateOf(false) }
    var showManualVideoUrlInput by remember { mutableStateOf(false) }
    var showManualExpandedUrlInput by remember { mutableStateOf(false) }

    val clipboardManager = remember { context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager }
    fun copyToClipboard(label: String, text: String) {
        val clip = android.content.ClipData.newPlainText(label, text)
        clipboardManager?.setPrimaryClip(clip)
        Toast.makeText(context, "Copiado al portapapeles: $text", Toast.LENGTH_SHORT).show()
    }

    val hasFilledFields = title.isNotBlank() ||
        content.isNotBlank() ||
        videoUrl.isNotBlank() ||
        expandedImageUrl.isNotBlank() ||
        externalUrl.isNotBlank() ||
        budgetText.isNotBlank() ||
        editingIndex != null

    fun attemptDismiss() {
        if (hasFilledFields) {
            Toast.makeText(
                context,
                "No puedes salir mientras haya campos con información. Guarda el anuncio o límpialos para evitar cierres accidentales.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = { attemptDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        BackHandler(enabled = true) {
            if (showCpmFromNotices) {
                showCpmFromNotices = false
            } else if (editingIndex != null) {
                editingIndex = null
                editingNoticeId = null
                title = ""
                content = ""
                videoUrl = ""
                expandedImageUrl = ""
                externalUrl = ""
                showManualVideoUrlInput = false
                showManualExpandedUrlInput = false
                Toast.makeText(context, "Edición cancelada.", Toast.LENGTH_SHORT).show()
            } else {
                attemptDismiss()
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = HextechDarkBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Barra superior fija / cabecera completa
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = HextechSurface,
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Announcement, contentDescription = null, tint = HextechGold, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Gestor de Anuncios y Noticias", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(
                                    if (hasFilledFields) "Edición activa (salida bloqueada contra pérdidas)" else "Pantalla completa • Gestión de avisos oficiales",
                                    color = if (hasFilledFields) HextechGold else TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        IconButton(
                            onClick = { attemptDismiss() },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (hasFilledFields) HextechGold.copy(alpha = 0.15f) else HextechSurfaceVariant
                            )
                        ) {
                            Icon(
                                imageVector = if (hasFilledFields) Icons.Default.Lock else Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = if (hasFilledFields) HextechGold else TextSecondary
                            )
                        }
                    }
                }

                // Banner de advertencia de protección si hay campos llenos
                if (hasFilledFields) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = HextechGold.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = HextechGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Protección activa: Hay campos con información. Guarda o vacía los campos para poder salir.",
                                    color = HextechGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            TextButton(
                                onClick = {
                                    editingIndex = null
                                    editingNoticeId = null
                                    title = ""
                                    content = ""
                                    videoUrl = ""
                                    expandedImageUrl = ""
                                    externalUrl = ""
                                    showManualVideoUrlInput = false
                                    showManualExpandedUrlInput = false
                                    Toast.makeText(context, "Campos limpiados. Salida desbloqueada.", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = DangerRed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Limpiar campos", color = DangerRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Contenido desplazable
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text("Administra los avisos y anuncios oficiales que se muestran en la pantalla de inicio:", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(10.dp))

                // Banner to open CPM & Monetization metrics
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCpmFromNotices = true },
                    color = Color(0xFF00FF66).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.2.dp, Color(0xFF00FF66).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF00FF66), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Métricas de CPM & Monetización", color = Color(0xFF00FF66), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Ver impresiones, clics, CTR e ingresos estimados", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF00FF66), modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Card with copyable image dimension recommendations
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhotoSizeSelectActual, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Medidas Recomendadas (Toca para Copiar)", color = HextechCyan, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Utiliza estas resoluciones exactas para que tus imágenes y videos queden perfectamente encuadrados:", color = TextSecondary, fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Horizontal dimensions chip
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { copyToClipboard("Medida Horizontal", "1920x1080") },
                            color = HextechSurfaceVariant,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Horizontal (Panel de Inicio / Tarjeta):", color = HextechCyan, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                    Text("1920 x 1080 px  (Relación 16:9)", color = TextPrimary, fontSize = 10.sp)
                                }
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = HextechCyan, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Vertical dimensions chip
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { copyToClipboard("Medida Vertical", "1080x1920") },
                            color = HextechSurfaceVariant,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Vertical (Vista Ampliada / Fullscreen):", color = HextechGold, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                    Text("1080 x 1920 px  (9:16 Pantalla Completa)", color = TextPrimary, fontSize = 10.sp)
                                }
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = HextechGold, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Interval configuration for streamer and publicidad notices
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = HextechSurfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Intervalo de Rotación (Todos los Anuncios > 1)", color = HextechGold, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Si hay más de 1 anuncio en una categoría, rotarán automáticamente con este intervalo (si solo hay 1, se queda fijo):", color = TextSecondary, fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = intervalValueText,
                                onValueChange = { intervalValueText = it.filter { c -> c.isDigit() } },
                                label = { Text("Valor") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            val units = listOf("seconds" to "Seg", "minutes" to "Min", "hours" to "Horas")
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                units.forEach { (key, label) ->
                                    val isSelected = intervalUnit == key
                                    Button(
                                        onClick = { intervalUnit = key },
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) HextechCyan else HextechSurface
                                        )
                                    ) {
                                        Text(label, color = if (isSelected) HextechDarkBg else TextPrimary, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // List of existing notices (only active/non-expired ones in Anuncios Actuales)
                val now = System.currentTimeMillis()
                val activeNotices = noticesList.filter { notice -> notice.expiresAtMillis == 0L || notice.expiresAtMillis > now }
                val expiredNotices = noticesList.filter { notice -> notice.expiresAtMillis > 0L && notice.expiresAtMillis <= now }

                if (activeNotices.isNotEmpty()) {
                    Text("Anuncios Actuales (${activeNotices.size}):", color = HextechCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    activeNotices.forEach { notice ->
                        val originalIndex = noticesList.indexOfFirst { it.id == notice.id }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            color = HextechSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (notice.isEnabled) HextechGold.copy(alpha = 0.5f) else TextMuted.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = notice.tag, color = HextechGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        if (notice.sponsorEmail.isNotBlank() || notice.tag.equals("Publicidad", true)) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                                border = BorderStroke(0.5.dp, Color(0xFF10B981))
                                            ) {
                                                Text(
                                                    "PATROCINIO APROBADO",
                                                    color = Color(0xFF10B981),
                                                    fontSize = 7.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "•", color = TextMuted, fontSize = 9.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = notice.title.ifBlank { "Sin título" },
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                    Text(
                                        text = notice.content,
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    // Visual Media Indicators
                                    Row(
                                        modifier = Modifier.padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (notice.videoUrl.isNotBlank()) {
                                            Surface(
                                                color = HextechCyan.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp),
                                                border = BorderStroke(0.5.dp, HextechCyan.copy(alpha = 0.4f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        if (notice.videoUrl.contains("video") || notice.videoUrl.endsWith(".mp4")) Icons.Default.Videocam else Icons.Default.Image,
                                                        contentDescription = null,
                                                        tint = HextechCyan,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("Horizontal", color = HextechCyan, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        if (notice.expandedImageUrl.isNotBlank()) {
                                            Surface(
                                                color = HextechGold.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp),
                                                border = BorderStroke(0.5.dp, HextechGold.copy(alpha = 0.4f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = HextechGold, modifier = Modifier.size(10.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("Vertical", color = HextechGold, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        if (notice.externalUrl.isNotBlank()) {
                                            Surface(
                                                color = Color(0xFF3399FF).copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp),
                                                border = BorderStroke(0.5.dp, Color(0xFF3399FF).copy(alpha = 0.4f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = Color(0xFF3399FF), modifier = Modifier.size(10.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("Enlace", color = Color(0xFF3399FF), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        if (notice.budget > 0) {
                                            Surface(
                                                color = Color(0xFF00FF66).copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp),
                                                border = BorderStroke(0.5.dp, Color(0xFF00FF66).copy(alpha = 0.4f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color(0xFF00FF66), modifier = Modifier.size(10.dp))
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text("$${String.format(Locale.US, "%.2f", notice.budget)}", color = Color(0xFF00FF66), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            editingIndex = originalIndex
                                            editingNoticeId = notice.id
                                            title = notice.title
                                            content = notice.content
                                            videoUrl = notice.videoUrl
                                            expandedImageUrl = notice.expandedImageUrl
                                            externalUrl = notice.externalUrl
                                            selectedTag = notice.tag
                                            titleColor = notice.titleColor
                                            contentColor = notice.contentColor
                                            isEnabled = notice.isEnabled
                                            budgetText = if (notice.budget > 0) String.format(Locale.US, "%.2f", notice.budget) else ""
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = HextechCyan, modifier = Modifier.size(14.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            if (editingIndex == originalIndex || editingNoticeId == notice.id) {
                                                editingIndex = null
                                                editingNoticeId = null
                                                title = ""
                                                content = ""
                                                videoUrl = ""
                                                expandedImageUrl = ""
                                                externalUrl = ""
                                                budgetText = ""
                                            }
                                            noticesList = noticesList.filter { it.id != notice.id }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = DangerRed, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (expiredNotices.isNotEmpty()) {
                    Text("Anuncios Expirados (${expiredNotices.size}):", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    expiredNotices.forEach { notice ->
                        val originalIndex = noticesList.indexOfFirst { it.id == notice.id }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            color = HextechSurfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.5.dp, TextMuted.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "${notice.tag} • ${notice.title.ifBlank { "Sin título" }} (Expirado)", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                                IconButton(
                                    onClick = {
                                        noticesList = noticesList.filter { it.id != notice.id }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = DangerRed, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Divider(color = HextechCardBorder)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (editingIndex != null) "✏️ Editando Anuncio #${editingIndex!! + 1}" else "Agregar Nuevo Anuncio:",
                        color = HextechGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (editingIndex != null) {
                        TextButton(
                            onClick = {
                                editingIndex = null
                                editingNoticeId = null
                                title = ""
                                content = ""
                                videoUrl = ""
                                expandedImageUrl = ""
                                externalUrl = ""
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = DangerRed, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancelar", color = DangerRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Tag selector
                Text("Etiqueta / Categoría:", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tagsList.forEach { tag ->
                        val isSelected = selectedTag == tag
                        Button(
                            onClick = { selectedTag = tag },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) HextechGold else HextechSurfaceVariant
                            )
                        ) {
                            Text(tag, color = if (isSelected) HextechDarkBg else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título del Aviso") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("🎨 Color del Título:", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val colorsList = listOf(
                        "Dorado" to "#FFD700",
                        "Cian" to "#00F2FE",
                        "Blanco" to "#FFFFFF",
                        "Verde" to "#00FF66",
                        "Naranja" to "#FF9900",
                        "Rojo" to "#FF3333",
                        "Morado" to "#CC66FF"
                    )
                    colorsList.forEach { (name, hex) ->
                        val isSelected = titleColor.equals(hex, true)
                        val parsedColor = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { HextechGold }
                        Button(
                            onClick = { titleColor = hex },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) parsedColor else HextechSurfaceVariant
                            ),
                            border = BorderStroke(1.dp, parsedColor)
                        ) {
                            Text(name, color = if (isSelected) HextechDarkBg else parsedColor, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Contenido / Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("🎨 Color de la Descripción:", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val contentColorsList = listOf(
                        "Gris Claro" to "#CCCCCC",
                        "Blanco" to "#FFFFFF",
                        "Cian" to "#00F2FE",
                        "Dorado" to "#FFD700",
                        "Verde" to "#00FF66",
                        "Amarillo" to "#FFEE55"
                    )
                    contentColorsList.forEach { (name, hex) ->
                        val isSelected = contentColor.equals(hex, true)
                        val parsedColor = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { TextSecondary }
                        Button(
                            onClick = { contentColor = hex },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) parsedColor else HextechSurfaceVariant
                            ),
                            border = BorderStroke(1.dp, parsedColor)
                        ) {
                            Text(name, color = if (isSelected) HextechDarkBg else parsedColor, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 1. Horizontal Media (Home screen)
                Spacer(modifier = Modifier.height(12.dp))
                Text("1. Multimedia Horizontal (Panel de Inicio):", color = HextechCyan, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                val horizontalMediaPickerLauncher = rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                ) { uri: android.net.Uri? ->
                    uri?.let { pickedUri ->
                        coroutineScope.launch {
                            isProcessingMedia = true
                            try {
                                val validation = NoticeMediaUtils.validateMediaForSlot(context, pickedUri, isVerticalSlot = false)
                                if (!validation.isValid) {
                                    Toast.makeText(context, validation.errorMessage ?: "El archivo no cumple con el tamaño o proporción recomendada para banner horizontal", Toast.LENGTH_LONG).show()
                                    isProcessingMedia = false
                                    return@launch
                                }

                                if (validation.isVideo) {
                                    Toast.makeText(context, "Procesando video horizontal...", Toast.LENGTH_SHORT).show()
                                    val finalVideoUrl = com.example.util.NoticeMediaStorageManager.uploadOrSaveVideo(context, pickedUri)
                                    videoUrl = finalVideoUrl
                                    Toast.makeText(context, "Video horizontal configurado exitosamente", Toast.LENGTH_SHORT).show()
                                } else {
                                    val cloudDataUrl = com.example.util.NoticeMediaStorageManager.convertImageToCloudDataUrl(context, pickedUri)
                                    videoUrl = cloudDataUrl
                                    Toast.makeText(context, "Imagen horizontal configurada correctamente", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "No se pudo procesar el archivo seleccionado", Toast.LENGTH_SHORT).show()
                            } finally {
                                isProcessingMedia = false
                            }
                        }
                    }
                }

                if (videoUrl.isNotBlank()) {
                    Surface(
                        color = HextechSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            val isVideo = NoticeMediaUtils.isVideo(context, videoUrl)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isVideo) {
                                    NoticeMediaViewer(
                                        mediaUrl = videoUrl,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    val decodedBytes = remember(videoUrl) {
                                        if (videoUrl.startsWith("data:image/")) {
                                            com.example.util.NoticeMediaStorageManager.decodeDataUriToBytes(videoUrl)
                                        } else if (videoUrl.startsWith("file://")) {
                                            val path = Uri.parse(videoUrl).path ?: ""
                                            java.io.File(path)
                                        } else null
                                    }
                                    AsyncImage(
                                        model = decodedBytes ?: videoUrl,
                                        contentDescription = "Vista previa multimedia horizontal",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isVideo) "Video Horizontal Configurado" else "Imagen Horizontal Configurada",
                                        color = HextechCyan,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Visible en la tarjeta de novedades en el inicio.",
                                        color = TextMuted,
                                        fontSize = 9.5.sp
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = { horizontalMediaPickerLauncher.launch("*/*") },
                                        colors = ButtonDefaults.buttonColors(containerColor = HextechSurface),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.6f)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Cambiar", color = HextechCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { videoUrl = "" },
                                        colors = ButtonDefaults.buttonColors(containerColor = HextechSurface),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.6f)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = DangerRed, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Quitar", color = DangerRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (showManualVideoUrlInput) {
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = videoUrl,
                                    onValueChange = { videoUrl = it },
                                    label = { Text("Editar enlace o URL") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(6.dp)
                                )
                            } else {
                                Text(
                                    text = "Editar URL o enlace web",
                                    color = HextechCyan.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .clickable { showManualVideoUrlInput = true }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = videoUrl,
                        onValueChange = { videoUrl = it },
                        label = { Text("URL YouTube o Imagen/Video Horizontal") },
                        singleLine = true,
                        maxLines = 1,
                        isError = !isUrlValid,
                        supportingText = {
                            if (!isUrlValid) {
                                Text("Enlace inválido. Solo URLs de YouTube o archivos de galería.", color = DangerRed, fontSize = 10.sp)
                            } else {
                                Text("Se muestra en la tarjeta del panel de inicio (Horizontal)", color = TextMuted, fontSize = 10.sp)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { horizontalMediaPickerLauncher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = HextechSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Subir Multimedia Horizontal desde Galería", color = HextechCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // 2. Vertical Expanded Media (Video or Image)
                Spacer(modifier = Modifier.height(12.dp))
                Text("2. Multimedia Vertical (Video o Imagen para Vista Ampliada):", color = HextechGold, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                val verticalImagePickerLauncher = rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                ) { uri: android.net.Uri? ->
                    uri?.let { pickedUri ->
                        coroutineScope.launch {
                            isProcessingMedia = true
                            try {
                                val validation = NoticeMediaUtils.validateMediaForSlot(context, pickedUri, isVerticalSlot = true)
                                if (!validation.isValid) {
                                    Toast.makeText(context, validation.errorMessage ?: "El archivo no cumple con el tamaño o proporción recomendada para vista vertical", Toast.LENGTH_LONG).show()
                                    isProcessingMedia = false
                                    return@launch
                                }

                                if (validation.isVideo) {
                                    val finalVideoUrl = com.example.util.NoticeMediaStorageManager.uploadOrSaveVideo(context, pickedUri)
                                    expandedImageUrl = finalVideoUrl
                                    Toast.makeText(context, "Video vertical configurado correctamente", Toast.LENGTH_SHORT).show()
                                } else {
                                    val cloudDataUrl = com.example.util.NoticeMediaStorageManager.convertImageToCloudDataUrl(context, pickedUri)
                                    expandedImageUrl = cloudDataUrl
                                    Toast.makeText(context, "Imagen vertical subida correctamente", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "No se pudo procesar el archivo seleccionado", Toast.LENGTH_SHORT).show()
                            } finally {
                                isProcessingMedia = false
                            }
                        }
                    }
                }

                if (expandedImageUrl.isNotBlank()) {
                    Surface(
                        color = HextechSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val isVerticalVideo = remember(expandedImageUrl) {
                                    NoticeMediaUtils.isVideo(context, expandedImageUrl)
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.55f)
                                        .aspectRatio(9f / 16f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black)
                                        .border(1.dp, HextechGold.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isVerticalVideo) {
                                        NoticeMediaViewer(
                                            mediaUrl = expandedImageUrl,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        val decodedVerticalBytes = remember(expandedImageUrl) {
                                            if (expandedImageUrl.startsWith("data:image/")) {
                                                com.example.util.NoticeMediaStorageManager.decodeDataUriToBytes(expandedImageUrl)
                                            } else if (expandedImageUrl.startsWith("file://")) {
                                                val path = Uri.parse(expandedImageUrl).path ?: ""
                                                java.io.File(path)
                                            } else null
                                        }
                                        AsyncImage(
                                            model = decodedVerticalBytes ?: expandedImageUrl,
                                            contentDescription = "Vista previa vertical",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isVerticalVideo) "Video Vertical Configurado" else "Imagen Vertical Configurada",
                                        color = HextechGold,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isVerticalVideo) "Se reproducirá a pantalla completa en modo vertical al pulsar 'Ampliar'." else "Se mostrará a pantalla completa al pulsar 'Ampliar' o al tocar la imagen.",
                                        color = TextMuted,
                                        fontSize = 9.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = { verticalImagePickerLauncher.launch("*/*") },
                                            colors = ButtonDefaults.buttonColors(containerColor = HextechSurface),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.6f)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, tint = HextechGold, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Cambiar", color = HextechGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { expandedImageUrl = "" },
                                            colors = ButtonDefaults.buttonColors(containerColor = HextechSurface),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.6f)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, tint = DangerRed, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Quitar", color = DangerRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            if (showManualExpandedUrlInput) {
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = expandedImageUrl,
                                    onValueChange = { expandedImageUrl = it },
                                    label = { Text("Editar URL vertical manualmente") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(6.dp)
                                )
                            } else {
                                Text(
                                    text = "Editar URL o enlace web",
                                    color = HextechGold.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .clickable { showManualExpandedUrlInput = true }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = expandedImageUrl,
                        onValueChange = { expandedImageUrl = it },
                        label = { Text("URL o Video/Imagen Vertical Ampliada (Opcional)") },
                        singleLine = true,
                        maxLines = 1,
                        isError = !isExpandedUrlValid,
                        supportingText = {
                            if (!isExpandedUrlValid) {
                                Text("Enlace inválido. Solo URLs de YouTube/Imágenes o archivos de galería.", color = DangerRed, fontSize = 10.sp)
                            } else {
                                Text("Video o Imagen vertical que se observará al pulsar en 'Ampliar'", color = TextMuted, fontSize = 10.sp)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { verticalImagePickerLauncher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = HextechSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = HextechGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Subir Multimedia Vertical (Video o Imagen)", color = HextechGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (isProcessingMedia) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(HextechSurfaceVariant, RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = HextechCyan, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Optimizando multimedia para Multidispositivo...", color = HextechCyan, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = HextechGold.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = HextechGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MULTIDISPOSITIVO ACTIVO (Sincronización en la nube)",
                                color = HextechGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Las imágenes de galería se convierten automáticamente al formato de nube para verse en CUALQUIER celular y no ponerse negras al reiniciar.\n• Para videos en múltiples celulares, usa enlaces de YouTube (o Shorts) o URLs web (.mp4). Los videos locales se guardan permanentemente en este celular.",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("3. Enlace Web Externo (Opcional):", color = HextechGold, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = externalUrl,
                    onValueChange = { externalUrl = it },
                    label = { Text("URL de sitio web externo (Redirección al tocar imagen)") },
                    supportingText = {
                        Text("Si se define, al ampliar la imagen se podrá abrir este enlace web externamente.", color = TextMuted, fontSize = 10.sp)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("4. Presupuesto de Campaña / Anuncio (USD - Opcional):", color = HextechGold, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) {
                            budgetText = input
                        }
                    },
                    label = { Text("Presupuesto en USD (ej. 50.00)") },
                    placeholder = { Text("0.00") },
                    leadingIcon = {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, tint = HextechGold)
                    },
                    supportingText = {
                        Text("Monitorea el gasto y saldo restante de este anuncio en el panel de analíticas CPM.", color = TextMuted, fontSize = 10.sp)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Activar anuncio en inicio", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Switch(checked = isEnabled, onCheckedChange = { isEnabled = it })
                }

                Spacer(modifier = Modifier.height(8.dp))
                if (editingIndex != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                editingIndex = null
                                editingNoticeId = null
                                title = ""
                                content = ""
                                videoUrl = ""
                                expandedImageUrl = ""
                                externalUrl = ""
                                budgetText = ""
                                showManualVideoUrlInput = false
                                showManualExpandedUrlInput = false
                                Toast.makeText(context, "Edición cancelada", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                            border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = DangerRed, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancelar Edición", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                if (title.isBlank() && content.isBlank()) {
                                    Toast.makeText(context, "Ingresa un título o contenido para el anuncio", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (!isUrlValid || !isExpandedUrlValid) {
                                    Toast.makeText(context, "URL multimedia inválida", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val targetId = editingNoticeId ?: noticesList[editingIndex!!].id
                                val newNotice = com.example.data.AppNotice(
                                    id = targetId,
                                    title = title.ifBlank { "Aviso Oficial" },
                                    content = content,
                                    videoUrl = videoUrl,
                                    expandedImageUrl = expandedImageUrl,
                                    externalUrl = externalUrl,
                                    tag = selectedTag,
                                    titleColor = titleColor,
                                    contentColor = contentColor,
                                    isEnabled = isEnabled,
                                    budget = budgetText.toDoubleOrNull() ?: 0.0
                                )
                                noticesList = noticesList.toMutableList().apply { set(editingIndex!!, newNotice) }
                                editingIndex = null
                                editingNoticeId = null
                                title = ""
                                content = ""
                                videoUrl = ""
                                expandedImageUrl = ""
                                externalUrl = ""
                                budgetText = ""
                                showManualVideoUrlInput = false
                                showManualExpandedUrlInput = false
                                Toast.makeText(context, "Anuncio guardado en la lista", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = HextechCyan, contentColor = HextechDarkBg),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Guardar Cambios", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            if (title.isBlank() && content.isBlank()) {
                                Toast.makeText(context, "Ingresa un título o contenido para el anuncio", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (!isUrlValid || !isExpandedUrlValid) {
                                Toast.makeText(context, "URL multimedia inválida", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val targetId = UUID.randomUUID().toString()
                            val newNotice = com.example.data.AppNotice(
                                id = targetId,
                                title = title.ifBlank { "Aviso Oficial" },
                                content = content,
                                videoUrl = videoUrl,
                                expandedImageUrl = expandedImageUrl,
                                externalUrl = externalUrl,
                                tag = selectedTag,
                                titleColor = titleColor,
                                contentColor = contentColor,
                                isEnabled = isEnabled,
                                budget = budgetText.toDoubleOrNull() ?: 0.0
                            )
                            noticesList = noticesList + newNotice
                            title = ""
                            content = ""
                            videoUrl = ""
                            expandedImageUrl = ""
                            externalUrl = ""
                            budgetText = ""
                            editingNoticeId = null
                            showManualVideoUrlInput = false
                            showManualExpandedUrlInput = false
                            Toast.makeText(context, "Anuncio guardado en la lista", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = HextechCyan, contentColor = HextechDarkBg),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Agregar a la Lista de Anuncios", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("👁️ Vista Previa del Anuncio:", color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))

                // Live Preview Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, HextechGold.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = HextechSurfaceVariant.copy(alpha = 0.9f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Campaign, contentDescription = null, tint = HextechGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title.ifBlank { "Título del aviso..." },
                                    color = HextechGold,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                color = HextechGold.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = selectedTag.uppercase(),
                                    color = HextechGold,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = content.ifBlank { "Escribe el contenido del anuncio para visualizarlo aquí..." },
                            color = TextPrimary,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                        if (videoUrl.isNotBlank() && isUrlValid) {
                            Spacer(modifier = Modifier.height(8.dp))
                            NoticeMediaViewer(
                                mediaUrl = videoUrl,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                            )
                        }
                        if (expandedImageUrl.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Vista previa (Media Ampliada):", color = HextechGold, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            NoticeMediaViewer(
                                mediaUrl = expandedImageUrl,
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(160.dp)
                            )
                        }
                    }
                }
            }

            // Barra inferior fija de acciones
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = HextechSurface,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { attemptDismiss() },
                            enabled = !isSavingCloud,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (hasFilledFields) HextechGold.copy(alpha = 0.5f) else TextMuted),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (hasFilledFields) HextechGold else TextSecondary
                            )
                        ) {
                            Icon(
                                imageVector = if (hasFilledFields) Icons.Default.Lock else Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (hasFilledFields) "Cerrar (Bloqueado)" else "Cerrar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (isSavingCloud) return@Button
                                isSavingCloud = true
                                val intVal = intervalValueText.toIntOrNull() ?: 10
                                com.example.data.AppNoticeManager.saveAllNoticesAndInterval(
                                    context = context,
                                    newNotices = noticesList,
                                    intervalValue = intVal,
                                    intervalUnit = intervalUnit
                                ) { success, errorMsg ->
                                    isSavingCloud = false
                                    if (success) {
                                        Toast.makeText(context, "¡Anuncios sincronizados correctamente!", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    } else {
                                        Toast.makeText(context, "Guardado localmente con éxito", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                }
                            },
                            enabled = !isSavingCloud,
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HextechGold, contentColor = HextechDarkBg)
                        ) {
                            if (isSavingCloud) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = HextechDarkBg, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sincronizando...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Publicar Todos", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class UserFilterTab(val label: String) {
    ALL("Todos"),
    PREMIUM("Premium"),
    FREE("Gratis"),
    ONLINE("Online"),
    ADMINS("Admins"),
    MODS("Mods"),
    SPONSORS("Patrocinador"),
    BANNED("Baneados"),
    STREAMERS("Streamers"),
    CREATORS("Creadores")
}

@Composable
fun EnhancedUserManagementPanel(
    isMinimized: Boolean = false,
    onToggleMinimize: () -> Unit = {}
) {
    val context = LocalContext.current
    var users by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(UserFilterTab.ALL) }

    // Dialogs
    var selectedUserForManage by remember { mutableStateOf<Map<String, Any>?>(null) }
    var selectedUserForAvatarGift by remember { mutableStateOf<Map<String, Any>?>(null) }

    BackHandler(enabled = selectedUserForManage != null || selectedUserForAvatarGift != null) {
        selectedUserForManage = null
        selectedUserForAvatarGift = null
    }

    var listenerReg by remember { mutableStateOf<com.google.firebase.firestore.ListenerRegistration?>(null) }

    fun loadUsers() {
        isLoading = true
        errorMessage = null
        listenerReg?.remove()
        listenerReg = FirebaseFirestore.getInstance().collection("users")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    errorMessage = "Error al cargar usuarios: ${e.message}"
                    isLoading = false
                    isRefreshing = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.map { doc ->
                        val data = doc.data?.toMutableMap() ?: mutableMapOf()
                        data["uid"] = doc.id
                        data
                    }
                    users = list
                    isLoading = false
                    isRefreshing = false
                }
            }
    }

    DisposableEffect(Unit) {
        onDispose {
            listenerReg?.remove()
        }
    }

    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        loadUsers()
        while (true) {
            kotlinx.coroutines.delay(1000L)
            currentTime = System.currentTimeMillis()
        }
    }

    // Cálculos de métricas en tiempo real
    val totalUsers = users.size
    val now = currentTime
    val onlineThreshold = 10 * 60 * 1000L // Activos en últimos 10 minutos o con flag is_online

    val onlineUsers = users.count { u ->
        val isOnlineFlag = u["is_online"] as? Boolean
        val lastActive = (u["last_active"] as? Number)?.toLong() ?: (u["lastActiveTimestamp"] as? Number)?.toLong() ?: 0L
        if (isOnlineFlag == false) false else (lastActive > 0L && now - lastActive < onlineThreshold)
    }

    val premiumUsers = users.count { u ->
        val role = u["role"] as? String ?: "free"
        val until = (u["premiumUntil"] as? Number)?.toLong()
        role == "premium" && (until == null || until == 0L || until > now)
    }

    val adminUsers = users.count { (it["role"] as? String) == "admin" }
    val modUsers = users.count { (it["role"] as? String) == "moderador" }
    val sponsorUsers = users.count { (it["role"] as? String) == "patrocinador" }
    val streamerUsers = users.count { (it["role"] as? String) == "streamer" }
    val creatorUsers = users.count { (it["role"] as? String) == "creador_vip" || (it["role"] as? String) == "creador" }
    val freeUsers = users.count { u ->
        val role = u["role"] as? String ?: "free"
        val until = (u["premiumUntil"] as? Number)?.toLong()
        role == "free" || (role == "premium" && until != null && until > 0L && until <= now)
    }
    val bannedUsers = users.count { (it["banned"] as? Boolean) == true || (it["role"] as? String) == "banned" }

    // Filtrado de usuarios
    val filteredUsers = remember(users, searchQuery, selectedFilter) {
        users.filter { user ->
            val name = (user["name"] as? String ?: "").lowercase()
            val email = (user["email"] as? String ?: "").lowercase()
            val uid = (user["uid"] as? String ?: "").lowercase()
            val secRole = (user["secondaryRole"] as? String ?: "").lowercase()
            val query = searchQuery.trim().lowercase()

            val matchesQuery = query.isEmpty() || name.contains(query) || email.contains(query) || uid.contains(query) || secRole.contains(query)

            val role = user["role"] as? String ?: "free"
            val until = (user["premiumUntil"] as? Number)?.toLong()
            val isPrem = role == "premium" && (until == null || until == 0L || until > now)
            val explicitOnline = user["is_online"] as? Boolean
            val lastActiveTmp = (user["last_active"] as? Number)?.toLong() ?: (user["lastActiveTimestamp"] as? Number)?.toLong() ?: 0L
            val isOnline = if (explicitOnline == false) false else (lastActiveTmp > 0L && now - lastActiveTmp < onlineThreshold)
            val isBanned = (user["banned"] as? Boolean) == true || role == "banned"

            val matchesTab = when (selectedFilter) {
                UserFilterTab.ALL -> true
                UserFilterTab.PREMIUM -> isPrem
                UserFilterTab.FREE -> role == "free" || (!isPrem && role != "admin" && role != "moderador" && role != "patrocinador" && role != "streamer" && role != "creador_vip" && role != "creador")
                UserFilterTab.ONLINE -> isOnline
                UserFilterTab.ADMINS -> role == "admin"
                UserFilterTab.MODS -> role == "moderador"
                UserFilterTab.SPONSORS -> role == "patrocinador"
                UserFilterTab.STREAMERS -> role == "streamer"
                UserFilterTab.CREATORS -> role == "creador_vip" || role == "creador"
                UserFilterTab.BANNED -> isBanned
            }

            matchesQuery && matchesTab
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Bloque de monitoreo y métricas minimizable
        AnimatedVisibility(visible = !isMinimized) {
            Column {
                // KPI Cards Bar
                AdminKpiCards(
                    total = totalUsers,
                    premium = premiumUsers,
                    free = freeUsers,
                    online = onlineUsers,
                    onRefresh = {
                        isRefreshing = true
                        loadUsers()
                    },
                    isRefreshing = isRefreshing
                )

                ServerScraperHealthCard()
            }
        }

        // Barra informativa de estado minimizado y botón para alternar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isMinimized) "Mostrando vista completa de usuarios" else "Monitoreo y herramientas activas",
                color = if (isMinimized) HextechGold else TextMuted,
                fontSize = 11.sp,
                fontWeight = if (isMinimized) FontWeight.SemiBold else FontWeight.Normal
            )
            TextButton(
                onClick = onToggleMinimize,
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = if (isMinimized) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = HextechCyan,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isMinimized) "Ver Monitoreo / Reportes" else "Minimizar Monitoreo",
                    color = HextechCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Buscador y Chips de Filtro
        Surface(
            color = HextechDarkBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por nombre, email o UID...", color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HextechGold) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = TextMuted)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HextechGold,
                        unfocusedBorderColor = HextechCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Chips de filtro con scroll horizontal
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    UserFilterTab.values().forEach { tab ->
                        val isSelected = selectedFilter == tab
                        val count = when (tab) {
                            UserFilterTab.ALL -> totalUsers
                            UserFilterTab.PREMIUM -> premiumUsers
                            UserFilterTab.FREE -> freeUsers
                            UserFilterTab.ONLINE -> onlineUsers
                            UserFilterTab.ADMINS -> adminUsers
                            UserFilterTab.MODS -> modUsers
                            UserFilterTab.SPONSORS -> sponsorUsers
                            UserFilterTab.STREAMERS -> streamerUsers
                            UserFilterTab.CREATORS -> creatorUsers
                            UserFilterTab.BANNED -> bannedUsers
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = tab },
                            label = {
                                Text(
                                    text = "${tab.label} ($count)",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HextechGold.copy(alpha = 0.25f),
                                selectedLabelColor = HextechGold,
                                containerColor = HextechSurfaceBg,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) HextechGold else HextechCardBorder
                            )
                        )
                    }
                }
            }
        }

        // Lista de Usuarios
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = HextechGold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Cargando usuarios y membresías...", color = TextMuted, fontSize = 13.sp)
                }
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = DangerRed, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage!!, color = DangerRed, textAlign = TextAlign.Center, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { loadUsers() }, colors = ButtonDefaults.buttonColors(containerColor = HextechGold)) {
                        Text("Reintentar", color = HextechDarkBg)
                    }
                }
            }
        } else if (filteredUsers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PersonSearch, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No se encontraron usuarios en esta categoría", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    if (searchQuery.isNotEmpty()) {
                        Text("Intenta con otro término de búsqueda.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredUsers, key = { it["uid"] as? String ?: "" }) { user ->
                    EnhancedUserAdminCard(
                        user = user,
                        currentTime = currentTime,
                        onManageClick = { selectedUserForManage = user },
                        onAvatarGiftClick = { selectedUserForAvatarGift = user },
                        onResetSlotsClick = {
                            val uid = user["uid"] as? String ?: return@EnhancedUserAdminCard
                            resetUserHardwareSlots(context, uid) {
                                Toast.makeText(context, "Slots de hardware liberados exitosamente", Toast.LENGTH_SHORT).show()
                                loadUsers()
                            }
                        }
                    )
                }
            }
        }
    }

    // Modal Detallado de Gestión de Usuario
    selectedUserForManage?.let { user ->
        UserDetailManagementDialog(
            user = user,
            onDismiss = { selectedUserForManage = null },
            onUserUpdated = { updatedMap ->
                users = users.map { if (it["uid"] == updatedMap["uid"]) updatedMap else it }
                selectedUserForManage = updatedMap
            },
            onOpenAvatarGift = {
                selectedUserForAvatarGift = user
            },
            onReloadAll = { loadUsers() }
        )
    }

    // Modal de Galería para Regalar Avatares
    selectedUserForAvatarGift?.let { user ->
        AdminAvatarGiftDialog(
            user = user,
            onDismiss = { selectedUserForAvatarGift = null },
            onAvatarGifted = { newAvatarId ->
                loadUsers()
            }
        )
    }
}

@Composable
private fun AdminKpiCards(
    total: Int,
    premium: Int,
    free: Int,
    online: Int,
    onRefresh: () -> Unit,
    isRefreshing: Boolean
) {
    Surface(
        color = HextechSurfaceBg,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Métricas Generales de Usuarios",
                    color = HextechGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { if (!isRefreshing) onRefresh() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refrescar",
                        tint = if (isRefreshing) HextechGold else TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isRefreshing) "Actualizando..." else "Refrescar",
                        color = if (isRefreshing) HextechGold else TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total
                KpiItemCard(
                    title = "Registrados",
                    value = total.toString(),
                    icon = Icons.Default.People,
                    accentColor = Color(0xFF60A5FA),
                    modifier = Modifier.weight(1f)
                )

                // Premium
                KpiItemCard(
                    title = "Premium",
                    value = premium.toString(),
                    icon = Icons.Default.WorkspacePremium,
                    accentColor = HextechGold,
                    modifier = Modifier.weight(1f)
                )

                // Gratuitos
                KpiItemCard(
                    title = "Gratuitos",
                    value = free.toString(),
                    icon = Icons.Default.SportsEsports,
                    accentColor = HextechCyan,
                    modifier = Modifier.weight(1f)
                )

                // Online
                KpiItemCard(
                    title = "En Línea",
                    value = online.toString(),
                    icon = Icons.Default.Sensors,
                    accentColor = Color(0xFF00FF7F),
                    isLive = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun KpiItemCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isLive: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlphaPulse"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(HextechDarkBg)
            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isLive) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00FF7F).copy(alpha = alphaAnim))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(13.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
            Text(
                text = title,
                color = TextMuted,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EnhancedUserAdminCard(
    user: Map<String, Any>,
    currentTime: Long = System.currentTimeMillis(),
    onManageClick: () -> Unit,
    onAvatarGiftClick: () -> Unit,
    onResetSlotsClick: () -> Unit
) {
    val context = LocalContext.current
    val uid = user["uid"] as? String ?: ""
    val name = user["name"] as? String ?: "Sin Nombre"
    val email = user["email"] as? String ?: ""
    val role = user["role"] as? String ?: "free"
    val isBanned = (user["banned"] as? Boolean) == true || role == "banned"
    val avatarId = user["avatarId"] as? String ?: "default_poro"
    val rankBorder = user["rankBorder"] as? String ?: "NONE"
    val secondaryRole = user["secondaryRole"] as? String ?: "none"
    val premiumUntil = (user["premiumUntil"] as? Number)?.toLong()

    val registeredDevices = (user["registeredDevices"] as? List<*>) ?: emptyList<Any>()
    val deviceSlotsUsed = registeredDevices.size.coerceAtLeast(0)

    val unlockedAvatars = (user["unlockedAvatars"] as? List<*>)?.mapNotNull { it?.toString() } ?: listOf("default_poro")
    val unlockedCount = unlockedAvatars.size

    val lastActiveTimestamp = (user["last_active"] as? Number)?.toLong() ?: (user["lastActiveTimestamp"] as? Number)?.toLong() ?: 0L
    val now = currentTime
    val explicitOnline = user["is_online"] as? Boolean
    val isOnline = if (explicitOnline == false) false else (lastActiveTimestamp > 0L && now - lastActiveTimestamp < 10 * 60 * 1000L)

    val isPremiumActive = when {
        role == "admin" || role == "moderador" -> true
        role in listOf("premium", "creador_vip", "streamer") -> premiumUntil == null || premiumUntil == 0L || premiumUntil > now
        else -> false
    }

    val cardBorderColor = when {
        role == "admin" -> HextechGold.copy(alpha = 0.6f)
        isBanned -> DangerRed.copy(alpha = 0.5f)
        role == "moderador" -> Color(0xFF10B981).copy(alpha = 0.5f)
        role == "creador_vip" -> Color(0xFFA855F7).copy(alpha = 0.5f)
        role == "streamer" -> Color(0xFFEC4899).copy(alpha = 0.5f)
        isPremiumActive -> HextechCyan.copy(alpha = 0.4f)
        else -> HextechCardBorder
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = HextechSurfaceBg,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Fila superior: Avatar + Info Usuario + Badge de Rol + Estado de Conexión
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar con badge de estado online
                val secRoleStr = user["secondaryRole"] as? String ?: "none"
                val secRoleObjRow = com.example.model.AppUserSecondaryRole.fromId(secRoleStr)
                val hasSpecialFrameRow = (role == "admin") || (secRoleObjRow.frameDrawableRes != null)
                Box(
                    modifier = Modifier.padding(
                        horizontal = if (hasSpecialFrameRow) 4.dp else 0.dp,
                        vertical = if (hasSpecialFrameRow) 2.dp else 0.dp
                    ),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    UserAvatarView(
                        avatarId = avatarId,
                        size = 46.dp,
                        fallbackInitial = name.take(1).uppercase(),
                        rankBorder = rankBorder,
                        secondaryRole = secRoleStr,
                        isAdmin = (role == "admin"),
                        fitFrameToSize = true
                    )
                    // Indicador de conexión verde/gris
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (isOnline) Color(0xFF00FF7F) else Color(0xFF6B7280))
                            .border(1.5.dp, HextechDarkBg, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Datos de Usuario
                val isVerifiedUser = (user["isVerified"] as? Boolean) == true || (user["verified"] as? Boolean) == true || role == "admin" || role == "moderador"
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Usuario", name))
                            Toast.makeText(context, "Usuario copiado: $name", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(
                            text = name,
                            fontWeight = FontWeight.Bold,
                            color = if (role == "admin") HextechGold else TextPrimary,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isVerifiedUser) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = "Verificado",
                                tint = if (role == "admin") HextechGold else HextechCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Role Badge debajo del usuario
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        RoleBadge(role = role, isPremiumActive = isPremiumActive, isBanned = isBanned)
                        if (secondaryRole != "none" && secondaryRole.isNotBlank()) {
                            SecondaryRoleBadge(secondaryRole = secondaryRole, size = RoleBadgeSize.COMPACT)
                        }
                    }

                    if (email.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = email,
                            color = TextSecondary,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Correo", email))
                                Toast.makeText(context, "Correo copiado: $email", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    // UID y Estado de Conexión en vivo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // UID copiable con un toque
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("UID", uid))
                                Toast.makeText(context, "UID copiado", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text(
                                text = "UID: ${uid.take(10)}...",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar UID", tint = TextMuted, modifier = Modifier.size(10.dp))
                        }

                        // Badge de Conexión / Última Conexión en Vivo
                        Surface(
                            color = if (isOnline) Color(0xFF00FF7F).copy(alpha = 0.15f) else HextechDarkBg,
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                0.5.dp,
                                if (isOnline) Color(0xFF00FF7F).copy(alpha = 0.5f) else HextechCardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(if (isOnline) Color(0xFF00FF7F) else Color(0xFF9CA3AF))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formatLastConnection(lastActiveTimestamp, isOnline, now),
                                    color = if (isOnline) Color(0xFF00FF7F) else TextMuted,
                                    fontSize = 9.5.sp,
                                    fontWeight = if (isOnline) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = HextechCardBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Fila de Estado: Suscripción & Slots de Hardware
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info de Suscripción con conteo en vivo de segundos
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (isPremiumActive) HextechCyan else TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = getSubscriptionStatusText(role, premiumUntil, isPremiumActive, now),
                            fontSize = 11.sp,
                            color = if (isPremiumActive) HextechCyan else TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (isPremiumActive && role != "admin" && premiumUntil != null && premiumUntil > 0L) {
                        Text(
                            text = formatExpirationDateDetailed(premiumUntil),
                            color = if (premiumUntil - now < 3 * 86400000L) Color(0xFFFBBF24) else TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                // Info de Slots de Dispositivos, Avatares y Esencia Azul
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val blueEssence = (user["blueEssence"] as? Number)?.toLong() ?: 0L
                    // Badge de Esencia Azul
                    Surface(
                        color = HextechDarkBg,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF0EA5E9).copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = com.example.R.drawable.ic_blue_essence),
                                contentDescription = "Esencia Azul",
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "$blueEssence EA",
                                fontSize = 10.sp,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Badge de Slots de Dispositivo
                    Surface(
                        color = HextechDarkBg,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, HextechCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Smartphone, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "$deviceSlotsUsed/2 slots",
                                fontSize = 10.sp,
                                color = if (deviceSlotsUsed >= 2) DangerRed else TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Badge de Avatares desbloqueados
                    Surface(
                        color = HextechDarkBg,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, HextechCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = HextechGold, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "$unlockedCount avatares",
                                fontSize = 10.sp,
                                color = HextechGold,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botones de acción rápida en la tarjeta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Botón Regalar Avatar
                AnimatedAdminOutlinedButton(
                    onClick = onAvatarGiftClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HextechGold),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Regalar Avatar", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                // Botón Reiniciar Slots
                AnimatedAdminOutlinedButton(
                    onClick = onResetSlotsClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF60A5FA)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF60A5FA).copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset Slots", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                // Botón Gestionar Completo
                AnimatedAdminActionButton(
                    onClick = onManageClick,
                    modifier = Modifier.weight(1.1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = HextechDarkBg, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Gestionar", color = HextechDarkBg, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun formatLastConnection(lastActiveTimestamp: Long, isOnline: Boolean, currentTimestamp: Long = System.currentTimeMillis()): String {
    if (isOnline) {
        if (lastActiveTimestamp > 0L) {
            val diff = (currentTimestamp - lastActiveTimestamp).coerceAtLeast(0L)
            val secs = diff / 1000L
            if (secs < 60) return "En línea (${secs}s)"
            val mins = secs / 60
            return "En línea (${mins}m)"
        }
        return "En línea ahora"
    }
    if (lastActiveTimestamp <= 0L) return "Sin registro reciente"
    val diff = (currentTimestamp - lastActiveTimestamp).coerceAtLeast(0L)
    val secs = diff / 1000L
    if (secs < 60) return "Hace ${secs}s"
    val mins = secs / 60
    if (mins < 60) {
        val remSecs = secs % 60
        return "Hace ${mins}m ${remSecs}s"
    }
    val hours = mins / 60
    if (hours < 24) {
        val remMins = mins % 60
        return "Hace ${hours}h ${remMins}m"
    }
    val days = hours / 24
    if (days < 7) return "Hace $days d"
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(lastActiveTimestamp))
    } catch (_: Exception) {
        "Hace $days d"
    }
}

private fun getSubscriptionStatusText(role: String, premiumUntil: Long?, isPremiumActive: Boolean, currentTimestamp: Long = System.currentTimeMillis()): String {
    if (role == "admin") return "Acceso Administrador (Vitalicio)"
    if (role == "banned") return "Cuenta Suspendida"
    if (!isPremiumActive) return "Plan Gratuito"
    if (premiumUntil == null || premiumUntil == 0L) return "Premium Vitalicio "

    val diff = premiumUntil - currentTimestamp
    if (diff <= 0) return "Suscripción Expirada"

    val days = diff / (24 * 60 * 60 * 1000L)
    val hours = (diff % (24 * 60 * 60 * 1000L)) / (60 * 60 * 1000L)
    val minutes = (diff % (60 * 60 * 1000L)) / (60 * 1000L)
    val seconds = (diff % (60 * 1000L)) / 1000L

    return when {
        days > 0 -> "Premium: ${days}d ${hours}h ${minutes}m ${seconds}s restantes"
        hours > 0 -> "Premium: ${hours}h ${minutes}m ${seconds}s restantes"
        minutes > 0 -> "Premium: ${minutes}m ${seconds}s restantes"
        else -> "Premium: ${seconds}s restantes"
    }
}

private fun formatExpirationDateDetailed(timestamp: Long?): String {
    if (timestamp == null || timestamp == 0L) return "Vitalicio / Permanente"
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        "Vence: " + sdf.format(Date(timestamp))
    } catch (_: Exception) {
        "Vence: $timestamp"
    }
}

// -------------------------------------------------------------------------------------------------
// MODAL DE GESTIÓN INTEGRAL DE USUARIO (DURACIONES, SLOTS, ROLES, AVATARES)
// -------------------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailManagementDialog(
    user: Map<String, Any>,
    onDismiss: () -> Unit,
    onUserUpdated: (Map<String, Any>) -> Unit,
    onOpenAvatarGift: () -> Unit,
    onReloadAll: () -> Unit
) {
    val context = LocalContext.current
    val uid = user["uid"] as? String ?: ""
    var currentName by remember { mutableStateOf(user["name"] as? String ?: "Sin Nombre") }
    val email = user["email"] as? String ?: ""
    var currentEmailInput by remember { mutableStateOf(email) }
    var currentRole by remember { mutableStateOf(user["role"] as? String ?: "free") }
    var currentSecondaryRole by remember { mutableStateOf(user["secondaryRole"] as? String ?: "none") }
    var currentBanned by remember { mutableStateOf((user["banned"] as? Boolean) == true || currentRole == "banned") }
    var currentVerified by remember { mutableStateOf((user["isVerified"] as? Boolean) == true || (user["verified"] as? Boolean) == true || currentRole == "admin" || currentRole == "moderador") }
    var currentPremiumUntil by remember { mutableStateOf((user["premiumUntil"] as? Number)?.toLong()) }
    val avatarId = user["avatarId"] as? String ?: "default_poro"
    val rankBorder = user["rankBorder"] as? String ?: "NONE"

    var roleToConfirm by remember { mutableStateOf<AppUserRole?>(null) }
    var isChangingRole by remember { mutableStateOf(false) }

    var isProcessing by remember { mutableStateOf(false) }
    var customDaysInput by remember { mutableStateOf("") }
    var showCustomDaysDialog by remember { mutableStateOf(false) }
    var showGiveEssenceDialog by remember { mutableStateOf(false) }
    var showPrivateMessageDialog by remember { mutableStateOf(false) }
    var showUserMessagesViewerDialog by remember { mutableStateOf(false) }

    val registeredDevices = (user["registeredDevices"] as? List<*>) ?: emptyList<Any>()
    var currentDeviceCount by remember { mutableStateOf(registeredDevices.size) }

    val isPremiumActive = when {
        currentRole == "admin" || currentRole == "moderador" -> true
        currentRole in listOf("premium", "creador_vip", "streamer") -> currentPremiumUntil == null || currentPremiumUntil == 0L || currentPremiumUntil!! > System.currentTimeMillis()
        else -> false
    }

    Dialog(
        onDismissRequest = {
            when {
                showCustomDaysDialog -> showCustomDaysDialog = false
                showGiveEssenceDialog -> showGiveEssenceDialog = false
                showPrivateMessageDialog -> showPrivateMessageDialog = false
                showUserMessagesViewerDialog -> showUserMessagesViewerDialog = false
                roleToConfirm != null -> roleToConfirm = null
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
                showCustomDaysDialog -> showCustomDaysDialog = false
                showGiveEssenceDialog -> showGiveEssenceDialog = false
                showPrivateMessageDialog -> showPrivateMessageDialog = false
                showUserMessagesViewerDialog -> showUserMessagesViewerDialog = false
                roleToConfirm != null -> roleToConfirm = null
                else -> onDismiss()
            }
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = HextechDarkBg,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, HextechGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header del Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val secRoleDetailObj = com.example.model.AppUserSecondaryRole.fromId(currentSecondaryRole)
                        val hasSpecialFrameDetail = (currentRole == "admin") || (secRoleDetailObj.frameDrawableRes != null)
                        Box(
                            modifier = Modifier.padding(
                                horizontal = if (hasSpecialFrameDetail) 4.dp else 0.dp,
                                vertical = if (hasSpecialFrameDetail) 2.dp else 0.dp
                            )
                        ) {
                            UserAvatarView(
                                avatarId = avatarId,
                                size = 48.dp,
                                fallbackInitial = currentName.take(1).uppercase(),
                                rankBorder = rankBorder,
                                secondaryRole = currentSecondaryRole,
                                isAdmin = (currentRole == "admin"),
                                fitFrameToSize = true
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = currentName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = HextechGold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                RoleBadge(
                                    role = currentRole,
                                    isPremiumActive = isPremiumActive,
                                    isBanned = currentBanned,
                                    size = RoleBadgeSize.NORMAL
                                )
                                if (currentSecondaryRole != "none" && currentSecondaryRole.isNotBlank()) {
                                    SecondaryRoleBadge(
                                        secondaryRole = currentSecondaryRole,
                                        size = RoleBadgeSize.NORMAL
                                    )
                                }
                            }
                            Text(
                                text = email.ifBlank { "UID: $uid" },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = HextechCardBorder)
                Spacer(modifier = Modifier.height(12.dp))

                // Contenido Scrollable
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // SECCIÓN: EDITAR CORREO ELECTRÓNICO
                    item {
                        Surface(
                            color = HextechSurfaceBg,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Cambiar Correo Electrónico", fontWeight = FontWeight.Bold, color = HextechCyan, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = currentEmailInput,
                                    onValueChange = { currentEmailInput = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    label = { Text("Nuevo Correo Electrónico", color = TextSecondary, fontSize = 11.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = HextechCyan,
                                        unfocusedBorderColor = HextechCardBorder,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        cursorColor = HextechCyan
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        if (currentEmailInput.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(currentEmailInput.trim()).matches()) {
                                            Toast.makeText(context, "Ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        updateUserEmail(context, uid, currentEmailInput.trim()) { newEmail ->
                                            onUserUpdated(user.toMutableMap().apply {
                                                put("email", newEmail)
                                            })
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.End),
                                    colors = ButtonDefaults.buttonColors(containerColor = HextechCyan),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Guardar Correo", color = HextechDarkBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // SECCIÓN 1: ASIGNACIÓN DE SUSCRIPCIÓN PREMIUM
                    item {
                        Surface(
                            color = HextechSurfaceBg,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = HextechGold, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Gestión de Suscripción Premium", fontWeight = FontWeight.Bold, color = HextechGold, fontSize = 13.sp)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Estado actual
                                Surface(
                                    color = HextechDarkBg,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "Estado: ${if (isPremiumActive) "PREMIUM ACTIVO" else "GRATUITO"}",
                                            color = if (isPremiumActive) HextechCyan else TextSecondary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = getSubscriptionStatusText(currentRole, currentPremiumUntil, isPremiumActive),
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                        if (currentPremiumUntil != null && currentPremiumUntil!! > 0L) {
                                            Text(
                                                text = formatExpirationDateDetailed(currentPremiumUntil),
                                                color = HextechGold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Asignar o Extender Tiempo Premium:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(6.dp))

                                // Grid de Duraciones Rápidas
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    DurationButton(
                                        label = "+1 Día",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            applyPremiumDuration(context, uid, 1, isPermanent = false) { newUntil ->
                                                currentRole = "premium"
                                                currentPremiumUntil = newUntil
                                                onUserUpdated(user.toMutableMap().apply {
                                                    put("role", "premium")
                                                    put("premiumUntil", newUntil)
                                                })
                                            }
                                        }
                                    )
                                    DurationButton(
                                        label = "+7 Días",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            applyPremiumDuration(context, uid, 7, isPermanent = false) { newUntil ->
                                                currentRole = "premium"
                                                currentPremiumUntil = newUntil
                                                onUserUpdated(user.toMutableMap().apply {
                                                    put("role", "premium")
                                                    put("premiumUntil", newUntil)
                                                })
                                            }
                                        }
                                    )
                                    DurationButton(
                                        label = "+30 Días",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            applyPremiumDuration(context, uid, 30, isPermanent = false) { newUntil ->
                                                currentRole = "premium"
                                                currentPremiumUntil = newUntil
                                                onUserUpdated(user.toMutableMap().apply {
                                                    put("role", "premium")
                                                    put("premiumUntil", newUntil)
                                                })
                                            }
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    DurationButton(
                                        label = "+90 Días (3m)",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            applyPremiumDuration(context, uid, 90, isPermanent = false) { newUntil ->
                                                currentRole = "premium"
                                                currentPremiumUntil = newUntil
                                                onUserUpdated(user.toMutableMap().apply {
                                                    put("role", "premium")
                                                    put("premiumUntil", newUntil)
                                                })
                                            }
                                        }
                                    )
                                    DurationButton(
                                        label = "+1 Año (365d)",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            applyPremiumDuration(context, uid, 365, isPermanent = false) { newUntil ->
                                                currentRole = "premium"
                                                currentPremiumUntil = newUntil
                                                onUserUpdated(user.toMutableMap().apply {
                                                    put("role", "premium")
                                                    put("premiumUntil", newUntil)
                                                })
                                            }
                                        }
                                    )
                                    DurationButton(
                                        label = "Vitalicio",
                                        modifier = Modifier.weight(1f),
                                        accent = true,
                                        onClick = {
                                            applyPremiumDuration(context, uid, 0, isPermanent = true) {
                                                currentRole = "premium"
                                                currentPremiumUntil = 0L
                                                onUserUpdated(user.toMutableMap().apply {
                                                    put("role", "premium")
                                                    put("premiumUntil", 0L)
                                                })
                                            }
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Personalizado en días
                                    OutlinedButton(
                                        onClick = { showCustomDaysDialog = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HextechCyan),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, HextechCyan.copy(alpha = 0.6f)),
                                        contentPadding = PaddingValues(vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Días Personalizados...", fontSize = 11.sp)
                                    }

                                    // Quitar Premium
                                    OutlinedButton(
                                        onClick = {
                                            removePremiumFromUser(context, uid) {
                                                currentRole = "free"
                                                currentPremiumUntil = null
                                                onUserUpdated(user.toMutableMap().apply {
                                                    put("role", "free")
                                                    put("premiumUntil", 0L)
                                                })
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.6f)),
                                        contentPadding = PaddingValues(vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Quitar Premium", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // SECCIÓN: GESTIÓN Y ASIGNACIÓN DE ROL DE USUARIO
                    item {
                        Surface(
                            color = HextechSurfaceBg,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Badge,
                                            contentDescription = null,
                                            tint = HextechGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Gestión y Cambio de Rol",
                                            fontWeight = FontWeight.Bold,
                                            color = HextechGold,
                                            fontSize = 13.sp
                                        )
                                    }

                                    // Badge animado del rol actual
                                    RoleBadge(
                                        role = currentRole,
                                        isPremiumActive = isPremiumActive,
                                        isBanned = currentBanned,
                                        size = RoleBadgeSize.NORMAL
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Asigna o modifica el rango del usuario en la plataforma. Por directivas de seguridad institucional, la asignación de rol Administrador está excluida.",
                                    color = TextSecondary,
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                if (currentRole == "admin") {
                                    Surface(
                                        color = HextechGold.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, HextechGold.copy(alpha = 0.35f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.AdminPanelSettings,
                                                contentDescription = null,
                                                tint = HextechGold,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Esta cuenta posee el rango de Administrador Maestro protegido. Por directiva de seguridad, no se puede alterar ni degradar su rol desde este panel.",
                                                color = HextechGold,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                } else {
                                    // Lista de roles asignables (EXCLUYENDO ADMIN)
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        AppUserRole.assignableRoles.forEach { targetRole ->
                                            val isSelected = (currentRole.equals(targetRole.id, ignoreCase = true) && (!currentBanned || targetRole == AppUserRole.BANNED))
                                            
                                            Surface(
                                                onClick = {
                                                    if (!isSelected && !isChangingRole) {
                                                        roleToConfirm = targetRole
                                                    }
                                                },
                                                enabled = !isSelected && !isChangingRole,
                                                color = if (isSelected) targetRole.primaryColor.copy(alpha = 0.15f) else HextechDarkBg,
                                                shape = RoundedCornerShape(8.dp),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    width = if (isSelected) 1.2.dp else 0.8.dp,
                                                    color = if (isSelected) targetRole.primaryColor else HextechCardBorder
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text(
                                                            text = targetRole.emoji,
                                                            fontSize = 15.sp,
                                                            modifier = Modifier.padding(end = 8.dp)
                                                        )
                                                        Column {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Text(
                                                                    text = targetRole.displayName,
                                                                    color = if (isSelected) targetRole.primaryColor else TextPrimary,
                                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                                    fontSize = 12.sp
                                                                )
                                                                if (isSelected) {
                                                                    Spacer(modifier = Modifier.width(6.dp))
                                                                    Text(
                                                                        text = "• ACTIVO",
                                                                        color = targetRole.primaryColor,
                                                                        fontSize = 10.sp,
                                                                        fontWeight = FontWeight.ExtraBold
                                                                    )
                                                                }
                                                            }
                                                            Text(
                                                                text = targetRole.description,
                                                                color = TextMuted,
                                                                fontSize = 10.5.sp,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }

                                                    if (isSelected) {
                                                        Icon(
                                                            Icons.Default.CheckCircle,
                                                            contentDescription = null,
                                                            tint = targetRole.primaryColor,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    } else {
                                                        Surface(
                                                            color = targetRole.primaryColor.copy(alpha = 0.12f),
                                                            shape = RoundedCornerShape(4.dp),
                                                            border = androidx.compose.foundation.BorderStroke(0.5.dp, targetRole.primaryColor.copy(alpha = 0.4f))
                                                        ) {
                                                            Text(
                                                                text = "Asignar",
                                                                color = targetRole.primaryColor,
                                                                fontSize = 10.5.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // SECCIÓN: ROL SECUNDARIO (RANGO COMPETITIVO)
                    item {
                        var isSecRolesExpanded by remember { mutableStateOf(false) }
                        val activeSecRoleObj = com.example.model.AppUserSecondaryRole.fromId(currentSecondaryRole)

                        Surface(
                            color = HextechSurfaceBg,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (activeSecRoleObj != com.example.model.AppUserSecondaryRole.NONE) activeSecRoleObj.primaryColor.copy(alpha = 0.5f) else HextechCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isSecRolesExpanded = !isSecRolesExpanded },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.Shield,
                                            contentDescription = null,
                                            tint = if (activeSecRoleObj != com.example.model.AppUserSecondaryRole.NONE) activeSecRoleObj.primaryColor else HextechGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = "Rol Secundario (Rango)",
                                                fontWeight = FontWeight.Bold,
                                                color = if (activeSecRoleObj != com.example.model.AppUserSecondaryRole.NONE) activeSecRoleObj.primaryColor else TextPrimary,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = "Actual: ${activeSecRoleObj.displayName}",
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (activeSecRoleObj != com.example.model.AppUserSecondaryRole.NONE) {
                                            SecondaryRoleBadge(
                                                secondaryRole = currentSecondaryRole,
                                                size = RoleBadgeSize.COMPACT
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Icon(
                                            imageVector = if (isSecRolesExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                AnimatedVisibility(visible = isSecRolesExpanded) {
                                    Column(
                                        modifier = Modifier.padding(top = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Asignar rol secundario / rango competitivo al usuario:",
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                        com.example.model.AppUserSecondaryRole.assignableSecondaryRoles.forEach { targetSecRole ->
                                            val isTargetActive = activeSecRoleObj == targetSecRole
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        if (isTargetActive) return@clickable
                                                        val db = FirebaseFirestore.getInstance()
                                                        db.collection("users").document(uid)
                                                            .set(hashMapOf("secondaryRole" to targetSecRole.id), SetOptions.merge())
                                                            .addOnSuccessListener {
                                                                currentSecondaryRole = targetSecRole.id
                                                                Toast.makeText(context, "Rol secundario actualizado a ${targetSecRole.displayName}", Toast.LENGTH_SHORT).show()
                                                            }
                                                            .addOnFailureListener { e ->
                                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                                            }
                                                    },
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isTargetActive) targetSecRole.primaryColor.copy(alpha = 0.15f) else HextechDarkBg,
                                                border = androidx.compose.foundation.BorderStroke(
                                                    width = if (isTargetActive) 1.5.dp else 0.5.dp,
                                                    color = if (isTargetActive) targetSecRole.primaryColor else HextechCardBorder.copy(alpha = 0.5f)
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(8.dp)
                                                                .clip(CircleShape)
                                                                .background(targetSecRole.primaryColor)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = targetSecRole.displayName,
                                                            color = if (isTargetActive) targetSecRole.primaryColor else TextPrimary,
                                                            fontSize = 12.5.sp,
                                                            fontWeight = if (isTargetActive) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    }
                                                    if (targetSecRole != com.example.model.AppUserSecondaryRole.NONE) {
                                                        SecondaryRoleBadge(
                                                            secondaryRole = targetSecRole.id,
                                                            size = RoleBadgeSize.COMPACT
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // SECCIÓN: VERIFICACIÓN OFICIAL DE CUENTA
                    item {
                        Surface(
                            color = HextechSurfaceBg,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (currentVerified) HextechCyan.copy(alpha = 0.5f) else HextechCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.Verified,
                                            contentDescription = null,
                                            tint = if (currentVerified) HextechCyan else TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Verificación Oficial de Cuenta",
                                            fontWeight = FontWeight.Bold,
                                            color = if (currentVerified) HextechCyan else TextPrimary,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Surface(
                                        color = if (currentVerified) HextechCyan.copy(alpha = 0.15f) else HextechDarkBg,
                                        shape = RoundedCornerShape(4.dp),
                                        border = androidx.compose.foundation.BorderStroke(0.5.dp, if (currentVerified) HextechCyan else HextechCardBorder)
                                    ) {
                                        Text(
                                            text = if (currentVerified) "VERIFICADA" else "NO VERIFICADA",
                                            color = if (currentVerified) HextechCyan else TextMuted,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Otorga o revoca la insignia de cuenta verificada para este invocador. La insignia se muestra junto a su nombre en su perfil y en la gestión de comunidad.",
                                    color = TextSecondary,
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        val newStatus = !currentVerified
                                        updateUserVerification(context, uid, newStatus) {
                                            currentVerified = newStatus
                                            onUserUpdated(user.toMutableMap().apply {
                                                put("isVerified", newStatus)
                                                put("verified", newStatus)
                                            })
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (currentVerified) DangerRed.copy(alpha = 0.18f) else HextechCyan,
                                        contentColor = if (currentVerified) DangerRed else HextechDarkBg
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    border = if (currentVerified) androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)) else null
                                ) {
                                    Icon(
                                        imageVector = if (currentVerified) Icons.Default.Close else Icons.Filled.Verified,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (currentVerified) "Revocar Estado de Verificado" else "Otorgar Estado de Verificado",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // SECCIÓN 2: GESTIÓN DE HARDWARE Y SLOTS DE DISPOSITIVOS
                    item {
                        Surface(
                            color = HextechSurfaceBg,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Devices, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Slots de Hardware y Dispositivos", fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA), fontSize = 13.sp)
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "El usuario tiene $currentDeviceCount de 2 slots de hardware vinculados. Si el usuario cambió de teléfono o tiene problemas de sesión, puedes liberar todos sus slots.",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        resetUserHardwareSlots(context, uid) {
                                            currentDeviceCount = 0
                                            onUserUpdated(user.toMutableMap().apply {
                                                put("registeredDevices", emptyList<String>())
                                                put("sessionToken", "")
                                            })
                                            Toast.makeText(context, "Slots de hardware liberados (0/2 en uso)", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Liberar / Reiniciar Todos los Slots de Hardware", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // SECCIÓN 3: COSMÉTICOS Y REGALOS DE AVATARES
                    item {
                        Surface(
                            color = HextechSurfaceBg,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Palette, contentDescription = null, tint = HextechGold, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Regalos de Avatares y Cosméticos", fontWeight = FontWeight.Bold, color = HextechGold, fontSize = 13.sp)
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Permite desbloquear avatares exclusivos individuales o regalar todo el catálogo de una vez.",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            onDismiss()
                                            onOpenAvatarGift()
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = HextechDarkBg, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Ver Galería de Avatares", color = HextechDarkBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            giftAllAvatarsToUser(context, uid) {
                                                Toast.makeText(context, "¡Todo el catálogo de avatares desbloqueado!", Toast.LENGTH_SHORT).show()
                                                onReloadAll()
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Desbloquear TODO", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Button(
                                    onClick = {
                                        revokeAllExclusiveAvatarsFromUser(context, uid) {
                                            Toast.makeText(context, "¡Regalos de avatares retirados correctamente!", Toast.LENGTH_SHORT).show()
                                            onReloadAll()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.85f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Quitar Regalos de Avatares", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // SECCIÓN 4: SEGURIDAD Y ESTADO DE LA CUENTA
                    item {
                        Surface(
                            color = HextechSurfaceBg,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Seguridad y Estado de la Cuenta", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                if (currentRole == "admin") {
                                    Surface(
                                        color = HextechGold.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, HextechGold.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = HextechGold, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Esta cuenta posee rango de Administrador Maestro protegido.",
                                                color = HextechGold,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                } else {
                                    // Solo opción de suspender/banear o reactivar
                                    Button(
                                        onClick = {
                                            val newBanned = !currentBanned
                                            val newRole = if (newBanned) "banned" else "free"
                                            toggleUserBanStatus(context, uid, newBanned, newRole) {
                                                currentBanned = newBanned
                                                currentRole = newRole
                                                onUserUpdated(user.toMutableMap().apply {
                                                    put("banned", newBanned)
                                                    put("role", newRole)
                                                })
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (currentBanned) Color(0xFF10B981) else DangerRed
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(if (currentBanned) Icons.Default.LockOpen else Icons.Default.Block, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (currentBanned) "Desbanear y Reactivar Cuenta" else "Suspender / Banear Cuenta", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    // SECCIÓN 5: COMUNICACIÓN Y RECOMPENSAS
                    item {
                        Surface(
                            color = HextechSurfaceBg,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFF0EA5E9), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Comunicación y Recompensas", fontWeight = FontWeight.Bold, color = Color(0xFF0EA5E9), fontSize = 13.sp)
                                    }

                                    val currentEssence = (user["blueEssence"] as? Number)?.toLong() ?: 0L
                                    Surface(
                                        color = HextechDarkBg,
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF0EA5E9).copy(alpha = 0.6f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Image(
                                                painter = painterResource(id = com.example.R.drawable.ic_blue_essence),
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "$currentEssence EA",
                                                fontSize = 10.5.sp,
                                                color = Color(0xFF38BDF8),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { showUserMessagesViewerDialog = true },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Message, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Mensajes Privados", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { showGiveEssenceDialog = true },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = com.example.R.drawable.ic_blue_essence),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Dar Esencia Azul", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGiveEssenceDialog) {
        AdminGiveEssenceDialog(
            userUid = uid,
            onDismiss = { showGiveEssenceDialog = false },
            onSuccess = { 
                Toast.makeText(context, "Esencia Azul enviada.", Toast.LENGTH_SHORT).show() 
            }
        )
    }

    if (showUserMessagesViewerDialog) {
        AdminUserMessagesViewerDialog(
            userUid = uid,
            userName = currentName,
            onDismiss = { showUserMessagesViewerDialog = false },
            onOpenSendNewMessage = { showPrivateMessageDialog = true }
        )
    }

    if (showPrivateMessageDialog) {
        AdminPrivateMessageDialog(
            userUid = uid,
            onDismiss = { showPrivateMessageDialog = false },
            onSuccess = { 
                Toast.makeText(context, "Mensaje privado enviado.", Toast.LENGTH_SHORT).show() 
            }
        )
    }

    // Diálogo para ingresar Días Personalizados
    if (showCustomDaysDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDaysDialog = false },
            title = { Text("Días Personalizados de Premium", fontWeight = FontWeight.Bold, color = HextechGold) },
            text = {
                Column {
                    Text("Ingresa el número de días que deseas otorgarle a $currentName:", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customDaysInput,
                        onValueChange = { customDaysInput = it.filter { ch -> ch.isDigit() } },
                        placeholder = { Text("Ej. 15, 45, 180...") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val days = customDaysInput.toIntOrNull()
                        if (days != null && days > 0) {
                            applyPremiumDuration(context, uid, days, isPermanent = false) { newUntil ->
                                currentRole = "premium"
                                currentPremiumUntil = newUntil
                                onUserUpdated(user.toMutableMap().apply {
                                    put("role", "premium")
                                    put("premiumUntil", newUntil)
                                })
                                showCustomDaysDialog = false
                            }
                        } else {
                            Toast.makeText(context, "Ingresa una cantidad válida de días", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold)
                ) {
                    Text("Aplicar Días", color = HextechDarkBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDaysDialog = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }

    // Diálogo de Confirmación para Cambio de Rol
    if (roleToConfirm != null) {
        val target = roleToConfirm!!
        AlertDialog(
            onDismissRequest = { if (!isChangingRole) roleToConfirm = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ManageAccounts,
                        contentDescription = null,
                        tint = target.primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "¿Cambiar rol a ${target.displayName}?",
                        color = HextechGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "¿Confirmas asignar este nuevo rol al usuario '$currentName'?",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = HextechDarkBg,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, target.primaryColor.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "NUEVO ROL ASIGNADO",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            RoleBadge(
                                role = target.id,
                                isPremiumActive = target in listOf(AppUserRole.PREMIUM, AppUserRole.MODERATOR, AppUserRole.CREATOR_VIP, AppUserRole.STREAMER, AppUserRole.CREATOR),
                                isBanned = (target == AppUserRole.BANNED),
                                size = RoleBadgeSize.LARGE
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = target.description,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (target == AppUserRole.BANNED) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Al asignar el rol Baneado, la cuenta del usuario será suspendida de inmediato y no podrá utilizar los servicios de la app.",
                            color = DangerRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else if (target in listOf(AppUserRole.PREMIUM, AppUserRole.MODERATOR, AppUserRole.PATROCINADOR, AppUserRole.CREATOR_VIP, AppUserRole.STREAMER)) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Este rango incluye acceso activo a las herramientas y ventajas del Pase Hextech.",
                            color = HextechCyan,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isChangingRole = true
                        updateUserRoleInCloud(context, uid, target.id) { newRole, isBanned ->
                            isChangingRole = false
                            roleToConfirm = null
                            currentRole = newRole
                            currentBanned = isBanned
                            if (newRole in listOf("premium", "moderador", "patrocinador", "creador", "creador_vip", "streamer")) {
                                currentPremiumUntil = 0L
                            }
                            onUserUpdated(user.toMutableMap().apply {
                                put("role", newRole)
                                put("banned", isBanned)
                                if (newRole in listOf("premium", "moderador", "patrocinador", "creador", "creador_vip", "streamer")) {
                                    put("premiumUntil", 0L)
                                }
                            })
                            onReloadAll()
                        }
                    },
                    enabled = !isChangingRole,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (target == AppUserRole.BANNED) DangerRed else HextechGold
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isChangingRole) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = HextechDarkBg, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "Confirmar y Asignar",
                            color = if (target == AppUserRole.BANNED) Color.White else HextechDarkBg,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!isChangingRole) roleToConfirm = null },
                    enabled = !isChangingRole
                ) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = HextechSurfaceBg,
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
private fun DurationButton(
    label: String,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (accent) HextechGold else HextechDarkBg
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (accent) HextechGold else HextechCardBorder),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (accent) HextechDarkBg else TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// -------------------------------------------------------------------------------------------------
// MODAL DE GALERÍA PARA REGALAR AVATARES
// -------------------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAvatarGiftDialog(
    user: Map<String, Any>,
    onDismiss: () -> Unit,
    onAvatarGifted: (String) -> Unit
) {
    val context = LocalContext.current
    val uid = user["uid"] as? String ?: ""
    val userName = user["name"] as? String ?: "Usuario"
    val unlockedAvatars = remember(user) {
        ((user["unlockedAvatars"] as? List<*>)?.mapNotNull { it?.toString() } ?: listOf("default_poro")).toSet()
    }
    var currentUnlocked by remember { mutableStateOf(unlockedAvatars) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedRarity by remember { mutableStateOf("Todas") }

    // Excluir avatares comunes y clásicos del panel de regalar porque ya están desbloqueados para todos
    val allAvatars = remember {
        AvatarCatalog.avatars.filter { item ->
            val r = item.rarity.lowercase()
            !r.contains("común") && !r.contains("comun") && !r.contains("clásico") && !r.contains("clasico") && !item.isDefault
        }
    }
    val rarities = listOf("Todas", "Raro", "Épico", "Legendario", "Mítico")

    val filteredAvatars = remember(searchQuery, selectedRarity, allAvatars) {
        allAvatars.filter { item ->
            val matchesQuery = searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true) || item.title.contains(searchQuery, ignoreCase = true)
            val matchesRarity = selectedRarity == "Todas" || item.rarity.equals(selectedRarity, ignoreCase = true)
            matchesQuery && matchesRarity
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f),
            shape = RoundedCornerShape(16.dp),
            color = HextechDarkBg,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, HextechGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(listOf(HextechGold, Color(0xFF8B6B23)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = HextechDarkBg, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Regalar Avatar a $userName",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = HextechGold
                            )
                            val unlockedExclusiveCount = currentUnlocked.count { id -> allAvatars.any { it.id == id } }
                            Text(
                                text = "$unlockedExclusiveCount de ${allAvatars.size} exclusivos desbloqueados",
                                style = MaterialTheme.typography.bodySmall,
                                color = HextechCyan,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Buscador y Filtros
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar avatar o campeón...", color = TextMuted, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HextechGold, modifier = Modifier.size(16.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = TextMuted)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HextechGold,
                        unfocusedBorderColor = HextechCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filtros de Rareza
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rarities.forEach { r ->
                        val isSel = selectedRarity == r
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedRarity = r },
                            label = { Text(r, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HextechGold.copy(alpha = 0.25f),
                                selectedLabelColor = HextechGold,
                                containerColor = HextechSurfaceBg,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botones Regalar / Quitar Todo el Catálogo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            giftAllAvatarsToUser(context, uid) {
                                currentUnlocked = allAvatars.map { it.id }.toSet()
                                Toast.makeText(context, "¡Todos los avatares han sido regalados!", Toast.LENGTH_SHORT).show()
                                onAvatarGifted("all")
                            }
                        },
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Regalar Todos (${allAvatars.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                    }

                    Button(
                        onClick = {
                            revokeAllExclusiveAvatarsFromUser(context, uid) {
                                currentUnlocked = setOf("default_poro")
                                Toast.makeText(context, "¡Regalos de avatares retirados!", Toast.LENGTH_SHORT).show()
                                onAvatarGifted("none")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.85f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Quitar Regalos", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cuadrícula de Avatares
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredAvatars, key = { it.id }) { item ->
                        val isAlreadyUnlocked = currentUnlocked.contains(item.id)

                        AvatarGiftCard(
                            item = item,
                            isUnlocked = isAlreadyUnlocked,
                            onGift = {
                                giftSingleAvatarToUser(context, uid, item.id) {
                                    currentUnlocked = currentUnlocked + item.id
                                    Toast.makeText(context, "¡Avatar ${item.name} regalado!", Toast.LENGTH_SHORT).show()
                                    onAvatarGifted(item.id)
                                }
                            },
                            onRevoke = {
                                val currentEquipped = user["avatarId"] as? String
                                revokeSingleAvatarFromUser(context, uid, item.id, currentEquipped) {
                                    currentUnlocked = currentUnlocked - item.id
                                    Toast.makeText(context, "¡Avatar ${item.name} retirado!", Toast.LENGTH_SHORT).show()
                                    onAvatarGifted(item.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarGiftCard(
    item: AvatarItem,
    isUnlocked: Boolean,
    onGift: () -> Unit,
    onRevoke: () -> Unit = {}
) {
    Surface(
        color = HextechSurfaceBg,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isUnlocked) Color(0xFF00FF7F).copy(alpha = 0.5f) else HextechCardBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UserAvatarView(
                avatarId = item.id,
                size = 52.dp,
                fallbackInitial = item.name.take(1)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.name,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = item.rarity,
                fontSize = 9.sp,
                color = HextechGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (isUnlocked) {
                Surface(
                    color = Color(0xFF00FF7F).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Desbloqueado",
                        color = Color(0xFF00FF7F),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onRevoke,
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.RemoveCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Quitar", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onGift,
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Regalar", color = HextechDarkBg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// DIÁLOGO DE ANUNCIO / NOTIFICACIÓN GLOBAL
// -------------------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBroadcastAnnouncementDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isUrgent by remember { mutableStateOf(false) }
    var sendNotification by remember { mutableStateOf(false) }
    var isPublishing by remember { mutableStateOf(false) }
    var isDeactivating by remember { mutableStateOf(false) }

    LaunchedEffect(isUrgent) {
        if (isUrgent) {
            sendNotification = true
        }
    }

    val activeAnnouncement by com.example.data.GlobalAnnouncementManager.currentAnnouncement.collectAsState()
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Campaign, contentDescription = null, tint = HextechGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Publicar Anuncio Global", fontWeight = FontWeight.Bold, color = HextechGold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Este anuncio se enviará en tiempo real a todos los dispositivos (con o sin sesión iniciada).",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                // Si hay un anuncio activo actualmente, mostrar ficha con opción de desactivarlo
                if (activeAnnouncement != null && activeAnnouncement!!.active) {
                    val activeAnn = activeAnnouncement!!
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = HextechSurface),
                        border = BorderStroke(1.dp, if (activeAnn.isUrgent) DangerRed else HextechGold.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (activeAnn.isUrgent) DangerRed else Color(0xFF22C55E))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (activeAnn.isUrgent) "ACTIVO (URGENTE)" else "ACTIVO EN DISPOSITIVOS",
                                        color = if (activeAnn.isUrgent) DangerRed else Color(0xFF22C55E),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = activeAnn.getFormattedDate(),
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activeAnn.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = activeAnn.message,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    isDeactivating = true
                                    com.example.data.GlobalAnnouncementManager.deactivateAnnouncement(context) { success, err ->
                                        isDeactivating = false
                                        if (success) {
                                            Toast.makeText(context, "Anuncio global desactivado", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Error al desactivar: $err", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                enabled = !isDeactivating,
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.85f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    if (isDeactivating) "Desactivando..." else "Desactivar Anuncio Actual",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título del Anuncio") },
                    placeholder = { Text("Ej. Nuevo parche 6.0 o Mantenimiento") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Mensaje Detallado") },
                    placeholder = { Text("Escribe el comunicado para los usuarios...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isUrgent = !isUrgent }
                ) {
                    Checkbox(
                        checked = isUrgent,
                        onCheckedChange = { isUrgent = it },
                        colors = CheckboxDefaults.colors(checkedColor = DangerRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Marcar como Urgente / Mantenimiento", color = if (isUrgent) DangerRed else TextSecondary, fontSize = 12.sp)
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { sendNotification = !sendNotification }
                ) {
                    Checkbox(
                        checked = sendNotification,
                        onCheckedChange = { sendNotification = it },
                        colors = CheckboxDefaults.colors(checkedColor = HextechGold)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Enviar notificación a los dispositivos", color = if (sendNotification) HextechGold else TextSecondary, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || message.isBlank()) {
                        Toast.makeText(context, "Por favor completa título y mensaje", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isPublishing = true
                    com.example.data.GlobalAnnouncementManager.publishAnnouncement(
                        context = context,
                        title = title,
                        message = message,
                        isUrgent = isUrgent,
                        sendNotification = sendNotification
                    ) { success, err ->
                        isPublishing = false
                        if (success) {
                            Toast.makeText(context, "¡Anuncio global publicado a todos los dispositivos!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } else {
                            Toast.makeText(context, "Error al publicar: $err", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                enabled = !isPublishing,
                colors = ButtonDefaults.buttonColors(containerColor = HextechGold)
            ) {
                Text(if (isPublishing) "Publicando..." else "Publicar", color = HextechDarkBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextMuted)
            }
        }
    )
}

// -------------------------------------------------------------------------------------------------
// FUNCIONES AUXILIARES DE FIRESTORE PARA ADMINISTRACIÓN
// -------------------------------------------------------------------------------------------------

private fun applyPremiumDuration(
    context: Context,
    uid: String,
    days: Int,
    isPermanent: Boolean,
    onSuccess: (Long) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(uid)

    val calculatedUntil = if (isPermanent) {
        0L // 0 indica permanente/vitalicio
    } else {
        val now = System.currentTimeMillis()
        now + (days.toLong() * 24L * 60L * 60L * 1000L)
    }

    val updatePayload = hashMapOf<String, Any>(
        "role" to "premium",
        "premiumUntil" to calculatedUntil,
        "subscriptionPlan" to if (isPermanent) "Admin Vitalicio" else "Admin Grant ($days días)",
        "lastModifiedByAdmin" to System.currentTimeMillis()
    )

    userRef.set(updatePayload, SetOptions.merge())
        .addOnSuccessListener {
            val msg = if (isPermanent) "Premium Vitalicio otorgado" else "Premium otorgado por $days días"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            onSuccess(calculatedUntil)
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
}

private fun removePremiumFromUser(
    context: Context,
    uid: String,
    onSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val updatePayload = hashMapOf<String, Any>(
        "role" to "free",
        "premiumUntil" to 0L,
        "subscriptionPlan" to "Gratuito",
        "lastModifiedByAdmin" to System.currentTimeMillis()
    )

    db.collection("users").document(uid)
        .set(updatePayload, SetOptions.merge())
        .addOnSuccessListener {
            Toast.makeText(context, "Suscripción revocada (Cambiado a Gratuito)", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
}

private fun updateUserVerification(
    context: Context,
    uid: String,
    isVerified: Boolean,
    onSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val updatePayload = hashMapOf<String, Any>(
        "isVerified" to isVerified,
        "verified" to isVerified,
        "lastModifiedByAdmin" to System.currentTimeMillis()
    )

    db.collection("users").document(uid)
        .set(updatePayload, SetOptions.merge())
        .addOnSuccessListener {
            val msg = if (isVerified) "Verificación otorgada exitosamente" else "Verificación revocada"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            onSuccess()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error al actualizar verificación: ${e.message}", Toast.LENGTH_LONG).show()
        }
}

private fun resetUserHardwareSlots(
    context: Context,
    uid: String,
    onSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val updatePayload = hashMapOf<String, Any>(
        "registeredDevices" to emptyList<String>(),
        "sessionToken" to "",
        "slotsResetTimestamp" to System.currentTimeMillis()
    )

    db.collection("users").document(uid)
        .set(updatePayload, SetOptions.merge())
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error al reiniciar slots: ${e.message}", Toast.LENGTH_LONG).show()
        }
}

private fun giftSingleAvatarToUser(
    context: Context,
    uid: String,
    avatarId: String,
    onSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(uid)

    userRef.update("unlockedAvatars", FieldValue.arrayUnion(avatarId))
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->
            // Si el campo no existía aún, usamos set con merge
            val updateData = hashMapOf<String, Any>(
                "unlockedAvatars" to listOf("default_poro", avatarId)
            )
            userRef.set(updateData, SetOptions.merge())
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { err ->
                    Toast.makeText(context, "Error: ${err.message}", Toast.LENGTH_LONG).show()
                }
        }
}

private fun giftAllAvatarsToUser(
    context: Context,
    uid: String,
    onSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val allIds = AvatarCatalog.avatars.map { it.id }

    val updateData = hashMapOf<String, Any>(
        "unlockedAvatars" to allIds,
        "avatarAllAccessGranted" to true
    )

    db.collection("users").document(uid)
        .set(updateData, SetOptions.merge())
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error al regalar avatares: ${e.message}", Toast.LENGTH_LONG).show()
        }
}

private fun revokeSingleAvatarFromUser(
    context: Context,
    uid: String,
    avatarId: String,
    currentEquippedAvatarId: String? = null,
    onSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(uid)

    val updates = mutableMapOf<String, Any>(
        "unlockedAvatars" to FieldValue.arrayRemove(avatarId),
        "avatarAllAccessGranted" to false
    )
    if (currentEquippedAvatarId == avatarId) {
        updates["avatarId"] = "default_poro"
    }

    userRef.update(updates)
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error al quitar avatar: ${e.message}", Toast.LENGTH_LONG).show()
        }
}

private fun revokeAllExclusiveAvatarsFromUser(
    context: Context,
    uid: String,
    onSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(uid)

    val defaultAvatars = listOf("default_poro")
    val updateData = hashMapOf<String, Any>(
        "unlockedAvatars" to defaultAvatars,
        "avatarAllAccessGranted" to false,
        "avatarId" to "default_poro"
    )

    userRef.set(updateData, SetOptions.merge())
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error al remover avatares: ${e.message}", Toast.LENGTH_LONG).show()
        }
}

private fun updateUserRoleInCloud(
    context: Context,
    uid: String,
    targetRoleId: String,
    onSuccess: (newRole: String, isBanned: Boolean) -> Unit
) {
    if (targetRoleId == "admin") {
        Toast.makeText(context, "Operación denegada: No se puede asignar el rol de Administrador por directivas de seguridad.", Toast.LENGTH_LONG).show()
        return
    }

    val db = FirebaseFirestore.getInstance()
    val isBanned = (targetRoleId == "banned")
    val updatePayload = hashMapOf<String, Any>(
        "role" to targetRoleId,
        "banned" to isBanned,
        "last_role_update" to System.currentTimeMillis()
    )

    if (isBanned) {
        updatePayload["bannedTimestamp"] = System.currentTimeMillis()
        updatePayload["sessionToken"] = ""
    } else {
        updatePayload["bannedTimestamp"] = 0L
    }

    // Si el rol es de acceso premium / vitalicio por defecto
    if (targetRoleId in listOf("premium", "moderador", "patrocinador", "creador", "creador_vip", "streamer")) {
        updatePayload["premiumUntil"] = 0L
        updatePayload["subscriptionPlan"] = when (targetRoleId) {
            "moderador" -> "Moderador (Vitalicio)"
            "patrocinador" -> "Patrocinador (Vitalicio)"
            "creador_vip" -> "Creador VIP (Vitalicio)"
            "creador" -> "Creador (Vitalicio)"
            "streamer" -> "Streamer (Vitalicio)"
            else -> "Premium Vitalicio"
        }
        updatePayload["is_premium"] = true
    } else if (targetRoleId == "free") {
        updatePayload["is_premium"] = false
    }

    db.collection("users").document(uid)
        .set(updatePayload, SetOptions.merge())
        .addOnSuccessListener {
            val roleName = AppUserRole.fromId(targetRoleId).displayName
            Toast.makeText(context, "Rol actualizado a $roleName", Toast.LENGTH_SHORT).show()
            onSuccess(targetRoleId, isBanned)
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error al actualizar rol: ${e.message}", Toast.LENGTH_LONG).show()
        }
}

private fun toggleUserBanStatus(
    context: Context,
    uid: String,
    isBanned: Boolean,
    newRole: String,
    onSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val updatePayload = hashMapOf<String, Any>(
        "banned" to isBanned,
        "role" to newRole,
        "bannedTimestamp" to if (isBanned) System.currentTimeMillis() else 0L
    )

    db.collection("users").document(uid)
        .set(updatePayload, SetOptions.merge())
        .addOnSuccessListener {
            Toast.makeText(context, if (isBanned) "Usuario BANEADO" else "Usuario Desbaneado", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
}


@Composable
private fun ServerScraperHealthCard() {
    val context = LocalContext.current
    val sourceStatuses by BestBuildWrScraper.sourceStatuses.collectAsState()
    val globalStatus by BestBuildWrScraper.globalSyncStatus.collectAsState()
    val isSyncing by BestBuildWrScraper.isSyncing.collectAsState()

    // Actualización reactiva constante en tiempo real en segundo plano
    LaunchedEffect(Unit) {
        while (isActive) {
            try {
                BestBuildWrScraper.syncGlobalTierList(context)
            } catch (e: Exception) {
                // Prevenir interrupción
            }
            delay(8000L) // Actualización automática constante cada 8 segundos
        }
    }

    Surface(
        color = HextechSurfaceBg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, HextechCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Monitoreo Multi-Servidor (CN, NA, Global)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(HextechCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isSyncing) HextechGold else Color(0xFF00FF7F))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isSyncing) "Sincronizando..." else "En tiempo real",
                        color = if (isSyncing) HextechGold else HextechCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Estado de Red: $globalStatus",
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            val allSources = sourceStatuses.values.toList()
            allSources.forEach { status ->
                val regionPrefix = when (status.region) {
                    "CN" -> "[CN]"
                    "NA" -> "🌎 [NA]"
                    else -> "🌍 [Global]"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (status.isHealthy) Color(0xFF00FF7F) else Color(0xFFFF5252))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$regionPrefix ${status.name}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${status.responseTimeMs} ms",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = if (status.isHealthy) Color(0xFF2E7D32).copy(alpha = 0.2f) else Color(0xFFC62828).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.5.dp, if (status.isHealthy) Color(0xFF81C784) else Color(0xFFEF9A9A))
                        ) {
                            Text(
                                text = if (status.isHealthy) "OPERATIVO (OK)" else (status.errorMessage ?: "ERROR"),
                                color = if (status.isHealthy) Color(0xFF81C784) else Color(0xFFEF9A9A),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun updateUserEmail(
    context: Context,
    uid: String,
    newEmail: String,
    onSuccess: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val updatePayload = hashMapOf<String, Any>(
        "email" to newEmail.trim(),
        "lastModifiedByAdmin" to System.currentTimeMillis()
    )
    db.collection("users").document(uid)
        .set(updatePayload, SetOptions.merge())
        .addOnSuccessListener {
            Toast.makeText(context, "Correo electrónico actualizado exitosamente", Toast.LENGTH_SHORT).show()
            onSuccess(newEmail.trim())
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error al actualizar correo: ${e.message}", Toast.LENGTH_LONG).show()
        }
}
