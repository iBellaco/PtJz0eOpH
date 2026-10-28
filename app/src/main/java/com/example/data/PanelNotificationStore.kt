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
import com.example.MainActivity
import com.example.R
import com.example.model.RolePanel
import com.example.model.RolePanelAccess
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Navigation and profile share listeners; the last subscriber removes them. */
object PanelNotificationStore {
    data class Key(val uid: String, val role: String, val secondary: String, val adminClaim: Boolean)
    private val _queues = MutableStateFlow<Map<NotificationPanel, Set<String>>>(emptyMap())
    val queues = _queues.asStateFlow()
    private var key: Key? = null
    private var consumers = 0
    private var queueVersions = emptyMap<NotificationPanel, Map<String, String>>()
    private val listeners = mutableListOf<ListenerRegistration>()
    private var supportPrimed = false
    private var notifiedSupportRevisions = emptyMap<String, String>()

    @Synchronized fun acquire(next: Key) {
        if (key != next) {
            listeners.forEach { it.remove() }; listeners.clear()
            _queues.value = emptyMap(); queueVersions = emptyMap(); PanelReadRepository.updateVersions(emptyMap()); PanelReadRepository.updateRead(emptySet());
            supportPrimed = false; notifiedSupportRevisions = emptyMap(); consumers = 0; key = next
            if (next.uid.isNotBlank()) {
                val db = FirebaseFirestore.getInstance()
                var legacyReads = emptySet<String>()
                var profileReads = emptySet<String>()
                listeners += db.collection("users").document(next.uid).addSnapshotListener { snapshot, error ->
                    if (key == next && error == null) {
                        profileReads = (snapshot?.get("panelReadKeys") as? List<*>)?.filterIsInstance<String>().orEmpty().toSet()
                        PanelReadRepository.updateRead(legacyReads + profileReads)
                    }
                }
                listeners += db.collection("users").document(next.uid).collection("panel_reads").addSnapshotListener { snapshot, error ->
                    if (key == next && error == null) {
                        legacyReads = snapshot?.documents.orEmpty().map { it.id }.toSet()
                        PanelReadRepository.updateRead(legacyReads + profileReads)
                    }
                }
                val admin = RolePanelAccess.isAdministrator(next.role, next.adminClaim)
                if (RolePanelAccess.canOpen(RolePanel.MODERATION, next.role, next.secondary, next.adminClaim)) {
                    val query = if (admin) db.collection("support_reports") else db.collection("support_reports").whereEqualTo("staffVisible", true)
                    listeners += query.addSnapshotListener { snapshot, error ->
                        publish(next, NotificationPanel.SUPPORT, if (error == null) snapshot?.documents.orEmpty().filter {
                            UserPanelNotificationPolicy.staffNeedsAttention(it.data.orEmpty(), admin) && it.getString("userId") != next.uid
                        }.associate { "support:${it.id}" to PanelReadRepository.revision(it.data.orEmpty()) } else emptyMap())
                    }
                }
                if (admin) {
                    var roleRequests = emptyMap<String, String>()
                    var streamerRequests = emptyMap<String, String>()
                    var cashRequests = emptyMap<String, String>()
                    var sponsorRequests = emptyMap<String, String>()
                    fun publishAdministration() = publish(next, NotificationPanel.ADMINISTRATION,
                        roleRequests + streamerRequests + cashRequests + sponsorRequests)
                    listeners += EssenceEconomyRepository.redemptions.whereEqualTo("status", "PENDING").addSnapshotListener { snapshot, error ->
                        cashRequests = if (error == null) snapshot?.documents.orEmpty().associate { "support:payment_${it.id}" to PanelReadRepository.revision(it.data.orEmpty()) } else emptyMap()
                        publishAdministration()
                    }
                    listeners += db.collection("moderator_requests").whereEqualTo("status", "PENDIENTE").addSnapshotListener { snapshot, error ->
                        roleRequests = if (error == null) snapshot?.documents.orEmpty().associate { "support:${it.id}" to PanelReadRepository.revision(it.data.orEmpty()) } else emptyMap()
                        publishAdministration()
                    }
                    listeners += StreamerRepository.requests.whereEqualTo("status", "PENDING").addSnapshotListener { snapshot, error ->
                        streamerRequests = if (error == null) snapshot?.documents.orEmpty().filterNot { StreamerPublicationPolicy.isExpired(it.data.orEmpty()) }
                            .associate { "streamer:${it.id}" to PanelReadRepository.revision(it.data.orEmpty()) } else emptyMap()
                        publishAdministration()
                    }
                    listeners += db.collection("pending_sponsor_ads").whereEqualTo("isApproved", false).addSnapshotListener { snapshot, error ->
                        sponsorRequests = if (error == null) snapshot?.documents.orEmpty()
                            .filter { SponsorReviewPolicy.pending(it.data.orEmpty()) }
                            .associate { "sponsor:${it.id}" to PanelReadRepository.revision(it.data.orEmpty()) } else emptyMap()
                        publishAdministration()
                    }
                }
            }
        }
        consumers++
    }

    @Synchronized private fun publish(source: Key, panel: NotificationPanel, events: Map<String, String>) {
        if (key == source) {
            if (panel == NotificationPanel.SUPPORT && RolePanelAccess.canOpen(RolePanel.MODERATION, source.role, source.secondary, source.adminClaim)) {
                val previous = queueVersions[NotificationPanel.SUPPORT].orEmpty()
                if (supportPrimed) {
                    events.forEach { (event, revision) ->
                        if (previous[event] != revision && notifiedSupportRevisions[event] != revision) {
                            notifySupportMessage(source.uid, event, revision)
                        }
                    }
                }
                supportPrimed = true
            }
            queueVersions = queueVersions + (panel to events)
            // A pending payment shares its event with support. The latest conversation wins.
            PanelReadRepository.updateVersions(queueVersions[NotificationPanel.ADMINISTRATION].orEmpty() + queueVersions[NotificationPanel.SUPPORT].orEmpty())
            _queues.value = _queues.value + (panel to events.keys)
        }
    }

    /** Shows an immediate staff alert when a support conversation changes while the app is open. */
    private fun notifySupportMessage(uid: String, event: String, revision: String) {
        val context = runCatching { com.google.firebase.FirebaseApp.getInstance().applicationContext }.getOrNull() ?: return
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        val channelId = "coach_support_staff"
        if (Build.VERSION.SDK_INT >= 26) {
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.createNotificationChannel(
                NotificationChannel(channelId, com.example.util.appTr("Bandeja de Entrada"), NotificationManager.IMPORTANCE_HIGH)
            )
        }
        val intent = Intent(context, MainActivity::class.java)
            .putExtra("OPEN_SUPPORT_PANEL", true)
            .putExtra("OPEN_SUPPORT_UID", uid)
            .setAction("coach.support.$event")
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pending = PendingIntent.getActivity(context, event.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val id = PanelReadRepository.key(uid, event).hashCode()
        runCatching {
            manager.notify(id, NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(com.example.util.appTr("Nuevo mensaje en Coach"))
                .setContentText(com.example.util.appTr("Abre tu bandeja de entrada para leerlo."))
                .setContentIntent(pending).setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).build())
            notifiedSupportRevisions = notifiedSupportRevisions + (event to revision)
        }
    }

    @Synchronized fun release(source: Key) {
        if (key != source) return
        consumers--
        if (consumers <= 0) {
            listeners.forEach { it.remove() }; listeners.clear()
            _queues.value = emptyMap(); PanelReadRepository.updateVersions(emptyMap()); PanelReadRepository.updateRead(emptySet()); key = null; consumers = 0
        }
    }
}
