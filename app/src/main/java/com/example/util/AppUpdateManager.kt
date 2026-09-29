package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AppUpdateInfo(
    val isUpdateAvailable: Boolean = false,
    val latestVersionCode: Int = BuildConfig.VERSION_CODE,
    val latestVersionName: String = BuildConfig.VERSION_NAME,
    val releaseNotes: String = "",
    val downloadUrl: String = "",
    val isMandatory: Boolean = false,
    val publishedAt: String = ""
)

/**
 * Gestor de verificación y descarga de actualizaciones en tiempo real para la aplicación.
 * Consulta la API de GitHub Releases.
 */
object AppUpdateManager {

    private const val TAG = "AppUpdateManager"
    private const val DEFAULT_REPO = "barbadiego695/wild-rift-drafting"
    private const val PREFS_NAME = "app_update_prefs"
    private const val KEY_SNOOZED_VERSION = "snoozed_version_code"

    private val _updateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    val updateInfo: StateFlow<AppUpdateInfo?> = _updateInfo.asStateFlow()

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Comprueba si hay una nueva versión disponible comparando versionCode y versionName.
     */
    suspend fun checkForUpdates(context: Context, forceManualCheck: Boolean = false): AppUpdateInfo {
        return withContext(Dispatchers.IO) {
            var result = AppUpdateInfo()

            // 1. Consultar GitHub Releases API directamente (rápido, sin dependencias de Play Services)
            try {
                val customRepo = context.getSharedPreferences("feedback_prefs", Context.MODE_PRIVATE)
                    .getString("github_repo", DEFAULT_REPO)?.trim() ?: DEFAULT_REPO
                val cleanRepo = customRepo.removePrefix("https://github.com/").removeSuffix("/")

                val request = Request.Builder()
                    .url("https://api.github.com/repos/$cleanRepo/releases/latest")
                    .header("Accept", "application/vnd.github.v3+json")
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyStr = response.body?.string() ?: ""
                        val json = JSONObject(bodyStr)
                        val tagName = json.optString("tag_name", "").removePrefix("v").trim()
                        val bodyNotes = json.optString("body", "Nuevas mejoras de balance y asistente flotante.")
                        val htmlUrl = json.optString("html_url", "https://github.com/$cleanRepo/releases/latest")

                        // Buscar asset APK si existe
                        var apkDownloadUrl = htmlUrl
                        val assets = json.optJSONArray("assets")
                        if (assets != null) {
                            for (i in 0 until assets.length()) {
                                val asset = assets.getJSONObject(i)
                                val name = asset.optString("name", "")
                                if (name.endsWith(".apk", ignoreCase = true)) {
                                    apkDownloadUrl = asset.optString("browser_download_url", htmlUrl)
                                    break
                                }
                            }
                        }

                        // Comprobar si la versión de GitHub es mayor
                        val isHigherVersion = compareVersionNames(tagName, BuildConfig.VERSION_NAME) > 0
                        if (isHigherVersion) {
                            result = AppUpdateInfo(
                                isUpdateAvailable = true,
                                latestVersionCode = BuildConfig.VERSION_CODE + 1,
                                latestVersionName = tagName,
                                releaseNotes = bodyNotes,
                                downloadUrl = apkDownloadUrl,
                                isMandatory = false,
                                publishedAt = json.optString("published_at", "")
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                AppLogger.d(TAG, "GitHub releases check: ${e.message}")
            }

            if (!forceManualCheck && result.isUpdateAvailable) {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val snoozedCode = prefs.getInt(KEY_SNOOZED_VERSION, -1)
                if (snoozedCode == result.latestVersionCode && !result.isMandatory) {
                    // Pospuesta, no mostrar modal intrusivo automáticamente
                    AppLogger.d(TAG, "Update ${result.latestVersionName} snoozed by user.")
                } else {
                    _updateInfo.value = result
                }
            } else if (forceManualCheck) {
                if (result.isUpdateAvailable) {
                    _updateInfo.value = result
                }
            } else if (result.isUpdateAvailable) {
                _updateInfo.value = result
            }

            result
        }
    }

    /**
     * Posponer la notificación de la versión actual.
     */
    fun snoozeUpdate(context: Context, versionCode: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_SNOOZED_VERSION, versionCode).apply()
        _updateInfo.value = null
    }

    /**
     * Descarga e inicia la instalación de la actualización abriendo el enlace directo del APK o release.
     */
    fun startUpdateDownload(context: Context, downloadUrl: String) {
        try {
            val finalUrl = if (downloadUrl.isNotBlank()) downloadUrl else "https://github.com/$DEFAULT_REPO/releases/latest"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Toast.makeText(context, com.example.util.appTr("Abriendo descarga de la actualización..."), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Fallo al abrir enlace de actualización", e)
            Toast.makeText(context, com.example.util.appTr("Error al abrir enlace: ${e.localizedMessage}"), Toast.LENGTH_LONG).show()
        }
    }

    fun dismissAlert() {
        _updateInfo.value = null
    }

    /**
     * Compara strings semánticos de versión tipo "1.20" vs "1.19".
     * Retorna > 0 si v1 > v2, < 0 si v1 < v2, 0 si son iguales.
     */
    private fun compareVersionNames(v1: String, v2: String): Int {
        val parts1 = v1.split(".").mapNotNull { it.trim().toIntOrNull() }
        val parts2 = v2.split(".").mapNotNull { it.trim().toIntOrNull() }
        val maxLen = maxOf(parts1.size, parts2.size)

        for (i in 0 until maxLen) {
            val num1 = parts1.getOrElse(i) { 0 }
            val num2 = parts2.getOrElse(i) { 0 }
            if (num1 != num2) {
                return num1.compareTo(num2)
            }
        }
        return 0
    }
}
