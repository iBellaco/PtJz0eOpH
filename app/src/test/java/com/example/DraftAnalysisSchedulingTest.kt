package com.example

import com.example.model.DraftAnalysisResult
import com.example.ui.components.DraftAnalysisRequest
import com.example.ui.components.calculateDraftAnalysis
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class DraftAnalysisSchedulingTest {
    private val request = DraftAnalysisRequest(null, emptyList(), emptyList(), null, false, "es")
    private val result = DraftAnalysisResult(0, 0, 0, frontlineStatus = "",
        directMatchupWarning = null, directCounterBestPick = null, recommendations = emptyList())

    @Test fun expensiveAnalysisRunsOutsideTheCallingThread() = runBlocking {
        val caller = Thread.currentThread()
        val actual = calculateDraftAnalysis(request) {
            assertNotSame(caller, Thread.currentThread())
            result
        }
        assertSame(result, actual)
    }

    @Test fun cancelledAnalysisCannotPublishItsOldDraft() = runBlocking {
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        var published = false
        val task = launch(start = CoroutineStart.UNDISPATCHED) {
            calculateDraftAnalysis(request) {
                entered.countDown()
                check(release.await(5, TimeUnit.SECONDS))
                result
            }
            published = true
        }
        try {
            assertTrue(entered.await(5, TimeUnit.SECONDS))
            task.cancel()
            release.countDown()
            task.join()
            assertFalse(published)
            assertTrue(task.isCancelled)
        } finally { release.countDown(); task.cancelAndJoin() }
    }
}
