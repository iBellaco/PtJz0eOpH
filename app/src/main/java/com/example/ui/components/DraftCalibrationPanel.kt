package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.screen.DraftVisionScanner
import com.example.service.screen.VisionCalibrationConfig
import com.example.ui.theme.*
import com.example.util.tr
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.roundToInt

enum class CalibrationTarget(val title: String, val subtitle: String) {
    TOP_ENEMY_5("⭐ 10º Pick Rival Superior (Top 5)", "Calibrar círculo superior derecho del 10º pick rival"),
    TOP_ALLY_5("⭐ 10º Pick Aliado Superior (Top 5)", "Calibrar círculo superior izquierdo del 10º pick aliado"),
    TOP_AVATAR_Y("Altura Y Círculos Superiores", "Mover arriba/abajo la barra superior de avatares"),
    TOP_AVATAR_SIZE("Tamaño Círculos Superiores (⌀)", "Ajustar diámetro de los 10 avatares de la barra superior"),
    AVATAR_SIZE("Tamaño Avatar Slots (⌀)", "Agrandar o reducir radio de escaneo de retratos en slots"),
    GLOBAL_ALLY_X("Columna Aliados X", "Mover horizontalmente todos los avatares aliados verticales"),
    GLOBAL_ENEMY_X("Columna Rivales X", "Mover horizontalmente todos los avatares rivales verticales"),
    ENEMY_SLOT_4("10º Pick Rival Inferior (Slot 5)", "Ajuste vertical Y del slot 5 rival (abajo derecha)"),
    ALLY_SLOT_4("10º Pick Aliado Inferior (Slot 5)", "Ajuste vertical Y del slot 5 aliado (abajo izquierda)"),
    ALLY_SLOT_0("Aliado 1 (TOP)", "Ajuste vertical Y del carril de Barón"),
    ALLY_SLOT_1("Aliado 2 (JG)", "Ajuste vertical Y de la Jungla"),
    ALLY_SLOT_2("Aliado 3 (MID)", "Ajuste vertical Y del carril Central"),
    ALLY_SLOT_3("Aliado 4 (ADC)", "Ajuste vertical Y del Tirador"),
    ENEMY_SLOT_0("Rival 1", "Ajuste vertical Y del slot 1 rival"),
    ENEMY_SLOT_1("Rival 2", "Ajuste vertical Y del slot 2 rival"),
    ENEMY_SLOT_2("Rival 3", "Ajuste vertical Y del slot 3 rival"),
    ENEMY_SLOT_3("Rival 4", "Ajuste vertical Y del slot 4 rival"),
    GLOBAL_Y("Mover Todos los Slots (Y)", "Desplazar verticalmente todas las casillas")
}

@Composable
fun DraftCalibrationPanel(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onDragDelta: ((dx: Int, dy: Int, isDragging: Boolean, isEnded: Boolean) -> Unit)? = null
) {
    val context = LocalContext.current
    val currentConfig by DraftVisionScanner.calibrationConfigFlow.collectAsStateWithLifecycle()
    var config by remember { mutableStateOf(currentConfig) }
    LaunchedEffect(currentConfig) {
        config = currentConfig
    }
    var selectedTarget by remember { mutableStateOf(CalibrationTarget.TOP_ENEMY_5) }
    var stepFactor by remember { mutableStateOf(0.005f) } // 0.5% paso normal

    fun updateAndApply(newConfig: VisionCalibrationConfig) {
        config = newConfig
        DraftVisionScanner.updateCalibration(context, newConfig)
    }

    fun modify(deltaX: Float = 0f, deltaY: Float = 0f, deltaSize: Float = 0f) {
        val cur = config
        val effectiveDeltaSize = if (deltaSize != 0f) deltaSize else (deltaY * 0.5f + deltaX * 0.5f)
        val effectiveDeltaX = deltaX
        val effectiveDeltaY = deltaY

        val updated = when (selectedTarget) {
            CalibrationTarget.TOP_ENEMY_5 -> cur.copy(
                topEnemy5XRatio = (cur.topEnemy5XRatio + effectiveDeltaX).coerceIn(0.70f, 0.99f),
                topEnemyXRatios = cur.topEnemyXRatios.toMutableList().also {
                    it[4] = (it[4] + effectiveDeltaX).coerceIn(0.70f, 0.99f)
                }
            )
            CalibrationTarget.TOP_ALLY_5 -> cur.copy(
                topAlly5XRatio = (cur.topAlly5XRatio + effectiveDeltaX).coerceIn(0.05f, 0.35f),
                topAllyXRatios = cur.topAllyXRatios.toMutableList().also {
                    it[4] = (it[4] + effectiveDeltaX).coerceIn(0.05f, 0.35f)
                }
            )
            CalibrationTarget.TOP_AVATAR_Y -> cur.copy(topAvatarYRatio = (cur.topAvatarYRatio + effectiveDeltaY).coerceIn(0.01f, 0.30f))
            CalibrationTarget.TOP_AVATAR_SIZE -> cur.copy(topAvatarDiameterRatio = (cur.topAvatarDiameterRatio + effectiveDeltaSize).coerceIn(0.02f, 0.20f))
            CalibrationTarget.GLOBAL_ALLY_X -> cur.copy(allyAvatarCenterX = (cur.allyAvatarCenterX + effectiveDeltaX).coerceIn(0.01f, 0.40f))
            CalibrationTarget.GLOBAL_ENEMY_X -> cur.copy(enemyAvatarCenterX = (cur.enemyAvatarCenterX + effectiveDeltaX).coerceIn(0.60f, 0.99f))
            CalibrationTarget.AVATAR_SIZE -> cur.copy(avatarDiameterRatio = (cur.avatarDiameterRatio + effectiveDeltaSize).coerceIn(0.04f, 0.28f))
            CalibrationTarget.ENEMY_SLOT_4 -> cur.copy(enemySlotYRatios = cur.enemySlotYRatios.toMutableList().also { it[4] = (it[4] + effectiveDeltaY).coerceIn(0.05f, 0.95f) })
            CalibrationTarget.ALLY_SLOT_4 -> cur.copy(allySlotYRatios = cur.allySlotYRatios.toMutableList().also { it[4] = (it[4] + effectiveDeltaY).coerceIn(0.05f, 0.95f) })
            CalibrationTarget.ALLY_SLOT_0 -> cur.copy(allySlotYRatios = cur.allySlotYRatios.toMutableList().also { it[0] = (it[0] + effectiveDeltaY).coerceIn(0.05f, 0.95f) })
            CalibrationTarget.ALLY_SLOT_1 -> cur.copy(allySlotYRatios = cur.allySlotYRatios.toMutableList().also { it[1] = (it[1] + effectiveDeltaY).coerceIn(0.05f, 0.95f) })
            CalibrationTarget.ALLY_SLOT_2 -> cur.copy(allySlotYRatios = cur.allySlotYRatios.toMutableList().also { it[2] = (it[2] + effectiveDeltaY).coerceIn(0.05f, 0.95f) })
            CalibrationTarget.ALLY_SLOT_3 -> cur.copy(allySlotYRatios = cur.allySlotYRatios.toMutableList().also { it[3] = (it[3] + effectiveDeltaY).coerceIn(0.05f, 0.95f) })
            CalibrationTarget.ENEMY_SLOT_0 -> cur.copy(enemySlotYRatios = cur.enemySlotYRatios.toMutableList().also { it[0] = (it[0] + effectiveDeltaY).coerceIn(0.05f, 0.95f) })
            CalibrationTarget.ENEMY_SLOT_1 -> cur.copy(enemySlotYRatios = cur.enemySlotYRatios.toMutableList().also { it[1] = (it[1] + effectiveDeltaY).coerceIn(0.05f, 0.95f) })
            CalibrationTarget.ENEMY_SLOT_2 -> cur.copy(enemySlotYRatios = cur.enemySlotYRatios.toMutableList().also { it[2] = (it[2] + effectiveDeltaY).coerceIn(0.05f, 0.95f) })
            CalibrationTarget.ENEMY_SLOT_3 -> cur.copy(enemySlotYRatios = cur.enemySlotYRatios.toMutableList().also { it[3] = (it[3] + effectiveDeltaY).coerceIn(0.05f, 0.95f) })
            CalibrationTarget.GLOBAL_Y -> cur.copy(
                allySlotYRatios = cur.allySlotYRatios.map { (it + effectiveDeltaY).coerceIn(0.05f, 0.95f) },
                enemySlotYRatios = cur.enemySlotYRatios.map { (it + effectiveDeltaY).coerceIn(0.05f, 0.95f) }
            )
        }
        updateAndApply(updated)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
        border = BorderStroke(1.5.dp, HextechCyan)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Barra de agarre (Drag Handle) superior para mover el panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDragDelta?.invoke(dragAmount.x.roundToInt(), dragAmount.y.roundToInt(), true, false)
                            },
                            onDragEnd = { onDragDelta?.invoke(0, 0, false, false) },
                            onDragCancel = { onDragDelta?.invoke(0, 0, false, false) }
                        )
                    }
                    .padding(top = 1.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(HextechCyan.copy(alpha = 0.65f))
                )
            }

            // CABECERA con soporte de arrastre
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDragDelta?.invoke(dragAmount.x.roundToInt(), dragAmount.y.roundToInt(), true, false)
                            },
                            onDragEnd = { onDragDelta?.invoke(0, 0, false, false) },
                            onDragCancel = { onDragDelta?.invoke(0, 0, false, false) }
                        )
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = HextechCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tr("Calibrador de Visión (10º Pick & Draft)"),
                        color = HextechGold,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar Calibrador",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // MAPA VISUAL ESQUEMÁTICO DEL ÁREA DE ESCANEO
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF07121E))
                    .border(1.dp, HextechCardBorder.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                // Barra Superior (Top Avatars)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Top 5 Aliados
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        (0..4).forEach { idx ->
                            val is10thAlly = idx == 4 && selectedTarget == CalibrationTarget.TOP_ALLY_5
                            val isTopActive = is10thAlly || selectedTarget == CalibrationTarget.TOP_AVATAR_Y || selectedTarget == CalibrationTarget.TOP_AVATAR_SIZE
                            Box(
                                modifier = Modifier
                                    .size(13.dp)
                                    .clip(CircleShape)
                                    .background(if (isTopActive) HextechCyan else Color(0xFF0F3B56))
                                    .border(0.8.dp, if (isTopActive) Color.White else HextechCyan.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${idx + 1}", color = if (isTopActive) Color.Black else HextechCyan, fontSize = 6.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Título Top Bar
                    Text("BARRA SUPERIOR", color = HextechGold.copy(alpha = 0.7f), fontSize = 6.5.sp, fontWeight = FontWeight.Bold)

                    // Top 5 Rivales
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        (0..4).forEach { idx ->
                            val is10thEnemy = idx == 4 && selectedTarget == CalibrationTarget.TOP_ENEMY_5
                            val isTopActive = is10thEnemy || selectedTarget == CalibrationTarget.TOP_AVATAR_Y || selectedTarget == CalibrationTarget.TOP_AVATAR_SIZE
                            Box(
                                modifier = Modifier
                                    .size(13.dp)
                                    .clip(CircleShape)
                                    .background(if (is10thEnemy) HextechGold else if (isTopActive) DangerRed else Color(0xFF4A1A22))
                                    .border(0.8.dp, if (is10thEnemy) Color.White else DangerRed.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${idx + 1}", color = if (is10thEnemy) Color.Black else Color.White, fontSize = 6.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Slots Verticales Aliados
                val allyRoles = listOf("TOP", "JG", "MID", "ADC", "SUP")
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.CenterStart)
                        .padding(start = 6.dp, top = 20.dp, bottom = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    config.allySlotYRatios.forEachIndexed { i, _ ->
                        val isTargeted = selectedTarget == CalibrationTarget.GLOBAL_ALLY_X ||
                                (selectedTarget == CalibrationTarget.ALLY_SLOT_4 && i == 4) ||
                                (selectedTarget == CalibrationTarget.ALLY_SLOT_0 && i == 0)
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (isTargeted) AllyBlue.copy(alpha = 0.5f) else Color(0xFF13273D))
                                .border(0.8.dp, if (isTargeted) HextechCyan else AllyBlue.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(allyRoles.getOrElse(i) { "$i" }, color = if (isTargeted) HextechCyan else TextMuted, fontSize = 6.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Centro Informativo del Escáner
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(top = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "DETECCIÓN 10º PICK & SLOTS",
                        color = HextechGold.copy(alpha = 0.85f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "10º Rival Top: ${(config.topEnemy5XRatio * 100).format(1)}% | Top Y: ${(config.topAvatarYRatio * 100).format(1)}%",
                        color = HextechCyan,
                        fontSize = 7.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(HextechSurface)
                            .border(0.5.dp, HextechGold.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Calibrando: ${selectedTarget.title}",
                            color = HextechGold,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Slots Verticales Rivales
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.CenterEnd)
                        .padding(end = 6.dp, top = 20.dp, bottom = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    config.enemySlotYRatios.forEachIndexed { i, _ ->
                        val isTargeted = selectedTarget == CalibrationTarget.GLOBAL_ENEMY_X ||
                                (selectedTarget == CalibrationTarget.ENEMY_SLOT_4 && i == 4) ||
                                (selectedTarget == CalibrationTarget.ENEMY_SLOT_0 && i == 0)
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (isTargeted) DangerRed.copy(alpha = 0.5f) else Color(0xFF38141B))
                                .border(0.8.dp, if (isTargeted) Color(0xFFFF5252) else DangerRed.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("R${i + 1}", color = if (isTargeted) Color(0xFFFF5252) else TextMuted, fontSize = 6.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SELECTOR DE OBJETIVO A CALIBRAR (CHIPS HORIZONTALES)
            Text(
                text = "1. Selecciona qué elemento calibrar:",
                color = TextPrimary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CalibrationTarget.entries.forEach { target ->
                    val isSel = selectedTarget == target
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) HextechCyan.copy(alpha = 0.2f) else HextechSurface)
                            .border(
                                1.dp,
                                if (isSel) HextechCyan else HextechCardBorder.copy(alpha = 0.5f),
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedTarget = target }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = target.title,
                            color = if (isSel) HextechCyan else TextMuted,
                            fontSize = 8.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CONTROLES DE MOVIMIENTO DIRECCIONAL Y TAMAÑO (D-PAD)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pad de Movimiento (Arriba, Abajo, Izquierda, Derecha)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    // Arriba
                    Surface(
                        modifier = Modifier
                            .size(34.dp)
                            .clickable { modify(deltaY = -stepFactor) },
                        shape = RoundedCornerShape(6.dp),
                        color = HextechSurface,
                        border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.6f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Arriba", tint = HextechGold, modifier = Modifier.size(20.dp))
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 3.dp)
                    ) {
                        // Izquierda
                        Surface(
                            modifier = Modifier
                                .size(34.dp)
                                .clickable { modify(deltaX = -stepFactor) },
                            shape = RoundedCornerShape(6.dp),
                            color = HextechSurface,
                            border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.6f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Izquierda", tint = HextechGold, modifier = Modifier.size(20.dp))
                            }
                        }

                        // Indicador Central
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0F1E2E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "±${(stepFactor * 100).format(1)}%",
                                color = HextechCyan,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Derecha
                        Surface(
                            modifier = Modifier
                                .size(34.dp)
                                .clickable { modify(deltaX = stepFactor) },
                            shape = RoundedCornerShape(6.dp),
                            color = HextechSurface,
                            border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.6f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Derecha", tint = HextechGold, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    // Abajo
                    Surface(
                        modifier = Modifier
                            .size(34.dp)
                            .clickable { modify(deltaY = stepFactor) },
                        shape = RoundedCornerShape(6.dp),
                        color = HextechSurface,
                        border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.6f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Abajo", tint = HextechGold, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // Controles de Tamaño (Agrandar / Reducir) y Selector de Paso
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "Tamaño de Cuadro:",
                        color = TextPrimary,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { modify(deltaSize = stepFactor) },
                            modifier = Modifier.weight(1f).height(32.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B3854)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = HextechCyan, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Agrandar", color = HextechCyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { modify(deltaSize = -stepFactor) },
                            modifier = Modifier.weight(1f).height(32.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E1C22)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = DangerRed, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Reducir", color = DangerRed, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Selector de Paso
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Paso:", color = TextMuted, fontSize = 7.5.sp)
                        val steps = listOf(
                            Pair("0.1%", 0.001f),
                            Pair("0.5%", 0.005f),
                            Pair("1.0%", 0.010f)
                        )
                        steps.forEach { (label, value) ->
                            val isSel = stepFactor == value
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSel) HextechGold.copy(alpha = 0.25f) else HextechSurface)
                                    .border(0.6.dp, if (isSel) HextechGold else HextechCardBorder, RoundedCornerShape(4.dp))
                                    .clickable { stepFactor = value }
                                    .padding(vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) HextechGold else TextMuted,
                                    fontSize = 7.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // BOTONES DE ACCIÓN: COPIAR COORDENADAS, RESTABLECER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Botón Copiar Coordenadas
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("WildRift_Vision_Calibration", config.toFormattedCoordinatesString())
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Coordenadas copiadas al portapapeles", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier.weight(1.3f).height(34.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = HextechDarkBg, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Copiar Coordenadas", color = HextechDarkBg, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }

                // Botón Restablecer
                Button(
                    onClick = {
                        DraftVisionScanner.resetCalibration(context)
                        config = DraftVisionScanner.calibrationConfig
                        Toast.makeText(context, "Valores restablecidos de fábrica", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(0.9f).height(34.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HextechSurface),
                    border = BorderStroke(1.dp, HextechCardBorder),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text("Restablecer", color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private fun Float.format(digits: Int): String = "%.${digits}f".format(java.util.Locale.US, this)
