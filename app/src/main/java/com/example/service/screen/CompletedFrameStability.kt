package com.example.service.screen

/** Consecutive matching frames expire while waiting for the next observation,
 * not while the current portrait comparison is still computing its result. */
internal class CompletedFrameStability(private val requiredFrames: Int, private val maxGapMs: Long) {
    var count: Int = 0
        private set
    private var candidate: String? = null
    private var completedAt: Long? = null

    fun isRecent(startedAt: Long): Boolean = completedAt?.let { startedAt-it in 0L..maxGapMs } == true

    fun record(id: String, startedAt: Long, finishedAt: Long): Int {
        require(finishedAt>=startedAt)
        count=if(candidate==id && isRecent(startedAt)) (count+1).coerceAtMost(requiredFrames) else 1
        candidate=id
        completedAt=finishedAt
        return count
    }

    fun reset() { count=0;candidate=null;completedAt=null }
}
