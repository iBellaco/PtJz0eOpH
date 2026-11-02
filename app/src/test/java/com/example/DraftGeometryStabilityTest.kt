package com.example

import com.example.service.screen.DraftGeometryStability
import com.example.service.screen.VisionCalibrationConfig
import org.junit.Assert.*
import org.junit.Test

class DraftGeometryStabilityTest {
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
