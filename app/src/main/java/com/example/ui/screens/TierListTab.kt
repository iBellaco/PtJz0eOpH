package com.example.ui.screens

import com.example.ui.components.CoachFilterChip as FilterChip
import com.example.ui.components.CoachTextButton as TextButton
import com.example.ui.components.CoachIconButton as IconButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.History
import android.widget.Toast
import com.example.data.local.FavoriteChampionsManager
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.Composable
import com.example.util.tr
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.data.WildRiftRepository
import com.example.data.sync.GlobalMetaSyncService
import com.example.model.Champion
import com.example.model.LaneRole
import com.example.ui.components.ChampionAvatar
import com.example.ui.components.TierMovementBadge
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
import com.example.ui.theme.TierAColor
import com.example.ui.theme.TierBColor
import com.example.ui.theme.TierCColor
import com.example.ui.theme.TierSColor
import com.example.ui.theme.TierSPlusColor

enum class TierSortOption(val displayName: String, val shortLabel: String) {
    BY_TIER("Por Tier", "Tier "),
    WIN_RATE("Win Rate", "Win Rate "),
    PICK_RATE("Pick Rate", "Pick Rate "),
    BAN_RATE("Ban Rate", "Ban Rate ")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TierListTab(
    isOverlay: Boolean = false,
    onSelectChampion: (Champion) -> Unit,
    isPremium: Boolean = false,
    horizontalPadding: androidx.compose.ui.unit.Dp = if (isOverlay) 4.dp else 16.dp
) {
    val isSignedIn by com.example.util.AuthManager.isSignedIn.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val syncState by GlobalMetaSyncService.syncState.collectAsStateWithLifecycle()
    val currentRegion by GlobalMetaSyncService.currentRegion.collectAsStateWithLifecycle()
    val metaRevision = WildRiftRepository.regionRevision
    // La vista se mantiene viva aunque el editor externo publique por lotes.
    // El valor solo cambia cuando llega una observación nueva de la fuente.
    var liveTick by remember { mutableStateOf(0L) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            liveTick = System.currentTimeMillis()
            kotlinx.coroutines.delay(1_000L)
        }
    }

    val favorites by FavoriteChampionsManager.favoritesFlow.collectAsStateWithLifecycle()
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var selectedLane by remember { mutableStateOf<LaneRole?>(null) }
    var selectedSort by remember { mutableStateOf(TierSortOption.BY_TIER) }
    var showTierFilters by rememberSaveable { mutableStateOf(true) }

    val rawChampionsToDisplay = remember(selectedLane, showFavoritesOnly, favorites, syncState, currentRegion, metaRevision, liveTick, WildRiftRepository.champions.toList()) {
        val champs = if (selectedLane == null) WildRiftRepository.champions.toList()
        else WildRiftRepository.getChampionsByRole(selectedLane!!)
        val visibleTiers = champs.filter { it.tier in setOf("S+", "S", "A+", "A", "B", "C") }
        if (showFavoritesOnly) visibleTiers.filter { it.id in favorites } else visibleTiers
    }

    val championsToDisplay = remember(rawChampionsToDisplay, selectedSort, currentRegion) {
        when (selectedSort) {
            TierSortOption.BY_TIER -> rawChampionsToDisplay
            TierSortOption.WIN_RATE -> rawChampionsToDisplay.sortedByDescending { if (it.hasRegionalStats && it.winrate.isFinite()) it.winrate else Double.NEGATIVE_INFINITY }
            TierSortOption.PICK_RATE -> rawChampionsToDisplay.sortedByDescending { if (it.hasRegionalStats && it.pickRate.isFinite()) it.pickRate else Double.NEGATIVE_INFINITY }
            TierSortOption.BAN_RATE -> rawChampionsToDisplay.sortedByDescending { if (it.hasRegionalStats && it.banRate.isFinite()) it.banRate else Double.NEGATIVE_INFINITY }
        }
    }

    val tierSPlus = championsToDisplay.filter { it.tier == "S+" }
    val tierS = championsToDisplay.filter { (it.tier == "S") && it !in tierSPlus }
    val tierB = championsToDisplay.filter { it.tier == "B" }
    val tierC = championsToDisplay.filter { it.tier == "C" }
    val tierA = championsToDisplay.filter { (it.tier == "A+" || it.tier == "A") && it !in tierSPlus && it !in tierS }

    LazyColumn(
        modifier = Modifier
            .testTag("tier_list")
            .fillMaxSize()
            .padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            com.example.ui.components.MetaSyncSummary()
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            // Collapsible Header for Tier List Filters and Sorting
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .coachClickable { showTierFilters = !showTierFilters }
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
                        text = tr("Filtrar por Línea"),
                        color = HextechCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (!showTierFilters) {
                        Spacer(modifier = Modifier.width(6.dp))
                        val laneLabel = selectedLane?.shortName ?: "Todas las Líneas"
                        Surface(
                            color = HextechGold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.5.dp, HextechGold.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = com.example.util.tr("${tr(laneLabel)} • ${tr(selectedSort.displayName)}"),
                                color = HextechGold,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showTierFilters = !showTierFilters },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (showTierFilters) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = com.example.util.trNullable(if (showTierFilters) tr("Minimizar filtros") else tr("Expandir filtros")),
                            tint = HextechGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (showTierFilters) {
            item {
                // Role Filter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(if (isOverlay) 4.dp else 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = showFavoritesOnly,
                        onClick = {
                            if (isPremium) {
                                showFavoritesOnly = !showFavoritesOnly
                            } else {
                                Toast.makeText(context, com.example.util.appTr("Requiere Premium"), Toast.LENGTH_SHORT).show()
                            }
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tr("Favoritos"), fontSize = if (isOverlay) 10.sp else 11.5.sp)
                                if (!isPremium) {
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(HextechGold)
                                            .padding(horizontal = 2.5.dp, vertical = 0.5.dp)
                                    ) {
                                        Text(com.example.util.tr("PRO"), color = HextechDarkBg, fontSize = 7.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HextechCyan,
                            selectedLabelColor = HextechDarkBg
                        ),
                        leadingIcon = {
                            if (showFavoritesOnly) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(if (isOverlay) 14.dp else 16.dp))
                            } else {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(if (isOverlay) 14.dp else 16.dp), tint = if (isPremium) TextPrimary else TextMuted)
                            }
                        }
                    )
                    FilterChip(
                        selected = selectedLane == null,
                        onClick = { selectedLane = null },
                        label = { Text(com.example.util.tr(if (isOverlay) tr("Todas") else tr("Todas las Líneas")), fontSize = if (isOverlay) 10.sp else 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HextechCyan,
                            selectedLabelColor = HextechDarkBg
                        )
                    )
                    LaneRole.entries.forEach { role ->
                        FilterChip(
                            selected = selectedLane == role,
                            onClick = { selectedLane = if (selectedLane == role) null else role },
                            leadingIcon = {
                                Image(
                                    painter = painterResource(id = role.iconResId),
                                    contentDescription = null,
                                    modifier = Modifier.size(if (isOverlay) 13.dp else 16.dp)
                                )
                            },
                            label = { Text(tr(role.shortName), fontSize = if (isOverlay) 10.sp else 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HextechCyan,
                                selectedLabelColor = HextechDarkBg
                            )
                        )
                    }
                }
            }

            item {
                // Header con indicador de deslizamiento para orden
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
                            tint = HextechGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tr("Criterio de Orden"),
                            color = HextechGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = tr("Desliza opciones"),
                        color = TextMuted,
                        fontSize = 9.sp
                    )
                }

                // Sorting Selector (Por Tier, Win Rate, Pick Rate, Ban Rate)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(if (isOverlay) 4.dp else 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TierSortOption.entries.forEach { sortOpt ->
                        val isSelected = selectedSort == sortOpt
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSort = sortOpt },
                            label = { Text(tr(sortOpt.shortLabel), fontSize = if (isOverlay) 9.5.sp else 10.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HextechGold,
                                selectedLabelColor = HextechDarkBg
                            )
                        )
                    }
                }
            }


        }

        if (selectedSort == TierSortOption.BY_TIER) {
            // Tier S+ / T0
            if (tierSPlus.isNotEmpty()) {
                item {
                    TierSectionCard(
                        isOverlay = isOverlay,
                        showTrend = isSignedIn,
                        tierName = "TIER S+ (${tr("Dominantes / Prioridad Pick & Ban")})",
                        tierColor = TierSPlusColor,
                        champions = tierSPlus,
                        onSelectChampion = onSelectChampion
                    )
                }
            }

            // Tier S / T1
            if (tierS.isNotEmpty()) {
                item {
                    TierSectionCard(
                        isOverlay = isOverlay,
                        showTrend = isSignedIn,
                        tierName = "TIER S (${tr("Meta Muy Fuerte / Alta Prioridad")})",
                        tierColor = TierSColor,
                        champions = tierS,
                        onSelectChampion = onSelectChampion
                    )
                }
            }

            // Tier A / T2-T3
            if (tierA.isNotEmpty()) {
                item {
                    TierSectionCard(
                        isOverlay = isOverlay,
                        showTrend = isSignedIn,
                        tierName = "TIER A (${tr("Opciones Sólidas y Balanceadas")})",
                        tierColor = TierAColor,
                        champions = tierA,
                        onSelectChampion = onSelectChampion
                    )
                }
            }

            listOf("B" to (TierBColor to tierB), "C" to (TierCColor to tierC)).forEach { (tier, section) ->
                if (section.second.isNotEmpty()) item {
                    TierSectionCard(isOverlay = isOverlay, showTrend = isSignedIn,
                        tierName = "TIER $tier", tierColor = section.first,
                        champions = section.second, onSelectChampion = onSelectChampion)
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        } else {
            // Sorted Ranked List by Win Rate, Pick Rate, or Ban Rate
            item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = com.example.util.tr("${tr("Clasificación por")} ${tr(selectedSort.displayName)} (${championsToDisplay.size})"),
                            color = HextechGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                itemsIndexed(championsToDisplay) { index, champ ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .coachClickable { onSelectChampion(champ) },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechSurface),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when {
                                index == 0 -> HextechGold
                                index < 3 -> HextechCyan
                                else -> HextechCardBorder
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            // Fila Principal: Rango + Avatar + Nombre/Rol (Izquierda) y Estadísticas Clave (Derecha)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    // Rank Badge
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (index) {
                                                    0 -> HextechGold
                                                    1 -> HextechCyan
                                                    2 -> Color(0xFFCD7F32)
                                                    else -> HextechSurfaceVariant
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = com.example.util.tr("${index + 1}"),
                                            color = if (index < 3) HextechDarkBg else TextMuted,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    ChampionAvatar(champion = champ, size = 42.dp, showTierBadge = true)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.padding(end = 4.dp)) {
                                        Text(
                                            text = com.example.util.tr(champ.name),
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        TierMovementBadge(champ.tierMovement)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = com.example.util.tr("${com.example.util.tr(champ.primaryRole.shortName)} • ${com.example.util.tr(champ.damageType.displayName)}"),
                                            color = HextechCyan,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        when (selectedSort) {
                                            TierSortOption.WIN_RATE -> {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    if (!isOverlay) com.example.ui.components.RateChangeBadge(if (champ.has24hComparison) champ.winrateDelta else Double.NaN)
                                                    Text(com.example.util.tr("WR: ${com.example.util.championStatPercent(champ, champ.winrate, "wr")}"), color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                com.example.ui.components.ChampionRateLine(champ)
                                            }
                                            TierSortOption.PICK_RATE -> {
                                                Text(com.example.util.tr("Pick: ${com.example.util.championStatPercent(champ, champ.pickRate, "pick")}"), color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                com.example.ui.components.ChampionRateLine(champ, includeWin = true, includePick = false)
                                            }
                                            TierSortOption.BAN_RATE -> {
                                                Text(com.example.util.tr("Ban: ${com.example.util.championStatPercent(champ, champ.banRate, "ban")}"), color = DangerRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                com.example.ui.components.ChampionRateLine(champ, includeWin = true, includeBan = false)
                                            }
                                            else -> {}
                                        }
                                    }
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = HextechCyan.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // History is available only to registered, signed-in accounts.

                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
        }
    }
}

@Composable
fun TierSectionCard(
    isOverlay: Boolean = false,
    showTrend: Boolean = false,
    tierName: String,
    tierColor: Color,
    champions: List<Champion>,
    onSelectChampion: (Champion) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, tierColor.copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(tierColor)
                )
                Text(
                    text = com.example.util.tr(tierName),
                    color = tierColor,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                champions.forEach { champ ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .coachClickable { onSelectChampion(champ) },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechSurfaceVariant.copy(alpha = 0.6f)),
                        border = androidx.compose.foundation.BorderStroke(0.6.dp, HextechCardBorder.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(9.dp)
                        ) {
                            // Fila Superior: Avatar + Nombre / Rol (Izquierda) vs WR / Pick / Ban / Flecha (Derecha)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    ChampionAvatar(champion = champ, size = 42.dp, showTierBadge = false)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.padding(end = 4.dp)) {
                                        Text(
                                            text = com.example.util.tr(champ.name),
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        TierMovementBadge(champ.tierMovement)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = com.example.util.tr("${com.example.util.tr(champ.primaryRole.shortName)} • ${com.example.util.tr(champ.damageType.displayName)}"),
                                            color = HextechCyan,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (!isOverlay) com.example.ui.components.RateChangeBadge(if (champ.has24hComparison) champ.winrateDelta else Double.NaN)
                                            Text(
                                                text = com.example.util.tr(tr("WR") + ": ${com.example.util.championStatPercent(champ, champ.winrate, "wr")}"),
                                                color = HextechGold,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp
                                            )
                                        }
                                        if (!isOverlay) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            com.example.ui.components.ChampionRateLine(champ)
                                        }
                                    }
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = HextechCyan.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Gráfica de tendencia (hace 24h, 12h y actual) para todos los campeones

                        }
                    }
                }
            }
        }
    }
}

// ====================================================================
// TAB 3: CATÁLOGO DE OBJETOS (ITEMS) DE WILD RIFT
// ====================================================================
