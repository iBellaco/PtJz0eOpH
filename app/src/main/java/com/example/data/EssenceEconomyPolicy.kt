package com.example.data

import com.example.model.PremiumAccessPolicy

enum class EssenceCurrency(val field: String, val abbreviation: String) {
    BLUE("blueEssence", "EA"), ORANGE("orangeEssence", "EN")
}

enum class EssencePremiumPlan(val days: Int, val blueCost: Long, val orangeCost: Long) {
    MONTHLY(30, 100, 9), ANNUAL(365, 1100, 95);
    fun cost(currency: EssenceCurrency) = if (currency == EssenceCurrency.BLUE) blueCost else orangeCost
}

object EssenceEconomyPolicy {
    val redemptionAmounts = listOf(10L, 25L, 50L)
    fun balance(account: Map<String, Any>, currency: EssenceCurrency) =
        (account[currency.field] as? Number)?.toLong() ?: 0L
    fun purchase(account: Map<String, Any>, plan: EssencePremiumPlan, currency: EssenceCurrency, now: Long): Pair<Long, Long> {
        check(account["banned"] != true && account["role"] != "banned" && account["secondaryRole"] != "banned") { "Cuenta suspendida" }
        check(!PremiumAccessPolicy.isLifetime(account["role"] as? String ?: "", account["secondaryRole"] as? String ?: "")) { "Tu acceso premium es vitalicio" }
        val remaining = balance(account, currency) - plan.cost(currency)
        check(remaining >= 0) { "Esencias insuficientes" }
        return remaining to PremiumAccessPolicy.extend(PremiumAccessPolicy.deadline(account["premiumUntil"]), plan.days, now)
    }
    fun redeem(account: Map<String, Any>, amount: Long): Long {
        check(account["banned"] != true && account["role"] != "banned" && account["secondaryRole"] != "banned") { "Cuenta suspendida" }
        require(amount in redemptionAmounts)
        return (balance(account, EssenceCurrency.ORANGE) - amount).also { check(it >= 0) { "Esencias insuficientes" } }
    }
}
