package com.example.ui.screens

import com.example.ui.components.CoachButton as Button
import com.example.ui.components.CoachTextButton as TextButton
import com.example.ui.components.CoachIconButton as IconButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.ui.components.coachClickable
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AccountProfile
import com.example.data.local.entity.SavedDraftEntity
import com.example.data.repository.DraftHistoryRepository
import com.example.model.LaneRole
import com.example.ui.components.ChampionAvatar
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedSurface
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.HextechSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.tr
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun SavedDraftCard(
    draft: SavedDraftEntity,
    isOverlay: Boolean = false,
    onClick: () -> Unit,
    onLoad: () -> Unit,
    onUpdateResult: (String) -> Unit,
    onDelete: () -> Unit
) {
    val allies = remember(draft.allyPicksJson) { DraftHistoryRepository.parseDraftSlots(draft.allyPicksJson) }
    val enemies = remember(draft.enemyPicksJson) { DraftHistoryRepository.parseDraftSlots(draft.enemyPicksJson) }
    val formattedDate = remember(draft.timestamp) {
        val sdf = SimpleDateFormat("dd MMM • HH:mm", Locale.getDefault())
        sdf.format(Date(draft.timestamp))
    }

    var resultMenuExpanded by remember { mutableStateOf(false) }

    val roleObj = try { LaneRole.valueOf(draft.userRole) } catch (_: Exception) { LaneRole.MID }

    val resultBg = when (draft.matchResult.uppercase()) {
        "VICTORY" -> Color(0xFF81C784).copy(alpha = 0.18f)
        "DEFEAT" -> DangerRed.copy(alpha = 0.18f)
        else -> HextechGold.copy(alpha = 0.18f)
    }
    val resultBorder = when (draft.matchResult.uppercase()) {
        "VICTORY" -> Color(0xFF81C784)
        "DEFEAT" -> DangerRed
        else -> HextechGold
    }
    val resultLabel = when (draft.matchResult.uppercase()) {
        "VICTORY" -> "👑 " + tr("Victoria")
        "DEFEAT" -> "💔 " + tr("Derrota")
        else -> "⏳ " + tr("En espera")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .coachClickable { onClick() }
            .testTag("saved_draft_card_${draft.id}"),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        border = BorderStroke(1.dp, HextechCardBorder)
    ) {
        Column(modifier = Modifier.padding(if (isOverlay) 10.dp else 12.dp)) {
            // Header Row: Date & Result Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = HextechGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tr(roleObj.displayName),
                        color = HextechGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    val modeColor = when {
                        draft.isLegendary || draft.matchMode.equals("LEGENDARY", ignoreCase = true) -> Color(0xFFC084FC)
                        else -> HextechGold
                    }
                    val modeBg = when {
                        draft.isLegendary || draft.matchMode.equals("LEGENDARY", ignoreCase = true) -> Color(0xFF9333EA).copy(alpha = 0.25f)
                        else -> HextechGold.copy(alpha = 0.15f)
                    }
                    val modeLabel = when {
                        draft.isLegendary || draft.matchMode.equals("LEGENDARY", ignoreCase = true) -> tr("Legendaria")
                        else -> tr("Clasificatoria")
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Surface(
                        color = modeBg,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.8.dp, modeColor)
                    ) {
                        Text(
                            text = com.example.util.tr(modeLabel),
                            color = modeColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    if (draft.myScore.isNotBlank()) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Surface(
                            color = HextechGold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.8.dp, HextechGold.copy(alpha = 0.7f))
                        ) {
                            Text(
                                text = com.example.util.tr("Score: ${draft.myScore}"),
                                color = HextechGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    if (draft.accountProfileName.isNotBlank()) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Surface(
                            color = HextechCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(0.5.dp, HextechCyan.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = com.example.util.tr("👤 ${draft.accountProfileName}"),
                                color = HextechCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = com.example.util.tr("• $formattedDate"),
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                // Interactive Result Badge with dropdown
                Box {
                    Surface(
                        color = resultBg,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, resultBorder),
                        modifier = Modifier.coachClickable { resultMenuExpanded = true }
                    ) {
                        Text(
                            text = com.example.util.tr(resultLabel),
                            color = resultBorder,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = resultMenuExpanded,
                        onDismissRequest = { resultMenuExpanded = false },
                        modifier = Modifier.background(HextechSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text(com.example.util.tr("👑 " + tr("Victoria")), color = Color(0xFF81C784), fontWeight = FontWeight.Bold) },
                            onClick = {
                                onUpdateResult("VICTORY")
                                resultMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(com.example.util.tr("💔 " + tr("Derrota")), color = DangerRed, fontWeight = FontWeight.Bold) },
                            onClick = {
                                onUpdateResult("DEFEAT")
                                resultMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Champion matchup headline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = com.example.util.tr(draft.title),
                    color = TextPrimary,
                    fontSize = if (isOverlay) 12.5.sp else 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = com.example.util.tr("${draft.estimatedWinrate}% " + tr("WR Est.")),
                    color = HextechCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isOverlay) {
                // Team-by-team rows for compact overlay
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(HextechDarkBg.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Allies Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = com.example.util.tr("🔵"),
                            fontSize = 9.sp,
                            modifier = Modifier.width(18.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            allies.take(5).forEach { slot ->
                                ChampionAvatar(champion = slot.champion, size = 26.dp)
                            }
                        }
                    }
                    // Enemies Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = com.example.util.tr("🔴"),
                            fontSize = 9.sp,
                            modifier = Modifier.width(18.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            enemies.take(5).forEach { slot ->
                                ChampionAvatar(champion = slot.champion, size = 26.dp)
                            }
                        }
                    }
                }
            } else {
                // 5v5 Team Avatar Visualizer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(HextechDarkBg.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Allies
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        allies.take(5).forEach { slot ->
                            ChampionAvatar(
                                champion = slot.champion,
                                size = 30.dp
                            )
                        }
                    }

                    // VS Badge
                    Text(
                        text = com.example.util.tr("VS"),
                        color = DangerRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    // Enemies
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        enemies.take(5).forEach { slot ->
                            ChampionAvatar(
                                champion = slot.champion,
                                size = 30.dp
                            )
                        }
                    }
                }
            }

            // Notes preview if present
            if (draft.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = com.example.util.tr(" ${draft.notes}"),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.SportsKabaddi, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(tr("Ver Análisis"), color = HextechCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = tr("Eliminar"), tint = TextMuted, modifier = Modifier.size(15.dp))
                    }

                    Button(
                        onClick = onLoad,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HextechGold,
                            contentColor = HextechDarkBg
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(tr("Cargar Draft"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DraftDetailBottomSheet(
    isOverlay: Boolean = false,
    draft: SavedDraftEntity,
    profiles: List<AccountProfile>,
    onDismiss: () -> Unit,
    onLoad: () -> Unit,
    onSaveNotes: (String) -> Unit,
    onAssignProfile: (String, String) -> Unit
) {
    val context = LocalContext.current
    val effectiveOverlay = isOverlay || context.isOverlayOrNonActivity()

    if (effectiveOverlay) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.85f))
                .coachClickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .coachClickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                border = BorderStroke(1.5.dp, HextechGold)
            ) {
                DraftDetailInnerContent(
                    isOverlay = true,
                    draft = draft,
                    profiles = profiles,
                    onDismiss = onDismiss,
                    onLoad = onLoad,
                    onSaveNotes = onSaveNotes,
                    onAssignProfile = onAssignProfile
                )
            }
        }
    } else {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = HextechSurfaceVariant
        ) {
            DraftDetailInnerContent(
                isOverlay = false,
                draft = draft,
                profiles = profiles,
                onDismiss = onDismiss,
                onLoad = onLoad,
                onSaveNotes = onSaveNotes,
                onAssignProfile = onAssignProfile
            )
        }
    }
}

@Composable
private fun DraftDetailInnerContent(
    isOverlay: Boolean,
    draft: SavedDraftEntity,
    profiles: List<AccountProfile>,
    onDismiss: () -> Unit,
    onLoad: () -> Unit,
    onSaveNotes: (String) -> Unit,
    onAssignProfile: (String, String) -> Unit
) {
    val allies = remember(draft.allyPicksJson) { DraftHistoryRepository.parseDraftSlots(draft.allyPicksJson) }
    val enemies = remember(draft.enemyPicksJson) { DraftHistoryRepository.parseDraftSlots(draft.enemyPicksJson) }

    var userNotes by remember(draft.notes) { mutableStateOf(draft.notes) }
    var isEditingNotes by remember { mutableStateOf(false) }

    val roleObj = try { LaneRole.valueOf(draft.userRole) } catch (_: Exception) { LaneRole.MID }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isOverlay) 12.dp else 20.dp, vertical = if (isOverlay) 8.dp else 0.dp)
            .verticalScroll(rememberScrollState())
    ) {
        if (isOverlay) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(HextechSurface)
                        .coachClickable { onDismiss() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = HextechGold, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("Volver"), color = HextechGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.Close, contentDescription = tr("Cerrar"), tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = com.example.util.tr(draft.title),
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    val detailModeText = when {
                        draft.isLegendary || draft.matchMode.equals("LEGENDARY", ignoreCase = true) -> " • 🏆 " + tr("Legendaria")
                        else -> " • ⚔️ " + tr("Clasificatoria")
                    }
                    val detailScoreText = if (draft.myScore.isNotBlank()) " • 🏅 Score: ${draft.myScore}" else ""
                    Text(
                        text = com.example.util.tr(tr("Línea:") + " ${com.example.util.tr(roleObj.displayName)} • " + (if (draft.isFirstPick) tr("Primer Pick") else tr("Counter Pick")) + detailModeText + detailScoreText),
                        color = if (draft.isLegendary || draft.matchMode.equals("LEGENDARY", ignoreCase = true)) Color(0xFFC084FC) else HextechGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onLoad,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HextechGold,
                        contentColor = HextechDarkBg
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("Cargar en Selección"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Account Profile Pill & Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(HextechSurface)
                    .border(1.dp, HextechGold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var accountMenuExpanded by remember { mutableStateOf(false) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(tr("Cuenta Asignada:"), color = TextMuted, fontSize = 11.5.sp)
                }

                Box {
                    Surface(
                        color = HextechGold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f)),
                        modifier = Modifier.coachClickable { accountMenuExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = com.example.util.tr(if (draft.accountProfileName.isNotBlank()) draft.accountProfileName else tr("Principal")),
                                color = HextechGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.SwapHoriz, contentDescription = tr("Cambiar cuenta"), tint = HextechGold, modifier = Modifier.size(13.dp))
                        }
                    }

                    DropdownMenu(
                        expanded = accountMenuExpanded,
                        onDismissRequest = { accountMenuExpanded = false },
                        modifier = Modifier.background(HextechSurface)
                    ) {
                        profiles.forEach { prof ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(com.example.util.tr(prof.name), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        if (prof.tag.isNotBlank()) {
                                            Text(com.example.util.tr("#${prof.tag}"), color = HextechCyan, fontSize = 10.sp)
                                        }
                                    }
                                },
                                onClick = {
                                    accountMenuExpanded = false
                                    onAssignProfile(prof.id, prof.name)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Equipo Aliado
            Text(tr("Tu Equipo Aliado"), color = HextechCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                allies.forEach { slot ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (slot.assignedRole == roleObj) HextechGold.copy(alpha = 0.15f) else HextechSurface)
                            .border(1.dp, if (slot.assignedRole == roleObj) HextechGold else HextechCardBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ChampionAvatar(champion = slot.champion, size = 36.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(com.example.util.tr(slot.champion.name), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    if (slot.assignedRole == roleObj) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(tr("(Mío)"), color = HextechGold, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = slot.assignedRole.iconResId),
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = com.example.util.tr(tr(slot.assignedRole.displayName) + " • Tier ${slot.champion.tier}"),
                                        color = HextechCyan,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                        Text(
                            text = com.example.util.tr("${String.format(java.util.Locale.US, "%.2f", slot.champion.winrate)}% WR"),
                            color = HextechGold,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Equipo Rival
            Text(tr("Equipo Rival"), color = DangerRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                enemies.forEach { slot ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (slot.assignedRole == roleObj) DangerRedSurface else HextechSurface)
                            .border(1.dp, if (slot.assignedRole == roleObj) DangerRed else HextechCardBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ChampionAvatar(champion = slot.champion, size = 36.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(com.example.util.tr(slot.champion.name), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    if (slot.assignedRole == roleObj) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(com.example.util.tr("(" + tr("Rival Directo") + ")"), color = DangerRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = slot.assignedRole.iconResId),
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = com.example.util.tr(tr(slot.assignedRole.displayName) + " • Tier ${slot.champion.tier}"),
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                        Text(
                            text = com.example.util.tr("${String.format(java.util.Locale.US, "%.2f", slot.champion.winrate)}% WR"),
                            color = HextechGold,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Damage Balance
            Text(tr("Distribución de Daño Aliado"), color = HextechGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))) {
                if (draft.allyDamagePhysical > 0) Box(modifier = Modifier.weight(draft.allyDamagePhysical.toFloat()).fillMaxHeight().background(Color(0xFFE57373)))
                if (draft.allyDamageMagic > 0) Box(modifier = Modifier.weight(draft.allyDamageMagic.toFloat()).fillMaxHeight().background(Color(0xFF64B5F6)))
                if (draft.allyDamageTrue > 0) Box(modifier = Modifier.weight(draft.allyDamageTrue.toFloat()).fillMaxHeight().background(Color.White))
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(com.example.util.tr("${draft.allyDamagePhysical}% " + tr("Físico")), color = Color(0xFFE57373), fontSize = 9.5.sp)
                Text(com.example.util.tr("${draft.allyDamageMagic}% " + tr("Mágico")), color = Color(0xFF64B5F6), fontSize = 9.5.sp)
                Text(com.example.util.tr("${draft.allyDamageTrue}% " + tr("Verdadero")), color = Color.White, fontSize = 9.5.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Notes Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(tr("Notas Personales / Lecciones"), color = HextechGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                if (!isEditingNotes) {
                    TextButton(onClick = { isEditingNotes = true }) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(tr("Editar"), color = HextechCyan, fontSize = 11.sp)
                    }
                }
            }

            if (isEditingNotes) {
                OutlinedTextField(
                    value = userNotes,
                    onValueChange = { userNotes = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(tr("Escribe qué funcionó, errores o notas tácticas..."), color = TextMuted, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HextechGold,
                        unfocusedBorderColor = HextechCardBorder,
                        focusedContainerColor = HextechSurface,
                        unfocusedContainerColor = HextechSurface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = { isEditingNotes = false }) {
                        Text(tr("Cancelar"), color = TextMuted, fontSize = 11.5.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSaveNotes(userNotes)
                            isEditingNotes = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HextechGold, contentColor = HextechDarkBg),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(tr("Guardar Nota"), fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                    }
                }
            } else {
                Text(
                    text = com.example.util.tr(if (draft.notes.isNotBlank()) draft.notes else tr("Sin notas adicionales registradas.")),
                    color = if (draft.notes.isNotBlank()) TextPrimary else TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
}
