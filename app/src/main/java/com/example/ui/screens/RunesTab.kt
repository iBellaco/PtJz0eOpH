package com.example.ui.screens

import com.example.ui.components.CoachFilterChip as FilterChip
import com.example.ui.components.CoachButton as Button
import com.example.ui.components.CoachIconButton as IconButton
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WildRiftRepository
import com.example.model.RuneItem
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
internal fun RunesTab() {
    val lang = com.example.util.currentAppLanguage()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("TODOS") }
    var isGridView by remember { mutableStateOf(true) }
    var showFilterChips by remember { mutableStateOf(true) }
    var selectedRune by remember { mutableStateOf<RuneItem?>(null) }

    val allCategories = remember(com.example.data.WildRiftRepository.runes) {
        val cats = com.example.data.WildRiftRepository.runes.map { it.category }.distinct()
        cats.sortedBy { if (it.contains("Clave", ignoreCase = true) || it.contains("Keystone", ignoreCase = true)) 0 else 1 }
    }

    val filterOptions = remember(allCategories) {
        val options = mutableListOf("TODOS" to "Todos")
        allCategories.forEach { cat ->
            options.add(cat to cat)
        }
        options
    }

    val filteredRunes = remember(searchQuery, selectedFilter, lang, com.example.data.WildRiftRepository.runes) {
        WildRiftRepository.runes.filter { rune ->
            val matchesCategory = selectedFilter == "TODOS" || rune.category.equals(selectedFilter, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    rune.getLocalizedName(lang).contains(searchQuery, ignoreCase = true) ||
                    rune.getLocalizedDescription(lang).contains(searchQuery, ignoreCase = true) ||
                    com.example.util.trStr(lang, rune.category).contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    val treeCategories = remember(filteredRunes) {
        val result = mutableListOf<Pair<String, List<RuneItem>>>()
        val groups = filteredRunes.groupBy { it.category }

        val claveKey = groups.keys.firstOrNull { it.contains("Clave", ignoreCase = true) || it.contains("Keystone", ignoreCase = true) }
        if (claveKey != null) {
            result.add(claveKey to (groups[claveKey] ?: emptyList()))
        }

        groups.forEach { (cat, items) ->
            if (cat != claveKey) {
                result.add(cat to items)
            }
        }
        result
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        // Status & View Bar
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
                text = com.example.util.tr("${filteredRunes.size} " + tr("Runas Oficiales")),
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
            placeholder = { Text(tr("Buscar runa (ej. Conquistador, Banda de Flujo)..."), color = TextMuted, fontSize = 12.5.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HextechCyan) },
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

        // Collapsible Header for Rune Categories
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .coachClickable { showFilterChips = !showFilterChips }
                .padding(vertical = 4.dp, horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = HextechCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = tr("Filtrar por Categoría"),
                    color = HextechCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                if (!showFilterChips) {
                    Spacer(modifier = Modifier.width(6.dp))
                    val currentLabel = filterOptions.firstOrNull { it.first == selectedFilter }?.second ?: "Todos"
                    Surface(
                        color = HextechGold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, HextechGold.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = tr(currentLabel),
                            color = HextechGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            IconButton(
                onClick = { showFilterChips = !showFilterChips },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (showFilterChips) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = com.example.util.trNullable(if (showFilterChips) tr("Minimizar filtros") else tr("Expandir filtros")),
                    tint = HextechGold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = showFilterChips,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            // Category Filter Chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                filterOptions.forEach { (key, label) ->
                    val count = if (key == "TODOS") WildRiftRepository.runes.size else WildRiftRepository.runes.count { it.category.equals(key, ignoreCase = true) }
                    val isSelected = selectedFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = key },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tr(label), fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = com.example.util.tr("($count)"),
                                    color = if (isSelected) HextechDarkBg else HextechGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HextechCyan,
                            selectedLabelColor = HextechDarkBg
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isGridView) {
            // GRID / TREE VIEW LIKE WR-META
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                treeCategories.forEachIndexed { catIdx, (categoryName, runesInCat) ->
                    item(key = "tree_cat_${catIdx}_${categoryName}") {
                        val catColor = when {
                            categoryName.contains("clave", ignoreCase = true) -> HextechGold
                            categoryName.contains("brujer", ignoreCase = true) -> Color(0xFF6C75F0)
                            categoryName.contains("dominac", ignoreCase = true) -> Color(0xFFE84057)
                            categoryName.contains("precis", ignoreCase = true) -> Color(0xFFF3C258)
                            categoryName.contains("valor", ignoreCase = true) -> Color(0xFF4AC27E)
                            categoryName.contains("inspirac", ignoreCase = true) -> HextechCyan
                            else -> HextechCyan
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = HextechSurface.copy(alpha = 0.95f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, catColor.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                ) {
                                    Text(
                                        text = tr(categoryName).uppercase(),
                                        color = catColor,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.5.sp,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = com.example.util.tr("(${runesInCat.size})"),
                                        color = catColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    runesInCat.forEach { rune ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier
                                                .width(68.dp)
                                                .height(86.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .coachClickable { selectedRune = rune }
                                                .background(HextechSurface.copy(alpha = 0.6f))
                                                .border(0.5.dp, HextechCardBorder.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 4.dp, vertical = 6.dp)
                                        ) {
                                            AppAssetImage(
                                                url = rune.iconUrl,
                                                contentDescription = rune.name,
                                                fallbackText = rune.name,
                                                modifier = Modifier.size(40.dp),
                                                borderColor = catColor,
                                                shape = CircleShape
                                            )
                                            Text(
                                                text = tr(rune.name),
                                                color = TextPrimary,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 2,
                                                minLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                lineHeight = 11.sp,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        } else {
            // DETAILED LIST VIEW
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredRunes, key = { rune -> "rune_det_${rune.id}_${rune.name}" }) { rune ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .coachClickable { selectedRune = rune },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = HextechSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            AppAssetImage(
                                url = rune.iconUrl,
                                contentDescription = rune.name,
                                fallbackText = rune.name,
                                modifier = Modifier.size(44.dp),
                                borderColor = HextechCyan,
                                shape = CircleShape
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tr(rune.name), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(HextechCyan.copy(alpha = 0.12f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(tr(rune.category), color = HextechCyan, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                FormattedWildRiftText(
                                    text = tr(rune.description),
                                    color = TextPrimary.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
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

    // Rune Detail Dialog
    selectedRune?.let { rune ->
        AlertDialog(
            onDismissRequest = { selectedRune = null },
            containerColor = HextechSurface,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppAssetImage(
                        url = rune.iconUrl,
                        contentDescription = rune.name,
                        fallbackText = rune.name,
                        modifier = Modifier.size(48.dp),
                        borderColor = HextechGold,
                        shape = CircleShape
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = tr(rune.name),
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = tr("Rama") + ": " + tr(rune.category),
                            color = HextechCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(HextechSurface, RoundedCornerShape(8.dp))
                            .border(0.5.dp, HextechCardBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        FormattedWildRiftText(
                            text = tr(rune.description),
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = tr(" Consejo del Coach:"),
                        color = HextechGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tr(when (rune.name.lowercase()) {
                            "electrocutar" -> " Ideal para combos cortos de asesinos o magos que buscan estallar a un rival rápido."
                            "cosecha oscura" -> " Perfecto para campeones que escalan y aseguran asesinatos en peleas largas (ej. Katarina, Khazix)."
                            "fortalecimiento" -> " Excelente para tiradores o luchadores que dependen de ataques básicos rápidos."
                            "compás letal", "cadencia letal" -> " Fundamental en hypercarries como Jinx o Vayne para dominar las peleas largas."
                            "pies veloces" -> " Útil para sobrevivir líneas difíciles gracias a su curación y movilidad al kitear."
                            "conquistador" -> " La mejor opción para luchadores y duelistas que buscan intercambios prolongados (ej. Darius, Riven)."
                            "garras del inmortal" -> " Indispensable en tanques y colosos para tener sustain y escalar vida máxima."
                            "guardián" -> " Selecciona esta runa en soportes protectores (ej. Braum, Lulu) para mitigar burst enemigo."
                            "aery", "invocar a aery" -> " Muy versátil para soportes encantadores o magos de pokeo constante (ej. Karma, Orianna)."
                            "cometa arcano" -> " Ideal para magos de artillería que pokean a distancia (ej. Ziggs, Lux)."
                            "irrupción de fase" -> " Perfecta para magos de combo que necesitan reposicionarse rápido (ej. Orianna, Vladimir)."
                            "primer golpe" -> " Útil en asesinos o magos de ráfaga para escalar en oro rápidamente y explotar objetivos."
                            "soberano gélido" -> " Excelente para soportes de iniciación (ej. Leona, Nautilus) para potenciar su CC."
                            "réplica" -> " Runa perfecta para tanques de iniciación masiva (ej. Amumu, Alistar) que necesitan resistir el focus enemigo post-combo."
                            "triunfo" -> " Ideal en peleas de equipo cerradas. Te recompensa con vida vital tras cada eliminación o asistencia."
                            "fervor de batalla" -> " Útil en intercambios sostenidos cortos, incrementa tu daño para asegurar duelos tempranos."
                            "derribado" -> " Obligatorio si el equipo enemigo tiene muchos tanques y campeones con mucha vida extra."
                            "golpe de gracia" -> " Para asesinos o ADC que buscan asegurar la baja (ejecutar) a enemigos que intenten escapar a baja vida."
                            "leyenda: presteza" -> " Escoge esta runa si priorizas maximizar tu DPS (daño por segundo) a través de ataques básicos rápidos."
                            "leyenda: velocidad", "leyenda: tenacidad" -> " Otorga velocidad de habilidades progresiva (hasta +15) al eliminar súbditos, monstruos o campeones para lanzar habilidades más seguido."
                            "leyenda: linaje" -> " Si tu campeón no armará Robo de Vida temprano pero necesita sustento para sobrevivir y farmear."
                            "último esfuerzo" -> " Excelente en duelistas como Olaf o Tryndamere que se vuelven más letales cuando se acercan a la muerte."
                            else -> " Runa situacional: Úsala para complementar el estilo de juego de tu campeón frente a esta composición específica."
                        }),
                        color = TextMuted,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedRune = null },
                    colors = ButtonDefaults.buttonColors(containerColor = HextechCyan, contentColor = HextechDarkBg),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(tr("Cerrar"), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}


// ====================================================================
// TAB 5: CATÁLOGO EXCLUSIVO DE HECHIZOS DE INVOCADOR
// ====================================================================
