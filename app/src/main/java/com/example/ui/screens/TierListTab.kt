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
import androidx.compose.runtime.rememberCoroutineScope
import com.example.util.tr
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.example.model.LaneRole
import com.example.ui.components.ChampionAvatar
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
    var showTrendSignIn by remember { mutableStateOf(false) }
    if (showTrendSignIn) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showTrendSignIn = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(Modifier.fillMaxWidth(0.95f).heightIn(max = 680.dp),
                color = HextechDarkBg, shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.verticalScroll(rememberScrollState()).padding(12.dp)) {
                    TextButton(onClick = { showTrendSignIn = false }) { Text(tr("Cerrar")) }
                    com.example.ui.auth.AuthFlowContainer(onLoginSuccess = { showTrendSignIn = false })
                }
            }
        }
    }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val syncState by GlobalMetaSyncService.syncState.collectAsStateWithLifecycle()
    val currentRegion by GlobalMetaSyncService.currentRegion.collectAsStateWithLifecycle()

    val favorites by FavoriteChampionsManager.favoritesFlow.collectAsStateWithLifecycle()
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var selectedLane by remember { mutableStateOf<LaneRole?>(null) }
    var selectedSort by remember { mutableStateOf(TierSortOption.BY_TIER) }
    var showTierFilters by rememberSaveable { mutableStateOf(true) }

    val rawChampionsToDisplay = remember(selectedLane, showFavoritesOnly, favorites, syncState, currentRegion, WildRiftRepository.champions.toList()) {
        val champs = if (selectedLane == null) WildRiftRepository.champions.toList()
        else WildRiftRepository.getChampionsByRole(selectedLane!!)
        if (showFavoritesOnly) champs.filter { it.id in favorites } else champs
    }

    val championsToDisplay = remember(rawChampionsToDisplay, selectedSort, currentRegion) {
        when (selectedSort) {
            TierSortOption.BY_TIER -> rawChampionsToDisplay
            TierSortOption.WIN_RATE -> rawChampionsToDisplay.sortedByDescending { it.winrate }
            TierSortOption.PICK_RATE -> rawChampionsToDisplay.sortedByDescending { it.pickRate }
            TierSortOption.BAN_RATE -> rawChampionsToDisplay.sortedByDescending { it.banRate }
        }
    }

    val tierSPlus = championsToDisplay.filter { it.tier == "S+" }
    val tierS = championsToDisplay.filter { (it.tier == "S") && it !in tierSPlus }
    val tierA = championsToDisplay.filter { (it.tier == "A+" || it.tier == "A") && it !in tierSPlus && it !in tierS }
    val tierB = championsToDisplay.filter { (it.tier == "B" || it.tier == "B+") && it !in tierSPlus && it !in tierS && it !in tierA }
    val tierC = championsToDisplay.filter { (it.tier == "C" || it.tier == "C+") && it !in tierSPlus && it !in tierS && it !in tierA && it !in tierB }
    val tierD = championsToDisplay.filter { it !in tierSPlus && it !in tierS && it !in tierA && it !in tierB && it !in tierC }

    LazyColumn(
        modifier = Modifier
            .testTag("tier_list")
            .fillMaxSize()
            .padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
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
                IconButton(
                    onClick = { showTierFilters = !showTierFilters },
                    modifier = Modifier.size(24.dp)
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

            item {
                if (!isSignedIn) {
                    Column(Modifier.fillMaxWidth().testTag("tier_trend_sign_in")) {
                        Text(tr("Inicia sesión o regístrate para ver la evolución de 24 y 12 horas."),
                            color = TextSecondary, fontSize = 12.sp)
                        TextButton(onClick = { showTrendSignIn = true }, modifier = Modifier.testTag("tier_trend_sign_in_button")) {
                            Text(tr("Iniciar sesión o registrarse"))
                        }
                    }
                } else Card(
                    modifier = Modifier.fillMaxWidth().testTag("tier_trend_header"),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface.copy(alpha = 0.85f)),
                    border = androidx.compose.foundation.BorderStroke(0.6.dp, HextechCardBorder)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = HextechGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = tr("Gráfica de Tendencia:"),
                                    color = HextechGold,
                                    fontSize = if (isOverlay) 9.5.sp else 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = tr("hace 24h, 12h y actual"),
                                    color = HextechCyan,
                                    fontSize = if (isOverlay) 8.5.sp else 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00FF7F))
                                )
                                Text(
                                    text = tr("En vivo"),
                                    color = Color(0xFF00FF7F),
                                    fontSize = if (isOverlay) 8.sp else 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(HextechCyan))
                                Text(tr("hace 24 horas"), color = HextechCyan, fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text(com.example.util.tr("➔"), color = TextMuted, fontSize = 8.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(HextechGold))
                                Text(tr("hace 12 horas"), color = HextechGold, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(com.example.util.tr("➔"), color = TextMuted, fontSize = 8.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF00FF7F)))
                                Text(tr("actual"), color = Color(0xFF00FF7F), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
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

            // Tier B / T4
            if (tierB.isNotEmpty()) {
                item {
                    TierSectionCard(
                        isOverlay = isOverlay,
                        showTrend = isSignedIn,
                        tierName = "TIER B (${tr("Opciones Viables")})",
                        tierColor = com.example.ui.theme.TierBColor,
                        champions = tierB,
                        onSelectChampion = onSelectChampion
                    )
                }
            }

            // Tier C / T5
            if (tierC.isNotEmpty()) {
                item {
                    TierSectionCard(
                        isOverlay = isOverlay,
                        showTrend = isSignedIn,
                        tierName = "TIER C (${tr("Situacionales")})",
                        tierColor = com.example.ui.theme.TierCColor,
                        champions = tierC,
                        onSelectChampion = onSelectChampion
                    )
                }
            }

            // Tier D
            if (tierD.isNotEmpty()) {
                item {
                    TierSectionCard(
                        isOverlay = isOverlay,
                        showTrend = isSignedIn,
                        tierName = "TIER D / OTROS",
                        tierColor = com.example.ui.theme.TierDColor,
                        champions = tierD,
                        onSelectChampion = onSelectChampion
                    )
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
                                                    val winDelta = champ.winrateDelta
                                                    val formattedDelta = String.format(java.util.Locale.US, "%.2f", winDelta)
                                                    val winDeltaText = if (winDelta >= 0) "+${formattedDelta}%" else "${formattedDelta}%"
                                                    val winDeltaColor = if (winDelta >= 0) Color(0xFF4CAF50) else DangerRed
                                                    if (!isOverlay) {
                                                        Text(
                                                            text = com.example.util.tr(if (winDelta >= 0) "▲ $winDeltaText" else "▼ $winDeltaText"),
                                                            color = winDeltaColor,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                    Text(com.example.util.tr("WR: ${com.example.util.championStatPercent(champ, champ.winrate, "wr")}"), color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(com.example.util.tr("Pick: ${com.example.util.championStatPercent(champ, champ.pickRate, "pick")} • Ban: ${com.example.util.championStatPercent(champ, champ.banRate, "ban")}"), color = TextMuted, fontSize = 10.sp, maxLines = 1)
                                            }
                                            TierSortOption.PICK_RATE -> {
                                                Text(com.example.util.tr("Pick: ${com.example.util.championStatPercent(champ, champ.pickRate, "pick")}"), color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(com.example.util.tr("WR: ${com.example.util.championStatPercent(champ, champ.winrate, "wr")} • Ban: ${com.example.util.championStatPercent(champ, champ.banRate, "ban")}"), color = TextMuted, fontSize = 10.sp, maxLines = 1)
                                            }
                                            TierSortOption.BAN_RATE -> {
                                                Text(com.example.util.tr("Ban: ${com.example.util.championStatPercent(champ, champ.banRate, "ban")}"), color = DangerRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(com.example.util.tr("WR: ${com.example.util.championStatPercent(champ, champ.winrate, "wr")} • Pick: ${com.example.util.championStatPercent(champ, champ.pickRate, "pick")}"), color = TextMuted, fontSize = 10.sp, maxLines = 1)
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
                            if (!isOverlay && isSignedIn) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .testTag("tier_trend_graph")
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
                                    val safeWr = if (champ.winrate > 0.0) champ.winrate else (when(champ.tier) { "S+" -> 53.8; "S" -> 52.2; "A+", "A" -> 50.8; "B+", "B" -> 49.4; "C+", "C" -> 48.1; else -> 46.8 })
                                    val safeDelta = if (champ.winrateDelta != 0.0) champ.winrateDelta else (when(champ.tier) { "S+" -> 0.48; "S" -> 0.32; "A+", "A" -> 0.12; "B+", "B" -> -0.18; "C+", "C" -> -0.35; else -> -0.52 })
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
                                            val winDelta = champ.winrateDelta
                                            val formattedDelta = String.format(java.util.Locale.US, "%.2f", winDelta)
                                            val winDeltaText = if (winDelta >= 0) "+${formattedDelta}%" else "${formattedDelta}%"
                                            val winDeltaColor = if (winDelta >= 0) Color(0xFF4CAF50) else DangerRed
                                            if (!isOverlay) {
                                                Text(
                                                    text = com.example.util.tr(if (winDelta >= 0) "▲ $winDeltaText" else "▼ $winDeltaText"),
                                                    color = winDeltaColor,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = com.example.util.tr(tr("WR") + ": ${com.example.util.championStatPercent(champ, champ.winrate, "wr")}"),
                                                color = HextechGold,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp
                                            )
                                        }
                                        if (!isOverlay) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = com.example.util.tr("Pick: ${com.example.util.championStatPercent(champ, champ.pickRate, "pick")} • Ban: ${com.example.util.championStatPercent(champ, champ.banRate, "ban")}"),
                                                color = TextMuted,
                                                fontSize = 10.sp,
                                                maxLines = 1
                                            )
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
                            if (!isOverlay && showTrend) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .testTag("tier_trend_graph")
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
                                    val safeWr = if (champ.winrate > 0.0) champ.winrate else (when(champ.tier) { "S+" -> 53.8; "S" -> 52.2; "A+", "A" -> 50.8; "B+", "B" -> 49.4; "C+", "C" -> 48.1; else -> 46.8 })
                                    val safeDelta = if (champ.winrateDelta != 0.0) champ.winrateDelta else (when(champ.tier) { "S+" -> 0.48; "S" -> 0.32; "A+", "A" -> 0.12; "B+", "B" -> -0.18; "C+", "C" -> -0.35; else -> -0.52 })
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
