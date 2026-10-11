package com.example.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.example.MainActivity
import com.example.R
import com.example.util.AuthManager
import com.example.util.appTr
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import java.util.concurrent.TimeUnit

/** Local inbox alerts share durable revision acknowledgements across listener and worker. */
object InboxPhoneNotifications {
    private const val CHANNEL = "coach_inbox"
    private const val WORK = "CoachInboxNotifications"
    private const val EXTRA = "coach_open_inbox_uid"
    private val _openInbox = MutableStateFlow(false)
    val openInbox = _openInbox.asStateFlow()

    fun consumeIntent(intent: Intent) {
        val uid = intent.getStringExtra(EXTRA) ?: return
        intent.removeExtra(EXTRA)
        if (AuthManager.getAuth()?.currentUser?.uid == uid) _openInbox.value = true
    }
    fun consumed() { _openInbox.value = false }

    fun schedule(context: Context, uid: String) {
        try { WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK, ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<InboxNotificationWorker>(15, TimeUnit.MINUTES)
                .setInputData(workDataOf("uid" to uid))
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build())
        } catch (error: Exception) { com.example.util.AppLogger.e("InboxPhone", "No se pudo programar la revisión de mensajes", error) }
    }

    @Synchronized
    fun stop(context: Context) {
        runCatching { WorkManager.getInstance(context).cancelUniqueWork(WORK) }
        val prefs = context.getSharedPreferences("inbox_phone", Context.MODE_PRIVATE)
        val uid = prefs.getString("active_uid", "").orEmpty()
        prefs.getStringSet("active_keys", emptySet()).orEmpty().forEach { cancel(context, uid, it) }
        prefs.edit().remove("active_uid").remove("active_keys").apply()
        consumed()
    }

    private fun cancel(context: Context, uid: String, key: String) {
        NotificationManagerCompat.from(context).cancel(PanelReadRepository.key(uid, key), 1)
    }

    @Synchronized
    fun acknowledge(context: Context, uid: String, key: String) { cancel(context, uid, key) }

    @Synchronized
    fun update(context: Context, uid: String, messages: List<Map<String, Any>>, authoritative: Boolean = false) {
        val user = AuthManager.getAuth()?.currentUser ?: return
        if (user.uid != uid || AuthManager.isGuestOrUnauthenticated(user)) return
        val candidates = InboxPhoneNotificationPolicy.candidates(messages)
        val prefs = context.getSharedPreferences("inbox_phone", Context.MODE_PRIVATE)
        val previousUid = prefs.getString("active_uid", "").orEmpty()
        val previousKeys = prefs.getStringSet("active_keys", emptySet()).orEmpty()
        if (previousUid.isNotBlank() && previousUid != uid) previousKeys.forEach { cancel(context, previousUid, it) }
        if (authoritative && previousUid == uid) (previousKeys - candidates.keys).forEach { cancel(context, uid, it) }
        val active = if (authoritative || previousUid != uid) candidates.keys else previousKeys + candidates.keys
        prefs.edit().putString("active_uid", uid).putStringSet("active_keys", active).apply()
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled() || (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)) return
        val system = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        if (Build.VERSION.SDK_INT >= 26) system.createNotificationChannel(NotificationChannel(CHANNEL,
            appTr("Bandeja de Entrada"), NotificationManager.IMPORTANCE_DEFAULT))
        val historyKey = "delivered_" + PanelReadRepository.key(uid)
        val history = runCatching { JSONArray(prefs.getString(historyKey, "[]") ?: "[]") }.getOrDefault(JSONArray())
        val delivered = (0 until history.length()).map { history.getString(it) }.toMutableList()
        for ((key, revision) in candidates) {
            if (revision in delivered) continue
            val intent = Intent(context, MainActivity::class.java).putExtra(EXTRA, uid)
                .setAction("coach.inbox." + PanelReadRepository.key(uid, key))
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val notification = NotificationCompat.Builder(context, CHANNEL).setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(appTr("Nuevo mensaje en Coach"))
                .setContentText(appTr("Abre tu bandeja de entrada para leerlo."))
                .setContentIntent(pending).setAutoCancel(true).setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).build()
            try {
                manager.notify(PanelReadRepository.key(uid, key), 1, notification)
                delivered.add(revision)
            } catch (_: SecurityException) { return }
        }
        prefs.edit().putString(historyKey, JSONArray(delivered.takeLast(512)).toString()).apply()
    }
}

/** Android schedules this network check even after the activity/process has closed. */
class InboxNotificationWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        val uid = inputData.getString("uid") ?: return Result.success()
        val user = AuthManager.getAuth()?.currentUser ?: return Result.success()
        if (user.uid != uid || AuthManager.isGuestOrUnauthenticated(user)) return Result.success()
        return try {
            val profile = com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(uid)
            val snapshot = profile.get(com.google.firebase.firestore.Source.SERVER).await()
            val legacy = (snapshot.get("privateMessages") as? List<*>).orEmpty().filterIsInstance<Map<String, Any>>()
            val prefs = applicationContext.getSharedPreferences("inbox_phone", Context.MODE_PRIVATE)
            val cursorKey = "cursor_" + PanelReadRepository.key(uid)
            val cursor = prefs.getString(cursorKey, "").orEmpty()
            var query = profile.collection("messages").whereEqualTo("isRead", false)
                .orderBy(com.google.firebase.firestore.FieldPath.documentId()).limit(100)
            if (cursor.isNotBlank()) query = query.startAfter(cursor)
            val page = query.get(com.google.firebase.firestore.Source.SERVER).await().documents
            val documents = page.map { it.data.orEmpty() + ("id" to it.id) }
            InboxPhoneNotifications.update(applicationContext, uid, legacy + documents,
                authoritative = cursor.isBlank() && page.size < 100)
            prefs.edit().putString(cursorKey, if (page.size == 100) page.last().id else "").apply()
            Result.success()
        } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
        catch (_: Exception) { Result.retry() }
    }
}
