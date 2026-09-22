package com.example.service.screen

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.WildRiftApp
import com.example.util.AppLogger
import com.example.util.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

/**
 * Gestor del Modo Diagnóstico para el 10º Pick de Wild Rift.
 * 
 * Guarda automáticamente los recortes exactos de fotogramas pasados a los motores de
 * reconocimiento (Google MediaPipe / LiteRT / Vision Classifier) en el directorio de caché de la app,
 * permitiendo inspeccionar manualmente la calidad de la imagen, resolución, contraste,
 * iluminación y píxeles evaluados.
 */
object TenthPickDiagnosticManager {

    private const val TAG = "TenthPickDiagnostic"
    private const val DIAGNOSTIC_SUBDIR = "diagnostic_10th_pick"
    private const val MAX_SAVED_FRAMES = 100 // Límite circular para proteger el almacenamiento
    private const val MIN_SAVE_INTERVAL_MS = 250L // Evitar escrituras redundantes continuas en auto-scan rápido

    data class DiagnosticCropInfo(
        val id: String,
        val file: File,
        val fileName: String,
        val timestamp: Long,
        val formattedTime: String,
        val width: Int,
        val height: Int,
        val isAlly: Boolean,
        val slotIndex: Int,
        val stage: String,
        val candidateName: String?,
        val confidence: Int?,
        val similarityScore: Float?,
        val fileSizeBytes: Long,
        val avgBrightness: Float,
        val maxBrightness: Int
    ) {
        val formattedSize: String
            get() = when {
                fileSizeBytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", fileSizeBytes / (1024f * 1024f))
                fileSizeBytes >= 1024 -> String.format(Locale.US, "%.1f KB", fileSizeBytes / 1024f)
                else -> "$fileSizeBytes B"
            }

        val dimensionsText: String
            get() = "${width}x${height} px"
    }

    data class DiagnosticCacheStats(
        val totalFiles: Int = 0,
        val totalSizeBytes: Long = 0L,
        val formattedTotalSize: String = "0 KB",
        val directoryPath: String = ""
    )

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isDiagnosticModeEnabled = MutableStateFlow(true)
    val isDiagnosticModeEnabled: StateFlow<Boolean> = _isDiagnosticModeEnabled.asStateFlow()

    private val _savedFramesFlow = MutableStateFlow<List<DiagnosticCropInfo>>(emptyList())
    val savedFramesFlow: StateFlow<List<DiagnosticCropInfo>> = _savedFramesFlow.asStateFlow()

    private val _cacheStatsFlow = MutableStateFlow(DiagnosticCacheStats())
    val cacheStatsFlow: StateFlow<DiagnosticCacheStats> = _cacheStatsFlow.asStateFlow()

    private var lastSaveTimestamp = 0L
    private var lastSavedSignature = ""
    private var isInitialized = false

    /**
     * Inicializa el gestor y lee el estado persistido en preferencias.
     */
    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true
        val enabled = UserPreferences.isTenthPickDiagnosticEnabled(context)
        _isDiagnosticModeEnabled.value = enabled
        refreshSavedFrames(context)
    }

    /**
     * Activa o desactiva el modo diagnóstico.
     */
    fun setDiagnosticMode(context: Context, enabled: Boolean) {
        _isDiagnosticModeEnabled.value = enabled
        UserPreferences.setTenthPickDiagnosticEnabled(context, enabled)
        AppLogger.d(TAG, "Modo Diagnóstico del 10º Pick cambiado a: $enabled")
    }

    /**
     * Obtiene el directorio de caché para diagnósticos del 10º pick.
     */
    fun getDiagnosticCacheDir(context: Context): File {
        val dir = File(context.cacheDir, DIAGNOSTIC_SUBDIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Guarda el recorte utilizado para la detección del 10º campeón en el directorio de caché de la app.
     * Guarda en formato PNG sin pérdida (100% de calidad) para preservar la fidelidad óptica completa.
     */
    fun recordTenthPickCrop(
        cropBitmap: Bitmap?,
        isAlly: Boolean,
        slotIndex: Int,
        stage: String,
        candidateName: String? = null,
        confidence: Int? = null,
        similarityScore: Float? = null,
        context: Context? = null
    ): File? {
        val ctx = context ?: WildRiftApp.instance ?: return null
        if (!isInitialized) {
            init(ctx)
        }

        if (!_isDiagnosticModeEnabled.value) {
            return null
        }

        if (cropBitmap == null || cropBitmap.isRecycled || cropBitmap.width <= 0 || cropBitmap.height <= 0) {
            return null
        }

        val now = System.currentTimeMillis()
        val currentSignature = "${if (isAlly) "ally" else "enemy"}_${slotIndex}_${stage}_${candidateName ?: "none"}_${confidence ?: 0}"

        // Control de frecuencia para evitar escribir archivos idénticos en milisegundos durante auto-scan continuo
        if (now - lastSaveTimestamp < MIN_SAVE_INTERVAL_MS && currentSignature == lastSavedSignature) {
            return null
        }

        // Copia defensiva de seguridad antes de cualquier reciclado en hilos secundarios
        val bitmapCopy = try {
            cropBitmap.copy(Bitmap.Config.ARGB_8888, false)
        } catch (e: Throwable) {
            AppLogger.e(TAG, "No se pudo clonar bitmap para diagnóstico", e)
            return null
        } ?: return null

        lastSaveTimestamp = now
        lastSavedSignature = currentSignature

        val dir = getDiagnosticCacheDir(ctx)
        val timestampStr = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date(now))
        val teamStr = if (isAlly) "ally" else "enemy"
        val candStr = candidateName?.replace(Regex("[^a-zA-Z0-9]"), "_")?.lowercase(Locale.ROOT) ?: "unassigned"
        val confStr = confidence?.let { "${it}pct" } ?: "0pct"
        val stageStr = stage.replace(Regex("[^a-zA-Z0-9]"), "_").lowercase(Locale.ROOT)

        val fileName = "tenth_crop_${timestampStr}_${teamStr}_s${slotIndex}_${stageStr}_${candStr}_${confStr}.png"
        val targetFile = File(dir, fileName)

        // Calcular métricas de brillo óptico en el bitmap clonado
        var sumLum = 0.0
        var maxLum = 0
        val w = bitmapCopy.width
        val h = bitmapCopy.height
        val totalPixels = (w * h).coerceAtLeast(1)
        val step = max(1, (w * h) / 1000) // muestreo rápido
        var sampledCount = 0

        for (y in 0 until h step 2) {
            for (x in 0 until w step 2) {
                val px = bitmapCopy.getPixel(x, y)
                val r = (px shr 16) and 0xFF
                val g = (px shr 8) and 0xFF
                val b = px and 0xFF
                val lum = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                sumLum += lum
                if (lum > maxLum) maxLum = lum
                sampledCount++
            }
        }
        val avgLum = if (sampledCount > 0) (sumLum / sampledCount).toFloat() else 0f

        try {
            FileOutputStream(targetFile).use { out ->
                bitmapCopy.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
            }
        } catch (e: Throwable) {
            AppLogger.e(TAG, "Error guardando recorte diagnóstico en caché", e)
            try {
                bitmapCopy.recycle()
            } catch (_: Throwable) {}
            return null
        }

        val fileSize = targetFile.length()
        val formattedTime = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date(now))

        val newFrame = DiagnosticCropInfo(
            id = fileName,
            file = targetFile,
            fileName = fileName,
            timestamp = now,
            formattedTime = formattedTime,
            width = w,
            height = h,
            isAlly = isAlly,
            slotIndex = slotIndex,
            stage = stage,
            candidateName = candidateName,
            confidence = confidence,
            similarityScore = similarityScore,
            fileSizeBytes = fileSize,
            avgBrightness = avgLum,
            maxBrightness = maxLum
        )

        val currentList = _savedFramesFlow.value.toMutableList()
        currentList.add(0, newFrame)
        if (currentList.size > MAX_SAVED_FRAMES) {
            _savedFramesFlow.value = currentList.take(MAX_SAVED_FRAMES)
        } else {
            _savedFramesFlow.value = currentList
        }
        updateCacheStats(dir, _savedFramesFlow.value)

        scope.launch {
            try {
                enforceMaxFrameLimit(dir)
                AppLogger.d(TAG, "Recorte de 10º pick guardado en caché: ${targetFile.name} (${w}x${h}, ${(fileSize / 1024f).toInt()} KB, brillo avg=${avgLum.toInt()}, max=$maxLum)")
            } catch (e: Throwable) {
                AppLogger.e(TAG, "Error en limpieza de recortes diagnósticos", e)
            } finally {
                try {
                    bitmapCopy.recycle()
                } catch (_: Throwable) {}
            }
        }

        return targetFile
    }

    private fun enforceMaxFrameLimit(dir: File) {
        try {
            val pngFiles = dir.listFiles { _, name -> name.endsWith(".png", ignoreCase = true) } ?: return
            if (pngFiles.size > MAX_SAVED_FRAMES) {
                val sorted = pngFiles.sortedBy { it.lastModified() }
                val toDeleteCount = pngFiles.size - MAX_SAVED_FRAMES
                for (i in 0 until toDeleteCount) {
                    try {
                        sorted[i].delete()
                    } catch (_: Throwable) {}
                }
            }
        } catch (_: Throwable) {}
    }

    /**
     * Refresca la lista de recortes leídos desde el directorio de caché.
     */
    fun refreshSavedFrames(context: Context) {
        scope.launch {
            try {
                val dir = getDiagnosticCacheDir(context)
                val pngFiles = dir.listFiles { _, name -> name.endsWith(".png", ignoreCase = true) } ?: emptyArray()
                val sortedFiles = pngFiles.sortedByDescending { it.lastModified() }

                val frames = sortedFiles.mapNotNull { file ->
                    try {
                        val name = file.name
                        val parts = name.removeSuffix(".png").split("_")
                        val isAlly = name.contains("ally", ignoreCase = true)
                        val slotIdx = Regex("s(\\d+)").find(name)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 4
                        val stage = if (name.contains("waiting", ignoreCase = true)) {
                            "EN ESPERA"
                        } else if (name.contains("confirmed", ignoreCase = true)) {
                            "CONFIRMADO"
                        } else if (name.contains("inference", ignoreCase = true)) {
                            "INFERENCIA"
                        } else {
                            "RECORTE"
                        }

                        val lastMod = file.lastModified()
                        val formattedTime = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(lastMod))

                        // Leer dimensiones rápidamente sin cargar mapa de bits completo
                        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        BitmapFactory.decodeFile(file.absolutePath, opts)
                        val w = opts.outWidth
                        val h = opts.outHeight

                        DiagnosticCropInfo(
                            id = file.name,
                            file = file,
                            fileName = file.name,
                            timestamp = lastMod,
                            formattedTime = formattedTime,
                            width = w,
                            height = h,
                            isAlly = isAlly,
                            slotIndex = slotIdx,
                            stage = stage,
                            candidateName = null,
                            confidence = null,
                            similarityScore = null,
                            fileSizeBytes = file.length(),
                            avgBrightness = 0f,
                            maxBrightness = 0
                        )
                    } catch (t: Throwable) {
                        null
                    }
                }

                withContext(Dispatchers.Main) {
                    _savedFramesFlow.value = frames
                    updateCacheStats(dir, frames)
                }
            } catch (e: Throwable) {
                AppLogger.e(TAG, "Error al refrescar recortes diagnósticos", e)
            }
        }
    }

    private fun updateCacheStats(dir: File, frames: List<DiagnosticCropInfo>) {
        val totalBytes = frames.sumOf { it.fileSizeBytes }
        val formatted = when {
            totalBytes >= 1024 * 1024 -> String.format(Locale.US, "%.2f MB", totalBytes / (1024f * 1024f))
            totalBytes >= 1024 -> String.format(Locale.US, "%.1f KB", totalBytes / 1024f)
            else -> "$totalBytes B"
        }
        _cacheStatsFlow.value = DiagnosticCacheStats(
            totalFiles = frames.size,
            totalSizeBytes = totalBytes,
            formattedTotalSize = formatted,
            directoryPath = dir.absolutePath
        )
    }

    /**
     * Limpia y elimina todos los recortes diagnósticos de la caché.
     */
    fun clearAllCaches(context: Context) {
        val dir = getDiagnosticCacheDir(context)
        try {
            val pngFiles = dir.listFiles { _, name -> name.endsWith(".png", ignoreCase = true) } ?: emptyArray()
            pngFiles.forEach {
                try { it.delete() } catch (_: Throwable) {}
            }
        } catch (e: Throwable) {
            AppLogger.e(TAG, "Error vaciando caché de diagnósticos", e)
        }
        _savedFramesFlow.value = emptyList()
        updateCacheStats(dir, emptyList())
        AppLogger.d(TAG, "Caché de recortes diagnósticos vaciada con éxito")
    }

    /**
     * Elimina un único recorte específico de la caché.
     */
    fun deleteFrame(context: Context, frameId: String) {
        val dir = getDiagnosticCacheDir(context)
        try {
            val target = File(dir, frameId)
            if (target.exists()) {
                target.delete()
            }
        } catch (e: Throwable) {
            AppLogger.e(TAG, "Error eliminando recorte diagnóstico $frameId", e)
        }
        val currentList = _savedFramesFlow.value.filter { it.id != frameId && it.fileName != frameId }
        _savedFramesFlow.value = currentList
        updateCacheStats(dir, currentList)
    }

    /**
     * Abre el selector de compartir del sistema para exportar el recorte PNG hacia otra app o PC.
     */
    fun shareFrame(context: Context, file: File) {
        try {
            if (!file.exists()) return
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Recorte de 10º Pick - Coach")
                putExtra(Intent.EXTRA_TEXT, "Recorte de frame para reconocimiento del 10º pick: ${file.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Compartir recorte de diagnóstico").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Throwable) {
            AppLogger.e(TAG, "Error compartiendo archivo diagnóstico", e)
        }
    }
}
