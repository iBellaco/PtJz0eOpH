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
import androidx.compose.ui.unit.dp
import com.example.util.tr

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.model.RolePanel
import com.example.model.RolePanelAccess
import com.example.util.AuthManager
import com.example.util.SubscriptionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/** Observe only queues authorized for this account; listeners are removed on role/session changes. */
@Composable
fun userPanelNotificationCount(): Int {
    val unreadIds by SubscriptionManager.unreadMessageIds.collectAsStateWithLifecycle()
    val role by SubscriptionManager.userRole.collectAsStateWithLifecycle()
    val secondary by SubscriptionManager.secondaryRole.collectAsStateWithLifecycle()
    val adminClaim by AuthManager.isAdminClaim.collectAsStateWithLifecycle()
    val signedIn by AuthManager.isSignedIn.collectAsStateWithLifecycle()
    val notices by AppNoticeManager.notices.collectAsStateWithLifecycle()
    val uid = if (signedIn) FirebaseAuth.getInstance().currentUser?.uid.orEmpty() else ""
    var queues by remember(uid, role, secondary, adminClaim) { mutableStateOf<Map<String, Set<String>>>(emptyMap()) }
    val admin = RolePanelAccess.isAdministrator(role, adminClaim)
    DisposableEffect(uid, role, secondary, adminClaim) {
        val listeners = mutableListOf<com.google.firebase.firestore.ListenerRegistration>()
        if (uid.isNotBlank()) {
            val db = FirebaseFirestore.getInstance()
            if (RolePanelAccess.canOpen(RolePanel.MODERATION, role, secondary, adminClaim)) {
                val query = if (admin) db.collection("support_reports") else db.collection("support_reports").whereEqualTo("staffVisible", true)
                listeners += query.addSnapshotListener { snapshot, error ->
                    queues = queues + ("support" to if (error == null) snapshot?.documents.orEmpty().filter { doc ->
                        val data = doc.data.orEmpty()
                        UserPanelNotificationPolicy.staffNeedsAttention(data, admin) && data["userId"] != uid
                    }.map { "support:${it.id}" }.toSet() else emptySet())
                }
            }
            if (admin) {
                listeners += db.collection("moderator_requests").whereEqualTo("status", "PENDIENTE").addSnapshotListener { snapshot, error ->
                    queues = queues + ("roles" to if (error == null) snapshot?.documents.orEmpty().map { "support:${it.id}" }.toSet() else emptySet())
                }
                listeners += StreamerRepository.requests.whereEqualTo("status", "PENDING").addSnapshotListener { snapshot, error ->
                    queues = queues + ("streamers" to if (error == null) snapshot?.documents.orEmpty()
                        .filterNot { StreamerPublicationPolicy.isExpired(it.data.orEmpty()) }.map { "streamer:${it.id}" }.toSet() else emptySet())
                }
            }
        }
        onDispose { listeners.forEach { it.remove() } }
    }
    if (uid.isBlank()) return 0
    val pendingAds = if (admin) notices.filter { !it.isApproved }.map { "notice:${it.id}" } else emptyList()
    return (unreadIds + queues.values.flatten() + pendingAds).size
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
