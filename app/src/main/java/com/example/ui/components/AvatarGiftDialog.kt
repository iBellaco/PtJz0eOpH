package com.example.ui.components

import com.example.ui.components.CoachFilterChip as FilterChip
import com.example.ui.components.CoachButton as Button
import com.example.ui.components.CoachIconButton as IconButton
import com.example.util.tr
import androidx.compose.foundation.BorderStroke
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AvatarCatalog
import com.example.model.AvatarItem
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAvatarGiftDialog(
    user: Map<String, Any>,
    onDismiss: () -> Unit,
    onAvatarGifted: (String) -> Unit
) {
    val context = LocalContext.current
    val uid = user["uid"] as? String ?: ""
    val userName = user["name"] as? String ?: "Usuario"
    val unlockedAvatars = remember(user) {
        ((user["unlockedAvatars"] as? List<*>)?.mapNotNull { it?.toString() } ?: listOf("default_poro")).toSet()
    }
    var currentUnlocked by remember { mutableStateOf(unlockedAvatars) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedRarity by remember { mutableStateOf("Todas") }

    // Excluir avatares comunes y clásicos del panel de regalar porque ya están desbloqueados para todos
    val allAvatars = remember {
        AvatarCatalog.avatars.filter { item ->
            val r = item.rarity.lowercase()
            !r.contains("común") && !r.contains("comun") && !r.contains("clásico") && !r.contains("clasico") && !item.isDefault
        }
    }
    val rarities = listOf("Todas", "Raro", "Épico", "Legendario", "Mítico")

    val filteredAvatars = remember(searchQuery, selectedRarity, allAvatars) {
        allAvatars.filter { item ->
            val matchesQuery = searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true) || item.title.contains(searchQuery, ignoreCase = true)
            val matchesRarity = selectedRarity == "Todas" || item.rarity.equals(selectedRarity, ignoreCase = true)
            matchesQuery && matchesRarity
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f),
            shape = RoundedCornerShape(16.dp),
            color = HextechDarkBg,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, HextechGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.linearGradient(listOf(HextechGold, Color(0xFF8B6B23)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = HextechDarkBg, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = com.example.util.tr("Regalar Avatar a $userName"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = HextechGold
                            )
                            val unlockedExclusiveCount = currentUnlocked.count { id -> allAvatars.any { it.id == id } }
                            Text(
                                text = com.example.util.tr("$unlockedExclusiveCount de ${allAvatars.size} exclusivos desbloqueados"),
                                style = MaterialTheme.typography.bodySmall,
                                color = HextechCyan,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Cerrar"), tint = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Buscador y Filtros
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(tr("Buscar avatar o campeón..."), color = TextMuted, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HextechGold, modifier = Modifier.size(16.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = TextMuted)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HextechGold,
                        unfocusedBorderColor = HextechCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filtros de Rareza
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rarities.forEach { r ->
                        val isSel = selectedRarity == r
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedRarity = r },
                            label = { Text(com.example.util.tr(r), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HextechGold.copy(alpha = 0.25f),
                                selectedLabelColor = HextechGold,
                                containerColor = HextechSurfaceBg,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botones Regalar / Quitar Todo el Catálogo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            giftAllAvatarsToUser(context, uid) {
                                currentUnlocked = allAvatars.map { it.id }.toSet()
                                Toast.makeText(context, com.example.util.appTr("¡Todos los avatares han sido regalados!"), Toast.LENGTH_SHORT).show()
                                onAvatarGifted("all")
                            }
                        },
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(com.example.util.tr("🎁 Regalar Todos (${allAvatars.size})"), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                    }

                    Button(
                        onClick = {
                            revokeAllExclusiveAvatarsFromUser(context, uid) {
                                currentUnlocked = setOf("default_poro")
                                Toast.makeText(context, com.example.util.appTr("¡Regalos de avatares retirados!"), Toast.LENGTH_SHORT).show()
                                onAvatarGifted("none")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.85f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(tr("Quitar Regalos"), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cuadrícula de Avatares
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredAvatars, key = { it.id }) { item ->
                        val isAlreadyUnlocked = currentUnlocked.contains(item.id)

                        AvatarGiftCard(
                            item = item,
                            isUnlocked = isAlreadyUnlocked,
                            onGift = {
                                giftSingleAvatarToUser(context, uid, item.id) {
                                    currentUnlocked = currentUnlocked + item.id
                                    Toast.makeText(context, com.example.util.appTr("¡Avatar ${item.name} regalado!"), Toast.LENGTH_SHORT).show()
                                    onAvatarGifted(item.id)
                                }
                            },
                            onRevoke = {
                                val currentEquipped = user["avatarId"] as? String
                                revokeSingleAvatarFromUser(context, uid, item.id, currentEquipped) {
                                    currentUnlocked = currentUnlocked - item.id
                                    Toast.makeText(context, com.example.util.appTr("¡Avatar ${item.name} retirado!"), Toast.LENGTH_SHORT).show()
                                    onAvatarGifted(item.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarGiftCard(
    item: AvatarItem,
    isUnlocked: Boolean,
    onGift: () -> Unit,
    onRevoke: () -> Unit = {}
) {
    Surface(
        color = HextechSurfaceBg,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isUnlocked) Color(0xFF00FF7F).copy(alpha = 0.5f) else HextechCardBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UserAvatarView(
                avatarId = item.id,
                size = 52.dp,
                fallbackInitial = item.name.take(1)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = com.example.util.tr(item.name),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = com.example.util.tr(item.rarity),
                fontSize = 9.sp,
                color = HextechGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (isUnlocked) {
                Surface(
                    color = Color(0xFF00FF7F).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = tr("✓ Desbloqueado"),
                        color = Color(0xFF00FF7F),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onRevoke,
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.RemoveCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(tr("Quitar"), color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onGift,
                    colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(tr("Regalar"), color = HextechDarkBg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
