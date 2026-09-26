package com.example.data.download

import android.content.Context
import android.util.Log
import com.example.data.AvatarCatalog
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

enum class AssetDownloadStatus {
    IDLE,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    ERROR
}

data class GameAssetItem(
    val id: String,
    val name: String,
    val category: String, // "Habilidad", "Campeón", "Objeto", "Runa", "Hechizo"
    val assetPath: String, // path in assets (e.g. offline_images/... or champions/...)
    val targetFileName: String,
    val estimatedBytes: Long = 128 * 1024L // ~128 KB promedio por imagen
)

data class DownloadManagerProgress(
    val status: AssetDownloadStatus = AssetDownloadStatus.IDLE,
    val totalFiles: Int = 0,
    val downloadedFiles: Int = 0,
    val totalBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val remainingBytes: Long = 0L,
    val currentAssetName: String = "",
    val progressPercent: Float = 0f,
    val errorMessage: String? = null
)

object GameAssetDownloadManager {

    private const val TAG = "GameAssetDownloadMgr"
    private const val PREFS_NAME = "game_asset_download_prefs"
    private const val ASSETS_FOLDER_NAME = "game_assets"
    private const val KEY_IS_FULLY_DOWNLOADED = "is_fully_downloaded"

    private val _downloadProgress = MutableStateFlow(DownloadManagerProgress())
    val downloadProgress: StateFlow<DownloadManagerProgress> = _downloadProgress.asStateFlow()

    private var downloadJob: Job? = null
    @Volatile
    private var isPauseRequested = false

    fun getDownloadDirectory(context: Context): File {
        val dir = File(context.filesDir, ASSETS_FOLDER_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Verifica si un recurso específico ya está descargado en el almacenamiento local.
     */
    fun isAssetDownloaded(context: Context, fileName: String): Boolean {
        val cleanName = fileName.substringAfterLast("/")
        val file = File(getDownloadDirectory(context), cleanName)
        return file.exists() && file.length() > 0
    }

    /**
     * Obtiene el archivo local si ya fue descargado.
     */
    fun getDownloadedFile(context: Context, rawUrlOrPath: String): File? {
        val cleanName = rawUrlOrPath.substringAfterLast("/")
        val file = File(getDownloadDirectory(context), cleanName)
        return if (file.exists() && file.length() > 0) file else null
    }

    /**
     * Identifica los nombres de archivo excluidos (avatares de usuario y marcos de rango),
     * que deben permanecer estrictamente guardados de forma local en la app.
     */
    private fun getExcludedUserFiles(): Set<String> {
        val set = mutableSetOf<String>()
        AvatarCatalog.avatars.forEach { avatar ->
            val fileName = avatar.imageUrl.substringAfterLast("/")
            if (fileName.isNotBlank()) set.add(fileName.lowercase())
        }
        val defaultAvatarName = AvatarCatalog.DEFAULT_AVATAR.imageUrl.substringAfterLast("/")
        if (defaultAvatarName.isNotBlank()) set.add(defaultAvatarName.lowercase())

        set.addAll(
            listOf(
                "frame_administrador.png",
                "frame_moderador.png",
                "frame_creador.png",
                "frame_streamer.png",
                "frame_esmeralda.png",
                "frame_diamante.png",
                "frame_maestro.png",
                "frame_gran_maestro.png",
                "frame_aspirante.png",
                "frame_soberano.png"
            )
        )
        return set
    }

    /**
     * Genera la lista de todos los recursos del juego (habilidades, campeones, objetos, runas y hechizos)
     * listos para ser descargados y gestionados.
     */
    suspend fun getDownloadCatalog(context: Context): List<GameAssetItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<GameAssetItem>()
        val userFiles = getExcludedUserFiles()

        try {
            // 1. Escaneo de offline_images/ (Objetos, Habilidades, Runas, Hechizos)
            val offlineFiles = context.assets.list("offline_images") ?: emptyArray()
            for (file in offlineFiles) {
                if (file.isBlank()) continue
                val lower = file.lowercase()
                if (lower.startsWith("frame_") || lower in userFiles || lower.contains("avatar_")) {
                    continue
                }

                val category = when {
                    lower.contains("rune") || lower.contains("strike") || lower.contains("conqueror") || lower.contains("electrocute") -> "Runa"
                    lower.contains("spell") || lower.contains("flash") || lower.contains("ignite") || lower.contains("smite") -> "Hechizo"
                    lower.contains("item") || lower.contains("boots") || lower.contains("guard") || lower.contains("blade") -> "Objeto"
                    else -> "Habilidad"
                }

                val readableName = file.substringBeforeLast(".")
                    .replace("_", " ")
                    .replace("-", " ")
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

                val estSize = try {
                    context.assets.open("offline_images/$file").use { it.available().toLong() }
                } catch (_: Exception) {
                    120 * 1024L
                }

                items.add(
                    GameAssetItem(
                        id = "offline_$file",
                        name = readableName,
                        category = category,
                        assetPath = "offline_images/$file",
                        targetFileName = file,
                        estimatedBytes = estSize.coerceAtLeast(32 * 1024L)
                    )
                )
            }

            // 2. Escaneo de champions/ (Imágenes y avatares de campeones)
            val champFiles = context.assets.list("champions") ?: emptyArray()
            for (file in champFiles) {
                if (file.isBlank()) continue
                val lower = file.lowercase()
                if (lower.startsWith("frame_") || lower in userFiles || lower.contains("avatar_")) {
                    continue
                }

                val champName = file.substringBeforeLast(".")
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

                val estSize = try {
                    context.assets.open("champions/$file").use { it.available().toLong() }
                } catch (_: Exception) {
                    145 * 1024L
                }

                items.add(
                    GameAssetItem(
                        id = "champ_$file",
                        name = "Campeón $champName",
                        category = "Campeón",
                        assetPath = "champions/$file",
                        targetFileName = "champions_$file",
                        estimatedBytes = estSize.coerceAtLeast(40 * 1024L)
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error construyendo catálogo de descarga: ${e.message}", e)
        }

        items
    }

    /**
     * Inicializa y sincroniza el estado de descarga actual con el almacenamiento del dispositivo.
     */
    suspend fun refreshProgress(context: Context) = withContext(Dispatchers.IO) {
        val catalog = getDownloadCatalog(context)
        val downloadDir = getDownloadDirectory(context)

        var downloadedCount = 0
        var downloadedBytesAcc = 0L
        var totalBytesAcc = 0L

        for (item in catalog) {
            totalBytesAcc += item.estimatedBytes
            val localFile = File(downloadDir, item.targetFileName)
            if (localFile.exists() && localFile.length() > 0) {
                downloadedCount++
                downloadedBytesAcc += localFile.length()
            }
        }

        val total = catalog.size
        val isAllCompleted = total > 0 && downloadedCount >= total
        val status = when {
            _downloadProgress.value.status == AssetDownloadStatus.DOWNLOADING -> AssetDownloadStatus.DOWNLOADING
            _downloadProgress.value.status == AssetDownloadStatus.PAUSED -> AssetDownloadStatus.PAUSED
            isAllCompleted -> AssetDownloadStatus.COMPLETED
            else -> AssetDownloadStatus.IDLE
        }

        val remaining = (totalBytesAcc - downloadedBytesAcc).coerceAtLeast(0L)
        val percent = if (totalBytesAcc > 0) (downloadedBytesAcc.toFloat() / totalBytesAcc.toFloat()).coerceIn(0f, 1f) else 0f

        _downloadProgress.value = DownloadManagerProgress(
            status = status,
            totalFiles = total,
            downloadedFiles = downloadedCount,
            totalBytes = totalBytesAcc,
            downloadedBytes = downloadedBytesAcc,
            remainingBytes = remaining,
            progressPercent = percent,
            currentAssetName = if (isAllCompleted) "Todos los recursos descargados" else _downloadProgress.value.currentAssetName
        )

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_FULLY_DOWNLOADED, isAllCompleted).apply()
    }

    /**
     * Inicia o reanuda la descarga de los recursos del juego de forma pausables.
     */
    fun startOrResumeDownload(context: Context) {
        if (_downloadProgress.value.status == AssetDownloadStatus.DOWNLOADING) return

        isPauseRequested = false
        _downloadProgress.value = _downloadProgress.value.copy(
            status = AssetDownloadStatus.DOWNLOADING,
            errorMessage = null
        )

        downloadJob?.cancel()
        downloadJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val catalog = getDownloadCatalog(context)
                val downloadDir = getDownloadDirectory(context)
                val total = catalog.size
                var totalBytesAcc = catalog.sumOf { it.estimatedBytes }

                var downloadedCount = 0
                var downloadedBytesAcc = 0L

                // Contar los ya existentes
                for (item in catalog) {
                    val localFile = File(downloadDir, item.targetFileName)
                    if (localFile.exists() && localFile.length() > 0) {
                        downloadedCount++
                        downloadedBytesAcc += localFile.length()
                    }
                }

                for (item in catalog) {
                    // Si se solicitó pausa, detenemos el bucle
                    if (isPauseRequested) {
                        _downloadProgress.value = _downloadProgress.value.copy(
                            status = AssetDownloadStatus.PAUSED,
                            currentAssetName = "Descarga en pausa"
                        )
                        return@launch
                    }

                    val targetFile = File(downloadDir, item.targetFileName)
                    if (targetFile.exists() && targetFile.length() > 0) {
                        continue // Ya está descargado
                    }

                    _downloadProgress.value = _downloadProgress.value.copy(
                        currentAssetName = "${item.category}: ${item.name}",
                        downloadedFiles = downloadedCount,
                        downloadedBytes = downloadedBytesAcc,
                        remainingBytes = (totalBytesAcc - downloadedBytesAcc).coerceAtLeast(0L),
                        progressPercent = if (totalBytesAcc > 0) (downloadedBytesAcc.toFloat() / totalBytesAcc.toFloat()).coerceIn(0f, 1f) else 0f
                    )

                    // Descarga / Extracción segura del recurso hacia el almacenamiento interno
                    var inputStream: InputStream? = null
                    var outputStream: FileOutputStream? = null
                    try {
                        inputStream = context.assets.open(item.assetPath)
                        val tempFile = File(downloadDir, "${item.targetFileName}.tmp")
                        outputStream = FileOutputStream(tempFile)

                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            if (isPauseRequested) {
                                outputStream.flush()
                                outputStream.close()
                                tempFile.delete()
                                _downloadProgress.value = _downloadProgress.value.copy(
                                    status = AssetDownloadStatus.PAUSED,
                                    currentAssetName = "Descarga en pausa"
                                )
                                return@launch
                            }
                            outputStream.write(buffer, 0, bytesRead)
                            downloadedBytesAcc += bytesRead
                            _downloadProgress.value = _downloadProgress.value.copy(
                                downloadedBytes = downloadedBytesAcc,
                                remainingBytes = (totalBytesAcc - downloadedBytesAcc).coerceAtLeast(0L),
                                progressPercent = if (totalBytesAcc > 0) (downloadedBytesAcc.toFloat() / totalBytesAcc.toFloat()).coerceIn(0f, 1f) else 0f
                            )
                        }
                        outputStream.flush()
                        outputStream.close()
                        outputStream = null

                        // Renombrar temporal a destino final
                        if (tempFile.exists()) {
                            tempFile.renameTo(targetFile)
                        }
                        downloadedCount++
                        // Pequeña pausa para simular flujo de red suave y no congelar UI
                        delay(15)
                    } catch (e: Exception) {
                        Log.w(TAG, "Error descargando recurso ${item.name}: ${e.message}")
                    } finally {
                        try { inputStream?.close() } catch (_: Exception) {}
                        try { outputStream?.close() } catch (_: Exception) {}
                    }
                }

                val finalRemaining = (totalBytesAcc - downloadedBytesAcc).coerceAtLeast(0L)
                _downloadProgress.value = DownloadManagerProgress(
                    status = AssetDownloadStatus.COMPLETED,
                    totalFiles = total,
                    downloadedFiles = downloadedCount,
                    totalBytes = totalBytesAcc,
                    downloadedBytes = downloadedBytesAcc,
                    remainingBytes = 0L,
                    progressPercent = 1f,
                    currentAssetName = "¡Todos los recursos del juego están listos!"
                )

                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().putBoolean(KEY_IS_FULLY_DOWNLOADED, true).apply()

            } catch (e: Exception) {
                Log.e(TAG, "Error fatal en gestor de descarga: ${e.message}", e)
                _downloadProgress.value = _downloadProgress.value.copy(
                    status = AssetDownloadStatus.ERROR,
                    errorMessage = e.localizedMessage ?: e.message
                )
            }
        }
    }

    /**
     * Pausa la descarga activa de recursos.
     */
    fun pauseDownload() {
        isPauseRequested = true
        _downloadProgress.value = _downloadProgress.value.copy(
            status = AssetDownloadStatus.PAUSED,
            currentAssetName = "Descarga pausada por el usuario"
        )
    }

    /**
     * Formatea bytes a MB legible con 1 decimal.
     */
    fun formatBytesToMb(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        return String.format(Locale.US, "%.1f MB", mb)
    }
}
