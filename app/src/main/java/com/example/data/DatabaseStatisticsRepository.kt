package com.example.data

import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

data class SavedDataStatistic(val label: String, val description: String, val count: Long? = null, val failed: Boolean = false)

object DatabaseStatisticsRepository {
    suspend fun load(): List<SavedDataStatistic> = coroutineScope {
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
            runCatching { SavedDataStatistic(label, description, db.collection(path).count().get(AggregateSource.SERVER).await().count) }
                .getOrElse { SavedDataStatistic(label, description, failed = true) }
        } }.awaitAll() + listOf("messages" to "Mensajes de la bandeja de entrada", "subscription_history" to "Movimientos del historial")
            .map { (path, label) -> async {
                runCatching { SavedDataStatistic(label, "Registros guardados y sincronizados entre dispositivos.", db.collectionGroup(path).count().get(AggregateSource.SERVER).await().count) }
                    .getOrElse { SavedDataStatistic(label, "Registros guardados y sincronizados entre dispositivos.", failed = true) }
            } }.awaitAll() + accountTotals.await()
    }
}
