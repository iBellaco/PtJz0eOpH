package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    val advice = tr(DraftRecommendationText.clean(text))
    if (advice.isBlank()) return
    Column(Modifier.fillMaxWidth().testTag(tag), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(tr(title), color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(advice, color = TextPrimary, fontSize = 12.sp, lineHeight = 17.sp,
            modifier = Modifier.fillMaxWidth().testTag("${tag}_text"))
    }
}
