package com.example.ui.screens

import androidx.compose.ui.platform.testTag
import com.example.util.BuildChoiceRules
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.ui.components.coachClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.example.ui.theme.HextechDarkBg
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WildRiftRepository
import com.example.model.Champion
import com.example.ui.components.ChampionAvatar
import com.example.ui.components.FormattedWildRiftText
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.TextPrimary
import com.example.util.tr
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription

@Composable
internal fun BuildMatchupList(
    group: String,
    title: String,
    candidates: List<String>,
    accent: Color,
    premium: Boolean,
    signedIn: Boolean,
    compact: Boolean,
    language: String,
    modifier: Modifier = Modifier,
    onShowName: (String) -> Unit
) {
    val rows = BuildChoiceRules.matchupRows(candidates, premium, signedIn)
    Card(
        modifier = modifier.testTag("${group}_insight_card"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.5f))
    ) {
        Column(Modifier.fillMaxWidth().padding(4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$title (${rows.sumOf { it.size }})", color = accent,
                    fontWeight = FontWeight.Bold, fontSize = if (compact) 12.sp else 13.sp)
                if (!premium) Text("PRO 12", color = HextechGold, fontSize = 10.sp)
            }
            rows.forEachIndexed { index, row ->
                Row(Modifier.fillMaxWidth().testTag("build_matchup_${group}_row_$index"),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    row.forEach { target ->
                        val champion = resolveTargetChampion(target)
                        val name = champion?.getLocalizedName(language) ?: tr(target)
                        Box(Modifier.weight(1f).widthIn(min = 48.dp).heightIn(min = 48.dp)
                            .testTag("build_matchup_name_$group")
                            .semantics { contentDescription = name }
                            .coachClickable { onShowName(name) },
                            contentAlignment = Alignment.Center) {
                            if (champion != null) ChampionAvatar(champion = champion,
                                size = if (compact) 32.dp else 36.dp,
                                showTierBadge = false, borderColor = accent)
                            else Text(tr(target), color = TextPrimary, fontSize = 11.sp,
                                textAlign = TextAlign.Center)
                        }
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f).height(48.dp)) }
                }
            }
        }
    }
}

private fun resolveTargetChampion(nameOrId: String): Champion? {
    val clean = nameOrId.trim()
    return WildRiftRepository.getChampionByName(clean)
        ?: WildRiftRepository.getChampionById(clean.lowercase().replace(" ", "_").replace("-", "_").replace("'", ""))
        ?: WildRiftRepository.champions.find {
            it.name.equals(clean, ignoreCase = true) ||
            it.id.equals(clean, ignoreCase = true) ||
            it.ddragonId.equals(clean, ignoreCase = true)
        }
}

@Composable
internal fun BuildElementCoachAdvice(advice: String, catalogDescription: String = "") {
    val distinct = com.example.util.BuildElementAdvice.distinctAdvice(advice, catalogDescription)
    if (distinct.isBlank()) return
    Spacer(Modifier.height(14.dp))
    androidx.compose.material3.Surface(modifier = Modifier.fillMaxWidth().testTag("build_element_advice_card"),
        shape = RoundedCornerShape(8.dp), color = HextechSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, HextechGold.copy(alpha = 0.5f))) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(tr("Consejo del coach"), color = HextechGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            FormattedWildRiftText(text = tr(distinct), color = TextPrimary, fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
internal fun AdaptiveDetailAlertDialog(
    isOverlay: Boolean,
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit
) {
    if (isOverlay) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.85f))
                .coachClickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onDismissRequest
                )
                .padding(12.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .coachClickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = com.example.ui.theme.HextechDarkBg),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, com.example.ui.theme.HextechGold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    title()
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(modifier = Modifier.weight(1f, fill = false)) {
                        text()
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        confirmButton()
                    }
                }
            }
        }
    } else {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismissRequest,
            title = title,
            text = text,
            confirmButton = confirmButton,
            containerColor = com.example.ui.theme.HextechSurface,
            titleContentColor = com.example.ui.theme.HextechGold,
            textContentColor = com.example.ui.theme.TextPrimary
        )
    }
}
