package com.example.ui.auth

import androidx.compose.runtime.DisposableEffect

import com.example.ui.components.CoachButton as Button
import com.example.ui.components.CoachTextButton as TextButton
import com.example.ui.components.CoachIconButton as IconButton

import android.widget.Toast
import com.example.ui.theme.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer

import com.example.ui.components.coachClickable
import androidx.compose.foundation.border

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.Modifier
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.example.util.SubscriptionManager
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import com.example.ui.theme.AppTheme
import com.example.ui.theme.AppThemeManager
import com.example.ui.components.RoleBadge
import com.example.ui.components.RoleBadgeSize
import com.example.data.AvatarCatalog
import com.example.ui.components.UserAvatarView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.components.AvatarSelectionBottomSheet
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechSurface
import com.example.util.AuthManager
import androidx.compose.material.icons.filled.Videocam
import com.example.util.tr

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AuthFlowContainer(
    viewModel: AuthViewModel = viewModel(),
    onLoginSuccess: (() -> Unit)? = null
) {
    val auth = AuthManager.getAuth()
    val context = LocalContext.current
    var currentUser by remember { mutableStateOf(auth?.currentUser) }

    DisposableEffect(auth) {
        val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { changed ->
            // A new login waits for hardware/session registration before showing the profile.
            // A remotely displaced session must immediately show the signed-out flow.
            if (AuthManager.isGuestOrUnauthenticated(changed.currentUser)) currentUser = changed.currentUser
        }
        auth?.addAuthStateListener(listener)
        onDispose { auth?.removeAuthStateListener(listener) }
    }

    LaunchedEffect(Unit) {
        currentUser = auth?.currentUser
    }

    LaunchedEffect(currentUser) {
        SubscriptionManager.init(context)
    }

    // Check if user is already authenticated
    if (currentUser != null && !AuthManager.isGuestOrUnauthenticated(currentUser)) {
        AuthenticatedProfilePanel(
            user = currentUser!!,
            onSignOut = {
                SubscriptionManager.stopHeartbeat(currentUser?.uid)
                auth?.signOut()
                currentUser = null
                viewModel.resetSuccessState()
                com.example.util.GuestAuthHelper.ensureAuth()
            }
        )
        return
    }

    val uiState by viewModel.uiState.collectAsState()

    // Triggered when login/register succeeds to force a recomposition with the new user state and redirect
    val onAuthSuccess: () -> Unit = {
        com.example.util.DeviceAndSessionManager.registerDeviceAndSession(
            context = context,
            onSuccess = {
                currentUser = auth?.currentUser
                com.example.util.SubscriptionManager.init(context)
                if (uiState.deletionCancelled) Toast.makeText(context,
                    context.createConfigurationContext(android.content.res.Configuration(context.resources.configuration).apply {
                        setLocale(com.example.util.AppLanguage.locale(com.example.util.AppLanguage.current.value))
                    }).getString(com.example.R.string.account_delete_cancelled), Toast.LENGTH_LONG).show()
                viewModel.resetSuccessState()
                onLoginSuccess?.invoke()
            },
            onError = { errorMessage ->
                auth?.signOut()
                currentUser = null
                android.widget.Toast.makeText(context, com.example.util.appTr(errorMessage), android.widget.Toast.LENGTH_LONG).show()
                viewModel.resetSuccessState()
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HextechSurface),
            border = BorderStroke(1.dp, HextechCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = uiState.authScreen,
                    transitionSpec = {
                        (slideInHorizontally { width -> if (targetState > initialState) width else -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> if (targetState > initialState) -width else width } + fadeOut()
                        )
                    },
                    label = "auth_screen_transition"
                ) { targetScreen ->
                    when (targetScreen) {
                        AuthScreenType.LOGIN -> LoginScreen(
                            viewModel = viewModel,
                            onNavigateToRegister = { viewModel.navigateTo(AuthScreenType.REGISTER) },
                            onNavigateToForgot = { viewModel.navigateTo(AuthScreenType.FORGOT_PASSWORD) },
                            onLoginSuccess = onAuthSuccess
                        )
                        AuthScreenType.REGISTER -> RegisterScreen(
                            viewModel = viewModel,
                            onNavigateToLogin = { viewModel.navigateTo(AuthScreenType.LOGIN) },
                            onRegisterSuccess = onAuthSuccess
                        )
                        AuthScreenType.FORGOT_PASSWORD -> ForgotPasswordScreen(
                            viewModel = viewModel,
                            onNavigateToLogin = { viewModel.navigateTo(AuthScreenType.LOGIN) }
                        )
                        else -> { }
                    }
                }
            }
        }
    }
}

@Composable
fun AuthenticatedProfilePanel(user: com.google.firebase.auth.FirebaseUser? = null,
    panelNotifications: com.example.data.PanelNotificationState? = null, onSignOut: () -> Unit) {
    val notifications = panelNotifications ?: com.example.ui.components.userPanelNotificationSummary()
    val context = LocalContext.current
    val language = com.example.util.currentAppLanguage()
    val activeTheme = AppThemeManager.currentTheme
    val inboxLabel = tr("Bandeja de Entrada")
    val historyLabel = tr("Historial")
    val isPremium by SubscriptionManager.isPremium.collectAsState()
    val isVerified by SubscriptionManager.isVerified.collectAsState()
    val userRole by SubscriptionManager.userRole.collectAsState()
    val secondaryRole by SubscriptionManager.secondaryRole.collectAsState()
    val adminClaim by AuthManager.isAdminClaim.collectAsState()
    val premiumUntil by SubscriptionManager.premiumUntil.collectAsState()
    val savedUserName by SubscriptionManager.userName.collectAsState()
    val currentAvatarId by SubscriptionManager.currentAvatarId.collectAsState()
    val currentRankBorder by SubscriptionManager.currentRankBorder.collectAsState()
    val hasImageFrame = currentRankBorder.isNotBlank() && currentRankBorder.uppercase(java.util.Locale.ROOT) !in setOf("NONE", "DEFAULT")
    val hasRoleFrame = userRole in listOf("admin", "moderador", "creador", "creador_lvl2", "creador_lvl3", "creador_lvl4", "creador_lvl5", "streamer") || secondaryRole.isNotBlank() || hasImageFrame
    var showAvatarDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPlansDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showInboxDialog by remember { mutableStateOf(false) }
    var showAdminDashboard by remember { mutableStateOf(false) }
    var showModeratorDashboard by remember { mutableStateOf(false) }
    var showAdminCreatorDialog by remember { mutableStateOf(false) }
    var showStreamerPanel by remember { mutableStateOf(false) }
    var showSupportPanel by remember { mutableStateOf(false) }
    var showSponsorPanel by remember { mutableStateOf(false) }
    var showSponsorModerationDialog by remember { mutableStateOf(false) }
    var showBuyEssenceDialog by remember { mutableStateOf(false) }
    var showRedemptionDialog by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }
    val activeProfile by com.example.data.AccountProfileManager.activeProfile.collectAsState()

    val effectiveUserUid = user?.uid ?: activeProfile.id.ifBlank { "local-profile-test" }
    val effectiveEmail = user?.email ?: "coach@example.invalid"


    if (showRedemptionDialog) { com.example.ui.components.OrangeEssenceRedemptionDialog { showRedemptionDialog = false } }

    if (showAvatarDialog) {
        com.example.ui.components.AvatarSelectionBottomSheet(
            onDismiss = { showAvatarDialog = false },
            onOpenPremiumPlans = {
                showAvatarDialog = false
                showPlansDialog = true
            }
        )
    }

    if (showThemeDialog) {
        com.example.ui.components.ThemeCustomizationBottomSheet(
            isPremium = isPremium,
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

    if (showAdminDashboard) {
        com.example.ui.components.AdminDashboardDialog(
            onDismiss = { showAdminDashboard = false }
        )
    }

    if (showStreamerPanel) { com.example.ui.components.StreamerPanelDialog(onDismiss = { showStreamerPanel = false }) }

    if (showModeratorDashboard) {
        com.example.ui.components.ModeratorDashboardDialog(
            onDismiss = { showModeratorDashboard = false }
        )
    }

    if (showAdminCreatorDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showAdminCreatorDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(Modifier.fillMaxSize().background(HextechSurface).safeDrawingPadding()) {
                com.example.ui.components.AdminCreatorBuildsDialog(
                    onDismiss = { showAdminCreatorDialog = false }
                )
            }
        }
    }

    if (showSupportPanel) {
        com.example.ui.components.AdminFeedbackBottomSheet(
            onDismiss = { showSupportPanel = false }
        )
    }

    if (showSponsorPanel) {
        com.example.ui.components.SponsorCpmPanelDialog(
            onDismiss = { showSponsorPanel = false }
        )
    }

    if (showSponsorModerationDialog) {
        com.example.ui.components.AdminSponsorModerationDialog(
            onDismiss = { showSponsorModerationDialog = false }
        )
    }

    if (showBuyEssenceDialog) {
        com.example.ui.components.BlueEssenceStoreDialog(
            profileId = activeProfile.id,
            onDismiss = { showBuyEssenceDialog = false }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var showBuyEssenceDialog by remember { mutableStateOf(false) }
            var buyEssenceCurrency by remember { mutableStateOf("BLUE") }

            com.example.ui.components.EconomyPendingStatus(alwaysVisible = true)

            // Top Row with Inbox (top-left), Blue Essence (top-center), and History (top-right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Top-Left: Inbox button
                com.example.ui.components.CircularPanelNotificationButton(
                    notifications.count(com.example.data.NotificationPanel.INBOX), com.example.data.NotificationPanel.INBOX,
                    inboxLabel, Icons.Default.Message) { showInboxDialog = true }

                // Top-Center: Blue Essence & Orange Essence side-by-side badges
                val currentBlueEssence by SubscriptionManager.blueEssence.collectAsState()
                val currentOrangeEssence by SubscriptionManager.orangeEssence.collectAsState()
                var blueBounce by remember { mutableStateOf(false) }
                val blueScale by animateFloatAsState(
                    targetValue = if (blueBounce && !com.example.ui.components.LocalCoachButtonAnimation.current) 1.08f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = com.example.ui.components.COACH_BUTTON_SPRING_STIFFNESS),
                    label = "blueScale",
                    finishedListener = { blueBounce = false }
                )
                var orangeBounce by remember { mutableStateOf(false) }
                val orangeScale by animateFloatAsState(
                    targetValue = if (orangeBounce && !com.example.ui.components.LocalCoachButtonAnimation.current) 1.08f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = com.example.ui.components.COACH_BUTTON_SPRING_STIFFNESS),
                    label = "orangeScale",
                    finishedListener = { orangeBounce = false }
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Blue Essence compact badge
                    Surface(
                        modifier = Modifier
                            .height(38.dp)
                            .graphicsLayer {
                                scaleX = blueScale
                                scaleY = blueScale
                            }
                            .tactileClickable {
                                blueBounce = true
                                buyEssenceCurrency = "BLUE"
                                showBuyEssenceDialog = true
                            },
                        shape = RoundedCornerShape(19.dp),
                        color = activeTheme.surfaceVariant.copy(alpha = 0.9f),
                        border = BorderStroke(1.2.dp, activeTheme.primary.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = com.example.R.drawable.ic_blue_essence),
                                contentDescription = tr("Esencia Azul"),
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = com.example.util.tr("$currentBlueEssence " + tr("EA")),
                                color = HextechCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Orange Essence compact badge
                    Surface(
                        modifier = Modifier
                            .height(38.dp)
                            .graphicsLayer {
                                scaleX = orangeScale
                                scaleY = orangeScale
                            }
                            .tactileClickable {
                                orangeBounce = true
                                buyEssenceCurrency = "ORANGE"
                                showBuyEssenceDialog = true
                            },
                        shape = RoundedCornerShape(19.dp),
                        color = activeTheme.surfaceVariant.copy(alpha = 0.9f),
                        border = BorderStroke(1.2.dp, Color(0xFFFF8C00).copy(alpha = 0.7f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = com.example.R.drawable.ic_orange_essence),
                                contentDescription = tr("Esencia Naranja"),
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = com.example.util.tr("$currentOrangeEssence " + tr("EN")),
                                color = Color(0xFFFF9E1B),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Top-Right: History button
                com.example.ui.components.CircularPanelNotificationButton(
                    notifications.count(com.example.data.NotificationPanel.HISTORY), com.example.data.NotificationPanel.HISTORY,
                    historyLabel, Icons.Default.History) { showHistoryDialog = true }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val redemptionBalance by SubscriptionManager.orangeEssence.collectAsState()
            if (redemptionBalance > 0L && com.example.model.RolePanelAccess.canRedeemEssence(userRole, secondaryRole, adminClaim)) {
                com.example.ui.components.OrangeEssenceRedemptionEntry(redemptionBalance) { showRedemptionDialog = true }
            }

            AuthHeader(
                title = "Perfil de Invocador",
                subtitle = "Sesión iniciada correctamente",
                compact = true
            )

            Spacer(modifier = Modifier.height(if (hasRoleFrame) 2.dp else 8.dp))

            if (showInboxDialog) {
                com.example.ui.components.UserInboxDialog(
                    userUid = effectiveUserUid,
                    onDismiss = { showInboxDialog = false }
                )
            }
            if (showBuyEssenceDialog) {
                com.example.ui.components.BuyEssenceDialog(
                    isAdmin = userRole == "admin" || AuthManager.isCurrentUserAdmin(),
                    initialCurrency = buyEssenceCurrency,
                    onDismiss = { showBuyEssenceDialog = false }
                )
            }

            val isAdminUser = com.example.model.RolePanelAccess.isAdministrator(userRole, adminClaim)
            var showPurchaseHistoryDialog by remember { mutableStateOf(false) }
            var showVerifiedInfoDialog by remember { mutableStateOf(false) }

            if (showPurchaseHistoryDialog) {
                com.example.ui.components.PurchaseHistoryDialog(
                    isAdmin = isAdminUser,
                    onDismiss = { showPurchaseHistoryDialog = false }
                )
            }

            if (showVerifiedInfoDialog) {
                AlertDialog(
                    onDismissRequest = { showVerifiedInfoDialog = false },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(activeTheme.primary.copy(alpha = 0.15f))
                                .border(1.5.dp, activeTheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = tr("Verificado"),
                                tint = activeTheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    title = {
                        Text(
                            text = tr("Cuenta Verificada"),
                            color = activeTheme.secondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = tr("Esta cuenta de invocador se encuentra verificada y autenticada oficialmente en el sistema."),
                                color = TextPrimary,
                                fontSize = 14.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = activeTheme.primary.copy(alpha = 0.10f),
                                border = BorderStroke(1.dp, activeTheme.primary.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Verified,
                                        contentDescription = null,
                                        tint = activeTheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = tr("Estado: Perfil auténtico, protegido y sincronizado."),
                                        color = activeTheme.primary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showVerifiedInfoDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = activeTheme.primary,
                                contentColor = activeTheme.background
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(tr("Entendido"), fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = activeTheme.surface,
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 6.dp
                )
            }

            // Summoner Crest Avatar
            val finalUserName = savedUserName.takeIf { it.isNotBlank() } ?: "Invocador"

            val equippedAvatar = AvatarCatalog.getAvatarById(currentAvatarId)

            val activeTheme = AppThemeManager.currentTheme

            // Animated Runic Sweep Aura around Avatar
            val avatarTransition = rememberInfiniteTransition(label = "AvatarHalo")
            val haloRotation by avatarTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 10000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "haloRotation"
            )
            val haloPulse by avatarTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.06f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "haloPulse"
            )

            var avatarTapped by remember { mutableStateOf(false) }
            val avatarScale by animateFloatAsState(
                targetValue = if (avatarTapped) 0.92f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = com.example.ui.components.COACH_BUTTON_SPRING_STIFFNESS),
                label = "avatarScale",
                finishedListener = { avatarTapped = false }
            )

            // Avatar in center
            Box(
                modifier = Modifier
                    .size(if (hasImageFrame) 180.dp else if (hasRoleFrame) 140.dp else 92.dp)
                    .testTag("profile_avatar_frame_area")
                    .padding(
                        top = if (hasRoleFrame) 0.dp else 6.dp,
                        bottom = if (hasRoleFrame) 0.dp else 6.dp,
                        start = if (hasRoleFrame) 0.dp else 8.dp,
                        end = if (hasRoleFrame) 0.dp else 8.dp
                    )
                    .graphicsLayer {
                        scaleX = avatarScale
                        scaleY = avatarScale
                    }
                    .tactileClickable {
                        avatarTapped = true
                        showAvatarDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                if (!hasRoleFrame) {
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .graphicsLayer {
                                scaleX = haloPulse
                                scaleY = haloPulse
                            }
                            .rotate(haloRotation)
                            .border(
                                width = 2.dp,
                                brush = Brush.sweepGradient(
                                    listOf(
                                        activeTheme.primary,
                                        activeTheme.secondary,
                                        activeTheme.primaryGlow,
                                        activeTheme.primary
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                }

                UserAvatarView(
                    avatarId = currentAvatarId,
                    rankBorder = currentRankBorder,
                    modifier = Modifier.testTag("profile_avatar"),
                    size = if (hasRoleFrame) 140.dp else 76.dp,
                    frameScale = if (hasImageFrame) 1.40f else 1f,
                    fallbackInitial = finalUserName,
                    isAdmin = isAdminUser,
                    secondaryRole = secondaryRole,
                    isCurrentUser = true
                )
                // Botón interactivo de cambio de avatar (Lápiz)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(
                            x = if (hasImageFrame) (-16).dp else if (hasRoleFrame) (-6).dp else 2.dp,
                            y = if (hasImageFrame) (-16).dp else if (hasRoleFrame) (-6).dp else 2.dp
                        )
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(activeTheme.secondary)
                        .border(1.5.dp, activeTheme.background, CircleShape)
                        .coachClickable(
                            role = androidx.compose.ui.semantics.Role.Button,
                            onClick = {
                                avatarTapped = true
                                showAvatarDialog = true
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = tr("Cambiar Avatar"),
                        tint = activeTheme.background,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (hasImageFrame) 6.dp else 12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = com.example.util.tr(finalUserName),
                    modifier = Modifier.testTag("profile_user_name"),
                    color = activeTheme.secondary,
                    fontSize = 20.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )
                if (isVerified || isAdminUser || userRole == "moderador") {
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { showVerifiedInfoDialog = true },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = tr("Cuenta Verificada - Toca para más información"),
                            tint = activeTheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Rol visualizado directamente debajo del usuario, únicamente el rol sin tanto contexto
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Top
            ) {
                RoleBadge(
                    role = userRole,
                    isPremiumActive = isPremium,
                    isBanned = (userRole == "banned"),
                    isExpiringSoon = isPremium && !com.example.model.PremiumAccessPolicy.isLifetime(userRole, secondaryRole, adminClaim) && com.example.model.PremiumAccessPolicy.isExpiringSoon(premiumUntil, System.currentTimeMillis()),
                    size = RoleBadgeSize.NORMAL
                )
                if (secondaryRole.isNotBlank()) {
                    RoleBadge(
                        role = secondaryRole,
                        isPremiumActive = false,
                        isBanned = false,
                        isExpiringSoon = false,
                        size = RoleBadgeSize.NORMAL
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            com.example.ui.components.PremiumStatusCard(userRole, secondaryRole, premiumUntil, adminClaim,
                banned = SubscriptionManager.isBanned.collectAsState().value, granted = isPremium,
                onRenew = { showPlansDialog = true })
            Spacer(modifier = Modifier.height(10.dp))

            // Avatar Title & Region subtitle
            Text(
                text = com.example.util.tr("${tr(equippedAvatar.title)} • ${tr(equippedAvatar.region)}"),
                color = activeTheme.primary,
                fontSize = 12.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )

            // Dynamic Region Badge reflecting current theme
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = activeTheme.primary.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, activeTheme.primary.copy(alpha = 0.5f)),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = com.example.util.tr("${tr(activeTheme.regionTag).uppercase()} • ${tr(activeTheme.titleKey)}"),
                        color = activeTheme.primaryLight,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            var isEmailVisible by remember { mutableStateOf(false) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .tactileClickable { isEmailVisible = !isEmailVisible },
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = com.example.util.tr(if (isEmailVisible) effectiveEmail else "••••••••@••••.com"),
                    color = activeTheme.textSecondary,
                    fontSize = 13.5.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = activeTheme.textMuted,
                    modifier = Modifier.size(15.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Connected Devices panel right below email
            var registeredDevicesCount by remember { mutableStateOf(1) }
            var isSecurityExpanded by remember { mutableStateOf(false) }
            LaunchedEffect(effectiveUserUid) {
                if (effectiveUserUid.isNotBlank() && com.google.firebase.FirebaseApp.getApps(context).isNotEmpty()) {
                    try {
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(effectiveUserUid)
                            .get()
                            .addOnSuccessListener { doc ->
                                val devs = doc.get("registeredDevices") as? List<*> ?: emptyList<Any>()
                                registeredDevicesCount = devs.size.coerceAtLeast(1)
                            }
                    } catch (_: Exception) {}
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = activeTheme.surfaceVariant.copy(alpha = 0.55f)),
                border = BorderStroke(1.dp, activeTheme.cardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .tactileClickable { isSecurityExpanded = !isSecurityExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = com.example.util.tr("📱 " + tr("Dispositivos Conectados:")),
                                color = activeTheme.secondary,
                                fontSize = 12.5.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(activeTheme.primary.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = com.example.util.tr("$registeredDevicesCount " + tr("de 2 en uso")),
                                    color = activeTheme.primary,
                                    fontSize = 11.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                            }
                        }
                        Icon(
                            imageVector = if (isSecurityExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = activeTheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    AnimatedVisibility(visible = isSecurityExpanded) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            Text(
                                text = com.example.util.tr("🔒 " + com.example.util.tr("Por seguridad de tu cuenta, la liberación y reasignación de slots de hardware es gestionada exclusivamente por los Administradores desde el panel de soporte.")),
                                color = activeTheme.textSecondary,
                                fontSize = 10.5.sp,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = com.example.util.tr("⚠️ " + com.example.util.tr("Recomendación: Se recomienda no cerrar sesión para evitar un mal funcionamiento o problemas a futuro con tu cuenta, sincronización de licencias y el acceso fluido a tus herramientas de drafting.")),
                                color = activeTheme.textMuted,
                                fontSize = 10.5.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val isUserPremium = isPremium
            if (isUserPremium) {
                // Quick Theme Selector Strip: Instant 1-tap live theme transformation with horizontal scroll!
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(activeTheme.surfaceVariant.copy(alpha = 0.6f))
                        .border(1.dp, activeTheme.cardBorder, RoundedCornerShape(14.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Palette,
                                contentDescription = null,
                                tint = activeTheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = tr("Tema de Región:"),
                                color = activeTheme.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tr(activeTheme.titleKey),
                                color = activeTheme.secondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(activeTheme.primary.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = com.example.util.tr("↔ " + tr("Desliza temas")),
                                color = activeTheme.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Scrollable row of all region and thematic game styles
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val allThemes = AppTheme.entries
                        items(allThemes.size) { index ->
                            val themeItem = allThemes[index]
                            val isSelected = themeItem == activeTheme
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) themeItem.primary.copy(alpha = 0.25f) else themeItem.surface,
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 0.8.dp,
                                    if (isSelected) themeItem.secondary else themeItem.cardBorder.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier
                                    .tactileClickable(scaleDown = 0.92f) {
                                        AppThemeManager.setTheme(themeItem, context)
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(themeItem.primary)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = tr(themeItem.titleKey),
                                        color = if (isSelected) themeItem.secondary else themeItem.textSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = themeItem.secondary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Consejo del Coach Soberano colocado DIRECTAMENTE debajo de los temas de regiones
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = activeTheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, activeTheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .tactileClickable { }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(com.example.util.tr("💡"), fontSize = 22.sp)
                        Column {
                            Text(
                                text = tr("Consejo del Coach Soberano"),
                                color = activeTheme.secondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tr("¡Cada región altera la energía y colores de la interfaz! Selecciona tu región favorita para sincronizar tu estilo competitivo."),
                                color = activeTheme.textSecondary,
                                fontSize = 10.5.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }


            // Animated button for Plans
            com.example.ui.components.HextechAnimatedButton(
                onClick = { showPlansDialog = true },
                backgroundBrush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    listOf(
                        activeTheme.primary.copy(alpha = 0.22f),
                        activeTheme.secondary.copy(alpha = 0.22f)
                    )
                ),
                borderColor = activeTheme.primary,
                glowColor = activeTheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(10.dp),
                enableShimmer = true,
                enablePulse = true
            ) {
                Icon(
                    imageVector = Icons.Default.LocalActivity,
                    contentDescription = null,
                    tint = activeTheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = com.example.util.tr(if (isPremium) tr("Planes / Pase") else tr("Ver Planes Pro")),
                    modifier = Modifier.weight(1f, fill = false),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = activeTheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Spacer(Modifier.width(8.dp))
                com.example.ui.components.PanelNotificationBadge(notifications.count(com.example.data.NotificationPanel.PLANS), com.example.data.NotificationPanel.PLANS)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botón Creador (Panel de Usuario)
            com.example.ui.components.HextechAnimatedButton(
                onClick = {
                    showAdminCreatorDialog = true
                },
                backgroundBrush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    listOf(HextechGold, Color(0xFFD4AF37))
                ),
                borderColor = HextechCyan,
                glowColor = HextechGold,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(10.dp),
                enableShimmer = true,
                enablePulse = true
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = HextechDarkBg,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tr("Creador"),
                    modifier = Modifier.weight(1f, fill = false),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = HextechDarkBg,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(Modifier.width(8.dp))
                com.example.ui.components.PanelNotificationBadge(notifications.count(com.example.data.NotificationPanel.CREATOR), com.example.data.NotificationPanel.CREATOR)
            }

            if (showHistoryDialog) {
                com.example.ui.components.SubscriptionHistoryDialog(
                    userId = effectiveUserUid,
                    userEmail = effectiveEmail,
                    onDismiss = { showHistoryDialog = false }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (com.example.model.RolePanelAccess.canOpen(com.example.model.RolePanel.STREAMER, userRole, secondaryRole, adminClaim)) {
                com.example.ui.components.HextechAnimatedButton(
                    onClick = { showStreamerPanel = true },
                    backgroundBrush = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFF4C1D95))),
                    borderColor = HextechGold, glowColor = Color(0xFFA855F7),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(12.dp), enableShimmer = true, enablePulse = true
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(com.example.util.localizedString(com.example.R.string.streamer_panel), modifier = Modifier.weight(1f, fill = false), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    com.example.ui.components.PanelNotificationBadge(notifications.count(com.example.data.NotificationPanel.STREAMER), com.example.data.NotificationPanel.STREAMER)
                }
                Spacer(Modifier.height(10.dp))
            }

            if (isAdminUser) {
                // Panel de Administración / Gestión (Solo para Administradores)
                com.example.ui.components.HextechAnimatedButton(
                    onClick = { showAdminDashboard = true },
                    backgroundBrush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(com.example.ui.theme.DangerRed, Color(0xFFB91C1C))
                    ),
                    borderColor = com.example.ui.theme.HextechGold,
                    glowColor = com.example.ui.theme.DangerRed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enableShimmer = true,
                    enablePulse = true
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tr("Panel de Administración"),
                    modifier = Modifier.weight(1f, fill = false),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = Color.White,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    com.example.ui.components.PanelNotificationBadge(notifications.count(com.example.data.NotificationPanel.ADMINISTRATION), com.example.data.NotificationPanel.ADMINISTRATION)
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Panel de Moderador / Patrocinios (Solo para Administradores)
                com.example.ui.components.HextechAnimatedButton(
                    onClick = { showSponsorModerationDialog = true },
                    backgroundBrush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(Color(0xFFF97316), Color(0xFFEA580C))
                    ),
                    borderColor = com.example.ui.theme.HextechGold,
                    glowColor = Color(0xFFF97316),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enableShimmer = true,
                    enablePulse = true
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White,
                            modifier = Modifier.size(24.dp).testTag("moderator_panel_icon"))
                        Text(text = tr("Panel de Moderador"),
                            modifier = Modifier.padding(start = 8.dp).weight(1f, fill = false).testTag("moderator_panel_title"),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        com.example.ui.components.PanelNotificationBadge(notifications.count(com.example.data.NotificationPanel.SPONSOR_MODERATION), com.example.data.NotificationPanel.SPONSOR_MODERATION)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            if (com.example.model.RolePanelAccess.canOpen(com.example.model.RolePanel.MODERATION, userRole, secondaryRole, adminClaim)) {
                // Panel de Soporte y Moderación Exclusivo de Moderadores
                com.example.ui.components.HextechAnimatedButton(
                    onClick = { showModeratorDashboard = true },
                    backgroundBrush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(com.example.ui.theme.HextechCyan, Color(0xFF2563EB))
                    ),
                    borderColor = com.example.ui.theme.HextechGold,
                    glowColor = com.example.ui.theme.HextechCyan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enableShimmer = true,
                    enablePulse = true
                ) {
                    Icon(
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tr("Panel de Soporte y Moderación"),
                    modifier = Modifier.weight(1f, fill = false),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = Color.White,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    com.example.ui.components.PanelNotificationBadge(notifications.count(com.example.data.NotificationPanel.SUPPORT), com.example.data.NotificationPanel.SUPPORT)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (com.example.model.RolePanelAccess.canOpen(com.example.model.RolePanel.SPONSOR, userRole, secondaryRole, adminClaim)) {
                com.example.ui.components.HextechAnimatedButton(
                    onClick = { showSponsorPanel = true },
                    backgroundBrush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(Color(0xFFF97316), Color(0xFFEA580C))
                    ),
                    borderColor = com.example.ui.theme.HextechGold,
                    glowColor = Color(0xFFF97316),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enableShimmer = true,
                    enablePulse = true
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(tr("Panel de Patrocinador"), modifier = Modifier.weight(1f, fill = false), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    com.example.ui.components.PanelNotificationBadge(notifications.count(com.example.data.NotificationPanel.SPONSOR), com.example.data.NotificationPanel.SPONSOR)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            com.example.ui.components.AccountDeletionCard(
                email = effectiveEmail,
                submit = { com.example.data.AccountDeletionRepository.request(it) },
                onScheduled = { deadline ->
                    val resources = context.createConfigurationContext(android.content.res.Configuration(context.resources.configuration).apply {
                        setLocale(com.example.util.AppLanguage.locale(com.example.util.AppLanguage.current.value))
                    }).resources
                    val date = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT,
                        com.example.util.AppLanguage.locale(com.example.util.AppLanguage.current.value)).format(java.util.Date(deadline))
                    Toast.makeText(context, resources.getString(com.example.R.string.account_delete_scheduled, date), Toast.LENGTH_LONG).show()
                    onSignOut()
                }
            )
            Spacer(Modifier.height(12.dp))

            com.example.ui.components.HextechAnimatedOutlinedButton(
                onClick = { showSignOutConfirm = true },
                backgroundColor = com.example.ui.theme.HextechSurfaceVariant.copy(alpha = 0.5f),
                borderColor = DangerRed.copy(alpha = 0.65f),
                glowColor = DangerRed,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(12.dp),
                scaleDown = 0.92f
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = DangerRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(tr("Cerrar Sesión"), color = DangerRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text(tr("Cerrar Sesión"), color = DangerRed, fontWeight = FontWeight.Bold) },
            text = { Text(tr("¿Estás seguro de que deseas cerrar sesión? Se recomienda mantener la sesión abierta para asegurar la sincronización correcta de tu cuenta y licencias."), color = activeTheme.textSecondary, fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirm = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text(tr("Cerrar Sesión"), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) {
                    Text(tr("Cancelar"), color = activeTheme.textMuted)
                }
            },
            containerColor = activeTheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun Modifier.tactileClickable(scaleDown: Float = 0.94f, onClick: () -> Unit): Modifier =
    this.coachClickable(onClick = onClick)
