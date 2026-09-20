package com.example.ui.auth

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

import androidx.compose.foundation.clickable
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AuthFlowContainer(
    viewModel: AuthViewModel = viewModel(),
    onLoginSuccess: (() -> Unit)? = null
) {
    val auth = AuthManager.getAuth()
    val context = LocalContext.current
    var currentUser by remember { mutableStateOf(auth?.currentUser) }
    
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
                viewModel.resetSuccessState()
                onLoginSuccess?.invoke()
            },
            onError = { errorMessage ->
                if (errorMessage.contains("Límite de dispositivos", ignoreCase = true)) {
                    auth?.signOut()
                    currentUser = null
                    android.widget.Toast.makeText(context, errorMessage, android.widget.Toast.LENGTH_LONG).show()
                } else {
                    currentUser = auth?.currentUser
                    com.example.util.SubscriptionManager.init(context)
                    onLoginSuccess?.invoke()
                }
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
fun AuthenticatedProfilePanel(user: com.google.firebase.auth.FirebaseUser, onSignOut: () -> Unit) {
    val context = LocalContext.current
    val activeTheme = AppThemeManager.currentTheme
    val isPremium by SubscriptionManager.isPremium.collectAsState()
    val userRole by SubscriptionManager.userRole.collectAsState()
    val premiumUntil by SubscriptionManager.premiumUntil.collectAsState()
    val savedUserName by SubscriptionManager.userName.collectAsState()
    val currentAvatarId by SubscriptionManager.currentAvatarId.collectAsState()
    val currentRankBorder by SubscriptionManager.currentRankBorder.collectAsState()
    var showAvatarDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPlansDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showInboxDialog by remember { mutableStateOf(false) }
    var showAdminDashboard by remember { mutableStateOf(false) }
    var showAdminCreatorDialog by remember { mutableStateOf(false) }
    var showSupportPanel by remember { mutableStateOf(false) }
    var showSponsorPanel by remember { mutableStateOf(false) }
    var showSponsorModerationDialog by remember { mutableStateOf(false) }
    var showBuyEssenceDialog by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }
    val activeProfile by com.example.data.AccountProfileManager.activeProfile.collectAsState()

    val isExpiringSoon = remember(premiumUntil, isPremium, userRole) {
        SubscriptionManager.isExpiringSoon()
    }
    var remainingFormatted by remember { mutableStateOf(SubscriptionManager.getRemainingPremiumTimeFormatted()) }

    LaunchedEffect(premiumUntil, isPremium, userRole) {
        while (true) {
            remainingFormatted = SubscriptionManager.getRemainingPremiumTimeFormatted()
            kotlinx.coroutines.delay(1000)
        }
    }

    LaunchedEffect(user.uid) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                com.example.data.supabase.FeedbackRepository.syncAndPurgeOrphansForUser(
                    context,
                    user.uid,
                    user.email ?: ""
                )
            } catch (_: Exception) {}
        }
    }

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

    if (showAdminCreatorDialog) {
        com.example.ui.components.AdminCreatorBuildsDialog(
            onDismiss = { showAdminCreatorDialog = false }
        )
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
            // Top Row with Inbox (top-left), Blue Essence (top-center), and History (top-right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Top-Left: Inbox button
                val unreadCount by SubscriptionManager.unreadMessagesCount.collectAsState()
                val infiniteTransition = rememberInfiniteTransition(label = "inboxBtnAnim")
                val scaleAnim by infiniteTransition.animateFloat(
                    initialValue = 0.94f,
                    targetValue = 1.06f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "inboxScale"
                )
                com.example.ui.components.HextechAnimatedIconButton(
                    onClick = { showInboxDialog = true },
                    size = 40.dp,
                    backgroundColor = activeTheme.surfaceVariant,
                    borderColor = if (unreadCount > 0) com.example.ui.theme.HextechGold else activeTheme.cardBorder,
                    glowColor = if (unreadCount > 0) com.example.ui.theme.HextechGold else activeTheme.primary,
                    enablePulse = true,
                    modifier = Modifier.graphicsLayer {
                        scaleX = scaleAnim
                        scaleY = scaleAnim
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (unreadCount > 0) Icons.Default.MarkEmailUnread else Icons.Default.Message,
                            contentDescription = "Bandeja de Entrada",
                            tint = if (unreadCount > 0) com.example.ui.theme.HextechGold else activeTheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                                    .clip(CircleShape)
                                    .background(DangerRed)
                                    .border(1.dp, activeTheme.surfaceVariant, CircleShape)
                            )
                        }
                    }
                }

                // Top-Center: Blue Essence compact badge
                val currentBlueEssence by SubscriptionManager.blueEssence.collectAsState()
                var essenceBounce by remember { mutableStateOf(false) }
                val essenceScale by animateFloatAsState(
                    targetValue = if (essenceBounce) 1.08f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "essenceScale",
                    finishedListener = { essenceBounce = false }
                )

                Surface(
                    modifier = Modifier
                        .height(40.dp)
                        .graphicsLayer {
                            scaleX = essenceScale
                            scaleY = essenceScale
                        }
                        .tactileClickable {
                            essenceBounce = true
                            showBuyEssenceDialog = true
                        },
                    shape = RoundedCornerShape(20.dp),
                    color = activeTheme.surfaceVariant.copy(alpha = 0.9f),
                    border = BorderStroke(1.2.dp, activeTheme.primary.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = com.example.R.drawable.ic_blue_essence),
                            contentDescription = "Esencia Azul",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$currentBlueEssence EA",
                            color = HextechCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Top-Right: History button
                com.example.ui.components.HextechAnimatedIconButton(
                    onClick = { showHistoryDialog = true },
                    size = 40.dp,
                    backgroundColor = activeTheme.surfaceVariant,
                    borderColor = activeTheme.cardBorder,
                    glowColor = activeTheme.secondary
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Historial",
                        tint = activeTheme.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            var showBuyEssenceDialog by remember { mutableStateOf(false) }

            AuthHeader(
                title = "Perfil de Invocador",
                subtitle = "Sesión iniciada correctamente"
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (showInboxDialog) {
                com.example.ui.components.UserInboxDialog(
                    userUid = user.uid,
                    onDismiss = { showInboxDialog = false }
                )
            }
            if (showBuyEssenceDialog) {
                com.example.ui.components.BuyEssenceDialog(
                    isAdmin = userRole == "admin" || AuthManager.isCurrentUserAdmin(),
                    onDismiss = { showBuyEssenceDialog = false }
                )
            }

            val isAdminUser = userRole == "admin" || AuthManager.isCurrentUserAdmin()
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
                                contentDescription = "Verificado",
                                tint = activeTheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    title = {
                        Text(
                            text = "Cuenta Verificada",
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
                                text = "Esta cuenta de invocador se encuentra verificada y autenticada oficialmente en el sistema.",
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
                                        text = "Estado: Perfil auténtico, protegido y sincronizado.",
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
                            Text("Entendido", fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = activeTheme.surface,
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 6.dp
                )
            }

            // Summoner Crest Avatar
            val finalUserName = savedUserName.takeIf { it.isNotBlank() }
                ?: user.displayName?.takeIf { it.isNotBlank() }
                ?: user.email?.substringBefore("@")
                ?: "Invocador"

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
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "avatarScale",
                finishedListener = { avatarTapped = false }
            )

            // Avatar in center
            Box(
                modifier = Modifier
                    .padding(
                        top = if (isAdminUser) 24.dp else 6.dp,
                        bottom = if (isAdminUser) 10.dp else 6.dp,
                        start = if (isAdminUser) 24.dp else 8.dp,
                        end = if (isAdminUser) 24.dp else 8.dp
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
                if (!isAdminUser) {
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
                    size = if (isAdminUser) 74.dp else 72.dp,
                    fallbackInitial = finalUserName,
                    isAdmin = isAdminUser
                )
                // Botón interactivo de cambio de avatar (Lápiz)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(
                            x = if (isAdminUser) 8.dp else 2.dp,
                            y = if (isAdminUser) 6.dp else 2.dp
                        )
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(activeTheme.secondary)
                        .border(1.5.dp, activeTheme.background, CircleShape)
                        .clickable(
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
                        contentDescription = "Cambiar Avatar",
                        tint = activeTheme.background,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isAdminUser) 54.dp else 12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = finalUserName,
                    color = activeTheme.secondary,
                    fontSize = 20.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = { showVerifiedInfoDialog = true },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = "Cuenta Verificada - Toca para más información",
                        tint = activeTheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Rol visualizado directamente debajo del usuario, únicamente el rol sin tanto contexto
            RoleBadge(
                role = userRole,
                isPremiumActive = isPremium,
                isBanned = (userRole == "banned"),
                isExpiringSoon = isExpiringSoon,
                size = RoleBadgeSize.NORMAL
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Avatar Title & Region subtitle
            Text(
                text = "${equippedAvatar.title} • ${equippedAvatar.region}",
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
                        text = "${activeTheme.regionTag.uppercase()} • ${activeTheme.titleKey}",
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
                    text = if (isEmailVisible) (user.email ?: "") else "••••••••@••••.com",
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
            LaunchedEffect(user.uid) {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.uid)
                    .get()
                    .addOnSuccessListener { doc ->
                        val devs = doc.get("registeredDevices") as? List<*> ?: emptyList<Any>()
                        registeredDevicesCount = devs.size.coerceAtLeast(1)
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
                                text = "📱 Dispositivos Conectados:",
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
                                    text = "$registeredDevicesCount de 2 en uso",
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
                                text = "🔒 " + com.example.util.tr("Por seguridad de tu cuenta, la liberación y reasignación de slots de hardware es gestionada exclusivamente por los Administradores desde el panel de soporte."),
                                color = activeTheme.textSecondary,
                                fontSize = 10.5.sp,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "⚠️ " + com.example.util.tr("Recomendación: Se recomienda no cerrar sesión para evitar un mal funcionamiento o problemas a futuro con tu cuenta, sincronización de licencias y el acceso fluido a tus herramientas de drafting."),
                                color = activeTheme.textMuted,
                                fontSize = 10.5.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val isUserPremium = isPremium || userRole == "admin" || userRole == "moderador" || userRole == "creador_vip" || userRole == "streamer" || AuthManager.isCurrentUserAdmin()
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
                                text = "Tema de Región:",
                                color = activeTheme.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = activeTheme.titleKey,
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
                                text = "↔ Desliza temas",
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
                                        text = themeItem.titleKey,
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
                        Text("💡", fontSize = 22.sp)
                        Column {
                            Text(
                                text = "Consejo del Coach Soberano",
                                color = activeTheme.secondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "¡Cada región altera la energía y colores de la interfaz! Selecciona tu región favorita para sincronizar tu estilo competitivo.",
                                color = activeTheme.textSecondary,
                                fontSize = 10.5.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Alerta de suscripción por vencer (si aplica)
            if (isExpiringSoon) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(com.example.ui.theme.DangerRed.copy(alpha = 0.15f))
                        .border(1.dp, com.example.ui.theme.DangerRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = com.example.ui.theme.DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Suscripción por Vencer ($remainingFormatted)",
                                color = com.example.ui.theme.DangerRed,
                                fontSize = 13.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Renueva tu pase para mantener tus herramientas y temas activos.",
                            color = com.example.ui.theme.TextSecondary,
                            fontSize = 11.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        com.example.ui.components.HextechAnimatedButton(
                            onClick = { showPlansDialog = true },
                            backgroundColor = com.example.ui.theme.DangerRed,
                            borderColor = com.example.ui.theme.HextechGold,
                            glowColor = com.example.ui.theme.DangerRed,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            shape = RoundedCornerShape(8.dp),
                            enableShimmer = true,
                            enablePulse = true,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = androidx.compose.ui.graphics.Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Renovar / Extender Suscripción",
                                fontSize = 12.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
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
                    .height(48.dp),
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
                    text = if (isPremium) "Planes / Pase" else "Ver Planes Pro",
                    color = activeTheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botón Creador (Panel de Usuario)
            com.example.ui.components.HextechAnimatedButton(
                onClick = {
                    val isAdmin = userRole == "admin" || AuthManager.isCurrentUserAdmin()
                    if (isAdmin) {
                        showAdminCreatorDialog = true
                    } else {
                        Toast.makeText(context, "Acceso restringido únicamente para administradores", Toast.LENGTH_SHORT).show()
                    }
                },
                backgroundBrush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    listOf(HextechGold, Color(0xFFD4AF37))
                ),
                borderColor = HextechCyan,
                glowColor = HextechGold,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
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
                    text = "Creador",
                    color = HextechDarkBg,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            
            if (showHistoryDialog) {
                com.example.ui.components.SubscriptionHistoryDialog(
                    userId = user.uid,
                    userEmail = user.email,
                    onDismiss = { showHistoryDialog = false }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            
            if (userRole == "admin") {
                // Panel de Administración
                com.example.ui.components.HextechAnimatedButton(
                    onClick = { showAdminDashboard = true },
                    backgroundBrush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(com.example.ui.theme.DangerRed, Color(0xFFB91C1C))
                    ),
                    borderColor = com.example.ui.theme.HextechGold,
                    glowColor = com.example.ui.theme.DangerRed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
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
                        text = "Panel de Administración",
                        color = Color.White,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Panel de Moderador
                val allNoticesForSponsor by com.example.data.AppNoticeManager.notices.collectAsState()
                val hasPendingSponsorsForAuth = remember(allNoticesForSponsor) {
                    allNoticesForSponsor.any { (it.tag.equals("Publicidad", true) || it.sponsorEmail.isNotBlank()) && !it.isApproved }
                }

                com.example.ui.components.HextechAnimatedButton(
                    onClick = { showSponsorModerationDialog = true },
                    backgroundBrush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        if (hasPendingSponsorsForAuth) listOf(Color(0xFFEF4444), Color(0xFFB91C1C)) else listOf(Color(0xFFF97316), Color(0xFFEA580C))
                    ),
                    borderColor = if (hasPendingSponsorsForAuth) Color(0xFFFFD700) else com.example.ui.theme.HextechGold,
                    glowColor = if (hasPendingSponsorsForAuth) Color(0xFFFF4500) else Color(0xFFF97316),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enableShimmer = true,
                    enablePulse = true
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (hasPendingSponsorsForAuth) "Panel de Moderador (¡Solicitud Pendiente!)" else "Panel de Moderador",
                                color = Color.White,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }
                        if (hasPendingSponsorsForAuth) {
                            Badge(
                                containerColor = Color.White,
                                contentColor = Color.Red,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 16.dp)
                            ) {
                                Text("!")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Panel Patrocinador CPM
                com.example.ui.components.HextechAnimatedButton(
                    onClick = { showSponsorPanel = true },
                    backgroundBrush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(Color(0xFFEC4899), Color(0xFFDB2777))
                    ),
                    borderColor = com.example.ui.theme.HextechGold,
                    glowColor = Color(0xFFEC4899),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enableShimmer = true,
                    enablePulse = true
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Panel Patrocinador CPM",
                        color = Color.White,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            } else if (userRole == "moderador") {
                com.example.ui.components.HextechAnimatedButton(
                    onClick = { showSupportPanel = true },
                    backgroundBrush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(com.example.ui.theme.HextechCyan, com.example.ui.theme.HextechBlue)
                    ),
                    borderColor = com.example.ui.theme.HextechGold,
                    glowColor = com.example.ui.theme.HextechCyan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enableShimmer = true,
                    enablePulse = true
                ) {
                    Icon(
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = null,
                        tint = com.example.ui.theme.HextechDarkBg
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Panel de Soporte",
                        color = com.example.ui.theme.HextechDarkBg,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (userRole == "patrocinador") {
                com.example.ui.components.HextechAnimatedButton(
                    onClick = { showSponsorPanel = true },
                    backgroundBrush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(Color(0xFFF97316), Color(0xFFEA580C))
                    ),
                    borderColor = com.example.ui.theme.HextechGold,
                    glowColor = Color(0xFFF97316),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enableShimmer = true,
                    enablePulse = true
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Panel de Patrocinador", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            com.example.ui.components.HextechAnimatedOutlinedButton(
                onClick = { showSignOutConfirm = true },
                backgroundColor = com.example.ui.theme.HextechSurfaceVariant.copy(alpha = 0.5f),
                borderColor = DangerRed.copy(alpha = 0.65f),
                glowColor = DangerRed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
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
                Text("Cerrar Sesión", color = DangerRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text("Cerrar Sesión", color = DangerRed, fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que deseas cerrar sesión? Se recomienda mantener la sesión abierta para asegurar la sincronización correcta de tu cuenta y licencias.", color = activeTheme.textSecondary, fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirm = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Cerrar Sesión", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) {
                    Text("Cancelar", color = activeTheme.textMuted)
                }
            },
            containerColor = activeTheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun Modifier.tactileClickable(
    scaleDown: Float = 0.94f,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tactileScale"
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.material3.ripple(bounded = true),
            onClick = onClick
        )
}
