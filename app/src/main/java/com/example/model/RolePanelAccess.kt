package com.example.model

enum class RolePanel { ADMINISTRATION, MODERATION, CREATOR, STREAMER, SPONSOR }

/** Administrators inherit every role panel without changing their account role. */
object RolePanelAccess {
    fun canRedeemEssence(role: String, secondaryRole: String = "", adminClaim: Boolean = false): Boolean =
        role != "banned" && secondaryRole != "banned" &&
            adminClaim


    fun isAdministrator(role: String, adminClaim: Boolean = false): Boolean =
        adminClaim && role.trim().lowercase(java.util.Locale.ROOT) != "banned"

    fun canOpen(panel: RolePanel, role: String, secondaryRole: String = "", adminClaim: Boolean = false): Boolean {
        val roles = setOf(role, secondaryRole).map { it.trim().lowercase(java.util.Locale.ROOT) }
        if ("banned" in roles) return false
        if (isAdministrator(role, adminClaim)) return true
        return when (panel) {
            RolePanel.ADMINISTRATION -> false
            RolePanel.MODERATION -> "moderador" in roles
            RolePanel.STREAMER -> "streamer" in roles
            RolePanel.SPONSOR -> "patrocinador" in roles
            RolePanel.CREATOR -> roles.none { it in setOf("banned", "guest") }
        }
    }

    fun canCreateBuild(role: String, secondaryRole: String = "", adminClaim: Boolean = false): Boolean {
        if (role == "banned" || secondaryRole == "banned") return false
        if (isAdministrator(role, adminClaim)) return true
        return setOf(role, secondaryRole).any { it in setOf("moderador", "streamer", "creador", "creador_lvl2", "creador_lvl3", "creador_lvl4", "creador_lvl5") }
    }
}
