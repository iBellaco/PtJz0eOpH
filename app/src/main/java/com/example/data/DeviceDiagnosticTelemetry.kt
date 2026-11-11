package com.example.data

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.SystemClock
import kotlin.math.abs

/**
 * Lightweight session meter used only when the user explicitly sends a
 * diagnostic report. It does not keep a background polling loop: values are
 * sampled at session start and at the moment the report is sent.
 */
object DeviceDiagnosticTelemetry {
    data class Snapshot(
        val sessionDurationMs: Long = 0L,
        val batteryStartPercent: Int? = null,
        val batteryPercent: Int? = null,
        val batteryDrainPercent: Double? = null,
        val chargeStartMah: Double? = null,
        val chargeMah: Double? = null,
        val chargeDrainMah: Double? = null,
        val energyStartWh: Double? = null,
        val energyWh: Double? = null,
        val energyDrainWh: Double? = null,
        val currentMa: Double? = null,
        val voltageV: Double? = null,
        val powerW: Double? = null,
        val batteryHealth: String? = null
    )

    private data class Baseline(
        val startedAtElapsed: Long,
        val batteryPercent: Int?,
        val chargeMah: Double?,
        val energyWh: Double?
    )

    @Volatile private var baseline: Baseline? = null

    fun start(context: Context) {
        val state = readState(context)
        baseline = Baseline(SystemClock.elapsedRealtime(), state.percent, state.chargeMah, state.energyWh)
    }

    fun snapshot(context: Context): Snapshot {
        val state = readState(context)
        val start = baseline ?: Baseline(SystemClock.elapsedRealtime(), state.percent, state.chargeMah, state.energyWh)
        val duration = (SystemClock.elapsedRealtime() - start.startedAtElapsed).coerceAtLeast(0L)
        return Snapshot(
            sessionDurationMs = duration,
            batteryStartPercent = start.batteryPercent,
            batteryPercent = state.percent,
            batteryDrainPercent = percentDelta(start.batteryPercent, state.percent),
            chargeStartMah = start.chargeMah,
            chargeMah = state.chargeMah,
            chargeDrainMah = positiveDelta(start.chargeMah, state.chargeMah),
            energyStartWh = start.energyWh,
            energyWh = state.energyWh,
            energyDrainWh = positiveDelta(start.energyWh, state.energyWh),
            currentMa = state.currentMa,
            voltageV = state.voltageV,
            powerW = if (state.currentMa != null && state.voltageV != null) {
                state.currentMa * state.voltageV / 1000.0
            } else null,
            batteryHealth = state.health
        )
    }

    private data class State(
        val percent: Int?,
        val chargeMah: Double?,
        val energyWh: Double?,
        val currentMa: Double?,
        val voltageV: Double?,
        val health: String?
    )

    private fun readState(context: Context): State {
        val battery = runCatching {
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        }.getOrNull()
        val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percent = if (level >= 0 && scale > 0) (level * 100.0 / scale).coerceIn(0.0, 100.0).toInt() else null
        val manager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val chargeMah = manager?.longProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            ?.takeIf { it != Long.MIN_VALUE && it > 0L }
            ?.div(1000.0)
        val energyWh = manager?.longProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)
            ?.takeIf { it != Long.MIN_VALUE && it > 0L }
            ?.div(1_000_000_000.0)
        val currentMa = manager?.intProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            ?.takeIf { it != Int.MIN_VALUE && it != 0 }
            ?.let { abs(it).toDouble() / 1000.0 }
        val voltageV = battery?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
            ?.takeIf { it > 0 }
            ?.div(1000.0)
        val health = battery?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)?.let {
            when (it) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "buena"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "sobrecalentada"
                BatteryManager.BATTERY_HEALTH_DEAD -> "agotada"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "sobrevoltaje"
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "fallo no especificado"
                else -> null
            }
        }
        return State(percent, chargeMah, energyWh, currentMa, voltageV, health)
    }

    private fun BatteryManager.longProperty(id: Int): Long = if (Build.VERSION.SDK_INT >= 26) {
        runCatching { getLongProperty(id) }.getOrDefault(Long.MIN_VALUE)
    } else Long.MIN_VALUE

    private fun BatteryManager.intProperty(id: Int): Int =
        runCatching { getIntProperty(id) }.getOrDefault(Int.MIN_VALUE)

    private fun positiveDelta(start: Double?, current: Double?): Double? =
        if (start != null && current != null && start > current) (start - current).coerceAtLeast(0.0) else null

    private fun percentDelta(start: Int?, current: Int?): Double? =
        if (start != null && current != null && start > current) (start - current).toDouble() else null
}
