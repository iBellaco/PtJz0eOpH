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
class EconomyPendingException : IllegalStateException(appTr(
    "Tienes una solicitud en espera. Puedes verla en Usuario → Solicitudes o en Ver solicitud."))
class EconomyReviewException : IllegalStateException(appTr(
    "Tu solicitud requiere revisión. Consulta Usuario → Solicitudes y contacta con Soporte; no la repitas."))

object EconomyServiceClient {
    suspend fun call(action: String, fields: Map<String, Any> = emptyMap(), id: String = UUID.randomUUID().toString()): Map<String, Any> {
        val user = AuthManager.getAuth()?.currentUser ?: error(appTr("Inicia sesión"))
        check(!AuthManager.isGuestOrUnauthenticated(user)) { appTr("Inicia sesión") }
        val token = withTimeoutOrNull(10_000L) { user.getIdToken(false).await() }
            ?: throw IllegalStateException(appTr("No se pudo enviar la solicitud. Comprueba tu conexión y vuelve a intentarlo."))
        check(AuthManager.getAuth()?.currentUser?.uid == user.uid) { appTr("Inicia sesión") }
        val ref = FirebaseFirestore.getInstance().collection("economy_requests").document(user.uid)
        val payload = fields + mapOf("action" to action, "id" to id)
        val submission = try {
            withTimeoutOrNull(15_000L) {
                FirebaseFirestore.getInstance().runTransaction { tx ->
                    val current = tx.get(ref)
                    if (current.getString("status") in setOf("PENDING", "PROCESSING", "REVIEW")) {
                        val pending = current.get("payload") as? Map<*, *> ?: emptyMap<Any, Any>()
                        val same = EconomyRequestPolicy.sameCommand(pending, payload)
                        Triple(current.getString("operationId") ?: "", !same, current.getString("status") == "REVIEW")
                    } else {
                        tx.set(ref, mapOf("userId" to user.uid, "operationId" to id, "payload" to payload,
                            "status" to "PENDING", "schema" to 2, "createdAt" to FieldValue.serverTimestamp(),
                            "authTime" to ((token.claims["auth_time"] as? Number)?.toLong() ?: 0L)))
                        Triple(id, false, false)
                    }
                }.await()
            } ?: throw EconomyPendingException()
        } catch (error: FirebaseFirestoreException) {
            throw IllegalStateException(appTr(if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
                "No tienes permisos para esta operación" else "No se pudo enviar la solicitud. Comprueba tu conexión y vuelve a intentarlo."), error)
        }
        if (submission.third) throw EconomyReviewException()
        if (submission.second) throw EconomyPendingException()
        val operationId = submission.first
        check(operationId.isNotBlank()) { appTr("No se pudo completar la operación. Vuelve a intentarlo.") }
        val result = withTimeoutOrNull(5_000L) {
            suspendCancellableCoroutine<Map<String, Any>> { continuation ->
                var listener: ListenerRegistration? = null
                listener = FirebaseFirestore.getInstance().collection("economy_results")
                    .document("${user.uid}~$operationId").addSnapshotListener { snapshot, error ->
                    if (!continuation.isActive) return@addSnapshotListener
                    if (error != null) {
                        continuation.resumeWithException(EconomyPendingException().apply { initCause(error) })
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
        return result ?: throw EconomyPendingException()
    }

}
