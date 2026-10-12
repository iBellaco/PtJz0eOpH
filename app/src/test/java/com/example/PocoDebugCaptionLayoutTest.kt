package com.example

import android.app.Application
import android.graphics.Rect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
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
@Config(sdk=[34],application=Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PocoDebugCaptionLayoutTest {
    @get:Rule val compose=createComposeRule()
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
                    Box(Modifier.width(1280.dp).height(576.dp)) { ScannerDebugOverlay(overlayRect=null) }
                }
            }
            compose.runOnIdle {
                val masks=DraftVisionScanner.debugTextRects
                assertTrue(masks.size>=20)
                val allyCaption=masks[10];val rivalCaption=masks[11]
                assertTrue(masks.take(5).none { Rect.intersects(it,allyCaption) })
                assertTrue(masks.drop(5).take(5).none { Rect.intersects(it,rivalCaption) })
                assertFalse(Rect.intersects(allyCaption,Rect(190,92,264,107)))
                assertFalse(Rect.intersects(rivalCaption,Rect(1080,92,1170,107)))
            }
        } finally { DraftVisionScanner.resetSlotMemory();DraftVisionScanner.updateCalibration(ctx,previous) }
    }
}
