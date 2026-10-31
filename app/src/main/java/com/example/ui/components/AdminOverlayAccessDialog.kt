package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    var visible by remember { mutableStateOf(true) }
    var activationEnabled by remember { mutableStateOf(true) }
    var allowed by remember { mutableStateOf<Set<String>>(emptySet()) }
    var accounts by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val result = runCatching { OverlayAccessPolicy.load() }
        result.onSuccess { snapshot ->
            visible = snapshot.hubButtonsVisible
            activationEnabled = snapshot.activationButtonEnabled
            allowed = snapshot.allowedAccountIds.ifEmpty { snapshot.allowedUserUids }
        }
            .onFailure { error = true }
        loading = false
    }
    DisposableEffect(Unit) {
        val registration = com.example.data.ManagedUsersDirectory.observe { state -> accounts = state.users }
        onDispose { registration.remove() }
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(color = HextechDarkBg, shape = MaterialTheme.shapes.large) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(tr("Acceso al visor"), color = HextechGold, style = MaterialTheme.typography.titleLarge)
                Text(tr("Controla los botones del visor dentro del Hub. El overlay flotante seguirá funcionando."), color = TextSecondary)
                if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                SwitchRow(tr("Mostrar botones del visor en el Hub"), visible, Modifier.testTag("overlay_access_buttons_visible")) { visible = it }
                SwitchRow(androidx.compose.ui.res.stringResource(com.example.R.string.overlay_activation_admin_switch), activationEnabled,
                    Modifier.testTag("overlay_activation_admin_switch")) { activationEnabled = it }
                Text(androidx.compose.ui.res.stringResource(com.example.R.string.overlay_activation_admin_description), color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall)
                Text(tr("Cuentas autorizadas (vacío = todas las cuentas)"), color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                LazyColumn(Modifier.fillMaxWidth().heightIn(max = 240.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(accounts, key = { it["uid"].toString() }) { account ->
                        val id = account["uid"].toString()
                        val label = (account["name"] ?: account["displayName"] ?: account["email"] ?: id).toString()
                        val email = (account["email"] as? String).orEmpty()
                        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Checkbox(checked = id in allowed, onCheckedChange = { checked -> allowed = if (checked) allowed + id else allowed - id }, enabled = !loading && !saving,
                                modifier = Modifier.testTag("overlay_account_$id"))
                            Column { Text(label, color = TextPrimary); if (email.isNotBlank() && email != label) Text(email, color = TextSecondary, style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
                if (error) Text(tr("No se pudo cargar la configuración."), color = DangerRed)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, enabled = !saving) { Text(tr("Cancelar")) }
                    Button(onClick = {
                        saving = true; error = false
                        scope.launch {
                            runCatching { OverlayAccessPolicy.save(OverlayAccessSnapshot(hubButtonsVisible = visible, allowedAccountIds = allowed,
                                activationButtonEnabled = activationEnabled)) }
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
