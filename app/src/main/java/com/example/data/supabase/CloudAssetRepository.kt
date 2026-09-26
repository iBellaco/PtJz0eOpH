package com.example.data.supabase

import android.content.Context
import coil.request.CachePolicy
import coil.request.ImageRequest

/**
 * Repositorio de recursos visuales y resolución de imágenes.
 * 
 * Los avatares de usuario y marcos de perfil se mantienen de forma local en la aplicación.
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

    fun getCloudUrl(originalUrl: String): String {
        return originalUrl
    }

    fun buildImageRequest(context: Context, originalUrl: String): ImageRequest {
        return ImageRequest.Builder(context)
            .data(originalUrl)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }
}
