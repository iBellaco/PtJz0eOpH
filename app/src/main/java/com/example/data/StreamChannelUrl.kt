package com.example.data

import java.net.URI
import java.util.Locale

data class StreamChannel(val url: String, val platform: String)

/** Only channel URLs are accepted; host suffixes, redirects and video URLs are rejected. */
object StreamChannelUrl {
    fun parse(raw: String): StreamChannel? = runCatching {
        val uri = URI(raw.trim())
        if (uri.scheme?.lowercase(Locale.ROOT) != "https" || uri.userInfo != null || uri.port != -1 || uri.fragment != null) return null
        val host = uri.host?.lowercase(Locale.ROOT) ?: return null
        val path = uri.rawPath.orEmpty().trimEnd('/')
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
        StreamChannel("https://${host.removePrefix("m.").removePrefix("www.")}$path", platform)
    }.getOrNull()
}

object StreamerPublicationPolicy {
    const val MAX_LIVE = 5
    fun canRequest(entries: List<Map<String, Any>>, uid: String): Boolean =
        entries.size < MAX_LIVE && entries.none { it["userId"] == uid }
    fun approve(entries: List<Map<String, Any>>, entry: Map<String, Any>): List<Map<String, Any>> {
        check(canRequest(entries, entry["userId"] as String)) { "streamer_max" }
        return entries + entry
    }
}
