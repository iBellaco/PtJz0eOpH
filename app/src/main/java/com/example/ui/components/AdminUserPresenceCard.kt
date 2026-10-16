package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechGreen
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.util.tr
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

private data class PresenceMetrics(
    val registered: Int = 0,
    val signedIn: Int = 0,
    val online: Int = 0,
    val moderators: Int = 0,
    val moderatorsOnline: Int = 0,
    val admins: Int = 0,
    val adminsOnline: Int = 0,
    val sponsors: Int = 0,
    val sponsorsOnline: Int = 0,
    val streamers: Int = 0,
    val streamersOnline: Int = 0,
    val creators: Int = 0,
    val creatorsOnline: Int = 0,
    val freeUsers: Int = 0,
    val freeUsersOnline: Int = 0
)

/** Estado compacto de cuentas y actividad, actualizado desde el directorio de usuarios. */
@Composable
fun AdminUserPresenceCard(modifier: Modifier = Modifier) {
    var users by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }

    DisposableEffect(Unit) {
        val registration = FirebaseFirestore.getInstance().collection("users")
            .addSnapshotListener { snapshot, _ ->
                users = snapshot?.documents?.map { document ->
                    document.data.orEmpty() + ("uid" to document.id)
                }.orEmpty()
            }
        onDispose { registration.remove() }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000L)
            now = System.currentTimeMillis()
        }
    }

    fun roles(user: Map<String, Any>): Set<String> = setOf(
        (user["role"] as? String).orEmpty(),
        (user["secondaryRole"] as? String).orEmpty()
    ).map { it.lowercase() }.filter(String::isNotBlank).toSet()
    fun online(user: Map<String, Any>): Boolean {
        val active = (user["last_active"] as? Number)?.toLong()
            ?: (user["lastActiveTimestamp"] as? Number)?.toLong() ?: 0L
        return ((user["is_online"] as? Boolean) == true) && active > 0L && now - active in 0 until 10 * 60 * 1000L
    }
    fun countRole(role: String) = users.count { role in roles(it) }
    fun countRoleOnline(role: String) = users.count { role in roles(it) && online(it) }
    val metrics = PresenceMetrics(
        registered = users.distinctBy { it["uid"] }.size,
        signedIn = users.count { (it["is_online"] as? Boolean) == true },
        online = users.count(::online),
        moderators = countRole("moderador") + countRole("moderator"),
        moderatorsOnline = countRoleOnline("moderador") + countRoleOnline("moderator"),
        admins = countRole("admin") + countRole("administrador"),
        adminsOnline = countRoleOnline("admin") + countRoleOnline("administrador"),
        sponsors = countRole("patrocinador") + countRole("sponsor"),
        sponsorsOnline = countRoleOnline("patrocinador") + countRoleOnline("sponsor"),
        streamers = countRole("streamer"),
        streamersOnline = countRoleOnline("streamer"),
        creators = users.count { roles(it).any { role -> role.startsWith("creador") } },
        creatorsOnline = users.count { roles(it).any { role -> role.startsWith("creador") } && online(it) },
        freeUsers = users.count { roles(it).any { role -> role in setOf("gratis", "gratuito", "free") } },
        freeUsersOnline = users.count { roles(it).any { role -> role in setOf("gratis", "gratuito", "free") } && online(it) }
    )

    Card(
        modifier = modifier.fillMaxWidth().padding(12.dp).testTag("admin_user_presence_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HextechSurface)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Groups, null, tint = HextechCyan, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(tr("Actividad de usuarios"), color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("${metrics.registered} ${tr("registrados")}", color = TextMuted, fontSize = 10.sp)
                }
                MetricValue(Icons.Default.Sensors, metrics.online, HextechGreen, tr("conectados"))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RoleMetric(Icons.Default.AdminPanelSettings, tr("admins"), metrics.admins, metrics.adminsOnline, HextechGold, Modifier.weight(1f))
                RoleMetric(Icons.Default.Shield, tr("moderadores"), metrics.moderators, metrics.moderatorsOnline, HextechCyan, Modifier.weight(1f))
                RoleMetric(Icons.Default.VerifiedUser, tr("patrocinadores"), metrics.sponsors, metrics.sponsorsOnline, HextechGreen, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RoleMetric(Icons.Default.Groups, tr("streamers"), metrics.streamers, metrics.streamersOnline, HextechCyan, Modifier.weight(1f))
                RoleMetric(Icons.Default.VerifiedUser, tr("creadores"), metrics.creators, metrics.creatorsOnline, HextechGold, Modifier.weight(1f))
                RoleMetric(Icons.Default.People, tr("gratuitos"), metrics.freeUsers, metrics.freeUsersOnline, HextechCyan, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MetricValue(icon: ImageVector, value: Int, color: androidx.compose.ui.graphics.Color, label: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Column {
            Text(value.toString(), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(label, color = TextMuted, fontSize = 8.sp, maxLines = 1)
        }
    }
}

@Composable
private fun RoleMetric(icon: ImageVector, label: String, total: Int, online: Int, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = color, modifier = Modifier.size(15.dp))
        Text("$total", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(
            "$online ${tr("en línea")}",
            color = if (online > 0) HextechGreen else TextMuted,
            fontSize = 8.sp,
            maxLines = 1
        )
        Text(label, color = TextMuted, fontSize = 8.sp, maxLines = 1)
    }
}
