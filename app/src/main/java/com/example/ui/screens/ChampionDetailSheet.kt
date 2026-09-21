package com.example.ui.screens

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import com.example.utils.parseHtmlColorToAnnotatedString
import com.example.data.WildRiftItemsData
import com.example.model.WildRiftItem
import com.example.ui.theme.HextechGoldLight
import com.example.ui.theme.TextSecondary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.offset
import com.example.ui.theme.HextechDarkBg
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.FavoriteChampionsManager
import com.example.data.SituationalItemAdvisor
import com.example.data.SynergyAdvisor
import com.example.data.SynergyTeammate
import com.example.data.WildRiftRepository
import com.example.model.Champion
import com.example.util.SubscriptionManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.model.LaneRole
import com.example.util.LocalLanguage
import com.example.ui.components.AppAssetImage
import com.example.ui.components.ChampionAvatar
import com.example.ui.components.FormattedWildRiftText
import com.example.ui.components.SparklineTrendGraph
import com.example.ui.theme.AllyBlue
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechGoldLight
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.HextechSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TierSPlusColor
import com.example.util.ChampionRoleAdapter
import com.example.util.CoachingGenerator
import com.example.util.tr

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChampionDetailSheet(
    isOverlay: Boolean = false,
    champion: Champion?,
    onDismiss: () -> Unit
) {
    val isPremium by SubscriptionManager.isPremium.collectAsState()
    if (champion == null) return

    val context = LocalContext.current
    val favorites by FavoriteChampionsManager.favoritesFlow.collectAsStateWithLifecycle()
    val isFavorite = favorites.contains(champion.id.lowercase())

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // Solo las líneas Main y Flex en las que realmente se juega el campeón
    val availableRoles = remember(champion.id) {
        (listOf(champion.primaryRole) + champion.secondaryRoles).distinct()
    }
    var selectedRole by remember(champion.id) { 
        mutableStateOf(champion.primaryRole) 
    }
    
    LaunchedEffect(champion.id) {
        if (selectedRole !in availableRoles) {
            selectedRole = champion.primaryRole
        }
    }

    var matchupExplanationTarget by remember { mutableStateOf<String?>(null) }
    var matchupExplanationType by remember { mutableStateOf<String?>(null) }
    var selectedSituationalItem by remember { mutableStateOf<String?>(null) }
    var itemForDetail by remember { mutableStateOf<com.example.model.WildRiftItem?>(null) }
    var runeForDetail by remember { mutableStateOf<com.example.model.RuneItem?>(null) }
    var spellForDetail by remember { mutableStateOf<com.example.model.SummonerSpellItem?>(null) }
    var selectedBuildOptionIndex by remember(champion.id, selectedRole) { mutableStateOf(0) }

    val currentLang = LocalLanguage.current

    androidx.activity.compose.BackHandler {
        if (itemForDetail != null) {
            itemForDetail = null
        } else if (runeForDetail != null) {
            runeForDetail = null
        } else if (spellForDetail != null) {
            spellForDetail = null
        } else if (matchupExplanationTarget != null) {
            matchupExplanationTarget = null
            matchupExplanationType = null
        } else if (selectedSituationalItem != null) {
            selectedSituationalItem = null
        } else {
            onDismiss()
        }
    }

    // Perfil dinámico de estadísticas, build, runas y counters adaptados a la línea elegida
    val roleProfile = remember(champion.id, selectedRole) {
        ChampionRoleAdapter.getProfile(champion, selectedRole)
    }

    // Perfil de Sinergias del Meta y Compañeros complementarios
    val synergyProfile = remember(champion.id, selectedRole, currentLang) {
        SynergyAdvisor.getSynergyProfile(champion, selectedRole, currentLang)
    }

    val isCompact = isOverlay
    val avatarSize = if (isCompact) 36.dp else 68.dp
    val titleFontSize = if (isCompact) 13.5.sp else 22.sp
    val sectionTitleFontSize = if (isCompact) 11.sp else 15.sp
    val cardPadding = if (isCompact) 6.dp else 12.dp
    val itemBoxSize = if (isCompact) 28.dp else 46.dp
    val itemImageSize = if (isCompact) 24.dp else 42.dp
    val subItemSize = if (isCompact) 22.dp else 38.dp

    val dialogContent = @Composable {
        androidx.compose.material3.Card(
            modifier = if (isOverlay) Modifier.fillMaxWidth().heightIn(max = 480.dp) else Modifier.fillMaxWidth().fillMaxHeight(0.9f).padding(top = 16.dp),
            shape = if (isOverlay) androidx.compose.foundation.shape.RoundedCornerShape(14.dp) else androidx.compose.foundation.shape.RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = if (isOverlay) HextechDarkBg else HextechSurfaceVariant)
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isOverlay) 8.dp else 20.dp, vertical = if (isOverlay) 6.dp else 0.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (isOverlay) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(HextechSurface)
                            .clickable { onDismiss() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = tr("Volver"),
                            tint = HextechCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tr("Volver"),
                            color = HextechCyan,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = tr("Build y Tácticas"),
                        color = HextechGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = tr("Cerrar"), tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Header with Avatar, Name, Tier and Winrate
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ChampionAvatar(champion = champion, size = avatarSize)
                    Spacer(modifier = Modifier.width(if (isCompact) 8.dp else 14.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = champion.name,
                                color = TextPrimary,
                                fontSize = titleFontSize,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(if (isCompact) 4.dp else 6.dp))
                                    .background(TierSPlusColor)
                                    .padding(horizontal = if (isCompact) 4.dp else 6.dp, vertical = if (isCompact) 1.dp else 2.dp)
                            ) {
                                Text(
                                    text = "Tier ${roleProfile.tier}",
                                    color = Color.Black,
                                    fontSize = if (isCompact) 8.5.sp else 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        if (champion.title.isNotBlank()) {
                            Text(
                                text = tr(champion.title),
                                color = TextPrimary,
                                fontSize = if (isCompact) 9.5.sp else 12.sp
                            )
                        }
                        Text(
                            text = "${tr(selectedRole.displayName)}${if (selectedRole != champion.primaryRole) " (Flex)" else ""} • ${tr(champion.damageType.displayName)}",
                            color = HextechCyan,
                            fontSize = if (isCompact) 9.5.sp else 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isPremium by com.example.util.SubscriptionManager.isPremium.collectAsStateWithLifecycle()
                    Box(contentAlignment = Alignment.TopEnd) {
                        IconButton(
                            onClick = { 
                                if (isPremium) {
                                    FavoriteChampionsManager.toggleFavorite(context, champion.id) 
                                } else {
                                    android.widget.Toast.makeText(context, "Requiere suscripción Premium", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("detail_fav_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = if (isFavorite) tr("Quitar de Favoritos") else tr("Marcar como Favorito"),
                                tint = if (isFavorite) HextechGold else TextMuted.copy(alpha = 0.4f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        if (!isPremium) {
                            Box(
                                modifier = Modifier
                                    .offset(x = (-2).dp, y = 4.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(HextechGold)
                                    .padding(horizontal = 3.dp, vertical = 0.5.dp)
                            ) {
                                Text("PRO", color = HextechDarkBg, fontSize = 6.5.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = tr("Cerrar"), tint = TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // SELECTOR DE LÍNEA / ROL (TOP, JUNGLA, MID, ADC, SUPPORTE)
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = HextechSurface),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = tr("Cambiar Línea / Rol Activo:"),
                            color = HextechGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tr(selectedRole.displayName),
                            color = HextechCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        availableRoles.forEach { role ->
                            val isSelected = selectedRole == role
                            val isPrimary = champion.primaryRole == role
                            val isSecondary = champion.secondaryRoles.contains(role)

                            val roleLabel = when (role) {
                                LaneRole.TOP -> tr("TOP")
                                LaneRole.JUNGLE -> tr("JUNGLA")
                                LaneRole.MID -> tr("MID")
                                LaneRole.ADC -> tr("DÚO")
                                LaneRole.SUPPORT -> tr("SOPORTE")
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isSelected -> HextechGold.copy(alpha = 0.28f)
                                            isPrimary -> HextechSurfaceVariant
                                            else -> HextechSurfaceVariant.copy(alpha = 0.7f)
                                        }
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) HextechGold else HextechCardBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { 
                                        selectedRole = role 
                                        selectedBuildOptionIndex = 0
                                    }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = roleLabel,
                                        color = if (isSelected) HextechGold else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    if (isPrimary) {
                                        Text("Main", color = HextechCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text("Flex", color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }

                    if (selectedRole != champion.primaryRole) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(HextechGold.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "⭐ ${tr("Estadísticas, hechizos, runas y build adaptadas a")} ${tr(selectedRole.displayName)}.",
                                color = TextPrimary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // ESTADÍSTICAS ADAPTADAS A LA LÍNEA (CON COMPARATIVA VS. AYER)
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = HextechSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = HextechGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tr("Estadísticas del Meta Oficial"),
                                color = HextechGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SparklineTrendGraph(
                                winrate = roleProfile.winrate,
                                delta = roleProfile.winrateDelta,
                                modifier = Modifier.width(48.dp).height(20.dp)
                            )
                            Text(
                                text = tr("Tendencia en Vivo"),
                                color = HextechCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Winrate + Delta
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(tr("Tasa de Victoria"), color = TextMuted, fontSize = 11.sp)
                            Text("${String.format(java.util.Locale.US, "%.2f", roleProfile.winrate)}%", color = HextechGold, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            val winDelta = roleProfile.winrateDelta
                            val formattedWinDelta = String.format(java.util.Locale.US, "%.2f", winDelta)
                            val winDeltaText = if (winDelta >= 0) "+${formattedWinDelta}%" else "${formattedWinDelta}%"
                            val winDeltaColor = if (winDelta >= 0) Color(0xFF4CAF50) else DangerRed
                            Text(
                                text = if (winDelta >= 0) "▲ $winDeltaText" else "▼ $winDeltaText",
                                color = winDeltaColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Pick Rate + Delta
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(tr("Tasa de Selección"), color = TextMuted, fontSize = 11.sp)
                            Text("${String.format(java.util.Locale.US, "%.2f", roleProfile.pickRate)}%", color = HextechCyan, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            val pickDelta = roleProfile.pickRateDelta
                            val formattedPickDelta = String.format(java.util.Locale.US, "%.2f", pickDelta)
                            val pickDeltaText = if (pickDelta >= 0) "+${formattedPickDelta}%" else "${formattedPickDelta}%"
                            val pickDeltaColor = if (pickDelta >= 0) Color(0xFF29B6F6) else Color(0xFFFFA726)
                            Text(
                                text = if (pickDelta >= 0) "▲ $pickDeltaText" else "▼ $pickDeltaText",
                                color = pickDeltaColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Ban Rate + Delta
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(tr("Tasa de Bloqueo"), color = TextMuted, fontSize = 11.sp)
                            Text("${String.format(java.util.Locale.US, "%.2f", roleProfile.banRate)}%", color = DangerRed, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            val banDelta = roleProfile.banRateDelta
                            val formattedBanDelta = String.format(java.util.Locale.US, "%.2f", banDelta)
                            val banDeltaText = if (banDelta >= 0) "+${formattedBanDelta}%" else "${formattedBanDelta}%"
                            val banDeltaColor = if (banDelta >= 0) DangerRed else Color(0xFF4CAF50)
                            Text(
                                text = if (banDelta >= 0) "▲ $banDeltaText" else "▼ $banDeltaText",
                                color = banDeltaColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Resumen Táctico
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = HextechSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = HextechGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(tr("Análisis Táctico en Wild Rift"), color = HextechGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    val currentLang = com.example.util.LocalLanguage.current
                    val fullAnalysis = remember(champion.id, currentLang, selectedRole) {
                        CoachingGenerator.generateTacticalAnalysis(champion, selectedRole, currentLang)
                    }
                    FormattedWildRiftText(
                        text = fullAnalysis,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // HABILIDADES DEL CAMPEÓN (CON IMÁGENES)
            // ==========================================
            if (champion.skills.isNotEmpty()) {
                Text(
                    text = tr("Habilidades de") + " ${champion.name}",
                    color = HextechGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    champion.skills.forEach { skill ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = HextechSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                AppAssetImage(
                                    url = skill.iconUrl,
                                    contentDescription = skill.name,
                                    fallbackText = skill.slot,
                                    modifier = Modifier.size(42.dp),
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
                                        val slotTranslation = when {
                                            skill.slotName.contains("Pasiva", true) || skill.slot.equals("P", true) || skill.slot.equals("Passive", true) -> tr("Pasiva:")
                                            skill.slotName.contains("Habilidad 1", true) || skill.slot == "1" || skill.slot.equals("Q", true) -> tr("Habilidad") + " 1:"
                                            skill.slotName.contains("Habilidad 2", true) || skill.slot == "2" || skill.slot.equals("W", true) -> tr("Habilidad") + " 2:"
                                            skill.slotName.contains("Habilidad 3", true) || skill.slot == "3" || skill.slot.equals("E", true) -> tr("Habilidad") + " 3:"
                                            skill.slotName.contains("Definitiva", true) || skill.slot == "4" || skill.slot.equals("R", true) -> tr("Definitiva:")
                                            else -> if (skill.slotName.isNotBlank()) skill.slotName else if (skill.slot.isNotBlank()) "Habilidad ${skill.slot}:" else ""
                                        }
                                        Text(
                                            text = if (slotTranslation.isNotBlank()) "$slotTranslation ${skill.name}" else skill.name,
                                            color = TextPrimary,
                                            fontSize = if (isCompact) 11.sp else 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (skill.cooldown.isNotBlank()) {
                                            Text(
                                                text = skill.cooldown,
                                                color = HextechCyan,
                                                fontSize = if (isCompact) 9.sp else 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    FormattedWildRiftText(
                                        text = tr(skill.description),
                                        color = TextPrimary.copy(alpha = 0.9f),
                                        fontSize = if (isCompact) 9.5.sp else 12.sp,
                                        lineHeight = if (isCompact) 13.sp else 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ==========================================
            // BUILDS TÁCTICAS (4 OPCIONES SEGÚN META Y CRITERIO COACH)
            // ==========================================
            val customBuilds by com.example.data.local.CustomChampionBuildsManager.customBuilds.collectAsStateWithLifecycle()
            val championCustomBuilds = remember(customBuilds, champion.id) {
                customBuilds.filter { it.championId.equals(champion.id, ignoreCase = true) }
            }

            val baseBuildOptions = roleProfile.buildOptions.ifEmpty {
                // Fallback default options
                listOf(
                    com.example.util.ChampionBuildOption(
                        optionNumber = 1,
                        title = "Opción 1: Meta Core Estándar",
                        subtitle = "Meta Pro • Global",
                        source = "Meta Pro / Global",
                        badge = "ESTÁNDAR",
                        tacticalReason = "Build estándar de referencia oficial con mayor tasa de victoria equilibrada en el meta actual de Wild Rift.",
                        items = if (roleProfile.build8Items.isNotEmpty()) roleProfile.build8Items else (roleProfile.coreItems + roleProfile.situationalItems).take(8),
                        bootBase = roleProfile.bootBase.ifBlank { "Botas blindadas" },
                        bootUpgrade = roleProfile.bootUpgrade.ifBlank { "Avance blindado" },
                        runes = roleProfile.runesOption1.ifEmpty { listOf(roleProfile.recommendedRunes) },
                        spells = roleProfile.recommendedSpells,
                        spellsIcons = roleProfile.spellsIcons
                    )
                )
            }

            val buildOptionsList = remember(baseBuildOptions, championCustomBuilds) {
                val customOptions = championCustomBuilds.mapIndexed { idx, rec ->
                    com.example.util.ChampionBuildOption(
                        optionNumber = 100 + idx,
                        title = rec.buildTitle,
                        subtitle = "Creador: ${rec.creatorName}",
                        source = "Catálogo Creador • ${rec.creatorName}",
                        badge = "CREADOR",
                        tacticalReason = "Build personalizada creada y verificada por el creador oficial ${rec.creatorName}." +
                            if (rec.coreItemsWithDesc.isNotEmpty()) "\n\nObjetos Core:\n" + rec.coreItemsWithDesc.joinToString("\n") { "• ${it.itemName}: ${it.description}" } else "",
                        items = rec.coreItemsWithDesc.map { it.itemName }.ifEmpty { rec.coreItems },
                        bootBase = "Botas estándar",
                        bootUpgrade = "Encantamiento adaptativo",
                        situationalItems = rec.situationalItemsWithDesc.map { it.itemName }.ifEmpty { rec.situationalItems },
                        runes = rec.coreRunes.map { it.runeName }.ifEmpty { listOf(rec.runes) },
                        spells = rec.coreSpells.map { it.spellName }.ifEmpty { rec.spells },
                        spellsIcons = rec.coreSpells.map { it.iconUrl },
                        coreItemsWithDesc = rec.coreItemsWithDesc,
                        situationalItemsWithDesc = rec.situationalItemsWithDesc,
                        coreRunes = rec.coreRunes,
                        situationalRunes = rec.situationalRunes,
                        coreSpells = rec.coreSpells,
                        situationalSpells = rec.situationalSpells,
                        gameplayVideoUri = rec.gameplayVideoUri
                    )
                }
                customOptions + baseBuildOptions
            }

            val activeOption = buildOptionsList.getOrNull(selectedBuildOptionIndex.coerceIn(0, buildOptionsList.size - 1))
                ?: buildOptionsList.first()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${tr("Builds tácticas")} • ${selectedRole.shortName}",
                    color = HextechGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(HextechCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "↔ " + tr("Desliza opciones"),
                        color = HextechCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            // 4-Option Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Los títulos se extraerán directamente de opt.title

                buildOptionsList.forEachIndexed { idx, opt ->
                    val isSelected = selectedBuildOptionIndex == idx
                    val rawTitle = opt.title.replace(Regex("^Opción \\d: "), "")
                    val label = "${idx + 1}. $rawTitle"

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) HextechGold.copy(alpha = 0.2f) else HextechSurface
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) HextechGold else HextechCardBorder,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedBuildOptionIndex = idx }
                            .padding(horizontal = if (isCompact) 8.dp else 12.dp, vertical = if (isCompact) 4.dp else 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = label,
                                color = if (isSelected) HextechGold else TextMuted,
                                fontSize = if (isCompact) 10.sp else 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (isCompact) 6.dp else 10.dp))

            // Main Card of Active Option
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HextechSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
            ) {
                Column(modifier = Modifier.padding(if (isCompact) 8.dp else 12.dp)) {
                    // Header with Title, Badge and Source
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeOption.title,
                                color = HextechGold,
                                fontSize = if (isCompact) 11.5.sp else 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            val shareBuild = {
                                val shareText = buildString {
                                    appendLine("🛡️ Build: ${activeOption.title} para ${champion.name}")
                                    appendLine("👤 Rol: ${selectedRole.name}")
                                    appendLine("⚔️ Core: ${activeOption.items.joinToString(", ")}")
                                    if (activeOption.situationalItems.isNotEmpty()) appendLine("🔄 Situacionales: ${activeOption.situationalItems.joinToString(", ")}")
                                    appendLine("💎 Runas: ${activeOption.runes.joinToString(", ")}")
                                    appendLine("🔥 ¡Comparte desde Coach App!")
                                }
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "Compartir Build"))
                            }
                            IconButton(onClick = shareBuild, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Share, contentDescription = "Compartir", tint = HextechCyan, modifier = Modifier.size(16.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (activeOption.optionNumber) {
                                            2 -> Color(0xFFE53935).copy(alpha = 0.2f)
                                            3 -> Color(0xFFFB8C00).copy(alpha = 0.2f)
                                            4 -> Color(0xFF8E24AA).copy(alpha = 0.2f)
                                            else -> HextechCyan.copy(alpha = 0.2f)
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        when (activeOption.optionNumber) {
                                            2 -> Color(0xFFE53935)
                                            3 -> Color(0xFFFB8C00)
                                            4 -> Color(0xFF8E24AA)
                                            else -> HextechCyan
                                        },
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = activeOption.badge,
                                    color = when (activeOption.optionNumber) {
                                        2 -> Color(0xFFFF5252)
                                        3 -> Color(0xFFFFB74D)
                                        4 -> Color(0xFFCE93D8)
                                        else -> HextechCyan
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tactical Reason ("¿Por qué y contra quién?")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(HextechSurfaceVariant.copy(alpha = 0.7f))
                            .border(1.dp, HextechCardBorder.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(if (isCompact) 6.dp else 10.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🎯 OBJETIVO TÁCTICO & CUÁNDO USAR",
                                    color = HextechGold,
                                    fontSize = if (isCompact) 9.5.sp else 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(if (isCompact) 2.dp else 4.dp))
                            Text(
                                text = activeOption.tacticalReason,
                                color = TextPrimary,
                                fontSize = if (isCompact) 9.5.sp else 11.5.sp,
                                lineHeight = if (isCompact) 13.sp else 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(if (isCompact) 6.dp else 12.dp))

                    // Items List
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr("Objetos de la Build"),
                            color = HextechCyan,
                            fontSize = if (isCompact) 9.5.sp else 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "↔ " + tr("Desliza objetos"),
                            color = TextMuted,
                            fontSize = if (isCompact) 8.sp else 9.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        activeOption.items.forEach { rawName ->
                            val dbItem = WildRiftItemsData.getItemByName(rawName)
                                ?: com.example.data.WildRiftRepository.items.find {
                                    it.name.equals(rawName, ignoreCase = true) || it.nameEn.equals(rawName, ignoreCase = true)
                                }
                            val iconUrl = dbItem?.iconUrl ?: WildRiftItemsData.getItemIconByName(rawName)
                            val itemName = dbItem?.name?.let { tr(it) } ?: tr(rawName)
                            val isResolved = iconUrl.isNotBlank() && iconUrl.startsWith("http")
                            val finalBorderColor = if (!isResolved) com.example.ui.theme.DangerRed else HextechGold

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        if (dbItem != null) {
                                            itemForDetail = dbItem
                                        } else {
                                            selectedSituationalItem = rawName
                                        }
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(itemBoxSize)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(HextechSurfaceVariant)
                                        .border(
                                            width = 1.5.dp,
                                            color = finalBorderColor,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    AppAssetImage(
                                        url = iconUrl,
                                        contentDescription = itemName,
                                        fallbackText = itemName,
                                        modifier = Modifier.size(itemImageSize),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (activeOption.situationalItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(HextechCardBorder.copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = tr("Objetos Situacionales:"),
                                color = HextechGold,
                                fontSize = if (isCompact) 10.sp else 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "↔ " + tr("Desliza"),
                                color = HextechCyan,
                                fontSize = if (isCompact) 8.sp else 9.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val sitBoxSize = if (isCompact) 26.dp else 38.dp
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            activeOption.situationalItems.forEach { sitItemName ->
                                val dbSitItem = com.example.data.WildRiftRepository.items.find {
                                    it.name.equals(sitItemName, ignoreCase = true) ||
                                    sitItemName.contains(it.name, ignoreCase = true) ||
                                    it.name.contains(sitItemName, ignoreCase = true)
                                }
                                val sitIcon = dbSitItem?.iconUrl ?: com.example.data.WildRiftItemsData.getItemIconByName(sitItemName)
                                Box(
                                    modifier = Modifier
                                        .size(sitBoxSize)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(HextechSurfaceVariant)
                                        .border(1.dp, HextechGoldLight.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            if (dbSitItem != null) {
                                                itemForDetail = dbSitItem
                                            } else {
                                                itemForDetail = com.example.model.WildRiftItem(
                                                    id = sitItemName.lowercase().replace(" ", "_"),
                                                    name = sitItemName,
                                                    nameEn = sitItemName,
                                                    category = "Objeto Situacional",
                                                    goldCost = 3000,
                                                    stats = "Objeto adaptativo para el meta actual.",
                                                    statsEn = "Adaptive meta situational item.",
                                                    passive = "Recomendado como reemplazo táctico según la composición rival.",
                                                    passiveEn = "Recommended tactical swap depending on enemy composition.",
                                                    coachTip = "Elige este objeto situacionalmente para contrarrestar curaciones, escudos o daño excesivo.",
                                                    coachTipEn = "Pick this situational item to counter healing, shields or burst.",
                                                    iconUrl = sitIcon
                                                )
                                            }
                                        }
                                ) {
                                    AppAssetImage(
                                        url = sitIcon,
                                        contentDescription = tr(sitItemName),
                                        fallbackText = tr(sitItemName),
                                        modifier = Modifier.fillMaxSize(),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // BOTAS Y MEJORAS + HECHIZOS (DOS COLUMNAS)
            // ==========================================
            var selectedBootBaseOverride by remember(activeOption) { mutableStateOf<String?>(null) }
            val currentBootBase = selectedBootBaseOverride ?: activeOption.bootBase.ifBlank { "Botas blindadas" }
            val currentBootUpgrade = if (activeOption.bootUpgrade.isNotBlank()) activeOption.bootUpgrade else ChampionRoleAdapter.getTier3BootUpgrade(currentBootBase)

            val bootBoxSize = if (isCompact) 26.dp else 38.dp
            val spellBoxSize = if (isCompact) 26.dp else 38.dp
            val runeKeySize = if (isCompact) 28.dp else 44.dp
            val runeSecSize = if (isCompact) 24.dp else 36.dp

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Botas y Mejoras de esta Opción
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = tr("Botas y Mejoras"),
                            color = HextechGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val dbBoot1 = com.example.data.WildRiftRepository.items.find { it.name.equals(currentBootBase, ignoreCase = true) || currentBootBase.contains(it.name, ignoreCase = true) }
                            val dbBoot2 = com.example.data.WildRiftRepository.items.find { it.name.equals(currentBootUpgrade, ignoreCase = true) || currentBootUpgrade.contains(it.name, ignoreCase = true) }
                            
                            val boot1Icon = dbBoot1?.iconUrl ?: com.example.data.WildRiftItemsData.getItemIconByName(currentBootBase)
                            val boot2Icon = dbBoot2?.iconUrl ?: com.example.data.WildRiftItemsData.getItemIconByName(currentBootUpgrade)

                            Box(
                                modifier = Modifier
                                    .size(bootBoxSize)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(HextechSurfaceVariant)
                                    .border(1.dp, HextechGold, RoundedCornerShape(8.dp))
                                    .clickable { if (dbBoot1 != null) itemForDetail = dbBoot1 }
                            ) {
                                AppAssetImage(
                                    url = boot1Icon,
                                    contentDescription = tr(currentBootBase),
                                    fallbackText = tr(currentBootBase),
                                    modifier = Modifier.fillMaxSize(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = ">",
                                tint = HextechCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))

                            Box(
                                modifier = Modifier
                                    .size(bootBoxSize)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(HextechSurfaceVariant)
                                    .border(1.dp, HextechCyan, RoundedCornerShape(8.dp))
                                    .clickable { if (dbBoot2 != null) itemForDetail = dbBoot2 }
                            ) {
                                AppAssetImage(
                                    url = boot2Icon,
                                    contentDescription = tr(currentBootUpgrade),
                                    fallbackText = tr(currentBootUpgrade),
                                    modifier = Modifier.fillMaxSize(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }

                        val allBootCandidates = (listOf(activeOption.bootBase) + activeOption.situationalBoots).filter { it.isNotBlank() }.distinct()
                        if (allBootCandidates.size > 1) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.5.dp)
                                    .background(HextechCardBorder.copy(alpha = 0.5f))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tr("Opciones Situacionales:"),
                                color = TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                allBootCandidates.forEach { sitBootName ->
                                    val isSelected = currentBootBase.equals(sitBootName, ignoreCase = true)
                                    val dbSitBoot = com.example.data.WildRiftRepository.items.find { it.name.equals(sitBootName, ignoreCase = true) || sitBootName.contains(it.name, ignoreCase = true) }
                                    val sitIcon = dbSitBoot?.iconUrl ?: com.example.data.WildRiftItemsData.getItemIconByName(sitBootName)

                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) HextechCyan.copy(alpha = 0.3f) else HextechSurfaceVariant)
                                            .border(
                                                width = if (isSelected) 1.5.dp else 0.8.dp,
                                                color = if (isSelected) HextechCyan else HextechCardBorder,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .clickable {
                                                selectedBootBaseOverride = sitBootName
                                            }
                                    ) {
                                        AppAssetImage(
                                            url = sitIcon,
                                            contentDescription = tr(sitBootName),
                                            fallbackText = tr(sitBootName),
                                            modifier = Modifier.fillMaxSize(),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Card 2: Hechizos de Invocador de esta Opción
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = tr("Hechizos"),
                            color = HextechGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            activeOption.spellsIcons.take(2).forEachIndexed { idx, iconUrl ->
                                val rawSpellName = activeOption.spells.getOrNull(idx) ?: "Destello"
                                val spellName = tr(rawSpellName)
                                val dbSpell = com.example.data.WildRiftRepository.summonerSpells.find {
                                    it.name.equals(rawSpellName, ignoreCase = true) || rawSpellName.contains(it.name, ignoreCase = true) || it.name.contains(rawSpellName, ignoreCase = true)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(spellBoxSize)
                                        .clip(CircleShape)
                                        .background(HextechSurfaceVariant)
                                        .border(1.5.dp, HextechCyan, CircleShape)
                                        .clickable { if (dbSpell != null) spellForDetail = dbSpell }
                                ) {
                                    AppAssetImage(
                                        url = iconUrl,
                                        contentDescription = spellName,
                                        fallbackText = spellName,
                                        modifier = Modifier.fillMaxSize(),
                                        shape = CircleShape
                                    )
                                }
                                if (idx == 0) Spacer(modifier = Modifier.width(12.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // SECCIÓN RUNAS ASOCIADAS A ESTA OPCIÓN
            // ==========================================
            Text(
                text = "${tr("Runas")} • ${activeOption.title}",
                color = HextechGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = HextechSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val runesForActiveOption = activeOption.runes.ifEmpty {
                        listOf("Conquistador", "Triunfo", "Golpe de gracia", "Linaje")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Runa Clave: " + tr(runesForActiveOption.firstOrNull() ?: "Principal"),
                            color = HextechGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Secundarias: " + runesForActiveOption.drop(1).joinToString(" • "),
                            color = HextechCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        runesForActiveOption.take(5).forEachIndexed { idx, rName ->
                            val isKeystone = idx == 0
                            val foundRune = com.example.data.WildRiftSpellsAndRunes.getRuneByName(rName)
                                ?: com.example.data.WildRiftRepository.runes.find { r -> r.name.equals(rName, ignoreCase = true) || rName.contains(r.name, ignoreCase = true) || r.name.contains(rName, ignoreCase = true) }
                            val iconUrl = foundRune?.iconUrl ?: com.example.data.WildRiftSpellsAndRunes.getRuneIconByName(rName)
                            val isResolved = iconUrl.isNotBlank() && iconUrl.startsWith("http")
                            val finalRuneBorderColor = if (!isResolved) com.example.ui.theme.DangerRed else if (isKeystone) HextechGold else HextechCyan.copy(alpha = 0.6f)

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(if (isKeystone) runeKeySize else runeSecSize)
                                    .clip(CircleShape)
                                    .background(HextechSurfaceVariant)
                                    .border(
                                        width = if (isKeystone) 2.dp else 1.dp,
                                        color = finalRuneBorderColor,
                                        shape = CircleShape
                                    )
                                    .clickable { 
                                        runeForDetail = foundRune ?: com.example.model.RuneItem(
                                            id = rName.lowercase().replace(" ", "_"),
                                            name = rName,
                                            category = if (isKeystone) "Clave" else "Secundaria",
                                            iconUrl = iconUrl,
                                            description = "Runa recomendada para esta opción táctica en Wild Rift."
                                        )
                                    }
                            ) {
                                AppAssetImage(
                                    url = iconUrl,
                                    contentDescription = tr(rName),
                                    fallbackText = tr(rName),
                                    modifier = Modifier.fillMaxSize(),
                                    shape = CircleShape
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))


            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // COUNTERS Y SINERGIAS (ADAPTADOS AL ROL)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Fuerte Contra (Ventaja)
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AllyBlue.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(tr("Ventaja Contra:"), color = AllyBlue, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(4.dp))
                        val advantageList = roleProfile.advantageAgainst.take(3)
                        advantageList.forEach { target ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        matchupExplanationTarget = target
                                        matchupExplanationType = "Ventaja"
                                    }
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("• $target", color = TextPrimary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }

                // Débil Contra (Debilidad)
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(tr("Débil Contra:"), color = DangerRed, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(4.dp))
                        val counteredList = roleProfile.counteredBy.take(3)
                        counteredList.forEach { counter ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        matchupExplanationTarget = counter
                                        matchupExplanationType = "Debilidad"
                                    }
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("• $counter", color = TextPrimary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }

                // Sinergias (Compañeros ideales)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("synergy_insight_card"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(tr("Sinergias:"), color = HextechGold, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(4.dp))
                        val rawSynergies = if (roleProfile.synergies.isNotEmpty()) {
                            roleProfile.synergies
                        } else if (champion.synergies.isNotEmpty()) {
                            champion.synergies
                        } else {
                            synergyProfile.bestTeammates.map { it.championName }
                        }
                        val synergyList = rawSynergies.distinct().take(3)
                        if (synergyList.isEmpty()) {
                            Text("—", color = TextMuted, fontSize = 11.sp)
                        } else {
                            synergyList.forEach { partner ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            matchupExplanationTarget = partner
                                            matchupExplanationType = "Sinergia"
                                        }
                                        .padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("• $partner", color = TextPrimary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }



            Spacer(modifier = Modifier.height(30.dp))
        }
    }
    }

    if (isOverlay) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.95f))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
            ) {
                dialogContent()
            }
        }
    } else {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = onDismiss,
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            dialogContent()
        }
    }

@Composable
fun AdaptiveDetailAlertDialog(
    isOverlay: Boolean,
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit
) {
    if (isOverlay) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.85f))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onDismissRequest
                )
                .padding(12.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = com.example.ui.theme.HextechDarkBg),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, com.example.ui.theme.HextechGold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    title()
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(modifier = Modifier.weight(1f, fill = false)) {
                        text()
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        confirmButton()
                    }
                }
            }
        }
    } else {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismissRequest,
            title = title,
            text = text,
            confirmButton = confirmButton,
            containerColor = com.example.ui.theme.HextechSurface,
            titleContentColor = com.example.ui.theme.HextechGold,
            textContentColor = com.example.ui.theme.TextPrimary
        )
    }
}

    // ==========================================
    // DIALOG DE DETALLE DE OBJETO SITUACIONAL
    // ==========================================
    if (selectedSituationalItem != null) {
        val itemName = selectedSituationalItem!!
        val advice = SituationalItemAdvisor.getAdvice(itemName)

        AdaptiveDetailAlertDialog(
            isOverlay = isOverlay,
            onDismissRequest = { selectedSituationalItem = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (advice.iconUrl.isNotBlank()) {
                        AppAssetImage(
                            url = advice.iconUrl,
                            contentDescription = tr(advice.name),
                            fallbackText = advice.name,
                            modifier = Modifier.size(36.dp),
                            borderColor = HextechGold,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Column {
                        Text(
                            text = tr(advice.name),
                            color = HextechGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = tr(advice.categoryName),
                            color = HextechCyan,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // ¿Por qué comprarlo?
                    Column {
                        Text(
                            text = tr("¿Por qué comprar este objeto?"),
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        FormattedWildRiftText(
                            text = advice.purpose,
                            color = TextPrimary,
                            fontSize = 12.5.sp,
                            lineHeight = 17.sp
                        )
                    }

                    // Contra quién o qué es bueno
                    Column {
                        Text(
                            text = tr("Efectivo contra:"),
                            color = DangerRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            advice.bestAgainst.forEach { target ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(HextechSurfaceVariant)
                                        .border(1.dp, DangerRed.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "️ $target",
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Efecto clave
                    Column {
                        Text(
                            text = tr("Efecto clave:"),
                            color = HextechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        FormattedWildRiftText(
                            text = advice.keyEffect,
                            color = TextPrimary.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    // Consejo táctico
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(HextechGold.copy(alpha = 0.12f))
                            .border(1.dp, HextechGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = " ${advice.recommendationTip}",
                            color = TextPrimary,
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedSituationalItem = null }) {
                    Text(tr("Entendido"), color = HextechCyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }



    // ==========================================
    // DIALOG DE DETALLE DE MATCHUP / SINERGIA
    // ==========================================
    if (matchupExplanationTarget != null && matchupExplanationType != null) {
        val type = matchupExplanationType!!
        val target = matchupExplanationTarget!!

        val titleText = if (com.example.util.LocalLanguage.current == "es" || com.example.util.LocalLanguage.current == "auto") {
            when (type) {
                "Ventaja" -> "Ventaja contra $target"
                "Debilidad" -> "Débil contra $target"
                "Situacional" -> "Objeto Situacional: $target"
                else -> "Sinergia con $target"
            }
        } else {
            when (type) {
                "Ventaja" -> "Strong against $target"
                "Debilidad" -> "Weak against $target"
                "Situacional" -> "Situational Item: $target"
                else -> "Synergy with $target"
            }
        }

        val descText = CoachingGenerator.generateMatchupReason(champion, selectedRole, target, type, com.example.util.LocalLanguage.current)

        AdaptiveDetailAlertDialog(
            isOverlay = isOverlay,
            onDismissRequest = { matchupExplanationTarget = null },
            title = {
                Text(
                    text = titleText,
                    color = HextechGold,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(descText, color = TextPrimary)
            },
            confirmButton = {
                TextButton(onClick = { matchupExplanationTarget = null }) {
                    Text(tr("Entendido"), color = HextechCyan)
                }
            }
        )
    }


    itemForDetail?.let { item ->
        val itemDetailCard = @Composable {
            androidx.compose.material3.Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isOverlay) 4.dp else 16.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = com.example.ui.theme.HextechDarkBg),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, com.example.ui.theme.HextechGold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val lang = LocalLanguage.current
                    val localizedName = item.getLocalizedName(lang)
                    val statsList = item.getStatsList(lang)
                    val localizedPassive = item.getLocalizedPassive(lang)
                    val localizedCoachTip = item.getLocalizedCoachTip(lang)

                    com.example.ui.components.AppAssetImage(
                        url = item.iconUrl,
                        contentDescription = localizedName,
                        fallbackText = localizedName,
                        modifier = Modifier.size(72.dp),
                        borderColor = com.example.ui.theme.HextechGold,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = localizedName,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(com.example.ui.theme.HextechCyan.copy(alpha = 0.2f), androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = tr(item.category),
                                color = com.example.ui.theme.HextechCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(com.example.ui.theme.HextechGold.copy(alpha = 0.2f), androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = " ${item.goldCost} ${tr("Oro")}",
                                color = com.example.ui.theme.HextechGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (statsList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = tr("Estadísticas:"),
                            color = com.example.ui.theme.HextechGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            statsList.forEach { stat ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(com.example.ui.theme.HextechCyan, androidx.compose.foundation.shape.CircleShape)
                                    )
                                    Text(
                                        text = stat.parseHtmlColorToAnnotatedString(),
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                    if (localizedPassive.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = tr("Efecto / Pasiva:"),
                            color = com.example.ui.theme.HextechGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        com.example.ui.components.FormattedWildRiftText(
                            text = localizedPassive,
                            color = com.example.ui.theme.TextMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (localizedCoachTip.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.HextechGold.copy(alpha = 0.08f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.HextechGold.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("", fontSize = 13.sp)
                                    Text(
                                        text = tr("Consejos del Coach:"),
                                        color = com.example.ui.theme.HextechGoldLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                com.example.ui.components.FormattedWildRiftText(
                                    text = localizedCoachTip,
                                    color = TextPrimary.copy(alpha = 0.95f),
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.5.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                            .background(com.example.ui.theme.HextechCyan)
                            .clickable { itemForDetail = null }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(tr("Cerrar"), color = com.example.ui.theme.HextechDarkBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        if (isOverlay) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.85f))
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = { itemForDetail = null }
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                itemDetailCard()
            }
        } else {
            androidx.compose.ui.window.Dialog(onDismissRequest = { itemForDetail = null }) {
                itemDetailCard()
            }
        }
    }

    runeForDetail?.let { rune ->
        AdaptiveDetailAlertDialog(
            isOverlay = isOverlay,
            onDismissRequest = { runeForDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    com.example.ui.components.AppAssetImage(
                        url = rune.iconUrl,
                        contentDescription = rune.name,
                        fallbackText = rune.name,
                        modifier = Modifier.size(48.dp),
                        borderColor = com.example.ui.theme.HextechGold,
                        shape = androidx.compose.foundation.shape.CircleShape
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
                            text = tr(rune.category),
                            color = com.example.ui.theme.HextechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            text = {
                Text(
                    text = tr(rune.description),
                    color = com.example.ui.theme.TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { runeForDetail = null }) {
                    Text(tr("Cerrar"), color = com.example.ui.theme.HextechCyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    spellForDetail?.let { spell ->
        AdaptiveDetailAlertDialog(
            isOverlay = isOverlay,
            onDismissRequest = { spellForDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    com.example.ui.components.AppAssetImage(
                        url = spell.iconUrl,
                        contentDescription = spell.name,
                        fallbackText = spell.name,
                        modifier = Modifier.size(48.dp),
                        borderColor = com.example.ui.theme.HextechGold,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = tr(spell.name),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Text(
                    text = tr(spell.description),
                    color = com.example.ui.theme.TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { spellForDetail = null }) {
                    Text(tr("Cerrar"), color = com.example.ui.theme.HextechCyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

}
