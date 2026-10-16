package com.example.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.example.model.Champion
import com.example.ui.theme.*
import com.example.util.championStatPercent

@Composable
fun ChampionRateLine(champion: Champion, includeWin: Boolean = false,
    includePick: Boolean = true, includeBan: Boolean = true) {
    val rates = buildList {
        if (includeWin) add(Triple("WR", championStatPercent(champion, champion.winrate, "wr"), HextechGold))
        if (includePick) add(Triple("Pick", championStatPercent(champion, champion.pickRate, "pick"), HextechCyan))
        if (includeBan) add(Triple("Ban", championStatPercent(champion, champion.banRate, "ban"), DangerRed))
    }
    Text(buildAnnotatedString {
        rates.forEachIndexed { index, (label, value, color) ->
            if (index > 0) withStyle(SpanStyle(color = TextMuted)) { append(" • ") }
            withStyle(SpanStyle(color = color)) { append("$label: $value") }
        }
    }, fontSize = 10.sp, maxLines = 1)
}
