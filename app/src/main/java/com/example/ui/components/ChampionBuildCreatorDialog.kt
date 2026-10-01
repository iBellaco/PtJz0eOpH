package com.example.ui.components

import com.example.util.tr
import android.net.Uri
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

    val coreItems = remember {
        mutableStateListOf<EditableItemEntry>().apply {
            if (existingRecord != null) {
                if (existingRecord.coreItemsWithDesc.isNotEmpty()) {
                    addAll(existingRecord.coreItemsWithDesc.map { EditableItemEntry(it.itemName, com.example.data.WildRiftItemsData.getItemIconByName(it.itemName), it.description) })
                } else {
                    addAll(existingRecord.coreItems.map { EditableItemEntry(it, com.example.data.WildRiftItemsData.getItemIconByName(it), "") })
                }
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
    var bootsT2 by remember {
        mutableStateOf<EditableItemEntry?>(
            existingRecord?.bootsT2Item?.let { EditableItemEntry(it.itemName, com.example.data.WildRiftItemsData.getItemIconByName(it.itemName), it.description) }
        )
    }
    var bootsT3 by remember {
        mutableStateOf<EditableItemEntry?>(
            existingRecord?.bootsT3Item?.let { EditableItemEntry(it.itemName, com.example.data.WildRiftItemsData.getItemIconByName(it.itemName), it.description) }
        )
    }
    var situationalBootsT2 by remember {
        mutableStateOf<EditableItemEntry?>(
            existingRecord?.situationalBootsT2Item?.let { EditableItemEntry(it.itemName, com.example.data.WildRiftItemsData.getItemIconByName(it.itemName), it.description) }
        )
    }
    var situationalBootsT3 by remember {
        mutableStateOf<EditableItemEntry?>(
            existingRecord?.situationalBootsT3Item?.let { EditableItemEntry(it.itemName, com.example.data.WildRiftItemsData.getItemIconByName(it.itemName), it.description) }
        )
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

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val size = inputStream?.available() ?: 0
                inputStream?.close()
                if (size > 20 * 1024 * 1024) {
                    Toast.makeText(context, com.example.util.appTr("El video supera el límite máximo de 20MB"), Toast.LENGTH_SHORT).show()
                } else {
                    gameplayVideoUri = uri.toString()
                    Toast.makeText(context, com.example.util.appTr("Gameplay MP4 adjuntado con éxito"), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, com.example.util.appTr("Error al adjuntar video"), Toast.LENGTH_SHORT).show()
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
                if (size > 20 * 1024 * 1024) {
                    Toast.makeText(context, com.example.util.appTr("El video supera el límite máximo de 20MB"), Toast.LENGTH_SHORT).show()
                } else {
                    comboVideoUri = uri.toString()
                    Toast.makeText(context, com.example.util.appTr("Video de combos adjuntado con éxito"), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, com.example.util.appTr("Error al adjuntar video de combos"), Toast.LENGTH_SHORT).show()
            }
        }
    }

    var showChampionPicker by remember { mutableStateOf(false) }
    var showItemPickerForCore by remember { mutableStateOf(false) }
    var showItemPickerForSituational by remember { mutableStateOf(false) }
    var showItemPickerForBootsT2 by remember { mutableStateOf(false) }
    var showItemPickerForBootsT3 by remember { mutableStateOf(false) }
    var showItemPickerForSituationalBootsT2 by remember { mutableStateOf(false) }
    var showItemPickerForSituationalBootsT3 by remember { mutableStateOf(false) }
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
                            Text(tr("Creador de Builds Oficiales"), color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(tr("Configuración avanzada con imágenes y descripciones"), color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Cerrar"), tint = Color.White)
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
                    Text(tr("1. Seleccionar Campeón"), color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                                        text = com.example.util.tr(champ?.name ?: "Seleccionar campeón..."),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = com.example.util.tr("Rol: ${champ?.primaryRole?.displayName ?: "-"}"),
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Text(tr("Cambiar >"), color = HextechGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 2. Línea / Rol del Campeón
                    Text(tr("2. Seleccionar Línea / Rol de la Build"), color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                                        text = com.example.util.tr(role.shortName),
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
                    Text(tr("3. Título de la Build"), color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    OutlinedTextField(
                        value = buildTitle,
                        onValueChange = { buildTitle = it },
                        placeholder = { Text(tr("Ej: Build DPS Absoluto, Tanque Imparable..."), color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HextechGold,
                            unfocusedBorderColor = HextechSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                     // 3. Objetos Core (Con descripción obligatoria)
                     Row(
                         modifier = Modifier.fillMaxWidth(),
                         horizontalArrangement = Arrangement.SpaceBetween,
                         verticalAlignment = Alignment.CenterVertically
                     ) {
                         Text(
                             text = com.example.util.tr("3. Objetos Core (${coreItems.size}/3) *Desc. Obligatoria"),
                             color = HextechCyan,
                             fontWeight = FontWeight.Bold,
                             fontSize = 13.sp,
                             modifier = Modifier.weight(1f)
                         )
                         TextButton(onClick = {
                             if (coreItems.size >= 3) {
                                 Toast.makeText(context, com.example.util.appTr("Límite de 3 objetos core alcanzado"), Toast.LENGTH_SHORT).show()
                             } else {
                                 showItemPickerForCore = true
                             }
                         }) {
                             Text(tr("+ Añadir"), color = HextechGold, fontSize = 11.sp)
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
                                            Text(com.example.util.tr("${index + 1}. ${entry.name}"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        IconButton(
                                            onClick = { coreItems.remove(entry) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Eliminar"), tint = DangerRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    OutlinedTextField(
                                        value = entry.description,
                                        onValueChange = { entry.description = it },
                                        placeholder = { Text(tr("Descripción obligatoria del objeto core..."), color = TextSecondary) },
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
                            Text(tr("Ningún objeto core añadido."), color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    // 4. Objetos Situacionales (Con descripción obligatoria por cada uno)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = com.example.util.tr("4. Objetos Situacionales *Desc. Obligatoria"),
                            color = HextechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showItemPickerForSituational = true }) {
                            Text(tr("+ Añadir"), color = HextechGold, fontSize = 11.sp)
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
                                            Text(com.example.util.tr("Sit. ${index + 1}. ${entry.name}"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        IconButton(
                                            onClick = { situationalItems.remove(entry) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Eliminar"), tint = DangerRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    OutlinedTextField(
                                        value = entry.description,
                                        onValueChange = { entry.description = it },
                                        placeholder = { Text(tr("Descripción obligatoria del objeto situacional..."), color = TextSecondary) },
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
                            Text(tr("Ningún objeto situacional añadido."), color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5. Botas y Mejoras (Obligatorias)
                    Text(
                        text = com.example.util.tr("5. Botas y Mejoras (Obligatorias) *Desc. Obligatoria"),
                        color = HextechCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Botas Nivel 2 Obligatoria
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                            border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (bootsT2 != null) {
                                            AppAssetImage(
                                                url = bootsT2!!.iconUrl,
                                                contentDescription = bootsT2!!.name,
                                                fallbackText = bootsT2!!.name.take(2),
                                                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(4.dp))
                                            )
                                            Text(com.example.util.tr("Botas Nivel 2: ${bootsT2!!.name}"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        } else {
                                            Text(tr("Botas Nivel 2 *Requerido"), color = TextSecondary, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                        }
                                    }
                                    if (bootsT2 != null) {
                                        IconButton(onClick = { bootsT2 = null }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Eliminar"), tint = DangerRed, modifier = Modifier.size(16.dp))
                                        }
                                    } else {
                                        TextButton(onClick = { showItemPickerForBootsT2 = true }) {
                                            Text(tr("+ Seleccionar"), color = HextechGold, fontSize = 11.sp)
                                        }
                                    }
                                }
                                if (bootsT2 != null) {
                                    OutlinedTextField(
                                        value = bootsT2!!.description,
                                        onValueChange = { bootsT2!!.description = it },
                                        placeholder = { Text(tr("Descripción obligatoria de las botas nivel 2..."), color = TextSecondary) },
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

                        // Botas Nivel 3 Obligatoria
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                            border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (bootsT3 != null) {
                                            AppAssetImage(
                                                url = bootsT3!!.iconUrl,
                                                contentDescription = bootsT3!!.name,
                                                fallbackText = bootsT3!!.name.take(2),
                                                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(4.dp))
                                            )
                                            Text(com.example.util.tr("Mejora Nivel 3: ${bootsT3!!.name}"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        } else {
                                            Text(tr("Mejora Nivel 3 *Requerido"), color = TextSecondary, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                        }
                                    }
                                    if (bootsT3 != null) {
                                        IconButton(onClick = { bootsT3 = null }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Eliminar"), tint = DangerRed, modifier = Modifier.size(16.dp))
                                        }
                                    } else {
                                        TextButton(onClick = { showItemPickerForBootsT3 = true }) {
                                            Text(tr("+ Seleccionar"), color = HextechGold, fontSize = 11.sp)
                                        }
                                    }
                                }
                                if (bootsT3 != null) {
                                    OutlinedTextField(
                                        value = bootsT3!!.description,
                                        onValueChange = { bootsT3!!.description = it },
                                        placeholder = { Text(tr("Descripción obligatoria de la mejora nivel 3..."), color = TextSecondary) },
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5b. Botas y Mejoras Situacionales (Opcional)
                    Text(
                        text = com.example.util.tr("5b. Botas y Mejoras Situacionales (Opcional)"),
                        color = HextechCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Botas Nivel 2 Situacional
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                            border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (situationalBootsT2 != null) {
                                            AppAssetImage(
                                                url = situationalBootsT2!!.iconUrl,
                                                contentDescription = situationalBootsT2!!.name,
                                                fallbackText = situationalBootsT2!!.name.take(2),
                                                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(4.dp))
                                            )
                                            Text(com.example.util.tr("Bota Nivel 2 Situacional: ${situationalBootsT2!!.name}"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        } else {
                                            Text(tr("Añadir Bota Nivel 2 Situacional"), color = TextSecondary, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                        }
                                    }
                                    if (situationalBootsT2 != null) {
                                        IconButton(onClick = { situationalBootsT2 = null }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Eliminar"), tint = DangerRed, modifier = Modifier.size(16.dp))
                                        }
                                    } else {
                                        TextButton(onClick = { showItemPickerForSituationalBootsT2 = true }) {
                                            Text(tr("+ Seleccionar"), color = HextechGold, fontSize = 11.sp)
                                        }
                                    }
                                }
                                if (situationalBootsT2 != null) {
                                    OutlinedTextField(
                                        value = situationalBootsT2!!.description,
                                        onValueChange = { situationalBootsT2!!.description = it },
                                        placeholder = { Text(tr("Descripción de la bota nivel 2 situacional..."), color = TextSecondary) },
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

                        // Botas Nivel 3 Situacional
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                            border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (situationalBootsT3 != null) {
                                            AppAssetImage(
                                                url = situationalBootsT3!!.iconUrl,
                                                contentDescription = situationalBootsT3!!.name,
                                                fallbackText = situationalBootsT3!!.name.take(2),
                                                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(4.dp))
                                            )
                                            Text(com.example.util.tr("Mejora Nivel 3 Situacional: ${situationalBootsT3!!.name}"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        } else {
                                            Text(tr("Añadir Mejora Nivel 3 Situacional"), color = TextSecondary, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                        }
                                    }
                                    if (situationalBootsT3 != null) {
                                        IconButton(onClick = { situationalBootsT3 = null }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Eliminar"), tint = DangerRed, modifier = Modifier.size(16.dp))
                                        }
                                    } else {
                                        TextButton(onClick = { showItemPickerForSituationalBootsT3 = true }) {
                                            Text(tr("+ Seleccionar"), color = HextechGold, fontSize = 11.sp)
                                        }
                                    }
                                }
                                if (situationalBootsT3 != null) {
                                    OutlinedTextField(
                                        value = situationalBootsT3!!.description,
                                        onValueChange = { situationalBootsT3!!.description = it },
                                        placeholder = { Text(tr("Descripción de la mejora nivel 3 situacional..."), color = TextSecondary) },
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // 6. Runa Clave (1 Runa obligatoria con descripción)
                    Text(
                        text = com.example.util.tr("6. Runa Clave (1 Runa) *Desc. Obligatoria"),
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
                            Text(tr("+ Seleccionar Runa Clave"), color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                                        Text(com.example.util.tr("Runa Clave: ${entry.name}"), color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    IconButton(
                                        onClick = { coreKeystone = null },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Eliminar"), tint = DangerRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                                OutlinedTextField(
                                    value = entry.description,
                                    onValueChange = { entry.description = it },
                                    placeholder = { Text(tr("Descripción obligatoria de la runa clave..."), color = TextSecondary) },
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
                            text = com.example.util.tr("7. Runas Secundarias (${coreSecondaryRunes.size}/4) *Desc. Obligatoria"),
                            color = HextechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (coreSecondaryRunes.size < 4) {
                            TextButton(onClick = { showRunePickerForSecondary = true }) {
                                Text(tr("+ Añadir"), color = HextechGold, fontSize = 11.sp)
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
                                            Text(com.example.util.tr("Secundaria ${index + 1}. ${entry.name}"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        IconButton(
                                            onClick = { coreSecondaryRunes.remove(entry) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Eliminar"), tint = DangerRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    OutlinedTextField(
                                        value = entry.description,
                                        onValueChange = { entry.description = it },
                                        placeholder = { Text(tr("Descripción obligatoria de la runa secundaria..."), color = TextSecondary) },
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
                            Text(tr("Ninguna runa secundaria añadida (requiere 4)."), color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    // Runas Situacionales (Opcionales con descripción obligatoria si se añaden)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = com.example.util.tr("Runas Situacionales (Opcional)"),
                            color = HextechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showRunePickerForSituational = true }) {
                            Text(tr("+ Añadir"), color = HextechGold, fontSize = 11.sp)
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
                                            Text(com.example.util.tr("Sit. Runa ${index + 1}. ${entry.name}"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        IconButton(
                                            onClick = { situationalRunes.remove(entry) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Eliminar"), tint = DangerRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    OutlinedTextField(
                                        value = entry.description,
                                        onValueChange = { entry.description = it },
                                        placeholder = { Text(tr("Descripción obligatoria de runa situacional..."), color = TextSecondary) },
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

                    // 6. Hechizos Core (Imágenes con descripción obligatoria)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = com.example.util.tr("6. Hechizos Core (Imágenes) *Desc. Obligatoria"),
                            color = HextechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showSpellPickerForCore = true }) {
                            Text(tr("+ Añadir"), color = HextechGold, fontSize = 11.sp)
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
                                            Text(com.example.util.tr("Hechizo ${index + 1}. ${entry.name}"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        IconButton(
                                            onClick = { coreSpells.remove(entry) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Eliminar"), tint = DangerRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    OutlinedTextField(
                                        value = entry.description,
                                        onValueChange = { entry.description = it },
                                        placeholder = { Text(tr("Descripción obligatoria del hechizo..."), color = TextSecondary) },
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
                            Text(tr("Ningún hechizo core añadido."), color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    // Hechizos Situacionales (Opcionales con descripción obligatoria si se añaden)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = com.example.util.tr("Hechizos Situacionales (Opcional)"),
                            color = HextechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showSpellPickerForSituational = true }) {
                            Text(tr("+ Añadir"), color = HextechGold, fontSize = 11.sp)
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
                                            Text(com.example.util.tr("Sit. Hechizo ${index + 1}. ${entry.name}"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        IconButton(
                                            onClick = { situationalSpells.remove(entry) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Eliminar"), tint = DangerRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    OutlinedTextField(
                                        value = entry.description,
                                        onValueChange = { entry.description = it },
                                        placeholder = { Text(tr("Descripción obligatoria de hechizo situacional..."), color = TextSecondary) },
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

                     // 7. Subir Gameplay MP4 (Máximo 20MB)
                     Text(tr("7. Gameplay Demostrativo (MP4, Máx 20MB)"), color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                     Text(tr("Recomendado: Horizontal 16:9 (ej. 1920x1080 o 1280x720), duración < 1 min"), color = TextSecondary, fontSize = 11.sp)
                     Button(
                         onClick = { videoPickerLauncher.launch("video/mp4") },
                         modifier = Modifier.fillMaxWidth(),
                         colors = ButtonDefaults.buttonColors(containerColor = HextechSurfaceVariant),
                         border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.7f)),
                         shape = RoundedCornerShape(8.dp)
                     ) {
                         Icon(Icons.Default.Videocam, contentDescription = null, tint = HextechGold, modifier = Modifier.size(20.dp))
                         Spacer(modifier = Modifier.width(8.dp))
                         Text(
                             text = com.example.util.tr(if (gameplayVideoUri != null) "✓ Gameplay MP4 Adjuntado" else "Seleccionar archivo MP4 (Máx 20MB)"),
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
                                     Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Eliminar video"), tint = Color.White, modifier = Modifier.size(16.dp))
                                 }
                             }
                         }
                     }

                     Spacer(modifier = Modifier.height(12.dp))

                     // 8. Subir Guía de Combos MP4 (Máximo 20MB)
                     Text(tr("8. Guía de Combos y Habilidades (MP4, Máx 20MB)"), color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                     Text(tr("Recomendado: Vertical 9:16 o Cuadrado 1:1 (ej. 1080x1920), duración < 45s"), color = TextSecondary, fontSize = 11.sp)
                     Button(
                         onClick = { comboVideoPickerLauncher.launch("video/mp4") },
                         modifier = Modifier.fillMaxWidth(),
                         colors = ButtonDefaults.buttonColors(containerColor = HextechSurfaceVariant),
                         border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.7f)),
                         shape = RoundedCornerShape(8.dp)
                     ) {
                         Icon(Icons.Default.Videocam, contentDescription = null, tint = HextechGold, modifier = Modifier.size(20.dp))
                         Spacer(modifier = Modifier.width(8.dp))
                         Text(
                             text = com.example.util.tr(if (comboVideoUri != null) "✓ Video de Combos MP4 Adjuntado" else "Seleccionar archivo MP4 (Máx 20MB)"),
                             color = if (comboVideoUri != null) HextechGold else Color.White,
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
                             border = BorderStroke(1.dp, HextechGold)
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
                                     Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Eliminar video combos"), tint = Color.White, modifier = Modifier.size(16.dp))
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
                        Text(tr("Cancelar"), color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val champ = selectedChampion ?: champions.firstOrNull()
                            if (champ == null) {
                                Toast.makeText(context, com.example.util.appTr("Selecciona un campeón"), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (buildTitle.trim().isBlank()) {
                                Toast.makeText(context, com.example.util.appTr("Ingresa un título para la build"), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                             if (coreItems.size != 3) {
                                 Toast.makeText(context, com.example.util.appTr("Debes añadir exactamente 3 objetos core"), Toast.LENGTH_SHORT).show()
                                 return@Button
                             }
                            if (coreItems.any { it.description.trim().isBlank() }) {
                                Toast.makeText(context, com.example.util.appTr("Todos los objetos core deben tener su descripción obligatoria"), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (situationalItems.size < 2) {
                                Toast.makeText(context, com.example.util.appTr("Añade al menos dos objetos situacionales"), Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            if (situationalItems.any { it.description.trim().isBlank() }) {
                                Toast.makeText(context, com.example.util.appTr("Todos los objetos situacionales deben tener su descripción obligatoria"), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (bootsT2 == null) {
                                Toast.makeText(context, com.example.util.appTr("Debes seleccionar las Botas de Nivel 2"), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (bootsT2?.description?.trim()?.isBlank() == true) {
                                Toast.makeText(context, com.example.util.appTr("La descripción de las Botas de Nivel 2 es obligatoria"), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (bootsT3 == null) {
                                Toast.makeText(context, com.example.util.appTr("Debes seleccionar la Mejora de Botas de Nivel 3"), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (bootsT3?.description?.trim()?.isBlank() == true) {
                                Toast.makeText(context, com.example.util.appTr("La descripción de la Mejora de Botas de Nivel 3 es obligatoria"), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (situationalBootsT2 != null && situationalBootsT2?.description?.trim()?.isBlank() == true) {
                                Toast.makeText(context, com.example.util.appTr("La descripción de la Bota Nivel 2 Situacional es obligatoria"), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (situationalBootsT3 != null && situationalBootsT3?.description?.trim()?.isBlank() == true) {
                                Toast.makeText(context, com.example.util.appTr("La descripción de la Mejora Nivel 3 Situacional es obligatoria"), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (coreKeystone == null) {
                                Toast.makeText(context, com.example.util.appTr("Debes seleccionar 1 Runa Clave"), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (coreKeystone?.description?.trim()?.isBlank() == true) {
                                Toast.makeText(context, com.example.util.appTr("La Runa Clave debe tener su descripción obligatoria"), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (coreSecondaryRunes.size < 4) {
                                Toast.makeText(context, com.example.util.appTr("Debes seleccionar exactamente 4 Runas Secundarias"), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (coreSecondaryRunes.any { it.description.trim().isBlank() }) {
                                Toast.makeText(context, com.example.util.appTr("Todas las runas secundarias deben tener su descripción obligatoria"), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (situationalRunes.any { !com.example.util.BuildChoiceRules.hasSituationalReason(it.description, com.example.data.WildRiftSpellsAndRunes.getRuneByName(it.name)?.description.orEmpty()) }) {
                                Toast.makeText(context, com.example.util.appTr("Todas las runas situacionales deben tener su descripción obligatoria"), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (coreSpells.isEmpty()) {
                                Toast.makeText(context, com.example.util.appTr("Debes añadir al menos un hechizo core"), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (coreSpells.any { it.description.trim().isBlank() }) {
                                Toast.makeText(context, com.example.util.appTr("Todos los hechizos core deben tener su descripción obligatoria"), Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (situationalSpells.any { it.description.trim().isBlank() }) {
                                Toast.makeText(context, com.example.util.appTr("Todos los hechizos situacionales deben tener su descripción obligatoria"), Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val allCoreRunes = listOfNotNull(coreKeystone) + coreSecondaryRunes
                            fun runeChoice(name: String): com.example.util.BuildChoiceRules.RuneChoice {
                                val rune = com.example.data.WildRiftSpellsAndRunes.getRuneByName(name)
                                return com.example.util.BuildChoiceRules.RuneChoice(rune?.name ?: name, rune?.category.orEmpty())
                            }
                            val runePage = allCoreRunes.map { runeChoice(it.name) }
                            if (!com.example.util.BuildChoiceRules.validRunePage(runePage)) {
                                Toast.makeText(context, com.example.util.appTr("Las tres primeras secundarias deben ser de la misma rama y la cuarta de otra rama"), Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            val validAlternatives = com.example.util.BuildChoiceRules.runeAlternatives(runePage, situationalRunes.map { runeChoice(it.name) })
                            if (validAlternatives.size != situationalRunes.size) {
                                Toast.makeText(context, com.example.util.appTr("Las runas situacionales solo pueden sustituir la clave o la cuarta secundaria"), Toast.LENGTH_LONG).show()
                                return@Button
                            }
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
                                bootsT2Item = bootsT2?.let { ItemBuildEntry(it.name, it.description.trim()) },
                                bootsT3Item = bootsT3?.let { ItemBuildEntry(it.name, it.description.trim()) },
                                situationalBootsT2Item = situationalBootsT2?.let { ItemBuildEntry(it.name, it.description.trim()) },
                                situationalBootsT3Item = situationalBootsT3?.let { ItemBuildEntry(it.name, it.description.trim()) },
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
                                Toast.makeText(context, com.example.util.appTr("¡Build avanzada actualizada con éxito!"), Toast.LENGTH_SHORT).show()
                            } else {
                                CustomChampionBuildsManager.addBuild(context, record)
                                Toast.makeText(context, com.example.util.appTr("¡Build avanzada de ${champ.name} creada y publicada con éxito!"), Toast.LENGTH_SHORT).show()
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HextechGold)
                    ) {
                        Text(com.example.util.tr(if (existingRecord != null) "Actualizar Build" else "Publicar Build"), color = HextechDarkBg, fontWeight = FontWeight.Bold)
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
                    Text(tr("Seleccionar Campeón"), color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = searchFilterQuery,
                        onValueChange = { searchFilterQuery = it },
                        placeholder = { Text(tr("Buscar..."), color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        singleLine = true
                    )
                     val filtered = remember(searchFilterQuery, champions) {
                         val base = if (searchFilterQuery.isBlank()) champions else champions.filter { it.name.contains(searchFilterQuery, ignoreCase = true) }
                         base.sortedBy { it.name }
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
                                Text(com.example.util.tr(champ.name), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Selector de Objetos (Core, Situacional, o Botas)
    val showItemPicker = showItemPickerForCore || showItemPickerForSituational || showItemPickerForBootsT2 || showItemPickerForBootsT3 || showItemPickerForSituationalBootsT2 || showItemPickerForSituationalBootsT3
    if (showItemPicker) {
        Dialog(onDismissRequest = {
            showItemPickerForCore = false
            showItemPickerForSituational = false
            showItemPickerForBootsT2 = false
            showItemPickerForBootsT3 = false
            showItemPickerForSituationalBootsT2 = false
            showItemPickerForSituationalBootsT3 = false
            searchFilterQuery = ""
        }) {
            Card(
                modifier = Modifier.fillMaxWidth().height(500.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HextechSurface)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val pickerTitle = when {
                        showItemPickerForCore -> "Seleccionar Objeto Core"
                        showItemPickerForSituational -> "Seleccionar Objeto Situacional"
                        showItemPickerForBootsT2 -> "Seleccionar Botas Nivel 2 (Obligatorias)"
                        showItemPickerForBootsT3 -> "Seleccionar Mejora Nivel 3 (Obligatorias)"
                        showItemPickerForSituationalBootsT2 -> "Seleccionar Bota Nivel 2 Situacional"
                        showItemPickerForSituationalBootsT3 -> "Seleccionar Mejora Nivel 3 Situacional"
                        else -> "Seleccionar Objeto"
                    }
                    Text(
                        text = com.example.util.tr(pickerTitle),
                        color = HextechGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = searchFilterQuery,
                        onValueChange = { searchFilterQuery = it },
                        placeholder = { Text(tr("Buscar objeto..."), color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        singleLine = true
                    )
                     val filteredItems = remember(
                         searchFilterQuery, items,
                         showItemPickerForCore, showItemPickerForSituational,
                         showItemPickerForBootsT2, showItemPickerForBootsT3,
                         showItemPickerForSituationalBootsT2, showItemPickerForSituationalBootsT3,
                         coreItems, situationalItems
                     ) {
                         fun normalizeQuery(s: String) = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                             .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
                             .lowercase().trim()
                         val qNorm = normalizeQuery(searchFilterQuery)

                         val base = if (qNorm.isBlank()) items else items.filter { item ->
                             normalizeQuery(item.name).contains(qNorm) ||
                             normalizeQuery(item.nameEn).contains(qNorm) ||
                             normalizeQuery(item.namePt).contains(qNorm) ||
                             normalizeQuery(item.category).contains(qNorm)
                         }
                         val pool = when {
                             showItemPickerForBootsT2 || showItemPickerForSituationalBootsT2 -> {
                                 base.filter { it.category == "Botas Nivel 2" }
                             }
                             showItemPickerForBootsT3 || showItemPickerForSituationalBootsT3 -> {
                                 base.filter { it.category == "Botas Nivel 3" }
                             }
                             showItemPickerForCore -> {
                                 base.filter { item ->
                                     !item.category.contains("Botas", ignoreCase = true) &&
                                     coreItems.none { it.name.equals(item.name, ignoreCase = true) }
                                 }
                             }
                             else -> { // showItemPickerForSituational
                                 base.filter { item ->
                                     !item.category.contains("Botas", ignoreCase = true) &&
                                     coreItems.none { it.name.equals(item.name, ignoreCase = true) } &&
                                     situationalItems.none { it.name.equals(item.name, ignoreCase = true) }
                                 }
                             }
                         }
                         pool.sortedWith(compareBy<com.example.model.WildRiftItem> { it.category }.thenBy { it.name })
                     }
                     val groupedItems = remember(filteredItems) {
                         filteredItems.groupBy { it.category }
                     }
                     LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                         groupedItems.forEach { (categoryName, list) ->
                             item {
                                 Surface(
                                     color = HextechGold.copy(alpha = 0.15f),
                                     shape = RoundedCornerShape(4.dp),
                                     modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 4.dp)
                                 ) {
                                     Text(
                                         text = com.example.util.tr(categoryName),
                                         color = HextechGold,
                                         fontWeight = FontWeight.Bold,
                                         fontSize = 11.sp,
                                         modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                     )
                                 }
                             }
                             items(list) { item ->
                                 Row(
                                     modifier = Modifier
                                         .fillMaxWidth()
                                         .clickable {
                                             val entry = EditableItemEntry(item.name, item.iconUrl)
                                             when {
                                                 showItemPickerForCore -> {
                                                     coreItems.add(entry)
                                                     showItemPickerForCore = false
                                                 }
                                                 showItemPickerForSituational -> {
                                                     situationalItems.add(entry)
                                                     showItemPickerForSituational = false
                                                 }
                                                 showItemPickerForBootsT2 -> {
                                                     bootsT2 = entry
                                                     showItemPickerForBootsT2 = false
                                                 }
                                                 showItemPickerForBootsT3 -> {
                                                     bootsT3 = entry
                                                     showItemPickerForBootsT3 = false
                                                 }
                                                 showItemPickerForSituationalBootsT2 -> {
                                                     situationalBootsT2 = entry
                                                     showItemPickerForSituationalBootsT2 = false
                                                 }
                                                 showItemPickerForSituationalBootsT3 -> {
                                                     situationalBootsT3 = entry
                                                     showItemPickerForSituationalBootsT3 = false
                                                 }
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
                                         Text(com.example.util.tr(item.name), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                         Text(com.example.util.tr("Oro: ${item.goldCost}"), color = HextechGold, fontSize = 10.sp)
                                     }
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
                        text = com.example.util.tr(when {
                            showRunePickerForKeystone -> "Seleccionar Runa Clave"
                            showRunePickerForSecondary -> "Seleccionar Runa Secundaria (${coreSecondaryRunes.size}/4)"
                            else -> "Seleccionar Runa Situacional"
                        }),
                        color = HextechGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = searchFilterQuery,
                        onValueChange = { searchFilterQuery = it },
                        placeholder = { Text(tr("Buscar runa..."), color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        singleLine = true
                    )
                     val filteredRunes = remember(searchFilterQuery, runes, showRunePickerForKeystone, showRunePickerForSecondary, coreKeystone, coreSecondaryRunes, situationalRunes) {
                         val base = if (searchFilterQuery.isBlank()) runes else runes.filter { it.name.contains(searchFilterQuery, ignoreCase = true) }
                         val pool = base.filter { rune ->
                             val alreadyKeystone = coreKeystone?.name?.equals(rune.name, ignoreCase = true) == true
                             val alreadySecondary = coreSecondaryRunes.any { it.name.equals(rune.name, ignoreCase = true) }
                             val alreadySituational = situationalRunes.any { it.name.equals(rune.name, ignoreCase = true) }
                             val primaryBranch = coreSecondaryRunes.firstOrNull()?.let {
                                 com.example.data.WildRiftSpellsAndRunes.getRuneByName(it.name)?.category
                             }
                             val allowedBranch = when {
                                 showRunePickerForKeystone -> rune.category == "Clave"
                                 rune.category == "Clave" -> !showRunePickerForSecondary
                                 showRunePickerForSecondary && coreSecondaryRunes.size in 1..2 -> rune.category == primaryBranch
                                 showRunePickerForSecondary && coreSecondaryRunes.size == 3 -> rune.category != primaryBranch
                                 else -> rune.category == "Clave" || rune.category != primaryBranch
                             }
                             allowedBranch && !alreadyKeystone && !alreadySecondary && !alreadySituational
                         }
                         pool.sortedWith(compareBy<com.example.model.RuneItem> { it.category }.thenBy { it.name })
                     }
                     val groupedRunes = remember(filteredRunes) {
                         filteredRunes.groupBy { it.category }
                     }
                     LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                         groupedRunes.forEach { (categoryName, list) ->
                             item {
                                 Surface(
                                     color = HextechCyan.copy(alpha = 0.15f),
                                     shape = RoundedCornerShape(4.dp),
                                     modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 4.dp)
                                 ) {
                                     Text(
                                         text = com.example.util.tr(categoryName),
                                         color = HextechCyan,
                                         fontWeight = FontWeight.Bold,
                                         fontSize = 11.sp,
                                         modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                     )
                                 }
                             }
                             items(list) { rune ->
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
                                         Text(com.example.util.tr(rune.name), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                         Text(com.example.util.tr("Categoría: ${rune.category}"), color = HextechGold, fontSize = 10.sp)
                                     }
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
                        text = com.example.util.tr(if (showSpellPickerForCore) "Seleccionar Hechizo Core" else "Seleccionar Hechizo Situacional"),
                        color = HextechGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = searchFilterQuery,
                        onValueChange = { searchFilterQuery = it },
                        placeholder = { Text(tr("Buscar hechizo..."), color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        singleLine = true
                    )
                    val filteredSpells = remember(searchFilterQuery, spells, showSpellPickerForCore, coreSpells, situationalSpells) {
                        val base = if (searchFilterQuery.isBlank()) spells else spells.filter { it.name.contains(searchFilterQuery, ignoreCase = true) }
                        if (showSpellPickerForCore) {
                            base.filter { spell -> coreSpells.none { it.name.equals(spell.name, ignoreCase = true) } }
                        } else {
                            base.filter { spell -> situationalSpells.none { it.name.equals(spell.name, ignoreCase = true) } }
                        }
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
                                    Text(com.example.util.tr(spell.name), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(com.example.util.tr("CD: ${spell.cooldown}"), color = HextechGold, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
