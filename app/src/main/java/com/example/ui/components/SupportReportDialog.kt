package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.tasks.await
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.BuildConfig
import com.example.data.supabase.FeedbackRepository
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.HextechSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.DevicePhotoModelDetector
import com.example.util.AuthManager
import com.example.util.ImageUtils
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Diálogo accesible desde el panel de usuario para enviar un reporte de soporte.
 * Requiere título obligatorio, descripción obligatoria, y permite hasta 3 fotos opcionales
 * con un peso máximo de 2 MB por cada una.
 */
@Composable
fun SupportReportDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val auth = AuthManager.getAuth()
    val currentUser = auth?.currentUser

    val userRole by com.example.util.SubscriptionManager.userRole.collectAsState()
    val isSponsorUser = userRole.equals("patrocinador", ignoreCase = true) || userRole.equals("admin", ignoreCase = true) || AuthManager.isCurrentUserAdmin()

    var selectedTag by remember(isSponsorUser) { mutableStateOf(if (isSponsorUser) "PATROCINADOR" else "SOPORTE") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf(false) }
    var descriptionError by remember { mutableStateOf(false) }

    // Almacena las fotos en formato Base64 para subida e imágenes decodificadas para vista previa
    val base64Photos = remember { mutableStateListOf<String>() }
    var isSubmitting by remember { mutableStateOf(false) }
    var previewZoomBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 3)
    ) { uris ->
        if (uris.isNotEmpty()) {
            coroutineScope.launch(Dispatchers.IO) {
                val addedList = mutableListOf<String>()
                for (uri in uris) {
                    if (base64Photos.size + addedList.size >= 3) break

                    // Validar tamaño máximo estricto de 2 MB
                    var fileSizeInBytes: Long = 0
                    try {
                        val cursor = context.contentResolver.query(uri, null, null, null, null)
                        if (cursor != null && cursor.moveToFirst()) {
                            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                            if (sizeIndex != -1) {
                                fileSizeInBytes = cursor.getLong(sizeIndex)
                            }
                            cursor.close()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    if (fileSizeInBytes > 2 * 1024 * 1024) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "⚠️ Una foto supera el límite de 2 MB y fue descartada.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        continue
                    }

                    val b64 = ImageUtils.uriToBase64(context, uri)
                    if (b64 != null) {
                        addedList.add(b64)
                    }
                }

                withContext(Dispatchers.Main) {
                    val spaceLeft = 3 - base64Photos.size
                    val toAdd = addedList.take(spaceLeft)
                    base64Photos.addAll(toAdd)
                    if (toAdd.isNotEmpty()) {
                        Toast.makeText(context, "${toAdd.size} foto(s) adjuntada(s)", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HextechDarkBg.copy(alpha = 0.85f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 680.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                border = BorderStroke(1.5.dp, HextechCyan)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Encabezado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(HextechCyan.copy(alpha = 0.15f))
                                    .border(1.dp, HextechCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HeadsetMic,
                                    contentDescription = null,
                                    tint = HextechCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Reporte de Soporte",
                                    color = HextechGold,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Atención y ayuda al usuario",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        HextechAnimatedIconButton(
                            onClick = onDismiss,
                            enabled = !isSubmitting,
                            size = 32.dp,
                            backgroundColor = androidx.compose.ui.graphics.Color.Transparent,
                            borderColor = androidx.compose.ui.graphics.Color.Transparent,
                            glowColor = HextechCyan
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Selector de Etiqueta (Visible exclusivamente para rol Patrocinador / Admin)
                    if (isSponsorUser) {
                        Text(
                            text = "Etiqueta del reporte",
                            color = HextechGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedTag == "PATROCINADOR",
                                onClick = { selectedTag = "PATROCINADOR" },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (selectedTag == "PATROCINADOR") HextechDarkBg else HextechGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text("Patrocinador", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HextechGold,
                                    selectedLabelColor = HextechDarkBg,
                                    containerColor = HextechSurface,
                                    labelColor = HextechGold
                                ),
                                border = BorderStroke(1.dp, if (selectedTag == "PATROCINADOR") HextechGold else HextechCardBorder)
                            )
                            FilterChip(
                                selected = selectedTag == "SOPORTE",
                                onClick = { selectedTag = "SOPORTE" },
                                label = {
                                    Text("Soporte General", fontSize = 11.sp, fontWeight = if (selectedTag == "SOPORTE") FontWeight.Bold else FontWeight.Normal)
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HextechCyan,
                                    selectedLabelColor = HextechDarkBg,
                                    containerColor = HextechSurface,
                                    labelColor = HextechCyan
                                ),
                                border = BorderStroke(1.dp, if (selectedTag == "SOPORTE") HextechCyan else HextechCardBorder)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Campo: Título (Obligatorio)
                    Text(
                        text = "Título del reporte *",
                        color = if (titleError) DangerRed else HextechCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            if (it.length <= 90) {
                                title = it
                                if (titleError && it.isNotBlank()) titleError = false
                            }
                        },
                        placeholder = { Text("Ej: Error al sincronizar builds / Problema con mi cuenta", fontSize = 12.sp, color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = titleError,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HextechCyan,
                            unfocusedBorderColor = HextechCardBorder,
                            focusedContainerColor = HextechSurface,
                            unfocusedContainerColor = HextechSurface,
                            errorBorderColor = DangerRed
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    if (titleError) {
                        Text(
                            text = "El título es obligatorio.",
                            color = DangerRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Campo: Descripción (Obligatoria)
                    Text(
                        text = "Descripción detallada *",
                        color = if (descriptionError) DangerRed else HextechCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            if (it.length <= 1500) {
                                description = it
                                if (descriptionError && it.isNotBlank()) descriptionError = false
                            }
                        },
                        placeholder = {
                            Text(
                                "Describe claramente qué sucedió, en qué momento y cualquier detalle relevante para que podamos ayudarte lo más pronto posible...",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        isError = descriptionError,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HextechCyan,
                            unfocusedBorderColor = HextechCardBorder,
                            focusedContainerColor = HextechSurface,
                            unfocusedContainerColor = HextechSurface,
                            errorBorderColor = DangerRed
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    if (descriptionError) {
                        Text(
                            text = "La descripción es obligatoria.",
                            color = DangerRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sección Fotos Opcionales (Máx. 3, 2 MB c/u)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Fotos opcionales (Máx. 3)",
                                color = HextechGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Máximo 2 MB por cada imagen",
                                color = TextMuted,
                                fontSize = 10.5.sp
                            )
                        }
                        Text(
                            text = "${base64Photos.size}/3",
                            color = if (base64Photos.size >= 3) HextechGold else TextSecondary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Lista de fotos adjuntas o botón para agregar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (base64Photos.size < 3) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(HextechSurface)
                                    .border(BorderStroke(1.dp, HextechCyan.copy(alpha = 0.6f)), RoundedCornerShape(10.dp))
                                    .clickable(enabled = !isSubmitting) {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Agregar foto",
                                        tint = HextechCyan,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "+ Adjuntar",
                                        color = HextechCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(base64Photos) { index, b64 ->
                                val bitmap = remember(b64) {
                                    try {
                                        val decodedBytes = Base64.decode(b64, Base64.DEFAULT)
                                        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(HextechSurfaceVariant)
                                        .border(BorderStroke(1.dp, HextechCardBorder), RoundedCornerShape(10.dp))
                                ) {
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Foto adjunta $index",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable { previewZoomBitmap = bitmap }
                                        )
                                    }

                                    // Botón para eliminar foto
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(3.dp)
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(DangerRed)
                                            .clickable(enabled = !isSubmitting) {
                                                base64Photos.removeAt(index)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Eliminar",
                                            tint = androidx.compose.ui.graphics.Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Panel Informativo de Modelos Detectados a partir de las Fotos
                    if (base64Photos.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(HextechSurfaceVariant.copy(alpha = 0.5f))
                                .border(1.dp, HextechCyan.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Smartphone,
                                    contentDescription = null,
                                    tint = HextechCyan,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Posibles modelos de celular detectados en fotos:",
                                    color = HextechCyan,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            base64Photos.forEachIndexed { idx, b64 ->
                                val analysis = remember(b64) { DevicePhotoModelDetector.analyzeBase64(b64) }
                                if (analysis != null) {
                                    Text(
                                        text = "• Foto ${idx + 1}: ${analysis.primaryDeviceSummary}",
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Botón Enviar Reporte
                    HextechAnimatedButton(
                        onClick = {
                            val cleanTitle = title.trim()
                            val cleanDesc = description.trim()
                            var hasError = false
                            if (cleanTitle.isBlank()) {
                                titleError = true
                                hasError = true
                            }
                            if (cleanDesc.isBlank()) {
                                descriptionError = true
                                hasError = true
                            }
                            if (hasError) return@HextechAnimatedButton

                            isSubmitting = true
                            coroutineScope.launch {
                                try {
                                    val db = FirebaseFirestore.getInstance()
                                    val userEmail = currentUser?.email ?: "sin_email"
                                    val userId = currentUser?.uid ?: "anonimo"
                                    val subUser = com.example.util.SubscriptionManager.userName.value.trim()
                                    val activeProfile = com.example.data.AccountProfileManager.getActiveProfile(context)
                                    val rawDisplayName = currentUser?.displayName?.trim() ?: ""
                                    val exactUserName = when {
                                        subUser.isNotBlank() && !subUser.contains("@") -> subUser
                                        rawDisplayName.isNotBlank() && !rawDisplayName.contains("@") -> rawDisplayName
                                        activeProfile.name.isNotBlank() && activeProfile.name != "Invocador" && !activeProfile.name.contains("@") -> activeProfile.name.trim()
                                        else -> {
                                            val saved = context.getSharedPreferences("user_profile_prefs", Context.MODE_PRIVATE).getString("username", "") ?: ""
                                            if (saved.isNotBlank() && !saved.contains("@")) saved.trim() else "Invocador"
                                        }
                                    }
                                    val userName = exactUserName

                                    val initialMessageMap = mapOf<String, Any>(
                                        "id" to java.util.UUID.randomUUID().toString(),
                                        "senderName" to userName.ifBlank { "Invocador" },
                                        "senderRole" to "USER",
                                        "text" to cleanDesc,
                                        "timestampMillis" to System.currentTimeMillis(),
                                        "isGreeting" to false
                                    )
                                    val initialConversation = listOf(initialMessageMap)

                                    val reportId = java.util.UUID.randomUUID().toString()
                                    val finalTag = if (selectedTag == "PATROCINADOR") "PATROCINADOR" else "SOPORTE"
                                    val reportMap = hashMapOf<String, Any>(
                                        "id" to reportId,
                                        "title" to cleanTitle,
                                        "description" to cleanDesc,
                                        "tag" to finalTag,
                                        "type" to finalTag,
                                        "userId" to userId,
                                        "userEmail" to userEmail,
                                        "userName" to userName,
                                        "photos" to base64Photos.toList(),
                                        "createdAt" to Timestamp.now(),
                                        "status" to "PENDIENTE",
                                        "isRead" to true,
                                        "userRead" to true,
                                        "hasNewAdminReply" to false,
                                        "hasNewReply" to false,
                                        "hasNewUserReply" to true,
                                        "conversation" to initialConversation,
                                        "appVersion" to "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                                        "device" to "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})"
                                    )

                                    try {
                                        db.collection("support_reports").document(reportId).set(reportMap).await()
                                    } catch (e: Exception) {
                                        Log.w("SupportReportDialog", "Error guardando en support_reports: ${e.message}")
                                    }

                                    val initialEntry = com.example.data.SupportMessageEntry(
                                        id = "${reportId}_initial",
                                        senderName = userName.ifBlank { "Invocador" },
                                        senderRole = "USER",
                                        text = cleanDesc,
                                        timestampMillis = System.currentTimeMillis(),
                                        isGreeting = false
                                    )
                                    com.example.data.SupportReplyManager.saveConversation(context, reportId, listOf(initialEntry))

                                    val targetUserId = if (userId.isNotBlank() && userId != "anonimo") userId else (currentUser?.uid ?: "local_user")
                                    try {
                                        val inboxMsg = hashMapOf<String, Any>(
                                            "id" to reportId,
                                            "title" to "Reporte: $cleanTitle",
                                            "content" to cleanDesc,
                                            "description" to cleanDesc,
                                            "userId" to targetUserId,
                                            "userName" to userName,
                                            "userEmail" to userEmail,
                                            "photos" to base64Photos.toList(),
                                            "timestamp" to System.currentTimeMillis(),
                                            "createdAt" to Timestamp.now(),
                                            "isRead" to true,
                                            "tag" to finalTag,
                                            "sender" to userName,
                                            "reportId" to reportId,
                                            "status" to "PENDIENTE",
                                            "conversation" to initialConversation
                                        )
                                        db.collection("users").document(targetUserId).collection("messages").document(reportId).set(inboxMsg).await()
                                    } catch (e: Exception) {
                                        Log.w("SupportReportDialog", "Error guardando en bandeja de usuario: ${e.message}")
                                    }

                                    // 2. Respaldo adicional en FeedbackRepository si está configurado
                                    try {
                                        FeedbackRepository.submitFeedback(
                                            type = finalTag,
                                            title = cleanTitle,
                                            description = cleanDesc,
                                            email = userEmail,
                                            imagesBase64 = base64Photos.toList(),
                                            userName = userName,
                                            id = reportId
                                        )
                                    } catch (_: Exception) {}

                                    withContext(Dispatchers.Main) {
                                        isSubmitting = false
                                        Toast.makeText(
                                            context,
                                            "✅ Reporte de soporte enviado exitosamente. ¡Gracias!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        onDismiss()
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        isSubmitting = false
                                        Toast.makeText(
                                            context,
                                            "Error al enviar reporte: ${e.localizedMessage}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        backgroundColor = HextechCyan,
                        borderColor = HextechGold,
                        glowColor = HextechCyan,
                        shape = RoundedCornerShape(12.dp),
                        enableShimmer = true,
                        enablePulse = true
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = HextechDarkBg,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Enviando reporte...",
                                color = HextechDarkBg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = HextechDarkBg,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Enviar Reporte de Soporte",
                                color = HextechDarkBg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal de zoom para ver la foto adjunta en tamaño grande
    if (previewZoomBitmap != null) {
        val analysis = remember(previewZoomBitmap) { DevicePhotoModelDetector.analyzeBitmap(previewZoomBitmap) }
        Dialog(onDismissRequest = { previewZoomBitmap = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.9f))
                    .clickable { previewZoomBitmap = null }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        bitmap = previewZoomBitmap!!.asImageBitmap(),
                        contentDescription = "Vista previa foto",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    )
                    if (analysis != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(HextechDarkBg.copy(alpha = 0.9f))
                                .border(1.dp, HextechGold.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "📱 Posible modelo detectado en esta foto:",
                                    color = HextechGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${analysis.landscapeWidth} x ${analysis.landscapeHeight} (${analysis.aspectRatioLabel})",
                                    color = HextechCyan,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Modelos compatibles: ${analysis.probableDeviceModels.joinToString(", ")}",
                                    color = TextPrimary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
