package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import com.example.model.AppUserSecondaryRole
import com.example.data.AvatarCatalog
import com.example.model.AvatarItem
import com.example.ui.theme.*
import com.example.util.SubscriptionManager
import com.example.util.tr

fun getRarityColor(rarity: String): Color {
    return when (rarity.lowercase()) {
        "común", "comun" -> Color(0xFF9E9E9E)
        "raro" -> Color(0xFF3B82F6) // HextechCyan-like
        "épico", "epico" -> Color(0xFFA855F7) // Purple
        "legendario" -> Color(0xFFEF4444) // Red
        "mítico", "mitico" -> Color(0xFFEC4899) // Pink
        else -> HextechGold
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarSelectionBottomSheet(
    onDismiss: () -> Unit,
    onOpenPremiumPlans: () -> Unit
) {
    val context = LocalContext.current
    val isPremium by SubscriptionManager.isPremium.collectAsState()
    val currentAvatarId by SubscriptionManager.currentAvatarId.collectAsState()
    val currentRankBorder by SubscriptionManager.currentRankBorder.collectAsState()
    val activeFramePref by SubscriptionManager.activeFramePreference.collectAsState()
    val currentSecondaryRole by SubscriptionManager.currentSecondaryRole.collectAsState()
    val unlockedAvatars by SubscriptionManager.unlockedAvatars.collectAsState()
    val userRole by SubscriptionManager.userRole.collectAsState()
    val isAdmin = userRole == "admin" || com.example.util.AuthManager.isCurrentUserAdmin()

    val validRegions = remember {
        setOf("Aguas Esturbias", "Ciudad de Bandle", "Demacia", "El Vacío", "Freljord", "Islas de la Sombra", "Jonia", "Ixtal", "Noxus", "Piltóver", "Runaterra", "Shurima", "Targon", "Zaun", "Poro")
    }
    
    val prefs = remember { context.getSharedPreferences("avatar_prefs", android.content.Context.MODE_PRIVATE) }
    var favoriteAvatars by remember { mutableStateOf(prefs.getStringSet("favorites", emptySet())?.toSet() ?: emptySet()) }

    val filterOptions = remember(favoriteAvatars) {
        val baseOptions = if (favoriteAvatars.isNotEmpty()) listOf("Todas", "Favoritos") else listOf("Todas")
        baseOptions + AvatarCatalog.avatars.map { it.region }.filter { validRegions.contains(it) }.distinct().sorted()
    }
    var selectedFilter by remember { mutableStateOf("Todas") }
    
    LaunchedEffect(favoriteAvatars) {
        if (favoriteAvatars.isEmpty() && selectedFilter == "Favoritos") {
            selectedFilter = "Todas"
        }
    }
    
    var selectedCategory by remember { mutableStateOf("Avatares") }
    var showPremiumRequiredDialog by remember { mutableStateOf<AvatarItem?>(null) }
    var isUpdating by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val groupedAvatars = remember(selectedFilter, searchQuery, favoriteAvatars) {
        AvatarCatalog.avatars.filter {
            val matchesRegion = if (selectedFilter == "Favoritos") {
                favoriteAvatars.contains(it.id)
            } else {
                selectedFilter == "Todas" || it.region.equals(selectedFilter, ignoreCase = true)
            }
            val matchesSearch = searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
            matchesRegion && matchesSearch
        }.groupBy { if (selectedFilter == "Favoritos") "Favoritos" else it.region }.toSortedMap()
    }

    // Modal Bottom Sheet / Full Screen Dialog
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = HextechDarkBg,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 40.dp, height = 4.dp),
                shape = CircleShape,
                color = HextechGold.copy(alpha = 0.5f)
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 16.dp)
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
                            .clip(CircleShape)
                            .background(HextechGold.copy(alpha = 0.15f))
                            .border(1.dp, HextechGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Diamond,
                            contentDescription = null,
                            tint = HextechGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = tr("Avatares"),
                            color = HextechGoldLight,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isPremium) tr("Acceso Total Premium Desbloqueado") else tr("Avatares Exclusivos de League of Legends"),
                            color = if (isPremium) HextechCyan else TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Current Equipped Avatar Banner
            val currentAvatar = AvatarCatalog.getAvatarById(currentAvatarId)
            val currentRarityColorTop = getRarityColor(currentAvatar.rarity)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, currentRarityColorTop.copy(alpha = 0.7f), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HextechSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(68.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        UserAvatarView(
                            avatarId = currentAvatarId,
                            rankBorder = currentRankBorder,
                            secondaryRole = currentSecondaryRole,
                            equippedFrame = activeFramePref,
                            size = 68.dp,
                            isAdmin = isAdmin,
                            fitFrameToSize = true
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tr("Avatar Actual:"),
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val currentRarityColor = getRarityColor(currentAvatar.rarity)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(currentRarityColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = currentAvatar.rarity.uppercase(),
                                    color = currentRarityColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                        Text(
                            text = currentAvatar.name,
                            color = HextechGoldLight,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentAvatar.title} • ${currentAvatar.region}",
                            color = HextechCyan,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Selector de Categoría (Avatares vs Marcos de Perfil)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(HextechSurface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedCategory == "Avatares") HextechGold else Color.Transparent)
                        .clickable { selectedCategory = "Avatares" },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Default.Diamond,
                            contentDescription = null,
                            tint = if (selectedCategory == "Avatares") HextechDarkBg else TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = tr("Avatares"),
                            color = if (selectedCategory == "Avatares") HextechDarkBg else TextSecondary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedCategory == "Marcos") HextechGold else Color.Transparent)
                        .clickable { selectedCategory = "Marcos" },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = if (selectedCategory == "Marcos") HextechDarkBg else TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = tr("Marcos de Perfil"),
                            color = if (selectedCategory == "Marcos") HextechDarkBg else TextSecondary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedCategory == "Avatares") {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                placeholder = { Text(tr("Buscar avatar..."), color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HextechGold,
                    unfocusedBorderColor = HextechCardBorder,
                    focusedContainerColor = HextechDarkBg,
                    unfocusedContainerColor = HextechSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextSecondary
                )
            )

            // Filter Chips Carousel
            ScrollableTabRow(
                selectedTabIndex = filterOptions.indexOf(selectedFilter).coerceAtLeast(0),
                containerColor = Color.Transparent,
                contentColor = HextechCyan,
                edgePadding = 0.dp,
                divider = {}
            ) {
                filterOptions.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Tab(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        text = {
                            Text(
                                text = tr(filter),
                                color = if (isSelected) HextechCyan else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.5.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Non-premium info banner
            if (!isPremium && userRole != "admin") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(HextechGold.copy(alpha = 0.1f))
                        .border(1.dp, HextechGold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = HextechGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tr("El cambio de avatares requiere membresía Premium."),
                                color = TextPrimary,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        HextechAnimatedTextLink(
                            text = tr("Ver Planes"),
                            onClick = onOpenPremiumPlans,
                            color = HextechGoldLight,
                            fontSize = 11.5.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Grid of Avatars
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 16.dp)
            ) {
                groupedAvatars.forEach { (region, avatarsInRegion) ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = region.uppercase(),
                            color = HextechGoldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 4.dp)
                        )
                    }
                    items(avatarsInRegion, key = { it.id }) { avatar ->
                    val isEquipped = currentAvatarId.equals(avatar.id, ignoreCase = true)
                    val isGifted = unlockedAvatars.contains(avatar.id)
                    val canEquip = isPremium || userRole == "admin" || avatar.isDefault || isGifted || avatar.rarity.equals("común", true) || avatar.rarity.equals("comun", true)

                    val rarityColor = getRarityColor(avatar.rarity)
                    val parsedBorder = rarityColor

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = canEquip || isEquipped) {
                                if (isEquipped) {
                                    Toast.makeText(context, "Este avatar ya está equipado.", Toast.LENGTH_SHORT).show()
                                } else {
                                    isUpdating = true
                                    SubscriptionManager.changeAvatar(
                                        avatarId = avatar.id,
                                        onSuccess = {
                                            isUpdating = false
                                            Toast.makeText(context, "¡Avatar actualizado con éxito!", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { err ->
                                            isUpdating = false
                                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            }
                            .border(
                                width = when {
                                    isEquipped -> 3.dp
                                    avatar.rarity.lowercase().contains("mítico") || avatar.rarity.lowercase().contains("mitico") -> 2.5.dp
                                    avatar.rarity.lowercase().contains("legendario") -> 2.dp
                                    avatar.rarity.lowercase().contains("épico") || avatar.rarity.lowercase().contains("epico") -> 1.8.dp
                                    avatar.rarity.lowercase().contains("raro") -> 1.5.dp
                                    else -> 1.dp
                                },
                                brush = getRarityBorderBrush(avatar.rarity),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isEquipped) rarityColor.copy(alpha = 0.15f) else if (!canEquip) HextechSurface.copy(alpha = 0.5f) else HextechSurface
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    contentAlignment = Alignment.BottomEnd
                                ) {
                                    UserAvatarView(
                                        avatarId = avatar.id,
                                        size = 54.dp,
                                        customBorderColor = rarityColor
                                    )
    
                                    if (isEquipped) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(HextechGold)
                                                .border(1.5.dp, HextechDarkBg, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Equipado",
                                                tint = HextechDarkBg,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    } else if (!canEquip) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF0F172A).copy(alpha = 0.95f))
                                                .border(1.5.dp, HextechGold, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Lock,
                                                contentDescription = "Bloqueado",
                                                tint = HextechGold,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
    
                                Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = avatar.name,
                                color = if (isEquipped) HextechGoldLight else if (canEquip) TextPrimary else TextMuted,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = avatar.region,
                                color = if (canEquip) HextechCyan else TextMuted.copy(alpha = 0.7f),
                                fontSize = 9.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Status Tag
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (isEquipped) HextechGold.copy(alpha = 0.25f)
                                        else if (isGifted && !avatar.isDefault) Color(0xFF10B981).copy(alpha = 0.2f)
                                        else if (canEquip) HextechCyan.copy(alpha = 0.15f)
                                        else Color.Black.copy(alpha = 0.4f)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                if (isEquipped) {
                                    Text(
                                        text = tr("ACTIVO"),
                                        color = HextechGold,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                } else if (isGifted && !avatar.isDefault) {
                                    Text(
                                        text = tr("REGALO"),
                                        color = Color(0xFF10B981),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                } else if (canEquip) {
                                    Text(
                                        text = tr("LISTO"),
                                        color = HextechCyan,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = HextechGold.copy(alpha = 0.8f),
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = tr("PREMIUM"),
                                            color = HextechGold.copy(alpha = 0.8f),
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Botón de Favorito
                        IconButton(
                            onClick = {
                                val newFavorites = favoriteAvatars.toMutableSet()
                                if (newFavorites.contains(avatar.id)) {
                                    newFavorites.remove(avatar.id)
                                } else {
                                    newFavorites.add(avatar.id)
                                }
                                favoriteAvatars = newFavorites
                                prefs.edit().putStringSet("favorites", newFavorites).apply()
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(28.dp)
                        ) {
                            val isFav = favoriteAvatars.contains(avatar.id)
                            Icon(
                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (isFav) Color(0xFFE11D48) else TextMuted.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                } // End of items
            } // End of forEach
            } // End of LazyVerticalGrid
        } else {
            // SECCIÓN DE MARCOS DE PERFIL Y DE RANGO
            val activeFramePrefState by SubscriptionManager.activeFramePreference.collectAsState()
            val currentSecondaryRoleState by SubscriptionManager.currentSecondaryRole.collectAsState()
            val secRoleObj = remember(currentSecondaryRoleState) {
                if (currentSecondaryRoleState.isNotBlank() && currentSecondaryRoleState != "none") {
                    AppUserSecondaryRole.fromId(currentSecondaryRoleState)
                } else AppUserSecondaryRole.NONE
            }
            val hasSecRoleFrame = secRoleObj != AppUserSecondaryRole.NONE && secRoleObj.frameDrawableRes != null
            val hasSpecialFrame = isAdmin || (currentRankBorder != "NONE" && currentRankBorder.isNotBlank())

            val msgNoSecRole = tr("No posees un marco de rol secundario asignado")
            val msgNoSpecialFrame = tr("No posees un marco especial de rango disponible")
            val msgFrameSuccess = tr("Marco de perfil actualizado correctamente")

            var selectedFrameOption by remember(activeFramePrefState) { mutableStateOf(activeFramePrefState.uppercase()) }
            var isSavingFrame by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 12.dp)
            ) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = tr("PREFERENCIA DE MARCO VISUAL"),
                        color = HextechGoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FrameOptionCardInAvatar(
                            title = tr("Sin Marco (Desactivado)"),
                            subtitle = tr("Oculta el marco exterior y muestra solo el avatar."),
                            icon = Icons.Default.VisibilityOff,
                            iconTint = TextSecondary,
                            isSelected = selectedFrameOption == "NONE",
                            isEnabled = true,
                            onClick = { selectedFrameOption = "NONE" }
                        )

                        FrameOptionCardInAvatar(
                            title = tr("Automático (Recomendado)"),
                            subtitle = tr("Muestra el marco disponible de mayor jerarquía."),
                            icon = Icons.Default.Star,
                            iconTint = HextechGold,
                            isSelected = selectedFrameOption == "AUTO",
                            isEnabled = true,
                            onClick = { selectedFrameOption = "AUTO" }
                        )

                        val secRoleTitle = if (hasSecRoleFrame) "Marco de ${secRoleObj.displayName}" else tr("Marco de Rol Secundario")
                        val secRoleSubtitle = if (hasSecRoleFrame) tr("Marco exclusivo otorgado por Moderación/Administración.") else tr("Requiere un rol secundario asignado.")
                        FrameOptionCardInAvatar(
                            title = secRoleTitle,
                            subtitle = secRoleSubtitle,
                            icon = Icons.Default.Shield,
                            iconTint = if (hasSecRoleFrame) secRoleObj.primaryColor else TextSecondary.copy(alpha = 0.5f),
                            isSelected = selectedFrameOption == "SECONDARY",
                            isEnabled = hasSecRoleFrame,
                            onClick = {
                                if (hasSecRoleFrame) {
                                    selectedFrameOption = "SECONDARY"
                                } else {
                                    Toast.makeText(context, msgNoSecRole, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        val specialTitle = if (isAdmin) tr("Marco de Administrador") else if (currentRankBorder != "NONE") "Marco de Rango $currentRankBorder" else tr("Marco Especial / Rango")
                        val specialSubtitle = if (hasSpecialFrame) tr("Marco de estatus especial o rango competitivo.") else tr("No posees un marco especial de rango disponible.")
                        FrameOptionCardInAvatar(
                            title = specialTitle,
                            subtitle = specialSubtitle,
                            icon = Icons.Default.Star,
                            iconTint = if (hasSpecialFrame) HextechGold else TextSecondary.copy(alpha = 0.5f),
                            isSelected = selectedFrameOption == "SPECIAL" || selectedFrameOption == "RANK",
                            isEnabled = hasSpecialFrame,
                            onClick = {
                                if (hasSpecialFrame) {
                                    selectedFrameOption = "SPECIAL"
                                } else {
                                    Toast.makeText(context, msgNoSpecialFrame, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            isSavingFrame = true
                            SubscriptionManager.changeActiveFramePreference(
                                preference = selectedFrameOption,
                                onSuccess = {
                                    isSavingFrame = false
                                    Toast.makeText(context, msgFrameSuccess, Toast.LENGTH_SHORT).show()
                                },
                                onError = { err ->
                                    isSavingFrame = false
                                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        enabled = !isSavingFrame && selectedFrameOption != activeFramePrefState.uppercase(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HextechGold,
                            contentColor = HextechDarkBg
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isSavingFrame) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = HextechDarkBg,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (selectedFrameOption == activeFramePrefState.uppercase()) tr("Marco Preferido Actual") else tr("Guardar Preferencia de Marco"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
        } // End of Column
    } // End of ModalBottomSheet

    // Modal when user tries to equip a locked avatar
    if (showPremiumRequiredDialog != null) {
        val lockedAvatar = showPremiumRequiredDialog!!
        AlertDialog(
            onDismissRequest = { showPremiumRequiredDialog = null },
            containerColor = HextechDarkBg,
            shape = RoundedCornerShape(16.dp),
            icon = {
                UserAvatarView(
                    avatarId = lockedAvatar.id,
                    size = 64.dp
                )
            },
            title = {
                Text(
                    text = tr("Avatar Exclusivo Premium"),
                    color = HextechGoldLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = lockedAvatar.name,
                        color = HextechGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${lockedAvatar.title} • ${lockedAvatar.region}",
                        color = HextechCyan,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = tr("Para equipar este avatar legendario de League of Legends necesitas una membresía Premium activa."),
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                HextechAnimatedButton(
                    onClick = {
                        showPremiumRequiredDialog = null
                        onDismiss()
                        onOpenPremiumPlans()
                    },
                    backgroundColor = HextechGold,
                    borderColor = HextechCyan,
                    glowColor = HextechGold,
                    enableShimmer = true,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Diamond, contentDescription = null, tint = HextechDarkBg, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(tr("Desbloquear con Premium"), color = HextechDarkBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                HextechAnimatedTextLink(
                    text = tr("Cerrar"),
                    onClick = { showPremiumRequiredDialog = null },
                    color = TextSecondary
                )
            }
        )
    }
}


fun getRarityBorderBrush(rarity: String): Brush {
    val rarityLower = rarity.lowercase()
    val isCommon = rarityLower == "común" || rarityLower == "comun" || rarityLower == "clásico"
    return when {
        rarityLower.contains("mítico") || rarityLower.contains("mitico") -> Brush.sweepGradient(listOf(Color(0xFFC4B5FD), Color(0xFF7C3AED), Color(0xFF5B21B6), Color(0xFFC4B5FD)))
        rarityLower.contains("legendario") -> Brush.sweepGradient(listOf(Color(0xFFFFD700), Color(0xFFB91C1C), Color(0xFF991B1B), Color(0xFFFFD700)))
        rarityLower.contains("épico") || rarityLower.contains("epico") -> Brush.sweepGradient(listOf(Color(0xFFE9D5FF), Color(0xFF9333EA), Color(0xFFE9D5FF)))
        rarityLower.contains("raro") -> Brush.linearGradient(listOf(Color(0xFF93C5FD), Color(0xFF2563EB), Color(0xFF93C5FD)))
        else -> Brush.linearGradient(listOf(HextechCardBorder, HextechCardBorder))
    }
}

@Composable
private fun FrameOptionCardInAvatar(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled, onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) HextechGold.copy(alpha = 0.15f) else HextechSurface.copy(alpha = if (isEnabled) 0.65f else 0.3f)
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) HextechGold else HextechCardBorder.copy(alpha = if (isEnabled) 1f else 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isEnabled) iconTint else TextSecondary.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = title,
                        color = if (isEnabled) TextPrimary else TextSecondary.copy(alpha = 0.5f),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        color = if (isEnabled) TextSecondary else TextSecondary.copy(alpha = 0.4f),
                        fontSize = 10.5.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) HextechGold else TextSecondary.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
