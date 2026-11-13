package com.example.service.screen

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
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

    /**
     * Chooses the geometry to draw for the ban row without allowing an
     * incomplete observation to move the remaining saved positions.
     */
    internal fun evidenceBanRegions(
        config: VisionCalibrationConfig,
        banSlots: List<BanSlot>,
        isAlly: Boolean
    ): List<BanRegion> {
        val side = if (isAlly) BanSide.ALLY else BanSide.ENEMY
        val observedByIndex = banSlots
            .asSequence()
            .filter { it.side == side && it.index in 0..4 && it.region != null }
            .associateBy { it.index }
        return if ((0..4).all { observedByIndex[it]?.region != null }) {
            (0..4).mapNotNull { observedByIndex[it]?.region }
        } else {
            config.banRegions(isAlly)
        }
    }

    fun encode(
        frame: CalibrationEvidenceFrame,
        config: VisionCalibrationConfig,
        language: String = "es",
        visorActive: Boolean = true,
        literalCapture: Boolean = true,
        slotLabels: Map<String, String> = emptyMap(),
        banSlots: List<BanSlot> = emptyList(),
        avatarBitmaps: Map<String, Bitmap> = emptyMap()
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

            drawCalibrationOverlay(working!!, config, language, visorActive, literalCapture, slotLabels, banSlots, avatarBitmaps)
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
        literalCapture: Boolean,
        slotLabels: Map<String, String>,
        banSlots: List<BanSlot>,
        avatarBitmaps: Map<String, Bitmap>
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

        // Las cajas sirven para revisar la geometría, pero no sustituyen la
        // evidencia visual. Repetimos el retrato confirmado dentro de cada
        // región para que un administrador pueda reconocer al campeón aunque
        // la captura original tenga poca resolución o el visor esté oculto.
        fun drawAvatar(key: String, destination: RectF, circle: Boolean) {
            val avatar = avatarBitmaps[key]?.takeIf { !it.isRecycled } ?: return
            val source = android.graphics.Rect(0, 0, avatar.width, avatar.height)
            val avatarPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.save()
            if (circle) {
                val path = Path().apply { addOval(destination, Path.Direction.CW) }
                canvas.clipPath(path)
            } else {
                canvas.clipRect(destination)
            }
            canvas.drawBitmap(avatar, source, destination, avatarPaint)
            canvas.restore()
        }

        for (index in 0..4) {
            drawAvatar(
                "ally_$index",
                AdaptiveScreenLayoutEngine.calculateSlotCropRect(bitmap.width, bitmap.height, true, index, config).toRectF(),
                circle = true
            )
            drawAvatar(
                "enemy_$index",
                AdaptiveScreenLayoutEngine.calculateSlotCropRect(bitmap.width, bitmap.height, false, index, config).toRectF(),
                circle = true
            )
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
            // La captura debe auditar las coordenadas guardadas, no las
            // regiones adaptativas que un fotograma defectuoso pudo proponer.
            // Así los cinco baneos permanecen alineados y los huecos no se
            // desplazan por una observación aislada.
            evidenceBanRegions(config, banSlots, isAlly).forEachIndexed { index, region ->
                drawAvatar(
                    "${if (isAlly) "ally" else "enemy"}_ban_$index",
                    RectF(
                        width * region.x - height * region.radius,
                        height * region.y - height * region.radius,
                        width * region.x + height * region.radius,
                        height * region.y + height * region.radius
                    ),
                    circle = true
                )
                canvas.drawCircle(width * region.x, height * region.y, height * region.radius, p)
            }
        }
        drawBans(true, banPaint.color)
        drawBans(false, banPaint.color)

        // La captura de respaldo puede no contener el visor Compose. Repetir
        // únicamente las identidades confirmadas en la banda de cada slot para
        // que el administrador pueda comprobar qué campeón se leyó; si no hay
        // identidad, no se dibuja ningún nombre inventado.
        fun drawSlotLabels(isAlly: Boolean, color: Int) {
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = Color.WHITE
                textSize = max(11f, min(26f, height * 0.019f))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                textAlign = if (isAlly) Paint.Align.LEFT else Paint.Align.RIGHT
                setShadowLayer(3f, 0f, 0f, Color.BLACK)
            }
            val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = Color.argb(165, 5, 10, 20)
                style = Paint.Style.FILL
            }
            for (index in 0..4) {
                val value = slotLabels["${if (isAlly) "ally" else "enemy"}_$index"]
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?: continue
                val rect = AdaptiveScreenLayoutEngine.calculateSlotNameRect(
                    bitmap.width, bitmap.height, isAlly, index, config
                )
                val maxTextWidth = (rect.width() - 8f).coerceAtLeast(16f)
                var fitted = value
                while (fitted.length > 1 && textPaint.measureText("$fitted…") > maxTextWidth) {
                    fitted = fitted.dropLast(1)
                }
                if (textPaint.measureText(fitted) > maxTextWidth) fitted = "…"
                val left = rect.left.toFloat()
                val right = rect.right.toFloat()
                val top = rect.top.toFloat()
                val bottom = rect.bottom.toFloat()
                canvas.drawRoundRect(
                    RectF(left, top, right, bottom),
                    max(3f, height * .004f),
                    max(3f, height * .004f),
                    backgroundPaint
                )
                val metrics = textPaint.fontMetrics
                val baseline = (top + bottom) / 2f - (metrics.ascent + metrics.descent) / 2f
                val x = if (isAlly) left + 4f else right - 4f
                canvas.drawText(fitted, x, baseline, textPaint)
                // A thin side-colored edge makes the evidence readable without
                // covering the avatar or the ban header.
                val edgePaint = paint(color, Paint.Style.STROKE)
                canvas.drawRoundRect(RectF(left, top, right, bottom), 3f, 3f, edgePaint)
            }
        }
        drawSlotLabels(true, allyPaint.color)
        drawSlotLabels(false, enemyPaint.color)

        val topPaint = paint(Color.WHITE.copyAlpha(0.9f))
        val topRadius = height * config.topAvatarDiameterRatio / 2f
        config.topAllyXRatios.forEach { x -> canvas.drawCircle(width * x, height * config.topAvatarYRatio, topRadius, topPaint) }
        config.topEnemyXRatios.forEach { x -> canvas.drawCircle(width * x, height * config.topAvatarYRatio, topRadius, topPaint) }

        // No añadir una cabecera sobre la captura: en pantallas panorámicas esa
        // franja tapa la fila superior de baneos. El origen de la evidencia y el
        // estado del visor siguen viajando en el texto del informe.
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
