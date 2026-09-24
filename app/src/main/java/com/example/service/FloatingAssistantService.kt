package com.example.service
import android.graphics.Bitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.BoxWithConstraints

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.material.icons.filled.BugReport
import com.example.data.repository.DraftHistoryRepository
import com.example.util.ChampionRoleAdapter
import kotlinx.coroutines.launch

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.R
import com.example.data.WildRiftItemsData
import com.example.data.WildRiftRepository
import com.example.data.WildRiftSpellsAndRunes
import com.example.data.local.entity.SavedDraftEntity
import com.example.model.Champion
import com.example.model.DraftSlot
import com.example.model.LaneRole
import com.example.service.screen.DraftPickTurn
import com.example.service.screen.DraftVisionScanner
import com.example.service.screen.ScreenCaptureManager
import com.example.ui.components.AppAssetImage
import com.example.ui.components.ChampionAvatar
import com.example.ui.components.FormattedWildRiftText
import com.example.ui.components.SaveDraftDialog
import com.example.ui.components.WomboCombo
import com.example.ui.components.WomboComboSynergyDetector
import com.example.ui.theme.AllyBlue
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechGoldLight
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.HextechSurfaceVariant
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TierSPlusColor
import com.example.util.AppLogger
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import com.example.util.LocalLanguage
import com.example.util.SubscriptionManager
import com.example.util.tr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

enum class OverlayHubTab { DRAFT, TIER_LIST, CHAMPIONS, HISTORY }

private const val TAG = "FloatingAssistantService"

class FloatingAssistantService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner, ComponentCallbacks2 {
    private val overlayState = OverlayState()
    private var screenCaptureManager: ScreenCaptureManager? = null
    private var isDestroyed = false

    private var windowManager: WindowManager? = null
    private var floatingComposeView: ComposeView? = null
    
    // Estado para la orientación de la pantalla real
    private val isDeviceLandscape = androidx.compose.runtime.mutableStateOf(false)
    private var closeTargetComposeView: ComposeView? = null
    private var debugOverlayView: ComposeView? = null
    private var isDebugOverlayAttached = false
    private var floatingParams: WindowManager.LayoutParams? = null
    private var isOverlayExpanded: Boolean = false
    private var isCompactBubbleMode: Boolean = false
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private fun syncDebugOverlay(show: Boolean) {
        try {
            val wm = windowManager ?: return
            if (show && !isDebugOverlayAttached) {
                val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }
                val debugParams = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    layoutType,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.START
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                    }
                }
                if (debugOverlayView == null) {
                    debugOverlayView = ComposeView(this).apply {
                        setViewTreeLifecycleOwner(this@FloatingAssistantService)
                        setViewTreeViewModelStoreOwner(this@FloatingAssistantService)
                        setViewTreeSavedStateRegistryOwner(this@FloatingAssistantService)
                        setViewCompositionStrategy(androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

                        setContent {
                            val showBoxes by com.example.service.screen.DraftVisionScanner.showCalibrationBoxes.collectAsStateWithLifecycle()
                            if (showBoxes) {
                                com.example.ui.components.ScannerDebugOverlay(
                                    config = com.example.service.screen.DraftVisionScanner.calibrationConfig,
                                    overlayRect = com.example.service.screen.DraftVisionScanner.overlayRect
                                )
                            }
                        }
                    }
                }
                debugOverlayView?.let { view ->
                    wm.addView(view, debugParams)
                    isDebugOverlayAttached = true
                    AppLogger.d("FloatingService", "DebugOverlayView agregado dinámicamente")
                }
            } else if (!show && isDebugOverlayAttached) {
                debugOverlayView?.let { view ->
                    try { wm.removeViewImmediate(view) } catch (_: Throwable) {}
                    isDebugOverlayAttached = false
                    AppLogger.d("FloatingService", "DebugOverlayView retirado para liberar compositor")
                }
            }
        } catch (e: Throwable) {
            AppLogger.w("FloatingService", "Error sincronizando debug overlay: ${e.message}")
        }
    }

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        try {
            // Desactivar visor de depuración por defecto al iniciar o reabrir el overlay
            com.example.service.screen.DraftVisionScanner.showCalibrationBoxes.value = false
            com.example.data.WildRiftRepository.initChampions(applicationContext)
            screenCaptureManager = ScreenCaptureManager(this)
            savedStateRegistryController.performRestore(null)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

            createNotificationChannel()
            val notification = buildForegroundNotification()
            val hasPendingCapture = ScreenCaptureManager.pendingMediaProjectionData != null
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val fgsType = if (hasPendingCapture) {
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE or
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                    } else {
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    }
                    androidx.core.app.ServiceCompat.startForeground(
                        this,
                        NOTIFICATION_ID,
                        notification,
                        fgsType
                    )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (hasPendingCapture) {
                        androidx.core.app.ServiceCompat.startForeground(
                            this,
                            NOTIFICATION_ID,
                            notification,
                            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                        )
                    } else {
                        startForeground(NOTIFICATION_ID, notification)
                    }
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            } catch (e: Exception) {
                AppLogger.w("FloatingService", "Fallback foreground service start: ${e.message}")
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        androidx.core.app.ServiceCompat.startForeground(
                            this,
                            NOTIFICATION_ID,
                            notification,
                            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                        )
                    } else {
                        startForeground(NOTIFICATION_ID, notification)
                    }
                } catch (_: Exception) {}
            }

            if (ScreenCaptureManager.pendingMediaProjectionData != null) {
                screenCaptureManager?.initializeProjection(
                    ScreenCaptureManager.pendingMediaProjectionResultCode,
                    ScreenCaptureManager.pendingMediaProjectionData!!
                )
                ScreenCaptureManager.pendingMediaProjectionData = null
            }

            createFloatingOverlay()
            registerComponentCallbacks(this)
        } catch (e: Exception) {
            AppLogger.e("FloatingService", "Error starting floating service", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (ScreenCaptureManager.pendingMediaProjectionData != null) {
            try {
                val notification = buildForegroundNotification()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    androidx.core.app.ServiceCompat.startForeground(
                        this,
                        NOTIFICATION_ID,
                        notification,
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE or
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                    )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    androidx.core.app.ServiceCompat.startForeground(
                        this,
                        NOTIFICATION_ID,
                        notification,
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            } catch (e: Exception) {
                AppLogger.w("FloatingService", "Error asegurando tipo FGS: ${e.message}")
            }

            val success = screenCaptureManager?.initializeProjection(
                ScreenCaptureManager.pendingMediaProjectionResultCode,
                ScreenCaptureManager.pendingMediaProjectionData!!
            )
            if (success == true) {
                AppLogger.d("FloatingService", "ScreenCaptureManager initialized from pending intent.")
            }
            ScreenCaptureManager.pendingMediaProjectionData = null
        }
        return START_NOT_STICKY
    }

    private fun updateOverlayRect(params: WindowManager.LayoutParams, isExpanded: Boolean) {
        try {
            val density = resources.displayMetrics.density
            val isLandscape = resources.displayMetrics.widthPixels > resources.displayMetrics.heightPixels
            val cWidth = floatingComposeView?.width?.takeIf { it > 0 } ?: if (isExpanded) ((if (isLandscape) 560 else 330) * density).toInt() else (46 * density).toInt()
            val cHeight = floatingComposeView?.height?.takeIf { it > 0 } ?: if (isExpanded) ((if (isLandscape) 390 else 520) * density).toInt() else (46 * density).toInt()
            
            // Adjust coordinates to absolute screen pixels to match MediaProjection bitmap
            val loc = IntArray(2)
            floatingComposeView?.getLocationOnScreen(loc)
            val absoluteX = if (loc[0] != 0) loc[0] else params.x
            val absoluteY = if (loc[1] != 0) loc[1] else params.y
            
            val margin = (32 * density).toInt() // Incremented margin to be safe
            DraftVisionScanner.overlayRect = android.graphics.Rect(absoluteX - margin, absoluteY - margin, absoluteX + cWidth + margin, absoluteY + cHeight + margin)
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        if (isDestroyed) return
        isDestroyed = true

        try {
            unregisterComponentCallbacks(this)
        } catch (_: Exception) {}

        // 1. Desconectar todas las vistas del WindowManager ANTES de destruir el ciclo de vida
        removeFloatingOverlay()

        // 2. Liberar recursos de captura y apagar visor
        try {
            com.example.service.screen.DraftVisionScanner.showCalibrationBoxes.value = false
            serviceScope.cancel()
            screenCaptureManager?.release()
            screenCaptureManager = null
        } catch (_: Exception) {}

        // 3. Notificar fin de ciclo de vida y limpiar ViewModelStore de forma segura
        try {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            store.clear()
        } catch (_: Exception) {}

        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        try {
            System.gc()
        } catch (_: Exception) {}
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_MODERATE) {
            try {
                System.gc()
            } catch (_: Exception) {}
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // No detener el servicio en segundo plano para permitir uso continuo sobre Wild Rift y evitar cierres involuntarios al rotar o cambiar de app
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Asistente Flotante Wild Rift",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mantiene activo el asistente en superposición sobre Wild Rift"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, FloatingAssistantService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Coach Activo")
            .setContentText("Superposición en vivo sobre Wild Rift • Toca para abrir")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(0, "Detener", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createFloatingOverlay() {
        removeFloatingOverlay()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val density = displayMetrics.density
        val marginPx = (8 * density).toInt()
        
        val isLandscape = displayMetrics.widthPixels > displayMetrics.heightPixels
        val cardWidthPx = ((if (isLandscape) 560 else 330) * density).toInt()
        val cardHeightPx = ((if (isLandscape) 390 else 520) * density).toInt()
        var bubbleSizePx = (46 * density).toInt() // local

        

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val closeTargetParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
            y = (24 * density).toInt()
        }

        var isCloseTargetVisible by mutableStateOf(false)
        var isCloseTargetHovered by mutableStateOf(false)

        closeTargetComposeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingAssistantService)
            setViewTreeViewModelStoreOwner(this@FloatingAssistantService)
            setViewTreeSavedStateRegistryOwner(this@FloatingAssistantService)
            setViewCompositionStrategy(androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                FloatingCloseTarget(
                    isVisible = isCloseTargetVisible,
                    isTargeted = isCloseTargetHovered
                )
            }
        }

        try {
            windowManager?.addView(closeTargetComposeView, closeTargetParams)
        } catch (_: Exception) {}

        // Sincronización reactiva del visor de calibración:
        // No se agrega una ventana MATCH_PARENT permanente para evitar que Samsung Game Booster cierre el servicio.
        // Se añade únicamente cuando la depuración visual está habilitada por el usuario.
        serviceScope.launch {
            com.example.service.screen.DraftVisionScanner.showCalibrationBoxes.collect { show ->
                withContext(Dispatchers.Main) {
                    syncDebugOverlay(show)
                }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
            x = (screenWidth - bubbleSizePx - marginPx * 2).coerceAtLeast(marginPx)
            y = (120 * density).toInt()
        }
        floatingParams = params

        floatingComposeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingAssistantService)
            setViewTreeViewModelStoreOwner(this@FloatingAssistantService)
            setViewTreeSavedStateRegistryOwner(this@FloatingAssistantService)
            setViewCompositionStrategy(androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                val sharedPrefs = remember { getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
                var selectedLanguage by remember { mutableStateOf(sharedPrefs.getString("selected_language", "es") ?: "es") }

                DisposableEffect(sharedPrefs) {
                    val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
                        if (key == "selected_language") {
                            selectedLanguage = prefs.getString(key, "es") ?: "es"
                        }
                    }
                    sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
                    onDispose {
                        sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
                    }
                }

                androidx.compose.runtime.CompositionLocalProvider(LocalLanguage provides selectedLanguage) {
                    MyApplicationTheme {
                        FloatingOverlayContent(
                            state = overlayState,
                            isLandscapeMode = isDeviceLandscape.value,
                            screenCaptureManager = screenCaptureManager,
                            onClose = { stopSelf() },
                            onDragDelta = { dx, dy, isDragging, isEnded ->
                                val currentMetrics = resources.displayMetrics
                                val currentScreenWidth = currentMetrics.widthPixels
                                val currentScreenHeight = currentMetrics.heightPixels
                                val currentIsLandscape = currentScreenWidth > currentScreenHeight
                                val dynamicCardWidthPx = ((if (currentIsLandscape) 560 else 330) * density).toInt()
                                val currentWidth = if (overlayState.isExpanded) dynamicCardWidthPx else bubbleSizePx
                                val dynamicCardHeightPx = ((if (currentIsLandscape) 390 else 520) * density).toInt()
                                val currentHeight = if (overlayState.isExpanded) dynamicCardHeightPx else bubbleSizePx
                                val maxX = currentScreenWidth - marginPx
                                val maxY = (currentScreenHeight - currentHeight - marginPx).coerceAtLeast(marginPx)
                                
                                params.x = (params.x + dx).coerceIn(0, maxX)
                                params.y = (params.y + dy).coerceIn(0, maxY)

                                if (!isOverlayExpanded) {
                                    if (isDragging) {
                                        isCloseTargetVisible = true
                                        // Centro de la burbuja flotante
                                        val bubbleCenterX = params.x + bubbleSizePx / 2
                                        val bubbleCenterY = params.y + bubbleSizePx / 2
                                        
                                        // Centro del target circular inferior
                                        val targetCenterX = currentScreenWidth / 2
                                        val targetCenterY = currentScreenHeight - (24 * density).toInt() - (32 * density).toInt()
                                        
                                        val dist = kotlin.math.hypot(
                                            (bubbleCenterX - targetCenterX).toDouble(),
                                            (bubbleCenterY - targetCenterY).toDouble()
                                        )
                                        
                                        val isOver = dist < (72 * density) || (
                                            params.y >= currentScreenHeight - bubbleSizePx - (45 * density).toInt() &&
                                            kotlin.math.abs(bubbleCenterX - targetCenterX) < (80 * density).toInt()
                                        )
                                        isCloseTargetHovered = isOver
                                    } else {
                                        val shouldClose = isCloseTargetHovered
                                        isCloseTargetVisible = false
                                        isCloseTargetHovered = false

                                        if (shouldClose) {
                                            stopSelf()
                                        } else {
                                            // AUTO-SNAP: Cuando se suelta en forma de burbuja, pegarlo al borde lateral con animación fluida
                                            val targetX = if (params.x < currentScreenWidth / 2) marginPx else maxX
                                            val targetY = params.y

                                            val animator = ValueAnimator.ofFloat(0f, 1f)
                                            animator.duration = 250 // ms
                                            animator.interpolator = DecelerateInterpolator()
                                            
                                            val startX = params.x
                                            val startY = params.y
                                            
                                            animator.addUpdateListener { animation ->
                                                val fraction = animation.animatedFraction
                                                params.x = (startX + (targetX - startX) * fraction).toInt()
                                                params.y = (startY + (targetY - startY) * fraction).toInt()
                                                try {
                                                    if (this@apply.isAttachedToWindow) {
                                                        windowManager?.updateViewLayout(this@apply, params)
                                                    }
                                                } catch (_: Exception) {}
                                            }
                                            animator.start()
                                        }
                                    }
                                } else {
                                    isCloseTargetVisible = false
                                    isCloseTargetHovered = false
                                }

                                try {
                                    if (this@apply.isAttachedToWindow) {
                                        windowManager?.updateViewLayout(this@apply, params)
                                    }
                                } catch (_: Exception) {}
                            },
                            onExpandedChange = { expanded ->
                                val currentMetrics = resources.displayMetrics
                                val currentScreenWidth = currentMetrics.widthPixels
                                val currentScreenHeight = currentMetrics.heightPixels
                                isOverlayExpanded = expanded
                                isCloseTargetVisible = false
                                isCloseTargetHovered = false

                                if (expanded) {
                                    params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
                                } else {
                                    params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                }
                                if (expanded) {
                                    if (params.x + cardWidthPx > currentScreenWidth - marginPx) {
                                        params.x = (currentScreenWidth - cardWidthPx - marginPx).coerceAtLeast(marginPx)
                                    }
                                    if (params.y + cardHeightPx > currentScreenHeight - marginPx) {
                                        params.y = (currentScreenHeight - cardHeightPx - marginPx).coerceAtLeast(marginPx)
                                    }
                                }
                                try {
                                    if (this@apply.isAttachedToWindow) {
                                        windowManager?.updateViewLayout(this@apply, params)
                                        this@FloatingAssistantService.updateOverlayRect(params, isOverlayExpanded)
                                    }
                                } catch (_: Exception) {}
                            },
                            onCompactModeChange = { isCompact ->
                                isCompactBubbleMode = isCompact
                                bubbleSizePx = ((if (isCompact) 36f else 46f) * density).toInt()
                            }
                        )
                    }
                }
            }
        }

        try {
            windowManager?.addView(floatingComposeView, params)
        } catch (_: Exception) {}
    }

    private fun removeFloatingOverlay() {
        try {
            val wm = windowManager ?: return
            floatingComposeView?.let { view ->
                try { wm.removeViewImmediate(view) } catch (_: Exception) {}
            }
            floatingComposeView = null
            closeTargetComposeView?.let { view ->
                try { wm.removeViewImmediate(view) } catch (_: Exception) {}
            }
            closeTargetComposeView = null
            debugOverlayView?.let { view ->
                try { wm.removeViewImmediate(view) } catch (_: Exception) {}
            }
            debugOverlayView = null
            isDebugOverlayAttached = false
        } catch (_: Exception) {}
    }

    private var lastScreenWidth = 0
    private var lastScreenHeight = 0

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        try {
            val metrics = resources.displayMetrics
            val isNowLandscape = newConfig.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE || metrics.widthPixels > metrics.heightPixels
            isDeviceLandscape.value = isNowLandscape

            // Notificar al ScreenCaptureManager de la rotación de manera segura
            try {
                screenCaptureManager?.refreshProjection()
            } catch (_: Throwable) {}

            fun applyClampedLayout() {
                try {
                    val curMetrics = resources.displayMetrics
                    val screenW = curMetrics.widthPixels
                    val screenH = curMetrics.heightPixels
                    lastScreenWidth = screenW
                    lastScreenHeight = screenH

                    val params = floatingParams ?: return
                    val view = floatingComposeView ?: return
                    if (!view.isAttachedToWindow) return

                    val density = curMetrics.density
                    val marginPx = (8 * density).toInt()
                    val currentBubblePx = ((if (overlayState.isCompactBubble) 36f else 46f) * density).toInt()
                    val cardWidthPx = ((if (isNowLandscape) 550 else 330) * density).toInt()
                    val cardHeightPx = ((if (isNowLandscape) 345 else 520) * density).toInt()

                    val viewWidth = if (overlayState.isExpanded) cardWidthPx else currentBubblePx
                    val viewHeight = if (overlayState.isExpanded) cardHeightPx else currentBubblePx

                    val maxX = (screenW - viewWidth - marginPx).coerceAtLeast(marginPx)
                    val maxY = (screenH - viewHeight - marginPx).coerceAtLeast(marginPx)

                    params.x = params.x.coerceIn(0, maxX)
                    params.y = params.y.coerceIn(0, maxY)

                    windowManager?.updateViewLayout(view, params)
                    updateOverlayRect(params, isOverlayExpanded)
                } catch (eLayout: Throwable) {
                    AppLogger.w("FloatingService", "Error actualizando layout en rotación: ${eLayout.message}")
                }
            }

            // Aplicar inmediatamente y reaplicar tras 150ms para sincronizar con la animación de rotación del sistema
            applyClampedLayout()
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                applyClampedLayout()
            }, 150L)

        } catch (e: Throwable) {
            AppLogger.w("FloatingService", "Error adaptando layout tras cambio de configuración: ${e.message}")
        }
    }

    companion object {
        const val ACTION_START = "ACTION_START_FLOATING_ASSISTANT"
        const val ACTION_STOP = "ACTION_STOP_FLOATING_ASSISTANT"
        const val CHANNEL_ID = "wildrift_overlay_channel"
        const val NOTIFICATION_ID = 2001
    }
}

@Composable
private fun FloatingCloseTarget(
    isVisible: Boolean,
    isTargeted: Boolean
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn(initialScale = 0.5f),
        exit = fadeOut() + scaleOut(targetScale = 0.5f)
    ) {
        val targetSize by animateDpAsState(
            targetValue = if (isTargeted) 68.dp else 54.dp,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )
        val targetBgColor by animateColorAsState(
            targetValue = if (isTargeted) DangerRed else Color(0xDD12151D),
            animationSpec = tween(150)
        )
        val targetBorderColor by animateColorAsState(
            targetValue = if (isTargeted) Color.White else DangerRed.copy(alpha = 0.75f),
            animationSpec = tween(150)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(targetSize)
                    .clip(CircleShape)
                    .background(targetBgColor)
                    .border(2.5.dp, targetBorderColor, CircleShape)
                    .shadow(elevation = if (isTargeted) 16.dp else 6.dp, shape = CircleShape, ambientColor = DangerRed, spotColor = DangerRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar y desactivar overlay",
                    tint = Color.White,
                    modifier = Modifier.size(if (isTargeted) 32.dp else 24.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isTargeted) DangerRed else Color.Black.copy(alpha = 0.75f),
                border = BorderStroke(1.dp, if (isTargeted) Color.White else DangerRed.copy(alpha = 0.4f))
            ) {
                Text(
                    text = if (isTargeted) "✕ Soltar para desactivar" else "Arrastra aquí para cerrar",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

class OverlayState {
    var isExpanded by androidx.compose.runtime.mutableStateOf(false)
    var overlayHubTab by androidx.compose.runtime.mutableStateOf(OverlayHubTab.DRAFT)
    var showSaveDraftDialog by androidx.compose.runtime.mutableStateOf(false)
    var showRoleChangeDialog by androidx.compose.runtime.mutableStateOf(false)
    var isSavedRecently by androidx.compose.runtime.mutableStateOf(false)
    var activeRole by androidx.compose.runtime.mutableStateOf(LaneRole.MID)
    var isRoleManuallySelected by androidx.compose.runtime.mutableStateOf(false)
    var isFirstPick by androidx.compose.runtime.mutableStateOf(false)
    var isFirstPickManuallySelected by androidx.compose.runtime.mutableStateOf(false)
    var isLegendaryQueue by androidx.compose.runtime.mutableStateOf(false)
    var isCompactBubble by androidx.compose.runtime.mutableStateOf(false)
    var isScanning by androidx.compose.runtime.mutableStateOf(false)
    var autoScanEnabled by androidx.compose.runtime.mutableStateOf(false)
    var scanNoticeMessage by androidx.compose.runtime.mutableStateOf<String?>(null)
    var isDraggingBubble by androidx.compose.runtime.mutableStateOf(false)
    var dragAccumulatedY by androidx.compose.runtime.mutableFloatStateOf(0f)
    var isNearCloseThreshold by androidx.compose.runtime.mutableStateOf(false)
    var selectedChampionDetail by androidx.compose.runtime.mutableStateOf<com.example.model.Champion?>(null)
    var showChampionPickerForSlot by androidx.compose.runtime.mutableStateOf<Pair<Boolean, Int>?>(null)
    var isLoadingScreenMode by androidx.compose.runtime.mutableStateOf(false)
    var isOverlayTabsMinimized by androidx.compose.runtime.mutableStateOf(false)
    val allies = androidx.compose.runtime.mutableStateListOf<com.example.model.Champion?>().apply { repeat(5) { add(null) } }
    val enemies = androidx.compose.runtime.mutableStateListOf<com.example.model.Champion?>().apply { repeat(5) { add(null) } }
    val enemyConfidences = androidx.compose.runtime.mutableStateMapOf<LaneRole, Int>()
    val manualLockedAllySlots = androidx.compose.runtime.mutableStateMapOf<Int, Boolean>()
    val manualLockedEnemySlots = androidx.compose.runtime.mutableStateMapOf<Int, Boolean>()
    val allySummonerNames = androidx.compose.runtime.mutableStateMapOf<Int, String>()
    val enemySummonerNames = androidx.compose.runtime.mutableStateMapOf<Int, String>()
    val allySpells = androidx.compose.runtime.mutableStateMapOf<Int, List<String>>()
    val enemySpells = androidx.compose.runtime.mutableStateMapOf<Int, List<String>>()
}

@Composable
private fun FloatingOverlayContent(
    state: OverlayState,
    isLandscapeMode: Boolean,
    screenCaptureManager: ScreenCaptureManager?,
    onClose: () -> Unit,
    onDragDelta: (dx: Int, dy: Int, isDragging: Boolean, isEnded: Boolean) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onCompactModeChange: (Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isExpanded by state::isExpanded
    var overlayHubTab by state::overlayHubTab
    var showSaveDraftDialog by state::showSaveDraftDialog
    var showRoleChangeDialog by state::showRoleChangeDialog
    var isSavedRecently by state::isSavedRecently
    val isPremium by com.example.util.SubscriptionManager.isPremium.collectAsStateWithLifecycle()
    val activeProfileId by com.example.data.AccountProfileManager.activeProfileId.collectAsStateWithLifecycle()
    val isLoggedInAndPremium = isPremium && activeProfileId != null
    val userRole by com.example.util.SubscriptionManager.userRole.collectAsStateWithLifecycle()
    val currentAuthEmail = remember { com.example.util.AuthManager.getAuth()?.currentUser?.email }
    val isAdmin = userRole == "admin" || userRole == "moderador" || (currentAuthEmail != null && currentAuthEmail.contains("barbadiego", ignoreCase = true)) || com.example.util.AuthManager.isCurrentUserAdmin()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var activeRole by state::activeRole
    
    val sharedPrefs = remember { context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }
    
    LaunchedEffect(Unit) {
        val savedRoleStr = sharedPrefs.getString("saved_active_role", null)
        if (savedRoleStr != null) {
            try {
                activeRole = LaneRole.valueOf(savedRoleStr)
                state.isRoleManuallySelected = true
            } catch (e: Exception) { }
        }
    }
    
    LaunchedEffect(activeRole) {
        sharedPrefs.edit().putString("saved_active_role", activeRole.name).apply()
    }

    var isFirstPick by state::isFirstPick
    var isLegendaryQueue by state::isLegendaryQueue
    var isCompactBubble by state::isCompactBubble
    var showLiteRTViewer by remember { mutableStateOf(false) }

    val defaultRoles = remember { listOf(LaneRole.TOP, LaneRole.JUNGLE, LaneRole.MID, LaneRole.ADC, LaneRole.SUPPORT) }
    val allies = state.allies
    val enemies = state.enemies
    val manualLockedAllySlots = state.manualLockedAllySlots
    val manualLockedEnemySlots = state.manualLockedEnemySlots

    // Funciones de asignación con Regla Estricta MOBA de Unicidad Absoluta (ningún campeón puede duplicarse en ningún bando)
    val assignAllySlot: (Int, Champion) -> Unit = { targetIdx, champ ->
        for (i in 0 until 5) {
            if (enemies[i]?.id == champ.id) enemies[i] = null
        }
        for (i in 0 until 5) {
            if (i != targetIdx && allies[i]?.id == champ.id) allies[i] = null
        }
        if (targetIdx in 0 until 5) {
            allies[targetIdx] = champ
        }
    }

    val assignEnemySlot: (Int, Champion, Int?) -> Unit = { targetIdx, champ, conf ->
        // PROTECCIÓN ESTRICTA: Si el campeón ya está en el equipo aliado, NUNCA transferirlo al rival
        if (!allies.any { it?.id == champ.id }) {
            for (i in 0 until 5) {
                if (i != targetIdx && enemies[i]?.id == champ.id) {
                    enemies[i] = null
                    defaultRoles.getOrNull(i)?.let { state.enemyConfidences.remove(it) }
                }
            }
            if (targetIdx in 0 until 5) {
                enemies[targetIdx] = champ
                defaultRoles.getOrNull(targetIdx)?.let { role ->
                    state.enemyConfidences[role] = conf ?: 85
                }
            }
        } else {
            AppLogger.d("Overlay", "Ignorando asignación enemiga de ${champ.name}: pertenece al equipo aliado")
        }
    }

    var isScanning by state::isScanning
    var autoScanEnabled by state::autoScanEnabled
    var scanNoticeMessage by state::scanNoticeMessage

    var isDraggingBubble by state::isDraggingBubble
    var dragAccumulatedY by state::dragAccumulatedY
    var isNearCloseThreshold by state::isNearCloseThreshold

    var selectedChampionDetail by state::selectedChampionDetail
    var showChampionPickerForSlot by state::showChampionPickerForSlot
    var isLoadingScreenMode by state::isLoadingScreenMode
    var isOverlayTabsMinimized by state::isOverlayTabsMinimized

    val explicitEnemyOpponent = remember(activeRole, enemies.toList()) {
        val roleIndex = defaultRoles.indexOf(activeRole).coerceIn(0, 4)
        enemies.getOrNull(roleIndex)
    }

    val analysis = remember(activeRole, isFirstPick, allies.toList(), enemies.toList(), explicitEnemyOpponent) {
        WildRiftRepository.analyzeDraft(
            myRole = activeRole,
            allies = allies.filterNotNull(),
            enemies = enemies.filterNotNull(),
            enemyLaneOpponent = explicitEnemyOpponent,
            isFirstPick = isFirstPick
        )
    }

    // FRAME SKIPPING & BACKGROUND PROCESSING STRATEGY:
    // El bucle de visión se ejecuta en un contexto de segundo plano desacoplado (Dispatchers.Default),
    // liberando por completo el hilo principal (UI) para garantizar una tasa de refresco fluida (60-120 FPS)
    // tanto en el live scan overlay (ScannerDebugOverlay) como en las animaciones y gestos de la burbuja.
    // Estrategia de salto de fotogramas (Frame Skipping Strategy):
    // 1. In-flight Concurrency Guard (AtomicBoolean): Si el motor de visión ya se encuentra procesando
    //    una inferencia (OCR o LiteRT), cualquier fotograma entrante o ciclo es descartado de inmediato.
    // 2. User Gesture Throttling: Cuando el usuario arrastra la burbuja (isDraggingBubble) o interactúa
    //    con diálogos modales, la captura se suspende, dedicando el 100% de GPU y CPU a la fluidez táctil.
    // 3. Adaptive Cooldown: Si una inferencia toma más tiempo del previsto, se aplica un intervalo compensatorio
    //    para evitar saturación térmica y mantener estabilidad de fotogramas en Android.
    // 4. Null-Frame Drop: Si el ImageReader no tiene un fotograma nuevo, se salta el ciclo sin re-analizar imágenes estáticas.
    LaunchedEffect(autoScanEnabled) {
        if (!autoScanEnabled) {
            DraftVisionScanner.isVisionEngineBusy.value = false
            return@LaunchedEffect
        }

        withContext(Dispatchers.Default) {
            val isProcessingFrame = java.util.concurrent.atomic.AtomicBoolean(false)
            var loopCycleCounter = 0L
            var fpsLastTime = System.currentTimeMillis()
            var framesInSecond = 0

            while (autoScanEnabled) {
                // 1. REGLA DE SALTO: Arrastre o interacción de usuario
                if (isDraggingBubble || showSaveDraftDialog || showRoleChangeDialog) {
                    DraftVisionScanner.recordFrameSkipped()
                    delay(80L)
                    continue
                }

                // 2. REGLA DE SALTO: In-flight Concurrency Guard (si hay un análisis activo, omitir)
                if (!isProcessingFrame.compareAndSet(false, true)) {
                    DraftVisionScanner.recordFrameSkipped()
                    delay(30L)
                    continue
                }

                loopCycleCounter++
                val cycleStartTime = System.currentTimeMillis()

                // Medición de fotogramas de escaneo por segundo (Scan FPS)
                framesInSecond++
                val nowTime = System.currentTimeMillis()
                if (nowTime - fpsLastTime >= 1000L) {
                    val computedFps = (framesInSecond * 1000f) / (nowTime - fpsLastTime).coerceAtLeast(1L)
                    DraftVisionScanner.updateFps(computedFps)
                    framesInSecond = 0
                    fpsLastTime = nowTime
                }

                try {
                    DraftVisionScanner.isVisionEngineBusy.value = true

                    val confirmedPicksCount = allies.count { it != null } + enemies.count { it != null }
                    val tentativeFirstPick = isFirstPick ?: true
                    val sequence = DraftVisionScanner.getDraftPickSequence(tentativeFirstPick)
                    val activeTurns = DraftVisionScanner.computeActiveSelectionTurns(
                        sequence,
                        DraftVisionScanner.allySlotConfirmedChampions,
                        DraftVisionScanner.enemySlotConfirmedChampions
                    )

                    val isDraftComplete = (confirmedPicksCount >= 10)
                    val hasActiveTurns = activeTurns.isNotEmpty() && !isDraftComplete
                    val isTenthPickActive = activeTurns.any { it.turnNumber == 10 } || confirmedPicksCount >= 8
                    // Garantizar ciclo de sincronización global periódico o continuo si el visor está abierto
                    val isGlobalSyncCycle = showLiteRTViewer || isDraftComplete || !hasActiveTurns || (loopCycleCounter % 4L == 0L)

                    val dynamicLoopDelay = when {
                        showLiteRTViewer -> 50L // 20 Hz ultra-fluido en vivo para pruebas del usuario
                        isDraftComplete -> 800L
                        !isGlobalSyncCycle && hasActiveTurns -> 50L
                        isTenthPickActive -> 60L
                        else -> 120L
                    }

                    if (screenCaptureManager == null || !screenCaptureManager.isReady()) {
                        withContext(Dispatchers.Main) {
                            scanNoticeMessage = "Permiso de captura inactivo. Toca aquí para activarlo."
                        }
                        delay(500L)
                    } else if (!isScanning) {
                        val bitmap = screenCaptureManager?.captureCurrentFrame()
                        if (bitmap == null || bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) {
                            // Salto de fotograma: no hay imagen nueva disponible en ImageReader
                            DraftVisionScanner.recordFrameSkipped()
                        } else {
                            try {
                                if (!isGlobalSyncCycle && hasActiveTurns) {
                                    // -----------------------------------------------------------------
                                    // RUTA DE ALTA PRIORIDAD: ESCANEO DIRIGIDO DEL SLOT ACTIVO (<30ms)
                                    // -----------------------------------------------------------------
                                    val detectedPicks = mutableListOf<Pair<DraftPickTurn, Champion>>()
                                    for (turn in activeTurns) {
                                        val activeResult = DraftVisionScanner.scanActiveSlotDirectly(bitmap, turn, context)
                                        val champ = activeResult?.champion
                                        if (champ != null) {
                                            detectedPicks.add(turn to champ)
                                        }
                                    }

                                    if (detectedPicks.isNotEmpty()) {
                                        withContext(Dispatchers.Main) {
                                            var fastPicksAdded = 0
                                            for ((turn, champ) in detectedPicks) {
                                                if (turn.isAlly) {
                                                    val role = DraftVisionScanner.getAllySlotRole(turn.slotIndex)
                                                    val roleIdx = defaultRoles.indexOf(role)
                                                    val emptyIdx = allies.indices.firstOrNull { allies[it] == null && manualLockedAllySlots[it] != true }
                                                    val targetIdx = when {
                                                        roleIdx != -1 && allies[roleIdx] == null && manualLockedAllySlots[roleIdx] != true -> roleIdx
                                                        emptyIdx != null -> emptyIdx
                                                        manualLockedAllySlots[turn.slotIndex] != true -> turn.slotIndex
                                                        else -> allies.indices.firstOrNull { manualLockedAllySlots[it] != true } ?: turn.slotIndex
                                                    }
                                                    if (manualLockedAllySlots[targetIdx] != true && allies[targetIdx]?.id != champ.id) {
                                                        assignAllySlot(targetIdx, champ)
                                                        DraftVisionScanner.allySlotConfirmedChampions[turn.slotIndex] = champ
                                                        fastPicksAdded++
                                                        AppLogger.d("HighPriorityLoop", "Slot Aliado Activo ${turn.slotIndex} ($role) fijado en $targetIdx: ${champ.name}")
                                                    }
                                                } else {
                                                    val emptyIdx = enemies.indices.firstOrNull { enemies[it] == null && manualLockedEnemySlots[it] != true }
                                                    val targetIdx = when {
                                                        emptyIdx != null -> emptyIdx
                                                        manualLockedEnemySlots[turn.slotIndex] != true -> turn.slotIndex
                                                        else -> enemies.indices.firstOrNull { manualLockedEnemySlots[it] != true } ?: turn.slotIndex
                                                    }
                                                    if (manualLockedEnemySlots[targetIdx] != true && enemies[targetIdx]?.id != champ.id) {
                                                        assignEnemySlot(targetIdx, champ, 100)
                                                        DraftVisionScanner.enemySlotConfirmedChampions[turn.slotIndex] = champ
                                                        fastPicksAdded++
                                                        AppLogger.d("HighPriorityLoop", "Slot Rival Activo ${turn.slotIndex} fijado en $targetIdx: ${champ.name}")
                                                    }
                                                }
                                            }
                                            if (fastPicksAdded > 0) {
                                                val totalAllies = allies.filterNotNull().size
                                                val totalEnemies = enemies.filterNotNull().size
                                                if (totalAllies == 5 && totalEnemies == 5) {
                                                    autoScanEnabled = false
                                                    scanNoticeMessage = "10/10 Campeones confirmados"
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // -----------------------------------------------------------------
                                    // RUTA DE BAJA FRECUENCIA: SINCRONIZACIÓN GLOBAL Y SLOTS INACTIVOS
                                    // -----------------------------------------------------------------
                                    val result = DraftVisionScanner.scanDraftFromBitmap(bitmap, context, isFirstPick, activeRole)
                                    if (result.isSuccessful) {
                                        withContext(Dispatchers.Main) {
                                            if (result.detectedFirstPick != null && !state.isFirstPickManuallySelected) {
                                                isFirstPick = result.detectedFirstPick
                                            }

                                            var newAlliesAdded = 0
                                            var newEnemiesAdded = 0
                                            
                                            defaultRoles.forEachIndexed { idx, role ->
                                                if (manualLockedAllySlots[idx] != true) {
                                                    val scannedAlly = result.alliesByRole[role]
                                                    if (scannedAlly != null) {
                                                        if (allies[idx] == null || allies[idx]?.id != scannedAlly.id) {
                                                            assignAllySlot(idx, scannedAlly)
                                                            newAlliesAdded++
                                                        }
                                                    }
                                                }
                                                if (manualLockedEnemySlots[idx] != true) {
                                                    val scannedEnemy = result.enemiesByRole[role]
                                                    if (scannedEnemy != null) {
                                                        if (enemies[idx] == null || enemies[idx]?.id != scannedEnemy.id) {
                                                            assignEnemySlot(idx, scannedEnemy, result.enemyConfidencesByRole[role])
                                                            if (enemies[idx] == null) newEnemiesAdded++
                                                        }
                                                    }
                                                }
                                            }

                                            // Preservar campeones aliados no asignados con correspondencia de rol estricta
                                            for (slotIdx in 0..4) {
                                                val champ = result.alliesBySlot[slotIdx] ?: continue
                                                val isAlreadyInAllies = allies.any { it?.id == champ.id }
                                                if (!isAlreadyInAllies) {
                                                    val targetRole = DraftVisionScanner.allySlotRolesCache[slotIdx] ?: champ.primaryRole
                                                    val roleIdx = defaultRoles.indexOf(targetRole)
                                                    val targetIdx = if (roleIdx in 0..4 && allies[roleIdx] == null && manualLockedAllySlots[roleIdx] != true) {
                                                        roleIdx
                                                    } else {
                                                        allies.indices.firstOrNull { allies[it] == null && manualLockedAllySlots[it] != true }
                                                    }
                                                    if (targetIdx != null) {
                                                        assignAllySlot(targetIdx, champ)
                                                        newAlliesAdded++
                                                        AppLogger.d(TAG, "Campeón aliado detectado asignado a slot $targetIdx (${defaultRoles[targetIdx].shortName}): ${champ.name}")
                                                    }
                                                }
                                            }

                                            // Asignación directa y garantizada del 10º Pick
                                            val tenthChamp = result.lastPickChampion
                                            if (tenthChamp != null) {
                                                val isTenthAlly = result.tenthPickIsAlly ?: (!isFirstPick)
                                                if (isTenthAlly) {
                                                    val emptyIdx = allies.indices.firstOrNull { idx -> allies[idx] == null && manualLockedAllySlots[idx] != true } ?: -1
                                                    val targetIdx = if (emptyIdx != -1) emptyIdx else (result.tenthPickSlotIndex ?: 4).coerceIn(0, 4)
                                                    if (manualLockedAllySlots[targetIdx] != true) {
                                                        assignAllySlot(targetIdx, tenthChamp)
                                                        DraftVisionScanner.allySlotConfirmedChampions[(result.tenthPickSlotIndex ?: targetIdx).coerceIn(0, 4)] = tenthChamp
                                                        newAlliesAdded++
                                                        AppLogger.d(TAG, "10º Pick asignado automáticamente a Aliado Slot $targetIdx: ${tenthChamp.name}")
                                                    }
                                                } else {
                                                    val emptyIdx = enemies.indices.firstOrNull { idx -> enemies[idx] == null && manualLockedEnemySlots[idx] != true } ?: -1
                                                    val targetIdx = if (emptyIdx != -1) emptyIdx else (result.tenthPickSlotIndex ?: 4).coerceIn(0, 4)
                                                    if (manualLockedEnemySlots[targetIdx] != true) {
                                                        assignEnemySlot(targetIdx, tenthChamp, 100)
                                                        DraftVisionScanner.enemySlotConfirmedChampions[(result.tenthPickSlotIndex ?: targetIdx).coerceIn(0, 4)] = tenthChamp
                                                        newEnemiesAdded++
                                                        AppLogger.d(TAG, "10º Pick asignado automáticamente a Rival Slot $targetIdx: ${tenthChamp.name}")
                                                    }
                                                }
                                            }

                                            val currentAllyPicks = allies.count { it != null }
                                            val currentEnemyPicks = enemies.count { it != null }
                                            if (!state.isFirstPickManuallySelected) {
                                                if (currentEnemyPicks > 0 && currentAllyPicks == 0) {
                                                    isFirstPick = false
                                                } else if (currentAllyPicks > 0 && currentEnemyPicks == 0) {
                                                    isFirstPick = true
                                                }
                                            }

                                            if (result.isLegendaryRanked) {
                                                if (!isLegendaryQueue) {
                                                    isLegendaryQueue = true
                                                }
                                                if (state.allySummonerNames.isNotEmpty()) {
                                                    state.allySummonerNames.clear()
                                                }
                                            } else {
                                                defaultRoles.forEachIndexed { idx, role ->
                                                    val sName = result.allySummonerNamesByRole[role] ?: result.allySummonerNamesBySlot[idx]
                                                    if (!sName.isNullOrBlank()) {
                                                        val current = state.allySummonerNames[idx]
                                                        if (current.isNullOrBlank() || sName.length > current.length || (sName.contains(" ") && !current.contains(" "))) {
                                                            state.allySummonerNames[idx] = sName
                                                        }
                                                    }
                                                    val spells = result.allySpellsByRole[role] ?: result.allySpellsBySlot[idx]
                                                    if (!spells.isNullOrEmpty()) {
                                                        state.allySpells[idx] = spells
                                                    }
                                                }
                                            }
                                            if (state.enemySpells.isNotEmpty()) {
                                                state.enemySpells.clear()
                                            }

                                            val finalAlliesPicked = allies.filterNotNull().size
                                            val finalEnemiesPicked = enemies.filterNotNull().size
                                            val isDraftFullyConfirmed = (finalAlliesPicked == 5 && finalEnemiesPicked == 5)

                                            if (result.userExplicitlyDetectedRole != null && activeRole != result.userExplicitlyDetectedRole) {
                                                activeRole = result.userExplicitlyDetectedRole
                                                com.example.util.UserPreferences.setActiveDraftRole(context, result.userExplicitlyDetectedRole)
                                                scanNoticeMessage = "Auto-Scan: Tu rol detectado (${result.userExplicitlyDetectedRole.shortName})"
                                            } else if (isDraftFullyConfirmed) {
                                                autoScanEnabled = false
                                                scanNoticeMessage = "10/10 Campeones confirmados"
                                                AppLogger.i("FloatingService", "Auto-Scan desactivado: 10/10 campeones confirmados.")
                                            } else if (result.isPreparationPhase && (finalAlliesPicked < 5 || finalEnemiesPicked < 5)) {
                                                scanNoticeMessage = "Fase de Preparación: completando selección ($finalAlliesPicked/5 vs $finalEnemiesPicked/5)..."
                                            } else if (newAlliesAdded > 0 || newEnemiesAdded > 0) {
                                                scanNoticeMessage = "Auto-Scan: +${newAlliesAdded + newEnemiesAdded} picks detectados ($finalAlliesPicked/5 vs $finalEnemiesPicked/5)"
                                            }

                                            if (scanNoticeMessage != null) {
                                                coroutineScope.launch {
                                                    delay(2000)
                                                    scanNoticeMessage = null
                                                }
                                            }
                                        }
                                    }
                                }
                                val processDuration = System.currentTimeMillis() - cycleStartTime
                                DraftVisionScanner.recordFrameProcessed(processDuration)
                            } finally {
                                try {
                                    bitmap.recycle()
                                } catch (_: Throwable) {}
                            }
                        }
                    }

                    val totalCycleDuration = System.currentTimeMillis() - cycleStartTime
                    // 3. ESTRATEGIA ADAPTATIVA: Si el ciclo tomó más tiempo del previsto,
                    // compensar el retraso para evitar ráfagas y proteger la tasa de fotogramas del live overlay
                    val compensatedDelay = (dynamicLoopDelay - totalCycleDuration).coerceAtLeast(30L)
                    delay(compensatedDelay)

                } catch (t: Throwable) {
                    AppLogger.e("FloatingService", "Error in auto-scan loop", t)
                    delay(200L)
                } finally {
                    isProcessingFrame.set(false)
                    DraftVisionScanner.isVisionEngineBusy.value = false
                }
            }
        }
    }

    fun triggerManualScan() {
        if (screenCaptureManager?.isReady() != true) {
            scanNoticeMessage = "Requiere permiso de pantalla. Abriendo solicitud..."
            try {
                val reqIntent = Intent(context, com.example.MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra("EXTRA_REQUEST_CAPTURE", true)
                }
                context.startActivity(reqIntent)
            } catch (_: Exception) {}
            coroutineScope.launch {
                delay(3500)
                scanNoticeMessage = null
            }
            return
        }
        isScanning = true
        scanNoticeMessage = "Escaneando selección en directo..."
        coroutineScope.launch(Dispatchers.IO) {
            val bitmap = screenCaptureManager?.captureCurrentFrame()
            if (bitmap != null) {
                val result = DraftVisionScanner.scanDraftFromBitmap(bitmap, context, isFirstPick, activeRole)
                withContext(Dispatchers.Main) {
                    if (result.isSuccessful) {
                        // Sincronizar primera selección si se detectó y no ha sido fijada manualmente
                        if (result.detectedFirstPick != null && !state.isFirstPickManuallySelected) {
                            isFirstPick = result.detectedFirstPick
                        }

                        // 1. Asignación directa y de alta precisión por rol (respetando selecciones manuales)
                        defaultRoles.forEachIndexed { idx, role ->
                            if (manualLockedAllySlots[idx] != true) {
                                val scannedAlly = result.alliesByRole[role]
                                if (scannedAlly != null) {
                                    assignAllySlot(idx, scannedAlly)
                                }
                            }
                            if (manualLockedEnemySlots[idx] != true) {
                                val scannedEnemy = result.enemiesByRole[role]
                                if (scannedEnemy != null) {
                                    assignEnemySlot(idx, scannedEnemy, result.enemyConfidencesByRole[role])
                                }
                            }
                        }

                        // Asignación directa y garantizada del 10º Pick respetando el bando
                        val tenthChamp = result.lastPickChampion
                        if (tenthChamp != null) {
                            val isTenthAlly = result.tenthPickIsAlly ?: (!isFirstPick)
                            if (isTenthAlly) {
                                val targetIdx = (result.tenthPickSlotIndex ?: allies.indexOfFirst { it == null }).let { if (it in 0..4) it else 4 }
                                if (manualLockedAllySlots[targetIdx] != true) {
                                    assignAllySlot(targetIdx, tenthChamp)
                                    AppLogger.d(TAG, "10º Pick asignado manualmente/directo a Aliado Slot $targetIdx: ${tenthChamp.name}")
                                }
                            } else {
                                val targetIdx = (result.tenthPickSlotIndex ?: enemies.indexOfFirst { it == null }).let { if (it in 0..4) it else 4 }
                                if (manualLockedEnemySlots[targetIdx] != true) {
                                    assignEnemySlot(targetIdx, tenthChamp, 100)
                                    AppLogger.d(TAG, "10º Pick asignado manualmente/directo a Rival Slot $targetIdx: ${tenthChamp.name}")
                                }
                            }
                        }

                        // Verificación complementaria: si el rival ya tiene picks y aliados no, rival eligió 1º
                        val currentAllyPicks = allies.count { it != null }
                        val currentEnemyPicks = enemies.count { it != null }
                        if (!state.isFirstPickManuallySelected) {
                            if (currentEnemyPicks > 0 && currentAllyPicks == 0) {
                                isFirstPick = false
                            } else if (currentAllyPicks > 0 && currentEnemyPicks == 0) {
                                isFirstPick = true
                            }
                        }

                        // Sincronizar nombres de invocador aliados y hechizos
                        if (result.isLegendaryRanked) {
                            state.allySummonerNames.clear()
                        } else {
                            defaultRoles.forEachIndexed { idx, role ->
                                val sName = result.allySummonerNamesByRole[role] ?: result.allySummonerNamesBySlot[idx]
                                if (!sName.isNullOrBlank()) {
                                    val current = state.allySummonerNames[idx]
                                    if (current.isNullOrBlank() || sName.length > current.length || (sName.contains(" ") && !current.contains(" "))) {
                                        state.allySummonerNames[idx] = sName
                                    }
                                }
                                val spells = result.allySpellsByRole[role] ?: result.allySpellsBySlot[idx]
                                if (!spells.isNullOrEmpty()) {
                                    state.allySpells[idx] = spells
                                }
                            }
                        }
                        state.enemySpells.clear()

                        val totalAlliesPicked = allies.filterNotNull().size
                        val totalEnemiesPicked = enemies.filterNotNull().size

                        // Fase de confirmación de picks finalizada mediante escaneo local y OCR
                        if (result.detectedRole != null) {
                            activeRole = result.detectedRole
                            com.example.util.UserPreferences.setActiveDraftRole(context, result.detectedRole)
                        }
                        val totalDetected = allies.filterNotNull().size + enemies.filterNotNull().size
                        scanNoticeMessage = if (allies.filterNotNull().size == 5 && enemies.filterNotNull().size == 5) {
                            "10/10 Campeones confirmados"
                        } else {
                            "Escaneo exitoso ($totalDetected picks" +
                                (if (result.detectedRole != null) ", tu rol: ${result.detectedRole.shortName})" else ")")
                        }
                    } else {
                        scanNoticeMessage = result.statusMessage
                    }
                    isScanning = false
                }
            } else {
                withContext(Dispatchers.Main) {
                    scanNoticeMessage = "No hay frame de captura disponible"
                    isScanning = false
                }
            }
            delay(3500)
            scanNoticeMessage = null
        }
    }

    Box(modifier = Modifier.padding(2.dp)) {
        Column(horizontalAlignment = Alignment.Start) {
            if (!isExpanded) {
                // Minimized Floating Bubble
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val bubbleBorderColor by animateColorAsState(
                        targetValue = if (isNearCloseThreshold) DangerRed else (if (isScanning) HextechCyan else HextechGold),
                        animationSpec = tween(200)
                    )

                    Box(
                        modifier = Modifier
                            .size(if (isCompactBubble) 36.dp else 46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(HextechCyan, Color(0xFF005A82), HextechDarkBg)
                                )
                            )
                            .border(2.5.dp, if (isScanning) HextechCyan else HextechGold, CircleShape)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = {
                                        isDraggingBubble = true
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        onDragDelta(dragAmount.x.roundToInt(), dragAmount.y.roundToInt(), true, false)
                                    },
                                    onDragEnd = {
                                        isDraggingBubble = false
                                        onDragDelta(0, 0, false, false)
                                    },
                                    onDragCancel = {
                                        isDraggingBubble = false
                                        onDragDelta(0, 0, false, false)
                                    }
                                )
                            }
                            .clickable {
                                isExpanded = true
                                onExpandedChange(true)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(if (isCompactBubble) 28.dp else 36.dp),
                                color = HextechCyan,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Image(
                                painter = painterResource(id = com.example.R.drawable.ic_overlay_logo),
                                contentDescription = "Wild Rift Drafting Coach",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(if (isCompactBubble) 32.dp else 42.dp)
                                    .clip(CircleShape)
                            )
                        }

                        if (!isScanning) {
                            // Pulsing green auto-scan indicator
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .align(Alignment.TopEnd)
                                    .clip(CircleShape)
                                    .background(if (autoScanEnabled && isAdmin) Color(0xFF00FF7F) else HextechGold)
                            )
                        }
                    }

                    // Indicador sutil de arrastre
                    if (isDraggingBubble) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tr("↓ Arrastra al círculo inferior para cerrar"),
                                color = TextSecondary,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    val isBubbleLiveVisionActive by com.example.service.screen.DraftVisionScanner.showCalibrationBoxes.collectAsStateWithLifecycle()
                    if (isBubbleLiveVisionActive && !isDraggingBubble) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Surface(
                            modifier = Modifier
                                .clickable {
                                    com.example.service.screen.DraftVisionScanner.showCalibrationBoxes.value = false
                                }
                                .testTag("btn_bubble_live_vision_off"),
                            shape = RoundedCornerShape(8.dp),
                            color = HextechDarkBg.copy(alpha = 0.9f),
                            border = BorderStroke(1.dp, HextechCyan)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(HextechCyan)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "VISIÓN",
                                    color = HextechCyan,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Expanded Drafting Hub
            AnimatedVisibility(
                visible = isExpanded,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                var isDraggingPanel by remember { mutableStateOf(false) }

                val targetCardHeight = if (isLandscapeMode) 345.dp else 520.dp
                Card(
                    modifier = Modifier
                        .widthIn(min = if (isLandscapeMode) 520.dp else 300.dp, max = if (isLandscapeMode) 560.dp else 340.dp)
                        .height(targetCardHeight)
                        .clip(RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, HextechGold)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                        // Header con barra de arrastre para reposicionar el Hub cómodamente
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = {
                                            isDraggingPanel = true
                                            dragAccumulatedY = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragAccumulatedY += dragAmount.y
                                            onDragDelta(dragAmount.x.roundToInt(), dragAmount.y.roundToInt(), true, false)
                                        },
                                        onDragEnd = {
                                            isDraggingPanel = false
                                            onDragDelta(0, 0, false, false)
                                            dragAccumulatedY = 0f
                                        },
                                        onDragCancel = {
                                            isDraggingPanel = false
                                            dragAccumulatedY = 0f
                                            onDragDelta(0, 0, false, false)
                                        }
                                    )
                                }
                                .padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = com.example.R.drawable.ic_overlay_logo),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, HextechGold, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("COACH", color = HextechGold, fontWeight = FontWeight.Black, fontSize = 11.5.sp, maxLines = 1)
                                    val isCaptureReady = screenCaptureManager?.isReady() == true
                                    val indicatorColor = when {
                                        !isCaptureReady -> Color(0xFFFFB300)
                                        autoScanEnabled -> Color(0xFF00FF7F)
                                        else -> HextechGold
                                    }
                                    val indicatorText = when {
                                        !isCaptureReady -> tr("Sin permiso")
                                        autoScanEnabled -> tr("Auto-Scan")
                                        else -> tr("Manual")
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            if (!isCaptureReady) {
                                                try {
                                                    val reqIntent = Intent(context, com.example.MainActivity::class.java).apply {
                                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                                        putExtra("EXTRA_REQUEST_CAPTURE", true)
                                                    }
                                                    context.startActivity(reqIntent)
                                                } catch (_: Exception) {}
                                            }
                                        }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(indicatorColor)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = indicatorText,
                                            color = indicatorColor,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val isLiveVisionActive by com.example.service.screen.DraftVisionScanner.showCalibrationBoxes.collectAsStateWithLifecycle()

                                // 1. Botón de Visión en Vivo (Solo icono de ojo, sin texto)
                                Surface(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clickable {
                                            com.example.service.screen.DraftVisionScanner.showCalibrationBoxes.value = !isLiveVisionActive
                                        }
                                        .testTag("btn_live_vision_toggle"),
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isLiveVisionActive) HextechCyan.copy(alpha = 0.35f) else Color(0xFF1E293B),
                                    border = BorderStroke(1.dp, if (isLiveVisionActive) HextechCyan else HextechCyan.copy(alpha = 0.6f))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isLiveVisionActive) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Visión",
                                            tint = if (isLiveVisionActive) HextechCyan else TextSecondary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                // 2. Botón de Depurado LiteRT (Solo icono)
                                Surface(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clickable {
                                            autoScanEnabled = true
                                            showLiteRTViewer = true
                                        }
                                        .testTag("btn_debug_overlay"),
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (showLiteRTViewer) HextechCyan.copy(alpha = 0.35f) else Color(0xFF1E293B),
                                    border = BorderStroke(1.dp, if (showLiteRTViewer) HextechCyan else HextechCyan.copy(alpha = 0.6f))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.BugReport,
                                            contentDescription = "Depurado",
                                            tint = HextechCyan,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                // 3. Botón Minimizar (a Burbuja flotante) - Visible, resaltado y siempre asegurado
                                Surface(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clickable {
                                            isExpanded = false
                                            onExpandedChange(false)
                                        }
                                        .testTag("btn_minimize_hub"),
                                    shape = RoundedCornerShape(6.dp),
                                    color = HextechGold.copy(alpha = 0.2f),
                                    border = BorderStroke(1.2.dp, HextechGold)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Minimizar",
                                            tint = HextechGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Sub-Header con Pestañas de Navegación del Hub
                        AnimatedVisibility(
                            visible = !isOverlayTabsMinimized
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                            // Pestaña 1: Draft Coach
                            val isDraftActive = overlayHubTab == OverlayHubTab.DRAFT
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isDraftActive) HextechCyan.copy(alpha = 0.2f) else HextechSurface)
                                    .border(
                                        1.dp,
                                        if (isDraftActive) HextechCyan else HextechCardBorder.copy(alpha = 0.5f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { overlayHubTab = OverlayHubTab.DRAFT }
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = if (isDraftActive) HextechCyan else TextMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Draft",
                                        color = if (isDraftActive) HextechCyan else TextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = if (isDraftActive) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }

                            // Pestaña 2: Tier & Builds
                            val isTierActive = overlayHubTab == OverlayHubTab.TIER_LIST
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isTierActive) HextechGold.copy(alpha = 0.2f) else HextechSurface)
                                    .border(
                                        1.dp,
                                        if (isTierActive) HextechGold else HextechCardBorder.copy(alpha = 0.5f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { overlayHubTab = OverlayHubTab.TIER_LIST }
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = if (isTierActive) HextechGold else TextMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Tiers",
                                        color = if (isTierActive) HextechGold else TextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = if (isTierActive) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }

                            // Pestaña 3: Campeones
                            val isChampsActive = overlayHubTab == OverlayHubTab.CHAMPIONS
                            Box(
                                modifier = Modifier
                                    .weight(1.1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isChampsActive) HextechCyan.copy(alpha = 0.2f) else HextechSurface)
                                    .border(
                                        1.dp,
                                        if (isChampsActive) HextechCyan else HextechCardBorder.copy(alpha = 0.5f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { overlayHubTab = OverlayHubTab.CHAMPIONS }
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = if (isChampsActive) HextechCyan else TextMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Champs",
                                        color = if (isChampsActive) HextechCyan else TextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = if (isChampsActive) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }

                            // Pestaña 4: Historial
                            if (isLoggedInAndPremium) {
                                val isHistoryActive = overlayHubTab == OverlayHubTab.HISTORY
                                Box(
                                    modifier = Modifier
                                        .weight(0.85f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isHistoryActive) Color(0xFF00FF7F).copy(alpha = 0.15f) else HextechSurface)
                                        .border(
                                            1.dp,
                                            if (isHistoryActive) Color(0xFF00FF7F) else HextechCardBorder.copy(alpha = 0.5f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { overlayHubTab = OverlayHubTab.HISTORY }
                                        .padding(vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.History,
                                            contentDescription = null,
                                            tint = if (isHistoryActive) Color(0xFF00FF7F) else TextMuted,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "Hist",
                                            color = if (isHistoryActive) Color(0xFF00FF7F) else TextMuted,
                                            fontSize = 9.5.sp,
                                            fontWeight = if (isHistoryActive) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                        }

                        if (isOverlayTabsMinimized) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(HextechSurface)
                                    .border(1.dp, HextechGold.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .clickable { isOverlayTabsMinimized = false }
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Pestaña: ${when (overlayHubTab) {
                                        OverlayHubTab.DRAFT -> "Draft Coach"
                                        OverlayHubTab.TIER_LIST -> "Tiers & Builds"
                                        OverlayHubTab.CHAMPIONS -> "Campeones"
                                        OverlayHubTab.HISTORY -> "Historial & Perfiles"
                                    }}",
                                    color = HextechGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Mostrar barra", color = HextechCyan, fontSize = 9.sp)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(12.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        // Banner de estado de escaneo si existe
                        if (scanNoticeMessage != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(HextechSurface)
                                    .border(1.dp, HextechCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .clickable {
                                        if (scanNoticeMessage?.contains("Permiso", ignoreCase = true) == true) {
                                            try {
                                                val intent = Intent(context, com.example.MainActivity::class.java).apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                                    putExtra("EXTRA_REQUEST_CAPTURE", true)
                                                }
                                                context.startActivity(intent)
                                            } catch (_: Exception) {}
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = scanNoticeMessage ?: "",
                                    color = HextechCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // Contenido Principal del Hub según la Pestaña Activa o Detalle de Campeón
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            if (selectedChampionDetail != null) {
                                com.example.ui.screens.ChampionDetailSheet(
                                    isOverlay = true,
                                    champion = selectedChampionDetail,
                                    onDismiss = { selectedChampionDetail = null }
                                )
                            } else {
                                when (overlayHubTab) {
                                    OverlayHubTab.DRAFT -> {
                                        FloatingDraftCoachView(
                                            isLandscapeMode = isLandscapeMode,
                                            activeRole = activeRole,
                                            onActiveRoleChange = { 
                                                activeRole = it 
                                                state.isRoleManuallySelected = true
                                                com.example.util.UserPreferences.setActiveDraftRole(context, it)
                                            },
                                            isFirstPick = isFirstPick,
                                            onFirstPickToggle = { 
                                                state.isFirstPickManuallySelected = true
                                                isFirstPick = !isFirstPick 
                                            },
                                            isLegendaryQueue = isLegendaryQueue,
                                            onToggleLegendaryQueue = { isLegendaryQueue = !isLegendaryQueue },
                                            isLoadingScreenMode = isLoadingScreenMode,
                                            onLoadingScreenModeToggle = { isLoadingScreenMode = !isLoadingScreenMode },
                                            allies = allies,
                                            enemies = enemies,
                                            enemyConfidences = state.enemyConfidences,
                                            allySummonerNames = state.allySummonerNames,
                                            enemySummonerNames = state.enemySummonerNames,
                                            allySpells = state.allySpells,
                                            enemySpells = state.enemySpells,
                                            analysis = analysis,
                                            selectedChampionDetail = selectedChampionDetail,
                                            onSelectChampion = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); selectedChampionDetail = it },
                                            onOpenChampionPicker = { isAlly, idx -> 
                                                autoScanEnabled = false
                                                showChampionPickerForSlot = Pair(isAlly, idx) 
                                            },
                                            onSaveDraftClick = { 
                                                if (isPremium) {
                                                    if (!state.isRoleManuallySelected) {
                                                        android.widget.Toast.makeText(context, "Selecciona tu línea primero", android.widget.Toast.LENGTH_SHORT).show()
                                                    } else if (allies.count { it != null } < 5 || enemies.count { it != null } < 5) {
                                                        android.widget.Toast.makeText(context, "Debes seleccionar los 10 campeones", android.widget.Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        showSaveDraftDialog = true 
                                                    }
                                                } else {
                                                    android.widget.Toast.makeText(context, "Requiere suscripción Premium", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            isSavedRecently = isSavedRecently,
                                            onClearAll = { 
                                                for (i in 0 until 5) {
                                                    allies[i] = null
                                                    enemies[i] = null
                                                }
                                                manualLockedAllySlots.clear()
                                                manualLockedEnemySlots.clear()
                                                state.enemyConfidences.clear()
                                                state.allySummonerNames.clear()
                                                state.enemySummonerNames.clear()
                                                state.allySpells.clear()
                                                state.enemySpells.clear()
                                                state.isRoleManuallySelected = false
                                                state.isFirstPickManuallySelected = false
                                                DraftVisionScanner.resetSlotMemory()
                                                android.widget.Toast.makeText(context, "Equipos vaciados", android.widget.Toast.LENGTH_SHORT).show()
                                            },
                                            onGoToTierList = { overlayHubTab = OverlayHubTab.TIER_LIST },
                                            onManualEdit = { autoScanEnabled = false },
                                            onOpenLiteRTViewer = {
                                                autoScanEnabled = true
                                                showLiteRTViewer = true
                                            }
                                        )
                                    }
                                    OverlayHubTab.TIER_LIST -> {
                                        com.example.ui.screens.TierListTab(
                                            isOverlay = true,
                                            onSelectChampion = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); selectedChampionDetail = it },
                                            isPremium = isPremium
                                        )
                                    }
                                    OverlayHubTab.CHAMPIONS -> {
                                        com.example.ui.screens.ChampionsCatalogTab(
                                            isOverlay = true,
                                            onSelectChampion = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); selectedChampionDetail = it }
                                        )
                                    }
                                    OverlayHubTab.HISTORY -> {
                                        com.example.ui.screens.DraftHistoryScreen(
                                            isOverlay = true,
                                            onNavigateBack = {
                                                overlayHubTab = OverlayHubTab.DRAFT
                                            },
                                            onLoadDraft = { loadedAllies, loadedEnemies, role, isFirst ->
                                                for (i in 0 until 5) {
                                                    allies[i] = loadedAllies.getOrNull(i)?.champion
                                                    enemies[i] = loadedEnemies.getOrNull(i)?.champion
                                                }
                                                activeRole = role
                                                isFirstPick = isFirst
                                                overlayHubTab = OverlayHubTab.DRAFT
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Footer con acciones y estado
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✕ " + tr("Detener Asistente"),
                                color = DangerRed,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { onClose() }
                                    .padding(4.dp)
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = tr("Auto-Scan"),
                                    color = TextMuted,
                                    fontSize = 9.5.sp,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Switch(
                                    checked = autoScanEnabled,
                                    enabled = true,
                                    onCheckedChange = { isChecked -> 
                                        if (isChecked) {
                                            autoScanEnabled = true
                                            DraftVisionScanner.resetSlotMemory()
                                            if (screenCaptureManager?.isReady() != true) {
                                                scanNoticeMessage = "Requiere permiso de pantalla. Toca aquí para activarlo."
                                                try {
                                                    val reqIntent = Intent(context, com.example.MainActivity::class.java).apply {
                                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                                        putExtra("EXTRA_REQUEST_CAPTURE", true)
                                                    }
                                                    context.startActivity(reqIntent)
                                                } catch (_: Exception) {}
                                            }
                                        } else {
                                            autoScanEnabled = false
                                        }
                                    },
                                    modifier = Modifier.scale(0.7f),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = HextechDarkBg,
                                        checkedTrackColor = Color(0xFF00FF7F),
                                        disabledCheckedTrackColor = TextMuted.copy(alpha = 0.3f),
                                        disabledUncheckedTrackColor = HextechSurface
                                    )
                                )
                            }
                            }
                        }
                    }

                    } // close Box
                } // close Card
            } // close AnimatedVisibility
        } // close Column

    // Modal de selección de rol
    if (showRoleChangeDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable { showRoleChangeDialog = false },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clickable { /* no-op */ },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HextechSurfaceVariant),
                border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tr("Selecciona tu Línea"),
                        color = HextechGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LaneRole.entries.forEach { role ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (role == activeRole) HextechCyan.copy(alpha = 0.2f) else HextechSurface)
                                .border(1.dp, if (role == activeRole) HextechCyan else HextechCardBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    activeRole = role
                                    com.example.util.UserPreferences.setActiveDraftRole(context, role)
                                    showRoleChangeDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = role.iconResId),
                                contentDescription = null,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = tr(role.displayName),
                                color = if (role == activeRole) HextechCyan else TextPrimary,
                                fontWeight = if (role == activeRole) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // Modal para Guardar Partida en Base de Datos Room
    if (showSaveDraftDialog) {
        FloatingSaveMatchDialog(
            activeRole = activeRole,
            isFirstPick = isFirstPick,
            isLegendary = isLegendaryQueue,
            allies = allies.mapIndexedNotNull { index, champ -> 
                champ?.let { 
                    val role = when (index) { 
                        0 -> LaneRole.TOP 
                        1 -> LaneRole.JUNGLE 
                        2 -> LaneRole.MID 
                        3 -> LaneRole.ADC 
                        else -> LaneRole.SUPPORT 
                    } 
                    DraftSlot(it, role) 
                } 
            },
            enemies = enemies.mapIndexedNotNull { index, champ -> 
                champ?.let { 
                    val role = when (index) { 
                        0 -> LaneRole.TOP 
                        1 -> LaneRole.JUNGLE 
                        2 -> LaneRole.MID 
                        3 -> LaneRole.ADC 
                        else -> LaneRole.SUPPORT 
                    } 
                    DraftSlot(it, role) 
                } 
            },
            analysis = analysis,
            onDismiss = { showSaveDraftDialog = false },
            onSaved = {
                isSavedRecently = true
                showSaveDraftDialog = false
                overlayHubTab = OverlayHubTab.HISTORY
            }
        )
    }

    // Modal del Visor Google MediaPipe / LiteRT para el 10º Pick
    if (showLiteRTViewer) {
        com.example.ui.components.LiteRTEngineViewerDialog(
            onDismissRequest = { showLiteRTViewer = false }
        )
    }

    // Modal de selección rápida de campeón si el usuario toca un slot manual
    if (showChampionPickerForSlot != null) {
        val (isAllySlot, slotIndex) = showChampionPickerForSlot!!
        val targetRole = defaultRoles.getOrNull(slotIndex)
        var selectedRoleFilter by remember { mutableStateOf<LaneRole?>(null) }
        var searchChampQuery by remember { mutableStateOf("") }
        val currentChampInSlot = if (isAllySlot) allies.getOrNull(slotIndex)?.id else enemies.getOrNull(slotIndex)?.id
        val alreadySelectedIds = remember(allies.toList(), enemies.toList(), slotIndex, isAllySlot) {
            val set = (allies.filterNotNull().map { it.id } + enemies.filterNotNull().map { it.id }).toMutableSet()
            if (currentChampInSlot != null) {
                set.remove(currentChampInSlot)
            }
            set
        }

        val filteredList = remember(searchChampQuery, alreadySelectedIds, selectedRoleFilter, targetRole) {
            WildRiftRepository.champions.filter { champ ->
                val notSelected = !alreadySelectedIds.contains(champ.id)
                val matchesQuery = searchChampQuery.isBlank() || champ.name.contains(searchChampQuery, ignoreCase = true) || champ.summary.contains(searchChampQuery, ignoreCase = true)
                val matchesRole = selectedRoleFilter == null || champ.primaryRole == selectedRoleFilter || champ.secondaryRoles.contains(selectedRoleFilter)
                notSelected && matchesQuery && matchesRole
            }.sortedWith(
                compareByDescending<Champion> { selectedRoleFilter != null && it.primaryRole == selectedRoleFilter }
                    .thenByDescending { selectedRoleFilter == null && targetRole != null && (it.primaryRole == targetRole || it.secondaryRoles.contains(targetRole)) }
                    .thenByDescending { it.tier == "S+" }
                    .thenByDescending { it.tier == "S" }
                    .thenBy { it.name }
            )
        }

        Box(
            modifier = Modifier
                .widthIn(min = 280.dp, max = 330.dp)
                .heightIn(min = 340.dp, max = 460.dp)
                .padding(4.dp)
                .pointerInput(Unit) { },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(390.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isAllySlot) AllyBlue else DangerRed)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = (if (isAllySlot) tr("Elegir Aliado") else tr("Elegir Rival")) + (if (targetRole != null) " - ${com.example.util.tr(targetRole.displayName)}" else ""),
                            color = if (isAllySlot) AllyBlue else DangerRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                        IconButton(onClick = { showChampionPickerForSlot = null }, modifier = Modifier.size(22.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Buscador Compacto y Proporcionado
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(HextechSurface)
                            .border(1.dp, HextechCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchChampQuery.isEmpty()) {
                                    Text(tr("Buscar campeón..."), color = TextMuted, fontSize = 10.5.sp)
                                }
                                androidx.compose.foundation.text.BasicTextField(
                                    value = searchChampQuery,
                                    onValueChange = { searchChampQuery = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 10.5.sp),
                                    singleLine = true,
                                    cursorBrush = androidx.compose.ui.graphics.SolidColor(HextechCyan)
                                )
                            }
                            if (searchChampQuery.isNotEmpty()) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Limpiar",
                                    tint = TextMuted,
                                    modifier = Modifier.size(12.dp).clickable { searchChampQuery = "" }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Selector de Líneas / Filtro Flexible por Rol
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        val filterOptions = listOf<LaneRole?>(null, LaneRole.TOP, LaneRole.JUNGLE, LaneRole.MID, LaneRole.ADC, LaneRole.SUPPORT)
                        filterOptions.forEach { lane ->
                            val isSel = selectedRoleFilter == lane
                            val label = lane?.let { com.example.util.tr(it.shortName) } ?: tr("Todos")
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSel) HextechCyan else HextechSurface)
                                    .border(0.5.dp, if (isSel) HextechGold else HextechCardBorder, RoundedCornerShape(4.dp))
                                    .clickable { selectedRoleFilter = lane }
                                    .padding(vertical = 3.dp, horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (lane != null) {
                                        Image(
                                            painter = painterResource(id = lane.iconResId),
                                            contentDescription = null,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                    }
                                    Text(
                                        text = label,
                                        fontSize = 8.sp,
                                        fontWeight = if (isSel) FontWeight.Black else FontWeight.Medium,
                                        color = if (isSel) HextechDarkBg else TextPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        items(filteredList) { champ ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(HextechSurface)
                                    .clickable {
                                        if (isAllySlot) {
                                            manualLockedAllySlots[slotIndex] = true
                                            for (i in 0 until 5) {
                                                if (allies[i]?.id == champ.id) allies[i] = null
                                                if (enemies[i]?.id == champ.id) enemies[i] = null
                                            }
                                            if (slotIndex in 0 until 5) {
                                                allies[slotIndex] = champ
                                            }
                                        } else {
                                            manualLockedEnemySlots[slotIndex] = true
                                            for (i in 0 until 5) {
                                                if (allies[i]?.id == champ.id) allies[i] = null
                                                if (enemies[i]?.id == champ.id) enemies[i] = null
                                            }
                                            if (slotIndex in 0 until 5) {
                                                enemies[slotIndex] = champ
                                            }
                                        }
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showChampionPickerForSlot = null
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ChampionAvatar(champion = champ, size = 26.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(champ.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                                    Text(
                                        text = com.example.util.tr(champ.primaryRole.displayName),
                                        color = TextMuted,
                                        fontSize = 8.5.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(HextechGold.copy(alpha = 0.15f))
                                        .border(0.5.dp, HextechGold.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(champ.tier, color = HextechGold, fontWeight = FontWeight.Black, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                    
                }
            }
        }
    }
}

@Composable
private fun FloatingSaveMatchDialog(
    activeRole: LaneRole,
    isFirstPick: Boolean,
    isLegendary: Boolean = false,
    allies: List<DraftSlot>,
    enemies: List<DraftSlot>,
    analysis: com.example.model.DraftAnalysisResult,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedResult by remember { mutableStateOf("PENDING") }
    var isLegendaryMatch by remember(isLegendary) { mutableStateOf(isLegendary) }
    var notesText by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var showDuplicateConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        com.example.data.AccountProfileManager.init(context)
    }

    val profiles by com.example.data.AccountProfileManager.allProfiles.collectAsState()
    val activeProfileId by com.example.data.AccountProfileManager.activeProfileId.collectAsState()
    var selectedProfileId by remember(activeProfileId) { mutableStateOf(activeProfileId) }

    val myChampion = allies.find { it.assignedRole == activeRole }?.champion ?: allies.firstOrNull()?.champion
    val enemyOpponent = enemies.find { it.assignedRole == activeRole }?.champion ?: enemies.firstOrNull()?.champion
    val winrateDisplay = (analysis.bestOverallPick?.estimatedWinrate ?: analysis.recommendations.firstOrNull()?.estimatedWinrate ?: 50.0).toInt()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .padding(10.dp)
            .pointerInput(Unit) { },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 480.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
            border = BorderStroke(1.5.dp, HextechGold)
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr("Guardar en Historial"),
                        color = HextechGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                    }
                }

                // Matchup summary badge
                if (myChampion != null || enemyOpponent != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(HextechSurface)
                            .border(1.dp, HextechCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${myChampion?.name ?: "Mi Pick"} (${activeRole.shortName})",
                                color = AllyBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            if (enemyOpponent != null) {
                                Text(" vs ", color = TextMuted, fontSize = 10.sp)
                                Text(
                                    text = enemyOpponent.name,
                                    color = DangerRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Text(
                            text = "WR: $winrateDisplay%",
                            color = HextechGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        )
                    }
                }

                // Perfil de Cuenta
                if (profiles.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tr("Perfil / Cuenta:"),
                        color = TextPrimary,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        profiles.take(3).forEach { profile ->
                            val isSelected = selectedProfileId == profile.id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) HextechCyan.copy(alpha = 0.25f) else HextechSurface)
                                    .border(1.dp, if (isSelected) HextechCyan else HextechCardBorder, RoundedCornerShape(6.dp))
                                    .clickable { selectedProfileId = profile.id }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profile.name,
                                    color = if (isSelected) HextechCyan else TextPrimary,
                                    fontSize = 9.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1, softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = tr("Resultado de la Partida:"),
                    color = TextPrimary,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isPending = selectedResult == "PENDING" || selectedResult == "IN_PROGRESS"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isPending) HextechGold.copy(alpha = 0.25f) else HextechSurface)
                            .border(1.5.dp, if (isPending) HextechGold else HextechCardBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedResult = "PENDING" }
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tr("En espera"),
                            color = if (isPending) HextechGold else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        )
                    }
                    val isVic = selectedResult == "VICTORY"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isVic) Color(0xFF00FF7F).copy(alpha = 0.25f) else HextechSurface)
                            .border(1.5.dp, if (isVic) Color(0xFF00FF7F) else HextechCardBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedResult = "VICTORY" }
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tr("Victoria"),
                            color = if (isVic) Color(0xFF00FF7F) else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        )
                    }
                    val isDef = selectedResult == "DEFEAT"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDef) DangerRed.copy(alpha = 0.25f) else HextechSurface)
                            .border(1.5.dp, if (isDef) DangerRed else HextechCardBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedResult = "DEFEAT" }
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tr("Derrota"),
                            color = if (isDef) DangerRed else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = tr("Notas tácticas / Matchup:"),
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(3.dp))
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = { Text(tr("Ej: Matchup ganado en nivel 3, priorizar cortar curaciones..."), fontSize = 9.5.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HextechCyan,
                        unfocusedBorderColor = HextechCardBorder
                    ),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (!isSaving) {
                            isSaving = true
                            coroutineScope.launch {
                                val chosenProfile = profiles.find { it.id == selectedProfileId }
                                    ?: com.example.data.AccountProfileManager.getActiveProfile(context)
                                
                                val exists = DraftHistoryRepository.checkDraftExists(
                                    context = context,
                                    myRole = activeRole,
                                    allies = allies,
                                    enemies = enemies,
                                    accountProfileId = chosenProfile.id
                                )

                                if (exists) {
                                    showDuplicateConfirmation = true
                                    isSaving = false
                                } else {
                                    DraftHistoryRepository.saveDraft(
                                        context = context,
                                        myRole = activeRole,
                                        isFirstPick = isFirstPick,
                                        isLegendary = isLegendaryMatch,
                                        allies = allies,
                                        enemies = enemies,
                                        analysis = analysis,
                                        notes = notesText,
                                        matchResult = selectedResult,
                                        accountProfileId = chosenProfile.id,
                                        accountProfileName = chosenProfile.name
                                    )
                                    android.widget.Toast.makeText(context, "¡Partida guardada en el historial!", android.widget.Toast.LENGTH_SHORT).show()
                                    isSaving = false
                                    onSaved()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = HextechDarkBg, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "Guardar y Actualizar Historial",
                            color = HextechDarkBg,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
        
        if (showDuplicateConfirmation) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDuplicateConfirmation = false },
                title = { Text(tr("Draft Duplicado"), fontWeight = FontWeight.Bold, color = TextPrimary) },
                text = { Text(tr("Es el mismo draft que el anterior, ¿deseas guardarlo de todas formas?"), color = TextSecondary) },
                containerColor = HextechSurface,
                confirmButton = {
                    Button(
                        onClick = {
                            showDuplicateConfirmation = false
                            isSaving = true
                            coroutineScope.launch {
                                val chosenProfile = profiles.find { it.id == selectedProfileId }
                                    ?: com.example.data.AccountProfileManager.getActiveProfile(context)
                                DraftHistoryRepository.saveDraft(
                                    context = context,
                                    myRole = activeRole,
                                    isFirstPick = isFirstPick,
                                    isLegendary = isLegendaryMatch,
                                    allies = allies,
                                    enemies = enemies,
                                    analysis = analysis,
                                    notes = notesText,
                                    matchResult = selectedResult,
                                    accountProfileId = chosenProfile.id,
                                    accountProfileName = chosenProfile.name
                                )
                                android.widget.Toast.makeText(context, "¡Partida guardada en el historial!", android.widget.Toast.LENGTH_SHORT).show()
                                isSaving = false
                                onSaved()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HextechGold)
                    ) {
                        Text(tr("Sí"), color = Color.Black)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDuplicateConfirmation = false },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
                    ) {
                        Text(tr("No"))
                    }
                }
            )
        }
    }
}

@Composable
private fun FloatingDraftCoachView(
    isLandscapeMode: Boolean,
    activeRole: LaneRole,
    onActiveRoleChange: (LaneRole) -> Unit,
    isFirstPick: Boolean,
    onFirstPickToggle: () -> Unit,
    isLegendaryQueue: Boolean = false,
    onToggleLegendaryQueue: (() -> Unit)? = null,
    isLoadingScreenMode: Boolean,
    onLoadingScreenModeToggle: () -> Unit,
    allies: androidx.compose.runtime.snapshots.SnapshotStateList<Champion?>,
    enemies: androidx.compose.runtime.snapshots.SnapshotStateList<Champion?>,
    enemyConfidences: androidx.compose.runtime.snapshots.SnapshotStateMap<LaneRole, Int>,
    allySummonerNames: androidx.compose.runtime.snapshots.SnapshotStateMap<Int, String> = remember { androidx.compose.runtime.mutableStateMapOf() },
    enemySummonerNames: androidx.compose.runtime.snapshots.SnapshotStateMap<Int, String> = remember { androidx.compose.runtime.mutableStateMapOf() },
    allySpells: androidx.compose.runtime.snapshots.SnapshotStateMap<Int, List<String>> = remember { androidx.compose.runtime.mutableStateMapOf() },
    enemySpells: androidx.compose.runtime.snapshots.SnapshotStateMap<Int, List<String>> = remember { androidx.compose.runtime.mutableStateMapOf() },
    analysis: com.example.model.DraftAnalysisResult,
    selectedChampionDetail: Champion?,
    onSelectChampion: (Champion?) -> Unit,
    onOpenChampionPicker: (isAlly: Boolean, index: Int) -> Unit,
    onSaveDraftClick: () -> Unit,
    isSavedRecently: Boolean,
    onClearAll: () -> Unit,
    onGoToTierList: () -> Unit,
    onManualEdit: () -> Unit,
    onOpenLiteRTViewer: () -> Unit = {}
) {
    val isPremium by com.example.util.SubscriptionManager.isPremium.collectAsStateWithLifecycle()

    val defaultRoles = remember { listOf(LaneRole.TOP, LaneRole.JUNGLE, LaneRole.MID, LaneRole.ADC, LaneRole.SUPPORT) }

    val explicitEnemyOpponent = remember(activeRole, enemies.toList(), isLoadingScreenMode) {
        if (isLoadingScreenMode) {
            val roleIndex = defaultRoles.indexOf(activeRole).coerceIn(0, 4)
            enemies.getOrNull(roleIndex)
        } else {
            enemies.filterNotNull().find { it.primaryRole == activeRole }
        }
    }

    val allySlots = remember(allies.toList(), allySpells.toMap()) {
        allies.mapIndexedNotNull { index, champ ->
            val role = defaultRoles.getOrElse(index) { LaneRole.MID }
            champ?.let {
                DraftSlot(
                    champion = it,
                    assignedRole = role,
                    summonerName = null,
                    spells = allySpells[index] ?: emptyList()
                )
            }
        }
    }
    val enemySlots = remember(enemies.toList(), enemyConfidences.toMap(), enemySpells.toMap(), enemySummonerNames.toMap()) {
        // En Wild Rift el orden de líneas del rival está oculto en el draft. Se deduce por afinidad de rol primario o flex
        val availableRoles = defaultRoles.toMutableList()
        val assignedList = mutableListOf<DraftSlot>()
        enemies.filterNotNull().forEachIndexed { index, champ ->
            val targetRole = if (availableRoles.contains(champ.primaryRole)) {
                champ.primaryRole
            } else {
                champ.secondaryRoles.firstOrNull { availableRoles.contains(it) } ?: availableRoles.firstOrNull() ?: champ.primaryRole
            }
            availableRoles.remove(targetRole)
            val conf = enemyConfidences[targetRole] ?: 85
            assignedList.add(
                DraftSlot(
                    champion = champ,
                    assignedRole = targetRole,
                    confidence = conf,
                    summonerName = enemySummonerNames[index],
                    spells = enemySpells[index] ?: emptyList()
                )
            )
        }
        assignedList
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 4.dp)
    ) {
        // TABLERO DE DRAFT VERSUS (ALIADO VS RIVAL POR LÍNEAS)
        OverlayVersusDraftBoard(
            allySlots = allySlots,
            enemySlots = enemySlots,
            allySummonerNames = allySummonerNames.toMap(),
            activeUserRole = activeRole,
            isFirstPick = isFirstPick,
            onToggleFirstPick = onFirstPickToggle,
            isLegendary = isLegendaryQueue,
            onToggleLegendary = onToggleLegendaryQueue,
            onOpenLiteRTViewer = onOpenLiteRTViewer,
            onPickChampionForRole = { isAlly, role ->
                val index = defaultRoles.indexOf(role).coerceAtLeast(0)
                onOpenChampionPicker(isAlly, index)
            },
            onRemoveChampionForRole = { isAlly, role ->
                val roleIndex = defaultRoles.indexOf(role)
                if (roleIndex in 0 until 5) {
                    if (isAlly) {
                        allies[roleIndex] = null
                    } else {
                        enemies[roleIndex] = null
                        enemyConfidences.remove(role)
                    }
                    onManualEdit()
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // CONTENIDO DEL COACH (CONTROLES Y ANÁLISIS)
        CoachContent(
            allies = allies,
            enemies = enemies,
            activeRole = activeRole,
            onActiveRoleChange = onActiveRoleChange,
            isFirstPick = isFirstPick,
            onFirstPickToggle = onFirstPickToggle,
            analysis = analysis,
            explicitEnemyOpponent = explicitEnemyOpponent,
            onSelectChampion = onSelectChampion,
            onSaveDraftClick = onSaveDraftClick,
            isSavedRecently = isSavedRecently,
            onClearAll = onClearAll,
            onGoToTierList = onGoToTierList,
            isPremium = isPremium
        )
    }
}


@Composable
private fun OverlayVersusDraftBoard(
    allySlots: List<DraftSlot>,
    enemySlots: List<DraftSlot>,
    allySummonerNames: Map<Int, String> = emptyMap(),
    activeUserRole: LaneRole?,
    isFirstPick: Boolean = true,
    onToggleFirstPick: (() -> Unit)? = null,
    isLegendary: Boolean = false,
    onToggleLegendary: (() -> Unit)? = null,
    onOpenLiteRTViewer: (() -> Unit)? = null,
    onPickChampionForRole: (isAlly: Boolean, LaneRole) -> Unit,
    onRemoveChampionForRole: (isAlly: Boolean, LaneRole) -> Unit
) {
    val roles = listOf(
        Pair(LaneRole.TOP, "TOP"),
        Pair(LaneRole.JUNGLE, "JUG"),
        Pair(LaneRole.MID, "MID"),
        Pair(LaneRole.ADC, "DÚO"),
        Pair(LaneRole.SUPPORT, "SUP")
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        border = BorderStroke(1.dp, HextechCardBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
            // Etiquetas de Primera Selección y Clasificatoria Legendaria
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.clickable { onToggleFirstPick?.invoke() },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isFirstPick) AllyBlue.copy(alpha = 0.15f) else DangerRed.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (isFirstPick) AllyBlue.copy(alpha = 0.6f) else DangerRed.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (isFirstPick) AllyBlue else DangerRed)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isFirstPick) tr("1ª Selección: Aliados") else tr("1ª Selección: Rival"),
                            color = if (isFirstPick) AllyBlue else DangerRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    modifier = Modifier.clickable { onToggleLegendary?.invoke() },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isLegendary) Color(0xFFFF9800).copy(alpha = 0.2f) else HextechDarkBg,
                    border = BorderStroke(
                        1.dp,
                        if (isLegendary) Color(0xFFFF9800) else HextechCardBorder.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isLegendary) tr("Legendaria") else tr("Clasificatoria"),
                            color = if (isLegendary) Color(0xFFFFB74D) else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp
                        )
                    }
                }
            }

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp, top = 2.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleFirstPick?.invoke() }
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AllyBlue))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("EQUIPO ALIADO"), color = AllyBlue, fontWeight = FontWeight.Black, fontSize = 10.5.sp)
                    if (isFirstPick) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AllyBlue.copy(alpha = 0.2f),
                            border = BorderStroke(0.5.dp, AllyBlue)
                        ) {
                            Text(
                                text = tr("1ª SELECCIÓN"),
                                color = AllyBlue,
                                fontWeight = FontWeight.Black,
                                fontSize = 7.5.sp,
                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                            )
                        }
                    }
                }

                Surface(
                    color = HextechDarkBg,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(0.5.dp, HextechGold.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable { onToggleFirstPick?.invoke() }
                ) {
                    Text(
                        "VS",
                        color = HextechGold,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleFirstPick?.invoke() }
                ) {
                    if (!isFirstPick) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = DangerRed.copy(alpha = 0.2f),
                            border = BorderStroke(0.5.dp, DangerRed)
                        ) {
                            Text(
                                text = tr("1ª SELECCIÓN"),
                                color = DangerRed,
                                fontWeight = FontWeight.Black,
                                fontSize = 7.5.sp,
                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(tr("EQUIPO RIVAL"), color = DangerRed, fontWeight = FontWeight.Black, fontSize = 10.5.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(DangerRed))
                }
            }

            roles.forEachIndexed { index, (role, label) ->
                val allySlot = allySlots.find { it.assignedRole == role }
                val enemySlot = enemySlots.find { it.assignedRole == role }
                val isMyRole = activeUserRole == role
                val allyChamp = allySlot?.champion
                val enemyChamp = enemySlot?.champion

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    color = if (isMyRole) HextechCyan.copy(alpha = 0.08f) else HextechDarkBg.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        if (isMyRole) 1.dp else 0.5.dp,
                        if (isMyRole) HextechCyan.copy(alpha = 0.6f) else HextechCardBorder.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // LADO ALIADO (Avatar + 1. Nombre -> 2. Stats (WR/Ban/Pick) -> 3. Tier List)
                        Row(
                            modifier = Modifier
                                .weight(1.3f)
                                .clickable { onPickChampionForRole(true, role) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            DraftAvatarBox(
                                slot = allySlot,
                                placeholderInitial = null,
                                isEnemy = false,
                                isMyRole = isMyRole,
                                onClick = { onPickChampionForRole(true, role) },
                                onRemove = { onRemoveChampionForRole(true, role) }
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            if (allyChamp != null) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    // 1. Nombre del Campeón
                                    Text(
                                        text = allyChamp.name,
                                        color = if (isMyRole) HextechCyan else TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    // 2. Estadísticas (WR, Ban, Pick)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Text(
                                            text = "W:${allyChamp.winrate.toInt()}%",
                                            color = Color(0xFF00FF7F),
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "B:${allyChamp.banRate.toInt()}%",
                                            color = DangerRed,
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "P:${allyChamp.pickRate.toInt()}%",
                                            color = Color(0xFFFF9800),
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                    // 3. Tier List
                                    Text(
                                        text = "Tier ${allyChamp.tier}",
                                        color = HextechGold,
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    text = tr("+ Elegir"),
                                    color = AllyBlue.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }

                        // CENTRO: ÍCONO Y ETIQUETA DEL ROL + VS
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .widthIn(min = 40.dp)
                        ) {
                            Image(
                                painter = painterResource(id = role.iconResId),
                                contentDescription = label,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = label,
                                color = if (isMyRole) HextechCyan else TextSecondary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "VS",
                                color = HextechGold.copy(alpha = 0.7f),
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // LADO RIVAL (1. Nombre -> 2. Stats (WR/Ban/Pick) -> 3. Tier List + Avatar)
                        Row(
                            modifier = Modifier
                                .weight(1.3f)
                                .clickable { onPickChampionForRole(false, role) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (enemyChamp != null) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 6.dp),
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    // 1. Nombre del Campeón
                                    Text(
                                        text = enemyChamp.name,
                                        color = DangerRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.End
                                    )
                                    // 2. Estadísticas (WR, Ban, Pick)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text(
                                            text = "W:${enemyChamp.winrate.toInt()}%",
                                            color = Color(0xFF00FF7F),
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "B:${enemyChamp.banRate.toInt()}%",
                                            color = DangerRed,
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "P:${enemyChamp.pickRate.toInt()}%",
                                            color = Color(0xFFFF9800),
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                    // 3. Tier List
                                    Text(
                                        text = "Tier ${enemyChamp.tier}",
                                        color = HextechGold,
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.End
                                    )
                                    if (enemyChamp.secondaryRoles.isNotEmpty()) {
                                        val otherRoles = enemyChamp.secondaryRoles.joinToString("/") { it.shortName }
                                        Text(
                                            text = "FLEX ($otherRoles)",
                                            color = HextechCyan,
                                            fontSize = 6.5.sp,
                                            fontWeight = FontWeight.Black,
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = tr("+ Rival"),
                                    color = DangerRed.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    softWrap = false,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            }

                            DraftAvatarBox(
                                slot = enemySlot,
                                placeholderInitial = null,
                                isEnemy = true,
                                isMyRole = false,
                                onClick = { onPickChampionForRole(false, role) },
                                onRemove = { onRemoveChampionForRole(false, role) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DraftAvatarBox(
    slot: DraftSlot?,
    placeholderInitial: String? = null,
    isEnemy: Boolean,
    isMyRole: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val champ = slot?.champion
    val borderColor = if (isMyRole) HextechCyan else if (champ != null) (if (isEnemy) DangerRed else HextechGold) else HextechCardBorder.copy(alpha = 0.6f)

    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isMyRole -> HextechCyan.copy(alpha = 0.2f)
                    champ != null -> if (isEnemy) DangerRed.copy(alpha = 0.15f) else HextechGold.copy(alpha = 0.15f)
                    else -> Color(0xFF070D15)
                }
            )
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (champ != null) {
            AppAssetImage(
                url = champ.avatarUrl,
                contentDescription = champ.name,
                fallbackText = champ.name,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
            )
            if (isMyRole) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(1.5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(HextechCyan)
                        .padding(horizontal = 2.5.dp, vertical = 0.5.dp)
                ) {
                    Text(
                        text = "TÚ",
                        color = Color.Black,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(15.dp)
                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(bottomStart = 6.dp))
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = "Quitar", tint = Color.White, modifier = Modifier.size(11.dp))
            }
        } else if (!placeholderInitial.isNullOrBlank()) {
            Box(
                modifier = Modifier.fillMaxSize().background(HextechSurface.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = placeholderInitial,
                    color = HextechCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        } else {
            Icon(
                Icons.Default.Add,
                contentDescription = "Añadir",
                tint = if (isEnemy) DangerRed.copy(alpha = 0.5f) else AllyBlue.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}


@Composable
private fun CoachContent(
    allies: List<com.example.model.Champion?>,
    enemies: List<com.example.model.Champion?>,
    activeRole: LaneRole,
    onActiveRoleChange: (LaneRole) -> Unit,
    isFirstPick: Boolean,
    onFirstPickToggle: () -> Unit,
    analysis: com.example.model.DraftAnalysisResult,
    explicitEnemyOpponent: Champion?,
    onSelectChampion: (Champion?) -> Unit,
    onSaveDraftClick: () -> Unit,
    isSavedRecently: Boolean,
    onClearAll: () -> Unit,
    onGoToTierList: () -> Unit,
    isPremium: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 2. SELECTOR DE MI ROL / LÍNEA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            LaneRole.entries.forEach { role ->
                val isSelected = activeRole == role
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) HextechCyan else HextechSurface)
                        .border(1.dp, if (isSelected) HextechGold else HextechCardBorder, RoundedCornerShape(6.dp))
                        .clickable { onActiveRoleChange(role) }
                        .padding(vertical = 3.5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = role.iconResId),
                            contentDescription = null,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = tr(role.shortName),
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                            color = if (isSelected) HextechDarkBg else TextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tr("RECOMENDACIÓN:") + " ${tr(activeRole.displayName)}",
                color = HextechGold,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (isFirstPick) tr("1ª Elección") else tr("Counter Pick"),
                color = if (isFirstPick) HextechGold else HextechCyan,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(HextechSurface)
                    .border(0.5.dp, if (isFirstPick) HextechGold else HextechCyan, RoundedCornerShape(4.dp))
                    .clickable { onFirstPickToggle() }
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }

        // Sinergias (Wombos)
        val allyWombos = remember(allies.toList()) { WomboComboSynergyDetector.detectWombos(allies.filterNotNull()) }

        if (allyWombos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                allyWombos.forEach { wombo ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = HextechDarkBg.copy(alpha = 0.6f)),
                        border = BorderStroke(0.5.dp, AllyBlue)
                    ) {
                        Text(text = "${wombo.title}: ${wombo.description}", color = AllyBlue, fontSize = 8.5.sp, modifier = Modifier.padding(3.dp))
                    }
                }
            }
        }

        // Distribución de Daño del Draft (Aliados vs Enemigos)
        if (allies.any { it != null } || enemies.any { it != null }) {
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(HextechSurface)
                    .border(0.5.dp, HextechCardBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Daño Aliado: AD ${analysis.allyPhysicalDamagePercent}% | AP ${analysis.allyMagicDamagePercent}%",
                        color = AllyBlue,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Daño Enemigo: AD ${analysis.physicalDamagePercent}% | AP ${analysis.magicDamagePercent}%",
                        color = DangerRed,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                val winrateDisplay = (analysis.bestOverallPick?.estimatedWinrate ?: analysis.recommendations.firstOrNull()?.estimatedWinrate ?: 50.0).toInt()
                Text(
                    text = "WR Estimado: ${winrateDisplay}%",
                    color = HextechGold,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Alerta táctica del Coach / Win condition
        if (!analysis.directMatchupWarning.isNullOrBlank() || !analysis.allyCompositionWarning.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            val warningText = analysis.directMatchupWarning ?: analysis.allyCompositionWarning ?: ""
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = HextechGold.copy(alpha = 0.12f)),
                border = BorderStroke(0.8.dp, HextechGold.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = warningText,
                        color = HextechGold,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 10.sp
                    )
                }
            }
        }

        // Análisis 1v1 de línea / Matchup Directo con Rival
        if (explicitEnemyOpponent != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = HextechSurface),
                border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ChampionAvatar(champion = explicitEnemyOpponent, size = 22.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tr("Matchup 1v1 vs") + " ${explicitEnemyOpponent.name}",
                            color = DangerRed,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = analysis.directMatchupWarning ?: "Analizando ventana de poder en línea contra ${explicitEnemyOpponent.name}.",
                        color = TextPrimary,
                        fontSize = 8.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Botones de acción rápida: Guardar Partida, Vaciar Todo y Ver Tier List
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = onSaveDraftClick,
                modifier = Modifier.weight(1.1f).height(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSavedRecently) Color(0xFF00FF7F).copy(alpha = 0.2f) else HextechGold.copy(alpha = 0.15f)
                ),
                border = BorderStroke(1.dp, if (isSavedRecently) Color(0xFF00FF7F) else HextechGold),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isSavedRecently) tr("Guardado") else tr("Guardar"),
                        color = if (isSavedRecently) Color(0xFF00FF7F) else HextechGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (!isPremium) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(HextechGold)
                                .padding(horizontal = 2.5.dp, vertical = 0.5.dp)
                        ) {
                            Text("PRO", color = HextechDarkBg, fontSize = 6.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            Button(
                onClick = onClearAll,
                modifier = Modifier.weight(0.9f).height(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.7f)),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Text(
                    text = tr("Vaciar"),
                    color = DangerRed,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onGoToTierList,
                modifier = Modifier.weight(1f).height(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HextechCyan),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Text(
                    text = tr("Tier List"),
                    color = HextechDarkBg,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 3. MEJORES PICKS RECOMENDADOS POR EL COACH
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            analysis.recommendations.take(4).forEach { pick ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectChampion(pick.champion) },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                    border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChampionAvatar(champion = pick.champion, size = 34.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(pick.champion.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(TierSPlusColor)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(pick.champion.tier, color = Color.Black, fontSize = 7.5.sp, fontWeight = FontWeight.Black)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WR: ${pick.estimatedWinrate}%", color = HextechGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(pick.advantageBadge, color = HextechCyan, fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold)
                            Text(pick.tacticalReason, color = TextMuted, fontSize = 8.sp, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    
                }
            }
        }
    }
}

