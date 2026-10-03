package com.example.data

import com.example.util.AuthManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/** The receipt and optional notification share the same commit as the balance. */
object AdminEssenceAdjustment {
    fun delta(balance: Long, amount: Long, addition: Boolean): Long {
        require(amount > 0 && balance >= 0)
        return if (addition) { Math.addExact(balance, amount); amount } else -minOf(balance, amount)
    }

    suspend fun apply(uid: String, amount: Long, currency: String, addition: Boolean,
        notify: Boolean, title: String, customBody: String?): Map<String, Any> {
        check(SupportTicketAccess.isAdmin())
        require(currency in setOf("BLUE", "ORANGE") && uid.isNotBlank())
        val ref = FirebaseFirestore.getInstance().collection("users").document(uid)
        val id = java.util.UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        return FirebaseFirestore.getInstance().runTransaction { tx ->
            val account = tx.get(ref).data ?: error("Usuario no disponible")
            val field = if (currency == "BLUE") "blueEssence" else "orangeEssence"
            val abbreviation = if (currency == "BLUE") "EA" else "EN"
            val balance = (account[field] as? Number)?.toLong() ?: 0L
            val change = delta(balance, amount, addition)
            val updated = account + (field to balance + change)
            if (change != 0L) {
                val receipt = mapOf("id" to id, "timestamp" to now, "durationMillis" to 0L,
                    "planName" to if (addition) "Regalo de Esencias" else "Ajuste de Esencias",
                    "status" to if (addition) "Añadido por Administrador" else "Descontado por Administrador",
                    "amount" to "${if (change > 0) "+" else ""}$change $abbreviation",
                    "source" to "ADMIN_ESSENCE_ADJUSTMENT")
                tx.update(ref, mapOf(field to balance + change, "subscriptionHistory" to FieldValue.arrayUnion(receipt)))
                if (notify) {
                    val body = customBody?.takeIf { it.isNotBlank() } ?: if (addition)
                        "Se han acreditado +$change $abbreviation a tu cuenta de Coach."
                        else "Se ha realizado un ajuste de $change $abbreviation en tu saldo por parte del equipo de administración."
                    val message = mapOf("id" to id, "title" to title, "content" to body, "tag" to "GENERAL", "panel" to "HISTORY",
                        "historyRecord" to receipt, "timestamp" to now, "isRead" to false)
                    tx.set(ref.collection("messages").document(id), message)
                    tx.update(ref, mapOf("privateMessages" to FieldValue.arrayUnion(message), "hasUnreadMessages" to true,
                        "unreadMessagesCount" to FieldValue.increment(1)))
                }
            }
            updated
        }.await()
    }
}
