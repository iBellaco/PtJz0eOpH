package com.example

import com.example.service.screen.DraftGeometryStability
import com.example.service.screen.VisionCalibrationConfig
import org.junit.Assert.*
import org.junit.Test

class DraftGeometryStabilityTest {
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
