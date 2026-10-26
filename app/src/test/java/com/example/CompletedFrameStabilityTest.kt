package com.example

import com.example.service.screen.CompletedFrameStability
import org.junit.Assert.*
import org.junit.Test

class CompletedFrameStabilityTest {
    @Test fun slowColdComparisonDoesNotExpireItsOwnFirstObservation() {
        val frames=CompletedFrameStability(2,1200)
        assertEquals(1,frames.record("teemo",100,4000))
        assertTrue(frames.isRecent(4001))
        assertEquals(2,frames.record("teemo",4001,4050))
        assertEquals(2,frames.record("teemo",4051,9000))
    }
    @Test fun missingFramesAndChangedCandidatesStillBreakTheConfirmation() {
        val frames=CompletedFrameStability(2,1200)
        assertEquals(1,frames.record("teemo",100,150))
        assertFalse(frames.isRecent(1400))
        assertEquals(1,frames.record("teemo",1400,1450))
        assertEquals(1,frames.record("poppy",1451,1460))
        assertEquals(2,frames.record("poppy",1461,1470))
        frames.reset()
        assertEquals(0,frames.count)
        assertFalse(frames.isRecent(1471))
        assertEquals(1,frames.record("poppy",1471,1480))
    }
    @Test(expected=IllegalArgumentException::class)
    fun invalidProcessingTimestampsCannotCountAsAFrame() {
        CompletedFrameStability(2,1200).record("teemo",100,99)
    }
}
