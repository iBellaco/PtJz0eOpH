package com.example.data.supabase

import android.content.Context
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.download.GameAssetDownloadManager

/**
 * Repositorio de recursos visuales y resolución dinámica de imágenes.
 * 
 * Los avatares de usuario y marcos de perfil se mantienen de forma local en la aplicación.
 * Las imágenes del juego (habilidades, campeones, objetos, runas) se resuelven a través del gestor de descargas.
 */
object CloudAssetRepository {

    fun isExcludedFromCloud(url: String): Boolean {
        if (url.isBlank()) return true
        val lower = url.lowercase().trim()
        return lower.contains("frame_") ||
               lower.contains("avatar_") ||
               lower.contains("user_avatar") ||
               lower.contains("default_poro")
    }

    /**
     * Resuelve la ubicación óptima de la imagen:
     * 1. Si es avatar/marco de usuario -> local directo
     * 2. Si ya fue descargada por el gestor de descargas -> archivo interno de alta velocidad
     * 3. Si aún no está descargada -> URL original con política de caché
     */
    fun resolveImageUrl(context: Context, originalUrl: String): String {
        if (isExcludedFromCloud(originalUrl)) {
            return originalUrl
        }
        val downloadedFile = GameAssetDownloadManager.getDownloadedFile(context, originalUrl)
        if (downloadedFile != null) {
            return "file://${downloadedFile.absolutePath}"
        }
        return originalUrl
    }

    fun buildImageRequest(context: Context, originalUrl: String): ImageRequest {
        val resolved = resolveImageUrl(context, originalUrl)
        return ImageRequest.Builder(context)
            .data(resolved)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }
}
