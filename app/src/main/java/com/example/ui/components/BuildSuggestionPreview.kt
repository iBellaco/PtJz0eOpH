package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.ui.components.coachClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.WildRiftItem
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.HextechSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.util.tr
import androidx.compose.foundation.shape.CircleShape

@Composable
internal fun GraphicalBuildSuggestionView(
    build: ParsedBuildSuggestion,
    onItemClick: (WildRiftItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(HextechDarkBg.copy(alpha = 0.85f))
            .border(1.dp, HextechGold.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ENCABEZADO: Campeón + Rol + Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .border(2.dp, HextechGold, CircleShape)
                    .background(HextechSurface),
                contentAlignment = Alignment.Center
            ) {
                if (build.championObj != null) {
                    ChampionAvatar(
                        champion = build.championObj,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (!build.championAvatar.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(build.championAvatar)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)

                            .build(),
                        contentDescription = build.championName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(HextechGold.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = com.example.util.tr(build.championName.take(2).uppercase()),
                            color = HextechGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = com.example.util.tr(build.championName),
                        color = HextechGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (build.championObj != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(HextechCyan.copy(alpha = 0.2f))
                                .border(0.8.dp, HextechCyan.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = com.example.util.tr("Tier ${build.championObj.tier}"),
                                color = HextechCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                if (!build.role.isNullOrBlank()) {
                    Text(
                        text = com.example.util.tr("Rol / Línea: ${build.role}"),
                        color = HextechCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Hechizos y Runa Clave en el header si están disponibles
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (spell in build.spells) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.dp, HextechCyan.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                            .background(Color.Black)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(spell.iconUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)

                                .build(),
                            contentDescription = spell.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                build.keystoneRune?.let { rune ->
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .border(1.dp, HextechGold, CircleShape)
                            .background(Color.Black)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(rune.iconUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)

                                .build(),
                            contentDescription = rune.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = HextechCardBorder.copy(alpha = 0.6f), thickness = 0.8.dp)

        // SECCIÓN 1: OBJETOS CORE (Slots 1 al 5) Y BOTAS (Slot 6)
        if (build.coreItems.isNotEmpty() || build.boots != null) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr("OBJETOS CORE (1 al 5) & OBJETO 6 (BOTAS):"),
                        color = HextechGold,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val core5 = build.coreItems.take(5)
                    items(core5) { item ->
                        BuildItemSlot(
                            item = item,
                            badgeText = "${core5.indexOf(item) + 1}",
                            onClick = { onItemClick(item) }
                        )
                    }
                    build.boots?.let { boot ->
                        item {
                            BuildItemSlot(
                                item = boot,
                                badgeText = "6 ",
                                badgeColor = HextechGold,
                                onClick = { onItemClick(boot) }
                            )
                        }
                    }
                }
            }
        }

        // SECCIÓN 2: OBJETOS SITUACIONALES (Slots 7 y 8)
        if (build.situationalItems.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = tr("OBJETOS SITUACIONALES (7 y 8):"),
                        color = HextechCyan,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tr("(Adaptación estándar)"),
                        color = TextMuted,
                        fontSize = 9.5.sp
                    )
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(build.situationalItems) { item ->
                        val slotNum = 7 + build.situationalItems.indexOf(item)
                        BuildItemSlot(
                            item = item,
                            badgeText = "$slotNum",
                            badgeColor = HextechCyan,
                            onClick = { onItemClick(item) }
                        )
                    }
                }
            }
        }

        // SECCIÓN 3: ALTERNATIVAS SITUACIONALES (Vs Composición Rival)
        if (build.altSituationalItems.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = tr("ALTERNATIVAS SITUACIONALES:"),
                        color = Color(0xFFFFB74D),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tr("(Vs composición enemiga)"),
                        color = TextMuted,
                        fontSize = 9.5.sp
                    )
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(build.altSituationalItems) { item ->
                        BuildItemSlot(
                            item = item,
                            badgeText = "Alt",
                            badgeColor = Color(0xFFFFB74D),
                            onClick = { onItemClick(item) }
                        )
                    }
                }
            }
        }

        // SECCIÓN 4: BOTAS DETALLE + RUNAS SECUNDARIAS + HECHIZOS
        if (build.boots != null || build.secondaryRunes.isNotEmpty() || build.spells.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(HextechSurfaceVariant.copy(alpha = 0.5f))
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botas
                build.boots?.let { boot ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        BuildItemSlot(
                            item = boot,
                            badgeText = "",
                            size = 32.dp,
                            onClick = { onItemClick(boot) }
                        )
                        Column {
                            Text(
                                text = tr("Botas (Slot 6)"),
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                            Text(
                                text = com.example.util.tr(boot.name),
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Runas Secundarias
                if (build.secondaryRunes.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (rune in build.secondaryRunes) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .border(0.8.dp, HextechGold.copy(alpha = 0.6f), CircleShape)
                                    .background(Color.Black)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(rune.iconUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)

                                        .build(),
                                    contentDescription = rune.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }

        // SECCIÓN 5: JUSTIFICACIÓN TÁCTICA / MATCHUPS
        if (!build.tacticalNotes.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(HextechSurfaceVariant.copy(alpha = 0.6f))
                    .border(0.8.dp, HextechGold.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = HextechGold, modifier = Modifier.size(12.dp))
                        Text(
                            text = tr("Justificación Táctica / Matchups:"),
                            color = HextechGold,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = com.example.util.tr(build.tacticalNotes),
                        color = TextPrimary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun BuildItemSlot(
    item: WildRiftItem,
    badgeText: String,
    badgeColor: Color = HextechGold,
    size: Dp = 38.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
            .background(HextechSurface)
            .border(1.dp, HextechCardBorder, RoundedCornerShape(6.dp))
            .coachClickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.iconUrl)
.crossfade(true)
.placeholder(com.example.R.drawable.ic_placeholder_loading)

                .build(),
            contentDescription = item.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Badge de posición o tipo en esquina
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(topStart = 4.dp))
                .padding(horizontal = 3.dp, vertical = 1.dp)
        ) {
            Text(
                text = com.example.util.tr(badgeText),
                color = badgeColor,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
