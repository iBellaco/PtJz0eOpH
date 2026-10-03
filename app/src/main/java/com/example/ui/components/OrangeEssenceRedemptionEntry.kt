package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import com.example.util.tr

@Composable
fun OrangeEssenceRedemptionEntry(balance: Long, onClick: () -> Unit) {
    if (balance <= 0) return
    val accent = Color(0xFFFF9E1B)
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth().testTag("orange_redemption_entry"),
        color = HextechDarkBg, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, accent.copy(alpha = 0.65f))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = accent)
            Column(Modifier.weight(1f)) {
                Text(tr("Canjear Esencia Naranja"), color = accent, style = MaterialTheme.typography.titleSmall)
                Text(tr("Recibe USDT • Revisión manual de 24 a 72 horas"), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = accent)
        }
    }
}
