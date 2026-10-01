package com.example.data.sync

/** Legacy saved regions resolve to the sole supported Global dataset. */
object MetaRegion {
    const val DEFAULT = "GLOBAL"
    val available = listOf(DEFAULT)
    fun normalize(value: String): String = DEFAULT
    fun label(region: String) = "🌐 Global"
}
