package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Champion
import com.example.ui.theme.DangerRed
import com.example.ui.theme.TextSecondary
import com.example.util.currentAppLanguage
import com.example.util.tr

/** Confirmed bans are visible before the draft; these champions cannot enter recommendation candidates. */
@Composable
internal fun DetectedBansCard(champions: List<Champion>) {
    val language = currentAppLanguage()
    Column(Modifier.fillMaxWidth().testTag("draft_detected_bans")) {
        Text(tr("Campeones baneados"), color = DangerRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            champions.distinctBy { it.id }.forEach { champion ->
                Column {
                    ChampionAvatar(champion, size = 32.dp, showTierBadge = false)
                    Text(champion.getLocalizedName(language), color = TextSecondary, fontSize = 10.sp)
                }
            }
        }
    }
}
