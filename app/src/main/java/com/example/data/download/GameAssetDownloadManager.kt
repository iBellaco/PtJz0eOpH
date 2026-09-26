package com.example.data.download

import android.content.Context
import android.util.Log
import com.example.data.AvatarCatalog
import com.example.data.WildRiftRepository
import com.example.data.WildRiftSpellsAndRunes
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
    val category: String, // "Habilidad", "Hechizo", "Runa", "Objeto", "Campeón"
    val assetPath: String, // ruta local si existe
    val remoteUrl: String = "", // URL remota CDN en la nube
    val targetFileName: String,
    val estimatedBytes: Long = 120 * 1024L
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
     * Obtiene el archivo local si ya fue descargado por el gestor.
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
    fun getExcludedUserFiles(): Set<String> {
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
     * Construye el catálogo de recursos descargables del juego:
     * - Habilidades de Campeones
     * - Hechizos de Invocador
     * - Runas y Árboles
     * - Objetos Situacionales y Core
     * - Avatares de Campeones
     */
    suspend fun getDownloadCatalog(context: Context): List<GameAssetItem> = withContext(Dispatchers.IO) {
        val itemsMap = mutableMapOf<String, GameAssetItem>()
        val userFiles = getExcludedUserFiles()

        try {
            // 1. Hechizos de Invocador (Spells)
            val spellFiles = listOf(
                "flash.webp", "ignite.webp", "smite.webp", "barrier.webp",
                "exhaust.webp", "ghost.webp", "heal.webp", "clarity.jpg",
                "mark.jpg", "teleport.png", "cleanse.webp"
            )
            for (spell in spellFiles) {
                val clean = spell.substringBeforeLast(".")
                itemsMap[spell] = GameAssetItem(
                    id = "spell_$spell",
                    name = "Hechizo $clean",
                    category = "Hechizo",
                    assetPath = "spells/$spell",
                    remoteUrl = "https://ddragon.leagueoflegends.com/cdn/14.23.1/img/spell/Summoner$clean.png",
                    targetFileName = spell,
                    estimatedBytes = 95 * 1024L
                )
            }

            // 2. Runas (Runes)
            val runeFiles = listOf(
                "conqueror.png", "electrocute.png", "dark_harvest.png", "first_strike.png",
                "phase_rush.png", "lethal_tempo.png", "fleet_footwork.png", "grasp_undying.png",
                "arcane_comet.png", "guardian.webp", "demolish.webp", "font_of_life.webp",
                "bone_plating.webp", "second_wind.webp", "overgrowth.webp", "revitalize.webp",
                "triumph.webp", "coup_de_grace.webp", "cut_down.png", "last_stand.webp",
                "sudden_impact.webp", "cheap_shot.webp", "eyeball_collection.webp", "zombie_ward.webp",
                "gathering_storm.webp", "scorch.webp", "transcendence.webp", "celerity.webp",
                "manaflow_band.webp", "nimbus_cloak.webp", "absolute_focus.webp", "brutal.webp"
            )
            for (rune in runeFiles) {
                val clean = rune.substringBeforeLast(".").replace("_", " ")
                itemsMap[rune] = GameAssetItem(
                    id = "rune_$rune",
                    name = "Runa $clean",
                    category = "Runa",
                    assetPath = "runes/$rune",
                    targetFileName = rune,
                    estimatedBytes = 110 * 1024L
                )
            }

            // 3. Campeones y Habilidades (Champions & Skills)
            WildRiftRepository.initChampions(context)
            val championList = WildRiftRepository.champions.toList()
            for (champ in championList) {
                val champFile = "${champ.id}.png"
                if (!itemsMap.containsKey(champFile)) {
                    itemsMap[champFile] = GameAssetItem(
                        id = "champ_${champ.id}",
                        name = "Campeón ${champ.name}",
                        category = "Campeón",
                        assetPath = "champions/$champFile",
                        remoteUrl = "https://ddragon.leagueoflegends.com/cdn/14.23.1/img/champion/${champ.ddragonId.ifBlank { champ.id }}.png",
                        targetFileName = "champions_$champFile",
                        estimatedBytes = 140 * 1024L
                    )
                }

                // Habilidades del campeón
                champ.skills.forEach { skill ->
                    if (skill.iconUrl.isNotBlank()) {
                        val fileName = skill.iconUrl.substringAfterLast("/")
                        if (fileName.isNotBlank() && !itemsMap.containsKey(fileName)) {
                            itemsMap[fileName] = GameAssetItem(
                                id = "skill_${champ.id}_${skill.slot}",
                                name = "${champ.name} - ${skill.slotName}: ${skill.name}",
                                category = "Habilidad",
                                assetPath = "offline_images/$fileName",
                                targetFileName = fileName,
                                estimatedBytes = 125 * 1024L
                            )
                        }
                    }
                }
            }

            // 4. Objetos y demás imágenes de offline_images/
            val offlineFiles = context.assets.list("offline_images") ?: emptyArray()
            for (file in offlineFiles) {
                if (file.isBlank()) continue
                val lower = file.lowercase()
                if (lower.startsWith("frame_") || lower in userFiles || lower.contains("avatar_")) {
                    continue
                }
                if (!itemsMap.containsKey(file)) {
                    val category = when {
                        lower.contains("rune") || lower.contains("strike") -> "Runa"
                        lower.contains("spell") || lower.contains("flash") -> "Hechizo"
                        lower.contains("item") || lower.contains("boots") || lower.contains("blade") || lower.contains("guard") -> "Objeto"
                        else -> "Habilidad"
                    }
                    val clean = file.substringBeforeLast(".").replace("_", " ").replace("-", " ")
                    itemsMap[file] = GameAssetItem(
                        id = "offline_$file",
                        name = "$category: $clean",
                        category = category,
                        assetPath = "offline_images/$file",
                        targetFileName = file,
                        estimatedBytes = 120 * 1024L
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error construyendo catálogo de recursos: ${e.message}", e)
        }

        itemsMap.values.toList()
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
     * Inicia o reanuda la descarga de los recursos del juego de forma pausable.
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
                    if (isPauseRequested) {
                        _downloadProgress.value = _downloadProgress.value.copy(
                            status = AssetDownloadStatus.PAUSED,
                            currentAssetName = "Descarga en pausa"
                        )
                        return@launch
                    }

                    val targetFile = File(downloadDir, item.targetFileName)
                    if (targetFile.exists() && targetFile.length() > 0) {
                        continue
                    }

                    _downloadProgress.value = _downloadProgress.value.copy(
                        currentAssetName = "${item.category}: ${item.name}",
                        downloadedFiles = downloadedCount,
                        downloadedBytes = downloadedBytesAcc,
                        remainingBytes = (totalBytesAcc - downloadedBytesAcc).coerceAtLeast(0L),
                        progressPercent = if (totalBytesAcc > 0) (downloadedBytesAcc.toFloat() / totalBytesAcc.toFloat()).coerceIn(0f, 1f) else 0f
                    )

                    var inputStream: InputStream? = null
                    var outputStream: FileOutputStream? = null
                    try {
                        // 1. Intentar abrir desde assets o red
                        inputStream = try {
                            context.assets.open(item.assetPath)
                        } catch (_: Exception) {
                            if (item.remoteUrl.isNotBlank()) {
                                val url = URL(item.remoteUrl)
                                val conn = url.openConnection() as HttpURLConnection
                                conn.connectTimeout = 5000
                                conn.readTimeout = 5000
                                conn.inputStream
                            } else null
                        }

                        if (inputStream != null) {
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

                            if (tempFile.exists()) {
                                tempFile.renameTo(targetFile)
                            }
                            downloadedCount++
                            delay(12)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error descargando recurso ${item.name}: ${e.message}")
                    } finally {
                        try { inputStream?.close() } catch (_: Exception) {}
                        try { outputStream?.close() } catch (_: Exception) {}
                    }
                }

                _downloadProgress.value = DownloadManagerProgress(
                    status = AssetDownloadStatus.COMPLETED,
                    totalFiles = total,
                    downloadedFiles = downloadedCount,
                    totalBytes = totalBytesAcc,
                    downloadedBytes = downloadedBytesAcc,
                    remainingBytes = 0L,
                    progressPercent = 1f,
                    currentAssetName = "¡Habilidades, hechizos, runas y campeones listos!"
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

    fun formatBytesToMb(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        return String.format(Locale.US, "%.1f MB", mb)
    }
}
