package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppAssetImage
import androidx.compose.ui.layout.ContentScale
import com.example.model.LaneRole
import com.example.ui.theme.*
import com.example.util.tr
import kotlinx.coroutines.delay
import com.example.data.WildRiftRepository
import androidx.compose.material3.ExperimentalMaterial3Api

data class TrackedCooldown(
    val id: String,
    val name: String,
    val baseCooldownSeconds: Int,
    val iconFallback: String,
    val iconUrl: String = "",
    val accentColor: Color = HextechCyan
)

val DEFAULT_TRACKED_SPELLS = listOf(
    TrackedCooldown("flash", "Destello", 150, "FL", com.example.data.WildRiftSpellsAndRunes.SPELL_FLASH, HextechGold),
    TrackedCooldown("ignite", "Prender", 90, "IGN", com.example.data.WildRiftSpellsAndRunes.SPELL_IGNITE, DangerRed),
    TrackedCooldown("exhaust", "Extenuación", 105, "EXT", com.example.data.WildRiftSpellsAndRunes.SPELL_EXHAUST, Color(0xFFE5A500)),
    TrackedCooldown("barrier", "Barrera", 90, "BAR", com.example.data.WildRiftSpellsAndRunes.SPELL_BARRIER, Color(0xFF4FC3F7)),
    TrackedCooldown("heal", "Curar", 120, "HEA", com.example.data.WildRiftSpellsAndRunes.SPELL_HEAL, Color(0xFF66BB6A)),
    TrackedCooldown("ghost", "Fantasma", 90, "GHO", com.example.data.WildRiftSpellsAndRunes.SPELL_GHOST, Color(0xFF26C6DA)),
    TrackedCooldown("zhonya", "Estasis", 120, "ZHO", "file:///android_asset/offline_images/75b1f5c74f47e4997255bd4e93052816.png", HextechGoldLight),
    TrackedCooldown("ult", "Definitiva", 60, "R", "", TierSPlusColor)
)

data class CDNotification(
    val id: String,
    val message: String,
    val iconUrl: String,
    val fallbackIcon: String,
    val color: Color
)

object CooldownTrackerStateHolder {
    val notifications = androidx.compose.runtime.mutableStateListOf<CDNotification>()
    // Map of role name -> Map of spell id -> expiry timestamp in millis
    val activeTimers = mutableStateMapOf<String, Long>()
    val enemyChampions = mutableStateMapOf<String, com.example.model.Champion>()
    val ultimateRanks = mutableStateMapOf<String, Int>()

    fun startTimer(roleKey: String, spellId: String, durationSeconds: Int) {
        val key = "${roleKey}_$spellId"
        activeTimers[key] = System.currentTimeMillis() + (durationSeconds * 1000L)
    }

    fun adjustTimer(roleKey: String, spellId: String, deltaSeconds: Int) {
        val key = "${roleKey}_$spellId"
        val currentExpiry = activeTimers[key] ?: return
        val newExpiry = currentExpiry + (deltaSeconds * 1000L)
        if (newExpiry > System.currentTimeMillis()) {
            activeTimers[key] = newExpiry
        } else {
            activeTimers.remove(key)
        }
    }

    fun resetTimer(roleKey: String, spellId: String) {
        val key = "${roleKey}_$spellId"
        activeTimers.remove(key)
    }

    fun resetAllForRole(roleKey: String) {
        val keysToRemove = activeTimers.keys.filter { it.startsWith("${roleKey}_") }
        keysToRemove.forEach { activeTimers.remove(it) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CooldownTrackerPanel(
    modifier: Modifier = Modifier,
    isCompactOverlay: Boolean = false
) {
    var selectedRole by remember { mutableStateOf(LaneRole.MID) }
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showChampionPicker by remember { mutableStateOf(false) }

    // Tick en tiempo real cada 500ms
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(500)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(HextechDarkBg)
            .padding(if (isCompactOverlay) 4.dp else 12.dp)
    ) {
        // Selector de Rol Rival (TOP, JG, MID, ADC, SUPP)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LaneRole.entries.forEach { role ->
                val isSelected = selectedRole == role
                val activeCount = DEFAULT_TRACKED_SPELLS.count { spell ->
                    val expiry = CooldownTrackerStateHolder.activeTimers["${role.name}_${spell.id}"] ?: 0L
                    expiry > currentTimeMillis
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) HextechCyan else HextechSurface)
                        .border(
                            1.dp,
                            if (isSelected) HextechGold else HextechCardBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedRole = role }
                        .padding(vertical = if (isCompactOverlay) 4.dp else 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = tr(role.shortName),
                            fontSize = if (isCompactOverlay) 10.sp else 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) HextechDarkBg else TextPrimary
                        )
                        if (activeCount > 0) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(DangerRed)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Encabezado de estado del rol seleccionado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⏱️ " + tr("CD Tracker:") + " " + tr("Rival") + " " + tr(selectedRole.displayName),
                color = TextPrimary,
                fontSize = if (isCompactOverlay) 11.5.sp else 13.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = tr("Reiniciar Todos"),
                color = DangerRed,
                fontSize = if (isCompactOverlay) 9.5.sp else 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { CooldownTrackerStateHolder.resetAllForRole(selectedRole.name) }
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Lista de Hechizos y Enfriamientos
        Column(
            verticalArrangement = Arrangement.spacedBy(if (isCompactOverlay) 4.dp else 6.dp)
        ) {
            DEFAULT_TRACKED_SPELLS.forEach { spell ->
                val timerKey = "${selectedRole.name}_${spell.id}"
                val expiry = CooldownTrackerStateHolder.activeTimers[timerKey] ?: 0L
                val remainingSeconds = ((expiry - currentTimeMillis) / 1000).toInt()
                val isActive = remainingSeconds > 0
                val progress = if (isActive) {
                    remainingSeconds.toFloat() / spell.baseCooldownSeconds.toFloat()
                } else 0f

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) HextechSurfaceVariant else HextechSurface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isActive) DangerRed.copy(alpha = 0.8f) else HextechCardBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = if (isCompactOverlay) 6.dp else 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Ícono y Nombre del Hechizo
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(if (isCompactOverlay) 28.dp else 34.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isActive) DangerRed.copy(alpha = 0.2f) else spell.accentColor.copy(alpha = 0.15f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isActive) DangerRed else spell.accentColor,
                                        RoundedCornerShape(6.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (spell.iconUrl.isNotEmpty()) {
                                    AppAssetImage(
                                        url = spell.iconUrl,
                                        contentDescription = tr(spell.name),
                                        fallbackText = spell.iconFallback,
                                        modifier = Modifier.fillMaxSize(),
                                        borderColor = if (isActive) DangerRed else spell.accentColor,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                } else {
                                    Text(
                                        text = spell.iconFallback,
                                        color = if (isActive) DangerRed else spell.accentColor,
                                        fontWeight = FontWeight.Black,
                                        fontSize = if (isCompactOverlay) 9.sp else 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text(
                                    text = tr(spell.name),
                                    color = if (isActive) DangerRed else TextPrimary,
                                    fontSize = if (isCompactOverlay) 11.sp else 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isActive) tr("Enfriamiento:") + " ${remainingSeconds}s " + tr("restante") else tr("Base:") + " ${spell.baseCooldownSeconds}s • " + tr("¡Listo para usar!"),
                                    color = if (isActive) HextechGold else TextMuted,
                                    fontSize = if (isCompactOverlay) 9.sp else 10.5.sp
                                )
                            }
                        }

                        // Botones de acción / Cuenta regresiva
                        if (isActive) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Ajuste rápido -10s
                                IconButton(
                                    onClick = { CooldownTrackerStateHolder.adjustTimer(selectedRole.name, spell.id, -10) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Text("-10", color = HextechCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }

                                // Indicador grande de segundos restantes
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DangerRed)
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${remainingSeconds}s",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = if (isCompactOverlay) 11.sp else 13.sp
                                    )
                                }

                                // Reset
                                IconButton(
                                    onClick = { CooldownTrackerStateHolder.resetTimer(selectedRole.name, spell.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Reiniciar", tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        } else {
                            // Botón de 1-toque para marcar como gastado
                            Button(
                                onClick = {
                                    CooldownTrackerStateHolder.startTimer(selectedRole.name, spell.id, spell.baseCooldownSeconds)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = spell.accentColor),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(if (isCompactOverlay) 28.dp else 32.dp)
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = HextechDarkBg, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = tr("Gastado"),
                                    color = HextechDarkBg,
                                    fontSize = if (isCompactOverlay) 10.sp else 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
