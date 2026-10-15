package com.example.util

/** Accepts production release tags, including the actual build suffix. */
data class PublishedAppVersion(val name: String, val code: Int) {
    fun newerThan(name: String, code: Int): Boolean {
        if (this.code != code) return this.code > code
        val latest = this.name.split('.').map { it.toInt() }
        val installed = name.split('.').mapNotNull { it.toIntOrNull() }
        for (i in 0 until maxOf(latest.size, installed.size)) {
            val comparison = latest.getOrElse(i) { 0 }.compareTo(installed.getOrElse(i) { 0 })
            if (comparison != 0) return comparison > 0
        }
        return false
    }
    companion object {
        fun parse(tag: String): PublishedAppVersion? {
            val match = Regex("^v?([0-9]+(?:\\.[0-9]+){2,3})-b([0-9]+)$").matchEntire(tag.trim()) ?: return null
            val code = match.groupValues[2].toIntOrNull()?.takeIf { it > 0 } ?: return null
            if (match.groupValues[1].split('.').any { it.toIntOrNull() == null }) return null
            return PublishedAppVersion(match.groupValues[1], code)
        }
    }
}
