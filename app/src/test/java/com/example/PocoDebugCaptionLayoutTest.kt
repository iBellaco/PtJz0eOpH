package com.example

import android.app.Application
import android.graphics.Rect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.github.takahirom.roborazzi.captureRoboImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.example.service.screen.*
import com.example.ui.components.ScannerDebugOverlay
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class,qualifiers="w1280dp-h576dp-land-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PocoDebugCaptionLayoutTest {
    @get:Rule val compose=createComposeRule()
    @Test fun capturedSlotsAndBansAreRenderedAtScreenPositionsWithoutStretching() {
        val oldConfig = DraftVisionScanner.alignedConfigFlow.value
        val oldSize = DraftVisionScanner.alignedFrameSizeFlow.value
        val oldViewport = DraftVisionScanner.alignedViewportFlow.value
        val oldBans = DraftVisionScanner.banSlotsFlow.value
        val oldDetecting = DraftVisionScanner.banDetectionActiveFlow.value
        try {
            val screenConfig = VisionCalibrationConfig(allySlotXRatios = List(5) { .12f },
                allySlotYRatios = listOf(.20f, .34f, .48f, .62f, .76f))
            val viewport = Rect(0, 108, 1920, 972)
            DraftVisionScanner.alignedConfigFlow.value = DraftFrameGeometry.forViewport(screenConfig, viewport, 1920, 1080)
            DraftVisionScanner.alignedFrameSizeFlow.value = 1920 to 1080
            DraftVisionScanner.alignedViewportFlow.value = viewport
            DraftVisionScanner.banDetectionActiveFlow.value = true
            DraftVisionScanner.banSlotsFlow.value = emptyBanSlots().map { slot ->
                if (slot.side == BanSide.ALLY && slot.index == 0)
                    slot.copy(region = BanRegion(.08f, (108f + .052f * 864) / 1080, .03f * 864 / 1080))
                else slot
            }
            compose.setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f, 1f)) {
                    Box(Modifier.requiredSize(1280.dp, 576.dp)) { ScannerDebugOverlay(overlayRect = null) }
                }
            }
            val pixels = compose.onRoot().captureToImage().toPixelMap()
            fun hasCyanNear(x: Int, y: Int): Boolean = (x-2..x+2).any { px ->
                (y-2..y+2).any { py -> pixels[px, py].let { it.green > .5f && it.blue > .5f && it.red < .5f } }
            }
            assertTrue("Portrait center must return to screen space", hasCyanNear(154, 115))
            assertTrue("Ban rim must use the same inverse transform", hasCyanNear(120, 30))
        } finally {
            DraftVisionScanner.alignedConfigFlow.value = oldConfig
            DraftVisionScanner.alignedFrameSizeFlow.value = oldSize
            DraftVisionScanner.alignedViewportFlow.value = oldViewport
            DraftVisionScanner.banSlotsFlow.value = oldBans
            DraftVisionScanner.banDetectionActiveFlow.value = oldDetecting
        }
    }

    @OptIn(com.github.takahirom.roborazzi.ExperimentalRoborazziApi::class)
    @Test fun largeFontPocoCaptionsKeepBanLabelsAndActualFirstTitlesSeparate() {
        val ctx=RuntimeEnvironment.getApplication()
        val previous=DraftVisionScanner.calibrationConfig
        try {
            DraftVisionScanner.updateCalibration(ctx,VisionCalibrationConfig(
                allyAvatarCenterX=.12f,enemyAvatarCenterX=.911f,
                allySlotXRatios=List(5){.12f},enemySlotXRatios=List(5){.911f},
                allySlotYRatios=listOf(.184f,.320f,.454f,.592f,.728f),
                enemySlotYRatios=listOf(.179f,.315f,.449f,.587f,.723f)))
            DraftVisionScanner.banDetectionActiveFlow.value=true
            DraftVisionScanner.banSlotsFlow.value=emptyBanSlots().map { slot ->
                slot.copy(region=BanRegion((if(slot.side==BanSide.ALLY) .074f else .816f)+slot.index*.036f,.052f,.031f),state=BanState.UNKNOWN)
            }
            DraftVisionScanner.debugVisualMatches.value=mapOf("ally_0" to "Senna","enemy_0" to "Viktor","ally_3" to "Línea de Barón")
            compose.setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f,1.5f)) {
                    Box(Modifier.requiredSize(1280.dp,576.dp)) { ScannerDebugOverlay(overlayRect=null) }
                }
            }
            val evidence=java.io.File("build/reports/portuguese-rendered/poco-debug-large-font-es.png")
            evidence.parentFile.mkdirs()
            compose.onRoot().captureRoboImage(evidence.path,
                roborazziOptions=com.github.takahirom.roborazzi.RoborazziOptions(
                    taskType=com.github.takahirom.roborazzi.RoborazziTaskType.Record))
            assertTrue("Rendered Poco evidence must exist",evidence.length()>0)
            compose.runOnIdle {
                val masks=DraftVisionScanner.debugTextRects
                assertTrue("Rendered diagnostic masks: $masks",masks.size>=20)
                assertTrue("Test must draw at Poco screenshot width",masks.any {it.right>1000})
                val allyCaption=masks[10];val rivalCaption=masks[11]
                assertTrue(masks.take(5).none { Rect.intersects(it,allyCaption) })
                assertTrue(masks.drop(5).take(5).none { Rect.intersects(it,rivalCaption) })
                assertFalse(Rect.intersects(allyCaption,Rect(190,92,264,107)))
                assertFalse(Rect.intersects(rivalCaption,Rect(1080,92,1170,107)))
            }
        } finally { DraftVisionScanner.resetSlotMemory();DraftVisionScanner.updateCalibration(ctx,previous) }
    }
}
