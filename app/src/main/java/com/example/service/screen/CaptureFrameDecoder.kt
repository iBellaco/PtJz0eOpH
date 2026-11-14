package com.example.service.screen

import android.graphics.Bitmap
import java.nio.ByteBuffer
import kotlin.math.roundToInt

/** Handles RGBA planes whose last row omits the vendor's alignment padding. */
internal object CaptureFrameDecoder {
    /** Owned by one capture session and accessed under its frame lock. */
    class PackingBuffer {
        private var buffer: ByteBuffer? = null
        fun obtain(size: Int): ByteBuffer {
            val target = buffer?.takeIf { it.capacity() == size }
                ?: ByteBuffer.allocateDirect(size).also { buffer = it }
            target.clear()
            return target
        }
        fun clear() { buffer = null }
    }
    fun captureSize(width: Int, height: Int): Pair<Int, Int> {
        require(width > 0 && height > 0)
        val scale = minOf(1.0, 1920.0 / width, 1080.0 / height)
        return (width * scale).roundToInt().coerceAtLeast(1) to
            (height * scale).roundToInt().coerceAtLeast(1)
    }

    fun decode(source: ByteBuffer, width: Int, height: Int, pixelStride: Int, rowStride: Int,
        packingBuffer: PackingBuffer? = null): Bitmap? {
        if (width <= 0 || height <= 0 || pixelStride != 4 || rowStride < width.toLong() * 4) return null
        val rowBytes = width.toLong() * 4
        val required = (height - 1L) * rowStride + rowBytes
        val packedSize = rowBytes * height
        if (required > source.remaining() || packedSize > Int.MAX_VALUE) return null
        val pixels = source.duplicate()
        val start = pixels.position()
        val packed = if (rowStride.toLong() == rowBytes) pixels else {
            (packingBuffer?.obtain(packedSize.toInt()) ?: ByteBuffer.allocateDirect(packedSize.toInt())).also { target ->
                repeat(height) { row ->
                    pixels.limit(source.limit())
                    pixels.position(start + row * rowStride)
                    pixels.limit(pixels.position() + rowBytes.toInt())
                    target.put(pixels)
                }
                target.rewind()
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        try {
            bitmap.copyPixelsFromBuffer(packed)
            return bitmap
        } catch (failure: Throwable) {
            bitmap.recycle()
            throw failure
        }
    }
}
