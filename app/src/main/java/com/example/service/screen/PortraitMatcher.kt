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
            // Match pixel centers at subpixel precision. Nearest-neighbor sampling aliases
            // small JPEG slot portraits against the larger local reference (notably Vi).
            val px = ((0.5f + x * scale + dx) * width - 0.5f).coerceIn(0f, (width - 1).toFloat())
            val py = ((0.5f + y * scale + dy) * height - 0.5f).coerceIn(0f, (height - 1).toFloat())
            val x0 = px.toInt(); val y0 = py.toInt()
            val x1 = (x0 + 1).coerceAtMost(width - 1)
            val y1 = (y0 + 1).coerceAtMost(height - 1)
            val fx = px - x0; val fy = py - y0
            for (c in 0..2) {
                val shift = 16 - c * 8
                fun channel(x: Int, y: Int) = ((pixels[y * width + x] ushr shift) and 255).toFloat()
                val top = channel(x0, y0) * (1f - fx) + channel(x1, y0) * fx
                val bottom = channel(x0, y1) * (1f - fx) + channel(x1, y1) * fx
                val value = (top * (1f - fy) + bottom * fy) / 255f
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
        // Small slot crops can fall between the coarse scales/offsets, especially
        // after ring recentering or JPEG compression. Fill those gaps in the cached
        // references, rather than weakening confidence or the ambiguity margin.
        for (scale in floatArrayOf(0.925f, 1.075f))
            for (dx in floatArrayOf(-0.03f, 0f, 0.03f))
                for (dy in floatArrayOf(-0.03f, 0f, 0.03f))
                    add(descriptor(pixels, width, height, scale, dx, dy))
    }

    fun correlation(input: FloatArray, reference: FloatArray): Float {
        var dot = 0f
        for (i in input.indices) dot += input[i] * reference[i]
        return dot.coerceIn(0f, 1f)
    }

    fun similarity(input: FloatArray, references: List<FloatArray>): Float =
        references.maxOfOrNull { correlation(input, it) } ?: 0f

    /** Only these pixels are sampled (plus bilinear neighbours); border captions cannot alter a descriptor. */
    internal fun sampledRect(crop: android.graphics.Rect): android.graphics.Rect =
        android.graphics.Rect(crop).apply {
            val paddingPixels = ((minOf(width(), height()) / 2f) * .20f).toInt().minus(1).coerceAtLeast(0)
            inset(paddingPixels, paddingPixels)
        }

    /** Fine reference alignment is used only for an unresolved header portrait. */
    fun fineSimilarity(input: FloatArray, pixels: IntArray, width: Int, height: Int): Float {
        val variants = buildList {
            for (scale in floatArrayOf(.85f,1f,1.15f))
                for (dx in floatArrayOf(-.04f,0f,.04f)) for (dy in floatArrayOf(-.04f,0f,.04f)) add(Triple(scale,dx,dy))
            for (scale in floatArrayOf(.925f,1.075f))
                for (dx in floatArrayOf(-.03f,0f,.03f)) for (dy in floatArrayOf(-.03f,0f,.03f)) add(Triple(scale,dx,dy))
        }
        var point = variants.first()
        var best = 0f
        fun evaluate(candidate: Triple<Float,Float,Float>) {
            val score = correlation(input,descriptor(pixels,width,height,candidate.first,candidate.second,candidate.third))
            if (score > best) { best=score; point=candidate }
        }
        variants.forEach(::evaluate)
        for (step in floatArrayOf(.02f,.01f,.005f)) {
            val center=point
            for (ds in floatArrayOf(-step,0f,step)) for (dx in floatArrayOf(-step,0f,step)) for (dy in floatArrayOf(-step,0f,step))
                evaluate(Triple(center.first+ds,center.second+dx,center.third+dy))
        }
        return best
    }

    fun accepts(best: Float, second: Float, threshold: Float): Boolean =
        best >= threshold && best - second >= MIN_MARGIN
}
