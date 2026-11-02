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
    @Test fun repeatedLiveAlignmentWithItsOwnOutlinesDoesNotShrinkOrMoveTheSlots() {
        val context = RuntimeEnvironment.getApplication()
        val preferences = context.getSharedPreferences("vision_calibration_prefs", 0)
        preferences.edit().clear().commit()
        val oldMask = DraftVisionScanner.overlayRect
        val oldDebug = DraftVisionScanner.showCalibrationBoxes.value
        try {
            DraftVisionScanner.overlayRect = null
            DraftVisionScanner.showCalibrationBoxes.value = true
            for ((width, height) in listOf(1280 to 587, 1280 to 590, 1600 to 720)) {
                DraftVisionScanner.initCalibration(context)
                repeat(6) { pass ->
                    val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    try {
                        val canvas = Canvas(image)
                        canvas.drawColor(Color.rgb(10, 15, 30))
                        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                        val defaults = VisionCalibrationConfig()
                        val drawn = DraftVisionScanner.alignedConfigFlow.value
                        for (ally in listOf(true, false)) for (slot in 0..4) {
                            val x = width * if (ally) .125f else .906f
                            val y = height * (if (ally) defaults.allySlotYRatios else defaults.enemySlotYRatios)[slot]
                            paint.style = Paint.Style.FILL
                            // Changing portrait brightness simulates artwork, not a new layout.
                            paint.color = if (pass % 2 == 0) Color.WHITE else Color.LTGRAY
                            canvas.drawCircle(x, y, height * .02f, paint)
                            paint.style = Paint.Style.STROKE
                            paint.strokeWidth = 5f
                            paint.color = if (ally) Color.rgb(30, 190, 220) else Color.rgb(220, 40, 60)
                            canvas.drawCircle(x, y, height * .055f, paint)
                            paint.strokeWidth = 2.5f
                            paint.color = if (ally) Color.CYAN else Color.RED
                            val rect = AdaptiveScreenLayoutEngine.calculateSlotCropRect(width, height, ally, slot, drawn)
                            canvas.drawCircle(rect.exactCenterX(), rect.exactCenterY(), rect.width() / 2f, paint)
                        }
                        val aligned = DraftVisionScanner.configForFrame(image, refreshGeometry = true)
                        for (ally in listOf(true, false)) for (slot in 0..4) {
                            val x = if (ally) aligned.getAllySlotX(slot) else aligned.getEnemySlotX(slot)
                            assertEquals("pass=$pass dimensions=$width/$height", if (ally) .125f else .906f, x, .012f)
                            val diameter = aligned.getSlotDiameter(ally, slot)
                            assertTrue("Crops must not shrink cumulatively: $diameter", diameter in .083f.. .133f)
                            val y = (if (ally) aligned.allySlotYRatios else aligned.enemySlotYRatios)[slot]
                            val expectedY = (if (ally) defaults.allySlotYRatios else defaults.enemySlotYRatios)[slot]
                            assertEquals(expectedY, y, .015f)
                        }
                    } finally { image.recycle() }
                }
            }
        } finally {
            preferences.edit().clear().commit()
            DraftVisionScanner.initCalibration(context)
            DraftVisionScanner.overlayRect = oldMask
            DraftVisionScanner.showCalibrationBoxes.value = oldDebug
        }
    }

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
