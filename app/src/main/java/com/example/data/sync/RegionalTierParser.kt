package com.example.data.sync

import org.json.JSONObject

/** Parses published editorial categories from bestbuildwr.com, wildriftfire.com, and wildriftcore.com */
object RegionalTierParser {
    fun canonical(value: String): String = value.lowercase(java.util.Locale.ROOT)
        .filter { it.isLetterOrDigit() }.let { when (it) {
            "nunu", "nunuwillump", "nunuywillump" -> "nunuwillump"
            "monkeyking", "wukong" -> "wukong"
            "drmundo", "dr-mundo", "mundo" -> "drmundo"
            "aurelionsol", "aurelion-sol", "asol" -> "aurelionsol"
            "leesin", "lee-sin" -> "leesin"
            "xinzhao", "xin-zhao" -> "xinzhao"
            "masteryi", "master-yi", "yi" -> "masteryi"
            "missfortune", "miss-fortune", "mf" -> "missfortune"
            "jarvaniv", "jarvan-iv", "jarvan" -> "jarvaniv"
            "twistedfate", "twisted-fate", "tf" -> "twistedfate"
            "tahmkench", "tahm-kench" -> "tahmkench"
            else -> it
        } }

    private val order = listOf("S+", "S", "A+", "A", "B", "C", "D")

    // Comprehensive regex covering WildRiftFire, BestBuildWR, WildRiftCore, and standard HTML patterns
    private val tierHeaderRegex = Regex("""(?:tier[-_\s]*(splus|s\+|s|a\+|a|b|c|d|god|op)|class=["'][^"']*\btier[-_\s]+(splus|s\+|s|a\+|a|b|c|d)\b[^"']*["']|<h[1-6][^>]*>\s*(?:Tier\s+)?(S\+|S|A\+|A|B|C|D)\s*</h[1-6]>)""", RegexOption.IGNORE_CASE)
    private val champLinkRegex = Regex("""(?:href=["'](?:/guide/|/champion/|/es/champions/|/champions/)([^"'/?#]+)["']|data-champion=["']([^"']+)["']|alt=["']([^"']+)["'][^>]*class=["'][^"']*(?:champ|avatar|portrait|icon)[^"']*["'])""", RegexOption.IGNORE_CASE)

    fun parse(html: String): Map<String, String> {
        val result = linkedMapOf<String, String>()
        if (html.isBlank()) return result

        // 1. Check for embedded JSON payload (e.g. Next.js __NEXT_DATA__ or state scripts)
        try {
            val jsonMatch = Regex("""<script\s+id=["']__NEXT_DATA__["']\s+type=["']application/json["']>([^<]+)</script>""", RegexOption.IGNORE_CASE).find(html)
            if (jsonMatch != null) {
                val jsonStr = jsonMatch.groupValues[1]
                val root = JSONObject(jsonStr)
                // Search recursively for champion tier data
                extractTiersFromJson(root, result)
                if (result.size >= 10) return result
            }
        } catch (_: Exception) {}

        // 2. Sequential HTML token scanning (handling section-based tier lists)
        var currentTier: String? = null
        
        // Scan tokens sequentially
        val combinedRegex = Regex("""<div\b[^>]*class=["'][^"']*\btier[-_\s]*(splus|s\+|s|a\+|a|b|c|d)\b[^"']*["'][^>]*>|<h[1-6][^>]*>\s*(?:Tier\s+)?(S\+|S|A\+|A|B|C|D)\s*</h[1-6]>|<a\b[^>]*href=["'](?:/guide/|/champion/|/es/champions/|/champions/)([^"'/?#]+)["'][^>]*>|<(?:div|img)\b[^>]*data-champion=["']([^"']+)["']""", RegexOption.IGNORE_CASE)

        combinedRegex.findAll(html).forEach { match ->
            val tierClass = match.groupValues[1]
            val tierHeading = match.groupValues[2]
            val champHref = match.groupValues[3]
            val champData = match.groupValues[4]

            val detectedTier = when {
                tierClass.isNotEmpty() -> if (tierClass.equals("splus", true)) "S+" else tierClass.uppercase(java.util.Locale.ROOT)
                tierHeading.isNotEmpty() -> tierHeading.uppercase(java.util.Locale.ROOT)
                else -> null
            }

            if (detectedTier != null) {
                currentTier = detectedTier
            } else if (currentTier != null) {
                val rawId = champHref.ifEmpty { champData }
                if (rawId.isNotEmpty()) {
                    val id = canonical(rawId)
                    if (id.length in 2..25 && !id.contains("guide") && !id.contains("tierlist") && !id.contains("build") && !id.contains("es")) {
                        val existing = result[id]
                        if (existing == null || order.indexOf(currentTier!!) < order.indexOf(existing)) {
                            result[id] = currentTier!!
                        }
                    }
                }
            }
        }

        return result
    }

    private fun extractTiersFromJson(obj: Any?, result: MutableMap<String, String>) {
        when (obj) {
            is JSONObject -> {
                val tier = obj.optString("tier").ifEmpty { obj.optString("rank") }.uppercase(java.util.Locale.ROOT)
                val champ = obj.optString("champion").ifEmpty { obj.optString("name").ifEmpty { obj.optString("id") } }
                if (tier.isNotEmpty() && tier in order && champ.isNotEmpty()) {
                    val id = canonical(champ)
                    result[id] = tier
                }
                obj.keys().forEach { key ->
                    extractTiersFromJson(obj.opt(key), result)
                }
            }
            is org.json.JSONArray -> {
                for (i in 0 until obj.length()) {
                    extractTiersFromJson(obj.opt(i), result)
                }
            }
        }
    }
}

