package com.example.data

/** One event may be reachable from multiple panels, but is counted once in navigation. */
enum class NotificationPanel { INBOX, HISTORY, CREATOR, STREAMER, ADMINISTRATION, SPONSOR_MODERATION, SUPPORT, SPONSOR, PLANS }

data class PanelNotificationState(val events: Map<NotificationPanel, Set<String>> = emptyMap()) {
    fun count(panel: NotificationPanel): Int = events[panel].orEmpty().size
    val total: Int get() = events.values.flatten().toSet().size
}

object PanelNotificationPolicy {
    fun messagePanels(message: Map<String, Any>): Set<NotificationPanel> {
        val explicit = (message["panel"] as? String).orEmpty().uppercase(java.util.Locale.ROOT)
        val panel = NotificationPanel.entries.firstOrNull { it.name == explicit }
        val id = (message["id"] as? String).orEmpty()
        val title = (message["title"] as? String).orEmpty()
        val legacy = when {
            id.startsWith("streamer_review_") || title == "Publicación de streamer" -> NotificationPanel.STREAMER
            title.contains("Límite de Suscriptores", true) || title.contains("Suscripción con Esencia Naranja", true) -> NotificationPanel.CREATOR
            else -> null
        }
        return setOfNotNull(NotificationPanel.INBOX, panel ?: legacy)
    }

    fun combine(unread: Set<String>, routes: Map<String, Set<NotificationPanel>>, queues: Map<NotificationPanel, Set<String>>,
        pendingSponsors: Set<String>): PanelNotificationState {
        val result = queues.mapValues { it.value.toMutableSet() }.toMutableMap()
        for (id in unread) for (panel in routes[id] ?: setOf(NotificationPanel.INBOX)) result.getOrPut(panel) { mutableSetOf() }.add(id)
        result.getOrPut(NotificationPanel.SPONSOR_MODERATION) { mutableSetOf() }.addAll(pendingSponsors)
        return PanelNotificationState(result.mapValues { it.value.toSet() })
    }
}
