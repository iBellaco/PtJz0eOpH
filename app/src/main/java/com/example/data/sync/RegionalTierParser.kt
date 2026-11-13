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
    fun isComplete(): Boolean = listOf(winRate, pickRate, banRate).all { it != null && it.isFinite() && it in 0.0..100.0 }
}

/** Each publisher has its own schema. Never carry a tier into navigation/footer links. */
object RegionalTierParser {
    private data class CoreEmbeddedStats(
        val global: Map<String, ChampionMetaStats> = emptyMap(),
        val roles: Map<String, Map<com.example.model.LaneRole, ChampionMetaStats>> = emptyMap()
    )

    fun role(value: String?): com.example.model.LaneRole? = when (value?.trim()?.uppercase(Locale.ROOT)) {
        "TOP", "BARON", "BARON LANE" -> com.example.model.LaneRole.TOP
        "JUNGLE", "JG" -> com.example.model.LaneRole.JUNGLE
        "MID", "MIDDLE" -> com.example.model.LaneRole.MID
        "ADC", "DRAGON", "DUO", "BOT" -> com.example.model.LaneRole.ADC
        "SUPPORT", "SUPP" -> com.example.model.LaneRole.SUPPORT
        else -> null
    }
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

    private fun mergeMissingStats(current: ChampionMetaStats, fallback: ChampionMetaStats): ChampionMetaStats =
        ChampionMetaStats(
            winRate = current.winRate ?: fallback.winRate,
            pickRate = current.pickRate ?: fallback.pickRate,
            banRate = current.banRate ?: fallback.banRate
        )

    /**
     * WildRiftCore keeps the complete ranked dataset in `tlRankData`, even
     * when a tile omits the rate badges.  The array format is
     * [championSlug, tierIndex, winRate, pickRate, banRate].  Reading this
     * published JSON prevents the HTML presentation from hiding real rates.
     */
    private fun parseCoreEmbeddedStats(doc: org.jsoup.nodes.Document): CoreEmbeddedStats {
        val root = runCatching {
            doc.selectFirst("script#tlRankData")?.data()?.takeIf { it.isNotBlank() }?.let(::JSONObject)
        }.getOrNull() ?: return CoreEmbeddedStats()
        val tiers = root.optJSONObject("b") ?: return CoreEmbeddedStats()
        val global = linkedMapOf<String, ChampionMetaStats>()
        val roles = linkedMapOf<String, MutableMap<com.example.model.LaneRole, ChampionMetaStats>>()

        fun number(row: JSONArray, index: Int): Double? {
            val raw = row.opt(index)
            val value = when (raw) {
                is Number -> raw.toDouble()
                is String -> raw.replace(',', '.').toDoubleOrNull()
                else -> null
            } ?: return null
            return value.takeIf { it.isFinite() && it in 0.0..100.0 }
        }

        fun putGlobal(id: String, value: ChampionMetaStats) {
            val previous = global[id]
            if (previous == null || (value.pickRate ?: -1.0) > (previous.pickRate ?: -1.0)) {
                global[id] = value
            }
        }

        tiers.keys().forEach { tierKey ->
            val byRole = tiers.optJSONObject(tierKey) ?: return@forEach
            byRole.keys().forEach { roleKey ->
                val lane = role(roleKey) ?: return@forEach
                val rows = byRole.optJSONArray(roleKey) ?: return@forEach
                for (index in 0 until rows.length()) {
                    val row = rows.optJSONArray(index) ?: continue
                    val id = canonical(row.optString(0))
                    if (id.length !in 2..30) continue
                    val value = ChampionMetaStats(number(row, 2), number(row, 3), number(row, 4))
                    if (!value.isUsable()) continue
                    val laneValues = roles.getOrPut(id) { linkedMapOf() }
                    val previous = laneValues[lane]
                    if (previous == null || (value.pickRate ?: -1.0) > (previous.pickRate ?: -1.0)) {
                        laneValues[lane] = value
                    }
                    putGlobal(id, value)
                }
            }
        }
        return CoreEmbeddedStats(global, roles)
    }

    /** Some Core tiles publish a current win/pick pair in their explanation
     * attribute while intentionally hiding the metric badges.  Accept only
     * the explicit current-patch sentence; approximate historical text is
     * deliberately ignored.
     */
    private fun parseCoreReasonStats(tile: Element): ChampionMetaStats? {
        val text = tile.attr("data-reason").ifBlank { tile.attr("title") }
        val match = Regex(
            "Win\\s+rate\\s+([0-9]+(?:[.,][0-9]+)?)%\\s+at\\s+([0-9]+(?:[.,][0-9]+)?)%\\s+pick\\s+rate",
            RegexOption.IGNORE_CASE
        ).find(text) ?: return null
        val win = match.groupValues[1].replace(',', '.').toDoubleOrNull()
        val pick = match.groupValues[2].replace(',', '.').toDoubleOrNull()
        return ChampionMetaStats(win, pick, null).takeIf { it.isUsable() }
    }

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
        if (source == null || source == "wildrifter") {
            doc.select("a[href*=/champion/]").forEach { link ->
                val category = link.selectFirst("[aria-label^=Tier]")?.attr("aria-label")
                    ?.removePrefix("Tier ")?.let(::tier) ?: return@forEach
                add(link.attr("href").substringBefore('?').substringBefore('#')
                    .trimEnd('/').substringAfterLast('/'), category)
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
        // The ALL panel contains the publisher's global values. Role panels
        // repeat champions and must never overwrite that global observation.
        // A standalone fragment still falls back to every available tile.
        val globalTileScope = doc.selectFirst("#all-tier-list") ?: doc
        globalTileScope.select("a.tl-champ-tile[href]").forEach { tile ->
            val id = canonical(tile.attr("href").substringBefore('?').substringBefore('#')
                .trimEnd('/').substringAfterLast('/'))
            fun rate(selector: String): Double? = tile.selectFirst(selector)?.text()
                ?.let { Regex("[0-9]+(?:[.,][0-9]+)?").find(it)?.value }
                ?.replace(',', '.')?.toDoubleOrNull()?.takeIf { it in 0.0..100.0 }
            val stats = ChampionMetaStats(rate(".tl-champ-wr"), rate(".tl-champ-pr"), rate(".tl-champ-br"))
            if (id.length !in 2..30) return@forEach
            val published = if (stats.isUsable()) stats else parseCoreReasonStats(tile)
            if (published == null) return@forEach
            // A champion can appear in several lane panels. The last panel is
            // not a global statistic, so retain the row with the highest
            // published pick rate as the most representative fallback.
            val previous = output[id]
            if (previous == null || (published.pickRate ?: -1.0) > (previous.pickRate ?: -1.0)) {
                output[id] = published
            } else {
                output[id] = mergeMissingStats(previous, published)
            }
        }
        // On current WildRiftCore releases some rate badges are omitted from
        // the tile HTML, but the publisher still exposes the same values in
        // its embedded ranked dataset. Fill only missing fields; explicit ALL
        // panel values remain authoritative.
        parseCoreEmbeddedStats(doc).global.forEach { (id, published) ->
            output[id] = output[id]?.let { mergeMissingStats(it, published) } ?: published
        }
        // The complete rankings table publishes rates for the entire roster;
        // tierlist's podium only contains a small subset of champions.
        // WildRiftCore renamed rk-* to rkc-* in November 2026. Keep both
        // schemas so a rollout or cached response cannot silently empty stats.
        doc.select("tr.rk-row, tr.rkc-row").forEach { row ->
            val link = row.selectFirst("a.rk-champ[href], a.rkc-champ[href]") ?: return@forEach
            val id = canonical(link.attr("href").substringBefore('?').substringBefore('#')
                .trimEnd('/').substringAfterLast('/'))
            fun rate(selector: String): Double? = row.selectFirst(selector)?.text()
                ?.replace(',', '.')?.replace("%", "")?.trim()?.toDoubleOrNull()
                ?.takeIf { it.isFinite() && it in 0.0..100.0 }
            val stats = ChampionMetaStats(
                rate(".rk-val--win, .rk-stat--win, .rkc-c-win .rkc-v"),
                rate(".rk-stat--pick, .rkc-c-pick .rkc-v"),
                rate(".rk-stat--ban, .rkc-c-ban .rkc-v")
            )
            if (id.length !in 2..30 || !stats.isUsable()) return@forEach
            val previous = output[id]
            if (previous == null || (stats.pickRate ?: 0.0) > (previous.pickRate ?: 0.0)) {
                output[id] = stats
            }
        }
        return output
    }

    /** Rates indexed by the publisher's lane. Global rows are deliberately excluded. */
    fun parseRoleStats(html: String, source: String? = null): Map<String, Map<com.example.model.LaneRole, ChampionMetaStats>> {
        if (html.isBlank()) return emptyMap()
        if (source == "wildriftfire") return PublishedStatsParser.fire(html).roleStats
        if (source != null && source != "wildriftcore") return emptyMap()
        val output = linkedMapOf<String, MutableMap<com.example.model.LaneRole, ChampionMetaStats>>()
        val doc = Jsoup.parse(html)
        fun putRole(id: String, lane: com.example.model.LaneRole, stats: ChampionMetaStats) {
            if (id.length !in 2..30 || !stats.isUsable()) return
            val byLane = output.getOrPut(id) { linkedMapOf() }
            val previous = byLane[lane]
            if (previous == null || (stats.pickRate ?: -1.0) > (previous.pickRate ?: -1.0)) {
                byLane[lane] = stats
            } else {
                byLane[lane] = mergeMissingStats(previous, stats)
            }
        }
        doc.select("li.tl-cell, tr.rk-row, tr.rkc-row").forEach { row ->
            val dataKeyRole = row.attr("data-k").substringAfterLast(':', "").takeIf { it.isNotBlank() }
            val lane = role(row.selectFirst("[data-role]")?.attr("data-role")
                ?: row.parents().firstOrNull { it.hasAttr("data-role") }?.attr("data-role")
                ?: dataKeyRole) ?: return@forEach
            val link = row.selectFirst("a.tl-cell__name[href], a.rk-champ[href], a.rkc-champ[href]") ?: return@forEach
            val id = canonical(link.attr("href").substringBefore('?').trimEnd('/').substringAfterLast('/'))
            fun rate(selector: String): Double? = row.selectFirst(selector)?.text()
                ?.replace(',', '.')?.replace(Regex("[^0-9.+-]"), "")?.toDoubleOrNull()
                ?.takeIf { it.isFinite() && it in 0.0..100.0 }
            val ratesText = row.selectFirst(".tl-cell__rates")?.text().orEmpty()
            val pick = rate(".rk-stat--pick, .rkc-c-pick .rkc-v") ?: Regex("([0-9]+(?:[.,][0-9]+)?)%\\s*pick", RegexOption.IGNORE_CASE)
                .find(ratesText)?.groupValues?.get(1)?.replace(',', '.')?.toDoubleOrNull()
            val ban = rate(".rk-stat--ban, .rkc-c-ban .rkc-v") ?: Regex("([0-9]+(?:[.,][0-9]+)?)%\\s*ban", RegexOption.IGNORE_CASE)
                .find(ratesText)?.groupValues?.get(1)?.replace(',', '.')?.toDoubleOrNull()
            val stats = ChampionMetaStats(
                rate(".tl-cell__wr b, .rk-val--win, .rk-stat--win, .rkc-c-win .rkc-v"), pick, ban
            )
            putRole(id, lane, stats)
        }
        parseCoreEmbeddedStats(doc).roles.forEach { (id, lanes) ->
            lanes.forEach { (lane, stats) -> putRole(id, lane, stats) }
        }
        return output
    }
}
