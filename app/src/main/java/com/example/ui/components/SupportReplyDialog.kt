package com.example.ui.components

import com.example.util.tr
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.collectAsState
import com.example.data.SupportReplyManager
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SupportReplyDialog(
    reportId: String,
    reportTitle: String,
    reportDescription: String,
    userEmail: String,
    userName: String = "",
    userId: String = "",
    initialReply: String = "",
    tag: String = "SOPORTE",
    isFirestoreDoc: Boolean = false,
    onDismiss: () -> Unit,
    onReplySent: (replyText: String, markedAsRead: Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var markAsRead by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }

    val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val currentUserRole by com.example.util.SubscriptionManager.userRole.collectAsState()
    val currentUserName by com.example.util.SubscriptionManager.userName.collectAsState()

    val responderRoleLabel = remember(currentUserRole) {
        when {
            currentUserRole.equals("moderador", ignoreCase = true) -> "Moderador"
            currentUserRole.equals("admin", ignoreCase = true) || com.example.util.AuthManager.isCurrentUserAdmin() -> "Administrador"
            else -> "Soporte Coach"
        }
    }

    val senderCleanName = remember(currentUserName, authUser) {
        val nick = currentUserName
        if (nick.isNotBlank() && !nick.contains("@") && !nick.equals("Invocador", ignoreCase = true)) {
            nick
        } else {
            authUser?.displayName?.takeIf { it.isNotBlank() && !it.contains("@") } ?: ""
        }
    }

    val responderName = remember(senderCleanName, responderRoleLabel) {
        if (senderCleanName.isNotBlank()) {
            "$senderCleanName ($responderRoleLabel)"
        } else {
            responderRoleLabel
        }
    }

    // Resolver el nombre de usuario exacto (NUNCA usar iniciales de email)
    var resolvedUserName by remember {
        mutableStateOf(
            when {
                userName.isNotBlank() && !userName.contains("@") -> userName.trim()
                else -> ""
            }
        )
    }

    // Historial reactivo de la conversación
    var conversationMessages by remember {
        mutableStateOf(SupportReplyManager.getConversation(context, reportId))
    }

    // Inicializar con mensaje inicial del usuario si la conversación no lo tiene
    LaunchedEffect(reportId, reportDescription) {
        if (conversationMessages.none { it.senderRole.equals("USER", ignoreCase = true) } && reportDescription.isNotBlank()) {
            val initial = com.example.data.SupportMessageEntry(
                id = "${reportId}_initial",
                senderName = resolvedUserName.ifBlank { "Invocador" },
                senderRole = "USER",
                text = reportDescription,
                timestampMillis = System.currentTimeMillis(),
                isGreeting = false
            )
            val merged = listOf(initial) + conversationMessages
            conversationMessages = merged
            SupportReplyManager.saveConversation(context, reportId, merged)
        }
    }

    // Escuchar en tiempo real cambios en el ticket (para ver nuevas respuestas del usuario o de otros administradores al instante)
    DisposableEffect(reportId) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val listenerReg = db.collection("support_reports").document(reportId)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null && snapshot.exists()) {
                    val dbUser = snapshot.getString("userName") ?: snapshot.getString("displayName") ?: ""
                    if (dbUser.isNotBlank() && !dbUser.contains("@") && resolvedUserName.isBlank()) {
                        resolvedUserName = dbUser.trim()
                    }

                    val desc = snapshot.getString("description") ?: snapshot.getString("content") ?: reportDescription
                    val remoteConv = snapshot.get("conversation") as? List<Map<String, Any>>
                    val parsed = if (!remoteConv.isNullOrEmpty()) {
                        remoteConv.mapNotNull { item ->
                            val text = item["text"] as? String ?: return@mapNotNull null
                            com.example.data.SupportMessageEntry(
                                id = item["id"] as? String ?: java.util.UUID.randomUUID().toString(),
                                senderName = item["senderName"] as? String ?: "Soporte",
                                senderRole = item["senderRole"] as? String ?: "SUPPORT",
                                text = text,
                                timestampMillis = (item["timestampMillis"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                                isGreeting = (item["isGreeting"] as? Boolean) ?: false,
                                senderEmail = (item["senderEmail"] as? String) ?: (item["authorEmail"] as? String)
                            )
                        }
                    } else emptyList()

                    val hasUserInitial = parsed.any { it.senderRole.equals("USER", ignoreCase = true) }
                    val fullList = if (!hasUserInitial && desc.isNotBlank()) {
                        listOf(
                            com.example.data.SupportMessageEntry(
                                id = "${reportId}_initial",
                                senderName = resolvedUserName.ifBlank { "Invocador" },
                                senderRole = "USER",
                                text = desc,
                                timestampMillis = snapshot.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                                isGreeting = false
                            )
                        ) + parsed
                    } else {
                        parsed
                    }
                    conversationMessages = fullList
                    SupportReplyManager.saveConversation(context, reportId, fullList)
                }
            }

        onDispose {
            listenerReg.remove()
        }
    }

    // Consultar el nombre exacto del usuario por email si aún no lo tenemos
    LaunchedEffect(reportId, userEmail) {
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            if (resolvedUserName.isBlank() && userEmail.isNotBlank()) {
                val userDocs = db.collection("users").whereEqualTo("email", userEmail.trim()).limit(1).get().await()
                if (!userDocs.isEmpty) {
                    val u = userDocs.documents[0]
                    val exactNick = u.getString("userName")
                        ?: u.getString("displayName")
                        ?: u.getString("name")
                        ?: u.getString("summonerName")
                        ?: ""
                    if (exactNick.isNotBlank() && !exactNick.contains("@")) {
                        resolvedUserName = exactNick.trim()
                    }
                }
            }
        } catch (_: Exception) {}
    }

    val displayUserName = resolvedUserName.ifBlank { "Invocador" }

    val quickTemplates = remember(displayUserName, tag) {
        if (com.example.data.SupportConversationPolicy.isAdministratorOnly(tag) && tag.uppercase() in setOf("PAGO", "PAYMENT", "PAGAMENTO")) listOf(
            "Recibimos tu solicitud de pago USDT. La revisión y el pago manual pueden demorar de 24 a 72 horas.",
            "Estamos comprobando el importe, la red y la dirección de tu billetera USDT. Te confirmaremos el resultado en esta conversación.",
            "Comprueba que la billetera indicada corresponde a la red seleccionada. No envíes claves privadas ni frases de recuperación."
        ) else listOf(
            "Gracias por tu reporte. Estamos revisando lo que nos indicas y te responderemos en esta conversación.",
            "Indica qué pantalla abriste y los pasos previos al problema para que podamos reproducirlo. No compartas datos privados de tu cuenta de juego ni de tu vida personal.",
            "Prueba cerrando y abriendo Coach. Si el problema continúa, cuéntanos qué ocurrió.",
            "Comprueba que utilizas la versión más reciente de Coach e indícanos el número de versión que aparece en la app.",
            "¿El problema sigue ocurriendo después de la última indicación? Puedes responder aquí para continuar la revisión."
        )
    }
    val hasPriorSupportReply = remember(conversationMessages) {
        conversationMessages.any { it.senderRole.equals("SUPPORT", ignoreCase = true) }
    }

    var replyText by remember {
        mutableStateOf("")
    }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Dialog(
        onDismissRequest = { if (!isSending) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HextechDarkBg.copy(alpha = 0.92f))
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 720.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                border = BorderStroke(1.5.dp, HextechCyan)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Encabezado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onDismiss,
                                enabled = !isSending,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = com.example.util.trNullable("Volver"),
                                    tint = HextechCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(HextechCyan.copy(alpha = 0.15f))
                                    .border(1.dp, HextechCyan, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Forum,
                                    contentDescription = null,
                                    tint = HextechCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = com.example.util.tr("Conversación de Soporte"),
                                    color = HextechGold,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = com.example.util.tr("Usuario: $displayUserName"),
                                    color = HextechCyan,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            enabled = !isSending,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = tr("Cerrar"), tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tarjeta con información del reporte original inicial
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(HextechSurface)
                            .border(1.dp, HextechCardBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = com.example.util.tr(reportTitle.ifBlank { "Ticket de soporte" }),
                                    color = HextechCyan,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                if (userEmail.isNotBlank()) {
                                    Text(
                                        text = com.example.util.tr(userEmail),
                                        color = TextMuted,
                                        fontSize = 10.5.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = com.example.util.tr(reportDescription),
                                color = TextSecondary,
                                fontSize = 11.5.sp,
                                lineHeight = 15.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sección de Historial de Conversación
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = com.example.util.tr("Historial de Conversación (${conversationMessages.size}):"),
                            color = HextechGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (conversationMessages.isNotEmpty()) {
                            Text(
                                text = com.example.util.tr("En vivo"),
                                color = Color(0xFF10B981),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (conversationMessages.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(HextechSurfaceVariant.copy(alpha = 0.5f))
                                .border(0.8.dp, HextechCardBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = com.example.util.tr("Aún no hay respuestas enviadas. Selecciona una plantilla o escribe tu mensaje abajo."),
                                color = TextMuted,
                                fontSize = 11.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            conversationMessages.forEach { msg ->
                                val isFromSupport = msg.senderRole != "USER"
                                val bubbleBorderColor = if (isFromSupport) HextechCyan.copy(alpha = 0.6f) else HextechGold.copy(alpha = 0.6f)
                                val bubbleBg = if (isFromSupport) HextechDarkBg else HextechSurface
                                val roleColor = if (isFromSupport) HextechCyan else HextechGold

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = bubbleBg,
                                    border = BorderStroke(1.dp, bubbleBorderColor),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = if (msg.senderRole == "SYSTEM") com.example.util.localizedString(com.example.R.string.support_system_name) else com.example.util.tr(if (isFromSupport) "🛡️ Soporte Coach (${msg.senderName})" else "👤 $displayUserName"),
                                                        color = roleColor,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    )
                                                    if (msg.isGreeting || SupportReplyManager.isDefaultGreeting(msg.text)) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = HextechCyan.copy(alpha = 0.2f),
                                                            border = BorderStroke(0.5.dp, HextechCyan.copy(alpha = 0.5f))
                                                        ) {
                                                            Text(
                                                                text = com.example.util.tr("Saludo predeterminado"),
                                                                color = HextechCyan,
                                                                fontSize = 8.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                maxLines = 1,
                                                                softWrap = false,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                if (isFromSupport && !msg.senderEmail.isNullOrBlank()) {
                                                    Text(
                                                        text = com.example.util.tr(msg.senderEmail),
                                                        color = TextMuted,
                                                        fontSize = 9.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = com.example.util.tr(dateFormat.format(Date(msg.timestampMillis))),
                                                color = TextMuted,
                                                fontSize = 9.5.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (msg.senderRole == "SYSTEM") com.example.util.localizedString(com.example.R.string.support_system_greeting) else com.example.util.tr(msg.text),
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Plantillas rápidas de respuesta
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = com.example.util.tr("Plantillas rápidas para $displayUserName:"),
                            color = HextechGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(
                            onClick = { replyText = "" },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(tr("Eliminar texto"), color = Color(0xFFEF4444), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickTemplates.forEachIndexed { index, tpl ->
                            val label = when (index) {
                                0 -> "✅ Solucionado"
                                1 -> "🔄 Reinicio"
                                2 -> "🛡️ Cuenta"
                                3 -> "🔍 Revisión"
                                4 -> "💡 Guía"
                                else -> "Mensaje"
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(HextechSurfaceVariant)
                                    .border(0.8.dp, HextechCardBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        replyText = SupportReplyManager.sanitizePlainText(com.example.util.appTr(tpl), 500)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = com.example.util.tr(label),
                                    color = HextechCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Campo de redacción de respuesta
                    Text(
                        text = com.example.util.tr("Escribir nuevo mensaje o seguimiento:"),
                        color = HextechCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = SupportReplyManager.sanitizePlainText(it, 500) },
                        placeholder = {
                            Text(
                                text = com.example.util.tr("Escribe aquí la respuesta para $displayUserName..."),
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 180.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HextechCyan,
                            unfocusedBorderColor = HextechCardBorder,
                            focusedContainerColor = HextechSurface,
                            unfocusedContainerColor = HextechSurface
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = com.example.util.tr("${replyText.length}/500"),
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Checkbox para marcar como leído / solucionado
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { markAsRead = !markAsRead },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = markAsRead,
                            onCheckedChange = { markAsRead = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = HextechCyan,
                                checkmarkColor = HextechDarkBg
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = com.example.util.tr("Marcar mensaje como leído / atendido"),
                            color = TextPrimary,
                            fontSize = 11.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            isSending = true
                            coroutineScope.launch {
                                val ok = SupportReplyManager.updateReportStatus(context, reportId, "SOLVED")
                                isSending = false
                                if (ok) onDismiss()
                                else Toast.makeText(context, com.example.util.appTr("No se pudo sincronizar el estado. Inténtalo de nuevo."), Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isSending,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(com.example.util.localizedString(com.example.R.string.support_close_conversation), color = HextechCyan)
                    }
                    Text(com.example.util.localizedString(com.example.R.string.support_close_explanation),
                        color = TextMuted, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Botones de acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Si hay correo disponible, botón adicional para abrir app de email
                        if (userEmail.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    val cleanReply = replyText.trim()
                                    if (cleanReply.isNotBlank()) {
                                        try {
                                            val intent = SupportReplyManager.createEmailReplyIntent(
                                                email = userEmail,
                                                title = reportTitle,
                                                replyText = cleanReply
                                            )
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, com.example.util.appTr("No se encontró aplicación de correo"), Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, com.example.util.appTr("Escribe una respuesta antes de enviar por correo"), Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(tr("Vía Correo"), color = HextechCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Botón para cerrar el diálogo al terminar
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .height(42.dp),
                            border = BorderStroke(1.dp, TextMuted.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text(com.example.util.tr("Cerrar"), color = TextMuted, fontSize = 11.5.sp)
                        }

                        // Botón de guardar y enviar respuesta (permite seguir mandando mensajes)
                        Button(
                            onClick = {
                                val cleanText = replyText.trim()
                                if (cleanText.isBlank()) {
                                    Toast.makeText(context, com.example.util.appTr("El mensaje no puede estar vacío"), Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isSending = true
                                coroutineScope.launch {
                                    val ok = SupportReplyManager.sendSupportReply(
                                        context = context,
                                        reportId = reportId,
                                        replyText = cleanText,
                                        author = responderName,
                                        authorEmail = authUser?.email ?: "",
                                        userEmail = userEmail,
                                        userId = userId,
                                        reportTitle = reportTitle,
                                        reportDescription = reportDescription,
                                        tag = tag,
                                        isFirestoreDoc = isFirestoreDoc,
                                        markAsRead = markAsRead
                                    )
                                    isSending = false
                                    if (!ok) {
                                        Toast.makeText(context, com.example.util.appTr("No se pudo enviar el mensaje. Inténtalo de nuevo."), Toast.LENGTH_LONG).show()
                                        return@launch
                                    }
                                    // Actualizar el historial local mostrado en pantalla
                                    val updatedConv = SupportReplyManager.getConversation(context, reportId)
                                    conversationMessages = updatedConv
                                    replyText = "" // Dejar campo listo para enviar más mensajes
                                    Toast.makeText(context, com.example.util.appTr("Mensaje enviado exitosamente. Puedes seguir respondiendo."), Toast.LENGTH_SHORT).show()
                                    onReplySent(cleanText, markAsRead)
                                }
                            },
                            enabled = !isSending,
                            modifier = Modifier
                                .weight(1.3f)
                                .height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HextechCyan),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isSending) {
                                CircularProgressIndicator(color = HextechDarkBg, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, tint = HextechDarkBg, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(com.example.util.tr("Responder"), color = HextechDarkBg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Botón para finalizar / cerrar conversación
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp),
                        border = BorderStroke(1.dp, HextechCardBorder),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(tr("Cerrar Panel de Conversación"), color = TextSecondary, fontSize = 11.5.sp)
                    }
                }
            }
        }
    }
}
