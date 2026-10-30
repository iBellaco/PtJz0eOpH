package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.model.RolePanel
import com.example.model.RolePanelAccess
import com.example.util.AuthManager
import com.example.util.SubscriptionManager

/** UI follows the same signed claim and ban checks; labels and email never grant access. */
@Composable
fun sessionPanelAccess(panel: RolePanel = RolePanel.ADMINISTRATION): Boolean {
    val role by SubscriptionManager.userRole.collectAsState()
    val secondary by SubscriptionManager.secondaryRole.collectAsState()
    val banned by SubscriptionManager.isBanned.collectAsState()
    val admin by AuthManager.isAdminClaim.collectAsState()
    // Keep the last verified claim during a token refresh; a different session
    // clears it immediately in AuthManager. This avoids destroying open forms.
    return !banned && RolePanelAccess.canOpen(panel, role, secondary, admin)
}
