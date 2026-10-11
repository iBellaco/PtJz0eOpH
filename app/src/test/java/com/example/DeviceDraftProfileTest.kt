package com.example

import android.app.Application
import com.example.service.screen.VisionCalibrationConfig
import com.example.service.screen.DraftPickOrderPolicy
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBuild

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class DeviceDraftProfileTest {
    @Test fun pocoMeasuredCoordinatesAreLoadedAndManualControlsSurviveReload() {
        val context = RuntimeEnvironment.getApplication()
        val prefs = context.getSharedPreferences("vision_calibration_prefs", 0)
        prefs.edit().clear().commit()
        ShadowBuild.setManufacturer("Xiaomi")
        ShadowBuild.setModel("2412DPC0AG")
        try {
            com.example.service.screen.DraftVisionScanner.initCalibration(context)
            val preset = com.example.service.screen.DraftVisionScanner.calibrationConfig
            assertEquals(.120f, preset.getAllySlotX(0), .00001f)
            assertEquals(.911f, preset.getEnemySlotX(0), .00001f)
            assertEquals(listOf(.184f, .320f, .454f, .592f, .728f), preset.allySlotYRatios)
            assertEquals(listOf(.179f, .315f, .449f, .587f, .723f), preset.enemySlotYRatios)
            val manual = preset.adjustDraftSlot(true, 0, deltaX = .005f, deltaY = -.005f)
            manual.saveToPrefs(context)
            com.example.service.screen.DraftVisionScanner.initCalibration(context)
            assertEquals(manual, com.example.service.screen.DraftVisionScanner.calibrationConfig)
            assertEquals(manual, com.example.service.screen.DraftVisionScanner.calibrationConfigFlow.value)
            ShadowBuild.setManufacturer("nubia")
            ShadowBuild.setModel("NX789J")
            com.example.service.screen.DraftVisionScanner.initCalibration(context)
            assertEquals(VisionCalibrationConfig(), com.example.service.screen.DraftVisionScanner.calibrationConfig)
        } finally { prefs.edit().clear().commit(); ShadowBuild.reset() }
    }
    @Test fun nineConfirmedTitlesIdentifyTheSideRegardlessOfAnEarlierWrongPrefix() {
        val all = List(5) { true }
        val ninth = listOf(true, true, true, true, false)
        assertEquals(false, DraftPickOrderPolicy.tenthSideFromTitles(all, ninth))
        assertEquals(true, DraftPickOrderPolicy.tenthSideFromTitles(ninth, all))
        assertNull(DraftPickOrderPolicy.tenthSideFromTitles(ninth, ninth))
    }
}
