package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.UUID

object DeviceAndSessionManager {
    private const val TAG = "DeviceAndSessionManager"
    private const val PREFS_NAME = "device_session_prefs"
    private const val KEY_SESSION_TOKEN = "local_session_token"
    private const val KEY_PERSISTENT_DEVICE_ID = "persistent_device_id"

    private var localSessionToken: String? = null
    private var lastLocalLoginTimestamp: Long = 0L

    // Obtener o generar un token único para esta sesión de la app
    fun getOrCreateSessionToken(context: Context): String {
        if (localSessionToken != null) return localSessionToken!!
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedToken = prefs.getString(KEY_SESSION_TOKEN, null)
        if (savedToken != null && savedToken.isNotBlank()) {
            localSessionToken = savedToken
            return savedToken
        }
        val newToken = UUID.randomUUID().toString()
        localSessionToken = newToken
        prefs.edit().putString(KEY_SESSION_TOKEN, newToken).apply()
        return newToken
    }

    // Forzar renovación de token de sesión (ej. tras nuevo login)
    fun refreshSessionToken(context: Context): String {
        val newToken = UUID.randomUUID().toString()
        localSessionToken = newToken
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SESSION_TOKEN, newToken).apply()
        return newToken
    }

    private var cachedDeviceId: String? = null

    // Obtener ID del dispositivo persistente y estable por hardware / instalación
    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        if (!cachedDeviceId.isNullOrBlank()) {
            return cachedDeviceId!!
        }

        val appContext = context.applicationContext ?: context
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userPrefs = appContext.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        val internalFile = java.io.File(appContext.filesDir, "device_id.txt")

        // Preserve installation identity across signing-key/OS changes and every login.
        val persisted = listOfNotNull(prefs.getString(KEY_PERSISTENT_DEVICE_ID, null),
            runCatching { internalFile.takeIf { it.exists() }?.readText()?.trim() }.getOrNull(),
            userPrefs.getString(KEY_PERSISTENT_DEVICE_ID, null)).firstOrNull { it.isNotBlank() }
        if (persisted != null) {
            cachedDeviceId = persisted
            prefs.edit().putString(KEY_PERSISTENT_DEVICE_ID, persisted).apply()
            return persisted
        }

        // 1. Hardware-level Android ID (100% estable en el mismo teléfono físico)
        val androidId = try {
            Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (_: Exception) { null }

        if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
            val hardwareId = "WRD_DEVICE_" + androidId.trim().lowercase()
            cachedDeviceId = hardwareId
            prefs.edit().putString(KEY_PERSISTENT_DEVICE_ID, hardwareId).apply()
            userPrefs.edit().putString(KEY_PERSISTENT_DEVICE_ID, hardwareId).apply()
            try { internalFile.writeText(hardwareId) } catch (_: Exception) {}
            return hardwareId
        }

        // 2. SharedPreferences
        val savedId = prefs.getString(KEY_PERSISTENT_DEVICE_ID, null)
        if (!savedId.isNullOrBlank()) {
            cachedDeviceId = savedId
            try { internalFile.writeText(savedId) } catch (_: Exception) {}
            return savedId
        }

        // 3. Internal file
        try {
            if (internalFile.exists()) {
                val fileId = internalFile.readText().trim()
                if (fileId.isNotBlank()) {
                    cachedDeviceId = fileId
                    prefs.edit().putString(KEY_PERSISTENT_DEVICE_ID, fileId).apply()
                    return fileId
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error reading device_id.txt: ${e.message}")
        }

        // 4. User preferences backup
        val backupId = userPrefs.getString(KEY_PERSISTENT_DEVICE_ID, null)
        if (!backupId.isNullOrBlank()) {
            cachedDeviceId = backupId
            prefs.edit().putString(KEY_PERSISTENT_DEVICE_ID, backupId).apply()
            try { internalFile.writeText(backupId) } catch (_: Exception) {}
            return backupId
        }

        // 5. Generate stable persistent installation UUID once and store it forever
        val newId = "WRD_INST_" + UUID.randomUUID().toString()
        cachedDeviceId = newId
        prefs.edit().putString(KEY_PERSISTENT_DEVICE_ID, newId).apply()
        userPrefs.edit().putString(KEY_PERSISTENT_DEVICE_ID, newId).apply()
        try { internalFile.writeText(newId) } catch (_: Exception) {}
        return newId
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

        val deviceId = getDeviceId(context)
        val sessionToken = refreshSessionToken(context)
        val loginTimestamp = System.currentTimeMillis()
        lastLocalLoginTimestamp = loginTimestamp

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(user!!.uid)

        val appContext = context.applicationContext ?: context
        val aliases = buildSet {
            add(deviceId)
            appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_PERSISTENT_DEVICE_ID, null)?.let(::add)
            appContext.getSharedPreferences("user_preferences", Context.MODE_PRIVATE).getString(KEY_PERSISTENT_DEVICE_ID, null)?.let(::add)
            runCatching { java.io.File(appContext.filesDir, "device_id.txt").readText().trim() }.getOrNull()?.takeIf { it.isNotBlank() }?.let(::add)
            runCatching { Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID) }.getOrNull()
                ?.takeIf { it.isNotBlank() && it != "9774d56d682e549c" }?.let { add("WRD_DEVICE_${it.trim().lowercase()}") }
        }
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val role = snapshot.getString("role").orEmpty()
            val admin = com.example.model.RolePanelAccess.isAdministrator(role, AuthManager.isCurrentUserAdmin())
            val existing = (snapshot.get("registeredDevices") as? List<*>).orEmpty().filterIsInstance<String>()
            val devices = com.example.data.DeviceSlotPolicy.register(existing, deviceId, aliases, admin)
            transaction.set(userRef, mapOf("sessionToken" to sessionToken, "lastDeviceId" to deviceId,
                "last_active" to loginTimestamp, "is_online" to true, "registeredDevices" to devices), SetOptions.merge())
        }.addOnSuccessListener { onSuccess() }.addOnFailureListener { error ->
            // A network failure must neither replace other devices nor pretend registration succeeded.
            val message = generateSequence(error as Throwable?) { it.cause }.mapNotNull { it.message }
                .firstOrNull { it.contains("Límite de dispositivos") } ?: "No se pudo registrar el dispositivo. Vuelve a intentarlo."
            onError(message)
        }
    }

    fun handleSessionChanged(remoteSessionToken: String?, remoteDeviceId: String?, remoteTimestamp: Long = 0L, context: Context) {
        val user = AuthManager.getAuth()?.currentUser ?: return
        if (AuthManager.isGuestOrUnauthenticated(user)) return
        val isAdmin = AuthManager.isCurrentUserAdmin()

        // Administradores nunca se desconectan por concurrencia
        if (isAdmin) return

        val currentDeviceId = getDeviceId(context)

        // Si el snapshot remoto proviene de este MISMO dispositivo o los datos son incompletos, no cerrar sesión jamás
        if (remoteDeviceId.isNullOrBlank() || remoteDeviceId == currentDeviceId) {
            if (!remoteSessionToken.isNullOrBlank()) {
                localSessionToken = remoteSessionToken
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().putString(KEY_SESSION_TOKEN, remoteSessionToken).apply()
            }
            return
        }

        val currentLocalToken = getOrCreateSessionToken(context)
        if (!remoteSessionToken.isNullOrBlank() && remoteSessionToken != currentLocalToken) {
            // Solo desconectar si explícitamente se confirmó que otro dispositivo tomó la sesión activa y ocurrió después de nuestro login
            val isSubsequentRemoteLogin = remoteTimestamp > (lastLocalLoginTimestamp + 3000L)
            if (remoteDeviceId != currentDeviceId && remoteDeviceId.length >= 6 && isSubsequentRemoteLogin) {
                Log.w(TAG, "Sesión concurrente detectada desde otro dispositivo ($remoteDeviceId). Cerrando sesión local.")
                AuthManager.getAuth()?.signOut()
                Toast.makeText(context, com.example.util.appTr("Sesión cerrada: Tu cuenta se inició en otro dispositivo."), Toast.LENGTH_LONG).show()
                localSessionToken = null
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().remove(KEY_SESSION_TOKEN).apply()
            }
        }
    }
}


