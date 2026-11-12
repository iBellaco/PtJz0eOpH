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
        language: String = "es",
        visorActive: Boolean = true,
        literalCapture: Boolean = true
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

            drawCalibrationOverlay(working!!, config, language, visorActive, literalCapture)
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
        language: String,
        visorActive: Boolean,
        literalCapture: Boolean
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
            canvas.drawRect(
                AdaptiveScreenLayoutEngine.calculateSlotCropRect(bitmap.width, bitmap.height, true, index, config).toRectF(),
                allyPaint
            )
            canvas.drawRect(
                AdaptiveScreenLayoutEngine.calculateSlotCropRect(bitmap.width, bitmap.height, false, index, config).toRectF(),
                enemyPaint
            )
        }

        fun drawBans(isAlly: Boolean, color: Int) {
            val p = paint(color)
            config.banRegions(isAlly).forEach { region ->
                canvas.drawCircle(width * region.x, height * region.y, height * region.radius, p)
            }
        }
        drawBans(true, banPaint.color)
        drawBans(false, banPaint.color)

        val topPaint = paint(Color.WHITE.copyAlpha(0.9f))
        val topRadius = height * config.topAvatarDiameterRatio / 2f
        config.topAllyXRatios.forEach { x -> canvas.drawCircle(width * x, height * config.topAvatarYRatio, topRadius, topPaint) }
        config.topEnemyXRatios.forEach { x -> canvas.drawCircle(width * x, height * config.topAvatarYRatio, topRadius, topPaint) }

        val bannerHeight = max(28f, height * 0.052f)
        val bannerPaint = paint(Color.BLACK.copyAlpha(0.72f), Paint.Style.FILL)
        canvas.drawRect(0f, 0f, min(width, 760f), bannerHeight, bannerPaint)
        val label = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = max(12f, height * 0.019f)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val bannerText = if (language.equals("pt", ignoreCase = true)) {
            "COACH · ${if (literalCapture) "CAPTURA LITERAL" else "EVIDÊNCIA DE CALIBRAÇÃO"} · ${if (visorActive) "VISOR ATIVO" else "VISOR INATIVO"}"
        } else {
            "COACH · ${if (literalCapture) "CAPTURA LITERAL" else "EVIDENCIA DE CALIBRACIÓN"} · ${if (visorActive) "VISOR ACTIVO" else "VISOR INACTIVO"}"
        }
        canvas.drawText(bannerText, 10f, bannerHeight * 0.68f, label)
    }

    private fun compressBounded(bitmap: Bitmap): ByteArray {
        var quality = 82
        var bytes = ByteArray(0)
        while (quality >= 52) {
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

    private fun Int.copyAlpha(alpha: Float): Int = Color.argb(
        (alpha.coerceIn(0f, 1f) * 255).toInt(),
        Color.red(this), Color.green(this), Color.blue(this)
    )
}
