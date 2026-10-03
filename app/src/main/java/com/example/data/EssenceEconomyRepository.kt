package com.example.data

import com.example.util.AuthManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/** Balance, entitlement and receipt are committed together; a retry reuses its operation ID. */
object EssenceEconomyRepository {
    private val db get() = FirebaseFirestore.getInstance()
    val redemptions get() = db.collection("cash_redemptions")

    suspend fun purchase(id: String, plan: EssencePremiumPlan, currency: EssenceCurrency): Result<Unit> = withContext(Dispatchers.IO) { runCatching {
        val user = AuthManager.getAuth()?.currentUser
        check(!AuthManager.isGuestOrUnauthenticated(user)) { "Inicia sesión" }
        val profile = db.collection("users").document(user!!.uid)
        val operation = profile.collection("economy_operations").document(id)
        db.runTransaction { tx ->
            val previous = tx.get(operation)
            if (!previous.exists()) {
                val account = tx.get(profile).data.orEmpty()
                val now = System.currentTimeMillis()
                val (remaining, until) = EssenceEconomyPolicy.purchase(account, plan, currency, now)
                val receipt = mapOf("id" to id, "timestamp" to now, "durationMillis" to plan.days * com.example.model.PremiumAccessPolicy.DAY_MILLIS,
                    "planName" to if (plan == EssencePremiumPlan.MONTHLY) "Suscripción Premium mensual" else "Suscripción Premium anual",
                    "status" to "Completado", "amount" to "-${plan.cost(currency)} ${currency.abbreviation}", "source" to "ESSENCE_PURCHASE")
                tx.set(operation, mapOf("id" to id, "userId" to user.uid, "kind" to "PREMIUM", "plan" to plan.name,
                    "currency" to currency.name, "cost" to plan.cost(currency), "premiumUntil" to until,
                    "timestamp" to now, "createdAt" to FieldValue.serverTimestamp(), "receipt" to receipt))
                tx.update(profile, mapOf(currency.field to remaining, "premiumUntil" to until,
                    "subscriptionPlan" to receipt.getValue("planName"), "lastEconomyOperation" to id))
                tx.set(profile.collection("subscription_history").document(id), receipt)
            }
        }.await()
        Unit
    } }

    suspend fun redeem(id: String, amount: Long, network: UsdtNetwork, wallet: String): Result<Unit> = withContext(Dispatchers.IO) { runCatching {
        val user = AuthManager.getAuth()?.currentUser
        check(!AuthManager.isGuestOrUnauthenticated(user)) { "Inicia sesión" }
        check(UsdtWalletPolicy.valid(network, wallet)) { "Billetera USDT no válida" }
        val profile = db.collection("users").document(user!!.uid)
        val adminClaim = user.getIdToken(false).await().claims["admin"] == true
        val operation = profile.collection("economy_operations").document(id)
        db.runTransaction { tx ->
            val previous = tx.get(operation)
            if (!previous.exists()) {
                val account = tx.get(profile).data.orEmpty()
                val remaining = EssenceEconomyPolicy.redeem(account, amount, adminClaim)
                val now = System.currentTimeMillis()
                val receipt = mapOf("id" to id, "timestamp" to now, "durationMillis" to 0L, "planName" to "Canje de Esencia Naranja",
                    "status" to "Pendiente", "amount" to "-$amount EN", "source" to "CASH_REDEMPTION")
                tx.set(operation, mapOf("id" to id, "userId" to user.uid, "kind" to "CASH", "currency" to "ORANGE", "cost" to amount,
                    "usd" to amount, "network" to network.name, "wallet" to wallet, "timestamp" to now, "createdAt" to FieldValue.serverTimestamp(), "receipt" to receipt))
                tx.update(profile, mapOf("orangeEssence" to remaining, "lastEconomyOperation" to id))
                tx.set(redemptions.document(id), mapOf("id" to id, "userId" to user.uid, "email" to user.email.orEmpty(),
                    "amount" to amount, "usd" to amount, "paymentCurrency" to "USDT", "network" to network.name, "wallet" to wallet, "status" to "PENDING", "requestedAt" to FieldValue.serverTimestamp(),
                    "requestedAtMillis" to now))
                val ticketId = "payment_$id"
                val body = "Solicitud de pago: $amount USDT. Red: ${network.name}. Billetera: $wallet. Plazo de revisión: 24 a 72 horas."
                val conversation = SupportConversationPolicy.initial(ticketId, account["name"] as? String ?: "Usuario", body, now)
                    .map { SupportConversationPolicy.encode(it, if (it.senderRole == "USER") user.uid else "") }
                val ticket = mapOf("id" to ticketId, "reportId" to ticketId, "redemptionId" to id, "userId" to user.uid,
                    "userEmail" to user.email.orEmpty(), "userName" to (account["name"] as? String ?: "Usuario"),
                    "title" to "Solicitud de pago USDT", "description" to body, "content" to body, "tag" to "PAGO", "type" to "PAGO",
                    "panel" to "HISTORY", "staffVisible" to false, "status" to "PENDING", "staffRead" to false,
                    "isRead" to true, "userRead" to true, "userCanReply" to false, "timestamp" to now, "createdAt" to FieldValue.serverTimestamp(), "conversation" to conversation)
                tx.set(db.collection("support_reports").document(ticketId), ticket)
                tx.set(profile.collection("messages").document(ticketId), ticket)
                tx.set(profile.collection("subscription_history").document(id), receipt)
            }
        }.await()
        Unit
    } }

    suspend fun resolve(id: String, paid: Boolean): Result<Unit> = withContext(Dispatchers.IO) { runCatching {
        check(SupportTicketAccess.isAdmin())
        val request = redemptions.document(id)
        db.runTransaction { tx ->
            val snapshot = tx.get(request)
            check(snapshot.getString("status") == "PENDING")
            val uid = snapshot.getString("userId") ?: error("Solicitud no disponible")
            val profile = db.collection("users").document(uid)
            val account = tx.get(profile)
            val amount = snapshot.getLong("amount") ?: error("Solicitud no disponible")
            if (!paid) tx.update(profile, "orangeEssence", (account.getLong("orangeEssence") ?: 0L) + amount)
            tx.update(request, mapOf("status" to if (paid) "PAID" else "REJECTED", "resolvedAt" to FieldValue.serverTimestamp()))
            tx.update(profile.collection("subscription_history").document(id), "status", if (paid) "Completado" else "Rechazado y reembolsado")
            tx.set(profile.collection("messages").document("redemption_$id"), mapOf("id" to "redemption_$id", "title" to "Canje de Esencia Naranja",
                "content" to if (paid) "Tu solicitud de canje fue pagada." else "Tu solicitud fue rechazada y las esencias fueron devueltas.",
                "panel" to "HISTORY", "isRead" to false, "timestamp" to System.currentTimeMillis(), "tag" to "GENERAL"))
        }.await()
        Unit
    } }
}
