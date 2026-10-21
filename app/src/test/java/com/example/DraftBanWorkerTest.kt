package com.example

import android.app.Application
import android.graphics.Bitmap
import com.example.model.LaneRole
import com.example.service.DraftCompletionNavigation
import com.example.service.screen.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.CopyOnWriteArrayList

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[34],application=Application::class)
class DraftBanWorkerTest {
    @Test fun busyHeaderKeepsOnlyNewestFrameAndDoesNotBlockTitles() = runBlocking<Unit> {
        val scope = CoroutineScope(SupervisorJob()+Dispatchers.Default)
        val entered = CountDownLatch(1); val release = CountDownLatch(1)
        val lastFinished = CountDownLatch(1)
        val seen = CopyOnWriteArrayList<Long>()
        val frames = (0..2).map { DraftBanFrame(Bitmap.createBitmap(8,8,Bitmap.Config.ARGB_8888),
            emptyList(),VisionCalibrationConfig(),emptySet(),it.toLong()) }
        val worker = DraftBanWorker(scope) { frame,_ ->
            seen.add(frame.generation)
            if(frame.generation==0L) { entered.countDown(); assertTrue(release.await(5,TimeUnit.SECONDS)) }
            else lastFinished.countDown()
        }
        try {
            worker.offer(0L) { frames[0] }
            assertTrue(entered.await(5,TimeUnit.SECONDS))
            worker.offer(1500L) { frames[1] }
            worker.offer(3000L) { frames[2] }
            assertTrue("Superseded image must be recycled",frames[1].bitmap.isRecycled)
            // Title evidence advances while the header is deliberately still busy.
            val titles = SlotTitleConfirmation()
            assertFalse(titles.observe(true,0,"senna"))
            assertTrue(titles.observe(true,0,"senna"))
            assertEquals(listOf(0L),seen.toList())
            release.countDown()
            assertTrue(lastFinished.await(5,TimeUnit.SECONDS))
            assertEquals(listOf(0L,2L),seen.toList())
        } finally {
            release.countDown(); worker.close(); scope.cancel()
            frames.forEach { if(!it.bitmap.isRecycled) it.bitmap.recycle() }
        }
    }

    @Test fun firstPickOrderSurvivesIncompleteLaterReads() {
        val scanner = DraftVisionScanner
        scanner.resetSlotMemory()
        try {
            assertEquals(false,scanner.recoverFirstPick(0,1))
            assertEquals(false,scanner.recoverFirstPick(1,0))
            assertEquals(false,scanner.recoverFirstPick(3,2))
            assertEquals(false,scanner.recoverFirstPick(0,0,true))
            scanner.resetSlotMemory()
            assertEquals(true,scanner.recoverFirstPick(1,0))
            assertEquals(true,scanner.recoverFirstPick(0,1))
        } finally { scanner.resetSlotMemory() }
    }

    @Test fun completedDraftNavigationTargetsOwnLaneChampionOnly() {
        val allies = listOf(
            com.example.model.Champion(id = "top"),
            com.example.model.Champion(id = "jungle"),
            com.example.model.Champion(id = "mid"),
            com.example.model.Champion(id = "adc"),
            com.example.model.Champion(id = "support")
        )
        val enemies = List(5) { index -> com.example.model.Champion(id = "enemy$index") }
        assertEquals("mid", DraftCompletionNavigation.ownChampionWhenComplete(allies, enemies, LaneRole.MID)?.id)
        assertNull(DraftCompletionNavigation.ownChampionWhenComplete(allies, enemies.dropLast(1), LaneRole.MID))
    }
}
