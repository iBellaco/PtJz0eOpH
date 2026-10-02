package com.example.data

import java.net.URI
import java.util.Locale

data class StreamChannel(val url: String, val platform: String)

/** Only channel URLs are accepted; host suffixes, redirects and video URLs are rejected. */
object StreamChannelUrl {
    fun parse(raw: String, allowAdminTest: Boolean = false): StreamChannel? = runCatching {
        val uri = URI(raw.trim())
        if (uri.scheme?.lowercase(Locale.ROOT) != "https" || uri.userInfo != null || uri.port != -1 || uri.fragment != null) return null
        val host = uri.host?.lowercase(Locale.ROOT) ?: return null
        if (host in setOf("google.com", "www.google.com")) {
            return if (allowAdminTest && uri.path.orEmpty() in setOf("", "/") && uri.rawQuery == null)
                StreamChannel("https://www.google.com", "Google") else null
        }
        val path = uri.path.orEmpty().trimEnd('/')
        val platform = when (host) {
            "tiktok.com", "www.tiktok.com" -> if (path.matches(Regex("/@[A-Za-z0-9_.]{2,24}(/live)?"))) "TikTok" else return null
            "youtube.com", "www.youtube.com", "m.youtube.com" -> if (
                path.matches(Regex("/@[\\p{L}\\p{N}_.-]{3,30}(/live)?")) ||
                path.matches(Regex("/channel/UC[A-Za-z0-9_-]{22}(/live)?")) ||
                path.matches(Regex("/(c|user)/[A-Za-z0-9_.-]{1,100}(/live)?"))
            ) "YouTube" else return null
            "twitch.tv", "www.twitch.tv", "m.twitch.tv" -> if (path.matches(Regex("/[A-Za-z0-9_]{3,25}")) &&
                path.lowercase(Locale.ROOT) !in setOf("/directory", "/videos", "/downloads", "/settings", "/login", "/signup", "/search", "/wallet", "/inventory", "/subscriptions")) "Twitch" else return null
            "kick.com", "www.kick.com" -> if (path.matches(Regex("/[A-Za-z0-9_-]{3,25}")) &&
                path.lowercase(Locale.ROOT) !in setOf("/categories", "/browse", "/search", "/login", "/signup", "/settings", "/dashboard")) "Kick" else return null
            else -> return null
        }
        StreamChannel(URI("https", null, host.removePrefix("m.").removePrefix("www."), -1, path, null, null).toASCIIString(), platform)
    }.getOrNull()
}

object StreamerPublicationPolicy {
    const val MAX_LIVE = 5
    const val REVIEW_WINDOW_MILLIS = 3 * 60 * 60 * 1000L
    const val HISTORY_WINDOW_MILLIS = 7 * 24 * 60 * 60 * 1000L
    fun submittedAt(data: Map<String, Any>): Long =
        (data["submittedAt"] as? com.google.firebase.Timestamp)?.toDate()?.time
            ?: (data["submittedAtMillis"] as? Number)?.toLong() ?: 0L
    fun expiresAt(data: Map<String, Any>): Long = submittedAt(data) + REVIEW_WINDOW_MILLIS
    fun historyExpiresAt(data: Map<String, Any>): Long = submittedAt(data).let { if (it > 0L) it + HISTORY_WINDOW_MILLIS else 0L }
    fun historyExpired(data: Map<String, Any>, now: Long = System.currentTimeMillis()): Boolean =
        historyExpiresAt(data).let { it > 0L && now >= it }
    fun isExpired(data: Map<String, Any>, now: Long = System.currentTimeMillis()): Boolean =
        data["status"] == "PENDING" && now >= expiresAt(data)
    fun historyStatus(data: Map<String, Any>, now: Long): String =
        if (isExpired(data, now)) "REJECTED" else (data["status"] as? String).orEmpty()
    fun publicationId(data: Map<String, Any>): String =
        (data["publicationId"] as? String)?.takeIf { it.isNotBlank() } ?: "legacy_${submittedAt(data)}"
    fun canRequest(entries: List<Map<String, Any>>, uid: String): Boolean =
        entries.size < MAX_LIVE && entries.none { it["userId"] == uid }
    fun approve(entries: List<Map<String, Any>>, entry: Map<String, Any>): List<Map<String, Any>> {
        check(canRequest(entries, entry["userId"] as String)) { "streamer_max" }
        return entries + entry
    }
}
