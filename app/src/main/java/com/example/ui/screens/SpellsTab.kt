package com.example.ui.screens

import com.example.ui.components.CoachFilterChip as FilterChip
import com.example.ui.components.CoachButton as Button
import com.example.ui.components.CoachIconButton as IconButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.ui.components.coachClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.example.util.tr
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.ui.components.FormattedWildRiftText
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WildRiftRepository
import com.example.model.SummonerSpellItem
import com.example.ui.components.AppAssetImage
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.HextechSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SpellsTab() {
    val lang = com.example.util.currentAppLanguage()
    var searchQuery by remember { mutableStateOf("") }
    var isGridView by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("TODOS") }
    var selectedSpell by remember { mutableStateOf<SummonerSpellItem?>(null) }

    val allCategories = remember(com.example.data.WildRiftRepository.summonerSpells) {
        com.example.data.WildRiftRepository.summonerSpells.map { it.category }.distinct().sorted()
    }

    val filterOptions = remember(allCategories) {
        val options = mutableListOf("TODOS" to "Todos")
        allCategories.forEach { cat ->
            options.add(cat to cat)
        }
        options
    }

    val filteredSpells = remember(searchQuery, selectedFilter, lang, com.example.data.WildRiftRepository.summonerSpells) {
        WildRiftRepository.summonerSpells.filter { spell ->
            val matchesFilter = selectedFilter == "TODOS" || spell.category.equals(selectedFilter, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    spell.getLocalizedName(lang).contains(searchQuery, ignoreCase = true) ||
                    spell.getLocalizedDescription(lang).contains(searchQuery, ignoreCase = true) ||
                    com.example.util.trStr(lang, spell.category).contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        // Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HextechSurfaceVariant, RoundedCornerShape(8.dp))
                .border(0.5.dp, HextechGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = com.example.util.tr("${filteredSpells.size} " + tr("Hechizos de Invocador")),
                color = HextechCyan,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )

            // View mode toggle
            Row(
                modifier = Modifier
                    .background(HextechSurface, RoundedCornerShape(6.dp))
                    .border(0.5.dp, HextechCardBorder, RoundedCornerShape(6.dp))
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isGridView) HextechCyan else Color.Transparent)
                        .coachClickable { isGridView = true }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = tr("Cuadrícula"),
                        color = if (isGridView) HextechDarkBg else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (!isGridView) HextechCyan else Color.Transparent)
                        .coachClickable { isGridView = false }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = tr("Detallado"),
                        color = if (!isGridView) HextechDarkBg else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(tr("Buscar hechizo (ej. Destello, Prender, Castigo)..."), color = TextMuted, fontSize = 12.5.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HextechGold) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = tr("Cerrar"), tint = TextMuted)
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = HextechCyan,
                unfocusedBorderColor = HextechCardBorder,
                focusedContainerColor = HextechSurface,
                unfocusedContainerColor = HextechSurface
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            filterOptions.forEach { (key, label) ->
                FilterChip(
                    selected = selectedFilter == key,
                    onClick = { selectedFilter = key },
                    label = { Text(tr(label), fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = HextechCyan,
                        selectedLabelColor = HextechDarkBg
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 30.dp)
            ) {
                items(filteredSpells) { spell ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 112.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .testTag("catalog_spell_${spell.id}")
                            .coachClickable { selectedSpell = spell },
                        colors = CardDefaults.cardColors(containerColor = HextechSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 8.dp)
                        ) {
                            AppAssetImage(
                                url = spell.iconUrl,
                                contentDescription = spell.name,
                                fallbackText = spell.name,
                                modifier = Modifier.size(48.dp),
                                borderColor = HextechCyan,
                                shape = RoundedCornerShape(10.dp)
                            )
                            Text(
                                text = tr(spell.name),
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 2,
                                minLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 13.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = com.example.util.tr("CD ${spell.cooldown}"),
                                color = HextechCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredSpells) { spell ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("catalog_spell_${spell.id}")
                            .coachClickable { selectedSpell = spell },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            AppAssetImage(
                                url = spell.iconUrl,
                                contentDescription = spell.name,
                                fallbackText = spell.name,
                                modifier = Modifier.size(44.dp),
                                borderColor = HextechCyan,
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tr(spell.name), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(HextechGold.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(com.example.util.tr("CD: ${spell.cooldown}"), color = HextechCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(HextechGold.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(tr(spell.category), color = HextechCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                val spellPresentation = remember(spell.description, lang) {
                                    com.example.util.SpellCatalogFormatting.split(spell.getLocalizedDescription(lang), lang)
                                }
                                if (spellPresentation.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    FormattedWildRiftText(
                                        text = spellPresentation.description,
                                        color = TextPrimary.copy(alpha = 0.9f),
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }

    // Spell Detail Dialog
    selectedSpell?.let { spell ->
        AlertDialog(
            onDismissRequest = { selectedSpell = null },
            containerColor = HextechSurface,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppAssetImage(
                        url = spell.iconUrl,
                        contentDescription = spell.name,
                        fallbackText = spell.name,
                        modifier = Modifier.size(48.dp),
                        borderColor = HextechCyan,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = tr(spell.name),
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = com.example.util.tr(tr("Enfriamiento:") + " ${spell.cooldown}"),
                            color = HextechCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    val spellPresentation = remember(spell.description, lang) {
                        com.example.util.SpellCatalogFormatting.split(spell.getLocalizedDescription(lang), lang)
                    }
                    if (spellPresentation.mapLabels.isNotEmpty()) {
                        com.example.ui.components.SpellMapLabels(spellPresentation.mapLabels, Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (spellPresentation.description.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(HextechSurface, RoundedCornerShape(8.dp))
                                .border(0.5.dp, HextechCardBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            FormattedWildRiftText(
                                text = spellPresentation.description,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = tr(" Recomendación de Invocador:"),
                        color = HextechCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tr(when (spell.id) {
                            "flash" -> "Imprescindible en el 99% de las partidas para reposicionarse, iniciar peleas de equipo o escapar por encima de muros."
                            "ignite" -> "Clave para asesinos y soportes agresivos para asegurar asesinatos en juego temprano y anular curaciones de campeones como Aatrox, Soraka o Dr. Mundo."
                            "smite" -> "Obligatorio para el rol de Jungla para asegurar monstruos épicos (Dragones, Heraldo, Barón) y farmear eficientemente."
                            "exhaust" -> "Vital para neutralizar a hipercarries o asesinos rivales en peleas grupales reduciendo su daño y movilidad drásticamente."
                            "barrier" -> "Excelente para tiradores o magos de ráfaga para resistir emboscadas o burst sorpresa en línea."
                            "ghost" -> "Ideal para campeones con movilidad continua como Darius, Olaf, Singed o Gwen para evitar que los enemigos escapen."
                            "heal" -> "Ideal para el Tirador (ADC) en la línea de Dragón para sobrevivir al burst y salvar al soporte en 2vs2."
                            else -> "Uso situacional según la composición y mapa."
                        }),
                        color = TextMuted,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedSpell = null },
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold, contentColor = HextechDarkBg),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(tr("Cerrar"), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// ====================================================================
// TAB 5: OBJETIVOS DE MAPA (MONSTRUOS ÉPICOS DE WILD RIFT)
// ====================================================================
