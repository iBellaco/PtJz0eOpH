package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.data.AvatarCatalog
import com.example.model.AppUserRole
import com.example.ui.theme.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.UUID
import kotlinx.coroutines.launch

internal suspend fun applyPremiumDuration(uid: String, days: Int, extendExisting: Boolean = true): Map<String, Any> {
    @Suppress("UNCHECKED_CAST")
    return com.example.data.EconomyServiceClient.call("PREMIUM_GRANT", mapOf("uid" to uid,
        "days" to days, "extend" to extendExisting))["account"] as Map<String, Any>
}

internal suspend fun removePremiumFromUser(uid: String): Map<String, Any> {
    @Suppress("UNCHECKED_CAST")
    return com.example.data.EconomyServiceClient.call(
        "PREMIUM_REMOVE", mapOf("uid" to uid), awaitQueuedResult = false
    )["account"] as Map<String, Any>
}

internal fun updateUserVerification(
    context: Context,
    uid: String,
    isVerified: Boolean,
    onSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val updatePayload = hashMapOf<String, Any>(
        "isVerified" to isVerified,
        "verified" to isVerified,
        "lastModifiedByAdmin" to System.currentTimeMillis()
    )

    db.collection("users").document(uid)
        .set(updatePayload, SetOptions.merge())
        .addOnSuccessListener {
            val msg = if (isVerified) "Verificación otorgada exitosamente" else "Verificación revocada"
            Toast.makeText(context, com.example.util.appTr(msg), Toast.LENGTH_SHORT).show()
            onSuccess()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, com.example.util.appTr("Error al actualizar verificación: ${e.message}"), Toast.LENGTH_LONG).show()
        }
}

internal fun resetUserHardwareSlots(
    context: Context,
    uid: String,
    onSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val updatePayload = hashMapOf<String, Any>(
        "registeredDevices" to emptyList<String>(),
        "sessionToken" to "",
        "slotsResetTimestamp" to System.currentTimeMillis()
    )

    db.collection("users").document(uid)
        .set(updatePayload, SetOptions.merge())
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, com.example.util.appTr("Error al reiniciar slots: ${e.message}"), Toast.LENGTH_LONG).show()
        }
}

internal fun giftSingleAvatarToUser(
    context: Context,
    uid: String,
    avatarId: String,
    onSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(uid)

    userRef.update("unlockedAvatars", FieldValue.arrayUnion(avatarId))
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->
            // Si el campo no existía aún, usamos set con merge
            val updateData = hashMapOf<String, Any>(
                "unlockedAvatars" to listOf("default_poro", avatarId)
            )
            userRef.set(updateData, SetOptions.merge())
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { err ->
                    Toast.makeText(context, com.example.util.appTr("Error: ${err.message}"), Toast.LENGTH_LONG).show()
                }
        }
}

internal fun giftAllAvatarsToUser(
    context: Context,
    uid: String,
    onSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val allIds = AvatarCatalog.avatars.map { it.id }

    val updateData = hashMapOf<String, Any>(
        "unlockedAvatars" to allIds,
        "avatarAllAccessGranted" to true
    )

    db.collection("users").document(uid)
        .set(updateData, SetOptions.merge())
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, com.example.util.appTr("Error al regalar avatares: ${e.message}"), Toast.LENGTH_LONG).show()
        }
}

internal fun revokeSingleAvatarFromUser(
    context: Context,
    uid: String,
    avatarId: String,
    currentEquippedAvatarId: String? = null,
    onSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(uid)

    val updates = mutableMapOf<String, Any>(
        "unlockedAvatars" to FieldValue.arrayRemove(avatarId),
        "avatarAllAccessGranted" to false
    )
    if (currentEquippedAvatarId == avatarId) {
        updates["avatarId"] = "default_poro"
    }

    userRef.update(updates)
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, com.example.util.appTr("Error al quitar avatar: ${e.message}"), Toast.LENGTH_LONG).show()
        }
}

internal fun revokeAllExclusiveAvatarsFromUser(
    context: Context,
    uid: String,
    onSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(uid)

    val defaultAvatars = listOf("default_poro")
    val updateData = hashMapOf<String, Any>(
        "unlockedAvatars" to defaultAvatars,
        "avatarAllAccessGranted" to false,
        "avatarId" to "default_poro"
    )

    userRef.set(updateData, SetOptions.merge())
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, com.example.util.appTr("Error al remover avatares: ${e.message}"), Toast.LENGTH_LONG).show()
        }
}

internal fun updateUserRoleInCloud(
    context: Context,
    uid: String,
    targetRoleId: String,
    onError: (Throwable) -> Unit = {},
    onSuccess: (newRole: String, isBanned: Boolean, inheritedUntil: Long?) -> Unit
) {
    if (targetRoleId == "admin") {
        Toast.makeText(context, com.example.util.appTr("Operación denegada: No se puede asignar el rol de Administrador por directivas de seguridad."), Toast.LENGTH_LONG).show()
        onError(IllegalStateException(com.example.util.appTr("No tienes permisos para esta operación")))
        return
    }

    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
        runCatching { com.example.data.AdminRoleRepository.change(uid, targetRoleId) }
            .onSuccess { response ->
                val account = response["account"] as? Map<*, *>
                val inherited = (account?.get("premiumUntil") as? Number)?.toLong()
                val roleName = AppUserRole.fromId(targetRoleId).displayName
                Toast.makeText(context, com.example.util.appTr("Rol actualizado a $roleName"), Toast.LENGTH_SHORT).show()
                onSuccess(targetRoleId, targetRoleId == "banned", inherited)
            }.onFailure {
                onError(it)
                if (it !is com.example.data.EconomyPendingException) Toast.makeText(context, com.example.util.appTr(it.message ?: "No se pudo completar la operación. Vuelve a intentarlo."), Toast.LENGTH_LONG).show()
            }
    }
}

internal fun updateUserSecondaryRoleInCloud(
    context: Context,
    uid: String,
    targetSecondaryRoleId: String,
    onSuccess: (newSecondaryRole: String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val updatePayload = hashMapOf<String, Any>(
        "secondaryRole" to targetSecondaryRoleId,
        "last_secondary_role_update" to System.currentTimeMillis()
    )

    db.collection("users").document(uid)
        .set(updatePayload, SetOptions.merge())
        .addOnSuccessListener {
            val roleName = if (targetSecondaryRoleId.isNotBlank()) {
                AppUserRole.fromId(targetSecondaryRoleId).displayName
            } else {
                "Ninguno"
            }
            Toast.makeText(context, com.example.util.appTr("Rol secundario actualizado a $roleName"), Toast.LENGTH_SHORT).show()
            onSuccess(targetSecondaryRoleId)
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, com.example.util.appTr("Error al actualizar rol secundario: ${e.message}"), Toast.LENGTH_LONG).show()
        }
}

internal fun toggleUserBanStatus(
    context: Context,
    uid: String,
    isBanned: Boolean,
    newRole: String,
    onSuccess: () -> Unit
) {
    updateUserRoleInCloud(context, uid, newRole) { _, _, _ -> onSuccess() }

}

internal fun updateUserEmail(
    context: Context,
    uid: String,
    newEmail: String,
    onSuccess: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val updatePayload = hashMapOf<String, Any>(
        "email" to newEmail.trim(),
        "lastModifiedByAdmin" to System.currentTimeMillis()
    )
    db.collection("users").document(uid)
        .set(updatePayload, SetOptions.merge())
        .addOnSuccessListener {
            Toast.makeText(context, com.example.util.appTr("Correo electrónico actualizado exitosamente"), Toast.LENGTH_SHORT).show()
            onSuccess(newEmail.trim())
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, com.example.util.appTr("Error al actualizar correo: ${e.message}"), Toast.LENGTH_LONG).show()
        }
}

internal fun createUserManagementApprovalRequest(
    context: Context,
    requestType: String,
    targetUid: String,
    targetName: String,
    targetEmail: String,
    newValue: String,
    onSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val moderator = auth.currentUser

    val reqId = db.collection("moderator_requests").document().id
    val payload = hashMapOf<String, Any>(
        "id" to reqId,
        "requestType" to requestType,
        "targetUid" to targetUid,
        "targetName" to targetName,
        "targetEmail" to targetEmail,
        "newValue" to newValue,
        "requestedByUid" to (moderator?.uid ?: ""),
        "requestedByName" to (com.example.util.SubscriptionManager.userName.value.takeIf { it.isNotBlank() } ?: moderator?.displayName?.takeIf { it.isNotBlank() } ?: "Moderador"),
        "status" to "PENDIENTE",
        "timestamp" to System.currentTimeMillis()
    )

    db.collection("moderator_requests").document(reqId)
        .set(payload)
        .addOnSuccessListener {
            Toast.makeText(context, com.example.util.appTr("Solicitud enviada para aprobación del Administrador"), Toast.LENGTH_LONG).show()
            onSuccess()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, com.example.util.appTr("Error al crear solicitud: ${e.message}"), Toast.LENGTH_LONG).show()
        }
}
