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
    val statusMessage: String = "Inactivo",
    val lastError: String? = null,
    val syncedBytes: Long = 0L,
    val activeBucket: String = ""
)

object FirebaseAssetSyncManager {

    private const val TAG = "FirebaseAssetSync"
    private const val STORAGE_FOLDER = "game_assets"
    private const val PREFS_NAME = "firebase_asset_sync_prefs"
    private const val KEY_CUSTOM_BUCKET = "custom_storage_bucket"
    private const val KEY_SYNCED_BYTES = "synced_total_bytes"
    private const val KEY_SYNCED_COUNT = "synced_total_count"

    private val _syncProgress = MutableStateFlow(AssetSyncProgress())
    val syncProgress: StateFlow<AssetSyncProgress> = _syncProgress.asStateFlow()

    fun getCustomBucket(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CUSTOM_BUCKET, "") ?: ""
    }

    fun setCustomBucket(context: Context, bucket: String) {
        val cleanBucket = bucket.trim().removePrefix("gs://").removeSuffix("/")
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CUSTOM_BUCKET, cleanBucket).apply()
    }

    fun getSyncedStats(context: Context): Pair<Int, Long> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val count = prefs.getInt(KEY_SYNCED_COUNT, 0)
        val bytes = prefs.getLong(KEY_SYNCED_BYTES, 0L)
        return Pair(count, bytes)
    }

    private fun getStorageInstanceForBucket(bucket: String): FirebaseStorage {
        val clean = bucket.trim().removePrefix("gs://").removeSuffix("/")
        return if (clean.isNotBlank()) {
            FirebaseStorage.getInstance("gs://$clean")
        } else {
            FirebaseStorage.getInstance()
        }
    }

    fun getStorageInstance(context: Context): FirebaseStorage {
        val custom = getCustomBucket(context)
        return getStorageInstanceForBucket(custom)
    }

    /**
     * Prueba rápida de conexión con un bucket específico subiendo y borrando una pequeña sonda.
     */
    suspend fun probeBucket(bucket: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val storage = getStorageInstanceForBucket(bucket)
            val probeRef = storage.reference.child(STORAGE_FOLDER).child(".probe_connection.txt")
            val probeData = "probe_test_${System.currentTimeMillis()}".toByteArray()
            probeRef.putBytes(probeData).await()
            try { probeRef.delete().await() } catch (_: Exception) {}
            Pair(true, "Conexión exitosa con el bucket: ${if (bucket.isBlank()) "Default" else bucket}")
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: "Error desconocido"
            Log.w(TAG, "Fallo al probar bucket '$bucket': $msg")
            Pair(false, msg)
        }
    }

    /**
     * Prueba de conexión con Auto-Detección de Bucket.
     * Si el bucket predeterminado falla (por ejemplo con 404 / Object does not exist),
     * prueba automáticamente candidatos conocidos del proyecto.
     */
    suspend fun testConnection(context: Context): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val custom = getCustomBucket(context)
        val candidateBuckets = mutableListOf<String>()

        if (custom.isNotBlank()) candidateBuckets.add(custom)
        candidateBuckets.addAll(
            listOf(
                "wild-rift-drafting.firebasestorage.app",
                "wild-rift-drafting.appspot.com",
                "coach-wildrift.firebasestorage.app",
                "coach-wildrift.appspot.com",
                ""
            )
        )

        var lastError = "No se pudo conectar a ningún bucket de almacenamiento."

        for (bucket in candidateBuckets.distinct()) {
            val (success, msg) = probeBucket(bucket)
            if (success) {
                if (bucket.isNotBlank()) {
                    setCustomBucket(context, bucket)
                }
                return@withContext Pair(true, "Conectado exitosamente al almacenamiento: ${if (bucket.isBlank()) "Bucket Predeterminado" else bucket}")
            } else {
                lastError = msg
            }
        }

        val humanError = when {
            lastError.contains("Object does not exist", ignoreCase = true) || lastError.contains("404", ignoreCase = true) ->
                "El bucket de almacenamiento no existe o no ha sido inicializado. Ve a la sección Storage de tu consola en la nube y asegúrate de hacer clic en 'Empezar' (Get Started), o ingresa el nombre exacto de tu bucket arriba."
            lastError.contains("permission", ignoreCase = true) || lastError.contains("denied", ignoreCase = true) || lastError.contains("403", ignoreCase = true) ->
                "Permiso denegado por las reglas de seguridad. Revisa las reglas de Storage en tu consola."
            else -> lastError
        }

        Pair(false, humanError)
    }

    /**
     * Identifica los nombres de archivo que corresponden a avatares de usuario y marcos de perfil,
     * los cuales deben permanecer estrictamente guardados de forma local.
     */
    fun getUserLocalFileNames(): Set<String> {
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

    fun isLocalUserAsset(fileName: String): Boolean {
        val lower = fileName.lowercase().trim()
        if (lower.startsWith("frame_") || lower.contains("avatar_") || lower.contains("user_avatar")) {
            return true
        }
        return getUserLocalFileNames().contains(lower)
    }

    suspend fun getGameAssetsToUpload(context: Context): List<Pair<String, String>> = withContext(Dispatchers.IO) {
        val assetsList = mutableListOf<Pair<String, String>>()
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

    suspend fun startSync(context: Context) = withContext(Dispatchers.IO) {
        if (_syncProgress.value.isRunning) return@withContext

        _syncProgress.value = AssetSyncProgress(
            isRunning = true,
            statusMessage = "Verificando acceso y resolviendo bucket de almacenamiento..."
        )

        try {
            val (connected, connError) = testConnection(context)
            if (!connected) {
                _syncProgress.value = AssetSyncProgress(
                    isRunning = false,
                    isFinished = true,
                    errorCount = 1,
                    statusMessage = "No se pudo acceder al almacenamiento.",
                    lastError = connError
                )
                return@withContext
            }

            val storage = getStorageInstance(context)
            val baseRef = storage.reference.child(STORAGE_FOLDER)
            val activeBucketName = getCustomBucket(context).ifBlank { "Predeterminado" }

            val assetsToUpload = getGameAssetsToUpload(context)
            val total = assetsToUpload.size
            var processed = 0
            var errors = 0
            var lastErrStr: String? = null
            var totalBytesUploaded = 0L

            _syncProgress.value = AssetSyncProgress(
                isRunning = true,
                totalFiles = total,
                processedFiles = 0,
                statusMessage = "Sincronizando $total recursos con $activeBucketName...",
                activeBucket = activeBucketName
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
                        totalBytesUploaded += bytes.size
                    }
                    processed++
                } catch (e: Exception) {
                    errors++
                    lastErrStr = e.localizedMessage ?: e.message
                    Log.w(TAG, "Error subiendo archivo $cloudFileName a almacenamiento: ${e.message}")
                }
            }

            val finalMsg = if (errors == 0) {
                "Sincronización completada ($processed recursos subidos con éxito a la nube)."
            } else {
                "Sincronización finalizada: $processed subidos, $errors fallos."
            }

            _syncProgress.value = AssetSyncProgress(
                isRunning = false,
                totalFiles = total,
                processedFiles = processed,
                errorCount = errors,
                isFinished = true,
                statusMessage = finalMsg,
                lastError = lastErrStr,
                syncedBytes = totalBytesUploaded,
                activeBucket = activeBucketName
            )

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putLong("last_sync_timestamp", System.currentTimeMillis())
                .putInt(KEY_SYNCED_COUNT, processed)
                .putLong(KEY_SYNCED_BYTES, totalBytesUploaded)
                .apply()

        } catch (e: Exception) {
            Log.e(TAG, "Error general en proceso de sincronización: ${e.message}", e)
            _syncProgress.value = AssetSyncProgress(
                isRunning = false,
                isFinished = true,
                errorCount = 1,
                statusMessage = "Error en la sincronización: ${e.message}",
                lastError = e.localizedMessage ?: e.message
            )
        }
    }
}
