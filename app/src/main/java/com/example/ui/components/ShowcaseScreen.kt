package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CoachNavStyle
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.tr

/** Galería interactiva para revisar los cinco estilos, destinos y salida animada. */
@Composable
fun ShowcaseScreen(onClose: () -> Unit = {}, modifier: Modifier = Modifier) {
    var style by remember { mutableStateOf(CoachNavStyle.ARC_GLOWING) }
    var selected by remember { mutableIntStateOf(0) }
    val items = CoachNavPreviewItems()
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(tr("Navegación de Coach"), color = HextechGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(tr("Elige un estilo y prueba sus destinos"), color = TextSecondary, fontSize = 13.sp)
            }
            TextButton(onClick = onClose, modifier = Modifier.heightIn(min = 48.dp).testTag("nav_showcase_close")) { Text(tr("Cerrar")) }
        }
        CoachNavStyle.entries.forEach { option ->
            FilterChip(selected = style == option, onClick = { style = option }, label = { Text(tr(option.title)) },
                modifier = Modifier.heightIn(min = 48.dp).testTag("nav_style_${option.id}"))
        }
        Surface(color = HextechSurface, shape = MaterialTheme.shapes.large) {
            Column(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(tr("Vista previa"), color = TextPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 14.dp))
                CoachNavStyleRenderer(style, items, selected, onSelected = { selected = it })
            }
        }
        AnimatedLogoutButton(onLogoutComplete = {}, label = tr("Probar cierre de sesión"), modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun CoachNavStyleRenderer(style: CoachNavStyle, items: List<CoachNavItem>, selectedIndex: Int,
                          onSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    when (style) {
        CoachNavStyle.ARC_GLOWING -> ArcGlowingNavBar(items, selectedIndex, HextechDarkBg, HextechGold, TextSecondary, onSelected, modifier)
        CoachNavStyle.LIQUID -> LiquidNavBar(items, selectedIndex, HextechDarkBg, HextechGold, TextSecondary, onSelected, modifier)
        CoachNavStyle.CURVED_FAB -> CurvedFabNavBar(items, selectedIndex, HextechDarkBg, HextechGold, TextSecondary, onSelected, modifier)
        CoachNavStyle.CURVED_SCOOP -> CurvedScoopFloatingNavBar(items, selectedIndex, HextechDarkBg, HextechGold, TextSecondary, onSelected, modifier)
        CoachNavStyle.NEON_WAVE -> NeonWaveNavBar(items, selectedIndex, HextechDarkBg, HextechGold, TextSecondary, onSelected, modifier)
    }
}
