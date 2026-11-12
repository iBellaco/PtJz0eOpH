package com.example.data

import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

data class SavedDataStatistic(val label: String, val description: String, val count: Long? = null, val failed: Boolean = false, val estimatedBytes: Long? = null, val failureReason: String? = null)

data class StorageConsumption(val estimatedBytes: Long? = null, val dailyGrowthBytes: Long? = null,
    val sampledAtMillis: Long = 0, val complete: Boolean = false)

object DatabaseStatisticsRepository {
    val consumption = kotlinx.coroutines.flow.MutableStateFlow(StorageConsumption())
    private val mutex = kotlinx.coroutines.sync.Mutex()
    private var cachedRows: List<SavedDataStatistic> = emptyList()
    private var cachedAt = 0L
    suspend fun load(forceRefresh: Boolean = false): List<SavedDataStatistic> {
        mutex.lock()
        try {
            check(SupportTicketAccess.isAdmin())
            if (!forceRefresh && cachedRows.none { it.failed || it.failureReason != null } && cachedRows.isNotEmpty() && System.currentTimeMillis() - cachedAt < 300_000) return cachedRows
            val rows = query()
            val content = rows.filter { it.estimatedBytes != null }
            val now = System.currentTimeMillis()
            val total = content.sumOf { it.estimatedBytes ?: 0L }
            val complete = rows.none { it.failed } && content.size == rows.size
            // Store the baseline in the shared configuration so different devices use the same sample.
            val ref = FirebaseFirestore.getInstance().collection("system_config").document("saved_data_consumption")
            val previous = runCatching { ref.get(Source.SERVER).await() }.getOrNull()
            val coverage = content.map { it.label }.sorted()
            val sameCoverage = previous?.get("coverage") == coverage
            val baselineAt = if (sameCoverage) previous?.getLong("baselineAt") ?: now else now
            val baselineBytes = if (sameCoverage) previous?.getLong("baselineBytes") ?: total else total
            val rate = if (content.isNotEmpty()) StorageConsumptionPolicy.dailyGrowth(baselineBytes, total, baselineAt, now) else null
            consumption.value = StorageConsumption(total.takeIf { content.isNotEmpty() }, rate, now, complete)
            if (content.isNotEmpty()) runCatching {
                val reset = now - baselineAt >= 7 * 86_400_000L
                ref.set(mapOf("estimatedContentBytes" to total, "sampledAt" to now,
                    "baselineAt" to if (reset) now else baselineAt,
                    "baselineBytes" to if (reset) total else baselineBytes,
                    "dailyGrowthBytes" to (rate ?: 0L), "growthAvailable" to (rate != null), "coverage" to coverage)).await()
            }
            cachedRows = rows; cachedAt = now
            return rows
        } finally { mutex.unlock() }
    }

    private suspend fun query(): List<SavedDataStatistic> = coroutineScope {
        check(SupportTicketAccess.isAdmin())
        val db = FirebaseFirestore.getInstance()
        val categories = listOf(
            Triple("support_reports", "Conversaciones de soporte", "Reportes, respuestas y estado de cada conversación."),
            Triple("feedbacks", "Reportes y sugerencias", "Errores e ideas enviados por los usuarios."),
            Triple("pending_sponsor_ads", "Solicitudes de patrocinio", "Publicaciones de patrocinadores pendientes de revisión."),
            Triple("streamer_requests", "Solicitudes de streamers", "Última solicitud y estado de cada canal."),
            Triple("streamer_click_metrics", "Contadores de streamers", "Clics acumulados de cada publicación."),
            Triple("moderator_requests", "Solicitudes de roles", "Solicitudes de acceso pendientes y revisadas."),
            Triple("system_config", "Configuración activa", "Solo registros de configuración que la aplicación usa actualmente; se excluyen servicios retirados y datos financieros.")
        )
        // Read each parent registry once. Direct child queries avoid requiring a global
        // collection-group index/rule. Account profiles, balances, essences and
        // registered devices are deliberately not exposed in this summary.
        val streamers = async { readParents(db.collection("streamer_requests")) }
        val topRows = categories.map { (path, label, description) -> async {
            try {
                val known = when {
                    path == "streamer_requests" -> streamers.await().getOrThrow().documents
                    path == "system_config" -> readActiveSystemConfig(db)
                    else -> null
                }
                val result = if (known != null) {
                    val sizes = byteSizes(known)
                    SavedDataQueryResult(sizes.size.toLong(), sizes.sum())
                } else readCount(db.collection(path))
                statistic(label, description, result)
            } catch (error: Exception) { failedRow(label, description, error) }
        } }.awaitAll()
        val nestedRows = listOf("history" to "Historial de publicaciones")
            .map { (path, label) -> async {
                val description = "Historial operativo de publicaciones conservado para trazabilidad; no incluye perfiles, saldos ni dispositivos."
                try {
                    val parents = streamers.await().getOrThrow().documents
                    val limiter = Semaphore(4)
                    val children = parents.map { parent -> async { limiter.withPermit {
                        try { Result.success(readCount(parent.reference.collection(path))) }
                        catch (error: Exception) { if (error is CancellationException) throw error; Result.failure(error) }
                    } } }.awaitAll().map { it.getOrThrow() }
                    statistic(label, description, SavedDataQueryPolicy.combine(children))
                } catch (error: Exception) { failedRow(label, description, error) }
            } }.awaitAll()
        topRows + nestedRows
    }

    private suspend fun readActiveSystemConfig(db: FirebaseFirestore): List<com.google.firebase.firestore.DocumentSnapshot> {
        val activeIds = setOf(
            "app_notices", "announcement", "overlay_access", "overlay_activation",
            "app_notice_analytics", "streamer_live", "creator_builds", "saved_data_consumption"
        )
        val snapshot = db.collection("system_config").get(Source.SERVER).await()
        val active = snapshot.documents.filter { document ->
            document.id in activeIds || document.id.startsWith("tier_movement_")
        }
        return active
    }

    private suspend fun readParents(query: Query): Result<QuerySnapshot> = try {
        Result.success(query.get(Source.SERVER).await())
    } catch (error: Exception) {
        if (error is CancellationException) throw error
        Result.failure(error)
    }

    private fun byteSizes(snapshot: QuerySnapshot): List<Long> = byteSizes(snapshot.documents)

    private fun byteSizes(documents: List<com.google.firebase.firestore.DocumentSnapshot>): List<Long> = documents.map {
        org.json.JSONObject(it.data.orEmpty()).toString().toByteArray(Charsets.UTF_8).size.toLong()
    }

    private suspend fun readCount(query: Query) = SavedDataQueryPolicy.read(
        aggregate = { query.count().get(AggregateSource.SERVER).await().count },
        fullRead = { byteSizes(query.get(Source.SERVER).await()) },
        sample = { byteSizes(query.limit(25).get(Source.SERVER).await()) })

    private fun statistic(label: String, description: String, result: SavedDataQueryResult) =
        SavedDataStatistic(label, description, result.count, estimatedBytes = result.estimatedBytes,
            failureReason = if (result.estimatedBytes == null) "Recuento disponible. No se pudo estimar el tamaño de esta categoría." else null)

    private fun failedRow(label: String, description: String, error: Exception): SavedDataStatistic {
        if (error is CancellationException) throw error
        val reason = when ((error as? FirebaseFirestoreException)?.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> "El servicio rechazó el acceso a esta categoría. Sus permisos deben actualizarse."
            FirebaseFirestoreException.Code.UNAUTHENTICATED -> "La sesión no pudo verificarse. Vuelve a iniciar sesión."
            FirebaseFirestoreException.Code.UNAVAILABLE, FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> "El servicio no respondió. Comprueba tu conexión y vuelve a actualizar."
            else -> "No se pudo consultar esta categoría."
        }
        return SavedDataStatistic(label, description, failed = true, failureReason = reason)
    }
}
