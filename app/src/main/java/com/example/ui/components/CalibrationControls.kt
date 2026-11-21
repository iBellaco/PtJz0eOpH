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
import androidx.compose.ui.text.style.TextAlign
import com.example.R
import com.example.data.CalibrationReport
import com.example.data.CalibrationUserName
import com.example.data.FeedbackRepository
import com.example.service.screen.DraftVisionScanner
import com.example.service.screen.CalibrationEvidenceImage
import com.example.service.screen.CalibrationEvidenceFrame
import com.example.service.screen.ScreenCaptureManager
import com.example.service.screen.BanState
import com.example.service.screen.CalibrationReportSnapshot
import com.example.util.AppLanguage
import com.example.util.localizedString
import com.example.util.tr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Separate rows prevent size and step labels from competing with the directional pad. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalibrationControls(
    step: Float,
    onStep: (Float) -> Unit,
    onMove: (Float, Float, Float) -> Unit,
    onReset: () -> Unit,
    report: @Composable () -> Unit,
    onSave: (() -> Unit)? = null,
    saveTag: String = "calibration_save_device",
    directionTagPrefix: String = "vision_calibration",
    sizeTagPrefix: String = "calibration_size",
    stepTagPrefix: String = "calibration_step",
    resetTag: String = "vision_calibration_reset_device"
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            CoachOutlinedButton(onClick = { onMove(0f, -step, 0f) }, modifier = Modifier.size(56.dp).testTag("${directionTagPrefix}_up"), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.KeyboardArrowUp, tr("Arriba")) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                CoachOutlinedButton(onClick = { onMove(-step, 0f, 0f) }, modifier = Modifier.size(56.dp).testTag("${directionTagPrefix}_left"), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.KeyboardArrowLeft, tr("Izquierda")) }
                Text("±${java.lang.String.format(java.util.Locale.US, "%.1f", step * 100)}%", modifier = Modifier.widthIn(min = 56.dp), fontSize = 13.sp)
                CoachOutlinedButton(onClick = { onMove(step, 0f, 0f) }, modifier = Modifier.size(56.dp).testTag("${directionTagPrefix}_right"), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.KeyboardArrowRight, tr("Derecha")) }
            }
            CoachOutlinedButton(onClick = { onMove(0f, step, 0f) }, modifier = Modifier.size(56.dp).testTag("${directionTagPrefix}_down"), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.KeyboardArrowDown, tr("Abajo")) }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CoachButton(onClick = { onMove(0f, 0f, step) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("${sizeTagPrefix}_increase")) { Text(tr("Agrandar"), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 14.sp) }
            CoachButton(onClick = { onMove(0f, 0f, -step) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("${sizeTagPrefix}_decrease")) { Text(tr("Reducir"), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 14.sp) }
        }
        Text(tr("Paso:"), fontSize = 14.sp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("0.1%" to .001f, "0.5%" to .005f, "1.0%" to .01f, "2.0%" to .02f).forEach { (label, value) ->
                CoachOutlinedButton(onClick = { onStep(value) }, modifier = Modifier.heightIn(min = 48.dp).testTag("${stepTagPrefix}_${label.replace('.', '_').replace('%', 'p')}")) {
                    Text(if (step == value) "• $label" else label, fontSize = 14.sp)
                }
            }
        }
        onSave?.let { save ->
            CoachButton(
                onClick = save,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag(saveTag)
            ) {
                Icon(Icons.Default.Save, contentDescription = tr("Guardar coordenadas"))
                Spacer(Modifier.width(6.dp))
                Text(tr("Guardar coordenadas en este dispositivo"), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 13.sp)
            }
        }
        report()
        CoachOutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag(resetTag)) { Text(tr("Restablecer"), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 14.sp) }
    }
}

@Composable
fun CalibrationSendButton(
    screenCaptureManager: ScreenCaptureManager? = null,
    onBeforeSend: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sending by remember { mutableStateOf(false) }
    var sent by remember { mutableStateOf(false) }
    var limitReached by remember { mutableStateOf(false) }
    val prefs = remember { context.getSharedPreferences("calibration_reports", android.content.Context.MODE_PRIVATE) }
    val dayKey = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ROOT).format(java.util.Date()) }
    val sentToday = remember(dayKey) { prefs.getInt("sent_$dayKey", 0) }
    LaunchedEffect(sentToday) { limitReached = sentToday >= 5 }
    val send = localizedString(R.string.calibration_save_and_send)
    val pending = localizedString(R.string.calibration_sending)
    val success = localizedString(R.string.calibration_sent)
    val failure = localizedString(R.string.calibration_send_failed)
    val login = localizedString(R.string.calibration_verified_required)
    val dailyLimit = tr("Límite diario alcanzado")
    CoachButton(enabled = !sending, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("calibration_send_support"), onClick = {
        // Persist the device-specific calibration before attempting the cloud
        // report. The same action is intentionally used for both operations so
        // closing the overlay or losing connectivity never discards the local
        // coordinates.
        val saved = runCatching { onBeforeSend?.invoke() }.isSuccess
        if (!saved) {
            Toast.makeText(context, failure, Toast.LENGTH_LONG).show()
            return@CoachButton
        }
        if (limitReached) {
            Toast.makeText(context, dailyLimit, Toast.LENGTH_LONG).show()
            return@CoachButton
        }
        val account = runCatching { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }.getOrNull()
        if (account == null || !account.isEmailVerified) {
            Toast.makeText(context, login, Toast.LENGTH_LONG).show()
        } else if (!sending) {
            sending = true
            sent = false
            scope.launch {
                try {
                    val metrics = context.resources.displayMetrics
                    val displayName = CalibrationUserName.resolve(account)
                    val language = AppLanguage.current.value
                    val attachment = captureCalibrationAttachment(screenCaptureManager, language)
                    val snapshot = attachment.snapshot
                    val coordinates = snapshot.config.toFormattedCoordinatesString(snapshot.allyRolesBySlot)
                    val detectionEvidence = listOf(snapshot.detectionEvidenceReport(language), attachment.description(language))
                        .joinToString("\n")
                    val payload = CalibrationReport.build(
                        "${Build.MANUFACTURER} ${Build.MODEL}",
                        "",
                        "${metrics.widthPixels}x${metrics.heightPixels}",
                        "",
                        coordinates,
                        userName = displayName,
                        language = language,
                        androidVersion = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                        detectionEvidence = detectionEvidence
                    )
                    val title = CalibrationReport.title(
                        isPremium = com.example.util.SubscriptionManager.isPremium.value,
                        language = language
                    )
                    val result = FeedbackRepository.submitFeedback(
                        "COORDENADAS", title, payload,
                        imagesBase64 = listOfNotNull(attachment.image?.base64),
                        userName = displayName,
                        adminOnly = true,
                        writeUserInboxCopy = false
                    )
                    sent = result.isSuccess
                    if (sent) {
                        val count = prefs.getInt("sent_$dayKey", 0) + 1
                        prefs.edit().putInt("sent_$dayKey", count).apply()
                        limitReached = count >= 5
                    }
                    Toast.makeText(context, if (sent) success else failure, Toast.LENGTH_LONG).show()
                } catch (cancelled: kotlinx.coroutines.CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    Toast.makeText(context, failure, Toast.LENGTH_LONG).show()
                } finally {
                    sending = false
                }
            }
        }
    }) { Text(if (limitReached) dailyLimit else if (sending) pending else send, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = 14.sp) }
    if (sent) Text(success, modifier = Modifier.testTag("calibration_send_confirmed"), fontSize = 13.sp)
    if (limitReached) Text(tr("Puedes enviar hasta cinco informes de coordenadas al día."), fontSize = 12.sp)
}

/** Obtiene una captura literal reciente sin iniciar un bucle de monitoreo. */
internal suspend fun captureLiteralEvidenceFrame(manager: ScreenCaptureManager?): CalibrationEvidenceFrame? {
    if (manager == null || !manager.isReady()) return null
    repeat(4) {
        val capturedAt = android.os.SystemClock.elapsedRealtime()
        val bitmap = withContext(Dispatchers.Default) { manager.captureCurrentFrame() }
        if (bitmap != null && !bitmap.isRecycled && bitmap.width > 0 && bitmap.height > 0) {
            return CalibrationEvidenceFrame(bitmap, capturedAt)
        }
        delay(90L)
    }
    return null
}

internal data class CalibrationAttachment(
    val snapshot: CalibrationReportSnapshot,
    val image: CalibrationEvidenceImage.Encoded?,
    val fromScanner: Boolean = false
) {
    fun description(language: String): String {
        val pt = language.equals("pt", ignoreCase = true)
        val viewport = snapshot.viewport
        val geometry = "${snapshot.width}x${snapshot.height}, viewport=${viewport.left},${viewport.top}-${viewport.right},${viewport.bottom}"
        val bans = snapshot.bans.count { it.state == BanState.CONFIRMED }
        return buildString {
            if (image != null) {
                val source = if (fromScanner) {
                    if (pt) "Último fotograma processado pelo scanner" else "Último fotograma procesado por el escáner"
                } else {
                    if (pt) "Captura de tela no momento do envio" else "Captura de pantalla en el momento del envío"
                }
                appendLine("$source · ${image.width}x${image.height} · ${image.ageMs} ms · SHA-256=${image.sha256}")
            } else {
                appendLine(if (pt) "Nenhum fotograma disponível." else "No había un fotograma disponible.")
            }
            appendLine("Perfil: ${snapshot.source} · $geometry")
            append(if (pt) "Posições com identidade=${snapshot.picks.size} · banimentos confirmados=$bans"
                else "Posiciones con identidad=${snapshot.picks.size} · baneos confirmados=$bans")
        }
    }
}

internal suspend fun captureCalibrationAttachment(manager: ScreenCaptureManager?, language: String): CalibrationAttachment =
    withContext(Dispatchers.Default) {
        fun encode(frame: CalibrationEvidenceFrame, fromScanner: Boolean): CalibrationAttachment? {
            try {
                val bitmap = frame.bitmap
                val landscape = bitmap.width >= bitmap.height
                val snapshot = frame.snapshot ?: DraftVisionScanner.snapshotForLiteralCapture(bitmap)
                val image = CalibrationEvidenceImage.encode(frame, snapshot.config, visorActive = landscape,
                    slotLabels = snapshot.slotLabels(language)) ?: return null
                return CalibrationAttachment(snapshot, image, fromScanner)
            } finally {
                frame.bitmap.recycle()
            }
        }

        captureLiteralEvidenceFrame(manager)?.let { encode(it, fromScanner = false) }
            ?: DraftVisionScanner.calibrationEvidenceFrame()?.let { encode(it, fromScanner = true) }
            ?: CalibrationAttachment(DraftVisionScanner.calibrationReportSnapshot(), null)
    }
