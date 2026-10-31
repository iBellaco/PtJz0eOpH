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
@Config(sdk = [34])
class FloatingWindowLifecycleTest {
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
