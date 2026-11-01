package com.example.util

import java.net.URI
import java.util.Locale

/** Trust the parsed HTTPS authority, never a substring in a path or query. */
object WebMediaPolicy {
    private val videoDomains = setOf("youtube.com", "youtube-nocookie.com", "youtu.be",
        "vimeo.com", "streamable.com", "dailymotion.com", "twitch.tv")

    fun host(url: String): String? = runCatching {
        val uri = URI(url.trim())
        if (!uri.scheme.equals("https", true) || uri.rawUserInfo != null ||
            uri.port !in listOf(-1, 443)) null
        else uri.host?.lowercase(Locale.ROOT)
    }.getOrNull()

    fun matches(url: String, domain: String): Boolean {
        val host = host(url) ?: return false
        return host == domain || host.endsWith(".$domain")
    }

    fun allowsVideo(url: String): Boolean = videoDomains.any { matches(url, it) }
    fun allowsNavigation(initial: String, target: String): Boolean {
        if (!allowsVideo(initial)) return false
        // Redirects stay within the selected provider, including its subdomains.
        return videoDomains.any { matches(initial, it) && matches(target, it) } ||
            (matches(initial, "youtube-nocookie.com") && matches(target, "youtube.com"))
    }
}
