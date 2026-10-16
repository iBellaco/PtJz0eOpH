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
    val lastRatesChangedAt: Long = 0L
)

data class GlobalScrapingSource(val id: String, val name: String, val url: String,
    val displayUrl: String, val expectedPatch: String? = null)

/** HTTP success alone is insufficient: the publisher must identify the expected game patch. */
class GlobalTierSourceClient(
    private val client: OkHttpClient = OkHttpClient.Builder().connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS).callTimeout(18, TimeUnit.SECONDS).followRedirects(true).build()
) {
    data class SourceResult(val source: GlobalScrapingSource, val tiers: Map<String, String>,
        val status: ScraperSourceStatus, val stats: Map<String, ChampionMetaStats> = emptyMap(),
        val history: Map<String, List<ObservedRate>> = emptyMap(),
        val roleStats: Map<String, Map<com.example.model.LaneRole, ChampionMetaStats>> = emptyMap())

    private fun request(url: String): String = client.newCall(Request.Builder().url(url)
        .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/130.0.0.0 Mobile Safari/537.36")
        .header("Accept", "text/html,application/xhtml+xml").cacheControl(CacheControl.FORCE_NETWORK).build())
        .execute().use { response ->
            if (!response.isSuccessful) throw SourceHttpException(response.code)
            val body = response.body ?: error("Empty response")
            // Bound both the transfer and decoded text (the historical archive is ~4.4 MB).
            val bytes = body.byteStream().readBytesLimited(6_000_000)
            bytes.toString(Charsets.UTF_8)
        }

    suspend fun fetch(source: GlobalScrapingSource, knownIds: Set<String>): SourceResult {
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
        if (source.expectedPatch == null || patch == source.expectedPatch) {
            val extra = when {
                source.id == "wildriftcore" && source.url.contains("/tierlist/") -> source.url.replace("/tierlist/", "/rankings/")
                source.id == "wildriftfire" -> "https://www.wildriftfire.com/stats?patch=${source.expectedPatch.orEmpty()}"
                else -> null
            }
            if (extra != null) try {
                val html = request(extra)
                if (source.id == "wildriftfire") {
                    val report = PublishedStatsParser.fire(html)
                    if (source.expectedPatch == null || report.patch == source.expectedPatch) {
                        stats = stats + report.stats.filterKeys { it in known }
                        history = report.history.filterKeys { it in known }
                        roleStats = roleStats + report.roleStats.filterKeys { it in known }
                        if (history.values.none { it.isNotEmpty() }) error = "Historial no disponible"
                    } else error = "El historial pertenece a otro parche"
                } else {
                    val extraPatch = PublishedStatsParser.patch(html, source.id)
                    if (source.expectedPatch == null || extraPatch == source.expectedPatch) {
                        stats = stats + RegionalTierParser.parseStats(html, source.id).filterKeys { it in known }
                        roleStats = roleStats + RegionalTierParser.parseRoleStats(html, source.id).filterKeys { it in known }
                    } else error = "Las estadísticas pertenecen a otro parche"
                }
                if (stats.isEmpty()) error = "Estadísticas no disponibles"
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (http: SourceHttpException) { error = "Estadísticas: HTTP ${http.code}"
            } catch (_: Exception) { error = "No se pudieron consultar las estadísticas" }
        }
        return SourceResult(source, parsed, ScraperSourceStatus(name = source.name, url = source.url,
            isHealthy = parsed.isNotEmpty() || stats.isNotEmpty(), lastChecked = System.currentTimeMillis(),
            responseTimeMs = System.currentTimeMillis() - started, errorMessage = error,
            tierCount = parsed.size, statsCount = stats.size, publishedPatch = patch,
            rosterCount = (parsed.keys + stats.keys).size, historyCount = history.count { it.value.isNotEmpty() }), stats, history, roleStats)
    }

    private class SourceHttpException(val code: Int) : java.io.IOException()
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
