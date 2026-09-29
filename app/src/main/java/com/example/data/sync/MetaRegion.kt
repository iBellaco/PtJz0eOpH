package com.example.data.sync

/** Global/NA are bundled references; only CN has a live statistics endpoint. */
object MetaRegion {
    val available = listOf("GLOBAL", "NA", "CN")
    fun normalize(value: String): String = when (value.trim().uppercase(java.util.Locale.ROOT)) {
        "GLOBAL" -> "GLOBAL"
        "NA" -> "NA"
        else -> "CN"
    }
}
