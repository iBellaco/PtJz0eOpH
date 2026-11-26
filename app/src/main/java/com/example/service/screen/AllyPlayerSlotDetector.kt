package com.example.service.screen

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect

/** Small, device-independent evidence helper for the highlighted local-player row. */
internal object AllyPlayerSlotDetector {
    private const val MIN_SCORE = 0.015f
    private const val MIN_MARGIN = 0.008f

    /** Wild Rift uses a warm yellow/gold tint for the local player's name row. */
    internal fun isWarmHighlight(color: Int): Boolean {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return red >= 135 && green >= 95 && blue <= 155 &&
            red >= blue + 45 && green >= blue + 15 && red >= green * 0.82f
    }

    /** Fraction of warm highlight pixels inside one OCR name box. */
    internal fun score(bitmap: Bitmap, source: Rect): Float {
        if (bitmap.isRecycled) return 0f
        val left = source.left.coerceIn(0, bitmap.width)
        val top = source.top.coerceIn(0, bitmap.height)
        val right = source.right.coerceIn(left, bitmap.width)
        val bottom = source.bottom.coerceIn(top, bitmap.height)
        if (right <= left || bottom <= top) return 0f
        val pixels = IntArray((right - left) * (bottom - top))
        bitmap.getPixels(pixels, 0, right - left, left, top, right - left, bottom - top)
        return pixels.count { isWarmHighlight(it) }.toFloat() / pixels.size.toFloat()
    }

    /** Rejects a frame where two rows have comparable yellow evidence. */
    internal fun choose(scores: Map<Int, Float>): Int? {
        val ordered = scores.filterKeys { it in 0..4 }
            .entries.sortedByDescending { it.value }
        val best = ordered.firstOrNull() ?: return null
        val second = ordered.getOrNull(1)?.value ?: 0f
        return best.key.takeIf {
            best.value >= MIN_SCORE && (best.value - second >= MIN_MARGIN || second <= best.value * 0.55f)
        }
    }
}
