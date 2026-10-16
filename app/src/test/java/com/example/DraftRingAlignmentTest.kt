package com.example

import android.app.Application
import android.graphics.BitmapFactory
import android.graphics.Rect
import com.example.service.screen.DraftRingAlignment
import com.example.service.screen.VisionCalibrationConfig
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34], application=Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DraftRingAlignmentTest {
    @Test fun s26VisibleRivalRingsReplaceAnotherPhonesCoordinates() {
        val bitmap=javaClass.getResourceAsStream("/s26/draft.jpg").use { BitmapFactory.decodeStream(it) }
        try {
            val aligned=DraftRingAlignment.align(bitmap,VisionCalibrationConfig(),Rect(60,0,590,591))
            assertEquals(1168f/1280f,aligned.enemyAvatarCenterX,.012f)
            assertTrue(aligned.enemySlotXRatios.all { kotlin.math.abs(it-1168f/1280f)<.012f })
            // Occluded allies cannot be located from the HUD's cyan borders.
            assertEquals(VisionCalibrationConfig().allySlotXRatios,aligned.allySlotXRatios)
        } finally { bitmap.recycle() }
    }
    @Test fun blankImageDoesNotInventAColumn() {
        val bitmap=android.graphics.Bitmap.createBitmap(1280,591,android.graphics.Bitmap.Config.ARGB_8888)
        try { assertEquals(VisionCalibrationConfig(),DraftRingAlignment.align(bitmap,VisionCalibrationConfig(),null)) }
        finally { bitmap.recycle() }
    }
}
