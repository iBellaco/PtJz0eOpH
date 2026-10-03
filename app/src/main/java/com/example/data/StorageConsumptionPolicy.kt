package com.example.data

/** Estimates content bytes; provider indexes, replicas and billing are intentionally excluded. */
object StorageConsumptionPolicy {
    fun estimate(count: Long, sampleBytes: List<Long>): Long? = when {
        count == 0L -> 0L
        count < 0 || sampleBytes.isEmpty() -> null
        else -> (sampleBytes.sum().toDouble() / sampleBytes.size * count).toLong().coerceAtLeast(0)
    }
    fun dailyGrowth(previousBytes: Long, currentBytes: Long, previousAt: Long, now: Long): Long? {
        if (previousAt <= 0 || now - previousAt < 3_600_000L) return null
        return ((currentBytes - previousBytes).coerceAtLeast(0).toDouble() * 86_400_000 / (now - previousAt)).toLong()
    }
    fun formatBytes(bytes: Long): String = when {
        bytes >= 1_073_741_824 -> "%.2f GB".format(java.util.Locale.US, bytes / 1_073_741_824.0)
        bytes >= 1_048_576 -> "%.2f MB".format(java.util.Locale.US, bytes / 1_048_576.0)
        bytes >= 1024 -> "%.2f KB".format(java.util.Locale.US, bytes / 1024.0)
        else -> "$bytes B"
    }
}
