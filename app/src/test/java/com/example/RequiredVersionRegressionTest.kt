package com.example

import android.app.Application
import com.example.util.AppUpdateManager
import org.junit.Assert.*
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class RequiredVersionRegressionTest {
    private val context get() = RuntimeEnvironment.getApplication()
    @After fun clear() {
        context.getSharedPreferences("published_app_version", 0).edit()
            .putString("tag", "v${BuildConfig.VERSION_NAME}-b${BuildConfig.VERSION_CODE}").commit()
        AppUpdateManager.restore(context)
    }
    @Test fun `known newer release blocks offline and cannot be dismissed`() {
        context.getSharedPreferences("published_app_version", 0).edit()
            .putString("tag", "v1.1.99.999-b99999").commit()
        AppUpdateManager.restore(context)
        assertTrue(AppUpdateManager.updateInfo.value!!.isMandatory)
        assertEquals(99999, AppUpdateManager.updateInfo.value!!.latestVersionCode)
        assertEquals("", AppUpdateManager.updateInfo.value!!.downloadUrl)
        AppUpdateManager.dismissAlert()
        assertTrue(AppUpdateManager.updateInfo.value!!.isUpdateAvailable)
    }
    @Test fun `installed version clears an old cached mandatory notice`() {
        clear()
        assertNull(AppUpdateManager.updateInfo.value)
    }
}
