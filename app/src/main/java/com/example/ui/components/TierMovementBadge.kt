package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        Icon(
            imageVector = if (rising) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
            contentDescription = tr(if (rising) "Subió de tier" else "Bajó de tier"),
            tint = if (rising) HextechGreen else DangerRed
        )
        Text(tr(if (rising) "Subió" else "Bajó"), color = if (rising) HextechGreen else DangerRed, fontSize = 9.sp)
    }
}
