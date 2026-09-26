package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorDashboardDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val userRole by com.example.util.SubscriptionManager.userRole.collectAsState()
    
    // Safety check: only moderators are allowed
    if (userRole != "moderador") {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }

    var selectedTab by remember { mutableStateOf(0) } // 0 = Soporte, 1 = Usuarios
    var showSupportPanelDialog by remember { mutableStateOf(false) }

    if (showSupportPanelDialog) {
        AdminSupportReportsDialog(onDismiss = { showSupportPanelDialog = false })
    }

    Dialog(
        onDismissRequest = {
            if (showSupportPanelDialog) {
                showSupportPanelDialog = false
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false
        )
    ) {
        BackHandler(enabled = true) {
            if (showSupportPanelDialog) {
                showSupportPanelDialog = false
            } else {
                onDismiss()
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = HextechDarkBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                // Header
                Surface(
                    color = HextechSurface,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Brush.linearGradient(listOf(HextechCyan, HextechBlue))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SupportAgent,
                                    contentDescription = null,
                                    tint = HextechDarkBg,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Bandeja de Moderación",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = HextechGold
                                )
                                Text(
                                    text = "Soporte Técnico y Propuestas de Usuarios",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Custom Tab Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HextechSurface)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Tab Soporte
                    Button(
                        onClick = { selectedTab = 0 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 0) HextechGold else Color.Transparent,
                            contentColor = if (selectedTab == 0) HextechDarkBg else Color(0xFF94A3B8)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp),
                        elevation = null
                    ) {
                        Icon(
                            imageVector = Icons.Default.HeadsetMic,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Soporte Técnico", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Tab Usuarios
                    Button(
                        onClick = { selectedTab = 1 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 1) HextechGold else Color.Transparent,
                            contentColor = if (selectedTab == 1) HextechDarkBg else Color(0xFF94A3B8)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp),
                        elevation = null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gestor Usuarios", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    if (selectedTab == 0) {
                        // Soporte Técnico Panel
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(HextechSurface.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = null,
                                tint = HextechCyan,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Bandeja de Reportes de Soporte",
                                color = HextechCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Como moderador, puedes visualizar las dudas de los usuarios de la comunidad, responder a sus mensajes y cerrar reportes de soporte tecnico resueltos.",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { showSupportPanelDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = HextechCyan, contentColor = HextechDarkBg),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Abrir Reportes de Soporte", fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    } else {
                        // Gestor de Usuarios Panel
                        ModeratorUserListPanel()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorUserListPanel() {
    val context = LocalContext.current
    var users by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedUserForProposal by remember { mutableStateOf<Map<String, Any>?>(null) }
    var listenerRegistration by remember { mutableStateOf<ListenerRegistration?>(null) }

    fun loadUsers() {
        isLoading = true
        listenerRegistration?.remove()
        listenerRegistration = FirebaseFirestore.getInstance().collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Error al cargar usuarios: ${error.message}", Toast.LENGTH_LONG).show()
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    users = snapshot.documents.map { doc ->
                        val data = doc.data?.toMutableMap() ?: mutableMapOf()
                        data["uid"] = doc.id
                        data
                    }
                    isLoading = false
                }
            }
    }

    LaunchedEffect(Unit) {
        loadUsers()
    }

    DisposableEffect(Unit) {
        onDispose {
            listenerRegistration?.remove()
        }
    }

    val filteredUsers = remember(users, searchQuery) {
        users.filter { user ->
            val name = (user["name"] as? String ?: "").lowercase()
            name.contains(searchQuery.lowercase())
        }.sortedBy { (it["name"] as? String ?: "").lowercase() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar usuario por nombre...", color = Color(0xFF64748B)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HextechGold) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = HextechSurface,
                unfocusedContainerColor = HextechSurface,
                focusedBorderColor = HextechGold,
                unfocusedBorderColor = HextechCardBorder
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = HextechGold)
            }
        } else if (filteredUsers.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (searchQuery.isBlank()) "No se encontraron usuarios" else "No se encontraron coincidencias para '$searchQuery'",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredUsers) { user ->
                    val name = user["name"] as? String ?: "Sin Nombre"
                    val avatarId = user["avatarId"] as? String ?: "default_poro"
                    val isVerified = (user["isVerified"] as? Boolean) == true || (user["verified"] as? Boolean) == true

                    Surface(
                        onClick = { selectedUserForProposal = user },
                        color = HextechSurface,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Avatar (without secondary role/admin frames leakage)
                                UserAvatarView(
                                    avatarId = avatarId,
                                    size = 36.dp,
                                    fallbackInitial = name.take(1).uppercase(),
                                    secondaryRole = ""
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                                if (isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verificado",
                                        tint = HextechCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Sugerir Modificación",
                                tint = HextechGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (selectedUserForProposal != null) {
        ModeratorUserProposalDialog(
            user = selectedUserForProposal!!,
            onDismiss = { selectedUserForProposal = null }
        )
    }
}

@Composable
fun ModeratorUserProposalDialog(
    user: Map<String, Any>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val uid = user["uid"] as? String ?: ""
    val name = user["name"] as? String ?: "Usuario"
    val email = user["email"] as? String ?: ""
    val currentVerified = (user["isVerified"] as? Boolean) == true || (user["verified"] as? Boolean) == true
    val currentSecondaryRole = user["secondaryRole"] as? String ?: ""

    var targetVerified by remember { mutableStateOf(currentVerified) }
    var targetSecondaryRole by remember { mutableStateOf(currentSecondaryRole) }
    var isSending by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            color = HextechSurface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, HextechGold)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Proponer Modificación",
                        color = HextechGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // User details
                Text(
                    text = "Usuario: $name",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = email,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.5.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = HextechCardBorder, thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Verification proposal
                Text(
                    text = "Estado de Verificación Oficial:",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Button No Verificado
                    Button(
                        onClick = { targetVerified = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!targetVerified) Color(0xFFEF4444).copy(alpha = 0.2f) else HextechDarkBg,
                            contentColor = if (!targetVerified) Color(0xFFEF4444) else Color(0xFF64748B)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (!targetVerified) Color(0xFFEF4444).copy(alpha = 0.5f) else Color.Transparent
                        ),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text("No Verificado", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Button Verificado
                    Button(
                        onClick = { targetVerified = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (targetVerified) HextechCyan.copy(alpha = 0.2f) else HextechDarkBg,
                            contentColor = if (targetVerified) HextechCyan else Color(0xFF64748B)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (targetVerified) HextechCyan.copy(alpha = 0.5f) else Color.Transparent
                        ),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text("Verificado", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Secondary Role proposal
                Text(
                    text = "Rol Secundario Propuesto (Marco):",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                val availableRoles = listOf(
                    "" to "Sin Rol Secundario",
                    "esmeralda" to "Esmeralda",
                    "diamante" to "Diamante",
                    "maestro" to "Maestro",
                    "gran maestro" to "Gran Maestro",
                    "aspirante" to "Aspirante",
                    "soberano" to "Soberano"
                )

                // Render grid or list of roles in options
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HextechDarkBg, RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    availableRoles.forEach { (roleId, roleName) ->
                        val isSelected = targetSecondaryRole.lowercase() == roleId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) HextechGold.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable { targetSecondaryRole = roleId }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = roleName,
                                color = if (isSelected) HextechGold else Color(0xFFF1F5F9),
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = HextechGold, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Warning / Info
                Surface(
                    color = HextechGold.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HextechGold.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = HextechGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Estas modificaciones requieren la aprobacion del Administrador principal antes de verse reflejadas en el perfil.",
                            color = HextechGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF64748B)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancelar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            isSending = true
                            
                            val pendingType = if (targetVerified != currentVerified) "VERIFICATION" else "SECONDARY_ROLE"
                            val pendingValue = if (pendingType == "VERIFICATION") targetVerified.toString() else targetSecondaryRole

                            if (targetVerified == currentVerified && targetSecondaryRole == currentSecondaryRole) {
                                Toast.makeText(context, "No has sugerido ningun cambio nuevo", Toast.LENGTH_SHORT).show()
                                isSending = false
                                return@Button
                            }

                            // Trigger creation of approval requests
                            if (targetVerified != currentVerified) {
                                createModeratorApprovalRequest(
                                    context = context,
                                    requestType = "VERIFICATION",
                                    targetUid = uid,
                                    targetName = name,
                                    targetEmail = email,
                                    newValue = targetVerified.toString()
                                ) {
                                    if (targetSecondaryRole != currentSecondaryRole) {
                                        createModeratorApprovalRequest(
                                            context = context,
                                            requestType = "SECONDARY_ROLE",
                                            targetUid = uid,
                                            targetName = name,
                                            targetEmail = email,
                                            newValue = targetSecondaryRole
                                        ) {
                                            isSending = false
                                            onDismiss()
                                        }
                                    } else {
                                        isSending = false
                                        onDismiss()
                                    }
                                }
                            } else {
                                createModeratorApprovalRequest(
                                    context = context,
                                    requestType = "SECONDARY_ROLE",
                                    targetUid = uid,
                                    targetName = name,
                                    targetEmail = email,
                                    newValue = targetSecondaryRole
                                ) {
                                    isSending = false
                                    onDismiss()
                                }
                            }
                        },
                        enabled = !isSending,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = HextechGold, contentColor = HextechDarkBg),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(color = HextechDarkBg, modifier = Modifier.size(16.dp))
                        } else {
                            Text("Enviar Propuesta", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

private fun createModeratorApprovalRequest(
    context: Context,
    requestType: String,
    targetUid: String,
    targetName: String,
    targetEmail: String,
    newValue: String,
    onSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val moderator = auth.currentUser
    
    val reqId = db.collection("moderator_requests").document().id
    val payload = hashMapOf<String, Any>(
        "id" to reqId,
        "requestType" to requestType,
        "targetUid" to targetUid,
        "targetName" to targetName,
        "targetEmail" to targetEmail,
        "newValue" to newValue,
        "requestedByUid" to (moderator?.uid ?: ""),
        "requestedByName" to (moderator?.displayName ?: moderator?.email?.substringBefore("@") ?: "Moderador"),
        "status" to "PENDIENTE",
        "timestamp" to System.currentTimeMillis()
    )
    
    db.collection("moderator_requests").document(reqId)
        .set(payload)
        .addOnSuccessListener {
            Toast.makeText(context, "Solicitud enviada para aprobación del Administrador", Toast.LENGTH_LONG).show()
            onSuccess()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error al crear solicitud: ${e.message}", Toast.LENGTH_LONG).show()
        }
}
