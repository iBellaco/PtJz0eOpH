package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.AppNotice
import com.example.data.AppNoticeManager
import com.example.ui.theme.*
import com.example.util.NoticeMediaStorageManager
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import java.util.UUID

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SponsorCpmPanelDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val userEmail = authUser?.email ?: "patrocinador@coach.app"
    val coroutineScope = rememberCoroutineScope()

    val allNotices by AppNoticeManager.notices.collectAsState()
    val currentBlueEssence by com.example.util.SubscriptionManager.blueEssence.collectAsState()
    var showBuyEssenceDialog by remember { mutableStateOf(false) }

    if (showBuyEssenceDialog) {
        BuyEssenceDialog(isAdmin = false, onDismiss = { showBuyEssenceDialog = false })
    }
    
    // Lista local de anuncios pendientes (guardados en SharedPreferences para evitar que desaparezcan)
    val prefs = context.getSharedPreferences("sponsor_pending_ads", Context.MODE_PRIVATE)
    var localPendingAds by remember { 
        mutableStateOf<List<AppNotice>>(
            try {
                val json = prefs.getString("pending_ads", "[]") ?: "[]"
                val jsonArray = org.json.JSONArray(json)
                val list = mutableListOf<AppNotice>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        AppNotice(
                            id = obj.optString("id", ""),
                            title = obj.optString("title", ""),
                            content = obj.optString("content", ""),
                            videoUrl = obj.optString("videoUrl", ""),
                            expandedImageUrl = obj.optString("expandedImageUrl", ""),
                            externalUrl = obj.optString("externalUrl", ""),
                            tag = obj.optString("tag", "Publicidad"),
                            budget = obj.optDouble("budget", 0.0),
                            budgetUnit = obj.optString("budgetUnit", "day"),
                            durationValue = obj.optInt("durationValue", 1),
                            durationUnit = obj.optString("durationUnit", "day"),
                            approvedAtMillis = obj.optLong("approvedAtMillis", 0L),
                            expiresAtMillis = obj.optLong("expiresAtMillis", 0L),
                            isApproved = obj.optBoolean("isApproved", false),
                            isEnabled = obj.optBoolean("isEnabled", false),
                            sponsorEmail = obj.optString("sponsorEmail", "")
                        )
                    )
                }
                list
            } catch (e: Exception) { emptyList() }
        )
    }

    val now = System.currentTimeMillis()
    val sevenDaysMillis = 7 * 24 * 60 * 60 * 1000L

    val myNotices = remember(allNotices, userEmail, localPendingAds, now) {
        val remoteAds = allNotices.filter { notice ->
            val isPubTag = notice.tag.equals("Publicidad", ignoreCase = true) || notice.tag.equals("Ads", ignoreCase = true)
            (notice.sponsorEmail.equals(userEmail, true) || (notice.sponsorEmail.isBlank() && isPubTag))
        }
        val localFiltered = localPendingAds.filter { it.sponsorEmail.equals(userEmail, true) || it.sponsorEmail.isBlank() }
        val remoteAdIds = remoteAds.map { it.id }.toSet()
        val combined = remoteAds + localFiltered.filter { it.id !in remoteAdIds }
        
        // Conservar visibles durante 7 días después de haber expirado con contador regresivo de eliminación
        combined.filter { notice ->
            if (notice.expiresAtMillis > 0L) {
                val isExpired = now >= notice.expiresAtMillis
                if (isExpired) {
                    val elapsedSinceExp = now - notice.expiresAtMillis
                    elapsedSinceExp <= sevenDaysMillis // Retener durante 7 días
                } else true
            } else true
        }
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var showConfirmReviewDialog by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }
    var horizontalMediaInput by remember { mutableStateOf("") } // Banner horizontal o video horizontal
    var verticalMediaInput by remember { mutableStateOf("") } // Media vertical para pantalla completa
    var externalUrlInput by remember { mutableStateOf("") }
    var titleColor by remember { mutableStateOf("#FFD700") }
    var durationValueInput by remember { mutableStateOf("") }
    var selectedDurationUnit by remember { mutableStateOf("day") } // "hour", "day", "week", "month" (sin opción de 1 año)
    var isUploadingMedia by remember { mutableStateOf(false) }

    var isHorizontalVideo by remember { mutableStateOf(false) }
    var isVerticalVideo by remember { mutableStateOf(false) }
    var noticeToDelete by remember { mutableStateOf<AppNotice?>(null) }

    var currentMinute by remember { mutableStateOf(System.currentTimeMillis() / 60000L) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(10000L) // check every 10s is fine
            currentMinute = System.currentTimeMillis() / 60000L
        }
    }

    val durationValueInt = remember(durationValueInput) {
        durationValueInput.toIntOrNull()?.coerceAtLeast(1) ?: 1
    }

    // El cálculo toma en cuenta ÚNICAMENTE los anuncios de publicidad actualmente vigentes y no expirados
    val activeAdsCount = remember(allNotices, currentMinute) {
        val nowCurrent = System.currentTimeMillis()
        val activePublicidadAds = allNotices.filter { notice ->
            val isPubTag = notice.tag.equals("Publicidad", ignoreCase = true) || 
                           notice.tag.equals("Ads", ignoreCase = true) || 
                           notice.tag.equals("PUBLICIDAD", ignoreCase = true)
            val isLive = notice.isApproved && notice.isEnabled && (notice.expiresAtMillis == 0L || notice.expiresAtMillis > nowCurrent)
            isPubTag && isLive
        }
        activePublicidadAds.size
    }
    val publicationsMultiplier = remember(activeAdsCount) {
        when (activeAdsCount) {
            0, 1 -> 1.0
            2 -> 1.7
            3 -> 2.3
            else -> 2.8 + 0.5 * (activeAdsCount - 3)
        }
    }

    // El presupuesto se calcula de forma justa y accesible para una aplicación en crecimiento
    val autoBudget = remember(
        selectedDurationUnit, 
        durationValueInt,
        isHorizontalVideo,
        isVerticalVideo,
        horizontalMediaInput,
        verticalMediaInput,
        externalUrlInput,
        activeAdsCount
    ) {
        val unitPrice = when (selectedDurationUnit) {
            "hour" -> 0.50
            "day" -> 2.50
            "week" -> 10.00
            "month" -> 30.00
            else -> 2.50
        }
        var total = unitPrice * durationValueInt
        
        if (isHorizontalVideo || isVerticalVideo) {
            total += 1.00 * durationValueInt
        } else if (horizontalMediaInput.isNotBlank() || verticalMediaInput.isNotBlank()) {
            total += 0.50 * durationValueInt
        }
        
        if (externalUrlInput.isNotBlank()) {
            total += 0.50 * durationValueInt
        }
        
        // Más anuncios activos optimizan el costo en lugar de encarecerlo (eficiencia compartida de tráfico)
        val efficiencyDiscount = 1.0 / (1.0 + (activeAdsCount - 1) * 0.1)
        total *= efficiencyDiscount

        String.format(Locale.US, "%.2f", total.coerceAtLeast(0.50))
    }

    // Launcher para seleccionar multimedia horizontal (imágenes PNG/JPG, videos solo MP4 máx 15s, validación estricta de tamaño recomendado)
    val horizontalPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val validation = NoticeMediaUtils.validateMediaForSlot(context, uri, isVerticalSlot = false)
        if (!validation.isValid) {
            Toast.makeText(context, validation.errorMessage ?: "El archivo no cumple con el tamaño o proporción recomendada", Toast.LENGTH_LONG).show()
            return@rememberLauncherForActivityResult
        }

        val isVideo = validation.isVideo
        isUploadingMedia = true
        coroutineScope.launch {
            val result = if (isVideo) {
                NoticeMediaStorageManager.uploadOrSaveVideo(context, uri)
            } else {
                NoticeMediaStorageManager.convertImageToCloudDataUrl(context, uri)
            }
            horizontalMediaInput = result
            isHorizontalVideo = isVideo
            isUploadingMedia = false
            Toast.makeText(context, "Multimedia horizontal válida cargada (${validation.width}x${validation.height})", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher para seleccionar multimedia vertical (imágenes PNG/JPG, videos solo MP4 máx 15s, validación estricta de tamaño recomendado)
    val verticalPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val validation = NoticeMediaUtils.validateMediaForSlot(context, uri, isVerticalSlot = true)
        if (!validation.isValid) {
            Toast.makeText(context, validation.errorMessage ?: "El archivo no cumple con el tamaño o proporción recomendada", Toast.LENGTH_LONG).show()
            return@rememberLauncherForActivityResult
        }

        val isVideo = validation.isVideo
        isUploadingMedia = true
        coroutineScope.launch {
            val result = if (isVideo) {
                NoticeMediaStorageManager.uploadOrSaveVideo(context, uri)
            } else {
                NoticeMediaStorageManager.convertImageToCloudDataUrl(context, uri)
            }
            verticalMediaInput = result
            isVerticalVideo = isVideo
            isUploadingMedia = false
            Toast.makeText(context, "Multimedia vertical válida cargada (${validation.width}x${validation.height})", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = {
            if (showBuyEssenceDialog) {
                showBuyEssenceDialog = false
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
            if (showBuyEssenceDialog) {
                showBuyEssenceDialog = false
            } else {
                onDismiss()
            }
        }
        Surface(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = HextechDarkBg,
            border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = HextechGold, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Panel CPM de Patrocinador", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Gestiona tus anuncios publicitarios y presupuestos", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Calendario de Disponibilidad (Lo primero que ven antes de publicar un anuncio)
                AdAvailabilityCalendarPanel(allNotices = allNotices)

                Spacer(modifier = Modifier.height(14.dp))

                // Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tus Anuncios (${myNotices.size})", color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Button(
                        onClick = {
                            titleInput = ""
                            contentInput = ""
                            horizontalMediaInput = ""
                            verticalMediaInput = ""
                            externalUrlInput = ""
                            selectedDurationUnit = "day"
                            durationValueInput = "1"
                            showCreateDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = HextechDarkBg, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Nuevo Anuncio", color = HextechDarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // List of notices
                if (myNotices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AdsClick, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No tienes anuncios publicados.", color = TextSecondary, fontSize = 14.sp)
                            Text("Crea uno y espera la aprobación del administrador.", color = TextSecondary.copy(alpha = 0.7f), fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(myNotices, key = { it.id }) { notice ->
                            SponsorNoticeCard(notice = notice, onDelete = {
                                noticeToDelete = notice
                            })
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        val quantityLabel = when (selectedDurationUnit) {
            "hour" -> "Cantidad de horas"
            "day" -> "Cantidad de días"
            "week" -> "Cantidad de semanas"
            "month" -> "Cantidad de meses"
            else -> "Cantidad"
        }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = HextechSurface,
            title = { Text("Publicar Anuncio CPM", color = HextechGold, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Calendario de Programación y Disponibilidad (Visible antes de publicar)
                    Text("Calendario de Disponibilidad Actual:", color = HextechCyan, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    AdAvailabilityCalendarPanel(allNotices = allNotices)

                    Spacer(modifier = Modifier.height(4.dp))

                    // Título Obligatorio
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Título del Anuncio * (Obligatorio)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HextechGold, 
                            unfocusedBorderColor = HextechSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // Opción de Color del Título
                    Text("Color del Título:", color = HextechCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val colorsList = listOf(
                            "Dorado" to "#FFD700",
                            "Cian" to "#00F2FE",
                            "Blanco" to "#FFFFFF",
                            "Verde" to "#00FF66",
                            "Naranja" to "#FF9900",
                            "Rojo" to "#FF3333",
                            "Morado" to "#CC66FF"
                        )
                        colorsList.forEach { (name, hex) ->
                            val isSelected = titleColor.equals(hex, true)
                            val parsedColor = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { HextechGold }
                            Button(
                                onClick = { titleColor = hex },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) parsedColor else HextechSurfaceVariant
                                ),
                                border = BorderStroke(1.dp, parsedColor)
                            ) {
                                Text(name, color = if (isSelected) HextechDarkBg else parsedColor, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Multimedia Horizontal (Banner/Video horizontal para inicio)
                    Text("1. Multimedia Horizontal (Banner de Inicio):", color = HextechCyan, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    Text("• Medidas recomendadas: 1920 x 1080 px (Relación 16:9)\n• Formatos: PNG, JPG/JPEG (máx 5 MB) o Video MP4 (máx 15s y 10 MB)", color = TextSecondary, fontSize = 10.sp)
                    
                    Button(
                        onClick = { horizontalPicker.launch(arrayOf("image/jpeg", "image/jpg", "image/png", "video/mp4")) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = HextechSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, HextechGold)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = HextechGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (horizontalMediaInput.isBlank()) "Seleccionar desde Galería (Horizontal)" else "Cambiar Multimedia Horizontal", color = Color.White, fontSize = 12.sp)
                    }

                    // Preview Horizontal
                    if (horizontalMediaInput.isNotBlank()) {
                        val horizontalModel = remember(horizontalMediaInput) {
                            if (horizontalMediaInput.startsWith("/")) File(horizontalMediaInput) else horizontalMediaInput
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = HextechDarkBg,
                                border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.8f))
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    NoticeMediaViewer(
                                        mediaUrl = horizontalMediaInput,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    horizontalMediaInput = ""
                                    isHorizontalVideo = false
                                    Toast.makeText(context, "Multimedia horizontal eliminada", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(32.dp)
                                    .background(HextechDarkBg.copy(alpha = 0.85f), RoundedCornerShape(50))
                                    .border(1.dp, DangerRed, RoundedCornerShape(50))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Borrar multimedia", tint = DangerRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Multimedia Vertical (Imagen/Video vertical para modal pantalla completa)
                    Text("2. Multimedia Vertical (Vista Ampliada):", color = HextechCyan, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    Text("• Medidas recomendadas: 1080 x 1920 px (Relación 9:16)\n• Formatos: PNG, JPG/JPEG (máx 5 MB) o Video MP4 (máx 15s y 10 MB)", color = TextSecondary, fontSize = 10.sp)

                    Button(
                        onClick = { verticalPicker.launch(arrayOf("image/jpeg", "image/jpg", "image/png", "video/mp4")) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = HextechSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, HextechCyan)
                    ) {
                        Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = HextechCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (verticalMediaInput.isBlank()) "Seleccionar desde Galería (Vertical)" else "Cambiar Multimedia Vertical", color = Color.White, fontSize = 12.sp)
                    }
                    // Preview Vertical
                    if (verticalMediaInput.isNotBlank()) {
                        val verticalModel = remember(verticalMediaInput) {
                            if (verticalMediaInput.startsWith("/")) File(verticalMediaInput) else verticalMediaInput
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.55f)
                                    .aspectRatio(9f / 16f)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    color = HextechDarkBg,
                                    border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.8f))
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        NoticeMediaViewer(
                                            mediaUrl = verticalMediaInput,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        verticalMediaInput = ""
                                        isVerticalVideo = false
                                        Toast.makeText(context, "Multimedia vertical eliminada", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(32.dp)
                                        .background(HextechDarkBg.copy(alpha = 0.85f), RoundedCornerShape(50))
                                        .border(1.dp, DangerRed, RoundedCornerShape(50))
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Borrar multimedia", tint = DangerRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // Enlace Web Externo (OPCIONAL)
                    OutlinedTextField(
                        value = externalUrlInput,
                        onValueChange = { externalUrlInput = it },
                        label = { Text("Enlace Web Externo (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HextechGold, 
                            unfocusedBorderColor = HextechSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // Presupuesto Total (Automático - No Modificable)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                        border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Presupuesto Total (Automático)", color = TextSecondary, fontSize = 11.sp)
                                Text("Calculado automáticamente (No modificable)", color = HextechCyan, fontSize = 9.5.sp)
                            }
                            Text(
                                text = "$$autoBudget USD",
                                color = HextechGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    // Desglose de presupuesto
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(HextechDarkBg)
                            .padding(12.dp)
                    ) {
                        Text("Desglose del cálculo real y accesible:", color = HextechGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• Tarifa base justa ($selectedDurationUnit): USD ${String.format(Locale.US, "%.2f", when (selectedDurationUnit) { "hour" -> 0.50; "day" -> 2.50; "week" -> 10.00; "month" -> 30.00; else -> 2.50 } * durationValueInt)}", color = TextSecondary, fontSize = 10.sp)
                        
                        if (isHorizontalVideo || isVerticalVideo) {
                            Text("• Costo de procesamiento multimedia: +$${String.format(Locale.US, "%.2f", 1.00 * durationValueInt)} USD", color = TextSecondary, fontSize = 10.sp)
                        } else if (horizontalMediaInput.isNotBlank() || verticalMediaInput.isNotBlank()) {
                            Text("• Costo de procesamiento de imagen: +$${String.format(Locale.US, "%.2f", 0.50 * durationValueInt)} USD", color = TextSecondary, fontSize = 10.sp)
                        }

                        if (externalUrlInput.isNotBlank()) {
                            Text("• Redirección externa: +$${String.format(Locale.US, "%.2f", 0.50 * durationValueInt)} USD", color = TextSecondary, fontSize = 10.sp)
                        }

                        Text("• Eficiencia por anuncios activos (${activeAdsCount}): tasa optimizada y competitiva", color = TextSecondary, fontSize = 10.sp)

                        val estimatedBudgetFloat = autoBudget.toFloatOrNull() ?: 2.5f
                        val estimatedVisits = (estimatedBudgetFloat * 45).toInt().coerceAtLeast(15)
                        val estimatedClicks = (estimatedVisits * 0.04f).toInt().coerceAtLeast(1)
                        
                        androidx.compose.material3.Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFF334155))
                        Text("Rendimiento Estimado Realista:", color = HextechCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("• Visitas esperadas: ~%,d".format(Locale.getDefault(), estimatedVisits), color = TextSecondary, fontSize = 10.sp)
                        Text("• Clics únicos esperados: ~%,d".format(Locale.getDefault(), estimatedClicks), color = TextSecondary, fontSize = 10.sp)
                    }

                    Text("Duración de la Publicación:", color = HextechCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    
                    // Cantidad Dinámica según Unidad
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = durationValueInput,
                            onValueChange = { newVal -> 
                                if (newVal.all { char -> char.isDigit() }) {
                                    val parsed = newVal.toIntOrNull() ?: 0
                                    val maxVal = when (selectedDurationUnit) {
                                        "hour" -> 12
                                        "day" -> 3
                                        "week" -> 2
                                        "month" -> 6
                                        else -> 1
                                    }
                                    if (parsed <= maxVal) {
                                        durationValueInput = newVal
                                    } else {
                                        durationValueInput = maxVal.toString()
                                        Toast.makeText(context, "Máximo permitido: $maxVal", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            label = { Text(quantityLabel) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HextechGold, 
                                unfocusedBorderColor = HextechSurfaceVariant,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    // Selector de Unidades (Sin opción de 1 año)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "hour" to "Horas",
                            "day" to "Días",
                            "week" to "Semanas",
                            "month" to "Meses"
                        ).forEach { (unitId, unitLabel) ->
                            val isSelected = selectedDurationUnit == unitId
                            FilterChip(
                                selected = isSelected,
                                onClick = { 
                                    selectedDurationUnit = unitId 
                                    val maxVal = when (unitId) {
                                        "hour" -> 12
                                        "day" -> 3
                                        "week" -> 2
                                        "month" -> 6
                                        else -> 1
                                    }
                                    val current = durationValueInput.toIntOrNull() ?: 1
                                    if (current > maxVal) {
                                        durationValueInput = maxVal.toString()
                                    }
                                },
                                label = { Text(unitLabel, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = HextechGold, selectedLabelColor = HextechDarkBg)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    val parsedBudgetVal = autoBudget.replace(',', '.').toDoubleOrNull() ?: 10.0
                    val requiredEssences = (parsedBudgetVal * 10).toLong()
                    val hasEnoughEssence = currentBlueEssence >= requiredEssences

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = HextechSurface,
                        border = BorderStroke(1.dp, if (hasEnoughEssence) HextechGold.copy(alpha = 0.5f) else Color(0xFFEF4444))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Costo en Esencias Azules ($1 = 10 EA):", color = TextSecondary, fontSize = 11.sp)
                                Text("$requiredEssences EA", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Tu Saldo Actual:", color = TextSecondary, fontSize = 11.sp)
                                Text("$currentBlueEssence EA", color = if (hasEnoughEssence) Color(0xFF10B981) else Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            if (!hasEnoughEssence) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = { showBuyEssenceDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Recargar Esencias (Insuficientes)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Nota: El anuncio requiere la aprobación de un administrador para ser visible en la plataforma. Si el anuncio publicado no es aceptado en 7 días, se hace la devolución de las esencias azules. Al vencer permanecerá 7 días en tu historial con contador antes de su eliminación.", color = TextSecondary, fontSize = 10.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedBudget = autoBudget.replace(',', '.').toDoubleOrNull() ?: 10.0
                        val requiredEssences = (parsedBudget * 10).toLong()
                        if (titleInput.trim().isBlank()) {
                            Toast.makeText(context, "El título del anuncio es obligatorio", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (currentBlueEssence < requiredEssences) {
                            Toast.makeText(context, "No tienes suficientes esencias azules ($requiredEssences EA requeridas). Recarga para publicar.", Toast.LENGTH_LONG).show()
                            showBuyEssenceDialog = true
                            return@Button
                        }

                        // Corroborar solapamiento de fechas y horas con anuncios activos o en cola existentes
                        val propStart = System.currentTimeMillis()
                        val propEnd = AppNoticeManager.calculateExpirationMillis(propStart, durationValueInt, selectedDurationUnit)

                        val hasOverlap = allNotices.any { notice ->
                            val isPubTag = notice.tag.equals("Publicidad", ignoreCase = true) || notice.tag.equals("Ads", ignoreCase = true) || notice.tag.equals("PUBLICIDAD", ignoreCase = true)
                            val isExpired = notice.expiresAtMillis > 0L && notice.expiresAtMillis <= propStart
                            if (isPubTag && !isExpired) {
                                val existingStart = if (notice.approvedAtMillis > 0L) notice.approvedAtMillis else propStart
                                val existingEnd = if (notice.expiresAtMillis > 0L) notice.expiresAtMillis else AppNoticeManager.calculateExpirationMillis(existingStart, notice.durationValue, notice.durationUnit)
                                kotlin.math.max(propStart, existingStart) < kotlin.math.min(propEnd, existingEnd)
                            } else false
                        } || localPendingAds.any { notice ->
                            val existingStart = if (notice.approvedAtMillis > 0L) notice.approvedAtMillis else propStart
                            val existingEnd = if (notice.expiresAtMillis > 0L) notice.expiresAtMillis else AppNoticeManager.calculateExpirationMillis(existingStart, notice.durationValue, notice.durationUnit)
                            kotlin.math.max(propStart, existingStart) < kotlin.math.min(propEnd, existingEnd)
                        }

                        if (hasOverlap) {
                            Toast.makeText(context, "⚠️ El horario o fechas seleccionadas ya están ocupadas por otro anuncio. No se puede publicar ni descontar esencias azules.", Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        showConfirmReviewDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                    enabled = !isUploadingMedia
                ) {
                    if (isUploadingMedia) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = HextechDarkBg, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text("Enviar a Revisión", color = HextechDarkBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    if (showConfirmReviewDialog) {
        val parsedBudget = autoBudget.replace(',', '.').toDoubleOrNull() ?: 10.0
        val requiredEssences = (parsedBudget * 10).toLong()

        AlertDialog(
            onDismissRequest = { showConfirmReviewDialog = false },
            containerColor = HextechSurface,
            title = { Text("⚠️ Advertencia de Envío a Revisión", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "• Una vez enviado el anuncio a revisión, NO SE PUEDE MODIFICAR.\n" +
                        "• Si el anuncio no es aceptado en 7 días, se hace la devolución de las esencias azules.\n" +
                        "• Formatos admitidos: Imágenes en formato PNG o JPG/JPEG y Videos en formato MP4 (máximo 10 segundos).\n" +
                        "• Regla de Seguridad y Enlaces: Está estrictamente prohibido agregar enlaces maliciosos, contenido inapropiado o incumplir las normas comunitarias.\n" +
                        "• Penalización: Si se infringe cualquier regla, el anuncio será rechazado permanentemente y no será publicado.",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Text("¿Estás completamente seguro de enviar el anuncio?", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmReviewDialog = false
                        showCreateDialog = false

                        val newPendingNotice = AppNotice(
                            id = UUID.randomUUID().toString(),
                            title = titleInput.trim(),
                            content = contentInput.trim(),
                            videoUrl = horizontalMediaInput.trim(),
                            expandedImageUrl = verticalMediaInput.trim().ifBlank { horizontalMediaInput.trim() },
                            externalUrl = externalUrlInput.trim(),
                            tag = "PUBLICIDAD",
                            titleColor = titleColor,
                            budget = parsedBudget,
                            budgetUnit = selectedDurationUnit,
                            durationValue = durationValueInt,
                            durationUnit = selectedDurationUnit,
                            isApproved = false,
                            isEnabled = false,
                            sponsorEmail = userEmail
                        )

                        val updatedLocalList = localPendingAds + newPendingNotice
                        localPendingAds = updatedLocalList
                        AppNoticeManager.submitPendingSponsorNotice(context, newPendingNotice)
                        
                        val jsonArray = org.json.JSONArray()
                        updatedLocalList.forEach { n ->
                            val obj = org.json.JSONObject()
                            obj.put("id", n.id)
                            obj.put("title", n.title)
                            obj.put("content", n.content)
                            obj.put("videoUrl", n.videoUrl)
                            obj.put("expandedImageUrl", n.expandedImageUrl)
                            obj.put("externalUrl", n.externalUrl)
                            obj.put("tag", n.tag)
                            obj.put("titleColor", n.titleColor)
                            obj.put("budget", n.budget)
                            obj.put("budgetUnit", n.budgetUnit)
                            obj.put("durationValue", n.durationValue)
                            obj.put("durationUnit", n.durationUnit)
                            obj.put("approvedAtMillis", n.approvedAtMillis)
                            obj.put("expiresAtMillis", n.expiresAtMillis)
                            obj.put("isApproved", n.isApproved)
                            obj.put("isEnabled", n.isEnabled)
                            obj.put("sponsorEmail", n.sponsorEmail)
                            jsonArray.put(obj)
                        }
                        prefs.edit().putString("pending_ads", jsonArray.toString()).apply()
                        coroutineScope.launch {
                            com.example.util.SubscriptionManager.addBlueEssence(-requiredEssences)
                        }

                        Toast.makeText(context, "Anuncio enviado a revisión. Se descontaron $requiredEssences EA.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Aceptar y Enviar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmReviewDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    if (noticeToDelete != null) {
        val targetNotice = noticeToDelete!!
        AlertDialog(
            onDismissRequest = { noticeToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar Anuncio", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Text(
                    "¿Estás seguro de que deseas eliminar permanentemente el anuncio \"${targetNotice.title}\"? Esta acción no se puede deshacer.",
                    color = Color.White,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val noticeId = targetNotice.id
                        noticeToDelete = null

                        val updatedLocal = localPendingAds.filter { it.id != noticeId }
                        localPendingAds = updatedLocal

                        val jsonArray = org.json.JSONArray()
                        updatedLocal.forEach { n ->
                            val obj = org.json.JSONObject()
                            obj.put("id", n.id)
                            obj.put("title", n.title)
                            obj.put("content", n.content)
                            obj.put("videoUrl", n.videoUrl)
                            obj.put("expandedImageUrl", n.expandedImageUrl)
                            obj.put("externalUrl", n.externalUrl)
                            obj.put("tag", n.tag)
                            obj.put("titleColor", n.titleColor)
                            obj.put("budget", n.budget)
                            obj.put("budgetUnit", n.budgetUnit)
                            obj.put("durationValue", n.durationValue)
                            obj.put("durationUnit", n.durationUnit)
                            obj.put("approvedAtMillis", n.approvedAtMillis)
                            obj.put("expiresAtMillis", n.expiresAtMillis)
                            obj.put("isApproved", n.isApproved)
                            obj.put("isEnabled", n.isEnabled)
                            obj.put("sponsorEmail", n.sponsorEmail)
                            jsonArray.put(obj)
                        }
                        prefs.edit().putString("pending_ads", jsonArray.toString()).apply()

                        val updated = allNotices.filter { it.id != noticeId }
                        AppNoticeManager.saveNotices(context, updated)
                        Toast.makeText(context, "Anuncio eliminado permanentemente", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { noticeToDelete = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = HextechSurfaceVariant
        )
    }
}

@Composable
fun SponsorNoticeCard(
    notice: AppNotice,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val now = System.currentTimeMillis()
    val isExpired = notice.expiresAtMillis > 0L && now >= notice.expiresAtMillis

    val statusText = when {
        isExpired -> "Finalizado"
        notice.isApproved -> "Aprobado y Activo"
        else -> "Pendiente de Aprobación"
    }
    val statusColor = when {
        isExpired -> Color(0xFFEF4444)
        notice.isApproved -> Color(0xFF10B981)
        else -> Color(0xFFF59E0B)
    }

    val metricsMap by com.example.data.AppNoticeAnalyticsManager.metricsMap.collectAsState()
    val metrics = metricsMap[notice.id] ?: com.example.data.NoticeMetrics(notice.id)
    val ctr = if (metrics.impressions > 0) (metrics.clicks.toDouble() / metrics.impressions) * 100 else 0.0

    val unitLabel = when (notice.durationUnit.lowercase(Locale.ROOT)) {
        "hour", "hours", "hora", "horas" -> "Horas"
        "day", "days", "dia", "dias", "día", "días" -> "Días"
        "week", "weeks", "semana", "semanas" -> "Semanas"
        "month", "months", "mes", "meses" -> "Meses"
        else -> notice.durationUnit
    }

    // Cálculo de eliminación automática en 7 días para anuncios finalizados
    val deletionNoticeStr = if (isExpired && notice.expiresAtMillis > 0L) {
        val remainingDeletionMillis = (notice.expiresAtMillis + 7 * 24 * 60 * 60 * 1000L) - now
        if (remainingDeletionMillis > 0) {
            val totalHours = remainingDeletionMillis / (1000 * 60 * 60)
            val days = totalHours / 24
            val hours = totalHours % 24
            val mins = (remainingDeletionMillis / (1000 * 60)) % 60
            if (days > 0) "Se eliminará del historial en ${days}d ${hours}h"
            else "Se eliminará del historial en ${hours}h ${mins}m"
        } else {
            "Programado para eliminación"
        }
    } else if (notice.expiresAtMillis > 0L) {
        val diff = notice.expiresAtMillis - now
        val hours = diff / (1000 * 60 * 60)
        val days = hours / 24
        if (days > 0) "Expira en: ${days}d ${hours % 24}h"
        else "Expira en: ${hours}h ${(diff / (1000 * 60)) % 60}m"
    } else null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(notice.title, color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (notice.content.isNotBlank()) {
                Text(notice.content, color = TextSecondary, fontSize = 12.sp, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }

            // Statistics Row
            if (notice.isApproved) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(HextechDarkBg, RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Vistas", color = TextSecondary, fontSize = 10.sp)
                        Text("${metrics.impressions}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Clics", color = TextSecondary, fontSize = 10.sp)
                        Text("${metrics.clicks}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CTR", color = TextSecondary, fontSize = 10.sp)
                        Text(String.format(Locale.US, "%.1f%%", ctr), color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Presupuesto: $${String.format(Locale.US, "%.2f", notice.budget)} USD", color = HextechCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("Duración: ${notice.durationValue} $unitLabel", color = TextSecondary, fontSize = 10.sp)
                    if (deletionNoticeStr != null) {
                        Text(deletionNoticeStr, color = if (isExpired) DangerRed else HextechGold, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Botón para copiar estadísticas
                    IconButton(
                        onClick = {
                            val statsText = """
📊 Estadísticas del Anuncio:
• Título: ${notice.title}
• Estado: $statusText
• Vistas (Impresiones): ${metrics.impressions}
• Clics: ${metrics.clicks}
• CTR: ${String.format(Locale.US, "%.1f%%", ctr)}
• Presupuesto: $${String.format(Locale.US, "%.2f", notice.budget)} USD
• Duración: ${notice.durationValue} $unitLabel
${if (deletionNoticeStr != null) "• Estado de tiempo: $deletionNoticeStr" else ""}
                            """.trimIndent()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            val clip = ClipData.newPlainText("Estadísticas de Anuncio", statsText)
                            clipboard?.setPrimaryClip(clip)
                            Toast.makeText(context, "Estadísticas copiadas al portapapeles", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar estadísticas", tint = HextechCyan, modifier = Modifier.size(17.dp))
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = DangerRed, modifier = Modifier.size(17.dp))
                    }
                }
            }
        }
    }
}
