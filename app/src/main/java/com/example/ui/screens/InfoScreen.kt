package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import com.example.data.supabase.FeedbackRepository
import com.example.data.supabase.SupabaseClientManager
import com.example.ui.components.AdminFeedbackBottomSheet
import com.example.ui.theme.*
import com.example.util.tr
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    onNavigateBack: () -> Unit,
    onNavigateToFAQ: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var supabaseStatus by remember { mutableStateOf("") }
    var isTestingSupabase by remember { mutableStateOf(false) }
    var isPurging by remember { mutableStateOf(false) }
    var purgeStatus by remember { mutableStateOf("") }
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
                            text = tr("Información"),
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
                                text = tr(WildRiftRepository.CURRENT_PATCH_VERSION),
                                color = HextechCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
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
                            painter = painterResource(id = R.drawable.custom_app_icon),
                            contentDescription = "Coach Icon",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Coach",
                            color = HextechGoldLight,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tr("Asistente Táctico Oficial de Drafting"),
                            color = HextechCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        val patchLabel = WildRiftRepository.CURRENT_PATCH_VERSION.let { raw ->
                            if (raw.startsWith("Parche", ignoreCase = true) || raw.startsWith("Patch", ignoreCase = true)) {
                                tr(raw)
                            } else {
                                "${tr("Parche")} $raw"
                            }
                        }
                        Text(
                            text = "v${com.example.BuildConfig.VERSION_NAME} (Build ${com.example.BuildConfig.VERSION_CODE}) • $patchLabel",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Section 1: Compatibilidad y Parche Oficial (Mejorado)
            InfoCard(
                title = tr("1. Compatibilidad y Parche Oficial"),
                icon = Icons.Default.Info
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoStep(
                        title = tr("🎮 Compatibilidad de Juego Exclusiva:"),
                        description = tr("Desarrollado 100% para League of Legends: Wild Rift en dispositivos móviles. Todos los campeones, estadísticas base, escalados, objetos y runas corresponden exactamente a las versiones de Wild Rift.")
                    )
                    InfoStep(
                        title = tr("🔄 Sincronización de Parche en Tiempo Real:"),
                        description = tr("Totalmente sincronizado con el meta oficial de Wild Rift ") + "${WildRiftRepository.CURRENT_PATCH_VERSION}." + " " + tr("Incluye los últimos bufos, nerfeos, ajustes de objetos y rotaciones de tier list.")
                    )
                    InfoStep(
                        title = tr("⚡ Nomenclatura Oficial Móvil:"),
                        description = tr("Utiliza exclusivamente el esquema oficial de Wild Rift: Habilidad 1 (H1), Habilidad 2 (H2), Habilidad 3 (H3) y Definitiva (H4), además de hechizos y runas adaptadas al ritmo móvil.")
                    )
                    InfoStep(
                        title = tr("🛡️ Asistente Flotante y Alto Rendimiento:"),
                        description = tr("Overlay interactivo con permiso de superposición (SYSTEM_ALERT_WINDOW) diseñado con aceleración por hardware. Consumo ultra-bajo de batería (<2% por hora) y fluidez garantizada a 60, 90 y 120 FPS sin generar tirones ni input lag dentro de la partida.")
                    )
                    InfoStep(
                        title = tr("📱 Compatibilidad de Sistema Operativo:"),
                        description = tr("Compatible con Android 8.0 hasta Android 16 (API 24 a 36) con soporte nativo de modo multiventana, notch y orientación de pantalla horizontal.")
                    )
                }
            }

            // Section 2: Modo de Uso
            InfoCard(
                title = tr("2. Modo de Uso de la Aplicación"),
                icon = Icons.Default.Settings
            ) {
                InfoStep(
                    title = tr("Paso 1: Configura tus Líneas de Juego"),
                    description = tr("En la pantalla principal, selecciona tu 'Línea Main', 'Segunda Línea' y 'Rol Autofill' tocando cada tarjeta.")
                )
                InfoStep(
                    title = tr("Paso 2: Activa el Asistente Flotante"),
                    description = tr("Pulsa el botón central 'ACTIVAR'. Se desplegará la burbuja flotante en pantalla para acompañarte en tu partida.")
                )
                InfoStep(
                    title = tr("Paso 3: Selección de Campeones"),
                    description = tr("Abre Wild Rift y entra a la fase de selección. Toca el botón flotante en cualquier momento para ver recomendaciones, counters y sinergias tácticas en directo.")
                )
                InfoStep(
                    title = tr("Paso 4: Consulta de Builds y Runas"),
                    description = tr("Revisa los consejos tácticos, orden de habilidades móviles (Pasiva, 1, 2, 3, Definitiva) y armado de objetos recomendado para tu línea.")
                )
            }



            // Section 3: Donaciones, Preguntas Frecuentes e Información Legal
            InfoCard(
                title = tr("3. Donaciones, Preguntas Frecuentes y Legal"),
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
                        Text(tr("Apoyar el Proyecto (Donaciones)"), color = HextechDarkBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                        Text(tr("Preguntas Frecuentes (FAQ)"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                        Text(tr("Información Legal y Privacidad"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // Section 4: Desarrollador & Derechos de Autor
            val context = LocalContext.current
            InfoCard(
                title = tr("4. Desarrollador y Derechos de Autor"),
                icon = Icons.Default.Person
            ) {
                Text(
                    text = tr("Aplicación creada y desarrollada por Diego Barba Chavez."),
                    color = TextPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "© 2026 Diego Barba Chavez. " + tr("Todos los derechos reservados."),
                    color = HextechGoldLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tr("Diseñado para la comunidad competitiva de League of Legends: Wild Rift."),
                    color = TextSecondary,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Alfa v${com.example.BuildConfig.VERSION_NAME} (${com.example.BuildConfig.VERSION_CODE})",
                    color = TextMuted.copy(alpha = 0.9f),
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
