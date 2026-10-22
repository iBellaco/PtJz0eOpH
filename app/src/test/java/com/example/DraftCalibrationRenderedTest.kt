package com.example

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.service.screen.DraftVisionScanner
import com.example.service.screen.VisionCalibrationConfig
import com.example.ui.components.LiteRTEngineViewerDialog
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h891dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DraftCalibrationRenderedTest {
    @get:Rule val compose = createComposeRule()

    @Test fun firstManualMovementStartsAtTheObservedColumnNotTheDevicePreset() {
        val context=RuntimeEnvironment.getApplication()
        val original=DraftVisionScanner.calibrationConfig
        try {
            DraftVisionScanner.updateCalibration(context,VisionCalibrationConfig())
            val observed=VisionCalibrationConfig().copy(allyAvatarCenterX=.105f,
                allySlotXRatios=List(5){.105f},enemyAvatarCenterX=.920f,enemySlotXRatios=List(5){.920f})
            DraftVisionScanner.alignedConfigFlow.value=observed
            compose.setContent { MyApplicationTheme { LiteRTEngineViewerDialog({},initializeDiagnostics=false) } }
            compose.onNodeWithTag("calibration_target_ally_column_x").performScrollTo().performClick()
            compose.onNodeWithTag("vision_calibration_right").performScrollTo().performClick()
            compose.waitForIdle()
            assertEquals(.110f,DraftVisionScanner.calibrationConfig.getAllySlotX(0),.00001f)
            assertEquals(observed.enemySlotXRatios,DraftVisionScanner.calibrationConfig.enemySlotXRatios)
        } finally { DraftVisionScanner.updateCalibration(context,original) }
    }

    @Test fun bothBanRowsMoveInFourDirectionsResizeUniformlyAndPersist() {
        val context = RuntimeEnvironment.getApplication()
        val original = DraftVisionScanner.calibrationConfig
        try {
            DraftVisionScanner.updateCalibration(context, VisionCalibrationConfig())
            compose.setContent { MyApplicationTheme { LiteRTEngineViewerDialog({}, initializeDiagnostics = false) } }
            for (ally in listOf(true, false)) {
                val group = if (ally) "calibration_allies" else "calibration_rivals"
                val side = if (ally) "ally" else "enemy"
                compose.onNodeWithTag("calibration_target_${side}_column_x", useUnmergedTree = true)
                    .assert(hasAnyAncestor(hasTestTag(group)))
                for (index in 1..5) compose.onNodeWithTag("calibration_target_${side}_slot_$index", useUnmergedTree = true)
                    .assert(hasAnyAncestor(hasTestTag(group)))
            }
            for (ally in listOf(true, false)) {
                compose.onNodeWithTag(if (ally) "calibration_target_ally_bans" else "calibration_target_enemy_bans").performScrollTo().performClick()
                val before = DraftVisionScanner.calibrationConfig
                fun press(tag: String) { compose.onNodeWithTag(tag).performScrollTo().performClick(); compose.waitForIdle() }
                press("vision_calibration_right")
                val right = DraftVisionScanner.calibrationConfig
                val xs = if (ally) before.allyBanXRatios else before.enemyBanXRatios
                val moved = if (ally) right.allyBanXRatios else right.enemyBanXRatios
                xs.indices.forEach { assertEquals(xs[it] + .005f, moved[it], .00001f) }
                press("vision_calibration_left"); press("vision_calibration_down")
                assertEquals((if (ally) before.allyBanYRatio else before.enemyBanYRatio) + .005f,
                    if (ally) DraftVisionScanner.calibrationConfig.allyBanYRatio else DraftVisionScanner.calibrationConfig.enemyBanYRatio, .00001f)
                press("vision_calibration_up"); press("calibration_size_increase")
                assertEquals(before.banDiameterRatio + .005f, DraftVisionScanner.calibrationConfig.banDiameterRatio, .00001f)
                press("calibration_size_decrease")
                assertEquals(before.allySlotYRatios, DraftVisionScanner.calibrationConfig.allySlotYRatios)
                assertEquals(before.enemySlotXRatios, DraftVisionScanner.calibrationConfig.enemySlotXRatios)
                assertEquals(DraftVisionScanner.calibrationConfig, VisionCalibrationConfig.loadFromPrefs(context))
            }
        } finally { DraftVisionScanner.updateCalibration(context, original) }
    }

    @Test fun everyIndividualAvatarRespondsToFourDirectionsAndSizeWithoutMovingNeighbours() {
        val context = RuntimeEnvironment.getApplication()
        val original = DraftVisionScanner.calibrationConfig
        try {
            DraftVisionScanner.updateCalibration(context, VisionCalibrationConfig())
            compose.setContent { MyApplicationTheme { LiteRTEngineViewerDialog({}, initializeDiagnostics = false) } }
            for (ally in listOf(true, false)) for (index in 0..4) {
                val side = if (ally) "ally" else "enemy"
                compose.onNodeWithTag("calibration_target_${side}_slot_${index+1}").performScrollTo().performClick()
                val before = DraftVisionScanner.calibrationConfig
                fun press(tag: String) { compose.onNodeWithTag(tag).performScrollTo().performClick(); compose.waitForIdle() }
                press("vision_calibration_right")
                val right = DraftVisionScanner.calibrationConfig
                assertEquals((if (ally) before.getAllySlotX(index) else before.getEnemySlotX(index)) + .005f,
                    if (ally) right.getAllySlotX(index) else right.getEnemySlotX(index), .00001f)
                press("vision_calibration_left")
                press("vision_calibration_down")
                val down = DraftVisionScanner.calibrationConfig
                assertEquals((if (ally) before.allySlotYRatios else before.enemySlotYRatios)[index] + .005f,
                    (if (ally) down.allySlotYRatios else down.enemySlotYRatios)[index], .00001f)
                press("vision_calibration_up")
                press("calibration_size_increase")
                val enlarged = DraftVisionScanner.calibrationConfig
                assertEquals(before.getSlotDiameter(ally, index) + .005f, enlarged.getSlotDiameter(ally, index), .00001f)
                for (other in 0..4) if (other != index) assertEquals(before.getSlotDiameter(ally, other), enlarged.getSlotDiameter(ally, other), 0f)
                assertEquals(if (ally) before.enemySlotXRatios else before.allySlotXRatios,
                    if (ally) enlarged.enemySlotXRatios else enlarged.allySlotXRatios)
                press("calibration_size_decrease")
                val restored = DraftVisionScanner.calibrationConfig
                for (slot in 0..4) {
                    assertEquals(before.getAllySlotX(slot), restored.getAllySlotX(slot), .00001f)
                    assertEquals(before.getEnemySlotX(slot), restored.getEnemySlotX(slot), .00001f)
                    assertEquals(before.allySlotYRatios[slot], restored.allySlotYRatios[slot], .00001f)
                    assertEquals(before.enemySlotYRatios[slot], restored.enemySlotYRatios[slot], .00001f)
                    assertEquals(before.getSlotDiameter(ally, slot), restored.getSlotDiameter(ally, slot), .00001f)
                }
            }
        } finally { DraftVisionScanner.updateCalibration(context, original) }
    }
}
