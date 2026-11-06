package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HextechGreen
import com.example.util.tr

/** Indica un cambio real de categoría desde la última consulta comparada. */
@Composable
fun TierMovementBadge(movement: Int, modifier: Modifier = Modifier) {
    if (movement == 0) return
    val rising = movement > 0
    val steps = movement.toLong().let { kotlin.math.abs(it).coerceAtMost(3L) }.toInt()
    val tint = if (rising) HextechGreen else DangerRed
    Row(
        modifier = modifier.testTag(if (rising) "tier_movement_up" else "tier_movement_down"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (rising) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
            contentDescription = "${tr(if (rising) "Subió de categoría" else "Bajó de categoría")} x$steps",
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = "x$steps",
            color = tint,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.testTag("tier_movement_multiplier_x$steps")
        )
    }
}
