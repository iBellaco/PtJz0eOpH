package com.example.ui.components

import java.util.Locale
import android.net.Uri
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.WildRiftRepository
import com.example.data.local.CustomChampionBuildRecord
import com.example.data.local.CustomChampionBuildsManager
import com.example.data.local.ItemBuildEntry
import com.example.data.local.RuneBuildEntry
import com.example.data.local.SpellBuildEntry
import com.example.model.Champion
import com.example.util.AuthManager
import com.example.model.WildRiftItem
import com.example.model.RuneItem
import com.example.model.SummonerSpellItem
import com.example.ui.theme.*
import com.example.util.SubscriptionManager

class EditableItemEntry(
    val name: String,
    val iconUrl: String,
    initialDesc: String = ""
) {
    var description by mutableStateOf(initialDesc)
}

class EditableRuneEntry(
    val name: String,
    val iconUrl: String,
    initialDesc: String = ""
) {
    var description by mutableStateOf(initialDesc)
}

class EditableSpellEntry(
    val name: String,
    val iconUrl: String,
    initialDesc: String = ""
) {
    var description by mutableStateOf(initialDesc)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChampionBuildCreatorDialog(
    existingRecord: CustomChampionBuildRecord? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val champions = remember { WildRiftRepository.champions }
    val items = remember { WildRiftRepository.items }
    val runes = remember { WildRiftRepository.runes }
    val spells = remember { WildRiftRepository.summonerSpells }
    val creatorName by SubscriptionManager.userName.collectAsStateWithLifecycle()
    val currentAvatarId by SubscriptionManager.currentAvatarId.collectAsStateWithLifecycle()
    val currentRankBorder by SubscriptionManager.currentRankBorder.collectAsStateWithLifecycle()
    val userRole by SubscriptionManager.userRole.collectAsStateWithLifecycle()
    val authUser = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }

    var selectedChampion by remember { 
        mutableStateOf(
            if (existingRecord != null) champions.find { it.id.equals(existingRecord.championId, ignoreCase = true) } else champions.firstOrNull()
        ) 
    }
    var selectedRole by remember { 
        mutableStateOf(
            if (existingRecord != null) {
                com.example.model.LaneRole.values().find { it.displayName.equals(existingRecord.role, ignoreCase = true) } ?: selectedChampion?.primaryRole ?: com.example.model.LaneRole.MID
            } else {
                selectedChampion?.primaryRole ?: com.example.model.LaneRole.MID
            }
        )
    }

    LaunchedEffect(selectedChampion) {
        if (selectedChampion != null && existingRecord == null) {
            selectedRole = selectedChampion!!.primaryRole
        }
    }

    var buildTitle by remember { mutableStateOf(existingRecord?.buildTitle ?: "") }
    
    var bootT2Entry by remember {
        mutableStateOf<EditableItemEntry?>(
            existingRecord?.bootsT2Item?.let { EditableItemEntry(it.itemName, com.example.data.WildRiftItemsData.getItemIconByName(it.itemName), it.description) }
        )
    }
    var bootT3Entry by remember {
        mutableStateOf<EditableItemEntry?>(
            existingRecord?.bootsT3Item?.let { EditableItemEntry(it.itemName, com.example.data.WildRiftItemsData.getItemIconByName(it.itemName), it.description) }
        )
    }

    val coreItems = remember { 
        mutableStateListOf<EditableItemEntry>().apply {
            if (existingRecord != null) {
                val rawEntries = if (existingRecord.coreItemsWithDesc.isNotEmpty()) {
                    existingRecord.coreItemsWithDesc.map { EditableItemEntry(it.itemName, com.example.data.WildRiftItemsData.getItemIconByName(it.itemName), it.description) }
                } else {
                    existingRecord.coreItems.map { EditableItemEntry(it, com.example.data.WildRiftItemsData.getItemIconByName(it), "") }
                }
                val nonBoots = rawEntries.filter { entry ->
                    entry.name != existingRecord.bootsT2Item?.itemName && entry.name != existingRecord.bootsT3Item?.itemName
                }
                addAll(nonBoots.take(5))
            }
        }
    }
    val situationalItems = remember { 
        mutableStateListOf<EditableItemEntry>().apply {
            if (existingRecord != null) {
                if (existingRecord.situationalItemsWithDesc.isNotEmpty()) {
                    addAll(existingRecord.situationalItemsWithDesc.map { EditableItemEntry(it.itemName, com.example.data.WildRiftItemsData.getItemIconByName(it.itemName), it.description) })
                } else {
                    addAll(existingRecord.situationalItems.map { EditableItemEntry(it, com.example.data.WildRiftItemsData.getItemIconByName(it), "") })
                }
            }
        }
    }
    var coreKeystone by remember { 
        mutableStateOf<EditableRuneEntry?>(
            existingRecord?.coreRunes?.firstOrNull()?.let { EditableRuneEntry(it.runeName, it.iconUrl, it.description) }
        ) 
    }
    val coreSecondaryRunes = remember { 
        mutableStateListOf<EditableRuneEntry>().apply {
            if (existingRecord != null && existingRecord.coreRunes.size > 1) {
                addAll(existingRecord.coreRunes.drop(1).map { EditableRuneEntry(it.runeName, it.iconUrl, it.description) })
            }
        }
    }
    val situationalRunes = remember { 
        mutableStateListOf<EditableRuneEntry>().apply {
            if (existingRecord != null) {
                addAll(existingRecord.situationalRunes.map { EditableRuneEntry(it.runeName, it.iconUrl, it.description) })
            }
        }
    }
    val coreSpells = remember { 
        mutableStateListOf<EditableSpellEntry>().apply {
            if (existingRecord != null) {
                if (existingRecord.coreSpells.isNotEmpty()) {
                    addAll(existingRecord.coreSpells.map { EditableSpellEntry(it.spellName, it.iconUrl, it.description) })
                } else {
                    addAll(existingRecord.spells.map { EditableSpellEntry(it, com.example.data.WildRiftSpellsAndRunes.getSpellIconByName(it), "") })
                }
            }
        }
    }
    val situationalSpells = remember { 
        mutableStateListOf<EditableSpellEntry>().apply {
            if (existingRecord != null) {
                addAll(existingRecord.situationalSpells.map { EditableSpellEntry(it.spellName, it.iconUrl, it.description) })
            }
        }
    }
    var gameplayVideoUri by remember { mutableStateOf<String?>(existingRecord?.gameplayVideoUri) }
    var comboVideoUri by remember { mutableStateOf<String?>(existingRecord?.comboVideoUri) }

    val introVideoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val size = inputStream?.available() ?: 0
                inputStream?.close()
                if (size > 10 * 1024 * 1024) {
                    Toast.makeText(context, "El video de introducción supera el límite máximo de 10MB", Toast.LENGTH_SHORT).show()
                } else {
                    gameplayVideoUri = uri.toString()
                    Toast.makeText(context, "Video de introducción MP4 adjuntado (Máx 10MB)", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al adjuntar video de introducción", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val comboVideoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val size = inputStream?.available() ?: 0
                inputStream?.close()
                if (size > 10 * 1024 * 1024) {
                    Toast.makeText(context, "El video de combos supera el límite máximo de 10MB", Toast.LENGTH_SHORT).show()
                } else {
                    comboVideoUri = uri.toString()
                    Toast.makeText(context, "Video de combos MP4 adjuntado (Máx 10MB)", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al adjuntar video de combos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var showChampionPicker by remember { mutableStateOf(false) }
    var showItemPickerForCore by remember { mutableStateOf(false) }
    var showItemPickerForBootT2 by remember { mutableStateOf(false) }
    var showItemPickerForBootT3 by remember { mutableStateOf(false) }
    var showItemPickerForSituational by remember { mutableStateOf(false) }
    var showRunePickerForKeystone by remember { mutableStateOf(false) }
    var showRunePickerForSecondary by remember { mutableStateOf(false) }
    var showRunePickerForSituational by remember { mutableStateOf(false) }
    var showSpellPickerForCore by remember { mutableStateOf(false) }
    var showSpellPickerForSituational by remember { mutableStateOf(false) }
    var searchFilterQuery by remember { mutableStateOf("") }

    androidx.activity.compose.BackHandler { onDismiss() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = HextechSurface
    ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HextechGold.copy(alpha = 0.2f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = HextechGold, modifier = Modifier.padding(6.dp).size(20.dp))
                        }
                        Column {
                            Text("Creador de Builds Oficiales", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Configuración avanzada con imágenes y descripciones", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                HorizontalDivider(color = HextechCardBorder)

                // Form Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Selector de Campeón
                    Text("1. Seleccionar Campeón", color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showChampionPicker = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                        border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val champ = selectedChampion ?: champions.firstOrNull()
                                if (champ != null) {
                                    ChampionAvatar(champion = champ, size = 36.dp)
                                }
                                Column {
                                    Text(
                                        text = champ?.name ?: "Seleccionar campeón...",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Rol: ${champ?.primaryRole?.displayName ?: "-"}",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Text("Cambiar >", color = HextechGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 2. Línea / Rol del Campeón
                    Text("2. Seleccionar Línea / Rol de la Build", color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        com.example.model.LaneRole.values().forEach { role ->
                            val isSelected = selectedRole == role
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedRole = role },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) HextechGold.copy(alpha = 0.25f) else HextechDarkBg
                                ),
                                border = BorderStroke(1.dp, if (isSelected) HextechGold else HextechCardBorder)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    androidx.compose.foundation.Image(
                                        painter = androidx.compose.ui.res.painterResource(id = role.iconResId),
                                        contentDescription = role.displayName,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = role.shortName,
                                        color = if (isSelected) HextechGold else Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // 3. Título de la Build
                    Text("3. Título de la Build", color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = buildTitle,
                        onValueChange = { buildTitle = it },
                        placeholder = { Text("Ej: Build DPS Absoluto, Tanque Imparable...", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HextechGold,
                            unfocusedBorderColor = HextechSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // 3. Objetos Core (Máximo 5 objetos con descripción obligatoria)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "3. Objetos Core (${coreItems.size}/5) *Desc. Obligatoria",
                            color = HextechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (coreItems.size < 5) {
                            TextButton(onClick = { showItemPickerForCore = true }) {
                                Text("+ Añadir", color = HextechGold, fontSize = 11.sp)
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        coreItems.forEachIndexed { index, entry ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                                border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            AppAssetImage(
                                                url = entry.iconUrl,
                                                contentDescription = entry.name,
                                                fallbackText = entry.name.take(2),
                                                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(4.dp))
                                            )
                                            Text("${index + 1}. ${entry.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        IconButton(
                                            onClick = { coreItems.remove(entry) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = DangerRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    OutlinedTextField(
                                        value = entry.description,
                                        onValueChange = { entry.description = it },
                                        placeholder = { Text("Descripción obligatoria del objeto core...", color = TextSecondary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = HextechGold,
                                            unfocusedBorderColor = HextechSurfaceVariant,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                        if (coreItems.isEmpty()) {
                            Text("Ningún objeto core añadido (máx. 5).", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    // 4. Botas Nivel 2 y Botas Nivel 3 (Evolución) (Opcional)
                    Text(
                        text = "4. Botas Nivel 2 y Botas Nivel 3 (Evolución) (Opcional)",
                        color = HextechCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                        border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Botas Nivel 2
                            Text("Botas Nivel 2 (Base)", color = HextechGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            if (bootT2Entry == null) {
                                Button(
                                    onClick = { showItemPickerForBootT2 = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = HextechSurfaceVariant),
                                    border = BorderStroke(1.dp, HextechGold)
                                ) {
                                    Text("+ Seleccionar Botas Nivel 2", color = HextechGold, fontSize = 11.sp)
                                }
                            } else {
                                val entry = bootT2Entry!!
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            AppAssetImage(
                                                url = entry.iconUrl,
                                                contentDescription = entry.name,
                                                fallbackText = entry.name.take(2),
                                                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(4.dp))
                                            )
                                            Text(entry.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        IconButton(onClick = { bootT2Entry = null }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = DangerRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    OutlinedTextField(
                                        value = entry.description,
                                        onValueChange = { entry.description = it },
                                        placeholder = { Text("Descripción opcional de las botas...", color = TextSecondary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = HextechGold,
                                            unfocusedBorderColor = HextechSurfaceVariant,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                }
                            }

                            Divider(color = HextechSurfaceVariant)

                            // Botas Nivel 3 (Evolución / Encantamiento)
                            Text("Botas Nivel 3 (Evolución / Encantamiento)", color = HextechCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            if (bootT3Entry == null) {
                                Button(
                                    onClick = { showItemPickerForBootT3 = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = HextechSurfaceVariant),
                                    border = BorderStroke(1.dp, HextechCyan)
                                ) {
                                    Text("+ Seleccionar Botas Nivel 3", color = HextechCyan, fontSize = 11.sp)
                                }
                            } else {
                                val entry = bootT3Entry!!
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            AppAssetImage(
                                                url = entry.iconUrl,
                                                contentDescription = entry.name,
                                                fallbackText = entry.name.take(2),
                                                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(4.dp))
                                            )
                                            Text(entry.name, color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        IconButton(onClick = { bootT3Entry = null }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = DangerRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    OutlinedTextField(
                                        value = entry.description,
                                        onValueChange = { entry.description = it },
                                        placeholder = { Text("Descripción opcional de las Botas Nivel 3...", color = TextSecondary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = HextechCyan,
                                            unfocusedBorderColor = HextechSurfaceVariant,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 5. Objetos Situacionales (Con descripción obligatoria por cada uno)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "5. Objetos Situacionales *Desc. Obligatoria",
                            color = HextechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showItemPickerForSituational = true }) {
                            Text("+ Añadir", color = HextechGold, fontSize = 11.sp)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        situationalItems.forEachIndexed { index, entry ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                                border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            AppAssetImage(
                                                url = entry.iconUrl,
                                                contentDescription = entry.name,
                                                fallbackText = entry.name.take(2),
                                                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(4.dp))
                                            )
                                            Text("Sit. ${index + 1}. ${entry.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        IconButton(
                                            onClick = { situationalItems.remove(entry) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = DangerRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    OutlinedTextField(
                                        value = entry.description,
                                        onValueChange = { entry.description = it },
                                        placeholder = { Text("Descripción obligatoria del objeto situacional...", color = TextSecondary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = HextechGold,
                                            unfocusedBorderColor = HextechSurfaceVariant,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                        if (situationalItems.isEmpty()) {
                            Text("Ningún objeto situacional añadido.", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    // 6. Runa Clave (1 Runa obligatoria con descripción)
                    Text(
                        text = "6. Runa Clave (1 Runa) *Desc. Obligatoria",
                        color = HextechCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    if (coreKeystone == null) {
                        Button(
                            onClick = { showRunePickerForKeystone = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = HextechDarkBg),
                            border = BorderStroke(1.dp, HextechGold)
                        ) {
                            Text("+ Seleccionar Runa Clave", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else {
                        val entry = coreKeystone!!
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                            border = BorderStroke(1.dp, HextechGold)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        AppAssetImage(
                                            url = entry.iconUrl,
                                            contentDescription = entry.name,
                                            fallbackText = entry.name.take(2),
                                            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(4.dp))
                                        )
                                        Text("Runa Clave: ${entry.name}", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    IconButton(
                                        onClick = { coreKeystone = null },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = DangerRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                                OutlinedTextField(
                                    value = entry.description,
                                    onValueChange = { entry.description = it },
                                    placeholder = { Text("Descripción obligatoria de la runa clave...", color = TextSecondary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = HextechGold,
                                        unfocusedBorderColor = HextechSurfaceVariant,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // 7. Runas Secundarias (4 Runas obligatorias con descripción)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "7. Runas Secundarias (${coreSecondaryRunes.size}/4) *Desc. Obligatoria",
                            color = HextechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (coreSecondaryRunes.size < 4) {
                            TextButton(onClick = { showRunePickerForSecondary = true }) {
                                Text("+ Añadir", color = HextechGold, fontSize = 11.sp)
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        coreSecondaryRunes.forEachIndexed { index, entry ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                                border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            AppAssetImage(
                                                url = entry.iconUrl,
                                                contentDescription = entry.name,
                                                fallbackText = entry.name.take(2),
                                                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(4.dp))
                                            )
                                            Text("Secundaria ${index + 1}. ${entry.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        IconButton(
                                            onClick = { coreSecondaryRunes.remove(entry) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = DangerRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    OutlinedTextField(
                                        value = entry.description,
                                        onValueChange = { entry.description = it },
                                        placeholder = { Text("Descripción obligatoria de la runa secundaria...", color = TextSecondary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = HextechGold,
                                            unfocusedBorderColor = HextechSurfaceVariant,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                        if (coreSecondaryRunes.isEmpty()) {
                            Text("Ninguna runa secundaria añadida (requiere 4).", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    // Runas Situacionales (Opcionales con descripción obligatoria si se añaden)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Runas Situacionales (Opcional)",
                            color = HextechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showRunePickerForSituational = true }) {
                            Text("+ Añadir", color = HextechGold, fontSize = 11.sp)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        situationalRunes.forEachIndexed { index, entry ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                                border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            AppAssetImage(
                                                url = entry.iconUrl,
                                                contentDescription = entry.name,
                                                fallbackText = entry.name.take(2),
                                                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(4.dp))
                                            )
                                            Text("Sit. Runa ${index + 1}. ${entry.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        IconButton(
                                            onClick = { situationalRunes.remove(entry) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = DangerRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    OutlinedTextField(
                                        value = entry.description,
                                        onValueChange = { entry.description = it },
                                        placeholder = { Text("Descripción obligatoria de runa situacional...", color = TextSecondary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = HextechGold,
                                            unfocusedBorderColor = HextechSurfaceVariant,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 6. Hechizos Core (con descripción obligatoria)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "6. Hechizos Core *Desc. Obligatoria",
                            color = HextechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showSpellPickerForCore = true }) {
                            Text("+ Añadir", color = HextechGold, fontSize = 11.sp)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        coreSpells.forEachIndexed { index, entry ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                                border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            AppAssetImage(
                                                url = entry.iconUrl,
                                                contentDescription = entry.name,
                                                fallbackText = entry.name.take(2),
                                                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(4.dp))
                                            )
                                            Text("Hechizo ${index + 1}. ${entry.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        IconButton(
                                            onClick = { coreSpells.remove(entry) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = DangerRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    OutlinedTextField(
                                        value = entry.description,
                                        onValueChange = { entry.description = it },
                                        placeholder = { Text("Descripción obligatoria del hechizo...", color = TextSecondary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = HextechGold,
                                            unfocusedBorderColor = HextechSurfaceVariant,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                        if (coreSpells.isEmpty()) {
                            Text("Ningún hechizo core añadido.", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    // Hechizos Situacionales (Opcionales con descripción obligatoria si se añaden)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hechizos Situacionales (Opcional)",
                            color = HextechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showSpellPickerForSituational = true }) {
                            Text("+ Añadir", color = HextechGold, fontSize = 11.sp)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        situationalSpells.forEachIndexed { index, entry ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                                border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            AppAssetImage(
                                                url = entry.iconUrl,
                                                contentDescription = entry.name,
                                                fallbackText = entry.name.take(2),
                                                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(4.dp))
                                            )
                                            Text("Sit. Hechizo ${index + 1}. ${entry.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        IconButton(
                                            onClick = { situationalSpells.remove(entry) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = DangerRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    OutlinedTextField(
                                        value = entry.description,
                                        onValueChange = { entry.description = it },
                                        placeholder = { Text("Descripción obligatoria de hechizo situacional...", color = TextSecondary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = HextechGold,
                                            unfocusedBorderColor = HextechSurfaceVariant,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 9. Videos MP4 (Introducción y Combos, Máx 10MB cada uno)
                    Text("9. Video de Introducción (MP4, Máx 10MB)", color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Button(
                        onClick = { introVideoPickerLauncher.launch("video/mp4") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = HextechSurfaceVariant),
                        border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = HextechGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (gameplayVideoUri != null) "Video de Introducción MP4 Adjuntado" else "Seleccionar Video de Introducción (Máx 10MB)",
                            color = if (gameplayVideoUri != null) HextechGold else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    if (gameplayVideoUri != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                            border = BorderStroke(1.dp, HextechGold)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AndroidView(
                                    factory = { ctx ->
                                        VideoView(ctx).apply {
                                            setVideoURI(Uri.parse(gameplayVideoUri))
                                            setOnPreparedListener { mp ->
                                                mp.isLooping = true
                                                start()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = { gameplayVideoUri = null },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(28.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Eliminar video", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Text("10. Video de Combos (MP4, Máx 10MB) (Opcional)", color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Button(
                        onClick = { comboVideoPickerLauncher.launch("video/mp4") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = HextechSurfaceVariant),
                        border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (comboVideoUri != null) "Video de Combos MP4 Adjuntado" else "Seleccionar Video de Combos (Máx 10MB)",
                            color = if (comboVideoUri != null) HextechCyan else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    if (comboVideoUri != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                            border = BorderStroke(1.dp, HextechCyan)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AndroidView(
                                    factory = { ctx ->
                                        VideoView(ctx).apply {
                                            setVideoURI(Uri.parse(comboVideoUri))
                                            setOnPreparedListener { mp ->
                                                mp.isLooping = true
                                                start()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = { comboVideoUri = null },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(28.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Eliminar video", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Footer Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val champ = selectedChampion ?: champions.firstOrNull()
                            if (champ == null) {
                                Toast.makeText(context, "Selecciona un campeón", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (buildTitle.trim().isBlank()) {
                                Toast.makeText(context, "Ingresa un título para la build", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (coreItems.isEmpty()) {
                                Toast.makeText(context, "Debes añadir al menos un objeto core", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (coreItems.size > 5) {
                                Toast.makeText(context, "Máximo 5 objetos core permitidos", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (coreItems.any { it.description.trim().isBlank() }) {
                                Toast.makeText(context, "Todos los objetos core deben tener su descripción obligatoria", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (situationalItems.any { it.description.trim().isBlank() }) {
                                Toast.makeText(context, "Todos los objetos situacionales deben tener su descripción obligatoria", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (coreKeystone == null) {
                                Toast.makeText(context, "Debes seleccionar 1 Runa Clave", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (coreKeystone?.description?.trim()?.isBlank() == true) {
                                Toast.makeText(context, "La Runa Clave debe tener su descripción obligatoria", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (coreSecondaryRunes.size < 4) {
                                Toast.makeText(context, "Debes seleccionar exactamente 4 Runas Secundarias", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (coreSecondaryRunes.any { it.description.trim().isBlank() }) {
                                Toast.makeText(context, "Todas las runas secundarias deben tener su descripción obligatoria", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (situationalRunes.any { it.description.trim().isBlank() }) {
                                Toast.makeText(context, "Todas las runas situacionales deben tener su descripción obligatoria", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (coreSpells.isEmpty()) {
                                Toast.makeText(context, "Debes añadir al menos un hechizo core", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (coreSpells.any { it.description.trim().isBlank() }) {
                                Toast.makeText(context, "Todos los hechizos core deben tener su descripción obligatoria", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (situationalSpells.any { it.description.trim().isBlank() }) {
                                Toast.makeText(context, "Todos los hechizos situacionales deben tener su descripción obligatoria", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val allCoreRunes = listOfNotNull(coreKeystone) + coreSecondaryRunes
                            val record = CustomChampionBuildRecord(
                                id = existingRecord?.id ?: java.util.UUID.randomUUID().toString(),
                                championId = champ.id,
                                championName = champ.name,
                                buildTitle = buildTitle.trim(),
                                role = selectedRole.displayName,
                                coreItems = coreItems.map { it.name },
                                situationalItems = situationalItems.map { it.name },
                                runes = allCoreRunes.joinToString(", ") { it.name },
                                spells = coreSpells.map { it.name },
                                coreItemsWithDesc = coreItems.map { ItemBuildEntry(it.name, it.description.trim()) },
                                situationalItemsWithDesc = situationalItems.map { ItemBuildEntry(it.name, it.description.trim()) },
                                coreRunes = allCoreRunes.map { RuneBuildEntry(it.name, it.iconUrl, it.description.trim()) },
                                situationalRunes = situationalRunes.map { RuneBuildEntry(it.name, it.iconUrl, it.description.trim()) },
                                coreSpells = coreSpells.map { SpellBuildEntry(it.name, it.iconUrl, it.description.trim()) },
                                situationalSpells = situationalSpells.map { SpellBuildEntry(it.name, it.iconUrl, it.description.trim()) },
                                bootsT2Item = bootT2Entry?.let { ItemBuildEntry(it.name, it.description.trim()) },
                                bootsT3Item = bootT3Entry?.let { ItemBuildEntry(it.name, it.description.trim()) },
                                gameplayVideoUri = gameplayVideoUri,
                                comboVideoUri = comboVideoUri,
                                creatorName = existingRecord?.creatorName ?: if (creatorName.isBlank()) "Creador Oficial" else creatorName,
                                creatorAvatarId = existingRecord?.creatorAvatarId ?: currentAvatarId,
                                creatorRankBorder = existingRecord?.creatorRankBorder ?: currentRankBorder,
                                creatorIsAdmin = existingRecord?.creatorIsAdmin ?: (userRole == "admin" || AuthManager.isCurrentUserAdmin()),
                                creatorUserId = existingRecord?.creatorUserId ?: (authUser?.uid ?: "")
                            )

                            if (existingRecord != null) {
                                CustomChampionBuildsManager.updateBuild(context, record)
                                Toast.makeText(context, "¡Build avanzada actualizada con éxito!", Toast.LENGTH_SHORT).show()
                            } else {
                                CustomChampionBuildsManager.addBuild(context, record)
                                Toast.makeText(context, "¡Build avanzada de ${champ.name} creada y publicada con éxito!", Toast.LENGTH_SHORT).show()
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HextechGold)
                    ) {
                        Text(if (existingRecord != null) "Actualizar Build" else "Publicar Build", color = HextechDarkBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

    // Modal de selección de campeón
    if (showChampionPicker) {
        Dialog(onDismissRequest = { showChampionPicker = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HextechSurface)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Seleccionar Campeón", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = searchFilterQuery,
                        onValueChange = { searchFilterQuery = it },
                        placeholder = { Text("Buscar...", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        singleLine = true
                    )
                    val filtered = remember(searchFilterQuery, champions) {
                        if (searchFilterQuery.isBlank()) champions else champions.filter { it.name.contains(searchFilterQuery, ignoreCase = true) }
                    }
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(filtered) { champ ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedChampion = champ
                                        showChampionPicker = false
                                        searchFilterQuery = ""
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ChampionAvatar(champion = champ, size = 32.dp)
                                Text(champ.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Selector de Objetos (Core o Situacional)
    var selectedItemCategoryTab by remember { mutableStateOf("Todos") }
    val showItemPicker = showItemPickerForCore || showItemPickerForSituational
    if (showItemPicker) {
        Dialog(onDismissRequest = {
            showItemPickerForCore = false
            showItemPickerForSituational = false
            searchFilterQuery = ""
            selectedItemCategoryTab = "Todos"
        }) {
            Card(
                modifier = Modifier.fillMaxWidth().height(520.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HextechSurface)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (showItemPickerForCore) "Seleccionar Objeto Core" else "Seleccionar Objeto Situacional",
                            color = HextechGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        IconButton(
                            onClick = {
                                showItemPickerForCore = false
                                showItemPickerForSituational = false
                                searchFilterQuery = ""
                                selectedItemCategoryTab = "Todos"
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary)
                        }
                    }

                    OutlinedTextField(
                        value = searchFilterQuery,
                        onValueChange = { searchFilterQuery = it },
                        placeholder = { Text("Buscar objeto...", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        singleLine = true
                    )

                    // Filtros por Categoría
                    val categories = listOf("Todos", "Físico", "Mágico", "Defensa", "Apoyo")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            val isSel = selectedItemCategoryTab == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) HextechGold.copy(alpha = 0.25f) else HextechDarkBg)
                                    .border(1.dp, if (isSel) HextechGold else HextechCardBorder, RoundedCornerShape(6.dp))
                                    .clickable { selectedItemCategoryTab = cat }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSel) HextechGold else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    val filteredItems = remember(searchFilterQuery, items, showItemPickerForCore, coreItems, situationalItems, selectedItemCategoryTab) {
                        val base = if (searchFilterQuery.isBlank()) items else items.filter { it.name.contains(searchFilterQuery, ignoreCase = true) }
                        val nonBasicMid = base.filter { item ->
                            val cat = item.category.lowercase(Locale.ROOT)
                            val id = item.id.lowercase(Locale.ROOT)
                            !item.category.equals("Artículos Básicos", ignoreCase = true) &&
                            !item.category.equals("Objetos de Nivel Medio", ignoreCase = true) &&
                            !cat.contains("básico") && !cat.contains("basico") &&
                            !cat.contains("nivel medio") && !cat.contains("basic") &&
                            !cat.contains("mid tier") && !id.endsWith("_mid_tier") && !id.endsWith("_basic")
                        }.distinctBy { it.name.lowercase(Locale.ROOT).trim() }

                        val unselected = if (showItemPickerForCore) {
                            nonBasicMid.filter { item -> coreItems.none { it.name.equals(item.name, ignoreCase = true) } }
                        } else {
                            nonBasicMid.filter { item -> 
                                coreItems.none { it.name.equals(item.name, ignoreCase = true) } &&
                                situationalItems.none { it.name.equals(item.name, ignoreCase = true) }
                            }
                        }

                        val catFiltered = unselected.filter { item ->
                            val catLower = item.category.lowercase(Locale.ROOT)
                            val statsLower = item.stats.lowercase(Locale.ROOT)
                            when (selectedItemCategoryTab) {
                                "Físico" -> catLower.contains("físico") || catLower.contains("fisico") || statsLower.contains("daño de ataque") || statsLower.contains("tasa crítica")
                                "Mágico" -> catLower.contains("mágico") || catLower.contains("magico") || statsLower.contains("poder de habilidad")
                                "Defensa" -> catLower.contains("defensa") || catLower.contains("tanque") || statsLower.contains("armadura") || statsLower.contains("resistencia mágica")
                                "Apoyo" -> catLower.contains("apoyo") || catLower.contains("soporte")
                                else -> true
                            }
                        }

                        catFiltered.sortedBy { it.name.lowercase(Locale.ROOT) }
                    }

                    fun getItemCategoryName(item: com.example.model.WildRiftItem): String {
                        val catLower = item.category.lowercase(Locale.ROOT)
                        val statsLower = item.stats.lowercase(Locale.ROOT)
                        return when {
                            catLower.contains("físico") || catLower.contains("fisico") || statsLower.contains("daño de ataque") || statsLower.contains("tasa crítica") -> "Daño Físico"
                            catLower.contains("mágico") || catLower.contains("magico") || statsLower.contains("poder de habilidad") -> "Daño Mágico"
                            catLower.contains("defensa") || catLower.contains("tanque") || statsLower.contains("armadura") || statsLower.contains("resistencia mágica") -> "Defensa / Tanque"
                            catLower.contains("apoyo") || catLower.contains("soporte") -> "Apoyo / Utilidad"
                            else -> "Otros Objetos"
                        }
                    }

                    val groupedItems = remember(filteredItems, selectedItemCategoryTab, searchFilterQuery) {
                        if (selectedItemCategoryTab == "Todos" && searchFilterQuery.isBlank()) {
                            filteredItems.groupBy { getItemCategoryName(it) }
                        } else {
                            mapOf("" to filteredItems)
                        }
                    }

                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        groupedItems.forEach { (groupTitle, groupList) ->
                            if (groupTitle.isNotEmpty()) {
                                item(key = "header_$groupTitle") {
                                    Text(
                                        text = groupTitle.uppercase(Locale.ROOT),
                                        color = HextechGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
                                    )
                                }
                            }
                            items(groupList, key = { it.id }) { item ->
                                val catName = getItemCategoryName(item)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(HextechDarkBg)
                                        .clickable {
                                            if (showItemPickerForCore) {
                                                coreItems.add(EditableItemEntry(item.name, item.iconUrl))
                                                showItemPickerForCore = false
                                            } else {
                                                situationalItems.add(EditableItemEntry(item.name, item.iconUrl))
                                                showItemPickerForSituational = false
                                            }
                                            searchFilterQuery = ""
                                            selectedItemCategoryTab = "Todos"
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AppAssetImage(
                                        url = item.iconUrl,
                                        contentDescription = item.name,
                                        fallbackText = item.name.take(2),
                                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(6.dp))
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text("Oro: ${item.goldCost}", color = HextechGold, fontSize = 10.sp)
                                            Text("•", color = TextMuted, fontSize = 10.sp)
                                            Text(catName, color = HextechCyan, fontSize = 10.sp)
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

    // Modal Selector de Botas (Nivel 2 o Evolución Nivel 3)
    val showBootPicker = showItemPickerForBootT2 || showItemPickerForBootT3
    if (showBootPicker) {
        Dialog(onDismissRequest = {
            showItemPickerForBootT2 = false
            showItemPickerForBootT3 = false
            searchFilterQuery = ""
        }) {
            Card(
                modifier = Modifier.fillMaxWidth().height(500.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HextechSurface)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (showItemPickerForBootT2) "Seleccionar Botas Nivel 2" else "Seleccionar Botas Nivel 3 (Evolución)",
                        color = HextechGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = searchFilterQuery,
                        onValueChange = { searchFilterQuery = it },
                        placeholder = { Text("Buscar botas...", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        singleLine = true
                    )
                    val filteredBoots = remember(searchFilterQuery, items, showItemPickerForBootT2) {
                        val base = if (searchFilterQuery.isBlank()) items else items.filter { it.name.contains(searchFilterQuery, ignoreCase = true) }
                        val result = if (showItemPickerForBootT2) {
                            base.filter { item ->
                                val cat = item.category.lowercase(Locale.ROOT)
                                val id = item.id.lowercase(Locale.ROOT)
                                (item.category.equals("Botas Nivel 2", ignoreCase = true) || cat.contains("botas nivel 2") || cat.contains("botas n2") || id.startsWith("boot_")) &&
                                !cat.contains("básico") && !cat.contains("basico") && !cat.contains("nivel medio") &&
                                !cat.contains("nivel 3") && !cat.contains("botas nivel 3") && !cat.contains("botas n3") &&
                                !listOf("immortal_treds", "gunmetal_greaves", "chainlaced_crushers", "armored_advance", "crimson_lucidity", "spellslinger_s_shoes", "armorcrusher_boots").contains(id)
                            }
                        } else {
                            base.filter { item ->
                                val cat = item.category.lowercase(Locale.ROOT)
                                val id = item.id.lowercase(Locale.ROOT)
                                item.category.equals("Botas Nivel 3", ignoreCase = true) ||
                                cat.contains("botas nivel 3") || cat.contains("botas n3") ||
                                (cat.contains("nivel 3") && (cat.contains("bota") || cat.contains("boot") || id.contains("boot") || id.contains("greave") || id.contains("tread") || id.contains("tred"))) ||
                                listOf("immortal_treds", "gunmetal_greaves", "chainlaced_crushers", "armored_advance", "crimson_lucidity", "spellslinger_s_shoes", "armorcrusher_boots").contains(id)
                            }
                        }.distinctBy { it.name.lowercase(Locale.ROOT).trim() }
                        result.sortedBy { it.name.lowercase(Locale.ROOT) }
                    }
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(filteredBoots) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (showItemPickerForBootT2) {
                                            bootT2Entry = EditableItemEntry(item.name, item.iconUrl)
                                            showItemPickerForBootT2 = false
                                        } else {
                                            bootT3Entry = EditableItemEntry(item.name, item.iconUrl)
                                            showItemPickerForBootT3 = false
                                        }
                                        searchFilterQuery = ""
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AppAssetImage(
                                    url = item.iconUrl,
                                    contentDescription = item.name,
                                    fallbackText = item.name.take(2),
                                    modifier = Modifier.size(28.dp).clip(RoundedCornerShape(4.dp))
                                )
                                Column {
                                    Text(item.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Oro: ${item.goldCost} • ${item.category}", color = HextechGold, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Selector de Runas (Keystone, Secondary o Situational)
    val showRunePicker = showRunePickerForKeystone || showRunePickerForSecondary || showRunePickerForSituational
    if (showRunePicker) {
        Dialog(onDismissRequest = {
            showRunePickerForKeystone = false
            showRunePickerForSecondary = false
            showRunePickerForSituational = false
            searchFilterQuery = ""
        }) {
            Card(
                modifier = Modifier.fillMaxWidth().height(500.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HextechSurface)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = when {
                            showRunePickerForKeystone -> "Seleccionar Runa Clave"
                            showRunePickerForSecondary -> "Seleccionar Runa Secundaria (${coreSecondaryRunes.size}/4)"
                            else -> "Seleccionar Runa Situacional"
                        },
                        color = HextechGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = searchFilterQuery,
                        onValueChange = { searchFilterQuery = it },
                        placeholder = { Text("Buscar runa...", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        singleLine = true
                    )
                    val filteredRunes = remember(searchFilterQuery, runes, showRunePickerForKeystone, showRunePickerForSecondary, coreKeystone, coreSecondaryRunes, situationalRunes) {
                        val base = if (searchFilterQuery.isBlank()) runes else runes.filter { it.name.contains(searchFilterQuery, ignoreCase = true) }
                        val categorized = if (showRunePickerForKeystone) {
                            base.filter { it.category.contains("Clave", ignoreCase = true) }
                        } else {
                            // Runas secundarias y situacionales no deben mostrar runas clave
                            base.filter { !it.category.contains("Clave", ignoreCase = true) }
                        }
                        categorized.filter { rune ->
                            val alreadyKeystone = coreKeystone?.name?.equals(rune.name, ignoreCase = true) == true
                            val alreadySecondary = coreSecondaryRunes.any { it.name.equals(rune.name, ignoreCase = true) }
                            val alreadySituational = situationalRunes.any { it.name.equals(rune.name, ignoreCase = true) }
                            !alreadyKeystone && !alreadySecondary && !alreadySituational
                        }
                    }
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(filteredRunes) { rune ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (showRunePickerForKeystone) {
                                            coreKeystone = EditableRuneEntry(rune.name, rune.iconUrl)
                                            showRunePickerForKeystone = false
                                        } else if (showRunePickerForSecondary) {
                                            if (coreSecondaryRunes.size < 4) {
                                                coreSecondaryRunes.add(EditableRuneEntry(rune.name, rune.iconUrl))
                                            }
                                            showRunePickerForSecondary = false
                                        } else {
                                            situationalRunes.add(EditableRuneEntry(rune.name, rune.iconUrl))
                                            showRunePickerForSituational = false
                                        }
                                        searchFilterQuery = ""
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AppAssetImage(
                                    url = rune.iconUrl,
                                    contentDescription = rune.name,
                                    fallbackText = rune.name.take(2),
                                    modifier = Modifier.size(28.dp).clip(RoundedCornerShape(4.dp))
                                )
                                Column {
                                    Text(rune.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Categoría: ${rune.category}", color = HextechGold, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Selector de Hechizos (Core o Situacional)
    val showSpellPicker = showSpellPickerForCore || showSpellPickerForSituational
    if (showSpellPicker) {
        Dialog(onDismissRequest = {
            showSpellPickerForCore = false
            showSpellPickerForSituational = false
            searchFilterQuery = ""
        }) {
            Card(
                modifier = Modifier.fillMaxWidth().height(500.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HextechSurface)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (showSpellPickerForCore) "Seleccionar Hechizo Core" else "Seleccionar Hechizo Situacional",
                        color = HextechGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = searchFilterQuery,
                        onValueChange = { searchFilterQuery = it },
                        placeholder = { Text("Buscar hechizo...", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        singleLine = true
                    )
                    val filteredSpells = remember(searchFilterQuery, spells, showSpellPickerForCore, coreSpells, situationalSpells) {
                        val base = if (searchFilterQuery.isBlank()) spells else spells.filter { it.name.contains(searchFilterQuery, ignoreCase = true) }
                        val alreadySelectedNames = (coreSpells.map { it.name.lowercase() } + situationalSpells.map { it.name.lowercase() }).toSet()
                        base.filter { spell -> !alreadySelectedNames.contains(spell.name.lowercase()) }
                    }
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(filteredSpells) { spell ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (showSpellPickerForCore) {
                                            coreSpells.add(EditableSpellEntry(spell.name, spell.iconUrl))
                                            showSpellPickerForCore = false
                                        } else {
                                            situationalSpells.add(EditableSpellEntry(spell.name, spell.iconUrl))
                                            showSpellPickerForSituational = false
                                        }
                                        searchFilterQuery = ""
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AppAssetImage(
                                    url = spell.iconUrl,
                                    contentDescription = spell.name,
                                    fallbackText = spell.name.take(2),
                                    modifier = Modifier.size(28.dp).clip(RoundedCornerShape(4.dp))
                                )
                                Column {
                                    Text(spell.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("CD: ${spell.cooldown}", color = HextechGold, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
