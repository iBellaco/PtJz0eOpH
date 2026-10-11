package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.util.tr

/** A searchable, scrollable catalogue keeps a single live preview above Apply. */
@Composable
fun CoachNavStylePicker(selected: CoachNavStyle, isPremium: Boolean,
    onSelected: (CoachNavStyle) -> Unit, onPremiumRequired: () -> Unit) {
    var open by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { if (isPremium) onSelected(CoachNavStyle.entries[(selected.ordinal + CoachNavStyle.entries.size - 1) % CoachNavStyle.entries.size]) else onPremiumRequired() },
            modifier = Modifier.size(48.dp).testTag("theme_nav_previous")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, tr("Estilo anterior"), tint = HextechCyan)
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(tr(selected.title), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("${selected.ordinal + 1} / ${CoachNavStyle.entries.size}", color = TextSecondary, fontSize = 11.sp)
        }
        IconButton(onClick = { if (isPremium) onSelected(CoachNavStyle.entries[(selected.ordinal + 1) % CoachNavStyle.entries.size]) else onPremiumRequired() },
            modifier = Modifier.size(48.dp).testTag("theme_nav_next")) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, tr("Estilo siguiente"), tint = HextechCyan)
        }
    }
    OutlinedButton(onClick = { if (isPremium) { query = ""; open = true } else onPremiumRequired() },
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("theme_nav_catalog")) {
        Text(tr("Explorar estilos"))
    }
    if (open) Dialog(onDismissRequest = { open = false }) {
        Surface(modifier = Modifier.fillMaxWidth().fillMaxHeight(.8f), color = HextechSurface, shape = MaterialTheme.shapes.large) {
            Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(tr("Barra de navegación"), color = HextechGold, fontWeight = FontWeight.Bold)
                OutlinedTextField(query, { query = it }, label = { Text(tr("Buscar estilo")) },
                    leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("theme_nav_search"))
                val matching = CoachNavStyle.entries.filter { tr(it.title).contains(query.trim(), ignoreCase = true) }
                LazyVerticalGrid(columns = GridCells.Adaptive(120.dp), modifier = Modifier.fillMaxWidth()
                    .weight(1f).testTag("theme_nav_catalog_grid"),
                    verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(matching, key = { it.id }) { style ->
                        OutlinedButton(onClick = { onSelected(style); open = false },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp).testTag("theme_nav_style_${style.id}"),
                            border = BorderStroke(if (selected == style) 2.dp else 1.dp, if (selected == style) HextechCyan else HextechCardBorder)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (selected == style) Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp), tint = HextechCyan)
                                Text(tr(style.title), color = TextPrimary, fontSize = 12.sp)
                            }
                        }
                    }
                }
                if (matching.isEmpty()) Text(tr("Sin resultados"), color = TextSecondary)
                TextButton(onClick = { open = false }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("theme_nav_catalog_close")) {
                    Text(tr("Cerrar"))
                }
            }
        }
    }
}
