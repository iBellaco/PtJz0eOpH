package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import com.example.util.tr

val HextechDarkBg = Color(0xFF090E17)
val HextechSurface = Color(0xFF121B2B)
val HextechSurfaceVariant = Color(0xFF1B283F)
val HextechGold = Color(0xFFC8AA6E)
val HextechCyan = Color(0xFF0AC8B9)
val TextPrimary = Color(0xFFF0F6FC)
val TextSecondary = Color(0xFF8B949E)

@Composable
fun MultiServerStatsDialog(
    onDismiss: () -> Unit
) {
    val currentRegion by com.example.data.sync.ChineseMetaSyncService.currentRegion.collectAsState()
    val currentTier by com.example.data.sync.ChineseMetaSyncService.currentTier.collectAsState()

    val topCn = remember(currentRegion, currentTier) {
        com.example.data.WildRiftRepository.getTopChampionsForServer("CN", count = 3, tencentTier = currentTier)
    }
    val topGlobal = remember(currentRegion, currentTier) {
        com.example.data.WildRiftRepository.getTopChampionsForServer("Global", count = 3)
    }
    val topNa = remember(currentRegion, currentTier) {
        com.example.data.WildRiftRepository.getTopChampionsForServer("NA", count = 3)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, HextechGold.copy(alpha = 0.8f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = HextechDarkBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(HextechCyan.copy(alpha = 0.2f))
                                    .border(1.dp, HextechCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Public, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = tr("DATOS EN GENERAL Y ESTADÍSTICAS DEL META"),
                                    color = HextechGold,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = tr("China en línea • Global y NA locales"),
                                    color = HextechCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(HextechSurfaceVariant)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Cerrar"), tint = TextPrimary, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Global Sample Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, HextechCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = HextechSurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tr("Fuentes disponibles"),
                                    color = HextechGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                // Live pulsing indicator
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                                    val alpha by infiniteTransition.animateFloat(
                                        initialValue = 0.3f,
                                        targetValue = 1f,
                                        animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse),
                                        label = "alpha"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(HextechCyan.copy(alpha = alpha))
                                    )
                                    Text(text = tr("China / Local"), color = HextechCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "3",
                                color = TextPrimary,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tr("China: estadísticas en línea. Global y NA: referencias locales."),
                                color = TextSecondary,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Server 1: China Tencent
                    ServerStatCard(
                        flag = "🇨🇳",
                        serverName = tr("Servidor Chino (lolm.qq.com)"),
                        matchesText = tr("Muestra no publicada"),
                        topChampions = topCn,
                        borderColor = HextechGold,
                        dataSource = tr("Servidor chino")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Server 2: Global
                    ServerStatCard(
                        flag = "🌍",
                        serverName = tr("Global (referencia local)"),
                        matchesText = tr("Referencia local"),
                        topChampions = topGlobal,
                        borderColor = HextechCyan,
                        dataSource = tr("Referencia local"),
                        isLocal = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Server 3: Norteamérica NA
                    ServerStatCard(
                        flag = "🇺🇸",
                        serverName = tr("América (NA, referencia local)"),
                        matchesText = tr("Referencia local"),
                        topChampions = topNa,
                        borderColor = Color(0xFF4A90E2),
                        dataSource = tr("Referencia local"),
                        isLocal = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(tr("Entendido"), color = HextechDarkBg, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ServerStatCard(
    flag: String,
    serverName: String,
    matchesText: String,
    topChampions: List<com.example.model.Champion>,
    borderColor: Color,
    dataSource: String,
    isLocal: Boolean = false
) {
    var showDetailDialog by remember { mutableStateOf(false) }

    if (showDetailDialog) {
        AlertDialog(
            onDismissRequest = { showDetailDialog = false },
            containerColor = HextechDarkBg,
            titleContentColor = HextechGold,
            textContentColor = TextPrimary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = com.example.util.tr("${tr("Detalle de Análisis:")} $serverName"), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isLocal) tr("Lista incluida en la app; no representa estadísticas regionales en vivo.") else tr("Estadísticas consultadas en el servidor chino y conservadas para usar sin conexión."),
                        fontSize = 12.sp,
                        color = TextPrimary,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = com.example.util.tr(matchesText),
                        fontSize = 11.5.sp,
                        color = HextechGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tr("La referencia local se conserva al cambiar de servidor."),
                        fontSize = 11.5.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tr("No se generan porcentajes aleatorios."),
                        fontSize = 11.5.sp,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetailDialog = false }) {
                    Text(tr("Cerrar"), color = HextechGold, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, borderColor.copy(alpha = 0.7f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = HextechSurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = com.example.util.tr(flag), fontSize = 24.sp, modifier = Modifier.padding(top = 2.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = com.example.util.tr(serverName),
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (topChampions.isEmpty()) {
                        Text(
                            text = tr("Cargando datos..."),
                            color = HextechCyan,
                            fontSize = 11.5.sp
                        )
                    } else {
                        topChampions.take(3).forEachIndexed { index, champ ->
                            val rankNumber = index + 1
                            val rankColor = when (rankNumber) {
                                1 -> HextechGold
                                2 -> HextechCyan
                                else -> TextPrimary.copy(alpha = 0.85f)
                            }
                            Text(
                                text = com.example.util.tr("Meta #$rankNumber: ${champ.name} (${String.format(java.util.Locale.US, "%.2f", champ.winrate)}% WR)"),
                                color = rankColor,
                                fontSize = 11.5.sp,
                                fontWeight = if (rankNumber == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = com.example.util.tr(dataSource),
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable { showDetailDialog = true }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = com.example.util.tr(matchesText),
                        color = HextechGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = com.example.util.trNullable("Detalle"),
                        tint = HextechCyan,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = if (isLocal) tr("Sin conexión") else tr("Servidor chino"),
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}
