package com.example.ui.screens

import com.example.ui.components.CoachFilterChip as FilterChip
import com.example.ui.components.CoachIconButton as IconButton
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import android.widget.Toast
import com.example.data.local.FavoriteChampionsManager
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.example.util.tr
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.ui.components.SparklineTrendGraph
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.WildRiftRepository
import com.example.data.sync.GlobalMetaSyncService
import com.example.model.Champion
import com.example.util.SubscriptionManager
import com.example.model.LaneRole
import com.example.ui.components.AppAssetImage
import com.example.ui.components.ChampionAvatar
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChampionsCatalogTab(
    isOverlay: Boolean = false,
    onSelectChampion: (Champion) -> Unit,
    horizontalPadding: androidx.compose.ui.unit.Dp = if (isOverlay) 4.dp else 16.dp
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isPremium by com.example.util.SubscriptionManager.isPremium.collectAsStateWithLifecycle()
    val catalogSignedIn by com.example.util.AuthManager.isSignedIn.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val syncState by GlobalMetaSyncService.syncState.collectAsStateWithLifecycle()
    val currentRegion by GlobalMetaSyncService.currentRegion.collectAsStateWithLifecycle()
    val favorites by FavoriteChampionsManager.favoritesFlow.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf<LaneRole?>(null) }

    var selectedTierFilter by remember { mutableStateOf<String?>(null) }
    var showOnlyFavorites by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(true) }
    var showFilterChips by remember { mutableStateOf(true) }

    val filteredChampions = remember(searchQuery, selectedRoleFilter, selectedTierFilter, showOnlyFavorites, favorites, syncState, currentRegion, WildRiftRepository.activeRegionName, WildRiftRepository.champions.toList()) {
        val trimmedQuery = searchQuery.trim()
        val list = WildRiftRepository.champions.filter { champ ->
            val matchesQuery = trimmedQuery.isBlank() ||
                    champ.name.contains(trimmedQuery, ignoreCase = true)
            val matchesRole = selectedRoleFilter == null ||
                    champ.primaryRole == selectedRoleFilter ||
                    champ.secondaryRoles.contains(selectedRoleFilter)
            val matchesTier = selectedTierFilter == null || champ.tier == selectedTierFilter
            val matchesFavorite = !showOnlyFavorites || favorites.contains(champ.id.lowercase())
            matchesQuery && matchesRole && matchesTier && matchesFavorite
        }
        if (selectedRoleFilter != null) {
            list.sortedWith(
                compareByDescending<Champion> { it.primaryRole == selectedRoleFilter }
                    .thenByDescending { it.tier == "S+" }
                    .thenByDescending { it.tier == "S" }
                    .thenByDescending { it.tier == "A+" }
                    .thenByDescending { it.winrate }
            )
        } else {
            list.sortedWith(
                compareByDescending<Champion> { it.tier == "S+" }
                    .thenByDescending { it.tier == "S" }
                    .thenByDescending { it.tier == "A+" }
                    .thenByDescending { it.winrate }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding)
    ) {
        if (isOverlay) Spacer(modifier = Modifier.height(4.dp)) else Spacer(modifier = Modifier.height(10.dp))




        // Search Bar (Proporcionado y compacto en overlay)
        if (isOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(HextechSurface)
                    .border(1.dp, HextechCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp)
                    .testTag("champions_search_input"),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = HextechCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 10.5.sp),
                        singleLine = true,
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(HextechCyan),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = tr("Buscar campeón..."),
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .coachClickable { searchQuery = "" },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = com.example.util.trNullable("Limpiar"),
                                tint = TextMuted,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("champions_search_input"),
                placeholder = { Text(tr("Buscar campeón por nombre o habilidad..."), color = TextMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HextechCyan) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Limpiar"), tint = TextMuted)
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
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Collapsible Header for Role Filters
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
                    text = tr("Filtrar por Rol"),
                    color = HextechCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                if (!showFilterChips) {
                    Spacer(modifier = Modifier.width(6.dp))
                    val roleLabel = selectedRoleFilter?.displayName ?: "Todos los Roles"
                    Surface(
                        color = HextechGold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, HextechGold.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = tr(roleLabel),
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
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header con indicador de deslizamiento para líneas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = HextechCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tr("Filtrar por Línea"),
                            color = HextechCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(HextechCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr("Desliza para ver más líneas"),
                            color = HextechCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Role & Favorites Filter Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(if (isOverlay) 4.dp else 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    // 1. Favoritos Filter Chip
                    val favCount = favorites.size
                    FilterChip(
                        selected = showOnlyFavorites,
                        onClick = {
                            if (isPremium) {
                                showOnlyFavorites = !showOnlyFavorites
                                if (showOnlyFavorites) selectedRoleFilter = null
                            } else {
                                android.widget.Toast.makeText(context, com.example.util.appTr("Requiere suscripción Premium"), android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tr("Favoritos"), fontSize = if (isOverlay) 10.sp else 11.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = com.example.util.tr("($favCount)"),
                                    color = if (showOnlyFavorites) HextechDarkBg else HextechGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isOverlay) 9.5.sp else 10.5.sp
                                )
                                if (!isPremium) {
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (showOnlyFavorites) HextechDarkBg else HextechGold)
                                            .padding(horizontal = 2.5.dp, vertical = 0.5.dp)
                                    ) {
                                        Text(
                                            text = com.example.util.tr("PRO"),
                                            color = if (showOnlyFavorites) HextechGold else HextechDarkBg,
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HextechGold,
                            selectedLabelColor = HextechDarkBg
                        )
                    )

                    // 2. Todos los Roles
                    val totalCount = WildRiftRepository.champions.size
                    val isAllSelected = selectedRoleFilter == null && !showOnlyFavorites
                    FilterChip(
                        selected = isAllSelected,
                        onClick = {
                            selectedRoleFilter = null
                            showOnlyFavorites = false
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(com.example.util.tr(if (isOverlay) tr("Todos") else tr("Todos los Roles")), fontSize = if (isOverlay) 10.sp else 11.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = com.example.util.tr("($totalCount)"),
                                    color = if (isAllSelected) HextechDarkBg else HextechGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isOverlay) 9.5.sp else 10.5.sp
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HextechCyan,
                            selectedLabelColor = HextechDarkBg
                        )
                    )
                    LaneRole.entries.forEach { role ->
                        val count = WildRiftRepository.champions.count { it.primaryRole == role || it.secondaryRoles.contains(role) }
                        val isSelected = selectedRoleFilter == role && !showOnlyFavorites
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                showOnlyFavorites = false
                                selectedRoleFilter = if (selectedRoleFilter == role) null else role
                            },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(tr(role.shortName), fontSize = if (isOverlay) 10.sp else 11.sp)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = com.example.util.tr("($count)"),
                                        color = if (isSelected) HextechDarkBg else HextechGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (isOverlay) 9.5.sp else 10.5.sp
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
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Champions List or Empty State
        if (filteredChampions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 32.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (showOnlyFavorites) Icons.Default.Star else Icons.Default.Search,
                        contentDescription = null,
                        tint = if (showOnlyFavorites) HextechGold else HextechCyan,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = com.example.util.tr(if (showOnlyFavorites) tr("No tienes campeones favoritos") else tr("No se encontraron campeones")),
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = com.example.util.tr(if (showOnlyFavorites)
                            tr("Toca la estrella ⭐ en cualquier campeón de la lista para añadirlo a tus favoritos y tener acceso directo.")
                        else
                            tr("Prueba a buscar con otro nombre o restablece los filtros.")),
                        color = TextMuted,
                        fontSize = 12.5.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
            items(filteredChampions) { champion ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .coachClickable { onSelectChampion(champion) }
                        .testTag("champion_item_${champion.id}"),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChampionAvatar(champion = champion, size = if (isOverlay) 42.dp else 58.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    val isFav = favorites.contains(champion.id.lowercase())
                                    IconButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            if (isPremium) {
                                                FavoriteChampionsManager.toggleFavorite(context, champion.id)
                                            } else {
                                                android.widget.Toast.makeText(context, com.example.util.appTr("Requiere suscripción Premium"), android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier
                                            .size(24.dp)
                                            .testTag("fav_btn_${champion.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = com.example.util.trNullable(if (isFav) tr("Quitar de Favoritos") else tr("Marcar como Favorito")),
                                            tint = if (isFav) HextechGold else TextMuted.copy(alpha = 0.35f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = com.example.util.tr(champion.name),
                                        color = TextPrimary,
                                        fontSize = if (isOverlay) 13.sp else 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (!isOverlay) { Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val winDelta = champion.winrateDelta
                                    val formattedDelta = String.format(java.util.Locale.US, "%.2f", winDelta)
                                    val winDeltaText = if (winDelta >= 0) "+${formattedDelta}%" else "${formattedDelta}%"
                                    val winDeltaColor = if (winDelta >= 0) Color(0xFF4CAF50) else DangerRed
                                    Text(
                                        text = com.example.util.tr(if (winDelta >= 0) "▲ $winDeltaText" else "▼ $winDeltaText"),
                                        color = winDeltaColor,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val formattedWr = com.example.util.championStatPercent(champion, champion.winrate, "wr")
                                    val regionTag = "🌐 Global"
                                    Text(
                                        text = com.example.util.tr("$regionTag WR: $formattedWr"),
                                        color = HextechGold,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            val roleFilter = selectedRoleFilter
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (roleFilter != null && champion.primaryRole != roleFilter) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(HextechGold.copy(alpha = 0.2f))
                                            .border(1.dp, HextechGold.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = com.example.util.tr("⭐ " + tr("Flex en ") + tr(roleFilter.shortName)),
                                            color = HextechGold,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = com.example.util.tr("Principal: ${com.example.util.tr(champion.primaryRole.shortName)} • ${com.example.util.tr(champion.damageType.displayName)}"),
                                        color = HextechCyan,
                                        fontSize = 11.sp
                                    )
                                } else {
                                    Text(
                                        text = com.example.util.tr("${com.example.util.tr(champion.primaryRole.displayName)} • ${com.example.util.tr(champion.damageType.displayName)}"),
                                        color = HextechCyan,
                                        fontSize = if (isOverlay) 9.5.sp else 11.5.sp
                                    )
                                    if (champion.secondaryRoles.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(HextechCyan.copy(alpha = 0.15f))
                                                .border(0.5.dp, HextechCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            val lang = com.example.util.currentAppLanguage()
                                            Text(
                                                text = com.example.util.tr(tr("Flex: ") + champion.secondaryRoles.joinToString("/") { com.example.util.trStr(lang, it.shortName) }),
                                                color = HextechCyan,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tr(champion.summary),
                                color = TextMuted,
                                fontSize = if (isOverlay) 9.5.sp else 11.sp,
                                maxLines = if (isOverlay) 1 else 2,
                                lineHeight = if (isOverlay) 12.sp else 15.sp,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            if (!isOverlay && catalogSignedIn) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(HextechDarkBg.copy(alpha = 0.5f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = com.example.util.tr(tr("Tendencia") + ":"),
                                        color = TextMuted,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    val safeWr = if (champion.winrate > 0.0) champion.winrate else (when(champion.tier) { "S+" -> 53.8; "S" -> 52.2; "A+", "A" -> 50.8; "B+", "B" -> 49.4; "C+", "C" -> 48.1; else -> 46.8 })
                                    val safeDelta = if (champion.winrateDelta != 0.0) champion.winrateDelta else (when(champion.tier) { "S+" -> 0.48; "S" -> 0.32; "A+", "A" -> 0.12; "B+", "B" -> -0.18; "C+", "C" -> -0.35; else -> -0.52 })
                                    SparklineTrendGraph(
                                        winrate = safeWr,
                                        delta = safeDelta,
                                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                                        showTimeLabels = true,
                                        showFullText = true,
                                        canvasHeight = 16
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            // Skill icons preview
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                champion.skills.take(5).forEach { skill ->
                                    AppAssetImage(
                                        url = skill.iconUrl,
                                        contentDescription = skill.name,
                                        fallbackText = skill.slot,
                                        modifier = Modifier.size(20.dp),
                                        borderColor = HextechCyan.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tr("Toca para ver build y runas"),
                                    color = TextPrimary,
                                    fontSize = 10.5.sp
                                )
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
}
}

// ====================================================================
// TAB 2: TIER LIST OFICIAL WILD RIFT (POR LÍNEAS Y TIERS)
// ====================================================================
