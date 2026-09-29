package com.example.service.screen

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import com.example.util.AppLogger
import kotlin.math.max
import kotlin.math.min

/**
 * Módulo de Pre-procesamiento de Imagen para Motores de Visión por Computadora (reconocimiento visual local y OCR).
 * 
 * Aplica:
 * 1. Conversión de alta fidelidad a escala de grises por luminancia perceptual.
 * 2. Umbral adaptativo (Adaptive Thresholding) con ventana integral rápida para binarizar texto sobre fondos complejos.
 * 3. Filtrado y supresión de contornos pequeños e iconos de maestría al inicio de las áreas de texto de los slots.
 */
object DraftImagePreprocessor {

    private const val TAG = "DraftImagePreprocessor"

    /**
     * Pre-procesa una captura de pantalla completa para aislar y maximizar la legibilidad
     * del texto de campeones y roles, suprimiendo activamente los iconos de maestría
     * e insignias gráficas antes de alimentar el motor OCR y reconocimiento visual local.
     */
    fun preprocessForOcr(
        sourceBitmap: Bitmap,
        calib: VisionCalibrationConfig = VisionCalibrationConfig()
    ): Bitmap {
        val width = sourceBitmap.width
        val height = sourceBitmap.height
        if (width <= 0 || height <= 0) return sourceBitmap

        val outputBitmap = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val pixels = IntArray(width * height)
        outputBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val slotHeight = (height * 0.135f).toInt().coerceAtLeast(20)
        val allyMinX = (width * (calib.allyOcrMinX - 0.02f)).toInt().coerceIn(0, width - 1)
        val allyMaxX = (width * (calib.allyOcrMaxX + 0.03f)).toInt().coerceIn(0, width - 1)
        val enemyMinX = (width * (calib.enemyOcrMinX - 0.03f)).toInt().coerceIn(0, width - 1)
        val enemyMaxX = (width * (calib.enemyOcrMaxX + 0.02f)).toInt().coerceIn(0, width - 1)

        // Pre-procesar cada uno de los 5 slots aliados
        for (s in 0..4) {
            val centerY = (height * calib.allySlotYRatios[s]).toInt()
            val startY = (centerY - slotHeight / 2).coerceIn(0, height - 1)
            val endY = (centerY + slotHeight / 2).coerceIn(0, height - 1)
            val rect = Rect(allyMinX, startY, allyMaxX, endY)
            processTextRegion(pixels, width, height, rect, isAlly = true)
        }

        // Pre-procesar cada uno de los 5 slots rivales
        for (s in 0..4) {
            val centerY = (height * calib.enemySlotYRatios[s]).toInt()
            val startY = (centerY - slotHeight / 2).coerceIn(0, height - 1)
            val endY = (centerY + slotHeight / 2).coerceIn(0, height - 1)
            val rect = Rect(enemyMinX, startY, enemyMaxX, endY)
            processTextRegion(pixels, width, height, rect, isAlly = false)
        }

        outputBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return outputBitmap
    }

    /**
     * Procesa una región rectangular de texto: escala de grises, umbral adaptativo
     * y supresión de componentes conexos correspondientes a iconos de maestría.
     */
    private fun processTextRegion(
        pixels: IntArray,
        imgWidth: Int,
        imgHeight: Int,
        region: Rect,
        isAlly: Boolean
    ) {
        val rx0 = max(0, region.left)
        val rx1 = min(imgWidth - 1, region.right)
        val ry0 = max(0, region.top)
        val ry1 = min(imgHeight - 1, region.bottom)
        val rWidth = rx1 - rx0 + 1
        val rHeight = ry1 - ry0 + 1
        if (rWidth <= 4 || rHeight <= 4) return

        // 1. Escala de grises (Luminancia)
        val gray = IntArray(rWidth * rHeight)
        for (y in 0 until rHeight) {
            val py = ry0 + y
            for (x in 0 until rWidth) {
                val px = rx0 + x
                val c = pixels[py * imgWidth + px]
                val r = Color.red(c)
                val g = Color.green(c)
                val b = Color.blue(c)
                // Luminancia perceptual ITU-R BT.601
                val lum = ((r * 299 + g * 587 + b * 114) / 1000).coerceIn(0, 255)
                gray[y * rWidth + x] = lum
            }
        }

        // 2. Imagen Integral para Umbral Adaptativo (Bradley-Roth / Mean)
        val integral = LongArray(rWidth * rHeight)
        for (y in 0 until rHeight) {
            var sum = 0L
            for (x in 0 until rWidth) {
                sum += gray[y * rWidth + x]
                if (y == 0) {
                    integral[x] = sum
                } else {
                    integral[y * rWidth + x] = integral[(y - 1) * rWidth + x] + sum
                }
            }
        }

        // Ventana local adaptativa proporcional al tamaño de fuente (~20% de la altura del slot)
        val s = (rHeight * 0.28f).toInt().coerceIn(5, 25)
        val t = 10 // Factor de sensibilidad / compensación sobre fondo oscuro
        val binary = ByteArray(rWidth * rHeight)

        for (y in 0 until rHeight) {
            val y1 = max(0, y - s)
            val y2 = min(rHeight - 1, y + s)
            for (x in 0 until rWidth) {
                val x1 = max(0, x - s)
                val x2 = min(rWidth - 1, x + s)
                val count = (x2 - x1 + 1) * (y2 - y1 + 1)

                val bottom = integral[y2 * rWidth + x2]
                val top = if (y1 > 0) integral[(y1 - 1) * rWidth + x2] else 0L
                val left = if (x1 > 0) integral[y2 * rWidth + (x1 - 1)] else 0L
                val topLeft = if (x1 > 0 && y1 > 0) integral[(y1 - 1) * rWidth + (x1 - 1)] else 0L
                val localSum = bottom - top - left + topLeft

                val lum = gray[y * rWidth + x]
                // Binarización: se activa si supera el umbral local relativo y tiene una luminancia base mínima
                if (lum * count >= (localSum * (100 - t) / 100) && lum > 45) {
                    binary[y * rWidth + x] = 1 // Primer plano (texto / trazo)
                } else {
                    binary[y * rWidth + x] = 0 // Fondo
                }
            }
        }

        // 3. Supresión de Iconos de Maestría y Contornos Pequeños:
        // En los slots aliados, el icono de maestría se sitúa a la izquierda del nombre (primer 25% del ancho de la región)
        // En los slots rivales, si aparece a la izquierda o derecha, se evalúa la zona inicial de la cadena.
        val masteryZoneWidth = (rWidth * 0.26f).toInt()
        val visited = BooleanArray(rWidth * rHeight)

        // Etiquetado de componentes conexos en la zona de maestría inicial
        for (y in 0 until rHeight) {
            for (x in 0 until masteryZoneWidth) {
                val idx = y * rWidth + x
                if (binary[idx] == 1.toByte() && !visited[idx]) {
                    // Flood fill BFS para encontrar el contorno completo
                    val blobPixels = mutableListOf<Int>()
                    var minBlobX = x
                    var maxBlobX = x
                    var minBlobY = y
                    var maxBlobY = y

                    val queue = ArrayDeque<Int>()
                    queue.add(idx)
                    visited[idx] = true

                    while (queue.isNotEmpty()) {
                        val curr = queue.removeFirst()
                        blobPixels.add(curr)
                        val cx = curr % rWidth
                        val cy = curr / rWidth

                        if (cx < minBlobX) minBlobX = cx
                        if (cx > maxBlobX) maxBlobX = cx
                        if (cy < minBlobY) minBlobY = cy
                        if (cy > maxBlobY) maxBlobY = cy

                        // Vecinos 4-conectados
                        val neighbors = arrayOf(
                            Pair(cx - 1, cy),
                            Pair(cx + 1, cy),
                            Pair(cx, cy - 1),
                            Pair(cx, cy + 1)
                        )
                        for ((nx, ny) in neighbors) {
                            if (nx in 0 until rWidth && ny in 0 until rHeight) {
                                val nIdx = ny * rWidth + nx
                                if (binary[nIdx] == 1.toByte() && !visited[nIdx]) {
                                    visited[nIdx] = true
                                    queue.add(nIdx)
                                }
                            }
                        }
                    }

                    val blobW = maxBlobX - minBlobX + 1
                    val blobH = maxBlobY - minBlobY + 1
                    val blobArea = blobPixels.size

                    // Criterio de icono de maestría / insignias pequeñas al inicio:
                    // Se ubica al extremo izquierdo de la zona de texto (minBlobX < masteryZoneWidth * 0.7f),
                    // su tamaño es característico de un icono/ala (ancho <= 38px o altura <= rHeight * 0.75f)
                    // y no está pegado a letras continuas grandes del centro de la cadena.
                    val isMasteryIconCandidate = (minBlobX < masteryZoneWidth * 0.85f && maxBlobX < masteryZoneWidth) &&
                            (blobArea in 4..650) &&
                            (blobW <= (rWidth * 0.22f) && blobH <= (rHeight * 0.85f))

                    if (isMasteryIconCandidate) {
                        // Suprimir el icono pintando sus píxeles como fondo (0)
                        for (p in blobPixels) {
                            binary[p] = 0
                        }
                    }
                }
            }
        }

        // 4. Volcar la imagen binarizada y limpia de vuelta al array de píxeles
        for (y in 0 until rHeight) {
            val py = ry0 + y
            for (x in 0 until rWidth) {
                val px = rx0 + x
                val b = binary[y * rWidth + x]
                pixels[py * imgWidth + px] = if (b == 1.toByte()) {
                    Color.WHITE
                } else {
                    Color.BLACK
                }
            }
        }
    }
}
