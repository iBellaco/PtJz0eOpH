package com.example.data.supabase

import android.content.Context
import coil.request.CachePolicy
import coil.request.ImageRequest

/**
 * Repositorio y resolutor para almacenar y obtener imágenes del juego desde la nube (Supabase Storage).
 * 
 * NOTA DE SEGURIDAD Y CONFIGURACIÓN:
 * Excluye explícitamente los avatares e imágenes de marcos del panel de usuario, los cuales
 * se mantienen en el almacenamiento local de la aplicación.
 */
object CloudAssetRepository {

    const val BUCKET_NAME = "wildrift_assets"

    /**
     * Identifica si un recurso es un avatar o marco del panel de usuario que debe conservarse localmente.
     */
    fun isExcludedFromCloud(url: String): Boolean {
        if (url.isBlank()) return true
        val lower = url.lowercase().trim()
        return lower.contains("frame_") ||
               lower.contains("avatar_") ||
               lower.contains("user_avatar") ||
               lower.contains("default_poro")
    }

    /**
     * Construye la URL pública en la nube para cualquier recurso del juego.
     * Retorna la URL original si el recurso está excluido (avatares/marcos de usuario) o si no hay URL activa.
     */
    fun getCloudUrl(originalUrl: String): String {
        if (originalUrl.isBlank()) return originalUrl
        if (isExcludedFromCloud(originalUrl)) return originalUrl

        val baseUrl = SupabaseClientManager.getActiveUrl().removeSuffix("/")
        if (baseUrl.isBlank()) return originalUrl

        val fileName = when {
            originalUrl.startsWith("file:///android_asset/offline_images/") -> {
                originalUrl.removePrefix("file:///android_asset/offline_images/")
            }
            originalUrl.startsWith("file:///android_asset/champions/") -> {
                "champions_" + originalUrl.removePrefix("file:///android_asset/champions/")
            }
            originalUrl.contains("/") -> {
                originalUrl.substringAfterLast("/")
            }
            else -> originalUrl
        }

        if (fileName.isBlank()) return originalUrl

        return "$baseUrl/storage/v1/object/public/$BUCKET_NAME/$fileName"
    }

    /**
     * Prepara una petición de imagen Coil con configuración optimizada.
     * Intenta cargar la imagen desde la nube de almacenamiento seguro.
     */
    fun buildImageRequest(context: Context, originalUrl: String): ImageRequest {
        val cloudUrl = getCloudUrl(originalUrl)
        return ImageRequest.Builder(context)
            .data(cloudUrl)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }
}
