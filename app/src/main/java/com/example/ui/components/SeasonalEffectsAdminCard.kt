package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SeasonalDate
import com.example.data.SeasonalEffectsManager
import com.example.data.SeasonalEventSettings
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HextechCardBorder
import com.example.ui.theme.HextechCyan
import com.example.ui.theme.HextechDarkBg
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.util.tr
import kotlinx.coroutines.launch

/** Remote schedule editor for application-only seasonal decoration. */
@Composable
fun SeasonalEffectsAdminCard() {
    val published by SeasonalEffectsManager.config.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var staged by remember { mutableStateOf(published) }
    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    val genericSaveError = tr("No se pudieron guardar los efectos. Intenta de nuevo.")

    LaunchedEffect(Unit) { SeasonalEffectsManager.initialize() }
    LaunchedEffect(published) {
        if (!saving) staged = published
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("seasonal_effects_admin_card"),
        colors = CardDefaults.cardColors(containerColor = HextechSurface),
        border = BorderStroke(1.dp, HextechGold.copy(alpha = 0.72f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(tr("Efectos de temporada"), color = HextechGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(
                tr("Solo se muestran dentro de la aplicación; el asistente flotante no cambia. Cada teléfono usa su fecha y zona horaria local."),
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            SeasonalEventSettingsEditor(
                title = tr("Navidad"),
                description = tr("Nieve suave y destellos cálidos en toda la aplicación."),
                settings = staged.christmas,
                tagPrefix = "christmas",
                onChanged = { updated -> staged = staged.copy(christmas = updated) }
            )
            SeasonalEventSettingsEditor(
                title = tr("Año Nuevo"),
                description = tr("Confeti y destellos de celebración en toda la aplicación."),
                settings = staged.newYear,
                tagPrefix = "new_year",
                onChanged = { updated -> staged = staged.copy(newYear = updated) }
            )

            if (saveError != null) {
                Text(saveError!!, color = DangerRed, fontSize = 11.sp)
            }
            Button(
                onClick = {
                    scope.launch {
                        saving = true
                        saveError = null
                        runCatching { SeasonalEffectsManager.save(staged) }
                            .onFailure { saveError = genericSaveError }
                        saving = false
                    }
                },
                enabled = !saving,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("save_seasonal_effects"),
                colors = ButtonDefaults.buttonColors(containerColor = HextechGold, contentColor = HextechDarkBg)
            ) {
                if (saving) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp), color = HextechDarkBg, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(tr("Guardando…"), fontWeight = FontWeight.Bold)
                } else {
                    Text(tr("Guardar efectos"), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SeasonalEventSettingsEditor(
    title: String,
    description: String,
    settings: SeasonalEventSettings,
    tagPrefix: String,
    onChanged: (SeasonalEventSettings) -> Unit
) {
    var startText by remember(settings.start) { mutableStateOf(settings.start.asText()) }
    var endText by remember(settings.end) { mutableStateOf(settings.end.asText()) }
    val startInvalid = startText.isNotBlank() && SeasonalDate.parse(startText) == null
    val endInvalid = endText.isNotBlank() && SeasonalDate.parse(endText) == null
    val accent = if (settings.enabled) HextechCyan else TextMuted

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, accent.copy(alpha = 0.65f)), RoundedCornerShape(10.dp))
            .background(HextechDarkBg.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(description, color = TextMuted, fontSize = 10.sp, lineHeight = 13.sp)
            }
            Switch(
                checked = settings.enabled,
                onCheckedChange = { onChanged(settings.copy(enabled = it)) },
                modifier = Modifier.testTag("${tagPrefix}_seasonal_enabled"),
                colors = SwitchDefaults.colors(checkedTrackColor = HextechCyan)
            )
        }

        if (settings.enabled) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(tr("Programar por fecha local"), color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                    Text(tr("La programación respeta la fecha local de cada país."), color = TextMuted, fontSize = 9.5.sp)
                }
                Switch(
                    checked = settings.scheduled,
                    onCheckedChange = { onChanged(settings.copy(scheduled = it)) },
                    modifier = Modifier.testTag("${tagPrefix}_seasonal_schedule"),
                    colors = SwitchDefaults.colors(checkedTrackColor = HextechGold)
                )
            }
            if (settings.scheduled) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SeasonalDateField(
                        value = startText,
                        label = tr("Inicio (MM-DD)"),
                        isError = startInvalid,
                        tag = "${tagPrefix}_seasonal_start",
                        onValueChange = { value ->
                            startText = value
                            SeasonalDate.parse(value)?.let { onChanged(settings.copy(start = it)) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    SeasonalDateField(
                        value = endText,
                        label = tr("Fin (MM-DD)"),
                        isError = endInvalid,
                        tag = "${tagPrefix}_seasonal_end",
                        onValueChange = { value ->
                            endText = value
                            SeasonalDate.parse(value)?.let { onChanged(settings.copy(end = it)) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (startInvalid || endInvalid) {
                    Text(tr("Formato inválido. Usa MM-DD."), color = DangerRed, fontSize = 9.5.sp)
                }
            }
        }
    }
}

@Composable
private fun SeasonalDateField(
    value: String,
    label: String,
    isError: Boolean,
    tag: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.take(5)) },
        modifier = modifier
            .heightIn(min = 56.dp)
            .testTag(tag),
        label = { Text(label, fontSize = 10.sp) },
        singleLine = true,
        isError = isError,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = TextPrimary),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = HextechCyan,
            unfocusedBorderColor = HextechCardBorder,
            focusedLabelColor = HextechCyan
        )
    )
}
