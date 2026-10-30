package com.example.data

import com.example.util.AuthManager
import com.example.util.appTr
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
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
class EconomyUnconfirmedException : IllegalStateException(com.google.firebase.FirebaseApp.getInstance()
    .applicationContext.getString(com.example.R.string.operation_unconfirmed))

object EconomyServiceClient {
    private inline fun <T> withOperationStorage(block: () -> T): T = try { block() }
    catch (error: IllegalStateException) {
        throw IllegalStateException(com.google.firebase.FirebaseApp.getInstance().applicationContext
            .getString(com.example.R.string.operation_storage_failed), error)
    }

    private val operations = AccountOperationIds

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
        val admin = token.claims["admin"] == true
        val commandKey = EconomyRequestPolicy.operationKey(user.uid, action,
            if (action == "SPONSOR") fields + ("publicationId" to id) else fields)
        var directId = withOperationStorage { operations.reserve(commandKey, id) }
        val queued = try {
            val document = if (action == "SPONSOR") "${user.uid}~$directId" else user.uid
            withTimeoutOrNull(5_000L) { FirebaseFirestore.getInstance().collection("economy_requests")
                .document(document).get(Source.SERVER).await() }
        } catch (error: CancellationException) { throw error }
        catch (_: Exception) { null }
        val alreadyQueued = queued?.getString("status") in setOf("PENDING", "PROCESSING", "REVIEW")
        if (alreadyQueued) {
            val pending = queued?.get("payload") as? Map<*, *> ?: emptyMap<Any, Any>()
            if (!EconomyRequestPolicy.sameCommand(pending, fields + ("action" to action))) throw EconomyPendingException()
            directId = queued?.getString("operationId")?.takeIf { it.isNotBlank() } ?: throw EconomyReviewException()
            withOperationStorage { operations.adopt(commandKey, directId) }
            if (queued?.getString("status") == "REVIEW") throw EconomyReviewException()
        }
        val payload = fields + mapOf("action" to action, "id" to directId)
        // A previous process may have died after the server applied the command.
        // Read the receipt before resending; even if offline, reuse exactly the same ID.
        val previous = try {
            withTimeoutOrNull(5_000L) { FirebaseFirestore.getInstance().collection("economy_results")
                .document("${user.uid}~$directId").get(Source.SERVER).await() }
        } catch (error: CancellationException) { throw error }
        catch (_: Exception) { null }
        check(AuthManager.getAuth()?.currentUser?.uid == user.uid) { appTr("Inicia sesión") }
        if (previous?.getString("operationId") == directId) {
            when (previous.getString("status")) {
                "COMPLETED" -> {
                    @Suppress("UNCHECKED_CAST")
                    val result = previous.get("result") as? Map<String, Any>
                    if (result?.get("ok") == true) {
                        operations.complete(commandKey)
                        return result
                    }
                    throw EconomyReviewException()
                }
                "FAILED" -> {
                    operations.complete(commandKey)
                    throw IllegalStateException(appTr(previous.getString("error.message")
                        ?: "No se pudo completar la operación. Vuelve a intentarlo."))
                }
            }
        }
        val direct = if (alreadyQueued) null else try {
            @Suppress("UNCHECKED_CAST")
            withTimeoutOrNull(15_000L) {
                FirebaseFunctions.getInstance("us-central1").getHttpsCallable("coachEconomy").call(payload).await().data as? Map<String, Any>
            } ?: error("direct_service_timeout")
        } catch (error: FirebaseFunctionsException) {
            if (error.code in setOf(FirebaseFunctionsException.Code.NOT_FOUND, FirebaseFunctionsException.Code.UNAVAILABLE,
                    FirebaseFunctionsException.Code.DEADLINE_EXCEEDED, FirebaseFunctionsException.Code.INTERNAL,
                    FirebaseFunctionsException.Code.UNKNOWN, FirebaseFunctionsException.Code.CANCELLED)) null
            else {
                // Do not discard the retry identity on a transport or permission failure.
                throw IllegalStateException(functionErrorMessage(error, action), error)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) { null }
        direct?.let { result ->
            check(AuthManager.getAuth()?.currentUser?.uid == user.uid) { appTr("Inicia sesión") }
            if (result["ok"] == true) {
                operations.complete(commandKey)
                return result
            }
            throw IllegalStateException(appTr("No se pudo completar la operación. Vuelve a intentarlo."))
        }
        // Personal advertisements may use the trusted queue even for staff accounts.
        // The trusted worker verifies privileged commands again before applying them.
        check(EconomyRequestPolicy.canQueue(action, admin)) {
            appTr("No se pudo completar la operación. Vuelve a intentarlo.")
        }
        // The direct service is optional during migration. Keep the private queue as a
        // durable fallback and continue watching it instead of reporting a false success.
        // Sponsorships are independent requests. Do not reuse the legacy
        // per-user document, because one pending ad must not block later ads.
        val requestDocument = if (action == "SPONSOR") "${user.uid}~$directId" else user.uid
        val ref = FirebaseFirestore.getInstance().collection("economy_requests").document(requestDocument)
        val submission = try {
            withTimeoutOrNull(15_000L) {
                FirebaseFirestore.getInstance().runTransaction { tx ->
                    val current = tx.get(ref)
                    if (current.getString("status") in setOf("PENDING", "PROCESSING", "REVIEW")) {
                        val pending = current.get("payload") as? Map<*, *> ?: emptyMap<Any, Any>()
                        val same = EconomyRequestPolicy.sameCommand(pending, payload)
                        Triple(current.getString("operationId") ?: "", !same, current.getString("status") == "REVIEW")
                    } else {
                        tx.set(ref, mapOf("userId" to user.uid, "operationId" to directId, "payload" to payload,
                            "status" to "PENDING", "schema" to 2, "createdAt" to FieldValue.serverTimestamp(),
                            "authTime" to ((token.claims["auth_time"] as? Number)?.toLong() ?: 0L)))
                        Triple(directId, false, false)
                    }
                }.await()
            } ?: throw EconomyUnconfirmedException()
        } catch (error: FirebaseFirestoreException) {
            if (error.code in setOf(FirebaseFirestoreException.Code.UNAVAILABLE, FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
                    FirebaseFirestoreException.Code.ABORTED, FirebaseFirestoreException.Code.UNKNOWN)) {
                throw EconomyUnconfirmedException().apply { initCause(error) }
            }
            throw IllegalStateException(appTr(if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
                "No tienes permisos para esta operación" else "No se pudo enviar la solicitud. Comprueba tu conexión y vuelve a intentarlo."), error)
        }
        if (submission.third) throw EconomyReviewException()
        if (submission.second) throw EconomyPendingException()
        val operationId = submission.first
        check(operationId.isNotBlank()) { appTr("No se pudo completar la operación. Vuelve a intentarlo.") }
        withOperationStorage { operations.adopt(commandKey, operationId) }
        // Administrative screens must remain usable while the scheduled worker processes
        // a durable request. Callers that opt out still receive the queued/pending state.
        if (!awaitQueuedResult) throw EconomyPendingException()
        val result = withTimeoutOrNull(8_000L) {
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
                                operations.complete(commandKey)
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
        if (result == null) throw EconomyPendingException()
        operations.complete(commandKey)
        return result
    }

    private fun functionErrorMessage(error: FirebaseFunctionsException, action: String): String {
        val detail = error.message?.trim().orEmpty()
        if (detail.isNotBlank() && !detail.equals(error.code.name, ignoreCase = true)) return appTr(detail)
        if (action != "SPONSOR") return appTr(if (error.code == FirebaseFunctionsException.Code.PERMISSION_DENIED)
            "No tienes permisos para esta operación" else "No se pudo completar la operación. Vuelve a intentarlo.")
        return when (error.code) {
            FirebaseFunctionsException.Code.PERMISSION_DENIED -> appTr("Tu cuenta no tiene permiso para publicar anuncios.")
            FirebaseFunctionsException.Code.FAILED_PRECONDITION -> appTr("La publicación cambió o no cumple las condiciones actuales. Revisa el costo y los datos del anuncio.")
            FirebaseFunctionsException.Code.INVALID_ARGUMENT -> appTr("El anuncio contiene datos inválidos. Revisa título, descripción, duración y enlaces.")
            FirebaseFunctionsException.Code.ALREADY_EXISTS -> appTr("Este anuncio ya fue enviado. Revisa tus anuncios pendientes antes de repetirlo.")
            FirebaseFunctionsException.Code.RESOURCE_EXHAUSTED -> appTr("No hay suficientes esencias azules para publicar este anuncio.")
            else -> appTr("No se pudo completar la operación. Código: ${error.code.name}.")
        }
    }

}
