package com.example.ui.screens

import com.example.ui.components.CoachFilterChip as FilterChip
import com.example.ui.components.CoachTextButton as TextButton
import com.example.ui.components.CoachIconButton as IconButton
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.data.sync.MetaRegion
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
    BAN_RATE("Ban Rate", "Ban Rate ")
}

internal data class TierDisplayedStats(val role: LaneRole, val win: Double, val pick: Double,
    val ban: Double, val winDelta: Double)

/**
 * The server selector needs a visible state transition even when the shared
 * button-press animation is disabled (for example in the floating overlay).
 * Keeping the animation on the selected state also makes a region change
 * obvious without changing the selected dataset or its persistence.
 */
@Composable
private fun AnimatedRegionChip(
    region: String,
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val isChina = MetaRegion.normalize(region) == MetaRegion.CHINA
    val selectedContainer = if (isChina) Color(0xFFB56CFF) else HextechGold
    val unselectedContainer = if (isChina) Color(0xFF321B4D).copy(alpha = 0.5f) else HextechSurfaceVariant
    val containerColor by animateColorAsState(
        targetValue = if (selected) selectedContainer else unselectedContainer,
        animationSpec = spring(stiffness = 420f),
        label = "regionContainerColor"
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) HextechDarkBg else TextPrimary,
        animationSpec = spring(stiffness = 420f),
        label = "regionLabelColor"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.045f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = 420f
        ),
        label = "regionScale"
    )
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier
            .heightIn(min = 48.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = containerColor,
            selectedLabelColor = labelColor,
            containerColor = containerColor,
            labelColor = labelColor
        ),
        border = BorderStroke(1.dp, if (selected) selectedContainer else selectedContainer.copy(alpha = 0.35f))
    )
}

private fun completeLaneStats(stats: com.example.model.ChampionLaneStats?): Boolean =
    stats != null && listOf(stats.winRate, stats.pickRate, stats.banRate).all {
        it.isFinite() && it in 0.0..100.0
    }

private fun tierRank(value: String): Int = when (value.trim().uppercase(java.util.Locale.ROOT)) {
    "S+" -> 5
    "S" -> 4
    "A+", "A" -> 3
    "B" -> 2
    "C", "D" -> 1
    else -> 0
}

/** China publishes a different primary lane for some champions. */
internal fun chinaDefaultRole(champion: Champion): LaneRole =
    champion.laneStats.entries
        .filter { completeLaneStats(it.value) }
        .maxWithOrNull(compareBy<Map.Entry<String, com.example.model.ChampionLaneStats>>(
            { tierRank(it.value.tier) },
            { it.value.pickRate }
        ))
        ?.key
        ?.let { runCatching { LaneRole.valueOf(it) }.getOrNull() }
        ?: champion.primaryRole

internal fun tierRole(champion: Champion, selectedLane: LaneRole?, region: String): LaneRole =
    selectedLane ?: if (MetaRegion.normalize(region) == MetaRegion.CHINA) {
        chinaDefaultRole(champion)
    } else champion.primaryRole

internal fun tierForLane(champion: Champion, selectedLane: LaneRole?, region: String = MetaRegion.DEFAULT): String {
    val role = tierRole(champion, selectedLane, region)
    val laneTier = champion.laneStats[role.name]?.tier.orEmpty()
    return laneTier.ifBlank { champion.tier }
}

internal fun tierDisplayedStats(
    champion: Champion,
    selectedLane: LaneRole?,
    region: String = MetaRegion.DEFAULT
): TierDisplayedStats {
    val role = tierRole(champion, selectedLane, region)
    val lane = champion.laneStats[role.name]
    val isGlobalPrimary = MetaRegion.normalize(region) != MetaRegion.CHINA && role == champion.primaryRole
    return TierDisplayedStats(
        role,
        lane?.winRate?.takeIf(Double::isFinite) ?: if (isGlobalPrimary) champion.winrate else Double.NaN,
        lane?.pickRate?.takeIf(Double::isFinite) ?: if (isGlobalPrimary) champion.pickRate else Double.NaN,
        lane?.banRate?.takeIf(Double::isFinite) ?: if (isGlobalPrimary) champion.banRate else Double.NaN,
        lane?.winRateDelta?.takeIf(Double::isFinite) ?: if (isGlobalPrimary) champion.winrateDelta else 0.0
    )
}

/** Sorts the already validated rows with the metrics of the active region and lane. */
internal fun sortTierListChampions(
    champions: List<Champion>,
    selectedSort: TierSortOption,
    selectedLane: LaneRole?,
    region: String
): List<Champion> = when (selectedSort) {
    TierSortOption.BY_TIER -> champions
    TierSortOption.WIN_RATE -> champions.sortedWith(
        compareByDescending<Champion> { tierDisplayedStats(it, selectedLane, region).win }
            .thenBy { it.name }
    )
    TierSortOption.BAN_RATE -> champions.sortedWith(
        compareByDescending<Champion> { tierDisplayedStats(it, selectedLane, region).ban }
            .thenBy { it.name }
    )
}

/** A tier entry is meaningful only when a current competitive snapshot exists. */
internal fun isTierListChampionVisible(champion: Champion, selectedLane: LaneRole? = null): Boolean {
    if (champion.tier.trim().uppercase(java.util.Locale.ROOT) !in setOf("S+", "S", "A+", "A", "B", "C")) return false
    if (!champion.hasRegionalStats || champion.statsCheckedAt <= 0L) return false
    val stats = tierDisplayedStats(champion, selectedLane)
    // A tier entry must have the same three current metrics shown in its
    // detail sheet. Partial publisher rows stay out of the competitive list
    // until win, pick and ban are all available.
    return listOf(stats.win, stats.pick, stats.ban).all { it.isFinite() && it in 0.0..100.0 }
}

internal fun isTierListChampionVisible(
    champion: Champion,
    selectedLane: LaneRole?,
    region: String
): Boolean {
    val normalized = MetaRegion.normalize(region)
    if (normalized == MetaRegion.CHINA) {
        val role = tierRole(champion, selectedLane, normalized)
        if (!completeLaneStats(champion.laneStats[role.name])) return false
    }
    if (tierForLane(champion, selectedLane, normalized).trim().uppercase(java.util.Locale.ROOT)
        !in setOf("S+", "S", "A+", "A", "B", "C")) return false
    if (!champion.hasRegionalStats || champion.statsCheckedAt <= 0L) return false
    val stats = tierDisplayedStats(champion, selectedLane, normalized)
    return listOf(stats.win, stats.pick, stats.ban).all { it.isFinite() && it in 0.0..100.0 }
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
    val currentChinaElo by GlobalMetaSyncService.currentChinaElo.collectAsStateWithLifecycle()
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
    val regionScope = rememberCoroutineScope()
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var selectedLane by remember { mutableStateOf<LaneRole?>(null) }
    var selectedSort by remember { mutableStateOf(TierSortOption.BY_TIER) }
    var showTierFilters by rememberSaveable { mutableStateOf(true) }
    val isChinaRegion = MetaRegion.normalize(currentRegion) == MetaRegion.CHINA
    androidx.compose.runtime.LaunchedEffect(currentRegion) {
        selectedSort = TierSortOption.BY_TIER
        showFavoritesOnly = false
    }

    val rawChampionsToDisplay = remember(selectedLane, showFavoritesOnly, favorites, syncState, currentRegion, currentChinaElo, metaRevision, liveTick, WildRiftRepository.champions.toList()) {
        val champs = if (selectedLane == null) WildRiftRepository.champions.toList()
        else WildRiftRepository.champions.filter { publishedChampionRoles(it, currentRegion).contains(selectedLane) }
        val visibleTiers = champs.filter { isTierListChampionVisible(it, selectedLane, currentRegion) }
        if (showFavoritesOnly) visibleTiers.filter { it.id in favorites } else visibleTiers
    }

    val championsToDisplay = remember(rawChampionsToDisplay, selectedSort, selectedLane, currentRegion) {
        sortTierListChampions(rawChampionsToDisplay, selectedSort, selectedLane, currentRegion)
    }

    val tierSPlus = championsToDisplay.filter { tierForLane(it, selectedLane, currentRegion) == "S+" }
    val tierS = championsToDisplay.filter { tierForLane(it, selectedLane, currentRegion) == "S" }
    val tierB = championsToDisplay.filter { tierForLane(it, selectedLane, currentRegion) == "B" }
    val tierC = championsToDisplay.filter { tierForLane(it, selectedLane, currentRegion) == "C" }
    val tierA = championsToDisplay.filter {
        tierForLane(it, selectedLane, currentRegion) == "A" || tierForLane(it, selectedLane, currentRegion) == "A+"
    }

    LazyColumn(
        modifier = Modifier
            .testTag("tier_list")
            .fillMaxSize()
            .padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(if (isOverlay) 4.dp else 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = com.example.util.tr("Servidor") + ":",
                        color = HextechGold,
                        fontSize = if (isOverlay) 10.sp else 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    MetaRegion.available.forEach { region ->
                        val selected = MetaRegion.normalize(currentRegion) == region
                        AnimatedRegionChip(
                            region = region,
                            selected = selected,
                            onClick = {
                                if (!selected) GlobalMetaSyncService.setRegion(context, region, regionScope)
                            },
                            label = {
                                Text(
                                    com.example.util.tr(MetaRegion.label(region)),
                                    fontSize = if (isOverlay) 10.sp else 11.5.sp
                                )
                            },
                            modifier = Modifier.testTag("tier_region_${region.lowercase(java.util.Locale.ROOT)}"),
                        )
                    }
                }
            }

            if (isChinaRegion) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().testTag("china_elo_selector"),
                        color = Color(0xFF241638).copy(alpha = 0.72f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFB56CFF).copy(alpha = 0.75f))
                    ) {
                        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(if (isOverlay) 4.dp else 6.dp)
                            ) {
                                MetaRegion.chinaEloOptions.forEach { option ->
                                    val selected = currentChinaElo == option.id
                                    FilterChip(
                                        selected = selected,
                                        onClick = { GlobalMetaSyncService.setChinaElo(context, option.id) },
                                        label = { Text(tr(option.label), fontSize = if (isOverlay) 9.5.sp else 10.5.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFB56CFF),
                                            selectedLabelColor = HextechDarkBg,
                                            containerColor = Color(0xFF321B4D).copy(alpha = 0.6f)
                                        ),
                                        modifier = Modifier.testTag("china_elo_${option.id}")
                                    )
                                }
                            }
                        }
                    }
                }
            }

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
                        showStats = true,
                        showTrend = isSignedIn,
                        tierName = "TIER S+ (${tr("Dominantes / Prioridad Pick & Ban")})",
                        tierColor = TierSPlusColor,
                        champions = tierSPlus,
                        selectedLane = selectedLane,
                        region = currentRegion,
                        onSelectChampion = onSelectChampion
                    )
                }
            }

            // Tier S / T1
            if (tierS.isNotEmpty()) {
                item {
                    TierSectionCard(
                        isOverlay = isOverlay,
                        showStats = true,
                        showTrend = isSignedIn,
                        tierName = "TIER S (${tr("Meta Muy Fuerte / Alta Prioridad")})",
                        tierColor = TierSColor,
                        champions = tierS,
                        selectedLane = selectedLane,
                        region = currentRegion,
                        onSelectChampion = onSelectChampion
                    )
                }
            }

            // Tier A / T2-T3
            if (tierA.isNotEmpty()) {
                item {
                    TierSectionCard(
                        isOverlay = isOverlay,
                        showStats = true,
                        showTrend = isSignedIn,
                        tierName = "TIER A (${tr("Opciones Sólidas y Balanceadas")})",
                        tierColor = TierAColor,
                        champions = tierA,
                        selectedLane = selectedLane,
                        region = currentRegion,
                        onSelectChampion = onSelectChampion
                    )
                }
            }

            listOf("B" to (TierBColor to tierB), "C" to (TierCColor to tierC)).forEach { (tier, section) ->
                if (section.second.isNotEmpty()) item {
                    TierSectionCard(isOverlay = isOverlay, showStats = true, showTrend = isSignedIn,
                        tierName = "TIER $tier", tierColor = section.first,
                        champions = section.second, selectedLane = selectedLane, region = currentRegion,
                        onSelectChampion = onSelectChampion)
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
                    val displayed = tierDisplayedStats(champ, selectedLane, currentRegion)
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
                                            text = com.example.util.tr("${com.example.util.tr(displayed.role.shortName)} • ${com.example.util.tr(champ.damageType.displayName)}"),
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
                                                    Text(com.example.util.tr("WR: ${com.example.util.championStatPercent(champ, displayed.win, "wr")}"), color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                com.example.ui.components.ChampionRateLine(champ, includePick = false, winRate = displayed.win, pickRate = displayed.pick, banRate = displayed.ban)
                                            }
                                            TierSortOption.BAN_RATE -> {
                                                Text(com.example.util.tr("Ban: ${com.example.util.championStatPercent(champ, displayed.ban, "ban")}"), color = DangerRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                com.example.ui.components.ChampionRateLine(champ, includeWin = true, includeBan = false, winRate = displayed.win, pickRate = displayed.pick, banRate = displayed.ban)
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
    showStats: Boolean = true,
    showTrend: Boolean = false,
    tierName: String,
    tierColor: Color,
    champions: List<Champion>,
    selectedLane: LaneRole? = null,
    region: String = MetaRegion.DEFAULT,
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
                    val displayed = tierDisplayedStats(champ, selectedLane, region)
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
                                            text = com.example.util.tr("${com.example.util.tr(displayed.role.shortName)} • ${com.example.util.tr(champ.damageType.displayName)}"),
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
                                    if (showStats) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = com.example.util.tr(tr("WR") + ": ${com.example.util.championStatPercent(champ, displayed.win, "wr")}"),
                                                    color = HextechGold,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.5.sp
                                                )
                                            }
                                            if (!isOverlay) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                com.example.ui.components.ChampionRateLine(champ, includePick = false, winRate = displayed.win, pickRate = displayed.pick, banRate = displayed.ban)
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = com.example.util.tr("Clasificación CN"),
                                            color = HextechGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
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
