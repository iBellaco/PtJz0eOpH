package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.util.tr
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.ui.theme.AllyBlue
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechGoldLight
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.HextechSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.util.SystemPermissionHelper

@Composable
fun BatteryAndOverlayNoticeCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasOverlayPermission by remember { mutableStateOf(SystemPermissionHelper.hasOverlayPermission(context)) }
    var isBatteryExempt by remember { mutableStateOf(SystemPermissionHelper.isIgnoringBatteryOptimizations(context)) }
    var isServiceRunning by remember { mutableStateOf(SystemPermissionHelper.isServiceRunning(context)) }

    // Re-check permissions when the user returns to the app from system settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = SystemPermissionHelper.hasOverlayPermission(context)
                isBatteryExempt = SystemPermissionHelper.isIgnoringBatteryOptimizations(context)
                isServiceRunning = SystemPermissionHelper.isServiceRunning(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("battery_overlay_notice_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, HextechGold.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(HextechGold.copy(alpha = 0.2f))
                        .border(1.dp, HextechGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = null,
                        tint = HextechGold,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = tr("Configuración para Segundo Plano"),
                        color = HextechGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tr("Recomendaciones para funcionamiento óptimo sobre Wild Rift"),
                        color = TextMuted,
                        fontSize = 11.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Explanation
            Text(
                text = tr("Para que el asistente flote en tiempo real sobre tu partida de Wild Rift sin que Android cierre el proceso por consumo de memoria:"),
                color = TextPrimary.copy(alpha = 0.9f),
                fontSize = 12.5.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Overlay Permission Status Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(HextechSurfaceVariant.copy(alpha = 0.6f))
                    .border(
                        1.dp,
                        if (hasOverlayPermission) HextechCyan else DangerRed.copy(alpha = 0.5f),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable {
                        SystemPermissionHelper.openOverlaySettings(context)
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = if (hasOverlayPermission) HextechCyan else HextechGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = tr("1. Permiso de Superposición"),
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = com.example.util.tr(if (hasOverlayPermission) tr(" Concedido (Ventana flotante habilitada)") else tr(" Pendiente: Toca para autorizar")),
                            color = if (hasOverlayPermission) HextechCyan else DangerRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = HextechGold,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Battery Optimization Status Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(HextechSurfaceVariant.copy(alpha = 0.6f))
                    .border(
                        1.dp,
                        if (isBatteryExempt) HextechCyan else DangerRed.copy(alpha = 0.5f),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable {
                        SystemPermissionHelper.requestIgnoreBatteryOptimization(context)
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryAlert,
                        contentDescription = null,
                        tint = if (isBatteryExempt) HextechCyan else HextechGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = tr("2. Desactivar Ahorro de Batería"),
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = com.example.util.tr(if (isBatteryExempt) tr(" Sin restricciones (No se cerrará en segundo plano)") else tr(" Optimizado: Toca para quitar restricción")),
                            color = if (isBatteryExempt) HextechCyan else DangerRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = HextechGold,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Start / Stop Floating Service Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (!hasOverlayPermission) {
                            SystemPermissionHelper.openOverlaySettings(context)
                        } else {
                            if (isServiceRunning) {
                                SystemPermissionHelper.stopFloatingService(context)
                                isServiceRunning = false
                            } else {
                                SystemPermissionHelper.startFloatingService(context)
                                isServiceRunning = true
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("toggle_floating_service_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isServiceRunning) DangerRed else HextechCyan,
                        contentColor = HextechDarkBg
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (isServiceRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = com.example.util.tr(if (!hasOverlayPermission) tr("Conceder Permiso") else if (isServiceRunning) tr("Detener Flotante") else tr("Lanzar Flotante en Juego")),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }
            }
        }
    }
}
