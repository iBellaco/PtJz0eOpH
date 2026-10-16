package com.example.util

import android.content.Context
import com.example.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
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

/** Only verifies the production release. There is no downloader or installer. */
data class VersionCheckStatus(val lastAttemptAt: Long = 0, val lastSuccessAt: Long = 0,
    val publishedName: String = "")

object AppUpdateManager {
    private const val PREFS = "published_app_version"
    private const val ENDPOINT = "https://api.github.com/repos/iBellaco/PtJz0eOpH/releases/latest"
    private val mutex = Mutex()
    private var lastAttemptAt = 0L
    private val _updateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    val updateInfo = _updateInfo.asStateFlow()
    private val _checkStatus = MutableStateFlow(VersionCheckStatus())
    val checkStatus = _checkStatus.asStateFlow()
    private val _verificationFinished = MutableStateFlow(false)
    val verificationFinished = _verificationFinished.asStateFlow()
    private val _verificationEnabled = MutableStateFlow(true)
    val verificationEnabled = _verificationEnabled.asStateFlow()
    private var settingLoaded = false
    private val client = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS).callTimeout(20, TimeUnit.SECONDS).build()

    fun restore(context: Context) {
        if (!_verificationEnabled.value) return
        val prefs = context.getSharedPreferences(PREFS, 0)
        val version = PublishedAppVersion.parse(prefs.getString("tag", "").orEmpty()) ?: return
        publish(version)
    }

    private fun publish(version: PublishedAppVersion) {
        if (!_verificationEnabled.value) { _updateInfo.value = null; return }
        _updateInfo.value = if (version.newerThan(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE))
            AppUpdateInfo(true, version.code, version.name, isMandatory = true) else null
    }

    suspend fun checkForUpdates(context: Context, forceManualCheck: Boolean = false): AppUpdateInfo = withContext(Dispatchers.IO) {
        mutex.lock()
        try {
            refreshVerificationSetting()
            if (!_verificationEnabled.value) {
                _updateInfo.value = null
                _verificationFinished.value = true
                return@withContext AppUpdateInfo()
            }
            restore(context)
            val now = System.currentTimeMillis()
            if (!forceManualCheck && now - lastAttemptAt < 5 * 60_000L)
                return@withContext _updateInfo.value ?: AppUpdateInfo()
            lastAttemptAt = now
            _checkStatus.value = _checkStatus.value.copy(lastAttemptAt = now)
            val request = Request.Builder().url(ENDPOINT).cacheControl(CacheControl.FORCE_NETWORK)
                .header("Accept", "application/vnd.github+json").build()
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val release = JSONObject(response.body?.string().orEmpty())
                        val version = PublishedAppVersion.parse(release.optString("tag_name"))
                        val assets = release.optJSONArray("assets")
                        val hasApk = assets != null && (0 until assets.length()).any {
                            assets.optJSONObject(it)?.optString("name")?.endsWith(".apk") == true
                        }
                        if (version != null && hasApk && !release.optBoolean("draft") && !release.optBoolean("prerelease")) {
                            context.getSharedPreferences(PREFS, 0).edit()
                                .putString("tag", release.getString("tag_name")).apply()
                            publish(version)
                            _checkStatus.value = VersionCheckStatus(now, now, version.name)
                        }
                    }
                }
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (_: Exception) {
                // Keep a previously verified required update while offline.
            }
            _updateInfo.value ?: AppUpdateInfo()
        } finally { _verificationFinished.value = true; mutex.unlock() }
    }

    suspend fun refreshVerificationSetting(): Boolean {
        val enabled = runCatching {
            FirebaseFirestore.getInstance().collection("system_config")
                .document("app_update_verification").get().await()
                .getBoolean("enabled") ?: true
        }.getOrElse { if (settingLoaded) _verificationEnabled.value else true }
        settingLoaded = true
        _verificationEnabled.value = enabled
        if (!enabled) _updateInfo.value = null
        return enabled
    }

    suspend fun setVerificationEnabled(context: Context, enabled: Boolean) {
        FirebaseFirestore.getInstance().collection("system_config")
            .document("app_update_verification")
            .set(mapOf("enabled" to enabled, "updatedAt" to com.google.firebase.Timestamp.now()))
            .await()
        settingLoaded = true
        _verificationEnabled.value = enabled
        _verificationFinished.value = !enabled
        if (!enabled) _updateInfo.value = null else checkForUpdates(context, forceManualCheck = true)
    }

    fun dismissAlert() { if (_updateInfo.value?.isMandatory != true) _updateInfo.value = null }
}
