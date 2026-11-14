package com.example.data.sync

import java.util.Locale

/**
 * Competitive datasets shown by the tier list.
 *
 * Global is the default live dataset. China is the regional classification
 * bundled with the champion catalog; it is deliberately kept separate from
 * the live global rate feeds because its public endpoint does not publish the
 * same three current metrics for every champion.
 */
object MetaRegion {
    const val DEFAULT = "GLOBAL"
    const val CHINA = "CHINA"

    val available = listOf(DEFAULT, CHINA)

    fun normalize(value: String?): String = when (value.orEmpty().trim().uppercase(Locale.ROOT)) {
        CHINA, "CN", "ASIA", "ASIA_CN", "CHINESE", "CHINA_LOCAL" -> CHINA
        else -> DEFAULT
    }

    fun label(region: String): String = when (normalize(region)) {
        CHINA -> "China / Local"
        else -> "Global"
    }

    fun hasLiveRateStats(region: String): Boolean = normalize(region) == DEFAULT
}
