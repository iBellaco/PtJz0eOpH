package com.example.data

import com.example.util.AuthManager
import com.example.util.appTr
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.util.concurrent.TimeUnit

/** The server owns prices, authorization, time, balance and receipts. No local debit fallback. */
object EconomyServiceClient {
    suspend fun call(action: String, fields: Map<String, Any> = emptyMap(), id: String = UUID.randomUUID().toString()): Map<String, Any> {
        check(!AuthManager.isGuestOrUnauthenticated(AuthManager.getAuth()?.currentUser)) { appTr("Inicia sesión") }
        val callable = FirebaseFunctions.getInstance("us-central1").getHttpsCallable("coachEconomy")
        callable.setTimeout(60, TimeUnit.SECONDS)
        val payload = fields + mapOf("action" to action, "id" to id)
        val response = try { callable.call(payload).await() }
        catch (first: FirebaseFunctionsException) {
            if (first.code !in setOf(FirebaseFunctionsException.Code.UNAVAILABLE, FirebaseFunctionsException.Code.DEADLINE_EXCEEDED)) {
                throw IllegalStateException(appTr(message(first)), first)
            }
            // A lost response reuses the original ID and cannot charge twice.
            try { callable.call(payload).await() }
            catch (retry: FirebaseFunctionsException) { throw IllegalStateException(appTr(message(retry)), retry) }
        }
        @Suppress("UNCHECKED_CAST")
        val result = response.data as? Map<String, Any> ?: error(appTr("No se pudo completar la operación. Vuelve a intentarlo."))
        check(result["ok"] == true) { appTr("No se pudo completar la operación. Vuelve a intentarlo.") }
        return result
    }

    private fun message(error: FirebaseFunctionsException): String = when (error.code) {
        FirebaseFunctionsException.Code.NOT_FOUND, FirebaseFunctionsException.Code.UNAVAILABLE -> "El servicio de canjes no está disponible. Vuelve a intentarlo más tarde."
        FirebaseFunctionsException.Code.UNAUTHENTICATED -> "Inicia sesión"
        FirebaseFunctionsException.Code.PERMISSION_DENIED -> "No tienes permisos para esta operación"
        else -> error.message?.takeIf { it in setOf("Esencias insuficientes", "Cuenta suspendida", "Tu acceso premium es vitalicio", "La solicitud no se encuentra pendiente", "La cuenta tiene una solicitud de eliminación", "La comisión ha cambiado. Revisa el total antes de confirmar.", "El costo ha cambiado. Revisa el total antes de confirmar.") }
            ?: "No se pudo completar la operación. Vuelve a intentarlo."
    }
}
