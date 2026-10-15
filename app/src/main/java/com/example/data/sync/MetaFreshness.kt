package com.example.data.sync

/** A successful unchanged response is different from a failed or missing poll. */
data class MetaFreshness(
    val lastAttemptAt: Long = 0,
    val tiersCheckedAt: Long = 0,
    val ratesCheckedAt: Long = 0,
    val lastChangedAt: Long = 0,
    val unchangedChecks: Int = 0,
    val tierCount: Int = 0,
    val statsCount: Int = 0,
    val expectedCount: Int = 0,
    val changedChampions: Int = 0
) {
    fun stale(now: Long): Boolean = tiersCheckedAt == 0L || ratesCheckedAt == 0L ||
        now - minOf(tiersCheckedAt, ratesCheckedAt) > 3 * POLL_INTERVAL_MS
    fun longUnchanged(now: Long): Boolean = lastChangedAt > 0 && unchangedChecks >= 3 &&
        now - lastChangedAt >= 24 * 60 * 60_000L
    companion object { const val POLL_INTERVAL_MS = 60_000L }
}

data class ObservedRate(val at: Long, val value: Double)

/** Never interpolate a historical percentage that was not actually observed. */
object MetaRateHistory {
    fun record(previous: List<ObservedRate>, sample: ObservedRate): List<ObservedRate> =
        (previous.distinctBy { it.at / (10 * 60_000L) } + sample).distinctBy { it.at }
            .filter { it.at >= sample.at - 26 * 60 * 60_000L }
            .sortedBy { it.at }.takeLast(160)

    fun atAge(samples: List<ObservedRate>, now: Long, hours: Int): Double? {
        val target = now - hours * 60 * 60_000L
        return samples.filter { it.at <= target && target - it.at <= 15 * 60_000L }
            .maxByOrNull { it.at }?.value
    }
}
