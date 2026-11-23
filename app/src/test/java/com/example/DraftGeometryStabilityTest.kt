package com.example

import com.example.service.screen.DraftGeometryStability
import com.example.service.screen.VisionCalibrationConfig
import org.junit.Assert.*
import org.junit.Test

class DraftGeometryStabilityTest {
    @Test fun overlappingRimsCannotMoveEitherGridOrPreventAnIndependentHorizontalPreselection() {
        val base = VisionCalibrationConfig()
        val filter = DraftGeometryStability()
        val collided = base.copy(
            allySlotXRatios = base.allySlotXRatios.toMutableList().apply { this[2] += .045f },
            allySlotYRatios = base.allySlotYRatios.toMutableList().apply { this[2] = this[1] + .065f },
            enemySlotDiameterRatios = base.enemySlotDiameterRatios.toMutableList().apply { this[1] = .24f })
        repeat(3) {
            val result = filter.update(collided, baseline = base, frameHeight = 869)
            assertEquals(collided.allySlotXRatios, result.allySlotXRatios)
            assertEquals(base.allySlotYRatios, result.allySlotYRatios)
            assertEquals(base.enemySlotDiameterRatios, result.enemySlotDiameterRatios)
        }
        val shifted = base.copy(enemySlotYRatios = base.enemySlotYRatios.map { it + .025f })
        assertEquals(shifted.enemySlotYRatios, filter.update(shifted).enemySlotYRatios)
    }

    @Test fun changingRowsAtDifferentTimesNeverPublishesAMixedVerticalGrid() {
        val base = VisionCalibrationConfig()
        val filter = DraftGeometryStability()
        filter.update(base)
        val first = base.copy(allySlotYRatios = base.allySlotYRatios.map { it + .035f })
        val second = first.copy(allySlotYRatios = first.allySlotYRatios.mapIndexed { slot, y ->
            if (slot == 3) y + .009f else y
        })
        assertEquals(base.allySlotYRatios, filter.update(first).allySlotYRatios)
        assertEquals(base.allySlotYRatios, filter.update(second).allySlotYRatios)
        assertEquals(second.allySlotYRatios, filter.update(second).allySlotYRatios)
    }

    @Test fun framesWithoutRivalRingEvidenceCannotLockDefaultCoordinates() {
        val filter = DraftGeometryStability()
        val base = VisionCalibrationConfig()
        repeat(4) { filter.update(base, enemyMeasured=false) }
        val measured = base.copy(enemySlotXRatios = List(5) { .91f })
        assertEquals(measured.enemySlotXRatios, filter.update(measured).enemySlotXRatios)
        filter.update(measured)
        repeat(4) { assertEquals(measured.enemySlotXRatios, filter.update(base).enemySlotXRatios) }
    }
    @Test fun confirmedRivalPositionsRemainFixedUntilViewportReset() {
        val filter = DraftGeometryStability()
        val base = VisionCalibrationConfig()
        filter.update(base)
        filter.update(base)
        val moving = base.copy(enemySlotXRatios = List(5) { .83f },
            enemySlotYRatios = base.enemySlotYRatios.map { it + .03f },
            enemySlotDiameterRatios = base.enemySlotDiameterRatios.map { it * .8f })
        repeat(20) {
            val stable = filter.update(moving)
            assertEquals(base.enemySlotXRatios, stable.enemySlotXRatios)
            assertEquals(base.enemySlotYRatios, stable.enemySlotYRatios)
            assertEquals(base.enemySlotDiameterRatios, stable.enemySlotDiameterRatios)
        }
        filter.reset()
        assertEquals(moving.enemySlotXRatios, filter.update(moving).enemySlotXRatios)
    }
    @Test fun movingOneAllyDoesNotBlockAnotherRowOrMoveRivals() {
        val filter = DraftGeometryStability()
        val base = VisionCalibrationConfig()
        filter.update(base)
        val first = base.copy(allySlotXRatios = base.allySlotXRatios.toMutableList().apply { this[3] += .05f })
        filter.update(first)
        val second = first.copy(allySlotXRatios = first.allySlotXRatios.toMutableList().apply { this[4] += .04f })
        val result = filter.update(second)
        assertEquals(first.allySlotXRatios[3], result.allySlotXRatios[3])
        assertEquals(base.allySlotXRatios[4], result.allySlotXRatios[4])
        assertEquals(base.enemySlotXRatios, result.enemySlotXRatios)
        assertEquals(second.allySlotXRatios, filter.update(second).allySlotXRatios)
        filter.update(base)
        assertEquals(base.allySlotXRatios, filter.update(base).allySlotXRatios)
    }
    private val base = VisionCalibrationConfig()
    private val changed = base.copy(allySlotXRatios = List(5) { .12f },
        allySlotDiameterRatios = base.allySlotDiameterRatios.map { it * .8f })

    @Test fun alternatingSizesAndOffsetsDoNotMakeTheViewerJump() {
        val filter = DraftGeometryStability()
        filter.update(base)
        repeat(20) {
            assertEquals(base, filter.update(changed))
            assertEquals(base, filter.update(base))
        }
    }

    @Test fun coherentChangeIsAcceptedAfterTwoObservationsAndSidesAreIndependent() {
        val filter = DraftGeometryStability()
        filter.update(base)
        assertEquals(base, filter.update(changed))
        val accepted = filter.update(changed)
        assertEquals(changed.allySlotXRatios, accepted.allySlotXRatios)
        assertEquals(changed.allySlotDiameterRatios, accepted.allySlotDiameterRatios)
        assertEquals(base.enemySlotXRatios, accepted.enemySlotXRatios)
    }

    @Test fun smallNoiseIsNotIntegratedIntoAnEverMovingGrid() {
        val filter = DraftGeometryStability()
        filter.update(base)
        repeat(30) {
            val noisy = base.copy(allySlotYRatios = base.allySlotYRatios.map { it + .002f })
            assertEquals(base, filter.update(noisy))
        }
    }

    @Test fun viewportChangeAllowsANewMeasurementImmediately() {
        val filter = DraftGeometryStability()
        filter.update(base)
        filter.reset()
        assertEquals(changed.allySlotXRatios, filter.update(changed).allySlotXRatios)
    }
}
