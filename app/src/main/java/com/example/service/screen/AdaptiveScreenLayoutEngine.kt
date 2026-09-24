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

        val geometry = analyzeScreen(width, height)
        val ratio = geometry.aspectRatio

        // En Wild Rift, las columnas verticales de avatares están fijadas en los extremos de la pantalla:
        // - Columna aliada (izquierda): avatar circular centrado en x ≈ 0.072f (junto a hechizos de invocador)
        // - Columna rival (derecha): avatar circular centrado en x ≈ 0.959f (al extremo derecho del slot rival)
        // En tablets/plegables (ratio < 1.65f), la pantalla es más estrecha respecto al alto; se escala suavemente hacia adentro
        // para garantizar que los avatares y el texto OCR no colisionen con los bordes físicos.
        val tabletScale = if (geometry.isTabletOrFoldable) (BASE_ASPECT_RATIO / ratio).coerceIn(1.0f, 1.25f) else 1.0f
        val adaptiveAllyCenterX = (baseConfig.allyAvatarCenterX * tabletScale).coerceIn(0.065f, 0.110f)
        val adaptiveEnemyCenterX = (1.0f - (1.0f - baseConfig.enemyAvatarCenterX) * tabletScale).coerceIn(0.890f, 0.965f)

        val adaptiveAllySlotXRatios = baseConfig.allySlotXRatios.map { 
            (it * tabletScale).coerceIn(0.065f, 0.110f) 
        }
        val adaptiveEnemySlotXRatios = baseConfig.enemySlotXRatios.map { 
            (1.0f - (1.0f - it) * tabletScale).coerceIn(0.890f, 0.965f) 
        }

        // Rango de búsqueda OCR adaptativo:
        // El texto del slot aliado está a la derecha del avatar.
        // Captura tanto nombres cortos ("MID", "APOYO") como nombres largos ("CALLE DEL BARÓN", "CALLE DEL DRAGÓN").
        // JAMÁS debe invadir el carrusel central de selección de campeones (x >= 0.285 en móviles, x >= 0.330 en tablets).
        val allyOcrMinX = (adaptiveAllyCenterX - 0.022f).coerceAtLeast(0.035f)
        val allyOcrMaxX = (adaptiveAllyCenterX + 0.210f).coerceAtMost(0.315f)

        // El texto del slot rival está inmediatamente a la izquierda del avatar rival (acotado para no invadir el carrusel central)
        val enemyOcrMinX = (adaptiveEnemyCenterX - 0.155f).coerceIn(0.790f, 0.825f)
        val enemyOcrMaxX = (adaptiveEnemyCenterX - 0.034f).coerceIn(0.905f, 0.935f)

        // Ajuste de las posiciones horizontales de la barra superior (los 10 avatares de la cabecera)
        // En tablets los avatares superiores están ligeramente más comprimidos hacia el centro; en ultrawide hacia los bordes.
        val topAllySpacing = 0.035f * (BASE_ASPECT_RATIO / ratio)
        val topEnemySpacing = 0.035f * (BASE_ASPECT_RATIO / ratio)

        val topAllyStart = (0.025f + geometry.safeHorizontalInsetRatio * 0.5f).coerceIn(0.015f, 0.08f)
        val topEnemyEnd = (0.975f - geometry.safeHorizontalInsetRatio * 0.5f).coerceIn(0.92f, 0.985f)

        val adaptiveTopAllyXRatios = List(5) { i ->
            topAllyStart + i * topAllySpacing
        }

        val adaptiveTopEnemyXRatios = List(5) { i ->
            (topEnemyEnd - (4 - i) * topEnemySpacing).coerceIn(0.70f, 0.99f)
        }

        return baseConfig.copy(
            allyAvatarCenterX = adaptiveAllyCenterX,
            enemyAvatarCenterX = adaptiveEnemyCenterX,
            avatarDiameterRatio = baseConfig.avatarDiameterRatio,
            allySlotXRatios = adaptiveAllySlotXRatios,
            enemySlotXRatios = adaptiveEnemySlotXRatios,
            allyOcrMinX = allyOcrMinX,
            allyOcrMaxX = allyOcrMaxX,
            enemyOcrMinX = enemyOcrMinX,
            enemyOcrMaxX = enemyOcrMaxX,
            topAllyXRatios = adaptiveTopAllyXRatios,
            topEnemyXRatios = adaptiveTopEnemyXRatios,
            topAlly5XRatio = adaptiveTopAllyXRatios.lastOrNull() ?: 0.168f,
            topEnemy5XRatio = adaptiveTopEnemyXRatios.lastOrNull() ?: 0.974f
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

    /**
     * Extrae un recorte perfectamente centrado y completo del círculo del avatar del slot.
     * Incorpora auto-calibración de bordes por detección de aro circular:
     * - En el rival: aro carmesí/rojo
     * - En el aliado: aro azul/cian
     * Si detecta el contorno del aro, calcula la media de los extremos izquierdo y derecho para
     * mantener simetría geométrica perfecta sin sesgos hacia arcos no tapados.
     */
    fun extractSlotAvatarBitmap(
        sourceBitmap: android.graphics.Bitmap,
        width: Int,
        height: Int,
        isAlly: Boolean,
        slotIndex: Int,
        config: VisionCalibrationConfig
    ): android.graphics.Bitmap? {
        val sIdx = slotIndex.coerceIn(0, 4)
        val cxNominal = if (isAlly) (width * config.getAllySlotX(sIdx)).toInt() else (width * config.getEnemySlotX(sIdx)).toInt()
        val yRatios = if (isAlly) config.allySlotYRatios else config.enemySlotYRatios
        val cyNominal = (height * yRatios.getOrElse(sIdx) { 0.2f + sIdx * 0.13f }).toInt()

        val targetDiam = (height * config.getSlotDiameter(isAlly, sIdx)).toInt().coerceAtLeast(32)
        val radius = targetDiam / 2

        // Ventana de búsqueda adaptativa alrededor de la posición nominal para soporte multidispositivo
        val margin = (targetDiam * 0.20f).toInt()
        val searchLeft = (cxNominal - radius - margin).coerceIn(0, width - 1)
        val searchRight = (cxNominal + radius + margin).coerceIn(0, width)
        val searchTop = (cyNominal - radius - margin).coerceIn(0, height - 1)
        val searchBottom = (cyNominal + radius + margin).coerceIn(0, height)

        if (searchRight <= searchLeft + 16 || searchBottom <= searchTop + 16) {
            val baseRect = calculateSlotCropRect(width, height, isAlly, slotIndex, config)
            return try {
                android.graphics.Bitmap.createBitmap(sourceBitmap, baseRect.left, baseRect.top, baseRect.width(), baseRect.height())
            } catch (_: Throwable) { null }
        }

        var minRingX = Int.MAX_VALUE
        var maxRingX = Int.MIN_VALUE
        var minRingY = Int.MAX_VALUE
        var maxRingY = Int.MIN_VALUE
        var ringPixelCount = 0

        val step = 2
        val rMinExpectedSq = (radius * 0.72f) * (radius * 0.72f)
        val rMaxExpectedSq = (radius * 1.25f) * (radius * 1.25f)

        for (y in searchTop until searchBottom step step) {
            val dy = y - cyNominal
            for (x in searchLeft until searchRight step step) {
                val dx = x - cxNominal
                val distFromNominalSq = (dx * dx + dy * dy).toFloat()

                // Filtrar píxeles que pertenezcan estrictamente al anillo periférico (evita falsos positivos internos)
                if (distFromNominalSq < rMinExpectedSq || distFromNominalSq > rMaxExpectedSq) continue

                val px = sourceBitmap.getPixel(x, y)
                val r = (px shr 16) and 0xFF
                val g = (px shr 8) and 0xFF
                val b = px and 0xFF

                // En Wild Rift:
                // Aliado: borde azul eléctrico / cian (b alto con r bajo)
                // Rival: borde rojo escarlata / carmesí (r alto con g y b bajos)
                // Píxeles blancos/brillantes de campeones como Volibear (r,g,b altos) quedan descartados
                val isRing = if (isAlly) {
                    b > 115 && b > (r * 1.45f) && (g > 65 || b > 145) && (r + g + b) < 580
                } else {
                    r > 115 && r > (g * 1.40f) && r > (b * 1.40f) && (r + g + b) < 580
                }

                if (isRing) {
                    if (x < minRingX) minRingX = x
                    if (x > maxRingX) maxRingX = x
                    if (y < minRingY) minRingY = y
                    if (y > maxRingY) maxRingY = y
                    ringPixelCount++
                }
            }
        }

        val expectedMinDiam = (targetDiam * 0.75f).toInt()
        val expectedMaxDiam = (targetDiam * 1.25f).toInt()

        // Restricción de desviación máxima respecto al centro nominal (máximo 18% para multidispositivo)
        val maxDevX = (targetDiam * 0.18f).toInt()
        val maxDevY = (targetDiam * 0.18f).toInt()

        val actualCx = if (ringPixelCount >= 18 && (maxRingX - minRingX) in expectedMinDiam..expectedMaxDiam && kotlin.math.abs(((minRingX + maxRingX) / 2) - cxNominal) <= maxDevX) {
            ((minRingX + maxRingX) / 2).coerceIn(radius, width - radius)
        } else {
            cxNominal.coerceIn(radius, width - radius)
        }

        val actualCy = if (ringPixelCount >= 18 && (maxRingY - minRingY) in expectedMinDiam..expectedMaxDiam && kotlin.math.abs(((minRingY + maxRingY) / 2) - cyNominal) <= maxDevY) {
            ((minRingY + maxRingY) / 2).coerceIn(radius, height - radius)
        } else {
            cyNominal.coerceIn(radius, height - radius)
        }

        val cropLeft = (actualCx - radius).coerceIn(0, (width - targetDiam).coerceAtLeast(0))
        val cropTop = (actualCy - radius).coerceIn(0, (height - targetDiam).coerceAtLeast(0))
        val finalW = min(targetDiam, width - cropLeft)
        val finalH = min(targetDiam, height - cropTop)

        return try {
            android.graphics.Bitmap.createBitmap(sourceBitmap, cropLeft, cropTop, finalW, finalH)
        } catch (_: Throwable) {
            val fallbackRect = calculateSlotCropRect(width, height, isAlly, slotIndex, config)
            try {
                android.graphics.Bitmap.createBitmap(sourceBitmap, fallbackRect.left, fallbackRect.top, fallbackRect.width(), fallbackRect.height())
            } catch (_: Throwable) { null }
        }
    }
}
