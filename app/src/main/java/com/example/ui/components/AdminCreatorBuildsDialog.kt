package com.example.ui.components

import android.widget.Toast
import com.example.data.WildRiftRepository
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WorkspacePremium
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.data.local.AppDatabase
import com.example.data.local.CustomChampionBuildRecord
import com.example.data.local.CustomChampionBuildsManager
import com.example.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale
import com.example.util.CreatorSubscriptionManager
import com.example.util.SubscriptionManager
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.Lock

enum class BuildsFilterTab {
    ALL,
    FAVORITES
}

data class CreatorPodiumEntry(
    val userId: String = "",
    val name: String,
    val avatarId: String? = null,
    val rankBorder: String = "NONE",
    val isAdmin: Boolean = false,
    val role: String = "creador",
    val buildsCount: Int = 0,
    val totalVotes: Int = 0,
    val averageRating: Double = 0.0,
    val score: Double = 0.0,
    val subscribersCount: Int = 0
)

@Composable
fun AdminCreatorBuildsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val customBuilds by CustomChampionBuildsManager.customBuilds.collectAsStateWithLifecycle()
    val currentUserName by SubscriptionManager.userName.collectAsStateWithLifecycle()
    val currentUserRole by SubscriptionManager.userRole.collectAsStateWithLifecycle()
    val myBuildsCount = remember(customBuilds, currentUserName) {
        customBuilds.count { it.creatorName.equals(currentUserName, ignoreCase = true) }
    }
    var showBuildCreator by remember { mutableStateOf(false) }
    var buildToEdit by remember { mutableStateOf<CustomChampionBuildRecord?>(null) }
    var selectedBuildForDetail by remember { mutableStateOf<CustomChampionBuildRecord?>(null) }
    var selectedCreatorFilter by remember { mutableStateOf<String?>(null) }
    var selectedCreatorForProfile by remember { mutableStateOf<CreatorPodiumEntry?>(null) }

    val favoriteDao = remember { AppDatabase.getDatabase(context).favoriteBuildsDao() }
    val favorites by favoriteDao.getAllFavorites().collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedFilter by remember { mutableStateOf(BuildsFilterTab.ALL) }

    // Lista de usuarios registrados en la nube para sincronizar avatares y marcos de creadores
    var registeredUsers by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    LaunchedEffect(Unit) {
        CustomChampionBuildsManager.init(context)
        try {
            FirebaseFirestore.getInstance().collection("users").get()
                .addOnSuccessListener { snapshot ->
                    val list = mutableListOf<Map<String, Any>>()
                    for (doc in snapshot.documents) {
                        val data = doc.data ?: continue
                        val mut = HashMap<String, Any>(data)
                        mut["uid"] = doc.id
                        list.add(mut)
                    }
                    registeredUsers = list
                }
        } catch (_: Exception) {}
    }

    // Cálculo dinámico del Top 3 de Creadores para el Podio
    val podiumCreators = remember(customBuilds, registeredUsers) {
        val userMapByName = registeredUsers.associateBy { (it["name"] as? String ?: "").trim().lowercase(Locale.ROOT) }
        val userMapByUid = registeredUsers.associateBy { (it["uid"] as? String ?: "") }

        // 1. Agrupar builds por nombre de creador
        val creatorGrouped = customBuilds.groupBy { it.creatorName.trim() }
        val rankingList = mutableListOf<CreatorPodiumEntry>()

        for ((creatorName, builds) in creatorGrouped) {
            if (creatorName.isBlank()) continue
            val totalVotes = builds.sumOf { it.voteCount }
            val ratingSum = builds.sumOf { it.ratingSum }
            val avgRating = if (totalVotes > 0) ratingSum / totalVotes else if (builds.isNotEmpty()) 5.0 else 0.0
            val buildsCount = builds.size
            val score = (totalVotes * 10.0) + (avgRating * 20.0) + (buildsCount * 15.0)

            // Buscar datos de avatar y marco del usuario
            val firstRecord = builds.firstOrNull()
            val matchedUser = (if (!firstRecord?.creatorUserId.isNullOrBlank()) userMapByUid[firstRecord?.creatorUserId] else null)
                ?: userMapByName[creatorName.lowercase(Locale.ROOT)]

            val avatarId = (matchedUser?.get("avatarId") as? String)
                ?: firstRecord?.creatorAvatarId
                ?: "default_poro"

            val rankBorder = (matchedUser?.get("rankBorder") as? String)
                ?: firstRecord?.creatorRankBorder
                ?: "NONE"

            val role = (matchedUser?.get("role") as? String) ?: "creador"
            val isAdmin = role == "admin" || (matchedUser?.get("isAdmin") as? Boolean) == true || (firstRecord?.creatorIsAdmin == true)

            val userId = (matchedUser?.get("uid") as? String) ?: firstRecord?.creatorUserId ?: ""
            val realSubs = if (userId.isNotBlank()) {
                registeredUsers.count { u ->
                    val subList = (u["subscribedCreators"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    subList.any { sub -> sub == userId || sub.trim().lowercase() == creatorName.trim().lowercase() }
                }
            } else 0

            rankingList.add(
                CreatorPodiumEntry(
                    userId = userId,
                    name = creatorName,
                    avatarId = avatarId,
                    rankBorder = rankBorder,
                    isAdmin = isAdmin,
                    role = role,
                    buildsCount = buildsCount,
                    totalVotes = totalVotes,
                    averageRating = avgRating,
                    score = score + (realSubs * 12.0),
                    subscribersCount = realSubs
                )
            )
        }

        // 2. Si hay creadores registrados en la plataforma que aún no han creado builds, incorporarlos
        for (u in registeredUsers) {
            val uRole = u["role"] as? String ?: "free"
            val uName = (u["name"] as? String ?: "").trim()
            if (uName.isNotBlank() && (uRole == "creador_lvl2" || uRole == "creador_lvl3" || uRole == "creador_lvl4" || uRole == "creador_lvl5" || uRole == "creador" || uRole == "streamer" || uRole == "admin")) {
                val alreadyAdded = rankingList.any { it.name.equals(uName, ignoreCase = true) }
                if (!alreadyAdded) {
                    val userId = u["uid"] as? String ?: ""
                    val realSubs = if (userId.isNotBlank()) {
                        registeredUsers.count { ru ->
                            val subList = (ru["subscribedCreators"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                            subList.any { sub -> sub == userId || sub.trim().lowercase() == uName.trim().lowercase() }
                        }
                    } else 0

                    rankingList.add(
                        CreatorPodiumEntry(
                            userId = userId,
                            name = uName,
                            avatarId = u["avatarId"] as? String ?: "default_poro",
                            rankBorder = u["rankBorder"] as? String ?: "NONE",
                            isAdmin = uRole == "admin",
                            role = uRole,
                            buildsCount = 0,
                            totalVotes = 0,
                            averageRating = 5.0,
                            score = (if (uRole == "creador_lvl2") 50.0 else if (uRole == "admin") 40.0 else 30.0) + (realSubs * 12.0),
                            subscribersCount = realSubs
                        )
                    )
                }
            }
        }

        // 3. Fallback en caso de que no haya creadores suficientes para completar el podio de 3
        val defaultLegends = listOf(
            CreatorPodiumEntry(
                name = "Coach Sovereign",
                avatarId = "avatar_soberano_wr",
                rankBorder = "CHALLENGER",
                isAdmin = true,
                role = "admin",
                buildsCount = 6,
                totalVotes = 84,
                averageRating = 5.0,
                score = 300.0,
                subscribersCount = 254
            ),
            CreatorPodiumEntry(
                name = "Wild Rift Pro",
                avatarId = "avatar_kaisa",
                rankBorder = "GRANDMASTER",
                isAdmin = false,
                role = "creador_lvl2",
                buildsCount = 4,
                totalVotes = 52,
                averageRating = 4.9,
                score = 220.0,
                subscribersCount = 142
            ),
            CreatorPodiumEntry(
                name = "Hextech Master",
                avatarId = "avatar_zed",
                rankBorder = "MASTER",
                isAdmin = false,
                role = "creador",
                buildsCount = 3,
                totalVotes = 31,
                averageRating = 4.8,
                score = 160.0,
                subscribersCount = 89
            )
        )

        val finalList = rankingList.sortedWith(
            compareByDescending<CreatorPodiumEntry> { it.subscribersCount }
                .thenByDescending { it.totalVotes }
                .thenByDescending { it.score }
        ).toMutableList()
        var fallbackIdx = 0
        while (finalList.size < 3 && fallbackIdx < defaultLegends.size) {
            val fallback = defaultLegends[fallbackIdx]
            if (finalList.none { it.name.equals(fallback.name, ignoreCase = true) }) {
                finalList.add(fallback)
            }
            fallbackIdx++
        }

        finalList.take(3)
    }

    val officialCreatorsList = remember(registeredUsers, customBuilds) {
        registeredUsers.filter { u ->
            val uRole = u["role"] as? String ?: ""
            uRole in listOf("creador", "creador_lvl2", "creador_lvl3", "creador_lvl4", "creador_lvl5", "streamer")
        }.map { u ->
            val uName = (u["name"] as? String ?: "Anónimo").trim()
            val userId = u["uid"] as? String ?: ""
            val uRole = u["role"] as? String ?: "creador"
            val avatarId = u["avatarId"] as? String ?: "default_poro"
            val rankBorder = u["rankBorder"] as? String ?: "NONE"
            val isAdmin = uRole == "admin"
            
            // Calcular suscriptores para este creador
            val subsCount = registeredUsers.count { ru ->
                val subList = (ru["subscribedCreators"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                subList.any { sub -> sub == userId || sub.trim().lowercase() == uName.lowercase() }
            }
            
            val buildsCount = customBuilds.count { it.creatorName.equals(uName, ignoreCase = true) }
            
            CreatorPodiumEntry(
                userId = userId,
                name = uName,
                avatarId = avatarId,
                rankBorder = rankBorder,
                isAdmin = isAdmin,
                role = uRole,
                buildsCount = buildsCount,
                totalVotes = 0,
                averageRating = 5.0,
                score = 0.0,
                subscribersCount = subsCount
            )
        }.sortedByDescending { it.subscribersCount }
    }

    val filteredBuilds = remember(customBuilds, favorites, selectedFilter, selectedCreatorFilter) {
        val base = when (selectedFilter) {
            BuildsFilterTab.ALL -> customBuilds
            BuildsFilterTab.FAVORITES -> {
                val favIds = favorites.map { it.buildId }.toSet()
                customBuilds.filter { it.id in favIds }
            }
        }
        if (selectedCreatorFilter != null) {
            base.filter { it.creatorName.equals(selectedCreatorFilter, ignoreCase = true) }
        } else {
            base
        }
    }

    if (showBuildCreator || buildToEdit != null) {
        ChampionBuildCreatorDialog(
            existingRecord = buildToEdit,
            onDismiss = {
                showBuildCreator = false
                buildToEdit = null
            }
        )
    }

    if (selectedBuildForDetail != null) {
        CustomBuildDetailDialog(
            record = selectedBuildForDetail!!,
            onDismiss = { selectedBuildForDetail = null }
        )
    }

    if (selectedCreatorForProfile != null) {
        CreatorProfileDialog(
            entry = selectedCreatorForProfile!!,
            registeredUsers = registeredUsers,
            customBuilds = customBuilds,
            onDismiss = { selectedCreatorForProfile = null },
            onOpenBuild = { selectedBuildForDetail = it }
        )
    }

    androidx.activity.compose.BackHandler {
        if (selectedBuildForDetail != null) {
            selectedBuildForDetail = null
        } else if (selectedCreatorForProfile != null) {
            selectedCreatorForProfile = null
        } else if (showBuildCreator || buildToEdit != null) {
            showBuildCreator = false
            buildToEdit = null
        } else {
            onDismiss()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = HextechSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = HextechGold.copy(alpha = 0.2f)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = HextechGold,
                            modifier = Modifier.padding(6.dp).size(20.dp)
                        )
                    }
                    Column {
                        Text("Panel de Creador (Admin)", color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Gestión de builds, ranking y creadores oficiales", color = TextSecondary, fontSize = 11.sp)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        CustomChampionBuildsManager.syncFromCloud(context) { success ->
                            Toast.makeText(
                                context,
                                if (success) "Builds sincronizadas en tiempo real" else "Sincronizando builds...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }) {
                        Icon(Icons.Default.Sync, contentDescription = "Sincronizar builds", tint = HextechGold)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }
            }

            HorizontalDivider(color = HextechCardBorder)

            // Podio de Creadores (1er, 2do y 3er Lugar con Avatar, Nombre y Marco)
            if (podiumCreators.size >= 3) {
                // Título dinámico para podio (por popularidad)
                Text(
                    text = "🏆 PODIO POR POPULARIDAD",
                    color = HextechGold,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                CreatorPodiumCard(
                    first = podiumCreators[0],
                    second = podiumCreators[1],
                    third = podiumCreators[2],
                    selectedCreator = selectedCreatorFilter,
                    onSelectCreator = { entry ->
                        selectedCreatorForProfile = entry
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (officialCreatorsList.isNotEmpty()) {
                var isExpanded by remember { mutableStateOf(true) }
                
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👥 " + com.example.util.tr("Lista de Creadores Oficiales"),
                        color = HextechGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 4.dp).weight(1f)
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = HextechGold,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                if (isExpanded) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(officialCreatorsList) { creator ->
                            Surface(
                                onClick = {
                                    selectedCreatorForProfile = creator
                                },
                                color = HextechSurface.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, HextechCardBorder),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.width(105.dp)
                            ) {
                                Column(
                                modifier = Modifier.padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                UserAvatarView(
                                    avatarId = creator.avatarId,
                                    size = 32.dp,
                                    fallbackInitial = creator.name.take(1).uppercase(Locale.ROOT),
                                    rankBorder = creator.rankBorder,
                                    isAdmin = creator.isAdmin
                                )
                                Text(
                                    text = creator.name,
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                
                                val displayLabel = when (creator.role) {
                                    "creador" -> "Creador Lvl 1"
                                    "creador_lvl2" -> "Creador Lvl 2"
                                    "creador_lvl3" -> "Creador Lvl 3"
                                    "creador_lvl4" -> "Creador Lvl 4"
                                    "creador_lvl5" -> "Creador Lvl 5"
                                    "streamer" -> "Streamer"
                                    else -> creator.role.replaceFirstChar { it.uppercase() }
                                }
                                
                                Text(
                                    text = displayLabel,
                                    color = HextechGoldLight,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                Text(
                                    text = "👥 ${creator.subscribersCount} subs",
                                    color = TextSecondary,
                                    fontSize = 8.5.sp
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
            }

            // Action Button: Crear Build con límites según el nivel de creador
            Button(
                onClick = {
                    val limits = CreatorSubscriptionManager.getCreatorLimits(currentUserRole)
                    if (myBuildsCount >= limits.maxChampions) {
                        Toast.makeText(
                            context,
                            "Límite de builds alcanzado para tu plan (${limits.maxChampions} build/s). Por favor mejora tu nivel.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        showBuildCreator = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = HextechDarkBg, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Crear Nueva Build Oficial", color = HextechDarkBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            // Filtros de categoría y creador activo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedFilter == BuildsFilterTab.ALL && selectedCreatorFilter == null,
                    onClick = {
                        selectedFilter = BuildsFilterTab.ALL
                        selectedCreatorFilter = null
                    },
                    label = { Text("Todas (${customBuilds.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = HextechGold.copy(alpha = 0.2f),
                        selectedLabelColor = HextechGold
                    )
                )

                FilterChip(
                    selected = selectedFilter == BuildsFilterTab.FAVORITES,
                    onClick = { selectedFilter = BuildsFilterTab.FAVORITES },
                    label = { Text("Mis Favoritos (${favorites.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DangerRed.copy(alpha = 0.2f),
                        selectedLabelColor = DangerRed
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp), tint = DangerRed)
                    }
                )

                if (selectedCreatorFilter != null) {
                    FilterChip(
                        selected = true,
                        onClick = { selectedCreatorFilter = null },
                        label = { Text("Creador: $selectedCreatorFilter ✕") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HextechCyan.copy(alpha = 0.25f),
                            selectedLabelColor = HextechCyan
                        ),
                        border = BorderStroke(1.dp, HextechCyan)
                    )
                }
            }

            // Builds List
            if (filteredBuilds.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (selectedCreatorFilter != null) "No hay builds para el creador \"$selectedCreatorFilter\"."
                        else if (selectedFilter == BuildsFilterTab.FAVORITES) "No tienes builds favoritas guardadas."
                        else "No hay builds creadas todavía.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredBuilds) { record ->
                        val champObj = remember(record.championId) {
                            WildRiftRepository.champions.find { it.id.equals(record.championId, ignoreCase = true) }
                        }
                        val avgRating = if (record.voteCount > 0) record.ratingSum / record.voteCount else 0.0

                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                            border = BorderStroke(1.dp, HextechCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedBuildForDetail = record }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (champObj != null) {
                                            ChampionAvatar(champion = champObj, size = 40.dp, showTierBadge = false)
                                        }
                                        Column {
                                            Text(
                                                text = "${record.championName} - ${record.buildTitle}",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "Creador: ${record.creatorName} • ${record.role}",
                                                    color = HextechGold,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = HextechGold, modifier = Modifier.size(12.dp))
                                                Text(
                                                    text = "${String.format(Locale.US, "%.1f", avgRating)} (${record.voteCount} votos)",
                                                    color = TextSecondary,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { buildToEdit = record },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = HextechGold, modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(
                                            onClick = {
                                                CustomChampionBuildsManager.deleteBuild(context, record.id)
                                                Toast.makeText(context, "Build eliminada", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = DangerRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                // Texto animado para ver completo
                                val infiniteTransition = rememberInfiniteTransition(label = "tapPrompt")
                                val alpha by infiniteTransition.animateFloat(
                                    initialValue = 0.3f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1000),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "alpha"
                                )
                                Text(
                                    text = "Presiona para ver completo",
                                    color = HextechGold.copy(alpha = alpha),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Podio oficial para los 3 mejores creadores de la comunidad.
 * Visualiza el 1er lugar (centro, más alto), 2do lugar (izquierda) y 3er lugar (derecha)
 * mostrando Avatar, Nombre de Usuario y Marco correspondiente.
 */
@Composable
fun CreatorPodiumCard(
    first: CreatorPodiumEntry,
    second: CreatorPodiumEntry,
    third: CreatorPodiumEntry,
    selectedCreator: String?,
    onSelectCreator: (CreatorPodiumEntry) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = HextechDarkBg,
        border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Encabezado del podio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Podio de Creadores",
                        tint = HextechGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "PODIO DE CREADORES",
                        color = HextechGold,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = HextechGold.copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, HextechGold.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "TOP 3 OFICIAL",
                        color = HextechGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Estructura del Podio (2do Lugar, 1er Lugar, 3er Lugar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 🥈 2DO LUGAR (Izquierda)
                PodiumColumn(
                    entry = second,
                    rank = 2,
                    rankBadgeText = "🥈 2° Lugar",
                    badgeColor = Color(0xFF94A3B8),
                    badgeBgColor = Color(0xFF64748B).copy(alpha = 0.25f),
                    avatarSize = 52.dp,
                    pedestalHeight = 60.dp,
                    pedestalBrush = Brush.verticalGradient(
                        listOf(Color(0xFF475569), Color(0xFF1E293B))
                    ),
                    pedestalBorderColor = Color(0xFF94A3B8),
                    numeralColor = Color(0xFFCBD5E1),
                    isSelected = selectedCreator.equals(second.name, ignoreCase = true),
                    onClick = { onSelectCreator(second) },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 👑 1ER LUGAR (Centro - Elevado y Destacado)
                PodiumColumn(
                    entry = first,
                    rank = 1,
                    rankBadgeText = "👑 1° Lugar",
                    badgeColor = HextechGold,
                    badgeBgColor = HextechGold.copy(alpha = 0.25f),
                    avatarSize = 64.dp,
                    pedestalHeight = 82.dp,
                    pedestalBrush = Brush.verticalGradient(
                        listOf(HextechGold.copy(alpha = 0.5f), Color(0xFF854D0E), HextechDarkBg)
                    ),
                    pedestalBorderColor = HextechGold,
                    numeralColor = HextechGold,
                    isSelected = selectedCreator.equals(first.name, ignoreCase = true),
                    onClick = { onSelectCreator(first) },
                    modifier = Modifier.weight(1.15f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 🥉 3ER LUGAR (Derecha)
                PodiumColumn(
                    entry = third,
                    rank = 3,
                    rankBadgeText = "🥉 3° Lugar",
                    badgeColor = Color(0xFFFDBA74),
                    badgeBgColor = Color(0xFF9A3412).copy(alpha = 0.25f),
                    avatarSize = 48.dp,
                    pedestalHeight = 46.dp,
                    pedestalBrush = Brush.verticalGradient(
                        listOf(Color(0xFF78350F), Color(0xFF451A03))
                    ),
                    pedestalBorderColor = Color(0xFFB45309),
                    numeralColor = Color(0xFFFDBA74),
                    isSelected = selectedCreator.equals(third.name, ignoreCase = true),
                    onClick = { onSelectCreator(third) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PodiumColumn(
    entry: CreatorPodiumEntry,
    rank: Int,
    rankBadgeText: String,
    badgeColor: Color,
    badgeBgColor: Color,
    avatarSize: androidx.compose.ui.unit.Dp,
    pedestalHeight: androidx.compose.ui.unit.Dp,
    pedestalBrush: Brush,
    pedestalBorderColor: Color,
    numeralColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.border(1.dp, HextechCyan, RoundedCornerShape(8.dp)).background(HextechCyan.copy(alpha = 0.08f))
                else Modifier
            )
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Insignia del lugar
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = badgeBgColor,
            border = BorderStroke(0.6.dp, badgeColor.copy(alpha = 0.7f))
        ) {
            Text(
                text = rankBadgeText,
                color = badgeColor,
                fontSize = if (rank == 1) 9.5.sp else 8.5.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Avatar de usuario con marco integrado (UserAvatarView)
        Box(
            modifier = Modifier.padding(horizontal = if (entry.isAdmin) 4.dp else 0.dp),
            contentAlignment = Alignment.Center
        ) {
            UserAvatarView(
                avatarId = entry.avatarId,
                size = avatarSize,
                fallbackInitial = entry.name.take(1).uppercase(Locale.ROOT),
                rankBorder = entry.rankBorder,
                isAdmin = entry.isAdmin
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Nombre de usuario
        Text(
            text = entry.name,
            color = if (rank == 1) HextechGold else Color.White,
            fontWeight = if (rank == 1) FontWeight.ExtraBold else FontWeight.Bold,
            fontSize = if (rank == 1) 12.5.sp else 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        // Resumen de estadísticas del creador mostrando cantidad de suscriptores en vez de votos
        Text(
            text = "${entry.buildsCount} builds • 👥 ${entry.subscribersCount} Subs",
            color = if (rank == 1) HextechGoldLight else TextSecondary,
            fontSize = if (rank == 1) 9.5.sp else 8.5.sp,
            fontWeight = if (rank == 1) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Pedestal del Podio
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(pedestalHeight)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(pedestalBrush)
                .border(
                    BorderStroke(if (rank == 1) 1.5.dp else 1.dp, pedestalBorderColor),
                    RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                color = numeralColor,
                fontSize = if (rank == 1) 28.sp else if (rank == 2) 22.sp else 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
            )
        }
    }
}

@Composable
fun CreatorProfileDialog(
    entry: CreatorPodiumEntry,
    registeredUsers: List<Map<String, Any>>,
    customBuilds: List<CustomChampionBuildRecord>,
    onDismiss: () -> Unit,
    onOpenBuild: (CustomChampionBuildRecord) -> Unit
) {
    val context = LocalContext.current
    val subscribedSet by CreatorSubscriptionManager.subscribedCreatorKeys.collectAsStateWithLifecycle()
    val userRole by SubscriptionManager.userRole.collectAsStateWithLifecycle()
    val currentBlueEssence by SubscriptionManager.blueEssence.collectAsStateWithLifecycle()

    val creatorKey = if (entry.userId.isNotBlank()) entry.userId else entry.name
    val isSubscribed = remember(subscribedSet, creatorKey, entry.name) {
        CreatorSubscriptionManager.isSubscribed(creatorKey) || 
        CreatorSubscriptionManager.isSubscribed(entry.name) ||
        entry.name.lowercase().contains("system") ||
        userRole == "admin"
    }

    var showUnsubscribeConfirm1 by remember { mutableStateOf(false) }
    var showUnsubscribeConfirm2 by remember { mutableStateOf(false) }
    var showSubscribeConfirm by remember { mutableStateOf(false) }

    val creatorBuilds = remember(customBuilds, entry.name) {
        customBuilds.filter { it.creatorName.equals(entry.name, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            color = HextechSurface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, HextechGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Perfil de Creador",
                        color = HextechGold,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "Perfil de Creador WR: ${entry.name}")
                                putExtra(
                                    android.content.Intent.EXTRA_TEXT,
                                    "¡Mira el perfil oficial de ${entry.name} en el Coach de Wild Rift! Rango: ${entry.rankBorder}, builds publicadas: ${creatorBuilds.size}."
                                )
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Compartir Perfil de Creador"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Compartir Perfil", tint = HextechGold)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                        }
                    }
                }

                HorizontalDivider(color = HextechCardBorder)

                // Info Creador
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HextechDarkBg, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    UserAvatarView(
                        avatarId = entry.avatarId,
                        size = 64.dp,
                        fallbackInitial = entry.name.take(1).uppercase(Locale.ROOT),
                        rankBorder = entry.rankBorder,
                        isAdmin = entry.isAdmin
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = entry.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Rol: ${entry.role.replaceFirstChar { it.uppercase() }}",
                            color = HextechGold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "👥 ${entry.subscribersCount} Suscriptores • 📦 ${creatorBuilds.size} Builds",
                            color = TextSecondary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Botón de suscripción con verificación de límites
                if (!isSubscribed) {
                    Button(
                        onClick = {
                            val limits = CreatorSubscriptionManager.getCreatorLimits(entry.role)
                            if (entry.subscribersCount >= limits.maxSubscribers) {
                                CreatorSubscriptionManager.sendLimitExceededNotification(
                                    creatorUid = entry.userId,
                                    creatorName = entry.name
                                )
                                Toast.makeText(
                                    context,
                                    "El creador alcanzó el límite de su plan (${limits.maxSubscribers} subs). Se le envió una notificación para mejorar su plan.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                showSubscribeConfirm = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = HextechDarkBg)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Suscribirse por ${CreatorSubscriptionManager.SUBSCRIPTION_EA_COST} EA",
                            color = HextechDarkBg,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF00FF66).copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color(0xFF00FF66).copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color(0xFF00FF66))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Suscripción Activa (Acceso Total)",
                                    color = Color(0xFF00FF66),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            }
                        }

                        // Botón de Cancelar Suscripción con Doble Confirmación (Para cuentas que no son del sistema ni admins)
                        val isSystemOrAdmin = entry.name.lowercase().contains("system") || userRole == "admin"
                        if (!isSystemOrAdmin) {
                            Button(
                                onClick = {
                                    showUnsubscribeConfirm1 = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.15f), contentColor = DangerRed),
                                border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Quitar Suscripción",
                                    color = DangerRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // DIALOGO DE CONFIRMACIÓN 1
                if (showUnsubscribeConfirm1) {
                    androidx.compose.ui.window.Dialog(onDismissRequest = { showUnsubscribeConfirm1 = false }) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = HextechSurface,
                            border = BorderStroke(1.5.dp, HextechGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Confirmación de Suscripción",
                                    color = HextechGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Text(
                                    text = "¿Estás seguro de que deseas cancelar tu suscripción al perfil de ${entry.name}?",
                                    color = Color.White,
                                    fontSize = 12.5.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { showUnsubscribeConfirm1 = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = HextechDarkBg),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("No, Cancelar", fontSize = 11.5.sp)
                                    }
                                    Button(
                                        onClick = {
                                            showUnsubscribeConfirm1 = false
                                            showUnsubscribeConfirm2 = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Sí, Continuar", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // DIALOGO DE CONFIRMACIÓN 2 (ADVERTENCIA CRÍTICA)
                if (showUnsubscribeConfirm2) {
                    androidx.compose.ui.window.Dialog(onDismissRequest = { showUnsubscribeConfirm2 = false }) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = HextechSurface,
                            border = BorderStroke(2.dp, DangerRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "⚠️ ¡ADVERTENCIA CRÍTICA!",
                                    color = DangerRed,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Text(
                                    text = "Recuerda que al momento de quitar tu suscripción pierdes acceso total a las builds oficiales de este perfil. ¿Aún así deseas continuar con la baja?",
                                    color = Color.White,
                                    fontSize = 12.5.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { showUnsubscribeConfirm2 = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = HextechDarkBg),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("No, Conservar", fontSize = 11.5.sp)
                                    }
                                    Button(
                                        onClick = {
                                            showUnsubscribeConfirm2 = false
                                            CreatorSubscriptionManager.unsubscribe(creatorKey, context)
                                            Toast.makeText(context, "Suscripción cancelada correctamente", Toast.LENGTH_SHORT).show()
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Confirmar Baja", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                if (showSubscribeConfirm) {
                    Dialog(onDismissRequest = { showSubscribeConfirm = false }) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = HextechSurface),
                            border = BorderStroke(1.dp, HextechGold),
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
                                    tint = HextechGold,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "Advertencia de Suscripción",
                                    color = HextechGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Text(
                                    text = "Al suscribirte al perfil oficial de ${entry.name}, se descontarán ${CreatorSubscriptionManager.SUBSCRIPTION_EA_COST} Esencias Azules (EA) de tu cuenta de forma definitiva. Un porcentaje de estas esencias será entregado directamente al creador como soporte a su trabajo. ¿Deseas confirmar la suscripción?",
                                    color = Color.White,
                                    fontSize = 12.sp,
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
                                        Text("Cancelar", fontSize = 11.5.sp)
                                    }
                                    Button(
                                        onClick = {
                                            showSubscribeConfirm = false
                                            CreatorSubscriptionManager.subscribeWithBlueEssence(
                                                creatorKey = creatorKey,
                                                creatorName = entry.name,
                                                creatorUid = entry.userId,
                                                context = context
                                            ) { success, msg ->
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Confirmar", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = HextechDarkBg)
                                    }
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "Builds Creadas:",
                    color = HextechGoldLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                if (creatorBuilds.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Este creador aún no ha publicado builds.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(creatorBuilds) { b ->
                            val isLocked = !isSubscribed && !entry.name.lowercase().contains("system")
                            val champObj = remember(b.championId) {
                                WildRiftRepository.champions.find { it.id.equals(b.championId, ignoreCase = true) }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = HextechDarkBg,
                                border = BorderStroke(1.dp, HextechCardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onOpenBuild(b)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (champObj != null) {
                                        ChampionAvatar(champion = champObj, size = 32.dp, showTierBadge = false)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = b.buildTitle,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${b.championName} • ${b.role}",
                                            color = HextechGold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    if (isLocked) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Bloqueado",
                                            tint = HextechGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
