package com.example.data.sync

import java.util.Locale

/**
 * Competitive datasets shown by the tier list.
 *
 * Global is the default live dataset. China is a separate live dataset backed
 * by the official mainland Tencent feed and is never fused with Global rates.
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
        CHINA -> "China"
        else -> "Global"
    }

    fun hasLiveRateStats(region: String): Boolean = normalize(region) in setOf(DEFAULT, CHINA)
}
