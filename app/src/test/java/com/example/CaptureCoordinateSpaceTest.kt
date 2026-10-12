package com.example

import android.graphics.Rect
import com.example.service.screen.CaptureCoordinateSpace
import com.example.service.screen.AdaptiveScreenLayoutEngine
import com.example.service.screen.VisionCalibrationConfig
import com.example.service.screen.DraftVisionScanner
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import android.app.Application

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class CaptureCoordinateSpaceTest {
    @Test fun redMagicPhysicalCaptionWouldEraseSennaWithoutProjectionConversion() {
        val physicalCaption = Rect(94,134,735,166)
        val title = AdaptiveScreenLayoutEngine.calculateSlotNameRect(1920,868,true,0,VisionCalibrationConfig())
        assertTrue("Regression: raw physical caption overlaps the game's title",Rect.intersects(physicalCaption,title))
        val capturedCaption = CaptureCoordinateSpace.map(physicalCaption,2688,1216,1920,868)
        assertFalse("Correct coordinates keep Senna's title visible",Rect.intersects(capturedCaption,title))
    }
    @Test fun physicalHubWouldHideTheRivalBanColumnWithoutConversion() {
        val physicalHub = Rect(1029,29,1974,1000)
        val firstRivalBan = Rect(1550,14,1605,80)
        assertTrue(Rect.intersects(physicalHub,firstRivalBan))
        assertFalse(Rect.intersects(CaptureCoordinateSpace.map(physicalHub,2688,1216,1920,868),firstRivalBan))
    }
    @Test fun centeredLetterboxingUsesOneScaleOnBothAxes() {
        assertEquals(Rect(0,60,1920,1020),CaptureCoordinateSpace.map(Rect(0,0,2400,1200),2400,1200,1920,1080))
        assertEquals(Rect(480,300,1440,780),CaptureCoordinateSpace.map(Rect(600,300,1800,900),2400,1200,1920,1080))
    }
    @Test fun frameLocalFixtureMasksRemainLocalAndPublishedMasksKeepTheirSourceSize() {
        val previous = DraftVisionScanner.overlayRect
        try {
            DraftVisionScanner.overlayRect = Rect(100,50,200,100)
            assertEquals(Rect(100,50,200,100),DraftVisionScanner.overlayForFrame(1920,868))
            DraftVisionScanner.publishScreenOverlay(Rect(600,300,1800,900),2400,1200)
            assertEquals(Rect(480,300,1440,780),DraftVisionScanner.overlayForFrame(1920,1080))
            assertEquals(Rect(600,300,1800,900),DraftVisionScanner.overlayRect)
        } finally { DraftVisionScanner.overlayRect = previous }
    }
}
