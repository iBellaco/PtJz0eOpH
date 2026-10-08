package com.example.service

import kotlinx.coroutines.flow.collect
import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
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
import com.example.data.WildRiftRepository
import com.example.service.screen.DraftVisionScanner
import com.example.service.screen.ScreenCaptureManager
import com.example.ui.theme.DangerRed
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AppLogger
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import com.example.util.LocalLanguage
import com.example.util.tr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext

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
                            val overlayLanguage by com.example.util.AppLanguage.current.collectAsStateWithLifecycle()
                            androidx.compose.runtime.CompositionLocalProvider(com.example.util.LocalLanguage provides overlayLanguage) {
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

            com.example.util.AppLanguage.initialize(this)
            serviceScope.launch {
                com.example.util.AppLanguage.current.collect {
                    createNotificationChannel()
                    (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                        .notify(NOTIFICATION_ID, buildForegroundNotification())
                }
            }
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
                com.example.util.appTr("Asistente Flotante Wild Rift"),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = com.example.util.appTr("Mantiene activo el asistente en superposición sobre Wild Rift")
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
            .setContentTitle(com.example.util.appTr("Coach Activo"))
            .setContentText(com.example.util.appTr("Superposición en vivo sobre Wild Rift • Toca para abrir"))
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
                val overlayLanguage by com.example.util.AppLanguage.current.collectAsStateWithLifecycle()
                androidx.compose.runtime.CompositionLocalProvider(com.example.util.LocalLanguage provides overlayLanguage) {
                FloatingCloseTarget(
                    isVisible = isCloseTargetVisible,
                    isTargeted = isCloseTargetHovered
                )
                }
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
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
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
                val selectedLanguage by com.example.util.AppLanguage.current.collectAsState()

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
                                    params.flags = (params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()) or
                                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                } else {
                                    params.flags = params.flags or
                                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
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
                    contentDescription = com.example.util.tr("Cerrar y desactivar overlay"),
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
                    text = com.example.util.tr(if (isTargeted) com.example.util.tr("✕ Soltar para desactivar") else com.example.util.tr("Arrastra aquí para cerrar")),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}
