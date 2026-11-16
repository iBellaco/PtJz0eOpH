package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TextPrimary
import com.example.util.DraftRecommendationText
import com.example.util.tr

/** Full-width paragraphs remain readable in narrow screens and with larger system text. */
@Composable
fun DraftAdviceSection(title: String, text: String, color: Color, tag: String) {
    val cleaned = DraftRecommendationText.clean(text)
    val advice = tr(cleaned)
    if (advice.isBlank()) return

    val preview = tr(DraftRecommendationText.compact(cleaned, maxChars = 150, maxSentences = 2))
    ExpandableDraftText(
        text = advice,
        preview = preview,
        tag = tag,
        color = color,
        title = tr(title)
    )
}

/** A compact, champion-specific recommendation that can be expanded in place. */
@Composable
fun DraftRecommendationSummary(text: String, tag: String, maxChars: Int) {
    val cleaned = DraftRecommendationText.clean(text)
    val advice = tr(cleaned)
    if (advice.isBlank()) return

    val preview = tr(DraftRecommendationText.compact(cleaned, maxChars = maxChars, maxSentences = 2))
    ExpandableDraftText(
        text = advice,
        preview = preview,
        tag = tag,
        color = TextPrimary
    )
}

@Composable
private fun ExpandableDraftText(
    text: String,
    preview: String,
    tag: String,
    color: Color,
    title: String? = null
) {
    val canExpand = text != preview
    var expanded by rememberSaveable(tag, text) { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth().testTag(tag), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        title?.let {
            Text(it, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            text = if (expanded) text else preview,
            color = if (title == null) color.copy(alpha = 0.95f) else TextPrimary,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            modifier = Modifier.fillMaxWidth().testTag("${tag}_text")
        )
        if (canExpand) {
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("${tag}_toggle")
                    .padding(horizontal = 0.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Text(
                    text = tr(if (expanded) "Ver menos" else "Ver más"),
                    color = color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
