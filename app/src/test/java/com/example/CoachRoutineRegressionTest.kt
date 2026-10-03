package com.example

import com.example.data.*
import com.example.service.screen.DraftSyncCadence
import org.junit.Assert.*
import org.junit.Test

class CoachRoutineRegressionTest {
    @Test fun `nine confirmed picks cannot starve the reconciliation while portrait is pending`() {
        val cycles = (1L..120L).filter { DraftSyncCadence.globalCycle(9, true, true, it) }
        assertEquals(10, cycles.size)
        assertTrue(cycles.zipWithNext().all { (a, b) -> b - a <= 12 })
        assertTrue(DraftSyncCadence.globalCycle(10, false, true, 1))
    }
    @Test fun `CPM respects thousands fractional rates and invalid inputs`() {
        assertEquals(3.75, CpmPolicy.revenue(1500, 2.50), 0.0001)
        assertEquals(9.375, CpmPolicy.revenue(1500, 2.50, 2.5), 0.0001)
        assertEquals(0.0, CpmPolicy.revenue(-1, 2.50), 0.0)
        assertEquals(0.0, CpmPolicy.revenue(1000, Double.NaN), 0.0)
        assertEquals(0.0, CpmPolicy.ctr(0, 10), 0.0)
        assertEquals(2.0, CpmPolicy.ctr(1000, 20), 0.0)
    }
    @Test fun `CPM writes actual nested counters and keeps dots inside notice identifiers`() {
        val result = CpmPolicy.nestedWrite(mapOf("metrics.sponsor.test.impressions" to 1L,
            "metrics.sponsor.test.noticeId" to "sponsor.test", "updatedAt" to 12L))
        assertEquals(mapOf("metrics" to mapOf("sponsor.test" to mapOf("impressions" to 1L,
            "noticeId" to "sponsor.test")), "updatedAt" to 12L), result)
    }
    @Test fun `legacy dotted analytics remain readable without doubling counters`() {
        val decoded = decodedCpmMetrics(mapOf("metrics" to mapOf("notice" to mapOf("impressions" to 20L)),
            "metrics.notice.impressions" to 12L, "metrics.notice.clicks" to 3L))
        assertEquals(20L, decoded["notice"]?.get("impressions"))
        assertEquals(3L, decoded["notice"]?.get("clicks"))
    }
    @Test fun `storage growth waits for evidence and handles deletion and an empty collection`() {
        assertEquals(200L, StorageConsumptionPolicy.estimate(10, listOf(10L, 30L)))
        assertEquals(0L, StorageConsumptionPolicy.estimate(0, emptyList()))
        assertNull(StorageConsumptionPolicy.estimate(10, emptyList()))
        assertNull(StorageConsumptionPolicy.dailyGrowth(100, 200, 10_000, 20_000))
        assertEquals(400L, StorageConsumptionPolicy.dailyGrowth(100, 200, 10_000, 21_610_000))
        assertEquals(0L, StorageConsumptionPolicy.dailyGrowth(200, 100, 10_000, 21_610_000))
    }
    @Test fun `streamer retry requires the same approved publication and never a different channel`() {
        val request = mapOf<String, Any>("status" to "APPROVED", "userId" to "owner", "publicationId" to "p1", "channelUrl" to "https://twitch.tv/coach")
        val live = listOf(request + ("clickCount" to 20L))
        assertTrue(StreamerReviewPolicy.isAlreadyApplied(request, live, true))
        assertFalse(StreamerReviewPolicy.isAlreadyApplied(request, emptyList(), true))
        assertFalse(StreamerReviewPolicy.isAlreadyApplied(request, listOf(request + ("publicationId" to "p2")), true))
        assertFalse(StreamerReviewPolicy.isAlreadyApplied(request, live, false))
        assertTrue(StreamerReviewPolicy.isAlreadyApplied(request + ("status" to "REJECTED"), emptyList(), false))
    }
    @Test fun `clearing metrics removes legacy keys without deleting other notices or tariffs`() {
        val source = mapOf<String, Any>("baseCpmRate" to 2.5,
            "metrics" to mapOf("a.b" to mapOf("impressions" to 9L), "other" to mapOf("clicks" to 2L)),
            "metrics.a.b.impressions" to 8L, "metrics.other.clicks" to 3L)
        val deleted = clearedCpmData(source, "a.b")
        assertFalse(deleted.containsKey("metrics.a.b.impressions"))
        assertEquals(3L, decodedCpmMetrics(deleted)["other"]?.get("clicks"))
        assertEquals(2.5, deleted["baseCpmRate"])
        val reset = clearedCpmData(source)
        assertTrue(decodedCpmMetrics(reset).isEmpty())
        assertEquals(2.5, reset["baseCpmRate"])
    }

}
