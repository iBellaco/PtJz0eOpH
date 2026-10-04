package com.example.data

import kotlinx.coroutines.CancellationException

data class SavedDataQueryResult(val count: Long, val estimatedBytes: Long?)

/** Counts and size sampling are independent: a missing sample cannot erase a known count. */
object SavedDataQueryPolicy {
    suspend fun read(
        aggregate: suspend () -> Long,
        fullRead: suspend () -> List<Long>,
        sample: suspend () -> List<Long>
    ): SavedDataQueryResult {
        val count = try { aggregate() } catch (error: Exception) {
            if (error is CancellationException) throw error
            val sizes = fullRead()
            return SavedDataQueryResult(sizes.size.toLong(), sizes.sum())
        }
        val sizes = try { sample() } catch (error: Exception) {
            if (error is CancellationException) throw error
            emptyList()
        }
        return SavedDataQueryResult(count, StorageConsumptionPolicy.estimate(count, sizes))
    }

    fun combine(children: List<SavedDataQueryResult>): SavedDataQueryResult = SavedDataQueryResult(
        children.sumOf { it.count },
        if (children.all { it.estimatedBytes != null }) children.sumOf { it.estimatedBytes ?: 0L } else null)
}
