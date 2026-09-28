package com.example.ui.components

import com.example.util.tr

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import java.io.File
import coil.compose.AsyncImage
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object NoticeMediaUtils {
    val pauseAndMuteTrigger = kotlinx.coroutines.flow.MutableStateFlow(0)

    fun pauseAndMuteAll() {
        pauseAndMuteTrigger.value++
    }

    /**
     * Extrae de forma exhaustiva el ID de video de YouTube para URLs estándar,
     * cortas (youtu.be), shorts, live, embeds y con parámetros adicionales (si, feature, etc.).
     */
    fun extractYouTubeVideoId(url: String): String? {
        if (url.isBlank()) return null
        val trimmed = url.trim()

        // 1. Extracción mediante Uri nativo de Android
        try {
            val uri = Uri.parse(trimmed)
            val host = uri.host?.lowercase() ?: ""
            if (host.contains("youtu.be")) {
                val segment = uri.pathSegments.firstOrNull { it.isNotBlank() }
                if (!segment.isNullOrBlank()) {
                    val cleanId = segment.substringBefore("?").substringBefore("&")
                    if (cleanId.matches(Regex("^[a-zA-Z0-9_-]{11}$"))) {
                        return cleanId
                    }
                }
            } else if (host.contains("youtube.com") || host.contains("youtube-nocookie.com")) {
                val vParam = uri.getQueryParameter("v")
                if (!vParam.isNullOrBlank() && vParam.matches(Regex("^[a-zA-Z0-9_-]{11}$"))) {
                    return vParam
                }
                val segments = uri.pathSegments
                val keyIndex = segments.indexOfFirst {
                    it.equals("embed", true) || it.equals("shorts", true) || it.equals("live", true) || it.equals("v", true)
                }
                if (keyIndex != -1 && keyIndex + 1 < segments.size) {
                    val candidate = segments[keyIndex + 1].substringBefore("?").substringBefore("&")
                    if (candidate.matches(Regex("^[a-zA-Z0-9_-]{11}$"))) {
                        return candidate
                    }
                }
            }
        } catch (_: Exception) {}

        // 2. Extracción de patrones Regex avanzados
        val patterns = listOf(
            Regex("(?:https?://)?(?:www\\.|m\\.)?youtube\\.com/watch\\?.*?[?&]v=([a-zA-Z0-9_-]{11})"),
            Regex("(?:https?://)?(?:www\\.|m\\.)?youtube\\.com/watch\\?v=([a-zA-Z0-9_-]{11})"),
            Regex("(?:https?://)?(?:www\\.|m\\.)?youtu\\.be/([a-zA-Z0-9_-]{11})"),
            Regex("(?:https?://)?(?:www\\.|m\\.)?youtube(?:-nocookie)?\\.com/embed/([a-zA-Z0-9_-]{11})"),
            Regex("(?:https?://)?(?:www\\.|m\\.)?youtube\\.com/shorts/([a-zA-Z0-9_-]{11})"),
            Regex("(?:https?://)?(?:www\\.|m\\.)?youtube\\.com/live/([a-zA-Z0-9_-]{11})"),
            Regex("(?:https?://)?(?:www\\.|m\\.)?youtube\\.com/v/([a-zA-Z0-9_-]{11})")
        )

        for (pattern in patterns) {
            val match = pattern.find(trimmed)
            if (match != null && match.groupValues.size > 1) {
                return match.groupValues[1]
            }
        }

        if (trimmed.length == 11 && trimmed.matches(Regex("^[a-zA-Z0-9_-]{11}$"))) {
            return trimmed
        }
        return null
    }

    fun isYouTubeUrl(url: String): Boolean {
        val trimmed = url.trim()
        return trimmed.contains("youtube.com", ignoreCase = true) ||
               trimmed.contains("youtu.be", ignoreCase = true) ||
               extractYouTubeVideoId(trimmed) != null
    }

    /**
     * Identifica servicios de video basados en web como Vimeo, Streamable o Dailymotion
     * que se reproducen mediante WebView en lugar de VideoView.
     */
    fun isWebVideoUrl(url: String): Boolean {
        val trimmed = url.trim().lowercase()
        return trimmed.contains("vimeo.com") ||
               trimmed.contains("streamable.com") ||
               trimmed.contains("dailymotion.com") ||
               trimmed.contains("twitch.tv") ||
               (isYouTubeUrl(url) && extractYouTubeVideoId(url) == null)
    }

    fun isVideo(context: Context, url: String): Boolean {
        if (url.isBlank()) return false
        val trimmed = url.trim().lowercase()
        if (trimmed.startsWith("data:image/")) return false
        if (trimmed.startsWith("data:video/")) return true
        if (isYouTubeUrl(url) || isWebVideoUrl(url)) return true

        val cleanUrl = trimmed.substringBefore("?").substringBefore("#")
        val videoExtensions = listOf(".mp4", ".mkv", ".webm", ".mov", ".3gp", ".avi", ".m4v", ".ts")
        if (videoExtensions.any { cleanUrl.endsWith(it) || trimmed.contains(it) }) {
            return true
        }

        if (trimmed.contains("notice_videos") || trimmed.contains("/video") || trimmed.contains("video%2f") || trimmed.contains("video/")) {
            return true
        }

        if (trimmed.startsWith("file://") || trimmed.startsWith("/")) {
            val path = if (trimmed.startsWith("file://")) Uri.parse(url).path ?: "" else trimmed
            val ext = File(path).extension.lowercase()
            if (ext in listOf("mp4", "mkv", "webm", "mov", "3gp", "avi", "m4v")) return true
        }

        if (trimmed.startsWith("content://")) {
            try {
                val mimeType = context.contentResolver.getType(Uri.parse(url))
                if (mimeType?.startsWith("video/") == true) {
                    return true
                }
            } catch (_: Exception) {}
        }
        return false
    }

    fun isLocalVideo(context: Context, url: String): Boolean {
        return isVideo(context, url) && !isYouTubeUrl(url) && !isWebVideoUrl(url)
    }

    fun isValidNoticeMedia(url: String): Boolean {
        if (url.isBlank()) return true
        val trimmed = url.trim()
        if (trimmed.startsWith("content://") || trimmed.startsWith("file://") || trimmed.startsWith("data:image/") || trimmed.startsWith("data:video/")) return true
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return true
        if (isYouTubeUrl(trimmed) || isWebVideoUrl(trimmed)) return true
        return false
    }

    /**
     * Detecta si un video o imagen tiene proporción vertical (alto >= ancho).
     */
    fun isMediaVertical(context: Context, url: String): Boolean {
        if (url.isBlank()) return false
        val trimmed = url.trim()

        if (isVideo(context, trimmed)) {
            val retriever = android.media.MediaMetadataRetriever()
            try {
                if (trimmed.startsWith("file://") || trimmed.startsWith("/")) {
                    val path = if (trimmed.startsWith("file://")) Uri.parse(trimmed).path ?: "" else trimmed
                    retriever.setDataSource(path)
                } else if (trimmed.startsWith("content://")) {
                    retriever.setDataSource(context, Uri.parse(trimmed))
                } else if (trimmed.startsWith("data:video/")) {
                    val cached = com.example.util.NoticeMediaStorageManager.saveBase64VideoToCache(context, trimmed)
                    if (cached != null) retriever.setDataSource(cached.absolutePath)
                } else if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                    val cached = com.example.util.NoticeMediaStorageManager.getCachedVideoFile(context, trimmed)
                    if (cached != null && cached.exists()) {
                        retriever.setDataSource(cached.absolutePath)
                    } else {
                        retriever.setDataSource(trimmed, HashMap())
                    }
                }
                val widthStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                val heightStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                val rotationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION) ?: "0"
                retriever.release()

                val w = widthStr?.toIntOrNull() ?: 0
                val h = heightStr?.toIntOrNull() ?: 0
                val rot = rotationStr.toIntOrNull() ?: 0
                val isRotated = rot == 90 || rot == 270
                val effectiveW = if (isRotated) h else w
                val effectiveH = if (isRotated) w else h
                if (effectiveW > 0 && effectiveH > 0) {
                    return effectiveH >= effectiveW
                }
            } catch (_: Exception) {
                try { retriever.release() } catch (_: Exception) {}
            }
        } else {
            try {
                val options = android.graphics.BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                if (trimmed.startsWith("file://") || trimmed.startsWith("/")) {
                    val path = if (trimmed.startsWith("file://")) Uri.parse(trimmed).path ?: "" else trimmed
                    android.graphics.BitmapFactory.decodeFile(path, options)
                } else if (trimmed.startsWith("content://")) {
                    val stream = context.contentResolver.openInputStream(Uri.parse(trimmed))
                    android.graphics.BitmapFactory.decodeStream(stream, null, options)
                    stream?.close()
                } else if (trimmed.startsWith("data:image/")) {
                    val bytes = com.example.util.NoticeMediaStorageManager.decodeDataUriToBytes(trimmed)
                    if (bytes != null) {
                        android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                    }
                } else if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                    val cached = com.example.util.NoticeMediaStorageManager.getCachedVideoFile(context, trimmed)
                    if (cached != null && cached.exists()) {
                        android.graphics.BitmapFactory.decodeFile(cached.absolutePath, options)
                    }
                }
                if (options.outWidth > 0 && options.outHeight > 0) {
                    return options.outHeight >= options.outWidth
                }
            } catch (_: Exception) {}
        }
        return false
    }

    fun formatDurationMs(ms: Int): String {
        if (ms <= 0) return "00:00"
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    data class MediaValidationResult(
        val isValid: Boolean,
        val isVideo: Boolean,
        val width: Int,
        val height: Int,
        val isVertical: Boolean,
        val durationMs: Long = 0L,
        val fileSizeBytes: Long = 0L,
        val errorMessage: String? = null
    )

    /**
     * Valida de forma estricta el tamaño, dimensiones, proporción y formato recomendado
     * para la subida de anuncios tanto para el Promotor como para el Administrador.
     */
    fun validateMediaForSlot(context: Context, uri: Uri, isVerticalSlot: Boolean): MediaValidationResult {
        try {
            val mimeType = context.contentResolver.getType(uri) ?: ""
            val path = uri.toString().lowercase()
            val isVideo = com.example.util.NoticeMediaStorageManager.isUriVideo(context, uri) || mimeType.startsWith("video")

            var fileSizeBytes = 0L
            try {
                val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                fileSizeBytes = pfd?.statSize ?: 0L
                pfd?.close()
            } catch (_: Exception) {}

            if (isVideo) {
                val isMp4 = mimeType.equals("video/mp4", true) || path.endsWith(".mp4")
                if (!isMp4) {
                    return MediaValidationResult(
                        isValid = false, isVideo = true, width = 0, height = 0, isVertical = false,
                        errorMessage = "Formato no admitido: Los videos deben estar estrictamente en formato MP4."
                    )
                }
                if (fileSizeBytes > 10 * 1024 * 1024L) {
                    return MediaValidationResult(
                        isValid = false, isVideo = true, width = 0, height = 0, isVertical = false,
                        errorMessage = "Tamaño excedido: El video no debe superar los 10 MB."
                    )
                }

                val retriever = android.media.MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                val durStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                val widthStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                val heightStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                val rotationStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION) ?: "0"
                retriever.release()

                val durMs = durStr?.toLongOrNull() ?: 0L
                if (durMs > 15_500L) {
                    return MediaValidationResult(
                        isValid = false, isVideo = true, width = 0, height = 0, isVertical = false,
                        errorMessage = "Duración excedida: El video no puede superar los 15 segundos (detectado: ${(durMs / 1000)}s)."
                    )
                }

                val rawW = widthStr?.toIntOrNull() ?: 0
                val rawH = heightStr?.toIntOrNull() ?: 0
                val rot = rotationStr.toIntOrNull() ?: 0
                val isRotated = rot == 90 || rot == 270
                val w = if (isRotated) rawH else rawW
                val h = if (isRotated) rawW else rawH
                val isVertical = h >= w

                if (isVerticalSlot && !isVertical) {
                    return MediaValidationResult(
                        isValid = false, isVideo = true, width = w, height = h, isVertical = isVertical,
                        errorMessage = "Orientación incorrecta: Para la vista ampliada vertical debes subir un video vertical (proporción recomendada 9:16 / 1080x1920). Dimensiones detectadas: ${w}x${h}."
                    )
                }
                if (!isVerticalSlot && isVertical) {
                    return MediaValidationResult(
                        isValid = false, isVideo = true, width = w, height = h, isVertical = isVertical,
                        errorMessage = "Orientación incorrecta: Para el banner horizontal debes subir un video horizontal (proporción recomendada 16:9 / 1920x1080). Dimensiones detectadas: ${w}x${h}."
                    )
                }

                return MediaValidationResult(isValid = true, isVideo = true, width = w, height = h, isVertical = isVertical, durationMs = durMs, fileSizeBytes = fileSizeBytes)
            } else {
                val isPngOrJpg = mimeType.equals("image/png", true) || path.endsWith(".png") ||
                                 mimeType.equals("image/jpeg", true) || mimeType.equals("image/jpg", true) ||
                                 path.endsWith(".jpg") || path.endsWith(".jpeg") || mimeType.equals("image/webp", true) || path.endsWith(".webp")
                if (!isPngOrJpg) {
                    return MediaValidationResult(
                        isValid = false, isVideo = false, width = 0, height = 0, isVertical = false,
                        errorMessage = "Formato no admitido: Las imágenes deben ser PNG, JPG o WEBP."
                    )
                }
                if (fileSizeBytes > 5 * 1024 * 1024L) {
                    return MediaValidationResult(
                        isValid = false, isVideo = false, width = 0, height = 0, isVertical = false,
                        errorMessage = "Tamaño excedido: La imagen no debe superar los 5 MB."
                    )
                }

                val options = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                val stream = context.contentResolver.openInputStream(uri)
                android.graphics.BitmapFactory.decodeStream(stream, null, options)
                stream?.close()

                val w = options.outWidth
                val h = options.outHeight
                val isVertical = h >= w

                if (isVerticalSlot && !isVertical) {
                    return MediaValidationResult(
                        isValid = false, isVideo = false, width = w, height = h, isVertical = isVertical,
                        errorMessage = "Orientación incorrecta: Para la vista ampliada vertical debes subir una imagen vertical (proporción recomendada 9:16 / 1080x1920). Dimensiones detectadas: ${w}x${h}."
                    )
                }
                if (!isVerticalSlot && isVertical) {
                    return MediaValidationResult(
                        isValid = false, isVideo = false, width = w, height = h, isVertical = isVertical,
                        errorMessage = "Orientación incorrecta: Para el banner horizontal debes subir una imagen horizontal (proporción recomendada 16:9 / 1920x1080). Dimensiones detectadas: ${w}x${h}."
                    )
                }

                return MediaValidationResult(isValid = true, isVideo = false, width = w, height = h, isVertical = isVertical, fileSizeBytes = fileSizeBytes)
            }
        } catch (e: Exception) {
            return MediaValidationResult(isValid = false, isVideo = false, width = 0, height = 0, isVertical = false, errorMessage = "Error al leer archivo: ${e.message}")
        }
    }
}

@Composable
fun NoticeMediaViewer(
    mediaUrl: String,
    modifier: Modifier = Modifier,
    isFullscreen: Boolean = false,
    externalUrl: String = "",
    onExpand: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    onImageClick: (() -> Unit)? = null
) {
    if (mediaUrl.isBlank()) return
    val context = LocalContext.current
    val normalizedUrl = remember(mediaUrl) { com.example.util.NoticeMediaStorageManager.normalizeVideoUrl(mediaUrl.trim()) }

    val isYt = remember(normalizedUrl) { NoticeMediaUtils.isYouTubeUrl(normalizedUrl) }
    val ytVideoId = remember(normalizedUrl) { NoticeMediaUtils.extractYouTubeVideoId(normalizedUrl) }
    val isWebVideo = remember(normalizedUrl) { NoticeMediaUtils.isWebVideoUrl(normalizedUrl) }
    val isVideo = remember(normalizedUrl) { NoticeMediaUtils.isVideo(context, normalizedUrl) }
    val isVertical = remember(normalizedUrl, context) { NoticeMediaUtils.isMediaVertical(context, normalizedUrl) }

    val containerModifier = if (isFullscreen) {
        modifier.fillMaxSize()
    } else {
        modifier
            .fillMaxWidth()
            .then(
                if (isVertical) Modifier.aspectRatio(9f / 16f) else Modifier.aspectRatio(16f / 9f)
            )
    }

    val boxModifier = if (isFullscreen) {
        containerModifier.background(Color.Black)
    } else {
        containerModifier
            .clip(RoundedCornerShape(10.dp))
            .background(HextechDarkBg)
            .border(
                1.2.dp,
                if (isVertical) HextechCyan.copy(alpha = 0.65f) else HextechGold.copy(alpha = 0.65f),
                RoundedCornerShape(10.dp)
            )
    }

    Box(
        modifier = boxModifier
    ) {
        if (isYt && ytVideoId != null) {
            val lifecycleOwner = LocalLifecycleOwner.current
            var webViewInstance by remember { mutableStateOf<WebView?>(null) }

            DisposableEffect(lifecycleOwner, ytVideoId) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> {
                            try {
                                webViewInstance?.onResume()
                                webViewInstance?.resumeTimers()
                            } catch (_: Exception) {}
                        }
                        Lifecycle.Event.ON_PAUSE -> {
                            try {
                                webViewInstance?.onPause()
                                webViewInstance?.pauseTimers()
                            } catch (_: Exception) {}
                        }
                        Lifecycle.Event.ON_DESTROY -> {
                            try {
                                webViewInstance?.destroy()
                            } catch (_: Exception) {}
                        }
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            // YouTube Player with Error 153 fix, modern WebView settings, and custom Chrome user agent
            var isYtMuted by remember { mutableStateOf(true) }
            val currentPrimaryColor = MaterialTheme.colorScheme.primary

            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                mediaPlaybackRequiresUserGesture = false
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                allowFileAccess = true
                                allowContentAccess = true
                                setSupportZoom(false)
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                // Fixing YouTube Error 153: Emulate standard mobile Chrome browser
                                userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.6668.70 Mobile Safari/537.36"
                            }
                            webChromeClient = WebChromeClient()
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    return false
                                }
                            }
                            val html = """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                                    <style>
                                        * { box-sizing: border-box; margin: 0; padding: 0; }
                                        body, html { width: 100%; height: 100%; background-color: #000000; overflow: hidden; }
                                        .video-wrapper { position: relative; width: 100%; height: 100%; }
                                        iframe { position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: 0; }
                                    </style>
                                    <script>
                                        function unmuteVideo() {
                                            var iframe = document.getElementById('ytplayer');
                                            if (iframe && iframe.contentWindow) {
                                                iframe.contentWindow.postMessage('{"event":"command","func":"unMute","args":""}', '*');
                                            }
                                        }
                                        function muteVideo() {
                                            var iframe = document.getElementById('ytplayer');
                                            if (iframe && iframe.contentWindow) {
                                                iframe.contentWindow.postMessage('{"event":"command","func":"mute","args":""}', '*');
                                            }
                                        }
                                    </script>
                                </head>
                                <body>
                                    <div class="video-wrapper">
                                        <iframe
                                            id="ytplayer"
                                            type="text/html"
                                            src="https://www.youtube-nocookie.com/embed/$ytVideoId?autoplay=1&mute=1&controls=1&playsinline=1&enablejsapi=1&rel=0&iv_load_policy=3&modestbranding=1&origin=https://www.youtube-nocookie.com&widget_referrer=https://www.youtube-nocookie.com"
                                            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                                            allowfullscreen>
                                        </iframe>
                                    </div>
                                </body>
                                </html>
                            """.trimIndent()
                            loadDataWithBaseURL("https://www.youtube-nocookie.com", html, "text/html", "UTF-8", "https://www.youtube-nocookie.com")
                            webViewInstance = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // YouTube controls overlay (Unmute button + Fullscreen Expand)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Botón de Quitar Silencio / Silenciar
                    IconButton(
                        onClick = {
                            isYtMuted = !isYtMuted
                            val jsFunc = if (isYtMuted) "muteVideo()" else "unmuteVideo()"
                            webViewInstance?.evaluateJavascript(jsFunc, null)
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isYtMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = if (isYtMuted) "Quitar silencio" else "Silenciar",
                            tint = if (isYtMuted) currentPrimaryColor else HextechGold,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    if (!isFullscreen && onExpand != null) {
                        IconButton(
                            onClick = onExpand,
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Fullscreen,
                                contentDescription = "Pantalla Completa",
                                tint = currentPrimaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        } else if (isWebVideo || (isYt && ytVideoId == null)) {
            // Reproductor web embebido para servicios como Vimeo, Streamable o URLs web de video
            NoticeWebVideoPlayer(
                webUrl = normalizedUrl,
                isFullscreen = isFullscreen,
                onExpand = onExpand
            )
        } else if (isVideo) {
            // Reproductor universal de video (Local, Caché en disco, Base64 o Nube)
            LocalGalleryVideoPlayer(
                videoUriString = normalizedUrl,
                isFullscreen = isFullscreen,
                onExpand = onExpand
            )
        } else {
            // Image rendering (from Gallery, Base64 Data URL, or Image URL)
            val imageModel = remember(normalizedUrl) {
                if (normalizedUrl.startsWith("data:image/")) {
                    com.example.util.NoticeMediaStorageManager.decodeDataUriToBytes(normalizedUrl) ?: normalizedUrl
                } else if (normalizedUrl.startsWith("file://")) {
                    val path = Uri.parse(normalizedUrl).path ?: ""
                    java.io.File(path)
                } else {
                    normalizedUrl
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (onImageClick != null) {
                            Modifier.clickable { onImageClick() }
                        } else if (!isFullscreen && onExpand != null) {
                            Modifier.clickable { onExpand() }
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = "Multimedia de Anuncio",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(if (isFullscreen) 0.dp else 10.dp)),
                    contentScale = if (isFullscreen) ContentScale.Fit else ContentScale.Crop
                )

                if (!isFullscreen && onExpand != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .background(HextechSurface.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                            .border(0.8.dp, HextechCyan.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Fullscreen, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(12.dp))
                            Text("Ampliar", color = HextechCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoticeWebVideoPlayer(
    webUrl: String,
    isFullscreen: Boolean,
    onExpand: (() -> Unit)? = null
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isBuffering by remember { mutableStateOf(true) }

    DisposableEffect(lifecycleOwner, webUrl) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    try {
                        webViewInstance?.onResume()
                        webViewInstance?.resumeTimers()
                    } catch (_: Exception) {}
                }
                Lifecycle.Event.ON_PAUSE -> {
                    try {
                        webViewInstance?.onPause()
                        webViewInstance?.pauseTimers()
                    } catch (_: Exception) {}
                }
                Lifecycle.Event.ON_DESTROY -> {
                    try {
                        webViewInstance?.destroy()
                    } catch (_: Exception) {}
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        allowFileAccess = true
                        allowContentAccess = true
                        setSupportZoom(false)
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.6668.70 Mobile Safari/537.36"
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            if (newProgress >= 80) isBuffering = false
                        }
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isBuffering = false
                        }
                    }
                    loadUrl(webUrl)
                    webViewInstance = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isBuffering) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = HextechCyan,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (!isFullscreen && onExpand != null) {
            IconButton(
                onClick = onExpand,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(28.dp)
                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Fullscreen,
                    contentDescription = "Pantalla Completa",
                    tint = HextechCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun LocalGalleryVideoPlayer(
    videoUriString: String,
    isFullscreen: Boolean,
    onExpand: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isMuted by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var retryKey by remember { mutableStateOf(0) }
    var mediaPlayerRef by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
    val themePrimary = MaterialTheme.colorScheme.primary

    var currentPlayingTarget by remember { mutableStateOf<String?>(null) }
    var videoViewInstance by remember { mutableStateOf<android.widget.VideoView?>(null) }

    // Resolver ruta efectiva (Caché local en disco con OkHttp, Base64 decodificado, archivo local o streaming cloud)
    val effectiveUriString by produceState<String?>(initialValue = null, key1 = videoUriString, key2 = retryKey) {
        val trimmed = com.example.util.NoticeMediaStorageManager.normalizeVideoUrl(videoUriString.trim())
        if (trimmed.startsWith("data:video/")) {
            val f = com.example.util.NoticeMediaStorageManager.saveBase64VideoToCache(context, trimmed)
            value = f?.absolutePath ?: trimmed
        } else if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            val cached = com.example.util.NoticeMediaStorageManager.getCachedVideoFile(context, trimmed)
            if (cached != null && cached.exists() && cached.length() > 5000) {
                value = cached.absolutePath
            } else {
                value = trimmed
                // Descargar y cachear en segundo plano para reproducción instantánea y offline
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    val downloaded = com.example.util.NoticeMediaStorageManager.cacheVideoFromUrl(context, trimmed)
                    if (downloaded != null && downloaded.exists() && downloaded.length() > 5000) {
                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                            value = downloaded.absolutePath
                        }
                    }
                }
            }
        } else if (trimmed.startsWith("file://") || trimmed.startsWith("/")) {
            val path = if (trimmed.startsWith("file://")) Uri.parse(trimmed).path ?: "" else trimmed
            val f = File(path)
            if (f.exists()) {
                value = f.absolutePath
            } else {
                hasError = true
                isBuffering = false
                errorMessage = "El archivo de video local no se encuentra disponible."
                value = null
            }
        } else {
            value = trimmed
        }
    }

    val pauseTrigger by NoticeMediaUtils.pauseAndMuteTrigger.collectAsState()
    LaunchedEffect(pauseTrigger) {
        if (pauseTrigger > 0) {
            isPlaying = false
            isMuted = true
            try {
                mediaPlayerRef?.let { mp ->
                    mp.setVolume(0f, 0f)
                    if (mp.isPlaying) mp.pause()
                }
            } catch (_: Exception) {}
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    try {
                        if (mediaPlayerRef?.isPlaying == true) {
                            mediaPlayerRef?.pause()
                        }
                    } catch (_: Exception) {}
                }
                Lifecycle.Event.ON_RESUME -> {
                    try {
                        if (isPlaying && mediaPlayerRef?.isPlaying == false) {
                            mediaPlayerRef?.start()
                        }
                    } catch (_: Exception) {}
                }
                Lifecycle.Event.ON_DESTROY -> {
                    try {
                        mediaPlayerRef?.release()
                        mediaPlayerRef = null
                    } catch (_: Exception) {}
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                mediaPlayerRef?.release()
                mediaPlayerRef = null
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (effectiveUriString != null && !hasError) {
            AndroidView(
                factory = { ctx ->
                    val videoView = VideoView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                    videoViewInstance = videoView

                    val frameLayout = FrameLayout(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(android.graphics.Color.BLACK)
                        val params = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        ).apply {
                            gravity = android.view.Gravity.CENTER
                        }
                        addView(videoView, params)
                    }

                    val target = effectiveUriString!!
                    currentPlayingTarget = target
                    if (target.startsWith("/")) {
                        videoView.setVideoPath(target)
                    } else {
                        val headers = mapOf(
                            "User-Agent" to "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.6668.70 Mobile Safari/537.36",
                            "Accept" to "*/*"
                        )
                        videoView.setVideoURI(Uri.parse(target), headers)
                    }

                    videoView.setOnPreparedListener { mp ->
                        mediaPlayerRef = mp
                        try {
                            mp.isLooping = true
                            val vol = if (isMuted) 0f else 1f
                            mp.setVolume(vol, vol)
                            mp.start()
                        } catch (_: Exception) {}
                        try {
                            videoView.start()
                        } catch (_: Exception) {}
                        isPlaying = true
                        isBuffering = false
                        hasError = false
                    }

                    videoView.setOnCompletionListener {
                        try {
                            videoView.seekTo(0)
                            videoView.start()
                        } catch (_: Exception) {}
                    }

                    videoView.setOnInfoListener { _, what, _ ->
                        if (what == android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                            isBuffering = true
                        } else if (what == android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END ||
                                   what == android.media.MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                            isBuffering = false
                        }
                        true
                    }

                    videoView.setOnErrorListener { _, what, extra ->
                        android.util.Log.w("NoticeMediaViewer", "Error VideoView what=$what, extra=$extra para $target")
                        val trimmed = com.example.util.NoticeMediaStorageManager.normalizeVideoUrl(videoUriString.trim())
                        val cached = com.example.util.NoticeMediaStorageManager.getCachedVideoFile(ctx, trimmed)
                        if (cached != null && cached.exists() && cached.length() > 5000) {
                            try {
                                currentPlayingTarget = cached.absolutePath
                                videoView.setVideoPath(cached.absolutePath)
                                videoView.start()
                                hasError = false
                                isBuffering = false
                                return@setOnErrorListener true
                            } catch (_: Exception) {}
                        }

                        // Forzar descarga OkHttp en segundo plano de inmediato si aún no terminó
                        if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                val downloaded = com.example.util.NoticeMediaStorageManager.cacheVideoFromUrl(ctx, trimmed)
                                if (downloaded != null && downloaded.exists() && downloaded.length() > 5000) {
                                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        try {
                                            currentPlayingTarget = downloaded.absolutePath
                                            videoView.setVideoPath(downloaded.absolutePath)
                                            videoView.start()
                                            hasError = false
                                            isBuffering = false
                                        } catch (_: Exception) {}
                                    }
                                }
                            }
                        }

                        isBuffering = false
                        hasError = true
                        errorMessage = "Cargando video..."
                        true
                    }

                    frameLayout
                },
                update = {
                    try {
                        val target = effectiveUriString
                        if (target != null && target != currentPlayingTarget) {
                            currentPlayingTarget = target
                            isBuffering = true
                            if (target.startsWith("/")) {
                                videoViewInstance?.setVideoPath(target)
                            } else {
                                val headers = mapOf(
                                    "User-Agent" to "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.6668.70 Mobile Safari/537.36",
                                    "Accept" to "*/*"
                                )
                                videoViewInstance?.setVideoURI(Uri.parse(target), headers)
                            }
                        }

                        val vol = if (isMuted) 0f else 1f
                        mediaPlayerRef?.setVolume(vol, vol)

                        if (isPlaying) {
                            if (mediaPlayerRef?.isPlaying == false) {
                                try { mediaPlayerRef?.start() } catch (_: Exception) {}
                            }
                            if (videoViewInstance?.isPlaying == false) {
                                try { videoViewInstance?.start() } catch (_: Exception) {}
                            }
                        } else {
                            if (mediaPlayerRef?.isPlaying == true) {
                                try { mediaPlayerRef?.pause() } catch (_: Exception) {}
                            }
                            if (videoViewInstance?.isPlaying == true) {
                                try { videoViewInstance?.pause() } catch (_: Exception) {}
                            }
                        }
                    } catch (_: Exception) {}
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Indicador de carga Hextech elegante (evita pantalla negra estática)
        if (isBuffering && !hasError) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
            ) {
                CircularProgressIndicator(
                    color = HextechCyan,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Cargando video...",
                    color = HextechCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Mensaje de error visual con botón reintentar
        if (hasError) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VideocamOff,
                    contentDescription = null,
                    tint = Color(0xFFFF6B6B),
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = errorMessage ?: "No se pudo reproducir el video",
                    color = Color(0xFFE0E0E0),
                    fontSize = 11.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        hasError = false
                        isBuffering = true
                        retryKey++
                        // Forzar descarga OkHttp en segundo plano de inmediato al reintentar
                        val trimmed = com.example.util.NoticeMediaStorageManager.normalizeVideoUrl(videoUriString.trim())
                        if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                com.example.util.NoticeMediaStorageManager.cacheVideoFromUrl(context, trimmed)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HextechSurfaceVariant),
                    border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text("Reintentar", color = HextechCyan, fontSize = 10.5.sp)
                }
            }
        }

        // Overlay Controls: Unmute / Mute + Fullscreen Expand ONLY
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Botón de Play / Pausa
            IconButton(
                onClick = {
                    isPlaying = !isPlaying
                },
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                    tint = themePrimary,
                    modifier = Modifier.size(15.dp)
                )
            }

            // Botón de Quitar Silencio / Silenciar
            IconButton(
                onClick = {
                    isMuted = !isMuted
                    mediaPlayerRef?.let { mp ->
                        try {
                            val vol = if (isMuted) 0f else 1f
                            mp.setVolume(vol, vol)
                        } catch (_: Exception) {}
                    }
                },
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = if (isMuted) "Quitar silencio" else "Silenciar",
                    tint = if (isMuted) themePrimary else HextechGold,
                    modifier = Modifier.size(15.dp)
                )
            }

            if (!isFullscreen && onExpand != null) {
                IconButton(
                    onClick = onExpand,
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Fullscreen,
                        contentDescription = "Pantalla Completa",
                        tint = themePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NoticeMediaFullscreenDialog(
    mediaUrl: String,
    externalUrl: String = "",
    noticeId: String = "",
    isVertical: Boolean = false,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val trimmedUrl = mediaUrl.trim()
    val isVideo = remember(trimmedUrl) { NoticeMediaUtils.isVideo(context, trimmedUrl) }
    val isVerticalMedia = remember(trimmedUrl, isVertical) {
        isVertical || NoticeMediaUtils.isMediaVertical(context, trimmedUrl)
    }

    // Control de rotación de pantalla
    var isLandscape by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Dialog(
        onDismissRequest = {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val dialogWindow = (androidx.compose.ui.platform.LocalView.current.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
        SideEffect {
            dialogWindow?.let { win ->
                win.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                win.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.BLACK))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val openLinkAction: () -> Unit = {
                if (externalUrl.isNotBlank()) {
                    try {
                        if (noticeId.isNotBlank()) {
                            com.example.data.AppNoticeAnalyticsManager.recordClick(context, noticeId)
                        }
                        val cleanUrl = if (!externalUrl.startsWith("http://") && !externalUrl.startsWith("https://")) {
                            "https://${externalUrl.trim()}"
                        } else {
                            externalUrl.trim()
                        }
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
            }

            // 1. Contenedor del reproductor / medio a PANTALLA COMPLETA 100% (sin márgenes ni padding que compriman el video)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (!isVideo && externalUrl.isNotBlank()) {
                            Modifier.clickable { openLinkAction() }
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                NoticeMediaViewer(
                    mediaUrl = mediaUrl,
                    modifier = Modifier.fillMaxSize(),
                    isFullscreen = true,
                    onImageClick = if (!isVideo && externalUrl.isNotBlank()) openLinkAction else null
                )
            }

            // 2. Botón flotante superior derecho de Cerrar (elegante Hextech con fondo translúcido)
            IconButton(
                onClick = {
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    onDismiss()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 12.dp, end = 12.dp)
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // 3. Botón flotante inferior izquierdo de rotación (Modo Horizontal / Vertical)
            // Solo se muestra si el video es horizontal. Si es un video vertical, se oculta la opción de rotación.
            if (isVideo && !isVerticalMedia) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .navigationBarsPadding()
                        .padding(start = 14.dp, bottom = 14.dp)
                ) {
                    Button(
                        onClick = {
                            val nextLandscape = !isLandscape
                            isLandscape = nextLandscape
                            activity?.requestedOrientation = if (nextLandscape) {
                                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            } else {
                                ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HextechDarkBg.copy(alpha = 0.85f)),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.7f)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            if (isLandscape) Icons.Default.ScreenLockPortrait else Icons.Default.ScreenRotation,
                            contentDescription = "Rotar Pantalla",
                            tint = HextechCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isLandscape) "Modo Vertical" else "Pantalla Completa",
                            color = HextechCyan,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 4. Botón flotante inferior derecho de Cerrar (Solo horizontales)
            if (!isVerticalMedia) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(end = 14.dp, bottom = 14.dp)
                ) {
                    Button(
                        onClick = {
                            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HextechDarkBg.copy(alpha = 0.85f)),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Cerrar", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = TextPrimary)
                    }
                }
            }

            // 5. Enlace interactivo flotante si es imagen con enlace externo (Solo horizontales)
            if (!isVerticalMedia && !isVideo && externalUrl.isNotBlank()) {
                val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "PulseAnim"
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 14.dp)
                        .scale(pulseScale)
                        .background(HextechDarkBg.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                        .border(1.dp, HextechGold.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
                        .clickable { openLinkAction() }
                        .padding(horizontal = 16.dp, vertical = 7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TouchApp, contentDescription = null, tint = HextechGold, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(tr("Toca para abrir enlace"), color = HextechGold, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 6. Para anuncios verticales, quitar botones de abajo y mostrar botón Abrir enlace
            if (isVerticalMedia && externalUrl.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 24.dp)
                ) {
                    Button(
                        onClick = { openLinkAction() },
                        colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Abrir enlace",
                            color = HextechDarkBg,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

