package com.example.data.sync

import org.json.JSONObject
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Publisher schemas and dated reports. A day is never presented as an exact hourly measurement. */
object PublishedStatsParser {
    data class FireReport(val patch: String = "", val stats: Map<String, ChampionMetaStats> = emptyMap(),
        val history: Map<String, List<ObservedRate>> = emptyMap())

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

    fun rifter(html: String): Map<String, ChampionMetaStats> {
        val output = linkedMapOf<String, ChampionMetaStats>()
        Jsoup.parse(html).select("a[href*=/champion/]").forEach { link ->
            val values = link.select("dl div").associate { cell ->
                cell.selectFirst("dt")?.text().orEmpty().lowercase() to cell.selectFirst("dd")?.text()
                    ?.removeSuffix("%")?.toDoubleOrNull()?.takeIf { it in 0.0..100.0 }
            }
            val stats = ChampionMetaStats(values["win"], values["pick"], values["ban"])
            if (stats.isUsable()) output[RegionalTierParser.canonical(link.attr("href").substringAfterLast('/'))] = stats
        }
        return output
    }

    fun fire(html: String): FireReport = runCatching {
        val doc = Jsoup.parse(html)
        val current = JSONObject(doc.selectFirst("script#wf-stats-data")?.data().orEmpty())
        val rows = current.getJSONObject("brackets").getJSONObject("master").getJSONArray("rows")
        val stats = linkedMapOf<String, ChampionMetaStats>()
        val historyKeys = linkedMapOf<String, String>()
        for (i in 0 until rows.length()) {
            val row = rows.getJSONObject(i)
            if (!row.optBoolean("main")) continue
            val id = RegionalTierParser.canonical(row.getString("slug"))
            fun rate(key: String) = row.optDouble(key, Double.NaN).takeIf { it.isFinite() && it in 0.0..100.0 }
            val value = ChampionMetaStats(rate("win"), rate("pick"), rate("ban"))
            if (value.isUsable()) {
                stats[id] = value
                historyKeys[id] = row.getString("champion") + " (" + row.getString("role") + ")"
            }
        }
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
        FireReport(patch, stats, history)
    }.getOrDefault(FireReport())
}
