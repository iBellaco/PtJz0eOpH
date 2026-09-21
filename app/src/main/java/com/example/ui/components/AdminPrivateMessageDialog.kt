package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import java.util.UUID

enum class MessageTag(
    val id: String,
    val label: String,
    val emoji: String,
    val badgeBg: Color,
    val textColor: Color
) {
    SUPPORT("support", "Soporte", "🎧", Color(0xFF0EA5E9), Color.White),
    PATROCINADOR("patrocinador", "Patrocinador", "💼", Color(0xFFC89B3C), Color.Black),
    AVISO("aviso", "Aviso", "📢", Color(0xFF3B82F6), Color.White),
    IMPORTANTE("importante", "Importante", "🚨", Color(0xFFEF4444), Color.White),
    MANTENIMIENTO("mantenimiento", "Mantenimiento", "🛠️", Color(0xFFF97316), Color.White),
    OFERTA("oferta", "Oferta", "💎", Color(0xFFEAB308), Color.Black),
    PRUEBA("prueba", "Prueba", "🧪", Color(0xFF06B6D4), Color.Black);

    companion object {
        fun fromId(id: String?): MessageTag {
            if (id.equals("support", ignoreCase = true) || id.equals("soporte", ignoreCase = true)) return SUPPORT
            if (id.equals("patrocinador", ignoreCase = true) || id.equals("sponsor", ignoreCase = true) || id.equals("publicidad", ignoreCase = true)) return PATROCINADOR
            return values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: AVISO
        }
    }
}

enum class MessageAudienceTarget(val label: String) {
    SINGLE_USER("Este usuario"),
    ALL_USERS("Todos los usuarios"),
    PREMIUM_ONLY("Solo Exclusivos")
}

@Composable
fun AdminPrivateMessageDialog(
    userUid: String,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf(MessageTag.AVISO) }
    var targetAudience by remember { mutableStateOf(MessageAudienceTarget.SINGLE_USER) }
    var isProcessing by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Message, contentDescription = null, tint = Color(0xFFF59E0B))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar Mensaje / Comunicado", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Selector de Etiquetas (Mantenimiento, Importante, Prueba, Oferta, Aviso)
                Text("Etiqueta del Mensaje:", color = Color.LightGray, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MessageTag.values().forEach { tag ->
                        val isSelected = selectedTag == tag
                        Button(
                            onClick = { selectedTag = tag },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) tag.badgeBg else tag.badgeBg.copy(alpha = 0.25f)
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color.White) else null
                        ) {
                            Text(
                                "${tag.emoji} ${tag.label}",
                                color = if (isSelected) tag.textColor else Color.White,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Destinatarios:", color = Color.LightGray, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MessageAudienceTarget.values().forEach { target ->
                        val isSelected = targetAudience == target
                        val btnColor = when (target) {
                            MessageAudienceTarget.SINGLE_USER -> Color(0xFF0EA5E9)
                            MessageAudienceTarget.ALL_USERS -> Color(0xFF8B5CF6)
                            MessageAudienceTarget.PREMIUM_ONLY -> Color(0xFFF59E0B)
                        }
                        Button(
                            onClick = { targetAudience = target },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) btnColor else btnColor.copy(alpha = 0.15f)
                            )
                        ) {
                            val icon = when (target) {
                                MessageAudienceTarget.SINGLE_USER -> Icons.Default.Person
                                MessageAudienceTarget.ALL_USERS -> Icons.Default.People
                                MessageAudienceTarget.PREMIUM_ONLY -> Icons.Default.Star
                            }
                            Icon(icon, contentDescription = null, tint = if (isSelected) Color.White else btnColor, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                target.label,
                                color = if (isSelected) Color.White else btnColor,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título (ej: Nueva Actualización)", color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFF59E0B)
                    )
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Mensaje del comunicado...", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFF59E0B)
                    )
                )
                
                if (statusText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(statusText, color = Color(0xFF38BDF8), fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, enabled = !isProcessing) {
                        Text("Cancelar", color = Color.Gray)
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank() && content.isNotBlank()) {
                                isProcessing = true
                                statusText = "Enviando mensaje..."
                                val db = FirebaseFirestore.getInstance()

                                when (targetAudience) {
                                    MessageAudienceTarget.SINGLE_USER -> {
                                        val messageId = UUID.randomUUID().toString()
                                        val messageData = hashMapOf<String, Any>(
                                            "id" to messageId,
                                            "title" to title.trim(),
                                            "content" to content.trim(),
                                            "tag" to selectedTag.id,
                                            "timestamp" to System.currentTimeMillis(),
                                            "isRead" to false
                                        )
                                        val userDocRef = db.collection("users").document(userUid)
                                        userDocRef.collection("messages").document(messageId)
                                            .set(messageData)
                                            .addOnSuccessListener {
                                                userDocRef.update(
                                                    "hasUnreadMessages", true,
                                                    "unreadMessagesCount", FieldValue.increment(1),
                                                    "privateMessages", FieldValue.arrayUnion(messageData)
                                                ).addOnCompleteListener {
                                                    isProcessing = false
                                                    Toast.makeText(context, "¡Mensaje enviado con éxito!", Toast.LENGTH_SHORT).show()
                                                    onSuccess()
                                                    onDismiss()
                                                }
                                            }
                                            .addOnFailureListener {
                                                userDocRef.update(
                                                    "hasUnreadMessages", true,
                                                    "unreadMessagesCount", FieldValue.increment(1),
                                                    "privateMessages", FieldValue.arrayUnion(messageData)
                                                ).addOnCompleteListener {
                                                    isProcessing = false
                                                    Toast.makeText(context, "¡Mensaje enviado!", Toast.LENGTH_SHORT).show()
                                                    onSuccess()
                                                    onDismiss()
                                                }
                                            }
                                    }

                                    MessageAudienceTarget.ALL_USERS,
                                    MessageAudienceTarget.PREMIUM_ONLY -> {
                                        db.collection("users").get().addOnSuccessListener { snapshot ->
                                            val now = System.currentTimeMillis()
                                            val targetDocs = snapshot.documents.filter { doc ->
                                                if (targetAudience == MessageAudienceTarget.PREMIUM_ONLY) {
                                                    val role = doc.getString("role") ?: "free"
                                                    val until = doc.getLong("premiumUntil")
                                                    role == "admin" || (role == "premium" && (until == null || until == 0L || until > now))
                                                } else {
                                                    true
                                                }
                                            }

                                            if (targetDocs.isEmpty()) {
                                                isProcessing = false
                                                Toast.makeText(context, "No se encontraron usuarios destinatarios.", Toast.LENGTH_SHORT).show()
                                                return@addOnSuccessListener
                                            }

                                            var completedCount = 0
                                            val total = targetDocs.size
                                            statusText = "Entregando a $total usuarios..."

                                            for (doc in targetDocs) {
                                                val messageId = UUID.randomUUID().toString()
                                                val messageData = hashMapOf<String, Any>(
                                                    "id" to messageId,
                                                    "title" to title.trim(),
                                                    "content" to content.trim(),
                                                    "tag" to selectedTag.id,
                                                    "timestamp" to System.currentTimeMillis(),
                                                    "isRead" to false
                                                )
                                                val uRef = doc.reference
                                                uRef.collection("messages").document(messageId).set(messageData)
                                                uRef.update(
                                                    "hasUnreadMessages", true,
                                                    "unreadMessagesCount", FieldValue.increment(1),
                                                    "privateMessages", FieldValue.arrayUnion(messageData)
                                                ).addOnCompleteListener {
                                                    completedCount++
                                                    if (completedCount >= total) {
                                                        isProcessing = false
                                                        Toast.makeText(context, "¡Comunicado enviado a $total usuario(s)!", Toast.LENGTH_LONG).show()
                                                        onSuccess()
                                                        onDismiss()
                                                    }
                                                }
                                            }
                                        }.addOnFailureListener { e ->
                                            isProcessing = false
                                            Toast.makeText(context, "Error obteniendo usuarios: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isProcessing && title.isNotBlank() && content.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Text("Enviar")
                        }
                    }
                }
            }
        }
    }
}

data class UserDirectMessageItem(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val tag: String = "aviso",
    val timestamp: Long = 0L,
    val isRead: Boolean = false
)

@Composable
fun AdminUserMessagesViewerDialog(
    userUid: String,
    userName: String,
    onDismiss: () -> Unit,
    onOpenSendNewMessage: () -> Unit
) {
    val context = LocalContext.current
    var messages by remember { mutableStateOf<List<UserDirectMessageItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isDeletingId by remember { mutableStateOf<String?>(null) }

    fun loadMessages() {
        isLoading = true
        errorMessage = null
        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection("users").document(userUid)

        userDocRef.get()
            .addOnSuccessListener { userDoc ->
                val resultMessages = mutableMapOf<String, UserDirectMessageItem>()

                // 1. Extraer del array 'privateMessages' del documento del usuario (siempre accesible para administradores)
                @Suppress("UNCHECKED_CAST")
                val pMsgs = userDoc.get("privateMessages") as? List<Map<String, Any>>
                if (pMsgs != null) {
                    for (m in pMsgs) {
                        val id = m["id"] as? String ?: continue
                        resultMessages[id] = UserDirectMessageItem(
                            id = id,
                            title = m["title"] as? String ?: "",
                            content = m["content"] as? String ?: "",
                            tag = m["tag"] as? String ?: "aviso",
                            timestamp = (m["timestamp"] as? Long) ?: 0L,
                            isRead = (m["isRead"] as? Boolean) ?: false
                        )
                    }
                }

                // 2. Intentar también leer de la subcolección 'messages' si está permitida y complementar
                userDocRef.collection("messages").get()
                    .addOnSuccessListener { subSnap ->
                        for (doc in subSnap.documents) {
                            val id = doc.id
                            resultMessages[id] = UserDirectMessageItem(
                                id = id,
                                title = doc.getString("title") ?: "",
                                content = doc.getString("content") ?: "",
                                tag = doc.getString("tag") ?: "aviso",
                                timestamp = doc.getLong("timestamp") ?: 0L,
                                isRead = doc.getBoolean("isRead") ?: false
                            )
                        }
                        messages = resultMessages.values.sortedByDescending { it.timestamp }
                        isLoading = false
                    }
                    .addOnFailureListener {
                        // Si la subcolección tiene restricción de reglas de seguridad, usamos con éxito los datos de 'privateMessages'
                        messages = resultMessages.values.sortedByDescending { it.timestamp }
                        isLoading = false
                    }
            }
            .addOnFailureListener { e ->
                // Si falla el documento de usuario, intentamos consultar directamente la subcolección
                userDocRef.collection("messages").get()
                    .addOnSuccessListener { subSnap ->
                        val list = subSnap.documents.map { doc ->
                            UserDirectMessageItem(
                                id = doc.id,
                                title = doc.getString("title") ?: "",
                                content = doc.getString("content") ?: "",
                                tag = doc.getString("tag") ?: "aviso",
                                timestamp = doc.getLong("timestamp") ?: 0L,
                                isRead = doc.getBoolean("isRead") ?: false
                            )
                        }
                        messages = list.sortedByDescending { it.timestamp }
                        isLoading = false
                    }
                    .addOnFailureListener { subErr ->
                        errorMessage = "Error cargando mensajes: ${e.message ?: subErr.message}"
                        isLoading = false
                    }
            }
    }

    LaunchedEffect(userUid) {
        loadMessages()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.80f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Message, contentDescription = null, tint = Color(0xFFF59E0B))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Mensajes Enviados",
                                color = Color(0xFFF59E0B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                "Usuario: $userName",
                                color = Color.LightGray,
                                fontSize = 11.5.sp,
                                maxLines = 1
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action row: Nuevo Mensaje & Recargar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total: ${messages.size} mensaje(s)",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { loadMessages() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Recargar", fontSize = 10.5.sp, color = Color.White)
                        }

                        Button(
                            onClick = {
                                onDismiss()
                                onOpenSendNewMessage()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Nuevo Mensaje", fontSize = 10.5.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(10.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFF59E0B))
                    }
                } else if (errorMessage != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(errorMessage ?: "", color = Color(0xFFEF4444), fontSize = 12.sp)
                    }
                } else if (messages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No hay mensajes enviados a este usuario", color = Color.Gray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    onDismiss()
                                    onOpenSendNewMessage()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Enviar primer mensaje", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = messages,
                            key = { it.id }
                        ) { msg ->
                            val msgTag = MessageTag.fromId(msg.tag)
                            val dateStr = if (msg.timestamp > 0) {
                                java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(msg.timestamp))
                            } else "Fecha desc."

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, msgTag.badgeBg.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = msgTag.badgeBg.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, msgTag.badgeBg.copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = "${msgTag.emoji} ${msgTag.label.uppercase()}",
                                                color = msgTag.badgeBg,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = if (msg.isRead) Color(0xFF00FF66).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = if (msg.isRead) "✓ Leído" else "⏳ Pendiente",
                                                    color = if (msg.isRead) Color(0xFF00FF66) else Color(0xFFF59E0B),
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(6.dp))

                                            IconButton(
                                                onClick = {
                                                    isDeletingId = msg.id
                                                    val db = FirebaseFirestore.getInstance()
                                                    val userDocRef = db.collection("users").document(userUid)

                                                    // 1. Intentar borrar de la subcolección
                                                    userDocRef.collection("messages").document(msg.id).delete()

                                                    // 2. Borrar del array 'privateMessages' del documento principal
                                                    userDocRef.get().addOnSuccessListener { snap ->
                                                        @Suppress("UNCHECKED_CAST")
                                                        val pMsgs = snap.get("privateMessages") as? List<Map<String, Any>>
                                                        if (pMsgs != null) {
                                                            val updated = pMsgs.filter { (it["id"] as? String) != msg.id }
                                                            val remainingUnread = updated.count { (it["isRead"] as? Boolean) == false }
                                                            userDocRef.update(
                                                                "privateMessages", updated,
                                                                "hasUnreadMessages", remainingUnread > 0,
                                                                "unreadMessagesCount", remainingUnread
                                                            ).addOnCompleteListener {
                                                                isDeletingId = null
                                                                messages = messages.filter { it.id != msg.id }
                                                                Toast.makeText(context, "Mensaje eliminado.", Toast.LENGTH_SHORT).show()
                                                            }
                                                        } else {
                                                            isDeletingId = null
                                                            messages = messages.filter { it.id != msg.id }
                                                            Toast.makeText(context, "Mensaje eliminado.", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }.addOnFailureListener {
                                                        isDeletingId = null
                                                        messages = messages.filter { it.id != msg.id }
                                                        Toast.makeText(context, "Mensaje eliminado.", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.size(24.dp),
                                                enabled = isDeletingId != msg.id
                                            ) {
                                                if (isDeletingId == msg.id) {
                                                    CircularProgressIndicator(modifier = Modifier.size(12.dp), color = Color.White)
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Eliminar",
                                                        tint = Color(0xFFEF4444),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = msg.title,
                                        color = Color.White,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = msg.content,
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "📅 $dateStr",
                                        color = Color.Gray,
                                        fontSize = 9.5.sp
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
