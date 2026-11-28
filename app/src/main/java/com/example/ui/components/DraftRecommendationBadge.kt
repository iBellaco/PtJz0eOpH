package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechGreen
import com.example.ui.theme.TextMuted
import com.example.util.tr
import java.util.Locale

internal enum class DraftRecommendationBadgeTone { COUNTER, COMBO, PRIORITY, FAVORITE, RISK, NEUTRAL }

internal fun draftRecommendationBadgeTone(label: String): DraftRecommendationBadgeTone {
    val normalized = label.uppercase(Locale.ROOT)
    return when {
        "RIESGO" in normalized || "DESFAVOR" in normalized || "VARIABLE" in normalized -> DraftRecommendationBadgeTone.RISK
        "WOMBO" in normalized || "COMBO" in normalized || "SINERGIA" in normalized -> DraftRecommendationBadgeTone.COMBO
        "COUNTER" in normalized || "VENTAJA" in normalized -> DraftRecommendationBadgeTone.COUNTER
        "PRIORIDAD" in normalized || "PRIORIDADE" in normalized || "PRIMER" in normalized || "PRIMEIRA" in normalized -> DraftRecommendationBadgeTone.PRIORITY
        "FAVORIT" in normalized -> DraftRecommendationBadgeTone.FAVORITE
        else -> DraftRecommendationBadgeTone.NEUTRAL
    }
}

@Composable
fun DraftRecommendationBadge(
    label: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val tone = draftRecommendationBadgeTone(label)
    val color = when (tone) {
        DraftRecommendationBadgeTone.COUNTER -> HextechCyan
        DraftRecommendationBadgeTone.COMBO -> Color(0xFFB99CFF)
        DraftRecommendationBadgeTone.PRIORITY -> HextechGold
        DraftRecommendationBadgeTone.FAVORITE -> HextechGreen
        DraftRecommendationBadgeTone.RISK -> DangerRed
        DraftRecommendationBadgeTone.NEUTRAL -> TextMuted
    }
    Surface(
        modifier = modifier.testTag("draft_recommendation_badge_${tone.name.lowercase(Locale.ROOT)}"),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(if (compact) 4.dp else 6.dp),
        color = color.copy(alpha = if (compact) 0.14f else 0.16f),
        contentColor = color,
        border = BorderStroke(if (compact) 0.7.dp else 1.dp, color.copy(alpha = 0.72f))
    ) {
        Text(
            text = tr(label),
            color = color,
            fontSize = if (compact) 8.sp else 10.5.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = if (compact) 10.sp else 13.sp,
            maxLines = if (compact) 1 else 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.padding(
                PaddingValues(
                    horizontal = if (compact) 5.dp else 7.dp,
                    vertical = if (compact) 2.dp else 3.dp
                )
            )
        )
    }
}
