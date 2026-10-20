package com.example.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.tr
import java.util.Locale

/** A zero change is neutral; missing history must never look like +0.00%. */
@Composable
fun RateChangeBadge(delta: Double) {
    val text = when {
        !delta.isFinite() -> "—"
        kotlin.math.abs(delta) < 0.005 -> "="
        else -> String.format(Locale.US, "%+.2f%%", delta)
    }
    Text(tr(text), color = when {
        !delta.isFinite() || kotlin.math.abs(delta) < 0.005 -> TextMuted
        delta > 0 -> HextechGreen
        else -> DangerRed
    }, fontSize = 10.sp)
}
