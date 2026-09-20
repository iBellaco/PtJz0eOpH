package com.example.ui.components

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
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
import com.example.data.local.CustomChampionBuildRecord
import com.example.ui.theme.*
import com.example.util.CreatorSubscriptionManager
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorProfileDialog(
    creatorName: String,
    creatorUid: String = "",
    avatarId: String = "default_poro",
    rankBorder: String = "NONE",
    secondaryRole: String = "none",
    equippedFrame: String = "AUTO",
    isAdmin: Boolean = false,
    role: String = "creador",
    isVerified: Boolean = true,
    creatorBuilds: List<CustomChampionBuildRecord> = emptyList(),
    onSelectBuild: (CustomChampionBuildRecord) -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        CreatorSubscriptionManager.init(context)
    }

    val subscribedSet by CreatorSubscriptionManager.subscribedCreatorKeys.collectAsStateWithLifecycle()
    val creatorKey = remember(creatorUid, creatorName) {
        if (creatorUid.isNotBlank()) creatorUid else creatorName.trim().lowercase(Locale.ROOT)
    }
    val isSubscribed = remember(subscribedSet, creatorKey) {
        subscribedSet.contains(creatorKey.lowercase(Locale.ROOT)) || subscribedSet.contains(creatorName.trim().lowercase(Locale.ROOT))
    }

    val totalVotes = remember(creatorBuilds) { creatorBuilds.sumOf { it.voteCount } }
    val avgRating = remember(creatorBuilds) {
        if (creatorBuilds.isNotEmpty()) {
            val totalSum = creatorBuilds.sumOf { it.ratingSum }
            if (totalVotes > 0) totalSum / totalVotes else 5.0
        } else 5.0
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = HextechDarkBg,
        scrimColor = Color.Black.copy(alpha = 0.75f),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Cabecera del Perfil con Botón Cerrar
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
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = HextechCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Perfil del Creador",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .background(HextechCardBorder.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tarjeta Principal del Creador (Avatar, Marcos, Nombre, Rol y Botón de Suscribirse)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = HextechSurface.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar con Marco Completo
                    Box(
                        modifier = Modifier.size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        UserAvatarView(
                            avatarId = avatarId,
                            size = 72.dp,
                            fallbackInitial = creatorName.take(1).uppercase(Locale.ROOT),
                            rankBorder = rankBorder,
                            secondaryRole = secondaryRole,
                            equippedFrame = equippedFrame,
                            isAdmin = isAdmin,
                            fitFrameToSize = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Nombre e Insignia de Verificación
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = creatorName,
                            color = HextechGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (isVerified || isAdmin) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verificado",
                                tint = HextechCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Rol Oficial y Etiquetas
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val roleTagText = when (role.lowercase(Locale.ROOT)) {
                            "admin" -> "ADMINISTRADOR"
                            "creador_vip" -> "CREADOR VIP"
                            "streamer" -> "STREAMER OFICIAL"
                            else -> "CREADOR OFICIAL"
                        }
                        val roleTagColor = when (role.lowercase(Locale.ROOT)) {
                            "admin" -> Color(0xFFFF4655)
                            "creador_vip" -> Color(0xFFA855F7)
                            "streamer" -> Color(0xFF3B82F6)
                            else -> HextechGold
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = roleTagColor.copy(alpha = 0.18f),
                            border = BorderStroke(0.8.dp, roleTagColor.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = roleTagText,
                                color = roleTagColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
                            )
                        }

                        if (secondaryRole.isNotBlank() && secondaryRole != "none") {
                            val secRoleObj = com.example.model.AppUserSecondaryRole.fromId(secondaryRole)
                            if (secRoleObj != com.example.model.AppUserSecondaryRole.NONE) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = secRoleObj.primaryColor.copy(alpha = 0.18f),
                                    border = BorderStroke(0.8.dp, secRoleObj.primaryColor.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = secRoleObj.displayName,
                                        color = secRoleObj.primaryColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Estadísticas Rápidas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${creatorBuilds.size}",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Builds",
                                color = TextSecondary,
                                fontSize = 10.5.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(HextechCardBorder)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = HextechGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = String.format(Locale.US, "%.1f", avgRating),
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Valoración",
                                color = TextSecondary,
                                fontSize = 10.5.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(HextechCardBorder)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$totalVotes",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Votos",
                                color = TextSecondary,
                                fontSize = 10.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // BOTÓN PRINCIPAL DE SUSCRIPCIÓN AL CREADOR / STREAMER
                    Button(
                        onClick = {
                            CreatorSubscriptionManager.toggleSubscription(
                                creatorKey,
                                creatorName,
                                context
                            ) { newlySubscribed ->
                                val msg = if (newlySubscribed) {
                                    "Te has suscrito a $creatorName"
                                } else {
                                    "Has cancelado la suscripcion a $creatorName"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSubscribed) Color(0xFF1E293B) else HextechGold,
                            contentColor = if (isSubscribed) HextechGold else Color.Black
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSubscribed) HextechGold.copy(alpha = 0.8f) else HextechGold
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isSubscribed) Icons.Default.Check else Icons.Default.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isSubscribed) "Suscrito" else "Suscribirse al Creador",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Título de Builds del Creador
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Builds Publicadas (${creatorBuilds.size})",
                    color = Color.White,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de Builds del Creador
            if (creatorBuilds.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Este creador aún no tiene builds publicadas.",
                        color = TextSecondary,
                        fontSize = 12.5.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(creatorBuilds) { record ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = HextechSurface,
                            border = BorderStroke(0.8.dp, HextechCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectBuild(record)
                                    onDismiss()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val champObj = remember(record.championId) {
                                        com.example.data.WildRiftRepository.champions.find { it.id == record.championId }
                                    }

                                    Box(modifier = Modifier.size(36.dp)) {
                                        if (champObj != null) {
                                            ChampionAvatar(
                                                champion = champObj,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.Gray, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = record.championName.take(1),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = if (record.buildTitle.isNotBlank()) record.buildTitle else "Build de ${record.championName}",
                                            color = Color.White,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${record.championName} • ${record.role}",
                                            color = HextechCyan,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = HextechGold,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    val rating = if (record.voteCount > 0) record.ratingSum / record.voteCount else 5.0
                                    Text(
                                        text = String.format(Locale.US, "%.1f", rating),
                                        color = HextechGold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
