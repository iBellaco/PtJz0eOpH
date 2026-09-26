package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.imageLoader
import coil.request.CachePolicy
import com.example.model.Champion
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.TierAColor
import com.example.ui.theme.TierSColor
import com.example.ui.theme.TierSPlusColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import kotlinx.coroutines.launch

@Composable
fun ChampionAvatar(
    champion: Champion,
    size: Dp = 56.dp,
    showTierBadge: Boolean = true,
    borderColor: Color = HextechGold,
    modifier: Modifier = Modifier
) {
    // Animación fluida de escala y deslizamiento al seleccionar o cambiar de campeón
    val scaleAnim = remember(champion.id) { Animatable(0.68f) }
    val slideAnim = remember(champion.id) { Animatable(12f) }
    val alphaAnim = remember(champion.id) { Animatable(0.2f) }

    LaunchedEffect(champion.id) {
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
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
            )
        }
    }

    val avatarBrush = when (champion.id) {
        "morgana" -> Brush.radialGradient(listOf(Color(0xFF8B5CF6), Color(0xFF2E1065), Color(0xFF0F051D)))
        "viego" -> Brush.radialGradient(listOf(Color(0xFF00F2FE), Color(0xFF005A82), Color(0xFF071426)))
        "nautilus" -> Brush.radialGradient(listOf(Color(0xFFD97706), Color(0xFF78350F), Color(0xFF1E1B18)))
        else -> Brush.radialGradient(listOf(Color(0xFF3A4B5C), Color(0xFF1E2A38), Color(0xFF0F1722)))
    }

    Box(
        contentAlignment = Alignment.BottomEnd, 
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
                translationY = slideAnim.value
                alpha = alphaAnim.value
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(HextechDarkBg)
                .border(2.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(avatarBrush),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = champion.name.take(2).uppercase(),
                    color = Color.White,
                    fontSize = (size.value * 0.32).sp,
                    fontWeight = FontWeight.Bold
                )
            }

            val modelData: Any = when {
                champion.id.isNotBlank() -> android.net.Uri.parse("file:///android_asset/champions/${champion.id}.png")
                champion.avatarUrl.isNotBlank() -> {
                    val parsedUrl = champion.avatarUrl.trim()
                    when {
                        parsedUrl.startsWith("file:///android_asset/") -> android.net.Uri.parse(parsedUrl)
                        parsedUrl.startsWith("file://") -> java.io.File(parsedUrl.removePrefix("file://"))
                        else -> parsedUrl
                    }
                }
                else -> ""
            }
            
            if (modelData != "") {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(modelData)
                        .crossfade(true)
                        .placeholder(com.example.R.drawable.ic_placeholder_loading)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCacheKey(modelData.toString() + "_v1365")
                        .diskCacheKey(modelData.toString() + "_v1365")
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .listener(
                            onError = { request, result -> 
                                com.example.util.AppLogger.e("ImageLoader", "Failed to load ${request.data}: ${result.throwable.message}") 
                            }
                        )
                        .build(),
                    imageLoader = LocalContext.current.imageLoader,
                    contentDescription = champion.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(size - 4.dp)
                        .clip(CircleShape)
                )
            }
        }
        
        if (showTierBadge && (champion.tier.isNotBlank() || champion.cnTier.isNotBlank())) {
            val isCnMode = com.example.data.WildRiftRepository.activeRegionName == "CN"
            val displayTier = if (isCnMode && champion.cnTier.isNotBlank()) champion.cnTier else champion.tier
            val tierColor = when (displayTier) {
                "S+", "T0" -> TierSPlusColor
                "S", "T1" -> TierSColor
                "A+", "T2" -> TierAColor
                "A", "T3" -> Color(0xFF4CAF50)
                "B", "T4" -> Color(0xFF8BC34A)
                "C" -> Color(0xFF8BC34A)
                else -> Color.Gray
            }
            Box(
                modifier = Modifier
                    .size(size * 0.35f)
                    .clip(CircleShape)
                    .background(HextechDarkBg)
                    .border(1.dp, HextechGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayTier,
                    color = tierColor,
                    fontSize = (size.value * 0.16).sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun AppAssetImage(
    url: String,
    contentDescription: String?,
    fallbackText: String,
    modifier: Modifier = Modifier,
    borderColor: Color = HextechGold,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp)
) {
    val context = LocalContext.current
    val parsedUrl = url.trim()

    // Animación fluida de selección / carga para imágenes de assets
    val scaleAnim = remember(url) { Animatable(0.72f) }
    val slideAnim = remember(url) { Animatable(10f) }
    val alphaAnim = remember(url) { Animatable(0.25f) }

    LaunchedEffect(url) {
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
    
    val modelData: Any? = when {
        parsedUrl.startsWith("file:///android_asset/") -> android.net.Uri.parse(parsedUrl)
        parsedUrl.startsWith("file://") -> java.io.File(parsedUrl.removePrefix("file://"))
        parsedUrl.isNotBlank() -> parsedUrl
        else -> null
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
                translationY = slideAnim.value
                alpha = alphaAnim.value
            }
            .clip(shape)
            .background(HextechDarkBg)
            .border(1.dp, borderColor, shape),
        contentAlignment = Alignment.Center
    ) {
        // Fallback initials underneath
        Text(
            text = fallbackText.take(2).uppercase(),
            color = borderColor.copy(alpha = 0.7f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        if (modelData != null && parsedUrl.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(modelData)
                    .crossfade(true)
                    .placeholder(com.example.R.drawable.ic_placeholder_loading)
                    .error(com.example.R.drawable.ic_placeholder_loading)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCacheKey(modelData.toString() + "_v1365")
                    .diskCacheKey(modelData.toString() + "_v1365")
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .listener(
                        onError = { request, result -> 
                            com.example.util.AppLogger.e("ImageLoader", "Failed to load ${request.data}: ${result.throwable.message}") 
                        }
                    )
                    .build(),
                imageLoader = context.imageLoader,
                contentDescription = contentDescription ?: fallbackText,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(shape)
            )
        }
    }
}
