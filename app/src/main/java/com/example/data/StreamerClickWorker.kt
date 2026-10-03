package com.example.data

import android.content.Context
import androidx.work.*
import java.util.UUID
import java.util.concurrent.TimeUnit

/** Each successful channel opening has a durable, unique operation, independent of the screen. */
class StreamerClickWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        val publication = inputData.getString("publication") ?: return Result.failure()
        val event = inputData.getString("event") ?: return Result.failure()
        return if (StreamerRepository.recordClick(publication, event).isSuccess) Result.success()
        else if (System.currentTimeMillis() - inputData.getLong("openedAt", 0L) < TimeUnit.HOURS.toMillis(24)) Result.retry()
        else Result.failure()
    }

    companion object {
        fun enqueue(context: Context, entry: Map<String, Any>) {
            val publication = StreamerPublicationPolicy.publicationId(entry)
            val event = UUID.randomUUID().toString()
            val work = OneTimeWorkRequestBuilder<StreamerClickWorker>()
                .setInputData(workDataOf("publication" to publication, "event" to event, "openedAt" to System.currentTimeMillis()))
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .addTag("streamer-click").build()
            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork("streamer-click-$event", ExistingWorkPolicy.KEEP, work)
        }
    }
}
