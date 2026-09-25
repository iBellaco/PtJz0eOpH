package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.alpha
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Champion
import com.example.model.DraftSlot
import com.example.model.LaneRole
import com.example.ui.theme.AllyBlue
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechGoldLight
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.isLightAppTheme
import com.example.util.tr

/**
 * Panel de Selección de Campeones de Drafting basado en las 5 Posiciones Oficiales de Wild Rift.
 * Diseño idéntico al panel de Ajustes de Posición pero donde las casillas inferiores
 * se utilizan para seleccionar, mostrar y gestionar los campeones asignados a cada línea.
 */
@Composable
fun DraftTeamPositionCard(
    isOverlay: Boolean = false,
    title: String,
    isEnemy: Boolean,
    slots: List<DraftSlot>,
    activeUserRole: LaneRole?,
    onPickChampionForRole: (LaneRole) -> Unit,
    onRemoveChampionForRole: (LaneRole) -> Unit,
    onChampionClick: (Champion) -> Unit,
    modifier: Modifier = Modifier
) {
    val roles = listOf(
        Pair(LaneRole.TOP, "TOP"),
        Pair(LaneRole.JUNGLE, "JUNGLA"),
        Pair(LaneRole.MID, "MID"),
        Pair(LaneRole.ADC, "DÚO"),
        Pair(LaneRole.SUPPORT, "SOPORTE")
    )

    val teamColor = if (isEnemy) DangerRed else AllyBlue
    val cardBorderColor = if (isEnemy) DangerRed.copy(alpha = 0.7f) else HextechGold.copy(alpha = 0.75f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(if (isEnemy) "enemy_team_draft_card" else "ally_team_draft_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                if (isEnemy) listOf(DangerRed, HextechGold.copy(alpha = 0.5f), DangerRed)
                else listOf(HextechGold, HextechCyan.copy(alpha = 0.6f), HextechGold)
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header del Equipo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tr(title).uppercase(),
                    color = if (isEnemy) DangerRed else HextechGold,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )

                val count = slots.count { it.champion != null }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isEnemy) DangerRed.copy(alpha = 0.2f) else HextechCyan.copy(alpha = 0.2f))
                        .border(1.dp, if (isEnemy) DangerRed else HextechCyan, RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$count / 5",
                        color = if (isEnemy) DangerRed else HextechCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = tr("Toca cada posición para asignar o cambiar campeón"),
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 5 Columnas de Posiciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                roles.forEach { (role, labelKey) ->
                    val slot = slots.find { it.assignedRole == role }
                    val champ = slot?.champion
                    val isMyRole = !isEnemy && role == activeUserRole

                    val isOccupied = champ != null && champ.id != "empty"
                    val displayChamp = if (isOccupied) champ else null

                    val borderColor by animateColorAsState(
                        targetValue = when {
                            isMyRole -> HextechCyan
                            isOccupied -> if (isEnemy) DangerRed.copy(alpha = 0.7f) else HextechGold
                            else -> HextechCardBorder
                        },
                        label = "colBorder"
                    )

                    val bgColor by animateColorAsState(
                        targetValue = when {
                            isMyRole -> HextechCyan.copy(alpha = if (isLightAppTheme) 0.12f else 0.18f)
                            isOccupied -> if (isEnemy) DangerRed.copy(alpha = 0.12f) else HextechGold.copy(alpha = 0.14f)
                            else -> if (isLightAppTheme) Color(0xFFF1F5F9) else Color(0xFF0A121D)
                        },
                        label = "colBg"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                            .clickable {
                                onPickChampionForRole(role)
                            }
                            .padding(vertical = if (isOverlay) 4.dp else 8.dp, horizontal = if (isOverlay) 1.dp else 2.dp)
                            .testTag("${if (isEnemy) "enemy" else "ally"}_pos_${role.name.lowercase()}"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Icono Oficial de Rol (Cresta de Wild Rift)
                        Image(
                            painter = painterResource(id = role.iconResId),
                            contentDescription = tr(labelKey),
                            modifier = Modifier
                                .size(if (isOverlay) 22.dp else 28.dp)
                                .alpha(if (isMyRole || isOccupied) 1f else 0.7f)
                                .padding(1.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Etiqueta de la Línea
                        Text(
                            text = tr(labelKey),
                            color = if (isMyRole) HextechCyan else if (isOccupied) TextPrimary else TextSecondary,
                            fontSize = 8.5.sp,
                            fontWeight = if (isOccupied || isMyRole) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // CASILLA DE SELECCIÓN DE CAMPEÓN (Sustituye la casilla 1 y 2)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isMyRole -> HextechCyan.copy(alpha = 0.22f)
                                        isOccupied -> if (isEnemy) DangerRed.copy(alpha = 0.25f) else HextechGold.copy(alpha = 0.25f)
                                        else -> if (isLightAppTheme) Color(0xFFE2E8F0) else Color(0xFF070D15)
                                    }
                                )
                                .border(
                                    width = if (isOccupied || isMyRole) 1.5.dp else 1.dp,
                                    color = when {
                                        isMyRole -> HextechCyan
                                        isOccupied -> if (isEnemy) DangerRed else HextechGold
                                        else -> HextechCardBorder
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = displayChamp,
                                transitionSpec = {
                                    if (targetState != null) {
                                        (fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                                                scaleIn(
                                                    initialScale = 0.65f,
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                                        stiffness = Spring.StiffnessMedium
                                                    )
                                                ) +
                                                slideInVertically(
                                                    initialOffsetY = { it / 3 },
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                                        stiffness = Spring.StiffnessMedium
                                                    )
                                                )
                                        ).togetherWith(
                                            fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.8f)
                                        )
                                    } else {
                                        (fadeIn(animationSpec = tween(180))).togetherWith(
                                            fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.8f)
                                        )
                                    }
                                },
                                label = "ChampionSelectionAnimation"
                            ) { selectedChamp ->
                                if (selectedChamp != null) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        // Imagen del Campeón Seleccionado (Al hacer click se abre el selector para ESTA casilla)
                                        AppAssetImage(
                                            url = selectedChamp.avatarUrl,
                                            contentDescription = selectedChamp.name,
                                            fallbackText = selectedChamp.name,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    onPickChampionForRole(role)
                                                }
                                        )

                                        // Indicador "TÚ" si corresponde a la línea del usuario
                                        if (isMyRole) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(2.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(HextechCyan)
                                                    .padding(horizontal = 3.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "Mío",
                                                    color = Color.Black,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }

                                        // Botón pequeño 'X' en la esquina superior para deseleccionar
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.75f))
                                                .clickable {
                                                    onRemoveChampionForRole(role)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = tr("Quitar"),
                                                tint = Color.White,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                } else {
                                    // Casilla vacía con botón "+" para añadir
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = tr("Seleccionar Campeón"),
                                            tint = if (isMyRole) HextechCyan else TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        if (isMyRole) {
                                            Text(
                                                text = "Mío",
                                                color = HextechCyan,
                                                fontSize = if (isOverlay) 5.sp else 7.5.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Nombre del campeón debajo de la casilla si está seleccionado
                        if (displayChamp != null) {
                            Spacer(modifier = Modifier.height(if (isMyRole) 8.dp else 3.dp))
                            Text(
                                text = displayChamp.name,
                                color = if (isMyRole) HextechCyan else TextPrimary,
                                fontSize = if (isOverlay) 6.sp else 8.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.clickable { onChampionClick(displayChamp) }
                            )
                            // Indicador de certeza / confianza SOLO para el equipo rival (desconocimiento de línea hasta loading screen)
                            if (isEnemy && slot?.confidence != null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFF0F1923).copy(alpha = 0.9f))
                                        .border(
                                            0.5.dp,
                                            if (slot.confidence >= 80) HextechCyan.copy(alpha = 0.6f) else HextechGold.copy(alpha = 0.6f),
                                            RoundedCornerShape(3.dp)
                                        )
                                        .padding(horizontal = 3.dp, vertical = 0.5.dp)
                                ) {
                                    Text(
                                        text = "${slot.confidence}%",
                                        color = if (slot.confidence >= 80) HextechCyan else HextechGold,
                                        fontSize = if (isOverlay) 6.sp else 7.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(if (isMyRole) 8.dp else 3.dp))
                            Text(
                                text = "-",
                                color = TextMuted,
                                fontSize = if (isOverlay) 6.sp else 8.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
