package com.example.data

import com.example.model.PremiumAccessPolicy

/** One grant changes access, deadline and history together without removing an existing role. */
object PremiumGrantPolicy {
    fun apply(account: Map<String, Any>, days: Int, extend: Boolean, now: Long, id: String): Map<String, Any> {
        val role = (account["role"] as? String).orEmpty().ifBlank { "free" }
        val secondary = (account["secondaryRole"] as? String).orEmpty()
        require(!com.example.model.RolePanelAccess.isAdministrator(role, account["admin"] == true)) { "El acceso de administración es vitalicio." }
        require(role != "banned" && secondary != "banned" && account["banned"] != true) { "La cuenta está suspendida." }
        val until = PremiumAccessPolicy.extend(if (extend) PremiumAccessPolicy.deadline(account["premiumUntil"]) else null, days, now)
        val record = mapOf<String, Any>("id" to id, "timestamp" to now, "durationMillis" to days.toLong() * PremiumAccessPolicy.DAY_MILLIS,
            "planName" to "Suscripción Premium regalada", "status" to "Completado", "amount" to "Regalo", "source" to "ADMIN_GIFT")
        val history = (account["subscriptionHistory"] as? List<*>).orEmpty().filterIsInstance<Map<String, Any>>()
        val result = account + mapOf("premiumUntil" to until, "subscriptionPlan" to "ADMIN_GIFT",
            "lastModifiedByAdmin" to now, "subscriptionHistory" to (history.filterNot { it["id"] == id } + record))
        return result
    }
}
