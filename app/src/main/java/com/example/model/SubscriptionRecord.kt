package com.example.model

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class SubscriptionRecord(
    val id: String = "",
    val timestamp: Long = 0L,
    val durationMillis: Long = 0L,
    val planName: String = "",
    val status: String = "",
    val amount: String = "",
    val source: String = ""
) {
    val isDeduction: Boolean
        get() = amount.startsWith("-") ||
                status.contains("Descontad", ignoreCase = true) ||
                status.contains("Consumo", ignoreCase = true) ||
                status.contains("Deducc", ignoreCase = true) ||
                planName.contains("Suscripción a Creador", ignoreCase = true) ||
                planName.contains("Consumo", ignoreCase = true)

    val isAddition: Boolean
        get() = !isDeduction && (amount.startsWith("+") ||
                status.contains("Añadid", ignoreCase = true) ||
                status.contains("Acreditad", ignoreCase = true) ||
                status.contains("Recarga", ignoreCase = true) ||
                status.contains("Recompensa", ignoreCase = true) ||
                planName.contains("Pago por Suscriptor", ignoreCase = true) ||
                planName.contains("Recompensa", ignoreCase = true) ||
                planName.contains("Recarga", ignoreCase = true) ||
                (planName.contains("Ajuste de Administrador", ignoreCase = true) && !amount.startsWith("-")))

    val isEssenceTransaction: Boolean
        get() = source != "ESSENCE_PURCHASE" && (isDeduction || isAddition ||
                amount.contains("EA", ignoreCase = true) ||
                amount.contains("EN", ignoreCase = true) ||
                planName.contains("Esencia", ignoreCase = true) ||
                status.contains("Esencia", ignoreCase = true) ||
                status.contains("Descontad", ignoreCase = true) ||
                status.contains("Añadid", ignoreCase = true))

    val isOrangeEssence: Boolean
        get() = amount.contains("EN", ignoreCase = true) ||
                planName.contains("Naranja", ignoreCase = true) ||
                status.contains("Naranja", ignoreCase = true) ||
                planName.contains("Suscripción a Creador", ignoreCase = true) ||
                planName.contains("Pago por Suscriptor", ignoreCase = true)

    val isBlueEssence: Boolean
        get() = amount.contains("EA", ignoreCase = true) ||
                planName.contains("Azul", ignoreCase = true) ||
                status.contains("Azul", ignoreCase = true)

    val isFromAdmin: Boolean
        get() = source.startsWith("ADMIN_") || planName.contains("Admin", ignoreCase = true) ||
                status.contains("Admin", ignoreCase = true) ||
                planName.contains("Asignación Manual", ignoreCase = true) ||
                planName.contains("Regalo Admin", ignoreCase = true)

    val isFromSubscription: Boolean
        get() = planName.contains("Suscrip", ignoreCase = true) ||
                planName.contains("Suscriptor", ignoreCase = true) ||
                status.contains("Suscrip", ignoreCase = true) ||
                status.contains("Suscriptor", ignoreCase = true) ||
                planName.contains("Creador", ignoreCase = true)

    companion object {
        fun fromDocument(doc: DocumentSnapshot): SubscriptionRecord? {
            return try {
                fromData(doc.id, doc.data ?: return null)
            } catch (e: Exception) {
                Log.w("SubscriptionRecord", "Failed to parse subscription document", e)
                null
            }
        }

        fun fromData(id: String, data: Map<String, Any>): SubscriptionRecord? {
            return try {
                // Parse timestamp with flexible support for Long, Double, Timestamp, Date, String
                val rawTimestamp = data["timestamp"] ?: data["created_at"] ?: data["date"] ?: data["time"]
                val timestamp: Long = when (rawTimestamp) {
                    is Number -> rawTimestamp.toLong()
                    is com.google.firebase.Timestamp -> rawTimestamp.toDate().time
                    is java.util.Date -> rawTimestamp.time
                    is String -> rawTimestamp.toLongOrNull() ?: 0L
                    else -> 0L
                }

                // Parse durationMillis with support for Long, Int, Double, String
                val rawDuration = data["durationMillis"] ?: data["duration_millis"] ?: data["duration"] ?: data["durationMs"]
                val durationMillis: Long = when (rawDuration) {
                    is Number -> rawDuration.toLong()
                    is String -> rawDuration.toLongOrNull() ?: 0L
                    else -> 0L
                }

                // Parse planName with multiple key variants
                val rawPlan = data["planName"] ?: data["plan_name"] ?: data["plan"] ?: data["name"] ?: data["title"]
                val planName = rawPlan?.toString()?.trim() ?: ""

                // Parse status
                val rawStatus = data["status"] ?: data["state"]
                val status = rawStatus?.toString()?.trim() ?: "Completado"

                // Parse amount / price
                val rawAmount = data["amount"] ?: data["price"] ?: data["cost"]
                val amount = when (rawAmount) {
                    is Number -> "$${rawAmount}"
                    is String -> if (rawAmount.isNotBlank()) rawAmount else "$0.00"
                    else -> "$0.00"
                }

                SubscriptionRecord(
                    id = id,
                    timestamp = timestamp,
                    durationMillis = durationMillis,
                    planName = planName.ifEmpty { "Suscripción Premium" },
                    status = status,
                    amount = amount,
                    source = data["source"] as? String ?: ""
                )
            } catch (e: Exception) {
                Log.w("SubscriptionRecord", "Failed to parse subscription record", e)
                null
            }
        }
    }
}
