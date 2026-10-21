package com.example.service.screen

import android.graphics.Rect
import kotlin.math.ceil
import kotlin.math.floor

/** Mirrors Android's uniform display scaling, including centered letterboxing. */
internal object CaptureCoordinateSpace {
    fun map(rect: Rect, sourceWidth: Int, sourceHeight: Int, frameWidth: Int, frameHeight: Int): Rect {
        require(sourceWidth > 0 && sourceHeight > 0 && frameWidth > 0 && frameHeight > 0)
        val scale = minOf(frameWidth.toDouble() / sourceWidth, frameHeight.toDouble() / sourceHeight)
        val offsetX = (frameWidth - sourceWidth * scale) / 2.0
        val offsetY = (frameHeight - sourceHeight * scale) / 2.0
        val left = floor(rect.left * scale + offsetX).toInt().coerceIn(0, frameWidth)
        val top = floor(rect.top * scale + offsetY).toInt().coerceIn(0, frameHeight)
        val right = ceil(rect.right * scale + offsetX).toInt().coerceIn(left, frameWidth)
        val bottom = ceil(rect.bottom * scale + offsetY).toInt().coerceIn(top, frameHeight)
        return Rect(left, top, right, bottom)
    }
}

/** Rectangles and their source size are one immutable publication across UI/capture threads. */
internal data class CaptureMaskSnapshot(val rects: List<Rect>, val width: Int = 0, val height: Int = 0) {
    fun forFrame(frameWidth: Int, frameHeight: Int): List<Rect> = rects.map {
        if (width > 0 && height > 0) CaptureCoordinateSpace.map(it, width, height, frameWidth, frameHeight) else Rect(it)
    }
}
