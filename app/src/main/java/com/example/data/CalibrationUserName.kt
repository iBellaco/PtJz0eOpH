package com.example.data

import com.example.util.SubscriptionManager
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

/** Resolves the public Coach username without deriving identity from an email address. */
object CalibrationUserName {
    internal fun select(candidates: Iterable<String?>): String = candidates
        .asSequence()
        .mapNotNull { it?.trim()?.takeIf(String::isNotBlank) }
        .firstOrNull { !it.contains('@') && it !in setOf("Cuenta Principal", "Invocador") }
        ?: "Usuario"

    suspend fun resolve(user: FirebaseUser): String {
        val profile = runCatching {
            withTimeoutOrNull(5_000L) {
                FirebaseFirestore.getInstance().collection("users").document(user.uid).get().await()
            }
        }.getOrNull()
        return select(listOf(
            profile?.getString("username"),
            profile?.getString("displayName"),
            profile?.getString("name"),
            SubscriptionManager.userName.value,
            user.displayName
        ))
    }
}
