package com.example.service.screen

import kotlin.math.sqrt

/** Spatial, mean-centered RGB correlation. Borders and corner overlays are excluded. */
internal object PortraitMatcher {
    const val SIZE = 24
    const val MIN_MARGIN = 0.08f
    private val samples = buildList {
        for (y in 0 until SIZE) for (x in 0 until SIZE) {
            val nx = (x + 0.5f) / SIZE - 0.5f
            val ny = (y + 0.5f) / SIZE - 0.5f
            if (nx * nx + ny * ny <= 0.38f * 0.38f) add(nx to ny)
        }
    }

    fun descriptor(pixels: IntArray, width: Int, height: Int,
                   scale: Float = 1f, dx: Float = 0f, dy: Float = 0f): FloatArray {
        require(width > 0 && height > 0 && pixels.size == width * height)
        val values = FloatArray(samples.size * 3)
        val means = FloatArray(3)
        samples.forEachIndexed { i, (x, y) ->
            val px = ((0.5f + x * scale + dx) * width).toInt().coerceIn(0, width - 1)
            val py = ((0.5f + y * scale + dy) * height).toInt().coerceIn(0, height - 1)
            val color = pixels[py * width + px]
            for (c in 0..2) {
                val value = ((color ushr (16 - c * 8)) and 255) / 255f
                values[i * 3 + c] = value
                means[c] += value
            }
        }
        for (c in 0..2) means[c] /= samples.size
        var norm = 0f
        for (i in values.indices) {
            values[i] -= means[i % 3]
            norm += values[i] * values[i]
        }
        norm = sqrt(norm).coerceAtLeast(1e-8f)
        for (i in values.indices) values[i] /= norm
        return values
    }

    fun references(pixels: IntArray, width: Int, height: Int): List<FloatArray> = buildList {
        for (scale in floatArrayOf(0.85f, 1f, 1.15f))
            for (dx in floatArrayOf(-0.04f, 0f, 0.04f))
                for (dy in floatArrayOf(-0.04f, 0f, 0.04f))
                    add(descriptor(pixels, width, height, scale, dx, dy))
    }

    fun similarity(input: FloatArray, references: List<FloatArray>): Float =
        references.maxOfOrNull { reference ->
            var dot = 0f
            for (i in input.indices) dot += input[i] * reference[i]
            dot.coerceIn(0f, 1f)
        } ?: 0f

    fun accepts(best: Float, second: Float, threshold: Float): Boolean =
        best >= threshold && best - second >= MIN_MARGIN
}
