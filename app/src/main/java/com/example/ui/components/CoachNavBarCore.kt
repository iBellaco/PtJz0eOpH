package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppThemeManager
import com.example.ui.theme.CoachNavStyle
import com.example.util.tr
import kotlin.math.min

data class CoachNavItem(val title: String, val icon: ImageVector, val accentColor: Color, val notificationCount: Int? = null)

@Composable
internal fun CoachNavBarCore(
    items: List<CoachNavItem>, selectedIndex: Int, style: CoachNavStyle,
    backgroundColor: Color, selectedColor: Color, unselectedColor: Color,
    onSelected: (Int) -> Unit, modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    val index = selectedIndex.coerceIn(items.indices)
    val position = animateFloatAsState(index.toFloat(), spring(dampingRatio = .72f, stiffness = 380f), label = "coach_nav_position")
    val activeIndicator = items[index].accentColor
    val selectionContainer = AppThemeManager.getNavBarSelectionContainerColor()
    val selectedLabelColor = AppThemeManager.getNavBarSelectedTextColor()
    val barShape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
    // Each variant owns its indicator.  A former generic circular icon surface
    // was being painted on top of every variant, which made otherwise distinct
    // styles look identical and added unnecessary height to the bar.
    BoxWithConstraints(modifier.fillMaxWidth().height(72.dp).clip(barShape).background(backgroundColor)) {
        Canvas(Modifier.fillMaxSize()) {
            val slotWidth = size.width / items.size
            val centerX = slotWidth * (position.value + .5f)
            val centerY = 24.dp.toPx()
            when (style) {
                CoachNavStyle.ORIGINAL -> Unit
                CoachNavStyle.ARC_GLOWING -> {
                    val radius = 18.dp.toPx()
                    drawCircle(selectionContainer.copy(alpha = .52f), radius, Offset(centerX, centerY))
                    drawArc(
                        color = activeIndicator,
                        startAngle = -215f,
                        sweepAngle = 250f,
                        useCenter = false,
                        topLeft = Offset(centerX - radius, centerY - radius),
                        size = Size(radius * 2f, radius * 2f),
                        style = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                CoachNavStyle.LIQUID -> {
                    val width = min(slotWidth * .84f, 64.dp.toPx())
                    val height = 34.dp.toPx()
                    val top = centerY - height / 2f
                    drawRoundRect(
                        selectionContainer.copy(alpha = .92f),
                        Offset(centerX - width / 2f, top),
                        Size(width, height),
                        androidx.compose.ui.geometry.CornerRadius(height / 2f)
                    )
                    drawRoundRect(
                        activeIndicator.copy(alpha = .86f),
                        Offset(centerX - width / 2f, top),
                        Size(width, height),
                        androidx.compose.ui.geometry.CornerRadius(height / 2f),
                        style = Stroke(1.5.dp.toPx())
                    )
                    drawLine(
                        Color.White.copy(alpha = .28f),
                        Offset(centerX - width * .26f, top + 7.dp.toPx()),
                        Offset(centerX + width * .2f, top + 7.dp.toPx()),
                        1.dp.toPx(),
                        StrokeCap.Round
                    )
                }
                CoachNavStyle.NEON_WAVE -> {
                    val width = min(slotWidth * .76f, 58.dp.toPx())
                    val top = centerY - 16.dp.toPx()
                    drawRoundRect(
                        selectionContainer.copy(alpha = .68f),
                        Offset(centerX - width / 2f, top),
                        Size(width, 32.dp.toPx()),
                        androidx.compose.ui.geometry.CornerRadius(14.dp.toPx())
                    )
                    val path = Path().apply {
                        val base = size.height - 5.dp.toPx()
                        moveTo(0f, base)
                        cubicTo(centerX - slotWidth * .28f, base, centerX - slotWidth * .22f, base - 18.dp.toPx(), centerX, base - 18.dp.toPx())
                        cubicTo(centerX + slotWidth * .22f, base - 18.dp.toPx(), centerX + slotWidth * .28f, base, size.width, base)
                    }
                    drawPath(path, activeIndicator.copy(alpha = .25f), style = Stroke(8.dp.toPx(), cap = StrokeCap.Round))
                    drawPath(path, activeIndicator, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
                }
                else -> Unit
            }
        }
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.Top) {
            items.forEachIndexed { itemIndex, item ->
                val selected = itemIndex == index
                val iconScale by animateFloatAsState(if (selected) 1.1f else 1f, spring(dampingRatio = .62f), label = "nav_icon_scale_$itemIndex")
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight()
                        .clickable { onSelected(itemIndex) }.testTag("coach_nav_item_$itemIndex")
                        .padding(top = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (itemIndex == 4 && items.size == 5) {
                                UserNotificationIcon(
                                    item.notificationCount ?: userPanelNotificationCount(),
                                    if (selected) selectedColor else unselectedColor
                                )
                            } else {
                                Icon(
                                    item.icon,
                                    contentDescription = tr(item.title),
                                    tint = if (selected) selectedColor else unselectedColor,
                                    modifier = Modifier.size(21.dp).graphicsLayer {
                                        scaleX = iconScale
                                        scaleY = iconScale
                                    }
                                )
                            }
                        }
                    }
                    Text(
                        tr(item.title),
                        modifier = Modifier.height(18.dp),
                        color = if (selected) selectedLabelColor else unselectedColor,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
internal fun CoachNavPreviewItems(): List<CoachNavItem> = listOf(
    CoachNavItem("Inicio", Icons.Default.Home, com.example.ui.theme.AppThemeManager.getNavBarAccentColor()),
    CoachNavItem("Selección", Icons.Default.Groups, com.example.ui.theme.AppThemeManager.getNavBarAccentColor()),
    CoachNavItem("Clasificación", Icons.Default.TrendingUp, com.example.ui.theme.AppThemeManager.getNavBarAccentColor()),
    CoachNavItem("Catálogo", Icons.AutoMirrored.Filled.MenuBook, com.example.ui.theme.AppThemeManager.getNavBarAccentColor()),
    CoachNavItem("Usuario", Icons.Default.Person, com.example.ui.theme.AppThemeManager.getNavBarAccentColor(), notificationCount = 0)
)
