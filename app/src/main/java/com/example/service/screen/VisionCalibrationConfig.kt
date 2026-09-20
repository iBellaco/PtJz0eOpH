package com.example.service.screen

import android.content.Context
import android.content.SharedPreferences

/**
 * Modelo de calibración geométrica de las áreas de escaneo de selección de campeones (Draft).
 * Los valores están expresados en ratios normalizados (0.0f a 1.0f) respecto a la pantalla/captura.
 */
data class VisionCalibrationConfig(
    // Posición horizontal X central de avatares en columnas verticales de draft (0..1)
    val allyAvatarCenterX: Float = 0.075f,
    // 10º Pick Aliado: alineado de forma milimétrica con el centro del avatar circular
    val allyTenthAvatarCenterX: Float = 0.075f,
    val enemyAvatarCenterX: Float = 0.925f,
    // 10º Pick Rival: alineado milimétricamente con el centro del avatar circular del slot 5 rival (simétrico a 0.075f)
    val enemyTenthAvatarCenterX: Float = 0.925f,

    // Diámetro del avatar relativo al alto de pantalla (0..1)
    val avatarDiameterRatio: Float = 0.122f,

    // Ratios verticales Y para los 5 slots aliados (0..4)
    val allySlotYRatios: List<Float> = listOf(
        0.190f,
        0.327f,
        0.465f,
        0.603f,
        0.741f
    ),

    // Ratios verticales Y para los 5 slots enemigos (0..4)
    val enemySlotYRatios: List<Float> = listOf(
        0.190f,
        0.327f,
        0.465f,
        0.603f,
        0.741f
    ),

    // Hechizos de invocador aliados
    val spellLeftRatio: Float = 0.021f,
    val spellSizeRatio: Float = 0.041f,
    val spellYOffsetRatio: Float = 0.0f,

    // Rango horizontal OCR para columnas de draft (cobertura integral de mitades de pantalla)
    val allyOcrMinX: Float = 0.000f,
    val allyOcrMaxX: Float = 0.480f,
    val enemyOcrMinX: Float = 0.520f,
    val enemyOcrMaxX: Float = 1.000f,

    // --- CÍRCULOS DE AVATARES SUPERIORES (10º PICK Y FASE DE PREPARACIÓN) ---
    // En la barra superior de Wild Rift durante selección final y fase de preparación:
    // - A la izquierda están los 5 avatares aliados (1..5: del 2.8% al 16.8% de la pantalla).
    // - A la derecha están los 5 avatares rivales (1..5: del 83.4% al 97.4% de la pantalla).
    // - Si el usuario es Primera Selección: el 10º Pick es el último avatar del lado derecho superior (Rival 5: 0.974f).
    // - Si el usuario NO es Primera Selección: el 10º Pick es el último avatar del lado izquierdo superior (Aliado 5: 0.168f).
    val topAvatarYRatio: Float = 0.045f,
    val topAvatarDiameterRatio: Float = 0.072f,
    val topAlly5XRatio: Float = 0.168f,
    val topEnemy5XRatio: Float = 0.974f,
    val topAllyXRatios: List<Float> = listOf(0.028f, 0.063f, 0.098f, 0.133f, 0.168f),
    val topEnemyXRatios: List<Float> = listOf(0.834f, 0.869f, 0.904f, 0.939f, 0.974f)
) {
    fun toFormattedCoordinatesString(): String {
        val sb = StringBuilder()
        sb.append("=== COORDENADAS DE CALIBRACIÓN VISION DRAFT ===\n")
        sb.append("• Diámetro Avatar Slots (⌀): ${(avatarDiameterRatio * 100).format(2)}% (${avatarDiameterRatio}f)\n")
        sb.append("• Centro X Aliados (Slots 1-4): ${(allyAvatarCenterX * 100).format(2)}% (${allyAvatarCenterX}f)\n")
        sb.append("• Centro X 10º Pick Aliado (Slot 5): ${(allyTenthAvatarCenterX * 100).format(2)}% (${allyTenthAvatarCenterX}f)\n")
        sb.append("• Centro X Rivales (Slots 1-4): ${(enemyAvatarCenterX * 100).format(2)}% (${enemyAvatarCenterX}f)\n")
        sb.append("• Centro X 10º Pick Rival (Slot 5): ${(enemyTenthAvatarCenterX * 100).format(2)}% (${enemyTenthAvatarCenterX}f)\n\n")

        sb.append("• Slots Aliados Verticales Y:\n")
        val roles = listOf("TOP", "JUNGLE", "MID", "ADC", "10º / SUPPORT")
        allySlotYRatios.forEachIndexed { i, y ->
            val roleName = roles.getOrElse(i) { "Slot $i" }
            sb.append("  - Slot ${i + 1} ($roleName): ${(y * 100).format(2)}% (${y}f)\n")
        }
        sb.append("\n• Slots Rivales Verticales Y:\n")
        enemySlotYRatios.forEachIndexed { i, y ->
            val roleName = roles.getOrElse(i) { "Slot $i" }
            sb.append("  - Slot ${i + 1} ($roleName): ${(y * 100).format(2)}% (${y}f)\n")
        }
        sb.append("===============================================")
        return sb.toString()
    }

    fun toKotlinCode(): String {
        return """
VisionCalibrationConfig(
    allyAvatarCenterX = ${allyAvatarCenterX}f,
    allyTenthAvatarCenterX = ${allyTenthAvatarCenterX}f,
    enemyAvatarCenterX = ${enemyAvatarCenterX}f,
    enemyTenthAvatarCenterX = ${enemyTenthAvatarCenterX}f,
    avatarDiameterRatio = ${avatarDiameterRatio}f,
    allySlotYRatios = listOf(${allySlotYRatios.joinToString(", ") { "${it}f" }}),
    enemySlotYRatios = listOf(${enemySlotYRatios.joinToString(", ") { "${it}f" }}),
    allyOcrMinX = ${allyOcrMinX}f,
    allyOcrMaxX = ${allyOcrMaxX}f,
    enemyOcrMinX = ${enemyOcrMinX}f,
    enemyOcrMaxX = ${enemyOcrMaxX}f
)
        """.trimIndent()
    }

    fun saveToPrefs(context: Context) {
        val prefs = context.getSharedPreferences("vision_calibration_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putFloat("allyAvatarCenterX", allyAvatarCenterX)
            putFloat("allyTenthAvatarCenterX", allyTenthAvatarCenterX)
            putFloat("enemyAvatarCenterX", enemyAvatarCenterX)
            putFloat("enemyTenthAvatarCenterX", enemyTenthAvatarCenterX)
            putFloat("avatarDiameterRatio", avatarDiameterRatio)
            allySlotYRatios.forEachIndexed { idx, v -> putFloat("ally_slot_y_$idx", v) }
            enemySlotYRatios.forEachIndexed { idx, v -> putFloat("enemy_slot_y_$idx", v) }
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
                // Actualizar automáticamente a la calibración milimétrica oficial Wild Rift 1:1
                val defaultAllyY = default.allySlotYRatios
                val defaultEnemyY = default.enemySlotYRatios
                
                val migrated = VisionCalibrationConfig(
                    allyAvatarCenterX = default.allyAvatarCenterX,
                    allyTenthAvatarCenterX = default.allyTenthAvatarCenterX,
                    enemyAvatarCenterX = default.enemyAvatarCenterX,
                    enemyTenthAvatarCenterX = default.enemyTenthAvatarCenterX,
                    avatarDiameterRatio = default.avatarDiameterRatio,
                    allySlotYRatios = defaultAllyY,
                    enemySlotYRatios = defaultEnemyY,
                    allyOcrMinX = default.allyOcrMinX,
                    allyOcrMaxX = default.allyOcrMaxX,
                    enemyOcrMinX = default.enemyOcrMinX,
                    enemyOcrMaxX = default.enemyOcrMaxX,
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
            val topAllyX = (0..4).map { idx -> prefs.getFloat("top_ally_x_$idx", default.topAllyXRatios[idx]) }
            val topEnemyX = (0..4).map { idx -> prefs.getFloat("top_enemy_x_$idx", default.topEnemyXRatios[idx]) }

            return VisionCalibrationConfig(
                allyAvatarCenterX = prefs.getFloat("allyAvatarCenterX", default.allyAvatarCenterX),
                allyTenthAvatarCenterX = prefs.getFloat("allyTenthAvatarCenterX", default.allyTenthAvatarCenterX),
                enemyAvatarCenterX = prefs.getFloat("enemyAvatarCenterX", default.enemyAvatarCenterX),
                enemyTenthAvatarCenterX = prefs.getFloat("enemyTenthAvatarCenterX", default.enemyTenthAvatarCenterX),
                avatarDiameterRatio = prefs.getFloat("avatarDiameterRatio", default.avatarDiameterRatio),
                allySlotYRatios = allyY,
                enemySlotYRatios = enemyY,
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
