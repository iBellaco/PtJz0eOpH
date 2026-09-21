package com.example.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Modelo de datos para comunicados globales y alertas del sistema.
 */
data class GlobalAnnouncement(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val isUrgent: Boolean = false,
    val sendNotification: Boolean = false,
    val timestamp: Long = 0L,
    val active: Boolean = false
) {
    fun getFormattedDate(): String {
        return try {
            if (timestamp <= 0L) return "Reciente"
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (_: Exception) {
            "Reciente"
        }
    }
}

/**
 * Gestor centralizado de comunicados y anuncios globales en tiempo real.
 * Funciona de manera transparente tanto en dispositivos con sesión iniciada
 * como en dispositivos invitados o sin iniciar sesión.
 */
object GlobalAnnouncementManager {
    private const val TAG = "GlobalAnnouncementMgr"
    private const val PREFS_NAME = "coach_global_announcements_prefs"
    private const val KEY_DISMISSED_ID = "dismissed_announcement_id"
    private const val KEY_DISMISSED_TIMESTAMP = "dismissed_announcement_timestamp"
    private const val KEY_CACHED_JSON = "cached_announcement_json"

    private const val FIRESTORE_COLLECTION = "system_config"
    private const val FIRESTORE_DOC_ANNOUNCEMENT = "announcement"

    private const val NOTIFICATION_CHANNEL_ID = "coach_broadcast_channel"
    private const val NOTIFICATION_ID = 8842

    private val _currentAnnouncement = MutableStateFlow<GlobalAnnouncement?>(null)
    val currentAnnouncement: StateFlow<GlobalAnnouncement?> = _currentAnnouncement.asStateFlow()

    private val _isAnnouncementVisible = MutableStateFlow(false)
    val isAnnouncementVisible: StateFlow<Boolean> = _isAnnouncementVisible.asStateFlow()

    private var firestoreListener: ListenerRegistration? = null
    private var authStateListener: FirebaseAuth.AuthStateListener? = null
    private var isAuthenticatingAnonymously = false
    private var isInitialized = false

    fun init(context: Context) {
        val appContext = context.applicationContext
        if (!isInitialized) {
            isInitialized = true
            // 1. Cargar anuncio en caché de inmediato (disponibilidad instantánea y offline)
            loadFromLocalStorage(appContext)

            // 2. Monitorear cambios de sesión de usuario
            setupAuthStateListener(appContext)
        }

        // 3. Garantizar autenticación anónima si no hay sesión iniciada y conectar listener
        ensureAuthAndSync(appContext)
    }

    private fun setupAuthStateListener(context: Context) {
        if (authStateListener != null) return
        try {
            val auth = FirebaseAuth.getInstance()
            authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                Log.d(TAG, "Estado de autenticación actualizado (uid=${user?.uid}, anon=${user?.isAnonymous})")
                if (user == null) {
                    ensureAuthAndSync(context)
                } else {
                    attachFirestoreListener(context, force = true)
                    refreshFromCloud(context)
                }
            }
            auth.addAuthStateListener(authStateListener!!)
        } catch (e: Exception) {
            Log.w(TAG, "Error configurando AuthStateListener: ${e.message}")
        }
    }

    private fun ensureAuthAndSync(context: Context) {
        val appContext = context.applicationContext
        com.example.util.GuestAuthHelper.ensureAuth {
            attachFirestoreListener(appContext, force = true)
            executeCloudRefresh(appContext, null)
        }
    }

    fun attachFirestoreListener(context: Context, force: Boolean = false) {
        if (force) {
            try {
                firestoreListener?.remove()
            } catch (_: Exception) {}
            firestoreListener = null
        }
        if (firestoreListener != null) return

        try {
            val db = FirebaseFirestore.getInstance()
            firestoreListener = db.collection(FIRESTORE_COLLECTION).document(FIRESTORE_DOC_ANNOUNCEMENT)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Error en listener de anuncios globales: ${error.message}")
                        try {
                            firestoreListener?.remove()
                        } catch (_: Exception) {}
                        firestoreListener = null
                        // Si ocurrió error de permisos, reintentar asegurar credenciales
                        com.example.util.GuestAuthHelper.ensureAuth {
                            attachFirestoreListener(context, force = false)
                        }
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        processFirestoreSnapshot(context, snapshot)
                    } else {
                        // El documento no existe o se eliminó
                        clearActiveAnnouncement(context)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo registrar snapshot listener de anuncios globales: ${e.message}")
            firestoreListener = null
        }
    }

    fun refreshFromCloud(context: Context, onComplete: ((Boolean) -> Unit)? = null) {
        val appContext = context.applicationContext
        val auth = try { FirebaseAuth.getInstance() } catch (_: Exception) { null }
        if (auth?.currentUser == null) {
            com.example.util.GuestAuthHelper.ensureAuth {
                executeCloudRefresh(appContext, onComplete)
            }
        } else {
            executeCloudRefresh(appContext, onComplete)
        }
    }

    private fun executeCloudRefresh(appContext: Context, onComplete: ((Boolean) -> Unit)?) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection(FIRESTORE_COLLECTION).document(FIRESTORE_DOC_ANNOUNCEMENT)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot != null && snapshot.exists()) {
                        processFirestoreSnapshot(appContext, snapshot)
                        onComplete?.invoke(true)
                    } else {
                        clearActiveAnnouncement(appContext)
                        onComplete?.invoke(true)
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error refrescando anuncio global desde la nube: ${e.message}")
                    onComplete?.invoke(false)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción en refreshFromCloud: ${e.message}")
            onComplete?.invoke(false)
        }
    }

    private fun processFirestoreSnapshot(context: Context, snapshot: com.google.firebase.firestore.DocumentSnapshot) {
        try {
            val active = snapshot.getBoolean("active") ?: false
            val title = snapshot.getString("title") ?: ""
            val message = snapshot.getString("message") ?: ""
            val isUrgent = snapshot.getBoolean("isUrgent") ?: false
            val sendNotification = snapshot.getBoolean("sendNotification") ?: isUrgent
            val timestamp = snapshot.getLong("timestamp") ?: 0L
            val id = snapshot.getString("id") ?: "${title.hashCode()}_$timestamp"

            if (!active || title.isBlank() || message.isBlank()) {
                clearActiveAnnouncement(context)
                return
            }

            val announcement = GlobalAnnouncement(
                id = id,
                title = title,
                message = message,
                isUrgent = isUrgent,
                sendNotification = sendNotification,
                timestamp = timestamp,
                active = true
            )

            // Guardar en caché local
            saveToLocalStorage(context, announcement)

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastDismissedId = prefs.getString(KEY_DISMISSED_ID, "") ?: ""
            val lastDismissedTimestamp = prefs.getLong(KEY_DISMISSED_TIMESTAMP, 0L)

            // Si es un anuncio nuevo o actualizado con un timestamp superior al descartado
            val isNewOrUpdated = (announcement.id != lastDismissedId) || 
                    (announcement.timestamp > lastDismissedTimestamp && announcement.timestamp - lastDismissedTimestamp > 1000)

            _currentAnnouncement.value = announcement

            if (isNewOrUpdated) {
                _isAnnouncementVisible.value = true
                // Disparar notificación en la barra de estado del sistema si está habilitado
                if (announcement.sendNotification) {
                    showSystemNotification(context, announcement)
                }
            } else {
                // Ya fue descartado previamente por el usuario, mantener en memoria para banner secundario
                _isAnnouncementVisible.value = false
            }

            Log.d(TAG, "Anuncio global procesado con éxito: ${announcement.title} (visible=${_isAnnouncementVisible.value})")
        } catch (e: Exception) {
            Log.e(TAG, "Error procesando snapshot de anuncio global: ${e.message}")
        }
    }

    fun dismissAnnouncement(context: Context, announcementId: String, timestamp: Long) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_DISMISSED_ID, announcementId)
                .putLong(KEY_DISMISSED_TIMESTAMP, timestamp)
                .apply()
            _isAnnouncementVisible.value = false
        } catch (e: Exception) {
            Log.w(TAG, "Error guardando descarte de anuncio: ${e.message}")
            _isAnnouncementVisible.value = false
        }
    }

    fun showAnnouncementModal() {
        if (_currentAnnouncement.value?.active == true) {
            _isAnnouncementVisible.value = true
        }
    }

    fun hideAnnouncementModal() {
        _isAnnouncementVisible.value = false
    }

    private fun clearActiveAnnouncement(context: Context) {
        _currentAnnouncement.value = null
        _isAnnouncementVisible.value = false
        cancelSystemNotification(context)
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().remove(KEY_CACHED_JSON).apply()
        } catch (_: Exception) {}
    }

    private fun saveToLocalStorage(context: Context, announcement: GlobalAnnouncement) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val json = JSONObject().apply {
                put("id", announcement.id)
                put("title", announcement.title)
                put("message", announcement.message)
                put("isUrgent", announcement.isUrgent)
                put("timestamp", announcement.timestamp)
                put("active", announcement.active)
            }
            prefs.edit().putString(KEY_CACHED_JSON, json.toString()).apply()
        } catch (e: Exception) {
            Log.w(TAG, "Error guardando anuncio en caché local: ${e.message}")
        }
    }

    private fun loadFromLocalStorage(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val rawJson = prefs.getString(KEY_CACHED_JSON, null) ?: return
            val json = JSONObject(rawJson)
            val active = json.optBoolean("active", false)
            if (!active) return

            val announcement = GlobalAnnouncement(
                id = json.optString("id", ""),
                title = json.optString("title", ""),
                message = json.optString("message", ""),
                isUrgent = json.optBoolean("isUrgent", false),
                timestamp = json.optLong("timestamp", 0L),
                active = true
            )

            val lastDismissedId = prefs.getString(KEY_DISMISSED_ID, "") ?: ""
            val lastDismissedTimestamp = prefs.getLong(KEY_DISMISSED_TIMESTAMP, 0L)
            val isNewOrUpdated = (announcement.id != lastDismissedId) ||
                    (announcement.timestamp > lastDismissedTimestamp && announcement.timestamp - lastDismissedTimestamp > 1000)

            _currentAnnouncement.value = announcement
            _isAnnouncementVisible.value = isNewOrUpdated
        } catch (e: Exception) {
            Log.w(TAG, "Error cargando anuncio desde caché local: ${e.message}")
        }
    }

    fun showSystemNotification(context: Context, announcement: GlobalAnnouncement) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Comunicados Globales Coach",
                    if (announcement.isUrgent) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notificaciones oficiales de mantenimiento, parches y anuncios globales de Coach"
                    enableLights(true)
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Validar permiso en Android 13+ (POST_NOTIFICATIONS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (!hasPermission) {
                    return
                }
            }

            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("extra_open_global_announcement", true)
                putExtra("extra_announcement_id", announcement.id)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                openIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val prefix = if (announcement.isUrgent) "🚨 COMUNICADO URGENTE: " else "📢 COMUNICADO OFICIAL: "

            val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(prefix + announcement.title)
                .setContentText(announcement.message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(announcement.message))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(if (announcement.isUrgent) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)

            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            Log.w(TAG, "Error publicando notificación del sistema: ${e.message}")
        }
    }

    fun cancelSystemNotification(context: Context) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.cancel(NOTIFICATION_ID)
        } catch (_: Exception) {}
    }

    /**
     * Publica o actualiza el comunicado global en la nube.
     * Llega inmediatamente a todos los dispositivos en tiempo real (con o sin sesión).
     */
    fun publishAnnouncement(
        context: Context,
        title: String,
        message: String,
        isUrgent: Boolean,
        sendNotification: Boolean,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val newId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val announcementData = hashMapOf(
            "id" to newId,
            "title" to title.trim(),
            "message" to message.trim(),
            "isUrgent" to isUrgent,
            "sendNotification" to sendNotification,
            "timestamp" to now,
            "active" to true
        )

        try {
            val db = FirebaseFirestore.getInstance()
            db.collection(FIRESTORE_COLLECTION).document(FIRESTORE_DOC_ANNOUNCEMENT)
                .set(announcementData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "Anuncio global publicado en Firestore con id=$newId")
                    val announcement = GlobalAnnouncement(
                        id = newId,
                        title = title.trim(),
                        message = message.trim(),
                        isUrgent = isUrgent,
                        sendNotification = sendNotification,
                        timestamp = now,
                        active = true
                    )
                    saveToLocalStorage(context, announcement)
                    _currentAnnouncement.value = announcement
                    onComplete(true, null)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error publicando anuncio global en Firestore: ${e.message}")
                    onComplete(false, e.message)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción publicando anuncio global: ${e.message}")
            onComplete(false, e.message)
        }
    }

    /**
     * Desactiva el comunicado global actual en la nube.
     */
    fun deactivateAnnouncement(
        context: Context,
        onComplete: (Boolean, String?) -> Unit
    ) {
        try {
            val db = FirebaseFirestore.getInstance()
            val updateData = hashMapOf<String, Any>(
                "active" to false
            )
            db.collection(FIRESTORE_COLLECTION).document(FIRESTORE_DOC_ANNOUNCEMENT)
                .set(updateData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "Anuncio global desactivado en Firestore.")
                    clearActiveAnnouncement(context)
                    onComplete(true, null)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error desactivando anuncio global: ${e.message}")
                    onComplete(false, e.message)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción desactivando anuncio global: ${e.message}")
            onComplete(false, e.message)
        }
    }
}
