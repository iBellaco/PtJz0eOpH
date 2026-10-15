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

data class ObservedRate(val at: Long, val value: Double, val pick: Double? = null,
    val ban: Double? = null, val daily: Boolean = false, val source: String = "")

/** Never interpolate a historical percentage that was not actually observed. */
object MetaRateHistory {
    fun record(previous: List<ObservedRate>, sample: ObservedRate): List<ObservedRate> =
        (previous.distinctBy { Pair(it.daily, it.at / (10 * 60_000L)) } + sample)
            .distinctBy { Pair(it.at, it.daily) }.filter { it.at >= sample.at - (if (it.daily) 72 else 26) * 60 * 60_000L && it.value.isFinite() }
            .sortedBy { it.at }.takeLast(170)

    fun sampleAtAge(samples: List<ObservedRate>, now: Long, hours: Int): ObservedRate? {
        val target = now - hours * 60 * 60_000L
        val hourly = samples.filter { !it.daily && it.at <= target && target - it.at <= 90 * 60_000L }
            .maxByOrNull { it.at }
        return hourly ?: samples.filter { it.daily && it.at <= target && target - it.at < 48 * 60 * 60_000L }
            .maxByOrNull { it.at }
    }

    fun atAge(samples: List<ObservedRate>, now: Long, hours: Int): Double? = sampleAtAge(samples, now, hours)?.value
}
