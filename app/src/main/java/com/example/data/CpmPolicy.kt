package com.example.data

object CpmPolicy {
    fun ctr(impressions: Long, clicks: Long): Double = if (impressions > 0)
        clicks.coerceIn(0, impressions).toDouble() / impressions * 100 else 0.0
    fun revenue(impressions: Long, rate: Double, multiplier: Double = 1.0): Double {
        if (!rate.isFinite() || !multiplier.isFinite() || rate < 0 || multiplier < 0) return 0.0
        return impressions.coerceAtLeast(0).toDouble() / 1000 * rate * multiplier
    }
    /** set(map, merge) takes a nested map; dots in a notice id must remain part of its key. */
    fun nestedWrite(fields: Map<String, Any>): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        val metrics = mutableMapOf<String, Any>()
        fields.forEach { (key, value) ->
            if (key.startsWith("metrics.") && key.lastIndexOf('.') > 8) {
                val split = key.lastIndexOf('.')
                val field = key.substring(split + 1)
                if (field in setOf("impressions", "clicks", "totalRawClicks", "fullscreenViews", "lastViewedTimestamp", "noticeId", "tag", "customCpmRate")) {
                    val id = key.substring(8, split)
                    @Suppress("UNCHECKED_CAST")
                    val entry = metrics.getOrPut(id) { mutableMapOf<String, Any>() } as MutableMap<String, Any>
                    entry[field] = value
                } else metrics[key.removePrefix("metrics.")] = value
            } else result[key] = value
        }
        if (metrics.isNotEmpty()) result["metrics"] = metrics
        return result
    }
}

/** Recover prior dotted-key writes without counting the same observations twice. */
fun decodedCpmMetrics(data: Map<String, Any>): Map<String, Map<String, Any>> {
    val result = mutableMapOf<String, MutableMap<String, Any>>()
    (data["metrics"] as? Map<*, *>)?.forEach { (id, fields) ->
        if (id is String && fields is Map<*, *>) result[id] = fields.entries.mapNotNull {
            (it.key as? String)?.let { key -> it.value?.let { value -> key to value } }
        }.toMap().toMutableMap()
    }
    data.forEach { (key, value) ->
        if (key.startsWith("metrics.") && key.lastIndexOf('.') > 8) {
            val split = key.lastIndexOf('.')
            val field = key.substring(split + 1)
            if (field in setOf("impressions", "clicks", "totalRawClicks", "fullscreenViews", "lastViewedTimestamp")) {
                val row = result.getOrPut(key.substring(8, split)) { mutableMapOf() }
                row[field] = maxOf((row[field] as? Number)?.toLong() ?: 0, (value as? Number)?.toLong() ?: 0)
            }
        }
    }
    return result
}

/** A reset/delete must also remove legacy dotted keys or their counters would reappear. */
fun clearedCpmData(data: Map<String, Any>, noticeId: String? = null): Map<String, Any> {
    val result = data.filterKeys { key ->
        if (!key.startsWith("metrics.")) true
        else noticeId != null && key.substring(8, key.lastIndexOf('.').coerceAtLeast(8)) != noticeId
    }.toMutableMap()
    result["metrics"] = if (noticeId == null) emptyMap<String, Any>()
        else decodedCpmMetrics(data).filterKeys { it != noticeId }
    return result
}
