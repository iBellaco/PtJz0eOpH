package com.example.ui.components

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.*
import com.example.service.screen.DraftVisionScanner
import com.example.service.screen.ScreenCaptureManager
import kotlinx.coroutines.launch

/** Explicit, one-shot report. Does not sample continuously or capture game content. */
@Composable
fun DeviceDiagnosticButton(screenCaptureManager: ScreenCaptureManager? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sending by remember { mutableStateOf(false) }
    var lastSent by remember { mutableLongStateOf(0L) }
    var quota by remember(context) { mutableStateOf(DeviceDiagnosticQuota.usage(context)) }
    val label = androidx.compose.ui.res.stringResource(com.example.R.string.device_diagnostic)
    LaunchedEffect(Unit) {
        DeviceDiagnosticTelemetry.start(context)
        quota = DeviceDiagnosticQuota.usage(context)
    }
    val buttonDescription = context.getString(
        com.example.R.string.diagnostic_button_description,
        quota.remaining,
        DeviceDiagnosticQuota.MAX_PER_DAY
    )
    IconButton(enabled = !sending, modifier = Modifier.size(48.dp).testTag("send_device_diagnostic"), onClick = {
        if (android.os.SystemClock.elapsedRealtime() - lastSent < 60_000 && lastSent != 0L) return@IconButton
        val reservation = DeviceDiagnosticQuota.tryReserve(context)
        if (reservation == null) {
            quota = DeviceDiagnosticQuota.usage(context)
            Toast.makeText(context, context.getString(com.example.R.string.diagnostic_limit_reached), Toast.LENGTH_LONG).show()
            return@IconButton
        }
        quota = DeviceDiagnosticQuota.usage(context)
        sending = true
        scope.launch {
            try {
                val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                check(user != null && user.isEmailVerified)
                val name = CalibrationUserName.resolve(user)
                val battery = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                val memory = ActivityManager.MemoryInfo()
                (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(memory)
                val power = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                val metrics = context.resources.displayMetrics
                val scannerSize = DraftVisionScanner.alignedFrameSizeFlow.value
                val batterySession = DeviceDiagnosticTelemetry.snapshot(context)
                val lastFrameAge = DraftVisionScanner.lastFrameAtMillis.value.takeIf { it > 0L }?.let {
                    (android.os.SystemClock.elapsedRealtime() - it).coerceAtLeast(0L)
                }
                val report = buildString {
                    appendLine("Usuario: $name")
                    appendLine("Coach: ${com.example.BuildConfig.VERSION_NAME}")
                    appendLine("Dispositivo: ${Build.MANUFACTURER} ${Build.MODEL}")
                    appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                    appendLine("Pantalla: ${metrics.widthPixels}x${metrics.heightPixels}; density=${metrics.density}; font=${context.resources.configuration.fontScale}")
                    appendLine("Captura: ${scannerSize.first}x${scannerSize.second}")
                    appendLine("Captura lista: ${screenCaptureManager?.isReady() == true}; último frame: ${DraftVisionScanner.lastFrameWidth.value}x${DraftVisionScanner.lastFrameHeight.value}; antigüedad: ${lastFrameAge?.let { "${it} ms" } ?: "N/D"}")
                    appendLine("Escáner: ${"%.2f".format(java.util.Locale.US, DraftVisionScanner.liveScanFps.value)} FPS; promedio: ${"%.2f".format(java.util.Locale.US, DraftVisionScanner.fpsAverage.value)}; rango: ${"%.2f".format(java.util.Locale.US, DraftVisionScanner.fpsMinimum.value)}-${"%.2f".format(java.util.Locale.US, DraftVisionScanner.fpsMaximum.value)} FPS")
                    appendLine("Frames recibidos/procesados: ${DraftVisionScanner.framesReceivedCount.value}/${DraftVisionScanner.framesProcessedCount.value}")
                    appendLine("Procesamiento último/promedio/mín-máx: ${DraftVisionScanner.lastProcessingDurationMs.value}/${DraftVisionScanner.processingAverageDurationMs.value}/${DraftVisionScanner.processingMinDurationMs.value}-${DraftVisionScanner.processingMaxDurationMs.value} ms")
                    appendLine("Fotogramas omitidos: ${DraftVisionScanner.framesSkippedCount.value}; procesando: ${DraftVisionScanner.isVisionEngineBusy.value}")
                    appendLine("Calibración: ${DraftVisionScanner.calibrationSourceFlow.value}; FPS del juego / latencia de red: no medidos")
                    appendLine("Diagnósticos enviados hoy: ${quota.used}/${DeviceDiagnosticQuota.MAX_PER_DAY}")
                    val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                    val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                    appendLine("Batería: ${if (level >= 0 && scale > 0) "${level * 100 / scale}%" else "N/D"}; carga conectada: ${(battery?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0) != 0}")
                    appendLine("Temperatura batería: ${battery?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)?.takeIf { it >= 0 }?.div(10.0) ?: "N/D"} °C")
                    appendLine("Salud batería: ${batterySession.batteryHealth ?: "N/D"}")
                    appendLine("Sesión diagnóstico: ${batterySession.sessionDurationMs / 1000}s")
                    appendLine("Batería inicio/actual: ${batterySession.batteryStartPercent?.let { "$it%" } ?: "N/D"}/${batterySession.batteryPercent?.let { "$it%" } ?: "N/D"}; consumo observado: ${batterySession.batteryDrainPercent?.let { "%.2f%%".format(java.util.Locale.US, it) } ?: "N/D"}")
                    appendLine("Carga acumulada inicio/actual: ${batterySession.chargeStartMah?.let { "%.1f mAh".format(java.util.Locale.US, it) } ?: "N/D"}/${batterySession.chargeMah?.let { "%.1f mAh".format(java.util.Locale.US, it) } ?: "N/D"}; consumo real medido: ${batterySession.chargeDrainMah?.let { "%.1f mAh".format(java.util.Locale.US, it) } ?: "N/D"}")
                    appendLine("Energía inicio/actual: ${batterySession.energyStartWh?.let { "%.3f Wh".format(java.util.Locale.US, it) } ?: "N/D"}/${batterySession.energyWh?.let { "%.3f Wh".format(java.util.Locale.US, it) } ?: "N/D"}; consumo: ${batterySession.energyDrainWh?.let { "%.3f Wh".format(java.util.Locale.US, it) } ?: "N/D"}")
                    appendLine("Corriente/voltaje/potencia instantánea: ${batterySession.currentMa?.let { "%.0f mA".format(java.util.Locale.US, it) } ?: "N/D"}/${batterySession.voltageV?.let { "%.2f V".format(java.util.Locale.US, it) } ?: "N/D"}/${batterySession.powerW?.let { "%.2f W".format(java.util.Locale.US, it) } ?: "N/D"}")
                    appendLine("Ahorro batería: ${power.isPowerSaveMode}")
                    if (Build.VERSION.SDK_INT >= 29) appendLine("Estado térmico Android: ${power.currentThermalStatus}")
                    appendLine("RAM disponible/total: ${memory.availMem / 1048576}/${memory.totalMem / 1048576} MiB; memoria baja: ${memory.lowMemory}")
                    appendLine("Memoria proceso: ${android.os.Debug.getPss()} KiB")
                    appendLine("Medición puntual; escáner pausado puede conservar la última duración registrada.")
                }
                FeedbackRepository.submitFeedback("DIAGNOSTICO", label, report, userName = name,
                    adminOnly = true, writeUserInboxCopy = false).getOrThrow()
                lastSent = android.os.SystemClock.elapsedRealtime()
                quota = DeviceDiagnosticQuota.usage(context)
                Toast.makeText(context, context.getString(com.example.R.string.diagnostic_sent_with_remaining, quota.remaining), Toast.LENGTH_LONG).show()
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                DeviceDiagnosticQuota.rollback(context, reservation)
                quota = DeviceDiagnosticQuota.usage(context)
                throw cancelled
            } catch (_: Exception) {
                DeviceDiagnosticQuota.rollback(context, reservation)
                quota = DeviceDiagnosticQuota.usage(context)
                Toast.makeText(context, context.getString(com.example.R.string.calibration_send_failed), Toast.LENGTH_LONG).show()
            } finally { sending = false }
        }
    }) {
        if (sending) CircularProgressIndicator(Modifier.size(20.dp))
        else Icon(Icons.Default.Speed, contentDescription = buttonDescription, modifier = Modifier.size(20.dp))
    }
}
