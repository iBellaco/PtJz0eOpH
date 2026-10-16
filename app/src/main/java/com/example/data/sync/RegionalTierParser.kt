package com.example.data.sync

import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.Locale

data class ChampionMetaStats(
    val winRate: Double? = null,
    val pickRate: Double? = null,
    val banRate: Double? = null
) {
    fun isUsable(): Boolean = listOf(winRate, pickRate, banRate).any { it != null && it.isFinite() && it in 0.0..100.0 }
}

/** Each publisher has its own schema. Never carry a tier into navigation/footer links. */
object RegionalTierParser {
    fun canonical(value: String): String = value.lowercase(Locale.ROOT)
        .filter { it.isLetterOrDigit() }.let {
            when (it) {
                "nunu", "nunuwillump", "nunuywillump", "nunuandwillump", "nunuampwillump" -> "nunuwillump"
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
        if (source == null || source == "wildriftguides") {
            // Each role contains separate rows. Scope to its list so navigation
            // and links for other categories cannot inherit the row's tier.
            doc.select("section[id$=-tier-list] ul").forEach { list ->
                val category = list.parent()?.selectFirst("span[title]")?.text()?.let(::tier)
                    ?: return@forEach
                list.select("a[href^=/champion/]").forEach { link ->
                    val slug = link.attr("href").substringBefore('?').trim('/').split('/').getOrNull(1)
                    if (!slug.isNullOrBlank()) add(slug, category)
                }
            }
        }
        if (source == "wildriftcounter" || source == "thewildpick") {
            // Ambos sitios publican fichas de enfrentamiento; cuando incluyen una
            // etiqueta de tier se aprovecha únicamente esa etiqueta verificable.
            doc.select("[data-tier], [class*=tier], [aria-label*=Tier]").forEach { node ->
                val category = tier(
                    node.attr("data-tier").ifBlank { node.attr("aria-label").removePrefix("Tier ").ifBlank { node.text() } }
                ) ?: return@forEach
                val link = node.selectFirst("a[href]") ?: if (node.tagName() == "a") node else null
                val href = link?.attr("href").orEmpty()
                val name = href.substringBefore('?').substringBefore('#').trimEnd('/').substringAfterLast('/')
                    .ifBlank { link?.text().orEmpty() }
                add(name, category)
            }
        }
        if (source == null || source == "wildlegends") {
            doc.select(".tier-list-grid").forEach { grid ->
                val category = grid.parent()?.selectFirst("span.relative.z-10")?.text()?.let(::tier)
                    ?: return@forEach
                grid.select("a[href*=/guia/]").forEach { link ->
                    add(link.attr("href").substringBefore('?').substringAfterLast('/'), category)
                }
            }
        }
        return result
    }

    /**
     * Lee las métricas publicadas junto a cada campeón cuando la fuente las ofrece.
     * Las páginas que solo publican categorías devuelven un mapa vacío y no se
     * fabrican porcentajes.
     */
    fun parseStats(html: String, source: String? = null): Map<String, ChampionMetaStats> {
        if (html.isBlank()) return emptyMap()
        if (source == "wildrifter") return PublishedStatsParser.rifter(html)
        if (source == "wildriftfire") return PublishedStatsParser.fire(html).stats
        if (source != null && source != "wildriftcore") return emptyMap()
        val doc = Jsoup.parse(html)
        val output = linkedMapOf<String, ChampionMetaStats>()
        doc.select("li.tl-cell").forEach { cell ->
            val link = cell.selectFirst("a.tl-cell__name[href]") ?: return@forEach
            val slug = link.attr("href").substringBefore('?').substringBefore('#')
                .trimEnd('/').substringAfterLast('/')
            val id = canonical(slug.ifBlank { link.text() })
            if (id.length !in 2..30) return@forEach

            fun number(selector: String): Double? = cell.selectFirst(selector)?.text()
                ?.replace(',', '.')
                ?.replace(Regex("[^0-9.+-]"), "")
                ?.toDoubleOrNull()
                ?.takeIf { it.isFinite() && it in 0.0..100.0 }

            val win = number(".tl-cell__wr b")
            val rates = cell.selectFirst(".tl-cell__rates")?.text().orEmpty()
            val pick = Regex("([0-9]+(?:[.,][0-9]+)?)%\\s*pick", RegexOption.IGNORE_CASE)
                .find(rates)?.groupValues?.get(1)?.replace(',', '.')?.toDoubleOrNull()
            val ban = Regex("([0-9]+(?:[.,][0-9]+)?)%\\s*ban", RegexOption.IGNORE_CASE)
                .find(rates)?.groupValues?.get(1)?.replace(',', '.')?.toDoubleOrNull()
            val stats = ChampionMetaStats(win, pick, ban)
            if (!stats.isUsable()) return@forEach

            // El sitio puede repetir un campeón por línea. Conservamos la fila con
            // mayor selección, que representa el uso global más observado.
            val previous = output[id]
            if (previous == null || (stats.pickRate ?: 0.0) > (previous.pickRate ?: 0.0)) {
                output[id] = stats
            }
        }
        // Complete tiles are also usable when the rankings request fails.
        doc.select("a.tl-champ-tile[href]").forEach { tile ->
            val id = canonical(tile.attr("href").trimEnd('/').substringAfterLast('/'))
            fun rate(selector: String): Double? = tile.selectFirst(selector)?.text()
                ?.let { Regex("[0-9]+(?:[.,][0-9]+)?").find(it)?.value }
                ?.replace(',', '.')?.toDoubleOrNull()?.takeIf { it in 0.0..100.0 }
            val stats = ChampionMetaStats(rate(".tl-champ-wr"), rate(".tl-champ-pr"), rate(".tl-champ-br"))
            if (id.length in 2..30 && stats.isUsable()) output[id] = stats
        }
        // The complete rankings table publishes rates for the entire roster;
        // tierlist's podium only contains a small subset of champions.
        doc.select("tr.rk-row").forEach { row ->
            val link = row.selectFirst("a.rk-champ[href]") ?: return@forEach
            val id = canonical(link.attr("href").trimEnd('/').substringAfterLast('/'))
            fun rate(selector: String): Double? = row.selectFirst(selector)?.text()
                ?.replace(',', '.')?.replace("%", "")?.trim()?.toDoubleOrNull()
                ?.takeIf { it.isFinite() && it in 0.0..100.0 }
            val stats = ChampionMetaStats(rate(".rk-val--win, .rk-stat--win"),
                rate(".rk-stat--pick"), rate(".rk-stat--ban"))
            if (id.length in 2..30 && stats.isUsable()) output[id] = stats
        }
        return output
    }
}
