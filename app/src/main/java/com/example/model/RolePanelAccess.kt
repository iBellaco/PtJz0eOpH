package com.example.model

enum class RolePanel { ADMINISTRATION, MODERATION, CREATOR, STREAMER, SPONSOR }

/** Administrators inherit every role panel without changing their account role. */
object RolePanelAccess {
    fun isAdministrator(role: String, adminClaim: Boolean = false): Boolean =
        role.trim().lowercase(java.util.Locale.ROOT) == "admin" || adminClaim

    fun canOpen(panel: RolePanel, role: String, secondaryRole: String = "", adminClaim: Boolean = false): Boolean {
        if (isAdministrator(role, adminClaim)) return true
        val roles = setOf(role, secondaryRole).map { it.trim().lowercase(java.util.Locale.ROOT) }
        return when (panel) {
            RolePanel.ADMINISTRATION -> false
            RolePanel.MODERATION -> "moderador" in roles
            RolePanel.STREAMER -> "streamer" in roles
            RolePanel.SPONSOR -> "patrocinador" in roles
            RolePanel.CREATOR -> roles.any { it in setOf("moderador", "streamer", "creador", "creador_lvl2", "creador_lvl3", "creador_lvl4", "creador_lvl5") }
        }
    }
}
