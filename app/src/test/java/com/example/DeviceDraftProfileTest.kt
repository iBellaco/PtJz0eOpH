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
    private data class ProfileKey(val manufacturer: String, val model: String, val width: Int, val height: Int)
    @Test fun redMagicOldCoordinatesAreDiscardedOnceButNewAdjustmentsAndOtherPhonesSurvive() {
        val context=RuntimeEnvironment.getApplication()
        val prefs=context.getSharedPreferences("vision_calibration_prefs",0)
        prefs.edit().clear().commit()
        ShadowBuild.setManufacturer("nubia");ShadowBuild.setModel("NX789J")
        val key=VisionCalibrationConfig.captureProfileKey(2688,1216)
        try {
            // Seed the OLD on-device format directly, before the migration exists.
            prefs.edit().putString("calibration_device_model","nubia:nx789j")
                .putFloat("ally_slot_x_0",.30f).putFloat("avatarDiameterRatio",.20f)
                .putFloat("profile_nubia:nx789j_${key}_ally_slot_x_0",.30f)
                .putFloat("profile_${key}_ally_slot_x_0",.30f)
                .putFloat("profile_xiaomi:2412dpc0ag_${key}_ally_slot_x_0",.12f)
                .putString("unrelated_setting","keep").commit()
            assertNull(VisionCalibrationConfig.loadProfileFromPrefs(context,key))
            assertFalse(VisionCalibrationConfig.hasCaptureProfiles(context))
            assertEquals(VisionCalibrationConfig(),VisionCalibrationConfig.loadFromPrefs(context))
            assertEquals("keep",prefs.getString("unrelated_setting",null))
            assertTrue(prefs.contains("profile_xiaomi:2412dpc0ag_${key}_ally_slot_x_0"))
            val fresh=VisionCalibrationConfig().adjustDraftSlot(true,0,deltaX=.01f)
            fresh.saveToPrefs(context);fresh.saveProfileToPrefs(context,key)
            assertEquals(fresh,VisionCalibrationConfig.loadFromPrefs(context))
            assertEquals(fresh,VisionCalibrationConfig.loadProfileFromPrefs(context,key))
        } finally { prefs.edit().clear().commit();ShadowBuild.reset() }
    }
    @Test fun captureProfilesAreScopedToDeviceAndAspectWithoutLosingManualAdjustments() {
        val context=RuntimeEnvironment.getApplication()
        val prefs=context.getSharedPreferences("vision_calibration_prefs",0)
        prefs.edit().clear().commit()
        try {
            ShadowBuild.setManufacturer("nubia");ShadowBuild.setModel("NX789J")
            val key=VisionCalibrationConfig.captureProfileKey(2688,1216)
            val manual=VisionCalibrationConfig(nameBandScale=.45f).adjustDraftSlot(true,2,deltaX=.01f)
            manual.saveProfileToPrefs(context,key)
            assertEquals(manual,VisionCalibrationConfig.loadProfileFromPrefs(context,key))
            assertTrue(VisionCalibrationConfig.hasCaptureProfiles(context))
            assertNull(VisionCalibrationConfig.loadProfileFromPrefs(context,VisionCalibrationConfig.captureProfileKey(1920,1080)))
            ShadowBuild.setManufacturer("Xiaomi");ShadowBuild.setModel("2412DPC0AG")
            assertNull(VisionCalibrationConfig.loadProfileFromPrefs(context,key))
            assertFalse(VisionCalibrationConfig.hasCaptureProfiles(context))
            ShadowBuild.setManufacturer("nubia");ShadowBuild.setModel("NX789J")
            assertEquals(manual,VisionCalibrationConfig.loadProfileFromPrefs(context,key))
        } finally {prefs.edit().clear().commit();ShadowBuild.reset()}
    }
    @Test fun pocoOldCoordinatesAreMigratedAndNewManualControlsSurviveReload() {
        val context = RuntimeEnvironment.getApplication()
        val prefs = context.getSharedPreferences("vision_calibration_prefs", 0)
        prefs.edit().clear().commit()
        ShadowBuild.setManufacturer("Xiaomi")
        ShadowBuild.setModel("2412DPC0AG")
        try {
            val key=VisionCalibrationConfig.captureProfileKey(2712,1220)
            prefs.edit().putString("calibration_device_model","xiaomi:2412dpc0ag")
                .putFloat("ally_slot_x_0",.12f).putFloat("avatarDiameterRatio",.11f)
                .putFloat("profile_xiaomi:2412dpc0ag_${key}_ally_slot_x_0",.12f)
                .putFloat("profile_${key}_ally_slot_x_0",.12f)
                .putString("unrelated_setting","keep").commit()
            assertNull(VisionCalibrationConfig.loadProfileFromPrefs(context,key))
            assertEquals("keep",prefs.getString("unrelated_setting",null))
            com.example.service.screen.DraftVisionScanner.initCalibration(context)
            val preset = com.example.service.screen.DraftVisionScanner.calibrationConfig
            assertEquals(VisionCalibrationConfig(),preset)
            val manual = preset.copy(nameBandScale = .7f).adjustDraftSlot(true, 0, deltaX = .005f, deltaY = -.005f)
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
    @Test fun everyBrandUsesTheSameAdaptiveStartingGeometry() {
        for ((brand,model) in listOf("nubia" to "NX789J", "Xiaomi" to "2412DPC0AG",
            "POCO" to "POCO X7 Pro", "Xiaomi" to "2412DPC0AI", "Honor" to "Magic V2",
            "Samsung" to "S26", "Google" to "Pixel", "unknown" to "unknown")) {
            assertEquals("$brand/$model",VisionCalibrationConfig(),VisionCalibrationConfig.defaultsForDevice(brand,model))
        }
    }
    @Test fun measuredDefaultsRequireTheExactDeviceAndCaptureDimensions() {
        val kenshin = VisionCalibrationConfig.measuredDefaultForCapture(
            "motorola", "motorola edge 30 neo", 2174, 1080)
        assertNotNull(kenshin)
        assertEquals(.107000016f, kenshin!!.allyAvatarCenterX)
        assertEquals(.919f, kenshin.enemyAvatarCenterX)
        assertEquals(.188f, kenshin.allySlotYRatios.first())
        assertEquals(.168f, kenshin.topAlly5XRatio)
        assertEquals(.974f, kenshin.topEnemy5XRatio)
        assertEquals(.172f, kenshin.allyBanXRatios.last())

        // Orientation may swap width and height, but another model or another
        // capture size must never inherit these coordinates.
        assertEquals(kenshin, VisionCalibrationConfig.measuredDefaultForCapture(
            "MOTOROLA", "MOTOROLA EDGE 30 NEO", 1080, 2174))
        assertEquals(kenshin, VisionCalibrationConfig.measuredDefaultForCapture(
            "motorola", "motorola edge 30 neo", 1920, 954))
        assertNull(VisionCalibrationConfig.measuredDefaultForCapture(
            "motorola", "motorola edge 30 neo", 2340, 1080))
        assertNull(VisionCalibrationConfig.measuredDefaultForCapture(
            "motorola", "another", 2174, 1080))
    }

    @Test fun previousMeasuredCatalogIsNotUsedAfterTheOwnerReset() {
        for ((manufacturer, model, width, height) in listOf(
            ProfileKey("samsung", "SM-S948B", 3120, 1440),
            ProfileKey("Xiaomi", "25062RN2DL", 2340, 1080),
            ProfileKey("OPPO", "CPH2791", 2354, 1080),
            ProfileKey("nubia", "NX809J", 2688, 1216)
        )) {
            assertNull(VisionCalibrationConfig.measuredDefaultForCapture(manufacturer, model, width, height))
        }
    }
    @Test fun nineConfirmedTitlesIdentifyTheSideRegardlessOfAnEarlierWrongPrefix() {
        val all = List(5) { true }
        val ninth = listOf(true, true, true, true, false)
        assertEquals(false, DraftPickOrderPolicy.tenthSideFromTitles(all, ninth))
        assertEquals(true, DraftPickOrderPolicy.tenthSideFromTitles(ninth, all))
        assertNull(DraftPickOrderPolicy.tenthSideFromTitles(ninth, ninth))
    }
}
