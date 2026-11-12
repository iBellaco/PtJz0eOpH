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

    private val profiles = listOf(
        Profile("samsung", "SM-A156U", 2340, 1080, measured(
            .11f, .09200001f, .934f,
            List(5) { .09200001f }, listOf(.188f, .324f, .458f, .591f, .725f),
            listOf(.12f, .12f, .12f, .12f, .115f),
            List(5) { .934f }, listOf(.188f, .324f, .457f, .59f, .722f), List(5) { .12f },
            listOf(.028f, .064f, .1f, .136f, .172f), List(5) { .052f }, List(5) { .06f }, false,
            listOf(.826f, .863f, .9f, .937f, .974f), List(5) { .052f }, List(5) { .06f }, false,
            .045f, .072f, .168f, .974f
        )),
        Profile("Xiaomi", "2412DPC0AG", 2712, 1220, measured(
            .11f, .11862696f, .91106254f,
            listOf(.11862696f, .118875f, .11862696f, .11862696f, .11862696f),
            listOf(.188f, .32118055f, .458f, .591f, .725f), listOf(.11f, .11f, .11f, .11f, .105f),
            List(5) { .91106254f }, listOf(.18750001f, .3244269f, .45833334f, .5922398f, .7251394f), List(5) { .11f },
            listOf(.027854925f, .0636684f, .09948187f, .13529535f, .17110883f), List(5) { .052f }, List(5) { .06f }, false,
            listOf(.82690156f, .8637098f, .9005181f, .93732643f, .9741347f), List(5) { .052f }, List(5) { .06f }, false,
            .045f, .072f, .16712953f, .9741347f
        )),
        Profile("Xiaomi", "25062RN2DL", 2340, 1080, measured(
            .11f, .15031247f, .959f,
            listOf(.15031247f, .11906247f, .11906247f, .11906247f, .11906247f),
            listOf(.19120136f, .32453611f, .45854485f, .5915535f, .72556216f),
            listOf(.11f, .11f, .11f, .11f, .105f),
            List(5) { .959f },
            listOf(.188f, .324f, .457f, .59f, .722f), List(5) { .11f },
            listOf(.028564192f, .06528959f, .102014974f, .13874036f, .17546576f),
            List(5) { .052f }, List(5) { .06f }, false,
            listOf(.8224939f, .86023945f, .897985f, .9357306f, .9734761f),
            List(5) { .052f }, List(5) { .06f }, false,
            .045f, .072f, .17138515f, .9734761f
        )),
        Profile("samsung", "SM-S942B", 2340, 1080, measured(
            .11f, .073450774f, .9109375f,
            listOf(.090450786f, .08984375f, .08984375f, .08984375f, .08984375f),
            listOf(.189f, .32438484f, .45735165f, .5883336f, .72030044f), List(5) { .105f },
            List(5) { .9109375f }, listOf(.18818618f, .3248731f, .45854485f, .5922166f, .7248833f), List(5) { .105f },
            listOf(.07538463f, .1119231f, .14653845f, .18499997f, .22153842f),
            listOf(.05138889f, .05138889f, .05277778f, .05138889f, .05138889f), List(5) { .06388889f }, true,
            listOf(.8224939f, .86023945f, .897985f, .9357306f, .9734761f), List(5) { .052f }, List(5) { .06f }, false,
            .045f, .072f, .17138515f, .9734761f
        )),
        Profile("HONOR", "BVL-N49", 2800, 1280, measured(
            .11f, .12200002f, .90703124f,
            List(5) { .12200002f }, listOf(.188f, .324f, .458f, .591f, .725f),
            listOf(.11f, .11f, .11f, .11f, .105f),
            List(5) { .90703124f }, listOf(.18571843f, .32257378f, .45641026f, .59024674f, .72307694f), List(5) { .11f },
            listOf(.076874204f, .113723f, .1505718f, .18678527f, .22371663f),
            listOf(.047222223f, .05277778f, .05277778f, .05277778f, .05138889f), List(5) { .058333334f }, true,
            listOf(.826f, .863f, .9f, .937f, .974f), List(5) { .052f }, List(5) { .06f }, false,
            .045f, .072f, .168f, .974f
        )),
        Profile("samsung", "SM-S938B", 2340, 1080, measured(
            .11f, .072f, .959f,
            List(5) { .072f }, listOf(.188f, .324f, .458f, .591f, .725f),
            listOf(.11f, .11f, .11f, .11f, .105f),
            List(5) { .959f }, listOf(.188f, .324f, .457f, .59f, .722f), List(5) { .11f },
            listOf(.028f, .064f, .1f, .136f, .172f), List(5) { .052f }, List(5) { .06f }, false,
            listOf(.826f, .863f, .9f, .937f, .974f), List(5) { .052f }, List(5) { .06f }, false,
            .045f, .072f, .168f, .974f
        )),
        Profile("samsung", "SM-S948B", 3120, 1440, measured(
            .11f, .1171875f, .91328126f,
            List(5) { .1171875f }, listOf(.19071549f, .32604176f, .45937794f, .5917191f, .7250552f),
            listOf(.11f, .11f, .11f, .11f, .105f),
            List(5) { .91328126f }, listOf(.18790826f, .32473567f, .45854485f, .592354f, .7251571f), List(5) { .11f },
            listOf(.028564192f, .06528959f, .102014974f, .13874036f, .17546576f), List(5) { .052f }, List(5) { .06f }, false,
            listOf(.8224939f, .86023945f, .897985f, .9357306f, .9734761f), List(5) { .052f }, List(5) { .06f }, false,
            .045f, .072f, .17138515f, .9734761f
        )),
        Profile("OPPO", "CPH2791", 2354, 1080, measured(
            .11f, .12803629f, .90703124f,
            List(5) { .12803629f }, listOf(.188f, .324f, .458f, .591f, .725f),
            listOf(.11f, .11f, .11f, .11f, .105f),
            List(5) { .90703124f },
            listOf(.18950696f, .3253833f, .45826235f, .5911414f, .72302145f), List(5) { .11f },
            listOf(.028402993f, .06492113f, .10143926f, .13795741f, .17447554f),
            List(5) { .052f }, List(5) { .06f }, false,
            listOf(.8234956f, .8610282f, .8985607f, .9360932f, .9736258f),
            List(5) { .052f }, List(5) { .06f }, false,
            .045f, .072f, .17041795f, .9736258f
        )),
        Profile("motorola", "moto g56 5G", 2400, 1080, measured(
            .11f, .1171875f, .91421247f,
            listOf(.1171875f, .08828125f, .08828125f, .08828125f, .08828125f),
            listOf(.1892361f, .32415208f, .45833334f, .5915132f, .7256944f),
            listOf(.105f, .105f, .105f, .105f, .1f),
            List(5) { .91421247f }, listOf(.188f, .324f, .457f, .59f, .722f), List(5) { .105f },
            listOf(.027854925f, .0636684f, .09948187f, .13529535f, .17110883f),
            List(5) { .052f }, List(5) { .06f }, false,
            listOf(.82690156f, .8637098f, .9005181f, .93732643f, .9741347f),
            List(5) { .052f }, List(5) { .06f }, false,
            .045f, .072f, .16712953f, .9741347f
        ))
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
