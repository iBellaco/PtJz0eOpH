package com.example.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedbackReport(
    val id: String? = null,
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val photosBase64: List<String> = emptyList(),
    val type: String = "BUG",
    val title: String = "",
    val description: String = "",
    @SerialName("app_version") val appVersion: String = "",
    @SerialName("device_info") val deviceInfo: String = "",
    @SerialName("created_at") val createdAt: String? = null,
    val status: String? = null,
    @SerialName("is_completed") val isCompleted: Boolean? = null,
    @SerialName("admin_reply") val adminReply: String? = null,
    @SerialName("replied_at") val repliedAt: String? = null,
    @SerialName("replied_by") val repliedBy: String? = null,
    @SerialName("replied_email") val repliedEmail: String? = null
) {
    val hasAdminReply: Boolean
        get() = !adminReply.isNullOrBlank()
    val parsedUserName: String?
        get() {
            if (userName.isNotBlank()) return userName
            val match = Regex("Usuario: (.*?)\n").find(description)
                ?: Regex("\\[Usuario: (.*?)\\]").find(description)
                ?: Regex("Invocador: (.*?)\n").find(description)
            return match?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() && !it.contains("@") }
        }

    val parsedEmail: String?
        get() {
            if (userEmail.isNotBlank()) return userEmail
            val match = Regex("Correo de contacto: (.*?)\n\n").find(description)
                ?: Regex("Correo de contacto: (.*?)\n").find(description)
            return match?.groupValues?.get(1)?.trim()
        }

    val cleanDescription: String
        get() = com.example.data.SupportTicketPresentation.cleanBody(description)
}
