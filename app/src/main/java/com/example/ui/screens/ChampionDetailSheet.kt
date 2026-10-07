package com.example.ui.screens

import com.example.ui.components.CoachTextButton as TextButton
import com.example.ui.components.CoachIconButton as IconButton

import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.platform.testTag
import com.example.util.BuildChoiceRules

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import com.example.utils.parseHtmlColorToAnnotatedString
import com.example.data.WildRiftItemsData
import com.example.model.WildRiftItem
import com.example.ui.theme.HextechGoldLight
import com.example.ui.theme.TextSecondary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.ui.components.coachClickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon

import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text

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
import com.example.ui.components.AppAssetImage
import com.example.ui.components.ChampionAvatar
import com.example.ui.components.FormattedWildRiftText
import com.example.ui.components.DetailedTrendGraphCard
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChampionDetailSheet(
    isOverlay: Boolean = false,
    champion: Champion?,
    onDismiss: () -> Unit,
    onChampionSelected: (Champion) -> Unit = {}
) {
    val isPremium by SubscriptionManager.isPremium.collectAsState()
    if (champion == null) return

    val context = LocalContext.current
    val isSignedIn by com.example.util.AuthManager.isSignedIn.collectAsStateWithLifecycle()
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

    var selectedMatchupName by remember(champion.id) { mutableStateOf<String?>(null) }
    var selectedSituationalItem by remember { mutableStateOf<String?>(null) }
    var selectedElementAdvice by remember { mutableStateOf("") }
    var buildAdvice by remember { mutableStateOf<Pair<String, String>?>(null) }
    var itemForDetail by remember { mutableStateOf<com.example.model.WildRiftItem?>(null) }
    var runeForDetail by remember { mutableStateOf<com.example.model.RuneItem?>(null) }
    var spellForDetail by remember { mutableStateOf<com.example.model.SummonerSpellItem?>(null) }
    var selectedBuildOptionIndex by remember(champion.id, selectedRole) { mutableStateOf(0) }

    val currentLang = com.example.util.currentAppLanguage()



    // Service-hosted overlays have no activity back dispatcher; their visible
    // back and close controls already invoke onDismiss.
    if (androidx.activity.compose.LocalOnBackPressedDispatcherOwner.current != null) {
        androidx.activity.compose.BackHandler {
            if (buildAdvice != null) {
                buildAdvice = null
            } else if (itemForDetail != null) {
                itemForDetail = null
            } else if (runeForDetail != null) {
                runeForDetail = null
            } else if (spellForDetail != null) {
                spellForDetail = null
            } else if (selectedSituationalItem != null) {
                selectedSituationalItem = null
            } else {
                onDismiss()
            }
        }
    }

    // Perfil dinámico de estadísticas, build, runas y counters adaptados a la línea elegida
    val roleProfile = remember(champion.id, selectedRole, currentLang) {
        ChampionRoleAdapter.getProfile(champion, selectedRole, currentLang)
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
                            .coachClickable { onDismiss() }
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
                                text = com.example.util.tr(champion.name),
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
                                    text = com.example.util.tr("Tier ${roleProfile.tier}"),
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
                            text = com.example.util.tr("${tr(selectedRole.displayName)}${if (selectedRole != champion.primaryRole) " (Flex)" else ""} • ${tr(champion.damageType.displayName)}"),
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
                                    android.widget.Toast.makeText(context, com.example.util.appTr("Requiere suscripción Premium"), android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("detail_fav_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = com.example.util.trNullable(if (isFavorite) tr("Quitar de Favoritos") else tr("Marcar como Favorito")),
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
                                Text(com.example.util.tr("PRO"), color = HextechDarkBg, fontSize = 6.5.sp, fontWeight = FontWeight.Black)
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
                                    .coachClickable {
                                        selectedRole = role
                                        selectedBuildOptionIndex = 0
                                    }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = com.example.util.tr(roleLabel),
                                        color = if (isSelected) HextechGold else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    if (isPrimary) {
                                        Text(com.example.util.tr("Main"), color = HextechCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text(com.example.util.tr("Flex"), color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
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
                                text = com.example.util.tr("⭐ ${tr("Estadísticas, hechizos, runas y build adaptadas a")} ${tr(selectedRole.displayName)}."),
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
                        Row(modifier = Modifier.weight(1f).padding(end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = HextechGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tr("Estadísticas del Meta Oficial"),
                                color = HextechGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (isSignedIn) Row(
                            modifier = Modifier.testTag("champion_trend_header"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
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
                            Text(com.example.util.tr("${String.format(java.util.Locale.US, "%.2f", roleProfile.winrate)}%"), color = HextechGold, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            val winDelta = roleProfile.winrateDelta
                            val formattedWinDelta = String.format(java.util.Locale.US, "%.2f", winDelta)
                            val winDeltaText = if (winDelta >= 0) "+${formattedWinDelta}%" else "${formattedWinDelta}%"
                            val winDeltaColor = if (winDelta >= 0) Color(0xFF4CAF50) else DangerRed
                            Text(
                                text = com.example.util.tr(if (winDelta >= 0) "▲ $winDeltaText" else "▼ $winDeltaText"),
                                color = winDeltaColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Pick Rate + Delta
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(tr("Tasa de Selección"), color = TextMuted, fontSize = 11.sp)
                            Text(com.example.util.tr("${String.format(java.util.Locale.US, "%.2f", roleProfile.pickRate)}%"), color = HextechCyan, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            val pickDelta = roleProfile.pickRateDelta
                            val formattedPickDelta = String.format(java.util.Locale.US, "%.2f", pickDelta)
                            val pickDeltaText = if (pickDelta >= 0) "+${formattedPickDelta}%" else "${formattedPickDelta}%"
                            val pickDeltaColor = if (pickDelta >= 0) Color(0xFF29B6F6) else Color(0xFFFFA726)
                            Text(
                                text = com.example.util.tr(if (pickDelta >= 0) "▲ $pickDeltaText" else "▼ $pickDeltaText"),
                                color = pickDeltaColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Ban Rate + Delta
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(tr("Tasa de Bloqueo"), color = TextMuted, fontSize = 11.sp)
                            Text(com.example.util.tr("${String.format(java.util.Locale.US, "%.2f", roleProfile.banRate)}%"), color = DangerRed, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            val banDelta = roleProfile.banRateDelta
                            val formattedBanDelta = String.format(java.util.Locale.US, "%.2f", banDelta)
                            val banDeltaText = if (banDelta >= 0) "+${formattedBanDelta}%" else "${formattedBanDelta}%"
                            val banDeltaColor = if (banDelta >= 0) DangerRed else Color(0xFF4CAF50)
                            Text(
                                text = com.example.util.tr(if (banDelta >= 0) "▲ $banDeltaText" else "▼ $banDeltaText"),
                                color = banDeltaColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tarjeta analítica de tendencia: hace 24 horas vs. hace 12 horas vs. actual
                    if (isSignedIn) {
                        DetailedTrendGraphCard(
                            winrate = roleProfile.winrate,
                            delta = roleProfile.winrateDelta
                        )
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
                    val currentLang = com.example.util.currentAppLanguage()
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
                    text = com.example.util.tr(tr("Habilidades de") + " ${champion.name}"),
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
                                    val localizedSkillName = skill.getLocalizedName(com.example.util.currentAppLanguage())
                                    val localizedSkillDesc = skill.getLocalizedDescription(com.example.util.currentAppLanguage())
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val slotTranslation = when {
                                            skill.slotName.contains("Pasiva", true) || skill.slot.equals("P", true) || skill.slot.equals("Passive", true) -> tr("Pasiva:")
                                            skill.slotName.contains("Habilidad 1", true) || skill.slot == "1" || skill.slot.equals("Q", true) -> tr("Habilidad 1:")
                                            skill.slotName.contains("Habilidad 2", true) || skill.slot == "2" || skill.slot.equals("W", true) -> tr("Habilidad 2:")
                                            skill.slotName.contains("Habilidad 3", true) || skill.slot == "3" || skill.slot.equals("E", true) -> tr("Habilidad 3:")
                                            skill.slotName.contains("Definitiva", true) || skill.slot == "4" || skill.slot.equals("R", true) -> tr("Definitiva:")
                                            else -> if (skill.slotName.isNotBlank()) tr(skill.slotName) else if (skill.slot.isNotBlank()) "${tr("Habilidad")} ${skill.slot}:" else ""
                                        }
                                        Text(
                                            text = com.example.util.tr(if (slotTranslation.isNotBlank()) "$slotTranslation $localizedSkillName" else localizedSkillName),
                                            color = TextPrimary,
                                            fontSize = if (isCompact) 11.sp else 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (skill.cooldown.isNotBlank()) {
                                            Text(
                                                text = com.example.util.tr(skill.cooldown),
                                                color = HextechCyan,
                                                fontSize = if (isCompact) 9.sp else 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    FormattedWildRiftText(
                                        text = tr(localizedSkillDesc),
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
            // BUILD TÁCTICA OFICIAL: UNA ÚNICA BUILD POR CAMPEÓN Y LÍNEA
            // ==========================================
            val customBuilds by com.example.data.local.CustomChampionBuildsManager.customBuilds.collectAsStateWithLifecycle()
            val championCustomBuilds = remember(customBuilds, champion.id, selectedRole, champion.primaryRole) {
                val expectedRole = if (selectedRole == champion.primaryRole) {
                    selectedRole.displayName
                } else {
                    "${selectedRole.displayName} (Flex)"
                }

                customBuilds.filter { record ->
                    record.championId.equals(champion.id, ignoreCase = true) &&
                        record.role.equals(expectedRole, ignoreCase = true)
                }
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
                    val t2 = rec.bootsT2Item?.itemName?.ifBlank { null } ?: "Botas blindadas"
                    val t3 = rec.bootsT3Item?.itemName?.ifBlank { null } ?: com.example.util.ChampionRoleAdapter.getTier3BootUpgrade(t2)
                    // Solo las botas de Nivel 2 son opciones seleccionables.
                    // La evolución de Nivel 3 se deriva automáticamente del par T2 -> T3.
                    val sitBoots = listOfNotNull(
                        rec.situationalBootsT2Item?.itemName?.ifBlank { null }
                    ).filter { !it.equals(t2, ignoreCase = true) }

                    com.example.util.ChampionBuildOption(
                        optionNumber = 1,
                        title = rec.buildTitle,
                        subtitle = "Línea: ${rec.role} • Análisis del coach",
                        source = "Criterio del coach • ${rec.creatorName}",
                        badge = if (rec.role.contains("Flex", ignoreCase = true)) "FLEX PRO" else "CRITERIO COACH",
                        tacticalReason = rec.coachAdvice.ifBlank { CoachingGenerator.generateTacticalAnalysis(champion, selectedRole, "es") },
                        items = rec.coreItemsWithDesc.map { it.itemName }.ifEmpty { rec.coreItems },
                        bootBase = t2,
                        bootBaseAdvice = rec.bootsT2Item?.description.orEmpty(),
                        bootUpgradeAdvice = rec.bootsT3Item?.description.orEmpty(),
                        bootUpgrade = t3,
                        situationalBoots = sitBoots,
                        situationalItems = rec.situationalItemsWithDesc.map { it.itemName }.ifEmpty { rec.situationalItems },
                        runes = rec.coreRunes.map { it.runeName }.ifEmpty { rec.runes.split(",").map { it.trim() }.filter { it.isNotBlank() } },
                        spells = rec.coreSpells.map { it.spellName }.ifEmpty { rec.spells },
                        spellsIcons = rec.coreSpells.map { it.iconUrl },
                        coreItemsWithDesc = rec.coreItemsWithDesc,
                        situationalItemsWithDesc = rec.situationalItemsWithDesc,
                        coreRunes = rec.coreRunes,
                        situationalRunes = rec.situationalRunes,
                        coreSpells = rec.coreSpells,
                        situationalSpells = rec.situationalSpells,
                        gameplayVideoUri = rec.gameplayVideoUri,
                        situationalBootReasons = listOfNotNull(rec.situationalBootsT2Item)
                            .associate { it.itemName to it.description }
                    )
                }
                listOf(customOptions.firstOrNull() ?: baseBuildOptions.first())
            }

            val activeOption = buildOptionsList.getOrNull(selectedBuildOptionIndex.coerceIn(0, buildOptionsList.size - 1))
                ?: buildOptionsList.first()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = com.example.util.tr("${tr("Build Táctica Oficial")} • ${selectedRole.shortName}"),
                    color = HextechGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                if (buildOptionsList.size > 1) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(HextechCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = com.example.util.tr("↔ " + tr("Desliza opciones")),
                            color = HextechCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(HextechGold.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = com.example.util.tr("ÚNICA POR LÍNEA"),
                            color = HextechGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            if (buildOptionsList.size > 1) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                                .coachClickable { selectedBuildOptionIndex = idx }
                                .padding(horizontal = if (isCompact) 8.dp else 12.dp, vertical = if (isCompact) 4.dp else 7.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = com.example.util.tr(label),
                                    color = if (isSelected) HextechGold else TextMuted,
                                    fontSize = if (isCompact) 10.sp else 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
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
                                text = tr(activeOption.title),
                                color = HextechGold,
                                fontSize = if (isCompact) 11.5.sp else 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            val currentLang = com.example.util.currentAppLanguage()
                            val shareTitle = tr("Compartir Build")
                            val shareDesc = tr("Compartir")
                            val shareBuild = {
                                val shareText = buildString {
                                    appendLine("🛡️ Build: ${com.example.util.trStr(currentLang, activeOption.title)} para ${champion.getLocalizedName(currentLang)}")
                                    appendLine("👤 Rol: ${selectedRole.getLocalizedName(currentLang)}")
                                    appendLine("⚔️ Core: ${activeOption.items.map { com.example.util.trStr(currentLang, it) }.joinToString(", ")}")
                                    if (activeOption.situationalItems.isNotEmpty()) appendLine("🔄 Situacionales: ${activeOption.situationalItems.map { com.example.util.trStr(currentLang, it) }.joinToString(", ")}")
                                    appendLine("💎 Runas: ${activeOption.runes.map { com.example.util.trStr(currentLang, it) }.joinToString(", ")}")
                                    appendLine("🔥 ¡Comparte desde Coach App!")
                                }
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, shareTitle))
                            }
                            IconButton(onClick = shareBuild, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Share, contentDescription = com.example.util.trNullable(shareDesc), tint = HextechCyan, modifier = Modifier.size(16.dp))
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
                                    text = tr(activeOption.badge),
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

                    if (activeOption.tacticalReason.isNotBlank()) {
                        Text(tr("Consejo del coach"), color = HextechGold, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        FormattedWildRiftText(
                            text = tr(activeOption.tacticalReason),
                            modifier = Modifier.testTag("build_coach_overview"),
                            color = TextPrimary,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(10.dp))
                    }

                    // Items List - 3 Core Items
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr("3 Core Items (Pico de Poder)"),
                            color = HextechGold,
                            fontSize = if (isCompact) 10.sp else 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(HextechGold.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = com.example.util.tr("CORE ESSENTIALS"),
                                color = HextechGold,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth().testTag("build_core_items_section")
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
                            val isResolved = iconUrl.isNotBlank() && (iconUrl.startsWith("http") || iconUrl.startsWith("file:") || iconUrl.startsWith("android.resource:"))
                            val finalBorderColor = if (!isResolved) com.example.ui.theme.DangerRed else HextechGold

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .testTag("build_item_details")
                                    .coachClickable {
                                        if (dbItem != null) {
                                            selectedElementAdvice = com.example.util.BuildElementAdvice.contextualItemAdvice(
                                                itemName = rawName,
                                                championName = champion.getLocalizedName(currentLang),
                                                roleName = selectedRole.getLocalizedName(currentLang),
                                                language = currentLang,
                                                situational = false
                                            )
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




                }
            }

            // Keep item choices visible; details belong to the icon, not inline advice.
            val situationalChoices = BuildChoiceRules.situationalItems(
                activeOption.items, activeOption.situationalItems,
                roleProfile.situationalItems + roleProfile.build8Items + champion.situationalItems
            )
            if (situationalChoices.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(Modifier.padding(10.dp)) {
                        Text(tr("Objetos Situacionales"), color = HextechGold, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            situationalChoices.forEach { itemName ->
                                val item = WildRiftItemsData.getItemByName(itemName)
                                AppAssetImage(
                                    url = item?.iconUrl ?: WildRiftItemsData.getItemIconByName(itemName),
                                    contentDescription = tr(itemName), fallbackText = tr(itemName),
                                    modifier = Modifier.size(if (isCompact) 32.dp else 42.dp).coachClickable {
                                        selectedElementAdvice = com.example.util.BuildElementAdvice.contextualItemAdvice(
                                            itemName = itemName,
                                            championName = champion.getLocalizedName(currentLang),
                                            roleName = selectedRole.getLocalizedName(currentLang),
                                            language = currentLang,
                                            situational = true
                                        )
                                        if (item != null) itemForDetail = item else selectedSituationalItem = itemName
                                    }, shape = RoundedCornerShape(6.dp)
                                )
                            }
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.height(12.dp))

            // ==========================================
            // BOTAS Y MEJORAS + HECHIZOS (DOS COLUMNAS)
            // ==========================================
            // Advice dialogs rebuild the option object; selection belongs to its stable identity.
            var selectedBootBaseOverride by remember(champion.id, selectedRole, activeOption.optionNumber) { mutableStateOf<String?>(null) }
            val primaryBootBase = activeOption.bootBase.ifBlank { "Botas blindadas" }
            val currentBootBase = selectedBootBaseOverride ?: primaryBootBase
            val currentBootUpgrade = if (selectedBootBaseOverride != null) {
                ChampionRoleAdapter.getTier3BootUpgrade(currentBootBase)
            } else if (activeOption.bootUpgrade.isNotBlank() && activeOption.bootUpgrade.equals(ChampionRoleAdapter.getTier3BootUpgrade(activeOption.bootBase), ignoreCase = true)) {
                activeOption.bootUpgrade
            } else {
                ChampionRoleAdapter.getTier3BootUpgrade(currentBootBase)
            }

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
                            val dbBoot1 = WildRiftItemsData.getItemByName(currentBootBase) ?: com.example.data.WildRiftRepository.items.find { it.name.equals(currentBootBase, ignoreCase = true) || currentBootBase.contains(it.name, ignoreCase = true) }
                            val dbBoot2 = WildRiftItemsData.getItemByName(currentBootUpgrade) ?: com.example.data.WildRiftRepository.items.find { it.name.equals(currentBootUpgrade, ignoreCase = true) || currentBootUpgrade.contains(it.name, ignoreCase = true) }

                            val boot1Icon = dbBoot1?.iconUrl ?: com.example.data.WildRiftItemsData.getItemIconByName(currentBootBase)
                            val boot2Icon = dbBoot2?.iconUrl ?: com.example.data.WildRiftItemsData.getItemIconByName(currentBootUpgrade)

                            Box(
                                modifier = Modifier
                                    .size(bootBoxSize)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(HextechSurfaceVariant)
                                    .border(1.dp, HextechGold, RoundedCornerShape(8.dp))
                                    .testTag("selected_boot_$currentBootBase")
                                    .coachClickable {
                                        if (dbBoot1 != null) {
                                            selectedElementAdvice = com.example.util.BuildElementAdvice.contextualBootAdvice(
                                                bootName = currentBootBase,
                                                championName = champion.getLocalizedName(currentLang),
                                                roleName = selectedRole.getLocalizedName(currentLang),
                                                language = currentLang,
                                                situational = !currentBootBase.equals(primaryBootBase, ignoreCase = true)
                                            )
                                            itemForDetail = dbBoot1
                                        }
                                    }
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
                                contentDescription = com.example.util.trNullable(">"),
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
                                    .testTag("selected_boot_$currentBootUpgrade")
                                    .coachClickable { if (dbBoot2 != null) {
                                        selectedElementAdvice = com.example.util.BuildElementAdvice.contextualBootAdvice(
                                            bootName = currentBootUpgrade,
                                            championName = champion.getLocalizedName(currentLang),
                                            roleName = selectedRole.getLocalizedName(currentLang),
                                            language = currentLang,
                                            situational = selectedBootBaseOverride != null
                                        )
                                        itemForDetail = dbBoot2
                                    } }
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

                        val situationalBootCandidates = activeOption.situationalBoots
                            .filter { it.isNotBlank() }
                            .filterNot { it.equals(activeOption.bootBase, ignoreCase = true) }
                            .distinct()

                        if (situationalBootCandidates.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.5.dp)
                                    .background(HextechCardBorder.copy(alpha = 0.5f))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tr("Seleccionar botas (Nivel 2):"),
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
                                (listOf(primaryBootBase) + situationalBootCandidates).forEach { sitBootName ->
                                    val isSelected = currentBootBase.equals(sitBootName, ignoreCase = true)
                                    val dbSitBoot = com.example.data.WildRiftRepository.items.find {
                                        it.name.equals(sitBootName, ignoreCase = true)
                                    }
                                    val sitIcon = dbSitBoot?.iconUrl
                                        ?: com.example.data.WildRiftItemsData.getItemIconByName(sitBootName)

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
                                            .testTag("build_boot_${sitBootName}")
                                            .selectable(selected = isSelected, onClick = {
                                                selectedBootBaseOverride = sitBootName.takeUnless { it.equals(primaryBootBase, true) }
                                                val reason = com.example.util.BuildElementAdvice.contextualBootAdvice(
                                                    bootName = sitBootName,
                                                    championName = champion.getLocalizedName(currentLang),
                                                    roleName = selectedRole.getLocalizedName(currentLang),
                                                    language = currentLang,
                                                    situational = !sitBootName.equals(primaryBootBase, ignoreCase = true)
                                                )
                                                if (reason.isNotBlank()) buildAdvice = "Consejo del coach" to reason
                                            })
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
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(HextechSurfaceVariant)
                                        .border(1.5.dp, HextechCyan, RoundedCornerShape(8.dp))
                                        .testTag("build_spell_details")
                                        .coachClickable { if (dbSpell != null) {
                                            selectedElementAdvice = com.example.util.BuildElementAdvice.contextualSpellAdvice(
                                                spellName = rawSpellName,
                                                championName = champion.getLocalizedName(currentLang),
                                                roleName = selectedRole.getLocalizedName(currentLang),
                                                language = currentLang
                                            )
                                            spellForDetail = dbSpell
                                        } }
                                ) {
                                    AppAssetImage(
                                        url = iconUrl,
                                        contentDescription = spellName,
                                        fallbackText = spellName,
                                        modifier = Modifier.fillMaxSize(),
                                        shape = RoundedCornerShape(8.dp)
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
                text = "${tr("Runas")} • ${tr(activeOption.title)}",
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

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = com.example.util.tr("${tr("Runa Clave:")} ${tr(runesForActiveOption.firstOrNull() ?: "Principal")}"),
                            color = HextechGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = com.example.util.tr("${tr("Secundarias:")} ${runesForActiveOption.drop(1).map { tr(it) }.joinToString(" • ")}"),
                            color = HextechCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
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
                            val isResolved = iconUrl.isNotBlank() && (iconUrl.startsWith("http") || iconUrl.startsWith("file:") || iconUrl.startsWith("android.resource:"))
                            val finalRuneBorderColor = if (!isResolved) com.example.ui.theme.DangerRed else if (isKeystone) HextechGold else HextechCyan.copy(alpha = 0.6f)

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(if (isKeystone) runeKeySize else runeSecSize)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(HextechSurfaceVariant)
                                    .border(
                                        width = if (isKeystone) 2.dp else 1.dp,
                                        color = finalRuneBorderColor,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .testTag("build_rune_details")
                                    .coachClickable {
                                        selectedElementAdvice = com.example.util.BuildElementAdvice.contextualRuneAdvice(
                                            runeName = rName,
                                            championName = champion.getLocalizedName(currentLang),
                                            roleName = selectedRole.getLocalizedName(currentLang),
                                            language = currentLang,
                                            situational = false
                                        )
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
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    val validSitRunes = BuildChoiceRules.runeAlternatives(
                        runesForActiveOption.map { name ->
                            val rune = com.example.data.WildRiftSpellsAndRunes.getRuneByName(name)
                            BuildChoiceRules.RuneChoice(rune?.name ?: name, rune?.category.orEmpty())
                        },
                        activeOption.situationalRunes.filter { entry ->
                            val rune = com.example.data.WildRiftSpellsAndRunes.getRuneByName(entry.runeName)
                            BuildChoiceRules.hasSituationalReason(entry.description, rune?.description.orEmpty())
                        }.map { entry ->
                            val rune = com.example.data.WildRiftSpellsAndRunes.getRuneByName(entry.runeName)
                            BuildChoiceRules.RuneChoice(rune?.name ?: entry.runeName, rune?.category.orEmpty())
                        }
                    )
                    val nonDuplicateSitRunes = activeOption.situationalRunes.filter { entry ->
                        val canonical = com.example.data.WildRiftSpellsAndRunes.getRuneByName(entry.runeName)?.name ?: entry.runeName
                        validSitRunes.any { it.rune.name.equals(canonical, true) }
                    }.distinctBy { it.runeName.lowercase() }
                    if (nonDuplicateSitRunes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(HextechCardBorder.copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tr("Runas Situacionales"),
                                color = HextechGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tr("Adaptar según partida"),
                                color = HextechCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            nonDuplicateSitRunes.forEach { sRune ->
                                val rName = sRune.runeName
                                val foundRune = com.example.data.WildRiftSpellsAndRunes.getRuneByName(rName)
                                    ?: com.example.data.WildRiftRepository.runes.find { r -> r.name.equals(rName, ignoreCase = true) }
                                val iconUrl = sRune.iconUrl.ifBlank { foundRune?.iconUrl ?: com.example.data.WildRiftSpellsAndRunes.getRuneIconByName(rName) }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(HextechDarkBg.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                        .border(0.5.dp, HextechCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .coachClickable {
                                            selectedElementAdvice = com.example.util.BuildElementAdvice.contextualRuneAdvice(
                                                runeName = rName,
                                                championName = champion.getLocalizedName(currentLang),
                                                roleName = selectedRole.getLocalizedName(currentLang),
                                                language = currentLang,
                                                situational = true
                                            )
                                            runeForDetail = foundRune ?: com.example.model.RuneItem(
                                                id = rName.lowercase().replace(" ", "_"),
                                                name = rName,
                                                category = "Situacional",
                                                iconUrl = iconUrl,
                                                description = sRune.description
                                            )
                                        }
                                        .padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(runeSecSize)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(HextechSurfaceVariant)
                                            .border(1.dp, HextechCyan, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AppAssetImage(
                                            url = iconUrl,
                                            contentDescription = tr(rName),
                                            fallbackText = tr(rName),
                                            modifier = Modifier.fillMaxSize(),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val canonical = foundRune?.name ?: rName
                                    val alternative = validSitRunes.firstOrNull { it.rune.name.equals(canonical, true) }
                                    Text("←", color = HextechGold, modifier = Modifier.padding(horizontal = 6.dp))
                                    val sourceNames = alternative?.secondarySlot?.let { listOf(runesForActiveOption[it]) }
                                        ?: runesForActiveOption.drop(1).take(3)
                                    sourceNames.forEach { sourceName ->
                                        AppAssetImage(
                                            url = com.example.data.WildRiftSpellsAndRunes.getRuneIconByName(sourceName),
                                            contentDescription = tr(sourceName), fallbackText = tr(sourceName),
                                            modifier = Modifier.size(runeSecSize), shape = RoundedCornerShape(8.dp)
                                        )
                                    }

                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // COUNTERS Y SINERGIAS (ADAPTADOS AL ROL)
            // ==========================================
            val isUserPremium by com.example.util.SubscriptionManager.isPremium.collectAsStateWithLifecycle()
            val matchupUserRole by com.example.util.SubscriptionManager.userRole.collectAsStateWithLifecycle()
            val isPremium = isUserPremium || matchupUserRole == "admin"
            val maxMatchupCount = BuildChoiceRules.matchupLimit(isPremium, isSignedIn)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Fuerte Contra (Ventaja)
                Card(
                    modifier = Modifier.weight(1f).testTag("advantage_insight_card"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AllyBlue.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val advantageList = roleProfile.advantageAgainst.distinct().take(maxMatchupCount)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = com.example.util.tr("${tr("Ventaja")} (${advantageList.size})"),
                                color = AllyBlue,
                                fontSize = if (isCompact) 9.5.sp else 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!isPremium) {
                                Text(
                                    text = "PRO 12",
                                    color = HextechGold,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val advantageChunks = advantageList.chunked(if (isCompact) 1 else 2)
                        if (advantageList.isEmpty()) {
                            Text(com.example.util.tr("—"), color = TextMuted, fontSize = 11.sp)
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                advantageChunks.forEach { rowChampions ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        for (col in 0 until (if (isCompact) 1 else 2)) {
                                            val target = rowChampions.getOrNull(col)
                                            if (target != null) {
                                                val targetChamp = resolveTargetChampion(target)
                                                val matchupName = targetChamp?.getLocalizedName(currentLang) ?: tr(target)
                                                Box(
                                                    modifier = Modifier.size(48.dp).testTag("build_matchup_name_advantage")
                                                        .semantics { contentDescription = matchupName }
                                                        .coachClickable {
                                                            selectedMatchupName = matchupName
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (targetChamp != null) {
                                                        ChampionAvatar(
                                                            champion = targetChamp,
                                                            size = if (isCompact) 26.dp else 30.dp,
                                                            showTierBadge = false,
                                                            borderColor = AllyBlue
                                                        )
                                                    } else {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(if (isCompact) 26.dp else 30.dp)
                                                                .clip(CircleShape)
                                                                .background(HextechDarkBg)
                                                                .border(1.2.dp, AllyBlue, CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = com.example.util.tr(target.take(2).uppercase()),
                                                                color = TextPrimary,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.size(48.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Débil Contra (Debilidad)
                Card(
                    modifier = Modifier.weight(1f).testTag("weakness_insight_card"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = HextechSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val counteredList = roleProfile.counteredBy.distinct().take(maxMatchupCount)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = com.example.util.tr("${tr("Débil")} (${counteredList.size})"),
                                color = DangerRed,
                                fontSize = if (isCompact) 9.5.sp else 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!isPremium) {
                                Text(
                                    text = "PRO 12",
                                    color = HextechGold,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val counteredChunks = counteredList.chunked(if (isCompact) 1 else 2)
                        if (counteredList.isEmpty()) {
                            Text(com.example.util.tr("—"), color = TextMuted, fontSize = 11.sp)
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                counteredChunks.forEach { rowChampions ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        for (col in 0 until (if (isCompact) 1 else 2)) {
                                            val counter = rowChampions.getOrNull(col)
                                            if (counter != null) {
                                                val targetChamp = resolveTargetChampion(counter)
                                                val matchupName = targetChamp?.getLocalizedName(currentLang) ?: tr(counter)
                                                Box(
                                                    modifier = Modifier.size(48.dp).testTag("build_matchup_name_weakness")
                                                        .semantics { contentDescription = matchupName }
                                                        .coachClickable {
                                                            selectedMatchupName = matchupName
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (targetChamp != null) {
                                                        ChampionAvatar(
                                                            champion = targetChamp,
                                                            size = if (isCompact) 26.dp else 30.dp,
                                                            showTierBadge = false,
                                                            borderColor = DangerRed
                                                        )
                                                    } else {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(if (isCompact) 26.dp else 30.dp)
                                                                .clip(CircleShape)
                                                                .background(HextechDarkBg)
                                                                .border(1.2.dp, DangerRed, CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = com.example.util.tr(counter.take(2).uppercase()),
                                                                color = TextPrimary,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.size(48.dp))
                                            }
                                        }
                                    }
                                }
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val rawSynergies = if (roleProfile.synergies.isNotEmpty()) {
                            roleProfile.synergies
                        } else if (champion.synergies.isNotEmpty()) {
                            champion.synergies
                        } else {
                            synergyProfile.bestTeammates.map { it.championName }
                        }
                        val synergyList = rawSynergies.distinct().take(maxMatchupCount)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = com.example.util.tr("${tr("Sinergia")} (${synergyList.size})"),
                                color = HextechGold,
                                fontSize = if (isCompact) 9.5.sp else 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!isPremium) {
                                Text(
                                    text = "PRO 12",
                                    color = HextechGold,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val synergyChunks = synergyList.chunked(if (isCompact) 1 else 2)
                        if (synergyList.isEmpty()) {
                            Text(com.example.util.tr("—"), color = TextMuted, fontSize = 11.sp)
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                synergyChunks.forEach { rowChampions ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        for (col in 0 until (if (isCompact) 1 else 2)) {
                                            val partner = rowChampions.getOrNull(col)
                                            if (partner != null) {
                                                val targetChamp = resolveTargetChampion(partner)
                                                val matchupName = targetChamp?.getLocalizedName(currentLang) ?: tr(partner)
                                                Box(
                                                    modifier = Modifier.size(48.dp).testTag("build_matchup_name_synergy")
                                                        .semantics { contentDescription = matchupName }
                                                        .coachClickable {
                                                            selectedMatchupName = matchupName
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (targetChamp != null) {
                                                        ChampionAvatar(
                                                            champion = targetChamp,
                                                            size = if (isCompact) 26.dp else 30.dp,
                                                            showTierBadge = false,
                                                            borderColor = HextechGold
                                                        )
                                                    } else {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(if (isCompact) 26.dp else 30.dp)
                                                                .clip(CircleShape)
                                                                .background(HextechDarkBg)
                                                                .border(1.2.dp, HextechGold, CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = com.example.util.tr(partner.take(2).uppercase()),
                                                                color = TextPrimary,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.size(48.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!isSignedIn) {
                Spacer(Modifier.height(10.dp))
                Text(com.example.util.localizedString(com.example.R.string.matchup_sign_in_hint),
                    color = TextMuted, fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth().testTag("matchup_sign_in_hint"))
            }

            selectedMatchupName?.let { name ->
                androidx.compose.ui.window.Popup(
                    popupPositionProvider = object : androidx.compose.ui.window.PopupPositionProvider {
                        override fun calculatePosition(anchorBounds: androidx.compose.ui.unit.IntRect,
                            windowSize: androidx.compose.ui.unit.IntSize, layoutDirection: androidx.compose.ui.unit.LayoutDirection,
                            popupContentSize: androidx.compose.ui.unit.IntSize) = androidx.compose.ui.unit.IntOffset(
                                (windowSize.width - popupContentSize.width) / 2,
                                (windowSize.height - popupContentSize.height) / 2)
                    },
                    onDismissRequest = { selectedMatchupName = null },
                    properties = androidx.compose.ui.window.PopupProperties(focusable = true)
                ) {
                    androidx.compose.material3.Surface(
                        shape = RoundedCornerShape(12.dp), color = HextechSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, HextechGold)
                    ) {
                        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(name, Modifier.testTag("build_matchup_visible_name"), color = TextPrimary,
                                fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            TextButton(onClick = { selectedMatchupName = null }) { Text(tr("Cerrar")) }
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
                .coachClickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .coachClickable(
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
                .coachClickable(
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
                    .coachClickable(
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

    buildAdvice?.let { (title, advice) ->
        AdaptiveDetailAlertDialog(
            isOverlay = isOverlay,
            onDismissRequest = { buildAdvice = null },
            title = { Text(tr(title), color = HextechGold) },
            text = { Text(tr(advice), color = TextPrimary, modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) },
            confirmButton = { TextButton(onClick = { buildAdvice = null }) { Text(tr("Cerrar")) } }
        )
    }


    // ==========================================
    // DIALOG DE DETALLE DE OBJETO SITUACIONAL
    // ==========================================
    if (selectedSituationalItem != null) {
        val itemName = selectedSituationalItem!!
        val currentLang = com.example.util.currentAppLanguage()
        val advice = SituationalItemAdvisor.getAdvice(itemName, currentLang)

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
                    // Situación recomendada
                    Column {
                        Text(
                            text = tr("Situación de uso recomendada:"),
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        FormattedWildRiftText(
                            text = tr(advice.purpose),
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
                                        text = com.example.util.tr("️ $target"),
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
                            text = tr(advice.keyEffect),
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
                            text = com.example.util.tr(" ${tr(advice.recommendationTip)}"),
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
                    val lang = com.example.util.currentAppLanguage()
                    val localizedName = item.getLocalizedName(lang)
                    val statsList = item.getStatsList(lang)
                    val localizedPassive = item.getLocalizedPassive(lang)

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
                        text = com.example.util.tr(localizedName),
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
                                text = com.example.util.tr(" ${item.goldCost} ${tr("Oro")}"),
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
                                        text = com.example.util.tr(stat.parseHtmlColorToAnnotatedString()),
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

                    BuildElementCoachAdvice(selectedElementAdvice)
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                            .background(com.example.ui.theme.HextechCyan)
                            .coachClickable { itemForDetail = null }
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
                    .coachClickable(
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
                        contentDescription = rune.getLocalizedName(com.example.util.currentAppLanguage()),
                        fallbackText = rune.getLocalizedName(com.example.util.currentAppLanguage()),
                        modifier = Modifier.size(48.dp),
                        borderColor = com.example.ui.theme.HextechGold,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = rune.getLocalizedName(com.example.util.currentAppLanguage()),
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
                Column(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                    FormattedWildRiftText(
                        text = rune.getLocalizedDescription(com.example.util.currentAppLanguage()),
                        color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp
                    )
                    BuildElementCoachAdvice(selectedElementAdvice)
                }
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
                        contentDescription = spell.getLocalizedName(com.example.util.currentAppLanguage()),
                        fallbackText = spell.getLocalizedName(com.example.util.currentAppLanguage()),
                        modifier = Modifier.size(48.dp),
                        borderColor = com.example.ui.theme.HextechGold,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = spell.getLocalizedName(com.example.util.currentAppLanguage()),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                    val presentation = com.example.util.SpellCatalogFormatting.split(
                        spell.getLocalizedDescription(currentLang), currentLang
                    )
                    com.example.ui.components.SpellMapLabels(presentation.mapLabels)
                    Spacer(Modifier.height(8.dp))
                    FormattedWildRiftText(
                        text = presentation.description,
                        color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp
                    )
                    if (!com.example.util.BuildElementAdvice.isFlash(spell.name)) BuildElementCoachAdvice(selectedElementAdvice)
                }
            },
            confirmButton = {
                TextButton(onClick = { spellForDetail = null }) {
                    Text(tr("Cerrar"), color = com.example.ui.theme.HextechCyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

}

private fun resolveTargetChampion(nameOrId: String): Champion? {
    val clean = nameOrId.trim()
    return WildRiftRepository.getChampionByName(clean)
        ?: WildRiftRepository.getChampionById(clean.lowercase().replace(" ", "_").replace("-", "_").replace("'", ""))
        ?: WildRiftRepository.champions.find {
            it.name.equals(clean, ignoreCase = true) ||
            it.id.equals(clean, ignoreCase = true) ||
            it.ddragonId.equals(clean, ignoreCase = true)
        }
}

private fun getSituationalItemExplanation(itemName: String): String {
    val clean = itemName.lowercase()
    return when {
        clean.contains("malmortius") || clean.contains("fauces") -> "Usar contra composiciones con daño mágico pesado o asesinos AP de ráfaga (ej. Akali, Lux, Veigar) para activar un escudo protector salvavidas."
        clean.contains("ángel") || clean.contains("angel") || clean.contains("guardian") -> "Usar en el juego tardío o frente a composiciones con alto daño de dive para garantizar una segunda oportunidad en peleas de equipo decisivas."
        clean.contains("corta") || clean.contains("morellonomicón") || clean.contains("morellonomicon") || clean.contains("recordatorio letal") || clean.contains("recordatorio mortal") -> "Usar contra campeones con alta regeneración de salud, robo de vida o sanación continua (ej. Dr. Mundo, Soraka, Aatrox, Yuumi) para aplicar heridas graves."
        clean.contains("espinas") || clean.contains("thornmail") -> "Usar frente a atacantes físicos constantes y duelistas con curaciones en línea para devolver daño y frenar su sostenimiento."
        clean.contains("mercurio") || clean.contains("trituradoras") || clean.contains("treads") -> "Usar frente a equipos con múltiples habilidades de control de masas pesado (aturdimientos, ralentizaciones, provocaciones) y magos de control (ej. Morgana, Lux, Ashe)."
        clean.contains("blindada") || clean.contains("avance") || clean.contains("steelcaps") -> "Usar contra tiradores enemigos (ADCs) y duelistas con alto daño físico constante basado en ataques básicos directos."
        clean.contains("codiciosa") || clean.contains("inmortal") -> "Usar cuando requieras omnivampirismo prolongado, sustentación de vida en duelos largos y capacidad de supervivencia adaptativa."
        clean.contains("jonia") || clean.contains("lucidez") || clean.contains("carmesí") -> "Usar con magos, soportes o tiradores basados en habilidades para maximizar la aceleración de enfriamiento de habilidades y hechizos de invocador."
        clean.contains("dinámica") || clean.contains("dinamica") || clean.contains("quebrantarmadura") -> "Usar contra objetivos con armadura moderada para maximizar la penetración física temprana y ganar velocidad en rotaciones rápidas."
        clean.contains("maná") || clean.contains("mana") || clean.contains("lanzahechizos") -> "Usar con magos de alto gasto de maná y daño de ráfaga para amplificar la penetración mágica y acelerar la limpieza de oleadas."
        clean.contains("berserker") || clean.contains("metal") || clean.contains("gunmetal") -> "Usar con tiradores y duelistas de ataque rápido para maximizar la velocidad de ataque y optimizar el daño continuo."
        clean.contains("colmillo") || clean.contains("serpiente") -> "Usar contra composiciones con exceso de escudos protectores (ej. Sett, Karma, Lulu, Shen) para reducirlos drásticamente al impactar."
        clean.contains("randuin") -> "Usar contra tiradores críticos y campeones con daño crítico masivo (ej. Yasuo, Yone, Jinx, Caitlyn) para mitigar el impacto y reducir su velocidad de ataque."
        clean.contains("naturaleza") -> "Usar frente a equipos con dos o más magos de daño mágico continuo en el tiempo o quemaduras (ej. Brand, Aurelion Sol, Swain, Lillia)."
        clean.contains("zhonya") || clean.contains("estasis") -> "Usar para esquivar habilidades definitivas fatales y combos explosivos de asesinos mediante 2.5 segundos de invulnerabilidad."
        clean.contains("sterak") -> "Usar con luchadores y colosos para obtener un gran escudo de vida al recibir daño crítico y aumentar la tenacidad en peleas."
        clean.contains("serylda") -> "Usar para penetrar armaduras y aplicar ralentización continua con habilidades, facilitando el kiteo y persecución de tanques."
        clean.contains("dominik") -> "Usar con tiradores frente a equipos con múltiples tanques y colosos de alta vida para maximizar el daño por golpe crítico."
        clean.contains("corona") || clean.contains("fragmentada") -> "Usar con magos contra asesinos o iniciadores para reducir drásticamente el daño recibido al iniciar un enfrentamiento."
        else -> "Usar como reemplazo táctico para contrarrestar las mayores amenazas del equipo rival según la composición de la partida."
    }
}


@Composable
private fun BuildElementCoachAdvice(advice: String) {
    if (advice.isBlank()) return
    Spacer(Modifier.height(14.dp))
    androidx.compose.material3.Surface(modifier = Modifier.fillMaxWidth().testTag("build_element_advice_card"),
        shape = RoundedCornerShape(8.dp), color = HextechSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f))) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(tr("Consejo del coach"), color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            FormattedWildRiftText(text = tr(advice), color = TextPrimary, fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}
