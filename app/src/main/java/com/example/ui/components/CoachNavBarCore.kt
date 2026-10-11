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
import com.example.ui.theme.CoachNavStyle
import com.example.util.tr

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
    val active = items[index].accentColor
    val barShape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
    BoxWithConstraints(modifier.fillMaxWidth().height(76.dp).clip(barShape).background(backgroundColor)) {
        val density = androidx.compose.ui.platform.LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        Canvas(Modifier.fillMaxSize()) {
            val slotWidth = size.width / items.size
            val centerX = slotWidth * (position.value + .5f)
            val centerY = if (style == CoachNavStyle.LIQUID) size.height / 2f else 28.dp.toPx()
            when (style) {
                CoachNavStyle.ORIGINAL -> Unit
                CoachNavStyle.ARC_GLOWING -> {
                    drawCircle(active.copy(alpha = .12f), 22.dp.toPx(), Offset(centerX, centerY))
                    drawCircle(active.copy(alpha = .48f), 17.dp.toPx(), Offset(centerX, centerY), style = Stroke(6.dp.toPx()))
                    drawCircle(Color.White.copy(alpha = .9f), 13.dp.toPx(), Offset(centerX, centerY), style = Stroke(1.4.dp.toPx()))
                }
                CoachNavStyle.LIQUID -> {
                    val w = 46.dp.toPx(); val h = 26.dp.toPx()
                    val path = Path().apply {
                        moveTo(centerX - w / 2, centerY)
                        cubicTo(centerX - w / 2, centerY - h, centerX + w / 2, centerY - h, centerX + w / 2, centerY)
                        cubicTo(centerX + w / 2, centerY + h, centerX - w / 2, centerY + h, centerX - w / 2, centerY)
                        close()
                    }
                    drawPath(path, active.copy(alpha = .2f)); drawPath(path, active.copy(alpha = .85f), style = Stroke(1.5.dp.toPx()))
                }
                CoachNavStyle.NEON_WAVE -> {
                    val path = Path().apply {
                        moveTo(0f, size.height - 3.dp.toPx())
                        cubicTo(centerX - slotWidth * .28f, size.height - 3.dp.toPx(), centerX - slotWidth * .22f, size.height - 28.dp.toPx(), centerX, size.height - 28.dp.toPx())
                        cubicTo(centerX + slotWidth * .22f, size.height - 28.dp.toPx(), centerX + slotWidth * .28f, size.height - 3.dp.toPx(), size.width, size.height - 3.dp.toPx())
                    }
                    drawPath(path, active.copy(alpha = .25f), style = Stroke(8.dp.toPx(), cap = StrokeCap.Round))
                    drawPath(path, active, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
                }
            }
        }
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            items.forEachIndexed { itemIndex, item ->
                val selected = itemIndex == index
                val iconScale by animateFloatAsState(if (selected) 1.1f else 1f, spring(dampingRatio = .62f), label = "nav_icon_scale_$itemIndex")
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight()
                        .clickable { onSelected(itemIndex) }.testTag("coach_nav_item_$itemIndex")
                        .padding(top = if (style == CoachNavStyle.LIQUID) 0.dp else 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Box(
                        modifier = if (style == CoachNavStyle.LIQUID) Modifier.fillMaxSize() else Modifier.fillMaxWidth().height(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (itemIndex == 4 && items.size == 5) UserNotificationIcon(item.notificationCount ?: userPanelNotificationCount())
                        else Icon(item.icon, contentDescription = tr(item.title), tint = if (selected) selectedColor else unselectedColor,
                            modifier = Modifier.size(21.dp).graphicsLayer { scaleX = iconScale; scaleY = iconScale })
                    }
                    if (style != CoachNavStyle.LIQUID) {
                        Text(tr(item.title), color = if (selected) selectedColor else unselectedColor, fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
                    }
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
    CoachNavItem("Usuario", Icons.Default.Person, com.example.ui.theme.AppThemeManager.getNavBarAccentColor())
)
