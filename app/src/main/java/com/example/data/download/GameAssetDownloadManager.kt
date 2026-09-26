package com.example.data.download

import android.content.Context
import android.util.Log
import com.example.data.WildRiftRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

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
    val category: String,
    val remoteUrl: String,
    val targetFileName: String,
    val estimatedBytes: Long = 20 * 1024L
)

data class DownloadManagerProgress(
    val status: AssetDownloadStatus = AssetDownloadStatus.IDLE,
    val totalFiles: Int = 0,
    val downloadedFiles: Int = 0,
    val totalBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val remainingBytes: Long = 0L,
    val progressPercent: Float = 0f,
    val errorMessage: String? = null
)

object GameAssetDownloadManager {

    private const val TAG = "GameAssetDownloadMgr"
    private const val PREFS_NAME = "game_asset_download_prefs"
    private const val ASSETS_FOLDER_NAME = "game_assets"
    private const val KEY_IS_FULLY_DOWNLOADED = "is_fully_downloaded"
    
    // Concurrencia controlada para descargas simultáneas ultrarrápidas
    private const val CONCURRENCY_LIMIT = 8

    private const val DDRAGON_CDN = "https://ddragon.leagueoflegends.com/cdn/14.23.1/img"
    private const val CDRAGON_PERK_CDN = "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/perk-images/styles"

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

    fun isAssetDownloaded(context: Context, fileName: String): Boolean {
        return getDownloadedFile(context, fileName) != null
    }

    fun getDownloadedFile(context: Context, rawUrlOrPath: String): File? {
        if (rawUrlOrPath.isBlank()) return null
        val cleanName = rawUrlOrPath.substringAfterLast("/")
        val nameNoExt = cleanName.substringBeforeLast(".")
        val dir = getDownloadDirectory(context)

        // 1. Coincidencia exacta
        var file = File(dir, cleanName)
        if (file.exists() && file.length() > 0) return file

        // 2. Prefijo de campeones
        file = File(dir, "champions_$cleanName")
        if (file.exists() && file.length() > 0) return file

        // 3. Extensiones alternativas (.png, .webp, .jpg)
        for (ext in listOf("png", "webp", "jpg")) {
            file = File(dir, "$nameNoExt.$ext")
            if (file.exists() && file.length() > 0) return file
            file = File(dir, "champions_$nameNoExt.$ext")
            if (file.exists() && file.length() > 0) return file
        }
        return null
    }

    /**
     * Construye el catálogo de recursos descargables del juego.
     */
    suspend fun getFullGameAssetsCatalog(context: Context): List<GameAssetItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<GameAssetItem>()
        val seenTargetFiles = mutableSetOf<String>()

        try {
            // 1. Campeones (Avatares)
            WildRiftRepository.initChampions(context)
            val championList = WildRiftRepository.champions.toList()
            for (champ in championList) {
                val champFile = "${champ.id}.png"
                val ddragonChamp = if (champ.ddragonId.isNotBlank()) champ.ddragonId else champ.id.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                if (seenTargetFiles.add(champFile)) {
                    items.add(
                        GameAssetItem(
                            id = "champ_${champ.id}",
                            name = champ.name,
                            category = "Campeón",
                            remoteUrl = "$DDRAGON_CDN/champion/$ddragonChamp.png",
                            targetFileName = champFile,
                            estimatedBytes = 28 * 1024L
                        )
                    )
                }
            }

            // 2. Hechizos de Invocador (Spells)
            val spellsMap = mapOf(
                "flash.webp" to "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/summoner-spells/4.png",
                "ignite.webp" to "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/summoner-spells/14.png",
                "smite.webp" to "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/summoner-spells/11.png",
                "barrier.webp" to "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/summoner-spells/21.png",
                "exhaust.webp" to "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/summoner-spells/3.png",
                "ghost.webp" to "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/summoner-spells/6.png",
                "heal.webp" to "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/summoner-spells/7.png",
                "clarity.jpg" to "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/summoner-spells/13.png",
                "mark.jpg" to "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/summoner-spells/32.png",
                "teleport.png" to "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/summoner-spells/12.png",
                "cleanse.webp" to "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/summoner-spells/1.png"
            )

            for ((targetName, url) in spellsMap) {
                if (seenTargetFiles.add(targetName)) {
                    items.add(
                        GameAssetItem(
                            id = "spell_$targetName",
                            name = targetName.substringBeforeLast("."),
                            category = "Hechizo",
                            remoteUrl = url,
                            targetFileName = targetName,
                            estimatedBytes = 18 * 1024L
                        )
                    )
                }
            }

            // 3. Runas (Runes)
            val runesMap = mapOf(
                "conqueror.png" to "$CDRAGON_PERK_CDN/precision/conqueror/conqueror.png",
                "electrocute.png" to "$CDRAGON_PERK_CDN/domination/electrocute/electrocute.png",
                "dark_harvest.png" to "$CDRAGON_PERK_CDN/domination/darkharvest/darkharvest.png",
                "first_strike.png" to "$CDRAGON_PERK_CDN/inspiration/firststrike/firststrike.png",
                "phase_rush.png" to "$CDRAGON_PERK_CDN/sorcery/phaserush/phaserush.png",
                "lethal_tempo.png" to "$CDRAGON_PERK_CDN/precision/lethaltempo/lethaltempotemp.png",
                "fleet_footwork.png" to "$CDRAGON_PERK_CDN/precision/fleetfootwork/fleetfootwork.png",
                "grasp_undying.png" to "$CDRAGON_PERK_CDN/resolve/graspoftheundying/graspoftheundying.png",
                "arcane_comet.png" to "$CDRAGON_PERK_CDN/sorcery/arcanecomet/arcanecomet.png",
                "aery.png" to "$CDRAGON_PERK_CDN/sorcery/summongaery/summongaery.png",
                "guardian.webp" to "$CDRAGON_PERK_CDN/resolve/guardian/guardian.png",
                "glacial_augment.webp" to "$CDRAGON_PERK_CDN/inspiration/glacialaugment/glacialaugment.png",
                "fortalecimiento.webp" to "$CDRAGON_PERK_CDN/precision/presstheattack/presstheattack.png",
                "cheap_shot.webp" to "$CDRAGON_PERK_CDN/domination/cheapshot/cheapshot.png",
                "sudden_impact.webp" to "$CDRAGON_PERK_CDN/domination/suddenimpact/suddenimpact.png",
                "eyeball_collection.webp" to "$CDRAGON_PERK_CDN/domination/eyeballcollection/eyeballcollection.png",
                "zombie_ward.webp" to "$CDRAGON_PERK_CDN/domination/zombieward/zombieward.png",
                "triumph.webp" to "$CDRAGON_PERK_CDN/precision/triumph.png",
                "coup_de_grace.webp" to "$CDRAGON_PERK_CDN/precision/coupdegrace/coupdegrace.png",
                "cut_down.png" to "$CDRAGON_PERK_CDN/precision/cutdown/cutdown.png",
                "last_stand.webp" to "$CDRAGON_PERK_CDN/precision/laststand/laststand.png",
                "gathering_storm.webp" to "$CDRAGON_PERK_CDN/sorcery/gatheringstorm/gatheringstorm.png",
                "scorch.webp" to "$CDRAGON_PERK_CDN/sorcery/scorch/scorch.png",
                "transcendence.webp" to "$CDRAGON_PERK_CDN/sorcery/transcendence/transcendence.png",
                "celerity.webp" to "$CDRAGON_PERK_CDN/sorcery/celerity/celeritytemp.png",
                "manaflow_band.webp" to "$CDRAGON_PERK_CDN/sorcery/manaflowband/manaflowband.png",
                "nimbus_cloak.webp" to "$CDRAGON_PERK_CDN/sorcery/nimbuscloak/nimbuscloak.png",
                "absolute_focus.webp" to "$CDRAGON_PERK_CDN/sorcery/absolutefocus/absolutefocus.png",
                "demolish.webp" to "$CDRAGON_PERK_CDN/resolve/demolish/demolish.png",
                "font_of_life.webp" to "$CDRAGON_PERK_CDN/resolve/fontoflife/fontoflife.png",
                "bone_plating.webp" to "$CDRAGON_PERK_CDN/resolve/boneplating/boneplating.png",
                "second_wind.webp" to "$CDRAGON_PERK_CDN/resolve/secondwind/secondwind.png",
                "overgrowth.webp" to "$CDRAGON_PERK_CDN/resolve/overgrowth/overgrowth.png",
                "revitalize.webp" to "$CDRAGON_PERK_CDN/resolve/revitalize/revitalize.png"
            )

            for ((targetName, url) in runesMap) {
                if (seenTargetFiles.add(targetName)) {
                    items.add(
                        GameAssetItem(
                            id = "rune_$targetName",
                            name = targetName.substringBeforeLast("."),
                            category = "Runa",
                            remoteUrl = url,
                            targetFileName = targetName,
                            estimatedBytes = 16 * 1024L
                        )
                    )
                }
            }

            // 4. Objetos (Items)
            val itemsMap = mapOf(
                "ab38f2866c6c041524f8f14b1749fc1c.png" to "$DDRAGON_CDN/item/3075.png",
                "3b9e64690847f3bc956e9db35a455f32.png" to "$DDRAGON_CDN/item/3033.png",
                "473e58dc0df0c96012529455146ce012.png" to "$DDRAGON_CDN/item/3165.png",
                "8d0a2f1589e177a2bf2ad148cb31a65f.png" to "$DDRAGON_CDN/item/6695.png",
                "1420e397855c6263e113cf0a16d4bb71.png" to "$DDRAGON_CDN/item/3143.png",
                "e1f9d816eb318d20e5c768c4fa05290d.png" to "$DDRAGON_CDN/item/3065.png",
                "3b32bd3dbf4245dc0952c48bc603bcc8.png" to "$DDRAGON_CDN/item/3139.png",
                "7f8482a5143b2c02ad323ce93df371f1.png" to "$DDRAGON_CDN/item/3102.png",
                "422b305b36178590bc9ddd6e826c22ba.png" to "$DDRAGON_CDN/item/3814.png",
                "c3893b84c990398e6ed58b03c16cafa0.webp" to "$DDRAGON_CDN/item/3110.png",
                "dddefc0a5f24a544b89699b38a9a35e1.png" to "$DDRAGON_CDN/item/3001.png",
                "89889a5db477564f0dded7057e9a1916.png" to "$DDRAGON_CDN/item/4401.png",
                "f4c23d99ec30ef6893a81208846a8831.png" to "$DDRAGON_CDN/item/3025.png",
                "3a33fd10d1e6f9e3f55dd6b553970311.png" to "$DDRAGON_CDN/item/3036.png",
                "e361b2bafad7a688cb9f134d5c210943.png" to "$DDRAGON_CDN/item/3053.png",
                "989ee173a52f7cfc8ea3fd107415dc38.png" to "$DDRAGON_CDN/item/3026.png",
                "8501d4d39cb74524631ed6ef74b1f410.png" to "$DDRAGON_CDN/item/3157.png",
                "e450b4ac7163f1de8de7cfe932744c45.png" to "$DDRAGON_CDN/item/3135.png",
                "amaranths_twinguard.webp" to "$DDRAGON_CDN/item/6665.png",
                "essence_reaver.webp" to "$DDRAGON_CDN/item/3508.png",
                "stormrazor.webp" to "$DDRAGON_CDN/item/3095.png",
                "whispering_headband.webp" to "$DDRAGON_CDN/item/3140.png",
                "yun_tal_wildarrows.webp" to "$DDRAGON_CDN/item/6676.png",
                "dawnshroud.webp" to "$DDRAGON_CDN/item/6664.png",
                "unending_despair.webp" to "$DDRAGON_CDN/item/6667.png",
                "echoes_of_helia.webp" to "$DDRAGON_CDN/item/6620.png",
                "statikk_shiv.webp" to "$DDRAGON_CDN/item/3087.png",
                "rapid_firecannon.webp" to "$DDRAGON_CDN/item/3094.png",
                "immortal_shieldbow.webp" to "$DDRAGON_CDN/item/6673.png"
            )

            for ((targetName, url) in itemsMap) {
                if (seenTargetFiles.add(targetName)) {
                    items.add(
                        GameAssetItem(
                            id = "item_$targetName",
                            name = targetName.substringBeforeLast("."),
                            category = "Objeto",
                            remoteUrl = url,
                            targetFileName = targetName,
                            estimatedBytes = 20 * 1024L
                        )
                    )
                }
            }

            // 5. Habilidades de Campeones desde habilidades.json
            try {
                val jsonString = context.assets.open("habilidades.json").bufferedReader().use { it.readText() }
                val jsonArray = Json.parseToJsonElement(jsonString).jsonArray

                for (element in jsonArray) {
                    val obj = element.jsonObject
                    val skillName = obj["name"]?.jsonPrimitive?.content ?: ""
                    val iconUrl = obj["iconUrl"]?.jsonPrimitive?.content ?: ""

                    if (iconUrl.isNotBlank() && iconUrl.startsWith("http")) {
                        val fileName = iconUrl.substringAfterLast("/")
                        if (fileName.isNotBlank() && seenTargetFiles.add(fileName)) {
                            items.add(
                                GameAssetItem(
                                    id = "skill_$fileName",
                                    name = skillName,
                                    category = "Habilidad",
                                    remoteUrl = iconUrl,
                                    targetFileName = fileName,
                                    estimatedBytes = 22 * 1024L
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error leyendo habilidades.json: ${e.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error generando catalogo de recursos: ${e.message}", e)
        }

        items
    }

    /**
     * Consulta el progreso actual de forma asíncrona sin iniciar ninguna descarga automática.
     */
    fun refreshProgressAsync(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            refreshProgress(context)
        }
    }

    /**
     * Comprueba exactamente los archivos descargados y faltantes en el almacenamiento local.
     */
    suspend fun refreshProgress(context: Context) = withContext(Dispatchers.IO) {
        val catalog = getFullGameAssetsCatalog(context)
        val downloadDir = getDownloadDirectory(context)

        var downloadedCount = 0
        var downloadedBytesAcc = 0L
        var totalBytesAcc = 0L

        for (item in catalog) {
            val localFile = File(downloadDir, item.targetFileName)
            val isDownloaded = localFile.exists() && localFile.length() > 0
            val size = if (isDownloaded) localFile.length() else item.estimatedBytes
            totalBytesAcc += size

            if (isDownloaded) {
                downloadedCount++
                downloadedBytesAcc += localFile.length()
            }
        }

        val total = catalog.size
        val isAllCompleted = total > 0 && downloadedCount >= total

        val status = when {
            isAllCompleted -> AssetDownloadStatus.COMPLETED
            _downloadProgress.value.status == AssetDownloadStatus.DOWNLOADING -> AssetDownloadStatus.DOWNLOADING
            _downloadProgress.value.status == AssetDownloadStatus.PAUSED -> AssetDownloadStatus.PAUSED
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
            errorMessage = null
        )

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_FULLY_DOWNLOADED, isAllCompleted).apply()
    }

    /**
     * Inicia o reanuda la descarga concurrente utilizando Dispatchers.IO con Semaphore para
     * descarga simultánea de múltiples imágenes y actualización reactiva de la UI tras cada archivo.
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
                val catalog = getFullGameAssetsCatalog(context)
                val downloadDir = getDownloadDirectory(context)
                val total = catalog.size

                val downloadedCount = AtomicInteger(0)
                val downloadedBytesAcc = AtomicLong(0L)
                var totalBytesAcc = 0L

                val missingItems = mutableListOf<GameAssetItem>()

                for (item in catalog) {
                    val localFile = File(downloadDir, item.targetFileName)
                    val isDownloaded = localFile.exists() && localFile.length() > 0
                    val size = if (isDownloaded) localFile.length() else item.estimatedBytes
                    totalBytesAcc += size
                    if (isDownloaded) {
                        downloadedCount.incrementAndGet()
                        downloadedBytesAcc.addAndGet(localFile.length())
                    } else {
                        missingItems.add(item)
                    }
                }

                if (missingItems.isEmpty()) {
                    _downloadProgress.value = DownloadManagerProgress(
                        status = AssetDownloadStatus.COMPLETED,
                        totalFiles = total,
                        downloadedFiles = total,
                        totalBytes = totalBytesAcc,
                        downloadedBytes = totalBytesAcc,
                        remainingBytes = 0L,
                        progressPercent = 1f
                    )
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit().putBoolean(KEY_IS_FULLY_DOWNLOADED, true).apply()
                    return@launch
                }

                // Concurrencia controlada en Dispatchers.IO
                val semaphore = Semaphore(CONCURRENCY_LIMIT)

                coroutineScope {
                    for (item in missingItems) {
                        if (isPauseRequested) break

                        launch(Dispatchers.IO) {
                            semaphore.withPermit {
                                if (isPauseRequested) return@withPermit

                                val targetFile = File(downloadDir, item.targetFileName)
                                if (targetFile.exists() && targetFile.length() > 0) return@withPermit

                                var inputStream: InputStream? = null
                                var outputStream: FileOutputStream? = null
                                val tempFile = File(downloadDir, "${item.targetFileName}.tmp")

                                try {
                                    val url = URL(item.remoteUrl)
                                    val conn = (url.openConnection() as HttpURLConnection).apply {
                                        connectTimeout = 4000
                                        readTimeout = 4000
                                        setRequestProperty("User-Agent", "CoachWildRift/1.1")
                                    }

                                    if (conn.responseCode in 200..299) {
                                        inputStream = conn.inputStream
                                        outputStream = FileOutputStream(tempFile)

                                        val buffer = ByteArray(16384) // Buffer optimizado de 16KB
                                        var bytesRead: Int
                                        var fileBytesTotal = 0L
                                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                            if (isPauseRequested) {
                                                outputStream.flush()
                                                outputStream.close()
                                                tempFile.delete()
                                                return@withPermit
                                            }
                                            outputStream.write(buffer, 0, bytesRead)
                                            fileBytesTotal += bytesRead
                                        }
                                        outputStream.flush()
                                        outputStream.close()
                                        outputStream = null

                                        if (tempFile.exists()) {
                                            tempFile.renameTo(targetFile)
                                        }

                                        val curCount = downloadedCount.incrementAndGet()
                                        val curBytes = downloadedBytesAcc.addAndGet(fileBytesTotal)
                                        val remBytes = (totalBytesAcc - curBytes).coerceAtLeast(0L)
                                        val percent = if (totalBytesAcc > 0) (curBytes.toFloat() / totalBytesAcc.toFloat()).coerceIn(0f, 1f) else 0f

                                        // Notificación reactiva inmediata a la UI al completarse cada archivo
                                        _downloadProgress.value = DownloadManagerProgress(
                                            status = AssetDownloadStatus.DOWNLOADING,
                                            totalFiles = total,
                                            downloadedFiles = curCount,
                                            totalBytes = totalBytesAcc,
                                            downloadedBytes = curBytes,
                                            remainingBytes = remBytes,
                                            progressPercent = percent
                                        )
                                    }
                                } catch (_: Exception) {
                                } finally {
                                    try { inputStream?.close() } catch (_: Exception) {}
                                    try { outputStream?.close() } catch (_: Exception) {}
                                }
                            }
                        }
                    }
                }

                if (isPauseRequested) {
                    _downloadProgress.value = _downloadProgress.value.copy(
                        status = AssetDownloadStatus.PAUSED
                    )
                } else {
                    // Recalcular estado final
                    var finalDownloadedCount = 0
                    var finalDownloadedBytes = 0L
                    for (item in catalog) {
                        val f = File(downloadDir, item.targetFileName)
                        if (f.exists() && f.length() > 0) {
                            finalDownloadedCount++
                            finalDownloadedBytes += f.length()
                        }
                    }

                    val isFinished = finalDownloadedCount >= total
                    _downloadProgress.value = DownloadManagerProgress(
                        status = if (isFinished) AssetDownloadStatus.COMPLETED else AssetDownloadStatus.IDLE,
                        totalFiles = total,
                        downloadedFiles = finalDownloadedCount,
                        totalBytes = totalBytesAcc,
                        downloadedBytes = finalDownloadedBytes,
                        remainingBytes = (totalBytesAcc - finalDownloadedBytes).coerceAtLeast(0L),
                        progressPercent = if (totalBytesAcc > 0) (finalDownloadedBytes.toFloat() / totalBytesAcc.toFloat()).coerceIn(0f, 1f) else 0f
                    )

                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit().putBoolean(KEY_IS_FULLY_DOWNLOADED, isFinished).apply()
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error en descarga concurrente: ${e.message}", e)
                _downloadProgress.value = _downloadProgress.value.copy(
                    status = AssetDownloadStatus.ERROR,
                    errorMessage = e.localizedMessage ?: e.message
                )
            }
        }
    }

    fun pauseDownload() {
        isPauseRequested = true
        _downloadProgress.value = _downloadProgress.value.copy(
            status = AssetDownloadStatus.PAUSED
        )
    }

    fun formatBytesToMb(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        return String.format(Locale.US, "%.1f MB", mb)
    }
}
