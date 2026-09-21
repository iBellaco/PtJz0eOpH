package com.example.ui.components

import com.example.ui.theme.HextechGoldLight
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.WildRiftItemsData
import com.example.data.WildRiftRepository
import com.example.data.WildRiftSpellsAndRunes
import com.example.data.supabase.FeedbackRepository
import com.example.model.Champion
import com.example.model.RuneItem
import com.example.model.SummonerSpellItem
import com.example.model.WildRiftItem
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
import com.example.util.tr
import kotlinx.coroutines.launch

enum class FeedbackType(
    val title: String,
    val icon: ImageVector,
    val label: String
) {
    BUG("Reportar Bug", Icons.Default.BugReport, "Bug / Error"),
    SUGGESTION("Sugerencia", Icons.Default.Lightbulb, "Idea / Sugerencia"),
    BUILD_SUGGESTION("Sugerir Build", Icons.Default.SportsEsports, "Sugerir Build")
}

@Composable
fun BugReportFeedbackDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val userRole by com.example.util.SubscriptionManager.userRole.collectAsState()
    val isAdmin = userRole == "admin"

    var selectedType by remember { mutableStateOf(FeedbackType.BUG) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    
    // Build Suggestion graphical states
    var selectedChampionObj by remember { mutableStateOf<Champion?>(null) }
    var suggestedChampion by remember { mutableStateOf("") }
    var suggestedRole by remember { mutableStateOf("Mid") }
    
    val selectedCoreItems = remember { mutableStateListOf<WildRiftItem>() }
    val selectedBootsItems = remember { mutableStateListOf<WildRiftItem>() }
    var selectedBootTier2 by remember { mutableStateOf<WildRiftItem?>(null) }
    var selectedBootTier3 by remember { mutableStateOf<WildRiftItem?>(null) }
    val selectedSituationalItems = remember { mutableStateListOf<WildRiftItem>() }
    var situationalDescription by remember { mutableStateOf("") }
    var selectedKeystoneRune by remember { mutableStateOf<RuneItem?>(null) }
    val selectedSecondaryRunes = remember { mutableStateListOf<RuneItem>() }
    var selectedOptionalKeystoneRune by remember { mutableStateOf<RuneItem?>(null) }
    val selectedOptionalSecondaryRunes = remember { mutableStateListOf<RuneItem>() }
    var runesOptionalDescription by remember { mutableStateOf("") }
    val selectedSpells = remember { mutableStateListOf<SummonerSpellItem>() }
    val selectedOptionalSpells = remember { mutableStateListOf<SummonerSpellItem>() }
    var spellsOptionalDescription by remember { mutableStateOf("") }
    
    // Dialog pickers
    var showChampionPicker by remember { mutableStateOf(false) }
    var showItemPickerType by remember { mutableStateOf<String?>(null) } // "core", "boots", "situational"
    var showRunePickerType by remember { mutableStateOf<String?>(null) } // "keystone", "secondary", "optional_keystone", "optional_secondary"
    var showSpellPickerType by remember { mutableStateOf<String?>(null) } // "primary", "optional"

    var isSubmitting by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var selectedImages by remember { mutableStateOf<List<String>>(emptyList()) }

    val successMsg = tr("Imagen adjuntada correctamente")
    val errorMsg = tr("Error al procesar la imagen")
    val limitMsg = tr("La imagen excede el límite de 2 MB")
    
    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia(maxItems = 3)
    ) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                val newImages = mutableListOf<String>()
                for (uri in uris) {
                    try {
                        val cursor = context.contentResolver.query(uri, null, null, null, null)
                        var sizeInBytes: Long = 0
                        if (cursor != null && cursor.moveToFirst()) {
                            val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                            if (sizeIndex != -1) {
                                sizeInBytes = cursor.getLong(sizeIndex)
                            }
                            cursor.close()
                        }
                        
                        if (sizeInBytes > 2 * 1024 * 1024) {
                            Toast.makeText(context, limitMsg, Toast.LENGTH_LONG).show()
                            continue
                        }
                        
                        val base64 = com.example.util.ImageUtils.uriToBase64(context, uri)
                        if (base64 != null) {
                            newImages.add(base64)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                if (newImages.isNotEmpty()) {
                    val combined = (selectedImages + newImages).take(3)
                    selectedImages = combined
                    Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val isEmailValid = email.isBlank() || android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isSituationalValid = selectedSituationalItems.isEmpty() || situationalDescription.trim().isNotBlank()
    val hasOptionalRunes = selectedOptionalKeystoneRune != null || selectedOptionalSecondaryRunes.isNotEmpty()
    val isOptionalRunesValid = !hasOptionalRunes || runesOptionalDescription.trim().isNotBlank()
    val isOptionalSpellsValid = selectedOptionalSpells.isEmpty() || spellsOptionalDescription.trim().isNotBlank()

    val isBuildSuggestionComplete = selectedChampionObj != null &&
        suggestedRole.isNotBlank() &&
        selectedCoreItems.size == 5 &&
        selectedBootsItems.isNotEmpty() &&
        isSituationalValid &&
        selectedKeystoneRune != null &&
        selectedSecondaryRunes.size == 4 &&
        isOptionalRunesValid &&
        selectedSpells.size == 2 &&
        isOptionalSpellsValid &&
        title.trim().isNotBlank() &&
        description.trim().isNotBlank() &&
        isEmailValid

    val canPublish = if (selectedType == FeedbackType.BUILD_SUGGESTION) {
        isBuildSuggestionComplete
    } else {
        title.trim().isNotBlank() && description.trim().isNotBlank() && isEmailValid
    }

    val sendFeedbackMessage: () -> Unit = {
        if (canPublish) {
            isSubmitting = true
            statusMessage = null
            scope.launch {
                val effectiveChampName = selectedChampionObj?.name ?: suggestedChampion.ifBlank { "Campeón General" }
                var finalTitle = title.ifBlank { "Sugerencia de Build para $effectiveChampName ($suggestedRole)" }
                var finalDesc = description
                if (selectedType == FeedbackType.BUILD_SUGGESTION) {
                    val coreItemsStr = selectedCoreItems.joinToString(" • ") { it.name }
                    val bootsStr = selectedBootsItems.joinToString(" • ") { it.name }
                    val situItemsStr = selectedSituationalItems.joinToString(" • ") { it.name }
                    val currentKeystone = selectedKeystoneRune
                    val runesStr = buildString {
                        if (currentKeystone != null) {
                            append("Clave: ${currentKeystone.name}")
                        }
                        if (selectedSecondaryRunes.isNotEmpty()) {
                            if (isNotEmpty()) append(" | Secundarias: ")
                            append(selectedSecondaryRunes.joinToString(", ") { it.name })
                        }
                    }
                    val currentOptKeystone = selectedOptionalKeystoneRune
                    val optionalRunesStr = buildString {
                        if (currentOptKeystone != null) {
                            append("Clave Opcional: ${currentOptKeystone.name}")
                        }
                        if (selectedOptionalSecondaryRunes.isNotEmpty()) {
                            if (isNotEmpty()) append(" | Secundarias Opcionales: ")
                            append(selectedOptionalSecondaryRunes.joinToString(", ") { it.name })
                        }
                    }
                    val spellsStr = selectedSpells.joinToString(" + ") { it.name }
                    val optionalSpellsStr = selectedOptionalSpells.joinToString(" + ") { it.name }

                    val buildDetails = buildString {
                        appendLine("--- SUGERENCIA DE BUILD DE COMUNIDAD (CATÁLOGO) ---")
                        appendLine("• 1. Campeón: $effectiveChampName")
                        appendLine("• 2. Rol/Línea: $suggestedRole")
                        appendLine("• Título: ${title.trim()}")
                        if (coreItemsStr.isNotBlank()) appendLine("• 3. Objetos Core (1 al 5): $coreItemsStr")
                        if (bootsStr.isNotBlank()) appendLine("• 4. Botas & Encantamientos: $bootsStr")
                        if (situItemsStr.isNotBlank()) {
                            appendLine("• 5. Situacional: $situItemsStr")
                            if (situationalDescription.isNotBlank()) {
                                appendLine("  - Justificación Situacional (Cuándo / Por qué): ${situationalDescription.trim()}")
                            }
                        }
                        if (runesStr.isNotBlank()) appendLine("• 6. Runas (1 Clave + 4 Secundarias): $runesStr")
                        if (optionalRunesStr.isNotBlank()) {
                            appendLine("  - Runas Opcionales / Situacionales: $optionalRunesStr")
                            if (runesOptionalDescription.isNotBlank()) {
                                appendLine("  - Justificación Runas Opcionales: ${runesOptionalDescription.trim()}")
                            }
                        }
                        if (spellsStr.isNotBlank()) appendLine("• 7. Hechizos de Invocador: $spellsStr")
                        if (optionalSpellsStr.isNotBlank()) {
                            appendLine("  - Hechizos Opcionales: $optionalSpellsStr")
                            if (spellsOptionalDescription.isNotBlank()) {
                                appendLine("  - Justificación Hechizos Opcionales: ${spellsOptionalDescription.trim()}")
                            }
                        }
                        appendLine("\n• Justificación Táctica / Matchups:")
                        appendLine(description.trim())
                    }
                    finalDesc = buildDetails
                } else {
                    if (suggestedChampion.isNotBlank()) finalDesc += "\n\nCampeón Sugerido: $suggestedChampion"
                    if (suggestedRole.isNotBlank()) finalDesc += "\nRol Sugerido: $suggestedRole"
                }
                
                val result = FeedbackRepository.submitFeedback(
                    type = selectedType.name,
                    title = finalTitle,
                    description = finalDesc,
                    email = email.trim().takeIf { it.isNotEmpty() },
                    imagesBase64 = if (selectedType == FeedbackType.BUILD_SUGGESTION) emptyList() else selectedImages,
                    retentionDays = 7
                )
                isSubmitting = false
                if (result.isSuccess) {
                    Toast.makeText(context, "✅ ¡Sugerencia de Build enviada con éxito!", Toast.LENGTH_LONG).show()
                    onDismiss()
                } else {
                    val err = result.exceptionOrNull()?.message ?: "Error desconocido"
                    statusMessage = "❌ Error al enviar: $err"
                    Toast.makeText(context, "Error: $err", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            val msg = if (selectedType == FeedbackType.BUILD_SUGGESTION) {
                when {
                    selectedChampionObj == null && suggestedChampion.isBlank() -> "1. Por favor selecciona el campeón de la build (* Obligatorio)"
                    suggestedRole.isBlank() -> "2. Por favor selecciona el rol o línea (* Obligatorio)"
                    selectedCoreItems.size < 5 -> "3. Debes seleccionar los 5 objetos Core de la build (* Obligatorio)"
                    selectedBootsItems.isEmpty() -> "4. Debes seleccionar al menos una opción de Botas (* Obligatorio)"
                    selectedSituationalItems.isNotEmpty() && situationalDescription.trim().isBlank() -> "5. Escribe la justificación de cuándo armar los objetos situacionales (* Obligatorio)"
                    selectedKeystoneRune == null || selectedSecondaryRunes.size < 4 -> "6. Debes completar las 5 runas principales (1 clave + 4 secundarias) (* Obligatorio)"
                    hasOptionalRunes && runesOptionalDescription.trim().isBlank() -> "6. Escribe la justificación de cuándo usar las runas opcionales (* Obligatorio)"
                    selectedSpells.size < 2 -> "7. Debes seleccionar 2 hechizos de invocador principales (* Obligatorio)"
                    selectedOptionalSpells.isNotEmpty() && spellsOptionalDescription.trim().isBlank() -> "7. Escribe la justificación de cuándo usar los hechizos opcionales (* Obligatorio)"
                    title.trim().isBlank() -> "Por favor ingresa un título o resumen para la build (* Obligatorio)"
                    description.trim().isBlank() -> "La justificación táctica / matchups es obligatoria (* Obligatorio)"
                    !isEmailValid -> "El formato del correo electrónico no es válido"
                    else -> "Por favor completa todos los campos requeridos de la build"
                }
            } else {
                "Por favor completa el título y la descripción"
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("bug_report_dialog"),
        containerColor = HextechDarkBg,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = HextechGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tr("Buzón de Reportes & Ideas"),
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = tr("Cerrar"), tint = TextMuted)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val availableFeedbackTypes = remember {
                    FeedbackType.entries
                }

                // Selector de Tipo de Reporte
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(HextechSurfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    availableFeedbackTypes.forEach { type ->
                        val isSelected = selectedType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) HextechGold.copy(alpha = 0.2f)
                                    else Color.Transparent
                                )
                                .border(
                                    width = if (isSelected) 1.dp else 0.dp,
                                    color = if (isSelected) HextechGold else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { selectedType = type }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = type.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) HextechGold else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tr(type.label),
                                    color = if (isSelected) HextechGold else TextMuted,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // CAMPOS ESPECÍFICOS PARA SUGERIR BUILD (CATÁLOGO GRÁFICO)
                // ==========================================
                if (selectedType == FeedbackType.BUILD_SUGGESTION) {
                    // 1. SELECTOR GRÁFICO DE CAMPEÓN
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = tr("1. Selecciona Campeón:"),
                                    color = HextechGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val champStatusText = if (selectedChampionObj != null) "✓ " + tr("Seleccionado") else "* " + tr("Obligatorio")
                                val champStatusColor = if (selectedChampionObj != null) HextechGold else DangerRed
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(champStatusColor.copy(alpha = 0.2f))
                                        .border(0.8.dp, champStatusColor, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = champStatusText,
                                        color = champStatusColor,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        val currentChamp = selectedChampionObj
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(HextechSurfaceVariant)
                                .border(1.dp, if (currentChamp != null) HextechGold else DangerRed.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                                .clickable { showChampionPicker = true }
                                .padding(10.dp)
                        ) {
                            if (currentChamp != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        ChampionAvatar(
                                            champion = currentChamp,
                                            size = 40.dp,
                                            showTierBadge = false
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = currentChamp.name,
                                                color = HextechGold,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${currentChamp.primaryRole.shortName} • Tier ${currentChamp.tier}",
                                                color = TextMuted,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Text(
                                        text = tr("Cambiar"),
                                        color = HextechCyan,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(HextechSurface)
                                                .border(1.dp, HextechGold.copy(alpha = 0.5f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SportsEsports,
                                                contentDescription = null,
                                                tint = HextechGold,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = tr("Toca para elegir del catálogo... (* Obligatorio)"),
                                            color = TextMuted,
                                            fontSize = 12.5.sp
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = HextechCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 2. SELECTOR DE ROL / LÍNEA
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = tr("2. Rol / Línea:"),
                                    color = HextechGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val roleStatusText = if (suggestedRole.isNotBlank()) "✓ " + tr("Seleccionado") else "* " + tr("Obligatorio")
                                val roleStatusColor = if (suggestedRole.isNotBlank()) HextechGold else DangerRed
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(roleStatusColor.copy(alpha = 0.2f))
                                        .border(0.8.dp, roleStatusColor, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = roleStatusText,
                                        color = roleStatusColor,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val roles = listOf("Solo / Baron", "Jungla", "Mid", "Dúo / ADC", "Soporte")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            roles.forEach { r ->
                                val isRSelected = suggestedRole == r
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isRSelected) HextechCyan.copy(alpha = 0.25f) else HextechSurface)
                                        .border(1.dp, if (isRSelected) HextechCyan else HextechCardBorder, RoundedCornerShape(6.dp))
                                        .clickable { suggestedRole = r }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = r.split("/").last().trim(),
                                        color = if (isRSelected) HextechCyan else TextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = if (isRSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    // 3. OBJETOS CORE (1 al 5) - SELECCIÓN GRÁFICA (Máximo 5 objetos)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = tr("3. Objetos Core (1 al 5):"),
                                    color = HextechGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val coreStatusText = if (selectedCoreItems.size == 5) "✓ " + tr("Completo (5/5)") else "* " + tr("Obligatorio (${selectedCoreItems.size}/5)")
                                val coreStatusColor = if (selectedCoreItems.size == 5) HextechGold else DangerRed
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(coreStatusColor.copy(alpha = 0.2f))
                                        .border(0.8.dp, coreStatusColor, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = coreStatusText,
                                        color = coreStatusColor,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (selectedCoreItems.size < 5) {
                                Text(
                                    text = "+ " + tr("Añadir"),
                                    color = HextechCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { showItemPickerType = "core" }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(selectedCoreItems) { item ->
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(HextechSurfaceVariant)
                                        .border(1.5.dp, HextechGold, RoundedCornerShape(8.dp))
                                        .clickable { selectedCoreItems.remove(item) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(item.iconUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)
                                            
                                            .build(),
                                        contentDescription = item.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(16.dp)
                                            .background(Color.Black.copy(alpha = 0.7f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Quitar",
                                            tint = Color.Red,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }
                            if (selectedCoreItems.size < 5) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(HextechSurface.copy(alpha = 0.6f))
                                            .border(1.dp, HextechCardBorder, RoundedCornerShape(8.dp))
                                            .clickable { showItemPickerType = "core" },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Añadir Core",
                                            tint = HextechCyan,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. BOTAS (NIVEL 2 + EVOLUCIÓN NIVEL 3) - OBLIGATORIO
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(HextechSurfaceVariant.copy(alpha = 0.55f))
                            .border(
                                width = if (selectedBootTier2 != null || selectedBootsItems.isNotEmpty()) 1.5.dp else 1.dp,
                                color = if (selectedBootTier2 != null || selectedBootsItems.isNotEmpty()) HextechGold else DangerRed.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = tr("4. Botas"),
                                    color = HextechGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (selectedBootTier2 != null || selectedBootsItems.isNotEmpty()) HextechGold.copy(alpha = 0.2f) else DangerRed.copy(alpha = 0.2f))
                                        .border(0.8.dp, if (selectedBootTier2 != null || selectedBootsItems.isNotEmpty()) HextechGold else DangerRed, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = if (selectedBootTier2 != null || selectedBootsItems.isNotEmpty()) "✓ " + tr("Configuradas") else "* " + tr("Obligatorio (Nivel 2)"),
                                        color = if (selectedBootTier2 != null || selectedBootsItems.isNotEmpty()) HextechGold else DangerRed,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        val effectiveT2 = selectedBootTier2 ?: selectedBootsItems.firstOrNull()
                        val effectiveT3 = selectedBootTier3 ?: if (selectedBootsItems.size > 1) selectedBootsItems.getOrNull(1) else null

                        if (effectiveT2 == null) {
                            Button(
                                onClick = { showItemPickerType = "boots_t2" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HextechCyan.copy(alpha = 0.2f)
                                ),
                                border = BorderStroke(1.dp, HextechGold),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = HextechGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tr("Seleccionar Botas Nivel 2 y su Evolución Nivel 3"),
                                    fontSize = 11.5.sp,
                                    color = HextechGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Bota Nivel 2
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(HextechSurface.copy(alpha = 0.7f))
                                        .border(1.dp, HextechGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .clickable { showItemPickerType = "boots_t2" }
                                        .padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(HextechSurfaceVariant)
                                            .border(1.5.dp, HextechGold, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(effectiveT2.iconUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)
                                                
                                                .build(),
                                            contentDescription = effectiveT2.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "★ " + tr("Bota Nivel 2: ") + effectiveT2.name,
                                            color = TextPrimary,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${effectiveT2.category} • ${effectiveT2.goldCost} 💰 (Toca para cambiar)",
                                            color = HextechCyan,
                                            fontSize = 9.5.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            selectedBootTier2 = null
                                            selectedBootTier3 = null
                                            selectedBootsItems.clear()
                                        },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Quitar", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }

                                // Indicador de Evolución hacia Nivel 3
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "↓ " + tr("Evolución a Nivel 3 (Encantamiento / Bota T3)") + " ↓",
                                        color = HextechGold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Bota Nivel 3 / Evolución
                                if (effectiveT3 != null) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(HextechSurface.copy(alpha = 0.7f))
                                            .border(1.dp, HextechCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                            .clickable { showItemPickerType = "boots_t3" }
                                            .padding(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(HextechSurfaceVariant)
                                                .border(1.5.dp, HextechCyan, RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(effectiveT3.iconUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)
                                                    
                                                    .build(),
                                                contentDescription = effectiveT3.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "⚡ " + tr("Evolución Nivel 3: ") + effectiveT3.name,
                                                color = TextPrimary,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${effectiveT3.category} • ${effectiveT3.goldCost} 💰 (Toca para cambiar)",
                                                color = HextechCyan,
                                                fontSize = 9.5.sp
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                selectedBootTier3 = null
                                                selectedBootsItems.clear()
                                                if (selectedBootTier2 != null) selectedBootsItems.add(selectedBootTier2!!)
                                            },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Quitar evolución", tint = Color.Red, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = { showItemPickerType = "boots_t3" },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = HextechSurface
                                        ),
                                        border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.6f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = tr("+ Seleccionar Evolución (Botas Nivel 3)"),
                                            fontSize = 11.sp,
                                            color = HextechCyan,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 5. OBJETOS SITUACIONALES (OPCIONAL - DESCRIPCIÓN OBLIGATORIA SOLO SI SE ELIGEN OBJETOS)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(HextechSurfaceVariant.copy(alpha = 0.55f))
                            .border(
                                width = if (isSituationalValid && selectedSituationalItems.isNotEmpty()) 1.5.dp else 1.dp,
                                color = if (isSituationalValid && selectedSituationalItems.isNotEmpty()) HextechGold else if (selectedSituationalItems.isNotEmpty() && situationalDescription.trim().isBlank()) DangerRed else HextechCyan.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = tr("5. Objetos Situacionales"),
                                    color = HextechGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val sitStatusText = when {
                                    selectedSituationalItems.isEmpty() -> tr("(Opcional)")
                                    situationalDescription.trim().isNotBlank() -> "✓ " + tr("Completo")
                                    else -> "* " + tr("Justificación requerida")
                                }
                                val sitStatusColor = when {
                                    selectedSituationalItems.isEmpty() -> TextMuted
                                    situationalDescription.trim().isNotBlank() -> HextechGold
                                    else -> DangerRed
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(sitStatusColor.copy(alpha = 0.2f))
                                        .border(0.8.dp, sitStatusColor, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                    Text(
                                        text = sitStatusText,
                                        color = sitStatusColor,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Lista única de Objetos Situacionales
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = tr("Selecciona objetos situacionales (Opcional):"),
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(selectedSituationalItems) { item ->
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(HextechSurfaceVariant)
                                            .border(1.5.dp, HextechCyan, RoundedCornerShape(8.dp))
                                            .clickable { selectedSituationalItems.remove(item) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(item.iconUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)
                                                
                                                .build(),
                                            contentDescription = item.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(14.dp)
                                                .background(Color.Black.copy(alpha = 0.7f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Quitar",
                                                tint = Color.Red,
                                                modifier = Modifier.size(9.dp)
                                            )
                                        }
                                    }
                                }
                                if (selectedSituationalItems.size < 6) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(HextechSurface)
                                                .border(1.dp, HextechCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                                .clickable { showItemPickerType = "situational" },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Justificación de los situacionales
                        OutlinedTextField(
                            value = situationalDescription,
                            onValueChange = { situationalDescription = it },
                            label = {
                                Text(
                                    if (selectedSituationalItems.isNotEmpty()) tr("¿Por qué o en qué situaciones armarlos? (* Obligatorio)") else tr("¿Por qué o en qué situaciones armarlos? (Opcional)"),
                                    fontSize = 11.sp
                                )
                            },
                            placeholder = { Text(tr("Ej: Armar Cortacuras si hay Aatrox/Soraka, comprar Penetración si arman armadura..."), fontSize = 10.5.sp, color = TextMuted) },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HextechCyan,
                                unfocusedBorderColor = if (selectedSituationalItems.isNotEmpty() && situationalDescription.trim().isBlank()) DangerRed.copy(alpha = 0.8f) else HextechCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    // 6. RUNAS (CLAVE + SECUNDARIAS + OPCIONALES)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(HextechSurfaceVariant.copy(alpha = 0.55f))
                            .border(
                                width = if (selectedKeystoneRune != null && selectedSecondaryRunes.size == 4 && isOptionalRunesValid) 1.5.dp else 1.dp,
                                color = if (selectedKeystoneRune != null && selectedSecondaryRunes.size == 4 && isOptionalRunesValid) HextechGold else DangerRed.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = tr("6. Runas"),
                                    color = HextechGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val runeStatusText = if (selectedKeystoneRune != null && selectedSecondaryRunes.size == 4 && isOptionalRunesValid) "✓ " + tr("Completo") else "* " + tr("Obligatorio")
                                val runeStatusColor = if (selectedKeystoneRune != null && selectedSecondaryRunes.size == 4 && isOptionalRunesValid) HextechGold else DangerRed
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(runeStatusColor.copy(alpha = 0.2f))
                                        .border(0.8.dp, runeStatusColor, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = runeStatusText,
                                        color = runeStatusColor,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Runas Principales (1 Clave + 4 Secundarias)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = tr("Runas Principales (* 1 Clave + 4 Secundarias):"),
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            val currentKeystone = selectedKeystoneRune
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Keystone
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(HextechSurfaceVariant)
                                        .border(2.dp, if (currentKeystone != null) HextechGold else DangerRed.copy(alpha = 0.7f), CircleShape)
                                        .clickable { showRunePickerType = "keystone" },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (currentKeystone != null) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(currentKeystone.iconUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)
                                                
                                                .build(),
                                            contentDescription = currentKeystone.name,
                                            modifier = Modifier.size(38.dp)
                                        )
                                    } else {
                                        Icon(Icons.Default.Star, contentDescription = "Keystone", tint = HextechGold, modifier = Modifier.size(20.dp))
                                    }
                                }

                                // 4 Secundarias
                                for (i in 0 until 4) {
                                    val rune = selectedSecondaryRunes.getOrNull(i)
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(HextechSurface)
                                            .border(1.dp, if (rune != null) HextechCyan else HextechCardBorder, CircleShape)
                                            .clickable {
                                                if (rune != null) {
                                                    selectedSecondaryRunes.remove(rune)
                                                } else {
                                                    showRunePickerType = "secondary"
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (rune != null) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(rune.iconUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)
                                                    
                                                    .build(),
                                                contentDescription = rune.name,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        } else {
                                            Icon(Icons.Default.Add, contentDescription = "Añadir", tint = HextechCyan.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Runas Opcionales / Situacionales
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = tr("Runas Opcionales / Situacionales (Opcional - 1 Clave + hasta 4 Secundarias):"),
                                color = HextechCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            val currentOptKeystone = selectedOptionalKeystoneRune
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Keystone Opcional
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(HextechSurfaceVariant)
                                        .border(2.dp, if (currentOptKeystone != null) HextechGoldLight else HextechCardBorder, CircleShape)
                                        .clickable {
                                            if (currentOptKeystone != null) {
                                                selectedOptionalKeystoneRune = null
                                            } else {
                                                showRunePickerType = "optional_keystone"
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (currentOptKeystone != null) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(currentOptKeystone.iconUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)
                                                
                                                .build(),
                                            contentDescription = currentOptKeystone.name,
                                            modifier = Modifier.size(38.dp)
                                        )
                                    } else {
                                        Icon(Icons.Default.Star, contentDescription = "Keystone Opcional", tint = HextechGoldLight.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                                    }
                                }

                                // Hasta 4 Secundarias Opcionales
                                for (i in 0 until 4) {
                                    val rune = selectedOptionalSecondaryRunes.getOrNull(i)
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(HextechSurface)
                                            .border(1.dp, if (rune != null) HextechCyan else HextechCardBorder, CircleShape)
                                        .clickable {
                                            if (rune != null) {
                                                selectedOptionalSecondaryRunes.remove(rune)
                                            } else {
                                                showRunePickerType = "optional_secondary"
                                            }
                                        },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (rune != null) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(rune.iconUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)
                                                    
                                                    .build(),
                                                contentDescription = rune.name,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        } else {
                                            Icon(Icons.Default.Add, contentDescription = "Añadir secundaria opcional", tint = HextechGoldLight.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Justificación de runas opcionales
                        OutlinedTextField(
                            value = runesOptionalDescription,
                            onValueChange = { runesOptionalDescription = it },
                            label = {
                                Text(
                                    if (hasOptionalRunes) tr("¿Por qué y en qué situaciones usar estas runas? (* Obligatorio)") else tr("¿Por qué y en qué situaciones usar estas runas? (Opcional)"),
                                    fontSize = 11.sp
                                )
                            },
                            placeholder = { Text(tr("Ej: Usar Conquistador si hay composiciones tanque, o Cazador Titán contra mucho CC..."), fontSize = 10.5.sp, color = TextMuted) },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HextechCyan,
                                unfocusedBorderColor = if (hasOptionalRunes && runesOptionalDescription.trim().isBlank()) DangerRed.copy(alpha = 0.8f) else HextechCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    // 7. HECHIZOS DE INVOCADOR (PRINCIPALES + OPCIONALES)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(HextechSurfaceVariant.copy(alpha = 0.55f))
                            .border(
                                width = if (selectedSpells.size == 2 && isOptionalSpellsValid) 1.5.dp else 1.dp,
                                color = if (selectedSpells.size == 2 && isOptionalSpellsValid) HextechGold else DangerRed.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = tr("7. Hechizos de Invocador"),
                                    color = HextechGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val spellStatusText = if (selectedSpells.size == 2 && isOptionalSpellsValid) "✓ " + tr("Completo (2/2)") else "* " + tr("Obligatorio")
                                val spellStatusColor = if (selectedSpells.size == 2 && isOptionalSpellsValid) HextechGold else DangerRed
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(spellStatusColor.copy(alpha = 0.2f))
                                        .border(0.8.dp, spellStatusColor, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = spellStatusText,
                                        color = spellStatusColor,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Hechizos Principales (2 obligatorios)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = tr("Hechizos Principales (* 2 Hechizos):"),
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                for (i in 0 until 2) {
                                    val spell = selectedSpells.getOrNull(i)
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(HextechSurfaceVariant)
                                            .border(1.5.dp, if (spell != null) HextechCyan else DangerRed.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            if (spell != null) {
                                                selectedSpells.remove(spell)
                                            } else {
                                                showSpellPickerType = "primary"
                                            }
                                        },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (spell != null) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(spell.iconUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)
                                                    
                                                    .build(),
                                                contentDescription = spell.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(Icons.Default.FlashOn, contentDescription = "Hechizo", tint = HextechCyan, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                                if (selectedSpells.isNotEmpty()) {
                                    Text(
                                        text = selectedSpells.joinToString(" + ") { it.name },
                                        color = TextPrimary,
                                        fontSize = 11.5.sp,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                        }

                        // Hechizos Opcionales / Situacionales (Hasta 2)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = tr("Hechizos Opcionales / Situacionales (Opcional - Máximo 2):"),
                                color = HextechCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                for (i in 0 until 2) {
                                    val spell = selectedOptionalSpells.getOrNull(i)
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(HextechSurfaceVariant)
                                            .border(1.5.dp, if (spell != null) HextechGoldLight else HextechCardBorder, RoundedCornerShape(8.dp))
                                            .clickable {
                                                if (spell != null) {
                                                    selectedOptionalSpells.remove(spell)
                                                } else {
                                                    showSpellPickerType = "optional"
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (spell != null) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(spell.iconUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)
                                                    
                                                    .build(),
                                                contentDescription = spell.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(Icons.Default.Add, contentDescription = "Añadir Hechizo Opcional", tint = HextechGoldLight, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                                if (selectedOptionalSpells.isNotEmpty()) {
                                    Text(
                                        text = selectedOptionalSpells.joinToString(" + ") { it.name },
                                        color = TextPrimary,
                                        fontSize = 11.5.sp,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                        }

                        // Justificación de hechizos opcionales
                        OutlinedTextField(
                            value = spellsOptionalDescription,
                            onValueChange = { spellsOptionalDescription = it },
                            label = {
                                Text(
                                    if (selectedOptionalSpells.isNotEmpty()) tr("¿Por qué y en qué situaciones usar estos hechizos? (* Obligatorio)") else tr("¿Por qué y en qué situaciones usar estos hechizos? (Opcional)"),
                                    fontSize = 11.sp
                                )
                            },
                            placeholder = { Text(tr("Ej: Llevar Extenuación si hay asesinos explosivos como Zed/Kha'Zix, o Fantasma para mayor persecución..."), fontSize = 10.5.sp, color = TextMuted) },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HextechCyan,
                                unfocusedBorderColor = if (selectedOptionalSpells.isNotEmpty() && spellsOptionalDescription.trim().isBlank()) DangerRed.copy(alpha = 0.8f) else HextechCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                }

                // Campo Título
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (selectedType == FeedbackType.BUILD_SUGGESTION) tr("Título o resumen de la build") else tr("Título del reporte o sugerencia"), fontSize = 12.sp) },
                    placeholder = {
                        Text(
                            when (selectedType) {
                                FeedbackType.BUG -> tr("Ej: El overlay no detecta la pantalla de selección")
                                FeedbackType.SUGGESTION -> tr("Ej: Agregar temporizador de dragones con audio")
                                FeedbackType.BUILD_SUGGESTION -> tr("Ej: Build de Burst Letal para Midlane")
                            },
                            fontSize = 11.5.sp,
                            color = TextMuted
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("feedback_title_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HextechGold,
                        unfocusedBorderColor = HextechCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = HextechGold,
                        unfocusedLabelColor = TextMuted
                    )
                )

                // Campo Descripción / Justificación Táctica
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (selectedType == FeedbackType.BUILD_SUGGESTION) tr("Justificación táctica / Matchups (* Obligatorio)") else tr("Descripción detallada (* Obligatorio)"), fontSize = 12.sp) },
                    placeholder = {
                        Text(
                            when (selectedType) {
                                FeedbackType.BUG -> tr("Describe qué sucedió o cómo reproducir el error...")
                                FeedbackType.SUGGESTION -> tr("Describe tu idea o mejora para la aplicación...")
                                FeedbackType.BUILD_SUGGESTION -> tr("Explica contra qué composición usar esta build, power spikes y matchups clave (* Requerido)...")
                            },
                            fontSize = 11.5.sp,
                            color = TextMuted
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .testTag("feedback_desc_input"),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HextechGold,
                        unfocusedBorderColor = HextechCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = HextechGold,
                        unfocusedLabelColor = TextMuted
                    )
                )

                // Campo Correo Electrónico (Opcional)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(tr("Correo electrónico (Opcional)"), fontSize = 12.sp) },
                    placeholder = {
                        Text(
                            tr("Para contactarte si necesitamos más detalles..."),
                            fontSize = 11.5.sp,
                            color = TextMuted
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("feedback_email_input"),
                    singleLine = true,
                    isError = email.isNotBlank() && !isEmailValid,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HextechGold,
                        unfocusedBorderColor = HextechCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = HextechGold,
                        unfocusedLabelColor = TextMuted
                    )
                )
                
                if (email.isNotBlank() && !isEmailValid) {
                    Text(
                        text = tr("Formato de correo inválido"),
                        color = Color(0xFFFF5252),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                // Subir Imágenes (Max 3) - DESHABILITADO PARA SUGERENCIAS DE BUILD
                if (selectedType != FeedbackType.BUILD_SUGGESTION) {
                    if (selectedImages.size < 3) {
                        OutlinedButton(
                            onClick = {
                                imagePickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HextechCyan.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = HextechCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tr("Adjuntar Captura") + " (${selectedImages.size}/3)",
                                color = HextechCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    if (selectedImages.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        selectedImages.forEachIndexed { index, base64 ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(HextechSurfaceVariant.copy(alpha = 0.5f))
                                    .border(1.dp, HextechGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                                    .padding(bottom = if (index < selectedImages.size - 1) 8.dp else 0.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = HextechGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = tr("Imagen subida") + " " + (index + 1),
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val newList = selectedImages.toMutableList()
                                        newList.removeAt(index)
                                        selectedImages = newList
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = tr("Eliminar imagen"),
                                        tint = Color(0xFFFF5252)
                                    )
                                }
                            }
                        }
                    }
                }

                if (statusMessage != null) {
                    Text(
                        text = statusMessage!!,
                        color = Color(0xFFFF5252),
                        fontSize = 11.sp
                    )
                }

                // Diagnóstico del sistema
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(HextechSurfaceVariant.copy(alpha = 0.6f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "${tr(" Dispositivo:")} ${Build.MODEL} • Android ${Build.VERSION.RELEASE} • ${tr(WildRiftRepository.CURRENT_PATCH_VERSION)}",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        },
        confirmButton = {
            val buttonText = when (selectedType) {
                FeedbackType.BUG -> tr("Enviar reporte")
                FeedbackType.SUGGESTION -> tr("Enviar sugerencia")
                FeedbackType.BUILD_SUGGESTION -> tr("Enviar sugerencia de build")
            }

            Button(
                onClick = sendFeedbackMessage,
                enabled = canPublish && !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HextechGold,
                    disabledContainerColor = HextechGold.copy(alpha = 0.25f),
                    disabledContentColor = TextMuted
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_feedback_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = HextechDarkBg,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tr("Enviando mensaje..."),
                        color = HextechDarkBg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = if (canPublish) Color.Black else TextMuted,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = buttonText,
                            color = if (canPublish) Color.Black else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        },
        dismissButton = {}
    )

    // =========================================================================
    // MODALES Y DIÁLOGOS DE SELECCIÓN GRÁFICA DE CATÁLOGO
    // =========================================================================
    
    // 1. Selector de Campeón
    if (showChampionPicker) {
        ChampionCatalogSelectionDialog(
            onDismiss = { showChampionPicker = false },
            onSelect = { champion ->
                selectedChampionObj = champion
                suggestedChampion = champion.name
                showChampionPicker = false
            }
        )
    }

    // 2. Selector de Objeto
    if (showItemPickerType != null) {
        val pickerType = showItemPickerType!!
        val excludedIds = when (pickerType) {
            "core" -> selectedCoreItems.map { it.id }.toSet()
            "boots", "boots_t2", "boots_t3" -> emptySet()
            "situational" -> (selectedCoreItems.map { it.id } + selectedSituationalItems.map { it.id }).toSet()
            else -> emptySet()
        }
        ItemCatalogSelectionDialog(
            type = pickerType,
            excludedItemIds = excludedIds,
            onDismiss = { showItemPickerType = null },
            onSelect = { item ->
                when (pickerType) {
                    "core" -> {
                        if (selectedCoreItems.size < 5 && !selectedCoreItems.contains(item)) {
                            selectedCoreItems.add(item)
                        }
                        showItemPickerType = null
                    }
                    "boots_t2" -> {
                        selectedBootTier2 = item
                        selectedBootsItems.clear()
                        selectedBootsItems.add(item)
                        if (selectedBootTier3 != null) selectedBootsItems.add(selectedBootTier3!!)
                        // Inmediatamente abre la selección de evolución de Nivel 3
                        showItemPickerType = "boots_t3"
                    }
                    "boots_t3" -> {
                        selectedBootTier3 = item
                        selectedBootsItems.clear()
                        if (selectedBootTier2 != null) selectedBootsItems.add(selectedBootTier2!!)
                        selectedBootsItems.add(item)
                        showItemPickerType = null
                    }
                    "boots" -> {
                        if (selectedBootsItems.size < 4 && !selectedBootsItems.contains(item)) {
                            selectedBootsItems.add(item)
                        }
                        showItemPickerType = null
                    }
                    "situational" -> {
                        if (selectedSituationalItems.size < 6 && !selectedSituationalItems.contains(item)) {
                            selectedSituationalItems.add(item)
                        }
                        showItemPickerType = null
                    }
                    else -> {
                        showItemPickerType = null
                    }
                }
            }
        )
    }

    // 3. Selector de Runas
    if (showRunePickerType != null) {
        val pickerType = showRunePickerType!!
        val excludedNames = when (pickerType) {
            "keystone" -> emptySet()
            "secondary" -> selectedSecondaryRunes.map { it.name }.toSet()
            "optional_keystone" -> emptySet()
            "optional_secondary" -> selectedOptionalSecondaryRunes.map { it.name }.toSet()
            else -> emptySet()
        }
        RuneCatalogSelectionDialog(
            mode = pickerType,
            excludedRuneNames = excludedNames,
            onDismiss = { showRunePickerType = null },
            onSelect = { rune ->
                when (pickerType) {
                    "keystone" -> {
                        selectedKeystoneRune = rune
                    }
                    "secondary" -> {
                        if (selectedSecondaryRunes.size < 4 && !selectedSecondaryRunes.contains(rune)) {
                            selectedSecondaryRunes.add(rune)
                        }
                    }
                    "optional_keystone" -> {
                        selectedOptionalKeystoneRune = rune
                    }
                    "optional_secondary" -> {
                        if (selectedOptionalSecondaryRunes.size < 4 && !selectedOptionalSecondaryRunes.contains(rune)) {
                            selectedOptionalSecondaryRunes.add(rune)
                        }
                    }
                }
                showRunePickerType = null
            }
        )
    }

    // 4. Selector de Hechizos
    if (showSpellPickerType != null) {
        val pickerType = showSpellPickerType!!
        val excludedNames = when (pickerType) {
            "primary" -> selectedSpells.map { it.name }.toSet()
            "optional" -> selectedOptionalSpells.map { it.name }.toSet() // Los opcionales pueden coincidir con los principales, solo no duplicar entre opcionales
            else -> emptySet()
        }
        SpellCatalogSelectionDialog(
            title = if (pickerType == "primary") tr("Seleccionar Hechizo Principal") else tr("Seleccionar Hechizo Opcional (Máx 2)"),
            excludedSpellNames = excludedNames,
            onDismiss = { showSpellPickerType = null },
            onSelect = { spell ->
                when (pickerType) {
                    "primary" -> {
                        if (selectedSpells.size < 2 && !selectedSpells.contains(spell)) {
                            selectedSpells.add(spell)
                        }
                    }
                    "optional" -> {
                        if (selectedOptionalSpells.size < 2 && !selectedOptionalSpells.contains(spell)) {
                            selectedOptionalSpells.add(spell)
                        }
                    }
                }
                showSpellPickerType = null
            }
        )
    }
}

@Composable
internal fun ChampionCatalogSelectionDialog(
    onDismiss: () -> Unit,
    onSelect: (Champion) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("Todos") }
    val roles = listOf("Todos", "Solo", "Jungla", "Mid", "ADC", "Soporte")

    val champions = remember(searchQuery, selectedRoleFilter) {
        WildRiftRepository.champions.filter { champ ->
            val matchesSearch = champ.name.contains(searchQuery, ignoreCase = true)
            val roleStr = champ.primaryRole.displayName
            val matchesRole = if (selectedRoleFilter == "Todos") true else {
                when (selectedRoleFilter) {
                    "Solo" -> roleStr.contains("Solo", ignoreCase = true) || roleStr.contains("Barón", ignoreCase = true) || roleStr.contains("Top", ignoreCase = true)
                    "Jungla" -> roleStr.contains("Jungla", ignoreCase = true)
                    "Mid" -> roleStr.contains("Mid", ignoreCase = true) || roleStr.contains("Central", ignoreCase = true)
                    "ADC" -> roleStr.contains("Dúo", ignoreCase = true) || roleStr.contains("Dragón", ignoreCase = true) || roleStr.contains("ADC", ignoreCase = true)
                    "Soporte" -> roleStr.contains("Soporte", ignoreCase = true)
                    else -> true
                }
            }
            matchesSearch && matchesRole
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HextechDarkBg,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr("Seleccionar Campeón"),
                    color = HextechGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(tr("Buscar campeón por nombre..."), fontSize = 12.sp, color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HextechGold) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HextechGold,
                        unfocusedBorderColor = HextechCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(roles) { r ->
                        val isSel = selectedRoleFilter == r
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) HextechCyan.copy(alpha = 0.25f) else HextechSurface)
                                .border(1.dp, if (isSel) HextechCyan else HextechCardBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedRoleFilter = r }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tr(r),
                                color = if (isSel) HextechCyan else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
                ) {
                    items(champions) { champ ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(HextechSurfaceVariant.copy(alpha = 0.5f))
                                .border(1.dp, HextechCardBorder, RoundedCornerShape(8.dp))
                                .clickable { onSelect(champ) }
                                .padding(6.dp)
                        ) {
                            ChampionAvatar(champion = champ, size = 44.dp, showTierBadge = false)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = champ.name,
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
internal fun ItemCatalogSelectionDialog(
    type: String,
    excludedItemIds: Set<String> = emptySet(),
    onDismiss: () -> Unit,
    onSelect: (WildRiftItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCat by remember(type) {
        mutableStateOf(
            when (type) {
                "boots_t2" -> "Botas Nivel 2"
                "boots_t3" -> "Evolución Nivel 3"
                "boots" -> "Botas"
                else -> "Todos"
            }
        )
    }
    
    val categories = when (type) {
        "boots_t2" -> listOf("Botas Nivel 2")
        "boots_t3" -> listOf("Evolución Nivel 3")
        "boots" -> listOf("Botas")
        else -> listOf("Todos", "Físico", "Magia", "Defensa", "Apoyo")
    }

    val items = remember(searchQuery, selectedCat, type, excludedItemIds) {
        WildRiftItemsData.list.filter { item ->
            if (excludedItemIds.contains(item.id)) return@filter false
            val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) || item.nameEn.contains(searchQuery, ignoreCase = true)
            val cat = item.category.lowercase()
            val id = item.id.lowercase()
            val name = item.name.lowercase()
            val nameEn = item.nameEn.lowercase()
            val icon = item.iconUrl.lowercase()

            val isBootItem = item.category.equals("Botas Nivel 2", ignoreCase = true) ||
                item.category.equals("Botas Nivel 3", ignoreCase = true) ||
                cat.contains("bota") ||
                cat.contains("boot") ||
                id.contains("boot") ||
                id.contains("greave") ||
                id.contains("tread") ||
                id.contains("tred") ||
                icon.contains("boot") ||
                icon.contains("greaves") ||
                icon.contains("treads") ||
                name.startsWith("botas") ||
                name.startsWith("grebas") ||
                name.contains("botas ") ||
                name.contains("grebas ") ||
                nameEn.contains("boots") ||
                nameEn.contains("greaves") ||
                nameEn.contains("treads") ||
                listOf(
                    "gluttonous_greaves", "berserker_s_greaves", "mercury_s_treads", "plated_steelcaps",
                    "ionian_boots_of_lucidity", "boots_of_mana", "boots_of_dynamism", "boots_of_swiftness", "boots_of_speed"
                ).contains(id)

            val isBootT3 = item.category.equals("Botas Nivel 3", ignoreCase = true) ||
                cat.contains("nivel 3") ||
                listOf("immortal_treds", "gunmetal_greaves", "chainlaced_crushers", "armored_advance", "crimson_lucidity", "spellslinger_s_shoes", "armorcrusher_boots").contains(id)
            
            val isBootT2 = (item.category.equals("Botas Nivel 2", ignoreCase = true) || (isBootItem && !isBootT3 && !id.contains("speed")))

            when (type) {
                "boots_t2" -> matchesSearch && isBootT2
                "boots_t3" -> matchesSearch && (isBootT3 || item.category.contains("Nivel 3", ignoreCase = true))
                "boots" -> matchesSearch && (isBootT2 || isBootT3)
                else -> {
                    val matchesCat = when (selectedCat) {
                        "Físico" -> item.category.contains("físico", ignoreCase = true) || item.category.contains("ataque", ignoreCase = true)
                        "Magia" -> item.category.contains("magia", ignoreCase = true) || item.category.contains("mágico", ignoreCase = true)
                        "Defensa" -> item.category.contains("defensa", ignoreCase = true) || item.category.contains("tanque", ignoreCase = true)
                        "Apoyo" -> item.category.contains("apoyo", ignoreCase = true) || item.category.contains("soporte", ignoreCase = true)
                        else -> true
                    }
                    matchesSearch && matchesCat && !isBootItem
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HextechDarkBg,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (type) {
                        "boots_t2" -> tr("Seleccionar Botas (Nivel 2)")
                        "boots_t3" -> tr("Seleccionar Evolución (Nivel 3)")
                        "boots" -> tr("Seleccionar Botas")
                        "situational" -> tr("Seleccionar Objeto Situacional")
                        else -> tr("Seleccionar Objeto Core (Slots 1 a 5)")
                    },
                    color = HextechGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (type == "boots") tr("Buscar botas...") else tr("Buscar objeto en catálogo..."), fontSize = 12.sp, color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HextechCyan) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HextechCyan,
                        unfocusedBorderColor = HextechCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                if (type != "boots") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { c ->
                            val isSel = selectedCat == c
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) HextechGold.copy(alpha = 0.25f) else HextechSurface)
                                    .border(1.dp, if (isSel) HextechGold else HextechCardBorder, RoundedCornerShape(6.dp))
                                    .clickable { selectedCat = c }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = tr(c),
                                    color = if (isSel) HextechGold else TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    items(items) { item ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(HextechSurfaceVariant.copy(alpha = 0.6f))
                                .border(1.dp, HextechCardBorder, RoundedCornerShape(8.dp))
                                .clickable { onSelect(item) }
                                .padding(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(HextechSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(item.iconUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)
                                        
                                        .build(),
                                    contentDescription = item.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.name,
                                color = TextPrimary,
                                fontSize = 9.5.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
internal fun RuneCatalogSelectionDialog(
    mode: String, // "keystone", "secondary", "optional_keystone", "optional_secondary"
    excludedRuneNames: Set<String> = emptySet(),
    onDismiss: () -> Unit,
    onSelect: (RuneItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val isKeystoneMode = mode == "keystone" || mode == "optional_keystone"
    var selectedCat by remember(mode) { mutableStateOf("Todas") }

    val categories = if (!isKeystoneMode) listOf("Todas", "Dominación", "Precisión", "Valor", "Inspiración") else emptyList()

    val runes = remember(searchQuery, mode, selectedCat, excludedRuneNames) {
        WildRiftSpellsAndRunes.runes.filter { r ->
            if (excludedRuneNames.contains(r.name)) return@filter false
            val matchesSearch = r.name.contains(searchQuery, ignoreCase = true)
            val isClave = r.category.equals("Clave", ignoreCase = true)

            val matchesType = if (isKeystoneMode) {
                isClave
            } else {
                !isClave && when (selectedCat) {
                    "Dominación" -> r.category.contains("Dominaci", ignoreCase = true)
                    "Precisión" -> r.category.contains("Precisi", ignoreCase = true)
                    "Valor" -> r.category.contains("Valor", ignoreCase = true)
                    "Inspiración" -> r.category.contains("Inspiraci", ignoreCase = true) || r.category.contains("Brujer", ignoreCase = true)
                    else -> true
                }
            }
            matchesSearch && matchesType
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HextechDarkBg,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (mode) {
                        "keystone" -> tr("Seleccionar Runa Clave")
                        "optional_keystone" -> tr("Seleccionar Runa Clave Opcional")
                        "secondary" -> tr("Seleccionar Runa Secundaria")
                        "optional_secondary" -> tr("Seleccionar Runa Secundaria Opcional")
                        else -> tr("Seleccionar Runa")
                    },
                    color = HextechGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(tr("Buscar runa..."), fontSize = 12.sp, color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HextechGold) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HextechGold,
                        unfocusedBorderColor = HextechCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                if (categories.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { c ->
                            val isSel = selectedCat == c
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) HextechGold.copy(alpha = 0.25f) else HextechSurface)
                                    .border(1.dp, if (isSel) HextechGold else HextechCardBorder, RoundedCornerShape(6.dp))
                                    .clickable { selectedCat = c }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = tr(c),
                                    color = if (isSel) HextechGold else TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
                ) {
                    items(runes) { rune ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(HextechSurfaceVariant.copy(alpha = 0.6f))
                                .border(1.dp, HextechCardBorder, RoundedCornerShape(8.dp))
                                .clickable { onSelect(rune) }
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(HextechSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(rune.iconUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)
                                        
                                        .build(),
                                    contentDescription = rune.name,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = rune.name,
                                color = TextPrimary,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
internal fun SpellCatalogSelectionDialog(
    title: String = tr("Seleccionar Hechizo de Invocador"),
    excludedSpellNames: Set<String> = emptySet(),
    onDismiss: () -> Unit,
    onSelect: (SummonerSpellItem) -> Unit
) {
    val spells = remember(excludedSpellNames) {
        WildRiftSpellsAndRunes.summonerSpells.filter { !excludedSpellNames.contains(it.name) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HextechDarkBg,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = HextechGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                }
            }
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp)
            ) {
                items(spells) { spell ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(HextechSurfaceVariant.copy(alpha = 0.6f))
                            .border(1.dp, HextechCardBorder, RoundedCornerShape(8.dp))
                            .clickable { onSelect(spell) }
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(HextechSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(spell.iconUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)
                                    
                                    .build(),
                                contentDescription = spell.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = spell.name,
                            color = TextPrimary,
                            fontSize = 10.5.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}
