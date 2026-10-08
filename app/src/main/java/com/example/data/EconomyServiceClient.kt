package com.example.data

import com.example.util.AuthManager
import com.example.util.appTr
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Only a private command is submitted here. The trusted worker owns every balance and receipt. */
object EconomyServiceClient {
    suspend fun call(action: String, fields: Map<String, Any> = emptyMap(), id: String = UUID.randomUUID().toString()): Map<String, Any> {
        val user = AuthManager.getAuth()?.currentUser
        check(!AuthManager.isGuestOrUnauthenticated(user)) { appTr("Inicia sesión") }
        val token = user!!.getIdToken(false).await()
        check(AuthManager.getAuth()?.currentUser?.uid == user.uid) { appTr("Inicia sesión") }
        val ref = FirebaseFirestore.getInstance().collection("economy_requests").document(user.uid)
        val payload = fields + mapOf("action" to action, "id" to id)
        val operationId = try {
            FirebaseFirestore.getInstance().runTransaction { tx ->
                val current = tx.get(ref)
                if (current.getString("status") in setOf("PENDING", "PROCESSING")) {
                    val pending = current.get("payload") as? Map<*, *> ?: emptyMap<Any, Any>()
                    check(canonical(pending.filterKeys { it != "id" }) == canonical(payload.filterKeys { it != "id" })) {
                        appTr("Tienes una solicitud pendiente. Espera a que termine antes de enviar otra.")
                    }
                    current.getString("operationId") ?: error(appTr("No se pudo completar la operación. Vuelve a intentarlo."))
                } else {
                    tx.set(ref, mapOf("userId" to user.uid, "operationId" to id, "payload" to payload,
                        "status" to "PENDING", "schema" to 2, "createdAt" to FieldValue.serverTimestamp(),
                        "authTime" to ((token.claims["auth_time"] as? Number)?.toLong() ?: 0L)))
                    id
                }
            }.await()
        } catch (error: FirebaseFirestoreException) {
            throw IllegalStateException(appTr(if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
                "No tienes permisos para esta operación" else "No se pudo enviar la solicitud. Comprueba tu conexión y vuelve a intentarlo."), error)
        }
        val result = withTimeoutOrNull(45_000L) {
            suspendCancellableCoroutine<Map<String, Any>> { continuation ->
                var listener: ListenerRegistration? = null
                listener = FirebaseFirestore.getInstance().collection("economy_results")
                    .document("${user.uid}~$operationId").addSnapshotListener { snapshot, error ->
                    if (!continuation.isActive) return@addSnapshotListener
                    if (error != null) {
                        continuation.resumeWithException(IllegalStateException(appTr("Tu solicitud sigue pendiente. Se actualizará al procesarse; no la repitas."), error))
                        listener?.remove()
                    } else if (snapshot?.getString("operationId") == operationId) {
                        when (snapshot.getString("status")) {
                            "COMPLETED" -> {
                                @Suppress("UNCHECKED_CAST")
                                val completed = snapshot.get("result") as? Map<String, Any>
                                if (completed?.get("ok") == true) continuation.resume(completed)
                                else continuation.resumeWithException(IllegalStateException(appTr("No se pudo completar la operación. Vuelve a intentarlo.")))
                                listener?.remove()
                            }
                            "FAILED" -> {
                                continuation.resumeWithException(IllegalStateException(appTr(snapshot.getString("error.message")
                                    ?: "No se pudo completar la operación. Vuelve a intentarlo.")))
                                listener?.remove()
                            }
                        }
                    }
                }
                continuation.invokeOnCancellation { listener?.remove() }
                if (!continuation.isActive) listener?.remove()
            }
        }
        check(AuthManager.getAuth()?.currentUser?.uid == user.uid) { appTr("Inicia sesión") }
        return result ?: throw IllegalStateException(appTr("Tu solicitud sigue pendiente. Se actualizará al procesarse; no la repitas."))
    }

    private fun canonical(value: Any?): Any? = when (value) {
        is Map<*, *> -> value.entries.associate { it.key.toString() to canonical(it.value) }.toSortedMap()
        is List<*> -> value.map(::canonical)
        is Number -> value.toDouble()
        else -> value
    }
}
