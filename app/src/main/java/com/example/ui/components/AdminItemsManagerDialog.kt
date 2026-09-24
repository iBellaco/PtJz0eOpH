package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.WildRiftItemsData
import com.example.data.WildRiftRepository
import com.example.data.local.WildRiftLocalCache
import com.example.model.WildRiftItem
import com.example.ui.theme.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminItemsManagerDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var itemsList by remember { mutableStateOf(WildRiftRepository.items.toList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("TODAS") }

    // Multi-selection state for batch moves
    var isMultiSelectMode by remember { mutableStateOf(false) }
    val selectedItemIds = remember { mutableStateListOf<String>() }

    // Dialogs state
    var showCreateCategoryDialog by remember { mutableStateOf(false) }
    var showRenameCategoryDialog by remember { mutableStateOf<String?>(null) }
    var showMoveSingleItemDialog by remember { mutableStateOf<WildRiftItem?>(null) }
    var showBatchMoveDialog by remember { mutableStateOf(false) }
    var showCreateItemDialog by remember { mutableStateOf(false) }
    var showEditItemDialog by remember { mutableStateOf<WildRiftItem?>(null) }
    var showDeleteItemConfirm by remember { mutableStateOf<WildRiftItem?>(null) }

    // Dynamic list of categories from items + standard categories
    val defaultCategories = listOf(
        "Objetos con Daños Físicos",
        "Objetos de Daño Mágico",
        "Objetos Defensivos",
        "Objetos de Apoyo",
        "Objetos de Hechizo Activos",
        "Botas Nivel 2",
        "Botas Nivel 3",
        "Objetos de Nivel Medio",
        "Artículos Básicos"
    )
    val customCategories = remember(itemsList) {
        val fromItems = itemsList.map { it.category.trim() }.filter { it.isNotBlank() }.distinct()
        val allCats = (defaultCategories + fromItems).distinct()
        allCats
    }

    // Helper to persist changes
    fun persistChanges(updated: List<WildRiftItem>) {
        itemsList = updated
        WildRiftRepository.items = updated
        WildRiftLocalCache.saveToLocalCache(context, items = updated)
    }

    // Export & copy JSON function
    fun copyAllItemsData() {
        try {
            val jsonFormatter = Json {
                prettyPrint = true
                encodeDefaults = true
                ignoreUnknownKeys = true
            }
            val jsonString = jsonFormatter.encodeToString(itemsList)
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Wild Rift Items Configuration", jsonString)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "¡Configuración de objetos copiada al portapapeles! (${itemsList.size} objetos)", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error al copiar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
    ) {
        BackHandler(enabled = true) {
            onDismiss()
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
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Brush.linearGradient(listOf(HextechGold, Color(0xFF8B6B23)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Category,
                                        contentDescription = null,
                                        tint = HextechDarkBg,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Gestor de Objetos y Secciones",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = HextechGold
                                    )
                                    Text(
                                        text = "${itemsList.size} objetos en ${customCategories.size} secciones",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
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
                                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Copy all configuration button
                            Button(
                                onClick = { copyAllItemsData() },
                                modifier = Modifier.weight(1.3f),
                                colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = HextechDarkBg, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copiar Configuración", color = HextechDarkBg, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                            }

                            // New category button
                            OutlinedButton(
                                onClick = { showCreateCategoryDialog = true },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.6f)),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = HextechCyan.copy(alpha = 0.12f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Nueva Sección", color = HextechCyan, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, maxLines = 1)
                            }

                            // New Item button
                            OutlinedButton(
                                onClick = { showCreateItemDialog = true },
                                modifier = Modifier.weight(0.9f),
                                border = BorderStroke(1.dp, Color(0xFF00FF66).copy(alpha = 0.6f)),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF00FF66).copy(alpha = 0.12f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF00FF66), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Crear Objeto", color = Color(0xFF00FF66), fontWeight = FontWeight.SemiBold, fontSize = 11.sp, maxLines = 1)
                            }
                        }

                        // Batch Move Toggle
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    isMultiSelectMode = !isMultiSelectMode
                                    if (!isMultiSelectMode) selectedItemIds.clear()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isMultiSelectMode) Icons.Default.CheckCircle else Icons.Default.Checklist,
                                    contentDescription = null,
                                    tint = if (isMultiSelectMode) HextechGold else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isMultiSelectMode) "Modo selección activo (${selectedItemIds.size})" else "Selección múltiple",
                                    color = if (isMultiSelectMode) HextechGold else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isMultiSelectMode) FontWeight.Bold else FontWeight.Normal
                                )
                            }

                            if (isMultiSelectMode && selectedItemIds.isNotEmpty()) {
                                Button(
                                    onClick = { showBatchMoveDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = HextechCyan),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.DriveFileMove, contentDescription = null, tint = HextechDarkBg, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mover (${selectedItemIds.size}) a...", color = HextechDarkBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Search & Filter Tabs
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Search box
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar objeto por nombre, stat o pasiva...", color = TextMuted, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HextechGold) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = TextMuted)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HextechGold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedContainerColor = HextechSurface,
                            unfocusedContainerColor = HextechSurface,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Horizontal Category Pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val pillOptions = listOf("TODAS") + customCategories
                        pillOptions.forEach { cat ->
                            val isSelected = selectedCategoryFilter.equals(cat, ignoreCase = true)
                            val count = if (cat == "TODAS") itemsList.size else itemsList.count { it.category.equals(cat, ignoreCase = true) }
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategoryFilter = cat },
                                label = { Text("$cat ($count)", fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HextechGold.copy(alpha = 0.25f),
                                    selectedLabelColor = HextechGold,
                                    containerColor = HextechSurface,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color.White.copy(alpha = 0.1f),
                                    selectedBorderColor = HextechGold
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                // Filter items according to search and category
                val filteredItems = remember(itemsList, searchQuery, selectedCategoryFilter) {
                    itemsList.filter { item ->
                        val matchesCat = selectedCategoryFilter == "TODAS" || item.category.equals(selectedCategoryFilter, ignoreCase = true)
                        val matchesSearch = searchQuery.isBlank() ||
                                item.name.contains(searchQuery, ignoreCase = true) ||
                                item.nameEn.contains(searchQuery, ignoreCase = true) ||
                                item.stats.contains(searchQuery, ignoreCase = true) ||
                                item.passive.contains(searchQuery, ignoreCase = true) ||
                                item.category.contains(searchQuery, ignoreCase = true)
                        matchesCat && matchesSearch
                    }
                }

                val groupedItems = remember(filteredItems, customCategories) {
                    val groups = filteredItems.groupBy { it.category }
                    val orderedList = mutableListOf<Pair<String, List<WildRiftItem>>>()
                    // Add existing categories that have matching items
                    customCategories.forEach { cat ->
                        val inCat = groups[cat]
                        if (!inCat.isNullOrEmpty()) {
                            orderedList.add(cat to inCat)
                        }
                    }
                    // Add any other categories not in customCategories list
                    groups.forEach { (cat, inCat) ->
                        if (orderedList.none { it.first.equals(cat, ignoreCase = true) }) {
                            orderedList.add(cat to inCat)
                        }
                    }
                    orderedList
                }

                // Items list
                if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No se encontraron objetos", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
                    ) {
                        groupedItems.forEach { (categoryName, itemsInCategory) ->
                            item(key = "header_$categoryName") {
                                Surface(
                                    color = HextechSurface,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(HextechGold)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = categoryName,
                                                color = HextechGold,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(${itemsInCategory.size})",
                                                color = TextMuted,
                                                fontSize = 11.sp
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            // Rename category button
                                            IconButton(
                                                onClick = { showRenameCategoryDialog = categoryName },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Renombrar sección", tint = HextechCyan, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            items(itemsInCategory, key = { it.id }) { item ->
                                val isSelected = selectedItemIds.contains(item.id)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isMultiSelectMode) {
                                                if (isSelected) selectedItemIds.remove(item.id) else selectedItemIds.add(item.id)
                                            } else {
                                                showEditItemDialog = item
                                            }
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) HextechGold.copy(alpha = 0.15f) else HextechSurface
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) HextechGold else Color.White.copy(alpha = 0.08f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isMultiSelectMode) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = { checked ->
                                                    if (checked) selectedItemIds.add(item.id) else selectedItemIds.remove(item.id)
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = HextechGold,
                                                    uncheckedColor = TextMuted,
                                                    checkmarkColor = HextechDarkBg
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }

                                        // Item Icon
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(1.dp, HextechGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                .background(HextechDarkBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AsyncImage(
                                                model = item.iconUrl,
                                                contentDescription = item.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // Item info
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = item.name,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary,
                                                    fontSize = 13.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (item.goldCost > 0) {
                                                    Surface(
                                                        color = Color(0xFF8B6B23).copy(alpha = 0.3f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "${item.goldCost}g",
                                                            color = HextechGold,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            if (item.stats.isNotBlank()) {
                                                Text(
                                                    text = item.stats,
                                                    color = TextSecondary,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            if (item.passive.isNotBlank()) {
                                                Text(
                                                    text = item.passive,
                                                    color = TextMuted,
                                                    fontSize = 10.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Action buttons
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Move to section button
                                            Button(
                                                onClick = { showMoveSingleItemDialog = item },
                                                colors = ButtonDefaults.buttonColors(containerColor = HextechCyan.copy(alpha = 0.2f)),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(Icons.Default.DriveFileMove, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Mover", color = HextechCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            }

                                            IconButton(
                                                onClick = { showEditItemDialog = item },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = TextSecondary, modifier = Modifier.size(16.dp))
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
    }

    // ==========================================
    // DIALOG: CREATE NEW CATEGORY
    // ==========================================
    if (showCreateCategoryDialog) {
        var newCategoryName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateCategoryDialog = false },
            title = { Text("Crear Nueva Sección de Objetos", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    Text("Ingresa el nombre de la nueva categoría (ej. 'Objetos de Letalidad', 'Objetos de Asesino', etc.):", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nombre de la sección...", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HextechGold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedContainerColor = HextechSurface,
                            unfocusedContainerColor = HextechSurface,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanName = newCategoryName.trim()
                        if (cleanName.isNotBlank()) {
                            selectedCategoryFilter = cleanName
                            showCreateCategoryDialog = false
                            Toast.makeText(context, "Sección '$cleanName' lista para recibir objetos.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold)
                ) {
                    Text("Crear Sección", color = HextechDarkBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateCategoryDialog = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            },
            containerColor = HextechSurface
        )
    }

    // ==========================================
    // DIALOG: RENAME CATEGORY
    // ==========================================
    showRenameCategoryDialog?.let { currentCategory ->
        var updatedCategoryName by remember { mutableStateOf(currentCategory) }
        AlertDialog(
            onDismissRequest = { showRenameCategoryDialog = null },
            title = { Text("Renombrar Sección", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    Text("Cambiar el nombre de '$currentCategory' para todos los objetos incluidos en ella:", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = updatedCategoryName,
                        onValueChange = { updatedCategoryName = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HextechGold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedContainerColor = HextechSurface,
                            unfocusedContainerColor = HextechSurface,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = updatedCategoryName.trim()
                        if (clean.isNotBlank() && clean != currentCategory) {
                            val updated = itemsList.map { item ->
                                if (item.category.equals(currentCategory, ignoreCase = true)) {
                                    item.copy(category = clean)
                                } else {
                                    item
                                }
                            }
                            persistChanges(updated)
                            if (selectedCategoryFilter.equals(currentCategory, ignoreCase = true)) {
                                selectedCategoryFilter = clean
                            }
                            showRenameCategoryDialog = null
                            Toast.makeText(context, "Sección renombrada a '$clean'.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold)
                ) {
                    Text("Guardar", color = HextechDarkBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameCategoryDialog = null }) {
                    Text("Cancelar", color = TextMuted)
                }
            },
            containerColor = HextechSurface
        )
    }

    // ==========================================
    // DIALOG: MOVE SINGLE ITEM
    // ==========================================
    showMoveSingleItemDialog?.let { targetItem ->
        var chosenCategory by remember { mutableStateOf(targetItem.category) }
        var newCategoryInput by remember { mutableStateOf("") }
        var isCustomCategory by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showMoveSingleItemDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = targetItem.iconUrl,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mover ${targetItem.name}", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Selecciona la sección de destino para este objeto:", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    customCategories.forEach { cat ->
                        val isSelected = !isCustomCategory && chosenCategory.equals(cat, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) HextechGold.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable {
                                    chosenCategory = cat
                                    isCustomCategory = false
                                }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    chosenCategory = cat
                                    isCustomCategory = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = HextechGold, unselectedColor = TextMuted)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(cat, color = if (isSelected) HextechGold else TextPrimary, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Or create new inline
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { isCustomCategory = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isCustomCategory,
                            onClick = { isCustomCategory = true },
                            colors = RadioButtonDefaults.colors(selectedColor = HextechCyan, unselectedColor = TextMuted)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Crear y mover a nueva sección...", color = if (isCustomCategory) HextechCyan else TextSecondary, fontSize = 12.sp)
                    }

                    if (isCustomCategory) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = newCategoryInput,
                            onValueChange = { newCategoryInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Nombre de nueva sección...", color = TextMuted, fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HextechCyan,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedContainerColor = HextechDarkBg,
                                unfocusedContainerColor = HextechDarkBg,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val destinationCategory = if (isCustomCategory) newCategoryInput.trim() else chosenCategory.trim()
                        if (destinationCategory.isNotBlank()) {
                            val updated = itemsList.map {
                                if (it.id == targetItem.id) it.copy(category = destinationCategory) else it
                            }
                            persistChanges(updated)
                            showMoveSingleItemDialog = null
                            Toast.makeText(context, "'${targetItem.name}' movido a '$destinationCategory'", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold)
                ) {
                    Text("Confirmar Cambio", color = HextechDarkBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMoveSingleItemDialog = null }) {
                    Text("Cancelar", color = TextMuted)
                }
            },
            containerColor = HextechSurface
        )
    }

    // ==========================================
    // DIALOG: BATCH MOVE MULTIPLE ITEMS
    // ==========================================
    if (showBatchMoveDialog) {
        var chosenBatchCategory by remember { mutableStateOf(customCategories.firstOrNull() ?: "Objetos con Daños Físicos") }
        var customBatchCategory by remember { mutableStateOf("") }
        var isCustomBatch by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showBatchMoveDialog = false },
            title = {
                Text("Mover ${selectedItemIds.size} objetos seleccionados", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Selecciona la categoría a la que deseas reubicar todos los objetos seleccionados:", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    customCategories.forEach { cat ->
                        val isSelected = !isCustomBatch && chosenBatchCategory.equals(cat, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) HextechGold.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable {
                                    chosenBatchCategory = cat
                                    isCustomBatch = false
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    chosenBatchCategory = cat
                                    isCustomBatch = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = HextechGold, unselectedColor = TextMuted)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(cat, color = if (isSelected) HextechGold else TextPrimary, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { isCustomBatch = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isCustomBatch,
                            onClick = { isCustomBatch = true },
                            colors = RadioButtonDefaults.colors(selectedColor = HextechCyan, unselectedColor = TextMuted)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mover a nueva sección...", color = if (isCustomBatch) HextechCyan else TextSecondary, fontSize = 12.sp)
                    }

                    if (isCustomBatch) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = customBatchCategory,
                            onValueChange = { customBatchCategory = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Nombre de nueva sección...", color = TextMuted, fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HextechCyan,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedContainerColor = HextechDarkBg,
                                unfocusedContainerColor = HextechDarkBg,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val destination = if (isCustomBatch) customBatchCategory.trim() else chosenBatchCategory.trim()
                        if (destination.isNotBlank()) {
                            val updated = itemsList.map {
                                if (selectedItemIds.contains(it.id)) it.copy(category = destination) else it
                            }
                            persistChanges(updated)
                            selectedItemIds.clear()
                            isMultiSelectMode = false
                            showBatchMoveDialog = false
                            Toast.makeText(context, "Se movieron los objetos a '$destination'", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold)
                ) {
                    Text("Mover Seleccionados", color = HextechDarkBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchMoveDialog = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            },
            containerColor = HextechSurface
        )
    }

    // ==========================================
    // DIALOG: CREATE OR EDIT ITEM
    // ==========================================
    if (showCreateItemDialog || showEditItemDialog != null) {
        val isEditing = showEditItemDialog != null
        val initialItem = showEditItemDialog

        var id by remember { mutableStateOf(initialItem?.id ?: "") }
        var name by remember { mutableStateOf(initialItem?.name ?: "") }
        var nameEn by remember { mutableStateOf(initialItem?.nameEn ?: "") }
        var category by remember { mutableStateOf(initialItem?.category ?: (customCategories.firstOrNull() ?: "Objetos con Daños Físicos")) }
        var goldCostText by remember { mutableStateOf(initialItem?.goldCost?.toString() ?: "3000") }
        var stats by remember { mutableStateOf(initialItem?.stats ?: "") }
        var statsEn by remember { mutableStateOf(initialItem?.statsEn ?: "") }
        var passive by remember { mutableStateOf(initialItem?.passive ?: "") }
        var passiveEn by remember { mutableStateOf(initialItem?.passiveEn ?: "") }
        var coachTip by remember { mutableStateOf(initialItem?.coachTip ?: "") }
        var iconUrl by remember { mutableStateOf(initialItem?.iconUrl ?: "") }

        AlertDialog(
            onDismissRequest = {
                showCreateItemDialog = false
                showEditItemDialog = null
            },
            title = {
                Text(
                    text = if (isEditing) "Editar Objeto: ${initialItem?.name}" else "Crear Nuevo Objeto",
                    color = HextechGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!isEditing) {
                        OutlinedTextField(
                            value = id,
                            onValueChange = { id = it },
                            label = { Text("ID único (ej. fiendhunter_bolts)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre (Español)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = nameEn,
                        onValueChange = { nameEn = it },
                        label = { Text("Nombre (Inglés)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Categoría / Sección") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = goldCostText,
                        onValueChange = { goldCostText = it },
                        label = { Text("Costo en Oro (g)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = iconUrl,
                        onValueChange = { iconUrl = it },
                        label = { Text("URL de la Imagen / Icono") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = stats,
                        onValueChange = { stats = it },
                        label = { Text("Estadísticas (Español)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = passive,
                        onValueChange = { passive = it },
                        label = { Text("Pasiva / Efecto (Español)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    OutlinedTextField(
                        value = coachTip,
                        onValueChange = { coachTip = it },
                        label = { Text("Consejo del Coach") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanName = name.trim()
                        val cleanCategory = category.trim()
                        val cleanId = if (isEditing) initialItem!!.id else if (id.isNotBlank()) id.trim() else cleanName.lowercase().replace(" ", "_")
                        val cost = goldCostText.toIntOrNull() ?: 3000

                        if (cleanName.isNotBlank() && cleanCategory.isNotBlank()) {
                            val newItem = WildRiftItem(
                                id = cleanId,
                                name = cleanName,
                                nameEn = nameEn.trim(),
                                category = cleanCategory,
                                goldCost = cost,
                                stats = stats.trim(),
                                statsEn = statsEn.trim(),
                                passive = passive.trim(),
                                passiveEn = passiveEn.trim(),
                                coachTip = coachTip.trim(),
                                iconUrl = iconUrl.trim()
                            )

                            val updated = if (isEditing) {
                                itemsList.map { if (it.id == initialItem!!.id) newItem else it }
                            } else {
                                listOf(newItem) + itemsList.filter { it.id != cleanId }
                            }

                            persistChanges(updated)
                            showCreateItemDialog = false
                            showEditItemDialog = null
                            Toast.makeText(context, "Objeto '$cleanName' guardado correctamente.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold)
                ) {
                    Text(if (isEditing) "Guardar Cambios" else "Crear Objeto", color = HextechDarkBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreateItemDialog = false
                    showEditItemDialog = null
                }) {
                    Text("Cancelar", color = TextMuted)
                }
            },
            containerColor = HextechSurface
        )
    }
}
