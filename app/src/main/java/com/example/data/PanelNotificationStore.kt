package com.example.data

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

    @Synchronized fun acquire(next: Key) {
        if (key != next) {
            listeners.forEach { it.remove() }; listeners.clear()
            _queues.value = emptyMap(); queueVersions = emptyMap(); PanelReadRepository.updateVersions(emptyMap()); PanelReadRepository.updateRead(emptySet()); consumers = 0; key = next
            if (next.uid.isNotBlank()) {
                val db = FirebaseFirestore.getInstance()
                listeners += db.collection("users").document(next.uid).collection("panel_reads").addSnapshotListener { snapshot, error ->
                    if (key == next && error == null) PanelReadRepository.updateRead(snapshot?.documents.orEmpty().map { it.id }.toSet())
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
                    listeners += EssenceEconomyRepository.redemptions.whereEqualTo("status", "PENDING").addSnapshotListener { snapshot, error ->
                        cashRequests = if (error == null) snapshot?.documents.orEmpty().associate { "support:payment_${it.id}" to PanelReadRepository.revision(it.data.orEmpty()) } else emptyMap()
                        publish(next, NotificationPanel.ADMINISTRATION, roleRequests + streamerRequests + cashRequests)
                    }
                    listeners += db.collection("moderator_requests").whereEqualTo("status", "PENDIENTE").addSnapshotListener { snapshot, error ->
                        roleRequests = if (error == null) snapshot?.documents.orEmpty().associate { "support:${it.id}" to PanelReadRepository.revision(it.data.orEmpty()) } else emptyMap()
                        publish(next, NotificationPanel.ADMINISTRATION, roleRequests + streamerRequests + cashRequests)
                    }
                    listeners += StreamerRepository.requests.whereEqualTo("status", "PENDING").addSnapshotListener { snapshot, error ->
                        streamerRequests = if (error == null) snapshot?.documents.orEmpty().filterNot { StreamerPublicationPolicy.isExpired(it.data.orEmpty()) }
                            .associate { "streamer:${it.id}" to PanelReadRepository.revision(it.data.orEmpty()) } else emptyMap()
                        publish(next, NotificationPanel.ADMINISTRATION, roleRequests + streamerRequests + cashRequests)
                    }
                }
            }
        }
        consumers++
    }

    @Synchronized private fun publish(source: Key, panel: NotificationPanel, events: Map<String, String>) {
        if (key == source) {
            queueVersions = queueVersions + (panel to events)
            // A pending payment shares its event with support. The latest conversation wins.
            PanelReadRepository.updateVersions(queueVersions[NotificationPanel.ADMINISTRATION].orEmpty() + queueVersions[NotificationPanel.SUPPORT].orEmpty())
            _queues.value = _queues.value + (panel to events.keys)
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
