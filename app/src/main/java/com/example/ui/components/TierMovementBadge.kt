package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HextechGreen

/** Indica un cambio real de categoría desde la última consulta comparada. */
@Composable
fun TierMovementBadge(movement: Int, modifier: Modifier = Modifier) {
    if (movement == 0) return
    val rising = movement > 0
    Icon(
        imageVector = if (rising) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
        contentDescription = if (rising) "Subió de tier" else "Bajó de tier",
        tint = if (rising) HextechGreen else DangerRed,
        modifier = modifier
    )
}
