package com.example.data.sync

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.CacheControl
import java.util.concurrent.TimeUnit

data class ScraperSourceStatus(
    val name: String, val url: String, val isHealthy: Boolean, val lastChecked: Long,
    val responseTimeMs: Long, val errorMessage: String?, val region: String = "GLOBAL",
    val tierCount: Int = 0, val statsCount: Int = 0, val lastSuccessAt: Long = 0,
    val lastChangedAt: Long = 0, val unchangedChecks: Int = 0,
    val publishedPatch: String = "", val rosterCount: Int = 0, val historyCount: Int = 0,
    val lastRatesChangedAt: Long = 0L,
    val partialStatsCount: Int = 0, val missingStatsCount: Int = 0,
    val partialStatIds: List<String> = emptyList(), val missingStatIds: List<String> = emptyList()
)

data class GlobalScrapingSource(val id: String, val name: String, val url: String,
    val displayUrl: String, val expectedPatch: String? = null,
    val region: String = MetaRegion.DEFAULT)

/** HTTP success alone is insufficient: the publisher must identify the expected game patch. */
class GlobalTierSourceClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .callTimeout(45, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .followRedirects(true)
        .build()
) {
    data class SourceResult(val source: GlobalScrapingSource, val tiers: Map<String, String>,
        val status: ScraperSourceStatus, val stats: Map<String, ChampionMetaStats> = emptyMap(),
        val history: Map<String, List<ObservedRate>> = emptyMap(),
        val roleStats: Map<String, Map<com.example.model.LaneRole, ChampionMetaStats>> = emptyMap(),
        val chinaEloGroups: Map<String, ChinaLolmParser.EloSnapshot> = emptyMap())

    private fun request(url: String): String = client.newCall(Request.Builder().url(url)
        .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/130.0.0.0 Mobile Safari/537.36")
        .header("Accept", "application/json,text/html,application/xhtml+xml").cacheControl(CacheControl.FORCE_NETWORK).build())
        .execute().use { response ->
            if (!response.isSuccessful) throw SourceHttpException(response.code)
            val body = response.body ?: error("Empty response")
            // Bound both the transfer and decoded text. Fire's current archive
            // is already ~4.8 MB, so leave measured headroom for normal growth.
            val bytes = body.byteStream().readBytesLimited(8_000_000)
            bytes.toString(Charsets.UTF_8)
        }

    suspend fun fetch(source: GlobalScrapingSource, knownIds: Set<String>): SourceResult =
        if (source.region == MetaRegion.CHINA || source.id == "china_lolm") {
            fetchChina(source, knownIds)
        } else {
            fetchGlobal(source, knownIds)
        }

    private suspend fun fetchGlobal(source: GlobalScrapingSource, knownIds: Set<String>): SourceResult {
        val started = System.currentTimeMillis()
        val known = knownIds.map(RegionalTierParser::canonical).toSet()
        var parsed = emptyMap<String, String>()
        var stats = emptyMap<String, ChampionMetaStats>()
        var history = emptyMap<String, List<ObservedRate>>()
        var roleStats = emptyMap<String, Map<com.example.model.LaneRole, ChampionMetaStats>>()
        var error: String? = null
        var patch = ""
        for (attempt in 0..1) {
            currentCoroutineContext().ensureActive()
            try {
                val html = request(source.url)
                patch = PublishedStatsParser.patch(html, source.id).orEmpty()
                if (source.expectedPatch != null && patch != source.expectedPatch) {
                    error = if (patch.isBlank()) "No se pudo verificar el parche" else "Parche incompatible: $patch"
                    break
                }
                parsed = GlobalTierConsensus.validated(RegionalTierParser.parse(html, source.id), knownIds)
                stats = RegionalTierParser.parseStats(html, source.id).filterKeys { it in known }
                roleStats = RegionalTierParser.parseRoleStats(html, source.id).filterKeys { it in known }
                error = if (parsed.isEmpty() && stats.isEmpty()) "No se encontraron categorías ni estadísticas válidas" else null
                break
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (http: SourceHttpException) {
                error = "HTTP ${http.code}"
                if (attempt == 1 || (http.code != 429 && http.code !in 500..599)) break
            } catch (_: java.io.IOException) {
                error = "No se pudo conectar con la fuente"
                if (attempt == 1) break
            } catch (_: Exception) { error = "No se pudo interpretar la respuesta"; break }
            delay(500L)
        }
        // The tier page and the rates page are independent publisher endpoints.
        // A temporary failure or a layout change in one must not prevent us
        // from reading valid current rates from the other one. This is also
        // important for mobile networks where the large tier page can time out
        // while the compact rankings endpoint still responds.
        val extras = when {
            source.id == "wildriftcore" && source.url.contains("/tierlist/") -> listOf(source.url.replace("/tierlist/", "/rankings/"))
            source.id == "wildriftfire" -> listOf(
                "https://www.wildriftfire.com/stats?patch=${source.expectedPatch.orEmpty()}",
                "https://www.wildriftfire.com/stats"
            )
            else -> emptyList()
        }
        if (extras.isNotEmpty()) {
            try {
                var html: String? = null
                var last: Exception? = null
                for (candidate in extras) {
                    try {
                        html = requestWithRetry(candidate)
                        break
                    } catch (cancelled: CancellationException) { throw cancelled
                    } catch (exception: Exception) { last = exception }
                }
                if (html == null) throw last ?: java.io.IOException("Statistics request failed")
                if (source.id == "wildriftfire") {
                    val report = PublishedStatsParser.fire(html!!)
                    if (source.expectedPatch == null || report.patch == source.expectedPatch) {
                        stats = stats + report.stats.filterKeys { it in known }
                        history = report.history.filterKeys { it in known }
                        roleStats = roleStats + report.roleStats.filterKeys { it in known }
                        if (report.patch.isNotBlank()) patch = report.patch
                        if (stats.isNotEmpty()) error = null
                    } else if (stats.isEmpty()) error = "El historial pertenece a otro parche"
                } else {
                    val extraPatch = PublishedStatsParser.patch(html!!, source.id)
                    if (source.expectedPatch == null || extraPatch == source.expectedPatch) {
                        val fallbackStats = RegionalTierParser.parseStats(html, source.id).filterKeys { it in known }
                        // Rankings are lane rows. They can fill a missing global
                        // value but must not replace the explicit ALL tile.
                        stats = fallbackStats.filterKeys { it !in stats } + stats
                        roleStats = GlobalTierConsensus.mergeRoleStats(roleStats,
                            RegionalTierParser.parseRoleStats(html, source.id).filterKeys { it in known })
                        if (!extraPatch.isNullOrBlank()) patch = extraPatch
                        if (stats.isNotEmpty()) error = null
                    } else if (stats.isEmpty()) error = "Las estadísticas pertenecen a otro parche"
                }
                if (stats.isEmpty()) error = "Estadísticas no disponibles"
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (http: SourceHttpException) {
                if (stats.isEmpty()) error = "Estadísticas: HTTP ${http.code}"
            } catch (_: Exception) {
                if (stats.isEmpty()) error = "No se pudieron consultar las estadísticas"
            }
        }
        // Small catalogs require every known champion; full rosters require at
        // least 70%, rounded up so 99/142 cannot incorrectly pass.
        val minimumCoverage = if (known.size < 100) known.size else kotlin.math.ceil(known.size * 0.70).toInt()
        val coverage = (parsed.keys + stats.keys).map(RegionalTierParser::canonical).toSet().size
        val completeEnough = coverage >= minimumCoverage
        val normalizedStats = stats.mapKeys { (id, _) -> RegionalTierParser.canonical(id) }
        val completeStats = normalizedStats.count { it.value.isComplete() }
        // Any row that arrived but cannot prove all three finite metrics is
        // partial. This also catches malformed values instead of silently
        // classifying them as absent.
        val partialIds = normalizedStats.filterValues { !it.isComplete() }.keys.sorted()
        val missingIds = (known - normalizedStats.keys).sorted()
        if (error == null && !completeEnough) error = "Cobertura insuficiente: $coverage/${known.size} (mínimo $minimumCoverage)"
        return SourceResult(source, parsed, ScraperSourceStatus(name = source.name, url = source.url,
            isHealthy = error == null && completeEnough, lastChecked = System.currentTimeMillis(),
            responseTimeMs = System.currentTimeMillis() - started, errorMessage = error,
            region = source.region, tierCount = parsed.size, statsCount = completeStats, publishedPatch = patch,
            rosterCount = coverage, historyCount = history.count { it.value.isNotEmpty() },
            partialStatsCount = partialIds.size, missingStatsCount = missingIds.size,
            partialStatIds = partialIds, missingStatIds = missingIds), stats, history, roleStats)
    }

    private suspend fun fetchChina(source: GlobalScrapingSource, knownIds: Set<String>): SourceResult {
        val started = System.currentTimeMillis()
        var error: String? = null
        var parsed = ChinaLolmParser.Parsed()
        for (attempt in 0..1) {
            currentCoroutineContext().ensureActive()
            try {
                val heroPayload = request(ChinaLolmParser.HERO_LIST_URL)
                val rankPayload = request(ChinaLolmParser.RANK_LIST_URL)
                parsed = ChinaLolmParser.parse(heroPayload, rankPayload, knownIds)
                error = if (parsed.tiers.isEmpty() && parsed.stats.isEmpty())
                    "No se encontraron datos publicados de China" else null
                break
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (http: SourceHttpException) {
                error = "HTTP ${http.code}"
                if (attempt == 1 || (http.code != 429 && http.code !in 500..599)) break
            } catch (_: java.io.IOException) {
                error = "No se pudo conectar con la fuente china"
                if (attempt == 1) break
            } catch (_: Exception) {
                error = "No se pudo interpretar la respuesta china"
                break
            }
            delay(500L)
        }
        val normalizedStats = parsed.stats.mapKeys { (id, _) -> RegionalTierParser.canonical(id) }
        val normalizedTiers = parsed.tiers.mapKeys { (id, _) -> RegionalTierParser.canonical(id) }
        val completeStats = normalizedStats.count { it.value.isComplete() }
        val partialIds = normalizedStats.filterValues { !it.isComplete() }.keys.sorted()
        val known = knownIds.map(RegionalTierParser::canonical).toSet()
        val coverageIds = (normalizedTiers.keys + normalizedStats.keys).filter { it in known }.toSet()
        val missingIds = (known - normalizedStats.keys).sorted()
        val minimumCoverage = if (known.size < 100) known.size else kotlin.math.ceil(known.size * 0.70).toInt()
        if (error == null && coverageIds.size < minimumCoverage) {
            error = "Cobertura insuficiente: ${coverageIds.size}/${known.size} (mínimo $minimumCoverage)"
        }
        val status = ScraperSourceStatus(
            name = source.name,
            url = source.url,
            region = source.region,
            isHealthy = error == null && coverageIds.size >= minimumCoverage,
            lastChecked = System.currentTimeMillis(),
            responseTimeMs = System.currentTimeMillis() - started,
            errorMessage = error,
            tierCount = normalizedTiers.count { it.key in known },
            statsCount = completeStats,
            publishedPatch = parsed.publishedDate,
            rosterCount = coverageIds.size,
            partialStatsCount = partialIds.size,
            missingStatsCount = missingIds.size,
            partialStatIds = partialIds,
            missingStatIds = missingIds
        )
        return SourceResult(source, normalizedTiers.filterKeys { it in known }, status, normalizedStats,
            roleStats = parsed.roleStats.mapKeys { (id, _) -> RegionalTierParser.canonical(id) }
                .filterKeys { it in known },
            chinaEloGroups = parsed.eloGroups.mapValues { (_, group) ->
                group.copy(
                    tiers = group.tiers.mapKeys { (id, _) -> RegionalTierParser.canonical(id) }
                        .filterKeys { it in known },
                    stats = group.stats.mapKeys { (id, _) -> RegionalTierParser.canonical(id) }
                        .filterKeys { it in known },
                    roleStats = group.roleStats.mapKeys { (id, _) -> RegionalTierParser.canonical(id) }
                        .filterKeys { it in known }
                )
            })
    }

    private class SourceHttpException(val code: Int) : java.io.IOException()

    private suspend fun requestWithRetry(url: String): String {
        var last: Exception? = null
        for (attempt in 0..1) {
            currentCoroutineContext().ensureActive()
            try {
                return request(url)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (http: SourceHttpException) {
                last = http
                if (attempt == 1 || (http.code != 429 && http.code !in 500..599)) break
            } catch (io: java.io.IOException) {
                last = io
                if (attempt == 1) break
            }
            delay(500L)
        }
        throw last ?: java.io.IOException("Request failed")
    }

    private fun java.io.InputStream.readBytesLimited(limit: Int): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            val count = read(buffer)
            if (count < 0) break
            if (output.size() + count > limit) throw java.io.IOException("Response too large")
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }
}
