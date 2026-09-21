package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppUserRole

enum class RoleBadgeSize {
    COMPACT, // Para tarjetas de usuarios y listas
    NORMAL,  // Para cabeceras y perfiles
    LARGE    // Para paneles de selección y detalles destacados
}

private data class BadgeMetrics(
    val fontSize: TextUnit,
    val hPad: Dp,
    val vPad: Dp,
    val iconSize: TextUnit
)

@Composable
fun RoleBadge(
    role: String,
    isPremiumActive: Boolean = false,
    isBanned: Boolean = false,
    isExpiringSoon: Boolean = false,
    size: RoleBadgeSize = RoleBadgeSize.COMPACT,
    modifier: Modifier = Modifier
) {
    val appRole = when {
        isBanned -> AppUserRole.BANNED
        role.equals("admin", ignoreCase = true) -> AppUserRole.ADMIN
        role.equals("moderador", ignoreCase = true) -> AppUserRole.MODERATOR
        role.equals("creador_vip", ignoreCase = true) -> AppUserRole.CREATOR_VIP
        role.equals("streamer", ignoreCase = true) -> AppUserRole.STREAMER
        role.equals("creador", ignoreCase = true) -> AppUserRole.CREATOR
        role.equals("premium", ignoreCase = true) -> AppUserRole.PREMIUM
        isPremiumActive -> AppUserRole.PREMIUM
        else -> AppUserRole.fromId(role)
    }

    // Animaciones constantes de pulso, aura y shimmer
    val infiniteTransition = rememberInfiniteTransition(label = "roleBadgeAnim_${appRole.id}")

    // 1. Pulso de luminosidad y aura
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // 2. Respiración sutil de escala (efecto orgánico y táctico)
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (appRole == AppUserRole.FREE) 1.01f else 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // 3. Shimmer continuo que recorre el fondo del badge
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -150f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    // Transición animada al cambiar de rol
    AnimatedContent(
        targetState = appRole,
        transitionSpec = {
            (fadeIn(animationSpec = tween(280)) + scaleIn(initialScale = 0.82f))
                .togetherWith(fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 0.82f))
        },
        label = "roleTransition"
    ) { currentRoleState ->
        val metrics = when (size) {
            RoleBadgeSize.COMPACT -> BadgeMetrics(9.5.sp, 6.dp, 2.dp, 10.5.sp)
            RoleBadgeSize.NORMAL -> BadgeMetrics(11.5.sp, 9.dp, 4.dp, 12.5.sp)
            RoleBadgeSize.LARGE -> BadgeMetrics(13.5.sp, 13.dp, 6.dp, 15.sp)
        }

        val badgeEmoji = if (isExpiringSoon && currentRoleState != AppUserRole.ADMIN && !isBanned) "⚠️" else currentRoleState.emoji
        val badgeText = if (isExpiringSoon && currentRoleState != AppUserRole.ADMIN && !isBanned) "EXPIRA PRONTO" else currentRoleState.displayName.uppercase()
        val badgeColor = if (isExpiringSoon && currentRoleState != AppUserRole.ADMIN && !isBanned) Color(0xFFEF4444) else currentRoleState.primaryColor

        val baseBg = badgeColor.copy(alpha = 0.16f)
        val highlightBg = badgeColor.copy(alpha = (0.35f * pulseAlpha).coerceIn(0.18f, 0.45f))
        val ambientBorderColor = badgeColor.copy(alpha = (0.4f + 0.55f * pulseAlpha).coerceIn(0.4f, 0.95f))

        val shimmerBrush = Brush.linearGradient(
            colors = listOf(
                baseBg,
                highlightBg,
                currentRoleState.secondaryColor.copy(alpha = 0.22f),
                baseBg
            ),
            start = Offset(shimmerOffset, 0f),
            end = Offset(shimmerOffset + 80f, 60f)
        )

        Box(
            modifier = modifier
                .scale(pulseScale)
                .clip(RoundedCornerShape(6.dp))
                .background(shimmerBrush)
                .border(
                    width = if (size == RoleBadgeSize.LARGE) 1.5.dp else 1.dp,
                    color = ambientBorderColor,
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(horizontal = metrics.hPad, vertical = metrics.vPad),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = badgeEmoji,
                    fontSize = metrics.iconSize,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = badgeText,
                    color = badgeColor,
                    fontSize = metrics.fontSize,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
