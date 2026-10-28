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
    val detectingBans by DraftVisionScanner.banDetectionActiveFlow.collectAsStateWithLifecycle()
    val banSlots by DraftVisionScanner.banSlotsFlow.collectAsStateWithLifecycle()
    val rawConfig by DraftVisionScanner.alignedConfigFlow.collectAsStateWithLifecycle()
    val debugMatches by DraftVisionScanner.debugVisualMatches.collectAsStateWithLifecycle()
    val language = com.example.util.currentAppLanguage()
    val density = LocalDensity.current

    // Telemetría del motor de visión y estrategia de salto de fotogramas
    val liveScanFps by DraftVisionScanner.liveScanFps.collectAsStateWithLifecycle()
    val skippedFrames by DraftVisionScanner.framesSkippedCount.collectAsStateWithLifecycle()
    val lastDurationMs by DraftVisionScanner.lastProcessingDurationMs.collectAsStateWithLifecycle()
    val isBusy by DraftVisionScanner.isVisionEngineBusy.collectAsStateWithLifecycle()
    val automaticScanActive by com.example.service.screen.TenthPickDiagnosticManager.automaticScanActiveFlow.collectAsStateWithLifecycle()
    val isFirstPickAlly by DraftVisionScanner.isFirstPickState.collectAsStateWithLifecycle()

    // OPTIMIZACIÓN DE RENDIMIENTO (60-120 FPS):
    // Recordar pinturas nativas fuera de Canvas para eliminar asignaciones masivas de memoria
    // y evitar pausas de Garbage Collector (GC) que reducen la tasa de fotogramas del overlay.

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

    // Keep the first row's diagnostic caption out of the header ban labels.
    val firstAllyLabelPaint = remember(density) {
        android.graphics.Paint(allyLabelPaint).apply { textSize = with(density) { 8.sp.toPx() } }
    }
    val firstEnemyLabelPaint = remember(density) {
        android.graphics.Paint(enemyLabelPaint).apply { textSize = with(density) { 8.sp.toPx() } }
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

        val textRects = mutableListOf<android.graphics.Rect>()
        allyLabelPaint.textSize = minOf(with(density) { 9.5.sp.toPx() }, h * .018f)
        enemyLabelPaint.textSize = allyLabelPaint.textSize
        firstAllyLabelPaint.textSize = minOf(with(density) { 8.sp.toPx() }, h * .014f)
        firstEnemyLabelPaint.textSize = firstAllyLabelPaint.textSize
        labelPaint.textSize = minOf(with(density) { 9.sp.toPx() }, h * .015f)
        fun caption(text: String, paint: android.graphics.Paint, maxWidth: Float): String {
            if (paint.measureText(text) <= maxWidth) return text
            var fitted = text
            while (fitted.isNotEmpty() && paint.measureText("$fitted…") > maxWidth) fitted = fitted.dropLast(1)
            return "$fitted…"
        }
        fun drawDiagnosticText(rawText: String, x: Float, y: Float, paint: android.graphics.Paint, margin: Float = 5f) {
            val text = com.example.util.appTr(rawText)
            val width = paint.measureText(text)
            val left = when (paint.textAlign) {
                android.graphics.Paint.Align.CENTER -> x - width / 2f
                android.graphics.Paint.Align.RIGHT -> x - width
                else -> x
            }
            val metrics = paint.fontMetrics
            textRects.add(android.graphics.Rect(
                (left - margin).toInt(), (y + metrics.top - margin).toInt(),
                (left + width + margin).toInt(), (y + metrics.bottom + margin).toInt()
            ))
            drawContext.canvas.nativeCanvas.drawText(text, x, y, paint)
        }

        // Already resolved against the captured viewport. A second adaptation moves
        // the header away from the actual regions used by recognition.
        val currentConfig = rawConfig

        // The rectangles are the exact header bands searched by the classifier.
        // Thin circle strokes stay outside its portrait descriptor samples.
        if (detectingBans) {
            val sideWidth = com.example.service.screen.banHeaderSideWidthRatio(w / h)
            for (side in com.example.service.screen.BanSide.entries) {
                val x = if (side == com.example.service.screen.BanSide.ALLY) 0f else w * (1f - sideWidth)
                val color = if (side == com.example.service.screen.BanSide.ALLY) Color(0xCC00E5FF) else Color(0xCCFF1744)
                drawRect(color.copy(alpha = .35f), Offset(x, 0f), Size(w * sideWidth, h * .12f), style = Stroke(1f))
                for (slot in banSlots.filter { it.side == side }) {
                    val region = slot.region ?: continue
                    val center = Offset(region.x * w, region.y * h)
                    val radius = region.radius * h
                    drawCircle(if (slot.state == com.example.service.screen.BanState.EMPTY) Color.Gray else color,
                        radius, center, style = Stroke(if (slot.state == com.example.service.screen.BanState.CONFIRMED) 2f else 1f))
                    val status = when (slot.state) {
                        com.example.service.screen.BanState.CONFIRMED ->
                            com.example.data.WildRiftRepository.champions.firstOrNull { it.id == slot.championId }?.getLocalizedName(language) ?: "Ban ${slot.index + 1}"
                        com.example.service.screen.BanState.EMPTY -> com.example.util.appTr("Sin ban")
                        else -> "Ban ${slot.index + 1}"
                    }
                    val halfText = labelPaint.measureText(status) / 2f
                    drawDiagnosticText(caption(status, labelPaint, radius * 2f + 4f), center.x.coerceIn(halfText + 2f, w - halfText - 2f),
                        center.y + radius - labelPaint.fontMetrics.top + 6f, labelPaint, margin = 1f)
                }
            }
        }

        // SLOTS VERTICALES (LADO IZQUIERDO Y DERECHO) - AJUSTADOS PARA DRAFT REAL
        for (sIdx in 0..4) {
            // -------------------------------------------------------------
            // 1. Columna Aliada (Izquierda: Círculo de Avatar + Región OCR de Nombre)
            // -------------------------------------------------------------
            val allyCrop = AdaptiveScreenLayoutEngine.calculateSlotCropRect(w.toInt(), h.toInt(), true, sIdx, currentConfig)
            val allyY = allyCrop.exactCenterY()
            val allyX = allyCrop.exactCenterX()
            val allyAvatarRad = allyCrop.width() / 2f

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
            val allyNameRect = AdaptiveScreenLayoutEngine.calculateSlotNameRect(
                w.toInt(), h.toInt(), true, sIdx, currentConfig
            )
            val allyOcrLeft = allyNameRect.left.toFloat()
            val allyOcrRight = allyNameRect.right.toFloat()
            val allyOcrTop = allyNameRect.top.toFloat()
            val allyOcrHeight = allyNameRect.height().toFloat()

            drawRoundRect(
                color = Color(0x8800E5FF),
                topLeft = Offset(allyOcrLeft, allyOcrTop),
                size = Size((allyOcrRight - allyOcrLeft).coerceAtLeast(10f), allyOcrHeight),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 1.2f)
            )

            // Same extra title band used by OCR before the allied spell column
            // appears. Portrait circles remain selection targets, not lane ROIs.
            val laneRect = AdaptiveScreenLayoutEngine.calculateAllyLaneRect(w.toInt(), h.toInt(), sIdx, currentConfig)
            drawRect(color = Color(0x6600E5FF),
                topLeft = Offset(laneRect.left.toFloat(), laneRect.top.toFloat()),
                size = Size(laneRect.width().toFloat(), laneRect.height().toFloat()),
                style = Stroke(width = 1f))

            // Etiqueta del slot / rol aliado dinámico: único 1 a 1 por equipo sin duplicados
            val slotLabel = "Aliado ${sIdx + 1}"
            val allyLabelX = (allyX - allyAvatarRad).coerceAtLeast(8f)
            val allyMatch = debugMatches["ally_$sIdx"]
            val belowHeader = detectingBans && sIdx == 0
            val allyCaptionPaint = if (belowHeader) firstAllyLabelPaint else allyLabelPaint
            drawDiagnosticText(
                caption(if (allyMatch != null) "$slotLabel: $allyMatch" else slotLabel, allyCaptionPaint, allyNameRect.width().toFloat()),
                if (belowHeader) allyOcrLeft else allyLabelX,
                if (belowHeader) allyOcrTop - allyCaptionPaint.fontMetrics.top + 1f else allyY - allyAvatarRad - 6f,
                allyCaptionPaint, margin = if (belowHeader) 1f else 5f
            )


            // -------------------------------------------------------------
            // 2. Columna Rival (Derecha: Círculo de Avatar + Región OCR)
            // -------------------------------------------------------------
            val enemyCrop = AdaptiveScreenLayoutEngine.calculateSlotCropRect(w.toInt(), h.toInt(), false, sIdx, currentConfig)
            val enemyY = enemyCrop.exactCenterY()
            val enemyX = enemyCrop.exactCenterX()
            val enemyAvatarRad = enemyCrop.width() / 2f

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
            val enemyNameRect = AdaptiveScreenLayoutEngine.calculateSlotNameRect(
                w.toInt(), h.toInt(), false, sIdx, currentConfig
            )
            val enemyOcrLeft = enemyNameRect.left.toFloat()
            val enemyOcrRight = enemyNameRect.right.toFloat()
            val enemyOcrTop = enemyNameRect.top.toFloat()
            val enemyOcrHeight = enemyNameRect.height().toFloat()

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
            val enemySlotLabel = "Rival ${sIdx + 1}"

            val enemyLabelX = (enemyX + enemyAvatarRad).coerceAtMost(w - 10f)
            val enemyCaptionPaint = if (belowHeader) firstEnemyLabelPaint else enemyLabelPaint
            drawDiagnosticText(
                caption(if (enemyMatch != null) "$enemySlotLabel: $enemyMatch" else enemySlotLabel, enemyCaptionPaint, enemyNameRect.width().toFloat()),
                if (belowHeader) enemyOcrRight else enemyLabelX,
                if (belowHeader) enemyOcrTop - enemyCaptionPaint.fontMetrics.top + 1f else enemyY - enemyAvatarRad - 6f,
                enemyCaptionPaint, margin = if (belowHeader) 1f else 5f
            )

        }

        // -------------------------------------------------------------
        // 3. Círculo de Visión del 10º Pick (Dinámico según 1ª Selección: Aliado 5 o Rival 5)
        // -------------------------------------------------------------
        isFirstPickAlly?.let { firstAlly ->
        val tenthIsAlly = !firstAlly
        val tenthCrop = AdaptiveScreenLayoutEngine.calculateSlotCropRect(w.toInt(), h.toInt(), tenthIsAlly, 4, currentConfig)
        val tenthTargetY = tenthCrop.exactCenterY()
        val tenthTargetX = tenthCrop.exactCenterX()
        val avatarRadius = tenthCrop.width() / 2f
        val tenthLabel = if (tenthIsAlly) "10º PICK (Aliado 5)" else "10º PICK (Rival 5)"

        drawCircle(
            color = Color(0xFFFFD700),
            center = Offset(tenthTargetX, tenthTargetY),
            radius = avatarRadius + 4f,
            style = Stroke(width = 3.0f)
        )
        drawDiagnosticText(
            tenthLabel,
            tenthTargetX.coerceIn(52f, w - 52f),
            tenthTargetY + avatarRadius + 14f,
            tenthPickPaint
        )
        }

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
            if (isBusy) "PROCESANDO" else if (automaticScanActive) "LISTO" else "PAUSADO",
            liveScanFps,
            lastDurationMs,
            skippedFrames
        )
        val hudX = w * 0.29f
        val hudWidth = w * .42f
        hudPaint.textSize = with(density) { 9.5.sp.toPx() }
        val measuredHud = hudPaint.measureText(com.example.util.appTr(hudText))
        if (measuredHud > hudWidth - 12f) hudPaint.textSize *= (hudWidth - 12f) / measuredHud
        val hudY = 24f

        drawRoundRect(
            color = Color(0xCC050A14),
            topLeft = Offset(hudX - 4f, 6f),
            size = Size(hudWidth, 26f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawRoundRect(
            color = if (isBusy) Color(0xFFFFD700) else Color(0x8800E676),
            topLeft = Offset(hudX - 4f, 6f),
            size = Size(hudWidth, 26f),
            cornerRadius = CornerRadius(6f, 6f),
            style = Stroke(width = 1f)
        )

        drawDiagnosticText(
            hudText,
            hudX,
            hudY,
            hudPaint
        )
        DraftVisionScanner.publishScreenDiagnosticText(textRects, w.toInt(), h.toInt())
    }
}
