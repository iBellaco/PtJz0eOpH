package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ui.components.CoachButton as Button
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.TextMuted
import com.example.util.tr
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

@Composable
internal fun UserDeviceManagementCard(deviceCount: Int, onResetRequested: () -> Unit) {
    Surface(color = HextechSurface, shape = RoundedCornerShape(10.dp), border = androidx.compose.foundation.BorderStroke(1.dp, HextechCardBorder)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Devices, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(tr("Slots de Hardware y Dispositivos"), fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA), fontSize = 13.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text(tr("El usuario tiene $deviceCount de 2 slots de hardware vinculados. Si el usuario cambió de teléfono o tiene problemas de sesión, puedes liberar todos sus slots."), color = TextMuted, fontSize = 11.sp)
            Spacer(Modifier.height(10.dp))
            Button(onClick = onResetRequested, modifier = Modifier.fillMaxWidth().testTag("reset_user_device_slots"), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)), shape = RoundedCornerShape(8.dp)) {
                Icon(Icons.Default.RestartAlt, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(tr("Liberar / Reiniciar Todos los Slots de Hardware"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
