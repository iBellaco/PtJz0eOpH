package com.example.data

import com.google.firebase.firestore.FirebaseFirestore

object EssenceEconomyRepository {
    suspend fun purchase(id: String, plan: EssencePremiumPlan, currency: EssenceCurrency): Result<Unit> = runCatching {
        EconomyServiceClient.call("PURCHASE", mapOf("plan" to plan.name, "currency" to currency.name,
            "language" to com.example.util.AppLanguage.current.value), id)
        Unit
    }

}
