package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.tr
import kotlinx.coroutines.launch

data class TutorialPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TutorialScreen(onFinish: () -> Unit) {
    val pages = listOf(
        TutorialPage(
            title = "1. Otorga los Permisos",
            description = "Para que el Asistente Flotante funcione, debes darle permiso para mostrarse sobre otras apps (Overlay) y para leer la pantalla de selección de campeones.",
            icon = Icons.Default.Security,
            color = HextechGold
        ),
        TutorialPage(
            title = "2. Activa el Asistente",
            description = "Presiona el botón principal en la pantalla de inicio. Un pequeño poro flotante aparecerá en tu pantalla.",
            icon = Icons.Default.RocketLaunch,
            color = HextechCyan
        ),
        TutorialPage(
            title = "3. Entra a Wild Rift",
            description = "Abre el juego y entra a una partida. Cuando estés en la selección de campeones (Draft), presiona el asistente flotante para abrir el panel táctico.",
            icon = Icons.Default.VideogameAsset,
            color = AllyBlue
        ),
        TutorialPage(
            title = "4. Escaneo Automático",
            description = "¡Usa el botón mágico! Escanea los campeones elegidos y recibe recomendaciones en tiempo real sobre qué elegir y qué runas llevar.",
            icon = Icons.Default.DocumentScanner,
            color = DangerRed
        ),
        TutorialPage(
            title = "5. ¡Carrea tu Partida!",
            description = "Sigue las builds recomendadas, evita los wombo-combos enemigos y asegura la victoria.",
            icon = Icons.Default.EmojiEvents,
            color = HextechGold
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    androidx.activity.compose.BackHandler {
        onFinish()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = HextechDarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Botón Salir superior
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onFinish) {
                    Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Cerrar Tutorial"), tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
            ) { page ->
                val currentPage = pages[page]
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        color = currentPage.color.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(2.dp, currentPage.color.copy(alpha = 0.5f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = currentPage.icon,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp),
                                tint = currentPage.color
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = tr(currentPage.title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = tr(currentPage.description),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // Indicadores de página
            Row(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) HextechGold else HextechSurfaceVariant
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(if (pagerState.currentPage == iteration) 10.dp else 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones inferiores
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(
                    visible = pagerState.currentPage > 0,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }) {
                        Text(tr("Atrás"), color = TextMuted, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onFinish()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = com.example.util.tr(if (pagerState.currentPage < pages.size - 1) tr("Siguiente") else tr("¡Empezar!")),
                        color = HextechDarkBg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
