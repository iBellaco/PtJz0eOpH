package com.example.data.sync

import org.json.JSONArray
import org.json.JSONObject

/**
 * Parser for the official mainland-China Wild Rift feed used by
 * https://lolm.qq.com/act/a20220818raider/index.html.
 *
 * The page is a JavaScript shell. Its two JSON feeds are the stable contract:
 * the first maps Tencent hero ids to the visible roster and the second carries
 * rates for each rank bracket and lane. Values are kept as published; no rate
 * is estimated when a row omits one of the three metrics.
 */
object ChinaLolmParser {
    const val HERO_LIST_URL = "https://game.gtimg.cn/images/lgamem/act/lrlib/js/heroList/hero_list.js"
    const val RANK_LIST_URL = "https://mlol.qt.qq.com/go/lgame_battle_info/hero_rank_list_v2"

    data class EloSnapshot(
        val id: String,
        val label: String,
        val tiers: Map<String, String> = emptyMap(),
        val stats: Map<String, ChampionMetaStats> = emptyMap(),
        val roleStats: Map<String, Map<com.example.model.LaneRole, ChampionMetaStats>> = emptyMap(),
        val publishedDate: String = ""
    )

    data class Parsed(
        val tiers: Map<String, String> = emptyMap(),
        val stats: Map<String, ChampionMetaStats> = emptyMap(),
        val roleStats: Map<String, Map<com.example.model.LaneRole, ChampionMetaStats>> = emptyMap(),
        val publishedDate: String = "",
        val eloGroups: Map<String, EloSnapshot> = emptyMap()
    )

    /** Tencent's ids are not Data Dragon ids; this explicit map prevents a
     * name-translation guess from silently assigning a rate to another champion.
     */
    private val heroIds = mapOf(
        "10001" to "garen", "10002" to "aatrox", "10003" to "lux", "10004" to "amumu",
        "10005" to "kayn", "10006" to "rengar", "10007" to "karma", "10008" to "nunu",
        "10009" to "monkeyking", "10010" to "vayne", "10011" to "ekko", "10012" to "orianna",
        "10013" to "diana", "10014" to "zoe", "10015" to "tryndamere", "10016" to "xinzhao",
        "10017" to "khazix", "10018" to "jinx", "10019" to "hecarim", "10020" to "yuumi",
        "10021" to "braum", "10022" to "jhin", "10023" to "aurelionsol", "10024" to "syndra",
        "10025" to "twistedfate", "10026" to "kaisa", "10027" to "nilah", "10028" to "morgana",
        "10029" to "evelynn", "10030" to "pantheon", "10031" to "akali", "10032" to "fiora",
        "10033" to "blitzcrank", "10034" to "seraphine", "10035" to "mordekaiser", "10036" to "annie",
        "10037" to "yasuo", "10038" to "ahri", "10039" to "zed", "10040" to "jax",
        "10041" to "kayle", "10042" to "lucian", "10044" to "leona", "10045" to "tristana",
        "10046" to "teemo", "10047" to "sona", "10048" to "lulu", "10049" to "shyvana",
        "10050" to "vi", "10051" to "xayah", "10052" to "rakan", "10053" to "twitch",
        "10054" to "gwen", "10055" to "camille", "10056" to "jarvaniv", "10057" to "viego",
        "10058" to "volibear", "10059" to "leesin", "10060" to "corki", "10061" to "kennen",
        "10062" to "drmundo", "10063" to "zyra", "10064" to "rammus", "10065" to "nasus",
        "10066" to "missfortune", "10067" to "zeri", "10068" to "sett", "10069" to "yone",
        "10070" to "masteryi", "10071" to "darius", "10072" to "graves", "10073" to "kindred",
        "10074" to "maokai", "10075" to "janna", "10076" to "poppy", "10077" to "draven",
        "10078" to "gnar", "10079" to "ashe", "10080" to "zilean", "10081" to "alistar",
        "10082" to "ambessa", "10083" to "malphite", "10084" to "nidalee", "10085" to "aurora",
        "10086" to "fizz", "10087" to "ziggs", "10088" to "chogath", "10089" to "gragas",
        "10090" to "vladimir", "10091" to "nami", "10092" to "riven", "10093" to "varus",
        "10094" to "irelia", "10095" to "katarina", "10096" to "singed", "10097" to "soraka",
        "10098" to "ezreal", "10099" to "galio", "10100" to "akshan", "10101" to "jayce",
        "10102" to "urgot", "10103" to "kassadin", "10104" to "samira", "10105" to "bard",
        "10106" to "olaf", "10107" to "sivir", "10108" to "milio", "10109" to "lissandra",
        "10110" to "kogmaw", "10112" to "caitlyn", "10116" to "rell", "10117" to "skarner",
        "10118" to "shen", "10119" to "brand", "10120" to "sylas", "10121" to "renekton",
        "10122" to "veigar", "10123" to "senna", "10124" to "pyke", "10125" to "sion",
        "10126" to "swain", "10127" to "heimerdinger", "10128" to "talon", "10129" to "velkoz",
        "10130" to "thresh", "10131" to "warwick", "10132" to "nautilus", "10133" to "fiddlesticks",
        "10134" to "kalista", "10135" to "lillia", "10136" to "ornn", "10137" to "ryze",
        "10138" to "smolder", "10139" to "mel", "10140" to "rumble", "10141" to "vex",
        "10144" to "viktor", "10145" to "reksai", "10146" to "taliyah", "10147" to "hwei",
        "10148" to "yunara", "10151" to "nocturne", "10152" to "ksante", "10166" to "norra"
    ).mapValues { RegionalTierParser.canonical(it.value) }

    private val bracketOrder = listOf("1", "2", "3", "4", "0")

    fun parse(heroPayload: String, rankPayload: String, knownIds: Set<String>): Parsed {
        val known = knownIds.map(RegionalTierParser::canonical).toSet()
        val heroList = runCatching { JSONObject(heroPayload).optJSONObject("heroList") }.getOrNull()
            ?: return Parsed()
        val validTencentIds = heroList.keys().asSequence().toSet()
        val numericToChampion = heroIds.filterKeys { it in validTencentIds }
            .filterValues { it in known }
        if (numericToChampion.isEmpty()) return Parsed()
        val data = runCatching { JSONObject(rankPayload).optJSONObject("data") }.getOrNull()
            ?: return Parsed()

        fun number(row: JSONObject, percentKey: String, decimalKey: String): Double? {
            val raw = row.opt(percentKey)
            val direct = when (raw) {
                is Number -> raw.toDouble()
                is String -> raw.replace(',', '.').toDoubleOrNull()
                else -> null
            }
            val value = direct ?: row.optString(decimalKey).replace(',', '.').toDoubleOrNull()?.times(100.0)
            return value?.takeIf { it.isFinite() && it in 0.0..100.0 }
        }
        fun role(position: String): com.example.model.LaneRole? = when (position) {
            "1" -> com.example.model.LaneRole.MID
            "2" -> com.example.model.LaneRole.TOP
            "3" -> com.example.model.LaneRole.ADC
            "4" -> com.example.model.LaneRole.SUPPORT
            "5" -> com.example.model.LaneRole.JUNGLE
            else -> null
        }
        fun tier(row: JSONObject): String? = when (row.optString("strength_level").toIntOrNull()) {
            5 -> "S+"
            4 -> "S"
            3 -> "A"
            2 -> "B"
            1 -> "C"
            else -> null
        }

        fun tierRank(value: String): Int = when (value) {
            "S+" -> 5
            "S" -> 4
            "A" -> 3
            "B" -> 2
            "C" -> 1
            else -> 0
        }

        fun parseBracket(bracketKey: String): EloSnapshot? {
            val bracket = data.optJSONObject(bracketKey) ?: return null
            val stats = linkedMapOf<String, ChampionMetaStats>()
            val tiers = linkedMapOf<String, String>()
            val roles = linkedMapOf<String, MutableMap<com.example.model.LaneRole, ChampionMetaStats>>()
            var publishedDate = ""
            bracket.keys().forEach { position ->
                val lane = role(position)
                val rows = bracket.optJSONArray(position) ?: return@forEach
                for (index in 0 until rows.length()) {
                    val row = rows.optJSONObject(index) ?: continue
                    val id = numericToChampion[row.optString("hero_id")] ?: continue
                    val date = row.optString("dtstatdate")
                    if (date > publishedDate) publishedDate = date
                    val value = ChampionMetaStats(
                        number(row, "win_rate_percent", "win_rate"),
                        number(row, "appear_rate_percent", "appear_rate"),
                        number(row, "forbid_rate_percent", "forbid_rate"),
                        tier = tier(row)
                    )
                    val previous = stats[id]
                    if (previous == null || !previous.isComplete() ||
                        (value.isComplete() && (value.pickRate ?: -1.0) > (previous.pickRate ?: -1.0))) {
                        stats[id] = value
                    }
                    tier(row)?.let { candidate ->
                        val current = tiers[id]
                        if (current == null || tierRank(candidate) > tierRank(current)) tiers[id] = candidate
                    }
                    if (lane != null && value.isUsable()) {
                        val byLane = roles.getOrPut(id) { linkedMapOf() }
                        val old = byLane[lane]
                        if (old == null || !old.isComplete() ||
                            (value.isComplete() && (value.pickRate ?: -1.0) > (old.pickRate ?: -1.0))) {
                            byLane[lane] = value
                        }
                    }
                }
            }
            if (stats.isEmpty() && tiers.isEmpty()) return null
            return EloSnapshot(
                id = bracketKey,
                label = MetaRegion.chinaEloLabel(bracketKey),
                tiers = tiers,
                stats = stats,
                roleStats = roles.mapValues { it.value.toMap() },
                publishedDate = publishedDate
            )
        }

        val eloGroups = listOf("1", "2", "3", "4").mapNotNull { key ->
            parseBracket(key)?.let { key to it }
        }.toMap()

        // The page defaults to Diamond+ (bracket 1). Other brackets are used
        // only to fill champions/lanes absent from that primary publication;
        // their values never overwrite the default bracket. This preserves the
        // previous aggregate behavior while exposing every Elo group to the UI.
        val stats = linkedMapOf<String, ChampionMetaStats>()
        val tiers = linkedMapOf<String, String>()
        val roles = linkedMapOf<String, MutableMap<com.example.model.LaneRole, ChampionMetaStats>>()
        var publishedDate = ""
        bracketOrder.forEach { bracketKey ->
            val snapshot = parseBracket(bracketKey) ?: return@forEach
            if (snapshot.publishedDate > publishedDate) publishedDate = snapshot.publishedDate
            snapshot.stats.forEach { (id, value) ->
                val previous = stats[id]
                val useRow = previous == null || !previous.isComplete() ||
                    (bracketKey == "1" && value.isComplete() &&
                        (value.pickRate ?: -1.0) > (previous?.pickRate ?: -1.0))
                if (useRow) stats[id] = value
            }
            snapshot.tiers.forEach { (id, value) ->
                if (bracketKey == "1" || !tiers.containsKey(id)) tiers[id] = value
            }
            snapshot.roleStats.forEach { (id, byLane) ->
                val target = roles.getOrPut(id) { linkedMapOf() }
                byLane.forEach { (lane, value) ->
                    val old = target[lane]
                    val replace = old == null || !old.isComplete() ||
                        (bracketKey == "1" && value.isComplete() &&
                            (value.pickRate ?: -1.0) > (old.pickRate ?: -1.0))
                    if (replace && (bracketKey == "1" || old == null || !old.isComplete())) target[lane] = value
                }
            }
        }
        return Parsed(tiers, stats, roles.mapValues { it.value.toMap() }, publishedDate, eloGroups)
    }
}
