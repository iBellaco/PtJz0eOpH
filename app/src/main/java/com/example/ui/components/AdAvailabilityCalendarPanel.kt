package com.example.ui.components

import com.example.ui.components.CoachIconButton as IconButton

import com.example.util.tr
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import com.example.ui.components.coachClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppNotice
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdAvailabilityCalendarPanel(
    allNotices: List<AppNotice>,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    var isExpanded by remember { mutableStateOf(true) }

    // Filtrar anuncios publicitarios activos/visibles y en cola (excluyendo expirados)
    val activePublicityAds = remember(allNotices, now) {
        allNotices.filter { notice ->
            val isPubTag = notice.tag.equals("Publicidad", ignoreCase = true) ||
                           notice.tag.equals("Ads", ignoreCase = true) ||
                           notice.tag.equals("PUBLICIDAD", ignoreCase = true)
            val isExpired = notice.expiresAtMillis > 0L && notice.expiresAtMillis <= now
            // Excluir expirados; incluir aprobados habilitados (visibles) y pendientes (en cola)
            val isValidStatus = (notice.isApproved && notice.isEnabled) || !notice.isApproved
            isPubTag && !isExpired && isValidStatus
        }
    }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header del Calendario de Disponibilidad con botón de minimizar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .coachClickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.EventAvailable,
                        contentDescription = null,
                        tint = HextechGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = com.example.util.tr("Calendario de Disponibilidad"),
                        color = HextechGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Badge proporcionado y compacto
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = (if (activePublicityAds.isNotEmpty()) Color(0xFFF59E0B) else Color(0xFF10B981)).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, if (activePublicityAds.isNotEmpty()) Color(0xFFF59E0B) else Color(0xFF10B981))
                    ) {
                        Text(
                            text = com.example.util.tr(if (activePublicityAds.isNotEmpty()) "${activePublicityAds.size} Ocupados" else "Libre"),
                            color = if (activePublicityAds.isNotEmpty()) Color(0xFFF59E0B) else Color(0xFF10B981),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Botón Minimizar / Expandir
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = com.example.util.trNullable(if (isExpanded) "Minimizar" else "Expandir"),
                            tint = HextechGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Contenido expandible
            if (isExpanded) {
                Text(
                    text = com.example.util.tr("Consulte las fechas, horas, días y semanas ocupadas antes de programar su anuncio."),
                    color = TextSecondary,
                    fontSize = 10.5.sp
                )

                // Resumen compacto
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechDarkBg)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(tr("Activos / En Cola"), color = TextSecondary, fontSize = 9.sp)
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = com.example.util.tr("${activePublicityAds.size}"),
                                color = HextechGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechDarkBg)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(com.example.util.tr("Estado Red"), color = TextSecondary, fontSize = 9.sp)
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = com.example.util.tr(if (activePublicityAds.size >= 5) "Alta Demanda" else "Disponible"),
                                color = if (activePublicityAds.size >= 5) Color(0xFFF59E0B) else Color(0xFF10B981),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Lista detallada de franjas horarias / días / semanas / meses ocupados
                if (activePublicityAds.isNotEmpty()) {
                    Text(
                        text = com.example.util.tr("Franjas y Fechas Ocupadas:"),
                        color = HextechCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(HextechDarkBg, RoundedCornerShape(6.dp))
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        activePublicityAds.forEach { ad ->
                            val startStr = if (ad.approvedAtMillis > 0L) dateFormat.format(Date(ad.approvedAtMillis)) else "Inmediato"
                            val endStr = if (ad.expiresAtMillis > 0L) dateFormat.format(Date(ad.expiresAtMillis)) else "Indefinido"
                            val unitLabel = when (ad.durationUnit.lowercase(Locale.ROOT)) {
                                "hour", "hours", "hora", "horas" -> "Horas"
                                "day", "days", "dia", "dias", "día", "días" -> "Días"
                                "week", "weeks", "semana", "semanas" -> "Semanas"
                                "month", "months", "mes", "meses" -> "Meses"
                                else -> ad.durationUnit
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(HextechSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .padding(6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = com.example.util.tr(ad.title),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(1.dp))
                                    Text(
                                        text = com.example.util.tr("Prog: ${ad.durationValue} $unitLabel"),
                                        color = HextechGold,
                                        fontSize = 9.sp
                                    )
                                    Text(
                                        text = com.example.util.tr("Desde: $startStr | Hasta: $endStr"),
                                        color = TextSecondary,
                                        fontSize = 8.5.sp
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(3.dp),
                                    color = (if (ad.isApproved) DangerRed else Color(0xFFF59E0B)).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = com.example.util.tr(if (ad.isApproved) "OCUPADO" else "EN COLA"),
                                        color = if (ad.isApproved) DangerRed else Color(0xFFF59E0B),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(HextechDarkBg, RoundedCornerShape(6.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = com.example.util.tr("Espacios totalmente disponibles para programar."),
                                color = Color(0xFF10B981),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
