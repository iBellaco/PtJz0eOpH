package com.example.ui.screens

import com.example.ui.components.CoachButton as Button
import com.example.ui.components.CoachOutlinedButton as OutlinedButton
import com.example.ui.components.CoachIconButton as IconButton

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.Image
import com.example.ui.components.coachClickable
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WildRiftRepository
import com.example.ui.components.AdminFeedbackBottomSheet
import com.example.ui.theme.*
import com.example.util.localizedString
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    onNavigateBack: () -> Unit,
    onNavigateToFAQ: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isPurging by remember { mutableStateOf(false) }
    var purgeStatus by remember { mutableStateOf("") }
    val patchVersion = WildRiftRepository.CURRENT_PATCH_VERSION.removePrefix("Parche ").removePrefix("Patch ")
    val patchLabel = localizedString(R.string.info_patch, patchVersion)
    var showDonationDialog by remember { mutableStateOf(false) }
    var showLegalDialog by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler {
        if (showDonationDialog) {
            showDonationDialog = false
        } else if (showLegalDialog) {
            showLegalDialog = false
        } else {
            onNavigateBack()
        }
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = localizedString(R.string.info_informacion),
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(HextechCyan.copy(alpha = 0.15f))
                                .border(1.dp, HextechCyan, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = patchLabel,
                                color = HextechCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = localizedString(R.string.info_back), tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HextechDarkBg)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // App Identity & Icon Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HextechGold.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = HextechSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.5.dp, HextechGold, RoundedCornerShape(14.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = localizedString(R.string.info_coach_icon),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = localizedString(R.string.info_coach),
                            color = HextechGoldLight,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = localizedString(R.string.info_asistente_tactico_oficial_de_drafting),
                            color = HextechCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = localizedString(R.string.info_version, com.example.BuildConfig.VERSION_NAME, com.example.BuildConfig.VERSION_CODE, patchLabel),
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Section 1: Compatibilidad y Parche Oficial (Mejorado)
            InfoCard(
                title = localizedString(R.string.info_1_compatibilidad_y_parche_oficial),
                icon = Icons.Default.Info
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoStep(
                        title = localizedString(R.string.info_compatibilidad_de_juego_exclusiva),
                        description = localizedString(R.string.info_desarrollado_100_para_league_of_legends_wild_rift_en_dispositivos)
                    )
                    InfoStep(
                        title = localizedString(R.string.info_sincronizacion_de_parche_en_tiempo_real),
                        description = localizedString(R.string.info_patch_description, patchLabel)
                    )
                    InfoStep(
                        title = localizedString(R.string.info_nomenclatura_oficial_movil),
                        description = localizedString(R.string.info_utiliza_exclusivamente_el_esquema_oficial_de_wild_rift_habilidad)
                    )
                    InfoStep(
                        title = localizedString(R.string.info_asistente_flotante_y_alto_rendimiento),
                        description = localizedString(R.string.info_overlay_interactivo_con_permiso_de_superposicion_system_alert_win)
                    )
                    InfoStep(
                        title = localizedString(R.string.info_compatibilidad_de_sistema_operativo),
                        description = localizedString(R.string.info_compatible_con_android_8_0_hasta_android_16_api_24_a_36_con_sopor)
                    )
                }
            }

            // Section 2: Modo de Uso
            InfoCard(
                title = localizedString(R.string.info_2_modo_de_uso_de_la_aplicacion),
                icon = Icons.Default.Settings
            ) {
                InfoStep(
                    title = localizedString(R.string.info_paso_1_configura_tus_lineas_de_juego),
                    description = localizedString(R.string.info_en_la_pantalla_principal_selecciona_tu_linea_main_segunda_linea_y)
                )
                InfoStep(
                    title = localizedString(R.string.info_paso_2_activa_el_asistente_flotante),
                    description = localizedString(R.string.info_pulsa_el_boton_central_activar_se_desplegara_la_burbuja_flotante)
                )
                InfoStep(
                    title = localizedString(R.string.info_paso_3_seleccion_de_campeones),
                    description = localizedString(R.string.info_abre_wild_rift_y_entra_a_la_fase_de_seleccion_toca_el_boton_flota)
                )
                InfoStep(
                    title = localizedString(R.string.info_paso_4_consulta_de_builds_y_runas),
                    description = localizedString(R.string.info_revisa_los_consejos_tacticos_orden_de_habilidades_moviles_pasiva)
                )
            }



            // Section 3: Donaciones, Preguntas Frecuentes e Información Legal
            InfoCard(
                title = localizedString(R.string.info_3_donaciones_preguntas_frecuentes_y_legal),
                icon = Icons.Default.Star
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { showDonationDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = HextechDarkBg, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(localizedString(R.string.info_apoyar_el_proyecto_donaciones), color = HextechDarkBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = onNavigateToFAQ,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HextechCyan),
                        border = BorderStroke(1.2.dp, HextechCyan.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(localizedString(R.string.info_preguntas_frecuentes_faq), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = { showLegalDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HextechGoldLight),
                        border = BorderStroke(1.2.dp, HextechGold.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = HextechGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(localizedString(R.string.info_informacion_legal_y_privacidad), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // Section 4: Desarrollador & Derechos de Autor
            val context = LocalContext.current
            InfoCard(
                title = localizedString(R.string.info_4_desarrollador_derechos_y_legal),
                icon = Icons.Default.Person
            ) {
                Text(
                    text = localizedString(R.string.info_aplicacion_creada_y_desarrollada_por_diego_barba_chavez),
                    color = TextPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "© 2026 Diego Barba Chavez. " + localizedString(R.string.info_todos_los_derechos_reservados),
                    color = HextechGoldLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = localizedString(R.string.info_disenado_para_la_comunidad_competitiva_de_league_of_legends_wild),
                    color = TextSecondary,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = localizedString(R.string.info_coach_no_cuenta_con_el_respaldo_de_riot_games_y_no_refleja_las_op),
                    color = TextMuted,
                    fontSize = 10.5.sp,
                    lineHeight = 14.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = localizedString(R.string.info_version, com.example.BuildConfig.VERSION_NAME, com.example.BuildConfig.VERSION_CODE, patchLabel),
                    color = HextechCyan.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showDonationDialog) {
        com.example.ui.components.DonationDialog(
            onDismiss = { showDonationDialog = false }
        )
    }

    if (showLegalDialog) {
        com.example.ui.components.PrivacyPolicyDialog(
            onDismiss = { showLegalDialog = false }
        )
    }
}

@Composable
fun InfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = HextechDarkBg.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, HextechGold.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = HextechGold, modifier = Modifier.size(20.dp))
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun InfoStep(title: String, description: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(text = title, color = HextechCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = description, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
    }
}
