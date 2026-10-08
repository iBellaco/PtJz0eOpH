package com.example.data

import com.google.firebase.firestore.FirebaseFirestore

object EssenceEconomyRepository {
    val redemptions get() = FirebaseFirestore.getInstance().collection("cash_redemptions")
    const val CASH_HISTORY_RETENTION_MILLIS = 14L * 24 * 60 * 60 * 1000L

    suspend fun purchase(id: String, plan: EssencePremiumPlan, currency: EssenceCurrency): Result<Unit> = runCatching {
        EconomyServiceClient.call("PURCHASE", mapOf("plan" to plan.name, "currency" to currency.name), id)
        Unit
    }

    suspend fun redeem(id: String, amount: Long, network: UsdtNetwork?, wallet: String, binanceEmail: String = ""): Result<Unit> = runCatching {
        val email = binanceEmail.trim().lowercase(java.util.Locale.ROOT)
        EconomyServiceClient.call("REDEEM", mapOf("amount" to amount,
            "network" to if (email.isNotBlank()) "" else network!!.name,
            "wallet" to if (email.isNotBlank()) "" else wallet.trim(), "binanceEmail" to email,
            "expectedFee" to if (email.isNotBlank()) 0L else BinanceCommissionManager.getFee(network!!)), id)
        Unit
    }

    suspend fun resolve(id: String, paid: Boolean): Result<Unit> = runCatching {
        EconomyServiceClient.call("RESOLVE", mapOf("redemptionId" to id, "paid" to paid))
        Unit
    }

    suspend fun cleanupExpiredResolvedRedemptions(now: Long = System.currentTimeMillis()): Result<Int> = runCatching {
        // Retention uses the server clock.
        (EconomyServiceClient.call("CLEANUP")["count"] as Number).toInt()
    }
}
