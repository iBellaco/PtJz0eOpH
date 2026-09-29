package com.example.ui.components

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Champion
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechSurface

@Composable
fun DraftEnemyTeamCard(
    enemies: List<Champion>,
    onPickEnemy: () -> Unit,
    onRemoveEnemy: (Champion) -> Unit,
    onChampionClick: (Champion) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("enemy_team_draft_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                listOf(DangerRed, HextechGold.copy(alpha = 0.5f), DangerRed)
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
                    text = com.example.util.tr("Equipo Rival"),
                    color = DangerRed,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = com.example.util.tr("${enemies.size}/5"),
                    color = HextechGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 5) {
                    val champ = enemies.getOrNull(i)
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(HextechDarkBg)
                            .border(1.dp, HextechGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable {
                                if (champ != null) {
                                    onChampionClick(champ)
                                } else {
                                    onPickEnemy()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = champ,
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
                            label = "EnemyChampionSlotAnimation"
                        ) { selectedChamp ->
                            if (selectedChamp != null) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    UserAvatarView(
                                        avatarId = selectedChamp.id,
                                        size = 52.dp,
                                        showBorder = false
                                    )

                                    // Botón de eliminar
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(2.dp),
                                        contentAlignment = Alignment.TopEnd
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(DangerRed)
                                                .clickable { onRemoveEnemy(selectedChamp) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Eliminar"), tint = Color.White, modifier = Modifier.size(10.dp))
                                        }
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Add, contentDescription = com.example.util.trNullable("Agregar"), tint = HextechGold.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }

            if (enemies.isEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = com.example.util.tr("Toca un recuadro para agregar un pick rival"),
                    color = HextechGold.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
