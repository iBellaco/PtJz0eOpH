package com.example.service.screen

import android.graphics.Bitmap
import android.graphics.Rect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

internal data class DraftBanFrame(val bitmap: Bitmap, val masks: List<Rect>,
    val config: VisionCalibrationConfig, val pickedIds: Set<String>, val generation: Long,
    val tenthPickSide: BanSide? = null)

/** One background header job and at most one newest pending frame; never blocks title OCR. */
internal class DraftBanWorker(scope: CoroutineScope, private val process: (DraftBanFrame, Job) -> Unit) {
    // Keep title OCR responsive while avoiding four full-frame copies and
    // competing header passes per second alongside the game. The conflated
    // channel retains only the newest pending observation.
    private companion object {
        const val MIN_OFFER_INTERVAL_MS = 750L
    }
    private val pending = Channel<DraftBanFrame>(Channel.CONFLATED, onUndeliveredElement = { it.bitmap.recycle() })
    private val lastOffered = AtomicLong(-MIN_OFFER_INTERVAL_MS)
    private val job = scope.launch(Dispatchers.Default) {
        for(frame in pending) {
            try { process(frame, coroutineContext[Job]!!) }
            catch (error: Exception) {
                if(error is kotlinx.coroutines.CancellationException) throw error
                com.example.util.AppLogger.w("DraftBanWorker","Header scan failed: ${error.javaClass.simpleName}")
            }
            finally { frame.bitmap.recycle() }
        }
    }

    fun offer(nowMs: Long, capture: () -> DraftBanFrame) {
        val previous = lastOffered.get()
        if(nowMs-previous<MIN_OFFER_INTERVAL_MS || !job.isActive || !lastOffered.compareAndSet(previous,nowMs)) return
        val frame = capture()
        if(pending.trySend(frame).isFailure) frame.bitmap.recycle()
    }

    fun close() { pending.cancel(); job.cancel() }
}
