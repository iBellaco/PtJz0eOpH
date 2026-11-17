package com.example.service.screen

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import kotlin.math.max
import kotlin.math.min

/**
 * Creates a bounded administrator-only evidence image from an actual captured
 * screen frame. The frame can be the literal MediaProjection capture taken at
 * send time or the scanner's last processed frame when screen capture is not
 * available. In both cases the calibration geometry is drawn on top so the
 * reviewer can compare the saved coordinates with what the visor used.
 */
internal object CalibrationEvidenceImage {
    private const val MAX_WIDTH = 1280
    private const val MAX_BYTES = 420_000

    data class Encoded(
        val base64: String,
        val width: Int,
        val height: Int,
        val ageMs: Long,
        val sha256: String
    )

    fun encode(
        frame: CalibrationEvidenceFrame,
        config: VisionCalibrationConfig,
        visorActive: Boolean = true,
        slotLabels: Map<String, String> = emptyMap()
    ): Encoded? {
        val source = frame.bitmap
        if (source.isRecycled || source.width <= 0 || source.height <= 0) return null

        val targetWidth = min(source.width, MAX_WIDTH)
        val targetHeight = (source.height.toFloat() * targetWidth / source.width.toFloat())
            .toInt().coerceAtLeast(1)
        var scaled: Bitmap? = null
        var working: Bitmap? = null
        return try {
            scaled = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
            working = scaled?.copy(Bitmap.Config.ARGB_8888, true) ?: return null
            scaled?.let { if (it !== source) it.recycle() }
            scaled = null

            if (visorActive) drawCalibrationOverlay(working!!, config, slotLabels)
            val bytes = compressBounded(working!!)
            if (bytes.isEmpty() || bytes.size > MAX_BYTES) return null
            val age = (android.os.SystemClock.elapsedRealtime() - frame.capturedAtElapsedMs).coerceAtLeast(0L)
            Encoded(
                base64 = Base64.encodeToString(bytes, Base64.NO_WRAP),
                width = working!!.width,
                height = working!!.height,
                ageMs = age,
                sha256 = sha256(bytes)
            )
        } catch (_: Throwable) {
            null
        } finally {
            runCatching { scaled?.recycle() }
            runCatching { working?.recycle() }
        }
    }

    private fun drawCalibrationOverlay(
        bitmap: Bitmap,
        config: VisionCalibrationConfig,
        slotLabels: Map<String, String>
    ) {
        val canvas = Canvas(bitmap)
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val stroke = max(2f, min(width, height) * 0.0022f)

        fun paint(color: Int, style: Paint.Style = Paint.Style.STROKE): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            this.style = style
            this.strokeWidth = stroke
        }

        val allyPaint = paint(Color.rgb(0, 235, 220))
        val enemyPaint = paint(Color.rgb(255, 78, 112))
        val banPaint = paint(Color.rgb(255, 201, 83))

        for (index in 0..4) {
            canvas.drawOval(
                AdaptiveScreenLayoutEngine.calculateSlotCropRect(bitmap.width, bitmap.height, true, index, config).toRectF(),
                allyPaint
            )
            canvas.drawOval(
                AdaptiveScreenLayoutEngine.calculateSlotCropRect(bitmap.width, bitmap.height, false, index, config).toRectF(),
                enemyPaint
            )
        }

        for (ally in listOf(true, false)) {
            for (region in config.banRegions(ally)) {
                canvas.drawCircle(width * region.x, height * region.y, height * region.radius, banPaint)
            }
        }

        // Label the crop above its rim so the original portrait and game title remain visible.
        fun drawSlotLabels(isAlly: Boolean) {
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = Color.WHITE
                textSize = max(9f, min(20f, height * .016f))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                textAlign = if (isAlly) Paint.Align.LEFT else Paint.Align.RIGHT
                setShadowLayer(2f, 0f, 0f, Color.BLACK)
            }
            val background = paint(Color.argb(170, 5, 10, 20), Paint.Style.FILL)
            for (index in 0..4) {
                val label = slotLabels["${if (isAlly) "ally" else "enemy"}_$index"] ?: continue
                val crop = AdaptiveScreenLayoutEngine.calculateSlotCropRect(bitmap.width, bitmap.height, isAlly, index, config)
                val x = if (isAlly) crop.left.toFloat() else crop.right.toFloat()
                val available = if (isAlly) width - x else x
                val count = textPaint.breakText(label, true, available.coerceAtLeast(0f), null)
                val text = label.take(count)
                val baseline = crop.top - stroke - textPaint.descent()
                val length = textPaint.measureText(text)
                val left = if (isAlly) x else x - length
                canvas.drawRect(left, baseline + textPaint.ascent(), left + length, baseline + textPaint.descent(), background)
                canvas.drawText(text, x, baseline, textPaint)
            }
        }
        drawSlotLabels(true)
        drawSlotLabels(false)
    }

    private fun compressBounded(bitmap: Bitmap): ByteArray {
        var quality = 82
        var bytes = ByteArray(0)
        // Keep the attachment bounded for Firestore while still guaranteeing
        // an image for busy game frames. The frame is already capped at 1280px;
        // lowering JPEG quality is preferable to silently dropping evidence.
        while (quality >= 22) {
            ByteArrayOutputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
                bytes = output.toByteArray()
            }
            if (bytes.size <= MAX_BYTES) return bytes
            quality -= 10
        }
        return bytes
    }

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString("") { byte -> "%02x".format(byte) }

    private fun android.graphics.Rect.toRectF() = RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
}
