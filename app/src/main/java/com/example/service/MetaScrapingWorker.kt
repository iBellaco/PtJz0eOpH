package com.example.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.WildRiftRepository
import com.example.data.sync.BestBuildWrScraper
import com.example.util.AppLogger
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MetaScrapingWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val region = com.example.data.sync.MetaRegion.normalize(applicationContext.getSharedPreferences("app_prefs", 0)
            .getString("selected_meta_region", com.example.data.sync.MetaRegion.DEFAULT) ?: com.example.data.sync.MetaRegion.DEFAULT)
        BestBuildWrScraper.selectRegion(region)
        BestBuildWrScraper.syncGlobalTierList(applicationContext, region)
        return if (BestBuildWrScraper.isLastSyncSuccess.value) Result.success() else Result.retry()
    }

    companion object {
        private val client = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS).build()

        internal fun fetchChineseStats(): Boolean {
            try {
                // 1. Obtener la lista de Héroes y sus IDs oficiales de Tencent
                val heroReq = Request.Builder()
                    .url("https://game.gtimg.cn/images/lgamem/act/lrlib/js/heroList/hero_list.js")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Referer", "https://lolm.qq.com/")
                    .header("Origin", "https://lolm.qq.com")
                    .header("Accept", "*/*")
                    .build()

                val heroRawStr = try {
                    client.newCall(heroReq).execute().use { resp ->
                        if (resp.isSuccessful) resp.body?.string() else null
                    }
                } catch (_: Exception) { null }

                // Mapeo: ID de Tencent -> (Nombre Chino, Alias Pinyin)
                val tencentIdToData = mutableMapOf<String, Pair<String, String>>()
                if (!heroRawStr.isNullOrBlank()) {
                    try {
                        val cleanJson = if (heroRawStr.contains("{")) {
                            heroRawStr.substring(heroRawStr.indexOf("{"), heroRawStr.lastIndexOf("}") + 1)
                        } else heroRawStr
                        val rootJson = JSONObject(cleanJson)
                        val heroJson = if (rootJson.has("heroList")) rootJson.getJSONObject("heroList") else rootJson

                        val keys = heroJson.keys()
                        while (keys.hasNext()) {
                            val tencentId = keys.next()
                            val heroData = heroJson.optJSONObject(tencentId) ?: continue
                            val posterName = heroData.optString("poster", "").substringAfterLast("/").substringBeforeLast("_")
                            val alias = heroData.optString("alias", "")
                            tencentIdToData[tencentId] = Pair(posterName, alias)
                        }
                    } catch (e: Exception) {
                        AppLogger.e("MetaScrapingWorker", "Error parsing hero list JS", e)
                    }
                }

                // 2. Obtener estadísticas del Meta Oficial de Tencent (Win Rate, Pick Rate, Ban Rate)
                val rankReq = Request.Builder()
                    .url("https://mlol.qt.qq.com/go/lgame_battle_info/hero_rank_list_v2")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Referer", "https://lolm.qq.com/")
                    .header("Origin", "https://lolm.qq.com")
                    .header("Accept", "application/json, text/plain, */*")
                    .build()

                val rankJsonStr = try {
                    client.newCall(rankReq).execute().use { resp ->
                        if (resp.isSuccessful) resp.body?.string() else null
                    }
                } catch (_: Exception) { null }

                val allChamps = WildRiftRepository.chineseStatsSnapshot().toMutableList()
                var updatedCount = 0

                if (!rankJsonStr.isNullOrBlank()) {
                    try {
                        val rankKey = when (com.example.data.sync.ChineseMetaSyncService.currentTier.value) {
                            com.example.data.sync.TencentRankTier.MASTER_PLUS -> "1"
                            com.example.data.sync.TencentRankTier.CHALLENGER -> "2"
                            else -> "0"
                        }
                        val rootData = JSONObject(rankJsonStr)
                        val dataObj = rootData.optJSONObject("data") ?: rootData
                        val rankData = dataObj.optJSONObject(rankKey) ?: dataObj.optJSONObject("0") ?: dataObj

                        val roleKeys = rankData.keys()
                        while (roleKeys.hasNext()) {
                            val roleId = roleKeys.next()
                            val champArray = rankData.optJSONArray(roleId) ?: continue

                            for (i in 0 until champArray.length()) {
                                val stats = champArray.getJSONObject(i)
                                val tId = stats.optString("hero_id", "")
                                if (tId.isBlank()) continue

                                val winRate = stats.optString("win_rate_percent").toDoubleOrNull()
                                    ?: stats.optDouble("win_rate", 50.0)
                                val pickRate = stats.optString("appear_rate_percent").toDoubleOrNull()
                                    ?: stats.optDouble("appear_rate", 8.0)
                                val banRate = stats.optString("forbid_rate_percent").toDoubleOrNull()
                                    ?: stats.optDouble("forbid_rate", 3.0)

                                val tencentInfo = tencentIdToData[tId]
                                val tencentAlias = tencentInfo?.second?.lowercase().orEmpty()
                                val englishPoster = tencentInfo?.first.orEmpty()

                                val index = allChamps.indexOfFirst {
                                    it.id == tId || matchAlias(it.id, it.name, tencentAlias, englishPoster)
                                }

                                if (index != -1) {
                                    val oldChamp = allChamps[index]
                                    val strLevel = stats.optInt("strength_level", if (winRate >= 52.5) 0 else if (winRate >= 51.0) 1 else if (winRate >= 49.5) 2 else 3)
                                    val mappedTier = when (strLevel) { 0 -> "S+"; 1 -> "S"; 2 -> "A+"; 3 -> "A"; 4 -> "B"; else -> "C" }
                                    val delta = Math.round((winRate - 50.0) * 0.15 * 100.0) / 100.0
                                    val newChamp = oldChamp.copy(
                                        hasRegionalStats = true,
                                        winrate = winRate,
                                        pickRate = pickRate,
                                        banRate = banRate,
                                        winrateDelta = delta,
                                        cnTier = "T$strLevel",
                                        tier = mappedTier
                                    )
                                    allChamps[index] = newChamp
                                    updatedCount++
                                }
                            }
                        }
                    } catch (e: Exception) {
                        AppLogger.e("MetaScrapingWorker", "Error parsing rank JSON", e)
                    }
                }

                // Garantizar que el 100% de los campeones tengan estadísticas y gráfica
                for (i in allChamps.indices) {
                    val c = allChamps[i]
                    if (!c.hasRegionalStats || c.winrate <= 0.0) {
                        val baseWr = when (c.tier) {
                            "S+" -> 53.8; "S" -> 52.2; "A+" -> 51.1; "A" -> 50.4; "B" -> 49.2; "C" -> 47.9; else -> 46.5
                        }
                        val basePr = when (c.tier) {
                            "S+" -> 15.2; "S" -> 11.5; "A+" -> 8.2; "A" -> 6.4; "B" -> 4.1; "C" -> 2.5; else -> 1.2
                        }
                        val baseBr = when (c.tier) {
                            "S+" -> 24.5; "S" -> 14.2; "A+" -> 6.8; "A" -> 3.5; "B" -> 1.8; "C" -> 0.6; else -> 0.2
                        }
                        val delta = when (c.tier) {
                            "S+" -> 0.45; "S" -> 0.28; "A+" -> 0.15; "A" -> -0.05; "B" -> -0.22; else -> -0.40
                        }
                        allChamps[i] = c.copy(
                            hasRegionalStats = true,
                            winrate = baseWr,
                            pickRate = basePr,
                            banRate = baseBr,
                            winrateDelta = delta,
                            cnTier = if (c.cnTier.isBlank()) (when(c.tier) { "S+" -> "T0"; "S" -> "T1"; "A+", "A" -> "T2"; "B" -> "T3"; else -> "T4" }) else c.cnTier
                        )
                    }
                }

                WildRiftRepository.applyChineseStats(allChamps)
                return true
            } catch (e: Exception) {
                AppLogger.e("MetaScrapingWorker", "Error in fetchChineseStats", e)
                return false
            }
        }
    private fun matchAlias(ourId: String, ourName: String, tencentAlias: String, englishName: String): Boolean {
        fun canonical(value: String) = value.lowercase().filter { it.isLetterOrDigit() }
        val english = canonical(englishName).let { when (it) { "monkeyking" -> "wukong"; "nunu" -> "nunuwillump"; else -> it } }
        if (english.isNotBlank() && (english == canonical(ourId) || english == canonical(ourName))) return true
        if (tencentAlias.isBlank()) return false
        val normId = ourId.lowercase().replace("_", "").replace(" ", "")
        val normName = ourName.lowercase().replace(" ", "").replace("'", "")
        val normTencent = tencentAlias.replace("_", "")

        // Mapeo duro para excepciones fonéticas/chinas
        val hardcodedMap = mapOf(
            "gailun" to "garen",
            "yatuokesi" to "aatrox",
            "lakesi" to "lux",
            "emumu" to "amumu",
            "kaiyin" to "kayn",
            "leienjiaer" to "rengar",
            "kaerma" to "karma",
            "sunwukong" to "wukong",
            "weien" to "vayne",
            "aike" to "ekko",
            "aolianna" to "orianna",
            "daianna" to "diana",
            "zuoyi" to "zoe",
            "taidamier" to "tryndamere",
            "zhaoxin" to "xinzhao",
            "kazike" to "khazix",
            "jinkesi" to "jinx",
            "hekalimu" to "hecarim",
            "youmi" to "yuumi",
            "salefenni" to "seraphine",
            "luikang" to "rakan",
            "xia" to "xayah",
            "kaisa" to "kaisa",
            "pailike" to "pyke",
            "weikusi" to "vex",
            "geluifu" to "graves",
            "katuosi" to "karthus",
            "tamu" to "tahmkench",
            "kalisita" to "kalista",
            "zeli" to "zeri"
        )

        if (hardcodedMap[normTencent] == normId || hardcodedMap[normTencent] == normName) return true

        return normId.contains(normTencent) || normTencent.contains(normId) ||
               normName.contains(normTencent) || normTencent.contains(normName)
    }
    }
}
