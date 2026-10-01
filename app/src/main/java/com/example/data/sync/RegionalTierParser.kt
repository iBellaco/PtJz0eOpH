package com.example.data.sync

import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.Locale

/** Each publisher has its own schema. Never carry a tier into navigation/footer links. */
object RegionalTierParser {
    fun canonical(value: String): String = value.lowercase(Locale.ROOT)
        .filter { it.isLetterOrDigit() }.let {
            when (it) {
                "nunu", "nunuwillump", "nunuywillump", "nunuandwillump" -> "nunuwillump"
                "monkeyking", "wukong" -> "wukong"
                "mundo", "drmundo" -> "drmundo"
                "asol", "aurelionsol" -> "aurelionsol"
                "yi", "masteryi", "maestroyi" -> "masteryi"
                "mf", "missfortune" -> "missfortune"
                "tf", "twistedfate" -> "twistedfate"
                "jarvan", "jarvaniv" -> "jarvaniv"
                "bardo", "bard" -> "bard"
                else -> it
            }
        }

    private val order = listOf("S+", "S", "A+", "A", "B", "C", "D")
    private fun tier(value: String): String? = value.trim().uppercase(Locale.ROOT)
        .replace("SPLUS", "S+").takeIf { it in order }

    fun parse(html: String, source: String? = null): Map<String, String> {
        if (html.isBlank()) return emptyMap()
        val doc = Jsoup.parse(html)
        val result = linkedMapOf<String, String>()
        fun add(name: String, category: String) {
            val id = canonical(name)
            if (id.length !in 2..30) return
            val previous = result[id]
            if (previous == null || order.indexOf(category) < order.indexOf(previous)) result[id] = category
        }
        fun links(container: Element, category: String, selector: String) {
            container.select(selector).forEach { link ->
                val slug = link.attr("href").substringBefore('?').substringBefore('#').trimEnd('/').substringAfterLast('/')
                if (slug.isNotBlank()) add(slug, category)
            }
        }
        if (source == null || source == "bestbuildwr") {
            // The tier is the dictionary key, not a field inside each champion object.
            doc.select("script#__NEXT_DATA__").forEach { script ->
                runCatching {
                    val groups = JSONObject(script.data()).optJSONObject("props")?.optJSONObject("pageProps")
                        ?.optJSONObject("tierData")?.optJSONArray("champions") ?: JSONArray()
                    for (i in 0 until groups.length()) {
                        val tiers = groups.optJSONObject(i)?.optJSONObject("tiers") ?: continue
                        tiers.keys().forEach { key ->
                            val category = tier(key) ?: return@forEach
                            val champs = tiers.optJSONArray(key) ?: return@forEach
                            for (j in 0 until champs.length()) {
                                val champ = champs.optJSONObject(j) ?: continue
                                add(champ.optString("slug").ifBlank { champ.optString("name") }, category)
                            }
                        }
                    }
                }
            }
        }
        if (source == null || source == "wildriftfire") {
            val scope = doc.selectFirst(".wf-tier-list__tiers__main") ?: doc
            scope.select("div.tier").forEach { row ->
                val category = row.classNames().firstNotNullOfOrNull { tier(it) } ?: return@forEach
                links(row, category, "a.ico-holder[href^=/guide/]")
            }
        }
        if (source == null || source == "wildriftcore") {
            // Prefer the publisher's combined list rather than merging duplicated role panels.
            val scope = doc.selectFirst("#all-tier-list") ?: doc
            scope.select(".tl-tier-row[data-tier]").forEach { row ->
                val category = tier(row.attr("data-tier")) ?: return@forEach
                links(row, category, "a.tl-champ-tile[href]")
            }
        }
        return result
    }
}
