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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
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

enum class BuildsFilterTab {
    ALL,
    CREATORS,
    FAVORITES
}

data class CreatorListItem(
    val uid: String = "",
    val name: String,
    val avatarId: String = "default_poro",
    val rankBorder: String = "NONE",
    val secondaryRole: String = "none",
    val equippedFrame: String = "AUTO",
    val isAdmin: Boolean = false,
    val isVerified: Boolean = false,
    val buildsCount: Int = 0,
    val subscribersCount: Int = 0
)

data class CreatorPodiumEntry(
    val userId: String = "",
    val name: String,
    val avatarId: String? = null,
    val rankBorder: String = "NONE",
    val secondaryRole: String = "none",
    val equippedFrame: String = "AUTO",
    val isAdmin: Boolean = false,
    val role: String = "creador",
    val buildsCount: Int = 0,
    val totalVotes: Int = 0,
    val averageRating: Double = 0.0,
    val subscribersCount: Int = 0,
    val score: Double = 0.0
)

@Composable
fun AdminCreatorBuildsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val customBuilds by CustomChampionBuildsManager.customBuilds.collectAsStateWithLifecycle()
    var showBuildCreator by remember { mutableStateOf(false) }
    var buildToEdit by remember { mutableStateOf<CustomChampionBuildRecord?>(null) }
    var selectedBuildForDetail by remember { mutableStateOf<CustomChampionBuildRecord?>(null) }
    var selectedCreatorFilter by remember { mutableStateOf<String?>(null) }
    var creatorToViewProfile by remember { mutableStateOf<CreatorListItem?>(null) }

    val favoriteDao = remember { AppDatabase.getDatabase(context).favoriteBuildsDao() }
    val favorites by favoriteDao.getAllFavorites().collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedFilter by remember { mutableStateOf(BuildsFilterTab.ALL) }
    var userSearchQuery by remember { mutableStateOf("") }

    // Lista de usuarios registrados en la nube para sincronizar avatares y marcos de creadores
    var registeredUsers by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val subscribedSet by com.example.util.CreatorSubscriptionManager.subscribedCreatorKeys.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        CustomChampionBuildsManager.init(context)
        com.example.util.CreatorSubscriptionManager.init(context)
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

    // Helper para calcular la cantidad de suscriptores reales de un creador
    fun countSubscribersForCreator(creatorUid: String, creatorName: String, matchedUser: Map<String, Any>?): Int {
        val cUidClean = creatorUid.trim().lowercase(Locale.ROOT)
        val cNameClean = creatorName.trim().lowercase(Locale.ROOT)
        val uniqueSubs = mutableSetOf<String>()

        for (u in registeredUsers) {
            val uUid = (u["uid"] as? String ?: "").ifBlank { "anon_${uniqueSubs.size}" }
            val rawSubs = u["subscribedCreators"] as? List<*>
            val uSubs = rawSubs?.mapNotNull { it?.toString()?.trim()?.lowercase(Locale.ROOT) } ?: emptyList()

            if ((cUidClean.isNotBlank() && uSubs.contains(cUidClean)) ||
                (cNameClean.isNotBlank() && uSubs.contains(cNameClean))) {
                uniqueSubs.add(uUid)
            }
        }

        val currentAuthUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "current_device_user"
        if ((cUidClean.isNotBlank() && subscribedSet.contains(cUidClean)) ||
            (cNameClean.isNotBlank() && subscribedSet.contains(cNameClean))) {
            uniqueSubs.add(currentAuthUid)
        }

        val baseFromDoc = ((matchedUser?.get("subscribersCount") as? Number)
            ?: (matchedUser?.get("subsCount") as? Number)
            ?: (matchedUser?.get("followersCount") as? Number))?.toInt() ?: 0

        return maxOf(uniqueSubs.size, baseFromDoc)
    }

    // Listado filtrado ESTRICTAMENTE a usuarios que poseen rol relevante (creador, streamer, moderador, admin) Y que poseen AL MENOS 1 build creada
    val allCreators = remember(registeredUsers, customBuilds, subscribedSet) {
        val list = mutableListOf<CreatorListItem>()
        val seenNames = mutableSetOf<String>()

        for (u in registeredUsers) {
            val rawRole = (u["role"] as? String)
                ?: (u["userRole"] as? String)
                ?: (u["roleId"] as? String)
                ?: ""
            val normalizedRole = rawRole.trim().lowercase(Locale.ROOT)
            val secRole = ((u["secondaryRole"] as? String) ?: (u["secondary_role"] as? String) ?: "").trim().lowercase(Locale.ROOT)

            val isEligibleRole = normalizedRole in listOf("creador", "creador_vip", "creator", "creator_vip", "streamer", "moderador", "moderator", "admin") ||
                secRole in listOf("creador", "creador_vip", "creator", "creator_vip", "streamer", "moderador", "moderator") ||
                (u["isCreator"] as? Boolean) == true ||
                (u["creator"] as? Boolean) == true ||
                (u["isVipCreator"] as? Boolean) == true

            if (!isEligibleRole) continue

            val rawName = (u["name"] as? String)
                ?: (u["userName"] as? String)
                ?: (u["username"] as? String)
                ?: ""
            val cleanName = rawName.trim()
            val uUid = u["uid"] as? String ?: ""

            if (cleanName.isNotBlank() && seenNames.add(cleanName.lowercase(Locale.ROOT))) {
                val count = customBuilds.count { 
                    it.creatorName.trim().equals(cleanName, ignoreCase = true) || 
                    (uUid.isNotBlank() && it.creatorUserId == uUid) 
                }

                // REGLA ESTRICTA: Solo visualizar si tiene al menos 1 build creada
                if (count <= 0) continue

                val avatarId = u["avatarId"] as? String ?: "default_poro"
                val rankBorder = u["rankBorder"] as? String ?: "NONE"
                val secondaryRole = (u["secondaryRole"] as? String) ?: (u["secondary_role"] as? String) ?: "none"
                val equippedFrame = (u["activeFramePreference"] as? String) ?: (u["equippedFrame"] as? String) ?: "AUTO"
                val isAdmin = (u["isAdmin"] as? Boolean) == true || normalizedRole == "admin"
                val isVerified = (u["isVerified"] as? Boolean) == true ||
                    (u["verified"] as? Boolean) == true ||
                    isAdmin ||
                    normalizedRole in listOf("moderador", "moderator")
                val subsCount = countSubscribersForCreator(uUid, cleanName, u)

                list.add(
                    CreatorListItem(
                        uid = uUid,
                        name = cleanName,
                        avatarId = avatarId,
                        rankBorder = rankBorder,
                        secondaryRole = secondaryRole,
                        equippedFrame = equippedFrame,
                        isAdmin = isAdmin,
                        isVerified = isVerified,
                        buildsCount = count,
                        subscribersCount = subsCount
                    )
                )
            }
        }

        // También agregar creadores presentes en customBuilds que no estuviesen en registeredUsers
        val userMapByName = registeredUsers.associateBy { (it["name"] as? String ?: "").trim().lowercase(Locale.ROOT) }
        val userMapByUid = registeredUsers.associateBy { (it["uid"] as? String ?: "") }

        for (build in customBuilds) {
            val cName = build.creatorName.trim()
            if (cName.isNotBlank() && seenNames.add(cName.lowercase(Locale.ROOT))) {
                val cUid = build.creatorUserId
                val matchedUser = (if (cUid.isNotBlank()) userMapByUid[cUid] else null)
                    ?: userMapByName[cName.lowercase(Locale.ROOT)]

                val count = customBuilds.count { 
                    it.creatorName.trim().equals(cName, ignoreCase = true) || 
                    (cUid.isNotBlank() && it.creatorUserId == cUid) 
                }

                if (count > 0) {
                    val avatarId = (matchedUser?.get("avatarId") as? String) ?: build.creatorAvatarId ?: "default_poro"
                    val rankBorder = (matchedUser?.get("rankBorder") as? String) ?: build.creatorRankBorder ?: "NONE"
                    val secondaryRole = (matchedUser?.get("secondaryRole") as? String) ?: (matchedUser?.get("secondary_role") as? String) ?: "none"
                    val equippedFrame = (matchedUser?.get("activeFramePreference") as? String) ?: (matchedUser?.get("equippedFrame") as? String) ?: "AUTO"
                    val rawRole = (matchedUser?.get("role") as? String) ?: ""
                    val normalizedRole = rawRole.trim().lowercase(Locale.ROOT)
                    val isAdmin = build.creatorIsAdmin || normalizedRole == "admin" || (matchedUser?.get("isAdmin") as? Boolean) == true
                    val isVerified = (matchedUser?.get("isVerified") as? Boolean) == true || isAdmin || normalizedRole in listOf("moderador", "moderator")
                    val subsCount = countSubscribersForCreator(cUid, cName, matchedUser)

                    list.add(
                        CreatorListItem(
                            uid = cUid,
                            name = cName,
                            avatarId = avatarId,
                            rankBorder = rankBorder,
                            secondaryRole = secondaryRole,
                            equippedFrame = equippedFrame,
                            isAdmin = isAdmin,
                            isVerified = isVerified,
                            buildsCount = count,
                            subscribersCount = subsCount
                        )
                    )
                }
            }
        }

        list.sortedBy { it.name.lowercase(Locale.ROOT) }
    }

    val filteredCreators = remember(allCreators, userSearchQuery) {
        if (userSearchQuery.isBlank()) {
            allCreators
        } else {
            allCreators.filter { it.name.contains(userSearchQuery.trim(), ignoreCase = true) }
        }
    }

    // Cálculo dinámico del Top 3 de Creadores para el Podio (Solo creadores con builds creadas > 0)
    val podiumCreators = remember(customBuilds, registeredUsers, subscribedSet) {
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
            if (buildsCount <= 0) continue

            val score = (totalVotes * 10.0) + (avgRating * 20.0) + (buildsCount * 15.0)

            // Buscar datos de avatar y marco del usuario
            val firstRecord = builds.firstOrNull()
            val creatorUid = firstRecord?.creatorUserId ?: ""
            val matchedUser = (if (creatorUid.isNotBlank()) userMapByUid[creatorUid] else null)
                ?: userMapByName[creatorName.lowercase(Locale.ROOT)]

            val subsCount = countSubscribersForCreator(creatorUid, creatorName, matchedUser)

            val avatarId = (matchedUser?.get("avatarId") as? String)
                ?: firstRecord?.creatorAvatarId
                ?: "default_poro"

            val rankBorder = (matchedUser?.get("rankBorder") as? String)
                ?: firstRecord?.creatorRankBorder
                ?: "NONE"

            val secondaryRole = (matchedUser?.get("secondaryRole") as? String)
                ?: (matchedUser?.get("secondary_role") as? String)
                ?: "none"

            val equippedFrame = (matchedUser?.get("activeFramePreference") as? String)
                ?: (matchedUser?.get("equippedFrame") as? String)
                ?: "AUTO"

            val role = (matchedUser?.get("role") as? String) ?: "creador"
            val isAdmin = role == "admin" || (matchedUser?.get("isAdmin") as? Boolean) == true || (firstRecord?.creatorIsAdmin == true)

            rankingList.add(
                CreatorPodiumEntry(
                    userId = (matchedUser?.get("uid") as? String) ?: creatorUid,
                    name = creatorName,
                    avatarId = avatarId,
                    rankBorder = rankBorder,
                    secondaryRole = secondaryRole,
                    equippedFrame = equippedFrame,
                    isAdmin = isAdmin,
                    role = role,
                    buildsCount = buildsCount,
                    totalVotes = totalVotes,
                    averageRating = avgRating,
                    subscribersCount = subsCount,
                    score = score
                )
            )
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
                subscribersCount = 150,
                score = 300.0
            ),
            CreatorPodiumEntry(
                name = "Wild Rift Pro",
                avatarId = "avatar_kaisa",
                rankBorder = "GRANDMASTER",
                isAdmin = false,
                role = "creador",
                buildsCount = 4,
                totalVotes = 52,
                averageRating = 4.9,
                subscribersCount = 110,
                score = 220.0
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
                subscribersCount = 85,
                score = 160.0
            )
        )

        // Clasificación ESTRICTA por la cantidad de suscriptores (Subscribers Count)
        val sortedList = rankingList.sortedWith(
            compareByDescending<CreatorPodiumEntry> { it.subscribersCount }
                .thenByDescending { it.score }
                .thenByDescending { it.totalVotes }
        ).toMutableList()

        var fallbackIdx = 0
        while (sortedList.size < 3 && fallbackIdx < defaultLegends.size) {
            val fallback = defaultLegends[fallbackIdx]
            if (sortedList.none { it.name.equals(fallback.name, ignoreCase = true) }) {
                sortedList.add(fallback)
            }
            fallbackIdx++
        }

        // Re-ordenar por suscriptores tras agregar fallbacks
        sortedList.sortedWith(
            compareByDescending<CreatorPodiumEntry> { it.subscribersCount }
                .thenByDescending { it.score }
        ).take(3)
    }

    val filteredBuilds = remember(customBuilds, favorites, selectedFilter, selectedCreatorFilter) {
        val base = when (selectedFilter) {
            BuildsFilterTab.ALL, BuildsFilterTab.CREATORS -> customBuilds
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

    androidx.activity.compose.BackHandler {
        if (selectedBuildForDetail != null) {
            selectedBuildForDetail = null
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
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

            // Podio de Creadores (1er, 2do y 3er Lugar con Avatar, Nombre y Marco adaptado)
            if (podiumCreators.size >= 3) {
                CreatorPodiumCard(
                    first = podiumCreators[0],
                    second = podiumCreators[1],
                    third = podiumCreators[2],
                    selectedCreator = selectedCreatorFilter,
                    onSelectCreator = { creatorName ->
                        val entry = podiumCreators.find { it.name.equals(creatorName, ignoreCase = true) }
                        if (entry != null) {
                            val matchedCreatorItem = allCreators.find { it.name.equals(entry.name, ignoreCase = true) }
                                ?: CreatorListItem(
                                    uid = entry.userId,
                                    name = entry.name,
                                    avatarId = entry.avatarId ?: "default_poro",
                                    rankBorder = entry.rankBorder,
                                    secondaryRole = entry.secondaryRole,
                                    equippedFrame = entry.equippedFrame,
                                    isAdmin = entry.isAdmin,
                                    isVerified = true,
                                    buildsCount = entry.buildsCount
                                )
                            creatorToViewProfile = matchedCreatorItem
                        }
                    }
                )
            }

            // Action Button: Crear Build
            Button(
                onClick = { showBuildCreator = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = HextechGold),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = HextechDarkBg, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Crear Nueva Build Oficial", color = HextechDarkBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            // Filtros de categoría, creadores y creador activo
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
                    selected = selectedFilter == BuildsFilterTab.CREATORS,
                    onClick = {
                        selectedFilter = BuildsFilterTab.CREATORS
                        selectedCreatorFilter = null
                    },
                    label = { Text("Creadores (${allCreators.size})") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (selectedFilter == BuildsFilterTab.CREATORS) HextechCyan else TextSecondary
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = HextechCyan.copy(alpha = 0.25f),
                        selectedLabelColor = HextechCyan
                    ),
                    border = if (selectedFilter == BuildsFilterTab.CREATORS) BorderStroke(1.dp, HextechCyan) else null
                )

                FilterChip(
                    selected = selectedFilter == BuildsFilterTab.FAVORITES,
                    onClick = {
                        selectedFilter = BuildsFilterTab.FAVORITES
                        selectedCreatorFilter = null
                    },
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
                        label = { Text("Creador: $selectedCreatorFilter ") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HextechCyan.copy(alpha = 0.25f),
                            selectedLabelColor = HextechCyan
                        ),
                        border = BorderStroke(1.dp, HextechCyan)
                    )
                }
            }

            // Contenido dinámico según la pestaña seleccionada
            if (selectedFilter == BuildsFilterTab.CREATORS) {
                // VISTA: LISTADO EXCLUSIVO DE CREADORES (Nombre de invocador, avatar con marco y estado de verificación)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = userSearchQuery,
                        onValueChange = { userSearchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar creador por nombre...", color = TextSecondary, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Buscar", tint = HextechGold, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (userSearchQuery.isNotBlank()) {
                                IconButton(onClick = { userSearchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HextechGold,
                            unfocusedBorderColor = HextechCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = HextechGold
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    if (filteredCreators.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (userSearchQuery.isNotBlank()) "No se encontraron creadores para \"$userSearchQuery\"" else "No hay usuarios con rol de creador registrados.",
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
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(filteredCreators, key = { it.name }) { creator ->
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = HextechDarkBg),
                                    border = BorderStroke(
                                        1.dp,
                                        if (selectedCreatorFilter.equals(creator.name, ignoreCase = true)) HextechCyan else HextechCardBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            creatorToViewProfile = creator
                                        }
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
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            // Avatar con su respectivo marco
                                            Box(
                                                modifier = Modifier.size(46.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                UserAvatarView(
                                                    avatarId = creator.avatarId,
                                                    size = 42.dp,
                                                    fallbackInitial = creator.name.take(1).uppercase(Locale.ROOT),
                                                    rankBorder = creator.rankBorder,
                                                    secondaryRole = creator.secondaryRole,
                                                    equippedFrame = creator.equippedFrame,
                                                    isAdmin = creator.isAdmin,
                                                    fitFrameToSize = true
                                                )
                                            }

                                            // Nombre de invocador y Estado de Verificación
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Text(
                                                    text = creator.name,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.5.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    // Indicador si está verificado o no
                                                    if (creator.isVerified) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = HextechCyan.copy(alpha = 0.15f),
                                                            border = BorderStroke(0.6.dp, HextechCyan.copy(alpha = 0.6f))
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                            ) {
                                                                Icon(
                                                                    Icons.Filled.Verified,
                                                                    contentDescription = "Verificado",
                                                                    tint = HextechCyan,
                                                                    modifier = Modifier.size(11.dp)
                                                                )
                                                                Text(
                                                                    text = "Verificado",
                                                                    color = HextechCyan,
                                                                    fontSize = 9.5.sp,
                                                                    fontWeight = FontWeight.SemiBold
                                                                )
                                                            }
                                                        }
                                                    } else {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = HextechCardBorder.copy(alpha = 0.35f),
                                                            border = BorderStroke(0.6.dp, HextechCardBorder)
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                            ) {
                                                                Icon(
                                                                    Icons.Default.Close,
                                                                    contentDescription = "No Verificado",
                                                                    tint = TextSecondary,
                                                                    modifier = Modifier.size(10.dp)
                                                                )
                                                                Text(
                                                                    text = "No Verificado",
                                                                    color = TextSecondary,
                                                                    fontSize = 9.5.sp,
                                                                    fontWeight = FontWeight.Normal
                                                                )
                                                            }
                                                        }
                                                    }

                                                    if (creator.buildsCount > 0) {
                                                        Text(
                                                            text = "• ${creator.buildsCount} builds",
                                                            color = HextechGold,
                                                            fontSize = 10.5.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = HextechGold.copy(alpha = 0.12f),
                                            border = BorderStroke(0.6.dp, HextechGold.copy(alpha = 0.35f)),
                                            modifier = Modifier.clickable {
                                                creatorToViewProfile = creator
                                            }
                                        ) {
                                            Text(
                                                text = "Ver Perfil",
                                                color = HextechGold,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // VISTA: LISTA DE BUILDS (Todas / Favoritos / Creador seleccionado)
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
                                                        text = "Creador: ${record.creatorName}",
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

        if (creatorToViewProfile != null) {
            val creator = creatorToViewProfile!!
            val creatorBuilds = remember(customBuilds, creator.name) {
                customBuilds.filter { it.creatorName.trim().equals(creator.name.trim(), ignoreCase = true) }
            }
            CreatorProfileDialog(
                creatorName = creator.name,
                creatorUid = creator.uid,
                avatarId = creator.avatarId,
                rankBorder = creator.rankBorder,
                secondaryRole = creator.secondaryRole,
                equippedFrame = creator.equippedFrame,
                isAdmin = creator.isAdmin,
                isVerified = creator.isVerified,
                creatorBuilds = creatorBuilds,
                onSelectBuild = { build ->
                    selectedBuildForDetail = build
                },
                onDismiss = {
                    creatorToViewProfile = null
                }
            )
        }
    }
}

/**
 * Podio oficial para los 3 mejores creadores de la comunidad.
 * Visualiza el 1er lugar (centro, más alto), 2do lugar (izquierda) y 3er lugar (derecha)
 * con espaciado vertical reservado para que los marcos no se sobrepongan a las insignias de lugar.
 */
@Composable
fun CreatorPodiumCard(
    first: CreatorPodiumEntry,
    second: CreatorPodiumEntry,
    third: CreatorPodiumEntry,
    selectedCreator: String?,
    onSelectCreator: (String) -> Unit
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
                .padding(horizontal = 10.dp, vertical = 12.dp),
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
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2DO LUGAR (Izquierda)
                PodiumColumn(
                    entry = second,
                    rank = 2,
                    rankBadgeText = "2° Lugar",
                    badgeColor = Color(0xFFE2E8F0),
                    badgeBgColor = Color(0xFF334155).copy(alpha = 0.7f),
                    avatarSize = 40.dp,
                    avatarContainerHeight = 78.dp,
                    pedestalHeight = 52.dp,
                    pedestalBrush = Brush.verticalGradient(
                        listOf(Color(0xFF475569), Color(0xFF1E293B))
                    ),
                    pedestalBorderColor = Color(0xFF94A3B8),
                    numeralColor = Color(0xFFCBD5E1),
                    isSelected = selectedCreator.equals(second.name, ignoreCase = true),
                    onClick = { onSelectCreator(second.name) },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // 1ER LUGAR (Centro - Elevado y Destacado)
                PodiumColumn(
                    entry = first,
                    rank = 1,
                    rankBadgeText = "1° Lugar",
                    badgeColor = HextechGold,
                    badgeBgColor = HextechGold.copy(alpha = 0.22f),
                    avatarSize = 48.dp,
                    avatarContainerHeight = 90.dp,
                    pedestalHeight = 74.dp,
                    pedestalBrush = Brush.verticalGradient(
                        listOf(HextechGold.copy(alpha = 0.55f), Color(0xFF854D0E), HextechDarkBg)
                    ),
                    pedestalBorderColor = HextechGold,
                    numeralColor = HextechGold,
                    isSelected = selectedCreator.equals(first.name, ignoreCase = true),
                    onClick = { onSelectCreator(first.name) },
                    modifier = Modifier.weight(1.18f)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // 3ER LUGAR (Derecha)
                PodiumColumn(
                    entry = third,
                    rank = 3,
                    rankBadgeText = "3° Lugar",
                    badgeColor = Color(0xFFFDBA74),
                    badgeBgColor = Color(0xFF7C2D12).copy(alpha = 0.45f),
                    avatarSize = 38.dp,
                    avatarContainerHeight = 76.dp,
                    pedestalHeight = 40.dp,
                    pedestalBrush = Brush.verticalGradient(
                        listOf(Color(0xFF78350F), Color(0xFF451A03))
                    ),
                    pedestalBorderColor = Color(0xFFB45309),
                    numeralColor = Color(0xFFFDBA74),
                    isSelected = selectedCreator.equals(third.name, ignoreCase = true),
                    onClick = { onSelectCreator(third.name) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Columna individual de cada posición en el podio.
 * Mantiene una zona reservada y holgada para el avatar con su marco,
 * evitando colisiones con la insignia superior o el nombre inferior.
 */
@Composable
private fun PodiumColumn(
    entry: CreatorPodiumEntry,
    rank: Int,
    rankBadgeText: String,
    badgeColor: Color,
    badgeBgColor: Color,
    avatarSize: androidx.compose.ui.unit.Dp,
    avatarContainerHeight: androidx.compose.ui.unit.Dp,
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
        // Insignia del lugar (Siempre visible y separada por encima del marco)
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = badgeBgColor,
            border = BorderStroke(0.8.dp, badgeColor.copy(alpha = 0.8f))
        ) {
            Text(
                text = rankBadgeText,
                color = badgeColor,
                fontSize = if (rank == 1) 9.5.sp else 8.5.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Contenedor con altura reservada para Avatar + Marco sin colisiones
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(avatarContainerHeight),
            contentAlignment = Alignment.Center
        ) {
            UserAvatarView(
                avatarId = entry.avatarId,
                size = avatarSize,
                fallbackInitial = entry.name.take(1).uppercase(Locale.ROOT),
                rankBorder = entry.rankBorder,
                secondaryRole = entry.secondaryRole,
                equippedFrame = entry.equippedFrame,
                isAdmin = entry.isAdmin,
                fitFrameToSize = true
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Nombre de usuario / Invocador
        Text(
            text = entry.name,
            color = if (rank == 1) HextechGold else Color.White,
            fontWeight = if (rank == 1) FontWeight.ExtraBold else FontWeight.Bold,
            fontSize = if (rank == 1) 12.sp else 10.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        // Resumen de estadísticas del creador (Suscriptores principales)
        Text(
            text = "${entry.subscribersCount} subs • ${entry.buildsCount} builds",
            color = if (rank == 1) HextechGoldLight else TextSecondary,
            fontSize = if (rank == 1) 9.sp else 8.sp,
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
