package com.example.ui.screens

import com.example.ui.components.CoachFilterChip as FilterChip
import com.example.ui.components.CoachIconButton as IconButton
import com.example.data.WildRiftItemsData
import com.example.utils.parseHtmlColorToAnnotatedString
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.ui.components.coachClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.util.tr
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.ui.components.FormattedWildRiftText
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WildRiftRepository
import com.example.model.WildRiftItem
import com.example.ui.components.AppAssetImage
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechGoldLight
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.HextechSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ItemsCatalogTab() {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedLevel by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isGridView by remember { mutableStateOf(true) }
    var showFilterChips by remember { mutableStateOf(true) }
    var itemForDetail by remember { mutableStateOf<WildRiftItem?>(null) }

    val allItems = WildRiftRepository.items

    val allCategories = WildRiftItemsData.officialCategoryOrder

    val filterOptions = remember(allCategories) {
        val options = mutableListOf("TODOS" to "Todos")
        allCategories.forEach { cat ->
            options.add(cat to cat)
        }
        options
    }

    val lang = com.example.util.currentAppLanguage()

    fun normalizeSearch(text: String): String {
        return java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
            .trim()
    }

    fun itemMatchesQuery(item: WildRiftItem, query: String): Boolean {
        if (query.isBlank()) return true
        val qNorm = normalizeSearch(query)
        return normalizeSearch(item.getLocalizedName(lang)).contains(qNorm) ||
               normalizeSearch(item.name).contains(qNorm) ||
               normalizeSearch(item.nameEn).contains(qNorm) ||
               normalizeSearch(item.namePt).contains(qNorm) ||
               normalizeSearch(item.id).contains(qNorm) ||
               normalizeSearch(item.category).contains(qNorm) ||
               normalizeSearch(item.getLocalizedStats(lang)).contains(qNorm) ||
               normalizeSearch(item.getLocalizedPassive(lang)).contains(qNorm) ||
               normalizeSearch(item.getLocalizedCoachTip(lang)).contains(qNorm)
    }

    val treeCategories = remember(selectedCategory, selectedLevel, searchQuery, lang) {
        val result = mutableListOf<WildRiftItemsData.CatalogGroup>()
        val catsToProcess = if (selectedCategory != null) listOf(selectedCategory!!) else allCategories
        val processedItemIds = mutableSetOf<String>()
        catsToProcess.forEach { category ->
            WildRiftItemsData.getCatalogGroups(category).filter { selectedLevel == null || it.level == selectedLevel }.forEach { group ->
                val matching = group.items.filter { itemMatchesQuery(it, searchQuery) }
                if (matching.isNotEmpty()) {
                    result += group.copy(items = matching)
                    processedItemIds.addAll(matching.map { it.id })
                }
            }
        }
        if (selectedCategory == null && searchQuery.isNotBlank() && (selectedLevel == null || selectedLevel == "Completos")) {
            val remaining = WildRiftItemsData.list.filter { it.id !in processedItemIds && itemMatchesQuery(it, searchQuery) }
            if (remaining.isNotEmpty()) result += WildRiftItemsData.CatalogGroup("Otros Objetos", "Completos", remaining)
        }
        result
    }
    val filteredItems = remember(treeCategories) { treeCategories.flatMap { it.items }.distinctBy { it.id } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Estado de estadísticas del servidor global
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HextechSurfaceVariant, RoundedCornerShape(8.dp))
                .border(0.5.dp, HextechGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = com.example.util.tr("${filteredItems.size} ${tr("Objetos Oficiales")}"),
                color = HextechCyan,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                // View mode toggle
                Row(
                    modifier = Modifier
                        .background(HextechSurface, RoundedCornerShape(6.dp))
                        .border(0.5.dp, HextechCardBorder, RoundedCornerShape(6.dp))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isGridView) HextechCyan else Color.Transparent)
                            .coachClickable { isGridView = true }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = tr("Cuadrícula"),
                            color = if (isGridView) HextechDarkBg else TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (!isGridView) HextechCyan else Color.Transparent)
                            .coachClickable { isGridView = false }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = tr("Detallado"),
                            color = if (!isGridView) HextechDarkBg else TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(tr("Buscar objeto por nombre o estadísticas..."), color = TextMuted, fontSize = 12.5.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HextechCyan) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = tr("Cerrar"), tint = TextMuted)
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HextechCyan,
                unfocusedBorderColor = HextechCardBorder,
                focusedContainerColor = HextechSurface,
                unfocusedContainerColor = HextechSurface
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf<String?>(null, "Completos", "Nivel Medio", "Básico", "Inicial").forEachIndexed { index, level ->
                FilterChip(selected = selectedLevel == level, onClick = { selectedLevel = level },
                    modifier = Modifier.testTag("catalog_level_$index"),
                    label = { Text(tr(level ?: "Todos"), fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = HextechGold, selectedLabelColor = HextechDarkBg))
            }
        }

        // Collapsible Header for Item Categories
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .coachClickable { showFilterChips = !showFilterChips }
                .padding(vertical = 4.dp, horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = HextechCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = tr("Filtrar por Categoría"),
                    color = HextechCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                if (!showFilterChips) {
                    Spacer(modifier = Modifier.width(6.dp))
                    val catLabel = selectedCategory ?: "Todos"
                    Surface(
                        color = HextechGold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, HextechGold.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = tr(catLabel),
                            color = HextechGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            IconButton(
                onClick = { showFilterChips = !showFilterChips },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (showFilterChips) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = com.example.util.trNullable(if (showFilterChips) tr("Minimizar filtros") else tr("Expandir filtros")),
                    tint = HextechGold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = showFilterChips,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            // Category Filter Chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                filterOptions.forEach { (key, label) ->
                    val count = (if (key == "TODOS") allCategories.flatMap { WildRiftItemsData.getCatalogGroups(it) } else WildRiftItemsData.getCatalogGroups(key))
                        .filter { selectedLevel == null || it.level == selectedLevel }.flatMap { it.items }.distinctBy { it.id }.size
                    val isSelected = (selectedCategory == null && key == "TODOS") || (selectedCategory != null && selectedCategory.equals(key, ignoreCase = true))
                    FilterChip(
                        selected = isSelected,
                        modifier = Modifier.testTag("catalog_section_$key"),
                        onClick = { selectedCategory = if (key == "TODOS") null else key },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tr(label), fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = com.example.util.tr("($count)"),
                                    color = if (isSelected) HextechDarkBg else HextechGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HextechCyan,
                            selectedLabelColor = HextechDarkBg
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            treeCategories.groupBy { it.section }.forEach { (categoryName, groups) ->
                item(key = "item_cat_$categoryName") {
                    val catColor = when {
                        categoryName.contains("Luchador", ignoreCase = true) -> Color(0xFFFF8C00)
                        categoryName.contains("Asesino", ignoreCase = true) -> Color(0xFFEF4444)
                        categoryName.contains("Tirador", ignoreCase = true) -> Color(0xFFF59E0B)
                        categoryName.contains("Mágico", ignoreCase = true) || categoryName.contains("Magico", ignoreCase = true) -> Color(0xFF60A5FA)
                        categoryName.contains("Defensa", ignoreCase = true) -> Color(0xFF4ADE80)
                        categoryName.contains("Apoyo", ignoreCase = true) -> Color(0xFFE879F9)
                        categoryName.contains("Bota", ignoreCase = true) -> Color(0xFF38BDF8)
                        else -> HextechCyan
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("catalog_panel_$categoryName"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechSurface.copy(alpha = 0.95f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, catColor.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 10.dp)
                            ) {
                                Text(
                                    text = tr(categoryName).uppercase(),
                                    color = catColor,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.5.sp,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = com.example.util.tr("(${groups.sumOf { it.items.size }})"),
                                    color = catColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            groups.forEachIndexed { groupIndex, (_, itemLevel, itemsInCat) ->
                                if (groupIndex > 0) Spacer(Modifier.height(16.dp))
                                Text(tr(itemLevel).uppercase(), color = HextechCyan, fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp, modifier = Modifier.padding(bottom = 8.dp).testTag("catalog_group_${categoryName}_${itemLevel}"))
                                if (isGridView) {
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                        itemsInCat.forEach { item ->
                                            ItemGridCard(item = item, onClick = { itemForDetail = item }, modifier = Modifier.width(68.dp), borderColor = catColor)
                                        }
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        itemsInCat.forEach { item -> ItemListCard(item = item, onClick = { itemForDetail = item }, borderColor = catColor) }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Item Detail Modal Dialog
    selectedRuneItemModal(itemForDetail) { itemForDetail = null }
}

@Composable
internal fun selectedRuneItemModal(
    item: WildRiftItem?,
    onDismiss: () -> Unit
) {
    item?.let { itm ->
        val lang = com.example.util.currentAppLanguage()
        val localizedName = itm.getLocalizedName(lang)
        val statsList = itm.getStatsList(lang)
        val localizedPassive = itm.getLocalizedPassive(lang)
        val localizedCoachTip = itm.getLocalizedCoachTip(lang)

        androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HextechSurface),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, HextechGold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppAssetImage(
                        url = itm.iconUrl,
                        contentDescription = localizedName,
                        fallbackText = localizedName,
                        modifier = Modifier.size(72.dp),
                        borderColor = HextechGold,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = com.example.util.tr(localizedName),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(HextechCyan.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = tr(itm.category),
                                color = HextechCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (!itm.isEvolution && itm.goldCost > 0) {
                            Box(
                                modifier = Modifier
                                    .background(HextechGold.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = com.example.util.tr(" ${itm.goldCost} ${tr("Oro")}"),
                                    color = HextechGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (itm.isEvolution) {
                        Spacer(Modifier.height(6.dp))
                        ItemEvolutionLabel(itm)
                    }

                    if (statsList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = tr("Estadísticas:"),
                            color = HextechGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            statsList.forEach { stat ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(HextechCyan, CircleShape)
                                    )
                                    Text(
                                        text = com.example.util.tr(stat.parseHtmlColorToAnnotatedString()),
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    if (localizedPassive.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = tr("Efecto / Pasiva:"),
                            color = HextechGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FormattedWildRiftText(
                            text = localizedPassive,
                            color = TextPrimary.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (localizedCoachTip.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = HextechGold.copy(alpha = 0.08f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HextechGold.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(com.example.util.tr(""), fontSize = 13.sp)
                                    Text(
                                        text = tr("Consejos del Coach:"),
                                        color = HextechGoldLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = com.example.util.tr(localizedCoachTip),
                                    color = TextPrimary.copy(alpha = 0.95f),
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.5.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(HextechCyan)
                            .coachClickable { onDismiss() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(tr("Cerrar"), color = HextechDarkBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemGridCard(
    item: WildRiftItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = item.getThemeColor()
) {
    val lang = com.example.util.currentAppLanguage()
    val localizedName = item.getLocalizedName(lang)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .coachClickable(onClick = onClick)
            .background(HextechSurface.copy(alpha = 0.6f))
            .border(0.5.dp, HextechCardBorder.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(6.dp)
    ) {
        AppAssetImage(
            url = item.iconUrl,
            contentDescription = localizedName,
            fallbackText = localizedName,
            modifier = Modifier.size(42.dp),
            borderColor = borderColor,
            shape = RoundedCornerShape(8.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = com.example.util.tr(localizedName),
            color = TextPrimary,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 11.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        if (item.isEvolution || item.goldCost <= 0) {
            Text(
                text = tr("Evolución"),
                color = HextechCyan,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            Text(
                text = com.example.util.tr("${item.goldCost} G"),
                color = HextechGold,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ItemListCard(
    item: WildRiftItem,
    onClick: () -> Unit,
    borderColor: Color = item.getThemeColor()
) {
    val lang = com.example.util.currentAppLanguage()
    val localizedName = item.getLocalizedName(lang)
    val statsList = item.getStatsList(lang)
    val localizedPassive = item.getLocalizedPassive(lang)
    val localizedCoachTip = item.getLocalizedCoachTip(lang)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .coachClickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            AppAssetImage(
                url = item.iconUrl,
                contentDescription = localizedName,
                fallbackText = localizedName,
                modifier = Modifier.size(44.dp),
                borderColor = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(com.example.util.tr(localizedName), modifier = Modifier.weight(1f).padding(end = 6.dp), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    if (!item.isEvolution && item.goldCost > 0) {
                        Text(com.example.util.tr(" ${item.goldCost} G"), color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
                if (item.isEvolution) {
                    Spacer(Modifier.height(4.dp))
                    ItemEvolutionLabel(item)
                }
                Text(tr(item.category), color = HextechCyan, fontSize = 10.5.sp)
                if (statsList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        statsList.forEach { stat ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(HextechCyan, CircleShape)
                                )
                                Text(
                                    text = com.example.util.tr(stat.parseHtmlColorToAnnotatedString()),
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                if (localizedPassive.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    FormattedWildRiftText(
                        text = localizedPassive,
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (localizedCoachTip.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(com.example.util.tr(""), fontSize = 10.sp)
                        Text(
                            text = com.example.util.tr(localizedCoachTip),
                            color = HextechGoldLight.copy(alpha = 0.9f),
                            fontSize = 10.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ====================================================================
// TAB 4: CATÁLOGO EXCLUSIVO DE RUNAS (BÚSQUEDA Y RAMAS)
// ====================================================================
// TAB 4: CATÁLOGO EXCLUSIVO DE RUNAS
// ====================================================================

@Composable
private fun ItemEvolutionLabel(item: WildRiftItem) {
    val language = com.example.util.currentAppLanguage()
    val sourceName = WildRiftItemsData.getItemByName(item.evolvesFrom)?.getLocalizedName(language)
        ?: tr(item.evolvesFrom)
    Surface(
        color = HextechCyan.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.5.dp, HextechCyan.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
            Text(
                text = if (sourceName.isBlank()) tr("Evolución (Sin coste de oro)") else tr("Evolución de"),
                color = HextechCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
            if (sourceName.isNotBlank()) {
                Text(sourceName, color = TextPrimary, fontSize = 11.sp, lineHeight = 15.sp)
            }
        }
    }
}
