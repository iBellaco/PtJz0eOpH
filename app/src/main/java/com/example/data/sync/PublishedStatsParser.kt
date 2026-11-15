package com.example.data.sync

import org.json.JSONObject
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Publisher schemas and dated reports. A day is never presented as an exact hourly measurement. */
object PublishedStatsParser {
    data class FireReport(val patch: String = "", val stats: Map<String, ChampionMetaStats> = emptyMap(),
        val history: Map<String, List<ObservedRate>> = emptyMap(),
        val roleStats: Map<String, Map<com.example.model.LaneRole, ChampionMetaStats>> = emptyMap())

    fun patch(html: String, source: String): String? {
        val doc = Jsoup.parse(html)
        val text = when (source) {
            "bestbuildwr" -> runCatching { JSONObject(doc.selectFirst("script#__NEXT_DATA__")?.data().orEmpty())
                .getJSONObject("props").getJSONObject("pageProps").getString("patchVersion") }.getOrNull().orEmpty()
            "wildriftcore" -> doc.selectFirst(".nav-patch-badge")?.text().orEmpty()
            "wildlegends" -> doc.select(".badge").firstOrNull { it.text().contains("PATCH", true) }?.text().orEmpty()
            else -> doc.title()
        }
        return Regex("(?<![0-9])([0-9]+\\.[0-9]+[a-z]?)(?![0-9a-z])", RegexOption.IGNORE_CASE)
            .find(text)?.groupValues?.get(1)?.lowercase()
    }

    fun fire(html: String): FireReport = runCatching {
        val doc = Jsoup.parse(html)
        val current = JSONObject(doc.selectFirst("script#wf-stats-data")?.data().orEmpty())
        val brackets = current.optJSONObject("brackets") ?: return@runCatching FireReport()
        val preferred = listOf("master", "apex", "challenger", "diamond")
            .filter { brackets.optJSONObject(it) != null }
        if (preferred.isEmpty()) return@runCatching FireReport()
        val stats = linkedMapOf<String, ChampionMetaStats>()
        val fallbackStats = linkedMapOf<String, ChampionMetaStats>()
        val roleStats = linkedMapOf<String, MutableMap<com.example.model.LaneRole, ChampionMetaStats>>()
        val historyKeys = linkedMapOf<String, String>()
        fun readBracket(name: String, fillOnly: Boolean) {
            val rows = brackets.optJSONObject(name)?.optJSONArray("rows") ?: return
            for (i in 0 until rows.length()) {
                val row = rows.optJSONObject(i) ?: continue
                val slug = row.optString("slug")
                val champion = row.optString("champion")
                val roleName = row.optString("role")
                if (slug.isBlank() || roleName.isBlank()) continue
                val id = RegionalTierParser.canonical(slug)
                fun rate(key: String) = row.optDouble(key, Double.NaN).takeIf { it.isFinite() && it in 0.0..100.0 }
                val value = ChampionMetaStats(rate("win"), rate("pick"), rate("ban"))
                RegionalTierParser.role(roleName)?.let { role ->
                    if (value.isUsable()) {
                        val byRole = roleStats.getOrPut(id) { linkedMapOf() }
                        val old = byRole[role]
                        if (old == null || !old.isComplete() || !fillOnly ||
                            (value.pickRate ?: -1.0) > (old.pickRate ?: -1.0)) {
                            // Master is authoritative. Apex/challenger rows
                            // only fill a champion/role absent there.
                            if (!fillOnly || old == null || !old.isComplete()) byRole[role] = value
                        }
                    }
                }
                // Some rotations publish a complete row without marking it as
                // the main lane. Keep the best complete fallback so a missing
                // flag does not turn real published rates into a dash.
                if (!row.optBoolean("main")) {
                    if (value.isComplete()) {
                        val oldFallback = fallbackStats[id]
                        if (oldFallback == null || (value.pickRate ?: -1.0) > (oldFallback.pickRate ?: -1.0)) {
                            fallbackStats[id] = value
                        }
                    }
                    continue
                }
                val old = stats[id]
                if (old == null || !old.isComplete() ||
                    (!fillOnly && (value.pickRate ?: -1.0) > (old.pickRate ?: -1.0))) {
                    stats[id] = value
                    if (champion.isNotBlank()) historyKeys[id] = "$champion ($roleName)"
                }
            }
        }
        preferred.forEachIndexed { index, name -> readBracket(name, fillOnly = index > 0) }
        fallbackStats.forEach { (id, value) -> if (id !in stats) stats[id] = value }
        val history = linkedMapOf<String, List<ObservedRate>>()
        val archive = doc.selectFirst("script#wf-stats-history")?.data()?.let(::JSONObject)
        val patch = current.getString("patch")
        val patches = archive?.optJSONArray("patches")
        val patchDate = (0 until (patches?.length() ?: 0)).mapNotNull { patches?.optJSONObject(it) }
            .firstOrNull { it.optString("version") == patch }?.optString("date").orEmpty()
        val series = archive?.optJSONObject("brackets")?.optJSONObject("master")
        if (patchDate.isNotBlank()) historyKeys.forEach { (id, key) ->
            val samples = series?.optJSONArray(key)
            history[id] = (0 until (samples?.length() ?: 0)).mapNotNull { i ->
                val row = samples?.optJSONObject(i) ?: return@mapNotNull null
                val date = row.optString("date")
                if (date < patchDate) return@mapNotNull null
                val win = row.optDouble("win", Double.NaN)
                if (!win.isFinite() || win !in 0.0..100.0) return@mapNotNull null
                val at = runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC"); isLenient = false
                }.parse(date)?.time }.getOrNull()
                    ?: return@mapNotNull null
                ObservedRate(at, win, row.optDouble("pick", Double.NaN).takeIf(Double::isFinite),
                    row.optDouble("ban", Double.NaN).takeIf(Double::isFinite), daily = true, source = "wildriftfire")
            }.takeLast(3)
        }
        FireReport(patch, stats, history, roleStats)
    }.getOrDefault(FireReport())
}
