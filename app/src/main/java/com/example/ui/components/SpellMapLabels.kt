package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HextechCyan

/** Informational labels shared by the spell catalog and spell details. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpellMapLabels(labels: List<String>, modifier: Modifier = Modifier) {
    if (labels.isEmpty()) return
    FlowRow(
        modifier = modifier.testTag("spell_map_labels"),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        labels.forEach { label ->
            Box(
                Modifier.clip(RoundedCornerShape(6.dp))
                    .background(HextechCyan.copy(alpha = 0.14f))
                    .border(0.5.dp, HextechCyan.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(label, color = HextechCyan, fontSize = 9.5.sp, lineHeight = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
