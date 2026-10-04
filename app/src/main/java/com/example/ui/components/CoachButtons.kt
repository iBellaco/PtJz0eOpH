package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/** Enabled by the activity only. Shared overlay screens retain their existing behavior. */
val LocalCoachButtonAnimation = staticCompositionLocalOf { false }
const val COACH_BUTTON_PRESSED_SCALE = 0.94f
const val COACH_BUTTON_SPRING_STIFFNESS = 380f

@Composable
fun Modifier.coachClickable(enabled: Boolean = true, onClickLabel: String? = null,
    role: Role? = null, onClick: () -> Unit): Modifier {
    if (!LocalCoachButtonAnimation.current) return clickable(enabled = enabled, onClickLabel = onClickLabel, role = role, onClick = onClick)
    val source = remember { MutableInteractionSource() }
    return coachButtonMotion(source, enabled).clickable(interactionSource = source,
        indication = ripple(), enabled = enabled, onClickLabel = onClickLabel, role = role, onClick = onClick)
}

@Composable
fun Modifier.coachClickable(interactionSource: MutableInteractionSource, indication: Indication?,
    enabled: Boolean = true, onClickLabel: String? = null, role: Role? = null, onClick: () -> Unit): Modifier =
    coachButtonMotion(interactionSource, enabled).clickable(interactionSource = interactionSource,
        indication = indication, enabled = enabled, onClickLabel = onClickLabel, role = role, onClick = onClick)

@Composable
internal fun CoachActionBox(onClick: () -> Unit, modifier: Modifier, enabled: Boolean,
    shape: Shape, background: androidx.compose.ui.graphics.Color, borderColor: androidx.compose.ui.graphics.Color,
    padding: PaddingValues, backgroundBrush: androidx.compose.ui.graphics.Brush? = null,
    content: @Composable () -> Unit) {
    val source = remember { MutableInteractionSource() }
    androidx.compose.foundation.layout.Box(
        modifier.coachButtonMotion(source, enabled).clip(shape)
            .then(if (backgroundBrush != null) Modifier.background(backgroundBrush) else Modifier.background(background))
            .border(BorderStroke(1.dp, borderColor), shape)
            .clickable(interactionSource = source, indication = ripple(), enabled = enabled, onClick = onClick)
            .padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center
    ) { content() }
}

@Composable
internal fun Modifier.coachButtonMotion(source: MutableInteractionSource, enabled: Boolean): Modifier {
    if (!LocalCoachButtonAnimation.current) return this
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && enabled) COACH_BUTTON_PRESSED_SCALE else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = COACH_BUTTON_SPRING_STIFFNESS), label = "coachButtonPress")
    return graphicsLayer { scaleX = scale; scaleY = scale }
}

@Composable
fun CoachButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape, colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(), border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding, interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    Button(onClick, modifier.coachButtonMotion(source, enabled), enabled, shape, colors, elevation, border, contentPadding, source, content)
}

@Composable
fun CoachOutlinedButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    shape: Shape = ButtonDefaults.outlinedShape, colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null, border: BorderStroke? = ButtonDefaults.outlinedButtonBorder(enabled),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding, interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    OutlinedButton(onClick, modifier.coachButtonMotion(source, enabled), enabled, shape, colors, elevation, border, contentPadding, source, content)
}

@Composable
fun CoachTextButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    shape: Shape = ButtonDefaults.textShape, colors: ButtonColors = ButtonDefaults.textButtonColors(),
    elevation: ButtonElevation? = null, border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding, interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    TextButton(onClick, modifier.coachButtonMotion(source, enabled), enabled, shape, colors, elevation, border, contentPadding, source, content)
}

@Composable
fun CoachIconButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(), interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    IconButton(onClick, modifier.coachButtonMotion(source, enabled), enabled, colors, source, content = content)
}

@Composable
fun CoachFilterChip(selected: Boolean, onClick: () -> Unit, label: @Composable () -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true, leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null, shape: Shape = FilterChipDefaults.shape,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(),
    elevation: SelectableChipElevation? = FilterChipDefaults.filterChipElevation(),
    border: BorderStroke? = FilterChipDefaults.filterChipBorder(enabled, selected),
    interactionSource: MutableInteractionSource? = null) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    FilterChip(selected, onClick, label, modifier.coachButtonMotion(source, enabled), enabled,
        leadingIcon, trailingIcon, shape, colors, elevation, border, source)
}

@Composable
fun CoachAssistChip(onClick: () -> Unit, label: @Composable () -> Unit, modifier: Modifier = Modifier,
    enabled: Boolean = true, leadingIcon: @Composable (() -> Unit)? = null, trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = AssistChipDefaults.shape, colors: ChipColors = AssistChipDefaults.assistChipColors(),
    elevation: ChipElevation? = AssistChipDefaults.assistChipElevation(),
    border: BorderStroke? = AssistChipDefaults.assistChipBorder(enabled), interactionSource: MutableInteractionSource? = null) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    AssistChip(onClick, label, modifier.coachButtonMotion(source, enabled), enabled, leadingIcon, trailingIcon, shape, colors, elevation, border, source)
}

@Composable
fun CoachTab(selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    text: @Composable (() -> Unit)? = null, icon: @Composable (() -> Unit)? = null,
    selectedContentColor: androidx.compose.ui.graphics.Color = LocalContentColor.current,
    unselectedContentColor: androidx.compose.ui.graphics.Color = selectedContentColor, interactionSource: MutableInteractionSource? = null) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    Tab(selected, onClick, modifier.coachButtonMotion(source, enabled), enabled, text, icon, selectedContentColor, unselectedContentColor, source)
}

@Composable
fun CoachTab(selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    selectedContentColor: androidx.compose.ui.graphics.Color = LocalContentColor.current,
    unselectedContentColor: androidx.compose.ui.graphics.Color = selectedContentColor, interactionSource: MutableInteractionSource? = null,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    Tab(selected, onClick, modifier.coachButtonMotion(source, enabled), enabled, selectedContentColor, unselectedContentColor, source, content)
}

@Composable
fun RowScope.CoachNavigationBarItem(selected: Boolean, onClick: () -> Unit, icon: @Composable () -> Unit,
    modifier: Modifier = Modifier, enabled: Boolean = true, label: @Composable (() -> Unit)? = null,
    alwaysShowLabel: Boolean = true, colors: NavigationBarItemColors = NavigationBarItemDefaults.colors(),
    interactionSource: MutableInteractionSource? = null) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    NavigationBarItem(selected, onClick, icon, modifier.coachButtonMotion(source, enabled), enabled, label, alwaysShowLabel, colors, source)
}

@Composable
fun CoachClickableSurface(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    shape: Shape = androidx.compose.ui.graphics.RectangleShape, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
    contentColor: androidx.compose.ui.graphics.Color = contentColorFor(color), tonalElevation: androidx.compose.ui.unit.Dp = 0.dp,
    shadowElevation: androidx.compose.ui.unit.Dp = 0.dp, border: BorderStroke? = null,
    interactionSource: MutableInteractionSource? = null, content: @Composable () -> Unit) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    Surface(onClick, modifier.coachButtonMotion(source, enabled), enabled, shape, color, contentColor, tonalElevation, shadowElevation, border, source, content)
}
