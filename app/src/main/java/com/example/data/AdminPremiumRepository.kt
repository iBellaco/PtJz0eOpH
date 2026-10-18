package com.example.data

import com.example.model.PremiumAccessPolicy
import com.example.util.AuthManager
import com.example.util.appTr
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID

/** Signed administrator writes are bounded by administrativePremiumChange on the server. */
object AdminPremiumPolicy {
    fun patch(account: Map<String, Any>, days: Int, extend: Boolean, now: Long, id: String): Map<String, Any> {
        require(days in 0..3650) { "Operación no válida" }
        require(account["admin"] != true && account["role"] !in setOf("admin", "administrador")) { "El acceso de administración es vitalicio." }
        require(account["banned"] != true && account["role"] != "banned" && account["secondaryRole"] != "banned") { "La cuenta está suspendida." }
        val remove = days == 0
        val until = if (remove) 0L else PremiumAccessPolicy.extend(if (extend) PremiumAccessPolicy.deadline(account["premiumUntil"]) else null, days, now)
        val record = mapOf<String, Any>("id" to id, "timestamp" to now, "durationMillis" to days.toLong() * PremiumAccessPolicy.DAY_MILLIS,
            "planName" to if (remove) "Suscripción Premium retirada" else "Suscripción Premium regalada",
            "status" to "Completado", "amount" to if (remove) "0" else "Regalo", "source" to if (remove) "ADMIN_REVOCATION" else "ADMIN_GIFT")
        val history = (account["subscriptionHistory"] as? List<*>).orEmpty().filterIsInstance<Map<String, Any>>()
        return buildMap {
            put("premiumUntil", until); put("subscriptionPlan", if (remove) "FREE" else "ADMIN_GIFT")
            put("subscriptionHistory", history + record)
            put("lastPremiumDays", days); put("lastPremiumExtend", extend); put("lastPremiumOperationId", id)
            if (!remove && account["role"] == "free") {
                put("role", "premium"); put("banned", false); put("bannedTimestamp", 0L)
            }
            if (remove && account["role"] == "premium") {
                put("role", "free"); put("banned", false); put("bannedTimestamp", 0L)
            }
        }
    }
}

object AdminPremiumRepository {
    suspend fun change(uid: String, days: Int, extend: Boolean = true): Map<String, Any> {
        val actor = AuthManager.getAuth()?.currentUser ?: error(appTr("Inicia sesión"))
        val token = withTimeoutOrNull(10_000L) { actor.getIdToken(true).await() }
        check(token?.claims?.get("admin") == true && !AuthManager.isGuestOrUnauthenticated(actor)) { appTr("No tienes permisos para esta operación") }
        val key = "${actor.uid}:premium:$uid:$days:$extend"
        val id = AdminDirectOperationIds.reserve(key, UUID.randomUUID().toString())
        val result = withTimeoutOrNull(15_000L) {
            val db = FirebaseFirestore.getInstance()
            db.runTransaction { tx ->
                val ref = db.collection("users").document(uid)
                val account = tx.get(ref).data ?: error(appTr("Usuario no disponible"))
                val history = (account["subscriptionHistory"] as? List<*>).orEmpty().filterIsInstance<Map<String, Any>>()
                if (history.any { it["id"] == id }) return@runTransaction account + ("uid" to uid)
                val patch = AdminPremiumPolicy.patch(account, days, extend, System.currentTimeMillis(), id)
                val stamps = mutableMapOf<String, Any>("lastModifiedByAdmin" to FieldValue.serverTimestamp(),
                    "lastPremiumChangedAt" to FieldValue.serverTimestamp(), "lastPremiumChangedBy" to actor.uid)
                if (patch.containsKey("role")) stamps["last_role_update"] = FieldValue.serverTimestamp()
                tx.update(ref, patch + stamps)
                account + patch + ("uid" to uid)
            }.await()
        } ?: error(appTr("No se pudo completar la operación. Vuelve a intentarlo."))
        check(AuthManager.getAuth()?.currentUser?.uid == actor.uid) { appTr("Inicia sesión") }
        AdminDirectOperationIds.complete(key)
        return result
    }
}
