package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.screen.AdaptiveScreenLayoutEngine
import com.example.service.screen.DraftVisionScanner
import com.example.service.screen.VisionCalibrationConfig

@Composable
fun ScannerDebugOverlay(
    config: VisionCalibrationConfig = DraftVisionScanner.calibrationConfigFlow.collectAsStateWithLifecycle().value,
    overlayRect: android.graphics.Rect?
) {
    val rawConfig by DraftVisionScanner.calibrationConfigFlow.collectAsStateWithLifecycle()
    val debugMatches by DraftVisionScanner.debugVisualMatches.collectAsStateWithLifecycle()
    val density = LocalDensity.current
    
    val allyRoles = listOf("TOP", "JUNGLE", "MID", "ADC", "SUPPORT")

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0 || h <= 0) return@Canvas

        val currentConfig = AdaptiveScreenLayoutEngine.computeAdaptiveConfig(w.toInt(), h.toInt(), rawConfig)
        val avatarDiameter = h * currentConfig.avatarDiameterRatio
        val avatarRadius = avatarDiameter / 2f
        
        val allyTextPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#00E5FF")
            textSize = with(density) { 10.sp.toPx() }
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
        }

        val enemyTextPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#FF5252")
            textSize = with(density) { 10.sp.toPx() }
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
        }

        val labelPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = with(density) { 9.sp.toPx() }
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            setShadowLayer(3f, 0f, 0f, android.graphics.Color.BLACK)
        }

        // SLOTS VERTICALES (LADO IZQUIERDO Y DERECHO) - AJUSTADOS PARA DRAFT REAL
        for (sIdx in 0..4) {
            // -------------------------------------------------------------
            // 1. Columna Aliada (Izquierda: Círculo de Avatar + Región OCR de Nombre)
            // -------------------------------------------------------------
            val allyY = h * currentConfig.allySlotYRatios.getOrElse(sIdx) { 0.188f + sIdx * 0.136f }
            val allyX = w * currentConfig.allyAvatarCenterX
            
            // Círculo del avatar del slot aliado (ajustado en x ≈ 0.073f)
            drawCircle(
                color = Color(0xCC00E5FF),
                center = Offset(allyX, allyY),
                radius = avatarRadius,
                style = Stroke(width = 2.5f)
            )

            // Centro del círculo de avatar
            drawCircle(
                color = Color(0x8800E5FF),
                center = Offset(allyX, allyY),
                radius = 3.5f
            )

            // Región de detección OCR para el nombre de campeón y carril aliado
            val allyOcrLeft = w * currentConfig.allyOcrMinX
            val allyOcrRight = w * currentConfig.allyOcrMaxX
            val allyOcrTop = allyY - (avatarDiameter * 0.40f)
            val allyOcrHeight = avatarDiameter * 0.80f

            drawRoundRect(
                color = Color(0x8800E5FF),
                topLeft = Offset(allyOcrLeft, allyOcrTop),
                size = Size((allyOcrRight - allyOcrLeft).coerceAtLeast(10f), allyOcrHeight),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 1.2f)
            )

            // Etiqueta del slot / rol aliado
            val allyRoleLabel = allyRoles.getOrElse(sIdx) { "S${sIdx + 1}" }
            drawContext.canvas.nativeCanvas.drawText(
                "Slot ${sIdx + 1} ($allyRoleLabel)",
                allyX,
                allyY - avatarRadius - 6f,
                labelPaint
            )
            
            val allyMatch = debugMatches["ally_$sIdx"]
            if (allyMatch != null) {
                drawContext.canvas.nativeCanvas.drawText(
                    allyMatch,
                    (allyOcrLeft + allyOcrRight) / 2f,
                    allyY + (with(density) { 4.sp.toPx() }),
                    allyTextPaint
                )
            }
            
            // -------------------------------------------------------------
            // 2. Columna Rival (Derecha: Círculo de Avatar + Región OCR)
            // -------------------------------------------------------------
            val enemyY = h * currentConfig.enemySlotYRatios.getOrElse(sIdx) { 0.188f + sIdx * 0.136f }
            val enemyX = w * currentConfig.enemyAvatarCenterX
            
            // Círculo del avatar del slot rival (en x ≈ 0.960f)
            drawCircle(
                color = Color(0xCCFF1744),
                center = Offset(enemyX, enemyY),
                radius = avatarRadius,
                style = Stroke(width = 2.5f)
            )

            // Centro del círculo de avatar
            drawCircle(
                color = Color(0x88FF1744),
                center = Offset(enemyX, enemyY),
                radius = 3.5f
            )

            // Región OCR para nombre de rival
            val enemyOcrLeft = w * currentConfig.enemyOcrMinX
            val enemyOcrRight = w * currentConfig.enemyOcrMaxX
            val enemyOcrTop = enemyY - (avatarDiameter * 0.40f)
            val enemyOcrHeight = avatarDiameter * 0.80f

            drawRoundRect(
                color = Color(0x88FF1744),
                topLeft = Offset(enemyOcrLeft, enemyOcrTop),
                size = Size((enemyOcrRight - enemyOcrLeft).coerceAtLeast(10f), enemyOcrHeight),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 1.2f)
            )

            // Etiqueta del slot rival
            drawContext.canvas.nativeCanvas.drawText(
                "Rival ${sIdx + 1}",
                enemyX,
                enemyY - avatarRadius - 6f,
                labelPaint
            )

            val enemyMatch = debugMatches["enemy_$sIdx"]
            if (enemyMatch != null) {
                drawContext.canvas.nativeCanvas.drawText(
                    enemyMatch,
                    (enemyOcrLeft + enemyOcrRight) / 2f,
                    enemyY + (with(density) { 4.sp.toPx() }),
                    enemyTextPaint
                )
            }
        }
        
        // 3. Límites del Asistente Flotante
        if (overlayRect != null) {
            drawRect(
                color = Color(0x55C89B3C),
                topLeft = Offset(overlayRect.left.toFloat(), overlayRect.top.toFloat()),
                size = Size(overlayRect.width().toFloat(), overlayRect.height().toFloat()),
                style = Stroke(width = 1.5f)
            )
        }
    }
}
