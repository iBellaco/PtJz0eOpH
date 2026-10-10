package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CoachNavStyle

/** Variante neon_wave de navegación de Coach. */
@Composable
fun NeonWaveNavBar(
    items: List<CoachNavItem>, selectedIndex: Int,
    backgroundColor: Color, selectedColor: Color, unselectedColor: Color,
    onSelected: (Int) -> Unit, modifier: Modifier = Modifier
) = CoachNavBarCore(items, selectedIndex, CoachNavStyle.NEON_WAVE, backgroundColor, selectedColor, unselectedColor, onSelected, modifier)
