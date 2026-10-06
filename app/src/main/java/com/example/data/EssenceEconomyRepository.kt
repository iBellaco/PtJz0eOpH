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
    const val CASH_HISTORY_RETENTION_MILLIS = 14L * 24 * 60 * 60 * 1000L

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

    suspend fun redeem(id: String, amount: Long, network: UsdtNetwork?, wallet: String, binanceEmail: String = ""): Result<Unit> = withContext(Dispatchers.IO) { runCatching {
        val user = AuthManager.getAuth()?.currentUser
        check(!AuthManager.isGuestOrUnauthenticated(user)) { "Inicia sesión" }
        val cleanBinanceEmail = binanceEmail.trim().lowercase(java.util.Locale.ROOT)
        val cleanWallet = wallet.trim()
        if (cleanBinanceEmail.isNotBlank()) {
            check(UsdtWalletPolicy.validBinanceEmail(cleanBinanceEmail)) { "Correo electrónico de Binance no válido" }
        } else {
            check(network != null && UsdtWalletPolicy.valid(network, cleanWallet)) { "Billetera USDT no válida" }
        }
        val payoutNetwork = if (cleanBinanceEmail.isNotBlank()) "" else network!!.name
        val payoutWallet = if (cleanBinanceEmail.isNotBlank()) "" else cleanWallet
        val profile = db.collection("users").document(user!!.uid)
        val adminClaim = runCatching { user.getIdToken(false).await().claims["admin"] == true }.getOrDefault(false)
        val operation = profile.collection("economy_operations").document(id)
        val fee = if (cleanBinanceEmail.isNotBlank()) 0L else BinanceCommissionManager.getFee(network!!)
        val totalCost = amount + fee
        val userEmail = (user.email ?: "").trim().lowercase()
        db.runTransaction { tx ->
            val previous = tx.get(operation)
            if (!previous.exists()) {
                val account = tx.get(profile).data.orEmpty()
                val remainingAfterCost = EssenceEconomyPolicy.redeem(account, amount, fee, adminClaim)
                val now = System.currentTimeMillis()
                val userName = (account["name"] as? String).orEmpty()
                val visibleUserName = userName.ifBlank { user.displayName?.takeIf { it.isNotBlank() } ?: "Usuario" }
                val receipt = mapOf("id" to id, "timestamp" to now, "durationMillis" to 0L, "planName" to "Canje de Esencia Naranja",
                    "status" to "Pendiente", "amount" to "-$totalCost EN", "source" to "CASH_REDEMPTION")
                tx.set(operation, mapOf("id" to id, "userId" to user.uid, "kind" to "CASH", "currency" to "ORANGE", "cost" to amount,
                    "usd" to amount, "network" to payoutNetwork, "wallet" to payoutWallet, "binanceEmail" to cleanBinanceEmail,
                    "fee" to fee, "totalCost" to totalCost,
                    "timestamp" to now, "createdAt" to FieldValue.serverTimestamp(), "receipt" to receipt))
                tx.update(profile, mapOf("orangeEssence" to remainingAfterCost, "lastEconomyOperation" to id))
                tx.set(redemptions.document(id), mapOf("id" to id, "userId" to user.uid, "email" to userEmail, "userName" to visibleUserName,
                    "amount" to amount, "usd" to amount, "paymentCurrency" to "USDT", "network" to payoutNetwork, "wallet" to payoutWallet,
                    "binanceEmail" to cleanBinanceEmail, "status" to "PENDING", "requestedAt" to FieldValue.serverTimestamp(),
                    "requestedAtMillis" to now, "fee" to fee, "totalDeducted" to totalCost))
                val ticketId = "payment_$id"
                val destination = if (cleanBinanceEmail.isNotBlank()) {
                    "Correo Binance: $cleanBinanceEmail. Sin comisión de red."
                } else {
                    "Red: $payoutNetwork. Comisión de red: $fee EN (a cargo del usuario). Billetera: $payoutWallet."
                }
                val body = "Solicitud de pago: $amount USDT. $destination Total descontado: $totalCost EN. Plazo de revisión: 24 a 72 horas."
                val conversation = listOf(
                    mapOf(
                        "id" to "${ticketId}_initial",
                        "senderName" to visibleUserName,
                        "senderRole" to "USER",
                        "senderUid" to user.uid,
                        "senderEmail" to userEmail,
                        "text" to body,
                        "timestampMillis" to now,
                        "isGreeting" to false
                    ),
                    mapOf(
                        "id" to "${ticketId}_system",
                        "senderName" to "Sistema Coach",
                        "senderRole" to "SYSTEM",
                        "senderUid" to "",
                        "senderEmail" to "",
                        "text" to SupportConversationPolicy.LEGACY_SYSTEM_GREETING,
                        "timestampMillis" to now + 1,
                        "isGreeting" to true
                    )
                )
                val ticket = mapOf("id" to ticketId, "reportId" to ticketId, "redemptionId" to id, "userId" to user.uid,
                    "userEmail" to userEmail, "userName" to visibleUserName,
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
        check(SupportTicketAccess.isAdmin()) { "No tienes permisos de administrador" }
        val request = redemptions.document(id)
        db.runTransaction { tx ->
            // --- FASE 1: TODAS LAS LECTURAS PRIMERO ---
            val snapshot = tx.get(request)
            check(snapshot.getString("status") == "PENDING") { "La solicitud no se encuentra pendiente" }
            val uid = snapshot.getString("userId") ?: error("Solicitud no disponible")
            val profile = db.collection("users").document(uid)
            val account = tx.get(profile)
            val subHistRef = profile.collection("subscription_history").document(id)
            val subHistSnap = tx.get(subHistRef)
            val ticketId = "payment_$id"
            val ticketRef = db.collection("support_reports").document(ticketId)
            val ticketSnap = tx.get(ticketRef)
            val msgRef = profile.collection("messages").document(ticketId)

            // --- FASE 2: PREPARACIÓN DE DATOS ---
            val amount = snapshot.getLong("amount") ?: error("Solicitud no disponible")
            val fee = snapshot.getLong("fee") ?: 0L
            val totalRefund = snapshot.getLong("totalDeducted") ?: (amount + fee)
            val now = System.currentTimeMillis()
            val replyText = if (paid) "Tu solicitud de pago de $amount USDT ha sido procesada y pagada con éxito." else "Tu solicitud de pago de $amount USDT ha sido rechazada y las esencias han sido reembolsadas."
            val replyEntry = SupportMessageEntry(
                id = "${ticketId}_reply_$now",
                senderName = "Administrador",
                senderRole = "SUPPORT",
                text = replyText,
                timestampMillis = now,
                isGreeting = false
            )
            val previousConv = if (ticketSnap.exists()) {
                SupportConversationPolicy.decode(ticketSnap.get("conversation"))
            } else {
                val body = "Solicitud de pago: $amount USDT."
                SupportConversationPolicy.initial(ticketId, account.getString("name") ?: "Usuario", body, now)
            }
            val updatedConv = (previousConv + replyEntry).map { SupportConversationPolicy.encode(it, if (it.senderRole == "USER") uid else "admin") }
            val status = if (paid) "RESUELTO" else "RECHAZADO"
            val updateTicketData = mapOf<String, Any>(
                "status" to status,
                "isCompleted" to true,
                "conversation" to updatedConv,
                "adminReply" to replyText,
                "lastAdminReply" to replyText,
                "repliedBy" to "Administrador",
                "repliedAt" to FieldValue.serverTimestamp(),
                "lastMessageAt" to FieldValue.serverTimestamp(),
                "isRead" to false,
                "userRead" to false,
                "hasNewAdminReply" to true,
                "hasNewReply" to true,
                "staffRead" to true
            )

            // --- FASE 3: TODAS LAS ESCRITURAS ---
            if (!paid) {
                tx.update(profile, "orangeEssence", (account.getLong("orangeEssence") ?: 0L) + totalRefund)
            }
            tx.update(request, mapOf(
                "status" to if (paid) "PAID" else "REJECTED",
                "resolvedAt" to FieldValue.serverTimestamp(),
                "resolvedAtMillis" to now,
                "historyDeleteAtMillis" to now + CASH_HISTORY_RETENTION_MILLIS
            ))

            if (subHistSnap.exists()) {
                tx.update(subHistRef, "status", if (paid) "Completado" else "Rechazado y reembolsado")
            } else {
                val receipt = mapOf(
                    "id" to id,
                    "timestamp" to now,
                    "durationMillis" to 0L,
                    "planName" to "Canje de Esencia Naranja",
                    "status" to if (paid) "Completado" else "Rechazado y reembolsado",
                    "amount" to "-$totalRefund EN",
                    "source" to "CASH_REDEMPTION"
                )
                tx.set(subHistRef, receipt, com.google.firebase.firestore.SetOptions.merge())
            }

            if (ticketSnap.exists()) {
                tx.update(ticketRef, updateTicketData)
            } else {
                tx.set(ticketRef, updateTicketData + mapOf("id" to ticketId, "reportId" to ticketId, "userId" to uid, "title" to "Solicitud de pago USDT", "tag" to "PAGO", "type" to "PAGO", "panel" to "HISTORY", "createdAt" to FieldValue.serverTimestamp()), com.google.firebase.firestore.SetOptions.merge())
            }
            tx.set(msgRef, updateTicketData + mapOf("id" to ticketId, "reportId" to ticketId, "title" to "Solicitud de pago USDT", "content" to replyText, "tag" to "PAGO", "panel" to "HISTORY", "timestamp" to now), com.google.firebase.firestore.SetOptions.merge())
        }.await()
        Unit
    } }

    suspend fun cleanupExpiredResolvedRedemptions(now: Long = System.currentTimeMillis()): Result<Int> = withContext(Dispatchers.IO) { runCatching {
        check(SupportTicketAccess.isAdmin()) { "No tienes permisos de administrador" }
        val snapshot = redemptions.get().await()
        val expired = snapshot.documents.filter { document ->
            val status = document.getString("status").orEmpty()
            if (status !in setOf("PAID", "REJECTED")) return@filter false
            val resolvedAt = document.getLong("resolvedAtMillis") ?: 0L
            val deleteAt = document.getLong("historyDeleteAtMillis")
                ?: resolvedAt.takeIf { it > 0L }?.plus(CASH_HISTORY_RETENTION_MILLIS)
                ?: Long.MAX_VALUE
            now >= deleteAt
        }
        expired.chunked(400).forEach { group ->
            val batch = db.batch()
            group.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
        expired.size
    } }
}
