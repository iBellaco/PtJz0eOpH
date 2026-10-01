package com.example.data.sync

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class ScraperSourceStatus(
    val name: String,
    val url: String,
    val isHealthy: Boolean,
    val lastChecked: Long,
    val responseTimeMs: Long,
    val errorMessage: String?,
    val region: String = "GLOBAL"
)

data class GlobalScrapingSource(
    val id: String,
    val name: String,
    val url: String,
    val displayUrl: String
)

/** Isolated HTTP/parser boundary: only validated categories count as a healthy source. */
class GlobalTierSourceClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(25, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
) {
    data class SourceResult(val source: GlobalScrapingSource, val tiers: Map<String, String>,
                                    val status: ScraperSourceStatus)

    suspend fun fetch(source: GlobalScrapingSource, knownIds: Set<String>): SourceResult {
        val started = System.currentTimeMillis()
        var parsed = emptyMap<String, String>()
        var error: String? = null
        for (attempt in 0..1) {
            currentCoroutineContext().ensureActive()
            try {
                val request = Request.Builder().url(source.url)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/130.0.0.0 Mobile Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml")
                    .build()
                val response = client.newCall(request).execute()
                var retry = false
                response.use {
                    if (!it.isSuccessful) {
                        error = "HTTP ${it.code}"
                        retry = it.code == 429 || it.code in 500..599
                    } else {
                        val html = it.body?.string().orEmpty()
                        parsed = if (html.length <= 4_000_000)
                            GlobalTierConsensus.validated(RegionalTierParser.parse(html, source.id), knownIds)
                            else emptyMap()
                        error = if (parsed.isEmpty()) "No se encontraron suficientes categorías válidas" else null
                    }
                }
                currentCoroutineContext().ensureActive()
                if (!retry || attempt == 1) break
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: java.io.IOException) {
                error = "No se pudo conectar con la fuente"
                if (attempt == 1) break
            } catch (_: Exception) {
                error = "No se pudo interpretar la respuesta"
                break
            }
            delay(500L)
        }
        return SourceResult(source, parsed, ScraperSourceStatus(
            name = "${source.name} (${source.displayUrl})", url = source.url,
            isHealthy = parsed.isNotEmpty(), lastChecked = System.currentTimeMillis(),
            responseTimeMs = System.currentTimeMillis() - started, errorMessage = error
        ))
    }

}
