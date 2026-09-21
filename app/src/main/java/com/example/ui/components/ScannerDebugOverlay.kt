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
        val topDiameter = h * currentConfig.topAvatarDiameterRatio
        val topRadius = topDiameter / 2f
        val topY = h * currentConfig.topAvatarYRatio
        
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
            val allyX = w * currentConfig.allyAvatarCenterX
            
            drawCircle(
                color = Color(0x9900B0FF),
                center = Offset(allyX, allyY),
                radius = avatarDiameter / 2f,
                style = Stroke(width = 2.0f)
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
                color = Color(0x99FF1744),
                center = Offset(enemyX, enemyY),
                radius = avatarDiameter / 2f,
                style = Stroke(width = 2.0f)
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

        // 2. CÍRCULOS SUPERIORES DIRECTOS EN PANTALLA (LOS 10 CAMPEONES EN LA BARRA SUPERIOR)
        // Aliados Superiores (5 Círculos en Top-Left: Índices 0..4)
        for (idx in 0..4) {
            val topAllyX = w * currentConfig.topAllyXRatios.getOrElse(idx) { 0.028f + idx * 0.035f }
            
            drawCircle(
                color = Color(0xCC00B0FF),
                center = Offset(topAllyX, topY),
                radius = topRadius,
                style = Stroke(width = 2.0f)
            )

            val match = debugMatches["top_ally_$idx"]
            if (match != null) {
                drawContext.canvas.nativeCanvas.drawText(
                    match,
                    topAllyX,
                    topY + topRadius + 14f,
                    allyTextPaint
                )
            }
        }

        // Rivales Superiores (5 Círculos en Top-Right: Índices 0..4)
        for (idx in 0..4) {
            val topEnemyX = w * currentConfig.topEnemyXRatios.getOrElse(idx) { 0.832f + idx * 0.035f }
            
            drawCircle(
                color = Color(0xCCFF1744),
                center = Offset(topEnemyX, topY),
                radius = topRadius,
                style = Stroke(width = 2.0f)
            )

            val match = debugMatches["top_enemy_$idx"]
            if (match != null) {
                drawContext.canvas.nativeCanvas.drawText(
                    match,
                    topEnemyX,
                    topY + topRadius + 14f,
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
