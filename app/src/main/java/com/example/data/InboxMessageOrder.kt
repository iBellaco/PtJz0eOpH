package com.example.data

/** Content activity determines inbox position; reading a message never moves it. */
object InboxMessageOrder {
    private val activityKeys = listOf("lastMessageAt", "lastUserMessageAt", "repliedAt", "replied_at",
        "timestamp", "timestampMillis", "createdAt", "created_at", "date", "fecha")

    fun latestMessageAt(message: Map<String, Any>): Long {
        val fields = activityKeys.mapNotNull { SupportReportDecoder.timestampMillis(message[it]) }
        val conversation = SupportConversationPolicy.decode(message["conversation"])
            .map { it.timestampMillis }.filter { it > 0 }
        return (fields + conversation).maxOrNull() ?: 0L
    }

    fun newestFirst(messages: List<Map<String, Any>>): List<Map<String, Any>> =
        messages.sortedWith(compareByDescending<Map<String, Any>> { latestMessageAt(it) }
            .thenBy { it["id"] as? String ?: "" })
}
