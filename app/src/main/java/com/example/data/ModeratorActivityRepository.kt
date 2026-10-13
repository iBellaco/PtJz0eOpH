package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/** Every moderator account change and support action is committed with its audit event. */
object ModeratorActivityRepository {
    private val rankRoles = setOf("", "esmeralda", "diamante", "maestro", "gran_maestro", "aspirante", "soberano")

    suspend fun changeSecondaryRole(uid: String, role: String) {
        require(role in rankRoles) { "Este rol secundario no se puede asignar desde moderación." }
        changeAccount(uid, "SECONDARY_ROLE", role, mapOf("secondaryRole" to role, "last_secondary_role_update" to System.currentTimeMillis()))
    }

    suspend fun changeVerification(uid: String, verified: Boolean) {
        changeAccount(uid, "VERIFICATION", verified.toString(), mapOf("isVerified" to verified, "verified" to verified))
    }

    /** Profile approval and ticket closure share one commit, so neither can be left half-finished. */
    suspend fun reviewVerification(reportId: String, uid: String, approved: Boolean) {
        val actor = FirebaseAuth.getInstance().currentUser ?: error("Inicia sesión para continuar")
        require(uid != actor.uid && reportId.isNotBlank())
        val db = FirebaseFirestore.getInstance()
        val reportRef = db.collection("support_reports").document(reportId)
        val accountRef = db.collection("users").document(uid)
        val accountEvent = eventReference(db, actor.uid, db.collection("moderator_activity").document().id)
        val statusEvent = eventReference(db, actor.uid, db.collection("moderator_activity").document().id)
        val status = if (approved) "ACCEPTED" else "REJECTED"
        db.runTransaction { transaction ->
            val report = transaction.get(reportRef)
            val account = transaction.get(accountRef)
            check(report.exists() && report.getString("userId") == uid && report.getString("tag") == "VERIFICAR") { "Solicitud no disponible" }
            check(report.getString("status") == "PENDING") { "Esta solicitud ya fue revisada" }
            check((report.get("photos") as? List<*>)?.size == 3) { "Faltan imágenes para la verificación" }
            check(account.exists() && account.getString("role") !in setOf("admin", "moderador", "banned")) { "Perfil no disponible" }
            if (approved && account.getBoolean("isVerified") != true) {
                transaction.update(accountRef, mapOf("isVerified" to true, "verified" to true, "lastModeratorActionId" to accountEvent.id))
                transaction.set(accountEvent, mapOf("actorUid" to actor.uid, "targetUid" to uid,
                    "action" to "VERIFICATION", "value" to "true", "createdAt" to FieldValue.serverTimestamp()))
            }
            val changes = mapOf("status" to status, "isCompleted" to true, "staffRead" to true,
                "updatedAt" to FieldValue.serverTimestamp(), "lastModeratorActionId" to statusEvent.id)
            transaction.update(reportRef, changes)
            transaction.set(db.collection("users").document(uid).collection("messages").document(reportId), changes, SetOptions.merge())
            transaction.set(statusEvent, mapOf("actorUid" to actor.uid, "targetUid" to uid,
                "reportId" to reportId, "action" to "SUPPORT_STATUS", "value" to status,
                "createdAt" to FieldValue.serverTimestamp()))
        }.await()
    }

    private suspend fun changeAccount(uid: String, action: String, value: String, changes: Map<String, Any>) {
        val actor = FirebaseAuth.getInstance().currentUser ?: error("Inicia sesión para continuar")
        require(uid != actor.uid) { "No puedes modificar tu propia cuenta." }
        val db = FirebaseFirestore.getInstance()
        val target = db.collection("users").document(uid)
        val event = db.collection("moderator_activity").document(actor.uid).collection("events").document()
        db.runTransaction { transaction ->
            val account = transaction.get(target)
            check(account.exists() && account.getString("role") !in setOf("admin", "moderador", "banned")) {
                "Esta cuenta no está disponible para moderación."
            }
            if (action == "SECONDARY_ROLE" && account.getString("secondaryRole").orEmpty() == value) return@runTransaction
            if (action == "VERIFICATION" && account.getBoolean("isVerified") == (value == "true")) return@runTransaction
            transaction.update(target, changes + ("lastModeratorActionId" to event.id))
            transaction.set(event, mapOf(
                "actorUid" to actor.uid, "targetUid" to uid, "action" to action,
                "value" to value, "createdAt" to FieldValue.serverTimestamp()
            ))
        }.await()
    }

    fun eventReference(db: FirebaseFirestore, actorUid: String, eventId: String) =
        db.collection("moderator_activity").document(actorUid).collection("events").document(eventId)
}
