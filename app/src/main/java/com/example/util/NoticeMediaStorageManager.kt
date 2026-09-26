package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.tasks.await
import com.google.firebase.storage.FirebaseStorage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.OkHttpClient
import okhttp3.Request
import android.media.MediaMetadataRetriever
import android.provider.OpenableColumns
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject

object NoticeMediaStorageManager {
    private const val TAG = "NoticeMediaStorage"
    private const val MEDIA_DIR = "notice_media"
    private const val VIDEO_CACHE_DIR = "notice_video_cache"

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Normaliza URLs comunes de videos para permitir su descarga y reproducción directa
     */
    fun normalizeVideoUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        if (trimmed.isBlank()) return trimmed

        // Google Drive: convertir enlaces compartidos a descarga directa
        val driveMatch = Regex("drive\\.google\\.com/file/d/([a-zA-Z0-9_-]+)").find(trimmed)
            ?: Regex("drive\\.google\\.com/open\\?id=([a-zA-Z0-9_-]+)").find(trimmed)
        if (driveMatch != null) {
            val fileId = driveMatch.groupValues[1]
            return "https://drive.google.com/uc?export=download&id=$fileId"
        }

        // Dropbox: cambiar dl=0 a dl=1 o raw=1 para obtener el flujo binario
        if (trimmed.contains("dropbox.com", ignoreCase = true)) {
            if (trimmed.contains("dl=0")) {
                return trimmed.replace("dl=0", "dl=1")
            }
            if (!trimmed.contains("dl=1") && !trimmed.contains("raw=1")) {
                val sep = if (trimmed.contains("?")) "&" else "?"
                return "$trimmed${sep}dl=1"
            }
        }

        return trimmed
    }

    /**
     * Obtiene el archivo de video en caché local si ya fue descargado previamente y es válido.
     */
    fun getCachedVideoFile(context: Context, url: String): File? {
        try {
            val normalized = normalizeVideoUrl(url)
            val dir = File(context.cacheDir, VIDEO_CACHE_DIR)
            if (!dir.exists()) return null
            val key = "vid_" + normalized.hashCode().toString().replace("-", "n") + ".mp4"
            val file = File(dir, key)
            if (file.exists() && file.length() > 5000) {
                return file
            }
        } catch (_: Exception) {}
        return null
    }

    /**
     * Descarga y almacena en caché un video en segundo plano para reproducción instantánea y offline
     * utilizando OkHttp con redirecciones automáticas y User-Agent de navegador móvil.
     */
    suspend fun cacheVideoFromUrl(context: Context, url: String): File? = withContext(Dispatchers.IO) {
        val normalized = normalizeVideoUrl(url)
        if (!normalized.startsWith("http://", ignoreCase = true) && !normalized.startsWith("https://", ignoreCase = true)) {
            return@withContext null
        }
        try {
            val dir = File(context.cacheDir, VIDEO_CACHE_DIR)
            if (!dir.exists()) dir.mkdirs()
            val key = "vid_" + normalized.hashCode().toString().replace("-", "n") + ".mp4"
            val file = File(dir, key)
            if (file.exists() && file.length() > 5000) {
                return@withContext file
            }

            val tempFile = File(dir, "$key.tmp")
            val request = Request.Builder()
                .url(normalized)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.6668.70 Mobile Safari/537.36")
                .header("Accept", "*/*")
                .header("Connection", "keep-alive")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body
                if (body != null) {
                    val contentType = response.header("Content-Type", "") ?: ""
                    // Si el servidor devuelve HTML en vez de archivo de video (ej. página web o error), no guardar como mp4
                    if (contentType.contains("text/html", ignoreCase = true)) {
                        Log.w(TAG, "La URL devolvió una página HTML en lugar de un stream de video: $contentType")
                        response.close()
                        return@withContext null
                    }

                    body.byteStream().use { input ->
                        FileOutputStream(tempFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    if (tempFile.exists() && tempFile.length() > 5000) {
                        // Verificar que los primeros bytes no sean etiquetas HTML
                        val preview = ByteArray(64)
                        val readBytes = tempFile.inputStream().use { it.read(preview) }
                        val prefix = if (readBytes > 0) String(preview, 0, readBytes).trim().lowercase() else ""
                        if (prefix.startsWith("<!doctype") || prefix.startsWith("<html")) {
                            tempFile.delete()
                            Log.w(TAG, "El archivo descargado contiene HTML, descartando del caché de video.")
                            return@withContext null
                        }

                        tempFile.renameTo(file)
                        Log.d(TAG, "Video descargado y almacenado en caché: ${file.absolutePath} (${file.length() / 1024} KB)")
                        return@withContext file
                    }
                }
            } else {
                Log.w(TAG, "Fallo HTTP al descargar video: ${response.code} en $normalized")
            }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo almacenar en caché el video: ${e.message}")
        }
        null
    }

    /**
     * Guarda un video codificado en Base64 data:video/... en un archivo temporal de caché para VideoView.
     */
    fun saveBase64VideoToCache(context: Context, dataUri: String): File? {
        return try {
            if (!dataUri.startsWith("data:video/")) return null
            val base64Data = dataUri.substringAfter("base64,")
            val bytes = Base64.decode(base64Data, Base64.DEFAULT)
            val dir = File(context.cacheDir, VIDEO_CACHE_DIR)
            if (!dir.exists()) dir.mkdirs()
            val key = "b64vid_" + dataUri.hashCode().toString().replace("-", "n") + ".mp4"
            val file = File(dir, key)
            if (!file.exists() || file.length() != bytes.size.toLong()) {
                FileOutputStream(file).use { it.write(bytes) }
            }
            file
        } catch (e: Exception) {
            Log.w(TAG, "Error procesando video Base64: ${e.message}")
            null
        }
    }

    /**
     * Guarda el archivo localmente en el almacenamiento privado del app
     * para que jamás caduque ni dependa de permisos de ContentResolver.
     * Retorna la ruta con esquema file://
     */
    suspend fun saveMediaToInternalStorage(context: Context, uri: Uri, isVideo: Boolean): String = withContext(Dispatchers.IO) {
        try {
            val dir = File(context.filesDir, MEDIA_DIR)
            if (!dir.exists()) dir.mkdirs()

            val ext = if (isVideo) "mp4" else "jpg"
            val targetFile = File(dir, "media_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.$ext")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(32768)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                    output.flush()
                }
            }
            try { targetFile.setReadable(true, false) } catch (_: Exception) {}
            Log.d(TAG, "Media guardada permanentemente en almacenamiento interno: ${targetFile.absolutePath}, tamaño: ${targetFile.length()} bytes")
            "file://${targetFile.absolutePath}"
        } catch (e: Exception) {
            Log.e(TAG, "Error guardando en almacenamiento interno: ${e.message}")
            uri.toString()
        }
    }

    /**
     * Determina con precisión blindada si una URI seleccionada de la galería o explorador es un archivo de video.
     * Comprueba tipo MIME de ContentResolver, extensiones de nombre, encabezados/firmas binarias (magic bytes)
     * y metadatos de medios.
     */
    fun isUriVideo(context: Context, uri: Uri): Boolean {
        // 1. Tipo MIME directo
        try {
            val mime = context.contentResolver.getType(uri)
            if (mime?.startsWith("video/", ignoreCase = true) == true) return true
            if (mime?.startsWith("image/", ignoreCase = true) == true) return false
        } catch (_: Exception) {}

        // 2. Nombre del archivo o parámetro de URI
        val uriStr = uri.toString().lowercase()
        val videoExtensions = listOf(".mp4", ".mkv", ".webm", ".mov", ".3gp", ".avi", ".m4v", ".ts")
        if (videoExtensions.any { uriStr.endsWith(it) || uriStr.contains(it) }) return true

        // 3. Consultar nombre en ContentResolver
        try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx != -1) {
                        val name = cursor.getString(nameIdx)?.lowercase() ?: ""
                        if (videoExtensions.any { name.endsWith(it) }) return true
                        val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp")
                        if (imageExtensions.any { name.endsWith(it) }) return false
                    }
                }
            }
        } catch (_: Exception) {}

        // 4. Verificación de Magic Bytes en la cabecera del archivo
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val header = ByteArray(32)
                val read = stream.read(header)
                if (read >= 12) {
                    // MP4 / MOV / 3GP: bytes 4..7 suelen ser "ftyp"
                    val isFtyp = header[4] == 'f'.code.toByte() &&
                            header[5] == 't'.code.toByte() &&
                            header[6] == 'y'.code.toByte() &&
                            header[7] == 'p'.code.toByte()
                    if (isFtyp) return true

                    // Matroska / WebM: 0x1A 0x45 0xDF 0xA3
                    if (header[0] == 0x1A.toByte() && header[1] == 0x45.toByte() &&
                        header[2] == 0xDF.toByte() && header[3] == 0xA3.toByte()) return true

                    // AVI: RIFF ... AVI 
                    if (header[0] == 'R'.code.toByte() && header[1] == 'I'.code.toByte() &&
                        header[2] == 'F'.code.toByte() && header[3] == 'F'.code.toByte() &&
                        header[8] == 'A'.code.toByte() && header[9] == 'V'.code.toByte() &&
                        header[10] == 'I'.code.toByte()) return true

                    // Descartar si es imagen conocida
                    val isJpeg = header[0] == 0xFF.toByte() && header[1] == 0xD8.toByte() && header[2] == 0xFF.toByte()
                    val isPng = header[0] == 0x89.toByte() && header[1] == 'P'.code.toByte() &&
                            header[2] == 'N'.code.toByte() && header[3] == 'G'.code.toByte()
                    val isGif = header[0] == 'G'.code.toByte() && header[1] == 'I'.code.toByte() && header[2] == 'F'.code.toByte()
                    if (isJpeg || isPng || isGif) return false
                }
            }
        } catch (_: Exception) {}

        // 5. Verificación de respaldo mediante MediaMetadataRetriever
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)
            retriever.release()
            hasVideo == "yes"
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Extrae una captura o miniatura en Bitmap del video seleccionado
     */
    fun getVideoThumbnail(context: Context, uri: Uri): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val frame = retriever.getFrameAtTime(1000000) // 1 seg
                ?: retriever.getFrameAtTime(0)
            retriever.release()
            frame
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Intenta subir un archivo de video al almacenamiento remoto en la nube (Firebase Storage).
     * Configura metadatos video/mp4 y guarda en caché local inmediata para visualización instantánea.
     */
    suspend fun tryUploadVideoToCloud(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        withTimeoutOrNull(45000L) {
            try {
                // Asegurar autenticación para Firebase Storage
                val auth = try { com.google.firebase.auth.FirebaseAuth.getInstance() } catch (_: Exception) { null }
                if (auth?.currentUser == null) {
                    try {
                        auth?.signInAnonymously()?.await()
                    } catch (_: Exception) {}
                }

                // Guardar en archivo temporal seguro para lectura estable
                val tempUploadFile = File(context.cacheDir, "temp_upload_${System.currentTimeMillis()}.mp4")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempUploadFile).use { output ->
                        input.copyTo(output)
                    }
                }

                if (!tempUploadFile.exists() || tempUploadFile.length() == 0L) {
                    return@withTimeoutOrNull null
                }

                val storages = listOfNotNull(
                    try { FirebaseStorage.getInstance("gs://wild-rift-drafting.firebasestorage.app") } catch (_: Exception) { null },
                    try { FirebaseStorage.getInstance() } catch (_: Exception) { null },
                    try { FirebaseStorage.getInstance("gs://wild-rift-drafting.appspot.com") } catch (_: Exception) { null }
                )

                var finalUrl: String? = null
                val filename = "notice_videos/${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.mp4"
                val metadata = com.google.firebase.storage.StorageMetadata.Builder()
                    .setContentType("video/mp4")
                    .setCustomMetadata("uploadedAt", System.currentTimeMillis().toString())
                    .build()

                for (storage in storages) {
                    try {
                        val videoRef = storage.reference.child(filename)
                        videoRef.putFile(Uri.fromFile(tempUploadFile), metadata).await()
                        val downloadUrl = videoRef.downloadUrl.await().toString()
                        if (downloadUrl.isNotBlank()) {
                            finalUrl = downloadUrl
                            Log.d(TAG, "Video subido exitosamente a Firebase Storage: $finalUrl")
                            break
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Intento de subida en bucket ${storage.app.name} falló: ${e.message}")
                    }
                }

                if (finalUrl != null) {
                    // Guardar copia inmediata en caché local
                    try {
                        val dir = File(context.cacheDir, VIDEO_CACHE_DIR)
                        if (!dir.exists()) dir.mkdirs()
                        val key = "vid_" + finalUrl.hashCode().toString().replace("-", "n") + ".mp4"
                        val cachedFile = File(dir, key)
                        tempUploadFile.copyTo(cachedFile, overwrite = true)
                    } catch (_: Exception) {}
                }

                try { tempUploadFile.delete() } catch (_: Exception) {}
                finalUrl
            } catch (e: Exception) {
                Log.w(TAG, "Error subiendo video a Firebase Storage: ${e.message}")
                null
            }
        }
    }

    /**
     * Sube un video a Catbox.moe para obtener un enlace público directo permanente HTTPS (.mp4)
     * compatible con reproducción instantánea en cualquier dispositivo.
     */
    suspend fun tryUploadToCatbox(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        withTimeoutOrNull(45000L) {
            try {
                val tempFile = File(context.cacheDir, "catbox_upload_${System.currentTimeMillis()}.mp4")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { output -> input.copyTo(output) }
                }
                if (!tempFile.exists() || tempFile.length() == 0L) return@withTimeoutOrNull null

                val requestBody = okhttp3.MultipartBody.Builder()
                    .setType(okhttp3.MultipartBody.FORM)
                    .addFormDataPart("reqtype", "fileupload")
                    .addFormDataPart(
                        "fileToUpload",
                        "video_${System.currentTimeMillis()}.mp4",
                        okhttp3.RequestBody.create("video/mp4".toMediaTypeOrNull(), tempFile)
                    )
                    .build()

                val request = Request.Builder()
                    .url("https://catbox.moe/user/api.php")
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) CoachApp/1.0")
                    .post(requestBody)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseText = response.body?.string()?.trim() ?: ""
                if (response.isSuccessful && responseText.startsWith("http")) {
                    Log.d(TAG, "Video subido exitosamente a Catbox: $responseText")
                    // Guardar en caché local
                    try {
                        val dir = File(context.cacheDir, VIDEO_CACHE_DIR)
                        if (!dir.exists()) dir.mkdirs()
                        val key = "vid_" + responseText.hashCode().toString().replace("-", "n") + ".mp4"
                        tempFile.copyTo(File(dir, key), overwrite = true)
                    } catch (_: Exception) {}
                    try { tempFile.delete() } catch (_: Exception) {}
                    return@withTimeoutOrNull responseText
                }
                try { tempFile.delete() } catch (_: Exception) {}
                null
            } catch (e: Exception) {
                Log.w(TAG, "Error subiendo a Catbox: ${e.message}")
                null
            }
        }
    }

    /**
     * Sube un video a Tmpfiles API como alternativa en la nube directa.
     */
    suspend fun tryUploadToTmpfiles(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        withTimeoutOrNull(30000L) {
            try {
                val tempFile = File(context.cacheDir, "tmpfiles_upload_${System.currentTimeMillis()}.mp4")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { output -> input.copyTo(output) }
                }
                if (!tempFile.exists() || tempFile.length() == 0L) return@withTimeoutOrNull null

                val requestBody = okhttp3.MultipartBody.Builder()
                    .setType(okhttp3.MultipartBody.FORM)
                    .addFormDataPart(
                        "input_file",
                        "video_${System.currentTimeMillis()}.mp4",
                        okhttp3.RequestBody.create("video/mp4".toMediaTypeOrNull(), tempFile)
                    )
                    .build()

                val request = Request.Builder()
                    .url("https://tmpfiles.org/api/v1/upload")
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) CoachApp/1.0")
                    .post(requestBody)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseText = response.body?.string()?.trim() ?: ""
                if (response.isSuccessful && responseText.isNotBlank()) {
                    val json = JSONObject(responseText)
                    if (json.optString("status") == "success") {
                        val originalUrl = json.getJSONObject("data").getString("url")
                        val directUrl = originalUrl.replace("tmpfiles.org/", "tmpfiles.org/dl/")
                        Log.d(TAG, "Video subido exitosamente a Tmpfiles: $directUrl")
                        try {
                            val dir = File(context.cacheDir, VIDEO_CACHE_DIR)
                            if (!dir.exists()) dir.mkdirs()
                            val key = "vid_" + directUrl.hashCode().toString().replace("-", "n") + ".mp4"
                            tempFile.copyTo(File(dir, key), overwrite = true)
                        } catch (_: Exception) {}
                        try { tempFile.delete() } catch (_: Exception) {}
                        return@withTimeoutOrNull directUrl
                    }
                }
                try { tempFile.delete() } catch (_: Exception) {}
                null
            } catch (e: Exception) {
                Log.w(TAG, "Error subiendo a Tmpfiles: ${e.message}")
                null
            }
        }
    }

    /**
     * Intenta subir un archivo de video a Supabase Storage con timeout y manejo robusto de red.
     */
    suspend fun tryUploadVideoToSupabase(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        withTimeoutOrNull(15000L) {
            try {
                val supabaseUrl = com.example.data.supabase.SupabaseClientManager.getActiveUrl()
                val supabaseKey = com.example.data.supabase.SupabaseClientManager.getActiveKey()
                if (supabaseUrl.isBlank() || supabaseKey.isBlank()) return@withTimeoutOrNull null

                val filename = "vid_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.mp4"
                val bucket = "notice_media"
                val uploadUrl = "${supabaseUrl.removeSuffix("/")}/storage/v1/object/$bucket/$filename"

                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withTimeoutOrNull null
                val bytes = inputStream.use { it.readBytes() }
                if (bytes.isEmpty()) return@withTimeoutOrNull null

                val mediaType = "video/mp4".toMediaTypeOrNull()
                val requestBody = bytes.toRequestBody(mediaType)

                val request = Request.Builder()
                    .url(uploadUrl)
                    .addHeader("apikey", supabaseKey)
                    .addHeader("Authorization", "Bearer $supabaseKey")
                    .addHeader("x-upsert", "true")
                    .post(requestBody)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                if (response.isSuccessful || response.code in 200..204) {
                    val publicUrl = "${supabaseUrl.removeSuffix("/")}/storage/v1/object/public/$bucket/$filename"
                    Log.d(TAG, "Video subido exitosamente a Supabase Storage: $publicUrl")

                    // Guardar copia local inmediata en caché
                    try {
                        val dir = File(context.cacheDir, VIDEO_CACHE_DIR)
                        if (!dir.exists()) dir.mkdirs()
                        val key = "vid_" + publicUrl.hashCode().toString().replace("-", "n") + ".mp4"
                        val file = File(dir, key)
                        FileOutputStream(file).use { it.write(bytes) }
                    } catch (_: Exception) {}

                    publicUrl
                } else {
                    Log.w(TAG, "Fallo al subir video a Supabase Storage: HTTP ${response.code}")
                    null
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error subiendo video a Supabase Storage: ${e.message}")
                null
            }
        }
    }

    /**
     * Convierte videos ultra-ligeros (< 100 KB) a Data URL Base64 para sincronización ligera.
     * Para videos mayores, se almacena en disco local permanente o en almacenamiento remoto
     * evitando saturar el límite de tamaño de documento de 1MB.
     */
    fun convertVideoToDataUrl(context: Context, uri: Uri, maxBytes: Int = 100_000): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bytes = inputStream.use { it.readBytes() }
            if (bytes.isEmpty() || bytes.size > maxBytes) {
                Log.d(TAG, "Video de ${bytes.size} bytes: se utilizará almacenamiento en archivo local o remoto.")
                return null
            }
            val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            "data:video/mp4;base64,$b64"
        } catch (e: Exception) {
            Log.w(TAG, "Error convirtiendo video a Base64: ${e.message}")
            null
        }
    }

    /**
     * Guarda el video de forma blindada:
     * 1. Almacena copia permanente en disco local para reproducción instantánea (0ms).
     * 2. Intenta subirlo a Firebase Storage con metadatos video/mp4 (prioritario para sincronización global).
     * 3. Si no está disponible, intenta Catbox Cloud (enlace HTTPS mp4 público permanente multidispositivo).
     * 4. Si no está disponible, intenta Tmpfiles Cloud.
     * 5. Si no está disponible, intenta Supabase Storage si estuviera configurado.
     * 6. Si es ligero (< 500KB), genera Data URL Base64 para que se replique en Firestore en todos los dispositivos.
     * 7. Si todo lo anterior falla, retorna la ruta local permanente file://.
     */
    suspend fun uploadOrSaveVideo(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        // 1. Guardar siempre copia permanente en almacenamiento local privado
        val localPath = saveMediaToInternalStorage(context, uri, isVideo = true)

        // 2. Intentar subir primero a Firebase Storage (almacenamiento en la nube multidispositivo)
        val cloudUrl = tryUploadVideoToCloud(context, uri)
        if (!cloudUrl.isNullOrBlank()) {
            return@withContext cloudUrl
        }

        // 3. Intentar subir a Catbox Cloud (enlace HTTPS mp4 público permanente multidispositivo)
        val catboxUrl = tryUploadToCatbox(context, uri)
        if (!catboxUrl.isNullOrBlank()) {
            return@withContext catboxUrl
        }

        // 4. Intentar subir a Tmpfiles Cloud
        val tmpfilesUrl = tryUploadToTmpfiles(context, uri)
        if (!tmpfilesUrl.isNullOrBlank()) {
            return@withContext tmpfilesUrl
        }

        // 5. Intentar subir a Supabase Storage como almacenamiento secundario si está disponible
        val supabaseUrl = tryUploadVideoToSupabase(context, uri)
        if (!supabaseUrl.isNullOrBlank()) {
            return@withContext supabaseUrl
        }

        // 6. Si es video compacto (< 500KB), generar Data URL Base64 para sincronización instantánea en la nube
        val dataUrl = convertVideoToDataUrl(context, uri, maxBytes = 500_000)
        if (!dataUrl.isNullOrBlank()) {
            return@withContext dataUrl
        }

        // 7. Retornar la ruta local permanente file://
        localPath
    }

    suspend fun uploadVideoToCloud(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        uploadOrSaveVideo(context, uri)
    }

    suspend fun convertImageToCloudDataUrl(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            // 1. Guardar primero copia permanente en disco local
            val localPath = saveMediaToInternalStorage(context, uri, isVideo = false)

            // 2. Leer los bytes para comprimir y codificar a Base64
            val localFile = File(Uri.parse(localPath).path ?: "")
            val inputStream: InputStream = if (localFile.exists()) {
                localFile.inputStream()
            } else {
                context.contentResolver.openInputStream(uri) ?: return@withContext localPath
            }

            val originalBitmap = inputStream.use { BitmapFactory.decodeStream(it) } ?: return@withContext localPath

            // Escalar proporcionalmente si excede 1000px
            val maxDim = 1000
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scaledBitmap = if (width > maxDim || height > maxDim) {
                val ratio = width.toFloat() / height.toFloat()
                val targetW: Int
                val targetH: Int
                if (width > height) {
                    targetW = maxDim
                    targetH = (maxDim / ratio).toInt().coerceAtLeast(1)
                } else {
                    targetH = maxDim
                    targetW = (maxDim * ratio).toInt().coerceAtLeast(1)
                }
                Bitmap.createScaledBitmap(originalBitmap, targetW, targetH, true)
            } else {
                originalBitmap
            }

            val baos = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos)
            val bytes = baos.toByteArray()
            val base64String = Base64.encodeToString(bytes, Base64.NO_WRAP)
            Log.d(TAG, "Imagen convertida a Data URL Base64 (${bytes.size / 1024} KB) para sincronización multidispositivo")
            "data:image/jpeg;base64,$base64String"
        } catch (e: Exception) {
            Log.e(TAG, "Error convirtiendo imagen a Cloud Data URL: ${e.message}")
            uri.toString()
        }
    }

    /**
     * Decodifica un Data URL Base64 a ByteArray para que Coil o ImageView lo cargue directamente
     */
    fun decodeDataUriToBytes(dataUri: String): ByteArray? {
        return try {
            if (dataUri.startsWith("data:image/")) {
                val base64Data = dataUri.substringAfter("base64,")
                Base64.decode(base64Data, Base64.DEFAULT)
            } else null
        } catch (e: Exception) {
            Log.w(TAG, "Error decodificando Data URL: ${e.message}")
            null
        }
    }
}
