package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.screen.DraftVisionScanner
import com.example.service.screen.VisionCalibrationConfig

@Composable
fun ScannerDebugOverlay(
    config: VisionCalibrationConfig = DraftVisionScanner.calibrationConfigFlow.collectAsStateWithLifecycle().value,
    overlayRect: android.graphics.Rect?
) {
    val currentConfig by DraftVisionScanner.calibrationConfigFlow.collectAsStateWithLifecycle()
    val debugMatches by DraftVisionScanner.debugVisualMatches.collectAsStateWithLifecycle()
    val density = LocalDensity.current
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        val avatarDiameter = h * currentConfig.avatarDiameterRatio
        
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

        // 1. SLOTS VERTICALES (LADO IZQUIERDO Y DERECHO)
        for (sIdx in 0..4) {
            // Columna Aliada (Izquierda)
            val allyY = h * currentConfig.allySlotYRatios.getOrElse(sIdx) { 0.2f + sIdx * 0.13f }
            // Para el 10º pick del lado aliado (Slot 5 / sIdx == 4), se ubica desplazado un poco a la izquierda
            val allyX = w * if (sIdx == 4) currentConfig.allyTenthAvatarCenterX else currentConfig.allyAvatarCenterX
            
            drawCircle(
                color = if (sIdx == 4) Color(0xCC00E5FF) else Color(0x9900B0FF),
                center = Offset(allyX, allyY),
                radius = avatarDiameter / 2f,
                style = Stroke(width = if (sIdx == 4) 2.5f else 2.0f)
            )
            
            val allyMatch = debugMatches["ally_$sIdx"]
            if (allyMatch != null) {
                drawContext.canvas.nativeCanvas.drawText(
                    allyMatch,
                    allyX,
                    allyY - avatarDiameter / 2 - 6f,
                    allyTextPaint
                )
            }
            
            // Columna Rival (Derecha)
            val enemyY = h * currentConfig.enemySlotYRatios.getOrElse(sIdx) { 0.2f + sIdx * 0.13f }
            val enemyX = w * currentConfig.enemyAvatarCenterX
            
            drawCircle(
                color = if (sIdx == 4) Color(0xFFFF5252) else Color(0x99FF1744),
                center = Offset(enemyX, enemyY),
                radius = avatarDiameter / 2f,
                style = Stroke(width = if (sIdx == 4) 2.5f else 2.0f)
            )
            
            val enemyMatch = debugMatches["enemy_$sIdx"]
            if (enemyMatch != null) {
                drawContext.canvas.nativeCanvas.drawText(
                    enemyMatch,
                    enemyX,
                    enemyY - avatarDiameter / 2 - 6f,
                    enemyTextPaint
                )
            }
        }
        
        // 2. Límites del Asistente Flotante
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
