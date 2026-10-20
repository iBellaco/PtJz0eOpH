package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
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
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
    val freeUsersOnline: Int = 0,
    val premiumUsers: Int = 0,
    val premiumUsersOnline: Int = 0
)

/** Estado compacto de cuentas y actividad, actualizado desde el directorio de usuarios. */
@Composable
fun AdminUserPresenceCard(modifier: Modifier = Modifier, onUserClick: (Map<String, Any>) -> Unit = {}) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var users by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedRole by remember { mutableStateOf<String?>(null) }

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
    fun premium(user: Map<String, Any>) = com.example.model.PremiumAccessPolicy.hasActiveSubscription(user, now)
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
        freeUsers = users.count { roles(it).any { role -> role in setOf("gratis", "gratuito", "free") } && !premium(it) },
        freeUsersOnline = users.count { roles(it).any { role -> role in setOf("gratis", "gratuito", "free") } && !premium(it) && online(it) },
        premiumUsers = users.count(::premium),
        premiumUsersOnline = users.count { premium(it) && online(it) }
    )
    fun matchesSelection(user: Map<String, Any>, selection: String): Boolean = when (selection) {
        "__all__" -> true
        "admin" -> roles(user).any { it == "admin" || it == "administrador" }
        "moderador" -> roles(user).any { it == "moderador" || it == "moderator" }
        "patrocinador" -> roles(user).any { it == "patrocinador" || it == "sponsor" }
        "creador" -> roles(user).any { it.startsWith("creador") }
        "gratis" -> roles(user).any { it == "gratis" || it == "gratuito" || it == "free" } && !premium(user)
        "premium" -> premium(user)
        else -> selection in roles(user)
    }
    val selectedUsers = selectedRole?.let { selection ->
        users.filter { online(it) && matchesSelection(it, selection) }
            .sortedBy { ((it["name"] ?: it["username"] ?: it["displayName"] ?: "Usuario") as? String).orEmpty().lowercase() }
    }.orEmpty()

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
                MetricValue(Icons.Default.Sensors, metrics.online, HextechGreen, tr("conectados"), onClick = { selectedRole = "__all__" })
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RoleMetric(Icons.Default.AdminPanelSettings, tr("admins"), metrics.admins, metrics.adminsOnline, HextechGold, Modifier.weight(1f)) { selectedRole = "admin" }
                RoleMetric(Icons.Default.Shield, tr("moderadores"), metrics.moderators, metrics.moderatorsOnline, HextechCyan, Modifier.weight(1f)) { selectedRole = "moderador" }
                RoleMetric(Icons.Default.VerifiedUser, tr("patrocinadores"), metrics.sponsors, metrics.sponsorsOnline, HextechGreen, Modifier.weight(1f)) { selectedRole = "patrocinador" }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RoleMetric(Icons.Default.WorkspacePremium, tr("Premium"), metrics.premiumUsers, metrics.premiumUsersOnline, HextechGold, Modifier.weight(1f)) { selectedRole = "premium" }
                RoleMetric(Icons.Default.Groups, tr("streamers"), metrics.streamers, metrics.streamersOnline, HextechCyan, Modifier.weight(1f)) { selectedRole = "streamer" }
                RoleMetric(Icons.Default.VerifiedUser, tr("creadores"), metrics.creators, metrics.creatorsOnline, HextechGold, Modifier.weight(1f)) { selectedRole = "creador" }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RoleMetric(Icons.Default.People, tr("gratuitos"), metrics.freeUsers, metrics.freeUsersOnline, HextechCyan, Modifier.weight(1f)) { selectedRole = "gratis" }
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.weight(1f))
            }
        }
    }
    selectedRole?.let { selection ->
        val title = if (selection == "__all__") tr("Usuarios conectados") else tr("Conectados: ${selection.replaceFirstChar { it.uppercase() }}")
        AlertDialog(
            onDismissRequest = { selectedRole = null },
            title = { Text(title, color = HextechGold) },
            text = {
                if (selectedUsers.isEmpty()) Text(tr("No hay usuarios conectados en esta sección."), color = TextMuted)
                else LazyColumn(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.45f)) {
                    items(selectedUsers, key = { it["uid"]?.toString().orEmpty() }) { user ->
                        val displayName = (user["name"] ?: user["username"] ?: user["displayName"] ?: "Usuario").toString()
                        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(vertical = 2.dp)
                            .testTag("connected_user_${user["uid"]}")
                            .coachClickable { onUserClick(user); selectedRole = null }, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sensors, null, tint = HextechGreen, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(displayName, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            androidx.compose.material3.IconButton(onClick = {
                                copyPresence(context, userLine(user))
                            }, modifier = Modifier.size(40.dp).testTag("copy_connected_user_${user["uid"]}")) {
                                Icon(Icons.Default.ContentCopy, contentDescription = tr("Copiar usuario"), tint = HextechCyan)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { copyPresence(context, selectedUsers.joinToString("\n", transform = ::userLine)) }, modifier = Modifier.testTag("copy_connected_users_all")) {
                        Text(tr("Copiar todos"), color = HextechCyan)
                    }
                    TextButton(onClick = { selectedRole = null }) { Text(tr("Cerrar"), color = HextechCyan) }
                }
            }
        )
    }
}

private fun userLine(user: Map<String, Any>): String {
    val name = (user["name"] ?: user["username"] ?: user["displayName"] ?: "Usuario").toString()
    val role = (user["role"] as? String).orEmpty()
    return if (role.isBlank()) name else "$name · $role"
}

private fun copyPresence(context: Context, value: String) {
    (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.setPrimaryClip(ClipData.newPlainText("usuarios conectados", value))
    Toast.makeText(context, tr("Copiado"), Toast.LENGTH_SHORT).show()
}

@Composable
private fun MetricValue(icon: ImageVector, value: Int, color: androidx.compose.ui.graphics.Color, label: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Row(modifier.coachClickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Column {
            Text(value.toString(), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(label, color = TextMuted, fontSize = 8.sp, maxLines = 1)
        }
    }
}

@Composable
private fun RoleMetric(icon: ImageVector, label: String, total: Int, online: Int, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Column(modifier.coachClickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
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
