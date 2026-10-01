package com.example.data.sync

import android.content.Context

object MetaCrawlerSyncService {
    suspend fun syncPatchData(context: Context) {
        BestBuildWrScraper.syncGlobalTierList(context)
    }
}
