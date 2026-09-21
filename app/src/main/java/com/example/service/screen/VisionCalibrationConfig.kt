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
    val enemyAvatarCenterX: Float = 0.960f,

    // Diámetro del avatar relativo al alto de pantalla (0..1)
    val avatarDiameterRatio: Float = 0.114f,

    // Ratios verticales Y para los 5 slots aliados (0..4)
    val allySlotYRatios: List<Float> = listOf(
        0.196f,
        0.329f,
        0.465f,
        0.603f,
        0.739f
    ),

    // Ratios verticales Y para los 5 slots enemigos (0..4)
    val enemySlotYRatios: List<Float> = listOf(
        0.196f,
        0.329f,
        0.465f,
        0.603f,
        0.739f
    ),

    // Hechizos de invocador aliados
    val spellLeftRatio: Float = 0.021f,
    val spellSizeRatio: Float = 0.041f,
    val spellYOffsetRatio: Float = 0.0f,

    // Rango horizontal OCR para columnas de draft (restringido estrictamente a las columnas de slots, sin tocar el centro)
    val allyOcrMinX: Float = 0.08f,
    val allyOcrMaxX: Float = 0.225f,
    val enemyOcrMinX: Float = 0.78f,
    val enemyOcrMaxX: Float = 0.935f,

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
        sb.append("• Centro X Aliados (Slots): ${(allyAvatarCenterX * 100).format(2)}% (${allyAvatarCenterX}f)\n")
        sb.append("• Centro X Rivales (Slots): ${(enemyAvatarCenterX * 100).format(2)}% (${enemyAvatarCenterX}f)\n\n")
        
        sb.append("• Círculos Superiores (Top Bar / 10º Pick):\n")
        sb.append("  - Altura Y: ${(topAvatarYRatio * 100).format(2)}% (${topAvatarYRatio}f)\n")
        sb.append("  - Diámetro (⌀): ${(topAvatarDiameterRatio * 100).format(2)}% (${topAvatarDiameterRatio}f)\n")
        sb.append("  - Top 10º Pick Rival (5º Rival X): ${(topEnemy5XRatio * 100).format(2)}% (${topEnemy5XRatio}f)\n")
        sb.append("  - Top 10º Pick Aliado (5º Aliado X): ${(topAlly5XRatio * 100).format(2)}% (${topAlly5XRatio}f)\n\n")

        sb.append("• Slots Aliados Verticales Y:\n")
        val roles = listOf("TOP", "JUNGLE", "MID", "ADC", "SUPPORT")
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
    enemyAvatarCenterX = ${enemyAvatarCenterX}f,
    avatarDiameterRatio = ${avatarDiameterRatio}f,
    allySlotYRatios = listOf(${allySlotYRatios.joinToString(", ") { "${it}f" }}),
    enemySlotYRatios = listOf(${enemySlotYRatios.joinToString(", ") { "${it}f" }}),
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
        private const val CURRENT_CALIBRATION_VERSION = 6

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
                // Actualizar automáticamente a la calibración oficial Wild Rift 1:1
                val allyY = (0..4).map { idx -> prefs.getFloat("ally_slot_y_$idx", default.allySlotYRatios[idx]) }
                val enemyY = (0..4).map { idx -> prefs.getFloat("enemy_slot_y_$idx", default.enemySlotYRatios[idx]) }
                
                val migrated = VisionCalibrationConfig(
                    allyAvatarCenterX = prefs.getFloat("allyAvatarCenterX", default.allyAvatarCenterX),
                    enemyAvatarCenterX = prefs.getFloat("enemyAvatarCenterX", default.enemyAvatarCenterX),
                    avatarDiameterRatio = prefs.getFloat("avatarDiameterRatio", default.avatarDiameterRatio),
                    allySlotYRatios = allyY,
                    enemySlotYRatios = enemyY,
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
            val topAllyX = (0..4).map { idx -> prefs.getFloat("top_ally_x_$idx", default.topAllyXRatios[idx]) }
            val topEnemyX = (0..4).map { idx -> prefs.getFloat("top_enemy_x_$idx", default.topEnemyXRatios[idx]) }

            return VisionCalibrationConfig(
                allyAvatarCenterX = prefs.getFloat("allyAvatarCenterX", default.allyAvatarCenterX),
                enemyAvatarCenterX = prefs.getFloat("enemyAvatarCenterX", default.enemyAvatarCenterX),
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
