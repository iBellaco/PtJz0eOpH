package com.example.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TextPrimary

object WildRiftDamageColors {
    val PhysicalDamage = Color(0xFFFF8C00) // Naranja vivo para daño físico / DA / AD
    val MagicDamage = Color(0xFF60A5FA)    // Azul mágico / Celeste / PH / AP (o 0xFF9370DB / 0xFF818CF8)
    val TrueDamage = Color(0xFFFFF176)     // Amarillo brillante / Blanco puro para daño verdadero
    val AdaptiveDamage = Color(0xFFE879F9) // Rosa magenta para daño adaptable / fuerza adaptable
    val HealingAndLife = Color(0xFF4ADE80) // Verde esmeralda para vida, curación, escudos, defensas
    val AttackSpeed = Color(0xFFFDE047)    // Amarillo relámpago para velocidad de ataque
    val MovementSpeed = Color(0xFF38BDF8)  // Celeste cian para velocidad de movimiento
    val AbilityHaste = Color(0xFFA78BFA)   // Púrpura para velocidad de habilidades / enfriamiento
    val ManaColor = Color(0xFF38BDF8)      // Azul para maná
    val EnergyColor = Color(0xFFFBBF24)    // Dorado para energía
    val OmnivampColor = Color(0xFFF87171)  // Rojo carmesí para omnisucción / robo de vida
    val CriticalColor = Color(0xFFEF4444)  // Rojo fuerte para críticos
}

/**
 * Formats a description text with vibrant, distinct colors for:
 * - Daño físico (Orange)
 * - Daño mágico (Light Blue / Magic)
 * - Daño verdadero (Gold / White)
 * - Daño adaptable (Magenta)
 * - Curación / Vida / Escudos (Green)
 * - Velocidad de ataque / movimiento (Yellow / Sky Blue)
 * - Maná / Energía (Blue / Yellow)
 */
fun formatWildRiftDescription(text: String, defaultColor: Color = TextPrimary): AnnotatedString {
    return try {
        val annotated = buildAnnotatedString {
            append(text)

            // Rule helper
            fun highlightMatches(regex: Regex, color: Color, isBold: Boolean = true) {
                try {
                    regex.findAll(text).forEach { match ->
                        val start = match.range.first.coerceIn(0, text.length)
                        val end = (match.range.last + 1).coerceIn(start, text.length)
                        if (start < end) {
                            addStyle(
                                style = SpanStyle(
                                    color = color,
                                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold
                                ),
                                start = start,
                                end = end
                            )
                        }
                    }
                } catch (_: Exception) {}
            }

            // 1. Daño Verdadero (Mayor prioridad para evitar que "daño" genérico lo solape)
            highlightMatches(
                Regex("(?i)(?<![\\p{L}\\p{N}_])(daño verdadero( adicional)?|dano verdadeiro( adicional)?|true damage)(?![\\p{L}\\p{N}_])"),
                WildRiftDamageColors.TrueDamage
            )

            // 2. Daño Adaptable / Fuerza Adaptable
            highlightMatches(
                Regex("(?i)(?<![\\p{L}\\p{N}_])(daño adaptable( adicional)?|fuerza adaptable|adaptable|força adaptativa|dano adaptativo( adicional)?|adaptive force|adaptive damage)(?![\\p{L}\\p{N}_])"),
                WildRiftDamageColors.AdaptiveDamage
            )

            // 3. Daño Mágico / Poder de Habilidad / PH / AP
            highlightMatches(
                Regex("(?i)(?<![\\p{L}\\p{N}_])(daño mágico( adicional)?|daño magico( adicional)?|poder de habilidad|(?-i:PH)|dano mágico( adicional)?|poder de habilidade|resistência mágica|penetração mágica|magic damage|(?-i:AP)|resistencia mágica|resistencia magica|penetración mágica|penetracion magica)(?![\\p{L}\\p{N}_])"),
                WildRiftDamageColors.MagicDamage
            )

            // 4. Daño Físico / Daño de Ataque / DA / AD
            highlightMatches(
                Regex("(?i)(?<![\\p{L}\\p{N}_])(daño físico( adicional)?|daño fisico( adicional)?|daño de ataque|(?-i:DA)|dano físico( adicional)?|dano de ataque|letalidade|penetração de armadura|physical damage|(?-i:AD)|letalidad|armadura|penetración de armadura|penetracion de armadura)(?![\\p{L}\\p{N}_])"),
                WildRiftDamageColors.PhysicalDamage
            )

            // 5. Curación, Vida, Escudo
            highlightMatches(
                Regex("(?i)(?<![\\p{L}\\p{N}_])(cura|curação|curación|curacion|vida restaurada|vida adicional|vida máxima|vida maxima|salud máxima|salud maxima|escudo(s)?|roubo de vida|onivampirismo|salud|omnisucción|omnisuccion|robo de vida|succión física|succion fisica|vampiro( físico| mágico)?|vampirismo)(?![\\p{L}\\p{N}_])"),
                WildRiftDamageColors.HealingAndLife
            )

            // 5.5. Críticos
            highlightMatches(
                Regex("(?i)(?<![\\p{L}\\p{N}_])(acerto(s)? crítico(s)?|dano crítico|chance de acerto crítico|tasa crítica|tasa critica|daño crítico|daño critico|golpe(s)? crítico(s)?|impacto(s)? crítico(s)?|probabilidad de (golpe )?crítico)(?![\\p{L}\\p{N}_])"),
                WildRiftDamageColors.CriticalColor
            )

            // 6. Velocidades y Aceleración
            highlightMatches(
                Regex("(?i)(?<![\\p{L}\\p{N}_])(velocidade de ataque|velocidade de movimento|aceleração (de habilidade(s)?|da habilidade ultimate)|tempo de recarga|velocidad de ataque|velocidad de movimiento|velocidad de habilidades( básicas)?|velocidad de habilidad definitiva|aceleración de habilidad(es)?|enfriamiento)(?![\\p{L}\\p{N}_])"),
                WildRiftDamageColors.AttackSpeed
            )

            // 7. Maná / Energía
            highlightMatches(
                Regex("(?i)(?<![\\p{L}\\p{N}_])(maná( máximo)?|mana|regeneração de mana|energía|energia|regeneración de maná|regeneracion de mana)(?![\\p{L}\\p{N}_])"),
                WildRiftDamageColors.ManaColor
            )

            // 8. Support Item Restriction Warning
            highlightMatches(
                Regex("(?i)(?<![\\p{L}\\p{N}_])(Este objeto es para los apoyos.*?activará\\.)"),
                WildRiftDamageColors.CriticalColor
            )

            // 9. Números, estadísticas y ratios entre paréntesis (ej. (+15), (20%), (30s), (10 a 30))
            highlightMatches(
                Regex("\\(([+−-]?\\d+(?:[.,]\\d+)?(?:%|s| seg| CD| adic| ad| ap| oro)?(?:\\s*(?:a|-|/)\\s*\\d+(?:[.,]\\d+)?(?:%|s)?)?)\\)"),
                Color(0xFFFBBF24)
            )
        }
        annotated
    } catch (_: Exception) {
        AnnotatedString(text)
    }
}

@Composable
fun FormattedWildRiftText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = TextPrimary,
    fontSize: TextUnit = 12.sp,
    lineHeight: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    Text(
        text = formatWildRiftDescription(com.example.util.tr(text), color),
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        lineHeight = lineHeight,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}
