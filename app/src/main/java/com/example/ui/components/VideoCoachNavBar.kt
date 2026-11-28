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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
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
    val selectionContainer = AppThemeManager.getNavBarSelectionContainerColor()
    val active = AppThemeManager.getNavBarSelectedIconColor()
    val selectedLabel = AppThemeManager.getNavBarSelectedTextColor()
    val inactive = AppThemeManager.getNavBarUnselectedColor()
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val labelStyle = LocalTextStyle.current.merge(TextStyle(fontSize = 10.sp, lineHeight = 13.sp,
        textAlign = TextAlign.Center))
    BoxWithConstraints(modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp)) {
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
      val barHeight = 56.dp + labelHeight
      Box(Modifier.fillMaxWidth().height(barHeight).testTag("video_nav_surface")) {
        Canvas(Modifier.fillMaxSize()) {
            val slot = size.width / items.size
            val x = slot * (position + .5f)
            val top = 8.dp.toPx()
            val radius = min(20.dp.toPx(), slot * .34f)
            val center = Offset(x, top + 20.dp.toPx())
            val round = 22.dp.toPx()
            val bodyColor = when (style) {
                CoachNavStyle.PURPLE_MESSAGES -> androidx.compose.ui.graphics.lerp(background, accent, .18f)
                else -> background
            }
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
                drawPath(body, selectionContainer.copy(alpha = .78f), style = Stroke(1.dp.toPx()))
                val capWidth = min(slot * .5f, 40.dp.toPx())
                val capHeight = 12.dp.toPx()
                val capTop = top + 1.dp.toPx()
                drawRoundRect(
                    Brush.horizontalGradient(listOf(accent.copy(alpha = .3f), accent.copy(alpha = .78f), accent.copy(alpha = .3f))),
                    Offset(x - capWidth / 2f, capTop),
                    Size(capWidth, capHeight),
                    CornerRadius(capHeight / 2f)
                )
                drawRoundRect(
                    Color.White.copy(alpha = .32f),
                    Offset(x - capWidth / 2f, capTop),
                    Size(capWidth, capHeight),
                    CornerRadius(capHeight / 2f),
                    style = Stroke(1.dp.toPx())
                )
            } else {
                drawRoundRect(bodyColor, Offset(0f, top), Size(size.width, size.height - top), CornerRadius(round))
                drawRoundRect(selectionContainer.copy(alpha = .78f), Offset(0f, top), Size(size.width, size.height - top),
                    CornerRadius(round), style = Stroke(1.dp.toPx()))
                when (style) {
                    CoachNavStyle.GLASS_PILL -> {
                        val w = min(slot - 6.dp.toPx(), 78.dp.toPx())
                        drawRoundRect(Brush.horizontalGradient(listOf(accent.copy(alpha = .55f), accent.copy(alpha = .2f)), x - w / 2, x + w / 2),
                            Offset(x - w / 2, top + 5.dp.toPx()), Size(w, 42.dp.toPx()), CornerRadius(21.dp.toPx()))
                    }
                    CoachNavStyle.TABS_V2 -> {
                        val width = min(slot * .62f, 46.dp.toPx())
                        val height = 34.dp.toPx()
                        val tabTop = center.y - height / 2f
                        drawRoundRect(
                            Brush.verticalGradient(listOf(accent.copy(alpha = .4f), selectionContainer.copy(alpha = .84f))),
                            Offset(x - width / 2f, tabTop),
                            Size(width, height),
                            CornerRadius(10.dp.toPx())
                        )
                        drawRoundRect(
                            accent.copy(alpha = .86f),
                            Offset(x - width / 2f, tabTop),
                            Size(width, height),
                            CornerRadius(10.dp.toPx()),
                            style = Stroke(1.4.dp.toPx())
                        )
                    }
                    CoachNavStyle.PURPLE_MESSAGES -> {
                        val bubbleWidth = min(slot * .72f, 52.dp.toPx())
                        val bubbleHeight = 32.dp.toPx()
                        val bubbleTop = center.y - bubbleHeight / 2f
                        drawRoundRect(
                            accent.copy(alpha = .26f),
                            Offset(x - bubbleWidth / 2f, bubbleTop),
                            Size(bubbleWidth, bubbleHeight),
                            CornerRadius(11.dp.toPx())
                        )
                        drawRoundRect(
                            accent.copy(alpha = .9f),
                            Offset(x - bubbleWidth / 2f, bubbleTop),
                            Size(bubbleWidth, bubbleHeight),
                            CornerRadius(11.dp.toPx()),
                            style = Stroke(1.2.dp.toPx())
                        )
                        val tail = Path().apply {
                            moveTo(x + 4.dp.toPx(), bubbleTop + bubbleHeight)
                            lineTo(x + 12.dp.toPx(), bubbleTop + bubbleHeight)
                            lineTo(x + 7.dp.toPx(), bubbleTop + bubbleHeight + 6.dp.toPx())
                            close()
                        }
                        drawPath(tail, accent)
                    }
                    else -> Unit
                }
            }
        }
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.Top) {
            items.forEachIndexed { i, item ->
                val selected = i == index
                val lift = when (style) {
                    CoachNavStyle.TABS_V2 -> -4f
                    CoachNavStyle.MENISCUS -> -14f
                    else -> 0f
                }
                val offset by animateFloatAsState(if (selected) lift else 0f, spring(.75f, 380f), label = "video_nav_lift_$i")
                val tint = if (selected) active else inactive
                val labelTint = if (selected) selectedLabel else inactive
                Column(Modifier.weight(1f).fillMaxHeight()
                    .selectable(selected = selected, role = Role.Tab, onClick = { onSelected(i) })
                    .testTag("coach_nav_item_$i"), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.height(40.dp).fillMaxWidth().graphicsLayer { translationY = offset.dp.toPx() },
                        contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (i == 4 && items.size == 5) {
                                UserNotificationIcon(item.notificationCount ?: userPanelNotificationCount(), tint)
                            } else {
                                Icon(item.icon, tr(item.title), Modifier.size(22.dp), tint = tint)
                            }
                        }
                    }
                    Text(tr(item.title), modifier = Modifier.fillMaxWidth().height(labelHeight)
                        .padding(horizontal = 1.dp).testTag("nav_label_$i"),
                        color = labelTint, style = labelStyle,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                }
            }
        }
      }
    }
}
