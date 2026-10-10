package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CoachNavStyle

/** Variante arc_glowing de navegación de Coach. */
@Composable
fun ArcGlowingNavBar(
    items: List<CoachNavItem>, selectedIndex: Int,
    backgroundColor: Color, selectedColor: Color, unselectedColor: Color,
    onSelected: (Int) -> Unit, modifier: Modifier = Modifier
) = CoachNavBarCore(items, selectedIndex, CoachNavStyle.ARC_GLOWING, backgroundColor, selectedColor, unselectedColor, onSelected, modifier)
