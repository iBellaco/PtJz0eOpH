package com.example.ui.components

import com.example.util.tr

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.WildRiftRepository
import com.example.data.local.CustomChampionBuildRecord
import com.example.data.local.CustomChampionBuildsManager
import com.example.ui.theme.*
import androidx.compose.ui.window.Dialog

import android.content.Intent
import android.net.Uri
import android.widget.VideoView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WorkspacePremium
import com.example.data.local.AppDatabase
import com.example.data.local.entity.FavoriteBuildEntity
import com.example.util.AuthManager
import com.example.util.CreatorSubscriptionManager
import com.example.util.SubscriptionManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextOverflow
import java.util.Locale

@Composable
fun CustomBuildDetailDialog(
    record: CustomChampionBuildRecord,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val champ = remember(record.championId) {
        WildRiftRepository.champions.find { it.id.equals(record.championId, ignoreCase = true) }
    }

    val userVotedMap by CustomChampionBuildsManager.userVotedBuilds.collectAsStateWithLifecycle()
    val hasVoted = userVotedMap.containsKey(record.id)
    val userRating = userVotedMap[record.id] ?: 0

    val avgRating = if (record.voteCount > 0) record.ratingSum / record.voteCount else 0.0

    val scope = rememberCoroutineScope()
    val favoriteDao = remember { AppDatabase.getDatabase(context).favoriteBuildsDao() }
    val isFavorite by favoriteDao.isFavorite(record.id).collectAsState(initial = false)

    val subscribedSet by CreatorSubscriptionManager.subscribedCreatorKeys.collectAsStateWithLifecycle()
    val userRole by SubscriptionManager.userRole.collectAsStateWithLifecycle()
    val currentBlueEssence by SubscriptionManager.blueEssence.collectAsStateWithLifecycle()

    val currentAuthUser = remember { FirebaseAuth.getInstance().currentUser }
    val currentUserId = currentAuthUser?.uid ?: ""
    val currentUserName = (currentAuthUser?.displayName ?: "").trim()

    val creatorUidClean = record.creatorUserId.trim().lowercase(Locale.ROOT)
    val creatorNameClean = record.creatorName.trim().lowercase(Locale.ROOT)

    val isSystemBuild = record.creatorIsAdmin ||
        creatorNameClean.contains("coach system") ||
        creatorNameClean == "system" ||
        record.creatorName.isBlank()

    val isOwnBuild = (currentUserId.isNotBlank() && currentUserId.lowercase(Locale.ROOT) == creatorUidClean) ||
        (currentUserName.isNotBlank() && currentUserName.lowercase(Locale.ROOT) == creatorNameClean)

    val isAdmin = userRole == "admin" || AuthManager.isCurrentUserAdmin()

    val isSubscribedToCreator = remember(subscribedSet, creatorUidClean, creatorNameClean) {
        (creatorUidClean.isNotBlank() && subscribedSet.contains(creatorUidClean)) ||
        (creatorNameClean.isNotBlank() && subscribedSet.contains(creatorNameClean))
    }

    val canViewBuild = isSystemBuild || isOwnBuild || isAdmin || isSubscribedToCreator

    var showSubscribeConfirm by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler { onDismiss() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = HextechDarkBg
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (champ != null) {
                        ChampionAvatar(champion = champ, size = 48.dp, showTierBadge = false)
                    }
                    Column {
                        Text(
                            text = com.example.util.tr(record.buildTitle),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = com.example.util.tr("${record.championName} • Rol: ${record.role} • Creador: ${record.creatorName}"),
                            color = HextechGold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        scope.launch {
                            if (isFavorite) {
                                favoriteDao.deleteFavorite(record.id)
                            } else {
                                favoriteDao.insertFavorite(FavoriteBuildEntity(buildId = record.id))
                            }
                        }
                    }) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = com.example.util.trNullable("Favorito"),
                            tint = if (isFavorite) DangerRed else HextechGold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = com.example.util.trNullable("Cerrar"), tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rating summary badge
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = HextechSurface),
                border = BorderStroke(1.dp, HextechCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = HextechGold, modifier = Modifier.size(20.dp))
                        Text(
                            text = com.example.util.tr(String.format("%.1f", avgRating)),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = com.example.util.tr("(${record.voteCount} votos)"),
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = com.example.util.tr("Publicado por ${record.creatorName}"),
                        color = HextechCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!canViewBuild) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                    border = BorderStroke(1.5.dp, HextechGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(HextechGold.copy(alpha = 0.15f))
                                .border(1.5.dp, HextechGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = com.example.util.trNullable("Protegida"),
                                tint = HextechGold,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = com.example.util.tr("Build Protegida para Suscriptores"),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Text(
                            text = com.example.util.tr("Esta build fue creada por ${record.creatorName}. Debes estar suscrito a este creador para desbloquear y ver sus objetos, runas y estrategias completas."),
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HextechDarkBg,
                            border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = com.example.util.tr("Tu saldo:"),
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = com.example.util.tr("$currentBlueEssence EA"),
                                    color = HextechCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .weight(0.35f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color.Gray)
                            ) {
                                Text(tr("Cancelar"), color = Color.LightGray, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Button(
                                onClick = {
                                    showSubscribeConfirm = true
                                },
                                modifier = Modifier
                                    .weight(0.65f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HextechGold,
                                    contentColor = Color.Black
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    androidx.compose.foundation.Image(
                                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_orange_essence),
                                        contentDescription = "Esencia Naranja",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = com.example.util.tr(tr("Suscribirse") + " (${CreatorSubscriptionManager.SUBSCRIPTION_EN_COST} EN)"),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Core Items
                if (record.coreItemsWithDesc.isNotEmpty()) {
                    item {
                        Text(tr("Objetos Core"), color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            record.coreItemsWithDesc.forEach { item ->
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                                    border = BorderStroke(1.dp, HextechCardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val iconUrl = com.example.data.WildRiftItemsData.getItemIconByName(item.itemName)
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(HextechSurfaceVariant)
                                                .border(1.dp, HextechGold, RoundedCornerShape(6.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AppAssetImage(
                                                url = iconUrl,
                                                contentDescription = item.itemName,
                                                fallbackText = item.itemName,
                                                modifier = Modifier.size(28.dp),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                        }
                                        Column {
                                            Text(com.example.util.tr(item.itemName), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(com.example.util.tr(item.description), color = TextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Botas Core (Nivel 2 + Mejora Nivel 3)
                if (record.bootsT2Item != null || record.bootsT3Item != null) {
                    item {
                        Text(tr("Botas y Mejoras (Obligatorias)"), color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            record.bootsT2Item?.let { boot ->
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                                    border = BorderStroke(1.dp, HextechGold),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val iconUrl = com.example.data.WildRiftItemsData.getItemIconByName(boot.itemName)
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(HextechSurfaceVariant)
                                                .border(1.dp, HextechGold, RoundedCornerShape(6.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AppAssetImage(
                                                url = iconUrl,
                                                contentDescription = boot.itemName,
                                                fallbackText = boot.itemName,
                                                modifier = Modifier.size(28.dp),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                        }
                                        Column {
                                            Text(com.example.util.tr("[Botas N2] ${boot.itemName}"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            if (boot.description.isNotBlank()) Text(com.example.util.tr(boot.description), color = TextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                            record.bootsT3Item?.let { enchant ->
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                                    border = BorderStroke(1.dp, HextechCyan),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val iconUrl = com.example.data.WildRiftItemsData.getItemIconByName(enchant.itemName)
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(HextechSurfaceVariant)
                                                .border(1.dp, HextechCyan, RoundedCornerShape(6.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AppAssetImage(
                                                url = iconUrl,
                                                contentDescription = enchant.itemName,
                                                fallbackText = enchant.itemName,
                                                modifier = Modifier.size(28.dp),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                        }
                                        Column {
                                            Text(com.example.util.tr("[Mejora N3] ${enchant.itemName}"), color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            if (enchant.description.isNotBlank()) Text(com.example.util.tr(enchant.description), color = TextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Botas Situacionales (Opcional)
                if (record.situationalBootsT2Item != null || record.situationalBootsT3Item != null) {
                    item {
                        Text(tr("Botas y Mejoras Situacionales (Opcional)"), color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            record.situationalBootsT2Item?.let { boot ->
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                                    border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val iconUrl = com.example.data.WildRiftItemsData.getItemIconByName(boot.itemName)
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(HextechSurfaceVariant)
                                                .border(1.dp, HextechCyan, RoundedCornerShape(6.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AppAssetImage(
                                                url = iconUrl,
                                                contentDescription = boot.itemName,
                                                fallbackText = boot.itemName,
                                                modifier = Modifier.size(28.dp),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                        }
                                        Column {
                                            Text(com.example.util.tr("[Bota N2 Situacional] ${boot.itemName}"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            if (boot.description.isNotBlank()) Text(com.example.util.tr(boot.description), color = TextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                            record.situationalBootsT3Item?.let { enchant ->
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                                    border = BorderStroke(1.dp, HextechCyan.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val iconUrl = com.example.data.WildRiftItemsData.getItemIconByName(enchant.itemName)
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(HextechSurfaceVariant)
                                                .border(1.dp, HextechCyan, RoundedCornerShape(6.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AppAssetImage(
                                                url = iconUrl,
                                                contentDescription = enchant.itemName,
                                                fallbackText = enchant.itemName,
                                                modifier = Modifier.size(28.dp),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                        }
                                        Column {
                                            Text(com.example.util.tr("[Mejora N3 Situacional] ${enchant.itemName}"), color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            if (enchant.description.isNotBlank()) Text(com.example.util.tr(enchant.description), color = TextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Situational Items
                if (record.situationalItemsWithDesc.isNotEmpty()) {
                    item {
                        Text(tr("Objetos Situacionales"), color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            record.situationalItemsWithDesc.forEach { item ->
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                                    border = BorderStroke(1.dp, HextechCardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val iconUrl = com.example.data.WildRiftItemsData.getItemIconByName(item.itemName)
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(HextechSurfaceVariant)
                                                .border(1.dp, HextechCyan, RoundedCornerShape(6.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AppAssetImage(
                                                url = iconUrl,
                                                contentDescription = item.itemName,
                                                fallbackText = item.itemName,
                                                modifier = Modifier.size(28.dp),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                        }
                                        Column {
                                            Text(com.example.util.tr(item.itemName), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(com.example.util.tr(item.description), color = TextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Runes
                if (record.coreRunes.isNotEmpty()) {
                    item {
                        Text(tr("Runas (1 Clave + 4 Secundarias)"), color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            record.coreRunes.forEachIndexed { idx, rune ->
                                val isKeystone = idx == 0
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                                    border = BorderStroke(1.dp, if (isKeystone) HextechGold else HextechCardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(HextechSurfaceVariant)
                                                .border(1.5.dp, if (isKeystone) HextechGold else HextechCyan, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AppAssetImage(
                                                url = rune.iconUrl,
                                                contentDescription = rune.runeName,
                                                fallbackText = rune.runeName,
                                                modifier = Modifier.size(26.dp),
                                                shape = CircleShape
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = com.example.util.tr((if (isKeystone) "[Clave] " else "[Secundaria] ") + rune.runeName),
                                                color = if (isKeystone) HextechGold else Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(com.example.util.tr(rune.description), color = TextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Spells
                if (record.coreSpells.isNotEmpty()) {
                    item {
                        Text(tr("Hechizos de Invocador"), color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            record.coreSpells.forEach { spell ->
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                                    border = BorderStroke(1.dp, HextechCardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(HextechSurfaceVariant)
                                                .border(1.dp, HextechCyan, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AppAssetImage(
                                                url = spell.iconUrl,
                                                contentDescription = spell.spellName,
                                                fallbackText = spell.spellName,
                                                modifier = Modifier.size(26.dp),
                                                shape = CircleShape
                                            )
                                        }
                                        Column {
                                            Text(com.example.util.tr(spell.spellName), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(com.example.util.tr(spell.description), color = TextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Situational Spells
                if (record.situationalSpells.isNotEmpty()) {
                    item {
                        Text(tr("Hechizos Situacionales (Opcional)"), color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            record.situationalSpells.forEach { spell ->
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                                    border = BorderStroke(1.dp, HextechCardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(HextechSurfaceVariant)
                                                .border(1.dp, HextechCyan, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AppAssetImage(
                                                url = spell.iconUrl,
                                                contentDescription = spell.spellName,
                                                fallbackText = spell.spellName,
                                                modifier = Modifier.size(26.dp),
                                                shape = CircleShape
                                            )
                                        }
                                        Column {
                                            Text(com.example.util.tr(spell.spellName), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(com.example.util.tr(spell.description), color = TextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Video de Introducción / Gameplay
                if (!record.gameplayVideoUri.isNullOrBlank()) {
                    item {
                        Text(tr("Video de Introducción"), color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                            border = BorderStroke(1.dp, HextechGold)
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    VideoView(ctx).apply {
                                        setVideoURI(Uri.parse(record.gameplayVideoUri))
                                        setOnPreparedListener { mp -> mp.isLooping = true; start() }
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // Video de Combos
                if (!record.comboVideoUri.isNullOrBlank()) {
                    item {
                        Text(tr("Video de Combos / Demostración"), color = HextechCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                            border = BorderStroke(1.dp, HextechCyan)
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    VideoView(ctx).apply {
                                        setVideoURI(Uri.parse(record.comboVideoUri))
                                        setOnPreparedListener { mp -> mp.isLooping = true; start() }
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Rating section (1 to 5 stars)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HextechSurface),
                border = BorderStroke(1.dp, HextechGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = com.example.util.tr(if (hasVoted) "¡Gracias por tu valoración!" else "Califica esta Build (1 a 5 Estrellas)"),
                        color = HextechGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..5) {
                            IconButton(
                                onClick = {
                                    if (!hasVoted) {
                                        CustomChampionBuildsManager.rateBuild(context, record.id, i)
                                        Toast.makeText(context, com.example.util.appTr("¡Calificación de $i estrellas enviada!"), Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = com.example.util.trNullable("$i estrellas"),
                                    tint = if (i <= userRating) HextechGold else TextSecondary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
            }
        }
    }

    if (showSubscribeConfirm) {
        val currentOrangeBalance by SubscriptionManager.orangeEssence.collectAsStateWithLifecycle()

        Dialog(onDismissRequest = { showSubscribeConfirm = false }) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HextechSurface),
                border = BorderStroke(1.dp, Color(0xFFFF8C00)),
                modifier = Modifier.fillMaxWidth(0.95f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = Color(0xFFFF8C00),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = com.example.util.tr("Suscripción al Creador"),
                        color = Color(0xFFFF9E1B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    // Card showing Orange Essence cost & user balance
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF8C00).copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, Color(0xFFFF8C00).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_orange_essence),
                                    contentDescription = "Esencia Naranja",
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = com.example.util.tr("Costo de Suscripción"),
                                        color = Color.LightGray,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = com.example.util.tr("${CreatorSubscriptionManager.SUBSCRIPTION_EN_COST} Esencias Naranjas"),
                                        color = Color(0xFFFF9E1B),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = com.example.util.tr("Tu saldo"),
                                    color = Color.LightGray,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = com.example.util.tr("$currentOrangeBalance EN"),
                                    color = if (currentOrangeBalance >= CreatorSubscriptionManager.SUBSCRIPTION_EN_COST) Color.White else DangerRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = com.example.util.tr("Al suscribirte al perfil oficial de ${record.creatorName}, se descontarán ${CreatorSubscriptionManager.SUBSCRIPTION_EN_COST} Esencias Naranjas (EN) de tu cuenta de forma definitiva. Un porcentaje será entregado directamente al creador como soporte a su trabajo. ¿Deseas confirmar la suscripción?"),
                        color = Color.White,
                        fontSize = 11.5.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showSubscribeConfirm = false },
                            colors = ButtonDefaults.buttonColors(containerColor = HextechDarkBg),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(tr("Cancelar"), fontSize = 11.5.sp)
                        }
                        Button(
                            onClick = {
                                showSubscribeConfirm = false
                                val creatorKey = if (record.creatorUserId.isNotBlank()) record.creatorUserId else record.creatorName
                                CreatorSubscriptionManager.subscribeWithOrangeEssence(
                                    creatorKey = creatorKey,
                                    creatorName = record.creatorName,
                                    creatorUid = record.creatorUserId,
                                    context = context
                                ) { success, msg ->
                                    Toast.makeText(context, com.example.util.appTr(msg), Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF8C00)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(com.example.util.tr("Confirmar (${CreatorSubscriptionManager.SUBSCRIPTION_EN_COST} EN)"), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = HextechDarkBg)
                        }
                    }
                }
            }
        }
    }
}
