package com.example

import android.app.Application
import android.graphics.*
import com.example.service.screen.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Synthetic geometry/packing checks, not physical device certification. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DraftMultideviceBoxesTest {
    @Test fun edgeDistancesAndSquarePortraitsSurviveAspectInsetsAndTabletLetterboxing() {
        val base = VisionCalibrationConfig()
        for ((w, h) in listOf(1280 to 720, 2340 to 1080, 2400 to 1080,
            2520 to 1080, 1600 to 1000, 1024 to 768)) {
            for (insets in listOf(0 to 0, 40 to 0, 0 to 80, 60 to 90)) {
                val bounds = Rect(insets.first, 30, w - insets.second, h - 30)
                val config = DraftFrameGeometry.calculateMultideviceBoxes(base, bounds, w, h)
                for (ally in listOf(true, false)) for (slot in 0..4) {
                    val rect = AdaptiveScreenLayoutEngine.calculateSlotCropRect(w, h, ally, slot, config)
                    assertEquals(rect.width(), rect.height())
                    assertTrue(Rect(0, 0, w, h).contains(rect))
                    val distance = if (ally) rect.exactCenterX() - bounds.left
                        else bounds.right - rect.exactCenterX()
                    val reference = if (ally) base.getAllySlotX(slot) else 1f - base.getEnemySlotX(slot)
                    assertEquals(reference * (1280f / 579f) * bounds.height(), distance, 2f)
                    assertEquals(bounds.top + bounds.height() *
                        (if (ally) base.allySlotYRatios else base.enemySlotYRatios)[slot], rect.exactCenterY(), 2f)
                }
            }
        }
    }

    @Test fun nearBlackAsymmetricBarsAreFoundAndBlankFramesAreNotViewports() {
        val bitmap = Bitmap.createBitmap(1280, 720, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.rgb(10, 10, 10))
            val expected = Rect(40, 0, 1200, 720)
            canvas.drawRect(expected, Paint().apply { color = Color.DKGRAY })
            assertEquals(expected, DraftFrameGeometry.contentBounds(bitmap))
            // A dark centre row alone must not invent side insets.
            canvas.drawColor(Color.DKGRAY)
            canvas.drawRect(Rect(0, 359, 100, 362), Paint().apply { color = Color.BLACK })
            assertEquals(Rect(0, 0, 1280, 720), DraftFrameGeometry.contentBounds(bitmap))
            canvas.drawColor(Color.BLACK)
            assertEquals(Rect(0, 0, 1280, 720), DraftFrameGeometry.contentBounds(bitmap))
        } finally { bitmap.recycle() }
    }

    @Test fun columnPackingKeepsOriginalTitlePixelsAndMasksWithoutMutatingCapture() {
        val frame = Bitmap.createBitmap(1600, 900, Bitmap.Config.ARGB_8888)
        try {
            frame.eraseColor(Color.DKGRAY)
            val config = DraftFrameGeometry.calculateMultideviceBoxes(VisionCalibrationConfig(),
                Rect(40, 0, 1520, 900), 1600, 900)
            val mask = AdaptiveScreenLayoutEngine.calculateSlotNameRect(1600, 900, true, 0, config)
            val atlas = DraftNameAtlas.createColumns(frame, config, listOf(mask))
            try {
                assertEquals(10, atlas.tiles.size)
                for (tile in atlas.tiles) {
                    assertEquals(tile.source.width(), tile.region.width())
                    assertEquals(tile.source.height(), tile.region.height())
                    val expected = if (tile.ally && tile.slot == 0) Color.BLACK else Color.DKGRAY
                    assertEquals(expected, atlas.bitmap.getPixel(tile.region.centerX(), tile.region.centerY()))
                }
                assertEquals(Color.DKGRAY, frame.getPixel(mask.centerX(), mask.centerY()))
            } finally { atlas.bitmap.recycle() }
        } finally { frame.recycle() }
    }

    @Test fun evenTinyFramesKeepPortraitCropsSquareAndInsideBounds() {
        for ((w, h) in listOf(20 to 10, 10 to 20, 1 to 1)) {
            val rect = AdaptiveScreenLayoutEngine.calculateSlotCropRect(w, h, false, 4, VisionCalibrationConfig())
            assertEquals(rect.width(), rect.height())
            assertTrue(Rect(0, 0, w, h).contains(rect))
        }
    }
}
