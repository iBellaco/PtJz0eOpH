package com.example.service.screen

import android.graphics.Bitmap
import android.graphics.Rect

/** Only contiguous, nearly black outer bars are excluded; never crop to the central artwork. */
internal object DraftFrameGeometry {
    fun contentBounds(bitmap: Bitmap): Rect {
        val w = bitmap.width; val h = bitmap.height
        fun black(x: Int, y: Int): Boolean {
            val c = bitmap.getPixel(x, y)
            return ((c shr 16) and 255) <= 6 && ((c shr 8) and 255) <= 6 && (c and 255) <= 6
        }
        fun blackRow(y: Int): Boolean = (0 until 128).all { black(it * (w - 1) / 127, y) }
        fun blackColumn(x: Int, top: Int, bottom: Int): Boolean =
            (0 until 128).all { black(x, top + it * (bottom - top - 1) / 127) }
        var top = 0; var bottom = h; var left = 0; var right = w
        while (top < h / 3 && blackRow(top)) top++
        while (bottom > h * 2 / 3 && blackRow(bottom - 1)) bottom--
        while (left < w / 3 && blackColumn(left, top, bottom)) left++
        while (right > w * 2 / 3 && blackColumn(right - 1, top, bottom)) right--
        // Reaching a search limit means no actual content boundary was found.
        // A sparse header fixture/loading frame must not become a guessed viewport.
        if (top >= h / 3 || bottom <= h * 2 / 3 || left >= w / 3 || right <= w * 2 / 3)
            return Rect(0, 0, w, h)
        val bounds = Rect(left, top, right, bottom)
        // Loading/blank frames are not evidence of a viewport.
        return if (bounds.width().toFloat() / bounds.height() in 1.5f..3.1f &&
            // A landscape game fitted into a nearly square foldable occupies less
            // than half the height. The outer-bar search limits already reject
            // blank/loading frames; do not discard an observed boundary here.
            bounds.width() > w / 3 && bounds.height() > h / 3) bounds else Rect(0, 0, w, h)
    }

    /** One geometry is published to both recognition and the viewer. Manual profiles are frame-relative. */
    fun forViewport(base: VisionCalibrationConfig, bounds: Rect, width: Int, height: Int): VisionCalibrationConfig {
        if (bounds == Rect(0, 0, width, height)) return base
        fun x(value: Float) = (bounds.left + value * bounds.width()) / width
        fun y(value: Float) = (bounds.top + value * bounds.height()) / height
        val scale = bounds.height().toFloat() / height
        return base.copy(
            nameBandScale = base.nameBandScale * scale,
            allyAvatarCenterX = x(base.allyAvatarCenterX), enemyAvatarCenterX = x(base.enemyAvatarCenterX),
            allySlotXRatios = base.allySlotXRatios.map(::x), enemySlotXRatios = base.enemySlotXRatios.map(::x),
            allySlotYRatios = base.allySlotYRatios.map(::y), enemySlotYRatios = base.enemySlotYRatios.map(::y),
            avatarDiameterRatio = base.avatarDiameterRatio * scale,
            allySlotDiameterRatios = base.allySlotDiameterRatios.map { it * scale },
            enemySlotDiameterRatios = base.enemySlotDiameterRatios.map { it * scale },
            allyBanXRatios = base.allyBanXRatios.map(::x), enemyBanXRatios = base.enemyBanXRatios.map(::x),
            allyBanYRatio = y(base.allyBanYRatio), enemyBanYRatio = y(base.enemyBanYRatio),
            banDiameterRatio = base.banDiameterRatio * scale,
            allyOcrMinX = x(base.allyOcrMinX), allyOcrMaxX = x(base.allyOcrMaxX),
            enemyOcrMinX = x(base.enemyOcrMinX), enemyOcrMaxX = x(base.enemyOcrMaxX),
            spellLeftRatio = x(base.spellLeftRatio), spellSizeRatio = base.spellSizeRatio * scale,
            topAvatarYRatio = y(base.topAvatarYRatio), topAvatarDiameterRatio = base.topAvatarDiameterRatio * scale,
            topAlly5XRatio = x(base.topAlly5XRatio), topEnemy5XRatio = x(base.topEnemy5XRatio),
            topAllyXRatios = base.topAllyXRatios.map(::x), topEnemyXRatios = base.topEnemyXRatios.map(::x)
        )
    }
}
