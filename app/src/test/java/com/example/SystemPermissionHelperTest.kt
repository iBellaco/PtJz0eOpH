package com.example

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import com.example.util.SystemPermissionHelper
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class SystemPermissionHelperTest {
    private class SettingsContext(private val accept: (Intent) -> Boolean) :
        ContextWrapper(RuntimeEnvironment.getApplication()) {
        val attempted = mutableListOf<Intent>()
        override fun startActivity(intent: Intent) {
            attempted += intent
            if(!accept(intent)) throw ActivityNotFoundException("OEM screen absent")
        }
    }

    @Test fun overlayOpensPackageSettingsWithoutGrantingPermission() {
        val context=SettingsContext { true }
        SystemPermissionHelper.openOverlaySettings(context)
        assertEquals(1,context.attempted.size)
        val intent=context.attempted.single()
        assertEquals(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,intent.action)
        assertEquals("package:${context.packageName}",intent.data.toString())
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
        assertFalse(SystemPermissionHelper.hasOverlayPermission(context))
    }

    @Test fun missingOemOverlayScreensFallBackToAppInfo() {
        val context=SettingsContext { it.action==Settings.ACTION_APPLICATION_DETAILS_SETTINGS }
        SystemPermissionHelper.openOverlaySettings(context)
        assertEquals(listOf(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Settings.ACTION_APPLICATION_DETAILS_SETTINGS),
            context.attempted.map { it.action })
        assertNull(context.attempted[1].data)
        assertEquals("package:${context.packageName}",context.attempted.last().data.toString())
    }

    @Test fun appInfoIsExplicitAndHasSafeFallbackWhenEveryScreenIsMissing() {
        val context=SettingsContext { false }
        SystemPermissionHelper.openApplicationDetails(context)
        assertEquals(listOf(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Settings.ACTION_SETTINGS),
            context.attempted.map { it.action })
        SystemPermissionHelper.openOverlaySettings(context) // must not crash
        assertEquals(Settings.ACTION_SETTINGS,context.attempted.last().action)
    }

    @Test fun devicePolicyCannotBeBypassedAndDoesNotCrash() {
        val context=SettingsContext { throw SecurityException("Device administrator restriction") }
        SystemPermissionHelper.openOverlaySettings(context)
        assertEquals(4,context.attempted.size)
        assertFalse(SystemPermissionHelper.hasOverlayPermission(context))
    }

    @Test fun guidanceUsesSelectedLanguageAndIncludesTrustWarning() {
        val app=RuntimeEnvironment.getApplication()
        for((language,trustWord) in listOf("es" to "confías","pt" to "confiar")) {
            val config=Configuration(app.resources.configuration).apply { setLocale(Locale(language)) }
            val text=app.createConfigurationContext(config).getString(R.string.overlay_restricted_help)
            assertTrue(text,text.contains(trustWord))
            assertTrue(text,text.contains("APK"))
            assertTrue(text,text.contains("⋮"))
        }
    }
}
