package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HextechGreen
import com.example.util.tr

/** Indica un cambio real de categoría desde la última consulta comparada. */
@Composable
fun TierMovementBadge(movement: Int, modifier: Modifier = Modifier) {
    if (movement == 0) return
    val rising = movement > 0
    Icon(
        imageVector = if (rising) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
        contentDescription = tr(if (rising) "Subió de categoría" else "Bajó de categoría"),
        tint = if (rising) HextechGreen else DangerRed,
        modifier = modifier.size(18.dp).testTag(if (rising) "tier_movement_up" else "tier_movement_down")
    )
}
