package com.example.ui.components

import androidx.compose.runtime.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.PremiumAccessPolicy
import com.example.util.localizedString

@Composable
fun PremiumStatusCard(role: String, secondary: String = "", until: Long?, adminClaim: Boolean = false,
    banned: Boolean = false, granted: Boolean = false, onRenew: (() -> Unit)? = null) {
    val lifetime = !banned && PremiumAccessPolicy.isLifetime(role, secondary, adminClaim)
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(until, lifetime, role, secondary, granted) {
        now = System.currentTimeMillis()
        if (!lifetime && until != null && until > now) while (now < until) {
            kotlinx.coroutines.delay(1000)
            now = System.currentTimeMillis()
        }
    }
    val active = PremiumAccessPolicy.isActive(role, until, now, secondary, adminClaim, banned, granted)
    val soon = active && !lifetime && PremiumAccessPolicy.isExpiringSoon(until, now)
    val expired = !lifetime && until != null && until > 0 && until <= now
    Surface(modifier = Modifier.fillMaxWidth().testTag("premium_status_card"), shape = RoundedCornerShape(10.dp),
        color = com.example.ui.theme.HextechDarkBg,
        border = BorderStroke(1.dp, if (soon || expired) Color(0xFFFBBF24) else com.example.ui.theme.HextechGold.copy(alpha = 0.4f))) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(localizedString(when { lifetime -> R.string.premium_lifetime_label; active -> R.string.premium_active_label;
                expired -> R.string.premium_expired_label; else -> R.string.premium_free_label }), color = com.example.ui.theme.HextechGold)
            if (active && !lifetime && until != null) Text(localizedString(R.string.premium_remaining_time,
                PremiumAccessPolicy.remaining(until, now)), color = Color.White, modifier = Modifier.testTag("premium_remaining_time"))
            if (soon) Text(localizedString(R.string.premium_expiry_warning), color = Color(0xFFFBBF24), modifier = Modifier.testTag("premium_expiry_warning"))
            if (onRenew != null && (soon || expired)) TextButton(onClick = onRenew, modifier = Modifier.testTag("premium_renew_button")) {
                Text(localizedString(R.string.premium_renew_label), color = com.example.ui.theme.HextechCyan)
            }
        }
    }
}
