package com.example.ui.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Champion
import com.example.model.LaneRole
import com.example.ui.theme.*
import com.example.util.tr

/**
 * Comparador Rápido de Matchup 1v1 (Matchup Preview / Cara a Cara)
 * Muestra el enfrentamiento en línea con picos de poder nivel 1-5, escalado y consejos tácticos.
 */
@Composable
fun MatchupPreviewDialog(
    isOverlay: Boolean = false,
    myChampion: Champion,
    enemyOpponent: Champion,
    activeRole: LaneRole,
    onDismiss: () -> Unit
) {
    val language = com.example.util.currentAppLanguage()
    val coaching = remember(myChampion, enemyOpponent, activeRole, language) {
        com.example.util.ChampionMatchupCoaching.forDuel(myChampion, enemyOpponent, activeRole, language)
    }
    val relation = com.example.data.MatchupKnowledge.relation(myChampion,enemyOpponent)
    val isMyCounter = relation == com.example.data.MatchupRelation.FAVORABLE
    val isEnemyCounter = relation == com.example.data.MatchupRelation.UNFAVORABLE

    val matchupFavor = when {
        isMyCounter && !isEnemyCounter -> "FAVORABLE"
        isEnemyCounter && !isMyCounter -> "DESFAVORABLE"
        else -> "SKILL_MATCHUP"
    }

    val dialogContent = @Composable {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(18.dp))
                .border(1.5.dp, HextechGold, RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SportsKabaddi,
                            contentDescription = null,
                            tint = HextechGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = tr("Análisis de Enfrentamiento"),
                                color = HextechGold,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp
                            )
                            Text(
                                text = com.example.util.tr("${tr("Enfrentamiento en")} ${tr(activeRole.displayName)}"),
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Cerrar"), tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Face to Face Visual Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechSurface),
                        border = BorderStroke(1.dp, HextechCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Mi Campeón
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    ChampionAvatar(champion = myChampion, size = 52.dp, showTierBadge = true)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = com.example.util.tr(myChampion.name),
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = tr("Tu Pick (Aliado)"),
                                        color = AllyBlue,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = com.example.util.tr("WR: ${String.format(java.util.Locale.US, "%.2f", myChampion.winrate)}%"),
                                        color = HextechGold,
                                        fontSize = 10.sp
                                    )
                                }

                                // VS Emblem & Matchup Status
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (matchupFavor) {
                                                    "FAVORABLE" -> Color(0xFF1B5E20)
                                                    "DESFAVORABLE" -> Color(0xFFB71C1C)
                                                    else -> Color(0xFFE65100)
                                                }
                                            )
                                            .border(1.dp, HextechGold, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = com.example.util.tr("VS"),
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = com.example.util.tr(when (matchupFavor) {
                                            "FAVORABLE" -> tr("Favorable")
                                            "DESFAVORABLE" -> tr("Difícil")
                                            else -> tr("Habilidad")
                                        }),
                                        color = when (matchupFavor) {
                                            "FAVORABLE" -> Color(0xFF4CAF50)
                                            "DESFAVORABLE" -> DangerRed
                                            else -> HextechGold
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }

                                // Rival de Línea
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    ChampionAvatar(champion = enemyOpponent, size = 52.dp, showTierBadge = true)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = com.example.util.tr(enemyOpponent.name),
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = tr("Rival de Línea"),
                                        color = DangerRed,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = com.example.util.tr("WR: ${String.format(java.util.Locale.US, "%.2f", enemyOpponent.winrate)}%"),
                                        color = DangerRed.copy(alpha = 0.8f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechSurface),
                        border = BorderStroke(0.8.dp, HextechGold)
                    ) {
                        Text(
                            text = com.example.util.ChampionMatchupCoaching.sovereignFeedback(
                                myChampion, activeRole, language, enemyOpponent),
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }

                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold, contentColor = HextechDarkBg),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(tr("Entendido, volver al Draft"), fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
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
