package com.example.service.screen

import android.content.Context
import android.content.SharedPreferences

/**
 * Modelo de calibración geométrica de las áreas de escaneo de selección de campeones (Draft).
 * Los valores están expresados en ratios normalizados (0.0f a 1.0f) respecto a la pantalla/captura.
 */
data class VisionCalibrationConfig(
    // Posición horizontal X central de avatares en columnas verticales de draft (0..1)
    val allyAvatarCenterX: Float = 0.072f,
    val enemyAvatarCenterX: Float = 0.959f,

    // Diámetro del avatar relativo al alto de pantalla (0..1)
    val avatarDiameterRatio: Float = 0.110f,

    // Ratios verticales Y para los 5 slots aliados (0..4)
    val allySlotYRatios: List<Float> = listOf(
        0.188f,
        0.324f,
        0.458f,
        0.591f,
        0.725f
    ),

    // Ratios verticales Y para los 5 slots enemigos (0..4)
    val enemySlotYRatios: List<Float> = listOf(
        0.188f,
        0.324f,
        0.457f,
        0.590f,
        0.722f
    ),

    // Ratios individuales horizontales X para cada slot aliado (0..4)
    val allySlotXRatios: List<Float> = listOf(
        0.072f,
        0.072f,
        0.072f,
        0.072f,
        0.072f
    ),

    // Ratios individuales horizontales X para cada slot enemigo (0..4)
    val enemySlotXRatios: List<Float> = listOf(
        0.959f,
        0.959f,
        0.959f,
        0.959f,
        0.959f
    ),

    // Diámetros individuales para cada slot aliado (0..4)
    val allySlotDiameterRatios: List<Float> = listOf(
        0.110f,
        0.110f,
        0.110f,
        0.110f,
        0.105f
    ),

    // Diámetros individuales para cada slot enemigo (0..4)
    val enemySlotDiameterRatios: List<Float> = listOf(
        0.110f,
        0.110f,
        0.110f,
        0.110f,
        0.110f
    ),

    // Ban rows have their own geometry, independent from player slots and tenth pick.
    val allyBanXRatios: List<Float> = listOf(.028f, .064f, .100f, .136f, .172f),
    val enemyBanXRatios: List<Float> = listOf(.826f, .863f, .900f, .937f, .974f),
    val allyBanYRatio: Float = .052f,
    val enemyBanYRatio: Float = .052f,
    val banDiameterRatio: Float = .060f,
    val manualAllyBans: Boolean = false,
    val manualEnemyBans: Boolean = false,

    // Hechizos de invocador aliados
    val spellLeftRatio: Float = 0.021f,
    val spellSizeRatio: Float = 0.041f,
    val spellYOffsetRatio: Float = 0.0f,

    // Rango horizontal OCR para columnas de draft (restringido estrictamente a las columnas de slots, sin tocar el centro)
    val allyOcrMinX: Float = 0.050f,
    val allyOcrMaxX: Float = 0.280f,
    val enemyOcrMinX: Float = 0.805f,
    val enemyOcrMaxX: Float = 0.925f,

    // --- CÍRCULOS DE AVATARES SUPERIORES (10º PICK Y FASE DE PREPARACIÓN) ---
    val topAvatarYRatio: Float = 0.045f,
    val topAvatarDiameterRatio: Float = 0.072f,
    val topAlly5XRatio: Float = 0.168f,
    val topEnemy5XRatio: Float = 0.974f,
    val topAllyXRatios: List<Float> = listOf(0.028f, 0.063f, 0.098f, 0.133f, 0.168f),
    val topEnemyXRatios: List<Float> = listOf(0.834f, 0.869f, 0.904f, 0.939f, 0.974f)
) {
    fun getAllySlotX(slotIdx: Int): Float = allySlotXRatios.getOrElse(slotIdx) { allyAvatarCenterX }
    fun getEnemySlotX(slotIdx: Int): Float = enemySlotXRatios.getOrElse(slotIdx) { enemyAvatarCenterX }
    fun getSlotDiameter(isAlly: Boolean, slotIdx: Int): Float =
        if (isAlly) allySlotDiameterRatios.getOrElse(slotIdx) { avatarDiameterRatio }
        else enemySlotDiameterRatios.getOrElse(slotIdx) { avatarDiameterRatio }

    /** Applies the calibrator's four-way movement and size controls to every circle in one column. */
    fun adjustDraftColumn(
        isAlly: Boolean,
        deltaX: Float = 0f,
        deltaY: Float = 0f,
        deltaDiameter: Float = 0f
    ): VisionCalibrationConfig {
        val centerX = if (isAlly) allyAvatarCenterX else enemyAvatarCenterX
        val minX = if (isAlly) 0.01f else 0.55f
        val maxX = if (isAlly) 0.45f else 0.99f
        val adjustedCenterX = (centerX + deltaX).coerceIn(minX, maxX)
        val appliedX = adjustedCenterX - centerX
        val xRatios = (if (isAlly) allySlotXRatios else enemySlotXRatios)
            .map { (it + appliedX).coerceIn(minX, maxX) }
        val yRatios = (if (isAlly) allySlotYRatios else enemySlotYRatios)
            .map { (it + deltaY).coerceIn(0.05f, 0.95f) }
        val diameterRatios = (if (isAlly) allySlotDiameterRatios else enemySlotDiameterRatios)
            .map { (it + deltaDiameter).coerceIn(0.03f, 0.30f) }

        return if (isAlly) copy(
            allyAvatarCenterX = adjustedCenterX,
            allySlotXRatios = xRatios,
            allySlotYRatios = yRatios,
            allySlotDiameterRatios = diameterRatios
        ) else copy(
            enemyAvatarCenterX = adjustedCenterX,
            enemySlotXRatios = xRatios,
            enemySlotYRatios = yRatios,
            enemySlotDiameterRatios = diameterRatios
        )
    }

    /** Moves/resizes one portrait without changing its neighbours or the other team. */
    fun adjustDraftSlot(isAlly: Boolean, slotIndex: Int, deltaX: Float = 0f,
                        deltaY: Float = 0f, deltaDiameter: Float = 0f): VisionCalibrationConfig {
        require(slotIndex in 0..4)
        val xs = List(5) { if (isAlly) getAllySlotX(it) else getEnemySlotX(it) }.toMutableList()
        val ys = List(5) { (if (isAlly) allySlotYRatios else enemySlotYRatios)
            .getOrElse(it) { 0.188f + it * 0.136f } }.toMutableList()
        val diameters = List(5) { getSlotDiameter(isAlly, it) }.toMutableList()
        xs[slotIndex] = (xs[slotIndex] + deltaX).coerceIn(if (isAlly) 0.01f else 0.55f, if (isAlly) 0.45f else 0.99f)
        ys[slotIndex] = (ys[slotIndex] + deltaY).coerceIn(0.05f, 0.95f)
        diameters[slotIndex] = (diameters[slotIndex] + deltaDiameter).coerceIn(0.03f, 0.30f)
        return if (isAlly) copy(allySlotXRatios = xs, allySlotYRatios = ys, allySlotDiameterRatios = diameters)
        else copy(enemySlotXRatios = xs, enemySlotYRatios = ys, enemySlotDiameterRatios = diameters)
    }

    fun adjustBanRow(isAlly: Boolean, deltaX: Float = 0f, deltaY: Float = 0f,
                     deltaDiameter: Float = 0f): VisionCalibrationConfig {
        val xs = (if (isAlly) allyBanXRatios else enemyBanXRatios)
            .map { (it + deltaX).coerceIn(if (isAlly) .01f else .55f, if (isAlly) .45f else .99f) }
        val y = ((if (isAlly) allyBanYRatio else enemyBanYRatio) + deltaY).coerceIn(.025f, .10f)
        val diameter = (banDiameterRatio + deltaDiameter).coerceIn(.03f, .09f)
        return if (isAlly) copy(allyBanXRatios = xs, allyBanYRatio = y,
            banDiameterRatio = diameter, manualAllyBans = true)
        else copy(enemyBanXRatios = xs, enemyBanYRatio = y,
            banDiameterRatio = diameter, manualEnemyBans = true)
    }

    /** Start manual movement at the row actually seen on this phone, never at another phone's defaults. */
    internal fun withObservedBanRow(isAlly: Boolean, regions: List<BanRegion>): VisionCalibrationConfig {
        if ((if (isAlly) manualAllyBans else manualEnemyBans) || regions.size != 5) return this
        val y = regions.map { it.y }.average().toFloat()
        val diameter = (regions.map { it.radius }.average().toFloat() * 2f).coerceIn(.03f, .09f)
        return if (isAlly) copy(allyBanXRatios = regions.map { it.x }, allyBanYRatio = y, banDiameterRatio = diameter)
        else copy(enemyBanXRatios = regions.map { it.x }, enemyBanYRatio = y, banDiameterRatio = diameter)
    }

    internal fun banRegions(isAlly: Boolean): List<BanRegion> =
        (if (isAlly) allyBanXRatios else enemyBanXRatios).map {
            BanRegion(it, if (isAlly) allyBanYRatio else enemyBanYRatio, banDiameterRatio / 2f)
        }

    fun toFormattedCoordinatesString(): String {
        val sb = StringBuilder()
        sb.append("=== COORDENADAS DE CALIBRACIÓN VISION DRAFT ===\n")
        sb.append("• Diámetro Avatar Global (⌀): ${(avatarDiameterRatio * 100).format(2)}% (${avatarDiameterRatio}f)\n")
        sb.append("• Columna Aliada X: ${(allyAvatarCenterX * 100).format(2)}% (${allyAvatarCenterX}f)\n")
        sb.append("• Columna Rival X: ${(enemyAvatarCenterX * 100).format(2)}% (${enemyAvatarCenterX}f)\n\n")
        
        sb.append("• Círculos Superiores (Top Bar / 10º Pick):\n")
        sb.append("  - Altura Y: ${(topAvatarYRatio * 100).format(2)}% (${topAvatarYRatio}f)\n")
        sb.append("  - Diámetro (⌀): ${(topAvatarDiameterRatio * 100).format(2)}% (${topAvatarDiameterRatio}f)\n")
        sb.append("  - Top 10º Pick Rival (5º Rival X): ${(topEnemy5XRatio * 100).format(2)}% (${topEnemy5XRatio}f)\n")
        sb.append("  - Top 10º Pick Aliado (5º Aliado X): ${(topAlly5XRatio * 100).format(2)}% (${topAlly5XRatio}f)\n\n")

        sb.append("• Baneos: ⌀=${(banDiameterRatio * 100).format(2)}%\n")
        sb.append("  - Baneo aliado: X=$allyBanXRatios | Y=$allyBanYRatio | Manual=$manualAllyBans\n")
        sb.append("  - Baneo rival: X=$enemyBanXRatios | Y=$enemyBanYRatio | Manual=$manualEnemyBans\n\n")
        sb.append("• Slots Aliados (Posición X, Y y Diámetro):\n")
        val roles = listOf("TOP", "JUNGLE", "MID", "ADC", "SUPPORT")
        for (i in 0..4) {
            val roleName = roles.getOrElse(i) { "Slot $i" }
            val x = getAllySlotX(i)
            val y = allySlotYRatios.getOrElse(i) { 0.188f }
            val d = getSlotDiameter(true, i)
            sb.append("  - Slot ${i + 1} ($roleName): X=${(x * 100).format(2)}% (${x}f) | Y=${(y * 100).format(2)}% (${y}f) | ⌀=${(d * 100).format(2)}% (${d}f)\n")
        }
        sb.append("\n• Slots Rivales (Posición X, Y y Diámetro):\n")
        for (i in 0..4) {
            val roleName = roles.getOrElse(i) { "Slot $i" }
            val x = getEnemySlotX(i)
            val y = enemySlotYRatios.getOrElse(i) { 0.188f }
            val d = getSlotDiameter(false, i)
            sb.append("  - Slot ${i + 1} ($roleName): X=${(x * 100).format(2)}% (${x}f) | Y=${(y * 100).format(2)}% (${y}f) | ⌀=${(d * 100).format(2)}% (${d}f)\n")
        }
        sb.append("===============================================")
        return sb.toString()
    }

    fun toKotlinCode(): String {
        return """
VisionCalibrationConfig(
    allyAvatarCenterX = ${allyAvatarCenterX}f,
    enemyAvatarCenterX = ${enemyAvatarCenterX}f,
    avatarDiameterRatio = ${avatarDiameterRatio}f,
    allySlotYRatios = listOf(${allySlotYRatios.joinToString(", ") { "${it}f" }}),
    enemySlotYRatios = listOf(${enemySlotYRatios.joinToString(", ") { "${it}f" }}),
    allySlotXRatios = listOf(${allySlotXRatios.joinToString(", ") { "${it}f" }}),
    enemySlotXRatios = listOf(${enemySlotXRatios.joinToString(", ") { "${it}f" }}),
    allySlotDiameterRatios = listOf(${allySlotDiameterRatios.joinToString(", ") { "${it}f" }}),
    enemySlotDiameterRatios = listOf(${enemySlotDiameterRatios.joinToString(", ") { "${it}f" }}),
    allyOcrMinX = ${allyOcrMinX}f,
    allyOcrMaxX = ${allyOcrMaxX}f,
    enemyOcrMinX = ${enemyOcrMinX}f,
    enemyOcrMaxX = ${enemyOcrMaxX}f,
    topAvatarYRatio = ${topAvatarYRatio}f,
    topAvatarDiameterRatio = ${topAvatarDiameterRatio}f,
    topAlly5XRatio = ${topAlly5XRatio}f,
    topEnemy5XRatio = ${topEnemy5XRatio}f,
    allyBanXRatios = listOf(${allyBanXRatios.joinToString(", ") { "${it}f" }}),
    enemyBanXRatios = listOf(${enemyBanXRatios.joinToString(", ") { "${it}f" }}),
    allyBanYRatio = ${allyBanYRatio}f,
    enemyBanYRatio = ${enemyBanYRatio}f,
    banDiameterRatio = ${banDiameterRatio}f,
    manualAllyBans = $manualAllyBans,
    manualEnemyBans = $manualEnemyBans,
    topAllyXRatios = listOf(${topAllyXRatios.joinToString(", ") { "${it}f" }}),
    topEnemyXRatios = listOf(${topEnemyXRatios.joinToString(", ") { "${it}f" }})
)
        """.trimIndent()
    }

    fun saveToPrefs(context: Context) {
        val prefs = context.getSharedPreferences("vision_calibration_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("calibration_device_model", deviceModelKey())
            putInt("calibration_version", CURRENT_CALIBRATION_VERSION)
            allyBanXRatios.forEachIndexed { i, x -> putFloat("ban_ally_x_$i", x) }
            enemyBanXRatios.forEachIndexed { i, x -> putFloat("ban_enemy_x_$i", x) }
            putFloat("ban_ally_y", allyBanYRatio)
            putFloat("ban_enemy_y", enemyBanYRatio)
            putFloat("ban_diameter", banDiameterRatio)
            putBoolean("ban_manual_ally", manualAllyBans)
            putBoolean("ban_manual_enemy", manualEnemyBans)
            putFloat("allyAvatarCenterX", allyAvatarCenterX)
            putFloat("enemyAvatarCenterX", enemyAvatarCenterX)
            putFloat("avatarDiameterRatio", avatarDiameterRatio)
            allySlotYRatios.forEachIndexed { idx, v -> putFloat("ally_slot_y_$idx", v) }
            enemySlotYRatios.forEachIndexed { idx, v -> putFloat("enemy_slot_y_$idx", v) }
            allySlotXRatios.forEachIndexed { idx, v -> putFloat("ally_slot_x_$idx", v) }
            enemySlotXRatios.forEachIndexed { idx, v -> putFloat("enemy_slot_x_$idx", v) }
            allySlotDiameterRatios.forEachIndexed { idx, v -> putFloat("ally_slot_diam_$idx", v) }
            enemySlotDiameterRatios.forEachIndexed { idx, v -> putFloat("enemy_slot_diam_$idx", v) }
            putFloat("allyOcrMinX", allyOcrMinX)
            putFloat("allyOcrMaxX", allyOcrMaxX)
            putFloat("enemyOcrMinX", enemyOcrMinX)
            putFloat("enemyOcrMaxX", enemyOcrMaxX)
            putFloat("topAvatarYRatio", topAvatarYRatio)
            putFloat("topAvatarDiameterRatio", topAvatarDiameterRatio)
            putFloat("topAlly5XRatio", topAlly5XRatio)
            putFloat("topEnemy5XRatio", topEnemy5XRatio)
            topAllyXRatios.forEachIndexed { idx, v -> putFloat("top_ally_x_$idx", v) }
            topEnemyXRatios.forEachIndexed { idx, v -> putFloat("top_enemy_x_$idx", v) }
            apply()
        }
    }

    companion object {
        private const val CURRENT_CALIBRATION_VERSION = 9
        private fun deviceModelKey(): String =
            "${android.os.Build.MANUFACTURER}:${android.os.Build.MODEL}".lowercase(java.util.Locale.ROOT)

        /** Measured normalized capture coordinates; saved individual adjustments take precedence. */
        internal fun defaultsForDevice(manufacturer: String, model: String): VisionCalibrationConfig {
            val device = "$manufacturer $model".lowercase(java.util.Locale.ROOT)
            // POCO X7 Pro regional identifiers, including the global 2412DPC0AG.
            val poco = "poco x7 pro" in device || "2412dpc0a" in device || "2412dpc0i" in device
            return if (!poco) VisionCalibrationConfig() else VisionCalibrationConfig(
                allyAvatarCenterX = .120f, enemyAvatarCenterX = .911f,
                allySlotXRatios = List(5) { .120f }, enemySlotXRatios = List(5) { .911f },
                allySlotYRatios = listOf(.184f, .320f, .454f, .592f, .728f),
                enemySlotYRatios = listOf(.179f, .315f, .449f, .587f, .723f),
                allySlotDiameterRatios = List(5) { .110f }
            )
        }

        fun resetToDefaults(context: Context): VisionCalibrationConfig {
            val prefs = context.getSharedPreferences("vision_calibration_prefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            val defaultConfig = defaultsForDevice(android.os.Build.MANUFACTURER, android.os.Build.MODEL)
            defaultConfig.saveToPrefs(context)
            prefs.edit().putInt("calibration_version", CURRENT_CALIBRATION_VERSION).apply()
            return defaultConfig
        }

        fun loadFromPrefs(context: Context): VisionCalibrationConfig {
            val prefs = context.getSharedPreferences("vision_calibration_prefs", Context.MODE_PRIVATE)
            val savedModel = prefs.getString("calibration_device_model", null)
            if (savedModel != null && savedModel != deviceModelKey()) {
                // Cloud restore may also target a different phone; its portrait positions are independent.
                prefs.edit().clear().apply()
            }
            prefs.edit().putString("calibration_device_model", deviceModelKey()).apply()
            if (!prefs.getBoolean("device_profile_checked", false)) {
                val poco = defaultsForDevice(android.os.Build.MANUFACTURER, android.os.Build.MODEL)
                val generic = VisionCalibrationConfig()
                val untouched = (0..4).all { i ->
                    prefs.getFloat("ally_slot_x_$i", generic.getAllySlotX(i)) == generic.getAllySlotX(i) &&
                    prefs.getFloat("enemy_slot_x_$i", generic.getEnemySlotX(i)) == generic.getEnemySlotX(i) &&
                    prefs.getFloat("ally_slot_y_$i", generic.allySlotYRatios[i]) == generic.allySlotYRatios[i] &&
                    prefs.getFloat("enemy_slot_y_$i", generic.enemySlotYRatios[i]) == generic.enemySlotYRatios[i] &&
                    prefs.getFloat("ally_slot_diam_$i", generic.getSlotDiameter(true, i)) == generic.getSlotDiameter(true, i) &&
                    prefs.getFloat("enemy_slot_diam_$i", generic.getSlotDiameter(false, i)) == generic.getSlotDiameter(false, i)
                }
                if (poco != generic && untouched) {
                    // Preserve existing ban adjustments while replacing only the player-column preset.
                    val old = prefs.all.filterKeys { it.startsWith("ban_") }
                    poco.saveToPrefs(context)
                    prefs.edit().apply { old.forEach { (key, value) -> when(value) {
                        is Float -> putFloat(key, value); is Boolean -> putBoolean(key, value)
                    } }; apply() }
                }
                prefs.edit().putBoolean("device_profile_checked", true).apply()
            }
            val version = prefs.getInt("calibration_version", 0)
            val default = defaultsForDevice(android.os.Build.MANUFACTURER, android.os.Build.MODEL)

            if (!prefs.contains("avatarDiameterRatio") || version < CURRENT_CALIBRATION_VERSION) {
                val savedAllyX = prefs.getFloat("allyAvatarCenterX", default.allyAvatarCenterX)
                val migratedAllyX = if (version < 9 && (savedAllyX == 0.076f || savedAllyX == 0.080f)) default.allyAvatarCenterX else savedAllyX
                
                val savedEnemyX = prefs.getFloat("enemyAvatarCenterX", default.enemyAvatarCenterX)
                val migratedEnemyX = if (version < 9 && (savedEnemyX == 0.962f || savedEnemyX == 0.928f)) default.enemyAvatarCenterX else savedEnemyX

                val allyY = (0..4).map { idx -> prefs.getFloat("ally_slot_y_$idx", default.allySlotYRatios[idx]) }
                val enemyY = (0..4).map { idx -> prefs.getFloat("enemy_slot_y_$idx", default.enemySlotYRatios[idx]) }
                val allyX = (0..4).map { idx -> 
                    val x = prefs.getFloat("ally_slot_x_$idx", default.allySlotXRatios[idx])
                    if (version < 9 && (x == 0.076f || x == 0.080f)) default.allySlotXRatios[idx] else x
                }
                val enemyX = (0..4).map { idx -> 
                    val x = prefs.getFloat("enemy_slot_x_$idx", default.enemySlotXRatios[idx])
                    if (version < 9 && (x == 0.962f || x == 0.928f)) default.enemySlotXRatios[idx] else x
                }
                val allyD = (0..4).map { idx -> prefs.getFloat("ally_slot_diam_$idx", default.allySlotDiameterRatios[idx]) }
                val enemyD = (0..4).map { idx -> prefs.getFloat("enemy_slot_diam_$idx", default.enemySlotDiameterRatios[idx]) }

                val migrated = VisionCalibrationConfig(
                    allyAvatarCenterX = migratedAllyX,
                    enemyAvatarCenterX = migratedEnemyX,
                    avatarDiameterRatio = prefs.getFloat("avatarDiameterRatio", default.avatarDiameterRatio),
                    allySlotYRatios = allyY,
                    enemySlotYRatios = enemyY,
                    allySlotXRatios = allyX,
                    enemySlotXRatios = enemyX,
                    allySlotDiameterRatios = allyD,
                    enemySlotDiameterRatios = enemyD,
                    allyOcrMinX = prefs.getFloat("allyOcrMinX", default.allyOcrMinX),
                    allyOcrMaxX = prefs.getFloat("allyOcrMaxX", default.allyOcrMaxX),
                    enemyOcrMinX = prefs.getFloat("enemyOcrMinX", default.enemyOcrMinX),
                    enemyOcrMaxX = prefs.getFloat("enemyOcrMaxX", default.enemyOcrMaxX),
                    topAvatarYRatio = default.topAvatarYRatio,
                    topAvatarDiameterRatio = default.topAvatarDiameterRatio,
                    topAlly5XRatio = default.topAlly5XRatio,
                    topEnemy5XRatio = default.topEnemy5XRatio,
                    topAllyXRatios = default.topAllyXRatios,
                    topEnemyXRatios = default.topEnemyXRatios
                )
                migrated.saveToPrefs(context)
                prefs.edit().putInt("calibration_version", CURRENT_CALIBRATION_VERSION).apply()
                return migrated
            }

            val allyY = (0..4).map { idx -> prefs.getFloat("ally_slot_y_$idx", default.allySlotYRatios[idx]) }
            val enemyY = (0..4).map { idx -> prefs.getFloat("enemy_slot_y_$idx", default.enemySlotYRatios[idx]) }
            val allyX = (0..4).map { idx -> prefs.getFloat("ally_slot_x_$idx", default.allySlotXRatios[idx]) }
            val enemyX = (0..4).map { idx -> prefs.getFloat("enemy_slot_x_$idx", default.enemySlotXRatios[idx]) }
            val allyD = (0..4).map { idx -> prefs.getFloat("ally_slot_diam_$idx", default.allySlotDiameterRatios[idx]) }
            val enemyD = (0..4).map { idx -> prefs.getFloat("enemy_slot_diam_$idx", default.enemySlotDiameterRatios[idx]) }
            val topAllyX = (0..4).map { idx -> prefs.getFloat("top_ally_x_$idx", default.topAllyXRatios[idx]) }
            val topEnemyX = (0..4).map { idx -> prefs.getFloat("top_enemy_x_$idx", default.topEnemyXRatios[idx]) }

            val loadedEnemyX = prefs.getFloat("enemyAvatarCenterX", default.enemyAvatarCenterX)
            val resolvedEnemyX = if (loadedEnemyX == 0.928f) default.enemyAvatarCenterX else loadedEnemyX

            return VisionCalibrationConfig(
                allyAvatarCenterX = prefs.getFloat("allyAvatarCenterX", default.allyAvatarCenterX),
                enemyAvatarCenterX = resolvedEnemyX,
                avatarDiameterRatio = prefs.getFloat("avatarDiameterRatio", default.avatarDiameterRatio),
                allySlotYRatios = allyY,
                enemySlotYRatios = enemyY,
                allySlotXRatios = allyX,
                enemySlotXRatios = enemyX,
                allySlotDiameterRatios = allyD,
                enemySlotDiameterRatios = enemyD,
                allyOcrMinX = prefs.getFloat("allyOcrMinX", default.allyOcrMinX),
                allyOcrMaxX = prefs.getFloat("allyOcrMaxX", default.allyOcrMaxX),
                enemyOcrMinX = prefs.getFloat("enemyOcrMinX", default.enemyOcrMinX),
                enemyOcrMaxX = prefs.getFloat("enemyOcrMaxX", default.enemyOcrMaxX),
                topAvatarYRatio = prefs.getFloat("topAvatarYRatio", default.topAvatarYRatio),
                topAvatarDiameterRatio = prefs.getFloat("topAvatarDiameterRatio", default.topAvatarDiameterRatio),
                topAlly5XRatio = prefs.getFloat("topAlly5XRatio", default.topAlly5XRatio),
                topEnemy5XRatio = prefs.getFloat("topEnemy5XRatio", default.topEnemy5XRatio),
                topAllyXRatios = topAllyX,
                topEnemyXRatios = topEnemyX,
                allyBanXRatios = (0..4).map { prefs.getFloat("ban_ally_x_$it", default.allyBanXRatios[it]) },
                enemyBanXRatios = (0..4).map { prefs.getFloat("ban_enemy_x_$it", default.enemyBanXRatios[it]) },
                allyBanYRatio = prefs.getFloat("ban_ally_y", default.allyBanYRatio),
                enemyBanYRatio = prefs.getFloat("ban_enemy_y", default.enemyBanYRatio),
                banDiameterRatio = prefs.getFloat("ban_diameter", default.banDiameterRatio),
                manualAllyBans = prefs.getBoolean("ban_manual_ally", false),
                manualEnemyBans = prefs.getBoolean("ban_manual_enemy", false)
            )
        }

        private fun Float.format(digits: Int): String = "%.${digits}f".format(java.util.Locale.US, this)
    }
}
