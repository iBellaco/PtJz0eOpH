package com.example

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.example.service.screen.AdaptiveScreenLayoutEngine
import com.example.service.screen.DraftVisionScanner
import com.example.service.screen.VisionCalibrationConfig
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowSystemClock
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DraftPreviewAlignmentTest {
    @Test fun secondPreviewFrameRechecksViewportWithoutWaitingForTheAutoScanInterval() {
        val context = RuntimeEnvironment.getApplication()
        val preferences = context.getSharedPreferences("vision_calibration_prefs", 0)
        preferences.edit().clear().commit()
        val source = javaClass.getResourceAsStream("/portraits/draft-ten-selected.jpg").use { BitmapFactory.decodeStream(it)!! }
        val first = Bitmap.createBitmap(1024, 768, Bitmap.Config.ARGB_8888)
        val second = Bitmap.createBitmap(1024, 768, Bitmap.Config.ARGB_8888)
        val oldMask = DraftVisionScanner.overlayRect
        try {
            DraftVisionScanner.overlayRect = null
            DraftVisionScanner.initCalibration(context)
            Canvas(first).drawColor(Color.rgb(30, 35, 45))
            ShadowSystemClock.advanceBy(Duration.ofMillis(100))
            DraftVisionScanner.configForFrame(first, refreshGeometry = true)
            Canvas(second).apply {
                drawColor(Color.BLACK)
                drawBitmap(source, null, Rect(0, 152, 1024, 615), Paint(Paint.FILTER_BITMAP_FLAG))
            }
            ShadowSystemClock.advanceBy(Duration.ofMillis(300))
            val aligned = DraftVisionScanner.configForFrame(second, refreshGeometry = true)
            val crop = AdaptiveScreenLayoutEngine.calculateSlotCropRect(1024, 768, true, 0, aligned)
            assertEquals(152 + 110f * 463 / 579, crop.exactCenterY(), 5f)
            assertEquals(Rect(0, 152, 1024, 615), DraftVisionScanner.alignedViewportFlow.value)
            assertEquals(1024 to 768, DraftVisionScanner.alignedFrameSizeFlow.value)
            assertEquals(aligned, DraftVisionScanner.alignedConfigFlow.value)
        } finally {
            source.recycle(); first.recycle(); second.recycle()
            preferences.edit().clear().commit()
            DraftVisionScanner.initCalibration(context)
            DraftVisionScanner.overlayRect = oldMask
        }
    }

    @Test fun forcedPreviewKeepsUserCalibration() {
        val context = RuntimeEnvironment.getApplication()
        val preferences = context.getSharedPreferences("vision_calibration_prefs", 0)
        preferences.edit().clear().commit()
        val bitmap = Bitmap.createBitmap(1280, 590, Bitmap.Config.ARGB_8888)
        try {
            DraftVisionScanner.initCalibration(context)
            val manual = VisionCalibrationConfig(allySlotXRatios = List(5) { .15f },
                enemySlotXRatios = List(5) { .85f })
            DraftVisionScanner.updateCalibration(context, manual)
            val aligned = DraftVisionScanner.configForFrame(bitmap, refreshGeometry = true)
            assertEquals(manual.allySlotXRatios, aligned.allySlotXRatios)
            assertEquals(manual.enemySlotXRatios, aligned.enemySlotXRatios)
        } finally {
            bitmap.recycle()
            preferences.edit().clear().commit()
            DraftVisionScanner.initCalibration(context)
        }
    }
}
