package com.example.data

/** Collapse legacy/current copies before testing unread state; content revisions notify once. */
object InboxPhoneNotificationPolicy {
    fun candidates(messages: List<Map<String, Any>>): Map<String, String> = messages
        .mapNotNull { message -> InboxNotificationPolicy.key(message)?.let { it to message } }
        .groupBy({ it.first }, { it.second })
        .mapValues { (_, copies) -> copies.sortedBy { InboxMessageOrder.latestMessageAt(it) }.last() }
        .filterValues { message -> InboxNotificationPolicy.unreadKeys(listOf(message)).isNotEmpty() }
        .mapValues { (key, message) -> PanelReadRepository.key(key,
            PanelReadRepository.revision(message) + "|" + listOf(message["title"], message["body"], message["message"]).joinToString("|")) }
}
