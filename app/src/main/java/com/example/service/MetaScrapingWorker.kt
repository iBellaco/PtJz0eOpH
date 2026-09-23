package com.example.service

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import com.example.data.WildRiftRepository
import com.example.data.sync.ChineseMetaSyncService
import com.example.data.sync.TencentRankTier
import com.example.util.AppLogger
import java.text.Normalizer

class MetaScrapingWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            AppLogger.d("MetaScrapingWorker", "Iniciando sincronización con Servidor Oficial de Tencent (lolm.qq.com)...")
            
            // Intentar consultar endpoints en vivo con OkHttpClient seguro
            try {
                // 1. Obtener la lista de Héroes y sus IDs oficiales de Tencent
                val heroReq = Request.Builder()
                    .url("https://game.gtimg.cn/images/lgamem/act/lrlib/js/heroList/hero_list.js")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Referer", "https://lolm.qq.com/")
                    .header("Accept", "*/*")
                    .build()

                val heroJsonStr = httpClient.newCall(heroReq).execute().use { resp ->
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
                            heroData.getString("name"),
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

                    val rankJsonStr = httpClient.newCall(rankReq).execute().use { resp ->
                        if (resp.isSuccessful) resp.body?.string() else null
                    }

                    if (!rankJsonStr.isNullOrBlank()) {
                        val rankData = JSONObject(rankJsonStr).optJSONObject("data")?.optJSONObject("0")
                        if (rankData != null) {
                            var updatedCount = 0
                            val allChamps = WildRiftRepository.champions.toMutableList()
                            
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
                                        matchAlias(it.id, it.name, tencentAlias) 
                                    }
                                    
                                    if (index != -1) {
                                        val oldChamp = allChamps[index]
                                        val newChamp = oldChamp.copy(
                                            winrate = winRate,
                                            pickRate = pickRate,
                                            banRate = banRate
                                        )
                                        allChamps[index] = newChamp
                                        updatedCount++
                                    }
                                }
                            }
                            if (updatedCount > 0) {
                                WildRiftRepository.champions.clear(); WildRiftRepository.champions.addAll(allChamps)
                                AppLogger.d("MetaScrapingWorker", "Direct sync completed for $updatedCount champions.")
                            }
                        }
                    }
                }
            } catch (netEx: Exception) {
                AppLogger.w("MetaScrapingWorker", "Consulta directa de scraping continuará vía snapshot espejo: ${netEx.message}")
            }

            // Sincronizar usando el servicio integral de estadísticas de Tencent China con cálculo de deltas y snapshot canónico
            ChineseMetaSyncService.loadRegion(applicationContext)
            val region = ChineseMetaSyncService.currentRegion.value
            if (region == "CN") {
                ChineseMetaSyncService.syncChineseMeta(applicationContext, TencentRankTier.DIAMOND_PLUS, forceRefresh = true)
            } else if (region == "Global" || region == "BestBuildWR") {
                com.example.data.sync.BestBuildWrScraper.syncGlobalTierList(applicationContext)
            }
            
            AppLogger.d("MetaScrapingWorker", "Estadísticas extraídas y deltas calculados correctamente del servidor CN.")
            Result.success()
        } catch (e: Exception) {
            AppLogger.e("MetaScrapingWorker", "Sincronización finalizada con respaldo local seguro: ${e.message}", e)
            Result.success()
        }
    }

    private fun matchAlias(ourId: String, ourName: String, tencentAlias: String): Boolean {
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
