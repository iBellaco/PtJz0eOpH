package com.example.service.screen

import java.util.Locale

/**
 * Mediciones aportadas desde dispositivos físicos.
 *
 * Solo se aplican cuando coinciden fabricante, modelo y dimensiones de captura.
 * Una calibración guardada en el dispositivo siempre tiene prioridad y una
 * resolución coincidente, por sí sola, nunca selecciona el perfil de otro móvil.
 */
internal object MeasuredDeviceCalibrationDefaults {
    private data class Profile(
        val manufacturer: String,
        val model: String,
        val landscapeWidth: Int,
        val landscapeHeight: Int,
        val config: VisionCalibrationConfig
    )

    private fun measured(
        avatarDiameter: Float,
        allyCenter: Float,
        enemyCenter: Float,
        allyXs: List<Float>,
        allyYs: List<Float>,
        allyDiameters: List<Float>,
        enemyXs: List<Float>,
        enemyYs: List<Float>,
        enemyDiameters: List<Float>,
        allyBanXs: List<Float>,
        allyBanYs: List<Float>,
        allyBanDiameters: List<Float>,
        allyBansManual: Boolean,
        enemyBanXs: List<Float>,
        enemyBanYs: List<Float>,
        enemyBanDiameters: List<Float>,
        enemyBansManual: Boolean,
        topY: Float,
        topDiameter: Float,
        topAlly5X: Float,
        topEnemy5X: Float
    ): VisionCalibrationConfig {
        val shared = VisionCalibrationConfig()
        return shared.copy(
            allyAvatarCenterX = allyCenter,
            enemyAvatarCenterX = enemyCenter,
            avatarDiameterRatio = avatarDiameter,
            allySlotXRatios = allyXs,
            allySlotYRatios = allyYs,
            allySlotDiameterRatios = allyDiameters,
            enemySlotXRatios = enemyXs,
            enemySlotYRatios = enemyYs,
            enemySlotDiameterRatios = enemyDiameters,
            allyBanXRatios = allyBanXs,
            allyBanYRatios = allyBanYs,
            allyBanDiameterRatios = allyBanDiameters,
            manualAllyBans = allyBansManual,
            enemyBanXRatios = enemyBanXs,
            enemyBanYRatios = enemyBanYs,
            enemyBanDiameterRatios = enemyBanDiameters,
            manualEnemyBans = enemyBansManual,
            topAvatarYRatio = topY,
            topAvatarDiameterRatio = topDiameter,
            topAlly5XRatio = topAlly5X,
            topEnemy5XRatio = topEnemy5X,
            topAllyXRatios = shared.topAllyXRatios.toMutableList().apply { this[4] = topAlly5X },
            topEnemyXRatios = shared.topEnemyXRatios.toMutableList().apply { this[4] = topEnemy5X }
        )
    }

    /*
     * The previous measured catalog was intentionally removed at the owner's
     * request after the ban overlay proved unreliable on several devices.
     * New measurements are accepted one device/capture at a time so an old
     * geometry cannot silently become the fallback for another phone.
     */
    private val kenshinConfig = measured(
            .11f, .107000016f, .919f,
            List(5) { .107000016f }, listOf(.188f, .324f, .458f, .591f, .725f),
            listOf(.11f, .11f, .11f, .11f, .105f),
            List(5) { .919f }, listOf(.188f, .324f, .457f, .59f, .722f), List(5) { .11f },
            listOf(.028f, .064f, .1f, .136f, .172f), List(5) { .052f }, List(5) { .06f }, false,
            listOf(.826f, .863f, .9f, .937f, .974f), List(5) { .052f }, List(5) { .06f }, false,
            .045f, .072f, .168f, .974f
        )

    private val profiles = listOf(
        Profile("motorola", "motorola edge 30 neo", 2174, 1080, kenshinConfig),
        // ScreenCaptureManager limits wide captures to 1920 px. This is the
        // same phone and the same normalized measurement, expressed at the
        // exact 1920x954 surface that Android produces from 2174x1080.
        Profile("motorola", "motorola edge 30 neo", 1920, 954, kenshinConfig)
    )

    fun forCapture(
        manufacturer: String,
        model: String,
        width: Int,
        height: Int
    ): VisionCalibrationConfig? {
        if (width <= 0 || height <= 0) return null
        val normalizedManufacturer = manufacturer.trim().lowercase(Locale.ROOT)
        val normalizedModel = model.trim().lowercase(Locale.ROOT)
        val landscapeWidth = maxOf(width, height)
        val landscapeHeight = minOf(width, height)
        return profiles.firstOrNull {
            it.manufacturer.lowercase(Locale.ROOT) == normalizedManufacturer &&
                it.model.lowercase(Locale.ROOT) == normalizedModel &&
                it.landscapeWidth == landscapeWidth && it.landscapeHeight == landscapeHeight
        }?.config
    }
}
