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
    val isMyCounter = enemyOpponent.counteredBy.any { 
        it.equals(myChampion.name, ignoreCase = true) || it.equals(myChampion.id, ignoreCase = true) 
    } || myChampion.advantageAgainst.any { 
        it.equals(enemyOpponent.name, ignoreCase = true) || it.equals(enemyOpponent.id, ignoreCase = true) 
    }

    val isEnemyCounter = myChampion.counteredBy.any { 
        it.equals(enemyOpponent.name, ignoreCase = true) || it.equals(enemyOpponent.id, ignoreCase = true) 
    } || enemyOpponent.advantageAgainst.any { 
        it.equals(myChampion.name, ignoreCase = true) || it.equals(myChampion.id, ignoreCase = true) 
    }

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
                                text = "${tr("Enfrentamiento en")} ${tr(activeRole.displayName)}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
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
                                        text = myChampion.name,
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
                                        text = "WR: ${String.format(java.util.Locale.US, "%.2f", myChampion.winrate)}%",
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
                                            text = "VS",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = when (matchupFavor) {
                                            "FAVORABLE" -> tr("Favorable")
                                            "DESFAVORABLE" -> tr("Difícil")
                                            else -> tr("Habilidad")
                                        },
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
                                        text = enemyOpponent.name,
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
                                        text = "WR: ${String.format(java.util.Locale.US, "%.2f", enemyOpponent.winrate)}%",
                                        color = DangerRed.copy(alpha = 0.8f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 1. Fase de Líneas (Niveles 1-5 y Power Spikes)
                    Text(
                        text = "⏱️ " + tr("Ventanas de Poder & Fase de Líneas"),
                        color = HextechGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechSurfaceVariant.copy(alpha = 0.6f)),
                        border = BorderStroke(0.8.dp, HextechCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Niveles 1-3
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(HextechCyan.copy(alpha = 0.2f))
                                        .border(0.8.dp, HextechCyan, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text("Nv. 1-3", color = HextechCyan, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isMyCounter) 
                                        "Ventaja en intercambios tempranos. En Wild Rift la primera oleada otorga nivel 2 inmediato; presiona para denegar el Fruto de Miel (1:15)." 
                                    else if (isEnemyCounter) 
                                        "Precaución en fase temprana. Cede la prioridad de la primera oleada, farmea bajo torre y espera tu pico al nivel 3 (kit completo)."
                                    else 
                                        "Línea neutra de Wild Rift. Controla los arbustos de línea, guarda la Flor del Adivino y castiga tras esquivar su habilidad principal.",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    lineHeight = 14.5.sp
                                )
                            }

                            // Nivel 5 (Ultimate Spike)
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(HextechGold.copy(alpha = 0.2f))
                                        .border(0.8.dp, HextechGold, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text("Nv. 5 (Definitiva)", color = HextechGold, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pico de Definitiva (Nivel 5): En Wild Rift los enfriamientos de R son cortos (35-50s). Si ${enemyOpponent.name} falla su definitiva, castiga agresivamente antes del objetivo del minuto 5:00.",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    lineHeight = 14.5.sp
                                )
                            }

                            // Mid/Late Game Scaling
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(TierSPlusColor.copy(alpha = 0.2f))
                                        .border(0.8.dp, TierSPlusColor, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text("Mid/Late", color = TierSPlusColor, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Macro y Objetivos Móviles: Al minuto 5:00 asegura la primera rotación (Dragón o Heraldo). En minuto 7:30 caen las placas de torre y a los 12:00 el Barón/Ancestral.",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    lineHeight = 14.5.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Cooldowns Clave & Habilidades a Esquivar
                    Text(
                        text = "🎯 " + tr("Análisis Táctico del Rival"),
                        color = HextechGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = DangerRedSurface),
                        border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Fuerte contra (Matchups favorables): ${if (enemyOpponent.advantageAgainst.isNotEmpty()) enemyOpponent.advantageAgainst.take(3).joinToString(", ") else "Intercambio en línea de Wild Rift"}",
                                    color = DangerRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Consejo del rival: ${enemyOpponent.tacticalAdvice.ifBlank { "Castiga cuando falle sus habilidades principales o use recursos en la oleada." }}",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    // 3. Sinergias y Macro Wild Rift
                    Text(
                        text = "🤝 " + tr("Sinergias y Macro (Wild Rift)"),
                        color = HextechCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechSurface),
                        border = BorderStroke(0.8.dp, HextechCyan.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Condición de Victoria Móvil:",
                                color = HextechCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tr("Aprovecha que las rotaciones en Wild Rift son rápidas. Prioriza rotar al Dragón antes del minuto 5. Si %s rota primero, castiga su torre por placas.").format(enemyOpponent.name),
                                color = TextPrimary,
                                fontSize = 11.sp,
                                lineHeight = 14.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Veredicto del Coach
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(HextechGold.copy(alpha = 0.2f), HextechCyan.copy(alpha = 0.2f))
                                )
                            )
                            .border(1.dp, HextechGold, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SportsEsports, contentDescription = null, tint = HextechGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Veredicto del Coach Soberano:",
                                    color = HextechGold,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = if (isMyCounter) 
                                        "Tienes la ventaja de campeón. Mantén el control de la oleada y usa los arbustos laterales para rotar rápido y emboscar (roam) a otras líneas."
                                    else if (isEnemyCounter)
                                        "Mantén la calma y no cedas oro. En Wild Rift el juego tardío llega rápido; agrupa con tu equipo tan pronto caiga la primera torre."
                                    else
                                        "Duelo equilibrado. Mantén visión en el río con Lente Revelador antes de los objetivos y castiga cuando use habilidades en la oleada.",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    lineHeight = 14.5.sp
                                )
                            }
                        }
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
