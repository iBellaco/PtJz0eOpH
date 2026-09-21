package com.example.ui.components

import com.example.utils.parseHtmlColorToAnnotatedString

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WildRiftRepository
import com.example.model.Champion
import com.example.model.LaneRole
import com.example.model.DraftAnalysisResult
import com.example.util.tr
import com.example.util.LocalLanguage
import com.example.ui.theme.*
import kotlin.math.roundToInt

enum class OverlayTab {
    DRAFT, ITEMS, RUNES, SPELLS, OBJECTIVES
}

@Composable
fun FloatingAssistantOverlay(
    isVisible: Boolean,
    onClose: () -> Unit,
    initialRole: LaneRole? = null,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    var isExpanded by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(100f) }
    
    var activeTab by remember { mutableStateOf(OverlayTab.DRAFT) }
    
    // Draft state
    var activeRole by remember { mutableStateOf(initialRole ?: LaneRole.MID) }
    var draftSearchQuery by remember { mutableStateOf("") }
    var lockedChampion by remember { mutableStateOf<Champion?>(null) }
    
    // Items state
    var itemSearchQuery by remember { mutableStateOf("") }
    var itemSelectedCategory by remember { mutableStateOf<String?>(null) }

    // Runes state
    var runesSearchQuery by remember { mutableStateOf("") }

    // Spells state
    var spellsSearchQuery by remember { mutableStateOf("") }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
        ) {
            if (!isExpanded) {
                // Minimized bubble
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(HextechDarkBg)
                        .border(2.dp, HextechGold, CircleShape)
                        .clickable { isExpanded = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Open Assistant", tint = HextechCyan)
                }
            } else {
                // Expanded panel
                Card(
                    modifier = Modifier
                        .width(340.dp)
                        .height(500.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                    border = BorderStroke(1.dp, HextechGold)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(HextechSurface)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Wild Rift Assistant", color = HextechGold, fontWeight = FontWeight.Bold)
                            Row {
                                IconButton(onClick = { isExpanded = false }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minimize", tint = HextechCyan)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = DangerRed)
                                }
                            }
                        }
                        
                        // Tabs
                        ScrollableTabRow(
                            selectedTabIndex = activeTab.ordinal,
                            containerColor = HextechSurface,
                            contentColor = HextechGold,
                            edgePadding = 8.dp
                        ) {
                            OverlayTab.entries.forEach { tab ->
                                Tab(
                                    selected = activeTab == tab,
                                    onClick = { activeTab = tab },
                                    text = { Text(tr(tab.name), fontSize = 11.sp, fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Normal) }
                                )
                            }
                        }
                        
                        // Content
                        Box(modifier = Modifier.weight(1f).padding(8.dp)) {
                            when (activeTab) {
                                OverlayTab.DRAFT -> OverlayDraftTabContent(
                                    activeRole = activeRole,
                                    onRoleSelect = { activeRole = it },
                                    isFirstPick = false,
                                    onFirstPickChange = {},
                                    analysis = DraftAnalysisResult(0,0,0,0,0,0,null,"",null,null,false,null,emptyList()),
                                    onSelectChampion = { lockedChampion = it },
                                    onClearChampion = { lockedChampion = null }
                                )
                                OverlayTab.ITEMS -> OverlayItemsTabContent(
                                    searchQuery = itemSearchQuery,
                                    onSearchChange = { itemSearchQuery = it },
                                    selectedCategory = itemSelectedCategory,
                                    onCategoryChange = { itemSelectedCategory = it }
                                )
                                OverlayTab.RUNES -> OverlayRunesTabContent(
                                    lockedChampion = lockedChampion,
                                    searchQuery = runesSearchQuery,
                                    onSearchChange = { runesSearchQuery = it },
                                    onSelectChampion = { lockedChampion = it },
                                    onClearChampion = { lockedChampion = null }
                                )
                                OverlayTab.SPELLS -> OverlaySpellsTabContent(
                                    lockedChampion = lockedChampion,
                                    searchQuery = spellsSearchQuery,
                                    onSearchChange = { spellsSearchQuery = it },
                                    onSelectChampion = { lockedChampion = it },
                                    onClearChampion = { lockedChampion = null }
                                )
                                OverlayTab.OBJECTIVES -> OverlayObjectivesTabContent()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverlayDraftTabContent(
    activeRole: LaneRole,
    onRoleSelect: (LaneRole) -> Unit,
    isFirstPick: Boolean,
    onFirstPickChange: (Boolean) -> Unit,
    analysis: DraftAnalysisResult,
    onSelectChampion: (Champion) -> Unit,
    onClearChampion: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Draft Analysis (Coming Soon...)", color = HextechCyan)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OverlayItemsTabContent(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: String?,
    onCategoryChange: (String?) -> Unit
) {
    val lang = LocalLanguage.current
    val filteredItems = remember(searchQuery, selectedCategory, lang) {
        WildRiftRepository.items.filter { item ->
            val matchCategory = selectedCategory == null || item.category.equals(selectedCategory, ignoreCase=true)
            val matchQuery = searchQuery.isBlank() ||
                    item.getLocalizedName(lang).contains(searchQuery, ignoreCase = true) ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.nameEn.contains(searchQuery, ignoreCase = true) ||
                    item.namePt.contains(searchQuery, ignoreCase = true) ||
                    item.getLocalizedPassive(lang).contains(searchQuery, ignoreCase = true) ||
                    item.getLocalizedStats(lang).contains(searchQuery, ignoreCase = true)
            matchCategory && matchQuery
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text(tr("Buscar objeto..."), fontSize = 11.sp) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HextechCyan,
                unfocusedBorderColor = HextechCardBorder
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val dynamicCats = WildRiftRepository.items.map { it.category }.distinct()
            dynamicCats.forEach { cat ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) HextechCyan else HextechSurface)
                        .clickable { onCategoryChange(if (isSelected) null else cat) }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = tr(cat),
                        color = if (isSelected) HextechDarkBg else TextMuted,
                        fontSize = 9.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filteredItems) { item ->
                val localizedName = item.getLocalizedName(lang)
                val localizedStats = item.getLocalizedStats(lang)
                val localizedPassive = item.getLocalizedPassive(lang)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                    border = BorderStroke(1.dp, HextechCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        AppAssetImage(
                            url = item.iconUrl,
                            contentDescription = localizedName,
                            fallbackText = localizedName,
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(6.dp)).border(1.dp, HextechGold, RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(localizedName, color = HextechGoldLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("${item.goldCost} ${tr("Oro")}", color = HextechGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            if (localizedStats.isNotBlank()) {
                                Text(localizedStats.parseHtmlColorToAnnotatedString(), color = HextechCyan, fontSize = 10.sp)
                            }
                            if (localizedPassive.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(localizedPassive, color = TextPrimary.copy(alpha = 0.85f), fontSize = 10.sp, lineHeight = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverlayObjectivesTabContent() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Objectives Tab", color = HextechCyan)
    }
}

// ====================================================================
// OVERLAY SUB-TAB 4: RUNAS META (EXCLUSIVO RUNAS)
// ====================================================================
@Composable
private fun OverlayRunesTabContent(
    lockedChampion: Champion?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSelectChampion: (Champion) -> Unit,
    onClearChampion: () -> Unit
) {
    if (lockedChampion == null) {
        val matchingChampions = remember(searchQuery, WildRiftRepository.champions.toList()) {
            WildRiftRepository.champions.filter { champ ->
                searchQuery.isBlank() || champ.name.contains(searchQuery, ignoreCase = true)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(HextechSurface)
                    .border(1.dp, HextechCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tr("Selecciona un campeón para ver su página de runas óptima"),
                    color = HextechCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(tr("Buscar campeón fijado..."), color = TextMuted, fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(16.dp)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HextechCyan,
                    unfocusedBorderColor = HextechCardBorder,
                    focusedContainerColor = HextechSurface,
                    unfocusedContainerColor = HextechSurface
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(matchingChampions) { champ ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(HextechSurface)
                            .clickable { onSelectChampion(champ) }
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ChampionAvatar(champion = champ, size = 32.dp, showTierBadge = false)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(champ.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("${tr(champ.primaryRole.shortName)}", color = HextechGold, fontSize = 10.sp)
                            }
                        }
                        Icon(Icons.Default.Check, contentDescription = "Seleccionar", tint = HextechCyan, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Selected Champion Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(HextechSurface)
                    .border(1.dp, HextechCyan, RoundedCornerShape(10.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ChampionAvatar(champion = lockedChampion, size = 36.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${lockedChampion.name} (${tr("Fijado")})",
                            color = HextechCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${tr(lockedChampion.primaryRole.displayName)} • ${tr("Página de Runas")}",
                            color = HextechGoldLight,
                            fontSize = 10.sp
                        )
                    }
                }

                IconButton(
                    onClick = onClearChampion,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cambiar campeón", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Runes Breakdown Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = HextechSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, HextechCyan.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = " " + tr("Runa Clave Recomendada"),
                        color = HextechCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = lockedChampion.recommendedRunes,
                        color = HextechGoldLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                    if (lockedChampion.runeTreeDetails.isNotBlank()) {
                        val parsedRunes = lockedChampion.runeTreeDetails
                            .replace(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+:\\s*"), "")
                            .split("•")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                        
                        if (parsedRunes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            @OptIn(ExperimentalLayoutApi::class)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                parsedRunes.forEach { rName ->
                                    val allRunes = com.example.data.WildRiftSpellsAndRunes.runes
                                    val foundRune = allRunes.find { r -> r.name.equals(rName, ignoreCase = true) || rName.contains(r.name) }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (foundRune != null) {
                                            AppAssetImage(
                                                url = foundRune.iconUrl,
                                                contentDescription = foundRune.name,
                                                fallbackText = "",
                                                modifier = Modifier.size(18.dp),
                                                shape = CircleShape
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        } else {
                                            Box(modifier = Modifier.size(4.dp).background(HextechCyan, CircleShape))
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(rName, color = TextPrimary.copy(alpha = 0.9f), fontSize = 11.sp)
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = lockedChampion.runeTreeDetails,
                                color = TextPrimary.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ====================================================================
// OVERLAY SUB-TAB 5: HECHIZOS DE INVOCADOR & HABILIDADES (EXCLUSIVO)
// ====================================================================
@Composable
private fun OverlaySpellsTabContent(
    lockedChampion: Champion?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSelectChampion: (Champion) -> Unit,
    onClearChampion: () -> Unit
) {
    if (lockedChampion == null) {
        val matchingChampions = remember(searchQuery, WildRiftRepository.champions.toList()) {
            WildRiftRepository.champions.filter { champ ->
                searchQuery.isBlank() || champ.name.contains(searchQuery, ignoreCase = true)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(HextechSurface)
                    .border(1.dp, HextechGold.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tr("Selecciona un campeón para ver sus hechizos de invocador recomendados"),
                    color = HextechGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(tr("Buscar campeón..."), color = TextMuted, fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HextechGold, modifier = Modifier.size(16.dp)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HextechGold,
                    unfocusedBorderColor = HextechCardBorder,
                    focusedContainerColor = HextechSurface,
                    unfocusedContainerColor = HextechSurface
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(matchingChampions) { champ ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(HextechSurface)
                            .clickable { onSelectChampion(champ) }
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ChampionAvatar(champion = champ, size = 32.dp, showTierBadge = false)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(champ.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("${tr(champ.primaryRole.shortName)} • ${tr("Hechizos")}: ${champ.recommendedSpells.joinToString("+")}", color = HextechGold, fontSize = 10.sp)
                            }
                        }
                        Icon(Icons.Default.Check, contentDescription = "Seleccionar", tint = HextechGold, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Selected Champion Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(HextechSurface)
                    .border(1.dp, HextechGold, RoundedCornerShape(10.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ChampionAvatar(champion = lockedChampion, size = 36.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${lockedChampion.name} (${tr("Fijado")})",
                            color = HextechGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${tr(lockedChampion.primaryRole.displayName)} • ${tr("Hechizos & Orden de Habilidades")}",
                            color = HextechCyan,
                            fontSize = 10.sp
                        )
                    }
                }

                IconButton(
                    onClick = onClearChampion,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cambiar campeón", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Recommended Spells & Skill Order Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = HextechSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, HextechGold.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = " " + tr("Hechizos de Invocador Recomendados"),
                        color = HextechGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        lockedChampion.recommendedSpells.forEach { spell ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(HextechSurfaceVariant)
                                    .border(1.dp, HextechGold, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = tr(spell),
                                    color = HextechGoldLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = HextechCardBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = " " + tr("Prioridad de Habilidades"),
                        color = HextechCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tr("Maxeo:") + " ${lockedChampion.skillOrder}",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
