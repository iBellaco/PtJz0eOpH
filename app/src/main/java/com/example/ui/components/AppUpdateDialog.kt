package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.BuildConfig
import com.example.ui.theme.*
import com.example.util.AppUpdateInfo
import com.example.util.AppUpdateManager
import com.example.util.tr
import kotlinx.coroutines.launch

/** Blocking notice only: the user obtains the update through their usual channel. */
@Composable
fun AppUpdateDialog(updateInfo: AppUpdateInfo, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var checking by remember { mutableStateOf(false) }
    Dialog(onDismissRequest = { if (!updateInfo.isMandatory) onDismiss() }, properties = DialogProperties(
        dismissOnBackPress = !updateInfo.isMandatory, dismissOnClickOutside = !updateInfo.isMandatory)) {
        Surface(Modifier.fillMaxWidth().testTag("app_update_dialog"),
            color = HextechDarkBg, shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(if (updateInfo.isMandatory) tr("Actualización obligatoria") else context.getString(com.example.R.string.update_recommended), style = MaterialTheme.typography.titleLarge, color = HextechGold)
                Text(if (updateInfo.isMandatory) tr("La versión que estás usando es antigua. Actualiza Coach para continuar.") else context.getString(com.example.R.string.update_can_wait), color = TextPrimary)
                Text(tr("Versión instalada") + ": " + BuildConfig.VERSION_NAME + "\n" +
                    tr("Versión disponible") + ": " + updateInfo.latestVersionName, color = HextechCyan)
                Text(tr("Instala la nueva versión desde el mismo lugar donde recibiste Coach y vuelve a abrir la aplicación."), color = TextSecondary)
                CoachButton(onClick = {
                    scope.launch {
                        checking = true
                        try { AppUpdateManager.checkForUpdates(context, forceManualCheck = true) }
                        finally { checking = false }
                    }
                }, enabled = !checking, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    .testTag("recheck_app_version")) { Text(tr(if (checking) "Comprobando versión..." else "Volver a comprobar")) }
                if (!updateInfo.isMandatory) {
                    CoachButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .testTag("defer_app_update")) { Text(context.getString(com.example.R.string.update_later)) }
                }
            }
        }
    }
}
