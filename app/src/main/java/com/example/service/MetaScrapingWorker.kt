package com.example.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.sync.BestBuildWrScraper

class MetaScrapingWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val region = com.example.data.sync.MetaRegion.normalize(applicationContext.getSharedPreferences("app_prefs", 0)
            .getString("selected_meta_region", com.example.data.sync.MetaRegion.DEFAULT) ?: com.example.data.sync.MetaRegion.DEFAULT)
        BestBuildWrScraper.selectRegion(region)
        BestBuildWrScraper.syncGlobalTierList(applicationContext, region)
        return if (BestBuildWrScraper.isLastSyncSuccess.value) Result.success() else Result.retry()
    }

}
