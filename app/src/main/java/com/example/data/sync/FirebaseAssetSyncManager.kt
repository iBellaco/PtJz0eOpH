package com.example.data.sync

import android.content.Context
import android.util.Log
import com.example.data.AvatarCatalog
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.InputStream

data class AssetSyncProgress(
    val isRunning: Boolean = false,
    val totalFiles: Int = 0,
    val processedFiles: Int = 0,
    val currentFileName: String = "",
    val errorCount: Int = 0,
    val isFinished: Boolean = false,
    val statusMessage: String = "Inactivo"
)

object FirebaseAssetSyncManager {

    private const val TAG = "FirebaseAssetSync"
    private const val STORAGE_FOLDER = "game_assets"
    private const val PREFS_NAME = "firebase_asset_sync_prefs"

    private val _syncProgress = MutableStateFlow(AssetSyncProgress())
    val syncProgress: StateFlow<AssetSyncProgress> = _syncProgress.asStateFlow()

    /**
     * Identifica los nombres de archivo que corresponden a avatares de usuario y marcos de perfil,
     * los cuales deben permanecer estrictamente guardados de forma local.
     */
    fun getUserLocalFileNames(): Set<String> {
        val set = mutableSetOf<String>()
        // 1. Avatares del catálogo de usuario
        AvatarCatalog.avatars.forEach { avatar ->
            val fileName = avatar.imageUrl.substringAfterLast("/")
            if (fileName.isNotBlank()) set.add(fileName.lowercase())
        }
        val defaultAvatarName = AvatarCatalog.DEFAULT_AVATAR.imageUrl.substringAfterLast("/")
        if (defaultAvatarName.isNotBlank()) set.add(defaultAvatarName.lowercase())

        // 2. Marcos de perfil de usuario
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
     * Determina si un archivo debe permanecer localmente (avatares y marcos de perfil de usuario).
     */
    fun isLocalUserAsset(fileName: String): Boolean {
        val lower = fileName.lowercase().trim()
        if (lower.startsWith("frame_") || lower.contains("avatar_") || lower.contains("user_avatar")) {
            return true
        }
        return getUserLocalFileNames().contains(lower)
    }

    /**
     * Obtiene la lista de todos los recursos del juego (objetos, runas, hechizos, campeones, habilidades)
     * listos para ser sincronizados con el almacenamiento en la nube.
     */
    suspend fun getGameAssetsToUpload(context: Context): List<Pair<String, String>> = withContext(Dispatchers.IO) {
        val assetsList = mutableListOf<Pair<String, String>>() // (assetPath, cloudFileName)
        val userFiles = getUserLocalFileNames()

        try {
            // 1. Escanear offline_images/
            val offlineFiles = context.assets.list("offline_images") ?: emptyArray()
            for (file in offlineFiles) {
                if (file.isBlank()) continue
                val lower = file.lowercase()
                if (lower.startsWith("frame_") || lower in userFiles || lower.contains("avatar_")) {
                    continue
                }
                assetsList.add("offline_images/$file" to file)
            }

            // 2. Escanear champions/
            val championFiles = context.assets.list("champions") ?: emptyArray()
            for (file in championFiles) {
                if (file.isBlank()) continue
                val lower = file.lowercase()
                if (lower.startsWith("frame_") || lower in userFiles || lower.contains("avatar_")) {
                    continue
                }
                assetsList.add("champions/$file" to "champions_$file")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error listando assets para sincronización: ${e.message}", e)
        }

        assetsList
    }

    /**
     * Inicia el proceso de sincronización y subida de todas las imágenes del juego al almacenamiento en la nube.
     */
    suspend fun startSync(context: Context) = withContext(Dispatchers.IO) {
        if (_syncProgress.value.isRunning) return@withContext

        _syncProgress.value = AssetSyncProgress(
            isRunning = true,
            statusMessage = "Escaneando recursos del juego..."
        )

        try {
            val assetsToUpload = getGameAssetsToUpload(context)
            val total = assetsToUpload.size
            var processed = 0
            var errors = 0

            val storage = FirebaseStorage.getInstance()
            val baseRef = storage.reference.child(STORAGE_FOLDER)

            _syncProgress.value = AssetSyncProgress(
                isRunning = true,
                totalFiles = total,
                processedFiles = 0,
                statusMessage = "Sincronizando recursos con la nube..."
            )

            for ((assetPath, cloudFileName) in assetsToUpload) {
                try {
                    _syncProgress.value = _syncProgress.value.copy(
                        currentFileName = cloudFileName,
                        processedFiles = processed,
                        statusMessage = "Subiendo $cloudFileName (${processed + 1}/$total)..."
                    )

                    var inputStream: InputStream? = null
                    val bytes = try {
                        inputStream = context.assets.open(assetPath)
                        inputStream.readBytes()
                    } finally {
                        try { inputStream?.close() } catch (_: Exception) {}
                    }

                    if (bytes.isNotEmpty()) {
                        val fileRef = baseRef.child(cloudFileName)
                        val contentType = when {
                            cloudFileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
                            cloudFileName.endsWith(".png", ignoreCase = true) -> "image/png"
                            cloudFileName.endsWith(".jpg", ignoreCase = true) || cloudFileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                            else -> "image/webp"
                        }

                        val metadata = StorageMetadata.Builder()
                            .setContentType(contentType)
                            .setCustomMetadata("uploaded_by", "coach_app_sync")
                            .build()

                        fileRef.putBytes(bytes, metadata).await()
                    }
                    processed++
                } catch (e: Exception) {
                    errors++
                    Log.w(TAG, "Error subiendo archivo $cloudFileName a almacenamiento: ${e.message}")
                }
            }

            _syncProgress.value = AssetSyncProgress(
                isRunning = false,
                totalFiles = total,
                processedFiles = processed,
                errorCount = errors,
                isFinished = true,
                statusMessage = "Sincronización completada ($processed recursos procesados)."
            )

            // Guardar marca de tiempo de sincronización
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putLong("last_sync_timestamp", System.currentTimeMillis()).apply()

        } catch (e: Exception) {
            Log.e(TAG, "Error general en proceso de sincronización: ${e.message}", e)
            _syncProgress.value = AssetSyncProgress(
                isRunning = false,
                isFinished = true,
                errorCount = 1,
                statusMessage = "Error en la sincronización: ${e.message}"
            )
        }
    }
}
