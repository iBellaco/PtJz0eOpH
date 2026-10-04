package com.example

import android.os.Bundle
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.util.DeviceAndSessionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceIdentityInstalledTest {
    @Test fun hardwareIdentityIsIndependentOfInstallationData() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val hardware = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        assertTrue(!hardware.isNullOrBlank())
        val id = DeviceAndSessionManager.getDeviceId(context)
        assertEquals("WRD_DEVICE_${hardware.lowercase(java.util.Locale.ROOT)}", id)
        InstrumentationRegistry.getInstrumentation().sendStatus(0, Bundle().apply { putString("coach_device_identity", id) })
    }
}
