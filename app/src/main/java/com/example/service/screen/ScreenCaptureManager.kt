package com.example.service.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import com.example.util.AppLogger
import java.nio.ByteBuffer

/**
 * Gestor de captura de pantalla en tiempo real utilizando MediaProjection y VirtualDisplay.
 * Diseñado con optimización de memoria (reutilización de buffers, reciclaje de Bitmaps)
 * y cumplimiento estricto de los requisitos de Foreground Service en Android 14+.
 */
class ScreenCaptureManager(private val context: Context) {

    enum class ProjectionFailure {
        NONE,
        USER_CANCELLED,
        SECURITY_EXCEPTION,
        INVALID_CONSENT,
        INITIALIZATION_ERROR
    }

    companion object {
        private const val TAG = "ScreenCaptureManager"
        private const val VIRTUAL_DISPLAY_NAME = "WildRiftDraftCapture"
        
        // Cache global temporal para transferir el intent de MediaProjection entre Activity y Service
        var pendingMediaProjectionData: Intent? = null
        var pendingMediaProjectionResultCode: Int = 0

        fun clearPendingMediaProjection() {
            pendingMediaProjectionData = null
            pendingMediaProjectionResultCode = 0
        }
    }

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    private var screenWidth: Int = 1080
    private var screenHeight: Int = 2400
    private var screenDensity: Int = 420

    // Hilo secundario dedicado para procesamiento de fotogramas sin bloquear el hilo principal (UI)
    private val captureThread = HandlerThread("ScreenCaptureThread").apply { start() }
    private val handler = Handler(captureThread.looper)
    private var surfaceAttached = false
    private val idleCapture = Runnable {
        synchronized(projectionLock) {
            try {
                virtualDisplay?.surface = null
                surfaceAttached = false
            } catch (_: Exception) { }
        }
    }

    /** Suspend the producer when nobody requests frames, retaining the same consent session. */
    private fun requestCapture(): Boolean = synchronized(projectionLock) {
        val display = virtualDisplay ?: return@synchronized false
        val power = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        if (!power.isInteractive) {
            handler.removeCallbacks(idleCapture)
            handler.post(idleCapture)
            return@synchronized false
        }
        try {
            if (!surfaceAttached) {
                // Drop pre-pause pixels; they must not confirm a new selection.
                synchronized(frameLock) { imageReader?.acquireLatestImage()?.close() }
                display.surface = imageReader?.surface
                surfaceAttached = true
                handler.removeCallbacks(idleCapture)
                handler.postDelayed(idleCapture, 1500L)
                return@synchronized false
            }
            handler.removeCallbacks(idleCapture)
            handler.postDelayed(idleCapture, 1500L)
            true
        } catch (_: Exception) { false }
    }

    init {
        updateScreenDimensions()
    }

    private fun updateScreenDimensions() {
        try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                val bounds = windowManager.maximumWindowMetrics.bounds
                screenWidth = bounds.width().coerceAtLeast(1)
                screenHeight = bounds.height().coerceAtLeast(1)
                screenDensity = context.resources.configuration.densityDpi
            } else {
                val metrics = DisplayMetrics()
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.getRealMetrics(metrics)
                screenWidth = metrics.widthPixels
                screenHeight = metrics.heightPixels
                screenDensity = metrics.densityDpi
            }
        } catch (_: Throwable) {
            val metrics = context.resources.displayMetrics
            screenWidth = metrics.widthPixels
            screenHeight = metrics.heightPixels
            screenDensity = metrics.densityDpi
        }
    }

    private val frameLock = Any()
    private val projectionLock = Any()
    private var lastFrame: Bitmap? = null

    @Volatile
    var lastProjectionFailure: ProjectionFailure = ProjectionFailure.NONE
        private set

    private fun processImageToBitmap(img: Image): Bitmap? {
        return try {
            val plane = img.planes.firstOrNull() ?: return null
            CaptureFrameDecoder.decode(plane.buffer, img.width, img.height, plane.pixelStride, plane.rowStride)
        } catch (oom: OutOfMemoryError) {
            AppLogger.e(TAG, "Memoria insuficiente (OOM) en frame: forzando recoleccion segura")
            System.gc()
            null
        } catch (t: Throwable) {
            AppLogger.w(TAG, "Error seguro procesando imagen a Bitmap: ${t.message}")
            null
        }
    }

    /**
     * Inicializa MediaProjection con los datos de consentimiento de captura otorgados por el usuario.
     */
    @SuppressLint("WrongConstant")
    fun initializeProjection(resultCode: Int, data: Intent): Boolean {
        synchronized(projectionLock) {
            lastProjectionFailure = ProjectionFailure.NONE
            try {
                clearProjection(stop = true)
                val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                val projection = projectionManager.getMediaProjection(resultCode, data)
                mediaProjection = projection

                if (mediaProjection == null) {
                    AppLogger.e(TAG, "MediaProjection no pudo ser creado a partir del Intent.")
                    lastProjectionFailure = ProjectionFailure.INVALID_CONSENT
                    return false
                }

                try {
                    mediaProjection?.registerCallback(object : MediaProjection.Callback() {
                        override fun onStop() {
                            super.onStop()
                            AppLogger.w(TAG, "MediaProjection detenido por el sistema.")
                            synchronized(projectionLock) {
                                // A stopped, older consent session must not clear its replacement.
                                if (mediaProjection === projection) clearProjection(stop = false)
                            }
                        }
                    }, handler)
                } catch (e: Throwable) {
                    clearProjection(stop = true)
                    throw e
                }

                updateScreenDimensions()

                // Wild Rift corre exclusivamente en formato horizontal (apaisado: maxDim x minDim).
                // Al inicializar el VirtualDisplay e ImageReader en la resolución apaisada nativa
                // con VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR garantizamos:
                // 1. Captura 1:1 sin distorsión ni artefactos cuando el juego está en pantalla.
                // 2. El compositor de Android adapta de forma nativa y segura cualquier orientación (vertical/horizontal)
                //    sin necesidad de destruir el VirtualDisplay ni el ImageReader.
                // 3. Se previene de raíz la SecurityException de Android 14+ generada al reutilizar el token de MediaProjection.
                val rawW = maxOf(screenWidth, screenHeight)
                val rawH = minOf(screenWidth, screenHeight)
                // En pantallas QHD+ (como Samsung Galaxy S24/S25/S26 Ultra 3120x1440), limitar la resolución de captura
                // a un máximo de 1920x1080 para evitar consumo excesivo de memoria en la cola nativa de ImageReader
                // y prevenir cierres forzados por falta de memoria (OOM) o saturación de Game Booster.
                val (captureWidth, captureHeight) = CaptureFrameDecoder.captureSize(rawW, rawH)

                // ImageReader configurado con 2 buffers bajo demanda para reducir huella de memoria en segundo plano
                imageReader = ImageReader.newInstance(
                    captureWidth,
                    captureHeight,
                    PixelFormat.RGBA_8888,
                    2
                )

                virtualDisplay = mediaProjection?.createVirtualDisplay(
                    VIRTUAL_DISPLAY_NAME,
                    captureWidth,
                    captureHeight,
                    screenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader?.surface,
                    null,
                    handler
                )

                check(virtualDisplay != null) { "Android no pudo crear el VirtualDisplay." }
                surfaceAttached = true
                handler.postDelayed(idleCapture, 1500L)
                AppLogger.d(TAG, "MediaProjection y VirtualDisplay inicializados exitosamente ($captureWidth x $captureHeight).")
                return true
            } catch (e: SecurityException) {
                // Android 14+ invalida tokens reutilizados o sesiones iniciadas sin el
                // tipo correcto de foreground service. Se trata como un estado recuperable,
                // no como un fallo silencioso ni como una excepción genérica.
                lastProjectionFailure = ProjectionFailure.SECURITY_EXCEPTION
                clearProjection(stop = true)
                AppLogger.e(TAG, "Android rechazó la sesión de MediaProjection por seguridad.", e)
                return false
            } catch (e: Throwable) {
                lastProjectionFailure = ProjectionFailure.INITIALIZATION_ERROR
                clearProjection(stop = true)
                AppLogger.e(TAG, "Fallo al inicializar captura de pantalla", e)
                return false
            }
        }
    }

    fun markAuthorizationCancelled() {
        synchronized(projectionLock) {
            lastProjectionFailure = ProjectionFailure.USER_CANCELLED
        }
    }

    private var lastRefreshTimestamp = 0L

    /**
     * Notifica y actualiza dimensiones de pantalla tras rotación sin destruir la proyección virtual.
     */
    fun refreshProjection() {
        val now = System.currentTimeMillis()
        if (now - lastRefreshTimestamp < 350L) return
        lastRefreshTimestamp = now

        try {
            updateScreenDimensions()
            AppLogger.d(TAG, "Rotación de pantalla registrada de forma segura (${screenWidth}x${screenHeight}).")
        } catch (e: Throwable) {
            AppLogger.w(TAG, "Error actualizando dimensiones en rotación: ${e.message}")
        }
    }

    /**
     * Captura el frame actual de la pantalla bajo demanda como un Bitmap con sincronización protegida.
     * Garantiza acceso thread-safe exclusivo a ImageReader, reciclaje directo y omisión de copias pesadas.
     * Si no hay fotograma nuevo disponible, retorna null permitiendo la estrategia de frame skipping.
     */
    fun captureCurrentFrame(): Bitmap? {
        if (!requestCapture()) return null
        synchronized(frameLock) {
            val reader = imageReader ?: return null
            var image: Image? = null
            try {
                // Adquiere el fotograma más reciente disponible y descarta fotogramas viejos en cola
                image = reader.acquireLatestImage() ?: reader.acquireNextImage()
                if (image != null) {
                    val cleanBmp = processImageToBitmap(image)
                    if (cleanBmp != null) {
                        return cleanBmp
                    }
                }
            } catch (e: Throwable) {
                AppLogger.w(TAG, "Extracción de frame segura: ${e.message}")
            } finally {
                try {
                    image?.close()
                } catch (_: Throwable) {}
            }
            return null
        }
    }

    fun isReady(): Boolean = synchronized(projectionLock) {
        mediaProjection != null && imageReader != null && virtualDisplay != null
    }

    /** Call under projectionLock; detach references before stop can dispatch its callback. */
    private fun clearProjection(stop: Boolean) {
        handler.removeCallbacks(idleCapture)
        surfaceAttached = false
        val previous = mediaProjection
        mediaProjection = null
        synchronized(frameLock) {
            lastFrame?.recycle()
            lastFrame = null
            try { virtualDisplay?.release() } catch (_: Exception) {}
            virtualDisplay = null
            try { imageReader?.close() } catch (_: Exception) {}
            imageReader = null
        }
        if (stop) try { previous?.stop() } catch (_: Exception) {}
    }

    fun release() {
        synchronized(projectionLock) { clearProjection(stop = true) }
        captureThread.quitSafely()
        AppLogger.d(TAG, "Recursos de MediaProjection y HandlerThread liberados.")
    }
}
