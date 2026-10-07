package com.example.ui.components

import com.example.ui.components.CoachFilterChip as FilterChip
import com.example.ui.components.CoachButton as Button
import com.example.ui.components.CoachTextButton as TextButton
import com.example.ui.components.CoachIconButton as IconButton
import com.example.util.tr
import androidx.compose.foundation.BorderStroke
import com.example.data.sync.BestBuildWrScraper
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.border
import com.example.ui.components.coachClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.AuthManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class UserFilterTab(val label: String) {
    ALL("Todos"),
    PREMIUM("👑 Premium"),
    FREE("🎮 Gratis"),
    ONLINE("🟢 Online"),
    ADMINS("🛡️ Admins"),
    MODS("🛡️ Mods"),
    SPONSORS("💎 Patrocinador"),
    BANNED("⛔ Baneados"),
    STREAMERS("🎥 Streamers"),
    CREATORS("✨ Creadores")
}

@Composable
fun EnhancedUserManagementPanel(
    isMinimized: Boolean = false,
    onToggleMinimize: () -> Unit = {}
) {
    val context = LocalContext.current
    var users by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(UserFilterTab.ALL) }

    var pendingRequestsCount by remember { mutableStateOf(0) }
    var showRequestsDialog by remember { mutableStateOf(false) }

    val userRoleForRequests = com.example.util.SubscriptionManager.userRole.collectAsState().value
    val isAdminUserForRequests = userRoleForRequests == "admin" || com.example.util.AuthManager.isCurrentUserAdmin()

    if (isAdminUserForRequests) {
        DisposableEffect(Unit) {
            val listener = FirebaseFirestore.getInstance().collection("moderator_requests")
                .whereEqualTo("status", "PENDIENTE")
                .addSnapshotListener { snapshot, e ->
                    if (snapshot != null) {
                        pendingRequestsCount = snapshot.size()
                    }
                }
            onDispose { listener.remove() }
        }
    }

    // Dialogs
    var selectedUserForManage by remember { mutableStateOf<Map<String, Any>?>(null) }
    var selectedUserForAvatarGift by remember { mutableStateOf<Map<String, Any>?>(null) }

    BackHandler(enabled = selectedUserForManage != null || selectedUserForAvatarGift != null) {
        selectedUserForManage = null
        selectedUserForAvatarGift = null
    }

    var listenerReg by remember { mutableStateOf<com.google.firebase.firestore.ListenerRegistration?>(null) }

    fun loadUsers() {
        isLoading = true
        errorMessage = null
        listenerReg?.remove()
        listenerReg = FirebaseFirestore.getInstance().collection("users")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    errorMessage = "Error al cargar usuarios: ${e.message}"
                    isLoading = false
                    isRefreshing = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.map { doc ->
                        val data = doc.data?.toMutableMap() ?: mutableMapOf()
                        data["uid"] = doc.id
                        data
                    }
                    users = list
                    isLoading = false
                    isRefreshing = false
                }
            }
    }

    DisposableEffect(Unit) {
        onDispose {
            listenerReg?.remove()
        }
    }

    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        loadUsers()
        while (true) {
            kotlinx.coroutines.delay(1000L)
            currentTime = System.currentTimeMillis()
        }
    }

    // Cálculos de métricas en tiempo real. Los roles secundarios también cuentan en su categoría.
    val now = currentTime
    val onlineThreshold = 10 * 60 * 1000L
    fun roleSet(user: Map<String, Any>): Set<String> = setOf(
        (user["role"] as? String).orEmpty().lowercase(),
        (user["secondaryRole"] as? String).orEmpty().lowercase()
    ).filter(String::isNotBlank).toSet()
    fun userOnline(user: Map<String, Any>): Boolean {
        val explicitOnline = user["is_online"] as? Boolean ?: false
        val lastActive = (user["last_active"] as? Number)?.toLong()
            ?: (user["lastActiveTimestamp"] as? Number)?.toLong() ?: 0L
        return explicitOnline && lastActive > 0L && now - lastActive in 0 until onlineThreshold
    }
    fun userBanned(user: Map<String, Any>): Boolean =
        (user["banned"] as? Boolean) == true || "banned" in roleSet(user)
    fun hasRole(user: Map<String, Any>, vararg roles: String): Boolean =
        roleSet(user).any { current -> roles.any { current == it } }

    val totalUsers = users.mapNotNull { (it["uid"] as? String)?.takeIf(String::isNotBlank) }.distinct().size
    val onlineUsers = users.count(::userOnline)
    val premiumUsers = users.count { com.example.model.PremiumAccessPolicy.isActiveAccount(it, now) && !userBanned(it) }
    val adminUsers = users.count { hasRole(it, "admin", "administrador") && !userBanned(it) }
    val modUsers = users.count { hasRole(it, "moderador", "moderator") && !userBanned(it) }
    val sponsorUsers = users.count { hasRole(it, "patrocinador", "sponsor") && !userBanned(it) }
    val streamerUsers = users.count { hasRole(it, "streamer") && !userBanned(it) }
    val creatorUsers = users.count { hasRole(it, "creador", "creador_lvl2", "creador_lvl3", "creador_lvl4", "creador_lvl5") && !userBanned(it) }
    val bannedUsers = users.count(::userBanned)
    val freeUsers = users.count { !com.example.model.PremiumAccessPolicy.isActiveAccount(it, now) && !userBanned(it) }

    // Filtrado de usuarios
    val filteredUsers = remember(users, searchQuery, selectedFilter, now) {
        users.filter { user ->
            val name = (user["name"] as? String ?: "").lowercase()
            val email = (user["email"] as? String ?: "").lowercase()
            val uid = (user["uid"] as? String ?: "").lowercase()
            val query = searchQuery.trim().lowercase()

            val matchesQuery = query.isEmpty() || name.contains(query) || email.contains(query) || uid.contains(query)

            val isPrem = com.example.model.PremiumAccessPolicy.isActiveAccount(user, now)
            val isOnline = userOnline(user)
            val isBanned = userBanned(user)

            val matchesTab = when (selectedFilter) {
                UserFilterTab.ALL -> true
                UserFilterTab.PREMIUM -> isPrem && !isBanned
                UserFilterTab.FREE -> !isPrem && !isBanned
                UserFilterTab.ONLINE -> isOnline
                UserFilterTab.ADMINS -> hasRole(user, "admin", "administrador") && !isBanned
                UserFilterTab.MODS -> hasRole(user, "moderador", "moderator") && !isBanned
                UserFilterTab.SPONSORS -> hasRole(user, "patrocinador", "sponsor") && !isBanned
                UserFilterTab.STREAMERS -> hasRole(user, "streamer") && !isBanned
                UserFilterTab.CREATORS -> hasRole(user, "creador", "creador_lvl2", "creador_lvl3", "creador_lvl4", "creador_lvl5") && !isBanned
                UserFilterTab.BANNED -> isBanned
            }

            matchesQuery && matchesTab
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Bloque de monitoreo y métricas minimizable
        AnimatedVisibility(visible = !isMinimized) {
            Column {
                // KPI Cards Bar
                AdminKpiCards(
                    total = totalUsers,
                    premium = premiumUsers,
                    free = freeUsers,
                    online = onlineUsers,
                    onRefresh = {
                        isRefreshing = true
                        loadUsers()
                    },
                    isRefreshing = isRefreshing
                )

                ServerScraperHealthCard()
            }
        }

        // Barra informativa de estado minimizado y botón para alternar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = com.example.util.tr(if (isMinimized) "Mostrando vista completa de usuarios" else "Monitoreo y herramientas activas"),
                color = if (isMinimized) HextechGold else TextMuted,
                fontSize = 11.sp,
                fontWeight = if (isMinimized) FontWeight.SemiBold else FontWeight.Normal
            )
            TextButton(
                onClick = onToggleMinimize,
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = if (isMinimized) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = HextechCyan,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = com.example.util.tr(if (isMinimized) "Ver Monitoreo / Reportes" else "Minimizar Monitoreo"),
                    color = HextechCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Buscador y Chips de Filtro
        Surface(
            color = HextechDarkBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(tr("Buscar por nombre, email o UID..."), color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HextechGold) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = com.example.util.trNullable("Limpiar"), tint = TextMuted)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HextechGold,
                        unfocusedBorderColor = HextechCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                if (isAdminUserForRequests && pendingRequestsCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    com.example.ui.components.CoachClickableSurface(
                        onClick = { showRequestsDialog = true },
                        color = HextechGold.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HextechGold.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PendingActions,
                                    contentDescription = null,
                                    tint = HextechGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = com.example.util.tr("Tienes $pendingRequestsCount solicitudes de moderador pendientes"),
                                    color = HextechGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = tr("Revisar"),
                                color = HextechCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                if (showRequestsDialog) {
                    AdminModeratorRequestsDialog(onDismiss = { showRequestsDialog = false })
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Chips de filtro con scroll horizontal
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    UserFilterTab.values().forEach { tab ->
                        val isSelected = selectedFilter == tab
                        val count = when (tab) {
                            UserFilterTab.ALL -> totalUsers
                            UserFilterTab.PREMIUM -> premiumUsers
                            UserFilterTab.FREE -> freeUsers
                            UserFilterTab.ONLINE -> onlineUsers
                            UserFilterTab.ADMINS -> adminUsers
                            UserFilterTab.MODS -> modUsers
                            UserFilterTab.SPONSORS -> sponsorUsers
                            UserFilterTab.STREAMERS -> streamerUsers
                            UserFilterTab.CREATORS -> creatorUsers
                            UserFilterTab.BANNED -> bannedUsers
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = tab },
                            label = {
                                Text(
                                    text = com.example.util.tr("${tab.label} ($count)"),
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HextechGold.copy(alpha = 0.25f),
                                selectedLabelColor = HextechGold,
                                containerColor = HextechSurfaceBg,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) HextechGold else HextechCardBorder
                            )
                        )
                    }
                }
            }
        }

        // Lista de Usuarios
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = HextechGold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(tr("Cargando usuarios y membresías..."), color = TextMuted, fontSize = 13.sp)
                }
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = DangerRed, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = com.example.util.tr(errorMessage!!), color = DangerRed, textAlign = TextAlign.Center, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { loadUsers() }, colors = ButtonDefaults.buttonColors(containerColor = HextechGold)) {
                        Text(tr("Reintentar"), color = HextechDarkBg)
                    }
                }
            }
        } else if (filteredUsers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PersonSearch, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(tr("No se encontraron usuarios en esta categoría"), color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    if (searchQuery.isNotEmpty()) {
                        Text(tr("Intenta con otro término de búsqueda."), color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredUsers, key = { it["uid"] as? String ?: "" }) { user ->
                    EnhancedUserAdminCard(
                        user = user,
                        currentTime = currentTime,
                        onManageClick = { selectedUserForManage = user },
                        onAvatarGiftClick = { selectedUserForAvatarGift = user },
                        onResetSlotsClick = {
                            val uid = user["uid"] as? String ?: return@EnhancedUserAdminCard
                            resetUserHardwareSlots(context, uid) {
                                Toast.makeText(context, com.example.util.appTr("Slots de hardware liberados exitosamente"), Toast.LENGTH_SHORT).show()
                                loadUsers()
                            }
                        }
                    )
                }
            }
        }
    }

    // Modal Detallado de Gestión de Usuario
    selectedUserForManage?.let { user ->
        UserDetailManagementDialog(
            user = user,
            onDismiss = { selectedUserForManage = null },
            onUserUpdated = { updatedMap ->
                users = users.map { if (it["uid"] == updatedMap["uid"]) updatedMap else it }
                selectedUserForManage = updatedMap
            },
            onOpenAvatarGift = {
                selectedUserForAvatarGift = user
            },
            onReloadAll = { loadUsers() }
        )
    }

    // Modal de Galería para Regalar Avatares
    selectedUserForAvatarGift?.let { user ->
        AdminAvatarGiftDialog(
            user = user,
            onDismiss = { selectedUserForAvatarGift = null },
            onAvatarGifted = { newAvatarId ->
                loadUsers()
            }
        )
    }
}

@Composable
private fun AdminKpiCards(
    total: Int,
    premium: Int,
    free: Int,
    online: Int,
    onRefresh: () -> Unit,
    isRefreshing: Boolean
) {
    Surface(
        color = HextechSurfaceBg,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr("Métricas Generales de Usuarios"),
                    color = HextechGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.coachClickable { if (!isRefreshing) onRefresh() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = com.example.util.trNullable("Refrescar"),
                        tint = if (isRefreshing) HextechGold else TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = com.example.util.tr(if (isRefreshing) "Actualizando..." else "Refrescar"),
                        color = if (isRefreshing) HextechGold else TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total
                KpiItemCard(
                    title = "Registrados",
                    value = total.toString(),
                    icon = Icons.Default.People,
                    accentColor = Color(0xFF60A5FA),
                    modifier = Modifier.weight(1f)
                )

                // Premium
                KpiItemCard(
                    title = "Premium",
                    value = premium.toString(),
                    icon = Icons.Default.WorkspacePremium,
                    accentColor = HextechGold,
                    modifier = Modifier.weight(1f)
                )

                // Gratuitos
                KpiItemCard(
                    title = "Gratuitos",
                    value = free.toString(),
                    icon = Icons.Default.SportsEsports,
                    accentColor = HextechCyan,
                    modifier = Modifier.weight(1f)
                )

                // Online
                KpiItemCard(
                    title = "En Línea",
                    value = online.toString(),
                    icon = Icons.Default.Sensors,
                    accentColor = Color(0xFF00FF7F),
                    isLive = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun KpiItemCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isLive: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlphaPulse"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(HextechDarkBg)
            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isLive) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00FF7F).copy(alpha = alphaAnim))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(13.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = com.example.util.tr(value),
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
            Text(
                text = com.example.util.tr(title),
                color = TextMuted,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EnhancedUserAdminCard(
    user: Map<String, Any>,
    currentTime: Long = System.currentTimeMillis(),
    onManageClick: () -> Unit,
    onAvatarGiftClick: () -> Unit,
    onResetSlotsClick: () -> Unit
) {
    val context = LocalContext.current
    val uid = user["uid"] as? String ?: ""
    val name = user["name"] as? String ?: "Sin Nombre"
    val email = user["email"] as? String ?: ""
    val role = user["role"] as? String ?: "free"
    val isBanned = (user["banned"] as? Boolean) == true || role == "banned"
    val avatarId = user["avatarId"] as? String ?: "default_poro"
    val rankBorder = user["rankBorder"] as? String ?: "NONE"
    val premiumUntil = com.example.model.PremiumAccessPolicy.deadline(user["premiumUntil"])

    val registeredDevices = (user["registeredDevices"] as? List<*>) ?: emptyList<Any>()
    val deviceSlotsUsed = registeredDevices.size.coerceAtLeast(0)

    val unlockedAvatars = (user["unlockedAvatars"] as? List<*>)?.mapNotNull { it?.toString() } ?: listOf("default_poro")
    val unlockedCount = unlockedAvatars.size

    val lastActiveTimestamp = (user["last_active"] as? Number)?.toLong() ?: (user["lastActiveTimestamp"] as? Number)?.toLong() ?: 0L
    val now = currentTime
    val explicitOnline = user["is_online"] as? Boolean ?: false
    val isOnline = explicitOnline && lastActiveTimestamp > 0L && now - lastActiveTimestamp in 0 until 10 * 60 * 1000L

    val isPremiumActive = com.example.model.PremiumAccessPolicy.isActiveAccount(user, now)

    val cardBorderColor = when {
        role == "admin" -> HextechGold.copy(alpha = 0.6f)
        isBanned -> DangerRed.copy(alpha = 0.5f)
        role == "moderador" -> Color(0xFF10B981).copy(alpha = 0.5f)
        role in listOf("creador", "creador_lvl2", "creador_lvl3", "creador_lvl4", "creador_lvl5") -> Color(0xFFA855F7).copy(alpha = 0.5f)
        role == "streamer" -> Color(0xFFEC4899).copy(alpha = 0.5f)
        isPremiumActive -> HextechCyan.copy(alpha = 0.4f)
        else -> HextechCardBorder
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = HextechSurfaceBg,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Fila superior: Avatar + Info Usuario + Badge de Rol + Estado de Conexión
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar con badge de estado online
                Box(
                    modifier = Modifier.padding(
                        horizontal = if (role == "admin") 6.dp else 0.dp,
                        vertical = if (role == "admin") 4.dp else 0.dp
                    ),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    UserAvatarView(
                        avatarId = avatarId,
                        size = if (role == "admin") 34.dp else 46.dp,
                        fallbackInitial = name.take(1).uppercase(),
                        rankBorder = rankBorder,
                        isAdmin = (role == "admin"),
                        role = role,
                        secondaryRole = user["secondaryRole"] as? String
                    )
                    // Indicador de conexión verde/gris
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (isOnline) Color(0xFF00FF7F) else Color(0xFF6B7280))
                            .border(1.5.dp, HextechDarkBg, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Datos de Usuario
                val isVerifiedUser = (user["isVerified"] as? Boolean) == true || (user["verified"] as? Boolean) == true || role == "admin" || role == "moderador"
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.coachClickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Usuario" , name))
                            Toast.makeText(context, com.example.util.appTr("Usuario copiado: $name"), Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(
                            text = com.example.util.tr(name),
                            fontWeight = FontWeight.Bold,
                            color = if (role == "admin") HextechGold else TextPrimary,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isVerifiedUser) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = com.example.util.trNullable("Verificado"),
                                tint = if (role == "admin") HextechGold else HextechCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Role Badge debajo del usuario
                    RoleBadge(role = role, isPremiumActive = isPremiumActive, isBanned = isBanned)

                    if (email.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = com.example.util.tr(email),
                            color = TextSecondary,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.coachClickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Correo" , email))
                                Toast.makeText(context, com.example.util.appTr("Correo copiado: $email"), Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    // UID y Estado de Conexión en vivo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // UID copiable con un toque
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.coachClickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("UID" , uid))
                                Toast.makeText(context, com.example.util.appTr("UID copiado"), Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text(
                                text = com.example.util.tr("UID: ${uid.take(10)}..."),
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(Icons.Default.ContentCopy, contentDescription = com.example.util.trNullable("Copiar UID"), tint = TextMuted, modifier = Modifier.size(10.dp))
                        }

                        // Badge de Conexión / Última Conexión en Vivo
                        Surface(
                            color = if (isOnline) Color(0xFF00FF7F).copy(alpha = 0.15f) else HextechDarkBg,
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                0.5.dp,
                                if (isOnline) Color(0xFF00FF7F).copy(alpha = 0.5f) else HextechCardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(if (isOnline) Color(0xFF00FF7F) else Color(0xFF9CA3AF))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = com.example.util.tr(formatLastConnection(lastActiveTimestamp, isOnline, now)),
                                    color = if (isOnline) Color(0xFF00FF7F) else TextMuted,
                                    fontSize = 9.5.sp,
                                    fontWeight = if (isOnline) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = HextechCardBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Fila de Estado: Suscripción & Slots de Hardware
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info de Suscripción con conteo en vivo de segundos
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (isPremiumActive) HextechCyan else TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = com.example.util.tr(getSubscriptionStatusText(role, premiumUntil, isPremiumActive, now, (user["secondaryRole"] as? String).orEmpty())),
                            fontSize = 11.sp,
                            color = if (isPremiumActive) HextechCyan else TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (isPremiumActive && !com.example.model.PremiumAccessPolicy.isLifetime(role, (user["secondaryRole"] as? String).orEmpty()) && premiumUntil != null && premiumUntil > 0L) {
                        Text(
                            text = com.example.util.tr(formatExpirationDateDetailed(premiumUntil)),
                            color = if (premiumUntil - now < 3 * 86400000L) Color(0xFFFBBF24) else TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                // Info de Slots de Dispositivos, Avatares y Esencia Azul
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val blueEssence = (user["blueEssence"] as? Number)?.toLong() ?: 0L
                    // Badge de Esencia Azul
                    Surface(
                        color = HextechDarkBg,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF0EA5E9).copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = com.example.R.drawable.ic_blue_essence),
                                contentDescription = com.example.util.tr("Esencia Azul"),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = com.example.util.tr("$blueEssence EA"),
                                fontSize = 10.sp,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Badge de Slots de Dispositivo
                    Surface(
                        color = HextechDarkBg,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, HextechCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Smartphone, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = com.example.util.tr("$deviceSlotsUsed/2 slots"),
                                fontSize = 10.sp,
                                color = if (deviceSlotsUsed >= 2) DangerRed else TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Badge de Avatares desbloqueados
                    Surface(
                        color = HextechDarkBg,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, HextechCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = HextechGold, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = com.example.util.tr("$unlockedCount avatares"),
                                fontSize = 10.sp,
                                color = HextechGold,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botones de acción rápida en la tarjeta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Botón Regalar Avatar
                AnimatedAdminOutlinedButton(
                    onClick = onAvatarGiftClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HextechGold),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("Regalar Avatar"), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                // Botón Reiniciar Slots
                AnimatedAdminOutlinedButton(
                    onClick = onResetSlotsClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF60A5FA)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF60A5FA).copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("Reset Slots"), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                // Botón Gestionar Completo
                AnimatedAdminActionButton(
                    onClick = onManageClick,
                    modifier = Modifier.weight(1.1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = HextechDarkBg, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("Gestionar"), color = HextechDarkBg, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun formatLastConnection(lastActiveTimestamp: Long, isOnline: Boolean, currentTimestamp: Long = System.currentTimeMillis()): String {
    if (isOnline) {
        if (lastActiveTimestamp > 0L) {
            val diff = (currentTimestamp - lastActiveTimestamp).coerceAtLeast(0L)
            val secs = diff / 1000L
            if (secs < 60) return "En línea (${secs}s)"
            val mins = secs / 60
            return "En línea (${mins}m)"
        }
        return "En línea ahora"
    }
    if (lastActiveTimestamp <= 0L) return "Sin registro reciente"
    val diff = (currentTimestamp - lastActiveTimestamp).coerceAtLeast(0L)
    val secs = diff / 1000L
    if (secs < 60) return "Hace ${secs}s"
    val mins = secs / 60
    if (mins < 60) {
        val remSecs = secs % 60
        return "Hace ${mins}m ${remSecs}s"
    }
    val hours = mins / 60
    if (hours < 24) {
        val remMins = mins % 60
        return "Hace ${hours}h ${remMins}m"
    }
    val days = hours / 24
    if (days < 7) return "Hace $days d"
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(lastActiveTimestamp))
    } catch (_: Exception) {
        "Hace $days d"
    }
}

internal fun getSubscriptionStatusText(role: String, premiumUntil: Long?, isPremiumActive: Boolean, currentTimestamp: Long = System.currentTimeMillis(), secondary: String = ""): String {
    if (role == "banned") return "Cuenta Suspendida"
    if (!isPremiumActive) return "Plan Gratuito"
    if (role == "admin") return "Acceso Administrador (Vitalicio)"
    if (role == "moderador" || secondary == "moderador") return "Acceso Moderador (Vitalicio)"
    if (premiumUntil == null || premiumUntil == 0L) return "Plan Gratuito"

    val diff = premiumUntil - currentTimestamp
    if (diff <= 0) return "Suscripción Expirada"

    val days = diff / (24 * 60 * 60 * 1000L)
    val hours = (diff % (24 * 60 * 60 * 1000L)) / (60 * 60 * 1000L)
    val minutes = (diff % (60 * 60 * 1000L)) / (60 * 1000L)
    val seconds = (diff % (60 * 1000L)) / 1000L

    return when {
        days > 0 -> "Premium: ${days}d ${hours}h ${minutes}m ${seconds}s restantes"
        hours > 0 -> "Premium: ${hours}h ${minutes}m ${seconds}s restantes"
        minutes > 0 -> "Premium: ${minutes}m ${seconds}s restantes"
        else -> "Premium: ${seconds}s restantes"
    }
}

internal fun formatExpirationDateDetailed(timestamp: Long?): String {
    if (timestamp == null || timestamp == 0L) return "Vitalicio / Permanente"
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        "Vence: " + sdf.format(Date(timestamp))
    } catch (_: Exception) {
        "Vence: $timestamp"
    }
}

@Composable
private fun ServerScraperHealthCard() {
    val context = LocalContext.current
    val sourceStatuses by BestBuildWrScraper.sourceStatuses.collectAsState()
    val globalStatus by BestBuildWrScraper.globalSyncStatus.collectAsState()
    val isSyncing by BestBuildWrScraper.isSyncing.collectAsState()

    // Actualización reactiva constante en tiempo real en segundo plano
    LaunchedEffect(Unit) {
        while (isActive) {
            try {
                BestBuildWrScraper.syncGlobalTierList(context)
            } catch (e: Exception) {
                // Prevenir interrupción
            }
            delay(8000L) // Actualización automática constante cada 8 segundos
        }
    }

    Surface(
        color = HextechSurfaceBg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, HextechCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tr("Fuentes del meta Global"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(HextechCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isSyncing) HextechGold else Color(0xFF00FF7F))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = com.example.util.tr(if (isSyncing) "Sincronizando..." else "En tiempo real"),
                        color = if (isSyncing) HextechGold else HextechCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = com.example.util.tr("Estado de Red: $globalStatus"),
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            val allSources = sourceStatuses.values.toList()
            allSources.forEach { status ->
                val regionPrefix = when (status.region) {
                    "GLOBAL" -> "🌐 [GLOBAL]"
                    else -> "🌍 [Global]"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (status.isHealthy) Color(0xFF00FF7F) else Color(0xFFFF5252))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = com.example.util.tr("$regionPrefix ${status.name}"),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = com.example.util.tr("${status.responseTimeMs} ms"),
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = if (status.isHealthy) Color(0xFF2E7D32).copy(alpha = 0.2f) else Color(0xFFC62828).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.5.dp, if (status.isHealthy) Color(0xFF81C784) else Color(0xFFEF9A9A))
                        ) {
                            Text(
                                text = com.example.util.tr(if (status.isHealthy) "OPERATIVO (OK)" else (status.errorMessage ?: "ERROR")),
                                color = if (status.isHealthy) Color(0xFF81C784) else Color(0xFFEF9A9A),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
