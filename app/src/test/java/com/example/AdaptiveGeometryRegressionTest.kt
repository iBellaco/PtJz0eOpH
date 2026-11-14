package com.example

import com.example.service.screen.AdaptiveScreenLayoutEngine
import com.example.service.screen.BanSide
import com.example.service.screen.VisionCalibrationConfig
import com.example.service.screen.computeAdaptiveBanRegions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveGeometryRegressionTest {
    @Test fun genericSideSlotsScaleWithAspectRatioAroundTheSixteenByNineReference() {
        val engine = AdaptiveScreenLayoutEngine
        val canonical = VisionCalibrationConfig()
        val standard = engine.computeAdaptiveConfig(1920, 1080, canonical)
        val panoramic = engine.computeAdaptiveConfig(2400, 1080, canonical)

        assertEquals(.072f, standard.getAllySlotX(0), .0001f)
        assertEquals(.959f, standard.getEnemySlotX(4), .0001f)
        assertEquals(.0576f, panoramic.getAllySlotX(0), .0002f)
        assertEquals(.9672f, panoramic.getEnemySlotX(4), .0002f)
        assertTrue(panoramic.allyOcrMinX < panoramic.allyOcrMaxX)
        assertTrue(panoramic.enemyOcrMinX < panoramic.enemyOcrMaxX)
    }

    @Test fun explicitCalibrationRemainsFrameRelative() {
        val calibrated = VisionCalibrationConfig(
            allyAvatarCenterX = .157f,
            enemyAvatarCenterX = .843f,
            allySlotXRatios = List(5) { .157f },
            enemySlotXRatios = List(5) { .843f }
        )
        val result = AdaptiveScreenLayoutEngine.computeAdaptiveConfig(2400, 1080, calibrated)
        assertEquals(.157f, result.getAllySlotX(0), .0001f)
        assertEquals(.843f, result.getEnemySlotX(4), .0001f)
    }

    @Test fun banRowsUseHeightBasedNominalSpacing() {
        val standard = computeAdaptiveBanRegions(16f / 9f, BanSide.ALLY)
        val panoramic = computeAdaptiveBanRegions(20f / 9f, BanSide.ALLY)
        assertEquals(.028125f, standard.first().x, .0002f)
        assertEquals(.045f, standard[1].x - standard[0].x, .0002f)
        assertEquals(.0225f, panoramic.first().x, .0002f)
        assertEquals(.036f, panoramic[1].x - panoramic[0].x, .0002f)
        assertEquals(1f - panoramic[4].x, computeAdaptiveBanRegions(20f / 9f, BanSide.ENEMY).first().x, .0002f)
    }
}
