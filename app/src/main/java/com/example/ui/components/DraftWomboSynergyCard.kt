package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Champion
import com.example.model.DraftSlot
import com.example.ui.theme.*
import com.example.util.tr

data class WomboCombo(
    val title: String,
    val type: String, // "KNOCKUP", "SHOCKWAVE", "HYPERCARRY", "CC_BURST", "AREA_CURSE", "ENCHANTER"
    val champ1: Champion,
    val champ2: Champion,
    val description: String,
    val executionTip: String
)

object WomboComboSynergyDetector {
    fun detectWombos(allies: List<Champion>): List<WomboCombo> {
        val detected = mutableListOf<WomboCombo>()
        if (allies.size < 2) return emptyList()

        val names = allies.associateBy { it.name.lowercase().trim() }

        fun find(vararg targets: String): Champion? {
            for (t in targets) {
                val found = names.values.firstOrNull { 
                    it.name.contains(t, ignoreCase = true) || it.id.contains(t, ignoreCase = true) 
                }
                if (found != null) return found
            }
            return null
        }

        // 1. Yasuo + Airborne Knock-ups (Wombo Supremo)
        val yasuo = find("yasuo")
        val knockupper = find("malphite", "diana", "alistar", "yone", "wukong", "rakan", "gragas", "vi", "nautilus", "lee sin", "xin zhao", "nunu", "sett", "braum")
        if (yasuo != null && knockupper != null && yasuo.id != knockupper.id) {
            detected.add(WomboCombo(
                title = "💥 Wombo-Combo Aéreo Imparable", type = "KNOCKUP", champ1 = knockupper, champ2 = yasuo,
                description = "Iniciación de ${knockupper.name} con Derribo Aéreo masivo activa instantáneamente la Definitiva (H4 - Último Aliento) de Yasuo para prolongar la suspensión.",
                executionTip = "Esperar a que ${knockupper.name} conecte su derribo aéreo sobre 2 o más enemigos antes de presionar la Definitiva de Yasuo para maximizar la penetración de armadura."
            ))
        }

        // 2. Orianna + Ball Delivery / Heavy Engage
        val orianna = find("orianna")
        val ballCarrier = find("malphite", "jarvan", "rakan", "diana", "amumu", "sett", "vi", "hecarim", "alistar", "rengar", "wukong", "camille", "kennen")
        if (orianna != null && ballCarrier != null && orianna.id != ballCarrier.id) {
            detected.add(WomboCombo(
                title = "🌀 Balón de Choque & Erradicación", type = "SHOCKWAVE", champ1 = ballCarrier, champ2 = orianna,
                description = "El balón de Orianna colocado sobre ${ballCarrier.name} permite ejecutar una Definitiva (H4 - Orden: Onda de Choque) en el epicentro del salto.",
                executionTip = "Colocar H3 (Proteger) sobre ${ballCarrier.name} justo antes del dive; activar H4 inmediatamente al momento del impacto."
            ))
        }

        // 3. Amumu / Malphite / Leona + Miss Fortune / Samira / Katarina (Cadena de CC en Área & Cataclismo)
        val aoeLockdown = find("amumu", "malphite", "leona", "seraphine", "sona", "galio", "jarvan")
        val aoeExecutioner = find("miss fortune", "samira", "katarina", "kennen", "fiddlesticks")
        if (aoeLockdown != null && aoeExecutioner != null && aoeLockdown.id != aoeExecutioner.id) {
            detected.add(WomboCombo(
                title = "🔥 Tormenta en Área & Encierro Masivo", type = "AREA_CURSE", champ1 = aoeLockdown, champ2 = aoeExecutioner,
                description = "El aturdimiento masivo en área de ${aoeLockdown.name} congela al equipo rival dentro de la Definitiva (H4) destructiva de ${aoeExecutioner.name}.",
                executionTip = "Pelear en zonas cerradas (fosas de Dragón/Barón o cuellos de botella de la jungla). ${aoeExecutioner.name} debe esperar a que se gasten las interrupciones rivales."
            ))
        }

        // 4. Diana + Yasuo / Kennen / Sett (Atracción Lunar & Devastación)
        val diana = find("diana")
        val aoeFollowUp = find("yasuo", "kennen", "sett", "miss fortune", "samira")
        if (diana != null && aoeFollowUp != null && diana.id != aoeFollowUp.id && aoeFollowUp.id != knockupper?.id) {
            detected.add(WomboCombo(
                title = "🌙 Atracción Lunar & Aniquilación", type = "SHOCKWAVE", champ1 = diana, champ2 = aoeFollowUp,
                description = "La Definitiva (H4 - Lluvia de Luna) de Diana agrupa y derriba a todos los enemigos cercanos hacia el centro, creando el agrupamiento perfecto para ${aoeFollowUp.name}.",
                executionTip = "Diana entra con H3 (Impulso Lunar), canaliza H4 y ${aoeFollowUp.name} descarga todo su arsenal en el centro."
            ))
        }

        // 5. Lulu/Yuumi/Janna/Milio + Hypercarry Protection
        val enchanter = find("lulu", "yuumi", "milio", "janna", "karma")
        val hypercarry = find("vayne", "jinx", "kog'maw", "twitch", "tristana", "zeri", "samira", "kaisa", "kai'sa")
        if (enchanter != null && hypercarry != null && enchanter.id != hypercarry.id) {
            detected.add(WomboCombo(
                title = "🛡️ Hypercarry Blindado & Velocidad Letal", type = "HYPERCARRY", champ1 = enchanter, champ2 = hypercarry,
                description = "Buffs de velocidad de ataque, escudos y utilidad de ${enchanter.name} transforman a ${hypercarry.name} en una máquina imparable de daño sostenido.",
                executionTip = "Reservar la Definitiva (H4) del soporte exclusivamente para contrarrestar el dive o flanqueo enemigo sobre el tirador."
            ))
        }

        // 6. Hard CC Support + Aggressive ADC All-In
        val aggressiveAdc = find("samira", "lucian", "draven", "kalista", "nilah", "tristana", "kaisa", "xayah")
        val hardCcSupport = find("nautilus", "leona", "thresh", "pyke", "blitzcrank", "braum", "alistar", "rakan")
        if (aggressiveAdc != null && hardCcSupport != null && aggressiveAdc.id != hardCcSupport.id) {
            detected.add(WomboCombo(
                title = "⛓️ Cadena de CC & All-In Explosivo", type = "CC_BURST", champ1 = hardCcSupport, champ2 = aggressiveAdc,
                description = "El control de masas encadenado de ${hardCcSupport.name} garantiza el combo completo de burst de ${aggressiveAdc.name}.",
                executionTip = "En niveles 2, 3 y 5 buscar all-in inmediato tras el primer gancho o aturdimiento acertado."
            ))
        }

        // 7. Lucian + Nami
        val lucian = find("lucian")
        val nami = find("nami")
        if (lucian != null && nami != null) {
            detected.add(WomboCombo(
                title = "🌊 Electro-Ráfaga Acuática", type = "ENCHANTER", champ1 = nami, champ2 = lucian,
                description = "La Habilidad 3 (Bendición de la Marea) de Nami se activa de golpe con los dobles disparos pasivos de Lucian, infligiendo daño mágico devastador.",
                executionTip = "Nami aplica H3 sobre Lucian justo cuando este usa su desplazamiento (H3) hacia adelante para un intercambio rápido."
            ))
        }

        // 8. Xayah + Rakan
        val xayah = find("xayah")
        val rakan = find("rakan")
        if (xayah != null && rakan != null) {
            detected.add(WomboCombo(
                title = "💖 Pareja Letal: Baile de Plumas", type = "DUO_SYNERGY", champ1 = rakan, champ2 = xayah,
                description = "Interacciones únicas: Rakan obtiene mayor rango de salto hacia Xayah con H3 y comparten el regreso a base acelerado.",
                executionTip = "Rakan inicia desde larga distancia con H2 o Definitiva (H4), mientras Xayah despliega plumas con H1 y H2 para encadenar enraizamiento con H3."
            ))
        }

        // 9. Jarvan IV + Galio
        val jarvan = find("jarvan")
        val galio = find("galio")
        if (jarvan != null && galio != null) {
            detected.add(WomboCombo(
                title = "⚔️ Entrada Heroica & Cataclismo", type = "AREA_CURSE", champ1 = jarvan, champ2 = galio,
                description = "Jarvan encierra a múltiples enemigos en su arena con Definitiva (H4 - Cataclismo), creando el aterrizaje perfecto para la Definitiva de Galio.",
                executionTip = "Jarvan inicia con su combo H3+H1 seguido de H4; Galio lanza su Definitiva (H4) sobre Jarvan para encadenar provocación con H2."
            ))
        }

        // 10. Ashe + Braum
        val ashe = find("ashe")
        val braum = find("braum")
        if (ashe != null && braum != null) {
            detected.add(WomboCombo(
                title = "❄️ Freljord: Hielo Eterno", type = "CC_BURST", champ1 = braum, champ2 = ashe,
                description = "La pasiva de Braum (Golpes Conmocionantes) se acumula a máxima velocidad con la Habilidad 1 (Concentración Máxima) de Ashe.",
                executionTip = "Braum aplica la primera marca con H1 o básico; Ashe activa H1 para aturdir al instante."
            ))
        }

        // 11. Caitlyn + Morgana
        val caitlyn = find("caitlyn")
        val morgana = find("morgana")
        if (caitlyn != null && morgana != null) {
            detected.add(WomboCombo(
                title = "🎯 Trampas Encadenadas (Snare City)", type = "CC_BURST", champ1 = morgana, champ2 = caitlyn,
                description = "Si Morgana conecta su H1 (Hechizo Oscuro), Caitlyn coloca su H2 (Trampa para Yordles) bajo los pies del enemigo para duplicar la inmovilización.",
                executionTip = "Morgana lanza H1; Caitlyn coloca inmediatamente su H2 bajo el objetivo inmovilizado y descarga Disparo a la Cabeza."
            ))
        }

        // 12. Twitch/Evelynn + Yuumi
        val invisible = find("twitch", "evelynn", "rengar", "kha'zix", "khazix")
        val yuumi = find("yuumi")
        if (invisible != null && yuumi != null) {
            detected.add(WomboCombo(
                title = "👻 Submarino Invisible", type = "DIVE", champ1 = yuumi, champ2 = invisible,
                description = "Yuumi se vuelve invisible junto al portador, permitiendo canalizar su Definitiva (H4) desde el sigilo absoluto.",
                executionTip = "El asesino flanquea en sigilo; Yuumi activa H4 (Últimas Páginas) para inmovilizar a la retaguardia enemiga sin previo aviso."
            ))
        }

        // 13. Sett + Kennen / Brand (Atracción y Tormenta Eléctrica)
        val sett = find("sett")
        val aoeMage = find("kennen", "brand", "ziggs", "vex")
        if (sett != null && aoeMage != null) {
            detected.add(WomboCombo(
                title = "⚡ El Gran Espectáculo & Tormenta", type = "SHOCKWAVE", champ1 = sett, champ2 = aoeMage,
                description = "Sett estrella al tanque rival sobre los carries enemigos con su Definitiva (H4), mientras ${aoeMage.name} desata su tormenta en área.",
                executionTip = "Sett ultea al tanque enemigo hacia la retaguardia rival; ${aoeMage.name} entra con H4 para aniquilar al grupo aturdido."
            ))
        }

        // 14. Seraphine + Sona / Amumu (Armonía Sinfónica & CC Infinito)
        val seraphine = find("seraphine")
        val sona = find("sona", "amumu")
        if (seraphine != null && sona != null) {
            detected.add(WomboCombo(
                title = "🎶 Armonía Sinfónica & Encanto Infinito", type = "AREA_CURSE", champ1 = sona, champ2 = seraphine,
                description = "La Definitiva (H4) de ${sona.name} inmoviliza al equipo enemigo, permitiendo a Seraphine extender el alcance de su Definitiva (H4) a través de todos ellos.",
                executionTip = "Lanzar la Definitiva de ${sona.name} primero; Seraphine apunta su Definitiva a través de los aliados y enemigos para duplicar el alcance y encanto."
            ))
        }

        // 15. Yone + Lillia
        val yone = find("yone")
        val lillia = find("lillia")
        if (yone != null && lillia != null) {
            detected.add(WomboCombo(
                title = "💤 Sueño Inevitable & Destino Sellado", type = "AREA_CURSE", champ1 = lillia, champ2 = yone,
                description = "Lillia duerme a múltiples enemigos con su Definitiva (H4 - Arrullo Rítmico), dejando el blanco inmóvil para el Destino Sellado (H4) de Yone.",
                executionTip = "Lillia activa H4 sobre los enemigos marcados; Yone carga su H4 para impactar a todos los objetivos dormidos simultáneamente."
            ))
        }

        // 16. Camille + Galio / Shen
        val camille = find("camille")
        val protector = find("galio", "shen")
        if (camille != null && protector != null) {
            detected.add(WomboCombo(
                title = "⛓️ Aislamiento Hextech & Caída Heroica", type = "DIVE", champ1 = protector, champ2 = camille,
                description = "Camille aísla al carry rival en su Ultimátum Hextech (H4) sin permitirle escapar, mientras ${protector.name} aterriza sobre ella para asegurarlo.",
                executionTip = "Camille encierra al tirador con H4; ${protector.name} lanza su Definitiva global sobre Camille al instante."
            ))
        }

        // 17. Tristana + Alistar / Leona
        val tristana = find("tristana")
        val diveEngage = find("alistar", "leona", "nautilus")
        if (tristana != null && diveEngage != null) {
            detected.add(WomboCombo(
                title = "💣 Bomba Explosiva & Derribo Coordinado", type = "CC_BURST", champ1 = diveEngage, champ2 = tristana,
                description = "El control de masas de ${diveEngage.name} le da a Tristana el tiempo exacto para cargar al 100% su H3 (Carga Explosiva) y reiniciar su H2 (Salto Cohete).",
                executionTip = "El soporte inicia el CC; Tristana coloca H3, descarga 4 ataques básicos y salta con H2 para rematar y reiniciar el salto."
            ))
        }

        return detected.distinctBy { "${it.champ1.name}_${it.champ2.name}" }
    }
}

/**
 * Tarjeta de Sinergia y Wombo-Combo con Efecto Neón Hextech animado.
 */
@Composable
fun DraftWomboSynergyCard(
    wombo: WomboCombo,
    onChampionClick: (Champion) -> Unit,
    modifier: Modifier = Modifier,
    isEnemy: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neonPulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderGlow"
    )

    val neonGradient = Brush.horizontalGradient(
        listOf(
            HextechCyan.copy(alpha = borderAlpha),
            HextechGold.copy(alpha = borderAlpha),
            TierSPlusColor.copy(alpha = borderAlpha)
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.5.dp, neonGradient, RoundedCornerShape(14.dp))
            .shadow(6.dp, RoundedCornerShape(14.dp), spotColor = HextechCyan),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF07121E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header con Chip Neón Pulsante
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(HextechGold.copy(alpha = 0.25f), HextechCyan.copy(alpha = 0.25f))
                                )
                            )
                            .border(1.dp, HextechGold, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = null,
                                tint = HextechGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tr("💥 Sinergia Letal"),
                                color = HextechGold,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = wombo.title,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Campeones involucrados en el Combo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(HextechDarkBg.copy(alpha = 0.8f))
                    .border(0.8.dp, HextechCardBorder, RoundedCornerShape(10.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Campeón 1
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onChampionClick(wombo.champ1) }
                ) {
                    ChampionAvatar(champion = wombo.champ1, size = 40.dp, showTierBadge = false)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = wombo.champ1.name,
                            color = TextPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tr(wombo.champ1.primaryRole.shortName),
                            color = HextechCyan,
                            fontSize = 10.sp
                        )
                    }
                }

                // Ícono de Enlace Neón
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(HextechGold.copy(alpha = 0.2f))
                        .border(1.dp, HextechGold.copy(alpha = borderAlpha), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = HextechGold,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Campeón 2
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onChampionClick(wombo.champ2) }
                ) {
                    ChampionAvatar(champion = wombo.champ2, size = 40.dp, showTierBadge = false)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = wombo.champ2.name,
                            color = TextPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tr(wombo.champ2.primaryRole.shortName),
                            color = HextechCyan,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Descripción y Guía de Ejecución
            Text(
                text = wombo.description,
                color = TextPrimary.copy(alpha = 0.9f),
                fontSize = 11.5.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0F2336))
                    .border(0.6.dp, HextechCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = HextechCyan,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tip Coach: ${wombo.executionTip}",
                    color = HextechCyanLight,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
