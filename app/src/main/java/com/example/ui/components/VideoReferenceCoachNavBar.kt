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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppThemeManager
import com.example.ui.theme.CoachNavStyle
import com.example.util.tr
import kotlin.math.floor
import kotlin.math.min

/**
 * Distinct renderings catalogued from the supplied navigation videos.
 *
 * These are intentionally not aliases of the older catalogue: each one uses
 * a different active-state geometry so a user can compare the references in
 * the premium preview without receiving the same bar under another name.
 */
@Composable
fun VideoReferenceCoachNavBar(
    items: List<CoachNavItem>,
    selectedIndex: Int,
    style: CoachNavStyle,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    val index = selectedIndex.coerceIn(items.indices)
    val position by animateFloatAsState(index.toFloat(), spring(.75f, 380f), label = "reference_video_position")
    val background = AppThemeManager.getNavBarBackgroundColor()
    val accent = AppThemeManager.getNavBarAccentColor()
    val selectionContainer = AppThemeManager.getNavBarSelectionContainerColor()
    val active = AppThemeManager.getNavBarSelectedIconColor()
    val selectedLabel = AppThemeManager.getNavBarSelectedTextColor()
    val inactive = AppThemeManager.getNavBarUnselectedColor()
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val labelStyle = LocalTextStyle.current.merge(
        TextStyle(fontSize = 10.sp, lineHeight = 13.sp, textAlign = TextAlign.Center)
    )

    BoxWithConstraints(modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp)) {
        val labelWidth = with(density) {
            (floor(maxWidth.toPx() / items.size).toInt() - 2.dp.roundToPx()).coerceAtLeast(1)
        }
        val labelPixels = items.maxOf { item ->
            listOf(FontWeight.Bold, FontWeight.Medium).maxOf { weight ->
                measurer.measure(
                    tr(item.title),
                    style = labelStyle.copy(fontWeight = weight),
                    constraints = Constraints(maxWidth = labelWidth)
                ).size.height
            }
        }
        val labelHeight = with(density) { labelPixels.toDp() } + 4.dp
        val barHeight = 56.dp + labelHeight
        Box(Modifier.fillMaxWidth().height(barHeight).testTag("video_nav_surface")) {
            Canvas(Modifier.fillMaxSize()) {
                val slot = size.width / items.size
                val x = slot * (position + .5f)
                val top = 8.dp.toPx()
                val iconCenter = top + 20.dp.toPx()
                val round = 22.dp.toPx()
                drawReferenceBase(style, background, accent, top, round)
                when (style) {
                    CoachNavStyle.VIDEO_LIQUID_GLASS -> drawLiquidGlass(slot, x, top, background, accent, selectionContainer)
                    CoachNavStyle.VIDEO_DARK_NEON_LENS -> drawDarkNeonLens(x, iconCenter, size.width, size.height, top, background, accent, selectionContainer)
                    CoachNavStyle.VIDEO_BOUNCE -> drawBounce(x, slot, iconCenter, top, accent, selectionContainer)
                    CoachNavStyle.VIDEO_NEON_WAVE -> drawNeonWave(x, top, size.width, size.height, round, accent, selectionContainer)
                    CoachNavStyle.VIDEO_SPOTLIGHT -> drawVideoSpotlight(x, slot, top, iconCenter, size.height, accent, selectionContainer)
                    CoachNavStyle.VIDEO_FLOATING_BUTTON -> drawFloatingButton(x, top, accent, selectionContainer)
                    CoachNavStyle.GLASS_WAVE -> drawGlassWave(x, slot, top, size.height, background, accent, selectionContainer)
                    CoachNavStyle.GLASS_CONE -> drawGlassCone(x, slot, top, iconCenter, accent, selectionContainer)
                    CoachNavStyle.GLASS_BEAD -> drawGlassBead(x, slot, top, size.width, size.height, accent, selectionContainer)
                    CoachNavStyle.WARM_TABS -> drawWarmTabs(x, slot, top, size.width, size.height, background, accent, selectionContainer)
                    CoachNavStyle.RIBBON_MATRIX -> drawRibbonMatrix(items.size, slot, x, top, size.height, background, accent, selectionContainer)
                    CoachNavStyle.PRISM_LATTICE -> drawPrismLattice(items.size, slot, x, top, size.height, background, accent, selectionContainer)
                    else -> Unit
                }
            }
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.Top) {
                items.forEachIndexed { itemIndex, item ->
                    val selected = itemIndex == index
                    val lift = when (style) {
                        CoachNavStyle.VIDEO_BOUNCE -> -10f
                        CoachNavStyle.VIDEO_FLOATING_BUTTON -> -13f
                        CoachNavStyle.GLASS_BEAD -> -13f
                        else -> 0f
                    }
                    val offset by animateFloatAsState(
                        if (selected) lift else 0f,
                        spring(.75f, 380f),
                        label = "reference_video_lift_$itemIndex"
                    )
                    val tint = if (selected) active else inactive
                    val labelTint = if (selected) selectedLabel else inactive
                    Column(
                        Modifier.weight(1f).fillMaxHeight()
                            .selectable(selected = selected, role = Role.Tab, onClick = { onSelected(itemIndex) })
                            .testTag("coach_nav_item_$itemIndex"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(8.dp))
                        Box(
                            Modifier.height(40.dp).fillMaxWidth().graphicsLayer { translationY = offset.dp.toPx() },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (itemIndex == 4 && items.size == 5) {
                                    UserNotificationIcon(item.notificationCount ?: userPanelNotificationCount(), tint)
                                } else {
                                    Icon(item.icon, tr(item.title), Modifier.size(22.dp), tint = tint)
                                }
                            }
                        }
                        Text(
                            tr(item.title),
                            modifier = Modifier.fillMaxWidth().height(labelHeight).padding(horizontal = 1.dp)
                                .testTag("nav_label_$itemIndex"),
                            color = labelTint,
                            style = labelStyle,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawReferenceBase(
    style: CoachNavStyle,
    background: Color,
    accent: Color,
    top: Float,
    round: Float
) {
    val body = when (style) {
        CoachNavStyle.WARM_TABS -> background.copy(alpha = .94f)
        CoachNavStyle.RIBBON_MATRIX -> background.copy(alpha = .24f)
        CoachNavStyle.PRISM_LATTICE -> background.copy(alpha = .98f)
        else -> background.copy(alpha = .94f)
    }
    if (style == CoachNavStyle.RIBBON_MATRIX) {
        drawRect(body, Offset(0f, top + 10.dp.toPx()), Size(size.width, size.height - top - 10.dp.toPx()))
    } else if (style == CoachNavStyle.PRISM_LATTICE) {
        val base = Path().apply {
            moveTo(14.dp.toPx(), top + 5.dp.toPx())
            lineTo(size.width - 14.dp.toPx(), top + 5.dp.toPx())
            lineTo(size.width, top + 18.dp.toPx())
            lineTo(size.width, size.height - 12.dp.toPx())
            lineTo(size.width - 14.dp.toPx(), size.height)
            lineTo(14.dp.toPx(), size.height)
            lineTo(0f, size.height - 12.dp.toPx())
            lineTo(0f, top + 18.dp.toPx())
            close()
        }
        drawPath(base, body)
        drawPath(base, accent.copy(alpha = .2f), style = Stroke(1.dp.toPx()))
    } else {
        drawRoundRect(body, Offset(0f, top), Size(size.width, size.height - top), CornerRadius(round))
        drawRoundRect(accent.copy(alpha = .2f), Offset(0f, top), Size(size.width, size.height - top),
            CornerRadius(round), style = Stroke(1.dp.toPx()))
    }
}

private fun DrawScope.drawLiquidGlass(
    slot: Float, x: Float, top: Float,
    background: Color, accent: Color, active: Color
) {
    val width = min(slot * .82f, 82.dp.toPx())
    drawRoundRect(
        Brush.verticalGradient(listOf(Color.White.copy(alpha = .16f), background.copy(alpha = .2f), accent.copy(alpha = .18f))),
        Offset(x - width / 2f, top + 8.dp.toPx()), Size(width, 38.dp.toPx()), CornerRadius(19.dp.toPx())
    )
    drawRoundRect(Color.White.copy(alpha = .28f), Offset(x - width / 2f + 3.dp.toPx(), top + 10.dp.toPx()),
        Size(width - 6.dp.toPx(), 34.dp.toPx()), CornerRadius(17.dp.toPx()), style = Stroke(1.dp.toPx()))
    drawRoundRect(active.copy(alpha = .22f), Offset(x - slot * .36f, top + 11.dp.toPx()),
        Size(slot * .72f, 32.dp.toPx()), CornerRadius(16.dp.toPx()))
    drawLine(Color.White.copy(alpha = .4f), Offset(x - slot * .22f, top + 13.dp.toPx()),
        Offset(x + slot * .2f, top + 13.dp.toPx()), 1.dp.toPx(), StrokeCap.Round)
}

private fun DrawScope.drawDarkNeonLens(
    x: Float, iconCenter: Float, width: Float, height: Float, top: Float,
    background: Color, accent: Color, active: Color
) {
    drawLine(accent.copy(alpha = .18f), Offset(18.dp.toPx(), height - 9.dp.toPx()),
        Offset(width - 18.dp.toPx(), height - 9.dp.toPx()), 1.dp.toPx())
    val lens = Size(54.dp.toPx(), 28.dp.toPx())
    val lensTop = iconCenter - lens.height / 2f
    drawOval(Brush.radialGradient(listOf(active.copy(alpha = .62f), accent.copy(alpha = .1f), Color.Transparent)),
        Offset(x - lens.width / 2f, lensTop), lens)
    drawOval(active.copy(alpha = .92f), Offset(x - lens.width / 2f, lensTop), lens, style = Stroke(1.5.dp.toPx()))
    drawOval(Color.White.copy(alpha = .28f), Offset(x - lens.width / 2f + 4.dp.toPx(), lensTop + 4.dp.toPx()),
        Size(lens.width - 8.dp.toPx(), lens.height - 8.dp.toPx()), style = Stroke(1.dp.toPx()))
    drawLine(background.copy(alpha = .9f), Offset(x - 17.dp.toPx(), top + 8.dp.toPx()),
        Offset(x + 17.dp.toPx(), top + 8.dp.toPx()), 1.dp.toPx(), StrokeCap.Round)
}

private fun DrawScope.drawBounce(x: Float, slot: Float, iconCenter: Float, top: Float, accent: Color, active: Color) {
    val width = min(slot * .5f, 34.dp.toPx())
    val height = 30.dp.toPx()
    val tileTop = iconCenter - 10.dp.toPx() - height / 2f
    drawOval(Color.Black.copy(alpha = .32f), Offset(x - width * .38f, iconCenter + 12.dp.toPx()),
        Size(width * .76f, 4.dp.toPx()))
    drawRoundRect(
        Brush.verticalGradient(listOf(active.copy(alpha = .68f), accent.copy(alpha = .18f))),
        Offset(x - width / 2f, tileTop),
        Size(width, height),
        CornerRadius(10.dp.toPx())
    )
    drawRoundRect(active.copy(alpha = .9f), Offset(x - width / 2f, tileTop), Size(width, height),
        CornerRadius(10.dp.toPx()), style = Stroke(1.4.dp.toPx()))
    drawLine(accent.copy(alpha = .55f), Offset(x - width * .35f, top + 43.dp.toPx()),
        Offset(x - width * .1f, top + 43.dp.toPx()), 1.dp.toPx(), StrokeCap.Round)
    drawLine(accent.copy(alpha = .55f), Offset(x + width * .1f, top + 43.dp.toPx()),
        Offset(x + width * .35f, top + 43.dp.toPx()), 1.dp.toPx(), StrokeCap.Round)
}

private fun DrawScope.drawNeonWave(
    x: Float, top: Float, width: Float, height: Float, round: Float, accent: Color, active: Color
) {
    drawRoundRect(accent.copy(alpha = .13f), Offset(0f, top), Size(width, height - top), CornerRadius(round))
    drawRoundRect(accent.copy(alpha = .85f), Offset(0f, top), Size(width, height - top), CornerRadius(round),
        style = Stroke(2.dp.toPx()))
    val crest = Path().apply {
        moveTo(0f, top + 9.dp.toPx())
        lineTo(x - 44.dp.toPx(), top + 9.dp.toPx())
        cubicTo(x - 31.dp.toPx(), top + 9.dp.toPx(), x - 28.dp.toPx(), top + 20.dp.toPx(), x, top + 20.dp.toPx())
        cubicTo(x + 28.dp.toPx(), top + 20.dp.toPx(), x + 31.dp.toPx(), top + 9.dp.toPx(), x + 44.dp.toPx(), top + 9.dp.toPx())
        lineTo(width, top + 9.dp.toPx())
    }
    drawPath(crest, active.copy(alpha = .3f), style = Stroke(8.dp.toPx(), cap = StrokeCap.Round))
    drawPath(crest, active, style = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round))
}

private fun DrawScope.drawVideoSpotlight(
    x: Float, slot: Float, top: Float, iconCenter: Float, height: Float,
    accent: Color, active: Color
) {
    val beamTop = top + 1.dp.toPx()
    val beamBottom = min(iconCenter + 25.dp.toPx(), height - 10.dp.toPx())
    val halfTop = min(slot * .1f, 8.dp.toPx())
    val halfBottom = min(slot * .42f, 32.dp.toPx())
    val beam = Path().apply {
        moveTo(x - halfTop, beamTop)
        lineTo(x + halfTop, beamTop)
        lineTo(x + halfBottom, beamBottom)
        lineTo(x - halfBottom, beamBottom)
        close()
    }
    drawPath(beam, Brush.verticalGradient(listOf(accent.copy(alpha = .45f), active.copy(alpha = .04f))))
    drawLine(accent.copy(alpha = .85f), Offset(x - halfTop, beamTop), Offset(x - halfBottom, beamBottom),
        1.dp.toPx(), StrokeCap.Round)
    drawLine(accent.copy(alpha = .85f), Offset(x + halfTop, beamTop), Offset(x + halfBottom, beamBottom),
        1.dp.toPx(), StrokeCap.Round)
    drawLine(active, Offset(x - halfBottom, beamBottom), Offset(x + halfBottom, beamBottom),
        2.dp.toPx(), StrokeCap.Round)
}

private fun DrawScope.drawFloatingButton(x: Float, top: Float, accent: Color, active: Color) {
    val width = 32.dp.toPx()
    val height = 28.dp.toPx()
    val buttonCenter = Offset(x, top + 7.dp.toPx())
    drawRoundRect(accent.copy(alpha = .55f), Offset(x - 2.dp.toPx(), top + 2.dp.toPx()),
        Size(4.dp.toPx(), 20.dp.toPx()), CornerRadius(2.dp.toPx()))
    drawRoundRect(
        Brush.verticalGradient(listOf(active.copy(alpha = .72f), accent.copy(alpha = .28f))),
        Offset(buttonCenter.x - width / 2f, buttonCenter.y - height / 2f),
        Size(width, height),
        CornerRadius(10.dp.toPx())
    )
    drawRoundRect(active.copy(alpha = .9f), Offset(buttonCenter.x - width / 2f, buttonCenter.y - height / 2f),
        Size(width, height), CornerRadius(10.dp.toPx()), style = Stroke(1.4.dp.toPx()))
}

private fun DrawScope.drawGlassWave(
    x: Float, slot: Float, top: Float, height: Float,
    background: Color, accent: Color, active: Color
) {
    val half = min(slot * .48f, 38.dp.toPx())
    val fill = Path().apply {
        moveTo(x - half, top + 7.dp.toPx())
        cubicTo(x - half * .62f, top + 7.dp.toPx(), x - half * .56f, top + 19.dp.toPx(), x - half * .2f, top + 19.dp.toPx())
        cubicTo(x - half * .06f, top + 19.dp.toPx(), x - half * .04f, top + 8.dp.toPx(), x, top + 8.dp.toPx())
        cubicTo(x + half * .04f, top + 8.dp.toPx(), x + half * .06f, top + 19.dp.toPx(), x + half * .2f, top + 19.dp.toPx())
        cubicTo(x + half * .56f, top + 19.dp.toPx(), x + half * .62f, top + 7.dp.toPx(), x + half, top + 7.dp.toPx())
        lineTo(x + half, top + 45.dp.toPx())
        lineTo(x - half, top + 45.dp.toPx())
        close()
    }
    drawPath(fill, Brush.verticalGradient(listOf(active.copy(alpha = .42f), background.copy(alpha = .2f))))
    drawPath(fill, active.copy(alpha = .84f), style = Stroke(1.4.dp.toPx()))
    drawLine(Color.White.copy(alpha = .25f), Offset(x - half + 5.dp.toPx(), height - 8.dp.toPx()),
        Offset(x + half - 5.dp.toPx(), height - 8.dp.toPx()), 1.dp.toPx(), StrokeCap.Round)
}

private fun DrawScope.drawGlassCone(x: Float, slot: Float, top: Float, iconCenter: Float, accent: Color, active: Color) {
    val halfBase = min(slot * .42f, 28.dp.toPx())
    val cone = Path().apply {
        moveTo(x - 9.dp.toPx(), top + 3.dp.toPx())
        lineTo(x + 9.dp.toPx(), top + 3.dp.toPx())
        lineTo(x + halfBase, iconCenter + 18.dp.toPx())
        lineTo(x - halfBase, iconCenter + 18.dp.toPx())
        close()
    }
    drawPath(cone, Brush.verticalGradient(listOf(active.copy(alpha = .5f), accent.copy(alpha = .03f))))
    drawLine(active.copy(alpha = .8f), Offset(x - 9.dp.toPx(), top + 3.dp.toPx()),
        Offset(x - halfBase, iconCenter + 18.dp.toPx()), 1.dp.toPx(), StrokeCap.Round)
    drawLine(active.copy(alpha = .8f), Offset(x + 9.dp.toPx(), top + 3.dp.toPx()),
        Offset(x + halfBase, iconCenter + 18.dp.toPx()), 1.dp.toPx(), StrokeCap.Round)
}

private fun DrawScope.drawGlassBead(x: Float, slot: Float, top: Float, width: Float, height: Float, accent: Color, active: Color) {
    drawRect(Color.Transparent, Offset.Zero, Size.Zero)
    drawRect(accent.copy(alpha = .16f), Offset(0f, top + 8.dp.toPx()), Size(width, 1.dp.toPx()))
    drawLine(accent.copy(alpha = .38f), Offset(10.dp.toPx(), height - 7.dp.toPx()),
        Offset(x - 24.dp.toPx(), height - 7.dp.toPx()), 1.dp.toPx(), StrokeCap.Round)
    drawLine(accent.copy(alpha = .38f), Offset(x + 24.dp.toPx(), height - 7.dp.toPx()),
        Offset(width - 10.dp.toPx(), height - 7.dp.toPx()), 1.dp.toPx(), StrokeCap.Round)
    val beadWidth = min(slot * .46f, 30.dp.toPx())
    val beadHeight = 14.dp.toPx()
    val beadTop = top
    drawRoundRect(
        Brush.horizontalGradient(listOf(accent.copy(alpha = .32f), active.copy(alpha = .74f), accent.copy(alpha = .32f))),
        Offset(x - beadWidth / 2f, beadTop),
        Size(beadWidth, beadHeight),
        CornerRadius(beadHeight / 2f)
    )
    drawRoundRect(active.copy(alpha = .9f), Offset(x - beadWidth / 2f, beadTop), Size(beadWidth, beadHeight),
        CornerRadius(beadHeight / 2f), style = Stroke(1.2.dp.toPx()))
}

private fun DrawScope.drawWarmTabs(
    x: Float, slot: Float, top: Float, width: Float, height: Float,
    background: Color, accent: Color, active: Color
) {
    val selectedWidth = min(slot - 8.dp.toPx(), 70.dp.toPx())
    drawRoundRect(Brush.verticalGradient(listOf(active.copy(alpha = .46f), background.copy(alpha = .94f))),
        Offset(x - selectedWidth / 2f, top + 5.dp.toPx()), Size(selectedWidth, 42.dp.toPx()), CornerRadius(21.dp.toPx()))
    drawRoundRect(accent.copy(alpha = .72f), Offset(x - selectedWidth / 2f, top + 5.dp.toPx()),
        Size(selectedWidth, 42.dp.toPx()), CornerRadius(21.dp.toPx()), style = Stroke(1.dp.toPx()))
    drawLine(accent.copy(alpha = .36f), Offset(12.dp.toPx(), top + 7.dp.toPx()),
        Offset(width - 12.dp.toPx(), top + 7.dp.toPx()), 1.dp.toPx(), StrokeCap.Round)
    drawRoundRect(active.copy(alpha = .76f), Offset(x - min(slot * .28f, 22.dp.toPx()), height - 10.dp.toPx()),
        Size(min(slot * .56f, 44.dp.toPx()), 3.dp.toPx()), CornerRadius(2.dp.toPx()))
}

private fun DrawScope.drawRibbonMatrix(
    itemCount: Int, slot: Float, selectedX: Float, top: Float, height: Float,
    background: Color, accent: Color, active: Color
) {
    for (i in 0 until itemCount) {
        val center = slot * (i + .5f)
        val half = min(slot * .34f, 25.dp.toPx())
        val ribbon = Path().apply {
            moveTo(center - half, top + 9.dp.toPx())
            lineTo(center + half - 6.dp.toPx(), top + 9.dp.toPx())
            lineTo(center + half, height - 2.dp.toPx())
            lineTo(center - half + 6.dp.toPx(), height - 2.dp.toPx())
            close()
        }
        drawPath(ribbon, if (i == ((selectedX / slot) - .5f).toInt().coerceIn(0, itemCount - 1)) active.copy(alpha = .34f) else background.copy(alpha = .24f))
        drawPath(ribbon, if (i == ((selectedX / slot) - .5f).toInt().coerceIn(0, itemCount - 1)) active.copy(alpha = .8f) else accent.copy(alpha = .18f), style = Stroke(1.dp.toPx()))
    }
    drawLine(accent.copy(alpha = .65f), Offset(0f, height - 2.dp.toPx()), Offset(size.width, height - 2.dp.toPx()), 1.dp.toPx())
}

private fun DrawScope.drawPrismLattice(
    itemCount: Int, slot: Float, selectedX: Float, top: Float, height: Float,
    background: Color, accent: Color, active: Color
) {
    val activeIndex = ((selectedX / slot) - .5f).toInt().coerceIn(0, itemCount - 1)
    for (i in 0 until itemCount) {
        val center = slot * (i + .5f)
        drawLine(accent.copy(alpha = .18f), Offset(center, top + 7.dp.toPx()), Offset(center, height), 1.dp.toPx())
        drawLine(accent.copy(alpha = .12f), Offset(center - slot / 2f, height - 10.dp.toPx()),
            Offset(center, top + 8.dp.toPx()), 1.dp.toPx())
        drawLine(accent.copy(alpha = .12f), Offset(center, top + 8.dp.toPx()),
            Offset(center + slot / 2f, height - 10.dp.toPx()), 1.dp.toPx())
        if (i == activeIndex) {
            val diamond = Path().apply {
                moveTo(center, top + 2.dp.toPx())
                lineTo(center + min(slot * .38f, 27.dp.toPx()), top + 25.dp.toPx())
                lineTo(center, top + 48.dp.toPx())
                lineTo(center - min(slot * .38f, 27.dp.toPx()), top + 25.dp.toPx())
                close()
            }
            drawPath(diamond, Brush.verticalGradient(listOf(active.copy(alpha = .52f), accent.copy(alpha = .08f))))
            drawPath(diamond, active.copy(alpha = .9f), style = Stroke(1.4.dp.toPx()))
        }
    }
}
