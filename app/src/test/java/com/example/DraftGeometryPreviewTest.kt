package com.example

import com.example.service.screen.refreshDraftGeometryPreview
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DraftGeometryPreviewTest {
    @Test fun onlyTwoNewFramesAreProcessedAndReleasedThenCaptureStops() = runTest {
        var requests = 0
        val processed = mutableListOf<Int>()
        val released = mutableListOf<Int>()
        refreshDraftGeometryPreview({ ++requests }, { released += it }, { true }) { processed += it }
        advanceUntilIdle()
        assertEquals(listOf(1, 2), processed)
        assertEquals(processed, released)
        assertEquals(2, requests)
    }

    @Test fun producerWakeupCanReturnNullButDoesNotReuseAnOldFrame() = runTest {
        var requests = 0
        val processed = mutableListOf<Int>()
        refreshDraftGeometryPreview({ if (++requests <= 2) null else requests }, {}, { true }) { processed += it }
        assertEquals(listOf(3, 4), processed)
    }

    @Test fun unavailableCaptureTimesOutWithoutAContinuousLoop() = runTest {
        var requests = 0
        refreshDraftGeometryPreview<Int>({ requests++; null }, { fail("No acquired frame") }, { true }) {
            fail("No image to process")
        }
        assertTrue(requests in 1..14)
        val stoppedAt = requests
        advanceUntilIdle()
        assertEquals(stoppedAt, requests)
    }

    @Test fun cancellationReleasesTheAcquiredFrameAndDoesNotAcquireAnother() = runTest {
        val released = mutableListOf<Int>()
        var requests = 0
        val job = launch {
            refreshDraftGeometryPreview({ ++requests }, { released += it }, { true }) { awaitCancellation() }
        }
        runCurrent()
        job.cancel()
        advanceUntilIdle()
        assertEquals(1, requests)
        assertEquals(listOf(1), released)
    }

    @Test fun enablingAutoScanPreventsTheSecondPreviewFrame() = runTest {
        var allowed = true
        var requests = 0
        val released = mutableListOf<Int>()
        refreshDraftGeometryPreview({ ++requests }, { released += it }, { allowed }) { allowed = false }
        assertEquals(1, requests)
        assertEquals(listOf(1), released)
    }
}
