package com.example

import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState

import androidx.compose.foundation.background
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import com.example.util.LocalLanguage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LaneRole
import com.example.ui.components.AppUpdateDialog
import com.example.ui.components.PrivacyPolicyDialog
import com.example.ui.screens.InfoScreen
import com.example.ui.screens.MainDraftingScreen
import com.example.ui.screens.AnimatedSplashScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.LanguageSelectionScreen
import com.example.ui.screens.MetaScreenMode
import com.example.ui.screens.MetaAndDraftScreen
import com.example.ui.screens.TutorialScreen
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechGold
import com.example.ui.theme.AppThemeManager
import com.example.ui.theme.TextSecondary
import com.example.util.tr
import com.example.util.AppUpdateManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private enum class NavBarRuneShape {
    DIAMOND,
    CROSS_STAR,
    ORB,
    RUNIC_PULSE
}

private data class NavBarParticle(
    val relX: Float,
    val relY: Float,
    val driftSpeed: Float,
    val swayFreq: Float,
    val swayAmp: Float,
    val size: Float,
    val shape: NavBarRuneShape,
    val baseColor: Color,
    val pulsePhase: Float
)

@Composable
fun RunicNavBarParticleAnimation(
    modifier: Modifier = Modifier,
    particleCount: Int = 16,
    accentColor: Color = Color(0xFFC8AA6E)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "navRunicTransition")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "navParticleProgress"
    )

    val particles = remember(accentColor) {
        val random = Random(77)
        val colors = listOf(
            accentColor,
            Color(0xFF0AC8B9),
            Color(0xFF00E5FF),
            Color(0xFFF0E6D2),
            Color(0xFF818CF8),
            Color(0xFF00FF7F)
        )
        val shapes = NavBarRuneShape.values()
        List(particleCount) {
            NavBarParticle(
                relX = random.nextFloat(),
                relY = random.nextFloat(),
                driftSpeed = 0.25f + random.nextFloat() * 0.45f,
                swayFreq = 1.0f + random.nextFloat() * 2.0f,
                swayAmp = 0.02f + random.nextFloat() * 0.04f,
                size = 2.5f + random.nextFloat() * 5.0f,
                shape = shapes[random.nextInt(shapes.size)],
                baseColor = colors[random.nextInt(colors.size)],
                pulsePhase = random.nextFloat() * (2f * PI.toFloat())
            )
        }
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        if (width <= 0 || height <= 0) return@Canvas

        // Soft atmospheric ambient glow
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.08f),
                    Color(0xFF0AC8B9).copy(alpha = 0.04f),
                    Color.Transparent
                ),
                center = Offset(width * 0.5f, height * 0.5f),
                radius = width * 0.5f
            )
        )

        val computedPoints = particles.map { p ->
            val rawY = (p.relY - animationProgress * p.driftSpeed) % 1f
            val currentY = if (rawY < 0f) rawY + 1f else rawY
            val sway = sin(animationProgress * 2f * PI.toFloat() * p.swayFreq + p.pulsePhase) * p.swayAmp
            val currentX = (p.relX + sway).coerceIn(0.01f, 0.99f)
            val px = currentX * width
            val py = currentY * height
            val alpha = (sin(animationProgress * 2f * PI.toFloat() * 1.5f + p.pulsePhase) * 0.35f + 0.5f).coerceIn(0.1f, 0.85f)
            Triple(Offset(px, py), alpha, p)
        }

        // Draw connective constellation lines
        for (i in computedPoints.indices) {
            val (pos1, alpha1, p1) = computedPoints[i]
            for (j in i + 1 until computedPoints.size) {
                val (pos2, alpha2, _) = computedPoints[j]
                val dx = pos1.x - pos2.x
                val dy = pos1.y - pos2.y
                val dist = dx * dx + dy * dy
                val maxDist = (width * 0.18f) * (width * 0.18f)
                if (dist < maxDist) {
                    val lineAlpha = (1f - dist / maxDist) * 0.12f * ((alpha1 + alpha2) * 0.5f)
                    drawLine(
                        brush = Brush.linearGradient(
                            listOf(
                                p1.baseColor.copy(alpha = lineAlpha),
                                accentColor.copy(alpha = lineAlpha * 0.5f)
                            )
                        ),
                        start = pos1,
                        end = pos2,
                        strokeWidth = 0.8f
                    )
                }
            }
        }

        // Draw runic particle shapes
        for ((pos, alpha, p) in computedPoints) {
            val px = pos.x
            val py = pos.y
            val baseRadius = p.size

            // Soft glowing halo
            drawCircle(
                color = p.baseColor.copy(alpha = alpha * 0.2f),
                radius = baseRadius * 2.5f,
                center = pos
            )

            when (p.shape) {
                NavBarRuneShape.DIAMOND -> {
                    val path = Path().apply {
                        moveTo(px, py - baseRadius)
                        lineTo(px + baseRadius * 0.75f, py)
                        lineTo(px, py + baseRadius)
                        lineTo(px - baseRadius * 0.75f, py)
                        close()
                    }
                    drawPath(path, color = p.baseColor.copy(alpha = alpha))
                    drawPath(
                        path,
                        color = Color(0xFFF0E6D2).copy(alpha = alpha * 0.8f),
                        style = Stroke(width = 0.8f)
                    )
                }
                NavBarRuneShape.CROSS_STAR -> {
                    val armLength = baseRadius * 1.3f
                    val starColor = p.baseColor.copy(alpha = alpha)
                    drawLine(
                        color = starColor,
                        start = Offset(px - armLength, py),
                        end = Offset(px + armLength, py),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = starColor,
                        start = Offset(px, py - armLength),
                        end = Offset(px, py + armLength),
                        strokeWidth = 1f
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = baseRadius * 0.3f,
                        center = pos
                    )
                }
                NavBarRuneShape.ORB -> {
                    drawCircle(
                        color = p.baseColor.copy(alpha = alpha * 0.8f),
                        radius = baseRadius * 0.65f,
                        center = pos
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = alpha * 0.9f),
                        radius = baseRadius * 0.25f,
                        center = pos
                    )
                }
                NavBarRuneShape.RUNIC_PULSE -> {
                    drawCircle(
                        color = p.baseColor.copy(alpha = alpha * 0.65f),
                        radius = baseRadius * 0.85f,
                        center = pos,
                        style = Stroke(width = 1f)
                    )
                    drawCircle(
                        color = accentColor.copy(alpha = alpha * 0.85f),
                        radius = baseRadius * 0.3f,
                        center = pos
                    )
                }
            }
        }
    }
}

enum class AppScreen {
    SPLASH,
    ONBOARDING,
    LOGIN,
    LANGUAGE_SELECTION,
    MAIN,
    INFO,
    FAQ,
    META
}

class MainActivity : ComponentActivity() {    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted
        } else {
            // Permission is denied
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // OWASP MASVS: Anti-Tampering & Screen Protection (DevSecOps)
        // com.example.util.AppSecurityManager.enableScreenProtection(this)
        
        if (com.example.util.AppSecurityManager.isDeviceRooted() || com.example.util.AppSecurityManager.isDebuggerAttached()) {
            android.util.Log.w("AppSecurity", "WARNING: Device may be rooted or debugger is attached. Applying degraded functionality mode or just warning.")
            // Real apps might exit here: finishAffinity()
        }
        
        AppThemeManager.init(this)
        com.example.util.SubscriptionManager.init(this)
        val currentAuthUser = com.example.util.AuthManager.getAuth()?.currentUser
        if (currentAuthUser != null && !com.example.util.AuthManager.isGuestOrUnauthenticated(currentAuthUser)) {
            com.example.util.DeviceAndSessionManager.registerDeviceAndSession(this, onError = { msg -> 
                if (msg.contains("Límite de dispositivos", ignoreCase = true)) {
                    android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_LONG).show()
                    com.example.util.AuthManager.getAuth()?.signOut()
                }
            })
        }
        askNotificationPermission()
        com.example.data.GlobalAnnouncementManager.init(this)
        if (intent.getBooleanExtra("extra_open_global_announcement", false)) {
            com.example.data.GlobalAnnouncementManager.showAnnouncementModal()
            intent.removeExtra("extra_open_global_announcement")
        }

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                com.example.ui.components.BlurredMeshBackground(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        DraftingApp()
                        

                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        com.example.util.SubscriptionManager.init(this)
        com.example.data.GlobalAnnouncementManager.init(this)
        if (intent.getBooleanExtra("extra_open_global_announcement", false)) {
            com.example.data.GlobalAnnouncementManager.showAnnouncementModal()
            intent.removeExtra("extra_open_global_announcement")
        }
    }

    override fun onStop() {
        super.onStop()
        com.example.util.SubscriptionManager.stopHeartbeat()
    }
}

@Composable
fun DashboardScreen(
    onNavigateToInfo: () -> Unit,
    onNavigateToFAQ: () -> Unit,
    onNavigateToLogin: () -> Unit,
    mainRole: LaneRole,
    onMainRoleChange: (LaneRole) -> Unit,
    secondRole: LaneRole,
    onSecondRoleChange: (LaneRole) -> Unit,
    autofillRole: LaneRole,
    onAutofillRoleChange: (LaneRole) -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val targetChampId = activity?.intent?.getStringExtra("OPEN_CHAMPION_DETAIL")
    val initialPage = if (activity?.intent?.getBooleanExtra("OPEN_TIER_LIST", false) == true || targetChampId != null) 2 else 0
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 5 })
    
    // Clear intent so we don't reopen tier list on rotation
    androidx.compose.runtime.LaunchedEffect(Unit) {
        activity?.intent?.removeExtra("OPEN_TIER_LIST")
        activity?.intent?.removeExtra("OPEN_CHAMPION_DETAIL")
    }
    
    var showExitDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val isPremium by com.example.util.SubscriptionManager.isPremium.collectAsStateWithLifecycle()

    if (showExitDialog) {
        com.example.ui.components.ExitConfirmationDialog(
            onConfirmExit = {
                val activity = context as? android.app.Activity
                activity?.finish()
            },
            onDismiss = { showExitDialog = false }
        )
    }

    val pageHistory = remember { mutableStateListOf<Int>() }
    LaunchedEffect(pagerState.currentPage) {
        if (pageHistory.isEmpty() || pageHistory.last() != pagerState.currentPage) {
            pageHistory.add(pagerState.currentPage)
        }
    }

    BackHandler(enabled = true) {
        if (pageHistory.size > 1) {
            pageHistory.removeAt(pageHistory.lastIndex)
            val prev = pageHistory.last()
            coroutineScope.launch { pagerState.animateScrollToPage(prev) }
        } else if (pagerState.currentPage != 0) {
            coroutineScope.launch { pagerState.animateScrollToPage(0) }
        } else {
            showExitDialog = true
        }
    }

    val navBg = AppThemeManager.getNavBarBackgroundColor()
    val navAccent = AppThemeManager.getNavBarAccentColor()
    val navIndicator = AppThemeManager.getNavBarIndicatorColor()
    val navSelectedIcon = AppThemeManager.getNavBarSelectedIconColor()
    val navSelectedText = AppThemeManager.getNavBarSelectedTextColor()
    val navUnselected = AppThemeManager.getNavBarUnselectedColor()

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        bottomBar = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(navBg)
                ) {
                // Ambient Runic Particles Floating across Bottom Navigation Bar in background
                if (AppThemeManager.isParticlesEnabled && isPremium) {
                    RunicNavBarParticleAnimation(
                        modifier = Modifier.matchParentSize(),
                        particleCount = 16,
                        accentColor = navAccent
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    // Top golden/accent glowing divider line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        navAccent.copy(alpha = 0.6f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    NavigationBar(
                        containerColor = Color.Transparent,
                        contentColor = navSelectedText
                    ) {
                    // 1. Inicio
                    NavigationBarItem(
                        selected = pagerState.currentPage == 0,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                        label = { Text(tr("Inicio")) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = navSelectedIcon,
                            selectedTextColor = navSelectedText,
                            indicatorColor = navIndicator,
                            unselectedIconColor = navUnselected,
                            unselectedTextColor = navUnselected
                        )
                    )
                    // 2. Selección (Drafting)
                    NavigationBarItem(
                        selected = pagerState.currentPage == 1,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                        icon = { Icon(Icons.Default.Groups, contentDescription = "Selección") },
                        label = { Text(tr("Selección")) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = navSelectedIcon,
                            selectedTextColor = navSelectedText,
                            indicatorColor = navIndicator,
                            unselectedIconColor = navUnselected,
                            unselectedTextColor = navUnselected
                        )
                    )
                    // 3. Tier List
                    NavigationBarItem(
                        selected = pagerState.currentPage == 2,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(2) } },
                        icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Tier List") },
                        label = { Text(tr("Tier List")) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = navSelectedIcon,
                            selectedTextColor = navSelectedText,
                            indicatorColor = navIndicator,
                            unselectedIconColor = navUnselected,
                            unselectedTextColor = navUnselected
                        )
                    )
                    // 4. Catálogo (Objetos, Runas, Hechizos)
                    NavigationBarItem(
                        selected = pagerState.currentPage == 3,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(3) } },
                        icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Catálogo") },
                        label = { Text(tr("Catálogo")) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = navSelectedIcon,
                            selectedTextColor = navSelectedText,
                            indicatorColor = navIndicator,
                            unselectedIconColor = navUnselected,
                            unselectedTextColor = navUnselected
                        )
                    )
                    // 5. Usuario
                    NavigationBarItem(
                        selected = pagerState.currentPage == 4,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(4) } },
                        icon = { 
                            val unreadCount by com.example.util.SubscriptionManager.unreadMessagesCount.collectAsStateWithLifecycle(0)
                            val allNotices by com.example.data.AppNoticeManager.notices.collectAsStateWithLifecycle(com.example.data.AppNoticeManager.notices.value)
                            val userRole by com.example.util.SubscriptionManager.userRole.collectAsStateWithLifecycle()
                            val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                            val isLogged = authUser != null && !com.example.util.AuthManager.isGuestOrUnauthenticated(authUser)

                            val hasPendingSponsors = remember(allNotices, isLogged, userRole) {
                                isLogged && (userRole == "admin" || userRole == "patrocinador" || com.example.util.AuthManager.isCurrentUserAdmin()) &&
                                    allNotices.any { (it.tag.equals("Publicidad", true) || it.sponsorEmail.isNotBlank()) && !it.isApproved }
                            }

                            val effectiveUnreadCount = if (isLogged) unreadCount else 0
                            val showBadge = isLogged && (effectiveUnreadCount > 0 || hasPendingSponsors)

                            val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "nav_sponsor_pulse")
                            val scale by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 1.35f,
                                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                                    animation = androidx.compose.animation.core.tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                                ),
                                label = "scale"
                            )

                            BadgedBox(
                                badge = {
                                    if (showBadge) {
                                        Badge(
                                            containerColor = if (hasPendingSponsors) Color(0xFFEF4444) else com.example.ui.theme.DangerRed,
                                            contentColor = Color.White,
                                            modifier = if (hasPendingSponsors) Modifier.graphicsLayer(scaleX = scale, scaleY = scale) else Modifier
                                        ) {
                                            Text(if (hasPendingSponsors) "!" else effectiveUnreadCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Box(modifier = if (showBadge && hasPendingSponsors) Modifier.graphicsLayer(scaleX = scale, scaleY = scale) else Modifier) {
                                    Icon(Icons.Default.Person, contentDescription = "Usuario", tint = if (showBadge && hasPendingSponsors) Color(0xFFF97316) else LocalContentColor.current)
                                }
                            }
                        },
                        label = { Text(tr("Usuario")) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = navSelectedIcon,
                            selectedTextColor = navSelectedText,
                            indicatorColor = navIndicator,
                            unselectedIconColor = navUnselected,
                            unselectedTextColor = navUnselected
                        )
                    )
                }
                }

                // Ambient Runic Particles Floating across Bottom Navigation Bar
                if (AppThemeManager.isParticlesEnabled && isPremium) {
                    RunicNavBarParticleAnimation(
                        modifier = Modifier
                            .matchParentSize()
                            .clipToBounds(),
                        particleCount = 14,
                        accentColor = navAccent
                    )
                }
            }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true
            ) { page ->
                when (page) {
                0 -> {
                    MainDraftingScreen(
                        onNavigateToInfo = onNavigateToInfo,
                        onNavigateToFAQ = onNavigateToFAQ,
                        onNavigateToMeta = { coroutineScope.launch { pagerState.animateScrollToPage(2) } },
                        onNavigateToLogin = { coroutineScope.launch { pagerState.animateScrollToPage(4) } },
                        mainRole = mainRole,
                        onMainRoleChange = onMainRoleChange,
                        secondRole = secondRole,
                        onSecondRoleChange = onSecondRoleChange,
                        autofillRole = autofillRole,
                        onAutofillRoleChange = onAutofillRoleChange,
                        currentLanguage = currentLanguage,
                        onLanguageChange = onLanguageChange
                    )
                }
                1 -> {
                    MetaAndDraftScreen(
                        mode = MetaScreenMode.DRAFTING,
                        userMainRole = mainRole,
                        onNavigateBack = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                    )
                }
                2 -> {
                    MetaAndDraftScreen(
                        mode = MetaScreenMode.TIER_LIST,
                        userMainRole = mainRole,
                        initialChampionId = targetChampId,
                        onNavigateBack = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                    )
                }
                3 -> {
                    MetaAndDraftScreen(
                        mode = MetaScreenMode.CATALOG,
                        userMainRole = mainRole,
                        onNavigateBack = { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                    )
                }
                4 -> {
                    com.example.ui.auth.AuthFlowContainer(
                        onLoginSuccess = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        }
                    )
                }
                else -> {
                    MainDraftingScreen(
                        onNavigateToInfo = onNavigateToInfo,
                        onNavigateToFAQ = onNavigateToFAQ,
                        onNavigateToMeta = { coroutineScope.launch { pagerState.animateScrollToPage(2) } },
                        onNavigateToLogin = { coroutineScope.launch { pagerState.animateScrollToPage(4) } },
                        mainRole = mainRole,
                        onMainRoleChange = onMainRoleChange,
                        secondRole = secondRole,
                        onSecondRoleChange = onSecondRoleChange,
                        autofillRole = autofillRole,
                        onAutofillRoleChange = onAutofillRoleChange,
                        currentLanguage = currentLanguage,
                        onLanguageChange = onLanguageChange
                    )
                }
            }
            } // HorizontalPager
        }
    }
}


@Composable
fun DraftingApp() {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    var isLanguageSet by remember { mutableStateOf(sharedPrefs.getBoolean("is_language_set", false)) }
    var hasAcceptedLegal by remember { mutableStateOf(sharedPrefs.getBoolean("has_accepted_legal", false)) }
    var hasSeenOnboarding by remember { mutableStateOf(sharedPrefs.getBoolean("has_seen_onboarding", false)) }
    var showLegalDialog by remember { mutableStateOf(isLanguageSet && !hasAcceptedLegal) }
    var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }
    
    val coroutineScope = rememberCoroutineScope()
    var mainRole by remember { mutableStateOf(com.example.util.UserPreferences.getMainRole(context)) }
    var secondRole by remember { mutableStateOf(com.example.util.UserPreferences.getSecondRole(context)) }
    var autofillRole by remember { mutableStateOf(com.example.util.UserPreferences.getAutofillRole(context)) }
    val activeUpdateInfo by AppUpdateManager.updateInfo.collectAsStateWithLifecycle()
    val isBanned by com.example.util.SubscriptionManager.isBanned.collectAsStateWithLifecycle()
    val currentGlobalAnnouncement by com.example.data.GlobalAnnouncementManager.currentAnnouncement.collectAsStateWithLifecycle()
    val isAnnouncementVisible by com.example.data.GlobalAnnouncementManager.isAnnouncementVisible.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        // Inicializar listado maestro de campeones desde assets JSON
        com.example.data.WildRiftRepository.initChampions(context)

        // Ejecuta la sincronización en segundo plano al arrancar la app para traer los datos desde la nube
        com.example.data.sync.MetaCrawlerSyncService.syncPatchData(context)
        com.example.data.GlobalAnnouncementManager.init(context)
        com.example.data.GlobalAnnouncementManager.refreshFromCloud(context)
        // AppUpdateManager.checkForUpdates disabled
    }

    var selectedLanguage by remember { mutableStateOf(sharedPrefs.getString("selected_language", "es") ?: "es") }

    CompositionLocalProvider(LocalLanguage provides selectedLanguage) {
        if (isBanned) {
            Box(
                modifier = Modifier.fillMaxSize().background(HextechDarkBg),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Cuenta Suspendida", color = Color.Red, fontSize = 24.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tu acceso ha sido revocado permanentemente. Contacta con soporte si crees que esto es un error.", color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
            return@CompositionLocalProvider
        }
        
        // Modal de Alerta de Actualización Disponible con opción de descarga directa
        activeUpdateInfo?.let { update ->
            if (update.isUpdateAvailable) {
                AppUpdateDialog(
                    updateInfo = update,
                    onDismiss = { AppUpdateManager.dismissAlert() }
                )
            }
        }

    BackHandler(enabled = !hasAcceptedLegal) {
        (context as? android.app.Activity)?.finishAffinity()
    }

    BackHandler(enabled = !showLegalDialog && currentScreen != AppScreen.MAIN && currentScreen != AppScreen.LANGUAGE_SELECTION) {
        currentScreen = AppScreen.MAIN
    }

                    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (initialState == AppScreen.SPLASH) {
                (fadeIn(animationSpec = androidx.compose.animation.core.tween(500))).togetherWith(fadeOut(animationSpec = androidx.compose.animation.core.tween(500)))
            } else if (targetState == AppScreen.MAIN && (initialState == AppScreen.LANGUAGE_SELECTION || initialState == AppScreen.LOGIN)) {
                (fadeIn()).togetherWith(fadeOut())
            } else if (targetState == AppScreen.LANGUAGE_SELECTION && initialState == AppScreen.LOGIN) {
                (fadeIn()).togetherWith(fadeOut())
            } else if (targetState == AppScreen.MAIN) {
                (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
            } else {
                (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
            }
        },
        label = "screen_navigation"
    ) { screen ->
        when (screen) {
            AppScreen.SPLASH -> {
                AnimatedSplashScreen(
                    onSplashFinished = {
                        currentScreen = when {
                            !isLanguageSet -> AppScreen.LANGUAGE_SELECTION
                            !hasAcceptedLegal -> AppScreen.LANGUAGE_SELECTION
                            !hasSeenOnboarding -> AppScreen.ONBOARDING
                            else -> AppScreen.MAIN
                        }
                    }
                )
            }
            AppScreen.LOGIN -> {
            }
                        AppScreen.ONBOARDING -> {
                OnboardingScreen(
                    onFinish = {
                        sharedPrefs.edit().putBoolean("has_seen_onboarding", true).apply()
                        hasSeenOnboarding = true
                        currentScreen = AppScreen.MAIN
                    }
                )
            }
            AppScreen.LANGUAGE_SELECTION -> {
                LanguageSelectionScreen(
                    onLanguageSelected = { langCode ->
                        sharedPrefs.edit()
                            .putBoolean("is_language_set", true)
                            .putString("selected_language", langCode)
                            .apply()
                        isLanguageSet = true
                        selectedLanguage = langCode
                        if (!hasAcceptedLegal) {
                            showLegalDialog = true
                        } else {
                            currentScreen = if (!hasSeenOnboarding) AppScreen.ONBOARDING else AppScreen.MAIN
                        }
                        // AppUpdateManager.checkForUpdates disabled
                    }
                )
            }
            AppScreen.MAIN -> {
                DashboardScreen(
                    onNavigateToInfo = { currentScreen = AppScreen.INFO },
                    onNavigateToFAQ = { currentScreen = AppScreen.FAQ },
                    onNavigateToLogin = { currentScreen = AppScreen.LOGIN },
                    mainRole = mainRole,
                    onMainRoleChange = { 
                        mainRole = it
                        com.example.util.UserPreferences.setMainRole(context, it)
                    },
                    secondRole = secondRole,
                    onSecondRoleChange = { 
                        secondRole = it
                        com.example.util.UserPreferences.setSecondRole(context, it)
                    },
                    autofillRole = autofillRole,
                    onAutofillRoleChange = { 
                        autofillRole = it
                        com.example.util.UserPreferences.setAutofillRole(context, it)
                    },
                    currentLanguage = selectedLanguage,
                    onLanguageChange = { newLang ->
                        sharedPrefs.edit().putString("selected_language", newLang).apply()
                        selectedLanguage = newLang
                    }
                )
            }
            AppScreen.META -> {}
            AppScreen.INFO -> {
                InfoScreen(
                    onNavigateBack = { currentScreen = AppScreen.MAIN },
                    onNavigateToFAQ = { currentScreen = AppScreen.FAQ }
                )
            }
            AppScreen.FAQ -> {
                com.example.ui.screens.FAQScreen(
                    onNavigateBack = { currentScreen = AppScreen.MAIN }
                )
            }
        }
    }

    if (showLegalDialog && !hasAcceptedLegal) {
        PrivacyPolicyDialog(
            isMandatoryAcceptance = true,
            onAccept = {
                sharedPrefs.edit().putBoolean("has_accepted_legal", true).apply()
                hasAcceptedLegal = true
                showLegalDialog = false
                currentScreen = if (!hasSeenOnboarding) AppScreen.ONBOARDING else AppScreen.MAIN
            },
            onDismiss = {
                // Si la cierran en vez de aceptar se cierra la aplicación y no la pueden usar
                (context as? android.app.Activity)?.finishAffinity()
            }
        )
    }

    // Modal de Comunicado / Anuncio Global Oficial
    if (hasAcceptedLegal && isAnnouncementVisible && currentGlobalAnnouncement != null && currentGlobalAnnouncement!!.active) {
        com.example.ui.components.GlobalAnnouncementDialog(
            announcement = currentGlobalAnnouncement!!,
            onDismiss = {
                com.example.data.GlobalAnnouncementManager.dismissAnnouncement(
                    context = context,
                    announcementId = currentGlobalAnnouncement!!.id,
                    timestamp = currentGlobalAnnouncement!!.timestamp
                )
            }
        )
    }
}
}
