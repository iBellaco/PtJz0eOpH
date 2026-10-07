package com.example.service

import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Champion
import com.example.model.DraftSlot
import com.example.model.LaneRole
import com.example.ui.components.AppAssetImage
import com.example.ui.components.ChampionAvatar
import com.example.ui.components.WomboComboSynergyDetector
import com.example.ui.theme.AllyBlue
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TierSPlusColor
import com.example.util.SubscriptionManager
import com.example.util.tr

@Composable
internal fun FloatingDraftCoachView(
    isLandscapeMode: Boolean,
    activeRole: LaneRole,
    onActiveRoleChange: (LaneRole) -> Unit,
    isFirstPick: Boolean,
    onFirstPickToggle: () -> Unit,
    isLegendaryQueue: Boolean = false,
    onToggleLegendaryQueue: (() -> Unit)? = null,
    isLoadingScreenMode: Boolean,
    onLoadingScreenModeToggle: () -> Unit,
    allies: androidx.compose.runtime.snapshots.SnapshotStateList<Champion?>,
    enemies: androidx.compose.runtime.snapshots.SnapshotStateList<Champion?>,
    enemyConfidences: androidx.compose.runtime.snapshots.SnapshotStateMap<LaneRole, Int>,
    allySummonerNames: androidx.compose.runtime.snapshots.SnapshotStateMap<Int, String> = remember { androidx.compose.runtime.mutableStateMapOf() },
    enemySummonerNames: androidx.compose.runtime.snapshots.SnapshotStateMap<Int, String> = remember { androidx.compose.runtime.mutableStateMapOf() },
    allySpells: androidx.compose.runtime.snapshots.SnapshotStateMap<Int, List<String>> = remember { androidx.compose.runtime.mutableStateMapOf() },
    enemySpells: androidx.compose.runtime.snapshots.SnapshotStateMap<Int, List<String>> = remember { androidx.compose.runtime.mutableStateMapOf() },
    analysis: com.example.model.DraftAnalysisResult,
    selectedChampionDetail: Champion?,
    onSelectChampion: (Champion?) -> Unit,
    onOpenChampionPicker: (isAlly: Boolean, index: Int) -> Unit,
    onSaveDraftClick: () -> Unit,
    isSavedRecently: Boolean,
    onClearAll: () -> Unit,
    onManualEdit: () -> Unit,
    onOpenLiteRTViewer: () -> Unit = {}
) {
    val isPremium by com.example.util.SubscriptionManager.isPremium.collectAsStateWithLifecycle()

    val defaultRoles = remember { listOf(LaneRole.TOP, LaneRole.JUNGLE, LaneRole.MID, LaneRole.ADC, LaneRole.SUPPORT) }

    val explicitEnemyOpponent = remember(activeRole, enemies.toList(), isLoadingScreenMode) {
        if (isLoadingScreenMode) {
            val roleIndex = defaultRoles.indexOf(activeRole).coerceIn(0, 4)
            enemies.getOrNull(roleIndex)
        } else {
            enemies.filterNotNull().find { it.primaryRole == activeRole }
        }
    }

    val allySlots = remember(allies.toList(), allySpells.toMap()) {
        allies.mapIndexedNotNull { index, champ ->
            val role = defaultRoles.getOrElse(index) { LaneRole.MID }
            champ?.let {
                DraftSlot(
                    champion = it,
                    assignedRole = role,
                    summonerName = null,
                    spells = allySpells[index] ?: emptyList()
                )
            }
        }
    }
    val enemySlots = remember(enemies.toList(), enemyConfidences.toMap(), enemySpells.toMap(), enemySummonerNames.toMap()) {
        // En Wild Rift el orden de líneas del rival está oculto en el draft. Se deduce por afinidad de rol primario o flex
        val availableRoles = defaultRoles.toMutableList()
        val assignedList = mutableListOf<DraftSlot>()
        enemies.filterNotNull().forEachIndexed { index, champ ->
            val targetRole = if (availableRoles.contains(champ.primaryRole)) {
                champ.primaryRole
            } else {
                champ.secondaryRoles.firstOrNull { availableRoles.contains(it) } ?: availableRoles.firstOrNull() ?: champ.primaryRole
            }
            availableRoles.remove(targetRole)
            val conf = enemyConfidences[targetRole] ?: 85
            assignedList.add(
                DraftSlot(
                    champion = champ,
                    assignedRole = targetRole,
                    confidence = conf,
                    summonerName = enemySummonerNames[index],
                    spells = enemySpells[index] ?: emptyList()
                )
            )
        }
        assignedList
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 4.dp)
    ) {
        // TABLERO DE DRAFT VERSUS (ALIADO VS RIVAL POR LÍNEAS)
        OverlayVersusDraftBoard(
            allySlots = allySlots,
            enemySlots = enemySlots,
            allySummonerNames = allySummonerNames.toMap(),
            activeUserRole = activeRole,
            isFirstPick = isFirstPick,
            onToggleFirstPick = onFirstPickToggle,
            isLegendary = isLegendaryQueue,
            onToggleLegendary = onToggleLegendaryQueue,
            onOpenLiteRTViewer = onOpenLiteRTViewer,
            onPickChampionForRole = { isAlly, role ->
                val index = defaultRoles.indexOf(role).coerceAtLeast(0)
                onOpenChampionPicker(isAlly, index)
            },
            onRemoveChampionForRole = { isAlly, role ->
                val roleIndex = defaultRoles.indexOf(role)
                if (roleIndex in 0 until 5) {
                    if (isAlly) {
                        allies[roleIndex] = null
                    } else {
                        enemies[roleIndex] = null
                        enemyConfidences.remove(role)
                    }
                    onManualEdit()
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // CONTENIDO DEL COACH (CONTROLES Y ANÁLISIS)
        CoachContent(
            allies = allies,
            enemies = enemies,
            activeRole = activeRole,
            onActiveRoleChange = onActiveRoleChange,
            isFirstPick = isFirstPick,
            onFirstPickToggle = onFirstPickToggle,
            analysis = analysis,
            explicitEnemyOpponent = explicitEnemyOpponent,
            onSelectChampion = onSelectChampion,
            onSaveDraftClick = onSaveDraftClick,
            isSavedRecently = isSavedRecently,
            onClearAll = onClearAll,
            isPremium = isPremium
        )
    }
}

@Composable
private fun OverlayVersusDraftBoard(
    allySlots: List<DraftSlot>,
    enemySlots: List<DraftSlot>,
    allySummonerNames: Map<Int, String> = emptyMap(),
    activeUserRole: LaneRole?,
    isFirstPick: Boolean = true,
    onToggleFirstPick: (() -> Unit)? = null,
    isLegendary: Boolean = false,
    onToggleLegendary: (() -> Unit)? = null,
    onOpenLiteRTViewer: (() -> Unit)? = null,
    onPickChampionForRole: (isAlly: Boolean, LaneRole) -> Unit,
    onRemoveChampionForRole: (isAlly: Boolean, LaneRole) -> Unit
) {
    val roles = listOf(
        Pair(LaneRole.TOP, "TOP"),
        Pair(LaneRole.JUNGLE, "JUG"),
        Pair(LaneRole.MID, "MID"),
        Pair(LaneRole.ADC, "DÚO"),
        Pair(LaneRole.SUPPORT, "SUP")
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        border = BorderStroke(1.dp, HextechCardBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
            // Etiquetas de Primera Selección y Clasificatoria Legendaria
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.clickable { onToggleFirstPick?.invoke() },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isFirstPick) AllyBlue.copy(alpha = 0.15f) else DangerRed.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (isFirstPick) AllyBlue.copy(alpha = 0.6f) else DangerRed.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (isFirstPick) AllyBlue else DangerRed)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = com.example.util.tr(if (isFirstPick) tr("1ª Selección: Aliados") else tr("1ª Selección: Rival")),
                            color = if (isFirstPick) AllyBlue else DangerRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    modifier = Modifier.clickable { onToggleLegendary?.invoke() },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isLegendary) Color(0xFFFF9800).copy(alpha = 0.2f) else HextechDarkBg,
                    border = BorderStroke(
                        1.dp,
                        if (isLegendary) Color(0xFFFF9800) else HextechCardBorder.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = com.example.util.tr(if (isLegendary) tr("Legendaria") else tr("Clasificatoria")),
                            color = if (isLegendary) Color(0xFFFFB74D) else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp
                        )
                    }
                }
            }

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp, top = 2.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleFirstPick?.invoke() }
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AllyBlue))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("EQUIPO ALIADO"), color = AllyBlue, fontWeight = FontWeight.Black, fontSize = 10.5.sp)
                    if (isFirstPick) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AllyBlue.copy(alpha = 0.2f),
                            border = BorderStroke(0.5.dp, AllyBlue)
                        ) {
                            Text(
                                text = tr("1ª SELECCIÓN"),
                                color = AllyBlue,
                                fontWeight = FontWeight.Black,
                                fontSize = 7.5.sp,
                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                            )
                        }
                    }
                }

                Surface(
                    color = HextechDarkBg,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(0.5.dp, HextechGold.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable { onToggleFirstPick?.invoke() }
                ) {
                    Text(
                        com.example.util.tr("VS"),
                        color = HextechGold,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleFirstPick?.invoke() }
                ) {
                    if (!isFirstPick) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = DangerRed.copy(alpha = 0.2f),
                            border = BorderStroke(0.5.dp, DangerRed)
                        ) {
                            Text(
                                text = tr("1ª SELECCIÓN"),
                                color = DangerRed,
                                fontWeight = FontWeight.Black,
                                fontSize = 7.5.sp,
                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(tr("EQUIPO RIVAL"), color = DangerRed, fontWeight = FontWeight.Black, fontSize = 10.5.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(DangerRed))
                }
            }

            roles.forEachIndexed { index, (role, label) ->
                val allySlot = allySlots.find { it.assignedRole == role }
                val enemySlot = enemySlots.find { it.assignedRole == role }
                val isMyRole = activeUserRole == role
                val allyChamp = allySlot?.champion
                val enemyChamp = enemySlot?.champion

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    color = if (isMyRole) HextechCyan.copy(alpha = 0.08f) else HextechDarkBg.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        if (isMyRole) 1.dp else 0.5.dp,
                        if (isMyRole) HextechCyan.copy(alpha = 0.6f) else HextechCardBorder.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // LADO ALIADO (Avatar + 1. Nombre -> 2. Stats (WR/Ban/Pick) -> 3. Tier List)
                        Row(
                            modifier = Modifier
                                .weight(1.3f)
                                .clickable { onPickChampionForRole(true, role) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            DraftAvatarBox(
                                slot = allySlot,
                                placeholderInitial = null,
                                isEnemy = false,
                                isMyRole = isMyRole,
                                onClick = { onPickChampionForRole(true, role) },
                                onRemove = { onRemoveChampionForRole(true, role) }
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            if (allyChamp != null) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    // 1. Nombre del Campeón
                                    Text(
                                        text = com.example.util.tr(allyChamp.name),
                                        color = if (isMyRole) HextechCyan else TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    // 2. Estadísticas (WR, Ban, Pick)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Text(
                                            text = com.example.util.tr("W:${allyChamp.winrate.toInt()}%"),
                                            color = Color(0xFF00FF7F),
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = com.example.util.tr("B:${allyChamp.banRate.toInt()}%"),
                                            color = DangerRed,
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = com.example.util.tr("P:${allyChamp.pickRate.toInt()}%"),
                                            color = Color(0xFFFF9800),
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                    // 3. Tier List
                                    Text(
                                        text = com.example.util.tr("Tier ${allyChamp.tier}"),
                                        color = HextechGold,
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Text(
                                    text = tr("+ Elegir"),
                                    color = AllyBlue.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }

                        // CENTRO: ÍCONO Y ETIQUETA DEL ROL + VS
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .widthIn(min = 40.dp)
                        ) {
                            Image(
                                painter = painterResource(id = role.iconResId),
                                contentDescription = label,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = com.example.util.tr(label),
                                color = if (isMyRole) HextechCyan else TextSecondary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = com.example.util.tr("VS"),
                                color = HextechGold.copy(alpha = 0.7f),
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // LADO RIVAL (1. Nombre -> 2. Stats (WR/Ban/Pick) -> 3. Tier List + Avatar)
                        Row(
                            modifier = Modifier
                                .weight(1.3f)
                                .clickable { onPickChampionForRole(false, role) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (enemyChamp != null) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 6.dp),
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    // 1. Nombre del Campeón
                                    Text(
                                        text = com.example.util.tr(enemyChamp.name),
                                        color = DangerRed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.End
                                    )
                                    // 2. Estadísticas (WR, Ban, Pick)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text(
                                            text = com.example.util.tr("W:${enemyChamp.winrate.toInt()}%"),
                                            color = Color(0xFF00FF7F),
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = com.example.util.tr("B:${enemyChamp.banRate.toInt()}%"),
                                            color = DangerRed,
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = com.example.util.tr("P:${enemyChamp.pickRate.toInt()}%"),
                                            color = Color(0xFFFF9800),
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                    // 3. Tier List
                                    Text(
                                        text = com.example.util.tr("Tier ${enemyChamp.tier}"),
                                        color = HextechGold,
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.End
                                    )
                                    if (enemyChamp.secondaryRoles.isNotEmpty()) {
                                        val otherRoles = enemyChamp.secondaryRoles.joinToString("/") { it.shortName }
                                        Text(
                                            text = com.example.util.tr("FLEX ($otherRoles)"),
                                            color = HextechCyan,
                                            fontSize = 6.5.sp,
                                            fontWeight = FontWeight.Black,
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = tr("+ Rival"),
                                    color = DangerRed.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    softWrap = false,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            }

                            DraftAvatarBox(
                                slot = enemySlot,
                                placeholderInitial = null,
                                isEnemy = true,
                                isMyRole = false,
                                onClick = { onPickChampionForRole(false, role) },
                                onRemove = { onRemoveChampionForRole(false, role) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DraftAvatarBox(
    slot: DraftSlot?,
    placeholderInitial: String? = null,
    isEnemy: Boolean,
    isMyRole: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val champ = slot?.champion
    val borderColor = if (isMyRole) HextechCyan else if (champ != null) (if (isEnemy) DangerRed else HextechGold) else HextechCardBorder.copy(alpha = 0.6f)

    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isMyRole -> HextechCyan.copy(alpha = 0.2f)
                    champ != null -> if (isEnemy) DangerRed.copy(alpha = 0.15f) else HextechGold.copy(alpha = 0.15f)
                    else -> Color(0xFF070D15)
                }
            )
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (champ != null) {
            AppAssetImage(
                url = champ.avatarUrl,
                contentDescription = champ.name,
                fallbackText = champ.name,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
            )
            if (isMyRole) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(1.5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(HextechCyan)
                        .padding(horizontal = 2.5.dp, vertical = 0.5.dp)
                ) {
                    Text(
                        text = com.example.util.tr("TÚ"),
                        color = Color.Black,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(15.dp)
                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(bottomStart = 6.dp))
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Quitar"), tint = Color.White, modifier = Modifier.size(11.dp))
            }
        } else if (!placeholderInitial.isNullOrBlank()) {
            Box(
                modifier = Modifier.fillMaxSize().background(HextechSurface.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = com.example.util.tr(placeholderInitial),
                    color = HextechCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        } else {
            Icon(
                Icons.Default.Add,
                contentDescription = com.example.util.trNullable("Añadir"),
                tint = if (isEnemy) DangerRed.copy(alpha = 0.5f) else AllyBlue.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun CoachContent(
    allies: List<com.example.model.Champion?>,
    enemies: List<com.example.model.Champion?>,
    activeRole: LaneRole,
    onActiveRoleChange: (LaneRole) -> Unit,
    isFirstPick: Boolean,
    onFirstPickToggle: () -> Unit,
    analysis: com.example.model.DraftAnalysisResult,
    explicitEnemyOpponent: Champion?,
    onSelectChampion: (Champion?) -> Unit,
    onSaveDraftClick: () -> Unit,
    isSavedRecently: Boolean,
    onClearAll: () -> Unit,
    isPremium: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 2. SELECTOR DE MI ROL / LÍNEA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            LaneRole.entries.forEach { role ->
                val isSelected = activeRole == role
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) HextechCyan else HextechSurface)
                        .border(1.dp, if (isSelected) HextechGold else HextechCardBorder, RoundedCornerShape(6.dp))
                        .clickable { onActiveRoleChange(role) }
                        .padding(vertical = 3.5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = role.iconResId),
                            contentDescription = null,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = tr(role.shortName),
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                            color = if (isSelected) HextechDarkBg else TextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = com.example.util.tr(tr("RECOMENDACIÓN:") + " ${tr(activeRole.displayName)}"),
                color = HextechGold,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = com.example.util.tr(if (isFirstPick) tr("1ª Elección") else tr("Counter Pick")),
                color = if (isFirstPick) HextechGold else HextechCyan,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(HextechSurface)
                    .border(0.5.dp, if (isFirstPick) HextechGold else HextechCyan, RoundedCornerShape(4.dp))
                    .clickable { onFirstPickToggle() }
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }

        // Sinergias (Wombos)
        val allyWombos = remember(allies.toList()) { WomboComboSynergyDetector.detectWombos(allies.filterNotNull()) }

        if (allyWombos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                allyWombos.forEach { wombo ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = HextechDarkBg.copy(alpha = 0.6f)),
                        border = BorderStroke(0.5.dp, AllyBlue)
                    ) {
                        Text(text = com.example.util.tr("${wombo.title}: ${wombo.description}"), color = AllyBlue, fontSize = 8.5.sp, modifier = Modifier.padding(3.dp))
                    }
                }
            }
        }

        // Distribución de Daño del Draft (Aliados vs Enemigos)
        if (allies.any { it != null } || enemies.any { it != null }) {
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(HextechSurface)
                    .border(0.5.dp, HextechCardBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = com.example.util.tr("Daño Aliado: AD ${analysis.allyPhysicalDamagePercent}% | AP ${analysis.allyMagicDamagePercent}%"),
                        color = AllyBlue,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = com.example.util.tr("Daño Enemigo: AD ${analysis.physicalDamagePercent}% | AP ${analysis.magicDamagePercent}%"),
                        color = DangerRed,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                val winrateDisplay = (analysis.bestOverallPick?.estimatedWinrate ?: analysis.recommendations.firstOrNull()?.estimatedWinrate ?: 50.0).toInt()
                Text(
                    text = com.example.util.tr("WR Estimado: ${winrateDisplay}%"),
                    color = HextechGold,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Alerta táctica del Coach / Win condition
        if (!analysis.directMatchupWarning.isNullOrBlank() || !analysis.allyCompositionWarning.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            val warningText = analysis.directMatchupWarning ?: analysis.allyCompositionWarning ?: ""
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = HextechGold.copy(alpha = 0.12f)),
                border = BorderStroke(0.8.dp, HextechGold.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = com.example.util.tr(warningText),
                        color = HextechGold,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 10.sp
                    )
                }
            }
        }

        // Análisis 1v1 de línea / Matchup Directo con Rival
        if (explicitEnemyOpponent != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = HextechSurface),
                border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ChampionAvatar(champion = explicitEnemyOpponent, size = 22.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = com.example.util.tr(tr("Matchup 1v1 vs") + " ${explicitEnemyOpponent.name}"),
                            color = DangerRed,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = com.example.util.tr(analysis.directMatchupWarning ?: "Analizando ventana de poder en línea contra ${explicitEnemyOpponent.name}."),
                        color = TextPrimary,
                        fontSize = 8.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Acciones del draft; la lista de tiers se abre desde la navegación del hub.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = onSaveDraftClick,
                modifier = Modifier.weight(1.1f).height(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSavedRecently) Color(0xFF00FF7F).copy(alpha = 0.2f) else HextechGold.copy(alpha = 0.15f)
                ),
                border = BorderStroke(1.dp, if (isSavedRecently) Color(0xFF00FF7F) else HextechGold),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = com.example.util.tr(if (isSavedRecently) tr("Guardado") else tr("Guardar")),
                        color = if (isSavedRecently) Color(0xFF00FF7F) else HextechGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (!isPremium) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(HextechGold)
                                .padding(horizontal = 2.5.dp, vertical = 0.5.dp)
                        ) {
                            Text(com.example.util.tr("PRO"), color = HextechDarkBg, fontSize = 6.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            Button(
                onClick = onClearAll,
                modifier = Modifier.weight(0.9f).height(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.7f)),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Text(
                    text = tr("Vaciar"),
                    color = DangerRed,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 3. MEJORES PICKS RECOMENDADOS POR EL COACH
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            analysis.recommendations.take(4).forEach { pick ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectChampion(pick.champion) },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                    border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChampionAvatar(champion = pick.champion, size = 34.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(com.example.util.tr(pick.champion.name), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(TierSPlusColor)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(com.example.util.tr(pick.champion.tier), color = Color.Black, fontSize = 7.5.sp, fontWeight = FontWeight.Black)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(com.example.util.tr("WR: ${pick.estimatedWinrate}%"), color = HextechGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(tr(pick.advantageBadge), color = HextechCyan, fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold)
                            Text(tr(pick.tacticalReason), color = TextMuted, fontSize = 8.sp, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
                        }
                    }

                }
            }
        }
    }
}
