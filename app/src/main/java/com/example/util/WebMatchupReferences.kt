package com.example.util

import android.content.Context
import com.example.model.LaneRole
import org.json.JSONObject

/** Published champion IDs from five editorial sites; never interpreted as live win rates. */
internal object WebMatchupReferences {
    var byChampion: Map<String, MatchupRoleResult> = emptyMap()
        private set
    var byRole: Map<String, Map<LaneRole, MatchupRoleResult>> = emptyMap()
        private set
    private var loaded = false

    @Synchronized fun load(context: Context) {
        if (loaded) return
        val payload = context.assets.open("matchup_references.json").bufferedReader().use { JSONObject(it.readText()) }
        val champions = payload.getJSONObject("champions")
        val ids = champions.keys().asSequence().toList()
        val relationships = ids.associateWith { read(champions.getJSONObject(it)) }
        val roles = ids.associateWith { id ->
            val rows = champions.getJSONObject(id).optJSONObject("roles") ?: JSONObject()
            rows.keys().asSequence().mapNotNull { key ->
                val role = runCatching { LaneRole.valueOf(key) }.getOrNull() ?: return@mapNotNull null
                role to read(rows.getJSONObject(key))
            }.toMap()
        }
        byChampion = relationships
        byRole = roles
        loaded = true
    }

    private fun read(value: JSONObject): MatchupRoleResult {
        fun names(key: String): List<String> {
            val list = value.optJSONArray(key) ?: return emptyList()
            return (0 until list.length()).map { list.getString(it) }.distinct()
        }
        return MatchupRoleResult(names("advantages"), names("counters"), names("synergies"))
    }
}
