package com.example.data.download

import android.content.Context
import android.util.Log
import com.example.data.AvatarCatalog
import com.example.data.WildRiftRepository
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
    val remoteUrl: String, // URL remota CDN en la nube
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
    private const val KEY_AUTO_DOWNLOAD_DISABLED = "auto_download_disabled"

    private const val DDRAGON_CDN = "https://ddragon.leagueoflegends.com/cdn/14.23.1/img"

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
     * Verifica si un recurso específico ya está descargado en el almacenamiento local interno.
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
     * Construye el catálogo dinámico de recursos en la nube listos para ser descargados:
     * - Habilidades de Campeones
     * - Hechizos de Invocador
     * - Runas
     * - Objetos
     * - Retratos de Campeones
     */
    suspend fun getDownloadCatalog(context: Context): List<GameAssetItem> = withContext(Dispatchers.IO) {
        val itemsMap = mutableMapOf<String, GameAssetItem>()

        try {
            // 1. Hechizos de Invocador (Spells)
            val spellMap = mapOf(
                "flash.webp" to "SummonerFlash",
                "ignite.webp" to "SummonerDot",
                "smite.webp" to "SummonerSmite",
                "barrier.webp" to "SummonerBarrier",
                "exhaust.webp" to "SummonerExhaust",
                "ghost.webp" to "SummonerHaste",
                "heal.webp" to "SummonerHeal",
                "clarity.jpg" to "SummonerMana",
                "mark.jpg" to "SummonerSnowball",
                "teleport.png" to "SummonerTeleport",
                "cleanse.webp" to "SummonerBoost"
            )
            for ((fileName, ddragonSpell) in spellMap) {
                val spellName = fileName.substringBeforeLast(".")
                itemsMap[fileName] = GameAssetItem(
                    id = "spell_$fileName",
                    name = "Hechizo: ${spellName.replaceFirstChar { it.uppercase() }}",
                    category = "Hechizo",
                    remoteUrl = "$DDRAGON_CDN/spell/$ddragonSpell.png",
                    targetFileName = fileName,
                    estimatedBytes = 90 * 1024L
                )
            }

            // 2. Runas (Runes)
            val runeMap = mapOf(
                "conqueror.png" to "https://raw.communitydragon.org/latest/game/assets/perks/styles/precision/conqueror/conqueror.png",
                "electrocute.png" to "https://raw.communitydragon.org/latest/game/assets/perks/styles/domination/electrocute/electrocute.png",
                "dark_harvest.png" to "https://raw.communitydragon.org/latest/game/assets/perks/styles/domination/darkharvest/darkharvest.png",
                "first_strike.png" to "https://raw.communitydragon.org/latest/game/assets/perks/styles/inspiration/firststrike/firststrike.png",
                "phase_rush.png" to "https://raw.communitydragon.org/latest/game/assets/perks/styles/sorcery/phaserush/phaserush.png",
                "lethal_tempo.png" to "https://raw.communitydragon.org/latest/game/assets/perks/styles/precision/lethaltempo/lethaltempotemp.png",
                "fleet_footwork.png" to "https://raw.communitydragon.org/latest/game/assets/perks/styles/precision/fleetfootwork/fleetfootwork.png",
                "grasp_undying.png" to "https://raw.communitydragon.org/latest/game/assets/perks/styles/resolve/graspoftheundying/graspoftheundying.png",
                "arcane_comet.png" to "https://raw.communitydragon.org/latest/game/assets/perks/styles/sorcery/arcanecomet/arcanecomet.png",
                "guardian.webp" to "https://raw.communitydragon.org/latest/game/assets/perks/styles/resolve/guardian/guardian.png"
            )
            for ((fileName, url) in runeMap) {
                val runeName = fileName.substringBeforeLast(".").replace("_", " ")
                itemsMap[fileName] = GameAssetItem(
                    id = "rune_$fileName",
                    name = "Runa: ${runeName.replaceFirstChar { it.uppercase() }}",
                    category = "Runa",
                    remoteUrl = url,
                    targetFileName = fileName,
                    estimatedBytes = 110 * 1024L
                )
            }

            // 3. Campeones y Habilidades (Champions & Skills)
            WildRiftRepository.initChampions(context)
            val championList = WildRiftRepository.champions.toList()
            for (champ in championList) {
                val champFile = "${champ.id}.png"
                val ddragonChamp = champ.ddragonId.ifBlank { champ.id.replaceFirstChar { it.uppercase() } }
                if (!itemsMap.containsKey(champFile)) {
                    itemsMap[champFile] = GameAssetItem(
                        id = "champ_${champ.id}",
                        name = "Campeón ${champ.name}",
                        category = "Campeón",
                        remoteUrl = "$DDRAGON_CDN/champion/$ddragonChamp.png",
                        targetFileName = "champions_$champFile",
                        estimatedBytes = 135 * 1024L
                    )
                }

                // Habilidades del campeón
                champ.skills.forEach { skill ->
                    if (skill.iconUrl.isNotBlank()) {
                        val fileName = skill.iconUrl.substringAfterLast("/")
                        if (fileName.isNotBlank() && !itemsMap.containsKey(fileName)) {
                            val remoteSkillUrl = if (skill.slot.equals("P", ignoreCase = true)) {
                                "$DDRAGON_CDN/passive/${ddragonChamp}P.png"
                            } else {
                                "$DDRAGON_CDN/spell/${ddragonChamp}${skill.slot}.png"
                            }
                            itemsMap[fileName] = GameAssetItem(
                                id = "skill_${champ.id}_${skill.slot}",
                                name = "${champ.name} - ${skill.slotName}: ${skill.name}",
                                category = "Habilidad",
                                remoteUrl = remoteSkillUrl,
                                targetFileName = fileName,
                                estimatedBytes = 120 * 1024L
                            )
                        }
                    }
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
            currentAssetName = if (isAllCompleted) "Todos los recursos del juego están listos" else _downloadProgress.value.currentAssetName
        )

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_FULLY_DOWNLOADED, isAllCompleted).apply()
    }

    /**
     * Inicio automático al entrar a la aplicación (si aún no se han descargado todos los recursos).
     */
    fun autoStartOnLaunch(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isCompleted = prefs.getBoolean(KEY_IS_FULLY_DOWNLOADED, false)
        if (!isCompleted && _downloadProgress.value.status != AssetDownloadStatus.DOWNLOADING && _downloadProgress.value.status != AssetDownloadStatus.PAUSED) {
            startOrResumeDownload(context)
        }
    }

    /**
     * Inicia o reanuda la descarga de los recursos del juego de forma pausable desde la nube.
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
                        if (item.remoteUrl.isNotBlank()) {
                            val url = URL(item.remoteUrl)
                            val conn = url.openConnection() as HttpURLConnection
                            conn.connectTimeout = 6000
                            conn.readTimeout = 6000
                            conn.setRequestProperty("User-Agent", "CoachWildRift/1.1")
                            if (conn.responseCode in 200..299) {
                                inputStream = conn.inputStream
                            }
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
                            delay(10)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Descarga remota en cola para ${item.name}: ${e.message}")
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
                Log.e(TAG, "Error en gestor de descarga: ${e.message}", e)
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
