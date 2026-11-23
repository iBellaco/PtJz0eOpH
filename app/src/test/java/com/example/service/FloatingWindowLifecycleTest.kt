package com.example.service

import android.app.Activity
import androidx.compose.ui.platform.ComposeView
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.Text
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.Shadows.shadowOf
import android.os.Looper

@RunWith(RobolectricTestRunner::class)
// Exercise Android window/capture lifecycles without starting production sync workers.
@Config(sdk = [34], application = android.app.Application::class)
class FloatingWindowLifecycleTest {
    @Test fun resizingTheCapturedGameSwapsTheReaderAndReusesTheDisplayEvenWhileIdle() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val manager = com.example.service.screen.ScreenCaptureManager(context)
        val display = org.mockito.Mockito.mock(android.hardware.display.VirtualDisplay::class.java)
        val first = org.mockito.Mockito.mock(android.media.ImageReader::class.java)
        org.mockito.Mockito.`when`(first.width).thenReturn(1920)
        org.mockito.Mockito.`when`(first.height).thenReturn(869)
        fun field(name: String) = manager.javaClass.getDeclaredField(name).apply { isAccessible = true }
        field("virtualDisplay").set(manager, display)
        field("imageReader").set(manager, first)
        val resize = manager.javaClass.getDeclaredMethod("resizeCapture", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
            .apply { isAccessible = true }
        try {
            resize.invoke(manager, 0, 0)
            resize.invoke(manager, 2688, 1216)
            org.mockito.Mockito.verifyNoInteractions(display)
            resize.invoke(manager, 2880, 1800)
            val tablet = field("imageReader").get(manager) as android.media.ImageReader
            assertEquals(1728 to 1080, tablet.width to tablet.height)
            org.mockito.Mockito.verify(first).close()
            org.mockito.Mockito.verify(display).resize(1728, 1080, field("screenDensity").getInt(manager))
            org.mockito.Mockito.verifyNoMoreInteractions(display)
            resize.invoke(manager, 2880, 1800)
            assertSame(tablet, field("imageReader").get(manager))
            field("surfaceAttached").setBoolean(manager, true)
            resize.invoke(manager, 2000, 1200)
            val active = field("imageReader").get(manager) as android.media.ImageReader
            assertEquals(1800 to 1080, active.width to active.height)
            org.mockito.Mockito.verify(display).setSurface(active.surface)
        } finally { manager.release() }
    }

    @Test fun captureStopsProducingWhileIdleAndResumesWithoutNewConsent() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val power = context.getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
        shadowOf(power).setIsInteractive(true)
        val manager = com.example.service.screen.ScreenCaptureManager(context)
        val display = org.mockito.Mockito.mock(android.hardware.display.VirtualDisplay::class.java)
        val reader = org.mockito.Mockito.mock(android.media.ImageReader::class.java)
        val surface = org.mockito.Mockito.mock(android.view.Surface::class.java)
        org.mockito.Mockito.`when`(reader.surface).thenReturn(surface)
        fun field(name: String) = manager.javaClass.getDeclaredField(name).apply { isAccessible = true }
        field("virtualDisplay").set(manager, display)
        field("imageReader").set(manager, reader)
        val handler = field("handler").get(manager) as android.os.Handler
        try {
            assertNull(manager.captureCurrentFrame()) // Reattach; never return stale pixels.
            org.mockito.Mockito.verify(display).setSurface(surface)
            shadowOf(handler.looper).idleFor(java.time.Duration.ofMillis(1500))
            org.mockito.Mockito.verify(display).setSurface(null)
            assertNull(manager.captureCurrentFrame())
            org.mockito.Mockito.verify(display, org.mockito.Mockito.times(2)).setSurface(surface)
            shadowOf(power).setIsInteractive(false)
            assertNull(manager.captureCurrentFrame())
            shadowOf(handler.looper).idle()
            org.mockito.Mockito.verify(display, org.mockito.Mockito.times(2)).setSurface(null)
        } finally { manager.release() }
        org.mockito.Mockito.verify(display).release()
        org.mockito.Mockito.verify(reader).close()
    }

    @Test fun detachedWindowHasItsOwnOwnersAndCanComposeSavedState() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup()
        val owner = FloatingWindowLifecycleOwner()
        val view = ComposeView(activity.get())
        owner.attach(view)
        assertSame(owner, view.findViewTreeSavedStateRegistryOwner())
        assertSame(owner, view.findViewTreeViewModelStoreOwner())
        view.setContent { Text(rememberSaveable { mutableStateOf("Coach") }.value) }
        activity.get().setContentView(view)
        view.createComposition()
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue(view.hasComposition)
        owner.destroy()
        assertEquals(Lifecycle.State.DESTROYED, owner.lifecycle.currentState)
        assertFalse(view.hasComposition)
        owner.destroy()
        activity.pause().stop().destroy()
    }
}
