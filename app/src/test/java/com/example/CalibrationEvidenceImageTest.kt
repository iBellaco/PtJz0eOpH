package com.example

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.service.screen.CalibrationEvidenceFrame
import com.example.service.screen.CalibrationEvidenceImage
import com.example.service.screen.VisionCalibrationConfig
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class CalibrationEvidenceImageTest {
    @Test fun encodesBoundedImageWithCalibrationOverlayAndHash() {
        val source = Bitmap.createBitmap(640, 360, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.rgb(8, 20, 32))
        }
        try {
            val encoded = CalibrationEvidenceImage.encode(
                CalibrationEvidenceFrame(source, android.os.SystemClock.elapsedRealtime()),
                VisionCalibrationConfig(),
                "es"
            )
            assertNotNull(encoded)
            val result = encoded!!
            assertTrue(result.base64.isNotBlank())
            assertTrue(result.sha256.matches(Regex("[0-9a-f]{64}")))
            assertTrue(result.width <= 1280)
            assertTrue(result.height > 0)
            val decoded = BitmapFactory.decodeByteArray(
                Base64.decode(result.base64, Base64.NO_WRAP), 0,
                Base64.decode(result.base64, Base64.NO_WRAP).size
            )
            assertNotNull(decoded)
            decoded.recycle()
        } finally {
            source.recycle()
        }
    }
}
