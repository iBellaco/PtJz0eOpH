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
    @Test fun measuredDefaultsAreEmptyAfterTheCoordinateReset() {
        // The application no longer ships device-specific coordinate presets.
        // Every device starts with adaptive geometry and may save a calibration
        // only when the user explicitly creates one at runtime.
        data class Case(val manufacturer: String, val model: String, val width: Int, val height: Int)
        for (case in listOf(
            Case("motorola", "motorola edge 30 neo", 2174, 1080),
            Case("MOTOROLA", "MOTOROLA EDGE 30 NEO", 1080, 2174),
            Case("motorola", "motorola edge 30 neo", 1920, 954),
            Case("motorola", "motorola edge 30 neo", 2340, 1080),
            Case("motorola", "another", 2174, 1080)
        )) {
            assertNull(VisionCalibrationConfig.measuredDefaultForCapture(
                case.manufacturer, case.model, case.width, case.height))
        }
    }

    @Test fun nx789jReferenceIsScopedToTheExactDeviceAndCaptureAliases() {
        val physical = requireNotNull(VisionCalibrationConfig.measuredDefaultForCapture(
            "nubia", "NX789J", 2688, 1216
        ))
        val capture = requireNotNull(VisionCalibrationConfig.measuredDefaultForCapture(
            "NUBIA", "nx789j", 1920, 869
        ))
        assertEquals(physical, capture)
        assertEquals(listOf(.028f, .064f, .100f, .136f, .172f), physical.allyBanXRatios)
        assertEquals(listOf(.826f, .863f, .900f, .937f, .974f), physical.enemyBanXRatios)
        assertEquals(.168f, physical.topAlly5XRatio, 0.0001f)
        assertEquals(.974f, physical.topEnemy5XRatio, 0.0001f)
        assertNull(VisionCalibrationConfig.measuredDefaultForCapture("nubia", "NX789J", 1920, 868))
        assertNull(VisionCalibrationConfig.measuredDefaultForCapture("Xiaomi", "NX789J", 2688, 1216))
    }

    @Test fun xiaomi24122rkc7gReferenceKeepsSuppliedBanAndSlotGeometry() {
        val physical = requireNotNull(VisionCalibrationConfig.measuredDefaultForCapture(
            "Xiaomi", "24122RKC7G", 2400, 1080
        ))
        val aligned = requireNotNull(VisionCalibrationConfig.measuredDefaultForCapture(
            "xiaomi", "24122rkc7g", 1920, 864
        ))
        val literal = requireNotNull(VisionCalibrationConfig.measuredDefaultForCapture(
            "XIAOMI", "24122RKC7G", 1280, 576
        ))

        assertEquals(physical, aligned)
        assertEquals(physical, literal)
        val xs = physical.allyBanXRatios.map { it * 1280 }
        assertEquals(88f, xs.first(), 1f)
        assertEquals(275f, xs.last(), 1f)
        assertTrue(xs.zipWithNext().all { (a, b) -> b - a in 46f..48f })
        assertEquals(listOf(.785f, .82125f, .856875f, .8925f, .93f), physical.enemyBanXRatios)
        physical.allyBanYRatios.forEach { assertEquals(30f, it * 576, .01f) }
        val legacy = physical.copy(allyBanXRatios = listOf(.06875f, .087500006f, .13f, .1725f, .21499999f),
            allyBanYRatios = listOf(.05277778f, .06388889f, .05138889f, .043055557f, .05138889f))
        assertEquals(physical, com.example.service.screen.MeasuredDeviceCalibrationDefaults.restoreBanRow(legacy, literal))
        val custom = legacy.adjustBanSlot(true, 1, deltaX = .005f)
        assertEquals(custom, com.example.service.screen.MeasuredDeviceCalibrationDefaults.restoreBanRow(custom, literal))
        assertEquals(listOf(.05138889f, .05138889f, .05138889f, .047222223f, .05277778f), physical.enemyBanYRatios)
        assertTrue(physical.manualAllyBans)
        assertTrue(physical.manualEnemyBans)
        assertEquals(listOf(.188f, .324f, .458f, .591f, .725f), physical.allySlotYRatios)
        assertEquals(listOf(.18861003f, .32380405f, .4560159f, .58822775f, .71944547f), physical.enemySlotYRatios)
        assertEquals(.11662696f, physical.allyAvatarCenterX, 0.000001f)
        assertEquals(.9165625f, physical.enemyAvatarCenterX, 0.000001f)
        assertEquals(.16712953f, physical.topAllyXRatios.last(), 0.000001f)
        assertEquals(.9741347f, physical.topEnemyXRatios.last(), 0.000001f)
        assertNull(VisionCalibrationConfig.measuredDefaultForCapture("Xiaomi", "24122RKC7G", 2400, 1081))
        assertNull(VisionCalibrationConfig.measuredDefaultForCapture("Xiaomi", "24122DPC0AG", 2400, 1080))
    }

    @Test fun oppoCph2791ReferenceKeepsManualBanRowsSeparateFromPickColumns() {
        val physical = requireNotNull(VisionCalibrationConfig.measuredDefaultForCapture(
            "OPPO", "CPH2791", 2354, 1080
        ))
        val aligned = requireNotNull(VisionCalibrationConfig.measuredDefaultForCapture(
            "oppo", "cph2791", 1920, 881
        ))
        val literal = requireNotNull(VisionCalibrationConfig.measuredDefaultForCapture(
            "OPPO", "CPH2791", 1280, 587
        ))

        assertEquals(physical, aligned)
        assertEquals(physical, literal)
        assertEquals(listOf(.078402996f, .11492116f, .15143925f, .18795736f, .22447549f), physical.allyBanXRatios)
        assertEquals(listOf(.77501595f, .8100701f, .8470363f, .8833652f, .9203314f), physical.enemyBanXRatios)
        assertEquals(List(5) { .052f }, physical.allyBanYRatios)
        assertEquals(listOf(.05f, .048611112f, .048611112f, .05f, .05277778f), physical.enemyBanYRatios)
        assertTrue(physical.manualAllyBans)
        assertTrue(physical.manualEnemyBans)
        assertEquals(.12303629f, physical.allyAvatarCenterX, .000001f)
        assertEquals(.90703124f, physical.enemyAvatarCenterX, .000001f)
        assertEquals(listOf(.188f, .324f, .458f, .591f, .725f), physical.allySlotYRatios)
        assertEquals(listOf(.18950696f, .3253833f, .45826235f, .5911414f, .72302145f), physical.enemySlotYRatios)
        assertEquals(.17041795f, physical.topAlly5XRatio, .000001f)
        assertEquals(.9736258f, physical.topEnemy5XRatio, .000001f)
        assertNull(VisionCalibrationConfig.measuredDefaultForCapture("OPPO", "CPH2791", 2354, 1081))
        assertNull(VisionCalibrationConfig.measuredDefaultForCapture("Xiaomi", "CPH2791", 2354, 1080))
    }

    @Test fun samsungSmS938bReferenceKeepsMeasuredRowsAndMovedAllySlots() {
        val physical = requireNotNull(VisionCalibrationConfig.measuredDefaultForCapture(
            "samsung", "SM-S938B", 2340, 1080
        ))
        val aligned = requireNotNull(VisionCalibrationConfig.measuredDefaultForCapture(
            "SAMSUNG", "sm-s938b", 1920, 886
        ))
        val literal = requireNotNull(VisionCalibrationConfig.measuredDefaultForCapture(
            "Samsung", "SM-S938B", 1280, 590
        ))

        assertEquals(physical, aligned)
        assertEquals(physical, literal)
        assertEquals(listOf(.06923077f, .10512821f, .14166667f, .17884615f, .21730769f), physical.allyBanXRatios)
        assertEquals(listOf(.7826923f, .8211538f, .85641026f, .8923077f, .9301282f), physical.enemyBanXRatios)
        assertFalse(physical.manualAllyBans)
        assertFalse(physical.manualEnemyBans)
        assertTrue(physical.hasExplicitBanGeometry(true))
        assertTrue(physical.hasExplicitBanGeometry(false))
        assertTrue(physical.measuredBanGeometry)
        assertEquals(listOf(.1140625f, .1140625f, .06953125f, .06953125f, .06953125f), physical.allySlotXRatios)
        assertEquals(listOf(.1928934f, .32787907f, .42808804f, .5578877f, .6886633f), physical.allySlotYRatios)
        assertEquals(.17138515f, physical.topAlly5XRatio, .000001f)
        assertEquals(.9734761f, physical.topEnemy5XRatio, .000001f)
        assertNull(VisionCalibrationConfig.measuredDefaultForCapture("Samsung", "SM-S938B", 2340, 1081))
        assertNull(VisionCalibrationConfig.measuredDefaultForCapture("Samsung", "SM-S938A", 2340, 1080))
    }

    @Test fun unrelatedOldMeasuredCatalogIsNotUsedAfterTheOwnerReset() {
        for ((manufacturer, model, width, height) in listOf(
            ProfileKey("samsung", "SM-S948B", 3120, 1440),
            ProfileKey("Xiaomi", "25062RN2DL", 2340, 1080),
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
