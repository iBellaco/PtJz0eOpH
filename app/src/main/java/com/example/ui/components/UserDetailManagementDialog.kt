package com.example.ui.components

import com.example.ui.components.CoachButton as Button
import com.example.ui.components.CoachOutlinedButton as OutlinedButton
import com.example.ui.components.CoachTextButton as TextButton
import com.example.ui.components.CoachIconButton as IconButton
import com.example.util.tr
import androidx.compose.foundation.BorderStroke
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AppUserRole
import com.example.ui.theme.*
import com.example.util.AuthManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailManagementDialog(
    user: Map<String, Any>,
    onDismiss: () -> Unit,
    onUserUpdated: (Map<String, Any>) -> Unit,
    onOpenAvatarGift: () -> Unit,
    onReloadAll: () -> Unit,
    premiumGrantAction: ((Int, Boolean, (Result<Map<String, Any>>) -> Unit) -> Unit)? = null,
    roleChangeAction: ((String, (Result<Map<String, Any>>) -> Unit) -> Unit)? = null
) {
    val context = LocalContext.current
    val uid = user["uid"] as? String ?: ""
    var currentName by remember { mutableStateOf(user["name"] as? String ?: "Sin Nombre") }
    val email = user["email"] as? String ?: ""
    var currentEmailInput by remember { mutableStateOf(email) }
    var currentRole by remember { mutableStateOf(user["role"] as? String ?: "free") }
    var currentSecondaryRole by remember { mutableStateOf(user["secondaryRole"] as? String ?: "") }
    var currentBanned by remember { mutableStateOf((user["banned"] as? Boolean) == true || currentRole == "banned") }
    var currentVerified by remember { mutableStateOf((user["isVerified"] as? Boolean) == true || (user["verified"] as? Boolean) == true || currentRole == "admin" || currentRole == "moderador") }
    var currentPremiumUntil by remember { mutableStateOf(com.example.model.PremiumAccessPolicy.deadline(user["premiumUntil"])) }
    var currentPremiumPlan by remember { mutableStateOf((user["subscriptionPlan"] as? String).orEmpty()) }
    var currentBlueEssence by remember(uid) { mutableStateOf((user["blueEssence"] as? Number)?.toLong() ?: 0L) }
    var currentOrangeEssence by remember(uid) { mutableStateOf((user["orangeEssence"] as? Number)?.toLong() ?: 0L) }
    LaunchedEffect(user) {
        currentBlueEssence = (user["blueEssence"] as? Number)?.toLong() ?: currentBlueEssence
        currentOrangeEssence = (user["orangeEssence"] as? Number)?.toLong() ?: currentOrangeEssence
    }
    DisposableEffect(uid) {
        val listener = if (uid.isNotBlank()) FirebaseFirestore.getInstance().collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null && snapshot.exists()) {
                    currentBlueEssence = snapshot.getLong("blueEssence") ?: 0L
                    currentOrangeEssence = snapshot.getLong("orangeEssence") ?: 0L
                    snapshot.getString("role")?.let { currentRole = it }
                    snapshot.getBoolean("banned")?.let { currentBanned = it }
                    if (snapshot.contains("premiumUntil")) currentPremiumUntil = com.example.model.PremiumAccessPolicy.deadline(snapshot.get("premiumUntil"))
                    snapshot.getString("subscriptionPlan")?.let { currentPremiumPlan = it }
                }
            } else null
        onDispose { listener?.remove() }
    }
    val avatarId = user["avatarId"] as? String ?: "default_poro"
    val rankBorder = user["rankBorder"] as? String ?: "NONE"

    val currentLoggedInRole by com.example.util.SubscriptionManager.userRole.collectAsState()
    val isMod = currentLoggedInRole == "moderador"
    val isAdmin = currentLoggedInRole == "admin" || com.example.util.AuthManager.isCurrentUserAdmin()
    val canAssignSecondaryOrVerify = isAdmin || isMod

    var roleToConfirm by remember { mutableStateOf<AppUserRole?>(null) }
    var secondaryRoleToConfirm by remember { mutableStateOf<AppUserRole?>(null) }
    var isChangingRole by remember { mutableStateOf(false) }
    var showEconomyRequest by remember { mutableStateOf(false) }
    var roleChangeError by remember { mutableStateOf<String?>(null) }
    if (showEconomyRequest) EconomyRequestDialog(onDismiss = { showEconomyRequest = false })
    var isChangingSecondaryRole by remember { mutableStateOf(false) }

    var isProcessing by remember { mutableStateOf(false) }
    var customDaysInput by remember { mutableStateOf("") }
    var showCustomDaysDialog by remember { mutableStateOf(false) }
    var showGiveEssenceDialog by remember { mutableStateOf(false) }
    var showPrivateMessageDialog by remember { mutableStateOf(false) }
    var showUserMessagesViewerDialog by remember { mutableStateOf(false) }
    var showManagedHistory by remember(uid) { mutableStateOf(false) }
    if (isAdmin && showManagedHistory) {
        SubscriptionHistoryDialog(userId = uid, userEmail = email, initialBalances = com.example.data.HistoryBalances(currentBlueEssence, currentOrangeEssence), onDismiss = { showManagedHistory = false })
    }

    val registeredDevices = (user["registeredDevices"] as? List<*>) ?: emptyList<Any>()
    var currentDeviceCount by remember { mutableStateOf(registeredDevices.size) }

    var premiumNow by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(currentPremiumUntil, currentRole, currentSecondaryRole) {
        premiumNow = System.currentTimeMillis()
        while (!com.example.model.PremiumAccessPolicy.isLifetime(currentRole, currentSecondaryRole, user["admin"] == true) && (currentPremiumUntil ?: 0L) > premiumNow) {
            kotlinx.coroutines.delay(1000)
            premiumNow = System.currentTimeMillis()
        }
    }
    val isPremiumActive = com.example.model.PremiumAccessPolicy.isActive(currentRole, currentPremiumUntil, now = premiumNow, secondary = currentSecondaryRole, adminClaim = user["admin"] == true, banned = currentBanned, granted = com.example.model.PremiumAccessPolicy.hasGrant(currentPremiumPlan))

    fun acceptPremiumUpdate(updated: Map<String, Any>) {
        currentRole = (updated["role"] as? String) ?: currentRole
        currentSecondaryRole = (updated["secondaryRole"] as? String).orEmpty()
        currentPremiumUntil = com.example.model.PremiumAccessPolicy.deadline(updated["premiumUntil"])
        currentPremiumPlan = (updated["subscriptionPlan"] as? String).orEmpty()
        onUserUpdated(user + updated)
    }
    fun submitPremiumDays(days: Int, extend: Boolean = true, afterSuccess: () -> Unit = {}) {
        if (isProcessing || !isAdmin) return
        isProcessing = true
        fun complete(result: Result<Map<String, Any>>) {
            isProcessing = false
            result.onSuccess { acceptPremiumUpdate(it); afterSuccess() }
        }
        if (premiumGrantAction != null) premiumGrantAction(days, extend, ::complete)
        else applyPremiumDuration(context, uid, days, extend, onError = { isProcessing = false }) { complete(Result.success(it)) }
    }


    Dialog(
        onDismissRequest = {
            when {
                showCustomDaysDialog -> showCustomDaysDialog = false
                showGiveEssenceDialog -> showGiveEssenceDialog = false
                showPrivateMessageDialog -> showPrivateMessageDialog = false
                showUserMessagesViewerDialog -> showUserMessagesViewerDialog = false
                roleToConfirm != null -> roleToConfirm = null
                secondaryRoleToConfirm != null -> secondaryRoleToConfirm = null
                else -> onDismiss()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false
        )
    ) {
        BackHandler(enabled = true) {
            when {
                showCustomDaysDialog -> showCustomDaysDialog = false
                showGiveEssenceDialog -> showGiveEssenceDialog = false
                showPrivateMessageDialog -> showPrivateMessageDialog = false
                showUserMessagesViewerDialog -> showUserMessagesViewerDialog = false
                roleToConfirm != null -> roleToConfirm = null
                secondaryRoleToConfirm != null -> secondaryRoleToConfirm = null
                else -> onDismiss()
            }
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = HextechDarkBg,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, HextechGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                EconomyPendingStatus(alwaysVisible = true)
                // Header del Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        UserAvatarView(
                            avatarId = avatarId,
                            size = 48.dp,
                            fallbackInitial = currentName.take(1).uppercase(),
                            rankBorder = rankBorder,
                            isAdmin = (currentRole == "admin"),
                            role = currentRole,
                            secondaryRole = currentSecondaryRole
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = com.example.util.tr(currentName),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = HextechGold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            RoleBadge(
                                role = currentRole,
                                isPremiumActive = isPremiumActive,
                                isBanned = currentBanned,
                                size = RoleBadgeSize.NORMAL
                            )
                            if (currentSecondaryRole.isNotBlank() && AppUserRole.fromId(currentSecondaryRole) != AppUserRole.FREE) {
                                Spacer(Modifier.height(5.dp))
                                RoleBadge(role = currentSecondaryRole, size = RoleBadgeSize.NORMAL,
                                    modifier = Modifier.testTag("managed_user_secondary_role"))
                            }
                            Text(
                                text = com.example.util.tr(if (isAdmin) email.ifBlank { "UID: $uid" } else "UID: $uid"),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Cerrar"), tint = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = HextechCardBorder)
                Spacer(modifier = Modifier.height(12.dp))

                // Contenido Scrollable
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (isAdmin) item {
                        OutlinedButton(onClick = { showManagedHistory = true }, modifier = Modifier.fillMaxWidth().testTag("managed_user_history")) {
                            Icon(Icons.Default.History, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(tr("Historial"))
                        }
                    }
                    // SECCIÓN: EDITAR CORREO ELECTRÓNICO
                    if (isAdmin) item {
                        Surface(
                            color = HextechSurfaceBg,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(tr("Cambiar Correo Electrónico"), fontWeight = FontWeight.Bold, color = HextechCyan, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = currentEmailInput,
                                    onValueChange = { currentEmailInput = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    label = { Text(tr("Nuevo Correo Electrónico"), color = TextSecondary, fontSize = 11.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = HextechCyan,
                                        unfocusedBorderColor = HextechCardBorder,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        cursorColor = HextechCyan
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        if (currentEmailInput.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(currentEmailInput.trim()).matches()) {
                                            Toast.makeText(context, com.example.util.appTr("Ingresa un correo electrónico válido"), Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        updateUserEmail(context, uid, currentEmailInput.trim()) { newEmail ->
                                            onUserUpdated(user.toMutableMap().apply {
                                                put("email", newEmail)
                                            })
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.End),
                                    colors = ButtonDefaults.buttonColors(containerColor = HextechCyan),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(tr("Guardar Correo"), color = HextechDarkBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    if (com.example.model.RolePanelAccess.isAdministrator(currentRole, user["admin"] == true)) item(key = "premium-status") {
                        PremiumStatusCard(currentRole, currentSecondaryRole, currentPremiumUntil,
                            adminClaim = user["admin"] == true, banned = currentBanned,
                            granted = com.example.model.PremiumAccessPolicy.hasGrant(currentPremiumPlan))
                    }
                    // System lifetime access is not an editable subscription.
                    if (!com.example.model.RolePanelAccess.isAdministrator(currentRole, user["admin"] == true)) item {

                        Surface(
                            color = HextechSurfaceBg,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = HextechGold, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(tr("Gestión de Suscripción Premium"), fontWeight = FontWeight.Bold, color = HextechGold, fontSize = 13.sp)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Estado actual
                                Surface(
                                    color = HextechDarkBg,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = com.example.util.tr("Estado: ${if (isPremiumActive) "⭐ PREMIUM ACTIVO" else "⚪ GRATUITO"}"),
                                            color = if (isPremiumActive) HextechCyan else TextSecondary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        PremiumStatusCard(currentRole, currentSecondaryRole, currentPremiumUntil,
                                            banned = currentBanned, granted = com.example.model.PremiumAccessPolicy.hasGrant(currentPremiumPlan))
                                        if (com.example.model.PremiumAccessPolicy.isLifetime(currentRole, currentSecondaryRole))
                                            Text(tr(getSubscriptionStatusText(currentRole, currentPremiumUntil, isPremiumActive, secondary = currentSecondaryRole)), color = TextMuted)
                                        if (currentPremiumUntil != null && currentPremiumUntil!! > 0L) {
                                            if (com.example.model.PremiumAccessPolicy.isLifetime(currentRole, currentSecondaryRole))
                                                Text(tr("Plazo guardado para cambios de rol:"), color = TextSecondary, fontSize = 11.sp)
                                            Text(
                                                text = com.example.util.tr(formatExpirationDateDetailed(currentPremiumUntil)),
                                                color = HextechGold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(tr("Editar o extender tiempo premium:"), color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text(com.example.util.localizedString(com.example.R.string.premium_duration_hint), color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                                Spacer(modifier = Modifier.height(6.dp))

                                // Grid de Duraciones Rápidas
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    DurationButton(
                                        enabled = isAdmin && !isProcessing && !currentBanned,
                                        label = "+1 Día",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            submitPremiumDays(1)
                                        }
                                    )
                                    DurationButton(
                                        enabled = isAdmin && !isProcessing && !currentBanned,
                                        label = "+7 Días",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            submitPremiumDays(7)
                                        }
                                    )
                                    DurationButton(
                                        enabled = isAdmin && !isProcessing && !currentBanned,
                                        label = "+30 Días",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            submitPremiumDays(30)
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    DurationButton(
                                        enabled = isAdmin && !isProcessing && !currentBanned,
                                        label = "+90 Días (3m)",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            submitPremiumDays(90)
                                        }
                                    )
                                    DurationButton(
                                        enabled = isAdmin && !isProcessing && !currentBanned,
                                        label = "+1 Año (365d)",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            submitPremiumDays(365)
                                        }
                                    )

                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Personalizado en días
                                    OutlinedButton(
                                        enabled = isAdmin && !isProcessing && !currentBanned,
                                        onClick = { showCustomDaysDialog = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HextechCyan),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, HextechCyan.copy(alpha = 0.6f)),
                                        contentPadding = PaddingValues(vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(tr("Días Personalizados..."), fontSize = 11.sp)
                                    }

                                    // Quitar Premium
                                    OutlinedButton(
                                        enabled = isAdmin && !isProcessing && !com.example.model.PremiumAccessPolicy.isLifetime(currentRole, currentSecondaryRole),
                                        onClick = {
                                            removePremiumFromUser(context, uid) {
                                                currentPremiumUntil = null
                                                onUserUpdated(user.toMutableMap().apply {
                                                    put("premiumUntil", 0L)
                                                })
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.6f)),
                                        contentPadding = PaddingValues(vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(tr("Quitar Premium"), fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // SECCIÓN: GESTIÓN Y ASIGNACIÓN DE ROL PRINCIPAL
                    item {
                        Surface(
                            color = HextechSurfaceBg,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Badge,
                                            contentDescription = null,
                                            tint = HextechGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = tr("Gestión de Rol Principal"),
                                            fontWeight = FontWeight.Bold,
                                            color = HextechGold,
                                            fontSize = 13.sp
                                        )
                                    }

                                    // Badge animado del rol actual
                                    RoleBadge(
                                        role = currentRole,
                                        isPremiumActive = isPremiumActive,
                                        isBanned = currentBanned,
                                        size = RoleBadgeSize.NORMAL
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = tr("Asigna o modifica el rango principal de usuario. Por directiva institucional, la asignación del rol Administrador está excluida. Solo Administradores pueden cambiar este rol."),
                                    color = TextSecondary,
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                if (currentRole == "admin") {
                                    Surface(
                                        color = HextechGold.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, HextechGold.copy(alpha = 0.35f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.AdminPanelSettings,
                                                contentDescription = null,
                                                tint = HextechGold,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = tr("Esta cuenta posee el rango de Administrador Maestro protegido. Por directiva de seguridad, no se puede alterar ni degradar su rol desde este panel."),
                                                color = HextechGold,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                } else {
                                    if (!isAdmin) {
                                        Surface(
                                            color = Color.Yellow.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Yellow.copy(alpha = 0.25f)),
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                        ) {
                                            Text(
                                                text = tr("Solo los Administradores principales tienen privilegios para cambiar el Rol Principal."),
                                                color = Color.Yellow,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }

                                    // Lista de roles asignables principales
                                    val primaryRoles = listOf(
                                        AppUserRole.FREE,
                                        AppUserRole.PATROCINADOR,
                                        AppUserRole.PREMIUM,
                                        AppUserRole.MODERATOR,
                                        AppUserRole.CREATOR,
                                        AppUserRole.CREATOR_LVL2,
                                        AppUserRole.CREATOR_LVL3,
                                        AppUserRole.CREATOR_LVL4,
                                        AppUserRole.CREATOR_LVL5,
                                        AppUserRole.STREAMER,
                                        AppUserRole.BANNED
                                    )

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        primaryRoles.forEach { targetRole ->
                                            val isSelected = (currentRole.equals(targetRole.id, ignoreCase = true) && (!currentBanned || targetRole == AppUserRole.BANNED))

                                            com.example.ui.components.CoachClickableSurface(
                                                onClick = {
                                                    if (!isSelected && !isChangingRole && isAdmin) {
                                                        roleChangeError = null
                                                        roleToConfirm = targetRole
                                                    }
                                                },
                                                enabled = !isSelected && !isChangingRole && isAdmin,
                                                color = if (isSelected) targetRole.primaryColor.copy(alpha = 0.15f) else HextechDarkBg,
                                                shape = RoundedCornerShape(8.dp),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    width = if (isSelected) 1.2.dp else 0.8.dp,
                                                    color = if (isSelected) targetRole.primaryColor else HextechCardBorder
                                                ),
                                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("managed_role_${targetRole.id}")
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text(
                                                            text = com.example.util.tr(targetRole.emoji),
                                                            fontSize = 15.sp,
                                                            modifier = Modifier.padding(end = 8.dp)
                                                        )
                                                        Column {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Text(
                                                                    text = com.example.util.tr(targetRole.displayName),
                                                                    color = if (isSelected) targetRole.primaryColor else TextPrimary,
                                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                                    fontSize = 12.sp
                                                                )
                                                                if (isSelected) {
                                                                    Spacer(modifier = Modifier.width(6.dp))
                                                                    Text(
                                                                        text = tr("• ACTIVO"),
                                                                        color = targetRole.primaryColor,
                                                                        fontSize = 10.sp,
                                                                        fontWeight = FontWeight.ExtraBold
                                                                    )
                                                                }
                                                            }
                                                            Text(
                                                                text = com.example.util.tr(targetRole.description),
                                                                color = TextMuted,
                                                                fontSize = 10.5.sp,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }

                                                    if (isSelected) {
                                                        Icon(
                                                            Icons.Default.CheckCircle,
                                                            contentDescription = null,
                                                            tint = targetRole.primaryColor,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    } else if (isAdmin) {
                                                        Surface(
                                                            color = targetRole.primaryColor.copy(alpha = 0.12f),
                                                            shape = RoundedCornerShape(4.dp),
                                                            border = androidx.compose.foundation.BorderStroke(0.5.dp, targetRole.primaryColor.copy(alpha = 0.4f))
                                                        ) {
                                                            Text(
                                                                text = tr("Asignar"),
                                                                color = targetRole.primaryColor,
                                                                fontSize = 10.5.sp,
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
							}
						}
					}

                    // SECCIÓN: GESTIÓN Y ASIGNACIÓN DE ROL SECUNDARIO (RANGOS DE ELO COMPETITIVO)
                    item {
                        Surface(
                            color = HextechSurfaceBg,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Badge,
                                            contentDescription = null,
                                            tint = HextechCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = tr("Gestión de Rol Secundario"),
                                            fontWeight = FontWeight.Bold,
                                            color = HextechCyan,
                                            fontSize = 13.sp
                                        )
                                    }

                                    // Badge animado del rol secundario actual
                                    if (currentSecondaryRole.isNotBlank()) {
                                        RoleBadge(
                                            role = currentSecondaryRole,
                                            isPremiumActive = false,
                                            isBanned = false,
                                            size = RoleBadgeSize.NORMAL
                                        )
                                    } else {
                                        Surface(
                                            color = Color.White.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(4.dp),
                                            border = androidx.compose.foundation.BorderStroke(0.5.dp, HextechCardBorder)
                                        ) {
                                            Text(
                                                text = tr("SIN ROL SECUNDARIO"),
                                                color = TextMuted,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = tr("Asigna un rol secundario (Rango de Elo competitivo) que no posee ningun privilegio en el sistema. Este rol puede ser asignado tanto por Administradores como por Moderadores."),
                                    color = TextSecondary,
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                val secondaryRoles = listOf(
                                    AppUserRole.ESMERALDA,
                                    AppUserRole.DIAMANTE,
                                    AppUserRole.MAESTRO,
                                    AppUserRole.GRAN_MAESTRO,
                                    AppUserRole.ASPIRANTE,
                                    AppUserRole.SOBERANO
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Opción para quitar/limpiar rol secundario
                                    val isSecondaryEmpty = currentSecondaryRole.isBlank()
                                    com.example.ui.components.CoachClickableSurface(
                                        onClick = {
                                            if (!isSecondaryEmpty && !isChangingSecondaryRole && canAssignSecondaryOrVerify) {
                                                secondaryRoleToConfirm = AppUserRole.FREE
                                            }
                                        },
                                        enabled = !isSecondaryEmpty && !isChangingSecondaryRole && canAssignSecondaryOrVerify,
                                        color = if (isSecondaryEmpty) HextechCyan.copy(alpha = 0.12f) else HextechDarkBg,
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (isSecondaryEmpty) 1.2.dp else 0.8.dp,
                                            color = if (isSecondaryEmpty) HextechCyan else HextechCardBorder
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(tr("Ninguno (Quitar)"), color = if (isSecondaryEmpty) HextechCyan else TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                                Text(tr("Remueve el rol secundario actual de la cuenta"), color = TextMuted, fontSize = 10.5.sp)
                                            }
                                            if (isSecondaryEmpty) {
                                                Text(tr("• ACTIVO"), color = HextechCyan, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                            } else if (canAssignSecondaryOrVerify) {
                                                Surface(
                                                    color = DangerRed.copy(alpha = 0.12f),
                                                    shape = RoundedCornerShape(4.dp),
                                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, DangerRed.copy(alpha = 0.4f))
                                                ) {
                                                    Text(
                                                        text = tr("Remover"),
                                                        color = DangerRed,
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Lista de roles secundarios
                                    secondaryRoles.forEach { targetRole ->
                                        val isSelected = currentSecondaryRole.equals(targetRole.id, ignoreCase = true)

                                        com.example.ui.components.CoachClickableSurface(
                                            onClick = {
                                                if (!isSelected && !isChangingSecondaryRole && canAssignSecondaryOrVerify) {
                                                    secondaryRoleToConfirm = targetRole
                                                }
                                            },
                                            enabled = !isSelected && !isChangingSecondaryRole && canAssignSecondaryOrVerify,
                                            color = if (isSelected) targetRole.primaryColor.copy(alpha = 0.15f) else HextechDarkBg,
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(
                                                width = if (isSelected) 1.2.dp else 0.8.dp,
                                                color = if (isSelected) targetRole.primaryColor else HextechCardBorder
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Column {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = com.example.util.tr(targetRole.displayName),
                                                                color = if (isSelected) targetRole.primaryColor else TextPrimary,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                                fontSize = 12.sp
                                                            )
                                                            if (isSelected) {
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Text(
                                                                    text = tr("• ACTIVO"),
                                                                    color = targetRole.primaryColor,
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.ExtraBold
                                                                )
                                                            }
                                                        }
                                                        Text(
                                                            text = com.example.util.tr(targetRole.description),
                                                            color = TextMuted,
                                                            fontSize = 10.5.sp,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }

                                                if (isSelected) {
                                                    Icon(
                                                        Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = targetRole.primaryColor,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                } else if (canAssignSecondaryOrVerify) {
                                                    Surface(
                                                        color = targetRole.primaryColor.copy(alpha = 0.12f),
                                                        shape = RoundedCornerShape(4.dp),
                                                        border = androidx.compose.foundation.BorderStroke(0.5.dp, targetRole.primaryColor.copy(alpha = 0.4f))
                                                    ) {
                                                        Text(
                                                            text = tr("Asignar"),
                                                            color = targetRole.primaryColor,
                                                            fontSize = 10.5.sp,
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
                        }
                    }

                    // SECCIÓN: VERIFICACIÓN OFICIAL DE CUENTA
                    item {
                        Surface(
                            color = HextechSurfaceBg,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (currentVerified) HextechCyan.copy(alpha = 0.5f) else HextechCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.Verified,
                                            contentDescription = null,
                                            tint = if (currentVerified) HextechCyan else TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = tr("Verificación Oficial de Cuenta"),
                                            fontWeight = FontWeight.Bold,
                                            color = if (currentVerified) HextechCyan else TextPrimary,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Surface(
                                        color = if (currentVerified) HextechCyan.copy(alpha = 0.15f) else HextechDarkBg,
                                        shape = RoundedCornerShape(4.dp),
                                        border = androidx.compose.foundation.BorderStroke(0.5.dp, if (currentVerified) HextechCyan else HextechCardBorder)
                                    ) {
                                        Text(
                                            text = com.example.util.tr(if (currentVerified) "VERIFICADA" else "NO VERIFICADA"),
                                            color = if (currentVerified) HextechCyan else TextMuted,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = tr("Otorga o revoca la insignia de cuenta verificada para este invocador. La insignia se muestra junto a su nombre en su perfil y en la gestión de comunidad."),
                                    color = TextSecondary,
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        val newStatus = !currentVerified
                                        if (isAdmin) {
                                            updateUserVerification(context, uid, newStatus) {
                                                currentVerified = newStatus
                                                onUserUpdated(user.toMutableMap().apply {
                                                    put("isVerified", newStatus)
                                                    put("verified", newStatus)
                                                })
                                            }
                                        } else if (isMod) {
                                            createUserManagementApprovalRequest(
                                                context = context,
                                                requestType = "VERIFICATION",
                                                targetUid = uid,
                                                targetName = currentName,
                                                targetEmail = email,
                                                newValue = newStatus.toString()
                                            ) {
                                                // Success callback
                                            }
                                        }
                                    },
                                    enabled = canAssignSecondaryOrVerify,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (currentVerified) DangerRed.copy(alpha = 0.18f) else HextechCyan,
                                        contentColor = if (currentVerified) DangerRed else HextechDarkBg
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    border = if (currentVerified) androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)) else null
                                ) {
                                    Icon(
                                        imageVector = if (currentVerified) Icons.Default.Close else Icons.Filled.Verified,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = com.example.util.tr(if (currentVerified) "Revocar Estado de Verificado" else "Otorgar Estado de Verificado"),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // SECCIÓN 2: GESTIÓN DE HARDWARE Y SLOTS DE DISPOSITIVOS
                    item {
                        Surface(
                            color = HextechSurfaceBg,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Devices, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(tr("Slots de Hardware y Dispositivos"), fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA), fontSize = 13.sp)
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = com.example.util.tr("El usuario tiene $currentDeviceCount de 2 slots de hardware vinculados. Si el usuario cambió de teléfono o tiene problemas de sesión, puedes liberar todos sus slots."),
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        resetUserHardwareSlots(context, uid) {
                                            currentDeviceCount = 0
                                            onUserUpdated(user.toMutableMap().apply {
                                                put("registeredDevices", emptyList<String>())
                                                put("sessionToken", "")
                                            })
                                            Toast.makeText(context, com.example.util.appTr("Slots de hardware liberados (0/2 en uso)"), Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(tr("Liberar / Reiniciar Todos los Slots de Hardware"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // SECCIÓN 3: COSMÉTICOS Y REGALOS DE AVATARES
                    item {
                        Surface(
                            color = HextechSurfaceBg,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Palette, contentDescription = null, tint = HextechGold, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(tr("Regalos de Avatares y Cosméticos"), fontWeight = FontWeight.Bold, color = HextechGold, fontSize = 13.sp)
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = tr("Permite desbloquear avatares exclusivos individuales o regalar todo el catálogo de una vez."),
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            onDismiss()
                                            onOpenAvatarGift()
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = HextechDarkBg, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(tr("Ver Galería de Avatares"), color = HextechDarkBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            giftAllAvatarsToUser(context, uid) {
                                                Toast.makeText(context, com.example.util.appTr("¡Todo el catálogo de avatares desbloqueado!"), Toast.LENGTH_SHORT).show()
                                                onReloadAll()
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(tr("Desbloquear TODO"), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Button(
                                    onClick = {
                                        revokeAllExclusiveAvatarsFromUser(context, uid) {
                                            Toast.makeText(context, com.example.util.appTr("¡Regalos de avatares retirados correctamente!"), Toast.LENGTH_SHORT).show()
                                            onReloadAll()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.85f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(tr("Quitar Regalos de Avatares"), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // SECCIÓN 4: SEGURIDAD Y ESTADO DE LA CUENTA
                    item {
                        Surface(
                            color = HextechSurfaceBg,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(tr("Seguridad y Estado de la Cuenta"), fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                if (currentRole == "admin") {
                                    Surface(
                                        color = HextechGold.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, HextechGold.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = HextechGold, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = tr("Esta cuenta posee rango de Administrador Maestro protegido."),
                                                color = HextechGold,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                } else {
                                    // Solo opción de suspender/banear o reactivar
                                    Button(
                                        onClick = {
                                            val newBanned = !currentBanned
                                            val newRole = if (newBanned) "banned" else "free"
                                            toggleUserBanStatus(context, uid, newBanned, newRole) {
                                                currentBanned = newBanned
                                                currentRole = newRole
                                                onUserUpdated(user.toMutableMap().apply {
                                                    put("banned", newBanned)
                                                    put("role", newRole)
                                                })
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (currentBanned) Color(0xFF10B981) else DangerRed
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(if (currentBanned) Icons.Default.LockOpen else Icons.Default.Block, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(com.example.util.tr(if (currentBanned) "Desbanear y Reactivar Cuenta" else "Suspender / Banear Cuenta"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    // SECCIÓN 5: COMUNICACIÓN Y RECOMPENSAS
                    item {
                        Surface(
                            color = HextechSurfaceBg,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFF0EA5E9), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(tr("Comunicación y Recompensas"), fontWeight = FontWeight.Bold, color = Color(0xFF0EA5E9), fontSize = 13.sp)
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {

                                        Surface(
                                            color = HextechDarkBg,
                                            shape = RoundedCornerShape(6.dp),
                                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF0EA5E9).copy(alpha = 0.6f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Image(
                                                    painter = painterResource(id = com.example.R.drawable.ic_blue_essence),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = com.example.util.tr("$currentBlueEssence EA"), modifier = Modifier.testTag("managed_user_blue_balance"),
                                                    fontSize = 10.5.sp,
                                                    color = Color(0xFF38BDF8),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Surface(
                                            color = HextechDarkBg,
                                            shape = RoundedCornerShape(6.dp),
                                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFFF9E1B).copy(alpha = 0.6f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Image(
                                                    painter = painterResource(id = com.example.R.drawable.ic_orange_essence),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = com.example.util.tr("$currentOrangeEssence EN"), modifier = Modifier.testTag("managed_user_orange_balance"),
                                                    fontSize = 10.5.sp,
                                                    color = Color(0xFFFFB74D),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { showUserMessagesViewerDialog = true },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Message, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(tr("Mensajes Privados"), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { showGiveEssenceDialog = true },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = com.example.R.drawable.ic_blue_essence),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(tr("Dar Esencia"), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGiveEssenceDialog) {
        AdminGiveEssenceDialog(
            userUid = uid,
            onDismiss = { showGiveEssenceDialog = false },
            onBalancesUpdated = { updated ->
                currentBlueEssence = (updated["blueEssence"] as? Number)?.toLong() ?: currentBlueEssence
                currentOrangeEssence = (updated["orangeEssence"] as? Number)?.toLong() ?: currentOrangeEssence
                onUserUpdated(user + updated)
            },
            onSuccess = {
                Toast.makeText(context, com.example.util.appTr("Esencias actualizadas con éxito."), Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showUserMessagesViewerDialog) {
        AdminUserMessagesViewerDialog(
            userUid = uid,
            userName = currentName,
            onDismiss = { showUserMessagesViewerDialog = false },
            onOpenSendNewMessage = { showPrivateMessageDialog = true }
        )
    }

    if (showPrivateMessageDialog) {
        AdminPrivateMessageDialog(
            userUid = uid,
            onDismiss = { showPrivateMessageDialog = false },
            onSuccess = {
                Toast.makeText(context, com.example.util.appTr("Mensaje privado enviado."), Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Diálogo para ingresar Días Personalizados
    if (showCustomDaysDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDaysDialog = false },
            title = { Text(tr("Establecer días restantes de premium"), fontWeight = FontWeight.Bold, color = HextechGold) },
            text = {
                Column {
                    Text(tr("Establece los días restantes desde hoy."), color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customDaysInput,
                        onValueChange = { customDaysInput = it.filter { ch -> ch.isDigit() } },
                        placeholder = { Text(tr("Ej. 15, 45, 180...")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val days = customDaysInput.toIntOrNull()
                        if (days != null && days in 1..36500) {
                            submitPremiumDays(days, false) { showCustomDaysDialog = false }
                        } else {
                            Toast.makeText(context, com.example.util.appTr("Ingresa una cantidad válida de días"), Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold)
                ) {
                    Text(tr("Aplicar Días"), color = HextechDarkBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDaysDialog = false }) {
                    Text(tr("Cancelar"), color = TextMuted)
                }
            }
        )
    }

    // Diálogo de Confirmación para Cambio de Rol
    if (roleToConfirm != null) {
        val target = roleToConfirm!!
        AlertDialog(
            onDismissRequest = { if (!isChangingRole) roleToConfirm = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ManageAccounts,
                        contentDescription = null,
                        tint = target.primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = com.example.util.tr("¿Cambiar rol a ${target.displayName}?"),
                        color = HextechGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = com.example.util.tr("¿Confirmas asignar este nuevo rol al usuario '$currentName'?"),
                        color = TextPrimary,
                        fontSize = 13.sp
                    )

                    roleChangeError?.let { Text(it, color = DangerRed, modifier = Modifier.testTag("role_change_error")) }
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = HextechDarkBg,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, target.primaryColor.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = tr("NUEVO ROL ASIGNADO"),
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            RoleBadge(
                                role = target.id,
                                isPremiumActive = target in listOf(AppUserRole.ADMIN, AppUserRole.MODERATOR, AppUserRole.PREMIUM),
                                isBanned = (target == AppUserRole.BANNED),
                                size = RoleBadgeSize.LARGE
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = com.example.util.tr(target.description),
                                color = TextSecondary,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (target == AppUserRole.BANNED) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = tr("⚠️ Al asignar el rol Baneado, la cuenta del usuario será suspendida de inmediato y no podrá utilizar los servicios de la app."),
                            color = DangerRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else if (target in listOf(AppUserRole.PREMIUM, AppUserRole.MODERATOR)) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = tr("✨ Este rango incluye acceso activo a las herramientas y ventajas del Pase Hextech."),
                            color = HextechCyan,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isChangingRole = true
                        roleChangeError = null
                        val failed: (Throwable) -> Unit = { failure ->
                            isChangingRole = false
                            if (failure is com.example.data.EconomyPendingException) {
                                roleToConfirm = null
                                showEconomyRequest = true
                            } else roleChangeError = failure.message ?: com.example.util.appTr("No se pudo completar la operación. Vuelve a intentarlo.")
                        }
                        val succeeded: (String, Boolean, Long?) -> Unit = { newRole, isBanned, inheritedUntil ->
                            isChangingRole = false
                            roleToConfirm = null
                            currentRole = newRole
                            currentBanned = isBanned
                            currentPremiumUntil = inheritedUntil
                            if (newRole == "free") currentPremiumPlan = "FREE"
                            onUserUpdated(user.toMutableMap().apply {
                                put("role", newRole)
                                put("banned", isBanned)
                                if (inheritedUntil != null) put("premiumUntil", inheritedUntil)
                                if (newRole == "free") put("subscriptionPlan", "FREE")
                            })
                            onReloadAll()
                        }
                        if (roleChangeAction != null) roleChangeAction(target.id) { outcome ->
                            outcome.onSuccess { response ->
                                val account = response["account"] as? Map<*, *>
                                succeeded(target.id, target.id == "banned", (account?.get("premiumUntil") as? Number)?.toLong())
                            }.onFailure(failed)
                        } else updateUserRoleInCloud(context, uid, target.id, onError = failed, onSuccess = succeeded)
                    },
                    modifier = Modifier.heightIn(min = 48.dp).testTag("role_change_confirm"),
                    enabled = !isChangingRole,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (target == AppUserRole.BANNED) DangerRed else HextechGold
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isChangingRole) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = HextechDarkBg, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = tr("Confirmar y Asignar"),
                            color = if (target == AppUserRole.BANNED) Color.White else HextechDarkBg,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!isChangingRole) roleToConfirm = null },
                    modifier = Modifier.heightIn(min = 48.dp).testTag("role_change_cancel"),
                    enabled = !isChangingRole
                ) {
                    Text(tr("Cancelar"), color = TextSecondary)
                }
            },
            containerColor = HextechSurfaceBg,
            shape = RoundedCornerShape(14.dp)
        )
    }

    if (secondaryRoleToConfirm != null) {
        val target = secondaryRoleToConfirm!!
        val isRemoving = target == AppUserRole.FREE
        AlertDialog(
            onDismissRequest = { if (!isChangingSecondaryRole) secondaryRoleToConfirm = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ManageAccounts,
                        contentDescription = null,
                        tint = target.primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = com.example.util.tr(if (isRemoving) "Quitar Rol Secundario" else "Cambiar Rol Secundario a ${target.displayName}"),
                        color = HextechGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = com.example.util.tr(if (isRemoving) {
                            "¿Confirmas quitar el rol secundario asignado al usuario '$currentName'?"
                        } else {
                            "¿Confirmas asignar el rol secundario '${target.displayName}' al usuario '$currentName'?"
                        }),
                        color = TextPrimary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!isRemoving) {
                        Surface(
                            color = HextechDarkBg,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, target.primaryColor.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = tr("NUEVO ROL SECUNDARIO"),
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                RoleBadge(
                                    role = target.id,
                                    isPremiumActive = false,
                                    isBanned = false,
                                    size = RoleBadgeSize.LARGE
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = com.example.util.tr(target.description),
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isChangingSecondaryRole = true
                        val targetRoleId = if (isRemoving) "" else target.id
                        if (isAdmin) {
                            updateUserSecondaryRoleInCloud(context, uid, targetRoleId) { newSecondaryRole ->
                                isChangingSecondaryRole = false
                                secondaryRoleToConfirm = null
                                currentSecondaryRole = newSecondaryRole
                                onUserUpdated(user.toMutableMap().apply {
                                    put("secondaryRole", newSecondaryRole)
                                })
                                onReloadAll()
                            }
                        } else if (isMod) {
                            createUserManagementApprovalRequest(
                                context = context,
                                requestType = "SECONDARY_ROLE",
                                targetUid = uid,
                                targetName = currentName,
                                targetEmail = email,
                                newValue = targetRoleId
                            ) {
                                isChangingSecondaryRole = false
                                secondaryRoleToConfirm = null
                            }
                        }
                    },
                    enabled = !isChangingSecondaryRole,
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isChangingSecondaryRole) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = HextechDarkBg, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = tr("Confirmar"),
                            color = HextechDarkBg,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!isChangingSecondaryRole) secondaryRoleToConfirm = null },
                    enabled = !isChangingSecondaryRole
                ) {
                    Text(tr("Cancelar"), color = TextSecondary)
                }
            },
            containerColor = HextechSurfaceBg,
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
private fun DurationButton(
    label: String,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.then(Modifier.testTag("premium_duration_$label")),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (accent) HextechGold else HextechDarkBg
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (accent) HextechGold else HextechCardBorder),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Text(
            text = com.example.util.tr(label),
            color = if (accent) HextechDarkBg else TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
