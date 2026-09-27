package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AccountProfileManager
import com.example.model.Champion
import com.example.model.LaneRole
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechGoldLight
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.HextechSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.tr

@Composable
fun SaveDraftDialog(
    isOverlay: Boolean = false,
    myChampion: Champion?,
    enemyLaneOpponent: Champion?,
    userRole: LaneRole,
    estimatedWinrate: Double,
    onDismiss: () -> Unit,
    onSave: (result: String, notes: String, profileId: String, profileName: String, isLegendary: Boolean, matchMode: String, myScore: String) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val activeProfile = remember { AccountProfileManager.getActiveProfile(context) }
    val allProfiles = remember { AccountProfileManager.allProfiles.value }
    var selectedProfile by remember { mutableStateOf(activeProfile) }
    var profileDropdownExpanded by remember { mutableStateOf(false) }

    var selectedResult by remember { mutableStateOf("PENDING") } // "PENDING", "VICTORY", "DEFEAT"
    var selectedMatchMode by remember { mutableStateOf("RANKED") } // "RANKED", "LEGENDARY", "NORMAL"
    val isLegendaryMatch = selectedMatchMode == "LEGENDARY"
    var myScore by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val dialogContent = @Composable {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(18.dp))
                .border(1.5.dp, HextechGold.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
                .testTag("save_draft_dialog"),
            colors = CardDefaults.cardColors(containerColor = HextechDarkBg.copy(alpha = 0.98f)),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(HextechGold.copy(alpha = 0.15f))
                                .border(1.dp, HextechGold.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdd,
                                contentDescription = null,
                                tint = HextechGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = tr("Guardar en Historial"),
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tr("Registra el resultado de la partida"),
                                color = HextechCyan,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = tr("Cerrar"),
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Account Profile Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(HextechSurface)
                            .border(1.dp, HextechGold.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .clickable { profileDropdownExpanded = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = HextechGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = tr("Perfil / Cuenta:"),
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = selectedProfile.name,
                                        color = HextechGold,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (selectedProfile.tag.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "#${selectedProfile.tag}",
                                            color = HextechCyan,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = tr("Cambiar ▼"),
                            color = HextechCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    DropdownMenu(
                        expanded = profileDropdownExpanded,
                        onDismissRequest = { profileDropdownExpanded = false },
                        modifier = Modifier.background(HextechSurface)
                    ) {
                        allProfiles.forEach { profile ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = profile.name,
                                            color = if (profile.id == selectedProfile.id) HextechGold else TextPrimary,
                                            fontWeight = if (profile.id == selectedProfile.id) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                        if (profile.tag.isNotBlank()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "#${profile.tag}",
                                                color = HextechCyan,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedProfile = profile
                                    profileDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Matchup Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                    border = BorderStroke(1.dp, HextechCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (myChampion != null) {
                                AppAssetImage(
                                    url = myChampion.avatarUrl,
                                    contentDescription = myChampion.name,
                                    fallbackText = myChampion.name.take(2).uppercase(),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, HextechGold, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Column {
                                Text(
                                    text = if (myChampion != null && enemyLaneOpponent != null) {
                                        "${myChampion.name} vs ${enemyLaneOpponent.name}"
                                    } else if (myChampion != null) {
                                        myChampion.name
                                    } else {
                                        tr("Draft de Equipo")
                                    },
                                    color = TextPrimary,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = HextechGold,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = tr(userRole.displayName),
                                        color = HextechGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${estimatedWinrate.toInt()}% WR",
                            color = HextechCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(HextechCyan.copy(alpha = 0.12f))
                                .border(1.dp, HextechCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Match Mode Selector (Clasificatoria Normal, Legendaria, Normal)
                Text(
                    text = tr("Tipo de Partida"),
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val modes = listOf(
                        Triple("RANKED", tr("Clasificatoria"), HextechGold),
                        Triple("LEGENDARY", tr("Legendaria"), Color(0xFFAB47BC)),
                        Triple("NORMAL", tr("Normal"), HextechCyan)
                    )
                    modes.forEach { (mode, label, accentColor) ->
                        val isSel = selectedMatchMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) accentColor.copy(alpha = 0.22f) else HextechSurface)
                                .border(1.dp, if (isSel) accentColor else HextechCardBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedMatchMode = mode }
                                .padding(vertical = 8.dp, horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) accentColor else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Result selector title
                Text(
                    text = tr("¿Cuál fue el resultado de la partida?"),
                    color = TextPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Result Buttons / Cards (3 Options: En espera, Victoria, Derrota)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Option 1: En espera (Default)
                    val isPendingSelected = selectedResult == "PENDING"
                    val pendingScale by animateFloatAsState(
                        targetValue = if (isPendingSelected) 1.03f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                        label = "pending_scale"
                    )
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .scale(pendingScale)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (isPendingSelected) 1.5.dp else 1.dp,
                                color = if (isPendingSelected) HextechGold else HextechCardBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedResult = "PENDING" }
                            .testTag("save_result_pending"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPendingSelected) HextechGold.copy(alpha = 0.22f) else HextechSurface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = tr("En espera"),
                                color = if (isPendingSelected) HextechGold else TextSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = if (isPendingSelected) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }

                    // Option 2: Victoria
                    val isVictorySelected = selectedResult == "VICTORY"
                    val victoryScale by animateFloatAsState(
                        targetValue = if (isVictorySelected) 1.03f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                        label = "victory_scale"
                    )
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .scale(victoryScale)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (isVictorySelected) 1.5.dp else 1.dp,
                                color = if (isVictorySelected) Color(0xFF81C784) else HextechCardBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedResult = "VICTORY" }
                            .testTag("save_result_victory"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isVictorySelected) Color(0xFF2E7D32).copy(alpha = 0.35f) else HextechSurface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = tr("Victoria"),
                                color = if (isVictorySelected) Color(0xFF81C784) else TextSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = if (isVictorySelected) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }

                    // Option 3: Derrota
                    val isDefeatSelected = selectedResult == "DEFEAT"
                    val defeatScale by animateFloatAsState(
                        targetValue = if (isDefeatSelected) 1.03f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                        label = "defeat_scale"
                    )
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .scale(defeatScale)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (isDefeatSelected) 1.5.dp else 1.dp,
                                color = if (isDefeatSelected) DangerRed else HextechCardBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedResult = "DEFEAT" }
                            .testTag("save_result_defeat"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDefeatSelected) DangerRed.copy(alpha = 0.25f) else HextechSurface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = tr("Derrota"),
                                color = if (isDefeatSelected) DangerRed else TextSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = if (isDefeatSelected) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Score Field (Optional)
                OutlinedTextField(
                    value = myScore,
                    onValueChange = { myScore = it },
                    label = { Text(tr("Tu Score / KDA (Opcional)"), fontSize = 11.5.sp) },
                    placeholder = { Text(tr("Ej: 12/2/8 o 5.0 KDA"), fontSize = 11.sp, color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HextechGold,
                        unfocusedBorderColor = HextechCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = HextechSurface,
                        unfocusedContainerColor = HextechSurface,
                        focusedLabelColor = HextechGold,
                        unfocusedLabelColor = TextMuted
                    ),
                    singleLine = true,
                    supportingText = {
                        Text(
                            text = tr("Refina el cálculo automático de tu Tier List Personal y enfrentamientos 1v1."),
                            fontSize = 10.sp,
                            color = HextechCyan
                        )
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(tr("Notas o recordatorio (opcional)"), fontSize = 11.5.sp) },
                    placeholder = { Text(tr("Ej: Buen counter en early, build con cortacuras..."), fontSize = 11.sp, color = TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HextechGold,
                        unfocusedBorderColor = HextechCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = HextechSurface,
                        unfocusedContainerColor = HextechSurface,
                        focusedLabelColor = HextechGold,
                        unfocusedLabelColor = TextMuted
                    ),
                    maxLines = 2,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, HextechCardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
                    ) {
                        Text(text = tr("Cancelar"), fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSave(selectedResult, notes, selectedProfile.id, selectedProfile.name, isLegendaryMatch, selectedMatchMode, myScore)
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(42.dp)
                            .testTag("confirm_save_draft_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (selectedResult) {
                                "VICTORY" -> Color(0xFF2E7D32)
                                "DEFEAT" -> DangerRed.copy(alpha = 0.85f)
                                else -> HextechGold
                            },
                            contentColor = when (selectedResult) {
                                "VICTORY" -> Color.White
                                "DEFEAT" -> Color.White
                                else -> HextechDarkBg
                            }
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tr("Guardar"),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (isOverlay) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = {}
            )) {
                dialogContent()
            }
        }
    } else {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            dialogContent()
        }
    }
}

