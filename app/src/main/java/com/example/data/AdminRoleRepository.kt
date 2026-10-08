package com.example.data

import com.example.model.PremiumAccessPolicy
import com.example.util.AuthManager
import com.example.util.appTr
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

/** Role assignment is an authenticated profile transaction, independent of scheduled purchases. */
object AdminRolePolicy {
    private val roles = setOf("free", "premium", "banned", "moderador", "streamer", "patrocinador",
        "creador", "creador_lvl2", "creador_lvl3", "creador_lvl4", "creador_lvl5")
    fun patch(account: Map<String, Any>, role: String, now: Long): Map<String, Any> {
        require(role in roles) { "Rol no válido" }
        return buildMap {
            put("role", role)
            put("banned", role == "banned")
            if (role == "free") { put("premiumUntil", 0L); put("subscriptionPlan", "FREE") }
            if (role == "premium" && (PremiumAccessPolicy.deadline(account["premiumUntil"]) ?: 0L) <= now)
                put("premiumUntil", now + 30L * 86400000L)
            if (role == "banned") put("sessionToken", "")
        }
    }
}

object AdminRoleRepository {
    suspend fun change(uid: String, role: String): Map<String, Any> {
        val user = AuthManager.getAuth()?.currentUser ?: error(appTr("Inicia sesión"))
        val token = withTimeoutOrNull(10_000L) { user.getIdToken(true).await() }
        check(token?.claims?.get("admin") == true && !AuthManager.isGuestOrUnauthenticated(user)) {
            appTr("No tienes permisos para esta operación")
        }
        try {
            val result = withTimeoutOrNull(15_000L) {
                val db = FirebaseFirestore.getInstance()
                db.runTransaction { tx ->
                    val ref = db.collection("users").document(uid)
                    val snapshot = tx.get(ref)
                    val account = snapshot.data ?: error(appTr("Usuario no disponible"))
                    val patch = AdminRolePolicy.patch(account, role, System.currentTimeMillis())
                    tx.update(ref, patch + mapOf("last_role_update" to FieldValue.serverTimestamp(),
                        "lastRoleChangedBy" to user.uid, "bannedTimestamp" to if (role == "banned") FieldValue.serverTimestamp() else 0L))
                    mapOf("account" to (account + patch + ("uid" to uid)), "ok" to true)
                }.await()
            }
            check(AuthManager.getAuth()?.currentUser?.uid == user.uid) { appTr("Inicia sesión") }
            return result ?: error(appTr("No se pudo confirmar el cambio de rol. Revisa la cuenta y la conexión antes de intentarlo de nuevo."))
        } catch (error: FirebaseFirestoreException) {
            throw IllegalStateException(appTr(if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
                "No tienes permisos para esta operación" else "No se pudo confirmar el cambio de rol. Revisa la cuenta y la conexión antes de intentarlo de nuevo."), error)
        }
    }
}
