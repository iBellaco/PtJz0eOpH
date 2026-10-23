package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppThemeManager
import com.example.ui.theme.CoachNavStyle
import com.example.util.tr
import kotlin.math.min
import kotlin.math.floor

/** Each indicator and its icon share one coordinate system, including animated travel. */
@Composable
fun VideoCoachNavBar(items: List<CoachNavItem>, selectedIndex: Int, style: CoachNavStyle,
    onSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    if (items.isEmpty()) return
    val index = selectedIndex.coerceIn(items.indices)
    val position by animateFloatAsState(index.toFloat(), spring(.75f, 380f), label = "video_nav_position")
    val background = AppThemeManager.getNavBarBackgroundColor()
    val accent = AppThemeManager.getNavBarAccentColor()
    val active = AppThemeManager.getNavBarSelectedIconColor()
    val inactive = AppThemeManager.getNavBarUnselectedColor()
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val labelStyle = LocalTextStyle.current.merge(TextStyle(fontSize = 10.sp, lineHeight = 13.sp,
        textAlign = TextAlign.Center))
    BoxWithConstraints(modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
      // Measure the narrowest rounded slot with the same font, locale and scale as Text.
      // Portuguese labels can need three lines at accessibility sizes.
      val labelWidth = with(density) { (floor(maxWidth.toPx() / items.size).toInt() - 2.dp.roundToPx()).coerceAtLeast(1) }
      val labelPixels = items.maxOf { item ->
          val title = tr(item.title)
          listOf(FontWeight.Bold, FontWeight.Medium).maxOf { weight ->
              measurer.measure(title, style = labelStyle.copy(fontWeight = weight),
                  constraints = Constraints(maxWidth = labelWidth)).size.height
          }
      }
      val labelHeight = with(density) { labelPixels.toDp() } + 4.dp
      val barHeight = 64.dp + labelHeight
      Box(Modifier.fillMaxWidth().height(barHeight).testTag("video_nav_surface")) {
        Canvas(Modifier.fillMaxSize()) {
            val slot = size.width / items.size
            val x = slot * (position + .5f)
            val top = 16.dp.toPx()
            val radius = min(22.dp.toPx(), slot * .36f)
            val center = Offset(x, top + 24.dp.toPx())
            val round = 26.dp.toPx()
            val bodyColor = if (style == CoachNavStyle.PURPLE_MESSAGES)
                androidx.compose.ui.graphics.lerp(background, accent, .18f) else background
            if (style == CoachNavStyle.MENISCUS) {
                // The upper edge sinks around the illuminated bead as it moves.
                val half = min(34.dp.toPx(), slot * .48f)
                val body = Path().apply {
                    moveTo(round, top)
                    lineTo(x - half, top)
                    cubicTo(x - half * .7f, top, x - half * .7f, top + radius * 1.5f, x, top + radius * 1.5f)
                    cubicTo(x + half * .7f, top + radius * 1.5f, x + half * .7f, top, x + half, top)
                    lineTo(size.width - round, top)
                    quadraticTo(size.width, top, size.width, top + round)
                    lineTo(size.width, size.height - round)
                    quadraticTo(size.width, size.height, size.width - round, size.height)
                    lineTo(round, size.height)
                    quadraticTo(0f, size.height, 0f, size.height - round)
                    lineTo(0f, top + round)
                    quadraticTo(0f, top, round, top)
                    close()
                }
                drawPath(body, bodyColor)
                drawPath(body, active.copy(alpha = .22f), style = Stroke(1.dp.toPx()))
                val bead = Offset(x, top + 2.dp.toPx())
                drawCircle(Brush.radialGradient(listOf(accent.copy(alpha = .5f), Color.Transparent), bead, radius * 1.7f), radius * 1.7f, bead)
                drawCircle(accent, radius * .78f, bead)
            } else {
                drawRoundRect(bodyColor, Offset(0f, top), Size(size.width, size.height - top), CornerRadius(round))
                drawRoundRect(active.copy(alpha = .22f), Offset(0f, top), Size(size.width, size.height - top),
                    CornerRadius(round), style = Stroke(1.dp.toPx()))
                when (style) {
                    CoachNavStyle.GLASS_PILL -> {
                        val w = min(slot - 6.dp.toPx(), 78.dp.toPx())
                        drawRoundRect(Brush.horizontalGradient(listOf(accent.copy(alpha = .55f), accent.copy(alpha = .2f)), x - w / 2, x + w / 2),
                            Offset(x - w / 2, top + 5.dp.toPx()), Size(w, 42.dp.toPx()), CornerRadius(21.dp.toPx()))
                    }
                    CoachNavStyle.TABS_V2 -> {
                        val lifted = Offset(x, top + 5.dp.toPx())
                        drawCircle(Brush.radialGradient(listOf(accent.copy(alpha = .45f), Color.Transparent), lifted, radius * 1.4f), radius * 1.4f, lifted)
                        drawCircle(accent.copy(alpha = .3f), radius, lifted)
                        drawCircle(accent, radius, lifted, style = Stroke(1.5.dp.toPx()))
                    }
                    CoachNavStyle.PURPLE_MESSAGES -> {
                        val floor = size.height - 3.dp.toPx()
                        val crest = Path().apply {
                            moveTo(x - radius, floor)
                            cubicTo(x - radius * .5f, floor, x - radius * .5f, floor - 9.dp.toPx(), x, floor - 9.dp.toPx())
                            cubicTo(x + radius * .5f, floor - 9.dp.toPx(), x + radius * .5f, floor, x + radius, floor)
                            close()
                        }
                        drawCircle(accent.copy(alpha = .16f), radius, center)
                        drawPath(crest, accent)
                    }
                    else -> Unit
                }
            }
        }
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.Top) {
            items.forEachIndexed { i, item ->
                val selected = i == index
                val lift = when (style) {
                    CoachNavStyle.TABS_V2 -> -19f
                    CoachNavStyle.MENISCUS -> -22f
                    else -> 0f
                }
                val offset by animateFloatAsState(if (selected) lift else 0f, spring(.75f, 380f), label = "video_nav_lift_$i")
                val tint = if (selected) active else inactive
                Column(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(20.dp))
                    .selectable(selected = selected, role = Role.Tab, onClick = { onSelected(i) })
                    .testTag("coach_nav_item_$i"), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(16.dp))
                    Box(Modifier.height(48.dp).fillMaxWidth().graphicsLayer { translationY = offset.dp.toPx() },
                        contentAlignment = Alignment.Center) {
                        if (i == 4 && items.size == 5) UserNotificationIcon(item.notificationCount ?: userPanelNotificationCount(), tint)
                        else Icon(item.icon, tr(item.title), Modifier.size(22.dp), tint = tint)
                    }
                    Text(tr(item.title), modifier = Modifier.fillMaxWidth().height(labelHeight)
                        .padding(horizontal = 1.dp).testTag("nav_label_$i"),
                        color = tint, style = labelStyle,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                }
            }
        }
      }
    }
}
