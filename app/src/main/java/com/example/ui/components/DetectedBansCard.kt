package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WildRiftRepository
import com.example.service.screen.*
import com.example.ui.theme.*
import com.example.util.currentAppLanguage
import com.example.util.tr

/** Ten fixed, compact positions. Each side keeps duplicate bans and unconfirmed/empty positions. */
@Composable
internal fun DetectedBansCard(slots: List<BanSlot>) {
    val language = currentAppLanguage()
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp).testTag("draft_detected_bans"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (side in BanSide.entries) {
            val sideKey = if (side == BanSide.ALLY) "ally" else "enemy"
            val color = if (side == BanSide.ALLY) HextechCyan else DangerRed
            Column(Modifier.weight(1f).testTag("draft_bans_$sideKey")) {
                Text(tr(if (side == BanSide.ALLY) "Baneos aliados" else "Baneos rivales"), color = color, fontSize = 8.sp, lineHeight = 10.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for (index in 0..4) {
                        val slot = slots.firstOrNull { it.side == side && it.index == index } ?: BanSlot(side, index)
                        val champion = if (slot.state == BanState.CONFIRMED)
                            WildRiftRepository.champions.firstOrNull { it.id == slot.championId } else null
                        val label = champion?.getLocalizedName(language) ?: tr(when(slot.state) {
                            BanState.EMPTY -> "Sin ban"
                            BanState.PENDING -> "Detectando"
                            else -> "Sin confirmar"
                        })
                        Column(Modifier.weight(1f).testTag("draft_ban_${sideKey}_$index")
                            .semantics { contentDescription = label }, horizontalAlignment = Alignment.CenterHorizontally) {
                            if (champion != null) ChampionAvatar(champion, size = 20.dp, showTierBadge = false)
                            else Box(Modifier.size(20.dp).background(HextechSurfaceVariant, CircleShape)
                                .border(.8.dp, color.copy(alpha = .4f), CircleShape), contentAlignment = Alignment.Center) {
                                Text(if(slot.state == BanState.EMPTY) "—" else if(slot.state == BanState.PENDING) "…" else "?",
                                    color = TextMuted, fontSize = 10.sp, lineHeight = 12.sp)
                            }
                            Text(label, color = TextSecondary, fontSize = 6.5.sp, lineHeight = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}
