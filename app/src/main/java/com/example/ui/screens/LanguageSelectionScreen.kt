package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechGoldLight
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.util.AppLogger
import com.example.util.tr

@Composable
fun LanguageSelectionScreen(onLanguageSelected: (String) -> Unit) {
    var selectedLang by remember { mutableStateOf("es") }

    val screenTitle = if (selectedLang == "pt") "Escolha seu idioma" else "Elige tu idioma"
    val screenSubtitle = if (selectedLang == "pt") "Selecione o idioma do assistente tático" else "Selecciona el idioma del asistente táctico"
    val buttonText = if (selectedLang == "pt") "Continuar em Português" else "Continuar en Español"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Language,
            contentDescription = null,
            tint = HextechGold,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = screenTitle,
            color = HextechGold,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = screenSubtitle,
            color = TextMuted,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        LanguageOption(
            title = "Español",
            subtitle = "Español Oficial (Latinoamérica / España) • 100% Activo",
            flagEmoji = "🇪🇸",
            isSelected = selectedLang == "es",
            isEnabled = true,
            onClick = { selectedLang = "es" }
        )

        LanguageOption(
            title = "Português",
            subtitle = "Português (Brasil / Portugal) • ⚠️ Em Manutenção",
            flagEmoji = "🇧🇷",
            isSelected = false,
            isEnabled = false,
            onClick = { }
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = {
                AppLogger.d("LANG", "Selected Language: $selectedLang")
                onLanguageSelected(selectedLang)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HextechGold)
        ) {
            Text(
                text = buttonText,
                color = HextechDarkBg,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun LanguageOption(
    title: String,
    subtitle: String,
    flagEmoji: String,
    isSelected: Boolean,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    val borderColor = when {
        isSelected -> HextechCyan
        !isEnabled -> HextechCardBorder.copy(alpha = 0.3f)
        else -> HextechSurface
    }
    val bgColor = when {
        isSelected -> HextechCyan.copy(alpha = 0.12f)
        !isEnabled -> HextechSurface.copy(alpha = 0.5f)
        else -> HextechSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = isEnabled) { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = flagEmoji,
            fontSize = 26.sp,
            modifier = Modifier.alpha(if (isEnabled) 1.0f else 0.4f)
        )
        Spacer(modifier = Modifier.size(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (isEnabled) TextPrimary else TextMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = if (isEnabled) HextechGoldLight else TextMuted.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = HextechCyan,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
