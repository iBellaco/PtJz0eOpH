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

    /**
     * Exact physical reference supplied by the owner. The second entry is the
     * 1920x869 surface produced by MediaProjection on the same device; it is
     * an alias of the same normalized geometry, not a profile shared by
     * resolution with another model.
     */
    private val nubiaNx789jReference = VisionCalibrationConfig().copy(
        allyAvatarCenterX = .072f,
        enemyAvatarCenterX = .959f,
        avatarDiameterRatio = .11f,
        allySlotXRatios = List(5) { .072f },
        allySlotYRatios = listOf(.188f, .324f, .458f, .591f, .725f),
        allySlotDiameterRatios = listOf(.11f, .11f, .11f, .11f, .105f),
        enemySlotXRatios = List(5) { .959f },
        enemySlotYRatios = listOf(.188f, .324f, .457f, .590f, .722f),
        enemySlotDiameterRatios = List(5) { .11f },
        allyBanXRatios = listOf(.028f, .064f, .100f, .136f, .172f),
        allyBanYRatios = List(5) { .052f },
        allyBanDiameterRatios = List(5) { .06f },
        manualAllyBans = false,
        enemyBanXRatios = listOf(.826f, .863f, .900f, .937f, .974f),
        enemyBanYRatios = List(5) { .052f },
        enemyBanDiameterRatios = List(5) { .06f },
        manualEnemyBans = false,
        topAvatarYRatio = .045f,
        topAvatarDiameterRatio = .072f,
        topAlly5XRatio = .168f,
        topEnemy5XRatio = .974f,
        topAllyXRatios = listOf(.028f, .063f, .098f, .133f, .168f),
        topEnemyXRatios = listOf(.834f, .869f, .904f, .939f, .974f)
    )

    private val profiles: List<Profile> = listOf(
        Profile("nubia", "NX789J", 2688, 1216, nubiaNx789jReference),
        Profile("nubia", "NX789J", 1920, 869, nubiaNx789jReference)
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
