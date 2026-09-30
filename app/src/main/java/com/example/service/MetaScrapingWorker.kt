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
        if (region == "NA") return Result.success()
        return if (BestBuildWrScraper.isLastSyncSuccess.value) Result.success() else Result.retry()
    }

    companion object {
        private val client = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS).build()

        internal fun fetchChineseStats(): Boolean {
                // 1. Obtener la lista de Héroes y sus IDs oficiales de Tencent
                val heroReq = Request.Builder()
                    .url("https://game.gtimg.cn/images/lgamem/act/lrlib/js/heroList/hero_list.js")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Referer", "https://lolm.qq.com/")
                    .header("Accept", "*/*")
                    .build()

                val heroJsonStr = client.newCall(heroReq).execute().use { resp ->
                    if (resp.isSuccessful) resp.body?.string() else null
                }

                if (!heroJsonStr.isNullOrBlank()) {
                    val heroJson = JSONObject(heroJsonStr).getJSONObject("heroList")

                    // Mapeo: ID de Tencent -> (Nombre Chino, Alias Pinyin)
                    val tencentIdToData = mutableMapOf<String, Pair<String, String>>()
                    val keys = heroJson.keys()
                    while (keys.hasNext()) {
                        val tencentId = keys.next()
                        val heroData = heroJson.getJSONObject(tencentId)
                        tencentIdToData[tencentId] = Pair(
                            heroData.optString("poster").substringAfterLast("/").substringBeforeLast("_"),
                            heroData.optString("alias", "")
                        )
                    }

                    // 2. Obtener estadísticas del Meta (Win Rate, Pick Rate, Ban Rate)
                    val rankReq = Request.Builder()
                        .url("https://mlol.qt.qq.com/go/lgame_battle_info/hero_rank_list_v2")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .header("Referer", "https://lolm.qq.com/")
                        .header("Accept", "application/json, text/plain, */*")
                        .build()

                    val rankJsonStr = client.newCall(rankReq).execute().use { resp ->
                        if (resp.isSuccessful) resp.body?.string() else null
                    }

                    if (!rankJsonStr.isNullOrBlank()) {
                        val rankKey = when (com.example.data.sync.ChineseMetaSyncService.currentTier.value) {
                            com.example.data.sync.TencentRankTier.MASTER_PLUS -> "1"
                            com.example.data.sync.TencentRankTier.CHALLENGER -> "2"
                            else -> "0"
                        }
                        val rankData = JSONObject(rankJsonStr).optJSONObject("data")?.optJSONObject(rankKey)
                        if (rankData != null) {
                            var updatedCount = 0
                            val allChamps = WildRiftRepository.chineseStatsSnapshot().toMutableList()

                            val roleKeys = rankData.keys()
                            while (roleKeys.hasNext()) {
                                val roleId = roleKeys.next()
                                val champArray = rankData.optJSONArray(roleId) ?: continue

                                for (i in 0 until champArray.length()) {
                                    val stats = champArray.getJSONObject(i)
                                    val tId = stats.optString("hero_id", "")
                                    if (tId.isBlank()) continue

                                    val winRate = stats.optString("win_rate_percent").toDoubleOrNull() ?: continue
                                    val pickRate = stats.optString("appear_rate_percent").toDoubleOrNull() ?: continue
                                    val banRate = stats.optString("forbid_rate_percent").toDoubleOrNull() ?: continue

                                    val tencentInfo = tencentIdToData[tId] ?: continue
                                    val tencentAlias = tencentInfo.second.lowercase()

                                    val index = allChamps.indexOfFirst {
                                        matchAlias(it.id, it.name, tencentAlias, tencentInfo.first)
                                    }

                                    if (index != -1) {
                                        val oldChamp = allChamps[index]
                                        val newChamp = oldChamp.copy(
                                            hasRegionalStats = true,
                                            winrate = winRate,
                                            pickRate = pickRate,
                                            banRate = banRate,
                                            cnTier = "T" + stats.optInt("strength_level", 4),
                                            tier = when (stats.optInt("strength_level", 4)) { 0 -> "S+"; 1 -> "S"; 2 -> "A"; 3 -> "B"; else -> "C" }
                                        )
                                        allChamps[index] = newChamp
                                        updatedCount++
                                    }
                                }
                            }
                            if (updatedCount > 0) {
                                WildRiftRepository.applyChineseStats(allChamps)
                                return true
                            }
                        }
                    }
                }
            return false
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
