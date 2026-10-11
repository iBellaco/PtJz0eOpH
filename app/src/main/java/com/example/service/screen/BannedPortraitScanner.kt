package com.example.service.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect

/** Confirms header portraits across independent frames; it never changes tenth-pick state. */
internal class BannedPortraitScanner {
    private var previous = emptySet<String>()
    private var confirmed = emptySet<String>()
    private var lastScanAt = -1500L

    @Synchronized fun reset() {
        previous = emptySet(); confirmed = emptySet(); lastScanAt = -1500L
    }

    @Synchronized fun scan(bitmap: Bitmap, context: Context?, nowMs: Long,
                           exclusions: List<Rect> = emptyList()): Set<String> {
        if (bitmap.isRecycled || bitmap.width < bitmap.height) return confirmed
        if (nowMs - lastScanAt < 1500L) return confirmed
        lastScanAt = nowMs
        val seen = LiteRTVisionClassifier.findHeaderBans(bitmap, context, exclusions)
        confirmed = confirmed + (previous intersect seen)
        previous = seen
        return confirmed
    }
}

/** Visual access follows the administrator role or its trusted session claim, excluding moderators. */
internal object BanDetectionAccess {
    fun canDetect(role: String, adminClaim: Boolean): Boolean = role == "admin" || adminClaim
}
