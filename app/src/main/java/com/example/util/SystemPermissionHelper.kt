package com.example.util

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.service.FloatingAssistantService

object SystemPermissionHelper {

    /**
     * Comprueba si la aplicación tiene permiso para mostrarse sobre otras aplicaciones (SYSTEM_ALERT_WINDOW).
     */
    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    /**
     * Abre la pantalla de ajustes del sistema para conceder el permiso de superposición.
     */
    fun openOverlaySettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        openFirstAvailable(context, listOf(intent,
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION),
            applicationDetailsIntent(context), Intent(Settings.ACTION_SETTINGS)))
    }

    /** Android restricted settings require an explicit action by the owner.
     * Opening this screen never grants a permission or bypasses a device policy. */
    fun openApplicationDetails(context: Context) {
        openFirstAvailable(context,listOf(applicationDetailsIntent(context),Intent(Settings.ACTION_SETTINGS)))
    }

    private fun applicationDetailsIntent(context: Context) = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))

    private fun openFirstAvailable(context: Context, intents: List<Intent>) {
        for(intent in intents) {
            try {
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                return
            } catch (_: android.content.ActivityNotFoundException) {
                // OEMs may omit the package-specific or global settings screen.
            } catch (_: SecurityException) {
                // Managed-device policy can disallow this destination.
            }
        }
    }

    /**
     * Comprueba si la aplicación está exenta de la optimización de ahorro de batería de Android.
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.applicationContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(context.applicationContext.packageName) ?: false
    }

    fun hasStoragePermission(context: Context): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            true // MediaStore and system pickers do not require broad gallery access.
        } else {
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Solicita al usuario desactivar el ahorro de batería para evitar que el sistema cierre
     * el asistente flotante en segundo plano mientras se ejecuta Wild Rift.
     * Compatible con Android 14, 15 y Android 16 (API 34/35/36).
     */
    @SuppressLint("BatteryLife")
    fun requestIgnoreBatteryOptimization(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val appDetailsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(appDetailsIntent)
            } catch (_: Exception) {
                try {
                    val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(fallback)
                } catch (_: Exception) {
                    try {
                        val generalSettings = Intent(Settings.ACTION_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(generalSettings)
                    } catch (_: Exception) {}
                }
            }
        }
    }

    /**
     * Inicia el servicio en primer plano para la superposición flotante sobre el juego.
     */
    fun startFloatingService(context: Context): Boolean {
        val intent = Intent(context, FloatingAssistantService::class.java).apply {
            action = FloatingAssistantService.ACTION_START
        }
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
            true
        } catch (e: RuntimeException) {
            com.example.util.AppLogger.e("SystemPermissionHelper", "Android rechazó el inicio del servicio", e)
            false
        }
    }

    /**
     * Detiene el servicio en primer plano de la superposición flotante.
     */
    fun stopFloatingService(context: Context) {
        // stopService no intenta lanzar un servicio desde segundo plano en Android 12+.
        context.stopService(Intent(context, FloatingAssistantService::class.java))
    }

    /**
     * Comprueba si el servicio está activo.
     */
    @Suppress("DEPRECATION")
    fun isServiceRunning(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (FloatingAssistantService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }
}
