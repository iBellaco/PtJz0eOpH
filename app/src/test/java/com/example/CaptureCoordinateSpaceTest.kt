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
    @Test fun viewerUndoesCaptureScalingForSlotsAndBansAcrossScreenDimensions() {
        // Dimensions only: no device identity or model-specific coordinates.
        for ((screenW, screenH) in listOf(1280 to 587, 1280 to 590, 1920 to 1080,
            2400 to 1080, 3120 to 1440, 1024 to 768)) {
            for ((frameW, frameH) in listOf(1280 to 590, 1920 to 1080)) {
                val transform = CaptureCoordinateSpace.transform(screenW, screenH, frameW, frameH)
                // Portraits, title bands and header bans use the same inverse.
                for ((x, y) in listOf(.12 to .19, .91 to .72, .08 to .05, .92 to .05)) {
                    val screenX = x * screenW
                    val screenY = y * screenH
                    val frameX = screenX * transform.scale + transform.offsetX
                    val frameY = screenY * transform.scale + transform.offsetY
                    assertEquals(screenX, (frameX - transform.offsetX) / transform.scale, .001)
                    assertEquals(screenY, (frameY - transform.offsetY) / transform.scale, .001)
                }
                val content = CaptureCoordinateSpace.map(Rect(0, 0, screenW, screenH), screenW, screenH, frameW, frameH)
                assertTrue(content.left >= 0 && content.top >= 0 && content.right <= frameW && content.bottom <= frameH)
            }
        }
    }

    @Test fun collapsingTheHubPublishesItsNewBoundsToTheViewer() {
        val previous = DraftVisionScanner.overlayRect
        try {
            DraftVisionScanner.publishScreenOverlay(Rect(300, 0, 900, 550), 1280, 590)
            DraftVisionScanner.publishScreenOverlay(Rect(400, 0, 460, 90), 1280, 590)
            assertEquals(Rect(400, 0, 460, 90), DraftVisionScanner.screenOverlayBoundsFlow.value)
            assertEquals(Rect(400, 0, 460, 90), DraftVisionScanner.overlayForFrame(1280, 590))
            DraftVisionScanner.overlayRect = null
            assertNull(DraftVisionScanner.screenOverlayBoundsFlow.value)
        } finally { DraftVisionScanner.overlayRect = previous }
    }

    @Test fun captureProfilesGroupDifferentResolutionsByAspectRatio() {
        assertEquals(
            VisionCalibrationConfig.captureProfileKey(1920, 1080),
            VisionCalibrationConfig.captureProfileKey(2560, 1440)
        )
        assertNotEquals(
            VisionCalibrationConfig.captureProfileKey(1920, 1080),
            VisionCalibrationConfig.captureProfileKey(2400, 1080)
        )
    }

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
    @Test fun mappedRegionsAreAlwaysClampedToCapturedFrame() {
        assertEquals(Rect(0, 0, 1920, 1080), CaptureCoordinateSpace.map(Rect(-400, -200, 3000, 1800), 2400, 1200, 1920, 1080))
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
