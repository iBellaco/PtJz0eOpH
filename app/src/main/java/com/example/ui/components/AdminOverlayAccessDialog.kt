package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.OverlayAccessPolicy
import com.example.data.OverlayAccessSnapshot
import com.example.ui.theme.*
import com.example.util.tr
import kotlinx.coroutines.launch

@Composable
fun AdminOverlayAccessDialog(onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    var enabled by remember { mutableStateOf(true) }
    var allowed by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val result = runCatching { OverlayAccessPolicy.load() }
        result.onSuccess { enabled = it.enabled; allowed = it.allowedUserUids.joinToString("\n") }
            .onFailure { error = true }
        loading = false
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(color = HextechDarkBg, shape = MaterialTheme.shapes.large) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(tr("Acceso al visor"), color = HextechGold, style = MaterialTheme.typography.titleLarge)
                Text(tr("Deshabilita el visor y el botón flotante o limita su uso a UID concretos."), color = TextSecondary)
                if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                SwitchRow(tr("Visor habilitado"), enabled, Modifier.testTag("overlay_access_enabled")) { enabled = it }
                OutlinedTextField(
                    value = allowed,
                    onValueChange = { allowed = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 110.dp).testTag("overlay_access_allowed_uids"),
                    label = { Text(tr("UID autorizados (uno por línea)")) },
                    placeholder = { Text(tr("Vacío = todos los usuarios")) },
                    enabled = !loading && !saving
                )
                if (error) Text(tr("No se pudo cargar la configuración."), color = DangerRed)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, enabled = !saving) { Text(tr("Cancelar")) }
                    Button(onClick = {
                        saving = true; error = false
                        scope.launch {
                            runCatching { OverlayAccessPolicy.save(OverlayAccessSnapshot(enabled, allowed.lines().map(String::trim).filter(String::isNotBlank).toSet())) }
                                .onFailure { error = true }.onSuccess { onDismiss() }
                            saving = false
                        }
                    }, enabled = !loading && !saving, modifier = Modifier.heightIn(min = 48.dp).testTag("overlay_access_save")) {
                        Text(tr(if (saving) "Guardando…" else "Guardar"))
                    }
                }
            }
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, modifier: Modifier = Modifier, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextPrimary, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = modifier)
    }
}
