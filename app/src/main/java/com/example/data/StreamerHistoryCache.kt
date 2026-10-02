package com.example.data

import android.content.Context
import com.google.firebase.Timestamp
import org.json.JSONArray
import org.json.JSONObject

/** Owner-scoped backup. Server snapshots replace matching records, never the whole archive. */
object StreamerHistoryCache {
    private const val PREFS = "streamer_history_backup"
    @Synchronized fun records(context: Context, uid: String, now: Long = System.currentTimeMillis()): List<Map<String, Any>> {
        if (uid.isBlank()) return emptyList()
        val array = runCatching { JSONArray(context.getSharedPreferences(PREFS, 0).getString(uid, "[]")) }.getOrElse { JSONArray() }
        val rows = (0 until array.length()).mapNotNull { index ->
            val obj = array.optJSONObject(index) ?: return@mapNotNull null
            obj.keys().asSequence().associateWith { obj.get(it) }.filterValues { it != JSONObject.NULL }
        }.filterNot { StreamerPublicationPolicy.historyExpired(it, now) }
        if (rows.size != array.length()) context.getSharedPreferences(PREFS, 0).edit().putString(uid, JSONArray(rows.map(::JSONObject)).toString()).apply()
        return rows
    }
    @Synchronized fun merge(context: Context, uid: String, incoming: List<Map<String, Any>>, now: Long = System.currentTimeMillis()): List<Map<String, Any>> {
        if (uid.isBlank()) return emptyList()
        val latest = LinkedHashMap<String, Map<String, Any>>()
        (records(context, uid, now) + incoming).filter { it.isNotEmpty() }.forEach { row ->
            val id = StreamerPublicationPolicy.publicationId(row)
            val normalized = row.mapValues { (_, value) -> if (value is Timestamp) value.toDate().time else value }.toMutableMap()
            // Preserve the authoritative submission time after flattening timestamps for local storage.
            normalized.remove("submittedAt")
            normalized["submittedAtMillis"] = StreamerPublicationPolicy.submittedAt(row)
            latest[id] = latest[id].orEmpty() + normalized
        }
        val rows = latest.values.filterNot { StreamerPublicationPolicy.historyExpired(it, now) }
            .sortedByDescending(StreamerPublicationPolicy::submittedAt)
        context.getSharedPreferences(PREFS, 0).edit().putString(uid, JSONArray(rows.map(::JSONObject)).toString()).apply()
        return rows
    }
}
