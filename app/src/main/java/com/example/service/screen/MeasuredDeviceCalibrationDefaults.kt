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

    /**
     * Vision Draft calibration supplied from the Xiaomi 24122RKC7G capture.
     *
     * The ban circles are intentionally manual: the source evidence places
     * each circle independently, so they must not be redistributed by the
     * adaptive header detector. The 1920x864 and 1280x576 entries are capture
     * aliases from the same phone, not resolution-based profiles shared with
     * another device.
     */
    private val xiaomi24122rkc7gReference = VisionCalibrationConfig().copy(
        allyAvatarCenterX = .11662696f,
        enemyAvatarCenterX = .9165625f,
        avatarDiameterRatio = .11f,
        allySlotXRatios = List(5) { .11662696f },
        allySlotYRatios = listOf(.188f, .324f, .458f, .591f, .725f),
        allySlotDiameterRatios = listOf(.105f, .105f, .105f, .105f, .1f),
        enemySlotXRatios = List(5) { .9165625f },
        enemySlotYRatios = listOf(.18861003f, .32380405f, .4560159f, .58822775f, .71944547f),
        enemySlotDiameterRatios = List(5) { .107f },
        allyBanXRatios = listOf(.06875f, .087500006f, .13f, .1725f, .21499999f),
        allyBanYRatios = listOf(.05277778f, .06388889f, .05138889f, .043055557f, .05138889f),
        allyBanDiameterRatios = List(5) { .061111107f },
        manualAllyBans = true,
        enemyBanXRatios = listOf(.785f, .82125f, .856875f, .8925f, .93f),
        enemyBanYRatios = listOf(.05138889f, .05138889f, .05138889f, .047222223f, .05277778f),
        enemyBanDiameterRatios = List(5) { .058333334f },
        manualEnemyBans = true,
        topAvatarYRatio = .045f,
        topAvatarDiameterRatio = .072f,
        topAlly5XRatio = .16712953f,
        topEnemy5XRatio = .9741347f,
        topAllyXRatios = listOf(.028f, .063f, .098f, .133f, .16712953f),
        topEnemyXRatios = listOf(.834f, .869f, .904f, .939f, .9741347f)
    )

    /**
     * Vision Draft calibration supplied from the OPPO CPH2791 capture.
     *
     * The ban rows are manual frame coordinates. They must remain independent
     * from the slot columns because the supplied capture shows the picks in
     * the expected columns while the ban circles sit on a different row.
     * 1920x881 and 1280x587 are capture aliases of the same device profile.
     */
    private val oppoCph2791Reference = VisionCalibrationConfig().copy(
        allyAvatarCenterX = .12303629f,
        enemyAvatarCenterX = .90703124f,
        avatarDiameterRatio = .11f,
        allySlotXRatios = List(5) { .12303629f },
        allySlotYRatios = listOf(.188f, .324f, .458f, .591f, .725f),
        allySlotDiameterRatios = listOf(.11f, .11f, .11f, .11f, .105f),
        enemySlotXRatios = List(5) { .90703124f },
        enemySlotYRatios = listOf(.18950696f, .3253833f, .45826235f, .5911414f, .72302145f),
        enemySlotDiameterRatios = List(5) { .11f },
        allyBanXRatios = listOf(.078402996f, .11492116f, .15143925f, .18795736f, .22447549f),
        allyBanYRatios = List(5) { .052f },
        allyBanDiameterRatios = List(5) { .06f },
        manualAllyBans = true,
        enemyBanXRatios = listOf(.77501595f, .8100701f, .8470363f, .8833652f, .9203314f),
        enemyBanYRatios = listOf(.05f, .048611112f, .048611112f, .05f, .05277778f),
        enemyBanDiameterRatios = List(5) { .06f },
        manualEnemyBans = true,
        topAvatarYRatio = .045f,
        topAvatarDiameterRatio = .072f,
        topAlly5XRatio = .17041795f,
        topEnemy5XRatio = .9736258f,
        topAllyXRatios = listOf(.03041795f, .06541795f, .10041795f, .13541795f, .17041795f),
        topEnemyXRatios = listOf(.8336258f, .8686258f, .9036258f, .9386258f, .9736258f)
    )

    private val profiles: List<Profile> = listOf(
        Profile("nubia", "NX789J", 2688, 1216, nubiaNx789jReference),
        Profile("nubia", "NX789J", 1920, 869, nubiaNx789jReference),
        Profile("Xiaomi", "24122RKC7G", 2400, 1080, xiaomi24122rkc7gReference),
        Profile("Xiaomi", "24122RKC7G", 1920, 864, xiaomi24122rkc7gReference),
        Profile("Xiaomi", "24122RKC7G", 1280, 576, xiaomi24122rkc7gReference),
        Profile("OPPO", "CPH2791", 2354, 1080, oppoCph2791Reference),
        Profile("OPPO", "CPH2791", 1920, 881, oppoCph2791Reference),
        Profile("OPPO", "CPH2791", 1280, 587, oppoCph2791Reference)
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
