package com.example.ui.screens

import com.example.ui.components.CoachButton as Button
import com.example.ui.components.CoachTextButton as TextButton

import com.example.R
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WildRiftRepository
import com.example.ui.theme.*
import com.example.util.localizedString
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val highlights: List<String>,
    val badge: String = WildRiftRepository.CURRENT_PATCH_VERSION
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = listOf(
        OnboardingPage(
            title = localizedString(R.string.onboarding_coach_de_elite_draft),
            subtitle = localizedString(R.string.onboarding_analisis_tactico_en_tiempo_real),
            description = localizedString(R.string.onboarding_selecciona_composiciones_optimas_con_evaluacion_para_los_5_roles),
            icon = Icons.Default.SportsEsports,
            accentColor = HextechGold,
            highlights = listOf(
                localizedString(R.string.onboarding_recomendador_de_3_mejores_picks),
                localizedString(R.string.onboarding_wombo_combos_y_balance_de_dano),
                localizedString(R.string.onboarding_condicion_de_victoria_del_equipo)
            )
        ),
        OnboardingPage(
            title = localizedString(R.string.onboarding_asistente_flotante_en_juego),
            subtitle = localizedString(R.string.onboarding_burbuja_flotante_vision_ocr),
            description = localizedString(R.string.onboarding_activa_la_burbuja_flotante_para_recibir_coaching_en_directo_sobre),
            icon = Icons.Default.Layers,
            accentColor = HextechCyan,
            highlights = listOf(
                localizedString(R.string.onboarding_burbuja_flotante_movible),
                localizedString(R.string.onboarding_escaner_visual_de_seleccion),
                localizedString(R.string.onboarding_consejos_tacticos_sin_cambiar_de_app)
            )
        ),
        OnboardingPage(
            title = localizedString(R.string.onboarding_tier_list_oficial),
            subtitle = localizedString(R.string.onboarding_meta_global_y_counters),
            description = localizedString(R.string.onboarding_consulta_la_tier_list_oficial_actualizada_al_ultimo_parche_y_desc),
            icon = Icons.Default.Leaderboard,
            accentColor = TierSPlusColor,
            highlights = listOf(
                localizedString(R.string.onboarding_tier_list_global_y_de_servidores_asiaticos),
                localizedString(R.string.onboarding_identificacion_de_mejores_picks_del_meta),
                localizedString(R.string.onboarding_analisis_de_enfrentamientos_directos)
            )
        ),
        OnboardingPage(
            title = localizedString(R.string.onboarding_ventajas_suscripcion_premium),
            subtitle = localizedString(R.string.onboarding_acceso_total_de_elite_personalizacion),
            description = localizedString(R.string.onboarding_desbloquea_todas_las_ventajas_exclusivas_historial_estadistico_de),
            icon = Icons.Default.WorkspacePremium,
            accentColor = Color(0xFFFFB300),
            highlights = listOf(
                localizedString(R.string.onboarding_escaner_automatico_del_draft),
                localizedString(R.string.onboarding_historial_del_draft),
                localizedString(R.string.onboarding_win_rate_personal),
                localizedString(R.string.onboarding_campeones_favoritos),
                localizedString(R.string.onboarding_temas_exclusivos),
                localizedString(R.string.onboarding_avatares_exclusivos)
            )
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HextechDarkBg)
    ) {
        // Decorative background glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            pages[pagerState.currentPage].accentColor.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        radius = 800f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // Top Bar: Skip button and Patch Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Patch Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HextechSurface,
                    border = BorderStroke(1.dp, HextechCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(HextechGold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = localizedString(R.string.info_patch, WildRiftRepository.CURRENT_PATCH_VERSION.removePrefix("Parche ").removePrefix("Patch ")),
                            color = HextechGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Skip button
                if (pagerState.currentPage < pages.size - 1) {
                    TextButton(onClick = onFinish) {
                        Text(
                            text = localizedString(R.string.onboarding_omitir),
                            color = TextMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            // Carousel Pager Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { position ->
                OnboardingPageContent(page = pages[position])
            }

            // Bottom Navigation Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                // Indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        val color = if (isSelected) pages[index].accentColor else TextMuted.copy(alpha = 0.25f)
                        val width = if (isSelected) 28.dp else 8.dp

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(6.dp)
                                .width(width)
                                .clip(RoundedCornerShape(3.dp))
                                .background(color)
                        )
                    }
                }

                // Main CTA Button
                val isLastPage = pagerState.currentPage == pages.size - 1
                val currentPage = pages[pagerState.currentPage]

                Button(
                    onClick = {
                        if (isLastPage) {
                            onFinish()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLastPage) HextechGold else HextechCyan
                    ),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isLastPage) localizedString(R.string.onboarding_comenzar_ahora) else localizedString(R.string.onboarding_siguiente),
                            color = HextechDarkBg,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (isLastPage) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = HextechDarkBg,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon with glowing concentric circles
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(page.accentColor.copy(alpha = 0.12f))
                .border(1.5.dp, page.accentColor.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .clip(CircleShape)
                    .background(HextechSurface)
                    .border(1.dp, page.accentColor.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    tint = page.accentColor,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Subtitle badge
        Text(
            text = page.subtitle.uppercase(),
            color = page.accentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Title
        Text(
            text = page.title,
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Description
        Text(
            text = page.description,
            color = TextSecondary,
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Feature Highlights Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = HextechSurface.copy(alpha = 0.8f)),
            border = BorderStroke(1.dp, page.accentColor.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                page.highlights.forEach { highlight ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(page.accentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = page.accentColor,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = highlight,
                            color = TextPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
