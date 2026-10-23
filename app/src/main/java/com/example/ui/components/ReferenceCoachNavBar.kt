package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppThemeManager
import com.example.ui.theme.CoachNavStyle
import com.example.util.tr

/** Reference-inspired indicators use the active Coach palette in every destination. */
@Composable
fun ReferenceCoachNavBar(items: List<CoachNavItem>, selectedIndex: Int, style: CoachNavStyle,
    onSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    if (items.isEmpty()) return
    val index = selectedIndex.coerceIn(items.indices)
    val position = animateFloatAsState(index.toFloat(), spring(.72f, 380f), label = "reference_indicator")
    val background = AppThemeManager.getNavBarBackgroundColor()
    val accent = AppThemeManager.getNavBarAccentColor()
    val selectedColor = AppThemeManager.getNavBarSelectedIconColor()
    val inactive = AppThemeManager.getNavBarUnselectedColor()
    val expanded = style == CoachNavStyle.CAPSULE || style == CoachNavStyle.ACTIVE_LABEL
    val videoStyle = style == CoachNavStyle.GLASS_PILL || style == CoachNavStyle.TABS_V2 ||
        style == CoachNavStyle.PREMIUM_MENU || style == CoachNavStyle.PURPLE_MESSAGES ||
        style == CoachNavStyle.MENISCUS
    val weights = items.indices.map { if (expanded && it == index) 2f else 1f }
    val animatedWeights = weights.mapIndexed { i, weight ->
        animateFloatAsState(weight, spring(.8f, 400f), label = "reference_width_$i")
    }
    Box(modifier.fillMaxWidth().height(if (videoStyle) 72.dp else 76.dp)
        .clip(if (videoStyle) RoundedCornerShape(28.dp) else RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
        .background(background)) {
        Canvas(Modifier.fillMaxSize()) {
            val slot = size.width / items.size
            val x = slot * (position.value + .5f)
            val y = if (videoStyle) size.height / 2f else 28.dp.toPx()
            when (style) {
                CoachNavStyle.RUNIC_TILE -> drawRoundRect(
                    Brush.verticalGradient(listOf(accent.copy(alpha = .38f), selectedColor.copy(alpha = .2f))),
                    Offset(x - 23.dp.toPx(), 5.dp.toPx()), Size(46.dp.toPx(), 46.dp.toPx()), CornerRadius(12.dp.toPx()))
                CoachNavStyle.TOP_BEACON -> drawRoundRect(accent, Offset(x - 15.dp.toPx(), 0f),
                    Size(30.dp.toPx(), 4.dp.toPx()), CornerRadius(3.dp.toPx()))
                CoachNavStyle.UNDERLINE -> {
                    drawRoundRect(accent.copy(alpha = .18f), Offset(x - 28.dp.toPx(), 69.dp.toPx()),
                        Size(56.dp.toPx(), 6.dp.toPx()), CornerRadius(3.dp.toPx()))
                    drawRoundRect(accent, Offset(x - 23.dp.toPx(), 71.dp.toPx()),
                        Size(46.dp.toPx(), 2.dp.toPx()), CornerRadius(1.dp.toPx()))
                }
                CoachNavStyle.ORBIT -> {
                    drawCircle(accent.copy(alpha = .1f), 24.dp.toPx(), Offset(x, y))
                    drawCircle(accent, 22.dp.toPx(), Offset(x, y), style = Stroke(1.6.dp.toPx()))
                }
                CoachNavStyle.GLASS_PILL -> {
                    drawRoundRect(accent.copy(alpha = .12f), Offset(x - 31.dp.toPx(), 8.dp.toPx()),
                        Size(62.dp.toPx(), 54.dp.toPx()), CornerRadius(27.dp.toPx()))
                    drawRoundRect(accent.copy(alpha = .85f), Offset(x - 21.dp.toPx(), 65.dp.toPx()),
                        Size(42.dp.toPx(), 2.dp.toPx()), CornerRadius(1.dp.toPx()))
                }
                CoachNavStyle.TABS_V2 -> {
                    drawCircle(accent.copy(alpha = .28f), 21.dp.toPx(), Offset(x, y))
                    drawCircle(accent.copy(alpha = .95f), 17.dp.toPx(), Offset(x, y), style = Stroke(1.3.dp.toPx()))
                }
                CoachNavStyle.PREMIUM_MENU -> {
                    drawRoundRect(accent.copy(alpha = .85f), Offset(x - 18.dp.toPx(), 64.dp.toPx()),
                        Size(36.dp.toPx(), 2.dp.toPx()), CornerRadius(1.dp.toPx()))
                }
                CoachNavStyle.PURPLE_MESSAGES -> {
                    drawCircle(accent.copy(alpha = .28f), 20.dp.toPx(), Offset(x, y))
                    drawCircle(accent, 3.dp.toPx(), Offset(x, size.height - 5.dp.toPx()))
                }
                CoachNavStyle.MENISCUS -> {
                    drawCircle(accent.copy(alpha = .3f), 23.dp.toPx(), Offset(x, y))
                    drawCircle(accent.copy(alpha = .9f), 15.dp.toPx(), Offset(x, y))
                }
                else -> Unit
            }
        }
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            items.forEachIndexed { i, item ->
                val selected = i == index
                val tint = if (selected) selectedColor else inactive
                val itemModifier = Modifier.weight(animatedWeights[i].value).fillMaxHeight()
                    .clickable { onSelected(i) }.testTag("coach_nav_item_$i")
                Column(itemModifier, horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    Row(Modifier.height(48.dp).padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (style == CoachNavStyle.CAPSULE && selected) accent.copy(alpha = .25f) else Color.Transparent)
                        .padding(horizontal = if (expanded && selected) 10.dp else 4.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        if (i == 4 && items.size == 5) UserNotificationIcon(item.notificationCount ?: userPanelNotificationCount(), tint)
                        else Icon(item.icon, tr(item.title), Modifier.size(if (videoStyle) 21.dp else 24.dp), tint = tint)
                        if ((expanded && selected) || (style == CoachNavStyle.GLASS_PILL && selected)) Text(tr(item.title), modifier = Modifier.padding(start = 6.dp),
                            color = tint, fontSize = 10.sp, maxLines = 1, fontWeight = FontWeight.Bold)
                    }
                    if (!expanded && !videoStyle) Text(tr(item.title), modifier = Modifier.height(20.dp), color = if (selected) AppThemeManager.getNavBarSelectedTextColor() else inactive,
                        fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
                }
            }
        }
    }
}
