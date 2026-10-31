package com.example

import com.example.data.*
import com.example.model.SubscriptionRecord
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class HistoryConsistencyTest {
    @Test fun `managed wallet cannot inherit the viewing account even while loading`() {
        val viewer = HistoryBalances(282577, 400)
        val target = HistoryAccountPolicy.balances(mapOf("blueEssence" to 0L, "orangeEssence" to 100L))
        assertFalse(HistoryAccountPolicy.isOwnAccount("target", "viewer"))
        assertFalse(HistoryAccountPolicy.isOwnAccount(null, null))
        assertEquals(HistoryBalances(0, 100), HistoryAccountPolicy.visibleBalances(false, viewer, target))
        assertNull(HistoryAccountPolicy.visibleBalances(false, viewer, null))
        assertEquals(viewer, HistoryAccountPolicy.visibleBalances(true, viewer, target))
        assertTrue(HistoryAccountPolicy.isOwnAccount("viewer", "viewer"))
    }
    @Test fun `actual and legacy removal receipts are never lifetime subscriptions`() {
        assertTrue(SubscriptionRecord(planName="Suscripción Premium retirada", status="Completado", source="ADMIN_REVOCATION").isRevocation)
        assertTrue(SubscriptionRecord(planName="Suscripción Premium retirada", status="Completado").isRevocation)
        assertTrue(SubscriptionRecord(status="Cancelado").isRevocation)
        assertFalse(SubscriptionRecord(planName="Suscripción Premium regalada", source="ADMIN_GIFT").isRevocation)
    }
    @Test fun `sample failure preserves a confirmed count and marks size unknown`() = runBlocking {
        val result = SavedDataQueryPolicy.read({ 42 }, { error("Full download must not run") }, { error("Sample unavailable") })
        assertEquals(42L, result.count)
        assertNull(result.estimatedBytes)
    }
    @Test fun `aggregate failure falls back to real documents instead of zero`() = runBlocking {
        val result = SavedDataQueryPolicy.read({ error("Aggregate not supported") }, { listOf(10L, 30L, 20L) }, { error("Unneeded sample") })
        assertEquals(SavedDataQueryResult(3, 60), result)
        try {
            SavedDataQueryPolicy.read({ error("Permission denied") }, { error("Permission denied") }, { emptyList() })
            fail("An inaccessible category must not become zero")
        } catch (_: IllegalStateException) { }
    }
    @Test fun `cancellation cannot trigger a fallback download`() = runBlocking {
        try {
            SavedDataQueryPolicy.read({ throw CancellationException() }, { fail("Cancelled query must stop"); emptyList() }, { emptyList() })
            fail("Expected cancellation")
        } catch (_: CancellationException) { }
    }
    @Test fun `parent totals preserve unknown sizes and count real empty categories`() {
        assertEquals(SavedDataQueryResult(0, 0), SavedDataQueryPolicy.combine(emptyList()))
        assertEquals(SavedDataQueryResult(5, null), SavedDataQueryPolicy.combine(listOf(SavedDataQueryResult(2, 30), SavedDataQueryResult(3, null))))
        assertEquals(SavedDataQueryResult(5, 80), SavedDataQueryPolicy.combine(listOf(SavedDataQueryResult(2, 30), SavedDataQueryResult(3, 50))))
    }
}
