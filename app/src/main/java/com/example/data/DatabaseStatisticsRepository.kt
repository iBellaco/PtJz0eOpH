package com.example.data

import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

data class SavedDataStatistic(val label: String, val description: String, val count: Long? = null, val failed: Boolean = false, val estimatedBytes: Long? = null)

data class StorageConsumption(val estimatedBytes: Long? = null, val dailyGrowthBytes: Long? = null,
    val sampledAtMillis: Long = 0, val complete: Boolean = false)

object DatabaseStatisticsRepository {
    val consumption = kotlinx.coroutines.flow.MutableStateFlow(StorageConsumption())
    private val mutex = kotlinx.coroutines.sync.Mutex()
    private var cachedRows: List<SavedDataStatistic> = emptyList()
    private var cachedAt = 0L
    suspend fun load(): List<SavedDataStatistic> {
        mutex.lock()
        try {
            check(SupportTicketAccess.isAdmin())
            if (cachedRows.isNotEmpty() && System.currentTimeMillis() - cachedAt < 300_000) return cachedRows
            val rows = query()
            val content = rows.filter { it.estimatedBytes != null }
            val now = System.currentTimeMillis()
            val total = content.sumOf { it.estimatedBytes ?: 0L }
            val complete = rows.none { it.failed } && content.size == 12
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
            Triple("users", "Cuentas registradas", "Perfiles, roles, saldos, suscripciones y dispositivos registrados."),
            Triple("support_reports", "Conversaciones de soporte", "Reportes, respuestas y estado de cada conversación."),
            Triple("feedbacks", "Reportes y sugerencias", "Errores e ideas enviados por los usuarios."),
            Triple("pending_sponsor_ads", "Solicitudes de patrocinio", "Publicaciones de patrocinadores pendientes de revisión."),
            Triple("streamer_requests", "Solicitudes de streamers", "Última solicitud y estado de cada canal."),
            Triple("streamer_click_metrics", "Contadores de streamers", "Clics acumulados de cada publicación."),
            Triple("moderator_requests", "Solicitudes de roles", "Solicitudes de acceso pendientes y revisadas."),
            Triple("cash_redemptions", "Solicitudes de canje", "Importes solicitados, pagos manuales y devoluciones."),
            Triple("system_config", "Configuración compartida", "Avisos, canales activos, builds publicadas y preferencias generales.")
        )
        val accountTotals = async {
            runCatching {
                val accounts = db.collection("users").get(Source.SERVER).await().documents.mapNotNull { it.data }
                listOf(SavedDataStatistic("Suscripciones premium activas", "Acceso vigente por tiempo o por función del usuario.", accounts.count { com.example.model.PremiumAccessPolicy.isActiveAccount(it) }.toLong()),
                    SavedDataStatistic("Esencias Azules guardadas", "Suma de los saldos actuales de todas las cuentas.", accounts.sumOf { (it["blueEssence"] as? Number)?.toLong() ?: 0L }),
                    SavedDataStatistic("Esencias Naranjas guardadas", "Suma de los saldos actuales de todas las cuentas.", accounts.sumOf { (it["orangeEssence"] as? Number)?.toLong() ?: 0L }),
                    SavedDataStatistic("Dispositivos registrados", "Dispositivos diferentes que ocupan espacios en las cuentas.", accounts.sumOf { (it["registeredDevices"] as? List<*>)?.filterIsInstance<String>()?.distinct()?.size?.toLong() ?: 0L }))
            }.getOrElse { listOf(SavedDataStatistic("Saldos y dispositivos", "Resumen de los datos de las cuentas.", failed=true)) }
        }
        categories.map { (path, label, description) -> async {
            runCatching {
                val count = db.collection(path).count().get(AggregateSource.SERVER).await().count
                val sample = db.collection(path).limit(25).get(Source.SERVER).await().documents
                SavedDataStatistic(label, description, count, estimatedBytes = StorageConsumptionPolicy.estimate(count,
                    sample.map { org.json.JSONObject(it.data.orEmpty()).toString().toByteArray(Charsets.UTF_8).size.toLong() }))
            }
                .getOrElse { SavedDataStatistic(label, description, failed = true) }
        } }.awaitAll() + listOf("messages" to "Mensajes de la bandeja de entrada", "subscription_history" to "Movimientos del historial", "history" to "Historial de publicaciones")
            .map { (path, label) -> async {
                runCatching {
                    val count = db.collectionGroup(path).count().get(AggregateSource.SERVER).await().count
                    val sample = db.collectionGroup(path).limit(25).get(Source.SERVER).await().documents
                    SavedDataStatistic(label, "Registros guardados y sincronizados entre dispositivos.", count,
                        estimatedBytes = StorageConsumptionPolicy.estimate(count, sample.map {
                            org.json.JSONObject(it.data.orEmpty()).toString().toByteArray(Charsets.UTF_8).size.toLong() }))
                }
                    .getOrElse { SavedDataStatistic(label, "Registros guardados y sincronizados entre dispositivos.", failed = true) }
            } }.awaitAll() + accountTotals.await()
    }
}
