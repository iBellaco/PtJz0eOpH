package com.example.data

import com.example.util.AuthManager
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object AccountDeletionRepository {
    private val db get() = FirebaseFirestore.getInstance()
    private fun currentUser() = AuthManager.getAuth()?.currentUser
        ?.takeUnless { AuthManager.isGuestOrUnauthenticated(it) }
        ?: error("Inicia sesión para solicitar la eliminación.")

    suspend fun request(password: String): Long = withContext(Dispatchers.IO) {
        val user = currentUser()
        val email = user.email ?: error("No se pudo verificar tu cuenta.")
        require(password.isNotBlank()) { "Ingresa tu contraseña para confirmar." }
        user.reauthenticate(EmailAuthProvider.getCredential(email, password)).await()
        user.getIdToken(true).await()
        val request = db.collection("account_deletions").document(user.uid)
        val profile = db.collection("users").document(user.uid)
        val requestId = java.util.UUID.randomUUID().toString()
        db.runTransaction { tx ->
            val service = tx.get(db.collection("system_config").document("account_deletion_service"))
            val previous = tx.get(request)
            val account = tx.get(profile)
            check(service.getBoolean("enabled") == true) { "El servicio de eliminación no está disponible. Contacta a soporte." }
            check(!previous.exists() || previous.getString("status") == "CANCELLED") {
                "Ya existe una solicitud de eliminación para esta cuenta."
            }
            check(account.exists()) { "No se pudo verificar tu cuenta." }
            tx.set(request, mapOf("userId" to user.uid, "requestId" to requestId,
                "status" to "PENDING", "graceDays" to AccountDeletionPolicy.GRACE_DAYS,
                "requestedAt" to FieldValue.serverTimestamp()))
        }.await()
        val timestamp = request.get(Source.SERVER).await().getTimestamp("requestedAt")
            ?: error("No se pudo comprobar la fecha de la solicitud. Contacta a soporte antes de repetirla.")
        AccountDeletionPolicy.deadline(timestamp.toDate().time)
    }

    /** Call only after an explicit sign-in; token refresh and app restoration do not cancel. */
    suspend fun cancelAfterSignIn(): Boolean = withContext(Dispatchers.IO) {
        val user = currentUser()
        user.getIdToken(true).await()
        val request = db.collection("account_deletions").document(user.uid)
        db.runTransaction { tx ->
            val snapshot = tx.get(request)
            when (snapshot.getString("status")) {
                null -> false
                "CANCELLED" -> true
                "PENDING" -> {
                    tx.update(request, mapOf("status" to "CANCELLED", "cancelledAt" to FieldValue.serverTimestamp()))
                    true
                }
                else -> error("La eliminación definitiva ya comenzó. Contacta a soporte.")
            }
        }.await()
    }
}
