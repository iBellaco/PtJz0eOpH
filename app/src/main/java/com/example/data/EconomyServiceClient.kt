package com.example.data

import com.example.util.AuthManager
import com.example.util.appTr
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.CancellationException
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Only a private command is submitted here. The trusted worker owns every balance and receipt. */
class EconomyPendingException : IllegalStateException(appTr(
    "Tienes una solicitud pendiente. Se procesará automáticamente; consulta tu bandeja de entrada."))
class EconomyReviewException : IllegalStateException(appTr(
    "Tu solicitud requiere revisión. Contacta con Soporte; no la repitas."))

object EconomyServiceClient {
    suspend fun call(
        action: String,
        fields: Map<String, Any> = emptyMap(),
        id: String = UUID.randomUUID().toString(),
        awaitQueuedResult: Boolean = true
    ): Map<String, Any> {
        val user = AuthManager.getAuth()?.currentUser ?: error(appTr("Inicia sesión"))
        check(!AuthManager.isGuestOrUnauthenticated(user)) { appTr("Inicia sesión") }
        val token = withTimeoutOrNull(10_000L) { user.getIdToken(false).await() }
            ?: throw IllegalStateException(appTr("No se pudo enviar la solicitud. Comprueba tu conexión y vuelve a intentarlo."))
        check(AuthManager.getAuth()?.currentUser?.uid == user.uid) { appTr("Inicia sesión") }
        val payload = fields + mapOf("action" to action, "id" to id)
        val direct = try {
            @Suppress("UNCHECKED_CAST")
            withTimeoutOrNull(15_000L) {
                FirebaseFunctions.getInstance("us-central1").getHttpsCallable("coachEconomy").call(payload).await().data as? Map<String, Any>
            } ?: error("direct_service_timeout")
        } catch (error: FirebaseFunctionsException) {
            if (error.code in setOf(FirebaseFunctionsException.Code.NOT_FOUND, FirebaseFunctionsException.Code.UNAVAILABLE, FirebaseFunctionsException.Code.DEADLINE_EXCEEDED)) null
            else throw IllegalStateException(error.message ?: appTr("No se pudo completar la operación. Vuelve a intentarlo."), error)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) { null }
        direct?.let { result ->
            check(AuthManager.getAuth()?.currentUser?.uid == user.uid) { appTr("Inicia sesión") }
            if (result["ok"] == true) return result
            throw IllegalStateException(appTr("No se pudo completar la operación. Vuelve a intentarlo."))
        }
        // The direct service is optional during migration. Keep the private queue as a
        // durable fallback and continue watching it instead of reporting a false success.
        val ref = FirebaseFirestore.getInstance().collection("economy_requests").document(user.uid)
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
        // Administrative screens must remain usable while the scheduled worker processes
        // a durable request. Callers that opt out still receive the queued/pending state.
        if (!awaitQueuedResult) throw EconomyPendingException()
        val result = withTimeoutOrNull(6 * 60_000L) {
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
