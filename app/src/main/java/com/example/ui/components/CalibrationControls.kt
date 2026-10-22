package com.example.ui.components

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.R
import com.example.data.CalibrationReport
import com.example.data.FeedbackRepository
import com.example.service.screen.DraftVisionScanner
import com.example.service.screen.VisionCalibrationConfig
import com.example.util.AppLanguage
import com.example.util.localizedString
import com.example.util.tr
import kotlinx.coroutines.launch

/** Separate rows prevent size and step labels from competing with the directional pad. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalibrationControls(
    step: Float,
    onStep: (Float) -> Unit,
    onMove: (Float, Float, Float) -> Unit,
    onReset: () -> Unit,
    report: @Composable () -> Unit
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            CoachOutlinedButton(onClick = { onMove(0f, -step, 0f) }, modifier = Modifier.size(56.dp).testTag("vision_calibration_up"), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.KeyboardArrowUp, tr("Arriba")) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                CoachOutlinedButton(onClick = { onMove(-step, 0f, 0f) }, modifier = Modifier.size(56.dp).testTag("vision_calibration_left"), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.KeyboardArrowLeft, tr("Izquierda")) }
                Text("±${java.lang.String.format(java.util.Locale.US, "%.1f", step * 100)}%", modifier = Modifier.widthIn(min = 56.dp), fontSize = 13.sp)
                CoachOutlinedButton(onClick = { onMove(step, 0f, 0f) }, modifier = Modifier.size(56.dp).testTag("vision_calibration_right"), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.KeyboardArrowRight, tr("Derecha")) }
            }
            CoachOutlinedButton(onClick = { onMove(0f, step, 0f) }, modifier = Modifier.size(56.dp).testTag("vision_calibration_down"), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.KeyboardArrowDown, tr("Abajo")) }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CoachButton(onClick = { onMove(0f, 0f, step) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("calibration_size_increase")) { Text(tr("Agrandar"), fontSize = 14.sp) }
            CoachButton(onClick = { onMove(0f, 0f, -step) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("calibration_size_decrease")) { Text(tr("Reducir"), fontSize = 14.sp) }
        }
        Text(tr("Paso:"), fontSize = 14.sp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("0.1%" to .001f, "0.5%" to .005f, "1.0%" to .01f, "2.0%" to .02f).forEach { (label, value) ->
                CoachOutlinedButton(onClick = { onStep(value) }, modifier = Modifier.heightIn(min = 48.dp).testTag("calibration_step_${label.replace('.', '_').replace('%', 'p')}")) {
                    Text(if (step == value) "• $label" else label, fontSize = 14.sp)
                }
            }
        }
        report()
        CoachOutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("vision_calibration_reset_device")) { Text(tr("Restablecer"), fontSize = 14.sp) }
    }
}

@Composable
fun CalibrationSendButton() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sending by remember { mutableStateOf(false) }
    var sent by remember { mutableStateOf(false) }
    val send = localizedString(R.string.calibration_send)
    val pending = localizedString(R.string.calibration_sending)
    val success = localizedString(R.string.calibration_sent)
    val failure = localizedString(R.string.calibration_send_failed)
    val login = localizedString(R.string.calibration_verified_required)
    CoachButton(enabled = !sending, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("calibration_send_support"), onClick = {
        val account = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (account == null || !account.isEmailVerified) {
            Toast.makeText(context, login, Toast.LENGTH_LONG).show()
        } else if (!sending) {
            sending = true
            sent = false
            scope.launch {
                try {
                    val metrics = context.resources.displayMetrics
                    val snapshot = DraftVisionScanner.calibrationReportSnapshot()
                    val payload = CalibrationReport.build(
                        "${Build.MANUFACTURER} ${Build.MODEL}; API ${Build.VERSION.SDK_INT}",
                        "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                        "${metrics.widthPixels}x${metrics.heightPixels}; density=${metrics.density}; font=${context.resources.configuration.fontScale}; orientation=${context.resources.configuration.orientation}",
                        if (snapshot.width > 0) "${snapshot.width}x${snapshot.height}" else "not_observed",
                        snapshot.config.toKotlinCode()
                    )
                    val title = if (AppLanguage.current.value == "pt") "Coordenadas do scanner" else "Coordenadas del escáner"
                    val result = FeedbackRepository.submitFeedback("SOPORTE", title, payload, userName = account.displayName)
                    sent = result.isSuccess
                    Toast.makeText(context, if (sent) success else failure, Toast.LENGTH_LONG).show()
                } catch (cancelled: kotlinx.coroutines.CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    Toast.makeText(context, failure, Toast.LENGTH_LONG).show()
                } finally { sending = false }
            }
        }
    }) { Text(if (sending) pending else send, fontSize = 14.sp) }
    if (sent) Text(success, modifier = Modifier.testTag("calibration_send_confirmed"), fontSize = 13.sp)
}
