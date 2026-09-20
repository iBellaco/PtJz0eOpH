package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.AvatarCatalog
import com.example.model.AvatarItem
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechGoldLight
import kotlinx.coroutines.launch

@Composable
fun UserAvatarView(
    avatarId: String?,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    fallbackInitial: String = "U",
    showBorder: Boolean = true,
    customBorderColor: Color? = null,
    rankBorder: String = "NONE",
    secondaryRole: String? = null,
    isAdmin: Boolean = false,
    adminFrameResId: Int = com.example.R.drawable.ic_frame_admin,
    adminFrameUrl: String? = null,
    equippedFrame: String = "AUTO"
) {
    val secRoleObj = remember(secondaryRole) {
        if (!secondaryRole.isNullOrBlank() && secondaryRole != "none") {
            com.example.model.AppUserSecondaryRole.fromId(secondaryRole)
        } else {
            com.example.model.AppUserSecondaryRole.NONE
        }
    }
    val secFrameRes = secRoleObj.frameDrawableRes

    val effectiveFrameType = remember(equippedFrame, isAdmin, secFrameRes, rankBorder) {
        when (equippedFrame.uppercase()) {
            "NONE" -> "NONE"
            "SECONDARY" -> if (secFrameRes != null) "SECONDARY" else "NONE"
            "SPECIAL", "RANK" -> if (isAdmin) "ADMIN" else if (rankBorder != "NONE" && rankBorder.isNotBlank()) "RANK" else "NONE"
            else -> { // "AUTO"
                if (isAdmin) "ADMIN"
                else if (secFrameRes != null) "SECONDARY"
                else if (rankBorder != "NONE" && rankBorder.isNotBlank()) "RANK"
                else "NONE"
            }
        }
    }

    val avatar: AvatarItem = AvatarCatalog.getAvatarById(avatarId ?: "default_poro")
    val parsedBorderColor = customBorderColor ?: try {
        Color(android.graphics.Color.parseColor(avatar.borderHex))
    } catch (e: Exception) {
        HextechGold
    }
    
    val rarityLower = avatar.rarity.lowercase()
    val borderWidth = when {
        rarityLower.contains("mítico") || rarityLower.contains("mitico") -> if (size > 60.dp) 3.5.dp else 2.5.dp
        rarityLower.contains("legendario") -> if (size > 60.dp) 3.dp else 2.dp
        rarityLower.contains("épico") || rarityLower.contains("epico") -> if (size > 60.dp) 2.5.dp else 1.5.dp
        rarityLower.contains("raro") -> if (size > 60.dp) 2.dp else 1.5.dp
        else -> 1.dp
    }
    
    val runicBorderBrush = when {
        rarityLower.contains("mítico") || rarityLower.contains("mitico") -> Brush.sweepGradient(listOf(Color(0xFFC4B5FD), Color(0xFF7C3AED), Color(0xFF5B21B6), Color(0xFFC4B5FD)))
        rarityLower.contains("legendario") -> Brush.sweepGradient(listOf(Color(0xFFFFD700), Color(0xFFB91C1C), Color(0xFF991B1B), Color(0xFFFFD700)))
        rarityLower.contains("épico") || rarityLower.contains("epico") -> Brush.sweepGradient(listOf(Color(0xFFE9D5FF), Color(0xFF9333EA), Color(0xFFE9D5FF)))
        rarityLower.contains("raro") -> Brush.linearGradient(listOf(Color(0xFF93C5FD), Color(0xFF2563EB), Color(0xFF93C5FD)))
        else -> Brush.linearGradient(listOf(parsedBorderColor, parsedBorderColor))
    }
    
    val actualShowBorder = showBorder

    val infiniteTransition = rememberInfiniteTransition(label = "SoberanoGlow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowPulse"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    val scaleAnim = remember(avatarId) { Animatable(0.7f) }
    val slideAnim = remember(avatarId) { Animatable(10f) }
    val alphaAnim = remember(avatarId) { Animatable(0.2f) }

    LaunchedEffect(avatarId) {
        launch {
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        launch {
            slideAnim.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        launch {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
                translationY = slideAnim.value
                alpha = alphaAnim.value
            },
        contentAlignment = Alignment.Center
    ) {
        // Círculo base del Avatar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF1E293B),
                            Color(0xFF0F172A),
                            HextechDarkBg
                        )
                    )
                )
                .then(
                    if (effectiveFrameType == "RANK") {
                        Modifier.rankedBorderPainter(
                            rank = rankBorder,
                            glowPulse = glowPulse,
                            rotation = rotation
                        )
                    } else if (actualShowBorder && effectiveFrameType == "NONE") {
                        val isCom = rarityLower == "común" || rarityLower == "comun" || rarityLower == "clásico"
                        if (!isCom) {
                            Modifier.premiumBorderPainter(
                                rarity = rarityLower
                            )
                        } else {
                            Modifier.border(
                                width = borderWidth,
                                brush = runicBorderBrush,
                                shape = CircleShape
                            )
                        }
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = fallbackInitial.take(1).uppercase(),
                color = HextechGoldLight,
                fontWeight = FontWeight.ExtraBold,
                fontSize = (size.value * 0.38f).sp,
                fontFamily = FontFamily.Serif
            )
            if (avatar.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(avatar.imageUrl)
                        .crossfade(true)
                        .placeholder(com.example.R.drawable.ic_placeholder_loading)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    contentDescription = avatar.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
        }

        // Marco exclusivo de Administrador o Rol Secundario (rodeando el avatar por fuera)
        if (effectiveFrameType == "ADMIN") {
            if (adminFrameResId != 0) {
                Image(
                    painter = painterResource(id = adminFrameResId),
                    contentDescription = "Marco de Administrador",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .requiredSize(size * 2.48f)
                        .offset(y = size * 0.09f)
                        .align(Alignment.Center)
                )
            } else if (!adminFrameUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(adminFrameUrl)
                        .crossfade(true)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    contentDescription = "Marco de Administrador",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .requiredSize(size * 2.48f)
                        .offset(y = size * 0.09f)
                        .align(Alignment.Center)
                )
            }
        } else if (effectiveFrameType == "SECONDARY" && secFrameRes != null) {
            Image(
                painter = painterResource(id = secFrameRes),
                contentDescription = "Marco de Rol Secundario (${secRoleObj.displayName})",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .requiredSize(size * 2.48f)
                    .offset(y = size * 0.09f)
                    .align(Alignment.Center)
            )
        }
    }
}

fun Modifier.rankedBorderPainter(rank: String, glowPulse: Float, rotation: Float): Modifier = this.drawWithCache {
    onDrawWithContent {
        drawContent()
        val cx = size.width / 2
        val cy = size.height / 2
        val r = size.width / 2
        
        when (rank.uppercase()) {
            "ESMERALDA", "EMERALD" -> {
                drawCircle(
                    brush = Brush.sweepGradient(listOf(Color(0xFF10B981), Color(0xFF047857), Color(0xFF34D399), Color(0xFF065F46), Color(0xFF10B981))),
                    radius = r - 2.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(width = 3.8.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFF34D399).copy(alpha = 0.45f + (0.35f * glowPulse)),
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }
            "DIAMANTE", "DIAMOND" -> {
                drawCircle(
                    brush = Brush.sweepGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFFE0F2FE), Color(0xFF0284C7), Color(0xFF38BDF8))),
                    radius = r - 2.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(width = 4.2.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFF7DD3FC).copy(alpha = 0.5f),
                    radius = r - 4.5.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
                )
            }
            "MASTER", "MAESTRO" -> {
                drawCircle(
                    brush = Brush.sweepGradient(listOf(Color(0xFFFF00FF), Color(0xFF8A2BE2), Color(0xFF4B0082), Color(0xFFFF00FF))),
                    radius = r - 2.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(width = 4.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFFFF00FF).copy(alpha = 0.5f),
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            "GRANDMASTER", "GRAN_MAESTRO", "GRAN MAESTRO" -> {
                drawCircle(
                    brush = Brush.sweepGradient(listOf(Color(0xFFFF4500), Color(0xFFDC143C), Color(0xFFFFD700), Color(0xFFFF4500))),
                    radius = r - 2.5.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(width = 5.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFFFFD700),
                    radius = r - 5.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                )
            }
            "CHALLENGER", "ASPIRANTE" -> {
                rotate(rotation) {
                    drawCircle(
                        brush = Brush.sweepGradient(listOf(Color(0xFF00FFFF), Color(0xFFFFD700), Color(0xFF00BFFF), Color(0xFFFFD700), Color(0xFF00FFFF))),
                        radius = r - 3.dp.toPx(),
                        center = Offset(cx, cy),
                        style = Stroke(width = 6.dp.toPx())
                    )
                }
                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = 0.3f + (0.4f * glowPulse)),
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(width = 8.dp.toPx())
                )
                val gemPath = Path().apply {
                    val gemR = 8.dp.toPx()
                    val gemY = size.height - 2.dp.toPx()
                    moveTo(cx, gemY - gemR)
                    lineTo(cx + gemR * 0.866f, gemY - gemR/2)
                    lineTo(cx + gemR * 0.866f, gemY + gemR/2)
                    lineTo(cx, gemY + gemR)
                    lineTo(cx - gemR * 0.866f, gemY + gemR/2)
                    lineTo(cx - gemR * 0.866f, gemY - gemR/2)
                    close()
                }
                drawPath(
                    path = gemPath,
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF00FFFF), Color(0xFF008080)),
                        center = Offset(cx, size.height - 2.dp.toPx()),
                        radius = 8.dp.toPx()
                    )
                )
                drawPath(
                    path = gemPath,
                    color = Color(0xFFFFD700),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
            "SOBERANO", "SOVEREIGN" -> {
                rotate(rotation * 1.25f) {
                    drawCircle(
                        brush = Brush.sweepGradient(listOf(Color(0xFF00F0FF), Color(0xFF818CF8), Color(0xFFFFD700), Color(0xFF38BDF8), Color(0xFF00F0FF))),
                        radius = r - 3.2.dp.toPx(),
                        center = Offset(cx, cy),
                        style = Stroke(width = 6.5.dp.toPx())
                    )
                }
                drawCircle(
                    color = Color(0xFF00F0FF).copy(alpha = 0.4f + (0.5f * glowPulse)),
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(width = 9.dp.toPx())
                )
                val gemPath = Path().apply {
                    val gemR = 9.dp.toPx()
                    val gemY = size.height - 2.dp.toPx()
                    moveTo(cx, gemY - gemR)
                    lineTo(cx + gemR * 0.866f, gemY - gemR/2)
                    lineTo(cx + gemR * 0.866f, gemY + gemR/2)
                    lineTo(cx, gemY + gemR)
                    lineTo(cx - gemR * 0.866f, gemY + gemR/2)
                    lineTo(cx - gemR * 0.866f, gemY - gemR/2)
                    close()
                }
                drawPath(
                    path = gemPath,
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFD700), Color(0xFF00F0FF)),
                        center = Offset(cx, size.height - 2.dp.toPx()),
                        radius = 9.dp.toPx()
                    )
                )
                drawPath(
                    path = gemPath,
                    color = Color(0xFF00F0FF),
                    style = Stroke(width = 1.8.dp.toPx())
                )
            }
        }
    }
}

fun Modifier.premiumBorderPainter(rarity: String): Modifier {
    val rarityLower = rarity.lowercase()
    val isMythic = rarityLower.contains("mítico") || rarityLower.contains("mitico")
    val isLegendary = rarityLower.contains("legendario")
    val isEpic = rarityLower.contains("épico") || rarityLower.contains("epico")
    
    return this.drawWithCache {
        val strokeWidth = when {
            isMythic -> 4.dp.toPx()
            isLegendary -> 3.5.dp.toPx()
            isEpic -> 3.dp.toPx()
            else -> 2.5.dp.toPx() // Raro
        }
        
        val primaryColor = when {
            isMythic -> Color(0xFFC4B5FD)
            isLegendary -> Color(0xFFFFD700)
            isEpic -> Color(0xFFE9D5FF)
            else -> Color(0xFF93C5FD) // Raro
        }
        val secondaryColor = when {
            isMythic -> Color(0xFF7C3AED)
            isLegendary -> Color(0xFFB91C1C)
            isEpic -> Color(0xFF9333EA)
            else -> Color(0xFF2563EB) // Raro
        }
        val darkColor = when {
            isMythic -> Color(0xFF4C1D95)
            isLegendary -> Color(0xFF7F1D1D)
            isEpic -> Color(0xFF6B21A8)
            else -> Color(0xFF1E3A8A) // Raro
        }
        
        val brush = Brush.sweepGradient(
            listOf(primaryColor, secondaryColor, darkColor, secondaryColor, primaryColor)
        )
        
        onDrawWithContent {
            drawContent()
            
            drawCircle(
                brush = brush,
                radius = size.width / 2 - strokeWidth / 2,
                center = Offset(size.width / 2, size.height / 2),
                style = Stroke(
                    width = strokeWidth,
                    pathEffect = when {
                        isMythic -> PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
                        isLegendary -> PathEffect.dashPathEffect(floatArrayOf(24f, 12f), 0f)
                        isEpic -> PathEffect.dashPathEffect(floatArrayOf(15f, 8f), 0f)
                        else -> PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f)
                    }
                )
            )
            
            drawCircle(
                color = when {
                    isMythic -> Color(0xFFE9D5FF)
                    isLegendary -> Color(0xFFFEF08A)
                    isEpic -> Color(0xFFD8B4FE)
                    else -> Color(0xFFBFDBFE)
                },
                radius = size.width / 2 - strokeWidth,
                center = Offset(size.width / 2, size.height / 2),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}
