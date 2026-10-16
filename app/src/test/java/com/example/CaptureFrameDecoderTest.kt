package com.example

import android.app.Application
import com.example.service.screen.CaptureFrameDecoder
import java.nio.ByteBuffer
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class CaptureFrameDecoderTest {
    @Test fun lastRowDoesNotNeedAlignmentPadding() {
        // Two 2-pixel rows; the vendor supplies four padding bytes only after row one.
        val bytes = ByteBuffer.allocateDirect(20)
        repeat(2) { bytes.put(byteArrayOf(-1, 0, 0, -1)) }
        bytes.putInt(0)
        repeat(2) { bytes.put(byteArrayOf(0, -1, 0, -1)) }
        bytes.rewind()
        val bitmap = CaptureFrameDecoder.decode(bytes, 2, 2, 4, 12)!!
        try {
            assertEquals(2, bitmap.width)
            assertEquals(bitmap.getPixel(0, 0), bitmap.getPixel(1, 0))
            assertEquals(bitmap.getPixel(0, 1), bitmap.getPixel(1, 1))
            assertNotEquals(bitmap.getPixel(0, 0), bitmap.getPixel(0, 1))
            assertEquals(0, bytes.position())
        } finally { bitmap.recycle() }
    }
    @Test fun truncatedPlaneIsSkippedWithoutReadingOutsideItsBuffer() {
        assertNull(CaptureFrameDecoder.decode(ByteBuffer.allocate(19), 2, 2, 4, 12))
    }
    @Test fun deviceAspectRatioIsPreservedWithoutUpscaling() {
        for ((width, height) in listOf(3120 to 1440, 2688 to 1216, 2712 to 1220, 2208 to 1840, 848 to 540)) {
            val (w, h) = CaptureFrameDecoder.captureSize(width, height)
            assertTrue(w <= 1920 && h <= 1080)
            assertTrue(w <= width && h <= height)
            assertEquals(width.toDouble() / height, w.toDouble() / h, 0.005)
        }
    }
}
