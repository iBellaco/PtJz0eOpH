package com.example.service.screen

import android.content.Context
import android.content.SharedPreferences

/**
 * Modelo de calibración geométrica de las áreas de escaneo de selección de campeones (Draft).
 * Los valores están expresados en ratios normalizados (0.0f a 1.0f) respecto a la pantalla/captura.
 */
data class VisionCalibrationConfig(
    // Posición horizontal X central de avatares en columnas verticales de draft (0..1)
    val allyAvatarCenterX: Float = 0.076f,
    val enemyAvatarCenterX: Float = 0.962f,

    // Diámetro del avatar relativo al alto de pantalla (0..1)
    val avatarDiameterRatio: Float = 0.110f,

    // Ratios verticales Y para los 5 slots aliados (0..4)
    val allySlotYRatios: List<Float> = listOf(
        0.188f,
        0.324f,
        0.458f,
        0.596f,
        0.732f
    ),

    // Ratios verticales Y para los 5 slots enemigos (0..4)
    val enemySlotYRatios: List<Float> = listOf(
        0.188f,
        0.324f,
        0.458f,
        0.596f,
        0.732f
    ),

    // Ratios individuales horizontales X para cada slot aliado (0..4)
    val allySlotXRatios: List<Float> = listOf(
        0.076f,
        0.076f,
        0.076f,
        0.076f,
        0.076f
    ),

    // Ratios individuales horizontales X para cada slot enemigo (0..4)
    val enemySlotXRatios: List<Float> = listOf(
        0.962f,
        0.962f,
        0.962f,
        0.962f,
        0.962f
    ),

    // Diámetros individuales para cada slot aliado (0..4)
    val allySlotDiameterRatios: List<Float> = listOf(
        0.110f,
        0.110f,
        0.110f,
        0.110f,
        0.110f
    ),

    // Diámetros individuales para cada slot enemigo (0..4)
    val enemySlotDiameterRatios: List<Float> = listOf(
        0.110f,
        0.110f,
        0.110f,
        0.110f,
        0.110f
    ),

    // Hechizos de invocador aliados
    val spellLeftRatio: Float = 0.021f,
    val spellSizeRatio: Float = 0.041f,
    val spellYOffsetRatio: Float = 0.0f,

    // Rango horizontal OCR para columnas de draft (restringido estrictamente a las columnas de slots, sin tocar el centro)
    val allyOcrMinX: Float = 0.050f,
    val allyOcrMaxX: Float = 0.280f,
    val enemyOcrMinX: Float = 0.700f,
    val enemyOcrMaxX: Float = 0.950f,

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
    topAllyXRatios = listOf(${topAllyXRatios.joinToString(", ") { "${it}f" }}),
    topEnemyXRatios = listOf(${topEnemyXRatios.joinToString(", ") { "${it}f" }})
)
        """.trimIndent()
    }

    fun saveToPrefs(context: Context) {
        val prefs = context.getSharedPreferences("vision_calibration_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
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
        private const val CURRENT_CALIBRATION_VERSION = 8

        fun resetToDefaults(context: Context): VisionCalibrationConfig {
            val prefs = context.getSharedPreferences("vision_calibration_prefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            val defaultConfig = VisionCalibrationConfig()
            defaultConfig.saveToPrefs(context)
            prefs.edit().putInt("calibration_version", CURRENT_CALIBRATION_VERSION).apply()
            return defaultConfig
        }

        fun loadFromPrefs(context: Context): VisionCalibrationConfig {
            val prefs = context.getSharedPreferences("vision_calibration_prefs", Context.MODE_PRIVATE)
            val version = prefs.getInt("calibration_version", 0)
            val default = VisionCalibrationConfig()

            if (!prefs.contains("avatarDiameterRatio") || version < CURRENT_CALIBRATION_VERSION) {
                val allyY = (0..4).map { idx -> prefs.getFloat("ally_slot_y_$idx", default.allySlotYRatios[idx]) }
                val enemyY = (0..4).map { idx -> prefs.getFloat("enemy_slot_y_$idx", default.enemySlotYRatios[idx]) }
                val allyX = (0..4).map { idx -> prefs.getFloat("ally_slot_x_$idx", default.allySlotXRatios[idx]) }
                val enemyX = (0..4).map { idx -> prefs.getFloat("enemy_slot_x_$idx", default.enemySlotXRatios[idx]) }
                val allyD = (0..4).map { idx -> prefs.getFloat("ally_slot_diam_$idx", default.allySlotDiameterRatios[idx]) }
                val enemyD = (0..4).map { idx -> prefs.getFloat("enemy_slot_diam_$idx", default.enemySlotDiameterRatios[idx]) }
                
                val savedEnemyX = prefs.getFloat("enemyAvatarCenterX", default.enemyAvatarCenterX)
                val migratedEnemyX = if (version < 7 || savedEnemyX == 0.928f) default.enemyAvatarCenterX else savedEnemyX

                val migrated = VisionCalibrationConfig(
                    allyAvatarCenterX = prefs.getFloat("allyAvatarCenterX", default.allyAvatarCenterX),
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
                topEnemyXRatios = topEnemyX
            )
        }

        private fun Float.format(digits: Int): String = "%.${digits}f".format(java.util.Locale.US, this)
    }
}
