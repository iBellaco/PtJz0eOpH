package com.example

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import com.example.service.screen.AdaptiveScreenLayoutEngine
import com.example.service.screen.DraftSlotLifecycle.Observation.*
import com.example.service.screen.DraftVisionScanner
import com.example.service.screen.VisionCalibrationConfig
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DraftSlotVisibilityTest {
    @Test fun realDraftRemainsVisibleButOneResidualCircleCannotKeepScanning() {
        val scanner = DraftVisionScanner
        val original = scanner.calibrationConfig
        val context = RuntimeEnvironment.getApplication()
        val fixturePath = listOf(java.io.File("src/androidTest/assets/draft/live-red-magic-7448.json"),
            java.io.File("app/src/androidTest/assets/draft/live-red-magic-7448.json")).first { it.isFile }
        val fixture = org.json.JSONObject(fixturePath.readText()).getString("pngBase64")
        val bytes = android.util.Base64.decode(fixture, android.util.Base64.DEFAULT)
        val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)!!
        val departed = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
        try {
            val config = VisionCalibrationConfig()
            scanner.updateCalibration(context, config)
            scanner.resetSlotLifecycle()
            assertEquals(ACTIVE, scanner.observeSelectionSlots(image, 9))
            val roi = AdaptiveScreenLayoutEngine.calculateSlotCropRect(image.width, image.height, true, 0, config)
            Canvas(departed).apply {
                drawColor(Color.BLACK)
                drawBitmap(image, roi, roi, null)
            }
            assertEquals(DISAPPEARING, scanner.observeSelectionSlots(departed, 9))
            assertEquals(FINISHED, scanner.observeSelectionSlots(departed, 9))
        } finally {
            image.recycle(); departed.recycle()
            scanner.resetSlotLifecycle(); scanner.updateCalibration(context, original)
        }
    }
}
