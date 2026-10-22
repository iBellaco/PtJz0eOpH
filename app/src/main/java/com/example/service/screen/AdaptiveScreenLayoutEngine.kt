package com.example.service.screen

import android.graphics.Rect
import kotlin.math.max
import kotlin.math.min

/**
 * Motor de Diseño y Calibración Geométrica Adaptativa Multipantalla para Wild Rift.
 * 
 * Garantiza que el escaneo visual y OCR funcione de forma precisa en cualquier dispositivo móvil,
 * independientemente de la resolución (HD, FHD, 2K, QHD) o relación de aspecto:
 * - Estándar 16:9 (1.777)
 * - Panorámicas 18:9 (2.000), 19.5:9 (2.167), 20:9 (2.222), 21:9 (2.333)
 * - Tablets y Plegables 4:3 (1.333), 16:10 (1.600)
 */
object AdaptiveScreenLayoutEngine {
    /** The name band belongs to one row and stops before its portrait. */
    fun calculateSlotNameRect(width: Int, height: Int, isAlly: Boolean, slot: Int,
        config: VisionCalibrationConfig): Rect {
        val center = height * (if (isAlly) config.allySlotYRatios else config.enemySlotYRatios)[slot]
        val avatarX = if (isAlly) config.getAllySlotX(slot) else config.getEnemySlotX(slot)
        val radius = height * config.getSlotDiameter(isAlly, slot) / 2f
        val bandWidth = height * .31f
        val avatarEdge = width * avatarX + if (isAlly) radius else -radius
        val left = (if (isAlly) avatarEdge else avatarEdge - bandWidth).toInt().coerceIn(0, width - 1)
        val right = (if (isAlly) avatarEdge + bandWidth else avatarEdge).toInt().coerceIn(left + 1, width)
        val top = (center - height * 0.05f).toInt().coerceIn(0, height - 1)
        // The ally's next line is the summoner name, which can itself contain a
        // champion name. Read only the title that switches from lane to champion.
        val bottom = (center + height * 0.005f).toInt().coerceIn(top + 1, height)
        return Rect(left, top, right, bottom)
    }

    // Relación de aspecto canónica de referencia para la UI de Wild Rift (16:9)
    private const val BASE_ASPECT_RATIO = 16f / 9f // ~1.7778f

    data class ScreenGeometry(
        val width: Int,
        val height: Int,
        val aspectRatio: Float,
        val isUltrawide: Boolean,
        val isTabletOrFoldable: Boolean,
        val safeHorizontalInsetRatio: Float
    )

    /**
     * Analiza las dimensiones de pantalla actuales y calcula las métricas de geometría.
     */
    fun analyzeScreen(width: Int, height: Int): ScreenGeometry {
        val w = max(width, height).toFloat()
        val h = min(width, height).toFloat().coerceAtLeast(1f)
        val ratio = w / h

        val isUltrawide = ratio > 1.85f
        val isTablet = ratio < 1.65f

        // Margen seguro adaptativo para evitar cámaras/cutouts en pantallas panorámicas
        val insetRatio = when {
            ratio >= 2.2f -> 0.045f // 20:9 o 21:9
            ratio >= 2.0f -> 0.035f // 18:9 o 19.5:9
            ratio >= 1.75f -> 0.020f // 16:9
            else -> 0.010f           // Tablets / 4:3
        }

        return ScreenGeometry(
            width = w.toInt(),
            height = h.toInt(),
            aspectRatio = ratio,
            isUltrawide = isUltrawide,
            isTabletOrFoldable = isTablet,
            safeHorizontalInsetRatio = insetRatio
        )
    }

    /**
     * Genera una configuración de calibración adaptada dinámicamente a la resolución y aspect ratio
     * del frame actual.
     */
    fun computeAdaptiveConfig(width: Int, height: Int, baseConfig: VisionCalibrationConfig = VisionCalibrationConfig()): VisionCalibrationConfig {
        if (width <= 0 || height <= 0) return baseConfig

        // Las coordenadas X ya están normalizadas respecto al frame capturado. No deben
        // escalarse ni limitarse a los valores predeterminados: eso anulaba la calibración
        // manual (por ejemplo, 15.7% terminaba escaneándose en 11%) y variaba entre formatos.
        // El recorte final se limita a los bordes físicos en calculateSlotCropRect.
        val adaptiveAllyCenterX = baseConfig.allyAvatarCenterX.coerceIn(0.005f, 0.495f)
        val adaptiveEnemyCenterX = baseConfig.enemyAvatarCenterX.coerceIn(0.505f, 0.995f)
        val adaptiveAllySlotXRatios = baseConfig.allySlotXRatios.map { it.coerceIn(0.005f, 0.495f) }
        val adaptiveEnemySlotXRatios = baseConfig.enemySlotXRatios.map { it.coerceIn(0.505f, 0.995f) }

        // Rango de búsqueda OCR adaptativo:
        // Dimensiones perfectamente simétricas e idénticas en anchura horizontal para ambos bandos (Aliado y Rival)
        // Evita que el lado aliado se extienda demasiado y evita que el lado rival corte letras finales o colisione con el carrusel.
        val ocrBoxWidth = 0.185f
        val allyOcrMinX = (adaptiveAllyCenterX - 0.024f).coerceIn(0.005f, 0.795f)
        val allyOcrMaxX = allyOcrMinX + ocrBoxWidth

        val enemyOcrMaxX = (adaptiveEnemyCenterX + 0.024f).coerceIn(0.205f, 0.995f)
        val enemyOcrMinX = enemyOcrMaxX - ocrBoxWidth

        // Header calibration is frame-relative too. Never overwrite measured
        // positions using a guessed device inset; live header detection owns alignment.

        return baseConfig.copy(
            allyAvatarCenterX = adaptiveAllyCenterX,
            enemyAvatarCenterX = adaptiveEnemyCenterX,
            avatarDiameterRatio = baseConfig.avatarDiameterRatio,
            allySlotXRatios = adaptiveAllySlotXRatios,
            enemySlotXRatios = adaptiveEnemySlotXRatios,
            allyOcrMinX = allyOcrMinX,
            allyOcrMaxX = allyOcrMaxX,
            enemyOcrMinX = enemyOcrMinX,
            enemyOcrMaxX = enemyOcrMaxX
        )
    }

    /**
     * Calcula una caja de recorte segura en coordenadas de píxeles para cualquier slot o región de interés.
     */
    fun calculateSlotCropRect(
        width: Int,
        height: Int,
        isAlly: Boolean,
        slotIndex: Int,
        config: VisionCalibrationConfig
    ): Rect {
        val sIdx = slotIndex.coerceIn(0, 4)
        val cx = if (isAlly) (width * config.getAllySlotX(sIdx)).toInt() else (width * config.getEnemySlotX(sIdx)).toInt()
        val yRatios = if (isAlly) config.allySlotYRatios else config.enemySlotYRatios
        val cy = (height * yRatios.getOrElse(sIdx) { 0.2f + sIdx * 0.13f }).toInt()
        val diam = (height * config.getSlotDiameter(isAlly, sIdx)).toInt().coerceAtLeast(32)
        val radius = diam / 2

        val left = (cx - radius).coerceIn(0, (width - diam).coerceAtLeast(0))
        val top = (cy - radius).coerceIn(0, (height - diam).coerceAtLeast(0))
        return Rect(left, top, min(width, left + diam), min(height, top + diam))
    }

    /** Extract exactly the region published to the viewer; alignment happens once upstream. */
    fun extractSlotAvatarBitmap(
        sourceBitmap: android.graphics.Bitmap,
        width: Int,
        height: Int,
        isAlly: Boolean,
        slotIndex: Int,
        config: VisionCalibrationConfig
    ): android.graphics.Bitmap? {
        val rect = calculateSlotCropRect(width, height, isAlly, slotIndex, config)
        return try {
            android.graphics.Bitmap.createBitmap(sourceBitmap, rect.left, rect.top, rect.width(), rect.height())
        } catch (_: IllegalArgumentException) { null }
    }
}
