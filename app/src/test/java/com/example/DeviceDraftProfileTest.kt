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
        val samsungS948 = VisionCalibrationConfig.measuredDefaultForCapture(
            "Samsung", "SM-S948B", 3120, 1440)
        assertNotNull(samsungS948)
        assertEquals(.1171875f, samsungS948!!.allyAvatarCenterX)
        assertEquals(.91328126f, samsungS948.enemyAvatarCenterX)
        assertEquals(.19071549f, samsungS948.allySlotYRatios.first())

        // Orientation may swap width and height, but another model or another
        // capture size must never inherit these coordinates.
        assertEquals(samsungS948, VisionCalibrationConfig.measuredDefaultForCapture(
            "samsung", "sm-s948b", 1440, 3120))
        assertNull(VisionCalibrationConfig.measuredDefaultForCapture(
            "Samsung", "SM-S938B", 3120, 1440))
        assertNull(VisionCalibrationConfig.measuredDefaultForCapture(
            "Samsung", "SM-S948B", 2340, 1080))
    }
    @Test fun sameResolutionDoesNotShareCoordinatesBetweenMeasuredPhones() {
        val a156 = VisionCalibrationConfig.measuredDefaultForCapture("samsung", "SM-A156U", 2340, 1080)
        val s942 = VisionCalibrationConfig.measuredDefaultForCapture("samsung", "SM-S942B", 2340, 1080)
        val s938 = VisionCalibrationConfig.measuredDefaultForCapture("samsung", "SM-S938B", 2340, 1080)
        assertNotNull(a156); assertNotNull(s942); assertNotNull(s938)
        assertNotEquals(a156!!.allyAvatarCenterX, s942!!.allyAvatarCenterX)
        assertNotEquals(s942.enemyAvatarCenterX, s938!!.enemyAvatarCenterX)
        assertEquals(.11862696f, VisionCalibrationConfig.measuredDefaultForCapture(
            "Xiaomi", "2412DPC0AG", 2712, 1220)!!.allyAvatarCenterX)
        assertEquals(.90703124f, VisionCalibrationConfig.measuredDefaultForCapture(
            "HONOR", "BVL-N49", 2800, 1280)!!.enemyAvatarCenterX)
        assertNull(VisionCalibrationConfig.measuredDefaultForCapture("unknown", "unknown", 2340, 1080))
    }
    @Test fun oppoCph2791UsesItsExactMeasuredCaptureProfile() {
        val oppo = VisionCalibrationConfig.measuredDefaultForCapture("OPPO", "CPH2791", 2354, 1080)
        assertNotNull(oppo)
        assertEquals(.12803629f, oppo!!.allyAvatarCenterX)
        assertEquals(.90703124f, oppo.enemyAvatarCenterX)
        assertEquals(.17041795f, oppo.topAlly5XRatio)
        assertEquals(.9736258f, oppo.topEnemy5XRatio)
        assertEquals(.18950696f, oppo.enemySlotYRatios.first())
        assertEquals(.72302145f, oppo.enemySlotYRatios.last())
        assertNull(VisionCalibrationConfig.measuredDefaultForCapture("OPPO", "CPH2791", 2340, 1080))
        assertNull(VisionCalibrationConfig.measuredDefaultForCapture("OPPO", "another", 2354, 1080))
    }
    @Test fun xiaomi25062Rn2dlUsesItsExactMeasuredCaptureProfile() {
        val xiaomi = VisionCalibrationConfig.measuredDefaultForCapture("Xiaomi", "25062RN2DL", 2340, 1080)
        assertNotNull(xiaomi)
        assertEquals(.15031247f, xiaomi!!.allyAvatarCenterX)
        assertEquals(.959f, xiaomi.enemyAvatarCenterX)
        assertEquals(.15031247f, xiaomi.allySlotXRatios.first())
        assertEquals(.11906247f, xiaomi.allySlotXRatios[1])
        assertEquals(.19120136f, xiaomi.allySlotYRatios.first())
        assertEquals(.72556216f, xiaomi.allySlotYRatios.last())
        assertEquals(.105f, xiaomi.allySlotDiameterRatios.last())
        assertEquals(.9734761f, xiaomi.topEnemy5XRatio)
        assertNull(VisionCalibrationConfig.measuredDefaultForCapture("Xiaomi", "25062RN2DL", 2400, 1080))
        assertNull(VisionCalibrationConfig.measuredDefaultForCapture("Xiaomi", "another", 2340, 1080))
    }
    @Test fun motorolaMotoG56UsesItsExactMeasuredCaptureProfile() {
        val moto = VisionCalibrationConfig.measuredDefaultForCapture("motorola", "moto g56 5G", 2400, 1080)
        assertNotNull(moto)
        assertEquals(.1171875f, moto!!.allyAvatarCenterX)
        assertEquals(.91421247f, moto.enemyAvatarCenterX)
        assertEquals(.16712953f, moto.topAlly5XRatio)
        assertEquals(.9741347f, moto.topEnemy5XRatio)
        assertEquals(.1892361f, moto.allySlotYRatios.first())
        assertEquals(.7256944f, moto.allySlotYRatios.last())
        assertEquals(.1f, moto.allySlotDiameterRatios.last())
        assertEquals(.91421247f, moto.enemySlotXRatios.first())
        assertEquals(.722f, moto.enemySlotYRatios.last())
        assertNull(VisionCalibrationConfig.measuredDefaultForCapture("motorola", "moto g56 5G", 2340, 1080))
        assertNull(VisionCalibrationConfig.measuredDefaultForCapture("motorola", "another", 2400, 1080))
    }

    @Test fun nubiaNx809jUsesTheOwnerMeasurementsAndObservedCaptureAlias() {
        val panel = VisionCalibrationConfig.measuredDefaultForCapture("nubia", "NX809J", 2688, 1216)
        assertNotNull(panel)
        assertEquals(.08704145f, panel!!.allyAvatarCenterX)
        assertEquals(.975f, panel.enemyAvatarCenterX)
        assertEquals(.23799995f, panel.allySlotYRatios.first())
        assertEquals(.68706477f, panel.enemySlotYRatios.last())
        assertEquals(.1680967f, panel.topAlly5XRatio)
        assertEquals(.973985f, panel.topEnemy5XRatio)
        assertEquals(.17209902f, panel.allyBanXRatios.last())

        // This is the exact capture surface reported by the diagnostic for the
        // same model; it uses the same normalized measurements, not a shared
        // resolution fallback.
        val capture = VisionCalibrationConfig.measuredDefaultForCapture("nubia", "NX809J", 1920, 869)
        assertEquals(panel, capture)
        assertNull(VisionCalibrationConfig.measuredDefaultForCapture("nubia", "NX809J", 1920, 868))
        assertNull(VisionCalibrationConfig.measuredDefaultForCapture("nubia", "another", 2688, 1216))
    }
    @Test fun nineConfirmedTitlesIdentifyTheSideRegardlessOfAnEarlierWrongPrefix() {
        val all = List(5) { true }
        val ninth = listOf(true, true, true, true, false)
        assertEquals(false, DraftPickOrderPolicy.tenthSideFromTitles(all, ninth))
        assertEquals(true, DraftPickOrderPolicy.tenthSideFromTitles(ninth, all))
        assertNull(DraftPickOrderPolicy.tenthSideFromTitles(ninth, ninth))
    }
}
