package com.example.data.sync

object MetaRegion {
    const val DEFAULT = "GLOBAL"
    val available = listOf("GLOBAL", "CN")
    fun normalize(value: String): String = when (value.trim().uppercase(java.util.Locale.ROOT)) {
        "CN" -> "CN"
        else -> DEFAULT
    }
    fun label(region: String) = when (normalize(region)) {
        "CN" -> "🇨🇳 China"
        else -> "🌐 Global"
    }
}

