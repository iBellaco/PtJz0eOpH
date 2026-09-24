package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

    // Telemetría del motor de visión y estrategia de salto de fotogramas
    val liveScanFps by DraftVisionScanner.liveScanFps.collectAsStateWithLifecycle()
    val skippedFrames by DraftVisionScanner.framesSkippedCount.collectAsStateWithLifecycle()
    val lastDurationMs by DraftVisionScanner.lastProcessingDurationMs.collectAsStateWithLifecycle()
    val isBusy by DraftVisionScanner.isVisionEngineBusy.collectAsStateWithLifecycle()
    val isFirstPickAlly by DraftVisionScanner.isFirstPickState.collectAsStateWithLifecycle()

    // OPTIMIZACIÓN DE RENDIMIENTO (60-120 FPS):
    // Recordar pinturas nativas fuera de Canvas para eliminar asignaciones masivas de memoria
    // y evitar pausas de Garbage Collector (GC) que reducen la tasa de fotogramas del overlay.
    val allyTextPaint = remember(density) {
        android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#00E5FF")
            textSize = with(density) { 10.sp.toPx() }
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
        }
    }

    val enemyTextPaint = remember(density) {
        android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#FF5252")
            textSize = with(density) { 10.sp.toPx() }
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
        }
    }

    val allyLabelPaint = remember(density) {
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = with(density) { 9.5.sp.toPx() }
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.LEFT
            setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
        }
    }

    val enemyLabelPaint = remember(density) {
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = with(density) { 9.5.sp.toPx() }
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.RIGHT
            setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
        }
    }

    val labelPaint = remember(density) {
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = with(density) { 9.sp.toPx() }
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            setShadowLayer(3f, 0f, 0f, android.graphics.Color.BLACK)
        }
    }

    val tenthPickPaint = remember(density) {
        android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#FFD700")
            textSize = with(density) { 9.sp.toPx() }
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
        }
    }

    val hudPaint = remember(density) {
        android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#00E676")
            textSize = with(density) { 9.5.sp.toPx() }
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.LEFT
            setShadowLayer(3f, 0f, 0f, android.graphics.Color.BLACK)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0 || h <= 0) return@Canvas

        val currentConfig = AdaptiveScreenLayoutEngine.computeAdaptiveConfig(w.toInt(), h.toInt(), rawConfig)
        val avatarDiameter = h * currentConfig.avatarDiameterRatio
        val avatarRadius = avatarDiameter / 2f

        // SLOTS VERTICALES (LADO IZQUIERDO Y DERECHO) - AJUSTADOS PARA DRAFT REAL
        for (sIdx in 0..4) {
            // -------------------------------------------------------------
            // 1. Columna Aliada (Izquierda: Círculo de Avatar + Región OCR de Nombre)
            // -------------------------------------------------------------
            val allyY = h * currentConfig.allySlotYRatios.getOrElse(sIdx) { 0.188f + sIdx * 0.136f }
            val allyX = w * currentConfig.getAllySlotX(sIdx)
            val allyAvatarDiam = h * currentConfig.getSlotDiameter(true, sIdx)
            val allyAvatarRad = allyAvatarDiam / 2f
            
            // Círculo del avatar del slot aliado
            drawCircle(
                color = Color(0xCC00E5FF),
                center = Offset(allyX, allyY),
                radius = allyAvatarRad,
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
            val allyOcrTop = allyY - (allyAvatarDiam * 0.40f)
            val allyOcrHeight = allyAvatarDiam * 0.80f

            drawRoundRect(
                color = Color(0x8800E5FF),
                topLeft = Offset(allyOcrLeft, allyOcrTop),
                size = Size((allyOcrRight - allyOcrLeft).coerceAtLeast(10f), allyOcrHeight),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 1.2f)
            )

            // Etiqueta del slot / rol aliado dinámico: único 1 a 1 por equipo sin duplicados
            val detectedAllyRole = DraftVisionScanner.allySlotRolesCache[sIdx]?.shortName
                ?: DraftVisionScanner.allySlotOcrLaneCache[sIdx]?.shortName
            val slotLabel = if (detectedAllyRole != null) "Aliado ${sIdx + 1} ($detectedAllyRole)" else "Aliado ${sIdx + 1}"
            val allyLabelX = (allyX - allyAvatarRad).coerceAtLeast(8f)
            drawContext.canvas.nativeCanvas.drawText(
                slotLabel,
                allyLabelX,
                allyY - allyAvatarRad - 6f,
                allyLabelPaint
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
            val enemyX = w * currentConfig.getEnemySlotX(sIdx)
            val enemyAvatarDiam = h * currentConfig.getSlotDiameter(false, sIdx)
            val enemyAvatarRad = enemyAvatarDiam / 2f
            
            // Círculo del avatar del slot rival
            drawCircle(
                color = Color(0xCCFF1744),
                center = Offset(enemyX, enemyY),
                radius = enemyAvatarRad,
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
            val enemyOcrTop = enemyY - (enemyAvatarDiam * 0.40f)
            val enemyOcrHeight = enemyAvatarDiam * 0.80f

            drawRoundRect(
                color = Color(0x88FF1744),
                topLeft = Offset(enemyOcrLeft, enemyOcrTop),
                size = Size((enemyOcrRight - enemyOcrLeft).coerceAtLeast(10f), enemyOcrHeight),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 1.2f)
            )

            // Etiqueta del slot rival: en Wild Rift la línea del enemigo está oculta.
            // No se muestra línea por defecto, pero si ya hay un campeón detectado se infiere su rol primario o flex
            val enemyMatch = debugMatches["enemy_$sIdx"]
            val confirmedEnemy = DraftVisionScanner.enemySlotConfirmedChampions[sIdx]
            val enemyChamp = confirmedEnemy ?: enemyMatch?.let { name ->
                com.example.data.WildRiftRepository.getChampionByName(name)
            }

            val enemySlotLabel = if (enemyChamp != null) {
                val primary = enemyChamp.primaryRole.shortName
                val secondaries = enemyChamp.secondaryRoles.map { it.shortName }.filter { it != primary }
                if (secondaries.isNotEmpty()) {
                    val flexStr = (listOf(primary) + secondaries).distinct().joinToString("/")
                    "Rival ${sIdx + 1} ($flexStr • Flex)"
                } else {
                    "Rival ${sIdx + 1} ($primary)"
                }
            } else {
                "Rival ${sIdx + 1}"
            }

            val enemyLabelX = (enemyX + avatarRadius).coerceAtMost(w - 10f)
            drawContext.canvas.nativeCanvas.drawText(
                enemySlotLabel,
                enemyLabelX,
                enemyY - avatarRadius - 6f,
                enemyLabelPaint
            )

            if (enemyMatch != null) {
                drawContext.canvas.nativeCanvas.drawText(
                    enemyMatch,
                    (enemyOcrLeft + enemyOcrRight) / 2f,
                    enemyY + (with(density) { 4.sp.toPx() }),
                    enemyTextPaint
                )
            }
        }

        // -------------------------------------------------------------
        // 3. Círculo de Visión del 10º Pick (Dinámico según 1ª Selección: Aliado 5 o Rival 5)
        // -------------------------------------------------------------
        val tenthIsAlly = !isFirstPickAlly
        val tenthTargetY = if (tenthIsAlly) {
            h * currentConfig.allySlotYRatios.getOrElse(4) { 0.732f }
        } else {
            h * currentConfig.enemySlotYRatios.getOrElse(4) { 0.732f }
        }
        val tenthTargetX = if (tenthIsAlly) {
            w * currentConfig.allyAvatarCenterX
        } else {
            w * currentConfig.enemyAvatarCenterX
        }
        val tenthLabel = if (tenthIsAlly) "10º PICK (Aliado 5)" else "10º PICK (Rival 5)"

        drawCircle(
            color = Color(0xFFFFD700),
            center = Offset(tenthTargetX, tenthTargetY),
            radius = avatarRadius + 4f,
            style = Stroke(width = 3.0f)
        )
        drawContext.canvas.nativeCanvas.drawText(
            tenthLabel,
            tenthTargetX.coerceIn(52f, w - 52f),
            tenthTargetY + avatarRadius + 14f,
            tenthPickPaint
        )
        
        // 4. Límites del Asistente Flotante
        if (overlayRect != null) {
            drawRect(
                color = Color(0x55C89B3C),
                topLeft = Offset(overlayRect.left.toFloat(), overlayRect.top.toFloat()),
                size = Size(overlayRect.width().toFloat(), overlayRect.height().toFloat()),
                style = Stroke(width = 1.5f)
            )
        }

        // -------------------------------------------------------------
        // 5. Panel HUD de Rendimiento y Salto de Fotogramas (Frame Skipping)
        // -------------------------------------------------------------
        val hudText = String.format(
            java.util.Locale.US,
            "Motor Visión: %s | Tasa: %.1f Hz | Latencia: %dms | Fotogramas Saltados: %d",
            if (isBusy) "PROCESANDO" else "LISTO",
            liveScanFps,
            lastDurationMs,
            skippedFrames
        )
        val hudX = w * 0.12f
        val hudY = 24f

        drawRoundRect(
            color = Color(0xCC050A14),
            topLeft = Offset(hudX - 10f, 6f),
            size = Size(with(density) { 340.sp.toPx() }, 26f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawRoundRect(
            color = if (isBusy) Color(0xFFFFD700) else Color(0x8800E676),
            topLeft = Offset(hudX - 10f, 6f),
            size = Size(with(density) { 340.sp.toPx() }, 26f),
            cornerRadius = CornerRadius(6f, 6f),
            style = Stroke(width = 1f)
        )

        drawContext.canvas.nativeCanvas.drawText(
            hudText,
            hudX,
            hudY,
            hudPaint
        )
    }
}
