package com.example.data


/** The receipt and optional notification share the same commit as the balance. */
object AdminEssenceAdjustment {
    fun delta(balance: Long, amount: Long, addition: Boolean): Long {
        require(amount > 0 && balance >= 0)
        return if (addition) { Math.addExact(balance, amount); amount } else -minOf(balance, amount)
    }

    suspend fun apply(uid: String, amount: Long, currency: String, addition: Boolean,
        notify: Boolean, title: String, customBody: String?): Map<String, Any> {
        @Suppress("UNCHECKED_CAST")
        return EconomyServiceClient.call("ADJUST", mapOf("uid" to uid, "amount" to amount,
            "currency" to currency, "addition" to addition, "notify" to notify,
            "title" to title, "customBody" to customBody.orEmpty()))["account"] as Map<String, Any>
    }
}
