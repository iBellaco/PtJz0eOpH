package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.tr
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun DamagePenetrationCalculator(
    modifier: Modifier = Modifier,
    isCompactOverlay: Boolean = false
) {
    var rawResistance by remember { mutableFloatStateOf(100f) } // Armor or MR
    var flatPenetration by remember { mutableFloatStateOf(15f) } // Flat Lethality or Flat MPen
    var percentPenetration by remember { mutableFloatStateOf(30f) } // % Armor Pen or % MPen
    var baseDamage by remember { mutableFloatStateOf(500f) } // Base spell or attack damage

    // Cálculos matemáticos
    val effectiveResistance = max(
        0f,
        (rawResistance * (1f - (percentPenetration / 100f))) - flatPenetration
    )

    val baseReductionPercent = if (rawResistance > 0) (rawResistance / (100f + rawResistance)) * 100f else 0f
    val effectiveReductionPercent = if (effectiveResistance > 0) (effectiveResistance / (100f + effectiveResistance)) * 100f else 0f

    val rawDamageDealt = baseDamage * (100f / (100f + rawResistance))
    val actualDamageDealt = baseDamage * (100f / (100f + effectiveResistance))
    val damageIncreasePercent = if (rawDamageDealt > 0) ((actualDamageDealt - rawDamageDealt) / rawDamageDealt) * 100f else 0f

    // Break-even point: A partir de cuánta armadura % Pen rinde más que Flat Pen
    val breakEvenArmor = if (percentPenetration > 0) (flatPenetration / (percentPenetration / 100f)).roundToInt() else 0

    val currentLang = com.example.util.currentAppLanguage()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(HextechDarkBg)
            .padding(if (isCompactOverlay) 6.dp else 14.dp)
            .then(if (isCompactOverlay) Modifier else Modifier.verticalScroll(rememberScrollState()))
    ) {
        // Título y Coach Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Calculate, contentDescription = null, tint = HextechGold, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tr("Calculadora de Daño & Penetración"),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isCompactOverlay) 12.sp else 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tarjeta de Resultados Principales
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = HextechSurface),
            border = BorderStroke(1.dp, HextechCyan)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(tr("Defensa Efectiva"), color = TextMuted, fontSize = 11.sp)
                        Text(
                            text = com.example.util.tr("${effectiveResistance.roundToInt()} / ${rawResistance.roundToInt()}"),
                            color = HextechCyan,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(tr("Reducción de Daño"), color = TextMuted, fontSize = 11.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = com.example.util.tr("${baseReductionPercent.roundToInt()}%"),
                                color = DangerRed,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = HextechGold, modifier = Modifier.size(14.dp).padding(horizontal = 2.dp))
                            Text(
                                text = com.example.util.tr("${effectiveReductionPercent.roundToInt()}%"),
                                color = AllyBlue,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = HextechCardBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(com.example.util.tr("${tr("Daño Real Infligido")}:"), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(
                        text = com.example.util.tr("${actualDamageDealt.roundToInt()} (+${damageIncreasePercent.roundToInt()}%)"),
                        color = HextechGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Controles y Sliders
        // 1. Armadura / Resistencia Mágica del Enemigo
        Text(
            text = com.example.util.tr("${tr("Armadura / Resistencia Mágica")}: ${rawResistance.roundToInt()}"),
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Slider(
            value = rawResistance,
            onValueChange = { rawResistance = it },
            valueRange = 20f..350f,
            steps = 33,
            colors = SliderDefaults.colors(
                thumbColor = HextechGold,
                activeTrackColor = HextechGold,
                inactiveTrackColor = HextechSurface
            )
        )

        // 2. Letalidad / Penetración Plana
        Text(
            text = com.example.util.tr("${tr("Letalidad / Penetración Plana")}: ${flatPenetration.roundToInt()}"),
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Slider(
            value = flatPenetration,
            onValueChange = { flatPenetration = it },
            valueRange = 0f..60f,
            steps = 12,
            colors = SliderDefaults.colors(
                thumbColor = DangerRed,
                activeTrackColor = DangerRed,
                inactiveTrackColor = HextechSurface
            )
        )

        // 3. Porcentaje de Penetración (% Armor Pen / % Magic Pen)
        Text(
            text = com.example.util.tr("${tr("Penetración Porcentual")}: ${percentPenetration.roundToInt()}%"),
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Slider(
            value = percentPenetration,
            onValueChange = { percentPenetration = it },
            valueRange = 0f..50f,
            steps = 10,
            colors = SliderDefaults.colors(
                thumbColor = HextechCyan,
                activeTrackColor = HextechCyan,
                inactiveTrackColor = HextechSurface
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Veredicto del Coach Táctico
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = HextechSurfaceVariant),
            border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = HextechGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tr("Veredicto del Coach de Élite:"),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                val tacticalAdvice = when {
                    rawResistance >= 180f -> {
                        when (currentLang) {
                            "pt" -> "️ ALVO HÍPER-TANQUE (${rawResistance.roundToInt()} def): Letalidade plana é ineficaz aqui. Priorize Lembranças do Lorde Dominik / Serylda / Cajado do Vazio + Dano por % de Vida (Espada do Rei Destruído / Liandry)."
                            else -> "️ OBJETIVO HIPER-TANQUE (${rawResistance.roundToInt()} def): La Letalidad plana es inútil aquí. Prioriza Dominik / Serylda / Bastón del Vacío + Daño por % de vida (Rey Arruinado / Liandry)."
                        }
                    }
                    rawResistance >= breakEvenArmor -> {
                        when (currentLang) {
                            "pt" -> "️ PONTO DE INFLEXÃO ALCANÇADO: Com ${rawResistance.roundToInt()} de defesa, a Penetração Percentual (${percentPenetration.roundToInt()}%) concede MAIS dano por ouro do que mais Letalidade plana."
                            else -> "️ PUNTO DE INFLEXIÓN ALCANZADO: Con ${rawResistance.roundToInt()} de defensa, la Penetración Porcentual (${percentPenetration.roundToInt()}%) te otorga MÁS daño por oro que comprar más Letalidad plana."
                        }
                    }
                    else -> {
                        when (currentLang) {
                            "pt" -> " ALVO FRÁGIL (${rawResistance.roundToInt()} def): A Letalidade Plana / Penetração plana ignora quase toda a armadura base, causando dano praticamente verdadeiro."
                            else -> " OBJETIVO FRÁGIL (${rawResistance.roundToInt()} def): La Letalidad Plana / Penetración plana ignora casi toda su armadura base, infligiendo daño prácticamente verdadero."
                        }
                    }
                }
                Text(
                    text = com.example.util.tr(tacticalAdvice),
                    color = TextPrimary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
