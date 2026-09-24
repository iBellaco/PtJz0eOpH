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

    companion object {
        private const val TAG = "ScreenCaptureManager"
        private const val VIRTUAL_DISPLAY_NAME = "WildRiftDraftCapture"
        
        // Cache global temporal para transferir el intent de MediaProjection entre Activity y Service
        var pendingMediaProjectionData: Intent? = null
        var pendingMediaProjectionResultCode: Int = 0
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

    init {
        updateScreenDimensions()
    }

    private fun updateScreenDimensions() {
        try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                val bounds = windowManager.currentWindowMetrics.bounds
                screenWidth = bounds.width().coerceAtLeast(720)
                screenHeight = bounds.height().coerceAtLeast(720)
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

    private fun processImageToBitmap(img: Image): Bitmap? {
        return try {
            val planes = img.planes
            if (planes.isNullOrEmpty()) return null
            val buffer = planes[0].buffer ?: return null
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val width = img.width
            val height = img.height
            if (width <= 0 || height <= 0 || pixelStride <= 0 || rowStride <= 0) return null

            val rowPadding = rowStride - pixelStride * width
            val bitmapWidth = width + rowPadding / pixelStride
            if (bitmapWidth <= 0) return null

            val requiredBytes = (height - 1) * rowStride + width * pixelStride
            if (buffer.remaining() < requiredBytes) {
                return null
            }

            val bmp = Bitmap.createBitmap(
                bitmapWidth,
                height,
                Bitmap.Config.ARGB_8888
            )
            bmp.copyPixelsFromBuffer(buffer)

            if (rowPadding != 0) {
                val cropped = Bitmap.createBitmap(bmp, 0, 0, width, height)
                bmp.recycle()
                cropped
            } else {
                bmp
            }
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
            try {
                val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                mediaProjection = projectionManager.getMediaProjection(resultCode, data)

                if (mediaProjection == null) {
                    AppLogger.e(TAG, "MediaProjection no pudo ser creado a partir del Intent.")
                    return false
                }

                try {
                    mediaProjection?.registerCallback(object : MediaProjection.Callback() {
                        override fun onStop() {
                            super.onStop()
                            AppLogger.w(TAG, "MediaProjection detenido por el sistema.")
                            synchronized(frameLock) {
                                lastFrame?.recycle()
                                lastFrame = null
                            }
                            mediaProjection = null
                        }
                    }, handler)
                } catch (e: Throwable) {
                    AppLogger.w(TAG, "No se pudo registrar callback en MediaProjection: ${e.message}")
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
                val scale = if (rawW > 1920) 1920f / rawW else 1.0f
                val captureWidth = (((rawW * scale).toInt() / 2) * 2).coerceIn(1280, 1920)
                val captureHeight = (((rawH * scale).toInt() / 2) * 2).coerceIn(720, 1080)

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

                AppLogger.d(TAG, "MediaProjection y VirtualDisplay inicializados exitosamente ($captureWidth x $captureHeight).")
                return true
            } catch (e: Throwable) {
                AppLogger.e(TAG, "Fallo al inicializar captura de pantalla", e)
                return false
            }
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

    fun isReady(): Boolean = mediaProjection != null && imageReader != null

    fun release() {
        try {
            synchronized(frameLock) {
                lastFrame?.recycle()
                lastFrame = null
            }
            virtualDisplay?.release()
            virtualDisplay = null
            imageReader?.close()
            imageReader = null
            mediaProjection?.stop()
            mediaProjection = null
            captureThread.quitSafely()
            AppLogger.d(TAG, "Recursos de MediaProjection y HandlerThread liberados.")
        } catch (e: Throwable) {
            AppLogger.e(TAG, "Error liberando MediaProjection", e)
        }
    }
}
