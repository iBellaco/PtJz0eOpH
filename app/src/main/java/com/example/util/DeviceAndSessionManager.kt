package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.UUID

object DeviceAndSessionManager {
    private const val PREFS_NAME = "device_session_prefs"
    private const val KEY_SESSION_TOKEN = "local_session_token"
    private const val KEY_PERSISTENT_DEVICE_ID = "persistent_device_id"

    private var localSessionToken: String? = null
    @Volatile private var registrationPending = false
    private val legacyAliases = mutableSetOf<String>()

    private var cachedDeviceId: String? = null

    // Prefer the signing-scoped hardware ID over installation files and restored preferences.
    @SuppressLint("HardwareIds")
    @Synchronized
    fun getDeviceId(context: Context): String {
        cachedDeviceId?.let { return it }
        val appContext = context.applicationContext ?: context
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userPrefs = appContext.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        val file = java.io.File(appContext.filesDir, "device_id.txt")
        val saved = listOfNotNull(prefs.getString(KEY_PERSISTENT_DEVICE_ID, null),
            userPrefs.getString(KEY_PERSISTENT_DEVICE_ID, null),
            runCatching { file.takeIf { it.exists() }?.readText()?.trim() }.getOrNull()).filter { it.isNotBlank() }
        legacyAliases += saved
        legacyAliases += prefs.getStringSet("legacy_device_aliases", emptySet()).orEmpty()
        val androidId = runCatching { Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID) }.getOrNull()
        val id = com.example.data.DeviceIdentityPolicy.resolve(androidId, saved.firstOrNull())
        legacyAliases += id
        prefs.edit().putString(KEY_PERSISTENT_DEVICE_ID, id).putStringSet("legacy_device_aliases", legacyAliases.toSet()).apply()
        userPrefs.edit().putString(KEY_PERSISTENT_DEVICE_ID, id).apply()
        runCatching { file.writeText(id) }
        cachedDeviceId = id
        return id
    }

    fun updatePresence(uid: String, context: Context, online: Boolean) {
        if (registrationPending) return
        val token = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_SESSION_TOKEN, null) ?: return
        val device = runCatching { getDeviceId(context) }.getOrNull() ?: return
        val ref = FirebaseFirestore.getInstance().collection("users").document(uid)
        FirebaseFirestore.getInstance().runTransaction { tx ->
            val account = tx.get(ref)
            if (com.example.data.DeviceSessionPolicy.owns(token, device, account.getString("sessionToken"), account.getString("lastDeviceId"))) {
                tx.update(ref, mapOf("last_active" to System.currentTimeMillis(), "is_online" to online))
            }
        }
    }

    /** Restoring the app never takes a session away from the other phone. */
    fun resumeDeviceAndSession(context: Context, onError: (String) -> Unit = {}) {
        val user = AuthManager.getAuth()?.currentUser ?: return
        if (AuthManager.isGuestOrUnauthenticated(user)) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val token = prefs.getString(KEY_SESSION_TOKEN, null)
        registrationPending = true
        FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener { account ->
                if (AuthManager.getAuth()?.currentUser?.uid != user.uid) { registrationPending = false; return@addOnSuccessListener }
                registrationPending = false
                val device = runCatching { getDeviceId(context) }.getOrNull()
                if (device != null && com.example.data.DeviceSessionPolicy.owns(token, device, account.getString("sessionToken"), account.getString("lastDeviceId"))) {
                    localSessionToken = token
                    updatePresence(user.uid, context, true)
                } else if (account.getString("sessionToken").isNullOrBlank() ||
                    (!device.isNullOrBlank() && account.getString("lastDeviceId") in legacyAliases && account.getString("sessionToken") == token)) {
                    // One-time migration keeps an existing installation's occupied slot.
                    registerDeviceAndSession(context, onError = onError)
                } else {
                    closeDisplacedSession(context)
                    onError("Sesión cerrada: Tu cuenta se inició en otro dispositivo.")
                }
            }.addOnFailureListener {
                registrationPending = false
                onError("No se pudo comprobar la sesión. Vuelve a intentarlo.")
            }
    }

    // Limpia slots huérfanos o resetea dispositivos registrados para dejar solo el teléfono actual
    fun resetDeviceSlots(context: Context, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val user = AuthManager.getAuth()?.currentUser
        if (AuthManager.isGuestOrUnauthenticated(user)) {
            onError("Usuario no logueado")
            return
        }
        val currentDeviceId = getDeviceId(context)
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(user!!.uid)
        userRef.update("registeredDevices", listOf(currentDeviceId))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Error al resetear dispositivos") }
    }

    // Registrar sesión y dispositivo en Firestore de manera segura y sin desconexiones accidentales
    fun registerDeviceAndSession(context: Context, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val user = AuthManager.getAuth()?.currentUser
        if (AuthManager.isGuestOrUnauthenticated(user)) {
            onError("Usuario no logueado")
            return
        }

        val deviceId = runCatching { getDeviceId(context) }.getOrElse {
            onError("No se pudo identificar este dispositivo. Vuelve a intentarlo.")
            return
        }
        val sessionToken = UUID.randomUUID().toString()
        registrationPending = true
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(user!!.uid)
        val aliases = synchronized(this) { legacyAliases.toSet() }

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val existing = (snapshot.get("registeredDevices") as? List<*>).orEmpty().filterIsInstance<String>()
            val devices = com.example.data.DeviceSlotPolicy.register(existing, deviceId, aliases, false)
            transaction.set(userRef, mapOf("sessionToken" to sessionToken, "lastDeviceId" to deviceId,
                "last_active" to System.currentTimeMillis(), "sessionStartedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(), "is_online" to true, "registeredDevices" to devices), SetOptions.merge())
        }.addOnSuccessListener {
            if (AuthManager.getAuth()?.currentUser?.uid != user.uid) { registrationPending = false; return@addOnSuccessListener }
            // Persist ownership only after the server commits registration.
            localSessionToken = sessionToken
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(KEY_SESSION_TOKEN, sessionToken).apply()
            registrationPending = false
            onSuccess()
        }.addOnFailureListener { error ->
            registrationPending = false
            // A network failure must neither replace other devices nor pretend registration succeeded.
            val message = generateSequence(error as Throwable?) { it.cause }.mapNotNull { it.message }
                .firstOrNull { it.contains("Límite de dispositivos") } ?: "No se pudo registrar el dispositivo. Vuelve a intentarlo."
            onError(message)
        }
    }

    fun handleSessionChanged(remoteSessionToken: String?, remoteDeviceId: String?,
        context: Context, active: Boolean = true) {
        val user = AuthManager.getAuth()?.currentUser ?: return
        if (AuthManager.isGuestOrUnauthenticated(user) || registrationPending) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val token = localSessionToken ?: prefs.getString(KEY_SESSION_TOKEN, null)
        val device = runCatching { getDeviceId(context) }.getOrNull() ?: return
        if (com.example.data.DeviceSessionPolicy.displaced(token, device, remoteSessionToken, remoteDeviceId, active)) {
            closeDisplacedSession(context)
        }
    }

    private fun closeDisplacedSession(context: Context) {
        AuthManager.getAuth()?.signOut()
        localSessionToken = null
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().remove(KEY_SESSION_TOKEN).apply()
        Toast.makeText(context, appTr("Sesión cerrada: Tu cuenta se inició en otro dispositivo."), Toast.LENGTH_LONG).show()
    }
}
