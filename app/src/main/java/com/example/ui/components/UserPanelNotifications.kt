package com.example.ui.components

import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.example.util.tr

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.model.RolePanelAccess
import com.example.util.AuthManager
import com.example.util.SubscriptionManager

/** One shared queue subscription supplies both navigation and the individual panel buttons. */
@Composable
fun userPanelNotificationSummary(): PanelNotificationState {
    val unread by SubscriptionManager.unreadMessageIds.collectAsStateWithLifecycle()
    val routes by SubscriptionManager.unreadMessageRoutes.collectAsStateWithLifecycle()
    val uid by SubscriptionManager.currentUserUid.collectAsStateWithLifecycle()
    val role by SubscriptionManager.userRole.collectAsStateWithLifecycle()
    val secondary by SubscriptionManager.secondaryRole.collectAsStateWithLifecycle()
    val claim by AuthManager.isAdminClaim.collectAsStateWithLifecycle()
    val signedIn by AuthManager.isSignedIn.collectAsStateWithLifecycle()
    val notices by AppNoticeManager.notices.collectAsStateWithLifecycle()
    val queues by PanelNotificationStore.queues.collectAsStateWithLifecycle()
    val key = remember(uid, signedIn, role, secondary, claim) { PanelNotificationStore.Key(if (signedIn) uid else "", role, secondary, claim) }
    DisposableEffect(key) {
        PanelNotificationStore.acquire(key)
        onDispose { PanelNotificationStore.release(key) }
    }
    if (key.uid.isBlank()) return PanelNotificationState()
    val sponsors = if (RolePanelAccess.isAdministrator(role, claim)) notices.filter {
        (it.tag.equals("Publicidad", true) || it.sponsorEmail.isNotBlank()) && !it.isApproved
    }.map { "notice:${it.id}" }.toSet() else emptySet()
    return PanelNotificationPolicy.combine(unread, routes, queues, sponsors)
}

@Composable
fun userPanelNotificationCount(): Int = userPanelNotificationSummary().total

/** The same animated red bell and counter is used on every panel. */
@Composable
fun PanelNotificationBadge(count: Int, panel: NotificationPanel) {
    if (count <= 0) return
    val transition = rememberInfiniteTransition(label = "panelBellPulse")
    val scale by transition.animateFloat(0.90f, 1.15f,
        infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "panelBellScale")
    val description = com.example.util.localizedString(com.example.R.string.panel_pending_notifications, count)
    androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp),
        modifier = Modifier.testTag("panel_notification_badge_${panel.name}").semantics {
            contentDescription = description
        }) {
        Icon(Icons.Default.Notifications, contentDescription = null, tint = com.example.ui.theme.DangerRed,
            modifier = Modifier.size(16.dp).testTag("panel_notification_icon_${panel.name}").graphicsLayer { scaleX = scale; scaleY = scale })
        Badge(containerColor = com.example.ui.theme.DangerRed, contentColor = Color.White) { Text(count.toString()) }
    }
}

/** Same bell pulse as the inbox, including draw-layer scaling without layout churn. */
@Composable
fun UserNotificationIcon(count: Int) {
    val showBadge = count > 0
    val scale = if (showBadge) {
        val transition = rememberInfiniteTransition(label = "bellPulseAnim")
        transition.animateFloat(0.90f, 1.15f,
            infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bellScale")
    } else remember { mutableFloatStateOf(1f) }
    BadgedBox(badge = {
        if (showBadge) Badge(containerColor = com.example.ui.theme.DangerRed, contentColor = Color.White,
            modifier = Modifier.testTag("user_navigation_badge")) { Text(count.toString()) }
    }) {
        Icon(if (showBadge) Icons.Default.Notifications else Icons.Default.Person,
            contentDescription = tr(if (showBadge) "Notificaciones" else "Usuario"),
            modifier = Modifier.size(24.dp).testTag("user_navigation_icon").graphicsLayer {
                if (showBadge) { scaleX = scale.value; scaleY = scale.value }
            })
    }
}
