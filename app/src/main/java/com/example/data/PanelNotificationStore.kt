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
    private val listeners = mutableListOf<ListenerRegistration>()

    @Synchronized fun acquire(next: Key) {
        if (key != next) {
            listeners.forEach { it.remove() }; listeners.clear()
            _queues.value = emptyMap(); consumers = 0; key = next
            if (next.uid.isNotBlank()) {
                val db = FirebaseFirestore.getInstance()
                val admin = RolePanelAccess.isAdministrator(next.role, next.adminClaim)
                if (RolePanelAccess.canOpen(RolePanel.MODERATION, next.role, next.secondary, next.adminClaim)) {
                    val query = if (admin) db.collection("support_reports") else db.collection("support_reports").whereEqualTo("staffVisible", true)
                    listeners += query.addSnapshotListener { snapshot, error ->
                        publish(next, NotificationPanel.SUPPORT, if (error == null) snapshot?.documents.orEmpty().filter {
                            UserPanelNotificationPolicy.staffNeedsAttention(it.data.orEmpty(), admin) && it.getString("userId") != next.uid
                        }.map { "support:${it.id}" }.toSet() else emptySet())
                    }
                }
                if (admin) {
                    var roleRequests = emptySet<String>()
                    var streamerRequests = emptySet<String>()
                    listeners += db.collection("moderator_requests").whereEqualTo("status", "PENDIENTE").addSnapshotListener { snapshot, error ->
                        roleRequests = if (error == null) snapshot?.documents.orEmpty().map { "support:${it.id}" }.toSet() else emptySet()
                        publish(next, NotificationPanel.ADMINISTRATION, roleRequests + streamerRequests)
                    }
                    listeners += StreamerRepository.requests.whereEqualTo("status", "PENDING").addSnapshotListener { snapshot, error ->
                        streamerRequests = if (error == null) snapshot?.documents.orEmpty().filterNot { StreamerPublicationPolicy.isExpired(it.data.orEmpty()) }
                            .map { "streamer:${it.id}" }.toSet() else emptySet()
                        publish(next, NotificationPanel.ADMINISTRATION, roleRequests + streamerRequests)
                    }
                }
            }
        }
        consumers++
    }

    @Synchronized private fun publish(source: Key, panel: NotificationPanel, events: Set<String>) {
        if (key == source) _queues.value = _queues.value + (panel to events)
    }

    @Synchronized fun release(source: Key) {
        if (key != source) return
        consumers--
        if (consumers <= 0) {
            listeners.forEach { it.remove() }; listeners.clear()
            _queues.value = emptyMap(); key = null; consumers = 0
        }
    }
}
