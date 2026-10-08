package com.example.service

import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.example.data.repository.DraftHistoryRepository
import kotlinx.coroutines.launch
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DraftSlot
import com.example.model.LaneRole
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
import com.example.util.tr

@Composable
internal fun FloatingSaveMatchDialog(
    activeRole: LaneRole,
    isFirstPick: Boolean,
    isLegendary: Boolean = false,
    allies: List<DraftSlot>,
    enemies: List<DraftSlot>,
    analysis: com.example.model.DraftAnalysisResult,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val currentLang = com.example.util.currentAppLanguage()
    val coroutineScope = rememberCoroutineScope()
    var selectedResult by remember { mutableStateOf("PENDING") }
    var selectedMatchMode by remember(isLegendary) { mutableStateOf(if (isLegendary) "LEGENDARY" else "RANKED") }
    val isLegendaryMatch = selectedMatchMode == "LEGENDARY"
    var myScoreText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var showDuplicateConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        com.example.data.AccountProfileManager.init(context)
    }

    val profiles by com.example.data.AccountProfileManager.allProfiles.collectAsState()
    val activeProfileId by com.example.data.AccountProfileManager.activeProfileId.collectAsState()
    var selectedProfileId by remember(activeProfileId) { mutableStateOf(activeProfileId) }

    val myChampion = allies.find { it.assignedRole == activeRole }?.champion ?: allies.firstOrNull()?.champion
    val enemyOpponent = enemies.find { it.assignedRole == activeRole }?.champion ?: enemies.firstOrNull()?.champion
    val winrateDisplay = (analysis.bestOverallPick?.estimatedWinrate ?: analysis.recommendations.firstOrNull()?.estimatedWinrate ?: 50.0).toInt()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .padding(10.dp)
            .pointerInput(Unit) { },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 480.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
            border = BorderStroke(1.5.dp, HextechGold)
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr("Guardar en Historial"),
                        color = HextechGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = tr("Cerrar"), tint = TextMuted)
                    }
                }

                // Matchup summary badge
                if (myChampion != null || enemyOpponent != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(HextechSurface)
                            .border(1.dp, HextechCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${myChampion?.getLocalizedName(currentLang) ?: com.example.util.tr("Mi Pick")} (${activeRole.shortName})",
                                color = AllyBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            if (enemyOpponent != null) {
                                Text(com.example.util.tr(" vs "), color = TextMuted, fontSize = 10.sp)
                                Text(
                                    text = com.example.util.tr(enemyOpponent.name),
                                    color = DangerRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Text(
                            text = com.example.util.tr("WR: $winrateDisplay%"),
                            color = HextechGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        )
                    }
                }

                // Perfil de Cuenta
                if (profiles.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tr("Perfil / Cuenta:"),
                        color = TextPrimary,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        profiles.take(3).forEach { profile ->
                            val isSelected = selectedProfileId == profile.id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) HextechCyan.copy(alpha = 0.25f) else HextechSurface)
                                    .border(1.dp, if (isSelected) HextechCyan else HextechCardBorder, RoundedCornerShape(6.dp))
                                    .clickable { selectedProfileId = profile.id }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = com.example.util.tr(profile.name),
                                    color = if (isSelected) HextechCyan else TextPrimary,
                                    fontSize = 9.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1, softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = tr("Tipo de Partida:"),
                    color = TextPrimary,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) accentColor.copy(alpha = 0.25f) else HextechSurface)
                                .border(1.dp, if (isSel) accentColor else HextechCardBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedMatchMode = mode }
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = com.example.util.tr(label),
                                color = if (isSel) accentColor else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = tr("Resultado de la Partida:"),
                    color = TextPrimary,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isPending = selectedResult == "PENDING" || selectedResult == "IN_PROGRESS"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isPending) HextechGold.copy(alpha = 0.25f) else HextechSurface)
                            .border(1.5.dp, if (isPending) HextechGold else HextechCardBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedResult = "PENDING" }
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tr("En espera"),
                            color = if (isPending) HextechGold else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        )
                    }
                    val isVic = selectedResult == "VICTORY"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isVic) Color(0xFF00FF7F).copy(alpha = 0.25f) else HextechSurface)
                            .border(1.5.dp, if (isVic) Color(0xFF00FF7F) else HextechCardBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedResult = "VICTORY" }
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tr("Victoria"),
                            color = if (isVic) Color(0xFF00FF7F) else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        )
                    }
                    val isDef = selectedResult == "DEFEAT"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDef) DangerRed.copy(alpha = 0.25f) else HextechSurface)
                            .border(1.5.dp, if (isDef) DangerRed else HextechCardBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedResult = "DEFEAT" }
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tr("Derrota"),
                            color = if (isDef) DangerRed else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = tr("Tu Score / KDA (Opcional):"),
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(3.dp))
                OutlinedTextField(
                    value = myScoreText,
                    onValueChange = { myScoreText = it },
                    placeholder = { Text(tr("Ej: 12/2/8 o 5.0 KDA"), fontSize = 9.5.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HextechGold,
                        unfocusedBorderColor = HextechCardBorder
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = tr("Notas tácticas / Matchup:"),
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(3.dp))
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = { Text(tr("Ej: Matchup ganado en nivel 3, priorizar cortar curaciones..."), fontSize = 9.5.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HextechCyan,
                        unfocusedBorderColor = HextechCardBorder
                    ),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (!isSaving) {
                            isSaving = true
                            coroutineScope.launch {
                                val chosenProfile = profiles.find { it.id == selectedProfileId }
                                    ?: com.example.data.AccountProfileManager.getActiveProfile(context)

                                val exists = DraftHistoryRepository.checkDraftExists(
                                    context = context,
                                    myRole = activeRole,
                                    allies = allies,
                                    enemies = enemies,
                                    accountProfileId = chosenProfile.id
                                )

                                if (exists) {
                                    showDuplicateConfirmation = true
                                    isSaving = false
                                } else {
                                    DraftHistoryRepository.saveDraft(
                                        context = context,
                                        myRole = activeRole,
                                        isFirstPick = isFirstPick,
                                        isLegendary = selectedMatchMode == "LEGENDARY",
                                        matchMode = selectedMatchMode,
                                        myScore = myScoreText,
                                        allowDuplicate = false,
                                        allies = allies,
                                        enemies = enemies,
                                        analysis = analysis,
                                        notes = notesText,
                                        matchResult = selectedResult,
                                        accountProfileId = chosenProfile.id,
                                        accountProfileName = chosenProfile.name
                                    )
                                    android.widget.Toast.makeText(context, com.example.util.appTr("¡Partida guardada en el historial!"), android.widget.Toast.LENGTH_SHORT).show()
                                    isSaving = false
                                    onSaved()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = HextechDarkBg, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = tr("Guardar y Actualizar Historial"),
                            color = HextechDarkBg,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        if (showDuplicateConfirmation) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDuplicateConfirmation = false },
                title = { Text(tr("Draft Duplicado"), fontWeight = FontWeight.Bold, color = TextPrimary) },
                text = { Text(tr("Es el mismo draft que el anterior, ¿deseas guardarlo de todas formas?"), color = TextSecondary) },
                containerColor = HextechSurface,
                confirmButton = {
                    Button(
                        onClick = {
                            showDuplicateConfirmation = false
                            isSaving = true
                            coroutineScope.launch {
                                val chosenProfile = profiles.find { it.id == selectedProfileId }
                                    ?: com.example.data.AccountProfileManager.getActiveProfile(context)
                                DraftHistoryRepository.saveDraft(
                                    context = context,
                                    myRole = activeRole,
                                    isFirstPick = isFirstPick,
                                    isLegendary = selectedMatchMode == "LEGENDARY",
                                    matchMode = selectedMatchMode,
                                    myScore = myScoreText,
                                    allowDuplicate = true,
                                    allies = allies,
                                    enemies = enemies,
                                    analysis = analysis,
                                    notes = notesText,
                                    matchResult = selectedResult,
                                    accountProfileId = chosenProfile.id,
                                    accountProfileName = chosenProfile.name
                                )
                                android.widget.Toast.makeText(context, com.example.util.appTr("¡Partida guardada en el historial!"), android.widget.Toast.LENGTH_SHORT).show()
                                isSaving = false
                                onSaved()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HextechGold)
                    ) {
                        Text(tr("Sí"), color = Color.Black)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDuplicateConfirmation = false },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
                    ) {
                        Text(tr("No"))
                    }
                }
            )
        }
    }
}
