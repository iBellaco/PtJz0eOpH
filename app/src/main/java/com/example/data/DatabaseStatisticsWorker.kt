package com.example.data

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/** Automatic sampling survives leaving the statistics dialog; ordinary accounts make no queries. */
class DatabaseStatisticsWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        if (!SupportTicketAccess.isAdmin()) return Result.success()
        val prefs = applicationContext.getSharedPreferences("saved-data-sampling", Context.MODE_PRIVATE)
        if (System.currentTimeMillis() - prefs.getLong("sampledAt", 0) < 6 * 3_600_000) return Result.success()
        return try { DatabaseStatisticsRepository.load(); prefs.edit().putLong("sampledAt", System.currentTimeMillis()).apply(); Result.success() } catch (_: Exception) { Result.retry() }
    }
    companion object {
        private var scheduled = false
        @Synchronized fun schedule(context: Context) {
            if (scheduled) return
            scheduled = true
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork("saved-data-consumption",
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<DatabaseStatisticsWorker>(6, TimeUnit.HOURS).setConstraints(constraints).build())
            WorkManager.getInstance(context).enqueueUniqueWork("saved-data-consumption-initial", ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<DatabaseStatisticsWorker>().setConstraints(constraints).build())
        }
    }
}
